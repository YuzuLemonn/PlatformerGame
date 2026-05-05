package gamestates;

public class Scene {
    public final SceneType type;
    public final int levelIndex;     // which lvl file to load (-1 for cutscenes)
    public final String[] lines;     // dialogue/narration lines
    public final String speaker;     // who is speaking (null = narrator)
    public final int bgmIndex;       // which bgm to play

    // cutscene constructor
    public Scene(String speaker, String[] lines, int bgmIndex) {
        this.type = SceneType.CUTSCENE;
        this.levelIndex = -1;
        this.speaker = speaker;
        this.lines = lines;
        this.bgmIndex = bgmIndex;
    }

    // level/boss constructor
    public Scene(SceneType type, int levelIndex, int bgmIndex) {
        this.type = type;
        this.levelIndex = levelIndex;
        this.speaker = null;
        this.lines = null;
        this.bgmIndex = bgmIndex;
    }
}