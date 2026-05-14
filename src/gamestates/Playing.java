package gamestates;

import entities.EnemyManager;
import entities.Player;
import entities.Projectile;
import levels.Level;
import levels.LevelManager;
import main.Game;
import objects.ObjectManager;
import ui.*;
import utilz.LoadSave;
import entities.NPC;

import java.util.ArrayList;
import audio.AudioPlayer;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import static utilz.Constants.PlayerConstants.MAX_STAMINA;

public class Playing extends State implements Statemethods {
    private Player player;
    private LevelManager levelManager;
    private EnemyManager enemyManager;
    private ObjectManager objectManager;
    private PauseOverlay pauseOverlay;
    private GameOverOverlay gameOverOverlay;
    private GameCompletedOverlay gameCompletedOverlay;
    private LevelCompletedOverlay levelCompletedOverlay;
    private boolean paused = false;
    private boolean debugMode = false;

    private int xLvlOffset;
    private int leftBorder  = (int)(0.2 * Game.GAME_WIDTH);
    private int rightBorder = (int)(0.8 * Game.GAME_WIDTH);
    private int maxLvlOffsetX;

    private BufferedImage[] backgroundImgs;

    private boolean gameOver;
    private boolean lvlCompleted;
    private boolean gameCompleted;
    private boolean playerDying;

    private ArrayList<NPC> npcs = new ArrayList<>();
    private NPC activeNPC = null;
    private DialogueOverlay dialogueOverlay;
    private LevelNameOverlay levelNameOverlay;
    private boolean dialogueActive = false;
    private boolean shopActive = false;
    private boolean allEnemiesCleared = false;

    private BossCutscene bossCutscene = null;
    private boolean cutsceneActive = false;

    public Playing(Game game) {
        super(game);
        initClasses();

        backgroundImgs = new BufferedImage[]{
                LoadSave.GetSpriteAtlas("playing_bg_village.png"),
                LoadSave.GetSpriteAtlas("playing_bg_img1.png"),
                LoadSave.GetSpriteAtlas("playing_bg_img1.png"),
                LoadSave.GetSpriteAtlas("playing_bg_img2.png"),
                LoadSave.GetSpriteAtlas("playing_bg_img2.png"),
                LoadSave.GetSpriteAtlas("playing_bg_img3.png"),
                LoadSave.GetSpriteAtlas("playing_bg_img3.png")
        };

        calcLvlOffset();
        loadStartLevel();
    }

    public void loadNextLevel() {
        player.saveCheckpoint();
        saveShopCheckpoints();
        resetAll();
        levelManager.loadNextLevel();
        player.setSpawn(levelManager.getCurrentLevel().getPlayerSpawn());
        player.loadLvlData(levelManager.getCurrentLevel().getLevelData()); 
        player.resetAll(); 
        game.getAudioPlayer().setLevelSong(levelManager.getLvlIndex());
        npcs.clear();
        initNPCs();
        xLvlOffset = 0;
        calcLvlOffset();
        levelNameOverlay.start(levelManager.getLvlIndex());
        tryStartBossCutscene();
    }

    private void loadStartLevel() {
        enemyManager.loadEnemies(levelManager.getCurrentLevel());
        objectManager.loadObjects(levelManager.getCurrentLevel());
        levelNameOverlay.start(levelManager.getLvlIndex());
    }

    public void setPlayer(Player p) {
    this.player = p;
    player.setSpawn(levelManager.getCurrentLevel().getPlayerSpawn());
    player.loadLvlData(levelManager.getCurrentLevel().getLevelData());
    player.setAttacking(false);
    player.saveCheckpoint();
}

    private void calcLvlOffset() {
        maxLvlOffsetX = levelManager.getCurrentLevel().getLvlOffset();
    }

    private void initClasses() {
        levelManager = new LevelManager(game);
        enemyManager = new EnemyManager(this);
        objectManager = new ObjectManager(this);

        player = new entities.players.Brawler(200, 200,
                (int)(64 * Game.SCALE), (int)(40 * Game.SCALE), this);
        player.loadLvlData(levelManager.getCurrentLevel().getLevelData());
        player.setSpawn(levelManager.getCurrentLevel().getPlayerSpawn());

        pauseOverlay          = new PauseOverlay(this);
        gameOverOverlay       = new GameOverOverlay(this);
        levelCompletedOverlay = new LevelCompletedOverlay(this);
        gameCompletedOverlay  = new GameCompletedOverlay(this);
        dialogueOverlay       = new DialogueOverlay(this);
        levelNameOverlay      = new LevelNameOverlay();

        initNPCs();
    }

