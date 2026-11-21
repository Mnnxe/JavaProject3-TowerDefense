// 6813115 Renuka Choosingkae
// 6813238 Napat Chonsaktrakul
// 6813248 Pimpichcha Saengngoen
// 6813258 Sittichok Boonthittanont
package JavaProject3_TowerDefense;

import java.awt.event.*;
import javax.swing.*;

// =====================================================================
//  PauseDialog : Resume / How to Play / Settings / Exit to menu
// =====================================================================
public class PauseDialog extends BaseDialog {
    private static final int BTN_W = 250, BTN_H = 48, FIRST_Y = 40, STEP_Y = 55;

    public PauseDialog(final GameFrame game) {
        super(game, "Paused", 340, 330);
        hideBody();
        hideCloseButton();

        int x = centerX(BTN_W);
        JButton resume  = addButton("btn_yellow.png", "Resume", x, FIRST_Y, BTN_W, BTN_H);
        JButton how     = addButton("btn_yellow.png", "How to Play", x, FIRST_Y + STEP_Y, BTN_W, BTN_H);
        JButton setting = addButton("btn_yellow.png", "Settings", x, FIRST_Y + 2 * STEP_Y, BTN_W, BTN_H);
        JButton exit    = addButton("btn_yellow.png", "Exit to Main Menu", x, FIRST_Y + 3 * STEP_Y, BTN_W, BTN_H);

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
    private static final int RESULT_Y = 12, RESULT_H = 80;
    private static final int INFO_Y = 95, INFO_STEP = 36, INFO_H = 30;
    private static final int BTN_W = 160, BTN_H = 44, BTN_GAP = 30, BTN_Y = 250;

    public EndGameDialog(GameFrame game, boolean victory, String name, final String map, int waves, int kills) {
        super(game, victory ? "Victory" : "Defeat", 500, 380);
        hideTitle();

        JLabel result = GameConstants.makeLabel(
                        victory ? "VICTORY!" : "DEFEAT",GameConstants.TITLE_FONT,
                        victory ? GameConstants.GOLD : GameConstants.RED
        );
        result.setHorizontalAlignment(SwingConstants.CENTER);
        result.setBounds(0, RESULT_Y, body.getWidth(), RESULT_H);
        body.add(result);

        String[] info = {
            "Player: " + name,
            "Map: " + map,
            "Waves Completed: " + waves + " / " + GameConstants.MAX_WAVE,
            "Mobs Killed: " + kills
        };
        for (int i = 0; i < info.length; i++)
            addCenteredLabel(info[i], GameConstants.NORMAL_FONT, INFO_Y + i * INFO_STEP, INFO_H);

        int x = centerX(BTN_W * 2 + BTN_GAP);
        JButton boardBtn = addButton("btn_yellow.png", "Leaderboard", x, BTN_Y, BTN_W, BTN_H);
        JButton menuBtn  = addButton("btn_yellow.png", "Main Menu", x + BTN_W + BTN_GAP, BTN_Y, BTN_W, BTN_H);

        boardBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new LeaderboardDialog(EndGameDialog.this, map).setVisible(true);
            }
        });
        menuBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { dispose(); }
        });
    }
}