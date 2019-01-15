package org.mole.collector.consumer;

import internal.io.netty.buffer.ByteBuf;
import org.mole.tracer.consumer.kcp.KcpOnUdp;
import org.mole.tracer.consumer.kcp.KcpServer;

import java.nio.charset.Charset;
import java.util.function.Consumer;

/**
 * Created by k3a
 * on 19-1-14  下午1:46
 */
public class TracerKCPServer extends KcpServer {

    private Consumer<String> consumer;

    public TracerKCPServer(int port, int workerSize, Consumer<String> consumer) {
        super(port, workerSize);
        this.consumer = consumer;
    }

    @Override
    public void handleReceive(ByteBuf bb, KcpOnUdp kcp) {
        String content = bb.toString(Charset.forName("utf-8"));
        consumer.accept(content);
//        if (c < 10000) {
//            kcp.send(bb);//echo
//        } else {
//            System.out.println("cost:" + (System.currentTimeMillis() - start));
//        }
    }

    @Override
    public void handleException(Throwable ex, KcpOnUdp kcp) {
        ex.printStackTrace();
    }

    @Override
    public void handleClose(KcpOnUdp kcp) {
        System.out.println("client left:" + kcp);
        System.out.println("waitSnd:" + kcp.getKcp().waitSnd());
    }


}
