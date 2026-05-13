package gamestates;

import main.Game;
import ui.MenuButton;
import utilz.LoadSave;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

public class Menu extends State implements Statemethods{
    private MenuButton[] buttons = new MenuButton[4];
    private BufferedImage backdropImg;

    public Menu(Game game) {
        super(game);
        loadButtons();
        backdropImg = LoadSave.GetSpriteAtlas(LoadSave.MENU_BACKGROUND_IMG);
    }

    private void loadButtons() {
        int centerX = Game.GAME_WIDTH / 2;
        int startY  = (int)(Game.GAME_HEIGHT * 0.50f);
        int gap     = (int)(50 * Game.SCALE);

        buttons[0] = new MenuButton(centerX, startY,0, Gamestate.CHARACTER_SELECT);
        buttons[1] = new MenuButton(centerX, startY + gap,1, Gamestate.OPTIONS);
        buttons[2] = new MenuButton(centerX, startY + gap * 2, 3, Gamestate.CREDITS);
        buttons[3] = new MenuButton(centerX, startY + gap * 3, 2, Gamestate.QUIT);

    }

    @Override
    public void update() {
        for(MenuButton mb : buttons){
            mb.update();
        }
    }

    @Override
    public void draw(Graphics g) {
        g.drawImage(backdropImg, 0, 0, Game.GAME_WIDTH, Game.GAME_HEIGHT, null);

        for(MenuButton mb : buttons){
            mb.draw(g);
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {
        for(MenuButton mb : buttons){
            if(isIn(e, mb)){
                mb.setMousePressed(true);
                break;
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        for (MenuButton mb : buttons) {
            if (isIn(e, mb)) {
                if (mb.isMousePressed()) {
                    mb.applyGamestate();
                    if (mb.getState() == Gamestate.CHARACTER_SELECT) {
                        game.getAudioPlayer().setLevelSong(0);
                        game.getPlaying().restartGame();
                    }
                    if (mb.getState() == Gamestate.CHARACTER_SELECT) {
                        game.getAudioPlayer().setLevelSong(0);
                        game.getPlaying().restartGame();
                    }
                }
                break;
            }
        }
        resetButtons();
    }

    private void resetButtons() {
        for(MenuButton mb : buttons){
            mb.resetBools();
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        for(MenuButton mb : buttons){
            mb.setMouseOver(false);
        }

        for(MenuButton mb : buttons){
            if(isIn(e, mb)){
                mb.setMouseOver(true);
                break;
            }
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            Gamestate.state = Gamestate.PLAYING;
            game.getAudioPlayer().setLevelSong(0);
            game.getPlaying().restartGame();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }
}
