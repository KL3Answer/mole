package org.mole.tracer.watcher;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mole.tracer.context.TracerContext;
import org.mole.tracer.plugins.TraceHelper;
import org.mole.tracer.utils.SimpleLoggerManager;

import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by k3a
 * on 19-1-7  下午8:25
 */
public enum WatcherMediator {

    @SuppressWarnings("unused") INSTANCE;

    private static final AtomicBoolean isInit = new AtomicBoolean(false);

    private static TracerContext context;

    private static TraceHelper traceHelper;

    private static final Logger LOGGER = LogManager.getLogger(WatcherMediator.class);


    public static void init(TracerContext _context) {
        if (!isInit.compareAndSet(false, true)) {
            return;
        }
        Objects.requireNonNull(_context, "context can not be null");
        context = _context;

        try {
            final Class<?> clazz = Class.forName(_context.config._helperClass);
            traceHelper = (TraceHelper) clazz.newInstance();
        } catch (Exception e) {
            SimpleLoggerManager.logFullStackTrace(e);
        }

    }

    /**
     * use catching exception instead of checking init status
     * and avoid checking init status every time
     */
    @SuppressWarnings("unused")
    public static boolean needRecord() {
        try {
            return ThreadLocalRandom.current().nextInt(100) < context.config._rate;
        } catch (NullPointerException e) {
            // maybe not init yet
            SimpleLoggerManager.error("WatcherMediator maybe not init yet!");
            SimpleLoggerManager.logFullStackTrace(e);
        } catch (Throwable e) {
            SimpleLoggerManager.logFullStackTrace(e);
        }
        return false;
    }


    /**
     * gather profile info
     * <p>
     * <p>
     * extra format:
     * String       String        Thread           Long         String String
     * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     * | methodName | methodDesc | currentThread| currentTimeMills | trace | span |
     * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     * <p>
     * args format:
     * <p>
     * extra is an Object array
     * <p>
     * when args is a primitive type array ,the watched method is vararg and receive primitive type args
     */
    @SuppressWarnings("unused")
    public static void doRecord(Object extra[], Object args) {
        try {
            final String separator = context.config._separator;

            final StringBuilder sb = new StringBuilder();
            // handle extra
            if (extra != null) {
                for (int i = 0; i < extra.length; i++) {
                    switch (i) {
                        case 0://methodName
                        case 1://methodDesc
                            if (extra[i] != null) {
                                sb.append((String) extra[i]).append(separator);
                            }
                            break;
                        case 2://Thread
                            if (extra[i] != null) {
                                sb.append(((Thread) extra[i]).getName()).append(separator);
                            }
                            break;
                        case 3://timemills
                            if (extra[i] != null) {
                                sb.append(((Long) extra[i]).toString()).append(separator);
                            }
                            break;
                        case 4://traceId
                        case 5://spanId
                            sb.append(extra[i]).append(separator);
                            break;
                        //todo handle duration
                    }
                }
            }

            // use an extra _separator to separate extra info and method args
            sb.append(separator);

            //handle method args
            if (args == null) {
                sb.append("null").append(separator);
            } else if (args instanceof Object[]) {
                for (int i = 0; i < ((Object[]) args).length; i++) {
                    sb.append(String.valueOf(((Object[]) args)[i])).append(separator);
                }
            } else if (args instanceof boolean[]) {
                for (int i = 0; i < ((boolean[]) args).length; i++) {
                    sb.append(String.valueOf(((boolean[]) args)[i])).append(separator);
                }
            } else if (args instanceof byte[]) {
                for (int i = 0; i < ((byte[]) args).length; i++) {
                    sb.append(String.valueOf(((byte[]) args)[i])).append(separator);
                }
            } else if (args instanceof short[]) {
                for (int i = 0; i < ((short[]) args).length; i++) {
                    sb.append(String.valueOf(((short[]) args)[i])).append(separator);
                }
            } else if (args instanceof int[]) {
                for (int i = 0; i < ((int[]) args).length; i++) {
                    sb.append(String.valueOf(((int[]) args)[i])).append(separator);
                }
            } else if (args instanceof char[]) {
                for (int i = 0; i < ((char[]) args).length; i++) {
                    sb.append(String.valueOf(((char[]) args)[i])).append(separator);
                }
            } else if (args instanceof float[]) {
                for (int i = 0; i < ((float[]) args).length; i++) {
                    sb.append(String.valueOf(((float[]) args)[i])).append(separator);
                }
            } else if (args instanceof long[]) {
                for (int i = 0; i < ((long[]) args).length; i++) {
                    sb.append(String.valueOf(((long[]) args)[i])).append(separator);
                }
            } else if (args instanceof double[]) {
                for (int i = 0; i < ((double[]) args).length; i++) {
                    sb.append(String.valueOf(((double[]) args)[i])).append(separator);
                }
            } else {
                sb.append(args).append(separator);
            }
            //send or write to disk
            LOGGER.info(sb.toString());
        } catch (Throwable e) {
            SimpleLoggerManager.logFullStackTrace(e);
        }
    }

    /**
     * generate trace id
     */
    @SuppressWarnings("unused")
    public static String genTraceId(Object object) {
        try {
            if (traceHelper != null) {
                return traceHelper.genTraceId(object);
            } else {
                return null;
            }
        } catch (Exception e) {
            SimpleLoggerManager.logFullStackTrace(e);
        }
        return null;
    }

    /**
     * generate span id
     */
    @SuppressWarnings("unused")
    public static String genSpanId(Object object) {
        try {
            if (traceHelper != null) {
                return traceHelper.genSpanId(object);
            } else {
                return null;
            }
        } catch (Exception e) {
            SimpleLoggerManager.logFullStackTrace(e);
        }
        return null;
    }
}
