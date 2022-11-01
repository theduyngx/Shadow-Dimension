import bagel.Image;

/**
 * Demon class extended from Enemy: initializing various Demon's properties
 * and pass it to parent's (Enemy) constructor.
 */

public class Demon extends Enemy {
    // names and file names of demon
    public static final String DEMON_NAME = "Demon";
    public static final String DEMON_FILE_NAME = "demon";
    // max health
    public static final double MAX_DEMON_HEALTH = 40;
    // attack range and damage
    private static final double DEMON_RANGE = 150;
    public static final double DEMON_DAMAGE = 10;

    /**
     * Demon's constructor: calling Enemy constructor
     */
    public Demon() {
        super(new Image("res/navec/navecRight.png"),
              DEMON_NAME, DEMON_FILE_NAME, DEMON_DAMAGE, MAX_DEMON_HEALTH, DEMON_RANGE, false);
    }
}
