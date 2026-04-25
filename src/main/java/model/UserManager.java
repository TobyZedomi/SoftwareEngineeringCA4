package model;

import org.mindrot.jbcrypt.BCrypt;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UserManager {

    private Map<String, User> users = new ConcurrentHashMap<>();


    public UserManager(){

        bootstrapUserList();
    }

    // check if user already exist
    public boolean checkIfUserExist(String username){
        boolean match = false;
        if(!users.containsKey(username)) {
            match = true;
        }
        return match;
    }

    // register User


    public boolean registerUser(String username, String password){

        User userToBeRegistered = new User(username,hashPassword(password));

        return register(userToBeRegistered);
    }

    private boolean register(User u){
        boolean added = false;
        if(!users.containsKey(u.getUsername())) {
            added = true;
            users.put(u.getUsername(), u);
        }
        return added;
    }


    // check if passwords are the same

    public boolean checkIfPasswordsAreTheSame(String password, String confirmPassword){

        boolean match = false;

        if (password.equals(confirmPassword)){

            match = true;
        }

        return match;
    }


    // check if password match regex

    public boolean checkIfPasswordsMatchRegex(String password, String confirmPassword){

        boolean match = false;

        String pattern = "(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}";

        if (password.matches(pattern) && confirmPassword.matches(pattern)){

            match = true;
        }


        return match;
    }



    /// check if user is logged in

    public boolean loginUser(String username, String password){

        boolean match = false;

        User u = users.get(username);

        if (u == null){

            match = false;
        }

        if (u != null){
            if (checkPassword(password, u.getPassword())){

                match = true;
            }
        }

        return match;
    }

    // Method to fill the list of quotations with a set of initial quotes
    private void bootstrapUserList()
    {


        users.put("toby", new User("toby", hashPassword("Password1@")));
        users.put("sean", new User("sean", hashPassword("Password1@")));
        users.put("adam", new User("adam", hashPassword("Password1@")));

    }

    private static int workload = 12;

    /**
     * Hash the password based
     * @param password_plaintext is the password being hashed
     * @return hashed password
     */

    public static String hashPassword(String password_plaintext) {
        String salt = BCrypt.gensalt(workload);
        String hashed_password = BCrypt.hashpw(password_plaintext, salt);


        return(hashed_password);
    }

    /**
     * Check password matches hash password
     * @param password_plaintext is the password being searched
     * @param stored_hash is the hashed password being searched
     * @return true if tehy match and false if they don't match
     */

    private static boolean checkPassword(String password_plaintext, String stored_hash) {
        boolean password_verified = false;

        if(null == stored_hash || !stored_hash.startsWith("$2a$"))
            throw new java.lang.IllegalArgumentException("Invalid hash provided for comparison");

        password_verified = BCrypt.checkpw(password_plaintext, stored_hash);

        return(password_verified);
    }

}
