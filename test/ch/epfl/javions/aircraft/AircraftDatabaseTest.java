package ch.epfl.javions.aircraft;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class AircraftDatabaseTest {

    @Test
    void testGetExistingAircraft() throws IOException {
        AircraftDatabase database = new AircraftDatabase("/aircraft.zip");
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
        AircraftDatabase database = new AircraftDatabase("/aircraft.zip");
        IcaoAddress address = new IcaoAddress("000000");
        AircraftData data = database.get(address);
        assertNull(data);
    }

    @Test
    void testGetNullAddress() {
        assertThrows(NullPointerException.class, () -> {
            new AircraftDatabase("/aircraft.zip").get(null);
        });
    }

    @Test
    void testGetInvalidZipFile() {
        assertThrows(IOException.class, () -> {
            new AircraftDatabase("/path/to/invalid.zip");
        });
    }
}
