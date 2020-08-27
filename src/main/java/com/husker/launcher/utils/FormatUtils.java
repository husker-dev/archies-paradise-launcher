package com.husker.launcher.utils;


public class FormatUtils {

    public static boolean isCorrectName(String name){
        if(name == null)
            return false;

        for(String word : new String[]{",", ".", "/", "<", ">", "<", "?", "'", "\"", "\\", "|", "-", "~", "@", "!", "$", "%", "&", " "})
            if (name.contains(word))
                return false;
        return true;
    }

    public static boolean isCorrectEmail(String email){
        return email != null && email.contains("@") && email.contains(".")
                                && !email.startsWith(".") && !email.endsWith(".")
                                && !email.endsWith("@") && !email.startsWith("@")
                                && email.indexOf('@') == email.lastIndexOf('@');
    }

    public static boolean isCorrectPassword(String password){
        return password != null && password.length() >= 5 && !password.contains(" ");
    }
}
