package ch.epfl.javions.gui;

import ch.epfl.javions.adsb.CallSign;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.input.MouseButton;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Objects;
import java.util.function.Consumer;


import static javafx.scene.control.TreeTableView.CONSTRAINED_RESIZE_POLICY_SUBSEQUENT_COLUMNS;

public final class AircraftTableController {

    private final ObservableList<ObservableAircraftState> states;
    private final ObjectProperty<ObservableAircraftState> stateProperty;
    private final javafx.scene.control.TableView<ObservableAircraftState> tableView =
            new javafx.scene.control.TableView<>();


    public AircraftTableController(ObservableList<ObservableAircraftState> states,
                                   ObjectProperty<ObservableAircraftState> stateProperty) {

        this.states = states;
        this.stateProperty = stateProperty;
        createTableView(tableView);

        states.addListener((ListChangeListener<ObservableAircraftState>) c -> {
            while (c.next()) {
                if (c.wasAdded()) {
                    tableView.getItems().addAll(c.getAddedSubList());
                    tableView.sort();
                }
                if (c.wasRemoved()) {
                    tableView.getItems().removeAll(c.getRemoved());
                }
            }
        });

        stateProperty.addListener((P, oldS, newS) -> {
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

    private void setColumnsComparators (TableColumn<ObservableAircraftState, String> tableColumn, NumberFormat nf) {

        tableColumn.setComparator((s1, s2) -> {
            if (s1.isEmpty() || s2.isEmpty()) {
                return s1.compareTo(s2);
            } else {
                try {
                    return Double.compare(nf.parse(s1).doubleValue(), nf.parse(s2).doubleValue());
                } catch (ParseException ignored) {}
            }
            return 0;
        });
    }

    private void createTableView (javafx.scene.control.TableView tableView) {

        tableView.setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY_SUBSEQUENT_COLUMNS);
        tableView.setTableMenuButtonVisible(true);

        TableColumn<ObservableAircraftState, String> oaciColumn = new TableColumn<>("OACI");
        oaciColumn.setPrefWidth(60);
        oaciColumn.setCellValueFactory(f ->
                new ReadOnlyObjectWrapper<>(f.getValue().getAddress().string()));

        TableColumn<ObservableAircraftState, String> indicatifColumn = new TableColumn<>("Indicatif");
        indicatifColumn.setPrefWidth(70);
        indicatifColumn.setCellValueFactory(f ->
                f.getValue().callSignProperty().map(CallSign::string));

        TableColumn<ObservableAircraftState, String> immatriculationColumn = new TableColumn<>("Immatriculation");
        immatriculationColumn.setPrefWidth(90);
        immatriculationColumn.setCellValueFactory(f ->
                new ReadOnlyObjectWrapper<>(f.getValue().getData().registration().string()));

        TableColumn<ObservableAircraftState, String> modelColumn = new TableColumn<>("Modèle");
        modelColumn.setPrefWidth(230);
        modelColumn.setCellValueFactory(f ->
                new ReadOnlyObjectWrapper<>(f.getValue().getData().model()));

        TableColumn<ObservableAircraftState, String> typeColumn = new TableColumn<>("Type");
        typeColumn.setPrefWidth(50);
        typeColumn.setCellValueFactory(f ->
                new ReadOnlyObjectWrapper<>(f.getValue().getData().typeDesignator().string()));

        TableColumn<ObservableAircraftState, String> descriptionColumn = new TableColumn<>("Description");
        descriptionColumn.setPrefWidth(70);
        typeColumn.setCellValueFactory(f ->
                new ReadOnlyObjectWrapper<>(f.getValue().getData().description().string()));

        NumberFormat nf4 = NumberFormat.getInstance();
        nf4.setMinimumFractionDigits(4); nf4.setMaximumFractionDigits(4);

        NumberFormat nf0 = NumberFormat.getInstance();
        nf0.setMinimumFractionDigits(0); nf0.setMaximumFractionDigits(0);

        TableColumn<ObservableAircraftState, String> longitudeColumn = new TableColumn<>("Longitude (°)");
        longitudeColumn.getStyleClass().add("numeric");
        longitudeColumn.setPrefWidth(85);
        longitudeColumn.setCellValueFactory(f ->
                new SimpleStringProperty(nf4.format(f.getValue().getPosition().longitude())));
        setColumnsComparators(longitudeColumn, nf4);

        TableColumn<ObservableAircraftState, String> latitudeColumn = new TableColumn<>("Latitude (°)");
        latitudeColumn.getStyleClass().add("numeric");
        latitudeColumn.setPrefWidth(85);
        latitudeColumn.setCellValueFactory(f ->
                new SimpleStringProperty(nf4.format(f.getValue().getPosition().latitude())));
        setColumnsComparators(latitudeColumn, nf4);

        TableColumn<ObservableAircraftState, String> altitudeColumn = new TableColumn<>("Altitude (m)");
        altitudeColumn.getStyleClass().add("numeric");
        altitudeColumn.setPrefWidth(85);
        altitudeColumn.setCellValueFactory(f ->
                new SimpleStringProperty(nf0.format(f.getValue().getAltitude())));
        setColumnsComparators(altitudeColumn, nf0);

        TableColumn<ObservableAircraftState, String> velocityColumn = new TableColumn<>("Vitesse (km/h)");
        velocityColumn.getStyleClass().add("numeric");
        velocityColumn.setPrefWidth(85);
        velocityColumn.setCellValueFactory(f ->
                new SimpleStringProperty(nf0.format(f.getValue().getVelocity())));
        setColumnsComparators(velocityColumn, nf0);


        tableView.getColumns().addAll(oaciColumn, indicatifColumn, immatriculationColumn, modelColumn, typeColumn,
                descriptionColumn, longitudeColumn, latitudeColumn, altitudeColumn, velocityColumn);

        tableView.getStylesheets().add(Objects.requireNonNull(getClass().getResource("table.css")).toExternalForm());
    }

    public javafx.scene.control.TableView<ObservableAircraftState> pane(){ return tableView; }

    public void setOnDoubleClick(Consumer<ObservableAircraftState> stateConsumer){

        tableView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2 && e.getButton() == MouseButton.PRIMARY) {
                ObservableAircraftState selectedState = tableView.getSelectionModel().getSelectedItem();

                if (stateConsumer != null && selectedState != null) {
                    stateConsumer.accept(selectedState);
                }
            }
        });
    }

}