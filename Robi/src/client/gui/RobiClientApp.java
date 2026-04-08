package client.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import client.RobiClient;
import core.Environment;
import core.Reference;
import core.RobiInterpreter;
import core.commands.AddBot;
import core.commands.AddElement;
import core.commands.AddScript;
import core.commands.AddVar;
import core.commands.And;
import core.commands.Clear;
import core.commands.DelBot;
import core.commands.DelElement;
import core.commands.If;
import core.commands.NewElement;
import core.commands.NewImage;
import core.commands.NewString;
import core.commands.Not;
import core.commands.Or;
import core.commands.Repeat;
import core.commands.SetColor;
import core.commands.SetDim;
import core.commands.Sleep;
import core.commands.StartBots;
import core.commands.StopBots;
import core.commands.While;
import core.statemachine.RobibotManager;
import graphicLayer.GImage;
import graphicLayer.GOval;
import graphicLayer.GRect;
import graphicLayer.GSpace;
import graphicLayer.GString;
import stree.parser.SNode;

/**
 * Application cliente Robi avec interface graphique complete.
 * Fournit un selecteur d'element actif, des fleches directionnelles,
 * un color picker, une console avec historique et un mode connecte
 * ou deconnecte (execution locale).
 */
public class RobiClientApp extends JFrame implements RobiClient.ConnectionListener {

    private final RobiClient client;
    private final RobiInterpreter interpreter;
    private final GSpace space;
    private final RobibotManager botManager;

    private final ConnectionPanel connectionPanel;
    private final ToolbarPanel toolbarPanel;
    private final ScriptPanel scriptPanel;
    private final StatusBar statusBar;

    private int rectCount = 0;
    private int ovalCount = 0;
    private int labelCount = 0;
    private int imageCount = 0;

    /** Pas de deplacement pour les fleches (en pixels). */
    private static final int MOVE_STEP = 10;

