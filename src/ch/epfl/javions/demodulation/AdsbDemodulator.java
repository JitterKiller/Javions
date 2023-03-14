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

    /* Fenêtre de puissance pour la démodulation ADS-B */
    private final PowerWindow window;

    /* Tableau pour stocker les octets des messages bruts */
    private final byte[] message = new byte[14];

    /* Constante représentant la taille de fenêtre d'échantillons de puissance */
    private final static int WINDOW_SIZE = 1200;

    /* Constante représentant le temps entre deux échantillons de puissance en nanosecondes */
    private final static int TIME_STAMP_NS_CONST = 100;

    /**
     * Constructeur d'AdsbDemodulator qui prend en entrée un InputStream
     * représentant le flux de données à démoduler.
     *
     * @param stream
     *          Le flux de données à démoduler.
     * @throws IOException
     *          Si une erreur se produit lors de la lecture du flux.
     */
    public AdsbDemodulator(InputStream stream) throws IOException {
        window = new PowerWindow(stream, WINDOW_SIZE);
    }

    /**
     * Méthode qui retourne le prochain message ADS-B démodulé à partir du flux d'entrée.
     * @return  Le prochain message ADS-B démodulé ou null s'il n'y a plus de
     *          messages à démoduler dans le flux.
     * @throws IOException
     *          Si une erreur se produit lors de la lecture du flux.
     */
    public RawMessage nextMessage() throws IOException {

        /* Somme des pics de puissance de porteuse actuelle et précédente */
        int sumCarrierPeak;
        int previousSumCarrierPeak = 0;

        /* Parcours de la fenêtre de puissance pour détecter les messages */
        for (; window.isFull(); window.advance()) {
            /* Calcul de la somme des pics de puissance de porteuse actuelle */
            sumCarrierPeak = computeCarrierSum();
            /* Si un nouveau pic de puissance de porteuse est détecté et que
            la puissance est suffisante, on décode le message brut */
            if (isPeakDetected(sumCarrierPeak,previousSumCarrierPeak)) {
                if (computeCarrierSum() >= computeTwiceBottomOutSums()) {
                    /* Si le downlink format (DF) du premier octet correspond à 17
                    on décode les octets restant du message brut */
                    if (RawMessage.size(computeFirstByte()) == RawMessage.LENGTH) {
                        computeRemainingBytes();
                        RawMessage rawMessage = RawMessage.of((window.position() * TIME_STAMP_NS_CONST), message);
                        /* Si le CRC du message est égal à 0 (donc un message valide),
                        on le retourne et on avance la fenêtre d'échantillons de puissance de 1199 + 1
                        (à l'aide de la boucle). */
                        if(rawMessage != null) {
                            window.advanceBy(WINDOW_SIZE - 1);
                            return rawMessage;
                        }
                    }
                }
            }
            /* On met à jour la somme des pics de puissance de porteuse précédente */
            previousSumCarrierPeak = sumCarrierPeak;
        }
        /* On retourne null lorsqu'il n'y a plus de messages à démoduler */
        return null;
    }

    /**
     * Méthode décodant le bit d'indice i d'un message ADS-B brut.
     * @param i
     *          Index du bit.
     * @return La valeur du bit (1 si la condition est vraie, 0 sinon).
     */
    private boolean decodeBits(int i) {
        return window.get(80 + (10 * i)) >= window.get(85 + (10 * i));
    }

    /**
     * Méthode qui vérifie si un pic de puissance est détecté.
     * @param sumCarrierPeak
     *          Somme des pics de porteuse.
     * @param previousSumCarrierPeak
     *          Somme des pics de porteuse précédente.
     * @return Vrai si un pic est détecté, sinon retourne faux.
     */
    private boolean isPeakDetected(int sumCarrierPeak, int previousSumCarrierPeak) {
        if(sumCarrierPeak > previousSumCarrierPeak) {
            int nextSumCarrierPeak = window.get(1) + window.get(11) + window.get(36) + window.get(46);
            return sumCarrierPeak > nextSumCarrierPeak;
        }
        return false;
    }

    /**
     * Méthode calculant la somme des pics de porteuse.
     * @return La somme des pics de porteuse.
     */
    private int computeCarrierSum() {
        return (window.get(0) + window.get(10) + window.get(35) + window.get(45));
    }

    /**
     * Méthode calculant la somme des vallées (des creux) multipliée par deux.
     * @return Deux fois la somme des vallées.
     */
    private int computeTwiceBottomOutSums() {
        return 2 * (window.get(5) + window.get(15) + window.get(20) + window.get(30) + window.get(40));
    }

    /**
     * Méthode calculant le premier octet d'un message brut.
     * @return Le premier octet d'un message brut.
     */
    private byte computeFirstByte() {
        byte byte0 = 0;
        for (int i = 0; i < Byte.SIZE; ++i) {
            if(decodeBits(i)) {
                byte0 |= (1 << (7 - i));
            }
        }
        message[0] = byte0;
        return byte0;
    }

    /**
     * Méthode calculant les 13 octets restant du message brut.
     */
    private void computeRemainingBytes() {
        for(int i = 1; i < message.length; ++i) {
            byte b = 0;
            int index = i * Byte.SIZE;
            for (int j = 0; j < Byte.SIZE; ++j) {
                if (decodeBits(index)) {
                    b |= (1 << (7 - j));
                }
                ++index;
            }
            message[i] = b;
        }
    }
}
