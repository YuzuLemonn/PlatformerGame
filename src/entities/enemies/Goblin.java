package entities.enemies;

import entities.Enemy;
import entities.Player;
import main.Game;

import static utilz.Constants.EnemyConstants.*;

public class Goblin extends Enemy {

    public Goblin(float x, float y) {
        super(x, y, GOBLIN_WIDTH, GOBLIN_HEIGHT, GOBLIN);
        initHitbox(20, 28);
        initAttackBox(20, 28, (int)(Game.SCALE * 10));
        walkSpeed = Game.SCALE * 0.40f; // faster than slime
    }

    public void update(int[][] lvlData, Player player) {
        updateBehavior(lvlData, player);
        updateAnimationTick();
        updateAttackBoxFlip();
    }

    private void updateBehavior(int[][] lvlData, Player player) {
        if (firstUpdate)
            firstUpdateCheck(lvlData);

        if (state == HIT) {
            updateKnockback(lvlData);
            if (!inAir) {
                pushDrawOffset = 0;
                newState(IDLE);
            }
            return;
        }

        if (inAir) {
            updateInAir(lvlData);
        } else {
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
                    if (aniIndex == 1 && !attackChecked)
                        checkEnemyHit(attackBox, player);
                }
            }
        }
    }

    @Override
    public int getCoinValue() { return 8; }
}