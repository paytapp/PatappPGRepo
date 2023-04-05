package com.paymentgateway.pg.core.util;

import javax.crypto.Cipher;

public class MAC
{
  public MAC() {}
  
  public static char toHexChar(int i) {
    if ((i >= 0) && (i <= 9)) {
      return (char)(48 + i);
    }
    return (char)(97 + (i - 10));
  }
  
  public String asciiChar2binary(String asciiString)
  {
    if (asciiString == null) {
      return null;
    }
    StringBuilder binaryString = new StringBuilder();
    int intValue = 0;
    for (int i = 0; i < asciiString.length(); i++) {
      intValue = asciiString.charAt(i);
      String temp = "00000000" + Integer.toBinaryString(intValue);
      binaryString.append(temp.substring(temp.length() - 8));
    }
    return binaryString.toString();
  }
  
  public String binary2hex(String binaryString) {
    if (binaryString == null) {
      return null;
    }
    StringBuilder hexString = new StringBuilder();
    for (int i = 0; i < binaryString.length(); i += 8) {
      String temp = binaryString.substring(i, i + 8);
      int intValue = 0;
      int k = 0; for (int j = temp.length() - 1; j >= 0; k++) {
        intValue = (int)(intValue + Integer.parseInt(temp, temp.charAt(j)) * Math.pow(2.0D, k));j--;
      }
      temp = "0" + Integer.toHexString(intValue);
      hexString.append(temp.substring(temp.length() - 2));
    }
    return hexString.toString();
  }
  
  public byte[] hexToBytes(String hexString) {
    int len = hexString.length();
    byte[] data = new byte[len / 2];
    for (int i = 0; i < len; i += 2) {
      data[(i / 2)] = ((byte)((Character.digit(hexString.charAt(i), 16) << 4) + Character.digit(hexString.charAt(i + 1), 16)));
    }
    return data;
  }
  
  public String getDexValue(String s, String s1) throws Exception {
    String algorithm = null;
    String mode = null;
    String padding = null;
    Cipher cipher = null;
    javax.crypto.spec.SecretKeySpec skeySpec = null;
    try {
      algorithm = "AES";
      mode = "ECB";
      padding = "PKCS5PADDING";
      skeySpec = new javax.crypto.spec.SecretKeySpec(s1.getBytes("UTF-8"), algorithm);
      cipher = Cipher.getInstance(algorithm + "/" + mode + "/" + padding);
      cipher.init(2, skeySpec);
      byte[] ciphertext = cipher.doFinal(hexToBytes(s));
      return toString(ciphertext);
    } catch (Exception exception) {
      throw exception;
    } finally {
      algorithm = null;
      mode = null;
      padding = null;
      cipher = null;
      skeySpec = null;
    }
  }
  
  public String getHexValue(String pin, String key) throws Exception {
    String algorithm = null;
    String mode = null;
    String padding = null;
    Cipher cipher = null;
    javax.crypto.spec.SecretKeySpec skeySpec = null;
    try {
      algorithm = "AES";
      mode = "ECB";
      padding = "PKCS5PADDING";
      skeySpec = new javax.crypto.spec.SecretKeySpec(key.getBytes("UTF-8"), algorithm);
      cipher = Cipher.getInstance(algorithm + "/" + mode + "/" + padding);
      cipher.init(1, skeySpec);
      byte[] encrypted = cipher.doFinal(pin.getBytes());
      return cryptix.util.core.Hex.toString(encrypted);
    } catch (Exception e) {
      throw e;
    } finally {
      algorithm = null;
      mode = null;
      padding = null;
      cipher = null;
      skeySpec = null;
    }
  }
  
  public String getTripleDesValue(String pin, String key1, String key2, String key3) throws Exception {
    try {
      String decryptedKey = getDexValue(pin, key1);
      decryptedKey = binary2hex(asciiChar2binary(decryptedKey)).toUpperCase();
      decryptedKey = getHexValue(decryptedKey, key2);
      decryptedKey = getDexValue(decryptedKey, key3);
      return binary2hex(asciiChar2binary(decryptedKey)).toUpperCase();
    }
    catch (Exception e) {
      throw e;
    }
  }
  
  public String getAESValue(String pin, String key) throws Exception {
    String decryptedKey = null;
    try {
      decryptedKey = getDexValue(pin, key);
      decryptedKey = hex2Alpha(decryptedKey);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return decryptedKey;
  }
  
  public String getTripleHexValue(String pin, String key1, String key2, String key3) throws Exception {
    try {
      String encryptedKey = getHexValue(pin, key1);
      encryptedKey = getDexValue(encryptedKey, key2);
      encryptedKey = binary2hex(asciiChar2binary(encryptedKey)).toUpperCase();
      return getHexValue(encryptedKey, key3);
    }
    catch (Exception e) {
      throw e;
    }
  }
  
  public static String hex2Alpha(String data) {
    int len = data.length();
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < len; i += 2) {
      int j = i + 2;
      sb.append((char)Integer.valueOf(data.substring(i, j), 16).intValue());
    }
    data = sb.toString();
    return data;
  }
  
  public static String alpha2Hex(String data) {
    char[] alpha = data.toCharArray();
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < alpha.length; i++) {
      int count = Integer.toHexString(alpha[i]).toUpperCase().length();
      if (count <= 1) {
        sb.append("0").append(Integer.toHexString(alpha[i]).toUpperCase());
      } else {
        sb.append(Integer.toHexString(alpha[i]).toUpperCase());
      }
    }
    return sb.toString();
  }
  
  public static String alpha2HexC(String data) {
    char[] alpha = data.toCharArray();
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < alpha.length; i++) {
      int count = Integer.toHexString(alpha[i]).toUpperCase().length();
      if (count <= 1) {
        sb.append(" ").append("0").append(Integer.toHexString(alpha[i]).toUpperCase());
      } else {
        sb.append(" ").append(Integer.toHexString(alpha[i]).toUpperCase());
      }
    }
    return sb.toString();
  }
  
  public String hexToString(String txtInHex) {
    byte[] txtInByte = new byte[txtInHex.length() / 2];
    int j = 0;
    for (int i = 0; i < txtInHex.length(); i += 2) {
      txtInByte[(j++)] = Byte.parseByte(txtInHex.substring(i, i + 2), 16);
    }
    return new String(txtInByte);
  }
  
  public static String rightPadZeros(String Str) {
    if (Str == null) {
      return null;
    }
    StringBuilder padStr = new StringBuilder(Str);
    for (int i = Str.length(); i % 8 != 0; i++) {
      padStr.append('0');
    }
    return padStr.toString();
  }
  
  public byte[] toByteArray(String key) {
    char[] ch = key.toCharArray();
    byte[] temp = new byte[ch.length];
    for (int i = 0; i < ch.length; i++) {
      temp[i] = ((byte)ch[i]);
    }
    return temp;
  }
  
  public String toString(byte[] temp) {
    char[] ch = new char[temp.length];
    for (int i = 0; i < temp.length; i++) {
      ch[i] = ((char)temp[i]);
    }
    String s = new String(ch);
    return s;
  }
}