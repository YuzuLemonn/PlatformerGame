package gamestates;

public class CharacterStats {
    public final int hp;
    public final int stamina;
    public final int skill1Dmg;
    public final int skill2Dmg;
    public final int skill3Dmg;
    public final String skill1Name;
    public final String skill2Name;
    public final String skill3Name;
    public static final int MAX_BRAWLER_HP  = 100;
    public static final int MAX_MAGE_HP     = 70;
    public static final int MAX_ASSASSIN_HP = 80;

    public static final int GLOBAL_MAX_HP = MAX_BRAWLER_HP;

    public CharacterStats(int hp, int stamina,
                          String skill1Name, int skill1Dmg,
                          String skill2Name, int skill2Dmg,
                          String skill3Name, int skill3Dmg) {
        this.hp = hp;
        this.stamina = stamina;
        this.skill1Name = skill1Name;
        this.skill1Dmg = skill1Dmg;
        this.skill2Name = skill2Name;
        this.skill2Dmg = skill2Dmg;
        this.skill3Name = skill3Name;
        this.skill3Dmg = skill3Dmg;
    }
}