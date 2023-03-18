package ch.epfl.javions.adsb;

import ch.epfl.javions.demodulation.AdsbDemodulator;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

class AirbornePositionMessageTest {
    private static boolean isTypeCodeValid(int typeCode) {
        return ((typeCode >= 9) && (typeCode <= 18)) || ((typeCode >= 20) && (typeCode <= 22));
    }

    @Test
    void testAirbornePositionMessageTestWithSamplesDOTBin() throws IOException {
        var samplesResourceMac = "/Users/adam/Downloads/samples_20230304_1442.bin";
        var samplesResourceWindows = "C:\\Users\\WshLaStreet\\Downloads\\samples_20230304_1442.bin";
        try (InputStream s = new FileInputStream(samplesResourceWindows)) {
            AdsbDemodulator d = new AdsbDemodulator(s);
            RawMessage m;
            int numberOfMessages = 0;
            while ((m = d.nextMessage()) != null) {
                if(AirbornePositionMessage.of(m) != null ) {
                    System.out.println(AirbornePositionMessage.of(m));
                    ++numberOfMessages;
                }
            }
            System.out.println(numberOfMessages);
        }
    }
}
