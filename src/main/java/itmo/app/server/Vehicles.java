package itmo.app.server;

import itmo.app.shared.entities.Coordinates;
import itmo.app.shared.entities.FuelType;
import itmo.app.shared.entities.Vehicle;
import itmo.app.shared.entities.VehicleType;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.stream.Stream;

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

    private static Deque<Vehicle> collection = new ArrayDeque<>();
    private static Connection database;

    public static Stream<Vehicle> stream() {
        return Vehicles.collection.stream();
    }

    public static void instantiateDatabase(String url) throws SQLException {
        Vehicles.database = DriverManager.getConnection(url);
        if (Vehicles.database == null) {
            throw new SQLException(
                "Failed to connect to the database, connection is 'null'"
            );
        }
        Vehicles.createTables();
        Vehicles.updateInMemoryCollection();
    }

    private static void updateInMemoryCollection() throws SQLException {
        try (var stat = Vehicles.database.createStatement();) {
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
            Vehicles.collection = newCollection;
        }
    }

    private static void createTables() throws SQLException {
        try (var stat = Vehicles.database.createStatement();) {
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
                        name varchar(64) not null,
                        coordinates_id integer not null,
                        creation_date date not null default CURRENT_DATE,
                        engine_power integer null check (engine_power > 0),
                        vehicle_type vehicle_type null,
                        fuel_type fuel_type null,
                        foreign key (coordinates_id) references coordinates (id)
                    )
                    """
            );
            stat.executeBatch();
            Server.logger.info("All database tables were created");
        }
    }
}
