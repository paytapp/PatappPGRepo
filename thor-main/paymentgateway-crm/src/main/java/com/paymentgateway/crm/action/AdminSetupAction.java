package com.paymentgateway.crm.action;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ModelDriven;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.Fields;

public class AdminSetupAction extends AbstractSecureAction implements ModelDriven<User>{
	/**
	 * @ Neeraj
	 */
	
	@Autowired
	private UserDao userDao;
	
	private static final long serialVersionUID = 8870322503068475573L;
	private static Logger logger = LoggerFactory.getLogger(AdminSetupAction.class.getName());
	private User user = new User();
	
	public String execute() {
		try {
		  setUser(userDao.findPayId(user.getPayId()));
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

	@Override
	public User getModel() {
	
		return user;
	}

}
