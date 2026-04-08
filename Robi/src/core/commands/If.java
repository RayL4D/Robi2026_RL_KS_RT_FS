package core.commands;

import core.Command;
import core.Interpreter;
import core.Reference;
import stree.parser.SNode;

/**
 * Command that implements conditional branching.
 * Syntax: (space if (condition) (actionIfTrue) [(actionIfFalse)])
 * The else block is optional.
 */
public class If implements Command {

    private final Interpreter interpreter;

    /**
     * Constructs an If command.
     *
     * @param interpreter the interpreter used to evaluate conditions and execute branches
     */
    public If(Interpreter interpreter) {
        this.interpreter = interpreter;
    }

    /**
     * Evaluates the condition and executes the appropriate branch.
     * If the condition does not return a boolean, an error is printed and
     * the receiver reference is returned unchanged.
     *
     * @param reference the receiver reference
     * @param method    the S-expression node representing the command call
     * @return the receiver reference
     */
    @Override
    public Reference run(Reference reference, SNode method) {
        SNode condition = method.get(2);
        SNode actionIfTrue = method.get(3);

        // Optional else block
        SNode actionIfFalse = (method.size() > 4) ? method.get(4) : null;

        Reference resultRef = interpreter.executeNode(condition);
        if (resultRef == null || !(resultRef.getReceiver() instanceof Boolean)) {
            System.err.println("If : la condition n'a pas retourné un booléen.");
            return reference;
        }

        boolean conditionTrue = (Boolean) resultRef.getReceiver();
        if (conditionTrue) {
            interpreter.executeNode(actionIfTrue);
        } else if (actionIfFalse != null) {
            interpreter.executeNode(actionIfFalse);
        }

        return reference;
    }
}
