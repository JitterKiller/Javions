package ch.epfl.javions.demodulation;

import ch.epfl.javions.Preconditions;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public final class SamplesDecoder {

    private InputStream stream;
    private final int batchSize;
    private byte[] bytesBuffer;

    public SamplesDecoder(InputStream stream, int batchSize){
        Preconditions.checkArgument(batchSize > 0);
        Objects.requireNonNull(stream);
        this.stream = stream;
        this.batchSize = batchSize;
        bytesBuffer = new byte[batchSize * 2];
    }

    public int readBatch(short[] batch) throws IOException {
        Preconditions.checkArgument(batch.length == batchSize);
        int bytesToRead = batchSize * 2;
        int bytesRead = stream.readNBytes(bytesBuffer, 0, bytesToRead);
        if( bytesRead == -1) {
            return 0;
        }
        int samplesToConvert = bytesToRead / 2;
        for(int i = 0; i < samplesToConvert; ++i) {
            int byteIndex = i * 2;
            int sample = (Byte.toUnsignedInt(bytesBuffer[byteIndex]) << 8) | Byte.toUnsignedInt(bytesBuffer[byteIndex + 1]);
            batch[i] = (short) (sample - 2048);
        }
        return samplesToConvert;
    }

}
