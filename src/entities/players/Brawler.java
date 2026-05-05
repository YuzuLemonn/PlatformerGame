package entities.players;

import entities.Player;
import gamestates.Playing;
import main.Game;
import utilz.LoadSave;
import java.awt.image.BufferedImage;
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
        idleFrames   = loadActionFrames("IdleAni",  GetSpriteAmount(IDLE));
        runFrames    = loadActionFrames("RunAni",    GetSpriteAmount(RUNNING));
        jumpFrames   = loadActionFrames("JumpAni",  GetSpriteAmount(JUMP));
        attackFrames = loadActionFrames("Attack1",  GetSpriteAmount(ATTACK));
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
}