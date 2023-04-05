package com.paymentgateway.pgui.action;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts2.ServletActionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.Amount;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CrmFieldConstants;
import com.paymentgateway.commons.util.CrmFieldType;
import com.paymentgateway.commons.util.CrmValidator;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.TransactionType;

/**
 * @author Chandan
 *
 */
public class RefundRequest extends AbstractSecureAction {

	private static Logger logger = LoggerFactory.getLogger(RefundRequest.class.getName());
	private static final long serialVersionUID = -1955264811888217325L;
	private HttpServletRequest request;
	private String origTxnId;
	private String amount;
	private String payId;
	private String currencyCode;
	private String response;
	private String orderId;
	private String custEmail;
	private boolean refundFlag;
	private boolean emailExceptionHandlerFlag;
	@Autowired
	private UserDao userDao;
	/*
	 * @Autowired private SendTransactionEmail sendTransactionEmail;
	 */
	/*@Autowired  TODO
	private EmailBodyCreator emailBodyCreator;*/
	/*@Autowired  TODO
	private RefundLimitUpdater refundLimitUpdater;*/
	@Autowired
	private CrmValidator validator;
	
	public String execute() {
		try {

			HttpServletRequest request = ServletActionContext.getRequest();
			;
			User sessionUser = (User) sessionMap.get(Constants.USER.getValue());
			Fields responseMap = null;
			Map<String, String> requestMap = new HashMap<String, String>();
			// format amount first
			requestMap.put(FieldType.AMOUNT.getName(), Amount.formatAmount(getAmount(), getCurrencyCode()));
			requestMap.put(FieldType.PAY_ID.getName(), getPayId());
			
			User user = userDao.findPayId(payId);
			// Check Refund Amount
		//	RefundChecker refundChecker = new RefundChecker();  TODO
		//	refundFlag = refundChecker.setAllRefundValidation(currencyCode, requestMap, user, getOrigTxnId());
			if (refundFlag == true) {
				setResponse(ErrorType.REFUND_FAILED.getResponseMessage() + getOrderId());
				return SUCCESS;
			}
			requestMap.put(FieldType.ORIG_TXN_ID.getName(), getOrigTxnId());
			requestMap.put(FieldType.PAY_ID.getName(), getPayId());
			requestMap.put(FieldType.TXNTYPE.getName(), TransactionType.REFUND.getName());
			requestMap.put(FieldType.CURRENCY_CODE.getName(), currencyCode);
			requestMap.put(FieldType.CUST_EMAIL.getName(), getCustEmail());
			requestMap.put(FieldType.INTERNAL_USER_EMAIL.getName(), sessionUser.getEmailId());
			requestMap.put(FieldType.HASH.getName(),
					"1234567890123456789012345678901234567890123456789012345678901234");
			requestMap.put(FieldType.INTERNAL_VALIDATE_HASH_YN.getName(), "N");
			// Preparing fields
			Fields fields = new Fields(requestMap);
			fields.put((FieldType.INTERNAL_CUST_IP.getName()), request.getRemoteAddr());
			fields.logAllFields("All request fields :");
		//	RequestRouter router = new RequestRouter(fields); TODO
		//	responseMap = new Fields(router.route());

			String responseCode = responseMap.get(FieldType.RESPONSE_CODE.getName());
			// send SMS
		//	SmsSender.sendSMS(fields); TODO

			// Sending Email for Transaction Status to merchant or customer
			if (responseCode.equals("000")) {
	//TODO code review from puneet.......................... 	
	//		sendTransactionEmail.sendEmail(fields);
				
				/*if (user.isRefundTransactionCustomerEmailFlag()) {
					
					emailBuilder.transactionRefundEmail(responseMap, CrmFieldConstants.CUSTOMER.toString(),
							fields.get(FieldType.CUST_EMAIL.getName()), user.getBusinessName());
				}
				if (user.isRefundTransactionMerchantEmailFlag()) {
					String transactionEmailIdString = user.getTransactionEmailId();
					String defaultMerchatEmail = user.getEmailId();
				
					String heading = CrmFieldConstants.MERCHANT_HEADING.getValue();
					String message = user.getBusinessName();
					String body = emailBodyCreator.bodyTransactionEmail(responseMap, heading, message); TODO
					Emailer.sendEmail(body, message, defaultMerchatEmail, transactionEmailIdString,isEmailExceptionHandlerFlag());
				}*/
			}
			if (null == responseCode || !responseCode.equals(ErrorType.SUCCESS.getCode())) {
				setResponse(ErrorType.REFUND_NOT_SUCCESSFULL.getResponseMessage() + getOrderId());
				return SUCCESS;
			}
			// Refund Limit Update
			
			/*refundLimitUpdater.extraRefundLimitUpdate(currencyCode, requestMap, user, getOrigTxnId(), getPayId(),  TODO
					refundChecker.getTodayRefundedAmount(), refundChecker.getTodayTotalCapturedAmount());*/
			setResponse(ErrorType.REFUND_SUCCESSFULL.getResponseMessage() + getOrderId());
			return SUCCESS;
		} catch (Exception exception) {
			logger.error("Exception", exception);
			setResponse(ErrorType.REFUND_NOT_SUCCESSFULL.getResponseMessage() + getOrderId());
			return SUCCESS;
		}
	}

