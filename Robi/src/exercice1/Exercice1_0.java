package exercice1;

import java.awt.Color;
import java.awt.Dimension;

import graphicLayer.GRect;
import graphicLayer.GSpace;

/**
 * Exercice 1 : Animation d'un rectangle qui se deplace le long des bords
 * de la fenetre en boucle infinie, avec changement de couleur aleatoire
 * a chaque tour complet.
 */
public class Exercice1_0 {

    GSpace space = new GSpace("Exercice 1", new Dimension(200, 150));
    GRect robi = new GRect();

    /**
     * Initialise l'espace graphique, ajoute le rectangle bleu
     * et lance l'animation.
     */
    public Exercice1_0() {
        robi.setColor(Color.BLUE);
        space.addElement(robi);
        space.open();

        animate();
    }

    private void animate() {
        while (true) {
            // Deplacement vers le bord droit
            while (robi.getX() < space.getWidth() - robi.getWidth()) {
                robi.setX(robi.getX() + 1);
                pause(5);
            }

            // Deplacement vers le bord bas
            while (robi.getY() < space.getHeight() - robi.getHeight()) {
                robi.setY(robi.getY() + 1);
                pause(5);
            }

            // Deplacement vers le bord gauche
            while (robi.getX() > 0) {
                robi.setX(robi.getX() - 1);
                pause(5);
            }

            // Deplacement vers le bord haut
            while (robi.getY() > 0) {
                robi.setY(robi.getY() - 1);
                pause(5);
            }

            // Changement de couleur aleatoire a la fin du tour
            int r = (int) (Math.random() * 256);
            int g = (int) (Math.random() * 256);
            int b = (int) (Math.random() * 256);
            robi.setColor(new Color(r, g, b));
        }
    }

    private void pause(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Point d'entree de l'exercice 1.
     *
     * @param args les arguments de la ligne de commande (non utilises)
     */
    public static void main(String[] args) {
        new Exercice1_0();
    }
}
