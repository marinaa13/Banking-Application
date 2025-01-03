package org.poo.commands;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.fileio.CommandInput;
import org.poo.main.Application;

/**
 * Represents a command to print the transaction history for a specific user.
 * <p>
 * This command retrieves the transaction history for the user  and generates a JSON array
 * containing the details of each transaction, returning it as part of the command's output.
 */
public class PrintTransactions implements Command {
    private final Application app;
    private final int timestamp;
    private final String email;

    /**
     * Constructs a {@link PrintTransactions} command using the provided
     * {@link Application} and {@link CommandInput}.
     * Initializes the email and timestamp based on the input.
     *
     * @param app the {@link Application} instance to interact with the application logic
     * @param input the {@link CommandInput} containing the user's email and timestamp
     */
    public PrintTransactions(final Application app, final CommandInput input) {
        this.app = app;
        this.email = input.getEmail();
        this.timestamp = input.getTimestamp();
    }

    /**
     * Executes the print transactions command by retrieving the transaction history
     * for the specified user. The transaction details are returned as a JSON array.
     * <p>
     * If the user exists, the transaction history is returned in JSON format;
     * otherwise, an empty response is returned.
     *
     * @return an {@link ObjectNode} containing the result of the command,
     * including the transaction history and timestamp
     */
    @Override
    public ObjectNode execute() {
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        node.put("command", "printTransactions");

        ArrayNode inner = app.printTransactions(email);

        node.set("output", inner);
        node.put("timestamp", timestamp);
        return node;
    }
}
