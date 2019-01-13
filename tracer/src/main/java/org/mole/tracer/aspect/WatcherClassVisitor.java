package org.mole.tracer.aspect;

import org.mole.tracer.context.ContextConfig;
import org.mole.tracer.context.TracerContext;
import org.mole.tracer.dto.P;
import org.mole.tracer.dto.SimpleMethod;
import org.mole.tracer.utils.SimpleLoggerManager;
import org.mole.tracer.utils.SimpleStringUtils;
import org.mole.tracer.watcher.WatcherMediator;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.objectweb.asm.Opcodes.*;

public class WatcherClassVisitor extends ClassVisitor {

    //# mediator
    private static final String MEDIATOR_NAME = WatcherMediator.class.getName().replace(".", "/");

    private static final String NEED_PROFILE = "needRecord";
    private static final String NEED_PROFILE_DESC = "()Z";

    private static final String DO_PROFILER = "doRecord";
    private static final String DO_PROFILER_DESC = "([Ljava/lang/Object;Ljava/lang/Object;)V";

    private static final String GEN_TRACE_ID = "genTraceId";
    private static final String GEN_TRACE_ID_DESC = "(Ljava/lang/Object;)Ljava/lang/String;";

    private static final String GEN_SPAN_ID = "genSpanId";
    private static final String GEN_SPAN_ID_DESC = "(Ljava/lang/Object;)Ljava/lang/String;";
    //#

    //# System
    private static final String SYSTEM_NAME = System.class.getName().replace(".", "/");
    private static final String CURRENT_TIMEMILLS = "currentTimeMillis";
    private static final String CURRENT_TIMEMILLS_DESC = "()J";
    //#

    //# Thread
    private static final String THREAD_NAME = Thread.class.getName().replace(".", "/");
    private static final String CURRENT_THREAD = "currentThread";
    private static final String CURRENT_THREAD_DESC = "()Ljava/lang/Thread;";
    //#

    private static final String OBJECT_NAME = Object.class.getName().replace(".", "/");

    //#byte
    private static final String BYTE_NAME = Byte.class.getName().replace(".", "/");
    private static final String BYTE_VALUE_OF = "valueOf";
    private static final String BYTE_VALUE_OF_DESC = "(J)Ljava/lang/Byte;";
    //#

    //#short
    private static final String SHORT_NAME = Short.class.getName().replace(".", "/");
    private static final String SHORT_VALUE_OF = "valueOf";
    private static final String SHORT_VALUE_OF_DESC = "(S)Ljava/lang/Short;";
    //#

    //#int
    private static final String INTEGER_NAME = Integer.class.getName().replace(".", "/");
    private static final String INTEGER_VALUE_OF = "valueOf";
    private static final String INTEGER_VALUE_OF_DESC = "(I)Ljava/lang/Integer;";
    //#

    //#boolean
    private static final String BOOLEAN_NAME = Boolean.class.getName().replace(".", "/");
    private static final String BOOLEAN_VALUE_OF = "valueOf";
    private static final String BOOLEAN_VALUE_OF_DESC = "(Z)Ljava/lang/Boolean;";
    //#

    //#char
    private static final String CHARACTER_NAME = Character.class.getName().replace(".", "/");
    private static final String CHARACTER_VALUE_OF = "valueOf";
    private static final String CHARACTER_VALUE_OF_DESC = "(C)Ljava/lang/Character;";
    //#

    //#long
    private static final String LONG_NAME = Long.class.getName().replace(".", "/");
    private static final String LONG_VALUE_OF = "valueOf";
    private static final String LONG_VALUE_OF_DESC = "(J)Ljava/lang/Long;";
    //#

    //#float
    private static final String FLOAT_NAME = Float.class.getName().replace(".", "/");
    private static final String FLOAT_VALUE_OF = "valueOf";
    private static final String FLOAT_VALUE_OF_DESC = "(F)Ljava/lang/Float;";
    //#

