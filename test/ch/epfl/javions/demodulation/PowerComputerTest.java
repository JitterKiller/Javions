package ch.epfl.javions.demodulation;

import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.util.Objects;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.*;

class PowerComputerTest {

    @Test
    void PowerComputerTest10FirstValuesSamplesDOTBin() throws IOException {
        var samplesResourceUrl = getClass().getResource("/samples.bin");
        InputStream stream = new FileInputStream(Objects.requireNonNull(samplesResourceUrl).getFile());
        var power = new PowerComputer(stream,32);
        var expectedValues = new int[]{73, 292, 65, 745, 98, 4226, 12244, 25722, 36818, 23825};
        var samplesBuffer = new int[32];
        power.readBatch(samplesBuffer);
        for(int i = 0; i < 10; ++i){
            assertEquals(expectedValues[i],samplesBuffer[i]);
        }
    }

    @Test
    void readBatchWorksWithLicitArguments() throws IOException{
        String s = getClass().getResource("/samples.bin").getFile();
        s = URLDecoder.decode(s, UTF_8);
        InputStream stream = new FileInputStream(s);
        PowerComputer decoder = new PowerComputer(stream,2400/2);
        int[] batch = new int[2400/2];
        decoder.readBatch(batch);
        int [] expected = new int[]{73, 292, 65, 745, 98, 4226, 12244, 25722, 36818, 23825};
        for (int i = 0; i <10 ; i++) {
            assertEquals(expected[i],batch[i]);
        }
    }

    @Test
    void constructorThrowsNegativeSize() throws IOException {
        String testFile = getClass().getResource("/samples.bin").getFile();
        testFile = URLDecoder.decode(testFile, UTF_8);
        try(InputStream stream = new FileInputStream(testFile)) {
            assertThrows(IllegalArgumentException.class, () -> new PowerComputer(stream, -1));
        }
    }

    @Test
    void constructorThrowsIllegalSize() throws IOException {
        String testFile = getClass().getResource("/samples.bin").getFile();
        testFile = URLDecoder.decode(testFile, UTF_8);
        try(InputStream stream = new FileInputStream(testFile)) {
            assertThrows(IllegalArgumentException.class, () -> new PowerComputer(stream, 9));
        }
    }

    @Test
    void readBatchFailsWithIllicitArgument() throws IOException{
        String testFile = getClass().getResource("/samples.bin").getFile();
        testFile = URLDecoder.decode(testFile, UTF_8);
        InputStream stream = new FileInputStream(testFile);
        PowerComputer powerComputer = new PowerComputer(stream, 16);
        int[] testArray = new int[6];

        assertThrows(IllegalArgumentException.class, () -> powerComputer.readBatch(testArray));
    }

}
