package com.meteor.bookapi;

import com.google.common.annotations.Beta;
import org.bukkit.Bukkit;

@Beta
public class ReflectionUtil {
    private static final String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];

    public static Class<?> getNMSClass(String className, String mojangMap) throws ClassNotFoundException {
        try {
            return java.lang.Class.forName("net.minecraft.server." + ReflectionUtil.version + "." + className);
        } catch (ClassNotFoundException e) {
            return java.lang.Class.forName(mojangMap + "." + className);
        }
    }

    public static Class<?> getOBCClass(String className) throws ClassNotFoundException {
        return java.lang.Class.forName("org.bukkit.craftbukkit." + ReflectionUtil.version + "." + className);
    }

    public static String getVersion() {
        return ReflectionUtil.version;
    }

    static boolean supportedClass(String className) {
        try {
            Class.forName(className);
        } catch (ClassNotFoundException e) {
            return false;
        } return true;
    }

    static boolean supportedMethod(Class<?> clazz, String methodName, Class<?>... params) {
        try {
            clazz.getMethod(methodName, params);
        } catch (NoSuchMethodException e) {
            return false;
        } return true;
    }
}
