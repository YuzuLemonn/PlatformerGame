package gamestates;

import main.Game;
import utilz.LoadSave;
import java.awt.*;
import java.awt.image.BufferedImage;

public class CharacterCard {
    private String name;
    private Rectangle bounds;
    private BufferedImage[] idleFrames;
    private int aniTick, aniIndex, frameCount;

    public CharacterCard(String name, int x, int y, int w, int h,
                         String spritePath, int frameCount) {
        this.name = name;
        this.bounds = new Rectangle(x, y, w, h);
        this.frameCount = frameCount;
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

        if (idleFrames != null && aniIndex < idleFrames.length) {
            int size = (int)(bounds.width * 0.7);
            int sx = bounds.x + bounds.width/2 - size/2;
            int sy = bounds.y + (int)(bounds.height * 0.1);
            g.drawImage(idleFrames[aniIndex], sx, sy, size, size, null);
        }

        g.setColor(hovered ? Color.YELLOW : Color.WHITE);
        g.setFont(new Font("Monospaced", Font.BOLD, (int)(7 * Game.SCALE)));
        FontMetrics fm = g.getFontMetrics();
        g.drawString(name, bounds.x + bounds.width/2 - fm.stringWidth(name)/2,
                bounds.y + bounds.height - (int)(8 * Game.SCALE));
    }

    public Rectangle getBounds() { return bounds; }
    public String getName() { return name; }
}