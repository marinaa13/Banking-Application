package org.poo.commands;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.fileio.CommandInput;
import org.poo.main.Application;
import org.poo.utils.Output;

/**
 * Represents a command to print the details of all users in the application.
 * <p>
 * This command generates a JSON array containing the information of each user in the system,
 * and returns it as part of the command's output.
 */
public class PrintUsers implements Command {
    private final Application app;
    private final int timestamp;

    /**
     * Constructs a {@link PrintUsers} command using the provided
     * {@link Application} and {@link CommandInput}.
     * Initializes the timestamp based on the input.
     *
     * @param app the {@link Application} instance to interact with the application logic
     * @param input the {@link CommandInput} containing the timestamp data
     */
    public PrintUsers(final Application app, final CommandInput input) {
        this.app = app;
        timestamp = input.getTimestamp();
    }

    /**
     * Executes the print users command by retrieving the list of users from the application
     * and generating a JSON array with the details of each user.
     * <p>
     * The command returns the user data in JSON format,
     * including the timestamp and command details.
     *
     * @return an {@link ObjectNode} containing the result of the command
     */
    @Override
    public ObjectNode execute() {
        ArrayNode inner = app.printUsers();
        return Output.getCommand("printUsers", inner, timestamp);
    }
}
