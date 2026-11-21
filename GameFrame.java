// 6813115 Renuka Choosingkae
// 6813238 Napat Chonsaktrakul
// 6813248 Pimpichcha Saengngoen
// 6813258 Sittichok Boonthittanont
package JavaProject3_TowerDefense;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

// =====================================================================
//  GameFrame : HUD (top) + map (center) + towers & buttons (bottom)
// =====================================================================
public class GameFrame extends JFrame {
    private static final int PAUSE_SIZE = 45, HP_BAR_W = 170, HP_BAR_H = 22;
    private static final int TOWER_CELL_W = 105, TOWER_CELL_H = 34;
    private static final int SELL_W = 150, SELL_H = 38, START_W = 300, START_H = 38;

    private final MainMenuFrame menu;
    private final String playerName;
    private final MapData map;
    private final GamePanel gamePanel;

    private JProgressBar hpBar;
    private JLabel waveLabel, mobsLabel, coinLabel, killLabel, infoLabel;
    private JList<String> towerList;
    private JToggleButton sellToggle;
    private JButton startWaveButton;

    private int upcomingWave = 1;
    private boolean closed = false, recordSaved = false;

    public GameFrame(MainMenuFrame menu, String playerName, MapData map, Component sameSpotAs) {
        super("Tower Defense - " + map.getName());
        this.menu = menu;
        this.playerName = playerName;
        this.map = map;
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) { openPause(); }
        });

        gamePanel = new GamePanel(this, map);
        JPanel mapArea = new ImagePanel("game_bg.png", GameConstants.PANEL_BG);
        mapArea.setLayout(new GridBagLayout());          // keeps the map in the middle
        mapArea.add(gamePanel);

        JPanel root = new JPanel(new BorderLayout());
        root.add(buildHud(), BorderLayout.NORTH);
        root.add(mapArea, BorderLayout.CENTER);
        root.add(buildBottomBar(), BorderLayout.SOUTH);

        GameConstants.setupFrame(this, root, sameSpotAs);  // same size as the main menu
        refreshTowerList(1);
        setVisible(true);
        gamePanel.startGameLoop();                         // the game loop fills in the HUD values
        gamePanel.requestFocusInWindow();
    }

    // ================= HUD (top) =================
    private JPanel buildHud() {
        Font f = GameConstants.NORMAL_FONT;
        Color c = GameConstants.BEIGE;

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 7));
        left.setOpaque(false);
        left.add(GameConstants.makeLabel("Player: " + playerName, f, c));

        hpBar = new JProgressBar(0, GameConstants.MAX_HP);
        hpBar.setStringPainted(true);
        hpBar.setFont(f);
        hpBar.setPreferredSize(new Dimension(HP_BAR_W, HP_BAR_H));
        left.add(hpBar);

        waveLabel = GameConstants.makeIconLabel(" ", "icon_wave.png", f, c);
        mobsLabel = GameConstants.makeLabel(" ", f, c);
        killLabel = GameConstants.makeIconLabel(" ", "icon_kill.png", f, c);
        coinLabel = GameConstants.makeIconLabel(" ", "icon_coin.png", f, GameConstants.GOLD);
        left.add(waveLabel);
        left.add(mobsLabel);
        left.add(killLabel);
        left.add(coinLabel);

        JButton pauseButton = GameConstants.makeIconButton("btn_pause.png", "II", PAUSE_SIZE);
        pauseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //SoundManager.playSFX("click"); 
                openPause(); 
            }
        });
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 3));
        right.setOpaque(false);
        right.add(pauseButton);

        JPanel hud = new ImagePanel("", GameConstants.PANEL_BG);
        hud.setLayout(new BorderLayout());
        hud.add(left, BorderLayout.WEST);
        hud.add(right, BorderLayout.EAST);
        return hud;
    }

    // ================= bottom bar (towers + buttons) =================
    private JPanel buildBottomBar() {
        Font small = GameConstants.SMALL_FONT;

        // ---- tower list (JList) ----
        towerList = new JList<String>();
        towerList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
        towerList.setVisibleRowCount(1);
        towerList.setFixedCellWidth(TOWER_CELL_W);
        towerList.setFixedCellHeight(TOWER_CELL_H);
        towerList.setFont(small);
        towerList.setFocusable(false);
        towerList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        towerList.setBackground(GameConstants.PANEL_BG);
        towerList.setForeground(GameConstants.BEIGE);
        towerList.setSelectionBackground(GameConstants.BEIGE);
        towerList.setSelectionForeground(GameConstants.PANEL_BG);
        towerList.setBorder(BorderFactory.createLineBorder(GameConstants.BEIGE));
        towerList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) towerSelected();
            }
        });

        // ---- deconstruct toggle (btn_white.png, btn_yellow.png when ON) ----
        sellToggle = new JToggleButton("Deconstruct [S]");
        sellToggle.setFont(small);
        GameConstants.styleButton(sellToggle, "btn_white.png", SELL_W, SELL_H);
        ImageIcon onIcon = GameConstants.getIcon("btn_yellow.png", SELL_W, SELL_H);
        if (onIcon != null) sellToggle.setSelectedIcon(onIcon);
        sellToggle.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                boolean on = (e.getStateChange() == ItemEvent.SELECTED);
                gamePanel.setSellMode(on);
                if (on) {
                    towerList.clearSelection();
                    infoLabel.setText("Deconstruct mode: click a tower to sell it ("
                            + Math.round(GameConstants.REFUND_RATE * 100) + "% refund).");
                }
                gamePanel.requestFocusInWindow();
            }
        });

        // ---- start wave + speed ----
        startWaveButton = GameConstants.makeButton("btn_startwave.png", "Start Wave [Space]", START_W, START_H);
        startWaveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                gamePanel.startWave();
                gamePanel.requestFocusInWindow();
            }
        });

        final JComboBox<String> speedBox = new JComboBox<String>(new String[]{"Speed x1", "Speed x2"});
        speedBox.setFont(small);
        speedBox.setFocusable(false);
        speedBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                gamePanel.setSpeed(speedBox.getSelectedIndex() + 1);
                gamePanel.requestFocusInWindow();
            }
        });

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setOpaque(false);
        left.add(towerList);
        left.add(sellToggle);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        right.add(speedBox);
        right.add(startWaveButton);

        infoLabel = GameConstants.makeLabel("Select a tower (or press 1-4), then click an empty tile to build.",
                GameConstants.NORMAL_FONT, GameConstants.BEIGE);
        infoLabel.setBorder(BorderFactory.createEmptyBorder(6, 4, 0, 0));

        JPanel bar = new ImagePanel("bottom_bg.png", GameConstants.PANEL_BG);
        bar.setLayout(new BorderLayout());
        bar.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        bar.add(left, BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);
        bar.add(infoLabel, BorderLayout.SOUTH);
        return bar;
    }

    private void towerSelected() {
        int idx = towerList.getSelectedIndex();
        if (idx < 0) {
            gamePanel.setSelectedTower(null);
            return;
        }
        TowerType t = TowerType.values()[idx];
        if (!t.isAvailable(upcomingWave)) {
            infoLabel.setText(t.getName() + " unlocks at wave " + t.getUnlockWave() + ".");
            SoundManager.playSFX("error");
            towerList.clearSelection();
            return;
        }
        sellToggle.setSelected(false);
        gamePanel.setSelectedTower(t);
        infoLabel.setText(t.getInfo(1));
        SoundManager.playSFX("click");
        gamePanel.requestFocusInWindow();
    }

    // ================= methods called by GamePanel =================
    public void refreshTowerList(final int wave) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                upcomingWave = wave;
                int sel = towerList.getSelectedIndex();
                TowerType[] types = TowerType.values();
                String[] items = new String[types.length];
                for (int i = 0; i < types.length; i++) {
                    if (types[i].isAvailable(wave))
                        items[i] = (i + 1) + ". " + types[i].getName() + " " + types[i].getCost(1) + "c";
                    else
                        items[i] = "Locked (W" + types[i].getUnlockWave() + ")";
                }
                towerList.setListData(items);
                if (sel >= 0 && types[sel].isAvailable(wave)) towerList.setSelectedIndex(sel);
            }
        });
    }

    public void updateHud(final int hp, final int wave, final int mobs, final int coins,
                          final int kills, final boolean running) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                hpBar.setValue(hp);
                hpBar.setString("HP " + hp + " / " + GameConstants.MAX_HP);
                hpBar.setForeground(GameConstants.hpColor(hp / (double) GameConstants.MAX_HP));
                waveLabel.setText("Wave: " + wave + " / " + GameConstants.MAX_WAVE);
                mobsLabel.setText(running ? "Mobs Left: " + mobs : "Next Wave: " + mobs + " mobs");
                coinLabel.setText("Coins: " + coins);
                killLabel.setText("Kills: " + kills);
            }
        });
    }

    public void setWaveRunning(final boolean running) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (running) sellToggle.setSelected(false);
                towerList.setEnabled(!running);
                sellToggle.setEnabled(!running);
                startWaveButton.setEnabled(!running);
                infoLabel.setText(running ? "Wave in progress... towers are locked until the wave ends."
                                          : "Build, upgrade or sell towers, then press SPACE.");
            }
        });
    }

    public void selectTowerByKey(int index) {
        if (towerList.isEnabled() && index >= 0 && index < TowerType.values().length)
            towerList.setSelectedIndex(index);
    }

    public void toggleSell() {
        if (sellToggle.isEnabled()) sellToggle.setSelected(!sellToggle.isSelected());
    }

    public void clearSelection() {
        towerList.clearSelection();
        sellToggle.setSelected(false);
        infoLabel.setText("Selection cancelled.");
    }

    // ================= pause / end / exit =================
    public void openPause() {
        if (closed || gamePanel.isGameOver()) return;
        gamePanel.setPaused(true);
        new PauseDialog(this).setVisible(true);       // modal -> waits here
        if (!closed) {
            gamePanel.setPaused(false);
            gamePanel.requestFocusInWindow();
        }
    }

    public void showEndGame(boolean victory) {
        if (closed) return;
        saveRecord();
        new EndGameDialog(this, victory, playerName, map.getName(),
                gamePanel.getWavesCompleted(), gamePanel.getKills()).setVisible(true);
        exitToMenu();
    }

    public void exitToMenu() {
        if (closed) return;
        closed = true;
        gamePanel.stopGameLoop();
        saveRecord();
        menu.setLocation(getLocation());     // menu appears at the same place
        dispose();
        menu.setVisible(true);
        SoundManager.playMenuBGM();
    }

    private void saveRecord() {
        if (recordSaved || !gamePanel.hasStarted()) return;
        recordSaved = true;
        Leaderboard.addRecord(map.getName(), playerName, gamePanel.getWavesCompleted(), gamePanel.getKills());
    }
}