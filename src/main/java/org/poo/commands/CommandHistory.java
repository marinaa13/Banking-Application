package org.poo.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;

/**
 * Represents the history of commands executed in the application.
 * <p>
 * This class stores a list of commands that have been executed, represented as JSON objects.
 * The history is stored as an array of {@link ObjectNode} instances, where each node contains
 * details about a specific command's execution.
 * It allows for adding new commands to the history and retrieving the list of executed commands.
 */
@Getter
public class CommandHistory {
    private final ArrayNode history;

    /**
     * Constructs a {@link CommandHistory} object and initializes the history array.
     * The history array will store the details of each command that is executed in the application
     */
    public CommandHistory() {
        ObjectMapper mapper = new ObjectMapper();
        history = mapper.createArrayNode();
    }

    /**
     * Adds a new command to the history.
     * <p>
     * This method takes a JSON object representing a command and adds it to the history array.
     * If the provided node is not {@code null}, it will be added to the history.
     *
     * @param node the {@link ObjectNode} containing the details of the command to add
     */
    public void addToHistory(final ObjectNode node) {
        if (node != null) {
            history.add(node);
        }
    }
}
