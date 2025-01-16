package org.poo.commands;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.fileio.CommandInput;
import org.poo.main.Application;
import org.poo.main.cardTypes.Card;
import org.poo.main.cardTypes.OneTimeCard;

/**
 * Represents a command to create a new card for a specific user.
 * <p>
 * This command creates either a standard {@link Card} or a {@link OneTimeCard} based on the input
 * It then associates the card with the user identified by email and
 * registers the card in the application.
 */
public class CreateCard implements Command {
    private final Application app;
    private final Card card;
    private final String email;
    private final int timestamp;

    /**
     * Constructs a {@link CreateCard} command using the provided
     * {@link Application} and {@link CommandInput}.
     * Initializes the card type based on the command type, either creating a standard card
     * or a one-time-use card.
     * Also initializes the user's email and the timestamp.
     *
     * @param app the {@link Application} instance to interact with the application logic
     * @param input the {@link CommandInput} containing the card details, email, and timestamp
     */
    public CreateCard(final Application app, final CommandInput input) {
        this.app = app;
        if (input.getCommand().equals("createCard")) {
            card = new Card(input);
        } else {
            card = new OneTimeCard(input);
        }
        email = input.getEmail();
        timestamp = input.getTimestamp();
    }

    /**
     * Executes the create card command by calling the
     * {@link Application#createCard(String, Card, int)}
     * method to create the card and associate it with the specified user.
     * <p>
     * The command does not return any specific output, as the card is created
     * and added to the user's account.
     *
     * @return {@code null}; the result of the command is handled in the application logic
     */
    @Override
    public ObjectNode execute() {
        app.createCard(email, card, timestamp);
        return null;
    }
}
