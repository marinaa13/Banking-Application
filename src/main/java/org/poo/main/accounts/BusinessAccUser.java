package org.poo.main.accounts;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter @Setter
public class BusinessAccUser {
    private final String name;
    private double spentAmount;
    private double depositAmount;
    private List<String> cards;

    public BusinessAccUser(String name) {
        this.name = name;
        this.spentAmount = 0;
        this.depositAmount = 0;
        cards = new ArrayList<>();
    }

    public ObjectNode getJson() {
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        node.put("username", name);
        node.put("spent", spentAmount);
        node.put("deposited", depositAmount);
        return node;
    }

    public void addCard(String card) {
        cards.add(card);
    }
}
