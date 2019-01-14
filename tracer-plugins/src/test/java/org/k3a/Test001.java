package org.k3a;

/**
 * Created by k3a
 * on 19-1-13  下午9:53
 */
public class Test001 {

    public static void main(String[] args) {


        long a = Integer.MAX_VALUE;
        final long l = System.currentTimeMillis();
        while (a > 0) {
            a--;
        }
        System.out.println(System.currentTimeMillis() - l);

    }
}
