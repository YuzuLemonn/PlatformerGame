package gamestates;

public enum Gamestate {
    PLAYING, MENU, OPTIONS, QUIT, CREDITS, CHARACTER_SELECT, CUTSCENE;

    public static Gamestate state = MENU;
}