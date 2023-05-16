package itmo.app.shared;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;

public class Utils {

    public static ByteBuffer objectToBuffer(Object response) throws IOException {
        try (
            var byteOut = new ByteArrayOutputStream();
            var objectStream = new ObjectOutputStream(byteOut);
        ) {
            objectStream.writeObject(response);
            byte[] objectBytes = byteOut.toByteArray();

            int objectSize = objectBytes.length;
            var buffer = ByteBuffer.allocate(Integer.BYTES + objectSize);
            buffer.putInt(objectSize);
            buffer.put(objectBytes);
            return buffer;
        }
    }
}
