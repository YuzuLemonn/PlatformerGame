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

    public LeaderboardState(Game game) {
        super(game);
        backgroundImg = LoadSave.GetSpriteAtlas(LoadSave.MENU_BACKGROUND_IMG);
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

        g.setColor(Color.WHITE);
        g.setFont(new Font("Monospaced", Font.BOLD, (int)(16 * Game.SCALE)));
        FontMetrics titleMetrics = g.getFontMetrics();
        String title = "LEADERBOARD";
        g.drawString(title, Game.GAME_WIDTH / 2 - titleMetrics.stringWidth(title) / 2, (int)(48 * Game.SCALE));

        List<Leaderboard.Entry> entries = game.getLeaderboard().getEntries();
        int tableX = Game.GAME_WIDTH / 2 - (int)(230 * Game.SCALE);
        int y = (int)(88 * Game.SCALE);
        int lineH = (int)(22 * Game.SCALE);

        g.setFont(new Font("Monospaced", Font.BOLD, (int)(8 * Game.SCALE)));
        g.setColor(new Color(255, 215, 120));
        g.drawString("RANK", tableX, y);
        g.drawString("NAME", tableX + (int)(55 * Game.SCALE), y);
        g.drawString("TIME", tableX + (int)(205 * Game.SCALE), y);
        g.drawString("DATE", tableX + (int)(315 * Game.SCALE), y);

        g.setFont(new Font("Monospaced", Font.PLAIN, (int)(8 * Game.SCALE)));
        if (entries.isEmpty()) {
            g.setColor(Color.WHITE);
            String empty = "No completed runs yet.";
            FontMetrics fm = g.getFontMetrics();
            g.drawString(empty, Game.GAME_WIDTH / 2 - fm.stringWidth(empty) / 2, y + lineH * 2);
        } else {
            for (int i = 0; i < entries.size(); i++) {
                Leaderboard.Entry entry = entries.get(i);
                int rowY = y + lineH * (i + 1);
                g.setColor(i == 0 ? new Color(255, 235, 140) : Color.WHITE);
                g.drawString(String.valueOf(i + 1), tableX, rowY);
                g.drawString(entry.getName(), tableX + (int)(55 * Game.SCALE), rowY);
                g.drawString(entry.getFormattedTime(), tableX + (int)(205 * Game.SCALE), rowY);
                g.drawString(entry.getFormattedCompletedAt(), tableX + (int)(315 * Game.SCALE), rowY);
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
