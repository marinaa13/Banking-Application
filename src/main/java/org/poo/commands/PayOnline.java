package org.poo.commands;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.fileio.CommandInput;
import org.poo.main.Application;
import org.poo.utils.Output;

/**
 * Represents a command to make an online payment using a specified card.
 * <p>
 * This command processes an online payment by interacting with the
 * {@link Application} class to deduct the specified amount from the card,
 * converting the currency if necessary, and recording the payment.
 */
public class PayOnline implements Command {
    private final Application app;
    private final String cardNumber;
    private final double amount;
    private final String currency;
    private String commerciant;
    private final int timestamp;
    private final String email;

    /**
     * Constructs a {@link PayOnline} command using the provided
     * {@link Application} and {@link CommandInput}.
     * Initializes the card number, amount, currency, commerciant, and timestamp.
     *
     * @param app the {@link Application} instance to interact with the application logic
     * @param input the {@link CommandInput} containing the payment details
     */
    public PayOnline(final Application app, final CommandInput input) {
        this.app = app;
        cardNumber = input.getCardNumber();
        amount = input.getAmount();
        currency = input.getCurrency();
        timestamp = input.getTimestamp();
        commerciant = input.getCommerciant();
        email = input.getEmail();
    }

    /**
     * Executes the online payment command by calling the
     * {@link Application#payOnline(String, double, String, int, String, String)}
     * method to process the payment for the specified card and amount.
     * <p>
     * If the payment is successful, the result is returned in JSON format,
     * including the payment details and timestamp.
     * If the payment fails (e.g., due to insufficient funds or invalid card),
     * {@code null} is returned.
     *
     * @return an {@link ObjectNode} containing the result of the payment
     *         or {@code null} if the payment is not successful
     */
    @Override
    public ObjectNode execute() {
        if (amount == 0) {
            return null;
        }
        ObjectNode inner = app.payOnline(cardNumber, amount, currency, timestamp,
                                         commerciant, email);
        if (inner != null) {
            return Output.getCommand("payOnline", inner, timestamp);
        }
        return null;
    }
}
