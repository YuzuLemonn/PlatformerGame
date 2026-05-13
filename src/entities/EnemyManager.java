package entities;

import entities.enemies.Goblin;
import entities.enemies.Slime;
import gamestates.Playing;
import levels.Level;
import main.Game;
import objects.ObjectManager;
import utilz.LoadSave;

import static utilz.Constants.Directions.LEFT;
import static utilz.Constants.Directions.RIGHT;
import static utilz.Constants.EnemyConstants.*;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import entities.enemies.Zombie;
import entities.bosses.BossDemon;
import entities.bosses.BossGolem;
import entities.bosses.BossWorm;

public class EnemyManager {
    private Playing playing;

    private BufferedImage[][] zombieArr;
    private BufferedImage[][] slimeArr;
    private BufferedImage[][] goblinArr;

    private ArrayList<Zombie> zombies = new ArrayList<>();
    private entities.BaseBoss boss;
    private ArrayList<Slime>  slimes  = new ArrayList<>();
    private ArrayList<Goblin> goblins = new ArrayList<>();

    private boolean bossDeathHandled = false;

    
    private int gold = 0;
    private float damageMultiplier = 1.0f;

    public EnemyManager(Playing playing) {
        this.playing = playing;
        loadEnemyImgs();
    }

    public void loadEnemies(Level level) {
        slimes  = level.getSlimes();
        goblins = level.getGoblins();
        zombies = level.getZombies();
        for (Zombie z : zombies)
            z.setPlaying(playing);

        boss = null;
        int lvlIndex = playing.getLevelManager().getLvlIndex();
        if (lvlIndex == 2) {
            float bossX = level.getLevelData()[0].length / 2f * Game.TILES_SIZE;
            float bossY = (Game.TILES_IN_HEIGHT - 2) * Game.TILES_SIZE - (80 * Game.SCALE);
            boss = new BossWorm(bossX, bossY, playing);
        }

        if (lvlIndex == 4) {
            float bossX = level.getLevelData()[0].length / 2f * Game.TILES_SIZE;
            float bossY = (Game.TILES_IN_HEIGHT - 4) * Game.TILES_SIZE - (120 * Game.SCALE);
            boss = new BossDemon(bossX, bossY, playing);
        }

        if (lvlIndex == 6) {
            float bossX = level.getLevelData()[0].length / 2f * Game.TILES_SIZE;
            float bossY = (Game.TILES_IN_HEIGHT - 4) * Game.TILES_SIZE - (120 * Game.SCALE);
            boss = new BossGolem(bossX, bossY, playing);
            System.out.println("DEBUG: BossGolem spawned at level " + lvlIndex);
        }
        
        for (Slime s : slimes) s.setPlaying(playing);
        for (Goblin g : goblins) g.setPlaying(playing);
    }

    public void update(int[][] lvlData, Player player) {
        for (Slime s : slimes)
            if (s.isActive()) s.update(lvlData, player);

        for (Goblin g : goblins)
            if (g.isActive()) g.update(lvlData, player);

        for (Zombie z : zombies)
            if (z.isActive()) z.update(lvlData, player);

        // OUTSIDE all loops!
        if (boss != null && boss.isActive())
            boss.update(lvlData, player);

        checkCoinDrops(player);
    }

    public void draw(Graphics g, int xLvlOffset) {
        drawSlimes(g, xLvlOffset);
        drawGoblins(g, xLvlOffset);
        drawZombies(g, xLvlOffset);
        if(boss != null && boss.isActive())
            boss.draw(g, xLvlOffset);
    }

    private void drawZombies(Graphics g, int xLvlOffset) {
        for (Zombie z : zombies) {
            if (z.isActive()) {
                int state = z.getState();
                int frame = z.getAniIndex();
                if (zombieArr[state] != null && frame < zombieArr[state].length) {
                    BufferedImage currentFrame = zombieArr[state][frame];

                    int drawWidth = ZOMBIE_WIDTH;
                    int drawHeight = ZOMBIE_HEIGHT;
                    int flipX = z.getWalkDir() == LEFT ? drawWidth : 0;
                    int flipW = z.getWalkDir() == LEFT ? -1 : 1;

                    g.drawImage(currentFrame,
                            (int) z.getHitbox().x - xLvlOffset - ZOMBIE_DRAWOFFSET_X + flipX,
                            (int) z.getHitbox().y - ZOMBIE_DRAWOFFSET_Y,
                            drawWidth * flipW,
                            drawHeight,
                            null);
                }
                z.drawHitbox(g, xLvlOffset);
                z.drawAttackBox(g, xLvlOffset);
            }
        }
    }

