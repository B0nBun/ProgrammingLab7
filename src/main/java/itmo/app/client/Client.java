package itmo.app.client;

import itmo.app.shared.ClientRequest;
import itmo.app.shared.ServerResponse;
import itmo.app.shared.commands.Command;
import itmo.app.shared.commands.CommandRegistery;
import java.io.IOException;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.nio.BufferUnderflowException;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.Map;
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
        String login = null;
        while (login == null || login.length() == 0) {
            Client.logger.info("Login:");
            login = System.console().readLine();
            if (login.length() == 0) Client.logger.warn(
                "login must be a nonempty string"
            );
        }
        String password = null;
        while (password == null || password.length() == 0) {
            Client.logger.info("Password:");
            password = new String(System.console().readPassword());
            if (password.length() == 0) Client.logger.warn(
                "password must be a nonempty string"
            );
        }

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

            Map.Entry<String, List<String>> nameAndParams = Command.nameAndStringParams(
                commandString
            );
            Command<Serializable, Serializable> command = CommandRegistery.global.get(
                nameAndParams.getKey()
            );
            if (command == null) {
                Client.logger.warn("Unknown command'" + nameAndParams.getKey() + "'");
                continue;
            }
            Serializable params = command.getParamsFromStrings(nameAndParams.getValue());
            Serializable additional = command.scanAdditionalObject(currentScanner);
            var request = new ClientRequest<Serializable, Serializable>(
                login,
                password,
                nameAndParams.getKey(),
                params,
                additional
            );

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
