// Group members: 6xxxxxxx Firstname Lastname 1, 6xxxxxxx Firstname Lastname 2,
//                6xxxxxxx Firstname Lastname 3, 6xxxxxxx Firstname Lastname 4,
//                6xxxxxxx Firstname Lastname 5
package JavaProject3_TowerDefense;
import java.awt.*;
import java.awt.image.BufferedImage;

// =====================================================================
//  Mob : one enemy walking on the path
// =====================================================================
public class Mob {
    private static final int CELL = GameConstants.CELL;

    private MobType type;
    private MapData map;
    private boolean special;
    private double x, y, hp, distance = 0;
    private int nextWp = 1;
    private double slowTimer = 0, slowPct = 0, burnTimer = 0, burnDps = 0, hitFlash = 0;
    private boolean alive = true, reachedBase = false;

    public Mob(MobType type, MapData map, boolean special) {
        this.type = type;
        this.map = map;
        this.special = special;
        this.hp = type.getMaxHp();
        double[] p = map.getWaypointPixel(0);
        x = p[0];
        y = p[1];
    }

    public void update(double dt) {
        if (!alive || reachedBase) return;
        if (hitFlash > 0) hitFlash -= dt;
        if (burnTimer > 0) { hp -= burnDps * dt; burnTimer -= dt; }
        double factor = 1.0;
        if (slowTimer > 0) { slowTimer -= dt; factor = 1.0 - slowPct; }

        double move = type.getSpeed() * CELL * factor * dt;
        while (move > 0 && nextWp < map.getWaypointCount()) {
            double[] t = map.getWaypointPixel(nextWp);
            double dx = t[0] - x, dy = t[1] - y;
            double d = Math.sqrt(dx * dx + dy * dy);
            if (d <= move) {
                x = t[0]; y = t[1];
                move -= d; distance += d;
                nextWp++;
            } else {
                x += dx / d * move; y += dy / d * move;
                distance += move; move = 0;
            }
        }
        if (nextWp >= map.getWaypointCount()) reachedBase = true;
    }

    public void takeDamage(double dmg) { hp -= dmg; hitFlash = 0.08; }

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
    public boolean isAlive() { return alive; }
    public void setAlive(boolean a) { alive = a; }
    public boolean hasReachedBase() { return reachedBase; }
    public String getName() { return type.getName(special); }

    // ================= drawing =================
    public void draw(Graphics2D g) {
        int r = type.getRadius();
        int cx = (int) x, cy = (int) y;

        // Basic mobs use 3 walking frames from resources/images/mobs/ (e.g. mobs/goblin_1.png)
        // The frame changes with distance travelled, so the animation stops when the mob stops.
        String imageName = type.getImage(special);
        if (!special && !type.isBoss()) {
            int frame = ((int) (distance / 8) % 3) + 1;
            imageName = type.name().toLowerCase() + "_" + frame + ".png";
        }

        BufferedImage img = GameConstants.getImage(imageName);
        if (img != null) {
            int s = Math.max(32, r * 3);
            g.drawImage(img, cx - s / 2, cy - s / 2, s, s, null);
        } else {
            drawShape(g, cx, cy, r);
        }

        if (slowTimer > 0) {                                   // ice effect
            g.setColor(new Color(130, 210, 255, 110));
            g.fillOval(cx - r - 3, cy - r - 3, 2 * r + 6, 2 * r + 6);
        }
        if (burnTimer > 0) {                                   // fire effect (flickering flames)
            int flick = (int) (System.currentTimeMillis() / 60 % 3);
            g.setColor(new Color(255, 110 + flick * 40, 0, 200));
            for (int i = -1; i <= 1; i++) {
                int fx = cx + i * r / 2;
                g.fillPolygon(new int[]{fx - 3, fx, fx + 3}, new int[]{cy - r + 2, cy - r - 7 - flick * 2, cy - r + 2}, 3);
            }
        }
        if (hitFlash > 0) {
            g.setColor(new Color(255, 255, 255, 140));
            g.fillOval(cx - r, cy - r, 2 * r, 2 * r);
        }

        // HP bar
        int bw = Math.max(26, r * 2);
        double ratio = Math.max(0, hp / type.getMaxHp());
        g.setColor(Color.BLACK);
        g.fillRect(cx - bw / 2 - 1, cy - r - 11, bw + 2, 6);
        g.setColor(ratio > 0.5 ? new Color(60, 220, 60) : ratio > 0.25 ? Color.ORANGE : Color.RED);
        g.fillRect(cx - bw / 2, cy - r - 10, (int) (bw * ratio), 4);

        if (special || type.isBoss()) {                        // show names on bosses / special map
            g.setFont(new Font("SansSerif", Font.BOLD, 11));
            FontMetrics fm = g.getFontMetrics();
            String n = getName();
            g.setColor(Color.BLACK);
            g.drawString(n, cx - fm.stringWidth(n) / 2 + 1, cy - r - 14);
            g.setColor(Color.WHITE);
            g.drawString(n, cx - fm.stringWidth(n) / 2, cy - r - 15);
        }
    }

