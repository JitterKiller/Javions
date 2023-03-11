package ch.epfl.javions.demodulation;

import ch.epfl.javions.ByteString;
import ch.epfl.javions.adsb.RawMessage;

import java.io.IOException;
import java.io.InputStream;


public final class AdsbDemodulator {
    private final PowerWindow window;
    private int sumCarrierPeak;
    private int previousSumCarrierPeak;
    private int nextSumCarrierPeak;
    private int sumCarrierBottomOut;
    public AdsbDemodulator(InputStream stream) throws IOException {
        window = new PowerWindow(stream,1200);
    }

    public RawMessage nextMessage() throws IOException {


        sumCarrierPeak = 0;

        for(;window.isFull(); window.advance()){
            previousSumCarrierPeak = sumCarrierPeak;
            computeCarrierSum();
            if(isPeakDetected()) {
                computeBottomOutSums();
                if(sumCarrierPeak >= 2 * sumCarrierBottomOut) {
                    byte[] message = new byte[14];
                    for(int i = 0; i < message.length; ++i) {
                        byte b = 0;
                        for(int j = 0; j < Byte.SIZE; ++j){
                            int index = i * 8 + j;
                            if (decodeBits(index)) {
                                b |= (1 << (7 - j));
                            }
                        }
                        message[i] = b;
                    }
                    if(RawMessage.size(message[0]) == RawMessage.LENGTH) {
                        if(RawMessage.of((window.position() - 1L) * 100, message) != null) {
                            ByteString byteString = new ByteString(message);
                            window.advanceBy(1199);
                            return new RawMessage((window.position() - 1L) * 100, byteString);
                        }
                    }
                }
            }
        }
        return null;
    }

    private boolean isPeakDetected(){
        return (sumCarrierPeak > previousSumCarrierPeak) && (sumCarrierPeak > nextSumCarrierPeak);
    }

    private void computeCarrierSum() {
        sumCarrierPeak = window.get(0) + window.get(10) + window.get(35) + window.get(45);
        nextSumCarrierPeak = window.get(1) + window.get(11) + window.get(36) + window.get(46);
    }

    private void computeBottomOutSums() {
        sumCarrierBottomOut = window.get(5) + window.get(15) + window.get(20) + window.get(30) + window.get(40);
    }

    private boolean decodeBits(int i) {
        return window.get(80 + (10 * i)) >= window.get(85 + (10 * i));
    }

}
