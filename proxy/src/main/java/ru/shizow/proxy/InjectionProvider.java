package ru.shizow.proxy;

import java.lang.annotation.Annotation;

public interface InjectionProvider {
    Object getResource(Annotation[] annotations);
}
