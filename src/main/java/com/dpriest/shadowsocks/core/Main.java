package com.dpriest.shadowsocks.core;


import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;

public class Main {

    private static String serverHost = "127.0.0.1";
    private static int port = 8000;
    private static String password = "123456";

    public static void main(String[] args) {
        setOps(args);
        new ShadowSocksServer().start(serverHost, port, "aes-256-cfb", password);
    }

    private static void setOps(String[] argv) {
        LongOpt[] longOpts = new LongOpt[3];
        longOpts[0] = new LongOpt("serverHost", LongOpt.NO_ARGUMENT, null, 'h');
        longOpts[1] = new LongOpt("port", LongOpt.REQUIRED_ARGUMENT, null, 'p');
        longOpts[2] = new LongOpt("password", LongOpt.REQUIRED_ARGUMENT, null, 'k');

        Getopt g = new Getopt("shadowsocks", argv, "h:p:k", longOpts);

        int c;
        while ((c = g.getopt()) != -1) {
            switch (c) {
                case 'h':
                    serverHost = g.getOptarg();
                    break;
                case 'p':
                    port = Integer.parseInt(g.getOptarg());
                    break;
                case 'k':
                    password = g.getOptarg();
                    break;
                default:
                    throw new IllegalArgumentException("unknown option: " + c);
            }
        }
    }

}
