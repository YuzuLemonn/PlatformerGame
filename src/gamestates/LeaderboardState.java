package gamestates;

import main.Game;
import utilz.Leaderboard;
import utilz.LoadSave;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.List;

public class LeaderboardState extends State implements Statemethods {
    private BufferedImage backgroundImg;
    private BufferedImage overlayBackground;

    public LeaderboardState(Game game) {
        super(game);
        backgroundImg = LoadSave.GetSpriteAtlas(LoadSave.MENU_BACKGROUND_IMG);
        overlayBackground = LoadSave.GetSpriteAtlas(LoadSave.LEADERBOARD);
    }

    @Override
    public void update() {
    }

    @Override
    public void draw(Graphics g) {
        if (backgroundImg != null)
            g.drawImage(backgroundImg, 0, 0, Game.GAME_WIDTH, Game.GAME_HEIGHT, null);
        else {
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, Game.GAME_WIDTH, Game.GAME_HEIGHT);
        }

        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(new Color(0, 0, 0, 170));
        g2.fillRect(0, 0, Game.GAME_WIDTH, Game.GAME_HEIGHT);

        int overlayW = (int)(258 * Game.SCALE);
        int overlayH = (int)(258 * Game.SCALE);
        int overlayX = Game.GAME_WIDTH / 2 - overlayW / 2;
        int overlayY = Game.GAME_HEIGHT / 2 - overlayH / 2;

        if (overlayBackground != null)
            g.drawImage(overlayBackground, overlayX, overlayY, overlayW, overlayH, null);
        else {
            g.setColor(new Color(0, 0, 0, 210));
            g.fillRect(overlayX, overlayY, overlayW, overlayH);
            g.setColor(Color.WHITE);
            g.drawRect(overlayX, overlayY, overlayW, overlayH);
        }

        List<Leaderboard.Entry> entries = game.getLeaderboard().getEntries();
        int tableX = overlayX + (int)(36 * Game.SCALE);
        int y = overlayY + (int)(75 * Game.SCALE);
        int lineH = (int)(13 * Game.SCALE);

        g.setFont(new Font("Monospaced", Font.BOLD, (int)(6 * Game.SCALE)));
        g.setColor(new Color(255, 215, 120));
        g.drawString("#", tableX, y);
        g.drawString("NAME", tableX + (int)(24 * Game.SCALE), y);
        g.drawString("TIME", tableX + (int)(128 * Game.SCALE), y);

        g.setFont(new Font("Monospaced", Font.PLAIN, (int)(6 * Game.SCALE)));
        if (entries.isEmpty()) {
            g.setColor(Color.WHITE);
            String empty = "No completed runs yet";
            FontMetrics fm = g.getFontMetrics();
            g.drawString(empty, overlayX + overlayW / 2 - fm.stringWidth(empty) / 2, y + lineH * 4);
        } else {
            for (int i = 0; i < entries.size(); i++) {
                Leaderboard.Entry entry = entries.get(i);
                int rowY = y + lineH * (i + 1);
                g.setColor(i == 0 ? new Color(255, 235, 140) : Color.WHITE);
                g.drawString(String.valueOf(i + 1), tableX, rowY);
                g.drawString(entry.getName(), tableX + (int)(24 * Game.SCALE), rowY);
                g.drawString(entry.getFormattedTime(), tableX + (int)(128 * Game.SCALE), rowY);
            }
        }

        g.setColor(new Color(210, 210, 210));
        g.setFont(new Font("Monospaced", Font.ITALIC, (int)(7 * Game.SCALE)));
        String prompt = "Press ESC to return to menu";
        FontMetrics fm = g.getFontMetrics();
        g.drawString(prompt, Game.GAME_WIDTH / 2 - fm.stringWidth(prompt) / 2, Game.GAME_HEIGHT - (int)(35 * Game.SCALE));
    }

    @Override public void mouseClicked(MouseEvent e) {}
    @Override public void mousePressed(MouseEvent e) {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseMoved(MouseEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
            setGamestate(Gamestate.MENU);
    }

    @Override public void keyReleased(KeyEvent e) {}
}
