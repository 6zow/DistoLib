package ru.shizow.proxy.transform;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;
import ru.shizow.proxy.MethodProxy;
import ru.shizow.proxy.TypeUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ProxyMethodAdapter extends AdviceAdapter {
    private final String name;
    private final String desc;
    private final Set<String> annotationDescriptors;
    private final String className;
    private boolean dataAware;

    public ProxyMethodAdapter(MethodVisitor mv, int access, String name, String desc, Set<String> annotationDescriptors,
                              String className) {
        super(mv, access, name, desc);
        this.name = name;
        this.desc = desc;
        this.annotationDescriptors = annotationDescriptors;
        this.className = className;
    }

    private static String getReturnType(String desc) {
        return desc.substring(desc.indexOf(')') + 1);
    }

    private static List<String> parseParams(String desc) {
        char[] d = desc.toCharArray();
        List<String> res = new ArrayList<String>();
        String prefix = "";
        for (int i = 1; i < d.length; i++) {
            switch (d[i]) {
                case ')':
                    return res;
                case '[':
                    prefix += "[";
                    break;
                case 'L':
                    int semiIdx = desc.indexOf(';', i);
                    res.add(prefix + new String(d, i, semiIdx));
                    i = semiIdx;
                    prefix = "";
                    break;
                default:
                    res.add(prefix + String.valueOf(d[i]));
                    prefix = "";
            }
        }
        throw new IllegalArgumentException("Invalid method description");
    }

    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        if (annotationDescriptors.contains(desc)) {
            dataAware = true;
        }
        return super.visitAnnotation(desc, visible);
    }

    @Override
    protected void onMethodEnter() {
        if (name.equals("<init>")) {
            mv.visitIntInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESPECIAL, className, ProxyAdapter.INJECT_INIT_NAME, "()V");
        }
        if (dataAware) {
            // if (requiresProxying()) {
            mv.visitMethodInsn(INVOKESTATIC, Type.getInternalName(MethodProxy.class), "requiresProxying", "()Z");
            Label label = new Label();
            mv.visitJumpInsn(IFEQ, label);
            // target = this
            mv.visitVarInsn(ALOAD, 0);
//                    mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;");
            // method name
            mv.visitLdcInsn(name);
            // method desc
            mv.visitLdcInsn(desc);
            // a = new Object[params.length]
            List<String> paramTypes = parseParams(desc);
            mv.visitIntInsn(BIPUSH, paramTypes.size());
            mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");
            int slot = 1;
            for (int i = 0; i < paramTypes.size(); i++) {
                // a[i] = params[i]
                mv.visitInsn(DUP);
                mv.visitIntInsn(BIPUSH, i);
                char t = paramTypes.get(i).charAt(0);
                switch (t) {
                    case 'L':
                    case '[':
                        mv.visitVarInsn(ALOAD, slot++);
                        break;
                    default:
                        int idx = TypeUtil.PRIM_TYPES.indexOf(t);
                        if (idx < 0) {
                            throw new RuntimeException("Unexpected parameter type " + paramTypes.get(i));
                        }
                        mv.visitVarInsn(ILOAD + TypeUtil.INST_OFFSET[idx], slot);
                        slot += TypeUtil.SLOT_LEN[idx];
                        mv.visitMethodInsn(INVOKESTATIC, "java/lang/" + TypeUtil.TYPE_NAMES[idx], "valueOf",
                                "(" + t + ")Ljava/lang/" + TypeUtil.TYPE_NAMES[idx] + ";");
                }
                mv.visitInsn(AASTORE);
            }
            // r = invoke(target, name, desc, a)
            mv.visitMethodInsn(INVOKESTATIC, Type.getInternalName(MethodProxy.class), "__invoke",
                    "(Ljava/lang/Object;Ljava/lang/String;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/Object;");
            String returnType = getReturnType(desc);
            char t = returnType.charAt(0);
            switch (t) {
                case 'V':
                    // return
                    mv.visitInsn(RETURN);
                    break;
                case 'L':
                    // return (returnType) r
                    mv.visitTypeInsn(CHECKCAST, returnType.substring(1, returnType.length() - 1));
                    mv.visitInsn(ARETURN);
                    break;
                case '[':
                    // return (returnType) r
                    mv.visitTypeInsn(CHECKCAST, returnType);
                    mv.visitInsn(ARETURN);
                    break;
                default:
                    // return r.intValue() .. doubleValue()
                    int idx = TypeUtil.PRIM_TYPES.indexOf(t);
                    if (idx < 0) {
                        throw new RuntimeException("Unexpected return type: " + returnType);
                    }
                    mv.visitTypeInsn(CHECKCAST, "java/lang/" + TypeUtil.TYPE_NAMES[idx]);
                    mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/" + TypeUtil.TYPE_NAMES[idx], TypeUtil.SHORT_TYPE_NAMES[idx] + "Value",
                            "()" + t);
                    mv.visitInsn(IRETURN + TypeUtil.INST_OFFSET[idx]);
            }
            mv.visitLabel(label);
        }
    }

    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
        super.visitMaxs(Math.max(maxStack, 8), maxLocals);
    }
}
