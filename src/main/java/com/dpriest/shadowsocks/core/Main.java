package com.dpriest.shadowsocks.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        ApplicationContext context = new ClassPathXmlApplicationContext(
                "SpringBeans.xml"
        );
        HelloWorld obj = (HelloWorld) context.getBean("helloBean");
        obj.printHello();
        logger.debug("main() is executed, value {}", "heeloBean");
        logger.error("this is Error message", new Exception("Testing"));
    }
}
