package ch.epfl.javions.gui;

import ch.epfl.javions.GeoPos;
import ch.epfl.javions.Units;
import ch.epfl.javions.adsb.CallSign;
import ch.epfl.javions.aircraft.AircraftData;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseButton;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import static javafx.scene.control.TableView.CONSTRAINED_RESIZE_POLICY_SUBSEQUENT_COLUMNS;

public final class AircraftTableController {

    private static final String TABLE_CSS = "table.css";
    private static final String NUMERIC_COLUMN_CLASS = "numeric";
    private static final String ICAO_COLUMN_TITLE = "OACI";
    private static final String CALL_SIGN_COLUMN_TITLE = "Indicatif";
    private static final String REGISTRATION_COLUMN_TITLE = "Immatriculation";
    private static final String MODEL_COLUMN_TITLE = "Modèle";
    private static final String TYPE_DESIGNATOR_COLUMN_TITLE = "Type";
    private static final String DESCRIPTION_COLUMN_TITLE = "Description";
    private static final String LONGITUDE_COLUMN_TITLE = "Longitude (°)";
    private static final String LATITUDE_COLUMN_TITLE = "Latitude (°)";
    private static final String ALTITUDE_COLUMN_TITLE = "Altitude (m)";
    private static final String VELOCITY_COLUMN_TITLE = "Vitesse (km/h)";
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
    private final ObjectProperty<ObservableAircraftState> selectedAircraft;
    private final TableView<ObservableAircraftState> tableView = new TableView<>();
    private Consumer<ObservableAircraftState> stateConsumer;

    public AircraftTableController(ObservableSet<ObservableAircraftState> states,
                                   ObjectProperty<ObservableAircraftState> selectedAircraft) {

        this.selectedAircraft = selectedAircraft;
        createTableView(tableView);
        setNumberFormat();

        states.addListener((SetChangeListener<ObservableAircraftState>) c -> {
            if (c.wasAdded()) {
                tableView.getItems().add(c.getElementAdded());
                tableView.sort();
            }
            if (c.wasRemoved()) tableView.getItems().remove(c.getElementRemoved());
        });

        selectedAircraftProperty().addListener((p, oldS, newS) -> {
            if (!newS.equals(tableView.getSelectionModel().getSelectedItem()))
                tableView.scrollTo(newS);
            tableView.getSelectionModel().select(newS);
        });

        tableView.getSelectionModel().selectedItemProperty().addListener(
                (p, oldS, newS) -> setSelectedAircraft(newS));

        tableView.setOnMouseClicked(e -> {
            if (e.getClickCount() == DOUBLE_CLICK && e.getButton() == MouseButton.PRIMARY) {
                if (stateConsumer != null && tableView.getSelectionModel().getSelectedItem() != null)
                    stateConsumer.accept(tableView.getSelectionModel().getSelectedItem());
            }
        });
    }

    public ReadOnlyObjectProperty<ObservableAircraftState> selectedAircraftProperty() {
        return selectedAircraft;
    }

    public ObservableAircraftState getSelectedAircraft() {
        return selectedAircraftProperty().get();
    }

