package ru.shizow.proxy;

import org.objectweb.asm.Type;
import sun.reflect.Reflection;

import java.lang.reflect.Method;

public class MethodProxy {
    private static InjectionProvider injectionProvider;
    private static InvocationDelegate invocationDelegate;

    public static boolean requiresProxying() {
        return invocationDelegate != null && Reflection.getCallerClass(3) != MethodProxy.class;
    }

    public static Object __invoke(Object target, String methodName, String descriptor, Object[] params) {
//        return invoke(target, methodName, descriptor, params);
        return invocationDelegate.invoke(target, methodName, descriptor, params);
    }

    public static Object invoke(Object target, String methodName, String descriptor, Object[] params) {
        try {
            Class<?> clazz = target.getClass();
            Class<?>[] argTypes = getArgTypes(descriptor, clazz.getClassLoader());
            Method m = clazz.getMethod(methodName, argTypes);
            return m.invoke(target, params);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Class<?>[] getArgTypes(String descriptor, ClassLoader classLoader) throws ClassNotFoundException {
        Type[] argumentTypes = Type.getArgumentTypes(descriptor);
        Class<?>[] argTypes = new Class<?>[argumentTypes.length];
        for (int i = 0; i < argTypes.length; i++) {
            Type type = argumentTypes[i];
            char ch = type.getDescriptor().charAt(0);
            String className = ch == '[' ? type.getElementType().getClassName() : type.getClassName();
            int idx = TypeUtil.PRIM_TYPES.indexOf(ch);
            if (idx < 0) {
                if (ch == '[') {
                    className = type.getDescriptor().replace('/', '.');
                }
                argTypes[i] = Class.forName(className, false, classLoader);
            } else {
                argTypes[i] = TypeUtil.PRIM_CLASSES[idx];
            }
        }
        return argTypes;
    }

    public static Object getInjectValue(Object target, String fieldName) {
        if (injectionProvider == null) {
            throw new IllegalStateException("Injection requested, but injection provider is not set");
        }
        try {
            return injectionProvider.getResource(target.getClass().getDeclaredField(fieldName).getAnnotations());
        } catch (NoSuchFieldException e) {
            throw new RuntimeException("Could not inject resource", e);
        }
    }

    public static InjectionProvider getInjectionProvider() {
        return injectionProvider;
    }

    public static void setInjectionProvider(InjectionProvider injectionProvider) {
        MethodProxy.injectionProvider = injectionProvider;
    }

    public static InvocationDelegate getInvocationDelegate() {
        return invocationDelegate;
    }

    public static void setInvocationDelegate(InvocationDelegate invocationDelegate) {
        MethodProxy.invocationDelegate = invocationDelegate;
    }
}
