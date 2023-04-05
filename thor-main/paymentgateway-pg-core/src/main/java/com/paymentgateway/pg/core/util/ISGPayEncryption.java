package com.paymentgateway.pg.core.util;

import java.security.Key;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class ISGPayEncryption
{
    private static String[] parameters;
    
    static {
        ISGPayEncryption.parameters = new String[] { "MerchantId", "TerminalId", "BankId", "Version", "ReturnURL", "TxnType", "PassCode", "MCC", "Currency", "Amount", "TxnRefNo" };
    }
    
    public Map<String,String> encrypt(final LinkedHashMap<String, String> hmReqFields, final String sMerchantID, final String sTerminalID, final String sBankID, final String sVersionNum, final String sEncryptionKey, final String sSecureSecret) throws Exception {
        try {
            if (StringUtils.isBlank(sMerchantID)) {
                throw new Exception("Merchant ID Can Not Be Empty or Null!!!");
            }
            if (StringUtils.isBlank(sTerminalID)) {
                throw new Exception("Terminal ID Can Not Be Empty or Null!!!");
            }
            if (StringUtils.isBlank(sBankID)) {
                throw new Exception("Bank ID Can Not Be Empty or Null!!!");
            }
            if (StringUtils.isBlank(sVersionNum)) {
                throw new Exception("Version Number Can Not Be Empty or Null!!!");
            }
            for (int i = 0; i < ISGPayEncryption.parameters.length; ++i) {
                if (!ISGPayEncryption.parameters[i].equals("MerchantId") && !ISGPayEncryption.parameters[i].equals("TerminalId") && !ISGPayEncryption.parameters[i].equals("BankId")) {
                    if (!ISGPayEncryption.parameters[i].equals("Version")) {
                        if (hmReqFields.get(ISGPayEncryption.parameters[i]) == null || "".equals(hmReqFields.get(ISGPayEncryption.parameters[i])) || "null".equalsIgnoreCase(hmReqFields.get(ISGPayEncryption.parameters[i]))) {
                            throw new Exception("Missing Parameters:::" + ISGPayEncryption.parameters[i]);
                        }
                    }
                }
            }
            if (sSecureSecret == null || "".equals(sSecureSecret)) {
                throw new Exception("SecureSecret can not be null or empty!!!");
            }
            if (sEncryptionKey == null || "".equals(sEncryptionKey)) {
                throw new Exception("Encryption Key can not be null or empty!!!");
            }
            hmReqFields.put("MerchantId", sMerchantID);
            hmReqFields.put("TerminalId", sTerminalID);
            hmReqFields.put("BankId", sBankID);
            hmReqFields.put("Version", sVersionNum);
            
            generateHash(hmReqFields, sSecureSecret);
            String ENC_DATA = doISGPayEncrypt(hmReqFields, sEncryptionKey);
            if ("".equals(ENC_DATA)) {
                throw new Exception("Problem during encryption....");
            }
            String MERCHANT_ID = sMerchantID;
            String BANK_ID = sBankID;
            String TERMINAL_ID = sTerminalID;
            String VERSION = sVersionNum;
            
            Map<String,String> EncDataMap = new HashMap<String,String>();
            
            EncDataMap.put("MERCHANT_ID", MERCHANT_ID);
            EncDataMap.put("BANK_ID", BANK_ID);
            EncDataMap.put("TERMINAL_ID", TERMINAL_ID);
            EncDataMap.put("VERSION", VERSION);
            EncDataMap.put("ENC_DATA", ENC_DATA);
            return EncDataMap;
        }
        catch (Exception e) {
            System.out.println("Exception Occured in ISGPayEncryption.encrypt(String):" + e);
            throw e;
        }
    }
    
    
    private String doISGPayEncrypt(final LinkedHashMap<String, String> hmReqFields, final String sEncryptionKey) throws Exception {
        try {
            final StringBuffer sb = new StringBuffer();
            for (final String fieldName : hmReqFields.keySet()) {
                sb.append(fieldName);
                sb.append("||");
                sb.append(hmReqFields.get(fieldName));
                sb.append("::");
            }
            final byte[] keyByte = sEncryptionKey.getBytes();
            final Key key = new SecretKeySpec(keyByte, "AES");
            final Cipher c = Cipher.getInstance("AES");
            c.init(1, key);
            final byte[] encVal = c.doFinal(sb.toString().getBytes());
            final byte[] encryptedByteValue = new Base64().encode(encVal);
            return new String(encryptedByteValue);
        }
        catch (Exception e) {
            System.out.println("Exception Occured in ISGPayHashGeneration.generateHash():" + e);
            throw e;
        }
    }
    
    public String generateHash(final LinkedHashMap<String, String> hmReqFields, final String sSecureSecret) throws Exception {
        final StringBuffer hexString = new StringBuffer();
        try {
            hmReqFields.remove("inprocess");
            final ArrayList<String> fieldNames = new ArrayList<String>(hmReqFields.keySet());
            Collections.sort(fieldNames);
            final StringBuffer buf = new StringBuffer();
            buf.append(sSecureSecret);
            final Iterator<String> itr = fieldNames.iterator();
            String hashKeys = "";
            while (itr.hasNext()) {
                final String fieldName = itr.next();
                final String fieldValue = hmReqFields.get(fieldName);
                hashKeys = String.valueOf(hashKeys) + fieldName + ", ";
                if (fieldValue != null && fieldValue.length() > 0) {
                    buf.append(fieldValue);
                }
            }
            final MessageDigest digest = MessageDigest.getInstance("SHA-256");
            final byte[] hash = digest.digest(buf.toString().getBytes("UTF-8"));
            for (int i = 0; i < hash.length; ++i) {
                final String hex = Integer.toHexString(0xFF & hash[i]);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            hmReqFields.put("SecureHash", hexString.toString());
        }
        catch (Exception e) {
            System.out.println("Exception Occured in ISGPayHashGeneration.generateHash():" + e);
            throw e;
        }
        return hexString.toString();
    }
}
