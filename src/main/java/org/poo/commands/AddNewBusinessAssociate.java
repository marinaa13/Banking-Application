package org.poo.commands;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.fileio.CommandInput;
import org.poo.main.Application;

/**
 * Represents a command to add a new business associate to a business account.
 */
public class AddNewBusinessAssociate implements Command {

    private final String account;
    private final String role;
    private final String email;
    private final int timestamp;
    private final Application app;

    /**
     * Constructs a new {@code AddNewBusinessAssociate} command.
     *
     * @param app   the application instance to interact with
     * @param input the command input containing the account, role, email, and timestamp
     */
    public AddNewBusinessAssociate(final Application app, final CommandInput input) {
        account = input.getAccount();
        role = input.getRole();
        email = input.getEmail();
        timestamp = input.getTimestamp();
        this.app = app;
    }

    /**
     * Executes the add new business associate command.
     *
     * @return {@code null} as this command does not produce a specific output
     */
    @Override
    public ObjectNode execute() {
        app.addNewBussinessAssociate(account, role, email, timestamp);
        return null;
    }
}
