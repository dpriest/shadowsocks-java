package com.dpriest.shadowsocks.core.crypto;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class Utils {

    private static byte[][] EVP_BytesToKey(int key_len, int iv_len, MessageDigest md, byte[] salt, byte[] data, int count) {
        byte[][] both = new byte[2][];
        byte[] key = new byte[key_len];
        int key_ix = 0;
        byte[] iv = new byte[iv_len];
        int iv_ix = 0;
        both[0] = key;
        both[1] = iv;
        byte[] md_buf = null;
        int nKey = key_len;
        int niv = iv_len;
        int i;
        if (data == null) {
            return both;
        }
        int addmd = 0;
        for (;;) {
            md.reset();
            if (addmd++ > 0) {
                md.update(md_buf);
            }
            md.update(data);
            if (null != salt) {
                md.update(salt, 0, 8);
            }
            md_buf = md.digest();
            for (i = 1; i < count; i++) {
                md.reset();
                md.update(md_buf);
                md_buf = md.digest();
            }
            i = 0;
            if (nKey > 0) {
                for (;;) {
                    if (nKey == 0)
                        break;
                    if (i == md_buf.length)
                        break;
                    key[key_ix++] = md_buf[i];
                    nKey--;
                    i++;
                }
            }
            if (niv > 0 && i != md_buf.length) {
                for (;;) {
                    if (niv == 0)
                        break;
                    if (i == md_buf.length)
                        break;
                    iv[iv_ix++] = md_buf[i];
                    niv--;
                    i++;
                }
            }
            if (nKey == 0 && niv == 0) {
                break;
            }
        }
        for (i = 0; i < md_buf.length; i++) {
            md_buf[i] = 0;
        }
        return both;
    }

    static byte[] getKey(String password, int keyLen, int ivLen) throws CryptoException {
        MessageDigest md;
        byte[] passwordBytes;
        byte[][] keyAdnIV;

        try {
            md = MessageDigest.getInstance("MD5");
            passwordBytes = password.getBytes("ASCII");
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            throw new CryptoException(e);
        }

        keyAdnIV = EVP_BytesToKey(keyLen, ivLen, md, null, passwordBytes, 1);

        return keyAdnIV[0];
    }

    public static byte[] randomBytes(int size) {
        byte[] bytes = new byte[size];
        new SecureRandom().nextBytes(bytes);
        return bytes;
    }
}
