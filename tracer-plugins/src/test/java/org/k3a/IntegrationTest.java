package org.k3a;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

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

        while (true) {
            final long start = System.nanoTime();
            bob.doSomething(1.2D, Arrays.asList(hashMap, 2, 5, 6, false), 1, 2, 3, 4, 5, 3.1D, 7);
            final long e = System.nanoTime()-start;


            final long mid1 = System.nanoTime();
            foo.doSomething(1.2D, Arrays.asList(hashMap, 2, 5, 6, false), 1, 2, 3, 4, 5, 3.1D, 7);
            final long l = System.nanoTime() - mid1;

            System.out.println(e + "|" + l);

        }

    }


}

class Foo {
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
        sb.toString();
//            System.out.println(sb);
    }

}

