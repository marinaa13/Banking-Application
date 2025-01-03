package org.poo.commands;

import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Represents a command in the system that can be executed.
 * <p>
 * This interface defines a common structure for all command classes in the application.
 * Any command class that implements this interface must define the {@link #execute()} method,
 * which encapsulates the logic for performing the command's action and returning the result
 * in the form of a JSON object.
 */
public interface Command {

    /**
     * Executes the command and returns the result in the form of a JSON object.
     * <p>
     * This method is responsible for performing the specific action of the command
     * (e.g., adding funds, creating an account, etc.). The result of the operation is returned
     * as a JSON object that can be used to communicate the outcome of the operation to other
     * components or layers of the application.
     *
     * @return an {@link ObjectNode} containing the result of the operation
     */
    ObjectNode execute();
}
