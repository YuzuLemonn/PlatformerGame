package ui;

import main.Game;
import java.awt.*;

public class BossCutscene {

    public static class CutsceneLine {
        public String speaker;
        public String text;
        public CutsceneLine(String speaker, String text) {
            this.speaker = speaker;
            this.text    = text;
        }
    }

    // World 1 — Worm boss
    public static final CutsceneLine[] BOSS1_LINES = {
        new CutsceneLine("???",    "I am the Worm-Keeper. None leave Abyssos."),
        new CutsceneLine("Player", "I'm done living beneath everything."),
        new CutsceneLine("???",    "Return… or be devoured."),
        new CutsceneLine("Player", "Then I'll rise — or die trying.")
    };

    // World 2 — Demon boss
    public static final CutsceneLine[] BOSS2_LINES = {
        new CutsceneLine("???",    "No mortal shall pass this abyss."),
        new CutsceneLine("Player", "I didn't come this far to stop now."),
        new CutsceneLine("???",    "Then your soul will remain here forever.")
    };

    // World 3 — Golem boss
    public static final CutsceneLine[] BOSS3_LINES = {
        new CutsceneLine("???",    "Speak your name, trespasser."),
        new CutsceneLine("Player", "I seek the surface. I have come too far to turn back."),
        new CutsceneLine("???",    "Then face the truth of the forest."),
    };

    // ── state ──────────────────────────────────────────────
    private CutsceneLine[] lines;
    private int    lineIndex     = 0;
    private String displayedText = "";
    private int    charIndex     = 0;
    private int    textTick      = 0;
    private int    textSpeed     = 2;   // lower = faster typing
    private boolean finished     = false;

    // fade
    private float fadeAlpha  = 1.0f;
    private boolean fadingIn = true;

    // layout
    private static final int FADE_SPEED   = 5;   // alpha steps out of 255
    private static final Color BG         = new Color(0, 0, 0);
    private static final Color PLAYER_COL = new Color(180, 220, 255);
    private static final Color BOSS_COL   = new Color(255, 100, 80);
    private static final Color HINT_COL   = new Color(180, 180, 180);

    public BossCutscene(CutsceneLine[] lines) {
        this.lines = lines;
        startLine();
    }

    private void startLine() {
        if (lineIndex >= lines.length) { finished = true; return; }
        displayedText = "";
        charIndex     = 0;
        textTick      = 0;
    }

    public void update() {
        if (finished) return;

        if (fadingIn) {
            fadeAlpha = Math.max(0f, fadeAlpha - FADE_SPEED / 255f);
            if (fadeAlpha <= 0f) fadingIn = false;
        }

        String full = lines[lineIndex].text;
        if (charIndex < full.length()) {
            textTick++;
            if (textTick >= textSpeed) {
                textTick = 0;
                charIndex++;
                displayedText = full.substring(0, charIndex);
            }
        }
    }

    /** Called when player presses E. Returns true if cutscene is now done. */
    public boolean advance() {
        String full = lines[lineIndex].text;

        // If still typing, skip to end of current line
        if (charIndex < full.length()) {
            charIndex     = full.length();
            displayedText = full;
            return false;
        }

        lineIndex++;
        if (lineIndex >= lines.length) {
            finished = true;
            return true;
        }
        startLine();
        return false;
    }

    public boolean isFinished() { return finished; }

    public void draw(Graphics g) {
        // Full black background
        g.setColor(BG);
        g.fillRect(0, 0, Game.GAME_WIDTH, Game.GAME_HEIGHT);

        if (lines == null || lineIndex >= lines.length) return;

        CutsceneLine current = lines[lineIndex];
        boolean isPlayer = current.speaker.equals("Player");

        // Speaker name
        g.setFont(new Font("Monospaced", Font.BOLD, (int) (10 * Game.SCALE)));
        g.setColor(isPlayer ? PLAYER_COL : BOSS_COL);
        FontMetrics fmName = g.getFontMetrics();
        int nameX = Game.GAME_WIDTH / 2 - fmName.stringWidth(current.speaker) / 2;
        int nameY = Game.GAME_HEIGHT / 2 - (int)(20 * Game.SCALE);
        g.drawString(current.speaker, nameX, nameY);

//        // Separator line
//        g.setColor(new Color(80, 80, 80));
//        g.fillRect(Game.GAME_WIDTH / 4, nameY + (int)(4 * Game.SCALE),
//                   Game.GAME_WIDTH / 2, 1);

        // Dialogue text — wrap long lines
        g.setFont(new Font("Monospaced", Font.BOLD, (int) (8 * Game.SCALE)));
        g.setColor(Color.WHITE);
        drawWrappedText(g, displayedText,
                Game.GAME_WIDTH / 2,
                nameY + (int)(24 * Game.SCALE),
                (int)(Game.GAME_WIDTH * 0.7f));

        // Progress dots
        drawProgressDots(g);

        // Continue hint — only when line is fully typed
        if (charIndex >= lines[lineIndex].text.length()) {
            g.setFont(new Font("Monospaced", Font.ITALIC, (int) (6 * Game.SCALE)));
            g.setColor(HINT_COL);
            String hint = "SPACE / E to continue";
            FontMetrics fmHint = g.getFontMetrics();
            g.drawString(hint,
                    Game.GAME_WIDTH / 2 - fmHint.stringWidth(hint) / 2,
                    Game.GAME_HEIGHT - (int)(20 * Game.SCALE));
        }

        // Fade overlay
        if (fadingIn && fadeAlpha > 0f) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setColor(new Color(0, 0, 0, (int)(fadeAlpha * 255)));
            g2d.fillRect(0, 0, Game.GAME_WIDTH, Game.GAME_HEIGHT);
        }
    }

    private void drawWrappedText(Graphics g, String text, int centerX, int y, int maxWidth) {
        FontMetrics fm = g.getFontMetrics();
        String[] words  = text.split(" ");
        StringBuilder line = new StringBuilder();
        int lineHeight = fm.getHeight() + (int)(2 * Game.SCALE);
        int currentY = y;

        for (String word : words) {
            String test = line.length() == 0 ? word : line + " " + word;
            if (fm.stringWidth(test) > maxWidth) {
                String s = line.toString();
                g.drawString(s, centerX - fm.stringWidth(s) / 2, currentY);
                currentY += lineHeight;
                line = new StringBuilder(word);
            } else {
                line = new StringBuilder(test);
            }
        }
        if (line.length() > 0) {
            String s = line.toString();
            g.drawString(s, centerX - fm.stringWidth(s) / 2, currentY);
        }
    }

    private void drawProgressDots(Graphics g) {
        int total  = lines.length;
        int dotSize = (int)(6 * Game.SCALE);
        int gap     = (int)(10 * Game.SCALE);
        int totalW  = total * dotSize + (total - 1) * gap;
        int startX  = Game.GAME_WIDTH / 2 - totalW / 2;
        int dotY    = Game.GAME_HEIGHT - (int)(40 * Game.SCALE);

        for (int i = 0; i < total; i++) {
            g.setColor(i == lineIndex ? Color.WHITE : new Color(60, 60, 60));
            g.fillOval(startX + i * (dotSize + gap), dotY, dotSize, dotSize);
        }
    }
}