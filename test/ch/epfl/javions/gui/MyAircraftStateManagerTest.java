package ch.epfl.javions.gui;

import ch.epfl.javions.ByteString;
import ch.epfl.javions.adsb.RawMessage;
import org.junit.jupiter.api.Test;

import java.io.*;

class MyAircraftStateManagerTest {

    @Test
    void testAircraftStateManagerFirstMessages() throws IOException {
        try (DataInputStream s = new DataInputStream(new BufferedInputStream(
                new FileInputStream("/Users/adam/Documents/CS-108/Javions/resources/messages_20230318_0915.bin")))){
            byte[] bytes = new byte[RawMessage.LENGTH];
            for(int i = 0; i < 3; ++i) {
                long timeStampNs = s.readLong();
                int bytesRead = s.readNBytes(bytes, 0, bytes.length);
                assert bytesRead == RawMessage.LENGTH;
                ByteString message = new ByteString(bytes);
                System.out.printf("%13d: %s\n", timeStampNs, message);
            }
        }
    }

}
