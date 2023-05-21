package itmo.app.server;

import io.github.cdimascio.dotenv.Dotenv;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Server {

    public static final Logger logger = LoggerFactory.getLogger("itmo.app.server.logger");

    private static Optional<Dotenv> dotenv;

    static {
        try {
            Server.dotenv = Optional.of(Dotenv.load());
            // There is a DotenvException thrown actually, but for some reason java doesn't see it
            // so I had to use Throwable
        } catch (Throwable err) {
            Server.logger.error("Couldn't load dotenv file: {}", err.getMessage());
            Server.dotenv = Optional.empty();
        }
    }

    public static void main(String[] args) throws IOException {
        Optional<String> psqlUrl = Server.dotenv
            .map(d -> d.get("VEHICLES_DATABASE_URL"));
        if (args.length >= 1) {
            psqlUrl = Optional.of(args[0]);
        }
        if (psqlUrl.isEmpty()) {
            Server.logger.warn(
                "Url to database is unknown. Either set the VEHICLES_DATABASE_URL environment variable or provide the url in the command line arguments"
            );
            return;
        }
        try {
            DataSource.instantiateDatabase(psqlUrl.get());
            Server.logger.info("Connected to the database: {}", psqlUrl);
        } catch (SQLException err) {
            Server.logger.error("Error in database instantiation: {}", err.getMessage());
            return;
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
