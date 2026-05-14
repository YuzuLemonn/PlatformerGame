package ui;

import main.Game;

import java.awt.*;

public class LevelNameOverlay {
    private static final int FADE_IN_TICKS = 80;
    private static final int HOLD_TICKS = 420;
    private static final int FADE_OUT_TICKS = 220;
    private static final int TOTAL_TICKS = FADE_IN_TICKS + HOLD_TICKS + FADE_OUT_TICKS;

    private String worldName = "";
    private String worldCounter = "";
    private int tick = TOTAL_TICKS;

    public void start(int levelIndex) {
        worldName = getWorldName(levelIndex);
        worldCounter = getWorldCounter(levelIndex);
        tick = 0;
    }

    public void update() {
        if (isActive())
            tick++;
    }

    public void draw(Graphics g) {
        if (!isActive())
            return;

        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, getAlpha()));
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int titleY = (int)(26 * Game.SCALE);
        int counterY = titleY + (int)(20 * Game.SCALE);

        drawCenteredString(g2d, worldName, titleY, new Font("Monospaced", Font.BOLD, (int)(13 * Game.SCALE)));
        drawCenteredString(g2d, worldCounter, counterY, new Font("Monospaced", Font.BOLD, (int)(8 * Game.SCALE)));

        g2d.dispose();
    }

    private boolean isActive() {
        return tick < TOTAL_TICKS;
    }

    private float getAlpha() {
        if (tick < FADE_IN_TICKS)
            return tick / (float) FADE_IN_TICKS;
        if (tick < FADE_IN_TICKS + HOLD_TICKS)
            return 1f;

        int fadeOutTick = tick - FADE_IN_TICKS - HOLD_TICKS;
        return Math.max(0f, 1f - fadeOutTick / (float) FADE_OUT_TICKS);
    }

    private void drawCenteredString(Graphics2D g2d, String text, int y, Font font) {
        g2d.setFont(font);
        FontMetrics fm = g2d.getFontMetrics();
        int x = (Game.GAME_WIDTH - fm.stringWidth(text)) / 2;

        g2d.setColor(new Color(0, 0, 0, 160));
        g2d.drawString(text, x + 2, y + 2);
        g2d.setColor(Color.WHITE);
        g2d.drawString(text, x, y);
    }

    private String getWorldName(int levelIndex) {
        if (levelIndex == 2)
            return "Josiah - The Worm Keeper";
        if (levelIndex == 4)
            return "Edvard - The Starcaller";
        if (levelIndex == 6)
            return "Mark - The Sentinel of the Deepwood";

        if (levelIndex <= 2)
            return "World 1: Abyssos – The Underground Village";
        if (levelIndex <= 4)
            return "World 2: Infernal Depths (Demon World)";
        return "World 3: The Surface Realm";
    }

    private String getWorldCounter(int levelIndex) {
        if (levelIndex <= 2)
            return "1-" + (levelIndex + 1) + "/3";
        if (levelIndex <= 4)
            return "2-" + (levelIndex - 2) + "/2";
        return "3-" + (levelIndex - 4) + "/2";
    }
}
