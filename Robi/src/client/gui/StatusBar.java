package client.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Barre de statut en bas de la fenetre.
 * Affiche le statut de connexion et les messages informatifs.
 */
public class StatusBar extends JPanel {

    private final JLabel statusLabel;
    private final JLabel infoLabel;

    /**
     * Construit la barre de statut avec un label de statut a gauche
     * et un label d'information a droite.
     */
    public StatusBar() {
        setBackground(Theme.BG_MEDIUM);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.BORDER),
            Theme.padding(4, 10, 4, 10)
        ));
        setPreferredSize(new Dimension(0, 28));

        statusLabel = new JLabel("Pr\u00eat");
        statusLabel.setForeground(Theme.TEXT_SECONDARY);
        statusLabel.setFont(Theme.FONT_UI_SMALL);
        add(statusLabel, BorderLayout.WEST);

        infoLabel = new JLabel("Robi 2026");
        infoLabel.setForeground(Theme.TEXT_MUTED);
        infoLabel.setFont(Theme.FONT_UI_SMALL);
        add(infoLabel, BorderLayout.EAST);
    }

    /**
     * Met a jour le texte de statut avec la couleur secondaire par defaut.
     *
     * @param text le texte de statut a afficher
     */
    public void setStatus(String text) {
        statusLabel.setText(text);
    }

    /**
     * Met a jour le texte de statut avec une couleur specifique.
     *
     * @param text le texte de statut a afficher
     * @param color la couleur du texte
     */
    public void setStatus(String text, Color color) {
        statusLabel.setText(text);
        statusLabel.setForeground(color);
    }

    /**
     * Met a jour le texte d'information affiche a droite.
     *
     * @param text le texte d'information
     */
    public void setInfo(String text) {
        infoLabel.setText(text);
    }
}
