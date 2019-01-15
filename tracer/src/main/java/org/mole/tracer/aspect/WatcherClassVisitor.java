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

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.mole.tracer.context.ContextConfig.ConfigValue.methodDesc;
import static org.objectweb.asm.Opcodes.*;

public class WatcherClassVisitor extends ClassVisitor {

    //# mediator
    private static final String MEDIATOR_NAME = WatcherMediator.class.getName().replace(".", "/");

    private static final String NEED_PROFILE = "needRecord";
    private static final String NEED_PROFILE_DESC = "()Z";

    private static final String DO_PROFILER = "doRecord";
    private static final String DO_PROFILER_DESC = "(Ljava/lang/String;)V";

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
    //#

    private static final String TO_STRING = "toString";

    //# StringBuilder
    private static final String STRING_BUILDER_NAME = StringBuilder.class.getName().replace(".", "/");
    private static final String STRING_BUILDER_APPEND = "append";
    private static final String STRING_BUILDER_TO_STRING_DESC = "()Ljava/lang/String;";
    private static final String STRING_BUILDER_APPEND_DESC_OBJ = "(Ljava/lang/Object;)Ljava/lang/StringBuilder;";
    private static final String STRING_BUILDER_APPEND_DESC_STR = "(Ljava/lang/String;)Ljava/lang/StringBuilder;";
    private static final String STRING_BUILDER_APPEND_DESC_Z = "(Z)Ljava/lang/StringBuilder;";
    private static final String STRING_BUILDER_APPEND_DESC_B = "(B)Ljava/lang/StringBuilder;";
    private static final String STRING_BUILDER_APPEND_DESC_I = "(I)Ljava/lang/StringBuilder;";
    private static final String STRING_BUILDER_APPEND_DESC_C = "(C)Ljava/lang/StringBuilder;";
    private static final String STRING_BUILDER_APPEND_DESC_F = "(F)Ljava/lang/StringBuilder;";
    private static final String STRING_BUILDER_APPEND_DESC_D = "(D)Ljava/lang/StringBuilder;";
    private static final String STRING_BUILDER_APPEND_DESC_J = "(J)Ljava/lang/StringBuilder;";
    //#

    //# Arrays
    private static final String ARRAYS_NAME = Arrays.class.getName().replace(".", "/");
    private static final String ARRAYS_TO_STRING_DESC_OBJ = "([Ljava/lang/Object;)Ljava/lang/String;";
    private static final String ARRAYS_TO_STRING_DESC_Z = "([Z)Ljava/lang/String;";
    private static final String ARRAYS_TO_STRING_DESC_B = "([B)Ljava/lang/String;";
    private static final String ARRAYS_TO_STRING_DESC_S = "([S)Ljava/lang/String;";
    private static final String ARRAYS_TO_STRING_DESC_I = "([I)Ljava/lang/String;";
    private static final String ARRAYS_TO_STRING_DESC_C = "([C)Ljava/lang/String;";
    private static final String ARRAYS_TO_STRING_DESC_F = "([F)Ljava/lang/String;";
    private static final String ARRAYS_TO_STRING_DESC_D = "([D)Ljava/lang/String;";
    private static final String ARRAYS_TO_STRING_DESC_J = "([J)Ljava/lang/String;";
    //#

    private static final String OBJECT_INIT_NAME = "<init>";
    private static final String OBJECT_INIT_NAME_DESC = "()V";

    //if method contains those access flags ,then do not insert insn
    private static final int ACC_MASK = ACC_BRIDGE & ACC_SYNTHETIC & ACC_NATIVE & ACC_ABSTRACT;

    private final String className;

    private final Map<String, Map<String, SimpleMethod>> simpleMethods;

    private final List<String> recordField;

    private final TracerContext context;

    private final Map<String, P<String, String>> gens;


    private final String separator;
    private final String argsSeparator;

