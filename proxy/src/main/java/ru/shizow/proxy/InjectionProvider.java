package ru.shizow.proxy;

import java.lang.annotation.Annotation;

/**
 * The injection provider for resource injections.
 *
 * @author Max Gorbunov
 */
public interface InjectionProvider {
    /**
     * Provides a resource for given annotations.
     * Must throw an exception if none of the annotations is meaningful for this provider
     * or multiple conflicting annotations are present.
     * <p/>
     * Annotated fields must be references. Primitive types are not supported.
     *
     * @param annotations the list of annotations on a field which requires injection.
     *                    Annotation do not necessarily describe injections,
     *                    but at least one of them must be an injection annotation.
     * @return a requested resource (may be null if this makes sense)
     */
    Object getResource(Annotation[] annotations);
}
