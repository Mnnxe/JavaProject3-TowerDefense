// 6813115 Renuka Choosingkae
// 6813238 Napat Chonsaktrakul
// 6813248 Pimpichcha Saengngoen
// 6813258 Sittichok Boonthittanont
package JavaProject3_TowerDefense;

import java.awt.*;
import java.awt.image.BufferedImage;

// =====================================================================
//  Mob : one enemy walking on the path
// =====================================================================
public class Mob {
    private static final int CELL = GameConstants.CELL;
    private static final int WALK_FRAMES = 3, FRAME_DISTANCE = 8, MIN_SIZE = 32, IMAGE_LIFT = 7;
    private static final float FROST_ALPHA = 0.6f, BURN_ALPHA = 0.7f;
    private static final Color FROST_COLOR = new Color(130, 210, 255, 110);
    private static final Color BURN_COLOR  = new Color(255, 120, 0, 110);

    private final MobType type;
    private final MapData map;
    private double x, y, hp, distance = 0;
    private int nextWp = 1;
    private double slowTimer = 0, slowPct = 0, burnTimer = 0, burnDps = 0;
    private boolean alive = true, reachedBase = false;

    public Mob(MobType type, MapData map) {
        this.type = type;
        this.map = map;
        this.hp = type.getMaxHp();
        double[] start = map.getWaypointPixel(0);
        x = start[0];
        y = start[1];
    }

    // ================= logic =================
    public void update(double dt) {
        if (!alive || reachedBase) return;
        if (burnTimer > 0) {
            hp -= burnDps * dt;
            burnTimer -= dt;
        }
        double factor = 1.0;
        if (slowTimer > 0) {
            slowTimer -= dt;
            factor = 1.0 - slowPct;
        }

        double move = type.getSpeed() * CELL * factor * dt;
        while (move > 0 && nextWp < map.getWaypointCount()) {
            double[] t = map.getWaypointPixel(nextWp);
            double dx = t[0] - x, dy = t[1] - y;
            double d = Math.sqrt(dx * dx + dy * dy);
            if (d <= move) {                // reach this waypoint, keep moving to the next one
                x = t[0];
                y = t[1];
                move -= d;
                distance += d;
                nextWp++;
            } else {
                x += dx / d * move;
                y += dy / d * move;
                distance += move;
                move = 0;
            }
        }
        if (nextWp >= map.getWaypointCount()) reachedBase = true;
    }

    public void takeDamage(double dmg) { hp -= dmg; }

    public void applySlow(double pct, double time) {
        if (slowTimer <= 0 || pct >= slowPct) slowPct = pct;
        slowTimer = time;
    }

    public void applyBurn(double dps, double time) {
        if (burnTimer <= 0 || dps >= burnDps) burnDps = dps;
        burnTimer = time;
    }

    public MobType getType() { return type; }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getHp() { return hp; }
    public double getDistance() { return distance; }
    public boolean isAlive() { return alive && hp > 0; }
    public void setAlive(boolean a) { alive = a; }
    public boolean hasReachedBase() { return reachedBase; }

    // ================= drawing =================
    public void draw(Graphics2D g) {
        int r = type.getRadius();
        int cx = (int) x, cy = (int) y;
        int s = Math.max(MIN_SIZE, r * 3);

        // 3 walking frames, e.g. goblin1.png, goblin2.png, goblin3.png
        int frame = ((int) (distance / FRAME_DISTANCE) % WALK_FRAMES) + 1;
        BufferedImage img = GameConstants.getImage(type.getImagePrefix(map.isSpecial()) + frame + ".png");
        if (img != null) {
            g.drawImage(img, cx - s / 2, cy - s / 2 - IMAGE_LIFT, s, s, null);
        } else {
            g.setColor(Color.BLACK);                       // simple fallback
            g.fillOval(cx - r, cy - r, 2 * r, 2 * r);
        }

        if (slowTimer > 0) drawStatus(g, "frost.png", FROST_COLOR, FROST_ALPHA, cx, cy, s);
        if (burnTimer > 0) drawStatus(g, "burn.png", BURN_COLOR, BURN_ALPHA, cx, cy, s);
        drawHpBar(g, cx, cy, r);
        if (map.isSpecial() || type.isBoss()) drawName(g, cx, cy, r);
    }

