package entities;

import audio.AudioPlayer;
import gamestates.Playing;
import main.Game;
import utilz.LoadSave;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import java.awt.event.MouseEvent;

import static utilz.Constants.*;
import static utilz.Constants.PlayerConstants.*;
import static utilz.Constants.Directions.*;
import static utilz.HelpMethods.*;

public abstract class Player extends Entity {
    protected BufferedImage[] idleFrames;
    protected BufferedImage[] runFrames;
    protected BufferedImage[] jumpFrames;
    protected BufferedImage[] attackFrames;
    protected BufferedImage[] skill2Frames;
    protected BufferedImage[] skill3Frames;
    protected BufferedImage statusBarImg;
    protected boolean skill3;
    protected boolean skill2;
    
    protected abstract void loadAnimations();
    protected abstract String getCharacterName();
    protected abstract boolean isProjectileAttack();
    protected abstract void spawnProjectile();
    protected abstract void useSkill2();
    protected abstract void useSkill3();
    
    protected ArrayList<Projectile> projectiles = new ArrayList<>();

    protected boolean moving = false, attacking = false;
    protected boolean left, right, jump;
    protected abstract int getAttackHitFrame();
    protected abstract int getSkill2HitFrame();
    protected abstract int getSkill3HitFrame();
    
    protected int[][] lvlData;
    private float xDrawOffset = 21 * Game.SCALE;
    private float yDrawOffset = 10 * Game.SCALE;
    
    // Jumping / Gravity
    private float jumpSpeed = -2.25f * Game.SCALE;
    private float fallSpeedAfterCollision = 0.5f * Game.SCALE;
    
    // Status bar UI
    private int statusBarWidth  = (int)(192 * Game.SCALE);
    private int statusBarHeight = (int)(58  * Game.SCALE);
    private int statusBarX      = (int)(10  * Game.SCALE);
    private int statusBarY      = (int)(10  * Game.SCALE);
    
    private int healthBarWidth  = (int)(150 * Game.SCALE);
    private int healthBarHeight = (int)(4   * Game.SCALE);
    private int healthBarXStart = (int)(34  * Game.SCALE);
    private int healthBarYStart = (int)(14  * Game.SCALE);
    private int healthWidth     = healthBarWidth;
    
    protected int flipX = 0;
    protected int flipW = 1;
    
    private boolean attackChecked;
    private float stamina = MAX_STAMINA;
    protected Playing playing;
    private boolean skill2Checked, skill3Checked;
    
    public Player(float x, float y, int width, int height, Playing playing) {
        super(x, y, width, height);
        this.playing = playing;
        this.state = IDLE;
        this.maxHealth = 100;
        this.currentHealth = maxHealth;
        this.walkSpeed = Game.SCALE * 1.0f;
    }

    public void setSpawn(Point spawn) {
        this.x = spawn.x;
        this.y = spawn.y;
        hitbox.x = x;
        hitbox.y = y;
    }

