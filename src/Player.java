import bagel.Image;
import bagel.Input;
import bagel.Keys;

/**
 * Player class representing the player in the game;
 * includes properties and methods that are relevant to the object Player
 * to detect collisions, attacks inflicted on and by the player, etc.
 */

public class Player extends LiveObject {
    // health and damage
    private static final double MAX_HEALTH = 100;
    private static final double PLAYER_SPEED = 2;
    private static final double PLAYER_DAMAGE = 20;
    // player's file related
    public static final String PLAYER = "Fae";
    public static final String PLAYER_FILE_NAME = "fae";
    // attack and cool down
    private static final double MAX_ATTACK_TIME = 1000;
    private static final double MAX_COOLDOWN_TIME = 2000;
    private static final double MAX_ATTACK_FRAMES = MAX_ATTACK_TIME*TO_FRAME;
    private static final double MAX_COOLDOWN_FRAMES = MAX_COOLDOWN_TIME*TO_FRAME;

    /**
     * Properties
     */
    // attack-related
    private double attackFrames = 0;
    private double coolDown = 0;
    private String attackString = "";
    // previous position
    private double xPrev, yPrev;

    /**
     * Player constructor: constructing from player's image, getting other
     * information by calling LiveObject constructor with specified parameters.
     *
     * @param image     player's image
     * @see   Image
     */
    public Player(Image image) {
        super(image, PLAYER, PLAYER_FILE_NAME, PLAYER_DAMAGE, MAX_HEALTH);
        super.setDirection('r');
    }

    /**
     * Get player's speed; used when player initiates a movement, where
     * speed determines how much the player moves per frame.
     *
     * @return player's speed
     */
    public double getSpeed() {
        return PLAYER_SPEED;
    }

    /**
     * Get attack time frame of player. If it's 0, player is no longer in
     * attack mode, hence any contact with enemy will not inflict damage.
     *
     * @return number of frames left player is in Attack state.
     */
    public double getAttackFrames() {
        return attackFrames;
    }

    /**
     * Cool down frames: if it's 0, player has completely cooled down from
     * previous (if there's any) attack and can now initiate another attack.
     *
     * @return number of frames left before player can initiate new attack.
     */
    public double getCoolDown() {
        return coolDown;
    }

    /**
     * Resetting image implicitly. Used when player changes direction (between
     * left and right), or when player is in and out of attack mode.
     * @see Image
     */
    @Override
    protected void resetImage() {
        super.setImage(new Image("res/" + super.getFilename() + "/" + super.getFilename() + attackString +
                super.getDirectionLR() + ".png"));
    }

    /**
     * Setting player's position to the previous
     *
     * @param xPrev player's would-be previous x-position
     * @param yPrev player's would-be previous y-position
     */
    protected void setPrevPos(double xPrev, double yPrev) {
        this.xPrev = xPrev;
        this.yPrev = yPrev;
    }

    /**
     * Setting default health, which is the max health.
     * Also resetting the health bar displayed on screen (to max health).
     */
    protected void setDefaultHealth() {
        super.setHealth(MAX_HEALTH);
    }

    /**
     * Set attack frames to maximum value if used immediately after an attack, and
     * set player's image to attack mode. Otherwise, it continues the attack frame.
     */
    protected void setAttackFrames() {
        if (attackFrames == 0) attackFrames = MAX_ATTACK_FRAMES;
        attackString = "Attack";
        resetImage();
    }

    /**
     * Decreasing player's attacking frame by 1. If it is 0, cool down continues
     * (if required), and updates player's image to idle mode (instead of attack).
     */
    protected void attackFramesDecrement() {
        if (attackFrames <= 0) {
            attackFrames = 0;
            attackString = "";
            coolDownDecrement();
            resetImage();
        }
        else attackFrames--;
    }

