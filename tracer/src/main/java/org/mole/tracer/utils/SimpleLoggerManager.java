package org.mole.tracer.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by k3a
 * on 19-1-2  下午7:46
 * <p>
 */
public enum SimpleLoggerManager {

    INSTANCE;

    //    private static final Logger LOGGER = LogManager.getLogger(SimpleLoggerManager.class);
    private static  Logger LOGGER;

    static {
        try {

            LOGGER = LogManager.getLogger(SimpleLoggerManager.class);
        } catch (Exception e) {
//            LOGGER = null;
            e.printStackTrace();
        }
    }

    public static void info(String msg) {
        LOGGER.info(msg);
    }

    public static void error(String msg) {
        LOGGER.error(msg);
    }

    public static void logFullStackTrace(Throwable throwable) {
        LOGGER.catching(throwable);
    }


}
