package JavaProject3_TowerDefense;
import java.awt.*;
import java.util.ArrayList;

// =====================================================================
//  MapData : path, theme and drawing of each map
// =====================================================================
public class MapData {
    private static final int COLS = GameConstants.COLS, ROWS = GameConstants.ROWS;

    // ground1, ground2, path, path edge
    private static final Color[][] THEME = {
        {new Color(96,168,78),  new Color(86,156,70),  new Color(176,136,84), new Color(132,98,58)},   // forest
        {new Color(234,208,146),new Color(224,197,134),new Color(196,154,94), new Color(152,112,64)},  // desert
        {new Color(236,243,250),new Color(222,233,246),new Color(166,180,200),new Color(122,138,164)}, // snow
        {new Color(204,204,210),new Color(190,190,198),new Color(64,94,166),  new Color(42,64,124)},   // EGCO room
        {new Color(72,80,94),   new Color(64,72,86),   new Color(44,172,162), new Color(24,122,116)}   // lab
    };

    // every path: 22 grids horizontal + 16 grids vertical = same length (38 grids)
    public static final MapData[] MAPS = {
        new MapData("Forest", false, 0, new int[][]{{-1,2},{5,2},{5,8},{11,8},{11,2},{17,2},{17,6},{21,6}}),
        new MapData("Desert", false, 1, new int[][]{{-1,8},{4,8},{4,1},{10,1},{10,7},{16,7},{16,4},{21,4}}),
        new MapData("Snow",   false, 2, new int[][]{{-1,5},{3,5},{3,1},{8,1},{8,9},{14,9},{14,5},{21,5}}),
        new MapData("EGCO Common Room 6157", true, 3, new int[][]{{-1,1},{7,1},{7,9},{13,9},{13,3},{19,3},{19,5},{21,5}}),
        new MapData("Lab 6273", true, 4, new int[][]{{-1,9},{2,9},{2,3},{9,3},{9,8},{15,8},{15,3},{21,3}})
    };

    private String name;
    private boolean special;
    private int theme;
    private int[][] waypoints;
    private boolean[][] pathCells = new boolean[COLS][ROWS];

    public MapData(String name, boolean special, int theme, int[][] waypoints) {
        this.name = name;
        this.special = special;
        this.theme = theme;
        this.waypoints = waypoints;
        for (int i = 0; i < waypoints.length - 1; i++) {
            int x = waypoints[i][0], y = waypoints[i][1];
            int x2 = waypoints[i + 1][0], y2 = waypoints[i + 1][1];
            int dx = Integer.signum(x2 - x), dy = Integer.signum(y2 - y);
            mark(x, y);
            while (x != x2 || y != y2) { x += dx; y += dy; mark(x, y); }
        }
    }

    private void mark(int c, int r) {
        if (c >= 0 && c < COLS && r >= 0 && r < ROWS) pathCells[c][r] = true;
    }

    public String getName() { return name; }
    public boolean isSpecial() { return special; }
    public int getWaypointCount() { return waypoints.length; }

    public double[] getWaypointPixel(int i) {
        int cell = GameConstants.CELL;
        return new double[]{ (waypoints[i][0] + 0.5) * cell, (waypoints[i][1] + 0.5) * cell };
    }

    public boolean isPath(int c, int r) {
        if (c < 0 || c >= COLS || r < 0 || r >= ROWS) return false;
        return pathCells[c][r];
    }

    private boolean isPathOrOutside(int c, int r) {
        if (c < 0 || c >= COLS || r < 0 || r >= ROWS) return true;
        return pathCells[c][r];
    }

