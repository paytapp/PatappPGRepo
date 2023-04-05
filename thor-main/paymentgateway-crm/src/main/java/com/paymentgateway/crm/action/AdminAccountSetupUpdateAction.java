package com.paymentgateway.crm.action;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ModelDriven;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.CrmFieldType;
import com.paymentgateway.commons.util.CrmValidator;
import com.paymentgateway.crm.actionBeans.AdminRecordUpdater;

public class AdminAccountSetupUpdateAction extends AbstractSecureAction implements  ModelDriven<User> {

	/**
	 * @ Neeraj
	 */
	private static final long serialVersionUID = -2804573223359698889L;
	private static Logger logger = LoggerFactory.getLogger(AdminAccountSetupUpdateAction.class.getName());
	
	@Autowired
	private UserDao userDao;
	
	@Autowired
	private CrmValidator validator;
	@Autowired
	private AdminRecordUpdater adminRecordUpdater;
	private String payId ;
	private User user = new User();
	public String updateAdminSetup(){
		User userDb = userDao.findPayId(user.getPayId());
		
		Date date = new Date();
		try{
			userDb.setActivationDate(date);
			userDb.setUserStatus(user.getUserStatus());
			setUser(adminRecordUpdater.updateUserProfile(userDb));
			addActionMessage("Admin Status successfully Updated.");
			return SUCCESS;
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return ERROR;     
		}
		
	}
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}

	public void validate(){
		
		if ((validator.validateBlankField(getPayId()))) {
			addFieldError(CrmFieldType.PAY_ID.getName(), validator.getResonseObject().getResponseMessage());
		} else if (!(validator.validateField(CrmFieldType.PAY_ID, getPayId()))) {
			addFieldError(CrmFieldType.PAY_ID.getName(), validator.getResonseObject().getResponseMessage());
		}
	}

	@Override
	public User getModel() {
		return user;
	}
	public String getPayId() {
		return payId;
	}
	public void setPayId(String payId) {
		this.payId = payId;
	}

}
