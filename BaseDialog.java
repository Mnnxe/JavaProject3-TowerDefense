package JavaProject3_TowerDefense;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.DefaultTableModel;

// =====================================================================
//  BaseDialog : parent of every pop-up (title bar + X button + PNG background)
// =====================================================================
public class BaseDialog extends JDialog {
    //protected JPanel body;
    protected JLabel titleLabel;
    protected ImagePanel body;
    protected JButton closeBtn;

    public BaseDialog(Window owner, String title, int w, int h) {
        super(owner, title, Dialog.ModalityType.APPLICATION_MODAL);
        setUndecorated(true);
        setBackground(new Color(0,0,0,0));
        setSize(w, h);

        JPanel root = new JPanel();
        root.setLayout(null);
        root.setOpaque(false);

        int bodyX = 30;
        int bodyY = 35;

        int bodyW = w - 60;
        int bodyH = h - 65;

        int titleW = 220;
        int titleH = 60;

        int titleX = bodyX + (bodyW - titleW) / 2;
        int titleY = 5;

        ImageIcon titleIcon = GameConstants.getIcon("btn_yellow.png", 220, 60);
        titleLabel = new JLabel(title,titleIcon,SwingConstants.CENTER);
        titleLabel.setHorizontalTextPosition(SwingConstants.CENTER);
        titleLabel.setVerticalTextPosition(SwingConstants.CENTER);
        titleLabel.setFont(GameConstants.HEADER_FONT);
        titleLabel.setForeground(Color.BLACK);
        titleLabel.setBounds(titleX,titleY,titleW,titleH);

        body = new ImagePanel(GameConstants.BAGE);        
        body.setLayout(null);
        body.setBorder(
        BorderFactory.createLineBorder(Color.BLACK, 3));
        body.setBounds(bodyX, bodyY, bodyW, bodyH);
        
        int closeSize = 45;
        closeBtn = GameConstants.makeImageButton("btn_close.png","",GameConstants.RED,45, 45,14);
        closeBtn.setBounds(bodyX + bodyW - closeSize / 2,bodyY - closeSize / 2,closeSize,closeSize);        
        closeBtn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    SoundManager.playSFX("click");
                    onClose();
                }
            }
        );

        root.add(titleLabel);
        root.add(closeBtn);
        root.add(body);

        root.setComponentZOrder(closeBtn, 0);
        root.setComponentZOrder(titleLabel, 1);
        root.setComponentZOrder(body, 2);


        setContentPane(root);
        setLocationRelativeTo(owner);
    }

    protected void hideTitle() {
        titleLabel.setVisible(false);
    }
 
    protected void hideCloseButton() {
        closeBtn.setVisible(false);
    }

    protected void hideBody() {
        body.setTransparent();
    }

    protected void onClose() { dispose(); }
}

// =====================================================================
//  ExitDialog : "Quit this game?"  Yes / No
// =====================================================================
class ExitDialog extends BaseDialog {
    public ExitDialog(Window owner) {
        super(owner, "Exit", 480, 240);
        hideCloseButton();
        body.setLayout(null);

        JLabel q = GameConstants.makeLabel("Quit this game?", GameConstants.HEADER_FONT, GameConstants.TEXT);
        q.setHorizontalAlignment(SwingConstants.CENTER);
        q.setBounds(0, 30, body.getWidth(), 50);

        int startX = 100;

        JButton yes = GameConstants.makeImageButton("btn_yes.png", "Yes", GameConstants.RED, 100, 40, 16);
        yes.setBounds(startX, 100, 100, 40);
        yes.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                SoundManager.stopBGM();
                System.exit(0);
            }
        });

        JButton no = GameConstants.makeImageButton("btn_no.png", "No", GameConstants.BLUE, 100, 40, 16);
        no.setBounds(startX+yes.getWidth()+20, 100, 100, 40);
        no.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { 
                SoundManager.playSFX("click");
                onClose();
            }
        });

        body.add(q);
        body.add(yes);
        body.add(no);
    }
}

// =====================================================================
//  HowToPlayDialog : pages with Previous / Next
//  Uses howto_1.png ... howto_5.png. If a picture is missing -> text page.
// =====================================================================
class HowToPlayDialog extends BaseDialog {

    private static final String[] TITLES = {
        "Goal",
        "Building Towers",
        "Upgrade & Sell",
        "Controls",
        "Enemies"
    };

