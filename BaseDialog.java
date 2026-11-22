// 6813115 Renuka Choosingkae
// 6813238 Napat Chonsaktrakul
// 6813248 Pimpichcha Saengngoen
// 6813258 Sittichok Boonthittanont
package JavaProject3_TowerDefense;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.DefaultTableModel;

// =====================================================================
//  BaseDialog : parent of every pop-up (title picture + X button + beige body)
// =====================================================================
public class BaseDialog extends JDialog {
    private static final int MARGIN_X = 30, MARGIN_TOP = 35, MARGIN_BOTTOM = 30;
    private static final int TITLE_W = 220, TITLE_H = 60, TITLE_Y = 5;
    private static final int CLOSE_SIZE = 45, BORDER = 3;

    protected final JPanel body;
    private final JLabel titleLabel;
    private final JButton closeBtn;

    public BaseDialog(Window owner, String title, int w, int h) {
        super(owner, title, Dialog.ModalityType.APPLICATION_MODAL);
        setUndecorated(true);
        setBackground(GameConstants.TRANSPARENT);
        setSize(w, h);

        int bodyW = w - 2 * MARGIN_X;
        int bodyH = h - MARGIN_TOP - MARGIN_BOTTOM;

        body = new JPanel(null);
        body.setBackground(GameConstants.BEIGE);
        body.setBorder(BorderFactory.createLineBorder(Color.BLACK, BORDER));
        body.setBounds(MARGIN_X, MARGIN_TOP, bodyW, bodyH);

        titleLabel = new JLabel(title, GameConstants.getIcon("btn_yellow.png", TITLE_W, TITLE_H), SwingConstants.CENTER);
        titleLabel.setHorizontalTextPosition(SwingConstants.CENTER);
        titleLabel.setVerticalTextPosition(SwingConstants.CENTER);
        titleLabel.setFont(GameConstants.HEADER_FONT);
        titleLabel.setForeground(GameConstants.TEXT);
        titleLabel.setBounds(MARGIN_X + (bodyW - TITLE_W) / 2, TITLE_Y, TITLE_W, TITLE_H);

        closeBtn = GameConstants.makeIconButton("btn_close.png", "X", CLOSE_SIZE);
        closeBtn.setBounds(MARGIN_X + bodyW - CLOSE_SIZE / 2, MARGIN_TOP - CLOSE_SIZE / 2, CLOSE_SIZE, CLOSE_SIZE);
        closeBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { dispose(); }
        });

        JPanel root = new JPanel(null);
        root.setOpaque(false);
        root.add(closeBtn);      // added first = drawn on top
        root.add(titleLabel);
        root.add(body);
        setContentPane(root);
        setLocationRelativeTo(owner);
    }

    // ================= options for child dialogs =================
    protected void hideTitle()       { titleLabel.setVisible(false); }
    protected void hideCloseButton() { closeBtn.setVisible(false); }
    protected void hideBody() {
        body.setOpaque(false);
        body.setBorder(null);
    }

    // ================= helpers shared by every dialog =================
    protected int centerX(int width) { return (body.getWidth() - width) / 2; }

    protected JLabel addLabel(String text, Font font, int x, int y, int w, int h) {
        JLabel l = GameConstants.makeLabel(text, font, GameConstants.TEXT);
        l.setBounds(x, y, w, h);
        body.add(l);
        return l;
    }

    protected JLabel addCenteredLabel(String text, Font font, int y, int h) {
        JLabel l = addLabel(text, font, 0, y, body.getWidth(), h);
        l.setHorizontalAlignment(SwingConstants.CENTER);
        return l;
    }

    protected JButton addButton(String imageFile, String text, int x, int y, int w, int h) {
        JButton b = GameConstants.makeButton(imageFile, text, w, h);
        b.setBounds(x, y, w, h);
        body.add(b);
        return b;
    }
}

// =====================================================================
//  ExitDialog : "Quit this game?"  Yes / No
// =====================================================================
class ExitDialog extends BaseDialog {
    private static final int BTN_W = 100, BTN_H = 40, GAP = 20, BTN_Y = 100;

    public ExitDialog(Window owner) {
        super(owner, "Exit", 480, 240);
        hideCloseButton();
        addCenteredLabel("Quit this game?", GameConstants.HEADER_FONT, 30, 50);

        int x = centerX(BTN_W * 2 + GAP);
        JButton yes = addButton("btn_yes.png", "Yes", x, BTN_Y, BTN_W, BTN_H);
        JButton no  = addButton("btn_no.png", "No", x + BTN_W + GAP, BTN_Y, BTN_W, BTN_H);

        yes.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                SoundManager.stopBGM();
                System.exit(0);
            }
        });
        no.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { dispose(); }
        });
    }
}

// =====================================================================
//  HowToPlayDialog : text pages with Previous / Next
//  (numbers come from the constants, so the text always matches the game)
// =====================================================================
class HowToPlayDialog extends BaseDialog {
    private static final int NAV_SIZE = 30, NAV_Y = 350, NAV_MARGIN = 60;

    private static final String[] TITLES = {"Goal", "Building Towers", "Upgrade & Sell", "Controls"};

