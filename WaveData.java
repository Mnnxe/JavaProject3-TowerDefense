package JavaProject3_TowerDefense;
import java.util.ArrayList;

public class WaveData {
    private static final MobType G = MobType.GOBLIN, W = MobType.WOLF, B = MobType.BAT,
            GO = MobType.GOLEM, MB = MobType.MINI_BOSS, BS = MobType.BOSS;

    private static final MobType[][] TYPES = {
        {G}, {G, W}, {G, B}, {W, G, B, GO}, {G, GO, MB, B, G, W},
        {B, GO, W, G}, {GO, W, B, G}, {B, GO, W, G}, {GO, G, B, W}, {B, MB, GO, W, BS, B, GO, G}
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
