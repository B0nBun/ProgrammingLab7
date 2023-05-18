package itmo.app.shared.commands;

import java.io.PrintWriter;
import java.io.Serializable;
import java.util.List;
import java.util.Scanner;

public class HelpCommand implements Command<Serializable, Serializable> {

    @Override
    public Serializable getParamsFromStrings(List<String> stringParams) {
        return Command.dummySerializable;
    }

    @Override
    public Serializable scanAdditionalObject(Scanner scanner, boolean logback) {
        return Command.dummySerializable;
    }

    @Override
    public void execute(Context<Serializable, Serializable> commandContext) {
        PrintWriter printer = commandContext.printer();
        CommandRegistery.global
            .getAllCommands()
            .forEach(command -> {
                printer.println("- " + String.join(", ", command.getKey()));
                printer.println(
                    "  parameters: [" +
                    String.join(", ", command.getValue().helpParams()) +
                    "]"
                );
                printer.println("    " + command.getValue().helpMessage());
            });
    }

    @Override
    public String helpMessage() {
        return "prints out the list of commands with their description";
    }
}
