package core.commands;

import core.Command;
import core.Environment;
import core.Interpreter;
import core.Reference;
import graphicLayer.GElement;
import stree.parser.SNode;

/**
 * Commande : (Rect new) ou (Oval new)
 * Instancie un nouvel element graphique a partir de sa classe
 * et lui attache toutes les commandes standard + add/del/addScript.
 */
public class NewElement implements Command {

    private final Environment env;
    private final Interpreter interpreter;

    /**
     * Construit une commande NewElement.
     *
     * @param env l'environnement contenant les references nommees
     * @param interpreter l'interpreteur utilise pour evaluer les arguments
     */
    public NewElement(Environment env, Interpreter interpreter) {
        this.env = env;
        this.interpreter = interpreter;
    }

    /**
     * Execute la commande NewElement sur la reference donnee.
     * Instancie un nouvel element graphique et lui attache les commandes standard.
     *
     * @param reference la reference contenant la classe de l'element a creer
     * @param method le noeud S-expression representant l'appel de commande
     * @return une nouvelle reference vers l'element cree, ou null en cas d'erreur
     */
    @Override
    public Reference run(Reference reference, SNode method) {
        try {
            @SuppressWarnings("unchecked")
            Class<? extends GElement> clazz = (Class<? extends GElement>) reference.getReceiver();
            GElement element = clazz.getDeclaredConstructor().newInstance();

            Reference ref = new Reference(element);

            // Commandes graphiques de base
            ref.addCommand("setColor", new SetColor(interpreter));
            ref.addCommand("translate", new Translate(interpreter));
            ref.addCommand("setDim", new SetDim(interpreter));

            // Capacite de conteneur (imbrication d'elements)
            ref.addCommand("add", new AddElement(env, interpreter));
            ref.addCommand("del", new DelElement(env));

            // Capacite de scripting
            ref.addCommand("addScript", new AddScript(interpreter));

            return ref;
        } catch (ReflectiveOperationException e) {
            System.err.println("NewElement : impossible d'instancier -> " + e.getMessage());
            return null;
        }
    }
}
