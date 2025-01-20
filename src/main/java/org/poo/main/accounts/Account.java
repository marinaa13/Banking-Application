package org.poo.main.accounts;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.Setter;
import org.poo.fileio.CommandInput;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import org.poo.main.Application;
import org.poo.main.Commerciant;
import org.poo.main.ServicePlan;
import org.poo.main.cardTypes.Card;
import org.poo.main.ExchangeRatesGraph;
import org.poo.main.CashbackService;
import org.poo.main.User;
import org.poo.utils.Errors;
import org.poo.utils.Utils;

/**
 * Represents a bank account with associated details like IBAN, balance, currency, and cards.
 * <p>
 * This class includes methods for managing the account information.
 */
@Getter
@Setter
public abstract class Account {
    private final String iban;
    private String alias = " ";
    private double balance;
    private double minBalance;
    private String currency;
    private List<Card> cards;
    private TreeMap<Integer, ObjectNode> report;
    private TreeMap<Integer, ObjectNode> spendingsReport;
    private CashbackService cashbackService;
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
     * It is overridden by subclasses to include additional fields.
     *
     * @return a JSON representation of the account
     */
    public ObjectNode getJson() {
        return null;
    }

    /**
     * Adds a new card to the account.
     *
     * @param card the card to be added to the account
     */
    public void addCard(final Card card, final String email) {
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
     * It checks for commission and cashback, and updates the account balance accordingly.
     *
     * @param card          the card used for the payment
     * @param amount        the amount to be paid
     * @param payCurrency   the currency of the payment
     * @param exchangeRates the exchange rates used for payCurrency conversion
     * @param timestamp     the timestamp of the payment
     * @param commerciant   the merchant name for the payment
     * @return an {@link ObjectNode} containing the payment result, or an error if payment fails
     */
    public ObjectNode makePayment(final Card card, double amount, final String payCurrency,
                                  final ExchangeRatesGraph exchangeRates, final int timestamp,
                                  final String commerciant, final String email) {
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        if (card.getStatus().equals("frozen")) {
            return Errors.frozenCard(timestamp);
        }

        if (!card.getAccountBelonging().getOwner().getEmail().equals(email)) {
            return Errors.cardNotFound(timestamp);
        }

        amount *= exchangeRates.getRate(payCurrency, currency);
        double ronAmount = amount * exchangeRates.getRate(currency, Utils.DEFAULT_CURRENCY);
        double newAmount = amount * getOwner().getCommission(ronAmount);

        if (balance < newAmount) {
            return Errors.insufficientFunds(timestamp);
        }

        double cashback = cashbackService.giveCashbackForTransactions(commerciant, amount);

        cashbackService.addTransactionToCommerciant(commerciant, ronAmount);

        double newCashback = cashbackService.giveCashbackForAmount(commerciant,
                ronAmount - cashback, getOwner().getPlan());
        newCashback = newCashback * exchangeRates.getRate(Utils.DEFAULT_CURRENCY, currency);
        cashback += newCashback;

        balance = Utils.bigDecimalPrecision(balance, newAmount, cashback);
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
     * @param toAccount   the IBAN of the recipient account
     * @param amount      the amount to send
     * @param commission  the commission to be deducted from the amount
     * @param description a description for the transaction
     * @param timestamp   the timestamp of the transaction
     */
    public void sendMoney(final String toAccount, final double amount, final double commission,
                          final String description, final int timestamp) {
        balance -= amount * commission;
        ObjectNode node = addTransaction(iban, toAccount, amount, description, timestamp);
        if (owner != null) {
            owner.getCommandHistory().addToHistory(node);
        }
        addToReport(node);
    }

    /**
     * Receives money from another account, adding the amount to the balance.
     * A transaction is recorded in the account's report.
     *
     * @param fromAccount the IBAN of the sender account
     * @param amount      the amount to receive
     * @param description a description for the transaction
     * @param timestamp   the timestamp of the transaction
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
     * @param toAccount   the IBAN of the recipient account
     * @param amount      the transaction amount
     * @param description a description for the transaction
     * @param timestamp   the timestamp of the transaction
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
     * @param array       a list of accounts involved in the split payment
     * @param payCurrency the currency of the split payment
     * @param amount      the total amount of the payment
     * @param timestamp   the timestamp of the transaction
     * @return an {@link ObjectNode} representing the split transaction
     */
    public ObjectNode addSplitTransaction(final ArrayNode array, final String payCurrency,
                                          final double amount, final int timestamp,
                                          final ArrayNode amountsArray, final String type) {
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        if (type.equals("custom")) {
            node.set("amountForUsers", amountsArray);
        } else {
            node.put("amount", amountsArray.get(0).asDouble());
        }
        node.put("currency", payCurrency);
        String formatted = String.format("%.2f", amount);
        node.put("description", "Split payment of " + formatted + " " + payCurrency);
        node.set("involvedAccounts", array);
        node.put("splitPaymentType", type);
        node.put("timestamp", timestamp);
        return node;
    }

    /**
     * Sets the interest rate for the account.
     * <p>
     * It is overridden by subclasses to set the interest rate for the specific account type.
     */
    public void setInterestRate(final double interestRate) {
    }

    /**
     * Adds interest to the account.
     * <p>
     * It is overridden by subclasses to add interest to the specific account type.
     */
    public ObjectNode addInterest() {
        return null;
    }

    /**
     * Retrieves the transaction history for the account within a specified time range.
     *
     * @param startTimestamp the start timestamp of the range
     * @param endTimestamp   the end timestamp of the range
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
     * @param endTimestamp   the end timestamp of the range
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
     * @param endTimestamp   the end timestamp of the range
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
                commerchants.put(commerciant, commerchants.getOrDefault(commerciant, 0.0) + amount);
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
     * @param timestamp  the timestamp of the deletion
     * @param email      the email of the account owner
     */
    public void deleteCard(final String cardNumber, final int timestamp, final String email) {
        Card card = getCardByNumber(cardNumber);
        if (card == null || !getOwner().getEmail().equals(email)) {
            return;
        }
        if (balance > 0) {
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

    /**
     * Checks if the account is a savings account.
     *
     * @return {@code true} if the account is a savings account, {@code false} otherwise
     */
    public boolean isSavingsAccount() {
        return false;
    }

    /**
     * Checks if the account is a classic account.
     *
     * @return {@code true} if the account is a classic account, {@code false} otherwise
     */
    public boolean isClassicAccount() {
        return false;
    }

    /**
     * Checks if the account is a business account.
     *
     * @return {@code true} if the account is a current account, {@code false} otherwise
     */
    public boolean isBusinessAccount() {
        return false;
    }

    /**
     * Deducts a fee from the account balance.
     * If the account balance is insufficient, an exception is thrown.
     */
    public void deductFee(final double amount) throws Exception {
        if (balance < amount) {
            throw new Exception("Insufficient funds");
        } else {
            balance -= amount;
        }
    }

    /**
     * Handles a cash withdrawal transaction from the account using the specified card.
     * <p>
     * If the card is invalid, frozen, or insufficient funds are detected, an error is returned.
     *
     * @param card          the {@link Card} used for the withdrawal
     * @param amount        the amount to withdraw
     * @param timestamp     the timestamp of the withdrawal transaction
     * @param exchangeRates the {@link ExchangeRatesGraph} used for currency conversion
     * @return {@code null} if the operation is successful, or an {@link ObjectNode} containing
     *         error details if the operation fails
     */
    public ObjectNode cashWithdrawal(final Card card, final double amount, final int timestamp,
                                     final ExchangeRatesGraph exchangeRates) {
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        if (card == null) {
            return Errors.cardNotFound(timestamp);
        }
        if (card.getStatus().equals("frozen")) {
            return Errors.frozenCard(timestamp);
        }

        double newAmount = amount * getOwner().getCommission(amount)
                * exchangeRates.getRate(Utils.DEFAULT_CURRENCY,
                card.getAccountBelonging().getCurrency());

        if (balance < newAmount) {
            return Errors.insufficientFunds(timestamp);
        }

        balance -= newAmount;
        node.put("timestamp", timestamp);
        node.put("description", "Cash withdrawal of " + amount);
        node.put("amount", amount);

        card.getAccountBelonging().getOwner().getCommandHistory().addToHistory(node);
        card.getAccountBelonging().addToReport(node);
        return null;
    }

    /**
     * Sends money to a commerciant, applying commissions and cashback.
     *
     * @param amount        the amount to send
     * @param exchangeRates the exchange rates for currency conversion
     * @param comm          the {@link Commerciant} receiving the money
     * @param description   the transaction description
     * @param timestamp     the transaction timestamp
     */
    public void sendMoneyToCommerciant(final double amount, final ExchangeRatesGraph exchangeRates,
                                       final Commerciant comm, final String description,
                                       final int timestamp) {
        double ronAmount = amount * exchangeRates.getRate(currency, Utils.DEFAULT_CURRENCY);
        double commission = getOwner().getCommission(ronAmount);
        double newAmount = amount * commission;

        if (balance < newAmount) {
            ObjectNode node = Errors.insufficientFunds(timestamp);
            getOwner().getCommandHistory().addToHistory(node);
            addToReport(node);
            return;
        }

        double cashback = cashbackService.giveCashbackForTransactions(comm.getName(), amount);

        cashbackService.addTransactionToCommerciant(comm.getName(), ronAmount);

        double newCashback = cashbackService.giveCashbackForAmount(comm.getName(),
                ronAmount - cashback, getOwner().getPlan());
        newCashback = newCashback * exchangeRates.getRate(Utils.DEFAULT_CURRENCY, currency);
        cashback += newCashback;

        balance -= (newAmount - cashback);
        ObjectNode node = addTransaction(iban, comm.getAccount(), amount, description, timestamp);

        if (owner != null) {
            owner.getCommandHistory().addToHistory(node);
        }
        addToReport(node);
    }

    /**
     * Checks if the account qualifies for an upgrade to the Gold plan.
     *
     * @param amount        the transaction amount to evaluate
     * @param exchangeRates the exchange rates for currency conversion
     * @param timestamp     the transaction timestamp
     * @return an {@link ObjectNode} with plan upgrade details if conditions are met,
     *         or {@code null} if no upgrade is performed
     */
    public ObjectNode checkForGold(final double amount, final ExchangeRatesGraph exchangeRates,
                                   final int timestamp) {
        double ronAmount = amount * exchangeRates.getRate(currency, Utils.DEFAULT_CURRENCY);
        if (ronAmount > Utils.THRESHOLD_300) {
            if (getOwner().getPlan() == ServicePlan.SILVER) {
                getOwner().setNumPayments(getOwner().getNumPayments() + 1);
                if (getOwner().getNumPayments() >= Utils.NUM_PAYMENTS_FOR_GOLD) {
                    getOwner().setPlan(ServicePlan.GOLD);
                    ObjectNode node = JsonNodeFactory.instance.objectNode();
                    node.put("timestamp", timestamp);
                    node.put("description", "Upgrade plan");
                    node.put("accountIBAN", iban);
                    node.put("newPlanType", "gold");
                    return node;
                }
            }
        }
        return null;
    }

    /**
     * Adds a user to the account.
     *
     * @param email the email of the user to add
     * @param boss the owner of the account
     * @param user  the {@link User} object representing the new user
     */
    public void addUser(final String email, final String boss, final User user) {
    }

    /**
     * Adds a new business associate to the account.
     *
     * @param email       the email of the associate to add
     * @param role        the role assigned to the associate
     * @param timestamp   the timestamp of the action
     * @param application the {@link Application} instance to interact with
     */
    public void addNewBusinessAssociate(final String email, final String role, final int timestamp,
                                        final Application application) {
    }

    /**
     * Changes the spending limit for the account.
     *
     * @param amount    the new spending limit
     * @param email     the email of the account owner
     * @param timestamp the timestamp of the action
     * @return {@code null} if successful, or an {@link ObjectNode} with error details if failed
     */
    public ObjectNode changeSpendingLimit(final double amount, final String email,
                                          final int timestamp) {
        return null;
    }

    /**
     * Changes the deposit limit for the account.
     *
     * @param amount    the new deposit limit
     * @param email     the email of the account owner
     * @param timestamp the timestamp of the action
     * @return {@code null} if successful, or an {@link ObjectNode} with error details if failed
     */
    public ObjectNode changeDepositLimit(final double amount, final String email,
                                         final int timestamp) {
        return null;
    }

    /**
     * Retrieves a business report for the account within a specified time range.
     *
     * @param startTimestamp the start timestamp of the range
     * @param endTimestamp   the end timestamp of the range
     * @param type           the type of report to generate
     * @return an {@link ObjectNode} containing the business report data
     * @throws UnsupportedOperationException if the account is not a business account
     */
    public ObjectNode getBusinessReport(final int startTimestamp, final int endTimestamp,
                                        final String type) {
        throw new UnsupportedOperationException("Account is not of type business");
    }

    /**
     * Adds funds to the account.
     *
     * @param amount    the amount to add
     * @param email     the email of the account owner
     * @param timestamp the timestamp of the transaction
     */
    public void addFunds(final double amount, final String email, final int timestamp) {
        balance += amount;
    }

    /**
     * Deletes a one-time card from the account.
     *
     * @param cardNumber the card number of the card to delete
     * @param timestamp  the timestamp of the deletion
     */
    public void deleteOneTimeCard(final String cardNumber, final int timestamp) {
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
