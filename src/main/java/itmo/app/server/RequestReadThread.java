package itmo.app.server;

import itmo.app.shared.ClientRequest;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.net.Socket;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

public class RequestReadThread extends Thread {

    Socket client;

    public RequestReadThread(Socket client) {
        this.client = client;
    }

    @Override
    public void run() {
        try {
            ClientRequest<Serializable, Serializable> request = readRequest(
                this.client.getInputStream()
            );
            ExecuteThreadPool.execute(this.client, request);
        } catch (IOException | ClassNotFoundException err) {
            Server.logger.error(
                "Couldn't read the request from the client: {}",
                err.getMessage()
            );
            Server.logger.error(
                "Closing socket '" + this.client.getRemoteSocketAddress() + "'"
            );
            try {
                this.client.close();
            } catch (IOException errr) {
                Server.logger.error("Couldn't close the socket: {}" + errr.getMessage());
            }
        } catch (BufferUnderflowException err) {
            Server.logger.warn(
                "Buffer underflow exception, user most likely disconnected"
            );
        }
    }

    private static ClientRequest<Serializable, Serializable> readRequest(InputStream in)
        throws IOException, ClassNotFoundException {
        byte[] sizeBytes = in.readNBytes(Integer.BYTES);
        int objectSize = ByteBuffer.wrap(sizeBytes).getInt();
        byte[] objectBytes = in.readNBytes(objectSize);
        var bytesInput = new ByteArrayInputStream(objectBytes);
        var objectInput = new ObjectInputStream(bytesInput);
        try {
            @SuppressWarnings({ "unchecked" })
            var message = (ClientRequest<Serializable, Serializable>) objectInput.readObject();
            return message;
        } finally {
            bytesInput.close();
            objectInput.close();
        }
    }
}
