package com.paymentgateway.crm.action;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.mongo.MongoInstance;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.MopType;
import com.paymentgateway.commons.util.PropertiesManager;

public class AgentUpdateAction extends AbstractSecureAction {

	private static final long serialVersionUID = 1800261822743108331L;

	private static Logger logger = LoggerFactory.getLogger(AgentUpdateAction.class.getName());

	@Autowired
	private MongoInstance mongoInstance;

	private static final String prefix = "MONGO_DB_";

	List<BasicDBObject> conditionQueryList = new ArrayList<BasicDBObject>();

	private User sessionUser = null;

	private String responseStatus;
	private BigInteger transactionId;
	private String txnId;
	private String payId;
	private String merchant;
	private String txnType;
	private String paymentType;
	private String status;
	private String amount;
	private String totalAmount;
	private String orderId;
	private String mopType;
	private String pgRefNum;
	private String tDate;
	private String custName;
	private String rrn;
	private String acqId;
	private String pgResponseMessage;
	private String acquirerTxnMessage;
	private String responseCode;
	private String cardNum;
	private String cardMask;
	private String refund_txn_id;
	private String paymentGatewayResponseMessage;
	private String ACQUIRER_TDR_SC;
	private String ACQUIRER_GST;
	private String PG_GST;
	private String PG_TDR_SC;
	private String acquirerType;
	private String payment_Region;
	private String card_Holder_Type;
	private String acquirerMode;
	private String totalGst;
	private String totalTdrSc;
	private String subMerchantId;
	private String resellerCharges;
	private String resellerGst;
	private String totalChargeTdrSc;
	private String txnSettledType;
	private String srNo;
	private String response;
	private String dbUpdate; // status/txn; status(data from collection transactionStatus) | txn(data from
							

	public String execute() {

		sessionUser = (User) sessionMap.get(Constants.USER.getValue());
		if (sessionUser.getUserType().equals(UserType.ADMIN)) {
			int flag = 0;
			try {
				flag = updateData();
				if (flag > 0) {
					response = "SUCCESS";									
				} else {
					response = "ERROR";					
				}
				
			} catch (Exception e) {
				logger.error("Error while updating db");
				response = "ERROR";
				return SUCCESS;
			}
			return SUCCESS;	
		} else {
			response = "ERROR";
			return SUCCESS;
		}		
	}

	private int updateData() {

		logger.info("Inside updateData() : " + "TXN_ID = " + txnId);
		List<BasicDBObject> allConditionQueryList = new ArrayList<BasicDBObject>();
		allConditionQueryList.add(new BasicDBObject(FieldType.TXN_ID.getName(), txnId));
		MongoDatabase dbIns = mongoInstance.getDB();

		String collectionName = "";

		if (dbUpdate instanceof String) {
			if (dbUpdate.equalsIgnoreCase("status"))
				collectionName = Constants.TRANSACTION_STATUS_COLLECTION.getValue();
			else if (dbUpdate.equalsIgnoreCase("txn"))
				collectionName = Constants.COLLECTION_NAME.getValue();
			else
				collectionName = "";
		}

		int count = 0;

		if (!collectionName.isEmpty()) {

			MongoCollection<Document> collection = dbIns
					.getCollection(PropertiesManager.propertiesMap.get(prefix + collectionName));

			BasicDBObject finalquery = new BasicDBObject(FieldType.TXN_ID.getName(), txnId);
			MongoCursor<Document> cursor = collection.find(finalquery).iterator();

			while (cursor.hasNext()) {
				Document document = (Document) cursor.next();
				BasicDBObject oldFieldsObj = new BasicDBObject();
				oldFieldsObj.put("_id", document.getString("_id"));

				document.putAll(getDataForAgentUpdate());
				Document oldDoc = new Document(oldFieldsObj);
				Document newDoc = new Document("$set", document);
				collection.updateOne(oldDoc, newDoc);

				count++;
			}
		} else {
			logger.info("value for dbUpdate not found.(should be 'txn' or 'status')");
		}
		return count;
	}

