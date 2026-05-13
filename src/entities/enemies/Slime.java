package entities.enemies;

import entities.Enemy;
import entities.Player;
import main.Game;
import java.awt.geom.Rectangle2D;

import static utilz.Constants.EnemyConstants.*;

public class Slime extends Enemy {

    public Slime(float x, float y) {
        super(x, y, SLIME_WIDTH, SLIME_HEIGHT, SLIME);
        initHitbox(22, 19);
        initAttackBox();
        walkSpeed = Game.SCALE * 0.3f;
    }

    private void initAttackBox() {
        attackBox = new Rectangle2D.Float(x, y,
                (int)(60 * Game.SCALE), (int)(19 * Game.SCALE));
        attackBoxOffsetX = (int)(Game.SCALE * 20);
    }

    public void update(int[][] lvlData, Player player) {
        updateBehavior(lvlData, player);
        updateAnimationTick();
        updateAttackBox();
    }

    private void updateBehavior(int[][] lvlData, Player player) {
        if (firstUpdate)
            firstUpdateCheck(lvlData);
        if (inAir)
            updateInAir(lvlData);
        else {
            switch (state) {
                case IDLE -> newState(RUNNING);
                case RUNNING -> {
                    if (canSeePlayer(lvlData, player)) {
                        turnTowardsPlayer(player);
                        if (isPlayerCloseForAttack(player))
                            newState(ATTACK);
                    }
                    move(lvlData);
                }
                case ATTACK -> {
                    if (aniIndex == 0) attackChecked = false;
                    if (aniIndex == 3 && !attackChecked)
                        checkEnemyHit(attackBox, player);
                }
                case HIT -> {}
            }
        }
    }

    @Override
    public int getCoinValue() { return 7; }
}