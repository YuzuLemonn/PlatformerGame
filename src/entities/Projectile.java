package entities;

import gamestates.Playing;
import main.Game;
import utilz.LoadSave;
import static utilz.HelpMethods.CanMoveHere;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

public class Projectile {
    private Rectangle2D.Float hitbox;
    private float speed = 2.0f * Game.SCALE;
    private int dir;
    private boolean active = true;
    private int damage = 15;
    private Playing playing;

    private BufferedImage[] frames;
    private int aniTick, aniIndex;

    public Projectile(float x, float y, int dir, Playing playing) {
        this.dir = dir;
        this.playing = playing;
        hitbox = new Rectangle2D.Float(x, y,
                (int)(16 * Game.SCALE),
                (int)(8  * Game.SCALE));
        loadFrames();
    }

    private void loadFrames() {
        BufferedImage sheet = LoadSave.GetSpriteAtlas("sprites/Mage/Attack1Projectile_Mage.png");
        if (sheet == null) return;
        int count = 4; // adjust to your sprite frame count
        int fw = sheet.getWidth() / count;
        frames = new BufferedImage[count];
        for (int i = 0; i < count; i++)
            frames[i] = sheet.getSubimage(i * fw, 0, fw, sheet.getHeight());
    }

    public void update(int[][] lvlData) {
        if (!active) return;
        float newX = hitbox.x + speed * dir;
        if (CanMoveHere(newX, hitbox.y, hitbox.width, hitbox.height, lvlData)) {
            hitbox.x = newX;
        } else {
            active = false;
        }
        // animate
        aniTick++;
        if (aniTick >= 8) {
            aniTick = 0;
            if (frames != null)
                aniIndex = (aniIndex + 1) % frames.length;
        }
    }

    public void render(Graphics g, int lvlOffset) {
        if (!active || frames == null || aniIndex >= frames.length) return;
        int drawX = (int)(hitbox.x - lvlOffset);
        int drawW = (int)(hitbox.width * 2) * dir; // flip with dir
        if (dir == -1) drawX += (int)(hitbox.width * 2);
        g.drawImage(frames[aniIndex], drawX, (int)hitbox.y,
                drawW, (int)(hitbox.height * 2), null);
    }

    public Rectangle2D.Float getHitbox() { return hitbox; }
    public boolean isActive() { return active; }
    public void setActive(boolean b) { active = b; }
    public int getDamage() { return damage; }

    public int getDir() { return dir; }
}