package server;

import java.awt.Dimension;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import core.Environment;
import core.Reference;
import core.RobiInterpreter;
import core.commands.*;
import core.statemachine.RobibotManager;
import graphicLayer.*;

/**
 * Serveur Robi : accepte des connexions clients, parse les S-Expressions,
 * les execute localement (rendu serveur) et renvoie les SNodes serialises.
 *
 * <p>Le serveur maintient son propre rendu graphique pour la comparaison.
 * Chaque client est gere dans un thread separe.
 */
public class RobiServer {

    private final int port;
    private final RobiInterpreter interpreter;
    private final GSpace space;
    private final RobibotManager botManager;
    private boolean running;
    private final List<String> commandHistory = Collections.synchronizedList(new ArrayList<>());
    private final List<ClientHandler> clients = new CopyOnWriteArrayList<>();

    /**
     * Construit un serveur Robi sur le port specifie.
     *
     * @param port le port d'ecoute du serveur
     */
    public RobiServer(int port) {
        this.port = port;
        this.interpreter = new RobiInterpreter();
        this.space = new GSpace("Robi Serveur", new Dimension(500, 500));
        this.botManager = new RobibotManager(interpreter);
        setupInterpreter();
    }

    /**
     * Configure l'interpreteur avec l'ensemble des commandes disponibles
     * et les references aux classes graphiques.
     */
    private void setupInterpreter() {
        Environment env = interpreter.getEnvironment();

        space.open();

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

    /**
     * Demarre le serveur et accepte les connexions clients en boucle.
     * Chaque client est traite dans un thread dedie.
     */
    public void start() {
        running = true;
        System.out.println("=== Serveur Robi demarre sur le port " + port + " ===");

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (running) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Nouveau client connecte : " + clientSocket.getRemoteSocketAddress());
                ClientHandler handler = new ClientHandler(clientSocket, interpreter, space, botManager, this);
                new Thread(handler, "Client-" + clientSocket.getRemoteSocketAddress()).start();
            }
        } catch (IOException e) {
            System.err.println("Erreur serveur : " + e.getMessage());
        }
    }

    /**
     * Arrete le serveur en mettant fin a la boucle d'acceptation.
     */
    public void stop() {
        running = false;
    }

    /**
     * Retourne l'interpreteur Robi utilise par ce serveur.
     *
     * @return l'interpreteur Robi
     */
    public RobiInterpreter getInterpreter() {
        return interpreter;
    }

    /**
     * Retourne l'espace graphique du serveur.
     *
     * @return le GSpace du serveur
     */
    public GSpace getSpace() {
        return space;
    }

    /**
     * Retourne le gestionnaire de bots du serveur.
     *
     * @return le RobibotManager
     */
    public RobibotManager getBotManager() {
        return botManager;
    }

    /**
     * Enregistre un script dans l'historique des commandes.
     *
     * @param script le script execute avec succes
     */
    public void recordCommand(String script) {
        commandHistory.add(script);
    }

    /**
     * Retourne l'historique complet des commandes executees.
     *
     * @return une copie de l'historique
     */
    public List<String> getCommandHistory() {
        synchronized (commandHistory) {
            return new ArrayList<>(commandHistory);
        }
    }

    /**
     * Enregistre un client connecte.
     *
     * @param handler le gestionnaire du client
     */
    public void registerClient(ClientHandler handler) {
        clients.add(handler);
    }

    /**
     * Desinscrit un client deconnecte.
     *
     * @param handler le gestionnaire du client
     */
    public void unregisterClient(ClientHandler handler) {
        clients.remove(handler);
    }

    /**
     * Diffuse un script a tous les clients sauf l'emetteur.
     *
     * @param script le script a diffuser
     * @param sender le client qui a emis le script
     */
    public void broadcastScript(String script, ClientHandler sender) {
        for (ClientHandler client : clients) {
            if (client != sender) {
                client.sendBroadcast(script);
            }
        }
    }

    /**
     * Point d'entree du serveur Robi.
     * Accepte un numero de port en argument optionnel.
     *
     * @param args arguments de la ligne de commande (port optionnel)
     */
    public static void main(String[] args) {
        int port = protocol.RobiProtocol.DEFAULT_PORT;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Port invalide, utilisation du port par defaut : " + port);
            }
        }
        new RobiServer(port).start();
    }
}
