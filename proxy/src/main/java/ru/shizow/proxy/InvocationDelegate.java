package ru.shizow.proxy;

public interface InvocationDelegate {
    Object invoke(Object target, String methodName, String descriptor, Object[] params);
}
