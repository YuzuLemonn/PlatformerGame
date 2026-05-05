package entities.players;

import entities.Player;
import gamestates.Playing;
import main.Game;
import utilz.LoadSave;
import java.awt.image.BufferedImage;
import static utilz.Constants.PlayerConstants.*;

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
}