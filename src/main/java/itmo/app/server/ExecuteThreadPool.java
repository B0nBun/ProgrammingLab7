package itmo.app.server;

import itmo.app.shared.ClientRequest;
import itmo.app.shared.ServerResponse;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExecuteThreadPool {

    private static ExecutorService executorService = Executors.newFixedThreadPool(10);

    public static void execute(Socket client, ClientRequest request) {
        executorService.execute(() -> {
            Server.logger.info(
                "Server is running command '" + request.commandName() + "'"
            );
            try {
                Thread.sleep(500);
            } catch (InterruptedException err) {}
            var response = new ServerResponse(
                "Output in response to command: " + request.commandName()
            );
            new ResponseWriteThread(client, response).start();
        });
    }
}
