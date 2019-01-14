package org.k3a;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by k3a
 * on 19-1-1  下午6:39
 */
public class IntegrationTest {

    @SuppressWarnings("Duplicates")
    public static void main(String[] args) throws ClassNotFoundException, InterruptedException {

        final HashMap<String, String> hashMap = new HashMap<String, String>() {{
            put("spanId", "1233213");
        }};

        final Bob bob = new Bob();
        final Foo foo = new Foo();
        final ExecutorService pool = new ThreadPoolExecutor(2, 2, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(), new ThreadPoolExecutor.CallerRunsPolicy());
        while (true) {
//            Thread.sleep(100);
            final long start = System.nanoTime();
            bob.doSomething(1.2D, Arrays.asList(hashMap, 2, 5, 6, false), 1, 2, 3, 4, 5, 3.1D, 7);
            final long mid = System.nanoTime();


            final long mid1 = System.nanoTime();
            final String s = "" + 1.2D + Arrays.asList(hashMap, 2, 5, 6, false) + 1 + 2 + 3 + 4 + 5 + 3.1D + 7 + System.currentTimeMillis() + Thread.currentThread();

//            System.out.println("" + 1.2D + Arrays.asList(hashMap, 2, 5, 6, false) + 1 + 2 + 3 + 4 + 5 + 3.1D + 7+System.currentTimeMillis()+Thread.currentThread());
//            foo.doSomething(1.2D, Arrays.asList(hashMap, 2, 5, 6, false), 1, 2, 3, 4, 5, 3.1D, 7);
            final long l = System.nanoTime() - mid1;


            final long a = System.nanoTime();
            final String sd = "" + 1.2D + Arrays.asList(hashMap, 2, 5, 6, false) + 1 + 2 + 3 + 4 + 5 + 3.1D + 7;
            pool.execute(() -> {
//                System.out.print(sd);
            });
            final long b = System.nanoTime() - a;

            System.out.println(mid - start + "|" + l + "|" + b);
//
        }

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
//            LOGGER.info(sb.toString());
        }

    }

}


