package org.poo.commands;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.fileio.CommandInput;
import org.poo.main.Application;

/**
 * Represents a command to set an alias for a specific account.
 * <p>
 * This command allows updating the alias for an account by interacting with the
 * {@link Application} class. Once executed, it sets the new alias for the specified account.
 */
public class SetAlias implements Command {
    private final Application app;
    private final String alias;
    private final String account;

    /**
     * Constructs a {@link SetAlias} command using the provided
     * {@link Application} and {@link CommandInput}.
     * Initializes the alias and account based on the input data.
     *
     * @param app the {@link Application} instance to interact with the application logic
     * @param input the {@link CommandInput} containing the alias and account data
     */
    public SetAlias(final Application app, final CommandInput input) {
        this.app = app;
        alias = input.getAlias();
        account = input.getAccount();
    }

    /**
     * Executes the set alias command by calling the
     * {@link Application#setAlias(String, String)} method.
     * <p>
     * The command does not return any specific output.
     * The alias for the account is updated internally.
     *
     * @return {@code null}, as the result of the command is handled within the application logic
     */
    @Override
    public ObjectNode execute() {
        app.setAlias(account, alias);
        return null;
    }
}
