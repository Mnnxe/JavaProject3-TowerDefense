// 6813115 Renuka Choosingkae
// 6813238 Napat Chonsaktrakul
// 6813248 Pimpichcha Saengngoen
// 6813258 Sittichok Boonthittanont
package JavaProject3_TowerDefense;

import java.io.File;
import javax.swing.*;

public class MainApplication {
    public static void main(String[] args) {
        File resources = new File(GameConstants.PATH);
        if (!resources.exists())
            System.out.println("Resources folder NOT found: " + resources.getAbsolutePath());

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new MainMenuFrame().setVisible(true);
                SoundManager.playMenuBGM();
            }
        });
    }
}