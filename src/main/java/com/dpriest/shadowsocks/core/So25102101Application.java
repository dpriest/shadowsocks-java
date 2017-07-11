package com.dpriest.shadowsocks.core;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.ip.IpHeaders;
import org.springframework.integration.ip.tcp.TcpReceivingChannelAdapter;
import org.springframework.integration.ip.tcp.TcpSendingMessageHandler;
import org.springframework.integration.ip.tcp.connection.TcpConnectionOpenEvent;
import org.springframework.integration.ip.tcp.connection.TcpNetServerConnectionFactory;
import org.springframework.integration.ip.tcp.connection.TcpServerConnectionFactory;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.support.MessageBuilder;

import javax.net.SocketFactory;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;

@SpringBootApplication
public class So25102101Application {

    public static void main(String[] args) throws Exception {
        ConfigurableApplicationContext context = new SpringApplicationBuilder(So25102101Application.class)
                .web(false)
                .run(args);
        int port = context.getBean(TcpServerConnectionFactory.class).getPort();
        Socket socket = SocketFactory.getDefault().createSocket("localhost", port);
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        String line = reader.readLine();
        System.out.println(line);
        context.close();
    }

    @Bean
    public TcpReceivingChannelAdapter server(TcpNetServerConnectionFactory cf) {
        TcpReceivingChannelAdapter adapter = new TcpReceivingChannelAdapter();
        adapter.setConnectionFactory(cf);
        adapter.setOutputChannel(inputChannel());
        return adapter;
    }

    @Bean
    public MessageChannel inputChannel() {
        return new QueueChannel();
    }

    @Bean
    public MessageChannel outputChannel() {
        return new DirectChannel();
    }

    @Bean
    public TcpNetServerConnectionFactory cf() {
        return new TcpNetServerConnectionFactory(0);
    }

    @Bean
    public IntegrationFlow outbound() {
        return IntegrationFlows.from(outputChannel())
                .handle(sender())
                .get();
    }

    @Bean
    public MessageHandler sender() {
        TcpSendingMessageHandler tcpSendingMessageHandler = new TcpSendingMessageHandler();
        tcpSendingMessageHandler.setConnectionFactory(cf());
        return tcpSendingMessageHandler;
    }

    @Bean
    public ApplicationListener<TcpConnectionOpenEvent> listener() {
        return new ApplicationListener<TcpConnectionOpenEvent>() {
            public void onApplicationEvent(TcpConnectionOpenEvent event) {
                outputChannel().send(MessageBuilder.withPayload("foo")
                        .setHeader(IpHeaders.CONNECTION_ID, event.getConnectionId())
                        .build());
            }
        };
    }
}