    private static final String[] PAGES = {
        "Mobs walk along the path from the portal to your base.\n\n"
      + "- Every mob that reaches the base takes away base HP.\n"
      + "- Base HP starts at " + GameConstants.MAX_HP + " and can NOT be recovered.\n"
      + "- If it hits 0 -> DEFEAT.\n"
      + "- Survive all " + GameConstants.MAX_WAVE + " waves -> VICTORY!\n\n"
      + "Wave 5 has a Mini Boss and wave 10 has the Final Boss.",

        "1. Click a tower in the list at the bottom (or press 1-4).\n"
      + "2. Move the mouse over the map to see its range.\n"
      + "3. Click an empty tile to build.\n"
      + "You cannot build on the path or without enough coins.\n\n"
      + towerList(),

        "UPGRADE: click a tower on the map (max Lv." + TowerType.MAX_LEVEL + ").\n\n"
      + "SELL: turn on 'Deconstruct' (or press S), then click a tower. "
      + "You get back " + Math.round(GameConstants.REFUND_RATE * 100) + "% of all coins spent on it.\n\n"
      + "Once a wave starts you cannot build, upgrade or sell.\n\n"
      + "Coins: start with " + GameConstants.START_COINS + ", earn from kills, and get "
      + GameConstants.WAVE_BONUS + " + (wave x " + GameConstants.WAVE_BONUS_PER_WAVE + ") bonus after each wave.",

        "SPACE - start next wave\n"
      + "P - pause menu\n"
      + "1 / 2 / 3 / 4 - select Archer / Cannon / Ice / Fire\n"
      + "S - toggle deconstruct mode\n"
      + "ESC / Right click - cancel selection\n\n"
      + "Speed box: play at x1 or x2 speed."
    };

    private int page = 0;
    private JLabel pageTitle, pageNumber;
    private JTextArea textArea;
    private JButton prevBtn, nextBtn;

    public HowToPlayDialog(Window owner) {
        super(owner, "HOW TO PLAY", 620, 460);

        pageTitle = addCenteredLabel("", GameConstants.HEADER_FONT, 35, 35);

        textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setFocusable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setFont(GameConstants.TEXT_FONT);
        textArea.setForeground(GameConstants.TEXT);
        textArea.setOpaque(false);
        textArea.setBounds(55, 85, 450, 250);
        body.add(textArea);

        prevBtn = addNavButton("btn_prev.png", "<", NAV_MARGIN, -1);
        nextBtn = addNavButton("btn_next.png", ">", body.getWidth() - NAV_MARGIN - NAV_SIZE, 1);
        pageNumber = addLabel("", GameConstants.BOLD_FONT, centerX(160), NAV_Y, 160, NAV_SIZE);
        pageNumber.setHorizontalAlignment(SwingConstants.CENTER);

        showPage();
    }

    // one line per tower: name, cost, unlock wave and skill
    private static String towerList() {
        String s = "";
        for (TowerType t : TowerType.values())
            s += t.getName() + " (" + t.getCost(1) + "c, wave " + t.getUnlockWave() + "+) - " + t.getSkill(1) + "\n";
        return s;
    }

    private JButton addNavButton(String imageFile, String text, int x, final int step) {
        JButton b = GameConstants.makeIconButton(imageFile, text, NAV_SIZE);
        b.setBounds(x, NAV_Y, NAV_SIZE, NAV_SIZE);
        b.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { changePage(step); }
        });
        body.add(b);
        return b;
    }

    private void changePage(int step) {
        page = Math.max(0, Math.min(PAGES.length - 1, page + step));
        showPage();
    }

    private void showPage() {
        pageTitle.setText(TITLES[page]);
        textArea.setText(PAGES[page]);
        pageNumber.setText("Page " + (page + 1) + " / " + PAGES.length);
        prevBtn.setEnabled(page > 0);
        nextBtn.setEnabled(page < PAGES.length - 1);
    }
}

// =====================================================================
//  CreditDialog : IDs and names of all group members
// =====================================================================
class CreditDialog extends BaseDialog {
    private static final String[][] MEMBERS = {
        {"6813115", "Renuka",     "Choosingkae"},
        {"6813238", "Napat",      "Chonsaktrakul"},
        {"6813248", "Pimpichcha", "Saengngoen"},
        {"6813258", "Sittichok",  "Boonthittanont"}
    };
    private static final int[] COLUMN_X = {50, 160, 310};
    private static final int FIRST_Y = 50, ROW_H = 30, CELL_W = 200, CELL_H = 25;

    public CreditDialog(Window owner) {
        super(owner, "CREDITS", 580, 260);
        for (int row = 0; row < MEMBERS.length; row++)
            for (int col = 0; col < COLUMN_X.length; col++)
                addLabel(MEMBERS[row][col], GameConstants.BOLD_FONT,
                         COLUMN_X[col], FIRST_Y + row * ROW_H, CELL_W, CELL_H);
    }
}

// =====================================================================
//  SettingDialog : audio ON/OFF radio buttons + BGM / SFX sliders
// =====================================================================
class SettingDialog extends BaseDialog {
    private static final int LABEL_X = 30, SLIDER_X = 130, VALUE_X = 350;
    private static final int FIRST_Y = 50, ROW_GAP = 50;

