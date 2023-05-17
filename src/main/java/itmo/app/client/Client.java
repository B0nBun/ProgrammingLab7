package itmo.app.client;

import itmo.app.shared.ClientRequest;
import itmo.app.shared.ServerResponse;
import java.io.IOException;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.nio.BufferUnderflowException;
import java.nio.channels.SocketChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
        String passwordHashed;
        {
            Client.logger.info("Password:");
            String password = new String(System.console().readPassword());
            passwordHashed = hashPassword(password);
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

            ClientRequest request;
            {
                request = new ClientRequest(login, passwordHashed, commandString, "", "");
            }

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

    private static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-224");
            var no = new BigInteger(1, md.digest(password.getBytes()));
            String hash = no.toString(16);
            while (hash.length() < 32) {
                hash = "0" + hash;
            }
            return hash;
        } catch (NoSuchAlgorithmException err) {
            throw new RuntimeException(err.getMessage());
        }
    }
}
