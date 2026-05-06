package entities.players;

import entities.Player;
import gamestates.Playing;
import main.Game;
import utilz.LoadSave;
import java.awt.image.BufferedImage;
import static utilz.Constants.DamageConstants.*;
import static utilz.Constants.PlayerConstants.*;

public class Brawler extends Player {

    public Brawler(float x, float y, int width, int height, Playing playing) {
        super(x, y, width, height, playing);
        this.maxHealth = 100;
        this.currentHealth = maxHealth;
        this.walkSpeed = Game.SCALE * 1.0f;
        loadAnimations();
        initHitbox(20, 27);
        initAttackBox();
    }

    @Override
    protected void loadAnimations() {
        idleFrames   = loadActionFrames("IdleAni",  GetSpriteAmount(IDLE, "Brawler"));
        runFrames    = loadActionFrames("RunAni",    GetSpriteAmount(RUNNING, "Brawler"));
        jumpFrames   = loadActionFrames("JumpAni",  GetSpriteAmount(JUMP, "Brawler"));
        attackFrames = loadActionFrames("Attack1",  GetSpriteAmount(ATTACK, "Brawler"));
        skill2Frames = loadActionFrames("Attack2",  GetSpriteAmount(SKILL2, "Brawler"));
        skill3Frames = loadActionFrames("Attack3",  GetSpriteAmount(SKILL3, "Brawler"));
        statusBarImg = LoadSave.GetSpriteAtlas(LoadSave.STATUS_BAR);
    }

    private BufferedImage[] loadActionFrames(String action, int frameCount) {
        BufferedImage sheet = LoadSave.GetBrawlerSprite(action);
        if (sheet == null) return new BufferedImage[frameCount];
        int fw = sheet.getWidth() / frameCount;
        int fh = sheet.getHeight();
        BufferedImage[] frames = new BufferedImage[frameCount];
        for (int i = 0; i < frameCount; i++)
            frames[i] = sheet.getSubimage(i * fw, 0, fw, fh);
        return frames;
    }

    @Override
    protected String getCharacterName() { return "Brawler"; }

    @Override
    protected boolean isProjectileAttack() { return false; }

    @Override
    protected void spawnProjectile() {} // melee, no projectile

    @Override
    protected void useSkill2() {
        if (!useStamina(STAMINA_COST_SKILL2)) return;
        playing.checkEnemyHit(attackBox, BRAWLER_SKILL2_DMG);
        playing.getGame().getAudioPlayer().playAttackSound();
    }

    @Override
    protected void useSkill3() {
        if (!useStamina(STAMINA_COST_SKILL3)) return;
        playing.checkEnemyHit(attackBox, BRAWLER_SKILL3_DMG);
        playing.getGame().getAudioPlayer().playAttackSound();
    }

    @Override
    protected int getAttackHitFrame() { 
        return 2; 
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