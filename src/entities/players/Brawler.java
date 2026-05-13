package entities.players;

import entities.Player;
import entities.Projectile;
import gamestates.Playing;
import main.Game;
import utilz.LoadSave;
import java.awt.image.BufferedImage;
import static utilz.Constants.DamageConstants.*;
import static utilz.Constants.PlayerConstants.*;

public class Brawler extends Player {

    private boolean skill3SecondShotFired = false;

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
    public void update() {
        super.update();

        if (skill3 && state == SKILL3) {
            if (aniIndex == 12 && !skill3SecondShotFired) {
                skill3SecondShotFired = true;
                spawnSkill3Projectile();
            }
        } else {
            skill3SecondShotFired = false;
        }
    }

    private void spawnSkill3Projectile() {
        int dir = (flipW == 1) ? 1 : -1;
        float projX = (dir == 1)
                ? hitbox.x + hitbox.width
                : hitbox.x - (16 * Game.SCALE);
        float projY = hitbox.y + hitbox.height / 2 - (4 * Game.SCALE);
        projectiles.add(new Projectile(projX, projY, dir, playing,
                "sprites/Brawler/Attack3_Projectile_Brawler.png", 4, BRAWLER_SKILL3_DMG));
        playing.getGame().getAudioPlayer().playAttackSound();
    }

    @Override
    protected String getCharacterName() { return "Brawler"; }

    @Override
    protected boolean isProjectileAttack() { return false; }

    @Override
    protected void spawnProjectile() {}

    @Override
    protected void useSkill2() {
        playing.checkEnemyHit(attackBox, BRAWLER_SKILL2_DMG);
        playing.getGame().getAudioPlayer().playAttackSound();
    }

    @Override
    protected void useSkill3() {
        skill3SecondShotFired = false;
        spawnSkill3Projectile();
    }

    @Override protected int getAttackHitFrame()  { return 2; }
    @Override protected int getSkill2HitFrame()  { return 3; }
    @Override protected int getSkill3HitFrame()  { return 8; }

    @Override protected int getAttackStaminaCost() { return STAMINA_COST_ATTACK; }
    @Override protected int getSkill2StaminaCost() { return STAMINA_COST_SKILL2; }
    @Override protected int getSkill3StaminaCost() { return STAMINA_COST_SKILL3; }
}