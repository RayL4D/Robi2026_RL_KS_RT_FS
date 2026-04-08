package core.commands;

import java.awt.Point;

import core.Command;
import core.Interpreter;
import core.Reference;
import graphicLayer.GElement;
import stree.parser.SNode;

/**
 * Commande : (cible translate dx dy)
 * dx et dy peuvent etre des litteraux, des variables ou des sous-expressions.
 */
public class Translate implements Command {

    private final Interpreter interpreter;

    /**
     * Construit une commande Translate.
     *
     * @param interpreter l'interpreteur utilise pour evaluer les arguments
     */
    public Translate(Interpreter interpreter) {
        this.interpreter = interpreter;
    }

    /**
     * Execute la commande Translate sur la reference donnee.
     * Deplace un GElement selon les deltas dx et dy.
     *
     * @param reference la reference contenant le GElement cible
     * @param method le noeud S-expression representant l'appel de commande
     * @return la reference d'origine
     */
    @Override
    public Reference run(Reference reference, SNode method) {
        Object receiver = reference.getReceiver();
        if (!(receiver instanceof GElement)) {
            System.err.println("Translate : cet objet n'est pas un GElement.");
            return reference;
        }
        int dx = (Integer) interpreter.evaluateArgument(method.get(2));
        int dy = (Integer) interpreter.evaluateArgument(method.get(3));
        ((GElement) receiver).translate(new Point(dx, dy));
        return reference;
    }
}
