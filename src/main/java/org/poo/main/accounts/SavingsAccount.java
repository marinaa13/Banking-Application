package org.poo.main.accounts;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.Setter;
import org.poo.fileio.CommandInput;
import org.poo.main.cardTypes.Card;

/**
 * Represents a savings account that extends from the {@link Account} class.
 * Includes a specific interest rate and specific methods.
 */
@Getter @Setter
public class SavingsAccount extends Account {
    private double interestRate;

    /**
     * Constructs a new {@link SavingsAccount} using the provided {@link CommandInput}.
     * Initializes the savings account with the input data, including the interest rate.
     *
     * @param input the {@link CommandInput} containing the account data
     */
    public SavingsAccount(final CommandInput input) {
        super(input);
        this.interestRate = input.getInterestRate();
    }

    /**
     * Converts the savings account to a JSON object.
     * The resulting JSON includes all the account data, as well as
     * the cards associated with the account.
     *
     * @return a JSON representation of the savings account
     */
    @Override
    public ObjectNode getJson() {
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        node.put("IBAN", getIban());
        node.put("balance", getBalance());
        node.put("currency", getCurrency());
        node.put("type", "savings");

        ArrayNode array = JsonNodeFactory.instance.arrayNode();
        for (Card card : getCards()) {
            array.add(card.getJson());
        }
        node.set("cards", array);
        return node;
    }

    /**
     * Adds interest to the savings account balance based on the interest rate.
     * The interest is calculated using the current balance and the interest rate.
     */
    public ObjectNode addInterest() {
        ObjectNode node = JsonNodeFactory.instance.objectNode();

        double interest = getBalance() * interestRate;
        setBalance(getBalance() + interest);

        node.put("amount", interest);
        node.put("currency", getCurrency());
        node.put("description", "Interest rate income");
        return node;
    }

    /**
     * {@inheritDoc}
     * Savings accounts do not have a spending report, so this method throws an
     * {@link UnsupportedOperationException}.
     */
    @Override
    public ArrayNode getSpendingsReport(final int startTimestamp, final int endTimestamp) {
        throw new UnsupportedOperationException("Savings accounts do not have a spendings report");
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
    public boolean isSavingsAccount() {
        return true;
    }
}
