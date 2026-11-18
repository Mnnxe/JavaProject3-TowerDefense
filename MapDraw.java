// Group members: 6xxxxxxx Firstname Lastname 1, 6xxxxxxx Firstname Lastname 2,
//                6xxxxxxx Firstname Lastname 3, 6xxxxxxx Firstname Lastname 4,
//                6xxxxxxx Firstname Lastname 5
package JavaProject3_TowerDefense;
import java.awt.*;
import java.awt.image.BufferedImage;

// =====================================================================
//  MapDraw : draws a MapData with any cell size (game + preview)
//  PNG files (optional):  map_<theme>.png  (whole ground background)
//                         path_<theme>.png (one path tile)
//                         portal.png, base.png
// =====================================================================
public class MapDraw {
    private static final int COLS = GameConstants.COLS, ROWS = GameConstants.ROWS;
    private static final String[] THEME_KEYS = {"forest", "desert", "snow", "egco", "lab"};

    // ground1, ground2, path, path edge  (used when there is no PNG)
    private static final Color[][] THEME = {
        {new Color(96,168,78),  new Color(86,156,70),  new Color(176,136,84), new Color(132,98,58)},   // forest
        {new Color(234,208,146),new Color(224,197,134),new Color(196,154,94), new Color(152,112,64)},  // desert
        {new Color(236,243,250),new Color(222,233,246),new Color(166,180,200),new Color(122,138,164)}, // snow
        {new Color(204,204,210),new Color(190,190,198),new Color(64,94,166),  new Color(42,64,124)},   // EGCO room
        {new Color(72,80,94),   new Color(64,72,86),   new Color(44,172,162), new Color(24,122,116)}   // lab
    };

    private MapDraw() { }   // only static methods

    public static void draw(Graphics2D g, MapData map, int cs) {
        drawGround(g, map, cs);
        drawPath(g, map, cs);
        drawPortal(g, 0, map.getStartRow(), cs);
        drawBase(g, map.getBaseCol(), map.getBaseRow(), cs);
    }

    // light grid lines (shown while building)
    public static void drawGrid(Graphics2D g, int cs) {
        g.setColor(new Color(255, 255, 255, 35));
        for (int c = 0; c <= COLS; c++) g.drawLine(c * cs, 0, c * cs, ROWS * cs);
        for (int r = 0; r <= ROWS; r++) g.drawLine(0, r * cs, COLS * cs, r * cs);
    }

    // ================= background =================
    private static void drawGround(Graphics2D g, MapData map, int cs) {
        int t = map.getTheme();
        BufferedImage bg = GameConstants.getImage("map_" + THEME_KEYS[t] + ".png");
        if (bg != null) {
            g.drawImage(bg, 0, 0, COLS * cs, ROWS * cs, null);
            return;
        }
        Color[] c = THEME[t];
        for (int col = 0; col < COLS; col++) {
            for (int row = 0; row < ROWS; row++) {
                int x = col * cs, y = row * cs;
                g.setColor((col + row) % 2 == 0 ? c[0] : c[1]);
                g.fillRect(x, y, cs, cs);
                if (!map.isPath(col, row) && (col * 31 + row * 17 + t * 5) % 9 == 0) drawDecoration(g, t, x, y, cs);
            }
        }
    }

    // ================= path =================
    private static void drawPath(Graphics2D g, MapData map, int cs) {
        int t = map.getTheme();
        BufferedImage tile = GameConstants.getImage("path_" + THEME_KEYS[t] + ".png");
        Color[] c = THEME[t];
        for (int col = 0; col < COLS; col++) {
            for (int row = 0; row < ROWS; row++) {
                if (!map.isPath(col, row)) continue;
                int x = col * cs, y = row * cs;
                if (tile != null) {
                    g.drawImage(tile, x, y, cs, cs, null);
                } else {
                    g.setColor(c[2]);
                    g.fillRect(x, y, cs, cs);
                }
            }
        }
        if (tile != null) return;

        // path borders (only for the default drawing)
        Stroke old = g.getStroke();
        g.setStroke(new BasicStroke(Math.max(1f, cs / 12f)));
        g.setColor(c[3]);
        for (int col = 0; col < COLS; col++) {
            for (int row = 0; row < ROWS; row++) {
                if (!map.isPath(col, row)) continue;
                int x = col * cs, y = row * cs;
                if (!map.isPathOrOutside(col - 1, row)) g.drawLine(x, y, x, y + cs);
                if (!map.isPathOrOutside(col + 1, row)) g.drawLine(x + cs, y, x + cs, y + cs);
                if (!map.isPathOrOutside(col, row - 1)) g.drawLine(x, y, x + cs, y);
                if (!map.isPathOrOutside(col, row + 1)) g.drawLine(x, y + cs, x + cs, y + cs);
            }
        }
        g.setStroke(old);
    }

