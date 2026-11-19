// Group members: 6xxxxxxx Firstname Lastname 1, 6xxxxxxx Firstname Lastname 2,
//                6xxxxxxx Firstname Lastname 3, 6xxxxxxx Firstname Lastname 4,
//                6xxxxxxx Firstname Lastname 5
package JavaProject3_TowerDefense;
import java.awt.*;
import java.util.ArrayList;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

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
        this.type = type; 
        this.level = level;
        this.x = x; 
        this.y = y; 
        this.target = target;
        this.tx = target.getX(); 
        this.ty = target.getY();
        if (type == TowerType.ARCHER) speed = 520;
        else if (type == TowerType.CANNON) speed = 360;
        else speed = 480;
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
            case ARCHER:
                if (targetOk) target.takeDamage(dmg);
                //effects.add(Effect.hit(x, y, type));
                break;
            case CANNON:
                double r = TowerType.SPLASH_RADIUS * GameConstants.CELL;
                for (Mob m : mobs) {
                    if (!m.isAlive() || m.getHp() <= 0) continue;
                    double ddx = m.getX() - x, ddy = m.getY() - y;
                    if (ddx * ddx + ddy * ddy <= r * r) m.takeDamage(dmg);
                }
                effects.add(Effect.explosion(x, y, r, type));
                break;
            case ICE:
                if (targetOk) {
                    target.takeDamage(dmg);
                    target.applySlow(TowerType.SLOW_PCT[level - 1], TowerType.SLOW_TIME);
                }
                effects.add(Effect.iceBurst(x, y, type));
                break;
            case FIRE:
                if (targetOk) {
                    target.takeDamage(dmg);
                    target.applyBurn(TowerType.BURN_DPS[level - 1], TowerType.BURN_TIME);
                }
                effects.add(Effect.fireBurst(x, y, type));
                break;
            default:
                if (targetOk) target.takeDamage(dmg);
        }
    }

    public void draw(Graphics2D g) {
        int ix = (int) x, iy = (int) y;
        int size = 1;
        //Stroke old = g.getStroke();
         
        BufferedImage pjt = GameConstants.getImage(type.name().toLowerCase() + "_projectile.png");
        if (pjt != null) {
        double angle = Math.atan2(ty - y, tx - x);
        AffineTransform old = g.getTransform();
        g.translate(ix, iy);
        g.rotate(angle+Math.PI/2);

        g.drawImage(pjt, size / 2, size / 2, null);

        g.setTransform(old);
        } /*else 
            
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
        //g.setStroke(old);
    }*/
}
}