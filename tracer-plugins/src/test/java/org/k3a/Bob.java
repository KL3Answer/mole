package org.k3a;

import org.mole.tracer.plugins.TraceHelper;

import java.util.List;

/**
 * Created by k3a
 * on 19-1-3  下午8:44
 */
public class Bob {

    public void doSomething(double s, List obj, int a, int b, int d, int e, int f, double g, int v) {
        //code


    }

}

class Pro {
    public static volatile TraceHelper traceHelper = new TestTraceHelper();


    public static String genTraceId(Object obj){
        return "asdas";
    }

    public static String genSpanId(Object obj){
        return "1212";
    }

    public static boolean needRecord() {
        return true;
    }

    public static void doRecord(Object extra[], Object args) {

    }
}
