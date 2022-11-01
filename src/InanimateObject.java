import bagel.Image;
import bagel.util.Point;
import bagel.util.Rectangle;

/**
 * Inanimate object abstract class representing objects that are stationary.
 * The class is inherited by ObstructingBlock (Wall & Tree) and Sinkhole class.
 */

public abstract class InanimateObject {
    // block's image, position, bounding box and name
    private final Image blockImg;
    private final Point position;
    private final Rectangle blockRectangle;
    private final String name;

    /**
     * Inanimate object constructor.
     *
     * @param filename object's name in file directory
     * @param name     object's name (how it is stored in CSV file)
     * @param xPos     object's x-position
     * @param yPos     object's y-position
     */
    public InanimateObject(String filename, String name, double xPos, double yPos) {
        this.blockImg = new Image("res/" + filename + ".png");
        this.position = new Point(xPos, yPos);
        this.blockRectangle = new Rectangle(position, blockImg.getWidth(), blockImg.getHeight());
        this.name = name;
    }

    /**
     * Get bounding block of block to check for collisions.
     *
     * @return block's bounding block
     * @see    Rectangle
     */
    public Rectangle getRectangle(){
        return blockRectangle;
    }

    /**
     * Get block's name, which not only can be used in logs but can also be
     * used to access CSV data on specified block.
     *
     * @return block's name
     */
    public String getName(){
        return name;
    }

    /**
     * Get image of inanimate objects to draw in state updates.
     *
     * @return object's display image
     * @see    Image
     */
    public Image getBlockImg() {
        return blockImg;
    }

    /**
     * Get stationary position of inanimate object.
     *
     * @return position as a Point with x,y coordinates
     * @see    Point
     */
    public Point getPosition() {
        return position;
    }

    /**
     * Abstract method that performs state update of inanimate object. In other
     * words, it should draw, if not yet disappeared, the object every frame.
     */
    public abstract void update();
}
