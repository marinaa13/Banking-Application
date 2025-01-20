package org.poo.commands;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.fileio.CommandInput;
import org.poo.main.Application;
import org.poo.utils.Output;

/**
 * Represents a command to generate a business report for a specific account
 * within a given time range.
 */
public class BusinessReport implements Command {

    private final String type;
    private final int startTimestamp;
    private final int endTimestamp;
    private final String account;
    private final int timestamp;
    private final Application app;

    /**
     * Constructs a new {@code BusinessReport} command.
     *
     * @param app   the application instance to interact with
     * @param input the command input containing report type, timestamps, and account details
     */
    public BusinessReport(final Application app, final CommandInput input) {
        this.app = app;
        type = input.getType();
        startTimestamp = input.getStartTimestamp();
        endTimestamp = input.getEndTimestamp();
        account = input.getAccount();
        timestamp = input.getTimestamp();
    }

    /**
     * Executes the business report command.
     *
     * @return an {@link ObjectNode} containing the generated business report details
     */
    @Override
    public ObjectNode execute() {
        ObjectNode inner = app.businessReport(type, startTimestamp, endTimestamp,
                                                account, timestamp);
        return Output.getCommand("businessReport", inner, timestamp);
    }
}
