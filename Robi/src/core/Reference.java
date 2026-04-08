package core;

import java.util.HashMap;
import java.util.Map;

import stree.parser.SNode;

/**
 * Associe un objet recepteur (GSpace, GRect, Class, GVar,
 * Boolean...) a un dictionnaire de commandes nommees.
 * Le dispatch se fait dynamiquement via le nom de la commande
 * dans la S-Expression.
 */
public class Reference {
    private final Object receiver;
    private final Map<String, Command> primitives;

    /**
     * Construit une reference autour d'un objet recepteur.
     *
     * @param receiver l'objet recepteur associe
     */
    public Reference(Object receiver) {
        this.receiver = receiver;
        this.primitives = new HashMap<>();
    }

    /**
     * Retourne l'objet recepteur de cette reference.
     *
     * @return l'objet recepteur
     */
    public Object getReceiver() {
        return receiver;
    }

    /**
     * Ajoute une commande associee a un selecteur.
     *
     * @param selector  le nom du selecteur de la commande
     * @param primitive la commande a enregistrer
     */
    public void addCommand(String selector, Command primitive) {
        primitives.put(selector, primitive);
    }

    /**
     * Retourne la commande associee au selecteur donne.
     *
     * @param selector le nom du selecteur recherche
     * @return la commande correspondante, ou {@code null}
     */
    public Command getCommandByName(String selector) {
        return primitives.get(selector);
    }

    /**
     * Dispatch dynamique : extrait le nom de la commande depuis
     * la S-Expression (index 1) et l'execute si elle existe.
     *
     * @param method la S-Expression contenant la commande
     * @return la reference resultante, ou {@code null}
     */
    public Reference run(SNode method) {
        String cmdName = method.get(1).contents();
        Command cmd = getCommandByName(cmdName);

        if (cmd != null) {
            return cmd.run(this, method);
        }

        System.err.println(
                "Commande inconnue pour cet objet : " + cmdName);
        return null;
    }
}
