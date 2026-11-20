// Group members: 6xxxxxxx Firstname Lastname 1, 6xxxxxxx Firstname Lastname 2,
//                6xxxxxxx Firstname Lastname 3, 6xxxxxxx Firstname Lastname 4,
//                6xxxxxxx Firstname Lastname 5
package JavaProject3_TowerDefense;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.TitledBorder;

// =====================================================================
//  GetReadyFrame : name (text field), map (combo box + preview),
//                  song (5 radio buttons), Back / Start
//  Each section can have a PNG frame; components are placed on top of it.
// =====================================================================
public class GetReadyFrame extends JFrame {
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
         SoundManager.playBGM(0);
    }

    // ================= sections =================
    private JPanel buildNamePanel() {
        JPanel p = section("btn_yellow.png", null);
        p.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
        p.add(GameConstants.makeLabel("Player Name:", GameConstants.HEADER_FONT, GameConstants.BROWN));

        nameField = new JTextField(16);
        nameField.setFont(GameConstants.HEADER_FONT);
        errorLabel = GameConstants.makeLabel(" ", GameConstants.NORMAL_FONT, GameConstants.RED);
        errorLabel.setPreferredSize(new Dimension(230, 25));   // fixed width -> layout does not jump

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
        JPanel p = section("frame_map.png", "Select Map");
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
        mapStatus = GameConstants.makeLabel(" ", GameConstants.BOLD_FONT, GameConstants.TEXT);

        p.add(mapBox, BorderLayout.NORTH);
        p.add(preview, BorderLayout.CENTER);
        p.add(mapStatus, BorderLayout.SOUTH);
        return p;
    }

    private JPanel buildSongPanel() {
        JPanel p = section("frame_song.png", "Select Song");
        p.setLayout(new GridLayout(SoundManager.SONG_NAMES.length, 1, 0, 6));
        p.setPreferredSize(new Dimension(280, 0));

        ButtonGroup group = new ButtonGroup();
        songButtons = new JRadioButton[SoundManager.SONG_NAMES.length];
        ItemListener songListener = new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() != ItemEvent.SELECTED) return;
                for (int i = 0; i < songButtons.length; i++)
                    if (e.getSource() == songButtons[i]) SoundManager.playBGM(i);   // change music immediately
            }
        };
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
        for (JRadioButton rb : songButtons) rb.addItemListener(songListener);
        return p;
    }

    private JPanel buildButtonPanel() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        JButton backBtn = GameConstants.makeImageButton("btn_yellow.png", "BACK", GameConstants.RED, 160, 50, 18);
        JButton startBtn = GameConstants.makeImageButton("btn_yellow.png", "START", GameConstants.GREEN, 160, 50, 18);
        backBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { goBack(); }
        });
        startBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { startGame(); }
        });
        p.add(backBtn, BorderLayout.WEST);
        p.add(startBtn, BorderLayout.EAST);
        return p;
    }

    // PNG frame if it exists, otherwise a gold titled border
    private JPanel section(String imageFile, String title) {
        JPanel p = new ImagePanel(new Color(255, 250, 220, 200));
        if (title != null) {
             p.setBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(GameConstants.BROWN, 2),
                title,
                TitledBorder.LEFT,
                TitledBorder.TOP,
                GameConstants.BOLD_FONT,
                GameConstants.BROWN
            )
        );
        }
        return p;
    }

    // ================= actions =================
    private boolean isLocked(MapData m) { return m.isSpecial() && !specialUnlocked; }

    private void updatePreview() {
        MapData m = MapData.MAPS[mapBox.getSelectedIndex()];
        preview.setMap(m, isLocked(m));
        if (isLocked(m)) {
            mapStatus.setForeground(GameConstants.RED);
            mapStatus.setText("LOCKED - play any basic map first!");
        } else if (m.isSpecial()) {
            mapStatus.setForeground(GameConstants.BROWN);
            mapStatus.setText("Special map - the professors are coming!");
        } else {
            mapStatus.setForeground(GameConstants.BROWN);
            mapStatus.setText("Basic map");
        }
    }

    private void goBack() {
        SoundManager.playSFX("click");
        menu.setLocation(getLocation());
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
        new GameFrame(menu, name, m, this);    // opens at the same place, same size
        dispose();
    }

    // ================= small panel that draws the chosen map (auto-scaled) =================
    class MapPreviewPanel extends JPanel {
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
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int cs = Math.max(1, Math.min(getWidth() / GameConstants.COLS, getHeight() / GameConstants.ROWS));
            int w = cs * GameConstants.COLS, h = cs * GameConstants.ROWS;
            int ox = (getWidth() - w) / 2, oy = (getHeight() - h) / 2;

            g2.translate(ox, oy);
            MapDraw.draw(g2, map, cs);
            if (locked) {
                g2.setColor(new Color(0, 0, 0, 170));
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