    private void initNPCs() {
        String playerName = BossCutscene.getPlayerName();

        for (Point p : levelManager.getCurrentLevel().getMotherSpawns()) {
            NPC npc = new NPC(
                    p.x, p.y,
                    "Mother",
                    new String[]{
                            "Dear, come back inside. The tunnels aren't safe.",
                            playerName + ": I'm not afraid of the dark anymore.",
                            "That's exactly why I'm afraid for you.",
                            playerName + ": I want to see the surface. I want to know if the stories are true.",
                            "The surface is filled with danger. Creatures, wars... things we don't understand.",
                            playerName + ": Down here feels like a cage.",
                            "...If you leave, there's no promise you'll return.",
                            playerName + ": I know. But I have to try.",
                            "..."
                    },
                    this
            );
            npc.loadLvlData(levelManager.getCurrentLevel().getLevelData());
            npcs.add(npc);
        }

        for (Point p : levelManager.getCurrentLevel().getMerchantSpawns()) {
            if (levelManager.getLvlIndex() == 1) {
                NPC npc = new NPC(
                        p.x, p.y,
                        "Merchant Raineir",
                        new String[]{
                                "Raineir: Supplies for a journey upward? Not many try.",
                                playerName + ": I don't plan to turn back.",
                                "Raineir: Then take these supplies. You'll need them.",
                                playerName + ": Thank you.",
                                "Raineir: May the stone guide your path."
                        },
                        this
                );
                npc.loadLvlData(levelManager.getCurrentLevel().getLevelData());
                npc.setGiftOnEnd(2, 2);
                npcs.add(npc);
            } else {
                NPC npc = new NPC(
                        p.x, p.y,
                        "Mysterious Merchant",
                        new String[]{
                                "Welcome to my shop!",
                                "I have wares if you have coin."
                        },
                        this
                );
                npc.loadLvlData(levelManager.getCurrentLevel().getLevelData());
                npc.setShopkeeper(true);
                npcs.add(npc);
            }
        }
    }

    @Override
    public void update() {
        if (cutsceneActive && bossCutscene != null) {
        bossCutscene.update();
        return;
        }

        levelNameOverlay.update();

        if (paused)
            pauseOverlay.update();
        else if (lvlCompleted)
            levelCompletedOverlay.update();
        else if (gameCompleted)
            gameCompletedOverlay.update();
        else if (gameOver)
            gameOverOverlay.update();
        else if (playerDying)
            player.update();
        else if (dialogueActive) {
            for (NPC npc : npcs) npc.update();
            dialogueOverlay.update(activeNPC);
        } else {
            levelManager.update();
            player.update();
            player.updateProjectiles(levelManager.getCurrentLevel().getLevelData());

            if (enemyManager.getBoss() != null && enemyManager.getBoss().isActive()) {
                for (Projectile p : enemyManager.getBoss().getProjectiles()) {
                    if (p.isActive()) {
                        p.update(levelManager.getCurrentLevel().getLevelData());
                    }
                }
            }

            allEnemiesCleared = enemyManager.areAllEnemiesCleared();
            enemyManager.update(levelManager.getCurrentLevel().getLevelData(), player);
            objectManager.updatePortals(allEnemiesCleared);
            objectManager.checkSpikesTouched(player);
            enemyManager.checkSpikesTouched(objectManager);

            checkCloseToBorder();
            for (NPC npc : npcs) npc.update();

            for (entities.Projectile proj : player.getProjectiles())
                if (proj.isActive())
                    enemyManager.checkEnemyHitByProjectile(proj);
        }
    }

    public void checkSpikesTouched(Player p) {
        objectManager.checkSpikesTouched(p);
    }

    private void checkCloseToBorder() {
        int playerX = (int) player.getHitbox().x;
        int diff = playerX - xLvlOffset;

        if (diff > rightBorder)
            xLvlOffset += diff - rightBorder;
        else if (diff < leftBorder)
            xLvlOffset += diff - leftBorder;

        xLvlOffset = Math.max(Math.min(xLvlOffset, maxLvlOffsetX), 0);
    }

