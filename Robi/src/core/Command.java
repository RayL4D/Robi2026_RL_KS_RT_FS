package core;

import stree.parser.SNode;

/**
 * Interface fonctionnelle pour toutes les commandes de
 * l'interpreteur. Le receiver est la Reference qui possede
 * l'objet cible. method est la S-Expression complete parsee.
 */
public interface Command {

    /**
     * Execute la commande sur la reference donnee.
     *
     * @param reference la reference possedant l'objet cible
     * @param method    la S-Expression complete parsee
     * @return la reference resultante de l'execution
     */
    Reference run(Reference reference, SNode method);
}
