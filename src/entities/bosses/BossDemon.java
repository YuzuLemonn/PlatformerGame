package entities.bosses;

import entities.BaseBoss;
import entities.Player;
import gamestates.Playing;
import main.Game;
import utilz.HelpMethods;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;

import static utilz.Constants.GRAVITY;
import static utilz.Constants.EnemyConstants.HIT;
import static utilz.Constants.EnemyConstants.DEAD;
import static utilz.Constants.BossConstants.*;
import static utilz.Constants.Directions.*;

public class BossDemon extends BaseBoss {

    private static final float WALK_SPEED   = 0.3f * Game.SCALE; // slow
    private static final int   DETECT_RANGE = 900;
    private static final int   BURN_TICKS   = 3; // 3 instances × 10 dmg

    private boolean attackHitDealt = false;

    public BossDemon(float x, float y, Playing playing) {
        super(x, y, BOSS2_WIDTH, BOSS2_HEIGHT, BOSS_2, playing);
        initHitbox((int)(40 * 0.6f * Game.SCALE), (int)(50 * 0.8f * Game.SCALE));
        loadFrames();
    }

    @Override
    protected void loadFrames() {
        moveFrames   = loadStrip("sprites/Boss2/Boss2_Move.png",     5);
        attackFrames = loadStrip("sprites/Boss2/Boss2_Attack.png", 5);
        hitFrames    = loadStrip("sprites/Boss2/Boss2_Hit.png",      3);
        deadFrames   = loadStrip("sprites/Boss2/Boss2_Dead.png",     7);
    }

    @Override
    protected void updateAI(int[][] lvlData, Player player) {
        if (state == BOSS_HIT || state == BOSS_ATTACKED)
            return;

        if (firstUpdate) {
            if (!HelpMethods.IsEntityOnFloor(hitbox, lvlData))
                inAir = true;
            firstUpdate = false;
        }

        if (inAir) {
            if (HelpMethods.CanMoveHere(hitbox.x, hitbox.y + airSpeed, hitbox.width, hitbox.height, lvlData)) {
                hitbox.y += airSpeed;
                airSpeed += GRAVITY;
            } else {
                while (!HelpMethods.CanMoveHere(hitbox.x, hitbox.y + 1, hitbox.width, hitbox.height, lvlData))
                    hitbox.y--;
                inAir = false;
                airSpeed = 0;
            }
            return;
        }

        float speed = (phase == 2) ? WALK_SPEED * 1.5f : WALK_SPEED;
        float dx = player.getHitbox().x - hitbox.x;
        float dy = player.getHitbox().y - hitbox.y;

        turnTowardsPlayer(player);

        float move = (walkDir == RIGHT) ? speed : -speed;

        if (HelpMethods.CanMoveHere(hitbox.x + move, hitbox.y, hitbox.width, hitbox.height, lvlData))
            hitbox.x += move;
        else
            walkDir = (walkDir == RIGHT) ? LEFT : RIGHT;

        boolean playerClose = Math.abs(dx) < hitbox.width && Math.abs(dy) < hitbox.height;
        if (playerClose && attackCooldown <= 0) {
            doAttack(player);
            attackCooldown = getAttackCooldown();
        }
    }

    @Override
    protected void checkPhaseTransition() {
        // no phase 2
    }

    @Override
    protected void doAttack(Player player) {
        if (state == BOSS_ATTACKED) return; 
        state = BOSS_ATTACKED;
        aniIndex = 0;
        attackHitDealt = false;
    }

    @Override
    protected void onAnimationComplete() {
        if (state == BOSS_ATTACKED) {
            state = BOSS_MOVE;
            aniIndex = 0;
            attackHitDealt = false;
        }
        if (state == BOSS_HIT || state == HIT) {
            state = BOSS_MOVE;
            aniIndex = 0;
        }
    }

    @Override
    protected void updateAnimationTick() {
        super.updateAnimationTick();
        if (state == BOSS_ATTACKED && aniIndex == 4 && !attackHitDealt) {
            attackHitDealt = true;
            Player player = playing.getPlayer();
            if (hitbox.intersects(player.getHitbox())) {
                int dmg = GetEnemyDmgBoss(BOSS_2); // 20
                if (phase == 2) dmg = (int)(dmg * 1.5f);
                player.changeHealth(-dmg);
                player.applyBurn(BURN_TICKS);
            }
        }
    }

    @Override
    public void hurt(int amount) {
        currentHealth -= amount;
        if (currentHealth <= 0) {
            currentHealth = 0;
            state = BOSS_DEAD;
            active = false;
        } else {
            state = BOSS_HIT;
            aniIndex = 0;  
            aniTick = 0;    
        }
    }

    @Override
    public void hurt(int amount, int playerDir) {
        hurt(amount); // no knockback
    }

    @Override
    public void draw(Graphics g, int lvlOffset) {
        BufferedImage[] frames = getCurrentFrames();
        if (frames == null || aniIndex >= frames.length || frames[aniIndex] == null) return;

        int flipX = (walkDir == RIGHT) ? BOSS2_WIDTH : 0;
        int flipW = (walkDir == RIGHT) ? -1 : 1;

        g.drawImage(frames[aniIndex],
            (int)(hitbox.x) - lvlOffset - BOSS2_DRAWOFFSET_X + flipX,
            (int)(hitbox.y) - BOSS2_DRAWOFFSET_Y,
            BOSS2_WIDTH * flipW, BOSS2_HEIGHT, null);
    }

    @Override protected int getAttackCooldown() { 
        return 120; 
    }
    @Override protected int getPhase2Cooldown() { 
        return 80; 
    }
    @Override protected String getBossName() { 
        return "INFERNO BEAST"; 
    }

    protected float getSpawnX(levels.Level level) {
        return level.getLevelData()[0].length / 2f * Game.TILES_SIZE;
    }

    protected float getSpawnY() {
        return (Game.TILES_IN_HEIGHT - 2) * Game.TILES_SIZE - (70 * Game.SCALE);
    }

    @Override
    public void resetEnemy() {
        super.resetEnemy();
        phase = 1;
        phaseTransitioned = false;
        attackCooldown = 0;
        aniTick = 0;
        aniIndex = 0;
        inAir = false;
        airSpeed = 0;
        walkDir = RIGHT;
        firstUpdate = true;
        attackHitDealt = false;
        state = BOSS_MOVE;
        applySpawn(null);
    }

    @Override
    public void applySpawn(Point spawnPoint) {
        hitbox.x = 500 * Game.SCALE;
        hitbox.y = 5 * Game.TILES_SIZE;
    }
}