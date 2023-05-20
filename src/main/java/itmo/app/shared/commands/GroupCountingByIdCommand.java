package itmo.app.shared.commands;

import itmo.app.server.DataSource;
import itmo.app.shared.exceptions.InvalidParamsException;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class GroupCountingByIdCommand implements Command<Serializable, Serializable> {

    @Override
    public Serializable getParamsFromStrings(List<String> stringParams)
        throws InvalidParamsException {
        return Command.dummySerializable;
    }

    @Override
    public Serializable scanAdditionalObject(Scanner scanner, boolean logBack) {
        return Command.dummySerializable;
    }

    @Override
    public void execute(Context<Serializable, Serializable> commandContext)
        throws SQLException {
        var map = new HashMap<Integer, Integer>();
        DataSource.Vehicles
            .stream()
            .forEach(v -> {
                var currentCount = map.getOrDefault(v.id(), 0);
                map.put(v.id(), currentCount + 1);
            });
        map
            .entrySet()
            .forEach(entry -> {
                commandContext
                    .printer()
                    .println(entry.getKey() + " -> " + entry.getValue());
            });
    }
}
