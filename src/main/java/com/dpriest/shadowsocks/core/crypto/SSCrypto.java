package com.dpriest.shadowsocks.core.crypto;

public interface SSCrypto {
    byte[] encrypt(byte[] data, int length) throws CryptoException;
    byte[] decrypt(byte[] data, int length) throws CryptoException;
    int getIVLength();
    int getKeyLength();
    byte[] getIV(boolean encrypt);
}
