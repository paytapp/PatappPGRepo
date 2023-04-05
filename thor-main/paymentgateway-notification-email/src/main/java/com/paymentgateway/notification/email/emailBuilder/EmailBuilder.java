package com.paymentgateway.notification.email.emailBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.user.Invoice;
import com.paymentgateway.commons.user.ResponseObject;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.AWSSESEmailService;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CrmFieldConstants;
import com.paymentgateway.commons.util.EmailerConstants;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.MPAStatusEmail;
import com.paymentgateway.commons.util.MailersObject;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.notification.email.emailBodyCreater.EmailBodyCreator;
import com.paymentgateway.notification.email.objects.MerchantPaymentAdviseReportpdfAutoSend;

/**
 * @author Neeraj
 *
 */
@Component
public class EmailBuilder {

	private Logger logger = LoggerFactory.getLogger(EmailBuilder.class.getName());
	private String body;
	private String subject;
	private String toEmail;
	private String emailToBcc;
	private boolean emailExceptionHandlerFlag;
	private StringBuilder responseMessage = new StringBuilder();

	@Autowired
	@Qualifier("userDao")
	private UserDao userDao;

	@Autowired
	private EmailBodyCreator emailBodyCreator;

	@Autowired
	@Qualifier("propertiesManager")
	private PropertiesManager propertiesManager;

	/*
	 * @Autowired MerchantPaymentAdviseReportAutoSend
	 * merchantPaymentAdviseReportAutoSend;
	 */

	@Autowired
	MerchantPaymentAdviseReportpdfAutoSend merchantPaymentAdviseReportpdfAutoSend;

	@Autowired
	Emailer emailer;
	
	@Autowired
	AWSSESEmailService awsSESEmailService;

	public void transactionEmailer(Map<String, String> responseMap, String senderType, User user) throws Exception {
		String heading = null;
		String message = null;
		String sendToEmail = null;
		String defaultMerchantEmail = null;
		try {
			if (senderType.equals(UserType.MERCHANT.toString())) {
				heading = CrmFieldConstants.MERCHANT_HEADING.getValue();
				message = CrmFieldConstants.MERCHANT_MESSAGE.getValue();
				sendToEmail = user.getTransactionEmailId();
				defaultMerchantEmail = user.getEmailId();
				if (responseMap.get(FieldType.RESPONSE_CODE.getName()).equals(Constants.SUCCESS_CODE.getValue())) {
					body = emailBodyCreator.bodyTransactionEmail(responseMap, heading, message);
					//emailer.sendEmail(body, message, defaultMerchantEmail, sendToEmail, isEmailExceptionHandlerFlag());
					awsSESEmailService.sendEmail(body, message, defaultMerchantEmail, sendToEmail);
				} else {
					body = emailBodyCreator.transactionFailled(responseMap);
					//emailer.sendEmail(body, message, defaultMerchantEmail, sendToEmail, isEmailExceptionHandlerFlag());
					awsSESEmailService.sendEmail(getBody(), getSubject(), getToEmail(), getEmailToBcc());
				}

			}
		} catch (Exception exception) {
			logger.error("Exception", exception);
		}
	}

	// transactionCustomerEmailerFlag()
	public void transactionCustomerEmail(Map<String, String> responseMap) throws Exception {
		String heading = null;
		String message = null;
		String sendToEmail = null;
		try {
			heading = CrmFieldConstants.CUSTOMER_HEADING.getValue();
			message = CrmFieldConstants.CUSTOMER_MESSAGE.getValue();
			sendToEmail = responseMap.get(FieldType.CUST_EMAIL.getName());

			if (responseMap.get(FieldType.RESPONSE_CODE.getName()).equals(Constants.SUCCESS_CODE.getValue())) {
				body = emailBodyCreator.bodyTransactionEmail(responseMap, heading, message);
				subject = EmailerConstants.COMPANY.getValue() + Constants.PAYMENT_RECEIVED_ACKNOWLEDGEMENT.getValue();
				toEmail = sendToEmail;
				setEmailExceptionHandlerFlag(false);
				/*
				 * emailer.sendEmail(getBody(), getSubject(), getToEmail(), getEmailToBcc(),
				 * isEmailExceptionHandlerFlag());
				 */
				awsSESEmailService.sendEmail(getBody(), getSubject(), getToEmail(), getEmailToBcc());
				// transaction Failed Acknowledgement
			} else {
				body = emailBodyCreator.transactionFailled(responseMap);
				subject = EmailerConstants.COMPANY.getValue() + Constants.PAYMENT_RECEIVED_ACKNOWLEDGEMENT.getValue();
				toEmail = sendToEmail;
				setEmailExceptionHandlerFlag(false);
				/*
				 * emailer.sendEmail(getBody(), getSubject(), getToEmail(), getEmailToBcc(),
				 * isEmailExceptionHandlerFlag());
				 */
				awsSESEmailService.sendEmail(getBody(), getSubject(), getToEmail(), getEmailToBcc());
			}

		} catch (Exception exception) {
			logger.error("Exception", exception);
		}

	}

