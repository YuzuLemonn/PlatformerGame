package gamestates;

import entities.players.Assassin;
import entities.players.Brawler;
import entities.players.Mage;
import main.Game;
import ui.BossCutscene;
import utilz.Leaderboard;
import utilz.LoadSave;

import static utilz.Constants.DamageConstants.*;
import static utilz.Constants.PlayerConstants.MAX_STAMINA;
import static utilz.Constants.CharacterStats;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

public class CharacterSelect extends State implements Statemethods {
    private BufferedImage backgroundImg;
    private CharacterCard[] cards;
    private int hoveredCard = -1;
    private String playerName = "";

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
                "Basic Punch",  BRAWLER_ATTACK_DMG,
                "Haymaker",   BRAWLER_SKILL2_DMG,
                "Showstopper",  BRAWLER_SKILL3_DMG));

        cards[1] = new CharacterCard("Mage",
            startX + cardWidth + spacing, cardY, cardWidth, cardHeight,
            "sprites/Mage/IdleAni_Mage.png", 9,
            new CharacterStats(70, MAX_STAMINA,
                "Fire Ball",   MAGE_ATTACK_DMG,
                "Perseverance",   0,
                "Crescendo",  MAGE_SKILL3_DMG));

        cards[2] = new CharacterCard("Assassin",
            startX + (cardWidth+spacing)*2, cardY, cardWidth, cardHeight,
            "sprites/Assassin/IdleAni_Assassin.png", 8,
            new CharacterStats(80, MAX_STAMINA,
                "Slash",  ASSASSIN_ATTACK_DMG,
                "Throwing Knife",  ASSASSIN_SKILL2_DMG,
                "Backstab", ASSASSIN_SKILL3_DMG));
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

        drawNameInput(g);

        for (int i = 0; i < cards.length; i++)
            cards[i].draw(g, i == hoveredCard);
    }

    private void drawNameInput(Graphics g) {
        int boxW = (int)(250 * Game.SCALE);
        int boxH = (int)(26 * Game.SCALE);
        int boxX = Game.GAME_WIDTH / 2 - boxW / 2;
        int boxY = (int)(52 * Game.SCALE);

        g.setColor(new Color(0, 0, 0, 160));
        g.fillRect(boxX, boxY, boxW, boxH);
        g.setColor(new Color(230, 210, 150));
        g.drawRect(boxX, boxY, boxW, boxH);

        g.setFont(new Font("Monospaced", Font.PLAIN, (int)(7 * Game.SCALE)));
        g.setColor(Color.WHITE);
        String nameToShow = playerName.isEmpty() ? "Player" : playerName;
        String text = "NAME: " + nameToShow + "_";
        FontMetrics fm = g.getFontMetrics();
        g.drawString(text, Game.GAME_WIDTH / 2 - fm.stringWidth(text) / 2,
                boxY + boxH / 2 + fm.getAscent() / 2 - 2);
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
        case "Brawler" -> {
            playing.setPlayer(new Brawler(200, 200, (int)(64*Game.SCALE), (int)(40*Game.SCALE), playing));
            playing.getPlayer().setPlayerClass("BRAWLER");
            BossCutscene.setPlayerName("Sett");
        }

        case "Mage" -> {
            playing.setPlayer(new Mage(200, 200, (int)(64*Game.SCALE), (int)(40*Game.SCALE), playing));
            playing.getPlayer().setPlayerClass("MAGE");
            BossCutscene.setPlayerName("Sonia");
        }
        
        case "Assassin" -> {
            playing.setPlayer(new Assassin(200, 200, (int)(64*Game.SCALE), (int)(40*Game.SCALE), playing));
            playing.getPlayer().setPlayerClass("ASSASSIN");
            BossCutscene.setPlayerName("Sylas");
        }
    }

    game.startRun(playerName);
    game.getStoryManager().startStory();
    }

    @Override public void mouseClicked(MouseEvent e) {}

    @Override public void mousePressed(MouseEvent e) {}

    public void mouseDragged(MouseEvent e) {}

    @Override public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            Gamestate.state = Gamestate.MENU;
            return;
        }

        if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
            if (!playerName.isEmpty())
                playerName = playerName.substring(0, playerName.length() - 1);
            return;
        }

        if (e.getKeyCode() == KeyEvent.VK_ENTER)
            return;

        char c = e.getKeyChar();
        if (playerName.length() < 16 &&
                (Character.isLetterOrDigit(c) || c == ' ' || c == '_' || c == '-')) {
            playerName = Leaderboard.sanitizeName(playerName + c);
        }
    }

    @Override public void keyReleased(KeyEvent e) {}
}
