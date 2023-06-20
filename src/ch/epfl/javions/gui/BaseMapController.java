package ch.epfl.javions.gui;

import ch.epfl.javions.GeoPos;
import ch.epfl.javions.WebMercator;
import javafx.application.Platform;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;

import java.io.IOException;

import static ch.epfl.javions.gui.TileManager.TILE_SIDE;

/**
 * La classe BaseMapController du sous-paquetage gui, gère l'affichage et l'interaction avec le fond de carte.
 *
 * @author Adam AIT BOUSSELHAM (356365)
 * @author Abdellah JANATI IDRISSI (362341)
 */
public final class BaseMapController {

    private final TileManager tileManager;
    private final MapParameters mapParameters;
    private Canvas canvas;
    private GraphicsContext graphicsContext;
    private Pane pane;
    private boolean redrawNeeded;

    /**
     * Constructeur de BaseMapController.
     *
     * @param tileManager   Le gestionnaire de tuiles à utiliser pour obtenir les tuiles de la carte.
     * @param mapParameters Les paramètres de la portion visible de la carte.
     */
    public BaseMapController(TileManager tileManager, MapParameters mapParameters) {

        this.tileManager = tileManager;
        this.mapParameters = mapParameters;

        initializePane();
        setupEvents();
        setupListeners();
        setupBindings();
    }

    /**
     * Méthode qui sert à retarder le re-dessin de la carte, appelle la méthode drawMap()
     * si et seulement si l'attribut redrawNeeded est vrai.
     */
    private void redrawIfNeeded() {
        if (!redrawNeeded) return;
        redrawNeeded = false;

        drawMap();
    }


    /**
     * Méthode appelée dans redrawIfNeed(), dessine la carte avec les tuiles correspondantes.
     */
    private void drawMap() {
        graphicsContext.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        for (int i = 0; i < canvas.getWidth() + TILE_SIDE; i += TILE_SIDE) {
            for (int j = 0; j < canvas.getHeight() + TILE_SIDE; j += TILE_SIDE) {
                int xTile = (int) (mapParameters.getMinX() + i) / TILE_SIDE;
                int yTile = (int) (mapParameters.getMinY() + j) / TILE_SIDE;
                if (TileManager.TileId.isValid(mapParameters.getZoom(), xTile, yTile)) {
                    TileManager.TileId id = new TileManager.TileId(mapParameters.getZoom(), xTile, yTile);
                    int xToPlace = (int) (xTile * TILE_SIDE - mapParameters.getMinX());
                    int yToPlace = (int) (yTile * TILE_SIDE - mapParameters.getMinY());
                    try {
                        graphicsContext.drawImage(tileManager.imageForTileAt(id), xToPlace, yToPlace);
                    } catch (IOException ignored) {}
                }
            }
        }
    }

    /**
     * Méthode permettant de demander un re-dessin au prochain battement (60 secondes).
     */
    private void redrawOnNextPulse() {
        redrawNeeded = true;
        Platform.requestNextPulse();
    }

    /**
     * Méthode appelée dans le constructeur qui initialise le panneau JavaFX
     * sur lequel les tuiles seront affichées.
     */
    private void initializePane() {
        canvas = new Canvas();
        pane = new Pane(canvas);
        graphicsContext = canvas.getGraphicsContext2D();
    }


    /**
     * Méthode appelée dans le constructeur, permet de gérer les auditeurs JavaFX
     * qui détectent les situations dans lesquelles le fond de carte doit être redessiné
     * et appeler la méthode redrawOnNextPulse() dans ce cas.
     */
    private void setupListeners() {
        mapParameters.zoomProperty().addListener((p, oldS, newS) -> redrawOnNextPulse());
        mapParameters.minXProperty().addListener((p, oldS, newS) -> redrawOnNextPulse());
        mapParameters.minYProperty().addListener((p, oldS, newS) -> redrawOnNextPulse());
        canvas.widthProperty().addListener((p, oldS, newS) -> redrawOnNextPulse());
        canvas.heightProperty().addListener((p, oldS, newS) -> redrawOnNextPulse());
        canvas.sceneProperty().addListener((p, oldS, newS) -> {
            assert oldS == null;
            newS.addPreLayoutPulseListener(this::redrawIfNeeded);
        });
    }

    /**
     * Méthode appelée dans le constructeur, permet la gestion des événements d'utilisation
     * de la molette de la souris ou du touchpad (pour changer le niveau de zoom de la carte)
     * et le déplacement de la souris lorsque le bouton gauche est maintenu pressé (permet
     * de faire glisser la carte).
     */
    private void setupEvents() {
        final Point2D[] memoryPosition = new Point2D[1];
        LongProperty minScrollTime = new SimpleLongProperty();
        pane.setOnScroll(e -> {
            int zoomDelta = (int) Math.signum(e.getDeltaY());
            if (zoomDelta == 0) return;

            long currentTime = System.currentTimeMillis();
            if (currentTime < minScrollTime.get()) return;
            minScrollTime.set(currentTime + 200);
            mapParameters.scroll(e.getX(), e.getY());
            mapParameters.changeZoomLevel(zoomDelta);
            mapParameters.scroll(-e.getX(), -e.getY());
        });

        pane.setOnMousePressed(e -> memoryPosition[0] = new Point2D(e.getX(), e.getY()));

        pane.setOnMouseDragged(e -> {
            double x = (memoryPosition[0].getX() - e.getX());
            double y = (memoryPosition[0].getY() - e.getY());

            mapParameters.scroll(x, y);
            memoryPosition[0] = new Point2D(e.getX(), e.getY());
        });

        pane.setOnMouseReleased(e -> memoryPosition[0] = null);

    }

    /**
     * Méthode appelée dans le constructeur.
     * Permet de mettre en place les liens sur le Canvas.
     */
    private void setupBindings() {
        canvas.widthProperty().bind(pane.widthProperty());
        canvas.heightProperty().bind(pane.heightProperty());
    }

    /**
     * Méthode d'accès à l'attribut pane.
     *
     * @return Le panneau JavaFX affichant le fond de carte.
     */
    public Pane pane() {
        return pane;
    }

    /**
     * Méthode permettant de centrer la carte sur un aéronef donné.
     *
     * @param point Le point à la surface de la Terre qui sera au centre de la portion
     *              visible de la carte lorsqu'elle sera déplacée.
     */
    public void centerOn(GeoPos point) {

        double longitude = point.longitude();
        double latitude = point.latitude();

        double x = WebMercator.x(mapParameters.getZoom(), longitude) - mapParameters.getMinX() - canvas.getWidth() / 2;
        double y = WebMercator.y(mapParameters.getZoom(), latitude) - mapParameters.getMinY() - canvas.getHeight() / 2;

        mapParameters.scroll(x, y);
    }

}