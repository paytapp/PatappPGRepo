package com.paymentgateway.commons.dao;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoException;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.paymentgateway.commons.mongo.MongoInstance;
import com.paymentgateway.commons.user.TransactionDetails;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserSettingData;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.AcquirerType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.DataEncDecTool;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TransactionType;
import com.paymentgateway.commons.util.TxnType;

@Service
public class TransactionDetailsPopUpDao {
	
	private static Logger logger = LoggerFactory.getLogger(TransactionDetailsPopUpDao.class.getName());
	private static final String prefix = "MONGO_DB_";

	@Autowired
	private PropertiesManager propertiesManager;

	@Autowired
	private MongoInstance mongoInstance;
	
	@Autowired
	private DataEncDecTool dataEncDecTool;
	
	@Autowired
	private UserSettingDao userSettingDao;

	public TransactionDetails getAllDetail(String orderId, String txnType, UserType userType) {
			
		logger.info("get all details for by order_Id");
		TransactionDetails trans = new TransactionDetails();
		DetailsByOrderId(orderId, trans);
		return transactionDetailsByOrderId(orderId, txnType, userType, trans);
	}
	
	public void DetailsByOrderId(String orderId, TransactionDetails trans) {
		logger.info("Inside TransactionDetailsPopUpDao for get customerDetails");
		try{
			BasicDBObject finalQuery = new BasicDBObject();
			finalQuery.put(FieldType.ORDER_ID.getName(), orderId);
			
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns
					.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.BILLING_COLLECTION.getValue()));
			MongoCursor<Document> cursor = coll.find(finalQuery).iterator();
			if(cursor.hasNext() == false) {
				logger.info("there is no data available for this order id in billing collection" +orderId);
				trans.setCustName(Constants.NA.getValue());
				trans.setCustMobileNum(Constants.NA.getValue());
				trans.setCustAddress(Constants.NA.getValue());
				trans.setCustCity(Constants.NA.getValue());
				trans.setCustState(Constants.NA.getValue());
				trans.setCustPin(Constants.NA.getValue());
				trans.setCustCountry(Constants.NA.getValue());
				trans.setCustShippingName(Constants.NA.getValue());
				trans.setCustShippingMobileNum(Constants.NA.getValue());
				trans.setCustShippingAddress(Constants.NA.getValue());
				trans.setCustShippingCity(Constants.NA.getValue());
				trans.setCustShippingState(Constants.NA.getValue());
				trans.setCustShippingPin(Constants.NA.getValue());
				trans.setCustShippingCountry(Constants.NA.getValue());
			}
			
			
			while (cursor.hasNext()) {
				logger.info("getting data from billing collection");
				Document doc = cursor.next();
				
				if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
						&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
					doc = dataEncDecTool.decryptDocument(doc);
				} 
				
				//For Customer Details
				if (StringUtils.isNotBlank(doc.getString(FieldType.CUST_NAME.getName()))) {
					trans.setCustName(doc.getString(FieldType.CUST_NAME.getName()));
				} else {
					trans.setCustName(Constants.NA.getValue());
				}
				
				if (StringUtils.isNotBlank(doc.getString(FieldType.CUST_PHONE.getName()))) {
					trans.setCustMobileNum(doc.getString(FieldType.CUST_PHONE.getName()));
				} else {
					trans.setCustMobileNum(Constants.NA.getValue());
				}
				
				if (StringUtils.isNotBlank(doc.getString(FieldType.CUST_STREET_ADDRESS1.getName()))) {
					trans.setCustAddress(doc.getString(FieldType.CUST_STREET_ADDRESS1.getName()));
				} else {
					trans.setCustAddress(Constants.NA.getValue());
				}
				
				if (StringUtils.isNotBlank(doc.getString(FieldType.CUST_CITY.getName()))) {
					trans.setCustCity(doc.getString(FieldType.CUST_CITY.getName()));
				} else {
					trans.setCustCity(Constants.NA.getValue());
				}
				if (StringUtils.isNotBlank(doc.getString(FieldType.CUST_STATE.getName()))) {
					trans.setCustState(doc.getString(FieldType.CUST_STATE.getName()));
				} else {
					trans.setCustState(Constants.NA.getValue());
				}
				
				if (StringUtils.isNotBlank(doc.getString(FieldType.CUST_ZIP.getName()))) {
					trans.setCustPin(doc.getString(FieldType.CUST_ZIP.getName()));
				} else {
					trans.setCustPin(Constants.NA.getValue());
				}
				
