package org.poo.commands;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.fileio.CommandInput;
import org.poo.main.Application;
import org.poo.utils.Output;

/**
 * Represents a command to delete a specific account for a user identified by email.
 * <p>
 * This command interacts with the {@link Application} class to delete the account
 * associated with the specifiednemail and account number. The result of the deletion,
 * including any success or error messages, is returned as part of the command's output.
 */
public class DeleteAccount implements Command {
    private final String email;
    private final String account;
    private final Application app;
    private final int timestamp;

    /**
     * Constructs a {@link DeleteAccount} command using the provided
     * {@link Application} and {@link CommandInput}.
     * Initializes the email, account, and timestamp based on the input.
     *
     * @param app the {@link Application} instance to interact with the application logic
     * @param input the {@link CommandInput} containing the user's email, account,
     * and timestamp for the deletion request
     */
    public DeleteAccount(final Application app, final CommandInput input) {
        email = input.getEmail();
        account = input.getAccount();
        this.app = app;
        timestamp = input.getTimestamp();
    }

    /**
     * Executes the delete account command by calling the
     * {@link Application#deleteAccount(String, String, int)}
     * method to delete the specified account for the user identified by email.
     * <p>
     * The command returns the result of the deletion process in JSON format,
     * which includes a success or error message along with the timestamp of the operation.
     *
     * @return an {@link ObjectNode} containing the result of the deletion command
     */
    @Override
    public ObjectNode execute() {
        ObjectNode inner = app.deleteAccount(email, account, timestamp);
        return Output.getCommand("deleteAccount", inner, timestamp);
    }
}
