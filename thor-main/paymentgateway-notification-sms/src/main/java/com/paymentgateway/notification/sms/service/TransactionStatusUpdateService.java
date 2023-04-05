package com.paymentgateway.notification.sms.service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;

import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mongodb.BasicDBObject;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.paymentgateway.commons.mongo.MongoInstance;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.DataEncDecTool;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.SystemProperties;
import com.paymentgateway.commons.util.TransactionType;
import com.paymentgateway.commons.util.threadpool.ThreadPoolProvider;

@Service
public class TransactionStatusUpdateService {

	private static final Collection<String> aLLDB_Fields = SystemProperties.getDBFields();
	private static final String prefix = "MONGO_DB_";
	private static Logger logger = LoggerFactory.getLogger(TransactionStatusUpdateService.class.getName());

	@Autowired
	private MongoInstance mongoInstance;

	@Autowired
	private PropertiesManager propertiesManager;
	
	@Autowired
	private DataEncDecTool dataEncDecTool;

	public void updateTxnStatus(Fields fields) {

		try {
			BasicDBObject newFieldsObj = new BasicDBObject();

			newFieldsObj.put(FieldType.AMOUNT.getName(), fields.get(FieldType.AMOUNT.getName()));

			if (!StringUtils.isEmpty(fields.get(FieldType.TOTAL_AMOUNT.getName()))) {
				newFieldsObj.put(FieldType.TOTAL_AMOUNT.getName(), fields.get(FieldType.TOTAL_AMOUNT.getName()));
			}

			if (!StringUtils.isEmpty(fields.get(FieldType.SURCHARGE_AMOUNT.getName()))) {
				newFieldsObj.put(FieldType.SURCHARGE_AMOUNT.getName(),
						fields.get(FieldType.SURCHARGE_AMOUNT.getName()));
			}

			if (!StringUtils.isEmpty(fields.get(FieldType.ORIG_TXN_ID.getName()))) {
				newFieldsObj.put(FieldType.ORIG_TXN_ID.getName(), fields.get(FieldType.ORIG_TXN_ID.getName()));
			}

			if (!StringUtils.isEmpty(fields.get(FieldType.ORIG_TXNTYPE.getName()))) {
				newFieldsObj.put(FieldType.ORIG_TXNTYPE.getName(), fields.get(FieldType.ORIG_TXNTYPE.getName()));
			}

			if (!StringUtils.isEmpty(fields.get(FieldType.PG_REF_NUM.getName()))) {
				newFieldsObj.put(FieldType.PG_REF_NUM.getName(), fields.get(FieldType.PG_REF_NUM.getName()));
			}

			String invoiceId = fields.get(FieldType.INVOICE_ID.getName());
			if (!StringUtils.isEmpty(invoiceId)) {
				newFieldsObj.put(FieldType.INVOICE_ID.getName(), invoiceId);
			}

			if (!StringUtils.isEmpty(fields.get(FieldType.ACCT_ID.getName()))) {
				newFieldsObj.put(FieldType.ACCT_ID.getName(), fields.get(FieldType.ACCT_ID.getName()));
			}

			if (!StringUtils.isEmpty(fields.get(FieldType.ACQ_ID.getName()))) {
				newFieldsObj.put(FieldType.ACQ_ID.getName(), fields.get(FieldType.ACQ_ID.getName()));
			}

			if (!StringUtils.isEmpty(fields.get(FieldType.UDF1.getName()))) {
				newFieldsObj.put(FieldType.UDF1.getName(), fields.get(FieldType.UDF1.getName()));
			}

			if (!StringUtils.isEmpty(fields.get(FieldType.UDF2.getName()))) {
				newFieldsObj.put(FieldType.UDF2.getName(), fields.get(FieldType.UDF2.getName()));
			}

			if (!StringUtils.isEmpty(fields.get(FieldType.UDF3.getName()))) {
				newFieldsObj.put(FieldType.UDF3.getName(), fields.get(FieldType.UDF3.getName()));
			}

			if (!StringUtils.isEmpty(fields.get(FieldType.UDF4.getName()))) {
				newFieldsObj.put(FieldType.UDF4.getName(), fields.get(FieldType.UDF4.getName()));
			}

			if (!StringUtils.isEmpty(fields.get(FieldType.UDF5.getName()))) {
				newFieldsObj.put(FieldType.UDF5.getName(), fields.get(FieldType.UDF5.getName()));
			}

			if (!StringUtils.isEmpty(fields.get(FieldType.UDF6.getName()))) {
				newFieldsObj.put(FieldType.UDF6.getName(), fields.get(FieldType.UDF6.getName()));
			}

			if (!StringUtils.isEmpty(fields.get(FieldType.OID.getName()))) {
				newFieldsObj.put(FieldType.OID.getName(), fields.get(FieldType.OID.getName()));
			}

			if (!StringUtils.isEmpty(fields.get(FieldType.PAYMENTS_REGION.getName()))) {
				newFieldsObj.put(FieldType.PAYMENTS_REGION.getName(), fields.get(FieldType.PAYMENTS_REGION.getName()));
			}

			if (!StringUtils.isEmpty(fields.get(FieldType.CARD_HOLDER_TYPE.getName()))) {
				newFieldsObj.put(FieldType.CARD_HOLDER_TYPE.getName(),
						fields.get(FieldType.CARD_HOLDER_TYPE.getName()));
			}

			if (!StringUtils.isEmpty(fields.get(FieldType.REQUEST_DATE.getName()))) {
				newFieldsObj.put(FieldType.REQUEST_DATE.getName(), fields.get(FieldType.REQUEST_DATE.getName()));
			}

			if (!StringUtils.isEmpty(fields.get(FieldType.SALE_AMOUNT.getName()))) {
				newFieldsObj.put(FieldType.SALE_AMOUNT.getName(), fields.get(FieldType.SALE_AMOUNT.getName()));
			}

			if (!StringUtils.isEmpty(fields.get(FieldType.SALE_TOTAL_AMOUNT.getName()))) {
				newFieldsObj.put(FieldType.SALE_TOTAL_AMOUNT.getName(),
						fields.get(FieldType.SALE_TOTAL_AMOUNT.getName()));
			}

			if (!StringUtils.isEmpty(fields.get(FieldType.TXNTYPE.getName()))) {
				newFieldsObj.put(FieldType.TXNTYPE.getName(), fields.get(FieldType.TXNTYPE.getName()));
			}

			if (!StringUtils.isEmpty(fields.get(FieldType.STATUS.getName()))) {
				newFieldsObj.put(FieldType.STATUS.getName(), fields.get(FieldType.STATUS.getName()));
			}

			for (int i = 0; i < fields.size(); i++) {

				for (String columnName : aLLDB_Fields) {

					if (columnName.equals(FieldType.CREATE_DATE.getName())) {
						newFieldsObj.put(columnName, fields.get(FieldType.CREATE_DATE.getName()));
					} else if (columnName.equals(FieldType.DATE_INDEX.getName())) {
						newFieldsObj.put(columnName, fields.get(FieldType.DATE_INDEX.getName()));
					} else if (columnName.equals(FieldType.UPDATE_DATE.getName())) {
						newFieldsObj.put(columnName, fields.get(FieldType.UPDATE_DATE.getName()));
					} else if (columnName.equals("_id")) {
						newFieldsObj.put(columnName, fields.get(FieldType.TXN_ID.getName()));
					} else if (columnName.equals(FieldType.INSERTION_DATE.getName())) {
						newFieldsObj.put(columnName, fields.get(FieldType.INSERTION_DATE.getName()));
					} else if (columnName.equals(FieldType.ACQUIRER_TDR_SC.getName())) {
						newFieldsObj.put(columnName, fields.get(FieldType.ACQUIRER_TDR_SC.getName()));
					} else if (columnName.equals(FieldType.ACQUIRER_GST.getName())) {
						newFieldsObj.put(columnName, fields.get(FieldType.ACQUIRER_GST.getName()));
					} else if (columnName.equals(FieldType.PG_TDR_SC.getName())) {
						newFieldsObj.put(columnName, fields.get(FieldType.PG_TDR_SC.getName()));
					} else if (columnName.equals(FieldType.PG_GST.getName())) {
						newFieldsObj.put(columnName, fields.get(FieldType.PG_GST.getName()));
					} else if (columnName.equals(FieldType.AMOUNT.getName())) {
						continue;
					} else if (columnName.equals(FieldType.TOTAL_AMOUNT.getName())) {
						continue;
					} else if (columnName.equals(FieldType.SURCHARGE_AMOUNT.getName())) {
						continue;
					} else if (columnName.equals(FieldType.ORIG_TXN_ID.getName())) {
						continue;
					} else if (columnName.equals(FieldType.PG_REF_NUM.getName())) {
						continue;
					} else if (columnName.equals(FieldType.ACCT_ID.getName())) {
						continue;
					} else if (columnName.equals(FieldType.ACQ_ID.getName())) {
						continue;
					} else if (columnName.equals(FieldType.OID.getName())) {
						continue;
					} else if (columnName.equals(FieldType.UDF1.getName())) {
						continue;
					} else if (columnName.equals(FieldType.UDF2.getName())) {
						continue;
					} else if (columnName.equals(FieldType.UDF3.getName())) {
						continue;
					} else if (columnName.equals(FieldType.UDF4.getName())) {
						continue;
					} else if (columnName.equals(FieldType.UDF5.getName())) {
						continue;
					} else if (columnName.equals(FieldType.UDF6.getName())) {
						continue;
					} else if (columnName.equals(FieldType.PAYMENTS_REGION.getName())) {
						continue;
					} else if (columnName.equals(FieldType.CARD_HOLDER_TYPE.getName())) {
						continue;
					} else if (columnName.equals(FieldType.SALE_AMOUNT.getName())) {
						continue;
					} else if (columnName.equals(FieldType.SALE_TOTAL_AMOUNT.getName())) {
						continue;
					} else if (columnName.equals(FieldType.ORIG_TXNTYPE.getName())) {
						continue;
					} else {
						newFieldsObj.put(columnName, fields.get(columnName));
					}
				}
			}

			Document doc = new Document(newFieldsObj);
			
			ExecutorService es =  ThreadPoolProvider.getExecutorService();
			es.execute(new Runnable() {
				@Override
	            public void run() {
	                try {
	                	updateStatusColl(fields, doc);
	                	
	                } catch (Exception exception) {
	    	        	logger.error("Exception calling transacion status update service ",exception);
	                }
	            }
	        });	        
			
			es.shutdown();
			
		

		} catch (Exception exception) {
			String message = "Error while inserting transaction status in database";
			logger.error(message, exception);
		}

	}

