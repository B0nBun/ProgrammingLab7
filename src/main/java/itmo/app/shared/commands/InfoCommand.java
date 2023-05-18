package itmo.app.shared.commands;

import itmo.app.server.DataSource;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class InfoCommand implements Command<Serializable, Serializable> {

    @Override
    public Serializable getParamsFromStrings(List<String> stringParams) {
        return Command.dummySerializable;
    }

    @Override
    public Serializable scanAdditionalObject(Scanner scanner, boolean logBack) {
        return Command.dummySerializable;
    }

    @Override
    public void execute(Context<Serializable, Serializable> commandContext)
        throws SQLException {
        commandContext.printer().println(DataSource.Vehicles.info());
    }
}