	// Preparing Refund Transaction Email for Customer
	public void transactionRefundEmail(Map<String, String> fields, String senderType, String getToEmail,
			String businessName) throws Exception {
		String headingRefund = null;
		try {
			if (senderType.equals(UserType.MERCHANT.toString())) {
				headingRefund = CrmFieldConstants.MERCHANT_HEADING.getValue();
			} else {
				headingRefund = CrmFieldConstants.CUSTOMER_HEADING.getValue();
			}
			body = emailBodyCreator.refundEmailBody(fields, headingRefund, businessName);
			subject = EmailerConstants.COMPANY.getValue() + Constants.REFUND_FOR_ODER_ID.getValue()
					+ fields.get(FieldType.ORDER_ID.getName());
			toEmail = getToEmail;
			setEmailExceptionHandlerFlag(false);
			//emailer.sendEmail(getBody(), getSubject(), getToEmail(), getEmailToBcc(), isEmailExceptionHandlerFlag());
			awsSESEmailService.sendEmail(getBody(), getSubject(), getToEmail(), getEmailToBcc());
		} catch (Exception exception) {
			logger.error("Exception", exception);
		}
	}

	// Preparing Email for merchant registration validation
	public void emailValidator(ResponseObject responseObject) throws Exception {
		try {
			
				body = emailBodyCreator.accountValidation(responseObject.getAccountValidationID(),
						propertiesManager.getEmailProperty(Constants.EMAIL_VALIDATORURL.getValue()), "");
				subject = "Verify Your Account | Payment Gateway Solution Private Limited";
			
			toEmail = responseObject.getEmail();
			setEmailExceptionHandlerFlag(true);
			//emailer.sendEmail(getBody(), getSubject(), getToEmail(), getEmailToBcc(), isEmailExceptionHandlerFlag());
			awsSESEmailService.sendEmail(getBody(), getSubject(), getToEmail(), getEmailToBcc());
		} catch (Exception exception) {
			logger.error("Exception", exception);
		}
	}

	
	
	// Preparing Email for SubUser add
	public void emailAddUser(ResponseObject responseObject, String firstName) throws Exception {
		try {
			body = emailBodyCreator.addUser(firstName, responseObject,
					propertiesManager.getResetPasswordProperty(Constants.RESET_PASSWORD_URL.getValue()));

			if (!StringUtils.isEmpty(responseObject.getUserType())
					&& responseObject.getUserType().equalsIgnoreCase("SUBADMIN"))
				subject = "Sub-Admin Creation | Account Validation | " + responseObject.getName();
			else if (!StringUtils.isEmpty(responseObject.getUserType())
					&& responseObject.getUserType().equalsIgnoreCase("AGENT"))
				subject = "Agent Creation | Account Validation | " + responseObject.getName();
			else if (!StringUtils.isEmpty(responseObject.getUserType())
					&& responseObject.getUserType().equalsIgnoreCase("SUBUSER"))
				subject = "Sub-User Creation | Account Validation | " + responseObject.getName();
			// subject = Constants.ACCOUNT_VALIDATION_EMAIL.getValue() +
			// EmailerConstants.COMPANY.getValue();
			toEmail = responseObject.getEmail();
			setEmailExceptionHandlerFlag(false);
			//emailer.sendEmail(getBody(), getSubject(), getToEmail(), getEmailToBcc(), isEmailExceptionHandlerFlag());
			awsSESEmailService.sendEmail(getBody(), getSubject(), getToEmail(), getEmailToBcc());
		} catch (Exception exception) {
			logger.error("Exception", exception);
		}
	}

