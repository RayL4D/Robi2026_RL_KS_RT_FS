package core.commands;

import core.Command;
import core.Interpreter;
import core.Reference;
import stree.parser.SNode;

/**
 * Command that implements a while loop.
 * Syntax: (space while (condition) (instruction1) (instruction2) ...)
 * Loops as long as the condition evaluates to true.
 */
public class While implements Command {

    private final Interpreter interpreter;

    /**
     * Constructs a While command.
     *
     * @param interpreter the interpreter used to evaluate conditions and execute instructions
     */
    public While(Interpreter interpreter) {
        this.interpreter = interpreter;
    }

    /**
     * Repeatedly evaluates the condition and, while it returns true,
     * executes all subsequent instruction nodes in order.
     *
     * @param reference the receiver reference
     * @param method    the S-expression node representing the command call
     * @return the receiver reference
     */
    @Override
    public Reference run(Reference reference, SNode method) {
        SNode condition = method.get(2);

        while (true) {
            Reference resultRef = interpreter.executeNode(condition);
            if (resultRef == null || !(resultRef.getReceiver() instanceof Boolean)) {
                break;
            }
            if (!(Boolean) resultRef.getReceiver()) {
                break;
            }

            for (int i = 3; i < method.size(); i++) {
                interpreter.executeNode(method.get(i));
            }
        }
        return reference;
    }
}
