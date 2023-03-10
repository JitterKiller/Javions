package ch.epfl.javions.demodulation;

import ch.epfl.javions.ByteString;
import ch.epfl.javions.adsb.RawMessage;

import java.io.IOException;
import java.io.InputStream;

public final class AdsbDemodulator {

    private final PowerWindow window;
    private int sumCarrierPeak = 0;
    private int previousSumCarrierPeak = 0;
    private int nextSumCarrierPeak = 0;
    private int sumCarrierBottomOut = 0;

    public AdsbDemodulator(InputStream stream) throws IOException {
        window = new PowerWindow(stream,1200);
    }

    public RawMessage nextMessage() throws IOException {

//        for(window.size();; window.advance()) {
//            if(isPeakDetected(i)) {
//
//            }
//        }

        return new RawMessage(window.position(), new ByteString(new byte[0]));
    }

    private boolean isPeakDetected(int i) {
        computeSums(i);
        return (sumCarrierPeak > previousSumCarrierPeak) && (sumCarrierPeak > nextSumCarrierPeak);
    }

    private void computeSums(int i) {
        sumCarrierPeak = window.get(i) + window.get(i + 10) + window.get(i + 35) + window.get(i + 45);
        previousSumCarrierPeak = window.get(i - 1) + window.get(i + 9) + window.get(i + 34) + window.get(i + 44);
        sumCarrierPeak = window.get(i +1) + window.get(i + 11) + window.get(i + 36) + window.get(i + 46);
    }

}
