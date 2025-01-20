package org.poo.commands;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.fileio.CommandInput;
import org.poo.main.Application;

/**
 * Represents a command to withdraw savings from a specific account.
 * <p>
 * This command interacts with the {@link Application} class to process the withdrawal
 * of savings in a specified currency and amount. The command returns {@code null}
 * on success or an error message in JSON format if the operation fails.
 */
public class WithdrawSavings implements Command {

    private final Application app;
    private final String account;
    private final double amount;
    private final String currency;
    private final int timestamp;

    /**
     * Constructs a {@code WithdrawSavings} command using the provided {@link Application}
     * and {@link CommandInput}.
     *
     * @param app   the {@link Application} instance to interact with
     * @param input the {@link CommandInput} containing the account, amount, currency,
     *              and timestamp details
     */
    public WithdrawSavings(final Application app, final CommandInput input) {
        this.app = app;
        this.account = input.getAccount();
        this.amount = input.getAmount();
        this.currency = input.getCurrency();
        this.timestamp = input.getTimestamp();
    }

    /**
     * Executes the withdrawal savings command by calling the
     * {@link Application#withdrawSavings(String, double, String, int)} method.
     * <p>
     * The method processes the withdrawal request. This command always returns {@code null},
     * as there is no additional output for successful operations.
     *
     * @return {@code null} as this command does not produce any output
     */
    @Override
    public ObjectNode execute() {
        app.withdrawSavings(account, amount, currency, timestamp);
        return null;
    }
}
