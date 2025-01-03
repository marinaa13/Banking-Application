package org.poo.commands;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.fileio.CommandInput;
import org.poo.main.Application;

public class UpgradePlan implements Command {
    private final Application app;
    private final String account;
    private final String newPlanType;
    private int timestamp;

    public UpgradePlan(final Application app, final CommandInput input) {
        this.app = app;
        account = input.getAccount();
        newPlanType = input.getNewPlanType();
        timestamp = input.getTimestamp();
    }

    @Override
    public ObjectNode execute() {
        app.upgradePlan(account, newPlanType, timestamp);
        return null;
    }
}
