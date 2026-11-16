package JavaProject3_TowerDefense;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.DefaultTableModel;

// =====================================================================
//  BaseDialog : parent of every pop-up (title bar + X button at top-right)
// =====================================================================
public class BaseDialog extends JDialog {
    protected JPanel body;

    public BaseDialog(Window owner, String title, int w, int h) {
        super(owner, title, Dialog.ModalityType.APPLICATION_MODAL);
        setUndecorated(true);
        setSize(w, h);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(GameConstants.DARK_BG);
        root.setBorder(BorderFactory.createLineBorder(GameConstants.GOLD, 3));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(GameConstants.HEADER_BG);
        JLabel titleLabel = GameConstants.makeLabel("  " + title, GameConstants.HEADER_FONT, GameConstants.GOLD);
        JButton closeBtn = GameConstants.makeButton("X", GameConstants.RED, 14);
        closeBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                SoundManager.playSFX("click");
                onClose();
            }
        });
        header.add(titleLabel, BorderLayout.WEST);
        header.add(closeBtn, BorderLayout.EAST);

        body = new JPanel();
        body.setBackground(GameConstants.DARK_BG);

        root.add(header, BorderLayout.NORTH);
        root.add(body, BorderLayout.CENTER);
        setContentPane(root);
        setLocationRelativeTo(owner);
    }

    protected void onClose() { dispose(); }
}

// =====================================================================
//  ExitDialog : "Quit this game?"  Yes / No
// =====================================================================
class ExitDialog extends BaseDialog {
    public ExitDialog(Window owner) {
        super(owner, "Exit", 380, 200);
        body.setLayout(null);

        JLabel q = GameConstants.makeLabel("Quit this game?", GameConstants.HEADER_FONT, GameConstants.TEXT);
        q.setHorizontalAlignment(SwingConstants.CENTER);
        q.setBounds(0, 25, 374, 30);

        JButton yes = GameConstants.makeButton("Yes", GameConstants.RED);
        yes.setBounds(70, 85, 100, 40);
        yes.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                SoundManager.stopBGM();
                System.exit(0);
            }
        });

        JButton no = GameConstants.makeButton("No", GameConstants.BLUE);
        no.setBounds(205, 85, 100, 40);
        no.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { dispose(); }
        });

        body.add(q);
        body.add(yes);
        body.add(no);
    }
}

// =====================================================================
//  HowToPlayDialog : pages with Previous / Next
// =====================================================================
class HowToPlayDialog extends BaseDialog {
    private static final String[] TITLES = {"Goal", "Building Towers", "Upgrade & Sell", "Controls", "Enemies"};
    private static final String[] PAGES = {
        "Mobs walk along the path from the purple portal to your castle.\n\n"
      + "- Every mob that reaches the castle takes away base HP.\n"
      + "- Base HP starts at 100. If it hits 0 -> DEFEAT.\n"
      + "- Survive all 10 waves -> VICTORY!\n\n"
      + "Wave 5 has a Mini Boss and wave 10 has the Final Boss.",

        "1. Click a tower in the list at the bottom (or press 1-4).\n"
      + "2. Move the mouse over the map: the attack range is shown.\n"
      + "3. Click an empty tile to build.\n\n"
      + "You cannot build on the path ('Not allowed') or without enough coins ('Can't afford').\n\n"
      + "Archer (50c)  - single target, wave 1+\n"
      + "Cannon (80c)  - splash damage 1 grid, wave 2+\n"
      + "Ice (100c)    - slows enemies, wave 3+\n"
      + "Fire (130c)   - burns enemies over time, wave 4+",

        "UPGRADE: click a tower that is already on the map (max Lv.3).\n\n"
      + "SELL: turn on 'Deconstruct' (or press S), then click a tower.\n"
      + "You get back 70% of all coins you spent on that tower.\n\n"
      + "IMPORTANT: once a wave starts you cannot build, upgrade or sell.\n\n"
      + "Coins: start with 150, earn from kills, and get 30 + (wave x 5) bonus after each wave.",

        "SPACE          - start next wave\n"
      + "P              - pause menu\n"
      + "1 / 2 / 3 / 4  - select Archer / Cannon / Ice / Fire\n"
      + "S              - toggle deconstruct mode\n"
      + "ESC / Right click - cancel selection\n\n"
      + "Speed box: play at x1 or x2 speed.",

        "Goblin  - normal          (60 HP)\n"
      + "Wolf    - fast            (40 HP)\n"
      + "Bat     - swarm           (25 HP)\n"
      + "Golem   - tank            (220 HP)\n"
      + "Mini Boss (wave 5)        (1,000 HP)\n"
      + "Boss      (wave 10)       (2,800 HP)\n\n"
      + "Killed mobs drop coins and sometimes restore base HP.\n"
      + "Play any basic map to unlock the 2 special maps... the professors are waiting!"
    };

