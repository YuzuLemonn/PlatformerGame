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
    private int checkpointGold = 0;
    private int checkpointPotions = 0;
    private int potionCount = 0;
    private int potionHealAmount = 50;
    private int gold = 0;
    private float damageMultiplier = 1.0f;
    private int manaPotionCount = 0;
    private int manaRestoreAmount = 40;
    private int checkpointManaPotions = 0;

    // Icons
    private BufferedImage goldIcon;
    private BufferedImage healthIcon;
    private BufferedImage manaIcon;
    private int iconSize = (int)(20 * Game.SCALE);

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
    private int burnTicksLeft = 0;
    private int burnTimer = 0;
    private static final int BURN_INTERVAL = (int)(0.5f * 200); // 0.5s at 200 UPS
    private static final int BURN_DAMAGE = 10;


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
    protected float stamina = MAX_STAMINA;
    private int staminaBarWidth  = (int)(105 * Game.SCALE);
    private int staminaBarHeight = (int)(4   * Game.SCALE);
    private int staminaBarXStart = (int)(44  * Game.SCALE);
    private int staminaBarYStart = (int)(33  * Game.SCALE);

    protected int flipX = 0;
    protected int flipW = 1;

    private int iFramesTimer = 0;
    private static final int IFRAME_DURATION = 100;

    private boolean attackChecked;
    protected Playing playing;
    private boolean skill2Checked, skill3Checked;
    protected String playerClass;


    public String getPlayerClass() {
        return playerClass;
    }

    public Player(float x, float y, int width, int height, Playing playing) {
        super(x, y, width, height);
        this.playing = playing;
        this.state = IDLE;
        this.maxHealth = 100;
        this.currentHealth = maxHealth;
        this.walkSpeed = Game.SCALE * 1.0f;
        loadIcons();
    }

    private void loadIcons() {
        goldIcon   = LoadSave.GetSpriteAtlas(LoadSave.GOLD_ICON);
        healthIcon = LoadSave.GetSpriteAtlas(LoadSave.HEALTH_ICON);
        manaIcon   = LoadSave.GetSpriteAtlas(LoadSave.MANA_ICON);
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
        updateBurn();

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
            case SKILL2:   return skill2Frames;
            case SKILL3:   return skill3Frames;
            default:      return idleFrames;
        }
    }

    private void drawUI(Graphics g) {
        // Status bar background
        g.drawImage(statusBarImg, statusBarX, statusBarY, statusBarWidth, statusBarHeight, null);

        // Health bar
        g.setColor(Color.red);
        g.fillRect(healthBarXStart + statusBarX, healthBarYStart + statusBarY, healthWidth, healthBarHeight);

        // Stamina bar
        int staminaWidth = (int)((stamina / (float) MAX_STAMINA) * staminaBarWidth);
        g.setColor(Color.blue);
        g.fillRect(staminaBarXStart + statusBarX, staminaBarYStart + statusBarY, staminaWidth, staminaBarHeight);

        // Not enough stamina message
        if (staminaMessageTimer > 0) {
            String msg = "Not enough stamina!";
            g.setColor(new Color(255, 220, 0));
            g.setFont(new Font("Monospaced", Font.BOLD, (int)(9 * Game.SCALE)));
            FontMetrics fm = g.getFontMetrics();
            int msgX = (Game.GAME_WIDTH - fm.stringWidth(msg)) / 2;
            int msgY = (Game.GAME_HEIGHT / 2);
            g.drawString(msg, msgX, msgY);
        }

        // Burn overlay
        if (isBurning()) {
            g.setColor(new Color(255, 100, 0, 150));
            g.fillRect(healthBarXStart + statusBarX, healthBarYStart + statusBarY, healthWidth, healthBarHeight);
            g.setColor(new Color(255, 50, 0));
            g.setFont(new Font("Monospaced", Font.BOLD, (int)(8 * Game.SCALE)));
            g.drawString("BURNING!", statusBarX + healthBarXStart, statusBarY + healthBarYStart - 5);
        }

        // --- Icon HUD top right ---
        Graphics2D g2d = (Graphics2D) g;
        g2d.setFont(new Font("Monospaced", Font.BOLD, (int)(8 * Game.SCALE)));
        FontMetrics fm = g2d.getFontMetrics();

        int iconY    = (int)(8  * Game.SCALE);
        int textYOff = (int)(14 * Game.SCALE);
        int colGap   = (int)(6  * Game.SCALE);
        int rowGap   = iconSize + (int)(4 * Game.SCALE);

        // Gold row
        String goldText = "" + gold;
        int goldX = Game.GAME_WIDTH - fm.stringWidth(goldText) - iconSize - colGap - (int)(10 * Game.SCALE);
        if (goldIcon != null)
            g2d.drawImage(goldIcon, goldX, iconY, iconSize, iconSize, null);
        g2d.setColor(Color.YELLOW);
        g2d.drawString(goldText, goldX + iconSize + colGap, iconY + textYOff);

        // HP potion row
        String hpText = "" + potionCount;
        int hpX = Game.GAME_WIDTH - fm.stringWidth(hpText) - iconSize - colGap - (int)(10 * Game.SCALE);
        if (healthIcon != null)
            g2d.drawImage(healthIcon, hpX, iconY + rowGap, iconSize, iconSize, null);
        g2d.setColor(new Color(255, 100, 100));
        g2d.drawString(hpText, hpX + iconSize + colGap, iconY + rowGap + textYOff);

        // Mana potion row
        String mpText = "" + manaPotionCount;
        int mpX = Game.GAME_WIDTH - fm.stringWidth(mpText) - iconSize - colGap - (int)(10 * Game.SCALE);
        if (manaIcon != null)
            g2d.drawImage(manaIcon, mpX, iconY + rowGap * 2, iconSize, iconSize, null);
        g2d.setColor(new Color(100, 150, 255));
        g2d.drawString(mpText, mpX + iconSize + colGap, iconY + rowGap * 2 + textYOff);
    }

    private void updateAnimationTick() {
        if (iFramesTimer > 0) {
            iFramesTimer--;
        }

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
            if (state != SKILL3) {
                state = SKILL3;
                aniIndex = 0;
                aniTick = 0;
            }
            return;
        } else if (skill2) {
            if (state != SKILL2) {
                state = SKILL2;
                aniIndex = 0;
                aniTick = 0;
            }
            return;
        } else if (attacking) {
            if (state != ATTACK) {
                state = ATTACK;
                aniIndex = 0;
                aniTick = 0;
            }
            return;
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
            if (state == HIT || iFramesTimer > 0) {
                return;
            }
            else {
                newState(HIT);
                iFramesTimer = IFRAME_DURATION;
            }
        }
        currentHealth += value;
        currentHealth = Math.max(Math.min(currentHealth, maxHealth), 0);
    }

    public void changeHealth(int value, Enemy e) {
        if (state == HIT || iFramesTimer > 0) {
            return;
        }
        changeHealth(value);
        iFramesTimer = IFRAME_DURATION;
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
        inAir         = false;
        attacking     = false;
        moving        = false;
        state         = IDLE;
        currentHealth = maxHealth;
        airSpeed      = 0f;
        skill2        = false;
        skill2Checked = false;
        skill3        = false;
        skill3Checked = false;

        stamina              = MAX_STAMINA;
        staminaMessageTimer  = 0;
        burnTicksLeft        = 0;

        iFramesTimer = 0;

        hitbox.x = x;
        hitbox.y = y;

        restoreCheckpoint();

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

    public void setPlayerClass(String playerClass) {
        this.playerClass = playerClass;
    }

    public int getGold() { return gold; }
    public void addGold(int amount) { gold += amount; }
    public boolean spendGold(int amount) {
        if (gold < amount) return false;
        gold -= amount;
        return true;
    }

    public float getDamageMultiplier() { return damageMultiplier; }
    public void increaseDamage(float amount) { damageMultiplier += amount; }

    public void addPotion()  { potionCount++; }
    public int getPotionCount() { return potionCount; }

    public void usePotion() {
        if (potionCount <= 0) return;
        potionCount--;
        changeHealth(potionHealAmount);
    }

    public void addManaPotion()        { manaPotionCount++; }
    public int  getManaPotionCount()   { return manaPotionCount; }

    public void useManaPotion() {
        if (manaPotionCount <= 0) return;
        manaPotionCount--;
        stamina = Math.min(MAX_STAMINA, stamina + manaRestoreAmount);
        System.out.println("[PLAYER] Used mana potion! Stamina: " + stamina + " Left: " + manaPotionCount);
    }

    public void saveCheckpoint() {
        checkpointGold    = gold;
        checkpointPotions = potionCount;
        checkpointManaPotions = manaPotionCount;
    }

    public void restoreCheckpoint() {
        gold        = checkpointGold;
        potionCount = checkpointPotions;
        manaPotionCount  = checkpointManaPotions;
    }

    private void updateStamina() {
        boolean isIdle = !moving && !inAir; // adjust to your actual idle condition
        float regen = isIdle ? STAMINA_REGEN_IDLE : STAMINA_REGEN_PASSIVE;
        stamina = Math.min(MAX_STAMINA, stamina + regen);

        if (staminaMessageTimer > 0)
            staminaMessageTimer--;
    }

    public boolean useStamina(int cost) {
        if (stamina < cost) {
            showNotEnoughStamina();
            return false; // blocked
        }
        stamina -= cost;
        return true;
    }

    private int staminaMessageTimer = 0;

    private static final int STAMINA_MESSAGE_DURATION = 120; // frames

    private void showNotEnoughStamina() {
        staminaMessageTimer = STAMINA_MESSAGE_DURATION;
    }

    public void applyBurn(int ticks) {
        burnTicksLeft = ticks;
        burnTimer = 0;
    }

    public boolean isBurning() {
        return burnTicksLeft > 0;
    }

    private void updateBurn() {
        if (burnTicksLeft <= 0) return;
        burnTimer++;
        if (burnTimer >= BURN_INTERVAL) {
            burnTimer = 0;
            burnTicksLeft--;
            changeHealth(-BURN_DAMAGE);
        }
    }

    public void setStamina(float stamina) {
        this.stamina = Math.max(0, Math.min(stamina, MAX_STAMINA));
    }

    public void clearBurn() {
        burnTicksLeft = 0;
        burnTimer = 0;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public float getMaxStamina() {
        return MAX_STAMINA;
    }
}