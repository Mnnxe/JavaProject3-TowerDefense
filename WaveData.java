// 6813115 Renuka Choosingkae
// 6813238 Napat Chonsaktrakul
// 6813248 Pimpichcha Saengngoen
// 6813258 Sittichok Boonthittanont
package JavaProject3_TowerDefense;

import java.util.ArrayList;

// =====================================================================
//  WaveData : mob order of each wave
// =====================================================================
public class WaveData {
    private static final MobType G  = MobType.GOBLIN;
    private static final MobType W  = MobType.WOLF;
    private static final MobType B  = MobType.BAT;
    private static final MobType GO = MobType.GOLEM;
    private static final MobType MB = MobType.MINI_BOSS;
    private static final MobType BS = MobType.BOSS;

    // TYPES[w][i] is spawned COUNTS[w][i] times
    private static final MobType[][] TYPES = {
        {G},
        {G, W},
        {G, B},
        {W, B, GO},
        {G, GO, MB, B, W},
        {B, GO, W},
        {GO, W, B},
        {B, GO, W},
        {GO, G, B, W},
        {B, GO, BS, W, B, GO}
    };

    private static final int[][] COUNTS = {
        {5},
        {5, 3},
        {6, 12},
        {8, 8, 2},
        {6, 3, 1, 8, 6},
        {16, 5, 9},
        {6, 12, 14},
        {18, 8, 10},
        {9, 10, 18, 12},
        {10, 5, 1, 10, 10, 3}
    };

    private WaveData() { }   // only static methods

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
        return buildQueue(wave).size();
    }
}