    private int page = 0;
    private JLabel pageTitle, pageNumber;
    private JTextArea textArea;
    private JButton prevBtn, nextBtn;

    public HowToPlayDialog(Window owner) {
        super(owner, "How to Play", 620, 460);
        body.setLayout(new BorderLayout(10, 10));
        body.setBorder(BorderFactory.createEmptyBorder(10, 20, 15, 20));

        pageTitle = GameConstants.makeLabel("", GameConstants.HEADER_FONT, GameConstants.GOLD);

        textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 15));
        textArea.setBackground(GameConstants.PANEL_BG);
        textArea.setForeground(GameConstants.TEXT);
        textArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        prevBtn = GameConstants.makeButton("< Previous", GameConstants.BLUE, 14);
        nextBtn = GameConstants.makeButton("Next >", GameConstants.BLUE, 14);
        pageNumber = GameConstants.makeLabel("", GameConstants.BOLD_FONT, GameConstants.TEXT);
        pageNumber.setHorizontalAlignment(SwingConstants.CENTER);

        prevBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { changePage(-1); }
        });
        nextBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { changePage(1); }
        });

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.add(prevBtn, BorderLayout.WEST);
        bottom.add(pageNumber, BorderLayout.CENTER);
        bottom.add(nextBtn, BorderLayout.EAST);

        body.add(pageTitle, BorderLayout.NORTH);
        body.add(new JScrollPane(textArea), BorderLayout.CENTER);
        body.add(bottom, BorderLayout.SOUTH);
        showPage();
    }

    private void changePage(int d) {
        page = Math.max(0, Math.min(PAGES.length - 1, page + d));
        SoundManager.playSFX("click");
        showPage();
    }

    private void showPage() {
        pageTitle.setText(TITLES[page]);
        textArea.setText(PAGES[page]);
        textArea.setCaretPosition(0);
        pageNumber.setText("Page " + (page + 1) + " / " + PAGES.length);
        prevBtn.setEnabled(page > 0);
        nextBtn.setEnabled(page < PAGES.length - 1);
    }
}

// =====================================================================
//  SettingDialog : audio ON/OFF radio buttons + BGM / SFX sliders
// =====================================================================
class SettingDialog extends BaseDialog {
    private JRadioButton onRadio, offRadio;
    private JSlider bgmSlider, sfxSlider;
    private JLabel bgmValue, sfxValue;

