package org.poo.main;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.Setter;
import org.poo.commands.CommandInvoker;
import org.poo.fileio.ObjectInput;
import org.poo.main.accounts.Account;
import org.poo.utils.Errors;
import org.poo.utils.Utils;
import org.poo.utils.Search;

import java.util.ArrayList;
import java.util.List;

/**
 * The main application class that manages users, accounts, exchange rates, and commands.
 * <p>
 * This class is responsible for parsing user input, managing accounts, performing transactions
 * and interacting with exchange rates. It also handles command execution and generates reports.
 */
@Getter @Setter
public class Application {
    private final ObjectInput input;
    private List<User> users;
    private List<Commerciant> commerciants;
    private ExchangeRatesGraph exchangeRates;

    /**
     * Constructs an {@link Application} instance with the specified ObjectMapper and ObjectInput.
     *
     * @param input the input object containing users, exchange rates, and commands
     */
    public Application(final ObjectInput input) {
        this.input = input;
        users = new ArrayList<>();
        commerciants = new ArrayList<>();
    }

    /**
     * Parses the provided exchange rates and initializes the exchange rates graph.
     *
     * @param rates the list of {@link ExchangeRate} objects to parse
     */
    public void parseExchangeRates(final List<ExchangeRate> rates) {
        exchangeRates = new ExchangeRatesGraph(rates);
        exchangeRates.makeGraph(rates);
    }

    /**
     * Parses the user input to create users and exchange rates.
     * <p>
     * This method initializes the users list and sets up the exchange rates if available.
     */
    public void parseInput() {
        for (var userInput : input.getUsers()) {
            users.add(new User(userInput, this));
        }

        for (var commerciantInput : input.getCommerciants()) {
            commerciants.add(new Commerciant(commerciantInput));
        }

        if (input.getExchangeRates() != null) {
            List<ExchangeRate> exchangeRatesList = new ArrayList<>();
            for (var exchangeInput : input.getExchangeRates()) {
                exchangeRatesList.add(new ExchangeRate(exchangeInput));
            }
            parseExchangeRates(exchangeRatesList);
        }
    }

    /**
     * Executes commands parsed from the input and returns the resulting JSON array.
     *
     * @return an {@link ArrayNode} containing the results of the executed commands
     */
    public ArrayNode parseCommands() {
        ArrayNode outputArray = JsonNodeFactory.instance.arrayNode();
        CommandInvoker invoker = new CommandInvoker();

        for (var commandInput : input.getCommands()) {
            ObjectNode output = invoker.executeCommand(commandInput, this);
            if (output != null) {
                outputArray.add(output.deepCopy());
            }
        }
        return outputArray;
    }

    /**
     * Adds a new account for the specified user identified by email.
     *
     * @param email the email of the user to add the account to
     * @param account the account to be added
     * @param timestamp the timestamp of the action
     */
    public void addAccount(final String email, final Account account, final int timestamp) {
        User user = Search.getUserByEmail(users, email);
        if (user != null) {
            user.addAccount(account, timestamp);
            account.setOwner(user);
            account.setCashbackService(new TransactionManager(this));
        }
    }

    /**
     * Deletes an account identified by IBAN for a user identified by email.
     *
     * @param email the email of the user
     * @param account the IBAN of the account to be deleted
     * @param timestamp the timestamp of the deletion
     * @return an {@link ObjectNode} with the result of the deletion (success or error message)
     */
    public ObjectNode deleteAccount(final String email, final String account,
                                    final int timestamp) {
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        User user = Search.getUserByEmail(users, email);
        if (user == null) {
            return null;
        }
        int ret = user.deleteAccount(account, timestamp);
        if (ret == 0) {
            node.put("error",
                    "Account couldn't be deleted - see org.poo.transactions for details");
        } else {
            node.put("success", "Account deleted");
        }
        return node;
    }

    /**
     * Creates a new card for the specified user identified by email.
     *
     * @param email the email of the user to create the card for
     * @param card the card to be created
     * @param timestamp the timestamp of the card creation
     */
    public void createCard(final String email, final Card card, final int timestamp) {
        User user = Search.getUserByEmail(users, email);
        if (user != null) {
            card.setCardNumber(Utils.generateCardNumber());
            user.addCard(card, timestamp);
        }
    }

