package ch.epfl.javions.demodulation;

import ch.epfl.javions.Preconditions;

import java.io.IOException;
import java.io.InputStream;

public final class PowerComputer {

    private final SamplesDecoder decoder;
    private final short[] circularBuffer;
    private final short[] powerBuffer;
    private final int batchSize;

    public PowerComputer(InputStream stream, int batchSize) {

        Preconditions.checkArgument(batchSize > 0 && batchSize % 8 == 0);

        this.batchSize = batchSize;
        decoder = new SamplesDecoder(stream, batchSize);
        circularBuffer = new short[8];
        powerBuffer = new short[batchSize];
    }

    public int readBatch(int[] batch) throws IOException {
        Preconditions.checkArgument(batch.length == batchSize);

        int samplesRead = decoder.readBatch(powerBuffer);

        int bufferIndex = 0;
        for (int i = 0; i < samplesRead; i+=2) {
            circularBuffer[bufferIndex % 8] = powerBuffer[i];
            circularBuffer[(bufferIndex+1) % 8] = powerBuffer[i+1];
            bufferIndex +=2;
            double power = Math.pow(circularBuffer[0] - circularBuffer [2] + circularBuffer [4] - circularBuffer [6], 2) + Math.pow(circularBuffer[1] - circularBuffer [3] + circularBuffer [5] - circularBuffer [7], 2);
            batch[i/2] = (int) power;
        }

        return samplesRead / 2;
    }
}