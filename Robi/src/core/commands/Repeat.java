package core.commands;

import core.Command;
import core.Interpreter;
import core.Reference;
import stree.parser.SNode;

/**
 * Command that repeats a block of instructions a fixed number of times.
 * Syntax: (space repeat N (instruction1) (instruction2) ...)
 * N can be a literal, a variable, or a sub-expression.
 */
public class Repeat implements Command {

    private final Interpreter interpreter;

    /**
     * Constructs a Repeat command.
     *
     * @param interpreter the interpreter used to evaluate arguments and execute instructions
     */
    public Repeat(Interpreter interpreter) {
        this.interpreter = interpreter;
    }

    /**
     * Evaluates N and executes all subsequent instruction nodes N times.
     *
     * @param reference the receiver reference
     * @param method    the S-expression node representing the command call
     * @return the receiver reference
     */
    @Override
    public Reference run(Reference reference, SNode method) {
        int iterations = (Integer) interpreter.evaluateArgument(method.get(2));

        for (int i = 0; i < iterations; i++) {
            for (int j = 3; j < method.size(); j++) {
                interpreter.executeNode(method.get(j));
            }
        }
        return reference;
    }
}
