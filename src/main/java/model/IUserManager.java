package model;

public interface IUserManager {

    boolean registerUser(String username, String password);

    boolean checkIfPasswordsAreTheSame(String password, String confirmPassword);

    boolean checkIfPasswordsMatchRegex(String password, String confirmPassword);

    boolean loginUser(String username, String password);

    boolean checkIfUserExist(String username);
}
