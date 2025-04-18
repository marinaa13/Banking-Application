package org.poo.main;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.Setter;
import org.poo.commands.CommandInvoker;
import org.poo.fileio.ObjectInput;
import org.poo.main.accounts.Account;
import org.poo.main.cardTypes.Card;
import org.poo.main.splitPayment.SplitPayment;
import org.poo.main.splitPayment.SplitPaymentInfo;
import org.poo.main.splitPayment.SplitPaymentStatus;
import org.poo.utils.Errors;
import org.poo.utils.Utils;
import org.poo.utils.Search;

import java.util.ArrayList;
import java.util.Comparator;
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
            account.setCashbackService(new CashbackService(this));
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
        node.put("timestamp", timestamp);
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
    public void deleteCard(final String cardNumber, final int timestamp, final String email) {
        Account acc = Search.getAccountByCard(users, cardNumber);
        if (acc != null) {
            acc.deleteCard(cardNumber, timestamp, email);
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
    public void addFunds(final String account, final double amount, final String email,
                         final int timestamp) {
        Account acc = Search.getAccountByIBAN(users, account);
        if (acc != null) {
            acc.addFunds(amount, email, timestamp);
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
                                final int timestamp, final String commerciant,
                                final String email) {
        Card card = Search.getCardByNumber(users, cardNumber);
        if (card == null) {
            return Errors.cardNotFound(timestamp);
        }

        if (Search.getCommerciantByName(commerciants, commerciant) == null) {
            return Errors.commerciantNotFound(timestamp);
        }

        ObjectNode node = card.getAccountBelonging()
                .makePayment(card, amount, currency, exchangeRates, timestamp, commerciant, email);

        if (node.equals(Errors.cardNotFound(timestamp))) {
            return Errors.cardNotFound(timestamp);
        }

        card.getAccountBelonging().getOwner().getCommandHistory().addToHistory(node);
        if (node.has("amount")) {
            card.madePayment(timestamp);
            ObjectNode upgrade = card.getAccountBelonging().checkForGold(amount,
                                exchangeRates, timestamp);
            if (upgrade != null) {
                card.getAccountBelonging().getOwner().getCommandHistory().addToHistory(upgrade);
            }
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
    public ObjectNode sendMoney(final String fromAccount, final String toAccount, double amount,
                          final String description, final int timestamp) {
        if (!isIBAN(fromAccount)) {
            return null;
        }
        Account from = Search.getAccountByIBAN(users, fromAccount);
        Account to = isIBAN(toAccount) ? Search.getAccountByIBAN(users, toAccount)
                                        : Search.getAccountByAlias(users, toAccount);

        if (to == null) {
            Commerciant comm = Search.getCommerciantByIban(commerciants, toAccount);
            if (comm != null && from != null) {
                //transfer bancar catre comerciant
                from.sendMoneyToCommerciant(amount, exchangeRates, comm, description, timestamp);
                return null;
            }
        }

        if (from == null || to == null) {
            return Errors.userNotFound(timestamp);
        }

        double ronAmount = amount
                * exchangeRates.getRate(from.getCurrency(), Utils.DEFAULT_CURRENCY);
        double commission = from.getOwner().getCommission(ronAmount);
        double newAmount = amount * commission;

        if (from.getBalance() < newAmount) {
            ObjectNode node = Errors.insufficientFunds(timestamp);
            from.getOwner().getCommandHistory().addToHistory(node);
            from.addToReport(node);
            return null;
        }

        from.sendMoney(toAccount, amount, commission, description, timestamp);
        ObjectNode result = from.checkForGold(newAmount, exchangeRates, timestamp);
        amount *= exchangeRates.getRate(from.getCurrency(), to.getCurrency());
        to.receiveMoney(fromAccount, amount, description, timestamp);

        if (result != null) {
            from.getOwner().getCommandHistory().addToHistory(result);
        }
        from.setBalance(from.getBalance());
        to.setBalance(to.getBalance());
        return null;
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
            ArrayNode array = user.getCommandHistory().getHistory();
            List<JsonNode> list = new ArrayList<>();
            array.forEach(list::add);

            list.sort(Comparator.comparingLong(node -> node.get("timestamp").asLong()));

            array.removeAll();
            list.forEach(array::add);
            return array;
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
            acc.addToReport(node);
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
            ObjectNode node = acc.addInterest();
            node.put("timestamp", timestamp);
            acc.getOwner().getCommandHistory().addToHistory(node);
            acc.addToReport(node);
        } catch (UnsupportedOperationException e) {
            return Errors.notSavingsAccount(timestamp);
        }
        return null;
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
    public ObjectNode splitPayment(final String type, final List<String> accounts,
                                   final String currency, final double amount,
                                   final List<Double> amountForUsers, final int timestamp,
                                   final int id) {
        SplitPayment splitPayment = new SplitPayment(type, accounts, amount, currency,
                amountForUsers, exchangeRates, timestamp);
        for (String account : accounts) {
            Account acc = Search.getAccountByIBAN(users, account);
            if (acc == null) {
                return Errors.invalidAccountForSplit(timestamp);
            }
            splitPayment.addObserver(acc.getOwner());
        }

        for (String account : accounts) {
            Account acc = Search.getAccountByIBAN(users, account);
            User user = acc.getOwner();
            splitPayment.getUserStatuses().put(acc, SplitPaymentStatus.PENDING);
            SplitPaymentInfo splitPaymentInfo = new SplitPaymentInfo(splitPayment, acc, id);
            user.getSplitPaymentQueue().add(splitPaymentInfo);
        }
        return null;
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
                                final int endTimestamp, final int timestamp) {
        Account acc = Search.getAccountByIBAN(users, account);
        if (acc == null) {
            return Errors.accountNotFound(timestamp);
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
                                         final int endTimestamp, final int timestamp) {
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        Account acc = Search.getAccountByIBAN(users, account);
        if (acc == null) {
            return Errors.accountNotFound(timestamp);
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

    /**
     * Processes a savings account withdrawal for a specified account.
     *
     * @param account    the IBAN of the savings account to withdraw from
     * @param amount     the amount to withdraw
     * @param currency   the currency of the withdrawal amount
     * @param timestamp  the timestamp of the withdrawal request
     */
    public void withdrawSavings(final String account, double amount, final String currency,
                                final int timestamp) {
        Account acc = Search.getAccountByIBAN(users, account);
        if (acc == null) {
            return;
        }
        amount *= exchangeRates.getRate(currency, acc.getCurrency());
        acc.getOwner().withdrawSavings(acc, amount, currency, timestamp);
    }

    /**
     * Upgrades the service plan of a specified account to a new plan type.
     *
     * @param account      the IBAN of the account to upgrade
     * @param newPlanType  the new plan type to upgrade to (e.g., "silver", "gold")
     * @param timestamp    the timestamp of the upgrade request
     * @return {@code null} on successful upgrade,
     * or an error {@link ObjectNode} if the account is not found
     */
    public ObjectNode upgradePlan(final String account, final String newPlanType,
                                  final int timestamp) {
       Account acc = Search.getAccountByIBAN(users, account);
       if (acc == null) {
           return Errors.accountNotFound(timestamp);
       }
       double rate = exchangeRates.getRate(Utils.DEFAULT_CURRENCY, acc.getCurrency());
       acc.getOwner().upgradePlan(acc, ServicePlan.valueOf(newPlanType.toUpperCase()),
                                    rate, timestamp);
         return null;
    }

    /**
     * Processes a cash withdrawal from a specified card.
     *
     * @param cardNumber the card number from which the withdrawal is requested
     * @param amount     the amount to withdraw
     * @param email      the email of the user requesting the withdrawal
     * @param timestamp  the timestamp of the withdrawal request
     * @return {@code null} on successful withdrawal,
     * or an error {@link ObjectNode} if the user or card is invalid
     */
    public ObjectNode cashWithdrawal(final String cardNumber, final double amount,
                                     final String email, final int timestamp) {
        User user = Search.getUserByEmail(users, email);
        if (user == null) {
            return Errors.userNotFound(timestamp);
        }
        Card card = Search.getCardByNumber(users, cardNumber);
        if (card == null || !card.getAccountBelonging().getOwner().getEmail().equals(email)) {
            return Errors.cardNotFound(timestamp);
        }

        ObjectNode inner = card.getAccountBelonging().cashWithdrawal(card,
                amount, timestamp, exchangeRates);
        card.getAccountBelonging().setBalance(card.getAccountBelonging().getBalance());
        card.getAccountBelonging().getOwner().getCommandHistory().addToHistory(inner);
        return null;
    }

    /**
     * Creates a JSON representation of all users in the system.
     *
     * @return an {@link ArrayNode} containing the JSON data of all users
     */
    public ArrayNode printUsers() {
        ArrayNode array = JsonNodeFactory.instance.arrayNode();
        for (User user : users) {
            array.add(user.getJson());
        }
        return array;
    }

    /**
     * Processes the acceptance of a split payment by a user.
     *
     * @param email     the email of the user accepting the split payment
     * @param timestamp the timestamp of the acceptance
     * @param type      the type of split payment (e.g., "custom" or "equal")
     * @return {@code null} on successful acceptance,
     * or an error {@link ObjectNode} if the user is not found
     */
    public ObjectNode acceptSplitPayment(final String email, final int timestamp,
                                         final String type) {
        User user = Search.getUserByEmail(users, email);
        if (user == null) {
            return Errors.userNotFound(timestamp);
        }
        user.handleSplitPayment(SplitPaymentStatus.ACCEPTED, type);
        return null;
    }

    /**
     * Processes the rejection of a split payment by a user.
     *
     * @param email     the email of the user rejecting the split payment
     * @param timestamp the timestamp of the rejection
     * @param type      the type of split payment (e.g., "custom" or "equal")
     * @return {@code null} on successful rejection,
     * or an error {@link ObjectNode} if the user is not found
     */
    public ObjectNode rejectSplitPayment(final String email, final int timestamp,
                                         final String type) {
        User user = Search.getUserByEmail(users, email);
        if (user == null) {
            return Errors.userNotFound(timestamp);
        }
        user.handleSplitPayment(SplitPaymentStatus.REJECTED, type);
        return null;
    }

    /**
     * Adds a new business associate to a specified business account.
     *
     * @param account   the IBAN of the business account
     * @param role      the role of the new associate (e.g., "manager" or "employee")
     * @param email     the email of the user to be added as an associate
     * @param timestamp the timestamp of the action
     */
    public void addNewBussinessAssociate(final String account, final String role,
                                         final String email, final int timestamp) {
        Account acc = Search.getAccountByIBAN(users, account);
        if (acc == null) {
            return;
        }
        acc.addNewBusinessAssociate(email, role, timestamp, this);
    }

    /**
     * Changes the spending limit for a specified business account.
     *
     * @param email     the email of the user requesting the change
     * @param account   the IBAN of the account for which the spending limit is to be changed
     * @param amount    the new spending limit amount
     * @param timestamp the timestamp of the request
     * @return an {@link ObjectNode} representing the result of the operation,
     *         or an error if the user or account is invalid or the account is not of type business
     */
    public ObjectNode changeSpendingLimit(final String email, final String account,
                                          final double amount, final int timestamp) {
        User user = Search.getUserByEmail(users, email);
        if (user == null) {
            return Errors.userNotFound(timestamp);
        }
        Account acc = Search.getAccountByIBAN(users, account);
        if (acc == null) {
            return Errors.accountNotFound(timestamp);
        }
        try {
            return acc.changeSpendingLimit(amount, email, timestamp);
        } catch (UnsupportedOperationException e) {
            return Errors.notBusinessAccount(timestamp);
        }
    }

    /**
     * Changes the deposit limit for a specified business account.
     *
     * @param email     the email of the user requesting the change
     * @param account   the IBAN of the account for which the deposit limit is to be changed
     * @param amount    the new deposit limit amount
     * @param timestamp the timestamp of the request
     * @return an {@link ObjectNode} representing the result of the operation,
     *         or an error if the user or account is invalid or the account is not of type business
     */
    public ObjectNode changeDepositLimit(final String email, final String account,
                                         final double amount, final int timestamp) {
        User user = Search.getUserByEmail(users, email);
        if (user == null) {
            return Errors.userNotFound(timestamp);
        }
        Account acc = Search.getAccountByIBAN(users, account);
        if (acc == null) {
            return Errors.accountNotFound(timestamp);
        }
        try {
            return acc.changeDepositLimit(amount, email, timestamp);
        } catch (UnsupportedOperationException e) {
            return Errors.notBusinessAccount(timestamp);
        }
    }

    /**
     * Generates a business report for a specified account within a given time range.
     *
     * @param type          the type of report (e.g., transaction statistics or commerciant report)
     * @param startTimestamp the start timestamp of the report range
     * @param endTimestamp   the end timestamp of the report range
     * @param account        the IBAN of the business account
     * @param timestamp      the timestamp of the report request
     * @return an {@link ObjectNode} containing the business report,
     * or an error if the account is invalid
     */
    public ObjectNode businessReport(final String type, final int startTimestamp,
                                     final int endTimestamp, final String account,
                                     final int timestamp) {
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        Account acc = Search.getAccountByIBAN(users, account);
        if (acc == null) {
            return Errors.accountNotFound(timestamp);
        }
        try {
            node = acc.getBusinessReport(startTimestamp, endTimestamp, type);

        } catch (UnsupportedOperationException e) {
            node.put("error", "Account is not of type business");
        }
        return node;
    }

    /**
     * Removes the specified account from the user who owns it.
     *
     * @param a the {@link Account} to remove
     */
    public void removeAccount(final Account a) {
        for (User u : users) {
            if (u.getAccounts().contains(a)) {
                u.getAccounts().remove(a);
                return;
            }
        }
    }
}
