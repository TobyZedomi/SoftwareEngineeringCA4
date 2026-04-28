package commands;

import model.UserManager;
import service.UserUtilities;

public class RegisterCommand implements Command {

    private final UserManager userManager;
    private final String username;
    private final String password;
    private final String confirmPassword;
    private boolean result;
    private String response;

    public RegisterCommand(UserManager userManager, String username, String password, String confirmPassword) {
        this.userManager = userManager;
        this.username = username;
        this.password = password;
        this.confirmPassword = confirmPassword;
    }

    @Override
    public void execute() {

        if (!username.isEmpty()) {
            if (!password.isEmpty()) {
                if (!confirmPassword.isEmpty()) {

                    boolean checkIfUserExist = userManager.checkIfUserExist(username);

                    if (checkIfUserExist == true) {

                        boolean checkIfPasswordsMatch = userManager.checkIfPasswordsAreTheSame(password, confirmPassword);

                        if (checkIfPasswordsMatch == true) {
                            boolean checkPasswordFormat = userManager.checkIfPasswordsMatchRegex(password, confirmPassword);
                            if (checkPasswordFormat == true) {

                                userManager.registerUser(username, password);
                                response = UserUtilities.REGISTER_SUCCESSFUL + "%%Registration successful. Welcome " + username;
                                result = true;
                            } else {
                                response = UserUtilities.INVALID + "%%Password must be at least 8 characters long and include at least one uppercase letter, one lowercase letter, one digit, and one special character";
                                result = false;
                            }
                        } else {
                            response = UserUtilities.PASSWORDS_DONT_MATCH + "%%Passwords do not match";
                            result = false;
                        }

                    } else {
                        response = UserUtilities.USER_ALREADY_EXIST + "%%Username already exists";
                        result = false;
                    }

                } else {
                    response = UserUtilities.INVALID + "%%Confirm password cannot be empty";
                    result = false;
                }

            } else {
                response = UserUtilities.INVALID + "%%Password cannot be empty";
                result = false;
            }
        } else {
            response = UserUtilities.INVALID + "%%Username cannot be empty";
            result = false;
        }
    }

    public boolean getResult() {
        return result;
    }

    public String getResponse() {
        return response;
    }
}
