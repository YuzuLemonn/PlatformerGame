package ui;

import entities.NPC;
import gamestates.Playing;
import main.Game;
import utilz.LoadSave;

import java.awt.*;
import java.awt.image.BufferedImage;

public class DialogueOverlay {
    private Playing playing;

    private int boxX = (int)(20  * Game.SCALE);
    private int boxY = (int)(Game.GAME_HEIGHT - 100 * Game.SCALE);
    private int boxW = (int)(Game.GAME_WIDTH  - 40  * Game.SCALE);
    private int boxH = (int)(80  * Game.SCALE);

    private int portraitX    = (int)(25 * Game.SCALE);
    private int portraitY    = (int)(Game.GAME_HEIGHT - 95 * Game.SCALE);
    private int portraitSize = (int)(70 * Game.SCALE);

    private int textX = (int)(110 * Game.SCALE);
    private int textY = (int)(Game.GAME_HEIGHT - 65 * Game.SCALE);

    private int nameX = (int)(200 * Game.SCALE);


    private String displayedText = "";
    private int    charIndex     = 0;
    private int    textTick      = 0;
    private int    textSpeed     = 3;
    private String fullText      = "";

    private BufferedImage merchant1Portrait;
    private BufferedImage motherPortrait;
    private BufferedImage merchantPortrait;

    public DialogueOverlay(Playing playing) {
        this.playing = playing;
        motherPortrait   = LoadSave.GetSpriteAtlas(LoadSave.MOTHER_IDLE);   // or a dedicated portrait image
        merchant1Portrait = LoadSave.GetSpriteAtlas(LoadSave.MERCHANT1_IDLE);
        merchantPortrait = LoadSave.GetSpriteAtlas(LoadSave.MERCHANT2_IDLE);
    }

    public void update(NPC npc) {
        if (npc == null || !npc.isDialogueActive()) return;

        if (!fullText.equals(npc.getCurrentLine())) {
            fullText      = npc.getCurrentLine();
            displayedText = "";
            charIndex     = 0;
            textTick      = 0;
        }

        if (charIndex < fullText.length()) {
            textTick++;
            if (textTick >= textSpeed) {
                textTick = 0;
                charIndex++;
                displayedText = fullText.substring(0, charIndex);
            }
        }
    }

    public void draw(Graphics g, NPC npc) {
        if (npc == null || !npc.isDialogueActive()) return;

        // box
        g.setColor(new Color(0, 0, 0, 200));
        g.fillRoundRect(boxX, boxY, boxW, boxH, 10, 10);
        g.setColor(Color.WHITE);
        g.drawRoundRect(boxX, boxY, boxW, boxH, 10, 10);

        BufferedImage sheet;
        int frameCount;

        switch (npc.getName()) {
            case "Mysterious Merchant"  -> { sheet = merchantPortrait;  frameCount = 4; }
            case "Merchant Raineir" -> { sheet = merchant1Portrait; frameCount = 4; }
            case "Trader Francis" -> { sheet = merchant1Portrait; frameCount = 4; }
            default          -> { sheet = motherPortrait;    frameCount = 5; }
        }

        if (sheet != null) {
            int frameW = sheet.getWidth() / frameCount;
            int frameH = sheet.getHeight();
            BufferedImage portrait = sheet.getSubimage(0, 0, frameW, frameH);
            g.drawImage(portrait, portraitX, portraitY, portraitSize, portraitSize, null);
        } else {
            g.setColor(new Color(80, 80, 80, 220));
            g.fillRect(portraitX, portraitY, portraitSize, portraitSize);
        }
        g.setColor(Color.WHITE);
        g.drawRect(portraitX, portraitY, portraitSize, portraitSize);

        // NPC name
        g.setFont(new Font("Monospaced", Font.BOLD, (int)(9 * Game.SCALE)));
        g.setColor(Color.YELLOW);
        String displayName = npc.getName().equals("Merchant Ranier") ? "Raineir" : npc.getName();
        g.drawString(displayName, nameX, textY - (int)(16 * Game.SCALE));

        // dialogue text
        g.setFont(new Font("Monospaced", Font.PLAIN, (int)(7 * Game.SCALE)));
        g.setColor(Color.WHITE);
        g.drawString(displayedText, textX, textY);

        // continue hint
        if (isTextComplete()) {
            g.setFont(new Font("Monospaced", Font.ITALIC, (int)(6 * Game.SCALE)));
            g.setColor(Color.LIGHT_GRAY);
            g.drawString("[E] Continue", boxX + boxW - (int)(60 * Game.SCALE),
                    boxY + boxH - (int)(5 * Game.SCALE));
        }
    }

    public boolean isTextComplete() { return charIndex >= fullText.length(); }

    public void skipToEnd() {
        charIndex     = fullText.length();
        displayedText = fullText;
    }
}
