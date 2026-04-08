package core.commands;

import core.Command;
import core.Interpreter;
import core.Reference;
import graphicLayer.GString;
import stree.parser.SNode;

/**
 * Commande : (Label new "texte")
 * Cree un GString avec le texte fourni en parametre.
 */
public class NewString implements Command {

    private final Interpreter interpreter;

    /**
     * Construit une commande NewString.
     *
     * @param interpreter l'interpreteur utilise pour evaluer les arguments
     */
    public NewString(Interpreter interpreter) {
        this.interpreter = interpreter;
    }

    /**
     * Execute la commande NewString sur la reference donnee.
     * Cree un GString avec le texte specifie et lui attache les commandes standard.
     *
     * @param reference la reference contenant la classe Label
     * @param method le noeud S-expression representant l'appel de commande
     * @return une nouvelle reference vers le GString cree, ou null en cas d'erreur
     */
    @Override
    public Reference run(Reference reference, SNode method) {
        try {
            String text = "";
            if (method.size() > 2) {
                text = (String) interpreter.evaluateArgument(method.get(2));
            }
            GString gString = new GString(text);
            Reference ref = new Reference(gString);
            ref.addCommand("setColor", new SetColor(interpreter));
            ref.addCommand("translate", new Translate(interpreter));
            ref.addCommand("setDim", new SetDim(interpreter));
            return ref;
        } catch (Exception e) {
            System.err.println("NewString : erreur -> " + e.getMessage());
            return null;
        }
    }
}
