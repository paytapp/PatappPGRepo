package com.paymentgateway.payout;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.paymentgateway.commons.dao.ChargebackDao;
import com.paymentgateway.commons.dao.SUFDetailDao;
import com.paymentgateway.commons.dao.UserSettingDao;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.mongo.MongoInstance;
import com.paymentgateway.commons.user.Chargeback;
import com.paymentgateway.commons.user.SUFDetail;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserSettingData;
import com.paymentgateway.commons.util.Amount;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.DataEncDecTool;
import com.paymentgateway.commons.util.DateCreater;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StatusType;

/**
 * @author Shiva
 *
 */
@Service
public class VendorPayoutDao {

	@Autowired
	private MongoInstance mongoInstance;

	@Autowired
	private ChargebackDao chargebackDao;

	@Autowired
	private UserDao userDao;
	
	@Autowired
	private DataEncDecTool dataEncDecTool;
	
	@Autowired
	private PropertiesManager propertiesManager;
	
	@Autowired
	private SUFDetailDao sufDetailDao;
	
	@Autowired
	private UserSettingDao userSettingDao;

	private static final String prefix = "MONGO_DB_";

	private static Logger logger = LoggerFactory.getLogger(VendorPayoutDao.class.getName());

	public BigDecimal getSettleTransactionDiff(Fields fields) {
		logger.info("Inside  getSettleTransactionDiff()");
		BigDecimal diffrenceAmount = new BigDecimal("0").setScale(2);
		try {

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns
					.getCollection(PropertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));
			BasicDBObject query = new BasicDBObject();

			String payId = fields.get(FieldType.PAY_ID.getName());
			String subMerchantId = null;

			BigDecimal saleAmount = new BigDecimal("0").setScale(2);
			BigDecimal refundAmount = new BigDecimal("0").setScale(2);

			User user = userDao.findPayId(payId);

			if (StringUtils.isNotBlank(user.getSuperMerchantId())) {
				subMerchantId = user.getPayId();
				payId = user.getSuperMerchantId();
			}

			if (StringUtils.isNotBlank(payId)) {
				query.append(FieldType.PAY_ID.getName(), payId);
			}

			if (StringUtils.isNotBlank(subMerchantId)) {
				query.append(FieldType.SUB_MERCHANT_ID.getName(), subMerchantId);
			}

			query.append(FieldType.STATUS.getName(), StatusType.SETTLED.getName());
			query.append(FieldType.DATE_INDEX.getName(),
					DateCreater.formatDateForDb(new Date()).substring(0, 10).replace("-", ""));

			MongoCursor<Document> cursor = coll.find(query).iterator();

			while (cursor.hasNext()) {
				Document dbobj = cursor.next();
				if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
                        && propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
                    dbobj = dataEncDecTool.decryptDocument(dbobj);
                }
				
				BigDecimal dbAmount = new BigDecimal(dbobj.getString(FieldType.TOTAL_AMOUNT.getName())).setScale(2);
				BigDecimal pgTdr = new BigDecimal(dbobj.getString(FieldType.PG_TDR_SC.getName())).setScale(2);
				BigDecimal pgGst = new BigDecimal(dbobj.getString(FieldType.PG_GST.getName())).setScale(2);
				BigDecimal acqTdr = new BigDecimal(dbobj.getString(FieldType.ACQUIRER_TDR_SC.getName())).setScale(2);
				BigDecimal acqGst = new BigDecimal(dbobj.getString(FieldType.ACQUIRER_GST.getName())).setScale(2);

				if (StringUtils.isBlank(String.valueOf(pgTdr))) {
					pgTdr.add(BigDecimal.ZERO);
				}
				if (StringUtils.isBlank(String.valueOf(pgGst))) {
					pgGst.add(BigDecimal.ZERO);
				}
				if (StringUtils.isBlank(String.valueOf(acqTdr))) {
					acqTdr.add(BigDecimal.ZERO);
				}
				if (StringUtils.isBlank(String.valueOf(acqGst))) {
					acqGst.add(BigDecimal.ZERO);
				}

				if (dbobj.getString(FieldType.TXNTYPE.getName()).equalsIgnoreCase("RECO")) {

					saleAmount = saleAmount.add(dbAmount).subtract(pgTdr).subtract(pgGst).subtract(acqTdr)
							.subtract(acqGst);
				} else if (dbobj.getString(FieldType.TXNTYPE.getName()).equalsIgnoreCase("RECOREFUND")) {

					refundAmount = refundAmount.add(dbAmount).subtract(pgTdr).subtract(pgGst).subtract(acqTdr)
							.subtract(acqGst);
				}

			}
			diffrenceAmount = saleAmount.subtract(refundAmount);
			logger.info("Amount after Settled (Sale-Refund) = " + diffrenceAmount);
		} catch (Exception e) {
			logger.error("Exception caugth while fetching transaction settled amount " , e);
		}
		return diffrenceAmount;

	}

	public BigDecimal getImpsTransactionAmount(Fields fields) {
		logger.info("Inside getImpsTransactionAmount()");
		BigDecimal totalAmount = new BigDecimal("0").setScale(2);
		try {

			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			String todayDate = sdf.format(new Date());

			String dateFrom = todayDate + " " + "00:00:00";
			String dateTo = todayDate + " " + "23:59:59";

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.IMPS_SETTlEMENT_COLLECTION.getValue()));
			BasicDBObject query = new BasicDBObject();

			query.append(FieldType.CREATE_DATE.getName(),
					BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(dateFrom).toLocalizedPattern())
							.add("$lte", new SimpleDateFormat(dateTo).toLocalizedPattern()).get());

			String payId = fields.get(FieldType.PAY_ID.getName());
			String subMerchantId = null;

			BigDecimal settledAmount = new BigDecimal("0").setScale(2);

			User user = userDao.findPayId(payId);

			if (StringUtils.isNotBlank(user.getSuperMerchantId())) {
				subMerchantId = user.getPayId();
				payId = user.getSuperMerchantId();
			}

			if (StringUtils.isNotBlank(payId)) {
				query.append(FieldType.PAY_ID.getName(), payId);
			}

			if (StringUtils.isNotBlank(subMerchantId)) {
				query.append(FieldType.SUB_MERCHANT_ID.getName(), subMerchantId);
			}

			query.append(FieldType.STATUS.getName(), StatusType.CAPTURED.getName());

			MongoCursor<Document> cursor = coll.find(query).iterator();

			while (cursor.hasNext()) {
				Document dbobj = cursor.next();
				if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
                        && propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
                    dbobj = dataEncDecTool.decryptDocument(dbobj);
                }
				
				BigDecimal dbAmount = new BigDecimal(dbobj.getString(FieldType.AMOUNT.getName())).setScale(2);
				settledAmount = settledAmount.add(dbAmount);
			}
			totalAmount = settledAmount;
			logger.info("Total today's Transactions Amount  " + totalAmount);
		} catch (Exception e) {
			logger.error("Exception caugth fetching imps transaction : " , e);
		}
		return totalAmount;

	}

	public BigDecimal getChargebackTransactionAmount(Fields fields) {
		logger.info("inside getChargebackTransactionAmount()");
		List<Chargeback> totalChargeBackData = null;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		BigDecimal chargebackAmount = new BigDecimal(0).setScale(2);

		try {
			String todayDate = sdf.format(new Date());

			String dateFrom = todayDate + " " + "00:00:00";
			String dateTo = todayDate + " " + "23:59:59";

			String payId = fields.get(FieldType.PAY_ID.getName());
			String subMerchantId = null;

			User user = userDao.findPayId(payId);

			if (StringUtils.isNotBlank(user.getSuperMerchantId())) {
				subMerchantId = payId;
				payId = user.getSuperMerchantId();
			}

			totalChargeBackData = chargebackDao.findChargebackByPayid(payId, dateFrom,
					dateTo);/*
							 * (dateFrom, dateTo, payId, subMerchantId, "ALL",
							 * "Accepted");
							 */

			if (totalChargeBackData != null) {
				for (Chargeback c : totalChargeBackData) {
					if (StringUtils.isNotBlank(subMerchantId)) {
						if (c.getSubMerchantId().equals(subMerchantId) && (c.getStatus().equalsIgnoreCase("Accepted")
								|| c.getStatus().equalsIgnoreCase("Open")))
							chargebackAmount = chargebackAmount.add(new BigDecimal(c.getCapturedAmount()));
					} else {
						if (c.getStatus().equalsIgnoreCase("Accepted") || c.getStatus().equalsIgnoreCase("Open"))
							chargebackAmount = chargebackAmount.add(new BigDecimal(c.getCapturedAmount()));
					}
				}
			}
			logger.info("Total chargeback amount is " + chargebackAmount);
		} catch (Exception e) {
			logger.info("Exception : " , e);
		}
		return chargebackAmount;

	}

	public boolean checkDuplicateAccountNumber(Fields fields) {
		logger.info("Inside  checkDuplicateAccountNumber()");

		try {

			Date dNow = new Date();
			String dateNow = DateCreater.formatDateForDb(dNow);

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.IMPS_SETTlEMENT_COLLECTION.getValue()));
			BasicDBObject query = new BasicDBObject();

			String bankAccountNumber = fields.get(FieldType.BENE_ACCOUNT_NO.getName());
			String ifscCode = fields.get(FieldType.IFSC_CODE.getName());
			if (StringUtils.isNotBlank(bankAccountNumber)) {
				query.append(FieldType.BENE_ACCOUNT_NO.getName(), bankAccountNumber);
				query.append(FieldType.IFSC_CODE.getName(), ifscCode);
			}

			List<BasicDBObject> statusListQuery = new ArrayList<BasicDBObject>();

			statusListQuery.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.CAPTURED.getName()));
			statusListQuery.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.TIMEOUT.getName()));
			query.append("$or", statusListQuery);
