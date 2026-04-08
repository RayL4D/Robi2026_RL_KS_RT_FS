package core.commands;

import core.Command;
import core.Interpreter;
import core.Reference;
import stree.parser.SNode;

/**
 * Dynamic command created by {@link AddScript}.
 * Performs macro-expansion by replacing formal parameters with actual arguments,
 * then executes the resulting script through the interpreter.
 */
public class CustomScript implements Command {

    private final SNode scriptDef;
    private final Interpreter interpreter;

    /**
     * Constructs a CustomScript command.
     *
     * @param scriptDef   the S-expression node defining the script (parameters + body)
     * @param interpreter the interpreter used to execute the expanded script
     */
    public CustomScript(SNode scriptDef, Interpreter interpreter) {
        this.scriptDef = scriptDef;
        this.interpreter = interpreter;
    }

    /**
     * Performs macro-expansion on the script definition by substituting formal
     * parameters with actual arguments, then executes the expanded script.
     *
     * @param reference the receiver reference (bound to the first formal parameter)
     * @param method    the S-expression node representing the command call
     * @return the receiver reference
     */
    @Override
    public Reference run(Reference reference, SNode method) {
        SNode params = scriptDef.get(0); // e.g. (self name c w)

        // 1. Reconstruct the script text (all instructions after the params)
        StringBuilder template = new StringBuilder();
        for (int i = 1; i < scriptDef.size(); i++) {
            template.append(snodeToString(scriptDef.get(i))).append(" ");
        }

        String expandedScript = template.toString();

        // 2. Macro-expansion: replace each formal parameter with the actual argument
        for (int i = 0; i < params.size(); i++) {
            String paramName = params.get(i).contents();
            // self (index 0) maps to the receiver (index 0 of the call)
            // other params (index 1, 2...) map to args (index 2, 3... of the call)
            String argValue = method.get(i == 0 ? 0 : i + 1).contents();
            expandedScript = expandedScript.replaceAll("\\b" + paramName + "\\b", argValue);
        }

        // 3. Execute the expanded script
        interpreter.oneShot(expandedScript);

        return reference;
    }

    /**
     * Converts an SNode tree into a readable S-expression string.
     *
     * @param node the SNode to convert
     * @return the string representation of the S-expression
     */
    private String snodeToString(SNode node) {
        if (node.isLeaf()) {
            return node.contents();
        }

        StringBuilder sb = new StringBuilder("(");
        for (int i = 0; i < node.size(); i++) {
            if (i > 0) {
                sb.append(" ");
            }
            sb.append(snodeToString(node.get(i)));
        }
        sb.append(")");
        return sb.toString();
    }
}
