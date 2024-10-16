package ch.epfl.javions.demodulation;

import ch.epfl.javions.adsb.RawMessage;

import java.io.IOException;
import java.io.InputStream;

/**
 * La classe AdsbDemodulator du sous-paquetage demodulation, publique et finale, représente un
 * démodulateur de messages ADS-B.
 *
 * @author Adam AIT BOUSSELHAM (356365)
 * @author Abdellah JANATI IDRISSI (362341)
 */
public final class AdsbDemodulator {
    private static final int WINDOW_SIZE = 1200;
    private static final int TIME_STAMP_NS_CONST = 100;
    private static final int FIRST_BYTE_INDEX = 0;
    private final PowerWindow window;
    private final byte[] message = new byte[RawMessage.LENGTH];
    private int sumCarrierPeak, previousSumCarrierPeak, nextSumCarrierPeak;

    /**
     * Constructeur d'AdsbDemodulator qui prend en entrée un InputStream
     * représentant le flux de données à démoduler.
     *
     * @param stream Le flux de données à démoduler.
     * @throws IOException Si une erreur se produit lors de la lecture du flux.
     */
    public AdsbDemodulator(InputStream stream) throws IOException {
        window = new PowerWindow(stream, WINDOW_SIZE);
    }

    /**
     * Méthode qui retourne le prochain message ADS-B démodulé à partir du flux d'entrée.
     *
     * @return Le prochain message ADS-B démodulé ou null s'il n'y a plus de
     * messages à démoduler dans le flux.
     * @throws IOException Si une erreur se produit lors de la lecture du flux.
     */
    public RawMessage nextMessage() throws IOException {
        previousSumCarrierPeak = sumCarrierPeak = 0;
        /* Parcours de la fenêtre de puissance pour détecter les messages */
        for (; window.isFull(); window.advance()) {
            /* Calcul de la somme des pics de puissance de porteuse suivante */
            nextSumCarrierPeak = computeNextCarrierSum();
            /* Si un nouveau pic de puissance de porteuse est détecté et que
             * la puissance est suffisante, on décode le message brut */
            if (isPeakDetected()) {
                if (sumCarrierPeak >= computeTwiceBottomOutSums()) {
                    /* Si le downlink format (DF) du premier octet correspond à 17
                     * on décode les octets restant du message brut */
                    if (RawMessage.size(getByte(FIRST_BYTE_INDEX)) == RawMessage.LENGTH) {
                        computeRemainingBytes();
                        RawMessage rawMessage = RawMessage.of((window.position() * TIME_STAMP_NS_CONST), message);
                        /* Si le CRC du message est égal à 0 (donc un message valide),
                         * on le retourne et on avance la fenêtre d'échantillons de puissance de 1199 + 1
                         * (à l'aide de la boucle). */
                        if (rawMessage != null) {
                            window.advanceBy(WINDOW_SIZE - 1);
                            return rawMessage;
                        }
                    }
                }
            }
            /* On met à jour la somme des pics de puissance de porteuse précédente et actuelle*/
            previousSumCarrierPeak = sumCarrierPeak;
            sumCarrierPeak = nextSumCarrierPeak;
        }
        /* On retourne null lorsqu'il n'y a plus de messages à démoduler */
        return null;
    }

    /**
     * Méthode décodant le bit d'indice i d'un message ADS-B brut.
     *
     * @param i Index du bit.
     * @return  La valeur du bit (1 si la condition est vraie, 0 sinon).
     */
    private boolean decodeBits(int i) {
        return window.get(80 + (10 * i)) >= window.get(85 + (10 * i));
    }

    /**
     * Méthode qui vérifie si un pic de puissance est détecté.
     *
     * @return Vrai si un pic est détecté, sinon retourne faux.
     */
    private boolean isPeakDetected() {
        return sumCarrierPeak > previousSumCarrierPeak && sumCarrierPeak > nextSumCarrierPeak;
    }

    /**
     * Méthode calculant la somme des pics de porteuse suivante.
     *
     * @return La somme des pics de porteuse suivante.
     */
    private int computeNextCarrierSum() {
        return (window.get(1) + window.get(11) + window.get(36) + window.get(46));
    }

    /**
     * Méthode calculant la somme des vallées (des creux) multipliée par deux.
     *
     * @return Deux fois la somme des vallées.
     */
    private int computeTwiceBottomOutSums() {
        return 2 * (window.get(5) + window.get(15) + window.get(20) + window.get(30) + window.get(40));
    }

    /**
     * Méthode calculant l'octet d'indice i d'un message ADS-B brut.
     *
     * @param i L'indice de l'octet du message brut.
     * @return  L'octet d'indice i d'un message brut.
     */
    private byte getByte(int i) {
        byte b = 0;
        int adsbIndex = i * Byte.SIZE;
        for (int j = 0; j < Byte.SIZE; ++j) {
            if (decodeBits(adsbIndex)) {
                b |= (1 << (7 - j));
            }
            ++adsbIndex;
        }
        message[i] = b;
        return b;
    }

    /**
     * Méthode calculant les 13 octets restant du message brut.
     */
    private void computeRemainingBytes() {
        for (int i = 1; i < RawMessage.LENGTH; ++i) {
            message[i] = getByte(i);
        }
    }
}