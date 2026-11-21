// 6813115 Renuka Choosingkae
// 6813238 Napat Chonsaktrakul
// 6813248 Pimpichcha Saengngoen
// 6813258 Sittichok Boonthittanont
package JavaProject3_TowerDefense;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.*;

// =====================================================================
//  GamePanel : game loop thread + game logic + mouse/key input + drawing
// =====================================================================
public class GamePanel extends JPanel implements Runnable {
    private static final int CELL = GameConstants.CELL, COLS = GameConstants.COLS, ROWS = GameConstants.ROWS;
    private static final int MAP_W = GameConstants.MAP_W, MAP_H = GameConstants.MAP_H;
    private static final long FRAME_MS = 15;
    private static final double MAX_DT = 0.05, FIRST_SPAWN_DELAY = 0.3, END_DELAY = 1.8, FLASH_TIME = 0.4;

    private static final Color VALID_TILE   = new Color(0, 255, 0, 90);
    private static final Color INVALID_TILE = new Color(255, 0, 0, 90);
    private static final Color SELL_TILE    = new Color(255, 0, 0, 120);

    private final GameFrame frame;
    private final MapData map;
    private final Object lock = new Object();          // shared by the game thread and the Swing thread

    private final ArrayList<Mob> mobs = new ArrayList<Mob>();
    private final ArrayList<Tower> towers = new ArrayList<Tower>();
    private final ArrayList<Projectile> projectiles = new ArrayList<Projectile>();
    private final ArrayList<Effect> effects = new ArrayList<Effect>();
    private final Tower[][] towerGrid = new Tower[COLS][ROWS];
    private ArrayList<MobType> spawnQueue = new ArrayList<MobType>();

    private int hp = GameConstants.MAX_HP, coins = GameConstants.START_COINS;
    private int wave = 0, wavesCompleted = 0, kills = 0, remainingInWave = 0;
    private double spawnTimer = 0, baseFlash = 0, endDelay = 0;
    private boolean waveRunning = false, gameOver = false, victory = false, started = false;

    private volatile boolean running = false, paused = false, sellMode = false;
    private volatile int speed = 1, hoverCol = -1, hoverRow = -1, mouseX, mouseY;
    private volatile TowerType selectedType = null;
    private Thread gameThread;
    private String lastHudKey = "";

