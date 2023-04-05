package com.paymentgateway.crm.action;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.dao.SearchUserService;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.Agent;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CrmFieldType;
import com.paymentgateway.commons.util.CrmValidator;
import com.paymentgateway.commons.util.DataEncoder;
import com.paymentgateway.commons.util.SubAdmin;

public class SearchAgentAction extends AbstractSecureAction {

	
	private static final long serialVersionUID = 5929390136391392637L;
	private static Logger logger = LoggerFactory.getLogger(SearchAgentAction.class.getName());
	private List<Agent> aaData = new ArrayList<Agent>();
	private String emailId;
	private String phoneNo;

	@Autowired
	private SearchUserService searchUserService;
	
	public String execute() {
		User sessionUser = (User) sessionMap.get(Constants.USER.getValue());
		DataEncoder encoder = new DataEncoder();
		try {
			// TODO resolve error
			if ((sessionUser.getUserType().equals(UserType.ADMIN)) || (sessionUser.getUserType().equals(UserType.SUBADMIN))) {
				setAaData(encoder.encodenewAgentsObj(searchUserService.getnewAgentsList()));

			}

		} catch (Exception exception) {
			logger.error("Exception", exception);
		}
		return SUCCESS;
	}

	public void validate() {
		CrmValidator validator = new CrmValidator();

		if (validator.validateBlankField(getEmailId())) {
		} else if (!validator.validateField(CrmFieldType.EMAILID, getEmailId())) {
			addFieldError(CrmFieldType.EMAILID.getName(), ErrorType.INVALID_FIELD.getResponseMessage());
		}

		if (validator.validateBlankField(getPhoneNo())) {
		} else if (!validator.validateField(CrmFieldType.MOBILE, getPhoneNo())) {
			addFieldError("phoneNo", ErrorType.INVALID_FIELD.getResponseMessage());
		}
	}

	
	public List<Agent> getAaData() {
		return aaData;
	}

	public void setAaData(List<Agent> aaData) {
		this.aaData = aaData;
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

}