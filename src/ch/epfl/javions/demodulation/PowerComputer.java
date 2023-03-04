package ch.epfl.javions.demodulation;

import ch.epfl.javions.Preconditions;

import java.io.IOException;
import java.io.InputStream;

public final class PowerComputer {

    private InputStream stream;
    private final int batchSize;
    private byte[] bytesBuffer;
    public PowerComputer(InputStream stream, int batchSize){
        Preconditions.checkArgument(batchSize % 8 == 0 && batchSize > 0);

        this.stream = stream;
        this.batchSize = batchSize;
        bytesBuffer = new byte[batchSize * 2];

    }

    public int readBatch(int[] batch) throws IOException {
        Preconditions.checkArgument(batch.length == batchSize);

    }
}
