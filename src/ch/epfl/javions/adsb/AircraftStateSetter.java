package ch.epfl.javions.adsb;

import ch.epfl.javions.GeoPos;

/**
 * Interface définissant les méthodes pour mettre à jour l'état d'un aéronef.
 *
 * @author Adam AIT BOUSSELHAM (356365)
 * @author Abdellah JANATI IDRISSI (362341)
 */
public interface AircraftStateSetter {

    /**
     * Met à jour le timestamp du dernier message reçu pour cet aéronef.
     * @param timeStampNs
     *          Le nouveau timestamp en nanosecondes.
     */
    void setLastMessageTimeStampNs(long timeStampNs);

    /**
     * Met à jour la catégorie de l'aéronef.
     * @param category
     *          La nouvelle catégorie de l'aéronef.
     */
    void setCategory(int category);

    /**
     * Met à jour le call sign de l'aéronef.
     * @param callSign
     *          Le nouveau call sign de l'aéronef.
     */
    void setCallSign(CallSign callSign);

    /**
     * Met à jour la position géographique de l'aéronef.
     * @param position
     *          La nouvelle position géographique de l'aéronef.
     */
    void setPosition(GeoPos position);

    /**
     * Met à jour l'altitude de l'aéronef.
     * @param altitude
     *          La nouvelle altitude de l'aéronef.
     */
    void setAltitude(double altitude);

    /**
     * Met à jour la vitesse de l'aéronef.
     * @param velocity
     *          La nouvelle vitesse de l'aéronef.
     */
    void setVelocity(double velocity);

    /**
     * Met à jour le cap / l'orientation de l'aéronef.
     * @param trackOrHeading
     *          Le nouveau cap / orientation de l'aéronef.
     */
    void setTrackOrHeading(double trackOrHeading);

}
