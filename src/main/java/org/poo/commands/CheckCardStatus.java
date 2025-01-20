package org.poo.commands;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.fileio.CommandInput;
import org.poo.main.Application;
import org.poo.utils.Output;

/**
 * Represents a command to check the status of a card.
 * <p>
 * This command verifies the status of a specific card (e.g., if it is frozen
 * due to insufficient funds) by interacting with the {@link Application} class.
 * The command returns {@code null} if the operation is successful and an error message
 * in JSON format if it fails.
 */
public class CheckCardStatus implements Command {

    private final String cardNumber;
    private final Application app;
    private final int timestamp;

    /**
     * Constructs a {@code CheckCardStatus} command using the provided
     * {@link Application} and {@link CommandInput}.
     * Initializes the card number and timestamp based on the input data.
     *
     * @param app   the {@link Application} instance to interact with the application logic
     * @param input the {@link CommandInput} containing the card number and timestamp
     */
    public CheckCardStatus(final Application app, final CommandInput input) {
        this.app = app;
        cardNumber = input.getCardNumber();
        timestamp = input.getTimestamp();
    }

    /**
     * Executes the check card status command by calling the
     * {@link Application#checkCardStatus(String, int)} method.
     * <p>
     * If the operation is successful, {@code null} is returned.
     * If the operation fails, an {@link ObjectNode} containing the error details is returned.
     */
    @Override
    public ObjectNode execute() {
        ObjectNode inner = app.checkCardStatus(cardNumber, timestamp);
        if (inner != null) {
            return Output.getCommand("checkCardStatus", inner, timestamp);
        }
        return null;
    }
}
