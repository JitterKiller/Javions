package ch.epfl.javions.adsb;

import ch.epfl.javions.ByteString;
import ch.epfl.javions.demodulation.AdsbDemodulator;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HexFormat;

import static org.junit.jupiter.api.Assertions.*;

public class MyAirborneVelocityMessageTest {
    private static boolean isTypeCodeValid(int typeCode) {
        return (typeCode == 19);
    }

    @Test
    void testAirborneVelocityMessageWithSamplesDOTBin() throws IOException {
        var samplesResourceMac = "/Users/adam/Downloads/samples_20230304_1442.bin";
        var samplesResourceWindows = "C:\\Users\\WshLaStreet\\Downloads\\samples_20230304_1442.bin";
        try (InputStream s = new FileInputStream(samplesResourceMac)) {
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
            assertEquals(147,numberOfMessages);
        }
    }

    @Test
    void testAircraftVelocityMessagesAB() {
        var bytesA = HexFormat.of().parseHex("8D485020994409940838175B284F");
        var byteStringA = new ByteString(bytesA);
        var RawMessageA = new RawMessage(1L,byteStringA);
        assertEquals(81.90013721178154,AirborneVelocityMessage.of(RawMessageA).speed());
        assertEquals(3.1918647255875205,AirborneVelocityMessage.of(RawMessageA).trackOrHeading());

        var bytesB = HexFormat.of().parseHex("8DA05F219B06B6AF189400CBC33F");
        var byteStringB = new ByteString(bytesB);
        var RawMessageB = new RawMessage(2L,byteStringB);
        assertEquals(192.91666666666669,AirborneVelocityMessage.of(RawMessageB).speed());
        assertEquals(4.25833066717054,AirborneVelocityMessage.of(RawMessageB).trackOrHeading());
    }
}