    private static void drawDecoration(Graphics2D g, int theme, int x, int y, int s) {
        switch (theme) {
            case 0: // bush
                g.setColor(new Color(50, 115, 45));
                g.fillOval(x + s / 4, y + s / 4, s / 2, s / 2);
                g.setColor(new Color(70, 140, 60));
                g.fillOval(x + s / 3, y + s / 4, s / 4, s / 4);
                break;
            case 1: // cactus
                g.setColor(new Color(70, 140, 70));
                g.fillRoundRect(x + s * 2 / 5, y + s / 5, s / 5, s * 3 / 5, 6, 6);
                g.fillRoundRect(x + s / 4, y + s * 2 / 5, s / 6, s / 5, 4, 4);
                g.fillRoundRect(x + s * 3 / 5, y + s / 3, s / 6, s / 5, 4, 4);
                break;
            case 2: // snowy pine
                g.setColor(new Color(60, 110, 90));
                g.fillPolygon(new int[]{x + s / 2, x + s / 4, x + s * 3 / 4}, new int[]{y + s / 6, y + s * 3 / 4, y + s * 3 / 4}, 3);
                g.setColor(Color.WHITE);
                g.fillPolygon(new int[]{x + s / 2, x + s * 2 / 5, x + s * 3 / 5}, new int[]{y + s / 6, y + s / 3, y + s / 3}, 3);
                break;
            case 3: // desk with paper
                g.setColor(new Color(150, 110, 70));
                g.fillRect(x + s / 5, y + s / 4, s * 3 / 5, s / 2);
                g.setColor(Color.WHITE);
                g.fillRect(x + s / 3, y + s / 3, s / 4, s / 5);
                break;
            default: // lab computer
                g.setColor(new Color(40, 40, 45));
                g.fillRect(x + s / 4, y + s / 4, s / 2, s / 3);
                g.setColor(new Color(80, 220, 255));
                g.fillRect(x + s / 4 + 2, y + s / 4 + 2, s / 2 - 4, s / 3 - 4);
                break;
        }
    }

    // ================= portal & base =================
    private static void drawPortal(Graphics2D g, int c, int r, int cs) {
        int x = c * cs, y = r * cs;
        BufferedImage img = GameConstants.getImage("portal.png");
        if (img != null) {
            g.drawImage(img, x, y, cs, cs, null);
            return;
        }
        g.setColor(new Color(90, 30, 140));
        g.fillOval(x + cs / 8, y + cs / 10, cs * 3 / 4, cs * 4 / 5);
        g.setColor(new Color(170, 90, 230));
        g.fillOval(x + cs / 4, y + cs / 4, cs / 2, cs / 2);
        g.setColor(new Color(240, 200, 255));
        g.fillOval(x + cs * 3 / 8, y + cs * 3 / 8, cs / 4, cs / 4);
    }

    private static void drawBase(Graphics2D g, int c, int r, int cs) {
        int x = c * cs, y = r * cs;
        BufferedImage img = GameConstants.getImage("base.png");
        if (img != null) {
            g.drawImage(img, x, y, cs, cs, null);
            return;
        }
        g.setColor(new Color(150, 150, 165));
        g.fillRect(x + cs / 8, y + cs / 3, cs * 3 / 4, cs * 3 / 5);
        int bw = cs / 6;
        for (int i = 0; i < 3; i++)
            g.fillRect(x + cs / 8 + i * (bw + cs / 12) + 1, y + cs / 5, bw, cs / 6);
        g.setColor(new Color(90, 60, 40));
        g.fillRoundRect(x + cs * 3 / 8, y + cs * 3 / 5, cs / 4, cs / 3, cs / 6, cs / 6);
        g.setColor(Color.DARK_GRAY);
        g.drawLine(x + cs / 2, y + cs / 5, x + cs / 2, y);
        g.setColor(Color.RED);
        g.fillPolygon(new int[]{x + cs / 2, x + cs / 2 + cs / 4, x + cs / 2}, new int[]{y, y + cs / 12, y + cs / 6}, 3);
        g.setColor(Color.BLACK);
        g.drawRect(x + cs / 8, y + cs / 3, cs * 3 / 4, cs * 3 / 5);
    }
}