	public void validate() {
		
		if (validator.validateBlankField(getOrigTxnId())) {
		} else if (!validator.validateField(CrmFieldType.TRANSACTION_ID, getOrigTxnId())) {
			addFieldError(CrmFieldConstants.ORIG_TXN_ID.getValue(), ErrorType.INVALID_FIELD.getResponseMessage());
		}

		if (validator.validateBlankField(getAmount())) {
		} else if (!validator.validateField(CrmFieldType.AMOUNT, getAmount())) {
			addFieldError(CrmFieldType.CURRENCY.getName(), ErrorType.INVALID_FIELD.getResponseMessage());
		}

		if (validator.validateBlankField(getPayId())) {
		} else if (!validator.validateField(CrmFieldType.PAY_ID, getPayId())) {
			addFieldError(CrmFieldType.PAY_ID.getName(), ErrorType.INVALID_FIELD.getResponseMessage());
		}

		if (validator.validateBlankField(getOrderId())) {
		} else if (!validator.validateField(CrmFieldType.ORDER_ID, getOrderId())) {
			addFieldError(CrmFieldType.ORDER_ID.getName(), ErrorType.INVALID_FIELD.getResponseMessage());
		}

		if (validator.validateBlankField(getResponse())) {
		} else if (!validator.validateField(CrmFieldType.SUCCESS_MESSAGE, getResponse())) {
			addFieldError(CrmFieldConstants.RESPONSE.getValue(), ErrorType.INVALID_FIELD.getResponseMessage());
		}

		if (validator.validateBlankField(getCurrencyCode())) {
		} else if (!validator.validateField(CrmFieldType.CURRENCY, getCurrencyCode())) {
			addFieldError(CrmFieldConstants.CURRENCY_CODE.getValue(), ErrorType.INVALID_FIELD.getResponseMessage());
		}
	}

	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
	}

	public String getOrigTxnId() {
		return origTxnId;
	}

	public void setOrigTxnId(String origTxnId) {
		this.origTxnId = origTxnId;
	}

	public String getAmount() {
		return amount;
	}

	public void setAmount(String amount) {
		this.amount = amount;
	}

	public String getPayId() {
		return payId;
	}

	public void setPayId(String payId) {
		this.payId = payId;
	}

	public String getCurrencyCode() {
		return currencyCode;
	}

	public void setCurrencyCode(String currencyCode) {
		this.currencyCode = currencyCode;
	}

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	public String getCustEmail() {
		return custEmail;
	}

	public void setCustEmail(String custEmail) {
		this.custEmail = custEmail;
	}

	public HttpServletRequest getRequest() {
		return request;
	}

	public void setRequest(HttpServletRequest request) {
		this.request = request;
	}

	public boolean isEmailExceptionHandlerFlag() {
		return emailExceptionHandlerFlag;
	}

	public void setEmailExceptionHandlerFlag(boolean emailExceptionHandlerFlag) {
		this.emailExceptionHandlerFlag = emailExceptionHandlerFlag;
	}

}