				if (StringUtils.isNotBlank(doc.getString(FieldType.CUST_COUNTRY.getName()))) {
					trans.setCustCountry(doc.getString(FieldType.CUST_COUNTRY.getName()));
				} else {
					trans.setCustCountry(Constants.NA.getValue());
				}
				
				//for Shipping Details
				
				if (StringUtils.isNotBlank(doc.getString(FieldType.CUST_SHIP_NAME.getName()))) {
					trans.setCustShippingName(doc.getString(FieldType.CUST_SHIP_NAME.getName()));
				} else {
					trans.setCustShippingName(Constants.NA.getValue());
				}
				
				if(StringUtils.isNotBlank(doc.getString(FieldType.CUST_SHIP_PHONE.getName()))) {
					trans.setCustShippingMobileNum(doc.getString(FieldType.CUST_SHIP_PHONE.getName()));
				} else {
					trans.setCustShippingMobileNum(Constants.NA.getValue());
				}
				
				if(StringUtils.isNotBlank(doc.getString(FieldType.CUST_SHIP_STREET_ADDRESS1.getName()))) {
					trans.setCustShippingAddress(doc.getString(FieldType.CUST_SHIP_STREET_ADDRESS1.getName()));
				} else {
					trans.setCustShippingAddress(Constants.NA.getValue());
				}
				
				if(StringUtils.isNotBlank(doc.getString(FieldType.CUST_SHIP_CITY.getName()))) {
					trans.setCustShippingCity(doc.getString(FieldType.CUST_SHIP_CITY.getName()));
				} else {
					trans.setCustShippingCity(Constants.NA.getValue());
				}
				
				if(StringUtils.isNotBlank(doc.getString(FieldType.CUST_SHIP_STATE.getName()))) {
					trans.setCustShippingState(doc.getString(FieldType.CUST_SHIP_STATE.getName()));
				} else {
					trans.setCustShippingState(Constants.NA.getValue());
				}
				
				if(StringUtils.isNotBlank(doc.getString(FieldType.CUST_SHIP_ZIP.getName()))) {
					trans.setCustShippingPin(doc.getString(FieldType.CUST_SHIP_ZIP.getName()));
				} else {
					trans.setCustShippingPin(Constants.NA.getValue());
				}
				
