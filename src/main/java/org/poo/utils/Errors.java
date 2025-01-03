package org.poo.utils;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Utility class that provides methods for generating JSON objects representing
 * different types of error messages related to different operations.
 * <p>
 * This class cannot be instantiated as its constructor is private.
 */
public final class Errors {

    /**
     * Private constructor to prevent instantiation of the utility class.
     */
    private Errors() {
    }

    /**
     * Creates a JSON object representing an "Insufficient funds" error with a timestamp.
     *
     * @param timestamp the timestamp when the error occurred
     * @return a JSON object containing the timestamp and the error description
     */
    public static ObjectNode insufficientFunds(final int timestamp) {
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        node.put("timestamp", timestamp);
        node.put("description", "Insufficient funds");
        return node;
    }

    /**
     * Creates a JSON object representing an "Account not found" error with a timestamp.
     *
     * @param timestamp the timestamp when the error occurred
     * @return a JSON object containing the timestamp and the error description
     */
    public static ObjectNode accountNotFound(final int timestamp) {
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        node.put("timestamp", timestamp);
        node.put("description", "Account not found");
        return node;
    }

    /**
     * Creates a JSON object representing a "Card not found" error with a timestamp.
     *
     * @param timestamp the timestamp when the error occurred
     * @return a JSON object containing the timestamp and the error description
     */
    public static ObjectNode cardNotFound(final int timestamp) {
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        node.put("timestamp", timestamp);
        node.put("description", "Card not found");
        return node;
    }

    /**
     * Creates a JSON object representing a "Frozen card" error with a timestamp.
     *
     * @param timestamp the timestamp when the error occurred
     * @return a JSON object containing the timestamp and the error description
     */
    public static ObjectNode frozenCard(final int timestamp) {
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        node.put("timestamp", timestamp);
        node.put("description", "The card is frozen");
        return node;
    }

    /**
     * Creates a JSON object representing a "Not a savings account" error with a timestamp.
     *
     * @param timestamp the timestamp when the error occurred
     * @return a JSON object containing the timestamp and the error description
     */
    public static ObjectNode notSavingsAccount(final int timestamp) {
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        node.put("timestamp", timestamp);
        node.put("description", "This is not a savings account");
        return node;
    }

    /**
     * Creates a JSON object representing a "Funds remaining" error with a timestamp.
     *
     * @param timestamp the timestamp when the error occurred
     * @return a JSON object containing the timestamp and the error description
     */
    public static ObjectNode fundsRemaining(final int timestamp) {
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        node.put("timestamp", timestamp);
        node.put("description", "Account couldn't be deleted - there are funds remaining");
        return node;
    }
}
