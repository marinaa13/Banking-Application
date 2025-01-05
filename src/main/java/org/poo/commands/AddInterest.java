package org.poo.commands;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.fileio.CommandInput;
import org.poo.main.Application;
import org.poo.utils.Output;

/**
 * Represents a command to add interest to a specific account.
 * <p>
 * This command calculates and adds interest to the account identified by its IBAN. The action is
 * executed by interacting with the {@link Application} class,
 * which handles the logic for adding interest.
 */
public class AddInterest implements Command {
    private final Application app;
    private final String account;
    private final int timestamp;

    /**
     * Constructs an {@link AddInterest} command using the provided
     * {@link Application} and {@link CommandInput}.
     * Initializes the account and timestamp based on the input data.
     *
     * @param app the {@link Application} instance to interact with the application logic
     * @param input the {@link CommandInput} containing the account and timestamp data
     */
    public AddInterest(final Application app, final CommandInput input) {
        this.app = app;
        this.account = input.getAccount();
        this.timestamp = input.getTimestamp();
    }

    /**
     * Executes the add interest command by calling the
     * {@link Application#addInterest(String, int)}
     * method to add the interest to the specified account.
     * <p>
     * If the operation is successful, the result is returned in JSON format
     * including the timestamp and details.
     * If the operation is not successful, {@code null} is returned.
     *
     * @return an {@link ObjectNode} containing the result of the operation,
     * or {@code null} if the operation fails
     */
    @Override
    public ObjectNode execute() {
        ObjectNode inner = app.addInterest(account, timestamp);
        if (inner != null) {
            return Output.getCommand("addInterest", inner, timestamp);
        }
        return null;
    }
}
