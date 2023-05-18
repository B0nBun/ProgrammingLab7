package itmo.app.shared.commands;

import itmo.app.server.DataSource;
import java.io.Serializable;
import java.util.List;
import java.util.Scanner;

public class ShowCommand implements Command<Serializable, Serializable> {

    @Override
    public Serializable getParamsFromStrings(List<String> stringParams) {
        return Command.dummyParams;
    }

    @Override
    public Serializable scanAdditionalObject(Scanner scanner, boolean logback) {
        return Command.dummyAdditionalObject;
    }

    @Override
    public void execute(Context<Serializable, Serializable> commandContext) {
        DataSource.Vehicles
            .stream()
            .forEach(vehicle -> {
                commandContext.printer().println(vehicle);
            });
    }

    @Override
    public String helpMessage() {
        return "prints out the collection";
    }
}
