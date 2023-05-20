package itmo.app.shared.commands;

import itmo.app.server.DataSource;
import itmo.app.shared.entities.Vehicle;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class RemoveLowerCommand implements Command<Serializable, Vehicle.CreationSchema> {

    @Override
    public Serializable getParamsFromStrings(List<String> stringParams) {
        return Command.dummySerializable;
    }

    @Override
    public Vehicle.CreationSchema scanAdditionalObject(Scanner scanner, boolean logback) {
        return Vehicle.CreationSchema.fromScanner(scanner, logback);
    }

    @Override
    public void execute(Context<Serializable, Vehicle.CreationSchema> commandContext)
        throws SQLException {
        Vehicle.CreationSchema scanned = commandContext.request().additionalObject();
        int removed = DataSource.Vehicles.removeLower(
            commandContext.request().login(),
            scanned
        );
        commandContext.printer().println("Removed " + removed + " elements");
    }
}
