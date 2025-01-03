package org.poo.main;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.Setter;
import org.poo.fileio.CommandInput;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import org.poo.utils.Errors;
import org.poo.utils.Utils;

/**
 * Represents a bank account with associated details like IBAN, balance, currency, and cards.
 * <p>
 * This class includes methods for managing the account information.
 */
@Getter @Setter
public class Account {
    private final String iban;
    private String alias = " ";
    private double balance;
    private double minBalance;
    private String currency;
    private List<Card> cards;
    private TreeMap<Integer, ObjectNode> report;
    private TreeMap<Integer, ObjectNode> spendingsReport;
    private User owner;

    /**
     * Constructs an {@link Account} using the provided {@link CommandInput}.
     * The account is initialized with a balance of 0, a minimum balance of 0
     * and the specified currency.
     * A unique IBAN is generated for the account.
     *
     * @param input the {@link CommandInput} containing the account data
     */
    public Account(final CommandInput input) {
        this.balance = 0;
        this.minBalance = 0;
        this.currency = input.getCurrency();
        cards = new ArrayList<>();
        report = new TreeMap<>();
        spendingsReport = new TreeMap<>();
        iban = Utils.generateIBAN();
    }

    /**
     * Converts the account to a JSON object.
     * The resulting JSON includes the IBAN, balance, currency, account type, and associated cards.
     *
     * @return a JSON representation of the account
     */
    public ObjectNode getJson() {
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        node.put("IBAN", iban);
        node.put("balance", balance);
        node.put("currency", currency);
        node.put("type", "classic");

        ArrayNode array = JsonNodeFactory.instance.arrayNode();
        for (Card card : cards) {
            array.add(card.getJson());
        }
        node.set("cards", array);
        return node;
    }

    /**
     * Adds a new card to the account.
     *
     * @param card the card to be added to the account
     */
    public void addCard(final Card card) {
        cards.add(card);
        card.setAccountBelonging(this);
    }

    /**
     * Retrieves a card from the account by its card number.
     *
     * @param cardNumber the card number of the card to retrieve
     * @return the {@link Card} with the specified card number, or {@code null} if not found
     */
    public Card getCardByNumber(final String cardNumber) {
        for (Card card : cards) {
            if (card.getCardNumber().equals(cardNumber)) {
                return card;
            }
        }
        return null;
    }

    /**
     * Makes a payment from the account using the specified card.
     * The payment amount is converted to the account's currency using the provided exchange rates.
     * If the card is frozen or there are insufficient funds, an error is returned.
     *
     * @param card the card used for the payment
     * @param amount the amount to be paid
     * @param payCurrency the currency of the payment
     * @param exchangeRates the exchange rates used for payCurrency conversion
     * @param timestamp the timestamp of the payment
     * @param commerciant the merchant name for the payment
     * @return an {@link ObjectNode} containing the payment result, or an error if payment fails
     */
    public ObjectNode makePayment(final Card card, double amount, final String payCurrency,
                                  final ExchangeRatesGraph exchangeRates,
                                  final int timestamp, final String commerciant) {
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        if (card.getStatus().equals("frozen")) {
            return Errors.frozenCard(timestamp);
        }
        amount *= exchangeRates.getRate(payCurrency, card.getAccountBelonging().getCurrency());

        if (balance < amount) {
            return Errors.insufficientFunds(timestamp);
        }

        balance -= amount;
        node.put("timestamp", timestamp);
        node.put("description", "Card payment");
        node.put("amount", amount);
        node.put("commerciant", commerciant);

        card.getAccountBelonging().addToReport(node);
        card.getAccountBelonging().addToSpendingsReport(node);

        return node;
    }

    /**
     * Sends money from the account to another account, deducting the amount from the balance.
     * A transaction is recorded in the account's report.
     *
     * @param toAccount the IBAN of the recipient account
     * @param amount the amount to send
     * @param description a description for the transaction
     * @param timestamp the timestamp of the transaction
     */
    public void sendMoney(final String toAccount, final double amount,
                          final String description, final int timestamp) {
        balance -= amount;
        ObjectNode node = addTransaction(iban, toAccount, amount, description, timestamp);
        owner.getCommandHistory().addToHistory(node);
        addToReport(node);
    }

    /**
     * Receives money from another account, adding the amount to the balance.
     * A transaction is recorded in the account's report.
     *
     * @param fromAccount the IBAN of the sender account
     * @param amount the amount to receive
     * @param description a description for the transaction
     * @param timestamp the timestamp of the transaction
     */
    public void receiveMoney(final String fromAccount, final double amount,
                             final String description, final int timestamp) {
        balance += amount;
        ObjectNode node = addTransaction(fromAccount, iban, amount, description, timestamp);
        owner.getCommandHistory().addToHistory(node);
        addToReport(node);
    }

