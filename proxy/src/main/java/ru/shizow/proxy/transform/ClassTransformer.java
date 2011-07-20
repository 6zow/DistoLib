package ru.shizow.proxy.transform;

import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.io.IOException;
import java.io.InputStream;

/**
 * The helper class which transforms the class' bytecode with the {@link ProxyAdapter} class.
 *
 * @author Max Gorbunov
 */
public class ClassTransformer {

    /**
     * Instruments the class bytecode so that it calls proxy for each method
     * annotated with one the {@code proxyMethodAnnotations}
     * and initializes fields annotated with one of the {@code injectAnnotations}.
     *
     * @param inputStream            the input stream with the original class' bytecode
     * @param proxyMethodAnnotations annotations which mean that a method must be proxied
     * @param injectAnnotations      annotations which mean that a resource must be injected in a field
     * @return modified bytecode
     * @throws IOException if the original bytecode can't be read
     */
    public static byte[] transformClass(InputStream inputStream,
                                        Class<?>[] proxyMethodAnnotations, Class<?>[] injectAnnotations)
            throws IOException {
        ClassReader cr = new ClassReader(inputStream);
        ClassWriter cw = new ClassWriter(cr, 0);
        ClassAdapter ca = new ProxyAdapter(cw, proxyMethodAnnotations, injectAnnotations);
        cr.accept(ca, ClassReader.EXPAND_FRAMES);
        return cw.toByteArray();
    }
}