    @Override
    public void draw(Graphics g) {
        g.drawImage(backgroundImgs[levelManager.getLvlIndex()], 0, 0, Game.GAME_WIDTH, Game.GAME_HEIGHT, null);

        levelManager.draw(g, xLvlOffset);
        objectManager.draw(g, xLvlOffset);

        for (NPC npc : npcs) npc.draw(g, xLvlOffset);

        enemyManager.draw(g, xLvlOffset);
        player.render(g, xLvlOffset);
        player.renderProjectiles(g, xLvlOffset);

        if (enemyManager.getBoss() != null && enemyManager.getBoss().isActive()) {
            for (Projectile p : enemyManager.getBoss().getProjectiles()) {
                if (p.isActive()) {
                    p.render(g, xLvlOffset);
                }
            }
        }
        
        if (enemyManager.getBoss() != null && enemyManager.getBoss().isActive())
            enemyManager.getBoss().drawBossBar(g);
        
        // Dialogue and shop overlays
        if (dialogueActive && activeNPC != null)
            dialogueOverlay.draw(g, activeNPC);

        if (debugMode) {
            g.setColor(new Color(0, 0, 0, 150));
            g.fillRect(0, 0, 100, 40);
            g.setColor(Color.YELLOW);
            g.setFont(new Font("Monospaced", Font.BOLD, 12));
            g.drawString("DEBUG MODE", 5, 15);
            g.drawString("Ctrl+B = Boss3", 5, 30);
        }

        if (shopActive && activeNPC != null)
            activeNPC.drawShop(g);

        levelNameOverlay.draw(g);

        if (paused) {
            g.setColor(new Color(0, 0, 0, 150));
            g.fillRect(0, 0, Game.GAME_WIDTH, Game.GAME_HEIGHT);
            pauseOverlay.draw(g);
        } else if (gameOver)
            gameOverOverlay.draw(g);
        else if (lvlCompleted)
            levelCompletedOverlay.draw(g);
        else if (gameCompleted)
            gameCompletedOverlay.draw(g);

        if (cutsceneActive && bossCutscene != null) {
            bossCutscene.draw(g);
            return;
        }
    }

    public void resetAll() {
        allEnemiesCleared = false;
        gameOver          = false;
        paused            = false;
        lvlCompleted      = false;
        playerDying       = false;
        gameCompleted     = false;
        dialogueActive    = false;
        shopActive        = false;
        activeNPC         = null;
        player.resetAll();
        restoreShopCheckpoints();
        enemyManager.resetAllEnemies();
        objectManager.resetAllObjects();
        for (NPC npc : npcs) npc.endDialogue();
        cutsceneActive = false;
        bossCutscene   = null;
    }

    public void setGameOver(boolean gameOver)       { this.gameOver = gameOver; }
    public void setGameCompleted()                  { gameCompleted = true; }
    public void resetGameCompleted()                { gameCompleted = false; }
    public void setPlayerDying(boolean playerDying) { this.playerDying = playerDying; }

    public void checkEnemyHit(Rectangle2D.Float attackBox) {
        int baseDamage = getPlayerBaseDamage();   // per-class base
        int damage = (int)(baseDamage * player.getDamageMultiplier());
        enemyManager.checkEnemyHit(attackBox, damage);
    }

    private int getPlayerBaseDamage() {
        switch (player.getPlayerClass()) {
            case "BRAWLER"  -> { return 15; }  // hits hard
            case "MAGE"     -> { return 8;  }  // relies on projectiles
            case "ASSASSIN" -> { return 12; }  // fast but moderate
            default         -> { return 10; }
        }
    }

    public void checkEnemyHit(Rectangle2D.Float attackBox, int damage) {
        enemyManager.checkEnemyHit(attackBox, damage);
    }

    public void mouseDragged(MouseEvent e) {
        if (!gameOver && paused)
            pauseOverlay.mouseDragged(e);
    }

    @Override public void mouseClicked(MouseEvent e) {}

