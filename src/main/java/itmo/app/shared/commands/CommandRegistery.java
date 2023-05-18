package itmo.app.shared.commands;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Set;

public class CommandRegistery {

    public static CommandRegistery global = new CommandRegistery()
        .put(new HelpCommand(), "help", "h")
        .put(new ShowCommand(), "show", "s")
        .put(new AddCommand(), "add", "a")
        .put(new ClearCommand(), "clear")
        .put(new HeadCommand(), "head")
        .put(new InfoCommand(), "info");

    private LinkedHashMap<Collection<String>, Command<? extends Serializable, ? extends Serializable>> commandsMap = new LinkedHashMap<>();

    /**
     * Put a command entry in the internal map
     *
     * @param commandAliases
     * @param command
     * @return {@code this} for method chaining
     */
    private CommandRegistery put(
        Collection<String> commandAliases,
        Command<? extends Serializable, ? extends Serializable> command
    ) {
        this.commandsMap.put(commandAliases, command);
        return this;
    }

    /**
     * Put a command entry in the internal map
     *
     * @param commandAliases
     * @param command
     * @return {@code this} for method chaining
     */
    private CommandRegistery put(
        Command<? extends Serializable, ? extends Serializable> command,
        String... commandAliases
    ) {
        return this.put(Arrays.asList(commandAliases), command);
    }

    /**
     * Get a {@link DeprecatedCommand} associated with the given name. Traverses all of the keys in
     * the internal {@code Map<Collection<String>, Command>} and if the given key is found in the
     * key-collection, returns the found command.
     *
     * @param commandName
     * @return Found command
     */
    public Command<Serializable, Serializable> get(String commandName) {
        for (var entry : this.commandsMap.entrySet()) {
            if (entry.getKey().contains(commandName)) {
                @SuppressWarnings({ "unchecked" })
                var c = (Command<Serializable, Serializable>) entry.getValue();
                return c;
            }
        }
        return null;
    }

    /**
     * Get the entry set of the internal map
     *
     * @return
     */
    public Set<Entry<Collection<String>, Command<? extends Serializable, ? extends Serializable>>> getAllCommands() {
        return this.commandsMap.entrySet();
    }
}
