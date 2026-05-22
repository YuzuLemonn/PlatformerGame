package entities.players;

import entities.Player;
import entities.Projectile;
import entities.Enemy;
import gamestates.Playing;
import main.Game;
import utilz.LoadSave;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import static utilz.Constants.*;
import static utilz.Constants.PlayerConstants.*;
import static utilz.Constants.DamageConstants.*;
import static utilz.Constants.Directions.*;

public class Assassin extends Player {

    // Skill3 specific
    private boolean skill3Part2 = false;
    private boolean skill3TeleportDone = false;
    private Enemy backstabTarget = null;
    private float teleportX, teleportY;
    private BufferedImage[] skill3ShadowFrames;
    private BufferedImage[] skill3BackstabFrames;
    private boolean isUsingSkill3 = false;

    public Assassin(float x, float y, int width, int height, Playing playing) {
        super(x, y, width, height, playing);
        this.maxHealth = 80;
        this.currentHealth = maxHealth;
        this.walkSpeed = Game.SCALE * 1.3f;
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
        
        // Load skill3 frames
        int skill3FrameCount = GetSpriteAmount(SKILL3, "Assassin");  // This returns 22 now
        skill3ShadowFrames = loadFrames("Attack3_Shadow_Assassin.png", skill3FrameCount);
        skill3BackstabFrames = loadFrames("Attack3_Assassin.png", 5);
        skill3Frames = skill3ShadowFrames;
        
        statusBarImg = LoadSave.GetSpriteAtlas(LoadSave.STATUS_BAR);
    }

    private BufferedImage[] loadFrames(String fileName, int frameCount) {
        BufferedImage sheet = LoadSave.GetSpriteAtlas("sprites/Assassin/" + fileName);
        if (sheet == null) return new BufferedImage[frameCount];
        int fw = sheet.getWidth() / frameCount;
        int fh = sheet.getHeight();
        BufferedImage[] frames = new BufferedImage[frameCount];
        for (int i = 0; i < frameCount; i++)
            frames[i] = sheet.getSubimage(i * fw, 0, fw, fh);
        return frames;
    }

    @Override
    protected String getCharacterName() { return "Assassin"; }

    @Override
    protected boolean isProjectileAttack() { return false; }

    @Override
    protected void spawnProjectile() {}

    @Override
    protected void useSkill2() {
        //if (!useStamina(STAMINA_COST_SKILL2)) return;
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
        //if (!useStamina(STAMINA_COST_SKILL3)) return;
        if (isUsingSkill3) return;
        
        // Find the nearest enemy
        Enemy nearestEnemy = findNearestEnemy();
        if (nearestEnemy == null) {
            System.out.println("No enemy found to backstab!");
            return;
        }
        
        // Store target
        backstabTarget = nearestEnemy;
        skill3Part2 = false;
        skill3TeleportDone = false;
        isUsingSkill3 = true;
        startIFrames();
        
        // SIMPLE - teleport directly to enemy's position (no offsets!)
        teleportX = nearestEnemy.getHitbox().x;
        teleportY = nearestEnemy.getHitbox().y;
        
        // Start skill3 animation
        skill3Frames = skill3ShadowFrames;
        state = SKILL3;
        aniIndex = 0;
        aniTick = 0;
        
        playing.getGame().getAudioPlayer().playAttackSound();
    }
    
    @Override
    public void update() {
        // Handle skill3 animation
        if (state == SKILL3 && !skill3TeleportDone) {
            aniTick++;
            // Fixed animation speed
            if (aniTick >= 5) {  // Faster animation
                aniTick = 0;
                aniIndex++;
                
                // Teleport at frame 15
                if (aniIndex >= 15) {
                    if (backstabTarget != null && backstabTarget.isActive()) {
                        // DIRECT TELEPORT - no offsets
                        hitbox.x = teleportX;
                        hitbox.y = teleportY;
                        
                        // Face the enemy
                        if (hitbox.x > backstabTarget.getHitbox().x) {
                            flipW = -1;
                            flipX = width;
                        } else {
                            flipW = 1;
                            flipX = 0;
                        }
                    }
                    
                    skill3TeleportDone = true;
                    skill3Part2 = true;
                    aniIndex = 0;
                    skill3Frames = skill3BackstabFrames;
                    playing.getGame().getAudioPlayer().playAttackSound();
                }
            }
            return;
        }
        
        if (state == SKILL3 && skill3Part2) {
            aniTick++;
            if (aniTick >= ANI_SPEED) {
                aniTick = 0;
                aniIndex++;
                
                // Damage at frame 2
                if (aniIndex == 2 && backstabTarget != null && backstabTarget.isActive()) {
                    int damage = ASSASSIN_SKILL3_DMG;
                    damage = (int)(damage * getDamageMultiplier());
                    damage = (int)(damage * 1.5f);
                    
                    int knockbackDir = (hitbox.x > backstabTarget.getHitbox().x) ? RIGHT : LEFT;
                    backstabTarget.hurt(damage, knockbackDir);
                }
                
                // End of skill
                if (aniIndex >= 5) {
                    state = IDLE;
                    skill3 = false;
                    skill3Part2 = false;
                    skill3TeleportDone = false;
                    backstabTarget = null;
                    isUsingSkill3 = false;
                    clearIFrames();
                    aniIndex = 0;
                }
            }
            return;
        }
        
        // Normal update
        super.update();
    }
    
    private Enemy findNearestEnemy() {
        ArrayList<Enemy> allEnemies = playing.getEnemyManager().getAllEnemies();
        Enemy nearest = null;
         float minDistance = ASSASSIN_SKILL3_RANGE;
        
        for (Enemy enemy : allEnemies) {
            if (enemy.isActive() && enemy.getState() != DEAD) {
                float dx = enemy.getHitbox().x - hitbox.x;
                float dy = enemy.getHitbox().y - hitbox.y;
                float distance = (float) Math.sqrt(dx * dx + dy * dy);
                
                if (distance < minDistance) {
                    minDistance = distance;
                    nearest = enemy;
                }
            }
        }
        return nearest;
    }

    @Override
    public void resetAll() {
        super.resetAll();
        skill3Part2 = false;
        skill3TeleportDone = false;
        backstabTarget = null;
        isUsingSkill3 = false;
        skill3Frames = skill3ShadowFrames;
        clearIFrames();
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
        return 0;
    }

    @Override
    protected int getAttackStaminaCost() {
        return STAMINA_COST_ATTACK;
    }

    @Override
    protected int getSkill2StaminaCost() {
        return STAMINA_COST_SKILL2;
    }

    @Override
    protected int getSkill3StaminaCost() {
        return STAMINA_COST_SKILL3;
    }
}
