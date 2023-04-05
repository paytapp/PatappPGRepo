package com.paymentgateway.crm.action;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.dao.SearchUserService;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.user.Merchants;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CrmFieldType;
import com.paymentgateway.commons.util.CrmValidator;
import com.paymentgateway.commons.util.DataEncoder;

public class SearchUserAction extends AbstractSecureAction { 
	
	@Autowired
	private SearchUserService searchUserService;

	@Autowired
	private CrmValidator validator;
	
	@Autowired
	private DataEncoder encoder;
	
	private static Logger logger = LoggerFactory.getLogger(SearchUserAction.class.getName());
	private static final long serialVersionUID = 6916374933409478597L;

	private List<Merchants> aaData = new ArrayList<Merchants>();
	private String emailId;
	private String phoneNo;

	public String execute() {
		User sessionUser = (User) sessionMap.get(Constants.USER.getValue());
		try {
			if(sessionUser.getUserType().equals(UserType.ACQUIRER)){
				setAaData(encoder.encodeMerchantsObj(searchUserService.getAcquirerSubUsers(sessionUser.getPayId())));
			}else{
				setAaData(encoder.encodeMerchantsObj(searchUserService.getSubUsers(sessionUser.getPayId(), sessionUser.getUserStatus())));
			}
		} catch (Exception exception) {
			logger.error("Exception", exception);
		}
		return SUCCESS;
	}

	public void validate(){

		if(validator.validateBlankField(getEmailId())){
		}
        else if(!validator.validateField(CrmFieldType.EMAILID, getEmailId())){
        	addFieldError(CrmFieldType. EMAILID.getName(), ErrorType.INVALID_FIELD.getResponseMessage());
		}

		if(validator.validateBlankField(getPhoneNo())){
		}
        else if(!validator.validateField(CrmFieldType.MOBILE, getPhoneNo())){
        	addFieldError("phoneNo", ErrorType.INVALID_FIELD.getResponseMessage());
		}
	}

	public String getEmailId() {
		return emailId;
	}

	public void setEmailId(String emailId) {
		this.emailId = emailId;
	}
	public String getPhoneNo() {
		return phoneNo;
	}

	public void setPhoneNo(String phoneNo) {
		this.phoneNo = phoneNo;
	}
	public List<Merchants> getAaData() {
		return aaData;
	}

	public void setAaData(List<Merchants> aaData) {
		this.aaData = aaData;
	}
}
