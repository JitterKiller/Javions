package ch.epfl.javions.demodulation;

import ch.epfl.javions.Preconditions;

import java.io.IOException;
import java.io.InputStream;

public class PowerComputer {
    private final SamplesDecoder decoder;
    private final short[] circularBuffer;
    private int bufferIndex;
    private final int batchSize;

    public PowerComputer(InputStream stream, int batchSize) {
        if (batchSize <= 0 || batchSize % 8 != 0) {
            throw new IllegalArgumentException("Batch size must be a positive multiple of 8.");
        }

        decoder = new SamplesDecoder(stream, batchSize / 2);
        circularBuffer = new short[8];
        bufferIndex = 0;
        this.batchSize = batchSize;
    }

    public int readBatch(int[] sampleBuffer) throws IOException {
        Preconditions.checkArgument(sampleBuffer.length == batchSize);

        int numSamples = decoder.readBatch(convertIntToShortArray(sampleBuffer));

        if (numSamples == -1) {
            return 0;
        }

        // Calculate power samples
        for (int i = 0; i < numSamples; i++) {
            int[] x = new int[8];
            int[] y = new int[8];
            for (int j = 0; j < 8; j++) {
                x[j] = circularBuffer[(bufferIndex + j) % 8] << 16 >> 16;
                y[j] = circularBuffer[(bufferIndex + j) % 8] >> 16;
            }
            double power = Math.pow(x[2] - x[0] - x[1] - x[3], 2) + Math.pow(y[2] - y[0] - y[1] - y[3], 2);
            sampleBuffer[i] = (int) power;
            circularBuffer[bufferIndex] = (short) sampleBuffer[i];
            bufferIndex = (bufferIndex + 1) % 8;
        }

        return numSamples;
    }

    private short[] convertIntToShortArray(int[] arr) {
        short[] result = new short[arr.length];
        for (int i = 0; i < arr.length; i++) {
            result[i] = (short) arr[i];
        }
        return result;
    }
}
