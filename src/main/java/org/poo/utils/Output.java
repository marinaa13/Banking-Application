package org.poo.utils;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Utility class for generating JSON objects representing command outputs and specific responses.
 * <p>
 * This class cannot be instantiated as its constructor is private.
 */
public final class Output {
    private static final ObjectNode NODE = JsonNodeFactory.instance.objectNode();

    /**
     * Private constructor to prevent instantiation of the utility class.
     */
    private Output() {
    }

    /**
     * Generates a JSON object representing the output of a command.
     *
     * @param command   the name of the command
     * @param output    the command's output as an {@link ObjectNode}
     * @param timestamp the timestamp of the command execution
     * @return a JSON object containing the command details
     */
    public static ObjectNode getCommand(final String command, final ObjectNode output,
                                        final int timestamp) {
        NODE.put("command", command);
        NODE.set("output", output);
        NODE.put("timestamp", timestamp);
        return NODE;
    }

    /**
     * Generates a JSON object representing the output of a command.
     *
     * @param command   the name of the command
     * @param output    the command's output as an {@link ArrayNode}
     * @param timestamp the timestamp of the command execution
     * @return a JSON object containing the command details
     */
    public static ObjectNode getCommand(final String command, final ArrayNode output,
                                        final int timestamp) {
        NODE.put("command", command);
        NODE.set("output", output);
        NODE.put("timestamp", timestamp);
        return NODE;
    }

    /**
     * Generates a JSON object representing a successful plan upgrade.
     *
     * @param timestamp the timestamp of the upgrade
     * @param account   the IBAN of the account being upgraded
     * @return a JSON object containing the upgrade details
     */
    public static ObjectNode upgradePlan(final int timestamp, final String account) {
        NODE.put("timestamp", timestamp);
        NODE.put("description", "Upgrade plan");
        NODE.put("accountIBAN", account);
        NODE.put("newPlanType", "gold");
        return NODE;
    }
}
