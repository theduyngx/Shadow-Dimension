import bagel.Image;

/**
 * Navec class extended from Enemy: initializing various Navec's properties
 * and pass it to parent's (Enemy) constructor.
 */

public class Navec extends Enemy {
    // names and file names of demon
    public static final String NAVEC_NAME = "Navec";
    public static final String NAVEC_FILE_NAME = "navec";
    // max health
    public static final double MAX_NAVEC_HEALTH = Demon.MAX_DEMON_HEALTH*2;
    // attack range and damage
    private static final double NAVEC_RANGE = 200;
    public static final double NAVEC_DAMAGE = Demon.DEMON_DAMAGE*2;

    /**
     * Navec's constructor: calling Enemy constructor
     */
    public Navec() {
        super(new Image("res/navec/navecRight.png"),
              NAVEC_NAME, NAVEC_FILE_NAME, NAVEC_DAMAGE, MAX_NAVEC_HEALTH, NAVEC_RANGE, true);
    }
}
