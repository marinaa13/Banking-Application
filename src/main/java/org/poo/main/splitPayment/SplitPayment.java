package org.poo.main.splitPayment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.Getter;
import org.poo.main.ExchangeRatesGraph;
import org.poo.main.accounts.Account;
import org.poo.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class SplitPayment {
    private List<Observer> observers = new ArrayList<>();
    private Map<Account, SplitPaymentStatus> userStatuses = new HashMap<>();
    private boolean allAccepted = false;
    private boolean rejected = false;

    private final String splitPaymentType;
    private final List<String> accounts;
    private final double amount;
    private final String currency;
    private final List<Double> amountForUser;
    private final ExchangeRatesGraph exchangeRates;
    private String accountToBlame = "";
    private final int timestamp;

    public SplitPayment(String splitPaymentType, List<String> accounts, double amount, String currency, List<Double> amountForUser, ExchangeRatesGraph exchangeRates, int timestamp) {
        this.splitPaymentType = splitPaymentType;
        this.accounts = accounts;
        this.amount = amount;
        this.currency = currency;
        this.amountForUser = initialiseAmountForUser(amountForUser);
        this.exchangeRates = exchangeRates;
        this.timestamp = timestamp;
    }

    // Method to add observers (users involved)
    public void addObserver(Observer observer) {
        observers.add(observer);
    }

    // Method to remove observers (users)
    public void removeObserver(Observer observer) {
        observers.remove(observer);
    }

    // Notify all observers of status update -> they can now try to make the payment
    public void notifyObservers() {
        for (Observer observer : observers) {
            observer.update(this);
        }
    }

    // Update payment status for a user
    public void updatePaymentStatus(Account account, SplitPaymentStatus status) {
        if (status == SplitPaymentStatus.REJECTED) {
            rejected = true;
            notifyObservers();
            return;
        }
        userStatuses.put(account, status);
        checkPaymentStatus();
    }

    // Check if all users have accepted
    private void checkPaymentStatus() {
        allAccepted = true;
        for (SplitPaymentStatus status : userStatuses.values()) {
            if (status == SplitPaymentStatus.PENDING) {
                allAccepted = false;
                return;
            }
        }

        //verific daca se poate face plata - toti userii au destui bani
        accountToBlame = checkIfAllUsersHaveEnoughMoney();
        notifyObservers();
    }

    private String checkIfAllUsersHaveEnoughMoney() {
        int i = 0;
        for (Account account : userStatuses.keySet()) {
            Double amount = amountForUser.get(i++);
            amount *= exchangeRates.getRate(currency, account.getCurrency());
            double ronAmount = amount * exchangeRates.getRate(currency, Utils.DEFAULT_CURRENCY);
            amount *= account.getOwner().getCommission(ronAmount);

            if (account.getBalance() < amount) {
                // big boo boo
                return account.getIban();
            }
        }
        return "";
    }

    private List<Double> initialiseAmountForUser(List<Double> amountForUser) {
        if (splitPaymentType.equals("custom"))
            return amountForUser;

        amountForUser = new ArrayList<>();
        for (int i = 0; i < accounts.size(); i++) {
            amountForUser.add(amount / accounts.size());
        }
        return amountForUser;
    }

    /**
     * Converts a list of account IBANs to an array of JSON objects.
     * @return an {@link ArrayNode} containing the account IBANs as JSON
     */
    public ArrayNode getAccountsArray() {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.valueToTree(accounts);
    }

    public ArrayNode getAmountsArray() {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.valueToTree(amountForUser);
    }
}
