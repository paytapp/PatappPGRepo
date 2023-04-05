package com.paymentgateway.crm.actionBeans;

import org.springframework.stereotype.Service;

import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;

@Service
public class AdminRecordUpdater {

	public User updateUserProfile(User userFE) {
		
		User userFromDB = new User();
		UserDao userDao = new UserDao();
		
		userFromDB = userDao.findPayId(userFE.getPayId());
		userFromDB.setBusinessName(userFE.getBusinessName());
		userFromDB.setEmailId(userFE.getEmailId());
		userFromDB.setUserStatus(userFE.getUserStatus());
		
		userDao.update(userFromDB);
		return userFromDB;
	}

}
