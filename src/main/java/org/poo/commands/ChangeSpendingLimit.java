package org.poo.commands;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.fileio.CommandInput;
import org.poo.main.Application;
import org.poo.utils.Output;

public class ChangeSpendingLimit implements Command {
    private final String email;
    private final String account;
    private final double amount;
    private final int timestamp;
    private final Application app;

    public ChangeSpendingLimit(Application app, CommandInput input) {
        this.app = app;
        this.email = input.getEmail();
        this.account = input.getAccount();
        this.amount = input.getAmount();
        this.timestamp = input.getTimestamp();
    }

    @Override
    public ObjectNode execute() {
        ObjectNode inner = app.changeSpendingLimit(email, account, amount, timestamp);
        if (inner != null)
            return Output.getCommand("changeSpendingLimit", inner, timestamp);
        return null;
    }
}
