package org.poo.utils;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Utility class that provides methods for generating JSON objects representing
 * various error messages related to different operations.
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
     * Creates a JSON object representing a "User not found" error with a timestamp.
     *
     * @param timestamp the timestamp when the error occurred
     * @return a JSON object containing the timestamp and the error description
     */
    public static ObjectNode userNotFound(final int timestamp) {
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        node.put("timestamp", timestamp);
        node.put("description", "User not found");
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

    /**
     * Creates a JSON object representing an "Invalid account for split" error with a timestamp.
     *
     * @param timestamp the timestamp when the error occurred
     * @return a JSON object containing the timestamp and the error description
     */
    public static ObjectNode invalidAccountForSplit(final int timestamp) {
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        node.put("timestamp", timestamp);
        node.put("description", "One of the accounts is invalid");
        return node;
    }

    /**
     * Creates a JSON object representing a "Commerciant not found" error with a timestamp.
     *
     * @param timestamp the timestamp when the error occurred
     * @return a JSON object containing the timestamp and the error description
     */
    public static ObjectNode commerciantNotFound(final int timestamp) {
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        node.put("timestamp", timestamp);
        node.put("description", "The commerciant is not found");
        return node;
    }

    /**
     * Creates a JSON object representing a "Change spending limit" error with a timestamp.
     *
     * @param timestamp the timestamp when the error occurred
     * @return a JSON object containing the timestamp and the error description
     */
    public static ObjectNode changeSpendLimitError(final int timestamp) {
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        node.put("timestamp", timestamp);
        node.put("description", "You must be owner in order to change spending limit.");
        return node;
    }

    /**
     * Creates a JSON object representing a "Change deposit limit" error with a timestamp.
     *
     * @param timestamp the timestamp when the error occurred
     * @return a JSON object containing the timestamp and the error description
     */
    public static ObjectNode changeDepLimitError(final int timestamp) {
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        node.put("timestamp", timestamp);
        node.put("description", "You must be owner in order to change deposit limit.");
        return node;
    }

    /**
     * Creates a JSON object representing a "Downgrade plan" error with a timestamp.
     *
     * @param timestamp the timestamp when the error occurred
     * @return a JSON object containing the timestamp and the error description
     */
    public static ObjectNode downgradePlan(final int timestamp) {
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        node.put("timestamp", timestamp);
        node.put("description", "You cannot downgrade your plan.");
        return node;
    }

    /**
     * Creates a JSON object representing an "Already owned plan" error with a timestamp.
     *
     * @param timestamp the timestamp when the error occurred
     * @param plan      the plan that is already owned
     * @return a JSON object containing the timestamp and the error description
     */
    public static ObjectNode alreadyOwnedPlan(final int timestamp, final String plan) {
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        node.put("timestamp", timestamp);
        node.put("description", "The user already has the " + plan + " plan.");
        return node;
    }

    /**
     * Creates a JSON object representing a "Not a business account" error with a timestamp.
     *
     * @param timestamp the timestamp when the error occurred
     * @return a JSON object containing the timestamp and the error description
     */
    public static ObjectNode notBusinessAccount(final int timestamp) {
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        node.put("timestamp", timestamp);
        node.put("description", "This is not a business account");
        return node;
    }
}
