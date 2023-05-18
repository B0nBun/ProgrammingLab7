package itmo.app.shared.commands;

import itmo.app.server.DataSource;
import itmo.app.shared.exceptions.InvalidParamsException;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class RemoveByIdCommand
    implements Command<RemoveByIdCommand.Params, Serializable> {

    public static class Params implements Serializable {

        int id;

        public Params(int id) {
            this.id = id;
        }
    }

    @Override
    public Params getParamsFromStrings(List<String> stringParams)
        throws InvalidParamsException {
        if (stringParams.size() == 0) throw new InvalidParamsException(
            "Expected an integer id"
        );
        try {
            int id = Integer.parseInt(stringParams.get(0));
            return new Params(id);
        } catch (NumberFormatException err) {
            throw new InvalidParamsException("Expected an integer id");
        }
    }

    @Override
    public Serializable scanAdditionalObject(Scanner scanner, boolean logBack) {
        return Command.dummySerializable;
    }

    @Override
    public void execute(Context<Params, Serializable> commandContext)
        throws SQLException {
        int id = commandContext.request().commandParameters().id;
        boolean removed = DataSource.Vehicles.removeById(
            commandContext.request().login(),
            id
        );
        if (removed) {
            commandContext.printer().println("Removed");
        } else {
            commandContext
                .printer()
                .println("Couldn't remove the vehicle with id = " + id);
            commandContext
                .printer()
                .println(
                    "You are either unauthorized to do so or the vehicle doesn't exist"
                );
        }
    }
}
