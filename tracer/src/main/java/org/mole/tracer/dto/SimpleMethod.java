package org.mole.tracer.dto;

import org.mole.tracer.utils.SimpleLoggerManager;
import org.mole.tracer.utils.SimpleStringUtils;

import java.util.*;

/**
 * Created by k3a
 * on 19-1-2  下午10:56
 */
@SuppressWarnings("WeakerAccess")
public class SimpleMethod {

    public static final String[] MATCH_ALL = new String[0];

    public static final String WILDCARD = ".";

    public static final String VOID = "()";

    public static final Map<String, String> PT = new HashMap<>();
    public static final Map<Character, String> R_PT = new HashMap<>();
    public static final Set<String> PT_ARR = new HashSet<>();

    static {
        PT.put("boolean", "Z");
        PT.put("byte", "B");
        PT.put("short", "S");
        PT.put("int", "I");
        PT.put("char", "C");
        PT.put("long", "J");
        PT.put("float", "F");
        PT.put("double", "D");

        R_PT.put('Z', "boolean");
        R_PT.put('B', "byte");
        R_PT.put('S', "short");
        R_PT.put('I', "int");
        R_PT.put('C', "char");
        R_PT.put('J', "long");
        R_PT.put('F', "float");
        R_PT.put('D', "double");

        PT_ARR.add("[B");
        PT_ARR.add("[S");
        PT_ARR.add("[Z");
        PT_ARR.add("[I");
        PT_ARR.add("[C");
        PT_ARR.add("[J");
        PT_ARR.add("[F");
        PT_ARR.add("[D");

    }

    public final String className;
    public final String methodName;
    public final String[] argClassName;
    public final String argDesc;

    public SimpleMethod(String className, String methodName, String[] argClassName) {
        this.className = className;
        this.methodName = methodName;
        this.argClassName = argClassName;
        this.argDesc = fromType2Desc(this.argClassName);
    }

    /**
     * method args desc (without return Type)
     */
    @SuppressWarnings("Duplicates")
    public static String fromType2Desc(String[] argClassName) {
        if (argClassName == null) {
            return VOID;
        } else if (argClassName == MATCH_ALL) {
            return WILDCARD;
        } else {
            StringBuilder sb = new StringBuilder("(");
            for (String s : argClassName) {
                String tmp = PT.get(s);
                if (tmp != null) {
                    sb.append(tmp);
                } else if (PT_ARR.contains(s)) {
                    sb.append(s);
                } else {
                    sb.append("L").append(s.replace(".", "/")).append(";");
                }
            }
            sb.append(")");
            return sb.toString();
        }
    }

    @SuppressWarnings("Duplicates")
    public static String fromType2Desc(String type) {
        if (type == null) {
            return null;
        } else if (type.equals("")) {
            return "";
        } else {
            StringBuilder sb = new StringBuilder();
            String tmp = PT.get(type);
            if (tmp != null) {
                sb.append(tmp);
            } else if (PT_ARR.contains(type)) {
                sb.append(type);
            } else {
                sb.append("L").append(type.replace(".", "/")).append(";");
            }
            return sb.toString();
        }
    }

    public static String[] fromDesc2Type(String argDesc) {
        if (SimpleStringUtils.isBlank(argDesc)) {
            return new String[0];
        }

        final int length = argDesc.length();
        final ArrayList<String> list = new ArrayList<>(length);

        for (int i = 0; i < length; i++) {
            char c = argDesc.charAt(i);
            String s = R_PT.get(c);
            if (s != null) {
                list.add(s);
                continue;
            }

            if (c == '[') {
                String tmp = "[" + argDesc.charAt(i + 1);
                if (PT_ARR.contains(tmp)) {
                    list.add(tmp);
                    i++;
                } else if (tmp.equals("[L")) {
                    int end = argDesc.indexOf(';', i);
                    String substring = argDesc.substring(i + 2, end + 1).replace('/', '.');
                    list.add("[" + substring);
                    i += (end - i);
                }
            } else if (c == 'L') {
                int end = argDesc.indexOf(';', i + 1);
                list.add(argDesc.substring(i + 1, end).replace('/', '.'));
                i += (end - i);
            }
        }

        return list.toArray(new String[0]);
    }

