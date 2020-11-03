package com.husker.launcher.updater;


import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

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

    public static void configureLogging(String name){
        try {
            PrintStream stdOut = System.out;
            PrintStream stdErr = System.err;

            Files.createDirectories(Paths.get("./logs"));
            IOUtils.delete("./logs/" + name + "_log.txt");
            Files.createFile(Paths.get("./logs/" + name + "_log.txt"));
            System.setOut(new PrintStream(new OutputStream() {
                private final PrintStream fileOut = new PrintStream(new File("./logs/" + name + "_log.txt"));

                public void write(int b) throws IOException {

                }
                public void write(byte[] bytes) throws IOException {
                    super.write(bytes);
                    fileOut.write(bytes);
                    stdOut.write(bytes);
                }
                public void write(byte[] bytes, int i, int i1) throws IOException {
                    super.write(bytes, i, i1);
                    fileOut.write(bytes, i, i1);
                    stdOut.write(bytes, i, i1);
                }
                public void flush() throws IOException {
                    super.flush();
                    fileOut.flush();
                    stdOut.flush();
                }
                public void close() throws IOException {
                    super.close();
                    fileOut.close();
                }
            }));

            IOUtils.delete("./logs/" + name + "_err.txt");
            Files.createFile(Paths.get("./logs/" + name + "_err.txt"));
            System.setErr(new PrintStream(new OutputStream() {
                private final PrintStream fileOut = new PrintStream(new File("./logs/" + name + "_err.txt"));

                public void write(int b) throws IOException {

                }
                public void write(byte[] bytes) throws IOException {
                    super.write(bytes);
                    fileOut.write(bytes);
                    stdErr.write(bytes);
                }
                public void write(byte[] bytes, int i, int i1) throws IOException {
                    super.write(bytes, i, i1);
                    fileOut.write(bytes, i, i1);
                    stdErr.write(bytes, i, i1);
                }
                public void flush() throws IOException {
                    super.flush();
                    fileOut.flush();
                    stdErr.flush();
                }
                public void close() throws IOException {
                    super.close();
                    fileOut.close();
                }
            }));
        }catch (Exception ex){
            ex.getStackTrace();
        }
    }
}
