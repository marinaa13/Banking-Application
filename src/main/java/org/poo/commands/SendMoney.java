package org.poo.commands;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.fileio.CommandInput;
import org.poo.main.Application;

/**
 * Represents a command to transfer money from one account to another.
 * <p>
 * This command processes the transaction by calling the
 * {@link Application#sendMoney(String, String, double, String, int)}
 * method to transfer the specified amount from the sender's account to the receiver's account.
 */
public class SendMoney implements Command {
    private final Application app;
    private final String sender;
    private final String receiver;
    private final double amount;
    private final String description;
    private final int timestamp;

    /**
     * Constructs a {@link SendMoney} command using the provided
     * {@link Application} and {@link CommandInput}.
     * Initializes the sender, receiver, amount, description, and timestamp based on the input.
     *
     * @param app the {@link Application} instance to interact with the application logic
     * @param input the {@link CommandInput} containing the transaction details
     */
    public SendMoney(final Application app, final CommandInput input) {
        this.app = app;
        sender = input.getAccount();
        receiver = input.getReceiver();
        amount = input.getAmount();
        description = input.getDescription();
        timestamp = input.getTimestamp();
    }

    /**
     * Executes the send money command by calling the
     * {@link Application#sendMoney(String, String, double, String, int)}
     * method to transfer the money between the sender and receiver accounts.
     * <p>
     * The command does not return any specific output,
     * as the transaction is handled by the application logic.
     *
     * @return {@code null}, as the result of the command is handled internally
     */
    @Override
    public ObjectNode execute() {
        ObjectNode inner = app.sendMoney(sender, receiver, amount, description, timestamp);
        if (inner != null) {
            ObjectNode node = JsonNodeFactory.instance.objectNode();
            node.put("command", "sendMoney");
            node.set("output", inner);
            node.put("timestamp", timestamp);
            return node;
        }
        return null;
    }
}