    private static final String[] PAGES = {

        "Mobs walk along the path from the purple portal to your castle.\n\n"
      + "- Every mob that reaches the castle takes away base HP.\n"
      + "- Base HP starts at 100 and can NOT be recovered.\n"
      + "- If it hits 0 -> DEFEAT.\n"
      + "- Survive all 10 waves -> VICTORY!\n\n"
      + "Wave 5 has a Mini Boss and wave 10 has the Final Boss.",


        "1. Click a tower in the list at the bottom (or press 1-4).\n"
      + "2. Move the mouse over the map\n"
      + "3. Click an empty tile to build.\n\n"
      + "You cannot build on the path"
      + "or without enough coins\n\n"
      + "Archer (50c) - single target\n"
      + "Cannon (80c) - splash damage 1 grid\n"
      + "Ice (100c) - slows enemies\n"
      + "Fire (130c) - burns enemies over time",


        "UPGRADE: click a tower that is already on the map (max Lv.3).\n\n"
      + "SELL: turn on 'Deconstruct' (or press S), then click a tower.\n"
      + "You get back 70% of all coins you spent on that tower.\n\n"
      + "IMPORTANT: once a wave starts you cannot build, upgrade or sell.\n\n"
      + "Coins: start with 150, earn from kills, and get "
      + "30 + (wave x 5) bonus after each wave.",


        "SPACE - start next wave\n"
      + "P - pause menu\n"
      + "1 / 2 / 3 / 4 - select Archer / Cannon / Ice / Fire\n"
      + "S - toggle deconstruct mode\n"
      + "ESC / Right click - cancel selection\n\n"
      + "Speed box: play at x1 or x2 speed.",

    };


    private int page = 0;

    private JLabel pageTitle;
    private JLabel pageNumber;

    private JTextArea textArea;

    private JButton prevBtn;
    private JButton nextBtn;


    public HowToPlayDialog(Window owner) {

        // howto_bg.png = รูปพื้นหลังของ body
        super(owner,"HOW TO PLAY",620,460);

        // ================= PAGE TITLE =================
        pageTitle = GameConstants.makeLabel("",GameConstants.HEADER_FONT,GameConstants.TEXT);
        pageTitle.setBounds(0, 35, body.getWidth(), 35);
        pageTitle.setHorizontalAlignment(SwingConstants.CENTER);
        body.add(pageTitle);

        // ================= TEXT =================
        textArea = new JTextArea();

        textArea.setEditable(false);
        textArea.setFocusable(false);

        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);

        // ใช้ custom font
        textArea.setFont(GameConstants.GAME_FONT.deriveFont(Font.BOLD, (float) 16));
        textArea.setForeground(GameConstants.TEXT);

        // สำคัญ:
        // ทำให้พื้นหลัง JTextArea โปร่งใส
        // จะได้เห็น howto_bg.png ข้างหลัง
        textArea.setOpaque(false);
        textArea.setBounds(55, 85,450, 250);
        body.add(textArea);

        int sizebtn = 30;
        // ================= PREVIOUS =================
        prevBtn = GameConstants.makeImageButton("btn_prev.png","",GameConstants.BLUE,sizebtn, sizebtn,14);
        prevBtn.setBounds(60, 350, sizebtn, sizebtn);
        prevBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                changePage(-1);
            }
        });

        body.add(prevBtn);


        // ================= PAGE NUMBER =================
        pageNumber = GameConstants.makeLabel("",GameConstants.BOLD_FONT,GameConstants.TEXT);
        pageNumber.setHorizontalAlignment(SwingConstants.CENTER);
        pageNumber.setBounds(200, 350,160, 30);
        body.add(pageNumber);


        // ================= NEXT =================
        nextBtn = GameConstants.makeImageButton("btn_next.png","",GameConstants.BLUE,sizebtn,sizebtn,14);
        nextBtn.setBounds(450, 350, sizebtn, sizebtn);

        nextBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                changePage(1);
            }
        });

        body.add(nextBtn);
        // แสดงหน้าแรก
        showPage();
    }


    // ================= CHANGE PAGE =================
    private void changePage(int d) {

        page = page + d;

        if (page < 0) {
            page = 0;
        }

        if (page >= PAGES.length) {
            page = PAGES.length - 1;
        }

        SoundManager.playSFX("click");
        showPage();
    }


    // ================= SHOW PAGE =================
    private void showPage() {

        pageTitle.setText(TITLES[page]);
        textArea.setText(PAGES[page]);
        textArea.setCaretPosition(0);
        pageNumber.setText("Page "+ (page + 1)+ " / "+ PAGES.length);
        prevBtn.setEnabled(page > 0);
        nextBtn.setEnabled(
                page < PAGES.length - 1
        );
    }
}

// =====================================================================
//  CreditDialog : credits.png (must contain names + IDs), or text fallback
// =====================================================================
class CreditDialog extends BaseDialog {
    
    private static final String[][] MEMBERS = {
        {"6813115", "Renuka",     "Choosingkae"},
        {"6813238", "Napat",      "Chonsaktrakul"},
        {"6813248", "Pimpichcha", "Saengngoen"},
        {"6813258", "Sittichok",  "Boonthittanont"},
    };