//			query.append(FieldType.USER_TYPE.getName(), "Verification");    

			MongoCursor<Document> cursor = coll.find(query).iterator();

			while (cursor.hasNext()) {
				logger.info("found Duplicate Account number for "+bankAccountNumber);
				Document dbobj = cursor.next();
				if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
                        && propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
                    dbobj = dataEncDecTool.decryptDocument(dbobj);
                }
				
				String status = dbobj.get(FieldType.STATUS.getName()).toString();

				fields.put(FieldType.STATUS.getName(), dbobj.getString(FieldType.STATUS.getName()));
				fields.put(FieldType.PG_RESP_CODE.getName(), dbobj.getString(FieldType.PG_RESP_CODE.getName()));
				fields.put(FieldType.TXNTYPE.getName(), dbobj.getString(FieldType.TXNTYPE.getName()));
				fields.put(FieldType.CURRENCY_CODE.getName(), dbobj.getString(FieldType.CURRENCY_CODE.getName()));
				fields.put(FieldType.PG_TXN_MESSAGE.getName(), dbobj.getString(FieldType.PG_TXN_MESSAGE.getName()));
				fields.put(FieldType.AMOUNT.getName(), dbobj.getString(FieldType.AMOUNT.getName()));
				fields.put(FieldType.RESPONSE_CODE.getName(), dbobj.getString(FieldType.RESPONSE_CODE.getName()));
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), dbobj.getString(FieldType.RESPONSE_MESSAGE.getName()));
				fields.put(FieldType.RRN.getName(), dbobj.getString(FieldType.RRN.getName()));
				fields.put(FieldType.BENE_NAME.getName(), dbobj.getString(FieldType.BENE_NAME.getName()));

				return true;
			}
			cursor.close();

		} catch (Exception e) {
			logger.error("Exception caugth while fetching transaction settled amount : " , e);
		}
		return false;

	}
	
	public boolean checkDuplicateVPA(Fields fields) {
		logger.info("Inside  checkDuplicateVPA()");

		try {

			Date dNow = new Date();
			String dateNow = DateCreater.formatDateForDb(dNow);

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.BENE_VERIFICATION_COLLECTION.getValue()));
			BasicDBObject query = new BasicDBObject();

			String vpa = fields.get(FieldType.PAYER_ADDRESS.getName());
			
			if (StringUtils.isNotBlank(vpa)) {
				query.append(FieldType.PAYER_ADDRESS.getName(), vpa);
			}

			List<BasicDBObject> statusListQuery = new ArrayList<BasicDBObject>();

			statusListQuery.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.CAPTURED.getName()));
			statusListQuery.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.TIMEOUT.getName()));
			query.append("$or", statusListQuery);
			query.append(FieldType.USER_TYPE.getName(), "Verification");

			MongoCursor<Document> cursor = coll.find(query).iterator();

			while (cursor.hasNext()) {
				logger.info("found Duplicate VPA for "+vpa);
				Document dbobj = cursor.next();
				if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
                        && propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
                    dbobj = dataEncDecTool.decryptDocument(dbobj);
                }
				
				String status = dbobj.get(FieldType.STATUS.getName()).toString();

				fields.put(FieldType.STATUS.getName(), dbobj.getString(FieldType.STATUS.getName()));
				fields.put(FieldType.PG_RESP_CODE.getName(), dbobj.getString(FieldType.PG_RESP_CODE.getName()));
				fields.put(FieldType.TXNTYPE.getName(), dbobj.getString(FieldType.TXNTYPE.getName()));
				fields.put(FieldType.CURRENCY_CODE.getName(), dbobj.getString(FieldType.CURRENCY_CODE.getName()));
				fields.put(FieldType.PG_TXN_MESSAGE.getName(), dbobj.getString(FieldType.PG_TXN_MESSAGE.getName()));
				fields.put(FieldType.AMOUNT.getName(), dbobj.getString(FieldType.AMOUNT.getName()));
				fields.put(FieldType.RESPONSE_CODE.getName(), dbobj.getString(FieldType.RESPONSE_CODE.getName()));
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), dbobj.getString(FieldType.RESPONSE_MESSAGE.getName()));
				fields.put(FieldType.RRN.getName(), dbobj.getString(FieldType.RRN.getName()));

				return true;
			}
			cursor.close();

		} catch (Exception e) {
			logger.error("Exception caugth while fetching transaction settled amount : " , e);
		}
		return false;

	}

	public void getBeneData(Fields fields) {
		logger.info("Inside  getBeneData()");

		try {

			Date dNow = new Date();
			String dateNow = DateCreater.formatDateForDb(dNow);

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.IMPS_SETTlEMENT_COLLECTION.getValue()));
			BasicDBObject query = new BasicDBObject();

			String bankAccountNumber = fields.get(FieldType.BENE_ACCOUNT_NO.getName());
			String orderId = fields.get(FieldType.ORDER_ID.getName());
			String payId = fields.get(FieldType.PAY_ID.getName());
			String subMerchantId = null;

			User user = userDao.findPayId(payId);

			if (StringUtils.isNotBlank(user.getSuperMerchantId())) {
				subMerchantId = user.getPayId();
				payId = user.getSuperMerchantId();
			}

			if (StringUtils.isNotBlank(payId)) {
				query.append(FieldType.PAY_ID.getName(), payId);
			}

			if (StringUtils.isNotBlank(subMerchantId)) {
				query.append(FieldType.SUB_MERCHANT_ID.getName(), subMerchantId);
			}

			query.append(FieldType.BENE_ACCOUNT_NO.getName(), bankAccountNumber);
			query.append(FieldType.ORDER_ID.getName(), orderId);

			query.append(FieldType.USER_TYPE.getName(), "Verification");

			BasicDBObject match = new BasicDBObject("$match", query);
			BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject(FieldType.CREATE_DATE.name(), -1));
			BasicDBObject limit = new BasicDBObject("$limit", 1);
			List<BasicDBObject> pipeline = Arrays.asList(match, sort, limit);
			AggregateIterable<Document> output = coll.aggregate(pipeline);
			output.allowDiskUse(true);
			MongoCursor<Document> cursor = output.iterator();
			if (!cursor.hasNext()) {
				fields.put(FieldType.STATUS.getName(), StatusType.FAILED.getName());
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.NO_SUCH_TRANSACTION.getResponseMessage());
				fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.NO_SUCH_TRANSACTION.getCode());
			}

			while (cursor.hasNext()) {
				Document dbobj = cursor.next();
				if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
                        && propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
                    dbobj = dataEncDecTool.decryptDocument(dbobj);
                }
				
				String status = dbobj.get(FieldType.STATUS.getName()).toString();

				if (status.equalsIgnoreCase(StatusType.CAPTURED.getName())) {
					fields.put(FieldType.STATUS.getName(), StatusType.VERIFIED.getName());
				} else if (status.equalsIgnoreCase(StatusType.TIMEOUT.getName())) {
					fields.put(FieldType.STATUS.getName(), StatusType.PENDING.getName());
				} else {
					fields.put(FieldType.STATUS.getName(), StatusType.FAILED.getName());
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TXN_MESSAGE.getName())))
					fields.put(FieldType.RESPONSE_MESSAGE.getName(),
							dbobj.getString(FieldType.PG_TXN_MESSAGE.getName()));
				else
					fields.put(FieldType.RESPONSE_MESSAGE.getName(), fields.get(FieldType.STATUS.getName()));

				fields.put(FieldType.RESPONSE_CODE.getName(), dbobj.getString(FieldType.RESPONSE_CODE.getName()));

			}
			cursor.close();

		} catch (Exception e) {
			logger.error("Exception caugth while fetching transaction settled amount : " , e);
		}

	}

	public void getVendorPayoutData(Fields fields) {
		logger.info("Inside  getVendorPayoutData()");

		try {

			logger.info("Inside  VendorPayoutData()");
			Date dNow = new Date();
			String dateNow = DateCreater.formatDateForDb(dNow);

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.IMPS_SETTlEMENT_COLLECTION.getValue()));
			BasicDBObject query = new BasicDBObject();

			String bankAccountNumber = fields.get(FieldType.BENE_ACCOUNT_NO.getName());
			String vpa = fields.get(FieldType.PAYER_ADDRESS.getName());
			String orderId = fields.get(FieldType.ORDER_ID.getName());
			String payId = fields.get(FieldType.PAY_ID.getName());
			String amount = fields.get(FieldType.AMOUNT.getName());
			String subMerchantId = null;

			User user = userDao.findPayId(payId);

			if (StringUtils.isNotBlank(user.getSuperMerchantId())) {
				subMerchantId = user.getPayId();
				payId = user.getSuperMerchantId();
			}

			if (StringUtils.isNotBlank(payId)) {
				query.append(FieldType.PAY_ID.getName(), payId);
			}

			if (StringUtils.isNotBlank(subMerchantId)) {
				query.append(FieldType.SUB_MERCHANT_ID.getName(), subMerchantId);
			}

			if (StringUtils.isNotBlank(bankAccountNumber)) {
				query.append(FieldType.BENE_ACCOUNT_NO.getName(), bankAccountNumber);
			} else if (StringUtils.isNotBlank(vpa)) {
				query.append(FieldType.PAYER_ADDRESS.getName(), vpa);
			}
			query.append(FieldType.ORDER_ID.getName(), orderId);
			query.append(FieldType.AMOUNT.getName(), amount);

			

			BasicDBObject match = new BasicDBObject("$match", query);
			BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject(FieldType.CREATE_DATE.name(), -1));
			BasicDBObject limit = new BasicDBObject("$limit", 1);
			List<BasicDBObject> pipeline = Arrays.asList(match, sort, limit);
			AggregateIterable<Document> output = coll.aggregate(pipeline);
			output.allowDiskUse(true);
			MongoCursor<Document> cursor = output.iterator();
			if (!cursor.hasNext()) {
				fields.put(FieldType.STATUS.getName(), StatusType.FAILED.getName());
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.NO_SUCH_TRANSACTION.getResponseMessage());
				fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.NO_SUCH_TRANSACTION.getCode());
			}

			while (cursor.hasNext()) {
				Document dbobj = cursor.next();
				if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
                        && propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
                    dbobj = dataEncDecTool.decryptDocument(dbobj);
                }
				
				String status = dbobj.get(FieldType.STATUS.getName()).toString();

				fields.put(FieldType.RESPONSE_MESSAGE.getName(), dbobj.getString(FieldType.RESPONSE_MESSAGE.getName()));
				fields.put(FieldType.PG_TXN_MESSAGE.getName(), dbobj.getString(FieldType.PG_TXN_MESSAGE.getName()));
				fields.put(FieldType.STATUS.getName(), dbobj.getString(FieldType.STATUS.getName()));

				fields.put(FieldType.RESPONSE_CODE.getName(), dbobj.getString(FieldType.RESPONSE_CODE.getName()));

			}
			logger.info("Fetch the transaction for vendor payout status enquiry..");
			cursor.close();

		} catch (Exception e) {
			logger.error("Exception caugth while fetching transaction settled amount : " , e);
		}

	}
	
	/*
	 * public BigDecimal getImpsTransactionAmount(Fields fields){
	 * logger.info("Inside getImpsTransactionAmount()"); BigDecimal totalAmount=new
	 * BigDecimal("0").setScale(2); try {
	 * 
	 * SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd"); String
	 * todayDate=sdf.format(new Date());
	 * 
	 * String dateFrom=todayDate+" "+"00:00:00"; String
	 * dateTo=todayDate+" "+"23:59:59";
	 * 
	 * MongoDatabase dbIns = mongoInstance.getDB(); MongoCollection<Document> coll =
	 * dbIns.getCollection( PropertiesManager.propertiesMap.get(prefix +
	 * Constants.IMPS_SETTlEMENT_COLLECTION.getValue())); BasicDBObject query = new
	 * BasicDBObject();
	 * 
	 * 
	 * 
	 * query.append(FieldType.CREATE_DATE.getName(),
	 * BasicDBObjectBuilder.start("$gte", new
	 * SimpleDateFormat(dateFrom).toLocalizedPattern()) .add("$lte", new
	 * SimpleDateFormat(dateTo).toLocalizedPattern()).get());
	 * 
	 * 
	 * String payId=fields.get(FieldType.PAY_ID.getName()); String
	 * subMerchantId=null;
	 * 
	 * BigDecimal settledAmount=new BigDecimal("0").setScale(2);
	 * 
	 * User user=userDao.findPayId(payId);
	 * 
	 * if(StringUtils.isNotBlank(user.getSuperMerchantId())) {
	 * subMerchantId=user.getPayId(); payId=user.getSuperMerchantId(); }
	 * 
	 * if (StringUtils.isNotBlank(payId)) { query.append(FieldType.PAY_ID.getName(),
	 * payId); }
	 * 
	 * if (StringUtils.isNotBlank(subMerchantId)) {
	 * query.append(FieldType.SUB_MERCHANT_ID.getName(),subMerchantId); }
	 * 
	 * 
	 * query.append(FieldType.STATUS.getName(), StatusType.CAPTURED.getName());
	 * 
	 * MongoCursor<Document> cursor = coll.find(query).iterator();
	 * 
	 * while (cursor.hasNext()) { Document dbobj = cursor.next(); BigDecimal
	 * dbAmount=new
	 * BigDecimal(dbobj.getString(FieldType.AMOUNT.getName())).setScale(2);
	 * settledAmount=settledAmount.add(dbAmount); } totalAmount=settledAmount;
	 * logger.info("Total today's Transactions Amount  "+totalAmount); } catch
	 * (Exception e) { logger.error("Exception caugth fetching imps transaction, " +
	 * e); } return totalAmount;
	 * 
	 * 
	 * }
	 */
	
	public BigDecimal getECollectionTransactionAmount(Fields fields){
		logger.info("Inside getECollectionTransactionAmount()");
		BigDecimal totalAmount=new BigDecimal("0").setScale(2);
		try {
			
			BigDecimal closingAmount=new BigDecimal("0").setScale(2);
						
			 SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd"); 
			 String todayDate=sdf.format(new Date());
			 
			 String dateFrom=todayDate+" "+"00:00:00"; 
			 String dateTo=todayDate+" "+"23:59:59";
			 
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.E_COLLECTION.getValue()));
			BasicDBObject query = new BasicDBObject();
			BasicDBObject queryClosing = new BasicDBObject();
			
			query.append(FieldType.CREATE_DATE.getName(), 
					BasicDBObjectBuilder.start("$gte", new  SimpleDateFormat(dateFrom).toLocalizedPattern()) .add("$lte", new SimpleDateFormat(dateTo).toLocalizedPattern()).get());
			String payId=fields.get(FieldType.PAY_ID.getName());
			String subMerchantId=null;
			
			BigDecimal settledAmount=new BigDecimal("0").setScale(2);
			
			User user=userDao.findPayId(payId);
			
			if(StringUtils.isNotBlank(user.getSuperMerchantId()))
			{
				subMerchantId=user.getPayId();
				payId=user.getSuperMerchantId();
			}
			
			if (StringUtils.isNotBlank(payId)) {
				query.append(FieldType.PAY_ID.getName(), payId);
			}

			if (StringUtils.isNotBlank(subMerchantId)) {
				query.append(FieldType.SUB_MERCHANT_ID.getName(),subMerchantId);
			}
		
			query.append(FieldType.STATUS.getName(), StatusType.CAPTURED.getName());
			MongoCursor<Document> cursor = coll.find(query).iterator();

			while (cursor.hasNext()) {
				Document dbobj = cursor.next();
				if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
                        && propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
                    dbobj = dataEncDecTool.decryptDocument(dbobj);
                }
				
				BigDecimal dbAmount=new BigDecimal(dbobj.getString(FieldType.AMOUNT.getName())).setScale(2);
				settledAmount=settledAmount.add(dbAmount);
			}
			cursor.close();
			logger.info("settledAmount Amount  "+ settledAmount);
			MongoDatabase dbIns1 = mongoInstance.getDB();
			MongoCollection<Document> coll1 = dbIns1.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.CLOSING_AMOUNT_COLLECTION.getValue()));
			
			
		        queryClosing.append(FieldType.VIRTUAL_AC_CODE.getName(),user.getVirtualAccountNo());
		        
		        BasicDBObject match = new BasicDBObject("$match", queryClosing);
				BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject(FieldType.CLOSING_DATE.name(), -1));
				BasicDBObject limit = new BasicDBObject("$limit",1);
				List<BasicDBObject> pipeline2 = Arrays.asList(match, sort,limit);
				AggregateIterable<Document> output2 = coll1.aggregate(pipeline2);
				output2.allowDiskUse(true);
				MongoCursor<Document> cursor2 = output2.iterator();
		        
		        while (cursor2.hasNext()) {
					Document dbobj = cursor2.next();
					BigDecimal dbAmount=new BigDecimal(dbobj.getString(FieldType.AMOUNT.getName())).setScale(2);
					closingAmount=closingAmount.add(dbAmount);
				}
		        
		        cursor2.close();
		        
			totalAmount= closingAmount.add(settledAmount);
			logger.info("Total today's Transactions Amount  "+totalAmount);
		} catch (Exception e) {
			logger.error("Exception caugth fetching imps transaction : " , e);
		}
		return totalAmount;
		
		
	}
	
	public void insertUpdateForClosing(Fields fields) throws ParseException {
		logger.info("Inside insertUpdateForClosing(), VendorPayoutDao");
		try {
		String fieldAmount = Amount.toDecimal(fields.get(FieldType.AMOUNT.getName()),
				fields.get(FieldType.CURRENCY_CODE.getName()));
		MongoDatabase dbIns1 = mongoInstance.getDB();
		MongoCollection<Document> collection1 = dbIns1
				.getCollection(PropertiesManager.propertiesMap.get(prefix + Constants.CLOSING_AMOUNT_COLLECTION.getValue()));
		BasicDBObject newFieldsObj1 = new BasicDBObject();
		
		Date dNow = new Date();
		String dateNow = DateCreater.formatDateForDb(dNow);
		  SimpleDateFormat inputDate= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		  SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		  
		  String dtDate = dateFormat.format(inputDate.parse(dateNow));
		  
			DateFormat dateFormatIndex = new SimpleDateFormat("yyyyMMdd");
			String todaysDateIndex = dateFormatIndex.format(dNow.getTime());
		  
		String dateFrom=dtDate+" "+"00:00:00"; 
		String toDate=dtDate+" "+"23:59:59";
		newFieldsObj1.append(FieldType.CREATE_DATE.getName(), 
						BasicDBObjectBuilder.start("$gte", new  SimpleDateFormat(dateFrom).toLocalizedPattern()) .add("$lte", new SimpleDateFormat(toDate).toLocalizedPattern()).get());
		newFieldsObj1.append(FieldType.DATE_INDEX.getName(),todaysDateIndex);
		User user = userDao.findPayId(fields.get(FieldType.PAY_ID.getName()));
		if(user.isSuperMerchant() == false && StringUtils.isNotBlank(user.getSuperMerchantId())) {
			newFieldsObj1.append(FieldType.SUB_MERCHANT_ID.getName(),fields.get(FieldType.PAY_ID.getName())); 	
		}else {
		newFieldsObj1.append(FieldType.PAY_ID.getName(),fields.get(FieldType.PAY_ID.getName())); 
		}
		 MongoCursor<Document> cursor = collection1.find(newFieldsObj1).iterator();
		 BigDecimal closingAmount=new BigDecimal("0").setScale(2);
		 BigDecimal openingAmount=new BigDecimal("0").setScale(2);
		 BigDecimal impsAmount=new BigDecimal("0").setScale(2);
		 BigDecimal creditAmount=new BigDecimal("0").setScale(2);
		 BigDecimal debitAmount=new BigDecimal("0").setScale(2);
		 boolean flag = false;
		 while(cursor.hasNext()) {
			 flag = true;
			Document dbobj = cursor.next();
			logger.info("orginal request for closing collection  ,virtual account code  "
					+ dbobj.getString(FieldType.VIRTUAL_AC_CODE.getName()) + " , " + dbobj);
			BigDecimal dbAmount=new BigDecimal(dbobj.getString(FieldType.AMOUNT.getName())).setScale(2);
			impsAmount=new BigDecimal(fieldAmount).setScale(2);
			debitAmount = impsAmount.add(new BigDecimal(dbobj.getString(FieldType.DEBIT_AMOUNT.getName())).setScale(2));
			closingAmount = dbAmount.subtract(impsAmount);
			logger.info("debitAmount = " + debitAmount);
			logger.info("closingAmount = " + closingAmount);
		 }
		 cursor.close();
		 if(flag) {
			 Bson filter;
			 if(user.isSuperMerchant() == false && StringUtils.isNotBlank(user.getSuperMerchantId())) {
				 filter = new Document(FieldType.SUB_MERCHANT_ID.getName(), fields.get(FieldType.PAY_ID.getName())).
							append(FieldType.CLOSING_DATE.getName(), dateFrom);
				}else {
					filter = new Document(FieldType.PAY_ID.getName(), fields.get(FieldType.PAY_ID.getName())).
							append(FieldType.CLOSING_DATE.getName(), dateFrom);
				}
			 
			 Bson newValue;
			 if(fields.contains(FieldType.RESELLER_ID.getName())) {
				 newValue = new Document(FieldType.DEBIT_AMOUNT.getName(), debitAmount.toString()).append(FieldType.AMOUNT.getName(), closingAmount.toString()).append(FieldType.UPDATE_DATE.getName(), dateNow).append(FieldType.RESELLER_ID.getName(), fields.get(FieldType.RESELLER_ID.getName())); 
			 }else {
				 newValue = new Document(FieldType.DEBIT_AMOUNT.getName(), debitAmount.toString()).append(FieldType.AMOUNT.getName(), closingAmount.toString()).append(FieldType.UPDATE_DATE.getName(), dateNow);
			 }
				Bson updateOperationDocument = new Document("$set", newValue);
				collection1.updateOne(filter, updateOperationDocument);
				logger.info("update request for closing collection , " + newValue);
		 }else {
			 Calendar cal = Calendar.getInstance();
				DateFormat dateFormat1 = new SimpleDateFormat("yyyyMMdd");
				DateFormat dateFormat2 = new SimpleDateFormat("yyyy-MM-dd 00:00:00");
				
				String todaysDateIndex1 = dateFormat1.format(cal.getTime());
				String todaysDate = dateFormat2.format(cal.getTime());
				cal.add(Calendar.DATE, -1);
				String yesterdayDate = dateFormat2.format(cal.getTime());
				String yesterdayDateIndex = dateFormat1.format(cal.getTime());
				
				logger.info("Todays Date = " + todaysDate);
				logger.info("Yesterdays Date = " + yesterdayDate);
				BasicDBObject finalquery2 = new BasicDBObject();
				if(user.isSuperMerchant() == false && StringUtils.isNotBlank(user.getSuperMerchantId())){
					finalquery2.append(FieldType.SUB_MERCHANT_ID.getName(),user.getPayId());
				}else {
					finalquery2.append(FieldType.PAY_ID.getName(),fields.get(FieldType.PAY_ID.getName()));
				}
				finalquery2.append("DATE_INDEX", yesterdayDateIndex);
				MongoCursor<Document> cursor2 = collection1.find(finalquery2).iterator();
				
				if(cursor2.hasNext()) {
					BasicDBObject finalquery3 = new BasicDBObject("DATE_INDEX", yesterdayDateIndex);
					MongoCursor<Document> cursor3 = collection1.find(finalquery3).iterator();
				while (cursor3.hasNext()) {
					Document dbobj = cursor3.next();
					logger.info("orginal request for closing collection, Insertion ,virtual account code  "
							+ dbobj.getString(FieldType.VIRTUAL_AC_CODE.getName()) + " , " + dbobj);
					Date currentDate = new Date();
					String dateNowW = DateCreater.formatDateForDb(currentDate);
					String amount = dbobj.get("AMOUNT").toString();
					
					Document doc = new Document();
					doc.put(FieldType.CREATE_DATE.getName(), dateNowW);
					doc.put(FieldType.UPDATE_DATE.getName(), dateNowW);
					doc.put(FieldType.CLOSING_DATE.getName(), todaysDate);
					doc.put(FieldType.VIRTUAL_AC_CODE.getName(), dbobj.get(FieldType.VIRTUAL_AC_CODE.getName()).toString());
					doc.put(FieldType.PAY_ID.getName(), dbobj.get(FieldType.PAY_ID.getName()).toString());
					if(dbobj.containsKey(FieldType.SUB_MERCHANT_ID.getName())){
					doc.put(FieldType.SUB_MERCHANT_ID.getName(), dbobj.get(FieldType.SUB_MERCHANT_ID.getName()).toString());
				    }
					
					if(dbobj.containsKey(FieldType.RESELLER_ID.getName())){
						doc.put(FieldType.RESELLER_ID.getName(), dbobj.get(FieldType.RESELLER_ID.getName()).toString());
					    }
					doc.put(FieldType.DEBIT_AMOUNT.getName(), "0.00");
					doc.put(FieldType.CREDIT_AMOUNT.getName(), "0.00");
					doc.put(FieldType.AMOUNT.getName(),amount);
					doc.put(FieldType.OPENING_AMOUNT.getName(), amount);
					doc.put(FieldType.DATE_INDEX.getName(), todaysDateIndex1);
					collection1.insertOne(doc);
					logger.info("Insert in closing collection ,virtual account code " + doc.get(FieldType.VIRTUAL_AC_CODE.getName()).toString() + " , " + doc);				
				}
				}
				MongoCursor<Document> cursor4 = collection1.find(newFieldsObj1).iterator();
				boolean flag1 = false;
				 while(cursor4.hasNext()) {
					 Document dbobj = cursor4.next();
					 logger.info("orginal data in closing collection for update ,virtual account code " + dbobj.get(FieldType.VIRTUAL_AC_CODE.getName()).toString() + " , " + dbobj);
					 flag1 = true;
				BigDecimal dbAmount=new BigDecimal(dbobj.getString(FieldType.AMOUNT.getName())).setScale(2);
				impsAmount=new BigDecimal(fieldAmount).setScale(2);
				debitAmount = impsAmount.add(new BigDecimal(dbobj.getString(FieldType.DEBIT_AMOUNT.getName())).setScale(2));
				closingAmount = dbAmount.subtract(impsAmount);
				 }
				 
				 if(flag1) {
				 if (closingAmount.compareTo(BigDecimal.ZERO) >= 0) {
					 Bson filter;
					 if(user.isSuperMerchant() == false && StringUtils.isNotBlank(user.getSuperMerchantId())) {
						 filter = new Document(FieldType.SUB_MERCHANT_ID.getName(), fields.get(FieldType.PAY_ID.getName())).
									append(FieldType.CLOSING_DATE.getName(), dateFrom);
						}else {
							filter = new Document(FieldType.PAY_ID.getName(), fields.get(FieldType.PAY_ID.getName())).
									append(FieldType.CLOSING_DATE.getName(), dateFrom);
						}
					 Date dNowPayout = new Date();
						String dateNowPayout = DateCreater.formatDateForDb(dNowPayout);
					 Bson newValue;
					 if(fields.contains(FieldType.RESELLER_ID.getName())) {
						 newValue = new Document(FieldType.DEBIT_AMOUNT.getName(), debitAmount.toString()).append(FieldType.AMOUNT.getName(), closingAmount.toString()).append(FieldType.UPDATE_DATE.getName(), dateNowPayout).append(FieldType.RESELLER_ID.getName(), fields.get(FieldType.RESELLER_ID.getName())); 
					 }else {
						 newValue = new Document(FieldType.DEBIT_AMOUNT.getName(), debitAmount.toString()).append(FieldType.AMOUNT.getName(), closingAmount.toString()).append(FieldType.UPDATE_DATE.getName(), dateNowPayout);
					 }
						Bson updateOperationDocument = new Document("$set", newValue);
						collection1.updateOne(filter, updateOperationDocument);
						logger.info("Update in closing closing collection , pay Id " + fields.get(FieldType.PAY_ID.getName()).toString() + " , " + newValue);
				 }
				 }
		 }
		}catch(Exception e) {
			logger.error("Exception caugth insert insertUpdateForClosing, ", e);
		}
	}
		/*
		 * 
		 * Document doc1 = new Document();
		 * 
		 * BasicDBObject finalquery2 = new BasicDBObject();
		 * doc1.put(FieldType.CREATE_DATE.getName(), dateNow);
		 * doc1.put(FieldType.UPDATE_DATE.getName(), dateNow);
		 * doc1.put(FieldType.CLOSING_DATE.getName(), dateFrom);
		 * doc1.put(FieldType.VIRTUAL_AC_CODE.getName(), user.getVirtualAccountNo()); if
		 * (user.isSuperMerchant() == false &&
		 * StringUtils.isNotBlank(user.getSuperMerchantId())) {
		 * doc1.put(FieldType.PAY_ID.getName(), user.getSuperMerchantId());
		 * doc1.put(FieldType.SUB_MERCHANT_ID.getName(), user.getPayId());
		 * finalquery2.append(FieldType.SUB_MERCHANT_ID.getName(), user.getPayId()); }
		 * else { doc1.put(FieldType.PAY_ID.getName(),
		 * fields.get(FieldType.PAY_ID.getName()));
		 * finalquery2.append(FieldType.PAY_ID.getName(),
		 * fields.get(FieldType.PAY_ID.getName())); }
		 * 
		 * if (StringUtils.isNotBlank(fields.get(FieldType.RESELLER_ID.getName()))) {
		 * doc1.put(FieldType.RESELLER_ID.getName(),
		 * fields.get(FieldType.RESELLER_ID.getName())); }
		 * 
		 * BasicDBObject match2 = new BasicDBObject("$match", finalquery2);
		 * BasicDBObject sort2 = new BasicDBObject("$sort", new
		 * BasicDBObject("CREATE_DATE", -1));
		 * 
		 * List<BasicDBObject> pipeline2 = Arrays.asList(match2, sort2);
		 * AggregateIterable<Document> output2 = collection1.aggregate(pipeline2);
		 * output2.allowDiskUse(true); MongoCursor<Document> cursor2 =
		 * output2.iterator(); while (cursor2.hasNext()) { Document dbobj1 =
		 * cursor2.next(); BigDecimal dbAmount1 = new
		 * BigDecimal(dbobj1.getString(FieldType.AMOUNT.getName())).setScale(2);
		 * impsAmount = new BigDecimal(fieldAmount).setScale(2);
		 * 
		 * if (dbAmount1.toString().compareTo(impsAmount.toString()) >= 0) {
		 * closingAmount = dbAmount1.subtract(impsAmount); openingAmount = dbAmount1; }
		 * break; } doc1.put(FieldType.DEBIT_AMOUNT.getName(), fieldAmount); if
		 * (closingAmount.toString().equalsIgnoreCase("0.00")) {
		 * doc1.put(FieldType.AMOUNT.getName(), fieldAmount); } else {
		 * doc1.put(FieldType.AMOUNT.getName(), closingAmount.toString()); }
		 * doc1.put(FieldType.CREDIT_AMOUNT.getName(), creditAmount.toString());
		 * doc1.put(FieldType.OPENING_AMOUNT.getName(), openingAmount.toString());
		 * doc1.put(FieldType.CREATE_DATE.getName(), dateNow);
		 * 
		 * collection1.insertOne(doc1);
		 */	
	
	public BigDecimal getClosingTransactionAmount(Fields fields){
		logger.info("Inside getClosingTransactionAmount()");
		BigDecimal totalAmount=new BigDecimal("0").setScale(2);
		try {
			
			BigDecimal closingAmount=new BigDecimal("0").setScale(2);
			 
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.CLOSING_AMOUNT_COLLECTION.getValue()));
			BasicDBObject query = new BasicDBObject();
			
			String payId=fields.get(FieldType.PAY_ID.getName());
			String subMerchantId=null;
			
			User user=userDao.findPayId(payId);
			
			if(StringUtils.isNotBlank(user.getSuperMerchantId()))
			{
				subMerchantId=user.getPayId();
				payId=user.getSuperMerchantId();
			}
			
			if (StringUtils.isNotBlank(payId)) {
				query.append(FieldType.PAY_ID.getName(), payId);
			}

			if (StringUtils.isNotBlank(subMerchantId)) {
				query.append(FieldType.SUB_MERCHANT_ID.getName(),subMerchantId);
			}
			
			BasicDBObject match2 = new BasicDBObject("$match", query);
			BasicDBObject sort2 = new BasicDBObject("$sort", new BasicDBObject("CREATE_DATE", -1));

			List<BasicDBObject> pipeline2 = Arrays.asList(match2, sort2);
			AggregateIterable<Document> output2 = coll.aggregate(pipeline2);
			output2.allowDiskUse(true);
			MongoCursor<Document> cursor = output2.iterator();

			while (cursor.hasNext()) {
				Document dbobj = cursor.next();
				BigDecimal dbAmount=new BigDecimal(dbobj.getString(FieldType.AMOUNT.getName())).setScale(2);
				closingAmount=(dbAmount);
				break;
			}
			cursor.close();
		        
			totalAmount= closingAmount;
			logger.info("Total today's Transactions Amount  "+ totalAmount);
		} catch (Exception e) {
			logger.error("Exception Closing Amount transaction, ", e);
		}
		return totalAmount;
		
		
	}

	public void UpdateForClosing(Fields fields) throws ParseException {
		logger.info("Inside UpdateForClosing(), VendorPayoutDao");
		try {
			MongoDatabase dbIns1 = mongoInstance.getDB();
			MongoCollection<Document> collection1 = dbIns1.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.CLOSING_AMOUNT_COLLECTION.getValue()));
			BasicDBObject newFieldsObj1 = new BasicDBObject();

			Date dNow = new Date();
			String dateNow = DateCreater.formatDateForDb(dNow);
			SimpleDateFormat inputDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			String dtDate = dateFormat.format(inputDate.parse(dateNow));

			DateFormat dateFormatIndex = new SimpleDateFormat("yyyyMMdd");
			String todaysDateIndex = dateFormatIndex.format(dNow.getTime());

			String preDate = fields.get(FieldType.CREATE_DATE.getName());
			//String preDate = "2021-07-20 18:32:34";
			DateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.ENGLISH);
			Date dateStart = format.parse(preDate);
			LocalDate startDate = dateStart.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			String preDateIndex = startDate.toString().replaceAll("-", "");
			String fromDate = dateFormat.format(inputDate.parse(preDate));
			
			String daFrom = fromDate + " " + "00:00:00";
			String dateFrom = dtDate + " " + "00:00:00";
			String toDate = dtDate + " " + "23:59:59";
			if (preDateIndex.equalsIgnoreCase(todaysDateIndex)) {
				newFieldsObj1.append(FieldType.CREATE_DATE.getName(),
						BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(dateFrom).toLocalizedPattern())
								.add("$lte", new SimpleDateFormat(toDate).toLocalizedPattern()).get());
				newFieldsObj1.append(FieldType.DATE_INDEX.getName(), todaysDateIndex);
				User user = null;
				if (fields.contains(FieldType.SUB_MERCHANT_ID.getName())) {
					user = userDao.findPayId(fields.get(FieldType.SUB_MERCHANT_ID.getName()));
					newFieldsObj1.append(FieldType.SUB_MERCHANT_ID.getName(),
							fields.get(FieldType.SUB_MERCHANT_ID.getName()));
				} else {
					user = userDao.findPayId(fields.get(FieldType.PAY_ID.getName()));
					newFieldsObj1.append(FieldType.PAY_ID.getName(), fields.get(FieldType.PAY_ID.getName()));
				}
				MongoCursor<Document> cursor = collection1.find(newFieldsObj1).iterator();
				BigDecimal closingAmount = new BigDecimal("0").setScale(2);
				BigDecimal impsAmount = new BigDecimal("0").setScale(2);
				BigDecimal creditAmount = new BigDecimal("0").setScale(2);
				BigDecimal debitAmount = new BigDecimal("0").setScale(2);
				BigDecimal openingAmount = new BigDecimal("0").setScale(2);
				boolean flag = false;
				while (cursor.hasNext()) {
					flag = true;
					Document dbobj = cursor.next();
					logger.info("Updated Closing collection by status enquiry, orginal request ,virtual account code "
							+ dbobj.get(FieldType.VIRTUAL_AC_CODE.getName()).toString() + " , " + dbobj);
					BigDecimal dbAmount = new BigDecimal(dbobj.getString(FieldType.AMOUNT.getName())).setScale(2);
					impsAmount = new BigDecimal(fields.get(FieldType.AMOUNT.getName())).setScale(2);
					debitAmount = new BigDecimal(dbobj.getString(FieldType.DEBIT_AMOUNT.getName())).subtract(impsAmount)
							.setScale(2);
					closingAmount = dbAmount.add(impsAmount);
					logger.info("debitAmount : " + debitAmount);
					logger.info("closingAmount : " + closingAmount);
				}
				cursor.close();
				if (flag) {
					Bson filter;
					if (fields.contains(FieldType.SUB_MERCHANT_ID.getName())) {
						filter = new Document(FieldType.SUB_MERCHANT_ID.getName(),
								fields.get(FieldType.SUB_MERCHANT_ID.getName()))
										.append(FieldType.CLOSING_DATE.getName(), dateFrom);
					} else {
						filter = new Document(FieldType.PAY_ID.getName(), fields.get(FieldType.PAY_ID.getName()))
								.append(FieldType.CLOSING_DATE.getName(), dateFrom);
					}

					Bson newValue = new Document(FieldType.DEBIT_AMOUNT.getName(), debitAmount.toString())
							.append(FieldType.AMOUNT.getName(), closingAmount.toString())
							.append(FieldType.UPDATE_DATE.getName(), dateNow);
					Bson updateOperationDocument = new Document("$set", newValue);
					collection1.updateOne(filter, updateOperationDocument);
					logger.info("Updated Closing collection by status enquiry, Final request : , " + newValue);
				}
			} else {
				newFieldsObj1.append(FieldType.CREATE_DATE.getName(),
						BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(daFrom).toLocalizedPattern())
								.add("$lte", new SimpleDateFormat(toDate).toLocalizedPattern()).get());
				User user = null;
				if (fields.contains(FieldType.SUB_MERCHANT_ID.getName())) {
					user = userDao.findPayId(fields.get(FieldType.SUB_MERCHANT_ID.getName()));
					newFieldsObj1.append(FieldType.SUB_MERCHANT_ID.getName(),
							fields.get(FieldType.SUB_MERCHANT_ID.getName()));
				} else {
					user = userDao.findPayId(fields.get(FieldType.PAY_ID.getName()));
					newFieldsObj1.append(FieldType.PAY_ID.getName(), fields.get(FieldType.PAY_ID.getName()));
				}
				BasicDBObject match = new BasicDBObject("$match", newFieldsObj1);
				BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject("CREATE_DATE", 1));			
				List<BasicDBObject> pipeline = Arrays.asList(match, sort);
				AggregateIterable<Document> output = collection1.aggregate(pipeline);
				output.allowDiskUse(true);
				MongoCursor<Document> cursor = output.iterator();
				
				BigDecimal closingAmount = new BigDecimal("0").setScale(2);
				BigDecimal creditAmount = new BigDecimal("0").setScale(2);
				BigDecimal openingAmount = new BigDecimal("0").setScale(2);
				boolean flag = false;
				while (cursor.hasNext()) {
					Document dbobj = cursor.next();
					logger.info("Updated Closing collection by status enquiry, orginal request ,virtual account code "
							+ dbobj.get(FieldType.VIRTUAL_AC_CODE.getName()).toString() + " , " + dbobj);
					if(!flag) {
						flag = true;
					BigDecimal dbAmount = new BigDecimal(dbobj.getString(FieldType.AMOUNT.getName())).setScale(2);
					BigDecimal impsAmount = new BigDecimal(fields.get(FieldType.AMOUNT.getName())).setScale(2);
					BigDecimal debitAmount = new BigDecimal(dbobj.getString(FieldType.DEBIT_AMOUNT.getName())).subtract(impsAmount)
							.setScale(2);
					closingAmount = dbAmount.add(impsAmount);
					logger.info("debitAmount : " + debitAmount);
					logger.info("closingAmount : " + closingAmount);
					Bson filter;
					if (fields.contains(FieldType.SUB_MERCHANT_ID.getName())) {
						filter = new Document(FieldType.SUB_MERCHANT_ID.getName(),
								fields.get(FieldType.SUB_MERCHANT_ID.getName()))
										.append(FieldType.DATE_INDEX.getName(), dbobj.getString(FieldType.CREATE_DATE.getName()).substring(0, 10).replace("-", ""));
					} else {
						filter = new Document(FieldType.PAY_ID.getName(), fields.get(FieldType.PAY_ID.getName()))
								.append(FieldType.DATE_INDEX.getName(), dbobj.getString(FieldType.CREATE_DATE.getName()).substring(0, 10).replace("-", ""));
					}

					Bson newValue = new Document(FieldType.DEBIT_AMOUNT.getName(), debitAmount.toString())
							.append(FieldType.AMOUNT.getName(), closingAmount.toString())
							.append(FieldType.UPDATE_DATE.getName(), dateNow);
					Bson updateOperationDocument = new Document("$set", newValue);
					collection1.updateOne(filter, updateOperationDocument);
					logger.info("Updated Closing collection by status enquiry, Final request : ,virtual account code " + dbobj.get(FieldType.VIRTUAL_AC_CODE.getName()).toString() + " , " + newValue.toString());
					}else {
						if(flag) {
					
							BigDecimal dbAmount = new BigDecimal(dbobj.getString(FieldType.AMOUNT.getName())).setScale(2);
							dbAmount = dbAmount.add(new BigDecimal(fields.get(FieldType.AMOUNT.getName())));
							BigDecimal dbOpeningAmount = new BigDecimal(dbobj.getString(FieldType.OPENING_AMOUNT.getName()));
							dbOpeningAmount = dbOpeningAmount.add(new BigDecimal(fields.get(FieldType.AMOUNT.getName())));

							Bson filter;
							if (fields.contains(FieldType.SUB_MERCHANT_ID.getName())) {
								filter = new Document(FieldType.SUB_MERCHANT_ID.getName(),
										fields.get(FieldType.SUB_MERCHANT_ID.getName()))
												.append(FieldType.DATE_INDEX.getName(), dbobj.getString(FieldType.CREATE_DATE.getName()).substring(0, 10).replace("-", ""));
							} else {
								filter = new Document(FieldType.PAY_ID.getName(), fields.get(FieldType.PAY_ID.getName()))
										.append(FieldType.DATE_INDEX.getName(), dbobj.getString(FieldType.CREATE_DATE.getName()).substring(0, 10).replace("-", ""));
							}
							Bson newValue = new Document(FieldType.AMOUNT.getName(), dbAmount.toString()).
									append(FieldType.OPENING_AMOUNT.getName(), dbOpeningAmount.toString()).append(FieldType.UPDATE_DATE.getName(), dateNow);
						Bson updateOperationDocument = new Document("$set", newValue);
						logger.info("update request for closing: virtual account code = " + dbobj.getString(FieldType.VIRTUAL_AC_CODE.getName()) + " update value, " + newValue.toString());
						collection1.updateOne(filter, updateOperationDocument);
						}
					}
					
				}
				cursor.close();
			}
		} catch (Exception e) {
			logger.error("Exception : ", e);
		}
	}
		/*
			 * else { Document doc1=new Document();
			 * 
			 * BasicDBObject finalquery2 = new BasicDBObject();
			 * doc1.put(FieldType.CREATE_DATE.getName(), dateNow);
			 * doc1.put(FieldType.UPDATE_DATE.getName(), dateNow);
			 * doc1.put(FieldType.CLOSING_DATE.getName(), dateFrom);
			 * if(fields.contains(FieldType.VIRTUAL_AC_CODE.getName())) {
			 * doc1.put(FieldType.VIRTUAL_AC_CODE.getName(),fields.get(FieldType.
			 * VIRTUAL_AC_CODE.getName())); }else {
			 * doc1.put(FieldType.VIRTUAL_AC_CODE.getName(),user.getAccountNo()); }
			 * if(fields.contains(FieldType.SUB_MERCHANT_ID.getName())) {
			 * doc1.put(FieldType.PAY_ID.getName(),fields.get(FieldType.PAY_ID.getName()));
			 * doc1.put(FieldType.SUB_MERCHANT_ID.getName(),fields.get(FieldType.
			 * SUB_MERCHANT_ID.getName()));
			 * finalquery2.append(FieldType.SUB_MERCHANT_ID.getName(),fields.get(FieldType.
			 * SUB_MERCHANT_ID.getName())); }else {
			 * doc1.put(FieldType.PAY_ID.getName(),fields.get(FieldType.PAY_ID.getName()));
			 * finalquery2.append(FieldType.PAY_ID.getName(),fields.get(FieldType.PAY_ID.
			 * getName())); }
			 * 
			 * 
			 * BasicDBObject match2 = new BasicDBObject("$match", finalquery2);
			 * BasicDBObject sort2 = new BasicDBObject("$sort", new
			 * BasicDBObject("CREATE_DATE", -1));
			 * 
			 * List<BasicDBObject> pipeline2 = Arrays.asList(match2, sort2);
			 * AggregateIterable<Document> output2 = collection1.aggregate(pipeline2);
			 * output2.allowDiskUse(true); MongoCursor<Document> cursor2 =
			 * output2.iterator(); while (cursor2.hasNext()) { Document dbobj1 =
			 * cursor2.next(); BigDecimal dbAmount1=new
			 * BigDecimal(dbobj1.getString(FieldType.AMOUNT.getName())).setScale(2);
			 * impsAmount=new
			 * BigDecimal(fields.get(FieldType.AMOUNT.getName())).setScale(2);
			 * 
			 * if(impsAmount.toString().compareTo(dbAmount1.toString())>=0) { closingAmount
			 * = dbAmount1.subtract(impsAmount); openingAmount = dbAmount1; } break; }
			 * doc1.put(FieldType.DEBIT_AMOUNT.getName(),fields.get(FieldType.AMOUNT.getName
			 * ())); if(closingAmount.toString().equalsIgnoreCase("0.00")) {
			 * doc1.put(FieldType.AMOUNT.getName(),fields.get(FieldType.AMOUNT.getName()));
			 * }else { doc1.put(FieldType.AMOUNT.getName(),closingAmount.toString()); }
			 * doc1.put(FieldType.CREDIT_AMOUNT.getName(), creditAmount.toString());
			 * doc1.put(FieldType.OPENING_AMOUNT.getName(), openingAmount.toString());
			 * doc1.put(FieldType.CREATE_DATE.getName(), dateNow);
			 * 
			 * collection1.insertOne(doc1); }
			 */
	
	public void checkDuplicateOrderId(Fields fields) {
		try {

			MongoDatabase dbIns = null;
			String orderId = fields.get(FieldType.ORDER_ID.getName());
			String payId = fields.get(FieldType.PAY_ID.getName());
			String currencyString = fields.get(FieldType.CURRENCY_CODE.getName());
			String subMerchantId = "";
			User user = userDao.findPayId(payId);

			if (StringUtils.isNotBlank(user.getSuperMerchantId())) {
				subMerchantId = user.getPayId();
				payId = user.getSuperMerchantId();
			}

			List<BasicDBObject> conditionList = new ArrayList<BasicDBObject>();
			conditionList.add(new BasicDBObject(FieldType.ORDER_ID.getName(), orderId));
			conditionList.add(new BasicDBObject(FieldType.PAY_ID.getName(), payId));
			conditionList.add(new BasicDBObject(FieldType.USER_TYPE.getName(), "Verification"));
			conditionList.add(new BasicDBObject(FieldType.CURRENCY_CODE.getName(), currencyString));
			if (StringUtils.isNotBlank(subMerchantId)) {
				conditionList.add(new BasicDBObject(FieldType.SUB_MERCHANT_ID.getName(), subMerchantId));
			}

			BasicDBObject query = new BasicDBObject("$and", conditionList);
			dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.IMPS_SETTlEMENT_COLLECTION.getValue()));
			MongoCursor<Document> cursor = collection.find(query).iterator();
			if (cursor.hasNext()) {
				fields.put(FieldType.DUPLICATE_YN.getName(), "Y");
			} else {
				fields.put(FieldType.DUPLICATE_YN.getName(), "N");
			}
			cursor.close();

		} catch (Exception exception) {
			// MDC.put(FieldType.INTERNAL_CUSTOM_MDC.getName(),
			// fields.getCustomMDC());
			String message = "Error while inserting transaction in database";
			logger.error(message, exception);

		}
		
	}
	
	public void checkDuplicateOrderIdMerchantInitiatedDirect(Fields fields) {
		try {

			MongoDatabase dbIns = null;
			String orderId = fields.get(FieldType.ORDER_ID.getName());
			String payId = fields.get(FieldType.PAY_ID.getName());
			String currencyString = fields.get(FieldType.CURRENCY_CODE.getName());
			String subMerchantId = "";
			User user = userDao.findPayId(payId);

			if (StringUtils.isNotBlank(user.getSuperMerchantId())) {
				subMerchantId = user.getPayId();
				payId = user.getSuperMerchantId();
			}

			List<BasicDBObject> conditionList = new ArrayList<BasicDBObject>();
			conditionList.add(new BasicDBObject(FieldType.ORDER_ID.getName(), orderId));
			conditionList.add(new BasicDBObject(FieldType.PAY_ID.getName(), payId));
			conditionList.add(new BasicDBObject(FieldType.USER_TYPE.getName(), "Merchant Initiated Direct"));
			conditionList.add(new BasicDBObject(FieldType.CURRENCY_CODE.getName(), currencyString));
			if (StringUtils.isNotBlank(subMerchantId)) {
				conditionList.add(new BasicDBObject(FieldType.SUB_MERCHANT_ID.getName(), subMerchantId));
			}

			BasicDBObject query = new BasicDBObject("$and", conditionList);
			dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.IMPS_SETTlEMENT_COLLECTION.getValue()));
			MongoCursor<Document> cursor = collection.find(query).iterator();
			if (cursor.hasNext()) {
				fields.put(FieldType.DUPLICATE_YN.getName(), "Y");
			} else {
				fields.put(FieldType.DUPLICATE_YN.getName(), "N");
			}
			cursor.close();

		} catch (Exception exception) {
			// MDC.put(FieldType.INTERNAL_CUSTOM_MDC.getName(),
			// fields.getCustomMDC());
			String message = "Error while inserting transaction in database";
			logger.error(message, exception);

		}
		
	}

	public void checkVpaDuplicateOrderId(Fields fields) {
		try {

			MongoDatabase dbIns = null;
			String orderId = fields.get(FieldType.ORDER_ID.getName());
			String payId = fields.get(FieldType.PAY_ID.getName());
			String currencyString = fields.get(FieldType.CURRENCY_CODE.getName());
			String subMerchantId = "";
			User user = userDao.findPayId(payId);

			if (StringUtils.isNotBlank(user.getSuperMerchantId())) {
				subMerchantId = user.getPayId();
				payId = user.getSuperMerchantId();
			}

			List<BasicDBObject> conditionList = new ArrayList<BasicDBObject>();
			conditionList.add(new BasicDBObject(FieldType.ORDER_ID.getName(), orderId));
			conditionList.add(new BasicDBObject(FieldType.PAY_ID.getName(), payId));
			//conditionList.add(new BasicDBObject(FieldType.CURRENCY_CODE.getName(), currencyString));
			if (StringUtils.isNotBlank(subMerchantId)) {
				conditionList.add(new BasicDBObject(FieldType.SUB_MERCHANT_ID.getName(), subMerchantId));
			}

			BasicDBObject query = new BasicDBObject("$and", conditionList);
			dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.BENE_VERIFICATION_COLLECTION.getValue()));
			MongoCursor<Document> cursor = collection.find(query).iterator();
			if (cursor.hasNext()) {
				fields.put(FieldType.DUPLICATE_YN.getName(), "Y");
			} else {
				fields.put(FieldType.DUPLICATE_YN.getName(), "N");
			}
			cursor.close();

		} catch (Exception exception) {
			// MDC.put(FieldType.INTERNAL_CUSTOM_MDC.getName(),
			// fields.getCustomMDC());
			String message = "Error while inserting transaction in database";
			logger.error(message, exception);

		}
		
	}

	public void insertUpiBeneVerification(Fields fields) {
		logger.info("inside insertUpiBeneVerification()");
		try {

			Date dNow = new Date();
			String dateNow = DateCreater.formatDateForDb(dNow);
			fields.put(FieldType.CREATE_DATE.getName(), dateNow);
			
			fields.put(FieldType.DATE_INDEX.getName(),dateNow.substring(0, 10).replace("-", ""));

			BasicDBObject newFieldsObj = new BasicDBObject();

			for (String columnName : fields.keySet()) {
				newFieldsObj.put(columnName, fields.get(columnName));

			}
			logger.info("Fields for Insert "+newFieldsObj.toString());
			fields.remove(FieldType.DATE_INDEX.getName());

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.BENE_VERIFICATION_COLLECTION.getValue()));
			Document doc = new Document(newFieldsObj);
			if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
					&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {

				collection.insertOne(dataEncDecTool.encryptDocument(doc));
			} else {
				collection.insertOne(doc);
			}
			//collection.insertOne(doc);
			logger.info("Fields Inserted " + fields.getFields() + " txn id " + fields.get(FieldType.TXN_ID.getName()));
		} catch (Exception exception) {
			String message = "Error while inserting Composite Response in database";
			logger.error(message, exception);
			
		}
		
	}

	public void updateSufCharges(Fields fields) {
		logger.info("inside updateSufCharges() for Ecollection");
		try {
			String payId = fields.get(FieldType.PAY_ID.getName());
			String subMerchantPayId = fields.get(FieldType.SUB_MERCHANT_ID.getName());
			String mopType = fields.get(FieldType.MOP_TYPE.getName());
			BigDecimal amount = new BigDecimal(fields.get(FieldType.TOTAL_AMOUNT.getName()));

			UserSettingData userSettings;

			if (StringUtils.isNotBlank(subMerchantPayId))
				userSettings = userSettingDao.fetchDataUsingPayId(subMerchantPayId);
			else
				userSettings = userSettingDao.fetchDataUsingPayId(payId);

			if (userSettings.isAllowECollectionFee()) {

				logger.info("inside if isAllowECollectionFee = true");

				List<SUFDetail> sufDetailsOfMerchant = sufDetailDao.getSUFDetailForSlabCheck(payId, mopType, "Sale",
						mopType, "DOMESTIC", subMerchantPayId);

				for (SUFDetail sufCharges : sufDetailsOfMerchant) {

					String[] slabArray = sufCharges.getSlab().split("-");
					BigDecimal minSlab = new BigDecimal(slabArray[0]);
					BigDecimal maxSlab = new BigDecimal(slabArray[1]);

					if (amount.compareTo(minSlab) >= 0 && amount.compareTo(maxSlab) <= 0) {
						SUFDetail sufDetails = null;

						sufDetails = sufDetailDao.getSUFDetail(payId, mopType, "Sale", mopType, "DOMESTIC",
								sufCharges.getSlab(), subMerchantPayId);

						// calculation
						BigDecimal dbPercentage = BigDecimal.ZERO;
						BigDecimal dbFixedCharges = BigDecimal.ZERO;

						BigDecimal calPercentage = BigDecimal.ZERO;
						BigDecimal calFixedCharges = BigDecimal.ZERO;
						BigDecimal gst = BigDecimal.ZERO;

						if (StringUtils.isNotBlank(sufDetails.getPercentageAmount())) {
							dbPercentage = new BigDecimal(sufDetails.getPercentageAmount());

							if (dbPercentage.compareTo(BigDecimal.ZERO) == 1) {

								calPercentage = amount.multiply(dbPercentage).divide(new BigDecimal("100")).setScale(2,
										RoundingMode.HALF_UP);
								gst = gst
										.add(calPercentage.multiply(new BigDecimal("18")).divide(new BigDecimal("100")))
										.setScale(2, RoundingMode.HALF_UP); // 18%
																			// GST

								amount = amount.subtract(calPercentage.add(gst)).setScale(2, RoundingMode.HALF_UP);
								fields.put(FieldType.PERCENTAGE_CHARGES.getName(), String.valueOf(calPercentage));
							}
						}

						if (StringUtils.isNotBlank(sufDetails.getFixedCharge())) {
							dbFixedCharges = new BigDecimal(sufDetails.getPercentageAmount());
							BigDecimal fcGst = BigDecimal.ZERO;

							if (dbFixedCharges.compareTo(BigDecimal.ZERO) == 1) {

								calFixedCharges = amount.multiply(dbFixedCharges).divide(new BigDecimal("100"))
										.setScale(2, RoundingMode.HALF_UP);
								;
								fcGst = fcGst.add(
										calFixedCharges.multiply(new BigDecimal("18")).divide(new BigDecimal("100")))
										.setScale(2, RoundingMode.HALF_UP);
								; // 18% GST
								amount = amount.subtract(calFixedCharges.add(fcGst)).setScale(2, RoundingMode.HALF_UP);

								gst = gst.add(fcGst);

								fields.put(FieldType.FIXED_CHARGES.getName(), String.valueOf(calFixedCharges));

							}
						}

						fields.put(FieldType.PG_GST.getName(), String.valueOf(gst));
						fields.put(FieldType.AMOUNT.getName(), String.valueOf(amount));

						break;
					}
				}

				logger.info("charges updated successfully " + payId + " subMerchant Id " + subMerchantPayId);

			}
			logger.info("No Permission for the user " + payId + " subMerchant Id " + subMerchantPayId);
		} catch (Exception e) {
			logger.error("exception while Suf charges calculation in eCollection ", e);
		}

	}
}


