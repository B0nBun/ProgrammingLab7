package itmo.app.client;

import itmo.app.shared.ClientRequest;
import itmo.app.shared.ServerResponse;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.BufferUnderflowException;
import java.nio.channels.SocketChannel;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.Stack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Client {

    public static final Logger logger = LoggerFactory.getLogger(
        "ru.ifmo.app.client.logger"
    );

    public static void main(String[] args) throws ClassNotFoundException {
        Client.logger.info("Login:");
        String login = System.console().readLine();
        Client.logger.info("Password:");
        String password = new String(System.console().readPassword());

        var serverAddress = new InetSocketAddress("127.0.0.1", 1111);

        BlockingChannelWrapper channel;
        try {
            SocketChannel client = SocketChannel.open(serverAddress);
            client.configureBlocking(false);
            channel = new BlockingChannelWrapper(client);
        } catch (IOException err) {
            Client.logger.error("Couldn't open the connection: {}", err.getMessage());
            return;
        }

        var inputScanner = new Scanner(System.in);
        var scriptScanners = new Stack<Scanner>();

        while (true) {
            Scanner currentScanner = scriptScanners.empty()
                ? inputScanner
                : scriptScanners.peek();
            Client.logger.info("> ");
            String commandString = null;
            try {
                commandString = currentScanner.nextLine();
                if (currentScanner != inputScanner) {
                    Client.logger.info(commandString);
                }
            } catch (NoSuchElementException err) {
                if (currentScanner != inputScanner) {
                    Client.logger.info("End if file, execution ended...");
                    currentScanner.close();
                    scriptScanners.pop();
                    continue;
                }
                break;
            }

            var request = new ClientRequest(login, password, commandString, "", "");

            try {
                channel.writeRequest(request);
            } catch (IOException err) {
                Client.logger.error(
                    "Couldn't write a request to the server: {}",
                    err.getMessage()
                );
                continue;
            }

            try {
                ServerResponse response = channel.readResponse();
                Client.logger.info(response.output());
            } catch (IOException err) {
                Client.logger.error(
                    "IO Exception during reading response: {}",
                    err.getMessage()
                );
            } catch (BufferUnderflowException err) {
                Client.logger.warn("Couldn't connect to the server...");
                break;
            }
        }

        inputScanner.close();
        try {
            channel.close();
        } catch (IOException __) {}
    }
}
