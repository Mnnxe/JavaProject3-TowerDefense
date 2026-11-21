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
//  Projectile : arrow / cannonball / ice shard / fireball
// =====================================================================
public class Projectile {
    private static final int FALLBACK_SIZE = 8;

    private final TowerType type;
    private final int level;
    private final Mob target;
    private double x, y, tx, ty;
    private boolean done = false;

    public Projectile(TowerType type, int level, double x, double y, Mob target) {
        this.type = type;
        this.level = level;
        this.x = x;
        this.y = y;
        this.target = target;
        this.tx = target.getX();
        this.ty = target.getY();
    }

    public boolean isDone() { return done; }

    // fly to the target (follows it while it is alive)
    public void update(double dt, ArrayList<Mob> mobs, ArrayList<Effect> effects) {
        if (done) return;
        if (target.isAlive()) {
            tx = target.getX();
            ty = target.getY();
        }
        double dx = tx - x, dy = ty - y;
        double d = Math.sqrt(dx * dx + dy * dy);
        double step = type.getProjectileSpeed() * dt;
        if (d <= step) {
            x = tx;
            y = ty;
            hit(mobs, effects);
            done = true;
        } else {
            x += dx / d * step;
            y += dy / d * step;
        }
    }

    private void hit(ArrayList<Mob> mobs, ArrayList<Effect> effects) {
        int dmg = type.getDamage(level);
        if (type == TowerType.CANNON) {                     // splash: every mob near the hit point
            double r = TowerType.SPLASH_RADIUS * GameConstants.CELL;
            for (Mob m : mobs) {
                double ddx = m.getX() - x, ddy = m.getY() - y;
                if (m.isAlive() && ddx * ddx + ddy * ddy <= r * r) m.takeDamage(dmg);
            }
        } else if (target.isAlive()) {                      // single target
            target.takeDamage(dmg);
            if (type == TowerType.ICE) target.applySlow(TowerType.SLOW_PCT[level - 1], TowerType.SLOW_TIME);
            else if (type == TowerType.FIRE) target.applyBurn(TowerType.BURN_DPS[level - 1], TowerType.BURN_TIME);
        }
        if (type != TowerType.ARCHER) effects.add(Effect.impact(x, y, type));
    }

    public void draw(Graphics2D g) {
        BufferedImage img = GameConstants.getImage(type.getKey() + "_projectile.png");
        if (img == null) {                                   // simple fallback
            g.setColor(type.getColor());
            g.fillOval((int) x - FALLBACK_SIZE / 2, (int) y - FALLBACK_SIZE / 2, FALLBACK_SIZE, FALLBACK_SIZE);
            return;
        }
        AffineTransform old = g.getTransform();
        g.translate(x, y);
        g.rotate(Math.atan2(ty - y, tx - x) + Math.PI / 2);  // point to the target
        g.drawImage(img, -img.getWidth() / 2, -img.getHeight() / 2, null);
        g.setTransform(old);
    }
}