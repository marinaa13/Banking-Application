package org.poo.commands;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.fileio.CommandInput;
import org.poo.main.Application;

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
            ObjectNode node = JsonNodeFactory.instance.objectNode();
            node.put("command", "cashWithdrawal");
            node.set("output", inner);
            node.put("timestamp", timestamp);
            return node;
        }
        return null;
    }
}
