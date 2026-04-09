package client;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import protocol.RobiProtocol;
import protocol.SNodeSerializer;
import stree.parser.SNode;

/**
 * Client reseau Robi.
 * Se connecte au serveur, envoie des S-Expressions textuelles,
 * et recoit les SNodes serialises (sans jamais utiliser SParser).
 *
 * <p>Fournit des callbacks pour notifier l'IHM des evenements.
 */
public class RobiClient {

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private boolean connected;
    private Thread listenerThread;

    private final List<ConnectionListener> listeners = new ArrayList<>();
    private final List<BotTickListener> botTickListeners = new ArrayList<>();
    private final List<SyncListener> syncListeners = new ArrayList<>();
    private final LinkedBlockingQueue<String> responseQueue = new LinkedBlockingQueue<>();

    // ========================================================================
    // CONNEXION
    // ========================================================================

    /**
     * Etablit une connexion avec le serveur Robi.
     *
     * @param host l'adresse du serveur
     * @param port le port du serveur
     * @throws IOException si la connexion echoue
     */
    public void connect(String host, int port) throws IOException {
        socket = new Socket(host, port);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
        connected = true;
        startListenerThread();
        notifyConnected(host, port);
    }

    /**
     * Ferme la connexion avec le serveur.
     */
    public void disconnect() {
        try {
            connected = false;
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            notifyDisconnected();
        } catch (IOException e) {
            System.err.println("Erreur lors de la deconnexion : " + e.getMessage());
        }
    }

    /**
     * Verifie si le client est actuellement connecte au serveur.
     *
     * @return true si la connexion est active, false sinon
     */
    public boolean isConnected() {
        return connected && socket != null && !socket.isClosed();
    }

    // ========================================================================
    // ENVOI / RECEPTION
    // ========================================================================

    /**
     * Envoie un script S-Expression au serveur et retourne les SNodes deserialises.
     *
     * @param script le texte brut de la S-Expression
     * @return la liste des SNode reconstruits (sans SParser)
     * @throws IOException si l'envoi ou la reception echoue
     */
    public synchronized List<SNode> sendScript(String script) throws IOException {
        if (!isConnected()) {
            throw new IOException("Non connecte au serveur.");
        }

        // Envoi du script
        out.println(RobiProtocol.PREFIX_SCRIPT + script);
        out.println(RobiProtocol.END_OF_MESSAGE);

        // Lecture de la reponse
        String response = waitForResponse();

        if (response.startsWith(RobiProtocol.PREFIX_NODES)) {
            String serialized = response.substring(RobiProtocol.PREFIX_NODES.length());
            return SNodeSerializer.deserialize(serialized);
        } else if (response.startsWith(RobiProtocol.PREFIX_ERROR)) {
            throw new IOException(response.substring(RobiProtocol.PREFIX_ERROR.length()));
        }

        throw new IOException("Reponse inattendue du serveur : " + response);
    }

    /**
     * Demande une capture d'ecran au serveur.
     *
     * @return les donnees Base64 de l'image PNG
     * @throws IOException si la demande ou la reception echoue
     */
    public synchronized String requestScreenshot() throws IOException {
        if (!isConnected()) {
            throw new IOException("Non connecte au serveur.");
        }

        out.println(RobiProtocol.PREFIX_SCREENSHOT);
        out.println(RobiProtocol.END_OF_MESSAGE);

        String response = waitForResponse();

        if (response.startsWith(RobiProtocol.PREFIX_IMAGE)) {
            return response.substring(RobiProtocol.PREFIX_IMAGE.length());
        } else if (response.startsWith(RobiProtocol.PREFIX_ERROR)) {
            throw new IOException(response.substring(RobiProtocol.PREFIX_ERROR.length()));
        }

        throw new IOException("Reponse inattendue pour le screenshot.");
    }

