package com.paymentgateway.crm.actionBeans;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.user.UserRecordsDao;

/**
 * @author Chandan
 */

@Service
public class CheckOldPassword {
	
	private Logger logger = LoggerFactory.getLogger(CheckOldPassword.class.getName());
	
	@Autowired
	private UserRecordsDao userRecordsDao;
	
public  boolean isUsedPin(String newPin, String mobileNumber) {
		logger.info("Inside isUsedPin() checking old PIN for Mobile Number = " + mobileNumber);
		List<String> oldPin = new ArrayList<String>();
		oldPin = userRecordsDao.getOldPin(mobileNumber);
		if (oldPin.isEmpty()){
			logger.info("old passwords is empty");
		}
		if (!oldPin.isEmpty()){
			for (String pin : oldPin) {
				if (null == pin) {
					continue;
				}
				if (pin.equals(newPin)) {
					return true;
				}
			}
		}
		
		return false;
	}
}
