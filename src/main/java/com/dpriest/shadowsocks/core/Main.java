package com.dpriest.shadowsocks.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.core.env.MapPropertySource;
import org.springframework.integration.ip.tcp.connection.AbstractServerConnectionFactory;
import org.springframework.integration.ip.util.TestingUtilities;
import org.springframework.integration.test.util.SocketUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        logger.debug("main() is executed, value {}", "heeloBean");
        logger.error("this is Error message", new Exception("Testing"));

        final Scanner scanner = new Scanner(System.in);

        System.out.println("TCP-Client-Server Sample!");

        final GenericXmlApplicationContext context = Main.setupContext();
        final SimpleGateway gateway = context.getBean(SimpleGateway.class);
        final AbstractServerConnectionFactory crLfServer = context.getBean(AbstractServerConnectionFactory.class);

        System.out.println("Waiting for server to accept connections...");
        TestingUtilities.waitListening(crLfServer, 10000L);

        while (true) {
            final String input = scanner.nextLine();

            if ("q".equals(input.trim())) {
                break;
            } else {
                final String result = gateway.send(input);
                System.out.println(result);
            }
        }

        System.out.println("Exiting...");
        System.exit(0);
    }

    private static GenericXmlApplicationContext setupContext() {
        final GenericXmlApplicationContext context = new GenericXmlApplicationContext();

        System.out.print("Detect open server socket...");
        int availableServerSocket = SocketUtils.findAvailableServerSocket(5678);

        final Map<String, Object> sockets = new HashMap<String, Object>();
        sockets.put("availableServerSocket", availableServerSocket);

        final MapPropertySource propertySource = new MapPropertySource("sockets", sockets);

        context.getEnvironment().getPropertySources().addLast(propertySource);;

        System.out.println("using port" + context.getEnvironment().getProperty("availableServerSocket"));

        context.load("classpath:SpringBeans.xml");
        context.registerShutdownHook();
        context.refresh();

        return context;
    }
}
