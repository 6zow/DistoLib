package ru.shizow.proxy.transform;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * The modified {@link URLClassLoader} which transforms all loaded classes using {@link ClassTransformer}.
 *
 * @author Max Gorbunov
 */
public class TransformingUrlClassLoader extends URLClassLoader {
    private final Class<?>[] proxyMethodAnnotations;
    private final Class<?>[] injectAnnotations;

    /**
     * Creates a new {@code TransformingUrlClassLoader}.
     *
     * @param urls                   a list of URLs (see {@link URLClassLoader#URLClassLoader(java.net.URL[])})
     * @param proxyMethodAnnotations a list of annotations which mean that a method requires proxying
     * @param injectAnnotations      a list of annotations which mean that a field requires resource injection
     */
    public TransformingUrlClassLoader(URL[] urls, Class<?>[] proxyMethodAnnotations, Class<?>[] injectAnnotations) {
        super(urls);
        this.proxyMethodAnnotations = proxyMethodAnnotations;
        this.injectAnnotations = injectAnnotations;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        try {
            InputStream stream = getResourceAsStream(name.replace('.', '/') + ".class");
            if (stream == null) {
                return super.findClass(name);
            }
            byte[] bytes = ClassTransformer.transformClass(stream, proxyMethodAnnotations, injectAnnotations);
            return defineClass(name, bytes, 0, bytes.length);
        } catch (IOException e) {
            throw new ClassNotFoundException(name, e);
        }
    }
}
