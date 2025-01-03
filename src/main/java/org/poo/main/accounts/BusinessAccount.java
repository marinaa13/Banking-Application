package org.poo.main.accounts;

import org.poo.fileio.CommandInput;

public class BusinessAccount extends Account {
    /**
     * Constructs an {@link Account} using the provided {@link CommandInput}.
     * The account is initialized with a balance of 0, a minimum balance of 0
     * and the specified currency.
     * A unique IBAN is generated for the account.
     *
     * @param input the {@link CommandInput} containing the account data
     */
    public BusinessAccount(CommandInput input) {
        super(input);
    }
}
