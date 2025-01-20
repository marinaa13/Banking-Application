package org.poo.main.accounts;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.Setter;
import org.poo.fileio.CommandInput;
import org.poo.main.Application;
import org.poo.main.ExchangeRatesGraph;
import org.poo.main.cardTypes.Card;
import org.poo.main.User;
import org.poo.utils.Errors;
import org.poo.utils.Search;
import org.poo.utils.Utils;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Comparator;

@Getter @Setter
public class BusinessAccount extends Account {
    private Map<String, BusinessAccUser> managers;
    private Map<String, BusinessAccUser> employees;
    private double spendingLimit;
    private double depositLimit;
    private List<String> commerciants;

    /**
     * Constructs a {@link BusinessAccount} using the provided {@link CommandInput}.
     * The account is initialized with default spending and deposit limits
     * based on the application's exchange rates.
     *
     * @param input the {@link CommandInput} containing the account data
     * @param app   the {@link Application} instance to access exchange rates
     */
    public BusinessAccount(final CommandInput input, final Application app) {
        super(input);
        managers = new LinkedHashMap<>();
        employees = new LinkedHashMap<>();
        commerciants = new ArrayList<>();
        depositLimit = app.getExchangeRates().getRate(Utils.DEFAULT_CURRENCY,
                input.getCurrency()) * Utils.THRESHOLD_500;
        spendingLimit = app.getExchangeRates().getRate(Utils.DEFAULT_CURRENCY,
                input.getCurrency()) * Utils.THRESHOLD_500;
    }

    /**
     * Adds a user to the account with a specific role.
     *
     * @param email the email of the user
     * @param role  the role of the user (e.g., "owner", "manager", "employee")
     * @param user  the {@link User} object representing the user
     */
    @Override
    public void addUser(final String email, final String role, final User user) {
        switch (role) {
            case "owner" -> setOwner(user);
            case "manager" -> {
                BusinessAccUser newUser = new BusinessAccUser(user.getLastName()
                        + " " + user.getFirstName());
                managers.put(email, newUser);
                user.addBussinessAccount(this);
            }
            case "employee" -> {
                BusinessAccUser newUser = new BusinessAccUser(user.getLastName()
                        + " " + user.getFirstName());
                employees.put(email, newUser);
                user.addBussinessAccount(this);
            }
            default -> { }
        }
    }

    /**
     * Checks if the account is a business account.
     *
     * @return {@code true} since this is a business account
     */
    @Override
    public boolean isBusinessAccount() {
        return true;
    }

    /**
     * Adds a new business associate to the account.
     *
     * @param email     the email of the new associate
     * @param role      the role of the new associate
     * @param timestamp the timestamp of the operation
     * @param app       the {@link Application} instance to search for users
     */
    @Override
    public void addNewBusinessAssociate(final String email, final String role, final int timestamp,
                                        final Application app) {
        User userToAdd = Search.getUserByEmail(app.getUsers(), email);
        if (userToAdd == null) {
            return;
        }
        if (managers.containsKey(email) || employees.containsKey(email)
                || getOwner().getEmail().equals(email)) {
            return;
        }
        addUser(email, role, userToAdd);
    }

    /**
     * Updates the spending limit for the account.
     *
     * @param amount    the new spending limit
     * @param email     the email of the owner performing the update
     * @param timestamp the timestamp of the operation
     * @return {@code null} if successful, or an error {@link ObjectNode} if the update fails
     */
    @Override
    public ObjectNode changeSpendingLimit(final double amount, final String email,
                                          final int timestamp) {
        if (!getOwner().getEmail().equals(email)) {
            return Errors.changeSpendLimitError(timestamp);
        }
        setSpendingLimit(amount);
        return null;
    }

    /**
     * Updates the deposit limit for the account.
     *
     * @param amount    the new deposit limit
     * @param email     the email of the owner performing the update
     * @param timestamp the timestamp of the operation
     * @return {@code null} if successful, or an error {@link ObjectNode} if the update fails
     */
    @Override
    public ObjectNode changeDepositLimit(final double amount, final String email,
                                         final int timestamp) {
        if (!getOwner().getEmail().equals(email)) {
            return Errors.changeDepLimitError(timestamp);
        }
        setDepositLimit(amount);
        return null;
    }

