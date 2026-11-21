// 6813115 Renuka Choosingkae
// 6813238 Napat Chonsaktrakul
// 6813248 Pimpichcha Saengngoen
// 6813258 Sittichok Boonthittanont
package JavaProject3_TowerDefense;

import java.io.File;
import java.util.HashMap;
import javax.sound.sampled.*;

// =====================================================================
//  SoundManager : plays .wav files from resources/audio
//  (a missing file is simply silent -> the game still runs)
// =====================================================================
public class SoundManager {
    public static final String[] SONG_NAMES = {
        "Adventure", "Battle March", "Chill Lofi", "Retro Arcade", "Boss Theme"
    };
    private static final long SFX_GAP_MS = 90;          // same sound can't play again within 90 ms

    private static boolean audioOn = true, menuMusic = true;
    private static int bgmVolume = 50, sfxVolume = 70, currentSong = 0;
    private static Clip bgmClip = null;
    private static final HashMap<String, Long> lastPlayed = new HashMap<String, Long>();

    private SoundManager() { }   // only static methods

    // ================= BGM =================
    public static synchronized void playMenuBGM() {
        menuMusic = true;
        startBGM("bgm_menu.wav");
    }

    public static synchronized void playBGM(int index) {
        currentSong = index;
        menuMusic = false;
        startBGM("bgm" + (index + 1) + ".wav");
    }

    private static void startBGM(String fileName) {
        stopBGM();
        if (!audioOn) return;
        bgmClip = openClip(fileName, bgmVolume);
        if (bgmClip != null) bgmClip.loop(Clip.LOOP_CONTINUOUSLY);
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
        synchronized (lastPlayed) {
            Long last = lastPlayed.get(name);
            if (last != null && now - last < SFX_GAP_MS) return;
            lastPlayed.put(name, now);
        }
        final Clip clip = openClip(name + ".wav", sfxVolume);
        if (clip == null) return;
        clip.addLineListener(new LineListener() {
            public void update(LineEvent e) {
                if (e.getType() == LineEvent.Type.STOP) clip.close();
            }
        });
        clip.start();
    }

    // opens a .wav file, returns null if missing or not playable
    private static Clip openClip(String fileName, int volume) {
        File f = new File(GameConstants.AUDIO_PATH + fileName);
        if (!f.exists()) return null;
        try {
            Clip clip = AudioSystem.getClip();
            clip.open(AudioSystem.getAudioInputStream(f));
            setGain(clip, volume);
            return clip;
        } catch (Exception e) {
            System.out.println("Cannot play " + fileName + " : " + e.getMessage());
            return null;
        }
    }

    // ================= settings =================
    public static synchronized void setAudioOn(boolean on) {
        audioOn = on;
        if (!on) stopBGM();
        else if (menuMusic) playMenuBGM();
        else playBGM(currentSong);
    }

    public static synchronized void setBgmVolume(int v) {
        bgmVolume = v;
        setGain(bgmClip, v);
    }

    public static void setSfxVolume(int v) { sfxVolume = v; }
    public static boolean isAudioOn() { return audioOn; }
    public static int getBgmVolume() { return bgmVolume; }
    public static int getSfxVolume() { return sfxVolume; }
    public static int getCurrentSong() { return currentSong; }

    // volume 0-100 -> decibels
    private static void setGain(Clip clip, int volume) {
        if (clip == null) return;
        try {
            FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            float db = (volume <= 0) ? gain.getMinimum() : (float) (20.0 * Math.log10(volume / 100.0));
            gain.setValue(Math.max(gain.getMinimum(), Math.min(gain.getMaximum(), db)));
        } catch (Exception e) {
            System.out.println("Volume control not supported");
        }
    }
}