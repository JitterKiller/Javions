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
 * @author Abdellah Janati Idrissi (362341)
 */

public final class PowerWindow {

    private final InputStream stream;
    private final int windowSize;
    private final PowerComputer powerComputer;
    private int[] powerSamples;
    private int[] powerSamples_bis;
    private long position = 0;

    /**
     * Constructeur de la classe PowerWindow qui initialise le flot d'entrée, la taille de la fenêtre,
     * l'instance de PowerComputer, cree nos deux tableaux d'échantillons de puissance et initialise
     * le tableau principal d'échantillons de puissance grâce à readBatch.
     *
     * @param stream flot d'entrée.
     * @param windowSize Taille de la fenêtre de puissance.
     * @throws IOException si une erreur se produit lors de la lecture du flux d'entrée.
     * @throws IllegalArgumentException si la taille de la fenêtre est négative, nulle ou dépasse 2^16.
     */
    public PowerWindow(InputStream stream, int windowSize) throws IOException {

        Preconditions.checkArgument(windowSize > 0 && windowSize <= Math.scalb(1, 16));

        this.stream = stream;
        this.windowSize = windowSize;
        this.powerComputer = new PowerComputer(stream, (int) Math.scalb(1, 16));
        this.powerSamples = new int[(int) Math.scalb(1, 16)];
        this.powerSamples_bis = new int[(int) Math.scalb(1, 16)];
        powerComputer.readBatch(powerSamples);
    }

    /**
     * Retourne la taille de la fenêtre.
     * @return la taille de la fenêtre.
     */
    public int size() {
        return windowSize;
    }

    /**
     * Retourne la position actuelle de la fenêtre par rapport au début du flux de valeurs de puissance.
     * @return la position actuelle de la fenêtre par rapport au début du flux de valeurs de puissance.
     */
    public long position() {
        return position;
    }

    /**
     * Retourne vrai si la fenêtre est pleine, sinon retourne faux.
     * @return vrai si la fenêtre est pleine, sinon faux.
     */
    public boolean isFull() {
        return true;
        //return position + windowSize + 1 == powerComputer.readBatch(powerSamples);
    }

    /**
     * Permet de déterminer un échantillon de puissance à un index donné de la fenêtre.
     *
     * @param i index d'un échantillon de puissance dans une fenêtre donnée.
     * @return Retourne l'échantillon de puissance à l'index donné de la fenêtre.
     * @throws IndexOutOfBoundsException si cet index n'est pas compris entre 0 (inclus) et la taille de la fenêtre (exclu).
     */
    public int get(int i) {

        Objects.checkIndex(0, windowSize);

        if ((position + i) % powerSamples.length < i) {
            return powerSamples_bis[(int) ((position + i) % powerSamples.length)];
        } else {
            return powerSamples[(int) ((position + i) % powerSamples.length)];
        }
    }

    /**
     * Permet de déplacer la fenêtre de puissance d'un seul échantillon vers la droite.
     * @throws IOException en cas d'erreur d'entrée/sortie lors de la lecture du flux de données.
     */
    public void advance() throws IOException {

        if (position % powerSamples.length < powerSamples.length - 1) {

            if ((position + windowSize) % powerSamples.length < powerSamples.length - 1) {
                ++position;
            } else {
                powerComputer.readBatch(powerSamples_bis);
                ++position;
            }

        } else {
            int[] tempTab;
            tempTab = powerSamples;
            powerSamples = powerSamples_bis;
            powerSamples_bis = tempTab;
            ++position;
        }

    }

    /**
     * Permet de déplacer la fenêtre de puissance d'un nombre donné (offset) d'échantillon vers la droite.
     *
     * @throws IOException en cas d'erreur d'entrée/sortie lors de la lecture du flux de données.
     * @throws IllegalArgumentException si le nombre donné d'échantillon par lequel on deplace la fenêtre est nul
     */
    public void advanceBy(int offset) throws IOException {

        Preconditions.checkArgument(offset >= 0);

        for (int i = 0; i < offset; ++i) {
            advance();
        }
    }
}