    /**
     * Generates a business report for the account based on the specified type.
     *
     * @param startTimestamp the start of the time range
     * @param endTimestamp   the end of the time range
     * @param type           the type of report ("transaction" or "commerciant")
     * @return an {@link ObjectNode} containing the business report
     */
    @Override
    public ObjectNode getBusinessReport(final int startTimestamp, final int endTimestamp,
                                        final String type) {
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        node.put("IBAN", getIban());
        node.put("balance", getBalance());
        node.put("currency", getCurrency());
        node.put("spending limit", getSpendingLimit());
        node.put("deposit limit", getDepositLimit());
        node.put("statistics type", type);
        if (type.equals("transaction")) {
            node.set("managers", getManagersArray(startTimestamp, endTimestamp));
            node.set("employees", getEmployeesArray(startTimestamp, endTimestamp));
            node.put("total spent", getTotalSpent(startTimestamp, endTimestamp));
            node.put("total deposited", getTotalDeposited(startTimestamp, endTimestamp));
        } else {
            node.set("commerciants", getCommerciantsArray(startTimestamp, endTimestamp));
        }
        return node;
    }

    /**
     * Retrieves an array of commerciants with their total received amounts and associated users.
     *
     * @param startTimestamp the start of the time range
     * @param endTimestamp   the end of the time range
     * @return a {@link JsonNode} containing the commerciants, their total received amounts,
     * and the managers and employees who transacted with them
     */
    private JsonNode getCommerciantsArray(final int startTimestamp, final int endTimestamp) {
        ArrayNode array = JsonNodeFactory.instance.arrayNode();
        for (String commerciant : commerciants) {
            ObjectNode node = JsonNodeFactory.instance.objectNode();
            node.put("commerciant", commerciant);
            node.put("total received",
                    getTotalCommerciant(startTimestamp, endTimestamp, commerciant));
            node.set("managers", getManagersForCommerciant(commerciant));
            node.set("employees", getEmployeesForCommerciant(commerciant));
            array.add(node);
        }
        return sortArrayNodeByStringField(array, "commerciant");
    }

    /**
     * Retrieves an array of managers with their transaction details within the time range.
     *
     * @param startTimestamp the start of the time range
     * @param endTimestamp   the end of the time range
     * @return an {@link ArrayNode} containing the managers' transaction details
     */
    public ArrayNode getManagersArray(final int startTimestamp, final int endTimestamp) {
        ArrayNode array = JsonNodeFactory.instance.arrayNode();
        for (BusinessAccUser user : managers.values()) {
            array.add(user.getJson(startTimestamp, endTimestamp));
        }
        return array;
    }

    /**
     * Retrieves an array of employees with their transaction details within the time range.
     *
     * @param startTimestamp the start of the time range
     * @param endTimestamp   the end of the time range
     * @return an {@link ArrayNode} containing the employees' transaction details
     */
    public ArrayNode getEmployeesArray(final int startTimestamp, final int endTimestamp) {
        ArrayNode array = JsonNodeFactory.instance.arrayNode();
        for (BusinessAccUser user : employees.values()) {
            array.add(user.getJson(startTimestamp, endTimestamp));
        }
        return array;
    }

    /**
     * Retrieves an array of employees who have made transactions with a specific commerciant.
     *
     * @param commerciant the name of the commerciant
     * @return an {@link ArrayNode} containing the names of employees
     */
    public ArrayNode getEmployeesForCommerciant(final String commerciant) {
        ArrayNode array = JsonNodeFactory.instance.arrayNode();
        for (BusinessAccUser user : employees.values()) {
            for (Map.Entry<Integer, Transaction> entry : user.getTransactions().entrySet()) {
                if (entry.getValue().getCommerciant().equals(commerciant)) {
                    array.add(user.getName());
                }
            }
        }
        return array;
    }


    /**
     * Retrieves a list of managers who have made transactions with a specified commerciant.
     *
     * @param commerciant the name of the commerciant
     * @return an {@link ArrayNode} containing the names of managers who made transactions
     */
    public ArrayNode getManagersForCommerciant(final String commerciant) {
        ArrayNode array = JsonNodeFactory.instance.arrayNode();
        for (BusinessAccUser user : managers.values()) {
            for (Map.Entry<Integer, Transaction> entry : user.getTransactions().entrySet()) {
                if (entry.getValue().getCommerciant().equals(commerciant)) {
                    array.add(user.getName());
                }
            }
        }
        return array;
    }

