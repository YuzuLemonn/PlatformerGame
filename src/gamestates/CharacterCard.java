package gamestates;

import main.Game;
import utilz.LoadSave;
import java.awt.*;
import java.awt.image.BufferedImage;
import static utilz.Constants.CharacterStats;

public class CharacterCard {
    private String name;
    private Rectangle bounds;
    private BufferedImage[] idleFrames;
    private int aniTick, aniIndex, frameCount;
    private CharacterStats stats;

    public CharacterCard(String name, int x, int y, int w, int h,
                         String spritePath, int frameCount, CharacterStats stats) {
        this.name = name;
        this.bounds = new Rectangle(x, y, w, h);
        this.frameCount = frameCount;
        this.stats = stats;
        loadIdle(spritePath, frameCount);
    }

    private void loadIdle(String path, int frameCount) {
        BufferedImage sheet = LoadSave.GetSpriteAtlas(path);
        if (sheet == null) return;
        int fw = sheet.getWidth() / frameCount;
        int fh = sheet.getHeight();
        idleFrames = new BufferedImage[frameCount];
        for (int i = 0; i < frameCount; i++)
            idleFrames[i] = sheet.getSubimage(i * fw, 0, fw, fh);
    }

    public void update() {
        aniTick++;
        if (aniTick >= 20) {
            aniTick = 0;
            aniIndex = (aniIndex + 1) % frameCount;
        }
    }

    public void draw(Graphics g, boolean hovered) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(hovered ? new Color(255, 255, 255, 60) : new Color(0, 0, 0, 100));
        g2d.fillRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 20, 20);
        g2d.setStroke(new BasicStroke(2));
        g2d.setColor(hovered ? Color.YELLOW : Color.WHITE);
        g2d.drawRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 20, 20);

        // sprite
        if (idleFrames != null && aniIndex < idleFrames.length) {
            int size = (int)(bounds.width * 0.7);
            int sx = bounds.x + bounds.width/2 - size/2;
            int sy = bounds.y + (int)(bounds.height * 0.1);
            g.drawImage(idleFrames[aniIndex], sx, sy, size, size, null);
        }

        // name
        g.setColor(hovered ? Color.YELLOW : Color.WHITE);
        g.setFont(new Font("Monospaced", Font.BOLD, (int)(7 * Game.SCALE)));
        FontMetrics fm = g.getFontMetrics();
        g.drawString(name, bounds.x + bounds.width/2 - fm.stringWidth(name)/2,
                bounds.y + bounds.height - (int)(8 * Game.SCALE));

        // stats — only show on hover
        if (hovered && stats != null) {
            drawStats(g);
        }
    }

    private void drawStats(Graphics g) {
        int panelW = (int)(160 * Game.SCALE);
        int panelH = (int)(120 * Game.SCALE);
        int panelX = bounds.x + bounds.width/2 - panelW/2;
        int panelY = bounds.y + bounds.height - (int)(8 * Game.SCALE);

        // keep panel on screen
        if (panelX + panelW > Game.GAME_WIDTH)
            panelX = bounds.x - panelW - (int)(8 * Game.SCALE);

        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(new Color(0, 0, 0, 180));
        g2d.fillRoundRect(panelX, panelY, panelW, panelH, 12, 12);
        g2d.setColor(Color.YELLOW);
        g2d.setStroke(new BasicStroke(1));
        g2d.drawRoundRect(panelX, panelY, panelW, panelH, 12, 12);

        g.setFont(new Font("Monospaced", Font.BOLD, (int)(10 * Game.SCALE)));
        int lineH = (int)(14 * Game.SCALE);
        int tx = panelX + (int)(8 * Game.SCALE);
        int ty = panelY + lineH;

        // full HP bar + label
        drawStatRow(g, tx, ty, panelW, "HP", stats.hp, stats.hp, new Color(220, 50, 50)); ty += lineH;
        // full stamina bar + label  
        drawStatRow(g, tx, ty, panelW, "STAMINA", stats.stamina, stats.stamina, new Color(255, 200, 0)); ty += lineH;

        // divider
        g.setColor(new Color(255, 255, 255, 60));
        g.fillRect(tx, ty, panelW - (int)(16 * Game.SCALE), 1);
        ty += (int)(6 * Game.SCALE);

        // skill damages
        drawDmgRow(g, tx, ty, stats.skill1Name, stats.skill1Dmg, new Color(100, 180, 255)); ty += lineH;
        drawDmgRow(g, tx, ty, stats.skill2Name, stats.skill2Dmg, new Color(100, 255, 150)); ty += lineH;
        drawDmgRow(g, tx, ty, stats.skill3Name, stats.skill3Dmg, new Color(255, 120, 50));
    }

    private void drawStatRow(Graphics g, int x, int y, int panelW,
                          String label, int val, int max, Color barColor) {
        g.setColor(Color.WHITE);
        g.drawString(label, x, y);

        int barX = x + (int)(55 * Game.SCALE);
        int barW = panelW - (int)(85 * Game.SCALE); // slightly shorter to fit number
        int barH = (int)(6 * Game.SCALE);
        int barY = y - barH + 1;

        // background
        g.setColor(new Color(30, 30, 30));
        g.fillRect(barX, barY, barW, barH);

        // always full bar
        g.setColor(barColor);
        g.fillRect(barX, barY, barW, barH);

        // highlight
        g.setColor(new Color(255, 255, 255, 60));
        g.fillRect(barX, barY, barW, barH / 3);

        // value number shown after bar
        g.setColor(Color.WHITE);
        g.drawString(String.valueOf(val), barX + barW + (int)(4 * Game.SCALE), y);
    }

    private void drawDmgRow(Graphics g, int x, int y,
                             String skillName, int dmg, Color color) {
        g.setColor(color);
        g.drawString(skillName + ":", x, y);
        g.setColor(Color.WHITE);
        g.drawString(dmg + " dmg", x + (int)(70 * Game.SCALE), y);
    }

    public Rectangle getBounds() { return bounds; }
    public String getName() { return name; }
}