package itmo.app.shared.commands;

import itmo.app.server.DataSource;
import itmo.app.shared.entities.Vehicle;
import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class HeadCommand implements Command<Serializable, Serializable> {

    @Override
    public Serializable getParamsFromStrings(List<String> stringParams) {
        return Command.dummySerializable;
    }

    @Override
    public Serializable scanAdditionalObject(Scanner scanner, boolean logBack) {
        return Command.dummySerializable;
    }

    @Override
    public void execute(Context<Serializable, Serializable> commandContext) {
        Optional<Vehicle> head = DataSource.Vehicles.stream().findFirst();
        if (head.isEmpty()) {
            commandContext.printer().println("Collection is empty");
        } else {
            commandContext.printer().println(head.get().toString());
        }
    }
}
