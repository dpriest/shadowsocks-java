package com.dpriest.shadowsocks.core.crypto;

public class CryptoException extends Exception {

    public CryptoException(String message) {
        super(message);
    }

    public CryptoException(Exception e) {
        super(e);
    }
}