	private Map<String, String> getDataForAgentUpdate() {

		logger.info("Inside getMappedDataForAgentUpdate() : ");

		Map<String, String> map = new HashMap<String, String>();
		
		if(StringUtils.isNotBlank(acquirerMode) && !acquirerMode.equalsIgnoreCase("NA")) {
			map.put(FieldType.ACQUIRER_MODE.getName(), acquirerMode);
		}
		
		if(StringUtils.isNotBlank(card_Holder_Type) && !card_Holder_Type.equalsIgnoreCase("NA")) {
			map.put(FieldType.CARD_HOLDER_TYPE.getName(), card_Holder_Type);
		}
		
		if(StringUtils.isNotBlank(paymentType) && !paymentType.equalsIgnoreCase("NA")) {
			map.put(FieldType.PAYMENT_TYPE.getName(), paymentType);
		}
		
		if(StringUtils.isNotBlank(mopType) && !mopType.equalsIgnoreCase("NA")) {
			map.put(FieldType.MOP_TYPE.getName(), MopType.getMopTypeName(mopType));
		}

		if(StringUtils.isNotBlank(payment_Region) && !payment_Region.equalsIgnoreCase("NA")) {
			map.put(FieldType.PAYMENTS_REGION.getName(), payment_Region);
		}
		
		if(StringUtils.isNotBlank(txnSettledType) && !txnSettledType.equalsIgnoreCase("NA")) {
			map.put(FieldType.TXN_CAPTURE_FLAG.getName(), txnSettledType);
		}
		
		if(StringUtils.isNotBlank(status) && !status.equalsIgnoreCase("NA")) {
			map.put(FieldType.STATUS.getName(), status);
		}
		
		// Change Response code also when status is captured
		if(StringUtils.isNotBlank(status) && status.equalsIgnoreCase("Captured")) {
			map.put(FieldType.RESPONSE_CODE.getName(), ErrorType.SUCCESS.getCode());
		}
		
		if(StringUtils.isNotBlank(txnId) && !txnId.equalsIgnoreCase("NA")) {
			map.put(FieldType.TXN_ID.getName(), txnId);
		}

		if(StringUtils.isNotBlank(pgRefNum) && !pgRefNum.equalsIgnoreCase("NA")) {
			map.put(FieldType.PG_REF_NUM.getName(), pgRefNum);
		}
		
		if(StringUtils.isNotBlank(orderId) && !orderId.equalsIgnoreCase("NA")) {
			map.put(FieldType.ORDER_ID.getName(), orderId);
		}
		
		
		if(StringUtils.isNotBlank(refund_txn_id) && !refund_txn_id.equalsIgnoreCase("NA")) {
			map.put(FieldType.REFUND_ORDER_ID.getName(), refund_txn_id);
		}
		
		if(StringUtils.isNotBlank(tDate) && !tDate.equalsIgnoreCase("NA")) {
			map.put(FieldType.TXN_DATE.getName(), tDate);
		}

		if(StringUtils.isNotBlank(txnType) && !txnType.equalsIgnoreCase("NA")) {
			map.put(FieldType.TXNTYPE.getName(), txnType);
		}
		
		if(StringUtils.isNotBlank(acquirerType) && !acquirerType.equalsIgnoreCase("NA")) {
			map.put(FieldType.ACQUIRER_TYPE.getName(), acquirerType);
		}
		
		if(StringUtils.isNotBlank(cardNum) && !cardNum.equalsIgnoreCase("NA") && getMaskedNumber(cardNum)) {
			map.put(FieldType.CARD_MASK.getName(), cardNum);
		}

		if(StringUtils.isNotBlank(custName) && !custName.equalsIgnoreCase("NA")) {
			map.put(FieldType.CUST_NAME.getName(), custName);
		}
		
		if(StringUtils.isNotBlank(amount) && !amount.equalsIgnoreCase("NA")) {
			map.put(FieldType.AMOUNT.getName(), amount);
		}
		
		if(StringUtils.isNotBlank(totalAmount) && !totalAmount.equalsIgnoreCase("NA")) {
			map.put(FieldType.TOTAL_AMOUNT.getName(), totalAmount);
		}
		
		if(StringUtils.isNotBlank(totalTdrSc) && !totalTdrSc.equalsIgnoreCase("NA")) {
			map.put("TOTAL_TDR_SC", totalTdrSc);
		}

		if(StringUtils.isNotBlank(totalGst) && !totalGst.equalsIgnoreCase("NA")) {
			map.put("TOTAL_GST", totalGst);
		}
		
		if(StringUtils.isNotBlank(PG_TDR_SC) && !PG_TDR_SC.equalsIgnoreCase("NA")) {
			map.put(FieldType.PG_TDR_SC.getName(), PG_TDR_SC);
		}
		
		
		if(StringUtils.isNotBlank(PG_GST) && !PG_GST.equalsIgnoreCase("NA")) {
			map.put(FieldType.PG_GST.getName(), PG_GST);
		}
		
		if(StringUtils.isNotBlank(ACQUIRER_TDR_SC) && !ACQUIRER_TDR_SC.equalsIgnoreCase("NA")) {
			map.put(FieldType.ACQUIRER_TDR_SC.getName(), ACQUIRER_TDR_SC);
		}
		

		if(StringUtils.isNotBlank(ACQUIRER_GST) && !ACQUIRER_GST.equalsIgnoreCase("NA")) {
			map.put(FieldType.ACQUIRER_GST.getName(), ACQUIRER_GST);
		}
		
		if(StringUtils.isNotBlank(resellerCharges) && !resellerCharges.equalsIgnoreCase("NA")) {
			map.put(FieldType.RESELLER_CHARGES.getName(), resellerCharges);
		}
		
		if(StringUtils.isNotBlank(resellerGst) && !resellerGst.equalsIgnoreCase("NA")) {
			map.put(FieldType.RESELLER_GST.getName(), resellerGst);
		}
		
		if(StringUtils.isNotBlank(pgResponseMessage) && !pgResponseMessage.equalsIgnoreCase("NA")) {
			map.put(FieldType.RESPONSE_MESSAGE.getName(), pgResponseMessage);
		}

		if(StringUtils.isNotBlank(rrn) && !rrn.equalsIgnoreCase("NA")) {
			map.put(FieldType.RRN.getName(), rrn);
		}
		
		if(StringUtils.isNotBlank(acqId) && !acqId.equalsIgnoreCase("NA")) {
			map.put(FieldType.ACQ_ID.getName(), acqId);
		}
		
		
		if(StringUtils.isNotBlank(acquirerTxnMessage) && !acquirerTxnMessage.equalsIgnoreCase("NA")) {
			map.put(FieldType.PG_TXN_MESSAGE.getName(), acquirerTxnMessage);
		}
		

		return map;

	}
	public boolean getMaskedNumber(String number) {
			
		if(!number.contains("*") && number.length() >= 13 && number.length() <= 19) {
			StringBuilder builder = new StringBuilder(number);
			builder.replace(6, 12, "******");
			setCardNum(builder.toString());
			return true;
		} else if(number.contains("*") && number.length() >= 13 && number.length() <= 19){
			return true;
		}
		
		return false;
	}
	public static Logger getLogger() {
		return logger;
	}

