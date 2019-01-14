package org.mole.collector.consumer;

import java.util.function.Consumer;

/**
 * Created by k3a
 * on 19-1-14  下午1:45
 */
public class KCPServerConsumer {

    public KCPServerConsumer(int port, int workSize, Consumer<String> consumer) {
        //todo handle args
        TracerKCPServer s = new TracerKCPServer(2222, 1, consumer);
        s.noDelay(1, 10, 2, 1);
        s.setMinRto(10);
        s.wndSize(64, 64);
        s.setTimeout(10 * 1000);
        s.setMtu(512);
        s.start();
    }

}
