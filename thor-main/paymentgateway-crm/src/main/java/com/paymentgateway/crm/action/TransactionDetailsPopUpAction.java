package com.paymentgateway.crm.action;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.dao.TransactionDetailsPopUpDao;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.user.TransactionDetails;
import com.paymentgateway.commons.user.TransactionSearch;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CrmFieldType;
import com.paymentgateway.commons.util.CrmValidator;

public class TransactionDetailsPopUpAction extends AbstractSecureAction{

	@Autowired
	CrmValidator validator;
	
	@Autowired
	private TransactionDetailsPopUpDao transactionDetailsPopUpDao;
	
	private String orderId;
	private String txnType;
	private String txnId;
	
	private TransactionDetails aaData;
	private List<TransactionDetails> data;
	
	private static final long serialVersionUID = -89456281736579560L;
	
	private static Logger logger = LoggerFactory.getLogger(TransactionDetailsPopUpAction.class.getName());
	
	User sessionUser ;
	
	public String execute() {
		try {
			sessionUser = (User) sessionMap.get(Constants.USER.getValue());
			logger.info("inside TransactionDetailsPopUpAction ");
			setAaData(transactionDetailsPopUpDao.getAllDetail(orderId, txnType, sessionUser.getUserType()));
			setData(transactionDetailsPopUpDao.getDataByOID(getTxnId(),sessionUser));
		} catch (Exception ex) {
			logger.error("Caught exception in TransactionDetailsPopUpAction : ", ex);
			return ERROR;
		}
		return SUCCESS;
	}
	
	public void validator() {
		
		if (validator.validateBlankField(getOrderId())) {
		} else if (!validator.validateField(CrmFieldType.ORDER_ID, getOrderId())) {
			addFieldError(CrmFieldType.ORDER_ID.getName(), ErrorType.INVALID_FIELD.getResponseMessage());
		}
		
		if (validator.validateBlankField(getTxnType())) {
		} else if (!validator.validateField(CrmFieldType.TXNTYPE, getTxnType())) {
			addFieldError(CrmFieldType.ORDER_ID.getName(), ErrorType.INVALID_FIELD.getResponseMessage());
		}
	}	
	
	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}
	
	public String getTxnType() {
		return txnType;
	}

	public void setTxnType(String txnType) {
		this.txnType = txnType;
	}
	public TransactionDetails getAaData() {
		return aaData;
	}

	public void setAaData(TransactionDetails aaData) {
		this.aaData = aaData;
	}
	
	public String getTxnId() {
		return txnId;
	}

	public void setTxnId(String txnId) {
		this.txnId = txnId;
	}

	public List<TransactionDetails> getData() {
		return data;
	}

	public void setData(List<TransactionDetails> data) {
		this.data = data;
	}
	
	
}
