package core;

/**
 * Interface fonctionnelle pour ecouter les messages de sortie
 * de l'interpreteur. Utilisee par PrintVar et d'autres
 * commandes pour rediriger l'affichage vers l'IHM (historique)
 * au lieu de System.out.
 */
@FunctionalInterface
public interface OutputListener {

    /**
     * Appelee lorsqu'un message de sortie est emis par
     * l'interpreteur.
     *
     * @param message le message de sortie a traiter
     */
    void onOutput(String message);
}
