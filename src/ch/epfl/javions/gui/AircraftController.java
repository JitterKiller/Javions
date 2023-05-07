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

/**
 * La classe AircraftController du sous-paquetage gui gère la vue des aéronefs.
 *
 * @author Adam AIT BOUSSELHAM (356365)
 * @author Abdellah JANATI IDRISSI (362341)
 */
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

    /**
     * Constructeur public de la classe AircraftController.
     * Mise en place d'un autiteur sur l'ensemble (observable, mais non modifiable) des états des aéronefs
     * qui doivent apparaître sur la vue passée en argument pour ajouter et supprimer des états d'aéronefs
     * lors d'un ajout ou du retrait d'aéronefs sur cet ensemble.
     *
     * @param mapParameters    Les paramètres de la portion de la carte visible à l'écran.
     * @param aircraftStates   L'ensemble (observable, mais non modifiable) des états des aéronefs
     *                         qui doivent apparaître sur la vue (provient de la méthode states()
     *                         de la classe AircraftStateManager).
     * @param selectedAircraft La propriété JavaFX contenant l'état de l'aéronef sélectionné.
     */
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
                                e -> change.getElementRemoved().getAddress().string().equals(e.getId()));
                    }
                });
    }

    /**
     * Méthode d'accès à la propriété selectedAircraft en lecture seule.
     *
     * @return La propriété selectedAircraft.
     */
    private ReadOnlyObjectProperty<ObservableAircraftState> selectedAircraftProperty() {
        return selectedAircraft;
    }

    /**
     * Méthode d'accès à la valeur contenue dans la propriété selectedAircraft.
     *
     * @return La valeur contenue dans la propriété selectedAircraft.
     */
    private ObservableAircraftState getSelectedAircraft() {
        return selectedAircraftProperty().get();
    }

    /**
     * Méthode de modification de la valeur contenue dans la propriété selectedAircraft.
     *
     * @param selectedAircraft Le nouvel aéronef sélectionné.
     */
    private void setSelectedAircraft(ObservableAircraftState selectedAircraft) {
        this.selectedAircraft.set(selectedAircraft);
    }

    /**
     * Méthode appelée dans le constructeur qui initialise le panneau JavaFX
     * sur lequel les aéronefs sont affichés.
     */
    private void initializePane() {
        pane = new Pane(new Canvas());
        pane.getStylesheets().add(CSS_FILE);
        pane.setPickOnBounds(false);
    }

    /**
     * Méthode qui crée un Groupe JavaFX représentant l'état d'un aéronef qui sera contenu dans le panneau.
     * Le groupe est composé d'un groupe représentant l'icône de l'aéronef et son étiquette contenant des informations
     * tels que son immatriculation, sa vitesse et son altitude (obtenue grâce à la méthode labelIcon()),
     * ainsi qu'un autre groupe représentant la trajectoire de cet aéronef (obtenue grâce à la méthode trajectory()).
     *
     * @param aircraftState L'état de l'aéronef.
     */
    private void annotatedAircraft(ObservableAircraftState aircraftState) {
        Group annotated = new Group(trajectory(aircraftState), labelIcon(aircraftState));
        annotated.setId(aircraftState.getAddress().string());
        annotated.viewOrderProperty().bind(aircraftState.altitudeProperty().negate());
        pane.getChildren().add(annotated);
    }

    /**
     * Méthode appelée dans le constructeur qui s'occupe d'ajouter tous les états d'aéronefs passés en arguments
     * dans le panneau JavaFX (en appelant la méthode annotatedAircraft()).
     *
     * @param aircraftStates l'ensemble (observable, mais non modifiable) des états des aéronefs
     *                       qui doivent apparaître sur la vue (provient de la méthode states()
     *                       de la classe AircraftStateManager).
     */
    private void addAllAnnotatedAircraft(ObservableSet<ObservableAircraftState> aircraftStates) {
        for (ObservableAircraftState aircraftState : aircraftStates) {
            annotatedAircraft(aircraftState);
        }
    }

    /**
     * Méthode qui crée le groupe représentant la trajectoire de l'aéronef passé en argument
     * (cette méthode sera utilisée et appelée pour constituer le groupe global de l'état de l'aéronef en question).
     *
     * @param aircraftState L'état de l'aéronef.
     * @return le groupe représentant la trajectoire de l'aéronef passé en argument.
     */
    private Group trajectory(ObservableAircraftState aircraftState) {
        Group trajectory = new Group();
        trajectoryBinds(trajectory, aircraftState);
        trajectoryListeners(trajectory, aircraftState);
        trajectory.getStyleClass().add(TRAJECTORY_CLASS);
        return trajectory;
    }

    /**
     * Méthode qui s'occupe de mettre en place les liens du groupe représentant la trajectoire
     * de l'aéronef passé en argument (cette méthode est appelée lors de la création du groupe).
     * Les liens établis au groupe sont la visibilité de la trajectoire et les translations X et Y
     * du groupe représentant la trajectoire.
     *
     * @param trajectory    Le groupe représentant la trajectoire de l'aéronef passé en argument.
     * @param aircraftState L'état de l'aéronef.
     */
    private void trajectoryBinds(Group trajectory, ObservableAircraftState aircraftState) {
        trajectory.visibleProperty().bind(Bindings.createBooleanBinding(
                () -> aircraftState.equals(getSelectedAircraft()), selectedAircraftProperty()));

        trajectory.layoutXProperty().bind(Bindings.createDoubleBinding(
                mapParameters::getMinX, mapParameters.minXProperty()).negate());

        trajectory.layoutYProperty().bind(Bindings.createDoubleBinding(
                mapParameters::getMinY, mapParameters.minYProperty()).negate());
    }

    /**
     * Méthode qui s'occupe de mettre en place les auditeurs du groupe représentant la trajectoire
     * de l'aéronef passé en argument (cette méthode est appelée lors de la création du groupe).
     * Les auditeurs mis en place pour que la représentation graphique de la trajectoire soit à jour
     * sont la trajectoire elle-même, le niveau de zoom de la carte et la visibilité de la trajectoire.
     * Lorsque ces auditeurs reçoivent un changement, ils s'occupent d'appeler la méthode trajectoryUpdate()
     * qui s'occupe de dessiner la trajectoire de l'aéronef.
     *
     * @param trajectory    Le groupe représentant la trajectoire de l'aéronef passé en argument.
     * @param aircraftState L'état de l'aéronef.
     */
    private void trajectoryListeners(Group trajectory, ObservableAircraftState aircraftState) {
        aircraftState.getTrajectory().addListener((ListChangeListener<? super ObservableAircraftState.AirbornePos>)
                change -> trajectoryUpdate(trajectory, aircraftState.getTrajectory()));

        mapParameters.zoomProperty().addListener((ChangeListener<? super Number>)
                (p, oldS, newS) -> trajectoryUpdate(trajectory, aircraftState.getTrajectory()));

        trajectory.visibleProperty().addListener((ChangeListener<? super Boolean>)
                (p, oldS, newS) -> trajectoryUpdate(trajectory, aircraftState.getTrajectory()));
    }

    /**
     * Méthode appelée par les auditeurs du groupe de la trajectoire, dessine la trajectoire à l'aide
     * d'un groupe de segments de droites, chacun d'entre eux étant une instance de Line, et la colorie à l'aide de la
     * méthode getColor (la couleur dépend de l'altitude de l'aéronef).
     * La trajectoire est dessinée uniquement si le groupe de trajectoire est visible.
     *
     * @param trajectory     Le groupe représentant la trajectoire de l'aéronef passé en argument.
     * @param trajectoryList La liste de la trajectoire de l'aéronef passé en argument (contenant la position de cet
     *                       aéronef (longitude et latitude) et son altitude pour la couleur).
     */
    private void trajectoryUpdate(Group trajectory, ObservableList<ObservableAircraftState.AirbornePos> trajectoryList) {
        trajectory.getChildren().clear();

        if (trajectory.isVisible()) {
            for (int i = 0; i < trajectoryList.size() - 1; ++i) {
                Line line = new Line();
                line.setStartX(WebMercator.x(mapParameters.getZoom(), trajectoryList.get(i).position().longitude()));
                line.setStartY(WebMercator.y(mapParameters.getZoom(), trajectoryList.get(i).position().latitude()));
                line.setEndX(WebMercator.x(mapParameters.getZoom(), trajectoryList.get(i + 1).position().longitude()));
                line.setEndY(WebMercator.y(mapParameters.getZoom(), trajectoryList.get(i + 1).position().latitude()));
                /* Si l'altitude de l'aéronef à la position i est égale à celle à la position i+1,
                 * la couleur de la trajectoire est une couleur simple, de type Color. */
                if (trajectoryList.get(i).altitude() == trajectoryList.get(i + 1).altitude()) {
                    line.setStroke(getColor(trajectoryList.get(i).altitude()));
                    /* Sinon c'est un dégradé de couleur entre la couleur l'altitude à la position i,
                     *  t la couleur à la position i+1 */
                } else {
                    Stop s1 = new Stop(0, getColor(trajectoryList.get(i).altitude()));
                    Stop s2 = new Stop(1, getColor(trajectoryList.get(i + 1).altitude()));
                    line.setStroke(new LinearGradient(0, 0, 1, 0, true, NO_CYCLE, s1, s2));
                }
                trajectory.getChildren().add(line);
            }
        }
    }

    /**
     * Méthode qui crée le groupe représentant l'icône de l'aéronef passé en argument et son étiquette.
     * (cette méthode sera utilisée et appelée pour constituer le groupe global de l'état de l'aéronef en question).
     * Ce groupe est composé de l'icône de l'aéronef (obtenue par la méthode icon()) et de son groupe étiquette
     * (obtenue par la méthode label()).
     *
     * @param aircraftState L'état de l'aéronef.
     * @return le groupe représentant la trajectoire de l'aéronef passé en argument.
     */
    private Group labelIcon(ObservableAircraftState aircraftState) {
        Group labelIcon = new Group(label(aircraftState), icon(aircraftState));
        labelIconBinds(labelIcon, aircraftState.positionProperty());
        return labelIcon;
    }

    /**
     * Méthode qui s'occupe de mettre en place les liens du groupe représentant l'icône de l'aéronef
     * et son étiquette (cette méthode est appelée lors de la création du groupe).
     * Les liens établis au groupe sont les translations X et Y du groupe représentant la trajectoire
     * l'icône de l'aéronef et son étiquette et dépendent des paramètres de la portion de la carte visible à l'écran
     * et de la position actuelle de l'aéronef.
     * Les translations de ce groupe sont obtenus grâce aux méthodes xScreen() et yScreen().
     *
     * @param labelIcon        Le groupe représentant l'icône de l'aéronef et son étiquette.
     * @param positionProperty La propriété position de l'aéronef utilisée pour les translations du groupe.
     */
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

    /**
     * Méthode appelée dans labelIconBinds pour établir le lien avec la translation X du groupe
     * représentant l'icône de l'aéronef et son étiquette.
     *
     * @param positionProperty La propriété position de l'aéronef utilisée pour les translations du groupe.
     * @return La valeur utilisée pour établir le lien avec la translation X du groupe.
     */
    private double xScreen(ReadOnlyObjectProperty<GeoPos> positionProperty) {
        return WebMercator.x(mapParameters.getZoom(), positionProperty.getValue().longitude())
                - mapParameters.getMinX();
    }

    /**
     * Méthode appelée dans labelIconBinds pour établir le lien avec la translation Y du groupe
     * représentant l'icône de l'aéronef et son étiquette.
     *
     * @param positionProperty La propriété position de l'aéronef utilisée pour les translations du groupe.
     * @return La valeur utilisée pour établir le lien avec la translation Y du groupe.
     */
    private double yScreen(ReadOnlyObjectProperty<GeoPos> positionProperty) {
        return WebMercator.y(mapParameters.getZoom(), positionProperty.getValue().latitude())
                - mapParameters.getMinY();
    }

    /**
     * Méthode qui crée le groupe représentant l'étiquette de l'aéronef passé en argument.
     * Cette méthode sera appelée par la méthode labelIcon lors de la création du groupe icône et étiquette.
     * Ce groupe est composé d'un texte lui-même composée de deux lignes (la première affichant son immatriculation
     * si elle est connue, sinon son indicatif s'il est connu, sinon son adresse OAC et la deuxième affichant
     * sa vitesse en kilomètres par heure et son altitude en mètres) et d'un rectangle.
     * Le texte possède un lien qui lui permet d'afficher les informations en temps réels de l'état de l'aéronef
     * passé en argument. Les lignes du texte sont obtenues par les méthodes getFirstLine() et getSecondLine().
     * Ce lien déprend donc des propriétés de vitesse et d'altitude de l'aéronef passé en argument.
     * Le rectangle possède également un lien lui permettant d'avoir les bonnes largeurs/longueurs par rapport au
     * texte affiché.
     * Enfin, le groupe possède lui aussi un lien par rapport à sa visibilité. Sa visibilité dépend du niveau
     * de zoom de la carte et de l'aéronef sélectionné.
     *
     * @param aircraftState L'état de l'aéronef.
     * @return le groupe représentant l'étiquette de l'aéronef passé en argument.
     */
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
        label.visibleProperty().bind(Bindings.createBooleanBinding(
                () -> mapParameters.getZoom() >= MIN_ZOOM_LABEL_VISIBLE
                        || aircraftState.equals(getSelectedAircraft()),
                mapParameters.zoomProperty(),
                selectedAircraftProperty()
        ));
        label.getStyleClass().add(LABEL_CLASS);

        return label;
    }

    /**
     * Méthode utilisée dans la mise en place des liens du texte. Elle permet d'avoir la
     * première ligne du texte du groupe étiquette de l'aéronef passé en argument.
     *
     * @param aircraftState L'état de l'aéronef.
     * @return La première ligne du texte du groupe étiquette de l'aéronef passé en argument.
     */
    private String getFirstLine(ObservableAircraftState aircraftState) {
        AircraftData data = aircraftState.getData();
        if (data == null) {
            return aircraftState.getAddress().string();
        }
        return data.registration().string().isEmpty() ? data.typeDesignator().string() : data.registration().string();
    }

    /**
     * Méthode utilisée dans la mise en place des liens du texte. Elle permet d'avoir la
     * seconde ligne du texte du groupe étiquette de l'aéronef passé en argument.
     *
     * @param aircraftState L'état de l'aéronef.
     * @return La seconde ligne du texte du groupe étiquette de l'aéronef passé en argument.
     */
    private String getSecondLine(ObservableAircraftState aircraftState) {
        StringBuilder b = new StringBuilder();
        if (!(Double.isNaN(aircraftState.getVelocity()))) {
            b.append((int) Math.rint(Units.convertTo(aircraftState.getVelocity(), Units.Speed.KILOMETER_PER_HOUR)));
        } else b.append("?");
        b.append("km/h" + "\u2002").append((int) Math.rint(aircraftState.getAltitude())).append("m");
        return b.toString();
    }

    /**
     * Méthode permettant d'avoir l'icône de l'aéronef passé en argument.
     * Cette méthode sera utilisée pour constituer le groupe étiquette + icône de l'aéronef dans
     * la méthode labelIcon().
     * Il y a dans cette méthode la mise en place de liens et d'auditeurs.
     * Premièrement, un lien a été mis en place sur la propriété aircraftIconProperty qui permet d'avoir
     * la bonne icône de l'aéronef selon sa catégorie (puisqu'elle risque de changer au cours du temps alors que les
     * informations de l'aéronef sont constantes).
     * Ensuite l'icône elle-même à plusieurs liens dont sa propriété contentProperty() qui permet de lui assigner
     * le bon chemin de l'icône, sa propriété rotateProperty() qui permet d'orienter l'icône s'il le faut
     * (cela dépend donc du fait que si l'icône peut (et doit) être orientée, mais aussi du
     * cap/orientation de l'aéronef), mais également sa propriété fillProperty() qui permet de colorier l'icône
     * en fonction de l'altitude de l'aéronef passé en argument (la couleur est obtenue grâce à la méthode getColor().
     * Enfin, un gestionnaire d'événement est mis en place, lorsque l'on clique sur une icône, la propriété
     * selectedAircraft est mise à jour et prend pour valeur l'aéronef passé en argument.
     *
     * @param aircraftState L'état de l'aéronef.
     * @return Son icône correspondante.
     */
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
        icon.getStyleClass().add(ICON_CLASS);
        return icon;
    }

    /**
     * Méthode utilisée dans la création de nombreux groupes.
     * Retourne la couleur correspondante à l'altitude d'un aéronef passée en argument grâce à
     * la méthode at() de la classe ColorRamp et de son dégradé de couleur statique PLASMA.
     *
     * @param a L'altitude d'un aéronef en mètre.
     * @return La couleur correspondante à cette altitude.
     */
    private Color getColor(double a) {
        return ColorRamp.PLASMA.at(Math.cbrt(a / MAX_ALT));
    }

    /**
     * Seule méthode publique de la classe. Retourne le panneau JavaFX sur lequel les aéronefs sont affichés.
     * Ce panneau est destiné à être superposé à celui montrant le fond de carte.
     *
     * @return Le panneau JavaFX sur lequel les aéronefs sont affichés.
     */
    public Pane pane() {
        return pane;
    }

}