	public static void setLogger(Logger logger) {
		AgentUpdateAction.logger = logger;
	}

	public String getResponseStatus() {
		return responseStatus;
	}

	public void setResponseStatus(String responseStatus) {
		this.responseStatus = responseStatus;
	}

	public BigInteger getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(BigInteger transactionId) {
		this.transactionId = transactionId;
	}

	public String getTxnId() {
		return txnId;
	}

	public void setTxnId(String txnId) {
		this.txnId = txnId;
	}

	public String getPayId() {
		return payId;
	}

	public void setPayId(String payId) {
		this.payId = payId;
	}

	public String getMerchant() {
		return merchant;
	}

	public void setMerchant(String merchant) {
		this.merchant = merchant;
	}

	public String getTxnType() {
		return txnType;
	}

	public void setTxnType(String txnType) {
		this.txnType = txnType;
	}

	public String getPaymentType() {
		return paymentType;
	}

	public void setPaymentType(String paymentType) {
		this.paymentType = paymentType;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getAmount() {
		return amount;
	}

	public void setAmount(String amount) {
		this.amount = amount;
	}

	public String getTotalAmount() {
		return totalAmount;
	}

	public void setTotalAmount(String totalAmount) {
		this.totalAmount = totalAmount;
	}

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	public String getMopType() {
		return mopType;
	}

	public void setMopType(String mopType) {
		this.mopType = mopType;
	}

	public String getPgRefNum() {
		return pgRefNum;
	}

	public void setPgRefNum(String pgRefNum) {
		this.pgRefNum = pgRefNum;
	}

	public String gettDate() {
		return tDate;
	}

	public void settDate(String tDate) {
		this.tDate = tDate;
	}

	public String getCustName() {
		return custName;
	}

	public void setCustName(String custName) {
		this.custName = custName;
	}

	public String getRrn() {
		return rrn;
	}

	public void setRrn(String rrn) {
		this.rrn = rrn;
	}

	public String getAcqId() {
		return acqId;
	}

	public void setAcqId(String acqId) {
		this.acqId = acqId;
	}

	public String getPgResponseMessage() {
		return pgResponseMessage;
	}

	public void setPgResponseMessage(String pgResponseMessage) {
		this.pgResponseMessage = pgResponseMessage;
	}

	public String getAcquirerTxnMessage() {
		return acquirerTxnMessage;
	}

	public void setAcquirerTxnMessage(String acquirerTxnMessage) {
		this.acquirerTxnMessage = acquirerTxnMessage;
	}

	public String getResponseCode() {
		return responseCode;
	}

	public void setResponseCode(String responseCode) {
		this.responseCode = responseCode;
	}

	public String getCardNum() {
		return cardNum;
	}

	public void setCardNum(String cardNum) {
		this.cardNum = cardNum;
	}

	public String getCardMask() {
		return cardMask;
	}

	public void setCardMask(String cardMask) {
		this.cardMask = cardMask;
	}

	public String getRefund_txn_id() {
		return refund_txn_id;
	}

	public void setRefund_txn_id(String refund_txn_id) {
		this.refund_txn_id = refund_txn_id;
	}

	public String getPaymentGatewayResponseMessage() {
		return paymentGatewayResponseMessage;
	}

	public void setPaymentGatewayResponseMessage(String paymentGatewayResponseMessage) {
		this.paymentGatewayResponseMessage = paymentGatewayResponseMessage;
	}

	public String getACQUIRER_TDR_SC() {
		return ACQUIRER_TDR_SC;
	}

	public void setACQUIRER_TDR_SC(String aCQUIRER_TDR_SC) {
		ACQUIRER_TDR_SC = aCQUIRER_TDR_SC;
	}

	public String getACQUIRER_GST() {
		return ACQUIRER_GST;
	}

	public void setACQUIRER_GST(String aCQUIRER_GST) {
		ACQUIRER_GST = aCQUIRER_GST;
	}

	public String getPG_GST() {
		return PG_GST;
	}

	public void setPG_GST(String pG_GST) {
		PG_GST = pG_GST;
	}

	public String getPG_TDR_SC() {
		return PG_TDR_SC;
	}

	public void setPG_TDR_SC(String pG_TDR_SC) {
		PG_TDR_SC = pG_TDR_SC;
	}

	public String getAcquirerType() {
		return acquirerType;
	}

	public void setAcquirerType(String acquirerType) {
		this.acquirerType = acquirerType;
	}

	public String getPayment_Region() {
		return payment_Region;
	}

	public void setPayment_Region(String payment_Region) {
		this.payment_Region = payment_Region;
	}

	public String getCard_Holder_Type() {
		return card_Holder_Type;
	}

	public void setCard_Holder_Type(String card_Holder_Type) {
		this.card_Holder_Type = card_Holder_Type;
	}

	public String getAcquirerMode() {
		return acquirerMode;
	}

	public void setAcquirerMode(String acquirerMode) {
		this.acquirerMode = acquirerMode;
	}

	public String getTotalGst() {
		return totalGst;
	}

	public void setTotalGst(String totalGst) {
		this.totalGst = totalGst;
	}

	public String getTotalTdrSc() {
		return totalTdrSc;
	}

	public void setTotalTdrSc(String totalTdrSc) {
		this.totalTdrSc = totalTdrSc;
	}

	public String getSubMerchantId() {
		return subMerchantId;
	}

	public void setSubMerchantId(String subMerchantId) {
		this.subMerchantId = subMerchantId;
	}

	public String getResellerCharges() {
		return resellerCharges;
	}

	public void setResellerCharges(String resellerCharges) {
		this.resellerCharges = resellerCharges;
	}

	public String getResellerGst() {
		return resellerGst;
	}

	public void setResellerGst(String resellerGst) {
		this.resellerGst = resellerGst;
	}

	public String getTotalChargeTdrSc() {
		return totalChargeTdrSc;
	}

	public void setTotalChargeTdrSc(String totalChargeTdrSc) {
		this.totalChargeTdrSc = totalChargeTdrSc;
	}

	public String getTxnSettledType() {
		return txnSettledType;
	}

	public void setTxnSettledType(String txnSettledType) {
		this.txnSettledType = txnSettledType;
	}

	public String getSrNo() {
		return srNo;
	}

	public void setSrNo(String srNo) {
		this.srNo = srNo;
	}

	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
	}

	public String getDbUpdate() {
		return dbUpdate;
	}

	public void setDbUpdate(String dbUpdate) {
		this.dbUpdate = dbUpdate;
	}

}
