package core.commands;

import java.awt.Color;

import core.Command;
import core.Interpreter;
import core.Reference;
import graphicLayer.GElement;
import graphicLayer.GSpace;
import stree.parser.SNode;

/**
 * Commande : (cible setColor nomCouleur)
 * Utilise la reflexion sur java.awt.Color pour supporter toutes les couleurs standard.
 */
public class SetColor implements Command {

    private final Interpreter interpreter;

    /**
     * Construit une commande SetColor.
     *
     * @param interpreter l'interpreteur utilise pour evaluer les arguments
     */
    public SetColor(Interpreter interpreter) {
        this.interpreter = interpreter;
    }

    /**
     * Execute la commande SetColor sur la reference donnee.
     * Change la couleur d'un GSpace ou d'un GElement.
     *
     * @param reference la reference contenant l'objet cible
     * @param method le noeud S-expression representant l'appel de commande
     * @return la reference d'origine
     */
    @Override
    public Reference run(Reference reference, SNode method) {
        Object receiver = reference.getReceiver();
        String colorName = (String) interpreter.evaluateArgument(method.get(2));
        Color color = resolveColor(colorName);

        if (color == null) {
            System.err.println("SetColor : couleur inconnue -> " + colorName);
            return reference;
        }

        if (receiver instanceof GSpace) {
            ((GSpace) receiver).setColor(color);
        } else if (receiver instanceof GElement) {
            ((GElement) receiver).setColor(color);
        } else {
            System.err.println("SetColor : cet objet ne supporte pas le changement de couleur.");
        }
        return reference;
    }

    /**
     * Resout un nom de couleur en objet Color via les champs statiques de java.awt.Color.
     * Supporte toutes les couleurs standard : red, blue, green, cyan, magenta, orange, pink, etc.
     *
     * @param colorName le nom de la couleur a resoudre
     * @return l'objet Color correspondant, ou null si la couleur est inconnue
     */
    public static Color resolveColor(String colorName) {
        try {
            return (Color) Color.class.getField(colorName.toLowerCase()).get(null);
        } catch (NoSuchFieldException e) {
            // Fallback : essayer en majuscules (BLACK, RED, etc.)
            try {
                return (Color) Color.class.getField(colorName.toUpperCase()).get(null);
            } catch (Exception ex) {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }
}
