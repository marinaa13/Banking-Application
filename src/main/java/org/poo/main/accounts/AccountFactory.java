package org.poo.main.accounts;

import org.poo.fileio.CommandInput;
import org.poo.main.Application;

public final class AccountFactory {
    // Singleton instance
    private static final AccountFactory INSTANCE = new AccountFactory();

    // Private constructor to prevent external instantiation
    private AccountFactory() {
    }

    /**
     * Returns the singleton instance of the factory.
     *
     * @return the singleton {@link AccountFactory} instance
     */
    public static AccountFactory getInstance() {
        return INSTANCE;
    }

    /**
     * Factory method to create an Account object based on the account type.
     *
     * @param accountType the type of account to create (e.g., "SAVINGS", "CLASSIC", "BUSINESS")
     * @return the created {@link Account} object, or {@code null} if the type is invalid
     */
    public Account getAccount(final String accountType, final CommandInput input,
                              final Application app) {
        if (accountType == null) {
            return null;
        }
        if (accountType.equalsIgnoreCase("SAVINGS")) {
            return new SavingsAccount(input);
        } else if (accountType.equalsIgnoreCase("CLASSIC")) {
            return new ClassicAccount(input);
        } else if (accountType.equalsIgnoreCase("BUSINESS")) {
            return new BusinessAccount(input, app);
        }
        return null;
    }
}
