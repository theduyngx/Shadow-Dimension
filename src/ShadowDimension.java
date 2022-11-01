import bagel.*;
import bagel.Image;
import bagel.Window;

import java.io.*;
import java.util.ArrayList;

/**
 * Based on skeleton code for SWEN20003 Project, Semester 2, 2022, The University of Melbourne.
 * The program runs the game "Shadow Dimension", where it processes certain keyboard inputs,
 * and updates the game's state by frames, at the rate of 60 frames/second.
 *
 * @author The Duy Nguyen - 1100548 (theduyn@student.unimelb.edu.au)
 */

public class ShadowDimension extends AbstractGame {
    // block names in csv files
    private static final String TOP_LEFT = "TopLeft";
    private static final String BOTTOM_RIGHT = "BottomRight";
    // for window, title and background displays
    private final static int WINDOW_WIDTH = 1024;
    private final static int WINDOW_HEIGHT = 768;
    private final static String GAME_TITLE = "SHADOW DIMENSION";

    // title message position
    private static final int X_START_TITLE = 260;
    private static final int Y_START_TITLE = 250;
    // title-screen instructions message position
    private static final int X_START_TOP_MSG = X_START_TITLE+90;
    private static final int X_START_BOTTOM_MSG = X_START_TOP_MSG-50;
    private static final int Y_START_TOP_MSG = Y_START_TITLE+190;
    private static final int Y_START_BOTTOM_MSG = Y_START_TOP_MSG+50;
    // status (win/lose) message position
    private static final int Y_STATUS_MSG = Y_START_TITLE+170;
    private static final int X_GAMEOVER_MSG = X_START_TITLE+95;
    // level complete message position
    private static final int X_LVL_COMPLETE_MSG = X_START_TITLE+20;
    private static final int Y_LVL_COMPLETE_MSG = Y_STATUS_MSG;
    // level-complete instructions message position
    private static final int X_LVL_TOP_MSG = 350;
    private static final int Y_LVL_TOP_MSG = 350;
    private static final int X_LVL_MID_MSG = X_LVL_TOP_MSG+21;
    private static final int Y_LVL_MID_MSG = Y_LVL_TOP_MSG+60;
    private static final int X_LVL_BOTTOM_MSG = X_LVL_TOP_MSG+13;
    private static final int Y_LVL_BOTTOM_MSG = Y_LVL_MID_MSG+60;
    // font size
    private static final int TITLE_FONT_SIZE = 75;
    private static final int INSTRUCTION_FONT_SIZE = 40;

    // for time-frame conversion
    public static final double TO_FRAME = 60.0/1000;
    // time-related
    public static final double LEVEL_COMPLETE_TIME = 3*1000;
    public static final double LEVEL_COMPLETE_WAIT = LEVEL_COMPLETE_TIME*TO_FRAME;

    /**
     * Properties
     */
    // player
    private final Player player;
    // current level number initialized to 0, and level
    private int levelNum = 0;
    private Level level;
    // font and coloring
    private final Font titleDisplay, instructions;
    // initialRun will be set to false once the level is started
    private boolean initialRun = true;
    // wait frame after level completion (except level with final boss)
    private double levelCompleteWait = LEVEL_COMPLETE_WAIT;

    /**
     * Program constructor: initializing images, players, message fonts and some
     * important positions in the game.
     *
     * @see Image
     * @see Player
     * @see Font
     */
    public ShadowDimension() {
        // window initialization
        super(WINDOW_WIDTH, WINDOW_HEIGHT, GAME_TITLE);
        // player initialization
        Image playerImg = new Image("res/" +
                Player.PLAYER_FILE_NAME + "/" + Player.PLAYER_FILE_NAME + "Right.png");
        player = new Player(playerImg);
        // CSV data scanning + level 0 initialization
        readCSV();
        // for message displays
        titleDisplay = new Font("res/frostbite.ttf", TITLE_FONT_SIZE);
        instructions = new Font("res/frostbite.ttf", INSTRUCTION_FONT_SIZE);
    }

