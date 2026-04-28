package commands;

import model.OrderBookManager;
import model.UserManager;
import service.UserUtilities;

public class LoginCommand implements Command {

    private final UserManager userManager;
    private final String username;
    private final String password;
    private boolean result;

    private String response;

    public LoginCommand(UserManager userManager, String username, String password) {
        this.userManager = userManager;
        this.username = username;
        this.password = password;
    }

    @Override
    public void execute() {

        if (!username.isEmpty()) {
            if (!password.isEmpty()) {

                boolean loginSuccess = userManager.loginUser(username, password);

                if (loginSuccess) {
                    response = UserUtilities.LOGIN_SUCCESSFUL + "%%Login successful";
                    result = true;
                } else {
                    response = UserUtilities.INVALID + "%%Invalid username or password";
                    result = false;
                }

            } else {
                response = UserUtilities.PASSWORD_EMPTY + "%%Password cannot be empty";
                result = false;
            }
        } else {
            response = UserUtilities.LOGIN_FAILED + "%%Username cannot be empty";
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