	public void updateStatusColl(Fields fields, Document document) {

		try {

			MongoDatabase dbIns = mongoInstance.getDB();

			String orderId = document.getString(FieldType.ORDER_ID.getName());
			String oid = document.get(FieldType.OID.getName()).toString();
			String origTxnType = fields.get(FieldType.ORIG_TXNTYPE.getName());
			String pgRefNum = fields.get(FieldType.PG_REF_NUM.getName());
			String txnType = fields.get(FieldType.TXNTYPE.getName());
			
			if (StringUtils.isBlank(txnType)) {
				txnType = "SALE";
			}

			Date dNow = new Date();
			String dateNow = formatDateForDb(dNow);
			
			if (txnType.equalsIgnoreCase(TransactionType.INVALID.getName())
					|| txnType.equalsIgnoreCase(TransactionType.NEWORDER.getName())
					|| txnType.equalsIgnoreCase(TransactionType.RECO.getName())
					|| txnType.equalsIgnoreCase(TransactionType.ENROLL.getName())) {
				txnType = TransactionType.SALE.getName();
			}

			if (txnType.equalsIgnoreCase(TransactionType.REFUND.getName())
					|| txnType.equalsIgnoreCase(TransactionType.REFUNDRECO.getName())) {
				txnType = TransactionType.REFUND.getName();
			}

			if (StringUtils.isBlank(origTxnType)) {
				origTxnType = txnType;
			}

			if (origTxnType.equalsIgnoreCase(TransactionType.INVALID.getName())) {
				origTxnType = TransactionType.SALE.getName();
			}

			if (origTxnType.equalsIgnoreCase(TransactionType.STATUS.getName())) {
				origTxnType = txnType;
			}

			// SALE
			if (origTxnType.equalsIgnoreCase(TransactionType.SALE.getName())) {
				if (StringUtils.isEmpty(orderId) || StringUtils.isEmpty(oid) || StringUtils.isEmpty(origTxnType)) {

					logger.info("Cannot update transaction status collection for combination " + " Order Id = "
							+ orderId + " OID = " + oid + " orig Txn Type = " + origTxnType);
					logger.info("Txn cannot be added , moving to transactionStatusException ");

					Document doc = new Document();
					doc.put(FieldType.ORDER_ID.getName(), orderId);
					doc.put(FieldType.OID.getName(), oid);
					doc.put(FieldType.ORIG_TXNTYPE.getName(), origTxnType);

					MongoCollection<Document> excepColl = dbIns.getCollection(PropertiesManager.propertiesMap
							.get(prefix + Constants.TRANSACTION_STATUS_EXCEP_COLLECTION.getValue()));

					excepColl.insertOne(doc);
				} else {

					MongoCollection<Document> coll = dbIns.getCollection(PropertiesManager.propertiesMap
							.get(prefix + Constants.TRANSACTION_STATUS_COLLECTION.getValue()));

					List<BasicDBObject> dbObjList = new ArrayList<BasicDBObject>();
					dbObjList.add(new BasicDBObject(FieldType.ORDER_ID.getName(), orderId));
					dbObjList.add(new BasicDBObject(FieldType.OID.getName(), oid));

					dbObjList.add(new BasicDBObject(FieldType.ORIG_TXNTYPE.getName(), origTxnType));

					BasicDBObject andQuery = new BasicDBObject("$and", dbObjList);

					FindIterable<Document> cursor = coll.find(andQuery);

					if (cursor.iterator().hasNext()) {
						
						String transactionStatusFields = propertiesManager.propertiesMap.get("TransactionStatusFields");
						String transactionStatusFieldsArr[] = transactionStatusFields.split(",");

						MongoCollection<Document> txnStatusColl = dbIns.getCollection(PropertiesManager.propertiesMap
								.get(prefix + Constants.TRANSACTION_STATUS_COLLECTION.getValue()));

						BasicDBObject searchQuery = andQuery;
						BasicDBObject updateFields = new BasicDBObject();

						String status = document.get(FieldType.STATUS.getName()).toString();
						String statusALias = resolveStatus(status);

						// Added to avoid exception in report
						if (StringUtils.isEmpty(statusALias)) {
							logger.info("Alias Status not resolved for order Id = " + orderId);
							statusALias = "Cancelled";
						}

						for (String key : transactionStatusFieldsArr) {

							if (status.equalsIgnoreCase(StatusType.SETTLED.getName())) {

								if ((key.equalsIgnoreCase(FieldType.DATE_INDEX.getName()))
										|| (key.equalsIgnoreCase(FieldType.CREATE_DATE.getName()))
										|| (key.equalsIgnoreCase(FieldType.UPDATE_DATE.getName()))
										|| (key.equalsIgnoreCase(FieldType.INSERTION_DATE.getName()))
										|| (key.equalsIgnoreCase(FieldType.STATUS.getName()))) {
									continue;
								}

								if ((key.equalsIgnoreCase(FieldType.SETTLEMENT_DATE.getName()))) {
									updateFields.put(key, dateNow);
									continue;
								}

								if ((key.equalsIgnoreCase(FieldType.SETTLEMENT_FLAG.getName()))) {
									updateFields.put(key, "Y");
									continue;
								}

								if ((key.equalsIgnoreCase(FieldType.SETTLEMENT_DATE_INDEX.getName()))) {
									updateFields.put(key, dateNow.substring(0,10).replace("-", ""));
									continue;
								}
								
								if ((key.equalsIgnoreCase(FieldType.TXNTYPE.getName()))) {
									updateFields.put(key, txnType);
									continue;
								}

							}

							if (document.get(key) != null) {
								updateFields.put(key, document.get(key).toString());
							} else {
								updateFields.put(key, document.get(key));
							}

						}

						updateFields.put(FieldType.ALIAS_STATUS.getName(), statusALias);
						updateFields.put(FieldType.STATUS.getName(), statusALias);
						updateFields.put(FieldType.ORIG_TXNTYPE.getName(), origTxnType);
						txnStatusColl.updateOne(searchQuery, new BasicDBObject("$set", updateFields));

					} else {

						MongoCollection<Document> txnStatusColl = dbIns.getCollection(PropertiesManager.propertiesMap
								.get(prefix + Constants.TRANSACTION_STATUS_COLLECTION.getValue()));

						String status = fields.get(FieldType.STATUS.getName());
						String statusALias = resolveStatus(status);

						// Added to avoid exception in report
						if (StringUtils.isEmpty(statusALias)) {
							logger.info("Alias Status not resolved for order Id = " + orderId);
							statusALias = "Cancelled";
						}

						document.put(FieldType.ALIAS_STATUS.getName(), statusALias);
						document.put(FieldType.STATUS.getName(), statusALias);
						document.put(FieldType.ORIG_TXNTYPE.getName(), origTxnType);
						if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
								&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {

							txnStatusColl.insertOne(dataEncDecTool.encryptDocument(document));
						} else {
							txnStatusColl.insertOne(document);
						}
						//txnStatusColl.insertOne(document);

					}

				}
			}

			// REFUND

			if (origTxnType.equalsIgnoreCase(TransactionType.REFUND.getName())) {
				if (StringUtils.isEmpty(pgRefNum) || StringUtils.isEmpty(oid) || StringUtils.isEmpty(origTxnType)) {

					logger.info("Cannot update transaction status collection for combination " + " PG REF NUM = "
							+ pgRefNum + " OID = " + oid + " orig Txn Type = " + origTxnType);
					logger.info("Txn cannot be added , moving to transactionStatusException ");

					Document doc = new Document();
					doc.put(FieldType.PG_REF_NUM.getName(), pgRefNum);
					doc.put(FieldType.OID.getName(), oid);
					doc.put(FieldType.ORIG_TXNTYPE.getName(), origTxnType);

					MongoCollection<Document> excepColl = dbIns.getCollection(PropertiesManager.propertiesMap
							.get(prefix + Constants.TRANSACTION_STATUS_EXCEP_COLLECTION.getValue()));

					excepColl.insertOne(doc);
				} else {

					MongoCollection<Document> coll = dbIns.getCollection(PropertiesManager.propertiesMap
							.get(prefix + Constants.TRANSACTION_STATUS_COLLECTION.getValue()));

					List<BasicDBObject> dbObjList = new ArrayList<BasicDBObject>();
					dbObjList.add(new BasicDBObject(FieldType.PG_REF_NUM.getName(), pgRefNum));
					dbObjList.add(new BasicDBObject(FieldType.OID.getName(), oid));
					dbObjList.add(new BasicDBObject(FieldType.ORIG_TXNTYPE.getName(), origTxnType));

					BasicDBObject andQuery = new BasicDBObject("$and", dbObjList);

					FindIterable<Document> cursor = coll.find(andQuery);

					if (cursor.iterator().hasNext()) {
						
						String transactionStatusFields = propertiesManager.propertiesMap.get("TransactionStatusFields");
						String transactionStatusFieldsArr[] = transactionStatusFields.split(",");
						MongoCollection<Document> txnStatusColl = dbIns.getCollection(PropertiesManager.propertiesMap
								.get(prefix + Constants.TRANSACTION_STATUS_COLLECTION.getValue()));

						BasicDBObject searchQuery = andQuery;
						BasicDBObject updateFields = new BasicDBObject();
						
						

						for (String key : transactionStatusFieldsArr) {

							if ((key.equalsIgnoreCase(FieldType.DATE_INDEX.getName()))
									|| (key.equalsIgnoreCase(FieldType.CREATE_DATE.getName()))
									|| (key.equalsIgnoreCase(FieldType.UPDATE_DATE.getName()))
									|| (key.equalsIgnoreCase(FieldType.INSERTION_DATE.getName()))
									|| (key.equalsIgnoreCase(FieldType.STATUS.getName()))) {
								continue;
							}
							
							if (document.get(key) != null) {
								updateFields.put(key, document.get(key).toString());
							} else {
								updateFields.put(key, document.get(key));
							}
							
						}
						updateFields.put(FieldType.SETTLEMENT_DATE_INDEX.getName(), dateNow.substring(0,10).replace("-", ""));
						updateFields.put(FieldType.SETTLEMENT_DATE.getName(), dateNow);
						updateFields.put(FieldType.SETTLEMENT_FLAG.getName(), "Y");
						
						String status = fields.get(FieldType.STATUS.getName());
						String statusALias = resolveStatus(status);
						updateFields.put(FieldType.ALIAS_STATUS.getName(), statusALias);
						updateFields.put(FieldType.STATUS.getName(), statusALias);
						updateFields.put(FieldType.ORIG_TXNTYPE.getName(), origTxnType);
						updateFields.put(FieldType.TXNTYPE.getName(), origTxnType);
						txnStatusColl.updateOne(searchQuery, new BasicDBObject("$set", updateFields));

					} else {

						MongoCollection<Document> txnStatusColl = dbIns.getCollection(PropertiesManager.propertiesMap
								.get(prefix + Constants.TRANSACTION_STATUS_COLLECTION.getValue()));

						String status = fields.get(FieldType.STATUS.getName());
						String statusALias = resolveStatus(status);
						document.put(FieldType.ALIAS_STATUS.getName(), statusALias);
						document.put(FieldType.STATUS.getName(), statusALias);
						document.put(FieldType.ORIG_TXNTYPE.getName(), origTxnType);
						document.put(FieldType.TXNTYPE.getName(), origTxnType);
						document.put(FieldType.SETTLEMENT_DATE_INDEX.getName(), dateNow.substring(0,10).replace("-", ""));
						document.put(FieldType.SETTLEMENT_DATE.getName(), dateNow);
						document.put(FieldType.SETTLEMENT_FLAG.getName(), "Y");
						if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
								&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {

							txnStatusColl.insertOne(dataEncDecTool.encryptDocument(document));
						} else {
							txnStatusColl.insertOne(document);
						}
						//txnStatusColl.insertOne(document);

					}

				}
			}

		}

		catch (Exception e) {
			logger.error("Exception in adding txn to transaction status", e);
		}

	}

