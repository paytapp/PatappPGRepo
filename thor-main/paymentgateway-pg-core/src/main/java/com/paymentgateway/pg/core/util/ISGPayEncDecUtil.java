package com.paymentgateway.pg.core.util;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import org.apache.commons.codec.binary.Base64;

public class ISGPayEncDecUtil

{
    private static final byte[] SALT;
    private Cipher eCipher;
    private Cipher dCipher;
    private byte[] encrypt;
    private byte[] iv;
    
    static {
        SALT = new byte[] { -57, -75, -103, -12, 75, 124, -127, 119 };
    }
    
    public ISGPayEncDecUtil(String passPhrase,SecretKey secretKey) {
        try {
            (this.eCipher = Cipher.getInstance("AES/CBC/PKCS5Padding")).init(1, secretKey);
            this.dCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            this.iv = this.eCipher.getParameters().getParameterSpec(IvParameterSpec.class).getIV();
            this.dCipher.init(2, secretKey, new IvParameterSpec(this.iv));
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public ISGPayEncDecUtil( String passPhrase,  String encryptedString,SecretKey secretKey) {
        try {

            this.encrypt = Base64.decodeBase64(encryptedString);
            (this.eCipher = Cipher.getInstance("AES/CBC/PKCS5Padding")).init(1, secretKey);
            final byte[] iv = this.extractIV();
            (this.dCipher = Cipher.getInstance("AES/CBC/PKCS5Padding")).init(2, secretKey, new IvParameterSpec(iv));
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public String encrypt(final String encrypt) {
        String encStr = null;
        try {
            final byte[] bytes = encrypt.getBytes("UTF8");
            final byte[] encrypted = this.encrypt(bytes);
            final byte[] cipherText = new byte[encrypted.length + this.iv.length];
            System.arraycopy(this.iv, 0, cipherText, 0, this.iv.length);
            System.arraycopy(encrypted, 0, cipherText, this.iv.length, encrypted.length);
            encStr = new String(Base64.encodeBase64(cipherText));
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return encStr;
    }
    
    public byte[] encrypt(final byte[] plain) throws Exception {
        return this.eCipher.doFinal(plain);
    }
    
    private byte[] extractIV() {
        final byte[] iv = new byte[16];
        System.arraycopy(this.encrypt, 0, iv, 0, iv.length);
        return iv;
    }
    
    public String decrypt() {
        String decStr = null;
        try {
            final byte[] bytes = this.extractCipherText();
            final byte[] decrypted = this.decrypt(bytes);
            decStr = new String(decrypted, "UTF8");
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        return decStr;
    }
    
    private byte[] extractCipherText() {
        final byte[] ciphertext = new byte[this.encrypt.length - 16];
        System.arraycopy(this.encrypt, 16, ciphertext, 0, ciphertext.length);
        return ciphertext;
    }
    
    public byte[] decrypt(final byte[] encrypt) throws Exception {
        return this.dCipher.doFinal(encrypt);
    }
    

}
