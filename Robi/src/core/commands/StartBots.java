package core.commands;

import core.Command;
import core.Reference;
import core.statemachine.RobibotManager;
import stree.parser.SNode;

/**
 * Command that starts the tick loop for all registered bots.
 * Syntax: (space startBots)
 */
public class StartBots implements Command {

    private final RobibotManager manager;

    /**
     * Constructs a StartBots command.
     *
     * @param manager the bot manager controlling all registered bots
     */
    public StartBots(RobibotManager manager) {
        this.manager = manager;
    }

    /**
     * Starts all registered bots via the manager.
     *
     * @param reference the receiver reference
     * @param method    the S-expression node representing the command call
     * @return the receiver reference
     */
    @Override
    public Reference run(Reference reference, SNode method) {
        manager.startAll();
        System.out.println("Tous les bots démarrés.");
        return reference;
    }
}
