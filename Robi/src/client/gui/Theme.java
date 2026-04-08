package client.gui;

import java.awt.Color;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.border.Border;

/**
 * Theme visuel sombre pour l'application Robi Client.
 * Centralise toutes les couleurs, polices et bordures utilisees
 * par l'ensemble des composants de l'interface graphique.
 */
public class Theme {

    /** Couleur de fond foncee principale. */
    public static final Color BG_DARK = new Color(30, 30, 36);

    /** Couleur de fond moyenne. */
    public static final Color BG_MEDIUM = new Color(40, 42, 50);

    /** Couleur de fond claire. */
    public static final Color BG_LIGHT = new Color(55, 58, 68);

    /** Couleur de fond pour les champs de saisie. */
    public static final Color BG_INPUT = new Color(35, 37, 45);

    /** Couleur d'accent principale (bleu). */
    public static final Color ACCENT = new Color(99, 140, 255);

    /** Couleur d'accent au survol. */
    public static final Color ACCENT_HOVER = new Color(130, 165, 255);

    /** Couleur d'accent sombre. */
    public static final Color ACCENT_DARK = new Color(65, 100, 200);

    /** Couleur de succes (vert). */
    public static final Color SUCCESS = new Color(80, 200, 120);

    /** Couleur d'erreur (rouge). */
    public static final Color ERROR = new Color(240, 80, 80);

    /** Couleur d'avertissement (orange). */
    public static final Color WARNING = new Color(255, 180, 50);

    /** Couleur du texte principal. */
    public static final Color TEXT_PRIMARY = new Color(220, 222, 230);

    /** Couleur du texte secondaire. */
    public static final Color TEXT_SECONDARY = new Color(140, 145, 160);

    /** Couleur du texte attenue. */
    public static final Color TEXT_MUTED = new Color(90, 95, 110);

    /** Couleur de bordure par defaut. */
    public static final Color BORDER = new Color(60, 63, 75);

    /** Couleur de bordure lors du focus. */
    public static final Color BORDER_FOCUS = ACCENT;

    /** Police monospace standard. */
    public static final Font FONT_MONO = new Font("JetBrains Mono", Font.PLAIN, 13);

    /** Police monospace petite. */
    public static final Font FONT_MONO_SMALL = new Font("JetBrains Mono", Font.PLAIN, 11);

    /** Police UI standard. */
    public static final Font FONT_UI = new Font("Segoe UI", Font.PLAIN, 13);

    /** Police UI en gras. */
    public static final Font FONT_UI_BOLD = new Font("Segoe UI", Font.BOLD, 13);

    /** Police UI petite. */
    public static final Font FONT_UI_SMALL = new Font("Segoe UI", Font.PLAIN, 11);

    /** Police pour les titres. */
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 15);

    /**
     * Cree une bordure de ligne avec la couleur de bordure par defaut.
     *
     * @return la bordure par defaut
     */
    public static Border border() {
        return BorderFactory.createLineBorder(BORDER, 1);
    }

    /**
     * Cree une bordure de ligne avec la couleur de focus.
     *
     * @return la bordure de focus
     */
    public static Border borderFocus() {
        return BorderFactory.createLineBorder(BORDER_FOCUS, 1);
    }

    /**
     * Cree une bordure de marge interieure avec les dimensions specifiees.
     *
     * @param top marge superieure en pixels
     * @param left marge gauche en pixels
     * @param bottom marge inferieure en pixels
     * @param right marge droite en pixels
     * @return la bordure de marge
     */
    public static Border padding(int top, int left, int bottom, int right) {
        return BorderFactory.createEmptyBorder(top, left, bottom, right);
    }

    /**
     * Cree une bordure de marge uniforme sur les quatre cotes.
     *
     * @param p la marge en pixels
     * @return la bordure de marge uniforme
     */
    public static Border paddingAll(int p) {
        return padding(p, p, p, p);
    }

    /**
     * Constructeur prive pour empecher l'instanciation.
     */
    private Theme() {
    }
}
