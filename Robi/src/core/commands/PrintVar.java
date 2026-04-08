package core.commands;

import core.Command;
import core.GVar;
import core.Interpreter;
import core.Reference;
import stree.parser.SNode;

/**
 * Command that prints the current value of an integer variable.
 * Syntax: (variable print)
 * Output is routed through the interpreter to support UI listeners.
 */
public class PrintVar implements Command {

    private final Interpreter interpreter;

    /**
     * Constructs a PrintVar command.
     *
     * @param interpreter the interpreter used for output routing
     */
    public PrintVar(Interpreter interpreter) {
        this.interpreter = interpreter;
    }

    /**
     * Prints the variable name and its current value via the interpreter output.
     *
     * @param reference the reference holding the target variable
     * @param method    the S-expression node representing the command call
     * @return the same reference
     */
    @Override
    public Reference run(Reference reference, SNode method) {
        GVar var = (GVar) reference.getReceiver();
        String varName = method.get(0).contents();
        interpreter.output(varName + " = " + var.getValue());
        return reference;
    }
}
