// Group members: 6xxxxxxx Firstname Lastname 1, 6xxxxxxx Firstname Lastname 2,
//                6xxxxxxx Firstname Lastname 3, 6xxxxxxx Firstname Lastname 4,
//                6xxxxxxx Firstname Lastname 5
package JavaProject3_TowerDefense;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

// =====================================================================
//  Leaderboard : file format (one line per record) ->  map;name;waves;mobs
// =====================================================================
public class Leaderboard {

    public static class Record {
        public String map, name;
        public int waves, mobs;
        public Record(String map, String name, int waves, int mobs) {
            this.map = map; this.name = name; this.waves = waves; this.mobs = mobs;
        }
    }

    public static synchronized ArrayList<Record> loadAll() {
        ArrayList<Record> list = new ArrayList<Record>();
        File f = new File(GameConstants.LEADERBOARD_FILE);
        if (!f.exists()) return list;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] p = line.split(";");
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
            for (Record r : list) pw.println(r.map + ";" + r.name + ";" + r.waves + ";" + r.mobs);
        } catch (IOException e) {
            System.out.println("Cannot save leaderboard: " + e.getMessage());
        }
    }

    // keep only the BEST score of each player on each map
    public static synchronized void addRecord(String map, String name, int waves, int mobs) {
        name = name.replace(";", " ");
        ArrayList<Record> list = loadAll();
        for (Record r : list) {
            if (r.map.equals(map) && r.name.equalsIgnoreCase(name)) {
                if (waves > r.waves || (waves == r.waves && mobs > r.mobs)) {
                    r.waves = waves; r.mobs = mobs;
                    saveAll(list);
                }
                return;
            }
        }
        list.add(new Record(map, name, waves, mobs));
        saveAll(list);
    }

    public static ArrayList<Record> getRanking(String map) {
        ArrayList<Record> result = new ArrayList<Record>();
        for (Record r : loadAll()) if (r.map.equals(map)) result.add(r);
        Collections.sort(result, new Comparator<Record>() {
            public int compare(Record a, Record b) {
                if (a.waves != b.waves) return b.waves - a.waves;
                return b.mobs - a.mobs;
            }
        });
        return result;
    }

    public static boolean hasPlayedBasicMap() {
        for (Record r : loadAll())
            for (MapData m : MapData.MAPS)
                if (!m.isSpecial() && m.getName().equals(r.map)) return true;
        return false;
    }
}