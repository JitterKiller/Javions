package ch.epfl.javions.adsb;

import ch.epfl.javions.ByteString;
import ch.epfl.javions.demodulation.AdsbDemodulator;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HexFormat;

public class MyAirborneVelocityMessageTest {
    private static boolean isTypeCodeValid(int typeCode) {
        return (typeCode == 19);
    }

    @Test
    void testAircraftVelocityMessageWithSamplesDOTBin() throws IOException {
        var samplesResourceMac = "/Users/adam/Downloads/samples_20230304_1442.bin";
        var samplesResourceWindows = "C:\\Users\\WshLaStreet\\Downloads\\samples_20230304_1442.bin";
        try (InputStream s = new FileInputStream(samplesResourceWindows)) {
            AdsbDemodulator d = new AdsbDemodulator(s);
            RawMessage m;
            int numberOfMessages = 0;
            while ((m = d.nextMessage()) != null) {
                if(AirborneVelocityMessage.of(m) != null && isTypeCodeValid(m.typeCode())) {
                    System.out.println(AirborneVelocityMessage.of(m));
                    ++numberOfMessages;
                }
            }
            System.out.println(numberOfMessages);
        }
    }

    @Test
    void testAircraftVelocityMessageWithSubType3() {
        var bytes = HexFormat.of().parseHex("8DA05F219B06B6AF189400CBC33F");
        var byteString = new ByteString(bytes);
        var RawMessage = new RawMessage(1L,byteString);
        System.out.println(AirborneVelocityMessage.of(RawMessage));
    }
}
