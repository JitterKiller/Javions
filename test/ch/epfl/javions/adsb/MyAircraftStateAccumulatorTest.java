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
        //System.out.println("timeStampNs : " + timeStampNs);
    }

    @Override
    public void setCategory(int category) {
        //System.out.println("category : " + category);
    }

    @Override
    public void setCallSign(CallSign callSign) {
        System.out.println("indicatif : " + callSign);
    }

    @Override
    public void setPosition(GeoPos position) {
        System.out.println("position : " + position);
    }

    @Override
    public void setAltitude(double altitude) {
        //System.out.println("altitude : " + altitude);
    }

    @Override
    public void setVelocity(double velocity) {
        //System.out.println("velocity : " + velocity);
    }

    @Override
    public void setTrackOrHeading(double trackOrHeading) {
        //System.out.println("trackOrHeading : " + trackOrHeading);
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
}