    public void setSelectedAircraft(ObservableAircraftState selectedAircraft) {
        this.selectedAircraft.set(selectedAircraft);
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
                    throw new Error(e);
                }
            }
        });
    }

    private TableColumn<ObservableAircraftState, String> createTextualColumn
            (String title, int prefWidth, Function<ObservableAircraftState, ObservableValue<String>> function) {

        TableColumn<ObservableAircraftState, String> column = new TableColumn<>(title);
        column.setPrefWidth(prefWidth);
        column.setCellValueFactory(
                f -> function.apply(f.getValue())
        );
        return column;
    }

    private TableColumn<ObservableAircraftState, String> createNumericalColumn(
            String title, Function<ObservableAircraftState, ObservableValue<Double>> function,
            NumberFormat nf, double unit) {

        TableColumn<ObservableAircraftState, String> column = new TableColumn<>(title);
        column.setPrefWidth(NUMERIC_COLUMN_PREF_WIDTH);
        setColumnsComparators(column, nf);
        column.setCellValueFactory(f -> {
            ObservableValue<Double> value = function.apply(f.getValue());
            return Bindings.createStringBinding(() -> {
                if (!Double.isNaN(value.getValue())) {
                    return nf.format(Units.convertTo(value.getValue(), unit));
                } else return "";
            }, value);
        });
        column.getStyleClass().add(NUMERIC_COLUMN_CLASS);
        return column;
    }

    private void createTableView(TableView<ObservableAircraftState> tableView) {

        tableView.getStylesheets().add(TABLE_CSS);
        tableView.setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY_SUBSEQUENT_COLUMNS);
        tableView.setTableMenuButtonVisible(true);

        TableColumn<ObservableAircraftState, String> icaoColumn = createTextualColumn(
                ICAO_COLUMN_TITLE,
                ICAO_PREF_WIDTH,
                f -> new ReadOnlyStringWrapper(f.getAddress().string()));

        TableColumn<ObservableAircraftState, String> callSignColumn = createTextualColumn(
                CALL_SIGN_COLUMN_TITLE,
                CALL_SIGN_PREF_WIDTH,
                f -> f.callSignProperty().map(CallSign::string));

        TableColumn<ObservableAircraftState, String> registrationColumn = createTextualColumn(
                REGISTRATION_COLUMN_TITLE,
                REGISTRATION_PREF_WIDTH,
                f -> new ReadOnlyObjectWrapper<>(f.getData()).map(d -> d.registration().string()));

        TableColumn<ObservableAircraftState, String> modelColumn = createTextualColumn(
                MODEL_COLUMN_TITLE,
                MODEL_PREF_WIDTH,
                f -> new ReadOnlyObjectWrapper<>(f.getData()).map(AircraftData::model));

        TableColumn<ObservableAircraftState, String> typeDesignator = createTextualColumn(
                TYPE_DESIGNATOR_COLUMN_TITLE,
                TYPE_DESIGNATOR_PREF_WIDTH,
                f -> new ReadOnlyObjectWrapper<>(f.getData()).map(d -> d.typeDesignator().string()));

        TableColumn<ObservableAircraftState, String> descriptionColumn = createTextualColumn(
                DESCRIPTION_COLUMN_TITLE,
                DESCRIPTION_PREF_WIDTH,
                f -> new ReadOnlyObjectWrapper<>(f.getData()).map(d -> d.description().string()));

        TableColumn<ObservableAircraftState, String> longitudeColumn = createNumericalColumn(
                LONGITUDE_COLUMN_TITLE,
                f -> f.positionProperty().map(GeoPos::longitude),
                NUMBER_FORMAT_4,
                Units.Angle.DEGREE);

        TableColumn<ObservableAircraftState, String> latitudeColumn = createNumericalColumn(
                LATITUDE_COLUMN_TITLE,
                f -> f.positionProperty().map(GeoPos::latitude),
                NUMBER_FORMAT_4,
                Units.Angle.DEGREE);

        TableColumn<ObservableAircraftState, String> altitudeColumn = createNumericalColumn(
                ALTITUDE_COLUMN_TITLE,
                f -> f.altitudeProperty().map(Number::doubleValue),
                NUMBER_FORMAT_0,
                Units.BASE_UNIT);

        TableColumn<ObservableAircraftState, String> velocityColumn = createNumericalColumn(
                VELOCITY_COLUMN_TITLE,
                f -> f.velocityProperty().map(Number::doubleValue),
                NUMBER_FORMAT_0,
                Units.Speed.KILOMETER_PER_HOUR);


        tableView.getColumns().setAll(
                List.of(icaoColumn, callSignColumn, registrationColumn, modelColumn, typeDesignator,
                        descriptionColumn, longitudeColumn, latitudeColumn, altitudeColumn, velocityColumn));
    }

    public TableView<ObservableAircraftState> pane() {
        return tableView;
    }

    public void setOnDoubleClick(Consumer<ObservableAircraftState> stateConsumer) {
        this.stateConsumer = stateConsumer;
    }
}