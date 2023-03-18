package ch.epfl.javions.adsb;

import ch.epfl.javions.ByteString;
import ch.epfl.javions.Units;
import ch.epfl.javions.demodulation.AdsbDemodulator;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HexFormat;

import static org.junit.jupiter.api.Assertions.*;

class AirbornePositionMessageTest {
    private static boolean isTypeCodeValid(int typeCode) {
        return ((typeCode >= 9) && (typeCode <= 18)) || ((typeCode >= 20) && (typeCode <= 22));
    }

    @Test
    void testAirbornePositionMessageWithSamplesDOTBin() throws IOException {
        var samplesResourceMac = "/Users/adam/Downloads/samples_20230304_1442.bin";
        var samplesResourceWindows = "C:\\Users\\WshLaStreet\\Downloads\\samples_20230304_1442.bin";
        try (InputStream s = new FileInputStream(samplesResourceMac)) {
            AdsbDemodulator d = new AdsbDemodulator(s);
            RawMessage m;
            int numberOfMessages = 0;
            while ((m = d.nextMessage()) != null) {
                if(AirbornePositionMessage.of(m) != null && isTypeCodeValid(m.typeCode())) {
                    System.out.println(AirbornePositionMessage.of(m));
                    ++numberOfMessages;
                }
            }
            System.out.println(numberOfMessages);
        }
    }

    @Test
    void testAirbornePositionMessageWithQ0() {
        var bytes = HexFormat.of().parseHex("8DAE02C85864A5F5DD4975A1A3F5");
        var byteString = new ByteString(bytes);
        var RawMessage = new RawMessage(1L,byteString);
        System.out.println(AirbornePositionMessage.of(RawMessage));
    }
}
