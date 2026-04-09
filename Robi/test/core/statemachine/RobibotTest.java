package core.statemachine;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.Dimension;
import java.awt.Point;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import graphicLayer.GRect;
import graphicLayer.GSpace;

/**
 * Tests unitaires pour la classe Robibot.
 */
class RobibotTest {

    private GSpace space;
    private GRect rect;
    private Robibot bot;

    @BeforeEach
    void setUp() {
        space = new GSpace("Test", new Dimension(500, 500));
        // Forcer les dimensions reelles du composant Swing (pas de fenetre affichee)
        space.setPreferredSize(new Dimension(500, 500));
        space.setSize(500, 500);
        rect = new GRect();
        space.addElement(rect);
        bot = new Robibot("space.r1", rect, space);
    }

    // ========================================================================
    // Proprietes de base
    // ========================================================================

    @Test
    @DisplayName("Le nom de l'element est correctement retourne")
    void testGetElementName() {
        assertEquals("space.r1", bot.getElementName());
    }

    @Test
    @DisplayName("L'element graphique est correctement retourne")
    void testGetElement() {
        assertSame(rect, bot.getElement());
    }

    @Test
    @DisplayName("Le space est correctement retourne")
    void testGetSpace() {
        assertSame(space, bot.getSpace());
    }

    // ========================================================================
    // Direction
    // ========================================================================

    @Test
    @DisplayName("Direction initiale est (5, 5) par defaut")
    void testInitialDirection() {
        assertEquals(5, bot.getDx());
        assertEquals(5, bot.getDy());
    }

    @Test
    @DisplayName("setDirection modifie correctement la vitesse")
    void testSetDirection() {
        bot.setDirection(5, -3);

        assertEquals(5, bot.getDx());
        assertEquals(-3, bot.getDy());
    }

    @Test
    @DisplayName("Directions negatives sont supportees")
    void testNegativeDirection() {
        bot.setDirection(-10, -20);

        assertEquals(-10, bot.getDx());
        assertEquals(-20, bot.getDy());
    }

    // ========================================================================
    // Position et dimensions (GBounded)
    // ========================================================================

    @Test
    @DisplayName("Position initiale est (0, 0)")
    void testInitialPosition() {
        Point pos = bot.getPosition();
        assertEquals(0, pos.x);
        assertEquals(0, pos.y);
    }

    @Test
    @DisplayName("getPosition reflete la position du GBounded")
    void testGetPositionAfterTranslate() {
        rect.translate(new Point(30, 50));

        Point pos = bot.getPosition();
        assertEquals(30, pos.x);
        assertEquals(50, pos.y);
    }

    @Test
    @DisplayName("getWidth retourne la largeur de l'element")
    void testGetWidth() {
        assertTrue(bot.getWidth() > 0, "La largeur doit etre positive");
    }

    @Test
    @DisplayName("getHeight retourne la hauteur de l'element")
    void testGetHeight() {
        assertTrue(bot.getHeight() > 0, "La hauteur doit etre positive");
    }

    // ========================================================================
    // Start / Stop
    // ========================================================================

    @Test
    @DisplayName("Bot n'est pas en cours d'execution a la creation")
    void testNotRunningInitially() {
        assertFalse(bot.isRunning());
    }

    @Test
    @DisplayName("start puis stop changent l'etat")
    void testStartStop() {
        bot.getStateMachine().addState(new State("Move"));
        bot.getStateMachine().setInitialState("Move");

        bot.start();
        assertTrue(bot.isRunning());

        bot.stop();
        assertFalse(bot.isRunning());
    }

    // ========================================================================
    // Collision avec les bords
    // ========================================================================

    @Test
    @DisplayName("Pas de collision au centre du space")
    void testNoCollisionAtCenter() {
        rect.translate(new Point(200, 200));
        bot.setDirection(3, 3);

        // Position (200,200) + direction (3,3) + taille ~20 -> largement dans le space 500x500
        assertFalse(bot.isCollidingWithBorder());
    }

    @Test
    @DisplayName("Collision avec le bord droit")
    void testCollisionRightBorder() {
        int w = bot.getWidth();
        rect.translate(new Point(500 - w, 100)); // colle au bord droit
        bot.setDirection(5, 0); // prochain pas depasse le bord droit

        assertTrue(bot.isCollidingWithBorder());
    }

    @Test
    @DisplayName("Collision avec le bord bas")
    void testCollisionBottomBorder() {
        int h = bot.getHeight();
        rect.translate(new Point(100, 500 - h)); // colle au bord bas
        bot.setDirection(0, 5); // prochain pas depasse le bord bas

        assertTrue(bot.isCollidingWithBorder());
    }

    @Test
    @DisplayName("Collision avec le bord gauche")
    void testCollisionLeftBorder() {
        // Position initiale (0,0), prochain pas x + (-5) = -5 < 0
        bot.setDirection(-5, 0);

        assertTrue(bot.isCollidingWithBorder());
    }

    @Test
    @DisplayName("Collision avec le bord haut")
    void testCollisionTopBorder() {
        // Position initiale (0,0), prochain pas y + (-5) = -5 < 0
        bot.setDirection(0, -5);

        assertTrue(bot.isCollidingWithBorder());
    }

    // ========================================================================
    // Collision entre bots
    // ========================================================================

    @Test
    @DisplayName("Deux bots au meme endroit sont en collision")
    void testCollisionBetweenBots() {
        GRect rect2 = new GRect();
        space.addElement(rect2);
        Robibot bot2 = new Robibot("space.r2", rect2, space);

        // Les deux sont a (0,0) avec les memes dimensions -> collision
        assertTrue(bot.isCollidingWith(bot2));
    }

    @Test
    @DisplayName("Deux bots eloignes ne sont pas en collision")
    void testNoCollisionBetweenDistantBots() {
        GRect rect2 = new GRect();
        space.addElement(rect2);
        rect2.translate(new Point(300, 300));
        Robibot bot2 = new Robibot("space.r2", rect2, space);

        assertFalse(bot.isCollidingWith(bot2));
    }
}
