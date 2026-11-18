package JavaProject3_TowerDefense;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.swing.*;
import javax.swing.border.TitledBorder;

// =====================================================================
//  MainMenuFrame : first window
// =====================================================================
public class MainMenuFrame extends JFrame {
    private static final int W = 1020, H = 660;

    public MainMenuFrame() {
        super("Tower Defense: Professor Edition");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                new ExitDialog(MainMenuFrame.this).setVisible(true);
            }
        });

        MenuPanel panel = new MenuPanel();
        panel.setPreferredSize(new Dimension(W, H));
        panel.setLayout(null);
        setContentPane(panel);

        JButton leaderBtn = GameConstants.makeButton("Leaderboard", new Color(200, 140, 30), 14);
        leaderBtn.setBounds(W - 300, 15, 140, 36);
        JButton settingBtn = GameConstants.makeButton("Settings", new Color(95, 95, 120), 14);
        settingBtn.setBounds(W - 150, 15, 135, 36);

        int bx = (W - 260) / 2;
        JButton playBtn = GameConstants.makeButton("PLAY", GameConstants.GREEN, 24);
        playBtn.setBounds(bx, 235, 260, 58);
        JButton howBtn = GameConstants.makeButton("HOW TO PLAY", GameConstants.BLUE);
        howBtn.setBounds(bx, 308, 260, 48);
        JButton creditBtn = GameConstants.makeButton("CREDITS", GameConstants.PURPLE);
        creditBtn.setBounds(bx, 368, 260, 48);
        JButton exitBtn = GameConstants.makeButton("EXIT", GameConstants.RED);
        exitBtn.setBounds(bx, 428, 260, 48);

        playBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                SoundManager.playSFX("click");
                new GetReadyFrame(MainMenuFrame.this).setVisible(true);
                setVisible(false);
            }
        });
        howBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                SoundManager.playSFX("click");
                new HowToPlayDialog(MainMenuFrame.this).setVisible(true);
            }
        });
        creditBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                SoundManager.playSFX("click");
                new CreditDialog(MainMenuFrame.this).setVisible(true);
            }
        });
        exitBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new ExitDialog(MainMenuFrame.this).setVisible(true);
            }
        });
        leaderBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                SoundManager.playSFX("click");
                new LeaderboardDialog(MainMenuFrame.this, null).setVisible(true);
            }
        });
        settingBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                SoundManager.playSFX("click");
                new SettingDialog(MainMenuFrame.this).setVisible(true);
            }
        });

        panel.add(leaderBtn); panel.add(settingBtn);
        panel.add(playBtn); panel.add(howBtn); panel.add(creditBtn); panel.add(exitBtn);

        pack();
        setResizable(false);
        setLocationRelativeTo(null);
    }

    // background of the menu
    class MenuPanel extends JPanel {
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            BufferedImage bg = GameConstants.getImage("menu_bg.png");
            if (bg != null) {
                g2.drawImage(bg, 0, 0, getWidth(), getHeight(), null);
            } else {
                g2.setPaint(new GradientPaint(0, 0, new Color(25, 30, 70), 0, getHeight(), new Color(90, 40, 110)));
                g2.fillRect(0, 0, getWidth(), getHeight());
                // ground + path
                g2.setColor(new Color(60, 120, 60));
                g2.fillRect(0, getHeight() - 70, getWidth(), 70);
                g2.setColor(new Color(170, 130, 80));
                g2.fillRect(0, getHeight() - 45, getWidth(), 22);
                // small towers on ground
                for (int i = 0; i < 6; i++) {
                    int x = 60 + i * 150;
                    g2.setColor(new Color(130, 130, 140));
                    g2.fillRect(x, getHeight() - 110, 30, 50);
                    g2.fillRect(x - 4, getHeight() - 118, 10, 10);
                    g2.fillRect(x + 10, getHeight() - 118, 10, 10);
                    g2.fillRect(x + 24, getHeight() - 118, 10, 10);
                }
            }
            // title
            g2.setFont(GameConstants.TITLE_FONT);
            FontMetrics fm = g2.getFontMetrics();
            String t = "TOWER DEFENSE";
            int tx = (getWidth() - fm.stringWidth(t)) / 2;
            g2.setColor(Color.BLACK);
            g2.drawString(t, tx + 4, 134);
            g2.setColor(GameConstants.GOLD);
            g2.drawString(t, tx, 130);

            g2.setFont(new Font("SansSerif", Font.BOLD | Font.ITALIC, 24));
            fm = g2.getFontMetrics();
            String s = "~ Professor Edition ~";
            g2.setColor(Color.WHITE);
            g2.drawString(s, (getWidth() - fm.stringWidth(s)) / 2, 180);
        }
    }
}

