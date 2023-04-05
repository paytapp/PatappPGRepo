package com.paymentgateway.crm.chargeback;

import java.math.BigDecimal;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.user.ChargingDetailsDao;
import com.paymentgateway.commons.user.TransactionHistory;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.MopType;
import com.paymentgateway.commons.util.PaymentType;
import com.paymentgateway.crm.action.AbstractSecureAction;
import com.paymentgateway.crm.actionBeans.RefundDetailsProvider;

public class GenerateChargebackAction extends AbstractSecureAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3461692497780002314L;


	@Autowired
	private RefundDetailsProvider refundDetailsProvider;
	
	@Autowired
	private UserDao userDao;
	
	@Autowired
	private ChargingDetailsDao chargingDetailsDao;


	private static Logger logger = LoggerFactory.getLogger(GenerateChargebackAction.class.getName());
	private TransactionHistory transDetails = new TransactionHistory();
	
	private String txnId;
	private String orderId;
	private String payId;
	private String pgRefNum;
	private String businessName;
	private String subMerchantName;
	private BigDecimal refundedAmount;
	private BigDecimal refundAvailable;
	
	
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
		
		List<TransactionHistory> refundD = refundDetailsProvider.RefundProvider(pgRefNum, payId, txnId);
		refundDetailsProvider.getAllTransactions();
		transDetails = refundDetailsProvider.getTransDetails();
		transDetails.setMopType(MopType.getmopName(transDetails.getMopType()));
		transDetails.setPaymentType(PaymentType.getpaymentName(transDetails.getPaymentType()));
		transDetails.setRefundedAmount(refundedAmount);
		transDetails.setRefundAvailable(refundAvailable);
		transDetails.setPgRefNum(pgRefNum);
		transDetails.setBusinessName(businessName);
		transDetails.setSubMerchantName(subMerchantName);
		transDetails.setTxnId(txnId);
		
		User user=userDao.findPayId(transDetails.getPayId());
		setBusinessName(user.getBusinessName());
		if(transDetails.getSubMerchantPayId() != null) {
			setSubMerchantName(userDao.getBusinessNameByPayId(transDetails.getSubMerchantPayId()));
		}else {
			setSubMerchantName(null);
		}
		
				
	}
	
	public String getBusinessName() {
		return businessName;
	}
	public void setBusinessName(String businessName) {
		this.businessName = businessName;
	}
	public TransactionHistory getTransDetails() {
		return transDetails;
	}
	public void setTransDetails(TransactionHistory transDetails) {
		this.transDetails = transDetails;
	}
	public String getTxnId() {
		return txnId;
	}
	public void setTxnId(String txnId) {
		this.txnId = txnId;
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
	public BigDecimal getRefundedAmount() {
		return refundedAmount;
	}
	public void setRefundedAmount(BigDecimal refundedAmount) {
		this.refundedAmount = refundedAmount;
	}
	public BigDecimal getRefundAvailable() {
		return refundAvailable;
	}
	public void setRefundAvailable(BigDecimal refundAvailable) {
		this.refundAvailable = refundAvailable;
	}
	public String getPgRefNum() {
		return pgRefNum;
	}
	public void setPgRefNum(String pgRefNum) {
		this.pgRefNum = pgRefNum;
	}
	public String getSubMerchantName() {
		return subMerchantName;
	}
	public void setSubMerchantName(String subMerchantName) {
		this.subMerchantName = subMerchantName;
	}
	

}
