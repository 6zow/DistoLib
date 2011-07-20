package ru.shizow.proxy;

import org.objectweb.asm.Type;
import sun.reflect.Reflection;

import java.lang.reflect.Method;

/**
 * The helper class used by proxied classes and {@link InvocationDelegate}s.
 *
 * @author Max Gorbunov
 */
public class MethodProxy {
    private static InjectionProvider injectionProvider;
    private static InvocationDelegate invocationDelegate;

    /**
     * Internal method to determine if the calling method requires proxying.
     * <p/>
     * The method does not require proxying if it's called from this class
     * (see {@link #invoke(Object, String, String, Object[])}) or the {@link InvocationDelegate} is not set.
     *
     * @return {@code true} if the calling method requires proxying
     */
    public static boolean requiresProxying() {
        // TODO: make it JRE independent (don't use sun.reflect.Reflection)
        return invocationDelegate != null && Reflection.getCallerClass(3) != MethodProxy.class;
    }

    /**
     * Internal method to redirects a call to the {@link #invocationDelegate}.
     * This method is called from proxied objects and is not supposed to be called from applications.
     *
     * @param target     the target object ({@code this} in the method context)
     * @param methodName the method name
     * @param descriptor the method (signature) descriptor, used to distinguish between overloaded methods
     * @param params     actual method parameters. The primitive types are boxed.
     * @return an object returned by a proxied method. The primitive types must be boxed.
     */
    public static Object __invoke(Object target, String methodName, String descriptor, Object[] params) {
        return invocationDelegate.invoke(target, methodName, descriptor, params);
    }

    /**
     * Invokes a target object's method described by {@code methodName} and {@code descriptor}.
     *
     * @param target     the target object ({@code this} in the method context)
     * @param methodName the method name
     * @param descriptor the method (signature) descriptor, used to distinguish between overloaded methods
     * @param params     actual method parameters
     * @return an invocation result. Primitive types are be boxed.
     */
    public static Object invoke(Object target, String methodName, String descriptor, Object[] params) {
        try {
            Class<?> clazz = target.getClass();
            // TODO: cache results
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

    /**
     * Internal method to request a resource from an {@link InjectionProvider}.
     * This method is called from a proxied object. Don't call it directly.
     *
     * @param target    an object for which the injection was requested.
     *                  Only it's class is used to determine what needs to be injected.
     * @param fieldName a name of a field for which the injection was requested
     * @return the resource to be injected
     */
    public static Object getInjectValue(Object target, String fieldName) {
        if (injectionProvider == null) {
            throw new IllegalStateException("Injection requested, but injection provider is not set");
        }
        try {
            // TODO: cache annotations
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
