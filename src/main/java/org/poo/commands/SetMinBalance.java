package org.poo.commands;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.fileio.CommandInput;
import org.poo.main.Application;

/**
 * Represents a command to set the minimum balance for a specific account.
 * <p>
 * This command allows updating the minimum balance requirement for an account
 * by interacting with the {@link Application} class.
 */
public class SetMinBalance implements Command {
    private final Application app;
    private final double amount;
    private final String account;

    /**
     * Constructs a {@link SetMinBalance} command using the provided
     * {@link Application} and {@link CommandInput}.
     * Initializes the amount and account based on the input data.
     *
     * @param app the {@link Application} instance to interact with the application logic
     * @param input the {@link CommandInput} containing the amount and account data
     */
    public SetMinBalance(final Application app, final CommandInput input) {
        this.app = app;
        amount = input.getAmount();
        account = input.getAccount();
    }

    /**
     * Executes the set minimum balance command by calling the
     * {@link Application#setMinBalance(String, double)} method.
     * <p>
     * The command does not return any specific output.
     * The minimum balance for the account is updated internally.
     *
     * @return {@code null}, as the result of the command is handled within the application logic
     */
    @Override
    public ObjectNode execute() {
        app.setMinBalance(account, amount);
        return null;
    }
}
