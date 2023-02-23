package ch.epfl.javions;

/**
 * La classe Preconditions nous offre une unique methode checkArgument qui vérifie si un argument entré en paramètre
 * est vrai ou faux
 *
 * @author Adam AIT BOUSSELHAM (356365)
 * @author Abdellah Janati Idrissi (362341)
 */
public final class Preconditions {

    /**
     * Constructeur privé de la classe (non instanciable).
     */
    private Preconditions() {}

    /**
     * L'unique méthode de la classe, vérifie si un argument entré en paramètre est vrai ou faux.
     * Cette méthode est utilisée pour lancer l'exception "IllegalArgumentException" lorsque le booléen
     * entré en paramètre est faux
     * @param shouldBeTrue
     *          booléen qui doit être vrai pour ne paas lancer d'exception.
     * @throws IllegalArgumentException
     *          si le booléen est faux.
     */
    public static void checkArgument(boolean shouldBeTrue){
        if(!shouldBeTrue){
            throw new IllegalArgumentException();
        }
    }
}
