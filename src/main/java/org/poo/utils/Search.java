package org.poo.utils;

import org.poo.main.Commerciant;
import org.poo.main.accounts.Account;
import org.poo.main.cardTypes.Card;
import org.poo.main.User;
import java.util.List;

/**
 * Utility class that provides static methods for searching users, accounts, and cards
 * from a list of users.
 */
public final class Search {

    /**
     * Private constructor to prevent instantiation of the utility class.
     */
    private Search() {
    }

    /**
     * Retrieves a {@link User} by their email from a list of users.
     *
     * @param users the list of users to search through
     * @param email the email address of the user to find
     * @return the {@link User} with the specified email, or {@code null} if no user is found
     */
    public static User getUserByEmail(final List<User> users, final String email) {
        for (User user : users) {
            if (user.getEmail().equals(email)) {
                return user;
            }
        }
        return null;
    }

    /**
     * Retrieves an {@link Account} by its IBAN from a list of users.
     *
     * @param users the list of users to search through
     * @param iban the IBAN of the account to find
     * @return the {@link Account} with the specified IBAN, or {@code null} if no account is found
     */
    public static Account getAccountByIBAN(final List<User> users, final String iban) {
        for (User user : users) {
            for (Account account : user.getAccounts()) {
                if (account.getIban().equals(iban)) {
                    return account;
                }
            }
        }
        return null;
    }

    /**
     * Retrieves a {@link Commerciant} by its IBAN from a list of commerciants.
     * @param commerciants the list of commerciants to search through
     * @param iban the IBAN of the commerciant to find
     * @return the {@link Commerciant} with the specified IBAN,
     * or {@code null} if no commerciant is found
     */
    public static Commerciant getCommerciantByIban(final List<Commerciant> commerciants,
                                                   final String iban) {
        for (Commerciant commerciant : commerciants) {
            if (commerciant.getAccount().equals(iban)) {
                return commerciant;
            }
        }
        return null;
    }

    /**
     * Retrieves a {@link Commerciant} by its name from a list of commerciants.
     * @param commerciants the list of commerciants to search through
     * @param name the name of the commerciant to find
     * @return the {@link Commerciant} with the specified name,
     * or {@code null} if no commerciant is found
     */
    public static Commerciant getCommerciantByName(final List<Commerciant> commerciants,
                                                   final String name) {
        for (Commerciant commerciant : commerciants) {
            if (commerciant.getName().equals(name)) {
                return commerciant;
            }
        }
        return null;
    }

    /**
     * Retrieves an {@link Account} by its alias from a list of users.
     *
     * @param users the list of users to search through
     * @param alias the alias of the account to find
     * @return the {@link Account} with the specified alias, or {@code null} if no account is found
     */
    public static Account getAccountByAlias(final List<User> users, final String alias) {
        for (User user : users) {
            for (Account account : user.getAccounts()) {
                if (account.getAlias().equals(alias)) {
                    return account;
                }
            }
        }
        return null;
    }

    /**
     * Retrieves an {@link Account} by the associated {@link Card}'s number from a list of users.
     *
     * @param users the list of users to search through
     * @param cardNumber the card number of the card to find
     * @return the {@link Account} associated with the card number,
     * or {@code null} if no account is found
     */
    public static Account getAccountByCard(final List<User> users, final String cardNumber) {
        for (User user : users) {
            for (Account account : user.getAccounts()) {
                for (Card card : account.getCards()) {
                    if (card.getCardNumber().equals(cardNumber)) {
                        return account;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Retrieves a {@link Card} by its card number from a list of users.
     *
     * @param users the list of users to search through
     * @param cardNumber the card number of the card to find
     * @return the {@link Card} with the specified card number, or {@code null} if no card is found
     */
    public static Card getCardByNumber(final List<User> users, final String cardNumber) {
        for (User user : users) {
            for (Account account : user.getAccounts()) {
                for (Card card : account.getCards()) {
                    if (card.getCardNumber().equals(cardNumber)) {
                        return card;
                    }
                }
            }
        }
        return null;
    }
}
