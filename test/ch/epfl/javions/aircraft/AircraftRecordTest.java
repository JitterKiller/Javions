package ch.epfl.javions.aircraft;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AircraftRecordTest {

    @Test
    void aircraftRegistrationValidTest(){
        assertThrows(IllegalArgumentException.class, () -> {
            new AircraftRegistration("//*");
        });

        assertDoesNotThrow(() -> {
            new AircraftRegistration("AAA0001");
        });
    }

    @Test
    void aircraftDescriptionValidTest(){
        assertThrows(IllegalArgumentException.class, () -> {
            new AircraftDescription("//*");
        });

        assertDoesNotThrow(() -> {
            new AircraftDescription("A6E");
        });
    }
    @Test
    void icaoAddressValidTest(){
        assertThrows(IllegalArgumentException.class, () -> {
            new IcaoAddress("//*");
        });

        assertDoesNotThrow(() -> {
            new IcaoAddress("AAAAAA");
        });
    }
    @Test
    void typeDesignatorValidTest(){
        assertThrows(IllegalArgumentException.class, () -> {
            new AircraftTypeDesignator("//*");
        });

        assertDoesNotThrow(() -> {
            new AircraftTypeDesignator("A20N");
        });
    }
}