    public SettingDialog(Window owner) {
        super(owner, "Settings", 440, 290);
        body.setLayout(null);

        // ---- audio ON / OFF ----
        label("Audio:", 30, 25);
        onRadio = radio("ON", 140, 22);
        offRadio = radio("OFF", 220, 22);
        ButtonGroup group = new ButtonGroup();
        group.add(onRadio);
        group.add(offRadio);
        if (SoundManager.isAudioOn()) onRadio.setSelected(true); else offRadio.setSelected(true);

        ItemListener audioListener = new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    boolean on = onRadio.isSelected();
                    SoundManager.setAudioOn(on);
                    bgmSlider.setEnabled(on);
                    sfxSlider.setEnabled(on);
                }
            }
        };

        // ---- BGM slider ----
        label("BGM:", 30, 90);
        bgmSlider = slider(SoundManager.getBgmVolume(), 130, 80);
        bgmValue = label(SoundManager.getBgmVolume() + "%", 350, 90);
        bgmSlider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                SoundManager.setBgmVolume(bgmSlider.getValue());
                bgmValue.setText(bgmSlider.getValue() + "%");
            }
        });

        // ---- SFX slider ----
        label("SFX:", 30, 155);
        sfxSlider = slider(SoundManager.getSfxVolume(), 130, 145);
        sfxValue = label(SoundManager.getSfxVolume() + "%", 350, 155);
        sfxSlider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                SoundManager.setSfxVolume(sfxSlider.getValue());
                sfxValue.setText(sfxSlider.getValue() + "%");
                if (!sfxSlider.getValueIsAdjusting()) SoundManager.playSFX("place"); // preview
            }
        });

        bgmSlider.setEnabled(SoundManager.isAudioOn());
        sfxSlider.setEnabled(SoundManager.isAudioOn());
        onRadio.addItemListener(audioListener);
        offRadio.addItemListener(audioListener);
    }

    private JLabel label(String text, int x, int y) {
        JLabel l = GameConstants.makeLabel(text, GameConstants.BOLD_FONT, GameConstants.TEXT);
        l.setBounds(x, y, 90, 25);
        body.add(l);
        return l;
    }

    private JRadioButton radio(String text, int x, int y) {
        JRadioButton r = new JRadioButton(text);
        r.setOpaque(false);
        r.setForeground(GameConstants.TEXT);
        r.setFont(GameConstants.BOLD_FONT);
        r.setFocusable(false);
        r.setBounds(x, y, 70, 30);
        body.add(r);
        return r;
    }

    private JSlider slider(int value, int x, int y) {
        JSlider s = new JSlider(0, 100, value);
        s.setMajorTickSpacing(25);
        s.setPaintTicks(true);
        s.setOpaque(false);
        s.setFocusable(false);
        s.setBounds(x, y, 210, 45);
        body.add(s);
        return s;
    }
}

// =====================================================================
//  CreditDialog : IDs and names of group members
// =====================================================================
class CreditDialog extends BaseDialog {
    public CreditDialog(Window owner) {
        super(owner, "Credits", 460, 360);
        String[][] m = GameConstants.MEMBERS;
        body.setLayout(new GridLayout(m.length + 2, 1, 0, 4));
        body.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel head = GameConstants.makeLabel("Developed by", GameConstants.HEADER_FONT, GameConstants.GOLD);
        head.setHorizontalAlignment(SwingConstants.CENTER);
        body.add(head);

        for (String[] member : m) {
            JLabel l = GameConstants.makeLabel(member[0] + "     " + member[1], GameConstants.BOLD_FONT, GameConstants.TEXT);
            l.setHorizontalAlignment(SwingConstants.CENTER);
            body.add(l);
        }
        JLabel foot = GameConstants.makeLabel("EGCO213 - Project 3", GameConstants.NORMAL_FONT, Color.LIGHT_GRAY);
        foot.setHorizontalAlignment(SwingConstants.CENTER);
        body.add(foot);
    }
}

// =====================================================================
//  LeaderboardDialog : one tab per map, JTable of best scores
// =====================================================================
class LeaderboardDialog extends BaseDialog {
    public LeaderboardDialog(Window owner, String selectMap) {
        super(owner, "Leaderboard", 660, 440);
        body.setLayout(new BorderLayout());
        body.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(GameConstants.BOLD_FONT);
        for (int i = 0; i < MapData.MAPS.length; i++) {
            String name = MapData.MAPS[i].getName();
            tabs.addTab(name, buildTab(name));
            if (name.equals(selectMap)) tabs.setSelectedIndex(i);
        }
        body.add(tabs, BorderLayout.CENTER);
    }

    private JPanel buildTab(String map) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(GameConstants.PANEL_BG);
        ArrayList<Leaderboard.Record> list = Leaderboard.getRanking(map);

        if (list.isEmpty()) {
            JLabel none = GameConstants.makeLabel("NO RECORD", GameConstants.HEADER_FONT, Color.LIGHT_GRAY);
            none.setHorizontalAlignment(SwingConstants.CENTER);
            p.add(none, BorderLayout.CENTER);
            return p;
        }

