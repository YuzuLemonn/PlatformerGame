package gamestates;

public enum Gamestate {
    PLAYING, MENU, OPTIONS, QUIT, CREDITS, CHARACTER_SELECT;

    public static Gamestate state = MENU;
}
