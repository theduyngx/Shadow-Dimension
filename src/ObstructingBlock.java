/**
 * Obstructing block class extended from InanimateObject class. It represents
 * 'normal' obstruction - stationary and permanent - including Wall and Tree.
 */

public class ObstructingBlock extends InanimateObject {
    // file names for obstructing blocks, list index corresponds to level index
    public static final String[] OBSTRUCTION_FILE_NAMES = {"wall", "tree"};
    public static final String WALL_NAME = "Wall";
    public static final String TREE_NAME = "Tree";
    public static final String[] OBSTRUCTION_NAMES = {WALL_NAME, TREE_NAME};

    /**
     * Obstructing block constructor.
     *
     * @param filename  block's name in file directory ("wall" or "tree")
     * @param name      block's name
     * @param xPos      block's x-position
     * @param yPos      block's y-position
     */
    public ObstructingBlock(String filename, String name, double xPos, double yPos) {
        super(filename, name, xPos, yPos);
    }

    /**
     * Method that performs state update by simply drawing the blocks.
     * Method will be called in Level's update method, which will be called
     * in ShadowDimension to perform continual frame update.
     */
    @Override
    public void update() {
        super.getBlockImg().drawFromTopLeft(super.getPosition().x, super.getPosition().y);
    }
}
