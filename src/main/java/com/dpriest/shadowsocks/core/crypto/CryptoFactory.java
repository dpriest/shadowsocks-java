package com.dpriest.shadowsocks.core.crypto;

public class CryptoFactory {

    private static final String AES = "aes";

    public static SSCrypto create(String name, String password) throws CryptoException {
        String cipherName = name.toLowerCase();
        if (cipherName.startsWith(AES)) {
            return new AESCrypto(name, password);
        } else {
            throw new CryptoException("Unsupport method: " + name);
        }
    }
}
