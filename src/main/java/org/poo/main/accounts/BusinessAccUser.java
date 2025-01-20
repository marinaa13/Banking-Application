package org.poo.main.accounts;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Represents a user associated with a business account.
 * <p>
 * This class tracks the user's cards, transactions, and deposits within the business account.
 */
@Getter
@Setter
public class BusinessAccUser {
    private final String name;
    private List<String> cards;
    private Map<Integer, Transaction> transactions;
    private Map<Integer, Double> deposits;

    /**
     * Constructs a {@code BusinessAccUser} with the specified name.
     * Initializes empty lists and maps for cards, transactions, and deposits.
     *
     * @param name the name of the user
     */
    public BusinessAccUser(final String name) {
        this.name = name;
        cards = new ArrayList<>();
        transactions = new TreeMap<>();
        deposits = new TreeMap<>();
    }

    /**
     * Calculates the total amount spent by the user within the specified time range.
     *
     * @param startTime    the start timestamp of the range
     * @param endTimestamp the end timestamp of the range
     * @return the total amount spent within the time range
     */
    public double getSpentAmount(final int startTime, final int endTimestamp) {
        double spent = 0;
        for (Map.Entry<Integer, Transaction> entry : transactions.entrySet()) {
            if (entry.getKey() >= startTime && entry.getKey() <= endTimestamp) {
                spent += entry.getValue().getAmount();
            }
        }
        return spent;
    }

    /**
     * Calculates the total amount deposited by the user within the specified time range.
     *
     * @param startTime    the start timestamp of the range
     * @param endTimestamp the end timestamp of the range
     * @return the total amount deposited within the time range
     */
    public double getDepositAmount(final int startTime, final int endTimestamp) {
        double spent = 0;
        for (Map.Entry<Integer, Double> entry : deposits.entrySet()) {
            if (entry.getKey() >= startTime && entry.getKey() <= endTimestamp) {
                spent += entry.getValue();
            }
        }
        return spent;
    }

    /**
     * Calculates the total amount spent by the user at a specific commerciant
     * within the specified time range.
     *
     * @param startTimestamp the start timestamp of the range
     * @param endTimestamp   the end timestamp of the range
     * @param commerciant    the name of the commerciant
     * @return the total amount spent at the commerciant within the time range
     */
    public double getTotalSpentByCommerciant(final int startTimestamp, final int endTimestamp,
                                             final String commerciant) {
        double spent = 0;
        for (Map.Entry<Integer, Transaction> entry : transactions.entrySet()) {
            if (entry.getKey() >= startTimestamp && entry.getKey() <= endTimestamp) {
                if (entry.getValue().getCommerciant().equals(commerciant)) {
                    spent += entry.getValue().getAmount();
                }
            }
        }
        return spent;
    }

    /**
     * Generates a JSON representation of the user's activity within the specified time range.
     * <p>
     * Includes the user's name, total spent amount, and total deposited amount.
     *
     * @param startTimestamp the start timestamp of the range
     * @param endTimestamp   the end timestamp of the range
     * @return a JSON object representing the user's activity
     */
    public ObjectNode getJson(final int startTimestamp, final int endTimestamp) {
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        node.put("username", name);
        node.put("spent", getSpentAmount(startTimestamp, endTimestamp));
        node.put("deposited", getDepositAmount(startTimestamp, endTimestamp));
        return node;
    }

    /**
     * Adds a card to the user's list of cards.
     *
     * @param card the card number to add
     */
    public void addCard(final String card) {
        cards.add(card);
    }
}
