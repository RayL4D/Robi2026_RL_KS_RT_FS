package client.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSeparator;

/**
 * Barre d'outils sur deux lignes pour l'application Robi Client.
 * Ligne 1 : Selecteur, formes, images, suppression, couleur.
 * Ligne 2 : Deplacement, scene (sauver/charger), bots, capture, clear.
 */
public class ToolbarPanel extends JPanel {

    private final JComboBox<String> elementSelector;

    private final StyledButton btnRect;
    private final StyledButton btnOval;
    private final StyledButton btnLabel;
    private final StyledButton btnAlien;
    private final StyledButton btnExplosion;

    private final StyledButton btnDelete;

    private final JComboBox<String> colorSelector;

    private final StyledButton btnUp;
    private final StyledButton btnDown;
    private final StyledButton btnLeft;
    private final StyledButton btnRight;

    private final StyledButton btnSave;
    private final StyledButton btnLoad;

    private final StyledButton btnAddBot;
    private final StyledButton btnStartBots;
    private final StyledButton btnStopBots;

    private final StyledButton btnScreenshot;
    private final StyledButton btnClear;

    /** Noms des couleurs standard de java.awt.Color (compatibles Tools.getColorByName). */
    private static final String[] COLOR_NAMES = {
        "black", "blue", "cyan", "darkGray", "gray", "green",
        "lightGray", "magenta", "orange", "pink", "red", "white", "yellow"
    };

