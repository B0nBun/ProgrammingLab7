package itmo.app.server;

import io.github.cdimascio.dotenv.Dotenv;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Server {

    public static final Logger logger = LoggerFactory.getLogger(
        "ru.ifmo.app.server.logger"
    );

    private static Dotenv dotenv;

    static {
        Server.dotenv = Dotenv.configure().load();
    }

    public static void main(String[] args) throws IOException {
        String psqlUrl = Server.dotenv.get("VEHICLES_DATABASE_URL");
        if (args.length >= 1) {
            psqlUrl = args[0];
        }
        if (psqlUrl == null) {
            Server.logger.warn(
                "Url to database is unknown. Either set the VEHICLES_DATABASE_URL environment variable or provide the url in the command line arguments"
            );
        }
        try {
            Vehicles.instantiateDatabase(psqlUrl);
            Server.logger.info("Connected to the database: {}", psqlUrl);
        } catch (SQLException err) {
            Server.logger.error("Error in database instantiation: {}", err.getMessage());
            return;
        }

        for (var element : Vehicles.stream().toList()) {
            Server.logger.info("Loaded a vehicle: {}", element);
        }

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
