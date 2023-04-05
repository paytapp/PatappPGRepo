package com.paymentgateway.crm.actionBeans;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.api.Hasher;
import com.paymentgateway.commons.dao.UserSettingDao;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.user.PermissionType;
import com.paymentgateway.commons.user.Permissions;
import com.paymentgateway.commons.user.ResponseObject;
import com.paymentgateway.commons.user.Roles;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserSettingData;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.ConfigurationConstants;
import com.paymentgateway.commons.util.SaltFactory;
import com.paymentgateway.commons.util.SaltFileManager;
import com.paymentgateway.commons.util.TransactionManager;
import com.paymentgateway.commons.util.UserStatusType;

/**
 * @author Chandan
 *
 */

@Service
public class CreateSubUser {

	@Autowired
	private Hasher hasher;

	@Autowired
	private UserDao userDao;

	@Autowired
	@Qualifier("saltFileManager")
	private SaltFileManager saltFileManager;
	
	@Autowired
	private UserSettingDao userSettingDao;


	@Autowired
	private CheckExistingUser checkExistingUser;

	private static final int emailExpiredInTime = ConfigurationConstants.EMAIL_EXPIRED_HOUR.getValues();

	public ResponseObject createUser(User user, UserType userType, String parentPayId, Set<Permissions> permissions, String subUserType) throws SystemException {

		ResponseObject responseObject = new ResponseObject();
		ResponseObject responseActionObject = new ResponseObject();
		SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		Date date = new Date();
		String salt = SaltFactory.generateRandomSalt();

		responseObject = checkExistingUser.checkuser(user.getEmailId());
		if (ErrorType.USER_AVAILABLE.getResponseCode().equals(responseObject.getResponseCode())) {
			UserSettingData merchantSettings=userSettingDao.fetchDataUsingPayId(user.getPayId());
			
			user.setUserType(userType);
			user.setUserStatus(UserStatusType.ACTIVE);
			user.setPayId(getpayId());
			user.setAccountValidationKey(TransactionManager.getNewTransactionId());
			user.setEmailValidationFlag(false);
			//user.setExpressPayFlag(false);
			user.setRegistrationDate(date);
			if(subUserType!=null) {
				user.setSubUserType(subUserType);
			}else {
				user.setSubUserType("");
			}
			for(Permissions pr : permissions) {
				if(pr.getPermissionType().name().equals("ENACH_REPORT")) {
					user.seteNachReportFlag(true);
				}
				if(pr.getPermissionType().name().equals("UPI_AUTOPAY_REPORT")) {
					user.setUpiAutoPayReportFlag(true);
				}
				if(pr.getPermissionType().name().equals("ACCOUNT_VERIFICATION")){
					user.setAccountVerificationFlag(true);
				}
				if(pr.getPermissionType().name().equals("VPA_VERIFICATION")){
					user.setVpaVerificationFlag(true);
				}
				if(pr.getPermissionType().name().equals("ALLOW_EPOS")){
					user.setEposMerchant(true);
				}
			}
			//user.setEposMerchant(allowEpos);
			setExpiryTime(user);
			// This condition is created for subuser
			if (null != user.getPassword()) {
				user.setPassword(hasher.getHash(user.getPassword().concat(salt)));
			} else {
				user.setPassword("");// tp prevent password from being set null
			}

			if (null != user.getPin()) {
				user.setPin(hasher.getHash(user.getPin().concat(salt)));
			} else {
				user.setPin("");
			}
			
			User user1 = userDao.findPayId(parentPayId);
			if(user1.isCustomTransactionStatus()) {
				user.setCustomTransactionStatus(true);
			}
			if(user1.isMerchantInitiatedDirectFlag()) {
				user.setMerchantInitiatedDirectFlag(true);
			}
			
			user.setParentPayId(parentPayId);
			user.setPhoneValidationFlag(false);

			Set<Roles> roles = new HashSet<Roles>();
			Roles role = new Roles();

			role.setPermissions(permissions);
			role.setName(UserType.MERCHANT.name());
			roles.add(role);

			user.setRoles(roles);

			userDao.create(user);

			// Insert salt in salt.properties
			boolean isSaltInserted = saltFileManager.insertSalt(user.getPayId(), salt);

			if (!isSaltInserted) {
				// Rollback user creation
				userDao.delete(user);
				throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR,
						ErrorType.INTERNAL_SYSTEM_ERROR.getResponseMessage());
			}
			responseObject.setResponseCode(ErrorType.SUCCESS.getResponseCode());
			responseObject.setAccountValidationID(user.getAccountValidationKey());
			responseObject.setEmail(user.getEmailId());
			responseObject.setUserType(user.getUserType().name());
		} else {
			responseObject.setResponseCode(ErrorType.USER_UNAVAILABLE.getResponseCode());
			responseObject.setResponseMessage(ErrorType.USER_UNAVAILABLE.getResponseMessage());
		}
		return responseObject;
	}

	
	public User setExpiryTime(User user) {
		Date currnetDate = new Date();
		Calendar c = Calendar.getInstance();
		c.setTime(currnetDate);
		c.add(Calendar.HOUR, emailExpiredInTime);
		currnetDate = c.getTime();
		user.setEmailExpiryTime(currnetDate);
		return user;
	}

	private String getpayId() {
		return TransactionManager.getNewTransactionId();
	}
}
