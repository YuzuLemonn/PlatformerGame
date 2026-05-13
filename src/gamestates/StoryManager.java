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
                "In the depths of the earth, I was born...",
                "where the only light came from glowing mushrooms.",
                "My village, Abyssos, was carved from stone.",
                "But to me... it always felt like a prison.",
                "...",
                "Press SPACE to continue"
        }, AudioPlayer.MENU_1));

        // LEVEL 1 - village
        scenes.add(new Scene(SceneType.LEVEL, 0, AudioPlayer.LEVEL_1));

        // LEVEL 2 - minion
        scenes.add(new Scene(SceneType.LEVEL, 1, AudioPlayer.LEVEL_1));

        scenes.add(new Scene(SceneType.BOSS, 2, AudioPlayer.LEVEL_1));

        // POST-BOSS 1  (player stays in level 3 — portal is now open)
        scenes.add(new Scene(null, new String[]{
                "Josiah's final screech echoed through the tunnels",
                "as I climbed past the rubble.",
                "The air grew hotter.",
                "The stone glowed red.",
                "I had reached the next world.",
                "...",
                "Press SPACE to continue"
        }, AudioPlayer.LEVEL_2));

        // RETURN TO PLAYING so player can use the portal
        scenes.add(new Scene(SceneType.RESUME, -1, AudioPlayer.LEVEL_2));

        // LEVEL 4 - minion world 2
        scenes.add(new Scene(SceneType.LEVEL, 3, AudioPlayer.LEVEL_2));

        scenes.add(new Scene(SceneType.BOSS, 4, AudioPlayer.LEVEL_2));

        // POST-BOSS 2
        scenes.add(new Scene(null, new String[]{
                "Through the smoke, I saw something new.",
                "Green.   Trees.   Life.",
                "For the first time, I breathed clean air.",
                "...",
                "Press SPACE to continue"
        }, AudioPlayer.LEVEL_1));

        scenes.add(new Scene(SceneType.RESUME, -1, AudioPlayer.LEVEL_1));

        // LEVEL 6 - minion world 3
        scenes.add(new Scene(SceneType.LEVEL, 5, AudioPlayer.LEVEL_1));

        // LEVEL 7 - boss 3
        scenes.add(new Scene(SceneType.BOSS, 6, AudioPlayer.LEVEL_1));

        // ENDING
        scenes.add(new Scene("Ser Christian", new String[]{
                "Ser Christian:  You've ascended...",
                "  not just through worlds, but within yourself.",
                "Player:  So... I belong here?",
                "Ser Christian:  The surface is not easy.",
                "Player:  I didn't expect it to be.",
                "Ser Christian:  Then welcome to the world above.",
                "The sky stretched endlessly above me.",
                "For the first time, I felt truly free.",
                "Player (Inner Voice):",
                "  I left the darkness seeking the light...",
                "  but along the way, I found who I truly am.",
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

        if (scene.type == SceneType.LEVEL || scene.type == SceneType.BOSS) {
            game.getAudioPlayer().setLevelSong(scene.levelIndex);
            game.getPlaying().loadStartLevelByIndex(scene.levelIndex);
            Gamestate.state = Gamestate.PLAYING;
        } else if (scene.type == SceneType.RESUME) {
            game.getAudioPlayer().resumeSongAfterCutscene();
            Gamestate.state = Gamestate.PLAYING;
        } else {
            game.releaseAllPressedKeys();
            game.getPlaying().windowFocusLost();
            game.getAudioPlayer().pauseSongForCutscene();
            currentLineIndex = 0;
            textAlpha = 0f;
            fadingIn = true;
            Gamestate.state = Gamestate.CUTSCENE;
        }
    }

    public void advanceScene() {
        currentSceneIndex++;
        if (currentSceneIndex >= scenes.size()) {
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
            g.setFont(new Font("Monospaced", Font.BOLD, (int) (10 * Game.SCALE)));
            FontMetrics fm = g.getFontMetrics();
            g.drawString(scene.speaker,
                    Game.GAME_WIDTH / 2 - fm.stringWidth(scene.speaker) / 2,
                    (int) (Game.GAME_HEIGHT * 0.35f));
        }

        // current dialogue line with fade
        String line = scene.lines[currentLineIndex];
        g.setFont(new Font("Monospaced", Font.PLAIN, (int) (8 * Game.SCALE)));
        FontMetrics fm = g.getFontMetrics();
        g.setColor(new Color(255, 255, 255, (int) (textAlpha * 255)));
        g.drawString(line,
                Game.GAME_WIDTH / 2 - fm.stringWidth(line) / 2,
                (int) (Game.GAME_HEIGHT * 0.5f));

        if (textAlpha >= 1f) {
            g.setColor(new Color(180, 180, 180));
            g.setFont(new Font("Monospaced", Font.ITALIC, (int) (6 * Game.SCALE)));
            String prompt = "SPACE / E to continue";
            FontMetrics fmP = g.getFontMetrics();
            g.drawString(prompt,
                    Game.GAME_WIDTH / 2 - fmP.stringWidth(prompt) / 2,
                    (int) (Game.GAME_HEIGHT * 0.75f));
        }
    }

    public void onKeyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE ||
                e.getKeyCode() == KeyEvent.VK_E) {
            if (textAlpha >= 1f) // only advance when fully visible
                advanceLine();
        }
    }

    public void onLevelComplete() {
        advanceScene();
    }

    public void onBossDefeated(int levelIndex) {
        int bossSceneIndex = findBossSceneIndex(levelIndex);

        if (bossSceneIndex != -1)
            currentSceneIndex = bossSceneIndex;

        advanceScene();
    }

    private int findBossSceneIndex(int levelIndex) {
        for (int i = 0; i < scenes.size(); i++) {
            Scene scene = scenes.get(i);
            if (scene.type == SceneType.BOSS && scene.levelIndex == levelIndex)
                return i;
        }
        return -1;
    }

    public Scene getCurrentScene() {
        return scenes.get(currentSceneIndex);
    }

    public boolean currentSceneIsBoss() {
        return getCurrentScene().type == SceneType.BOSS;
    }
}
