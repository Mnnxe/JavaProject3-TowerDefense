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

    public void draw(Graphics2D g) {
        int x = col * CELL, y = row * CELL, cx = x + CELL / 2, cy = y + CELL / 2;
        g.setColor(new Color(125, 125, 135));
        g.fillRoundRect(x + 3, y + 3, CELL - 6, CELL - 6, 12, 12);
        g.setColor(new Color(70, 70, 80));
        g.drawRoundRect(x + 3, y + 3, CELL - 6, CELL - 6, 12, 12);

        BufferedImage img = GameConstants.getImage(type.name().toLowerCase() + ".png");
        if (img != null) {
            g.drawImage(img, x + 4, y + 4, CELL - 8, CELL - 8, null);
        } else {
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

        if (fireFlash > 0) {
            int fx = (int) (cx + Math.cos(angle) * 20), fy = (int) (cy + Math.sin(angle) * 20);
            g.setColor(new Color(255, 255, 150, 190));
            g.fillOval(fx - 5, fy - 5, 10, 10);
        }

        for (int i = 0; i < level; i++) {            // level stars
            g.setColor(GameConstants.GOLD);
            g.fillOval(x + 6 + i * 9, y + CELL - 12, 7, 7);
            g.setColor(Color.BLACK);
            g.drawOval(x + 6 + i * 9, y + CELL - 12, 7, 7);
        }
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

// =====================================================================
//  Projectile : arrow / cannonball / ice shard / fireball
// =====================================================================
class Projectile {
    private TowerType type;
    private int level;
    private double x, y, tx, ty, speed;
    private Mob target;
    private boolean done = false;

    public Projectile(TowerType type, int level, double x, double y, Mob target) {
        this.type = type; this.level = level;
        this.x = x; this.y = y; this.target = target;
        this.tx = target.getX(); this.ty = target.getY();
        if (type == TowerType.ARCHER) speed = 520;
        else if (type == TowerType.CANNON) speed = 320;
        else speed = 420;
    }

    public boolean isDone() { return done; }

    public void update(double dt, ArrayList<Mob> mobs, ArrayList<Effect> effects) {
        if (done) return;
        if (target.isAlive() && target.getHp() > 0) { tx = target.getX(); ty = target.getY(); }
        double dx = tx - x, dy = ty - y, d = Math.sqrt(dx * dx + dy * dy);
        double step = speed * dt;
        if (d <= step || d < 4) {
            x = tx; y = ty;
            hit(mobs, effects);
            done = true;
        } else {
            x += dx / d * step;
            y += dy / d * step;
        }
    }

    private void hit(ArrayList<Mob> mobs, ArrayList<Effect> effects) {
        int dmg = type.getDamage(level);
        boolean targetOk = target.isAlive() && target.getHp() > 0;
        switch (type) {
            case CANNON:
                double r = TowerType.SPLASH_RADIUS * GameConstants.CELL;
                for (Mob m : mobs) {
                    if (!m.isAlive() || m.getHp() <= 0) continue;
                    double ddx = m.getX() - x, ddy = m.getY() - y;
                    if (ddx * ddx + ddy * ddy <= r * r) m.takeDamage(dmg);
                }
                effects.add(Effect.explosion(x, y, r));
                break;
            case ICE:
                if (targetOk) {
                    target.takeDamage(dmg);
                    target.applySlow(TowerType.SLOW_PCT[level - 1], TowerType.SLOW_TIME);
                }
                effects.add(Effect.iceBurst(x, y));
                break;
            case FIRE:
                if (targetOk) {
                    target.takeDamage(dmg);
                    target.applyBurn(TowerType.BURN_DPS[level - 1], TowerType.BURN_TIME);
                }
                effects.add(Effect.fireBurst(x, y));
                break;
            default:
                if (targetOk) target.takeDamage(dmg);
        }
    }

    public void draw(Graphics2D g) {
        int ix = (int) x, iy = (int) y;
        Stroke old = g.getStroke();
        switch (type) {
            case ARCHER:
                double a = Math.atan2(ty - y, tx - x);
                g.setStroke(new BasicStroke(2));
                g.setColor(new Color(110, 70, 30));
                g.drawLine((int) (x - Math.cos(a) * 12), (int) (y - Math.sin(a) * 12), ix, iy);
                g.setColor(Color.LIGHT_GRAY);
                g.fillOval(ix - 2, iy - 2, 4, 4);
                break;
            case CANNON:
                g.setColor(Color.DARK_GRAY);
                g.fillOval(ix - 6, iy - 6, 12, 12);
                g.setColor(Color.BLACK);
                g.drawOval(ix - 6, iy - 6, 12, 12);
                break;
            case ICE:
                g.setColor(new Color(170, 235, 255));
                g.fillPolygon(new int[]{ix, ix + 5, ix, ix - 5}, new int[]{iy - 6, iy, iy + 6, iy}, 4);
                g.setColor(Color.WHITE);
                g.drawPolygon(new int[]{ix, ix + 5, ix, ix - 5}, new int[]{iy - 6, iy, iy + 6, iy}, 4);
                break;
            case FIRE:
                g.setColor(new Color(255, 90, 20));
                g.fillOval(ix - 6, iy - 6, 12, 12);
                g.setColor(Color.YELLOW);
                g.fillOval(ix - 3, iy - 3, 6, 6);
                break;
        }
        g.setStroke(old);
    }
}

// =====================================================================
//  Effect : explosion, ice/fire burst, death puff, floating text, banner
// =====================================================================
class Effect {
    private static final int EXPLOSION = 0, ICE = 1, FIRE = 2, DEATH = 3, TEXT = 4, BANNER = 5;

    private int kind;
    private double x, y, life, maxLife, radius = 16;
    private String text = "";
    private Color color = Color.WHITE;

    private Effect(int kind, double x, double y, double life) {
        this.kind = kind; this.x = x; this.y = y; this.life = life; this.maxLife = life;
    }

    public static Effect explosion(double x, double y, double radius) {
        Effect e = new Effect(EXPLOSION, x, y, 0.45); e.radius = radius; return e;
    }
    public static Effect iceBurst(double x, double y) { return new Effect(ICE, x, y, 0.3); }
    public static Effect fireBurst(double x, double y) { return new Effect(FIRE, x, y, 0.3); }
    public static Effect death(double x, double y, Color c) {
        Effect e = new Effect(DEATH, x, y, 0.45); e.color = c; return e;
    }
    public static Effect text(double x, double y, String t, Color c) {
        Effect e = new Effect(TEXT, x, y, 1.1); e.text = t; e.color = c; return e;
    }
    public static Effect banner(double x, double y, String t, Color c) {
        Effect e = new Effect(BANNER, x, y, 2.2); e.text = t; e.color = c; return e;
    }

    public void update(double dt) {
        life -= dt;
        if (kind == TEXT) y -= 30 * dt;
    }

    public boolean isDone() { return life <= 0; }

    public void draw(Graphics2D g) {
        double a = Math.max(0, Math.min(1, life / maxLife));
        double p = 1 - a;
        int ix = (int) x, iy = (int) y;
        Stroke old = g.getStroke();
        switch (kind) {
            case EXPLOSION: {
                int r = (int) (radius * (0.3 + 0.7 * p));
                g.setColor(fade(new Color(255, 140, 30), a * 0.6));
                g.fillOval(ix - r, iy - r, 2 * r, 2 * r);
                int r2 = r / 2;
                g.setColor(fade(new Color(255, 240, 120), a));
                g.fillOval(ix - r2, iy - r2, 2 * r2, 2 * r2);
                break;
            }
            case ICE: {
                int r = (int) (6 + 14 * p);
                g.setStroke(new BasicStroke(3));
                g.setColor(fade(new Color(160, 230, 255), a));
                g.drawOval(ix - r, iy - r, 2 * r, 2 * r);
                break;
            }
            case FIRE: {
                int r = (int) (5 + 12 * p);
                g.setColor(fade(new Color(255, 90, 20), a * 0.8));
                g.fillOval(ix - r, iy - r, 2 * r, 2 * r);
                break;
            }
            case DEATH: {
                int r = (int) (8 + 16 * p);
                g.setColor(fade(color, a * 0.8));
                g.drawOval(ix - r, iy - r, 2 * r, 2 * r);
                for (int k = 0; k < 6; k++) {
                    double ang = k * Math.PI / 3;
                    int px = (int) (ix + Math.cos(ang) * r), py = (int) (iy + Math.sin(ang) * r);
                    g.fillOval(px - 3, py - 3, 6, 6);
                }
                break;
            }
            case TEXT:
                drawCenteredText(g, ix, iy, 15, a);
                break;
            case BANNER:
                drawCenteredText(g, ix, iy, 40, Math.min(1, life / 0.5));
                break;
        }
        g.setStroke(old);
    }

    private void drawCenteredText(Graphics2D g, int cx, int cy, int size, double a) {
        g.setFont(new Font("SansSerif", Font.BOLD, size));
        FontMetrics fm = g.getFontMetrics();
        int tx = cx - fm.stringWidth(text) / 2, ty = cy + fm.getAscent() / 2 - 2;
        g.setColor(fade(Color.BLACK, a));
        g.drawString(text, tx + 2, ty + 2);
        g.setColor(fade(color, a));
        g.drawString(text, tx, ty);
    }

    private static Color fade(Color c, double a) {
        int alpha = (int) (255 * Math.max(0, Math.min(1, a)));
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha);
    }
}