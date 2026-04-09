package core.statemachine;

import graphicLayer.GBounded;
import graphicLayer.GElement;
import graphicLayer.GSpace;

/**
 * Factory that creates pre-configured Robibots with standard behaviours.
 *
 * <p>The "bouncing" behaviour works as follows:
 * <ul>
 *   <li>State "Move": translates by (dx, dy) on every tick.</li>
 *   <li>Transition "Collide" (self-loop on "Move"): fires when the bot
 *       touches a border; the condition callback inverts the direction.</li>
 *   <li>On entry after a bounce the colour is changed randomly.</li>
 * </ul>
 */
public class RobibotFactory {

    private static final String[] COLORS = {
        "red", "blue", "green", "cyan", "magenta", "orange", "yellow", "pink"
    };

    /**
     * Creates a bouncing bot that moves and bounces off the space borders.
     *
     * @param elementName the fully-qualified element name (e.g. "space.r1")
     * @param element     the graphic element to control (GBounded or GImage)
     * @param space       the parent GSpace
     * @param dx          the initial horizontal speed
     * @param dy          the initial vertical speed
     * @return a new Robibot configured with bouncing behaviour
     */
    public static Robibot createBouncingBot(String elementName, GElement element,
                                             GSpace space, int dx, int dy) {
        Robibot bot = new Robibot(elementName, element, space);
        bot.setDirection(dx, dy);

        StateMachine sm = bot.getStateMachine();

        // State "Move": translate on every tick
        State moveState = new State("Move");
        moveState.addDoAction("({element} translate {dx} {dy})");

        // Transition "Collide": when hitting a border, invert direction
        // and loop back to the same state (self-loop)
        Transition collide = new Transition("Move", b -> {
            if (b.isCollidingWithBorder()) {
                int bdx = b.getDx();
                int bdy = b.getDy();

                java.awt.Point pos = b.getPosition();
                int w = b.getWidth();
                int h = b.getHeight();
                int spaceW = b.getSpace().getWidth();
                int spaceH = b.getSpace().getHeight();

                // Invert direction on the axis that caused the collision
                if (pos.x + bdx < 0 || pos.x + w + bdx > spaceW) {
                    bdx = -bdx;
                }
                if (pos.y + bdy < 0 || pos.y + h + bdy > spaceH) {
                    bdy = -bdy;
                }

                b.setDirection(bdx, bdy);
                return true;
            }
            return false;
        });

        // On entry after a bounce: change colour randomly (only for shapes, not images)
        if (element instanceof GBounded) {
            moveState.addEntryAction("({element} setColor {randomColor})");
        }
        moveState.addTransition(collide);

        sm.addState(moveState);
        sm.setInitialState("Move");

        return bot;
    }

    /**
     * Creates a patrol bot that moves back and forth horizontally.
     *
     * @param elementName the fully-qualified element name (e.g. "space.r1")
     * @param element     the graphic element to control (GBounded or GImage)
     * @param space       the parent GSpace
     * @param speed       the horizontal speed
     * @return a new Robibot configured with horizontal patrol behaviour
     */
    public static Robibot createPatrolBot(String elementName, GElement element,
                                           GSpace space, int speed) {
        return createBouncingBot(elementName, element, space, speed, 0);
    }

    /**
     * Returns a random colour name from the predefined palette.
     *
     * @return a colour name string
     */
    private static String randomColor() {
        return COLORS[(int) (Math.random() * COLORS.length)];
    }
}
