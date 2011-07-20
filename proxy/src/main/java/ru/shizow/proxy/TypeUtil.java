package ru.shizow.proxy;

public class TypeUtil {
    public static final String PRIM_TYPES = "ZBSIFJD";
    public static final int[] INST_OFFSET = new int[]{0, 0, 0, 0, 2, 1, 3};
    public static final String[] TYPE_NAMES = new String[]{
            "Boolean", "Byte", "Short", "Integer", "Float", "Long", "Double"};
    public static final String[] SHORT_TYPE_NAMES = new String[]{
            "boolean", "byte", "short", "int", "float", "long", "double"};
    public static final int[] SLOT_LEN = new int[]{1, 1, 1, 1, 1, 2, 2};
    public static final Class<?>[] PRIM_CLASSES =
            new Class<?>[]{boolean.class, byte.class, short.class, int.class, float.class, long.class, double.class};
}
