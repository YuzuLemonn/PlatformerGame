package entities;

import main.Game;
import gamestates.Playing;

import java.awt.*;
import java.awt.event.KeyEvent;

public class ShopUI {

    private Playing playing;
    private String[]  items     = {};
    private String[]  labels    = {};
    private int[]     prices    = {};
    private float[]   dmgBoost  = {};
    private int[]     healAmount = {};
    private boolean[] isPotion  = {};
    private boolean[] purchased = {};
    private String playerClass  = "";
    private boolean[] checkpointPurchased = {};

    private int panelW = (int)(220 * Game.SCALE);
    private int panelH = (int)(160 * Game.SCALE);
    private int panelX = Game.GAME_WIDTH  / 2 - panelW / 2;
    private int panelY = Game.GAME_HEIGHT / 2 - panelH / 2;

    public ShopUI(Playing playing) {
        this.playing = playing;
    }

    public void init(String playerClass) {
        boolean classChanged = !playerClass.equals(this.playerClass);
        this.playerClass = playerClass;

        switch (playerClass) {
            case "BRAWLER" -> {
                items      = new String[] {"Iron Gauntlets", "HP Potion", "War Axe"};
                prices     = new int[]    {50,               20,          80};
                dmgBoost   = new float[]  {0.10f,            0.0f,        0.20f};
                healAmount = new int[]    {0,                0,           0};
                isPotion   = new boolean[]{false,            true,        false};
            }
            case "MAGE" -> {
                items      = new String[] {"Magic Staff", "Mana Potion", "Spell Tome"};
                prices     = new int[]    {60,            25,            90};
                dmgBoost   = new float[]  {0.10f,         0.0f,          0.20f};
                healAmount = new int[]    {0,             0,             0};
                isPotion   = new boolean[]{false,         true,          false};
            }
            case "ASSASSIN" -> {
                items      = new String[] {"Dagger", "HP Potion",  "Shadow Cloak"};
                prices     = new int[]    {55,       20,            85};
                dmgBoost   = new float[]  {0.15f,    0.0f,          0.10f};
                healAmount = new int[]    {0,        0,             0};
                isPotion   = new boolean[]{false,    true,          false};
            }
            default -> {
                items      = new String[] {"Mystery Box"};
                prices     = new int[]    {999};
                dmgBoost   = new float[]  {0.05f};
                healAmount = new int[]    {0};
                isPotion   = new boolean[]{false};
            }
        }

        labels = new String[items.length];
        for (int i = 0; i < items.length; i++)
            labels[i] = prices[i] + "g";

        // Only reset purchased if class changed, not on every shop open
        if (classChanged || purchased.length != items.length)
            purchased = new boolean[items.length];
    }

    public void update() {}

    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        g2d.setColor(new Color(0, 0, 0, 180));
        g2d.fillRoundRect(panelX, panelY, panelW, panelH, 16, 16);
        g2d.setColor(Color.YELLOW);
        g2d.drawRoundRect(panelX, panelY, panelW, panelH, 16, 16);

        g2d.setFont(new Font("Arial", Font.BOLD, (int)(9 * Game.SCALE)));
        g2d.setColor(Color.YELLOW);
        g2d.drawString("SHOP — " + playerClass,
                panelX + (int)(10 * Game.SCALE),
                panelY + (int)(18 * Game.SCALE));

        // Gold display top-right of panel
        g2d.setColor(Color.YELLOW);
        g2d.setFont(new Font("Arial", Font.BOLD, (int)(7 * Game.SCALE)));
        g2d.drawString("Gold: " + playing.getPlayer().getGold() + "g",
                panelX + panelW - (int)(60 * Game.SCALE),
                panelY + (int)(18 * Game.SCALE));

        // Items
        g2d.setFont(new Font("Arial", Font.PLAIN, (int)(7 * Game.SCALE)));
        int lineH  = (int)(18 * Game.SCALE);
        int startY = panelY + (int)(36 * Game.SCALE);

        for (int i = 0; i < items.length; i++) {
            if (!isPotion[i] && purchased[i]) {
                // Greyed out — already purchased
                g2d.setColor(Color.DARK_GRAY);
                g2d.drawString((i + 1) + ". " + items[i] + " [SOLD]",
                        panelX + (int)(10 * Game.SCALE),
                        startY + i * lineH);
                g2d.setColor(Color.DARK_GRAY);
                g2d.drawString(labels[i],
                        panelX + panelW - (int)(40 * Game.SCALE),
                        startY + i * lineH);
            } else {
                g2d.setColor(Color.WHITE);
                g2d.drawString((i + 1) + ". " + items[i],
                        panelX + (int)(10 * Game.SCALE),
                        startY + i * lineH);
                g2d.setColor(Color.LIGHT_GRAY);
                g2d.drawString(labels[i],
                        panelX + panelW - (int)(40 * Game.SCALE),
                        startY + i * lineH);
            }
        }

        // Potion count
        g2d.setColor(new Color(150, 255, 150));
        g2d.setFont(new Font("Arial", Font.BOLD, (int)(7 * Game.SCALE)));
        g2d.drawString("Potions: " + playing.getPlayer().getPotionCount() + "  (H to use)",
                panelX + (int)(10 * Game.SCALE),
                panelY + panelH - (int)(24 * Game.SCALE));

        g2d.setColor(Color.GRAY);
        g2d.setFont(new Font("Arial", Font.ITALIC, (int)(6 * Game.SCALE)));
        g2d.drawString("Press 1-" + items.length + " to buy  |  E to close",
                panelX + (int)(10 * Game.SCALE),
                panelY + panelH - (int)(10 * Game.SCALE));
    }

    public void handleKey(int keyCode) {
        int index = keyCode - KeyEvent.VK_1;
        if (index >= 0 && index < items.length)
            buyItem(index);
    }

    private void buyItem(int index) {
        Player player = playing.getPlayer();

        // Block repurchase of equipment
        if (!isPotion[index] && purchased[index]) {
            System.out.println("[SHOP] Already purchased: " + items[index]);
            return;
        }

        int cost = prices[index];
        if (!player.spendGold(cost)) {
            System.out.println("[SHOP] Not enough gold! Need " + cost + "g, have " + player.getGold() + "g");
            return;
        }

        if (dmgBoost[index] > 0)
            player.increaseDamage(dmgBoost[index]);

        if (isPotion[index])
            player.addPotion();

        if (!isPotion[index])
            purchased[index] = true;

        System.out.println("[SHOP] Bought: " + items[index] + " for " + cost + "g. "
                + "Gold left: " + player.getGold() + "g. "
                + "Potions: " + player.getPotionCount());
    }

        public void saveCheckpoint() {
        checkpointPurchased = purchased.clone();
    }

    public void restoreCheckpoint() {
        if (checkpointPurchased.length == purchased.length)
            purchased = checkpointPurchased.clone();
        else
            purchased = new boolean[items.length];  // safety fallback
    }
}