package com.dpriest.shadowsocks.core;

import com.dpriest.shadowsocks.core.crypto.SSCrypto;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.util.concurrent.atomic.AtomicReference;

public class ClientDataHandler extends ChannelInboundHandlerAdapter {

    private static Logger logger = LoggerFactory.getLogger(ClientDataHandler.class);
    private final SSCrypto ssCrypto;
    private final AtomicReference<Channel> remoteChannel = new AtomicReference<>();
    private final ByteBuf clientCache;

    ClientDataHandler(String host, int port, ChannelHandlerContext clientCtx, ByteBuf clientCache, SSCrypto ssCrypto) {
        this.ssCrypto = ssCrypto;
        this.clientCache = clientCache;
        init(host, port, clientCtx, clientCache, ssCrypto);
    }

    private void init(final String host, final int port, final ChannelHandlerContext clientCtx, final ByteBuf byteBuf, final SSCrypto ssCrypto) {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(clientCtx.channel().eventLoop())
                .channel(NioSocketChannel.class)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5 * 1000)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new RemoteDataHandler(clientCtx, ssCrypto, byteBuf));
                    }
                });
        try {
            ChannelFuture channelFuture = bootstrap.connect(InetAddress.getByName(host), port);
            channelFuture.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (future.isSuccess()) {
                        logger.info("success to connect to {}:{}", host, port);
                    } else {
                        logger.info("error to connect to {}:{}", host, port);
                        clientCtx.close();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            clientCtx.close();
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;
        if (buf.readableBytes() <= 0) {
            return;
        }
        byte[] bytes = ByteBufUtil.getBytes(buf);
        byte[] decrypt = ssCrypto.decrypt(bytes, bytes.length);
        if (remoteChannel.get() == null) {
            clientCache.writeBytes(decrypt);
        } else {
            remoteChannel.get().writeAndFlush(Unpooled.copiedBuffer(decrypt));
        }
    }

    public static class RemoteDataHandler extends SimpleChannelInboundHandler<ByteBuf> {

        private final ChannelHandlerContext clientCtx;
        private final SSCrypto ssCrypto;
        private final ByteBuf byteBuf;

        RemoteDataHandler(ChannelHandlerContext clientCtx, SSCrypto ssCrypto, ByteBuf byteBuf) {
            this.clientCtx = clientCtx;
            this.ssCrypto = ssCrypto;
            this.byteBuf = byteBuf;
        }

        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            ctx.writeAndFlush(byteBuf);
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
            byte[] bytes = ByteBufUtil.getBytes(msg);
            try {
                byte[] encrypt = ssCrypto.encrypt(bytes, bytes.length);
                clientCtx.writeAndFlush(Unpooled.copiedBuffer(encrypt));
            } catch (Exception e) {
                ctx.close();
                clientCtx.close();
            }
        }
    }
}
