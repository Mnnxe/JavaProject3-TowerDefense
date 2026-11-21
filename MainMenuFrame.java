// 6813115 Renuka Choosingkae
// 6813238 Napat Chonsaktrakul
// 6813248 Pimpichcha Saengngoen
// 6813258 Sittichok Boonthittanont
package JavaProject3_TowerDefense;

import java.awt.event.*;
import javax.swing.*;

// =====================================================================
//  MainMenuFrame : first window (Play / How to Play / Credits / Exit + icons)
// =====================================================================
public class MainMenuFrame extends JFrame {
    private static final int W = GameConstants.WIN_W, H = GameConstants.WIN_H;
    private static final int TITLE_W = 680, TITLE_H = 250, TITLE_Y = 60;
    private static final int ICON_SIZE = 56, ICON_MARGIN = 20, ICON_GAP = 10, ICON_Y = 15;
    private static final int PLAY_W = 245, PLAY_H = 90;
    private static final int BTN_W = 140, BTN_H = 54, BTN_GAP = 10, BTN_X = 100;

    public MainMenuFrame() {
        super("Tower Defense: Professor Edition");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) { openDialog(new ExitDialog(MainMenuFrame.this)); }
        });
        setGlassPane(new ImagePanel(null, GameConstants.OVERLAY));   // dark layer behind pop-ups

        ImagePanel panel = new ImagePanel("menu_bg.png", GameConstants.DARK_BG);
        panel.setLayout(null);

        // ---- title (title.png or text) ----
        JLabel title = GameConstants.makeImageLabel("title.png", "TOWER DEFENSE",
                GameConstants.TITLE_FONT, GameConstants.GOLD, TITLE_W, TITLE_H);
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setBounds(0, TITLE_Y, TITLE_W, TITLE_H);
        panel.add(title);

        // ---- icon buttons (top-right) ----
        JButton settingBtn = GameConstants.makeIconButton("icon_setting.png", "SET", ICON_SIZE);
        JButton leaderBtn  = GameConstants.makeIconButton("icon_leaderboard.png", "TOP", ICON_SIZE);
        settingBtn.setBounds(W - ICON_MARGIN - ICON_SIZE, ICON_Y, ICON_SIZE, ICON_SIZE);
        leaderBtn.setBounds(W - ICON_MARGIN - 2 * ICON_SIZE - ICON_GAP, ICON_Y, ICON_SIZE, ICON_SIZE);

        // ---- main buttons ----
        JButton playBtn   = GameConstants.makeButton("btn_yellow.png", "PLAY", PLAY_W, PLAY_H, GameConstants.TITLE_FONT);
        JButton howBtn    = GameConstants.makeButton("btn_white.png", "HOW TO PLAY", BTN_W, BTN_H);
        JButton creditBtn = GameConstants.makeButton("btn_white.png", "CREDITS", BTN_W, BTN_H);
        JButton exitBtn   = GameConstants.makeButton("btn_white.png", "EXIT", BTN_W, BTN_H);

        int playX = (W - PLAY_W) / 4, playY = H / 2 + 50;
        int by = playY + PLAY_H + 30;
        playBtn.setBounds(playX, playY, PLAY_W, PLAY_H);
        howBtn.setBounds(BTN_X, by, BTN_W, BTN_H);
        creditBtn.setBounds(BTN_X + (BTN_W + BTN_GAP), by, BTN_W, BTN_H);
        exitBtn.setBounds(BTN_X + (BTN_W + BTN_GAP) * 2, by, BTN_W, BTN_H);

        // ---- events ----
        playBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new GetReadyFrame(MainMenuFrame.this).setVisible(true);
                setVisible(false);
            }
        });
        howBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { openDialog(new HowToPlayDialog(MainMenuFrame.this)); }
        });
        creditBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { openDialog(new CreditDialog(MainMenuFrame.this)); }
        });
        exitBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { openDialog(new ExitDialog(MainMenuFrame.this)); }
        });
        leaderBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { openDialog(new LeaderboardDialog(MainMenuFrame.this, null)); }
        });
        settingBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { openDialog(new SettingDialog(MainMenuFrame.this)); }
        });

        panel.add(leaderBtn);
        panel.add(settingBtn);
        panel.add(playBtn);
        panel.add(howBtn);
        panel.add(creditBtn);
        panel.add(exitBtn);

        GameConstants.setupFrame(this, panel, null);
    }

    // darkens the menu while a pop-up is open (dialogs are modal -> waits here)
    private void openDialog(JDialog dialog) {
        getGlassPane().setVisible(true);
        dialog.setVisible(true);
        getGlassPane().setVisible(false);
    }
}