    /**
     * Creates a transaction object for a transfer from one account to another.
     *
     * @param fromAccount the IBAN of the sender account
     * @param toAccount the IBAN of the recipient account
     * @param amount the transaction amount
     * @param description a description for the transaction
     * @param timestamp the timestamp of the transaction
     * @return an {@link ObjectNode} representing the transaction
     */
    public ObjectNode addTransaction(final String fromAccount, final String toAccount,
                                     final double amount, final String description,
                                     final int timestamp) {
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        node.put("timestamp", timestamp);
        node.put("description", description);
        node.put("senderIBAN", fromAccount);
        node.put("receiverIBAN", toAccount);
        String amountStr = amount + " " + currency;
        node.put("amount", amountStr);
        if (iban.equals(fromAccount)) {
            node.put("transferType", "sent");
        } else {
            node.put("transferType", "received");
        }
        return node;
    }

    /**
     * Creates a split payment transaction across multiple accounts.
     *
     * @param array a list of accounts involved in the split payment
     * @param payCurrency the currency of the split payment
     * @param amount the total amount of the payment
     * @param timestamp the timestamp of the transaction
     * @return an {@link ObjectNode} representing the split transaction
     */
    public ObjectNode addSplitTransaction(final ArrayNode array, final String payCurrency,
                                          final double amount, final int timestamp) {
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        node.put("timestamp", timestamp);
        String formatted = String.format("%.2f", amount);
        node.put("description", "Split payment of " + formatted + " " + payCurrency);
        node.put("currency", payCurrency);
        node.put("amount", amount / array.size());
        node.set("involvedAccounts", array);
        return node;
    }

    /**
     * Sets the interest rate for the account.
     * <p>
     * This operation is not supported for this account type and throws an exception.
     */
    public void setInterestRate(final double interestRate) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Adds interest to the account.
     * <p>
     * This operation is not supported for this account type and throws an exception.
     */
    public void addInterest() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Retrieves the transaction history for the account within a specified time range.
     *
     * @param startTimestamp the start timestamp of the range
     * @param endTimestamp the end timestamp of the range
     * @return an {@link ArrayNode} containing the transactions within the time range
     */
    public ArrayNode getReport(final int startTimestamp, final int endTimestamp) {
        ArrayNode array = JsonNodeFactory.instance.arrayNode();
        for (ObjectNode reportNode : report.values()) {
            int timestamp = reportNode.get("timestamp").asInt();
            if (timestamp >= startTimestamp && timestamp <= endTimestamp) {
                array.add(reportNode);
            }
        }
        return array;
    }

    /**
     * Adds a transaction to the account's report.
     *
     * @param node the transaction to be added
     */
    public void addToReport(final ObjectNode node) {
        report.put(node.get("timestamp").asInt(), node);
    }

    /**
     * Retrieves the spending report for the account within a specified time range.
     *
     * @param startTimestamp the start timestamp of the range
     * @param endTimestamp the end timestamp of the range
     * @return an {@link ArrayNode} containing the spending transactions within the time range
     */
    public ArrayNode getSpendingsReport(final int startTimestamp, final int endTimestamp) {
        ArrayNode array = JsonNodeFactory.instance.arrayNode();
        for (ObjectNode reportNode : spendingsReport.values()) {
            int timestamp = reportNode.get("timestamp").asInt();
            if (timestamp >= startTimestamp && timestamp <= endTimestamp) {
                array.add(reportNode);
            }
        }
        return array;
    }

    /**
     * Adds a spending transaction to the account's spendings report.
     *
     * @param node the transaction to be added
     */
    public void addToSpendingsReport(final ObjectNode node) {
        spendingsReport.put(node.get("timestamp").asInt(), node);
    }

    /**
     * Retrieves the commerciants involved in the account's spending within a specified time range.
     *
     * @param startTimestamp the start timestamp of the range
     * @param endTimestamp the end timestamp of the range
     * @return an {@link ArrayNode} containing the commerciants and their total spending
     */
    public ArrayNode getCommerciants(final int startTimestamp, final int endTimestamp) {
        ArrayNode array = JsonNodeFactory.instance.arrayNode();
        TreeMap<String, Double> commerchants = new TreeMap<>();
        for (ObjectNode reportNode : spendingsReport.values()) {
            int timestamp = reportNode.get("timestamp").asInt();
            if (timestamp >= startTimestamp && timestamp <= endTimestamp) {
                String commerciant = reportNode.get("commerciant").asText();
                double amount = reportNode.get("amount").asDouble();
                commerchants.put(commerciant, commerchants.getOrDefault(commerciant, 0.0)
                        + amount);
            }
        }
        for (String commerciant : commerchants.keySet()) {
            ObjectNode node = JsonNodeFactory.instance.objectNode();
            node.put("total", commerchants.get(commerciant));
            node.put("commerciant", commerciant);
            array.add(node);
        }
        return array;
    }

    /**
     * Deletes a card identified by its card number from the account.
     * A transaction is recorded for the card deletion.
     *
     * @param cardNumber the card number of the card to delete
     * @param timestamp the timestamp of the deletion
     */
    public void deleteCard(final String cardNumber, final int timestamp) {
        Card card = getCardByNumber(cardNumber);
        if (card == null) {
            return;
        }
        cards.remove(card);
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        node.put("timestamp", timestamp);
        node.put("description", "The card has been destroyed");
        node.put("card", cardNumber);
        node.put("cardHolder", getOwner().getEmail());
        node.put("account", getIban());
        getOwner().getCommandHistory().addToHistory(node);
    }
}