    /**
     * Demande au serveur de sauvegarder la scene.
     *
     * @return le JSON de la scene
     * @throws IOException si la demande ou la reception echoue
     */
    public synchronized String requestSave() throws IOException {
        if (!isConnected()) {
            throw new IOException("Non connecte au serveur.");
        }

        out.println(RobiProtocol.PREFIX_SAVE);
        out.println(RobiProtocol.END_OF_MESSAGE);

        String response = waitForResponse();

        if (response.startsWith(RobiProtocol.PREFIX_STATE)) {
            return response.substring(RobiProtocol.PREFIX_STATE.length());
        } else if (response.startsWith(RobiProtocol.PREFIX_ERROR)) {
            throw new IOException(response.substring(RobiProtocol.PREFIX_ERROR.length()));
        }

        throw new IOException("Reponse inattendue pour la sauvegarde.");
    }

    /**
     * Envoie un JSON de scene au serveur pour le charger.
     *
     * @param json le JSON de la scene a charger
     * @return les SNodes deserialises a executer cote client
     * @throws IOException si l'envoi ou la reception echoue
     */
    public synchronized List<SNode> requestLoad(String json) throws IOException {
        if (!isConnected()) {
            throw new IOException("Non connecte au serveur.");
        }

        out.println(RobiProtocol.PREFIX_LOAD + json);
        out.println(RobiProtocol.END_OF_MESSAGE);

        String response = waitForResponse();

        if (response.startsWith(RobiProtocol.PREFIX_NODES)) {
            String serialized = response.substring(RobiProtocol.PREFIX_NODES.length());
            return SNodeSerializer.deserialize(serialized);
        } else if (response.startsWith(RobiProtocol.PREFIX_ERROR)) {
            throw new IOException(response.substring(RobiProtocol.PREFIX_ERROR.length()));
        }

        throw new IOException("Reponse inattendue pour le chargement.");
    }

