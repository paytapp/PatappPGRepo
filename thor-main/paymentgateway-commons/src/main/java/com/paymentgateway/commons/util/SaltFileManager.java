package com.paymentgateway.commons.util;

import org.springframework.stereotype.Service;

@Service("saltFileManager")
public class SaltFileManager extends PropertiesManager{

	public boolean insertSalt(String payId,String salt){
				
		return setProperty(payId,salt,Constants.SALT_FILE_PATH_NAME.getValue());		
	}	
	
	public boolean insertSaltEnc(String payId,String salt){
		
		return setProperty(payId,salt,Constants.SALT_ENC_FILE_PATH_NAME.getValue());		
	}
	
}
