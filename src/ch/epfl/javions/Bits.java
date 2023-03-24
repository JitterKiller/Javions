package ch.epfl.javions;

import java.util.Objects;

/**
 * La classe Bits, publique et non instanciable, contient des méthodes
 * permettant d'extraire un sous-ensemble des 64 bits d'une valeur de type long.
 *
 * @author Adam AIT BOUSSELHAM (356365)
 * @author Abdellah JANATI IDRISSI (362341)
 */
public class Bits {

    /**
     * Constructeur privé de la classe Bits (non instanciable).
     */
    private Bits() {
    }

    /**
     * Méthode qui extrait du vecteur de 64 bits "value" la plage de "size" bits commençant
     * au bit d'index start, qu'elle interprète comme une valeur non signée
     *
     * @param value le vecteur de 64 bits en question.
     * @param start paramètre indiquant le bit où commencer l'extraction.
     * @param size  la longueur du vecteur de bits extrait.
     * @return le vecteur de bits extrait.
     * @throws IllegalArgumentException  si la taille n'est pas strictement supérieure à 0 et strictement inférieure à 32,
     *                                   grâce à la méthode checkArgument() de la classe Preconditions.
     * @throws IndexOutOfBoundsException si la plage décrite par start et size n'est pas totalement comprise entre 0 (inclus) et 64 (exclu),
     *                                   grâce à la méthode checkFromIndexSize() de la classe Objects.
     */
    public static int extractUInt(long value, int start, int size) {

        Preconditions.checkArgument(size > 0 && size < Integer.SIZE);
        Objects.checkFromIndexSize(start, size, Long.SIZE);

        long mask = (1L << size) - 1;
        return (int) ((value >>> start) & mask);

    }

    /**
     * Méthode qui teste si le bit de "value" d'index donné vaut 1 ou 0.
     *
     * @param value le vecteur de 64 bits.
     * @param index l'index où l'on veut voir si le bit vaut 1 ou 0.
     * @return Vrai (true) si le bit de "value" d'index donné vaut 1.
     * Faux (false) si le bit de "value" d'index donné vaut 0.
     * @throws IndexOutOfBoundsException si le bit n'est pas compris entre 0 (inclus) et 64 (exclu),
     *                                   grâce à la méthode checkIndex() de la classe Objects.
     */
    public static boolean testBit(long value, int index) {

        Objects.checkIndex(index, Long.SIZE);

        long mask = 1L << index;
        return (value & mask) != 0;
    }
}

