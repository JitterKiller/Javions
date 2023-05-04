package ch.epfl.javions.gui;

import ch.epfl.javions.GeoPos;
import ch.epfl.javions.Units;
import ch.epfl.javions.WebMercator;
import ch.epfl.javions.aircraft.AircraftData;
import ch.epfl.javions.aircraft.AircraftDescription;
import ch.epfl.javions.aircraft.AircraftTypeDesignator;
import ch.epfl.javions.aircraft.WakeTurbulenceCategory;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.collections.*;
import javafx.scene.Group;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;

public final class AircraftController {


    private static final String CSS_FILE = "aircraft.css";
    private static final String TRAJECTORY_CLASS = "trajectory";
    private static final String AIRCRAFT_CLASS = "aircraft";
    private static final String LABEL_CLASS = "label";
    private final MapParameters mapParameters;
    private final ObservableSet<ObservableAircraftState> aircraftStates;
    private ObjectProperty<ObservableAircraftState> state;
    private Pane pane;

    public AircraftController(MapParameters mapParameters,
                              ObservableSet<ObservableAircraftState> aircraftStates,
                              ObjectProperty<ObservableAircraftState> state) {

        this.mapParameters = mapParameters;
        this.aircraftStates = aircraftStates;
        this.state = state;
        initializePane();
        addAllAnnotatedAircraft(aircraftStates);
        aircraftStates.addListener((SetChangeListener<ObservableAircraftState>)
                change -> {
                    if(change.wasAdded()){
                        annotatedAircraft(change.getElementAdded());
                    }
                    if(change.wasRemoved()){
                        pane.getChildren().remove(pane.lookup("#"+
                                change.getElementRemoved().getAddress().string()));
                    }
                });
    }

    private void initializePane() {
        pane = new Pane(new Canvas());
        pane.getStylesheets().add(CSS_FILE);
        pane.setPickOnBounds(false);
    }

    private void addAllAnnotatedAircraft(ObservableSet<ObservableAircraftState> aircraftStates) {
        for(ObservableAircraftState aircraftState: aircraftStates){
            annotatedAircraft(aircraftState);
            aircraftStates.add(aircraftState);
        }
    }

    private void annotatedAircraft(ObservableAircraftState aircraftState) {
        Group annotated = new Group(trajectory(aircraftState),labelIcon(aircraftState));
        annotated.setId(aircraftState.getAddress().string());
        annotated.viewOrderProperty().bind(aircraftState.altitudeProperty().negate());
        pane.getChildren().add(annotated);
    }

    private Group trajectory(ObservableAircraftState aircraftState){
        Group trajectory = new Group();
        ObservableList<ObservableAircraftState.AirbornePos> trajectoryList = aircraftState.getTrajectory();

        aircraftState.getTrajectory().addListener((ListChangeListener<? super ObservableAircraftState.AirbornePos>)
                change -> {
                    trajectory.getChildren().clear();
                    trajectoryUpdate(aircraftState, trajectory, trajectoryList);
                });

        mapParameters.zoomProperty().addListener((ChangeListener<? super Number>)
                (p, oldS, newS) -> {
                    trajectory.getChildren().clear();
                    trajectoryUpdate(aircraftState, trajectory, trajectoryList);
                });

        trajectory.visibleProperty().addListener((ChangeListener<? super Boolean>)
                (p, oldS, newS) -> {
                    trajectory.getChildren().clear();
                    trajectoryUpdate(aircraftState, trajectory, trajectoryList);
                });


        trajectory.layoutXProperty().bind(Bindings.createDoubleBinding(
                mapParameters::getMinX,
                mapParameters.minXProperty()
        ).negate());
        trajectory.layoutYProperty().bind(Bindings.createDoubleBinding(
                mapParameters::getMinY,
                mapParameters.minYProperty()
        ).negate());
        trajectory.visibleProperty().bind(Bindings.createBooleanBinding(
                () -> aircraftState.equals(state.get()),
                state
        ));
        trajectory.getStyleClass().add(TRAJECTORY_CLASS);

        return trajectory;
    }

    private void trajectoryUpdate(ObservableAircraftState aircraftState, Group trajectory, ObservableList<ObservableAircraftState.AirbornePos> trajectoryList) {
        if(aircraftState.equals(state.get()) && trajectory.isVisible()){
            for(int i = 0; i < trajectoryList.size() - 1; ++i){
                Line line = new Line();
                line.setStartX(WebMercator.x(mapParameters.getZoom(),trajectoryList.get(i).position().longitude()));
                line.setStartY(WebMercator.y(mapParameters.getZoom(),trajectoryList.get(i).position().latitude()));
                line.setEndX(WebMercator.x(mapParameters.getZoom(),trajectoryList.get(i+1).position().longitude()));
                line.setEndY(WebMercator.y(mapParameters.getZoom(),trajectoryList.get(i+1).position().latitude()));
                trajectory.getChildren().add(line);
                }
            }
    }

