package entities;

import main.Game;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import gamestates.Playing;
import utilz.LoadSave;

import static utilz.HelpMethods.CanMoveHere;
import static utilz.HelpMethods.GetEntityYPosUnderRoofOrAboveFloor;

public class NPC extends Entity {
    private Playing playing;
    private String name;
    private String[] dialogueLines;
    private int dialogueIndex = 0;
    private boolean dialogueActive = false;
    private boolean isShopkeeper = false;
    private ShopUI shopUI;

    private float interactRange = Game.TILES_SIZE * 2;
    private int[][] lvlData;
    private float fallSpeed = 0;
    private float gravity = 0.04f * Game.SCALE;
    private boolean inAir = true;

    private BufferedImage[][] animations;
    private int aniTick, aniIndex;
    private static final int ANI_SPEED = 30;

//    private float xDrawOffset = 21 * Game.SCALE;
//    private float yDrawOffset = 10 * Game.SCALE;

    public void loadLvlData(int[][] lvlData) {
        this.lvlData = lvlData;
    }

    public NPC(float x, float y, String name, String[] dialogueLines, Playing playing) {
        super(x, y, (int)(64 * Game.SCALE), (int)(64 * Game.SCALE));
        this.name = name;
        this.dialogueLines = dialogueLines;
        this.playing = playing;
        initHitbox((int)(14 * Game.SCALE), (int)(28 * Game.SCALE));
        loadAnimations(resolveSpritePath(name));
    }

    private void updateAnimationTick() {
        aniTick++;
        if (aniTick >= ANI_SPEED) {
            aniTick = 0;
            aniIndex = (aniIndex + 1) % animations[0].length;
        }
    }

    private String resolveSpritePath(String name) {
        switch (name) {
            case "Merchant": return LoadSave.MERCHANT2_IDLE;
            case "Mother":   return LoadSave.MOTHER_IDLE;
            default:         return LoadSave.MOTHER_IDLE;
        }
    }

    private void loadAnimations(String idleSpritePath) {
        BufferedImage img = LoadSave.GetSpriteAtlas(idleSpritePath);
        if (img == null) {
            System.err.println("NPC sprite not found: " + idleSpritePath);
            return;
        }

        boolean isMerchant = idleSpritePath.equals(LoadSave.MERCHANT2_IDLE);
        int frames      = isMerchant ? 4 : 5;
        int frameHeight = img.getHeight();
        int frameWidth  = img.getWidth() / frames;

        animations = new BufferedImage[1][frames];
        for (int i = 0; i < frames; i++)
            animations[0][i] = img.getSubimage(i * frameWidth, 0, frameWidth, frameHeight);
    }

    public void setShopkeeper(boolean shopkeeper) {
        this.isShopkeeper = shopkeeper;
        if (shopkeeper)
            shopUI = new ShopUI(playing);
    }

    public void update() {
        if (lvlData == null) {
            return;
        }
        updateAnimationTick();

        if (inAir) {
            if (CanMoveHere(hitbox.x, hitbox.y + fallSpeed,
                    hitbox.width, hitbox.height, lvlData)) {
                hitbox.y += fallSpeed;
                fallSpeed += gravity;
            } else {
                inAir = false;
                hitbox.y = GetEntityYPosUnderRoofOrAboveFloor(hitbox, fallSpeed);
                fallSpeed = 0;
            }
        }
    }

    public void draw(Graphics g, int xLvlOffset) {
        if (animations == null) return;

        BufferedImage frame = animations[0][aniIndex];
        int drawW = (int)(frame.getWidth()  * Game.SCALE);
        int drawH = (int)(frame.getHeight() * Game.SCALE);

        int drawX = (int)(hitbox.x - xLvlOffset) - (drawW - (int)hitbox.width) / 2;
        int drawY = (int)(hitbox.y) - (drawH - (int)hitbox.height) - (int)(8 * Game.SCALE);

        g.drawImage(frame, drawX, drawY, drawW, drawH, null);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, (int)(8 * Game.SCALE)));

        FontMetrics fm = g.getFontMetrics();
        int nameWidth = fm.stringWidth(name);
        g.drawString(name, drawX + (drawW - nameWidth) / 2, drawY - (int)(6 * Game.SCALE));
    }

    public boolean isPlayerInRange(Rectangle2D.Float playerHitbox) {
        float dist = Math.abs(playerHitbox.x - hitbox.x);
        return dist <= interactRange;
    }

    public void interact() {
        if (!dialogueActive) {
            dialogueActive = true;
            dialogueIndex = 0;
            return;
        }
        dialogueIndex++;
        if (dialogueIndex >= dialogueLines.length) {
            dialogueActive = false;
            dialogueIndex = 0;
        }
    }

    public void openShop() {
        if (shopUI != null)
            shopUI.init(playing.getPlayer().getPlayerClass());
    }

    public void drawShop(Graphics g) {
        if (shopUI != null)
            shopUI.draw(g);
    }

    public void handleShopKey(int keyCode) {
        if (shopUI != null)
            shopUI.handleKey(keyCode);
    }

    public void endDialogue() {
        dialogueActive = false;
        dialogueIndex = 0;
    }

    public boolean isTextFinished(String displayedText) {
        return displayedText.equals(getCurrentLine());
    }

    public boolean isShopkeeper()     { return isShopkeeper; }
    public boolean isDialogueActive() { return dialogueActive; }
    public String  getCurrentLine()   { return dialogueLines[dialogueIndex]; }
    public String  getName()          { return name; }

        public void saveShopCheckpoint() {
        if (shopUI != null) shopUI.saveCheckpoint();
    }

    public void restoreShopCheckpoint() {
        if (shopUI != null) shopUI.restoreCheckpoint();
    }
}