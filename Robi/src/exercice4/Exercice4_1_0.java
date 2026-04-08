package exercice4;

import java.awt.Dimension;

import core.Environment;
import core.Reference;
import core.RobiInterpreter;
import core.commands.SetColor;
import core.commands.Sleep;
import core.commands.Translate;
import graphicLayer.GRect;
import graphicLayer.GSpace;
import tools.Tools;

/**
 * Exercice 4.1 : Introduction de Environment, Reference et Command.
 * Dispatch dynamique sans if/else, lecture interactive en console.
 * Commandes supportees : setColor, sleep, translate.
 */
public class Exercice4_1_0 {

    RobiInterpreter interpreter = new RobiInterpreter();

    /**
     * Initialise l'environnement avec un space et un rectangle,
     * enregistre les commandes et lance la boucle de lecture interactive.
     */
    public Exercice4_1_0() {
        Environment env = interpreter.getEnvironment();

        GSpace space = new GSpace("Exercice 4.1", new Dimension(200, 100));
        GRect robi = new GRect();
        space.addElement(robi);
        space.open();

        Reference spaceRef = new Reference(space);
        Reference robiRef = new Reference(robi);

        spaceRef.addCommand("setColor", new SetColor(interpreter));
        spaceRef.addCommand("sleep", new Sleep(interpreter));

        robiRef.addCommand("setColor", new SetColor(interpreter));
        robiRef.addCommand("translate", new Translate(interpreter));

        env.addReference("space", spaceRef);
        env.addReference("robi", robiRef);

        this.mainLoop();
    }

    private void mainLoop() {
        while (true) {
            System.out.print("> ");
            String input = Tools.readKeyboard();
            interpreter.oneShot(input);
        }
    }

    /**
     * Point d'entree de l'exercice 4.1.
     *
     * @param args les arguments de la ligne de commande (non utilises)
     */
    public static void main(String[] args) {
        new Exercice4_1_0();
    }
}
