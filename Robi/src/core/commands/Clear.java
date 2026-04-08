package core.commands;

import core.Command;
import core.Reference;
import graphicLayer.GContainer;
import stree.parser.SNode;

/**
 * Commande : (space clear)
 * Supprime tous les elements graphiques du conteneur.
 */
public class Clear implements Command {

    /**
     * Execute la commande Clear sur la reference donnee.
     * Supprime tous les elements graphiques du conteneur cible.
     *
     * @param reference la reference contenant le conteneur a vider
     * @param method le noeud S-expression representant l'appel de commande
     * @return la reference d'origine
     */
    @Override
    public Reference run(Reference reference, SNode method) {
        Object receiver = reference.getReceiver();
        if (receiver instanceof GContainer) {
            ((GContainer) receiver).clear();
        } else {
            System.err.println("Clear : la cible n'est pas un conteneur.");
        }
        return reference;
    }
}
