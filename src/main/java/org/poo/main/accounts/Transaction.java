package org.poo.main.accounts;

import lombok.Getter;
import lombok.Setter;

/**
 * Represents a transaction associated with a commerciant and an amount.
 */
@Getter
@Setter
public class Transaction {
    private final String commerciant;
    private final double amount;

    /**
     * Constructs a new {@code Transaction}.
     *
     * @param commerciant the name of the commerciant involved in the transaction
     * @param amount      the amount of the transaction
     */
    public Transaction(final String commerciant, final double amount) {
        this.commerciant = commerciant;
        this.amount = amount;
    }
}
