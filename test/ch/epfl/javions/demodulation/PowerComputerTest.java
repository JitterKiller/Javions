package ch.epfl.javions.demodulation;

import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class PowerComputerTest {

    @Test
    void PowerComputerTest10FirstValuesSamplesDOTBin() throws IOException {
        var samplesResourceUrl = getClass().getResource("/samples.bin");
        InputStream stream = new FileInputStream(Objects.requireNonNull(samplesResourceUrl).getFile());
        var power = new PowerComputer(stream,16);
        var expectedValues = new int[]{73, 292, 65, 745, 98, 4226, 12244, 25722};
        var samplesBuffer = new int[16];
        power.readBatch(samplesBuffer);
        for(int i = 0; i < 8; ++i){
            assertEquals(expectedValues[i],samplesBuffer[i]);
        }
    }

}
