package org.mole.tracer.watcher;

import com.lmax.disruptor.EventTranslatorOneArg;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.SleepingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import internal.io.netty.buffer.ByteBuf;
import internal.io.netty.buffer.PooledByteBufAllocator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mole.tracer.consumer.kcp.KcpClient;
import org.mole.tracer.consumer.kcp.TracerKCPClient;
import org.mole.tracer.context.TracerContext;
import org.mole.tracer.dto.MsgEvent;
import org.mole.tracer.plugins.TraceHelper;
import org.mole.tracer.utils.SimpleLoggerManager;

import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

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

    private static KcpClient kcpClient;

    private static final EventTranslatorOneArg<MsgEvent, String> TRANSLATOR = (event, seq, str) -> event.setMsg(str);

    private static RingBuffer<MsgEvent> ringBuffer;

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

        //todo handle config
        final Disruptor<MsgEvent> disruptor = new Disruptor<>(MsgEvent::new, 1024 * 1024 * 16, new ThreadFactory() {
            private final AtomicInteger poolNumber = new AtomicInteger(1);

            @Override
            public Thread newThread(@SuppressWarnings("NullableProblems") Runnable r) {
                Thread t = new Thread(r, "mole-disruptor-" + poolNumber.getAndIncrement());
                t.setDaemon(true);
                t.setPriority(Thread.NORM_PRIORITY);
                return t;
            }
        }, ProducerType.MULTI, new SleepingWaitStrategy());

        disruptor.handleEventsWith((event, seq, end) -> {
            final String record = event.getMsg();
            if (kcpClient != null && kcpClient.isRunning()) {
                ByteBuf bb = PooledByteBufAllocator.DEFAULT.buffer(1500);
                for (int i = 0; i < record.length(); i++) {
                    bb.writeChar(record.charAt(i));
                }
                if (!kcpClient.send(bb)) {
                    LOGGER.info(record);
                }
            } else {
                LOGGER.info(record);
            }
        });

        disruptor.start();
        ringBuffer = disruptor.getRingBuffer();
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
        }
        return false;
    }


    /**
     * gather profile info
     * <p>
     * <p>
     * extra format:
     * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     * | methodName(String) | methodDesc(String) | currentThread(Thread)| currentTimeMills(Long) | trace(Object) | span(Object) |method args(Object) |duration(Long,not finished)|
     * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
     * <p>
     * args format:
     * <p>
     * extra is an Object array
     * <p>
     * when args is a primitive type array ,the watched method is vararg and receive primitive type args
     */
    @SuppressWarnings("unused")
    public static void doRecord(String record) {
        try {
            ringBuffer.publishEvent(TRANSLATOR, record);
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
