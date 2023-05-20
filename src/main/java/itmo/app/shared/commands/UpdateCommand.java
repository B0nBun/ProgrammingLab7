package itmo.app.shared.commands;

import itmo.app.server.DataSource;
import itmo.app.shared.entities.Vehicle;
import itmo.app.shared.entities.Vehicle.CreationSchema;
import itmo.app.shared.exceptions.InvalidParamsException;
import itmo.app.shared.exceptions.ParsingException;
import itmo.app.shared.fieldschema.FieldSchema;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class UpdateCommand implements Command<Integer, Vehicle.CreationSchema> {

    @Override
    public Integer getParamsFromStrings(List<String> stringParams)
        throws InvalidParamsException {
        if (stringParams.size() < 1) {
            throw new InvalidParamsException("Expected integer id in paramters");
        }
        try {
            return FieldSchema.integer().nonnull().parse(stringParams.get(0));
        } catch (ParsingException err) {
            throw new InvalidParamsException("Expected integer id: " + err.getMessage());
        }
    }

    @Override
    public Vehicle.CreationSchema scanAdditionalObject(Scanner scanner, boolean logback) {
        return Vehicle.CreationSchema.fromScanner(scanner, logback);
    }

    @Override
    public void execute(Context<Integer, CreationSchema> commandContext)
        throws SQLException {
        int id = commandContext.request().commandParameters();
        Vehicle.CreationSchema scanned = commandContext.request().additionalObject();
        boolean changed = DataSource.Vehicles.update(
            commandContext.request().login(),
            id,
            scanned
        );
        if (changed) {
            commandContext.printer().println("Vehicle updated");
        } else {
            commandContext
                .printer()
                .println(
                    "You are unauthorized to update this vehicle or such vehicle doesn't exist"
                );
        }
    }
}
