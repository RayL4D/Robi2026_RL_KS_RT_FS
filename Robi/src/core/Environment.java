package core;

import java.util.HashMap;
import java.util.Map;

/**
 * Registre central qui associe des noms (ex: "space",
 * "space.robi") a des References. Gere aussi le nettoyage
 * en cascade lors de la suppression d'elements imbriques.
 */
public class Environment {
    private final Map<String, Reference> variables;

    /**
     * Construit un environnement vide.
     */
    public Environment() {
        variables = new HashMap<>();
    }

    /**
     * Enregistre une reference sous le nom donne.
     *
     * @param name le nom de la variable
     * @param ref  la reference a enregistrer
     */
    public void addReference(String name, Reference ref) {
        variables.put(name, ref);
    }

    /**
     * Retourne la reference associee au nom donne.
     *
     * @param name le nom de la variable recherchee
     * @return la reference correspondante, ou {@code null}
     */
    public Reference getReferenceByName(String name) {
        return variables.get(name);
    }

    /**
     * Supprime une reference par son nom.
     *
     * @param name le nom de la reference a supprimer
     */
    public void removeReference(String name) {
        variables.remove(name);
    }

    /**
     * Supprime une reference et toutes ses sous-references
     * (notation pointee). Ex: removeWithChildren("space.robi")
     * supprime aussi "space.robi.img", etc.
     *
     * @param name le nom de la reference racine a supprimer
     */
    public void removeWithChildren(String name) {
        variables.remove(name);
        String prefix = name + ".";
        variables.entrySet()
                .removeIf(entry -> entry.getKey().startsWith(prefix));
    }

    /**
     * Supprime toutes les references de l'environnement.
     */
    public void clear() {
        variables.clear();
    }

    /**
     * Retourne une copie non modifiable de toutes les entrees.
     * Utile pour la serialisation (sauvegarde de la scene).
     *
     * @return une copie des associations nom-reference
     */
    public Map<String, Reference> getAll() {
        return new HashMap<>(variables);
    }
}
