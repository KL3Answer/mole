package org.k3a;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mole.tracer.watcher.WatcherMediator;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by k3a
 * on 19-1-1  下午6:39
 */
public class IntegrationTest {

    private static final Logger LOGGER = LogManager.getLogger(WatcherMediator.class);
    
    public static void main(String[] args) throws ClassNotFoundException {


//        new Bob().doSomething("233", 1212);
//        new Bob().doSomething("233");
//        new Bob().doSomething("asdfsdf", 1, 2.0F, (short) 1, '1', true, 2L);
//        new Bob().doSomething(1, 2, 3L, 'c', 1.2F, true, 1.2D, (byte) 1, "sdffdssfsd", new Object());
//        new Bob().doSomething(1, 2, 3, 4);

        final HashMap<String, String> hashMap = new HashMap<String, String>() {{
            put("2121", "zdgddg");
        }};

        final Bob bob = new Bob();
        while (true) {
            final long start = System.nanoTime();
            bob.doSomething(1.2D, Arrays.asList(hashMap, 2, 5, 6, false), 1, 2, 3, 4, 5, 3.1D, 7);
            final long mid = System.nanoTime()-start;


            final long mid1 = System.nanoTime();
            LOGGER.info("" + 1.2D + Arrays.asList(new HashMap(), 2, 5, 6, false) + 1 + 2 + 3 + 4 + 5 + 3.1D + 7);
            final long l = System.nanoTime() - mid1;


            System.out.println(mid + "|" + l );

        }

    }


}

class Foo {
    public void save(String s, List<Integer> list, int i, double d[], Long lo[], Map<String, String> map, byte obj, boolean o1, char o2) {
        if (Bar.needRecord()) {
            Bar.pre(new Object[]{"sfsfd", "sdfsdf", Thread.currentThread(), System.currentTimeMillis()}, s, list, i, d, lo, map, obj, o1, o2);
        }
        System.out.println(s + list + obj);
    }


    private void a() {
        long start = System.currentTimeMillis();
        long end = -1;
        try {
            if (Bar.needRecord()) {
                end = 0;
                //gen trace id
                //gen span id
            }


        } catch (Exception e) {
        }


        //some coding


        //
        Bar.pre(new Object[]{}, new Object[]{});
    }

    private void b() {
        long start = System.currentTimeMillis();
        try {
            if (Bar.needRecord()) {
                //gen trace id
                //gen span id

                Bar.pre(new Object[]{}, new Object[]{});
            }
        } catch (Exception e) {
        }

    }


//    public void _save(String s, List<Integer> list, int i, double d[], Long lo[], Map<String, String> map, Object obj, Object o1, Object o2) {
//        if (Bar.needRecord()) {
//            Bar.pre(s, list, i, d, lo, map, obj, o1, o2);
//        }
//        System.out.println(s + list + obj);
//    }

}


class Bar {
    public static void pre(Object extra[], Object... obj) {
        System.out.println(Arrays.asList(obj));
        System.out.println(Arrays.asList(extra));

    }

    public static void mono(Object obj) {

    }

    public static boolean needRecord() {
        return true;
    }

}