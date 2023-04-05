package com.paymentgateway.scheduler.commons;

import java.util.List;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.mongo.MongoInstance;
import com.paymentgateway.commons.util.FieldType;
import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

/**
 * @author Shaiwal
 *
 */
@Service
public class PendingStatusEnquiryTransactions {

	@Autowired
	private MongoInstance mongoInstance;

	@Autowired
	private ConfigurationProvider configurationProvider;

	private static final Logger logger = LoggerFactory.getLogger(PendingStatusEnquiryTransactions.class);

	public void insertPendingTransactionData(String pgRefNumber, String payId, String orderId, String amount,
			String acquirerType, String paymentType, String mopType, String paymentsRegion, String cardHolderType,
			String createDate, String status, String refundOderId) throws SystemException {
		try {
			logger.info("Inserting pending status enquiry transactions into Database");

			MongoDatabase dbIns = null;
			BasicDBObject insertData = new BasicDBObject();

			insertData.put(FieldType.PG_REF_NUM.getName(), pgRefNumber);
			insertData.put(FieldType.PAY_ID.getName(), payId);
			insertData.put(FieldType.ORDER_ID.getName(), orderId);
			insertData.put(FieldType.AMOUNT.getName(), amount);
			insertData.put(FieldType.ACQUIRER_TYPE.getName(), acquirerType);
			insertData.put(FieldType.PAYMENT_TYPE.getName(), paymentType);
			insertData.put(FieldType.MOP_TYPE.getName(), mopType);
			insertData.put(FieldType.PAYMENTS_REGION.getName(), paymentsRegion);
			insertData.put(FieldType.CARD_HOLDER_TYPE.getName(), cardHolderType);
			insertData.put(FieldType.CREATE_DATE.getName(), createDate);
			insertData.put(FieldType.LAST_STATUS.getName(), status);
			insertData.put(FieldType.REFUND_ORDER_ID.getName(), refundOderId);

			dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns
					.getCollection(configurationProvider.getMONGO_DB_statusEnquiryCollectionName());
			Document doc = new Document(insertData);
			collection.insertOne(doc);

		} catch (Exception exception) {
			String message = "Error while inserting transaction in database";
			logger.error(message, exception);
			throw new SystemException(ErrorType.DATABASE_ERROR, exception, message);
		}
	}

	public void insertStatusEnquiryTransactionData(List<Document> transactionEnquiryList) throws SystemException {
		try {
			logger.info("Inserting status enquiry transactions into Database");

			MongoDatabase dbIns = null;
			dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns
					.getCollection(configurationProvider.getMONGO_DB_statusEnquiryCollectionName());

			if (transactionEnquiryList.size() > 0) {
				collection.insertMany(transactionEnquiryList);
				logger.info("Inserted "+transactionEnquiryList.size()+" transaction data into DB");
			}

		} catch (Exception exception) {
			String message = "Error while inserting status enquiry transactions in database";
			logger.error(message, exception);
			throw new SystemException(ErrorType.DATABASE_ERROR, exception, message);
		}
	}

}
