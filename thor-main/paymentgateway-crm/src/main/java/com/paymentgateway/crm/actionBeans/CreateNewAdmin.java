package com.paymentgateway.crm.actionBeans;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.api.Hasher;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.user.PermissionType;
import com.paymentgateway.commons.user.Permissions;
import com.paymentgateway.commons.user.ResponseObject;
import com.paymentgateway.commons.user.Roles;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.SaltFactory;
import com.paymentgateway.commons.util.SaltFileManager;
import com.paymentgateway.commons.util.TransactionManager;
import com.paymentgateway.commons.util.UserStatusType;

@Service
public class CreateNewAdmin {
	
	@Autowired
	private UserDao userDao;
	
	@Autowired
	@Qualifier("saltFileManager")
	private SaltFileManager saltFileManager;
	
	@Autowired
	private CheckExistingUser checkExistingUser;
	
	
	public ResponseObject createUser(User user, UserType userType) throws SystemException {
		ResponseObject responseObject = new ResponseObject();
		ResponseObject responseActionObject = new ResponseObject();
		Date date = new Date();
		String salt = SaltFactory.generateRandomSalt();
		responseObject = checkExistingUser.checkuser(user.getEmailId());
		if (ErrorType.USER_AVAILABLE.getResponseCode().equals(responseObject.getResponseCode())) {
			Permissions permission1 = new Permissions();
			Permissions permission2 = new Permissions();
			Permissions permission3 = new Permissions();
			Set<Permissions> permissions = new HashSet<Permissions>();
			permission1.setPermissionType(PermissionType.CREATEUSER);
			permission2.setPermissionType(PermissionType.DELETEUSER);
			permission3.setPermissionType(PermissionType.LOGIN);
			permissions.add(permission1);
			permissions.add(permission2);
			permissions.add(permission3);
			Set<Roles> roles = new HashSet<Roles>();
			Roles role = new Roles();
			role.setPermissions(permissions);
			role.setName(UserType.SUPERADMIN.name());
			roles.add(role);
			user.setRoles(roles);
			user.setUserType(userType);
			user.setUserStatus(UserStatusType.PENDING);
			user.setPayId(getpayId());
			user.setAccountValidationKey(TransactionManager.getNewTransactionId());
			user.setEmailValidationFlag(false);
			//user.setExpressPayFlag(false);
			user.setRegistrationDate(date);
			// This condition is created for subuser
			if (null != user.getPassword()) {
				user.setPassword(Hasher.getHash(user.getPassword().concat(salt)));
			}
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
		} else {
			responseActionObject.setResponseCode(ErrorType.USER_UNAVAILABLE.getResponseCode());
			responseActionObject.setResponseMessage(ErrorType.USER_UNAVAILABLE.getResponseMessage());
		}
		return responseActionObject;
	}

	private String getpayId() {
		return TransactionManager.getNewTransactionId();
	}
}