	// Preparing Email for Demo EMandate Sign
	public String eMandateSign(Fields requestField) throws Exception {
		String response = null;
		try {
			body = emailBodyCreator.eMandateSignBody(requestField,
					userDao.getBusinessNameByPayId(requestField.get(FieldType.PAY_ID.getName())));
			subject = "E-Mandate Sign";
			toEmail = requestField.get("CUST_EMAIL");
			setEmailExceptionHandlerFlag(false);
			/*
			 * response = emailer.sendEmail(getBody(), getSubject(), getToEmail(),
			 * getEmailToBcc(), isEmailExceptionHandlerFlag());
			 */
			response = awsSESEmailService.sendEmail(getBody(), getSubject(), getToEmail(), getEmailToBcc());
		} catch (Exception exception) {
			logger.error("Exception", exception);
		}
		return response;
	}

	// Preparing Email for Demo Upi AutoPay Mandate Sign
	public String upiAutoPayMandateSign(Fields requestField) throws Exception {
		String response = null;
		try {
			body = emailBodyCreator.upiAutoPayMandateSignBody(requestField,
					userDao.getBusinessNameByPayId(requestField.get(FieldType.PAY_ID.getName())));
			subject = "UPI AutoPay Mandate Sign";
			toEmail = requestField.get("CUST_EMAIL");
			setEmailExceptionHandlerFlag(false);
			/*
			 * response = emailer.sendEmail(getBody(), getSubject(), getToEmail(),
			 * getEmailToBcc(), isEmailExceptionHandlerFlag());
			 */
			response = awsSESEmailService.sendEmail(getBody(), getSubject(), getToEmail(), getEmailToBcc());
		} catch (Exception exception) {
			logger.error("Exception", exception);
		}
		return response;
	}

	

	// Preparing Email for password change to merchant
	public void emailPasswordChange(ResponseObject responseObject, String emailId) throws Exception {
		try {
			body = emailBodyCreator.passwordUpdate();
			subject = EmailerConstants.COMPANY.getValue() + Constants.CRM_PASSWORD_CHANGE_ACKNOWLEDGEMENT.getValue();
			toEmail = emailId;
			setEmailExceptionHandlerFlag(false);
			//emailer.sendEmail(getBody(), getSubject(), getToEmail(), getEmailToBcc(), isEmailExceptionHandlerFlag());
			awsSESEmailService.sendEmail(getBody(), getSubject(), getToEmail(), getEmailToBcc());
		} catch (Exception exception) {
			logger.error("Exception", exception);
		}
	}

	// Preparing Email for password reset to merchant
	public void passwordResetEmail(String accountValidationKey, String emailId) throws Exception {
		try {
			body = emailBodyCreator.passwordReset(accountValidationKey,
					propertiesManager.getResetPasswordProperty(Constants.RESET_PASSWORD_URL.getValue()));
			subject = Constants.RESET_PASSWORD_EMAIL.getValue() + EmailerConstants.COMPANY.getValue();
			toEmail = emailId;
			setEmailExceptionHandlerFlag(true);
			//emailer.sendEmail(getBody(), getSubject(), getToEmail(), getEmailToBcc(), isEmailExceptionHandlerFlag());
			awsSESEmailService.sendEmail(getBody(), getSubject(), getToEmail(), getEmailToBcc());
		} catch (Exception exception) {
			logger.error("Exception", exception);
		}
	}

	// Preparing Email for invoice
	public void invoiceLinkEmail(Invoice invoice) {
		try {
			body = emailBodyCreator.invoiceLink(invoice);
			subject = "Payment Gateway Smart Payment Link -- Invoice ID " + invoice.getInvoiceId();
			toEmail = invoice.getEmail();
			setEmailExceptionHandlerFlag(true);
			try {
				/*
				 * emailer.sendEmail(getBody(), getSubject(), getToEmail(), getEmailToBcc(),
				 * isEmailExceptionHandlerFlag());
				 */
				awsSESEmailService.sendEmail(getBody(), getSubject(), getToEmail(), getEmailToBcc());
			} catch (Exception exception) {
				responseMessage.append(ErrorType.EMAIL_ERROR.getResponseMessage());
				responseMessage.append(invoice.getEmail());
				responseMessage.append("\n");
				logger.debug("Error!! Unable to send email Emailer fail: " + exception);
				return;
			}
		} catch (Exception exception) {
			logger.error("Exception", exception);
		}
	}

