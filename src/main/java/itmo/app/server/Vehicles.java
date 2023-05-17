package itmo.app.server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayDeque;
import java.util.Deque;

public class Vehicles {
    static {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException err) {
            Server.logger.error(
                "Couldn't find the class for postgresql driver: {}",
                err.getMessage()
            );
        }
    }

    private static Deque<Vehicles> collection = new ArrayDeque<>();
    private static Connection conn;

    public static void connectToDatabase(String url) throws SQLException {
        Vehicles.conn = DriverManager.getConnection(url);
        if (Vehicles.conn == null) {
            throw new SQLException(
                "Failed to connect to the database, connection is 'null'"
            );
        }
    }
}
