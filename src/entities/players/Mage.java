package entities.players;

import entities.Player;
import entities.Projectile;
import gamestates.Playing;
import main.Game;
import utilz.LoadSave;
import java.awt.image.BufferedImage;
import static utilz.Constants.PlayerConstants.*;
import static utilz.Constants.Directions.*;

public class Mage extends Player {

    public Mage(float x, float y, int width, int height, Playing playing) {
        super(x, y, width, height, playing);
        this.maxHealth = 70; // squishier
        this.currentHealth = maxHealth;
        this.walkSpeed = Game.SCALE * 1.1f; // slightly faster
        loadAnimations();
        initHitbox(20, 27);
        initAttackBox();
    }

    @Override
    protected void loadAnimations() {
        idleFrames   = loadFrames("IdleAni_Mage.png",  GetSpriteAmount(IDLE, "Mage"));
        runFrames    = loadFrames("RunAni_Mage.png",   GetSpriteAmount(RUNNING, "Mage"));
        jumpFrames   = loadFrames("JumpAni_Mage.png",  GetSpriteAmount(JUMP, "Mage"));
        attackFrames = loadFrames("Attack1_Mage.png",  GetSpriteAmount(ATTACK, "Mage"));
        statusBarImg = LoadSave.GetSpriteAtlas(LoadSave.STATUS_BAR);
    }

    private BufferedImage[] loadFrames(String fileName, int frameCount) {
        BufferedImage sheet = LoadSave.GetSpriteAtlas("sprites/Mage/" + fileName);
        if (sheet == null) return new BufferedImage[frameCount];
        int fw = (int) Math.floor((double) sheet.getWidth() / frameCount);
        int fh = sheet.getHeight();
        BufferedImage[] frames = new BufferedImage[frameCount];
        for (int i = 0; i < frameCount; i++)
            frames[i] = sheet.getSubimage(i * fw, 0, fw, fh);
        return frames;
    }

    @Override
    protected String getCharacterName() { return "Mage"; }

    @Override
    protected boolean isProjectileAttack() { return true; }

    @Override
    protected void spawnProjectile() {
        int dir = (flipW == 1) ? 1 : -1;
        float spawnX = dir == 1
                ? hitbox.x + hitbox.width
                : hitbox.x - (int)(20 * Game.SCALE);
        float spawnY = hitbox.y + hitbox.height / 4f;
        projectiles.add(new Projectile(spawnX, spawnY, dir, playing));
    }
}