    /**
     * Construit et affiche l'application cliente Robi.
     */
    public RobiClientApp() {
        super("Robi Client");
        this.client = new RobiClient();
        this.client.addConnectionListener(this);

        this.interpreter = new RobiInterpreter();
        this.space = new GSpace("Robi - Rendu Client", new Dimension(500, 400));
        this.botManager = new RobibotManager(interpreter);
        setupLocalInterpreter();

        // Ecoute des ticks de bots recus du serveur
        this.client.addBotTickListener(commands ->
            SwingUtilities.invokeLater(() -> {
                for (String cmd : commands) {
                    interpreter.oneShot(cmd);
                }
            })
        );

        // Composants GUI
        this.connectionPanel = new ConnectionPanel();
        this.toolbarPanel = new ToolbarPanel();
        this.scriptPanel = new ScriptPanel();
        this.statusBar = new StatusBar();

        // Rediriger les print de variables vers l'historique de l'IHM
        interpreter.setOutputListener(message ->
            SwingUtilities.invokeLater(() -> scriptPanel.appendInfo(message))
        );

        setupUI();
        setupActions();
        setupKeyBindings();

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1100, 700);
        setMinimumSize(new Dimension(800, 500));
        setLocationRelativeTo(null);
        getContentPane().setBackground(Theme.BG_DARK);
        setVisible(true);
    }

    private void setupLocalInterpreter() {
        Environment env = interpreter.getEnvironment();

        Reference spaceRef = new Reference(space);
        spaceRef.addCommand("setColor", new SetColor(interpreter));
        spaceRef.addCommand("sleep", new Sleep(interpreter));
        spaceRef.addCommand("setDim", new SetDim(interpreter));
        spaceRef.addCommand("add", new AddElement(env, interpreter));
        spaceRef.addCommand("del", new DelElement(env));
        spaceRef.addCommand("clear", new Clear());
        spaceRef.addCommand("addScript", new AddScript(interpreter));
        spaceRef.addCommand("repeat", new Repeat(interpreter));
        spaceRef.addCommand("if", new If(interpreter));
        spaceRef.addCommand("while", new While(interpreter));
        spaceRef.addCommand("addVar", new AddVar(env, interpreter));
        spaceRef.addCommand("and", new And(interpreter));
        spaceRef.addCommand("or", new Or(interpreter));
        spaceRef.addCommand("not", new Not(interpreter));
        spaceRef.addCommand("addBot", new AddBot(env, interpreter, botManager, space));
        spaceRef.addCommand("delBot", new DelBot(botManager));
        spaceRef.addCommand("startBots", new StartBots(botManager));
        spaceRef.addCommand("stopBots", new StopBots(botManager));

        Reference rectClassRef = new Reference(GRect.class);
        Reference ovalClassRef = new Reference(GOval.class);
        Reference imageClassRef = new Reference(GImage.class);
        Reference stringClassRef = new Reference(GString.class);

        rectClassRef.addCommand("new", new NewElement(env, interpreter));
        ovalClassRef.addCommand("new", new NewElement(env, interpreter));
        imageClassRef.addCommand("new", new NewImage(interpreter));
        stringClassRef.addCommand("new", new NewString(interpreter));

        env.addReference("space", spaceRef);
        env.addReference("Rect", rectClassRef);
        env.addReference("Oval", ovalClassRef);
        env.addReference("Image", imageClassRef);
        env.addReference("Label", stringClassRef);
    }

    private void setupUI() {
        setLayout(new BorderLayout(0, 0));

        // Nord : Connexion + Toolbar
        JPanel northPanel = new JPanel();
        northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.Y_AXIS));
        northPanel.add(connectionPanel);
        northPanel.add(toolbarPanel);
        add(northPanel, BorderLayout.NORTH);

        // Centre : Canvas + Script
        JPanel canvasWrapper = new JPanel(new BorderLayout());
        canvasWrapper.setBackground(Theme.BG_DARK);
        canvasWrapper.setBorder(Theme.padding(8, 8, 8, 0));

        JLabel canvasTitle = new JLabel("  \u25A3 Canvas (rendu client)");
        canvasTitle.setForeground(Theme.TEXT_PRIMARY);
        canvasTitle.setFont(Theme.FONT_TITLE);
        canvasTitle.setBorder(Theme.padding(0, 0, 6, 0));
        canvasTitle.setOpaque(true);
        canvasTitle.setBackground(Theme.BG_DARK);
        canvasWrapper.add(canvasTitle, BorderLayout.NORTH);

        space.setBackground(Color.WHITE);
        JScrollPane canvasScroll = new JScrollPane(space);
        canvasScroll.setBorder(Theme.border());
        canvasScroll.getViewport().setBackground(Color.WHITE);
        canvasWrapper.add(canvasScroll, BorderLayout.CENTER);

        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                canvasWrapper, scriptPanel);
        mainSplit.setDividerLocation(600);
        mainSplit.setDividerSize(5);
        mainSplit.setBackground(Theme.BG_DARK);
        mainSplit.setBorder(null);
        add(mainSplit, BorderLayout.CENTER);

        // Sud : Status bar
        add(statusBar, BorderLayout.SOUTH);
    }

    private void setupActions() {
        // Connexion
        connectionPanel.getConnectButton().addActionListener(e -> toggleConnection());

        // Execution du script
        scriptPanel.getExecuteButton().addActionListener(e -> executeCurrentScript());

        // Boutons de creation
        toolbarPanel.getBtnRect().addActionListener(e -> {
            String name = "r" + (++rectCount);
            String fullName = "space." + name;
            executeScript("(space add " + name + " (Rect new))");
            toolbarPanel.addElement(fullName);
        });

        toolbarPanel.getBtnOval().addActionListener(e -> {
            String name = "o" + (++ovalCount);
            String fullName = "space." + name;
            executeScript("(space add " + name + " (Oval new))");
            toolbarPanel.addElement(fullName);
        });

        toolbarPanel.getBtnLabel().addActionListener(e -> {
            String text = JOptionPane.showInputDialog(this,
                    "Texte du label :", "Nouveau label", JOptionPane.PLAIN_MESSAGE);
            if (text != null && !text.isEmpty()) {
                String name = "lbl" + (++labelCount);
                String fullName = "space." + name;
                executeScript("(space add " + name + " (Label new " + text + "))");
                toolbarPanel.addElement(fullName);
            }
        });

        // Boutons d'images
        toolbarPanel.getBtnAlien().addActionListener(e -> {
            String name = "img" + (++imageCount);
            String fullName = "space." + name;
            executeScript("(space add " + name + " (Image new alien.gif))");
            toolbarPanel.addElement(fullName);
        });

        toolbarPanel.getBtnExplosion().addActionListener(e -> {
            String name = "img" + (++imageCount);
            String fullName = "space." + name;
            executeScript("(space add " + name + " (Image new explosion.gif))");
            toolbarPanel.addElement(fullName);
        });

        // Supprimer l'element selectionne
        toolbarPanel.getBtnDelete().addActionListener(e -> {
            String target = toolbarPanel.getSelectedElement();
            if ("space".equals(target)) {
                scriptPanel.appendError("Impossible de supprimer le space.");
                return;
            }
            int lastDot = target.lastIndexOf('.');
            if (lastDot > 0) {
                String parent = target.substring(0, lastDot);
                String child = target.substring(lastDot + 1);
                executeScript("(" + parent + " del " + child + ")");
                toolbarPanel.removeElement(target);
            }
        });

        // Clear
        toolbarPanel.getBtnClear().addActionListener(e -> {
            executeScript("(space clear)");
            toolbarPanel.clearElements();
        });

        // Screenshot
        toolbarPanel.getBtnScreenshot().addActionListener(e -> requestScreenshot());

        // Sauvegarde / Chargement
        toolbarPanel.getBtnSave().addActionListener(e -> saveScene());
        toolbarPanel.getBtnLoad().addActionListener(e -> loadScene());

        // Selecteur de couleur -> applique sur l'element selectionne
        toolbarPanel.getColorSelector().addActionListener(e -> {
            String target = toolbarPanel.getSelectedElement();
            String colorName = toolbarPanel.getSelectedColor();
            executeScript("(" + target + " setColor " + colorName + ")");
        });

        // Fleches directionnelles -> deplace l'element selectionne
        toolbarPanel.getBtnUp().addActionListener(e -> moveSelectedElement(0, -MOVE_STEP));
        toolbarPanel.getBtnDown().addActionListener(e -> moveSelectedElement(0, MOVE_STEP));
        toolbarPanel.getBtnLeft().addActionListener(e -> moveSelectedElement(-MOVE_STEP, 0));
        toolbarPanel.getBtnRight().addActionListener(e -> moveSelectedElement(MOVE_STEP, 0));

        // Bots
        toolbarPanel.getBtnAddBot().addActionListener(e -> {
            String target = toolbarPanel.getSelectedElement();
            if ("space".equals(target)) {
                scriptPanel.appendError("S\u00e9lectionnez un \u00e9l\u00e9ment pour en faire un bot.");
                return;
            }
            String input = JOptionPane.showInputDialog(this,
                    "Vitesse (dx dy) :", "5 5");
            if (input != null && !input.isEmpty()) {
                String[] parts = input.trim().split("\\s+");
                String dx = parts.length >= 1 ? parts[0] : "5";
                String dy = parts.length >= 2 ? parts[1] : "5";
                executeScript("(space addBot " + target + " " + dx + " " + dy + ")");
                scriptPanel.appendInfo("Bot cr\u00e9\u00e9 sur " + target);
            }
        });

        toolbarPanel.getBtnStartBots().addActionListener(e ->
            executeScript("(space startBots)"));

        toolbarPanel.getBtnStopBots().addActionListener(e ->
            executeScript("(space stopBots)"));
    }

    private void setupKeyBindings() {
        // Ctrl+Entree pour executer le script
        JTextPane input = scriptPanel.getScriptInput();
        input.getInputMap(JComponent.WHEN_FOCUSED).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.CTRL_DOWN_MASK), "execute");
        input.getActionMap().put("execute", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                executeCurrentScript();
            }
        });

        // Fleches directionnelles globales (quand le focus est sur la fenetre)
        JRootPane root = getRootPane();
        InputMap im = root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = root.getActionMap();

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.ALT_DOWN_MASK), "moveUp");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.ALT_DOWN_MASK), "moveDown");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.ALT_DOWN_MASK), "moveLeft");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.ALT_DOWN_MASK), "moveRight");

        am.put("moveUp", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                moveSelectedElement(0, -MOVE_STEP);
            }
        });
        am.put("moveDown", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                moveSelectedElement(0, MOVE_STEP);
            }
        });
        am.put("moveLeft", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                moveSelectedElement(-MOVE_STEP, 0);
            }
        });
        am.put("moveRight", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                moveSelectedElement(MOVE_STEP, 0);
            }
        });
    }

    /**
     * Deplace l'element actuellement selectionne dans le selecteur.
     *
     * @param dx le deplacement horizontal en pixels
     * @param dy le deplacement vertical en pixels
     */
    private void moveSelectedElement(int dx, int dy) {
        String target = toolbarPanel.getSelectedElement();
        if ("space".equals(target)) {
            scriptPanel.appendError("Impossible de d\u00e9placer le space. S\u00e9lectionnez un \u00e9l\u00e9ment.");
            return;
        }
        executeScript("(" + target + " translate " + dx + " " + dy + ")");
    }

    private void toggleConnection() {
        if (client.isConnected()) {
            client.disconnect();
        } else {
            String host = connectionPanel.getHost();
            int port = connectionPanel.getPort();
            statusBar.setStatus("Connexion en cours...", Theme.WARNING);

            new Thread(() -> {
                try {
                    client.connect(host, port);
                } catch (IOException ex) {
                    SwingUtilities.invokeLater(() -> {
                        connectionPanel.setConnected(false);
                        statusBar.setStatus("\u00c9chec : " + ex.getMessage(), Theme.ERROR);
                        scriptPanel.appendError("Connexion impossible : " + ex.getMessage());
                    });
                }
            }).start();
        }
    }

    private void executeCurrentScript() {
        String script = scriptPanel.getScript();
        if (script.isEmpty()) {
            return;
        }
        executeScript(script);
        scriptPanel.clearInput();
    }

    /**
     * Envoie un script au serveur (si connecte) et execute les SNodes recus
     * localement. Si non connecte, execute directement en local.
     *
     * @param script le script S-Expression a executer
     */
    private void executeScript(String script) {
        statusBar.setStatus("Ex\u00e9cution...", Theme.WARNING);

        new Thread(() -> {
            try {
                if (client.isConnected()) {
                    List<SNode> nodes = client.sendScript(script);
                    SwingUtilities.invokeAndWait(() -> {
                        for (SNode node : nodes) {
                            interpreter.executeNode(node);
                        }
                    });
                } else {
                    SwingUtilities.invokeAndWait(() -> interpreter.oneShot(script));
                }

                SwingUtilities.invokeLater(() -> {
                    scriptPanel.appendSuccess(script);
                    statusBar.setStatus("Pr\u00eat", Theme.TEXT_SECONDARY);
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    scriptPanel.appendError(ex.getMessage());
                    statusBar.setStatus("Erreur : " + ex.getMessage(), Theme.ERROR);
                });
            }
        }).start();
    }

    private void requestScreenshot() {
        if (!client.isConnected()) {
            scriptPanel.appendError("Il faut \u00eatre connect\u00e9 au serveur pour demander une capture.");
            return;
        }

        statusBar.setStatus("Capture en cours...", Theme.WARNING);
        new Thread(() -> {
            try {
                String base64 = client.requestScreenshot();
                byte[] imageBytes = Base64.getDecoder().decode(base64);
                BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));

                SwingUtilities.invokeLater(() -> {
                    JFrame frame = new JFrame("Capture serveur");
                    frame.setSize(image.getWidth() + 20, image.getHeight() + 50);
                    frame.setLocationRelativeTo(this);
                    JLabel label = new JLabel(new ImageIcon(image));
                    label.setBorder(Theme.padding(5, 5, 5, 5));
                    frame.getContentPane().setBackground(Theme.BG_DARK);
                    frame.add(new JScrollPane(label));
                    frame.setVisible(true);
                    scriptPanel.appendInfo("Capture du serveur re\u00e7ue et affich\u00e9e.");
                    statusBar.setStatus("Pr\u00eat", Theme.TEXT_SECONDARY);
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    scriptPanel.appendError("Screenshot : " + ex.getMessage());
                    statusBar.setStatus("Erreur screenshot", Theme.ERROR);
                });
            }
        }).start();
    }

    private void saveScene() {
        new Thread(() -> {
            try {
                String json;
                if (client.isConnected()) {
                    json = client.requestSave();
                } else {
                    json = core.persistence.SceneSaver.saveToJson(
                            interpreter.getEnvironment(), space);
                }

                SwingUtilities.invokeLater(() -> {
                    JFileChooser fc = new JFileChooser();
                    fc.setDialogTitle("Sauvegarder la sc\u00e8ne");
                    fc.setSelectedFile(new File("scene.json"));
                    if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                        try {
                            Files.writeString(fc.getSelectedFile().toPath(), json);
                            scriptPanel.appendInfo("Sc\u00e8ne sauvegard\u00e9e : "
                                    + fc.getSelectedFile().getName());
                            statusBar.setStatus("Sc\u00e8ne sauvegard\u00e9e", Theme.SUCCESS);
                        } catch (IOException ex) {
                            scriptPanel.appendError("Erreur \u00e9criture : " + ex.getMessage());
                        }
                    }
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() ->
                    scriptPanel.appendError("Erreur sauvegarde : " + ex.getMessage()));
            }
        }).start();
    }

    private void loadScene() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Charger une sc\u00e8ne");
        if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        new Thread(() -> {
            try {
                String json = Files.readString(fc.getSelectedFile().toPath());

                if (client.isConnected()) {
                    List<SNode> nodes = client.requestLoad(json);
                    SwingUtilities.invokeAndWait(() -> {
                        toolbarPanel.clearElements();
                        for (SNode node : nodes) {
                            interpreter.executeNode(node);
                        }
                        rebuildElementSelector();
                    });
                } else {
                    java.util.List<String> commands =
                            core.persistence.SceneLoader.jsonToSExpressions(json);
                    SwingUtilities.invokeAndWait(() -> {
                        toolbarPanel.clearElements();
                        for (String cmd : commands) {
                            interpreter.oneShot(cmd);
                        }
                        rebuildElementSelector();
                    });
                }

                SwingUtilities.invokeLater(() -> {
                    scriptPanel.appendInfo("Sc\u00e8ne charg\u00e9e : "
                            + fc.getSelectedFile().getName());
                    statusBar.setStatus("Sc\u00e8ne charg\u00e9e", Theme.SUCCESS);
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() ->
                    scriptPanel.appendError("Erreur chargement : " + ex.getMessage()));
            }
        }).start();
    }

    /**
     * Reconstruit le selecteur d'elements a partir de l'environnement courant.
     */
    private void rebuildElementSelector() {
        toolbarPanel.clearElements();
        for (String name : interpreter.getEnvironment().getAll().keySet()) {
            if (!name.equals("space") && !name.equals("Rect") && !name.equals("Oval")
                    && !name.equals("Image") && !name.equals("Label")) {
                toolbarPanel.addElement(name);
            }
        }
    }

    @Override
    public void onConnected(String host, int port) {
        SwingUtilities.invokeLater(() -> {
            connectionPanel.setConnected(true);
            statusBar.setStatus("Connect\u00e9 \u00e0 " + host + ":" + port, Theme.SUCCESS);
            scriptPanel.appendInfo("Connect\u00e9 au serveur " + host + ":" + port);
        });
    }

    @Override
    public void onDisconnected() {
        SwingUtilities.invokeLater(() -> {
            connectionPanel.setConnected(false);
            statusBar.setStatus("D\u00e9connect\u00e9", Theme.TEXT_SECONDARY);
            scriptPanel.appendInfo("D\u00e9connect\u00e9 du serveur.");
        });
    }

    /**
     * Point d'entree de l'application cliente Robi.
     *
     * @param args les arguments de la ligne de commande (non utilises)
     */
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        SwingUtilities.invokeLater(RobiClientApp::new);
    }
}
