package client.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

/**
 * Dialogue d'aide affichant le guide d'utilisation de l'application Robi Client.
 */
public class HelpDialog extends JDialog {

    /**
     * Construit et affiche le dialogue d'aide.
     *
     * @param parent la fenetre parente
     */
    public HelpDialog(JFrame parent) {
        super(parent, "Guide d'utilisation — Robi 2026", true);
        setSize(720, 700);
        setLocationRelativeTo(parent);
        getContentPane().setBackground(Theme.BG_DARK);
        setLayout(new BorderLayout(0, 0));

        // Zone de texte HTML
        JTextPane helpPane = new JTextPane();
        helpPane.setContentType("text/html");
        helpPane.setEditable(false);
        helpPane.setBackground(Theme.BG_DARK);
        helpPane.setForeground(Theme.TEXT_PRIMARY);
        helpPane.setText(buildHelpHtml());
        helpPane.setCaretPosition(0);

        JScrollPane scroll = new JScrollPane(helpPane);
        scroll.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        scroll.getViewport().setBackground(Theme.BG_DARK);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);

        // Bouton Fermer
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.setBackground(Theme.BG_DARK);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));
        StyledButton closeBtn = StyledButton.primary("Fermer");
        closeBtn.addActionListener(e -> dispose());
        bottomPanel.add(closeBtn);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    /**
     * Genere le contenu HTML du guide d'utilisation.
     *
     * @return le HTML complet du guide
     */
    private String buildHelpHtml() {
        String bg = colorToHex(Theme.BG_DARK);
        String bgMed = colorToHex(Theme.BG_MEDIUM);
        String bgLight = colorToHex(Theme.BG_LIGHT);
        String text = colorToHex(Theme.TEXT_PRIMARY);
        String textSec = colorToHex(Theme.TEXT_SECONDARY);
        String accent = colorToHex(Theme.ACCENT);
        String success = colorToHex(Theme.SUCCESS);
        String warning = colorToHex(Theme.WARNING);
        String error = colorToHex(Theme.ERROR);

        return "<html><head><style>"
            + "body { font-family: 'Segoe UI', Arial, sans-serif; font-size: 13px; "
            + "  color: " + text + "; background: " + bg + "; margin: 8px; }"
            + "h1 { color: " + accent + "; font-size: 20px; margin-bottom: 4px; }"
            + "h2 { color: " + accent + "; font-size: 15px; margin-top: 18px; "
            + "  border-bottom: 1px solid " + bgLight + "; padding-bottom: 4px; }"
            + "h3 { color: " + success + "; font-size: 13px; margin-top: 12px; margin-bottom: 2px; }"
            + "p, li { line-height: 1.5; color: " + text + "; }"
            + "code { font-family: 'JetBrains Mono', monospace; font-size: 12px; "
            + "  background: " + bgMed + "; color: " + warning + "; "
            + "  padding: 1px 5px; border-radius: 3px; }"
            + "pre { background: " + bgMed + "; color: " + warning + "; "
            + "  padding: 10px 14px; border-radius: 6px; font-family: 'JetBrains Mono', monospace; "
            + "  font-size: 12px; white-space: pre-wrap; border: 1px solid " + bgLight + "; }"
            + ".kbd { display: inline-block; background: " + bgLight + "; color: " + text + "; "
            + "  padding: 1px 7px; border-radius: 4px; font-size: 12px; "
            + "  border: 1px solid " + textSec + "; font-family: 'JetBrains Mono', monospace; }"
            + "table { border-collapse: collapse; width: 100%; margin: 6px 0; }"
            + "th { background: " + bgMed + "; color: " + accent + "; padding: 6px 10px; "
            + "  text-align: left; font-size: 12px; }"
            + "td { padding: 5px 10px; border-bottom: 1px solid " + bgLight + "; font-size: 12px; }"
            + "tr:hover td { background: " + bgMed + "; }"
            + ".tip { background: " + bgMed + "; border-left: 3px solid " + accent + "; "
            + "  padding: 8px 12px; margin: 8px 0; border-radius: 0 6px 6px 0; }"
            + ".warn { background: " + bgMed + "; border-left: 3px solid " + warning + "; "
            + "  padding: 8px 12px; margin: 8px 0; border-radius: 0 6px 6px 0; }"
            + "</style></head><body>"

            // ---- TITRE ----
            + "<h1>Robi 2026 — Guide d'utilisation</h1>"
            + "<p style='color:" + textSec + "'>Application de dessin collaboratif avec "
            + "synchronisation client-serveur en temps reel.</p>"

            // ---- CONNEXION ----
            + "<h2>1. Connexion au serveur</h2>"
            + "<p>La barre de connexion en haut permet de se connecter a un serveur Robi :</p>"
            + "<table>"
            + "<tr><th>Champ</th><th>Description</th></tr>"
            + "<tr><td><b>Hote</b></td><td>Adresse du serveur (par defaut <code>localhost</code>)</td></tr>"
            + "<tr><td><b>Port</b></td><td>Port du serveur (par defaut <code>8888</code>)</td></tr>"
            + "<tr><td><b>Connecter / Deconnecter</b></td><td>Bascule la connexion</td></tr>"
            + "</table>"
            + "<div class='tip'><b>Mode local :</b> Sans connexion, toutes les commandes "
            + "s'executent localement. Ideal pour tester.</div>"

            // ---- TOOLBAR : CREATION ----
            + "<h2>2. Creer des elements</h2>"
            + "<p>La barre d'outils (ligne 1) permet de creer des formes et images :</p>"
            + "<table>"
            + "<tr><th>Bouton</th><th>Action</th></tr>"
            + "<tr><td><b>&#9645; Rect</b></td><td>Cree un rectangle dans l'element selectionne</td></tr>"
            + "<tr><td><b>&#9675; Oval</b></td><td>Cree un ovale</td></tr>"
            + "<tr><td><b>A Label</b></td><td>Cree un texte (saisie du contenu)</td></tr>"
            + "<tr><td><b>&#128126; Alien</b></td><td>Ajoute l'image <code>alien.gif</code></td></tr>"
            + "<tr><td><b>&#128165; Boom</b></td><td>Ajoute l'image <code>explosion.gif</code></td></tr>"
            + "</table>"
            + "<div class='tip'><b>Selecteur \"Cible\" :</b> Choisissez d'abord l'element parent "
            + "dans la liste deroulante. Par defaut c'est <code>space</code> (la scene racine).</div>"

            // ---- TOOLBAR : MODIFICATION ----
            + "<h2>3. Modifier des elements</h2>"
            + "<table>"
            + "<tr><th>Bouton</th><th>Action</th></tr>"
            + "<tr><td><b>&#10534; Taille</b></td><td>Redimensionne l'element selectionne "
            + "(saisir <code>largeur hauteur</code>)</td></tr>"
            + "<tr><td><b>Couleur</b></td><td>Change la couleur de l'element selectionne "
            + "(liste deroulante)</td></tr>"
            + "<tr><td><b>&#10006; Suppr.</b></td><td>Supprime l'element selectionne</td></tr>"
            + "</table>"

            // ---- TOOLBAR : DEPLACEMENT ----
            + "<h2>4. Deplacer des elements</h2>"
            + "<p>Ligne 2 de la toolbar, les fleches deplacent l'element selectionne "
            + "de <b>10 pixels</b> par clic :</p>"
            + "<table>"
            + "<tr><th>Bouton</th><th>Direction</th><th>Raccourci clavier</th></tr>"
            + "<tr><td><b>&#9650;</b></td><td>Haut</td>"
            + "<td><span class='kbd'>Alt + &#8593;</span></td></tr>"
            + "<tr><td><b>&#9660;</b></td><td>Bas</td>"
            + "<td><span class='kbd'>Alt + &#8595;</span></td></tr>"
            + "<tr><td><b>&#9664;</b></td><td>Gauche</td>"
            + "<td><span class='kbd'>Alt + &#8592;</span></td></tr>"
            + "<tr><td><b>&#9654;</b></td><td>Droite</td>"
            + "<td><span class='kbd'>Alt + &#8594;</span></td></tr>"
            + "</table>"

            // ---- BOTS ----
            + "<h2>5. Bots (automates)</h2>"
            + "<p>Les bots animent automatiquement les elements avec des rebonds "
            + "et changements de couleur :</p>"
            + "<table>"
            + "<tr><th>Bouton</th><th>Action</th></tr>"
            + "<tr><td><b>&#9881; Bot</b></td><td>Transforme l'element selectionne en bot. "
            + "Saisir la vitesse <code>dx dy</code> (ex: <code>5 5</code>)</td></tr>"
            + "<tr><td><b>&#9654; Start</b></td><td>Demarre tous les bots</td></tr>"
            + "<tr><td><b>&#9632; Stop</b></td><td>Arrete tous les bots</td></tr>"
            + "</table>"
            + "<div class='warn'><b>En mode connecte :</b> les bots sont geres par le serveur. "
            + "Le serveur calcule les deplacements et collisions, puis transmet les positions "
            + "a tous les clients pour garantir la synchronisation.</div>"

            // ---- SCENE ----
            + "<h2>6. Scene : sauvegarde et chargement</h2>"
            + "<table>"
            + "<tr><th>Bouton</th><th>Action</th></tr>"
            + "<tr><td><b>&#11015; Sauver</b></td><td>Exporte la scene en fichier JSON</td></tr>"
            + "<tr><td><b>&#11014; Charger</b></td><td>Importe une scene depuis un fichier JSON</td></tr>"
            + "<tr><td><b>&#8984; Capture</b></td><td>Capture le canvas en image PNG "
            + "(avec option d'enregistrement)</td></tr>"
            + "<tr><td><b>&#10006; Clear</b></td><td>Efface tous les elements de la scene</td></tr>"
            + "</table>"

            // ---- CONSOLE S-EXPRESSION ----
            + "<h2>7. Console S-Expression</h2>"
            + "<p>Le panneau de droite permet d'ecrire et executer des S-Expressions manuellement :</p>"
            + "<div class='tip'><b>Raccourci :</b> <span class='kbd'>Ctrl + Entree</span> "
            + "pour executer la commande.</div>"

            + "<h3>Exemples de commandes</h3>"
            + "<pre>"
            + "// Creer un rectangle et le colorer\n"
            + "(space add r1 (Rect new))\n"
            + "(space.r1 setColor red)\n"
            + "(space.r1 setDim 80 40)\n"
            + "(space.r1 translate 100 200)\n"
            + "\n"
            + "// Creer un ovale\n"
            + "(space add o1 (Oval new))\n"
            + "(space.o1 setColor cyan)\n"
            + "\n"
            + "// Creer un label\n"
            + "(space add txt (Label new Hello))\n"
            + "(space.txt setColor white)\n"
            + "\n"
            + "// Ajouter une image\n"
            + "(space add a1 (Image new alien.gif))\n"
            + "(space.a1 translate 50 50)\n"
            + "\n"
            + "// Transformer en bot et demarrer\n"
            + "(space addBot space.r1 5 3)\n"
            + "(space startBots)\n"
            + "\n"
            + "// Changer la couleur et dimension du canvas\n"
            + "(space setColor black)\n"
            + "(space setDim 800 600)"
            + "</pre>"

            + "<h3>Commandes disponibles</h3>"
            + "<table>"
            + "<tr><th>Commande</th><th>Description</th></tr>"
            + "<tr><td><code>add nom (Type new)</code></td><td>Ajoute un element enfant</td></tr>"
            + "<tr><td><code>del nom</code></td><td>Supprime un element enfant</td></tr>"
            + "<tr><td><code>setColor couleur</code></td><td>Change la couleur</td></tr>"
            + "<tr><td><code>setDim l h</code></td><td>Change les dimensions</td></tr>"
            + "<tr><td><code>translate dx dy</code></td><td>Deplace l'element</td></tr>"
            + "<tr><td><code>addBot ref dx dy</code></td><td>Cree un bot sur un element</td></tr>"
            + "<tr><td><code>delBot ref</code></td><td>Supprime un bot</td></tr>"
            + "<tr><td><code>startBots</code></td><td>Demarre les bots</td></tr>"
            + "<tr><td><code>stopBots</code></td><td>Arrete les bots</td></tr>"
            + "<tr><td><code>clear</code></td><td>Vide la scene</td></tr>"
            + "<tr><td><code>sleep ms</code></td><td>Pause en millisecondes</td></tr>"
            + "<tr><td><code>addScript nom (cmds)</code></td><td>Attache un script</td></tr>"
            + "<tr><td><code>addVar nom valeur</code></td><td>Declare une variable</td></tr>"
            + "<tr><td><code>if (cond) (alors) (sinon)</code></td><td>Condition</td></tr>"
            + "<tr><td><code>while (cond) (corps)</code></td><td>Boucle</td></tr>"
            + "<tr><td><code>repeat n (corps)</code></td><td>Repete n fois</td></tr>"
            + "</table>"

            // ---- MULTI-CLIENT ----
            + "<h2>8. Mode multi-client</h2>"
            + "<p>Quand plusieurs clients sont connectes au meme serveur :</p>"
            + "<ul>"
            + "<li><b>Synchronisation automatique :</b> un nouveau client recoit "
            + "l'etat actuel de la scene (positions, couleurs, dimensions)</li>"
            + "<li><b>Diffusion en temps reel :</b> chaque action d'un client est "
            + "automatiquement transmise a tous les autres</li>"
            + "<li><b>Bots centralises :</b> le serveur gere les bots pour garantir "
            + "que tous les clients voient les memes positions et couleurs</li>"
            + "<li><b>Noms uniques :</b> chaque client genere des noms avec un "
            + "prefixe unique pour eviter les conflits</li>"
            + "</ul>"

            // ---- RACCOURCIS ----
            + "<h2>9. Raccourcis clavier</h2>"
            + "<table>"
            + "<tr><th>Raccourci</th><th>Action</th></tr>"
            + "<tr><td><span class='kbd'>Ctrl + Entree</span></td>"
            + "<td>Executer le script dans la console</td></tr>"
            + "<tr><td><span class='kbd'>Alt + &#8593;&#8595;&#8592;&#8594;</span></td>"
            + "<td>Deplacer l'element selectionne</td></tr>"
            + "</table>"

            + "<br><p style='color:" + textSec + "; text-align:center; font-size:11px;'>"
            + "Robi 2026 — Projet L3 CDA</p>"

            + "</body></html>";
    }

    /**
     * Convertit une couleur AWT en code hexadecimal CSS.
     *
     * @param c la couleur
     * @return le code hex (ex: #1e1e24)
     */
    private static String colorToHex(Color c) {
        return String.format("#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue());
    }
}
