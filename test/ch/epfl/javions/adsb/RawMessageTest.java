package ch.epfl.javions.adsb;

import ch.epfl.javions.ByteString;
import ch.epfl.javions.ByteString;
import ch.epfl.javions.aircraft.IcaoAddress;
import org.junit.jupiter.api.Test;

import java.util.HexFormat;

import static org.junit.jupiter.api.Assertions.*;

class RawMessageTest {

    private static final long timeStampNs = 1L;

    @Test
    void testConstructor() {
        assertThrows(IllegalArgumentException.class, () -> new RawMessage(-1, new ByteString(new byte[14])));
        assertThrows(IllegalArgumentException.class, () -> new RawMessage(0, new ByteString(new byte[10])));
    }

    @Test
    void testIcaoAddress() {
        var bytes1 = HexFormat.of().parseHex("8D4B17E5F8210002004BB8B1F1AC");
        var byteString1 = new ByteString(bytes1);
        var rawMessage1 = new RawMessage(timeStampNs,byteString1);
        assertEquals(new IcaoAddress("4B17E5"),rawMessage1.icaoAddress());

        var bytes2 = HexFormat.of().parseHex("8D49529958B302E6E15FA352306B");
        var byteString2 = new ByteString(bytes2);
        var rawMessage2 = new RawMessage(timeStampNs,byteString2);
        assertEquals(new IcaoAddress("495299"),rawMessage2.icaoAddress());
    }

    @Test
    void testDownLinkFormat() {
        var validBytes = HexFormat.of().parseHex("8D4B17E5F8210002004BB8B1F1AC");
        var validByeString = new ByteString(validBytes);
        var validRawMessage = new RawMessage(timeStampNs,validByeString);
        StringBuilder bitStringBuilder = new StringBuilder();
        for(byte b : validBytes) {
            for(int i = 7; i >= 0; i--) {
                bitStringBuilder.append((b >> i) & 1);
            }
        }
        String bitString = bitStringBuilder.toString();
        System.out.println(bitString);
        assertEquals(17,validRawMessage.downLinkFormat());

        var invalidBytes = HexFormat.of().parseHex("7A6E944F3AD0CC5A3101CC7299D8");
        var invalidString = new ByteString(invalidBytes);
        var invalidRawMessage = new RawMessage(timeStampNs,invalidString);
        assertEquals(15,invalidRawMessage.downLinkFormat());
    }

    @Test
    void testPayload() {
        var bytes = HexFormat.of().parseHex("8D4B17E5F8210002004BB8B1F1AC");
        var byteString = new ByteString(bytes);
        var RawMessage = new RawMessage(timeStampNs,byteString);
        assertEquals(69842078141533112L,RawMessage.payload());
    }

    @Test
    void testStaticTyeCode() {
        var payload = 0b11111000001000010000000000000010000000000100101110111000L;
        assertEquals(31, ch.epfl.javions.adsb.RawMessage.typeCode(payload));
    }

    @Test
    void testTypeCode() {
        var bytes = HexFormat.of().parseHex("8D4B17E5F8210002004BB8B1F1AC");
        var byteString = new ByteString(bytes);
        var RawMessage = new RawMessage(timeStampNs,byteString);
        assertEquals(31,RawMessage.typeCode());
    }
}
