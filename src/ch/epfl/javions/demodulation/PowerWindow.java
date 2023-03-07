package ch.epfl.javions.demodulation;

import ch.epfl.javions.Preconditions;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public final class PowerWindow {
    private final PowerComputer powerComputer;
    private final int[] powerSamples;
    private final int windowSize;
    private int currentSampleIndex;
    private long position;

    public PowerWindow(InputStream stream, int windowSize) throws IOException {
        Preconditions.checkArgument(windowSize > 0 && windowSize <= 65536);

        powerComputer = new PowerComputer(stream, windowSize);
        powerSamples = new int[windowSize];
        this.windowSize = windowSize;
        currentSampleIndex = 0;
        position = 0;
    }

    public int size() {
        return windowSize;
    }

    public long position() {
        return position;
    }

    public boolean isFull() {
        return currentSampleIndex == windowSize;
    }

    public int get(int i) {
        Objects.checkIndex(i,windowSize);
        return powerSamples[i];
    }

    public void advance() throws IOException {
        if (currentSampleIndex == windowSize) {
            for (int i = 0; i < windowSize - 1; i++) {
                powerSamples[i] = powerSamples[i + 1];
            }
            powerSamples[windowSize - 1] = powerComputer.readBatch(new int[windowSize])[0];
            position++;
        } else {
            powerSamples[currentSampleIndex] = powerComputer.readBatch(new int[1])[0];
            currentSampleIndex++;
            position++;
        }
    }

    public void advanceBy(int offset) throws IOException {
        Preconditions.checkArgument(offset >= 0);
        for (int i = 0; i < offset; i++) {
            advance();
        }
    }
}