    private void drawSlimes(Graphics g, int xLvlOffset) {
        for (Slime s : slimes) {
            if (!s.isActive()) continue;
            int state = s.getState();
            int frame = s.getAniIndex();
            if (slimeArr[state] != null && frame < slimeArr[state].length) {
                g.drawImage(slimeArr[state][frame],
                        (int) s.getHitbox().x - xLvlOffset - SLIME_DRAWOFFSET_X + s.flipX(),
                        (int) s.getHitbox().y - SLIME_DRAWOFFSET_Y + (int) s.getPushDrawOffset(),
                        SLIME_WIDTH * s.flipW(), SLIME_HEIGHT, null);
            }
            s.drawHitbox(g, xLvlOffset);
            s.drawAttackBox(g, xLvlOffset);
        }
    }

    private void drawGoblins(Graphics g, int xLvlOffset) {
        for (Goblin gb : goblins) {
            if (!gb.isActive()) continue;
            int state = gb.getState();
            int frame = gb.getAniIndex();
            if (goblinArr[state] != null && frame < goblinArr[state].length) {
                g.drawImage(goblinArr[state][frame],
                        (int) gb.getHitbox().x - xLvlOffset - GOBLIN_DRAWOFFSET_X + gb.flipX(),
                        (int) gb.getHitbox().y - GOBLIN_DRAWOFFSET_Y + (int) gb.getPushDrawOffset(),
                        GOBLIN_WIDTH * gb.flipW(), GOBLIN_HEIGHT, null);
            }
            gb.drawHitbox(g, xLvlOffset);
            gb.drawAttackBox(g, xLvlOffset);
        }
    }

      //                c.drawHitbox(g, xLvlOffset);
      //                c.drawAttackBox(g, xLvlOffset);

    public void checkSpikesTouched(ObjectManager objectManager) {
        for (Slime  s : slimes) {
            if (s.isActive() && s.getState() != DEAD) {
                objectManager.checkSpikesTouched(s);
            }
        }
        for (Goblin g : goblins) {
            if (g.isActive() && g.getState() != DEAD) {
                objectManager.checkSpikesTouched(g);
            }
        }
        for (Zombie z : zombies) {
            if (z.isActive() && z.getState() != DEAD) {
                objectManager.checkSpikesTouched(z);
        if(boss != null && boss.isActive() && boss.getState() != DEAD)
            objectManager.checkSpikesTouched(boss);
            }
        }
    }

    private void loadEnemyImgs() {
        slimeArr  = new BufferedImage[5][];
        slimeArr[IDLE]    = loadAction(LoadSave.SLIME_WALK,   9,  56);
        slimeArr[RUNNING] = loadAction(LoadSave.SLIME_WALK,   9,  56);
        slimeArr[ATTACK]  = loadAction(LoadSave.SLIME_ATTACK, 7,  56);
        slimeArr[HIT]     = loadAction(LoadSave.SLIME_HIT,    3,  56);
        slimeArr[DEAD]    = loadAction(LoadSave.SLIME_DEATH,  6,  56);

        goblinArr  = new BufferedImage[5][];
        goblinArr[IDLE]    = loadAction(LoadSave.GOBLIN_WALK,   9, 56);
        goblinArr[RUNNING] = loadAction(LoadSave.GOBLIN_WALK,   9, 56);
        goblinArr[ATTACK]  = loadAction(LoadSave.GOBLIN_ATTACK, 3, 56);
        goblinArr[HIT]     = loadAction(LoadSave.GOBLIN_HIT,    3, 56);
        goblinArr[DEAD]    = loadAction(LoadSave.GOBLIN_DEATH,  9, 56);

        zombieArr = new BufferedImage[5][];
        zombieArr[IDLE]    = loadAction(LoadSave.ZOMBIE_WALK,   6,  56);
        zombieArr[RUNNING] = loadAction(LoadSave.ZOMBIE_WALK,   6,  56);
        zombieArr[ATTACK]  = loadAction(LoadSave.ZOMBIE_ATTACK, 5,  56);
        zombieArr[HIT]     = loadAction(LoadSave.ZOMBIE_HIT,    3,  56);
        zombieArr[DEAD]    = loadAction(LoadSave.ZOMBIE_DEATH,  11, 56);
    }

    public entities.BaseBoss getBoss() { return boss; }

    

    private BufferedImage[] loadAction(String path, int frameCount, int height) {
        BufferedImage sheet = LoadSave.GetSpriteAtlas(path);
        if (sheet == null) return new BufferedImage[frameCount];
        int frameWidth = sheet.getWidth() / frameCount;
        BufferedImage[] frames = new BufferedImage[frameCount];
        for (int i = 0; i < frameCount; i++)
            frames[i] = sheet.getSubimage(i * frameWidth, 0, frameWidth, height);
        return frames;
    }

