package com.paymentgateway.pg.core.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.UnsupportedEncodingException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Base64;
import java.util.Enumeration;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.primitives.Bytes;
import com.paymentgateway.commons.util.PropertiesManager;

/**
 * @author Rajit
 *
 */
@Service
public class IciciUpiAutoPayUtil {

	@Autowired
	private PropertiesManager propertiesManager; 
	private static Logger logger = LoggerFactory.getLogger(IciciUpiAutoPayUtil.class.getName());
	
	
	   @SuppressWarnings("static-access")
	public String upiAutoPayEncryption(String request){
	    	
	    	try {
				JSONObject jsonPayload = new JSONObject();
	    		String sessionKey = propertiesManager.propertiesMap.get("ICICI_ENCRYPTION_RANDOM_KEY");
	    		
				//Creating Base64 EncryptedKey using session key
				String encryptedKey = encrypt(sessionKey);
				
				IvParameterSpec ivParameterSpec = getIV();
				
				SecretKeySpec secretKeySpec = new SecretKeySpec(sessionKey.getBytes(), "AES");

	            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
	            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
	 
	            // Encrypt input text
	            byte[] encrypted = cipher.doFinal(request.getBytes());
	            byte[] totalData = Bytes.concat(ivParameterSpec.getIV(),encrypted);
	            String encryptedData = Base64.getEncoder().encodeToString(totalData);
	            
	            jsonPayload.put("encryptedKey", encryptedKey);
	            jsonPayload.put("encryptedData", encryptedData);
	            jsonPayload.put("iv","");
	            jsonPayload.put("requestId","");
	            jsonPayload.put("service","PaymentApi");
	            jsonPayload.put("oaepHashingAlgorithm","NONE");
	            jsonPayload.put("clientInfo","");
	            jsonPayload.put("optionalParam","");
	            
				return jsonPayload.toString();
				
			} catch (Exception e) {
				logger.error("exception in AES Encryption " , e);
				return null;
			}
	    }
	   
	   @SuppressWarnings("static-access")
	public String encrypt(String request){
	    	
		   try {	    		
				String fileName = System.getenv("DTECH_PROPS")+""+propertiesManager.propertiesMap.get("UPI_AUTOPAY_PUBLIC_KEY");
				File file = new File(fileName);
				PublicKey publicKey = getPublicKey(file);
				Cipher cipher = Cipher.getInstance(propertiesManager.propertiesMap.get("Encrypt_Decrypt_Algo"));
				cipher.init(Cipher.ENCRYPT_MODE, publicKey);
				return Base64.getEncoder().encodeToString(cipher.doFinal(request.getBytes()));
			} catch (Exception e) {
				logger.error("Exception in upi autoPay encrypt function " , e);
				return null;
			}
	    }

	   
	   private IvParameterSpec getIV() throws UnsupportedEncodingException{
	    	
	    	byte[] iv = new byte[16];
	        new SecureRandom().nextBytes(iv);
	        return new IvParameterSpec(iv);
	    }
	   
	    public String upiAutoPayDecryption(String response){
	    	logger.info("Starting Decryption for Upi AutoPay Response ");
	    	try {
	    		//put response in json Object
				JSONObject json=new JSONObject(response);
				
				//get the sessionKey from Json
				String sessionKey = decrypt(json.get("encryptedKey").toString());
				logger.info("Session key of ICICI "+sessionKey); 
				
				//Get the encrypted Data from JSON and decode in base64
				byte[] encryptedData = Base64.getDecoder().decode(json.get("encryptedData").toString());
				
				//Get Iv and data from encryptedData
				byte[] getIv = Arrays.copyOfRange(encryptedData, 0, 16);
				byte[] getData = Arrays.copyOfRange(encryptedData, 16, encryptedData.length);
				
				IvParameterSpec iv = new IvParameterSpec(getIv);
				SecretKeySpec skeySpec = new SecretKeySpec(sessionKey.getBytes(), "AES");
	 
				//Started Decryption
				Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
				cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
				byte[] original = cipher.doFinal(getData);
				return new String(original);
				
			} catch (Exception e) {
				logger.error("exception in AES decryption in Upi AutoPay Mandate " , e);
				return response;
			}
	    }
	    
	    //Get ICICI Public Key
	    private static PublicKey getPublicKey(File filename){
	    	 try {
				FileInputStream inputStream = new FileInputStream(filename);
				 CertificateFactory cert = CertificateFactory.getInstance("X.509");
				 X509Certificate cer = (X509Certificate) cert.generateCertificate(inputStream);
				 inputStream.close();
				 return cer.getPublicKey();
				 
			} catch (Exception e) {
				logger.error("Exception : " , e);
				return null;
			} 	
	    }
	    
	    //Decrypt using Payment Gateway private key
	    @SuppressWarnings("static-access")
		public String decrypt(String response) {
	    	try {
				String fileName = System.getenv("DTECH_PROPS")+""+propertiesManager.propertiesMap.get("PaymentGatewayPrivateKeyName");
				File file = new File(fileName);
				PrivateKey privateKey = getPrivateKey(file);
				byte[] bytes = Base64.getDecoder().decode(response.replace("\r\n",""));
				Cipher decriptCipher = Cipher.getInstance(propertiesManager.propertiesMap.get("Encrypt_Decrypt_Algo"));
				decriptCipher.init(Cipher.DECRYPT_MODE, privateKey);
				return new String(decriptCipher.doFinal(bytes));
			} catch (Exception e) {
				logger.error("Exception : " , e);
				return null;
			}
	    }
	    
	    //Get Payment Gateway Private key from PFX file
	    @SuppressWarnings({ "rawtypes", "static-access" })
		private PrivateKey getPrivateKey(File filename){
	    	
	    	try {
	    		String alias = "";
	    		String pass = propertiesManager.propertiesMap.get("PaymentGatewayPfxFilePassword");
	    		char[] bytePass = pass.toCharArray();
	    		
	    		KeyStore ks = KeyStore.getInstance("pkcs12", "SunJSSE");
	    		ks.load(new FileInputStream(filename),bytePass);
	    		
	    		// iterate over all aliases
	            Enumeration es = ks.aliases();
	            
	            boolean isAliasWithPrivateKey = false;
	            while (es.hasMoreElements())
	            {
	                alias = (String) es.nextElement();
	                // if alias refers to a private key break at that point as we want to use that certificate
	                if (isAliasWithPrivateKey = ks.isKeyEntry(alias))
	                {
	                    break;
	                }
	            }
	            if (!isAliasWithPrivateKey)
	            {
	            	logger.info("No Private key found in file");
	                return null;
	            }
	            KeyStore.PrivateKeyEntry pkEntry = (KeyStore.PrivateKeyEntry) ks.getEntry(alias, new KeyStore.PasswordProtection(bytePass));
	 
	            return pkEntry.getPrivateKey();
	         
			} catch (Exception e) {
				logger.error("Exception : " , e);
				return null;
			}
			
	   }
}
