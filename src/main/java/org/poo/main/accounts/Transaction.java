package org.poo.main.accounts;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class Transaction {
    private final String commerciant;
    private final double amount;

    public Transaction(String commerciant, double amount) {
        this.commerciant = commerciant;
        this.amount = amount;
    }
}
