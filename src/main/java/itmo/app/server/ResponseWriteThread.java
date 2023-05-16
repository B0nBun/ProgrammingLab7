package itmo.app.server;

import itmo.app.shared.ServerResponse;
import itmo.app.shared.Utils;
import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;

public class ResponseWriteThread extends Thread {

    private static ByteBuffer dummyResponseBuffer;

    static {
        try {
            ResponseWriteThread.dummyResponseBuffer =
                Utils.objectToBuffer(
                    new ServerResponse(
                        "Couldn't send a proper response due to an unexpected error"
                    )
                );
        } catch (IOException err) {
            throw new RuntimeException(err);
        }
    }

    Socket client;
    ServerResponse response;

    public ResponseWriteThread(Socket client, ServerResponse response) {
        this.client = client;
        this.response = response;
    }

    @Override
    public void run() {
        ByteBuffer responseBuffer = null;
        try {
            responseBuffer = Utils.objectToBuffer(this.response);
        } catch (IOException err) {
            Server.logger.error(
                "IO Exception in getting buffer from the response: {}",
                err.getMessage()
            );
            Server.logger.error("Sending dummy response buffer");
            responseBuffer = dummyResponseBuffer;
        }
        try {
            client.getOutputStream().write(responseBuffer.array());
        } catch (IOException err) {
            Server.logger.error(
                "IO Exception in sending the output to the client: {}",
                err.getMessage()
            );
        }
        new RequestReadThread(client).start();
    }
}
