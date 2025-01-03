package org.poo.main;

import lombok.Getter;

@Getter
public enum ServicePlan {
    STANDARD,    // 0.2% fee on transactions
    STUDENT,     // No fee on any transactions
    SILVER,      // 0.1% fee for transactions over 500 RON
    GOLD         // No fee on any transactions
}
