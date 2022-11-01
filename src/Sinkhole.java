/**
 * Sinkhole class extended from InanimateObject class.
 * Unlike normal obstructions, sinkholes damage player when collided,
 * and disappear (becoming inactive) afterwards.
 */

public class Sinkhole extends InanimateObject {
    public static final String SINK_NAME = "Sinkhole";
    private final static String filename = "sinkhole";
    private final static int DAMAGE_POINTS = 30;
    private boolean isActive = true;

    /**
     * Sinkhole constructor.
     *
     * @param xPos x-position for sinkhole
     * @param yPos y-position for sinkhole
     */
    public Sinkhole(double xPos, double yPos) {
        super(filename, SINK_NAME, xPos, yPos);
    }

    /**
     * Get sinkhole's damage point on player.
     * @return sinkhole's damage point
     */
    public int getDamagePoints(){
        return DAMAGE_POINTS;
    }

    /**
     * Whether sinkhole is still active or not. It will be active when
     * initialized, but will become inactive when player collides.
     *
     * @return boolean value whether sinkhole is still active or not.
     */
    public boolean isActive() {
        return isActive;
    }

    /**
     * Method setting sinkhole to become inactive. Used when player collides
     * with sinkhole.
     */
    public void setInactive() {
        isActive = false;
    }

    /**
     * Method that performs state update. Similar to ObstructingBlock's update,
     * it will be called in the same methods.
     */
    @Override
    public void update() {
        if (isActive)
            super.getBlockImg().drawFromTopLeft(super.getPosition().x, super.getPosition().y);
    }
}