    public CreditDialog(Window owner) {
        
        super(owner,"CREDITS",580,260);
        body.setLayout(null);


/*
// =========================
// DEVELOPED BY
// =========================
JLabel head = GameConstants.makeLabel(
    "Developed by",
    GameConstants.HEADER_FONT,
    Color.BLACK
);

head.setHorizontalAlignment(
    SwingConstants.CENTER
);

head.setBounds(
    30, 45,
    340, 30
);

body.add(head);

*/

        // =========================
        // MEMBERS
        // =========================
        int y = 50;
        

        for (String[] member : MEMBERS) {

            String text = String.format("%-7s %-10s %-15s",member[0],member[1],member[2]);

            JLabel memberLabel = GameConstants.makeLabel(text,GameConstants.BOLD_FONT,GameConstants.TEXT);
            memberLabel.setHorizontalAlignment(SwingConstants.LEFT);
            memberLabel.setBounds(50, y,500, 25);
            body.add(memberLabel);
            y += 30;
        }

        

/*
// =========================
// FOOTER
// =========================
JLabel foot = GameConstants.makeLabel(
    "EGCO213 - Project 3",
    GameConstants.NORMAL_FONT,
    Color.DARK_GRAY
);

foot.setHorizontalAlignment(
    SwingConstants.CENTER
);

foot.setBounds(
    30, 245,
    340, 25
);

body.add(foot);
*/
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
        super(owner, "Settings", 480, 290);
        body.setLayout(null);

        int y = 50;

        // ---- audio ON / OFF ----
        label("Audio:", 30, y);
        onRadio = radio("ON", 140, y);
        offRadio = radio("OFF", 220, y);
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
        int bgmY = y+50;
        label("BGM:", 30, bgmY);
        bgmSlider = slider(SoundManager.getBgmVolume(), 130, bgmY);
        bgmValue = label(SoundManager.getBgmVolume() + "%", 350, bgmY);
        bgmSlider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                SoundManager.setBgmVolume(bgmSlider.getValue());
                bgmValue.setText(bgmSlider.getValue() + "%");
            }
        });

        // ---- SFX slider ----
        int sfxY = bgmY+50;
        label("SFX:", 30, sfxY);
        sfxSlider = slider(SoundManager.getSfxVolume(), 130, sfxY);
        sfxValue = label(SoundManager.getSfxVolume() + "%", 350, sfxY);
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
//  LeaderboardDialog : one tab per map, JTable of best scores
// =====================================================================
class LeaderboardDialog extends BaseDialog {
    public LeaderboardDialog(Window owner, String selectMap) {
        super(owner,"LEADERBOARD",800,440);
        body.setLayout(null);


        // =========================
        // TABS
        // =========================
        JTabbedPane tabs = new JTabbedPane();

        tabs.setFont(GameConstants.BOLD_FONT);
        tabs.setBounds(35, 55, body.getWidth() - 60, 290);

        // =========================
        // MAP TABS
        // =========================
        for (int i = 0; i < MapData.MAPS.length; i++) {

            String name = MapData.MAPS[i].getName();

            tabs.addTab(name,buildTab(name));

            if (name.equals(selectMap)) {
                tabs.setSelectedIndex(i);
            }
        }

        // วาง tabs บนรูป leaderboard_bg.png
        body.add(tabs);
    }


    // =============================================
    // BUILD EACH MAP TAB
    // =============================================
    private JPanel buildTab(String map) {

        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(GameConstants.PANEL_BG);

        ArrayList<Leaderboard.Record> list = Leaderboard.getRanking(map);

        // =========================
        // NO RECORD
        // =========================
        if (list.isEmpty()) {
            JLabel none = GameConstants.makeLabel("NO RECORD",GameConstants.HEADER_FONT,Color.LIGHT_GRAY);
            none.setHorizontalAlignment(SwingConstants.CENTER);
            p.add(none,BorderLayout.CENTER);

            return p;
        }

        // =========================
        // TABLE DATA
        // =========================
        String[] cols = {
            "Rank",
            "Name",
            "Waves",
            "Mobs Killed"
        };

        Object[][] data =
            new Object[list.size()][4];


        for (int i = 0; i < list.size(); i++) {

            Leaderboard.Record r =
                list.get(i);

            data[i][0] = i + 1;
            data[i][1] = r.name;
            data[i][2] = r.waves;
            data[i][3] = r.mobs;
        }


        // =========================
        // TABLE MODEL
        // =========================
        DefaultTableModel model = new DefaultTableModel(data, cols) {
            public boolean isCellEditable(int row,int col) {
                    return false;
                }
            };


        // =========================
        // TABLE
        // =========================
        JTable table = new JTable(model);
        table.setRowHeight(26);
        table.setFont(GameConstants.NORMAL_FONT);
        table.getTableHeader().setFont(GameConstants.BOLD_FONT);
        table.getTableHeader().setReorderingAllowed(false);

        // =========================
        // SCROLL
        // =========================
        JScrollPane scroll = new JScrollPane(table);
        p.add(scroll,BorderLayout.CENTER);
        return p;
    }
}