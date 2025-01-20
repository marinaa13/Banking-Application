package org.poo.main.accounts;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.fileio.CommandInput;
import org.poo.main.cardTypes.Card;

/**
 * Represents a classic bank account that extends the {@link Account} class.
 * Classic accounts support basic banking functionalities but do not include
 * features like interest or spending/deposit limits.
 */
public class ClassicAccount extends Account {

    /**
     * Constructs a {@link ClassicAccount} using the provided {@link CommandInput}.
     *
     * @param input the {@link CommandInput} containing the account data
     */
    public ClassicAccount(final CommandInput input) {
        super(input);
    }

    /**
     * Converts the account to a JSON object representation.
     * Includes the account's IBAN, balance, currency, type, and associated cards.
     *
     * @return an {@link ObjectNode} containing the account's details
     */
    @Override
    public ObjectNode getJson() {
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        node.put("IBAN", getIban());
        node.put("balance", getBalance());
        node.put("currency", getCurrency());
        node.put("type", "classic");

        ArrayNode array = JsonNodeFactory.instance.arrayNode();
        for (Card card : getCards()) {
            array.add(card.getJson());
        }
        node.set("cards", array);
        return node;
    }

    /**
     * Sets the interest rate for the account.
     * This operation is not supported for classic accounts.
     *
     * @param interestRate the interest rate to be set
     * @throws UnsupportedOperationException always
     */
    @Override
    public void setInterestRate(final double interestRate) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Adds interest to the account.
     * This operation is not supported for classic accounts.
     *
     * @return nothing, as this operation is not supported
     * @throws UnsupportedOperationException always
     */
    @Override
    public ObjectNode addInterest() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Changes the deposit limit for the account.
     * This operation is not supported for classic accounts.
     *
     * @param amount    the new deposit limit
     * @param email     the email of the user requesting the change
     * @param timestamp the timestamp of the request
     * @return nothing, as this operation is not supported
     * @throws UnsupportedOperationException always
     */
    @Override
    public ObjectNode changeDepositLimit(final double amount, final String email,
                                         final int timestamp) {
        throw new UnsupportedOperationException("This is not a business account");
    }

    /**
     * Changes the spending limit for the account.
     * This operation is not supported for classic accounts.
     *
     * @param amount    the new spending limit
     * @param email     the email of the user requesting the change
     * @param timestamp the timestamp of the request
     * @return nothing, as this operation is not supported
     * @throws UnsupportedOperationException always
     */
    @Override
    public ObjectNode changeSpendingLimit(final double amount, final String email,
                                          final int timestamp) {
        throw new UnsupportedOperationException("This is not a business account");
    }

    /**
     * Checks if this account is a classic account.
     *
     * @return {@code true} since this is a classic account
     */
    @Override
    public boolean isClassicAccount() {
        return true;
    }
}
