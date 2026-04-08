package exercice1;

import java.awt.Color;
import java.awt.Dimension;
import graphicLayer.GRect;
import graphicLayer.GSpace;

public class Exercice1_0 {
    GSpace space = new GSpace("Exercice 1", new Dimension(200, 150));
    GRect robi = new GRect();

    public Exercice1_0() {
        // 1. Initialisation : robi doit être bleu au départ
        robi.setColor(Color.BLUE); 
        space.addElement(robi);
        space.open();

        // 2. On lance l'animation après l'ouverture de la fenêtre
        animate();
    }

    private void animate() {
        // Boucle infinie pour que l'animation se répète
        while (true) {
            
            // Étape 1 : Déplacement vers le bord droit
            // On avance tant que la position X de robi + sa largeur est inférieure à la largeur de la fenêtre
            while (robi.getX() < space.getWidth() - robi.getWidth()) {
                robi.setX(robi.getX() + 1);
                pause(5); // Pause de 5 millisecondes
            }

            // Étape 2 : Déplacement vers le bord bas
            while (robi.getY() < space.getHeight() - robi.getHeight()) {
                robi.setY(robi.getY() + 1);
                pause(5);
            }

            // Étape 3 : Déplacement vers le bord gauche
            while (robi.getX() > 0) {
                robi.setX(robi.getX() - 1);
                pause(5);
            }

            // Étape 4 : Déplacement vers le bord haut
            while (robi.getY() > 0) {
                robi.setY(robi.getY() - 1);
                pause(5);
            }

            // Étape 5 : Changement de couleur aléatoire à la fin du tour
            int r = (int) (Math.random() * 256);
            int g = (int) (Math.random() * 256);
            int b = (int) (Math.random() * 256);
            robi.setColor(new Color(r, g, b));
        }
    }

    // Méthode utilitaire pour gérer la pause sans surcharger le code principal
    private void pause(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new Exercice1_0();
    }
}