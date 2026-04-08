package exercice5;

import java.awt.Dimension;

import core.Environment;
import core.Reference;
import core.RobiInterpreter;
import core.commands.AddElement;
import core.commands.DelElement;
import core.commands.NewElement;
import core.commands.NewImage;
import core.commands.NewString;
import core.commands.SetColor;
import core.commands.SetDim;
import core.commands.Sleep;
import graphicLayer.GImage;
import graphicLayer.GOval;
import graphicLayer.GRect;
import graphicLayer.GSpace;
import graphicLayer.GString;
import tools.Tools;

/**
 * Exercice 5 : Notation pointee et conteneurs imbriques.
 * Les elements graphiques (Rect, Oval) peuvent contenir d'autres elements.
 * Commandes ajoutees : setDim, add/del sur les sous-elements.
 */
public class Exercice5 {

    RobiInterpreter interpreter = new RobiInterpreter();

    /**
     * Initialise l'environnement avec les commandes de conteneurs imbriques
     * et ouvre l'espace graphique.
     */
    public Exercice5() {
        Environment env = interpreter.getEnvironment();

        GSpace space = new GSpace("Exercice 5", new Dimension(200, 150));
        space.open();

        Reference spaceRef = new Reference(space);
        spaceRef.addCommand("setColor", new SetColor(interpreter));
        spaceRef.addCommand("sleep", new Sleep(interpreter));
        spaceRef.addCommand("add", new AddElement(env, interpreter));
        spaceRef.addCommand("del", new DelElement(env));
        spaceRef.addCommand("setDim", new SetDim(interpreter));

        Reference rectClassRef = new Reference(GRect.class);
        Reference ovalClassRef = new Reference(GOval.class);
        Reference imageClassRef = new Reference(GImage.class);
        Reference stringClassRef = new Reference(GString.class);

        rectClassRef.addCommand("new", new NewElement(env, interpreter));
        ovalClassRef.addCommand("new", new NewElement(env, interpreter));
        imageClassRef.addCommand("new", new NewImage(interpreter));
        stringClassRef.addCommand("new", new NewString(interpreter));

        env.addReference("space", spaceRef);
        env.addReference("Rect", rectClassRef);
        env.addReference("Oval", ovalClassRef);
        env.addReference("Image", imageClassRef);
        env.addReference("Label", stringClassRef);
    }

    /**
     * Lance la boucle de lecture interactive en console.
     */
    public void mainLoop() {
        while (true) {
            System.out.print("> ");
            String input = Tools.readKeyboard();
            interpreter.oneShot(input);
        }
    }

    /**
     * Point d'entree de l'exercice 5.
     *
     * @param args les arguments de la ligne de commande (non utilises)
     */
    public static void main(String[] args) {
        new Exercice5().mainLoop();
    }
}
