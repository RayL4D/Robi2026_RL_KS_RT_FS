package core.commands;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.Color;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Tests unitaires pour la commande SetColor et la resolution de couleurs.
 */
class SetColorTest {

    // ========================================================================
    // resolveColor - resolution de noms de couleurs
    // ========================================================================

    @ParameterizedTest
    @DisplayName("Resolution des couleurs standard")
    @CsvSource({
        "red",
        "blue",
        "green",
        "cyan",
        "magenta",
        "orange",
        "pink",
        "yellow",
        "white",
        "black",
        "gray"
    })
    void testResolveStandardColors(String colorName) {
        Color resolved = SetColor.resolveColor(colorName);
        assertNotNull(resolved, "La couleur '" + colorName + "' doit etre resolue");
    }

    @Test
    @DisplayName("Resolution de couleur en majuscules")
    void testResolveUpperCase() {
        Color color = SetColor.resolveColor("RED");
        assertNotNull(color);
        assertEquals(Color.RED, color);
    }

    @Test
    @DisplayName("Resolution de couleur en minuscules")
    void testResolveLowerCase() {
        Color color = SetColor.resolveColor("red");
        assertNotNull(color);
        assertEquals(Color.RED, color);
    }

    @Test
    @DisplayName("Couleur inconnue retourne null")
    void testResolveUnknownColor() {
        assertNull(SetColor.resolveColor("turquoise"));
        assertNull(SetColor.resolveColor(""));
        assertNull(SetColor.resolveColor("123"));
    }
}
