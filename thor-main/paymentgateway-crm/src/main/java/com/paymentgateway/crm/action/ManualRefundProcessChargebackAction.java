package com.paymentgateway.crm.action;

import java.util.Date;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.paymentgateway.commons.dao.ChargebackDao;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.user.Chargeback;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.Amount;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CurrencyTypes;
import com.paymentgateway.commons.util.DateCreater;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.ManualRefundProcess;
import com.paymentgateway.commons.util.TransactionManager;
import com.paymentgateway.crm.actionBeans.RefundCommunicator;
import com.paymentgateway.crm.mongoReports.TxnReports;

public class ManualRefundProcessChargebackAction extends AbstractSecureAction {

	private static final long serialVersionUID = 2320778314499105148L;
	private static Logger logger = LoggerFactory.getLogger(ManualRefundProcessChargebackAction.class.getName());

	private String orderId;
	private String caseId;
	private String payId;
	private String pgRefNum;
	private String CurrencyCode;
	private String amount;
	private String txnType;
	private String refundFlag;
	private String refundAmount;
	private String response;
	private String refundedAmount;
	private String refundAvailable;
	private String chargebackAmount;
	
	ManualRefundProcess manualRefundProcess = new ManualRefundProcess();
	
	@Autowired
	private  RefundCommunicator refundCommunicator;

	@Autowired
	private TxnReports txnReport;

	@Autowired
	private ChargebackDao chargebackDao;
	
	public String execute() {
		try {
			
			
			User sessionUser = (User) sessionMap.get(Constants.USER);
			
			JSONObject json = new JSONObject();
			json.put(FieldType.ORDER_ID.getName(), getOrderId());
			json.put(FieldType.PAY_ID.getName(), getPayId());
			json.put(FieldType.PG_REF_NUM.getName(), getPgRefNum());
			json.put(FieldType.CURRENCY_CODE.getName(), CurrencyTypes.getCurrencyCodeFromName(getCurrencyCode()));
			json.put(FieldType.TXNTYPE.getName(), getTxnType());
			json.put(FieldType.REFUND_FLAG.getName(), getRefundFlag());
			json.put(FieldType.AMOUNT.getName(), Amount.formatAmount(getRefundAmount(), getCurrencyCode()));
			json.put(FieldType.REFUND_ORDER_ID.getName(), "LP"+TransactionManager.getNewTransactionId());

			logger.info("call RefundCommunicator for refund");
			response = (refundCommunicator.communicator(json));
			JSONObject jobj = new JSONObject(response);
			response = (String) jobj.get(FieldType.RESPONSE_MESSAGE.getName());
			//String captured=jobj.getString("STATUS");
			// Convert rsponse to json
			
			// If success , chargeback 
			if(response.equals("SUCCESS")){
			
				Chargeback cbNew = chargebackDao.findbyId(caseId);
				Date newDate = new Date();
				Date closedDate = null;
				String closeDateString = DateCreater.formatDateForDb(newDate);
				cbNew.setId(TransactionManager.getNewTransactionId());
				if (sessionUser.getUserType().equals(UserType.ADMIN)){
					cbNew.setChargebackStatus("Refunded by Admin");
					cbNew.setStatus("Refunded");
					cbNew.setUpdateDate(DateCreater.defaultCurrentDateTimeType(newDate));
					chargebackDao.create(cbNew);
					
					closedDate = DateCreater.defaultCurrentDateTimeType(new Date(newDate.getTime() + 10 * 1000));
					closeDateString = DateCreater.formatDateForDb(closedDate);
					
					cbNew.setId(TransactionManager.getNewTransactionId());
					cbNew.setChargebackStatus("Closed by Admin");
					cbNew.setStatus("Closed");
					cbNew.setCloseDate(closeDateString);
					cbNew.setUpdateDate(closedDate);
					chargebackDao.create(cbNew);
					
				}else if (sessionUser.getUserType().equals(UserType.SUBADMIN)){
					cbNew.setChargebackStatus("Refunded by Subadmin");
					cbNew.setStatus("Refunded");
					cbNew.setUpdateDate(DateCreater.defaultCurrentDateTimeType(newDate));
					chargebackDao.create(cbNew);
					
					closedDate = DateCreater.defaultCurrentDateTimeType(new Date(newDate.getTime() + 10 * 1000));
					closeDateString = DateCreater.formatDateForDb(closedDate);
					
					cbNew.setId(TransactionManager.getNewTransactionId());
					cbNew.setChargebackStatus("Closed by Subadmin");
					cbNew.setStatus("Closed");
					cbNew.setCloseDate(closeDateString);
					cbNew.setUpdateDate(closedDate);
					chargebackDao.create(cbNew);
					
				}else if (sessionUser.getUserType().equals(UserType.MERCHANT)){
//					cbNew.setChargebackStatus("Refunded by merchant");
//					cbNew.setStatus("Refunded");
//					cbNew.setUpdateDate(DateCreater.defaultCurrentDateTimeType());
//					chargebackDao.create(cbNew);
				}
			}
			logger.info("refund API  response received from pg ws " + response);
		} catch (SystemException e) {
			logger.error("Exception Caught in ManualRefundProccessAction " , e);
			return ERROR;
		} catch(Exception ex) {
			logger.error("Exception Caught in ManualRefundProccessAction " , ex);
		}
		return SUCCESS;
	}

