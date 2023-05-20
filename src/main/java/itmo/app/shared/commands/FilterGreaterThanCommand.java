package itmo.app.shared.commands;

import itmo.app.server.DataSource;
import itmo.app.shared.entities.Vehicle;
import java.io.Serializable;
import java.util.List;
import java.util.Scanner;

public class FilterGreaterThanCommand
    implements Command<Serializable, Vehicle.CreationSchema> {

    @Override
    public Serializable getParamsFromStrings(List<String> stringParams) {
        return Command.dummySerializable;
    }

    @Override
    public Vehicle.CreationSchema scanAdditionalObject(Scanner scanner, boolean logback) {
        return Vehicle.CreationSchema.fromScanner(scanner, logback);
    }

    @Override
    public void execute(Context<Serializable, Vehicle.CreationSchema> commandContext) {
        Vehicle.CreationSchema scanned = commandContext.request().additionalObject();
        DataSource.Vehicles
            .stream()
            .filter(v -> v.compareToCreationSchema(scanned) > 0)
            .forEach(vehicle -> {
                commandContext.printer().println(vehicle);
            });
    }
}
