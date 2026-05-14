package main;

import inputs.KeyboardInputs;
import inputs.MouseInputs;
import java.awt.*;
import javax.swing.JPanel;


import static main.Game.GAME_HEIGHT;
import static main.Game.GAME_WIDTH;

public class GamePanel extends JPanel{
    private MouseInputs mouseInputs;
    private KeyboardInputs keyboardInputs;
    private Game game;

    public GamePanel(Game game){
        mouseInputs = new MouseInputs(this);
        keyboardInputs = new KeyboardInputs(this);
        this.game = game;

        setPanelSize();
        addKeyListener(keyboardInputs);
        addMouseListener(mouseInputs);
        addMouseMotionListener(mouseInputs);
    }

    private void setPanelSize() {
        Dimension size = new Dimension(GAME_WIDTH,GAME_HEIGHT);

        setPreferredSize(size);

        System.out.println("size: " + GAME_WIDTH + " : " + GAME_HEIGHT);

    }

    public void paintComponent(Graphics g){
        super.paintComponent(g);
        game.render(g);
    }

    public Game getGame(){
        return game;
    }

    public void releaseAllPressedKeys() {
        keyboardInputs.releaseAllPressedKeys();
    }

}