	public String refundProcess() {
		try {
			setRefundAvailable(getChargebackAmount());
			setManualRefundProcess(txnReport.searchForManualRefund(getPgRefNum(), getPayId(), getRefundedAmount(), getRefundAvailable(), getChargebackAmount(),"chargebackRefund",""));
			logger.info("refund Detail generate "+getManualRefundProcess().toString());
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return ERROR;
		}
		return INPUT;
	}
	
	public String chargebackRefundProcess() {
		try {
			setManualRefundProcess(txnReport.searchForManualRefund(getPgRefNum(), getPayId(), getRefundedAmount(), getRefundAvailable(), getChargebackAmount(),"",""));
			logger.info("refund Detail generate "+getManualRefundProcess().toString());
			
			
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return ERROR;
		}
		return INPUT;
	}
	
	
	

	public String getCaseId() {
		return caseId;
	}

	public void setCaseId(String caseId) {
		this.caseId = caseId;
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

	public String getPgRefNum() {
		return pgRefNum;
	}

	public void setPgRefNum(String pgRefNum) {
		this.pgRefNum = pgRefNum;
	}

	public String getCurrencyCode() {
		return CurrencyCode;
	}

	public void setCurrencyCode(String currencyCode) {
		CurrencyCode = currencyCode;
	}

	public String getAmount() {
		return amount;
	}

	public void setAmount(String amount) {
		this.amount = amount;
	}

	public String getTxnType() {
		return txnType;
	}

	public void setTxnType(String txnType) {
		this.txnType = txnType;
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

	
	public String getResponse() {
		return response;
	}

	public void setResponse(String string) {
		this.response = response;
	}
	public ManualRefundProcess getManualRefundProcess() {
		return manualRefundProcess;
	}

	public void setManualRefundProcess(ManualRefundProcess manualRefundProcess) {
		this.manualRefundProcess = manualRefundProcess;
	}
	public String getRefundedAmount() {
		return refundedAmount;
	}

	public void setRefundedAmount(String refundedAmount) {
		this.refundedAmount = refundedAmount;
	}

	public String getRefundAvailable() {
		return refundAvailable;
	}

	public void setRefundAvailable(String refundAvailable) {
		this.refundAvailable = refundAvailable;
	}
	public String getChargebackAmount() {
		return chargebackAmount;
	}

	public void setChargebackAmount(String chargebackAmount) {
		this.chargebackAmount = chargebackAmount;
	}
}
