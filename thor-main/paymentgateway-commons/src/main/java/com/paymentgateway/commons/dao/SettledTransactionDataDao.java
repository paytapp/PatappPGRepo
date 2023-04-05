package com.paymentgateway.commons.dao;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.mongo.MongoInstance;
import com.paymentgateway.commons.user.SettledTransactionDataObject;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.SettledTransactionDataService;

@Service
public class SettledTransactionDataDao {

	
	private static final String prefix = "MONGO_DB_";
	@Autowired
	private MongoInstance mongoInstance;

	@Autowired
	private PropertiesManager propertiesManager;
	private static Logger logger = LoggerFactory.getLogger(SettledTransactionDataDao.class.getName());
	
	public void insertTransaction(List<SettledTransactionDataObject> settledTransactionDataList) throws SystemException {
		try {
			MongoDatabase dbIns = null;
			List<Document> documents = new ArrayList<>();
			for (SettledTransactionDataObject settledTransactionData :settledTransactionDataList) {
				Document e = new Document();
				e.put("_id", settledTransactionData.get_id());
				e.put(FieldType.TXN_ID.getName(), settledTransactionData.getTransactionId());
				e.put(FieldType.PG_REF_NUM.getName(), settledTransactionData.getTransactionId());
				e.put(FieldType.PAYMENTS_REGION.getName(), settledTransactionData.getTransactionRegion());
				e.put(FieldType.POST_SETTLED_FLAG.getName(), settledTransactionData.getPostSettledFlag());
				e.put(FieldType.TXNTYPE.getName(), settledTransactionData.getTxnType());
				e.put(FieldType.ACQUIRER_TYPE.getName(), settledTransactionData.getAcquirerType());
				e.put(FieldType.PAYMENT_TYPE.getName(), settledTransactionData.getPaymentMethods());
				e.put(FieldType.CREATE_DATE.getName(), settledTransactionData.getCreateDate());
				e.put(FieldType.ORDER_ID.getName(), settledTransactionData.getOrderId());
				e.put(FieldType.PAY_ID.getName(), settledTransactionData.getPayId());
				e.put(FieldType.MOP_TYPE.getName(), settledTransactionData.getMopType());
				e.put(FieldType.CURRENCY_CODE.getName(), settledTransactionData.getCurrency());
				e.put(FieldType.CARD_HOLDER_TYPE.getName(), settledTransactionData.getCardHolderType());
				e.put(FieldType.PG_DATE_TIME.getName(), settledTransactionData.getCaptureDate());
				e.put(FieldType.ACQ_ID.getName(), settledTransactionData.getAcqId());
				e.put(FieldType.RRN.getName(), settledTransactionData.getRrn());
				e.put(FieldType.UDF6.getName(), settledTransactionData.getDeltaRefundFlag());
				e.put(FieldType.SURCHARGE_FLAG.getName(), settledTransactionData.getSurchargeFlag());
				e.put(FieldType.REFUND_FLAG.getName(), settledTransactionData.getRefundFlag());
				e.put(FieldType.ARN.getName(), settledTransactionData.getArn());
				e.put(FieldType.ORIG_TXN_ID.getName(), settledTransactionData.getOrigTxnId());
				e.put(FieldType.TOTAL_AMOUNT.getName(), settledTransactionData.getTotalAmount());
				e.put(FieldType.AMOUNT.getName(), settledTransactionData.getAmount());
				e.put(FieldType.SURCHARGE_ACQ.getName(), settledTransactionData.getTdrScAcquirer());
				e.put(FieldType.SURCHARGE_PG.getName(), settledTransactionData.getTdrScPg());
				e.put(FieldType.SURCHARGE_PAYMENT_GATEWAY.getName(), settledTransactionData.getTdrScPaymentGateway());
				e.put(FieldType.GST_ACQ.getName(), settledTransactionData.getGstScAcquirer());
				e.put(FieldType.GST_PG.getName(), settledTransactionData.getGstScPg());
				e.put(FieldType.GST_PAYMENT_GATEWAY.getName(), settledTransactionData.getGstScPaymentGateway());
				e.put(FieldType.STATUS.getName(), settledTransactionData.getStatus());
				documents.add(e);
			}
			
			logger.info("Added "+documents.size()+" transactions in mongo document for upload to settlement table");
			dbIns = mongoInstance.getDB();
			MongoCollection<Document>  collection = dbIns
					.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.SETTLED_TRANSACTIONS_NAME.getValue()));
			collection.insertMany(documents);
		}
		
		catch(Exception e) {
			logger.info("Exception in insertTransaction for settlement collection "+e);
		}
	}
	
}
