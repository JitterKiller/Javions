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
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
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
    private final ObservableAircraftState state;
    private Pane pane;

    public AircraftController(MapParameters mapParameters,
                              ObservableSet<ObservableAircraftState> aircraftStates,
                              ObjectProperty<ObservableAircraftState> state) {

        this.mapParameters = mapParameters;
        this.aircraftStates = aircraftStates;
        this.state = state.get();
        initializePane();
        addAnnotatedAircrafts(aircraftStates);
        aircraftStates.addListener((SetChangeListener<ObservableAircraftState>)
                change -> {
                    if(change.wasAdded()){
                        annotatedAircraft(change.getElementAdded());
                    }
                    if(change.wasRemoved()){
                        System.out.println("un changement a été détécté");
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

    private void addAnnotatedAircrafts(ObservableSet<ObservableAircraftState> aircraftStates) {
        for(ObservableAircraftState aircraftState: aircraftStates){
            annotatedAircraft(aircraftState);
        }
    }

    private void annotatedAircraft(ObservableAircraftState aircraftState) {
        //Group trajectory = trajectory(aircraftState);
        //Group annotated = new Group(label,trajectory);
        Group annotated = new Group(labelIcon(aircraftState));
        annotated.setId(aircraftState.getAddress().string());
        annotated.viewOrderProperty().bind(aircraftState.altitudeProperty().negate());
        pane.getChildren().add(annotated);
    }

    private Group trajectory(ObservableAircraftState aircraftState){
        Line line = new Line();
        return new Group();
    }

    private Group labelIcon(ObservableAircraftState aircraftState) {
        Group labelIcon = new Group(label(aircraftState),icon(aircraftState));
        SimpleObjectProperty<GeoPos> positionProperty = new SimpleObjectProperty<>();
        positionProperty.bind(aircraftState.positionProperty());
        labelIconBinds(labelIcon,positionProperty);
        return labelIcon;
    }

    private Group label(ObservableAircraftState aircraftState) {
        Group label = new Group();
        label.getStyleClass().add(LABEL_CLASS);

        Text t = new Text();
        t.textProperty().bind(Bindings.createStringBinding(
                () -> getFirstLine(aircraftState) + "\n" + getSecondLine(aircraftState), aircraftStates));

        Rectangle r = new Rectangle();
        r.widthProperty().bind(t.layoutBoundsProperty().map(b -> b.getWidth() + 4));
        r.heightProperty().bind(t.layoutBoundsProperty().map(b -> b.getHeight() + 4));

        return label;
    }

    private void labelIconBinds(Group labelIcon, SimpleObjectProperty<GeoPos> positionProperty ){
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

        icon.setContent(aircraftIcon.svgPath());
        icon.setStyle(pane.getStyle());
        icon.rotateProperty().bind(Bindings.createDoubleBinding(
                () -> aircraftIcon.canRotate() ? Units.convertTo(aircraftState.getTrackOrHeading(), Units.Angle.DEGREE)
                        : 0,aircraftState.trackOrHeadingProperty()
        ));
        return icon;
    }


    private double xScreen(SimpleObjectProperty<GeoPos> positionProperty) {
        return WebMercator.x(mapParameters.getZoom(), positionProperty.getValue().longitude())
                - mapParameters.minXProperty().get();
    }

    private double yScreen(SimpleObjectProperty<GeoPos> positionProperty) {
        return WebMercator.y(mapParameters.getZoom(), positionProperty.getValue().latitude())
                - mapParameters.minYProperty().get();
    }

    private String getFirstLine(ObservableAircraftState aircraftState) {
        AircraftData data = aircraftState.getData();
        if(data == null) {
            return aircraftState.getAddress().string();
        }
        if (data.typeDesignator() == null) {
            return aircraftState.getData().model();
        }
        else return data.typeDesignator().string();
    }

    private String getSecondLine(ObservableAircraftState aircraftState) {
        StringBuilder b = new StringBuilder();
        if(Double.isNaN(aircraftState.getVelocity())) b.append(aircraftState.getVelocity());
        else b.append("?");
        b.append("km/h" + "\u2002").append(aircraftState.getAltitude()).append("m");
        return b.toString();
    }
    private void listenHandlers() {
    }

    public Pane pane() {
        return pane;
    }

}
