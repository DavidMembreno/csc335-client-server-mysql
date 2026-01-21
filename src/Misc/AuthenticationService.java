package Misc;

import java.util.regex.*;

public class AuthenticationService {
    private static String emailregex = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
	private static Pattern emailpattern = Pattern.compile(emailregex);

    public static boolean authenticate(String username, String password, String[] usernames, String[] passwords) {
        // Placeholder authentication logic
        for (int i = 0; i < usernames.length; i++) {
            if (usernames[i].equals(username) && passwords[i].equals(password)) {
                return true;
            }
        }
        return false;
    }

    public static boolean validateEmailAddress(String email){
        Matcher matcher = emailpattern.matcher(email);
        return matcher.find();
    }

    public static boolean isLockedOut(String username, String[] usernames, int[] lockedOutStatus) {
        for (int i = 0; i < usernames.length; i++) {
            if (usernames[i].equals(username)) {
                return lockedOutStatus[i] == 1;
            }
        }
        return false;
    }
}
