package JavaProject3_TowerDefense;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import javax.imageio.ImageIO;
import javax.swing.*;

// =====================================================================
//  GameConstants : shared values + helper methods (frame, images, buttons)
// =====================================================================
public class GameConstants {
    //public static final String PATH = "src/main/java/JavaProject3_TowerDefense/resources/";
    public static final String PATH = "resources/";
    public static final String IMG_PATH = PATH + "images/";
    public static final String AUDIO_PATH = PATH + "audio/";
    public static final String LEADERBOARD_FILE = PATH + "leaderboard.txt";

    // ---------- window : EVERY frame uses this content size ----------
    public static final int WIN_W = 1100, WIN_H = 650;

    // ---------- map grid ----------
    public static final int COLS = 22, ROWS = 11, CELL = 46;
    public static final int MAP_W = COLS * CELL, MAP_H = ROWS * CELL;

    // ---------- game rules ---------- //-->change to insert picture
    public static final int MAX_HP = 100;
    public static final int START_COINS = 100;
    public static final int MAX_COINS = 999999;
    public static final int MAX_WAVE = 10;
    public static final double REFUND_RATE = 0.70;
    public static final int WAVE_BONUS = 30, WAVE_BONUS_PER_WAVE = 5;
    public static final int NAME_MAX_LENGTH = 15;

    // ---------- group members (shown in Credits) ---------- //change to insert picture
    //move to BaseDialog
    
    // ---------- colours ----------
    public static final Color DARK_BG   = new Color(28, 32, 48);
    public static final Color PANEL_BG  = new Color(42, 48, 72);
    public static final Color HEADER_BG = new Color(60, 68, 100);
    public static final Color GOLD   = new Color(255, 200, 60);
    public static final Color TEXT   = new Color(0, 0, 0);
    public static final Color RED    = new Color(215, 70, 70);
    public static final Color GREEN  = new Color(70, 180, 100);
    public static final Color BLUE   = new Color(70, 125, 215);
    public static final Color PURPLE = new Color(140, 90, 200);
    public static final Color GRAY   = new Color(95, 95, 120);
    public static final Color ORANGE = new Color(200, 140, 30);
    public static final Color BROWN = new Color(120, 80, 45);

    // ---------- fonts ----------
    //use only 1 font pattern, use this instead of get font
    private static Font loadGameFont() {
        try {
            return Font.createFont(Font.TRUETYPE_FONT, new File(PATH + "LcdSolid-VPzB.ttf"));
            
        } catch (Exception e) {
            System.out.println("Cannot load game font");
            return new Font("SansSerif",Font.PLAIN,16);
        }
    }

    public static final Font GAME_FONT = loadGameFont();

    public static final Font TITLE_FONT = GAME_FONT.deriveFont(Font.BOLD, 52f);
    public static final Font HEADER_FONT = GAME_FONT.deriveFont(Font.BOLD, 25f);
    public static final Font NORMAL_FONT = GAME_FONT.deriveFont(Font.PLAIN, 18f);
    public static final Font BOLD_FONT = GAME_FONT.deriveFont(Font.BOLD, 18f);

    // ================= frame helper =================
    // Gives every frame the SAME size. If sameSpotAs is visible, the new frame opens at the same place.
    public static void setupFrame(JFrame frame, JPanel content, Component sameSpotAs) {
        content.setPreferredSize(new Dimension(WIN_W, WIN_H));
        frame.setContentPane(content);
        frame.setResizable(false);          // call before pack()
        frame.pack();
        if (sameSpotAs != null && sameSpotAs.isShowing()) frame.setLocation(sameSpotAs.getLocation());
        else frame.setLocationRelativeTo(null);
    }
/*
public static Font getFont(String fileName, float size) {
    try {
        File file = new File(PATH + fileName);
        
        if (!file.exists()) {
            System.out.println("Font not found: " + fileName);
            return new Font("SansSerif", Font.BOLD, (int) size);
        }
        
        Font font = Font.createFont(Font.TRUETYPE_FONT,file);
        return font.deriveFont(size);
        
    } catch (Exception e) {
        e.printStackTrace();
        return new Font("SansSerif",Font.BOLD,(int) size);
    }
}
*/

    // ================= images =================
    // Each image is loaded once. Returns null if the file is missing -> caller uses default drawing.
    private static final HashMap<String, BufferedImage> imageCache = new HashMap<String, BufferedImage>();

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

    public static boolean hasImage(String fileName) { return getImage(fileName) != null; }

