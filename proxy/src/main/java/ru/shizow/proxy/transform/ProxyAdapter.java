package ru.shizow.proxy.transform;

import org.objectweb.asm.*;
import ru.shizow.proxy.InjectionProvider;
import ru.shizow.proxy.MethodProxy;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * The {@link ClassAdapter} which changes annotated methods and creates an initializer for annotated fields.
 *
 * @author Max Gorbunov
 */
public class ProxyAdapter extends ClassAdapter implements Opcodes {
    /**
     * The name of the injection initializer method.
     */
    static final String INJECT_INIT_NAME = "!inject";
    private final Set<String> proxyAnnotationDescriptors;
    private final Set<String> injectAnnotationDescriptors;
    private final Map<String, String> injectedFields = new HashMap<String, String>();
    private String className;

    public ProxyAdapter(ClassWriter cw, Class<?>[] proxyAnnotations, Class<?>[] injectAnnotations) {
        super(cw);
        proxyAnnotationDescriptors = new HashSet<String>();
        for (Class<?> a : proxyAnnotations) {
            proxyAnnotationDescriptors.add(Type.getDescriptor(a));
        }
        injectAnnotationDescriptors = new HashSet<String>();
        for (Class<?> a : injectAnnotations) {
            injectAnnotationDescriptors.add(Type.getDescriptor(a));
        }
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        className = name;
    }

    /**
     * Creates an adapter for constructors and annotated methods.
     *
     * @param access     {@inheritDoc}
     * @param name       {@inheritDoc}
     * @param desc       {@inheritDoc}
     * @param signature  {@inheritDoc}
     * @param exceptions {@inheritDoc}
     * @return {@inheritDoc}
     * @see ProxyMethodAdapter
     */
    @Override
    public MethodVisitor visitMethod(int access, final String name, final String desc, String signature,
                                     String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
        return new ProxyMethodAdapter(mv, access, name, desc, proxyAnnotationDescriptors, className);
    }

    /**
     * Collects the names and types of fields with injection annotations.
     *
     * @param access    {@inheritDoc}
     * @param name      {@inheritDoc}
     * @param fieldDesc {@inheritDoc}
     * @param signature {@inheritDoc}
     * @param value     {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public FieldVisitor visitField(int access, final String name, final String fieldDesc, String signature,
                                   Object value) {
        final FieldVisitor fv = super.visitField(access, name, fieldDesc, signature, value);
        return new FieldVisitor() {
            @Override
            public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
                if (injectAnnotationDescriptors.contains(desc)) {
                    injectedFields.put(name, fieldDesc);
                }
                return fv.visitAnnotation(desc, visible);
            }

            @Override
            public void visitAttribute(Attribute attr) {
                fv.visitAttribute(attr);
            }

            @Override
            public void visitEnd() {
                fv.visitEnd();
            }
        };
    }

    /**
     * Adds the injection initialization method.
     * This method initializes all annotated fields by calling an {@link InjectionProvider}.
     *
     * @see #INJECT_INIT_NAME
     * @see MethodProxy#getInjectValue(Object, String)
     */
    @Override
    public void visitEnd() {
        MethodVisitor mv = super.visitMethod(ACC_PRIVATE, INJECT_INIT_NAME, "()V", null, null);
        mv.visitCode();
        for (String fieldName : injectedFields.keySet()) {
            mv.visitIntInsn(ALOAD, 0);
            mv.visitIntInsn(ALOAD, 0);
            mv.visitLdcInsn(fieldName);
            mv.visitMethodInsn(INVOKESTATIC, Type.getInternalName(MethodProxy.class), "getInjectValue",
                    "(Ljava/lang/Object;Ljava/lang/String;)Ljava/lang/Object;");
            String type = injectedFields.get(fieldName);
            mv.visitTypeInsn(CHECKCAST, type.substring(1, type.length() - 1));
            mv.visitFieldInsn(PUTFIELD, className, fieldName, type);
        }
        mv.visitInsn(RETURN);
        mv.visitMaxs(3, 1);
        mv.visitEnd();
        super.visitEnd();
    }
}
