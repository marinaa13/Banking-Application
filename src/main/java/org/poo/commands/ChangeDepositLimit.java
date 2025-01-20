package org.poo.commands;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.fileio.CommandInput;
import org.poo.main.Application;
import org.poo.utils.Output;

/**
 * Represents a command to change the deposit limit for a specific account and user.
 */
public class ChangeDepositLimit implements Command {

    private final String email;
    private final String account;
    private final double amount;
    private final int timestamp;
    private final Application app;

    /**
     * Constructs a new {@code ChangeDepositLimit} command.
     *
     * @param app   the application instance to interact with
     * @param input the command input containing the email, account, amount, and timestamp
     */
    public ChangeDepositLimit(final Application app, final CommandInput input) {
        this.app = app;
        email = input.getEmail();
        account = input.getAccount();
        amount = input.getAmount();
        timestamp = input.getTimestamp();
    }

    /**
     * Executes the change deposit limit command.
     *
     * @return an {@link ObjectNode} containing the result of the operation,
     *         or {@code null} if the operation was unsuccessful
     */
    @Override
    public ObjectNode execute() {
        ObjectNode inner = app.changeDepositLimit(email, account, amount, timestamp);
        if (inner != null) {
            return Output.getCommand("changeDepositLimit", inner, timestamp);
        }
        return null;
    }
}