    @SuppressWarnings("WeakerAccess")
    public WatcherClassVisitor(ClassVisitor cv, String className, Map<String, Map<String, SimpleMethod>> simpleMethods, TracerContext context) {
        super(ASM7, cv);

        Objects.requireNonNull(context, "context can not be null");

        this.context = context;
        this.className = className;
        this.simpleMethods = simpleMethods;
        this.recordField = this.context.config.getExtra();
        this.gens = this.context.config.getMethodGen();
        this.separator = this.context.config.get_separator();
        this.argsSeparator = this.context.config.get_argsSeparator();
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

            //todo  use single array to carry extraInfo and method args
            //insert check rate
            mv.visitMethodInsn(INVOKESTATIC, MEDIATOR_NAME, NEED_PROFILE, NEED_PROFILE_DESC, false);
            Label L_goon = new Label();
            mv.visitJumpInsn(IFEQ, L_goon);

            //non-static offset
            final int offset = (access & ACC_STATIC) != 0 ? 0 : 1;

            //add extra info
            addExtraInfoIfNeeded(mv, access, name, desc, argDesc, offset);

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
    private void addExtraInfoIfNeeded(final MethodVisitor mv, int access, final String name, final String desc, final String[] descArr, int offset) {
        if (recordField == null || recordField.isEmpty()) {
            mv.visitInsn(ACONST_NULL);
            return;
        }

        //new StringBuilder
        mv.visitTypeInsn(NEW, STRING_BUILDER_NAME);
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKESPECIAL, STRING_BUILDER_NAME, OBJECT_INIT_NAME, OBJECT_INIT_NAME_DESC, false);

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


        //append as String
        for (int i = 0; i < ContextConfig.ConfigValue.values().length; i++) {
            final ContextConfig.ConfigValue value = ContextConfig.ConfigValue.values()[i];
            if (!recordField.contains(value.name())) {
                mv.visitLdcInsn("");
                mv.visitMethodInsn(INVOKEVIRTUAL, STRING_BUILDER_NAME, STRING_BUILDER_APPEND, STRING_BUILDER_APPEND_DESC_STR, false);
                mv.visitLdcInsn(separator);
                mv.visitMethodInsn(INVOKEVIRTUAL, STRING_BUILDER_NAME, STRING_BUILDER_APPEND, STRING_BUILDER_APPEND_DESC_STR, false);
                continue;
            }
            switch (value) {
                case methodName:
                    if (recordField.contains(methodDesc.name())) {
                        mv.visitLdcInsn(className + "." + name + separator + desc + separator);
                        mv.visitMethodInsn(INVOKEVIRTUAL, STRING_BUILDER_NAME, STRING_BUILDER_APPEND, STRING_BUILDER_APPEND_DESC_STR, false);
                        //skip methodDesc
                        i++;
                    } else {
                        mv.visitLdcInsn(className + "." + name + separator);
                        mv.visitMethodInsn(INVOKEVIRTUAL, STRING_BUILDER_NAME, STRING_BUILDER_APPEND, STRING_BUILDER_APPEND_DESC_STR, false);
                    }
                    break;
                case methodDesc:
                    mv.visitLdcInsn(desc + separator);
                    mv.visitMethodInsn(INVOKEVIRTUAL, STRING_BUILDER_NAME, STRING_BUILDER_APPEND, STRING_BUILDER_APPEND_DESC_STR, false);
                    break;
                case currentThread:
                    mv.visitMethodInsn(INVOKESTATIC, THREAD_NAME, CURRENT_THREAD, CURRENT_THREAD_DESC, false);
                    mv.visitMethodInsn(INVOKEVIRTUAL, STRING_BUILDER_NAME, STRING_BUILDER_APPEND, STRING_BUILDER_APPEND_DESC_OBJ, false);
                    mv.visitLdcInsn(separator);
                    mv.visitMethodInsn(INVOKEVIRTUAL, STRING_BUILDER_NAME, STRING_BUILDER_APPEND, STRING_BUILDER_APPEND_DESC_STR, false);
                    break;
                case currentTimeMills:
                    mv.visitMethodInsn(INVOKESTATIC, SYSTEM_NAME, CURRENT_TIMEMILLS, CURRENT_TIMEMILLS_DESC, false);
                    mv.visitMethodInsn(INVOKEVIRTUAL, STRING_BUILDER_NAME, STRING_BUILDER_APPEND, STRING_BUILDER_APPEND_DESC_J, false);
                    mv.visitLdcInsn(separator);
                    mv.visitMethodInsn(INVOKEVIRTUAL, STRING_BUILDER_NAME, STRING_BUILDER_APPEND, STRING_BUILDER_APPEND_DESC_STR, false);
                    break;
                case traceId:
                    //noinspection Duplicates
                    if (traceIndex >= 0) {
                        //handle primitive type
                        switch (SimpleMethod.fromType2Desc(traceClass)) {
                            case "Z":
                                mv.visitIntInsn(ILOAD, traceIndex);
                                mv.visitMethodInsn(INVOKESTATIC, BOOLEAN_NAME, BOOLEAN_VALUE_OF, BOOLEAN_VALUE_OF_DESC, false);
                                break;
                            case "B":
                                mv.visitIntInsn(ILOAD, traceIndex);
                                mv.visitMethodInsn(INVOKESTATIC, BYTE_NAME, BYTE_VALUE_OF, BYTE_VALUE_OF_DESC, false);
                                break;
                            case "S":
                                mv.visitIntInsn(ILOAD, traceIndex);
                                mv.visitMethodInsn(INVOKESTATIC, SHORT_NAME, SHORT_VALUE_OF, SHORT_VALUE_OF_DESC, false);
                                break;
                            case "I":
                                mv.visitIntInsn(ILOAD, traceIndex);
                                mv.visitMethodInsn(INVOKESTATIC, INTEGER_NAME, INTEGER_VALUE_OF, INTEGER_VALUE_OF_DESC, false);
                                break;
                            case "F":
                                mv.visitIntInsn(FLOAD, traceIndex);
                                mv.visitMethodInsn(INVOKESTATIC, FLOAT_NAME, FLOAT_VALUE_OF, FLOAT_VALUE_OF_DESC, false);
                                break;
                            case "C":
                                mv.visitIntInsn(ILOAD, traceIndex);
                                mv.visitMethodInsn(INVOKESTATIC, CHARACTER_NAME, CHARACTER_VALUE_OF, CHARACTER_VALUE_OF_DESC, false);
                                break;
                            case "J":
                                mv.visitIntInsn(LLOAD, traceIndex);
                                mv.visitMethodInsn(INVOKESTATIC, LONG_NAME, LONG_VALUE_OF, LONG_VALUE_OF_DESC, false);
                                break;
                            case "D":
                                mv.visitIntInsn(DLOAD, traceIndex);
                                mv.visitMethodInsn(INVOKESTATIC, DOUBLE_NAME, DOUBLE_VALUE_OF, DOUBLE_VALUE_OF_DESC, false);
                                break;
                            default:
                                mv.visitIntInsn(ALOAD, traceIndex);
                                break;
                        }

                        mv.visitMethodInsn(INVOKESTATIC, MEDIATOR_NAME, GEN_TRACE_ID, GEN_TRACE_ID_DESC, false);
                        mv.visitMethodInsn(INVOKEVIRTUAL, STRING_BUILDER_NAME, STRING_BUILDER_APPEND, STRING_BUILDER_APPEND_DESC_STR, false);
                        mv.visitLdcInsn(separator);
                        mv.visitMethodInsn(INVOKEVIRTUAL, STRING_BUILDER_NAME, STRING_BUILDER_APPEND, STRING_BUILDER_APPEND_DESC_STR, false);
                    }
                    break;
                case spanId:
                    //noinspection Duplicates
                    if (spanIndex >= 0) {
                        //handle primitive type
                        switch (SimpleMethod.fromType2Desc(spanClass)) {
                            case "Z":
                                mv.visitIntInsn(ILOAD, spanIndex);
                                mv.visitMethodInsn(INVOKESTATIC, BOOLEAN_NAME, BOOLEAN_VALUE_OF, BOOLEAN_VALUE_OF_DESC, false);
                                break;
                            case "B":
                                mv.visitIntInsn(ILOAD, spanIndex);
                                mv.visitMethodInsn(INVOKESTATIC, BYTE_NAME, BYTE_VALUE_OF, BYTE_VALUE_OF_DESC, false);
                                break;
                            case "S":
                                mv.visitIntInsn(ILOAD, spanIndex);
                                mv.visitMethodInsn(INVOKESTATIC, SHORT_NAME, SHORT_VALUE_OF, SHORT_VALUE_OF_DESC, false);
                                break;
                            case "I":
                                mv.visitIntInsn(ILOAD, spanIndex);
                                mv.visitMethodInsn(INVOKESTATIC, INTEGER_NAME, INTEGER_VALUE_OF, INTEGER_VALUE_OF_DESC, false);
                                break;
                            case "F":
                                mv.visitIntInsn(FLOAD, spanIndex);
                                mv.visitMethodInsn(INVOKESTATIC, FLOAT_NAME, FLOAT_VALUE_OF, FLOAT_VALUE_OF_DESC, false);
                                break;
                            case "C":
                                mv.visitIntInsn(ILOAD, spanIndex);
                                mv.visitMethodInsn(INVOKESTATIC, CHARACTER_NAME, CHARACTER_VALUE_OF, CHARACTER_VALUE_OF_DESC, false);
                                break;
                            case "J":
                                mv.visitIntInsn(LLOAD, spanIndex);
                                mv.visitMethodInsn(INVOKESTATIC, LONG_NAME, LONG_VALUE_OF, LONG_VALUE_OF_DESC, false);
                                break;
                            case "D":
                                mv.visitIntInsn(DLOAD, spanIndex);
                                mv.visitMethodInsn(INVOKESTATIC, DOUBLE_NAME, DOUBLE_VALUE_OF, DOUBLE_VALUE_OF_DESC, false);
                                break;
                            default:
                                mv.visitIntInsn(ALOAD, spanIndex);
                                break;
                        }
                        mv.visitMethodInsn(INVOKESTATIC, MEDIATOR_NAME, GEN_SPAN_ID, GEN_SPAN_ID_DESC, false);
                        mv.visitMethodInsn(INVOKEVIRTUAL, STRING_BUILDER_NAME, STRING_BUILDER_APPEND, STRING_BUILDER_APPEND_DESC_STR, false);
                        mv.visitLdcInsn(separator);
                        mv.visitMethodInsn(INVOKEVIRTUAL, STRING_BUILDER_NAME, STRING_BUILDER_APPEND, STRING_BUILDER_APPEND_DESC_STR, false);
                    }
                    break;
                case methodArgs:
                    //handle varargs methods
                    if (descArr.length == 1 && (access & ACC_VARARGS) != 0) {
                        mv.visitVarInsn(ALOAD, offset);

                        invokeArraysToString(mv, descArr[0]);
                        mv.visitMethodInsn(INVOKEVIRTUAL, STRING_BUILDER_NAME, STRING_BUILDER_APPEND, STRING_BUILDER_APPEND_DESC_STR, false);
                        mv.visitLdcInsn(separator);
                        mv.visitMethodInsn(INVOKEVIRTUAL, STRING_BUILDER_NAME, STRING_BUILDER_APPEND, STRING_BUILDER_APPEND_DESC_STR, false);
                    } else {
                        appendMethodArgs(mv, descArr, offset);
                    }
                    break;
                case duration://duration 7
                    //todo add duration
                    mv.visitLdcInsn("");
                    mv.visitMethodInsn(INVOKEVIRTUAL, STRING_BUILDER_NAME, STRING_BUILDER_APPEND, STRING_BUILDER_APPEND_DESC_STR, false);
//                    mv.visitLdcInsn(separator);
//                    mv.visitMethodInsn(INVOKEVIRTUAL, STRING_BUILDER_NAME, STRING_BUILDER_APPEND, STRING_BUILDER_APPEND_DESC_STR, false);
                    break;
                default:
                    break;
            }
        }

        mv.visitMethodInsn(INVOKEVIRTUAL, STRING_BUILDER_NAME, TO_STRING, STRING_BUILDER_TO_STRING_DESC, false);
    }

