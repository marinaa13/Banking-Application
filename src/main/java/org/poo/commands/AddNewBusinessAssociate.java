package org.poo.commands;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.fileio.CommandInput;
import org.poo.main.Application;

public class AddNewBusinessAssociate implements Command {
    private final String account;
    private final String role;
    private final String email;
    private final int timestamp;
    private final Application app;

    public AddNewBusinessAssociate(Application app, CommandInput input) {
        account = input.getAccount();
        role = input.getRole();
        email = input.getEmail();
        timestamp = input.getTimestamp();
        this.app = app;
    }

    @Override
    public ObjectNode execute() {
        app.addNewBussinessAssociate(account, role, email, timestamp);
        return null;
    }
}
