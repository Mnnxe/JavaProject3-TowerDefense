// 6813115 Renuka Choosingkae
// 6813238 Napat Chonsaktrakul
// 6813248 Pimpichcha Saengngoen
// 6813258 Sittichok Boonthittanont
package JavaProject3_TowerDefense;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

// =====================================================================
//  Tower : a tower placed on the map
// =====================================================================
public class Tower {
    private static final int CELL = GameConstants.CELL;
    private static final int WEAPON_OFFSET = 10, WEAPON_RISE_PER_LEVEL = 3, FALLBACK_PAD = 6;

    private final TowerType type;
    private final int col, row;
    private int level = 1, totalPaid;
    private double cooldown = 0, angle = 0;

    public Tower(TowerType type, int col, int row) {
        this.type = type;
        this.col = col;
        this.row = row;
        this.totalPaid = type.getCost(1);
    }

    public TowerType getType() { return type; }
    public int getLevel() { return level; }
    public boolean isMaxLevel() { return level >= TowerType.MAX_LEVEL; }
    public double getCenterX() { return (col + 0.5) * CELL; }
    public double getCenterY() { return (row + 0.5) * CELL; }
    public double getRangePixels() { return type.getRange(level) * CELL; }
    public int getUpgradeCost() { return isMaxLevel() ? 0 : type.getCost(level + 1); }
    public int getRefund() { return (int) Math.round(totalPaid * GameConstants.REFUND_RATE); }

    // the weapon sits on top of the tower (higher tower = higher weapon)
    private double getWeaponX() { return getCenterX(); }
    private double getWeaponY() { return getCenterY() - WEAPON_OFFSET - WEAPON_RISE_PER_LEVEL * level; }

    public void upgrade() {
        if (isMaxLevel()) return;
        totalPaid += type.getCost(level + 1);
        level++;
    }

    // ================= shooting =================
    public void update(double dt, ArrayList<Mob> mobs, ArrayList<Projectile> projectiles) {
        cooldown -= dt;
        if (cooldown > 0) return;

        Mob target = findTarget(mobs);
        if (target == null) {
            cooldown = 0;
            return;
        }
        angle = Math.atan2(target.getY() - getCenterY(), target.getX() - getCenterX());
        projectiles.add(new Projectile(type, level, getWeaponX(), getWeaponY(), target));
        cooldown = 1.0 / type.getRate(level);
        SoundManager.playSFX(type.getKey());
    }

    // the mob that walked the farthest inside the range
    private Mob findTarget(ArrayList<Mob> mobs) {
        double range = getRangePixels();
        Mob target = null;
        for (Mob m : mobs) {
            if (!m.isAlive()) continue;
            double dx = m.getX() - getCenterX(), dy = m.getY() - getCenterY();
            boolean inRange = dx * dx + dy * dy <= range * range;
            if (inRange && (target == null || m.getDistance() > target.getDistance())) target = m;
        }
        return target;
    }

    // ================= drawing (tower1-3.png + <type>_weapon.png) =================
    public void draw(Graphics2D g) {
        int x = col * CELL, y = row * CELL;
        BufferedImage base = GameConstants.getImage("tower" + level + ".png");
        BufferedImage weapon = GameConstants.getImage(type.getKey() + "_weapon.png");
        if (base == null || weapon == null) {
            drawSimple(g, x, y);
            return;
        }

        g.drawImage(base, x + 4, y - 15, CELL - 8, CELL + 10, null);
        AffineTransform old = g.getTransform();
        g.translate(getWeaponX(), getWeaponY());
        if (type.rotatesWeapon()) {
            g.rotate(angle + Math.PI / 2);                   // turn to face the target
            g.drawImage(weapon, -CELL / 2, -CELL / 2, CELL, CELL, null);
        } else {
            g.drawImage(weapon, -CELL / 2 + 12, -CELL / 2 + 2, CELL / 2, CELL / 2, null);
        }
        g.setTransform(old);
    }

