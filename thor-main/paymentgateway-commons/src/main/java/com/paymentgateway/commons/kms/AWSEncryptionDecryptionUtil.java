/*
 * package com.paymentgateway.commons.kms;
 * 
 * import java.util.Collections; import java.util.Map;
 * 
 * import org.slf4j.Logger; import org.slf4j.LoggerFactory; import
 * org.springframework.stereotype.Service;
 * 
 * import com.amazonaws.encryptionsdk.AwsCrypto; import
 * com.amazonaws.encryptionsdk.CryptoResult; import
 * com.amazonaws.encryptionsdk.kms.KmsMasterKey; import
 * com.amazonaws.encryptionsdk.kms.KmsMasterKeyProvider; import
 * com.paymentgateway.commons.util.PropertiesManager;
 * 
 * @Service public class AWSEncryptionDecryptionUtil {
 * 
 * private final PropertiesManager propertiesManager = new PropertiesManager();
 * private final String keyArn = propertiesManager.propertiesMap.get("AWSARN");
 * private final AwsCrypto crypto = new AwsCrypto(); private final
 * KmsMasterKeyProvider prov = new KmsMasterKeyProvider(keyArn);
 * 
 * private final String AWSContextName =
 * propertiesManager.propertiesMap.get("AWSContextName"); private final String
 * AWSContextValue = propertiesManager.propertiesMap.get("AWSContextValue");
 * private final Map<String, String> context =
 * Collections.singletonMap(AWSContextName, AWSContextValue);
 * 
 * private static Logger logger =
 * LoggerFactory.getLogger(AWSEncryptionDecryptionUtil.class.getName());
 * 
 * public String encrypt(String data) {
 * 
 * try {
 * 
 * logger.info("Inside AWSEncryptionDecryptionUtil encrypt"); final String
 * ciphertext = crypto.encryptString(prov, data, context).getResult(); return
 * ciphertext;
 * 
 * }
 * 
 * catch(Exception e) { logger.
 * error("Exception in AWSEncryptionDecryptionUtil encrypt , exception = "+e);
 * return null; }
 * 
 * }
 * 
 * public String decrypt(String data) {
 * 
 * try {
 * 
 * final CryptoResult<String, KmsMasterKey> decryptResult =
 * crypto.decryptString(prov, data);
 * 
 * if (!decryptResult.getMasterKeyIds().get(0).equals(keyArn)) { throw new
 * IllegalStateException("Wrong key ID!"); }
 * 
 * for (final Map.Entry<String, String> e : context.entrySet()) { if
 * (!e.getValue().equals(decryptResult.getEncryptionContext().get(e.getKey())))
 * { throw new IllegalStateException("Wrong Encryption Context!"); } }
 * 
 * String decryptedString = decryptResult.getResult(); return decryptedString;
 * 
 * }
 * 
 * catch(Exception e) { logger.
 * error("Exception in AWSEncryptionDecryptionUtil decrypt , exception = "+e);
 * return null; } } }
 */