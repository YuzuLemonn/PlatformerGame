package gamestates;

import main.Game;
import utilz.LoadSave;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

public class Credits extends State implements Statemethods {
    private BufferedImage backgroundImg;

    public Credits(Game game) {
        super(game);
        backgroundImg = LoadSave.GetSpriteAtlas(LoadSave.MENU_BACKGROUND_IMG);
    }

    @Override
    public void update() {}

    @Override
    public void draw(Graphics g) {
        g.drawImage(backgroundImg, 0, 0, Game.GAME_WIDTH, Game.GAME_HEIGHT, null);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Monospaced", Font.BOLD, (int)(14 * Game.SCALE)));

        String[] lines = {
                "CREDITS",
                "",
                "Game Design & Programming",
                "Yuzu & Team",
                "",
                "Art & Sprites",
                "Your artist here",
                "",
                "Music",
                "Your composer here",
                "",
                "Press ESC to return"
        };

        int startY = (int)(80 * Game.SCALE);
        int lineH  = (int)(22 * Game.SCALE);
        FontMetrics fm = g.getFontMetrics();

        for (int i = 0; i < lines.length; i++) {
            int x = Game.GAME_WIDTH/2 - fm.stringWidth(lines[i])/2;
            g.drawString(lines[i], x, startY + i * lineH);
        }
    }

    @Override public void mouseClicked(MouseEvent e) {}
    @Override public void mousePressed(MouseEvent e) {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseMoved(MouseEvent e) {}
    public void mouseDragged(MouseEvent e) {}
    @Override public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
            Gamestate.state = Gamestate.MENU;
    }
    @Override public void keyReleased(KeyEvent e) {}
}