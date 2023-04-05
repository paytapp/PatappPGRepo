/*package com.paymentgateway.commons.kms;

import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.util.PropertiesManager;

@Service
public class AzureEncryptDecryptService {

	private static Logger logger = LoggerFactory.getLogger(AzureEncryptDecryptService.class.getName());
	
	@Autowired
	AzureEncryptionDecryptionUtil azureEncryptionDecryptionUtil;
	
	public  String decrypt( String data) {
		
		try {
			String decryptedDataFromMap = PropertiesManager.propertiesMap.get(data);
			
			
			if (decryptedDataFromMap == null) {
				String decryptedData = azureEncryptionDecryptionUtil.decrypt(data);
				
				PropertiesManager.propertiesMap.put(data,decryptedData);
				return decryptedData;
			}
			
			return decryptedDataFromMap;
		}
		
		catch(Exception e) {
			
			logger.error("Exception occured in AzureEncryptDecryptService , decrypt( String data) , exception =   "+e);
			return null;
		}
		
		
	}

	public String encrypt(String data) {
		
		try {
			
			String encryptedData = azureEncryptionDecryptionUtil.encrypt(data);
			logger.info("Inside AzureEncryptDecryptService , encrypt = " + encryptedData);
			return encryptedData;
			
		}
		
		catch(Exception e) {
			logger.error("Exception occured in AzureEncryptDecryptService , encrypt( String data) , exception =   "+e);
			return null;
		}
		
		

	}
}
*/