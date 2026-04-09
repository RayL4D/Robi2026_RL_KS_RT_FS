package core.persistence;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.Color;
import java.awt.Dimension;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import core.Environment;
import core.Reference;
import core.RobiInterpreter;
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
 * Tests d'integration pour SceneSaver et SceneLoader.
 * Verifie le cycle complet : creation de scene -> sauvegarde JSON
 * -> generation de commandes -> recreation fidele.
 */
class SceneSaverLoaderTest {

    private RobiInterpreter interpreter;
    private GSpace space;

    @BeforeEach
    void setUp() {
        interpreter = new RobiInterpreter();
        space = new GSpace("Test Space", new Dimension(400, 300));
        // Forcer les dimensions reelles (composant Swing non affiche)
        space.setPreferredSize(new Dimension(400, 300));
        space.setSize(400, 300);
        Environment env = interpreter.getEnvironment();

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
    // SceneSaver
    // ========================================================================

    @Test
    @DisplayName("Sauvegarde d'une scene vide produit un JSON valide")
    void testSaveEmptyScene() {
        String json = SceneSaver.saveToJson(interpreter.getEnvironment(), space);

        assertNotNull(json);
        assertTrue(json.contains("\"space\""));
        assertTrue(json.contains("\"elements\""));
        assertTrue(json.contains("\"width\""));
        assertTrue(json.contains("\"height\""));
    }

    @Test
    @DisplayName("Sauvegarde capture les dimensions du space")
    void testSaveSpaceDimensions() {
        String json = SceneSaver.saveToJson(interpreter.getEnvironment(), space);

        assertTrue(json.contains("400"), "Doit contenir la largeur 400");
        assertTrue(json.contains("300"), "Doit contenir la hauteur 300");
    }

    @Test
    @DisplayName("Sauvegarde capture les elements avec positions")
    void testSaveElementsWithPositions() {
        interpreter.oneShot("(space add r1 (Rect new))");
        interpreter.oneShot("(space.r1 translate 120 80)");
        interpreter.oneShot("(space.r1 setColor red)");

        String json = SceneSaver.saveToJson(interpreter.getEnvironment(), space);

        assertTrue(json.contains("\"localName\": \"r1\""));
        assertTrue(json.contains("\"parent\": \"space\""));
        assertTrue(json.contains("120"), "Doit contenir la position x=120");
        assertTrue(json.contains("80"), "Doit contenir la position y=80");
    }

    @Test
    @DisplayName("Sauvegarde de plusieurs elements")
    void testSaveMultipleElements() {
        interpreter.oneShot("(space add r1 (Rect new))");
        interpreter.oneShot("(space add r2 (Rect new))");

        String json = SceneSaver.saveToJson(interpreter.getEnvironment(), space);

        assertTrue(json.contains("\"localName\": \"r1\""));
        assertTrue(json.contains("\"localName\": \"r2\""));
    }

    // ========================================================================
    // colorToName
    // ========================================================================

    @Test
    @DisplayName("colorToName convertit les couleurs standard correctement")
    void testColorToName() {
        assertEquals("red", SceneSaver.colorToName(Color.RED));
        assertEquals("blue", SceneSaver.colorToName(Color.BLUE));
        assertEquals("green", SceneSaver.colorToName(Color.GREEN));
        assertEquals("white", SceneSaver.colorToName(Color.WHITE));
        assertEquals("black", SceneSaver.colorToName(Color.BLACK));
    }

    @Test
    @DisplayName("colorToName retourne la couleur la plus proche pour une couleur non standard")
    void testColorToNameClosest() {
        // Un rouge fonce (200, 10, 10) devrait etre le plus proche de red (255, 0, 0)
        String name = SceneSaver.colorToName(new Color(200, 10, 10));
        assertEquals("red", name);
    }

    // ========================================================================
    // SceneLoader
    // ========================================================================

    @Test
    @DisplayName("Chargement d'un JSON de scene vide genere les commandes de base")
    void testLoadEmptyScene() {
        String json = SceneSaver.saveToJson(interpreter.getEnvironment(), space);
        List<String> commands = SceneLoader.jsonToSExpressions(json);

        assertFalse(commands.isEmpty());
        assertTrue(commands.get(0).contains("space clear"),
                "La premiere commande doit etre (space clear)");
    }

    @Test
    @DisplayName("Chargement genere les commandes setColor et setDim pour le space")
    void testLoadSpaceCommands() {
        String json = SceneSaver.saveToJson(interpreter.getEnvironment(), space);
        List<String> commands = SceneLoader.jsonToSExpressions(json);

        boolean hasSetColor = commands.stream().anyMatch(c -> c.contains("space setColor"));
        boolean hasSetDim = commands.stream().anyMatch(c -> c.contains("space setDim"));

        assertTrue(hasSetColor, "Doit contenir (space setColor ...)");
        assertTrue(hasSetDim, "Doit contenir (space setDim ...)");
    }

    @Test
    @DisplayName("Cycle complet : sauvegarde puis chargement restitue les elements")
    void testFullSaveLoadCycle() {
        // 1. Creer une scene
        interpreter.oneShot("(space add r1 (Rect new))");
        interpreter.oneShot("(space.r1 translate 50 75)");
        interpreter.oneShot("(space.r1 setColor red)");

        // 2. Sauvegarder
        String json = SceneSaver.saveToJson(interpreter.getEnvironment(), space);

        // 3. Recreer un environnement vierge
        RobiInterpreter interpreter2 = new RobiInterpreter();
        GSpace space2 = new GSpace("Test Space 2", new Dimension(500, 500));
        Environment env2 = interpreter2.getEnvironment();

        Reference spaceRef2 = new Reference(space2);
        spaceRef2.addCommand("setColor", new SetColor(interpreter2));
        spaceRef2.addCommand("setDim", new SetDim(interpreter2));
        spaceRef2.addCommand("add", new AddElement(env2, interpreter2));
        spaceRef2.addCommand("clear", new Clear());
        env2.addReference("space", spaceRef2);

        Reference rectClassRef2 = new Reference(GRect.class);
        rectClassRef2.addCommand("new", new NewElement(env2, interpreter2));
        env2.addReference("Rect", rectClassRef2);

        // 4. Charger les commandes generees
        List<String> commands = SceneLoader.jsonToSExpressions(json);
        for (String cmd : commands) {
            interpreter2.oneShot(cmd);
        }

        // 5. Verifier que l'element est recree
        Reference r1 = env2.getReferenceByName("space.r1");
        assertNotNull(r1, "space.r1 doit etre recree");
        assertInstanceOf(GRect.class, r1.getReceiver());

        // 6. Verifier la position
        GBounded rect = (GBounded) r1.getReceiver();
        assertEquals(50, rect.getPosition().x, "Position x doit etre 50");
        assertEquals(75, rect.getPosition().y, "Position y doit etre 75");
    }

    @Test
    @DisplayName("Les commandes de chargement contiennent les add pour chaque element")
    void testLoadCommandsContainAddForElements() {
        interpreter.oneShot("(space add r1 (Rect new))");
        interpreter.oneShot("(space add r2 (Rect new))");

        String json = SceneSaver.saveToJson(interpreter.getEnvironment(), space);
        List<String> commands = SceneLoader.jsonToSExpressions(json);

        boolean hasAddR1 = commands.stream().anyMatch(c -> c.contains("add r1"));
        boolean hasAddR2 = commands.stream().anyMatch(c -> c.contains("add r2"));

        assertTrue(hasAddR1, "Doit contenir (space add r1 ...)");
        assertTrue(hasAddR2, "Doit contenir (space add r2 ...)");
    }
}
