package exercice3;

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
 * Exercice 3 : Introduction du pattern Command.
 * Chaque commande du script est representee par un objet Command
 * avec une methode run(), ce qui elimine les if/else imbriques.
 */
public class Exercice3_0 {

    GSpace space = new GSpace("Exercice 3", new Dimension(200, 100));
    GRect robi = new GRect();
    String script = ""
            + "   (space setColor black) "
            + "   (robi setColor yellow)"
            + "   (space sleep 1000)"
            + "   (space setColor white)\n"
            + "   (space sleep 1000)"
            + "   (robi setColor red) \n"
            + "   (space sleep 1000)"
            + "   (robi translate 100 0)\n"
            + "   (space sleep 1000)\n"
            + "   (robi translate 0 50)\n"
            + "   (space sleep 1000)\n"
            + "   (robi translate -100 0)\n"
            + "   (space sleep 1000)\n"
            + "   (robi translate 0 -40)";

    /**
     * Initialise l'espace graphique, ajoute le rectangle
     * et execute le script d'animation via le pattern Command.
     */
    public Exercice3_0() {
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
        Command cmd = getCommandFromExpr(expr);
        if (cmd == null) {
            throw new Error("unable to get command for: " + expr);
        }
        cmd.run();
    }

    /**
     * Fabrique la commande correspondant a l'expression S-Expression donnee.
     *
     * @param expr l'expression S-Expression a analyser
     * @return la commande correspondante, ou null si non reconnue
     */
    Command getCommandFromExpr(SNode expr) {
        String target = expr.get(0).contents();
        String cmd = expr.get(1).contents();

        if (target.equals("space")) {
            if (cmd.equals("setColor")) {
                Color c = getColorFromString(expr.get(2).contents());
                return new SpaceChangeColor(c);
            } else if (cmd.equals("sleep")) {
                int duration = Integer.parseInt(expr.get(2).contents());
                return new SpaceSleep(duration);
            }
        } else if (target.equals("robi")) {
            if (cmd.equals("setColor")) {
                Color c = getColorFromString(expr.get(2).contents());
                return new RobiChangeColor(c);
            } else if (cmd.equals("translate")) {
                int dx = Integer.parseInt(expr.get(2).contents());
                int dy = Integer.parseInt(expr.get(3).contents());
                return new RobiTranslate(dx, dy);
            }
        }
        return null;
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
     * Point d'entree de l'exercice 3.
     *
     * @param args les arguments de la ligne de commande (non utilises)
     */
    public static void main(String[] args) {
        new Exercice3_0();
    }

    /**
     * Interface representant une commande executable.
     */
    public interface Command {

        /**
         * Execute la commande.
         */
        public void run();
    }

    /**
     * Commande qui change la couleur du space.
     */
    public class SpaceChangeColor implements Command {

        Color newColor;

        /**
         * Construit une commande de changement de couleur du space.
         *
         * @param newColor la nouvelle couleur
         */
        public SpaceChangeColor(Color newColor) {
            this.newColor = newColor;
        }

        @Override
        public void run() {
            space.setColor(newColor);
        }
    }

    /**
     * Commande qui change la couleur du rectangle robi.
     */
    public class RobiChangeColor implements Command {

        Color newColor;

        /**
         * Construit une commande de changement de couleur de robi.
         *
         * @param newColor la nouvelle couleur
         */
        public RobiChangeColor(Color newColor) {
            this.newColor = newColor;
        }

        @Override
        public void run() {
            robi.setColor(newColor);
        }
    }

    /**
     * Commande qui met en pause l'execution pendant une duree donnee.
     */
    public class SpaceSleep implements Command {

        int duration;

        /**
         * Construit une commande de pause.
         *
         * @param duration la duree en millisecondes
         */
        public SpaceSleep(int duration) {
            this.duration = duration;
        }

        @Override
        public void run() {
            try {
                Thread.sleep(duration);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Commande qui deplace le rectangle robi par translation.
     */
    public class RobiTranslate implements Command {

        int dx;
        int dy;

        /**
         * Construit une commande de translation de robi.
         *
         * @param dx le deplacement horizontal
         * @param dy le deplacement vertical
         */
        public RobiTranslate(int dx, int dy) {
            this.dx = dx;
            this.dy = dy;
        }

        @Override
        public void run() {
            robi.translate(new Point(dx, dy));
        }
    }
}
