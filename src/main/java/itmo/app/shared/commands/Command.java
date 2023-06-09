package itmo.app.shared.commands;

import itmo.app.shared.ClientRequest;
import itmo.app.shared.exceptions.InvalidParamsException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

public interface Command<P extends Serializable, A extends Serializable> {
    public static record Context<P extends Serializable, A extends Serializable>(
        ClientRequest<P, A> request,
        PrintWriter printer
    ) {}

    public static Serializable dummySerializable = new Serializable() {};

    public P getParamsFromStrings(List<String> stringParams)
        throws InvalidParamsException;

    public A scanAdditionalObject(Scanner scanner, boolean logBack);

    public void execute(Context<P, A> commandContext) throws SQLException;

    public default String helpMessage() {
        return "";
    }

    public default List<String> helpParams() {
        return List.of();
    }

    public static Entry<String, List<String>> nameAndStringParams(String commandString) {
        var splitted = List.of(commandString.trim().split("\\s+"));
        if (splitted.size() == 0) return null;
        return Map.entry(splitted.get(0), splitted.subList(1, splitted.size()));
    }
}
