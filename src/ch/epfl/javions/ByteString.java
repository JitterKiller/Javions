package ch.epfl.javions;

import java.util.Arrays;
import java.util.HexFormat;
import java.util.Objects;

/**
 * La classe ByteString, publique et finale, représente une chaîne (séquence) d'octets.
 * C'est une classe immuable et ces octets sont interprétés de manière non signée.
 *
 * @author Adam AIT BOUSSELHAM (356365)
 * @author Abdellah JANATI IDRISSI (362341)
 */
public final class ByteString {
    private static final HexFormat HF = HexFormat.of().withUpperCase();
    private final byte[] bytes;

    /**
     * Constructeur de la classe ByteString qui retourne une chaîne d'octets
     * dont le contenu est celui du tableau passé en argument
     *
     * @param bytes tableau de bytes.
     */
    public ByteString(byte[] bytes) {
        this.bytes = bytes.clone();
    }

    /**
     * Méthode qui facilite la construction de chaînes d'octets à partir de leur représentation hexadécimale
     *
     * @param hexString Représentation hexadécimale de la chaîne d'octets.
     * @return la chaîne d'octets à partir de la représentation hexadécimale de la chaine d'octets.
     * @throws IllegalArgumentException si la chaîne donnée n'est pas de longueur paire.
     * @throws IllegalArgumentException si la chaîne contient un caractère qui n'est pas un chiffre hexadécimal.
     */
    public static ByteString ofHexadecimalString(String hexString) {
        return new ByteString(HF.parseHex(hexString));
    }

    /**
     * Méthode pour connaitre la taille de la chaîne
     *
     * @return le nombre d'octets qu'elle contient.
     */
    public int size() {
        return bytes.length;
    }

    /**
     * Méthode pour obtenir l'octet à l'index donné.
     *
     * @param index l'index de l'octet que l'on souhaite avoir.
     * @return l'octet (interprété en non signé) à l'index donné.
     * @throws IndexOutOfBoundsException si l'index est invalide grâce à la méthode checkIndex()
     *                                   de la classe Objects.
     */
    public int byteAt(int index) {
        return Byte.toUnsignedInt(bytes[index]);
    }

    /**
     * Méthode pour obtenir les octets compris entre les index fromIndex (inclus) et toIndex (exclus)
     * sous la forme d'une valeur de type long
     *
     * @param fromIndex début de l'intervalle des octets.
     * @param toIndex   fin de l'intervalle des octets.
     * @return les octets compris entre ces index.
     * @throws IndexOutOfBoundsException si la plage décrite par fromIndex et toIndex n'est pas totalement comprise
     *                                   entre 0 et la taille de la chaîne grâce à la méthode checkFromIndexSize()
     *                                   de la classe Objects.
     * @throws IllegalArgumentException  si la différence entre toIndex et fromIndex n'est pas strictement inférieure à au
     *                                   nombre d'octets contenus dans une valeur de type long (8) grâce à la méthode checkArgument()
     *                                   de la classe Preconditions.
     */
    public long bytesInRange(int fromIndex, int toIndex) {

        Objects.checkFromIndexSize(fromIndex, toIndex - fromIndex, bytes.length);
        Preconditions.checkArgument(toIndex - fromIndex < Long.BYTES);

        long result = 0;

        for (int i = fromIndex; i < toIndex; i++) {
            result = (result << Long.BYTES) | byteAt(i);
        }

        return result;
    }

    /**
     * Override de la méthode equals pour la classe ByteString
     *
     * @param obj L'instance ByteString que l'on souhaite tester.
     * @return vrai si et seulement si la valeur qu'on lui passe est aussi une instance de
     * ByteString et que ses octets sont identiques à ceux du récepteur, sinon retourne faux.
     */
    @Override
    public boolean equals(Object obj) {
        return (obj instanceof ByteString that) && (Arrays.equals(bytes, that.bytes));
    }

    /**
     * Override de la méthode hashCode pour la classe ByteString
     *
     * @return la valeur retournée par la méthode hashCode de Arrays appliquée au tableau contenant les octets.
     */
    @Override
    public int hashCode() {
        return Arrays.hashCode(bytes);
    }

    /**
     * Override de la méthode toString pour la classe ByteString
     *
     * @return retourne une représentation des octets de la chaîne en hexadécimal,
     * chaque octet occupant exactement deux caractères.
     */
    @Override
    public String toString() {
        return HF.formatHex(bytes);
    }
}

