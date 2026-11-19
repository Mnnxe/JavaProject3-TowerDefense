// Group members: 6xxxxxxx Firstname Lastname 1, 6xxxxxxx Firstname Lastname 2,
//                6xxxxxxx Firstname Lastname 3, 6xxxxxxx Firstname Lastname 4,
//                6xxxxxxx Firstname Lastname 5
package JavaProject3_TowerDefense;
import java.awt.*;
import java.util.ArrayList;

// =====================================================================
//  Projectile : arrow / cannonball / ice shard / fireball
// =====================================================================
public class Projectile {
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
    Effect e = new Effect(EXPLOSION, x, y, 0.60);
    e.radius = radius;
    return e;
    }
    public static Effect iceBurst(double x, double y) {
        return new Effect(ICE, x, y, 0.45);
    }
    public static Effect fireBurst(double x, double y) {
        return new Effect(FIRE, x, y, 0.45);
    }
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