package client.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Panneau de connexion au serveur.
 * Affiche le host, le port, le bouton connexion/deconnexion et le statut.
 */
public class ConnectionPanel extends JPanel {

    private final JTextField hostField;
    private final JTextField portField;
    private final StyledButton connectButton;
    private final StyledButton helpButton;
    private final JLabel statusLabel;
    private final JLabel statusDot;

    /**
     * Construit le panneau de connexion avec les champs host et port,
     * le bouton de connexion, l'indicateur de statut et le bouton d'aide.
     */
    public ConnectionPanel() {
        setBackground(Theme.BG_DARK);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER));

        // Partie gauche : connexion + statut
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        leftPanel.setBackground(Theme.BG_DARK);

        // Icone reseau
        JLabel netIcon = new JLabel("\u26A1");
        netIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        leftPanel.add(netIcon);

        // Label Host
        JLabel hostLabel = createLabel("H\u00f4te :");
        leftPanel.add(hostLabel);

        hostField = createTextField("localhost", 10);
        leftPanel.add(hostField);

        // Label Port
        JLabel portLabel = createLabel("Port :");
        leftPanel.add(portLabel);

        portField = createTextField("12345", 5);
        leftPanel.add(portField);

        // Bouton connexion
        connectButton = StyledButton.primary("Connecter");
        leftPanel.add(connectButton);

        // Separateur
        leftPanel.add(Box.createHorizontalStrut(12));

        // Statut
        statusDot = new JLabel("\u25CF ");
        statusDot.setForeground(Theme.ERROR);
        statusDot.setFont(Theme.FONT_UI_SMALL);
        leftPanel.add(statusDot);

        statusLabel = new JLabel("D\u00e9connect\u00e9");
        statusLabel.setFont(Theme.FONT_UI_SMALL);
        statusLabel.setForeground(Theme.TEXT_SECONDARY);
        leftPanel.add(statusLabel);

        add(leftPanel, BorderLayout.CENTER);

        // Partie droite : bouton Aide
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
        rightPanel.setBackground(Theme.BG_DARK);
        helpButton = StyledButton.primary("? Aide");
        rightPanel.add(helpButton);
        add(rightPanel, BorderLayout.EAST);
    }

    /**
     * Retourne l'adresse hote saisie dans le champ.
     *
     * @return l'adresse hote
     */
    public String getHost() {
        return hostField.getText().trim();
    }

    /**
     * Retourne le numero de port saisi dans le champ.
     *
     * @return le numero de port, ou 12345 par defaut en cas d'erreur
     */
    public int getPort() {
        try {
            return Integer.parseInt(portField.getText().trim());
        } catch (NumberFormatException e) {
            return 12345;
        }
    }

    /**
     * Retourne le bouton de connexion/deconnexion.
     *
     * @return le bouton de connexion
     */
    public StyledButton getConnectButton() {
        return connectButton;
    }

    /**
     * Retourne le bouton d'aide.
     *
     * @return le bouton Aide
     */
    public StyledButton getHelpButton() {
        return helpButton;
    }

    /**
     * Met a jour l'affichage du panneau selon l'etat de connexion.
     *
     * @param connected true si connecte, false sinon
     */
    public void setConnected(boolean connected) {
        if (connected) {
            statusDot.setForeground(Theme.SUCCESS);
            statusLabel.setText("Connect\u00e9 \u00e0 " + getHost() + ":" + getPort());
            statusLabel.setForeground(Theme.SUCCESS);
            connectButton.setBackground(Theme.ERROR);
            connectButton.setText("D\u00e9connecter");
            hostField.setEnabled(false);
            portField.setEnabled(false);
        } else {
            statusDot.setForeground(Theme.ERROR);
            statusLabel.setText("D\u00e9connect\u00e9");
            statusLabel.setForeground(Theme.TEXT_SECONDARY);
            connectButton.setText("Connecter");
            hostField.setEnabled(true);
            portField.setEnabled(true);
        }
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(Theme.TEXT_SECONDARY);
        label.setFont(Theme.FONT_UI);
        return label;
    }

    private JTextField createTextField(String defaultText, int columns) {
        JTextField field = new JTextField(defaultText, columns);
        field.setBackground(Theme.BG_INPUT);
        field.setForeground(Theme.TEXT_PRIMARY);
        field.setCaretColor(Theme.TEXT_PRIMARY);
        field.setFont(Theme.FONT_MONO);
        field.setBorder(BorderFactory.createCompoundBorder(
            Theme.border(),
            Theme.padding(4, 8, 4, 8)
        ));
        return field;
    }
}
