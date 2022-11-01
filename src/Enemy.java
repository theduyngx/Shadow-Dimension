import bagel.DrawOptions;
import bagel.Image;
import bagel.util.Rectangle;

import java.util.Random;

/**
 * Class Enemy representing live obstacles for the player.
 * The class has 2 child classes - Demon and Navec.
 */

public abstract class Enemy extends LiveObject {
    // direction and speed constants
    private static final char[] DIRECTIONS = {'l', 'r', 'u', 'd'};
    private static final double MIN_SPEED = 0.2;
    private static final double MAX_SPEED = 0.7;

    /**
     * Properties
     */
    // attack (fire) image, and invincibility state
    private final Image attackImg;
    private String invincibleString = "";
    // attacking range
    private final double range;
    // speed
    private double speed;
    private final double initSpeed;

    /**
     * Enemy's constructor. Navec and Demon class will be initialized by calling this as
     * the parent's constructor. Enemy's constructor will only be passed with constant
     * properties of these 2 child classes.
     *
     * @param enemyImg      enemy's displayed image
     * @param name          enemy's name
     * @param filename      enemy's naming in files
     * @param damage        enemy's damage point
     * @param maxHealth     enemy's maximum health point
     * @param range         enemy's attacking range
     * @param isAggressive  whether enemy's aggressive (moving) or not
     */
    protected Enemy(Image enemyImg, String name, String filename,
                    double damage, double maxHealth, double range, boolean isAggressive) {
        // initialize from LiveObject constructor and the range
        super(enemyImg, name, filename, damage, maxHealth);
        this.range = range;
        Random rand = new Random();

        // state
        if (!isAggressive) isAggressive = (rand.nextInt(2) != 0);
        super.setHealth(maxHealth);
        attackImg = new Image("res/" + filename + "/" + filename + "Fire.png");

        // randomizing direction
        setDirection(DIRECTIONS[rand.nextInt(DIRECTIONS.length)]);
        // randomizing speed
        initSpeed = (isAggressive) ? MIN_SPEED + Math.random() * (MAX_SPEED - MIN_SPEED) : 0;
    }

    /**
     * Get enemy's speed. It may be changed in-game.
     *
     * @return enemy's speed
     */
    public double getSpeed() {
        return speed;
    }

    /**
     * Get Euclidean distance squared between monster and another object
     * (from the center); used to determine whether player has entered the
     * enemy's attack radius or not.
     *
     * @param xP  x center position of the other object
     * @param yP  y center position of the other object
     * @return    the Euclidean distance between the monster and object
     */
    public double getDistSq(double xP, double yP) {
        return Math.pow(xP - super.getXCenter(), 2) + Math.pow(yP - super.getYCenter(), 2);
    }

    /**
     * Resetting image implicitly; used when enemy changes direction (between
     * left and right), or when enemy goes into invincible state.
     * @see Image
     */
    @Override
    protected void resetImage() {
        super.setImage(new Image("res/" + super.getFilename() + "/" + super.getFilename()
                                 + invincibleString + super.getDirectionLR() + ".png"));
    }

    /**
     * Set speed for enemy based on timescale.
     *
     * @param timescale How much enemy's speed is sped up.
     */
    protected void setSpeed(double timescale) {
        speed = Math.pow(2, timescale)*initSpeed;
    }

    /**
     * Used after being attacked by the player; essentially sets monster's invincible
     * time frame to its maximum value to then start counting down. Also changes the
     * monster's image displayed (to the one displaying it is in invincibility state).
     */
    @Override
    protected void beginInvincible() {
        super.beginInvincible();
        invincibleString = "Invincible";
        resetImage();
    }

    /**
     * Decreasing invincible frames by 1 from monster. If it has reached 0, then
     * monster's image will display its normal attack state again.
     */
    @Override
    protected void invincibleFrameDecrement() {
        super.invincibleFrameDecrement();
        if (super.getInvincibleFrame() <= 0) {
            invincibleString = "";
            resetImage();
        }
    }

    /**
     * Check enemy's collision with an inanimate block. If enemy has collided,
     * it will move in the opposite direction.
     *
     * @param block inanimate object
     */
    @Override
    protected void processCollision(InanimateObject block) {
        // moving at opposite direction upon collision
        switch (getDirection()) {
            case 'l':
                setDirection('r');
                break;
            case 'r':
                setDirection('l');
                break;
            case 'd':
                setDirection('u');
                break;
            case 'u':
                setDirection('d');
        }
    }

    /**
     * Method processing enemy's movements. If it has collided with anything, be it
     * a block or the borders, it will head for the opposite direction.
     */
    protected void processMovement() {
        double xEnemy = getX(), yEnemy = getY();
        // enemy's movement
        switch (getDirection()) {
            case 'l':
                xEnemy -= getSpeed();
                break;
            case 'r':
                xEnemy += getSpeed();
                break;
            case 'd':
                yEnemy -= getSpeed();
                break;
            case 'u':
                yEnemy += getSpeed();
        }
        setPos(xEnemy, yEnemy);
    }

    /**
     * Method processes attacks by an enemy on the player. It detects when player
     * has entered the attack range to unleash the attack, and if any damage is
     * inflicted on player or not.
     *
     * @param player the player
     */
    @Override
    protected void processAttack(LiveObject player) {
        // position of enemy and its attack, as well as of player
        double xEnemy = getX();
        double yEnemy = getY();
        double xAttack, yAttack;
        double xPlayer = player.getX(), yPlayer = player.getY();
        // enemy's attack direction
        double rotationAngle, pi = Math.PI;
        DrawOptions rotation = new DrawOptions();
        double distSq = getDistSq(player.getXCenter(), player.getYCenter());

        // player within attack range of enemy
        if (distSq <= Math.pow(range, 2)) {
            // top left
            if (xPlayer <= xEnemy && yPlayer <= yEnemy) {
                xAttack = xEnemy - attackImg.getWidth();
                yAttack = yEnemy - attackImg.getHeight();
                rotationAngle = 0;
            }
            // bottom left
            else if (xPlayer <= xEnemy) {
                xAttack = xEnemy - attackImg.getWidth();
                yAttack = yEnemy + getImage().getHeight();
                rotationAngle = 3 * pi / 2;
            }
            // top right
            else if (yPlayer <= yEnemy) {
                xAttack = xEnemy + getImage().getWidth();
                yAttack = yEnemy - attackImg.getHeight();
                rotationAngle = pi / 2;
            }
            // bottom right
            else {
                xAttack = xEnemy + getImage().getWidth();
                yAttack = yEnemy + getImage().getHeight();
                rotationAngle = pi;
            }
            attackImg.drawFromTopLeft(xAttack, yAttack, rotation.setRotation(rotationAngle));

            // attack on player
            Rectangle attack = new Rectangle(xAttack, yAttack, attackImg.getWidth(), attackImg.getHeight());
            if (player.getInvincibleFrame() == 0 && attack.intersects(player.getRectangle())) {
                player.setHealth(player.getHealth() - getDamage());
                player.beginInvincible();
                attackLog(player);
            }
        }
        // enemy's invincibility frames counting down
        invincibleFrameDecrement();
        // draw enemy
        getImage().drawFromTopLeft(getX(), getY());
    }
}