    /**
     *
     */
    private static void invokeArraysToString(MethodVisitor mv, String desc) {
        //handle types
        if (desc.indexOf(';') > 0) {
            //reference type
            mv.visitMethodInsn(INVOKESTATIC, ARRAYS_NAME, TO_STRING, ARRAYS_TO_STRING_DESC_OBJ, false);
        } else {
            //primitive type
            switch (desc.charAt(1)) {
                case 'Z':
                    mv.visitMethodInsn(INVOKESTATIC, ARRAYS_NAME, TO_STRING, ARRAYS_TO_STRING_DESC_Z, false);
                    break;
                case 'B':
                    mv.visitMethodInsn(INVOKESTATIC, ARRAYS_NAME, TO_STRING, ARRAYS_TO_STRING_DESC_B, false);
                    break;
                case 'S':
                    mv.visitMethodInsn(INVOKESTATIC, ARRAYS_NAME, TO_STRING, ARRAYS_TO_STRING_DESC_S, false);
                    break;
                case 'I':
                    mv.visitMethodInsn(INVOKESTATIC, ARRAYS_NAME, TO_STRING, ARRAYS_TO_STRING_DESC_I, false);
                    break;
                case 'F':
                    mv.visitMethodInsn(INVOKESTATIC, ARRAYS_NAME, TO_STRING, ARRAYS_TO_STRING_DESC_F, false);
                    break;
                case 'C':
                    mv.visitMethodInsn(INVOKESTATIC, ARRAYS_NAME, TO_STRING, ARRAYS_TO_STRING_DESC_C, false);
                    break;
                case 'J':
                    // long/double takes 2 local var index
                    mv.visitMethodInsn(INVOKESTATIC, ARRAYS_NAME, TO_STRING, ARRAYS_TO_STRING_DESC_J, false);
                    break;
                case 'D':
                    mv.visitMethodInsn(INVOKESTATIC, ARRAYS_NAME, TO_STRING, ARRAYS_TO_STRING_DESC_D, false);
                    break;
            }
        }
    }

