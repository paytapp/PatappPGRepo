package com.paymentgateway.pg.core.paytm.util;

import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AesEncryption implements Encryption {
   private final byte[] ivParamBytes = new byte[]{64, 64, 64, 64, 38, 38, 38, 38, 35, 35, 35, 35, 36, 36, 36, 36};

   public String encrypt(String toEncrypt, String key) throws Exception {
      String encryptedValue = "";
      Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING", "SunJCE");
      cipher.init(1, new SecretKeySpec(key.getBytes(), "AES"), new IvParameterSpec(this.ivParamBytes));
      encryptedValue = Base64.getEncoder().encodeToString(cipher.doFinal(toEncrypt.getBytes()));
      return encryptedValue;
   }

   public String decrypt(String toDecrypt, String key) throws Exception {
      String decryptedValue = "";
      Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING", "SunJCE");
      cipher.init(2, new SecretKeySpec(key.getBytes(), "AES"), new IvParameterSpec(this.ivParamBytes));
      decryptedValue = new String(cipher.doFinal(Base64.getDecoder().decode(toDecrypt)));
      return decryptedValue;
   }
}
