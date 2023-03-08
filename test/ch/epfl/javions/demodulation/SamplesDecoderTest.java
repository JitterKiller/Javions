package ch.epfl.javions.demodulation;

import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;


class SamplesDecoderTest {

    @Test
    void SamplesDecoderTest10FirstValuesSamplesDOTBin() throws IOException {
        var samplesResourceUrl = getClass().getResource("/samples.bin");
        InputStream stream = new FileInputStream(Objects.requireNonNull(samplesResourceUrl).getFile());
        var decoder = new SamplesDecoder(stream,10);
        var expectedValues = new short[]{-3, 8, -9, -8, -5, -8, -12, -16, -23, -9};
        var batch = new short[10];
        decoder.readBatch(batch);
        for(int i = 0; i < 10; ++i) {
            assertEquals(expectedValues[i],batch[i]);
        }
    }

}
