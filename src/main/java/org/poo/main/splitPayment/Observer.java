package org.poo.main.splitPayment;

/**
 * Observer interface
 */
public interface Observer {

    /**
     * Update method
     * @param splitPayment the split payment
     */
    void update(SplitPayment splitPayment);
}
