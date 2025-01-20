package org.poo.commands;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.Setter;
import org.poo.fileio.CommandInput;
import org.poo.main.Application;
import org.poo.utils.Output;

/**
 * Represents a command that generates a report for a specific account within a given time range.
 * <p>
 * This command retrieves the transaction report for an account and generates a response
 * in JSON format, including the requested data or an error if the account is not found.
 */
@Getter @Setter
public class Report implements Command {
    private Application app;
    private String account;
    private int startTimestamp;
    private int endTimestamp;
    private int timestamp;

    /**
     * Constructs a {@link Report} command using the provided
     * {@link Application} and {@link CommandInput}.
     * Initializes the account, start and end timestamps, and timestamp based on the input.
     *
     * @param app the {@link Application} instance to interact with the application logic
     * @param input the {@link CommandInput} containing the data for the report request
     */
    public Report(final Application app, final CommandInput input) {
        this.app = app;
        account = input.getAccount();
        startTimestamp = input.getStartTimestamp();
        endTimestamp = input.getEndTimestamp();
        timestamp = input.getTimestamp();
    }

    /**
     * Executes the report command by calling the
     * {@link Application#getReport(String, int, int, int)} method
     * to retrieve the transaction report
     * for the specified account within the given time range.
     * <p>
     * If the account is found, the report is returned; otherwise, an error message is returned.
     *
     * @return an {@link ObjectNode} containing the result of the command
     */
    @Override
    public ObjectNode execute() {
        ObjectNode inner = app.getReport(account, startTimestamp, endTimestamp, timestamp);
        return Output.getCommand("report", inner, timestamp);
    }
}
