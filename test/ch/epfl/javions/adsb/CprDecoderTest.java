package ch.epfl.javions.adsb;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CprDecoderTest {

    @Test
    void testCprDecoderWithExampleValues(){

        var x0 = Math.scalb(111600d, -17);
        var y0 = Math.scalb(94445d, -17);
        var x1 = Math.scalb(108865d, -17);
        var y1 = Math.scalb(77558d, -17);

        var p = CprDecoder.decodePosition(x0, y0, x1, y1, 0);

        assertEquals(89192898, p.longitudeT32());
        assertEquals(552659081,p.latitudeT32());
        assertEquals("(7.476062346249819°, 46.323349038138986°)", p.toString());

    }

}
