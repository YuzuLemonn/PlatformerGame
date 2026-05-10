package gamestates;

import main.Game;
import utilz.LoadSave;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

public class Credits extends State implements Statemethods {
    private BufferedImage backgroundImg;
    private boolean musicStarted = false;

    public Credits(Game game) {
        super(game);
        backgroundImg = LoadSave.GetSpriteAtlas("black_bg.png");
    }

    @Override
    public void update() {
        if (!musicStarted) {
            game.getAudioPlayer().playCreditsSong();
            musicStarted = true;
        }
    }

    @Override
    public void draw(Graphics g) {
        g.drawImage(backgroundImg, 0, 0, Game.GAME_WIDTH, Game.GAME_HEIGHT, null);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Monospaced", Font.BOLD, (int)(8 * Game.SCALE)));

        String[] lines = {
                "CREDITS",
                "",
                "Project Mangers",
                "Neo Mark Tripoli",
                "Francis Louie Tantengco",
                "",
                "Programming",
                "Neo Mark Tripoli",
                "Josiah Angeles",
                "Ser Raineir Benedict Macailing",
                "",
                "Art & Sprites",
                "Edvard Antony De los Reyes",
                "Francis Louie Tantengco",
                "",
                "Level Design",
                "Neo Mark Tripoli",
                "Edvard Antony De los Reyes",
                "",
                "Music",
                "Stolen",
                "",
                "Press ESC to return"
        };

        int startY = (int)(80 * Game.SCALE);
        int lineH  = (int)(13 * Game.SCALE);
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
    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            musicStarted = false;
            game.getAudioPlayer().playSong(audio.AudioPlayer.MENU_1);
            Gamestate.state = Gamestate.MENU;
        }
    }
    @Override public void keyReleased(KeyEvent e) {}
}