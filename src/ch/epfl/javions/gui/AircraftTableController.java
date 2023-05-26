package ch.epfl.javions.gui;

import ch.epfl.javions.GeoPos;
import ch.epfl.javions.Units;
import ch.epfl.javions.adsb.CallSign;
import ch.epfl.javions.aircraft.AircraftData;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
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

/**
 * La classe AircraftTableController du sous-paquetage gui gère la table des aéronefs.
 *
 * @author Adam AIT BOUSSELHAM (356365)
 * @author Abdellah JANATI IDRISSI (362341)
 */
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
    private static final String EMPTY_CELL = "";
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
    private final TableView<ObservableAircraftState> tableView = new TableView<>();
    private Consumer<ObservableAircraftState> stateConsumer;

    /**
     * Constructeur public de la classe AircraftTableController.
     *
     * @param states           L'ensemble (observable, mais non modifiable) des états des aéronefs
     *                         qui doivent apparaître sur la vue (provient de la méthode states()
     *                         de la classe AircraftStateManager)
     * @param selectedAircraft La propriété JavaFX contenant l'état de l'aéronef sélectionné,
     *                         dont le contenu peut être nul lorsque aucun aéronef n'est sélectionné.
     */
    public AircraftTableController(ObservableSet<ObservableAircraftState> states,
                                   ObjectProperty<ObservableAircraftState> selectedAircraft) {

        setupNumberFormats();
        setupListeners(states,selectedAircraft);
        setupEvents();
        setupTableView();
    }

    /**
     * Méthode privée qui permet de configurer les deux instances de NumberFormat pour les colones numériques
     * (l'une ayant le nombre de décimaux après la virgule à 0 et l'autre à 4.)
     */
    private void setupNumberFormats() {
        NUMBER_FORMAT_0.setMinimumFractionDigits(DECIMALS_ALT_SPEED);
        NUMBER_FORMAT_0.setMaximumFractionDigits(DECIMALS_ALT_SPEED);

        NUMBER_FORMAT_4.setMinimumFractionDigits(DECIMALS_LON_LAT);
        NUMBER_FORMAT_4.setMaximumFractionDigits(DECIMALS_LON_LAT);
    }

    /**
     * Méthode appelée dans le constructeur, permet de mettre en place les auditeurs sur
     * l'ensemble des états d'aéronefs passé au constructeur, sur la propriété passée au constructeur
     * et sur la propriété selectedItemProperty du modèle de sélection.
     *
     * @param states           L'ensemble (observable, mais non modifiable) des états des aéronefs
     *                         qui doivent apparaître sur la vue (provient de la méthode states()
     *                         de la classe AircraftStateManager)
     * @param selectedAircraft La propriété JavaFX contenant l'état de l'aéronef sélectionné,
     *                         dont le contenu peut être nul lorsque aucun aéronef n'est sélectionné.
     */
    private void setupListeners(ObservableSet<ObservableAircraftState> states,
                                ObjectProperty<ObservableAircraftState> selectedAircraft) {
        /* Mise en place de l'auditeur sur l'ensemble des états d'aéronefs passé au constructeur,
         * afin de correctement faire apparaître et disparaître les aéronefs correspondants de la table. */
        states.addListener((SetChangeListener<ObservableAircraftState>) c -> {
            if (c.wasAdded()) {
                tableView.getItems().add(c.getElementAdded());
                tableView.sort();
            }
            if (c.wasRemoved()) tableView.getItems().remove(c.getElementRemoved());
        });

        /* Mise en place de l'auditeur sur la propriété passée au constructeur. */
        selectedAircraft.addListener((p, oldS, newS) -> {
            if (!newS.equals(tableView.getSelectionModel().getSelectedItem()))
                tableView.scrollTo(newS);
            tableView.getSelectionModel().select(newS);
        });

        /* Mise en place de l'auditeur la propriété selectedItemProperty du modèle de sélection. */
        tableView.getSelectionModel().selectedItemProperty().addListener(
                (p, oldS, newS) -> selectedAircraft.set(newS));
    }

    /**
     * Méthode appelée dans le constructeur, permet la gestion d'un clique double sur
     * la tableView.
     */
    private void setupEvents() {
        tableView.setOnMouseClicked(e -> {
            if (e.getClickCount() == DOUBLE_CLICK && e.getButton() == MouseButton.PRIMARY) {
                if (stateConsumer != null && tableView.getSelectionModel().getSelectedItem() != null)
                    stateConsumer.accept(tableView.getSelectionModel().getSelectedItem());
            }
        });
    }

    /**
     * Méthode qui permet de modifier le comparateur associé aux colonnes numériques
     * au moyen de la méthode setComparator.
     *
     * @param numericColumn La colonne numérique à laquelle on modifie le comparateur.
     * @param nf            L'instance de NumberFormat pour transformer Le contenu des colones numériques (qui sont des
     *                      chaines de caractères à la base) en nombres que l'on peut comparer.
     */
    private void setColumnsComparators(TableColumn<ObservableAircraftState, String> numericColumn,
                                       NumberFormat nf) {
        numericColumn.setComparator((s1, s2) -> {
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

    /**
     * Méthode privée servant à créer une nouvelle colonne textuelle.
     *
     * @param title     Le titre de la colonne.
     * @param prefWidth Sa largeur préférée.
     * @param function  La fonction servant à bien afficher la valeur dans les cellules de la colonne.
     * @return Une colonne textuelle fonctionnelle.
     */
    private TableColumn<ObservableAircraftState, String> createTextualColumn(
            String title, int prefWidth, Function<ObservableAircraftState,
            ObservableValue<String>> function) {

        TableColumn<ObservableAircraftState, String> column = new TableColumn<>(title);
        column.setPrefWidth(prefWidth);
        column.setCellValueFactory(
                f -> function.apply(f.getValue())
        );
        return column;
    }

    /**
     * Méthode privée servant à créer une nouvelle colonne numérique.
     *
     * @param title    Le titre de la colonne.
     * @param function La fonction servant à bien afficher la valeur dans les cellules de la colonne.
     * @param nf       L'instance de NumberFormat pour n'afficher que certains décimaux après la virgule des valeurs.
     * @param unit     L'unité des valeurs à afficher.
     * @return Une colonne numérique fonctionnelle.
     */
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
                } else return EMPTY_CELL;
            }, value);
        });
        column.getStyleClass().add(NUMERIC_COLUMN_CLASS);
        return column;
    }

    /**
     * Méthode appelée dans le constructeur de la classe.
     * Elle configure l'instance tableView.
     * Elle appelle les méthodes createTextualColumn() et createNumericalColumn() pour instancier
     * les différentes colonnes textuelles et numériques de la table.
     */
    private void setupTableView() {

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

    /**
     * Méthode retournant le nœud à la racine du graphe de scène de la classe AircraftTableController
     *
     * @return Retourne l'instance tableView.
     */
    public TableView<ObservableAircraftState> pane() {
        return tableView;
    }

    /**
     * Méthode qui prend en argument une valeur de type Consumer<ObservableAircraftState>
     * et qui enregistre ce dernier dans un attribut de la classe AircraftTableController.
     * Le consumer sera utilisé dans un gestionnaire d'événement lié à la tableView
     * (lorsqu'un double click sera effectué sur la table et qu'un aéronef est actuellement sélectionné,
     * on appelle la méthode accept du Consumer en lui passant en argument l'état de cet aéronef.)
     *
     * @param stateConsumer Le Consumer<ObservableAircraftState>.
     */
    public void setOnDoubleClick(Consumer<ObservableAircraftState> stateConsumer) {
        this.stateConsumer = stateConsumer;
    }
}