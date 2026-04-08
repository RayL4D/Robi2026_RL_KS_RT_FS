package core.commands;

import core.Command;
import core.Environment;
import core.Interpreter;
import core.Reference;
import core.statemachine.Robibot;
import core.statemachine.RobibotFactory;
import core.statemachine.RobibotManager;
import graphicLayer.GBounded;
import graphicLayer.GSpace;
import stree.parser.SNode;

/**
 * Command that transforms an existing graphical element into a bouncing Robibot.
 * Syntax: (space addBot elementName [dx dy])
 * If dx and dy are omitted, the default velocity (5, 5) is used.
 */
public class AddBot implements Command {

    private final Environment env;
    private final Interpreter interpreter;
    private final RobibotManager manager;
    private final GSpace space;

    /**
     * Constructs an AddBot command.
     *
     * @param env         the environment used to resolve element references
     * @param interpreter the interpreter used to evaluate optional velocity arguments
     * @param manager     the bot manager that tracks active bots
     * @param space       the graphical space in which the bot operates
     */
    public AddBot(Environment env, Interpreter interpreter,
                  RobibotManager manager, GSpace space) {
        this.env = env;
        this.interpreter = interpreter;
        this.manager = manager;
        this.space = space;
    }

    /**
     * Resolves the named graphical element, creates a bouncing bot from it,
     * and starts the bot immediately.
     *
     * @param reference the receiver reference
     * @param method    the S-expression node representing the command call
     * @return the receiver reference
     */
    @Override
    public Reference run(Reference reference, SNode method) {
        String elementName = method.get(2).contents();

        Reference elementRef = env.getReferenceByName(elementName);
        if (elementRef == null) {
            System.err.println("AddBot : élément inconnu -> " + elementName);
            return reference;
        }

        Object receiver = elementRef.getReceiver();
        if (!(receiver instanceof GBounded)) {
            System.err.println("AddBot : l'élément n'est pas un GBounded -> " + elementName);
            return reference;
        }

        int dx = 5;
        int dy = 5;

        // Optional velocity parameters
        if (method.size() >= 5) {
            Object argDx = interpreter.evaluateArgument(method.get(3));
            Object argDy = interpreter.evaluateArgument(method.get(4));
            if (argDx instanceof Integer) {
                dx = (Integer) argDx;
            }
            if (argDy instanceof Integer) {
                dy = (Integer) argDy;
            }
        }

        Robibot bot = RobibotFactory.createBouncingBot(
                elementName, (GBounded) receiver, space, dx, dy);

        manager.addBot(bot);
        bot.start();
        bot.getStateMachine().start();

        System.out.println("Bot créé : " + bot);
        return reference;
    }
}
