package entities.bosses;

import entities.BaseBoss;
import entities.Player;
import entities.Projectile;
import gamestates.Playing;
import main.Game;
import utilz.HelpMethods;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;

import static utilz.Constants.EnemyConstants.HIT;
import static utilz.Constants.GRAVITY;
import static utilz.Constants.BossConstants.*;  
import static utilz.Constants.Directions.*;

public class BossGolem extends BaseBoss {

    // Movement stats (same for both phases)
    private static final float WALK_SPEED = 0.35f * Game.SCALE;
    private static final int MELEE_COOLDOWN = 240;
    private static final int SHOOT_DELAY = 180; // Only used in phase 2
    private static final int DETECT_RANGE = 1200;
    private static final float PROJECTILE_SPEED = 0.4f * Game.SCALE; // Slow, jumpable!
    
    // Phase 2 flag
    private boolean canShoot = false;
    
    // Timers
    private int shootTimer = 0;
    private boolean attackHitDealt = false;
    
    // Animation frames
    private BufferedImage[] meleeAttackFrames;
    private BufferedImage[] rangedAttackFrames;

    public BossGolem(float x, float y, Playing playing) {
        super(x, y, BOSS3_WIDTH, BOSS3_HEIGHT, BOSS_3, playing);
        initHitbox((int)(30 * Game.SCALE), (int)(25 * Game.SCALE));
        loadFrames();
    }

    @Override
    protected void loadFrames() {
        moveFrames = loadStrip("sprites/Boss3/Boss3_Move.png", 4);
        meleeAttackFrames = loadStrip("sprites/Boss3/Boss3_Attack.png", 5);
        rangedAttackFrames = loadStrip("sprites/Boss3/Boss3_RangedAttack.png", 4);
        hitFrames = loadStrip("sprites/Boss3/Boss3_Hit.png", 3);
        deadFrames = loadStrip("sprites/Boss3/Boss3_Dead.png", 7);
        
        attackFrames = meleeAttackFrames;
    }

    @Override
    protected void onPhaseTransition() {
        // At 50% HP, unlock projectiles
        canShoot = true;
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

        float dx = player.getHitbox().x - hitbox.x;
        float dy = player.getHitbox().y - hitbox.y;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        
        float yDiff = Math.abs(dy);
        boolean sameGround = (yDiff < 100 * Game.SCALE);

        // Turn towards player
        if (distance < DETECT_RANGE && sameGround) {
            turnTowardsPlayer(player);
        }

        // Movement (same speed always)
        float move = (walkDir == RIGHT) ? WALK_SPEED : -WALK_SPEED;

        if (HelpMethods.CanMoveHere(hitbox.x + move, hitbox.y, hitbox.width, hitbox.height, lvlData)) {
            hitbox.x += move;
        } else {
            walkDir = (walkDir == RIGHT) ? LEFT : RIGHT;
        }

        // PHASE 2 ONLY: Shoot projectiles
        if (canShoot && sameGround && distance < DETECT_RANGE && distance > hitbox.width) {
            if (shootTimer <= 0) {
                doRangedAttack(player);
                shootTimer = SHOOT_DELAY;
            } else {
                shootTimer--;
            }
        } else {
            if (shootTimer > 0) shootTimer--;
        }

        // Melee attack when very close (works in both phases)
        boolean playerClose = Math.abs(dx) < hitbox.width && Math.abs(dy) < hitbox.height;
        if (playerClose && sameGround && attackCooldown <= 0) {
            doMeleeAttack(player);
            attackCooldown = MELEE_COOLDOWN;
        }
        
        if (attackCooldown > 0) attackCooldown--;
    }

    private void doRangedAttack(Player player) {
        if (state == BOSS_ATTACKED) return;
        attackFrames = rangedAttackFrames;
        state = BOSS_ATTACKED;
        aniIndex = 0;
        attackHitDealt = false;
    }

    private void doMeleeAttack(Player player) {
        if (state == BOSS_ATTACKED) return;
        attackFrames = meleeAttackFrames;
        state = BOSS_ATTACKED;
        aniIndex = 0;
        attackHitDealt = false;
    }

    @Override
    protected void doAttack(Player player) {
        doMeleeAttack(player);
    }

