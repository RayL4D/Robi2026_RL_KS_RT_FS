package protocol;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import stree.parser.SNode;
import stree.parser.SParser;

/**
 * Tests unitaires pour la serialisation/deserialisation de SNodes.
 */
class SNodeSerializerTest {

    // ========================================================================
    // Serialize
    // ========================================================================

    @Test
    @DisplayName("Serialisation d'une expression simple")
    void testSerializeSimpleExpression() throws Exception {
        SParser<SNode> parser = new SParser<>();
        List<SNode> nodes = parser.parse("(space setColor red)");

        String serialized = SNodeSerializer.serialize(nodes);

        assertNotNull(serialized);
        assertFalse(serialized.isEmpty());
        assertTrue(serialized.contains("space"));
        assertTrue(serialized.contains("setColor"));
        assertTrue(serialized.contains("red"));
    }

    @Test
    @DisplayName("Serialisation d'une expression imbriquee")
    void testSerializeNestedExpression() throws Exception {
        SParser<SNode> parser = new SParser<>();
        List<SNode> nodes = parser.parse("(space add r1 (Rect new))");

        String serialized = SNodeSerializer.serialize(nodes);

        assertNotNull(serialized);
        assertTrue(serialized.contains("space"));
        assertTrue(serialized.contains("add"));
        assertTrue(serialized.contains("r1"));
        assertTrue(serialized.contains("Rect"));
        assertTrue(serialized.contains("new"));
    }

    // ========================================================================
    // Deserialize
    // ========================================================================

    @Test
    @DisplayName("Deserialisation reconstruit les noeuds correctement")
    void testDeserializeRebuildsNodes() throws Exception {
        SParser<SNode> parser = new SParser<>();
        List<SNode> original = parser.parse("(space setColor red)");

        String serialized = SNodeSerializer.serialize(original);
        List<SNode> deserialized = SNodeSerializer.deserialize(serialized);

        assertNotNull(deserialized);
        assertEquals(original.size(), deserialized.size());
    }

    @Test
    @DisplayName("Le contenu des feuilles est preserve apres serialisation")
    void testLeafContentPreserved() throws Exception {
        SParser<SNode> parser = new SParser<>();
        List<SNode> original = parser.parse("(space translate 10 20)");

        String serialized = SNodeSerializer.serialize(original);
        List<SNode> deserialized = SNodeSerializer.deserialize(serialized);

        SNode root = deserialized.get(0);
        assertEquals("space", root.get(0).contents());
        assertEquals("translate", root.get(1).contents());
        assertEquals("10", root.get(2).contents());
        assertEquals("20", root.get(3).contents());
    }

    // ========================================================================
    // Cycle complet
    // ========================================================================

    @Test
    @DisplayName("Cycle serialize -> deserialize preserve la structure")
    void testRoundTrip() throws Exception {
        SParser<SNode> parser = new SParser<>();
        List<SNode> original = parser.parse("(space add r1 (Rect new))");

        String serialized = SNodeSerializer.serialize(original);
        List<SNode> deserialized = SNodeSerializer.deserialize(serialized);

        // Re-serialiser et comparer
        String reSerialized = SNodeSerializer.serialize(deserialized);
        assertEquals(serialized, reSerialized,
                "Double serialisation doit produire le meme resultat");
    }

    @Test
    @DisplayName("Cycle avec expression complexe")
    void testRoundTripComplex() throws Exception {
        SParser<SNode> parser = new SParser<>();
        List<SNode> original = parser.parse(
                "(space.r1 setColor blue)");

        String serialized = SNodeSerializer.serialize(original);
        List<SNode> deserialized = SNodeSerializer.deserialize(serialized);

        assertEquals("space.r1", deserialized.get(0).get(0).contents());
        assertEquals("setColor", deserialized.get(0).get(1).contents());
        assertEquals("blue", deserialized.get(0).get(2).contents());
    }

    @Test
    @DisplayName("Serialisation d'une liste vide")
    void testSerializeEmptyList() {
        String serialized = SNodeSerializer.serialize(List.of());

        // Devrait fonctionner sans erreur
        assertNotNull(serialized);
    }
}