    // Draws whole map with any cell size (used by game and by preview)
    public void draw(Graphics2D g, int cs) {
        Color[] c = THEME[theme];
        for (int col = 0; col < COLS; col++) {
            for (int row = 0; row < ROWS; row++) {
                int x = col * cs, y = row * cs;
                if (pathCells[col][row]) {
                    g.setColor(c[2]);
                    g.fillRect(x, y, cs, cs);
                } else {
                    g.setColor((col + row) % 2 == 0 ? c[0] : c[1]);
                    g.fillRect(x, y, cs, cs);
                    if ((col * 31 + row * 17 + theme * 5) % 9 == 0) drawDecoration(g, x, y, cs);
                }
            }
        }
        // path borders
        Stroke old = g.getStroke();
        g.setStroke(new BasicStroke(Math.max(1f, cs / 12f)));
        g.setColor(c[3]);
        for (int col = 0; col < COLS; col++) {
            for (int row = 0; row < ROWS; row++) {
                if (!pathCells[col][row]) continue;
                int x = col * cs, y = row * cs;
                if (!isPathOrOutside(col - 1, row)) g.drawLine(x, y, x, y + cs);
                if (!isPathOrOutside(col + 1, row)) g.drawLine(x + cs, y, x + cs, y + cs);
                if (!isPathOrOutside(col, row - 1)) g.drawLine(x, y, x + cs, y);
                if (!isPathOrOutside(col, row + 1)) g.drawLine(x, y + cs, x + cs, y + cs);
            }
        }
        g.setStroke(old);
        drawPortal(g, 0, waypoints[0][1], cs);
        drawBase(g, waypoints[waypoints.length - 1][0], waypoints[waypoints.length - 1][1], cs);
    }

    private void drawDecoration(Graphics2D g, int x, int y, int s) {
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

    private void drawPortal(Graphics2D g, int c, int r, int cs) {
        int x = c * cs, y = r * cs;
        g.setColor(new Color(90, 30, 140));
        g.fillOval(x + cs / 8, y + cs / 10, cs * 3 / 4, cs * 4 / 5);
        g.setColor(new Color(170, 90, 230));
        g.fillOval(x + cs / 4, y + cs / 4, cs / 2, cs / 2);
        g.setColor(new Color(240, 200, 255));
        g.fillOval(x + cs * 3 / 8, y + cs * 3 / 8, cs / 4, cs / 4);
    }

    private void drawBase(Graphics2D g, int c, int r, int cs) {
        int x = c * cs, y = r * cs;
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

// =====================================================================
//  WaveData : mob order of each wave
// =====================================================================
class WaveData {
    private static final MobType G = MobType.GOBLIN, W = MobType.WOLF, B = MobType.BAT,
            GO = MobType.GOLEM, MB = MobType.MINI_BOSS, BS = MobType.BOSS;

    private static final MobType[][] TYPES = {
        {G}, {G, W}, {G, B}, {W, G, B, GO}, {G, GO, MB, B, G, W},
        {B, GO, W, G}, {GO, W, B, G}, {B, GO, W, G}, {GO, G, B, W}, {B, MB, GO, W, BS, B, W, B, GO, G}
    };
    private static final int[][] COUNTS = {
        {6}, {6, 4}, {5, 12}, {5, 10, 7, 2}, {5, 4, 1, 11, 5, 8},
        {15, 7, 7, 5}, {4, 12, 15, 4}, {15, 8, 8, 10}, {10, 8, 15, 10}, {15, 1, 7, 10, 1, 10, 5, 10}
    };

    // wave is 1-based
    public static ArrayList<MobType> buildQueue(int wave) {
        ArrayList<MobType> q = new ArrayList<MobType>();
        if (wave < 1 || wave > TYPES.length) return q;
        for (int i = 0; i < TYPES[wave - 1].length; i++)
            for (int k = 0; k < COUNTS[wave - 1][i]; k++)
                q.add(TYPES[wave - 1][i]);
        return q;
    }

    public static int totalMobs(int wave) {
        if (wave < 1 || wave > COUNTS.length) return 0;
        int sum = 0;
        for (int c : COUNTS[wave - 1]) sum += c;
        return sum;
    }
}