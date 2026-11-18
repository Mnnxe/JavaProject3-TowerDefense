// Group members: 6xxxxxxx Firstname Lastname 1, 6xxxxxxx Firstname Lastname 2,
//                6xxxxxxx Firstname Lastname 3, 6xxxxxxx Firstname Lastname 4,
//                6xxxxxxx Firstname Lastname 5
package JavaProject3_TowerDefense;
import java.util.ArrayList;

// =====================================================================
//  MapData : name, special status, theme, waypoints, path cells (DATA only)
//  Drawing is in MapDraw.java
// =====================================================================
public class MapData {
    private static final int COLS = GameConstants.COLS, ROWS = GameConstants.ROWS;

    // theme index: 0 forest, 1 desert, 2 snow, 3 EGCO room, 4 lab
    // every path: 22 grids horizontal + 16 grids vertical = same length (38 grids)
    public static final MapData[] MAPS = {
        new MapData("Forest", false, 0, new int[][]{{0,2},{5,2},{5,8},{11,8},{11,2},{17,2},{17,6},{21,6}}),
        new MapData("Desert", false, 1, new int[][]{{0,8},{4,8},{4,1},{10,1},{10,7},{16,7},{16,4},{21,4}}),
        new MapData("Snow",   false, 2, new int[][]{{0,5},{3,5},{3,1},{8,1},{8,9},{14,9},{14,5},{21,5}}),
        new MapData("EGCO Common Room 6157", true, 3, new int[][]{{0,1},{7,1},{7,9},{13,9},{13,3},{19,3},{19,5},{21,5}}),
        new MapData("Lab 6273", true, 4, new int[][]{{0,9},{2,9},{2,3},{9,3},{9,8},{15,8},{15,3},{21,3}})
    };

    private final String name;
    private final boolean special;
    private final int theme;
    private final int[][] waypoints;
    private final boolean[][] pathCells = new boolean[COLS][ROWS];

    public MapData(String name, boolean special, int theme, int[][] waypoints) {
        this.name = name;
        this.special = special;
        this.theme = theme;
        this.waypoints = waypoints;
        // mark every cell between two waypoints as path
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
    public int getTheme() { return theme; }
    public int getWaypointCount() { return waypoints.length; }
    public int getStartRow() { return waypoints[0][1]; }
    public int getBaseCol() { return waypoints[waypoints.length - 1][0]; }
    public int getBaseRow() { return waypoints[waypoints.length - 1][1]; }

    public double[] getWaypointPixel(int i) {
        int cell = GameConstants.CELL;
        return new double[]{ (waypoints[i][0] + 0.5) * cell, (waypoints[i][1] + 0.5) * cell };
    }

    public boolean isPath(int c, int r) {
        if (c < 0 || c >= COLS || r < 0 || r >= ROWS) return false;
        return pathCells[c][r];
    }

    public boolean isPathOrOutside(int c, int r) {
        if (c < 0 || c >= COLS || r < 0 || r >= ROWS) return true;
        return pathCells[c][r];
    }
}