package com.paymentgateway.commons.util;

import java.util.UUID;

import com.paymentgateway.commons.user.User;


/**
 * @author Puneet
 *
 */

public class SaltFactory {

	public static final String dash = "-";
	
	//Generate new random salt
	public static String generateRandomSalt(){
		UUID uuId = UUID.randomUUID();
		String salt = uuId.toString().replaceAll(dash,Constants.BLANK_REPLACEMENT_STRING.getValue()).substring(0, Integer.parseInt(Constants.SALT_LENGTH.getValue()));
		return salt;
	}
	
	//get salt
	public static String getSaltProperty(User user){
		String salt = (new PropertiesManager()).getSalt(user.getPayId());
		return salt;
	}	
}
