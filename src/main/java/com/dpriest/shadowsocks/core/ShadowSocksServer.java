package com.dpriest.shadowsocks.core;

import com.dpriest.shadowsocks.core.crypto.CryptoFactory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ShadowSocksServer {
    private static Logger logger = LoggerFactory.getLogger(ShadowSocksServer.class);

    void start(String serverHost, int serverPort, final String cryptoMethod, final String password) {
        NioEventLoopGroup group = new NioEventLoopGroup(2);
        ServerBootstrap bootstrap = new ServerBootstrap();
        try {
            bootstrap.group(group)
                    .channel(NioServerSocketChannel.class)
                    .localAddress(serverHost, serverPort)
                    .option(ChannelOption.SO_TIMEOUT, 5000)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(new AddressHandler(CryptoFactory.create(cryptoMethod, password)));
                        }
                    });
            ChannelFuture channelFuture = bootstrap.bind().sync();
            logger.info("started and listen on " + channelFuture.channel().localAddress());
            channelFuture.channel().closeFuture().sync();

        } catch (Exception e) {
            logger.error("start server fail: {}", e);
        } finally {
            group.shutdownGracefully();
        }
    }
}
