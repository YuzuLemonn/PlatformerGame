package entities.bosses;

import entities.BaseBoss;
import entities.Player;
import gamestates.Playing;
import main.Game;
import utilz.HelpMethods;
import static utilz.Constants.BossConstants.*;
import static utilz.Constants.Directions.*;
import static utilz.Constants.EnemyConstants.HIT;
import static utilz.Constants.GRAVITY;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;

public class BossWorm extends BaseBoss {

    private static final float WALK_SPEED = 0.5f * Game.SCALE;
    private static final int DETECT_RANGE = 800;

    public BossWorm(float x, float y, Playing playing) {
        super(x, y, BOSS1_WIDTH, BOSS1_HEIGHT, BOSS_1, playing);
        initHitbox((int)(24 * Game.SCALE), (int)(28 * Game.SCALE));
        loadFrames();
    }

    @Override
    protected void loadFrames() {
        moveFrames   = loadStrip("sprites/Boss1/Boss1_Move.png",     5);
        attackedFrames = loadStrip("sprites/Boss1/Boss1_Attacked.png", 3);
        hitFrames    = loadStrip("sprites/Boss1/Boss1_Hit.png",      5);
        deadFrames   = loadStrip("sprites/Boss1/Boss1_Dead.png",     5);
        System.out.println("moveFrames null? " + (moveFrames == null || moveFrames[0] == null));
    System.out.println("hitFrames null? "  + (hitFrames  == null || hitFrames[0]  == null));
    }

    @Override
    protected void updateAI(int[][] lvlData, Player player) {
        if (state == BOSS_HIT || state == HIT || state == BOSS_ATTACKED)
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
                // manually snap to floor instead of using the broken helper
                while (!HelpMethods.CanMoveHere(hitbox.x, hitbox.y + 1, hitbox.width, hitbox.height, lvlData))
                    hitbox.y--;
                inAir = false;
                airSpeed = 0;
            }
            return;
        } else {
            // find the floor tile row directly and snap above it
            int floorTileY = (int)((hitbox.y + hitbox.height) / Game.TILES_SIZE) + 1;
            while (floorTileY < lvlData.length && lvlData[floorTileY][(int)(hitbox.x / Game.TILES_SIZE)] == 11)
                floorTileY++;
            hitbox.y = (floorTileY * Game.TILES_SIZE) - hitbox.height - 1;
            inAir = false;
            airSpeed = 0;
        }

        // only check floor edge AFTER confirming we're walking
        float speed = (phase == 2) ? WALK_SPEED * 1.5f : WALK_SPEED;
        float dx = player.getHitbox().x - hitbox.x;

        if (Math.abs(dx) < DETECT_RANGE)
            walkDir = (dx > 0) ? RIGHT : LEFT;

        float move = (walkDir == RIGHT) ? speed : -speed;

        if (HelpMethods.CanMoveHere(hitbox.x + move, hitbox.y, hitbox.width, hitbox.height, lvlData)
            && HelpMethods.IsFloor(hitbox, move, lvlData))
            hitbox.x += move;
        else
            walkDir = (walkDir == RIGHT) ? LEFT : RIGHT;

        // NOW check if walked off an edge
        if (!HelpMethods.IsEntityOnFloor(hitbox, lvlData))
            inAir = true;
    }

    @Override
    protected void doAttack(Player player) {
        float dx = Math.abs(player.getHitbox().x - hitbox.x);
        float dy = Math.abs(player.getHitbox().y - hitbox.y);
        
        // only enter attack animation if actually touching player
        if (dx < hitbox.width && dy < hitbox.height) {
            state = BOSS_ATTACKED;
            aniIndex = 0;
            player.changeHealth(-GetEnemyDmgBoss(BOSS_1));
        }
    }

    @Override
    protected void onAnimationComplete() {
        if (state == BOSS_ATTACKED || state == BOSS_HIT) {
            state = BOSS_MOVE;
            aniIndex = 0;
        }
    }

    @Override
    public void draw(Graphics g, int lvlOffset) {
        BufferedImage[] frames = getCurrentFrames();
        if (frames == null || aniIndex >= frames.length) return;

        int flipX = (walkDir == LEFT) ? width : 0;
        int flipW = (walkDir == LEFT) ? -1 : 1;

        g.drawImage(frames[aniIndex],
            (int)(hitbox.x) - lvlOffset + flipX - BOSS1_DRAWOFFSET_X,
            (int)(hitbox.y) - BOSS1_DRAWOFFSET_Y,
            width * flipW, height, null);
    }

    @Override protected int getAttackCooldown() { return 80; }
    @Override protected int getPhase2Cooldown() { return 50; }
    @Override protected String getBossName()     { return "CAVE WORM"; }

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
        state = BOSS_MOVE;
        applySpawn(null); 
    }

    @Override
    public void applySpawn(Point spawnPoint) {
        hitbox.x = 500 * Game.SCALE;  // adjust X to wherever you want
        hitbox.y = 2 * Game.TILES_SIZE; // adjust Y — boss will gravity-drop from here
    }
}