package ch.epfl.javions.demodulation;

import ch.epfl.javions.Preconditions;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public final class SamplesDecoder {

    private final InputStream stream;
    private final int batchSize;
    private final byte[] bytesBuffer;

    public SamplesDecoder(InputStream stream, int batchSize){
        Preconditions.checkArgument(batchSize > 0);
        Objects.requireNonNull(stream);
        this.stream = stream;
        this.batchSize = batchSize;
        bytesBuffer = new byte[batchSize * 2];
    }

    public int readBatch(short[] batch) throws IOException {
        Preconditions.checkArgument(batch.length == batchSize);
        int bytesRead = stream.readNBytes(bytesBuffer, 0, batchSize * 2);
        int samplesToConvert;
        if( bytesRead < batchSize * 2) {
            samplesToConvert = bytesRead / 2;
        } else {
            samplesToConvert = batchSize;
        }

        for(int i = 0; i < samplesToConvert; ++i) {
            int byteIndex = i * 2;
            int sample = (Byte.toUnsignedInt(bytesBuffer[byteIndex+1]) << 8) | (Byte.toUnsignedInt(bytesBuffer[byteIndex]));
            batch[i] = (short) (sample - 2048);
        }
        return samplesToConvert;
    }

}
