package core.commands;

import core.Command;
import core.Interpreter;
import core.Reference;
import stree.parser.SNode;

/**
 * Command that registers a user-defined script as a new command on a target reference.
 * Syntax: (target addScript scriptName (params) (instructions...))
 */
public class AddScript implements Command {

    private final Interpreter interpreter;

    /**
     * Constructs an AddScript command.
     *
     * @param interpreter the interpreter used to execute the registered script
     */
    public AddScript(Interpreter interpreter) {
        this.interpreter = interpreter;
    }

    /**
     * Extracts the script name and body from the method node, then registers
     * a new {@link CustomScript} command on the receiver reference.
     *
     * @param reference the target reference on which the script is registered
     * @param method    the S-expression node representing the command call
     * @return the target reference
     */
    @Override
    public Reference run(Reference reference, SNode method) {
        String scriptName = method.get(2).contents();
        SNode scriptBody = method.get(3);
        reference.addCommand(scriptName, new CustomScript(scriptBody, interpreter));
        return reference;
    }
}