    protected void initAttackBox() {
        attackBox = new Rectangle2D.Float(x, y,
                (int)(20 * Game.SCALE),
                (int)(27 * Game.SCALE));
    }

    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_A:     setLeft(true);   break;
            case KeyEvent.VK_D:     setRight(true);  break;
            case KeyEvent.VK_SPACE: setJump(true);   break;
            case KeyEvent.VK_R:
                if (!skill3)   
                    setSkill3(true);
                break;
        }
    }

    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_A:     setLeft(false);  break;
            case KeyEvent.VK_D:     setRight(false); break;
            case KeyEvent.VK_SPACE: setJump(false);  break;
            case KeyEvent.VK_R:
        }
    }

    
    public void update() {
        updateHealthBar();
        updateStamina();

        if (attacking)
            checkAttack();
        if (skill2)
            checkSkill2();
        if (skill3)
            checkSkill3();

        if (currentHealth <= 0) {
            if (state != DEAD) {
                state = DEAD;
                aniTick = 0;
                aniIndex = 0;
                playing.setPlayerDying(true);
                playing.getGame().getAudioPlayer().playEffect(AudioPlayer.DIE);

                if (!IsEntityOnFloor(hitbox, lvlData)) {
                    inAir = true;
                    airSpeed = 0;
                }
            } else if (aniIndex == GetSpriteAmount(DEAD) - 1 && aniTick >= ANI_SPEED - 1) {
                playing.setGameOver(true);
                playing.getGame().getAudioPlayer().stopSong();
                playing.getGame().getAudioPlayer().playEffect(AudioPlayer.GAMEOVER);
            } else {
                updateAnimationTick();

                if (inAir)
                    if (CanMoveHere(hitbox.x, hitbox.y + airSpeed, hitbox.width, hitbox.height, lvlData)) {
                        hitbox.y += airSpeed;
                        airSpeed += GRAVITY;
                    } else
                        inAir = false;
            }
            return;
        }

        updateAttackBox();

        if (state == HIT)
            updateKnockback(lvlData);
        else
            updatePos();

        updateAnimationTick();
        setAnimation();
    }

    public void updateProjectiles(int[][] lvlData) {
        projectiles.removeIf(p -> !p.isActive());
        for (Projectile p : projectiles)
            p.update(lvlData);
    }

    public void renderProjectiles(Graphics g, int lvlOffset) {
        for (Projectile p : projectiles)
            if (p.isActive())
                p.render(g, lvlOffset);
    }

    private void checkAttack() {
        if (attackChecked || aniIndex != getAttackHitFrame()) return;
        attackChecked = true;
        if (!useStamina(STAMINA_COST_ATTACK)) return; // gate here
        if (isProjectileAttack())
            spawnProjectile();
        else
            playing.checkEnemyHit(attackBox);
        playing.getGame().getAudioPlayer().playAttackSound();
    }

    private void updateAttackBox() {
        if (right && !left) {
            attackBox.x = hitbox.x + hitbox.width + (int)(Game.SCALE * 3);
            flipX = 0;
            flipW = 1;
        } else if (left && !right) {
            attackBox.x = hitbox.x - attackBox.width - (int)(Game.SCALE * 3);
        } else {
            if (flipW == 1)
                attackBox.x = hitbox.x + hitbox.width + (int)(Game.SCALE * 3);
            else
                attackBox.x = hitbox.x - attackBox.width - (int)(Game.SCALE * 3);
        }
        attackBox.y = hitbox.y + (Game.SCALE * 5);
    }

    private void updateHealthBar() {
        healthWidth = (int)((currentHealth / (float)maxHealth) * healthBarWidth);
    }

    public void render(Graphics g, int lvlOffset) {
        BufferedImage[] currentFrames = getCurrentFrames();
        if (currentFrames != null && aniIndex < currentFrames.length) {
            g.drawImage(currentFrames[aniIndex],
                    (int)(hitbox.x - xDrawOffset) - lvlOffset + flipX,
                    (int)(hitbox.y - yDrawOffset + (int)(pushDrawOffset)),
                    width * flipW, height, null);
        }
//        drawAttackBox(g, lvlOffset);
//        drawHitbox(g, lvlOffset);
        drawUI(g);
    }

    private BufferedImage[] getCurrentFrames() {
        switch (state) {
            case RUNNING: return runFrames;
            case JUMP:    return jumpFrames;
            case ATTACK:  return attackFrames;
            case SKILL2:   return skill2Frames;  // new
            case SKILL3:   return skill3Frames;
            default:      return idleFrames;
        }
    }

    private void drawUI(Graphics g) {
        g.drawImage(statusBarImg, statusBarX, statusBarY, statusBarWidth, statusBarHeight, null);
        
        // health bar
        g.setColor(Color.red);
        g.fillRect(healthBarXStart + statusBarX, healthBarYStart + statusBarY, healthWidth, healthBarHeight);
        
        // stamina bar
        int staminaBarWidth = (int)(healthBarWidth * 0.70f);
        int staminaWidth = (int)((stamina / MAX_STAMINA) * staminaBarWidth);
        g.setColor(new Color(255, 200, 0));
        g.fillRect(healthBarXStart + statusBarX + (int)(10 * Game.SCALE), // nudge right
        healthBarYStart + statusBarY + (int)(20 * Game.SCALE), 
        staminaWidth, healthBarHeight);
        if (staminaMessageTimer > 0) {
            staminaMessageTimer--;
            g.setFont(new Font("Arial", Font.BOLD, (int)(8 * Game.SCALE)));
            g.setColor(new Color(255, 80, 0));
            FontMetrics fm = g.getFontMetrics();
            String msg = "Not enough stamina!";
            int msgX = (Game.GAME_WIDTH - fm.stringWidth(msg)) / 2;
            int msgY = Game.GAME_HEIGHT / 2;
            g.drawString(msg, msgX, msgY);
        }
    }
    
    private void updateAnimationTick() {
        aniTick++;
        if (aniTick >= ANI_SPEED) {
            aniTick = 0;
            aniIndex++;
            int frameCount = getFrameCount(state);
            if (aniIndex >= frameCount) {
                aniIndex = 0;
                attacking = false;
                attackChecked = false;
                skill2 = false;        
                skill2Checked = false; 
                skill3 = false;        
                skill3Checked = false;
                if (state == HIT) {
                    newState(IDLE);
                    airSpeed = 0f;
                    pushDrawOffset = 0;
                    if (!IsEntityOnFloor(hitbox, lvlData))
                        inAir = true;
                }
            }
        }
    }

    private void setAnimation() {
        int startAni = state;

            if (skill3) {
            if (stamina < STAMINA_COST_SKILL3) {
                skill3 = false;
                showNotEnoughStamina();
            } else if (state != SKILL3) {
                state = SKILL3;
                aniIndex = 0;
                aniTick = 0;
                return;
            } else return;
        } else if (skill2) {
            if (stamina < STAMINA_COST_SKILL2) {
                skill2 = false;
                showNotEnoughStamina();
            } else if (state != SKILL2) {
                state = SKILL2;
                aniIndex = 0;
                aniTick = 0;
                return;
            } else return;
        } else if (attacking) {
            if (stamina < STAMINA_COST_ATTACK) {
                attacking = false;
                showNotEnoughStamina();
            } else if (state != ATTACK) {
                state = ATTACK;
                aniIndex = 0;
                aniTick = 0;
                return;
            } else return;
        }

        if (state == HIT) return;

        if (moving)
            state = RUNNING;
        else
            state = IDLE;

        if (inAir) {
            if (airSpeed < 0)
                state = JUMP;
            else
                state = FALLING;
        }

        if (startAni != state)
            resetAniTick();
    }

    private void resetAniTick() {
        aniTick = 0;
        aniIndex = 0;
    }

    private void updatePos() {
        moving = false;

        if (jump) jump();

        if (!inAir)
            if ((!left && !right) || (right && left))
                return;

        float xSpeed = 0;

        if (left) {
            xSpeed -= walkSpeed;
            flipX = width;
            flipW = -1;
        }
        if (right) {
            xSpeed += walkSpeed;
            flipX = 0;
            flipW = 1;
        }

        if (!inAir)
            if (!IsEntityOnFloor(hitbox, lvlData))
                inAir = true;

        if (inAir) {
            if (CanMoveHere(hitbox.x, hitbox.y + airSpeed, hitbox.width, hitbox.height, lvlData)) {
                hitbox.y += airSpeed;
                airSpeed += GRAVITY;
                updateXPos(xSpeed);
            } else {
                hitbox.y = GetEntityYPosUnderRoofOrAboveFloor(hitbox, airSpeed);
                if (airSpeed > 0)
                    resetInAir();
                else
                    airSpeed = fallSpeedAfterCollision;
                updateXPos(xSpeed);
            }
        } else {
            updateXPos(xSpeed);
        }
        moving = true;
    }

    private void jump() {
        if (inAir) return;
        inAir = true;
        airSpeed = jumpSpeed;
        playing.getGame().getAudioPlayer().playEffect(AudioPlayer.JUMP);
    }

    private void resetInAir() {
        inAir = false;
        airSpeed = 0;
    }

    private void updateXPos(float xSpeed) {
        if (CanMoveHere(hitbox.x + xSpeed, hitbox.y, hitbox.width, hitbox.height, lvlData))
            hitbox.x += xSpeed;
        else
            hitbox.x = GetEntityXPosNextToWall(hitbox, xSpeed);
    }

    public void changeHealth(int value) {
        if (value < 0) {
            if (state == HIT) return;
            else newState(HIT);
        }
        currentHealth += value;
        currentHealth = Math.max(Math.min(currentHealth, maxHealth), 0);
    }

    public void changeHealth(int value, Enemy e) {
        if (state == HIT) return;
        changeHealth(value);
        pushBackOffsetDir = UP;
        pushDrawOffset = 0;
        if (e.getHitbox().x < hitbox.x)
            pushBackDir = RIGHT;
        else
            pushBackDir = LEFT;
        applyKnockback(pushBackDir, -1.5f * Game.SCALE, 1.0f);
    }

    public void loadLvlData(int[][] lvlData) {
        this.lvlData = lvlData;
        if (!IsEntityOnFloor(hitbox, lvlData))
            inAir = true;
    }

    public void resetDirBooleans() {
        left = false;
        right = false;
        jump = false;
    }

    public void setAttacking(boolean attacking) { this.attacking = attacking; }

    public boolean isLeft()  { return left; }
    public void setLeft(boolean left)   { this.left = left; }
    public boolean isRight() { return right; }
    public void setRight(boolean right) { this.right = right; }
    public void setJump(boolean jump)   { this.jump = jump; }

    public void resetAll() {
        resetDirBooleans();
        inAir = false;
        attacking = false;
        moving = false;
        state = IDLE;
        currentHealth = maxHealth;
        airSpeed = 0f;
        skill2 = false;
        skill2Checked = false;
        skill3 = false;
        skill3Checked = false;

        hitbox.x = x;
        hitbox.y = y;

        if (!IsEntityOnFloor(hitbox, lvlData))
            inAir = true;
    }

    public void kill() { currentHealth = 0; }

    public int getWalkDir() {
        return (flipW == 1) ? RIGHT : LEFT;
    }

    protected int getFrameCount(int state) {
        return GetSpriteAmount(state, getCharacterName());
    }

    public void mousePressed(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1)
            setSkill2(true);
    }

    public void mouseReleased(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1)
            setSkill2(false);
    }

    public ArrayList<Projectile> getProjectiles() { return projectiles; }

    public void setSkill2(boolean skill2) { this.skill2 = skill2; }

    public void setSkill3(boolean skill3) { this.skill3 = skill3; }

    private void checkSkill2() {
        if (skill2Checked || aniIndex != getSkill2HitFrame()) return;
        skill2Checked = true;
        useSkill2();
    }

    private void checkSkill3() {
        if (skill3Checked || aniIndex != getSkill3HitFrame()) return;
        skill3Checked = true;
        useSkill3();
    }

    public boolean isSkill2Active() { 
        return skill2; 
    }

    private void updateStamina() {
        boolean isIdle = !moving && !inAir; // adjust to your actual idle condition
        float regen = isIdle ? STAMINA_REGEN_IDLE : STAMINA_REGEN_PASSIVE;
        stamina = Math.min(MAX_STAMINA, stamina + regen);
    }

    public boolean useStamina(int cost) {
        if (stamina < cost)
            return false; // blocked
        stamina -= cost;
        return true;
    }

    private int staminaMessageTimer = 0;

    private static final int STAMINA_MESSAGE_DURATION = 120; // frames

    private void showNotEnoughStamina() {
        staminaMessageTimer = STAMINA_MESSAGE_DURATION;
    }
}