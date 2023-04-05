package com.paymentgateway.crm.action;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.api.TransactionControllerServiceProvider;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;

/**
 * @author Rajit
 */
public class UpiAutoPayTransactionScheduleAction extends AbstractSecureAction {

	private static final long serialVersionUID = 2569791896570261919L;
	
	@Autowired
	private TransactionControllerServiceProvider transactionControllerServiceProvider;
	
	private static Logger logger = LoggerFactory.getLogger(UpiAutoPayTransactionScheduleAction.class.getName());
	
	private String orderId;
	private String merchantPayId;
	private String subMerchantPayId;
	private String toDate;
	private String fromDate;
	private String pgRefNumber;
	private String umnNumber;
	private String status;
	private String responseCode;
	private String response;
	
	public String payNowDebitTransaction() {
		
		logger.info("Inside UpiAutoPayTransactionScheduleAction payNowDebitTransaction function ");
		try {
		
			Fields fields = new Fields();
			
			fields.put(FieldType.ORDER_ID.getName(), orderId);
			fields.put(FieldType.PAY_ID.getName(), merchantPayId);
			fields.put(FieldType.PG_REF_NUM.getName(), pgRefNumber);
			Map<String, String> res = transactionControllerServiceProvider.transact(fields, Constants.UPI_AUTOPAY_DEBIT_TRANSACTION_URL.getValue());
			
			responseCode = res.get(FieldType.STATUS.getName());
			response = res.get(FieldType.RESPONSE_MESSAGE.getName());
			logger.info("debit API response received from pg ws "+response);
			if(response.isEmpty()) {
				logger.info("debit transaction not initiated !!");
			}
		
		} catch(Exception ex) {
			logger.error("caught exception while schedule debit transaction : ",ex);
		}
		return SUCCESS;
	}
	
	public String NotifyDebitTransaction() {
		
		logger.info("Inside UpiAutoPayTransactionScheduleAction NotifyDebitTransaction function ");
		try {
			
			Fields fields = new Fields();
			
			fields.put(FieldType.ORDER_ID.getName(), orderId);
			fields.put(FieldType.PAY_ID.getName(), merchantPayId);
			fields.put(FieldType.PG_REF_NUM.getName(), pgRefNumber);
			
			Map<String, String> res = transactionControllerServiceProvider.transact(fields, Constants.UPI_AUTOPAY_DEBIT_NOTIFICATION_URL.getValue());
			
			responseCode = res.get(FieldType.STATUS.getName());
			response = res.get(FieldType.RESPONSE_MESSAGE.getName());
			logger.info("debit transaction notification API response received from pg ws "+response);
			if(response.isEmpty()) {
				logger.info("debit transaction notification not send");
			} else {
				//logger.info("registration status enquiry Successfully !!, Response Status is :" + response.get(FieldType.STATUS.getName()));
			}
		
		} catch(Exception ex) {
			logger.error("caught exception while notify debit transaction : ",ex);
		}
		
		
		return SUCCESS;
	}
	
	
	public String getOrderId() {
		return orderId;
	}
	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}
	public String getMerchantPayId() {
		return merchantPayId;
	}
	public void setMerchantPayId(String merchantPayId) {
		this.merchantPayId = merchantPayId;
	}
	public String getSubMerchantPayId() {
		return subMerchantPayId;
	}
	public void setSubMerchantPayId(String subMerchantPayId) {
		this.subMerchantPayId = subMerchantPayId;
	}
	public String getToDate() {
		return toDate;
	}
	public void setToDate(String toDate) {
		this.toDate = toDate;
	}
	public String getFromDate() {
		return fromDate;
	}
	public void setFromDate(String fromDate) {
		this.fromDate = fromDate;
	}
	public String getPgRefNumber() {
		return pgRefNumber;
	}
	public void setPgRefNumber(String pgRefNumber) {
		this.pgRefNumber = pgRefNumber;
	}
	public String getUmnNumber() {
		return umnNumber;
	}
	public void setUmnNumber(String umnNumber) {
		this.umnNumber = umnNumber;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getResponseCode() {
		return responseCode;
	}
	public void setResponseCode(String responseCode) {
		this.responseCode = responseCode;
	}
	public String getResponse() {
		return response;
	}
	public void setResponse(String response) {
		this.response = response;
	}
}
