package org.poo.commands;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.Setter;
import org.poo.fileio.CommandInput;
import org.poo.main.Application;
import org.poo.utils.Errors;
import org.poo.utils.Output;

/**
 * Represents a command to generate a spendings report for a specific account
 * within a given time range.
 * <p>
 * This command retrieves the spendings report for an account from the application and generates
 * a response in JSON format, including the requested data or an error if the account is not found.
 */
@Getter @Setter
public class SpendingsReport implements Command {
    private Application app;
    private int startTimestamp;
    private int endTimestamp;
    private int timestamp;
    private String account;

    /**
     * Constructs a {@link SpendingsReport} command using the provided
     * {@link Application} and {@link CommandInput}.
     * Initializes the start and end timestamps, timestamp, and account based on the input.
     *
     * @param app the {@link Application} instance to interact with the application logic
     * @param input the {@link CommandInput} containing the data for the spendings report request
     */
    public SpendingsReport(final Application app, final CommandInput input) {
        this.app = app;
        startTimestamp = input.getStartTimestamp();
        endTimestamp = input.getEndTimestamp();
        timestamp = input.getTimestamp();
        account = input.getAccount();
    }

    /**
     * Executes the spendings report command by calling the
     * {@link Application#getSpendingsReport(String, int, int, int)} method to retrieve
     * the spendings report for the specified account within the given time range.
     * <p>
     * If the account is found, the report is returned;
     * otherwise, an error message is returned in JSON format.
     *
     * @return an {@link ObjectNode} containing the result of the command
     */
    @Override
    public ObjectNode execute() {
        ObjectNode inner = app.getSpendingsReport(account, startTimestamp, endTimestamp, timestamp);
        return Output.getCommand("spendingsReport", inner, timestamp);
    }
}
