package com.paymentgateway.crm.action;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.api.Hasher;
import com.paymentgateway.commons.api.TransactionControllerServiceProvider;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.user.Merchants;
import com.paymentgateway.commons.user.RefundRejection;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.util.Amount;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.DateCreater;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.TransactionManager;
import com.paymentgateway.commons.util.TransactionType;
import com.paymentgateway.crm.mongoReports.TxnReports;

public class RefundRejectionReportAction extends AbstractSecureAction {

	private static final long serialVersionUID = 149102749485831816L;

	private static Logger logger = LoggerFactory.getLogger(RefundRejectionReportAction.class.getName());

	private List<Merchants> merchantList = new LinkedList<Merchants>();
	private String orderId;
	private String merchant;
	private String paymentType;
	private String dateFrom;
	private String dateTo;
	private String acquirer;
	private String response;
	
	private String refundFlag;
	private String refundAmount;
	private String pgRefNum;
	private String refundDate;
	private String totalAmount;
	private String refundOrderId;
	private String currencyCode;
	
	private int count = 1;
	private User sessionUser = new User();
	List<RefundRejection> aaData = new ArrayList<RefundRejection>();

	@Autowired
	private TransactionControllerServiceProvider transactionControllerServiceProvider;
	
	@Autowired
	private TxnReports txnReports;

	public String execute() {
		try {
			sessionUser = (User) sessionMap.get(Constants.USER.getValue());
			setDateFrom(DateCreater.toDateTimeformatCreater(dateFrom));
			setDateTo(DateCreater.formDateTimeformatCreater(dateTo));
			setAaData(txnReports.searchRejectedRefund(getMerchant(), getOrderId(), getRefundOrderId(), getPaymentType(), getAcquirer(), dateFrom, dateTo));
			return SUCCESS;

		} catch (Exception exception) {
			logger.error("Exception in RefundRejectionReportAction Class, execute method ", exception);
		}

		return SUCCESS;
	}

	public String refund() {
		try {
			logger.info("RefundRejectionReportAction Class, refund method !!");
			//String hostUrl = PropertiesManager.propertiesMap.get("RefundURL");
			logger.info("Order Id : " + getOrderId());
			logger.info("Refund Flag : " + getRefundFlag());
			logger.info("Refund Amount : " + getRefundAmount());
			logger.info("Currency Code : " + getCurrencyCode());
			logger.info("PG_REF_NUM : " + getPgRefNum());
			logger.info("Refund Order Id : " + getRefundOrderId());
			logger.info("Merchant : " + getMerchant());
			
			Fields fields = new Fields();
			fields.put(FieldType.ORDER_ID.getName(),getOrderId());
			fields.put(FieldType.REFUND_FLAG.getName(),getRefundFlag());
			fields.put(FieldType.AMOUNT.getName(),(Amount.formatAmount(getRefundAmount(), getCurrencyCode())));
			fields.put(FieldType.PG_REF_NUM.getName(),getPgRefNum());
			fields.put(FieldType.REFUND_ORDER_ID.getName(),getRefundOrderId());
			fields.put(FieldType.CURRENCY_CODE.getName(),getCurrencyCode());
			fields.put(FieldType.TXNTYPE.getName(),TransactionType.REFUND.getName());
			fields.put(FieldType.PAY_ID.getName(),getMerchant());
			fields.put(FieldType.REQUEST_DATE.getName(),getRefundDate());
			fields.put(FieldType.HASH.getName(),Hasher.getHash(TransactionManager.getNewTransactionId()));
			logger.info("refund request : " + fields.getFieldsAsString());
			
			Map<String, String> response = transactionControllerServiceProvider.transact(fields, "RefundURL");
			logger.info("refund API  response received from pg ws " + response);
			if(response.isEmpty()) {
				setResponse("Refund not initiated !!");
			} else {
				setResponse("Refund Initiated Successfully !!, Response Status is :" + response.get(FieldType.STATUS.getName()));
			}
			
		} catch (JSONException exception) {			
			logger.error("Exception in RefundRejectionReportAction Class, refund function ", exception);
		} catch (SystemException exception) {
			logger.error("Exception in RefundRejectionReportAction Class, refund method ", exception);
		}
		return SUCCESS;
	}
	public String getDateFrom() {
		return dateFrom;
	}

	public void setDateFrom(String dateFrom) {
		this.dateFrom = dateFrom;
	}

	public String getDateTo() {
		return dateTo;
	}

	public void setDateTo(String dateTo) {
		this.dateTo = dateTo;
	}

	public String getPaymentType() {
		return paymentType;
	}

	public void setPaymentType(String paymentType) {
		this.paymentType = paymentType;
	}

	public String getAcquirer() {
		return acquirer;
	}

	public void setAcquirer(String acquirer) {
		this.acquirer = acquirer;
	}

	public String getMerchant() {
		return merchant;
	}

	public void setMerchant(String merchant) {
		this.merchant = merchant;
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

	public String getRefundFlag() {
		return refundFlag;
	}

	public void setRefundFlag(String refundFlag) {
		this.refundFlag = refundFlag;
	}

	public String getRefundAmount() {
		return refundAmount;
	}

	public void setRefundAmount(String refundAmount) {
		this.refundAmount = refundAmount;
	}

	public String getPgRefNum() {
		return pgRefNum;
	}

	public void setPgRefNum(String pgRefNum) {
		this.pgRefNum = pgRefNum;
	}

	public String getRefundDate() {
		return refundDate;
	}

	public void setRefundDate(String refundDate) {
		this.refundDate = refundDate;
	}

	public String getTotalAmount() {
		return totalAmount;
	}

	public void setTotalAmount(String totalAmount) {
		this.totalAmount = totalAmount;
	}

	public String getRefundOrderId() {
		return refundOrderId;
	}

	public void setRefundOrderId(String refundOrderId) {
		this.refundOrderId = refundOrderId;
	}
	
	public String getCurrencyCode() {
		return currencyCode;
	}

	public void setCurrencyCode(String currencyCode) {
		this.currencyCode = currencyCode;
	}

	public List<RefundRejection> getAaData() {
		return aaData;
	}

	public void setAaData(List<RefundRejection> aaData) {
		this.aaData = aaData;
	}

}
