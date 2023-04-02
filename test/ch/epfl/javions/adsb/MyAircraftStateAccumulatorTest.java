package ch.epfl.javions.adsb;

import ch.epfl.javions.GeoPos;
import ch.epfl.javions.aircraft.IcaoAddress;
import ch.epfl.javions.demodulation.AdsbDemodulator;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

class MyAircraftStateAccumulatorTest implements AircraftStateSetter {

    @Override
    public void setLastMessageTimeStampNs(long timeStampNs) {
        System.out.println("timeStampNs : " + timeStampNs);
    }

    @Override
    public void setCategory(int category) {
        System.out.println("category : " + category);
    }

    @Override
    public void setCallSign(CallSign callSign) {
        if(callSign != null) System.out.println("indicatif : " + callSign);
    }

    @Override
    public void setPosition(GeoPos position) {
        if(position != null) System.out.println("position : " + position);
    }

    @Override
    public void setAltitude(double altitude) {
        System.out.println("altitude : " + altitude);
    }

    @Override
    public void setVelocity(double velocity) {
        System.out.println("velocity : " + velocity);
    }

    @Override
    public void setTrackOrHeading(double trackOrHeading) {
        System.out.println("trackOrHeading : " + trackOrHeading);
    }

    @Test
    void testAircraftStateAccumulatorWithExampleValues() throws IOException {
        var samplesResourceMac = "/Users/adam/Downloads/samples_20230304_1442.bin";
        var samplesResourceWindows = "C:\\Users\\WshLaStreet\\Downloads\\samples_20230304_1442.bin";
        IcaoAddress expectedAddress = new IcaoAddress("4D2228");
        try (InputStream s = new FileInputStream(samplesResourceMac)) {
            AdsbDemodulator d = new AdsbDemodulator(s);
            RawMessage m;
            AircraftStateAccumulator<MyAircraftStateAccumulatorTest> a =
                    new AircraftStateAccumulator<>(new MyAircraftStateAccumulatorTest());
            while ((m = d.nextMessage()) != null) {
                if (!m.icaoAddress().equals(expectedAddress)) continue;

                Message pm = MessageParser.parse(m);
                if (pm != null) a.update(pm);
            }
        }
    }

    @Test
    void stateAccumulatesOnAllIcaoAddress() throws IOException {
        var samplesResourceMac = "/Users/adam/Downloads/samples_20230304_1442.bin";
        var samplesResourceWindows = "C:\\Users\\WshLaStreet\\Downloads\\samples_20230304_1442.bin";
        String[] icaoAddresses = new String[]{"4B17E5", "495299", "39D300", "4241A9", "4B1A00", "4D2228", "440237", "4D029F",
                "01024C", "3C6481", "4B2964", "4B1900", "4BCDE9", "4D0221", "4CA2BF", "4951CE", "39CEAA",
                "A4F239", "394C13", "4B17E1", "3C6545", "4CAC87", "4BB279", "4B1A1E"};
        try (InputStream s = new FileInputStream(samplesResourceMac)) {
            AdsbDemodulator d = new AdsbDemodulator(s);
            RawMessage m;
            AircraftStateAccumulator<MyAircraftStateAccumulatorTest>[] aircraftStates = new AircraftStateAccumulator[icaoAddresses.length];
            AircraftStateAccumulator<MyAircraftStateAccumulatorTest> a =
                    new AircraftStateAccumulator<>(new MyAircraftStateAccumulatorTest());
            while ((m = d.nextMessage()) != null) {
                for (int i = 0; i < icaoAddresses.length; i++) {
                    if (m.icaoAddress().string().equals(icaoAddresses[i])) {
                        Message pm = MessageParser.parse(m);

                        if (aircraftStates[i] == null) {

                            aircraftStates[i] = new AircraftStateAccumulator<>(new MyAircraftStateAccumulatorTest());
                        }
                        if (pm != null) {
                            aircraftStates[i].update(pm);
                        }
                    }
                }
            }
        }
    }
}
