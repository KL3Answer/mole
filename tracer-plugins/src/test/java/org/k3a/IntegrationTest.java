package org.k3a;

import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by k3a
 * on 19-1-1  下午6:39
 */
public class IntegrationTest {


    public static void main(String[] args) throws ClassNotFoundException {


        final HashMap<String, String> hashMap = new HashMap<String, String>() {{
            put("2121", "zdgddg");
        }};

        final Bob bob = new Bob();
        final ExecutorService pool = new ThreadPoolExecutor(2, 2, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(), new ThreadPoolExecutor.CallerRunsPolicy());
        while (true) {
            final long start = System.nanoTime();
            bob.doSomething(1.2D, Arrays.asList(hashMap, 2, 5, 6, false), 1, 2, 3, 4, 5, 3.1D, 7);
            final long mid = System.nanoTime();


            final long mid1 = System.nanoTime();
            final String s = "" + 1.2D + Arrays.asList(hashMap, 2, 5, 6, false) + 1 + 2 + 3 + 4 + 5 + 3.1D + 7;
//            System.out.println("" + 1.2D + Arrays.asList(new HashMap(), 2, 5, 6, false) + 1 + 2 + 3 + 4 + 5 + 3.1D + 7);
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


}

