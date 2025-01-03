package org.poo.main;

import lombok.Getter;
import lombok.Setter;
import org.poo.fileio.CommerciantInput;

@Getter @Setter
public class Commerciant {
    private final String name;
    private final int id;
    private final String account;
    private final String type;
    private final String cashbackStrategy;

    public Commerciant(final CommerciantInput input) {
        this.name = input.getCommerciant();
        this.id = input.getId();
        this.account = input.getAccount();
        this.type = input.getType();
        this.cashbackStrategy = input.getCashbackStrategy();
    }
}
