package ch.epfl.javions.adsb;

import ch.epfl.javions.GeoPos;
import ch.epfl.javions.Units;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MyCprDecoderTest {

    private final static double DIVIDER = Math.scalb(1.0, 17);

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

    @Test
    void testCprDecoderWithTeleValues() {
        GeoPos pos = CprDecoder.decodePosition(0.62d,0.42d,0.6200000000000000001d,0.4200000000000000001d,0);

        System.out.println(pos.toString());
    }

    @Test
    void testCprDecoderWithTrivialValues() {
            double x0 = 111600 / DIVIDER;
            double y0 = 94445 / DIVIDER;
            double x1 = 108865 / DIVIDER;
            double y1 = 77558 / DIVIDER;
            double expectedLat0 = Units.convertFrom((1.0 / 60) * (7 + y0), Units.Angle.TURN);
            double expectedLat1 = Units.convertFrom((1.0 / 59) * (7 + y1), Units.Angle.TURN);
            double expectedLon0 = Units.convertFrom((1.0 / 41) * x0, Units.Angle.TURN);
            double expectedLon1 = Units.convertFrom((1.0 / 40) * x1, Units.Angle.TURN);
            int mostRecent = 0;

            GeoPos pos = CprDecoder.decodePosition(x0, y0, x1, y1, mostRecent);

            assertNotNull(pos);
            assertEquals(expectedLat0, pos.latitude(), 1e-9);
            assertEquals(expectedLon0, pos.longitude(), 1e-9);

            mostRecent = 1;

            pos = CprDecoder.decodePosition(x0, y0, x1, y1, mostRecent);

            assertNotNull(pos);
            assertEquals(expectedLat1, pos.latitude(), 1e-9);
            assertEquals(expectedLon1, pos.longitude(), 1e-9);

            mostRecent = 2;

            int finalMostRecent = mostRecent;
            assertThrows(IllegalArgumentException.class, () -> {
                CprDecoder.decodePosition(x0, y0, x1, y1, finalMostRecent);
            });
        }
    }
