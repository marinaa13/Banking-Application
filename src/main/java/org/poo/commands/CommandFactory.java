package org.poo.commands;

import org.poo.fileio.CommandInput;
import org.poo.main.Application;

/**
 * Factory class responsible for creating commands based on the provided {@link CommandInput}.
 * <p>
 * This class is used to generate the appropriate command object based on the input received.
 * Each command is created based on its type, and the corresponding class for the command is
 * instantiated. The factory ensures that the appropriate command is created and initialized
 * with the necessary data.
 */
public class CommandFactory {

    /**
     * Creates a command based on the given {@link CommandInput} and {@link Application} instance.
     * <p>
     * This method uses a switch expression to determine the correct type of command to create
     * based on the input command string. It returns an instance of the corresponding command
     * class, initialized with the input and application instance.
     *
     * @param input the {@link CommandInput} containing the details needed to create the command
     * @param app the {@link Application} instance to interact with the application logic
     * @return a {@link Command} instance corresponding to the input command,
     * or {@code null} if the command is not recognized
     */
    public Command createCommand(final CommandInput input, final Application app) {
        return switch (input.getCommand()) {
            case "printUsers" -> new PrintUsers(app, input);
            case "printTransactions" -> new PrintTransactions(app, input);
            case "addAccount" -> new AddAccount(app, input);
            case "createCard", "createOneTimeCard" -> new CreateCard(app, input);
            case "deleteAccount" -> new DeleteAccount(app, input);
            case "deleteCard" -> new DeleteCard(app, input);
            case "addFunds" -> new AddFunds(app, input);
            case "setMinimumBalance" -> new SetMinBalance(app, input);
            case "checkCardStatus" -> new CheckCardStatus(app, input);
            case "payOnline" -> new PayOnline(app, input);
            case "splitPayment" -> new SplitPayment(app, input);
            case "sendMoney" -> new SendMoney(app, input);
            case "setAlias" -> new SetAlias(app, input);
            case "addInterest" -> new AddInterest(app, input);
            case "changeInterestRate" -> new ChangeInterestRate(app, input);
            case "report" -> new Report(app, input);
            case "spendingsReport" -> new SpendingsReport(app, input);
            case "upgradePlan" -> new UpgradePlan(app, input);
            case "withdrawSavings" -> new WithdrawSavings(app, input);
            case "cashWithdrawal" -> new CashWithdrawal(app, input);
            case "acceptSplitPayment" -> new AcceptSplitPayment(app, input);
            case "rejectSplitPayment" -> new RejectSplitPayment(app, input);
            case "addNewBusinessAssociate" -> new AddNewBusinessAssociate(app, input);
            case "changeSpendingLimit" -> new ChangeSpendingLimit(app, input);
            case "changeDepositLimit" -> new ChangeDepositLimit(app, input);
            case "businessReport" -> new BusinessReport(app, input);
            default -> new Default();
        };
    }
}
