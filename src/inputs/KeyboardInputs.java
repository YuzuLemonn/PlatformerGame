package inputs;

import gamestates.Gamestate;
import main.GamePanel;

import java.awt.event.KeyEvent;
import java. awt.event.KeyListener;
import static utilz.Constants.Directions.*;

public class KeyboardInputs implements KeyListener {
    private GamePanel gamePanel;


    public KeyboardInputs(GamePanel gamePanel){
        this.gamePanel = gamePanel;
    }

    @Override
    public void keyTyped(KeyEvent e){

   }

    @Override
    public void keyReleased(KeyEvent e){

        // DEBUG: Print all key presses to see if they're detected
        System.out.println("KeyboardInputs - Key: " + KeyEvent.getKeyText(e.getKeyCode()) + 
                           ", Ctrl: " + e.isControlDown() + 
                           ", Shift: " + e.isShiftDown());
        
        // DEBUG: Ctrl+B shortcut right here in KeyboardInputs
        if (e.getKeyCode() == KeyEvent.VK_B && e.isControlDown()) {
            System.out.println("KeyboardInputs: Ctrl+B detected! Calling teleport...");
            if (Gamestate.state == Gamestate.PLAYING) {
                gamePanel.getGame().getPlaying().teleportToBoss3();
                return; // Don't send to normal handler
            }
        }
        
        switch(Gamestate.state){
            case PLAYING:
                gamePanel.getGame().getPlaying().keyReleased(e);
                break;
            case MENU:
                gamePanel.getGame().getMenu().keyReleased(e);
                break;
            case CHARACTER_SELECT:
                gamePanel.getGame().getCharacterSelect().keyReleased(e);
                break;
            case OPTIONS:
                gamePanel.getGame().getGameOptions().keyReleased(e);
                break;
            case CREDITS:
                gamePanel.getGame().getCredits().keyReleased(e);
                break;

            default:
                break;
        }
    }

    @Override
    public void keyPressed(KeyEvent e){
        switch(Gamestate.state){
            case PLAYING:
                gamePanel.getGame().getPlaying().keyPressed(e);
                break;
            case MENU:
                gamePanel.getGame().getMenu().keyPressed(e);
                break;
            case CHARACTER_SELECT:
                gamePanel.getGame().getCharacterSelect().keyPressed(e);
                break;
            case OPTIONS:
                gamePanel.getGame().getGameOptions().keyPressed(e);
                break;
            case CREDITS:
                gamePanel.getGame().getCredits().keyPressed(e);
                break;
            case CUTSCENE:
                gamePanel.getGame().getStoryManager().onKeyPressed(e);
                break;
            default:
                break;
        }
    }
}
