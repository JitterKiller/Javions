package ch.epfl.javions;

/**
 * La classe Bits, publique et non instanciable, contient des méthodes
 * permettant d'extraire un sous-ensemble des 64 bits d'une valeur de type long.
 *
 * @author Adam AIT BOUSSELHAM (356365)
 * @author Abdellah Janati Idrissi (362341)
 */
public class Bits {

    /**
     * Constructeur privé de la classe Bits (non instanciable).
     */
    private Bits() {}

    /**
     * Méthode qui extrait du vecteur de 64 bits "value" la plage de "size" bits commençant
     * au bit d'index start, qu'elle interprète comme une valeur non signée.
     * @param value
     *          le vecteur de 64 bits en question
     * @param start
     *          paramètre indiquant le bit où commencer l'extraction
     * @param size
     *          la longueur du vecteur de bits extrait.
     * @return le vecteur de bits extrait.
     * @throws IllegalArgumentException
     *          si la taille n'est pas strictement supérieure à 0 et strictement inférieure à 32.
     * @throws IndexOutOfBoundsException
     *           si la plage décrite par start et size n'est pas totalement comprise entre 0 (inclus) et 64 (exclu).
     */
    public static int extractUInt(long value, int start, int size) {

        if (size <= 0 || size >= Integer.SIZE) {
            throw new IllegalArgumentException("La taille doit être strictement comprise entre 0 et 32 (exclus)");
        }
        if (start < 0 || start + size > Long.SIZE) {
            throw new IndexOutOfBoundsException("Start doit être positive et la plage décrite par start et size doit être totalement comprise entre 0 (inclus) et 64 (exclu)");
        }

        long mask1 = (1L << size) - 1;
        return (int) ((value >>> start) & mask1);

    }

    /**
     * Méthode qui teste si le bit de "value" d'index donné vaut 1 ou 0.
     * @param value
     *          le vecteur de 64 bits.
     * @param index
     *          l'index où l'on veut voir si le bit vaut 1 ou 0.
     * @return
     *          Vrai (true) si le bit de "value" d'index donné vaut 1.
     *          Faux (false) si le bit de "value" d'index donné vaut 0.
     * @throws IndexOutOfBoundsException
     *          si le bit n'est pas compris entre 0 (inclus) et 64 (exclu).
     */
    public static boolean testBit(long value, int index) {

        if (index < 0 || index >= 64) {
            throw new IndexOutOfBoundsException("L'index doit être compris entre 0 (inclus) et 64 (exclu)");
        }

        long mask2 = 1L << index;
        return (value & mask2) != 0;
    }
}