    @Override
    public void mousePressed(MouseEvent e) {
        if (!gameOver && !paused && !lvlCompleted && !gameCompleted) {
            if (e.getButton() == MouseEvent.BUTTON1)
                player.setAttacking(true);
            else if (e.getButton() == MouseEvent.BUTTON3)
                if (!player.isSkill2Active())
                    player.setSkill2(true);
        }
        if (gameOver)           gameOverOverlay.mousePressed(e);
        else if (paused)        pauseOverlay.mousePressed(e);
        else if (lvlCompleted)  levelCompletedOverlay.mousePressed(e);
        else if (gameCompleted) gameCompletedOverlay.mousePressed(e);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (gameOver)           gameOverOverlay.mouseReleased(e);
        else if (paused)        pauseOverlay.mouseReleased(e);
        else if (lvlCompleted)  levelCompletedOverlay.mouseReleased(e);
        else if (gameCompleted) gameCompletedOverlay.mouseReleased(e);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (gameOver)           gameOverOverlay.mouseMoved(e);
        else if (paused)        pauseOverlay.mouseMoved(e);
        else if (lvlCompleted)  levelCompletedOverlay.mouseMoved(e);
        else if (gameCompleted) gameCompletedOverlay.mouseMoved(e);
    }

    @Override
    public void keyPressed(KeyEvent e) {

        if (cutsceneActive && bossCutscene != null) {
            if (e.getKeyCode() == KeyEvent.VK_E ||
                    e.getKeyCode() == KeyEvent.VK_SPACE) {
                boolean done = bossCutscene.advance();
                if (done) {
                    cutsceneActive = false;
                    bossCutscene   = null;
                }
            }
            return;
        }

        if (debugMode) {
            if (e.getKeyCode() == KeyEvent.VK_B && e.isControlDown()) {
                System.out.println("DEBUG: Ctrl+B detected!"); 
                teleportToBoss3();
            }
        }

        if (gameOver || gameCompleted || lvlCompleted) return;

        switch (e.getKeyCode()) {
            case KeyEvent.VK_A:
            case KeyEvent.VK_D:
            case KeyEvent.VK_SPACE:
            case KeyEvent.VK_R:
                if (!shopActive && !dialogueActive)
                    player.keyPressed(e);
                break;

            case KeyEvent.VK_E:
                if (objectManager.isPlayerAtOpenPortal(player.getHitbox()))
                    game.getStoryManager().onLevelComplete();
                else
                    handleNPCInteract();
                break;

            case KeyEvent.VK_H:
                if (!shopActive && !dialogueActive)
                    player.usePotion();
                break;

            case KeyEvent.VK_G:
            if (!shopActive && !dialogueActive)
                player.useManaPotion();
            break;

            case KeyEvent.VK_1:
            case KeyEvent.VK_2:
            case KeyEvent.VK_3:
            case KeyEvent.VK_4:
            case KeyEvent.VK_5:
                if (shopActive && activeNPC != null)
                    activeNPC.handleShopKey(e.getKeyCode());
                break;

            case KeyEvent.VK_ESCAPE:
                paused = !paused;
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (gameOver || gameCompleted || lvlCompleted) return;

        switch (e.getKeyCode()) {
            case KeyEvent.VK_A:
            case KeyEvent.VK_D:
            case KeyEvent.VK_SPACE:
            case KeyEvent.VK_R:
                player.keyReleased(e);
                break;
        }
    }

    private void handleNPCInteract() {
        if (shopActive && activeNPC != null) {
            shopActive = false;
            activeNPC  = null;
            return;
        }

        // Dialogue active
        if (dialogueActive && activeNPC != null) {
            if (!dialogueOverlay.isTextComplete()) {
                dialogueOverlay.skipToEnd();
                return;
            }

            activeNPC.interact();

            if (!activeNPC.isDialogueActive()) {
                dialogueActive = false;
                if (activeNPC.isShopkeeper()) {
                    activeNPC.openShop();
                    shopActive = true;
                } else {
                    activeNPC = null;
                }
            }
            return;
        }

        for (NPC npc : npcs) {
            if (npc.isPlayerInRange(player.getHitbox())) {
                activeNPC = npc;
                npc.interact();
                dialogueActive = true;
                break;
            }
        }
    }

    public void setLevelCompleted(boolean levelCompleted) {
        game.getAudioPlayer().lvlCompleted();
        game.getStoryManager().onLevelComplete();
        if (levelManager.getLvlIndex() + 1 >= levelManager.getAmountOfLevels()) {
            levelManager.loadNextLevel();
            resetAll();
            game.getAudioPlayer().playSong(AudioPlayer.MENU_1);
            return;
        }
        this.lvlCompleted = levelCompleted;
    }

    public void restartGame() {
        player.saveCheckpoint();
        saveShopCheckpoints();
        levelManager.resetLevelIndex();
        player.setSpawn(levelManager.getCurrentLevel().getPlayerSpawn());
        player.resetAll();
        player.setAttacking(false);
        resetAll();
        xLvlOffset = 0;
        calcLvlOffset();
        npcs.clear();
        initNPCs();
        levelNameOverlay.start(levelManager.getLvlIndex());
    }

    public void loadStartLevelByIndex(int index) {
        player.saveCheckpoint();
        saveShopCheckpoints();
        resetAll();
        levelManager.setLevelIndex(index);
        enemyManager.loadEnemies(levelManager.getCurrentLevel());
        objectManager.loadObjects(levelManager.getCurrentLevel());
        player.setSpawn(levelManager.getCurrentLevel().getPlayerSpawn());
        player.loadLvlData(levelManager.getCurrentLevel().getLevelData());
        player.resetAll();
        xLvlOffset = 0;
        calcLvlOffset();
        npcs.clear();
        initNPCs();
        levelNameOverlay.start(levelManager.getLvlIndex());
        tryStartBossCutscene();
    }

    public void setMaxLvlOffset(int lvlOffset) { 
        this.maxLvlOffsetX = lvlOffset; 
    }
    public void unpauseGame()                  { 
        paused = false; 
    }
    public void windowFocusLost()              { 
        player.resetDirBooleans(); 
    }

    public Player getPlayer()         { 
        return player; 
    }
    public EnemyManager getEnemyManager()   { 
        return enemyManager; 
    }
    public LevelManager getLevelManager()   { 
        return levelManager; 
    }
    public ObjectManager getObjectManager()  { 
        return objectManager; 
    }

        private void saveShopCheckpoints() {
        for (NPC npc : npcs)
            if (npc.isShopkeeper())
                npc.saveShopCheckpoint();
    }

    private void restoreShopCheckpoints() {
        for (NPC npc : npcs)
            if (npc.isShopkeeper())
                npc.restoreShopCheckpoint();
    }

    public void teleportToBoss3() {
        int bossLevelIndex = 6;  // Level 7
        
        System.out.println("DEBUG: Teleporting to BOSS3 (Level " + (bossLevelIndex + 1) + ")");
        
        // Save checkpoint
        player.saveCheckpoint();
        saveShopCheckpoints();
        
        // Reset everything
        resetAll();
        
        // Load boss level
        levelManager.setLevelIndex(bossLevelIndex);
        Level bossLevel = levelManager.getCurrentLevel();
        enemyManager.loadEnemies(bossLevel);
        objectManager.loadObjects(bossLevel);
        
        // Setup player
        player.setSpawn(bossLevel.getPlayerSpawn());
        player.loadLvlData(bossLevel.getLevelData());
        
        // Full heal and stamina restore
        player.changeHealth(player.getMaxHealth() - player.getCurrentHealth());
        player.setStamina(MAX_STAMINA);
        player.clearBurn();
        
        // Reset action states
        player.setAttacking(false);
        player.setSkill2(false);
        player.setSkill3(false);
        player.setLeft(false);
        player.setRight(false);
        player.setJump(false);
        
        // Reset camera and game flags
        xLvlOffset = 0;
        calcLvlOffset();
        npcs.clear();
        initNPCs();
        dialogueActive = false;
        shopActive = false;
        activeNPC = null;
        paused = false;
        gameOver = false;
        lvlCompleted = false;
        gameCompleted = false;
        playerDying = false;
        levelNameOverlay.start(levelManager.getLvlIndex());
        
        System.out.println("DEBUG: Teleported to Boss3! Player fully healed.");
        tryStartBossCutscene();
    }

    private void tryStartBossCutscene() {
        int lvl = levelManager.getLvlIndex();
        BossCutscene.CutsceneLine[] lines = null;

        if (lvl == 2) {
            lines = BossCutscene.getBoss1Lines();
        } else if (lvl == 4) {
            lines = BossCutscene.getBoss2Lines();
        } else if (lvl == 6) {
            lines = BossCutscene.getBoss3Lines();
        }

        if (lines != null) {
            bossCutscene   = new BossCutscene(lines);
            cutsceneActive = true;
        }
    }

    public void onBossDefeated() {
        game.getStoryManager().onBossDefeated(levelManager.getLvlIndex());
    }
}
