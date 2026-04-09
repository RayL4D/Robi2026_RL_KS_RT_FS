package core;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests unitaires pour la classe Environment.
 */
class EnvironmentTest {

    private Environment env;

    @BeforeEach
    void setUp() {
        env = new Environment();
    }

    // ========================================================================
    // addReference / getReferenceByName
    // ========================================================================

    @Test
    @DisplayName("Ajout et recuperation d'une reference")
    void testAddAndGetReference() {
        Reference ref = new Reference("hello");
        env.addReference("myVar", ref);

        assertNotNull(env.getReferenceByName("myVar"));
        assertEquals("hello", env.getReferenceByName("myVar").getReceiver());
    }

    @Test
    @DisplayName("Recuperation d'une reference inexistante retourne null")
    void testGetUnknownReferenceReturnsNull() {
        assertNull(env.getReferenceByName("inexistant"));
    }

    @Test
    @DisplayName("Ecrasement d'une reference existante")
    void testOverwriteReference() {
        env.addReference("x", new Reference("ancien"));
        env.addReference("x", new Reference("nouveau"));

        assertEquals("nouveau", env.getReferenceByName("x").getReceiver());
    }

    // ========================================================================
    // removeReference
    // ========================================================================

    @Test
    @DisplayName("Suppression simple d'une reference")
    void testRemoveReference() {
        env.addReference("a", new Reference(1));
        env.removeReference("a");

        assertNull(env.getReferenceByName("a"));
    }

    @Test
    @DisplayName("Suppression d'une reference inexistante ne leve pas d'exception")
    void testRemoveNonExistentReference() {
        assertDoesNotThrow(() -> env.removeReference("fantome"));
    }

    // ========================================================================
    // removeWithChildren
    // ========================================================================

    @Test
    @DisplayName("Suppression en cascade avec notation pointee")
    void testRemoveWithChildren() {
        env.addReference("space", new Reference("s"));
        env.addReference("space.r1", new Reference("r1"));
        env.addReference("space.r1.inner", new Reference("inner"));
        env.addReference("space.o1", new Reference("o1"));

        env.removeWithChildren("space.r1");

        assertNull(env.getReferenceByName("space.r1"));
        assertNull(env.getReferenceByName("space.r1.inner"));
        // Les voisins ne sont pas affectes
        assertNotNull(env.getReferenceByName("space"));
        assertNotNull(env.getReferenceByName("space.o1"));
    }

    @Test
    @DisplayName("removeWithChildren ne supprime pas les noms similaires non-enfants")
    void testRemoveWithChildrenDoesNotAffectSimilarNames() {
        env.addReference("space.r1", new Reference("r1"));
        env.addReference("space.r10", new Reference("r10"));

        env.removeWithChildren("space.r1");

        assertNull(env.getReferenceByName("space.r1"));
        assertNotNull(env.getReferenceByName("space.r10"));
    }

    // ========================================================================
    // clear
    // ========================================================================

    @Test
    @DisplayName("Clear supprime toutes les references")
    void testClear() {
        env.addReference("a", new Reference(1));
        env.addReference("b", new Reference(2));
        env.addReference("c", new Reference(3));

        env.clear();

        assertTrue(env.getAll().isEmpty());
    }

    // ========================================================================
    // getAll
    // ========================================================================

    @Test
    @DisplayName("getAll retourne une copie independante")
    void testGetAllReturnsCopy() {
        env.addReference("x", new Reference(42));

        Map<String, Reference> copy = env.getAll();
        copy.put("intrus", new Reference(99));

        // L'environnement original n'est pas modifie
        assertNull(env.getReferenceByName("intrus"));
        assertEquals(1, env.getAll().size());
    }

    @Test
    @DisplayName("getAll contient toutes les references ajoutees")
    void testGetAllContainsAll() {
        env.addReference("a", new Reference(1));
        env.addReference("b", new Reference(2));

        Map<String, Reference> all = env.getAll();
        assertEquals(2, all.size());
        assertTrue(all.containsKey("a"));
        assertTrue(all.containsKey("b"));
    }
}
