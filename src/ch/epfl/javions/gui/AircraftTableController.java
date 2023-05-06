package ch.epfl.javions.gui;

import ch.epfl.javions.Units;
import ch.epfl.javions.adsb.CallSign;
import ch.epfl.javions.aircraft.AircraftData;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseButton;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.List;
import java.util.function.Consumer;

import static javafx.scene.control.TableView.CONSTRAINED_RESIZE_POLICY_SUBSEQUENT_COLUMNS;


public final class AircraftTableController {

    private static final String TABLE_CSS = "table.css";
    private static final String NUMERIC_CLASS = "numeric";
    private static final int ICAO_PREF_WIDTH = 60;
    private static final int CALL_SIGN_PREF_WIDTH = 70;
    private static final int REGISTRATION_PREF_WIDTH = 90;
    private static final int MODEL_PREF_WIDTH = 230;
    private static final int TYPE_DESIGNATOR_PREF_WIDTH = 50;
    private static final int DESCRIPTION_PREF_WIDTH = 70;
    private static final int NUMERIC_COLUMN_PREF_WIDTH = 85;
    private static final int DECIMALS_LON_LAT = 4;
    private static final int DECIMALS_ALT_SPEED = 0;
    private static final int DOUBLE_CLICK = 2;
    private static final NumberFormat NUMBER_FORMAT_0 = NumberFormat.getInstance();
    private static final NumberFormat NUMBER_FORMAT_4 = NumberFormat.getInstance();
    private final ObjectProperty<ObservableAircraftState> stateProperty;
    private final TableView<ObservableAircraftState> tableView = new TableView<>();

    public AircraftTableController(ObservableSet<ObservableAircraftState> states,
                                   ObjectProperty<ObservableAircraftState> stateProperty) {

        this.stateProperty = stateProperty;
        createTableView(tableView);
        setNumberFormat();

        states.addListener((SetChangeListener<ObservableAircraftState>) c -> {
            if (c.wasAdded()) {
                tableView.getItems().add(c.getElementAdded());
                tableView.sort();
            }
            if (c.wasRemoved()) {
                tableView.getItems().remove(c.getElementRemoved());
            }
        });

        stateProperty.addListener((p, oldS, newS) -> {
            tableView.getSelectionModel().select(newS);
            if (oldS != newS) {
                tableView.scrollTo(newS);
            }
        });

        tableView.getSelectionModel().selectedItemProperty().addListener((p, oldS, newS) -> {
            if (newS != null) {
                stateProperty.set(newS);
            }
        });

    }

    private void setNumberFormat() {
        NUMBER_FORMAT_0.setMinimumFractionDigits(DECIMALS_ALT_SPEED);
        NUMBER_FORMAT_0.setMaximumFractionDigits(DECIMALS_ALT_SPEED);

        NUMBER_FORMAT_4.setMinimumFractionDigits(DECIMALS_LON_LAT);
        NUMBER_FORMAT_4.setMaximumFractionDigits(DECIMALS_LON_LAT);
    }

