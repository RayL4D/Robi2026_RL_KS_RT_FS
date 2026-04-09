package core;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests unitaires pour la classe Reference.
 */
class ReferenceTest {

    @Test
    @DisplayName("Le receiver est correctement stocke et retourne")
    void testGetReceiver() {
        Reference ref = new Reference("monObjet");
        assertEquals("monObjet", ref.getReceiver());
    }

    @Test
    @DisplayName("Le receiver peut etre de n'importe quel type")
    void testReceiverTypes() {
        Reference refInt = new Reference(42);
        assertEquals(42, refInt.getReceiver());

        Reference refNull = new Reference(null);
        assertNull(refNull.getReceiver());
    }

    @Test
    @DisplayName("Ajout et recuperation d'une commande")
    void testAddAndGetCommand() {
        Reference ref = new Reference("obj");
        Command cmd = (reference, method) -> reference;

        ref.addCommand("doSomething", cmd);

        assertNotNull(ref.getCommandByName("doSomething"));
        assertSame(cmd, ref.getCommandByName("doSomething"));
    }

    @Test
    @DisplayName("Commande inexistante retourne null")
    void testUnknownCommandReturnsNull() {
        Reference ref = new Reference("obj");
        assertNull(ref.getCommandByName("fantome"));
    }

    @Test
    @DisplayName("Ecrasement d'une commande existante")
    void testOverwriteCommand() {
        Reference ref = new Reference("obj");
        Command cmd1 = (reference, method) -> reference;
        Command cmd2 = (reference, method) -> null;

        ref.addCommand("test", cmd1);
        ref.addCommand("test", cmd2);

        assertSame(cmd2, ref.getCommandByName("test"));
    }
}
