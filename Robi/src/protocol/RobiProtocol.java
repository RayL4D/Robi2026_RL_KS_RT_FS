package protocol;

/**
 * Constantes et utilitaires du protocole de communication Robi.
 *
 * <p>Format des messages :
 * <ul>
 *   <li>Client vers Serveur : SCRIPT:&lt;texte s-expression&gt;\nEOM</li>
 *   <li>Serveur vers Client : NODES:&lt;donnees serialisees&gt;\nEOM
 *       ou ERROR:&lt;message&gt;\nEOM
 *       ou IMAGE:&lt;base64 png&gt;\nEOM</li>
 * </ul>
 *
 * <p>EOM (End Of Message) est un marqueur de fin pour les messages multi-lignes.
 */
public class RobiProtocol {

    /** Port par defaut du serveur Robi. */
    public static final int DEFAULT_PORT = 12345;

    /** Prefixe pour l'envoi d'un script S-Expression. */
    public static final String PREFIX_SCRIPT = "SCRIPT:";

    /** Prefixe pour la reponse contenant des SNodes serialises. */
    public static final String PREFIX_NODES = "NODES:";

    /** Prefixe pour une reponse d'erreur. */
    public static final String PREFIX_ERROR = "ERROR:";

    /** Prefixe pour une reponse contenant une image encodee en Base64. */
    public static final String PREFIX_IMAGE = "IMAGE:";

    /** Prefixe pour une demande de capture d'ecran. */
    public static final String PREFIX_SCREENSHOT = "SCREENSHOT";

    /** Prefixe pour une demande de sauvegarde de scene. */
    public static final String PREFIX_SAVE = "SAVE";

    /** Prefixe pour une reponse contenant l'etat de la scene en JSON. */
    public static final String PREFIX_STATE = "STATE:";

    /** Prefixe pour une demande de chargement de scene depuis JSON. */
    public static final String PREFIX_LOAD = "LOAD:";

    /** Prefixe pour les ticks de bots transmis du serveur au client. */
    public static final String PREFIX_BOT_TICK = "BOT_TICK:";

    /** Prefixe pour la synchronisation de l'etat initial vers un nouveau client. */
    public static final String PREFIX_SYNC = "SYNC:";

    /** Prefixe pour la diffusion d'un script d'un client vers les autres clients. */
    public static final String PREFIX_BROADCAST = "BROADCAST:";

    /** Marqueur de fin de message. */
    public static final String END_OF_MESSAGE = "EOM";

    /**
     * Constructeur prive pour empecher l'instanciation de cette classe utilitaire.
     */
    private RobiProtocol() {
    }
}
