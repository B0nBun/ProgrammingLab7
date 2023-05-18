package itmo.app.server;

import itmo.app.server.DataSource.Auth.AuthResult;
import itmo.app.shared.ClientRequest;
import itmo.app.shared.ServerResponse;
import itmo.app.shared.commands.Command;
import itmo.app.shared.commands.CommandRegistery;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.net.Socket;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExecuteThreadPool {

    private static ExecutorService executorService = Executors.newFixedThreadPool(10);

    public static void execute(
        Socket client,
        ClientRequest<Serializable, Serializable> request
    ) {
        executorService.execute(() -> {
            try (
                var stringWriter = new StringWriter();
                var printer = new PrintWriter(stringWriter);
            ) {
                AuthResult authRes = DataSource.Auth.userAuthorized(
                    request.login(),
                    request.password()
                );
                if (!authRes.authorized()) {
                    var response = new ServerResponse(
                        "Authorization error. Invalid password"
                    );
                    new ResponseWriteThread(client, response).start();
                    return;
                }
                if (authRes.equals(AuthResult.REGISTERED)) {
                    printer.println(
                        "User with login '" + request.login() + "' registered"
                    );
                }
                runCommandFromRequest(request, printer);
                var response = new ServerResponse(stringWriter.toString());
                new ResponseWriteThread(client, response).start();
            } catch (IOException | SQLException err) {
                String exception = err.getClass() == IOException.class
                    ? "IO exception"
                    : "SQL exception";
                var response = new ServerResponse(
                    "Couldn't send the response due to" +
                    exception +
                    ": " +
                    err.getMessage()
                );
                new ResponseWriteThread(client, response).start();
            }
        });
    }

    private static void runCommandFromRequest(
        ClientRequest<Serializable, Serializable> request,
        PrintWriter printer
    ) {
        Server.logger.info("Server is running command '" + request.commandName() + "'");
        Command<Serializable, Serializable> command = CommandRegistery.global.get(
            request.commandName()
        );
        if (command == null) {
            printer.println("Couldn't find command '" + request.commandName() + "'");
            return;
        }
        try {
            command.execute(new Command.Context<>(request, printer));
        } catch (SQLException err) {
            printer.println(
                "Couldn't execute the command due to the SQL exception: " +
                err.getMessage()
            );
        }
    }
}
