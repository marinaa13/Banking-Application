package org.poo.main.splitPayment;

import lombok.Getter;
import lombok.Setter;
import org.poo.main.accounts.Account;

/**
 * Represents information about a specific split payment associated with an account.
 * <p>
 * This class tracks the split payment, its associated account, the user's status for the payment,
 * and a unique identifier for the split payment.
 */
@Getter
@Setter
public class SplitPaymentInfo {
    private SplitPayment splitPayment;
    private Account account;
    private SplitPaymentStatus status;
    private final int id;

    /**
     * Constructs a new {@code SplitPaymentInfo} object with the specified split payment,
     * account, and unique identifier.
     * <p>
     * @param splitPayment the {@link SplitPayment} associated with this information
     * @param account      the {@link Account} involved in the split payment
     * @param id           the unique identifier for this split payment information
     */
    public SplitPaymentInfo(final SplitPayment splitPayment, final Account account, final int id) {
        this.splitPayment = splitPayment;
        this.account = account;
        this.status = splitPayment.getUserStatuses().get(account);
        this.id = id;
    }
}
