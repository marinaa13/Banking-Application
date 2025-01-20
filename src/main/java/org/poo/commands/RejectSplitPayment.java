package org.poo.commands;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.fileio.CommandInput;
import org.poo.main.Application;
import org.poo.utils.Output;

/**
 * Represents a command to reject a split payment request in the application.
 * <p>
 * This command interacts with the {@link Application} class to process the rejection
 * of a split payment request. The command returns {@code null} on success or an error message
 * in JSON format if the operation fails.
 */
public class RejectSplitPayment implements Command {

    private final Application app;
    private final String email;
    private final int timestamp;
    private final String type;

    /**
     * Constructs a {@code RejectSplitPayment} command using the provided
     * {@link Application} and {@link CommandInput}.
     *
     * @param app   the {@link Application} instance to interact with
     * @param input the {@link CommandInput} containing email, timestamp, and payment type
     */
    public RejectSplitPayment(final Application app, final CommandInput input) {
        this.app = app;
        email = input.getEmail();
        timestamp = input.getTimestamp();
        type = input.getSplitPaymentType();
    }

    /**
     * Executes the reject split payment command by calling the
     * {@link Application#rejectSplitPayment(String, int, String)} method.
     * <p>
     * If the operation is successful, {@code null} is returned.
     * If the operation fails, an {@link ObjectNode} containing the error details is returned.
     *
     */
    @Override
    public ObjectNode execute() {
        ObjectNode inner = app.rejectSplitPayment(email, timestamp, type);
        if (inner != null) {
            return Output.getCommand("rejectSplitPayment", inner, timestamp);
        }
        return null;
    }
}
