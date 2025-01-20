package org.poo.main.splitPayment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.Getter;
import org.poo.main.ExchangeRatesGraph;
import org.poo.main.accounts.Account;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a split payment operation involving multiple accounts.
 * <p>
 * This class manages the statuses of user payments, notifies observers, and
 * verifies whether all users have accepted or rejected the payment.
 */
@Getter
public class SplitPayment {
    private final List<Observer> observers = new ArrayList<>();
    private final Map<Account, SplitPaymentStatus> userStatuses = new HashMap<>();
    private boolean allAccepted = false;
    private boolean rejected = false;

    private final String splitPaymentType;
    private final List<String> accounts;
    private final double totalAmount;
    private final String currency;
    private final List<Double> amountForUser;
    private final ExchangeRatesGraph exchangeRates;
    private String accountToBlame = "";
    private final int timestamp;

    /**
     * Constructs a new {@code SplitPayment}.
     *
     * @param splitPaymentType the type of split payment ("custom" or "equal")
     * @param accounts         the list of account IBANs involved in the payment
     * @param amount           the total totalAmount to split
     * @param currency         the currency of the payment
     * @param amountForUser    the amounts assigned to each user (for custom split)
     * @param exchangeRates    the exchange rates for currency conversion
     * @param timestamp        the timestamp of the split payment
     */
    public SplitPayment(final String splitPaymentType, final List<String> accounts,
                        final double amount, final String currency,
                        final List<Double> amountForUser, final ExchangeRatesGraph exchangeRates,
                        final int timestamp) {
        this.splitPaymentType = splitPaymentType;
        this.accounts = accounts;
        this.totalAmount = amount;
        this.currency = currency;
        this.amountForUser = initialiseAmountForUser(amountForUser);
        this.exchangeRates = exchangeRates;
        this.timestamp = timestamp;
    }

    /**
     * Adds an observer to the split payment.
     *
     * @param observer the observer to add
     */
    public void addObserver(final Observer observer) {
        observers.add(observer);
    }

    /**
     * Notifies all observers of the split payment about a status update.
     */
    public void notifyObservers() {
        for (Observer observer : observers) {
            observer.update(this);
        }
    }

    /**
     * Updates the payment status for a specific account.
     *
     * @param account the account whose status is updated
     * @param status  the new status of the payment
     */
    public void updatePaymentStatus(final Account account, final SplitPaymentStatus status) {
        if (status == SplitPaymentStatus.REJECTED) {
            rejected = true;
            notifyObservers();
            return;
        }
        userStatuses.put(account, status);
        checkPaymentStatus();
    }

    /**
     * Checks the overall status of the split payment and verifies whether all users
     * have accepted the payment or any user has insufficient funds.
     */
    private void checkPaymentStatus() {
        allAccepted = true;
        for (SplitPaymentStatus status : userStatuses.values()) {
            if (status == SplitPaymentStatus.PENDING) {
                allAccepted = false;
                return;
            }
        }
        accountToBlame = checkIfAllUsersHaveEnoughMoney();
        notifyObservers();
    }

    /**
     * Checks if all users have sufficient funds for their assigned split totalAmount.
     *
     * @return the IBAN of the first account with insufficient funds,
     * or an empty string if all users have sufficient funds
     */
    private String checkIfAllUsersHaveEnoughMoney() {
        int i = 0;
        for (String accountIban : accounts) {
            Account account = getAccountByIban(accountIban);
            double amount = amountForUser.get(i++);
            double newAmount = amount * exchangeRates.getRate(currency, account.getCurrency());

            if (account.getBalance() < newAmount) {
                return account.getIban();
            }
        }
        return "";
    }

    /**
     * Initializes the amounts for users based on the split type.
     * <p>
     * For "equal" split, amounts are distributed evenly.
     * For "custom" split, the provided amounts are used.
     *
     * @param amounts the custom split amounts
     * @return the initialized list of amounts for users
     */
    private List<Double> initialiseAmountForUser(List<Double> amounts) {
        if (splitPaymentType.equals("custom")) {
            return amounts;
        }

        amounts = new ArrayList<>();
        for (int i = 0; i < accounts.size(); i++) {
            amounts.add(totalAmount / accounts.size());
        }
        return amounts;
    }

    /**
     * Converts the list of account IBANs to an {@link ArrayNode}.
     *
     * @return an {@link ArrayNode} containing the account IBANs
     */
    public ArrayNode getAccountsArray() {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.valueToTree(accounts);
    }

    /**
     * Converts the list of user amounts to an {@link ArrayNode}.
     *
     * @return an {@link ArrayNode} containing the amounts for each user
     */
    public ArrayNode getAmountsArray() {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.valueToTree(amountForUser);
    }

    /**
     * Retrieves an account by its IBAN.
     *
     * @param account the IBAN of the account to retrieve
     * @return the {@link Account} object, or {@code null} if not found
     */
    public Account getAccountByIban(final String account) {
        for (Account acc : userStatuses.keySet()) {
            if (acc.getIban().equals(account)) {
                return acc;
            }
        }
        return null;
    }
}