    /**
     * Adds a card to the account and associates it with the specified user.
     *
     * @param card  the {@link Card} to be added to the account
     * @param email the email of the user to associate the card with
     */
    @Override
    public void addCard(final Card card, final String email) {
        getCards().add(card);
        card.setAccountBelonging(this);
        if (employees.containsKey(email)) {
            employees.get(email).addCard(card.getCardNumber());
        }
    }

    /**
     * Adds funds to the account and records the deposit for the specified user.
     *
     * @param amount    the amount to be added to the account
     * @param email     the email of the user making the deposit
     * @param timestamp the timestamp of the deposit
     */
    @Override
    public void addFunds(final double amount, final String email, final int timestamp) {
        if (!employees.containsKey(email) && !managers.containsKey(email)
                && !getOwner().getEmail().equals(email)) {
            return;
        }

        if (employees.containsKey(email)) {
            if (amount > depositLimit) {
                return;
            }
        }
        setBalance(getBalance() + amount);
        if (isEmployee(email)) {
            employees.get(email).getDeposits().put(timestamp, amount);
        } else if (managers.containsKey(email)) {
            managers.get(email).getDeposits().put(timestamp, amount);
        }
    }

    /**
     * Processes a payment transaction using a specified card and records the transaction details.
     * <p>
     * @param card          the {@link Card} used for the payment
     * @param amount        the payment amount in the specified payment currency
     * @param payCurrency   the currency in which the payment is made
     * @param exchangeRates the {@link ExchangeRatesGraph} used for currency conversion
     * @param timestamp     the timestamp of the payment
     * @param commerciant   the name of the commerciant where the payment was made
     * @param email         the email of the user initiating the payment
     * @return an {@link ObjectNode} containing the payment details if successful,
     * or an error message if the payment fails
     */
    @Override
    public ObjectNode makePayment(final Card card, double amount, final String payCurrency,
                                  final ExchangeRatesGraph exchangeRates, final int timestamp,
                                  final String commerciant, final String email) {
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        if (!employees.containsKey(email) && !managers.containsKey(email)
                && !getOwner().getEmail().equals(email)) {
            return Errors.cardNotFound(timestamp);
        }

        if (card.getStatus().equals("frozen")) {
            return Errors.frozenCard(timestamp);
        }

        amount *= exchangeRates.getRate(payCurrency, card.getAccountBelonging().getCurrency());
        double ronAmount = amount * exchangeRates.getRate(getCurrency(), Utils.DEFAULT_CURRENCY);
        double newAmount = amount * getOwner().getCommission(ronAmount);

        if (getBalance() < newAmount) {
            return Errors.insufficientFunds(timestamp);
        }

        if (isEmployee(email)) {
            if (spendingLimit < newAmount) {
                return Errors.userNotFound(timestamp);
            }
        }

        double cashback = getCashbackService().giveCashbackForTransactions(commerciant, amount);
        getCashbackService().addTransactionToCommerciant(commerciant, ronAmount);
        double newCashback = getCashbackService().giveCashbackForAmount(commerciant,
                ronAmount - cashback, getOwner().getPlan());
        newCashback = newCashback * exchangeRates.getRate(Utils.DEFAULT_CURRENCY, getCurrency());
        cashback += newCashback;

        setBalance(getBalance() - newAmount + cashback);
        node.put("timestamp", timestamp);
        node.put("description", "Card payment");
        node.put("amount", amount);
        node.put("commerciant", commerciant);

        card.getAccountBelonging().addToReport(node);
        card.getAccountBelonging().addToSpendingsReport(node);

        if (!commerciants.contains(commerciant)) {
            commerciants.add(commerciant);
        }
        addSpending(email, amount, timestamp, commerciant);
        return node;
    }

    /**
     * Checks if a specified email belongs to an employee.
     * @param email the email to be checked
     * @return {@code true} if the email belongs to an employee, {@code false} otherwise
     */
    public boolean isEmployee(final String email) {
        return employees.containsKey(email);
    }

