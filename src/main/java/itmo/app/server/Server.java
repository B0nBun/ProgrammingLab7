package itmo.app.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Server {

    public static final Logger logger = LoggerFactory.getLogger(
        "ru.ifmo.app.server.logger"
    );

    public static void main(String[] args) throws IOException {
        @SuppressWarnings({ "resource" })
        var server = new ServerSocket(1111);

        Server.logger.info("Server started at port: " + server.getLocalPort());
        while (true) {
            final Socket client;
            try {
                client = server.accept();
                Server.logger.info(
                    "Client connected: " + client.getRemoteSocketAddress()
                );
            } catch (IOException err) {
                Server.logger.error(
                    "Couldn't connect the client due to the IO Exception: " +
                    err.getMessage()
                );
                continue;
            }
            new RequestReadThread(client).start();
        }
    }
}
