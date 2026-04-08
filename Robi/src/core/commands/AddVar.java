package core.commands;

import core.Command;
import core.Environment;
import core.GVar;
import core.Interpreter;
import core.Reference;
import stree.parser.SNode;

/**
 * Command that creates an integer variable in the environment.
 * Syntax: (space addVar varName initialValue)
 * The created variable is equipped with set, add, print, and comparison commands.
 */
public class AddVar implements Command {

    private final Environment env;
    private final Interpreter interpreter;

    /**
     * Constructs an AddVar command.
     *
     * @param env         the environment in which the variable will be registered
     * @param interpreter the interpreter used to evaluate arguments
     */
    public AddVar(Environment env, Interpreter interpreter) {
        this.env = env;
        this.interpreter = interpreter;
    }

    /**
     * Creates a new integer variable and registers it in the environment
     * with its associated sub-commands (set, add, print, comparisons).
     *
     * @param reference the receiver reference
     * @param method    the S-expression node representing the command call
     * @return the newly created variable reference
     */
    @Override
    public Reference run(Reference reference, SNode method) {
        String varName = method.get(2).contents();
        int initialValue = (Integer) interpreter.evaluateArgument(method.get(3));

        Reference varRef = new Reference(new GVar(initialValue));
        varRef.addCommand("set", new SetVar(interpreter));
        varRef.addCommand("add", new AddToVar(interpreter));
        varRef.addCommand("print", new PrintVar(interpreter));
        varRef.addCommand("<", new LessThan(interpreter));
        varRef.addCommand(">", new GreaterThan(interpreter));
        varRef.addCommand("==", new Equals(interpreter));

        env.addReference(varName, varRef);
        return varRef;
    }
}
