package ch.epfl.javions.gui;

import ch.epfl.javions.GeoPos;
import ch.epfl.javions.adsb.AircraftStateSetter;
import ch.epfl.javions.adsb.CallSign;
import ch.epfl.javions.aircraft.AircraftData;
import ch.epfl.javions.aircraft.IcaoAddress;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.Objects;

/**
 * La classe ObservableAircraftState du sous-paquetage gui, représente l'état d'un aéronef.
 * Cet état a la caractéristique d'être observable au sens du patron de conception Observer.
 *
 * @author Adam AIT BOUSSELHAM (356365)
 * @author Abdellah JANATI IDRISSI (362341)
 */
public final class ObservableAircraftState implements AircraftStateSetter {

    private final IcaoAddress address;
    private final AircraftData data;
    private final LongProperty lastMessageTimeStampNs = new SimpleLongProperty(-1L);
    private final IntegerProperty category = new SimpleIntegerProperty(-1);
    private final ObjectProperty<CallSign> callSign = new SimpleObjectProperty<>(null);
    private final ObjectProperty<GeoPos> position = new SimpleObjectProperty<>(null);
    private final ObservableList<AirbornePos> trajectory = FXCollections.observableArrayList();
    private final ObservableList<AirbornePos> unmodifiableTrajectory = FXCollections.unmodifiableObservableList(trajectory);
    private final DoubleProperty altitude = new SimpleDoubleProperty(Double.NaN);
    private final DoubleProperty velocity = new SimpleDoubleProperty(Double.NaN);
    private final DoubleProperty trackOrHeading = new SimpleDoubleProperty(Double.NaN);
    private long curentMessageTimeStampNsTrajectory;

    /**
     * Constructeur de ObservableAircraftState caractérisant l'état d'un aéronef.
     *
     * @param address L'adresse ICAO de l'aéronef dont l'état est destiné
     *                à être représenté par l'instance à créer.
     * @param data    Les caractéristiques fixes de cet aéronef,
     *                provenant de la base de données mictronics.
     * @throws NullPointerException si L'adresse ICAO de l'aéronef passée en argument est nulle.
     */
    public ObservableAircraftState(IcaoAddress address, AircraftData data) {
        Objects.requireNonNull(address);
        this.address = address;
        this.data = data;
    }

    /**
     * Méthode d'accès à l'adresse ICAO de l'aéronef dont l'état est destiné
     * à être représenté par l'instance à créer.
     *
     * @return L'adresse ICAO de l'aéronef.
     */
    public IcaoAddress getAddress() {
        return address;
    }

    /**
     * Méthode d'accès aux caractéristiques fixes de cet aéronef,
     * provenant de la base de données mictronics.
     *
     * @return Les caractéristiques de l'aéronef.
     */
    public AircraftData getData() {
        return data;
    }

    /**
     * Méthode d'accès à la propriété lastMessageTimeStampNs en lecture seule.
     *
     * @return La propriété lastMessageTimeStampNs.
     */
    public ReadOnlyLongProperty lastMessageTimeStampNsProperty() {
        return lastMessageTimeStampNs;
    }

    /**
     * Méthode d'accès à la propriété category en lecture seule.
     *
     * @return La propriété category.
     */
    public ReadOnlyIntegerProperty categoryProperty() {
        return category;
    }

    /**
     * Méthode d'accès à la propriété callSign en lecture seule.
     *
     * @return La propriété callSign.
     */
    public ReadOnlyObjectProperty<CallSign> callSignProperty() {
        return callSign;
    }

    /**
     * Méthode d'accès à la propriété position en lecture seule.
     *
     * @return La propriété position.
     */
    public ReadOnlyObjectProperty<GeoPos> positionProperty() {
        return position;
    }

    /**
     * Méthode d'accès à la propriété altitude en lecture seule.
     *
     * @return La propriété altitude.
     */
    public ReadOnlyDoubleProperty altitudeProperty() {
        return altitude;
    }

    /**
     * @return La propriété velocity.
     */
    public ReadOnlyDoubleProperty velocityProperty() {
        return velocity;
    }

    /**
     * Méthode d'accès à la propriété trackOrHeading en lecture seule.
     *
     * @return La propriété trackOrHeading.
     */
    public ReadOnlyDoubleProperty trackOrHeadingProperty() {
        return trackOrHeading;
    }

    /**
     * Méthode d'accès à la valeur contenue dans la propriété lastMessageTimeStampNs.
     *
     * @return La valeur contenue dans la propriété lastMessageTimeStampNs.
     */
    public long getLastMessageTimeStampNs() {
        return lastMessageTimeStampNsProperty().get();
    }

