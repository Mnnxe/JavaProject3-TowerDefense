// Group members: 6xxxxxxx Firstname Lastname 1, 6xxxxxxx Firstname Lastname 2,
//                6xxxxxxx Firstname Lastname 3, 6xxxxxxx Firstname Lastname 4,
//                6xxxxxxx Firstname Lastname 5
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

    private TowerType type;
    private int col, row, level = 1, totalPaid;
    private double cooldown = 0, angle = 0, fireFlash = 0;

    public Tower(TowerType type, int col, int row) {
        this.type = type;
        this.col = col;
        this.row = row;
        this.totalPaid = type.getCost(1);
    }

    public TowerType getType() { return type; }
    public int getLevel() { return level; }
    public double getCenterX() { return (col + 0.5) * CELL; }
    public double getCenterY() { return (row + 0.5) * CELL; }
    public double getRangePixels() { return type.getRange(level) * CELL; }
    public int getUpgradeCost() { return level < 3 ? type.getCost(level + 1) : -1; }
    public int getRefund() { return (int) Math.round(totalPaid * GameConstants.REFUND_RATE); }

    public void upgrade() {
        if (level < 3) {
            totalPaid += type.getCost(level + 1);
            level++;
        }
    }

    // pick the mob that walked farthest inside range and shoot
    public void update(double dt, ArrayList<Mob> mobs, ArrayList<Projectile> projectiles) {
        if (fireFlash > 0) fireFlash -= dt;
        cooldown -= dt;
        if (cooldown > 0) return;

        double cx = getCenterX(), cy = getCenterY(), range = getRangePixels();
        Mob target = null;
        for (Mob m : mobs) {
            if (!m.isAlive() || m.getHp() <= 0) continue;
            double dx = m.getX() - cx, dy = m.getY() - cy;
            if (dx * dx + dy * dy <= range * range) {
                if (target == null || m.getDistance() > target.getDistance()) target = m;
            }
        }
        if (target == null) { cooldown = 0; return; }

        angle = Math.atan2(target.getY() - cy, target.getX() - cx);
        projectiles.add(new Projectile(type, level, cx, cy, target));
        cooldown = 1.0 / type.getRate(level);
        fireFlash = 0.1;
        SoundManager.playSFX(type.name().toLowerCase());
    }

    // ================= drawing (archer.png / cannon.png / ice.png / fire.png) =================
    public void draw(Graphics2D g) {
        int x = col * CELL, y = row * CELL, cx = x + CELL / 2, cy = y + CELL / 2;
       
        BufferedImage tower = GameConstants.getImage(type.name().toLowerCase()+ level + ".png");
        BufferedImage weapon = GameConstants.getImage(type.name().toLowerCase() + "_weapon.png");
        if (tower != null && weapon != null) {
            g.drawImage(tower, x + 4, y + 4, CELL - 8, CELL - 10, null);
            if (weapon != null) {
                AffineTransform old = g.getTransform();
                g.translate(cx, cy);
                int wSize = CELL;
                if(type != TowerType.ICE && type != TowerType.FIRE){
                    g.rotate(angle + Math.PI / 2);
                    g.drawImage(weapon, -wSize / 2, -wSize / 2, wSize, wSize, null);
                }
                else    g.drawImage(weapon, -wSize / 2, (-wSize / 2)-10, wSize, wSize, null);

                g.setTransform(old);
            }
        } 
        else drawShape(g, cx, cy);
        
        if (fireFlash > 0) {
            int fx = (int) (cx + Math.cos(angle) * 20), fy = (int) (cy + Math.sin(angle) * 20);
            g.setColor(new Color(255, 255, 150, 190));
            g.fillOval(fx - 5, fy - 5, 10, 10);
        }
        /*
        for (int i = 0; i < level; i++) {            // level stars
        g.setColor(GameConstants.GOLD);
        g.fillOval(x + 6 + i * 9, y + CELL - 12, 7, 7);
        g.setColor(Color.BLACK);
        g.drawOval(x + 6 + i * 9, y + CELL - 12, 7, 7);
    }
    */
    }

    // default drawing when the PNG is missing (turret rotates to the target)
    private void drawShape(Graphics2D g, int cx, int cy) {
        g.setColor(type.getColor());
        g.fillOval(cx - 14, cy - 14, 28, 28);
        g.setColor(type.getColor().darker());
        g.drawOval(cx - 14, cy - 14, 28, 28);

        AffineTransform old = g.getTransform();
        g.translate(cx, cy);
        g.rotate(angle);
        switch (type) {
            case ARCHER:
                g.setColor(new Color(90, 55, 25));
                g.setStroke(new BasicStroke(3));
                g.drawArc(-4, -12, 14, 24, -90, 180);
                g.setStroke(new BasicStroke(1));
                g.setColor(Color.LIGHT_GRAY);
                g.drawLine(-6, 0, 16, 0);
                break;
            case CANNON:
                g.setColor(new Color(30, 30, 35));
                g.fillRect(0, -5, 21, 10);
                g.fillOval(-8, -8, 16, 16);
                break;
            case ICE:
                g.setColor(new Color(235, 250, 255));
                g.fillPolygon(new int[]{-6, 6, 19, 6}, new int[]{0, -6, 0, 6}, 4);
                g.setColor(new Color(60, 140, 200));
                g.drawPolygon(new int[]{-6, 6, 19, 6}, new int[]{0, -6, 0, 6}, 4);
                break;
            case FIRE:
                g.setColor(new Color(120, 30, 10));
                g.fillRect(0, -4, 17, 8);
                g.setColor(Color.YELLOW);
                g.fillOval(-5, -5, 10, 10);
                break;
        }
        g.setTransform(old);
    }
}

