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
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * La classe Main du sous-paquetage gui contient le programme principal.
 *
 * @author Adam AIT BOUSSELHAM (356365)
 * @author Abdellah JANATI IDRISSI (362341)
 */
public final class Main extends Application {
    private static final int DEFAULT_ZOOM_LEVEL = 8;
    private static final int DEFAULT_MERCATOR_X = 33_530;
    private static final int DEFAULT_MERCATOR_Y = 23_070;
    private static final int MIN_WIDTH = 800;
    private static final int MIN_HEIGHT = 600;
    private static final int NS_TO_MS = 1_000_000;
    private static final long PURGE_UPDATE_NS = 1_000_000_000L;
    private static final String TITLE = "Javions";
    private static final String DEFAULT_TILE_SERVER = "tile.openstreetmap.org";
    private static final String DEFAULT_CACHE_DIR = "tile-cache";
    private static final String DEFAULT_DATABASE_URI = "/aircraft.zip";

    /**
     * Méthode main de la classe Main qui appelle la méthode launch de la classe abstraite
     * Application dont JavaFX hérite.
     *
     * @param args Les arguments du lancement du programme.
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Méthode qui démodule des messages reçus par une radio (connectée au périphérique actuel)
     * et les places directement dans une file de type ConcurrentLinkedQueue.
     *
     * @throws IOException Si une erreur se produit lors de la lecture du flux.
     */
    private void readRadioMessages(ConcurrentLinkedQueue<RawMessage> queue) throws IOException {
        AdsbDemodulator demodulator = new AdsbDemodulator(System.in);
        RawMessage nextMessage;
        while ((nextMessage = demodulator.nextMessage()) != null) {
            queue.add(nextMessage);
        }
    }

    /**
     * Méthode qui démodule des messages provenant d'un fichier, ces derniers n'y sont placés
     * que lorsqu'une durée égale à leur horodatage s'est écoulée depuis le début de l'exécution du programme.
     *
     * @param fileName Le nom du fichier contenant les messages.
     * @throws IOException Si une erreur se produit lors de l'accès ou la lecture
     *                     des messages du fichier contenant les messages.
     */
    private void readFileMessages(String fileName, ConcurrentLinkedQueue<RawMessage> queue) throws IOException {
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
                Thread.sleep(Math.max(0, (timeStampNs - (currentTime - startTime))) / NS_TO_MS);
                queue.add(rawMessage);
            }
        } catch (EOFException ignored) {

        } catch (InterruptedException e) {
            throw new Error(e);
        }
    }

    /**
     * Méthode qui démarre l'application en construisant le graphe de scène correspondant à l'interface graphique,
     * démarrant le fil d'exécution chargé d'obtenir les messages, et enfin démarrant le "minuteur d'animation"
     * chargé de mettre à jour les états d'aéronefs en fonction des messages reçus.
     *
     * @param primaryStage La scène principale de cette application.
     * @throws URISyntaxException Si le chemin d'accès à la base de donnée d'aéronefs est invalide.
     */
    @Override
    public void start(Stage primaryStage) throws URISyntaxException {

        final ConcurrentLinkedQueue<RawMessage> queue = new ConcurrentLinkedQueue<>();
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


        /* Fil d'exécution chargé d'obtenir les messages des aéronefs*/
        Thread thread = new Thread(() -> {
            try {
                if (args.isEmpty()) readRadioMessages(queue);
                else readFileMessages(args.get(0), queue);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
        thread.setDaemon(true);
        thread.start();

        /* Minuteur D'animation chargé de mettre à jour les états d'aéronefs en fonction des messages reçus */
        new AnimationTimer() {
            private long lastUpdate = 0;

            /**
             * Méthode appelée périodiquement par le fil JavaFX.
             * Se charge de traiter les messages.
             * Vide simplement la file partagée avec le fil chargé d'obtenir les messages et passe chacun
             * des éléments qu'elle contient à la méthode updateWithMessage de l'instance de AircraftStateManager
             * chargée de gérer les états des aéronefs.
             * Cela provoque, indirectement, la mise à jour de l'interface graphique.
             *
             * @param now La fréquence utilisée pour appeler la méthode handle() (en nanosecondes).
             */
            @Override
            public void handle(long now) {
                while (!queue.isEmpty()) {
                    Message message = MessageParser.parse(queue.poll());
                    if (message != null) {
                        try {
                            asm.updateWithMessage(message);
                            slc.addMessage();
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    }
                }
                if (now - lastUpdate >= PURGE_UPDATE_NS) {
                    asm.purge();
                    lastUpdate = now;
                }
            }
        }.start();
    }
}