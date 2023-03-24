package ch.epfl.javions.adsb;

import ch.epfl.javions.aircraft.IcaoAddress;

/**
 * Interface définissant la structure d'un message ADS-B.
 *
 * @author Adam AIT BOUSSELHAM (356365)
 * @author Abdellah JANATI IDRISSI (362341)
 */
public interface Message {

    /**
     * Retourne le timestamp en nanosecondes du message.
     *
     * @return le timestamp en nanosecondes du message.
     */
    long timeStampNs();

    /**
     * Retourne l'adresse ICAO de l'aéronef émetteur du message.
     *
     * @return l'adresse ICAO de l'aéronef.
     */
    IcaoAddress icaoAddress();
}
