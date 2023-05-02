package ch.epfl.javions.gui;

import ch.epfl.javions.GeoPos;
import ch.epfl.javions.WebMercator;
import ch.epfl.javions.aircraft.AircraftData;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableSet;
import javafx.scene.Group;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.Pane;
import javafx.scene.shape.SVGPath;

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

    }

    private void initializePane() {
        pane = new Pane(new Canvas());
        pane.getStylesheets().add(CSS_FILE);
        pane.setPickOnBounds(false);
    }

    private void annotatedAircraft(ObservableAircraftState aircraftStates) {
        Group label = label(aircraftStates);

    }

    private ReadOnlyDoubleProperty xScreen(SimpleObjectProperty<GeoPos> positionProperty) {
        double x = WebMercator.x(mapParameters.getZoom(), positionProperty.getValue().longitude())
                - mapParameters.minXProperty().get();
        return new SimpleDoubleProperty(x);
    }

    private ReadOnlyDoubleProperty yScreen(SimpleObjectProperty<GeoPos> positionProperty) {
        double y = WebMercator.y(mapParameters.getZoom(), positionProperty.getValue().latitude())
                - mapParameters.minYProperty().get();
        return new SimpleDoubleProperty(y);
    }
    private Group label(ObservableAircraftState aircraftState) {
        Group label = new Group(icon(aircraftState));
        label.getStyleClass().add(LABEL_CLASS);

        SimpleObjectProperty<GeoPos> positionProperty = new SimpleObjectProperty<>();
        positionProperty.bind(aircraftState.positionProperty());

        label.layoutXProperty().bind(xScreen(positionProperty));
        label.layoutYProperty().bind(yScreen(positionProperty));
        return label;
    }

    private SVGPath icon(ObservableAircraftState aircraftState) {
        AircraftData data = aircraftState.getData();
        int category = aircraftState.getCategory();

        AircraftIcon aircraftIcon = AircraftIcon.iconFor(data.typeDesignator(),
                data.description(),
                category,
                data.wakeTurbulenceCategory());

        SVGPath icon = new SVGPath();
        icon.setContent(aircraftIcon.svgPath());
        icon.setStyle(pane.getStyle());
        return icon;
    }


}
