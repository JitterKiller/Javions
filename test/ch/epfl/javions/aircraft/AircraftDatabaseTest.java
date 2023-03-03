package ch.epfl.javions.aircraft;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URLDecoder;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.*;

class AircraftDatabaseTest {

    @Test
    void testGetExistingAircraft() throws IOException {
        String d = getClass().getResource("/aircraft.zip").getFile();
        d = URLDecoder.decode(d, UTF_8);
        AircraftDatabase database = new AircraftDatabase(d);
        IcaoAddress address = new IcaoAddress("00820B");
        AircraftRegistration registration = new AircraftRegistration("ZS-ATM");
        AircraftTypeDesignator typeDesignator = new AircraftTypeDesignator("PC12");
        String model = "PILATUS PC-12";
        AircraftDescription description = new AircraftDescription("L1T");
        WakeTurbulenceCategory wakeTurbulenceCategory = WakeTurbulenceCategory.LIGHT;
        AircraftData data = new AircraftData(registration,typeDesignator,model,description,wakeTurbulenceCategory);
        assertNotNull(database.get(address));
        assertEquals(data, database.get(address));
    }

    @Test
    void testGetNonExistingAircraft() throws IOException {
        String d = getClass().getResource("/aircraft.zip").getFile();
        d = URLDecoder.decode(d, UTF_8);
        AircraftDatabase database = new AircraftDatabase(d);
        IcaoAddress address = new IcaoAddress("000000");
        AircraftData data = database.get(address);
        assertNull(data);
    }

    @Test
    void testGetNullAddress() {
        assertThrows(NullPointerException.class, () -> {
            new AircraftDatabase(null);
        });
    }

}
