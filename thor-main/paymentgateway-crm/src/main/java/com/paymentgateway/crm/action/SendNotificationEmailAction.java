package com.paymentgateway.crm.action;

import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.gson.Gson;
import com.paymentgateway.commons.user.NotificationEmailer;
import com.paymentgateway.commons.util.CrmFieldType;
import com.paymentgateway.commons.util.CrmValidator;
import com.paymentgateway.crm.actionBeans.MerchantRecordUpdater;

/**
 * @Neeraj
 */

public class SendNotificationEmailAction extends AbstractSecureAction {

	@Autowired
	private MerchantRecordUpdater merchantRecordUpdater;

	@Autowired
	private CrmValidator validator;

	private static final long serialVersionUID = 5574018510722062093L;
	private static Logger logger = LoggerFactory.getLogger(SendNotificationEmailAction.class.getName());
	private NotificationEmailer notificationEmailer = new NotificationEmailer();
	private String payId;
	private String transactionEmail;

	public String saveNotificationEamil() {

		try {
			Gson gson = new Gson();
			NotificationEmailer notificationEmailer = gson.fromJson(transactionEmail, NotificationEmailer.class);
			setNotificationEmailer(merchantRecordUpdater.updateNotificationEmail(notificationEmailer, payId));
		} catch (Exception exception) {
			logger.error("error : " , exception);
		}
		addActionMessage("Update Successfuly Notification Flag");
		return SUCCESS;
	}

	public void validate() {
		if ((validator.validateBlankField(getPayId()))) {
			addFieldError(CrmFieldType.PAY_ID.getName(), validator.getResonseObject().getResponseMessage());
		} else if (!(validator.validateField(CrmFieldType.PAY_ID, getPayId()))) {
			addFieldError(CrmFieldType.PAY_ID.getName(), validator.getResonseObject().getResponseMessage());
		}
		if ((validator.validateBlankField(getTransactionEmail()))) {
			addFieldError(CrmFieldType.EMAILID.getName(), validator.getResonseObject().getResponseMessage());
		} else if (!(validator.validateField(CrmFieldType.EMAILID, getTransactionEmail()))) {
			addFieldError(CrmFieldType.EMAILID.getName(), validator.getResonseObject().getResponseMessage());
		}

	}

	public NotificationEmailer getNotificationEmailer() {
		return notificationEmailer;
	}

	public void setNotificationEmailer(NotificationEmailer notificationEmailer) {
		this.notificationEmailer = notificationEmailer;
	}

	public String getPayId() {
		return payId;
	}

	public void setPayId(String payId) {
		this.payId = payId;
	}

	public String getTransactionEmail() {
		return transactionEmail;
	}

	public void setTransactionEmail(String transactionEmail) {
		this.transactionEmail = transactionEmail;
	}

}
