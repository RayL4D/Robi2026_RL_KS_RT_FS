package core.commands;

import core.Command;
import core.Reference;
import core.statemachine.RobibotManager;
import stree.parser.SNode;

/**
 * Command that stops the tick loop for all registered bots.
 * Syntax: (space stopBots)
 */
public class StopBots implements Command {

    private final RobibotManager manager;

    /**
     * Constructs a StopBots command.
     *
     * @param manager the bot manager controlling all registered bots
     */
    public StopBots(RobibotManager manager) {
        this.manager = manager;
    }

    /**
     * Stops all registered bots via the manager.
     *
     * @param reference the receiver reference
     * @param method    the S-expression node representing the command call
     * @return the receiver reference
     */
    @Override
    public Reference run(Reference reference, SNode method) {
        manager.stopAll();
        System.out.println("Tous les bots arrêtés.");
        return reference;
    }
}
