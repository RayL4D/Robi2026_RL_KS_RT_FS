package core.statemachine;

import java.awt.Point;

import graphicLayer.GBounded;
import graphicLayer.GElement;
import graphicLayer.GImage;
import graphicLayer.GSpace;

/**
 * A Robibot is a graphic element driven by a finite state machine.
 *
 * <p>It encapsulates:
 * <ul>
 *   <li>A reference to the graphic element ({@link GElement})</li>
 *   <li>The fully-qualified element name in the environment (e.g. "space.r1")</li>
 *   <li>A {@link StateMachine} describing its behaviour</li>
 *   <li>The current movement direction (dx, dy)</li>
 *   <li>A reference to the {@link GSpace} for border detection</li>
 * </ul>
 *
 * <p>Supports both {@link GBounded} (Rect, Oval, etc.) and {@link GImage} elements.
 */
public class Robibot {

    private final String elementName;
    private final GElement element;
    private final GSpace space;
    private final StateMachine stateMachine;
    private int dx;
    private int dy;
    private boolean running;

    /**
     * Creates a new Robibot bound to the given graphic element and space.
     *
     * @param elementName the fully-qualified element name (e.g. "space.r1")
     * @param element     the graphic element to control (GBounded or GImage)
     * @param space       the parent GSpace used for border detection
     */
    public Robibot(String elementName, GElement element, GSpace space) {
        this.elementName = elementName;
        this.element = element;
        this.space = space;
        this.stateMachine = new StateMachine();
        this.dx = 5;
        this.dy = 5;
        this.running = false;
    }

    /**
     * Returns the fully-qualified element name.
     *
     * @return the element name
     */
    public String getElementName() {
        return elementName;
    }

    /**
     * Returns the graphic element controlled by this bot.
     *
     * @return the graphic element
     */
    public GElement getElement() {
        return element;
    }

    /**
     * Returns the parent GSpace.
     *
     * @return the GSpace
     */
    public GSpace getSpace() {
        return space;
    }

    /**
     * Returns the state machine driving this bot.
     *
     * @return the state machine
     */
    public StateMachine getStateMachine() {
        return stateMachine;
    }

    /**
     * Returns the current horizontal speed.
     *
     * @return the dx value
     */
    public int getDx() {
        return dx;
    }

    /**
     * Returns the current vertical speed.
     *
     * @return the dy value
     */
    public int getDy() {
        return dy;
    }

    /**
     * Sets the movement direction.
     *
     * @param dx the horizontal speed
     * @param dy the vertical speed
     */
    public void setDirection(int dx, int dy) {
        this.dx = dx;
        this.dy = dy;
    }

    /**
     * Returns whether this bot is currently running.
     *
     * @return {@code true} if running
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * Starts this bot so that it is processed on each tick.
     */
    public void start() {
        this.running = true;
    }

    /**
     * Stops this bot so that it is no longer processed on ticks.
     */
    public void stop() {
        this.running = false;
    }

    /**
     * Returns the position of this bot's element.
     *
     * @return the position point
     */
    public Point getPosition() {
        if (element instanceof GBounded) {
            return ((GBounded) element).getPosition();
        } else if (element instanceof GImage) {
            return ((GImage) element).getPosition();
        }
        return new Point(0, 0);
    }

    /**
     * Returns the width of this bot's element.
     *
     * @return the width in pixels
     */
    public int getWidth() {
        if (element instanceof GBounded) {
            return ((GBounded) element).getWidth();
        } else if (element instanceof GImage) {
            return ((GImage) element).getRawImage().getWidth(null);
        }
        return 0;
    }

    /**
     * Returns the height of this bot's element.
     *
     * @return the height in pixels
     */
    public int getHeight() {
        if (element instanceof GBounded) {
            return ((GBounded) element).getHeight();
        } else if (element instanceof GImage) {
            return ((GImage) element).getRawImage().getHeight(null);
        }
        return 0;
    }

    /**
     * Checks whether the element is colliding with any border of the space.
     *
     * @return {@code true} if a border collision is detected
     */
    public boolean isCollidingWithBorder() {
        Point pos = getPosition();
        int x = pos.x;
        int y = pos.y;
        int w = getWidth();
        int h = getHeight();
        int spaceW = space.getWidth();
        int spaceH = space.getHeight();

        return (x + dx < 0) || (x + w + dx > spaceW)
                || (y + dy < 0) || (y + h + dy > spaceH);
    }

    /**
     * Checks whether this element collides with another Robibot using
     * axis-aligned bounding box intersection.
     *
     * @param other the other Robibot to test against
     * @return {@code true} if the two bots overlap
     */
    public boolean isCollidingWith(Robibot other) {
        if (other == this) {
            return false;
        }
        Point p1 = getPosition();
        Point p2 = other.getPosition();
        int w1 = getWidth();
        int h1 = getHeight();
        int w2 = other.getWidth();
        int h2 = other.getHeight();

        return p1.x < p2.x + w2 && p1.x + w1 > p2.x
                && p1.y < p2.y + h2 && p1.y + h1 > p2.y;
    }

    @Override
    public String toString() {
        return "Robibot[" + elementName + ", state="
                + (stateMachine.getCurrentState() != null
                        ? stateMachine.getCurrentState().getName() : "none")
                + "]";
    }
}
