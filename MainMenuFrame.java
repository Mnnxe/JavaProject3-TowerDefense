// Group members: 6xxxxxxx Firstname Lastname 1, 6xxxxxxx Firstname Lastname 2,
//                6xxxxxxx Firstname Lastname 3, 6xxxxxxx Firstname Lastname 4,
//                6xxxxxxx Firstname Lastname 5
package JavaProject3_TowerDefense;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

// =====================================================================
//  MainMenuFrame : first window (Play / How to Play / Credits / Exit + icons)
// =====================================================================
public class MainMenuFrame extends JFrame {
    private static final int W = GameConstants.WIN_W, H = GameConstants.WIN_H;

    public MainMenuFrame() {
        super("Tower Defense: Professor Edition");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                new ExitDialog(MainMenuFrame.this).setVisible(true);
            }
        });

        MenuPanel panel = new MenuPanel();
        panel.setLayout(null);

        // ---- title picture (if title.png exists, otherwise MenuPanel draws text) ----
        if (GameConstants.hasImage("title.png")) {
            JLabel title = new JLabel(GameConstants.getIcon("title.png", 640, 170));
            title.setBounds((W - 640) / 2, 40, 640, 170);
            panel.add(title);
        }

        // ---- icon buttons (top-right) ----
        JButton settingBtn = makeIconButton("icon_setting.png", "Settings", GameConstants.GRAY);
        JButton leaderBtn = makeIconButton("icon_leaderboard.png", "Leaderboard", GameConstants.ORANGE);
        Dimension s = settingBtn.getPreferredSize(), l = leaderBtn.getPreferredSize();
        settingBtn.setBounds(W - 20 - s.width, 15, s.width, s.height);
        leaderBtn.setBounds(W - 30 - s.width - l.width, 15, l.width, l.height);

        // ---- main buttons ----
        int bw = 280, bx = (W - bw) / 2;
        JButton playBtn   = GameConstants.makeImageButton("btn_play.png", "PLAY", GameConstants.GREEN, bw, 66, 26);
        JButton howBtn    = GameConstants.makeImageButton("btn_howtoplay.png", "HOW TO PLAY", GameConstants.BLUE, bw, 52, 18);
        JButton creditBtn = GameConstants.makeImageButton("btn_credits.png", "CREDITS", GameConstants.PURPLE, bw, 52, 18);
        JButton exitBtn   = GameConstants.makeImageButton("btn_exit.png", "EXIT", GameConstants.RED, bw, 52, 18);
        playBtn.setBounds(bx, 265, bw, 66);
        howBtn.setBounds(bx, 345, bw, 52);
        creditBtn.setBounds(bx, 410, bw, 52);
        exitBtn.setBounds(bx, 475, bw, 52);

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
                SoundManager.playSFX("click");
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

        GameConstants.setupFrame(this, panel, null);
    }

    // square icon if PNG exists, otherwise a normal text button
    private JButton makeIconButton(String file, String text, Color bg) {
        if (GameConstants.hasImage(file)) return GameConstants.makeImageButton(file, text, bg, 56, 56, 14);
        return GameConstants.makeImageButton(file, text, bg, 140, 40, 14);
    }

    // ================= background of the menu =================
    class MenuPanel extends ImagePanel {
        public MenuPanel() {
            super("menu_bg.png", new Color(25, 30, 70), new Color(90, 40, 110));
        }

        // drawn only when menu_bg.png is missing
        protected void drawFallback(Graphics2D g2) {
            int h = getHeight();
            g2.setColor(new Color(60, 120, 60));
            g2.fillRect(0, h - 70, getWidth(), 70);
            g2.setColor(new Color(170, 130, 80));
            g2.fillRect(0, h - 45, getWidth(), 22);
            for (int i = 0; i < 7; i++) {
                int x = 60 + i * 160;
                g2.setColor(new Color(130, 130, 140));
                g2.fillRect(x, h - 110, 30, 50);
                g2.fillRect(x - 4, h - 118, 10, 10);
                g2.fillRect(x + 10, h - 118, 10, 10);
                g2.fillRect(x + 24, h - 118, 10, 10);
            }
        }

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (GameConstants.hasImage("title.png")) return;   // title is a picture
            Graphics2D g2 = (Graphics2D) g;
            g2.setFont(GameConstants.TITLE_FONT);
            FontMetrics fm = g2.getFontMetrics();
            String t = "TOWER DEFENSE";
            int tx = (getWidth() - fm.stringWidth(t)) / 2;
            g2.setColor(Color.BLACK);
            g2.drawString(t, tx + 4, 144);
            g2.setColor(GameConstants.GOLD);
            g2.drawString(t, tx, 140);

            g2.setFont(new Font("SansSerif", Font.BOLD | Font.ITALIC, 24));
            fm = g2.getFontMetrics();
            String s = "~ Professor Edition ~";
            g2.setColor(Color.WHITE);
            g2.drawString(s, (getWidth() - fm.stringWidth(s)) / 2, 190);
        }
    }
}