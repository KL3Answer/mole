package org.mole.tracer.watcher;

import internal.io.netty.buffer.ByteBuf;
import internal.io.netty.buffer.PooledByteBufAllocator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mole.tracer.consumer.kcp.KcpClient;
import org.mole.tracer.consumer.kcp.TracerKCPClient;
import org.mole.tracer.context.TracerContext;
import org.mole.tracer.plugins.TraceHelper;
import org.mole.tracer.utils.SimpleLoggerManager;

import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.mole.tracer.context.ContextConfig.ConfigValue.methodArgs;

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

    private static final ThreadLocal<StringBuilder> SBTL = ThreadLocal.withInitial(StringBuilder::new);

    private static KcpClient kcpClient;

    private static String separator;
    private static String argsSeparator;

    public static void init(TracerContext _context) {
        if (!isInit.compareAndSet(false, true)) {
            return;
        }
        Objects.requireNonNull(_context, "context can not be null");
        context = _context;

        if ((context.config.getRecordMode() & 2) != 0) {
            // todo multi collector node
            kcpClient = new TracerKCPClient();
            kcpClient.noDelay(1, 20, 2, 1);
            kcpClient.setMinRto(10);
            kcpClient.wndSize(32, 32);
            kcpClient.setTimeout(10 * 1000);
            kcpClient.setMtu(1400);
            //todo handle args
            kcpClient.connect(new InetSocketAddress("localhost", 2222));
            kcpClient.start();
        }

        try {
            final Class<?> clazz = Class.forName(_context.config._helperClass);
            traceHelper = (TraceHelper) clazz.newInstance();
        } catch (Exception e) {
            SimpleLoggerManager.logFullStackTrace(e);
        }

        //separators
        separator = _context.config.get_separator();
        argsSeparator = _context.config.get_argsSeparator();

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
     * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     * | methodName(String) | methodDesc(String) | currentThread(Thread)| currentTimeMills(Long) | trace(Object) | span(Object) |duration(Long,not finished)|method args(Object) |
     * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     * <p>
     * args format:
     * <p>
     * extra is an Object array
     * <p>
     * when args is a primitive type array ,the watched method is vararg and receive primitive type args
     */
    @SuppressWarnings("unused")
    public static void doRecord(Object extra[]) {
        try {

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
                            if (extra[i] != null) {
                                sb.append(extra[i]).append(separator);
                            }
                            break;
                        case 6://duration
                            //todo handle duration
                            sb.append(separator);
                            break;
                        case 7:
                            if (!context.config.getExtra().contains(methodArgs.name())) {
                                continue;
                            }
                            //handle method args
                            final Object args = extra[i];
                            if (args == null) {
                                sb.append("null").append(separator);
                            } else if (args instanceof Object[]) {
                                for (int j = 0; j < ((Object[]) args).length; j++) {
                                    sb.append(((Object[]) args)[j]).append(argsSeparator);
                                }
                            } else if (args instanceof boolean[]) {
                                for (int j = 0; j < ((boolean[]) args).length; j++) {
                                    sb.append(Boolean.toString(((boolean[]) args)[j])).append(argsSeparator);
                                }
                            } else if (args instanceof byte[]) {
                                for (int j = 0; j < ((byte[]) args).length; j++) {
                                    sb.append(Integer.toString(((byte[]) args)[j])).append(argsSeparator);
                                }
                            } else if (args instanceof short[]) {
                                for (int j = 0; j < ((short[]) args).length; j++) {
                                    sb.append(Integer.toString(((byte[]) args)[j])).append(argsSeparator);
                                }
                            } else if (args instanceof int[]) {
                                for (int j = 0; j < ((int[]) args).length; j++) {
                                    sb.append(Integer.toString(((int[]) args)[j])).append(argsSeparator);
                                }
                            } else if (args instanceof char[]) {
                                for (int j = 0; j < ((char[]) args).length; j++) {
                                    sb.append(((char[]) args)[j]).append(argsSeparator);
                                }
                            } else if (args instanceof float[]) {
                                for (int j = 0; j < ((float[]) args).length; j++) {
                                    sb.append(String.valueOf(((float[]) args)[j])).append(argsSeparator);
                                }
                            } else if (args instanceof long[]) {
                                for (int j = 0; j < ((long[]) args).length; j++) {
                                    sb.append(Long.toString(((long[]) args)[j])).append(argsSeparator);
                                }
                            } else if (args instanceof double[]) {
                                for (int j = 0; j < ((double[]) args).length; j++) {
                                    sb.append(Double.toString(((double[]) args)[j])).append(argsSeparator);
                                }
                            } else {
                                sb.append(args).append(separator);
                            }
                            break;
                    }
                }
            }

            final String rs = sb.toString();

////            //send or write to disk
            if (kcpClient != null && kcpClient.isRunning()) {
                ByteBuf bb = PooledByteBufAllocator.DEFAULT.buffer(1500);
                for (int i = 0; i < rs.length(); i++) {
                    bb.writeChar(rs.charAt(i));
                }
                if (!kcpClient.send(bb)) {
                    LOGGER.info(rs);
                }
            } else {
                LOGGER.info(rs);
            }
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
