import bagel.DrawOptions;
import bagel.Font;
import bagel.Image;
import bagel.util.Point;
import bagel.util.Rectangle;
import bagel.util.Colour;

import java.util.Random;

/**
 * An abstract for live objects including player and monsters.
 */
public abstract class LiveObject {
    // conversion from time to frame constant, used for time-related properties
    public static final double TO_FRAME = 60.0/1000;
    // invincible time, converted to frames
    private static final double INVINCIBLE_TIME = 3000;
    private static final double INVINCIBLE_FRAMES = INVINCIBLE_TIME*TO_FRAME;
    // health and health colors
    private static final int HEALTH_SAFE = 65;
    private static final int HEALTH_CAUTIOUS = 35;
    private static final Colour HEALTH_SAFE_COLOR = new Colour(0, 0.8, 0.2);
    private static final Colour HEALTH_CAUTIOUS_COLOR = new Colour(0.9, 0.6, 0);
    private static final Colour HEALTH_DANGER_COLOR = new Colour(1, 0, 0);

    /**
     * Properties
     */
    // object's name, naming in files
    private final String name;
    private final String filename;
    // object's image and Rectangle block
    private Image objectImg;
    private final Rectangle block;
    // object's health and damage
    private double health;
    private final double maxHealth;
    private int healthDisplay = 100;
    private final double damage;
    private double invincibleFrame = 0;
    // alive status
    private boolean isKilled = false;
    // object's position
    private double x, y;
    private double xCenter, yCenter;
    // object's direction
    private char direction;
    private String dirLR = null;

    /**
     * Live object's constructor.
     *
     * @param objectImg  object's image, which is used to initialize object's
     *                   Rectangle block
     * @param name       object's name
     * @param filename   object's name stored in the files
     * @param damage     object's damage point
     * @param maxHealth  object's maximum health
     */
    public LiveObject(Image objectImg, String name, String filename, double damage, double maxHealth) {
        this.name = name;
        this.filename = filename;
        this.objectImg = objectImg;
        this.block = new Rectangle(0, 0, objectImg.getWidth(), objectImg.getHeight());
        this.damage = damage;
        this.maxHealth = maxHealth;
        this.health = maxHealth;
    }

    /**
     * Get name, as one of the identifications of the type of objects
     * (Player, Demon, Navec).
     *
     * @return name of object
     */
    public String getName() {
        return name;
    }

    /**
     * Get object's naming in the files; used when concatenating strings
     * for files accessing.
     *
     * @return file name of object.
     */
    public String getFilename() {
        return filename;
    }

    /**
     * Get object's image; object's image may change as a result
     * of object's updated direction or state (attack/invincible/etc.).
     *
     * @return object's image
     * @see    Image
     */
    public Image getImage() {
        return objectImg;
    }

    /**
     * Get object's block - a Rectangle covering the object's image. Rectangle
     * holds implicit location, which is important when detecting collisions
     * (checking whether 2 Rectangle objects intersect or not).
     *
     * @return object, as a Rectangle block
     * @see    Rectangle
     */
    public Rectangle getRectangle() {
        return block;
    }

    /**
     * Get live object's current health. Object's health is initialized
     * to the maximum health, and the lowest (killed threshold) is 0.
     *
     * @return player's health
     */
    public double getHealth() {
        return health;
    }

    /**
     * Get live object's maximum health.
     *
     * @return object's maximum health.
     */
    public double getMaxHealth() {
        return maxHealth;
    }

    /**
     * Get object's health display.
     * Differs from object's real health in that this is the health that would be
     * displayed on-screen. It is meant to be formatted as integer.
     *
     * @return health display
     */
    public int getHealthDisplay() {
        return healthDisplay;
    }

    /**
     * Getter returning whether object has been slain or not. If it's a player that
     * has been slain, then it's game over. Otherwise, it is simply an enemy killed.
     *
     * @return boolean value indicating monster's alive status.
     */
    public boolean isKilled() {
        return isKilled;
    }

    /**
     * Get damage point inflicted on opposition if object successfully lands
     * attack.
     *
     * @return object's damage
     */
    public double getDamage() {
        return damage;
    }

    /**
     * Get object's current x position.
     *
     * @return x position
     */
    public double getX() {
        return x;
    }
    /**
     * Get object's current y position.
     *
     * @return y position
     */
    public double getY() {
        return y;
    }

    /**
     * Get the x center position of object (meaning center of the Rectangle).
     * Along with y center position, this is to calculate the Euclidean distance
     * between 2 live objects to determine whether one is within attack range of
     * the other or not.
     *
     * @return x center position of object.
     */
    public double getXCenter() {
        return xCenter;
    }
    /**
     * Get the y center position.
     *
     * @return y center position of object.
     */
    public double getYCenter() {
        return yCenter;
    }

