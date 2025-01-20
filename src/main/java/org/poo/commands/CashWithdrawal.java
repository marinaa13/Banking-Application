package org.poo.commands;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.fileio.CommandInput;
import org.poo.main.Application;
import org.poo.utils.Output;

/**
 * Represents a command to perform a cash withdrawal from an account using a card.
 */
public class CashWithdrawal implements Command {

    private final Application app;
    private final String cardNumber;
    private final double amount;
    private final String email;
    private final String location;
    private final int timestamp;

    /**
     * Constructs a new {@code CashWithdrawal} command.
     *
     * @param app   the application instance to interact with
     * @param input the command input containing card details, amount,
     *              email, location, and timestamp
     */
    public CashWithdrawal(final Application app, final CommandInput input) {
        this.app = app;
        cardNumber = input.getCardNumber();
        amount = input.getAmount();
        email = input.getEmail();
        location = input.getLocation();
        timestamp = input.getTimestamp();
    }

    /**
     * Executes the cash withdrawal command.
     *
     * @return an {@link ObjectNode} containing the result of the withdrawal operation,
     *         or {@code null} if the operation was unsuccessful
     */
    @Override
    public ObjectNode execute() {
        ObjectNode inner = app.cashWithdrawal(cardNumber, amount, email, timestamp);
        if (inner != null) {
            return Output.getCommand("cashWithdrawal", inner, timestamp);
        }
        return null;
    }
}
