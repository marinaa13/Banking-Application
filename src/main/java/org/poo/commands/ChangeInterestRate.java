package org.poo.commands;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.fileio.CommandInput;
import org.poo.main.Application;

/**
 * Represents a command to change the interest rate for a specific account.
 * <p>
 * This command allows updating the interest rate for an account.
 * The new interest rate is applied to the specified
 * account by interacting with the {@link Application} class.
 */
public class ChangeInterestRate implements Command {
    private final Application app;
    private final double newInterestRate;
    private final int timestamp;
    private final String account;

    /**
     * Constructs a {@link ChangeInterestRate} command using the provided
     * {@link Application} and {@link CommandInput}.
     * Initializes the new interest rate, timestamp, and account based on the input data.
     *
     * @param app the {@link Application} instance to interact with the application logic
     * @param input the {@link CommandInput} containing the new interest rate,
     * account, and timestamp data
     */
    public ChangeInterestRate(final Application app, final CommandInput input) {
        this.app = app;
        newInterestRate = input.getInterestRate();
        timestamp = input.getTimestamp();
        account = input.getAccount();
    }

    /**
     * Executes the change interest rate command by calling the
     * {@link Application#changeInterestRate(double, String, int)}
     * method to update the interest rate for the specified account.
     * <p>
     * If the operation is successful, the result is returned in JSON format,
     * including the timestamp and details.
     * If the operation is not successful, {@code null} is returned.
     *
     * @return an {@link ObjectNode} containing the result of the operation,
     * or {@code null} if the operation fails
     */
    @Override
    public ObjectNode execute() {
        ObjectNode inner = app.changeInterestRate(newInterestRate, account, timestamp);
        if (inner != null) {
            ObjectNode node = JsonNodeFactory.instance.objectNode();
            node.put("command", "changeInterestRate");
            node.set("output", inner);
            node.put("timestamp", timestamp);
            return node;
        }
        return null;
    }
}
