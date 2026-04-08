package core;

import java.io.IOException;
import java.util.List;

import stree.parser.SNode;
import stree.parser.SParser;

/**
 * Implementation principale de l'interpreteur Robi.
 * Gere le parsing des S-Expressions, l'execution des noeuds,
 * et l'evaluation recursive des arguments.
 *
 * <p>Peut etre utilise seul (mode console) ou integre dans
 * une architecture client-serveur (partie 2).</p>
 */
public class RobiInterpreter implements Interpreter {
    private final Environment environment;
    private OutputListener outputListener;

    /**
     * Construit un interpreteur Robi avec un environnement
     * vide.
     */
    public RobiInterpreter() {
        this.environment = new Environment();
    }

    /**
     * Definit un listener pour rediriger les messages de sortie
     * (print, etc.). Utile pour afficher les resultats dans
     * l'IHM au lieu de System.out.
     *
     * @param listener le listener de sortie a enregistrer
     */
    public void setOutputListener(OutputListener listener) {
        this.outputListener = listener;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void output(String message) {
        if (outputListener != null) {
            outputListener.onOutput(message);
        } else {
            System.out.println(message);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Environment getEnvironment() {
        return environment;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void oneShot(String script) {
        SParser<SNode> parser = new SParser<>();
        try {
            List<SNode> compiled = parser.parse(script);
            for (SNode node : compiled) {
                executeNode(node);
            }
        } catch (IOException e) {
            System.err.println(
                    "Erreur de parsing : " + e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Reference executeNode(SNode expr) {
        String receiverName = expr.get(0).contents();
        Reference receiver =
                environment.getReferenceByName(receiverName);

        if (receiver == null) {
            System.err.println(
                    "Reference inconnue : " + receiverName);
            return null;
        }

        return receiver.run(expr);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object evaluateArgument(SNode arg) {
        // Cas 1 : sous-expression avec enfants -> recursion
        if (arg.isNode() && arg.hasChildren()) {
            Reference ref = executeNode(arg);
            if (ref != null) {
                Object receiver = ref.getReceiver();
                if (receiver instanceof GVar) {
                    return ((GVar) receiver).getValue();
                }
                return receiver;
            }
            return null;
        }

        // Cas 2 : feuille (texte simple)
        String text = arg.contents();

        // 2a. Est-ce un entier ?
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException ignored) {
            // Ce n'est pas un entier, on continue
        }

        // 2b. Est-ce le nom d'une variable ?
        Reference ref = environment.getReferenceByName(text);
        if (ref != null
                && ref.getReceiver() instanceof GVar) {
            return ((GVar) ref.getReceiver()).getValue();
        }

        // 2c. Sinon, texte brut (nom de couleur, etc.)
        return text;
    }
}
