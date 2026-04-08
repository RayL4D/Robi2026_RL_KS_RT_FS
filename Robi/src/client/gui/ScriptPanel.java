package client.gui;

import java.awt.BorderLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;

/**
 * Panneau d'edition de scripts S-Expression.
 * Contient une zone de saisie avec coloration syntaxique basique,
 * un bouton d'execution et un historique des commandes.
 */
public class ScriptPanel extends JPanel {

    private JTextPane scriptInput;
    private JTextArea historyArea;
    private StyledButton executeButton;
    private final List<String> commandHistory = new ArrayList<>();
    private int historyIndex = -1;

    /**
     * Construit le panneau d'edition de scripts avec la zone de saisie,
     * le bouton d'execution et l'historique des commandes.
     */
    public ScriptPanel() {
        setBackground(Theme.BG_DARK);
        setLayout(new BorderLayout(0, 0));
        setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, Theme.BORDER));

        // En-tete
        JLabel title = new JLabel("  \u2328 \u00c9diteur de script");
        title.setForeground(Theme.TEXT_PRIMARY);
        title.setFont(Theme.FONT_TITLE);
        title.setBorder(Theme.padding(10, 8, 8, 0));
        title.setOpaque(true);
        title.setBackground(Theme.BG_DARK);
        add(title, BorderLayout.NORTH);

        // Zone centrale (split : input en haut, historique en bas)
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setBackground(Theme.BG_DARK);
        splitPane.setBorder(null);
        splitPane.setDividerSize(4);
        splitPane.setResizeWeight(0.35);

        // Zone de saisie
        JPanel inputPanel = createInputPanel();
        splitPane.setTopComponent(inputPanel);

        // Historique
        JPanel historyPanel = createHistoryPanel();
        splitPane.setBottomComponent(historyPanel);

        add(splitPane, BorderLayout.CENTER);
    }

    private JPanel createInputPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setBackground(Theme.BG_DARK);
        panel.setBorder(Theme.padding(6, 10, 6, 10));

        // Zone de texte pour le script
        scriptInput = new JTextPane();
        scriptInput.setBackground(Theme.BG_INPUT);
        scriptInput.setForeground(Theme.TEXT_PRIMARY);
        scriptInput.setCaretColor(Theme.ACCENT);
        scriptInput.setFont(Theme.FONT_MONO);
        scriptInput.setText("(space setColor black)");

        // Navigation dans l'historique avec fleches haut/bas
        scriptInput.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_UP && e.isControlDown()) {
                    navigateHistory(-1);
                    e.consume();
                } else if (e.getKeyCode() == KeyEvent.VK_DOWN && e.isControlDown()) {
                    navigateHistory(1);
                    e.consume();
                }
            }
        });

        JScrollPane scrollInput = new JScrollPane(scriptInput);
        scrollInput.setBorder(Theme.border());
        scrollInput.getViewport().setBackground(Theme.BG_INPUT);
        panel.add(scrollInput, BorderLayout.CENTER);

        // Bouton + raccourci info
        JPanel bottomBar = new JPanel(new BorderLayout(8, 0));
        bottomBar.setBackground(Theme.BG_DARK);

        executeButton = StyledButton.primary("\u25B6  Ex\u00e9cuter");
        bottomBar.add(executeButton, BorderLayout.EAST);

        JLabel shortcutHint = new JLabel("Ctrl+Entr\u00e9e pour ex\u00e9cuter  |  Ctrl+\u2191\u2193 historique");
        shortcutHint.setFont(Theme.FONT_UI_SMALL);
        shortcutHint.setForeground(Theme.TEXT_MUTED);
        bottomBar.add(shortcutHint, BorderLayout.WEST);

        panel.add(bottomBar, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Theme.BG_DARK);
        panel.setBorder(Theme.padding(4, 10, 10, 10));

        JLabel histTitle = new JLabel("  \u23F3 Historique");
        histTitle.setForeground(Theme.TEXT_SECONDARY);
        histTitle.setFont(Theme.FONT_UI_BOLD);
        histTitle.setBorder(Theme.padding(4, 0, 6, 0));
        panel.add(histTitle, BorderLayout.NORTH);

        historyArea = new JTextArea();
        historyArea.setBackground(Theme.BG_INPUT);
        historyArea.setForeground(Theme.TEXT_SECONDARY);
        historyArea.setFont(Theme.FONT_MONO_SMALL);
        historyArea.setEditable(false);
        historyArea.setLineWrap(true);
        historyArea.setWrapStyleWord(true);

        JScrollPane scrollHistory = new JScrollPane(historyArea);
        scrollHistory.setBorder(Theme.border());
        scrollHistory.getViewport().setBackground(Theme.BG_INPUT);
        panel.add(scrollHistory, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Retourne le texte du script actuellement saisi.
     *
     * @return le script saisi, sans espaces de debut et fin
     */
    public String getScript() {
        return scriptInput.getText().trim();
    }

    /**
     * Definit le texte dans la zone de saisie du script.
     *
     * @param text le texte a afficher
     */
    public void setScript(String text) {
        scriptInput.setText(text);
    }

    /**
     * Efface le contenu de la zone de saisie.
     */
    public void clearInput() {
        scriptInput.setText("");
    }

    /**
     * Retourne le bouton d'execution.
     *
     * @return le bouton d'execution
     */
    public StyledButton getExecuteButton() {
        return executeButton;
    }

    /**
     * Retourne le composant de saisie du script.
     *
     * @return le JTextPane de saisie
     */
    public JTextPane getScriptInput() {
        return scriptInput;
    }

    /**
     * Ajoute une entree dans l'historique avec un prefixe de statut.
     *
     * @param prefix le prefixe a afficher avant le message
     * @param message le message a ajouter
     * @param isError true si c'est un message d'erreur
     */
    public void appendHistory(String prefix, String message, boolean isError) {
        String entry = prefix + " " + message + "\n";
        historyArea.append(entry);
        // Auto-scroll vers le bas
        historyArea.setCaretPosition(historyArea.getDocument().getLength());

        if (!isError && !message.isEmpty()) {
            commandHistory.add(message);
            historyIndex = commandHistory.size();
        }
    }

    /**
     * Ajoute un message de succes dans l'historique.
     *
     * @param script le script execute avec succes
     */
    public void appendSuccess(String script) {
        appendHistory("\u2714", script, false);
    }

    /**
     * Ajoute un message d'erreur dans l'historique.
     *
     * @param message le message d'erreur
     */
    public void appendError(String message) {
        appendHistory("\u2718", message, true);
    }

    /**
     * Ajoute un message d'information dans l'historique.
     *
     * @param message le message d'information
     */
    public void appendInfo(String message) {
        appendHistory("\u2139", message, true);
    }

    private void navigateHistory(int direction) {
        if (commandHistory.isEmpty()) {
            return;
        }
        historyIndex += direction;
        historyIndex = Math.max(0, Math.min(historyIndex, commandHistory.size()));

        if (historyIndex < commandHistory.size()) {
            scriptInput.setText(commandHistory.get(historyIndex));
        } else {
            scriptInput.setText("");
        }
    }
}
