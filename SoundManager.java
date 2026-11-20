package JavaProject3_TowerDefense;
import java.io.File;
import java.util.HashMap;
import java.util.Random;
import javax.sound.sampled.*;

// =====================================================================
//  SoundManager : plays .wav files, or generates simple tones if missing
// =====================================================================
public class SoundManager {
    public static final String[] SONG_NAMES = {
        "Adventure", "Battle March", "Chill Lofi",
        "Retro Arcade", "Boss Theme"
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

    public static synchronized void playMenuBGM() {
        stopBGM();

        if (!audioOn) return;
    
        try {
            Clip clip = AudioSystem.getClip();
            
            File f = new File(
            GameConstants.AUDIO_PATH + "bgm_menu.wav"
        );

        if (f.exists()) {
            clip.open(
                AudioSystem.getAudioInputStream(f)
            );
            
            bgmClip = clip;
            
            setGain(bgmClip, bgmVolume);
            
            bgmClip.loop(
                Clip.LOOP_CONTINUOUSLY
            );
        }
        
        }catch (Exception e) {
            System.out.println("Menu BGM error: " + e.getMessage());
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

    // ================= tone generation (fallback when .wav is missing) =================
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