    // default drawing when the PNG is missing
    private void drawShape(Graphics2D g, int cx, int cy, int r) {
        Color body = type.getColor();
        switch (type) {
            case GOBLIN:
                g.setColor(body.darker());
                g.fillPolygon(new int[]{cx - r + 2, cx - r - 7, cx - r / 3}, new int[]{cy - r / 3, cy - r - 2, cy - r + 3}, 3);
                g.fillPolygon(new int[]{cx + r - 2, cx + r + 7, cx + r / 3}, new int[]{cy - r / 3, cy - r - 2, cy - r + 3}, 3);
                g.setColor(body);
                g.fillOval(cx - r, cy - r, 2 * r, 2 * r);
                g.setColor(Color.BLACK);
                g.drawOval(cx - r, cy - r, 2 * r, 2 * r);
                eyes(g, cx, cy, r, Color.YELLOW);
                break;
            case WOLF:
                g.setColor(body.darker());
                g.fillPolygon(new int[]{cx - r + 1, cx - r / 2, cx - 2}, new int[]{cy - r / 2, cy - r - 7, cy - r / 2}, 3);
                g.fillPolygon(new int[]{cx + 2, cx + r / 2, cx + r - 1}, new int[]{cy - r / 2, cy - r - 7, cy - r / 2}, 3);
                g.setColor(body);
                g.fillOval(cx - r - 3, cy - r + 3, 2 * r + 6, 2 * r - 4);
                g.setColor(Color.BLACK);
                g.drawOval(cx - r - 3, cy - r + 3, 2 * r + 6, 2 * r - 4);
                eyes(g, cx, cy + 2, r, Color.RED);
                break;
            case BAT:
                int flap = (int) (Math.sin(System.currentTimeMillis() / 70.0) * 5);
                g.setColor(body.darker());
                g.fillPolygon(new int[]{cx, cx - r - 9, cx - r - 3, cx - r / 2}, new int[]{cy, cy - 6 + flap, cy + 5, cy + 3}, 4);
                g.fillPolygon(new int[]{cx, cx + r + 9, cx + r + 3, cx + r / 2}, new int[]{cy, cy - 6 + flap, cy + 5, cy + 3}, 4);
                g.setColor(body);
                g.fillOval(cx - r, cy - r, 2 * r, 2 * r);
                g.setColor(Color.BLACK);
                g.drawOval(cx - r, cy - r, 2 * r, 2 * r);
                g.setColor(Color.RED);
                g.fillOval(cx - 4, cy - 3, 3, 3);
                g.fillOval(cx + 1, cy - 3, 3, 3);
                break;
            case GOLEM:
                g.setColor(body);
                g.fillRoundRect(cx - r, cy - r, 2 * r, 2 * r, 10, 10);
                g.setColor(body.darker().darker());
                g.drawRoundRect(cx - r, cy - r, 2 * r, 2 * r, 10, 10);
                g.drawLine(cx - r / 2, cy + r / 3, cx, cy + r / 2);
                g.drawLine(cx + r / 3, cy - r / 2, cx + r / 2, cy);
                eyes(g, cx, cy, r, Color.ORANGE);
                break;
            case MINI_BOSS:
                g.setColor(Color.WHITE);
                g.fillPolygon(new int[]{cx - r + 3, cx - r - 5, cx - r / 3}, new int[]{cy - r / 2, cy - r - 10, cy - r + 2}, 3);
                g.fillPolygon(new int[]{cx + r - 3, cx + r + 5, cx + r / 3}, new int[]{cy - r / 2, cy - r - 10, cy - r + 2}, 3);
                g.setColor(body);
                g.fillOval(cx - r, cy - r, 2 * r, 2 * r);
                g.setColor(Color.BLACK);
                g.drawOval(cx - r, cy - r, 2 * r, 2 * r);
                eyes(g, cx, cy, r, Color.YELLOW);
                g.drawLine(cx - r / 2 - 3, cy - r / 2 - 2, cx - 3, cy - r / 3);
                g.drawLine(cx + r / 2 + 3, cy - r / 2 - 2, cx + 3, cy - r / 3);
                break;
            case BOSS:
                g.setColor(body);
                g.fillOval(cx - r, cy - r, 2 * r, 2 * r);
                g.setColor(Color.BLACK);
                g.drawOval(cx - r, cy - r, 2 * r, 2 * r);
                g.setColor(GameConstants.GOLD);
                g.fillPolygon(new int[]{cx - r / 2 - 4, cx - r / 2, cx - r / 4, cx, cx + r / 4, cx + r / 2, cx + r / 2 + 4},
                        new int[]{cy - r + 4, cy - r - 10, cy - r - 2, cy - r - 12, cy - r - 2, cy - r - 10, cy - r + 4}, 7);
                eyes(g, cx, cy, r, Color.RED);
                break;
        }
    }