    /**
     *
     */
    private void appendMethodArgs(final MethodVisitor mv, final String[] args, int offset) {
        for (int i = 0; i < args.length; i++) {
            // primitive types should be box as reference types
            final String arg = args[i];
            final char c = arg.charAt(0);
            switch (c) {
                case 'Z':
                    mv.visitIntInsn(ILOAD, i + offset);
                    mv.visitMethodInsn(INVOKEVIRTUAL, STRING_BUILDER_NAME, STRING_BUILDER_APPEND, STRING_BUILDER_APPEND_DESC_Z, false);
                    break;
                case 'B':
                    mv.visitIntInsn(ILOAD, i + offset);
                    mv.visitMethodInsn(INVOKEVIRTUAL, STRING_BUILDER_NAME, STRING_BUILDER_APPEND, STRING_BUILDER_APPEND_DESC_B, false);
                    break;
                case 'S':
                    mv.visitIntInsn(ILOAD, i + offset);
                    mv.visitMethodInsn(INVOKEVIRTUAL, STRING_BUILDER_NAME, STRING_BUILDER_APPEND, STRING_BUILDER_APPEND_DESC_I, false);
                    break;
                case 'I':
                    mv.visitIntInsn(ILOAD, i + offset);
                    mv.visitMethodInsn(INVOKEVIRTUAL, STRING_BUILDER_NAME, STRING_BUILDER_APPEND, STRING_BUILDER_APPEND_DESC_I, false);
                    break;
                case 'F':
                    mv.visitIntInsn(FLOAD, i + offset);
                    mv.visitMethodInsn(INVOKEVIRTUAL, STRING_BUILDER_NAME, STRING_BUILDER_APPEND, STRING_BUILDER_APPEND_DESC_F, false);
                    break;
                case 'C':
                    mv.visitIntInsn(ILOAD, i + offset);
                    mv.visitMethodInsn(INVOKEVIRTUAL, STRING_BUILDER_NAME, STRING_BUILDER_APPEND, STRING_BUILDER_APPEND_DESC_C, false);
                    break;
                case 'J':
                    // long/double takes 2 local var index
                    mv.visitIntInsn(LLOAD, i + offset++);
                    mv.visitMethodInsn(INVOKEVIRTUAL, STRING_BUILDER_NAME, STRING_BUILDER_APPEND, STRING_BUILDER_APPEND_DESC_J, false);
                    break;
                case 'D':
                    // long/double takes 2 local var index
                    mv.visitIntInsn(DLOAD, i + offset++);
                    mv.visitMethodInsn(INVOKEVIRTUAL, STRING_BUILDER_NAME, STRING_BUILDER_APPEND, STRING_BUILDER_APPEND_DESC_D, false);
                    break;
                default:
                    //String or Object
                    mv.visitIntInsn(ALOAD, i + offset);
                    if (arg.equals("Ljava/lang/String;")) {
                        mv.visitMethodInsn(INVOKEVIRTUAL, STRING_BUILDER_NAME, STRING_BUILDER_APPEND, STRING_BUILDER_APPEND_DESC_STR, false);
                    } else {
                        if (c == '[') {
                            invokeArraysToString(mv, arg);
                            mv.visitMethodInsn(INVOKEVIRTUAL, STRING_BUILDER_NAME, STRING_BUILDER_APPEND, STRING_BUILDER_APPEND_DESC_STR, false);
                        } else {
                            //reference type
                            mv.visitMethodInsn(INVOKEVIRTUAL, STRING_BUILDER_NAME, STRING_BUILDER_APPEND, STRING_BUILDER_APPEND_DESC_OBJ, false);
                        }
                    }
                    break;
            }
            if (i == args.length - 1) {
                mv.visitLdcInsn(separator);
            } else {
                mv.visitLdcInsn(argsSeparator);
            }
            mv.visitMethodInsn(INVOKEVIRTUAL, STRING_BUILDER_NAME, STRING_BUILDER_APPEND, STRING_BUILDER_APPEND_DESC_STR, false);
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