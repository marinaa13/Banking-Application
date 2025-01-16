package org.poo.main.splitPayment;

import lombok.Getter;
import lombok.Setter;
import org.poo.main.accounts.Account;

@Getter
@Setter
public class SplitPaymentInfo {
    SplitPayment splitPayment;          //the split payment he s part of
    Account account;                    //the account he s using to pay
    SplitPaymentStatus status;          //the status of the payment
    final int id;                       //the id of the payment

    public SplitPaymentInfo(final SplitPayment splitPayment, final Account account, final int id) {
        this.splitPayment = splitPayment;
        this.account = account;
        this.status = splitPayment.getUserStatuses().get(account);
        this.id = id;
    }
}