    private void setColumnsComparators(TableColumn<ObservableAircraftState, String> tableColumn,
                                       NumberFormat nf) {
        tableColumn.setComparator((s1, s2) -> {
            if (s1.isEmpty() || s2.isEmpty()) {
                return s1.compareTo(s2);
            } else {
                try {
                    return Double.compare(nf.parse(s1).doubleValue(), nf.parse(s2).doubleValue());
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private void createTableView(TableView<ObservableAircraftState> tableView) {

        tableView.getStylesheets().add(TABLE_CSS);
        tableView.setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY_SUBSEQUENT_COLUMNS);
        tableView.setTableMenuButtonVisible(true);

        TableColumn<ObservableAircraftState, String> icaoColumn = new TableColumn<>("OACI");
        icaoColumn.setPrefWidth(ICAO_PREF_WIDTH);
        icaoColumn.setCellValueFactory(f ->
                new ReadOnlyStringWrapper(f.getValue().getAddress().string()));

        TableColumn<ObservableAircraftState, String> callSignColumn = new TableColumn<>("Indicatif");
        callSignColumn.setPrefWidth(CALL_SIGN_PREF_WIDTH);
        callSignColumn.setCellValueFactory(f -> f.getValue().callSignProperty().map(CallSign::string));

        TableColumn<ObservableAircraftState, String> registrationColumn = new TableColumn<>("Immatriculation");
        registrationColumn.setPrefWidth(REGISTRATION_PREF_WIDTH);
        registrationColumn.setCellValueFactory(f -> {
            AircraftData ad = f.getValue().getData();
            return new ReadOnlyObjectWrapper<>(ad).map(d -> d.registration().string());
        });


        TableColumn<ObservableAircraftState, String> modelColumn = new TableColumn<>("Modèle");
        modelColumn.setPrefWidth(MODEL_PREF_WIDTH);
        modelColumn.setCellValueFactory(f -> {
            AircraftData ad = f.getValue().getData();
            return new ReadOnlyObjectWrapper<>(ad).map(AircraftData::model);
        });

        TableColumn<ObservableAircraftState, String> typeDesignator = new TableColumn<>("Type");
        typeDesignator.setPrefWidth(TYPE_DESIGNATOR_PREF_WIDTH);
        typeDesignator.setCellValueFactory(f -> {
            AircraftData ad = f.getValue().getData();
            return new ReadOnlyObjectWrapper<>(ad).map(d -> d.typeDesignator().string());
        });

        TableColumn<ObservableAircraftState, String> descriptionColumn = new TableColumn<>("Description");
        descriptionColumn.setPrefWidth(DESCRIPTION_PREF_WIDTH);
        descriptionColumn.setCellValueFactory(f -> {
            AircraftData ad = f.getValue().getData();
            return new ReadOnlyObjectWrapper<>(ad).map(d -> d.description().string());
        });

        TableColumn<ObservableAircraftState, String> longitudeColumn = new TableColumn<>("Longitude (°)");
        longitudeColumn.getStyleClass().add("numeric");
        longitudeColumn.setPrefWidth(NUMERIC_COLUMN_PREF_WIDTH);
        longitudeColumn.setCellValueFactory(f -> f.getValue().positionProperty().map(d ->
                NUMBER_FORMAT_4.format(Units.convertTo(d.longitude(), Units.Angle.DEGREE))));
        setColumnsComparators(longitudeColumn, NUMBER_FORMAT_4);

        TableColumn<ObservableAircraftState, String> latitudeColumn = new TableColumn<>("Latitude (°)");
        latitudeColumn.getStyleClass().add(NUMERIC_CLASS);
        latitudeColumn.setPrefWidth(NUMERIC_COLUMN_PREF_WIDTH);
        latitudeColumn.setCellValueFactory(f -> f.getValue().positionProperty().map(d ->
                NUMBER_FORMAT_4.format(Units.convertTo(d.latitude(), Units.Angle.DEGREE))));
        setColumnsComparators(latitudeColumn, NUMBER_FORMAT_4);

        TableColumn<ObservableAircraftState, String> altitudeColumn = new TableColumn<>("Altitude (m)");
        altitudeColumn.getStyleClass().add(NUMERIC_CLASS);
        altitudeColumn.setPrefWidth(NUMERIC_COLUMN_PREF_WIDTH);
        altitudeColumn.setCellValueFactory(f -> f.getValue().altitudeProperty().map(d ->
                NUMBER_FORMAT_0.format(d.doubleValue())));
        setColumnsComparators(altitudeColumn, NUMBER_FORMAT_0);

        TableColumn<ObservableAircraftState, String> velocityColumn = new TableColumn<>("Vitesse (km/h)");
        velocityColumn.getStyleClass().add(NUMERIC_CLASS);
        velocityColumn.setPrefWidth(NUMERIC_COLUMN_PREF_WIDTH);
        velocityColumn.setCellValueFactory(f -> f.getValue().velocityProperty().map(d ->
                NUMBER_FORMAT_0.format(Units.convertTo(d.doubleValue(), Units.Speed.KILOMETER_PER_HOUR))));
        setColumnsComparators(velocityColumn, NUMBER_FORMAT_0);

        tableView.getColumns().setAll(List.of(icaoColumn, callSignColumn, registrationColumn, modelColumn, typeDesignator,
                descriptionColumn, longitudeColumn, latitudeColumn, altitudeColumn, velocityColumn));
    }

    public TableView<ObservableAircraftState> pane() {
        return tableView;
    }

    public void setOnDoubleClick(Consumer<ObservableAircraftState> stateConsumer) {
        tableView.setOnMouseClicked(e -> {
            if (e.getClickCount() == DOUBLE_CLICK && e.getButton() == MouseButton.PRIMARY) {
                ObservableAircraftState selectedState = tableView.getSelectionModel().getSelectedItem();
                if (stateConsumer != null && selectedState != null) stateConsumer.accept(selectedState);
            }
        });
    }

}