package org.poo.main.moneyback;

import org.poo.main.Application;
import org.poo.main.Commerciant;
import org.poo.main.ServicePlan;
import org.poo.utils.Utils;

import java.util.HashMap;
import java.util.Map;

public class CashbackService {
    private final Application app;
    private final Map<String, Integer> nrOfTransactions = new HashMap<>();
    private double totalAmount = 0;
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

    public void incrementAmount(String commerciant, double amount) {
        totalAmount += amount;
    }

    public void addTransactionToCommerciant(String commerciant, double amount) {
        Commerciant c = getCommerciant(commerciant);
        if (c.getCashbackStrategy().equals("nrOfTransactions")) {
            incrementTransactions(commerciant, amount);
            checkIfCashbackToBeReceivedTransactions(c);
            return;
        } else if (c.getCashbackStrategy().equals("spendingThreshold")) {
            incrementAmount(commerciant, amount);
            return;
        }

    }

    public void checkIfCashbackToBeReceivedTransactions(Commerciant commerciant) {
        if (allCashbackUsed())
            return;
        checkForCashbackByType(commerciant, "Food", Utils.FOOD_DISCOUNT);
        checkForCashbackByType(commerciant, "Clothes", Utils.CLOTHES_DISCOUNT);
        checkForCashbackByType(commerciant, "Tech", Utils.TECH_DISCOUNT);
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
        return usedCashback.size() == Utils.TOTAL_DISCOUNTS;
    }

    // Can be applied to a commerciant with any cashback strategy, as long as the discount is available
    public double giveCashbackForTransactions(String name, double amount) {
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

    public double giveCashback(String name, double amount, ServicePlan type) {
        Commerciant commerciant = getCommerciant(name);
        if (commerciant == null) {
            return 0;
        }
        if (commerciant.getCashbackStrategy().equals("spendingThreshold")) {
            return giveCashbackForAmount(name, amount, type);
        } else if (commerciant.getCashbackStrategy().equals("nrOfTransactions")) {
            return giveCashbackForTransactions(name, amount);
        }
        return 0;
    }

    // Can be applied only to commerciants with spendingThreshold cashback strategy
    public double giveCashbackForAmount(String name, double amount, ServicePlan type) {
        Commerciant commerciant = getCommerciant(name);
        if (commerciant == null) {
            return 0;
        }
        Commerciant c = getCommerciant(name);
        if (c.getCashbackStrategy().equals("nrOfTransactions")) {
            return 0;
        }
        switch(type) {
            case STUDENT:
            case STANDARD:
                if (totalAmount >= 500) {
                    return amount * 0.0025;
                } else if (totalAmount >= 300) {
                    return amount * 0.002;
                } else if (totalAmount >= 100) {
                    return amount * 0.001;
                }
            case SILVER:
                if (totalAmount >= 500) {
                    return amount * 0.005;
                } else if (totalAmount >= 300) {
                    return amount * 0.004;
                } else if (totalAmount >= 100) {
                    return amount * 0.003;
                }
            case GOLD:
                if (totalAmount >= 500) {
                    return amount * 0.007;
                } else if (totalAmount >= 300) {
                    return amount * 0.0055;
                } else if (totalAmount >= 100) {
                    return amount * 0.005;
                }
        }
        return 0;
    }
}