	// Preparing Transaction Email
	public void transactionAuthenticationEmail(Map<String, String> fields) throws Exception {
		try {
			body = emailBodyCreator.transactionAuthenticationLink(fields,
					propertiesManager.getEmailProperty(Constants.EMAIL_TRANSACTION_AUTHENTICATION.getValue()),
					fields.get(FieldType.TXN_ID.getName()));
			subject = EmailerConstants.COMPANY.getValue() + Constants.PAYMENT_ATHENTICATION_ACKNOWLEDGEMENT.getValue()
					+ fields.get(FieldType.TXN_ID.getName());
			toEmail = fields.get(FieldType.CUST_EMAIL.getName());
			setEmailExceptionHandlerFlag(false);
			//emailer.sendEmail(getBody(), getSubject(), getToEmail(), getEmailToBcc(), isEmailExceptionHandlerFlag());
			awsSESEmailService.sendEmail(getBody(), getSubject(), getToEmail(), getEmailToBcc());
		} catch (Exception exception) {
			logger.error("Exception", exception);
		}
	}

	// Remittance Email to merchant
	public void remittanceProcessEmail(String utr, String payId, String merchant, String datefrom, String netAmount,
			String remittedDate, String remittedAmount, String status) {
		try {
			User user = userDao.getUserClass(payId);
			body = emailBodyCreator.remittanceEmailBody(utr, payId, merchant, datefrom, netAmount, remittedDate,
					remittedAmount, status);
			subject = EmailerConstants.COMPANY.getValue() + Constants.REMITTANCE_PROCESSED.getValue() + payId;
			toEmail = user.getEmailId();
			setEmailExceptionHandlerFlag(false);
			//emailer.sendEmail(getBody(), getSubject(), getToEmail(), getEmailToBcc(), isEmailExceptionHandlerFlag());
			awsSESEmailService.sendEmail(getBody(), getSubject(), getToEmail(), getEmailToBcc());
		} catch (Exception exception) {
			logger.error("Exception", exception);
		}

	}

	public void sendBulkEmailServiceTaxUpdate(String emailID, String subject, String messageBody) {
		try {
			messageBody = emailBodyCreator.serviceTaxUpdate(messageBody);
			
			/*
			 * emailer.sendEmail(messageBody, subject, Constants.PG_DEMO_EMAIL.getValue(),
			 * emailID, isEmailExceptionHandlerFlag()); // Sending
			 */			 			
			awsSESEmailService.sendEmail(messageBody, subject, Constants.PG_DEMO_EMAIL.getValue(),
					  emailID);

		} catch (Exception exception) {
			logger.error("Exception", exception);
		}
	}

	/*
	 * public void sendPaymentAdvieReport(String emailTo, String subject, String
	 * messageBody, String fileName, Object wb) { try { messageBody =
	 * emailBodyCreator.serviceTaxUpdate(messageBody);
	 * //emailer.sendPaymentAdviseAttachmentFileEmail(messageBody, subject, emailID,
	 * emailID, isEmailExceptionHandlerFlag(), fileName);
	 * emailer.sendEmailWithAttachment(messageBody, subject, messageBody, fileName,
	 * wb, emailTo, fileName);
	 * 
	 * //String recipients, String subject, String body,String attachmentFileName,
	 * SXSSFWorkbook sxssWorkBook, , String mailFrom
	 * 
	 * } catch (Exception exception) { logger.error("Exception", exception); }
	 * 
	 * }
	 */

