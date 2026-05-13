package entities.players;

import entities.Player;
import entities.Projectile;
import gamestates.Playing;
import main.Game;
import utilz.LoadSave;
import java.awt.image.BufferedImage;
import static utilz.Constants.PlayerConstants.*;
import static utilz.Constants.Directions.*;
import static utilz.Constants.DamageConstants.*;

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
        skill2Frames = loadFrames("Attack2_Mage.png", GetSpriteAmount(SKILL2, "Mage"));
        skill3Frames = loadFrames("Attack3_Mage.png", GetSpriteAmount(SKILL3, "Mage"));
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
        if (!useStamina(STAMINA_COST_ATTACK)) return;
        int dir = (flipW == 1) ? 1 : -1;
        float spawnX = dir == 1
                ? hitbox.x + hitbox.width
                : hitbox.x - (int)(20 * Game.SCALE);
        float spawnY = hitbox.y + hitbox.height / 4f;
        projectiles.add(new Projectile(spawnX, spawnY, dir, playing,
        "sprites/Mage/Attack1Projectile_Mage.png", 4, MAGE_ATTACK_DMG));
        playing.getGame().getAudioPlayer().playAttackSound();
    }

    @Override
    protected void useSkill2() {
        //if (!useStamina(STAMINA_COST_SKILL2)) return;
        changeHealth(20);  // adjust heal amount as needed
    }

    @Override
    protected void useSkill3() {
        //if (!useStamina(STAMINA_COST_SKILL3)) return;
        int dir = (flipW == 1) ? 1 : -1;
        float projX = (dir == 1)
                ? hitbox.x + hitbox.width
                : hitbox.x - (16 * Game.SCALE);
        float projY = hitbox.y + hitbox.height / 2 - (4 * Game.SCALE);
        projectiles.add(new Projectile(projX, projY, dir, playing,
        "sprites/Mage/Attack3Projectile_Mage.png", 3, MAGE_SKILL3_DMG));
        playing.getGame().getAudioPlayer().playAttackSound();
    }

    @Override
    protected int getAttackHitFrame() { 
        return 4; 
    }

    @Override
    protected int getSkill2HitFrame() { 
        return 2; 
    }

    @Override
    protected int getSkill3HitFrame() { 
        return 7; 
    }

    @Override
    protected int getAttackStaminaCost() {
        return STAMINA_COST_ATTACK;
    }

    @Override
    protected int getSkill2StaminaCost() {
        return STAMINA_COST_SKILL2;
    }

    @Override
    protected int getSkill3StaminaCost() {
        return STAMINA_COST_SKILL3;
    }
}