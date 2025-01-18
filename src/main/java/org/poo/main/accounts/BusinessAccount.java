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
import org.poo.main.userTypes.User;
import org.poo.utils.Errors;
import org.poo.utils.Search;
import org.poo.utils.Utils;

import java.util.*;

@Getter @Setter
public class BusinessAccount extends Account {
    private Map<String, BusinessAccUser> managers;
    private Map<String, BusinessAccUser> employees;
    private double spendingLimit;
    private double depositLimit;
    private List<String> commerciants;

    /**
     * Constructs an {@link Account} using the provided {@link CommandInput}.
     * The account is initialized with a balance of 0, a minimum balance of 0
     * and the specified currency.
     * A unique IBAN is generated for the account.
     *
     * @param input the {@link CommandInput} containing the account data
     */
    public BusinessAccount(CommandInput input, Application app) {
        super(input);
        managers = new LinkedHashMap<>();
        employees = new LinkedHashMap<>();
        commerciants = new ArrayList<>();
        depositLimit = app.getExchangeRates().getRate(Utils.DEFAULT_CURRENCY, input.getCurrency()) * 500;
        spendingLimit = app.getExchangeRates().getRate(Utils.DEFAULT_CURRENCY, input.getCurrency()) * 500;
    }

    @Override
    public void addUser(String email, String role, User user) {
        switch (role) {
            case "owner" -> setOwner(user);
            case "manager" -> {
                BusinessAccUser newUser = new BusinessAccUser(user.getLastName() + " " + user.getFirstName());
                managers.put(email, newUser);
                user.addBussinessAccount(this);
            }
            case "employee" -> {
                BusinessAccUser newUser = new BusinessAccUser(user.getLastName() + " " + user.getFirstName());
                employees.put(email, newUser);
                user.addBussinessAccount(this);
            }
        }
    }

    @Override
    public boolean isBusinessAccount() {
        return true;
    }

    @Override
    public void addNewBusinessAssociate(String email, String role, int timestamp, Application app) {
        User userToAdd = Search.getUserByEmail(app.getUsers(), email);
        if (userToAdd == null) {
            return;                         // "User not found"
        }
        if (managers.containsKey(email) || employees.containsKey(email) || getOwner().getEmail().equals(email)) {
            return;                         // "The user is already an associate of the account."
        }
        addUser(email, role, userToAdd);
    }

    @Override
    public ObjectNode changeSpendingLimit(double amount, String email, int timestamp) {
        if (!getOwner().getEmail().equals(email)) {
            return Errors.changeSpendLimitError(timestamp);     //tre sa mi dea “You are not authorized to make this transaction.” dar nuj unde
        }
        setSpendingLimit(amount);
        return null;
    }

    @Override
    public void changeDepositLimit(double amount, String email, int timestamp) {
        if (!getOwner().getEmail().equals(email)) {
            return;     //tre sa mi dea “You are not authorized to make this transaction.” dar nuj unde
        }
        setDepositLimit(amount);
    }

