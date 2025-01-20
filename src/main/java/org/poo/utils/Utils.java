package org.poo.utils;

import java.math.BigDecimal;
import java.util.Random;

/**
 * Utility class providing helper methods for generating IBANs, card numbers,
 * split payment IDs, and performing precise arithmetic operations.
 * <p>
 * This class cannot be instantiated as its constructor is private.
 */
public final class Utils {
    private Utils() {
        // Prevent instantiation
    }

    private static final int IBAN_SEED = 1;
    private static final int CARD_SEED = 2;
    private static final int SPLIT_PAYMENT_SEED = 3;

    private static final int DIGIT_BOUND = 10;
    private static final int DIGIT_GENERATION = 16;
    private static final int DIGIT_SPLIT = 1000;
    private static final String RO_STR = "RO";
    private static final String POO_STR = "POOB";
    public static final String DEFAULT_CURRENCY = "RON";
    public static final int TOTAL_DISCOUNTS = 3;
    public static final int FOOD_DISCOUNT = 2;
    public static final int CLOTHES_DISCOUNT = 5;
    public static final int TECH_DISCOUNT = 10;

    public static final int THRESHOLD_100 = 100;
    public static final int THRESHOLD_300 = 300;
    public static final int THRESHOLD_500 = 500;
    public static final double SILVER_COMM = 1.001;
    public static final double STANDARD_COMM = 1.002;
    public static final double ROUNDING_HELPER = 1000000.0;
    public static final int PLAN_FEE_100 = 100;
    public static final int PLAN_FEE_250 = 250;
    public static final int PLAN_FEE_350 = 350;
    public static final int MIN_AGE = 21;
    public static final int CURR_YEAR = 2024;
    public static final int NUM_PAYMENTS_FOR_GOLD = 5;

    public static final double BIG_CASHBACK_STD = 0.0025;
    public static final double BIG_CASHBACK_SILVER = 0.005;
    public static final double BIG_CASHBACK_GOLD = 0.007;

    public static final double MED_CASHBACK_STD = 0.002;
    public static final double MED_CASHBACK_SILVER = 0.004;
    public static final double MED_CASHBACK_GOLD = 0.0055;

    public static final double SMALL_CASHBACK_STD = 0.001;
    public static final double SMALL_CASHBACK_SILVER = 0.003;
    public static final double SMALL_CASHBACK_GOLD = 0.005;

    private static Random ibanRandom = new Random(IBAN_SEED);
    private static Random cardRandom = new Random(CARD_SEED);
    private static Random splitPaymentRandom = new Random(SPLIT_PAYMENT_SEED);

    /**
     * Generates a unique IBAN code.
     *
     * @return a generated IBAN as a {@link String}
     */
    public static String generateIBAN() {
        StringBuilder sb = new StringBuilder(RO_STR);
        for (int i = 0; i < RO_STR.length(); i++) {
            sb.append(ibanRandom.nextInt(DIGIT_BOUND));
        }

        sb.append(POO_STR);
        for (int i = 0; i < DIGIT_GENERATION; i++) {
            sb.append(ibanRandom.nextInt(DIGIT_BOUND));
        }

        return sb.toString();
    }

    /**
     * Generates a unique card number.
     *
     * @return a generated card number as a {@link String}
     */
    public static String generateCardNumber() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < DIGIT_GENERATION; i++) {
            sb.append(cardRandom.nextInt(DIGIT_BOUND));
        }

        return sb.toString();
    }

    /**
     * Generates a random split payment ID.
     *
     * @return a generated split payment ID as an {@code int}
     */
    public static int generateSplitPayment() {
        return splitPaymentRandom.nextInt(DIGIT_SPLIT);
    }

    /**
     * Resets the random generators to their initial seeds.
     * <p>
     * Ensures consistent random output across different runs.
     */
    public static void resetRandom() {
        ibanRandom = new Random(IBAN_SEED);
        cardRandom = new Random(CARD_SEED);
        splitPaymentRandom = new Random(SPLIT_PAYMENT_SEED);
    }

    /**
     * Performs precise arithmetic operations on balance, payment, and cashback values
     * using {@link BigDecimal} for accuracy.
     *
     * @param balance  the current balance
     * @param toPay    the amount to be paid
     * @param cashback the cashback amount to be added
     * @return the result of the precise arithmetic operation
     */
    public static double bigDecimalPrecision(final double balance, final double toPay,
                                             final double cashback) {
        BigDecimal b = new BigDecimal(balance);
        BigDecimal p = new BigDecimal(toPay);
        BigDecimal c = new BigDecimal(cashback);

        BigDecimal result = b.subtract(p).add(c);
        return result.doubleValue();
    }
}
