package entities.players;

import entities.Player;
import entities.Projectile;
import entities.Enemy;
import gamestates.Playing;
import main.Game;
import utilz.HelpMethods;
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
        
        // Load skill3 frames separately
        skill3ShadowFrames = loadFrames("Attack3_Shadow_Assassin.png", 21);
        skill3BackstabFrames = loadFrames("Attack3_Assassin.png", 5);
        skill3Frames = skill3ShadowFrames;
        
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
    protected boolean isProjectileAttack() { return false; }

    @Override
    protected void spawnProjectile() {}

    @Override
    protected void useSkill2() {
        if (!useStamina(STAMINA_COST_SKILL2)) return;
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
        if (!useStamina(STAMINA_COST_SKILL3)) return;
        if (isUsingSkill3) return;
        
        // Find the nearest enemy
        Enemy nearestEnemy = findNearestEnemy();
        if (nearestEnemy == null) {
            System.out.println("No enemy found - returning stamina?");
            stamina += STAMINA_COST_SKILL3;
            return;
        }
        
        // Store target and calculate teleport position
        backstabTarget = nearestEnemy;
        skill3Part2 = false;
        skill3TeleportDone = false;
        isUsingSkill3 = true;
        
        teleportX = calculateBehindPosition(nearestEnemy);
        teleportY = nearestEnemy.getHitbox().y + (nearestEnemy.getHitbox().height / 2) - (hitbox.height / 2);
        
        // Clamp to bounds
        teleportX = Math.max(10, Math.min(teleportX, Game.GAME_WIDTH - hitbox.width - 10));
        teleportY = Math.max(10, Math.min(teleportY, Game.GAME_HEIGHT - hitbox.height - 50));
        
        // Start skill3 animation
        skill3Frames = skill3ShadowFrames;
        state = SKILL3;
        aniIndex = 0;
        aniTick = 0;
        
        playing.getGame().getAudioPlayer().playAttackSound();
    }
    
    @Override
    public void update() {
        // Handle custom skill3 animation timing
        if (state == SKILL3 && !skill3TeleportDone) {
            // Faster frame counting
            aniTick++;
            int skill3Speed = ANI_SPEED / 2;
            if (skill3Speed < 5) skill3Speed = 5;
            
            if (aniTick >= skill3Speed) {
                aniTick = 0;
                aniIndex++;
                
                // TELEPORT HAPPENS HERE when animation reaches frame 21
                if (aniIndex >= 21) {
                    if (backstabTarget != null && backstabTarget.isActive()) {
                        // Store original position in case we need to revert
                        float originalX = hitbox.x;
                        float originalY = hitbox.y;
                        
                        // Set teleport position
                        hitbox.x = teleportX;
                        hitbox.y = teleportY;
                        
                        // SNAP TO GROUND - Find the floor below the teleport position
                        boolean foundGround = false;
                        for (int i = 0; i < 50; i++) {
                            if (HelpMethods.IsEntityOnFloor(hitbox, lvlData)) {
                                foundGround = true;
                                break;
                            }
                            hitbox.y += 2;
                        }
                        
                        // If no ground found below, try moving up
                        if (!foundGround) {
                            hitbox.y = teleportY;
                            for (int i = 0; i < 50; i++) {
                                if (HelpMethods.IsEntityOnFloor(hitbox, lvlData)) {
                                    foundGround = true;
                                    break;
                                }
                                hitbox.y -= 2;
                            }
                        }
                        
                        // If still no ground, revert to original position
                        if (!foundGround) {
                            hitbox.x = originalX;
                            hitbox.y = originalY;
                            System.out.println("No safe teleport position found!");
                        }
                        
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
                
                // Deal damage at frame 3 of backstab
                if (aniIndex == 3 && backstabTarget != null && backstabTarget.isActive()) {
                    int damage = ASSASSIN_SKILL3_DMG;
                    damage = (int)(damage * getDamageMultiplier());
                    damage = (int)(damage * 1.5f);
                    
                    int knockbackDir = (hitbox.x > backstabTarget.getHitbox().x) ? RIGHT : LEFT;
                    backstabTarget.hurt(damage, knockbackDir);
                    System.out.println("Backstab! Damage: " + damage);
                }
                
                // End of backstab animation
                if (aniIndex >= 5) {
                    state = IDLE;
                    skill3 = false;
                    skill3Part2 = false;
                    skill3TeleportDone = false;
                    backstabTarget = null;
                    isUsingSkill3 = false;
                    aniIndex = 0;
                }
            }
            return;
        }
        
        // Normal update for all other states
        super.update();
    }
    
    private Enemy findNearestEnemy() {
        ArrayList<Enemy> allEnemies = playing.getEnemyManager().getAllEnemies();
        Enemy nearest = null;
        float minDistance = 400 * Game.SCALE;
        
        System.out.println("Looking for enemies. Total enemies: " + allEnemies.size());
        
        for (Enemy enemy : allEnemies) {
            if (enemy.isActive() && enemy.getState() != DEAD) {
                float dx = enemy.getHitbox().x - hitbox.x;
                float dy = enemy.getHitbox().y - hitbox.y;
                float distance = (float) Math.sqrt(dx * dx + dy * dy);
                
                System.out.println("Enemy at distance: " + distance + ", range: " + minDistance);
                
                if (distance < minDistance) {
                    minDistance = distance;
                    nearest = enemy;
                    System.out.println("Found enemy at distance: " + distance);
                }
            }
        }
        
        if (nearest == null) {
            System.out.println("No enemies found in range!");
        }
        
        return nearest;
    }
    
    private float calculateBehindPosition(Enemy enemy) {
        boolean playerOnRight = (hitbox.x > enemy.getHitbox().x);
        
        if (playerOnRight) {
            // Teleport to left side of enemy
            return enemy.getHitbox().x - hitbox.width - 5 * Game.SCALE;
        } else {
            // Teleport to right side of enemy
            return enemy.getHitbox().x + enemy.getHitbox().width + 5 * Game.SCALE;
        }
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
}