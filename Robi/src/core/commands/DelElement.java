package core.commands;

import core.Command;
import core.Environment;
import core.Reference;
import graphicLayer.GContainer;
import graphicLayer.GElement;
import stree.parser.SNode;

/**
 * Commande : (parent del nomEnfant)
 * Supprime un element graphique d'un conteneur et nettoie l'environnement
 * (y compris les sous-elements en cascade via notation pointee).
 */
public class DelElement implements Command {

    private final Environment env;

    /**
     * Construit une commande DelElement.
     *
     * @param env l'environnement contenant les references nommees
     */
    public DelElement(Environment env) {
        this.env = env;
    }

    /**
     * Execute la commande DelElement sur la reference donnee.
     * Supprime l'element cible du conteneur et nettoie l'environnement.
     *
     * @param reference la reference contenant le conteneur parent
     * @param method le noeud S-expression representant l'appel de commande
     * @return la reference d'origine
     */
    @Override
    public Reference run(Reference reference, SNode method) {
        Object receiver = reference.getReceiver();
        if (!(receiver instanceof GContainer)) {
            System.err.println("DelElement : la cible n'est pas un conteneur.");
            return reference;
        }

        GContainer container = (GContainer) receiver;
        String parentName = method.get(0).contents();
        String targetName = method.get(2).contents();
        String fullName = parentName + "." + targetName;

        Reference targetRef = env.getReferenceByName(fullName);
        if (targetRef == null) {
            System.err.println("DelElement : element introuvable -> " + fullName);
            return reference;
        }

        container.removeElement((GElement) targetRef.getReceiver());
        env.removeWithChildren(fullName);
        container.repaint();

        return reference;
    }
}
