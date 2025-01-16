package org.poo.main.userTypes;

import org.poo.fileio.UserInput;
import org.poo.main.Application;

public class Owner extends User {
    /**
     * Constructs a new User using the provided UserInput.
     * Initializes an empty list of accounts and a new command history.
     *
     * @param userInput an instance of UserInput containing user details
     * @param app
     */
    public Owner(UserInput userInput, Application app) {
        super(userInput, app);
    }
}
