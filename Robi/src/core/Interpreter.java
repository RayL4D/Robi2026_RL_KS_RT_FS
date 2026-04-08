package core;

import stree.parser.SNode;

/**
 * Interface decouplant les commandes de la classe principale.
 * Permet aux Command d'evaluer des sous-expressions et
 * d'executer des noeuds sans dependre d'une classe concrete
 * (Exercice5, Exercice6, etc.).
 */
public interface Interpreter {

    /**
     * Execute un noeud S-Expression et retourne la Reference
     * resultante.
     *
     * @param expr le noeud S-Expression a executer
     * @return la reference resultante de l'execution
     */
    Reference executeNode(SNode expr);

    /**
     * Evalue un argument qui peut etre :
     * <ul>
     *   <li>une sous-expression (SNode avec enfants) -
     *       execution recursive</li>
     *   <li>un entier litteral</li>
     *   <li>le nom d'une variable existante - resolution
     *       de sa valeur</li>
     *   <li>un texte brut (nom de couleur, etc.)</li>
     * </ul>
     *
     * @param arg le noeud argument a evaluer
     * @return la valeur evaluee de l'argument
     */
    Object evaluateArgument(SNode arg);

    /**
     * Execute un script complet (chaine de S-Expressions).
     *
     * @param script le script a executer
     */
    void oneShot(String script);

    /**
     * Retourne l'environnement courant.
     *
     * @return l'environnement de l'interpreteur
     */
    Environment getEnvironment();

    /**
     * Envoie un message de sortie (print, debug, etc.) vers
     * l'interface. Par defaut, affiche dans System.out.
     *
     * @param message le message a afficher
     */
    default void output(String message) {
        System.out.println(message);
    }
}