    private Group labelIcon(ObservableAircraftState aircraftState) {
        Group labelIcon = new Group(label(aircraftState),icon(aircraftState));
        labelIconBinds(labelIcon,aircraftState.positionProperty());
        return labelIcon;
    }

    private Group label(ObservableAircraftState aircraftState) {
        Text t = new Text();
        t.textProperty().bind(Bindings.createStringBinding(
                () -> getFirstLine(aircraftState) + "\n" + getSecondLine(aircraftState),
                aircraftState.velocityProperty(),
                aircraftState.altitudeProperty()));

        Rectangle r = new Rectangle();
        r.widthProperty().bind(t.layoutBoundsProperty().map(b -> b.getWidth() + 4));
        r.heightProperty().bind(t.layoutBoundsProperty().map(b -> b.getHeight() + 4));


        Group label = new Group(r,t);
        label.getStyleClass().add(LABEL_CLASS);
        label.visibleProperty().bind(Bindings.createBooleanBinding(
                () -> mapParameters.getZoom() >= 11,mapParameters.zoomProperty()
        ));

        return label;
    }

    private void labelIconBinds(Group labelIcon, ReadOnlyObjectProperty<GeoPos> positionProperty ){
        labelIcon.layoutXProperty().bind(Bindings.createDoubleBinding(
                () -> xScreen(positionProperty),
                mapParameters.zoomProperty(),
                mapParameters.minXProperty(),
                positionProperty));
        labelIcon.layoutYProperty().bind(Bindings.createDoubleBinding(
                () -> yScreen(positionProperty),
                mapParameters.zoomProperty(),
                mapParameters.minYProperty(),
                positionProperty));
    }

    private SVGPath icon(ObservableAircraftState aircraftState) {
        AircraftData data = aircraftState.getData();
        int category = aircraftState.getCategory();

        AircraftIcon aircraftIcon;
        SVGPath icon = new SVGPath();

        if(data != null){
            aircraftIcon = AircraftIcon.iconFor(data.typeDesignator(),
                    data.description(),
                    category,
                    data.wakeTurbulenceCategory());
        } else {
            aircraftIcon = AircraftIcon.iconFor(new AircraftTypeDesignator("")
                    ,new AircraftDescription("")
                    ,category
                    , WakeTurbulenceCategory.UNKNOWN);
        }

        icon.setStyle(pane.getStyle());
        icon.setContent(aircraftIcon.svgPath());
        icon.rotateProperty().bind(Bindings.createDoubleBinding(
                () -> aircraftIcon.canRotate() ? Units.convertTo(aircraftState.getTrackOrHeading(), Units.Angle.DEGREE)
                        : 0,aircraftState.trackOrHeadingProperty()
        ));
        icon.setOnMousePressed(e -> state = new SimpleObjectProperty<>(aircraftState));
        return icon;
    }


    private double xScreen(ReadOnlyObjectProperty<GeoPos> positionProperty) {
        return WebMercator.x(mapParameters.getZoom(), positionProperty.getValue().longitude())
                - mapParameters.getMinX();
    }

    private double yScreen(ReadOnlyObjectProperty<GeoPos> positionProperty) {
        return WebMercator.y(mapParameters.getZoom(), positionProperty.getValue().latitude())
                - mapParameters.getMinY();
    }

    private String getFirstLine(ObservableAircraftState aircraftState) {
        AircraftData data = aircraftState.getData();
        if(data == null) {
            return aircraftState.getAddress().string();
        }
       return data.registration().string().isEmpty() ? data.typeDesignator().string() : data.registration().string();
    }

    private String getSecondLine(ObservableAircraftState aircraftState) {
        StringBuilder b = new StringBuilder();
        if(!(Double.isNaN(aircraftState.getVelocity()))) {
            b.append((int) Math.rint(Units.convertTo(aircraftState.getVelocity(), Units.Speed.KILOMETER_PER_HOUR)));
        }
        else b.append("?");
        b.append("km/h" + "\u2002").append((int) Math.rint(aircraftState.getAltitude())).append("m");
        return b.toString();
    }

    public Pane pane() {
        return pane;
    }

}
