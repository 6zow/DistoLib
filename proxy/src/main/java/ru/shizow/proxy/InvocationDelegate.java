package ru.shizow.proxy;

/**
 * The delegate which is called from a proxied object.
 *
 * @author Max Gorbunov
 */
public interface InvocationDelegate {
    /**
     * Implements the logic for a proxied method.
     * <p/>
     * If the delegate calls the original method,
     * it must do so using {@link MethodProxy#invoke(Object, String, String, Object[])} to avoid further proxying.
     *
     * @param target     the target object ({@code this} in the method context)
     * @param methodName the method name
     * @param descriptor the method (signature) descriptor, used to distinguish between overloaded methods
     * @param params     actual method parameters. The primitive types are boxed.
     * @return an object returned by a proxied method. The primitive types must be boxed.
     */
    Object invoke(Object target, String methodName, String descriptor, Object[] params);
}
