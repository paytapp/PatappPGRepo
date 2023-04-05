package com.paymentgateway.crypto.key;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.api.Hasher;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.PropertiesManager;

@Service
public class DefaultKeyProvider implements KeyProvider {

	private static Logger logger = LoggerFactory.getLogger(DefaultKeyProvider.class.getName());
	private static Map<String,String> keyMap = new HashMap<String,String>();
	private static Map<String,String> hostedKeyMap = new HashMap<String,String>();

	@Autowired
	private PropertiesManager propertiesManager;
	
	@Override
	public String generateKey(String payId) throws SystemException {
		
		if (StringUtils.isNotBlank(keyMap.get(payId))) {
			return keyMap.get(payId);
		}
		
		String salt = propertiesManager.getEncSalt(payId);
		logger.info("Creating new key for PAY_ID: " + payId);
		if(salt==null) {
			throw new SystemException(ErrorType.USER_NOT_FOUND,"No such user");
		}
		String generatedKey = (Hasher.getHash(salt)).substring(0,16);
		keyMap.put(payId, generatedKey);
		return generatedKey;
	}
	
	@Override
	public String generateHostedKey(String payId) throws SystemException {
		
		if (StringUtils.isNotBlank(hostedKeyMap.get(payId))) {
			return hostedKeyMap.get(payId);
		}
		
		String salt = propertiesManager.getEncSalt(payId);
		logger.info("Creating new key for PAY_ID: " + payId);
		if(salt==null) {
			throw new SystemException(ErrorType.USER_NOT_FOUND,"No such user");
		}
		String generatedKey = (Hasher.getHash(salt)).substring(0,32);
		hostedKeyMap.put(payId, generatedKey);
		return generatedKey;
	}
}
