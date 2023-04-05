package com.paymentgateway.crm.action;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CrmFieldConstants;
import com.paymentgateway.commons.util.CrmFieldType;
import com.paymentgateway.commons.util.CrmValidator;


/**
 * @author SHASHI
 *
 */
public class DefaultCurrencyUpdater extends AbstractSecureAction {
	
	@Autowired
	private UserDao userDao;
	
	@Autowired
	private CrmValidator validator;
	
	
	private static Logger logger = LoggerFactory.getLogger(DefaultCurrencyUpdater.class.getName());

	private static final long serialVersionUID = 7631759858298731581L;
	private String defaultCurrency;
	private String response;
	public String execute() {
		User userFromDB;
		User sessionUser;
		try {
			sessionUser = (User) sessionMap.get(Constants.USER.getValue());
				userFromDB = userDao.findPayId(sessionUser.getPayId());
				userFromDB.setDefaultCurrency(defaultCurrency);
				userDao.update(userFromDB);
				sessionMap.replace(Constants.USER.getValue(),userFromDB);
				response = CrmFieldConstants.DEFAUL_CURRENCY_UPDATE.getValue();
			return SUCCESS;
		}
		catch (Exception exception) {
			logger.error("DefaultCurrencyUpdater", exception);
			ErrorType.INVALID_DEFAULT_CURRENCY.getResponseMessage();
			return ERROR;
		}

	}
	public void validate(){
		 if(!validator.validateField(CrmFieldType.DEFAULT_CURRENCY, getDefaultCurrency())){
			 addFieldError(CrmFieldType.DEFAULT_CURRENCY.getName(),ErrorType.INTERNAL_SYSTEM_ERROR.getResponseMessage());
		}
	} 
	
	public String getDefaultCurrency() {
		return defaultCurrency;
	}

	public void setDefaultCurrency(String defaultCurrency) {
		this.defaultCurrency = defaultCurrency;
	}

	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
	}
}
