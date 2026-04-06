package entities;


import gamestates.Playing;
import main.Game;
import utilz.LoadSave;


import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import static utilz.Constants.*;
import static utilz.Constants.PlayerConstants.*;
import static utilz.HelpMethods.CanMoveHere;
import static utilz.HelpMethods.*;

public class Player extends Entity{
    private BufferedImage[] idleFrames;
    private BufferedImage[] runFrames;
    private BufferedImage[] jumpFrames;
    private BufferedImage[] attackFrames;

    private boolean moving = false, attacking = false;
    private boolean left ,right, jump;

    private int[][] lvlData;
    private float xDrawOffset = 21 * Game.SCALE;
    private float yDrawOffset = 10 * Game.SCALE;

    //for jumping & gravity
    private float jumpSpeed = -2.25f * Game.SCALE;
    private float fallSpeedAfterCollision = 0.5f * Game.SCALE;


    //Status bar UI
    private BufferedImage statusBarImg;

    private int statusBarWidth = (int)(192 * Game.SCALE);
    private int statusBarHeight = (int)(58 * Game.SCALE);
    private int statusBarX = (int)(10 * Game.SCALE);
    private int statusBarY = (int)(10 * Game.SCALE);

    private int healthBarWidth = (int)(150 * Game.SCALE);
    private int healthBarHeight = (int)(4 * Game.SCALE);
    private int healthBarXStart = (int) (34 * Game.SCALE);
    private int healthBarYStart = (int)(14 * Game.SCALE);


    private int healthWidth = healthBarWidth;

    //AttackBox


    private int flipX = 0;
    private int flipW = 1;

    private boolean attackChecked;
    private Playing playing;

    public Player(float x, float y, int width, int height, Playing playing) {
        super(x, y, width, height);
        this.playing = playing;
        this.state = IDLE;
        this.maxHealth = 100;
        this.currentHealth = maxHealth;
        this.walkSpeed = Game.SCALE * 1.0f;
        loadAnimations();
        initHitbox(20 ,27 );
        initAttackbox();
    }

    public void setSpawn(Point spawn){
        this.x = spawn.x;
        this.y = spawn.y;
        hitbox.x = x;
        hitbox.y = y;
    }

    private void initAttackbox() {
        attackBox = new Rectangle2D.Float(x, y, (int)(20 * Game.SCALE), (int)(20 * Game.SCALE));
    }

    public void update(){
        updateHealthBar();
        
        if(currentHealth <= 0){
            playing.setGameOver(true);
            return;
        }

        updateAttackBox();

        updatePos();
        if(attacking){
            checkAttack();
        }
        updateAnimationTick();
        setAnimation();
    }

    private void checkAttack() {
        if(attackChecked || aniIndex != 1){
            return;
        }
        attackChecked = true;
        playing.checkEnemyHit(attackBox);

    }

    private void updateAttackBox() {
        if(right){
            attackBox.x = hitbox.x + hitbox.width + (int)(Game.SCALE * 10);
        }else if(left){
            attackBox.x = hitbox.x - hitbox.width - (int)(Game.SCALE * 10);
        }

        attackBox.y = hitbox.y + (Game.SCALE * 10);
    }

    private void updateHealthBar() {
        healthWidth = (int)((currentHealth / (float)maxHealth) * healthBarWidth);
    }

    public void render(Graphics g, int lvlOffset){
        BufferedImage[] currentFrames = getCurrentFrames();
        if(currentFrames != null && aniIndex < currentFrames.length){
            g.drawImage(currentFrames[aniIndex],
                    (int)(hitbox.x - xDrawOffset) - lvlOffset + flipX,
                    (int)(hitbox.y - yDrawOffset),
                    width * flipW, height, null);
        }
        drawUI(g);
    }

    private BufferedImage[] getCurrentFrames(){
        switch(state){
            case RUNNING: return runFrames;
            case JUMP:    return jumpFrames;
            case ATTACK:  return attackFrames;
            default:      return idleFrames;
        }
    }

    private void drawUI(Graphics g) {
        g.drawImage(statusBarImg, statusBarX, statusBarY, statusBarWidth, statusBarHeight, null);
        g.setColor(Color.red);
        g.fillRect(healthBarXStart + statusBarX, healthBarYStart + statusBarY, healthWidth, healthBarHeight);
    }

    private void updateAnimationTick() {
        aniTick++;
        if(aniTick >= ANI_SPEED){
            aniTick = 0;
            aniIndex++;
            if(aniIndex >= GetSpriteAmount(state)){
                aniIndex = 0;
                attacking = false;
                attackChecked = false;
            }
        }
    }

