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
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.Optional;
import java.util.stream.Collectors;
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

        private static Collection<Vehicle> collection = Collections.synchronizedCollection(
            new ArrayDeque<Vehicle>()
        );

        public static Stream<Vehicle> stream() {
            return collection.stream();
        }

        public static String info() {
            return (
                "Class: " +
                DataSource.Vehicles.collection.getClass().getName() +
                "\n" +
                "Size: " +
                DataSource.Vehicles.collection.size() +
                "\n"
            );
        }

        public static int add(String login, Vehicle.CreationSchema schema)
            throws SQLException {
            try (
                var stat = DataSource.database.prepareStatement(
                    """
                        with coords_id as (insert into coordinates(x, y) values (?, ?) returning id)
                        insert into vehicles (created_by, name, coordinates_id, engine_power, vehicle_type, fuel_type)
                            values(?, ?, (select id from coords_id), ?, ?::vehicle_type, ?::fuel_type)
                            returning id, creation_date
                        """
                )
            ) {
                stat.setInt(1, schema.coordinates().x());
                stat.setFloat(2, schema.coordinates().y());
                stat.setString(3, login);
                stat.setString(4, schema.name());
                stat.setInt(5, schema.enginePower());
                stat.setString(
                    6,
                    Optional
                        .ofNullable(schema.vehicleType())
                        .map(v -> v.toString().toLowerCase())
                        .orElse(null)
                );
                stat.setString(
                    7,
                    Optional
                        .ofNullable(schema.fuelType())
                        .map(f -> f.toString().toLowerCase())
                        .orElse(null)
                );
                ResultSet res = stat.executeQuery();
                res.next();
                Vehicles.collection.add(
                    new Vehicle(
                        res.getInt("id"),
                        schema.name(),
                        login,
                        schema.coordinates(),
                        res.getDate("creation_date").toLocalDate(),
                        schema.enginePower(),
                        schema.vehicleType(),
                        schema.fuelType()
                    )
                );
                return res.getInt("id");
            }
        }

        public static boolean removeById(String login, int id) throws SQLException {
            try (
                var stat = DataSource.database.prepareStatement(
                    """
                        delete from vehicles where created_by = ? and id = ?
                        """
                )
            ) {
                stat.setString(1, login);
                stat.setInt(2, id);
                int removed = stat.executeUpdate();
                DataSource.Vehicles.collection.removeIf(v -> v.id() == id);
                return removed > 0;
            }
        }

        public static int clear(String login) throws SQLException {
            try (
                var stat = DataSource.database.prepareStatement(
                    """
                        delete from vehicles where created_by = ?
                        """
                );
            ) {
                stat.setString(1, login);
                int removed = stat.executeUpdate();
                DataSource.Vehicles.collection.removeIf(v -> v.createdBy().equals(login));
                return removed;
            }
        }

        public static int removeLower(
            String login,
            Vehicle.CreationSchema creationSchema
        ) throws SQLException {
            try (
                var stat = DataSource.database.prepareStatement(
                    """
                        delete from vehicles where created_by = ? and name < ?
                        """
                )
            ) {
                stat.setString(1, login);
                stat.setString(2, creationSchema.name());
                int removed = stat.executeUpdate();
                DataSource.Vehicles.collection.removeIf(v ->
                    v.name().compareTo(creationSchema.name()) < 0
                );
                return removed;
            }
        }

        public static boolean update(String login, int id, Vehicle.CreationSchema schema)
            throws SQLException {
            try (
                var stat = DataSource.database.prepareStatement(
                    """
                        with coords as (
                            update vehicles set
                                name = ?,
                                engine_power = ?,
                                vehicle_type = ?::vehicle_type,
                                fuel_type = ?::fuel_type
                            where created_by = ? and id = ?
                            returning coordinates_id as id
                        ) update coordinates set
                            x = ?,
                            y = ?
                        where id = (select id from coords)
                        """
                )
            ) {
                stat.setString(1, schema.name());
                stat.setInt(2, schema.enginePower());
                stat.setString(
                    3,
                    Optional
                        .ofNullable(schema.vehicleType())
                        .map(v -> v.toString().toLowerCase())
                        .orElse(null)
                );
                stat.setString(
                    4,
                    Optional
                        .ofNullable(schema.fuelType())
                        .map(v -> v.toString().toLowerCase())
                        .orElse(null)
                );
                stat.setString(5, login);
                stat.setInt(6, id);
                stat.setInt(7, schema.coordinates().x());
                stat.setFloat(8, schema.coordinates().y());
                int changed = stat.executeUpdate();
                var newCollection = DataSource.Vehicles.collection
                    .stream()
                    .map(v -> {
                        if (
                            v.createdBy().equals(login) && v.id() == id
                        ) return Vehicle.fromCreationSchema(
                            v.id(),
                            v.createdBy(),
                            v.creationDate(),
                            schema
                        );
                        return v;
                    })
                    .collect(
                        Collectors.toCollection(() ->
                            Collections.synchronizedCollection(
                                new ArrayDeque<>(DataSource.Vehicles.collection.size())
                            )
                        )
                    );
                synchronized (Vehicles.class) {
                    DataSource.Vehicles.collection = newCollection;
                }
                return changed > 0;
            }
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
                        login varchar(64) primary key not null constraint non_empty_login check (length(login) > 0),
                        password_hashed char(56) not null constraint hash_length_56 check (length(password_hashed) = 56)
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
                        foreign key (coordinates_id) references coordinates (id) on delete restrict on update cascade,
                        foreign key (created_by) references users (login)
                    )
                    """
            );
            stat.addBatch(
                """
                    create or replace function delete_coordinates_after_vehicles() 
                        returns trigger
                        language plpgsql
                    as $$
                    begin
                        delete from coordinates where id = old.coordinates_id;
                        return old;
                    end;
                    $$
                    """
            );
            stat.addBatch(
                """
                create or replace trigger delete_coordinates_with_vehicles
                    after delete on vehicles
                    for each row
                    execute function delete_coordinates_after_vehicles()
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
                "select *, vehicles.id as vehicle_id, coordinates.x, coordinates.y from vehicles inner join coordinates on vehicles.coordinates_id = coordinates.id;"
            );
            while (res.next()) {
                newCollection.add(
                    new Vehicle(
                        res.getInt("vehicle_id"),
                        res.getString("name"),
                        res.getString("created_by"),
                        new Coordinates(res.getInt("x"), res.getFloat("y")),
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
