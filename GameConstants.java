package JavaProject3_TowerDefense;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Random;
import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import javax.swing.*;

// =====================================================================
//  GameConstants : shared values, helper methods, image loading
// =====================================================================
public class GameConstants {
    // ---------- relative paths (from Project3_XXX folder) ----------
    //public static final String PATH = "src/main/java/JavaProject3_TowerDefense/resources/";
    public static final String PATH = "resources/";

    public static final String IMG_PATH = PATH + "images/";
    public static final String AUDIO_PATH = PATH + "audio/";
    public static final String LEADERBOARD_FILE = PATH + "leaderboard.txt";

    // ---------- map grid ----------
    public static final int COLS = 22, ROWS = 11, CELL = 46;
    public static final int MAP_W = COLS * CELL, MAP_H = ROWS * CELL;

    // ---------- game rules ---------- //-->change to insert picture
    public static final int MAX_HP = 100;
    public static final int START_COINS = 150;
    public static final int MAX_COINS = 999999;
    public static final int MAX_WAVE = 10;
    public static final double REFUND_RATE = 0.70;

    // ---------- group members (shown in Credits) ---------- //change to insert picture
    public static final String[][] MEMBERS = {
        {"6xxxxxxx", "Firstname Lastname 1"},
        {"6xxxxxxx", "Firstname Lastname 2"},
        {"6xxxxxxx", "Firstname Lastname 3"},
        {"6xxxxxxx", "Firstname Lastname 4"},
        {"6xxxxxxx", "Firstname Lastname 5"}
    };

    // ---------- colours ----------
    public static final Color DARK_BG  = new Color(28, 32, 48);
    public static final Color PANEL_BG = new Color(42, 48, 72);
    public static final Color HEADER_BG = new Color(60, 68, 100);
    public static final Color GOLD  = new Color(255, 200, 60);
    public static final Color TEXT  = new Color(235, 235, 245);
    public static final Color RED   = new Color(215, 70, 70);
    public static final Color GREEN = new Color(70, 180, 100);
    public static final Color BLUE  = new Color(70, 125, 215);
    public static final Color PURPLE = new Color(140, 90, 200);

    // ---------- fonts ----------
    public static final Font TITLE_FONT  = new Font("SansSerif", Font.BOLD, 52);
    public static final Font HEADER_FONT = new Font("SansSerif", Font.BOLD, 22);
    public static final Font NORMAL_FONT = new Font("SansSerif", Font.PLAIN, 15);
    public static final Font BOLD_FONT   = new Font("SansSerif", Font.BOLD, 15);

    public static JButton makeButton(String text, Color bg, int fontSize) {
        JButton b = new JButton(text);
        b.setFont(new Font("SansSerif", Font.BOLD, fontSize));
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

    public static JButton makeButton(String text, Color bg) {
        return makeButton(text, bg, 16);
    }

    public static JLabel makeLabel(String text, Font font, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(font);
        l.setForeground(color);
        return l;
    }

    // ---------- image loading (was ImageStore) ----------
    // Loads each image once. Returns null if file does not exist -> caller draws a shape instead.
    private static final HashMap<String, BufferedImage> imageCache = new HashMap<String, BufferedImage>();

    public static synchronized BufferedImage getImage(String fileName) {
        if (imageCache.containsKey(fileName)) return imageCache.get(fileName);
        BufferedImage img = null;
        try {
            File f = new File(IMG_PATH + fileName);
            System.out.println("find: " + f.getAbsolutePath() + " -> found?: " + f.exists());
            if (f.exists()) img = ImageIO.read(f);
        } catch (Exception e) {
            System.out.println("Cannot load image " + fileName + " : " + e.getMessage());
        }
        imageCache.put(fileName, img);
        return img;
    }
}

// =====================================================================
//  SoundManager : plays .wav files, or generates simple tones if missing
// =====================================================================
class SoundManager {
    public static final String[] SONG_NAMES = {
        "Song 1 - Adventure", "Song 2 - Battle March", "Song 3 - Chill Lofi",
        "Song 4 - Retro Arcade", "Song 5 - Boss Theme"
    };

    private static final float RATE = 22050f;
    private static final AudioFormat FORMAT = new AudioFormat(RATE, 8, 1, true, false);
    private static final int SINE = 0, SQUARE = 1, NOISE = 2, REST = -99;
    private static final Random rand = new Random();

