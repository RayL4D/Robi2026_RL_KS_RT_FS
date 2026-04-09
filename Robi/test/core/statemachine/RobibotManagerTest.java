package core.statemachine;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.Dimension;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import core.RobiInterpreter;
import graphicLayer.GRect;
import graphicLayer.GSpace;

/**
 * Tests unitaires pour RobibotManager.
 */
class RobibotManagerTest {

    private RobibotManager manager;
    private GSpace space;
    private RobiInterpreter interpreter;

    @BeforeEach
    void setUp() {
        interpreter = new RobiInterpreter();
        manager = new RobibotManager(interpreter);
        space = new GSpace("Test", new Dimension(500, 500));
    }

    private Robibot createBot(String name) {
        GRect rect = new GRect();
        space.addElement(rect);
        return new Robibot(name, rect, space);
    }

    // ========================================================================
    // Ajout / Suppression de bots
    // ========================================================================

    @Test
    @DisplayName("Ajout d'un bot l'enregistre dans le manager")
    void testAddBot() {
        Robibot bot = createBot("space.r1");
        manager.addBot(bot);

        assertNotNull(manager.getBot("space.r1"));
        assertEquals(1, manager.getAllBots().size());
    }

    @Test
    @DisplayName("Ajout de plusieurs bots")
    void testAddMultipleBots() {
        manager.addBot(createBot("space.r1"));
        manager.addBot(createBot("space.r2"));
        manager.addBot(createBot("space.r3"));

        assertEquals(3, manager.getAllBots().size());
    }

    @Test
    @DisplayName("getBot retourne le bon bot")
    void testGetBot() {
        Robibot bot = createBot("space.r1");
        manager.addBot(bot);

        assertSame(bot, manager.getBot("space.r1"));
    }

    @Test
    @DisplayName("getBot retourne null pour un bot inexistant")
    void testGetBotUnknown() {
        assertNull(manager.getBot("space.fantome"));
    }

    @Test
    @DisplayName("Suppression d'un bot par nom")
    void testRemoveBot() {
        manager.addBot(createBot("space.r1"));
        manager.addBot(createBot("space.r2"));

        manager.removeBot("space.r1");

        assertNull(manager.getBot("space.r1"));
        assertNotNull(manager.getBot("space.r2"));
        assertEquals(1, manager.getAllBots().size());
    }

    @Test
    @DisplayName("Suppression d'un bot inexistant ne leve pas d'exception")
    void testRemoveNonExistentBot() {
        assertDoesNotThrow(() -> manager.removeBot("space.fantome"));
    }

    // ========================================================================
    // clearBots
    // ========================================================================

    @Test
    @DisplayName("clearBots supprime tous les bots")
    void testClearBots() {
        manager.addBot(createBot("space.r1"));
        manager.addBot(createBot("space.r2"));

        manager.clearBots();

        assertTrue(manager.getAllBots().isEmpty());
    }

    // ========================================================================
    // getAllBots
    // ========================================================================

    @Test
    @DisplayName("getAllBots retourne tous les bots enregistres")
    void testGetAllBots() {
        manager.addBot(createBot("space.r1"));
        manager.addBot(createBot("space.r2"));

        var all = manager.getAllBots();
        assertTrue(all.containsKey("space.r1"));
        assertTrue(all.containsKey("space.r2"));
    }

    // ========================================================================
    // TickListener
    // ========================================================================

    @Test
    @DisplayName("Ajout et suppression de tick listeners")
    void testTickListeners() {
        RobibotManager.BotTickListener listener = commands -> {};

        assertDoesNotThrow(() -> manager.addTickListener(listener));
        assertDoesNotThrow(() -> manager.removeTickListener(listener));
    }

    // ========================================================================
    // Factory integration
    // ========================================================================

    @Test
    @DisplayName("Bot cree par la factory fonctionne avec le manager")
    void testFactoryBotWithManager() {
        GRect rect = new GRect();
        space.addElement(rect);
        Robibot bot = RobibotFactory.createBouncingBot("space.r1", rect, space, 5, 5);

        manager.addBot(bot);

        assertNotNull(manager.getBot("space.r1"));
        assertEquals(5, bot.getDx());
        assertEquals(5, bot.getDy());
    }
}
