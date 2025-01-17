package org.poo.commands;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.fileio.CommandInput;
import org.poo.main.Application;
import org.poo.utils.Output;

public class BusinessReport implements Command {
    private final String type;
    private final int startTimestamp;
    private final int endTimestamp;
    private final String account;
    private final int timestamp;
    private final Application app;

    public BusinessReport(Application app, CommandInput input) {
        this.app = app;
        this.type = input.getType();
        this.startTimestamp = input.getStartTimestamp();
        this.endTimestamp = input.getEndTimestamp();
        this.account = input.getAccount();
        this.timestamp = input.getTimestamp();
    }

    @Override
    public ObjectNode execute() {
        ObjectNode inner = app.businessReport(type, startTimestamp, endTimestamp, account, timestamp);
        return Output.getCommand("businessReport", inner, timestamp);
    }
}
