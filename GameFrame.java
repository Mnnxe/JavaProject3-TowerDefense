// Group members: 6xxxxxxx Firstname Lastname 1, 6xxxxxxx Firstname Lastname 2,
//                6xxxxxxx Firstname Lastname 3, 6xxxxxxx Firstname Lastname 4,
//                6xxxxxxx Firstname Lastname 5
package JavaProject3_TowerDefense;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

// =====================================================================
//  GameFrame : HUD (top) + map (center) + towers & buttons (bottom)
// =====================================================================
public class GameFrame extends JFrame {
    private final MainMenuFrame menu;
    private final String playerName;
    private final MapData map;
    private GamePanel gamePanel;

    private JProgressBar hpBar;
    private JLabel waveLabel, mobsLabel, coinLabel, killLabel, infoLabel;
    private JList<String> towerList;
    private JToggleButton sellToggle;
    private JButton startWaveButton, pauseButton;
    private JComboBox<String> speedBox;

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

        // map area: keeps the map in the middle of the window
        JPanel mapArea = new ImagePanel("game_bg.png", GameConstants.PANEL_BG);
        mapArea.setLayout(new GridBagLayout());
        mapArea.add(gamePanel);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(GameConstants.BAGE);
        root.add(buildHud(), BorderLayout.NORTH);
        root.add(mapArea, BorderLayout.CENTER);
        root.add(buildBottomBar(), BorderLayout.SOUTH);

        GameConstants.setupFrame(this, root, sameSpotAs);   // same size as the main menu
        refreshTowerList(1);
        setVisible(true);
        gamePanel.startGameLoop();
        gamePanel.requestFocusInWindow();
    }

    // ================= HUD (display only) =================
    private JPanel buildHud() {
        JPanel hud = new ImagePanel("hud_bg.png", GameConstants.PANEL_BG);
        hud.setLayout(new BorderLayout());
        
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 7));
        left.setOpaque(false);
        JLabel playerLabel = GameConstants.makeLabel("Player: " + playerName, GameConstants.NORMAL_FONT, GameConstants.BAGE);
        left.add(playerLabel);

        hpBar = new JProgressBar(0, GameConstants.MAX_HP);
        hpBar.setValue(GameConstants.MAX_HP);
        hpBar.setStringPainted(true);
        hpBar.setString("HP " + GameConstants.MAX_HP + " / " + GameConstants.MAX_HP);
        hpBar.setForeground(GameConstants.GREEN);
        hpBar.setFont(GameConstants.NORMAL_FONT);
        hpBar.setPreferredSize(new Dimension(170, 22));
        left.add(hpBar);

        Font f = GameConstants.NORMAL_FONT;
        waveLabel = GameConstants.makeIconLabel("Wave: 0 / " + GameConstants.MAX_WAVE, "icon_wave.png", f, GameConstants.BAGE);
        mobsLabel = GameConstants.makeLabel("Next Wave: " + WaveData.totalMobs(1) + " mobs", f, GameConstants.BAGE);
        killLabel = GameConstants.makeIconLabel("Kills: 0", "icon_kill.png", f, GameConstants.BAGE);
        coinLabel = GameConstants.makeIconLabel("Coins: " + GameConstants.START_COINS, "icon_coin.png", f, GameConstants.GOLD);
        
        left.add(waveLabel);
        left.add(mobsLabel);
        left.add(killLabel);
        left.add(coinLabel);
        
        //Right
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 3));
        right.setOpaque(false);
        

        pauseButton = GameConstants.makeImageButton("btn_pause.png", "", GameConstants.BLUE, 45, 45, 13);
        pauseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { 
                SoundManager.playSFX("click");
                openPause(); 
            }
        });
        right.add(pauseButton);

        hud.add(left, BorderLayout.WEST);
        hud.add(right, BorderLayout.EAST);
        return hud;
    }

    // ================= bottom bar (towers + buttons) =================
    private JPanel buildBottomBar() {
        JPanel bar = new ImagePanel("bottom_bg.png", GameConstants.PANEL_BG);
        bar.setLayout(new BorderLayout());
        bar.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        Font small = GameConstants.GAME_FONT.deriveFont(Font.PLAIN, 12f);

        // ---- tower list ----
        towerList = new JList<String>();
        towerList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
        towerList.setVisibleRowCount(1);
        towerList.setFixedCellWidth(105);
        towerList.setFixedCellHeight(34);
        towerList.setFont(small);
        towerList.setFocusable(false);
        towerList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        towerList.setBackground(GameConstants.PANEL_BG);
        towerList.setForeground(GameConstants.BAGE);
        towerList.setSelectionBackground(GameConstants.BAGE);
        towerList.setSelectionForeground(GameConstants.PANEL_BG);
        towerList.setBorder(BorderFactory.createLineBorder(GameConstants.BAGE));
        towerList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) towerSelected();
            }
        });

        // ---- deconstruct toggle (PNG: btn_sell.png / btn_sell_on.png) ----
        sellToggle = new JToggleButton("Deconstruct [S]");
        sellToggle.setFont(small);
        sellToggle.setFocusable(false);
        ImageIcon sellIcon = GameConstants.getIcon("btn_white.png", 150, 38);
        
        if (sellIcon != null) {
            sellToggle.setIcon(sellIcon);

            sellToggle.setHorizontalTextPosition(SwingConstants.CENTER);
            sellToggle.setVerticalTextPosition(SwingConstants.CENTER);
            sellToggle.setToolTipText("Deconstruct [S]");
            ImageIcon sellOn = GameConstants.getIcon("btn_yellow.png", 150, 38);
            if (sellOn != null) sellToggle.setSelectedIcon(sellOn);
            sellToggle.setBorderPainted(false);
            sellToggle.setContentAreaFilled(false);
        }
        sellToggle.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                boolean on = (e.getStateChange() == ItemEvent.SELECTED);
                gamePanel.setSellMode(on);
                if (on) {
                    towerList.clearSelection();
                    infoLabel.setText("Deconstruct mode: click a tower to sell it (70% refund).");
                    SoundManager.playSFX("click");
                }
                gamePanel.requestFocusInWindow();
            }
        });

        // ---- buttons ----
        startWaveButton = GameConstants.makeImageButton("btn_startwave.png", "Start Wave [Space]", GameConstants.GREEN, 300, 38, 14);
        startWaveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                gamePanel.startWave();
                gamePanel.requestFocusInWindow();
            }
        });

        speedBox = new JComboBox<String>(new String[]{"Speed x1", "Speed x2"});
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
                GameConstants.NORMAL_FONT, GameConstants.BAGE);
        infoLabel.setBorder(BorderFactory.createEmptyBorder(6, 4, 0, 0));

        bar.add(left, BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);
        bar.add(infoLabel, BorderLayout.SOUTH);
        return bar;
    }

    private void towerSelected() {
        int idx = towerList.getSelectedIndex();
        if (idx < 0) { gamePanel.setSelectedTower(null); return; }
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
                hpBar.setForeground(hp > 60 ? GameConstants.GREEN : hp > 30 ? Color.ORANGE : GameConstants.RED);
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
        SoundManager.stopBGM();
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