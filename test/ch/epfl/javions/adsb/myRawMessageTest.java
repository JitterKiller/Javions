package ch.epfl.javions.adsb;

import ch.epfl.javions.ByteString;
import ch.epfl.javions.aircraft.IcaoAddress;
import org.junit.jupiter.api.Test;

import java.util.HexFormat;

import static org.junit.jupiter.api.Assertions.*;

class myRawMessageTest {

    private static final long timeStampNs = 1L;
    private final RawMessage message = new RawMessage(8096200,
            ByteString.ofHexadecimalString("8D4B17E5F8210002004BB8B1F1AC"));


    private final HexFormat hf = HexFormat.of().withUpperCase();
    private final ByteString byteString = new ByteString(hf.parseHex("F8210002004BB8"));

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

    @Test
    void validIcaoAddressExtracted(){
        assertEquals("4B17E5" ,message.icaoAddress().string());
    }
    @Test
    void payloadIsValid(){
        assertEquals(byteString.bytesInRange(0,7),message.payload());
    }

    @Test
    void typeCodeIsValid(){
        int validTypeCode = Byte.toUnsignedInt((byte)(message.payload() >> 51));
        assertEquals(validTypeCode,message.typeCode());
    }

    @Test
    void sizeTest(){
        assertEquals(14, RawMessage.size((byte) 0x8D));
    }
    public static byte[] hexStringToBytes(String hexString) {
        int length = hexString.length();
        byte[] result = new byte[length / 2];

        for (int i = 0; i < length; i += 2) {
            result[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
                    + Character.digit(hexString.charAt(i+1), 16));
        }

        return result;
    }

    byte[] bytes = hexStringToBytes("8D4B17E5F8210002004BB8B1F1AC");

    RawMessage adsb =new RawMessage(8096200,new ByteString(bytes));



    @Test
    void of() {
    }

    @Test
    void size() {
        System.out.println(Byte.toUnsignedInt(bytes[0]));
        assertEquals(14,RawMessage.size(bytes[0]));
    }

    @Test
    void typeCode() {
    }

    @Test
    void downLinkFormat() {
        assertEquals(0b10001,adsb.downLinkFormat());
    }

    @Test
    void icaoAddress() {

        assertEquals("4B17E5",adsb.icaoAddress().string());
    }

    @Test
    void payload() {
        assertEquals(69842078141533112L,adsb.payload());
    }

    @Test
    void testTypeCodeBis() {
        assertEquals(31,adsb.typeCode());
    }

    @Test
    void timeStampNs() {
    }

    @Test
    void bytes() {
    }
    private final static ByteString rawMessage1 = new ByteString(new byte[]{
            (byte) 0x8D, (byte) 0x4B, (byte) 0x17, (byte) 0xE5,
            (byte) 0xF8, (byte) 0x21, (byte) 0x00, (byte) 0x02,
            (byte) 0x00, (byte) 0x4B, (byte) 0xB8, (byte) 0xB1,
            (byte) 0xF1, (byte) 0xAC});

    @Test
    void rawMessageConstructorsThrowsOnNegativeTimestamp() {
        assertThrows(IllegalArgumentException.class, () -> new RawMessage(-1, rawMessage1));
    }

    @Test
    void rawMessageConstructorsThrowsOnInvalidMessageLength() {
        assertThrows(IllegalArgumentException.class, () -> new RawMessage(8096200, new ByteString(new byte[0])));
    }

    @Test
    void typecodeWithParameterReturnsTheCorrectValue() {
        long payload = 0xF8210002004BB8L; // 1111_1000 _0010_0001 _0000_0000 _0000_0010 _0000_0000 _0100_1011 _1011_1000
        int actual = RawMessage.typeCode(payload);
        int expected = 31;
        assertEquals(expected, actual);
    }

    @Test
    void downLinkFormatReturnsTheCorrectValue() {
        RawMessage rawMessage = new RawMessage(8096200, rawMessage1);
        int actual = rawMessage.downLinkFormat();
        int expected = 17;
        assertEquals(expected, actual);
    }

    @Test
    void icaoAddressWorksOnTrivialValues() {
        RawMessage rawMessage = new RawMessage(8096200, rawMessage1);
        IcaoAddress expected = new IcaoAddress("4B17E5");
        IcaoAddress actual = rawMessage.icaoAddress();
        assertEquals(expected, actual);
    }

    @Test
    void payloadWorksOnTrivialValues() {
        RawMessage rawMessage = new RawMessage(8096200, rawMessage1);
        long expected = 0xF8210002004BB8L;
        long actual = rawMessage.payload();
        assertEquals(expected, actual);
    }

    @Test
    void typeCodeWithoutParameterWorksOnTrivialValues() {
        RawMessage rawMessage = new RawMessage(8096200, rawMessage1);
        int expected = 31;
        int actual = rawMessage.typeCode();
        assertEquals(expected, actual);
    }
}
