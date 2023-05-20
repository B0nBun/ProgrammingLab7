package itmo.app.shared.commands;

import itmo.app.server.DataSource;
import itmo.app.shared.entities.Vehicle;
import itmo.app.shared.entities.Vehicle.CreationSchema;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class AddIfMaxCommand implements Command<Serializable, Vehicle.CreationSchema> {

    @Override
    public Serializable getParamsFromStrings(List<String> stringParams) {
        return Command.dummySerializable;
    }

    @Override
    public Vehicle.CreationSchema scanAdditionalObject(Scanner scanner, boolean logback) {
        return Vehicle.CreationSchema.fromScanner(scanner, logback);
    }

    @Override
    public void execute(Context<Serializable, CreationSchema> commandContext)
        throws SQLException {
        Vehicle.CreationSchema scanned = commandContext.request().additionalObject();
        Optional<Vehicle> max = DataSource.Vehicles.stream().max(Vehicle::compareTo);
        boolean shouldAdd = max
            .map(m -> m.compareToCreationSchema(scanned) < 0)
            .orElse(true);
        if (shouldAdd) {
            int newid = DataSource.Vehicles.add(
                commandContext.request().login(),
                scanned
            );
            commandContext.printer().println("Added a vehicle with id = " + newid);
        } else {
            commandContext
                .printer()
                .println(
                    "Provided vehicle is not going to be the max, so it was not added"
                );
        }
    }
}