    private void shootWaterProjectile(Player player) {

        int dir = (player.getHitbox().x > hitbox.x) ? 1 : -1;
        
        float spawnX, spawnY;
        if (dir == 1) {
            spawnX = hitbox.x + hitbox.width - 10;
        } else {
            spawnX = hitbox.x - 20 * Game.SCALE;
        }
        spawnY = hitbox.y + hitbox.height / 2f;
        
        Projectile water = new Projectile(
            
            spawnX, spawnY, dir, playing,
            "sprites/Boss3/Boss3_Projectile.png",
            2,  // Change to your actual frame count
            BOSS3_WATER_DAMAGE
            
        );
        projectiles.add(water);
        water.setSpeed(PROJECTILE_SPEED);
        water.setSpawnX(spawnX);
        projectiles.add(water);
    }

    @Override
    protected void updateAnimationTick() {
        super.updateAnimationTick();
        
        if (state == BOSS_ATTACKED && !attackHitDealt) {
            if (attackFrames == rangedAttackFrames) {
                // Ranged attack - spawn projectile
                if (aniIndex == 2) {
                    attackHitDealt = true;
                    Player player = playing.getPlayer();
                    shootWaterProjectile(player);
                }
            } else if (attackFrames == meleeAttackFrames) {
                // Melee attack - damage frame (adjust index based on your animation)
                if (aniIndex == 3) {
                    attackHitDealt = true;
                    Player player = playing.getPlayer();
                    if (hitbox.intersects(player.getHitbox())) {
                        int dmg = GetEnemyDmgBoss(BOSS_3);
                        player.changeHealth(-dmg);
                    }
                }
            }
        }
    }

    @Override
    protected void onProjectileHit(Projectile p, Player player) {
        player.changeHealth(-p.getDamage());
        p.setInactive();
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
    protected BufferedImage[] getCurrentFrames() {
        if (currentHealth <= 0) return deadFrames;
        if (state == BOSS_HIT || state == HIT) return hitFrames;
        if (state == BOSS_ATTACKED) return attackFrames;
        return moveFrames;
    }

    @Override
    public void hurt(int amount) {
        currentHealth -= amount;
        
        // Phase transition at 50% health - UNLOCK PROJECTILES
        if (!phaseTransitioned && currentHealth < maxHealth * 0.5f) {
            phase = 2;
            phaseTransitioned = true;
            onPhaseTransition();
        }
        
        if (currentHealth <= 0) {
            currentHealth = 0;
            state = BOSS_DEAD;
        } else {
            state = BOSS_HIT;
            aniIndex = 0;
            aniTick = 0;
        }
    }

    @Override
    public void hurt(int amount, int playerDir) {
        hurt(amount);
    }

    @Override
    public void draw(Graphics g, int lvlOffset) {
        BufferedImage[] frames = getCurrentFrames();
        if (frames == null || aniIndex >= frames.length || frames[aniIndex] == null) return;

        int flipX = (walkDir == RIGHT) ? BOSS3_WIDTH : 0;
        int flipW = (walkDir == RIGHT) ? -1 : 1;

        g.drawImage(frames[aniIndex],
            (int)(hitbox.x) - lvlOffset - BOSS3_DRAWOFFSET_X + flipX,
            (int)(hitbox.y) - BOSS3_DRAWOFFSET_Y,
            BOSS3_WIDTH * flipW, BOSS3_HEIGHT, null);
        //drawHitbox(g, lvlOffset);
    }

    @Override
    protected int getAttackCooldown() { 
        return MELEE_COOLDOWN;
    }
    
    @Override
    protected int getPhase2Cooldown() { 
        return MELEE_COOLDOWN;
    }
    
    @Override
    protected String getBossName() { 
        return (phase == 2) ? "Neo (Projectiles Unlocked)" : "Neo"; 
    }

    @Override
    public void resetEnemy() {
        super.resetEnemy();
        attackCooldown = 0;
        shootTimer = 0;
        aniTick = 0;
        aniIndex = 0;
        inAir = false;
        airSpeed = 0;
        walkDir = RIGHT;
        firstUpdate = true;
        attackHitDealt = false;
        state = BOSS_MOVE;
        phase = 1;
        phaseTransitioned = false;
        canShoot = false;
        projectiles.clear();
        applySpawn(null);
    }

    @Override
    public void applySpawn(Point spawnPoint) {
        hitbox.x = 500 * Game.SCALE;
        hitbox.y = 5 * Game.TILES_SIZE;
    }

    @Override
    protected void checkPhaseTransition() {
        // Handled in hurt() method
    }
}