    /**
     * Deletes a card identified by its card number.
     *
     * @param cardNumber the card number of the card to be deleted
     * @param timestamp the timestamp of the deletion
     */
    public void deleteCard(final String cardNumber, final int timestamp) {
        Account acc = Search.getAccountByCard(users, cardNumber);
        if (acc != null) {
            acc.deleteCard(cardNumber, timestamp);
        }
    }

    /**
     * Runs the entire application by parsing the input and executing the commands.
     *
     * @return an {@link ArrayNode} containing the results of the executed commands
     */
    public ArrayNode runApplication() {
        parseInput();
        return parseCommands();
    }

    /**
     * Resets the application by clearing users and resetting random generators.
     */
    public void resetAll() {
        users.clear();
        Utils.resetRandom();
    }

    /**
     * Adds funds to an account identified by its IBAN.
     *
     * @param account the IBAN of the account to which funds will be added
     * @param amount the amount to be added
     */
    public void addFunds(final String account, final double amount) {
        Account acc = Search.getAccountByIBAN(users, account);
        if (acc != null) {
            acc.setBalance(acc.getBalance() + amount);
        }
    }

    /**
     * Sets the minimum balance for an account identified by its IBAN.
     *
     * @param account the IBAN of the account
     * @param amount the minimum balance to be set
     */
    public void setMinBalance(final String account, final double amount) {
        Account acc = Search.getAccountByIBAN(users, account);
        if (acc != null) {
            acc.setMinBalance(amount);
        }
    }

    /**
     * Pays for an online transaction using a specified card.
     *
     * @param cardNumber the card number used for the payment
     * @param amount the amount to be paid
     * @param currency the currency of the payment
     * @param timestamp the timestamp of the transaction
     * @param commerciant the merchant name for the transaction
     * @return an {@link ObjectNode} representing the result of the payment
     */
    public ObjectNode payOnline(final String cardNumber, double amount, final String currency,
                                final int timestamp, final String commerciant) {
        Card card = Search.getCardByNumber(users, cardNumber);
        if (card == null) {
            return Errors.cardNotFound(timestamp);
        }

        ObjectNode node = card.getAccountBelonging()
                .makePayment(card, amount, currency, exchangeRates, timestamp, commerciant);
        card.getAccountBelonging().getOwner().getCommandHistory().addToHistory(node);
        if (node.has("amount")) {
            card.madePayment(timestamp);
        }
        return null;
    }

    /**
     * Sends money from one account to another.
     *
     * @param fromAccount the IBAN of the account sending the money
     * @param toAccount the IBAN or alias of the account receiving the money
     * @param amount the amount to send
     * @param description a description for the transaction
     * @param timestamp the timestamp of the transaction
     */
    public void sendMoney(final String fromAccount, final String toAccount, double amount,
                          final String description, final int timestamp) {
        Account from = Search.getAccountByIBAN(users, fromAccount);
        Account to = isIBAN(toAccount) ? Search.getAccountByIBAN(users, toAccount)
                                        : Search.getAccountByAlias(users, toAccount);

        if (from == null || to == null) {
            return;
        }

        double ronAmount = amount * exchangeRates.getRate(from.getCurrency(), Utils.defaultCurrency);
        double commission = from.getOwner().getCommission(ronAmount);

        if (from.getBalance() < amount * commission) {
            ObjectNode node = Errors.insufficientFunds(timestamp);
            from.getOwner().getCommandHistory().addToHistory(node);
            from.addToReport(node);
            return;
        }

        from.sendMoney(toAccount, amount, commission, description, timestamp);
        amount *= exchangeRates.getRate(from.getCurrency(), to.getCurrency());
        to.receiveMoney(fromAccount, amount, description, timestamp);
    }

    /**
     * Sets an alias for an account identified by its IBAN.
     *
     * @param account the IBAN of the account
     * @param alias the alias to be set
     */
    public void setAlias(final String account, final String alias) {
        Account acc = Search.getAccountByIBAN(users, account);
        if (acc != null) {
            acc.setAlias(alias);
        }
    }

    /**
     * Prints the transaction history for a user identified by email.
     *
     * @param email the email of the user
     * @return an {@link ArrayNode} containing the user's transaction history
     */
    public ArrayNode printTransactions(final String email) {
        User user = Search.getUserByEmail(users, email);
        if (user != null) {
            return user.getCommandHistory().getHistory();
        }
        return null;
    }

