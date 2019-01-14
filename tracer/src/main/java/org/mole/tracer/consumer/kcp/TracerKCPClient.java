package org.mole.tracer.consumer.kcp;

import internal.io.netty.buffer.ByteBuf;
import internal.io.netty.buffer.PooledByteBufAllocator;
import org.mole.tracer.utils.SimpleLoggerManager;

import java.nio.charset.Charset;

/**
 * Created by k3a
 * on 19-1-14  下午1:22
 */
public class TracerKCPClient extends KcpClient {

    /**
     * send record msg
     */
    @Override
    public void handleReceive(ByteBuf bb, KcpOnUdp kcp) {
        String content = bb.toString(Charset.forName("utf-8"));
        ByteBuf buf = PooledByteBufAllocator.DEFAULT.buffer(2048);
        buf.writeBytes(content.getBytes(Charset.forName("utf-8")));
        kcp.send(buf);
        bb.release();
    }

    /**
     * 必须关闭，否则可能会导致OOM
     */
    @Override
    public void handleException(Throwable ex, KcpOnUdp kcp) {
        close();
    }

    @Override
    public void handleClose(KcpOnUdp kcp) {
        super.handleClose(kcp);
        SimpleLoggerManager.info("server left:" + kcp);
        SimpleLoggerManager.info("waitSnd:" + kcp.getKcp().waitSnd());
    }

    @Override
    public void out(ByteBuf msg, Kcp kcp, Object user) {
        super.out(msg, kcp, user);
    }
}
