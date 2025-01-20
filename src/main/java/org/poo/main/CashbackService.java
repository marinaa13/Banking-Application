package org.poo.main;

import org.poo.utils.Utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Service that handles cashback operations for transactions and spending thresholds.
 * <p>
 * The service tracks cashback based on specific commerciant strategies,
 * user transactions, and spending thresholds for different service plans.
 */
public class CashbackService {
    private final Application app;
    private final Map<String, Integer> nrOfTransactions = new HashMap<>();
    private double totalAmount = 0;
    private final Map<String, Double> availableCashback = new HashMap<>();
    private final Map<String, Double> usedCashback = new HashMap<>();

    /**
     * Constructs a {@code CashbackService} for the given application.
     *
     * @param app the {@link Application} instance used to retrieve commerciant details
     */
    public CashbackService(final Application app) {
        this.app = app;
    }

    /**
     * Retrieves a commerciant by name.
     *
     * @param name the name of the commerciant
     * @return the {@link Commerciant} if found, otherwise {@code null}
     */
    public Commerciant getCommerciant(final String name) {
        for (Commerciant commerciant : app.getCommerciants()) {
            if (commerciant.getName().equals(name)) {
                return commerciant;
            }
        }
        return null;
    }

    /**
     * Increments the transaction count for a specific commerciant.
     *
     * @param commerciant the name of the commerciant
     */
    public void incrementTransactions(final String commerciant) {
        nrOfTransactions.put(commerciant, nrOfTransactions.getOrDefault(commerciant, 0) + 1);
    }

    /**
     * Adds the specified amount to the total spending tracked by the service.
     *
     * @param amount the amount to add
     */
    public void incrementAmount(final double amount) {
        totalAmount += amount;
    }

    /**
     * Adds a transaction to a commerciant and checks for cashback eligibility.
     *
     * @param commerciant the name of the commerciant
     * @param amount      the transaction amount
     */
    public void addTransactionToCommerciant(final String commerciant, final double amount) {
        Commerciant c = getCommerciant(commerciant);
        if (c == null) {
            return;
        }

        if ("nrOfTransactions".equals(c.getCashbackStrategy())) {
            incrementTransactions(commerciant);
            checkIfCashbackToBeReceivedTransactions(c);
        } else if ("spendingThreshold".equals(c.getCashbackStrategy())) {
            incrementAmount(amount);
        }
    }

    /**
     * Checks if cashback should be granted based on the number of transactions for a commerciant.
     *
     * @param commerciant the {@link Commerciant} to check
     */
    public void checkIfCashbackToBeReceivedTransactions(final Commerciant commerciant) {
        if (allCashbackUsed()) {
            return;
        }
        checkForCashbackByType(commerciant, "Food", Utils.FOOD_DISCOUNT);
        checkForCashbackByType(commerciant, "Clothes", Utils.CLOTHES_DISCOUNT);
        checkForCashbackByType(commerciant, "Tech", Utils.TECH_DISCOUNT);
    }

    /**
     * Checks for cashback eligibility for a specific commerciant and type.
     *
     * @param commerciant the {@link Commerciant} to check
     * @param type        the type of cashback (e.g., "Food")
     * @param amount      the threshold for granting cashback
     */
    public void checkForCashbackByType(final Commerciant commerciant, final String type,
                                       final double amount) {
        if (nrOfTransactions.getOrDefault(commerciant.getName(), 0) >= amount) {
            if (!availableCashback.containsKey(type) && !usedCashback.containsKey(type)) {
                availableCashback.put(type, amount / 100);
            }
        }
    }

    /**
     * Determines if all cashback types have been used.
     *
     * @return {@code true} if all cashback types are used, {@code false} otherwise
     */
    public boolean allCashbackUsed() {
        return usedCashback.size() == Utils.TOTAL_DISCOUNTS;
    }

    /**
     * Grants cashback based on the number of transactions for a commerciant.
     *
     * @param name   the name of the commerciant
     * @param amount the transaction amount
     * @return the granted cashback amount, or {@code 0} if no cashback is available
     */
    public double giveCashbackForTransactions(final String name, final double amount) {
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

    /**
     * Grants cashback based on spending thresholds for a commerciant and service plan.
     *
     * @param name   the name of the commerciant
     * @param amount the transaction amount
     * @param type   the user's {@link ServicePlan}
     * @return the granted cashback amount, or {@code 0} if no cashback is eligible
     */
    public double giveCashbackForAmount(final String name, final double amount,
                                        final ServicePlan type) {
        Commerciant commerciant = getCommerciant(name);
        if (commerciant == null
                || !"spendingThreshold".equals(commerciant.getCashbackStrategy())) {
            return 0;
        }

        switch (type) {
            case STUDENT:
            case STANDARD:
                if (totalAmount >= Utils.THRESHOLD_500) {
                    return amount * Utils.BIG_CASHBACK_STD;
                } else if (totalAmount >= Utils.THRESHOLD_300) {
                    return amount * Utils.MED_CASHBACK_STD;
                } else if (totalAmount >= Utils.THRESHOLD_100) {
                    return amount * Utils.SMALL_CASHBACK_STD;
                }
            case SILVER:
                if (totalAmount >= Utils.THRESHOLD_500) {
                    return amount * Utils.BIG_CASHBACK_SILVER;
                } else if (totalAmount >= Utils.THRESHOLD_300) {
                    return amount * Utils.MED_CASHBACK_SILVER;
                } else if (totalAmount >= Utils.THRESHOLD_100) {
                    return amount * Utils.SMALL_CASHBACK_SILVER;
                }
            case GOLD:
                if (totalAmount >= Utils.THRESHOLD_500) {
                    return amount * Utils.BIG_CASHBACK_GOLD;
                } else if (totalAmount >= Utils.THRESHOLD_300) {
                    return amount * Utils.MED_CASHBACK_GOLD;
                } else if (totalAmount >= Utils.THRESHOLD_100) {
                    return amount * Utils.SMALL_CASHBACK_GOLD;
                }
            default:
        }
        return 0;
    }
}
