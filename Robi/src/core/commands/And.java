package core.commands;

import core.Command;
import core.Interpreter;
import core.Reference;
import stree.parser.SNode;

/**
 * Command that performs a logical AND on two boolean expressions.
 * Syntax: (space and expr1 expr2)
 * Returns true only if both expressions evaluate to true.
 */
public class And implements Command {

    private final Interpreter interpreter;

    /**
     * Constructs an And command.
     *
     * @param interpreter the interpreter used to evaluate boolean expressions
     */
    public And(Interpreter interpreter) {
        this.interpreter = interpreter;
    }

    /**
     * Evaluates two boolean expressions and returns their logical conjunction.
     *
     * @param reference the receiver reference
     * @param method    the S-expression node representing the command call
     * @return a new reference wrapping the boolean result
     */
    @Override
    public Reference run(Reference reference, SNode method) {
        Boolean b1 = (Boolean) interpreter.evaluateArgument(method.get(2));
        Boolean b2 = (Boolean) interpreter.evaluateArgument(method.get(3));
        return new Reference(b1 && b2);
    }
}