// =====================================================================
//  GetReadyFrame : name, map (combo box + preview), song (radio), Back/Start
// =====================================================================
class GetReadyFrame extends JFrame {
    private static final int PREVIEW_CELL = 20;
    private MainMenuFrame menu;
    private JTextField nameField;
    private JLabel errorLabel, mapStatus;
    private JComboBox<String> mapBox;
    private MapPreviewPanel preview;
    private JRadioButton[] songButtons;
    private boolean specialUnlocked;

    public GetReadyFrame(MainMenuFrame menu) {
        super("Get Ready!");
        this.menu = menu;
        this.specialUnlocked = Leaderboard.hasPlayedBasicMap();
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) { goBack(); }
        });

        JPanel root = new JPanel(new BorderLayout(15, 15));
        root.setBackground(GameConstants.DARK_BG);
        root.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // ===== TOP: player name =====
        JPanel top = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        top.setOpaque(false);
        top.add(GameConstants.makeLabel("Player Name:", GameConstants.HEADER_FONT, GameConstants.GOLD));
        nameField = new JTextField(16);
        nameField.setFont(GameConstants.HEADER_FONT);
        errorLabel = GameConstants.makeLabel(" ", GameConstants.BOLD_FONT, GameConstants.RED);
        nameField.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) startGame();
            }
            public void keyReleased(KeyEvent e) {
                String t = nameField.getText();
                if (t.length() > 15) {
                    nameField.setText(t.substring(0, 15));
                    errorLabel.setText("Max 15 characters");
                } else if (!t.trim().isEmpty()) {
                    errorLabel.setText(" ");
                }
            }
        });
        top.add(nameField);
        top.add(errorLabel);

        // ===== CENTER: map selection =====
        JPanel mapPanel = new JPanel(new BorderLayout(5, 8));
        mapPanel.setOpaque(false);
        mapPanel.setBorder(titled("Select Map"));
        String[] names = new String[MapData.MAPS.length];
        for (int i = 0; i < names.length; i++) names[i] = MapData.MAPS[i].getName();
        mapBox = new JComboBox<String>(names);
        mapBox.setFont(GameConstants.BOLD_FONT);
        mapBox.setFocusable(false);
        mapBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    SoundManager.playSFX("click");
                    updatePreview();
                }
            }
        });
        preview = new MapPreviewPanel();
        preview.setPreferredSize(new Dimension(GameConstants.COLS * PREVIEW_CELL, GameConstants.ROWS * PREVIEW_CELL));
        mapStatus = GameConstants.makeLabel(" ", GameConstants.BOLD_FONT, GameConstants.TEXT);
        mapPanel.add(mapBox, BorderLayout.NORTH);
        mapPanel.add(preview, BorderLayout.CENTER);
        mapPanel.add(mapStatus, BorderLayout.SOUTH);

        // ===== RIGHT: song selection =====
        JPanel songPanel = new JPanel(new GridLayout(SoundManager.SONG_NAMES.length, 1, 0, 6));
        songPanel.setOpaque(false);
        songPanel.setBorder(titled("Select Song"));
        songPanel.setPreferredSize(new Dimension(250, 0));
        ButtonGroup group = new ButtonGroup();
        songButtons = new JRadioButton[SoundManager.SONG_NAMES.length];
        ItemListener songListener = new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() != ItemEvent.SELECTED) return;
                for (int i = 0; i < songButtons.length; i++)
                    if (e.getSource() == songButtons[i]) SoundManager.playBGM(i);  // change music immediately
            }
        };
        for (int i = 0; i < songButtons.length; i++) {
            songButtons[i] = new JRadioButton(SoundManager.SONG_NAMES[i]);
            songButtons[i].setOpaque(false);
            songButtons[i].setForeground(GameConstants.TEXT);
            songButtons[i].setFont(GameConstants.BOLD_FONT);
            songButtons[i].setFocusable(false);
            group.add(songButtons[i]);
            songPanel.add(songButtons[i]);
        }
        songButtons[SoundManager.getCurrentSong()].setSelected(true);
        for (JRadioButton rb : songButtons) rb.addItemListener(songListener);

        // ===== BOTTOM: back / start =====
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        JButton backBtn = GameConstants.makeButton("< Back", GameConstants.RED, 18);
        JButton startBtn = GameConstants.makeButton("Start >", GameConstants.GREEN, 18);
        backBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { goBack(); }
        });
        startBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { startGame(); }
        });
        bottom.add(backBtn, BorderLayout.WEST);
        bottom.add(startBtn, BorderLayout.EAST);

        root.add(top, BorderLayout.NORTH);
        root.add(mapPanel, BorderLayout.CENTER);
        root.add(songPanel, BorderLayout.EAST);
        root.add(bottom, BorderLayout.SOUTH);
        setContentPane(root);

        updatePreview();
        pack();
        setResizable(false);
        setLocationRelativeTo(null);
    }

    private TitledBorder titled(String text) {
        return BorderFactory.createTitledBorder(BorderFactory.createLineBorder(GameConstants.GOLD, 2),
                text, TitledBorder.LEFT, TitledBorder.TOP, GameConstants.BOLD_FONT, GameConstants.GOLD);
    }

    private boolean isLocked(MapData m) { return m.isSpecial() && !specialUnlocked; }

    private void updatePreview() {
        MapData m = MapData.MAPS[mapBox.getSelectedIndex()];
        preview.setMap(m, isLocked(m));
        if (isLocked(m)) {
            mapStatus.setForeground(GameConstants.RED);
            mapStatus.setText("LOCKED - play any basic map first!");
        } else if (m.isSpecial()) {
            mapStatus.setForeground(GameConstants.GOLD);
            mapStatus.setText("Special map - the professors are coming!");
        } else {
            mapStatus.setForeground(GameConstants.TEXT);
            mapStatus.setText("Basic map");
        }
    }

    private void goBack() {
        SoundManager.playSFX("click");
        dispose();
        menu.setVisible(true);
    }

    private void startGame() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            errorLabel.setText("Name can't be empty!");
            SoundManager.playSFX("error");
            nameField.requestFocusInWindow();
            return;
        }
        MapData m = MapData.MAPS[mapBox.getSelectedIndex()];
        if (isLocked(m)) {
            errorLabel.setText("This map is locked!");
            SoundManager.playSFX("error");
            return;
        }
        SoundManager.playSFX("wave");
        dispose();
        new GameFrame(menu, name, m);
    }

    // small panel that draws the chosen map
    class MapPreviewPanel extends JPanel {
        private MapData map;
        private boolean locked;

        public void setMap(MapData map, boolean locked) {
            this.map = map;
            this.locked = locked;
            repaint();
        }

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (map == null) return;
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            map.draw(g2, PREVIEW_CELL);
            if (locked) {
                g2.setColor(new Color(0, 0, 0, 170));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setFont(GameConstants.TITLE_FONT);
                FontMetrics fm = g2.getFontMetrics();
                g2.setColor(GameConstants.RED);
                g2.drawString("LOCKED", (getWidth() - fm.stringWidth("LOCKED")) / 2, getHeight() / 2 + 18);
            }
        }
    }
}