package org.poo.utils;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

public final class Output {
    private static final ObjectNode node = JsonNodeFactory.instance.objectNode();
    private Output() {
    }

    public static ObjectNode getCommand(String command, ObjectNode output, int timestamp) {
        node.put("command", command);
        node.set("output", output);
        node.put("timestamp", timestamp);
        return node;
    }

    public static ObjectNode getCommand(String command, ArrayNode output, int timestamp) {
        node.put("command", command);
        node.set("output", output);
        node.put("timestamp", timestamp);
        return node;
    }

    public static ObjectNode upgradePlan(final int timestamp, final String account) {
        node.put("timestamp", timestamp);
        node.put("description", "Upgrade plan");
        node.put("accountIBAN", account);
        node.put("newPlanType", "gold");
        return node;
    }
}
