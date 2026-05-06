package entities.players;

import entities.Player;
import entities.Projectile;
import gamestates.Playing;
import main.Game;
import utilz.LoadSave;
import java.awt.image.BufferedImage;
import static utilz.Constants.PlayerConstants.*;
import static utilz.Constants.DamageConstants.*;

public class Assassin extends Player {

    public Assassin(float x, float y, int width, int height, Playing playing) {
        super(x, y, width, height, playing);
        this.maxHealth = 80;       // less health than brawler
        this.currentHealth = maxHealth;
        this.walkSpeed = Game.SCALE * 1.3f; // fastest of the three
        loadAnimations();
        initHitbox(20, 27);
        initAttackBox();
    }

    @Override
    protected void loadAnimations() {
        idleFrames   = loadFrames("IdleAni_Assassin.png",  GetSpriteAmount(IDLE, "Assassin"));
        runFrames    = loadFrames("RunAni_Assassin.png",   GetSpriteAmount(RUNNING, "Assassin"));
        jumpFrames   = loadFrames("JumpAni_Assassin.png",  GetSpriteAmount(JUMP, "Assassin"));
        attackFrames = loadFrames("Attack1_Assassin.png",  GetSpriteAmount(ATTACK, "Assassin"));
        skill2Frames = loadFrames("Attack2_Assassin.png",  GetSpriteAmount(SKILL2,  "Assassin"));
        skill3Frames = loadFrames("Attack3_Assassin.png",  GetSpriteAmount(SKILL3,  "Assassin"));
        statusBarImg = LoadSave.GetSpriteAtlas(LoadSave.STATUS_BAR);
    }

    private BufferedImage[] loadFrames(String fileName, int frameCount) {
        BufferedImage sheet = LoadSave.GetSpriteAtlas("sprites/Assassin/" + fileName);
        if (sheet == null) return new BufferedImage[frameCount];
        int fw = (int) Math.floor((double) sheet.getWidth() / frameCount);
        int fh = sheet.getHeight();
        BufferedImage[] frames = new BufferedImage[frameCount];
        for (int i = 0; i < frameCount; i++)
            frames[i] = sheet.getSubimage(i * fw, 0, fw, fh);
        return frames;
    }

    @Override
    protected String getCharacterName() { return "Assassin"; }

    @Override
    protected boolean isProjectileAttack() { return false; } // melee

    @Override
    protected void spawnProjectile() {} // melee, no projectile

    @Override
    protected void useSkill2() {
        if (!useStamina(STAMINA_COST_SKILL2)) return;
        int dir = (flipW == 1) ? 1 : -1;
        float projX = (dir == 1)
                ? hitbox.x + hitbox.width
                : hitbox.x - (16 * Game.SCALE);
        float projY = hitbox.y + hitbox.height / 2 - (4 * Game.SCALE);
        projectiles.add(new Projectile(projX, projY, dir, playing,
        "sprites/Assassin/Attack2_Projectile_Assassin.png", 8, ASSASSIN_SKILL2_DMG));
        playing.getGame().getAudioPlayer().playAttackSound();
    }

    @Override
    protected void useSkill3() {
        if (!useStamina(STAMINA_COST_SKILL3)) return;
        playing.checkEnemyHit(attackBox);
        playing.getGame().getAudioPlayer().playAttackSound();
    }

    @Override
    protected int getAttackHitFrame() { 
        return 5; 
    }

    @Override
    protected int getSkill2HitFrame() { 
        return 3; 
    }

    @Override
    protected int getSkill3HitFrame() { 
        return 7; 
    }
}