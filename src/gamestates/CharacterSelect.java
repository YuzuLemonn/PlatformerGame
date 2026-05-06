package gamestates;

import entities.players.Assassin;
import entities.players.Brawler;
import entities.players.Mage;
import main.Game;
import utilz.LoadSave;

import static utilz.Constants.DamageConstants.*;
import static utilz.Constants.PlayerConstants.*;
import static utilz.Constants.PlayerConstants.MAX_STAMINA;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

public class CharacterSelect extends State implements Statemethods {
    private BufferedImage backgroundImg;
    private CharacterCard[] cards;
    private int hoveredCard = -1;

    private int cardWidth  = (int)(120 * Game.SCALE);
    private int cardHeight = (int)(150 * Game.SCALE);
    private int cardY      = Game.GAME_HEIGHT / 2 - (int)(150 * Game.SCALE) / 2;
    private int spacing    = (int)(30 * Game.SCALE);

    public CharacterSelect(Game game) {
        super(game);
        backgroundImg = LoadSave.GetSpriteAtlas(LoadSave.CHARACTER_SELECT_BG);
        initCards();
    }

    private void initCards() {
        int totalWidth = 3 * cardWidth + 2 * spacing;
        int startX = Game.GAME_WIDTH / 2 - totalWidth / 2;
        cards = new CharacterCard[3];

        cards[0] = new CharacterCard("Brawler",
            startX, cardY, cardWidth, cardHeight,
            "sprites/Brawler/IdleAni_Brawler.png", 14,
            new CharacterStats(100, MAX_STAMINA,
                "Punch",  BRAWLER_ATTACK_DMG,
                "Slam",   BRAWLER_SKILL2_DMG,
                "Smash",  BRAWLER_SKILL3_DMG));

        cards[1] = new CharacterCard("Mage",
            startX + cardWidth + spacing, cardY, cardWidth, cardHeight,
            "sprites/Mage/IdleAni_Mage.png", 9,
            new CharacterStats(70, MAX_STAMINA,
                "Bolt",   MAGE_ATTACK_DMG,
                "Heal",   0,
                "Blast",  MAGE_SKILL3_DMG));

        cards[2] = new CharacterCard("Assassin",
            startX + (cardWidth+spacing)*2, cardY, cardWidth, cardHeight,
            "sprites/Assassin/IdleAni_Assassin.png", 8,
            new CharacterStats(80, MAX_STAMINA,
                "Slash",  ASSASSIN_ATTACK_DMG,
                "Throw",  ASSASSIN_SKILL2_DMG,
                "Flurry", ASSASSIN_SKILL3_DMG));
    }

    @Override
    public void update() {
        for (CharacterCard c : cards) c.update();
    }

    @Override
    public void draw(Graphics g) {
        g.drawImage(backgroundImg, 0, 0, Game.GAME_WIDTH, Game.GAME_HEIGHT, null);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Monospaced", Font.BOLD, (int)(12 * Game.SCALE)));
        FontMetrics fm = g.getFontMetrics();
        String title = "SELECT YOUR CHARACTER";
        g.drawString(title, Game.GAME_WIDTH/2 - fm.stringWidth(title)/2, (int)(30 * Game.SCALE));

        for (int i = 0; i < cards.length; i++)
            cards[i].draw(g, i == hoveredCard);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        hoveredCard = -1;
        for (int i = 0; i < cards.length; i++)
            if (cards[i].getBounds().contains(e.getX(), e.getY()))
                hoveredCard = i;
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        for (CharacterCard card : cards)
            if (card.getBounds().contains(e.getX(), e.getY())) {
                selectCharacter(card.getName());
                return;
            }
    }

    private void selectCharacter(String name) {
        Playing playing = game.getPlaying();
        switch (name) {
            case "Brawler"  -> playing.setPlayer(new Brawler( 200, 200, (int)(64*Game.SCALE), (int)(40*Game.SCALE), playing));
            case "Mage"     -> playing.setPlayer(new Mage(    200, 200, (int)(64*Game.SCALE), (int)(40*Game.SCALE), playing));
            case "Assassin" -> playing.setPlayer(new Assassin(200, 200, (int)(64*Game.SCALE), (int)(40*Game.SCALE), playing));
        }
        game.getStoryManager().startStory();
    }

    @Override public void mouseClicked(MouseEvent e) {}
    @Override public void mousePressed(MouseEvent e) {}
    public void mouseDragged(MouseEvent e) {}
    @Override public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
            Gamestate.state = Gamestate.MENU;
    }
    @Override public void keyReleased(KeyEvent e) {}
}