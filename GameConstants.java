// 6813115 Renuka Choosingkae
// 6813238 Napat Chonsaktrakul
// 6813248 Pimpichcha Saengngoen
// 6813258 Sittichok Boonthittanont
package JavaProject3_TowerDefense;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.HashMap;
import javax.imageio.ImageIO;
import javax.swing.*;

import org.w3c.dom.events.MouseEvent;

// =====================================================================
//  GameConstants : shared values + helper methods (frame, images, buttons, labels)
// =====================================================================
public class GameConstants {

    public static final String PROJECT_FOLDER   = "JavaProject3_TowerDefense";
    public static final String PATH             = "src/main/java/" + PROJECT_FOLDER + "/resources/";
    //public static final String PATH             = "resources/";

    public static final String IMG_PATH         = PATH + "images/";
    public static final String AUDIO_PATH       = PATH + "audio/";
    public static final String FONT_FILE        = PATH + "LcdSolid-VPzB.ttf";
    public static final String LEADERBOARD_FILE = PATH + "leaderboard.txt";

    // ---------- window (every frame uses this content size) ----------
    public static final int WIN_W = 1100, WIN_H = 650;

    // ---------- map grid ----------
    public static final int COLS = 22, ROWS = 11, CELL = 46;
    public static final int MAP_W = COLS * CELL, MAP_H = ROWS * CELL;

    // ---------- game rules ----------
    public static final int MAX_HP = 100;
    public static final int START_COINS = 100;
    public static final int MAX_COINS = 999999;
    public static final int MAX_WAVE = 10;
    public static final double REFUND_RATE = 0.70;
    public static final int WAVE_BONUS = 30, WAVE_BONUS_PER_WAVE = 5;
    public static final int NAME_MAX_LENGTH = 15;

    // ---------- colours ----------
    public static final Color DARK_BG     = new Color(48, 33, 2);
    public static final Color PANEL_BG    = new Color(61, 54, 48);
    public static final Color BEIGE       = new Color(255, 250, 220);
    public static final Color SECTION_BG  = new Color(255, 250, 220, 200);
    public static final Color BROWN       = new Color(120, 80, 45);
    public static final Color GOLD        = new Color(255, 200, 60);
    public static final Color TEXT        = new Color(0, 0, 0);
    public static final Color RED         = new Color(215, 70, 70);
    public static final Color GREEN       = new Color(70, 180, 100);
    public static final Color ORANGE      = new Color(255, 160, 40);
    public static final Color OVERLAY     = new Color(0, 0, 0, 140);
    public static final Color TOOLTIP_BG  = new Color(20, 20, 30, 220);
    public static final Color GRID_LINE   = new Color(0, 0, 0, 35);
    public static final Color TRANSPARENT = new Color(0, 0, 0, 0);

    // ---------- fonts (one font file, different sizes) ----------
    public static final Font GAME_FONT   = loadGameFont();
    public static final Font TITLE_FONT  = GAME_FONT.deriveFont(Font.BOLD, 52f);
    public static final Font BANNER_FONT = GAME_FONT.deriveFont(Font.BOLD, 40f);
    public static final Font HEADER_FONT = GAME_FONT.deriveFont(Font.BOLD, 25f);
    public static final Font BOLD_FONT   = GAME_FONT.deriveFont(Font.BOLD, 18f);
    public static final Font NORMAL_FONT = GAME_FONT.deriveFont(Font.PLAIN, 18f);
    public static final Font BUTTON_FONT = GAME_FONT.deriveFont(Font.BOLD, 16f);
    public static final Font TEXT_FONT   = GAME_FONT.deriveFont(Font.PLAIN, 16f);
    public static final Font POPUP_FONT  = GAME_FONT.deriveFont(Font.BOLD, 15f);
    public static final Font SMALL_FONT  = GAME_FONT.deriveFont(Font.PLAIN, 12f);
    public static final Font TINY_FONT   = GAME_FONT.deriveFont(Font.PLAIN, 11f);

    private static final int LABEL_ICON_SIZE = 22;
    private static final HashMap<String, BufferedImage> imageCache = new HashMap<String, BufferedImage>();

