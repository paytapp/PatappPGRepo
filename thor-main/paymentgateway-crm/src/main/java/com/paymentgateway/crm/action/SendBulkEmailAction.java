package com.paymentgateway.crm.action;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.email.EmailServiceProvider;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.user.Merchants;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CrmFieldType;
import com.paymentgateway.commons.util.CrmValidator;
import com.paymentgateway.commons.util.UserStatusType;

/**
 * @neeraj
 */

public class SendBulkEmailAction extends AbstractSecureAction {

	@Autowired
	private UserDao userDao;
	
	@Autowired
	private EmailServiceProvider emailServiceProvider;
	
	@Autowired
	private CrmValidator validator;
	
	private static final long serialVersionUID = 2421842267236138348L;
	private static Logger logger = LoggerFactory.getLogger(SendBulkEmailAction.class.getName());
	private String subject;
	private String messageBody;
	private User sessionUser = new User();
	public String sendBulkEmail() {
		
		sessionUser = (User) sessionMap.get(Constants.USER.getValue());
		
		// verify User before sendign email
		
		if (sessionUser.getUserStatus() != UserStatusType.ACTIVE) {
			logger.info("User status is inactive");
			return ERROR;
		}
		List<Merchants> merchantsList = new ArrayList<Merchants>();
		try {
			merchantsList = userDao.featchAllmerchant();
			boolean first = true;
			String emailID = "";
			for (Merchants merchants : merchantsList) {
				String merchantemailId = merchants.getEmailId();
				if (first) {
					emailID += merchantemailId;
					first = false;
				} else {
					emailID += "," + merchantemailId;
				}
			}
			emailServiceProvider.sendBulkEmailServiceTax(emailID,subject,messageBody);
			subject= " ";
			messageBody= " ";
			
			addActionMessage("Email successfully sent.");
		} catch (SystemException exception) {
			logger.error("Exception", exception);
			return ERROR;
		}
		return SUCCESS;

	}
	public void validate() {
	if ((validator.validateBlankField(getSubject()))) {
		addFieldError(CrmFieldType.SUBJECT.getName(), validator
				.getResonseObject().getResponseMessage());
	} else if (!(validator.validateField(CrmFieldType.SUBJECT,
			getSubject()))) {
		addFieldError(CrmFieldType.SUBJECT.getName(), validator
				.getResonseObject().getResponseMessage());
	}
	if ((validator.validateBlankField(getMessageBody()))){
		addFieldError(CrmFieldType.MESSAGE.getName(), validator
				.getResonseObject().getResponseMessage());
	} else if (!(validator.validateField(CrmFieldType.MESSAGE,
			getMessageBody()))) {
		addFieldError(CrmFieldType.MESSAGE.getName(), validator
				.getResonseObject().getResponseMessage());
	}
	}

	public String getMessageBody() {
		return messageBody;
	}

	public void setMessageBody(String messageBody) {
		this.messageBody = messageBody;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

}
