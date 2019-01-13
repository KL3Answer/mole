package org.mole.tracer.plugins;

/**
 * Created by k3a
 * on 19-1-9  下午10:39
 * <p>
 * sub class must have a no arg constructor
 */
public interface TraceHelper {

    String genTraceId(Object object);

    String genSpanId(Object object);

}
