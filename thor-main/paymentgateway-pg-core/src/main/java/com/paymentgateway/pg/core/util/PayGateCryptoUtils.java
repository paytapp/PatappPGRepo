package com.paymentgateway.pg.core.util;

import javax.crypto.SecretKey;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.io.UnsupportedEncodingException;
import javax.crypto.spec.IvParameterSpec;
import java.security.spec.AlgorithmParameterSpec;
import org.apache.commons.codec.binary.Base64;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import java.lang.reflect.Field;

@Service
public class PayGateCryptoUtils
{
    private static final String ENCRYPTION_IV = "0123456789abcdef";
    private static final String PADDING = "AES/CBC/PKCS5Padding";
    private static final String ALGORITHM = "AES";
    private static final String CHARTSET = "UTF-8";
    
    static {
        try {
            final Field field = Class.forName("javax.crypto.JceSecurity").getDeclaredField("isRestricted");
            field.setAccessible(true);
            //field.set(null, Boolean.FALSE);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public String encrypt(final String textToEncrypt, final String key) {
        try {
            final Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(1, makeKey(key), makeIv());
            return new String(Base64.encodeBase64(cipher.doFinal(textToEncrypt.getBytes())));
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public String decrypt(final String textToDecrypt, final String key) {
        try {
            final Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(2, makeKey(key), makeIv());
            return new String(cipher.doFinal(Base64.decodeBase64(textToDecrypt)));
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    private AlgorithmParameterSpec makeIv() {
        try {
            return new IvParameterSpec("0123456789abcdef".getBytes("UTF-8"));
        }
        catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    private Key makeKey(final String encryptionKey) {
        try {
            final byte[] key = Base64.decodeBase64(encryptionKey);
            return new SecretKeySpec(key, "AES");
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public String generateMerchantKey() {
        String newKey = null;
        try {
            final KeyGenerator kgen = KeyGenerator.getInstance("AES");
            kgen.init(256);
            final SecretKey skey = kgen.generateKey();
            final byte[] raw = skey.getEncoded();
            newKey = new String(Base64.encodeBase64(raw));
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return newKey;
    }
}