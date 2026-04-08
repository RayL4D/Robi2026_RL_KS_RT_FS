package core.commands;

import java.awt.Dimension;

import core.Command;
import core.Interpreter;
import core.Reference;
import graphicLayer.GBounded;
import graphicLayer.GSpace;
import stree.parser.SNode;

/**
 * Commande : (cible setDim largeur hauteur)
 * Redimensionne un GSpace ou un GBounded (GRect, GOval, etc.).
 */
public class SetDim implements Command {

    private final Interpreter interpreter;

    /**
     * Construit une commande SetDim.
     *
     * @param interpreter l'interpreteur utilise pour evaluer les arguments
     */
    public SetDim(Interpreter interpreter) {
        this.interpreter = interpreter;
    }

    /**
     * Execute la commande SetDim sur la reference donnee.
     * Redimensionne un GSpace ou un GBounded avec la largeur et la hauteur specifiees.
     *
     * @param reference la reference contenant l'objet cible
     * @param method le noeud S-expression representant l'appel de commande
     * @return la reference d'origine
     */
    @Override
    public Reference run(Reference reference, SNode method) {
        Object receiver = reference.getReceiver();
        int width = (Integer) interpreter.evaluateArgument(method.get(2));
        int height = (Integer) interpreter.evaluateArgument(method.get(3));
        Dimension newDim = new Dimension(width, height);

        if (receiver instanceof GSpace) {
            ((GSpace) receiver).changeWindowSize(newDim);
        } else if (receiver instanceof GBounded) {
            ((GBounded) receiver).setDimension(newDim);
        } else {
            System.err.println("SetDim : cet objet ne supporte pas le redimensionnement.");
        }
        return reference;
    }
}
