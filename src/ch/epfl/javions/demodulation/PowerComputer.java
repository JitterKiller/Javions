package ch.epfl.javions.demodulation;

import ch.epfl.javions.Preconditions;

import java.io.IOException;
import java.io.InputStream;

/**
 * La classe PowerComputer du sous-paquetage demodulation, publique et finale, représente un
 * "calculateur de puissance", c.-à-d. un objet capable de calculer les échantillons de puissance
 * du signal à partir des échantillons signés produits par un décodeur d'échantillons.
 *
 * @author Adam AIT BOUSSELHAM (356365)
 * @author Abdellah JANATI IDRISSI (362341)
 */
public final class PowerComputer {

    private final SamplesDecoder decoder;
    private final short[] circularBuffer = new short[8];
    private final short[] powerBuffer;
    private final int batchSize;

    /**
     * Le constructeur de la classe initialise un flot d'entrée (grâce au stream entré en argument),
     * une instance de la classe SamplesDecoder (qui utilise le flot d'entrée passé en argument),
     * une taille de lot (batchSize), et un tableau où sont stockés tous les échantillons produit par l'instance
     * de SamplesDecoder (powerBuffer) ainsi qu'un tableau circulaire où sont stockés les 8 derniers échantillons décodés
     * grâce à l'instance SamplesDecoder (circularBuffer) pour calculer les nouveaux échantillons de puissance.
     *
     * @param stream    le flux d'entrée qui fournit les données à démoduler.
     * @param batchSize la taille de lot, c'est-à-dire le nombre d'échantillons à démoduler à la fois.
     * @throws IllegalArgumentException si la taille des lots n'est un multiple de 8 strictement positif.
     */
    public PowerComputer(InputStream stream, int batchSize) {

        Preconditions.checkArgument((batchSize > 0) && ((batchSize % 8) == 0));

        this.batchSize = batchSize;
        decoder = new SamplesDecoder(stream, batchSize * 2);
        powerBuffer = new short[batchSize * 2];
    }

    /**
     * Méthode qui lit depuis le décodeur d'échantillons le nombre d'échantillons nécessaire au calcul d'un lot
     * d'échantillons de puissance, puis les calcule au moyen de la formule de calcul de Puissance, les place dans le
     * tableau passé en argument.
     * La méthode stocke également les huits derniers échantillons de SamplesDecoder dans un tableau circulaire
     * pour pouvoir calculer les nouveaux échantillons de puissance.
     *
     * @param batch le tableau d'échantillons de puissances qui doit être rempli.
     * @return le nombre d'échantillons de puissances convertis dans le tableau fourni (batch).
     * @throws IOException              si une erreur se produit lors de la lecture des échantillons
     *                                  de SamplesDecoder à partir du flux d'entrée.
     * @throws IllegalArgumentException si la taille du tableau passé en argument n'est pas égale à la taille d'un lot
     *                                  (on utilise la méthode checkArgument()).
     */
    public int readBatch(int[] batch) throws IOException {

        Preconditions.checkArgument(batch.length == batchSize);

        int samplesRead = decoder.readBatch(powerBuffer);

        int bufferIndex = 0;
        for (int i = 0; i < samplesRead; i += 2) {
            circularBuffer[bufferIndex % 8] = powerBuffer[i];
            circularBuffer[(bufferIndex + 1) % 8] = powerBuffer[i + 1];
            bufferIndex += 2;
            double power = Math.pow(circularBuffer[0] - circularBuffer[2] + circularBuffer[4] - circularBuffer[6], 2) + Math.pow(circularBuffer[1] - circularBuffer[3] + circularBuffer[5] - circularBuffer[7], 2);
            batch[i / 2] = (int) power;
        }

        return samplesRead / 2;
    }
}