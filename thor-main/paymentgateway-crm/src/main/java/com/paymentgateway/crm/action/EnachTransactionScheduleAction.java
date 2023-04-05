package com.paymentgateway.crm.action;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.api.TransactionControllerServiceProvider;
import com.paymentgateway.commons.user.Enach;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.crm.mongoReports.TxnReports;

/**
 * @author Rajit
 */
public class EnachTransactionScheduleAction  extends AbstractSecureAction {

	@Autowired
	private TxnReports txnReport;
	
	@Autowired
	private TransactionControllerServiceProvider transactionControllerServiceProvider;
	private static final long serialVersionUID = 8890788498597278336L;

private static Logger logger = LoggerFactory.getLogger(EnachTransactionScheduleAction.class.getName());
	
	private String orderId;
	private String merchantPayId;
	private String subMerchantPayId;
	private String toDate;
	private String fromDate;
	private String pgRefNumber;
	private String umrnNumber;
	private String status;
	private String responseCode;
	private String response;
	List<Enach> aaData = new ArrayList<Enach>();
	public String execute() {
		
		logger.info("Inside EnachTransactionScheduleAction execute function ");
	try {
		
		Fields fields = new Fields();
		
		fields.put(FieldType.ORDER_ID.getName(), orderId);
		fields.put(FieldType.PAY_ID.getName(), merchantPayId);
		fields.put(FieldType.PG_REF_NUM.getName(), pgRefNumber);
		
		fields = txnReport.getEnachRegistrationDetails(orderId, merchantPayId, pgRefNumber);
		
		Map<String, String> res = transactionControllerServiceProvider.transact(fields, Constants.ICICI_ENACH_TRANSACTION_SCHEDULE.getValue());
		
		responseCode = res.get(FieldType.RESPONSE_CODE.getName());
		response = res.get(FieldType.RESPONSE_MESSAGE.getName());
		logger.info("debit API response received from pg ws "+response);
		if(response.isEmpty()) {
			logger.info("debit transaction not initiated !!");
		} else {
			//logger.info("registration status enquiry Successfully !!, Response Status is :" + response.get(FieldType.STATUS.getName()));
		}
		
	} catch(Exception ex) {
		logger.error("caught exception while schedule debit transaction : ",ex);
	}
		
		return SUCCESS;
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

	public String getUmrnNumber() {
		return umrnNumber;
	}

	public void setUmrnNumber(String umrnNumber) {
		this.umrnNumber = umrnNumber;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
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
	public String getResponseCode() {
		return responseCode;
	}

	public void setResponseCode(String responseCode) {
		this.responseCode = responseCode;
	}
}
