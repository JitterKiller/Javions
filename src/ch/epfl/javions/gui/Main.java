package ch.epfl.javions.gui;

import ch.epfl.javions.ByteString;
import ch.epfl.javions.adsb.Message;
import ch.epfl.javions.adsb.MessageParser;
import ch.epfl.javions.adsb.RawMessage;
import ch.epfl.javions.aircraft.AircraftDatabase;
import ch.epfl.javions.demodulation.AdsbDemodulator;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
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
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

public final class Main extends Application {
    private static final int DEFAULT_ZOOM_LEVEL = 8;
    private static final int DEFAULT_MERCATOR_X = 33_530;
    private static final int DEFAULT_MERCATOR_Y = 23_070;
    private static final int MIN_WIDTH = 800;
    private static final int MIN_HEIGHT = 600;
    private static final int NS_TO_MS = 1_000_000;
    private static final String TITLE = "Javions";
    private static final String DEFAULT_TILE_SERVER = "tile.openstreetmap.org";
    private static final String DEFAULT_CACHE_DIR = "tile-cache";
    private static final String DEFAULT_DATABASE_URI = "/aircraft.zip";
    private static final ConcurrentLinkedQueue<RawMessage> queue = new ConcurrentLinkedQueue<>();

    public static void main(String[] args) {
        launch(args);
    }

    private void readRadioMessages()
            throws IOException, InterruptedException {
        AdsbDemodulator demodulator = new AdsbDemodulator(System.in);
        RawMessage nextMessage;
        while ((nextMessage = demodulator.nextMessage()) != null) {
            queue.add(nextMessage);
        }
    }

    private void readFileMessages(String fileName) throws IOException {
        try (DataInputStream s = new DataInputStream(
                new BufferedInputStream(
                        new FileInputStream(fileName)))) {
            long startTime = System.nanoTime();
            byte[] bytes = new byte[RawMessage.LENGTH];
            while (true) {
                long timeStampNs = s.readLong();
                int bytesRead = s.readNBytes(bytes, 0, bytes.length);
                assert bytesRead == RawMessage.LENGTH;
                long currentTime = System.nanoTime();
                RawMessage rawMessage = new RawMessage(timeStampNs, new ByteString(bytes));
                Thread.sleep(Math.max(0,(timeStampNs- (currentTime - startTime)))/NS_TO_MS);
                queue.add(rawMessage);
            }
        } catch (EOFException ignored) {

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        List<String> args = getParameters().getRaw();

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
        AircraftTableController atc = new AircraftTableController(asm.states(), sap);
        StatusLineController slc = new StatusLineController();

        StackPane topScene = new StackPane(bmc.pane(), ac.pane());
        BorderPane bottomScene = new BorderPane();
        bottomScene.setCenter(atc.pane());
        bottomScene.setTop(slc.pane());

        SplitPane scene = new SplitPane(topScene, bottomScene);
        scene.setOrientation(Orientation.VERTICAL);
        primaryStage.setScene(new Scene(scene));
        primaryStage.setMinWidth(MIN_WIDTH);
        primaryStage.setMinHeight(MIN_HEIGHT);
        primaryStage.setTitle(TITLE);
        primaryStage.show();

        slc.aircraftCountProperty().bind(Bindings.size(asm.states()));
        atc.setOnDoubleClick(state -> bmc.centerOn(state.getPosition()));

        new Thread(() -> {
            if (args.isEmpty()) {
                try {
                    readRadioMessages();
                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            } else {
                try {
                    readFileMessages(args.get(0));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();

        new AnimationTimer() {
            @Override
            public void handle(long now) {
                while(!queue.isEmpty()){
                    Message message = MessageParser.parse(queue.poll());
                    if (message != null) {
                        try {
                            asm.updateWithMessage(message);
                            slc.setMessageCount(slc.getMessageCount() + 1);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
                asm.purge();
            }
        }.start();
    }
}