    /**
     * Automatically sets player's cool down to its maximum value. Used right
     * after a player's attack.
     */
    protected void beginCoolDown() {
        coolDown = MAX_COOLDOWN_FRAMES;
    }

    /**
     * Reducing cool down frames by 1 for the player. If cool down frame is
     * already at 0, it won't do anything.
     */
    protected void coolDownDecrement() {
        coolDown = (coolDown <= 0) ? 0 : coolDown-1;
    }

    /**
     * Method for printing logs of damage inflicted on live object by sinkhole;
     * Used specifically for Player, since Enemy does not get damaged by sinkholes.
     *
     * @param sinkhole  sinkhole object
     * @see   Sinkhole
     */
    public void sinkAttackedLog(Sinkhole sinkhole) {
        System.out.println(sinkhole.getName() + " inflicts " + Math.round(sinkhole.getDamagePoints()) +
                           " damage points on " + getName() + ". " + getName() + "'s current health: " +
                           Math.round(getHealth()) + "/" + Math.round(getMaxHealth()));
    }

    /**
     * Checking for collision of player with an inanimate object. If it is only obstructing
     * block (wall/tree), it will trigger 'move-back' to previous position. If it's a sinkhole,
     * it will be recorded as inactive and disappear, and damage will be inflicted on player.
     * <p>
     * Since it needs to check for collisions, it will be called in inanimateBlockProcessing
     * method where each block is thereby visited by the program.
     *
     * @param block       obstructing block (wall/tree)
     * @see   InanimateObject
     */
    @Override
    protected void processCollision(InanimateObject block) {
        // if there's no collision
        if (!getRectangle().intersects(block.getRectangle())) return;
        // relevant variables
        double xPlayer = getX(), yPlayer = getY();
        setPos(xPrev, yPlayer);
        boolean xIntersect = block.getRectangle().intersects(getRectangle());
        // if it's a sinkhole collision, hole disappears and damage inflicted
        if (block instanceof Sinkhole) {
            ((Sinkhole) block).setInactive();
            setHealth(getHealth()-((Sinkhole) block).getDamagePoints());
            sinkAttackedLog((Sinkhole) block);
        }
        xPlayer = xIntersect ? xPlayer : xPrev;
        yPlayer = !xIntersect ? yPlayer : yPrev;
        // move back accordingly (to the collision)
        setPos(xPlayer, yPlayer);
    }

    /**
     * Method processes attacks by the player on an enemy. It detects if player has
     * successfully landed an attack on the enemy or not.
     *
     * @param enemy the opposition
     */
    @Override
    protected void processAttack(LiveObject enemy) {
        // attack on enemy (iff it's during player's attack and enemy is not in invincible state)
        if (!(getRectangle().intersects(enemy.getRectangle()) && enemy.getInvincibleFrame() == 0 &&
              getAttackFrames() > 0)) return;
        enemy.setHealth(enemy.getHealth() - getDamage());
        enemy.beginInvincible();
        attackLog(enemy);
    }

    /**
     * Method processing player's input.
     * <p>
     * Called in Level's update method to continually update player's input,
     * including usual movement and attacks.
     *
     * @param input  keyboard input
     * @see   Input
     */
    protected void update(Input input) {
        // movement input
        double xPlayer = getX(), yPlayer = getY();
        if (input.isDown(Keys.LEFT)) {
            setDirection('l');
            xPlayer -= getSpeed();
        }
        if (input.isDown(Keys.RIGHT)) {
            setDirection('r');
            xPlayer += getSpeed();
        }
        if (input.isDown(Keys.UP)) yPlayer -= getSpeed();
        if (input.isDown(Keys.DOWN)) yPlayer += getSpeed();

        // attacking frames and input
        attackFramesDecrement();
        if ((input.wasPressed(Keys.A) && getCoolDown() == 0) || getAttackFrames() > 0) {
            setAttackFrames();
            beginCoolDown();
        }
        setPos(xPlayer, yPlayer);
    }
}
