package ch.epfl.javions.gui;

import ch.epfl.javions.GeoPos;
import ch.epfl.javions.adsb.AircraftStateSetter;
import ch.epfl.javions.adsb.CallSign;
import ch.epfl.javions.aircraft.AircraftData;
import ch.epfl.javions.aircraft.IcaoAddress;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.Pair;

import java.util.List;
import java.util.Objects;

public final class ObservableAircraftState implements AircraftStateSetter {

    private final IcaoAddress address;
    private final AircraftData data;
    private final LongProperty lastMessageTimeStampNs = new SimpleLongProperty(-1L);
    private final IntegerProperty category = new SimpleIntegerProperty(-1);
    private final ObjectProperty<CallSign> callSign = new SimpleObjectProperty<>(null);
    private final ObjectProperty<GeoPos> position = new SimpleObjectProperty<>(null);
    private final ObservableList<Pair<AirbornePos, Double>> trajectory = FXCollections.observableArrayList();
    private final ObservableList<Pair<AirbornePos, Double>> unmodifiableTrajectory = FXCollections.unmodifiableObservableList(trajectory);
    private final ListProperty<Pair<AirbornePos, Double>> unmodifiableTrajectoryProperty = new SimpleListProperty<>(unmodifiableTrajectory);
    private final DoubleProperty altitude = new SimpleDoubleProperty(Double.NaN);
    private final DoubleProperty velocity = new SimpleDoubleProperty(Double.NaN);
    private final DoubleProperty trackOrHeading = new SimpleDoubleProperty(Double.NaN);
    private long curentMessageTimeStampNsTrajectory;
    public ObservableAircraftState(IcaoAddress address, AircraftData data) {
        Objects.requireNonNull(address);
        this.address = address;
        this.data = data;
    }

    public ReadOnlyLongProperty lastMessageTimeStampNsProperty() {
        return lastMessageTimeStampNs;
    }

    public ReadOnlyIntegerProperty categoryProperty() {
        return category;
    }

    public ReadOnlyObjectProperty<CallSign> callSignProperty() {
        return callSign;
    }

    public ReadOnlyObjectProperty<GeoPos> positionProperty() {
        return position;
    }

    public ObservableList<Pair<AirbornePos, Double>> trajectoryProperty() {
        return unmodifiableTrajectoryProperty;
    }

    public ReadOnlyDoubleProperty altitudeProperty() {
        return altitude;
    }

    public ReadOnlyDoubleProperty velocityProperty() {
        return velocity;
    }

    public ReadOnlyDoubleProperty trackOrHeadingProperty() {
        return trackOrHeading;
    }

    public long getLastMessageTimeStampNs() {
        return lastMessageTimeStampNs.get();
    }

    @Override
    public void setLastMessageTimeStampNs(long timeStampNs) {
        lastMessageTimeStampNs.set(timeStampNs);
    }

    public int getCategory() {
        return category.get();
    }

    @Override
    public void setCategory(int category) {
        this.category.set(category);
    }

    public CallSign getCallSign() {
        return callSign.get();
    }

    @Override
    public void setCallSign(CallSign callSign) {
        this.callSign.set(callSign);
    }

    public GeoPos getPosition() {
        return position.get();
    }

    @Override
    public void setPosition(GeoPos position) {
        this.position.set(position);
        AirbornePos potentialPos = new AirbornePos(position.longitudeT32(), position.latitudeT32());
        if (trajectory.isEmpty() || !(trajectory.get(trajectory.size() - 1).getKey().equals(potentialPos))) {
            trajectory.add(new Pair<>(potentialPos, getAltitude()));
            curentMessageTimeStampNsTrajectory = getLastMessageTimeStampNs();
        }
    }

    public List<Pair<AirbornePos, Double>> getTrajectory() {
        return unmodifiableTrajectory.stream().toList();
    }

    public double getAltitude() {
        return altitude.get();
    }

    @Override
    public void setAltitude(double altitude) {
        this.altitude.set(altitude);
        if (getLastMessageTimeStampNs() == curentMessageTimeStampNsTrajectory) {
            AirbornePos pos = new AirbornePos(getPosition().longitudeT32(), getPosition().latitudeT32());
            trajectory.set(trajectory.size() - 1, new Pair<>(pos, altitude));
        }
    }

    public double getVelocity() {
        return velocity.get();
    }

    @Override
    public void setVelocity(double velocity) {
        this.velocity.set(velocity);
    }

    public double getTrackOrHeading() {
        return trackOrHeading.get();
    }

    @Override
    public void setTrackOrHeading(double trackOrHeading) {
        this.trackOrHeading.set(trackOrHeading);
    }

    public IcaoAddress getAddress() {
        return address;
    }

    public AircraftData getData() {
        return data;
    }

    public record AirbornePos(double longitudeT32, double latitudeT32) {
    }
}
