package ch.epfl.javions.gui;
import javafx.application.Platform;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.geometry.Point2D;

import java.io.IOException;

import ch.epfl.javions.GeoPos;

public final class BaseMapController {

    private final TileManager tileManager;
    private final MapParameters parameters;
    private final Canvas canvas;
    private final Pane pane;
    private Point2D memoryPosition;
    private boolean redrawNeeded;

    public BaseMapController(TileManager tileManager, MapParameters parameters) {

        this.tileManager = tileManager;
        this.parameters = parameters;
        this.canvas = new Canvas();
        this.pane = new Pane(canvas);

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

        GraphicsContext graphicsContext = canvas.getGraphicsContext2D();
        TileManager.TileId id = new TileManager.TileId(
                parameters.getZoom(),
                (int) parameters.getMinX() / TileManager.TileId.TILE_FACTOR,
                (int) parameters.getMinY() / TileManager.TileId.TILE_FACTOR);

        try {
            graphicsContext.drawImage(tileManager.imageForTileAt(id), id.X(), id.Y());
        } catch(IOException ignored){}

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

            parameters.changeZoomLevel(zoomDelta);
        });

        pane.setOnMousePressed(e -> memoryPosition = new Point2D(e.getX(), e.getY()));

        pane.setOnMouseDragged(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                double x = e.getX() - memoryPosition.getX();
                double y = e.getY() - memoryPosition.getY();
                parameters.scroll(x,y);
            }
        });

        pane.setOnMouseReleased(e -> memoryPosition = null);

    }

    public Pane pane (){ return pane; }

    public void centerOn (GeoPos point){}

}