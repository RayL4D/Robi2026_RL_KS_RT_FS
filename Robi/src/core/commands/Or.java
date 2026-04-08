package core.commands;

import core.Command;
import core.Interpreter;
import core.Reference;
import stree.parser.SNode;

/**
 * Command that performs a logical OR on two boolean expressions.
 * Syntax: (space or expr1 expr2)
 * Returns true if at least one of the two expressions evaluates to true.
 */
public class Or implements Command {

    private final Interpreter interpreter;

    /**
     * Constructs an Or command.
     *
     * @param interpreter the interpreter used to evaluate boolean expressions
     */
    public Or(Interpreter interpreter) {
        this.interpreter = interpreter;
    }

    /**
     * Evaluates two boolean expressions and returns their logical disjunction.
     *
     * @param reference the receiver reference
     * @param method    the S-expression node representing the command call
     * @return a new reference wrapping the boolean result
     */
    @Override
    public Reference run(Reference reference, SNode method) {
        Boolean b1 = (Boolean) interpreter.evaluateArgument(method.get(2));
        Boolean b2 = (Boolean) interpreter.evaluateArgument(method.get(3));
        return new Reference(b1 || b2);
    }
}
