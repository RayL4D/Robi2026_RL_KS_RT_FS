package core.commands;

import core.Command;
import core.Reference;
import core.statemachine.RobibotManager;
import stree.parser.SNode;

/**
 * Command that removes the bot behavior from a graphical element.
 * Syntax: (space delBot elementName)
 * The graphical element remains in place; only the bot is removed.
 */
public class DelBot implements Command {

    private final RobibotManager manager;

    /**
     * Constructs a DelBot command.
     *
     * @param manager the bot manager from which the bot will be removed
     */
    public DelBot(RobibotManager manager) {
        this.manager = manager;
    }

    /**
     * Removes the bot associated with the named element from the manager.
     *
     * @param reference the receiver reference
     * @param method    the S-expression node representing the command call
     * @return the receiver reference
     */
    @Override
    public Reference run(Reference reference, SNode method) {
        String elementName = method.get(2).contents();
        manager.removeBot(elementName);
        System.out.println("Bot supprimé : " + elementName);
        return reference;
    }
}