    private static boolean audioOn = true;
    private static int bgmVolume = 50, sfxVolume = 70, currentSong = 0;
    private static Clip bgmClip = null;
    private static final HashMap<String, Long> lastPlayed = new HashMap<String, Long>();
    private static final HashMap<String, byte[]> sfxCache = new HashMap<String, byte[]>();

    // semitones from A4 (440 Hz)
    private static final int[][] MELODIES = {
        {3,7,10,15, 10,7,3,7, 5,8,12,8, 5,8,12,17, 3,7,10,15, 12,10,8,7, 5,7,8,5, 3,REST,3,REST},
        {0,0,7,0, 5,3,0,-2, 0,0,7,0, 8,7,5,3, -4,-4,3,-4, -2,-2,5,-2, 0,3,7,12, 7,3,0,REST},
        {-9,-5,-2,3, -2,-5,-9,REST, -7,-4,0,5, 0,-4,-7,REST, -12,-9,-5,0, -5,-9,-12,REST, -10,-7,-3,2, -3,-7,-10,REST},
        {12,15,19,15, 12,15,19,24, 10,14,17,14, 10,14,17,22, 8,12,15,12, 8,12,15,20, 7,10,14,19, 14,10,7,REST},
        {-24,-24,-21,-24, -17,-18,-24,REST, -24,-24,-21,-24, -14,-15,-17,REST, -22,-22,-19,-22, -15,-16,-22,REST, -24,-12,-24,-12, -24,REST,-24,REST}
    };
    private static final double[] TEMPO = {0.20, 0.14, 0.30, 0.13, 0.17};
    private static final int[] SONG_WAVE = {SINE, SQUARE, SINE, SQUARE, SQUARE};

    // ================= BGM =================
    public static synchronized void playBGM(int index) {
        stopBGM();
        currentSong = index;
        if (!audioOn) return;
        try {
            Clip clip = AudioSystem.getClip();
            File f = new File(GameConstants.AUDIO_PATH + "bgm" + (index + 1) + ".wav");
            if (f.exists()) {
                clip.open(AudioSystem.getAudioInputStream(f));
            } else {
                byte[] data = makeMelody(index);
                clip.open(FORMAT, data, 0, data.length);
            }
            bgmClip = clip;
            setGain(bgmClip, bgmVolume);
            bgmClip.loop(Clip.LOOP_CONTINUOUSLY);
        } catch (Exception e) {
            System.out.println("BGM error: " + e.getMessage());
            bgmClip = null;
        }
    }

    public static synchronized void stopBGM() {
        if (bgmClip != null) {
            bgmClip.stop();
            bgmClip.close();
            bgmClip = null;
        }
    }

    // ================= SFX =================
    public static void playSFX(String name) {
        if (!audioOn || sfxVolume <= 0) return;
        long now = System.currentTimeMillis();
        synchronized (lastPlayed) {               // avoid playing the same sound too many times
            Long last = lastPlayed.get(name);
            if (last != null && now - last < 90) return;
            lastPlayed.put(name, now);
        }
        try {
            final Clip clip = AudioSystem.getClip();
            File f = new File(GameConstants.AUDIO_PATH + name + ".wav");
            if (f.exists()) {
                clip.open(AudioSystem.getAudioInputStream(f));
            } else {
                byte[] data;
                synchronized (sfxCache) {
                    data = sfxCache.get(name);
                    if (data == null) { data = makeEffect(name); sfxCache.put(name, data); }
                }
                clip.open(FORMAT, data, 0, data.length);
            }
            setGain(clip, sfxVolume);
            clip.addLineListener(new LineListener() {
                public void update(LineEvent e) {
                    if (e.getType() == LineEvent.Type.STOP) clip.close();
                }
            });
            clip.start();
        } catch (Exception e) {
            // no audio device -> ignore
        }
    }

    // ================= settings =================
    public static synchronized void setAudioOn(boolean on) {
        audioOn = on;
        if (on) playBGM(currentSong); else stopBGM();
    }
    public static synchronized void setBgmVolume(int v) { bgmVolume = v; setGain(bgmClip, v); }
    public static void setSfxVolume(int v) { sfxVolume = v; }
    public static boolean isAudioOn() { return audioOn; }
    public static int getBgmVolume() { return bgmVolume; }
    public static int getSfxVolume() { return sfxVolume; }
    public static int getCurrentSong() { return currentSong; }

