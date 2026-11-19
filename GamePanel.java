// Group members: 6xxxxxxx Firstname Lastname 1, 6xxxxxxx Firstname Lastname 2,
//                6xxxxxxx Firstname Lastname 3, 6xxxxxxx Firstname Lastname 4,
//                6xxxxxxx Firstname Lastname 5
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

    private final GameFrame frame;
    private final MapData map;
    private final Object lock = new Object();          // shared by game thread and Swing thread

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
                mouseX = e.getX(); mouseY = e.getY();
                hoverCol = e.getX() / CELL; hoverRow = e.getY() / CELL;
            }
        });
        // ---------- KeyEvent ----------
        addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) { handleKey(e); }
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
        boolean notifyEnd = false;
        while (running) {
            long now = System.nanoTime();
            double dt = (now - last) / 1e9;
            last = now;
            if (dt > 0.05) dt = 0.05;

            synchronized (lock) {
                if (!paused) {
                    if (!gameOver) {
                        for (int i = 0; i < speed && !gameOver; i++) update(dt);   // x2 = update twice
                    } else {
                        updateEffects(dt);
                        endDelay -= dt;
                        if (endDelay <= 0) notifyEnd = true;
                    }
                    pushHud();
                }
            }
            repaint();
            if (notifyEnd) {
                running = false;
                final boolean v = victory;
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() { frame.showEndGame(v); }
                });
                break;
            }
            try { Thread.sleep(15); } catch (InterruptedException ex) { break; }
        }
    }

    // ================= game logic =================
    private void update(double dt) {
        if (baseFlash > 0) baseFlash -= dt;

        // 1) spawn mobs in order
        if (waveRunning && !spawnQueue.isEmpty()) {
            spawnTimer -= dt;
            if (spawnTimer <= 0) {
                MobType t = spawnQueue.remove(0);
                mobs.add(new Mob(t, map, map.isSpecial()));
                spawnTimer = t.getSpawnCooldown();
                if (t.isBoss()) {
                    effects.add(Effect.banner(MAP_W / 2.0, MAP_H / 2.0,
                            t.getName(map.isSpecial()).toUpperCase() + " INCOMING!", GameConstants.RED));
                    SoundManager.playSFX("boss");
                }
            }
        }

        // 2) towers shoot
        for (Tower t : towers) t.update(dt, mobs, projectiles);

        // 3) projectiles fly
        Iterator<Projectile> pit = projectiles.iterator();
        while (pit.hasNext()) {
            Projectile p = pit.next();
            p.update(dt, mobs, effects);
            if (p.isDone()) pit.remove();
        }

        // 4) mobs walk / die / hit base
        Iterator<Mob> mit = mobs.iterator();
        while (mit.hasNext()) {
            Mob m = mit.next();
            m.update(dt);
            if (m.getHp() <= 0) {
                killMob(m);
                m.setAlive(false);
                mit.remove();
            } else if (m.hasReachedBase()) {
                hp = Math.max(0, hp - m.getType().getBaseDamage());
                remainingInWave--;
                baseFlash = 0.4;
                effects.add(Effect.text(m.getX() - 20, m.getY() - 20, "-" + m.getType().getBaseDamage() + " HP", GameConstants.RED));
                SoundManager.playSFX("hit");
                m.setAlive(false);
                mit.remove();
            }
        }

        // 5) effects
        updateEffects(dt);

        // 6) wave end check
        if (waveRunning) {
            if (hp <= 0) endGame(false);
            else if (spawnQueue.isEmpty() && mobs.isEmpty()) waveCleared();
        }
    }

    private void updateEffects(double dt) {
        Iterator<Effect> eit = effects.iterator();
        while (eit.hasNext()) {
            Effect ef = eit.next();
            ef.update(dt);
            if (ef.isDone()) eit.remove();
        }
    }

    // killed mob -> coins only (base HP can NOT be recovered)
    private void killMob(Mob m) {
        MobType t = m.getType();
        kills++;
        remainingInWave--;
        addCoins(t.getCoinDrop());
        effects.add(Effect.death(m.getX(), m.getY(), t.getColor()));
        effects.add(Effect.text(m.getX(), m.getY() - 10, "+" + t.getCoinDrop() + "c", GameConstants.GOLD));
        SoundManager.playSFX("death");
    }

    private void waveCleared() {
        waveRunning = false;
        wavesCompleted = wave;
        int bonus = GameConstants.WAVE_BONUS + wave * GameConstants.WAVE_BONUS_PER_WAVE;
        addCoins(bonus);
        projectiles.clear();
        if (wave >= GameConstants.MAX_WAVE) { endGame(true); return; }
        effects.add(Effect.banner(MAP_W / 2.0, MAP_H / 2.0, "WAVE " + wave + " CLEARED!  +" + bonus + "c", GameConstants.GOLD));
        SoundManager.playSFX("clear");
        frame.setWaveRunning(false);
        frame.refreshTowerList(wave + 1);
    }

    private void endGame(boolean win) {
        gameOver = true;
        victory = win;
        waveRunning = false;
        endDelay = 1.8;
        effects.add(Effect.banner(MAP_W / 2.0, MAP_H / 2.0, win ? "VICTORY!" : "DEFEAT", win ? GameConstants.GOLD : GameConstants.RED));
        SoundManager.playSFX(win ? "victory" : "defeat");
    }

    private void addCoins(int n) { coins = Math.min(GameConstants.MAX_COINS, coins + n); }

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
            if (gameOver || paused) return;
            if (waveRunning) {
                effects.add(Effect.text(MAP_W / 2.0, 40, "Wave already running!", GameConstants.RED));
                return;
            }
            if (wave >= GameConstants.MAX_WAVE) return;
            wave++;
            started = true;
            spawnQueue = WaveData.buildQueue(wave);
            remainingInWave = spawnQueue.size();
            spawnTimer = 0.3;
            waveRunning = true;
            sellMode = false;
            effects.add(Effect.banner(MAP_W / 2.0, MAP_H / 2.0, "WAVE " + wave, Color.WHITE));
            SoundManager.playSFX("wave");
            pushHud();
        }
        frame.setWaveRunning(true);
    }

    // ================= input =================
    private void handleKey(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_P) frame.openPause();
        else if (code == KeyEvent.VK_SPACE) startWave();
        else if (code == KeyEvent.VK_ESCAPE) frame.clearSelection();
        else if (code == KeyEvent.VK_S) frame.toggleSell();
        else if (code >= KeyEvent.VK_1 && code <= KeyEvent.VK_4) frame.selectTowerByKey(code - KeyEvent.VK_1);
    }

    private void handleClick(MouseEvent e) {
        requestFocusInWindow();
        if (SwingUtilities.isRightMouseButton(e)) { frame.clearSelection(); return; }
        int col = e.getX() / CELL, row = e.getY() / CELL;
        if (col < 0 || col >= COLS || row < 0 || row >= ROWS) return;
        double px = e.getX(), py = e.getY();

        synchronized (lock) {
            if (gameOver || paused) return;
            Tower t = towerGrid[col][row];
            if (waveRunning) {                       // no changes during a wave
                if (t != null || selectedType != null || sellMode) {
                    popup(px, py, "Wave in progress!", GameConstants.RED);
                    SoundManager.playSFX("error");
                }
                return;
            }
            if (sellMode) sellTower(t, col, row, px, py);
            else if (t != null) upgradeTower(t, px, py);
            else if (selectedType != null) placeTower(col, row, px, py);
            pushHud();
        }
    }

    private void placeTower(int col, int row, double px, double py) {
        TowerType type = selectedType;
        if (map.isPath(col, row)) {
            popup(px, py, "Not allowed", GameConstants.RED);
            SoundManager.playSFX("error");
            return;
        }
        int cost = type.getCost(1);
        if (coins < cost) {
            popup(px, py, "Can't afford", GameConstants.RED);
            SoundManager.playSFX("error");
            return;
        }
        coins -= cost;
        Tower t = new Tower(type, col, row);
        towers.add(t);
        towerGrid[col][row] = t;
        popup(px, py, "-" + cost + "c", GameConstants.GOLD);
        SoundManager.playSFX("place");
    }

    private void upgradeTower(Tower t, double px, double py) {
        if (t.getLevel() >= 3) {
            popup(px, py, "MAX LEVEL", Color.WHITE);
            return;
        }
        int cost = t.getUpgradeCost();
        if (coins < cost) {
            popup(px, py, "Can't afford", GameConstants.RED);
            SoundManager.playSFX("error");
            return;
        }
        coins -= cost;
        t.upgrade();
        popup(px, py, "Level " + t.getLevel() + "!", GameConstants.GOLD);
        effects.add(Effect.iceBurst(t.getCenterX(), t.getCenterY()));
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
        effects.add(Effect.death(t.getCenterX(), t.getCenterY(), Color.LIGHT_GRAY));
        popup(px, py, "+" + refund + "c", GameConstants.GOLD);
        SoundManager.playSFX("sell");
    }

    private void popup(double x, double y, String text, Color c) {
        effects.add(Effect.text(x, y, text, c));
    }

    // ================= setters / getters =================
    public void setPaused(boolean p) { paused = p; repaint(); }
    public void setSpeed(int s) { speed = s; }
    public void setSelectedTower(TowerType t) { selectedType = t; if (t != null) sellMode = false; }
    public void setSellMode(boolean on) { sellMode = on; if (on) selectedType = null; }
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

            if (baseFlash > 0) {                                  // red screen border when base is hit
                g2.setColor(new Color(255, 0, 0, (int) (Math.min(1, baseFlash / 0.4) * 120)));
                g2.setStroke(new BasicStroke(10));
                g2.drawRect(5, 5, MAP_W - 10, MAP_H - 10);
                g2.setStroke(new BasicStroke(1));
            }
            if (!waveRunning && !gameOver) drawHint(g2);
            drawTooltip(g2);
            if (paused) {
                g2.setColor(new Color(0, 0, 0, 140));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setFont(GameConstants.TITLE_FONT);
                FontMetrics fm = g2.getFontMetrics();
                g2.setColor(Color.WHITE);
                g2.drawString("PAUSED", (getWidth() - fm.stringWidth("PAUSED")) / 2, getHeight() / 2);
            }
        }
    }

    private void drawHover(Graphics2D g) {
        int c = hoverCol, r = hoverRow;
        if (c < 0 || c >= COLS || r < 0 || r >= ROWS || gameOver) return;
        int x = c * CELL, y = r * CELL;
        Tower t = towerGrid[c][r];

        if (sellMode && !waveRunning) {
            g.setColor(new Color(255, 0, 0, t != null ? 120 : 45));
            g.fillRect(x, y, CELL, CELL);
            return;
        }
        if (t != null) {
            drawRange(g, t.getCenterX(), t.getCenterY(), t.getRangePixels(), Color.WHITE);
            return;
        }
        TowerType sel = selectedType;
        if (sel != null && !waveRunning) {
            boolean ok = !map.isPath(c, r) && coins >= sel.getCost(1);
            g.setColor(ok ? new Color(0, 255, 0, 90) : new Color(255, 0, 0, 90));
            g.fillRect(x, y, CELL, CELL);
            drawRange(g, x + CELL / 2.0, y + CELL / 2.0, sel.getRange(1) * CELL, ok ? Color.WHITE : GameConstants.RED);
        }
    }

    private void drawRange(Graphics2D g, double cx, double cy, double r, Color c) {
        int ir = (int) r;
        g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 40));
        g.fillOval((int) cx - ir, (int) cy - ir, ir * 2, ir * 2);
        g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 170));
        g.setStroke(new BasicStroke(2));
        g.drawOval((int) cx - ir, (int) cy - ir, ir * 2, ir * 2);
        g.setStroke(new BasicStroke(1));
    }

    private void drawHint(Graphics2D g) {
        String s = (wave == 0)
                ? "Build towers, then press SPACE (or Start Wave) to begin Wave 1"
                : "Wave " + (wave + 1) + " is ready - press SPACE to start";
        g.setFont(new Font("SansSerif", Font.BOLD, 16));
        FontMetrics fm = g.getFontMetrics();
        int w = fm.stringWidth(s) + 30, x = (MAP_W - w) / 2;
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRoundRect(x, 8, w, 30, 14, 14);
        g.setColor(Color.WHITE);
        g.drawString(s, x + 15, 29);
    }

    private void drawTooltip(Graphics2D g) {
        int c = hoverCol, r = hoverRow;
        if (c < 0 || c >= COLS || r < 0 || r >= ROWS || gameOver) return;
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
                lv < 3 ? "Click to upgrade: " + t.getUpgradeCost() + "c" : "MAX LEVEL",
                "Sell value: " + t.getRefund() + "c"
            };
        }
        g.setFont(new Font("SansSerif", Font.BOLD, 13));
        FontMetrics fm = g.getFontMetrics();
        int w = 0;
        for (String s : lines) w = Math.max(w, fm.stringWidth(s));
        w += 16;
        int h = lines.length * 18 + 10;
        int x = mouseX + 16, y = mouseY + 10;
        if (x + w > MAP_W) x = mouseX - w - 10;
        if (y + h > MAP_H) y = mouseY - h - 10;
        g.setColor(new Color(20, 20, 30, 220));
        g.fillRoundRect(x, y, w, h, 10, 10);
        g.setColor(GameConstants.GOLD);
        g.drawRoundRect(x, y, w, h, 10, 10);
        g.setColor(Color.WHITE);
        for (int i = 0; i < lines.length; i++) g.drawString(lines[i], x + 8, y + 20 + i * 18);
    }
}