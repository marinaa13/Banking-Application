package org.poo.commands;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.fileio.CommandInput;
import org.poo.main.Application;

/**
 * Invokes commands based on the provided {@link CommandInput}.
 * <p>
 * This class acts as an intermediary between the input data and the command execution process.
 * It uses the {@link CommandFactory} to create the appropriate command based on the input and then
 * executes the command. The result of the command execution is returned as an {@link ObjectNode},
 * which contains the output of the executed command.
 */
public class CommandInvoker {
    private final CommandFactory commandFactory;

    /**
     * Constructs a {@link CommandInvoker} object, which initializes the {@link CommandFactory}.
     * The factory is used to create commands based on the provided input.
     */
    public CommandInvoker() {
        this.commandFactory = new CommandFactory();
    }

    /**
     * Executes a command based on the given {@link CommandInput} and {@link Application} instance.
     * <p>
     * This method uses the {@link CommandFactory} to create the appropriate command object,
     * and then it calls the {@link Command#execute()} method to execute the command.
     * The result of the execution is returned as an {@link ObjectNode}.
     *
     * @param input the {@link CommandInput} containing the details needed to
     *              create and execute the command
     * @param app the {@link Application} instance to interact with the application logic
     * @return an {@link ObjectNode} containing the result of the command execution
     */
    public ObjectNode executeCommand(final CommandInput input, final Application app) {
        Command cmd = commandFactory.createCommand(input, app);
        return cmd.execute();
    }
}
