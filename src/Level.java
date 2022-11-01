import bagel.*;

/**
 * Level class - representing a single level in the game, hence creating a new
 * level means initializing a new Level object with instructed specifications.
 */

public class Level {
    // timescale
    private static final int MAX_TIMESCALE = 3;
    private static final int MIN_TIMESCALE = -3;
    // player's speed and number of levels
    public static final int NUM_LEVELS = 2;
    // winning position (for level 0)
    private static final double X_WIN = 950;
    private static final double Y_WIN = 670;
    // player's and enemy's health display position
    public static final int X_HEALTH = 20;
    public static final int Y_HEALTH = 25;
    public static final int Y_DIFF_ENEMY_HEALTH = 6;
    // backgrounds
    private final Image BACKGROUND_LAB = new Image("res/background0.png");
    private final Image BACKGROUND_UNDER = new Image("res/background1.png");
    private final Image[] BACKGROUNDS = {BACKGROUND_LAB, BACKGROUND_UNDER};
    // font size
    private static final int PLAYER_HEALTH_SIZE = 30;
    private static final int ENEMY_HEALTH_SIZE = 15;

    /**
     * Properties
     */
    // level properties (current level number and whether it is completed)
    private final int levelNum;
    private boolean isCompleted = false;
    private int timescale = 0;
    // player
    private final Player player;
    // corner coordinates
    private final int xLeft, yTop, xRight, yBottom;
    // enemies
    private final Enemy[] enemyList;
    // blocks
    private final Sinkhole[] sinkList;
    private final ObstructingBlock[] obstructList;
    // font and coloring
    private final Font playerHealthBar, enemyHealthBar;

    /**
     * Level constructor.
     *
     * @param levelNum      current game level
     * @param player        player argument
     * @param xLeft         leftmost x-position
     * @param yTop          topmost y-position
     * @param xRight        rightmost x-position
     * @param yBottom       bottommost y-position
     * @param enemyList     list of enemies
     * @param sinkList      list of sinkholes
     * @param obstructList  list of obstructing blocks
     * @see   Player
     * @see   Enemy
     * @see   Sinkhole
     * @see   ObstructingBlock
     */
    public Level(int levelNum, Player player, int xLeft, int yTop, int xRight, int yBottom,
                 Enemy[] enemyList, Sinkhole[] sinkList, ObstructingBlock[] obstructList) {
        this.levelNum = levelNum;
        this.player = player;
        this.xLeft = xLeft;
        this.yTop = yTop;
        this.xRight = xRight;
        this.yBottom = yBottom;
        this.sinkList = sinkList;
        this.enemyList = enemyList;
        this.obstructList = obstructList;
        playerHealthBar = new Font("res/frostbite.ttf", PLAYER_HEALTH_SIZE);
        enemyHealthBar = new Font("res/frostbite.ttf", ENEMY_HEALTH_SIZE);
    }

    /**
     * Get level status - whether it has been completed or not.
     *
     * @return boolean value whether level has been completed.
     */
    public boolean isCompleted() {
        return isCompleted;
    }

    /**
     * Method checking whether live object has exceeded the border. If it has,
     * object will be moved back to the latest position that does not exceed
     * the border.
     *
     * @param  object  Player / Enemy
     * @return         boolean value denoting whether object has exceeded the
     *                 border or not
     * @see    LiveObject
     */
    public boolean exceedBorder(LiveObject object) {
        double x = object.getX(), y = object.getY();
        // if move exceeds border
        boolean exceeded = (x < xLeft) || (x > xRight) || (y < yTop) || (y > yBottom);
        if (x < xLeft) x = xLeft;
        else if (x > xRight) x = xRight;
        if (y < yTop) y = yTop;
        else if (y > yBottom) y = yBottom;
        // move back
        object.setPos(x, y);
        return exceeded;
    }

    /**
     * Checking for enemy's collision. Scan through all enemies to see if any has collided
     * with the specified obstructing block. If yes then enemy will move in opposite direction.
     * <p>
     * Similar to playerCollision, this method will also be called in inanimateBlockProcessing.
     *
     * @param block    obstructing block
     * @see   InanimateObject
     */
    protected void enemiesCollision(InanimateObject block) {
        for (Enemy enemy : enemyList) {
            // if enemy's killed
            if (enemy.isKilled()) continue;
            // if enemy collides or exceeds border
            if (enemy.getRectangle().intersects(block.getRectangle()) ||
                exceedBorder(enemy)) {
                enemy.processCollision(block);
            }
        }
    }

    /**
     * Drawing and updating inanimate blocks, and checking if any live object has collided
     * with any of the inanimate blocks. Specifically, Player if collides will be moved
     * back to their previous position.
     */
    public void processInanimateBlocks() {
        // obstruction blocks
        for (ObstructingBlock obstructBlock : obstructList) {
            player.processCollision(obstructBlock);
            enemiesCollision(obstructBlock);
            obstructBlock.update();
        }
        // sinkholes
        for (Sinkhole sink : sinkList) {
            if (!sink.isActive()) continue;
            player.processCollision(sink);
            enemiesCollision(sink);
            sink.update();
        }
    }

    /**
     * Enemy processing: processes anything directly related to enemy in game.
     * Enemy's movements, attacks and status will be processed in this method.
     * <p>
     * Method called in update method as a direct enemy processing method.
     */
    protected void processEnemies() {
        for (Enemy enemy : enemyList) {
            if (enemy.isKilled()) {
                // if Navec is killed then we've won
                if (enemy instanceof Navec)
                    isCompleted = true;
                continue;
            }
            // enemy's speed set to timescale + movement
            enemy.setSpeed(timescale);
            enemy.processMovement();
            // checking for enemy's attack on player and vice versa
            enemy.processAttack(player);
            player.processAttack(enemy);
            // display enemy's health bar
            enemy.healthColor(enemyHealthBar, enemy.getX(), enemy.getY() - Y_DIFF_ENEMY_HEALTH);
        }
        // player's invincibility frames counting down
        player.invincibleFrameDecrement();
    }

    /**
     * Method setting timescale based on input, and recording timescale
     * change to logs.
     *
     * @param input keyboard input
     * @see   Input
     */
    protected void setTimescale(Input input) {
        if (input.wasPressed(Keys.L) && timescale < MAX_TIMESCALE) {
            timescale++;
            System.out.println("Sped up, Speed: " + timescale);
        }
        else if (input.wasPressed(Keys.K) && timescale > MIN_TIMESCALE) {
            timescale--;
            System.out.println("Slowed down, Speed: " + timescale);
        }
    }

    /**
     * Performs a state update. As mentioned, the method will be executed 60 times
     * in 1 second, due to being called in the ShadowDimension's update method.
     *
     * @param input keyboard input
     * @see   Input
     */
    protected void update(Input input) {
        // in-game: displaying game background and processing movement
        BACKGROUNDS[levelNum].draw(Window.getWidth() / 2.0, Window.getHeight() / 2.0);
        player.setPrevPos(player.getX(), player.getY());

        // timescale input
        setTimescale(input);
        // player's input processing, then check for collision
        player.update(input);
        exceedBorder(player);
        processInanimateBlocks();
        // draw player at finalized position and their health bar
        player.getImage().drawFromTopLeft(player.getX(), player.getY());
        player.healthColor(playerHealthBar, X_HEALTH, Y_HEALTH);

        // if not level 0, update enemy's position - check winning condition
        if (levelNum != 0) processEnemies();
        // if player's at level 0 - check level 0's winning condition
        else if (player.getX() >= X_WIN && player.getY() >= Y_WIN) isCompleted = true;
    }
}
