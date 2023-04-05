package com.paymentgateway.crm.actionBeans;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.api.Hasher;
import com.paymentgateway.commons.dao.HibernateSessionProvider;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.user.PermissionType;
import com.paymentgateway.commons.user.Permissions;
import com.paymentgateway.commons.user.ResponseObject;
import com.paymentgateway.commons.user.Roles;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.ConfigurationConstants;
import com.paymentgateway.commons.util.SaltFactory;
import com.paymentgateway.commons.util.SaltFileManager;
import com.paymentgateway.commons.util.SubAdmin;
import com.paymentgateway.commons.util.TransactionManager;
import com.paymentgateway.commons.util.UserStatusType;

@Service
public class CreateSubAdmin {
	
	@Autowired
	UserDao userDao;
	
	@Autowired
	Hasher hasher;
	
	@Autowired
	@Qualifier("saltFileManager")
	SaltFileManager saltFileManager;
	
	private static final int emailExpiredInTime = ConfigurationConstants.EMAIL_EXPIRED_HOUR.getValues();
	
	private static Logger logger = LoggerFactory.getLogger(CreateSubAdmin.class.getName());
	
	public ResponseObject createNewSubAdmin(User user, UserType userType, String parentPayId, Set<Permissions> permissions,String emailId)
			throws SystemException {
		logger.info("inside createNewSubadmin CreateSubAdmin ");
		ResponseObject responseObject = new ResponseObject();
		ResponseObject responseActionObject = new ResponseObject();
		CheckExistingUser checkExistingUser = new CheckExistingUser();
		SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		Date date = new Date();
		String salt = SaltFactory.generateRandomSalt();
		responseObject = checkExistingUser.checkuser(user.getEmailId());
		logger.info(" is existing user ? " + responseObject.getResponseMessage());
		if (ErrorType.USER_AVAILABLE.getResponseCode().equals(responseObject.getResponseCode())) {
			user.setUserType(userType);
			user.setUserStatus(UserStatusType.ACTIVE);
			user.setPayId(getpayId());
			user.setAccountValidationKey(TransactionManager.getNewTransactionId());
			user.setEmailValidationFlag(false);
			//user.setExpressPayFlag(false);
			user.setRegistrationDate(date);
			user.setBusinessName(user.getFirstName());
			
			if (!permissions.isEmpty()) {
				Iterator<Permissions> itr = permissions.iterator();
				while (itr.hasNext()) {
					PermissionType permissionType = itr.next().getPermissionType();
					String permission = permissionType.getPermission();
					if( permission == PermissionType.APPROVE_MPA.getPermission()) {
						user.setPermissionType("Checker");
					}else if(permission == PermissionType.REVIEW_MPA.getPermission()) {
						user.setPermissionType("Maker");
					}
				}
			}
			
			setExpiryTime(user);
			// This condition is created for Agent
			if (null != user.getPassword()) {
				user.setPassword(hasher.getHash(user.getPassword().concat(salt)));
			} else {
				user.setPassword("");// tp prevent password from being set null
			}
			user.setParentPayId(parentPayId);
			Set<Roles> roles = new HashSet<Roles>();
			Roles role = new Roles();

			role.setPermissions(permissions);
			role.setName(UserType.ADMIN.name());
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
			responseActionObject.setResponseCode(ErrorType.SUCCESS.getResponseCode());
			responseActionObject.setAccountValidationID(user.getAccountValidationKey());
			responseActionObject.setEmail(user.getEmailId());
			responseActionObject.setCreationDate(formatter.format(user.getRegistrationDate()));
			responseActionObject.setUserType(user.getUserType().name());
			logger.info("Create new subadmin success , email id = " + user.getEmailId());
		} else {
			responseActionObject.setResponseCode(ErrorType.USER_UNAVAILABLE.getResponseCode());
			responseActionObject.setResponseMessage(ErrorType.USER_UNAVAILABLE.getResponseMessage());
		}
		return responseActionObject;
	}
	
	public User setExpiryTime(User user){
		Date currnetDate = new Date();
		Calendar c = Calendar.getInstance(); 
		c.setTime(currnetDate); 
		c.add(Calendar.HOUR, emailExpiredInTime);
		Date expiryDate = c.getTime();
		user.setEmailExpiryTime(expiryDate);
		return user;
	}

	private String getpayId() {
		return TransactionManager.getNewTransactionId();
	}

}
