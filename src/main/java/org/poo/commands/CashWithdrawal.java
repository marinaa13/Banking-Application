package org.poo.commands;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.fileio.CommandInput;
import org.poo.main.Application;
import org.poo.utils.Output;

public class CashWithdrawal implements Command {
    private final Application app;
    private final String cardNumber;
    private final double amount;
    private final String email;
    private final String location;
    private final int timestamp;

    public CashWithdrawal(Application app, CommandInput input) {
        this.app = app;
        this.cardNumber = input.getCardNumber();
        this.amount = input.getAmount();
        this.email = input.getEmail();
        this.location = input.getLocation();
        this.timestamp = input.getTimestamp();
    }

    @Override
    public ObjectNode execute() {
        ObjectNode inner = app.cashWithdrawal(cardNumber, amount, email, location, timestamp);
        if (inner != null) {
            return Output.getCommand("cashWithdrawal", inner, timestamp);
        }
        return null;
    }
}
