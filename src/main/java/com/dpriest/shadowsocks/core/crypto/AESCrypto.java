package com.dpriest.shadowsocks.core.crypto;

import org.bouncycastle.crypto.StreamBlockCipher;
import org.bouncycastle.crypto.StreamCipher;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.modes.CFBBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;

import java.io.ByteArrayOutputStream;

public class AESCrypto extends BaseCrytpo {

    private final static String CIPHER_AES_256_CFB = "aes-256-cfb";
    private final static String CIPHER_AES_256_OFB =  "aes-256-ofb";

    private final static int IV_LENGTH = 16;

    AESCrypto(String name, String password) throws CryptoException {
        super(name, password);
    }

    @Override
    public int getIVLength() {
        return IV_LENGTH;
    }

    @Override
    public int getKeyLength() {
        if (mName.equals(CIPHER_AES_256_CFB) || mName.equals(CIPHER_AES_256_OFB)) {
            return 32;
        }
        return 0;
    }

    @Override
    protected StreamCipher createCipher(byte[] iv, boolean encrypt) throws CryptoException {
        StreamBlockCipher c = getCipher(encrypt);
        ParametersWithIV parametersWithIV = new ParametersWithIV(new KeyParameter(mKey), iv, 0, mIVLength);
        c.init(encrypt, parametersWithIV);
        return c;
    }

    private StreamBlockCipher getCipher(boolean isEncrypted) throws CryptoException {
        AESEngine engine = new AESEngine();
        StreamBlockCipher cipher;

        if (mName.equals(CIPHER_AES_256_CFB)) {
            cipher = new CFBBlockCipher(engine, getIVLength() * 8);
        } else {
            throw new CryptoException("Invalid AlgorithmParameter: " + mName);
        }

        return cipher;
    }

    @Override
    protected void process(byte[] in, ByteArrayOutputStream out, boolean encrypt) {
        int size;
        byte[] buffer = new byte[in.length];
        StreamBlockCipher cipher;
        if (encrypt) {
            cipher = (StreamBlockCipher)mEncryptCipher;
        } else {
            cipher = (StreamBlockCipher)mDecryptCipher;
        }
        size = cipher.processBytes(in, 0, in.length, buffer, 0);
        out.write(buffer, 0, size);
    }
}