    /**
     * Checks the status of a card, and freezes it if the balance is below the minimum threshold.
     *
     * @param cardNumber the card number to check
     * @param timestamp the timestamp of the action
     * @return an {@link ObjectNode} representing the result of the action
     */
    public ObjectNode checkCardStatus(final String cardNumber, final int timestamp) {
        Card card = Search.getCardByNumber(users, cardNumber);
        if (card == null) {
            return Errors.cardNotFound(timestamp);
        }

        Account account = card.getAccountBelonging();
        if (account.getBalance() <= account.getMinBalance()) {
            card.blockCard();
            ObjectNode node = JsonNodeFactory.instance.objectNode();
            node.put("timestamp", timestamp);
            node.put("description",
                    "You have reached the minimum amount of funds, the card will be frozen");
            account.getOwner().getCommandHistory().addToHistory(node);
        }
        return null;
    }

    /**
     * Changes the interest rate for an account identified by its IBAN.
     *
     * @param newInterestRate the new interest rate to set
     * @param account the IBAN of the account
     * @param timestamp the timestamp of the action
     * @return an {@link ObjectNode} with the result of the operation
     */
    public ObjectNode changeInterestRate(final double newInterestRate, final String account,
                                         final int timestamp) {
        Account acc = Search.getAccountByIBAN(users, account);
        if (acc == null) {
            return Errors.accountNotFound(timestamp);
        }
        try {
            acc.setInterestRate(newInterestRate);
            ObjectNode node = JsonNodeFactory.instance.objectNode();
            node.put("description", "Interest rate of the account changed to " + newInterestRate);
            node.put("timestamp", timestamp);
            acc.getOwner().getCommandHistory().addToHistory(node);
        } catch (UnsupportedOperationException e) {
            return Errors.notSavingsAccount(timestamp);
        }
        return null;
    }

    /**
     * Adds interest to an account identified by its IBAN.
     *
     * @param account the IBAN of the account to add interest to
     * @param timestamp the timestamp of the action
     * @return an {@link ObjectNode} with the result of the operation
     */
    public ObjectNode addInterest(final String account, final int timestamp) {
        Account acc = Search.getAccountByIBAN(users, account);
        if (acc == null) {
            return Errors.accountNotFound(timestamp);
        }
        try {
            acc.addInterest();
        } catch (UnsupportedOperationException e) {
            return Errors.notSavingsAccount(timestamp);
        }
        return null;
    }

    /**
     * Converts a list of account IBANs to an array of JSON objects.
     *
     * @param accounts a list of account IBANs
     * @return an {@link ArrayNode} containing the account IBANs as JSON
     */
    public ArrayNode getAccountsArray(final List<String> accounts) {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.valueToTree(accounts);
    }

    /**
     * Splits a payment across multiple accounts.
     * <p>
     * This method attempts to divide the payment equally among the accounts
     * and checks if each account has sufficient funds for its portion.
     * If any account lacks funds, an error is logged for that account.
     * If all accounts have sufficient funds, the amounts are deducted accordingly.
     *
     * @param accounts a list of account IBANs to split the payment across
     * @param currency the currency of the payment
     * @param amount the total amount to be paid
     * @param timestamp the timestamp of the payment
     */
    public void splitPayment(final List<String> accounts, final String currency,
                             final double amount, final int timestamp) {
        double toPay = amount / accounts.size();
        int ok = 1;
        String accountToBlame = "";

        for (String account : accounts) {
            Account acc = Search.getAccountByIBAN(users, account);
            if (acc == null) {
                return;
            }
            toPay *= exchangeRates.getRate(currency, acc.getCurrency());

            if (acc.getBalance() < toPay) {
                ok = 0;
                accountToBlame = acc.getIban();
            }
            toPay = amount / accounts.size();
        }

        if (ok == 0) {
            for (String account : accounts) {
                Account acc = Search.getAccountByIBAN(users, account);
                if (acc == null) {
                    return;
                }
                ObjectNode node = JsonNodeFactory.instance.objectNode();
                node.put("amount", toPay);
                node.put("currency", currency);
                String formatted = String.format("%.2f", amount);
                node.put("description", "Split payment of " + formatted + " " + currency);
                node.put("error",
                        "Account " + accountToBlame
                                + " has insufficient funds for a split payment.");
                node.set("involvedAccounts", getAccountsArray(accounts));
                node.put("timestamp", timestamp);
                acc.addToReport(node);
                acc.getOwner().getCommandHistory().addToHistory(node);
            }
        } else {
            for (String account : accounts) {
                Account acc = Search.getAccountByIBAN(users, account);
                if (acc == null) {
                    return;
                }
                acc.setBalance(acc.getBalance()
                        - toPay * exchangeRates.getRate(currency, acc.getCurrency()));
                ArrayNode accountsArray = getAccountsArray(accounts);
                ObjectNode node = acc.addSplitTransaction(accountsArray, currency,
                                                            amount, timestamp);
                acc.getOwner().getCommandHistory().addToHistory(node);
            }
        }
    }

