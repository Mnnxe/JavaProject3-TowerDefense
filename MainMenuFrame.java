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
        super("Tower Defense: Main Menu");
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
            int w = 680;
            int h = 250;
            JLabel title = new JLabel(GameConstants.getIcon("title.png", w,h));
            title.setBounds(0, 60, w, h);
            panel.add(title);
        }

        // ---- icon buttons (top-right) ----
        JButton settingBtn = makeIconButton("icon_setting.png", "", GameConstants.GRAY);
        JButton leaderBtn = makeIconButton("icon_leaderboard.png", "", GameConstants.ORANGE);
        Dimension s = settingBtn.getPreferredSize(), l = leaderBtn.getPreferredSize();
        settingBtn.setBounds(W - 20 - s.width, 15, s.width, s.height);
        leaderBtn.setBounds(W - 30 - s.width - l.width, 15, l.width, l.height);

        // ---- main buttons ----
        
        int pw = 325-80, ph = 105-15;
        int bw = 220-80, bh = 69-15;
        int gap = 10;
        JButton playBtn   = GameConstants.makeImageButton("btn_yellow.png", "PLAY", GameConstants.GREEN, pw, ph, 50);
        JButton howBtn    = GameConstants.makeImageButton("btn_white.png", "HOW TO PLAY", GameConstants.BLUE, bw, bh, 17);
        JButton creditBtn = GameConstants.makeImageButton("btn_white.png", "CREDITS", GameConstants.PURPLE, bw, bh, 17);
        JButton exitBtn   = GameConstants.makeImageButton("btn_white.png", "EXIT", GameConstants.RED, bw, bh, 17);
        
        // Play button
        int playX = (W - pw) / 4;
        int playY = (H/2)+50;
        playBtn.setBounds(playX, playY, pw, ph);

        // Other buttons
        //int totalWidth = (bw * 3) + (gap * 2);
        int bx = 100;
        int by = playBtn.getY() + ph + 30;
        howBtn.setBounds(bx, by, bw, bh);
        creditBtn.setBounds(bx  + bw + gap, by, bw, bh);
        exitBtn.setBounds(bx + (bw + gap)*2, by, bw, bh);
        
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
                showDarkOverlay();
                new HowToPlayDialog(MainMenuFrame.this).setVisible(true);
                hideDarkOverlay();
            }
        });
        creditBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                SoundManager.playSFX("click");
                showDarkOverlay();
                new CreditDialog(MainMenuFrame.this).setVisible(true);
                hideDarkOverlay();
            }
        });
        exitBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                SoundManager.playSFX("click");
                showDarkOverlay();
                new ExitDialog(MainMenuFrame.this).setVisible(true);
                hideDarkOverlay();
            }
        });
        leaderBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                SoundManager.playSFX("click");
                showDarkOverlay();
                new LeaderboardDialog(MainMenuFrame.this, null).setVisible(true);
                hideDarkOverlay();
            }
        });
        settingBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                SoundManager.playSFX("click");
                showDarkOverlay();
                new SettingDialog(MainMenuFrame.this).setVisible(true);
                hideDarkOverlay();
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

    //dark overlay
    private void showDarkOverlay() {
        JPanel overlay = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                
                g.setColor(new Color(0, 0, 0, 120));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        
        overlay.setOpaque(false);
        setGlassPane(overlay);
        overlay.setVisible(true);
    }

    private void hideDarkOverlay() {
        getGlassPane().setVisible(false);
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

            g2.setFont( GameConstants.GAME_FONT.deriveFont(Font.BOLD, (float) 25));
            fm = g2.getFontMetrics();
            String s = "~ Professor Edition ~";
            g2.setColor(Color.WHITE);
            g2.drawString(s, (getWidth() - fm.stringWidth(s)) / 2, 190);
        }
    }
}