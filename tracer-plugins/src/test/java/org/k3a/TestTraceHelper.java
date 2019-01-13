package org.k3a;

import org.mole.tracer.plugins.TraceHelper;

import java.util.Map;

/**
 * Created by k3a
 * on 19-1-10  下午5:26
 */
public class TestTraceHelper implements TraceHelper {

    @Override
    public String genTraceId(Object object) {
        if (object instanceof Map) {
            return ((Map) object).get("trace").toString();
        } else {
            return object.toString();
        }
    }

    @Override
    public String genSpanId(Object object) {
        if (object instanceof Map) {
            return ((Map) object).get("span").toString();
        } else {
            return object.toString();
        }
    }
}