    private void eyes(Graphics2D g, int cx, int cy, int r, Color c) {
        int e = Math.max(4, r / 3);
        g.setColor(c);
        g.fillOval(cx - r / 2 - e / 2 + 1, cy - r / 4 - e / 2, e, e);
        g.fillOval(cx + r / 2 - e / 2 - 1, cy - r / 4 - e / 2, e, e);
        g.setColor(Color.BLACK);
        g.fillOval(cx - r / 2, cy - r / 4 - 1, 2, 2);
        g.fillOval(cx + r / 2 - 2, cy - r / 4 - 1, 2, 2);
    }
}

// =====================================================================
//  MobType : stats of every mob (normal name / professor name)
//  (no HP drop any more -> base HP can't be recovered)
// =====================================================================
enum MobType {
    //         normal       special          hp   spawnCD speed dmg coin  colour                   radius normalImg      specialImg
    GOBLIN   ("Goblin",    "AJ.Ming",        60,  1.0, 1.0,   5,  10, new Color(90,170,70),   13, "goblin.png",   "ajming.png"),
    WOLF     ("Wolf",      "Konlakorn",      50,  1.0, 2.0,   5,  12, new Color(150,150,160), 13, "wolf.png",     "konlakorn.png"),
    BAT      ("Bat",       "AJ.Tanassanee",  40,  0.8, 1.4,   5,   5, new Color(120,70,150),   9, "bat.png",      "ajtanassanee.png"),
    GOLEM    ("Golem",     "AJ.Tum",        220,  1.2, 0.6,  10,  25, new Color(130,110,90),  17, "golem.png",    "ajtum.png"),
    MINI_BOSS("Mini Boss", "Vasin",        1000,  1.5, 0.5,  50, 100, new Color(200,50,50),   20, "mini_boss.png","ajvasin.png"),
    BOSS     ("Boss",      "AJ.Kob",       2800,  1.5, 0.5, 100, 250, new Color(90,40,120),   24, "boss.png",     "ajkob.png");

    private String normalName, specialName, normalImage, specialImage;
    private int maxHp, baseDamage, coinDrop, radius;
    private double spawnCooldown, speed;
    private Color color;

    MobType(String normalName, String specialName, int maxHp, double spawnCooldown, double speed,
            int baseDamage, int coinDrop, Color color, int radius, String normalImage, String specialImage) {
        this.normalName = normalName; this.specialName = specialName;
        this.maxHp = maxHp; this.spawnCooldown = spawnCooldown; this.speed = speed;
        this.baseDamage = baseDamage; this.coinDrop = coinDrop;
        this.color = color; this.radius = radius;
        this.normalImage = normalImage; this.specialImage = specialImage;
    }

    public String getName(boolean special) { return special ? specialName : normalName; }
    public String getImage(boolean special) { return special ? specialImage : normalImage; }
    public int getMaxHp() { return maxHp; }
    public double getSpawnCooldown() { return spawnCooldown; }
    public double getSpeed() { return speed; }
    public int getBaseDamage() { return baseDamage; }
    public int getCoinDrop() { return coinDrop; }
    public Color getColor() { return color; }
    public int getRadius() { return radius; }
    public boolean isBoss() { return this == MINI_BOSS || this == BOSS; }
}