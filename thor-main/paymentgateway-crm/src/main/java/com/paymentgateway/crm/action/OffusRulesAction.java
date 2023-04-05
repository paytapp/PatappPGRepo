package com.paymentgateway.crm.action;

import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.util.CrmFieldType;
import com.paymentgateway.commons.util.CrmValidator;

/**
 * @author lave gupta
 *
 */
public class OffusRulesAction extends AbstractSecureAction {

	@Autowired
	private CrmValidator validator;
	private static Logger logger = LoggerFactory.getLogger(OffusRulesAction.class.getName());

	private static final long serialVersionUID = 5779243311263572440L;

	private String listData;
	private String message;

	public String execute() {
		try {
			return SUCCESS;
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return ERROR;
		}

	}

	public void validate() {
		if ((validator.validateBlankField(getListData()))) {
			addFieldError(CrmFieldType.MAP_STRING.getName(), validator.getResonseObject().getResponseMessage());
		} else if (!(validator.validateField(CrmFieldType.MAP_STRING, getListData()))) {
			addFieldError(CrmFieldType.OPERATION.getName(), validator.getResonseObject().getResponseMessage());
		}
		if ((validator.validateBlankField(getMessage()))) {
			addFieldError(CrmFieldType.RESPONSE_MESSAGE.getName(), validator.getResonseObject().getResponseMessage());
		} else if (!(validator.validateField(CrmFieldType.RESPONSE_MESSAGE, getMessage()))) {
			addFieldError(CrmFieldType.RESPONSE_MESSAGE.getName(), validator.getResonseObject().getResponseMessage());
		}
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getListData() {
		return listData;
	}

	public void setListData(String listData) {
		this.listData = listData;
	}

}
