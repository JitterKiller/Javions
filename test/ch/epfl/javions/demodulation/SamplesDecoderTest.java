package ch.epfl.javions.demodulation;

import org.junit.jupiter.api.Test;

import java.io.*;
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

    @Test
    void readBatchWorks() throws IOException {
        File initialFile = new File("resources/samples.bin");
        try {
            short[] tab = new short[4804];
            InputStream stream = new FileInputStream(initialFile);
            SamplesDecoder decode = new SamplesDecoder(stream, 4804);
            decode.readBatch(tab);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testsIfConstructorWorks() throws IOException {
        File initialFile = new File("resources/samples.bin");
        try {
            short[] tab = new short[4804];
            InputStream stream = new FileInputStream(initialFile);
            SamplesDecoder decode = new SamplesDecoder(stream, 4804);
            decode.readBatch(tab);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }


    }
    @Test
    void readBatchTest() throws IOException {
        short[] expected = new short[]{-3, 8 ,-9 ,-8, -5 ,-8, -12, -16, -23 ,-9};
        short[] actual = new short[1200];

        InputStream stream = new FileInputStream("resources/samples.bin");
        SamplesDecoder a = new SamplesDecoder(stream,1200);
        int b = a.readBatch(actual);
        for (int i = 0; i < 10; i++) {
            assertEquals(expected[i],actual[i]);
        }
    }

    @Test
    void SamplesDOTBinDecodeAllValues() throws IOException{
        var samplesResourceUrl = getClass().getResource("/samples.bin");
        InputStream stream = new FileInputStream(Objects.requireNonNull(samplesResourceUrl).getFile());
        var decoder = new SamplesDecoder(stream,2402);
        var samples = new short[2402];
        decoder.readBatch(samples);
        for (short sample : samples) {
            System.out.println(sample);
        }
    }
}
