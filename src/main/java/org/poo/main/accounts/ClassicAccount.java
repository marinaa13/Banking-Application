package org.poo.main.accounts;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.fileio.CommandInput;
import org.poo.main.cardTypes.Card;

public class ClassicAccount extends Account{
    public ClassicAccount(CommandInput input) {
        super(input);
    }

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
     * <p>
     * This operation is not supported for this account type and throws an exception.
     */
    public void setInterestRate(final double interestRate) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Adds interest to the account.
     * <p>
     * This operation is not supported for this account type and throws an exception.
     */
    public ObjectNode addInterest() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void changeDepositLimit(double amount, String email, int timestamp) {
        throw new UnsupportedOperationException("This is not a business account");
    }

    @Override
    public ObjectNode changeSpendingLimit(double amount, String email, int timestamp) {
        throw new UnsupportedOperationException("This is not a business account");
    }

    @Override
    public boolean isClassicAccount() {
        return true;
    }

}