    public void checkEnemyHit(Rectangle2D.Float attackBox, int damage) {
    for (Slime s : slimes)
        if (s.isActive() && s.getState() != DEAD && s.getState() != HIT)
            if (attackBox.intersects(s.getHitbox())) {
                s.hurt(damage, playing.getPlayer().getWalkDir());
                return;
            }
    for (Goblin g : goblins)
        if (g.isActive() && g.getState() != DEAD && g.getState() != HIT)
            if (attackBox.intersects(g.getHitbox())) {
                g.hurt(damage, playing.getPlayer().getWalkDir());
                return;
            }
    for (Zombie z : zombies)
        if (z.isActive() && z.getState() != DEAD && z.getState() != HIT)
            if (attackBox.intersects(z.getHitbox())) {
                z.hurt(damage, playing.getPlayer().getWalkDir());
                return;
            }
    if (boss != null && boss.isActive() && boss.getState() != DEAD)
        if (attackBox.intersects(boss.getHitbox()))
            boss.hurt(damage, playing.getPlayer().getWalkDir());
    }

    public void checkEnemyHitByProjectile(entities.Projectile proj) {
        if (!proj.isActive()) return;
        int knockDir = (proj.getDir() == 1) ? RIGHT : LEFT;

        int damage = (int)(proj.getDamage() * playing.getPlayer().getDamageMultiplier());

        for (Slime s : slimes){
            if (s.isActive() && s.getState() != DEAD && s.getState() != HIT){
                if (proj.getHitbox().intersects(s.getHitbox())) {
                    s.hurt(damage, knockDir);
                    proj.setActive(false);
                    return;
                }
            }
        }

        for (Goblin g : goblins){
            if (g.isActive() && g.getState() != DEAD && g.getState() != HIT){
                if (proj.getHitbox().intersects(g.getHitbox())) {
                    g.hurt(damage, knockDir);
                    proj.setActive(false);
                    return;
                }
            }
        }

        for (Zombie z : zombies){
            if (z.isActive() && z.getState() != DEAD && z.getState() != HIT){
                if (proj.getHitbox().intersects(z.getHitbox())) {
                    z.hurt(damage, knockDir);
                    proj.setActive(false);
                    return;
                }
            }
        }

        if (boss != null && boss.isActive() && boss.getState() != DEAD && boss.getState() != HIT) {
            if (proj.getHitbox().intersects(boss.getHitbox())) {
                boss.hurt(damage, knockDir);
                proj.setActive(false);
            }
        }
    }

    public boolean areAllEnemiesCleared() {
        for (Slime  s : slimes) {
            if (s.isActive()) {
                return false;
            }
        }
        for (Goblin g : goblins) {
            if (g.isActive()) {
                return false;
            }
        }
        for (Zombie z : zombies) {
            if (z.isActive()) {
                return false;
            }
        }
        if (boss != null && boss.isActive()) {
            return false;
        }
        return true;
    }

    public void resetAllEnemies() {
        for (Slime  s : slimes) s.resetEnemy();
        for (Goblin g : goblins) g.resetEnemy();
        for (Zombie z : zombies) z.resetEnemy();
        bossDeathHandled = false;
        loadEnemies(playing.getLevelManager().getCurrentLevel());
    }

    public int getGold() { 
        return gold; 
    }
    public void addGold(int amount) { 
        gold += amount; 
    }
    public boolean spendGold(int amount) {
        if (gold < amount) return false;
        gold -= amount;
        return true;
    }

    public float getDamageMultiplier() { 
        return damageMultiplier; 

    }
    public void increaseDamage(float amount) { 
        damageMultiplier += amount; 
    }

    private void checkCoinDrops(Player player) {
        for (Slime s : slimes)
            if (!s.isActive() && !s.isCoinDropped()) {
                player.addGold(s.getCoinValue());
                s.markCoinDropped();
            }
        for (Goblin g : goblins)
            if (!g.isActive() && !g.isCoinDropped()) {
                player.addGold(g.getCoinValue());
                g.markCoinDropped();
            }
        for (Zombie z : zombies)
            if (!z.isActive() && !z.isCoinDropped()) {
                player.addGold(z.getCoinValue());
                z.markCoinDropped();
            }
        if (boss != null && !boss.isActive() && !boss.isCoinDropped()) {
            player.addGold(boss.getCoinValue());
            boss.markCoinDropped();
        }
        if (boss != null && !boss.isActive() && !bossDeathHandled) {
            bossDeathHandled = true;
            playing.onBossDefeated();
        }
    }

    public ArrayList<Enemy> getAllEnemies() {
        ArrayList<Enemy> all = new ArrayList<>();
        all.addAll(slimes);
        all.addAll(goblins);
        all.addAll(zombies);
        if (boss != null) all.add(boss);
        return all;
    }
}