	public Map<String, String> sendPaymentAdvieReport(Object subMerchantPayId, Object merchantPayId,
			Object sessionUserPayId, Object payoutDate, Object currency) {
		String msg = "success";
		Map<String, String> responseMap = new HashMap<String, String>();
		try {
			Runnable runnable = new Runnable() {

				@Override
				public void run() {
					try {
						merchantPaymentAdviseReportpdfAutoSend.getpdfFileForEmail(String.valueOf(subMerchantPayId),
								String.valueOf(merchantPayId), String.valueOf(sessionUserPayId),
								String.valueOf(payoutDate), String.valueOf(currency));
					} catch (Exception e) {
						logger.error(
								"Exception in runnable class inside sendPaymentAdvieReport method and EmailBuilder class ",
								e);
					}
				}
			};
			propertiesManager.executorImpl(runnable);
			responseMap.put("response", msg);
		} catch (Exception exception) {
			responseMap.put("response", "Exception");
			logger.error("Exception", exception);
		}
		return responseMap;
	}

	public void sendPendingRequestUpdateEmail(Map<String, String> requestMap) {
		try {
			String emailID = requestMap.get(Constants.EMAIL.getValue());
			String subject = requestMap.get(Constants.SUBJECT.getValue());
			String messageBody = requestMap.get(Constants.MESSAGE_BODY.getValue());

			messageBody = emailBodyCreator.emailPendingRequest(requestMap.get(Constants.MERCHANT.getValue()),
					requestMap.get("MakerName"), requestMap.get("CheckerName"),
					requestMap.get(Constants.CREATE_DATE.getValue()), requestMap.get(Constants.STATUS.getValue()),
					requestMap.get(Constants.USERTYPE.getValue()), messageBody);
			/*
			 * emailer.sendEmail(messageBody, subject, emailID,
			 * Constants.PG_DEMO_EMAIL.getValue(), isEmailExceptionHandlerFlag()); //
			 * Sending
			 */			
			awsSESEmailService.sendEmail(messageBody, subject, emailID, Constants.PG_DEMO_EMAIL.getValue());

		} catch (Exception exception) {
			logger.error("Exception", exception);
		}
	}

	public void sendMisReport(String emailID, String subject, String fileName) {
		try {
			//emailer.sendAttachmentFileEmail(fileName, subject, "", emailID, isEmailExceptionHandlerFlag());
			awsSESEmailService.sendAttachmentFileEmail(fileName, subject, "", emailID, isEmailExceptionHandlerFlag());
		} catch (Exception exception) {
			logger.error("Exception", exception);
		}

	}

	public void sendMPAEmail(MPAStatusEmail mpaStatusEmail) {

		String[] emailIdArray = mpaStatusEmail.getToEmail().split(",");

		try {
			for (int i = 0; i < emailIdArray.length; i++) {

				/*
				 * emailer.sendEmail(mpaStatusEmail.getMessageBody(),
				 * mpaStatusEmail.getSubject(), emailIdArray[i], "",
				 * isEmailExceptionHandlerFlag()); // Sending
				 */				
				awsSESEmailService.sendEmail(mpaStatusEmail.getMessageBody(), mpaStatusEmail.getSubject(), emailIdArray[i], "");
			}
		} catch (Exception exception) {
			logger.error("Exception", exception);
		}
	}

	public void sendMultipleUserSystemDownEmail(MailersObject mailersObject) {

		List<String> emailIdList = mailersObject.getToEmailList();

		try {
			for (String emailId : emailIdList) {

				/*
				 * emailer.sendEmail(mailersObject.getMessageBody(), mailersObject.getSubject(),
				 * emailId, "", isEmailExceptionHandlerFlag()); // Sending
				 */				
				awsSESEmailService.sendEmail(mailersObject.getMessageBody(), mailersObject.getSubject(), emailId, "");
			}
		} catch (Exception exception) {
			logger.error("Exception", exception);
		}
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getToEmail() {
		return toEmail;
	}

	public void setToEmail(String toEmail) {
		this.toEmail = toEmail;
	}

	public boolean isEmailExceptionHandlerFlag() {
		return emailExceptionHandlerFlag;
	}

	public void setEmailExceptionHandlerFlag(boolean emailExceptionHandlerFlag) {
		this.emailExceptionHandlerFlag = emailExceptionHandlerFlag;

	}

	public String getEmailToBcc() {
		return emailToBcc;
	}

	public void setEmailToBcc(String mailBcc) {
		this.emailToBcc = mailBcc;
	}

	public StringBuilder getResponseMessage() {
		return responseMessage;
	}

	public void setResponseMessage(StringBuilder responseMessage) {
		this.responseMessage = responseMessage;
	}
}
