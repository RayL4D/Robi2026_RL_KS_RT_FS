package core.commands;

import core.Command;
import core.Interpreter;
import core.Reference;
import stree.parser.SNode;

/**
 * Command that performs a logical NOT on a boolean expression.
 * Syntax: (space not expr)
 * Returns the negation of the evaluated expression.
 */
public class Not implements Command {

    private final Interpreter interpreter;

    /**
     * Constructs a Not command.
     *
     * @param interpreter the interpreter used to evaluate the boolean expression
     */
    public Not(Interpreter interpreter) {
        this.interpreter = interpreter;
    }

    /**
     * Evaluates a boolean expression and returns its negation.
     *
     * @param reference the receiver reference
     * @param method    the S-expression node representing the command call
     * @return a new reference wrapping the negated boolean result
     */
    @Override
    public Reference run(Reference reference, SNode method) {
        Boolean b = (Boolean) interpreter.evaluateArgument(method.get(2));
        return new Reference(!b);
    }
}
