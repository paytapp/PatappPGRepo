package com.paymentgateway.crm.action;

import java.util.HashMap;
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
public class IciciUpiAutoPayReportAction extends AbstractSecureAction {

	private static final long serialVersionUID = -7895936091772911370L;
	private static Logger logger = LoggerFactory.getLogger(IciciUpiAutoPayReportAction.class.getName());
	
	@Autowired
	private TransactionControllerServiceProvider transactionControllerServiceProvider;
	
	private String pgRefNum;
	
	private Map<String, String> aaData = new HashMap<String, String>();
	
	public String execute() {
		
		
		
		return SUCCESS;
	}

	
	public String debitTransaction() {
		logger.info("inside debitTransaction for debit transaction");
		Fields fields = new Fields();
		try {
			fields.put(FieldType.PG_REF_NUM.getName(), pgRefNum);
			aaData = transactionControllerServiceProvider.transact(fields,
					Constants.UPI_AUTOPAY_DEBIT_TRANSACTION_URL.getValue());
		} catch (Exception ex) {
			logger.info("exception while debit transaction " + ex);
			return ERROR;
		}
		return SUCCESS;
	}
	
	public String notificationTransaction() {
		logger.info("inside notificationTransaction for notification debit transaction");
		Fields fields = new Fields();
		try {
			fields.put(FieldType.PG_REF_NUM.getName(), pgRefNum);
			aaData = transactionControllerServiceProvider.transact(fields,
					Constants.UPI_AUTOPAY_DEBIT_NOTIFICATION_URL.getValue());
		} catch (Exception ex) {
			logger.info("exception while notification for debit transaction " + ex);
			return ERROR;
		}
		return SUCCESS;
	}
	
	public String statusEnquiry() {
		logger.info("inside statusEnquiry for status update");
		Fields fields = new Fields();
		try {
			fields.put(FieldType.PG_REF_NUM.getName(), pgRefNum);
			aaData = transactionControllerServiceProvider.transact(fields,
					Constants.UPI_AUTOPAY_STATUS_ENQUIRY_URL.getValue());
		} catch (Exception ex) {
			logger.info("exception while status enquiry " + ex);
			return ERROR;
		}
		return SUCCESS;
	}
	
	public String statusEnquiryByCriteria() {
		logger.info("inside statusEnquiryByCriteria for status update");
		Fields fields = new Fields();
		try {
			fields.put(FieldType.PG_REF_NUM.getName(), pgRefNum);
			aaData = transactionControllerServiceProvider.transact(fields,
					Constants.UPI_AUTOPAY_STATUS_ENQUIRY_CRITERIA_URL.getValue());
		} catch (Exception ex) {
			logger.info("exception while status enquiry by criteria " + ex);
			return ERROR;
		}
		return SUCCESS;
	}
	
	public String getPgRefNum() {
		return pgRefNum;
	}

	public void setPgRefNum(String pgRefNum) {
		this.pgRefNum = pgRefNum;
	}
	public Map<String, String> getAaData() {
		return aaData;
	}

	public void setAaData(Map<String, String> aaData) {
		this.aaData = aaData;
	}

}
