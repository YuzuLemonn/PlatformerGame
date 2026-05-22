package ui;

import gamestates.Gamestate;
import utilz.LoadSave;

import java.awt.*;
import java.awt.image.BufferedImage;
import static utilz.Constants.UI.Buttons.*;

public class MenuButton {
    //Buttons in the menu not menu button

    private int xPos, yPos, rowIndex, index;
    private int xOffsetCenter = B_WIDTH / 2;
    private Gamestate state;
    private String fallbackLabel;
    private BufferedImage[] imgs;
    private boolean mouseOver, mousePressed;
    private Rectangle bounds;

    public MenuButton(int xPos, int yPos, int rowIndex, Gamestate state){
        this(xPos, yPos, rowIndex, state, null);
    }

    public MenuButton(int xPos, int yPos, int rowIndex, Gamestate state, String fallbackLabel){
        this.xPos = xPos;
        this.yPos = yPos;
        this.rowIndex = rowIndex;
        this.state = state;
        this.fallbackLabel = fallbackLabel;
        loadImgs();
        initBounds();
    }

    private void initBounds() {
        bounds = new Rectangle(xPos - xOffsetCenter, yPos, B_WIDTH, B_HEIGHT);
    }

    private void loadImgs(){
        imgs = new BufferedImage[3];
        BufferedImage temp = LoadSave.GetSpriteAtlas(LoadSave.MENU_BUTTONS);
        for(int i = 0; i < imgs.length; i++){
            if (temp != null &&
                    (i + 1) * B_WIDTH_DEFAULT <= temp.getWidth() &&
                    (rowIndex + 1) * B_HEIGHT_DEFAULT <= temp.getHeight()) {
                imgs[i] = temp.getSubimage(i * B_WIDTH_DEFAULT, rowIndex * B_HEIGHT_DEFAULT, B_WIDTH_DEFAULT, B_HEIGHT_DEFAULT);
            } else {
                imgs[i] = createFallbackImg(i);
            }
        }
    }

    private BufferedImage createFallbackImg(int stateIndex) {
        BufferedImage img = new BufferedImage(B_WIDTH_DEFAULT, B_HEIGHT_DEFAULT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color fill = switch (stateIndex) {
            case 1 -> new Color(85, 76, 116);
            case 2 -> new Color(45, 39, 65);
            default -> new Color(62, 55, 86);
        };

        g.setColor(fill);
        g.fillRoundRect(2, 2, B_WIDTH_DEFAULT - 4, B_HEIGHT_DEFAULT - 4, 8, 8);
        g.setColor(new Color(232, 218, 170));
        g.drawRoundRect(2, 2, B_WIDTH_DEFAULT - 5, B_HEIGHT_DEFAULT - 5, 8, 8);

        String label = fallbackLabel == null ? "" : fallbackLabel;
        g.setFont(new Font("Monospaced", Font.BOLD, 14));
        FontMetrics fm = g.getFontMetrics();
        g.drawString(label, B_WIDTH_DEFAULT / 2 - fm.stringWidth(label) / 2,
                B_HEIGHT_DEFAULT / 2 + fm.getAscent() / 2 - 2);

        g.dispose();
        return img;
    }

    public void draw(Graphics g){
        g.drawImage(imgs[index], xPos - xOffsetCenter, yPos, B_WIDTH, B_HEIGHT,null);
    }

    public void update(){
        index = 0;
        if(mouseOver){
            index = 1;
        }
        if(mousePressed){
            index = 2;
        }
    }

    public boolean isMousePressed() {
        return mousePressed;
    }

    public void setMousePressed(boolean mousePressed) {
        this.mousePressed = mousePressed;
    }

    public boolean isMouseOver() {
        return mouseOver;
    }

    public void setMouseOver(boolean mouseOver) {
        this.mouseOver = mouseOver;
    }

    public Rectangle getBounds(){
        return bounds;
    }

    public void applyGamestate(){
        Gamestate.state = state;
    }

    public void resetBools(){
        mouseOver = false;
        mousePressed = false;
    }

    public Gamestate getState() {
        return state;
    }
}
