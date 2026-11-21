package JavaProject3_TowerDefense;
import java.awt.*;
import java.util.ArrayList;
import java.awt.image.BufferedImage;

public class Effect {
    private static final int HIT = 0, EXPLOSION = 1, ICE = 2, FIRE = 3, DEATH = 4, TEXT = 5, BANNER = 6;
    private TowerType type;
    private int kind;
    private double x, y, life, maxLife, radius = 16;
    private String text = "";
    private Color color = Color.WHITE;

    private Effect(int kind, double x, double y, double life) {
        this.kind = kind; 
        this.x = x; 
        this.y = y; 
        this.life = life; 
        this.maxLife = life;
    }
   
    public static Effect explosion(double x, double y, double radius, TowerType type) {
        Effect e = new Effect(EXPLOSION, x, y, 0.65); 
        e.radius = radius; 
        e.type = type; 
        return e;
    }
    public static Effect iceBurst(double x, double y, TowerType type) { 
        Effect e = new Effect(ICE, x, y, 0.45); 
        e.type = type; 
        return e;
    }
    public static Effect fireBurst(double x, double y, TowerType type) { 
        Effect e = new Effect(FIRE, x, y, 0.45); 
        e.type = type; 
        return e;
    }
    public static Effect death(double x, double y, Color c) {
        Effect e = new Effect(DEATH, x, y, 0.45); 
        e.color = c; 
        return e;
    }
    public static Effect text(double x, double y, String t, Color c) {
        Effect e = new Effect(TEXT, x, y, 1.1); 
        e.text = t; e.color = c; 
        return e;
    }
    public static Effect banner(double x, double y, String t, Color c) {
        Effect e = new Effect(BANNER, x, y, 2.2); 
        e.text = t; 
        e.color = c; 
        return e;
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
            case HIT:
            case EXPLOSION:
            case ICE:
            case FIRE:
                drawImpact(g, ix, iy, a, p);
                break;
            case DEATH: {
                drawDeath(g, ix, iy, a, p);
                break;
            }
            case TEXT:
                drawCenteredText(g, ix, iy, 15, a);
                break;
            case BANNER:
                drawCenteredText(g, ix, iy, 40, Math.min(1, life / 0.5));
                break;
                /*
                case UPGRADE: {
                    drawTowerUpgrade(g, ix, iy, 0.5, p);
                    break;
                }
                */
        }
        g.setStroke(old);
    }

    private void drawImpact(Graphics2D g, int ix, int iy, double a, double p) {
        BufferedImage impact = GameConstants.getImage(type.name().toLowerCase() + "_effect.png");
        if (impact != null) {
            int size = (int) (30 + 20 * p);
            Composite oldComposite = g.getComposite();
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,0.7f));
            g.drawImage(impact, ix - size / 2, iy - size / 2, size, size, null);
            g.setComposite(oldComposite);
            //System.out.println("Drawing impact effect for available image");
        } /*else{
            System.out.println("Drawing impact effect for missing image");
            int r = (int) (radius * (0.3 + 0.7 * p));
            g.setColor(fade(new Color(255, 140, 30), a * 0.6));       
            g.fillOval(ix - r, iy - r, 2 * r, 2 * r);
            int r2 = r / 2;
            g.setColor(fade(new Color(255, 240, 120), a));
            g.fillOval(ix - r2, iy - r2, 2 * r2, 2 * r2);
        }*/
    }
    private void drawDeath(Graphics2D g,int ix,int iy,double a,double p) {
        BufferedImage death = GameConstants.getImage("death_effect.png");
        if (death != null) {
            int size = (int) (20 + 15 * p);
            Composite old = g.getComposite();
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,(float) a));
            g.drawImage(death,ix - size / 2,iy - size / 2,size,size,null);
            g.setComposite(old);
        }
    }

    private void drawCenteredText(Graphics2D g, int cx, int cy, int size, double a) {
        g.setFont(GameConstants.GAME_FONT.deriveFont(Font.BOLD, (float) size));
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
