package com.husker.launcher.utils;

import com.husker.launcher.settings.LauncherConfig;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Map;
import java.util.stream.Stream;

public class SystemUtils {

    public static <K,V> void setEnv(final String key, final String value) {
        try {
            final Class<?> processEnvironmentClass = Class.forName("java.lang.ProcessEnvironment");
            final Field theEnvironmentField = processEnvironmentClass.getDeclaredField("theEnvironment");
            final boolean environmentAccessibility = theEnvironmentField.isAccessible();
            theEnvironmentField.setAccessible(true);

            final Map<K,V> env = (Map<K, V>) theEnvironmentField.get(null);

            if (isWindows()) {
                if (value == null) {
                    env.remove(key);
                } else {
                    env.put((K) key, (V) value);
                }
            } else {
                final Class<K> variableClass = (Class<K>) Class.forName("java.lang.ProcessEnvironment$Variable");
                final Method convertToVariable = variableClass.getMethod("valueOf", String.class);
                final boolean conversionVariableAccessibility = convertToVariable.isAccessible();
                convertToVariable.setAccessible(true);

                final Class<V> valueClass = (Class<V>) Class.forName("java.lang.ProcessEnvironment$Value");
                final Method convertToValue = valueClass.getMethod("valueOf", String.class);
                final boolean conversionValueAccessibility = convertToValue.isAccessible();
                convertToValue.setAccessible(true);

                if (value == null) {
                    env.remove(convertToVariable.invoke(null, key));
                } else {
                    env.put((K) convertToVariable.invoke(null, key), (V) convertToValue.invoke(null, value));
                    convertToValue.setAccessible(conversionValueAccessibility);
                    convertToVariable.setAccessible(conversionVariableAccessibility);
                }
            }
            theEnvironmentField.setAccessible(environmentAccessibility);
            final Field theCaseInsensitiveEnvironmentField = processEnvironmentClass.getDeclaredField("theCaseInsensitiveEnvironment");
            final boolean insensitiveAccessibility = theCaseInsensitiveEnvironmentField.isAccessible();
            theCaseInsensitiveEnvironmentField.setAccessible(true);
            final Map<String, String> cienv = (Map<String, String>) theCaseInsensitiveEnvironmentField.get(null);
            if (value == null) {
                cienv.remove(key);
            } else {
                cienv.put(key, value);
            }
            theCaseInsensitiveEnvironmentField.setAccessible(insensitiveAccessibility);
        } catch (final ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException("Failed setting environment variable <"+key+"> to <"+value+">", e);
        } catch (final NoSuchFieldException e) {
            final Map<String, String> env = System.getenv();
            Stream.of(Collections.class.getDeclaredClasses())
                    .filter(c1 -> "java.util.Collections$UnmodifiableMap".equals(c1.getName()))
                    .map(c1 -> {
                        try {
                            return c1.getDeclaredField("m");
                        } catch (final NoSuchFieldException e1) {
                            throw new IllegalStateException("Failed setting environment variable <"+key+"> to <"+value+"> when locating in-class memory map of environment", e1);
                        }
                    })
                    .forEach(field -> {
                        try {
                            final boolean fieldAccessibility = field.isAccessible();
                            field.setAccessible(true);
                            final Map<String, String> map = (Map<String, String>) field.get(env);
                            if (value == null) {
                                // remove if null
                                map.remove(key);
                            } else {
                                map.put(key, value);
                            }
                            field.setAccessible(fieldAccessibility);
                        } catch (final ConcurrentModificationException ignored) {
                        } catch (final IllegalAccessException e1) {
                            throw new IllegalStateException("Failed setting environment variable <"+key+"> to <"+value+">. Unable to access field!", e1);
                        }
                    });
        }
    }


    public static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }

    public static boolean isMac() {
        return System.getProperty("os.name").toLowerCase().contains("mac");
    }

    public static boolean isUnix() {
        String osName = System.getProperty("os.name").toLowerCase();
        return osName.contains("linux") || osName.contains("mpe/ix") || osName.contains("freebsd") || osName.contains("irix") || osName.contains("digital unix") || osName.contains("unix");
    }

    public static String getArch(){
        String arch = System.getProperty("os.arch");
        if(arch.contains("64"))
            arch = "64";
        if(arch.contains("32") || arch.contains("86"))
            arch = "32";
        return arch;
    }

    public static String getOSName(){
        if(isWindows())
            return "windows";
        else if(isMac())
            return "osx";
        else if(isUnix())
            return "linux";
        else
            return "other";
    }

    public static int getJavaVersion() {
        String version = System.getProperty("java.version");
        if(version.startsWith("1.")) {
            version = version.substring(2, 3);
        } else {
            int dot = version.indexOf(".");
            if(dot != -1) { version = version.substring(0, dot); }
        } return Integer.parseInt(version);
    }

    public static String getSettingsFolder(){
        try {
            Process p =  Runtime.getRuntime().exec("reg query \"HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Explorer\\Shell Folders\" /v personal");
            p.waitFor();

            InputStream in = p.getInputStream();
            byte[] b = new byte[in.available()];
            in.read(b);
            in.close();

            return new String(b).split("\\s\\s+")[4] + "/" + LauncherConfig.getFolderName();
        } catch(Throwable t) {
            t.printStackTrace();
        }
        return new File(".").getAbsolutePath();
    }

    public static int getRefreshRate(){
        return GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()[0].getDisplayMode().getRefreshRate();
    }

    public static int getRefreshRate(Component context){
        return context.getGraphicsConfiguration().getDevice().getDisplayMode().getRefreshRate();
    }

    public static double getWindowScaleFactor(GraphicsConfiguration configuration){
        AffineTransform transform = configuration.getDefaultTransform();
        return transform.getScaleX();
    }

    public static double getWindowScaleFactor(){
        if (GraphicsEnvironment.isHeadless())
            return 1.0;
        GraphicsConfiguration gc = GraphicsEnvironment
            .getLocalGraphicsEnvironment()
            .getDefaultScreenDevice()
            .getDefaultConfiguration();
        return getWindowScaleFactor(gc);
    }
}