    @Override
    public ObjectNode getBusinessReport(int startTimestamp, int endTimestamp, String type) {
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

    private JsonNode getCommerciantsArray(int startTimestamp, int endTimestamp) {
        ArrayNode array = JsonNodeFactory.instance.arrayNode();
        for (String commerciant : commerciants) {
            ObjectNode node = JsonNodeFactory.instance.objectNode();
            node.put("commerciant", commerciant);
            node.put("total received", getTotalCommerciant(startTimestamp, endTimestamp, commerciant));
            node.set("managers", getManagersForCommerciant(commerciant));
            node.set("employees", getEmployeesForCommerciant(commerciant));
            array.add(node);
        }
        return sortArrayNodeByStringField(array, "commerciant");
    }

    public ArrayNode getManagersArray(int startTimestamp, int endTimestamp){
        ArrayNode array = JsonNodeFactory.instance.arrayNode();
        for (BusinessAccUser user : managers.values()) {
            array.add(user.getJson(startTimestamp, endTimestamp));
        }
        return array;
    }

    public ArrayNode getEmployeesArray(int startTimestamp, int endTimestamp){
        ArrayNode array = JsonNodeFactory.instance.arrayNode();
        for (BusinessAccUser user : employees.values()) {
            array.add(user.getJson(startTimestamp, endTimestamp));
        }
        return array;
    }

    public ArrayNode getEmployeesForCommerciant(String commerciant) {
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

    public ArrayNode getManagersForCommerciant(String commerciant) {
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

    @Override
    public void addCard(final Card card, String email) {
        getCards().add(card);
        card.setAccountBelonging(this);
        // employees can only delete their own cards
        if (employees.containsKey(email)) {
            employees.get(email).addCard(card.getCardNumber());
        }
    }

    @Override
    public void addFunds(final double amount, final String email, final int timestamp) {
        if (!employees.containsKey(email) && !managers.containsKey(email) && !getOwner().getEmail().equals(email)) {
            return;
        }

        if (employees.containsKey(email)) {
            if (amount > depositLimit)
                return;
        }
        setBalance(getBalance() + amount);
        if (isEmployee(email)) {
            employees.get(email).getDeposits().put(timestamp, amount);
        } else if (managers.containsKey(email)) {
            managers.get(email).getDeposits().put(timestamp, amount);
        }
    }

    @Override
    public ObjectNode makePayment(final Card card, double amount, final String payCurrency,
                                  final ExchangeRatesGraph exchangeRates, final int timestamp,
                                  final String commerciant, String email) {
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        if (!employees.containsKey(email) && !managers.containsKey(email) && !getOwner().getEmail().equals(email)) {
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
                return Errors.userNotFound(timestamp);  //need to change in nu are voie
            }
        }

        // verific daca am vreun cashback de dat pt tranzactii, care poate fi primit de orice comerciant
        double cashback = getCashbackService().giveCashbackForTransactions(commerciant, amount);

        // adaug tranzactia, indiferent ce tip e
        getCashbackService().addTransactionToCommerciant(commerciant, ronAmount);

        //verific cashbackul pt amount, care poate fi primit si pt tranzactia curenta
        double newCashback = getCashbackService().giveCashbackForAmount(commerciant, ronAmount - cashback, getOwner().getPlan());
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

    public boolean isEmployee(String email) {
        return employees.containsKey(email);
    }

    public void addSpending(String email, double amount, int timestamp, final String commerciant) {
        Transaction transaction = new Transaction(commerciant, amount);
        if (employees.containsKey(email)) {
            employees.get(email).getTransactions().put(timestamp, transaction);
        } else if (managers.containsKey(email)) {
            managers.get(email).getTransactions().put(timestamp, transaction);
        }
    }

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

    public double getTotalCommerciant(final int startTimestamp, final int endTimestamp, final String commerciant) {
        double total = 0;
        for (BusinessAccUser user : managers.values()) {
            total += user.getTotalSpentByCommerciant(startTimestamp, endTimestamp, commerciant);
        }
        for (BusinessAccUser user : employees.values()) {
            total += user.getTotalSpentByCommerciant(startTimestamp, endTimestamp, commerciant);
        }
        return total;
    }



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

    public static ArrayNode sortArrayNodeByStringField(ArrayNode arrayNode, String fieldName) {
        // Convert ArrayNode to a List<JsonNode>
        List<JsonNode> nodes = new ArrayList<>();
        arrayNode.forEach(nodes::add);

        // Sort the list by the specified string field
        nodes.sort(Comparator.comparing(node -> node.get(fieldName).asText()));

        // Create a new sorted ArrayNode
        ArrayNode sortedArrayNode = JsonNodeFactory.instance.arrayNode();
        nodes.forEach(sortedArrayNode::add);

        return sortedArrayNode;
    }
}
