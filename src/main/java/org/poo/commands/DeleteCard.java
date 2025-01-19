package org.poo.commands;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.fileio.CommandInput;
import org.poo.main.Application;

/**
 * Represents a command to delete a specified card from the system.
 * <p>
 * This command interacts with the {@link Application} class to delete the card
 * identified by its card number.
 * After execution, the corresponding transaction will be recorded.
 */
public class DeleteCard implements Command {
    private final Application app;
    private final String cardNumber;
    private final int timestamp;
    private final String email;

    /**
     * Constructs a {@link DeleteCard} command using the provided
     * {@link Application} and {@link CommandInput}.
     * Initializes the card number and timestamp based on the input data.
     *
     * @param app the {@link Application} instance to interact with the application logic
     * @param input the {@link CommandInput} containing the card number and timestamp
     */
    public DeleteCard(final Application app, final CommandInput input) {
        this.app = app;
        this.cardNumber = input.getCardNumber();
        this.timestamp = input.getTimestamp();
        email = input.getEmail();
    }

    /**
     * Executes the delete card command by calling the
     * {@link Application#deleteCard(String, int, String)} method to delete the specified card.
     * <p>
     * The command does not return any specific output, as the operation is handled
     * internally within the application.
     *
     * @return {@code null}, as the result of the command is handled by the application logic
     */
    @Override
    public ObjectNode execute() {
        app.deleteCard(cardNumber, timestamp, email);
        return null;
    }
}
