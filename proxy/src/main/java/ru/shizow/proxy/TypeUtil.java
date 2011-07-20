package ru.shizow.proxy;

/**
 * @author Max Gorbunov
 */
public class TypeUtil {
    /**
     * One-character names for primitive types.
     */
    public static final String PRIM_TYPES = "ZBSIFJD";
    /**
     * Offsets for xLOAD and xRETURN instructions based on ILOAD and IRETURN.
     */
    public static final int[] INST_OFFSET = new int[]{0, 0, 0, 0, 2, 1, 3};
    /**
     * Boxed types names.
     */
    public static final String[] TYPE_NAMES = new String[]{
            "Boolean", "Byte", "Short", "Integer", "Float", "Long", "Double"};
    /**
     * Primitive types names.
     */
    public static final String[] SHORT_TYPE_NAMES = new String[]{
            "boolean", "byte", "short", "int", "float", "long", "double"};
    /**
     * The numbers of stack slots taken by types.
     */
    public static final int[] SLOT_LEN = new int[]{1, 1, 1, 1, 1, 2, 2};
    /**
     * Primitive {@link Class}es.
     */
    public static final Class<?>[] PRIM_CLASSES =
            new Class<?>[]{boolean.class, byte.class, short.class, int.class, float.class, long.class, double.class};
}
