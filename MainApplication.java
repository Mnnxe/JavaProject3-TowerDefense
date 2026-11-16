package JavaProject3_TowerDefense;
import javax.swing.*;

public class MainApplication {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                MainMenuFrame menu = new MainMenuFrame();
                menu.setVisible(true);
                SoundManager.playBGM(0);
            }
        });
    }
}