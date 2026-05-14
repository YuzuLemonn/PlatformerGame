package utilz;

import main.Game;

public class Constants {
    public static final float GRAVITY = 0.04f * Game.SCALE;
    public static final int ANI_SPEED = 25;

    public static class CharacterStats {
    public int hp;
    public int mana;
    public String skill1Name;
    public int skill1Dmg;
    public String skill2Name;
    public int skill2Dmg;
    public String skill3Name;
    public int skill3Dmg;

    public CharacterStats(int hp, int mana,
                          String skill1Name, int skill1Dmg,
                          String skill2Name, int skill2Dmg,
                          String skill3Name, int skill3Dmg) {
        this.hp         = hp;
        this.mana       = mana;
        this.skill1Name = skill1Name;
        this.skill1Dmg  = skill1Dmg;
        this.skill2Name = skill2Name;
        this.skill2Dmg  = skill2Dmg;
        this.skill3Name = skill3Name;
        this.skill3Dmg  = skill3Dmg;
    }
    }

    public static class EnemyConstants {
        public static final int SLIME = 22;
        public static final int GOBLIN = 44;
        public static final int ZOMBIE = 1;

        public static final int IDLE = 0;
        public static final int RUNNING = 1;
        public static final int ATTACK = 2;
        public static final int HIT = 3;
        public static final int DEAD = 4;

        public static final int GOBLIN_WIDTH_DEFAULT = 54;
        public static final int GOBLIN_HEIGHT_DEFAULT = 56;
        public static final int GOBLIN_WIDTH = (int)(GOBLIN_WIDTH_DEFAULT * Game.SCALE);
        public static final int GOBLIN_HEIGHT = (int)(GOBLIN_HEIGHT_DEFAULT * Game.SCALE);
        public static final int GOBLIN_DRAWOFFSET_X = (int)(17 * Game.SCALE);
        public static final int GOBLIN_DRAWOFFSET_Y = (int)(22 * Game.SCALE);

        public static final int SLIME_WIDTH_DEFAULT = 54;
        public static final int SLIME_HEIGHT_DEFAULT = 56;
        public static final int SLIME_WIDTH = (int)(SLIME_WIDTH_DEFAULT * Game.SCALE);
        public static final int SLIME_HEIGHT = (int)(SLIME_HEIGHT_DEFAULT * Game.SCALE);
        public static final int SLIME_DRAWOFFSET_X = (int)(16 * Game.SCALE);
        public static final int SLIME_DRAWOFFSET_Y = (int)(30 * Game.SCALE);

        public static final int ZOMBIE_WIDTH_DEFAULT = 40;
        public static final int ZOMBIE_HEIGHT_DEFAULT = 40;
        public static final int ZOMBIE_WIDTH = (int)(ZOMBIE_WIDTH_DEFAULT * Game.SCALE);
        public static final int ZOMBIE_HEIGHT = (int)(ZOMBIE_HEIGHT_DEFAULT * Game.SCALE);
        public static final int ZOMBIE_DRAWOFFSET_X = (int)(5 * Game.SCALE);
        public static final int ZOMBIE_DRAWOFFSET_Y = (int)(8 * Game.SCALE);

        

        public static int GetSpriteAmount(int enemy_type, int enemy_state) {
            switch(enemy_state) {
                case IDLE:
                case RUNNING:
                    if(enemy_type == ZOMBIE)  return 6;
                    if(enemy_type == SLIME)   return 9;
                    if(enemy_type == GOBLIN)  return 9;
                    return 9;
                case ATTACK:
                    if(enemy_type == ZOMBIE)  return 5;
                    if(enemy_type == SLIME)   return 7;
                    if(enemy_type == GOBLIN)  return 3;
                    return 7;
                case HIT:
                    if(enemy_type == ZOMBIE)  return 3;
                    if(enemy_type == SLIME)   return 3;
                    if(enemy_type == GOBLIN)  return 3;
                    return 3;
                case DEAD:
                    if(enemy_type == ZOMBIE)  return 11;
                    if(enemy_type == SLIME)   return 6;
                    if(enemy_type == GOBLIN)  return 9;
                    return 6;
            }
            return 0;
        }

        public static int GetMaxHealth(int enemy_type) {
            switch(enemy_type) {
                case ZOMBIE: return 15;
                case SLIME:  return 25;
                case GOBLIN: return 35;
                default: return 1;
            }
        }

        public static int GetEnemyDmg(int enemy_type) {
            switch(enemy_type) {
                case SLIME:  return 15;
                case GOBLIN: return 20;
                case ZOMBIE: return 10;
                default: return 0;
            }
        }
    }

    public static class DamageConstants {
        // Player attacks
        public static final int MAGE_ATTACK_DMG   = 20;
        public static final int MAGE_SKILL2_DMG   = 0;  // heal, no damage
        public static final int MAGE_SKILL3_DMG   = 30;

        public static final int ASSASSIN_ATTACK_DMG = 18;
        public static final int ASSASSIN_SKILL2_DMG = 25;
        public static final int ASSASSIN_SKILL3_DMG = 35;

