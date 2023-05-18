package itmo.app.shared.commands;

import itmo.app.server.DataSource;
import itmo.app.shared.entities.Vehicle;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class AddCommand implements Command<Serializable, Vehicle.CreationSchema> {

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
        try {
            int id = DataSource.Vehicles.add(
                commandContext.request().login(),
                commandContext.request().additionalObject()
            );
            commandContext.printer().println("Added a vehicle with id = " + id);
        } catch (SQLException err) {
            commandContext
                .printer()
                .println(
                    "Couldn't add vehicle to collection due to the SQL exception: " +
                    err.getMessage()
                );
        }
    }

    @Override
    public String helpMessage() {
        return "add an element to the collection";
    }
}
