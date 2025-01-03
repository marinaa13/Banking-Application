package org.poo.main;

import org.poo.fileio.CommandInput;
import org.poo.utils.Utils;

/**
 * Represents a one-time use card that extends the {@link Card} class.
 * <p>
 * This card is used for a single payment. After a payment is made, the card is deleted
 * and a new card is generated for the account.
 */
public class OneTimeCard extends Card {

    /**
     * Constructs a new {@link OneTimeCard} using the provided {@link CommandInput}.
     * Initializes the one-time use card with data from the input.
     *
     * @param input the {@link CommandInput} containing the card data
     */
    public OneTimeCard(final CommandInput input) {
        super(input);
    }

    /**
     * Processes a payment made using the one-time card.
     * After the payment, the card is deleted from the account, and a new card
     * is generated for the account. The new card is added to the owner's list of cards.
     *
     * @param timestamp the timestamp when the payment was made
     */
    @Override
    public void madePayment(final int timestamp) {
        getAccountBelonging().deleteCard(getCardNumber(), timestamp);

        Card card = new Card(getAccount());
        card.setCardNumber(Utils.generateCardNumber());

        getAccountBelonging().getOwner().addCard(card, timestamp);
    }
}
