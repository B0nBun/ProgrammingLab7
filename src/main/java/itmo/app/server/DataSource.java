package itmo.app.server;

import itmo.app.shared.entities.Coordinates;
import itmo.app.shared.entities.FuelType;
import itmo.app.shared.entities.Vehicle;
import itmo.app.shared.entities.VehicleType;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.stream.Stream;

public class DataSource {
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

    private static Connection database;

    public static class Vehicles {

        private static Deque<Vehicle> collection = new ArrayDeque<>();

        public static Stream<Vehicle> stream() {
            return collection.stream();
        }
    }

    public static class Auth {

        public static boolean userExists(String login) throws SQLException {
            try (
                var stat = DataSource.database.prepareStatement(
                    "select exists(select 1 from users where login = ?)"
                );
            ) {
                stat.setString(1, login);
                ResultSet res = stat.executeQuery();
                res.next();
                return res.getBoolean("exists");
            }
        }

        public static enum AuthResult {
            LOGGEDIN,
            REGISTERED,
            REJECTED;

            public boolean authorized() {
                return (
                    this.equals(AuthResult.LOGGEDIN) || this.equals(AuthResult.REGISTERED)
                );
            }
        }

        public static AuthResult userAuthorized(String login, String password)
            throws SQLException {
            String passwordHashed = hashPassword(password);
            boolean exists = userExists(login);
            if (exists) {
                try (
                    var stat = DataSource.database.prepareStatement(
                        """
                        select exists(select 1 from users where login = ? and password_hashed = ?)
                        """
                    );
                ) {
                    stat.setString(1, login);
                    stat.setString(2, passwordHashed);
                    ResultSet res = stat.executeQuery();
                    res.next();
                    if (res.getBoolean("exists")) {
                        return AuthResult.LOGGEDIN;
                    }
                    return AuthResult.REJECTED;
                }
            }
            try (
                var stat = DataSource.database.prepareStatement(
                    """
                        insert into users(login, password_hashed) values (?, ?)
                        """
                );
            ) {
                stat.setString(1, login);
                stat.setString(2, passwordHashed);
                stat.executeUpdate();
                return AuthResult.REGISTERED;
            }
        }

        private static String hashPassword(String password) {
            try {
                MessageDigest md = MessageDigest.getInstance("SHA-224");
                var no = new BigInteger(1, md.digest(password.getBytes()));
                String hash = no.toString(16);
                while (hash.length() < 56) {
                    hash = "0" + hash;
                }
                return hash;
            } catch (NoSuchAlgorithmException err) {
                throw new RuntimeException(err.getMessage());
            }
        }
    }

    public static void instantiateDatabase(String url) throws SQLException {
        DataSource.database = DriverManager.getConnection(url);
        if (DataSource.database == null) {
            throw new SQLException(
                "Failed to connect to the database, connection is 'null'"
            );
        }
        DataSource.createTables();
        DataSource.updateInMemoryCollection();
    }

    private static void createTables() throws SQLException {
        try (var stat = DataSource.database.createStatement();) {
            stat.addBatch(
                """
                    do $$ 
                    begin
                        if not exists (select 1 from pg_type where typname = 'vehicle_type') then
                            create type vehicle_type as enum (
                                'drone',
                                'ship',
                                'bicycle'
                            );
                        end if;
                        if not exists (select 1 from pg_type where typname = 'fuel_type') then
                            create type fuel_type as enum (
                                'gasoline',
                                'electricity',
                                'manpower',
                                'plasma',
                                'antimatter'
                            );
                        end if;
                    end$$
                    """
            );
            stat.addBatch(
                """
                    create table if not exists users (
                        login varchar(64) primary key not null,
                        password_hashed varchar(56) not null
                    )
                    """
            );
            stat.addBatch(
                """
                    create table if not exists coordinates (
                        id integer primary key generated always as identity,
                        x integer not null,
                        y float8 not null check (y > -738)
                    )
                    """
            );
            stat.addBatch(
                """
                    create table if not exists vehicles (
                        id integer primary key generated always as identity,
                        created_by varchar(64) not null,
                        name varchar(64) not null,
                        coordinates_id integer not null,
                        creation_date date not null default CURRENT_DATE,
                        engine_power integer null check (engine_power > 0),
                        vehicle_type vehicle_type null,
                        fuel_type fuel_type null,
                        foreign key (coordinates_id) references coordinates (id),
                        foreign key (created_by) references users (login)
                    )
                    """
            );
            stat.executeBatch();
            Server.logger.info("All database tables were created");
        }
    }

    private static void updateInMemoryCollection() throws SQLException {
        try (var stat = DataSource.database.createStatement();) {
            Deque<Vehicle> newCollection = new ArrayDeque<>();
            ResultSet res = stat.executeQuery(
                "select vehicles.id, name, creation_date, engine_power, vehicle_type, fuel_type, coordinates.x, coordinates.y from vehicles inner join coordinates on vehicles.coordinates_id = coordinates.id;"
            );
            while (res.next()) {
                newCollection.add(
                    new Vehicle(
                        res.getInt("id"),
                        res.getString("name"),
                        new Coordinates(res.getLong("x"), res.getInt("y")),
                        res.getDate("creation_date").toLocalDate(),
                        res.getInt("engine_power"),
                        VehicleType.fromString(res.getString("vehicle_type")),
                        FuelType.fromString(res.getString("fuel_type"))
                    )
                );
            }
            DataSource.Vehicles.collection = newCollection;
        }
    }
}