        String[] cols = {"Rank", "Name", "Waves", "Mobs Killed"};
        Object[][] data = new Object[list.size()][4];
        for (int i = 0; i < list.size(); i++) {
            Leaderboard.Record r = list.get(i);
            data[i][0] = i + 1;
            data[i][1] = r.name;
            data[i][2] = r.waves;
            data[i][3] = r.mobs;
        }
        DefaultTableModel model = new DefaultTableModel(data, cols) {
            public boolean isCellEditable(int row, int col) { return false; }
        };
        JTable table = new JTable(model);
        table.setRowHeight(26);
        table.setFont(GameConstants.NORMAL_FONT);
        table.getTableHeader().setFont(GameConstants.BOLD_FONT);
        table.getTableHeader().setReorderingAllowed(false);
        p.add(new JScrollPane(table), BorderLayout.CENTER);
        return p;
    }
}

// =====================================================================
//  PauseDialog : Resume / How to Play / Settings / Exit to menu
// =====================================================================
class PauseDialog extends BaseDialog {
    public PauseDialog(final GameFrame game) {
        super(game, "Paused", 340, 330);
        body.setLayout(new GridLayout(4, 1, 0, 12));
        body.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        JButton resume = GameConstants.makeButton("Resume", GameConstants.GREEN);
        JButton how = GameConstants.makeButton("How to Play", GameConstants.BLUE);
        JButton setting = GameConstants.makeButton("Settings", new Color(95, 95, 120));
        JButton exit = GameConstants.makeButton("Exit to Main Menu", GameConstants.RED);

        resume.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { dispose(); }
        });
        how.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { new HowToPlayDialog(PauseDialog.this).setVisible(true); }
        });
        setting.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { new SettingDialog(PauseDialog.this).setVisible(true); }
        });
        exit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
                game.exitToMenu();
            }
        });

        body.add(resume); body.add(how); body.add(setting); body.add(exit);

        // press P or ESC again to resume
        body.setFocusable(true);
        body.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_P || e.getKeyCode() == KeyEvent.VK_ESCAPE) dispose();
            }
        });
        addWindowListener(new WindowAdapter() {
            public void windowOpened(WindowEvent e) { body.requestFocusInWindow(); }
        });
    }
}

// =====================================================================
//  EndGameDialog : Victory / Defeat summary
// =====================================================================
class EndGameDialog extends BaseDialog {
    public EndGameDialog(GameFrame game, boolean victory, String name, final String map, int waves, int kills) {
        super(game, victory ? "Victory" : "Defeat", 460, 380);
        body.setLayout(null);

        JLabel result = GameConstants.makeLabel(victory ? "VICTORY!" : "DEFEAT",
                new Font("SansSerif", Font.BOLD, 44), victory ? GameConstants.GOLD : GameConstants.RED);
        result.setHorizontalAlignment(SwingConstants.CENTER);
        result.setBounds(0, 15, 454, 55);
        body.add(result);

        String[] info = {
            "Player: " + name,
            "Map: " + map,
            "Waves Completed: " + waves + " / " + GameConstants.MAX_WAVE,
            "Mobs Killed: " + kills
        };
        for (int i = 0; i < info.length; i++) {
            JLabel l = GameConstants.makeLabel(info[i], GameConstants.HEADER_FONT, GameConstants.TEXT);
            l.setHorizontalAlignment(SwingConstants.CENTER);
            l.setBounds(0, 85 + i * 36, 454, 30);
            body.add(l);
        }

        JButton boardBtn = GameConstants.makeButton("Leaderboard", new Color(200, 140, 30));
        boardBtn.setBounds(50, 250, 160, 44);
        boardBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new LeaderboardDialog(EndGameDialog.this, map).setVisible(true);
            }
        });
        JButton menuBtn = GameConstants.makeButton("Main Menu", GameConstants.BLUE);
        menuBtn.setBounds(244, 250, 160, 44);
        menuBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { dispose(); }
        });
        body.add(boardBtn);
        body.add(menuBtn);
    }
}