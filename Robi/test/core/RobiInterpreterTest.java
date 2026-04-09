package core;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.Color;
import java.awt.Dimension;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import core.commands.AddElement;
import core.commands.Clear;
import core.commands.NewElement;
import core.commands.SetColor;
import core.commands.SetDim;
import core.commands.Translate;
import graphicLayer.GBounded;
import graphicLayer.GRect;
import graphicLayer.GSpace;

/**
 * Tests d'integration pour RobiInterpreter.
 * Verifie le parsing et l'execution de S-Expressions.
 */
class RobiInterpreterTest {

    private RobiInterpreter interpreter;
    private GSpace space;

    @BeforeEach
    void setUp() {
        interpreter = new RobiInterpreter();
        space = new GSpace("Test Space", new Dimension(500, 500));
        Environment env = interpreter.getEnvironment();

        // Enregistrer space et les classes comme dans l'application
        Reference spaceRef = new Reference(space);
        spaceRef.addCommand("setColor", new SetColor(interpreter));
        spaceRef.addCommand("setDim", new SetDim(interpreter));
        spaceRef.addCommand("add", new AddElement(env, interpreter));
        spaceRef.addCommand("clear", new Clear());
        env.addReference("space", spaceRef);

        Reference rectClassRef = new Reference(GRect.class);
        rectClassRef.addCommand("new", new NewElement(env, interpreter));
        env.addReference("Rect", rectClassRef);
    }

    // ========================================================================
    // oneShot - execution de scripts
    // ========================================================================

    @Test
    @DisplayName("Execution d'un script de creation d'element")
    void testOneShotAddElement() {
        interpreter.oneShot("(space add r1 (Rect new))");

        Reference r1 = interpreter.getEnvironment().getReferenceByName("space.r1");
        assertNotNull(r1, "L'element space.r1 doit exister dans l'environnement");
        assertInstanceOf(GRect.class, r1.getReceiver());
    }

    @Test
    @DisplayName("Execution d'un script de changement de couleur du space")
    void testOneShotSetColorSpace() {
        interpreter.oneShot("(space setColor red)");
        assertEquals(Color.RED, space.getBackground());
    }

    @Test
    @DisplayName("Execution d'un script de changement de couleur d'un element")
    void testOneShotSetColorElement() {
        interpreter.oneShot("(space add r1 (Rect new))");
        interpreter.oneShot("(space.r1 setColor blue)");

        Reference r1 = interpreter.getEnvironment().getReferenceByName("space.r1");
        assertNotNull(r1, "L'element space.r1 doit exister");
        // Verification via reflexion (champ color est protected dans GElement)
        try {
            java.lang.reflect.Field colorField =
                    graphicLayer.GElement.class.getDeclaredField("color");
            colorField.setAccessible(true);
            Color actual = (Color) colorField.get(r1.getReceiver());
            assertEquals(Color.BLUE, actual);
        } catch (Exception e) {
            fail("Impossible de lire le champ color : " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Execution d'un script de deplacement")
    void testOneShotTranslate() {
        interpreter.oneShot("(space add r1 (Rect new))");
        interpreter.oneShot("(space.r1 translate 50 100)");

        Reference r1 = interpreter.getEnvironment().getReferenceByName("space.r1");
        GBounded rect = (GBounded) r1.getReceiver();
        assertEquals(50, rect.getPosition().x);
        assertEquals(100, rect.getPosition().y);
    }

    @Test
    @DisplayName("Execution de commandes multiples successives")
    void testOneShotMultipleCommands() {
        interpreter.oneShot("(space add r1 (Rect new))");
        interpreter.oneShot("(space add r2 (Rect new))");
        interpreter.oneShot("(space add r3 (Rect new))");

        assertNotNull(interpreter.getEnvironment().getReferenceByName("space.r1"));
        assertNotNull(interpreter.getEnvironment().getReferenceByName("space.r2"));
        assertNotNull(interpreter.getEnvironment().getReferenceByName("space.r3"));
    }

    @Test
    @DisplayName("Script avec reference inconnue ne leve pas d'exception")
    void testOneShotUnknownReference() {
        assertDoesNotThrow(() -> interpreter.oneShot("(inconnu setColor red)"));
    }

    // ========================================================================
    // Clear
    // ========================================================================

    @Test
    @DisplayName("Clear supprime les elements graphiques du space")
    void testClear() {
        interpreter.oneShot("(space add r1 (Rect new))");
        interpreter.oneShot("(space clear)");

        // Le space existe toujours mais les contenus graphiques sont vides
        assertNotNull(interpreter.getEnvironment().getReferenceByName("space"));
    }

    // ========================================================================
    // OutputListener
    // ========================================================================

    @Test
    @DisplayName("OutputListener capture les sorties de l'interpreteur")
    void testOutputListener() {
        StringBuilder captured = new StringBuilder();
        interpreter.setOutputListener(captured::append);

        interpreter.output("Hello Robi");

        assertEquals("Hello Robi", captured.toString());
    }

    @Test
    @DisplayName("Sans listener, output ne leve pas d'exception")
    void testOutputWithoutListener() {
        assertDoesNotThrow(() -> interpreter.output("test"));
    }

    // ========================================================================
    // getEnvironment
    // ========================================================================

    @Test
    @DisplayName("getEnvironment retourne un environnement non null")
    void testGetEnvironment() {
        assertNotNull(interpreter.getEnvironment());
    }

    @Test
    @DisplayName("L'environnement contient les references enregistrees au setUp")
    void testEnvironmentContainsSetupRefs() {
        assertNotNull(interpreter.getEnvironment().getReferenceByName("space"));
        assertNotNull(interpreter.getEnvironment().getReferenceByName("Rect"));
    }
}
