package core.commands;

import java.awt.Image;
import java.io.File;
import javax.imageio.ImageIO;

import core.Command;
import core.Interpreter;
import core.Reference;
import graphicLayer.GImage;
import stree.parser.SNode;

/**
 * Commande : (Image new "chemin/vers/image.png")
 * Charge une image depuis le systeme de fichiers et cree un GImage.
 */
public class NewImage implements Command {

    private final Interpreter interpreter;

    /**
     * Construit une commande NewImage.
     *
     * @param interpreter l'interpreteur utilise pour evaluer les arguments
     */
    public NewImage(Interpreter interpreter) {
        this.interpreter = interpreter;
    }

    /**
     * Execute la commande NewImage sur la reference donnee.
     * Charge une image depuis un fichier et cree un GImage avec les commandes associees.
     *
     * @param reference la reference contenant la classe Image
     * @param method le noeud S-expression representant l'appel de commande
     * @return une nouvelle reference vers le GImage cree, ou null en cas d'erreur
     */
    @Override
    public Reference run(Reference reference, SNode method) {
        try {
            String path = (String) interpreter.evaluateArgument(method.get(2));
            Image img = ImageIO.read(new File(path));
            if (img == null) {
                System.err.println("NewImage : impossible de charger l'image -> " + path);
                return null;
            }
            GImage gImage = new GImage(img);
            Reference ref = new Reference(gImage);
            ref.addCommand("setColor", new SetColor(interpreter));
            ref.addCommand("translate", new Translate(interpreter));
            return ref;
        } catch (Exception e) {
            System.err.println("NewImage : erreur -> " + e.getMessage());
            return null;
        }
    }
}
