package com.paymentgateway.crm.action;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.CrmFieldType;
import com.paymentgateway.commons.util.CrmValidator;
import com.paymentgateway.commons.util.Currency;

/**
 * @author shashi
 *
 */
public class MerchantCurrencyAction extends AbstractSecureAction {
	
	@Autowired
	private UserDao userDao;
	@Autowired
	private CrmValidator validator;

	private static Logger logger = LoggerFactory.getLogger(MerchantCurrencyAction.class.getName());
	private static final long serialVersionUID = -4779558725256308048L;
	private String emailId;
	private String payId;
	private String response;
	private Map<String, String> currencyMap;

	public String execute() {
		try {
			User userFromDb;
			if(payId!=null){
				userFromDb=userDao.findPayId(payId);
				currencyMap = Currency.getSupportedCurreny(userFromDb);
				// Sub Merchant User
				if (!userFromDb.isSuperMerchant() && StringUtils.isNotBlank(userFromDb.getSuperMerchantId())) {
					User superMerchant  = userDao.findPayId(userFromDb.getSuperMerchantId());
					if (superMerchant != null) {
						currencyMap = Currency.getSupportedCurreny(superMerchant);
					}
				}
			}
			else{
			userFromDb = userDao.findPayIdByEmail(emailId);
			currencyMap = Currency.getSupportedCurreny(userFromDb);
			// Sub Merchant User
			if (!userFromDb.isSuperMerchant() && StringUtils.isNotBlank(userFromDb.getSuperMerchantId())) {
				User superMerchant  = userDao.findPayId(userFromDb.getSuperMerchantId());
				if (superMerchant != null) {
					currencyMap = Currency.getSupportedCurreny(superMerchant);
				}
			}
			}
			return SUCCESS;
		} catch (Exception exception) {
			logger.error("Exception ", exception);
			return ERROR;
		}
	}

	public void validate() {
		/*if (validator.validateBlankField(getEmailId())) {
			addFieldError(CrmFieldType.EMAILID.getName(), validator.getResonseObject().getResponseMessage());
		} else if (!(validator.isValidEmailId(getEmailId()))) {
			addFieldError(CrmFieldType.EMAILID.getName(), ErrorType.INVALID_FIELD_VALUE.getResponseMessage());
		}*/
		if ((validator.validateBlankField(getResponse()))) {
			/*addFieldError(CrmFieldType.RESPONSE.getName(), validator.getResonseObject().getResponseMessage());*/
		} else if (!(validator.validateField(CrmFieldType.RESPONSE, getResponse()))) {
			addFieldError(CrmFieldType.RESPONSE.getName(), validator.getResonseObject().getResponseMessage());
		}
	}

	public String getEmailId() {
		return emailId;
	}

	public void setEmailId(String emailId) {
		this.emailId = emailId;
	}

	public String getResponse() {
		return response;
	}
	
	
	public String getPayId() {
		return payId;
	}

	public void setPayId(String payId) {
		this.payId = payId;
	}

	public void setResponse(String response) {
		this.response = response;
	}

	public Map<String, String> getCurrencyMap() {
		return currencyMap;
	}

	public void setCurrencyMap(Map<String, String> currencyMap) {
		this.currencyMap = currencyMap;
	}

}
