package com.husker.launcher.server.utils;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ConsoleUtils {


    public static void printDebug(Class<?> clazz, Object text){
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();

        System.out.println("(" + dtf.format(now) + ") [" + clazz.getSimpleName() + "] " + text);
    }

    public static String repeat(String text, int count){
        StringBuilder builder = new StringBuilder();
        for(int i = 0; i < count; i++)
            builder.append(text);
        return builder.toString();
    }
}
