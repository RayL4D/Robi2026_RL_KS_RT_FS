package exercice1;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;

import graphicLayer.GRect;
import graphicLayer.GSpace;
import tools.Tools;

public class Exercice1 {

    GSpace space = new GSpace("Exercice 1", new Dimension(200, 150));
    GRect robi = new GRect();

    public Exercice1() {
        space.addElement(robi);
        space.open();
        animate();
    }

    private void animate() {
        while (true) {
            int rW = robi.getWidth();
            int rH = robi.getHeight();

            // Repositionnement en (0,0)
            robi.setPosition(new Point(0, 0));

            // --> Bord droit
            while (robi.getX() < space.getWidth() - rW) {
                robi.translate(new Point(1, 0));
                Tools.sleep(8);
            }

            // --> Bord bas
            while (robi.getY() < space.getHeight() - rH) {
                robi.translate(new Point(0, 1));
                Tools.sleep(8);
            }

            // --> Bord gauche
            while (robi.getX() > 0) {
                robi.translate(new Point(-1, 0));
                Tools.sleep(8);
            }

            // --> Bord haut
            while (robi.getY() > 0) {
                robi.translate(new Point(0, -1));
                Tools.sleep(8);
            }

            // Couleur aléatoire
            robi.setColor(new Color((int) (Math.random() * 0x1000000)));
            Tools.sleep(200);
        }
    }

    public static void main(String[] args) {
        new Exercice1();
    }
}
