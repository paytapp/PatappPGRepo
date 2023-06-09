package com.EwalletPaytapp.service;

/*
 * Author Arvind Chaturvedi
 * 12-05-2023
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class WalletUserRegistrationService {

	private static Logger logger = LoggerFactory.getLogger(WalletUserRegistrationService.class.getName());
	public String AddEwalletUser(String custWaalletJson) {
		String custId="";
		try {
			if(custWaalletJson!=null) {
				logger.info("WalletUserRegistrationService >>>> AddEwalletUser"+custWaalletJson);
				
			}else {
				logger.info("WalletUserRegistrationService   >>> AddEwalletUser===Null");
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		return custId;
	}
}
