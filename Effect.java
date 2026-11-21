// 6813115 Renuka Choosingkae
// 6813238 Napat Chonsaktrakul
// 6813248 Pimpichcha Saengngoen
// 6813258 Sittichok Boonthittanont
package JavaProject3_TowerDefense;

import java.awt.*;
import java.awt.image.BufferedImage;

// =====================================================================
//  Effect : short animations (hit impact, death, floating text, banner)
// =====================================================================
public class Effect {
    private static final int IMPACT = 0, DEATH = 1, TEXT = 2, BANNER = 3;
    private static final double IMPACT_LIFE = 0.5, DEATH_LIFE = 0.45, TEXT_LIFE = 1.1, BANNER_LIFE = 2.2;
    private static final double TEXT_RISE_SPEED = 30, BANNER_FADE_TIME = 0.5;
    private static final float IMPACT_ALPHA = 0.7f;

    private final int kind;
    private final double maxLife;
    private double x, y, life;
    private TowerType tower;                 // only for IMPACT
    private String text = "";
    private Color color = Color.LIGHT_GRAY;

    private Effect(int kind, double x, double y, double life) {
        this.kind = kind;
        this.x = x;
        this.y = y;
        this.life = life;
        this.maxLife = life;
    }

    // ================= factory methods =================
    public static Effect impact(double x, double y, TowerType tower) {
        Effect e = new Effect(IMPACT, x, y, IMPACT_LIFE);
        e.tower = tower;
        e.color = tower.getColor();
        return e;
    }

    public static Effect death(double x, double y) {
        return new Effect(DEATH, x, y, DEATH_LIFE);
    }

    public static Effect text(double x, double y, String t, Color c) {
        Effect e = new Effect(TEXT, x, y, TEXT_LIFE);
        e.text = t;
        e.color = c;
        return e;
    }

    public static Effect banner(double x, double y, String t, Color c) {
        Effect e = new Effect(BANNER, x, y, BANNER_LIFE);
        e.text = t;
        e.color = c;
        return e;
    }

    // ================= update / draw =================
    public void update(double dt) {
        life -= dt;
        if (kind == TEXT) y -= TEXT_RISE_SPEED * dt;
    }

    public boolean isDone() { return life <= 0; }

    public void draw(Graphics2D g) {
        double a = Math.max(0, Math.min(1, life / maxLife));   // 1 -> 0 while fading
        double p = 1 - a;                                       // 0 -> 1 while growing
        int ix = (int) x, iy = (int) y;
        switch (kind) {
            case IMPACT:
                drawPicture(g, tower.getKey() + "_effect.png", ix, iy, (int) (30 + 20 * p), IMPACT_ALPHA);
                break;
            case DEATH:
                drawPicture(g, "death_effect.png", ix, iy, (int) (20 + 15 * p), (float) a);
                break;
            case TEXT:
                drawCenteredText(g, ix, iy, GameConstants.POPUP_FONT, a);
                break;
            case BANNER:
                drawCenteredText(g, ix, iy, GameConstants.BANNER_FONT, Math.min(1, life / BANNER_FADE_TIME));
                break;
        }
    }

    // PNG if it exists, otherwise a simple fading circle
    private void drawPicture(Graphics2D g, String file, int cx, int cy, int size, float alpha) {
        BufferedImage img = GameConstants.getImage(file);
        if (img != null) {
            GameConstants.drawFaded(g, img, cx - size / 2, cy - size / 2, size, size, alpha);
        } else {
            g.setColor(fade(color, alpha));
            g.fillOval(cx - size / 2, cy - size / 2, size, size);
        }
    }

    private void drawCenteredText(Graphics2D g, int cx, int cy, Font font, double a) {
        g.setFont(font);
        FontMetrics fm = g.getFontMetrics();
        int tx = cx - fm.stringWidth(text) / 2;
        int ty = cy + fm.getAscent() / 2 - 2;
        g.setColor(fade(Color.BLACK, a));
        g.drawString(text, tx + 2, ty + 2);          // shadow
        g.setColor(fade(color, a));
        g.drawString(text, tx, ty);
    }

    private static Color fade(Color c, double a) {
        int alpha = (int) (255 * Math.max(0, Math.min(1, a)));
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha);
    }
}