    public static ImageIcon getIcon(String fileName, int w, int h) {
        BufferedImage img = getImage(fileName);
        if (img == null) return null;
        return new ImageIcon(img.getScaledInstance(w, h, Image.SCALE_SMOOTH));
    }

    public static ImageIcon getIcon(String fileName) {
        BufferedImage img = getImage(fileName);
        if (img == null) {
            return null;
        }
        return new ImageIcon(img);
    }


    // ================= component helpers =================
    public static JButton makeButton(String text, Color bg, int fontSize) {
        JButton b = new JButton(text);
        b.setFont(GAME_FONT.deriveFont(Font.BOLD, (float) fontSize));
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setOpaque(true);
        b.setFocusPainted(false);
        b.setFocusable(false);   // keep keyboard focus on the game panel
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(bg.darker(), 2),
                BorderFactory.createEmptyBorder(6, 14, 6, 14)));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    public static JButton makeButton(String text, Color bg) { return makeButton(text, bg, 16); }

    // PNG button. If the PNG is missing -> normal coloured button with text.
    // Optional hover picture: same name + "_hover" (e.g. btn_play_hover.png)
    public static JButton makeImageButton(String fileName, String text, Color bg, int w, int h, int fontSize) {
        ImageIcon icon = getIcon(fileName, w, h);
        JButton b;
        if (icon == null) {
            b = makeButton(text, bg, fontSize);
        } else {
            b = new JButton(text, icon);

    
        b.setFont(GAME_FONT.deriveFont(Font.BOLD, (float) fontSize));
        // ให้ text อยู่ทับตรงกลาง PNG
        b.setHorizontalTextPosition(SwingConstants.CENTER);
        b.setVerticalTextPosition(SwingConstants.CENTER);
        // สีข้อความ
        b.setForeground(Color.BLACK);

        // =========================
        // Hover PNG
        // =========================
        ImageIcon hover = getIcon(fileName.replace(".png", "_hover.png"),w, h);
        if (hover != null) {
            b.setRolloverIcon(hover);
        }
        // =========================
        // Pressed PNG
        // =========================
        ImageIcon pressed = getIcon(fileName.replace(".png", "_pressed.png"),w, h);

        if (pressed != null) {b.setPressedIcon(pressed);}

        // เอาหน้าตา JButton ปกติออก
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setFocusPainted(false);
        b.setFocusable(false);

        b.setMargin(new Insets(0, 0, 0, 0));

        b.setCursor(
            new Cursor(Cursor.HAND_CURSOR)
        );

        b.setToolTipText(text);
        }
        b.setPreferredSize(new Dimension(w, h));
        return b;
    }

    public static JLabel makeLabel(String text, Font font, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(font);
        l.setForeground(color);
        return l;
    }

    // text label with a small icon in front (icon only if the PNG exists)
    public static JLabel makeIconLabel(String text, String iconFile, Font font, Color color) {
        JLabel l = makeLabel(text, font, color);
        ImageIcon icon = getIcon(iconFile, 22, 22);
        if (icon != null) l.setIcon(icon);
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
//  ImagePanel : JPanel with a PNG background (or gradient / transparent)
// =====================================================================
class ImagePanel extends JPanel {
    private String imageFile;
    private Color top, bottom;

    // top == null and no image -> transparent panel
    public ImagePanel(String imageFile, Color top, Color bottom) {
        this.imageFile = imageFile;
        this.top = top;
        this.bottom = bottom;

        if (top != null) setBackground(top);
        if (top != null && top.getAlpha() < 255) {
            setOpaque(false);
        } else {
            setOpaque(top != null || GameConstants.hasImage(imageFile));
        }
    }

    public ImagePanel(String imageFile, Color solid) { this(imageFile, solid, solid); }
    public ImagePanel(String imageFile) { this(imageFile, null, null); }
    public ImagePanel(Color solid) { this (null, solid, solid); }

    @Override 
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        BufferedImage img = GameConstants.getImage(imageFile);
        if (img != null) {
            g2.drawImage(img, 0, 0, getWidth(), getHeight(), null);
        } else if (top != null) {
            g2.setPaint(new GradientPaint(0, 0, top, 0, getHeight(), bottom));
            g2.fillRect(0, 0, getWidth(), getHeight());
            drawFallback(g2);
        }
    }

    // child classes can draw extra things when there is no PNG
    protected void drawFallback(Graphics2D g) { }
}