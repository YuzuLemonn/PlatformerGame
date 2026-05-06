package gamestates;

import audio.AudioPlayer;
import main.Game;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import utilz.LoadSave;

public class StoryManager {
    private Game game;
    private ArrayList<Scene> scenes = new ArrayList<>();
    private int currentSceneIndex = 0;
    private int currentLineIndex = 0;

    // cutscene rendering
    private float textAlpha = 0f;
    private boolean fadingIn = true;
    private static final float FADE_SPEED = 0.02f;
    private BufferedImage cutsceneBg;

    public StoryManager(Game game) {
        this.game = game;
        cutsceneBg = LoadSave.GetSpriteAtlas("black_bg.png"); // black png or null
        buildScenes();
    }

    private void buildScenes() {
        // PROLOGUE
        scenes.add(new Scene(null, new String[]{
                "I was born in the depths of the earth...",
                "where the air was cool and the only light",
                "came from glowing mushrooms along the cave walls.",
                "My village, Abyssos, was carved from stone.",
                "But to me... it always felt like a prison.",
                "...",
                "Press SPACE to continue"
        }, AudioPlayer.MENU_1));

        // VILLAGE — level 0 (1.png)
        scenes.add(new Scene(SceneType.LEVEL, 0, AudioPlayer.LEVEL_1));

        // PRE-BOSS 1 DIALOGUE
        scenes.add(new Scene("TYRONE", new String[]{
                "I am TYRONE, the Worm-Keeper.",
                "None leave Abyssos.",
                "",
                "Player: I'm done living beneath everything.",
                "",
                "TYRONE: Return... or be devoured.",
                "",
                "Player: Then I'll rise—or die trying.",
                "",
                "Press SPACE to fight"
        }, AudioPlayer.LEVEL_1));

        // BOSS 1 — level 1 (2.png)
        scenes.add(new Scene(SceneType.BOSS, 1, AudioPlayer.LEVEL_1));

        // POST-BOSS 1
        scenes.add(new Scene(null, new String[]{
                "Tyrone's final screech echoed through the tunnels",
                "as I climbed past the rubble.",
                "",
                "The air grew hotter.",
                "The stone glowed red.",
                "",
                "I had reached the next world.",
                "",
                "Press SPACE to continue"
        }, AudioPlayer.LEVEL_2));

        // INFERNAL DEPTHS — level 2 (3.png)
        scenes.add(new Scene(SceneType.LEVEL, 2, AudioPlayer.LEVEL_2));

        // PRE-BOSS 2
        scenes.add(new Scene("Gabryle, the Starcaller", new String[]{
                "No mortal shall pass this abyss.",
                "",
                "Player: I didn't come this far to stop now.",
                "",
                "Starcaller: Then your soul will remain here forever.",
                "",
                "Press SPACE to fight"
        }, AudioPlayer.LEVEL_2));

        // BOSS 2 — level 3 (4.png)
        scenes.add(new Scene(SceneType.BOSS, 3, AudioPlayer.LEVEL_2));

        // POST-BOSS 2
        scenes.add(new Scene(null, new String[]{
                "Through the smoke, I saw something new.",
                "",
                "Green.",
                "Trees.",
                "Life.",
                "",
                "Press SPACE to continue"
        }, AudioPlayer.LEVEL_1));

        // SURFACE REALM — level 4 (5.png)
        scenes.add(new Scene(SceneType.LEVEL, 4, AudioPlayer.LEVEL_1));

        // SYLENTHRA PRE-BOSS
        scenes.add(new Scene("Sylenthra", new String[]{
                "Speak your name, trespasser.",
                "",
                "Player: (Player) of Abyssos. I seek the surface.",
                "",
                "Sylenthra: The forest does not allow strangers to pass freely.",
                "",
                "Player: I have come too far to turn back.",
                "",
                "Sylenthra: Then face the truth of the forest.",
                "",
                "Press SPACE to fight"
        }, AudioPlayer.LEVEL_1));

        // BOSS 3 — level 5 (6.png)
        scenes.add(new Scene(SceneType.BOSS, 5, AudioPlayer.LEVEL_1));

        // POST SYLENTHRA + SER CHRISTIAN PRE-BOSS
        scenes.add(new Scene("Ser Christian", new String[]{
                "Sylenthra's body slowly turned into flowers.",
                "",
                "Sylenthra: You are not bound by fear... rise.",
                "",
                "Beyond the forest, I finally stepped into an open field.",
                "For the first time in my life, I saw the sun.",
                "",
                "Ser Christian: You are not yet free.",
                "Player: Who are you?",
                "Ser Christian: I am Ser Christian, the Last Gatekeeper.",
                "Player: I've crossed demons and darkness to reach this place.",
                "Ser Christian: Then prove that you belong here.",
                "Player: Fine.",
                "",
                "Press SPACE to fight"
        }, AudioPlayer.LEVEL_1));

        // FINAL BOSS — level 6 (7.png)
        scenes.add(new Scene(SceneType.BOSS, 6, AudioPlayer.LEVEL_1));

        // ENDING
        scenes.add(new Scene(null, new String[]{
                "Ser Christian: You've ascended...",
                "not just through worlds, but within yourself.",
                "",
                "Player: So... I belong here?",
                "Ser Christian: The surface is not easy.",
                "Player: I didn't expect it to be.",
                "Ser Christian: Then welcome to the world above.",
                "",
                "The sky stretched endlessly above me.",
                "For the first time, I felt truly free.",
                "",
                "Player (Inner Voice):",
                "\"I left the darkness seeking the light...",
                "but along the way, I discovered who I truly am.\"",
                "",
                "Press SPACE for credits"
        }, AudioPlayer.MENU_1));
    }

