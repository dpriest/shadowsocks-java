package com.dpriest.shadowsocks.core.crypto;

import org.bouncycastle.crypto.StreamCipher;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public abstract class BaseCrytpo implements SSCrypto {

    protected abstract StreamCipher createCipher(byte[] iv, boolean encrypt) throws CryptoException;
    protected abstract void process(byte[] in, ByteArrayOutputStream out, boolean encrypt);

    final String mName;
    final byte[] mKey;
    protected final int mIVLength;
    private final int mKeyLength;

    protected StreamCipher mEncryptCipher = null;
    protected StreamCipher mDecryptCipher = null;

    protected byte[] mEncryptIV;
    protected byte[] mDecryptIV;

    private ByteArrayOutputStream mData;

    private final byte[] mLock = new byte[0];

    BaseCrytpo(String name, String password) throws CryptoException {
        mName = name.toLowerCase();
        mIVLength = getIVLength();
        mKeyLength = getKeyLength();
        if (mKeyLength == 0) {
            throw new CryptoException("Unsupport method: " + mName);
        }
        mKey = Utils.getKey(password, mKeyLength, mIVLength);
        mData = new ByteArrayOutputStream();
    }

    public byte[] getKey() {
        return mKey;
    }

    public byte[] getIV(boolean encrypt) {
        if (encrypt) {
            if (mEncryptIV == null) {
                mEncryptIV = Utils.randomBytes(mIVLength);
            }
            return mEncryptIV;
        } else {
            return mDecryptIV;
        }
    }

    @Override
    public byte[] encrypt(byte[] in, int length) throws CryptoException {
        synchronized (mLock) {
            if (length != in.length) {
                byte[] data = new byte[length];
                System.arraycopy(in, 0, data, 0, length);
                return encryptLocked(data);
            } else {
                return encryptLocked(in);
            }
        }
    }

    private byte[] encryptLocked(byte[] in) throws CryptoException {
        mData.reset();
        if (mEncryptCipher == null) {
            mEncryptIV = getIV(true);
            mEncryptCipher = createCipher(mEncryptIV, true);
            try {
                mData.write(mEncryptIV);
            } catch (IOException e) {
                throw new CryptoException(e);
            }
        }
        process(in, mData, true);
        return mData.toByteArray();
    }

    public byte[] decrypt(byte[] in, int length) throws CryptoException {
        synchronized (mLock) {
            if (length != in.length) {
                byte[] data = new byte[length];
                System.arraycopy(in, 0, data, 0, length);
                return decryptLocked(data);
            } else {
                return decryptLocked(in);
            }
        }
    }

    private byte[] decryptLocked(byte[] in) throws CryptoException {
        byte[] data;
        mData.reset();
        if (mDecryptCipher == null) {
            mDecryptCipher = createCipher(in, false);
            mDecryptIV = new byte[mIVLength];
            data = new byte[in.length - mIVLength];
            System.arraycopy(in, 0, mDecryptIV, 0, mIVLength);
            System.arraycopy(in, mIVLength, data, 0, in.length - mIVLength);
        } else {
            data = in;
        }
        process(data, mData, false);
        return mData.toByteArray();
    }
}