    public GamePanel(GameFrame frame, MapData map) {
        this.frame = frame;
        this.map = map;
        setPreferredSize(new Dimension(MAP_W, MAP_H));
        setFocusable(true);

        // ---------- MouseEvent ----------
        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) { handleClick(e); }
            public void mouseExited(MouseEvent e) { hoverCol = -1; hoverRow = -1; }
        });
        addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseMoved(MouseEvent e) {
                mouseX = e.getX();
                mouseY = e.getY();
                hoverCol = mouseX / CELL;
                hoverRow = mouseY / CELL;
            }
        });
        // ---------- KeyEvent ----------
        addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) { handleKey(e.getKeyCode()); }
        });
    }

    // ================= thread =================
    public void startGameLoop() {
        running = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    public void stopGameLoop() {
        running = false;
        if (gameThread != null) gameThread.interrupt();
    }

    public void run() {
        long last = System.nanoTime();
        boolean finished = false;
        while (running && !finished) {
            long now = System.nanoTime();
            double dt = Math.min(MAX_DT, (now - last) / 1e9);
            last = now;

            synchronized (lock) {
                if (!paused) {
                    if (!gameOver) {
                        for (int i = 0; i < speed && !gameOver; i++) update(dt);   // x2 = update twice
                    } else {
                        updateEffects(dt);
                        endDelay -= dt;
                        finished = (endDelay <= 0);
                    }
                    pushHud();
                }
            }
            repaint();
            try { Thread.sleep(FRAME_MS); } catch (InterruptedException ex) { return; }
        }
        if (finished) {
            final boolean win = victory;
            SwingUtilities.invokeLater(new Runnable() {
                public void run() { frame.showEndGame(win); }
            });
        }
    }

    // ================= game logic =================
    private void update(double dt) {
        if (baseFlash > 0) baseFlash -= dt;
        spawnMobs(dt);
        for (Tower t : towers) t.update(dt, mobs, projectiles);
        updateProjectiles(dt);
        updateMobs(dt);
        updateEffects(dt);

        if (waveRunning) {
            if (hp <= 0) endGame(false);
            else if (spawnQueue.isEmpty() && mobs.isEmpty()) waveCleared();
        }
    }

    private void spawnMobs(double dt) {
        if (!waveRunning || spawnQueue.isEmpty()) return;
        spawnTimer -= dt;
        if (spawnTimer > 0) return;

        MobType t = spawnQueue.remove(0);
        mobs.add(new Mob(t, map));
        spawnTimer = t.getSpawnCooldown();
        if (t.isBoss()) {
            banner(t.getName(map.isSpecial()).toUpperCase() + " INCOMING!", GameConstants.RED);
            SoundManager.playSFX("boss");
        }
    }

    private void updateProjectiles(double dt) {
        Iterator<Projectile> it = projectiles.iterator();
        while (it.hasNext()) {
            Projectile p = it.next();
            p.update(dt, mobs, effects);
            if (p.isDone()) it.remove();
        }
    }

    private void updateMobs(double dt) {
        Iterator<Mob> it = mobs.iterator();
        while (it.hasNext()) {
            Mob m = it.next();
            m.update(dt);
            if (m.getHp() <= 0) {
                killMob(m);
                it.remove();
            } else if (m.hasReachedBase()) {
                mobReachedBase(m);
                it.remove();
            }
        }
    }

    private void updateEffects(double dt) {
        Iterator<Effect> it = effects.iterator();
        while (it.hasNext()) {
            Effect ef = it.next();
            ef.update(dt);
            if (ef.isDone()) it.remove();
        }
    }

    // killed mob -> coins only (base HP can NOT be recovered)
    private void killMob(Mob m) {
        int coin = m.getType().getCoinDrop();
        kills++;
        remainingInWave--;
        addCoins(coin);
        effects.add(Effect.death(m.getX(), m.getY()));
        popup(m.getX(), m.getY() - 10, "+" + coin + "c", GameConstants.GOLD);
        SoundManager.playSFX("death");
    }

    private void mobReachedBase(Mob m) {
        int dmg = m.getType().getBaseDamage();
        hp = Math.max(0, hp - dmg);
        remainingInWave--;
        baseFlash = FLASH_TIME;
        popup(m.getX() - 20, m.getY() - 20, "-" + dmg + " HP", GameConstants.RED);
        SoundManager.playSFX("hit");
        m.setAlive(false);
    }

    private void waveCleared() {
        waveRunning = false;
        wavesCompleted = wave;
        int bonus = GameConstants.WAVE_BONUS + wave * GameConstants.WAVE_BONUS_PER_WAVE;
        addCoins(bonus);
        projectiles.clear();
        if (wave >= GameConstants.MAX_WAVE) {
            endGame(true);
            return;
        }
        banner("WAVE " + wave + " CLEARED!  +" + bonus + "c", GameConstants.GOLD);
        SoundManager.playSFX("clear");
        frame.setWaveRunning(false);
        frame.refreshTowerList(wave + 1);
    }

    private void endGame(boolean win) {
        gameOver = true;
        victory = win;
        waveRunning = false;
        endDelay = END_DELAY;
        banner(win ? "VICTORY!" : "DEFEAT", win ? GameConstants.GOLD : GameConstants.RED);
        SoundManager.playSFX(win ? "victory" : "defeat");
    }

    private void addCoins(int n) { coins = Math.min(GameConstants.MAX_COINS, coins + n); }

    // send values to the HUD only when something changed
    private void pushHud() {
        int shown = waveRunning ? remainingInWave : WaveData.totalMobs(wave + 1);
        String key = hp + "|" + wave + "|" + shown + "|" + coins + "|" + kills + "|" + waveRunning;
        if (!key.equals(lastHudKey)) {
            lastHudKey = key;
            frame.updateHud(hp, wave, shown, coins, kills, waveRunning);
        }
    }

    public void startWave() {
        synchronized (lock) {
            if (gameOver || paused || wave >= GameConstants.MAX_WAVE) return;
            if (waveRunning) {
                popup(MAP_W / 2.0, 40, "Wave already running!", GameConstants.RED);
                return;
            }
            wave++;
            started = true;
            spawnQueue = WaveData.buildQueue(wave);
            remainingInWave = spawnQueue.size();
            spawnTimer = FIRST_SPAWN_DELAY;
            waveRunning = true;
            banner("WAVE " + wave, Color.WHITE);
            SoundManager.playSFX("wave");
            pushHud();
        }
        frame.setWaveRunning(true);
    }

    // ================= input =================
    private void handleKey(int code) {
        if (code == KeyEvent.VK_P) frame.openPause();
        else if (code == KeyEvent.VK_SPACE) startWave();
        else if (code == KeyEvent.VK_ESCAPE) frame.clearSelection();
        else if (code == KeyEvent.VK_S) frame.toggleSell();
        else if (code >= KeyEvent.VK_1 && code <= KeyEvent.VK_4) frame.selectTowerByKey(code - KeyEvent.VK_1);
    }

    private void handleClick(MouseEvent e) {
        requestFocusInWindow();
        if (SwingUtilities.isRightMouseButton(e)) {
            frame.clearSelection();
            return;
        }
        int col = e.getX() / CELL, row = e.getY() / CELL;
        if (!isOnMap(col, row)) return;
        double px = e.getX(), py = e.getY();
        TowerType type = selectedType;

        synchronized (lock) {
            if (gameOver || paused) return;
            if (waveRunning) {                       // no changes during a wave
                showError(px, py, "Wave in progress!");
                return;
            }
            Tower t = towerGrid[col][row];
            if (sellMode) sellTower(t, col, row, px, py);
            else if (t != null) upgradeTower(t, px, py);
            else if (type != null) placeTower(type, col, row, px, py);
            pushHud();
        }
    }

    private void placeTower(TowerType type, int col, int row, double px, double py) {
        if (map.isPath(col, row)) {
            showError(px, py, "Not allowed");
            return;
        }
        int cost = type.getCost(1);
        if (!trySpend(cost, px, py)) return;
        Tower t = new Tower(type, col, row);
        towers.add(t);
        towerGrid[col][row] = t;
        popup(px, py, "-" + cost + "c", GameConstants.GOLD);
        SoundManager.playSFX("place");
    }

    private void upgradeTower(Tower t, double px, double py) {
        if (t.isMaxLevel()) {
            popup(px, py, "MAX LEVEL", Color.WHITE);
            return;
        }
        if (!trySpend(t.getUpgradeCost(), px, py)) return;
        t.upgrade();
        popup(px, py, "Level " + t.getLevel() + "!", GameConstants.GOLD);
        SoundManager.playSFX("upgrade");
    }

    private void sellTower(Tower t, int col, int row, double px, double py) {
        if (t == null) {
            popup(px, py, "No tower here", Color.WHITE);
            return;
        }
        int refund = t.getRefund();
        addCoins(refund);
        towers.remove(t);
        towerGrid[col][row] = null;
        effects.add(Effect.death(t.getCenterX(), t.getCenterY()));
        popup(px, py, "+" + refund + "c", GameConstants.GOLD);
        SoundManager.playSFX("sell");
    }

    // pays the cost if possible, otherwise shows "Can't afford"
    private boolean trySpend(int cost, double px, double py) {
        if (coins < cost) {
            showError(px, py, "Can't afford");
            return false;
        }
        coins -= cost;
        return true;
    }

    private void showError(double x, double y, String text) {
        popup(x, y, text, GameConstants.RED);
        SoundManager.playSFX("error");
    }

    private void popup(double x, double y, String text, Color c) {
        effects.add(Effect.text(x, y, text, c));
    }

    private void banner(String text, Color c) {
        effects.add(Effect.banner(MAP_W / 2.0, MAP_H / 2.0, text, c));
    }

    private boolean isOnMap(int col, int row) {
        return col >= 0 && col < COLS && row >= 0 && row < ROWS;
    }

    // ================= setters / getters =================
    public void setPaused(boolean p) { paused = p; repaint(); }
    public void setSpeed(int s) { speed = s; }
    public void setSelectedTower(TowerType t) { selectedType = t; }
    public void setSellMode(boolean on) { sellMode = on; }
    public boolean isGameOver() { return gameOver; }
    public boolean hasStarted() { return started; }
    public int getWavesCompleted() { return wavesCompleted; }
    public int getKills() { return kills; }

    // ================= drawing =================
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        synchronized (lock) {
            MapDraw.draw(g2, map, CELL);
            if (!waveRunning && (selectedType != null || sellMode)) MapDraw.drawGrid(g2, CELL);
            for (Tower t : towers) t.draw(g2);
            drawHover(g2);
            for (Mob m : mobs) m.draw(g2);
            for (Projectile p : projectiles) p.draw(g2);
            for (Effect ef : effects) ef.draw(g2);

            if (baseFlash > 0) drawBaseFlash(g2);
            if (!waveRunning && !gameOver) drawHint(g2);
            drawTooltip(g2);
            if (paused) {
                g2.setColor(GameConstants.OVERLAY);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        }
    }

    // red border when the base is hit
    private void drawBaseFlash(Graphics2D g) {
        int alpha = (int) (Math.min(1, baseFlash / FLASH_TIME) * 120);
        g.setColor(new Color(255, 0, 0, alpha));
        g.setStroke(new BasicStroke(10));
        g.drawRect(5, 5, MAP_W - 10, MAP_H - 10);
        g.setStroke(new BasicStroke(1));
    }

    private void drawHover(Graphics2D g) {
        int c = hoverCol, r = hoverRow;
        if (!isOnMap(c, r) || gameOver) return;
        int x = c * CELL, y = r * CELL;
        Tower t = towerGrid[c][r];

        if (sellMode && !waveRunning) {
            if (t != null) {
                g.setColor(SELL_TILE);
                g.fillRect(x, y, CELL, CELL);
            }
            return;
        }
        if (t != null) {
            drawRange(g, t.getCenterX(), t.getCenterY(), t.getRangePixels(), Color.WHITE);
            return;
        }
        TowerType sel = selectedType;
        if (sel == null || waveRunning) return;
        boolean ok = !map.isPath(c, r) && coins >= sel.getCost(1);
        g.setColor(ok ? VALID_TILE : INVALID_TILE);
        g.fillRect(x, y, CELL, CELL);
        drawRange(g, x + CELL / 2.0, y + CELL / 2.0, sel.getRange(1) * CELL, ok ? Color.WHITE : GameConstants.RED);
    }

    private void drawRange(Graphics2D g, double cx, double cy, double radius, Color c) {
        int r = (int) radius, x = (int) cx - r, y = (int) cy - r;
        g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 40));
        g.fillOval(x, y, r * 2, r * 2);
        g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 170));
        g.setStroke(new BasicStroke(2));
        g.drawOval(x, y, r * 2, r * 2);
        g.setStroke(new BasicStroke(1));
    }

    private void drawHint(Graphics2D g) {
        String s = (wave == 0)
                ? "Build towers, then press SPACE (or Start Wave) to begin Wave 1"
                : "Wave " + (wave + 1) + " is ready - press SPACE to start";
        g.setFont(GameConstants.TEXT_FONT);
        FontMetrics fm = g.getFontMetrics();
        int w = fm.stringWidth(s) + 30, x = (MAP_W - w) / 2;
        g.setColor(GameConstants.OVERLAY);
        g.fillRoundRect(x, 8, w, 30, 14, 14);
        g.setColor(Color.WHITE);
        g.drawString(s, x + 15, 29);
    }

    private void drawTooltip(Graphics2D g) {
        int c = hoverCol, r = hoverRow;
        if (!isOnMap(c, r) || gameOver) return;
        Tower t = towerGrid[c][r];
        if (t == null) return;

        TowerType ty = t.getType();
        int lv = t.getLevel();
        String[] lines;
        if (sellMode) {
            lines = new String[]{ty.getName() + " Lv." + lv, "Click to sell: +" + t.getRefund() + "c"};
        } else {
            lines = new String[]{
                ty.getName() + " Lv." + lv,
                "Dmg " + ty.getDamage(lv) + "  |  " + ty.getRate(lv) + " hit/s  |  Range " + ty.getRange(lv),
                ty.getSkill(lv),
                t.isMaxLevel() ? "MAX LEVEL" : "Click to upgrade: " + t.getUpgradeCost() + "c",
                "Sell value: " + t.getRefund() + "c"
            };
        }

        g.setFont(GameConstants.SMALL_FONT);
        FontMetrics fm = g.getFontMetrics();
        int w = 0;
        for (String s : lines) w = Math.max(w, fm.stringWidth(s));
        w += 16;
        int h = lines.length * 18 + 10;
        int x = mouseX + 16, y = mouseY + 10;
        if (x + w > MAP_W) x = mouseX - w - 10;
        if (y + h > MAP_H) y = mouseY - h - 10;

        g.setColor(GameConstants.TOOLTIP_BG);
        g.fillRoundRect(x, y, w, h, 10, 10);
        g.setColor(GameConstants.GOLD);
        g.drawRoundRect(x, y, w, h, 10, 10);
        g.setColor(Color.WHITE);
        for (int i = 0; i < lines.length; i++) g.drawString(lines[i], x + 8, y + 20 + i * 18);
    }
}