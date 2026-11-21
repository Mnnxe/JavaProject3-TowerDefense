// 6813115 Renuka Choosingkae
// 6813238 Napat Chonsaktrakul
// 6813248 Pimpichcha Saengngoen
// 6813258 Sittichok Boonthittanont
package JavaProject3_TowerDefense;

import java.util.Random;

// =====================================================================
//  MapData : name, special status, theme, waypoints, path cells (DATA only)
//  Drawing is in MapDraw.java
// =====================================================================
public class MapData {
    private static final int COLS = GameConstants.COLS, ROWS = GameConstants.ROWS;
    private static final Random RANDOM = new Random();

    // themes (index of the picture / colour in MapDraw)
    public static final int FOREST = 0, DESERT = 1, SNOW = 2, LAB = 4;

    private static final MapData FOREST_MAP = new MapData("Forest", false, FOREST, new int[][]{{0,2},{5,2},{5,8},{11,8},{11,2},{17,2},{17,6},{21,6}});
    private static final MapData DESERT_MAP = new MapData("Desert", false, DESERT, new int[][]{{0,8},{4,8},{4,1},{10,1},{10,7},{16,7},{16,4},{21,4}});
    private static final MapData SNOW_MAP   = new MapData("Snow",   false, SNOW,   new int[][]{{0,5},{3,5},{3,1},{8,1},{8,9},{14,9},{14,5},{21,5}});
    private static final MapData LAB_MAP    = new MapData("Lab 6273", true, LAB,   new int[][]{{0,9},{2,9},{2,3},{9,3},{9,8},{15,8},{15,3},{21,3}});
    private static final MapData RANDOM_MAP = new MapData("Random");

    // 5 items for the map combo box
    public static final MapData[] MAPS = {
        FOREST_MAP, DESERT_MAP, SNOW_MAP, LAB_MAP, RANDOM_MAP
    };

    private final String name;
    private final boolean special, random;
    private final int theme;
    private final int[][] waypoints;
    private final boolean[][] pathCells = new boolean[COLS][ROWS];

    public MapData(String name, boolean special, int theme, int[][] waypoints) {
        this.name = name;
        this.special = special;
        this.random = false;
        this.theme = theme;
        this.waypoints = waypoints;
        markPath();
    }

    // Random is only a menu choice, not a real map
    private MapData(String name) {
        this.name = name;
        this.special = false;
        this.random = true;
        this.theme = -1;
        this.waypoints = new int[0][0];
    }

    // mark every cell between two waypoints as path
    private void markPath() {
        for (int i = 0; i < waypoints.length - 1; i++) {
            int x = waypoints[i][0], y = waypoints[i][1];
            int x2 = waypoints[i + 1][0], y2 = waypoints[i + 1][1];
            int dx = Integer.signum(x2 - x), dy = Integer.signum(y2 - y);
            mark(x, y);
            while (x != x2 || y != y2) {
                x += dx;
                y += dy;
                mark(x, y);
            }
        }
    }

    private void mark(int c, int r) {
        if (c >= 0 && c < COLS && r >= 0 && r < ROWS) pathCells[c][r] = true;
    }

    public String getName() { return name; }
    public boolean isSpecial() { return special; }
    public boolean isRandom() { return random; }
    public int getTheme() { return theme; }
    public int getWaypointCount() { return waypoints.length; }
    public int getStartRow() { return waypoints[0][1]; }

    // Random chooses a basic map, and Lab is included after special maps are unlocked
    public static MapData getRandomMap(boolean specialUnlocked) {
        int count = specialUnlocked ? 4 : 3;
        return MAPS[RANDOM.nextInt(count)];
    }

    // centre of waypoint i in pixels
    public double[] getWaypointPixel(int i) {
        int cell = GameConstants.CELL;
        return new double[]{(waypoints[i][0] + 0.5) * cell, (waypoints[i][1] + 0.5) * cell};
    }

    public boolean isPath(int c, int r) {
        if (c < 0 || c >= COLS || r < 0 || r >= ROWS) return false;
        return pathCells[c][r];
    }
}
