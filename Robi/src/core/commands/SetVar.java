package core.commands;

import core.Command;
import core.GVar;
import core.Interpreter;
import core.Reference;
import stree.parser.SNode;

/**
 * Command that sets the value of an existing integer variable.
 * Syntax: (variable set value)
 */
public class SetVar implements Command {

    private final Interpreter interpreter;

    /**
     * Constructs a SetVar command.
     *
     * @param interpreter the interpreter used to evaluate arguments
     */
    public SetVar(Interpreter interpreter) {
        this.interpreter = interpreter;
    }

    /**
     * Replaces the current value of the variable with the evaluated argument.
     *
     * @param reference the reference holding the target variable
     * @param method    the S-expression node representing the command call
     * @return the same reference after modification
     */
    @Override
    public Reference run(Reference reference, SNode method) {
        int newValue = (Integer) interpreter.evaluateArgument(method.get(2));
        ((GVar) reference.getReceiver()).setValue(newValue);
        return reference;
    }
}
