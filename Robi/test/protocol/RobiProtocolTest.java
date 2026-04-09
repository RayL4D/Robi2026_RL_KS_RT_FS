package protocol;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests unitaires pour les constantes du protocole Robi.
 * Verifie la coherence et l'unicite des prefixes.
 */
class RobiProtocolTest {

    @Test
    @DisplayName("Le port par defaut est 12345")
    void testDefaultPort() {
        assertEquals(12345, RobiProtocol.DEFAULT_PORT);
    }

    @Test
    @DisplayName("Tous les prefixes se terminent par ':'")
    void testPrefixesEndWithColon() {
        assertTrue(RobiProtocol.PREFIX_SCRIPT.endsWith(":"));
        assertTrue(RobiProtocol.PREFIX_NODES.endsWith(":"));
        assertTrue(RobiProtocol.PREFIX_ERROR.endsWith(":"));
        assertTrue(RobiProtocol.PREFIX_IMAGE.endsWith(":"));
        assertTrue(RobiProtocol.PREFIX_STATE.endsWith(":"));
        assertTrue(RobiProtocol.PREFIX_LOAD.endsWith(":"));
        assertTrue(RobiProtocol.PREFIX_BOT_TICK.endsWith(":"));
        assertTrue(RobiProtocol.PREFIX_SYNC.endsWith(":"));
        assertTrue(RobiProtocol.PREFIX_BROADCAST.endsWith(":"));
    }

    @Test
    @DisplayName("Tous les prefixes sont uniques")
    void testPrefixesAreUnique() {
        String[] prefixes = {
            RobiProtocol.PREFIX_SCRIPT,
            RobiProtocol.PREFIX_NODES,
            RobiProtocol.PREFIX_ERROR,
            RobiProtocol.PREFIX_IMAGE,
            RobiProtocol.PREFIX_SCREENSHOT,
            RobiProtocol.PREFIX_SAVE,
            RobiProtocol.PREFIX_STATE,
            RobiProtocol.PREFIX_LOAD,
            RobiProtocol.PREFIX_BOT_TICK,
            RobiProtocol.PREFIX_SYNC,
            RobiProtocol.PREFIX_BROADCAST
        };

        // Aucun doublon
        for (int i = 0; i < prefixes.length; i++) {
            for (int j = i + 1; j < prefixes.length; j++) {
                assertNotEquals(prefixes[i], prefixes[j],
                        "Les prefixes doivent etre uniques : "
                                + prefixes[i] + " vs " + prefixes[j]);
            }
        }
    }

    @Test
    @DisplayName("Aucun prefixe n'est un sous-ensemble d'un autre (pas d'ambiguite)")
    void testNoPrefixIsSubstringOfAnother() {
        String[] prefixes = {
            RobiProtocol.PREFIX_SCRIPT,
            RobiProtocol.PREFIX_NODES,
            RobiProtocol.PREFIX_ERROR,
            RobiProtocol.PREFIX_IMAGE,
            RobiProtocol.PREFIX_STATE,
            RobiProtocol.PREFIX_LOAD,
            RobiProtocol.PREFIX_BOT_TICK,
            RobiProtocol.PREFIX_SYNC,
            RobiProtocol.PREFIX_BROADCAST
        };

        for (int i = 0; i < prefixes.length; i++) {
            for (int j = 0; j < prefixes.length; j++) {
                if (i != j) {
                    assertFalse(prefixes[j].startsWith(prefixes[i]),
                            "Le prefixe '" + prefixes[i]
                                    + "' ne doit pas etre le debut de '"
                                    + prefixes[j] + "'");
                }
            }
        }
    }

    @Test
    @DisplayName("Le marqueur de fin de message est non vide")
    void testEndOfMessage() {
        assertNotNull(RobiProtocol.END_OF_MESSAGE);
        assertFalse(RobiProtocol.END_OF_MESSAGE.isEmpty());
        assertEquals("EOM", RobiProtocol.END_OF_MESSAGE);
    }

    @Test
    @DisplayName("Un message SYNC peut etre construit et parse correctement")
    void testSyncMessageFormat() {
        String cmd1 = "(space add r1 (Rect new))";
        String cmd2 = "(space.r1 setColor red)";
        String syncData = cmd1 + ";;" + cmd2;
        String message = RobiProtocol.PREFIX_SYNC + syncData;

        assertTrue(message.startsWith(RobiProtocol.PREFIX_SYNC));
        String payload = message.substring(RobiProtocol.PREFIX_SYNC.length());
        String[] commands = payload.split(";;");

        assertEquals(2, commands.length);
        assertEquals(cmd1, commands[0]);
        assertEquals(cmd2, commands[1]);
    }

    @Test
    @DisplayName("Un message BROADCAST peut etre construit et parse correctement")
    void testBroadcastMessageFormat() {
        String script = "(space.r1 translate 10 20)";
        String message = RobiProtocol.PREFIX_BROADCAST + script;

        assertTrue(message.startsWith(RobiProtocol.PREFIX_BROADCAST));
        String payload = message.substring(RobiProtocol.PREFIX_BROADCAST.length());
        assertEquals(script, payload);
    }
}
