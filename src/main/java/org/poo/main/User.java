package org.poo.main;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.Setter;
import org.poo.commands.CommandHistory;
import org.poo.fileio.UserInput;
import org.poo.main.accounts.Account;
import org.poo.main.accounts.BusinessAccount;
import org.poo.main.cardTypes.Card;
import org.poo.main.splitPayment.Observer;
import org.poo.main.splitPayment.SplitPayment;
import org.poo.main.splitPayment.SplitPaymentInfo;
import org.poo.main.splitPayment.SplitPaymentStatus;
import org.poo.utils.Errors;
import org.poo.utils.Utils;

import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Represents a user with personal information, associated accounts, and a command history.
 * <p>
 * This class contains methods for different banking operations
 * and converting the user data to JSON format.
 * The user is identified by their unique email address.
 */
@Getter @Setter
public class User implements Observer {
    private String firstName;
    private String lastName;
    private final String email;
    private final String birthDate;
    private final String occupation;
    private ServicePlan plan;
    private List<Account> accounts;
    private CommandHistory commandHistory;
    private boolean hasClassicAccount;
    private Queue<SplitPaymentInfo> splitPaymentQueue;
    private int numPayments;
    private final Application app;

    private boolean isOwner;
    private boolean isManager;
    private boolean isEmployee;

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
        splitPaymentQueue = new LinkedList<>();
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
            if (account.getOwner().equals(this)) {
                array.add(account.getJson());
            }
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

        if (account.isBusinessAccount()) {
            account.addUser(email, "owner", this);
        }
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        node.put("timestamp", timestamp);
        node.put("description", "New account created");

