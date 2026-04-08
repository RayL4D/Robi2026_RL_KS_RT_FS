package core.commands;

import core.Command;
import core.Environment;
import core.Interpreter;
import core.Reference;
import graphicLayer.GContainer;
import graphicLayer.GElement;
import stree.parser.SNode;

/**
 * Commande : (parent add nomEnfant (Classe new ...))
 * Ajoute un nouvel element graphique dans un conteneur et l'enregistre
 * dans l'environnement avec la notation pointee (parent.enfant).
 */
public class AddElement implements Command {

    private final Environment env;
    private final Interpreter interpreter;

    /**
     * Construit une commande AddElement.
     *
     * @param env l'environnement contenant les references nommees
     * @param interpreter l'interpreteur utilise pour evaluer les arguments
     */
    public AddElement(Environment env, Interpreter interpreter) {
        this.env = env;
        this.interpreter = interpreter;
    }

    /**
     * Execute la commande AddElement sur la reference donnee.
     * Cree un nouvel element graphique et l'ajoute au conteneur parent.
     *
     * @param reference la reference contenant le conteneur parent
     * @param method le noeud S-expression representant l'appel de commande
     * @return la reference d'origine
     */
    @Override
    public Reference run(Reference reference, SNode method) {
        Object receiver = reference.getReceiver();
        if (!(receiver instanceof GContainer)) {
            System.err.println("AddElement : la cible n'est pas un conteneur.");
            return reference;
        }

        GContainer container = (GContainer) receiver;
        String parentName = method.get(0).contents();
        String childName = method.get(2).contents();
        String fullName = parentName + "." + childName;

        SNode creationNode = method.get(3);

        // Evalue la commande de creation (ex: Rect new)
        String className = creationNode.get(0).contents();
        Reference classRef = env.getReferenceByName(className);
        if (classRef == null) {
            System.err.println("AddElement : classe inconnue -> " + className);
            return reference;
        }

        Reference newObjRef = classRef.run(creationNode);
        if (newObjRef == null) {
            System.err.println("AddElement : la creation de l'objet a echoue.");
            return reference;
        }

        container.addElement((GElement) newObjRef.getReceiver());
        env.addReference(fullName, newObjRef);
        container.repaint();

        return reference;
    }
}