    /**
     * Retrieves a report of an account's balance and transactions within a specific time range.
     *
     * @param account the IBAN of the account
     * @param startTimestamp the start timestamp of the report period
     * @param endTimestamp the end timestamp of the report period
     * @return an {@link ObjectNode} representing the account's report
     */
    public ObjectNode getReport(final String account, final int startTimestamp,
                                final int endTimestamp) {
        Account acc = Search.getAccountByIBAN(users, account);
        if (acc == null) {
            return null;
        }

        ObjectNode node = JsonNodeFactory.instance.objectNode();
        node.put("IBAN", acc.getIban());
        node.put("balance", acc.getBalance());
        node.put("currency", acc.getCurrency());
        node.set("transactions", acc.getReport(startTimestamp, endTimestamp));
        return node;
    }

    /**
     * Retrieves a report of an account's spending activity within a specific time range.
     *
     * @param account the IBAN of the account
     * @param startTimestamp the start timestamp of the report period
     * @param endTimestamp the end timestamp of the report period
     * @return an {@link ObjectNode} representing the account's spending report
     */
    public ObjectNode getSpendingsReport(final String account, final int startTimestamp,
                                         final int endTimestamp) {
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        Account acc = Search.getAccountByIBAN(users, account);
        if (acc == null) {
            return null;
        }
        try {
            ArrayNode array = acc.getSpendingsReport(startTimestamp, endTimestamp);
            node.put("IBAN", acc.getIban());
            node.put("balance", acc.getBalance());
            node.put("currency", acc.getCurrency());
            node.set("transactions", array);
            node.set("commerciants", acc.getCommerciants(startTimestamp, endTimestamp));
        } catch (UnsupportedOperationException e) {
            node.put("error", "This kind of report is not supported for a saving account");
        }
        return node;
    }

    /**
     * Checks if a string is a valid IBAN.
     *
     * @param string the string to check
     * @return true if the string is a valid IBAN, false otherwise
     */
    public boolean isIBAN(final String string) {
        return string.matches(".*\\d.*");
    }

    public void withdrawSavings(String account, double amount, String currency, int timestamp) {
        Account acc = Search.getAccountByIBAN(users, account);
        if (acc == null) {
            return;
        }
        amount *= exchangeRates.getRate(currency, acc.getCurrency());
        acc.getOwner().withdrawSavings(acc, amount, currency, timestamp);
    }

    public void upgradePlan(String account, String newPlanType, int timestamp) {
       Account acc = Search.getAccountByIBAN(users, account);
       if (acc == null) {
           return;
       }
       double rate = exchangeRates.getRate(Utils.defaultCurrency, acc.getCurrency());
       acc.getOwner().upgradePlan(acc, ServicePlan.valueOf(newPlanType.toUpperCase()), rate, timestamp);
    }

    public ObjectNode cashWithdrawal(String cardNumber, double amount, String email, String location, int timestamp) {
        User user = Search.getUserByEmail(users, email);
        if (user == null) {
            return Errors.userNotFound(timestamp);
        }
        Card card = Search.getCardByNumber(users, cardNumber);
        if (card == null) {
            return Errors.cardNotFound(timestamp);
        }

        ObjectNode inner = card.getAccountBelonging().cashWithdrawal(card, amount, email, location, timestamp, exchangeRates);
        card.getAccountBelonging().getOwner().getCommandHistory().addToHistory(inner);
        return null;
    }
}
