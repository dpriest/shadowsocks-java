package com.dpriest.shadowsocks.core;

import com.dpriest.shadowsocks.core.crypto.SSCrypto;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;

class AddressHandler extends ChannelInboundHandlerAdapter {

    private static Logger logger = LoggerFactory.getLogger(AddressHandler.class);
    private final static int ADDR_TYPE_IPV4 = 1;
    private final static int ADDR_TYPE_HOST = 3;

    private final ByteBuf dataQueue = Unpooled.buffer();
    private final SSCrypto ssCrypto;

    AddressHandler(SSCrypto ssCrypto) {
        this.ssCrypto = ssCrypto;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.info("connected with {}", ctx.channel());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;

        if (buf.readableBytes() <= 0) {
            return;
        }
        byte[] data = ByteBufUtil.getBytes(buf);
        byte[] decrypted = ssCrypto.decrypt(data, data.length);
        dataQueue.writeBytes(decrypted);
        if (dataQueue.readableBytes() < 2) {
            return;
        } String host;
        int port;
        int addressType = dataQueue.getUnsignedByte(0);
        if (addressType == ADDR_TYPE_IPV4) {
            // addrType[1] + ipv4(4) + port(2)
            if (dataQueue.readableBytes() < 7) {
                return;
            }
            dataQueue.readUnsignedByte();
            byte[] ipBytes = new byte[4];
            host = InetAddress.getByAddress(ipBytes).toString().substring(1);
            dataQueue.readBytes(ipBytes);
            port = dataQueue.readShort();
        } else if (addressType == ADDR_TYPE_HOST) {
            int hostLength = dataQueue.getUnsignedByte(1);
            if (dataQueue.readableBytes() < hostLength + 4) {
                return;
            }
            dataQueue.readUnsignedByte();
            dataQueue.readUnsignedByte();
            byte[] hostBytes = new byte[hostLength];
            dataQueue.readBytes(hostBytes);
            host = new String(hostBytes);
            port = dataQueue.readShort();
        } else {
            throw new IllegalStateException("unknown address type" + addressType);
        }
        ctx.channel().pipeline().addLast(new ClientDataHandler(host, port, ctx, buf, ssCrypto));
        ctx.channel().pipeline().remove(this);
    }
}
