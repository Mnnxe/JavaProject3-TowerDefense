// 6813115 Renuka Choosingkae
// 6813238 Napat Chonsaktrakul
// 6813248 Pimpichcha Saengngoen
// 6813258 Sittichok Boonthittanont
package JavaProject3_TowerDefense;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;

// =====================================================================
//  Leaderboard : reads / writes leaderboard.txt
//  file format (one line per record) ->  map;name;waves;mobs
// =====================================================================
public class Leaderboard {
    private static final String SEP = ";";

    private Leaderboard() { }   // only static methods

    // ================= one record =================
    public static class Record implements Comparable<Record> {
        private final String map, name;
        private int waves, mobs;

        public Record(String map, String name, int waves, int mobs) {
            this.map = map;
            this.name = name;
            this.waves = waves;
            this.mobs = mobs;
        }

        public String getMap()  { return map; }
        public String getName() { return name; }
        public int getWaves()   { return waves; }
        public int getMobs()    { return mobs; }

        // keeps the better score, returns true if it changed
        public boolean improve(int newWaves, int newMobs) {
            if (newWaves > waves || (newWaves == waves && newMobs > mobs)) {
                waves = newWaves;
                mobs = newMobs;
                return true;
            }
            return false;
        }

        // more waves first, then more kills
        public int compareTo(Record other) {
            if (waves != other.waves) return other.waves - waves;
            return other.mobs - mobs;
        }

        public String toFileLine() { return map + SEP + name + SEP + waves + SEP + mobs; }
    }

    // ================= file =================
    private static synchronized ArrayList<Record> loadAll() {
        ArrayList<Record> list = new ArrayList<Record>();
        File f = new File(GameConstants.LEADERBOARD_FILE);
        if (!f.exists()) return list;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split(SEP);
                if (p.length != 4) continue;
                try {
                    list.add(new Record(p[0].trim(), p[1].trim(),
                            Integer.parseInt(p[2].trim()), Integer.parseInt(p[3].trim())));
                } catch (NumberFormatException e) {
                    System.out.println("Skip bad line: " + line);
                }
            }
        } catch (IOException e) {
            System.out.println("Cannot read leaderboard: " + e.getMessage());
        }
        return list;
    }

    private static void saveAll(ArrayList<Record> list) {
        File f = new File(GameConstants.LEADERBOARD_FILE);
        if (f.getParentFile() != null) f.getParentFile().mkdirs();
        try (PrintWriter pw = new PrintWriter(new FileWriter(f))) {
            for (Record r : list) pw.println(r.toFileLine());
        } catch (IOException e) {
            System.out.println("Cannot save leaderboard: " + e.getMessage());
        }
    }

    // ================= public =================
    // keeps only the BEST score of each player on each map
    public static synchronized void addRecord(String map, String name, int waves, int mobs) {
        name = name.replace(SEP, " ");
        ArrayList<Record> list = loadAll();
        for (Record r : list) {
            if (r.getMap().equals(map) && r.getName().equalsIgnoreCase(name)) {
                if (r.improve(waves, mobs)) saveAll(list);
                return;
            }
        }
        list.add(new Record(map, name, waves, mobs));
        saveAll(list);
    }

    public static ArrayList<Record> getRanking(String map) {
        ArrayList<Record> result = new ArrayList<Record>();
        for (Record r : loadAll())
            if (r.getMap().equals(map)) result.add(r);
        Collections.sort(result);
        return result;
    }

    public static boolean hasPlayedBasicMap() {
        for (Record r : loadAll())
            for (MapData m : MapData.MAPS)
                if (!m.isSpecial() && m.getName().equals(r.getMap())) return true;
        return false;
    }
}