    /**
     * Object's direction, returned as a character; used mostly for enemies (live
     * objects that move on its own) to retrieve their current direction to thus
     * determine the direction of their next move.
     *
     * @return character indicating direction - 'l', 'r', 'd', 'u'.
     */
    public char getDirection() {
        return direction;
    }

    /**
     * Object's direction between Left/Right, returned as a string; used when
     * concatenating strings to access images in files with specified directions.
     *
     * @return String "Left" or "Right"
     */
    public String getDirectionLR() {
        return dirLR;
    }

    /**
     * Get object's speed. If it's player's then it should be a constant.
     * But for enemies, speed may be changed in-game.
     *
     * @return object's speed
     */
    public abstract double getSpeed();

    /**
     * Getting object's invincibility time frames. If it's 0, the live object is
     * vulnerable to opposition's attacks. If not, object is in invincible state.
     *
     * @return how many frames left object is in its invincible state
     */
    public double getInvincibleFrame() {
        return invincibleFrame;
    }

    /**
     * Setting live object's image.
     * @param objectImg object's updated image.
     */
    protected void setImage(Image objectImg) {
        this.objectImg = objectImg;
    }

    /**
     * Resetting image implicitly; used when live object changes direction (between
     * left and right), or, depending on the object, either is in and out of attack
     * or invincible state.
     * @see Image
     */
    protected abstract void resetImage();

    /**
     * Setting object's position: updates both x and y position; also moves
     * object's block (Rectangle), to update Rectangle's implicit location.
     *
     * @param x  new x position of object
     * @param y  new y position of object
     */
    protected void setPos(double x, double y) {
        this.x = x;
        this.y = y;
        block.moveTo(new Point(x, y));
        xCenter = x + objectImg.getWidth()/2;
        yCenter = y + objectImg.getHeight()/2;
    }

    /**
     * Setting object's health; simultaneously updates both object's
     * actual health and health display on-screen.
     *
     * @param health object's updated health
     */
    protected void setHealth(double health) {
        this.health = Math.max(health, 0);
        healthDisplay = (int) Math.round(health/maxHealth * 100);
        if (health <= 0) isKilled = true;
    }

    /**
     * Setting direction for object. Used when object changes their direction
     * between left and right.
     *
     * @param direction character 'l', 'r', 'u', 'd' denoting the directions.
     */
    protected void setDirection(char direction) {
        this.direction = direction;
        if (direction == 'l') dirLR = "Left";
        else if (direction == 'r') dirLR = "Right";
        if (dirLR == null) dirLR = (new Random().nextInt(2) != 0) ? "Left" : "Right";
        resetImage();
    }

    /**
     * Makes object invincible (which sets invincibility frames to its maximum
     * value); used immediately after getting attacked by an opposition.
     */
    protected void beginInvincible() {
        invincibleFrame = INVINCIBLE_FRAMES;
    }

    /**
     * Counts down object's invincibility frames. If it already reaches 0, then
     * it will not do anything. If not, it will decrement by 1 frame.
     */
    protected void invincibleFrameDecrement() {
        invincibleFrame = (invincibleFrame <= 0) ? 0 : invincibleFrame-1;
    }

    /**
     * Method for printing logs of damage inflicted on another object by this live object.
     *
     * @param attacked   live object that is damaged by this live object
     * @see   LiveObject
     */
    public void attackLog(LiveObject attacked) {
        System.out.println(getName() + " inflicts " + Math.round(getDamage()) + " damage points on " +
                attacked.getName() + ". " + attacked.getName() + "'s current health: " +
                Math.round(attacked.getHealth()) + "/" + Math.round(attacked.getMaxHealth()));
    }

    /**
     * Helper method displaying health bar of any live object (player or enemy).
     * Change color based on the current health of the object.
     *
     * @param font  font used to display the health
     * @param xPos  x position of where health bar would be displayed on screen
     * @param yPos  y position of health bar.
     * @see   Font
     */
    public void healthColor(Font font, double xPos, double yPos) {
        int health = getHealthDisplay();
        Colour color = (health >= HEALTH_SAFE) ? HEALTH_SAFE_COLOR :
                       (health >= HEALTH_CAUTIOUS) ? HEALTH_CAUTIOUS_COLOR :
                       HEALTH_DANGER_COLOR;
        DrawOptions coloring = new DrawOptions();
        coloring.setBlendColour(color);
        font.drawString(health + "%", xPos, yPos, coloring);
    }

    /**
     * Abstract method checking for live object's collision with inanimate objects.
     *
     * @param block inanimate object
     */
    protected abstract void processCollision(InanimateObject block);

    /**
     * Abstract method processing attacks of live object, specifically how it has,
     * if so, managed to damage the opposition.
     *
     * @param object the opposition
     */
    protected abstract void processAttack(LiveObject object);
}