    public static String fromDesc2TypeString(String argDesc) {
        if (SimpleStringUtils.isBlank(argDesc)) {
            return "";
        }

        final int length = argDesc.length();
        final StringBuilder sb = new StringBuilder();

        for (int i = 0; i < length; i++) {
            char c = argDesc.charAt(i);
            String s = R_PT.get(c);
            if (s != null) {
                sb.append(s).append(',');
                continue;
            }

            if (c == '[') {
                String tmp = "[" + argDesc.charAt(i + 1);
                if (PT_ARR.contains(tmp)) {
                    sb.append(tmp).append(',');
                    i++;
                } else if (tmp.equals("[L")) {
                    int end = argDesc.indexOf(';', i);
                    String substring = argDesc.substring(i + 2, end + 1).replace('/', '.');
                    sb.append('[').append(substring).append(',');
                    i += (end - i);
                }
            } else if (c == 'L') {
                int end = argDesc.indexOf(';', i + 1);
                sb.append(argDesc.substring(i + 1, end).replace('/', '.')).append(',');
                i += (end - i);
            }
        }

        final int i = sb.lastIndexOf(",");
        return sb.replace(i, i + 1, "").toString();
    }

    public static String[] splitArgsDesc(String argDesc) {
        if (SimpleStringUtils.isBlank(argDesc)) {
            return new String[0];
        }

        final int length = argDesc.length();
        final ArrayList<String> list = new ArrayList<>(length);

        for (int i = 0; i < length; i++) {
            char c = argDesc.charAt(i);
            String s = R_PT.get(c);
            if (s != null) {
                list.add(String.valueOf(c));
                continue;
            }

            if (c == '[') {
                String tmp = "[" + argDesc.charAt(i + 1);
                if (PT_ARR.contains(tmp)) {
                    list.add(tmp);
                    i++;
                } else if (tmp.equals("[L")) {
                    int end = argDesc.indexOf(';', i);
                    list.add(argDesc.substring(i, end + 1));
                    i += (end - i);
                }
            } else if (c == 'L') {
                int end = argDesc.indexOf(';', i + 1);
                list.add(argDesc.substring(i, end + 1));
                i += (end - i);
            }
        }

        return list.toArray(new String[0]);
    }

    /**
     * e.g.
     * <p>
     * org.k3a.test.Bob#doSomething(java.lang.String,int)
     * <p>
     * org.k3a.test.Bob#doSomething([Ljava.lang.Double;)
     * <p>
     * org.k3a.test.Bob#doSomething([D;)
     * <p>
     * org.k3a.test.Bob#doSomethingElse(.) '.' as wildcard
     * <p>
     * org.k3a.test.Bob#doNothing() no args
     */
    public static SimpleMethod of(String methodDescription) {
        if (SimpleStringUtils.isBlank(methodDescription)) {
            return null;
        }
        final String[] split = methodDescription.trim().split("#");
        if (split.length != 2) {
            SimpleLoggerManager.error("illegal format methods description:" + methodDescription);
            return null;
        }

        try {
            final int i = split[1].indexOf('(');
            final int j = split[1].indexOf(')');
            if (i <= 0 || i > j || j != split[1].length() - 1 || i != split[1].lastIndexOf('(')) {
                SimpleLoggerManager.error("malformed methods description:" + methodDescription);
                return null;
            }

            final String methodName = split[1].substring(0, i).trim();

            final String[] methodArgs;
            String rawArgStr = split[1].substring(i + 1, split[1].length() - 1);
            if (SimpleStringUtils.isBlank(rawArgStr)) {
                methodArgs = null;
            } else if (rawArgStr.equals(SimpleMethod.WILDCARD)) {
                methodArgs = SimpleMethod.MATCH_ALL;
            } else {
                methodArgs = SimpleStringUtils.trim(rawArgStr.split(","));
            }

            return new SimpleMethod(split[0], methodName, methodArgs);
        } catch (Exception e) {
            SimpleLoggerManager.logFullStackTrace(e);
            return null;
        }
    }


    /**
     * as above
     */
    public static SimpleMethod[] of(String methodDescription[]) {
        if (methodDescription == null || methodDescription.length == 0) {
            return null;
        }
        SimpleMethod[] methods = new SimpleMethod[methodDescription.length];
        for (int i = 0; i < methodDescription.length; i++) {
            SimpleMethod method = of(methodDescription[i]);
            if (method == null) {
                return null;
            }
            methods[i] = method;
        }
        return methods;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimpleMethod that = (SimpleMethod) o;
        return Objects.equals(className, that.className) &&
                Objects.equals(methodName, that.methodName) &&
                Arrays.equals(argClassName, that.argClassName);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(className, methodName);
        result = 31 * result + Arrays.hashCode(argClassName);
        return result;
    }
}