// =====================================================================
//  TowerType : stats of every tower type and level
// =====================================================================
enum TowerType {
    ARCHER("Archer", 1, new int[]{50, 75, 120},   new int[]{15, 28, 50}, new double[]{1.00, 1.25, 1.67}, new double[]{2.5, 3.0, 3.5}, new Color(160, 110, 60)),
    CANNON("Cannon", 2, new int[]{80, 120, 170},  new int[]{20, 38, 65}, new double[]{0.50, 0.58, 0.67}, new double[]{2.0, 2.2, 2.5}, new Color(80, 80, 92)),
    ICE   ("Ice",    3, new int[]{100, 140, 190}, new int[]{8, 15, 25},  new double[]{0.80, 1.00, 1.25}, new double[]{2.0, 2.5, 3.0}, new Color(120, 200, 240)),
    FIRE  ("Fire",   4, new int[]{130, 180, 250}, new int[]{35, 65, 110},new double[]{0.70, 0.83, 1.00}, new double[]{2.5, 3.0, 3.5}, new Color(230, 90, 40));

    public static final double[] SLOW_PCT = {0.30, 0.45, 0.60};
    public static final double SLOW_TIME = 2.5;
    public static final int[] BURN_DPS = {10, 20, 30};
    public static final double BURN_TIME = 3.0;
    public static final double SPLASH_RADIUS = 1.0; // grid

    private String name;
    private int unlockWave;
    private int[] cost, damage;
    private double[] rate, range;
    private Color color;

    TowerType(String name, int unlockWave, int[] cost, int[] damage, double[] rate, double[] range, Color color) {
        this.name = name; this.unlockWave = unlockWave; this.cost = cost;
        this.damage = damage; this.rate = rate; this.range = range; this.color = color;
    }

    public String getName() { return name; }
    public int getUnlockWave() { return unlockWave; }
    public boolean isAvailable(int wave) { return wave >= unlockWave; }
    public int getCost(int level) { return cost[level - 1]; }
    public int getDamage(int level) { return damage[level - 1]; }
    public double getRate(int level) { return rate[level - 1]; }
    public double getRange(int level) { return range[level - 1]; }
    public Color getColor() { return color; }

    public String getSkill(int level) {
        switch (this) {
            case CANNON: return "Splash AoE (1 grid)";
            case ICE:    return "Slow " + (int) Math.round(SLOW_PCT[level - 1] * 100) + "% for 2.5s";
            case FIRE:   return "Burn " + BURN_DPS[level - 1] + " dmg/s for 3s";
            default:     return "Single target";
        }
    }

    public String getInfo(int level) {
        return String.format("%s Lv.%d  |  Cost %dc  |  Dmg %d  |  %.2f hit/s  |  Range %.1f  |  %s",
                name, level, getCost(level), getDamage(level), getRate(level), getRange(level), getSkill(level));
    }
}