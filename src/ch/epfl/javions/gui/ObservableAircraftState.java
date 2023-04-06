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

    public record AirbornePos (double longitude, double latitude) { }

    private final IcaoAddress address;
    private final AircraftData data;
    private final LongProperty lastMessageTimeStampNs = new SimpleLongProperty(-1L);
    private final IntegerProperty category = new SimpleIntegerProperty(-1);
    private final ObjectProperty<CallSign> callSign = new SimpleObjectProperty<>(null);
    private final ObjectProperty<GeoPos> position = new SimpleObjectProperty<>(null);
    private final ObservableList<Pair<AirbornePos, Double>> trajectory = FXCollections.observableArrayList();
    private final ObservableList<Pair<AirbornePos, Double>> unmodifiableTrajectory = FXCollections.unmodifiableObservableList(trajectory);
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
        return unmodifiableTrajectory;
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
    public int getCategory() {
        return category.get();
    }
    public CallSign getCallSign() {
        return callSign.get();
    }
    public GeoPos getPosition() {
        return position.get();
    }
    public List<Pair<AirbornePos, Double>> getTrajectory() {
        return unmodifiableTrajectory.stream().toList();
    }
    public double getAltitude() {
        return altitude.get();
    }
    public double getVelocity() {
        return velocity.get();
    }
    public double getTrackOrHeading() {
        return trackOrHeading.get();
    }

    public IcaoAddress getAddress() {
        return address;
    }

    public AircraftData getData() {
        return data;
    }

    @Override
    public void setLastMessageTimeStampNs(long timeStampNs) {
        lastMessageTimeStampNs.set(timeStampNs);
    }
    @Override
    public void setCategory(int category) {
        this.category.set(category);
    }
    @Override
    public void setCallSign(CallSign callSign) {
        this.callSign.set(callSign);
    }
    @Override
    public void setPosition(GeoPos position) {
        this.position.set(position);
        AirbornePos potentialPos = new AirbornePos(position.longitude(),position.latitude());
        if (trajectory.isEmpty() || !(trajectory.get(trajectory.size() - 1).getKey().equals(potentialPos))) {
            trajectory.add(new Pair<>(potentialPos,getAltitude()));
            curentMessageTimeStampNsTrajectory = getLastMessageTimeStampNs();
        }
    }
    @Override
    public void setAltitude(double altitude) {
        this.altitude.set(altitude);
        if(getLastMessageTimeStampNs() == curentMessageTimeStampNsTrajectory) {
            AirbornePos pos = new AirbornePos(getPosition().longitude(),getPosition().latitude());
            trajectory.set(trajectory.size() - 1,new Pair<>(pos,altitude));
        }
    }
    @Override
    public void setVelocity(double velocity) {
        this.velocity.set(velocity);
    }
    @Override
    public void setTrackOrHeading(double trackOrHeading) {
        this.trackOrHeading.set(trackOrHeading);
    }
}
