package org.poo.commands;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.fileio.CommandInput;
import org.poo.main.Application;

/**
 * Represents a command to check the status of a card.
 * <p>
 * This command verifies the status of a specific card (e.g., if it is frozen
 * due to insufficient funds) by interacting with the {@link Application} class.
 * It returns the status of the card as part of the command's output.
 */
public class CheckCardStatus implements Command {
    private final String cardNumber;
    private final Application app;
    private final int timestamp;

    /**
     * Constructs a {@link CheckCardStatus} command using the provided
     * {@link Application} and {@link CommandInput}.
     * Initializes the card number and timestamp based on the input data.
     *
     * @param app the {@link Application} instance to interact with the application logic
     * @param input the {@link CommandInput} containing the card number and timestamp
     */
    public CheckCardStatus(final Application app, final CommandInput input) {
        this.app = app;
        this.cardNumber = input.getCardNumber();
        this.timestamp = input.getTimestamp();
    }

    /**
     * Executes the check card status command by calling the
     * {@link Application#checkCardStatus(String, int)} method.
     * The method checks if the card is frozen or has any issues that require attention.
     * <p>
     * If the operation is successful, it returns the status of the card in a JSON format,
     * including the timestamp and command details.
     * If the operation fails or the card cannot be found, {@code null} is returned.
     *
     * @return an {@link ObjectNode} containing the result of the operation,
     * including the card status and timestamp or {@code null} if the status could not be retrieved
     */
    @Override
    public ObjectNode execute() {
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        ObjectNode inner = app.checkCardStatus(cardNumber, timestamp);
        if (inner != null) {
            node.put("command", "checkCardStatus");
            node.set("output", inner);
            node.put("timestamp", timestamp);
            return node;
        }
        return null;
    }
}
