package org.poo.commands;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.fileio.CommandInput;
import org.poo.main.Application;

public class WithdrawSavings implements Command {
    private final Application app;
    private final String account;
    private final double amount;
    private final String currency;
    private final int timestamp;

    public WithdrawSavings(Application app, CommandInput input) {
        this.app = app;
        this.account = input.getAccount();
        this.amount = input.getAmount();
        this.currency = input.getCurrency();
        this.timestamp = input.getTimestamp();
    }

    @Override
    public ObjectNode execute() {
        app.withdrawSavings(account, amount, currency, timestamp);
        return null;
    }
}
