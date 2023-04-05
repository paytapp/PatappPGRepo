package com.paymentgateway.crm.action;

import java.util.List;

import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.user.LoginHistory;
import com.paymentgateway.commons.user.LoginHistoryDao;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CrmFieldConstants;
import com.paymentgateway.commons.util.CrmFieldType;
import com.paymentgateway.commons.util.CrmValidator;
import com.paymentgateway.commons.util.DataEncoder;

public class LoginHistorySubUserAction extends AbstractSecureAction {

	
	@Autowired
	LoginHistoryDao loginHistoryDao;
	
	@Autowired
	DataEncoder encoder;
	
	@Autowired
	private CrmValidator validator;
	
	private static Logger logger = LoggerFactory.getLogger(LoginHistorySubUserAction.class.getName());
	private static final long serialVersionUID = -6336397235790682930L;
	
	private List<LoginHistory> aaData;
	private String emailId;

	public String execute() {
		try {
			User user = (User) sessionMap.get(Constants.USER);
			if (user.getUserType() == UserType.MERCHANT) {
				if( (getEmailId().equals(CrmFieldConstants.ALL_USERS.getValue()))) {
					aaData = encoder.encodeLoginHistoryObj(loginHistoryDao.findAllUsers(user.getPayId()));
				}
				else {
					aaData =encoder.encodeLoginHistoryObj((List<LoginHistory>) loginHistoryDao
							.findLoginHisAllSubUser(getEmailId(), user.getPayId()));
				}
			} 
			return SUCCESS;
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return ERROR;
		}
	}

	public void validate(){

		if(validator.validateBlankField(getEmailId()) || getEmailId().equals(CrmFieldConstants.ALL_MERCHANTS.getValue()) || getEmailId().equals(CrmFieldConstants.ALL_USERS.getValue())){
		}
        else if(!validator.validateField(CrmFieldType.EMAILID, getEmailId())){
        	addFieldError(CrmFieldType. EMAILID.getName(), ErrorType.INVALID_FIELD.getResponseMessage());
		}
	}

	public List<LoginHistory> getAaData() {
		return aaData;
	}

	public void setAaData(List<LoginHistory> aaData) {
		this.aaData = aaData;
	}

	public String getEmailId() {
		return emailId;
	}

	public void setEmailId(String emailId) {
		this.emailId = emailId;
	}

}
