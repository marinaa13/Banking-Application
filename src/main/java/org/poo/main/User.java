package org.poo.main;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.Setter;
import org.poo.commands.CommandHistory;
import org.poo.fileio.UserInput;
import org.poo.main.accounts.Account;
import org.poo.utils.Errors;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a user with personal information, associated accounts, and a command history.
 * <p>
 * This class contains methods for different banking operations
 * and converting the user data to JSON format.
 * The user is identified by their unique email address.
 */
@Getter @Setter
public class User {
    private String firstName;
    private String lastName;
    private final String email;
    private final String birthDate;
    private final String occupation;
    private ServicePlan plan;
    private List<Account> accounts;
    private CommandHistory commandHistory;
    private boolean hasClassicAccount;
    private final Application app;

    /**
     * Constructs a new User using the provided UserInput.
     * Initializes an empty list of accounts and a new command history.
     *
     * @param userInput an instance of UserInput containing user details
     */
    public User(final UserInput userInput, final Application app) {
        firstName = userInput.getFirstName();
        lastName = userInput.getLastName();
        email = userInput.getEmail();
        birthDate = userInput.getBirthDate();
        occupation = userInput.getOccupation();
        this.plan = isStudent() ? ServicePlan.STUDENT : ServicePlan.STANDARD;
        accounts = new ArrayList<>();
        commandHistory = new CommandHistory();
        this.app = app;
    }

    /**
     * Converts the User object to a JSON object.
     * The resulting JSON object includes the user's banking information.
     *
     * @return a JSON representation of the user
     */
    public ObjectNode getJson() {
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        node.put("firstName", firstName);
        node.put("lastName", lastName);
        node.put("email", email);

        ArrayNode array = JsonNodeFactory.instance.arrayNode();
        for (Account account : accounts) {
            array.add(account.getJson());
        }

        node.set("accounts", array);
        return node;
    }

    /**
     * Adds a new account to the user's list of accounts.
     * It also logs a command to the user's command history.
     *
     * @param account the account to add
     * @param timestamp the timestamp when the account was added
     */
    public void addAccount(final Account account, final int timestamp) {
        accounts.add(account);
        if (account.isClassicAccount()) {
            hasClassicAccount = true;
        }
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        node.put("timestamp", timestamp);
        node.put("description", "New account created");

        // Add account report if it's not the first account
        if (accounts.size() > 1) {
            account.addToReport(node);
        }
        commandHistory.addToHistory(node);
    }

    /**
     * Deletes an account based on the provided account IBAN.
     * If the account has a non-zero balance, it cannot be deleted, and an error message is logged.
     *
     * @param account the IBAN of the account to delete
     * @param timestamp the timestamp when the action occurred
     * @return 1 if the account is deleted, 0 if the account cannot be deleted
     */
    public int deleteAccount(final String account, final int timestamp) {
        for (Account a : accounts) {
            if (a.getIban().equals(account)) {
                if (a.getBalance() != 0) {
                    break;
                } else {
                    accounts.remove(a);
                    return 1;
                }
            }
        }

        // Log the error if the account cannot be deleted
        ObjectNode node = Errors.fundsRemaining(timestamp);
        getCommandHistory().addToHistory(node);
        return 0;
    }

    /**
     * Adds a new card to the user's corresponding account.
     * The action is logged in the user's command history.
     *
     * @param card the card to add
     * @param timestamp the timestamp when the card was created
     */
    public void addCard(final Card card, final int timestamp) {
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        node.put("timestamp", timestamp);
        node.put("description", "New card created");
        node.put("card", card.getCardNumber());
        node.put("cardHolder", email);
        node.put("account", card.getAccount());

        commandHistory.addToHistory(node);

        for (Account acc : accounts) {
            if (acc.getIban().equals(card.getAccount())) {
                acc.addCard(card);
                acc.addToReport(node);
            }
        }
    }

    public boolean isStudent() {
        return occupation.equals("student");
    }

    public int getAge() {
        return 2024 - Integer.parseInt(birthDate.substring(0, 4));
    }

    public void withdrawSavings(Account acc, double amount, String currency, int timestamp) {
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        node.put("timestamp", timestamp);
        if (getFirstClassicAccount(currency) == null) {
            node.put("description", "You do not have a classic account.");
        } else if (getAge() < 21) {
            node.put("description", "You don't have the minimum age required.");
        } else if (!acc.isSavingsAccount()) {
            node.put("description", "Account is not of type savings.");
        } else {
            Account to = getFirstClassicAccount(currency);
            makeWithdrawal(acc, to, amount, currency, timestamp);
            node.put("description", "Savings withdrawal");
        }
        getCommandHistory().addToHistory(node);
    }

    public Account getFirstClassicAccount(String currency) {
        for (Account acc : accounts) {
            if (acc.isClassicAccount() && acc.getCurrency().equals(currency)) {
                return acc;
            }
        }
        return null;
    }

    public void makeWithdrawal(Account from, Account to, double amount, String currency, int timestamp) {
        if (from.getBalance() >= amount) {
            from.setBalance(from.getBalance() - amount);
            to.setBalance(to.getBalance() + amount);
        } else {
            ObjectNode node = Errors.insufficientFunds(timestamp);
            getCommandHistory().addToHistory(node);
        }
    }

    public void upgradePlan(Account acc, ServicePlan newPlanType, double rate, int timestamp) {
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        node.put("timestamp", timestamp);
        node.put("description", "Upgrade plan");
        node.put("accountIBAN", acc.getIban());
        node.put("newPlanType", newPlanType.toString().toLowerCase());
        commandHistory.addToHistory(node);

        if (newPlanType == plan) {
            node.put("description", "The user already has the " + plan + " plan.");
        } else if (newPlanType.ordinal() < plan.ordinal()) {
            node.put("description", "You cannot downgrade your plan.");
        } else {
            changePlan(newPlanType, acc, rate);
        }
    }

    private void changePlan(ServicePlan newPlanType, Account acc, double rate) {
        double amount = 0;
        if ((plan == ServicePlan.STUDENT || plan == ServicePlan.STANDARD) && newPlanType == ServicePlan.SILVER) {
            amount = 100 * rate;
        } else if ((plan == ServicePlan.STUDENT || plan == ServicePlan.STANDARD) && newPlanType == ServicePlan.GOLD) {
            amount = 350 * rate;
        } else if (plan == ServicePlan.SILVER && newPlanType == ServicePlan.GOLD) {
            amount = 250 * rate;
        }
        try {
            acc.deductFee(amount);
            plan = newPlanType;
        } catch (Exception e) {
            ObjectNode node = Errors.insufficientFunds(0);
            commandHistory.addToHistory(node);
        }
    }

    public double getCommission(double amount) {
        switch(plan) {
            case STANDARD:
                return 1.002;
            case SILVER:
                if (amount > 500) {
                    return 1.001;
                }
            default:
                return 1;
        }
    }
}
