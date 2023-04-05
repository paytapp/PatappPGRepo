package com.paymentgateway.crm.actionBeans;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.api.Hasher;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.user.ResponseObject;
import com.paymentgateway.commons.user.Roles;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.CrmFieldConstants;
import com.paymentgateway.commons.util.SaltFactory;
import com.paymentgateway.commons.util.SaltFileManager;
import com.paymentgateway.commons.util.TransactionManager;
import com.paymentgateway.commons.util.UserStatusType;

@Service
public class CreateAcquirer {

	@Autowired
	UserDao userDao;

	@Autowired
	@Qualifier("saltFileManager")
	SaltFileManager saltFileManager;

	private static Logger logger = LoggerFactory.getLogger(CreateAcquirer.class.getName());

	public ResponseObject createNewAcquirer(User user) throws SystemException {
		logger.info("Inside createNewAcquirer createNewAcquirer ");
		ResponseObject responseObject = new ResponseObject();
		ResponseObject responseActionObject = new ResponseObject();
		CheckExistingUser checkExistingUser = new CheckExistingUser();
		Date date = new Date();
		String salt = SaltFactory.generateRandomSalt();
		//responseObject = checkExistingUser.checkuser(user.getEmailId());
		responseObject = checkExistingUser.checkuserByFirstNameAndLastNameAndBusinessName(user.getFirstName(),user.getLastName(),user.getBusinessName());
		logger.info(" is existing user ? " + responseObject.getResponseMessage());
		if (ErrorType.USER_AVAILABLE.getResponseCode().equals(responseObject.getResponseCode())) {
			
			user.setUserType(UserType.ACQUIRER);
			user.setUserStatus(UserStatusType.ACTIVE);
			user.setPayId(getpayId());
			user.setAccountValidationKey(TransactionManager.getNewTransactionId());
			user.setEmailValidationFlag(false);
			//user.setExpressPayFlag(false);
			user.setRegistrationDate(date);
			user.setBusinessName(user.getBusinessName());
			user.setFirstName(user.getFirstName());
			user.setLastName(user.getLastName());
			user.setMobile(user.getMobile());

			if (null != user.getPassword()) {
				user.setPassword(Hasher.getHash(user.getPassword().concat(salt)));
			} else {
				user.setPassword("");
			}
			
			if (null != user.getPin()) {
				user.setPin(Hasher.getHash(user.getPin().concat(salt)));
			} else {
				user.setPin("");
			}
			
			Set<Roles> roles = new HashSet<Roles>();
			Roles role = new Roles();

			role.setName(UserType.ACQUIRER.name());
			roles.add(role);

			user.setRoles(roles);

			userDao.create(user);

			boolean isSaltInserted = saltFileManager.insertSalt(user.getPayId(), salt);

			if (!isSaltInserted) {
				userDao.delete(user);
				throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR,
						ErrorType.INTERNAL_SYSTEM_ERROR.getResponseMessage());
			}
			responseActionObject.setResponseCode(ErrorType.SUCCESS.getResponseCode());
			responseActionObject.setResponseMessage(CrmFieldConstants.DETAILS_ACQUIRER_SUCCESSFULLY.getValue());
			responseActionObject.setAccountValidationID(user.getAccountValidationKey());
			responseActionObject.setEmail(user.getEmailId());
			logger.info("Create new Acquirer success , email id = " + user.getEmailId());
		} else {
			responseActionObject.setResponseCode(ErrorType.USER_ALREADY_EXISTS.getResponseCode());
			responseActionObject.setResponseMessage(ErrorType.USER_ALREADY_EXISTS.getResponseMessage());
		}
		return responseActionObject;
	}

	private String getpayId() {
		return TransactionManager.getNewTransactionId();
	}

}