				if(StringUtils.isNotBlank(doc.getString(FieldType.CUST_SHIP_COUNTRY.getName()))) {
					trans.setCustShippingCountry(doc.getString(FieldType.CUST_SHIP_COUNTRY.getName()));
				} else {
					trans.setCustShippingCountry(Constants.NA.getValue());
				}
			}
		} catch(MongoException mongoException) {
			logger.error("Caught mongo exception while fetch data customerDetailsByOrderId from MongoDB : ", mongoException);
		}
		catch(Exception exception) {
			logger.error("caught exception while fetch data from customerDetailsByOrderId : ", exception);
		}
	}
	
	public TransactionDetails transactionDetailsByOrderId(String orderId, String txnType, UserType userType, TransactionDetails trans) {

		logger.info("Inside TransactionDetailsPopUpDao for get transactionDetails");
		try {
			BigDecimal totalAmount = new BigDecimal(0.00);
			BigDecimal amount = new BigDecimal(0.00);
			
			List<BasicDBObject> finalList = new ArrayList<BasicDBObject>();
			/*List<BasicDBObject> capturedList = new ArrayList<BasicDBObject>();
			List<BasicDBObject> settledList = new ArrayList<BasicDBObject>();
			List<BasicDBObject> mergeList = new ArrayList<BasicDBObject>();
			
			BasicDBObject capturedQuery = new BasicDBObject();
			BasicDBObject settledQuery = new BasicDBObject();
			BasicDBObject mergeQuery = new BasicDBObject();*/
			BasicDBObject finalQuery = new BasicDBObject();
			
			finalList.add(new BasicDBObject(FieldType.ORDER_ID.getName(), orderId));
			finalList.add(new BasicDBObject(FieldType.ORIG_TXNTYPE.getName(), txnType));
			/*capturedList.add(new BasicDBObject(FieldType.ORIG_TXNTYPE.getName(), txnType));
			capturedList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.CAPTURED.getName()));
			capturedQuery.put("$and", capturedList);
			
			settledList.add(new BasicDBObject(FieldType.ORIG_TXNTYPE.getName(), txnType));
			settledList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.SETTLED.getName()));
			settledQuery.put("$and", settledList);
			
			mergeList.add(capturedQuery);
			mergeList.add(settledQuery);
			
			mergeQuery.put("$or", mergeList);
			
			finalList.add(mergeQuery);*/
			finalQuery.put("$and", finalList);
			
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns
					.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));
			MongoCursor<Document> cursor = coll.find(finalQuery).iterator();
			if (cursor.hasNext() == false) {
				trans.setAmount(Constants.NA.getValue());
				trans.setTotalAmount(Constants.NA.getValue());
				trans.setPaymentType(Constants.NA.getValue());
				trans.setBankName(Constants.NA.getValue());
				trans.setRegion(Constants.NA.getValue());
				trans.setCardHolderType(Constants.NA.getValue());
				trans.setOrderId2(Constants.NA.getValue());
				trans.setPgRefNum2(Constants.NA.getValue());
				trans.setRrn(Constants.NA.getValue());
				trans.setCaptureResponseMessage(Constants.NA.getValue());
				trans.setSettleResponseMessage(Constants.NA.getValue());

				if (userType.toString().equalsIgnoreCase(UserType.MERCHANT.toString()) || userType.toString().equalsIgnoreCase(UserType.SUBUSER.toString())) {
					trans.setTdrORSurcharge(Constants.NA.getValue());
					trans.setGST(Constants.NA.getValue());
				} else if (userType.toString().equalsIgnoreCase(UserType.ADMIN.toString())
						|| userType.toString().equalsIgnoreCase(UserType.SUBADMIN.toString())) {

					trans.setAcquirerName(Constants.NA.getValue());
					trans.setPgCommission(Constants.NA.getValue());
					trans.setPgGST(Constants.NA.getValue());
					trans.setAcquirerCommission(Constants.NA.getValue());
					trans.setAcquirerGST(Constants.NA.getValue());

				}
			}
			while (cursor.hasNext()) {
				Document doc = cursor.next();

				if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
						&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {

					doc = dataEncDecTool.decryptDocument(doc);
				} 
				
				if (doc.getString(FieldType.STATUS.getName()).equals(StatusType.SETTLED.getName())) {
					trans.setSettleResponseMessage(doc.getString(FieldType.RESPONSE_MESSAGE.getName()));
					trans.setCaptureResponseMessage(doc.getString(FieldType.STATUS.getName()));
				} else {

					if(doc.getString(FieldType.ORIG_TXNTYPE.getName()).equalsIgnoreCase(TxnType.REFUND.getName())) {
						
						totalAmount = totalAmount.add(new BigDecimal(doc.getString(FieldType.TOTAL_AMOUNT.getName())));
						trans.setTotalAmount(totalAmount.toString());
						
						amount = amount.add(new BigDecimal(doc.getString(FieldType.AMOUNT.getName())));
						trans.setAmount(amount.toString());
						 
					} else {
						
						if (StringUtils.isNotBlank(doc.getString(FieldType.AMOUNT.getName()))) {
							trans.setAmount(doc.getString(FieldType.AMOUNT.getName()));
						} else {
							trans.setAmount(Constants.NA.getValue());
						}

						if (StringUtils.isNotBlank(doc.getString(FieldType.TOTAL_AMOUNT.getName()))) {
							trans.setTotalAmount(doc.getString(FieldType.TOTAL_AMOUNT.getName()));
						} else {
							trans.setTotalAmount(Constants.NA.getValue());
						}
						
					}
					
					if (StringUtils.isNotBlank(doc.getString(FieldType.PAYMENT_TYPE.getName()))) {
						trans.setPaymentType(doc.getString(FieldType.PAYMENT_TYPE.getName()));
					} else {
						trans.setPaymentType(Constants.NA.getValue());
					}

					if (StringUtils.isNotBlank(doc.getString(FieldType.INTERNAL_CARD_ISSUER_BANK.getName()))) {
						trans.setBankName(doc.getString(FieldType.INTERNAL_CARD_ISSUER_BANK.getName()));
					} else {
						trans.setBankName(Constants.NA.getValue());
					}
					if (StringUtils.isNotBlank(doc.getString(FieldType.PAYMENTS_REGION.getName()))) {
						trans.setRegion(doc.getString(FieldType.PAYMENTS_REGION.getName()));
					} else {
						trans.setRegion(Constants.NA.getValue());
					}
					if (StringUtils.isNotBlank(doc.getString(FieldType.CARD_HOLDER_TYPE.getName()))) {
						trans.setCardHolderType(doc.getString(FieldType.CARD_HOLDER_TYPE.getName()));
					} else {
						trans.setCardHolderType(Constants.NA.getValue());
					}
					if (StringUtils.isNotBlank(doc.getString(FieldType.ORDER_ID.getName()))) {
						trans.setOrderId2(doc.getString(FieldType.ORDER_ID.getName()));
					} else {
						trans.setOrderId2(Constants.NA.getValue());
					}
					if (StringUtils.isNotBlank(doc.getString(FieldType.PG_REF_NUM.getName()))) {
						trans.setPgRefNum2(doc.getString(FieldType.PG_REF_NUM.getName()));
					} else {
						trans.setPgRefNum2(Constants.NA.getValue());
					}

					if (StringUtils.isNotBlank(doc.getString(FieldType.RRN.getName()))) {
						trans.setRrn(doc.getString(FieldType.RRN.getName()));
					} else {
						trans.setRrn(Constants.NA.getValue());
					}

					if (StringUtils.isNotBlank(doc.getString(FieldType.RESPONSE_MESSAGE.getName()))) {
						trans.setCaptureResponseMessage(doc.getString(FieldType.STATUS.getName()));
					} else {
						trans.setCaptureResponseMessage(Constants.NA.getValue());
					}
						trans.setSettleResponseMessage(Constants.NA.getValue());

					if (userType.toString().equalsIgnoreCase(UserType.MERCHANT.toString()) || userType.toString().equalsIgnoreCase(UserType.SUBUSER.toString())) {

						if (StringUtils.isNotBlank(doc.getString(FieldType.PG_TDR_SC.getName()))) {
							trans.setPgCommission(doc.getString(FieldType.PG_TDR_SC.getName()));
						} else {
							trans.setPgCommission(Constants.NA.getValue());
						}

						if (StringUtils.isNotBlank(doc.getString(FieldType.PG_GST.getName()))) {
							trans.setPgGST(doc.getString(FieldType.PG_GST.getName()));
						} else {
							trans.setPgGST(Constants.NA.getValue());
						}

					} else if (userType.toString().equalsIgnoreCase(UserType.ADMIN.toString()) || userType.toString().equalsIgnoreCase(UserType.SUBADMIN.toString())) {

						if (StringUtils.isNotBlank(doc.getString(FieldType.ACQUIRER_TDR_SC.getName()))) {
							trans.setAcquirerCommission(doc.getString(FieldType.ACQUIRER_TDR_SC.getName()));
						} else {
							trans.setAcquirerCommission(Constants.NA.getValue());
						}

						if (StringUtils.isNotBlank(doc.getString(FieldType.ACQUIRER_GST.getName()))) {
							trans.setAcquirerGST(doc.getString(FieldType.ACQUIRER_GST.getName()));
						} else {
							trans.setAcquirerGST(Constants.NA.getValue());
						}

						if (StringUtils.isNotBlank(doc.getString(FieldType.PG_TDR_SC.getName()))) {
							trans.setPgCommission(doc.getString(FieldType.PG_TDR_SC.getName()));
						} else {
							trans.setPgCommission(Constants.NA.getValue());
						}

						if (StringUtils.isNotBlank(doc.getString(FieldType.PG_GST.getName()))) {
							trans.setPgGST(doc.getString(FieldType.PG_GST.getName()));
						} else {
							trans.setPgGST(Constants.NA.getValue());
						}

						String acquirerName = AcquirerType
								.getAcquirerName(doc.getString(FieldType.ACQUIRER_TYPE.getName()));

						if (StringUtils.isNotBlank(acquirerName)) {
							trans.setAcquirerName(acquirerName);
						} else {
							trans.setAcquirerName(Constants.NA.getValue());
						}
					}
				}
			}
		} catch(Exception ex) {
			logger.error("Caught Exception in transction Details in popUp Dao : ", ex);
		}
		return trans;
	}
	
	public List<TransactionDetails> getDataByOID(String txnId, User user){
	
		logger.info("Inside TransactionDetailsPopUpDao for get transactionDetails");
		try {
			List<TransactionDetails> transactionList = new ArrayList<TransactionDetails>();
			
			UserSettingData merchantSettings=userSettingDao.fetchDataUsingPayId(user.getPayId());

			BasicDBObject finalQuery = new BasicDBObject();
			finalQuery.put(FieldType.TXN_ID.getName(), txnId);
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns
					.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));
			MongoCursor<Document> cursor = coll.find(finalQuery).iterator();
			String oid;
			Document dbobj = cursor.next();
			
			if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
					&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {

				dbobj = dataEncDecTool.decryptDocument(dbobj);
			} 
			
			oid=dbobj.getString(FieldType.OID.getName());
			cursor.close();
			
			BasicDBObject finalQuery1 = new BasicDBObject();
			finalQuery1.put(FieldType.OID.getName(), oid);
			BasicDBObject match = new BasicDBObject("$match", finalQuery1);
			BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject("CREATE_DATE", -1));

			List<BasicDBObject> pipeline = Arrays.asList(match, sort/* , group */);
			AggregateIterable<Document> output = coll.aggregate(pipeline);
			output.allowDiskUse(true);
			MongoCursor<Document> cursor1 = output.iterator();
			
			while(cursor1.hasNext()) {
				TransactionDetails transList = new TransactionDetails();
				Document dbob = cursor1.next();
				
				if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
						&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {

					dbob = dataEncDecTool.decryptDocument(dbob);
				} 
				
				if (StringUtils.isNotBlank(dbob.getString(FieldType.CREATE_DATE.getName()))) {
					transList.setDate(dbob.getString(FieldType.CREATE_DATE.getName()));
				} else {
					transList.setDate(Constants.NA.getValue());
				}
				
				if (StringUtils.isNotBlank(dbob.getString(FieldType.PG_REF_NUM.getName()))) {
					transList.setPgRefNum2(dbob.getString(FieldType.PG_REF_NUM.getName()));
				} else {
					transList.setPgRefNum2(Constants.NA.getValue());
				}
				
				if (StringUtils.isNotBlank(dbob.getString(FieldType.ORDER_ID.getName()))) {
					transList.setOrderId2(dbob.getString(FieldType.ORDER_ID.getName()));
				} else {
					transList.setOrderId2(Constants.NA.getValue());
				}
				
				if (StringUtils.isNotBlank(dbob.getString(FieldType.AMOUNT.getName()))) {
					transList.setAmount(dbob.getString(FieldType.AMOUNT.getName()));
				} else {
					transList.setAmount(Constants.NA.getValue());
				}
				
				if ((StringUtils.isNotBlank(dbob.getString(FieldType.TXNTYPE.getName())))
						&& (dbob.getString(FieldType.TXNTYPE.toString())
								.equalsIgnoreCase(TransactionType.REFUND.getName()))) {
					transList.setTxnType(dbob.getString(FieldType.TXNTYPE.toString()));
				} else{
					transList.setTxnType(dbob.getString(FieldType.ORIG_TXNTYPE.toString()));
				}
				
				if (((user.getUserType().equals(UserType.MERCHANT))
						|| (user.getUserType().equals(UserType.SUBUSER))) && merchantSettings.isCustomTransactionStatus()) {
					if (((dbob.getString(FieldType.STATUS.toString()))
							.equalsIgnoreCase(StatusType.TIMEOUT.getName()))
							|| ((dbob.getString(FieldType.STATUS.toString()))
									.equalsIgnoreCase(StatusType.CAPTURED.getName()))
							|| ((dbob.getString(FieldType.STATUS.toString()))
									.equalsIgnoreCase(StatusType.ENROLLED.getName()))
							|| ((dbob.getString(FieldType.STATUS.toString()))
									.equalsIgnoreCase(StatusType.SETTLED.getName()))
							|| ((dbob.getString(FieldType.STATUS.toString()))
									.equalsIgnoreCase(StatusType.SENT_TO_BANK.getName()))
							|| ((dbob.getString(FieldType.STATUS.toString()))
									.equalsIgnoreCase(StatusType.PENDING.getName()))) {
						transList.setStatus(dbob.getString(FieldType.STATUS.toString()));
					} else {
						transList.setStatus("Failed");
					}
				} else {
					transList.setStatus(dbob.getString(FieldType.STATUS.getName()));
				}
				
				transactionList.add(transList);
			}
			
			logger.info("transactionList created and size = " + transactionList.size());
			cursor1.close();
			return transactionList;
			
		}catch(Exception e) {
			logger.error("exception caugth in popup : " , e);
			e.printStackTrace();
			return null;
		}
			
	}

	
}
