package exercice2;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import graphicLayer.GRect;
import graphicLayer.GSpace;
import stree.parser.SNode;
import stree.parser.SParser;

/**
 * Exercice 2.2 : Ajout des commandes translate et sleep au script S-Expression.
 * Anime un rectangle rouge en le deplacant avec des pauses entre chaque mouvement.
 */
public class Exercice2_2_0 {

    GSpace space = new GSpace("Exercice 2_2", new Dimension(200, 100));
    GRect robi = new GRect();

    String script = "(space color white) "
            + "(robi color red) "
            + "(robi translate 10 0) "
            + "(space sleep 100) "
            + "(robi translate 0 10) "
            + "(space sleep 100) "
            + "(robi translate -10 0) "
            + "(space sleep 100) "
            + "(robi translate 0 -10)";

    /**
     * Initialise l'espace graphique, ajoute le rectangle
     * et execute le script d'animation.
     */
    public Exercice2_2_0() {
        space.addElement(robi);
        space.open();
        this.runScript();
    }

    private void runScript() {
        SParser<SNode> parser = new SParser<>();
        List<SNode> rootNodes = null;
        try {
            rootNodes = parser.parse(script);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Iterator<SNode> itor = rootNodes.iterator();
        while (itor.hasNext()) {
            this.run(itor.next());
        }
    }

    private void run(SNode expr) {
        String target = expr.get(0).contents();
        String cmd = expr.get(1).contents();

        if (target.equals("space")) {
            if (cmd.equals("color") || cmd.equals("setColor")) {
                String colorName = expr.get(2).contents();
                space.setColor(getColorFromString(colorName));
            } else if (cmd.equals("sleep")) {
                int duration = Integer.parseInt(expr.get(2).contents());
                try {
                    Thread.sleep(duration);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } else if (target.equals("robi")) {
            if (cmd.equals("color") || cmd.equals("setColor")) {
                String colorName = expr.get(2).contents();
                robi.setColor(getColorFromString(colorName));
            } else if (cmd.equals("translate")) {
                int dx = Integer.parseInt(expr.get(2).contents());
                int dy = Integer.parseInt(expr.get(3).contents());
                robi.translate(new Point(dx, dy));
            }
        }
    }

    private Color getColorFromString(String colorName) {
        switch (colorName.toLowerCase()) {
            case "black": return Color.BLACK;
            case "yellow": return Color.YELLOW;
            case "white": return Color.WHITE;
            case "red": return Color.RED;
            case "blue": return Color.BLUE;
            default: return Color.GRAY;
        }
    }

    /**
     * Point d'entree de l'exercice 2.2.
     *
     * @param args les arguments de la ligne de commande (non utilises)
     */
    public static void main(String[] args) {
        new Exercice2_2_0();
    }
}