    /**
     * Construit la barre d'outils avec tous les boutons et selecteurs.
     */
    public ToolbarPanel() {
        setBackground(Theme.BG_MEDIUM);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER));

        // Ligne 1 : Selecteur | Formes + Images | Supprimer | Couleur
        JPanel row1 = createRow();

        row1.add(createLabel("  Cible :"));
        elementSelector = new JComboBox<>();
        elementSelector.setFont(Theme.FONT_MONO);
        elementSelector.setBackground(Theme.BG_INPUT);
        elementSelector.setForeground(Theme.TEXT_PRIMARY);
        elementSelector.setPreferredSize(new Dimension(150, 28));
        elementSelector.setMaximumSize(new Dimension(150, 28));
        elementSelector.addItem("space");
        row1.add(elementSelector);

        row1.add(createSeparator());

        row1.add(createGroupLabel("Ajouter"));
        btnRect = new StyledButton("\u25AD Rect");
        btnOval = new StyledButton("\u25CB Oval");
        btnLabel = new StyledButton("A Label");
        btnAlien = new StyledButton("\uD83D\uDC7E Alien");
        btnExplosion = new StyledButton("\uD83D\uDCA5 Boom");
        row1.add(btnRect);
        row1.add(btnOval);
        row1.add(btnLabel);
        row1.add(btnAlien);
        row1.add(btnExplosion);

        row1.add(createSeparator());

        btnDelete = StyledButton.danger("\u2716 Suppr.");
        row1.add(btnDelete);

        row1.add(createSeparator());

        row1.add(createGroupLabel("Couleur"));
        colorSelector = new JComboBox<>(COLOR_NAMES);
        colorSelector.setFont(Theme.FONT_MONO);
        colorSelector.setBackground(Theme.BG_INPUT);
        colorSelector.setForeground(Theme.TEXT_PRIMARY);
        colorSelector.setPreferredSize(new Dimension(110, 28));
        colorSelector.setMaximumSize(new Dimension(110, 28));
        colorSelector.setRenderer(new ColorCellRenderer());
        row1.add(colorSelector);

        row1.add(Box.createHorizontalGlue());
        add(row1);

        // Ligne 2 : Fleches | Scene | Bots | Capture | Clear
        JPanel row2 = createRow();

        row2.add(createGroupLabel("  D\u00e9placer"));
        btnLeft = new StyledButton("\u25C0");
        btnUp = new StyledButton("\u25B2");
        btnDown = new StyledButton("\u25BC");
        btnRight = new StyledButton("\u25B6");

        Dimension arrowSize = new Dimension(36, 28);
        btnLeft.setPreferredSize(arrowSize);
        btnUp.setPreferredSize(arrowSize);
        btnDown.setPreferredSize(arrowSize);
        btnRight.setPreferredSize(arrowSize);

        row2.add(btnLeft);
        row2.add(btnUp);
        row2.add(btnDown);
        row2.add(btnRight);

        row2.add(createSeparator());

        row2.add(createGroupLabel("Scene"));
        btnSave = new StyledButton("\u2B07 Sauver");
        btnLoad = new StyledButton("\u2B06 Charger");
        row2.add(btnSave);
        row2.add(btnLoad);

        row2.add(createSeparator());

        row2.add(createGroupLabel("Bots"));
        btnAddBot = new StyledButton("\u2699 Bot");
        btnStartBots = StyledButton.primary("\u25B6 Start");
        btnStopBots = new StyledButton("\u25A0 Stop");
        row2.add(btnAddBot);
        row2.add(btnStartBots);
        row2.add(btnStopBots);

        row2.add(createSeparator());

        btnScreenshot = new StyledButton("\u2318 Capture");
        row2.add(btnScreenshot);

        row2.add(createSeparator());

        btnClear = StyledButton.danger("\u2716 Clear");
        row2.add(btnClear);

        row2.add(Box.createHorizontalGlue());
        add(row2);
    }

    private JPanel createRow() {
        JPanel row = new JPanel();
        row.setLayout(new FlowLayout(FlowLayout.LEFT, 4, 3));
        row.setBackground(Theme.BG_MEDIUM);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        return row;
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(Theme.TEXT_SECONDARY);
        label.setFont(Theme.FONT_UI);
        return label;
    }

    private JLabel createGroupLabel(String text) {
        JLabel label = new JLabel(" " + text + " ");
        label.setForeground(Theme.TEXT_MUTED);
        label.setFont(Theme.FONT_UI_SMALL);
        return label;
    }

    private JSeparator createSeparator() {
        JSeparator sep = new JSeparator(JSeparator.VERTICAL);
        sep.setPreferredSize(new Dimension(1, 24));
        sep.setForeground(Theme.BORDER);
        return sep;
    }

    /**
     * Renderer personnalise qui affiche un carre de couleur a cote du nom.
     */
    private static class ColorCellRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            String colorName = (String) value;
            Color c = resolveAwtColor(colorName);
            if (c != null) {
                setIcon(new ColorIcon(c, 12, 12));
            }
            return this;
        }
    }

    /**
     * Petit carre de couleur utilise comme icone dans le renderer de couleurs.
     */
    private static class ColorIcon implements Icon {

        private final Color color;
        private final int w;
        private final int h;

        /**
         * Construit une icone de couleur carree.
         *
         * @param color la couleur a afficher
         * @param w la largeur en pixels
         * @param h la hauteur en pixels
         */
        ColorIcon(Color color, int w, int h) {
            this.color = color;
            this.w = w;
            this.h = h;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            g.setColor(color);
            g.fillRect(x, y, w, h);
            g.setColor(Color.DARK_GRAY);
            g.drawRect(x, y, w - 1, h - 1);
        }

        @Override
        public int getIconWidth() {
            return w;
        }

        @Override
        public int getIconHeight() {
            return h;
        }
    }

    /**
     * Resout un nom de couleur vers un objet java.awt.Color.
     *
     * @param name le nom de la couleur
     * @return l'objet Color correspondant, ou null si non trouve
     */
    static Color resolveAwtColor(String name) {
        try {
            return (Color) Color.class.getField(name.toUpperCase()).get(null);
        } catch (Exception e1) {
            try {
                return (Color) Color.class.getField(name).get(null);
            } catch (Exception e2) {
                return null;
            }
        }
    }

    /**
     * Retourne le nom de l'element actuellement selectionne.
     *
     * @return le nom de l'element selectionne, ou "space" par defaut
     */
    public String getSelectedElement() {
        String selected = (String) elementSelector.getSelectedItem();
        return (selected != null) ? selected : "space";
    }

    /**
     * Retourne le nom de la couleur actuellement selectionnee.
     *
     * @return le nom de la couleur selectionnee, ou "red" par defaut
     */
    public String getSelectedColor() {
        String selected = (String) colorSelector.getSelectedItem();
        return (selected != null) ? selected : "red";
    }

    /**
     * Ajoute un element au selecteur et le selectionne.
     *
     * @param fullName le nom complet de l'element
     */
    public void addElement(String fullName) {
        elementSelector.addItem(fullName);
        elementSelector.setSelectedItem(fullName);
    }

    /**
     * Supprime un element du selecteur.
     *
     * @param fullName le nom complet de l'element a supprimer
     */
    public void removeElement(String fullName) {
        elementSelector.removeItem(fullName);
    }

    /**
     * Supprime tous les elements du selecteur et remet "space" par defaut.
     */
    public void clearElements() {
        elementSelector.removeAllItems();
        elementSelector.addItem("space");
    }

    /**
     * Retourne le selecteur d'elements.
     *
     * @return le JComboBox du selecteur d'elements
     */
    public JComboBox<String> getElementSelector() {
        return elementSelector;
    }

    /**
     * Retourne le selecteur de couleurs.
     *
     * @return le JComboBox du selecteur de couleurs
     */
    public JComboBox<String> getColorSelector() {
        return colorSelector;
    }

    /**
     * Retourne le bouton de creation de rectangle.
     *
     * @return le bouton Rect
     */
    public StyledButton getBtnRect() {
        return btnRect;
    }

    /**
     * Retourne le bouton de creation d'ovale.
     *
     * @return le bouton Oval
     */
    public StyledButton getBtnOval() {
        return btnOval;
    }

    /**
     * Retourne le bouton de creation de label.
     *
     * @return le bouton Label
     */
    public StyledButton getBtnLabel() {
        return btnLabel;
    }

    /**
     * Retourne le bouton de creation d'alien.
     *
     * @return le bouton Alien
     */
    public StyledButton getBtnAlien() {
        return btnAlien;
    }

    /**
     * Retourne le bouton de creation d'explosion.
     *
     * @return le bouton Explosion
     */
    public StyledButton getBtnExplosion() {
        return btnExplosion;
    }

    /**
     * Retourne le bouton de suppression.
     *
     * @return le bouton Delete
     */
    public StyledButton getBtnDelete() {
        return btnDelete;
    }

    /**
     * Retourne le bouton de deplacement vers le haut.
     *
     * @return le bouton Up
     */
    public StyledButton getBtnUp() {
        return btnUp;
    }

    /**
     * Retourne le bouton de deplacement vers le bas.
     *
     * @return le bouton Down
     */
    public StyledButton getBtnDown() {
        return btnDown;
    }

    /**
     * Retourne le bouton de deplacement vers la gauche.
     *
     * @return le bouton Left
     */
    public StyledButton getBtnLeft() {
        return btnLeft;
    }

    /**
     * Retourne le bouton de deplacement vers la droite.
     *
     * @return le bouton Right
     */
    public StyledButton getBtnRight() {
        return btnRight;
    }

    /**
     * Retourne le bouton de sauvegarde de scene.
     *
     * @return le bouton Save
     */
    public StyledButton getBtnSave() {
        return btnSave;
    }

    /**
     * Retourne le bouton de chargement de scene.
     *
     * @return le bouton Load
     */
    public StyledButton getBtnLoad() {
        return btnLoad;
    }

    /**
     * Retourne le bouton d'ajout de bot.
     *
     * @return le bouton AddBot
     */
    public StyledButton getBtnAddBot() {
        return btnAddBot;
    }

    /**
     * Retourne le bouton de demarrage des bots.
     *
     * @return le bouton StartBots
     */
    public StyledButton getBtnStartBots() {
        return btnStartBots;
    }

    /**
     * Retourne le bouton d'arret des bots.
     *
     * @return le bouton StopBots
     */
    public StyledButton getBtnStopBots() {
        return btnStopBots;
    }

    /**
     * Retourne le bouton de capture d'ecran.
     *
     * @return le bouton Screenshot
     */
    public StyledButton getBtnScreenshot() {
        return btnScreenshot;
    }

    /**
     * Retourne le bouton de nettoyage de la scene.
     *
     * @return le bouton Clear
     */
    public StyledButton getBtnClear() {
        return btnClear;
    }
}
