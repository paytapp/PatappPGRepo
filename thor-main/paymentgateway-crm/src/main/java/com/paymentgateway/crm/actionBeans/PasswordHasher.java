package com.paymentgateway.crm.actionBeans;

import com.paymentgateway.commons.api.Hasher;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.PropertiesManager;

/**
 * @author Puneet
 *
 */
public class PasswordHasher {
	
	public static String hashPassword(String password,String payId) throws SystemException{
	
		String salt = (new PropertiesManager()).getSalt(payId);	
		if(null==salt){
			throw new SystemException(ErrorType.AUTHENTICATION_UNAVAILABLE, ErrorType.AUTHENTICATION_UNAVAILABLE.getResponseCode());
		}
		
		String hashedPassword = Hasher.getHash(password.concat(salt));		
		return hashedPassword;		
	}	
	
	public static String hashPin(String pin,String payId) throws SystemException{
		
		String salt = (new PropertiesManager()).getSalt(payId);	
		if(null==salt){
			throw new SystemException(ErrorType.AUTHENTICATION_UNAVAILABLE, ErrorType.AUTHENTICATION_UNAVAILABLE.getResponseCode());
		}
		
		String hashedPin = Hasher.getHash(pin.concat(salt));		
		return hashedPin;		
	}	
}

	
