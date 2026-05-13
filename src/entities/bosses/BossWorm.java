package entities.bosses;

import entities.BaseBoss;
import entities.Player;
import gamestates.Playing;
import main.Game;
import utilz.HelpMethods;
import static utilz.Constants.BossConstants.*;
import static utilz.Constants.Directions.*;
import static utilz.Constants.EnemyConstants.HIT;
import static utilz.Constants.PlayerConstants.DEAD;
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
        moveFrames = loadStrip("sprites/Boss1/Boss1_Move.png",     5);
        hitFrames = loadStrip("sprites/Boss1/Boss1_Hit.png", 3);
        attackFrames = loadStrip("sprites/Boss1/Boss1_Attack.png",      5);
        deadFrames = loadStrip("sprites/Boss1/Boss1_Dead.png",     5);
        System.out.println("moveFrames null? " + (moveFrames == null || moveFrames[0] == null));
    System.out.println("hitFrames null? "  + (hitFrames  == null || hitFrames[0]  == null));
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
        } else {
            int floorTileY = (int)((hitbox.y + hitbox.height) / Game.TILES_SIZE) + 1;
            while (floorTileY < lvlData.length && lvlData[floorTileY][(int)(hitbox.x / Game.TILES_SIZE)] == 11)
                floorTileY++;
            hitbox.y = (floorTileY * Game.TILES_SIZE) - hitbox.height - 1;
            tileY = (int)(hitbox.y / Game.TILES_SIZE);
            inAir = false;
            airSpeed = 0;
        }

        float speed = (phase == 2) ? WALK_SPEED * 1.5f : WALK_SPEED;
        float dx = (float)(player.getHitbox().getCenterX() - hitbox.getCenterX());
        float dy = (float)(player.getHitbox().getCenterY() - hitbox.getCenterY());

        boolean playerInRange = Math.abs(dx) < DETECT_RANGE && Math.abs(dy) < Game.TILES_SIZE * 4;
        if (playerInRange) {
            turnTowardsPlayer(player);
        }
        
        float move = (walkDir == RIGHT) ? speed : -speed;

        if (HelpMethods.CanMoveHere(hitbox.x + move, hitbox.y, hitbox.width, hitbox.height, lvlData)
            && HelpMethods.IsFloor(hitbox, move, lvlData))
            hitbox.x += move;
        else
            walkDir = (walkDir == RIGHT) ? LEFT : RIGHT;

        if (!HelpMethods.IsEntityOnFloor(hitbox, lvlData))
            inAir = true;

        boolean playerClose = hitbox.intersects(player.getHitbox())
                || (Math.abs(dx) < hitbox.width * 2 && Math.abs(dy) < hitbox.height * 2);

        if (playerClose && attackCooldown <= 0) {
            doAttack(player);
            attackCooldown = (phase == 2) ? getPhase2Cooldown() : getAttackCooldown();
        }
    }

    @Override
    protected void doAttack(Player player) {
        float dx = (float)Math.abs(player.getHitbox().getCenterX() - hitbox.getCenterX());
        float dy = (float)Math.abs(player.getHitbox().getCenterY() - hitbox.getCenterY());
        
        if (hitbox.intersects(player.getHitbox()) || (dx < hitbox.width && dy < hitbox.height)) {
            state = BOSS_ATTACKED;
            aniIndex = 0;
            player.changeHealth(-GetEnemyDmgBoss(BOSS_1));
        }
    }

    @Override
    protected void onAnimationComplete() {
        if (state == BOSS_ATTACKED || state == BOSS_HIT || state == HIT) {
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
        hitbox.y = 5 * Game.TILES_SIZE; // adjust Y — boss will gravity-drop from here
    }

    @Override
    protected void newState(int state) {
        if (state == HIT) {
            this.state = BOSS_HIT;
            aniIndex = 0;  // ← reset animation
            aniTick = 0;   // ← reset tick
            return;
        }
        if (state == DEAD) { this.state = BOSS_DEAD; active = false; return; }
        this.state = state;
    }
}
