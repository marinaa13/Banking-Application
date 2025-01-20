package org.poo.commands;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.fileio.CommandInput;
import org.poo.main.Application;
import org.poo.utils.Output;

/**
 * Represents a command to change the spending limit for a specific account and user.
 */
public class ChangeSpendingLimit implements Command {

    private final String email;
    private final String account;
    private final double amount;
    private final int timestamp;
    private final Application app;

    /**
     * Constructs a {@code ChangeSpendingLimit} command.
     *
     * @param app   the application instance to interact with
     * @param input the command input containing the email, account, amount, and timestamp
     */
    public ChangeSpendingLimit(final Application app, final CommandInput input) {
        this.app = app;
        this.email = input.getEmail();
        this.account = input.getAccount();
        this.amount = input.getAmount();
        this.timestamp = input.getTimestamp();
    }

    /**
     * Executes the change spending limit command by updating the spending limit
     * for the specified account and user.
     * <p>
     * If the operation fails, an {@link ObjectNode} containing error details is returned.
     * If the operation is successful, {@code null} is returned.
     *
     * @return an {@link ObjectNode} with the result of the operation on failure,
     *         or {@code null} if the operation is successful
     */
    @Override
    public ObjectNode execute() {
        ObjectNode inner = app.changeSpendingLimit(email, account, amount, timestamp);
        if (inner != null) {
            return Output.getCommand("changeSpendingLimit", inner, timestamp);
        }
        return null;
    }
}