    // frost / burn picture on top of the mob, or a see-through coloured circle
    private void drawStatus(Graphics2D g, String file, Color fallback, float alpha, int cx, int cy, int s) {
        BufferedImage img = GameConstants.getImage(file);
        if (img != null) {
            GameConstants.drawFaded(g, img, cx - s / 2, cy - s / 2, s, s, alpha);
        } else {
            g.setColor(fallback);
            g.fillOval(cx - s / 2, cy - s / 2, s, s);
        }
    }

    private void drawHpBar(Graphics2D g, int cx, int cy, int r) {
        int w = Math.max(26, r * 2);
        double ratio = Math.max(0, hp / type.getMaxHp());
        g.setColor(Color.BLACK);
        g.fillRect(cx - w / 2 - 1, cy - r - 11, w + 2, 6);
        g.setColor(GameConstants.hpColor(ratio));
        g.fillRect(cx - w / 2, cy - r - 10, (int) (w * ratio), 4);
    }

    private void drawName(Graphics2D g, int cx, int cy, int r) {
        String n = type.getName(map.isSpecial());
        g.setFont(GameConstants.TINY_FONT);
        int tx = cx - g.getFontMetrics().stringWidth(n) / 2;
        g.setColor(Color.BLACK);
        g.drawString(n, tx + 1, cy - r - 14);
        g.setColor(Color.WHITE);
        g.drawString(n, tx, cy - r - 15);
    }
}

// =====================================================================
//  MobType : stats of every mob (normal name / professor name)
//  (no HP drop -> base HP can't be recovered)
// =====================================================================
enum MobType {
    //          normal       special          hp  spawnCD speed dmg coin radius  image (normal / special)
    GOBLIN   ("Goblin",    "AJ.Ming",         60, 1.0, 1.0,   5,  10, 13, "goblin",   "ajming"),
    WOLF     ("Wolf",      "AJ.Konlakorn",    50, 1.0, 2.0,   5,  12, 13, "wolf",     "ajkonlakorn"),
    BAT      ("Bat",       "AJ.Tanasanee",    40, 0.8, 1.4,   5,   5,  9, "bat",      "ajtanasanee"),
    GOLEM    ("Golem",     "AJ.Tum",         220, 1.2, 0.6,  10,  25, 17, "golem",    "ajtum"),
    MINI_BOSS("Mini Boss", "AJ.Vasin",      1000, 1.5, 0.5,  50, 100, 20, "miniboss", "ajvasin"),
    BOSS     ("Boss",      "AJ.Kob",        2800, 1.5, 0.5, 100, 250, 24, "boss",     "ajkob");

    private final String normalName, specialName, normalImage, specialImage;
    private final int maxHp, baseDamage, coinDrop, radius;
    private final double spawnCooldown, speed;

    MobType(String normalName, String specialName, int maxHp, double spawnCooldown, double speed,
            int baseDamage, int coinDrop, int radius, String normalImage, String specialImage) {
        this.normalName = normalName;
        this.specialName = specialName;
        this.maxHp = maxHp;
        this.spawnCooldown = spawnCooldown;
        this.speed = speed;
        this.baseDamage = baseDamage;
        this.coinDrop = coinDrop;
        this.radius = radius;
        this.normalImage = normalImage;
        this.specialImage = specialImage;
    }

    public String getName(boolean special) { return special ? specialName : normalName; }
    public String getImagePrefix(boolean special) { return special ? specialImage : normalImage; }
    public int getMaxHp() { return maxHp; }
    public double getSpawnCooldown() { return spawnCooldown; }
    public double getSpeed() { return speed; }
    public int getBaseDamage() { return baseDamage; }
    public int getCoinDrop() { return coinDrop; }
    public int getRadius() { return radius; }
    public boolean isBoss() { return this == MINI_BOSS || this == BOSS; }
}