    //#double
    private static final String DOUBLE_NAME = Double.class.getName().replace(".", "/");
    private static final String DOUBLE_VALUE_OF = "valueOf";
    private static final String DOUBLE_VALUE_OF_DESC = "(D)Ljava/lang/Double;";
    //#

    //#Exception
    private static final String EXCEPTION_NAME = Exception.class.getName().replace(".", "/");

    //if method contains those access flags ,then do not insert insn
    private static final int ACC_MASK = ACC_BRIDGE & ACC_SYNTHETIC & ACC_NATIVE & ACC_ABSTRACT;

    private final String className;

    private final Map<String, Map<String, SimpleMethod>> simpleMethods;

    private final List<String> recordField;

    private final TracerContext context;

    private final Map<String, P<String, String>> gens;


    @SuppressWarnings("WeakerAccess")
    public WatcherClassVisitor(ClassVisitor cv, String className, Map<String, Map<String, SimpleMethod>> simpleMethods, TracerContext context) {
        super(ASM7, cv);

        Objects.requireNonNull(context, "context can not be null");

        this.context = context;
        this.className = className;
        this.simpleMethods = simpleMethods;
        this.recordField = this.context.config.getExtra();
        this.gens = this.context.config.getMethodGen();
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);
        //do not try to insert insn into bridge/synthetic/native/abstract methods
        if (mv != null && (access & ACC_MASK) == 0) {
            final Map<String, SimpleMethod> map = this.simpleMethods.get(name);
            //match methods
            if (map != null && (map.containsKey(SimpleMethod.WILDCARD) || map.get(desc.substring(0, desc.indexOf(')') + 1)) != null)) {
                mv = new WatcherMethodVisitor(1, ASM7, mv);
                //todo handle annotation
                insertCode(mv, name, desc.substring(1, desc.indexOf(')')), access);
            }
        }
        return mv;
    }

    /**
     * insert tracer code
     */
    @SuppressWarnings("WeakerAccess")
    protected void insertCode(MethodVisitor mv, String name, String desc, int access) {
        try {
            //new var local index
            final String[] argDesc = SimpleMethod.splitArgsDesc(desc);

            //start of try catch block
//            final Label start = new Label();
//            mv.visitLabel(start);

            //insert check rate
            mv.visitMethodInsn(INVOKESTATIC, MEDIATOR_NAME, NEED_PROFILE, NEED_PROFILE_DESC, false);
            Label L_goon = new Label();
            mv.visitJumpInsn(IFEQ, L_goon);

            //non-static offset
            final int offset = (access & ACC_STATIC) != 0 ? 0 : 1;

            //add extra info
            addExtraInfoIfNeeded(mv, name, desc, argDesc, offset);

            //add method args
            packMethodArgs(mv, access, argDesc, offset);

            //insert profile
            mv.visitMethodInsn(INVOKESTATIC, MEDIATOR_NAME, DO_PROFILER, DO_PROFILER_DESC, false);

            //jump to the origin insn
            mv.visitLabel(L_goon);

            //todo add try catch
            //end of try catch block
//            final Label end = new Label();
//            mv.visitLabel(end);

            //handle Exception
//            mv.visitIntInsn(ALOAD, offset + getLocalVarSize(argDesc));
//            final Label handle = new Label();
//            mv.visitLabel(handle);

//            insert try catch
//            mv.visitTryCatchBlock(start, end, handle, EXCEPTION_NAME);

            SimpleLoggerManager.info("insert tracer info method :" + name + desc);
        } catch (Exception e) {
            SimpleLoggerManager.error("insertCode error on class" + className + ",name:" + name + ",desc:" + desc);
        }
    }

    /**
     *
     */
    private void addExtraInfoIfNeeded(final MethodVisitor mv, final String name, final String desc, final String[] descArr, int offset) {
        if (recordField != null && !recordField.isEmpty()) {
            //new Object array
            mv.visitIntInsn(BIPUSH, ContextConfig.CONFIG_VALUES.size());
            mv.visitTypeInsn(ANEWARRAY, OBJECT_NAME);

            String traceClass = null;
            String spanClass = null;

            P<String, String> p = gens.get(className + '#' + name + "(.)");
            if (p != null) {
                traceClass = p.get0();
                spanClass = p.get1();
            }

            //wildcard method will be covered be the specific method
            if ((p = gens.get(className + '#' + name + '(' + SimpleMethod.fromDesc2TypeString(desc) + ')')) != null) {
                traceClass = p.get0();
                spanClass = p.get1();
            }

            //get trace span index
            int traceIndex = -1;
            if (SimpleStringUtils.isNotBlank(traceClass)) {
                traceIndex = getClassLocalIndex(offset, descArr, traceClass);
            }
            int spanIndex = -1;
            if (SimpleStringUtils.isNotBlank(spanClass)) {
                spanIndex = getClassLocalIndex(offset, descArr, spanClass);
            }

            for (String s : recordField) {
                switch (ContextConfig.ConfigValue.valueOf(s)) {
                    case methodName:
                        mv.visitInsn(DUP);
                        mv.visitInsn(ICONST_0);
                        mv.visitLdcInsn(className + "." + name);
                        mv.visitInsn(AASTORE);
                        break;
                    case methodDesc:
                        mv.visitInsn(DUP);
                        mv.visitInsn(ICONST_1);
                        mv.visitLdcInsn(desc);
                        mv.visitInsn(AASTORE);
                        break;
                    case currentThread:
                        mv.visitInsn(DUP);
                        mv.visitInsn(ICONST_2);
                        mv.visitMethodInsn(INVOKESTATIC, THREAD_NAME, CURRENT_THREAD, CURRENT_THREAD_DESC, false);
                        mv.visitInsn(AASTORE);
                        break;
                    case currentTimeMills:
                        mv.visitInsn(DUP);
                        mv.visitInsn(ICONST_3);
                        mv.visitMethodInsn(INVOKESTATIC, SYSTEM_NAME, CURRENT_TIMEMILLS, CURRENT_TIMEMILLS_DESC, false);
                        mv.visitMethodInsn(INVOKESTATIC, LONG_NAME, LONG_VALUE_OF, LONG_VALUE_OF_DESC, false);
                        mv.visitInsn(AASTORE);
                        break;
                    case traceId:
                        //noinspection Duplicates
                        if (traceIndex >= 0) {
                            mv.visitInsn(DUP);
                            mv.visitInsn(ICONST_4);
                            mv.visitIntInsn(ALOAD, traceIndex);
                            mv.visitMethodInsn(INVOKESTATIC, MEDIATOR_NAME, GEN_TRACE_ID, GEN_TRACE_ID_DESC, false);
                            mv.visitInsn(AASTORE);
                        }
                        break;
                    case spanId:
                        //noinspection Duplicates
                        if (spanIndex >= 0) {
                            mv.visitInsn(DUP);
                            mv.visitInsn(ICONST_5);
                            mv.visitIntInsn(ALOAD, spanIndex);
                            mv.visitMethodInsn(INVOKESTATIC, MEDIATOR_NAME, GEN_SPAN_ID, GEN_SPAN_ID_DESC, false);
                            mv.visitInsn(AASTORE);
                        }
                        break;
                    default:
                        break;
                }
            }
        } else {
            mv.visitInsn(ACONST_NULL);
        }
    }

    /**
     *
     */
    private void packMethodArgs(final MethodVisitor mv, final int access, final String[] args, final int offset) {
        //todo handle config
        //handle varargs methods
        if (args.length == 1 && (access & ACC_VARARGS) != 0) {
            mv.visitVarInsn(ALOAD, offset);
        } else {
            packAsArray(mv, args, offset);
        }
    }

    /**
     * handle non-varargs methods
     */
    private void packAsArray(final MethodVisitor mv, final String[] args, int offset) {
        //new Object array
        mv.visitIntInsn(BIPUSH, args.length);
        mv.visitTypeInsn(ANEWARRAY, OBJECT_NAME);
        //handle method args
        for (int i = 0; i < args.length; i++) {
            mv.visitInsn(DUP);
            switch (i) {
                case 0:
                    mv.visitInsn(ICONST_0);
                    break;
                case 1:
                    mv.visitInsn(ICONST_1);
                    break;
                case 2:
                    mv.visitInsn(ICONST_2);
                    break;
                case 3:
                    mv.visitInsn(ICONST_3);
                    break;
                case 4:
                    mv.visitInsn(ICONST_4);
                    break;
                case 5:
                    mv.visitInsn(ICONST_5);
                    break;
                default:
                    mv.visitIntInsn(BIPUSH, i);
                    break;
            }
            // primitive types should be box as reference types
            switch (args[i].charAt(0)) {
                case 'Z':
                    mv.visitIntInsn(ILOAD, i + offset);
                    mv.visitMethodInsn(INVOKESTATIC, BOOLEAN_NAME, BOOLEAN_VALUE_OF, BOOLEAN_VALUE_OF_DESC, false);
                    break;
                case 'B':
                    mv.visitIntInsn(ILOAD, i + offset);
                    mv.visitMethodInsn(INVOKESTATIC, BYTE_NAME, BYTE_VALUE_OF, BYTE_VALUE_OF_DESC, false);
                    break;
                case 'S':
                    mv.visitIntInsn(ILOAD, i + offset);
                    mv.visitMethodInsn(INVOKESTATIC, SHORT_NAME, SHORT_VALUE_OF, SHORT_VALUE_OF_DESC, false);
                    break;
                case 'I':
                    mv.visitIntInsn(ILOAD, i + offset);
                    mv.visitMethodInsn(INVOKESTATIC, INTEGER_NAME, INTEGER_VALUE_OF, INTEGER_VALUE_OF_DESC, false);
                    break;
                case 'F':
                    mv.visitIntInsn(FLOAD, i + offset);
                    mv.visitMethodInsn(INVOKESTATIC, FLOAT_NAME, FLOAT_VALUE_OF, FLOAT_VALUE_OF_DESC, false);
                    break;
                case 'C':
                    mv.visitIntInsn(ILOAD, i + offset);
                    mv.visitMethodInsn(INVOKESTATIC, CHARACTER_NAME, CHARACTER_VALUE_OF, CHARACTER_VALUE_OF_DESC, false);
                    break;
                case 'J':
                    // long/double takes 2 local var index
                    mv.visitIntInsn(LLOAD, i + offset++);
                    mv.visitMethodInsn(INVOKESTATIC, LONG_NAME, LONG_VALUE_OF, LONG_VALUE_OF_DESC, false);
                    break;
                case 'D':
                    // long/double takes 2 local var index
                    mv.visitIntInsn(DLOAD, i + offset++);
                    mv.visitMethodInsn(INVOKESTATIC, DOUBLE_NAME, DOUBLE_VALUE_OF, DOUBLE_VALUE_OF_DESC, false);
                    break;
                default:
                    mv.visitIntInsn(ALOAD, i + offset);
                    break;
            }
            mv.visitInsn(AASTORE);
        }

    }

    /**
     * count var size
     */
    private static int getLocalVarSize(String[] desc) {
        int size = 0;
        for (String s : desc) {
            if (s.equals("D") || s.equals("J")) {
                size += 2;
            } else {
                size++;
            }
        }
        return size;
    }

    private static int getClassLocalIndex(int offset, String[] desc, String className) {
        int j = 0;
        String s;
        for (int i = 0; i < desc.length; i++) {
            s = desc[i];
            if (s.equals("D") || s.equals("J")) {
                j++;
            }
            if (SimpleMethod.fromType2Desc(className).equals(s)) {
                return i + offset + j;
            }
        }
        return Integer.MIN_VALUE;
    }

}