    /**
     * Scanning through CSV file for initial positions and other information on the level,
     * such as corner x,y-positions.
     * <p>
     * NOTE: Method is to be used only as an initial scan for the level. It plays the role of
     * 'refreshing' data for a new level; hence will only be called once for each level in:
     * (1) constructor to initialize level 0, and
     * (2) update method once previous level is completed and transitioning to the next.
     */
    public void readCSV() {
        // starting/refreshing level
        initialRun = true;
        player.setDefaultHealth();
        int xLeft = 0, yTop = 0, xRight = 100, yBottom = 100;
        // enemy list
        ArrayList<Enemy> enemyList = new ArrayList<>();
        // block lists
        ArrayList<Sinkhole> sinkList = new ArrayList<>();
        ArrayList<ObstructingBlock> obstructList = new ArrayList<>();

        try {
            // (re-)reading the csv file
            String line; String[] tempArr;
            String csvFileName = "res/level" + levelNum + ".csv";
            BufferedReader csvReader = new BufferedReader(new FileReader(csvFileName));
            int xPos, yPos;
            // reading each line of csv file
            while ((line = csvReader.readLine()) != null) {
                tempArr = line.split(",");
                xPos = Integer.parseInt(tempArr[1]);
                yPos = Integer.parseInt(tempArr[2]);
                switch (tempArr[0]) {
                    // data on top left position
                    case TOP_LEFT:
                        xLeft = xPos;
                        yTop = yPos;
                        break;
                    // data on bottom right position
                    case BOTTOM_RIGHT:
                        xRight = xPos;
                        yBottom = yPos;
                        break;
                    // data on player's position
                    case Player.PLAYER:
                        player.setPos(xPos, yPos);
                        break;
                    // data on obstructing blocks
                    case ObstructingBlock.WALL_NAME:
                    case ObstructingBlock.TREE_NAME:
                        obstructList.add(new ObstructingBlock(ObstructingBlock.OBSTRUCTION_FILE_NAMES[levelNum],
                                         ObstructingBlock.OBSTRUCTION_NAMES[levelNum], xPos, yPos));
                        break;
                    // data on blocks -> counting
                    case Sinkhole.SINK_NAME:
                        sinkList.add(new Sinkhole(xPos, yPos));
                        break;
                    // data on enemy's position: initialize and add to enemy list
                    case Demon.DEMON_NAME:
                    case Navec.NAVEC_NAME:
                        boolean isNavec = (tempArr[0].equals(Navec.NAVEC_NAME));
                        Enemy enemy = (isNavec) ? new Navec() : new Demon();
                        enemy.setPos(xPos, yPos);
                        enemyList.add(enemy);
                }
            }
            csvReader.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        // converting object and enemy lists to arrays
        Sinkhole[] sinkArr = new Sinkhole[sinkList.size()];
        sinkArr = sinkList.toArray(sinkArr);
        ObstructingBlock[] obstructArr = new ObstructingBlock[obstructList.size()];
        obstructArr = obstructList.toArray(obstructArr);
        Enemy[] enemyArr = new Enemy[enemyList.size()];
        enemyArr = enemyList.toArray(enemyArr);

        // initialize current level
        level = new Level(levelNum, player, xLeft, yTop, xRight, yBottom, enemyArr, sinkArr, obstructArr);
    }

    /**
     * Draw title screen; used in update method upon starting the game and will only stop
     * being executed once player starts the level.
     */
    public void drawTitleScreen() {
        titleDisplay.drawString("SHADOW DIMENSION", X_START_TITLE, Y_START_TITLE);
        instructions.drawString("PRESS SPACE TO START", X_START_TOP_MSG, Y_START_TOP_MSG);
        instructions.drawString("USE ARROW KEYS TO FIND GATE", X_START_BOTTOM_MSG, Y_START_BOTTOM_MSG);
    }

    /**
     * Draw level 1 instructions; used in update method before player starts level 1.
     */
    public void drawInstruction() {
        instructions.drawString("PRESS SPACE TO START", X_LVL_TOP_MSG, Y_LVL_TOP_MSG);
        instructions.drawString("PRESS A TO ATTACK", X_LVL_MID_MSG, Y_LVL_MID_MSG);
        instructions.drawString("DEFEAT NAVEC TO WIN", X_LVL_BOTTOM_MSG, Y_LVL_BOTTOM_MSG);
    }

    /**
     * Draw game over screen; will be continuously executed in update when Player is killed.
     */
    public void drawGameOver() {
        titleDisplay.drawString("GAME OVER!", X_GAMEOVER_MSG, Y_STATUS_MSG);
    }

    /**
     * Detect whether it is a usual level completion or game completion;
     * Used in update once player wins a certain level or the entire game.
     */
    public void processWinning() {
        // winning
        if (levelNum >= Level.NUM_LEVELS-1) {
            titleDisplay.drawString("CONGRATULATIONS!", X_START_TITLE, Y_STATUS_MSG);
            return;
        }
        // normal level completion
        if (levelCompleteWait > 0) {
            titleDisplay.drawString("LEVEL COMPLETE!", X_LVL_COMPLETE_MSG, Y_LVL_COMPLETE_MSG);
            levelCompleteWait--;
            return;
        }
        levelCompleteWait = LEVEL_COMPLETE_WAIT;
        // resetting initialRun, timescale, and move to next level
        levelNum++;
        readCSV();
    }

    /**
     * Performs a state update. As mentioned, the method will be executed
     * 60 times in 1 second, each time performing an update of the frame.
     *
     * @param input keyboard input
     * @see   Input
     */
    @Override
    protected void update(Input input) {
        // exit
        if (input.wasPressed(Keys.ESCAPE)) Window.close();
        // game over
        if (player.isKilled()) {
            drawGameOver();
            return;
        }
        // instruction screen
        if (initialRun && !input.isDown(Keys.SPACE)) {
            // title screen (before level 0)
            if (levelNum == 0) {
                drawTitleScreen();
                return;
            }
            // other level screens
            drawInstruction();
            return;
        }
        // current level completed
        if (level.isCompleted()) {
            processWinning();
            return;
        }
        // running the level
        initialRun = false;
        level.update(input);
    }

    /**
     * The entry point for the program.
     */
    public static void main(String[] args) {
        ShadowDimension game = new ShadowDimension();
        game.run();
    }
}
