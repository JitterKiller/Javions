package ch.epfl.javions.demodulation;

import ch.epfl.javions.adsb.RawMessage;

import java.io.IOException;
import java.io.InputStream;

public final class AdsbDemodulator {
    private final PowerWindow window;
    private final byte[] message = new byte[14];

    public AdsbDemodulator(InputStream stream) throws IOException {
        window = new PowerWindow(stream, 1200);
    }

    public RawMessage nextMessage() throws IOException {

        int sumCarrierPeak;
        int previousSumCarrierPeak = 0;

        for (; window.isFull(); window.advance()) {
            sumCarrierPeak = computeCarrierSum();
            if (isPeakDetected(sumCarrierPeak,previousSumCarrierPeak)) {
                if (computeCarrierSum() >= computeTwiceBottomOutSums()) {
                    if (RawMessage.size(computeFirstByte()) == RawMessage.LENGTH) {
                        for(int i = 1; i < message.length; ++i) {
                            byte b = 0;
                            int index = i * Byte.SIZE;
                            for (int j = 0; j < Byte.SIZE; ++j) {
                                if (decodeBits(index)) {
                                    b |= (1 << (7 - j));;
                                }
                                ++index;
                            }
                            message[i] = b;
                        }
                        RawMessage rawMessage = RawMessage.of((window.position() * 100), message);
                        if(rawMessage != null) {
                            window.advanceBy(1200);
                            return rawMessage;
                        }
                    }
                }
            }
            previousSumCarrierPeak = sumCarrierPeak;
        }
        return null;
    }

    private boolean isPeakDetected(int sumCarrierPeak, int previousSumCarrierPeak) {
        int nextSumCarrierPeak = window.get(1) + window.get(11) + window.get(36) + window.get(46);
        return (sumCarrierPeak > previousSumCarrierPeak) && (sumCarrierPeak > nextSumCarrierPeak);
    }

    private int computeCarrierSum() {
        return (window.get(0) + window.get(10) + window.get(35) + window.get(45));
    }

    private int computeTwiceBottomOutSums() {
        return 2 * (window.get(5) + window.get(15) + window.get(20) + window.get(30) + window.get(40));
    }

    private boolean decodeBits(int i) {
        return window.get(80 + (10 * i)) >= window.get(85 + (10 * i));
    }
    private byte computeFirstByte() {
        byte byte0 = 0;
        for (int i = 0; i < Byte.SIZE; ++i) {
            if(decodeBits(i)) {
                byte0 |= (1 << (7 - i));
            }
        }
        message[0] = byte0;
        return byte0;
    }
}