    /**
     * Méthode de modification de la valeur contenue dans la propriété lastMessageTimeStampNs.
     *
     * @param timeStampNs Le nouveau timestamp en nanosecondes.
     */
    @Override
    public void setLastMessageTimeStampNs(long timeStampNs) {
        lastMessageTimeStampNs.set(timeStampNs);
    }

    /**
     * Méthode d'accès à la valeur contenue dans la propriété category.
     *
     * @return La valeur contenue dans la propriété category.
     */
    public int getCategory() {
        return categoryProperty().get();
    }

    /**
     * Méthode de modification de la valeur contenue dans la propriété category.
     *
     * @param category La nouvelle catégorie de l'aéronef.
     */
    @Override
    public void setCategory(int category) {
        this.category.set(category);
    }

    /**
     * Méthode d'accès à la valeur contenue dans la propriété callSign.
     *
     * @return La valeur contenue dans la propriété callSign.
     */
    public CallSign getCallSign() {
        return callSignProperty().get();
    }

    /**
     * Méthode de modification de la valeur contenue dans la propriété callSign.
     *
     * @param callSign Le nouveau call sign de l'aéronef.
     */
    @Override
    public void setCallSign(CallSign callSign) {
        this.callSign.set(callSign);
    }

    /**
     * Méthode d'accès à la valeur contenue dans la propriété position.
     *
     * @return La valeur contenue dans la propriété position.
     */
    public GeoPos getPosition() {
        return positionProperty().get();
    }

    /**
     * Méthode de modification de la valeur contenue dans la propriété position.
     *
     * @param position La nouvelle position géographique de l'aéronef.
     */
    @Override
    public void setPosition(GeoPos position) {
        this.position.set(position);
        if(!(Double.isNaN(getAltitude())))
            trajectory.add(new AirbornePos(position,getAltitude()));
    }

    /**
     * Méthode d'accès à la liste observable mais non modifiable unmodifiableTrajectory.
     *
     * @return La liste observable mais non modifiable unmodifiableTrajectory.
     */
    public ObservableList<AirbornePos> getTrajectory() {
        return unmodifiableTrajectory;
    }

    /**
     * Méthode d'accès à la valeur contenue dans la propriété altitude.
     *
     * @return la valeur contenue dans la propriété altitude.
     */
    public double getAltitude() {
        return altitudeProperty().get();
    }

    /**
     * Méthode de modification de la valeur contenue dans la propriété altitude.
     *
     * @param altitude La nouvelle altitude de l'aéronef.
     */
    @Override
    public void setAltitude(double altitude) {
        this.altitude.set(altitude);
        if(Objects.nonNull(getPosition())) {
            AirbornePos pos = new AirbornePos(getPosition(),altitude);
            if(trajectory.isEmpty()) trajectory.add(pos);
            if (getLastMessageTimeStampNs() == curentMessageTimeStampNsTrajectory) {
                trajectory.set(trajectory.size() - 1, pos);
            }
        }
    }

    /**
     * Méthode d'accès à la valeur contenue dans la propriété velocity.
     *
     * @return La valeur contenue dans la propriété velocity.
     */
    public double getVelocity() {
        return velocityProperty().get();
    }

    /**
     * Méthode de modification de la valeur contenue dans la propriété velocity.
     *
     * @param velocity La nouvelle vitesse de l'aéronef.
     */
    @Override
    public void setVelocity(double velocity) {
        this.velocity.set(velocity);
    }

    /**
     * Méthode d'accès à la valeur contenue dans la propriété trackOrHeading.
     *
     * @return La valeur contenue dans la propriété trackOrHeading.
     */
    public double getTrackOrHeading() {
        return trackOrHeadingProperty().get();
    }

    /**
     * Méthode de modification de la valeur contenue dans la propriété trackOrHeading.
     *
     * @param trackOrHeading Le nouveau cap / orientation de l'aéronef.
     */
    @Override
    public void setTrackOrHeading(double trackOrHeading) {
        this.trackOrHeading.set(trackOrHeading);
    }

    /**
     * Enregistrement imbriqué dans ObservableAircraftState, utilisé pour représenter
     * des positions dans l'espace que l'aéronef a occupées depuis le premier message reçu (donc sa trajectoire).
     * Chaque élément de la trajectoire est une paire constituée d'une position à la surface de la Terre
     * (longitude et latitude) ainsi qu'une altitude.
     *
     * @param position La longitude de l'aéronef exprimé en T32.
     * @param altitude La latitude de l'aéronef exprimé en T32.
     */
    public record AirbornePos(GeoPos position, double altitude) {
    }
}
