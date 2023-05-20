package itmo.app.shared.commands;

import itmo.app.server.DataSource;
import itmo.app.shared.entities.FuelType;
import itmo.app.shared.exceptions.InvalidParamsException;
import itmo.app.shared.exceptions.ParsingException;
import itmo.app.shared.fieldschema.FieldSchema;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class CountGreaterThanFuelTypeCommand implements Command<FuelType, Serializable> {

    @Override
    public FuelType getParamsFromStrings(List<String> stringParams)
        throws InvalidParamsException {
        if (stringParams.size() < 1) throw new InvalidParamsException(
            "fuel type in params expected"
        );
        try {
            return FieldSchema
                .enumeration(FuelType.class)
                .nonnull()
                .parse(stringParams.get(0));
        } catch (ParsingException err) {
            throw new InvalidParamsException(
                "fuel type in params expected: " + err.getMessage()
            );
        }
    }

    @Override
    public Serializable scanAdditionalObject(Scanner scanner, boolean logBack) {
        return Command.dummySerializable;
    }

    @Override
    public void execute(Context<FuelType, Serializable> commandContext)
        throws SQLException {
        FuelType type = commandContext.request().commandParameters();
        var count = DataSource.Vehicles
            .stream()
            .filter(v -> v.fuelType().compareTo(type) > 0)
            .count();
        commandContext
            .printer()
            .println("Vehicles with fuel type greater than '" + type + "': " + count);
    }
}
