package org.poo.commands;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.fileio.CommandInput;
import org.poo.main.Application;
import org.poo.utils.Output;

/**
 * Represents a command to upgrade the plan type for a specific account.
 * <p>
 * This command interacts with the {@link Application} class to change the account's plan
 * to a new type. The command returns {@code null} if the operation is successful
 * or an error message in JSON format if the operation fails.
 */
public class UpgradePlan implements Command {

    private final Application app;
    private final String account;
    private final String newPlanType;
    private final int timestamp;

    /**
     * Constructs an {@code UpgradePlan} command using the provided {@link Application}
     * and {@link CommandInput}.
     *
     * @param app   the {@link Application} instance to interact with
     * @param input the {@link CommandInput} containing the account, new plan type,
     *              and timestamp details
     */
    public UpgradePlan(final Application app, final CommandInput input) {
        this.app = app;
        this.account = input.getAccount();
        this.newPlanType = input.getNewPlanType();
        this.timestamp = input.getTimestamp();
    }

    /**
     * Executes the upgrade plan command by calling the
     * {@link Application#upgradePlan(String, String, int)} method.
     * <p>
     * If the operation is successful, {@code null} is returned.
     * If the operation fails, an {@link ObjectNode} containing the error details is returned.
     *
     */
    @Override
    public ObjectNode execute() {
        ObjectNode inner = app.upgradePlan(account, newPlanType, timestamp);
        if (inner != null) {
            return Output.getCommand("upgradePlan", inner, timestamp);
        }
        return null;
    }
}
