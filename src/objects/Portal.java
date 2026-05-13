package objects;

import main.Game;
import static utilz.Constants.ObjectConstants.*;

public class Portal extends GameObject {
    private boolean unlocked = false;
    private boolean doAnimation;

    public Portal(int x, int y) {
        super(x, y, PORTAL);
        initHitbox(16, 32);
        hitbox.x -= (int)(8 * Game.SCALE);
    }

    public void update(boolean allEnemiesCleared) {

        if (allEnemiesCleared && !unlocked) {
            unlocked = true;
            doAnimation = true;
        }
        if (doAnimation)
            updateAnimationTick();
    }

    private void updateAnimationTick() {
        aniTick++;
        if (aniTick >= utilz.Constants.ANI_SPEED) {
            aniTick = 0;
            aniIndex++;
            if (aniIndex >= GetSpriteAmount(PORTAL))
                aniIndex = 0;
        }
    }

    public boolean isUnlocked() { return unlocked; }

    // no enemies = unlock portal
    public void forceUnlock() {
        unlocked = true;
        doAnimation = true;
    }

    public void reset() {
        unlocked = false;
        doAnimation = false;
        aniTick = 0;
        aniIndex = 0;
    }
}