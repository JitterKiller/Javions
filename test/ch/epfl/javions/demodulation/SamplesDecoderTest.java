package ch.epfl.javions.demodulation;

import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.util.Objects;

import static java.nio.charset.StandardCharsets.UTF_8;
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

    @Test
    void constructorThrowsNegativeSize() throws IOException {
        var samplesResourceUrl = getClass().getResource("/samples.bin");
        InputStream stream = new FileInputStream(Objects.requireNonNull(samplesResourceUrl).getFile());
        assertThrows(IllegalArgumentException.class,()->{
            new SamplesDecoder(stream,-1);
        });
    }

    @Test
    void ValuesOfBatch() throws IOException{
        String s = getClass().getResource("/samples.bin").getFile();
        s = URLDecoder.decode(s, UTF_8);
        InputStream stream = new FileInputStream(s);
        SamplesDecoder decoder = new SamplesDecoder(stream,4804/2);
        short[] batch = new short[4804/2];
        decoder.readBatch(batch);
        int [] expected = new int[]{-3,8,-9,-8,-5,-8,-12,-16,-23,-9};
        for (int i = 0; i <10 ; i++) {
            System.out.println(batch[i]);
            assertEquals(expected[i],batch[i]);
        }

    }
}
