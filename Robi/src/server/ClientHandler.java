package server;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;

import core.RobiInterpreter;
import core.persistence.SceneLoader;
import core.persistence.SceneSaver;
import core.statemachine.RobibotManager;
import graphicLayer.GSpace;
import protocol.RobiProtocol;
import protocol.SNodeSerializer;
import stree.parser.SNode;
import stree.parser.SParser;

/**
 * Gere la communication avec un client connecte.
 * Recoit les S-Expressions, les parse, les execute cote serveur,
 * et renvoie les SNodes serialises au client.
 */
public class ClientHandler implements Runnable {

    private final Socket socket;
    private final RobiInterpreter interpreter;
    private final GSpace space;
    private final RobibotManager botManager;
    private PrintWriter clientOut;

    /**
     * Construit un gestionnaire pour un client connecte.
     *
     * @param socket le socket de la connexion client
     * @param interpreter l'interpreteur Robi partage
     * @param space l'espace graphique du serveur
     * @param botManager le gestionnaire de bots
     */
    public ClientHandler(Socket socket, RobiInterpreter interpreter,
                         GSpace space, RobibotManager botManager) {
        this.socket = socket;
        this.interpreter = interpreter;
        this.space = space;
        this.botManager = botManager;
    }

    /**
     * Boucle principale de lecture des messages du client.
     * Ecoute en continu les messages entrants et les dispatche au traitement.
     */
    @Override
    public void run() {
        // Listener pour recevoir les ticks des bots et les transmettre au client
        RobibotManager.BotTickListener tickListener = commands -> {
            if (clientOut != null) {
                synchronized (clientOut) {
                    String joined = String.join(";;", commands);
                    clientOut.println(RobiProtocol.PREFIX_BOT_TICK + joined);
                    clientOut.println(RobiProtocol.END_OF_MESSAGE);
                }
            }
        };

        try (
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true)
        ) {
            this.clientOut = out;
            botManager.addTickListener(tickListener);

            String line;
            while ((line = in.readLine()) != null) {
                // Lecture du message complet (multi-lignes jusqu'au EOM)
                StringBuilder messageBuilder = new StringBuilder();
                if (line.equals(RobiProtocol.END_OF_MESSAGE)) {
                    continue;
                }

                messageBuilder.append(line);
                while (!(line = in.readLine()).equals(RobiProtocol.END_OF_MESSAGE)) {
                    messageBuilder.append("\n").append(line);
                }

                String message = messageBuilder.toString();
                processMessage(message, out);
            }
        } catch (IOException e) {
            System.out.println("Client deconnecte : " + socket.getRemoteSocketAddress());
        } finally {
            botManager.removeTickListener(tickListener);
            this.clientOut = null;
            try {
                socket.close();
            } catch (IOException ignored) {
            }
        }
    }

    /**
     * Dispatche un message recu vers le gestionnaire approprie selon son prefixe.
     *
     * @param message le message complet recu du client
     * @param out le flux de sortie vers le client
     */
    private void processMessage(String message, PrintWriter out) {
        if (message.startsWith(RobiProtocol.PREFIX_SCRIPT)) {
            handleScript(message.substring(RobiProtocol.PREFIX_SCRIPT.length()), out);
        } else if (message.equals(RobiProtocol.PREFIX_SCREENSHOT)) {
            handleScreenshot(out);
        } else if (message.equals(RobiProtocol.PREFIX_SAVE)) {
            handleSave(out);
        } else if (message.startsWith(RobiProtocol.PREFIX_LOAD)) {
            handleLoad(message.substring(RobiProtocol.PREFIX_LOAD.length()), out);
        } else {
            sendError(out, "Message non reconnu : " + message);
        }
    }

    /**
     * Traite un script S-Expression : parse, execute cote serveur, et renvoie
     * les SNodes serialises au client.
     *
     * @param script le texte brut de la S-Expression
     * @param out le flux de sortie vers le client
     */
    private void handleScript(String script, PrintWriter out) {
        try {
            // 1. Parser la S-Expression
            SParser<SNode> parser = new SParser<>();
            List<SNode> nodes = parser.parse(script);

            // 2. Executer cote serveur (rendu serveur) sur l'EDT
            SwingUtilities.invokeAndWait(() -> {
                for (SNode node : nodes) {
                    interpreter.executeNode(node);
                }
            });

            // 3. Serialiser les SNodes et renvoyer au client
            String serialized = SNodeSerializer.serialize(nodes);
            out.println(RobiProtocol.PREFIX_NODES + serialized);
            out.println(RobiProtocol.END_OF_MESSAGE);

            System.out.println("Script execute : " + script.trim());
        } catch (Exception e) {
            sendError(out, "Erreur d'execution : " + e.getMessage());
        }
    }

    /**
     * Capture le contenu graphique du GSpace et l'envoie au client
     * sous forme d'image PNG encodee en Base64.
     *
     * @param out le flux de sortie vers le client
     */
    private void handleScreenshot(PrintWriter out) {
        try {
            // Capture du GSpace en BufferedImage
            int width = space.getWidth();
            int height = space.getHeight();
            if (width <= 0 || height <= 0) {
                sendError(out, "Le space n'a pas encore de dimensions valides.");
                return;
            }

            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = image.createGraphics();

            // Peindre le space sur l'image (sur l'EDT)
            SwingUtilities.invokeAndWait(() -> space.paint(g2d));
            g2d.dispose();

            // Encoder en Base64
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            String base64 = Base64.getEncoder().encodeToString(baos.toByteArray());

            out.println(RobiProtocol.PREFIX_IMAGE + base64);
            out.println(RobiProtocol.END_OF_MESSAGE);

            System.out.println("Screenshot envoye au client.");
        } catch (Exception e) {
            sendError(out, "Erreur screenshot : " + e.getMessage());
        }
    }

    /**
     * Sauvegarde la scene courante en JSON et l'envoie au client.
     *
     * @param out le flux de sortie vers le client
     */
    private void handleSave(PrintWriter out) {
        try {
            String json = SceneSaver.saveToJson(interpreter.getEnvironment(), space);
            out.println(RobiProtocol.PREFIX_STATE + json);
            out.println(RobiProtocol.END_OF_MESSAGE);
            System.out.println("Scene sauvegardee et envoyee au client.");
        } catch (Exception e) {
            sendError(out, "Erreur sauvegarde : " + e.getMessage());
        }
    }

    /**
     * Charge une scene a partir du JSON recu, genere les S-Expressions
     * correspondantes, les execute cote serveur, et renvoie les SNodes au client.
     *
     * @param json le JSON de la scene a charger
     * @param out le flux de sortie vers le client
     */
    private void handleLoad(String json, PrintWriter out) {
        try {
            // 1. Generer les S-Expressions a partir du JSON
            List<String> commands = SceneLoader.jsonToSExpressions(json);

            // 2. Executer cote serveur
            SParser<SNode> parser = new SParser<>();
            List<SNode> allNodes = new ArrayList<>();

            for (String cmd : commands) {
                List<SNode> nodes = parser.parse(cmd);
                allNodes.addAll(nodes);
            }

            SwingUtilities.invokeAndWait(() -> {
                for (SNode node : allNodes) {
                    interpreter.executeNode(node);
                }
            });

            // 3. Renvoyer les SNodes au client pour execution locale
            String serialized = SNodeSerializer.serialize(allNodes);
            out.println(RobiProtocol.PREFIX_NODES + serialized);
            out.println(RobiProtocol.END_OF_MESSAGE);

            System.out.println("Scene chargee depuis JSON (" + commands.size() + " commandes).");
        } catch (Exception e) {
            sendError(out, "Erreur chargement : " + e.getMessage());
        }
    }

    /**
     * Envoie un message d'erreur au client.
     *
     * @param out le flux de sortie vers le client
     * @param errorMessage le message d'erreur a envoyer
     */
    private void sendError(PrintWriter out, String errorMessage) {
        out.println(RobiProtocol.PREFIX_ERROR + errorMessage);
        out.println(RobiProtocol.END_OF_MESSAGE);
        System.err.println("Erreur envoyee au client : " + errorMessage);
    }
}
