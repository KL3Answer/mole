package org.k3a;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by k3a
 * on 19-1-14  下午7:03
 */
public class OPTest {

    private static final Logger LOGGER = LogManager.getLogger("org.mole.tracer.watcher.WatcherMediator");

    public static void main(String[] args) throws InterruptedException {
//        test01();//236763666
//        test02();//222148329
//        test03();//247942389

//        multiThreadTest01();//2833345854
//        multiThreadTest02();//2286824767
//        multiThreadTest03();//699917899
    }

    private static void test01() {
        final HashMap<String, String> hashMap = new HashMap<String, String>() {{
            put("spanId", "1233213");
        }};
        final Foo foo = new Foo();

        long count = 100_1000;

        while (count-- > 0) {
            foo.doSomething(1.2D, Arrays.asList(hashMap, 2, 5, 6, false), 1, 2, 3, 4, 5, 3.1D, 7);
        }


        count = 100_000;
        long start = System.nanoTime();
        while (count-- > 0) {
            foo.doSomething(1.2D, Arrays.asList(hashMap, 2, 5, 6, false), 1, 2, 3, 4, 5, 3.1D, 7);
        }
        long mid = System.nanoTime();
        System.out.println("\n================");
        System.out.println("\n================");
        System.out.println("\n================");
        System.out.println("\n================");
        System.out.println("\n================");
        System.out.println("\n================");
        System.out.println("\n================");
        System.out.println(mid - start + "\n==append==\n");
    }

    private static void test02() {
        final ExecutorService pool = new ThreadPoolExecutor(2, 2, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(), new ThreadPoolExecutor.CallerRunsPolicy());
        final HashMap<String, String> hashMap = new HashMap<String, String>() {{
            put("spanId", "1233213");
        }};

        long count = 100_1000;
        while (count-- > 0) {
            final String sd = "" + 1.2D + Arrays.asList(hashMap, 2, 5, 6, false) + 1 + 2 + 3 + 4 + 5 + 3.1D + 7;
            pool.execute(() -> {
                LOGGER.info(sd);
            });
        }


        count = 100_000;
        long start = System.nanoTime();
        while (count-- > 0) {
            final String sd = "" + 1.2D + Arrays.asList(hashMap, 2, 5, 6, false) + 1 + 2 + 3 + 4 + 5 + 3.1D + 7;
            pool.execute(() -> {
                LOGGER.info(sd);
            });
        }
        long mid = System.nanoTime();
        System.out.println("\n================");
        System.out.println("\n================");
        System.out.println("\n================");
        System.out.println("\n================");
        System.out.println("\n================");
        System.out.println("\n================");
        System.out.println("\n================");
        System.out.println(mid - start + "\n==async==\n");

    }


    private static void test03() {

        final HashMap<String, String> hashMap = new HashMap<String, String>() {{
            put("spanId", "1233213");
        }};

        final Bob bob = new Bob();

        long count = 100_000;
        while (count-- > 0) {
            bob.doSomething(1.2D, Arrays.asList(hashMap, 2, 5, 6, false), 1, 2, 3, 4, 5, 3.1D, 7);
        }

        count = 100_000;
        long start = System.nanoTime();
        while (count-- > 0) {
            bob.doSomething(1.2D, Arrays.asList(hashMap, 2, 5, 6, false), 1, 2, 3, 4, 5, 3.1D, 7);
        }
        long mid = System.nanoTime();

        System.out.println("\n================");
        System.out.println("\n================");
        System.out.println("\n================");
        System.out.println("\n================");
        System.out.println("\n================");
        System.out.println("\n================");
        System.out.println("\n================");
        System.out.println(mid - start + "\n==disruptor==\n");
    }

