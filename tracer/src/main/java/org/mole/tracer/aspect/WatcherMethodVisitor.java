package org.mole.tracer.aspect;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.MethodVisitor;

/**
 * Created by k3a
 * on 19-1-10  下午11:40
 */
public class WatcherMethodVisitor extends MethodVisitor {

    private final int newVarNum;

    public WatcherMethodVisitor(int newVarNum, int api, MethodVisitor methodVisitor) {
        super(api, methodVisitor);
        this.newVarNum = newVarNum;
    }

    @Override
    public void visitMaxs(final int maxStack, final int maxLocals) {
        //todo compute max
        if (mv != null) {
            mv.visitMaxs(maxStack, maxLocals + newVarNum);
        }
    }

    @Override
    public AnnotationVisitor visitAnnotation(final String descriptor, final boolean visible) {
        //todo handle method annotation
        if (mv != null) {
            return mv.visitAnnotation(descriptor, visible);
        }
        return null;
    }


    @Override
    public AnnotationVisitor visitParameterAnnotation(final int parameter, final String descriptor, final boolean visible) {
        //todo handle para annotation
        if (mv != null) {
            return mv.visitParameterAnnotation(parameter, descriptor, visible);
        }
        return null;
    }

}
