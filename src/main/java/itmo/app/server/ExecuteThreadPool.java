package itmo.app.server;

import itmo.app.server.DataSource.Auth.AuthResult;
import itmo.app.shared.ClientRequest;
import itmo.app.shared.ServerResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.Socket;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExecuteThreadPool {

    private static ExecutorService executorService = Executors.newFixedThreadPool(10);

    public static void execute(Socket client, ClientRequest request) {
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
                Server.logger.info(
                    "Server is running command '" + request.commandName() + "'"
                );
                printer.println(
                    "Placeholder for the result of the '" +
                    request.commandName() +
                    "' command"
                );
                var response = new ServerResponse(stringWriter.toString());
                new ResponseWriteThread(client, response).start();
            } catch (IOException err) {
                new ResponseWriteThread(
                    client,
                    new ServerResponse(
                        "Couldn't send the response due to IO exception: " +
                        err.getMessage()
                    )
                )
                    .start();
            } catch (SQLException err) {
                new ResponseWriteThread(
                    client,
                    new ServerResponse(
                        "Couldn't send the response due to SQL exception: " +
                        err.getMessage()
                    )
                )
                    .start();
            }
        });
    }
}
