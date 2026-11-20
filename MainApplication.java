// Group members: 6xxxxxxx Firstname Lastname 1, 6xxxxxxx Firstname Lastname 2,
//                6xxxxxxx Firstname Lastname 3, 6xxxxxxx Firstname Lastname 4,
//                6xxxxxxx Firstname Lastname 5
package JavaProject3_TowerDefense;
import javax.swing.*;

public class MainApplication {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                MainMenuFrame menu = new MainMenuFrame();
                menu.setVisible(true);
                SoundManager.playMenuBGM();
            }
        });
    }
}