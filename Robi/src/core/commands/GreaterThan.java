package core.commands;

import core.Command;
import core.GVar;
import core.Interpreter;
import core.Reference;
import stree.parser.SNode;

/**
 * Command that tests whether a variable is strictly greater than a given value.
 * Syntax: (variable > value)
 * Returns true if the variable's value is strictly greater than the compared value.
 */
public class GreaterThan implements Command {

    private final Interpreter interpreter;

    /**
     * Constructs a GreaterThan command.
     *
     * @param interpreter the interpreter used to evaluate the comparison value
     */
    public GreaterThan(Interpreter interpreter) {
        this.interpreter = interpreter;
    }

    /**
     * Compares the variable's value against the evaluated argument using
     * the strict greater-than operator.
     *
     * @param reference the reference holding the variable to compare
     * @param method    the S-expression node representing the command call
     * @return a new reference wrapping the boolean comparison result
     */
    @Override
    public Reference run(Reference reference, SNode method) {
        int varVal = ((GVar) reference.getReceiver()).getValue();
        int compVal = (Integer) interpreter.evaluateArgument(method.get(2));
        return new Reference(varVal > compVal);
    }
}