    /**
     * Records a spending transaction for a specified user (manager or employee).
     *
     * @param email       the email of the user making the transaction
     * @param amount      the amount of money spent
     * @param timestamp   the timestamp of the transaction
     * @param commerciant the name of the commerciant where the transaction occurred
     */
    public void addSpending(final String email, final double amount, final int timestamp,
                            final String commerciant) {
        Transaction transaction = new Transaction(commerciant, amount);
        if (employees.containsKey(email)) {
            employees.get(email).getTransactions().put(timestamp, transaction);
        } else if (managers.containsKey(email)) {
            managers.get(email).getTransactions().put(timestamp, transaction);
        }
    }

    /**
     * Calculates the total amount spent by all managers and employees
     * within a specified time range.
     *
     * @param startTimestamp the start of the time range (inclusive)
     * @param endTimestamp   the end of the time range (inclusive)
     * @return the total amount spent by managers and employees within the given time range
     */
    public double getTotalSpent(final int startTimestamp, final int endTimestamp) {
        double total = 0;
        for (BusinessAccUser user : managers.values()) {
            total += user.getSpentAmount(startTimestamp, endTimestamp);
        }
        for (BusinessAccUser user : employees.values()) {
            total += user.getSpentAmount(startTimestamp, endTimestamp);
        }
        return total;
    }

    /**
     * Calculates the total amount deposited by all managers and employees
     * within a specified time range.
     *
     * @param startTimestamp the start of the time range (inclusive)
     * @param endTimestamp   the end of the time range (inclusive)
     * @return the total amount deposited by managers and employees within the given time range
     */
    public double getTotalDeposited(final int startTimestamp, final int endTimestamp) {
        double total = 0;
        for (BusinessAccUser user : managers.values()) {
            total += user.getDepositAmount(startTimestamp, endTimestamp);
        }
        for (BusinessAccUser user : employees.values()) {
            total += user.getDepositAmount(startTimestamp, endTimestamp);
        }
        return total;
    }

    /**
     * Calculates the total amount spent at a specific commerciant by all managers and employees
     * within a specified time range.
     *
     * @param startTimestamp the start of the time range (inclusive)
     * @param endTimestamp   the end of the time range (inclusive)
     * @param commerciant    the name of the commerciant whose transactions are to be totaled
     * @return the total amount spent at the specified commerciant within the given time range
     */
    public double getTotalCommerciant(final int startTimestamp, final int endTimestamp,
                                      final String commerciant) {
        double total = 0;
        for (BusinessAccUser user : managers.values()) {
            total += user.getTotalSpentByCommerciant(startTimestamp, endTimestamp, commerciant);
        }
        for (BusinessAccUser user : employees.values()) {
            total += user.getTotalSpentByCommerciant(startTimestamp, endTimestamp, commerciant);
        }
        return total;
    }

    /**
     * Converts the business account's data to a JSON representation.
     * <p>
     * @return an {@link ObjectNode} representing the business account's details
     */
    @Override
    public ObjectNode getJson() {
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        node.put("IBAN", getIban());
        node.put("balance", getBalance());
        node.put("currency", getCurrency());
        node.put("type", "business");

        ArrayNode array = JsonNodeFactory.instance.arrayNode();
        for (Card card : getCards()) {
            array.add(card.getJson());
        }
        node.set("cards", array);
        return node;
    }

    /**
     * Sorts an {@link ArrayNode} based on the specified string field in ascending order.
     *
     * @param arrayNode the {@link ArrayNode} to be sorted
     * @param fieldName the name of the string field by which the sorting will be performed
     * @return a new {@link ArrayNode} containing the elements sorted by the specified field
     */
    public static ArrayNode sortArrayNodeByStringField(final ArrayNode arrayNode,
                                                       final String fieldName) {
        List<JsonNode> nodes = new ArrayList<>();
        arrayNode.forEach(nodes::add);

        nodes.sort(Comparator.comparing(node -> node.get(fieldName).asText()));

        ArrayNode sortedArrayNode = JsonNodeFactory.instance.arrayNode();
        nodes.forEach(sortedArrayNode::add);

        return sortedArrayNode;
    }
}
