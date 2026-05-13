package inputs;

import gamestates.Gamestate;
import main.GamePanel;

import java.awt.event.KeyEvent;
import java. awt.event.KeyListener;
import java.util.HashSet;
import java.util.Set;

public class KeyboardInputs implements KeyListener {
    private GamePanel gamePanel;
    private Set<Integer> pressedKeys = new HashSet<>();
    private Set<Integer> suppressedKeys = new HashSet<>();


    public KeyboardInputs(GamePanel gamePanel){
        this.gamePanel = gamePanel;
    }

    @Override
    public void keyTyped(KeyEvent e){

   }

    @Override
    public void keyReleased(KeyEvent e){
        pressedKeys.remove(e.getKeyCode());
        suppressedKeys.remove(e.getKeyCode());

        
        if (e.getKeyCode() == KeyEvent.VK_B && e.isControlDown()) {
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
        if (suppressedKeys.contains(e.getKeyCode()))
            return;

        if (Gamestate.state == Gamestate.PLAYING)
            pressedKeys.add(e.getKeyCode());

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

    public void releaseAllPressedKeys() {
        suppressedKeys.addAll(pressedKeys);

        for (int keyCode : new HashSet<>(pressedKeys)) {
            KeyEvent releaseEvent = new KeyEvent(
                    gamePanel,
                    KeyEvent.KEY_RELEASED,
                    System.currentTimeMillis(),
                    0,
                    keyCode,
                    KeyEvent.CHAR_UNDEFINED
            );
            gamePanel.getGame().getPlaying().keyReleased(releaseEvent);
        }

        pressedKeys.clear();
    }
}
