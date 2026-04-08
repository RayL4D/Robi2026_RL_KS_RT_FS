package exercice7;

import java.awt.Dimension;

import core.Environment;
import core.Reference;
import core.RobiInterpreter;
import core.commands.AddElement;
import core.commands.AddScript;
import core.commands.AddVar;
import core.commands.And;
import core.commands.Clear;
import core.commands.DelElement;
import core.commands.If;
import core.commands.NewElement;
import core.commands.NewImage;
import core.commands.NewString;
import core.commands.Not;
import core.commands.Or;
import core.commands.Repeat;
import core.commands.SetColor;
import core.commands.SetDim;
import core.commands.Sleep;
import core.commands.While;
import graphicLayer.GImage;
import graphicLayer.GOval;
import graphicLayer.GRect;
import graphicLayer.GSpace;
import graphicLayer.GString;
import tools.Tools;

/**
 * Exercice 7 : Variables, conditions, boucles et operateurs logiques.
 * Version complete de l'interpreteur qui cumule toutes les fonctionnalites
 * des exercices precedents avec en plus le controle de flux et l'arithmetique.
 * Commandes ajoutees : repeat, addVar, if, while, and, or, not.
 */
public class Exercice7 {

    RobiInterpreter interpreter = new RobiInterpreter();

    /**
     * Initialise l'environnement complet avec toutes les commandes
     * graphiques, de controle de flux et d'operateurs logiques.
     */
    public Exercice7() {
        Environment env = interpreter.getEnvironment();

        GSpace space = new GSpace("Exercice 7", new Dimension(400, 300));
        space.open();

        Reference spaceRef = new Reference(space);

        // Commandes graphiques
        spaceRef.addCommand("setColor", new SetColor(interpreter));
        spaceRef.addCommand("sleep", new Sleep(interpreter));
        spaceRef.addCommand("setDim", new SetDim(interpreter));
        spaceRef.addCommand("add", new AddElement(env, interpreter));
        spaceRef.addCommand("del", new DelElement(env));
        spaceRef.addCommand("clear", new Clear());
        spaceRef.addCommand("addScript", new AddScript(interpreter));

        // Controle de flux
        spaceRef.addCommand("repeat", new Repeat(interpreter));
        spaceRef.addCommand("if", new If(interpreter));
        spaceRef.addCommand("while", new While(interpreter));

        // Variables
        spaceRef.addCommand("addVar", new AddVar(env, interpreter));

        // Operateurs logiques
        spaceRef.addCommand("and", new And(interpreter));
        spaceRef.addCommand("or", new Or(interpreter));
        spaceRef.addCommand("not", new Not(interpreter));

        // Classes d'elements graphiques
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
     * Retourne l'interpreteur Robi configure.
     *
     * @return l'interpreteur Robi
     */
    public RobiInterpreter getInterpreter() {
        return interpreter;
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
     * Point d'entree de l'exercice 7.
     *
     * @param args les arguments de la ligne de commande (non utilises)
     */
    public static void main(String[] args) {
        new Exercice7().mainLoop();
    }
}
