package protocol;

import java.util.ArrayList;
import java.util.List;

import stree.parser.SDefaultNode;
import stree.parser.SNode;

/**
 * Serialise et deserialise des arbres SNode en format texte simple.
 * Permet au client de reconstruire des SNode sans avoir besoin de SParser.
 *
 * <p>Format :
 * <ul>
 *   <li>Feuille : L:contenu</li>
 *   <li>Noeud (n=3) : N:3 suivi de ses enfants</li>
 *   <li>Separateur entre expressions : ---</li>
 * </ul>
 */
public class SNodeSerializer {

    private static final String SEPARATOR = "---";
    private static final String LEAF_PREFIX = "L:";
    private static final String NODE_PREFIX = "N:";

    // ========================================================================
    // SERIALISATION (Serveur -> Client)
    // ========================================================================

    /**
     * Serialise une liste de SNode en chaine de lignes.
     *
     * @param nodes la liste des SNode a serialiser
     * @return la representation textuelle serialisee
     */
    public static String serialize(List<SNode> nodes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < nodes.size(); i++) {
            if (i > 0) {
                sb.append(SEPARATOR).append("\n");
            }
            serializeNode(nodes.get(i), sb);
        }
        return sb.toString();
    }

    /**
     * Serialise recursivement un SNode dans le StringBuilder.
     *
     * @param node le noeud a serialiser
     * @param sb le StringBuilder dans lequel ecrire
     */
    private static void serializeNode(SNode node, StringBuilder sb) {
        if (node.isLeaf()) {
            sb.append(LEAF_PREFIX).append(escapeContent(node.contents())).append("\n");
        } else {
            int childCount = node.size();
            sb.append(NODE_PREFIX).append(childCount).append("\n");
            for (int i = 0; i < childCount; i++) {
                serializeNode(node.get(i), sb);
            }
        }
    }

    /**
     * Echappe les caracteres speciaux dans le contenu d'une feuille.
     *
     * @param content le contenu a echapper
     * @return le contenu echappe
     */
    private static String escapeContent(String content) {
        if (content == null) {
            return "";
        }
        return content.replace("\\", "\\\\").replace("\n", "\\n");
    }

    // ========================================================================
    // DESERIALISATION (Client - sans SParser)
    // ========================================================================

    /**
     * Deserialise une chaine en liste de SNode.
     * Utilisable cote client sans aucune dependance a SParser (sauf SDefaultNode).
     *
     * @param data la chaine serialisee a deserialiser
     * @return la liste des SNode reconstruits
     */
    public static List<SNode> deserialize(String data) {
        List<SNode> result = new ArrayList<>();
        String[] blocks = data.split(SEPARATOR + "\n");
        for (String block : blocks) {
            if (block.trim().isEmpty()) {
                continue;
            }
            String[] lines = block.split("\n");
            int[] index = {0};
            SNode node = deserializeNode(lines, index);
            if (node != null) {
                result.add(node);
            }
        }
        return result;
    }

    /**
     * Deserialise recursivement un SNode a partir des lignes.
     *
     * @param lines le tableau de lignes a lire
     * @param index l'index courant dans le tableau (passe par reference via tableau)
     * @return le SNode deserialise, ou null si fin des lignes
     */
    private static SNode deserializeNode(String[] lines, int[] index) {
        if (index[0] >= lines.length) {
            return null;
        }

        String line = lines[index[0]];
        index[0]++;

        if (line.startsWith(LEAF_PREFIX)) {
            String content = unescapeContent(line.substring(LEAF_PREFIX.length()));
            SDefaultNode leaf = new SDefaultNode();
            leaf.setContents(content);
            return leaf;
        } else if (line.startsWith(NODE_PREFIX)) {
            int childCount = Integer.parseInt(line.substring(NODE_PREFIX.length()));
            SDefaultNode node = new SDefaultNode();
            for (int i = 0; i < childCount; i++) {
                SNode child = deserializeNode(lines, index);
                if (child != null) {
                    node.addChild(child);
                }
            }
            return node;
        }

        return null;
    }

    /**
     * Restaure les caracteres speciaux echappes dans le contenu d'une feuille.
     *
     * @param content le contenu echappe
     * @return le contenu restaure
     */
    private static String unescapeContent(String content) {
        return content.replace("\\n", "\n").replace("\\\\", "\\");
    }
}