    // every button plays a click sound
    public static final MouseAdapter CLICK_SOUND = new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                SoundManager.playSFX("click");
            }
    };
    private GameConstants() { }   // only static members

    private static Font loadGameFont() {
        try {
            return Font.createFont(Font.TRUETYPE_FONT, new File(FONT_FILE));
        } catch (Exception e) {
            System.out.println("Cannot load game font -> using SansSerif");
            return new Font("SansSerif", Font.PLAIN, 16);
        }
    }

    // ================= frame =================
    // Gives every frame the SAME size. If sameSpotAs is visible, the new frame opens at the same place.
    public static void setupFrame(JFrame frame, JPanel content, Component sameSpotAs) {
        content.setPreferredSize(new Dimension(WIN_W, WIN_H));
        frame.setContentPane(content);
        frame.setResizable(false);          // call before pack()
        frame.pack();
        if (sameSpotAs != null && sameSpotAs.isShowing()) frame.setLocation(sameSpotAs.getLocation());
        else frame.setLocationRelativeTo(null);
    }

    // ================= images =================
    // Each image is loaded once. Returns null if the file is missing -> caller uses a simple drawing.
    public static synchronized BufferedImage getImage(String fileName) {
        if (fileName == null) return null;
        if (imageCache.containsKey(fileName)) return imageCache.get(fileName);
        BufferedImage img = null;
        File f = new File(IMG_PATH + fileName);
        if (f.exists()) {
            try {
                img = ImageIO.read(f);
            } catch (Exception e) {
                System.out.println("Cannot load image " + fileName + " : " + e.getMessage());
            }
        }
        imageCache.put(fileName, img);
        return img;
    }

    public static ImageIcon getIcon(String fileName, int w, int h) {
        BufferedImage img = getImage(fileName);
        if (img == null) return null;
        return new ImageIcon(img.getScaledInstance(w, h, Image.SCALE_SMOOTH));
    }

    // draws an image with transparency (0 = invisible, 1 = solid)
    public static void drawFaded(Graphics2D g, BufferedImage img, int x, int y, int w, int h, float alpha) {
        Composite old = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0f, Math.min(1f, alpha))));
        g.drawImage(img, x, y, w, h, null);
        g.setComposite(old);
    }

    // green -> orange -> red (used by the HUD HP bar and mob HP bars)
    public static Color hpColor(double ratio) {
        if (ratio > 0.5) return GREEN;
        if (ratio > 0.25) return ORANGE;
        return RED;
    }

    // ================= buttons =================
    // PNG button with text on top. If the PNG is missing -> plain brown button.
    public static JButton makeButton(String imageFile, String text, int w, int h, Font font) {
        JButton b = new JButton(text);
        b.setFont(font);
        styleButton(b, imageFile, w, h);
        return b;
    }

    public static JButton makeButton(String imageFile, String text, int w, int h) {
        return makeButton(imageFile, text, w, h, BUTTON_FONT);
    }

    // picture-only button (X, pause, arrows...). fallbackText is shown only if the PNG is missing.
    public static JButton makeIconButton(String imageFile, String fallbackText, int size) {
        JButton b = makeButton(imageFile, fallbackText, size, size, SMALL_FONT);
        if (b.getIcon() != null) b.setText("");
        return b;
    }

    // shared look of every button (also used by the Deconstruct toggle)
    // optional pictures: <name>_hover.png and <name>_pressed.png
    public static void styleButton(AbstractButton b, String imageFile, int w, int h) {
        ImageIcon icon = getIcon(imageFile, w, h);
        if (icon != null) {
            b.setIcon(icon);
            /*ImageIcon hover = getIcon(imageFile.replace(".png", "_hover.png"), w, h);
            ImageIcon pressed = getIcon(imageFile.replace(".png", "_pressed.png"), w, h);
            if (hover != null) b.setRolloverIcon(hover);
            if (pressed != null) b.setPressedIcon(pressed);*/
            b.setHorizontalTextPosition(SwingConstants.CENTER);
            b.setVerticalTextPosition(SwingConstants.CENTER);
            b.setForeground(TEXT);
            b.setContentAreaFilled(false);
            b.setBorderPainted(false);
        } else {
            b.setBackground(BROWN);
            b.setForeground(BEIGE);
            b.setOpaque(true);
        }
        b.setMargin(new Insets(0, 0, 0, 0));
        b.setFocusPainted(false);
        b.setFocusable(false);              // keep keyboard focus on the game panel
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(w, h));
        b.addMouseListener(CLICK_SOUND);
    }

    // ================= labels =================
    public static JLabel makeLabel(String text, Font font, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(font);
        l.setForeground(color);
        return l;
    }

    // text label with a small icon in front (icon only if the PNG exists)
    public static JLabel makeIconLabel(String text, String iconFile, Font font, Color color) {
        JLabel l = makeLabel(text, font, color);
        l.setIcon(getIcon(iconFile, LABEL_ICON_SIZE, LABEL_ICON_SIZE));
        return l;
    }

    // picture label, or text label if the PNG is missing
    public static JLabel makeImageLabel(String fileName, String text, Font font, Color color, int w, int h) {
        ImageIcon icon = getIcon(fileName, w, h);
        if (icon != null) return new JLabel(icon);
        return makeLabel(text, font, color);
    }
}

// =====================================================================
//  ImagePanel : JPanel with a PNG background, or a plain colour if the PNG is missing
//               (colour may be see-through, e.g. SECTION_BG or OVERLAY)
// =====================================================================
class ImagePanel extends JPanel {
    private final String imageFile;
    private final Color fallbackColor;

    public ImagePanel(String imageFile, Color fallbackColor) {
        this.imageFile = imageFile;
        this.fallbackColor = fallbackColor;
        setOpaque(false);          // we paint the background ourselves
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        BufferedImage img = GameConstants.getImage(imageFile);
        if (img != null) {
            g.drawImage(img, 0, 0, getWidth(), getHeight(), null);
        } else if (fallbackColor != null) {
            g.setColor(fallbackColor);
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }
}