	public void updateOldTxnStatus(String fromDate, String toDate) {

		try {

			String startString = new SimpleDateFormat(fromDate).toLocalizedPattern();
			String endString = new SimpleDateFormat(toDate).toLocalizedPattern();

			DateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.ENGLISH);
			Date dateStart = format.parse(startString);
			Date dateEnd = format.parse(endString);

			LocalDate incrementingDate = dateStart.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			LocalDate endDate = dateEnd.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

			List<String> allDatesIndex = new ArrayList<>();

			while (!incrementingDate.isAfter(endDate)) {
				allDatesIndex.add(incrementingDate.toString().replaceAll("-", ""));
				incrementingDate = incrementingDate.plusDays(1);
			}

			MongoDatabase dbIns = mongoInstance.getDB();
			for (String dateIndex : allDatesIndex) {

				logger.info("Updating data for dateIndex " + dateIndex);
				int count = 0;
				BasicDBObject dateIndexQuery = new BasicDBObject(FieldType.DATE_INDEX.getName(), dateIndex);

				MongoCollection<Document> coll = dbIns.getCollection(
						PropertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));
				BasicDBObject match = new BasicDBObject("$match", dateIndexQuery);
				BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject("CREATE_DATE", 1));
				List<BasicDBObject> pipeline = Arrays.asList(match, sort);

