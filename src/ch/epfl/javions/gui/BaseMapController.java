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

public final class BaseMapController {

    private final TileManager tileManager;
    private final MapParameters parameters;
    private final Canvas canvas;
    private final GraphicsContext graphicsContext;
    private final Pane pane;
    private Point2D memoryPosition;
    private boolean redrawNeeded;

    public BaseMapController(TileManager tileManager, MapParameters parameters) {

        this.tileManager = tileManager;
        this.parameters = parameters;
        canvas = new Canvas();
        pane = new Pane(canvas);
        graphicsContext = canvas.getGraphicsContext2D();

        canvas.widthProperty().bind(pane.widthProperty());
        canvas.heightProperty().bind(pane.heightProperty());

        canvas.sceneProperty().addListener((p, oldS, newS) -> {
            assert oldS == null;
            newS.addPreLayoutPulseListener(this::redrawIfNeeded);
        });

        eventHandler();
        listenersHandler();
    }

    private void drawMap() {
        graphicsContext.clearRect(0,0,canvas.getWidth(),canvas.getHeight());
        for(int i = 0; i < pane.getWidth() + TILE_SIDE; i+= TILE_SIDE) {
            for(int j = 0; j < pane.getHeight() + TILE_SIDE; j+= TILE_SIDE) {
                int xTile = (int) (parameters.getMinXProperty() + i) / TILE_SIDE;
                int yTile = (int) (parameters.getMinYProperty() + j) / TILE_SIDE;
                TileManager.TileId id = new TileManager.TileId(parameters.getZoomProperty(), xTile, yTile);
                int xToPlace = (int) (xTile * TILE_SIDE - parameters.getMinXProperty());
                int yToPlace = (int) (yTile * TILE_SIDE - parameters.getMinYProperty());
                try {
                    graphicsContext.drawImage(tileManager.imageForTileAt(id),
                            xToPlace, yToPlace);
                } catch (IOException ignored){}
            }
        }
    }

    private void redrawIfNeeded() {
        if (!redrawNeeded) return;
        redrawNeeded = false;

        drawMap();
    }

    private void redrawOnNextPulse() {
        redrawNeeded = true;
        Platform.requestNextPulse();
    }

    private void listenersHandler(){
        parameters.zoomProperty().addListener((p, oldS, newS) -> redrawOnNextPulse());
        parameters.minXProperty().addListener((p, oldS, newS) -> redrawOnNextPulse());
        parameters.minYProperty().addListener((p, oldS, newS) -> redrawOnNextPulse());
        canvas.widthProperty().addListener((p, oldS, newS) -> redrawOnNextPulse());
        canvas.heightProperty().addListener((p, oldS, newS) -> redrawOnNextPulse());
    }

    private void eventHandler(){

        LongProperty minScrollTime = new SimpleLongProperty();
        pane.setOnScroll(e -> {
            int zoomDelta = (int) Math.signum(e.getDeltaY());
            if (zoomDelta == 0) return;

            long currentTime = System.currentTimeMillis();
            if (currentTime < minScrollTime.get()) return;
            minScrollTime.set(currentTime + 200);
            parameters.scroll(e.getX(), e.getY());
            parameters.changeZoomLevel(zoomDelta);
            parameters.scroll(-e.getX(), -e.getY());
        });

        pane.setOnMousePressed(e -> memoryPosition = new Point2D(e.getX(), e.getY()));

        pane.setOnMouseDragged(e -> {
            double x = (memoryPosition.getX() - e.getX());
            double y = (memoryPosition.getY() - e.getY());

            parameters.scroll(x,y);
            memoryPosition = new Point2D(e.getX(), e.getY());
        });

        pane.setOnMouseReleased(e -> memoryPosition.subtract(memoryPosition));

    }

    public Pane pane() {
        return pane;
    }

    public void centerOn (GeoPos point){

        double longitude = point.longitude();
        double latitude = point.latitude();
        double x = WebMercator.x(parameters.getZoomProperty(), longitude);
        double y = WebMercator.x(parameters.getZoomProperty(), latitude);
        parameters.setMinX(x - (canvas.getWidth()/2));
        parameters.setMinX(y - (canvas.getHeight()/2));
    }

}