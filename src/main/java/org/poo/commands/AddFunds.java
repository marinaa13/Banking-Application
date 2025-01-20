package org.poo.commands;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.fileio.CommandInput;
import org.poo.main.Application;

/**
 * Represents a command to add funds to a specific account.
 * <p>
 * This command allows adding a specified amount of funds to the account identified by its IBAN.
 * The action is performed by interacting with the {@link Application} class.
 */
public class AddFunds implements Command {
    private final Application app;
    private final String account;
    private final double amount;
    private final String email;
    private final int timestamp;

    /**
     * Constructs an {@link AddFunds} command using the provided
     * {@link Application} and {@link CommandInput}.
     * Initializes the account and amount based on the input.
     *
     * @param app the {@link Application} instance to interact with the application logic
     * @param input the {@link CommandInput} containing the account and amount data
     */
    public AddFunds(final Application app, final CommandInput input) {
        this.app = app;
        this.account = input.getAccount();
        this.amount = input.getAmount();
        this.timestamp = input.getTimestamp();
        email = input.getEmail();
    }

    /**
     * Executes the add funds command by calling the
     * {@link Application#addFunds(String, double, String, int)}
     * method to add the specified amount to the account.
     * <p>
     * The command does not return any specific output,
     * as the operation is handled internally within the application.
     *
     * @return {@code null}, as the result of the command is handled
     * internally in the application logic
     */
    @Override
    public ObjectNode execute() {
        app.addFunds(account, amount, email, timestamp);
        return null;
    }
}