        public static final int BRAWLER_ATTACK_DMG  = 15;
        public static final int BRAWLER_SKILL2_DMG  = 25;
        public static final int BRAWLER_SKILL3_DMG  = 15;
    }

    public static class ObjectConstants {
        public static final int SPIKE = 4;
        public static final int PORTAL = 100;

        public static final int SPIKE_WIDTH_DEFAULT = 32;
        public static final int SPIKE_HEIGHT_DEFAULT = 32;
        public static final int SPIKE_WIDTH = (int)(Game.SCALE * SPIKE_WIDTH_DEFAULT);
        public static final int SPIKE_HEIGHT = (int)(Game.SCALE * SPIKE_HEIGHT_DEFAULT);

        public static final int PORTAL_WIDTH_DEFAULT  = 32;
        public static final int PORTAL_HEIGHT_DEFAULT = 32;
        public static final int PORTAL_WIDTH  = (int)(PORTAL_WIDTH_DEFAULT  * Game.SCALE);
        public static final int PORTAL_HEIGHT = (int)(PORTAL_HEIGHT_DEFAULT * Game.SCALE);
        public static final int PORTAL_DRAWOFFSET_X = 0;
        public static final int PORTAL_DRAWOFFSET_Y = 0;

        public static int GetSpriteAmount(int objType) {
            return switch (objType) {
                case SPIKE  -> 1;
                case PORTAL -> 6;
                default -> 1;
            };
        }
    }

    public static class Environment {
        public static final int BIG_CLOUD_WIDTH_DEFAULT = 448;
        public static final int BIG_CLOUD_HEIGHT_DEFAULT = 101;
        public static final int SMALL_CLOUD_WIDTH_DEFAULT = 74;
        public static final int SMALL_CLOUD_HEIGHT_DEFAULT = 24;

        public static final int BIG_CLOUD_WIDTH = (int)(BIG_CLOUD_WIDTH_DEFAULT * Game.SCALE);
        public static final int BIG_CLOUD_HEIGHT = (int)(BIG_CLOUD_HEIGHT_DEFAULT * Game.SCALE);
        public static final int SMALL_CLOUD_WIDTH = (int)(SMALL_CLOUD_WIDTH_DEFAULT * Game.SCALE);
        public static final int SMALL_CLOUD_HEIGHT = (int)(SMALL_CLOUD_HEIGHT_DEFAULT * Game.SCALE);
    }

    public static class UI {
        public static class Buttons {
            public static final int B_WIDTH_DEFAULT = 140;
            public static final int B_HEIGHT_DEFAULT = 56;
            public static final int B_WIDTH = (int)(B_WIDTH_DEFAULT * Game.SCALE);
            public static final int B_HEIGHT = (int)(B_HEIGHT_DEFAULT * Game.SCALE);
        }

        public static class PauseButtons {
            public static final int SOUND_SIZE_DEFAULT = 42;
            public static final int SOUND_SIZE = (int)(SOUND_SIZE_DEFAULT * Game.SCALE);
        }

        public static class URMButtons {
            public static final int URM_DEFAULT_SIZE = 56;
            public static final int URM_SIZE = (int)(URM_DEFAULT_SIZE * Game.SCALE);
        }

        public static class VolumeButtons {
            public static final int VOLUME_DEFAULT_WIDTH = 28;
            public static final int VOLUME_DEFAULT_HEIGHT = 44;
            public static final int SLIDER_DEFAULT_WIDTH = 215;

            public static final int VOLUME_WIDTH = (int)(VOLUME_DEFAULT_WIDTH * Game.SCALE);
            public static final int VOLUME_HEIGHT = (int)(VOLUME_DEFAULT_HEIGHT * Game.SCALE);
            public static final int SLIDER_WIDTH = (int)(SLIDER_DEFAULT_WIDTH * Game.SCALE);
        }
    }

    public static class Directions {
        public static final int LEFT = 0;
        public static final int UP = 1;
        public static final int RIGHT = 2;
        public static final int DOWN = 3;
    }

    public static class PlayerConstants {
        public static final int IDLE = 0;
        public static final int RUNNING = 1;
        public static final int JUMP = 2;
        public static final int FALLING = 3;
        public static final int ATTACK = 4; 
        public static final int HIT = 5;
        public static final int DEAD = 6;
        public static final int SKILL2  = 7;
        public static final int SKILL3  = 8;
        public static final int MAX_STAMINA = 100;
        public static final float STAMINA_REGEN_PASSIVE = 0.01f;
        public static final float STAMINA_REGEN_IDLE    = 0.10f;
        public static final int   STAMINA_COST_ATTACK   = 15;
        public static final int   STAMINA_COST_SKILL2   = 25;
        public static final int   STAMINA_COST_SKILL3   = 40;
        public static final float ASSASSIN_SKILL3_RANGE = 700 * Game.SCALE;