    private static void setGain(Clip clip, int volume) {
        if (clip == null) return;
        try {
            FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            float db = (volume <= 0) ? gain.getMinimum() : (float) (20.0 * Math.log10(volume / 100.0));
            db = Math.max(gain.getMinimum(), Math.min(gain.getMaximum(), db));
            gain.setValue(db);
        } catch (Exception e) { /* control not supported */ }
    }

    // ================= tone generation (fallback) =================
    private static byte[] makeMelody(int index) {
        int[] notes = MELODIES[index];
        double[] freqs = new double[notes.length];
        for (int i = 0; i < notes.length; i++)
            freqs[i] = (notes[i] == REST) ? 0 : 440.0 * Math.pow(2, notes[i] / 12.0);
        double amp = SONG_WAVE[index] == SQUARE ? 0.12 : 0.35;
        return makeSequence(freqs, TEMPO[index], SONG_WAVE[index], amp, false);
    }

    private static byte[] makeEffect(String name) {
        switch (name) {
            case "archer":  return makeSequence(new double[]{1200, 900}, 0.03, SINE, 0.4, true);
            case "cannon":  return makeSequence(new double[]{90}, 0.18, NOISE, 0.5, true);
            case "ice":     return makeSequence(new double[]{1500, 1800}, 0.04, SINE, 0.35, true);
            case "fire":    return makeSequence(new double[]{300}, 0.12, NOISE, 0.35, true);
            case "place":   return makeSequence(new double[]{600, 900}, 0.06, SQUARE, 0.2, true);
            case "upgrade": return makeSequence(new double[]{523, 659, 784}, 0.07, SQUARE, 0.2, true);
            case "sell":    return makeSequence(new double[]{900, 600}, 0.07, SQUARE, 0.2, true);
            case "error":   return makeSequence(new double[]{180, 140}, 0.09, SQUARE, 0.25, false);
            case "death":   return makeSequence(new double[]{400, 250}, 0.04, SINE, 0.3, true);
            case "hit":     return makeSequence(new double[]{120}, 0.15, SQUARE, 0.3, true);
            case "wave":    return makeSequence(new double[]{440, 554, 659, 880}, 0.09, SQUARE, 0.2, false);
            case "clear":   return makeSequence(new double[]{659, 784, 1047}, 0.10, SINE, 0.35, false);
            case "boss":    return makeSequence(new double[]{110, 104, 98}, 0.25, SQUARE, 0.25, false);
            case "victory": return makeSequence(new double[]{523, 659, 784, 1047, 784, 1047}, 0.15, SINE, 0.4, false);
            case "defeat":  return makeSequence(new double[]{392, 370, 349, 330}, 0.25, SINE, 0.4, false);
            default:        return makeSequence(new double[]{1000}, 0.03, SINE, 0.3, true); // "click"
        }
    }

    private static byte[] makeSequence(double[] freqs, double noteSec, int wave, double amp, boolean decay) {
        int perNote = (int) (RATE * noteSec);
        int fade = Math.max(1, perNote / 10);
        byte[] buf = new byte[perNote * freqs.length];
        for (int n = 0; n < freqs.length; n++) {
            double f = freqs[n];
            for (int i = 0; i < perNote; i++) {
                double v = 0;
                if (f > 0) {
                    double s = Math.sin(2 * Math.PI * f * i / RATE);
                    if (wave == SQUARE) v = s >= 0 ? 1 : -1;
                    else if (wave == NOISE) v = rand.nextDouble() * 2 - 1;
                    else v = s;
                    double env = 1.0;
                    if (i < fade) env = i / (double) fade;
                    else if (i > perNote - fade) env = (perNote - i) / (double) fade;
                    if (decay) env *= 1.0 - (double) i / perNote;
                    v *= env * amp;
                }
                buf[n * perNote + i] = (byte) (v * 127);
            }
        }
        return buf;
    }
}

// =====================================================================
//  Leaderboard : file format (one line per record) ->  map;name;waves;mobs
// =====================================================================
class Leaderboard {

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