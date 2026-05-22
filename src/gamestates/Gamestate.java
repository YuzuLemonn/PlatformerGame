package gamestates;

public enum Gamestate {
    PLAYING, MENU, OPTIONS, QUIT, CREDITS, CHARACTER_SELECT, CUTSCENE, LEADERBOARD;

    public static Gamestate state = MENU;
}
