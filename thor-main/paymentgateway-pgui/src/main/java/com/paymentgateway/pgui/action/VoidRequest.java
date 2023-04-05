package com.paymentgateway.pgui.action;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.api.TransactionControllerServiceProvider;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.util.Amount;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.TransactionType;

/**
 * @author Puneet
 *
 */
public class VoidRequest extends AbstractSecureAction {

	@Autowired
	TransactionControllerServiceProvider transactionControllerServiceProvider;
	
	private static final long serialVersionUID = 6234428663640109964L;

	private static Logger logger = LoggerFactory.getLogger(VoidRequest.class
			.getName());

	private String origTxnId;
	private String amount;
	private String payId;
	private String currencyCode;
	private String response;
	private String orderId;
	private Date txnDate;

	public String execute() {
		try {
			Fields responseMap = null;
			Map<String, String> requestMap = new HashMap<String, String>();
			
			if(!DateUtils.isSameDay(new Date(), txnDate)){
				setResponse(ErrorType.VOID_NOT_SUCCESSFULL.getResponseMessage()
						+ getOrderId());
				return SUCCESS;
			}

			// format amount first
			requestMap.put(FieldType.AMOUNT.getName(),
					Amount.formatAmount(amount, currencyCode));

			requestMap.put(FieldType.ORIG_TXN_ID.getName(), origTxnId);
			requestMap.put(FieldType.PAY_ID.getName(), payId);
			requestMap.put(FieldType.TXNTYPE.getName(),
					TransactionType.CAPTURE.getName());
			requestMap.put(FieldType.CURRENCY_CODE.getName(),
					currencyCode);
			requestMap
					.put(FieldType.HASH.getName(),
							"1234567890123456789012345678901234567890123456789012345678901234"); 
																									
			requestMap
			.put(FieldType.INTERNAL_VALIDATE_HASH_YN.getName(),
					"N"); 
																							
			// Preparing fields and capture transaction
			Fields fields = new Fields(requestMap);

			fields.logAllFields("All capture request fields :");
			
			Map<String, String> response = transactionControllerServiceProvider.transact(fields, Constants.TXN_WS_INTERNAL.getValue());
			responseMap = new Fields(response);
			String responseCode = responseMap.get(FieldType.RESPONSE_CODE
					.getName());

			if (null == responseCode
					|| !responseCode.equals(ErrorType.SUCCESS.getCode())) {
				setResponse(ErrorType.VOID_NOT_SUCCESSFULL.getResponseMessage()
						+ getOrderId());
				return SUCCESS;
			}

			//Refund the transaction to make it void
			requestMap.put(FieldType.TXNTYPE.getName(),
					TransactionType.REFUND.getName());
			// Preparing fields
			Fields fieldsVoid = new Fields(requestMap);

			fieldsVoid.logAllFields("All void request fields :");
			
			Fields responseMapVoid = new Fields(transactionControllerServiceProvider.transact(fields, Constants.TXN_WS_INTERNAL.getValue()));
			logger.info("inside void Request , fieldsVoid values are "+ responseMapVoid.getFieldsAsString());
			String responseCodeVoid = responseMapVoid.get(FieldType.RESPONSE_CODE
								.getName());
			
			if (null == responseCodeVoid
					|| !responseCodeVoid.equals(ErrorType.SUCCESS.getCode())) {
				setResponse(ErrorType.VOID_NOT_SUCCESSFULL.getResponseMessage()
						+ getOrderId());
				return SUCCESS;
			}
			
			setResponse(ErrorType.VOID_SUCCESSFULL.getResponseMessage()
					+ getOrderId());
			return SUCCESS;
		} catch (Exception exception) {
			logger.error("Exception", exception);
			setResponse(ErrorType.VOID_NOT_SUCCESSFULL.getResponseMessage()
					+ getOrderId());
			return SUCCESS;
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

	public Date getTxnDate() {
		return txnDate;
	}

	public void setTxnDate(Date txnDate) {
		this.txnDate = txnDate;
	}

}
