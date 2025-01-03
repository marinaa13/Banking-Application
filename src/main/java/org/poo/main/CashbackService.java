package org.poo.main;

import java.util.HashMap;
import java.util.Map;

public class CashbackService {
    private final Application app;
    private final Map<String, Integer> nrOfTransactions = new HashMap<>();
    private final Map<String, Double> totalAmount = new HashMap<>();
    private final Map<String, Double> availableCashback = new HashMap<>();
    private final Map<String, Double> usedCashback = new HashMap<>();

    public CashbackService(Application app) {
        this.app = app;
    }

    public boolean isCommerciant(String account) {
        for (Commerciant commerciant : app.getCommerciants()) {
            if (commerciant.getAccount().equals(account)) {
                return true;
            }
        }
        return false;
    }

    public Commerciant getCommerciant(String name) {
        if (!isCommerciant(name)) {
            return null;
        }
        for (Commerciant commerciant : app.getCommerciants()) {
            if (commerciant.getName().equals(name)) {
                return commerciant;
            }
        }
        return null;
    }

    public void incrementTransactions(String commerciant, double amount) {
        if (nrOfTransactions.containsKey(commerciant)) {
            nrOfTransactions.put(commerciant, nrOfTransactions.get(commerciant) + 1);

        } else {
            nrOfTransactions.put(commerciant, 1);
        }
    }

    // in ce curency? ron?
    public void incrementAmount(String commerciant, double amount) {
        if (totalAmount.containsKey(commerciant)) {
            totalAmount.put(commerciant, totalAmount.get(commerciant) + amount);
        } else {
            totalAmount.put(commerciant, amount);
        }
    }

    public void addTransactionToCommerciant(String commerciant, double amount) {
        if (!isCommerciant(commerciant)) {
            return;
        }
        for (Commerciant c : app.getCommerciants()) {
            if (c.getCashbackStrategy().equals("nrOfTransactions")) {
                incrementTransactions(commerciant, amount);
                checkIfCashbackToBeReceived(c);
                return;
            } else if (c.getCashbackStrategy().equals("totalAmount")) {
                incrementAmount(commerciant, amount);
                return;
            }
        }
    }

    public void checkIfCashbackToBeReceived(Commerciant commerciant) {
        if (allCashbackUsed())
            return;
        checkForCashbackByType(commerciant, "Food", 2);
        checkForCashbackByType(commerciant, "Clothes", 5);
        checkForCashbackByType(commerciant, "Tech", 10);
    }

    public void checkForCashbackByType(Commerciant commerciant, String type, double amount) {
        if (nrOfTransactions.get(commerciant.getName()) >= amount) {
            if (availableCashback.containsKey(type) || usedCashback.containsKey(type)) {
                return;
            } else {
                availableCashback.put(type, amount / 100);
            }
        }
    }

    public boolean allCashbackUsed() {
        return usedCashback.size() == 3;
    }

    public double giveCashback(String name, double amount) {
        Commerciant commerciant = getCommerciant(name);
        if (commerciant == null) {
            return 0;
        }
        if (availableCashback.containsKey(commerciant.getType())) {
            double cashback = amount * availableCashback.get(commerciant.getType());
            usedCashback.put(commerciant.getType(), cashback);
            availableCashback.remove(commerciant.getType());
            return cashback;
        }
        return 0;
    }
}
