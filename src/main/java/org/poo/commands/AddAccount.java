package org.poo.commands;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.fileio.CommandInput;
import org.poo.main.accounts.Account;
import org.poo.main.accounts.AccountFactory;
import org.poo.main.accounts.SavingsAccount;
import org.poo.main.accounts.BusinessAccount;

import org.poo.main.Application;

/**
 * Represents a command to add a new account for a specific user.
 * <p>
 * This command allows the creation of either a regular {@link Account}, a {@link SavingsAccount}
 * or a {@link BusinessAccount} depending on the account type provided in the input.
 * The account is then added to the user's profile in the application.
 */
public class AddAccount implements Command {
    private final Application application;
    private final Account account;
    private final String email;
    private final int timestamp;

    /**
     * Constructs an {@link AddAccount} command using the provided
     * {@link Application} and {@link CommandInput}.
     * Initializes the account type based on the input. If the account type is "savings",
     * a {@link SavingsAccount} is created; otherwise, a regular {@link Account} is created.
     * The email and timestamp are also initialized.
     *
     * @param app the {@link Application} instance to interact with the application logic
     * @param input the {@link CommandInput} containing the user's email and account type
     */
    public AddAccount(final Application app, final CommandInput input) {
        this.application = app;
        this.email = input.getEmail();
        this.timestamp = input.getTimestamp();

        AccountFactory factory = AccountFactory.getInstance();
        account = factory.getAccount(input.getAccountType(), input, app);
    }

    /**
     * Executes the add account command by calling the
     * {@link Application#addAccount(String, Account, int)}
     * method to add the newly created account to the user's profile.
     * <p>
     * The command does not return any specific output,
     * as the account is added to the application internally.
     *
     * @return {@code null}, as the result of the command is handled by the application logic
     */
    @Override
    public ObjectNode execute() {
        application.addAccount(email, account, timestamp);
        return null;
    }
}
