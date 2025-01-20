package org.poo.commands;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.fileio.CommandInput;
import org.poo.main.Application;
import org.poo.utils.Output;

/**
 * Represents a command that allows a user to accept a split payment in the application.
 */
public class AcceptSplitPayment implements Command {

    private final Application app;
    private final String email;
    private final int timestamp;
    private final String type;

    /**
     * Constructs a new {@code AcceptSplitPayment} command.
     *
     * @param app   the application instance to interact with
     * @param input the command input containing details of the split payment
     */
    public AcceptSplitPayment(final Application app, final CommandInput input) {
        this.app = app;
        email = input.getEmail();
        timestamp = input.getTimestamp();
        type = input.getSplitPaymentType();
    }

    /**
     * Executes the accept split payment command.
     *
     * @return an {@link ObjectNode} containing the result of the command execution,
     *         or {@code null} if the operation was successful
     */
    @Override
    public ObjectNode execute() {
        ObjectNode inner = app.acceptSplitPayment(email, timestamp, type);
        if (inner != null) {
            return Output.getCommand("acceptSplitPayment", inner, timestamp);
        }
        return null;
    }
}
