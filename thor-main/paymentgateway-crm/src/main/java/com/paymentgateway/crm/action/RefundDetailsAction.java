package com.paymentgateway.crm.action;

import java.util.List;

import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.mongo.MongoInstance;
import com.paymentgateway.commons.user.TransactionHistory;
import com.paymentgateway.commons.util.CrmFieldType;
import com.paymentgateway.commons.util.CrmValidator;
import com.paymentgateway.commons.util.MopType;
import com.paymentgateway.commons.util.PaymentType;
import com.paymentgateway.crm.actionBeans.RefundDetailsProvider;


public class RefundDetailsAction extends AbstractSecureAction{
	
	@Autowired
	private CrmValidator validator;
	@Autowired
	private MongoInstance mongoInstance;
	@Autowired
	private RefundDetailsProvider refundDetailsProvider;
	
	private static Logger logger = LoggerFactory.getLogger(RefundDetailsAction.class.getName());
	private static final long serialVersionUID = -1573507922725429926L;

	private List<TransactionHistory> oldTransactions;
	private TransactionHistory transDetails = new TransactionHistory();

	private String transactionId;
	private String orderId;
	private String payId;
			
	public String execute() {
		try{
		getTransactionDetails();	
		return SUCCESS;
		}
		catch(Exception exception){
			logger.error("Exception", exception);
			return ERROR;
		}
	}

	private void getTransactionDetails() throws SystemException{
		List<TransactionHistory> refundD = refundDetailsProvider.RefundProvider(orderId, payId, transactionId);
		refundDetailsProvider.getAllTransactions();
		transDetails = refundDetailsProvider.getTransDetails();
		setOldTransactions(refundDetailsProvider.getOldTransactions());
		transDetails.setMopType(MopType.getmopName(transDetails.getMopType()));
		transDetails.setPaymentType(PaymentType.getpaymentName(transDetails.getPaymentType()));
	}
	
	public void validate(){
		
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

		if(validator.validateBlankField(getTransactionId())){
		}
		else if(!validator.validateField(CrmFieldType.TRANSACTION_ID, getTransactionId())){
			addFieldError(CrmFieldType.TRANSACTION_ID.getName(), ErrorType.INVALID_FIELD.getResponseMessage());
		}
	}

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	public String getPayId() {
		return payId;
	}

	public void setPayId(String payId) {
		this.payId = payId;
	}

	public TransactionHistory getTransDetails() {
		return transDetails;
	}

	public void setTransDetails(TransactionHistory transDetails) {
		this.transDetails = transDetails;
	}

	public String getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}

	public List<TransactionHistory> getOldTransactions() {
		return oldTransactions;
	}

	public void setOldTransactions(List<TransactionHistory> oldTransactions) {
		this.oldTransactions = oldTransactions;
	}
}
