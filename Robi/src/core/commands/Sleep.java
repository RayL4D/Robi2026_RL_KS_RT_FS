package core.commands;

import core.Command;
import core.Interpreter;
import core.Reference;
import stree.parser.SNode;

/**
 * Commande : (space sleep duree)
 * La duree peut etre un litteral, une variable ou une sous-expression.
 */
public class Sleep implements Command {

    private final Interpreter interpreter;

    /**
     * Construit une commande Sleep.
     *
     * @param interpreter l'interpreteur utilise pour evaluer les arguments
     */
    public Sleep(Interpreter interpreter) {
        this.interpreter = interpreter;
    }

    /**
     * Execute la commande Sleep sur la reference donnee.
     * Met en pause le thread courant pendant la duree specifiee.
     *
     * @param reference la reference contenant l'objet cible
     * @param method le noeud S-expression representant l'appel de commande
     * @return la reference d'origine
     */
    @Override
    public Reference run(Reference reference, SNode method) {
        int duration = (Integer) interpreter.evaluateArgument(method.get(2));
        try {
            Thread.sleep(duration);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Sleep interrompu.");
        }
        return reference;
    }
}
