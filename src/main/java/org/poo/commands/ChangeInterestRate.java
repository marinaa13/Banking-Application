package org.poo.commands;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.fileio.CommandInput;
import org.poo.main.Application;
import org.poo.utils.Output;

/**
 * Represents a command to change the interest rate for a specific account.
 * <p>
 * This command updates the interest rate for an account by interacting with
 * the {@link Application} class.
 */
public class ChangeInterestRate implements Command {

    private final Application app;
    private final double newInterestRate;
    private final int timestamp;
    private final String account;

    /**
     * Constructs a {@link ChangeInterestRate} command using the provided
     * {@link Application} and {@link CommandInput}.
     *
     * @param app   the {@link Application} instance to interact with
     * @param input the {@link CommandInput} containing the interest rate,
     *              account, and timestamp data
     */
    public ChangeInterestRate(final Application app, final CommandInput input) {
        this.app = app;
        newInterestRate = input.getInterestRate();
        timestamp = input.getTimestamp();
        account = input.getAccount();
    }

    /**
     * Executes the change interest rate command by updating the interest rate
     * for the specified account.
     *
     * @return an {@link ObjectNode} containing the result of the operation,
     *         or {@code null} if the operation fails
     */
    @Override
    public ObjectNode execute() {
        ObjectNode inner = app.changeInterestRate(newInterestRate, account, timestamp);
        if (inner != null) {
            return Output.getCommand("changeInterestRate", inner, timestamp);
        }
        return null;
    }
}
