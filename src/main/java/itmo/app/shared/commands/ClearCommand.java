package itmo.app.shared.commands;

import itmo.app.server.DataSource;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class ClearCommand implements Command<Serializable, Serializable> {

    @Override
    public Serializable getParamsFromStrings(List<String> stringParams) {
        return Command.dummySerializable;
    }

    @Override
    public Serializable scanAdditionalObject(Scanner scanner, boolean logback) {
        return Command.dummySerializable;
    }

    @Override
    public void execute(Context<Serializable, Serializable> context) throws SQLException {
        int removed = DataSource.Vehicles.clear(context.request().login());
        context.printer().println("Removed " + removed + " elements");
    }
}
