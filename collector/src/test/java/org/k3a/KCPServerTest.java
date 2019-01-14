package org.k3a;

import org.mole.collector.consumer.KCPServerConsumer;

/**
 * Created by k3a
 * on 19-1-14  下午1:49
 */
public class KCPServerTest {

    public static void main(String[] args) {
        new KCPServerConsumer(2222, 2, System.out::println);
    }
}