    // fallback: coloured circle with the level number
    private void drawSimple(Graphics2D g, int x, int y) {
        int size = CELL - 2 * FALLBACK_PAD;
        g.setColor(type.getColor());
        g.fillOval(x + FALLBACK_PAD, y + FALLBACK_PAD, size, size);
        g.setColor(Color.BLACK);
        g.drawOval(x + FALLBACK_PAD, y + FALLBACK_PAD, size, size);

        String lv = String.valueOf(level);
        g.setFont(GameConstants.BOLD_FONT);
        FontMetrics fm = g.getFontMetrics();
        g.drawString(lv, x + (CELL - fm.stringWidth(lv)) / 2, y + (CELL + fm.getAscent()) / 2 - 2);
    }
}

// =====================================================================
//  TowerType : stats of every tower type and level
// =====================================================================
enum TowerType {
    //     name     unlock  cost (Lv1-3)              damage                   hits per second                 range (grid)                  bullet speed  colour
    ARCHER("Archer", 1, new int[]{50, 75, 120},   new int[]{15, 26, 45}, new double[]{1.00, 1.20, 1.55}, new double[]{2.5, 2.9, 3.3}, 520, new Color(160, 110, 60)),
    CANNON("Cannon", 2, new int[]{80, 120, 170},  new int[]{20, 35, 58}, new double[]{0.50, 0.56, 0.63}, new double[]{2.0, 2.2, 2.4}, 320, new Color(80, 80, 92)),
    ICE   ("Ice",    3, new int[]{100, 140, 190}, new int[]{8, 14, 23},  new double[]{0.80, 0.95, 1.15}, new double[]{2.0, 2.4, 2.8}, 420, new Color(120, 200, 240)),
    FIRE  ("Fire",   4, new int[]{130, 180, 250}, new int[]{35, 60, 95}, new double[]{0.70, 0.80, 0.95}, new double[]{2.5, 2.9, 3.3}, 420, new Color(230, 90, 40));

    public static final int MAX_LEVEL = 3;
    public static final double[] SLOW_PCT = {0.30, 0.45, 0.60};
    public static final double SLOW_TIME = 2.5;
    public static final int[] BURN_DPS = {10, 20, 30};
    public static final double BURN_TIME = 3.0;
    public static final double SPLASH_RADIUS = 1.0;   // grid

    private final String name;
    private final int unlockWave, projectileSpeed;
    private final int[] cost, damage;
    private final double[] rate, range;
    private final Color color;

    TowerType(String name, int unlockWave, int[] cost, int[] damage, double[] rate, double[] range,
              int projectileSpeed, Color color) {
        this.name = name;
        this.unlockWave = unlockWave;
        this.cost = cost;
        this.damage = damage;
        this.rate = rate;
        this.range = range;
        this.projectileSpeed = projectileSpeed;
        this.color = color;
    }

    // "archer", "cannon", ... used for image and sound file names
    public String getKey() { return name().toLowerCase(); }
    public String getName() { return name; }
    public int getUnlockWave() { return unlockWave; }
    public boolean isAvailable(int wave) { return wave >= unlockWave; }
    public int getCost(int level) { return cost[level - 1]; }
    public int getDamage(int level) { return damage[level - 1]; }
    public double getRate(int level) { return rate[level - 1]; }
    public double getRange(int level) { return range[level - 1]; }
    public int getProjectileSpeed() { return projectileSpeed; }
    public Color getColor() { return color; }
    public boolean rotatesWeapon() { return this == ARCHER || this == CANNON; }

    public String getSkill(int level) {
        switch (this) {
            case CANNON: return "Splash damage (" + SPLASH_RADIUS + " grid)";
            case ICE:    return "Slow " + Math.round(SLOW_PCT[level - 1] * 100) + "% for " + SLOW_TIME + "s";
            case FIRE:   return "Burn " + BURN_DPS[level - 1] + " dmg/s for " + BURN_TIME + "s";
            default:     return "Single target";
        }
    }

    public String getInfo(int level) {
        return String.format("%s Lv.%d  |  Cost %dc  |  Dmg %d  |  %.2f hit/s  |  Range %.1f  |  %s",
                name, level, getCost(level), getDamage(level), getRate(level), getRange(level), getSkill(level));
    }
}