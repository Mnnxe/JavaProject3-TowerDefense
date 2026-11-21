package JavaProject3_TowerDefense;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

// =====================================================================
//  PauseDialog : Resume / How to Play / Settings / Exit to menu
// =====================================================================
public class PauseDialog extends BaseDialog {
    public PauseDialog(final GameFrame game) {
        super(game, "Paused", 340, 330);
        hideBody();
        hideCloseButton();
        body.setLayout(null);
        //body.setBorder(BorderFactory.createLineBorder(Color.BLACK, 3));
        
        int bw = 250;
        int bh = 48;
        int bx = (body.getWidth() - bw) / 2;

        JButton resume  = GameConstants.makeImageButton("btn_yellow.png", "Resume", GameConstants.GREEN, 250, 50, 16);
        JButton how     = GameConstants.makeImageButton("btn_yellow.png", "How to Play", GameConstants.BLUE, 250, 50, 16);
        JButton setting = GameConstants.makeImageButton("btn_yellow.png", "Settings", GameConstants.GRAY, 250, 50, 16);
        JButton exit    = GameConstants.makeImageButton("btn_yellow.png", "Exit to Main Menu", GameConstants.RED, 250, 50, 16);

        resume.setBounds(bx, 40, bw, bh);
        how.setBounds(bx, 95, bw, bh);
        setting.setBounds(bx, 150, bw, bh);
        exit.setBounds(bx, 205, bw, bh);
        resume.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { 
                SoundManager.playSFX("click");    
                dispose(); 
            }
        });
        how.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { 
                SoundManager.playSFX("click");
                new HowToPlayDialog(PauseDialog.this).setVisible(true); 
            }
        });
        setting.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { 
                SoundManager.playSFX("click");
                new SettingDialog(PauseDialog.this).setVisible(true); 
        }
        });
        exit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                SoundManager.playSFX("click");
                onClose();
                game.exitToMenu();
            }
        });

        body.add(resume); 
        body.add(how); 
        body.add(setting); 
        body.add(exit);

        // press P or ESC again to resume
        body.setFocusable(true);
        body.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {

                if (e.getKeyCode() == KeyEvent.VK_P
                    || e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    onClose();
                }
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
        super(game, victory ? "Victory" : "Defeat", 500, 380);
        hideTitle();
        body.setLayout(null);

        JLabel result = GameConstants.makeImageLabel(victory ? "victory.png" : "defeat.png",
                victory ? "VICTORY!" : "DEFEAT", GameConstants.HEADER_FONT,
                victory ? GameConstants.GOLD : GameConstants.RED, 500, 60);
        result.setHorizontalAlignment(SwingConstants.CENTER);
        result.setBounds(0, 12, body.getWidth(), 80);
        body.add(result);

        String[] info = {
            "Player: " + name,
            "Map: " + map,
            "Waves Completed: " + waves + " / " + GameConstants.MAX_WAVE,
            "Mobs Killed: " + kills
        };
        for (int i = 0; i < info.length; i++) {
            JLabel l = GameConstants.makeLabel(info[i], GameConstants.NORMAL_FONT, GameConstants.TEXT);
            l.setHorizontalAlignment(SwingConstants.CENTER);
            l.setBounds(0, 85 + i * 36, body.getWidth(), 30);
            body.add(l);
        }

        int bw = 160;
        int bh = 44;
        int gap = 30;

        int totalW = bw * 2 + gap;

        int startX = (body.getWidth() - totalW) / 2;


        JButton boardBtn = GameConstants.makeImageButton("btn_yellow.png", "Leaderboard", GameConstants.ORANGE, 160, 44, 16);
        boardBtn.setBounds(startX, 250, bw, bh);
        boardBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                SoundManager.playSFX("click");
                new LeaderboardDialog(EndGameDialog.this, map).setVisible(true);
            }
        });
        JButton menuBtn = GameConstants.makeImageButton("btn_yellow", "Main Menu", GameConstants.BLUE, 160, 44, 16);
        menuBtn.setBounds(startX+bw+gap, 250, bw, bh);
        menuBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { 
                SoundManager.playSFX("click");
                dispose(); 
            }
        });
        body.add(boardBtn);
        body.add(menuBtn);
    }
}