        public static int GetSpriteAmount(int player_action, String character) {
            switch (character) {
                case "Mage" -> {
                    return switch (player_action) {
                        case IDLE    -> 9;  // adjust to actual frame count of character
                        case RUNNING -> 8;
                        case JUMP    -> 8;
                        case ATTACK  -> 5;
                        case SKILL2  -> 8; 
                        case SKILL3  -> 8;
                        default      -> 1;
                    };
                }
                case "Assassin" -> {
                    return switch (player_action) {
                        case IDLE    -> 8;
                        case RUNNING -> 8;
                        case JUMP    -> 6;
                        case ATTACK  -> 5;
                        case SKILL2  -> 4;
                        case SKILL3  -> 22;
                        default      -> 1;
                    };
                }
                default -> { // Brawler
                    return switch (player_action) {
                        case IDLE    -> 14;
                        case RUNNING -> 8;
                        case JUMP    -> 7;
                        case ATTACK  -> 3;
                        case SKILL2  -> 5;
                        case SKILL3  -> 13;
                        default      -> 1;
                    };
                }
            }
        }

        // keep old one for Player base class death/hit states
        public static int GetSpriteAmount(int player_action) {
            return switch (player_action) {
                case IDLE    -> 14;
                case RUNNING -> 8;
                case JUMP    -> 7;
                case ATTACK  -> 3;
                default      -> 1;
            };
        }
    }

    public static class BossConstants {
        public static final int BOSS_1 = 10;
        public static final int BOSS_2 = 11;
        public static final int BOSS_3 = 12;
        public static final int BOSS_4 = 13;

        // boss states (shared)
        public static final int BOSS_MOVE     = 100;
        public static final int BOSS_HIT      = 101;
        public static final int BOSS_ATTACKED = 102;
        public static final int BOSS_DEAD     = 103;
        public static final int BOSS_SPAWN = 104;
        public static final int DEATH_ANI_SPEED = 25;

        public static final int BOSS1_WIDTH_DEFAULT  = 80;
        public static final int BOSS1_HEIGHT_DEFAULT = 80;
        public static final int BOSS1_WIDTH  = (int)(BOSS1_WIDTH_DEFAULT  * Game.SCALE);
        public static final int BOSS1_HEIGHT = (int)(BOSS1_HEIGHT_DEFAULT * Game.SCALE);
        public static final int BOSS1_DRAWOFFSET_X = (int)(15 * Game.SCALE);
        public static final int BOSS1_DRAWOFFSET_Y = (int)(28 * Game.SCALE);

        public static final int BOSS2_WIDTH_DEFAULT  = 80;
        public static final int BOSS2_HEIGHT_DEFAULT = 80;
        public static final int BOSS2_WIDTH  = (int)(BOSS2_WIDTH_DEFAULT  * Game.SCALE);
        public static final int BOSS2_HEIGHT = (int)(BOSS2_HEIGHT_DEFAULT * Game.SCALE);
        public static final int BOSS2_DRAWOFFSET_X = (int)(10 * Game.SCALE);
        public static final int BOSS2_DRAWOFFSET_Y = (int)(9 * Game.SCALE);

        public static final int BOSS3_WIDTH_DEFAULT  = 80;
        public static final int BOSS3_HEIGHT_DEFAULT = 80;
        public static final int BOSS3_WIDTH  = (int)(BOSS3_WIDTH_DEFAULT  * Game.SCALE);
        public static final int BOSS3_HEIGHT = (int)(BOSS3_HEIGHT_DEFAULT * Game.SCALE);
        public static final int BOSS3_DRAWOFFSET_X = (int)(10 * Game.SCALE);
        public static final int BOSS3_DRAWOFFSET_Y = (int)(30 * Game.SCALE);

        public static final int BOSS3_FLIGHT_DURATION = 8 * 60;
        public static final int BOSS3_REST_DURATION   = 4 * 60;
        public static final int BOSS3_SHOOT_DELAY_PHASE1 = 120;
        public static final int BOSS3_SHOOT_DELAY_PHASE2 = 15;
        public static final int BOSS3_ATTACK_COOLDOWN = 100;
        public static final int BOSS3_PHASE2_COOLDOWN = 70;
        
        public static final float BOSS3_WALK_SPEED = 0.4f * Game.SCALE;
        public static final float BOSS3_FLIGHT_SPEED = 1.2f * Game.SCALE;
        public static final int BOSS3_DETECT_RANGE = 800;
        public static final int BOSS3_WATER_DAMAGE = 12;

        public static int GetMaxHealthBoss(int BossType) {
            switch (BossType) {
                case BOSS_1: return 150;  // worm — low HP
                case BOSS_2: return 400;  // beast — high HP
                case BOSS_3: return 380;  // golem — moderate
                default: return 1;
            }
        }

        public static int GetEnemyDmgBoss(int BossType) {
            switch (BossType) {
            case BOSS_1: return 15;
            case BOSS_2: return 25;
            case BOSS_3: return 20;
            default: return 0;
            }
        }
    }
}