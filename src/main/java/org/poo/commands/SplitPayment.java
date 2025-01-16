package org.poo.commands;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.fileio.CommandInput;
import org.poo.main.Application;
import org.poo.utils.Utils;

import java.util.List;

/**
 * Represents a command that processes a split payment across multiple accounts.
 * <p>
 * This command splits a specified amount among a list of accounts and handles the transaction
 * by updating the account balances and generating the necessary reports.
 */
public class SplitPayment implements Command {
    private final Application app;
    private final String type;
    private final List<String> accounts;
    private final String currency;
    private final double amount;
    private final List<Double> amountForUsers;
    private final int timestamp;
    private final int id;
    /**
     * Constructs a {@link SplitPayment} command using the provided
     * {@link Application} and {@link CommandInput}.
     * Initializes the accounts, currency, amount, and timestamp based on the input.
     *
     * @param app the {@link Application} instance to interact with the application logic
     * @param input the {@link CommandInput} containing the data for the split payment
     */
    public SplitPayment(final Application app, final CommandInput input) {
        this.app = app;
        type = input.getSplitPaymentType();
        accounts = input.getAccounts();
        currency = input.getCurrency();
        amount = input.getAmount();
        amountForUsers = input.getAmountForUsers();
        timestamp = input.getTimestamp();
        id = Utils.generateSplitPayment();
    }

    /**
     * Executes the split payment command by calling the
     * {@link Application#splitPayment} method to process the payment across the specified accounts.
     * method to process the payment across the specified accounts.
     * <p>
     * The command does not return any specific result
     *
     * @return {@code null}, as the result of the command is handled within the application logic
     */
    @Override
    public ObjectNode execute() {
        app.splitPayment(type, accounts, currency, amount, amountForUsers, timestamp, id);
        return null;
    }
}
