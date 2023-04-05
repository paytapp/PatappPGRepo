package com.paymentgateway.commons.kms;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.api.TransactionControllerServiceProvider;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.PropertiesManager;

@Service
public class EncryptDecryptService {

	private static Logger logger = LoggerFactory.getLogger(EncryptDecryptService.class.getName());

	@Autowired
	@Qualifier("transactionControllerServiceProvider")
	private TransactionControllerServiceProvider transactionControllerServiceProvider;

	@Autowired
	private PropertiesManager propertiesManager;
	
	public String decrypt(String payId, String data) {
		try {
			//logger.info("Inside EncryptDecryptService , decrypt ");
			String decryptedDataFromMap = propertiesManager.propertiesMap.get(data);

			if (StringUtils.isBlank(decryptedDataFromMap)) {
				Map<String, String> decryptedData = transactionControllerServiceProvider.decrypt(payId, data);
				PropertiesManager.propertiesMap.putAll(decryptedData);
				return decryptedData.get(FieldType.ENCDATA.getName());
			}
			return decryptedDataFromMap;
		} catch (Exception e) {
			logger.error("Exception occured in AWSEncryptDecryptService , decrypt( String data) , exception =   " , e);
			return null;
		}
	}

	public String encrypt(String payId, String data) {
		try {
			logger.info("Inside EncryptDecryptService , encrypt ");
			Map<String, String> encryptedData = transactionControllerServiceProvider.encrypt(payId, data);
			return encryptedData.get(FieldType.ENCDATA.getName());
		} catch (Exception e) {
			logger.error("Exception occured in AWSEncryptDecryptService , encrypt( String data) , exception =   " , e);
			return null;
		}
	}
}