package ch.epfl.javions.demodulation;

import ch.epfl.javions.Preconditions;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/**
 * La classe PowerWindow du sous-paquetage demodulation, publique et finale, représente une fenêtre de
 * taille fixe sur une séquence d'échantillons de puissance produits par un calculateur de puissance.
 *
 * @author Adam AIT BOUSSELHAM (356365)
 * @author Abdellah JANATI IDRISSI (362341)
 */

public final class PowerWindow {

    private final int windowSize;
    private final static int BATCH_SIZE = 1<<16;
    private final PowerComputer powerComputer;
    private int[] powerSamples;
    private int[] powerSamplesBis;
    private long position;
    private int powerSamplesRead;


    /**
     * Constructeur public de la classe PowerWindow qui initialise la taille de la fenêtre,
     * l'instance de PowerComputer, cree nos deux tableaux d'échantillons de puissance et initialise
     * le tableau principal d'échantillons de puissance grâce à la méthode readBatch().
     *
     * @param stream
     *          flot d'entrée.
     * @param windowSize
     *          Taille de la fenêtre de puissance.
     * @throws IOException
     *          si une erreur se produit lors de la lecture du flux d'entrée.
     * @throws IllegalArgumentException
     *          si la taille de la fenêtre est négative, nulle ou dépasse 2^16.
     */
    public PowerWindow(InputStream stream, int windowSize) throws IOException {

        Preconditions.checkArgument((windowSize > 0) && (windowSize <= BATCH_SIZE));

        this.windowSize = windowSize;
        this.powerComputer = new PowerComputer(stream, BATCH_SIZE);
        this.powerSamples = new int[BATCH_SIZE];
        this.powerSamplesBis = new int[BATCH_SIZE];
        powerSamplesRead = powerComputer.readBatch(powerSamples);
        position = 0;
    }

    /**
     * Méthode qui retourne la taille de la fenêtre.
     * @return la taille de la fenêtre.
     */
    public int size() {
        return windowSize;
    }

    /**
     * Méthode qui retourne la position actuelle de la fenêtre par rapport au début du flux de valeurs de puissance.
     * @return la position actuelle de la fenêtre par rapport au début du flux de valeurs de puissance.
     */
    public long position() {
        return position;
    }

    /**
     * Méthode qui retourne vrai si la fenêtre est pleine, sinon retourne faux.
     * @return vrai si la fenêtre est pleine, sinon faux.
     */
    public boolean isFull() {
        return size() <= powerSamplesRead;
    }

    /**
     * Méthode qui permet de déterminer un échantillon de puissance à un index donné de la fenêtre.
     *
     * @param i
     *          index d'un échantillon de puissance dans une fenêtre donnée.
     * @return Retourne l'échantillon de puissance à l'index donné de la fenêtre.
     * @throws IndexOutOfBoundsException
     *          si cet index n'est pas compris entre 0 (inclus) et la taille de la fenêtre (exclu).
     */
    public int get(int i) {

        Objects.checkIndex(i, size());

        if ((position() + i) % BATCH_SIZE < i) {
            return powerSamplesBis[(int) ((position() + i) % BATCH_SIZE)];
        } else {
            return powerSamples[(int) ((position() + i) % BATCH_SIZE)];
        }
    }

    /**
     * Méthode qui permet de déplacer la fenêtre de puissance d'un seul échantillon vers la droite.
     * @throws IOException
     *          en cas d'erreur d'entrée/sortie lors de la lecture du flux de données.
     */
    public void advance() throws IOException {

        if (position() % powerSamples.length < BATCH_SIZE - 1) {
            if ((position() + size()) % BATCH_SIZE >= BATCH_SIZE - 1) {
                powerSamplesRead += powerComputer.readBatch(powerSamplesBis);
            }
        } else {
            int[] tempTab = powerSamples;
            powerSamples = powerSamplesBis;
            powerSamplesBis = tempTab;
        }

        ++position;
        --powerSamplesRead;
    }

    /**
     * Méthode qui permet de déplacer la fenêtre de puissance d'un nombre donné (offset) d'échantillon vers la droite.
     *
     * @throws IOException
     *          en cas d'erreur d'entrée/sortie lors de la lecture du flux de données.
     * @throws IllegalArgumentException
     *          si le nombre donné d'échantillon par lequel on déplace la fenêtre est nul.
     */
    public void advanceBy(int offset) throws IOException {

        Preconditions.checkArgument(offset >= 0);

        for (int i = 0; i < offset; ++i) {
            advance();
        }
    }
}