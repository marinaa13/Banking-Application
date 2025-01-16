package org.poo.commands;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.fileio.CommandInput;
import org.poo.main.Application;
import org.poo.utils.Output;

public class RejectSplitPayment implements Command {
    private final Application app;
    private final String email;
    private final int timestamp;

    public RejectSplitPayment(Application app, CommandInput input) {
        this.app = app;
        email = input.getEmail();
        timestamp = input.getTimestamp();
    }

    @Override
    public ObjectNode execute() {
        ObjectNode inner = app.rejectSplitPayment(email, timestamp);
        if (inner != null) {
            return Output.getCommand("rejectSplitPayment", inner, timestamp);
        }
        return null;
    }
}