    public void startStory() {
        currentSceneIndex = 0;
        currentLineIndex = 0;
        textAlpha = 0f;
        fadingIn = true;
        loadCurrentScene();
    }

    private void loadCurrentScene() {
        Scene scene = getCurrentScene();
        game.getAudioPlayer().setLevelSong(scene.bgmIndex == AudioPlayer.MENU_1 ? 0 : 1);

        if (scene.type == SceneType.LEVEL || scene.type == SceneType.BOSS) {
            game.getPlaying().getLevelManager().setLevelIndex(scene.levelIndex);
            game.getPlaying().loadStartLevelByIndex(scene.levelIndex);
            Gamestate.state = Gamestate.PLAYING;
        } else {
            currentLineIndex = 0;
            textAlpha = 0f;
            fadingIn = true;
            Gamestate.state = Gamestate.CUTSCENE;
        }
    }

    public void advanceScene() {
        currentSceneIndex++;
        if (currentSceneIndex >= scenes.size()) {
            // story complete, go to credits then menu
            Gamestate.state = Gamestate.CREDITS;
            return;
        }
        loadCurrentScene();
    }

    public void advanceLine() {
        Scene scene = getCurrentScene();
        if (scene.lines == null) return;
        currentLineIndex++;
        textAlpha = 0f;
        fadingIn = true;
        if (currentLineIndex >= scene.lines.length) {
            advanceScene();
        }
    }

    public void update() {
        // fade text in/out
        if (fadingIn) {
            textAlpha += FADE_SPEED;
            if (textAlpha >= 1f) {
                textAlpha = 1f;
                fadingIn = false;
            }
        }
    }

    public void draw(Graphics g) {
        // black background
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, Game.GAME_WIDTH, Game.GAME_HEIGHT);

        Scene scene = getCurrentScene();
        if (scene.lines == null || currentLineIndex >= scene.lines.length) return;

        // speaker name
        if (scene.speaker != null) {
            g.setColor(new Color(255, 200, 100));
            g.setFont(new Font("Monospaced", Font.BOLD, (int)(10 * Game.SCALE)));
            FontMetrics fm = g.getFontMetrics();
            g.drawString(scene.speaker,
                    Game.GAME_WIDTH/2 - fm.stringWidth(scene.speaker)/2,
                    (int)(Game.GAME_HEIGHT * 0.35f));
        }

        // current dialogue line with fade
        String line = scene.lines[currentLineIndex];
        g.setFont(new Font("Monospaced", Font.PLAIN, (int)(8 * Game.SCALE)));
        FontMetrics fm = g.getFontMetrics();
        g.setColor(new Color(255, 255, 255, (int)(textAlpha * 255)));
        g.drawString(line,
                Game.GAME_WIDTH/2 - fm.stringWidth(line)/2,
                (int)(Game.GAME_HEIGHT * 0.5f));

        // prompt
        if (textAlpha >= 1f) {
            g.setColor(new Color(180, 180, 180));
            g.setFont(new Font("Monospaced", Font.ITALIC, (int)(6 * Game.SCALE)));
            String prompt = "SPACE / E to continue";
            FontMetrics fmP = g.getFontMetrics();
            g.drawString(prompt,
                    Game.GAME_WIDTH/2 - fmP.stringWidth(prompt)/2,
                    (int)(Game.GAME_HEIGHT * 0.75f));
        }
    }

    public void onKeyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE ||
                e.getKeyCode() == KeyEvent.VK_E) {
            if (textAlpha >= 1f) // only advance when fully visible
                advanceLine();
        }
    }

    // called by Playing when a level/boss is completed
    public void onLevelComplete() {
        advanceScene();
    }

    public Scene getCurrentScene() {
        return scenes.get(currentSceneIndex);
    }

    public boolean currentSceneIsBoss() {
        return getCurrentScene().type == SceneType.BOSS;
    }
}