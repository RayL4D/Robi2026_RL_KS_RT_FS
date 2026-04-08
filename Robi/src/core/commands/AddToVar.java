package core.commands;

import core.Command;
import core.GVar;
import core.Interpreter;
import core.Reference;
import stree.parser.SNode;

/**
 * Command that adds a value to an existing integer variable.
 * Syntax: (variable add value)
 * A negative value can be used to subtract.
 */
public class AddToVar implements Command {

    private final Interpreter interpreter;

    /**
     * Constructs an AddToVar command.
     *
     * @param interpreter the interpreter used to evaluate arguments
     */
    public AddToVar(Interpreter interpreter) {
        this.interpreter = interpreter;
    }

    /**
     * Adds the evaluated amount to the variable's current value.
     *
     * @param reference the reference holding the target variable
     * @param method    the S-expression node representing the command call
     * @return the same reference after modification
     */
    @Override
    public Reference run(Reference reference, SNode method) {
        int amount = (Integer) interpreter.evaluateArgument(method.get(2));
        ((GVar) reference.getReceiver()).add(amount);
        return reference;
    }
}
