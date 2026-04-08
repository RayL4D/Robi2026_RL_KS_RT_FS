package core;

/**
 * Variable entiere utilisable dans les scripts. Sert de
 * recepteur dans une Reference pour les operations
 * arithmetiques et logiques.
 */
public class GVar {
    private int value;

    /**
     * Construit une variable avec la valeur initiale donnee.
     *
     * @param value la valeur initiale
     */
    public GVar(int value) {
        this.value = value;
    }

    /**
     * Retourne la valeur courante de la variable.
     *
     * @return la valeur entiere courante
     */
    public int getValue() {
        return value;
    }

    /**
     * Modifie la valeur de la variable.
     *
     * @param value la nouvelle valeur
     */
    public void setValue(int value) {
        this.value = value;
    }

    /**
     * Ajoute un montant a la valeur courante.
     *
     * @param amount le montant a ajouter
     */
    public void add(int amount) {
        this.value += amount;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "GVar{" + value + "}";
    }
}
