package ch.epfl.javions;

import ch.epfl.javions.adsb.Message;
import ch.epfl.javions.adsb.MessageParser;
import ch.epfl.javions.adsb.RawMessage;
import ch.epfl.javions.aircraft.AircraftDatabase;
import ch.epfl.javions.gui.AircraftStateManager;
import ch.epfl.javions.gui.ObservableAircraftState;

import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;

public class TestUI {

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


    public static void main(String[] args) throws IOException {
        String CSI = "\u001B[";
        String CLEAR_SCREEN = CSI + "2J";
        var fileMac = "/Users/adam/Documents/CS-108/Javions/resources/messages_20230318_0915.bin";
        var databaseFileMac = "/Users/adam/Documents/CS-108/Javions/resources/aircraft.zip";
        try (DataInputStream s = new DataInputStream(new BufferedInputStream(
                new FileInputStream(fileMac)))) {
            long startTimeNs = System.nanoTime();
            var database = new AircraftDatabase(databaseFileMac);
            var manager = new AircraftStateManager(database);
            var bytes = new byte[RawMessage.LENGTH];
            System.out.println(CLEAR_SCREEN);
            while (true) {
                long timeStampNs = s.readLong();
                int bytesRead = s.readNBytes(bytes, 0, bytes.length);
                assert bytesRead == RawMessage.LENGTH;
                Message pm = MessageParser.parse(new RawMessage(timeStampNs,new ByteString(bytes)));
                if(pm != null) {
                    manager.updateWithMessage(pm);
                    manager.purge();
                }
                ArrayList<ObservableAircraftState> states = new ArrayList<>(manager.states());
                states.sort(new AddressComparator());
                if(!states.isEmpty()) {
                    Thread.sleep(Math.max(0,(timeStampNs - (System.nanoTime() - startTimeNs)) / 1000000));
                    printTable(states);
                }
            }
        } catch (IOException ignored){

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static void printTable(ArrayList<ObservableAircraftState> states) throws InterruptedException {
        System.out.println("\u001B[2J");
        System.out.printf("%-7s %-9s %-7s %-18s %-11s %-10s %-5s %-5s\n","OACI","Indicatif","Immat.","Modèle","Longitude","Latitude","Alt.","Vit.");
        System.out.printf("――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――――%n");
        for(ObservableAircraftState state: states) {
            final char[] directions = new char[]{'↑', '↗', '→', '↘', '↓', '↙', '←', '↖'};
            String model;
            if(state.getData() != null && state.getData().model().length() >= 17) {
                model = state.getData().model().substring(0,16) + "…";
            } else if (state.getData() != null) {
                model = state.getData().model();
            } else model = "";
            System.out.printf("%-7s %-9s %-7s %-17.17s %10.5f %10.5f %6d %5.0f\n",
                    state.getAddress().string(),
                    state.getCallSign() == null ? "" : state.getCallSign().string(),
                    state.getData() == null ? "" : state.getData().registration().string(),
                    model,
                    Units.convertTo(state.getTrajectory().get(state.getTrajectory().size() - 1).getKey().longitude(),Units.Angle.DEGREE),
                    Units.convertTo(state.getTrajectory().get(state.getTrajectory().size() - 1).getKey().latitude(),Units.Angle.DEGREE),
                    (int) Math.rint(state.getTrajectory().get(state.getTrajectory().size() - 1).getValue()),
                    Double.isNaN(state.getVelocity()) ? Double.NaN : (int) Math.rint(Units.convertTo(state.getVelocity(),Units.Speed.KILOMETER_PER_HOUR)));
        }
        Thread.sleep(50);
    }
}
