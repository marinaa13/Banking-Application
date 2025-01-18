package org.poo.main.accounts;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Getter @Setter
public class BusinessAccUser {
    private final String name;
    private List<String> cards;
    private Map<Integer, Transaction> transactions;
    private Map<Integer, Double> deposits;

    public BusinessAccUser(String name) {
        this.name = name;
        cards = new ArrayList<>();
        transactions = new TreeMap<>();
        deposits = new TreeMap<>();
    }

    // total, indiferent de commerciant
    public double getSpentAmount(int startTime, int endTimestamp) {
        double spent = 0;
        for (Map.Entry<Integer, Transaction> entry : transactions.entrySet()) {
            if (entry.getKey() >= startTime && entry.getKey() <= endTimestamp) {
                spent += entry.getValue().getAmount();
            }
        }
        return spent;
    }

    public double getDepositAmount(int startTime, int endTimestamp) {
        double spent = 0;
        for (Map.Entry<Integer, Double> entry : deposits.entrySet()) {
            if (entry.getKey() >= startTime && entry.getKey() <= endTimestamp) {
                spent += entry.getValue();
            }
        }
        return spent;
    }

    public double getTotalSpentByCommerciant(final int startTimestamp, final int endTimestamp, final String commerciant) {
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

    public ObjectNode getJson(int startTimestamp, int endTimestamp) {
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        node.put("username", name);
        node.put("spent", getSpentAmount(startTimestamp, endTimestamp));
        node.put("deposited", getDepositAmount(startTimestamp, endTimestamp));
        return node;
    }

    public void addCard(String card) {
        cards.add(card);
    }
}
