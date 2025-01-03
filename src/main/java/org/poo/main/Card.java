package org.poo.main;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.Setter;
import org.poo.fileio.CommandInput;

/**
 * Represents a payment card associated with an account.
 * <p>
 * This class includes methods for handling payment-related functionality and
 * generating a JSON representation of the card.
 */
@Getter @Setter
public class Card {
    private String account;
    private String status;
    private String cardNumber;
    private Account accountBelonging;

    /**
     * Constructs a new {@link Card} using the provided {@link CommandInput}.
     * Initializes the card with the account and sets its status to "active".
     * The card number will be generated when the card is added to an account.
     *
     * @param input the {@link CommandInput} containing the account information for the card
     */
    public Card(final CommandInput input) {
        account = input.getAccount();
        status = "active";
    }

    /**
     * Constructs a new {@link Card} for the specified account.
     * Sets the status to "active".
     *
     * @param account the account associated with the card
     */
    public Card(final String account) {
        this.account = account;
        status = "active";
    }

    /**
     * Converts the card to a JSON object.
     * The resulting JSON object contains the card number and its status.
     *
     * @return a JSON representation of the card
     */
    public ObjectNode getJson() {
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        node.put("cardNumber", cardNumber);
        node.put("status", status);
        return node;
    }

    /**
     * Blocks the card, changing its status to "frozen".
     */
    public void blockCard() {
        status = "frozen";
    }

    /**
     * This method is called when a payment is made with the card.
     * Currently, this method is a placeholder and doesn't perform any action.
     *
     * @param timestamp the timestamp when the payment is made
     */
    public void madePayment(final int timestamp) {
    }
}
