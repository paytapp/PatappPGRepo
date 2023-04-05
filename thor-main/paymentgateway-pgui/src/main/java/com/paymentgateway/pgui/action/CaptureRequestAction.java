package com.paymentgateway.pgui.action;
/*package com.paymentgateway.pg.action;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.paymentgateway.commons.dao.CaptureTransactionService;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.util.Amount;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CrmFieldConstants;
import com.paymentgateway.commons.util.CrmFieldType;
import com.paymentgateway.commons.util.CrmValidator;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.TransactionType;

*//**
 * @author Chandan,Puneet
 *
 *//*
public class CaptureRequestAction extends AbstractSecureAction {
	@Autowired
	private CrmValidator validator;
	private static final long serialVersionUID = -5628640988767585608L;
	private static Logger logger = LoggerFactory.getLogger(CaptureRequestAction.class.getName());
	private String origTxnId;
	private String amount;
	private String payId;
	private String orderId;
	private String response;
	private String currencyCode;
	private String mopType;
	
	public String execute() {
		Fields responseMap = null;
		try {
			String capturedTxnId = confirmIfCaptured();
			if (capturedTxnId != null) {
				setResponse(ErrorType.ALREADY_CAPTURED_TRANSACTION.getResponseMessage() + capturedTxnId);
				return SUCCESS;
			}
			Map<String, String> requestMap = new HashMap<String, String>();

			// Put parameters in request map
			requestMap.put(FieldType.AMOUNT.getName(),
					Amount.formatAmount(amount, currencyCode));
			requestMap.put(FieldType.ORIG_TXN_ID.getName(), origTxnId);
			User sessionUser = (User) sessionMap.get(Constants.USER.getValue());
		//	requestMap.put(FieldType.PAY_ID.getName(), SessionUserIdentifier.getUserPayId(sessionUser, payId)); TODO
			requestMap.put(FieldType.TXNTYPE.getName(), TransactionType.CAPTURE.getName());
			requestMap.put(FieldType.CURRENCY_CODE.getName(), currencyCode);
			requestMap.put(FieldType.HASH.getName(), "1234567890123456789012345678901234567890123456789012345678901234"); 
			requestMap.put(FieldType.INTERNAL_VALIDATE_HASH_YN.getName(), "N"); 

			// Preparing fields
			Fields fields = new Fields(requestMap);
			fields.logAllFields("All request fields :");
			//RequestRouter router = new RequestRouter(fields); TODO
			
			// Process request
		//	responseMap = new Fields(router.route()); TODO
			
			//send sms
		//	SmsSender.sendSMS(fields); TODO

			String responseCode = responseMap.get(FieldType.RESPONSE_CODE.getName());
			if (null == responseCode
					|| !responseCode.equals(ErrorType.SUCCESS.getCode())) {
				setResponse(ErrorType.CAPTURE_NOT_SUCCESSFULL
						.getResponseMessage() + getOrderId());
				return SUCCESS;
			}
		} catch (Exception exception) {
			logger.error("Exception", exception);
			setResponse(ErrorType.CAPTURE_NOT_SUCCESSFULL.getResponseMessage()
					+ getOrderId());
			return SUCCESS;
		}
		setResponse(ErrorType.CAPTURE_SUCCESSFULL.getResponseMessage()
				+ getOrderId());
		return SUCCESS;
	}

	public void validate(){
		
		if(validator.validateBlankField(getOrigTxnId())){
		}
		else if(!validator.validateField(CrmFieldType.TRANSACTION_ID, getOrigTxnId())){
			addFieldError(CrmFieldConstants.ORIG_TXN_ID.getValue(), ErrorType.INVALID_FIELD.getResponseMessage());
		}

        if(validator.validateBlankField(getAmount())){
		}
        else if(!validator.validateField(CrmFieldType.AMOUNT, getAmount())){
        	addFieldError(CrmFieldType.CURRENCY.getName(), ErrorType.INVALID_FIELD.getResponseMessage());
		}

        if(validator.validateBlankField(getPayId())){
        }
        else if(!validator.validateField(CrmFieldType.PAY_ID, getPayId())){
        	addFieldError(CrmFieldType.PAY_ID.getName(), ErrorType.INVALID_FIELD.getResponseMessage());
		}

        if(validator.validateBlankField(getOrderId())){
        }
        else if(!validator.validateField(CrmFieldType.ORDER_ID, getOrderId())){
        	addFieldError(CrmFieldType.ORDER_ID.getName(), ErrorType.INVALID_FIELD.getResponseMessage());
		}

        if(validator.validateBlankField(getResponse())){
		}
        else if(!validator.validateField(CrmFieldType.SUCCESS_MESSAGE, getResponse())){
        	addFieldError(CrmFieldConstants.RESPONSE.getValue(), ErrorType.INVALID_FIELD.getResponseMessage());
		}

		if(validator.validateBlankField(getCurrencyCode())){
		}
		else if(!validator.validateField(CrmFieldType.CURRENCY, getCurrencyCode())){
        	addFieldError(CrmFieldConstants.CURRENCY_CODE.getValue(), ErrorType.INVALID_FIELD.getResponseMessage());
		}
    }

	private String confirmIfCaptured() throws SystemException {
		CaptureTransactionService captureService = new CaptureTransactionService();
		return captureService.getCapturedTransaction(origTxnId, payId);
	}

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
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

	public String getMopType() {
		return mopType;
	}

	public void setMopType(String mopType) {
		this.mopType = mopType;
	}
}
*/