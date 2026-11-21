// 6813115 Renuka Choosingkae
// 6813238 Napat Chonsaktrakul
// 6813248 Pimpichcha Saengngoen
// 6813258 Sittichok Boonthittanont
package JavaProject3_TowerDefense;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.TitledBorder;

// =====================================================================
//  GetReadyFrame : name (JTextField), map (JComboBox + preview),
//                  song (5 JRadioButtons), Back / Start
// =====================================================================
public class GetReadyFrame extends JFrame {
    private static final int SONG_PANEL_W = 280, BTN_W = 160, BTN_H = 50;
    private static final int NAME_COLUMNS = 16, ERROR_W = 230, ERROR_H = 25;

    private final MainMenuFrame menu;
    private final boolean specialUnlocked;
    private JTextField nameField;
    private JLabel errorLabel, mapStatus;
    private JComboBox<String> mapBox;
    private MapPreviewPanel preview;
    private JRadioButton[] songButtons;

    public GetReadyFrame(MainMenuFrame menu) {
        super("Get Ready!");
        this.menu = menu;
        this.specialUnlocked = Leaderboard.hasPlayedBasicMap();
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) { goBack(); }
        });

        ImagePanel root = new ImagePanel("menu_bg.png", GameConstants.DARK_BG);
        root.setLayout(new BorderLayout(15, 15));
        root.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        root.add(buildNamePanel(), BorderLayout.NORTH);
        root.add(buildMapPanel(), BorderLayout.CENTER);
        root.add(buildSongPanel(), BorderLayout.EAST);
        root.add(buildButtonPanel(), BorderLayout.SOUTH);

        updatePreview();
        GameConstants.setupFrame(this, root, menu);
        SoundManager.playBGM(SoundManager.getCurrentSong());
    }

    // ================= sections =================
    private JPanel buildNamePanel() {
        JPanel p = section(null);
        p.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
        p.add(GameConstants.makeLabel("Player Name:", GameConstants.HEADER_FONT, GameConstants.BROWN));

        nameField = new JTextField(NAME_COLUMNS);
        nameField.setFont(GameConstants.HEADER_FONT);
        errorLabel = GameConstants.makeLabel(" ", GameConstants.NORMAL_FONT, GameConstants.RED);
        errorLabel.setPreferredSize(new Dimension(ERROR_W, ERROR_H));   // fixed width -> layout does not jump

        nameField.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) startGame();
            }
            public void keyReleased(KeyEvent e) {
                String t = nameField.getText();
                if (t.length() > GameConstants.NAME_MAX_LENGTH) {
                    nameField.setText(t.substring(0, GameConstants.NAME_MAX_LENGTH));
                    errorLabel.setText("Max " + GameConstants.NAME_MAX_LENGTH + " characters");
                } else if (!t.trim().isEmpty()) {
                    errorLabel.setText(" ");
                }
            }
        });
        p.add(nameField);
        p.add(errorLabel);
        return p;
    }

    private JPanel buildMapPanel() {
        JPanel p = section("Select Map");
        p.setLayout(new BorderLayout(5, 8));

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
        mapStatus = GameConstants.makeLabel(" ", GameConstants.BOLD_FONT, GameConstants.BROWN);

        p.add(mapBox, BorderLayout.NORTH);
        p.add(preview, BorderLayout.CENTER);
        p.add(mapStatus, BorderLayout.SOUTH);
        return p;
    }

    private JPanel buildSongPanel() {
        JPanel p = section("Select Song");
        p.setLayout(new GridLayout(SoundManager.SONG_NAMES.length, 1, 0, 6));
        p.setPreferredSize(new Dimension(SONG_PANEL_W, 0));

        ButtonGroup group = new ButtonGroup();
        songButtons = new JRadioButton[SoundManager.SONG_NAMES.length];
        for (int i = 0; i < songButtons.length; i++) {
            songButtons[i] = new JRadioButton(SoundManager.SONG_NAMES[i]);
            songButtons[i].setOpaque(false);
            songButtons[i].setForeground(GameConstants.TEXT);
            songButtons[i].setFont(GameConstants.BOLD_FONT);
            songButtons[i].setFocusable(false);
            group.add(songButtons[i]);
            p.add(songButtons[i]);
        }
        songButtons[SoundManager.getCurrentSong()].setSelected(true);

        ItemListener songListener = new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() != ItemEvent.SELECTED) return;
                for (int i = 0; i < songButtons.length; i++)
                    if (e.getSource() == songButtons[i]) SoundManager.playBGM(i);   // change music immediately
            }
        };
        for (JRadioButton rb : songButtons) rb.addItemListener(songListener);
        return p;
    }

    private JPanel buildButtonPanel() {
        JButton backBtn = GameConstants.makeButton("btn_yellow.png", "BACK", BTN_W, BTN_H);
        JButton startBtn = GameConstants.makeButton("btn_yellow.png", "START", BTN_W, BTN_H);
        backBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { goBack(); }
        });
        startBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { startGame(); }
        });

        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        p.add(backBtn, BorderLayout.WEST);
        p.add(startBtn, BorderLayout.EAST);
        return p;
    }

    // see-through beige panel, with a brown titled border if title != null
    private JPanel section(String title) {
        JPanel p = new ImagePanel(null, GameConstants.SECTION_BG);
        if (title != null) {
            p.setBorder(BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(GameConstants.BROWN, 2), title,
                    TitledBorder.LEFT, TitledBorder.TOP, GameConstants.BOLD_FONT, GameConstants.BROWN));
        }
        return p;
    }

    // ================= actions =================
    private MapData selectedMap() { return MapData.MAPS[mapBox.getSelectedIndex()]; }

    private boolean isLocked(MapData m) { return m.isSpecial() && !specialUnlocked; }

    private void updatePreview() {
        MapData m = selectedMap();
        boolean locked = isLocked(m);
        preview.setMap(m, locked);
        mapStatus.setForeground(locked ? GameConstants.RED : GameConstants.BROWN);
        if (m.isRandom()) mapStatus.setText("Random map");
        else if (locked) mapStatus.setText("LOCKED - play any basic map first!");
        else if (m.isSpecial()) mapStatus.setText("Special map - the professors are coming!");
        else mapStatus.setText("Basic map");
    }

    private void goBack() {
        menu.setLocation(getLocation());
        dispose();
        menu.setVisible(true);
        SoundManager.playMenuBGM();
    }

    private void startGame() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            errorLabel.setText("Name can't be empty!");
            SoundManager.playSFX("error");
            nameField.requestFocusInWindow();
            return;
        }
        MapData m = selectedMap();
        if (isLocked(m)) {
            errorLabel.setText("This map is locked!");
            SoundManager.playSFX("error");
            return;
        }
        if (m.isRandom()) m = MapData.getRandomMap(specialUnlocked);
        SoundManager.playSFX("wave");
        new GameFrame(menu, name, m, this);    // opens at the same place, same size
        dispose();
    }

    // ================= small panel that draws the chosen map (auto-scaled) =================
    private class MapPreviewPanel extends JPanel {
        private MapData map;
        private boolean locked;

        public MapPreviewPanel() { setOpaque(false); }

        public void setMap(MapData map, boolean locked) {
            this.map = map;
            this.locked = locked;
            repaint();
        }

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (map == null) return;
            Graphics2D g2 = (Graphics2D) g;

            int cs = Math.max(1, Math.min(getWidth() / GameConstants.COLS, getHeight() / GameConstants.ROWS));
            int w = cs * GameConstants.COLS, h = cs * GameConstants.ROWS;
            int ox = (getWidth() - w) / 2, oy = (getHeight() - h) / 2;

            g2.translate(ox, oy);
            if (map.isRandom()) {
                g2.setColor(GameConstants.SECTION_BG);
                g2.fillRect(0, 0, w, h);
                g2.setFont(GameConstants.TITLE_FONT);
                FontMetrics fm = g2.getFontMetrics();
                g2.setColor(GameConstants.BROWN);
                g2.drawString("?", (w - fm.stringWidth("?")) / 2, h / 2 + fm.getAscent() / 2);
            } else {
                MapDraw.draw(g2, map, cs);
            }
            if (locked) {
                g2.setColor(GameConstants.OVERLAY);
                g2.fillRect(0, 0, w, h);
                g2.setFont(GameConstants.TITLE_FONT);
                FontMetrics fm = g2.getFontMetrics();
                g2.setColor(GameConstants.RED);
                g2.drawString("LOCKED", (w - fm.stringWidth("LOCKED")) / 2, h / 2 + 18);
            }
            g2.translate(-ox, -oy);
        }
    }
}