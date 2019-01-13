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


//    public void doSomething(String s, int a, float b, short d, char e,boolean f,boolean g,String v) {
//    }

//    public void doSomething(float s, int a, int b, int d, int e,int f,boolean g,String v,boolean io) {
//    }

//    public void doSomething(String s, int a, double b, float c, short d, char e, boolean f, long g, byte h) {
//    }
//
//    public void doSomething(String s, int a, double b, float c, short d, char e[], boolean f, long g, byte h) {
//    }
//
//    public void doSomething(String s, int a, double b, float c[], short d[], char e[], boolean f[], long[] g, byte[] h, Object o) {
//    }

    //    public void doSomething(Object... obj) {
//        System.out.println("1212121");
//    }
//
//    public static void doSomething(String a) {
//
//    }
//
//    public void doSomething(int... objects) {
//    }
//
//    public void doSomething(String s, int a) {
//    }
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
