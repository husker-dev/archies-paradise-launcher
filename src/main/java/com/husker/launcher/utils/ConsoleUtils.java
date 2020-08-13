package com.husker.launcher.utils;

import org.apache.commons.lang3.StringUtils;

public class ConsoleUtils {

    private static String lastDebug = null;

    public static void printDebug(Class clazz, String text){
        if(lastDebug != null)
            System.out.println();
        lastDebug = "[" + clazz.getSimpleName() + "] " + text;
        System.out.print(lastDebug);
    }

    public static void printResult(String result){
        System.out.println(StringUtils.repeat(".", (lastDebug != null ? Math.max(0, 70 - lastDebug.length()) : 0)) + "[" + result + "]");

        lastDebug = null;
    }
}
