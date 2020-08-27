package com.husker.launcher.updater;


public class ConsoleUtils {

    private static String lastDebug = null;

    public static void printDebug(Class<?> clazz, Object text){
        if(lastDebug != null)
            System.out.println();
        lastDebug = "[" + clazz.getSimpleName() + "] " + text;
        System.out.print(lastDebug);
    }

    public static void printResult(String result){
        System.out.println(repeat(".", (lastDebug != null ? Math.max(0, 70 - lastDebug.length()) : 0)) + "[" + result + "]");

        lastDebug = null;
    }

    public static String repeat(String text, int count){
        StringBuilder builder = new StringBuilder();
        for(int i = 0; i < count; i++)
            builder.append(text);
        return builder.toString();
    }
}
