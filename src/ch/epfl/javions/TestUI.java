package ch.epfl.javions;

import ch.epfl.javions.adsb.Message;
import ch.epfl.javions.adsb.MessageParser;
import ch.epfl.javions.adsb.RawMessage;
import ch.epfl.javions.aircraft.AircraftDatabase;
import ch.epfl.javions.gui.AircraftStateManager;
import ch.epfl.javions.gui.ObservableAircraftState;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Comparator;

public class TestUI {

    public static void main(String[] args) throws IOException, InterruptedException {
        String CSI = "\u001B[";
        String CLEAR_SCREEN = CSI + "2J";
        var fileMac = "/Users/adam/Documents/CS-108/Javions/resources/messages_20230318_0915.bin";
        var databaseFileMac = "/Users/adam/Documents/CS-108/Javions/resources/aircraft.zip";
        try (DataInputStream s = new DataInputStream(new BufferedInputStream(
                new FileInputStream(fileMac)))) {
            long startTime = System.nanoTime();
            var database = new AircraftDatabase(databaseFileMac);
            var manager = new AircraftStateManager(database);
            var bytes = new byte[RawMessage.LENGTH];
            System.out.println(CLEAR_SCREEN);

            while (true) {
                long timeStampNs = s.readLong();
                int bytesRead = s.readNBytes(bytes, 0, bytes.length);
                assert bytesRead == RawMessage.LENGTH;
                long elapsedTimeMs = (System.nanoTime() - startTime) / 1_000_000;
                long timeToWaitMs = (timeStampNs  / 1_000_000) - elapsedTimeMs;
                Message pm = MessageParser.parse(new RawMessage(timeStampNs, new ByteString(bytes)));
                if (pm != null) {
                    Thread.sleep(Math.max(0,timeToWaitMs));
                    manager.updateWithMessage(pm);
                    ArrayList<ObservableAircraftState> states = new ArrayList<>(manager.states());
                    states.sort(new AddressComparator());
                    if (!states.isEmpty()) {
                        manager.purge();
                        printTable(states);
                    }
                }
            }
        }
    }

    private static void printTable(ArrayList<ObservableAircraftState> states) {
        final char[] directions = new char[]{'↑', '↗', '→', '↘', '↓', '↙', '←', '↖'};
        System.out.println("\u001B[2J");
        System.out.printf("%-7s %-9s %-7s %-18s %-11s %-10s %-5s %-4s\n", "OACI", "Indicatif", "Immat.", "Modèle", "Longitude", "Latitude", "Alt.", "Vit.");
        System.out.printf("――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――%n");
        for (ObservableAircraftState state : states) {
            int directionIndex = getDirectionArrow(state.getTrackOrHeading());
            String model;
            if (state.getData() != null && state.getData().model().length() >= 17) {
                model = state.getData().model().substring(0, 16) + "…";
            } else if (state.getData() != null) {
                model = state.getData().model();
            } else model = "";
            System.out.printf("%-7s %-9s %-7s %-17.17s %10.5f %10.5f %6d %5.0f %1s\n",
                    state.getAddress().string(),
                    state.getCallSign() == null ? "" : state.getCallSign().string(),
                    state.getData() == null ? "" : state.getData().registration().string(),
                    model,
                    Units.convert(state.getTrajectory().get(state.getTrajectory().size() - 1).getKey().longitudeT32(), Units.Angle.T32, Units.Angle.DEGREE),
                    Units.convert(state.getTrajectory().get(state.getTrajectory().size() - 1).getKey().latitudeT32(), Units.Angle.T32, Units.Angle.DEGREE),
                    (int) Math.rint(state.getTrajectory().get(state.getTrajectory().size() - 1).getValue()),
                    Double.isNaN(state.getVelocity()) ? Double.NaN : (int) Math.rint(Units.convertTo(state.getVelocity(), Units.Speed.KILOMETER_PER_HOUR)),
                    Array.getChar(directions, directionIndex));
        }
    }

    private static int getDirectionArrow(double trackOrHeading) {
        double offset = Math.PI / 8;
        if (trackOrHeading >= 0 + offset && trackOrHeading <= Math.PI) {
            if (trackOrHeading <= (Math.PI / 2) - offset) {
                return 1;
            } else if (trackOrHeading > ((Math.PI / 2) - offset) && trackOrHeading <= (Math.PI / 2) + offset) {
                return 2;
            } else if (trackOrHeading < Math.PI - offset) {
                return 3;
            } else {
                return 4;
            }
        } else if (trackOrHeading > Math.PI && trackOrHeading <= (2d * Math.PI) - offset) {
            if (trackOrHeading <= Math.PI + offset) {
                return 4;
            } else if (trackOrHeading > (Math.PI + offset) && (trackOrHeading < ((1.5d * Math.PI) - offset))) {
                return 5;
            } else if (trackOrHeading >= ((1.5d * Math.PI) - offset) && trackOrHeading <= ((1.5d * Math.PI) + offset)) {
                return 6;
            } else {
                return 7;
            }
        }
        return 0;
    }

    private static class AddressComparator
            implements Comparator<ObservableAircraftState> {
        @Override
        public int compare(ObservableAircraftState o1,
                           ObservableAircraftState o2) {
            String s1 = o1.getAddress().string();
            String s2 = o2.getAddress().string();
            return s1.compareTo(s2);
        }
    }
}