				AggregateIterable<Document> output = coll.aggregate(pipeline);
				output.allowDiskUse(true);
				MongoCursor<Document> cursor = output.iterator();

				while (cursor.hasNext()) {
					Document dbobj = cursor.next();

					Fields fields = new Fields();

					if (dbobj.get(FieldType.OID.getName()) != null) {
						fields.put(FieldType.OID.getName(), dbobj.get(FieldType.OID.getName()).toString());
					} else {
						fields.put(FieldType.OID.getName(), null);
					}

					if (dbobj.get(FieldType.ORDER_ID.getName()) != null) {
						fields.put(FieldType.ORDER_ID.getName(), dbobj.get(FieldType.ORDER_ID.getName()).toString());
					} else {
						fields.put(FieldType.ORDER_ID.getName(), null);
					}

					if (dbobj.get(FieldType.ORIG_TXNTYPE.getName()) == null) {

						if (dbobj.get(FieldType.TXNTYPE.getName()) == null) {
							fields.put(FieldType.ORIG_TXNTYPE.getName(), null);
						} else {
							fields.put(FieldType.ORIG_TXNTYPE.getName(),
									dbobj.get(FieldType.TXNTYPE.getName()).toString());
						}

					} else {
						fields.put(FieldType.ORIG_TXNTYPE.getName(),
								dbobj.get(FieldType.ORIG_TXNTYPE.getName()).toString());
					}

					fields.put(FieldType.STATUS.getName(), dbobj.get(FieldType.STATUS.getName()).toString());
					fields.put(FieldType.PG_REF_NUM.getName(), dbobj.get(FieldType.PG_REF_NUM.getName()).toString());
					fields.put(FieldType.TXNTYPE.getName(), dbobj.get(FieldType.TXNTYPE.getName()).toString());
					updateStatusColl(fields, dbobj);
					count = count + 1;
				}

				logger.info("Total data updated for dateIndex " + dateIndex + " ==== " + count);
				count = 0;
				cursor.close();
			}
		} catch (Exception e) {
			logger.error("Exception in updating old txn data", e);
		}

	}

	public String resolveStatus(String status) {

		if (StringUtils.isBlank(status)) {
			return status;
		} else {
			if (status.equals(StatusType.CAPTURED.getName())) {
				return "Captured";

			} else if (status.equals(StatusType.SETTLED.getName())) {
				return "Captured";

			} else if (status.equals(StatusType.PENDING.getName()) || status.equals(StatusType.SENT_TO_BANK.getName())
					|| status.equals(StatusType.ENROLLED.getName())) {
				return "Pending";

			} else if (status.equals(StatusType.BROWSER_CLOSED.getName())
					|| status.equals(StatusType.CANCELLED.getName())) {
				return "Cancelled";

			} else if (status.equals(StatusType.INVALID.getName()) || status.equals(StatusType.DUPLICATE.getName())
					) {
				return "Invalid";

			} 
			else {
				return "Failed";
			}

		}
	}

	public static String formatDateForDb(Date date){
		SimpleDateFormat outputDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");		
		return outputDateFormat.format(date);
	}
	
}
