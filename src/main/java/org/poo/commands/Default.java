package org.poo.commands;

import com.fasterxml.jackson.databind.node.ObjectNode;

public class Default implements Command{
    @Override
    public ObjectNode execute() {
        return null;
    }
}
