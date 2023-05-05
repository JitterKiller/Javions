package ch.epfl.javions.gui;

import ch.epfl.javions.ByteString;
import ch.epfl.javions.adsb.Message;
import ch.epfl.javions.adsb.MessageParser;
import ch.epfl.javions.adsb.RawMessage;
import ch.epfl.javions.aircraft.AircraftDatabase;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.*;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class TestAircraftTableController extends Application {

    public static void main(String[] args) { launch(args); }

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

        // Création de la base de données
        URL dbUrl = getClass().getResource("/aircraft.zip");
        assert dbUrl != null;
        String f = Path.of(dbUrl.toURI()).toString();
        var db = new AircraftDatabase(f);

        AircraftStateManager asm = new AircraftStateManager(db);
        ObjectProperty<ObservableAircraftState> sap =
                new SimpleObjectProperty<>();
        AircraftTableController atc =
                new AircraftTableController(asm.states(),sap);
        primaryStage.setScene(new Scene(atc.pane()));
        primaryStage.show();

        URL mURL = getClass().getResource("/messages_20230318_0915.bin");
        assert mURL != null;
        String u = Path.of(mURL.toURI()).toString();

        var mi = readAllMessages(u)
                .iterator();

        // Animation des aéronefs
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                try {
                    for (int i = 0; i < 1; i += 1) {
                        assert mi.next() != null;
                        Message m = MessageParser.parse(mi.next());
                        asm.purge();
                        if (m != null) asm.updateWithMessage(m);
                    }
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
        }.start();
    }
}
