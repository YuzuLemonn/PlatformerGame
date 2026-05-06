package entities.bosses;

import entities.BaseBoss;
import entities.Player;
import gamestates.Playing;
import main.Game;
import static utilz.Constants.BossConstants.*;

public class BossWorm extends BaseBoss {

    private float baseSpeed = 2.5f * Game.SCALE; // fast

    public BossWorm(float x, float y, Playing playing) {
        super(x, y,
            (int)(48 * Game.SCALE),
            (int)(32 * Game.SCALE),
            BOSS_1, playing);
        initHitbox(36, 20);
        loadFrames();
    }

    @Override
    protected void loadFrames() {
        int bossNum = 1;
        moveFrames   = loadStrip("sprites/Boss/Boss1_Move.png",     8);
        attackFrames = loadStrip("sprites/Boss/Boss1_Attacked.png", 6);
        hitFrames    = loadStrip("sprites/Boss/Boss1_Hit.png",      4);
        deadFrames   = loadStrip("sprites/Boss/Boss1_Dead.png",     8);
    }

    @Override
    protected void updateAI(int[][] lvlData, Player player) {
        // worm ignores tiles — just move directly toward player
        float speed = (phase == 2) ? baseSpeed * 1.5f : baseSpeed;
        walkToward(player, speed);

        // also move vertically toward player (burrows through tiles)
        float dirY = (player.getHitbox().y > hitbox.y) ? 1 : -1;
        hitbox.y += dirY * speed * 0.5f;
    }

    @Override
    protected void doAttack(Player player) {
        // slam — deal damage if touching player
        if (hitbox.intersects(player.getHitbox()))
            player.changeHealth(-GetEnemyDmgBoss(BOSS_1));
    }

    @Override
    protected int getAttackCooldown() { return 80; }

    @Override
    protected int getPhase2Cooldown() { return 40; } // faster in phase 2

    @Override
    protected String getBossName() { return "CAVE WORM"; }
}