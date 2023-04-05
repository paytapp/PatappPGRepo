package com.paymentgateway.crm.action;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.bson.BSONObject;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.mongo.MongoInstance;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.util.Amount;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CurrencyTypes;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.ManualRefundProcess;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.TransactionManager;
import com.paymentgateway.commons.util.TxnType;
import com.paymentgateway.crm.actionBeans.RefundCommunicator;
import com.paymentgateway.crm.mongoReports.TxnReports;

public class ManualRefundProcessAction extends AbstractSecureAction {

	private static final long serialVersionUID = 2320778314499105148L;
	private static Logger logger = LoggerFactory.getLogger(ManualRefundProcessAction.class.getName());

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
	private String regNumber;
	private String objectId;
	private String skuCode;
	private String chargebackStatus;
	private List<String> skuCodeList = new ArrayList<String>();

	private ManualRefundProcess manualRefundProcess = new ManualRefundProcess();

	@Autowired
	private RefundCommunicator refundCommunicator;

	@Autowired
	private TxnReports txnReport;

	@Autowired
	private MongoInstance mongoInstance;

	@Autowired
	PropertiesManager propertiesManager;

	private static final String prefix = "MONGO_DB_";

	public String execute() {
		User sessionUser = null;
		sessionUser = (User) sessionMap.get(Constants.USER.getValue());

		try {
			JSONObject json = new JSONObject();
			json.put(FieldType.ORDER_ID.getName(), getOrderId());
			json.put(FieldType.PAY_ID.getName(), getPayId());
			json.put(FieldType.PG_REF_NUM.getName(), getPgRefNum());
			json.put(FieldType.CURRENCY_CODE.getName(), propertiesManager.getNumericCurrencyCode(getCurrencyCode()));
			json.put(FieldType.TXNTYPE.getName(), TxnType.REFUND.getName());
			json.put(FieldType.REFUND_FLAG.getName(), getRefundFlag());
			if(StringUtils.isNotBlank(chargebackAmount) && StringUtils.isNotBlank(chargebackStatus) 
					&& !(chargebackStatus.equalsIgnoreCase("Refunded") || chargebackStatus.equalsIgnoreCase("Closed"))) {
				json.put(FieldType.CHARGEBACK_AMOUNT.getName(), Amount.formatAmount(getChargebackAmount(), getCurrencyCode()));
			}
			if (getRefundAmount().equalsIgnoreCase("NAN")) {
				response = "Invalid AMOUNT";
				return SUCCESS;
			} else {
				json.put(FieldType.AMOUNT.getName(), Amount.formatAmount(getRefundAmount(), getCurrencyCode()));
			}
			String refundOrderId = "LP" + TransactionManager.getNewTransactionId();
			json.put(FieldType.REFUND_ORDER_ID.getName(), refundOrderId);
			json.put(FieldType.INTERNAL_USER_EMAIL.getName(), sessionUser.getEmailId());
			if (StringUtils.isNotBlank(regNumber)) {
				json.put(FieldType.REG_NUMBER.getName(), getRegNumber());
			}

			logger.info("call RefundCommunicator for refund");
			response = (refundCommunicator.communicator(json));
			JSONObject jobj = new JSONObject(response);
			if (response.contains(FieldType.RESPONSE_MESSAGE.getName())) {
				response = (String) jobj.get(FieldType.RESPONSE_MESSAGE.getName());
				if (StringUtils.isNotBlank(skuCode)) {
					updateProductDescriptionBasedOnSkuCode(skuCode, refundOrderId);
				}
			} else {
				response = jobj.getString(FieldType.PG_TXN_MESSAGE.getName());
			}

			logger.info("refund API  response received from pgws " + response);
		} catch (SystemException e) {
			logger.error("Exception Caught in ManualRefundProccessAction " , e);
			return ERROR;
		}
		return SUCCESS;
	}

	private void updateProductDescriptionBasedOnSkuCode(String skuCode, String refundOrderId) {
		txnReport.updateProductDescriptionBasedOnSkuCode(skuCode, getOrderId(), refundOrderId);
	}

	public String studentFeeRefundProcess() {

		try {
			setManualRefundProcess(txnReport.searchForStudentFeeManualRefund(getPgRefNum(), getPayId(),
					getRefundedAmount(), getRefundAvailable(), getRegNumber()));
			logger.info("refund Detail generated for school " + getManualRefundProcess().toString());
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return ERROR;
		}
		return INPUT;
	}

	public String refundProcess() {
		try {
			setSkuCodeList(txnReport.searchSkuCodeListForManualRefund(getPgRefNum(), getPayId(), getRefundedAmount(),
					getRefundAvailable(), getChargebackAmount()));
			setManualRefundProcess(txnReport.searchForManualRefund(getPgRefNum(), getPayId(), getRefundedAmount(),
					getRefundAvailable(), getChargebackAmount(),"",chargebackStatus));
			logger.info("refund Detail generated " + getManualRefundProcess().toString());
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return ERROR;
		}
		return SUCCESS;
	}

	

	public String chargebackRefundProcess() {
		try {
			setManualRefundProcess(txnReport.searchForManualRefund(getPgRefNum(), getPayId(), getRefundedAmount(),
					getRefundAvailable(), getChargebackAmount(),"",""));
			logger.info("refund Detail generate " + getManualRefundProcess().toString());

		} catch (Exception exception) {
			logger.error("Exception", exception);
			return ERROR;
		}
		return INPUT;
	}

	

	public void updateProductDescription(String orderId, String objectId, JSONObject jobj) {
		try {
			ObjectId id = new ObjectId(objectId);
			BasicDBObject obj = new BasicDBObject();
			obj.append("_id", id);
			BasicDBObject query = new BasicDBObject();
			query.putAll((BSONObject) obj);

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns.getCollection(
					propertiesManager.propertiesMap.get(prefix + Constants.PROD_DESC_COLLECTION.getValue()));
			BasicDBObject updateFields = new BasicDBObject();
			updateFields.append(FieldType.REFUND_ORDER_ID.getName(),
					jobj.getString(FieldType.REFUND_ORDER_ID.getName()));
			BasicDBObject updateQuery = new BasicDBObject();
			updateQuery.append("$set", updateFields);
			collection.updateMany(query, updateQuery);
		} catch (Exception e) {
			e.printStackTrace();
		}
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

	public String getRegNumber() {
		return regNumber;
	}

	public void setRegNumber(String regNumber) {
		this.regNumber = regNumber;
	}

	public String getObjectId() {
		return objectId;
	}

	public void setObjectId(String objectId) {
		this.objectId = objectId;
	}

	public String getSkuCode() {
		return skuCode;
	}

	public void setSkuCode(String skuCode) {
		this.skuCode = skuCode;
	}

	public List<String> getSkuCodeList() {
		return skuCodeList;
	}

	public void setSkuCodeList(List<String> skuCodeList) {
		this.skuCodeList = skuCodeList;
	}

	public String getChargebackStatus() {
		return chargebackStatus;
	}

	public void setChargebackStatus(String chargebackStatus) {
		this.chargebackStatus = chargebackStatus;
	}

}
