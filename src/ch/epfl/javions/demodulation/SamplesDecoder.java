package ch.epfl.javions.demodulation;

import ch.epfl.javions.Preconditions;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/**
 * La classe SamplesDecoder, du sous-paquetage demodulation, représente un décodeur d'échantillons
 * qui transforme un flot d'octets en un tableau d'échantillons et qui retourne le nombre d'échantillons
 * effectivement convertis.
 *
 * @author Adam AIT BOUSSELHAM (356365)
 * @author Abdellah JANATI IDRISSI (362341)
 */
public final class SamplesDecoder {
    private static final int OFFSET = 2048;
    private final InputStream stream;
    private final int batchSize;
    private final byte[] bytesBuffer;

    /**
     * Le constructeur de la classe initialise un flot d'entrée (grâce au stream entré en argument),
     * une taille de lot (batchSize) et un tampon (bytesBuffer) pour les données d'entrée (ces deux derniers
     * grâce à l'argument batchSize).
     *
     * @param stream    le flux d'entrée qui fournit les données à démoduler.
     * @param batchSize la taille de lot, c'est-à-dire le nombre d'échantillons à démoduler à la fois.
     * @throws IllegalArgumentException si la taille de lot n'est pas strictement positive (on utilise la méthode checkArgument()).
     * @throws NullPointerException     si le flot d'entrée est nul (on utilise la méthode requireNonNull()).
     */
    public SamplesDecoder(InputStream stream, int batchSize) {
        Preconditions.checkArgument(batchSize > 0);
        this.stream = Objects.requireNonNull(stream);
        this.batchSize = batchSize;
        bytesBuffer = new byte[batchSize * 2];
    }

    /**
     * Méthode qui lit une série d'octets depuis le flux d'entrée, puis les enregistre grâce
     * à la méthode readNBytes() dans un buffer.
     * Elle convertit ensuite ces octets en échantillons de 12 bits (dans un short de 16 bits) et les enregistre
     * dans le tableau d'échantillons fourni (batch).
     * Elle renvoie enfin le nombre d'échantillons convertis.
     *
     * @param batch le tableau d'échantillons qui doit être rempli.
     * @return le nombre d'échantillons convertis dans le tableau fourni (batch).
     * @throws IOException              si une erreur se produit lors de la lecture des octets à partir du flux d'entrée.
     * @throws IllegalArgumentException si la longueur du tableau d'échantillons fourni ne correspond pas à la
     *                                  taille de lot spécifiée lors de la création de l'objet SamplesDecoder
     *                                  (on utilise la méthode checkArgument()).
     */
    public int readBatch(short[] batch) throws IOException {

        Preconditions.checkArgument(batch.length == batchSize);

        int bytesRead = stream.readNBytes(bytesBuffer, 0, bytesBuffer.length);

        for (int i = 0; i < bytesRead / Short.BYTES; ++i) {
            int byteIndex = i * Short.BYTES;
            int sample = (Byte.toUnsignedInt(bytesBuffer[byteIndex + 1]) << Byte.SIZE)
                       | (Byte.toUnsignedInt(bytesBuffer[byteIndex]));
            batch[i] = (short) (sample - OFFSET);
        }
        return bytesRead / Short.BYTES;
    }
}
