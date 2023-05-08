package ch.epfl.javions.gui;

import ch.epfl.javions.ByteString;
import ch.epfl.javions.adsb.RawMessage;
import ch.epfl.javions.aircraft.AircraftDatabase;
import javafx.application.Application;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.*;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class Main extends Application {

    private static final int DEFAULT_ZOOM_LEVEL = 8;
    private static final int DEFAULT_MERCATOR_X = 33_530;
    private static final int DEFAULT_MERCATOR_Y = 23_070;
    private static final int DEFAULT_STAGE_WIDTH = 800;
    private static final int DEFAULT_STAGE_HEIGHT = 600;
    private static final String DEFAULT_STAGE_NAME = "Javions";
    private static final String DEFAULT_TILE_SERVER = "tile.openstreetmap.org";
    private static final String DEFAULT_CACHE_DIR = "tile-cache";
    private static final String DEFAULT_DATABASE_URI = "/aircraft.zip";
    private static final ConcurrentLinkedQueue<RawMessage> queue = new ConcurrentLinkedQueue<>();

    public static void main(String[] args) {
        launch(args);
    }

    static List<RawMessage> readAllMessages(String fileName)
            throws IOException {
        List<RawMessage> allMessages = new ArrayList<>();
        try (DataInputStream s = new DataInputStream(
                new BufferedInputStream(
                        new FileInputStream(fileName)))) {
            byte[] bytes = new byte[RawMessage.LENGTH];
            while (true) {
                long timeStampNs = s.readLong();
                int bytesRead = s.readNBytes(bytes, 0, bytes.length);
                assert bytesRead == RawMessage.LENGTH;
                ByteString message = new ByteString(bytes);
                allMessages.add(new RawMessage(timeStampNs, message));
            }
        } catch (EOFException e) {
            return allMessages;
        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        TileManager tm = new TileManager(Path.of(DEFAULT_CACHE_DIR), DEFAULT_TILE_SERVER);
        MapParameters mp = new MapParameters(DEFAULT_ZOOM_LEVEL, DEFAULT_MERCATOR_X, DEFAULT_MERCATOR_Y);
        BaseMapController bmc = new BaseMapController(tm, mp);

        URL dbUrl = getClass().getResource(DEFAULT_DATABASE_URI);
        assert dbUrl != null;
        String f = Path.of(dbUrl.toURI()).toString();
        AircraftDatabase db = new AircraftDatabase(f);

        AircraftStateManager asm = new AircraftStateManager(db);
        ObjectProperty<ObservableAircraftState> sap = new SimpleObjectProperty<>();
        AircraftController ac = new AircraftController(mp, asm.states(), sap);
        AircraftTableController atc = new AircraftTableController(asm.states(),sap);
        StatusLineController slc = new StatusLineController();

        StackPane topScene = new StackPane(bmc.pane(),ac.pane());
        BorderPane bottomScene = new BorderPane();
        bottomScene.setCenter(atc.pane());
        bottomScene.setTop(slc.pane());

        SplitPane scene = new SplitPane(topScene,bottomScene);
        scene.setOrientation(Orientation.VERTICAL);
        primaryStage.setScene(new Scene(scene));
        primaryStage.setMinWidth(DEFAULT_STAGE_WIDTH);
        primaryStage.setMinHeight(DEFAULT_STAGE_HEIGHT);
        primaryStage.setTitle(DEFAULT_STAGE_NAME);
        primaryStage.show();

        if(getParameters().getRaw().isEmpty()) {
            new Thread(() -> {

            });
        }
    }
}
