package main;

import utilz.LoadSave;

import javax.swing.JFrame;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.image.BufferedImage;

public class GameWindow {
    private JFrame jFrame;
    
    public GameWindow(GamePanel gamePanel){
        jFrame = new JFrame();

        // title
        jFrame.setTitle("Ascender");

        BufferedImage icon = LoadSave.GetSpriteAtlas("icon.png");
        jFrame.setIconImage(icon);

        if (icon != null) {
            try { //for mac dock icon
                java.awt.Taskbar taskbar = java.awt.Taskbar.getTaskbar();
                taskbar.setIconImage(icon);
            } catch (UnsupportedOperationException e) {
//                if not supported on this OS, skip silently
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jFrame.add(gamePanel);
        jFrame.setResizable(false);
        jFrame.pack();
        jFrame.setLocationRelativeTo(null);
        jFrame.setVisible(true);
        jFrame.addWindowFocusListener(new WindowFocusListener() {
            @Override
            public void windowGainedFocus(WindowEvent e) {

            }

            @Override
            public void windowLostFocus(WindowEvent e) {
                gamePanel.getGame().windowFocusLost();
            }
        });
    }

    
}
