package ch.epfl.javions.gui;

import ch.epfl.javions.GeoPos;
import ch.epfl.javions.Units;
import ch.epfl.javions.WebMercator;
import ch.epfl.javions.aircraft.AircraftData;
import ch.epfl.javions.aircraft.AircraftDescription;
import ch.epfl.javions.aircraft.AircraftTypeDesignator;
import ch.epfl.javions.aircraft.WakeTurbulenceCategory;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableObjectValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.scene.Group;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;

import static javafx.scene.paint.CycleMethod.NO_CYCLE;

public final class AircraftController {
    private static final String CSS_FILE = "aircraft.css";
    private static final String TRAJECTORY_CLASS = "trajectory";
    private static final String ICON_CLASS = "aircraft";
    private static final String LABEL_CLASS = "label";
    private static final int MAX_ALT = 12_000;
    private static final int MIN_ZOOM_LABEL_VISIBLE = 11;
    private static final int RECT_OFFSET = 4;
    private final MapParameters mapParameters;
    private final ObjectProperty<ObservableAircraftState> selectedAircraft;
    private Pane pane;

    public AircraftController(MapParameters mapParameters,
                              ObservableSet<ObservableAircraftState> aircraftStates,
                              ObjectProperty<ObservableAircraftState> selectedAircraft) {

        this.mapParameters = mapParameters;
        this.selectedAircraft = selectedAircraft;
        initializePane();
        addAllAnnotatedAircraft(aircraftStates);
        aircraftStates.addListener((SetChangeListener<ObservableAircraftState>)
                change -> {
                    if (change.wasAdded()) {
                        annotatedAircraft(change.getElementAdded());
                    }
                    if (change.wasRemoved()) {
                        pane.getChildren().removeIf(
                                e -> change.getElementRemoved().getAddress().string().equals(e.getId())
                        );
                    }
                });
    }

    private ReadOnlyObjectProperty<ObservableAircraftState> selectedAircraftProperty() {
        return selectedAircraft;
    }

    private ObservableAircraftState getSelectedAircraft() {
        return selectedAircraftProperty().get();
    }

    private void setSelectedAircraft(ObservableAircraftState selectedAircraft) {
        this.selectedAircraft.set(selectedAircraft);
    }

    private void initializePane() {
        pane = new Pane(new Canvas());
        pane.getStylesheets().add(CSS_FILE);
        pane.setPickOnBounds(false);
    }

    private void addAllAnnotatedAircraft(ObservableSet<ObservableAircraftState> aircraftStates) {
        for (ObservableAircraftState aircraftState : aircraftStates) {
            annotatedAircraft(aircraftState);
        }
    }

    private void annotatedAircraft(ObservableAircraftState aircraftState) {
        Group annotated = new Group(trajectory(aircraftState), labelIcon(aircraftState));
        annotated.setId(aircraftState.getAddress().string());
        annotated.viewOrderProperty().bind(aircraftState.altitudeProperty().negate());
        pane.getChildren().add(annotated);
    }

    private Group trajectory(ObservableAircraftState aircraftState) {
        Group trajectory = new Group();

        trajectory.visibleProperty().bind(Bindings.createBooleanBinding(
                () -> aircraftState.equals(getSelectedAircraft()), selectedAircraftProperty()));

        trajectory.layoutXProperty().bind(Bindings.createDoubleBinding(
                mapParameters::getMinX, mapParameters.minXProperty()).negate());

        trajectory.layoutYProperty().bind(Bindings.createDoubleBinding(
                mapParameters::getMinY, mapParameters.minYProperty()).negate());

        aircraftState.getTrajectory().addListener((ListChangeListener<? super ObservableAircraftState.AirbornePos>)
                change -> trajectoryUpdate(trajectory, aircraftState.getTrajectory()));

        mapParameters.zoomProperty().addListener((ChangeListener<? super Number>)
                (p, oldS, newS) -> trajectoryUpdate(trajectory, aircraftState.getTrajectory()));

        trajectory.visibleProperty().addListener((ChangeListener<? super Boolean>)
                (p, oldS, newS) -> trajectoryUpdate(trajectory, aircraftState.getTrajectory()));


        trajectory.getStyleClass().add(TRAJECTORY_CLASS);

        return trajectory;
    }

