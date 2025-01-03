package org.poo.main;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.Setter;
import org.poo.commands.CommandHistory;
import org.poo.fileio.UserInput;
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
    private List<Account> accounts;
    private CommandHistory commandHistory;

    /**
     * Constructs a new User with the specified first name, last name, and email.
     * Initializes an empty list of accounts and a new command history.
     *
     * @param firstName the user's first name
     * @param lastName the user's last name
     * @param email the user's email address
     */
    public User(final String firstName, final String lastName, final String email) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        accounts = new ArrayList<>();
        commandHistory = new CommandHistory();
    }

    /**
     * Constructs a new User using the provided UserInput.
     * Initializes an empty list of accounts and a new command history.
     *
     * @param userInput an instance of UserInput containing user details
     */
    public User(final UserInput userInput) {
        firstName = userInput.getFirstName();
        lastName = userInput.getLastName();
        email = userInput.getEmail();
        accounts = new ArrayList<>();
        commandHistory = new CommandHistory();
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
}