    private void setAnimation() {
        int startAni = state;

        if(moving){
            state = RUNNING;
        }else {
            state = IDLE;
        }

        if(inAir){
            if(airSpeed < 0){
                state = JUMP;
            }else{
                state = FALLING;
            }
        }

        if(attacking){
            state = ATTACK;
            if(startAni != ATTACK){
                aniIndex = 1;
                aniTick = 0;
                return;
            }
        }

        if(startAni != state){
            resetAniTick();
        }
    }

    private void resetAniTick() {
        aniTick = 0;
        aniIndex = 0;
    }

    private void updatePos(){
        moving = false;

        if(jump){
            jump();
        }
//        if(!left && !right && !inAir){
//            return;
//        }
        if(!inAir){
            if((!left && !right) || (right && left)){
                return;
            }
        }

        float xSpeed = 0;

        if(left){
            xSpeed -= walkSpeed;
            flipX = width;
            flipW = -1;
        }
        if(right){
            xSpeed += walkSpeed;
            flipX = 0;
            flipW = 1;
        }
        if(!inAir){
            if(!IsEntityOnFloor(hitbox, lvlData)){
                inAir = true;
            }
        }

        if(inAir){
            if(CanMoveHere(hitbox.x, hitbox.y + airSpeed, hitbox.width, hitbox.height, lvlData)){
                hitbox.y += airSpeed;
                airSpeed += GRAVITY;
                updateXPos(xSpeed);
            }else{
                hitbox.y = GetEntityYPosUnderRoofOrAboveFloor(hitbox, airSpeed);
                if(airSpeed > 0) {
                    resetInAir();
                }else{
                    airSpeed = fallSpeedAfterCollision;
                }
                updateXPos(xSpeed);
            }
        }else{
            updateXPos(xSpeed);
        }
        moving = true;

    }

    private void jump() {
        if(inAir){
            return;
        }

        inAir = true;
        airSpeed = jumpSpeed;
    }

    private void resetInAir() {
        inAir = false;
        airSpeed = 0;
    }

    private void updateXPos(float xSpeed) {
        if(CanMoveHere(hitbox.x + xSpeed, hitbox.y, hitbox.width, hitbox.height, lvlData)){
            hitbox.x += xSpeed;
        }else{
            hitbox.x = GetEntityXPosNextToWall(hitbox, xSpeed);
        }
    }

    public void changeHealth(int value){
        currentHealth += value;

        if(currentHealth <= 0){
            currentHealth = 0;
            //gameOver();
        }else if(currentHealth >= maxHealth){
            currentHealth = maxHealth;
        }
    }

    private void loadAnimations() {
        idleFrames   = loadActionFrames("IdleAni",  GetSpriteAmount(IDLE));
        runFrames    = loadActionFrames("RunAni",    GetSpriteAmount(RUNNING));
        jumpFrames   = loadActionFrames("JumpAni",   GetSpriteAmount(JUMP));
        attackFrames = loadActionFrames("Attack1",   GetSpriteAmount(ATTACK));

        statusBarImg = LoadSave.GetSpriteAtlas(LoadSave.STATUS_BAR);
    }

    private BufferedImage[] loadActionFrames(String action, int frameCount){
        BufferedImage sheet = LoadSave.GetBrawlerSprite(action);
        if(sheet == null){
            System.out.println("Failed to load: " + action + "_Brawler.png");
            return new BufferedImage[frameCount];
        }
        int frameWidth = sheet.getWidth() / frameCount;
        int frameHeight = sheet.getHeight();
        BufferedImage[] frames = new BufferedImage[frameCount];
        for(int i = 0; i < frameCount; i++){
            frames[i] = sheet.getSubimage(i * frameWidth, 0, frameWidth, frameHeight);
        }
        return frames;
    }

    public void loadLvlData(int[][] lvlData){
        this.lvlData = lvlData;
        if(!IsEntityOnFloor(hitbox, lvlData)){
            inAir = true;
        }
    }

    public void resetDirBooleans(){
        left = false;
        right = false;
    }

    public void setAttacking(boolean attacking){
        this.attacking = attacking;
    }

    public boolean isLeft(){
        return left;
    }

    public void setLeft(boolean left){
        this.left = left;
    }

    public boolean isRight(){
        return right;
    }

    public void setRight(boolean right){
        this.right = right;
    }

    public void setJump(boolean jump){
        this.jump = jump;
    }

    public void resetAll() {
        resetDirBooleans();
        inAir = false;
        attacking = false;
        moving = false;
        state = IDLE;
        currentHealth = maxHealth;

        hitbox.x = x;
        hitbox.y = y;

        if(!IsEntityOnFloor(hitbox, lvlData)) {
            inAir = true;
        }
    }
}