    private void trajectoryUpdate(Group trajectory, ObservableList<ObservableAircraftState.AirbornePos> trajectoryList) {
        trajectory.getChildren().clear();
        if(trajectory.isVisible()) {
            for (int i = 0; i < trajectoryList.size() - 1; ++i) {
                Line line = new Line();
                line.setStartX(WebMercator.x(mapParameters.getZoom(), trajectoryList.get(i).position().longitude()));
                line.setStartY(WebMercator.y(mapParameters.getZoom(), trajectoryList.get(i).position().latitude()));
                line.setEndX(WebMercator.x(mapParameters.getZoom(), trajectoryList.get(i + 1).position().longitude()));
                line.setEndY(WebMercator.y(mapParameters.getZoom(), trajectoryList.get(i + 1).position().latitude()));
                if (trajectoryList.get(i).position().equals(trajectoryList.get(i + 1).position())) {
                    line.setStroke(getColor(trajectoryList.get(i).altitude()));
                } else {
                    Stop s1 = new Stop(0, getColor(trajectoryList.get(i).altitude()));
                    Stop s2 = new Stop(1, getColor(trajectoryList.get(i + 1).altitude()));
                    line.setStroke(new LinearGradient(0, 0, 1, 0, true, NO_CYCLE, s1, s2));
                }
                trajectory.getChildren().add(line);
            }
        }
    }

    private Group labelIcon(ObservableAircraftState aircraftState) {
        Group labelIcon = new Group(label(aircraftState), icon(aircraftState));
        labelIconBinds(labelIcon, aircraftState.positionProperty());
        return labelIcon;
    }

    private void labelIconBinds(Group labelIcon, ReadOnlyObjectProperty<GeoPos> positionProperty) {
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

    private Group label(ObservableAircraftState aircraftState) {
        Text t = new Text();
        t.textProperty().bind(Bindings.createStringBinding(
                () -> getFirstLine(aircraftState) + "\n" + getSecondLine(aircraftState),
                aircraftState.velocityProperty(),
                aircraftState.altitudeProperty()));

        Rectangle r = new Rectangle();
        r.widthProperty().bind(t.layoutBoundsProperty().map(b -> b.getWidth() + RECT_OFFSET));
        r.heightProperty().bind(t.layoutBoundsProperty().map(b -> b.getHeight() + RECT_OFFSET));


        Group label = new Group(r, t);
        label.getStyleClass().add(LABEL_CLASS);
        label.visibleProperty().bind(Bindings.createBooleanBinding(
                () -> mapParameters.getZoom() >= MIN_ZOOM_LABEL_VISIBLE
                        || aircraftState.equals(getSelectedAircraft()),
                mapParameters.zoomProperty(),
                selectedAircraftProperty()
        ));

        return label;
    }

    private SVGPath icon(ObservableAircraftState aircraftState) {
        AircraftData data = aircraftState.getData();

        ObjectProperty<AircraftIcon> aircraftIconProperty = new SimpleObjectProperty<>();
        SVGPath icon = new SVGPath();

        aircraftIconProperty.bind(aircraftState.categoryProperty().map(
                (category) -> {
                    AircraftIcon aircraftIcon;
                    if (data != null) {
                        aircraftIcon = AircraftIcon.iconFor(
                                data.typeDesignator(),
                                data.description(),
                                category.intValue(),
                                data.wakeTurbulenceCategory()
                        );
                    } else {
                        aircraftIcon = AircraftIcon.iconFor(
                                new AircraftTypeDesignator(""),
                                new AircraftDescription(""),
                                category.intValue(),
                                WakeTurbulenceCategory.UNKNOWN
                        );
                    }
                    return aircraftIcon;
                }));

        icon.getStyleClass().add(ICON_CLASS);
        icon.contentProperty().bind(aircraftIconProperty.map(AircraftIcon::svgPath));
        icon.rotateProperty().bind(Bindings.createDoubleBinding(
                () -> (aircraftIconProperty.get().canRotate()) &&
                        (!Double.isNaN(aircraftState.getTrackOrHeading())) ?
                        Units.convertTo(aircraftState.getTrackOrHeading(), Units.Angle.DEGREE) : 0,
                aircraftState.trackOrHeadingProperty()
        ));
        icon.fillProperty().bind(aircraftState.altitudeProperty().map(
                (b) -> getColor(b.doubleValue())));
        icon.setOnMousePressed(e -> setSelectedAircraft(aircraftState));
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
        if (data == null) {
            return aircraftState.getAddress().string();
        }
        return data.registration().string().isEmpty() ? data.typeDesignator().string() : data.registration().string();
    }

    private String getSecondLine(ObservableAircraftState aircraftState) {
        StringBuilder b = new StringBuilder();
        if (!(Double.isNaN(aircraftState.getVelocity()))) {
            b.append((int) Math.rint(Units.convertTo(aircraftState.getVelocity(), Units.Speed.KILOMETER_PER_HOUR)));
        } else b.append("?");
        b.append("km/h" + "\u2002").append((int) Math.rint(aircraftState.getAltitude())).append("m");
        return b.toString();
    }

    private Color getColor(double a) {
        return ColorRamp.PLASMA.at(Math.cbrt(a / MAX_ALT));
    }

    public Pane pane() {
        return pane;
    }

}
