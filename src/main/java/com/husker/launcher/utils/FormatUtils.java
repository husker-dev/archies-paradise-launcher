package com.husker.launcher.utils;


import java.util.Arrays;
import java.util.List;

public class FormatUtils {

    public static boolean isCorrectName(String name){
        if(name == null || name.length() < 4)
            return false;

        List<String> nums = Arrays.asList("1","2","3","4","5","6","7","8","9","0");
        if(nums.contains(name.substring(0, 1)))
            return false;

        for(String word : new String[]{",", "/", "?", "'", "\"", "\\", "|", "%", "&"})
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
