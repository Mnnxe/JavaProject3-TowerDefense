// 6813115 Renuka Choosingkae
// 6813238 Napat Chonsaktrakul
// 6813248 Pimpichcha Saengngoen
// 6813258 Sittichok Boonthittanont
package JavaProject3_TowerDefense;

import java.awt.*;
import java.awt.image.BufferedImage;

// =====================================================================
//  MapDraw : draws a MapData with any cell size (game + preview)
//  PNG files: bg_<theme>.png, path_<theme>.png, portal1-3.png
//  If a PNG is missing -> plain colours
// =====================================================================
public class MapDraw {
    private static final int COLS = GameConstants.COLS, ROWS = GameConstants.ROWS;
    private static final String[] THEME_KEYS = {"forest", "desert", "snow", "egco", "lab"};

    // fallback colours (same order as THEME_KEYS)
    private static final Color[] GROUND = {
        new Color(96, 168, 78), new Color(234, 208, 146), new Color(236, 243, 250),
        new Color(204, 204, 210), new Color(72, 80, 94)
    };
    private static final Color[] PATH = {
        new Color(176, 136, 84), new Color(196, 154, 94), new Color(166, 180, 200),
        new Color(64, 94, 166), new Color(44, 172, 162)
    };
    private static final Color PORTAL = new Color(140, 60, 200);
    private static final double PORTAL_SCALE = 1.6;
    private static final int PORTAL_FRAMES = 3, PORTAL_FRAME_MS = 150;

    private MapDraw() { }   // only static methods

    public static void draw(Graphics2D g, MapData map, int cs) {
        drawGround(g, map.getTheme(), cs);
        drawPath(g, map, cs);
        drawPortal(g, map.getStartRow(), cs);
    }

    // light grid lines (shown while building)
    public static void drawGrid(Graphics2D g, int cs) {
        g.setColor(GameConstants.GRID_LINE);
        for (int c = 0; c <= COLS; c++) g.drawLine(c * cs, 0, c * cs, ROWS * cs);
        for (int r = 0; r <= ROWS; r++) g.drawLine(0, r * cs, COLS * cs, r * cs);
    }

    private static void drawGround(Graphics2D g, int theme, int cs) {
        BufferedImage bg = GameConstants.getImage("bg_" + THEME_KEYS[theme] + ".png");
        if (bg != null) {
            g.drawImage(bg, 0, 0, COLS * cs, ROWS * cs, null);
        } else {
            g.setColor(GROUND[theme]);
            g.fillRect(0, 0, COLS * cs, ROWS * cs);
        }
    }

    private static void drawPath(Graphics2D g, MapData map, int cs) {
        int theme = map.getTheme();
        BufferedImage tile = GameConstants.getImage("path_" + THEME_KEYS[theme] + ".png");
        g.setColor(PATH[theme]);
        for (int col = 0; col < COLS; col++) {
            for (int row = 0; row < ROWS; row++) {
                if (!map.isPath(col, row)) continue;
                if (tile != null) g.drawImage(tile, col * cs, row * cs, cs, cs, null);
                else g.fillRect(col * cs, row * cs, cs, cs);
            }
        }
    }

    // animated portal (portal1.png -> portal3.png) at the start of the path
    private static void drawPortal(Graphics2D g, int row, int cs) {
        int size = (int) (cs * PORTAL_SCALE);
        int x = -(size - cs) / 2;
        int y = row * cs - (size - cs) / 2;

        int frame = (int) ((System.currentTimeMillis() / PORTAL_FRAME_MS) % PORTAL_FRAMES) + 1;
        BufferedImage img = GameConstants.getImage("portal" + frame + ".png");
        if (img != null) {
            g.drawImage(img, x, y, size, size, null);
        } else {
            g.setColor(PORTAL);
            g.fillOval(cs / 8, row * cs + cs / 8, cs * 3 / 4, cs * 3 / 4);
        }
    }
}