    private static void multiThreadTest01() throws InterruptedException {
        final HashMap<String, String> hashMap = new HashMap<String, String>() {{
            put("spanId", "1233213");
        }};
        final Foo foo = new Foo();

        final ExecutorService pool = new ThreadPoolExecutor(4, 4, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(), new ThreadPoolExecutor.CallerRunsPolicy());



        long count = 100_1000;

        while (count-- > 0) {
            pool.execute(() -> {
                foo.doSomething(1.2D, Arrays.asList(hashMap, 2, 5, 6, false), 1, 2, 3, 4, 5, 3.1D, 7);
            });
        }

        count = 100_000;
        long start = System.nanoTime();
        while (count-- > 0) {
            pool.execute(() -> {
                foo.doSomething(1.2D, Arrays.asList(hashMap, 2, 5, 6, false), 1, 2, 3, 4, 5, 3.1D, 7);
            });
        }

        pool.shutdown();
        pool.awaitTermination(1,TimeUnit.HOURS);

        long mid = System.nanoTime();
        System.out.println("\n================");
        System.out.println("\n================");
        System.out.println("\n================");
        System.out.println("\n================");
        System.out.println("\n================");
        System.out.println("\n================");
        System.out.println("\n================");
        System.out.println(mid - start + "\n==append==\n");
    }

    private static void multiThreadTest02() throws InterruptedException {
        final ExecutorService pool = new ThreadPoolExecutor(2, 2, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(), new ThreadPoolExecutor.CallerRunsPolicy());
        final HashMap<String, String> hashMap = new HashMap<String, String>() {{
            put("spanId", "1233213");
        }};

        final ExecutorService ex = new ThreadPoolExecutor(4, 4, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(), new ThreadPoolExecutor.CallerRunsPolicy());

        long count = 100_1000;
        while (count-- > 0) {
            ex.execute(() -> {
                final String sd = "" + 1.2D + Arrays.asList(hashMap, 2, 5, 6, false) + 1 + 2 + 3 + 4 + 5 + 3.1D + 7;
                pool.execute(() -> {
                    LOGGER.info(sd);
                });
            });
        }


        count = 100_000;
        long start = System.nanoTime();
        while (count-- > 0) {
            ex.execute(() -> {
                final String sd = "" + 1.2D + Arrays.asList(hashMap, 2, 5, 6, false) + 1 + 2 + 3 + 4 + 5 + 3.1D + 7;
                pool.execute(() -> {
                    LOGGER.info(sd);
                });
            });
        }

        ex.shutdown();
        ex.awaitTermination(1,TimeUnit.HOURS);
        long mid = System.nanoTime();
        System.out.println("\n================");
        System.out.println("\n================");
        System.out.println("\n================");
        System.out.println("\n================");
        System.out.println("\n================");
        System.out.println("\n================");
        System.out.println("\n================");
        System.out.println(mid - start + "\n==async==\n");
    }


    private static void multiThreadTest03() throws InterruptedException {
        final HashMap<String, String> hashMap = new HashMap<String, String>() {{
            put("spanId", "1233213");
        }};

        final ExecutorService pool = new ThreadPoolExecutor(4, 4, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(), new ThreadPoolExecutor.CallerRunsPolicy());

        final Bob bob = new Bob();

        long count = 100_000;
        while (count-- > 0) {
            pool.execute(() -> {
                bob.doSomething(1.2D, Arrays.asList(hashMap, 2, 5, 6, false), 1, 2, 3, 4, 5, 3.1D, 7);
            });
        }

        count = 100_000;
        long start = System.nanoTime();
        while (count-- > 0) {
            pool.execute(() -> {
                bob.doSomething(1.2D, Arrays.asList(hashMap, 2, 5, 6, false), 1, 2, 3, 4, 5, 3.1D, 7);
            });
        }
        pool.shutdown();
        pool.awaitTermination(1,TimeUnit.HOURS);

        long mid = System.nanoTime();

        System.out.println("\n================");
        System.out.println("\n================");
        System.out.println("\n================");
        System.out.println("\n================");
        System.out.println("\n================");
        System.out.println("\n================");
        System.out.println("\n================");
        System.out.println(mid - start + "\n==disruptor==\n");
    }

    static class Foo {
        public void doSomething(double s, List obj, int a, int b, int d, int e, int f, double g, int v) {
            final StringBuilder sb = new StringBuilder();
            sb.append(s).append('|')
                    .append(obj).append('|')
                    .append(a).append('|')
                    .append(b).append('|')
                    .append(d).append('|')
                    .append(e).append('|')
                    .append(f).append('|')
                    .append(g).append('|')
                    .append(v).append('|')
                    .append(Thread.currentThread()).append('|')
                    .append(System.currentTimeMillis()).append('|')
                    .append("doSomething").append('|');
            LOGGER.info(sb);
        }

    }

}