        if (!accounts.isEmpty()) {
            account.addToReport(node);
        }
        commandHistory.addToHistory(node);
    }

    /**
     * Adds a new business account to the user's list of accounts.
     * @param account the business account to add
     */
    public void addBussinessAccount(final BusinessAccount account) {
        accounts.add(account);
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
                    if (a.isBusinessAccount()) {
                        if (email.equals(a.getOwner().getEmail())) {
                            app.removeAccount(a);
                            return 1;
                        } else {
                            return 0;
                        }
                    }
                    accounts.remove(a);
                    return 1;
                }
            }
        }

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
                acc.addCard(card, email);
                acc.addToReport(node);
            }
        }
    }

    /**
     * Checks if the user is a student based on their occupation.
     * @return true if the user is a student, false otherwise
     */
    public boolean isStudent() {
        return occupation.equals("student");
    }

    /**
     * Computes the user's age based on their birthdate.
     * @return the user's age
     */
    public int getAge() {
        return Utils.CURR_YEAR - Integer.parseInt(birthDate.substring(0, 4));
    }

    /**
     * Deposits a specified amount from a savings account to a classic account.
     * @param acc the account to deposit from
     * @param amount the amount to deposit
     * @param currency the currency of the amount
     * @param timestamp the timestamp when the deposit occurred
     */
    public void withdrawSavings(final Account acc, final double amount, final String currency,
                                final int timestamp) {
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        node.put("timestamp", timestamp);
        if (getFirstClassicAccount(currency) == null) {
            node.put("description", "You do not have a classic account.");
        } else if (getAge() < Utils.MIN_AGE) {
            node.put("description", "You don't have the minimum age required.");
        } else if (!acc.isSavingsAccount()) {
            node.put("description", "Account is not of type savings.");
        } else {
            Account to = getFirstClassicAccount(currency);
            node = makeSavingsWithdrawal(acc, to, amount, currency, timestamp);
        }
        getCommandHistory().addToHistory(node);
        acc.addToReport(node);
        if (node.has("amount")) {
            getCommandHistory().addToHistory(node);
        }
    }

    /**
     * Retrieves the first classic account of the user based on the currency.
     * @param currency the currency of the account
     * @return the first classic account of the user with the specified currency
     */
    public Account getFirstClassicAccount(final String currency) {
        for (Account acc : accounts) {
            if (acc.isClassicAccount() && acc.getCurrency().equals(currency)) {
                return acc;
            }
        }
        return null;
    }

    /**
     * Makes a savings withdrawal from a savings account to a classic account.
     * @param from the account to withdraw from
     * @param to the account to deposit to
     * @param amount the amount to withdraw
     * @param currency the currency of the amount
     * @param timestamp the timestamp when the withdrawal occurred
     * @return a JSON object representing the withdrawal operation
     */
    public ObjectNode makeSavingsWithdrawal(final Account from, final Account to,
                                            final double amount, final String currency,
                                            final int timestamp) {
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        double newAmount = amount * app.getExchangeRates().getRate(currency, from.getCurrency());

        if (from.getBalance() >= newAmount) {
            from.setBalance(from.getBalance() - newAmount);
            to.setBalance(to.getBalance() + amount);
            node.put("amount", amount);
            node.put("classicAccountIBAN", to.getIban());
            node.put("description", "Savings withdrawal");
            node.put("savingsAccountIBAN", from.getIban());
            node.put("timestamp", timestamp);

        } else {
            node = Errors.insufficientFunds(timestamp);
        }
        return node;
    }

    /**
     * Upgrades the user's plan to a new plan type.
     * @param acc the account that makes the payment
     * @param newPlanType the new plan type
     * @param rate the exchange rate
     * @param timestamp the timestamp when the upgrade occurred
     */
    public void upgradePlan(final Account acc, final ServicePlan newPlanType, final double rate,
                            final int timestamp) {
        ObjectNode node;
        if (newPlanType == plan) {
            node = Errors.alreadyOwnedPlan(timestamp, newPlanType.toString().toLowerCase());
        } else if (newPlanType.ordinal() < plan.ordinal()) {
            node = Errors.downgradePlan(timestamp);
        } else {
            node = changePlan(newPlanType, acc, rate, timestamp);
        }
        getCommandHistory().addToHistory(node);
        acc.addToReport(node);
    }

    /**
     * Changes the user's plan to a new plan type.
     * @param newPlanType the new plan type
     * @param acc the account that makes the payment
     * @param rate the exchange rate
     * @param timestamp the timestamp when the change occurred
     * @return a JSON object representing the plan change operation
     */
    private ObjectNode changePlan(final ServicePlan newPlanType, final Account acc,
                                  final double rate, final int timestamp) {
        double amount = 0;
        if ((plan == ServicePlan.STUDENT || plan == ServicePlan.STANDARD)
                && newPlanType == ServicePlan.SILVER) {
            amount = Utils.PLAN_FEE_100 * rate;
        } else if ((plan == ServicePlan.STUDENT || plan == ServicePlan.STANDARD)
                && newPlanType == ServicePlan.GOLD) {
            amount = Utils.PLAN_FEE_350 * rate;
        } else if (plan == ServicePlan.SILVER && newPlanType == ServicePlan.GOLD) {
            amount = Utils.PLAN_FEE_250 * rate;
        }
        amount = Math.round(amount * Utils.ROUNDING_HELPER) / Utils.ROUNDING_HELPER;
        try {
            acc.deductFee(amount);
            ObjectNode node = JsonNodeFactory.instance.objectNode();
            node.put("timestamp", timestamp);
            node.put("description", "Upgrade plan");
            node.put("accountIBAN", acc.getIban());
            node.put("newPlanType", newPlanType.toString().toLowerCase());
            plan = newPlanType;
            return node;
        } catch (Exception e) {
            return Errors.insufficientFunds(timestamp);
        }
    }

    /**
     * Computes the commission for a transaction based on the user's plan.
     * @param amount the amount of the transaction
     * @return the commission for the transaction
     */
    public double getCommission(final double amount) {
        switch (plan) {
            case STANDARD:
                return Utils.STANDARD_COMM;
            case SILVER:
                if (amount > Utils.THRESHOLD_500) {
                    return Utils.SILVER_COMM;
                }
            default:
                return 1;
        }
    }

    /**
     * Processes a split payment across multiple accounts.
     * @param splitPayment the payment to process
     */
    @Override
    public void update(final SplitPayment splitPayment) {
        SplitPaymentInfo currentOp = null;
        for (SplitPaymentInfo splitPaymentInfo : splitPaymentQueue) {
            if (splitPaymentInfo.getSplitPayment() == splitPayment) {
                currentOp = splitPaymentInfo;
                splitPaymentQueue.remove(splitPaymentInfo);
                break;
            }
        }
        Account acc = currentOp.getAccount();

        if (currentOp.getSplitPayment().isRejected()) {

            ObjectNode node = JsonNodeFactory.instance.objectNode();
            node.put("timestamp", splitPayment.getTimestamp());
            String formatted = String.format("%.2f", splitPayment.getTotalAmount());
            node.put("description", "Split payment of " + formatted + " "
                    + splitPayment.getCurrency());
            node.put("splitPaymentType", splitPayment.getSplitPaymentType());
            node.put("currency", splitPayment.getCurrency());
            node.set("amountForUsers", splitPayment.getAmountsArray());
            node.set("involvedAccounts", splitPayment.getAccountsArray());
            node.put("error", "One user rejected the payment.");
            acc.addToReport(node);
            getCommandHistory().addToHistory(node);
            return;
        }

        if (splitPayment.getAccountToBlame().isEmpty()) {
            double amount = splitPayment.getAmountForUser().get(splitPayment.getAccounts()
                    .indexOf(acc.getIban()));
            double newAmount = amount * app.getExchangeRates().getRate(splitPayment.getCurrency(),
                    acc.getCurrency());
            acc.setBalance(acc.getBalance() - newAmount);
            ArrayNode accountsArray = splitPayment.getAccountsArray();
            ArrayNode amountsArray = splitPayment.getAmountsArray();
            ObjectNode node = acc.addSplitTransaction(accountsArray, splitPayment.getCurrency(),
                    splitPayment.getTotalAmount(), splitPayment.getTimestamp(), amountsArray,
                    splitPayment.getSplitPaymentType());
            getCommandHistory().addToHistory(node);
        } else {
            ObjectNode node = JsonNodeFactory.instance.objectNode();
            if (splitPayment.getSplitPaymentType().equals("custom")) {
                node.set("amountForUsers", splitPayment.getAmountsArray());
            } else {
                node.put("amount", splitPayment.getAmountForUser().getFirst());
            }
            node.put("currency", splitPayment.getCurrency());
            String formatted = String.format("%.2f", splitPayment.getTotalAmount());
            node.put("description", "Split payment of " + formatted + " "
                    + splitPayment.getCurrency());
            node.put("error",
                    "Account " + splitPayment.getAccountToBlame()
                            + " has insufficient funds for a split payment.");
            node.set("involvedAccounts", splitPayment.getAccountsArray());
            node.put("splitPaymentType", splitPayment.getSplitPaymentType());
            node.put("timestamp", splitPayment.getTimestamp());
            acc.addToReport(node);
            getCommandHistory().addToHistory(node);
        }
    }

    /**
     * Updates the status of a split payment based on the payment status and type.
     * @param status the new status of the payment
     * @param type the type of the payment
     */
    public void handleSplitPayment(final SplitPaymentStatus status, final String type) {
        for (SplitPaymentInfo splitPaymentInfo : splitPaymentQueue) {
            if (splitPaymentInfo == null) {
                break;
            }
            if (splitPaymentInfo.getStatus() == SplitPaymentStatus.PENDING
                    && splitPaymentInfo.getSplitPayment().getSplitPaymentType().equals(type)) {
                SplitPayment splitPayment = splitPaymentInfo.getSplitPayment();
                splitPayment.updatePaymentStatus(splitPaymentInfo.getAccount(), status);
                splitPaymentInfo.setStatus(status);
                break;
            }
        }
    }
}