    private JRadioButton onRadio, offRadio;
    private JSlider bgmSlider, sfxSlider;

    public SettingDialog(Window owner) {
        super(owner, "Settings", 480, 290);

        // ---- audio ON / OFF ----
        addLabel("Audio:", GameConstants.BOLD_FONT, LABEL_X, FIRST_Y, 90, 25);
        onRadio = addRadio("ON", 140, FIRST_Y);
        offRadio = addRadio("OFF", 220, FIRST_Y);
        ButtonGroup group = new ButtonGroup();
        group.add(onRadio);
        group.add(offRadio);
        if (SoundManager.isAudioOn()) onRadio.setSelected(true);
        else offRadio.setSelected(true);

        // ---- volume sliders ----
        bgmSlider = addVolumeRow("BGM:", SoundManager.getBgmVolume(), FIRST_Y + ROW_GAP);
        sfxSlider = addVolumeRow("SFX:", SoundManager.getSfxVolume(), FIRST_Y + 2 * ROW_GAP);
        setSlidersEnabled(SoundManager.isAudioOn());

        bgmSlider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) { SoundManager.setBgmVolume(bgmSlider.getValue()); }
        });
        sfxSlider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                SoundManager.setSfxVolume(sfxSlider.getValue());
                if (!sfxSlider.getValueIsAdjusting()) SoundManager.playSFX("place");   // preview
            }
        });

        ItemListener audioListener = new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() != ItemEvent.SELECTED) return;
                boolean on = onRadio.isSelected();
                SoundManager.setAudioOn(on);
                setSlidersEnabled(on);
            }
        };
        onRadio.addItemListener(audioListener);
        offRadio.addItemListener(audioListener);
    }

    private void setSlidersEnabled(boolean on) {
        bgmSlider.setEnabled(on);
        sfxSlider.setEnabled(on);
    }

    private JRadioButton addRadio(String text, int x, int y) {
        JRadioButton r = new JRadioButton(text);
        r.setOpaque(false);
        r.setForeground(GameConstants.TEXT);
        r.setFont(GameConstants.BOLD_FONT);
        r.setFocusable(false);
        r.setBounds(x, y, 70, 30);
        body.add(r);
        return r;
    }

    // one row = name label + slider + "xx%" label
    private JSlider addVolumeRow(String name, int value, int y) {
        addLabel(name, GameConstants.BOLD_FONT, LABEL_X, y, 90, 25);
        final JLabel valueLabel = addLabel(value + "%", GameConstants.BOLD_FONT, VALUE_X, y, 60, 25);

        final JSlider s = new JSlider(0, 100, value);
        s.setMajorTickSpacing(25);
        s.setPaintTicks(true);
        s.setOpaque(false);
        s.setFocusable(false);
        s.setBounds(SLIDER_X, y - 10, 210, 45);
        s.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) { valueLabel.setText(s.getValue() + "%"); }
        });
        body.add(s);
        return s;
    }
}

// =====================================================================
//  LeaderboardDialog : one tab per map, JTable of best scores
// =====================================================================
class LeaderboardDialog extends BaseDialog {
    private static final String[] COLUMNS = {"Rank", "Name", "Waves", "Mobs Killed"};
    private static final int TABS_W = 680, TABS_H = 290, TABS_Y = 55, ROW_H = 26;

    public LeaderboardDialog(Window owner, String selectMap) {
        super(owner, "LEADERBOARD", 800, 440);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(GameConstants.BOLD_FONT);
        tabs.setBounds(centerX(TABS_W), TABS_Y, TABS_W, TABS_H);

        for (MapData m : MapData.MAPS) {
        if (m.isRandom()) continue;

        String name = m.getName();
        tabs.addTab(name, buildTab(name));

        if (name.equals(selectMap))
            tabs.setSelectedIndex(tabs.getTabCount() - 1);
        }
        body.add(tabs);
    }

    private JComponent buildTab(String map) {
        ArrayList<Leaderboard.Record> list = Leaderboard.getRanking(map);
        if (list.isEmpty()) {
            JLabel none = GameConstants.makeLabel("NO RECORD", GameConstants.HEADER_FONT, Color.GRAY);
            none.setHorizontalAlignment(SwingConstants.CENTER);
            return none;
        }

        Object[][] data = new Object[list.size()][COLUMNS.length];
        for (int i = 0; i < list.size(); i++) {
            Leaderboard.Record r = list.get(i);
            data[i][0] = i + 1;
            data[i][1] = r.getName();
            data[i][2] = r.getWaves();
            data[i][3] = r.getMobs();
        }

        DefaultTableModel model = new DefaultTableModel(data, COLUMNS) {
            public boolean isCellEditable(int row, int col) { return false; }
        };
        JTable table = new JTable(model);
        table.setRowHeight(ROW_H);
        table.setFont(GameConstants.NORMAL_FONT);
        table.getTableHeader().setFont(GameConstants.BOLD_FONT);
        table.getTableHeader().setReorderingAllowed(false);
        return new JScrollPane(table);
    }
}