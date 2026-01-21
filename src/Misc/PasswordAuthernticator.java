package Misc;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class PasswordAuthernticator {

    private static String passwordregex = "^(?=.*[0-9])(?=.*[a-z]).{8,}$";
    private static Pattern passwordpattern = Pattern.compile(passwordregex);

    // public static boolean isPasswordStrong(String password) {
    //     if (password.length() < 8) {
    //         return false; // Minimum length requirement
    //     }
    //     int numberOfAlphebet = 0;
    //     int numberOfNumbers = 0;
    //     for (char c : password.toCharArray()) {
    //         if (Character.isUpperCase(c)) {
    //             numberOfAlphebet++;
    //         } else if (Character.isLowerCase(c)) {
    //             numberOfAlphebet++;
    //         } else if (Character.isDigit(c)) {
    //             numberOfNumbers++;
    //         }
    //     }
    //     if (numberOfAlphebet < 1 || numberOfNumbers < 1) {
    //         return false;
    //     }

    //     return true;
    // }

    public static boolean validPassword(String password)
    {
        Matcher matcher = passwordpattern.matcher(password);
        return matcher.find();
    }

    
}