    /**
     * Demarre un thread d'ecoute qui lit en continu les messages du serveur.
     * Les messages BOT_TICK sont dispatches aux listeners.
     * Les reponses (NODES, ERROR, IMAGE, STATE) sont mises en file d'attente
     * pour etre recuperees par les methodes synchrones.
     */
    private void startListenerThread() {
        listenerThread = new Thread(() -> {
            try {
                while (connected) {
                    String message = readFullMessageDirect();
                    if (message == null) {
                        break;
                    }

                    if (message.startsWith(RobiProtocol.PREFIX_BOT_TICK)) {
                        String commandsStr = message.substring(
                                RobiProtocol.PREFIX_BOT_TICK.length());
                        String[] cmds = commandsStr.split(";;");
                        List<String> commands = new ArrayList<>();
                        for (String cmd : cmds) {
                            if (!cmd.isEmpty()) {
                                commands.add(cmd);
                            }
                        }
                        notifyBotTick(commands);
                    } else if (message.startsWith(RobiProtocol.PREFIX_SYNC)) {
                        String syncData = message.substring(
                                RobiProtocol.PREFIX_SYNC.length());
                        String[] cmds = syncData.split(";;");
                        List<String> commands = new ArrayList<>();
                        for (String cmd : cmds) {
                            if (!cmd.isEmpty()) {
                                commands.add(cmd);
                            }
                        }
                        notifySyncReceived(commands);
                    } else if (message.startsWith(RobiProtocol.PREFIX_BROADCAST)) {
                        String script = message.substring(
                                RobiProtocol.PREFIX_BROADCAST.length());
                        notifyBroadcastReceived(script);
                    } else {
                        responseQueue.put(message);
                    }
                }
            } catch (IOException e) {
                if (connected) {
                    connected = false;
                    notifyDisconnected();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "RobiClient-Listener");
        listenerThread.setDaemon(true);
        listenerThread.start();
    }

    /**
     * Lit un message complet depuis le flux d'entree jusqu'au marqueur EOM.
     *
     * @return le message complet, ou null si la connexion est fermee
     * @throws IOException si une erreur de lecture survient
     */
    private String readFullMessageDirect() throws IOException {
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) {
            if (line.equals(RobiProtocol.END_OF_MESSAGE)) {
                break;
            }
            if (sb.length() > 0) {
                sb.append("\n");
            }
            sb.append(line);
        }
        if (line == null) {
            connected = false;
            return null;
        }
        return sb.toString();
    }

    /**
     * Attend une reponse du serveur (hors BOT_TICK) avec un timeout de 30 secondes.
     *
     * @return la reponse du serveur
     * @throws IOException si le timeout est atteint ou si l'attente est interrompue
     */
    private String waitForResponse() throws IOException {
        try {
            String response = responseQueue.poll(30, TimeUnit.SECONDS);
            if (response == null) {
                throw new IOException("Timeout en attente de reponse du serveur.");
            }
            return response;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Attente interrompue.");
        }
    }

    // ========================================================================
    // LISTENERS
    // ========================================================================

    /**
     * Ajoute un listener pour les evenements de connexion.
     *
     * @param listener le listener a ajouter
     */
    public void addConnectionListener(ConnectionListener listener) {
        listeners.add(listener);
    }

    /**
     * Notifie les listeners qu'une connexion a ete etablie.
     *
     * @param host l'adresse du serveur
     * @param port le port du serveur
     */
    private void notifyConnected(String host, int port) {
        for (ConnectionListener l : listeners) {
            l.onConnected(host, port);
        }
    }

    /**
     * Notifie les listeners que la connexion a ete perdue.
     */
    private void notifyDisconnected() {
        for (ConnectionListener l : listeners) {
            l.onDisconnected();
        }
    }

    /**
     * Interface de callback pour les evenements de connexion.
     */
    public interface ConnectionListener {

        /**
         * Appele lorsque la connexion au serveur est etablie.
         *
         * @param host l'adresse du serveur
         * @param port le port du serveur
         */
        void onConnected(String host, int port);

        /**
         * Appele lorsque la connexion au serveur est perdue.
         */
        void onDisconnected();
    }

    // ========================================================================
    // BOT TICK LISTENERS
    // ========================================================================

    /**
     * Ajoute un listener pour les ticks de bots recus du serveur.
     *
     * @param listener le listener a ajouter
     */
    public void addBotTickListener(BotTickListener listener) {
        botTickListeners.add(listener);
    }

    /**
     * Notifie les listeners qu'un tick de bot a ete recu.
     *
     * @param commands la liste des commandes du tick
     */
    private void notifyBotTick(List<String> commands) {
        for (BotTickListener l : botTickListeners) {
            l.onBotTick(commands);
        }
    }

    /**
     * Interface de callback pour les ticks des bots recus du serveur.
     */
    public interface BotTickListener {

        /**
         * Appele lorsqu'un tick de bot est recu du serveur.
         *
         * @param commands la liste des commandes a executer
         */
        void onBotTick(List<String> commands);
    }

    // ========================================================================
    // SYNC / BROADCAST LISTENERS
    // ========================================================================

    /**
     * Ajoute un listener pour la synchronisation initiale.
     *
     * @param listener le listener a ajouter
     */
    public void addSyncListener(SyncListener listener) {
        syncListeners.add(listener);
    }

    /**
     * Notifie les listeners qu'une synchronisation initiale est recue.
     *
     * @param commands la liste des commandes de l'historique
     */
    private void notifySyncReceived(List<String> commands) {
        for (SyncListener l : syncListeners) {
            l.onSyncReceived(commands);
        }
    }

    /**
     * Notifie les listeners d'un script diffuse par un autre client.
     *
     * @param script le script diffuse
     */
    private void notifyBroadcastReceived(String script) {
        for (BotTickListener l : botTickListeners) {
            // Reuse bot tick listeners to replay broadcast commands
        }
        for (SyncListener l : syncListeners) {
            l.onBroadcastReceived(script);
        }
    }

    /**
     * Interface de callback pour la synchronisation et la diffusion.
     */
    public interface SyncListener {

        /**
         * Appele lorsque l'historique complet des commandes est recu a la connexion.
         *
         * @param commands la liste des commandes
         */
        void onSyncReceived(List<String> commands);

        /**
         * Appele lorsqu'un script diffuse par un autre client est recu.
         *
         * @param script le script a executer
         */
        void onBroadcastReceived(String script);
    }
}
