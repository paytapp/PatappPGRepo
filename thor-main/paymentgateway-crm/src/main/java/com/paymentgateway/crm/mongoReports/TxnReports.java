package com.paymentgateway.crm.mongoReports;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.MongoExecutionTimeoutException;
import com.mongodb.MongoQueryException;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.paymentgateway.commons.dao.MPADao;
import com.paymentgateway.commons.dao.ParentMerchantMappingDao;
import com.paymentgateway.commons.dao.UserSettingDao;
import com.paymentgateway.commons.mongo.MongoInstance;
import com.paymentgateway.commons.user.ECollectionObject;
import com.paymentgateway.commons.user.Enach;
import com.paymentgateway.commons.user.ImpsDownloadObject;
import com.paymentgateway.commons.user.MerchantPaymentAdviseDownloadObject;
import com.paymentgateway.commons.user.MerchantProcessingApplication;
import com.paymentgateway.commons.user.Merchants;
import com.paymentgateway.commons.user.ParentMerchantMapping;
import com.paymentgateway.commons.user.PaymentSearchDownloadObject;
import com.paymentgateway.commons.user.RefundRejection;
import com.paymentgateway.commons.user.SearchTransaction;
import com.paymentgateway.commons.user.TransactionSearch;
import com.paymentgateway.commons.user.TransactionSearchDownloadObject;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserSettingData;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.user.VendorPayouts;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CountryCodes;
import com.paymentgateway.commons.util.CrmFieldConstants;
import com.paymentgateway.commons.util.CrmFieldType;
import com.paymentgateway.commons.util.CurrencyTypes;
import com.paymentgateway.commons.util.DataEncDecTool;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.Frequency;
import com.paymentgateway.commons.util.ManualRefundProcess;
import com.paymentgateway.commons.util.MopType;
import com.paymentgateway.commons.util.PaymentType;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TransactionType;
import com.paymentgateway.commons.util.TxnType;

@Component
public class TxnReports {

	private static Logger logger = LoggerFactory.getLogger(TxnReports.class.getName());
	private static final String alphabaticFileName = "alphabatic-currencycode.properties";
	private static final String prefix = "MONGO_DB_";

	@Autowired
	private UserDao userdao;

	@Autowired
	PropertiesManager propertiesManager;

	@Autowired
	private MongoInstance mongoInstance;

	@Autowired
	private MPADao mpaDao;

	@Autowired
	private Fields fields;

	@Autowired
	private DataEncDecTool dataEncDecTool;

	@Autowired
	private UserSettingDao userSettingDao;

	@Autowired
	private ParentMerchantMappingDao parentMerchantMappingDao;

	@Autowired
	private UserDao userDao;

	@SuppressWarnings("static-access")
	public List<TransactionSearch> searchPayment(String pgRefNum, String orderId, String customerEmail, String SKUCode,
			String merchantPayId, String subMerchantPayId, String paymentType, String Userstatus, String currency,
			String transactionType, String fromDate, String toDate, User user, int start, int length,
			String partSettleFlag, String paymentRegion, Boolean searchFlag, String deliveryStatus,
			Set<String> orderIdSet, boolean allowLatest, String transactionFlag, String deltaFlag, String rrn) {
		Map<String, User> userMap = new HashMap<String, User>();

		logger.info("Inside TxnReports , searchPayment");
		boolean isGlocal = false;

		try {

			UserSettingData userSettings = userSettingDao.fetchDataUsingPayId(user.getPayId());

			if (!merchantPayId.equalsIgnoreCase("ALL")) {
				String identifierKey = propertiesManager.propertiesMap.get(Constants.MERCHANT_PAYID.getValue());
				if (StringUtils.isNotBlank(identifierKey) && identifierKey.contains(merchantPayId)) {
					isGlocal = true;
				}
			}

			List<TransactionSearch> transactionList = new ArrayList<TransactionSearch>();
			List<String> statusConditionList = new ArrayList<String>();
			PropertiesManager propManager = new PropertiesManager();
			List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();
			BasicDBObject statusQuery = new BasicDBObject();
			BasicDBObject customerQuery = new BasicDBObject();
			BasicDBObject customerQueryMask = new BasicDBObject();

			BasicDBObject dateIndexConditionQuery = new BasicDBObject();

			DateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.ENGLISH);
			Date dateStart = format.parse(fromDate);
			Date dateEnd = format.parse(toDate);

			LocalDate startDate = dateStart.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			LocalDate endDate = dateEnd.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

			String fromDateIndex = startDate.toString().replaceAll("-", "");
			String toDateIndex = endDate.toString().replaceAll("-", "");

			dateIndexConditionQuery.put("DATE_INDEX",
					BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(fromDateIndex).toLocalizedPattern())
							.add("$lte", new SimpleDateFormat(toDateIndex).toLocalizedPattern()).get());

			if (user.getUserType().equals(UserType.PARENTMERCHANT)) {
				paramConditionLst.add(new BasicDBObject(FieldType.PARENT_PAY_ID.getName(), user.getPayId()));
			} else if (!merchantPayId.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.PAY_ID.getName(), merchantPayId));
			}

			if (StringUtils.isNotBlank(subMerchantPayId) && !subMerchantPayId.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.SUB_MERCHANT_ID.getName(), subMerchantPayId));
			}
			if (StringUtils.isNotEmpty(paymentType) && !paymentType.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.PAYMENT_TYPE.getName(), paymentType));
			} else {

			}
			if (user.getUserType().equals(UserType.RESELLER)) {
				paramConditionLst.add(new BasicDBObject(FieldType.RESELLER_ID.getName(), user.getResellerId()));
			}

			if (!pgRefNum.isEmpty()) {
				paramConditionLst.add(new BasicDBObject(FieldType.PG_REF_NUM.getName(), pgRefNum));
			}
			
			if (!rrn.isEmpty()) {
				paramConditionLst.add(new BasicDBObject(FieldType.RRN.getName(), rrn));
			}

			BasicDBObject orderIdConditionQuery = null;
			if (orderIdSet != null) {
				List<String> orderIdList = new ArrayList<>(orderIdSet);
				orderIdConditionQuery = new BasicDBObject("$in", orderIdList);
			}
			if (!orderId.isEmpty()) {
				paramConditionLst.add(new BasicDBObject(FieldType.ORDER_ID.getName(), orderId));
			} else if (null != orderIdConditionQuery) {
				paramConditionLst.add(new BasicDBObject(FieldType.ORDER_ID.getName(), orderIdConditionQuery));
			}

			if (StringUtils.isNotEmpty(customerEmail) && !customerEmail.isEmpty()) {
				customerQuery.append(FieldType.CUST_EMAIL.getName(), customerEmail);
				if (StringUtils.isNotBlank(customerEmail)) {
					String email = customerEmail;
					String emailMask = null;
					final String mask = "*****";
					final int at = email.indexOf("@");
					if (at > 2) {
						final int maskLen = Math.min(Math.max(at / 2, 2), 5);
						final int start1 = (at - maskLen) / 2;
						emailMask = email.substring(0, start1) + mask.substring(0, maskLen)
								+ email.substring(start1 + maskLen);
					}
					customerQueryMask.append(FieldType.CUST_EMAIL.getName(), emailMask);
				}
			}
			if (StringUtils.isNotBlank(Userstatus) && !Userstatus.equalsIgnoreCase("ALL")) {
				String statusArr[] = Userstatus.split(",");
				for (String sts : statusArr) {
					statusConditionList.add(sts.trim());
				}
				statusQuery.append("$in", statusConditionList);
				paramConditionLst.add(new BasicDBObject(FieldType.STATUS.getName(), statusQuery));
			}

			if (!currency.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.CURRENCY_CODE.getName(), currency));
			}
			if (!partSettleFlag.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.PART_SETTLE.getName(), partSettleFlag));
			}

			if (!transactionType.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.ORIG_TXNTYPE.getName(), transactionType));
			}

			if (!paymentRegion.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.PAYMENTS_REGION.getName(), paymentRegion));
			}

			if (StringUtils.isNotBlank(transactionFlag) && !transactionFlag.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.TXN_CAPTURE_FLAG.getName(), transactionFlag));
			}

			if (StringUtils.isNotBlank(deltaFlag) && !deltaFlag.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.DELTA_REFUND_FLAG.getName(), deltaFlag));
			}

			if (StringUtils.isNotBlank(pgRefNum) || StringUtils.isNotBlank(orderId)|| StringUtils.isNotBlank(rrn)) {

			} else {

				if (!dateIndexConditionQuery.isEmpty()) {
					paramConditionLst.add(dateIndexConditionQuery);
				}
			}

			List<BasicDBObject> fianlQueryList = new ArrayList<BasicDBObject>();

			fianlQueryList.addAll(paramConditionLst);

			if (!customerQuery.isEmpty()) {
				paramConditionLst.add(customerQuery);
			}

			if (!customerQueryMask.isEmpty()) {
				fianlQueryList.add(customerQueryMask);
			}

			BasicDBObject finalquery = new BasicDBObject("$and", paramConditionLst);
			BasicDBObject finalqueryMask = new BasicDBObject("$and", fianlQueryList);

			if (!customerQueryMask.isEmpty()) {
				logger.info("Inside TxnReports , searchPayment , finalQuery = " + finalqueryMask);
			} else {
				logger.info("Inside TxnReports , searchPayment , finalQuery = " + finalquery);
			}

			MongoDatabase dbIns = mongoInstance.getDB();

			List<BasicDBObject> pipeline = null;
			BasicDBObject skip = null;
			BasicDBObject limit = null;

			BasicDBObject projectElement = new BasicDBObject();
			projectElement.put(FieldType.TXN_ID.getName(), 1);
			projectElement.put(FieldType.PG_REF_NUM.getName(), 1);
			projectElement.put(FieldType.PAY_ID.getName(), 1);
			projectElement.put(FieldType.CUST_NAME.getName(), 1);
			projectElement.put(FieldType.CUST_PHONE.getName(), 1);
			projectElement.put(FieldType.CUST_EMAIL.getName(), 1);
			projectElement.put(FieldType.PAYOUT_DATE.getName(), 1);
			projectElement.put(FieldType.UTR_NO.getName(), 1);
			projectElement.put(CrmFieldType.BUSINESS_NAME.getName(), 1);
			projectElement.put(FieldType.SUB_MERCHANT_ID.getName(), 1);
			projectElement.put(FieldType.REFUND_DAYS.getName(), 1);
			projectElement.put(FieldType.PRODUCT_PRICE.getName(), 1);
			projectElement.put(FieldType.VENDOR_ID.getName(), 1);
			projectElement.put(FieldType.SKU_CODE.getName(), 1);
			projectElement.put(FieldType.CATEGORY_CODE.getName(), 1);
			projectElement.put(FieldType.TXNTYPE.getName(), 1);
			projectElement.put(FieldType.ORIG_TXNTYPE.getName(), 1);
			projectElement.put(FieldType.STATUS.getName(), 1);
			projectElement.put(FieldType.MOP_TYPE.getName(), 1);
			projectElement.put(FieldType.PAYMENT_TYPE.getName(), 1);
			projectElement.put(FieldType.ACQUIRER_TYPE.getName(), 1);
			projectElement.put(FieldType.ACQ_ID.getName(), 1);
			projectElement.put(FieldType.UDF11.getName(), 1);
			projectElement.put(FieldType.UDF12.getName(), 1);
			projectElement.put(FieldType.UDF13.getName(), 1);
			projectElement.put(FieldType.UDF14.getName(), 1);
			projectElement.put(FieldType.UDF15.getName(), 1);
			projectElement.put(FieldType.UDF16.getName(), 1);
			projectElement.put(FieldType.UDF17.getName(), 1);
			projectElement.put(FieldType.UDF18.getName(), 1);
			projectElement.put(FieldType.RRN.getName(), 1);
			projectElement.put(FieldType.TXN_CAPTURE_FLAG.getName(), 1);
			projectElement.put(FieldType.TRANSACTION_MODE.getName(), 1);
			projectElement.put(FieldType.RESPONSE_MESSAGE.getName(), 1);
			projectElement.put(FieldType.PG_TXN_MESSAGE.getName(), 1);
			projectElement.put(FieldType.CARD_MASK.getName(), 1);
			projectElement.put(FieldType.PAYER_ADDRESS.getName(), 1);
			projectElement.put(FieldType.PART_SETTLE.getName(), 1);
			projectElement.put(FieldType.CREATE_DATE.getName(), 1);
			projectElement.put(FieldType.AMOUNT.getName(), 1);
			projectElement.put(FieldType.ORDER_ID.getName(), 1);
			projectElement.put(FieldType.REFUND_ORDER_ID.getName(), 1);
			projectElement.put(FieldType.PRODUCT_DESC.getName(), 1);
			projectElement.put(FieldType.PG_DATE_TIME.getName(), 1);
			projectElement.put(FieldType.INTERNAL_CARD_ISSUER_BANK.getName(), 1);
			projectElement.put(FieldType.INTERNAL_CARD_ISSUER_COUNTRY.getName(), 1);
			projectElement.put(FieldType.REFUNDABLE_AMOUNT.getName(), 1);
			projectElement.put(FieldType.TOTAL_AMOUNT.getName(), 1);
			projectElement.put(FieldType.CURRENCY_CODE.getName(), 1);
			projectElement.put(FieldType.PAYMENTS_REGION.getName(), 1);
			projectElement.put(FieldType.CARD_HOLDER_TYPE.getName(), 1);
			projectElement.put(FieldType.RESELLER_CHARGES.getName(), 1);
			projectElement.put(FieldType.RESELLER_GST.getName(), 1);
			projectElement.put(FieldType.ACQUIRER_TDR_SC.getName(), 1);
			projectElement.put(FieldType.PG_TDR_SC.getName(), 1);
			projectElement.put(FieldType.ACQUIRER_GST.getName(), 1);
			projectElement.put(FieldType.PG_GST.getName(), 1);
			projectElement.put(FieldType.SETTLEMENT_FLAG.getName(), 1);
			projectElement.put(FieldType.SETTLEMENT_DATE.getName(), 1);
			projectElement.put(FieldType.IS_ENCRYPTED.getName(), 1);

			BasicDBObject project = new BasicDBObject("$project", projectElement);
			MongoCollection<Document> coll = null;

			if (searchFlag == true) {
				coll = dbIns.getCollection(propertiesManager.propertiesMap
						.get(prefix + Constants.TRANSACTION_STATUS_COLLECTION.getValue()));
			} else {
				coll = dbIns.getCollection(
						propertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));
			}

			BasicDBObject match = new BasicDBObject("$match", finalquery);
			// BasicDBObject sort = new BasicDBObject("$sort", new
			// BasicDBObject("CREATE_DATE", -1));
			BasicDBObject sort = null;
			if (StringUtils.isNotBlank(orderId))
				sort = new BasicDBObject("$sort", new BasicDBObject("CREATE_DATE", 1));
			else
				sort = new BasicDBObject("$sort", new BasicDBObject("CREATE_DATE", -1));

			skip = new BasicDBObject("$skip", start);
			limit = new BasicDBObject("$limit", length);
			pipeline = Arrays.asList(match, project, sort, skip, limit);
			AggregateIterable<Document> output = coll.aggregate(pipeline);
			output.allowDiskUse(true);
			MongoCursor<Document> cursor = output.iterator();

//			 Map<String,String> checkedOrderIds = new HashMap<String,String>();

			while (cursor.hasNext()) {
				Document dbobj = cursor.next();
				if (StringUtils.isNotBlank(dbobj.getString("IS_ENCRYPTED"))
						&& dbobj.getString("IS_ENCRYPTED").equalsIgnoreCase("Y")) {
					dbobj = dataEncDecTool.decryptDocument(dbobj);
				}

				// key will be ORDERID+PAYID+CURRENCYCODE , if found the value skip next
				// transactions for the ORDER_ID
//                String searchKey = dbobj.get(FieldType.ORDER_ID.getName())+dbobj.getString(FieldType.PAY_ID.getName())+dbobj.getString(FieldType.CURRENCY_CODE.getName());
//
//                if(checkedOrderIds.containsKey(searchKey)){
//                    logger.info("found Key searchKey "+searchKey);
//                    continue;
//                }
//                
//                if(dbobj.getString(FieldType.STATUS.getName()).equalsIgnoreCase(StatusType.CAPTURED.getName())){
//                    logger.info("found Status Captured "+searchKey);
//                    checkedOrderIds.put(searchKey, StatusType.CAPTURED.getName());
//                }

				TransactionSearch transReport = new TransactionSearch();
				BigInteger txnID = new BigInteger(((Document) dbobj).getString(FieldType.TXN_ID.toString()));
				transReport.setTransactionId((txnID));
				transReport.setTxnId(((Document) dbobj).getString(FieldType.TXN_ID.toString()));
				transReport.setPgRefNum(((Document) dbobj).getString(FieldType.PG_REF_NUM.toString()));
				transReport.setPayId(((Document) dbobj).getString(FieldType.PAY_ID.toString()));

				if (null != ((Document) dbobj).getString(FieldType.CUST_NAME.toString())) {
					transReport.setCustomerName(((Document) dbobj).getString(FieldType.CUST_NAME.toString()));
				} else {
					transReport.setCustomerName(CrmFieldConstants.NA.getValue());
				}

				if (null != ((Document) dbobj).getString(FieldType.CUST_PHONE.toString())) {
					transReport.setCustomerMobile(((Document) dbobj).getString(FieldType.CUST_PHONE.toString()));
				} else {
					transReport.setCustomerMobile(CrmFieldConstants.NA.getValue());
				}

				if (null != ((Document) dbobj).getString(FieldType.CUST_EMAIL.toString())) {
					transReport.setCustomerEmail(((Document) dbobj).getString(FieldType.CUST_EMAIL.toString()));
				} else {
					transReport.setCustomerEmail(CrmFieldConstants.NA.getValue());
				}

				if (null != ((Document) dbobj).getString(FieldType.PAYOUT_DATE.toString())) {
					transReport.setPayOutDate(((Document) dbobj).getString(FieldType.PAYOUT_DATE.toString()));
				} else {
					transReport.setPayOutDate(CrmFieldConstants.NA.getValue());
				}
				if (null != ((Document) dbobj).getString(FieldType.UTR_NO.toString())) {
					transReport.setUtrNo(((Document) dbobj).getString(FieldType.UTR_NO.toString()));
				} else {
					transReport.setUtrNo(CrmFieldConstants.NA.getValue());
				}

				if (StringUtils.isNotBlank(dbobj.getString("IS_CUSTOM_HOSTED"))
						&& dbobj.getString("IS_CUSTOM_HOSTED").equalsIgnoreCase("Y")) {
					transReport.setCustomFlag(((Document) dbobj).getString("IS_CUSTOM_HOSTED"));
				} else {
					transReport.setCustomFlag("N");
				}

				User user1 = new User();

				if (StringUtils.isNotBlank((String) dbobj.get(FieldType.PAY_ID.getName()))) {
					String payid = (String) dbobj.get(FieldType.PAY_ID.getName());

					if (userMap.get(payid) != null && !userMap.get(payid).getPayId().isEmpty()) {
						user1 = userMap.get(payid);
					} else {
						user1 = userdao.findPayId(payid);
						userMap.put(payid, user1);
					}
				}
				if (user1 != null) {
					transReport.setMerchants(user1.getBusinessName());
				}

				if (user.getUserType().equals(UserType.PARENTMERCHANT)) {
					transReport.setMerchants(user.getBusinessName());
				}

				// if (!merchantPayId.equalsIgnoreCase("All") &&
				if (dbobj.containsKey(FieldType.SUB_MERCHANT_ID.getName())
						&& StringUtils.isNotBlank(dbobj.getString(FieldType.SUB_MERCHANT_ID.getName()))) {

					String subMerchant = dbobj.getString(FieldType.SUB_MERCHANT_ID.getName());
					User subMerchantUser = new User();

					if (userMap.get(subMerchant) != null && !userMap.get(subMerchant).getPayId().isEmpty()) {
						subMerchantUser = userMap.get(subMerchant);
					} else {
						subMerchantUser = userdao.findPayId(subMerchant);
						userMap.put(subMerchant, subMerchantUser);
					}
					if (subMerchantUser != null) {
						transReport.setSubMerchantId(subMerchantUser.getBusinessName());
					} else {
						transReport.setSubMerchantId(CrmFieldConstants.NA.getValue());
					}
				} else {
					if ((!pgRefNum.isEmpty() || !orderId.isEmpty())
							&& dbobj.containsKey(FieldType.SUB_MERCHANT_ID.getName())
							&& StringUtils.isNotBlank(dbobj.getString(FieldType.SUB_MERCHANT_ID.getName()))) {
						transReport.setSubMerchantId(
								userdao.getBusinessNameByPayId(dbobj.getString(FieldType.SUB_MERCHANT_ID.getName())));
					}
				}

				if (merchantPayId.equalsIgnoreCase("All")) {

					if (user.getUserType().equals(UserType.RESELLER)) {

					} else {

						if (StringUtils.isNotBlank(dbobj.getString(FieldType.REFUND_DAYS.getName()))) {
							transReport.setRefundCycle(dbobj.getString(FieldType.REFUND_DAYS.getName()));
						} else {
							transReport.setRefundCycle(CrmFieldConstants.NA.getValue());
						}

						if (StringUtils.isNotBlank(dbobj.getString(FieldType.PRODUCT_PRICE.getName()))) {
							transReport.setProductPrice(dbobj.getString(FieldType.PRODUCT_PRICE.getName()));
						} else {
							transReport.setProductPrice(CrmFieldConstants.NA.getValue());
						}
						if (StringUtils.isNotBlank(dbobj.getString(FieldType.VENDOR_ID.getName()))) {
							transReport.setVendorID(dbobj.getString(FieldType.VENDOR_ID.getName()));
						} else {
							transReport.setVendorID(CrmFieldConstants.NA.getValue());
						}
						if (StringUtils.isNotBlank(dbobj.getString(FieldType.SKU_CODE.getName()))) {
							transReport.setSKUCode(dbobj.getString(FieldType.SKU_CODE.getName()));
						} else {
							transReport.setSKUCode(CrmFieldConstants.NA.getValue());
						}

						if (StringUtils.isNotBlank(dbobj.getString(FieldType.CATEGORY_CODE.getName()))) {
							transReport.setCategoryCode(dbobj.getString(FieldType.CATEGORY_CODE.getName()));
						} else {
							transReport.setCategoryCode(CrmFieldConstants.NA.getValue());
						}
					}
				} else {

					if (user.getUserType().equals(UserType.ADMIN) || user.getUserType().equals(UserType.SUBADMIN)
							|| user.getUserType().equals(UserType.RESELLER)) {
						// User merhant = userMap.get(merchantPayId); // userdao.findPayId(payid);
						UserSettingData merchantSettings = userSettingDao.fetchDataUsingPayId(merchantPayId);

						if (merchantSettings.isRetailMerchantFlag()) {
							if (StringUtils.isNotBlank(dbobj.getString(FieldType.REFUND_DAYS.getName()))) {
								transReport.setRefundCycle(dbobj.getString(FieldType.REFUND_DAYS.getName()));
							} else {
								transReport.setRefundCycle(CrmFieldConstants.NA.getValue());
							}

							if (StringUtils.isNotBlank(dbobj.getString(FieldType.PRODUCT_PRICE.getName()))) {
								transReport.setProductPrice(dbobj.getString(FieldType.PRODUCT_PRICE.getName()));
							} else {
								transReport.setProductPrice(CrmFieldConstants.NA.getValue());
							}
							if (StringUtils.isNotBlank(dbobj.getString(FieldType.VENDOR_ID.getName()))) {
								transReport.setVendorID(dbobj.getString(FieldType.VENDOR_ID.getName()));
							} else {
								transReport.setVendorID(CrmFieldConstants.NA.getValue());
							}
							if (StringUtils.isNotBlank(dbobj.getString(FieldType.SKU_CODE.getName()))) {
								transReport.setSKUCode(dbobj.getString(FieldType.SKU_CODE.getName()));
							} else {
								transReport.setSKUCode(CrmFieldConstants.NA.getValue());
							}

							if (StringUtils.isNotBlank(dbobj.getString(FieldType.CATEGORY_CODE.getName()))) {
								transReport.setCategoryCode(dbobj.getString(FieldType.CATEGORY_CODE.getName()));
							} else {
								transReport.setCategoryCode(CrmFieldConstants.NA.getValue());
							}
						}
					} else {

						if (userSettings.isRetailMerchantFlag()) {
							if (StringUtils.isNotBlank(dbobj.getString(FieldType.REFUND_DAYS.getName()))) {
								transReport.setRefundCycle(dbobj.getString(FieldType.REFUND_DAYS.getName()));
							} else {
								transReport.setRefundCycle(CrmFieldConstants.NA.getValue());
							}

							if (StringUtils.isNotBlank(dbobj.getString(FieldType.PRODUCT_PRICE.getName()))) {
								transReport.setProductPrice(dbobj.getString(FieldType.PRODUCT_PRICE.getName()));
							} else {
								transReport.setProductPrice(CrmFieldConstants.NA.getValue());
							}
							if (StringUtils.isNotBlank(dbobj.getString(FieldType.VENDOR_ID.getName()))) {
								transReport.setVendorID(dbobj.getString(FieldType.VENDOR_ID.getName()));
							} else {
								transReport.setVendorID(CrmFieldConstants.NA.getValue());
							}
							if (StringUtils.isNotBlank(dbobj.getString(FieldType.SKU_CODE.getName()))) {
								transReport.setSKUCode(dbobj.getString(FieldType.SKU_CODE.getName()));
							} else {
								transReport.setSKUCode(CrmFieldConstants.NA.getValue());
							}

							if (StringUtils.isNotBlank(dbobj.getString(FieldType.CATEGORY_CODE.getName()))) {
								transReport.setCategoryCode(dbobj.getString(FieldType.CATEGORY_CODE.getName()));
							} else {
								transReport.setCategoryCode(CrmFieldConstants.NA.getValue());
							}
						}

					}

				}

				if (StringUtils.isNotBlank(deliveryStatus)) {
					transReport.setDeliveryStatus(deliveryStatus);
				} else {
					transReport.setDeliveryStatus("");
				}

				if ((StringUtils.isNotBlank(dbobj.getString(FieldType.TXNTYPE.getName()))) && (dbobj
						.getString(FieldType.TXNTYPE.toString()).equalsIgnoreCase(TransactionType.REFUND.getName()))) {
					if (StringUtils.isNotBlank(dbobj.getString(FieldType.SETTLEMENT_FLAG.getName()))
							&& dbobj.getString(FieldType.SETTLEMENT_FLAG.getName()).equals("Y")) {
						transReport.setTxnType(TransactionType.REFUND.getName());
					} else {
						transReport.setTxnType(dbobj.getString(FieldType.TXNTYPE.toString()));
					}
				} else if (StringUtils.isNotBlank(dbobj.getString(FieldType.ORIG_TXNTYPE.toString()))) {

					if (StringUtils.isNotBlank(dbobj.getString(FieldType.SETTLEMENT_FLAG.getName()))
							&& dbobj.getString(FieldType.SETTLEMENT_FLAG.getName()).equals("Y")) {
						transReport.setTxnType(TransactionType.SALE.getName());
					} else {
						transReport.setTxnType(dbobj.getString(FieldType.ORIG_TXNTYPE.toString()));
					}
				} else {

					// If ORIG_TXN_TYPE is not available incase of a timeout ,
					// set TXNTYPE instead
					// of ORIG_TXN_TYPE

					if (dbobj.getString(FieldType.STATUS.toString()).equalsIgnoreCase(StatusType.TIMEOUT.getName())) {
						transReport.setTxnType(dbobj.getString(FieldType.TXNTYPE.toString()));
					} else {
						transReport.setTxnType(CrmFieldConstants.NA.getValue());
					}
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.MOP_TYPE.toString()))) {
					transReport.setMopType(MopType.getmopName(dbobj.getString(FieldType.MOP_TYPE.toString())));
				} else {
					transReport.setMopType(CrmFieldConstants.NOT_AVAILABLE.getValue());
				}
				if (StringUtils.isNotBlank(dbobj.getString(FieldType.PAYMENT_TYPE.toString()))) {
					transReport.setPaymentMethods(
							PaymentType.getpaymentName(dbobj.getString(FieldType.PAYMENT_TYPE.toString())));
				} else {
					transReport.setPaymentMethods(CrmFieldConstants.NOT_AVAILABLE.getValue());
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TYPE.getName()))) {
					transReport.setAcquirerType(dbobj.getString(FieldType.ACQUIRER_TYPE.getName()));
				} else {
					transReport.setAcquirerType(CrmFieldConstants.NOT_AVAILABLE.getValue());
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQ_ID.getName()))) {
					transReport.setAcqId(dbobj.getString(FieldType.ACQ_ID.getName()));
				} else {
					transReport.setAcqId(CrmFieldConstants.NA.getValue());
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF11.getName()))) {
					transReport.setUDF11(dbobj.getString(FieldType.UDF11.getName()));
				} else {
					transReport.setUDF11(CrmFieldConstants.NA.getValue());
				}
				if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF12.getName()))) {
					transReport.setUDF12(dbobj.getString(FieldType.UDF12.getName()));
				} else {
					transReport.setUDF12(CrmFieldConstants.NA.getValue());
				}
				if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF13.getName()))) {
					transReport.setUDF13(dbobj.getString(FieldType.UDF13.getName()));
				} else {
					transReport.setUDF13(CrmFieldConstants.NA.getValue());
				}
				if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF14.getName()))) {
					transReport.setUDF14(dbobj.getString(FieldType.UDF14.getName()));
				} else {
					transReport.setUDF14(CrmFieldConstants.NA.getValue());
				}
				if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF15.getName()))) {
					transReport.setUDF15(dbobj.getString(FieldType.UDF15.getName()));
				} else {
					transReport.setUDF15(CrmFieldConstants.NA.getValue());
				}
				if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF16.getName()))) {
					transReport.setUDF16(dbobj.getString(FieldType.UDF16.getName()));
				} else {
					transReport.setUDF16(CrmFieldConstants.NA.getValue());
				}
				if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF17.getName()))) {
					transReport.setUDF17(dbobj.getString(FieldType.UDF17.getName()));
				} else {
					transReport.setUDF17(CrmFieldConstants.NA.getValue());
				}
				if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF18.getName()))) {
					transReport.setUDF18(dbobj.getString(FieldType.UDF18.getName()));
				} else {
					transReport.setUDF18(CrmFieldConstants.NA.getValue());
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.RRN.getName()))
						&& !dbobj.getString(FieldType.RRN.getName()).equalsIgnoreCase("-")) {
					transReport.setRrn(dbobj.getString(FieldType.RRN.getName()));
				} else {
					transReport.setRrn(CrmFieldConstants.NA.getValue());
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.TXN_CAPTURE_FLAG.getName()))) {
					transReport.setTxnSettledType(dbobj.getString(FieldType.TXN_CAPTURE_FLAG.getName()));
				} else {
					transReport.setTxnSettledType(CrmFieldConstants.NA.getValue());
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.TRANSACTION_MODE.getName()))) {
					transReport.setTransactionMode(dbobj.getString(FieldType.TRANSACTION_MODE.getName()));
				} else {
				}

				// if
				// (StringUtils.isNotBlank(dbobj.getString(FieldType.POST_SETTLED_FLAG.getName())))
				// {
				// transReport.setPostSettledFlag(dbobj.getString(FieldType.POST_SETTLED_FLAG.getName()));
				// } else {
				// transReport.setPostSettledFlag(CrmFieldConstants.NA.getValue());
				// }

				transReport.setResponseMessage(dbobj.getString(CrmFieldType.RESPONSE_MESSAGE.toString()));
				if (!StringUtils.isEmpty(dbobj.getString(CrmFieldType.PG_TXN_MESSAGE.toString()))) {
					transReport.setAccqResponseMessage(dbobj.getString(CrmFieldType.PG_TXN_MESSAGE.toString()));
				} else {
					transReport.setAccqResponseMessage(CrmFieldConstants.NA.getValue());
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.PAYMENT_TYPE.toString()))) {
					if (null != dbobj.getString(FieldType.CARD_MASK.toString())) {
						transReport.setCardNumber(dbobj.getString(FieldType.CARD_MASK.toString()));
					} else if (null != dbobj.getString(FieldType.PAYER_ADDRESS.getName())) {

						if (user.getUserType().equals(UserType.ADMIN) || user.getUserType().equals(UserType.SUBADMIN)) {
							transReport.setCardNumber(dbobj.getString(FieldType.PAYER_ADDRESS.getName()));
						} else {
							String vpaString = dbobj.getString(FieldType.PAYER_ADDRESS.getName());
							String[] vpaArray = vpaString.split("@");
							char[] vpaChar = vpaArray[0].toCharArray();
							StringBuilder vpastrBuilder = new StringBuilder();

							if (vpaChar.length > 3) {
								for (int i = 0; i < vpaChar.length - 3; i++) {
									vpastrBuilder.append(vpaChar[i]);
								}
								vpastrBuilder.append("***@");
								vpastrBuilder.append(vpaArray[1]);
							} else {
								vpastrBuilder.append(vpaChar[0]);
								vpastrBuilder.append("**@");
								vpastrBuilder.append(vpaArray[1]);
							}

							transReport.setCardNumber(vpastrBuilder.toString());
						}
					} else {
						transReport.setCardNumber(CrmFieldConstants.NA.getValue());
					}
				} else {
					transReport.setCardNumber(CrmFieldConstants.NA.getValue());
				}
				if (StringUtils.isNotBlank(dbobj.getString(FieldType.PART_SETTLE.toString()))
						&& dbobj.getString(FieldType.PART_SETTLE.toString()).equalsIgnoreCase(("Y"))) {
					transReport.setPartSettle(dbobj.getString(FieldType.PART_SETTLE.toString()));
				} else {
					transReport.setPartSettle(CrmFieldConstants.NA.getValue());
				}

				if (((user.getUserType().equals(UserType.MERCHANT)) || (user.getUserType().equals(UserType.SUBUSER)))
						&& userSettings.isCustomTransactionStatus()) {
					if (((dbobj.getString(FieldType.STATUS.toString())).equalsIgnoreCase(StatusType.TIMEOUT.getName()))
							|| ((dbobj.getString(FieldType.STATUS.toString()))
									.equalsIgnoreCase(StatusType.CAPTURED.getName()))
							|| ((dbobj.getString(FieldType.STATUS.toString()))
									.equalsIgnoreCase(StatusType.ENROLLED.getName()))
							|| ((dbobj.getString(FieldType.STATUS.toString()))
									.equalsIgnoreCase(StatusType.SETTLED.getName()))
							|| ((dbobj.getString(FieldType.STATUS.toString()))
									.equalsIgnoreCase(StatusType.SENT_TO_BANK.getName()))
							|| ((dbobj.getString(FieldType.STATUS.toString()))
									.equalsIgnoreCase(StatusType.PENDING.getName()))) {

						// if
						// (StringUtils.isNotBlank(dbobj.getString(FieldType.SETTLEMENT_FLAG.getName()))
						// &&
						// dbobj.getString(FieldType.SETTLEMENT_FLAG.getName()).equals("Y"))
						// {
						// transReport.setStatus(StatusType.SETTLED.getName());
						// } else {
						// transReport.setStatus(dbobj.getString(FieldType.STATUS.toString()));
						// }
						transReport.setStatus(dbobj.getString(FieldType.STATUS.toString()));
					} else {
						transReport.setStatus("Failed");
					}
				} else {
					// if
					// (StringUtils.isNotBlank(dbobj.getString(FieldType.SETTLEMENT_FLAG.getName()))
					// &&
					// dbobj.getString(FieldType.SETTLEMENT_FLAG.getName()).equals("Y"))
					// {
					// transReport.setStatus(StatusType.SETTLED.getName());
					// } else {
					// transReport.setStatus(dbobj.getString(FieldType.STATUS.toString()));
					// }
					transReport.setStatus(dbobj.getString(FieldType.STATUS.toString()));
				}
				// if
				// (StringUtils.isNotBlank(dbobj.getString(FieldType.SETTLEMENT_FLAG.getName()))
				// &&
				// dbobj.getString(FieldType.SETTLEMENT_FLAG.getName()).equals("Y"))
				// {
				// transReport.setDateFrom(dbobj.getString(FieldType.SETTLEMENT_DATE.getName()));
				// } else {
				// transReport.setDateFrom(dbobj.getString(FieldType.CREATE_DATE.getName()));
				// }
				transReport.setDateFrom(dbobj.getString(FieldType.CREATE_DATE.getName()));
				transReport.setAmount(dbobj.getString(FieldType.AMOUNT.toString()));
				transReport.setOrderId(dbobj.getString(FieldType.ORDER_ID.toString()));

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.REFUND_ORDER_ID.toString()))) {
					transReport.setRefundOrderId(dbobj.getString(FieldType.REFUND_ORDER_ID.toString()));
				} else {
					transReport.setRefundOrderId(CrmFieldConstants.NA.getValue());
				}

				transReport.setoId(dbobj.getString(FieldType.OID.toString()));

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.PRODUCT_DESC.toString()))) {
					transReport.setProductDesc(dbobj.getString(FieldType.PRODUCT_DESC.toString()));
				} else {
					transReport.setProductDesc(CrmFieldConstants.NA.getValue());
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.PG_DATE_TIME.getName()))) {
					transReport.setTransactionCaptureDate(dbobj.getString(FieldType.PG_DATE_TIME.toString()));
				} else {
					transReport.setTransactionCaptureDate(CrmFieldConstants.NA.getValue());
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.INTERNAL_CARD_ISSUER_BANK.getName()))) {
					transReport.setInternalCardIssusserBank(
							dbobj.getString(FieldType.INTERNAL_CARD_ISSUER_BANK.toString()));
				} else {
					transReport.setInternalCardIssusserBank(CrmFieldConstants.NA.getValue());
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.INTERNAL_CARD_ISSUER_COUNTRY.getName()))) {
					transReport.setInternalCardIssusserCountry(
							dbobj.getString(FieldType.INTERNAL_CARD_ISSUER_COUNTRY.toString()));
				} else {
					transReport.setInternalCardIssusserCountry(CrmFieldConstants.NA.getValue());
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.REFUNDABLE_AMOUNT.getName()))) {
					transReport.setRefundableAmount(dbobj.getString(FieldType.REFUNDABLE_AMOUNT.toString()));
				} else {
					transReport.setRefundableAmount(CrmFieldConstants.NA.getValue());
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.AMOUNT.getName()))) {
					transReport.setApprovedAmount(dbobj.getString(FieldType.AMOUNT.toString()));
				} else {
					transReport.setApprovedAmount(CrmFieldConstants.NA.getValue());
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))) {
					transReport.setTotalAmount(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()));
				} else {
					transReport.setTotalAmount("");
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.CURRENCY_CODE.toString()))) {
					transReport.setCurrency(
							propManager.getAlphabaticCurrencyCode(dbobj.getString(FieldType.CURRENCY_CODE.toString())));
				} else {
					transReport.setCurrency(CrmFieldConstants.NA.getValue());
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.PAYMENTS_REGION.toString()))) {
					transReport.setPaymentRegion(dbobj.getString(FieldType.PAYMENTS_REGION.toString()));

				} else {
					transReport.setPaymentRegion(CrmFieldConstants.NA.getValue());
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.CARD_HOLDER_TYPE.toString()))) {
					transReport.setCardHolderType(dbobj.getString(FieldType.CARD_HOLDER_TYPE.toString()));

				} else {
					transReport.setCardHolderType(CrmFieldConstants.NA.getValue());
				}
				// changes regarding partner reseller
				if (user.getUserType().equals(UserType.RESELLER) && user.isPartnerFlag()) {

					if (StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_CHARGES.toString()))) {

						transReport.setResellerCharges(String.format("%.2f",
								(Double.parseDouble(dbobj.getString(FieldType.RESELLER_CHARGES.toString())))));

					} else {
						transReport.setResellerCharges("0.00");
					}

					if (StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_GST.toString()))) {
						transReport.setResellerGST(String.format("%.2f",
								(Double.parseDouble(dbobj.getString(FieldType.RESELLER_GST.toString())))));

					} else {
						transReport.setResellerGST("0.00");
					}

					if (StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_CHARGES.toString()))) {
						if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
								&& (StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.toString())))) {

							transReport.setTdr_Surcharge(String.format("%.2f",
									(Double.parseDouble(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
											+ Double.parseDouble(dbobj.getString(FieldType.PG_TDR_SC.toString()))
											+ Double.parseDouble(
													dbobj.getString(FieldType.RESELLER_CHARGES.toString())))));

						} else {
							transReport.setTdr_Surcharge("0.00");
						}
					} else {
						if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
								&& (StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.toString())))) {

							transReport.setTdr_Surcharge(String.format("%.2f",
									(Double.parseDouble(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
											+ Double.parseDouble(dbobj.getString(FieldType.PG_TDR_SC.toString())))));

						} else {
							transReport.setTdr_Surcharge("0.00");
						}
					}

					if (StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_GST.toString()))) {
						if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.toString()))
								&& (StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.toString())))) {
							transReport.setGst_charge(String.format("%.2f",
									(Double.parseDouble(dbobj.getString(FieldType.ACQUIRER_GST.toString()))
											+ Double.parseDouble(dbobj.getString(FieldType.PG_GST.toString()))
											+ Double.parseDouble(dbobj.getString(FieldType.RESELLER_GST.toString())))));

						} else {
							transReport.setGst_charge("0.00");
						}
					} else {
						if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.toString()))
								&& (StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.toString())))) {
							transReport.setGst_charge(String.format("%.2f",
									(Double.parseDouble(dbobj.getString(FieldType.ACQUIRER_GST.toString()))
											+ Double.parseDouble(dbobj.getString(FieldType.PG_GST.toString())))));

						} else {
							transReport.setGst_charge("0.00");
						}
					}

				} // END

				else {

					if (StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_CHARGES.toString()))) {
						if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
								&& (StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.toString())))) {

							transReport.setTdr_Surcharge(String.format("%.2f",
									(Double.parseDouble(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
											+ Double.parseDouble(dbobj.getString(FieldType.PG_TDR_SC.toString()))
											+ Double.parseDouble(
													dbobj.getString(FieldType.RESELLER_CHARGES.toString())))));

						} else {
							transReport.setTdr_Surcharge("0.00");
						}
					} else {
						if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
								&& (StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.toString())))) {

							transReport.setTdr_Surcharge(String.format("%.2f",
									(Double.parseDouble(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
											+ Double.parseDouble(dbobj.getString(FieldType.PG_TDR_SC.toString())))));

						} else {
							transReport.setTdr_Surcharge("0.00");
						}
					}

					if (StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_GST.toString()))) {
						if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.toString()))
								&& (StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.toString())))) {
							transReport.setGst_charge(String.format("%.2f",
									(Double.parseDouble(dbobj.getString(FieldType.ACQUIRER_GST.toString()))
											+ Double.parseDouble(dbobj.getString(FieldType.PG_GST.toString()))
											+ Double.parseDouble(dbobj.getString(FieldType.RESELLER_GST.toString())))));

						} else {
							transReport.setGst_charge("0.00");
						}
					} else {
						if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.toString()))
								&& (StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.toString())))) {
							transReport.setGst_charge(String.format("%.2f",
									(Double.parseDouble(dbobj.getString(FieldType.ACQUIRER_GST.toString()))
											+ Double.parseDouble(dbobj.getString(FieldType.PG_GST.toString())))));

						} else {
							transReport.setGst_charge("0.00");
						}
					}

				}

				if (TxnType.SALE.getName().equalsIgnoreCase(dbobj.getString(FieldType.ORIG_TXNTYPE.getName()))) {

					if (StringUtils.isNotBlank(dbobj.getString(FieldType.PAYMENT_TYPE.toString()))) {

						if (dbobj.getString(FieldType.PAYMENT_TYPE.getName())
								.equalsIgnoreCase(PaymentType.COD.getCode())) {

							if (isGlocal == true) {
								transReport.setGlocalFlag(true);
								if (dbobj.containsKey(FieldType.UDF7.getName())) {

									if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF7.getName()))) {

										if (StringUtils.isNotBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
												&& StringUtils
														.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.toString()))
												&& StringUtils.isNotBlank(
														dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
												&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.toString()))
												&& StringUtils.isNotBlank(
														dbobj.getString(FieldType.ACQUIRER_GST.toString()))) {

											String merchantAmount = String.format("%.2f", Double
													.parseDouble(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
													- (Double.parseDouble(
															dbobj.getString(FieldType.PG_TDR_SC.toString()))
															+ Double.parseDouble(dbobj
																	.getString(FieldType.ACQUIRER_TDR_SC.toString()))
															+ Double.parseDouble(
																	dbobj.getString(FieldType.PG_GST.toString()))
															+ Double.parseDouble(dbobj
																	.getString(FieldType.ACQUIRER_GST.toString()))));

											transReport.setDoctor(String
													.valueOf(new BigDecimal(dbobj.getString(FieldType.UDF7.getName()))
															.multiply(new BigDecimal(merchantAmount))
															.divide(new BigDecimal(
																	Constants.MAX_NUMBER_OF_KEYS.getValue()))
															.setScale(2, BigDecimal.ROUND_HALF_UP)));
										} else {
											transReport.setDoctor("0");
										}
									} else {
										transReport.setDoctor("0");
									}
								} else {
									transReport.setDoctor("0");
								}

								if (dbobj.containsKey(FieldType.UDF8.getName())) {
									if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF8.getName()))) {

										if (StringUtils.isNotBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
												&& StringUtils
														.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.toString()))
												&& StringUtils.isNotBlank(
														dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
												&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.toString()))
												&& StringUtils.isNotBlank(
														dbobj.getString(FieldType.ACQUIRER_GST.toString()))) {

											String merchantAmount = String.format("%.2f", Double
													.parseDouble(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
													- (Double.parseDouble(
															dbobj.getString(FieldType.PG_TDR_SC.toString()))
															+ Double.parseDouble(dbobj
																	.getString(FieldType.ACQUIRER_TDR_SC.toString()))
															+ Double.parseDouble(
																	dbobj.getString(FieldType.PG_GST.toString()))
															+ Double.parseDouble(dbobj
																	.getString(FieldType.ACQUIRER_GST.toString()))));

											transReport.setGlocal(String
													.valueOf(new BigDecimal(dbobj.getString(FieldType.UDF8.getName()))
															.multiply(new BigDecimal(merchantAmount))
															.divide(new BigDecimal(
																	Constants.MAX_NUMBER_OF_KEYS.getValue()))
															.setScale(2, BigDecimal.ROUND_HALF_UP)));
										} else {
											transReport.setGlocal("0");
										}
									} else {
										transReport.setGlocal("0");
									}
								} else {
									transReport.setGlocal("0");
								}

								if (dbobj.containsKey(FieldType.UDF9.getName())) {
									if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF9.getName()))) {

										if (StringUtils.isNotBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
												&& StringUtils
														.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.toString()))
												&& StringUtils.isNotBlank(
														dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
												&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.toString()))
												&& StringUtils.isNotBlank(
														dbobj.getString(FieldType.ACQUIRER_GST.toString()))) {

											String merchantAmount = String.format("%.2f", Double
													.parseDouble(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
													- (Double.parseDouble(
															dbobj.getString(FieldType.PG_TDR_SC.toString()))
															+ Double.parseDouble(dbobj
																	.getString(FieldType.ACQUIRER_TDR_SC.toString()))
															+ Double.parseDouble(
																	dbobj.getString(FieldType.PG_GST.toString()))
															+ Double.parseDouble(dbobj
																	.getString(FieldType.ACQUIRER_GST.toString()))));

											transReport.setPartner(String
													.valueOf(new BigDecimal(dbobj.getString(FieldType.UDF9.getName()))
															.multiply(new BigDecimal(merchantAmount))
															.divide(new BigDecimal(
																	Constants.MAX_NUMBER_OF_KEYS.getValue()))
															.setScale(2, BigDecimal.ROUND_HALF_UP)));
										} else {
											transReport.setPartner("0");
										}
									} else {
										transReport.setPartner("0");
									}
								} else {
									transReport.setPartner("0");
								}

								if (dbobj.containsKey(FieldType.UDF11.getName())) {
									if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF11.getName()))) {
										transReport.setUniqueId(dbobj.getString(FieldType.UDF11.getName()));
									} else {
										transReport.setUniqueId("NA");
									}
								} else {
									transReport.setUniqueId("NA");
								}
							} else {
								transReport.setGlocalFlag(false);
							}

							if (StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_CHARGES.toString()))
									&& StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_GST.toString()))) {
								if (StringUtils.isNotBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
										&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.toString()))
										&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
										&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.toString()))
										&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.toString()))) {
									transReport.setTotalAmtPayable(String.format("%.2f", (Double
											.parseDouble(dbobj.getString(FieldType.PG_TDR_SC.toString()))
											+ Double.parseDouble(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
											+ Double.parseDouble(dbobj.getString(FieldType.PG_GST.toString()))
											+ Double.parseDouble(dbobj.getString(FieldType.ACQUIRER_GST.toString()))
											+ Double.parseDouble(dbobj.getString(FieldType.RESELLER_CHARGES.toString()))
											+ Double.parseDouble(dbobj.getString(FieldType.RESELLER_GST.toString())))));

									if (transReport.getTotalAmtPayable().equalsIgnoreCase("0.00")) {
										transReport.setTotalAmtPayable(transReport.getTotalAmtPayable());
									} else {
										transReport.setTotalAmtPayable("-" + transReport.getTotalAmtPayable());
									}
								} else {
									transReport.setTotalAmtPayable("NA");
								}

							} else {

								if (StringUtils.isNotBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
										&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.toString()))
										&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
										&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.toString()))
										&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.toString()))) {
									transReport.setTotalAmtPayable(String.format("%.2f", (Double
											.parseDouble(dbobj.getString(FieldType.PG_TDR_SC.toString()))
											+ Double.parseDouble(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
											+ Double.parseDouble(dbobj.getString(FieldType.PG_GST.toString()))
											+ Double.parseDouble(dbobj.getString(FieldType.ACQUIRER_GST.toString())))));
									if (transReport.getTotalAmtPayable().equalsIgnoreCase("0.00")) {
										transReport.setTotalAmtPayable(transReport.getTotalAmtPayable());
									} else {
										transReport.setTotalAmtPayable("-" + transReport.getTotalAmtPayable());
									}

								} else {
									transReport.setTotalAmtPayable("NA");
								}
							}

						} else if (isGlocal == true) {

							transReport.setGlocalFlag(true);

							if (dbobj.containsKey(FieldType.UDF7.getName())) {

								if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF7.getName()))) {

									if (StringUtils.isNotBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
											&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.toString()))
											&& StringUtils
													.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
											&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.toString()))
											&& StringUtils
													.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.toString()))) {

										String merchantAmount = String.format("%.2f", Double
												.parseDouble(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
												- (Double.parseDouble(dbobj.getString(FieldType.PG_TDR_SC.toString()))
														+ Double.parseDouble(
																dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
														+ Double.parseDouble(
																dbobj.getString(FieldType.PG_GST.toString()))
														+ Double.parseDouble(
																dbobj.getString(FieldType.ACQUIRER_GST.toString()))));

										transReport.setDoctor(
												String.valueOf(new BigDecimal(dbobj.getString(FieldType.UDF7.getName()))
														.multiply(new BigDecimal(merchantAmount))
														.divide(new BigDecimal(Constants.MAX_NUMBER_OF_KEYS.getValue()))
														.setScale(2, BigDecimal.ROUND_HALF_UP)));
									} else {
										transReport.setDoctor("0");
									}
								} else {
									transReport.setDoctor("0");
								}
							} else {
								transReport.setDoctor("0");
							}

							if (dbobj.containsKey(FieldType.UDF8.getName())) {
								if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF8.getName()))) {

									if (StringUtils.isNotBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
											&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.toString()))
											&& StringUtils
													.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
											&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.toString()))
											&& StringUtils
													.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.toString()))) {

										String merchantAmount = String.format("%.2f", Double
												.parseDouble(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
												- (Double.parseDouble(dbobj.getString(FieldType.PG_TDR_SC.toString()))
														+ Double.parseDouble(
																dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
														+ Double.parseDouble(
																dbobj.getString(FieldType.PG_GST.toString()))
														+ Double.parseDouble(
																dbobj.getString(FieldType.ACQUIRER_GST.toString()))));

										transReport.setGlocal(
												String.valueOf(new BigDecimal(dbobj.getString(FieldType.UDF8.getName()))
														.multiply(new BigDecimal(merchantAmount))
														.divide(new BigDecimal(Constants.MAX_NUMBER_OF_KEYS.getValue()))
														.setScale(2, BigDecimal.ROUND_HALF_UP)));
									} else {
										transReport.setGlocal("0");
									}
								} else {
									transReport.setGlocal("0");
								}
							} else {
								transReport.setGlocal("0");
							}

							if (dbobj.containsKey(FieldType.UDF9.getName())) {
								if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF9.getName()))) {

									if (StringUtils.isNotBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
											&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.toString()))
											&& StringUtils
													.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
											&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.toString()))
											&& StringUtils
													.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.toString()))) {

										String merchantAmount = String.format("%.2f", Double
												.parseDouble(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
												- (Double.parseDouble(dbobj.getString(FieldType.PG_TDR_SC.toString()))
														+ Double.parseDouble(
																dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
														+ Double.parseDouble(
																dbobj.getString(FieldType.PG_GST.toString()))
														+ Double.parseDouble(
																dbobj.getString(FieldType.ACQUIRER_GST.toString()))));

										transReport.setPartner(
												String.valueOf(new BigDecimal(dbobj.getString(FieldType.UDF9.getName()))
														.multiply(new BigDecimal(merchantAmount))
														.divide(new BigDecimal(Constants.MAX_NUMBER_OF_KEYS.getValue()))
														.setScale(2, BigDecimal.ROUND_HALF_UP)));
									} else {
										transReport.setPartner("0");
									}
								} else {
									transReport.setPartner("0");
								}
							} else {
								transReport.setPartner("0");
							}

							if (dbobj.containsKey(FieldType.UDF11.getName())) {
								if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF11.getName()))) {
									transReport.setUniqueId(dbobj.getString(FieldType.UDF11.getName()));
								} else {
									transReport.setUniqueId("NA");
								}
							} else {
								transReport.setUniqueId("NA");
							}
						} else {
							transReport.setGlocalFlag(false);
						}

						if (!dbobj.getString(FieldType.PAYMENT_TYPE.getName())
								.equalsIgnoreCase(PaymentType.COD.getCode())) {

							if (StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_CHARGES.toString()))
									&& StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_GST.toString()))) {

								if (StringUtils.isNotBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
										&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
										&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.toString()))) {
									transReport.setTotalAmtPayable(String.format("%.2f", Double
											.parseDouble(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
											- (Double.parseDouble(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
													+ Double.parseDouble(
															dbobj.getString(FieldType.ACQUIRER_GST.toString()))
													+ Double.parseDouble(
															dbobj.getString(FieldType.PG_TDR_SC.toString()))
													+ Double.parseDouble(dbobj.getString(FieldType.PG_GST.toString()))
													+ Double.parseDouble(
															dbobj.getString(FieldType.RESELLER_CHARGES.toString()))
													+ Double.parseDouble(
															dbobj.getString(FieldType.RESELLER_GST.toString())))));
								} else {
									if (StringUtils.isNotBlank(dbobj.getString(FieldType.PAYMENT_TYPE.toString()))
											&& (dbobj.getString(FieldType.PAYMENT_TYPE.toString())
													.equalsIgnoreCase("RTGS")
													|| dbobj.getString(FieldType.PAYMENT_TYPE.toString())
															.equalsIgnoreCase("NEFT")
													|| dbobj.getString(FieldType.PAYMENT_TYPE.toString())
															.equalsIgnoreCase("IMPS"))) {

										transReport
												.setTotalAmtPayable(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()));
									} else {
										transReport.setTotalAmtPayable("NA");
									}
								}

							} else {

								if (StringUtils.isNotBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
										&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
										&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.toString()))) {
									transReport.setTotalAmtPayable(String.format("%.2f", Double
											.parseDouble(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
											- (Double.parseDouble(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
													+ Double.parseDouble(
															dbobj.getString(FieldType.ACQUIRER_GST.toString()))
													+ Double.parseDouble(
															dbobj.getString(FieldType.PG_TDR_SC.toString()))
													+ Double.parseDouble(
															dbobj.getString(FieldType.PG_GST.toString())))));
								} else {
									if (StringUtils.isNotBlank(dbobj.getString(FieldType.PAYMENT_TYPE.toString()))
											&& (dbobj.getString(FieldType.PAYMENT_TYPE.toString())
													.equalsIgnoreCase("RTGS")
													|| dbobj.getString(FieldType.PAYMENT_TYPE.toString())
															.equalsIgnoreCase("NEFT")
													|| dbobj.getString(FieldType.PAYMENT_TYPE.toString())
															.equalsIgnoreCase("IMPS"))) {

										transReport
												.setTotalAmtPayable(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()));
									} else {
										transReport.setTotalAmtPayable("NA");
									}
								}
							}
						}

					} else {
						transReport.setTotalAmtPayable("NA");
					}

				} else if (TxnType.REFUND.getName()
						.equalsIgnoreCase(dbobj.getString(FieldType.ORIG_TXNTYPE.getName()))) {
					if (isGlocal == true) {
						transReport.setGlocalFlag(true);
					} else {
						transReport.setGlocalFlag(false);
					}
					if (StringUtils.isNotBlank(dbobj.getString(FieldType.PAYMENT_TYPE.getName()))) {

						if (dbobj.getString(FieldType.PAYMENT_TYPE.getName())
								.equalsIgnoreCase(PaymentType.COD.getCode())) {
							transReport.setGst_charge("0.00");
							transReport.setTdr_Surcharge("0.00");
							transReport.setTotalAmtPayable("0.00");
						} else {
							if (StringUtils.isNotBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))) {
								transReport.setTotalAmtPayable(String.format("%.2f",
										Double.parseDouble(dbobj.getString(FieldType.TOTAL_AMOUNT.toString())) * -1));
							} else {
								transReport.setTotalAmtPayable("NA");
							}
						}

					} else {
						transReport.setTotalAmtPayable("NA");
					}

				} else {
					transReport.setTotalAmtPayable("NA");
				}

				// Exclude certain records when loading search payment without
				// any filters

//				Comparator<TransactionSearch> comp = (TransactionSearch a, TransactionSearch b) -> {
//
//					if (a.getDateFrom().compareTo(b.getDateFrom()) > 0) {
//						return -1;
//					} else if (a.getDateFrom().compareTo(b.getDateFrom()) < 0) {
//						return 1;
//					} else {
//						return 0;
//					}
//				};
				transactionList.add(transReport);
//				Collections.sort(transactionList, comp);
			}

			String capturedDate = "";

			for (TransactionSearch searchTransaction : transactionList) {
				if (searchTransaction.getStatus().equalsIgnoreCase(StatusType.CAPTURED.getName())) {
					capturedDate = searchTransaction.getDateFrom();
				}
			}

			if (StringUtils.isNotBlank(capturedDate) && StringUtils.isNotBlank(orderId)) {

				userSettings = userSettingDao.fetchDataUsingPayId(user.getPayId());

				if (!merchantPayId.equalsIgnoreCase("ALL")) {
					String identifierKey = propertiesManager.propertiesMap.get(Constants.MERCHANT_PAYID.getValue());
					if (StringUtils.isNotBlank(identifierKey) && identifierKey.contains(merchantPayId)) {
						isGlocal = true;
					}
				}
				transactionList = new ArrayList<TransactionSearch>();
				statusConditionList = new ArrayList<String>();
				paramConditionLst = new ArrayList<BasicDBObject>();
				statusQuery = new BasicDBObject();
				customerQuery = new BasicDBObject();
				customerQueryMask = new BasicDBObject();
				BasicDBObject dateQuery = new BasicDBObject();
				DateFormat format1 = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
				Date date = format.parse(capturedDate);
				String dateFrom = format1.format(date) + " 00:00:00";
				String dateTo = capturedDate;
				dateQuery.put(FieldType.CREATE_DATE.getName(),
						BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(dateFrom).toLocalizedPattern())
								.add("$lte", new SimpleDateFormat(dateTo).toLocalizedPattern()).get());
				paramConditionLst.add(new BasicDBObject(dateQuery));
				BasicDBObject dateIndexConditionQuery1 = new BasicDBObject();
				String startString = new SimpleDateFormat(dateFrom).toLocalizedPattern();
				String endString = new SimpleDateFormat(dateTo).toLocalizedPattern();

				Date dateStart1 = format.parse(startString);
				Date dateEnd1 = format.parse(endString);

				LocalDate incrementingDate = dateStart1.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
				LocalDate endDate1 = dateEnd1.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

				List<String> allDatesIndex = new ArrayList<>();

				while (!incrementingDate.isAfter(endDate1)) {
					allDatesIndex.add(incrementingDate.toString().replaceAll("-", ""));
					incrementingDate = incrementingDate.plusDays(1);
				}
				BasicDBObject dateIndexIn = new BasicDBObject("$in", allDatesIndex);
				dateIndexConditionQuery1.append("DATE_INDEX", dateIndexIn);

				if (!merchantPayId.equalsIgnoreCase("ALL")) {
					paramConditionLst.add(new BasicDBObject(FieldType.PAY_ID.getName(), merchantPayId));
				}

				if (StringUtils.isNotBlank(subMerchantPayId) && !subMerchantPayId.equalsIgnoreCase("ALL")) {
					paramConditionLst.add(new BasicDBObject(FieldType.SUB_MERCHANT_ID.getName(), subMerchantPayId));
				}
				if (StringUtils.isNotEmpty(paymentType) && !paymentType.equalsIgnoreCase("ALL")) {
					paramConditionLst.add(new BasicDBObject(FieldType.PAYMENT_TYPE.getName(), paymentType));
				} else {

				}
				if (user.getUserType().equals(UserType.RESELLER)) {
					paramConditionLst.add(new BasicDBObject(FieldType.RESELLER_ID.getName(), user.getResellerId()));
				}

				if (!pgRefNum.isEmpty()) {
					paramConditionLst.add(new BasicDBObject(FieldType.PG_REF_NUM.getName(), pgRefNum));
				}

				BasicDBObject orderIdConditionQuery1 = null;
				if (orderIdSet != null) {
					List<String> orderIdList = new ArrayList<>(orderIdSet);
					orderIdConditionQuery1 = new BasicDBObject("$in", orderIdList);
				}
				if (!orderId.isEmpty()) {
					paramConditionLst.add(new BasicDBObject(FieldType.ORDER_ID.getName(), orderId));
				} else if (null != orderIdConditionQuery) {
					paramConditionLst.add(new BasicDBObject(FieldType.ORDER_ID.getName(), orderIdConditionQuery1));
				}

				if (StringUtils.isNotEmpty(customerEmail) && !customerEmail.isEmpty()) {
					customerQuery.append(FieldType.CUST_EMAIL.getName(), customerEmail);
					if (StringUtils.isNotBlank(customerEmail)) {
						String email = customerEmail;
						String emailMask = null;
						final String mask = "*****";
						final int at = email.indexOf("@");
						if (at > 2) {
							final int maskLen = Math.min(Math.max(at / 2, 2), 5);
							final int start1 = (at - maskLen) / 2;
							emailMask = email.substring(0, start1) + mask.substring(0, maskLen)
									+ email.substring(start1 + maskLen);
						}
						customerQueryMask.append(FieldType.CUST_EMAIL.getName(), emailMask);
					}
				}
				if (StringUtils.isNotBlank(Userstatus) && !Userstatus.equalsIgnoreCase("ALL")) {
					String statusArr[] = Userstatus.split(",");
					for (String sts : statusArr) {
						statusConditionList.add(sts.trim());
					}
					statusQuery.append("$in", statusConditionList);
					paramConditionLst.add(new BasicDBObject(FieldType.STATUS.getName(), statusQuery));
				}

				if (!currency.equalsIgnoreCase("ALL")) {
					paramConditionLst.add(new BasicDBObject(FieldType.CURRENCY_CODE.getName(), currency));
				}
				if (!partSettleFlag.equalsIgnoreCase("ALL")) {
					paramConditionLst.add(new BasicDBObject(FieldType.PART_SETTLE.getName(), partSettleFlag));
				}

				if (!transactionType.equalsIgnoreCase("ALL")) {
					paramConditionLst.add(new BasicDBObject(FieldType.ORIG_TXNTYPE.getName(), transactionType));
				}

				if (!paymentRegion.equalsIgnoreCase("ALL")) {
					paramConditionLst.add(new BasicDBObject(FieldType.PAYMENTS_REGION.getName(), paymentRegion));
				}

				if (StringUtils.isNotBlank(transactionFlag) && !transactionFlag.equalsIgnoreCase("ALL")) {
					paramConditionLst.add(new BasicDBObject(FieldType.TXN_CAPTURE_FLAG.getName(), transactionFlag));
				}

				if (StringUtils.isNotBlank(deltaFlag) && !deltaFlag.equalsIgnoreCase("ALL")) {
					paramConditionLst.add(new BasicDBObject(FieldType.DELTA_REFUND_FLAG.getName(), deltaFlag));
				}

				if (StringUtils.isNotBlank(pgRefNum) || StringUtils.isNotBlank(orderId)) {

				} else {

					if (!dateIndexConditionQuery1.isEmpty()) {
						paramConditionLst.add(dateIndexConditionQuery1);
					}
				}

				List<BasicDBObject> fianlQueryList1 = new ArrayList<BasicDBObject>();

				fianlQueryList1.addAll(paramConditionLst);

				if (!customerQuery.isEmpty()) {
					paramConditionLst.add(customerQuery);
				}

				if (!dateIndexConditionQuery1.isEmpty()) {
					paramConditionLst.add(dateIndexConditionQuery1);
				}

				if (!customerQueryMask.isEmpty()) {
					fianlQueryList.add(customerQueryMask);
				}

				BasicDBObject finalquery1 = new BasicDBObject("$and", paramConditionLst);
				BasicDBObject finalqueryMask1 = new BasicDBObject("$and", fianlQueryList1);

				if (!customerQueryMask.isEmpty()) {
					logger.info("Inside TxnReports , searchPayment , finalQuery = " + finalqueryMask1);
				} else {
					logger.info("Inside TxnReports , searchPayment , finalQuery = " + finalquery1);
				}

				MongoDatabase dbIns1 = mongoInstance.getDB();

				List<BasicDBObject> pipeline1 = null;
				BasicDBObject skip1 = null;
				BasicDBObject limit1 = null;

				BasicDBObject projectElement1 = new BasicDBObject();
				projectElement1.put(FieldType.TXN_ID.getName(), 1);
				projectElement1.put(FieldType.PG_REF_NUM.getName(), 1);
				projectElement1.put(FieldType.PAY_ID.getName(), 1);
				projectElement1.put(FieldType.CUST_NAME.getName(), 1);
				projectElement1.put(FieldType.CUST_PHONE.getName(), 1);
				projectElement1.put(FieldType.CUST_EMAIL.getName(), 1);
				projectElement1.put(FieldType.PAYOUT_DATE.getName(), 1);
				projectElement1.put(FieldType.UTR_NO.getName(), 1);
				projectElement1.put(CrmFieldType.BUSINESS_NAME.getName(), 1);
				projectElement1.put(FieldType.SUB_MERCHANT_ID.getName(), 1);
				projectElement1.put(FieldType.REFUND_DAYS.getName(), 1);
				projectElement1.put(FieldType.PRODUCT_PRICE.getName(), 1);
				projectElement1.put(FieldType.VENDOR_ID.getName(), 1);
				projectElement1.put(FieldType.SKU_CODE.getName(), 1);
				projectElement1.put(FieldType.CATEGORY_CODE.getName(), 1);
				projectElement1.put(FieldType.TXNTYPE.getName(), 1);
				projectElement1.put(FieldType.ORIG_TXNTYPE.getName(), 1);
				projectElement1.put(FieldType.STATUS.getName(), 1);
				projectElement1.put(FieldType.MOP_TYPE.getName(), 1);
				projectElement1.put(FieldType.PAYMENT_TYPE.getName(), 1);
				projectElement1.put(FieldType.ACQUIRER_TYPE.getName(), 1);
				projectElement1.put(FieldType.ACQ_ID.getName(), 1);
				projectElement1.put(FieldType.UDF11.getName(), 1);
				projectElement1.put(FieldType.UDF12.getName(), 1);
				projectElement1.put(FieldType.UDF13.getName(), 1);
				projectElement1.put(FieldType.UDF14.getName(), 1);
				projectElement1.put(FieldType.UDF15.getName(), 1);
				projectElement1.put(FieldType.UDF16.getName(), 1);
				projectElement1.put(FieldType.UDF17.getName(), 1);
				projectElement1.put(FieldType.UDF18.getName(), 1);
				projectElement1.put(FieldType.RRN.getName(), 1);
				projectElement1.put(FieldType.TXN_CAPTURE_FLAG.getName(), 1);
				projectElement1.put(FieldType.TRANSACTION_MODE.getName(), 1);
				projectElement1.put(FieldType.RESPONSE_MESSAGE.getName(), 1);
				projectElement1.put(FieldType.PG_TXN_MESSAGE.getName(), 1);
				projectElement1.put(FieldType.CARD_MASK.getName(), 1);
				projectElement1.put(FieldType.PAYER_ADDRESS.getName(), 1);
				projectElement1.put(FieldType.PART_SETTLE.getName(), 1);
				projectElement1.put(FieldType.CREATE_DATE.getName(), 1);
				projectElement1.put(FieldType.AMOUNT.getName(), 1);
				projectElement1.put(FieldType.ORDER_ID.getName(), 1);
				projectElement1.put(FieldType.REFUND_ORDER_ID.getName(), 1);
				projectElement1.put(FieldType.PRODUCT_DESC.getName(), 1);
				projectElement1.put(FieldType.PG_DATE_TIME.getName(), 1);
				projectElement1.put(FieldType.INTERNAL_CARD_ISSUER_BANK.getName(), 1);
				projectElement1.put(FieldType.INTERNAL_CARD_ISSUER_COUNTRY.getName(), 1);
				projectElement1.put(FieldType.REFUNDABLE_AMOUNT.getName(), 1);
				projectElement1.put(FieldType.TOTAL_AMOUNT.getName(), 1);
				projectElement1.put(FieldType.CURRENCY_CODE.getName(), 1);
				projectElement1.put(FieldType.PAYMENTS_REGION.getName(), 1);
				projectElement1.put(FieldType.CARD_HOLDER_TYPE.getName(), 1);
				projectElement1.put(FieldType.RESELLER_CHARGES.getName(), 1);
				projectElement1.put(FieldType.RESELLER_GST.getName(), 1);
				projectElement1.put(FieldType.ACQUIRER_TDR_SC.getName(), 1);
				projectElement1.put(FieldType.PG_TDR_SC.getName(), 1);
				projectElement1.put(FieldType.ACQUIRER_GST.getName(), 1);
				projectElement1.put(FieldType.PG_GST.getName(), 1);
				projectElement1.put(FieldType.SETTLEMENT_FLAG.getName(), 1);
				projectElement1.put(FieldType.SETTLEMENT_DATE.getName(), 1);
				projectElement1.put(FieldType.IS_ENCRYPTED.getName(), 1);

				BasicDBObject project1 = new BasicDBObject("$project", projectElement1);
				MongoCollection<Document> coll1 = null;

				if (searchFlag == true) {
					coll1 = dbIns1.getCollection(propertiesManager.propertiesMap
							.get(prefix + Constants.TRANSACTION_STATUS_COLLECTION.getValue()));
				} else {
					coll1 = dbIns1.getCollection(
							propertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));
				}

				BasicDBObject match1 = new BasicDBObject("$match", finalquery1);
				BasicDBObject sort1 = new BasicDBObject("$sort", new BasicDBObject("CREATE_DATE", -1));

				skip1 = new BasicDBObject("$skip", start);
				limit1 = new BasicDBObject("$limit", length);
				pipeline1 = Arrays.asList(match1, project1, sort1, skip1, limit1);
				AggregateIterable<Document> output1 = coll1.aggregate(pipeline1);
				output1.allowDiskUse(true);
				MongoCursor<Document> cursor1 = output1.iterator();

				while (cursor1.hasNext()) {
					Document dbobj = cursor1.next();

					if (StringUtils.isNotBlank(dbobj.getString("IS_ENCRYPTED"))
							&& dbobj.getString("IS_ENCRYPTED").equalsIgnoreCase("Y")) {
						dbobj = dataEncDecTool.decryptDocument(dbobj);
					}

					TransactionSearch transReport = new TransactionSearch();
					BigInteger txnID = new BigInteger(((Document) dbobj).getString(FieldType.TXN_ID.toString()));
					transReport.setTransactionId((txnID));
					transReport.setTxnId(((Document) dbobj).getString(FieldType.TXN_ID.toString()));
					transReport.setPgRefNum(((Document) dbobj).getString(FieldType.PG_REF_NUM.toString()));
					transReport.setPayId(((Document) dbobj).getString(FieldType.PAY_ID.toString()));

					if (null != ((Document) dbobj).getString(FieldType.CUST_NAME.toString())) {
						transReport.setCustomerName(((Document) dbobj).getString(FieldType.CUST_NAME.toString()));
					} else {
						transReport.setCustomerName(CrmFieldConstants.NA.getValue());
					}

					if (null != ((Document) dbobj).getString(FieldType.CUST_PHONE.toString())) {
						transReport.setCustomerMobile(((Document) dbobj).getString(FieldType.CUST_PHONE.toString()));
					} else {
						transReport.setCustomerMobile(CrmFieldConstants.NA.getValue());
					}

					if (null != ((Document) dbobj).getString(FieldType.CUST_EMAIL.toString())) {
						transReport.setCustomerEmail(((Document) dbobj).getString(FieldType.CUST_EMAIL.toString()));
					} else {
						transReport.setCustomerEmail(CrmFieldConstants.NA.getValue());
					}

					if (null != ((Document) dbobj).getString(FieldType.PAYOUT_DATE.toString())) {
						transReport.setPayOutDate(((Document) dbobj).getString(FieldType.PAYOUT_DATE.toString()));
					} else {
						transReport.setPayOutDate(CrmFieldConstants.NA.getValue());
					}
					if (null != ((Document) dbobj).getString(FieldType.UTR_NO.toString())) {
						transReport.setUtrNo(((Document) dbobj).getString(FieldType.UTR_NO.toString()));
					} else {
						transReport.setUtrNo(CrmFieldConstants.NA.getValue());
					}

					if (StringUtils.isNotBlank(dbobj.getString("IS_CUSTOM_HOSTED"))
							&& dbobj.getString("IS_CUSTOM_HOSTED").equalsIgnoreCase("Y")) {
						transReport.setCustomFlag(((Document) dbobj).getString("IS_CUSTOM_HOSTED"));
					} else {
						transReport.setCustomFlag("N");
					}

					User user1 = new User();

					if (StringUtils.isNotBlank((String) dbobj.get(FieldType.PAY_ID.getName()))) {
						String payid = (String) dbobj.get(FieldType.PAY_ID.getName());

						if (userMap.get(payid) != null && !userMap.get(payid).getPayId().isEmpty()) {
							user1 = userMap.get(payid);
						} else {
							user1 = userdao.findPayId(payid);
							userMap.put(payid, user1);
						}
					}
					if (user1 != null) {
						transReport.setMerchants(user1.getBusinessName());
					}

					if (user.getUserType().equals(UserType.PARENTMERCHANT)) {
						transReport.setMerchants(user.getBusinessName());
					}

					// if (!merchantPayId.equalsIgnoreCase("All") &&
					if (dbobj.containsKey(FieldType.SUB_MERCHANT_ID.getName())
							&& StringUtils.isNotBlank(dbobj.getString(FieldType.SUB_MERCHANT_ID.getName()))) {

						String subMerchant = dbobj.getString(FieldType.SUB_MERCHANT_ID.getName());
						User subMerchantUser = new User();

						if (userMap.get(subMerchant) != null && !userMap.get(subMerchant).getPayId().isEmpty()) {
							subMerchantUser = userMap.get(subMerchant);
						} else {
							subMerchantUser = userdao.findPayId(subMerchant);
							userMap.put(subMerchant, subMerchantUser);
						}
						if (subMerchantUser != null) {
							transReport.setSubMerchantId(subMerchantUser.getBusinessName());
						} else {
							transReport.setSubMerchantId(CrmFieldConstants.NA.getValue());
						}
					} else {
						if ((!pgRefNum.isEmpty() || !orderId.isEmpty())
								&& dbobj.containsKey(FieldType.SUB_MERCHANT_ID.getName())
								&& StringUtils.isNotBlank(dbobj.getString(FieldType.SUB_MERCHANT_ID.getName()))) {
							transReport.setSubMerchantId(userdao
									.getBusinessNameByPayId(dbobj.getString(FieldType.SUB_MERCHANT_ID.getName())));
						}
					}

					if (merchantPayId.equalsIgnoreCase("All")) {

						if (user.getUserType().equals(UserType.RESELLER)) {

						} else {

							if (StringUtils.isNotBlank(dbobj.getString(FieldType.REFUND_DAYS.getName()))) {
								transReport.setRefundCycle(dbobj.getString(FieldType.REFUND_DAYS.getName()));
							} else {
								transReport.setRefundCycle(CrmFieldConstants.NA.getValue());
							}

							if (StringUtils.isNotBlank(dbobj.getString(FieldType.PRODUCT_PRICE.getName()))) {
								transReport.setProductPrice(dbobj.getString(FieldType.PRODUCT_PRICE.getName()));
							} else {
								transReport.setProductPrice(CrmFieldConstants.NA.getValue());
							}
							if (StringUtils.isNotBlank(dbobj.getString(FieldType.VENDOR_ID.getName()))) {
								transReport.setVendorID(dbobj.getString(FieldType.VENDOR_ID.getName()));
							} else {
								transReport.setVendorID(CrmFieldConstants.NA.getValue());
							}
							if (StringUtils.isNotBlank(dbobj.getString(FieldType.SKU_CODE.getName()))) {
								transReport.setSKUCode(dbobj.getString(FieldType.SKU_CODE.getName()));
							} else {
								transReport.setSKUCode(CrmFieldConstants.NA.getValue());
							}

							if (StringUtils.isNotBlank(dbobj.getString(FieldType.CATEGORY_CODE.getName()))) {
								transReport.setCategoryCode(dbobj.getString(FieldType.CATEGORY_CODE.getName()));
							} else {
								transReport.setCategoryCode(CrmFieldConstants.NA.getValue());
							}
						}
					} else {

						if (user.getUserType().equals(UserType.ADMIN) || user.getUserType().equals(UserType.SUBADMIN)
								|| user.getUserType().equals(UserType.RESELLER)) {
							// User merhant = userMap.get(merchantPayId); // userdao.findPayId(payid);
							UserSettingData merchantSettings = userSettingDao.fetchDataUsingPayId(merchantPayId);

							if (merchantSettings.isRetailMerchantFlag()) {
								if (StringUtils.isNotBlank(dbobj.getString(FieldType.REFUND_DAYS.getName()))) {
									transReport.setRefundCycle(dbobj.getString(FieldType.REFUND_DAYS.getName()));
								} else {
									transReport.setRefundCycle(CrmFieldConstants.NA.getValue());
								}

								if (StringUtils.isNotBlank(dbobj.getString(FieldType.PRODUCT_PRICE.getName()))) {
									transReport.setProductPrice(dbobj.getString(FieldType.PRODUCT_PRICE.getName()));
								} else {
									transReport.setProductPrice(CrmFieldConstants.NA.getValue());
								}
								if (StringUtils.isNotBlank(dbobj.getString(FieldType.VENDOR_ID.getName()))) {
									transReport.setVendorID(dbobj.getString(FieldType.VENDOR_ID.getName()));
								} else {
									transReport.setVendorID(CrmFieldConstants.NA.getValue());
								}
								if (StringUtils.isNotBlank(dbobj.getString(FieldType.SKU_CODE.getName()))) {
									transReport.setSKUCode(dbobj.getString(FieldType.SKU_CODE.getName()));
								} else {
									transReport.setSKUCode(CrmFieldConstants.NA.getValue());
								}

								if (StringUtils.isNotBlank(dbobj.getString(FieldType.CATEGORY_CODE.getName()))) {
									transReport.setCategoryCode(dbobj.getString(FieldType.CATEGORY_CODE.getName()));
								} else {
									transReport.setCategoryCode(CrmFieldConstants.NA.getValue());
								}
							}
						} else {

							if (userSettings.isRetailMerchantFlag()) {
								if (StringUtils.isNotBlank(dbobj.getString(FieldType.REFUND_DAYS.getName()))) {
									transReport.setRefundCycle(dbobj.getString(FieldType.REFUND_DAYS.getName()));
								} else {
									transReport.setRefundCycle(CrmFieldConstants.NA.getValue());
								}

								if (StringUtils.isNotBlank(dbobj.getString(FieldType.PRODUCT_PRICE.getName()))) {
									transReport.setProductPrice(dbobj.getString(FieldType.PRODUCT_PRICE.getName()));
								} else {
									transReport.setProductPrice(CrmFieldConstants.NA.getValue());
								}
								if (StringUtils.isNotBlank(dbobj.getString(FieldType.VENDOR_ID.getName()))) {
									transReport.setVendorID(dbobj.getString(FieldType.VENDOR_ID.getName()));
								} else {
									transReport.setVendorID(CrmFieldConstants.NA.getValue());
								}
								if (StringUtils.isNotBlank(dbobj.getString(FieldType.SKU_CODE.getName()))) {
									transReport.setSKUCode(dbobj.getString(FieldType.SKU_CODE.getName()));
								} else {
									transReport.setSKUCode(CrmFieldConstants.NA.getValue());
								}

								if (StringUtils.isNotBlank(dbobj.getString(FieldType.CATEGORY_CODE.getName()))) {
									transReport.setCategoryCode(dbobj.getString(FieldType.CATEGORY_CODE.getName()));
								} else {
									transReport.setCategoryCode(CrmFieldConstants.NA.getValue());
								}
							}

						}

					}

					if (StringUtils.isNotBlank(deliveryStatus)) {
						transReport.setDeliveryStatus(deliveryStatus);
					} else {
						transReport.setDeliveryStatus("");
					}

					if ((StringUtils.isNotBlank(dbobj.getString(FieldType.TXNTYPE.getName())))
							&& (dbobj.getString(FieldType.TXNTYPE.toString())
									.equalsIgnoreCase(TransactionType.REFUND.getName()))) {
						if (StringUtils.isNotBlank(dbobj.getString(FieldType.SETTLEMENT_FLAG.getName()))
								&& dbobj.getString(FieldType.SETTLEMENT_FLAG.getName()).equals("Y")) {
							transReport.setTxnType(TransactionType.REFUND.getName());
						} else {
							transReport.setTxnType(dbobj.getString(FieldType.TXNTYPE.toString()));
						}
					} else if (StringUtils.isNotBlank(dbobj.getString(FieldType.ORIG_TXNTYPE.toString()))) {

						if (StringUtils.isNotBlank(dbobj.getString(FieldType.SETTLEMENT_FLAG.getName()))
								&& dbobj.getString(FieldType.SETTLEMENT_FLAG.getName()).equals("Y")) {
							transReport.setTxnType(TransactionType.SALE.getName());
						} else {
							transReport.setTxnType(dbobj.getString(FieldType.ORIG_TXNTYPE.toString()));
						}
					} else {

						if (dbobj.getString(FieldType.STATUS.toString())
								.equalsIgnoreCase(StatusType.TIMEOUT.getName())) {
							transReport.setTxnType(dbobj.getString(FieldType.TXNTYPE.toString()));
						} else {
							transReport.setTxnType(CrmFieldConstants.NA.getValue());
						}
					}

					if (StringUtils.isNotBlank(dbobj.getString(FieldType.MOP_TYPE.toString()))) {
						transReport.setMopType(MopType.getmopName(dbobj.getString(FieldType.MOP_TYPE.toString())));
					} else {
						transReport.setMopType(CrmFieldConstants.NOT_AVAILABLE.getValue());
					}
					if (StringUtils.isNotBlank(dbobj.getString(FieldType.PAYMENT_TYPE.toString()))) {
						transReport.setPaymentMethods(
								PaymentType.getpaymentName(dbobj.getString(FieldType.PAYMENT_TYPE.toString())));
					} else {
						transReport.setPaymentMethods(CrmFieldConstants.NOT_AVAILABLE.getValue());
					}

					if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TYPE.getName()))) {
						transReport.setAcquirerType(dbobj.getString(FieldType.ACQUIRER_TYPE.getName()));
					} else {
						transReport.setAcquirerType(CrmFieldConstants.NOT_AVAILABLE.getValue());
					}

					if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQ_ID.getName()))) {
						transReport.setAcqId(dbobj.getString(FieldType.ACQ_ID.getName()));
					} else {
						transReport.setAcqId(CrmFieldConstants.NA.getValue());
					}

					if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF11.getName()))) {
						transReport.setUDF11(dbobj.getString(FieldType.UDF11.getName()));
					} else {
						transReport.setUDF11(CrmFieldConstants.NA.getValue());
					}
					if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF12.getName()))) {
						transReport.setUDF12(dbobj.getString(FieldType.UDF12.getName()));
					} else {
						transReport.setUDF12(CrmFieldConstants.NA.getValue());
					}
					if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF13.getName()))) {
						transReport.setUDF13(dbobj.getString(FieldType.UDF13.getName()));
					} else {
						transReport.setUDF13(CrmFieldConstants.NA.getValue());
					}
					if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF14.getName()))) {
						transReport.setUDF14(dbobj.getString(FieldType.UDF14.getName()));
					} else {
						transReport.setUDF14(CrmFieldConstants.NA.getValue());
					}
					if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF15.getName()))) {
						transReport.setUDF15(dbobj.getString(FieldType.UDF15.getName()));
					} else {
						transReport.setUDF15(CrmFieldConstants.NA.getValue());
					}
					if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF16.getName()))) {
						transReport.setUDF16(dbobj.getString(FieldType.UDF16.getName()));
					} else {
						transReport.setUDF16(CrmFieldConstants.NA.getValue());
					}
					if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF17.getName()))) {
						transReport.setUDF17(dbobj.getString(FieldType.UDF17.getName()));
					} else {
						transReport.setUDF17(CrmFieldConstants.NA.getValue());
					}
					if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF18.getName()))) {
						transReport.setUDF18(dbobj.getString(FieldType.UDF18.getName()));
					} else {
						transReport.setUDF18(CrmFieldConstants.NA.getValue());
					}

					if (StringUtils.isNotBlank(dbobj.getString(FieldType.RRN.getName()))
							&& !dbobj.getString(FieldType.RRN.getName()).equalsIgnoreCase("-")) {
						transReport.setRrn(dbobj.getString(FieldType.RRN.getName()));
					} else {
						transReport.setRrn(CrmFieldConstants.NA.getValue());
					}

					if (StringUtils.isNotBlank(dbobj.getString(FieldType.TXN_CAPTURE_FLAG.getName()))) {
						transReport.setTxnSettledType(dbobj.getString(FieldType.TXN_CAPTURE_FLAG.getName()));
					} else {
						transReport.setTxnSettledType(CrmFieldConstants.NA.getValue());
					}

					if (StringUtils.isNotBlank(dbobj.getString(FieldType.TRANSACTION_MODE.getName()))) {
						transReport.setTransactionMode(dbobj.getString(FieldType.TRANSACTION_MODE.getName()));
					} else {
					}

					transReport.setResponseMessage(dbobj.getString(CrmFieldType.RESPONSE_MESSAGE.toString()));
					if (!StringUtils.isEmpty(dbobj.getString(CrmFieldType.PG_TXN_MESSAGE.toString()))) {
						transReport.setAccqResponseMessage(dbobj.getString(CrmFieldType.PG_TXN_MESSAGE.toString()));
					} else {
						transReport.setAccqResponseMessage(CrmFieldConstants.NA.getValue());
					}

					if (StringUtils.isNotBlank(dbobj.getString(FieldType.PAYMENT_TYPE.toString()))) {
						if (null != dbobj.getString(FieldType.CARD_MASK.toString())) {
							transReport.setCardNumber(dbobj.getString(FieldType.CARD_MASK.toString()));
						} else if (null != dbobj.getString(FieldType.PAYER_ADDRESS.getName())) {

							if (user.getUserType().equals(UserType.ADMIN)
									|| user.getUserType().equals(UserType.SUBADMIN)) {
								transReport.setCardNumber(dbobj.getString(FieldType.PAYER_ADDRESS.getName()));
							} else {
								String vpaString = dbobj.getString(FieldType.PAYER_ADDRESS.getName());
								String[] vpaArray = vpaString.split("@");
								char[] vpaChar = vpaArray[0].toCharArray();
								StringBuilder vpastrBuilder = new StringBuilder();

								if (vpaChar.length > 3) {
									for (int i = 0; i < vpaChar.length - 3; i++) {
										vpastrBuilder.append(vpaChar[i]);
									}
									vpastrBuilder.append("***@");
									vpastrBuilder.append(vpaArray[1]);
								} else {
									vpastrBuilder.append(vpaChar[0]);
									vpastrBuilder.append("**@");
									vpastrBuilder.append(vpaArray[1]);
								}

								transReport.setCardNumber(vpastrBuilder.toString());
							}
						} else {
							transReport.setCardNumber(CrmFieldConstants.NA.getValue());
						}
					} else {
						transReport.setCardNumber(CrmFieldConstants.NA.getValue());
					}
					if (StringUtils.isNotBlank(dbobj.getString(FieldType.PART_SETTLE.toString()))
							&& dbobj.getString(FieldType.PART_SETTLE.toString()).equalsIgnoreCase(("Y"))) {
						transReport.setPartSettle(dbobj.getString(FieldType.PART_SETTLE.toString()));
					} else {
						transReport.setPartSettle(CrmFieldConstants.NA.getValue());
					}

					if (((user.getUserType().equals(UserType.MERCHANT))
							|| (user.getUserType().equals(UserType.SUBUSER)))
							&& userSettings.isCustomTransactionStatus()) {
						if (((dbobj.getString(FieldType.STATUS.toString()))
								.equalsIgnoreCase(StatusType.TIMEOUT.getName()))
								|| ((dbobj.getString(FieldType.STATUS.toString()))
										.equalsIgnoreCase(StatusType.CAPTURED.getName()))
								|| ((dbobj.getString(FieldType.STATUS.toString()))
										.equalsIgnoreCase(StatusType.ENROLLED.getName()))
								|| ((dbobj.getString(FieldType.STATUS.toString()))
										.equalsIgnoreCase(StatusType.SETTLED.getName()))
								|| ((dbobj.getString(FieldType.STATUS.toString()))
										.equalsIgnoreCase(StatusType.SENT_TO_BANK.getName()))
								|| ((dbobj.getString(FieldType.STATUS.toString()))
										.equalsIgnoreCase(StatusType.PENDING.getName()))) {

							transReport.setStatus(dbobj.getString(FieldType.STATUS.toString()));
						} else {
							transReport.setStatus("Failed");
						}
					} else {

						transReport.setStatus(dbobj.getString(FieldType.STATUS.toString()));
					}

					transReport.setDateFrom(dbobj.getString(FieldType.CREATE_DATE.getName()));
					transReport.setAmount(dbobj.getString(FieldType.AMOUNT.toString()));
					transReport.setOrderId(dbobj.getString(FieldType.ORDER_ID.toString()));

					if (StringUtils.isNotBlank(dbobj.getString(FieldType.REFUND_ORDER_ID.toString()))) {
						transReport.setRefundOrderId(dbobj.getString(FieldType.REFUND_ORDER_ID.toString()));
					} else {
						transReport.setRefundOrderId(CrmFieldConstants.NA.getValue());
					}

					transReport.setoId(dbobj.getString(FieldType.OID.toString()));

					if (StringUtils.isNotBlank(dbobj.getString(FieldType.PRODUCT_DESC.toString()))) {
						transReport.setProductDesc(dbobj.getString(FieldType.PRODUCT_DESC.toString()));
					} else {
						transReport.setProductDesc(CrmFieldConstants.NA.getValue());
					}

					if (StringUtils.isNotBlank(dbobj.getString(FieldType.PG_DATE_TIME.getName()))) {
						transReport.setTransactionCaptureDate(dbobj.getString(FieldType.PG_DATE_TIME.toString()));
					} else {
						transReport.setTransactionCaptureDate(CrmFieldConstants.NA.getValue());
					}

					if (StringUtils.isNotBlank(dbobj.getString(FieldType.INTERNAL_CARD_ISSUER_BANK.getName()))) {
						transReport.setInternalCardIssusserBank(
								dbobj.getString(FieldType.INTERNAL_CARD_ISSUER_BANK.toString()));
					} else {
						transReport.setInternalCardIssusserBank(CrmFieldConstants.NA.getValue());
					}

					if (StringUtils.isNotBlank(dbobj.getString(FieldType.INTERNAL_CARD_ISSUER_COUNTRY.getName()))) {
						transReport.setInternalCardIssusserCountry(
								dbobj.getString(FieldType.INTERNAL_CARD_ISSUER_COUNTRY.toString()));
					} else {
						transReport.setInternalCardIssusserCountry(CrmFieldConstants.NA.getValue());
					}

					if (StringUtils.isNotBlank(dbobj.getString(FieldType.REFUNDABLE_AMOUNT.getName()))) {
						transReport.setRefundableAmount(dbobj.getString(FieldType.REFUNDABLE_AMOUNT.toString()));
					} else {
						transReport.setRefundableAmount(CrmFieldConstants.NA.getValue());
					}

					if (StringUtils.isNotBlank(dbobj.getString(FieldType.AMOUNT.getName()))) {
						transReport.setApprovedAmount(dbobj.getString(FieldType.AMOUNT.toString()));
					} else {
						transReport.setApprovedAmount(CrmFieldConstants.NA.getValue());
					}

					if (StringUtils.isNotBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))) {
						transReport.setTotalAmount(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()));
					} else {
						transReport.setTotalAmount("");
					}

					if (StringUtils.isNotBlank(dbobj.getString(FieldType.CURRENCY_CODE.toString()))) {
						transReport.setCurrency(propManager
								.getAlphabaticCurrencyCode(dbobj.getString(FieldType.CURRENCY_CODE.toString())));
					} else {
						transReport.setCurrency(CrmFieldConstants.NA.getValue());
					}

					if (StringUtils.isNotBlank(dbobj.getString(FieldType.PAYMENTS_REGION.toString()))) {
						transReport.setPaymentRegion(dbobj.getString(FieldType.PAYMENTS_REGION.toString()));

					} else {
						transReport.setPaymentRegion(CrmFieldConstants.NA.getValue());
					}

					if (StringUtils.isNotBlank(dbobj.getString(FieldType.CARD_HOLDER_TYPE.toString()))) {
						transReport.setCardHolderType(dbobj.getString(FieldType.CARD_HOLDER_TYPE.toString()));

					} else {
						transReport.setCardHolderType(CrmFieldConstants.NA.getValue());
					}
					// changes regarding partner reseller
					if (user.getUserType().equals(UserType.RESELLER) && user.isPartnerFlag()) {

						if (StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_CHARGES.toString()))) {

							transReport.setResellerCharges(String.format("%.2f",
									(Double.parseDouble(dbobj.getString(FieldType.RESELLER_CHARGES.toString())))));

						} else {
							transReport.setResellerCharges("0.00");
						}

						if (StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_GST.toString()))) {
							transReport.setResellerGST(String.format("%.2f",
									(Double.parseDouble(dbobj.getString(FieldType.RESELLER_GST.toString())))));

						} else {
							transReport.setResellerGST("0.00");
						}

						if (StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_CHARGES.toString()))) {
							if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
									&& (StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.toString())))) {

								transReport.setTdr_Surcharge(String.format("%.2f",
										(Double.parseDouble(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
												+ Double.parseDouble(dbobj.getString(FieldType.PG_TDR_SC.toString()))
												+ Double.parseDouble(
														dbobj.getString(FieldType.RESELLER_CHARGES.toString())))));

							} else {
								transReport.setTdr_Surcharge("0.00");
							}
						} else {
							if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
									&& (StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.toString())))) {

								transReport.setTdr_Surcharge(String.format("%.2f",
										(Double.parseDouble(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
												+ Double.parseDouble(
														dbobj.getString(FieldType.PG_TDR_SC.toString())))));

							} else {
								transReport.setTdr_Surcharge("0.00");
							}
						}

						if (StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_GST.toString()))) {
							if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.toString()))
									&& (StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.toString())))) {
								transReport.setGst_charge(String.format("%.2f",
										(Double.parseDouble(dbobj.getString(FieldType.ACQUIRER_GST.toString()))
												+ Double.parseDouble(dbobj.getString(FieldType.PG_GST.toString()))
												+ Double.parseDouble(
														dbobj.getString(FieldType.RESELLER_GST.toString())))));

							} else {
								transReport.setGst_charge("0.00");
							}
						} else {
							if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.toString()))
									&& (StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.toString())))) {
								transReport.setGst_charge(String.format("%.2f",
										(Double.parseDouble(dbobj.getString(FieldType.ACQUIRER_GST.toString()))
												+ Double.parseDouble(dbobj.getString(FieldType.PG_GST.toString())))));

							} else {
								transReport.setGst_charge("0.00");
							}
						}

					} // END

					else {

						if (StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_CHARGES.toString()))) {
							if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
									&& (StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.toString())))) {

								transReport.setTdr_Surcharge(String.format("%.2f",
										(Double.parseDouble(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
												+ Double.parseDouble(dbobj.getString(FieldType.PG_TDR_SC.toString()))
												+ Double.parseDouble(
														dbobj.getString(FieldType.RESELLER_CHARGES.toString())))));

							} else {
								transReport.setTdr_Surcharge("0.00");
							}
						} else {
							if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
									&& (StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.toString())))) {

								transReport.setTdr_Surcharge(String.format("%.2f",
										(Double.parseDouble(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
												+ Double.parseDouble(
														dbobj.getString(FieldType.PG_TDR_SC.toString())))));

							} else {
								transReport.setTdr_Surcharge("0.00");
							}
						}

						if (StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_GST.toString()))) {
							if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.toString()))
									&& (StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.toString())))) {
								transReport.setGst_charge(String.format("%.2f",
										(Double.parseDouble(dbobj.getString(FieldType.ACQUIRER_GST.toString()))
												+ Double.parseDouble(dbobj.getString(FieldType.PG_GST.toString()))
												+ Double.parseDouble(
														dbobj.getString(FieldType.RESELLER_GST.toString())))));

							} else {
								transReport.setGst_charge("0.00");
							}
						} else {
							if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.toString()))
									&& (StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.toString())))) {
								transReport.setGst_charge(String.format("%.2f",
										(Double.parseDouble(dbobj.getString(FieldType.ACQUIRER_GST.toString()))
												+ Double.parseDouble(dbobj.getString(FieldType.PG_GST.toString())))));

							} else {
								transReport.setGst_charge("0.00");
							}
						}

					}

					if (TxnType.SALE.getName().equalsIgnoreCase(dbobj.getString(FieldType.ORIG_TXNTYPE.getName()))) {

						if (StringUtils.isNotBlank(dbobj.getString(FieldType.PAYMENT_TYPE.toString()))) {

							if (dbobj.getString(FieldType.PAYMENT_TYPE.getName())
									.equalsIgnoreCase(PaymentType.COD.getCode())) {

								if (isGlocal == true) {
									transReport.setGlocalFlag(true);
									if (dbobj.containsKey(FieldType.UDF7.getName())) {

										if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF7.getName()))) {

											if (StringUtils
													.isNotBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
													&& StringUtils
															.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.toString()))
													&& StringUtils.isNotBlank(
															dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
													&& StringUtils
															.isNotBlank(dbobj.getString(FieldType.PG_GST.toString()))
													&& StringUtils.isNotBlank(
															dbobj.getString(FieldType.ACQUIRER_GST.toString()))) {

												String merchantAmount = String.format("%.2f", Double
														.parseDouble(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
														- (Double.parseDouble(
																dbobj.getString(FieldType.PG_TDR_SC.toString()))
																+ Double.parseDouble(dbobj.getString(
																		FieldType.ACQUIRER_TDR_SC.toString()))
																+ Double.parseDouble(
																		dbobj.getString(FieldType.PG_GST.toString()))
																+ Double.parseDouble(dbobj.getString(
																		FieldType.ACQUIRER_GST.toString()))));

												transReport.setDoctor(String.valueOf(
														new BigDecimal(dbobj.getString(FieldType.UDF7.getName()))
																.multiply(new BigDecimal(merchantAmount))
																.divide(new BigDecimal(
																		Constants.MAX_NUMBER_OF_KEYS.getValue()))
																.setScale(2, BigDecimal.ROUND_HALF_UP)));
											} else {
												transReport.setDoctor("0");
											}
										} else {
											transReport.setDoctor("0");
										}
									} else {
										transReport.setDoctor("0");
									}

									if (dbobj.containsKey(FieldType.UDF8.getName())) {
										if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF8.getName()))) {

											if (StringUtils
													.isNotBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
													&& StringUtils
															.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.toString()))
													&& StringUtils.isNotBlank(
															dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
													&& StringUtils
															.isNotBlank(dbobj.getString(FieldType.PG_GST.toString()))
													&& StringUtils.isNotBlank(
															dbobj.getString(FieldType.ACQUIRER_GST.toString()))) {

												String merchantAmount = String.format("%.2f", Double
														.parseDouble(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
														- (Double.parseDouble(
																dbobj.getString(FieldType.PG_TDR_SC.toString()))
																+ Double.parseDouble(dbobj.getString(
																		FieldType.ACQUIRER_TDR_SC.toString()))
																+ Double.parseDouble(
																		dbobj.getString(FieldType.PG_GST.toString()))
																+ Double.parseDouble(dbobj.getString(
																		FieldType.ACQUIRER_GST.toString()))));

												transReport.setGlocal(String.valueOf(
														new BigDecimal(dbobj.getString(FieldType.UDF8.getName()))
																.multiply(new BigDecimal(merchantAmount))
																.divide(new BigDecimal(
																		Constants.MAX_NUMBER_OF_KEYS.getValue()))
																.setScale(2, BigDecimal.ROUND_HALF_UP)));
											} else {
												transReport.setGlocal("0");
											}
										} else {
											transReport.setGlocal("0");
										}
									} else {
										transReport.setGlocal("0");
									}

									if (dbobj.containsKey(FieldType.UDF9.getName())) {
										if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF9.getName()))) {

											if (StringUtils
													.isNotBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
													&& StringUtils
															.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.toString()))
													&& StringUtils.isNotBlank(
															dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
													&& StringUtils
															.isNotBlank(dbobj.getString(FieldType.PG_GST.toString()))
													&& StringUtils.isNotBlank(
															dbobj.getString(FieldType.ACQUIRER_GST.toString()))) {

												String merchantAmount = String.format("%.2f", Double
														.parseDouble(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
														- (Double.parseDouble(
																dbobj.getString(FieldType.PG_TDR_SC.toString()))
																+ Double.parseDouble(dbobj.getString(
																		FieldType.ACQUIRER_TDR_SC.toString()))
																+ Double.parseDouble(
																		dbobj.getString(FieldType.PG_GST.toString()))
																+ Double.parseDouble(dbobj.getString(
																		FieldType.ACQUIRER_GST.toString()))));

												transReport.setPartner(String.valueOf(
														new BigDecimal(dbobj.getString(FieldType.UDF9.getName()))
																.multiply(new BigDecimal(merchantAmount))
																.divide(new BigDecimal(
																		Constants.MAX_NUMBER_OF_KEYS.getValue()))
																.setScale(2, BigDecimal.ROUND_HALF_UP)));
											} else {
												transReport.setPartner("0");
											}
										} else {
											transReport.setPartner("0");
										}
									} else {
										transReport.setPartner("0");
									}

									if (dbobj.containsKey(FieldType.UDF11.getName())) {
										if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF11.getName()))) {
											transReport.setUniqueId(dbobj.getString(FieldType.UDF11.getName()));
										} else {
											transReport.setUniqueId("NA");
										}
									} else {
										transReport.setUniqueId("NA");
									}
								} else {
									transReport.setGlocalFlag(false);
								}

								if (StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_CHARGES.toString()))
										&& StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_GST.toString()))) {
									if (StringUtils.isNotBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
											&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.toString()))
											&& StringUtils
													.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
											&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.toString()))
											&& StringUtils
													.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.toString()))) {
										transReport.setTotalAmtPayable(String.format("%.2f", (Double
												.parseDouble(dbobj.getString(FieldType.PG_TDR_SC.toString()))
												+ Double.parseDouble(
														dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
												+ Double.parseDouble(dbobj.getString(FieldType.PG_GST.toString()))
												+ Double.parseDouble(dbobj.getString(FieldType.ACQUIRER_GST.toString()))
												+ Double.parseDouble(
														dbobj.getString(FieldType.RESELLER_CHARGES.toString()))
												+ Double.parseDouble(
														dbobj.getString(FieldType.RESELLER_GST.toString())))));

										if (transReport.getTotalAmtPayable().equalsIgnoreCase("0.00")) {
											transReport.setTotalAmtPayable(transReport.getTotalAmtPayable());
										} else {
											transReport.setTotalAmtPayable("-" + transReport.getTotalAmtPayable());
										}
									} else {
										transReport.setTotalAmtPayable("NA");
									}

								} else {

									if (StringUtils.isNotBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
											&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.toString()))
											&& StringUtils
													.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
											&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.toString()))
											&& StringUtils
													.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.toString()))) {
										transReport.setTotalAmtPayable(String.format("%.2f",
												(Double.parseDouble(dbobj.getString(FieldType.PG_TDR_SC.toString()))
														+ Double.parseDouble(
																dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
														+ Double.parseDouble(
																dbobj.getString(FieldType.PG_GST.toString()))
														+ Double.parseDouble(
																dbobj.getString(FieldType.ACQUIRER_GST.toString())))));
										if (transReport.getTotalAmtPayable().equalsIgnoreCase("0.00")) {
											transReport.setTotalAmtPayable(transReport.getTotalAmtPayable());
										} else {
											transReport.setTotalAmtPayable("-" + transReport.getTotalAmtPayable());
										}

									} else {
										transReport.setTotalAmtPayable("NA");
									}
								}

							} else if (isGlocal == true) {

								transReport.setGlocalFlag(true);

								if (dbobj.containsKey(FieldType.UDF7.getName())) {

									if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF7.getName()))) {

										if (StringUtils.isNotBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
												&& StringUtils
														.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.toString()))
												&& StringUtils.isNotBlank(
														dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
												&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.toString()))
												&& StringUtils.isNotBlank(
														dbobj.getString(FieldType.ACQUIRER_GST.toString()))) {

											String merchantAmount = String.format("%.2f", Double
													.parseDouble(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
													- (Double.parseDouble(
															dbobj.getString(FieldType.PG_TDR_SC.toString()))
															+ Double.parseDouble(dbobj
																	.getString(FieldType.ACQUIRER_TDR_SC.toString()))
															+ Double.parseDouble(
																	dbobj.getString(FieldType.PG_GST.toString()))
															+ Double.parseDouble(dbobj
																	.getString(FieldType.ACQUIRER_GST.toString()))));

											transReport.setDoctor(String
													.valueOf(new BigDecimal(dbobj.getString(FieldType.UDF7.getName()))
															.multiply(new BigDecimal(merchantAmount))
															.divide(new BigDecimal(
																	Constants.MAX_NUMBER_OF_KEYS.getValue()))
															.setScale(2, BigDecimal.ROUND_HALF_UP)));
										} else {
											transReport.setDoctor("0");
										}
									} else {
										transReport.setDoctor("0");
									}
								} else {
									transReport.setDoctor("0");
								}

								if (dbobj.containsKey(FieldType.UDF8.getName())) {
									if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF8.getName()))) {

										if (StringUtils.isNotBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
												&& StringUtils
														.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.toString()))
												&& StringUtils.isNotBlank(
														dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
												&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.toString()))
												&& StringUtils.isNotBlank(
														dbobj.getString(FieldType.ACQUIRER_GST.toString()))) {

											String merchantAmount = String.format("%.2f", Double
													.parseDouble(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
													- (Double.parseDouble(
															dbobj.getString(FieldType.PG_TDR_SC.toString()))
															+ Double.parseDouble(dbobj
																	.getString(FieldType.ACQUIRER_TDR_SC.toString()))
															+ Double.parseDouble(
																	dbobj.getString(FieldType.PG_GST.toString()))
															+ Double.parseDouble(dbobj
																	.getString(FieldType.ACQUIRER_GST.toString()))));

											transReport.setGlocal(String
													.valueOf(new BigDecimal(dbobj.getString(FieldType.UDF8.getName()))
															.multiply(new BigDecimal(merchantAmount))
															.divide(new BigDecimal(
																	Constants.MAX_NUMBER_OF_KEYS.getValue()))
															.setScale(2, BigDecimal.ROUND_HALF_UP)));
										} else {
											transReport.setGlocal("0");
										}
									} else {
										transReport.setGlocal("0");
									}
								} else {
									transReport.setGlocal("0");
								}

								if (dbobj.containsKey(FieldType.UDF9.getName())) {
									if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF9.getName()))) {

										if (StringUtils.isNotBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
												&& StringUtils
														.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.toString()))
												&& StringUtils.isNotBlank(
														dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
												&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.toString()))
												&& StringUtils.isNotBlank(
														dbobj.getString(FieldType.ACQUIRER_GST.toString()))) {

											String merchantAmount = String.format("%.2f", Double
													.parseDouble(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
													- (Double.parseDouble(
															dbobj.getString(FieldType.PG_TDR_SC.toString()))
															+ Double.parseDouble(dbobj
																	.getString(FieldType.ACQUIRER_TDR_SC.toString()))
															+ Double.parseDouble(
																	dbobj.getString(FieldType.PG_GST.toString()))
															+ Double.parseDouble(dbobj
																	.getString(FieldType.ACQUIRER_GST.toString()))));

											transReport.setPartner(String
													.valueOf(new BigDecimal(dbobj.getString(FieldType.UDF9.getName()))
															.multiply(new BigDecimal(merchantAmount))
															.divide(new BigDecimal(
																	Constants.MAX_NUMBER_OF_KEYS.getValue()))
															.setScale(2, BigDecimal.ROUND_HALF_UP)));
										} else {
											transReport.setPartner("0");
										}
									} else {
										transReport.setPartner("0");
									}
								} else {
									transReport.setPartner("0");
								}

								if (dbobj.containsKey(FieldType.UDF11.getName())) {
									if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF11.getName()))) {
										transReport.setUniqueId(dbobj.getString(FieldType.UDF11.getName()));
									} else {
										transReport.setUniqueId("NA");
									}
								} else {
									transReport.setUniqueId("NA");
								}
							} else {
								transReport.setGlocalFlag(false);
							}

							if (!dbobj.getString(FieldType.PAYMENT_TYPE.getName())
									.equalsIgnoreCase(PaymentType.COD.getCode())) {

								if (StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_CHARGES.toString()))
										&& StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_GST.toString()))) {

									if (StringUtils.isNotBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
											&& StringUtils
													.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
											&& StringUtils
													.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.toString()))) {
										transReport.setTotalAmtPayable(String.format("%.2f", Double
												.parseDouble(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
												- (Double.parseDouble(
														dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
														+ Double.parseDouble(
																dbobj.getString(FieldType.ACQUIRER_GST.toString()))
														+ Double.parseDouble(
																dbobj.getString(FieldType.PG_TDR_SC.toString()))
														+ Double.parseDouble(
																dbobj.getString(FieldType.PG_GST.toString()))
														+ Double.parseDouble(
																dbobj.getString(FieldType.RESELLER_CHARGES.toString()))
														+ Double.parseDouble(
																dbobj.getString(FieldType.RESELLER_GST.toString())))));
									} else {
										if (StringUtils.isNotBlank(dbobj.getString(FieldType.PAYMENT_TYPE.toString()))
												&& (dbobj.getString(FieldType.PAYMENT_TYPE.toString())
														.equalsIgnoreCase("RTGS")
														|| dbobj.getString(FieldType.PAYMENT_TYPE.toString())
																.equalsIgnoreCase("NEFT")
														|| dbobj.getString(FieldType.PAYMENT_TYPE.toString())
																.equalsIgnoreCase("IMPS"))) {

											transReport.setTotalAmtPayable(
													dbobj.getString(FieldType.TOTAL_AMOUNT.toString()));
										} else {
											transReport.setTotalAmtPayable("NA");
										}
									}

								} else {

									if (StringUtils.isNotBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
											&& StringUtils
													.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
											&& StringUtils
													.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.toString()))) {
										transReport.setTotalAmtPayable(String.format("%.2f", Double
												.parseDouble(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
												- (Double.parseDouble(
														dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
														+ Double.parseDouble(
																dbobj.getString(FieldType.ACQUIRER_GST.toString()))
														+ Double.parseDouble(
																dbobj.getString(FieldType.PG_TDR_SC.toString()))
														+ Double.parseDouble(
																dbobj.getString(FieldType.PG_GST.toString())))));
									} else {
										if (StringUtils.isNotBlank(dbobj.getString(FieldType.PAYMENT_TYPE.toString()))
												&& (dbobj.getString(FieldType.PAYMENT_TYPE.toString())
														.equalsIgnoreCase("RTGS")
														|| dbobj.getString(FieldType.PAYMENT_TYPE.toString())
																.equalsIgnoreCase("NEFT")
														|| dbobj.getString(FieldType.PAYMENT_TYPE.toString())
																.equalsIgnoreCase("IMPS"))) {

											transReport.setTotalAmtPayable(
													dbobj.getString(FieldType.TOTAL_AMOUNT.toString()));
										} else {
											transReport.setTotalAmtPayable("NA");
										}
									}
								}
							}

						} else {
							transReport.setTotalAmtPayable("NA");
						}

					} else if (TxnType.REFUND.getName()
							.equalsIgnoreCase(dbobj.getString(FieldType.ORIG_TXNTYPE.getName()))) {
						if (isGlocal == true) {
							transReport.setGlocalFlag(true);
						} else {
							transReport.setGlocalFlag(false);
						}
						if (StringUtils.isNotBlank(dbobj.getString(FieldType.PAYMENT_TYPE.getName()))) {

							if (dbobj.getString(FieldType.PAYMENT_TYPE.getName())
									.equalsIgnoreCase(PaymentType.COD.getCode())) {
								transReport.setGst_charge("0.00");
								transReport.setTdr_Surcharge("0.00");
								transReport.setTotalAmtPayable("0.00");
							} else {
								if (StringUtils.isNotBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))) {
									transReport.setTotalAmtPayable(String.format("%.2f",
											Double.parseDouble(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
													* -1));
								} else {
									transReport.setTotalAmtPayable("NA");
								}
							}

						} else {
							transReport.setTotalAmtPayable("NA");
						}

					} else {
						transReport.setTotalAmtPayable("NA");
					}

					// Exclude certain records when loading search payment without
					// any filters

					Comparator<TransactionSearch> comp = (TransactionSearch a, TransactionSearch b) -> {

						if (a.getDateFrom().compareTo(b.getDateFrom()) > 0) {
							return -1;
						} else if (a.getDateFrom().compareTo(b.getDateFrom()) < 0) {
							return 1;
						} else {
							return 0;
						}
					};
					transactionList.add(transReport);
					Collections.sort(transactionList, comp);
				}

			}
			logger.info("Inside TxnReports , searchPayment , transactionListSize = " + transactionList.size());
			Comparator<TransactionSearch> comp = (TransactionSearch a, TransactionSearch b) -> {

				if (a.getDateFrom().compareTo(b.getDateFrom()) > 0) {
					return -1;
				} else if (a.getDateFrom().compareTo(b.getDateFrom()) < 0) {
					return 1;
				} else {
					return 0;
				}
			};
			Collections.sort(transactionList, comp);
			return transactionList;
		}

		catch (Exception e) {
			logger.error("Exception occured in TxnReports , searchPayment , Exception = ", e);
			return null;
		}
	}

	@SuppressWarnings("static-access")
	public int searchPaymentCount(String pgRefNum, String orderId, String customerEmail, String SKUCode,
			String merchantPayId, String subMerchantPayId, String paymentType, String Userstatus, String currency,
			String transactionType, String fromDate, String toDate, User user, String partSettleFlag,
			String paymentRegion, Boolean searchFlag, String deliveryStatus, Set<String> orderIdSet,
			boolean allowLatest, String transactionFlag, String deltaFlag, String rrn) {

		logger.info("Inside TxnReports , searchPaymentCount");
		try {
			int total = 0;
			List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();
			List<String> statusConditionList = new ArrayList<String>();

			BasicDBObject statusQuery = new BasicDBObject();
			BasicDBObject customerQuery = new BasicDBObject();
			BasicDBObject customerQueryMask = new BasicDBObject();

			BasicDBObject dateIndexConditionQuery = new BasicDBObject();

			DateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.ENGLISH);
			Date dateStart = format.parse(fromDate);
			Date dateEnd = format.parse(toDate);

			LocalDate startDate = dateStart.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			LocalDate endDate = dateEnd.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

			String fromDatesIndex = startDate.toString().replaceAll("-", "");
			String toDatesIndex = endDate.toString().replaceAll("-", "");

			dateIndexConditionQuery.put("DATE_INDEX",
					BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(fromDatesIndex).toLocalizedPattern())
							.add("$lte", new SimpleDateFormat(toDatesIndex).toLocalizedPattern()).get());

			if (user.getUserType().equals(UserType.PARENTMERCHANT)) {
				paramConditionLst.add(new BasicDBObject(FieldType.PARENT_PAY_ID.getName(), user.getPayId()));
			} else {
				if (!merchantPayId.equalsIgnoreCase("ALL")) {
					paramConditionLst.add(new BasicDBObject(FieldType.PAY_ID.getName(), merchantPayId));
				}
			}

			if (StringUtils.isNotBlank(subMerchantPayId) && !subMerchantPayId.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.SUB_MERCHANT_ID.getName(), subMerchantPayId));
			}
			if (StringUtils.isNotEmpty(paymentType) && !paymentType.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.PAYMENT_TYPE.getName(), paymentType));
			}

			if (user.getUserType().equals(UserType.RESELLER)) {
				paramConditionLst.add(new BasicDBObject(FieldType.RESELLER_ID.getName(), user.getResellerId()));
			}

			if (!pgRefNum.isEmpty()) {
				paramConditionLst.add(new BasicDBObject(FieldType.PG_REF_NUM.getName(), pgRefNum));
			}
			
			if (!rrn.isEmpty()) {
				paramConditionLst.add(new BasicDBObject(FieldType.RRN.getName(), rrn));
			}

			BasicDBObject orderIdConditionQuery = null;
			if (orderIdSet != null) {
				List<String> orderIdList = new ArrayList<>(orderIdSet);
				orderIdConditionQuery = new BasicDBObject("$in", orderIdList);
			}
			if (!orderId.isEmpty()) {
				paramConditionLst.add(new BasicDBObject(FieldType.ORDER_ID.getName(), orderId));
			} else if (null != orderIdConditionQuery) {
				paramConditionLst.add(new BasicDBObject(FieldType.ORDER_ID.getName(), orderIdConditionQuery));
			}

			if (StringUtils.isNotEmpty(customerEmail) && !customerEmail.isEmpty()) {
				customerQuery.append(FieldType.CUST_EMAIL.getName(), customerEmail);
				if (StringUtils.isNotBlank(customerEmail)) {
					String email = customerEmail;
					String emailMask = null;
					final String mask = "*****";
					final int at = email.indexOf("@");
					if (at > 2) {
						final int maskLen = Math.min(Math.max(at / 2, 2), 5);
						final int start1 = (at - maskLen) / 2;
						emailMask = email.substring(0, start1) + mask.substring(0, maskLen)
								+ email.substring(start1 + maskLen);
					}
					customerQueryMask.append(FieldType.CUST_EMAIL.getName(), emailMask);
				}
			}

			if (StringUtils.isNotBlank(Userstatus) && !Userstatus.equalsIgnoreCase("ALL")) {
				String statusArr[] = Userstatus.split(",");
				for (String sts : statusArr) {
					statusConditionList.add(sts.trim());
				}
				statusQuery.append("$in", statusConditionList);
				paramConditionLst.add(new BasicDBObject(FieldType.STATUS.getName(), statusQuery));
			}

			if (!currency.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.CURRENCY_CODE.getName(), currency));
			}
			if (!partSettleFlag.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.PART_SETTLE.getName(), partSettleFlag));
			}

			if (!transactionType.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.ORIG_TXNTYPE.getName(), transactionType));
			}

			if (!paymentRegion.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.PAYMENTS_REGION.getName(), paymentRegion));
			}

			if (StringUtils.isNotBlank(transactionFlag) && !transactionFlag.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.TXN_CAPTURE_FLAG.getName(), transactionFlag));
			}

			if (StringUtils.isNotBlank(deltaFlag) && !deltaFlag.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.DELTA_REFUND_FLAG.getName(), deltaFlag));
			}

			if (StringUtils.isNotBlank(pgRefNum) || StringUtils.isNotBlank(orderId) || StringUtils.isNotBlank(rrn)) {

			} else {

				if (!dateIndexConditionQuery.isEmpty()) {
					paramConditionLst.add(dateIndexConditionQuery);
				}

			}

			List<BasicDBObject> fianlQueryList = new ArrayList<BasicDBObject>();

			fianlQueryList.addAll(paramConditionLst);

			if (!customerQuery.isEmpty()) {
				paramConditionLst.add(customerQuery);
			}

			if (!customerQueryMask.isEmpty()) {
				fianlQueryList.add(customerQueryMask);
			}

			BasicDBObject finalquery = new BasicDBObject("$and", paramConditionLst);
			BasicDBObject finalqueryMask = new BasicDBObject("$and", fianlQueryList);

			if (!customerQueryMask.isEmpty()) {
				logger.info("Inside TxnReports , searchPayment , finalQuery = " + finalqueryMask);
			} else {
				logger.info("Inside TxnReports , searchPayment , finalQuery = " + finalquery);
			}

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = null;
			if (searchFlag == true) {
				coll = dbIns.getCollection(propertiesManager.propertiesMap
						.get(prefix + Constants.TRANSACTION_STATUS_COLLECTION.getValue()));
			} else {
				coll = dbIns.getCollection(
						propertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));
			}

			if (StringUtils.isNotBlank(orderId)) {
				List<TransactionSearch> transactionSearchList = new ArrayList<TransactionSearch>();
				MongoCursor<Document> cursor = coll.find(finalquery)
						.sort(new BasicDBObject(FieldType.CREATE_DATE.getName(), -1)).iterator();

				while (cursor.hasNext()) {
					Document doc = cursor.next();

					if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
							&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
						doc = dataEncDecTool.decryptDocument(doc);
					}
					TransactionSearch transactionSearch = new TransactionSearch();
					transactionSearch.setStatus(doc.getString(FieldType.STATUS.toString()));
					transactionSearch.setOrderId(doc.getString(FieldType.ORDER_ID.toString()));
					transactionSearch.setDateFrom(doc.getString(FieldType.CREATE_DATE.toString()));

					transactionSearchList.add(transactionSearch);
				}

				String capturedDate = "";

				for (TransactionSearch searchTransaction : transactionSearchList) {
					if (searchTransaction.getStatus().equalsIgnoreCase(StatusType.CAPTURED.getName())) {
						capturedDate = searchTransaction.getDateFrom();
					}

					if (StringUtils.isNotBlank(capturedDate)) {
						BasicDBObject dateQuery = new BasicDBObject();
						DateFormat format1 = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
						Date date = format.parse(capturedDate);
						String dateFrom = format1.format(date) + " 00:00:00";
						String dateTo = capturedDate;
						dateQuery.put(FieldType.CREATE_DATE.getName(),
								BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(dateFrom).toLocalizedPattern())
										.add("$lte", new SimpleDateFormat(dateTo).toLocalizedPattern()).get());
						paramConditionLst.add(new BasicDBObject(dateQuery));

						BasicDBObject finalquery1 = new BasicDBObject("$and", paramConditionLst);

						MongoDatabase dbIns1 = mongoInstance.getDB();
						MongoCollection<Document> coll1 = dbIns1.getCollection(
								propertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));

						total = (int) coll1.count(finalquery1);
					}
				}
			} else {
				total = (int) coll.count(finalquery);
			}
			logger.info("Inside searchPaymentCount , total records from DB  = " + total);
			return total;

		} catch (

		Exception e) {
			logger.error("Exception occured in TxnReports , searchPaymentCount n exception = ", e);
			return 0;
		}
	}

	public int transactionCount(String pgRefNum, String orderId, String customerEmail, String merchantPayId,
			String subMerchantPayId, String paymentType, String Userstatus, String currency, String transactionType,
			String fromDate, String toDate, User sessionUser, String partSettleFlag, String paymentRegion,
			Set<String> orderIdSet, String transactionFlag, String deltaFlag, String autoRefund) {

		logger.info("Inside TxnReports , transactionCount");
		try {
			int total = 0;
			List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();

			BasicDBObject customerQuery = new BasicDBObject();
			BasicDBObject customerQueryMask = new BasicDBObject();

			BasicDBObject txnCapturedFlag = new BasicDBObject();

			BasicDBObject dateIndexConditionQuery = new BasicDBObject();

			DateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.ENGLISH);
			Date dateStart = format.parse(fromDate);
			Date dateEnd = format.parse(toDate);

			LocalDate startDate = dateStart.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			LocalDate endDate = dateEnd.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

			String fromDatesIndex = startDate.toString().replaceAll("-", "");
			String toDatesIndex = endDate.toString().replaceAll("-", "");

			dateIndexConditionQuery.put("DATE_INDEX",
					BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(fromDatesIndex).toLocalizedPattern())
							.add("$lte", new SimpleDateFormat(toDatesIndex).toLocalizedPattern()).get());

			if (sessionUser.getUserType().equals(UserType.PARENTMERCHANT)) {
				paramConditionLst.add(new BasicDBObject(FieldType.PARENT_PAY_ID.getName(), sessionUser.getPayId()));

			} else if (!merchantPayId.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.PAY_ID.getName(), merchantPayId));
			}

			if (StringUtils.isNotBlank(subMerchantPayId) && !subMerchantPayId.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.SUB_MERCHANT_ID.getName(), subMerchantPayId));
			}
			if (StringUtils.isNotEmpty(paymentType) && !paymentType.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.PAYMENT_TYPE.getName(), paymentType));
			} else {

			}

			if (sessionUser.getUserType().equals(UserType.RESELLER)) {
				paramConditionLst.add(new BasicDBObject(FieldType.RESELLER_ID.getName(), sessionUser.getResellerId()));
			}

			if (!pgRefNum.isEmpty()) {
				paramConditionLst.add(new BasicDBObject(FieldType.PG_REF_NUM.getName(), pgRefNum));
			}

			BasicDBObject orderIdConditionQuery = null;
			if (orderIdSet != null) {
				List<String> orderIdList = new ArrayList<>(orderIdSet);
				orderIdConditionQuery = new BasicDBObject("$in", orderIdList);
			}
			if (!orderId.isEmpty()) {
				paramConditionLst.add(new BasicDBObject(FieldType.ORDER_ID.getName(), orderId));
			} else if (null != orderIdConditionQuery) {
				paramConditionLst.add(new BasicDBObject(FieldType.ORDER_ID.getName(), orderIdConditionQuery));
			}

			if (StringUtils.isNotEmpty(customerEmail) && !customerEmail.isEmpty()) {
				customerQuery.append(FieldType.CUST_EMAIL.getName(), customerEmail);
				if (StringUtils.isNotBlank(customerEmail)) {
					String email = customerEmail;
					String emailMask = null;
					final String mask = "*****";
					final int at = email.indexOf("@");
					if (at > 2) {
						final int maskLen = Math.min(Math.max(at / 2, 2), 5);
						final int start1 = (at - maskLen) / 2;
						emailMask = email.substring(0, start1) + mask.substring(0, maskLen)
								+ email.substring(start1 + maskLen);
					}
					customerQueryMask.append(FieldType.CUST_EMAIL.getName(), emailMask);
				}
			}

			if (StringUtils.isNotBlank(Userstatus)) {
				paramConditionLst.add(new BasicDBObject(FieldType.STATUS.getName(), Userstatus));
			}

			if (!currency.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.CURRENCY_CODE.getName(), currency));
			} else {

			}
			if (!partSettleFlag.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.PART_SETTLE.getName(), partSettleFlag));
			} else {

			}

			if (!transactionType.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.ORIG_TXNTYPE.getName(), transactionType));
			} else {
			}

			if (!paymentRegion.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.PAYMENTS_REGION.getName(), paymentRegion));
			} else {
			}
			if (StringUtils.isNotBlank(transactionFlag) && !transactionFlag.equalsIgnoreCase("ALL")) {
				txnCapturedFlag.append("$in", transactionFlag.split(","));
				paramConditionLst.add(new BasicDBObject(FieldType.TXN_CAPTURE_FLAG.getName(), txnCapturedFlag));
			}

			if (StringUtils.isNotBlank(deltaFlag) && !deltaFlag.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.DELTA_REFUND_FLAG.getName(), deltaFlag));
			}

			if (StringUtils.isNotBlank(autoRefund) && !autoRefund.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.AUTO_REFUND_FLAG.getName(), autoRefund));
			}

			if (StringUtils.isNotBlank(pgRefNum) || StringUtils.isNotBlank(orderId)) {

			} else {
				if (!dateIndexConditionQuery.isEmpty()) {
					paramConditionLst.add(dateIndexConditionQuery);
				}
			}

			List<BasicDBObject> fianlQueryList = new ArrayList<BasicDBObject>();

			fianlQueryList.addAll(paramConditionLst);
			if (!customerQuery.isEmpty()) {
				paramConditionLst.add(customerQuery);
			}

			if (!customerQueryMask.isEmpty()) {
				fianlQueryList.add(customerQueryMask);
			}

			BasicDBObject finalquery = new BasicDBObject("$and", paramConditionLst);
			BasicDBObject finalqueryMask = new BasicDBObject("$and", fianlQueryList);

			if (!customerQueryMask.isEmpty()) {
				logger.info("Inside TxnReports , transactionCount , finalQuery = " + finalqueryMask);
			} else {
				logger.info("Inside TxnReports , transactionCount , finalQuery = " + finalquery);
			}

			MongoDatabase dbIns = mongoInstance.getDB();

			MongoCollection<Document> coll = dbIns
					.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));
			BasicDBObject match = new BasicDBObject("$match", finalquery);
			BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject("CREATE_DATE", -1));

			List<BasicDBObject> pipeline = Arrays.asList(match, sort);
			total = (int) coll.count(finalquery);
			logger.info("Inside transactionCount , total records from DB  = " + total);
			return total;

		} catch (Exception e) {
			logger.error("Exception occured in TxnReports , transactionCount n exception = ", e);
			return 0;
		}
	}

	public List<TransactionSearch> transactionReport(String pgRefNum, String orderId, String customerEmail,
			String merchantPayId, String subMerchantPayId, String paymentType, String Userstatus, String currency,
			String transactionType, String fromDate, String toDate, User sessionUser, int start, int length,
			String partSettleFlag, String paymentRegion, Set<String> orderIdSet, String transactionFlag,
			String deltaFlag, String autoRefund) {
		Map<String, User> userMap = new HashMap<String, User>();

		logger.info("Inside TxnReports , transactionReport");

		boolean isGlocal = false;

		try {

			// if (!merchantPayId.equalsIgnoreCase("ALL")) {
			// String identifierKey =
			// propertiesManager.propertiesMap.get(Constants.MERCHANT_PAYID.getValue());
			// if (StringUtils.isNotBlank(identifierKey) &&
			// identifierKey.contains(merchantPayId)) {
			// isGlocal = true;
			// }
			// }

			UserSettingData userSettings = userSettingDao.fetchDataUsingPayId(sessionUser.getPayId());

			List<TransactionSearch> transactionList = new ArrayList<TransactionSearch>();
			PropertiesManager propManager = new PropertiesManager();
			List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();
			BasicDBObject customerQuery = new BasicDBObject();
			BasicDBObject customerQueryMask = new BasicDBObject();
			BasicDBObject txnCapturedFlag = new BasicDBObject();
			BasicDBObject dateIndexConditionQuery = new BasicDBObject();

			DateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.ENGLISH);
			Date dateStart = format.parse(fromDate);
			Date dateEnd = format.parse(toDate);

			LocalDate startDate = dateStart.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			LocalDate endDate = dateEnd.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

			String fromDatesIndex = startDate.toString().replaceAll("-", "");
			String toDatesIndex = endDate.toString().replaceAll("-", "");

			dateIndexConditionQuery.put("DATE_INDEX",
					BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(fromDatesIndex).toLocalizedPattern())
							.add("$lte", new SimpleDateFormat(toDatesIndex).toLocalizedPattern()).get());

			if (sessionUser.getUserType().equals(UserType.PARENTMERCHANT)) {
				paramConditionLst.add(new BasicDBObject(FieldType.PARENT_PAY_ID.getName(), sessionUser.getPayId()));

			} else if (!merchantPayId.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.PAY_ID.getName(), merchantPayId));
			}

			if (StringUtils.isNotBlank(subMerchantPayId) && !subMerchantPayId.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.SUB_MERCHANT_ID.getName(), subMerchantPayId));
			}
			if (StringUtils.isNotEmpty(paymentType) && !paymentType.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.PAYMENT_TYPE.getName(), paymentType));
			} else {

			}
			if (sessionUser.getUserType().equals(UserType.RESELLER)) {
				paramConditionLst.add(new BasicDBObject(FieldType.RESELLER_ID.getName(), sessionUser.getResellerId()));
			}

			if (!pgRefNum.isEmpty()) {
				paramConditionLst.add(new BasicDBObject(FieldType.PG_REF_NUM.getName(), pgRefNum));
			}

			BasicDBObject orderIdConditionQuery = null;
			if (orderIdSet != null) {
				List<String> orderIdList = new ArrayList<>(orderIdSet);
				orderIdConditionQuery = new BasicDBObject("$in", orderIdList);
			}
			if (!orderId.isEmpty()) {
				paramConditionLst.add(new BasicDBObject(FieldType.ORDER_ID.getName(), orderId));
			} else if (null != orderIdConditionQuery) {
				paramConditionLst.add(new BasicDBObject(FieldType.ORDER_ID.getName(), orderIdConditionQuery));
			}

			if (StringUtils.isNotEmpty(customerEmail) && !customerEmail.isEmpty()) {
				customerQuery.append(FieldType.CUST_EMAIL.getName(), customerEmail);
				if (StringUtils.isNotBlank(customerEmail)) {
					String email = customerEmail;
					String emailMask = null;
					final String mask = "*****";
					final int at = email.indexOf("@");
					if (at > 2) {
						final int maskLen = Math.min(Math.max(at / 2, 2), 5);
						final int start1 = (at - maskLen) / 2;
						emailMask = email.substring(0, start1) + mask.substring(0, maskLen)
								+ email.substring(start1 + maskLen);
					}
					customerQueryMask.append(FieldType.CUST_EMAIL.getName(), emailMask);
				}
			}

			if (StringUtils.isNotBlank(Userstatus)) {
				paramConditionLst.add(new BasicDBObject(FieldType.STATUS.getName(), Userstatus));
			}

			if (!currency.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.CURRENCY_CODE.getName(), currency));
			}
			if (!partSettleFlag.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.PART_SETTLE.getName(), partSettleFlag));
			}

			if (!transactionType.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.ORIG_TXNTYPE.getName(), transactionType));
			}

			if (!paymentRegion.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.PAYMENTS_REGION.getName(), paymentRegion));
			}

			if (StringUtils.isNotBlank(transactionFlag) && !transactionFlag.equalsIgnoreCase("ALL")) {
				txnCapturedFlag.append("$in", transactionFlag.split(","));
				paramConditionLst.add(new BasicDBObject(FieldType.TXN_CAPTURE_FLAG.getName(), txnCapturedFlag));
			}

			if (StringUtils.isNotBlank(deltaFlag) && !deltaFlag.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.DELTA_REFUND_FLAG.getName(), deltaFlag));
			}

			if (StringUtils.isNotBlank(autoRefund) && !autoRefund.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.AUTO_REFUND_FLAG.getName(), autoRefund));
			}

			if (StringUtils.isNotBlank(pgRefNum) || StringUtils.isNotBlank(orderId)) {

			} else {

				if (!dateIndexConditionQuery.isEmpty()) {
					paramConditionLst.add(dateIndexConditionQuery);
				}
			}

			List<BasicDBObject> fianlQueryList = new ArrayList<BasicDBObject>();
			fianlQueryList.addAll(paramConditionLst);

			if (!customerQuery.isEmpty()) {
				paramConditionLst.add(customerQuery);
			}

			if (!customerQueryMask.isEmpty()) {
				fianlQueryList.add(customerQueryMask);
			}

			BasicDBObject finalquery = new BasicDBObject("$and", paramConditionLst);
			BasicDBObject finalqueryMask = new BasicDBObject("$and", fianlQueryList);

			if (!customerQueryMask.isEmpty()) {
				logger.info("Inside TxnReports , transactionReport , finalQuery = " + finalqueryMask);
			} else {
				logger.info("Inside TxnReports , transactionReport , finalQuery = " + finalquery);
			}

			MongoDatabase dbIns = mongoInstance.getDB();

			List<BasicDBObject> pipeline = null;
			BasicDBObject skip = null;
			BasicDBObject limit = null;

			BasicDBObject projectElement = new BasicDBObject();
			projectElement.put(FieldType.TXN_ID.getName(), 1);
			projectElement.put(FieldType.PG_REF_NUM.getName(), 1);
			projectElement.put(FieldType.PAY_ID.getName(), 1);
			projectElement.put(FieldType.CUST_NAME.getName(), 1);
			projectElement.put(FieldType.CUST_PHONE.getName(), 1);
			projectElement.put(FieldType.CUST_EMAIL.getName(), 1);
			projectElement.put(FieldType.PAYOUT_DATE.getName(), 1);
			projectElement.put(FieldType.UTR_NO.getName(), 1);
			projectElement.put(CrmFieldType.BUSINESS_NAME.getName(), 1);
			projectElement.put(FieldType.SUB_MERCHANT_ID.getName(), 1);
			projectElement.put(FieldType.REFUND_DAYS.getName(), 1);
			projectElement.put(FieldType.PRODUCT_PRICE.getName(), 1);
			projectElement.put(FieldType.VENDOR_ID.getName(), 1);
			projectElement.put(FieldType.SKU_CODE.getName(), 1);
			projectElement.put(FieldType.CATEGORY_CODE.getName(), 1);
			projectElement.put(FieldType.TXNTYPE.getName(), 1);
			projectElement.put(FieldType.ORIG_TXNTYPE.getName(), 1);
			projectElement.put(FieldType.STATUS.getName(), 1);
			projectElement.put(FieldType.MOP_TYPE.getName(), 1);
			projectElement.put(FieldType.PAYMENT_TYPE.getName(), 1);
			projectElement.put(FieldType.ACQUIRER_TYPE.getName(), 1);
			projectElement.put(FieldType.ACQ_ID.getName(), 1);
			projectElement.put(FieldType.UDF11.getName(), 1);
			projectElement.put(FieldType.UDF12.getName(), 1);
			projectElement.put(FieldType.UDF13.getName(), 1);
			projectElement.put(FieldType.UDF14.getName(), 1);
			projectElement.put(FieldType.UDF15.getName(), 1);
			projectElement.put(FieldType.UDF16.getName(), 1);
			projectElement.put(FieldType.UDF17.getName(), 1);
			projectElement.put(FieldType.UDF18.getName(), 1);
			projectElement.put(FieldType.RRN.getName(), 1);
			projectElement.put(FieldType.TXN_CAPTURE_FLAG.getName(), 1);
			projectElement.put(FieldType.TRANSACTION_MODE.getName(), 1);
			projectElement.put(FieldType.RESPONSE_MESSAGE.getName(), 1);
			projectElement.put(FieldType.PG_TXN_MESSAGE.getName(), 1);
			projectElement.put(FieldType.CARD_MASK.getName(), 1);
			projectElement.put(FieldType.PAYER_ADDRESS.getName(), 1);
			projectElement.put(FieldType.PART_SETTLE.getName(), 1);
			projectElement.put(FieldType.CREATE_DATE.getName(), 1);
			projectElement.put(FieldType.AMOUNT.getName(), 1);
			projectElement.put(FieldType.ORDER_ID.getName(), 1);
			projectElement.put(FieldType.REFUND_ORDER_ID.getName(), 1);
			projectElement.put(FieldType.PRODUCT_DESC.getName(), 1);
			projectElement.put(FieldType.PG_DATE_TIME.getName(), 1);
			projectElement.put(FieldType.INTERNAL_CARD_ISSUER_BANK.getName(), 1);
			projectElement.put(FieldType.INTERNAL_CARD_ISSUER_COUNTRY.getName(), 1);
			projectElement.put(FieldType.REFUNDABLE_AMOUNT.getName(), 1);
			projectElement.put(FieldType.TOTAL_AMOUNT.getName(), 1);
			projectElement.put(FieldType.CURRENCY_CODE.getName(), 1);
			projectElement.put(FieldType.PAYMENTS_REGION.getName(), 1);
			projectElement.put(FieldType.CARD_HOLDER_TYPE.getName(), 1);
			projectElement.put(FieldType.RESELLER_CHARGES.getName(), 1);
			projectElement.put(FieldType.RESELLER_GST.getName(), 1);
			projectElement.put(FieldType.ACQUIRER_TDR_SC.getName(), 1);
			projectElement.put(FieldType.PG_TDR_SC.getName(), 1);
			projectElement.put(FieldType.ACQUIRER_GST.getName(), 1);
			projectElement.put(FieldType.PG_GST.getName(), 1);
			projectElement.put(FieldType.SETTLEMENT_FLAG.getName(), 1);
			projectElement.put(FieldType.SETTLEMENT_DATE.getName(), 1);
			projectElement.put(FieldType.IS_ENCRYPTED.getName(), 1);

			projectElement.put(FieldType.SUF_GST.getName(), 1);
			projectElement.put(FieldType.SUF_TDR.getName(), 1);

			BasicDBObject project = new BasicDBObject("$project", projectElement);

			MongoCollection<Document> coll = dbIns
					.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));
			BasicDBObject match = new BasicDBObject("$match", finalquery);
			BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject("CREATE_DATE", -1));

			skip = new BasicDBObject("$skip", start);
			limit = new BasicDBObject("$limit", length);
			pipeline = Arrays.asList(match, project, sort, skip, limit);
			AggregateIterable<Document> output = coll.aggregate(pipeline);
			output.allowDiskUse(true);
			MongoCursor<Document> cursor = output.iterator();

			while (cursor.hasNext()) {
				Document dbobj = cursor.next();
				if (StringUtils.isNotBlank(dbobj.getString("IS_ENCRYPTED"))
						&& dbobj.getString("IS_ENCRYPTED").equalsIgnoreCase("Y")) {
					dbobj = dataEncDecTool.decryptDocument(dbobj);
				}
				TransactionSearch transReport = new TransactionSearch();
				BigInteger txnID = new BigInteger(((Document) dbobj).getString(FieldType.TXN_ID.toString()));
				transReport.setTransactionId((txnID));
				transReport.setPgRefNum(((Document) dbobj).getString(FieldType.PG_REF_NUM.toString()));
				transReport.setPayId(((Document) dbobj).getString(FieldType.PAY_ID.toString()));

				if (null != ((Document) dbobj).getString(FieldType.CUST_NAME.toString())) {
					transReport.setCustomerName(((Document) dbobj).getString(FieldType.CUST_NAME.toString()));
				} else {
					transReport.setCustomerName(CrmFieldConstants.NA.getValue());
				}

				if (null != ((Document) dbobj).getString(FieldType.CUST_PHONE.toString())) {
					transReport.setCustomerMobile(((Document) dbobj).getString(FieldType.CUST_PHONE.toString()));
				} else {
					transReport.setCustomerMobile(CrmFieldConstants.NA.getValue());
				}

				if (null != ((Document) dbobj).getString(FieldType.CUST_EMAIL.toString())) {
					transReport.setCustomerEmail(((Document) dbobj).getString(FieldType.CUST_EMAIL.toString()));
				} else {
					transReport.setCustomerEmail(CrmFieldConstants.NA.getValue());
				}

				if (null != ((Document) dbobj).getString(FieldType.PAYOUT_DATE.toString())) {
					transReport.setPayOutDate(((Document) dbobj).getString(FieldType.PAYOUT_DATE.toString()));
				} else {
					transReport.setPayOutDate(CrmFieldConstants.NA.getValue());
				}
				if (null != ((Document) dbobj).getString(FieldType.UTR_NO.toString())) {
					transReport.setUtrNo(((Document) dbobj).getString(FieldType.UTR_NO.toString()));
				} else {
					transReport.setUtrNo(CrmFieldConstants.NA.getValue());
				}

				if (StringUtils.isNotBlank(dbobj.getString("IS_CUSTOM_HOSTED"))
						&& dbobj.getString("IS_CUSTOM_HOSTED").equalsIgnoreCase("Y")) {
					transReport.setCustomFlag(((Document) dbobj).getString("IS_CUSTOM_HOSTED"));
				} else {
					transReport.setCustomFlag("N");
				}

				transReport.setMerchants(dbobj.getString(CrmFieldType.BUSINESS_NAME.getName()));

				User user1 = new User();

				if (StringUtils.isNotBlank((String) dbobj.get(FieldType.PAY_ID.getName()))) {
					String payid = (String) dbobj.get(FieldType.PAY_ID.getName());

					if (userMap.get(payid) != null && !userMap.get(payid).getPayId().isEmpty()) {
						user1 = userMap.get(payid);
					} else {
						user1 = userdao.findPayId(payid);
						userMap.put(payid, user1);
					}
				}
				if (user1 != null) {
					transReport.setMerchants(user1.getBusinessName());
				}

				if (sessionUser.getUserType().equals(UserType.PARENTMERCHANT)) {
					transReport.setMerchants(sessionUser.getBusinessName());
				}

				if (dbobj.containsKey(FieldType.SUB_MERCHANT_ID.getName())
						&& StringUtils.isNotBlank(dbobj.getString(FieldType.SUB_MERCHANT_ID.getName()))) {

					String subMerchant = dbobj.getString(FieldType.SUB_MERCHANT_ID.getName());
					User subMerchantUser = new User();

					if (userMap.get(subMerchant) != null && !userMap.get(subMerchant).getPayId().isEmpty()) {
						subMerchantUser = userMap.get(subMerchant);
					} else {
						subMerchantUser = userdao.findPayId(subMerchant);
						userMap.put(subMerchant, subMerchantUser);
					}
					if (subMerchantUser != null) {
						transReport.setSubMerchantId(subMerchantUser.getBusinessName());
					} else {
						transReport.setSubMerchantId(CrmFieldConstants.NA.getValue());
					}
				} else {
					if ((!pgRefNum.isEmpty() || !orderId.isEmpty())
							&& dbobj.containsKey(FieldType.SUB_MERCHANT_ID.getName())
							&& StringUtils.isNotBlank(dbobj.getString(FieldType.SUB_MERCHANT_ID.getName()))) {
						transReport.setSubMerchantId(
								userdao.getBusinessNameByPayId(dbobj.getString(FieldType.SUB_MERCHANT_ID.getName())));
					}
				}

				if (merchantPayId.equalsIgnoreCase("All")) {

					if (sessionUser.getUserType().equals(UserType.RESELLER)) {

					} else {

						if (StringUtils.isNotBlank(dbobj.getString(FieldType.REFUND_DAYS.getName()))) {
							transReport.setRefundCycle(dbobj.getString(FieldType.REFUND_DAYS.getName()));
						} else {
							transReport.setRefundCycle(CrmFieldConstants.NA.getValue());
						}

						if (StringUtils.isNotBlank(dbobj.getString(FieldType.PRODUCT_PRICE.getName()))) {
							transReport.setProductPrice(dbobj.getString(FieldType.PRODUCT_PRICE.getName()));
						} else {
							transReport.setProductPrice(CrmFieldConstants.NA.getValue());
						}
						if (StringUtils.isNotBlank(dbobj.getString(FieldType.VENDOR_ID.getName()))) {
							transReport.setVendorID(dbobj.getString(FieldType.VENDOR_ID.getName()));
						} else {
							transReport.setVendorID(CrmFieldConstants.NA.getValue());
						}
						if (StringUtils.isNotBlank(dbobj.getString(FieldType.SKU_CODE.getName()))) {
							transReport.setSKUCode(dbobj.getString(FieldType.SKU_CODE.getName()));
						} else {
							transReport.setSKUCode(CrmFieldConstants.NA.getValue());
						}

						if (StringUtils.isNotBlank(dbobj.getString(FieldType.CATEGORY_CODE.getName()))) {
							transReport.setCategoryCode(dbobj.getString(FieldType.CATEGORY_CODE.getName()));
						} else {
							transReport.setCategoryCode(CrmFieldConstants.NA.getValue());
						}
					}
				} else {

					if (sessionUser.getUserType().equals(UserType.ADMIN)
							|| sessionUser.getUserType().equals(UserType.SUBADMIN)
							|| sessionUser.getUserType().equals(UserType.RESELLER)) {
						// User merhant = userMap.get(merchantPayId); // userdao.findPayId(payid);
						UserSettingData merchantSettings = userSettingDao.fetchDataUsingPayId(merchantPayId);
						if (merchantSettings.isRetailMerchantFlag()) {
							if (StringUtils.isNotBlank(dbobj.getString(FieldType.REFUND_DAYS.getName()))) {
								transReport.setRefundCycle(dbobj.getString(FieldType.REFUND_DAYS.getName()));
							} else {
								transReport.setRefundCycle(CrmFieldConstants.NA.getValue());
							}

							if (StringUtils.isNotBlank(dbobj.getString(FieldType.PRODUCT_PRICE.getName()))) {
								transReport.setProductPrice(dbobj.getString(FieldType.PRODUCT_PRICE.getName()));
							} else {
								transReport.setProductPrice(CrmFieldConstants.NA.getValue());
							}
							if (StringUtils.isNotBlank(dbobj.getString(FieldType.VENDOR_ID.getName()))) {
								transReport.setVendorID(dbobj.getString(FieldType.VENDOR_ID.getName()));
							} else {
								transReport.setVendorID(CrmFieldConstants.NA.getValue());
							}
							if (StringUtils.isNotBlank(dbobj.getString(FieldType.SKU_CODE.getName()))) {
								transReport.setSKUCode(dbobj.getString(FieldType.SKU_CODE.getName()));
							} else {
								transReport.setSKUCode(CrmFieldConstants.NA.getValue());
							}

							if (StringUtils.isNotBlank(dbobj.getString(FieldType.CATEGORY_CODE.getName()))) {
								transReport.setCategoryCode(dbobj.getString(FieldType.CATEGORY_CODE.getName()));
							} else {
								transReport.setCategoryCode(CrmFieldConstants.NA.getValue());
							}
						}
					} else {

						if (userSettings.isRetailMerchantFlag()) {
							if (StringUtils.isNotBlank(dbobj.getString(FieldType.REFUND_DAYS.getName()))) {
								transReport.setRefundCycle(dbobj.getString(FieldType.REFUND_DAYS.getName()));
							} else {
								transReport.setRefundCycle(CrmFieldConstants.NA.getValue());
							}

							if (StringUtils.isNotBlank(dbobj.getString(FieldType.PRODUCT_PRICE.getName()))) {
								transReport.setProductPrice(dbobj.getString(FieldType.PRODUCT_PRICE.getName()));
							} else {
								transReport.setProductPrice(CrmFieldConstants.NA.getValue());
							}
							if (StringUtils.isNotBlank(dbobj.getString(FieldType.VENDOR_ID.getName()))) {
								transReport.setVendorID(dbobj.getString(FieldType.VENDOR_ID.getName()));
							} else {
								transReport.setVendorID(CrmFieldConstants.NA.getValue());
							}
							if (StringUtils.isNotBlank(dbobj.getString(FieldType.SKU_CODE.getName()))) {
								transReport.setSKUCode(dbobj.getString(FieldType.SKU_CODE.getName()));
							} else {
								transReport.setSKUCode(CrmFieldConstants.NA.getValue());
							}

							if (StringUtils.isNotBlank(dbobj.getString(FieldType.CATEGORY_CODE.getName()))) {
								transReport.setCategoryCode(dbobj.getString(FieldType.CATEGORY_CODE.getName()));
							} else {
								transReport.setCategoryCode(CrmFieldConstants.NA.getValue());
							}
						}

					}

				}

				if ((StringUtils.isNotBlank(dbobj.getString(FieldType.TXNTYPE.getName()))) && (dbobj
						.getString(FieldType.TXNTYPE.toString()).equalsIgnoreCase(TransactionType.REFUND.getName()))) {
					if (StringUtils.isNotBlank(dbobj.getString(FieldType.SETTLEMENT_FLAG.getName()))
							&& dbobj.getString(FieldType.SETTLEMENT_FLAG.getName()).equals("Y")) {
						transReport.setTxnType(TransactionType.REFUND.getName());
					} else {
						transReport.setTxnType(dbobj.getString(FieldType.TXNTYPE.toString()));
					}
				} else if (StringUtils.isNotBlank(dbobj.getString(FieldType.ORIG_TXNTYPE.toString()))) {

					if (StringUtils.isNotBlank(dbobj.getString(FieldType.SETTLEMENT_FLAG.getName()))
							&& dbobj.getString(FieldType.SETTLEMENT_FLAG.getName()).equals("Y")) {
						transReport.setTxnType(TransactionType.SALE.getName());
					} else {
						transReport.setTxnType(dbobj.getString(FieldType.ORIG_TXNTYPE.toString()));
					}
				} else {

					// If ORIG_TXN_TYPE is not available incase of a timeout ,
					// set TXNTYPE instead
					// of ORIG_TXN_TYPE

					if (dbobj.getString(FieldType.STATUS.toString()).equalsIgnoreCase(StatusType.TIMEOUT.getName())) {
						transReport.setTxnType(dbobj.getString(FieldType.TXNTYPE.toString()));
					} else {
						transReport.setTxnType(CrmFieldConstants.NA.getValue());
					}
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.MOP_TYPE.toString()))) {
					transReport.setMopType(MopType.getmopName(dbobj.getString(FieldType.MOP_TYPE.toString())));
				} else {
					transReport.setMopType(CrmFieldConstants.NOT_AVAILABLE.getValue());
				}
				if (StringUtils.isNotBlank(dbobj.getString(FieldType.PAYMENT_TYPE.toString()))) {
					transReport.setPaymentMethods(
							PaymentType.getpaymentName(dbobj.getString(FieldType.PAYMENT_TYPE.toString())));
				} else {
					transReport.setPaymentMethods(CrmFieldConstants.NOT_AVAILABLE.getValue());
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TYPE.getName()))) {
					transReport.setAcquirerType(dbobj.getString(FieldType.ACQUIRER_TYPE.getName()));
				} else {
					transReport.setAcquirerType(CrmFieldConstants.NOT_AVAILABLE.getValue());
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQ_ID.getName()))) {
					transReport.setAcqId(dbobj.getString(FieldType.ACQ_ID.getName()));
				} else {
					transReport.setAcqId(CrmFieldConstants.NA.getValue());
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF11.getName()))) {
					transReport.setUDF11(dbobj.getString(FieldType.UDF11.getName()));
				} else {
					transReport.setUDF11(CrmFieldConstants.NA.getValue());
				}
				if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF12.getName()))) {
					transReport.setUDF12(dbobj.getString(FieldType.UDF12.getName()));
				} else {
					transReport.setUDF12(CrmFieldConstants.NA.getValue());
				}
				if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF13.getName()))) {
					transReport.setUDF13(dbobj.getString(FieldType.UDF13.getName()));
				} else {
					transReport.setUDF13(CrmFieldConstants.NA.getValue());
				}
				if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF14.getName()))) {
					transReport.setUDF14(dbobj.getString(FieldType.UDF14.getName()));
				} else {
					transReport.setUDF14(CrmFieldConstants.NA.getValue());
				}
				if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF15.getName()))) {
					transReport.setUDF15(dbobj.getString(FieldType.UDF15.getName()));
				} else {
					transReport.setUDF15(CrmFieldConstants.NA.getValue());
				}
				if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF16.getName()))) {
					transReport.setUDF16(dbobj.getString(FieldType.UDF16.getName()));
				} else {
					transReport.setUDF16(CrmFieldConstants.NA.getValue());
				}
				if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF17.getName()))) {
					transReport.setUDF17(dbobj.getString(FieldType.UDF17.getName()));
				} else {
					transReport.setUDF17(CrmFieldConstants.NA.getValue());
				}
				if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF18.getName()))) {
					transReport.setUDF18(dbobj.getString(FieldType.UDF18.getName()));
				} else {
					transReport.setUDF18(CrmFieldConstants.NA.getValue());
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.RRN.getName()))
						&& !dbobj.getString(FieldType.RRN.getName()).equalsIgnoreCase("-")) {
					transReport.setRrn(dbobj.getString(FieldType.RRN.getName()));
				} else {
					transReport.setRrn(CrmFieldConstants.NA.getValue());
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.TXN_CAPTURE_FLAG.getName()))) {
					transReport.setTxnSettledType(dbobj.getString(FieldType.TXN_CAPTURE_FLAG.getName()));
				} else {
					transReport.setTxnSettledType(CrmFieldConstants.NA.getValue());
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.TRANSACTION_MODE.getName()))) {
					transReport.setTransactionMode(dbobj.getString(FieldType.TRANSACTION_MODE.getName()));
				} else {
				}

				transReport.setResponseMessage(dbobj.getString(CrmFieldType.RESPONSE_MESSAGE.toString()));
				if (!StringUtils.isEmpty(dbobj.getString(CrmFieldType.PG_TXN_MESSAGE.toString()))) {
					transReport.setAccqResponseMessage(dbobj.getString(CrmFieldType.PG_TXN_MESSAGE.toString()));
				} else {
					transReport.setAccqResponseMessage(CrmFieldConstants.NA.getValue());
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.PAYMENT_TYPE.toString()))) {
					if (null != dbobj.getString(FieldType.CARD_MASK.toString())) {
						transReport.setCardNumber(dbobj.getString(FieldType.CARD_MASK.toString()));
					} else if (null != dbobj.getString(FieldType.PAYER_ADDRESS.getName())) {

						if (sessionUser.getUserType().equals(UserType.ADMIN)
								|| sessionUser.getUserType().equals(UserType.SUBADMIN)) {
							transReport.setCardNumber(dbobj.getString(FieldType.PAYER_ADDRESS.getName()));
						} else {
							String vpaString = dbobj.getString(FieldType.PAYER_ADDRESS.getName());
							String[] vpaArray = vpaString.split("@");
							char[] vpaChar = vpaArray[0].toCharArray();
							StringBuilder vpastrBuilder = new StringBuilder();

							if (vpaChar.length > 3) {
								for (int i = 0; i < vpaChar.length - 3; i++) {
									vpastrBuilder.append(vpaChar[i]);
								}
								vpastrBuilder.append("***@");
								vpastrBuilder.append(vpaArray[1]);
							} else {
								vpastrBuilder.append(vpaChar[0]);
								vpastrBuilder.append("**@");
								vpastrBuilder.append(vpaArray[1]);
							}

							transReport.setCardNumber(vpastrBuilder.toString());
						}
					} else {
						transReport.setCardNumber(CrmFieldConstants.NA.getValue());
					}
				} else {
					transReport.setCardNumber(CrmFieldConstants.NA.getValue());
				}
				if (StringUtils.isNotBlank(dbobj.getString(FieldType.PART_SETTLE.toString()))
						&& dbobj.getString(FieldType.PART_SETTLE.toString()).equalsIgnoreCase(("Y"))) {
					transReport.setPartSettle(dbobj.getString(FieldType.PART_SETTLE.toString()));
				} else {
					transReport.setPartSettle(CrmFieldConstants.NA.getValue());
				}

				if (((sessionUser.getUserType().equals(UserType.MERCHANT))
						|| (sessionUser.getUserType().equals(UserType.SUBUSER)))
						&& userSettings.isCustomTransactionStatus()) {
					if (((dbobj.getString(FieldType.STATUS.toString())).equalsIgnoreCase(StatusType.TIMEOUT.getName()))
							|| ((dbobj.getString(FieldType.STATUS.toString()))
									.equalsIgnoreCase(StatusType.CAPTURED.getName()))
							|| ((dbobj.getString(FieldType.STATUS.toString()))
									.equalsIgnoreCase(StatusType.ENROLLED.getName()))
							|| ((dbobj.getString(FieldType.STATUS.toString()))
									.equalsIgnoreCase(StatusType.SETTLED.getName()))
							|| ((dbobj.getString(FieldType.STATUS.toString()))
									.equalsIgnoreCase(StatusType.SENT_TO_BANK.getName()))
							|| ((dbobj.getString(FieldType.STATUS.toString()))
									.equalsIgnoreCase(StatusType.PENDING.getName()))) {

						if (StringUtils.isNotBlank(dbobj.getString(FieldType.SETTLEMENT_FLAG.getName()))
								&& dbobj.getString(FieldType.SETTLEMENT_FLAG.getName()).equals("Y")) {
							transReport.setStatus(StatusType.SETTLED.getName());
						} else {
							transReport.setStatus(dbobj.getString(FieldType.STATUS.toString()));
						}
						// transReport.setStatus(dbobj.getString(FieldType.STATUS.toString()));
					} else {
						transReport.setStatus("Failed");
					}
				} else {
					if (StringUtils.isNotBlank(dbobj.getString(FieldType.SETTLEMENT_FLAG.getName()))
							&& dbobj.getString(FieldType.SETTLEMENT_FLAG.getName()).equals("Y")) {
						transReport.setStatus(StatusType.SETTLED.getName());
					} else {
						transReport.setStatus(dbobj.getString(FieldType.STATUS.toString()));
					}
					// transReport.setStatus(dbobj.getString(FieldType.STATUS.toString()));
				}
				if (StringUtils.isNotBlank(dbobj.getString(FieldType.SETTLEMENT_FLAG.getName()))
						&& dbobj.getString(FieldType.SETTLEMENT_FLAG.getName()).equals("Y")) {
					transReport.setDateFrom(dbobj.getString(FieldType.SETTLEMENT_DATE.getName()));
				} else {
					transReport.setDateFrom(dbobj.getString(FieldType.CREATE_DATE.getName()));
				}
				// transReport.setDateFrom(dbobj.getString(FieldType.CREATE_DATE.getName()));
				transReport.setAmount(dbobj.getString(FieldType.AMOUNT.toString()));
				transReport.setOrderId(dbobj.getString(FieldType.ORDER_ID.toString()));

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.REFUND_ORDER_ID.toString()))) {
					transReport.setRefundOrderId(dbobj.getString(FieldType.REFUND_ORDER_ID.toString()));
				} else {
					transReport.setRefundOrderId(CrmFieldConstants.NA.getValue());
				}

				transReport.setoId(dbobj.getString(FieldType.OID.toString()));

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.PRODUCT_DESC.toString()))) {
					transReport.setProductDesc(dbobj.getString(FieldType.PRODUCT_DESC.toString()));
				} else {
					transReport.setProductDesc(CrmFieldConstants.NA.getValue());
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.PG_DATE_TIME.getName()))) {
					transReport.setTransactionCaptureDate(dbobj.getString(FieldType.PG_DATE_TIME.toString()));
				} else {
					transReport.setTransactionCaptureDate(CrmFieldConstants.NA.getValue());
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.INTERNAL_CARD_ISSUER_BANK.getName()))) {
					transReport.setInternalCardIssusserBank(
							dbobj.getString(FieldType.INTERNAL_CARD_ISSUER_BANK.toString()));
				} else {
					transReport.setInternalCardIssusserBank(CrmFieldConstants.NA.getValue());
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.INTERNAL_CARD_ISSUER_COUNTRY.getName()))) {
					transReport.setInternalCardIssusserCountry(
							dbobj.getString(FieldType.INTERNAL_CARD_ISSUER_COUNTRY.toString()));
				} else {
					transReport.setInternalCardIssusserCountry(CrmFieldConstants.NA.getValue());
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.REFUNDABLE_AMOUNT.getName()))) {
					transReport.setRefundableAmount(dbobj.getString(FieldType.REFUNDABLE_AMOUNT.toString()));
				} else {
					transReport.setRefundableAmount(CrmFieldConstants.NA.getValue());
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.AMOUNT.getName()))) {
					transReport.setApprovedAmount(dbobj.getString(FieldType.AMOUNT.toString()));
				} else {
					transReport.setApprovedAmount(CrmFieldConstants.NA.getValue());
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))) {
					transReport.setTotalAmount(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()));
				} else {
					transReport.setTotalAmount("");
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.CURRENCY_CODE.toString()))) {
					transReport.setCurrency(
							propManager.getAlphabaticCurrencyCode(dbobj.getString(FieldType.CURRENCY_CODE.toString())));
				} else {
					transReport.setCurrency(CrmFieldConstants.NA.getValue());
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.PAYMENTS_REGION.toString()))) {
					transReport.setPaymentRegion(dbobj.getString(FieldType.PAYMENTS_REGION.toString()));

				} else {
					transReport.setPaymentRegion(CrmFieldConstants.NA.getValue());
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.CARD_HOLDER_TYPE.toString()))) {
					transReport.setCardHolderType(dbobj.getString(FieldType.CARD_HOLDER_TYPE.toString()));

				} else {
					transReport.setCardHolderType(CrmFieldConstants.NA.getValue());
				}
				// changes regarding partner reseller
				if (sessionUser.getUserType().equals(UserType.RESELLER) && sessionUser.isPartnerFlag()) {

					if (StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_CHARGES.toString()))) {

						transReport.setResellerCharges(String.format("%.2f",
								(Double.parseDouble(dbobj.getString(FieldType.RESELLER_CHARGES.toString())))));

					} else {
						transReport.setResellerCharges("0.00");
					}

					if (StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_GST.toString()))) {
						transReport.setResellerGST(String.format("%.2f",
								(Double.parseDouble(dbobj.getString(FieldType.RESELLER_GST.toString())))));

					} else {
						transReport.setResellerGST("0.00");
					}

					if (StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_CHARGES.toString()))) {
						if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
								&& (StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.toString())))) {

							transReport.setTdr_Surcharge(String.format("%.2f",
									(Double.parseDouble(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
											+ Double.parseDouble(dbobj.getString(FieldType.PG_TDR_SC.toString()))
											+ Double.parseDouble(
													dbobj.getString(FieldType.RESELLER_CHARGES.toString())))));

						} else {
							transReport.setTdr_Surcharge("0.00");
						}
					} else {
						if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
								&& (StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.toString())))) {

							transReport.setTdr_Surcharge(String.format("%.2f",
									(Double.parseDouble(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
											+ Double.parseDouble(dbobj.getString(FieldType.PG_TDR_SC.toString())))));

						} else {
							transReport.setTdr_Surcharge("0.00");
						}
					}

					if (StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_GST.toString()))) {
						if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.toString()))
								&& (StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.toString())))) {
							transReport.setGst_charge(String.format("%.2f",
									(Double.parseDouble(dbobj.getString(FieldType.ACQUIRER_GST.toString()))
											+ Double.parseDouble(dbobj.getString(FieldType.PG_GST.toString()))
											+ Double.parseDouble(dbobj.getString(FieldType.RESELLER_GST.toString())))));

						} else {
							transReport.setGst_charge("0.00");
						}
					} else {
						if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.toString()))
								&& (StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.toString())))) {
							transReport.setGst_charge(String.format("%.2f",
									(Double.parseDouble(dbobj.getString(FieldType.ACQUIRER_GST.toString()))
											+ Double.parseDouble(dbobj.getString(FieldType.PG_GST.toString())))));

						} else {
							transReport.setGst_charge("0.00");
						}
					}

				} // END

				else {

					if (StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_CHARGES.toString()))) {
						if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
								&& (StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.toString())))) {

							transReport.setTdr_Surcharge(String.format("%.2f",
									(Double.parseDouble(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
											+ Double.parseDouble(dbobj.getString(FieldType.PG_TDR_SC.toString()))
											+ Double.parseDouble(
													dbobj.getString(FieldType.RESELLER_CHARGES.toString())))));

						} else {
							transReport.setTdr_Surcharge("0.00");
						}
					} else {
						if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
								&& (StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.toString())))) {

							transReport.setTdr_Surcharge(String.format("%.2f",
									(Double.parseDouble(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
											+ Double.parseDouble(dbobj.getString(FieldType.PG_TDR_SC.toString())))));

						} else {
							transReport.setTdr_Surcharge("0.00");
						}
					}

					if (StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_GST.toString()))) {
						if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.toString()))
								&& (StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.toString())))) {
							transReport.setGst_charge(String.format("%.2f",
									(Double.parseDouble(dbobj.getString(FieldType.ACQUIRER_GST.toString()))
											+ Double.parseDouble(dbobj.getString(FieldType.PG_GST.toString()))
											+ Double.parseDouble(dbobj.getString(FieldType.RESELLER_GST.toString())))));

						} else {
							transReport.setGst_charge("0.00");
						}
					} else {
						if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.toString()))
								&& (StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.toString())))) {
							transReport.setGst_charge(String.format("%.2f",
									(Double.parseDouble(dbobj.getString(FieldType.ACQUIRER_GST.toString()))
											+ Double.parseDouble(dbobj.getString(FieldType.PG_GST.toString())))));

						} else {
							transReport.setGst_charge("0.00");
						}
					}

				}

				if (TxnType.SALE.getName().equalsIgnoreCase(dbobj.getString(FieldType.ORIG_TXNTYPE.getName()))) {

					if (StringUtils.isNotBlank(dbobj.getString(FieldType.PAYMENT_TYPE.toString()))) {

						if (dbobj.getString(FieldType.PAYMENT_TYPE.getName())
								.equalsIgnoreCase(PaymentType.COD.getCode())) {

							if (isGlocal == true) {
								transReport.setGlocalFlag(true);
								if (dbobj.containsKey(FieldType.UDF7.getName())) {

									if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF7.getName()))) {

										if (StringUtils.isNotBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
												&& StringUtils
														.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.toString()))
												&& StringUtils.isNotBlank(
														dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
												&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.toString()))
												&& StringUtils.isNotBlank(
														dbobj.getString(FieldType.ACQUIRER_GST.toString()))) {

											String merchantAmount = String.format("%.2f", Double
													.parseDouble(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
													- (Double.parseDouble(
															dbobj.getString(FieldType.PG_TDR_SC.toString()))
															+ Double.parseDouble(dbobj
																	.getString(FieldType.ACQUIRER_TDR_SC.toString()))
															+ Double.parseDouble(
																	dbobj.getString(FieldType.PG_GST.toString()))
															+ Double.parseDouble(dbobj
																	.getString(FieldType.ACQUIRER_GST.toString()))));

											transReport.setDoctor(String
													.valueOf(new BigDecimal(dbobj.getString(FieldType.UDF7.getName()))
															.multiply(new BigDecimal(merchantAmount))
															.divide(new BigDecimal(
																	Constants.MAX_NUMBER_OF_KEYS.getValue()))
															.setScale(2, BigDecimal.ROUND_HALF_UP)));
										} else {
											transReport.setDoctor("0");
										}
									} else {
										transReport.setDoctor("0");
									}
								} else {
									transReport.setDoctor("0");
								}

								if (dbobj.containsKey(FieldType.UDF8.getName())) {
									if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF8.getName()))) {

										if (StringUtils.isNotBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
												&& StringUtils
														.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.toString()))
												&& StringUtils.isNotBlank(
														dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
												&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.toString()))
												&& StringUtils.isNotBlank(
														dbobj.getString(FieldType.ACQUIRER_GST.toString()))) {

											String merchantAmount = String.format("%.2f", Double
													.parseDouble(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
													- (Double.parseDouble(
															dbobj.getString(FieldType.PG_TDR_SC.toString()))
															+ Double.parseDouble(dbobj
																	.getString(FieldType.ACQUIRER_TDR_SC.toString()))
															+ Double.parseDouble(
																	dbobj.getString(FieldType.PG_GST.toString()))
															+ Double.parseDouble(dbobj
																	.getString(FieldType.ACQUIRER_GST.toString()))));

											transReport.setGlocal(String
													.valueOf(new BigDecimal(dbobj.getString(FieldType.UDF8.getName()))
															.multiply(new BigDecimal(merchantAmount))
															.divide(new BigDecimal(
																	Constants.MAX_NUMBER_OF_KEYS.getValue()))
															.setScale(2, BigDecimal.ROUND_HALF_UP)));
										} else {
											transReport.setGlocal("0");
										}
									} else {
										transReport.setGlocal("0");
									}
								} else {
									transReport.setGlocal("0");
								}

								if (dbobj.containsKey(FieldType.UDF9.getName())) {
									if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF9.getName()))) {

										if (StringUtils.isNotBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
												&& StringUtils
														.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.toString()))
												&& StringUtils.isNotBlank(
														dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
												&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.toString()))
												&& StringUtils.isNotBlank(
														dbobj.getString(FieldType.ACQUIRER_GST.toString()))) {

											String merchantAmount = String.format("%.2f", Double
													.parseDouble(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
													- (Double.parseDouble(
															dbobj.getString(FieldType.PG_TDR_SC.toString()))
															+ Double.parseDouble(dbobj
																	.getString(FieldType.ACQUIRER_TDR_SC.toString()))
															+ Double.parseDouble(
																	dbobj.getString(FieldType.PG_GST.toString()))
															+ Double.parseDouble(dbobj
																	.getString(FieldType.ACQUIRER_GST.toString()))));

											transReport.setPartner(String
													.valueOf(new BigDecimal(dbobj.getString(FieldType.UDF9.getName()))
															.multiply(new BigDecimal(merchantAmount))
															.divide(new BigDecimal(
																	Constants.MAX_NUMBER_OF_KEYS.getValue()))
															.setScale(2, BigDecimal.ROUND_HALF_UP)));
										} else {
											transReport.setPartner("0");
										}
									} else {
										transReport.setPartner("0");
									}
								} else {
									transReport.setPartner("0");
								}

								if (dbobj.containsKey(FieldType.UDF11.getName())) {
									if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF11.getName()))) {
										transReport.setUniqueId(dbobj.getString(FieldType.UDF11.getName()));
									} else {
										transReport.setUniqueId("NA");
									}
								} else {
									transReport.setUniqueId("NA");
								}
							} else {
								transReport.setGlocalFlag(false);
							}

							if (StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_CHARGES.toString()))
									&& StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_GST.toString()))) {
								if (StringUtils.isNotBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
										&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.toString()))
										&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
										&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.toString()))
										&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.toString()))) {
									transReport.setTotalAmtPayable(String.format("%.2f", (Double
											.parseDouble(dbobj.getString(FieldType.PG_TDR_SC.toString()))
											+ Double.parseDouble(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
											+ Double.parseDouble(dbobj.getString(FieldType.PG_GST.toString()))
											+ Double.parseDouble(dbobj.getString(FieldType.ACQUIRER_GST.toString()))
											+ Double.parseDouble(dbobj.getString(FieldType.RESELLER_CHARGES.toString()))
											+ Double.parseDouble(dbobj.getString(FieldType.RESELLER_GST.toString())))));

									if (transReport.getTotalAmtPayable().equalsIgnoreCase("0.00")) {
										transReport.setTotalAmtPayable(transReport.getTotalAmtPayable());
									} else {
										transReport.setTotalAmtPayable("-" + transReport.getTotalAmtPayable());
									}
								} else {
									transReport.setTotalAmtPayable("NA");
								}

							} else {

								if (StringUtils.isNotBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
										&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.toString()))
										&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
										&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.toString()))
										&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.toString()))) {
									transReport.setTotalAmtPayable(String.format("%.2f", (Double
											.parseDouble(dbobj.getString(FieldType.PG_TDR_SC.toString()))
											+ Double.parseDouble(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
											+ Double.parseDouble(dbobj.getString(FieldType.PG_GST.toString()))
											+ Double.parseDouble(dbobj.getString(FieldType.ACQUIRER_GST.toString())))));
									if (transReport.getTotalAmtPayable().equalsIgnoreCase("0.00")) {
										transReport.setTotalAmtPayable(transReport.getTotalAmtPayable());
									} else {
										transReport.setTotalAmtPayable("-" + transReport.getTotalAmtPayable());
									}

								} else {
									transReport.setTotalAmtPayable("NA");
								}
							}

						} else if (isGlocal == true) {

							transReport.setGlocalFlag(true);

							if (dbobj.containsKey(FieldType.UDF7.getName())) {

								if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF7.getName()))) {

									if (StringUtils.isNotBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
											&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.toString()))
											&& StringUtils
													.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
											&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.toString()))
											&& StringUtils
													.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.toString()))) {

										String merchantAmount = String.format("%.2f", Double
												.parseDouble(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
												- (Double.parseDouble(dbobj.getString(FieldType.PG_TDR_SC.toString()))
														+ Double.parseDouble(
																dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
														+ Double.parseDouble(
																dbobj.getString(FieldType.PG_GST.toString()))
														+ Double.parseDouble(
																dbobj.getString(FieldType.ACQUIRER_GST.toString()))));

										transReport.setDoctor(
												String.valueOf(new BigDecimal(dbobj.getString(FieldType.UDF7.getName()))
														.multiply(new BigDecimal(merchantAmount))
														.divide(new BigDecimal(Constants.MAX_NUMBER_OF_KEYS.getValue()))
														.setScale(2, BigDecimal.ROUND_HALF_UP)));
									} else {
										transReport.setDoctor("0");
									}
								} else {
									transReport.setDoctor("0");
								}
							} else {
								transReport.setDoctor("0");
							}

							if (dbobj.containsKey(FieldType.UDF8.getName())) {
								if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF8.getName()))) {

									if (StringUtils.isNotBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
											&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.toString()))
											&& StringUtils
													.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
											&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.toString()))
											&& StringUtils
													.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.toString()))) {

										String merchantAmount = String.format("%.2f", Double
												.parseDouble(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
												- (Double.parseDouble(dbobj.getString(FieldType.PG_TDR_SC.toString()))
														+ Double.parseDouble(
																dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
														+ Double.parseDouble(
																dbobj.getString(FieldType.PG_GST.toString()))
														+ Double.parseDouble(
																dbobj.getString(FieldType.ACQUIRER_GST.toString()))));

										transReport.setGlocal(
												String.valueOf(new BigDecimal(dbobj.getString(FieldType.UDF8.getName()))
														.multiply(new BigDecimal(merchantAmount))
														.divide(new BigDecimal(Constants.MAX_NUMBER_OF_KEYS.getValue()))
														.setScale(2, BigDecimal.ROUND_HALF_UP)));
									} else {
										transReport.setGlocal("0");
									}
								} else {
									transReport.setGlocal("0");
								}
							} else {
								transReport.setGlocal("0");
							}

							if (dbobj.containsKey(FieldType.UDF9.getName())) {
								if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF9.getName()))) {

									if (StringUtils.isNotBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
											&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.toString()))
											&& StringUtils
													.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
											&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.toString()))
											&& StringUtils
													.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.toString()))) {

										String merchantAmount = String.format("%.2f", Double
												.parseDouble(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
												- (Double.parseDouble(dbobj.getString(FieldType.PG_TDR_SC.toString()))
														+ Double.parseDouble(
																dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
														+ Double.parseDouble(
																dbobj.getString(FieldType.PG_GST.toString()))
														+ Double.parseDouble(
																dbobj.getString(FieldType.ACQUIRER_GST.toString()))));

										transReport.setPartner(
												String.valueOf(new BigDecimal(dbobj.getString(FieldType.UDF9.getName()))
														.multiply(new BigDecimal(merchantAmount))
														.divide(new BigDecimal(Constants.MAX_NUMBER_OF_KEYS.getValue()))
														.setScale(2, BigDecimal.ROUND_HALF_UP)));
									} else {
										transReport.setPartner("0");
									}
								} else {
									transReport.setPartner("0");
								}
							} else {
								transReport.setPartner("0");
							}

							if (dbobj.containsKey(FieldType.UDF11.getName())) {
								if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF11.getName()))) {
									transReport.setUniqueId(dbobj.getString(FieldType.UDF11.getName()));
								} else {
									transReport.setUniqueId("NA");
								}
							} else {
								transReport.setUniqueId("NA");
							}
						} else {
							transReport.setGlocalFlag(false);
						}

						if (!dbobj.getString(FieldType.PAYMENT_TYPE.getName())
								.equalsIgnoreCase(PaymentType.COD.getCode())) {

							if (StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_CHARGES.toString()))
									&& StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_GST.toString()))) {

								if (StringUtils.isNotBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
										&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
										&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.toString()))) {
									transReport.setTotalAmtPayable(String.format("%.2f", Double
											.parseDouble(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
											- (Double.parseDouble(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
													+ Double.parseDouble(
															dbobj.getString(FieldType.ACQUIRER_GST.toString()))
													+ Double.parseDouble(
															dbobj.getString(FieldType.PG_TDR_SC.toString()))
													+ Double.parseDouble(dbobj.getString(FieldType.PG_GST.toString()))
													+ Double.parseDouble(
															dbobj.getString(FieldType.RESELLER_CHARGES.toString()))
													+ Double.parseDouble(
															dbobj.getString(FieldType.RESELLER_GST.toString())))));
								} else {
									if (StringUtils.isNotBlank(dbobj.getString(FieldType.PAYMENT_TYPE.toString()))
											&& (dbobj.getString(FieldType.PAYMENT_TYPE.toString())
													.equalsIgnoreCase("RTGS")
													|| dbobj.getString(FieldType.PAYMENT_TYPE.toString())
															.equalsIgnoreCase("NEFT")
													|| dbobj.getString(FieldType.PAYMENT_TYPE.toString())
															.equalsIgnoreCase("IMPS"))) {

										transReport
												.setTotalAmtPayable(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()));
									} else {
										transReport.setTotalAmtPayable("NA");
									}
								}

							} else {

								if (StringUtils.isNotBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
										&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
										&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.toString()))) {
									transReport.setTotalAmtPayable(String.format("%.2f", Double
											.parseDouble(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
											- (Double.parseDouble(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
													+ Double.parseDouble(
															dbobj.getString(FieldType.ACQUIRER_GST.toString()))
													+ Double.parseDouble(
															dbobj.getString(FieldType.PG_TDR_SC.toString()))
													+ Double.parseDouble(
															dbobj.getString(FieldType.PG_GST.toString())))));
								} else {
									if (StringUtils.isNotBlank(dbobj.getString(FieldType.PAYMENT_TYPE.toString()))
											&& (dbobj.getString(FieldType.PAYMENT_TYPE.toString())
													.equalsIgnoreCase("RTGS")
													|| dbobj.getString(FieldType.PAYMENT_TYPE.toString())
															.equalsIgnoreCase("NEFT")
													|| dbobj.getString(FieldType.PAYMENT_TYPE.toString())
															.equalsIgnoreCase("IMPS"))) {

										transReport
												.setTotalAmtPayable(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()));
									} else {
										transReport.setTotalAmtPayable("NA");
									}
								}
							}
						}

					} else {
						transReport.setTotalAmtPayable("NA");
					}

				} else if (TxnType.REFUND.getName()
						.equalsIgnoreCase(dbobj.getString(FieldType.ORIG_TXNTYPE.getName()))) {
					if (isGlocal == true) {
						transReport.setGlocalFlag(true);
					} else {
						transReport.setGlocalFlag(false);
					}
					if (StringUtils.isNotBlank(dbobj.getString(FieldType.PAYMENT_TYPE.getName()))) {

						if (dbobj.getString(FieldType.PAYMENT_TYPE.getName())
								.equalsIgnoreCase(PaymentType.COD.getCode())) {
							transReport.setGst_charge("0.00");
							transReport.setTdr_Surcharge("0.00");
							transReport.setTotalAmtPayable("0.00");
						} else {
							if (StringUtils.isNotBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))) {
								transReport.setTotalAmtPayable(String.format("%.2f",
										Double.parseDouble(dbobj.getString(FieldType.TOTAL_AMOUNT.toString())) * -1));
							} else {
								transReport.setTotalAmtPayable("NA");
							}
						}

					} else {
						transReport.setTotalAmtPayable("NA");
					}

				} else {
					transReport.setTotalAmtPayable("NA");
				}

				/// add by vishal

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.SUF_GST.toString()))) {
					logger.error("null");
				} else {
					logger.error("null not");
				}

				String sufGst = dbobj.getString(FieldType.SUF_GST.toString());
//				if (StringUtils.isNotBlank(dbobj.getString(FieldType.SUF_GST.toString())) &&StringUtils.isNotBlank(transReport.getTotalAmtPayable()) &&transReport.getTotalAmtPayable()!="NA" ) {
				if (sufGst != null && !sufGst.equalsIgnoreCase("false") && transReport.getTotalAmtPayable() != null
						&& transReport.getTotalAmtPayable() != "NA") {
					transReport.setSufGst(dbobj.getString(FieldType.SUF_GST.toString()));
					transReport.setTotalAmtPayable(String.valueOf(new BigDecimal(transReport.getTotalAmtPayable())
							.subtract(new BigDecimal(dbobj.getString(FieldType.SUF_GST.toString())))));

				} else {
					transReport.setSufGst(CrmFieldConstants.NA.getValue());
				}
				String sufTdr = dbobj.getString(FieldType.SUF_TDR.toString());
				// if (StringUtils.isNotBlank(dbobj.getString(FieldType.SUF_TDR.toString()))
				// &&StringUtils.isNotBlank(transReport.getTotalAmtPayable())
				// &&transReport.getTotalAmtPayable()!="NA" ) {
				if (sufTdr != null && !sufTdr.equalsIgnoreCase("false") && transReport.getTotalAmtPayable() != null
						&& transReport.getTotalAmtPayable() != "NA") {
					transReport.setSufTdr(dbobj.getString(FieldType.SUF_TDR.toString()));
					transReport.setTotalAmtPayable(String.valueOf(new BigDecimal(transReport.getTotalAmtPayable())
							.subtract(new BigDecimal(dbobj.getString(FieldType.SUF_TDR.toString())))));
				} else {
					transReport.setSufTdr(CrmFieldConstants.NA.getValue());
				}

				// Exclude certain records when loading search payment without
				// any filters

				Comparator<TransactionSearch> comp = (TransactionSearch a, TransactionSearch b) -> {

					if (a.getDateFrom().compareTo(b.getDateFrom()) > 0) {
						return -1;
					} else if (a.getDateFrom().compareTo(b.getDateFrom()) < 0) {
						return 1;
					} else {
						return 0;
					}
				};

				transactionList.add(transReport);
				Collections.sort(transactionList, comp);
			}
			cursor.close();
			logger.info("Inside TxnReports , transactionReport , transactionListSize = " + transactionList.size());
			return transactionList;
		}

		catch (Exception e) {
			logger.error("Exception occured in TxnReports , transactionReport , Exception = ", e);
			e.printStackTrace();
			return null;
		}
	}

	@SuppressWarnings("static-access")
	public List<TransactionSearchDownloadObject> searchTransactionForDownload(String merchantPayId,
			String subMerchantPayId, String paymentType, String status, String currency, String transactionType,
			String fromDate, String toDate, User user, String paymentsRegion, String acquirer, Boolean searchFlag,
			Set<String> orderIdSet) {
		logger.info("Inside TxnReports , searchPaymentDownload");

		Map<String, User> userMap = new HashMap<String, User>();
		Map<String, String> merchNameMap = new HashMap<String, String>();

		List<TransactionSearchDownloadObject> transactionList = new ArrayList<TransactionSearchDownloadObject>();
		try {

			UserSettingData merchantSettings = userSettingDao.fetchDataUsingPayId(user.getPayId());

			List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();
			List<String> acquirerConditionLst = new ArrayList<String>();
			List<String> statusConditionList = new ArrayList<String>();
			BasicDBObject acquirerQuery = new BasicDBObject();
			BasicDBObject statusQuery = new BasicDBObject();

			List<BasicDBObject> payIdConditionList = new ArrayList<BasicDBObject>();
			List<String> payIdList = new ArrayList<String>();
			BasicDBObject payIdQuery = new BasicDBObject();
			BasicDBObject payIdOrQuery = new BasicDBObject();

			BasicDBObject dateIndexConditionQuery = new BasicDBObject();

			DateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.ENGLISH);
			Date dateStart = format.parse(fromDate);
			Date dateEnd = format.parse(toDate);

			LocalDate startDate = dateStart.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			LocalDate endDate = dateEnd.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

			String fromDateIndex = startDate.toString().replaceAll("-", "");
			String toDateIndex = endDate.toString().replaceAll("-", "");

			dateIndexConditionQuery.put("DATE_INDEX",
					BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(fromDateIndex).toLocalizedPattern())
							.add("$lte", new SimpleDateFormat(toDateIndex).toLocalizedPattern()).get());

			BasicDBObject orderIdConditionQuery = null;
			if (orderIdSet != null) {
				List<String> orderIdList = new ArrayList<>(orderIdSet);
				orderIdConditionQuery = new BasicDBObject("$in", orderIdList);
			}
			if (null != orderIdConditionQuery) {
				paramConditionLst.add(new BasicDBObject(FieldType.ORDER_ID.getName(), orderIdConditionQuery));
			}

			if (user.getUserType().equals(UserType.PARENTMERCHANT)) {
				paramConditionLst.add(new BasicDBObject(FieldType.PARENT_PAY_ID.getName(), user.getPayId()));

			} else if (StringUtils.isNotBlank(merchantPayId) && !merchantPayId.equalsIgnoreCase("ALL")) {
				String payIdArr[] = merchantPayId.split(",");
				for (String payId : payIdArr) {
					payIdList.add(payId.trim());
				}
				payIdQuery.append("$in", payIdList);
				payIdConditionList.add(new BasicDBObject(FieldType.PAY_ID.getName(), payIdQuery));
				payIdConditionList.add(new BasicDBObject(FieldType.SUPER_MERCHANT_ID.getName(), payIdQuery));

				payIdOrQuery = new BasicDBObject("$or", payIdConditionList);
				paramConditionLst.add(payIdOrQuery);
			}

			if (StringUtils.isNotBlank(subMerchantPayId) && !subMerchantPayId.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.SUB_MERCHANT_ID.getName(), subMerchantPayId));
			}

			if (StringUtils.isNotBlank(status) && !status.equalsIgnoreCase("ALL")) {
				String statusArr[] = status.split(",");
				for (String sts : statusArr) {
					statusConditionList.add(sts.trim());
				}
				statusQuery.append("$in", statusConditionList);
				paramConditionLst.add(new BasicDBObject(FieldType.STATUS.getName(), statusQuery));
			}

			if (StringUtils.isNotBlank(currency) && !currency.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.CURRENCY_CODE.getName(), currency));
			}

			if (StringUtils.isNotBlank(transactionType) && !transactionType.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.ORIG_TXNTYPE.getName(), transactionType));
			}
			if (StringUtils.isNotBlank(paymentType) && !paymentType.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.PAYMENT_TYPE.getName(), paymentType));
			}

			if (StringUtils.isNotBlank(paymentsRegion) && !paymentsRegion.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.PAYMENTS_REGION.getName(), paymentsRegion));
			}

			if (user.getUserType().equals(UserType.RESELLER)) {
				paramConditionLst.add(new BasicDBObject(FieldType.RESELLER_ID.getName(), user.getResellerId()));
			}

			if (StringUtils.isNotBlank(acquirer) && !acquirer.equalsIgnoreCase("ALL")) {
				List<String> acquirerList = Arrays.asList(acquirer.split(","));
				for (String acq : acquirerList) {
					acquirerConditionLst.add(acq.trim());
				}
				acquirerQuery.append("$in", acquirerConditionLst);
				paramConditionLst.add(new BasicDBObject(FieldType.ACQUIRER_TYPE.getName(), acquirerQuery));
			}

			if (!dateIndexConditionQuery.isEmpty()) {
				paramConditionLst.add(dateIndexConditionQuery);
			}

			BasicDBObject finalquery = new BasicDBObject("$and", paramConditionLst);

			logger.info("Inside TxnReports , searchPaymentDownload , finalquery = " + finalquery);

			MongoDatabase dbIns = mongoInstance.getDB();

			BasicDBObject projectElement = new BasicDBObject();
			projectElement.put(FieldType.TXN_ID.getName(), 1);
			projectElement.put(FieldType.PG_REF_NUM.getName(), 1);
			projectElement.put(FieldType.PAY_ID.getName(), 1);
			projectElement.put(FieldType.CUST_NAME.getName(), 1);
			projectElement.put(FieldType.CUST_PHONE.getName(), 1);
			projectElement.put(FieldType.CUST_EMAIL.getName(), 1);
			projectElement.put(FieldType.PAYOUT_DATE.getName(), 1);
			projectElement.put(FieldType.UTR_NO.getName(), 1);
			projectElement.put(CrmFieldType.BUSINESS_NAME.getName(), 1);
			projectElement.put(FieldType.SUB_MERCHANT_ID.getName(), 1);
			projectElement.put(FieldType.REFUND_DAYS.getName(), 1);
			projectElement.put(FieldType.PRODUCT_PRICE.getName(), 1);
			projectElement.put(FieldType.VENDOR_ID.getName(), 1);
			projectElement.put(FieldType.SKU_CODE.getName(), 1);
			projectElement.put(FieldType.CATEGORY_CODE.getName(), 1);
			projectElement.put(FieldType.TXNTYPE.getName(), 1);
			projectElement.put(FieldType.ORIG_TXNTYPE.getName(), 1);
			projectElement.put(FieldType.STATUS.getName(), 1);
			projectElement.put(FieldType.MOP_TYPE.getName(), 1);
			projectElement.put(FieldType.PAYMENT_TYPE.getName(), 1);
			projectElement.put(FieldType.ACQUIRER_TYPE.getName(), 1);
			projectElement.put(FieldType.ACQ_ID.getName(), 1);
			projectElement.put(FieldType.UDF11.getName(), 1);
			projectElement.put(FieldType.UDF12.getName(), 1);
			projectElement.put(FieldType.UDF13.getName(), 1);
			projectElement.put(FieldType.UDF14.getName(), 1);
			projectElement.put(FieldType.UDF15.getName(), 1);
			projectElement.put(FieldType.UDF16.getName(), 1);
			projectElement.put(FieldType.UDF17.getName(), 1);
			projectElement.put(FieldType.UDF18.getName(), 1);
			projectElement.put(FieldType.RRN.getName(), 1);
			projectElement.put(FieldType.TXN_CAPTURE_FLAG.getName(), 1);
			projectElement.put(FieldType.TRANSACTION_MODE.getName(), 1);
			projectElement.put(FieldType.RESPONSE_MESSAGE.getName(), 1);
			projectElement.put(FieldType.PG_TXN_MESSAGE.getName(), 1);
			projectElement.put(FieldType.CARD_MASK.getName(), 1);
			projectElement.put(FieldType.PAYER_ADDRESS.getName(), 1);
			projectElement.put(FieldType.PART_SETTLE.getName(), 1);
			projectElement.put(FieldType.CREATE_DATE.getName(), 1);
			projectElement.put(FieldType.AMOUNT.getName(), 1);
			projectElement.put(FieldType.ORDER_ID.getName(), 1);
			projectElement.put(FieldType.REFUND_ORDER_ID.getName(), 1);
			projectElement.put(FieldType.PRODUCT_DESC.getName(), 1);
			projectElement.put(FieldType.PG_DATE_TIME.getName(), 1);
			projectElement.put(FieldType.INTERNAL_CARD_ISSUER_BANK.getName(), 1);
			projectElement.put(FieldType.INTERNAL_CARD_ISSUER_COUNTRY.getName(), 1);
			projectElement.put(FieldType.REFUNDABLE_AMOUNT.getName(), 1);
			projectElement.put(FieldType.TOTAL_AMOUNT.getName(), 1);
			projectElement.put(FieldType.CURRENCY_CODE.getName(), 1);
			projectElement.put(FieldType.PAYMENTS_REGION.getName(), 1);
			projectElement.put(FieldType.CARD_HOLDER_TYPE.getName(), 1);
			projectElement.put(FieldType.RESELLER_CHARGES.getName(), 1);
			projectElement.put(FieldType.RESELLER_GST.getName(), 1);
			projectElement.put(FieldType.ACQUIRER_TDR_SC.getName(), 1);
			projectElement.put(FieldType.PG_TDR_SC.getName(), 1);
			projectElement.put(FieldType.ACQUIRER_GST.getName(), 1);
			projectElement.put(FieldType.PG_GST.getName(), 1);
			projectElement.put(FieldType.SETTLEMENT_FLAG.getName(), 1);
			projectElement.put(FieldType.SETTLEMENT_DATE.getName(), 1);
			projectElement.put(FieldType.IS_ENCRYPTED.getName(), 1);
			projectElement.put(FieldType.SUF_TDR.getName(), 1);
			projectElement.put(FieldType.SUF_GST.getName(), 1);
			BasicDBObject project = new BasicDBObject("$project", projectElement);

			MongoCollection<Document> coll = null;

			if (searchFlag == true) {
				coll = dbIns.getCollection(propertiesManager.propertiesMap
						.get(prefix + Constants.TRANSACTION_STATUS_COLLECTION.getValue()));
			} else {
				coll = dbIns.getCollection(
						propertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));
			}

			BasicDBObject match = new BasicDBObject("$match", finalquery);
			BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject("CREATE_DATE", -1));

			List<BasicDBObject> pipeline = Arrays.asList(match, project, sort);

			AggregateIterable<Document> output = coll.aggregate(pipeline);
			output.allowDiskUse(true);
			MongoCursor<Document> cursor = output.iterator();

			while (cursor.hasNext()) {

				Document dbobj = cursor.next();

				if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
						&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
					dbobj = dataEncDecTool.decryptDocument(dbobj);
				}

				TransactionSearchDownloadObject transReport = new TransactionSearchDownloadObject();

				transReport.setTransactionId(dbobj.getString(FieldType.TXN_ID.toString()));
				transReport.setPgRefNum(dbobj.getString(FieldType.PG_REF_NUM.toString()));
				transReport.setPayId(dbobj.getString(FieldType.PAY_ID.toString()));
				if (StringUtils.isNotBlank(dbobj.getString(FieldType.PAYMENTS_REGION.getName()))) {
					transReport.setTransactionRegion(dbobj.getString(FieldType.PAYMENTS_REGION.getName()));
				} else {
					transReport.setTransactionRegion(CrmFieldConstants.NA.getValue());
				}
				transReport.setTxnSettledType(dbobj.getString(FieldType.TXN_CAPTURE_FLAG.getName()));
				transReport.setResponseMessage(dbobj.getString(FieldType.RESPONSE_MESSAGE.getName()));
				if (!StringUtils.isEmpty(dbobj.getString(FieldType.PG_TXN_MESSAGE.getName()))) {
					transReport.setAccqResponseMessage(dbobj.getString(FieldType.PG_TXN_MESSAGE.getName()));
				} else {
					transReport.setAccqResponseMessage(CrmFieldConstants.NA.getValue());
				}
				if (dbobj.getString(FieldType.UDF6.getName()) != null) {
					transReport.setDeltaRefundFlag(dbobj.getString(FieldType.UDF6.getName()));
				} else {
					transReport.setDeltaRefundFlag("");
				}

				String payid = (String) dbobj.get(FieldType.PAY_ID.getName());
				transReport.setPayId(payid);

				if (merchNameMap.get(dbobj.getString(FieldType.PAY_ID.toString())) != null) {
					transReport.setMerchants(merchNameMap.get(dbobj.getString(FieldType.PAY_ID.toString())));
				} else {

					String businessName = userDao.getBusinessNameByPayId(dbobj.getString(FieldType.PAY_ID.toString()));
					merchNameMap.put(dbobj.getString(FieldType.PAY_ID.toString()), businessName);
					transReport
							.setMerchants(userDao.getBusinessNameByPayId(dbobj.getString(FieldType.PAY_ID.toString())));

				}

				/*
				 * if(user.getUserType().equals(UserType.PARENTMERCHANT)){
				 * transReport.setMerchants(user.getBusinessName()); }else{
				 * transReport.setMerchants(dbobj.getString(CrmFieldType.BUSINESS_NAME.getName()
				 * )); }
				 */

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.CARD_MASK.toString()))) {
					transReport.setCardNumber(((Document) dbobj).getString(FieldType.CARD_MASK.toString()));
				} else if (StringUtils.isNotBlank(dbobj.getString(FieldType.PAYER_ADDRESS.getName()))) {

					if (user.getUserType().equals(UserType.ADMIN) || user.getUserType().equals(UserType.SUBADMIN)) {
						transReport.setCardNumber(dbobj.getString(FieldType.PAYER_ADDRESS.getName()));
					} else {
						String vpaString = dbobj.getString(FieldType.PAYER_ADDRESS.getName());
						/*
						 * String[] vpaArray = vpaString.split("@"); char[] vpaChar =
						 * vpaArray[0].toCharArray(); StringBuilder vpastrBuilder = new StringBuilder();
						 * 
						 * if (vpaChar.length > 3) { for (int i = 0; i < vpaChar.length - 3; i++) {
						 * vpastrBuilder.append(vpaChar[i]); } vpastrBuilder.append("***@");
						 * vpastrBuilder.append(vpaArray[1]); } else { vpastrBuilder.append(vpaChar[0]);
						 * vpastrBuilder.append("**@"); vpastrBuilder.append(vpaArray[1]); }
						 */

						transReport.setCardNumber(vpaString);
					}
				} else {
					transReport.setCardNumber(CrmFieldConstants.NA.getValue());
				}

				// if (!merchantPayId.equalsIgnoreCase("All") &&
				if (dbobj.containsKey(FieldType.SUB_MERCHANT_ID.getName())
						&& StringUtils.isNotBlank(dbobj.getString(FieldType.SUB_MERCHANT_ID.getName()))) {

					String subMerchant = dbobj.getString(FieldType.SUB_MERCHANT_ID.getName());
					User subMerchantUser = new User();

					if (userMap.get(subMerchant) != null && !userMap.get(subMerchant).getPayId().isEmpty()) {
						subMerchantUser = userMap.get(subMerchant);
					} else {
						subMerchantUser = userdao.findPayId(subMerchant);
						userMap.put(subMerchant, subMerchantUser);
					}
					transReport.setSubMerchantId(subMerchantUser.getBusinessName());
				}

				if (null != dbobj.getString(FieldType.ORIG_TXNTYPE.toString())) {

					// if
					// (StringUtils.isNotBlank(dbobj.getString(FieldType.SETTLEMENT_FLAG.getName()))
					// &&
					// dbobj.getString(FieldType.SETTLEMENT_FLAG.getName()).equals("Y"))
					// {
					// if (dbobj.getString(FieldType.ORIG_TXNTYPE.toString())
					// .equals(TransactionType.REFUND.getName())) {
					// transReport.setTxnType(TransactionType.REFUND.getName());
					// } else {
					// transReport.setTxnType(TransactionType.SALE.getName());
					// }
					// } else {
					// transReport.setTxnType(dbobj.getString(FieldType.ORIG_TXNTYPE.toString()));
					// }
					transReport.setTxnType(dbobj.getString(FieldType.ORIG_TXNTYPE.toString()));
				} else {
					// If ORIG_TXN_TYPE is not available incase of a timeout ,
					// set TXNTYPE instead
					// of ORIG_TXN_TYPE
					if (dbobj.getString(FieldType.STATUS.toString()).equalsIgnoreCase(StatusType.TIMEOUT.getName())) {
						transReport.setTxnType(dbobj.getString(FieldType.TXNTYPE.toString()));
					} else {
						transReport.setTxnType(CrmFieldConstants.NA.getValue());
					}
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.TRANSACTION_MODE.getName()))) {
					transReport.setTransactionMode(dbobj.getString(FieldType.TRANSACTION_MODE.getName()));
				} else {
				}
				if (null != dbobj.getString(FieldType.ACQUIRER_TYPE.toString())) {
					transReport.setAcquirerType(dbobj.getString(FieldType.ACQUIRER_TYPE.toString()));
				} else {
					transReport.setAcquirerType(CrmFieldConstants.NA.getValue());
				}

				if (null != dbobj.getString(FieldType.ACQUIRER_MODE.getName())) {
					transReport.setAcquirerMode(dbobj.getString(FieldType.ACQUIRER_MODE.getName()));
				} else {
					transReport.setAcquirerMode(CrmFieldConstants.NA.getValue());
				}

				if (null != dbobj.getString(FieldType.PAYMENT_TYPE.toString())) {
					transReport.setPaymentMethods(
							PaymentType.getpaymentName(dbobj.getString(FieldType.PAYMENT_TYPE.toString())));
				} else {
					transReport.setPaymentMethods(CrmFieldConstants.NA.getValue());
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF11.getName()))) {
					transReport.setUDF11(dbobj.getString(FieldType.UDF11.getName()));
				} else {
					transReport.setUDF11(CrmFieldConstants.NA.getValue());
				}
				if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF12.getName()))) {
					transReport.setUDF12(dbobj.getString(FieldType.UDF12.getName()));
				} else {
					transReport.setUDF12(CrmFieldConstants.NA.getValue());
				}
				if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF13.getName()))) {
					transReport.setUDF13(dbobj.getString(FieldType.UDF13.getName()));
				} else {
					transReport.setUDF13(CrmFieldConstants.NA.getValue());
				}
				if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF14.getName()))) {
					transReport.setUDF14(dbobj.getString(FieldType.UDF14.getName()));
				} else {
					transReport.setUDF14(CrmFieldConstants.NA.getValue());
				}
				if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF15.getName()))) {
					transReport.setUDF15(dbobj.getString(FieldType.UDF15.getName()));
				} else {
					transReport.setUDF15(CrmFieldConstants.NA.getValue());
				}
				if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF16.getName()))) {
					transReport.setUDF16(dbobj.getString(FieldType.UDF16.getName()));
				} else {
					transReport.setUDF16(CrmFieldConstants.NA.getValue());
				}
				if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF17.getName()))) {
					transReport.setUDF17(dbobj.getString(FieldType.UDF17.getName()));
				} else {
					transReport.setUDF17(CrmFieldConstants.NA.getValue());
				}
				if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF18.getName()))) {
					transReport.setUDF18(dbobj.getString(FieldType.UDF18.getName()));
				} else {
					transReport.setUDF18(CrmFieldConstants.NA.getValue());
				}

				// if customTransaction flag is true
				if (((user.getUserType().equals(UserType.MERCHANT)) || (user.getUserType().equals(UserType.MERCHANT))
						|| (user.getUserType().equals(UserType.PARENTMERCHANT)))
						&& merchantSettings.isCustomTransactionStatus()) {
					if (((dbobj.getString(FieldType.STATUS.toString())).equalsIgnoreCase(StatusType.TIMEOUT.getName()))
							|| ((dbobj.getString(FieldType.STATUS.toString()))
									.equalsIgnoreCase(StatusType.CAPTURED.getName()))
							|| ((dbobj.getString(FieldType.STATUS.toString()))
									.equalsIgnoreCase(StatusType.ENROLLED.getName()))
							|| ((dbobj.getString(FieldType.STATUS.toString()))
									.equalsIgnoreCase(StatusType.SETTLED.getName()))
							|| ((dbobj.getString(FieldType.STATUS.toString()))
									.equalsIgnoreCase(StatusType.SENT_TO_BANK.getName()))
							|| ((dbobj.getString(FieldType.STATUS.toString()))
									.equalsIgnoreCase(StatusType.PENDING.getName()))) {
						// if
						// (StringUtils.isNotBlank(dbobj.getString(FieldType.SETTLEMENT_FLAG.getName()))
						// &&
						// dbobj.getString(FieldType.SETTLEMENT_FLAG.getName()).equals("Y"))
						// {
						// transReport.setStatus(StatusType.SETTLED.getName());
						// } else {
						// transReport.setStatus(dbobj.getString(FieldType.STATUS.toString()));
						// }
						transReport.setStatus(dbobj.getString(FieldType.STATUS.toString()));
					} else {
						transReport.setStatus("Failed");
					}
				} else {
					transReport.setStatus(dbobj.getString(FieldType.STATUS.toString()));
					// if
					// (StringUtils.isNotBlank(dbobj.getString(FieldType.SETTLEMENT_FLAG.getName()))
					// &&
					// dbobj.getString(FieldType.SETTLEMENT_FLAG.getName()).equals("Y"))
					// {
					// transReport.setStatus(StatusType.SETTLED.getName());
					// } else {
					// transReport.setStatus(dbobj.getString(FieldType.STATUS.toString()));
					// }
				}
				// if
				// (StringUtils.isNotBlank(dbobj.getString(FieldType.SETTLEMENT_FLAG.getName()))
				// &&
				// dbobj.getString(FieldType.SETTLEMENT_FLAG.getName()).equals("Y"))
				// {
				// transReport.setDateFrom(dbobj.getString(FieldType.SETTLEMENT_DATE.getName()));
				// } else {
				// transReport.setDateFrom(dbobj.getString(FieldType.CREATE_DATE.getName()));
				// }
				transReport.setDateFrom(dbobj.getString(FieldType.CREATE_DATE.getName()));
				transReport.setAmount(dbobj.getString(FieldType.AMOUNT.toString()));
				transReport.setOrderId(dbobj.getString(FieldType.ORDER_ID.toString()));
				if (dbobj.getString(FieldType.TOTAL_AMOUNT.toString()) != null) {
					transReport.setTotalAmount(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()));
				} else {
					transReport.setTotalAmount("");
				}

				if (dbobj.getString(FieldType.ACQ_ID.toString()) != null) {
					transReport.setAcqId(dbobj.getString(FieldType.ACQ_ID.toString()));
				} else {
					transReport.setAcqId(CrmFieldConstants.NA.getValue());
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.RRN.getName()))) {
					transReport.setRrn(dbobj.getString(FieldType.RRN.toString()));
				} else {
					transReport.setRrn(CrmFieldConstants.NA.getValue());
				}

				if (dbobj.getString(FieldType.REFUND_ORDER_ID.toString()) != null) {
					transReport.setRefundOrderId(dbobj.getString(FieldType.REFUND_ORDER_ID.toString()));
				} else {
					transReport.setRefundOrderId(CrmFieldConstants.NA.getValue());
				}

				if (user.getUserType().equals(UserType.ADMIN) || user.getUserType().equals(UserType.SUBADMIN)) {

					if (null != dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString())) {
						transReport.setAcquirerTdrOrSurcharge(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()));
					} else {
						transReport.setAcquirerTdrOrSurcharge(CrmFieldConstants.NA.getValue());
					}

					if (null != dbobj.getString(FieldType.ACQUIRER_GST.toString())) {
						transReport.setAcquirerGST(dbobj.getString(FieldType.ACQUIRER_GST.toString()));
					} else {
						transReport.setAcquirerGST(CrmFieldConstants.NA.getValue());
					}
					if (null != dbobj.getString(FieldType.PG_TDR_SC.toString())) {
						transReport.setPgTdrOrSurcharge(dbobj.getString(FieldType.PG_TDR_SC.toString()));
					} else {
						transReport.setPgTdrOrSurcharge(CrmFieldConstants.NA.getValue());
					}
					if (null != dbobj.getString(FieldType.PG_GST.toString())) {
						transReport.setPgGST(dbobj.getString(FieldType.PG_GST.toString()));
					} else {
						transReport.setPgGST(CrmFieldConstants.NA.getValue());
					}

					if (dbobj.containsKey(FieldType.RESELLER_CHARGES.getName())
							&& StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_CHARGES.getName()))) {
						transReport.setResellerCharges(dbobj.getString(FieldType.RESELLER_CHARGES.getName()));
					} else {
						transReport.setResellerCharges(CrmFieldConstants.NA.getValue());
					}

					if (dbobj.containsKey(FieldType.RESELLER_GST.getName())
							&& StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_GST.getName()))) {
						transReport.setResellerGst(dbobj.getString(FieldType.RESELLER_GST.getName()));
					} else {
						transReport.setResellerGst(CrmFieldConstants.NA.getValue());
					}

				} else {

					if (dbobj.containsKey(FieldType.RESELLER_CHARGES.getName())
							&& StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_CHARGES.toString()))) {

						if (null != dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString())
								&& (null != dbobj.getString(FieldType.PG_TDR_SC.toString()))) {

							transReport.setTdrOrSurcharge(String.format("%.2f", ((Double
									.parseDouble(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString())))
									+ (Double.parseDouble(dbobj.getString(FieldType.PG_TDR_SC.toString())))
									+ (Double.parseDouble(dbobj.getString(FieldType.RESELLER_CHARGES.toString()))))));
						} else {
							transReport.setTdrOrSurcharge("0.00");
						}

					} else {
						if (null != dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString())
								&& (null != dbobj.getString(FieldType.PG_TDR_SC.toString()))) {

							transReport.setTdrOrSurcharge(String.format("%.2f",
									(Double.parseDouble(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
											+ (Double.parseDouble(dbobj.getString(FieldType.PG_TDR_SC.toString()))))));
						} else {
							transReport.setTdrOrSurcharge("0.00");
						}
					}

					if (dbobj.containsKey(FieldType.RESELLER_GST.getName())
							&& StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_GST.toString()))) {

						if (null != dbobj.getString(FieldType.ACQUIRER_GST.toString())
								&& (null != dbobj.getString(FieldType.PG_GST.toString()))) {
							transReport.setGst(String.format("%.2f", ((Double
									.parseDouble(dbobj.getString(FieldType.ACQUIRER_GST.toString())))
									+ (Double.parseDouble(dbobj.getString(FieldType.PG_GST.toString())))
									+ (Double.parseDouble(dbobj.getString(FieldType.RESELLER_GST.toString()))))));

						} else {
							transReport.setGst("0.00");
						}
					} else {

						if (null != dbobj.getString(FieldType.ACQUIRER_GST.toString())
								&& (null != dbobj.getString(FieldType.PG_GST.toString()))) {
							transReport.setGst(String.format("%.2f",
									(Double.parseDouble(dbobj.getString(FieldType.ACQUIRER_GST.toString()))
											+ (Double.parseDouble(dbobj.getString(FieldType.PG_GST.toString()))))));

						} else {
							transReport.setGst("0.00");
						}
					}
				}

				if (null != dbobj.getString(FieldType.MOP_TYPE.toString())) {
					transReport.setMopType(MopType.getmopName(dbobj.getString(FieldType.MOP_TYPE.toString())));

				} else {
					transReport.setMopType(CrmFieldConstants.NA.toString());
				}

				if (null != dbobj.getString(FieldType.CARD_HOLDER_TYPE.toString())) {
					transReport.setCardHolderType(dbobj.getString(FieldType.CARD_HOLDER_TYPE.toString()));
				} else {
					transReport.setCardHolderType(CrmFieldConstants.NA.toString());
				}

				// Merchant TDR/SC and Merhcant GST
				if (dbobj.containsKey(FieldType.MERCHANT_TDR_SC.getName())
						&& dbobj.containsKey(FieldType.MERCHANT_GST.getName())
						&& StringUtils.isNotBlank(dbobj.getString(FieldType.MERCHANT_TDR_SC.getName()))
						&& StringUtils.isNotBlank(dbobj.getString(FieldType.MERCHANT_GST.getName()))) {

					transReport.setMerchantTdrOrSc(dbobj.getString(FieldType.MERCHANT_TDR_SC.getName()));
					transReport.setMerchantGst(dbobj.getString(FieldType.MERCHANT_GST.getName()));

				} else if (dbobj.containsKey(FieldType.PG_TDR_SC.getName())
						&& dbobj.containsKey(FieldType.PG_GST.getName())
						&& dbobj.containsKey(FieldType.ACQUIRER_TDR_SC.getName())
						&& dbobj.containsKey(FieldType.ACQUIRER_GST.getName())
						&& dbobj.containsKey(FieldType.RESELLER_CHARGES.getName())
						&& dbobj.containsKey(FieldType.RESELLER_GST.getName())
						&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.getName()))
						&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.getName()))
						&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.getName()))
						&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.getName()))
						&& StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_CHARGES.getName()))
						&& StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_GST.getName()))) {

					transReport.setMerchantTdrOrSc(String.format("%.2f",
							((Double.parseDouble(dbobj.getString(FieldType.PG_TDR_SC.toString())))
									+ (Double.parseDouble(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString())))
									+ (Double.parseDouble(dbobj.getString(FieldType.RESELLER_CHARGES.toString()))))));

					transReport.setMerchantGst(String.format("%.2f",
							((Double.parseDouble(dbobj.getString(FieldType.ACQUIRER_GST.toString())))
									+ (Double.parseDouble(dbobj.getString(FieldType.PG_GST.toString())))
									+ (Double.parseDouble(dbobj.getString(FieldType.RESELLER_GST.toString()))))));

				} else {

					if (dbobj.containsKey(FieldType.PG_TDR_SC.getName())
							&& dbobj.containsKey(FieldType.ACQUIRER_TDR_SC.getName())
							&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.getName()))
							&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.getName()))) {

						transReport.setMerchantTdrOrSc(String.format("%.2f",
								((Double.parseDouble(dbobj.getString(FieldType.PG_TDR_SC.toString()))) + (Double
										.parseDouble(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))))));
					} else {
						transReport.setMerchantTdrOrSc(CrmFieldConstants.NA.getValue());
					}

					if (dbobj.containsKey(FieldType.PG_GST.getName())
							&& dbobj.containsKey(FieldType.ACQUIRER_GST.getName())
							&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.getName()))
							&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.getName()))) {

						transReport.setMerchantGst(String.format("%.2f",
								((Double.parseDouble(dbobj.getString(FieldType.PG_GST.toString())))
										+ (Double.parseDouble(dbobj.getString(FieldType.ACQUIRER_GST.toString()))))));
					} else {
						transReport.setMerchantGst(CrmFieldConstants.NA.getValue());
					}

				}
				// add by vishal
				if (StringUtils.isNotBlank(dbobj.getString(FieldType.SUF_GST.toString()))) {
					transReport.setSufGst(dbobj.getString(FieldType.SUF_GST.toString()));
					// transReport.setTotalAmount(String.valueOf(new
					// BigDecimal(transReport.getTotalAmount()).subtract(new
					// BigDecimal(dbobj.getString(FieldType.SUF_GST.toString())))));

				} else {
					transReport.setSufGst(CrmFieldConstants.NA.getValue());
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.SUF_TDR.toString()))) {
					transReport.setSufTdr(dbobj.getString(FieldType.SUF_TDR.toString()));
					// transReport.setTotalAmount (String.valueOf(new
					// BigDecimal(transReport.getTotalAmount()).subtract(new
					// BigDecimal(dbobj.getString(FieldType.SUF_TDR.toString())))));
				} else {
					transReport.setSufTdr(CrmFieldConstants.NA.getValue());
				}

				transactionList.add(transReport);
			}
			logger.info("transactionList created and size = " + transactionList.size());
			cursor.close();
			logger.info("Inside TxnReports , searchPayment , transactionListSize = " + transactionList.size());
			Comparator<TransactionSearchDownloadObject> comp = (TransactionSearchDownloadObject a,
					TransactionSearchDownloadObject b) -> {

				if (a.getDateFrom().compareTo(b.getDateFrom()) > 0) {
					return -1;
				} else if (a.getDateFrom().compareTo(b.getDateFrom()) < 0) {
					return 1;
				} else {
					return 0;
				}
			};
			Collections.sort(transactionList, comp);
			logger.info("transactionList created and Sorted");
			return transactionList;
		}

		catch (Exception e) {
			logger.error("Exception occured in TxnReports , searchPaymentDownload , Exception = ", e);
			return transactionList;
		}
	}// close()method

	public List<PaymentSearchDownloadObject> searchPaymentForDownload(String merchantPayId, String subMerchantPayId,
			String customerEmail, String paymentType, String status, String currency, String transactionType,
			String fromDate, String toDate, User user, String paymentsRegion, String acquirer, String partSettleFlag,
			boolean isGlocal, Set<String> orderIdSet, String transactionFlag, String deltaFlag, String pgRefNum,
			String orderId, String autoRefund) {

		logger.info("Inside TxnReports , searchPaymentForDownload");
		Map<String, User> userMap = new HashMap<String, User>();
		boolean isParameterised = false;

		List<PaymentSearchDownloadObject> transactionList = new ArrayList<PaymentSearchDownloadObject>();
		try {

			UserSettingData userSettings = userSettingDao.fetchDataUsingPayId(user.getPayId());

			List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();
			List<String> acquirerConditionLst = new ArrayList<String>();
			BasicDBObject acquirerQuery = new BasicDBObject();
			BasicDBObject customerQuery = new BasicDBObject();
			BasicDBObject customerQueryMask = new BasicDBObject();
			BasicDBObject txnCapturedFlag = new BasicDBObject();
			BasicDBObject dateIndexConditionQuery = new BasicDBObject();

			DateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.ENGLISH);
			Date dateStart = format.parse(fromDate);
			Date dateEnd = format.parse(toDate);

			LocalDate startDate = dateStart.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			LocalDate endDate = dateEnd.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

			String fromDateIndex = startDate.toString().replaceAll("-", "");
			String toDateIndex = endDate.toString().replaceAll("-", "");

			dateIndexConditionQuery.put("DATE_INDEX",
					BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(fromDateIndex).toLocalizedPattern())
							.add("$lte", new SimpleDateFormat(toDateIndex).toLocalizedPattern()).get());

			BasicDBObject orderIdConditionQuery = null;
			if (orderIdSet != null) {
				List<String> orderIdList = new ArrayList<>(orderIdSet);
				orderIdConditionQuery = new BasicDBObject("$in", orderIdList);
			}

			if (!orderId.isEmpty()) {
				paramConditionLst.add(new BasicDBObject(FieldType.ORDER_ID.getName(), orderId));
			} else if (null != orderIdConditionQuery) {
				paramConditionLst.add(new BasicDBObject(FieldType.ORDER_ID.getName(), orderIdConditionQuery));
			}

			if (!pgRefNum.isEmpty()) {
				paramConditionLst.add(new BasicDBObject(FieldType.PG_REF_NUM.getName(), pgRefNum));
			}

			if (StringUtils.isNotBlank(merchantPayId) && !merchantPayId.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.PAY_ID.getName(), merchantPayId));
			}

			if (StringUtils.isNotBlank(subMerchantPayId) && !subMerchantPayId.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.SUB_MERCHANT_ID.getName(), subMerchantPayId));
			}

			if (StringUtils.isNotEmpty(customerEmail) && !customerEmail.isEmpty()
					&& !customerEmail.equalsIgnoreCase("ALL")) {
				isParameterised = true;
				customerQuery.append(FieldType.CUST_EMAIL.getName(), customerEmail);
				if (StringUtils.isNotBlank(customerEmail)) {
					String email = customerEmail;
					String emailMask = null;
					final String mask = "*****";
					final int at = email.indexOf("@");
					if (at > 2) {
						final int maskLen = Math.min(Math.max(at / 2, 2), 5);
						final int start1 = (at - maskLen) / 2;
						emailMask = email.substring(0, start1) + mask.substring(0, maskLen)
								+ email.substring(start1 + maskLen);
					}
					customerQueryMask.append(FieldType.CUST_EMAIL.getName(), emailMask);
				}
			}

			if (StringUtils.isNotBlank(status) && !status.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.STATUS.getName(), status));
			} else {

			}

			if (StringUtils.isNotBlank(currency) && !currency.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.CURRENCY_CODE.getName(), currency));
			}

			if (StringUtils.isNotBlank(partSettleFlag) && !partSettleFlag.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.PART_SETTLE.getName(), partSettleFlag));
			}

			if (StringUtils.isNotBlank(transactionType) && !transactionType.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.ORIG_TXNTYPE.getName(), transactionType));
			}

			if (StringUtils.isNotBlank(paymentType) && !paymentType.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.PAYMENT_TYPE.getName(), paymentType));
			}

			if (StringUtils.isNotBlank(paymentsRegion) && !paymentsRegion.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.PAYMENTS_REGION.getName(), paymentsRegion));
			}

			if (StringUtils.isNotBlank(transactionFlag) && !transactionFlag.equalsIgnoreCase("ALL")) {
				txnCapturedFlag.append("$in", transactionFlag.split(","));
				paramConditionLst.add(new BasicDBObject(FieldType.TXN_CAPTURE_FLAG.getName(), txnCapturedFlag));
			}

			if (StringUtils.isNotBlank(deltaFlag) && !deltaFlag.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.DELTA_REFUND_FLAG.getName(), deltaFlag));
			}

			if (StringUtils.isNotBlank(autoRefund) && !autoRefund.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.AUTO_REFUND_FLAG.getName(), autoRefund));
			}

			if (user.getUserType().equals(UserType.RESELLER)) {
				paramConditionLst.add(new BasicDBObject(FieldType.RESELLER_ID.getName(), user.getResellerId()));
			}

			if (StringUtils.isNotBlank(acquirer) && !acquirer.equalsIgnoreCase("ALL")) {
				List<String> acquirerList = Arrays.asList(acquirer.split(","));
				for (String acq : acquirerList) {
					acquirerConditionLst.add(acq.trim());
				}
				acquirerQuery.append("$in", acquirerConditionLst);
				paramConditionLst.add(new BasicDBObject(FieldType.ACQUIRER_TYPE.getName(), acquirerQuery));
			}

			List<BasicDBObject> fianlQueryList = new ArrayList<BasicDBObject>();

			if (StringUtils.isNotBlank(pgRefNum) || StringUtils.isNotBlank(orderId)) {

			} else {
				if (!dateIndexConditionQuery.isEmpty()) {
					paramConditionLst.add(dateIndexConditionQuery);
				}
			}

			fianlQueryList.addAll(paramConditionLst);
			if (!customerQuery.isEmpty()) {
				paramConditionLst.add(customerQuery);
			}

			if (!customerQueryMask.isEmpty()) {
				fianlQueryList.add(customerQueryMask);
			}

			BasicDBObject finalquery = new BasicDBObject("$and", paramConditionLst);
			BasicDBObject finalqueryMask = new BasicDBObject("$and", fianlQueryList);

			if (!customerQueryMask.isEmpty()) {
				logger.info("Inside TxnReports , searchPaymentForDownload , finalQuery = " + finalqueryMask);
			} else {
				logger.info("Inside TxnReports , searchPaymentForDownload , finalQuery = " + finalquery);
			}

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns
					.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));
			BasicDBObject match = new BasicDBObject("$match", finalquery);
			// Now the aggregate operation ()In case any parameter is passed in
			// search query
			// , then show all records
			BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject("CREATE_DATE", -1));

			List<BasicDBObject> pipeline = Arrays.asList(match, sort);

			AggregateIterable<Document> output = coll.aggregate(pipeline);
			output.allowDiskUse(true);
			MongoCursor<Document> cursor = output.iterator();
			int count = 0;
			while (cursor.hasNext()) {
				Document dbobj = cursor.next();
				if (StringUtils.isNotBlank(dbobj.getString("IS_ENCRYPTED"))
						&& dbobj.getString("IS_ENCRYPTED").equalsIgnoreCase("Y")) {
					dbobj = dataEncDecTool.decryptDocument(dbobj);
				}
				PaymentSearchDownloadObject transReport = new PaymentSearchDownloadObject();
				transReport.setTransactionId(dbobj.getString(FieldType.TXN_ID.toString()));
				transReport.setPgRefNum(dbobj.getString(FieldType.PG_REF_NUM.toString()));
				if (StringUtils.isNotBlank(dbobj.getString(FieldType.PAYMENTS_REGION.getName()))) {
					transReport.setTransactionRegion(dbobj.getString(FieldType.PAYMENTS_REGION.getName()));
				} else {
					transReport.setTransactionRegion(CrmFieldConstants.NA.getValue());
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.TXN_CAPTURE_FLAG.getName()))) {
					transReport.setTxnSettledType(dbobj.getString(FieldType.TXN_CAPTURE_FLAG.getName()));
				} else {
					transReport.setTxnSettledType(CrmFieldConstants.NA.getValue());
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.TRANSACTION_MODE.getName()))) {
					transReport.setTransactionMode(dbobj.getString(FieldType.TRANSACTION_MODE.getName()));
				} else {
				}

				// if
				// (StringUtils.isNotBlank(dbobj.getString(FieldType.POST_SETTLED_FLAG.getName())))
				// {
				// transReport.setPostSettledFlag(dbobj.getString(FieldType.POST_SETTLED_FLAG.getName()));
				// } else {
				// transReport.setPostSettledFlag(CrmFieldConstants.NA.getValue());
				// }

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.PART_SETTLE.toString()))
						&& dbobj.getString(FieldType.PART_SETTLE.toString()).equalsIgnoreCase(("Y"))) {
					transReport.setPartSettle(dbobj.getString(FieldType.PART_SETTLE.toString()));
				} else {
					transReport.setPartSettle(CrmFieldConstants.NA.getValue());
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.REFUND_FLAG.toString()))
						&& dbobj.getString(FieldType.REFUND_FLAG.toString()).equalsIgnoreCase(("R"))) {
					transReport.setRefund_flag(dbobj.getString(FieldType.REFUND_FLAG.toString()));
				} else {
					transReport.setRefund_flag("C");
				}

				// transReport.setCardMask(dbobj.getString(FieldType.CARD_MASK.getName()));
				if (null != dbobj.getString(FieldType.PAYMENT_TYPE.toString())) {
					if (null != dbobj.getString(FieldType.CARD_MASK.toString())) {
						transReport.setCardMask(dbobj.getString(FieldType.CARD_MASK.toString()));
					} else if (null != dbobj.getString(FieldType.PAYER_ADDRESS.getName())) {

						if (user.getUserType().equals(UserType.ADMIN) || user.getUserType().equals(UserType.SUBADMIN)) {
							transReport.setCardMask(dbobj.getString(FieldType.PAYER_ADDRESS.getName()));
						} else {
							String vpaString = dbobj.getString(FieldType.PAYER_ADDRESS.getName());
							String[] vpaArray = vpaString.split("@");
							char[] vpaChar = vpaArray[0].toCharArray();
							StringBuilder vpastrBuilder = new StringBuilder();

							if (vpaChar.length > 3) {
								for (int i = 0; i < vpaChar.length - 3; i++) {
									vpastrBuilder.append(vpaChar[i]);
								}
								vpastrBuilder.append("***@");
								vpastrBuilder.append(vpaArray[1]);
							} else {
								vpastrBuilder.append(vpaChar[0]);
								vpastrBuilder.append("**@");
								vpastrBuilder.append(vpaArray[1]);
							}

							transReport.setCardMask(vpastrBuilder.toString());
						}
					} else {
						transReport.setCardMask(CrmFieldConstants.NA.getValue());
					}
					// else if
					// ((dbobj.getString(FieldType.PAYMENT_TYPE.getName()))
					// .equals(PaymentType.WALLET.getCode())) {
					// transReport.setCardMask(CrmFieldConstants.WALLET.getValue());
					// }
				} else {
					transReport.setCardMask(CrmFieldConstants.NA.getValue());
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.CUST_NAME.getName()))) {
					transReport.setCustName(dbobj.getString(FieldType.CUST_NAME.getName()));
				} else {
					transReport.setCustName("NA");
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.CUST_EMAIL.getName()))) {
					transReport.setCustEmail(dbobj.getString(FieldType.CUST_EMAIL.getName()));
				} else {
					transReport.setCustEmail("NA");
				}
				if (StringUtils.isNotBlank(dbobj.getString(FieldType.PAYOUT_DATE.toString()))) {
					transReport.setPayOutDate((dbobj.getString(FieldType.PAYOUT_DATE.toString())));
				} else {
					transReport.setPayOutDate(CrmFieldConstants.NA.getValue());
				}
				if (StringUtils.isNotBlank(dbobj.getString(FieldType.UTR_NO.toString()))) {
					transReport.setUtrNo(dbobj.getString(FieldType.UTR_NO.toString()));
				} else {
					transReport.setUtrNo(CrmFieldConstants.NA.getValue());
				}
				/*
				 * if (dbobj.containsKey("CUSTOM_FLAG") &&
				 * StringUtils.isNotBlank(dbobj.getString("CUSTOM_FLAG"))) {
				 * transReport.setCustomFlag(((Document) dbobj).getString("CUSTOM_FLAG")); }
				 * else { transReport.setCustomFlag("N"); }
				 */

				User user1 = new User();

				if (StringUtils.isNotBlank((String) dbobj.get(FieldType.PAY_ID.getName()))) {
					String payid = (String) dbobj.get(FieldType.PAY_ID.getName());

					if (userMap.get(payid) != null && !userMap.get(payid).getPayId().isEmpty()) {
						user1 = userMap.get(payid);
					} else {
						user1 = userdao.findPayId(payid);
						userMap.put(payid, user1);
					}
				}
				transReport.setMerchants(user1.getBusinessName());

				if (merchantPayId.equalsIgnoreCase("All")) {

					if (StringUtils.isNotBlank(dbobj.getString(FieldType.REFUND_DAYS.getName()))) {
						transReport.setRefundCycle(dbobj.getString(FieldType.REFUND_DAYS.getName()));
					} else {
						transReport.setRefundCycle(CrmFieldConstants.NA.getValue());
					}

					if (StringUtils.isNotBlank(dbobj.getString(FieldType.PRODUCT_PRICE.getName()))) {
						transReport.setProductPrice(dbobj.getString(FieldType.PRODUCT_PRICE.getName()));
					} else {
						transReport.setProductPrice(CrmFieldConstants.NA.getValue());
					}
					if (StringUtils.isNotBlank(dbobj.getString(FieldType.VENDOR_ID.getName()))) {
						transReport.setVendorID(dbobj.getString(FieldType.VENDOR_ID.getName()));
					} else {
						transReport.setVendorID(CrmFieldConstants.NA.getValue());
					}
					if (StringUtils.isNotBlank(dbobj.getString(FieldType.SKU_CODE.getName()))) {
						transReport.setSKUCode(dbobj.getString(FieldType.SKU_CODE.getName()));
					} else {
						transReport.setSKUCode(CrmFieldConstants.NA.getValue());
					}

					if (StringUtils.isNotBlank(dbobj.getString(FieldType.CATEGORY_CODE.getName()))) {
						transReport.setCategoryCode(dbobj.getString(FieldType.CATEGORY_CODE.getName()));
					} else {
						transReport.setCategoryCode(CrmFieldConstants.NA.getValue());
					}
				} else {

					if (user.getUserType().equals(UserType.ADMIN) || user.getUserType().equals(UserType.SUBADMIN)
							|| user.getUserType().equals(UserType.RESELLER)) {
						User merhant = userMap.get(merchantPayId);
						UserSettingData merchantSettings = userSettingDao.fetchDataUsingPayId(merhant.getPayId());

						if (merchantSettings.isRetailMerchantFlag()) {
							if (StringUtils.isNotBlank(dbobj.getString(FieldType.REFUND_DAYS.getName()))) {
								transReport.setRefundCycle(dbobj.getString(FieldType.REFUND_DAYS.getName()));
							} else {
								transReport.setRefundCycle(CrmFieldConstants.NA.getValue());
							}

							if (StringUtils.isNotBlank(dbobj.getString(FieldType.PRODUCT_PRICE.getName()))) {
								transReport.setProductPrice(dbobj.getString(FieldType.PRODUCT_PRICE.getName()));
							} else {
								transReport.setProductPrice(CrmFieldConstants.NA.getValue());
							}
							if (StringUtils.isNotBlank(dbobj.getString(FieldType.VENDOR_ID.getName()))) {
								transReport.setVendorID(dbobj.getString(FieldType.VENDOR_ID.getName()));
							} else {
								transReport.setVendorID(CrmFieldConstants.NA.getValue());
							}
							if (StringUtils.isNotBlank(dbobj.getString(FieldType.SKU_CODE.getName()))) {
								transReport.setSKUCode(dbobj.getString(FieldType.SKU_CODE.getName()));
							} else {
								transReport.setSKUCode(CrmFieldConstants.NA.getValue());
							}

							if (StringUtils.isNotBlank(dbobj.getString(FieldType.CATEGORY_CODE.getName()))) {
								transReport.setCategoryCode(dbobj.getString(FieldType.CATEGORY_CODE.getName()));
							} else {
								transReport.setCategoryCode(CrmFieldConstants.NA.getValue());
							}
						}
					} else {

						if (userSettings.isRetailMerchantFlag()) {
							if (StringUtils.isNotBlank(dbobj.getString(FieldType.REFUND_DAYS.getName()))) {
								transReport.setRefundCycle(dbobj.getString(FieldType.REFUND_DAYS.getName()));
							} else {
								transReport.setRefundCycle(CrmFieldConstants.NA.getValue());
							}

							if (StringUtils.isNotBlank(dbobj.getString(FieldType.PRODUCT_PRICE.getName()))) {
								transReport.setProductPrice(dbobj.getString(FieldType.PRODUCT_PRICE.getName()));
							} else {
								transReport.setProductPrice(CrmFieldConstants.NA.getValue());
							}
							if (StringUtils.isNotBlank(dbobj.getString(FieldType.VENDOR_ID.getName()))) {
								transReport.setVendorID(dbobj.getString(FieldType.VENDOR_ID.getName()));
							} else {
								transReport.setVendorID(CrmFieldConstants.NA.getValue());
							}
							if (StringUtils.isNotBlank(dbobj.getString(FieldType.SKU_CODE.getName()))) {
								transReport.setSKUCode(dbobj.getString(FieldType.SKU_CODE.getName()));
							} else {
								transReport.setSKUCode(CrmFieldConstants.NA.getValue());
							}

							if (StringUtils.isNotBlank(dbobj.getString(FieldType.CATEGORY_CODE.getName()))) {
								transReport.setCategoryCode(dbobj.getString(FieldType.CATEGORY_CODE.getName()));
							} else {
								transReport.setCategoryCode(CrmFieldConstants.NA.getValue());
							}
						}

					}

				}

				// if (!merchantPayId.equalsIgnoreCase("All") &&
				if (dbobj.containsKey(FieldType.SUB_MERCHANT_ID.getName())
						&& StringUtils.isNotBlank(dbobj.getString(FieldType.SUB_MERCHANT_ID.getName()))) {

					String subMerchant = dbobj.getString(FieldType.SUB_MERCHANT_ID.getName());
					User subMerchantUser = new User();

					if (userMap.get(subMerchant) != null && !userMap.get(subMerchant).getPayId().isEmpty()) {
						subMerchantUser = userMap.get(subMerchant);
					} else {
						subMerchantUser = userdao.findPayId(subMerchant);
						userMap.put(subMerchant, subMerchantUser);
					}
					if (subMerchantUser != null) {
						transReport.setSubMerchantId(subMerchantUser.getBusinessName());
					} else {
						transReport.setSubMerchantId(CrmFieldConstants.NA.getValue());
					}

				}

				if (null != dbobj.getString(FieldType.ORIG_TXNTYPE.toString())) {
					transReport.setTxnType(dbobj.getString(FieldType.ORIG_TXNTYPE.toString()));
				} else {
					// If ORIG_TXN_TYPE is not available incase of a timeout ,
					// set TXNTYPE instead
					// of ORIG_TXN_TYPE
					if (dbobj.getString(FieldType.STATUS.toString()).equalsIgnoreCase(StatusType.TIMEOUT.getName())) {
						transReport.setTxnType(dbobj.getString(FieldType.TXNTYPE.toString()));
					} else {
						transReport.setTxnType(CrmFieldConstants.NA.getValue());
					}

				}
				if (null != dbobj.getString(FieldType.ACQUIRER_TYPE.toString())) {
					transReport.setAcquirerType(dbobj.getString(FieldType.ACQUIRER_TYPE.toString()));
				} else {
					transReport.setAcquirerType(CrmFieldConstants.NA.getValue());
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.REFUND_ORDER_ID.toString()))) {
					transReport.setRefundOrderId(dbobj.getString(FieldType.REFUND_ORDER_ID.toString()));
				} else {
					transReport.setRefundOrderId(CrmFieldConstants.NA.getValue());
				}

				transReport.setAcquirerMode(dbobj.getString(FieldType.ACQUIRER_MODE.getName()));
				if (null != dbobj.getString(FieldType.PAYMENT_TYPE.toString())) {
					transReport.setPaymentMethods(
							PaymentType.getpaymentName(dbobj.getString(FieldType.PAYMENT_TYPE.toString())));
				} else {
					transReport.setPaymentMethods(CrmFieldConstants.NA.getValue());
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF11.getName()))) {
					transReport.setUDF11(dbobj.getString(FieldType.UDF11.getName()));
				} else {
					transReport.setUDF11(CrmFieldConstants.NA.getValue());
				}
				if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF12.getName()))) {
					transReport.setUDF12(dbobj.getString(FieldType.UDF12.getName()));
				} else {
					transReport.setUDF12(CrmFieldConstants.NA.getValue());
				}
				if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF13.getName()))) {
					transReport.setUDF13(dbobj.getString(FieldType.UDF13.getName()));
				} else {
					transReport.setUDF13(CrmFieldConstants.NA.getValue());
				}
				if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF14.getName()))) {
					transReport.setUDF14(dbobj.getString(FieldType.UDF14.getName()));
				} else {
					transReport.setUDF14(CrmFieldConstants.NA.getValue());
				}
				if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF15.getName()))) {
					transReport.setUDF15(dbobj.getString(FieldType.UDF15.getName()));
				} else {
					transReport.setUDF15(CrmFieldConstants.NA.getValue());
				}
				if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF16.getName()))) {
					transReport.setUDF16(dbobj.getString(FieldType.UDF16.getName()));
				} else {
					transReport.setUDF16(CrmFieldConstants.NA.getValue());
				}
				if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF17.getName()))) {
					transReport.setUDF17(dbobj.getString(FieldType.UDF17.getName()));
				} else {
					transReport.setUDF17(CrmFieldConstants.NA.getValue());
				}
				if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF18.getName()))) {
					transReport.setUDF18(dbobj.getString(FieldType.UDF18.getName()));
				} else {
					transReport.setUDF18(CrmFieldConstants.NA.getValue());
				}

				transReport.setStatus(dbobj.getString(FieldType.STATUS.toString()));
				if (StatusType.CAPTURED.getName().equalsIgnoreCase(dbobj.getString(FieldType.STATUS.getName()))) {
					transReport.setDateFrom(dbobj.getString(FieldType.CREATE_DATE.getName()));
				} else {
					transReport.setSettledDate(dbobj.getString(FieldType.CREATE_DATE.getName()));
					transReport.setDateFrom(dbobj.getString(FieldType.PG_DATE_TIME.getName()));
					if (StringUtils.isNotBlank(dbobj.getString(FieldType.RRN.getName()))
							&& !dbobj.getString(FieldType.RRN.getName()).equalsIgnoreCase("-")) {
						transReport.setRrn(dbobj.getString(FieldType.RRN.getName()));
					} else {
						transReport.setRrn(CrmFieldConstants.NA.getValue());
					}
				}

				transReport.setAmount(dbobj.getString(FieldType.AMOUNT.toString()));
				transReport.setOrderId(dbobj.getString(FieldType.ORDER_ID.toString()));
				if (dbobj.getString(FieldType.TOTAL_AMOUNT.toString()) != null) {
					transReport.setTotalAmount(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()));
				} else {
					transReport.setTotalAmount("");
				}

				// changes regarding partner reseller
				if (user.getUserType().equals(UserType.RESELLER) && user.isPartnerFlag()) {

					if (StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_CHARGES.toString()))) {

						transReport.setResellerCharges(String.format("%.2f",
								(Double.parseDouble(dbobj.getString(FieldType.RESELLER_CHARGES.toString())))));

					} else {
						transReport.setResellerCharges("0.00");
					}

					if (StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_GST.toString()))) {
						transReport.setResellerGST(String.format("%.2f",
								(Double.parseDouble(dbobj.getString(FieldType.RESELLER_GST.toString())))));

					} else {
						transReport.setResellerGST("0.00");
					}

					if (StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_CHARGES.toString()))) {
						if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
								&& (StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.toString())))) {

							transReport.setTdrOrSurcharge(String.format("%.2f",
									(Double.parseDouble(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
											+ Double.parseDouble(dbobj.getString(FieldType.PG_TDR_SC.toString()))
											+ Double.parseDouble(
													dbobj.getString(FieldType.RESELLER_CHARGES.toString())))));

						} else {
							transReport.setTdrOrSurcharge("0.00");
						}
					} else {
						if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
								&& (StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.toString())))) {

							transReport.setTdrOrSurcharge(String.format("%.2f",
									(Double.parseDouble(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
											+ Double.parseDouble(dbobj.getString(FieldType.PG_TDR_SC.toString())))));

						} else {
							transReport.setTdrOrSurcharge("0.00");
						}
					}

					if (StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_GST.toString()))) {
						if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.toString()))
								&& (StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.toString())))) {
							transReport.setGst(String.format("%.2f",
									(Double.parseDouble(dbobj.getString(FieldType.ACQUIRER_GST.toString()))
											+ Double.parseDouble(dbobj.getString(FieldType.PG_GST.toString()))
											+ Double.parseDouble(dbobj.getString(FieldType.RESELLER_GST.toString())))));

						} else {
							transReport.setGst("0.00");
						}
					} else {
						if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.toString()))
								&& (StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.toString())))) {
							transReport.setGst(String.format("%.2f",
									(Double.parseDouble(dbobj.getString(FieldType.ACQUIRER_GST.toString()))
											+ Double.parseDouble(dbobj.getString(FieldType.PG_GST.toString())))));

						} else {
							transReport.setGst("0.00");
						}
					}

				} // END

				else {

					if (StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_CHARGES.toString()))) {
						if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
								&& (StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.toString())))) {

							transReport.setTdrOrSurcharge(String.format("%.2f",
									(Double.parseDouble(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
											+ Double.parseDouble(dbobj.getString(FieldType.PG_TDR_SC.toString()))
											+ Double.parseDouble(
													dbobj.getString(FieldType.RESELLER_CHARGES.toString())))));

						} else {
							transReport.setTdrOrSurcharge("0.00");
						}
					} else {
						if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
								&& (StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.toString())))) {

							transReport.setTdrOrSurcharge(String.format("%.2f",
									(Double.parseDouble(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
											+ Double.parseDouble(dbobj.getString(FieldType.PG_TDR_SC.toString())))));

						} else {
							transReport.setTdrOrSurcharge("0.00");
						}
					}

					if (StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_GST.toString()))) {
						if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.toString()))
								&& (StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.toString())))) {
							transReport.setGst(String.format("%.2f",
									(Double.parseDouble(dbobj.getString(FieldType.ACQUIRER_GST.toString()))
											+ Double.parseDouble(dbobj.getString(FieldType.PG_GST.toString()))
											+ Double.parseDouble(dbobj.getString(FieldType.RESELLER_GST.toString())))));

						} else {
							transReport.setGst("0.00");
						}
					} else {
						if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.toString()))
								&& (StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.toString())))) {
							transReport.setGst(String.format("%.2f",
									(Double.parseDouble(dbobj.getString(FieldType.ACQUIRER_GST.toString()))
											+ Double.parseDouble(dbobj.getString(FieldType.PG_GST.toString())))));

						} else {
							transReport.setGst("0.00");
						}
					}

				}
				// end else

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.PAYMENT_TYPE.toString()))
						&& dbobj.getString(FieldType.PAYMENT_TYPE.getName()).equalsIgnoreCase(PaymentType.COD.getCode())
						&& StringUtils.isNotBlank(dbobj.getString(FieldType.ORIG_TXNTYPE.toString()))
						&& dbobj.getString(FieldType.ORIG_TXNTYPE.getName()).equalsIgnoreCase(TxnType.SALE.getName())) {

					if (isGlocal == true) {

						if (dbobj.containsKey(FieldType.UDF7.getName())) {

							if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF7.getName()))) {

								if (StringUtils.isNotBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
										&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.toString()))
										&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
										&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.toString()))
										&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.toString()))) {

									String merchantAmount = String.format("%.2f", Double
											.parseDouble(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
											- (Double.parseDouble(dbobj.getString(FieldType.PG_TDR_SC.toString()))
													+ Double.parseDouble(
															dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
													+ Double.parseDouble(dbobj.getString(FieldType.PG_GST.toString()))
													+ Double.parseDouble(
															dbobj.getString(FieldType.ACQUIRER_GST.toString()))));

									transReport.setDoctor(
											String.valueOf(new BigDecimal(dbobj.getString(FieldType.UDF7.getName()))
													.multiply(new BigDecimal(merchantAmount))
													.divide(new BigDecimal(Constants.MAX_NUMBER_OF_KEYS.getValue()))
													.setScale(2, BigDecimal.ROUND_HALF_UP)));
								} else {
									transReport.setDoctor("0");
								}

							} else {
								transReport.setDoctor("0");
							}
						} else {
							transReport.setDoctor("0");
						}

						if (dbobj.containsKey(FieldType.UDF8.getName())) {
							if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF8.getName()))) {

								if (StringUtils.isNotBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
										&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.toString()))
										&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
										&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.toString()))
										&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.toString()))) {

									String merchantAmount = String.format("%.2f", Double
											.parseDouble(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
											- (Double.parseDouble(dbobj.getString(FieldType.PG_TDR_SC.toString()))
													+ Double.parseDouble(
															dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
													+ Double.parseDouble(dbobj.getString(FieldType.PG_GST.toString()))
													+ Double.parseDouble(
															dbobj.getString(FieldType.ACQUIRER_GST.toString()))));

									transReport.setGlocal(
											String.valueOf(new BigDecimal(dbobj.getString(FieldType.UDF8.getName()))
													.multiply(new BigDecimal(merchantAmount))
													.divide(new BigDecimal(Constants.MAX_NUMBER_OF_KEYS.getValue()))
													.setScale(2, BigDecimal.ROUND_HALF_UP)));
								} else {
									transReport.setGlocal("0");
								}
							} else {
								transReport.setGlocal("0");
							}
						} else {
							transReport.setGlocal("0");
						}

						if (dbobj.containsKey(FieldType.UDF9.getName())) {
							if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF9.getName()))) {

								if (StringUtils.isNotBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
										&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.toString()))
										&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
										&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.toString()))
										&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.toString()))) {

									String merchantAmount = String.format("%.2f", Double
											.parseDouble(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
											- (Double.parseDouble(dbobj.getString(FieldType.PG_TDR_SC.toString()))
													+ Double.parseDouble(
															dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
													+ Double.parseDouble(dbobj.getString(FieldType.PG_GST.toString()))
													+ Double.parseDouble(
															dbobj.getString(FieldType.ACQUIRER_GST.toString()))));

									transReport.setPartner(
											String.valueOf(new BigDecimal(dbobj.getString(FieldType.UDF9.getName()))
													.multiply(new BigDecimal(merchantAmount))
													.divide(new BigDecimal(Constants.MAX_NUMBER_OF_KEYS.getValue()))
													.setScale(2, BigDecimal.ROUND_HALF_UP)));
								} else {
									transReport.setPartner("0");
								}
							} else {
								transReport.setPartner("0");
							}
						} else {
							transReport.setPartner("0");
						}

						if (dbobj.containsKey(FieldType.UDF11.getName())) {
							if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF11.getName()))) {
								transReport.setUniqueId(dbobj.getString(FieldType.UDF11.getName()));
							} else {
								transReport.setUniqueId("NA");
							}
						} else {
							transReport.setUniqueId("NA");
						}
					}

					if (StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_CHARGES.getName()))
							&& StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_GST.getName()))) {

						if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.getName()))
								&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.getName()))
								&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.getName()))
								&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.getName()))) {

							transReport.setMerchantAmount(
									String.valueOf(new BigDecimal(dbobj.getString(FieldType.PG_GST.getName()))
											.add(new BigDecimal(dbobj.getString(FieldType.PG_TDR_SC.getName())))
											.add(new BigDecimal(dbobj.getString(FieldType.ACQUIRER_TDR_SC.getName())))
											.add(new BigDecimal(dbobj.getString(FieldType.ACQUIRER_GST.getName())))
											.add(new BigDecimal(dbobj.getString(FieldType.RESELLER_CHARGES.getName())))
											.add(new BigDecimal(dbobj.getString(FieldType.RESELLER_GST.getName())))));

							if (transReport.getMerchantAmount().equalsIgnoreCase("0.00")) {
								transReport.setMerchantAmount(transReport.getMerchantAmount());
							} else {
								transReport.setMerchantAmount("-" + transReport.getMerchantAmount());
							}

						} else {
							transReport.setMerchantAmount(CrmFieldConstants.NA.getValue());
						}

					} else {

						if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.getName()))
								&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.getName()))
								&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.getName()))
								&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.getName()))) {

							transReport.setMerchantAmount(
									String.valueOf(new BigDecimal(dbobj.getString(FieldType.PG_GST.getName()))
											.add(new BigDecimal(dbobj.getString(FieldType.PG_TDR_SC.getName())))
											.add(new BigDecimal(dbobj.getString(FieldType.ACQUIRER_TDR_SC.getName())))
											.add(new BigDecimal(dbobj.getString(FieldType.ACQUIRER_GST.getName())))));

							if (transReport.getMerchantAmount().equalsIgnoreCase("0.00")) {
								transReport.setMerchantAmount(transReport.getMerchantAmount());
							} else {
								transReport.setMerchantAmount("-" + transReport.getMerchantAmount());
							}

						} else {
							transReport.setMerchantAmount(CrmFieldConstants.NA.getValue());
						}
					}

				} else if (StringUtils.isNotBlank(dbobj.getString(FieldType.PAYMENT_TYPE.toString()))
						&& dbobj.getString(FieldType.PAYMENT_TYPE.getName()).equalsIgnoreCase(PaymentType.COD.getCode())
						&& StringUtils.isNotBlank(dbobj.getString(FieldType.ORIG_TXNTYPE.toString()))
						&& dbobj.getString(FieldType.ORIG_TXNTYPE.getName())
								.equalsIgnoreCase(TxnType.REFUND.getName())) {
					transReport.setMerchantAmount("0.00");
					transReport.setTdrOrSurcharge("0.00");
					transReport.setGst("0.00");
				} else {
					if (StringUtils.isNotBlank(dbobj.getString(FieldType.ORIG_TXNTYPE.toString())) && dbobj
							.getString(FieldType.ORIG_TXNTYPE.getName()).equalsIgnoreCase(TxnType.REFUND.getName())) {

						if (StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_CHARGES.getName()))
								&& StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_GST.getName()))) {

							if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.getName()))
									&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.getName()))
									&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.getName()))
									&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.getName()))) {

								transReport.setMerchantAmount("-" + String.valueOf(
										new BigDecimal(dbobj.getString(FieldType.TOTAL_AMOUNT.getName())).subtract(
												new BigDecimal(dbobj.getString(FieldType.ACQUIRER_TDR_SC.getName()))
														.add(new BigDecimal(
																dbobj.getString(FieldType.ACQUIRER_GST.getName())))
														.add(new BigDecimal(
																dbobj.getString(FieldType.PG_TDR_SC.getName())))
														.add(new BigDecimal(
																dbobj.getString(FieldType.PG_GST.getName())))
														.add(new BigDecimal(
																dbobj.getString(FieldType.RESELLER_CHARGES.getName())))
														.add(new BigDecimal(
																dbobj.getString(FieldType.RESELLER_GST.getName()))))));
							} else {
								transReport.setMerchantAmount(CrmFieldConstants.NA.getValue());
							}

						} else {
							if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.getName()))
									&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.getName()))
									&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.getName()))
									&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.getName()))) {

								transReport.setMerchantAmount("-" + String.valueOf(
										new BigDecimal(dbobj.getString(FieldType.TOTAL_AMOUNT.getName())).subtract(
												new BigDecimal(dbobj.getString(FieldType.ACQUIRER_TDR_SC.getName()))
														.add(new BigDecimal(
																dbobj.getString(FieldType.ACQUIRER_GST.getName())))
														.add(new BigDecimal(
																dbobj.getString(FieldType.PG_TDR_SC.getName()))
																		.add(new BigDecimal(dbobj.getString(
																				FieldType.PG_GST.getName())))))));
							} else {
								transReport.setMerchantAmount(CrmFieldConstants.NA.getValue());
							}
						}

					} else {

						if (isGlocal == true) {

							if (dbobj.containsKey(FieldType.UDF7.getName())) {

								if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF7.getName()))) {

									if (StringUtils.isNotBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
											&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.toString()))
											&& StringUtils
													.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
											&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.toString()))
											&& StringUtils
													.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.toString()))) {

										String merchantAmount = String.format("%.2f", Double
												.parseDouble(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
												- (Double.parseDouble(dbobj.getString(FieldType.PG_TDR_SC.toString()))
														+ Double.parseDouble(
																dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
														+ Double.parseDouble(
																dbobj.getString(FieldType.PG_GST.toString()))
														+ Double.parseDouble(
																dbobj.getString(FieldType.ACQUIRER_GST.toString()))));

										transReport.setDoctor(
												String.valueOf(new BigDecimal(dbobj.getString(FieldType.UDF7.getName()))
														.multiply(new BigDecimal(merchantAmount))
														.divide(new BigDecimal(Constants.MAX_NUMBER_OF_KEYS.getValue()))
														.setScale(2, BigDecimal.ROUND_HALF_UP)));
									} else {
										transReport.setDoctor("0");
									}

								} else {
									transReport.setDoctor("0");
								}
							} else {
								transReport.setDoctor("0");
							}

							if (dbobj.containsKey(FieldType.UDF8.getName())) {
								if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF8.getName()))) {

									if (StringUtils.isNotBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
											&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.toString()))
											&& StringUtils
													.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
											&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.toString()))
											&& StringUtils
													.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.toString()))) {

										String merchantAmount = String.format("%.2f", Double
												.parseDouble(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
												- (Double.parseDouble(dbobj.getString(FieldType.PG_TDR_SC.toString()))
														+ Double.parseDouble(
																dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
														+ Double.parseDouble(
																dbobj.getString(FieldType.PG_GST.toString()))
														+ Double.parseDouble(
																dbobj.getString(FieldType.ACQUIRER_GST.toString()))));

										transReport.setGlocal(
												String.valueOf(new BigDecimal(dbobj.getString(FieldType.UDF8.getName()))
														.multiply(new BigDecimal(merchantAmount))
														.divide(new BigDecimal(Constants.MAX_NUMBER_OF_KEYS.getValue()))
														.setScale(2, BigDecimal.ROUND_HALF_UP)));
									} else {
										transReport.setGlocal("0");
									}
								} else {
									transReport.setGlocal("0");
								}
							} else {
								transReport.setGlocal("0");
							}

							if (dbobj.containsKey(FieldType.UDF9.getName())) {
								if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF9.getName()))) {

									if (StringUtils.isNotBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
											&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.toString()))
											&& StringUtils
													.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
											&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.toString()))
											&& StringUtils
													.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.toString()))) {

										String merchantAmount = String.format("%.2f", Double
												.parseDouble(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
												- (Double.parseDouble(dbobj.getString(FieldType.PG_TDR_SC.toString()))
														+ Double.parseDouble(
																dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
														+ Double.parseDouble(
																dbobj.getString(FieldType.PG_GST.toString()))
														+ Double.parseDouble(
																dbobj.getString(FieldType.ACQUIRER_GST.toString()))));

										transReport.setPartner(
												String.valueOf(new BigDecimal(dbobj.getString(FieldType.UDF9.getName()))
														.multiply(new BigDecimal(merchantAmount))
														.divide(new BigDecimal(Constants.MAX_NUMBER_OF_KEYS.getValue()))
														.setScale(2, BigDecimal.ROUND_HALF_UP)));
									} else {
										transReport.setPartner("0");
									}
								} else {
									transReport.setPartner("0");
								}
							} else {
								transReport.setPartner("0");
							}

							if (dbobj.containsKey(FieldType.UDF11.getName())) {
								if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF11.getName()))) {
									transReport.setUniqueId(dbobj.getString(FieldType.UDF11.getName()));
								} else {
									transReport.setUniqueId("NA");
								}
							} else {
								transReport.setUniqueId("NA");
							}
						}

						if (StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_CHARGES.getName()))
								&& StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_GST.getName()))) {

							if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.getName()))
									&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.getName()))
									&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.getName()))
									&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.getName()))) {

								transReport.setMerchantAmount(String.valueOf(
										new BigDecimal(dbobj.getString(FieldType.TOTAL_AMOUNT.getName())).subtract(
												new BigDecimal(dbobj.getString(FieldType.ACQUIRER_TDR_SC.getName()))
														.add(new BigDecimal(
																dbobj.getString(FieldType.ACQUIRER_GST.getName())))
														.add(new BigDecimal(
																dbobj.getString(FieldType.PG_TDR_SC.getName())))
														.add(new BigDecimal(
																dbobj.getString(FieldType.PG_GST.getName())))
														.add(new BigDecimal(
																dbobj.getString(FieldType.RESELLER_CHARGES.getName())))
														.add(new BigDecimal(
																dbobj.getString(FieldType.RESELLER_GST.getName()))))));
							} else {
								transReport.setMerchantAmount(CrmFieldConstants.NA.getValue());
							}
						} else {
							if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.getName()))
									&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.getName()))
									&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.getName()))
									&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.getName()))) {

								transReport.setMerchantAmount(String.valueOf(
										new BigDecimal(dbobj.getString(FieldType.TOTAL_AMOUNT.getName())).subtract(
												new BigDecimal(dbobj.getString(FieldType.ACQUIRER_TDR_SC.getName()))
														.add(new BigDecimal(
																dbobj.getString(FieldType.ACQUIRER_GST.getName())))
														.add(new BigDecimal(
																dbobj.getString(FieldType.PG_TDR_SC.getName()))
																		.add(new BigDecimal(dbobj.getString(
																				FieldType.PG_GST.getName())))))));
							} else {
								transReport.setMerchantAmount(CrmFieldConstants.NA.getValue());
							}
						}

					}
				}

				if (null != dbobj.getString(FieldType.MOP_TYPE.toString())) {
					transReport.setMoptype(MopType.getmopName(dbobj.getString(FieldType.MOP_TYPE.toString())));
				} else {
					transReport.setMoptype(CrmFieldConstants.NA.getValue());
				}

				if (null != dbobj.getString(FieldType.CARD_HOLDER_TYPE.toString())) {
					transReport.setCardHolderType(dbobj.getString(FieldType.CARD_HOLDER_TYPE.toString()));
				} else {
					transReport.setCardHolderType(CrmFieldConstants.NA.toString());
				}
				// add by vishal
				String sufGst = dbobj.getString(FieldType.SUF_GST.toString());
				if (sufGst != null && !sufGst.equalsIgnoreCase("false") && transReport.getMerchantAmount() != null
						&& transReport.getMerchantAmount() != "NA") {
					transReport.setSufGst(dbobj.getString(FieldType.SUF_GST.toString()));
					transReport.setMerchantAmount(String.valueOf(new BigDecimal(transReport.getMerchantAmount())
							.subtract(new BigDecimal(dbobj.getString(FieldType.SUF_GST.toString())))));
				} else {
					transReport.setSufGst(CrmFieldConstants.NA.getValue());
				}

				String sufTdr = dbobj.getString(FieldType.SUF_TDR.toString());
				if (sufTdr != null && !sufTdr.equalsIgnoreCase("false") && transReport.getMerchantAmount() != null
						&& transReport.getMerchantAmount() != "NA") {
					transReport.setSufTdr(dbobj.getString(FieldType.SUF_TDR.toString()));
					transReport.setMerchantAmount(String.valueOf(new BigDecimal(transReport.getMerchantAmount())
							.subtract(new BigDecimal(dbobj.getString(FieldType.SUF_TDR.toString())))));

				} else {
					transReport.setSufTdr(CrmFieldConstants.NA.getValue());
				}

				transactionList.add(transReport);
			}
			logger.info("transactionList created and size = " + transactionList.size());
			cursor.close();
			logger.info(
					"Inside TxnReports , searchPaymentForDownload , transactionListSize = " + transactionList.size());
			Comparator<PaymentSearchDownloadObject> comp = (PaymentSearchDownloadObject a,
					PaymentSearchDownloadObject b) -> {
				if (a.getDateFrom().compareTo(b.getDateFrom()) > 0) {
					return -1;
				} else if (a.getDateFrom().compareTo(b.getDateFrom()) < 0) {
					return 1;
				} else {
					return 0;
				}
			};
			Collections.sort(transactionList, comp);
			logger.info("transactionList created and Sorted");
			return transactionList;
		} catch (Exception e) {
			logger.error("Exception occured in TxnReports , searchPayment , Exception = ", e);
			return transactionList;
		}
	}

	public List<PaymentSearchDownloadObject> unsettledCapturedForDownload(String merchantPayId, String subMerchantPayId,
			String customerEmail, String paymentType, String currency, String fromDate, String toDate, User user,
			String paymentsRegion, String acquirer, String partSettleFlag, boolean isGlocal, Set<String> orderIdSet,
			String transactionFlag, String deltaFlag, String pgRefNum, String orderId, String autoRefund) {

		logger.info("Inside TxnReports , searchPaymentForDownload");
		Map<String, User> userMap = new HashMap<String, User>();
		boolean isParameterised = false;

		List<PaymentSearchDownloadObject> transactionList = new ArrayList<PaymentSearchDownloadObject>();
		try {
			UserSettingData userSettings = userSettingDao.fetchDataUsingPayId(user.getPayId());

			List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();
			List<BasicDBObject> settledFlagQueryList = new ArrayList<BasicDBObject>();
			List<BasicDBObject> txnTypeQueryList = new ArrayList<BasicDBObject>();
			List<String> acquirerConditionLst = new ArrayList<String>();
			BasicDBObject acquirerQuery = new BasicDBObject();
			BasicDBObject customerQuery = new BasicDBObject();
			BasicDBObject customerQueryMask = new BasicDBObject();
			BasicDBObject txnCapturedFlag = new BasicDBObject();
			BasicDBObject dateIndexConditionQuery = new BasicDBObject();

			DateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.ENGLISH);
			Date dateStart = format.parse(fromDate);
			Date dateEnd = format.parse(toDate);

			LocalDate startDate = dateStart.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			LocalDate endDate = dateEnd.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

			String fromDateIndex = startDate.toString().replaceAll("-", "");
			String toDateIndex = endDate.toString().replaceAll("-", "");

			dateIndexConditionQuery.put("DATE_INDEX",
					BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(fromDateIndex).toLocalizedPattern())
							.add("$lte", new SimpleDateFormat(toDateIndex).toLocalizedPattern()).get());

			BasicDBObject orderIdConditionQuery = null;
			if (orderIdSet != null) {
				List<String> orderIdList = new ArrayList<>(orderIdSet);
				orderIdConditionQuery = new BasicDBObject("$in", orderIdList);
			}

			if (!orderId.isEmpty()) {
				paramConditionLst.add(new BasicDBObject(FieldType.ORDER_ID.getName(), orderId));
			} else if (null != orderIdConditionQuery) {
				paramConditionLst.add(new BasicDBObject(FieldType.ORDER_ID.getName(), orderIdConditionQuery));
			}

			if (!pgRefNum.isEmpty()) {
				paramConditionLst.add(new BasicDBObject(FieldType.PG_REF_NUM.getName(), pgRefNum));
			}

			if (StringUtils.isNotBlank(merchantPayId) && !merchantPayId.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.PAY_ID.getName(), merchantPayId));
			}

			if (StringUtils.isNotBlank(subMerchantPayId) && !subMerchantPayId.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.SUB_MERCHANT_ID.getName(), subMerchantPayId));
			}

			if (StringUtils.isNotEmpty(customerEmail) && !customerEmail.isEmpty()
					&& !customerEmail.equalsIgnoreCase("ALL")) {
				isParameterised = true;
				customerQuery.append(FieldType.CUST_EMAIL.getName(), customerEmail);
				if (StringUtils.isNotBlank(customerEmail)) {
					String email = customerEmail;
					String emailMask = null;
					final String mask = "*****";
					final int at = email.indexOf("@");
					if (at > 2) {
						final int maskLen = Math.min(Math.max(at / 2, 2), 5);
						final int start1 = (at - maskLen) / 2;
						emailMask = email.substring(0, start1) + mask.substring(0, maskLen)
								+ email.substring(start1 + maskLen);
					}
					customerQueryMask.append(FieldType.CUST_EMAIL.getName(), emailMask);
				}
			}

			BasicDBObject settledCheckQuery = new BasicDBObject();
			settledFlagQueryList
					.add(new BasicDBObject(FieldType.SETTLEMENT_FLAG.getName(), new BasicDBObject("$exists", false)));
			settledFlagQueryList.add(new BasicDBObject(FieldType.SETTLEMENT_FLAG.getName(), null));
			settledCheckQuery.put("$or", settledFlagQueryList);
			paramConditionLst.add(settledCheckQuery);

			paramConditionLst.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.CAPTURED.getName()));

			if (StringUtils.isNotBlank(currency) && !currency.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.CURRENCY_CODE.getName(), currency));
			}

			if (StringUtils.isNotBlank(partSettleFlag) && !partSettleFlag.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.PART_SETTLE.getName(), partSettleFlag));
			}

			BasicDBObject txntype = new BasicDBObject();
			txnTypeQueryList.add(new BasicDBObject(FieldType.ORIG_TXNTYPE.getName(), TransactionType.SALE.getName()));
			txnTypeQueryList.add(new BasicDBObject(FieldType.ORIG_TXNTYPE.getName(), TransactionType.REFUND.getName()));
			txntype.put("$or", txnTypeQueryList);
			paramConditionLst.add(txntype);
			if (StringUtils.isNotBlank(paymentType) && !paymentType.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.PAYMENT_TYPE.getName(), paymentType));
			}

			if (StringUtils.isNotBlank(paymentsRegion) && !paymentsRegion.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.PAYMENTS_REGION.getName(), paymentsRegion));
			}

			if (StringUtils.isNotBlank(transactionFlag) && !transactionFlag.equalsIgnoreCase("ALL")) {
				txnCapturedFlag.append("$in", transactionFlag.split(","));
				paramConditionLst.add(new BasicDBObject(FieldType.TXN_CAPTURE_FLAG.getName(), txnCapturedFlag));
			}

			if (StringUtils.isNotBlank(deltaFlag) && !deltaFlag.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.DELTA_REFUND_FLAG.getName(), deltaFlag));
			}

			if (StringUtils.isNotBlank(autoRefund) && !autoRefund.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.AUTO_REFUND_FLAG.getName(), autoRefund));
			}

			if (user.getUserType().equals(UserType.RESELLER)) {
				paramConditionLst.add(new BasicDBObject(FieldType.RESELLER_ID.getName(), user.getResellerId()));
			}

			if (StringUtils.isNotBlank(acquirer) && !acquirer.equalsIgnoreCase("ALL")) {
				List<String> acquirerList = Arrays.asList(acquirer.split(","));
				for (String acq : acquirerList) {
					acquirerConditionLst.add(acq.trim());
				}
				acquirerQuery.append("$in", acquirerConditionLst);
				paramConditionLst.add(new BasicDBObject(FieldType.ACQUIRER_TYPE.getName(), acquirerQuery));
			}

			List<BasicDBObject> fianlQueryList = new ArrayList<BasicDBObject>();

			if (StringUtils.isNotBlank(pgRefNum) || StringUtils.isNotBlank(orderId)) {

			} else {
				if (!dateIndexConditionQuery.isEmpty()) {
					paramConditionLst.add(dateIndexConditionQuery);
				}
			}

			fianlQueryList.addAll(paramConditionLst);
			if (!customerQuery.isEmpty()) {
				paramConditionLst.add(customerQuery);
			}

			if (!customerQueryMask.isEmpty()) {
				fianlQueryList.add(customerQueryMask);
			}

			BasicDBObject finalquery = new BasicDBObject("$and", paramConditionLst);
			BasicDBObject finalqueryMask = new BasicDBObject("$and", fianlQueryList);

			if (!customerQueryMask.isEmpty()) {
				logger.info("Inside TxnReports , searchPaymentForDownload , finalQuery = " + finalqueryMask);
			} else {
				logger.info("Inside TxnReports , searchPaymentForDownload , finalQuery = " + finalquery);
			}

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					propertiesManager.propertiesMap.get(prefix + Constants.TRANSACTION_STATUS_COLLECTION.getValue()));
			BasicDBObject match = new BasicDBObject("$match", finalquery);
			// Now the aggregate operation ()In case any parameter is passed in
			// search query
			// , then show all records
			BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject("CREATE_DATE", -1));

			List<BasicDBObject> pipeline = Arrays.asList(match, sort);

			AggregateIterable<Document> output = coll.aggregate(pipeline);
			output.allowDiskUse(true);
			MongoCursor<Document> cursor = output.iterator();
			int count = 0;
			while (cursor.hasNext()) {
				Document dbobj = cursor.next();
				if (StringUtils.isNotBlank(dbobj.getString("IS_ENCRYPTED"))
						&& dbobj.getString("IS_ENCRYPTED").equalsIgnoreCase("Y")) {
					dbobj = dataEncDecTool.decryptDocument(dbobj);
				}
				PaymentSearchDownloadObject transReport = new PaymentSearchDownloadObject();
				transReport.setTransactionId(dbobj.getString(FieldType.TXN_ID.toString()));
				transReport.setPgRefNum(dbobj.getString(FieldType.PG_REF_NUM.toString()));
				if (StringUtils.isNotBlank(dbobj.getString(FieldType.PAYMENTS_REGION.getName()))) {
					transReport.setTransactionRegion(dbobj.getString(FieldType.PAYMENTS_REGION.getName()));
				} else {
					transReport.setTransactionRegion(CrmFieldConstants.NA.getValue());
				}
				transReport.setMerchants(dbobj.getString(CrmFieldType.BUSINESS_NAME.getName()));

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.TXN_CAPTURE_FLAG.getName()))) {
					transReport.setTxnSettledType(dbobj.getString(FieldType.TXN_CAPTURE_FLAG.getName()));
				} else {
					transReport.setTxnSettledType(CrmFieldConstants.NA.getValue());
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.TRANSACTION_MODE.getName()))) {
					transReport.setTransactionMode(dbobj.getString(FieldType.TRANSACTION_MODE.getName()));
				} else {
				}

				// if
				// (StringUtils.isNotBlank(dbobj.getString(FieldType.POST_SETTLED_FLAG.getName())))
				// {
				// transReport.setPostSettledFlag(dbobj.getString(FieldType.POST_SETTLED_FLAG.getName()));
				// } else {
				// transReport.setPostSettledFlag(CrmFieldConstants.NA.getValue());
				// }

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.PART_SETTLE.toString()))
						&& dbobj.getString(FieldType.PART_SETTLE.toString()).equalsIgnoreCase(("Y"))) {
					transReport.setPartSettle(dbobj.getString(FieldType.PART_SETTLE.toString()));
				} else {
					transReport.setPartSettle(CrmFieldConstants.NA.getValue());
				}

				// transReport.setCardMask(dbobj.getString(FieldType.CARD_MASK.getName()));
				if (null != dbobj.getString(FieldType.PAYMENT_TYPE.toString())) {
					if (null != dbobj.getString(FieldType.CARD_MASK.toString())) {
						transReport.setCardMask(dbobj.getString(FieldType.CARD_MASK.toString()));
					} else if (null != dbobj.getString(FieldType.PAYER_ADDRESS.getName())) {

						if (user.getUserType().equals(UserType.ADMIN) || user.getUserType().equals(UserType.SUBADMIN)) {
							transReport.setCardMask(dbobj.getString(FieldType.PAYER_ADDRESS.getName()));
						} else {
							String vpaString = dbobj.getString(FieldType.PAYER_ADDRESS.getName());
							String[] vpaArray = vpaString.split("@");
							char[] vpaChar = vpaArray[0].toCharArray();
							StringBuilder vpastrBuilder = new StringBuilder();

							if (vpaChar.length > 3) {
								for (int i = 0; i < vpaChar.length - 3; i++) {
									vpastrBuilder.append(vpaChar[i]);
								}
								vpastrBuilder.append("***@");
								vpastrBuilder.append(vpaArray[1]);
							} else {
								vpastrBuilder.append(vpaChar[0]);
								vpastrBuilder.append("**@");
								vpastrBuilder.append(vpaArray[1]);
							}

							transReport.setCardMask(vpastrBuilder.toString());
						}
					} else {
						transReport.setCardMask(CrmFieldConstants.NA.getValue());
					}
					// else if
					// ((dbobj.getString(FieldType.PAYMENT_TYPE.getName()))
					// .equals(PaymentType.WALLET.getCode())) {
					// transReport.setCardMask(CrmFieldConstants.WALLET.getValue());
					// }
				} else {
					transReport.setCardMask(CrmFieldConstants.NA.getValue());
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.CUST_NAME.getName()))) {
					transReport.setCustName(dbobj.getString(FieldType.CUST_NAME.getName()));
				} else {
					transReport.setCustName("NA");
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.CUST_EMAIL.getName()))) {
					transReport.setCustEmail(dbobj.getString(FieldType.CUST_EMAIL.getName()));
				} else {
					transReport.setCustEmail("NA");
				}
				if (StringUtils.isNotBlank(dbobj.getString(FieldType.PAYOUT_DATE.toString()))) {
					transReport.setPayOutDate((dbobj.getString(FieldType.PAYOUT_DATE.toString())));
				} else {
					transReport.setPayOutDate(CrmFieldConstants.NA.getValue());
				}
				if (StringUtils.isNotBlank(dbobj.getString(FieldType.UTR_NO.toString()))) {
					transReport.setUtrNo(dbobj.getString(FieldType.UTR_NO.toString()));
				} else {
					transReport.setUtrNo(CrmFieldConstants.NA.getValue());
				}
				/*
				 * if (dbobj.containsKey("CUSTOM_FLAG") &&
				 * StringUtils.isNotBlank(dbobj.getString("CUSTOM_FLAG"))) {
				 * transReport.setCustomFlag(((Document) dbobj).getString("CUSTOM_FLAG")); }
				 * else { transReport.setCustomFlag("N"); }
				 */

				User user1 = new User();

				if (StringUtils.isNotBlank((String) dbobj.get(FieldType.PAY_ID.getName()))) {
					String payid = (String) dbobj.get(FieldType.PAY_ID.getName());

					if (userMap.get(payid) != null && !userMap.get(payid).getPayId().isEmpty()) {
						user1 = userMap.get(payid);
					} else {
						user1 = userdao.findPayId(payid);
						userMap.put(payid, user1);
					}
				}

				transReport.setMerchants(user1.getBusinessName());

				if (merchantPayId.equalsIgnoreCase("All")) {

					if (StringUtils.isNotBlank(dbobj.getString(FieldType.REFUND_DAYS.getName()))) {
						transReport.setRefundCycle(dbobj.getString(FieldType.REFUND_DAYS.getName()));
					} else {
						transReport.setRefundCycle(CrmFieldConstants.NA.getValue());
					}

					if (StringUtils.isNotBlank(dbobj.getString(FieldType.PRODUCT_PRICE.getName()))) {
						transReport.setProductPrice(dbobj.getString(FieldType.PRODUCT_PRICE.getName()));
					} else {
						transReport.setProductPrice(CrmFieldConstants.NA.getValue());
					}
					if (StringUtils.isNotBlank(dbobj.getString(FieldType.VENDOR_ID.getName()))) {
						transReport.setVendorID(dbobj.getString(FieldType.VENDOR_ID.getName()));
					} else {
						transReport.setVendorID(CrmFieldConstants.NA.getValue());
					}
					if (StringUtils.isNotBlank(dbobj.getString(FieldType.SKU_CODE.getName()))) {
						transReport.setSKUCode(dbobj.getString(FieldType.SKU_CODE.getName()));
					} else {
						transReport.setSKUCode(CrmFieldConstants.NA.getValue());
					}

					if (StringUtils.isNotBlank(dbobj.getString(FieldType.CATEGORY_CODE.getName()))) {
						transReport.setCategoryCode(dbobj.getString(FieldType.CATEGORY_CODE.getName()));
					} else {
						transReport.setCategoryCode(CrmFieldConstants.NA.getValue());
					}
				} else {

					if (user.getUserType().equals(UserType.ADMIN) || user.getUserType().equals(UserType.SUBADMIN)
							|| user.getUserType().equals(UserType.RESELLER)) {
						User merhant = userMap.get(merchantPayId);
						UserSettingData merchantSettings = userSettingDao.fetchDataUsingPayId(merhant.getPayId());

						if (merchantSettings.isRetailMerchantFlag()) {
							if (StringUtils.isNotBlank(dbobj.getString(FieldType.REFUND_DAYS.getName()))) {
								transReport.setRefundCycle(dbobj.getString(FieldType.REFUND_DAYS.getName()));
							} else {
								transReport.setRefundCycle(CrmFieldConstants.NA.getValue());
							}

							if (StringUtils.isNotBlank(dbobj.getString(FieldType.PRODUCT_PRICE.getName()))) {
								transReport.setProductPrice(dbobj.getString(FieldType.PRODUCT_PRICE.getName()));
							} else {
								transReport.setProductPrice(CrmFieldConstants.NA.getValue());
							}
							if (StringUtils.isNotBlank(dbobj.getString(FieldType.VENDOR_ID.getName()))) {
								transReport.setVendorID(dbobj.getString(FieldType.VENDOR_ID.getName()));
							} else {
								transReport.setVendorID(CrmFieldConstants.NA.getValue());
							}
							if (StringUtils.isNotBlank(dbobj.getString(FieldType.SKU_CODE.getName()))) {
								transReport.setSKUCode(dbobj.getString(FieldType.SKU_CODE.getName()));
							} else {
								transReport.setSKUCode(CrmFieldConstants.NA.getValue());
							}

							if (StringUtils.isNotBlank(dbobj.getString(FieldType.CATEGORY_CODE.getName()))) {
								transReport.setCategoryCode(dbobj.getString(FieldType.CATEGORY_CODE.getName()));
							} else {
								transReport.setCategoryCode(CrmFieldConstants.NA.getValue());
							}
						}
					} else {

						if (userSettings.isRetailMerchantFlag()) {
							if (StringUtils.isNotBlank(dbobj.getString(FieldType.REFUND_DAYS.getName()))) {
								transReport.setRefundCycle(dbobj.getString(FieldType.REFUND_DAYS.getName()));
							} else {
								transReport.setRefundCycle(CrmFieldConstants.NA.getValue());
							}

							if (StringUtils.isNotBlank(dbobj.getString(FieldType.PRODUCT_PRICE.getName()))) {
								transReport.setProductPrice(dbobj.getString(FieldType.PRODUCT_PRICE.getName()));
							} else {
								transReport.setProductPrice(CrmFieldConstants.NA.getValue());
							}
							if (StringUtils.isNotBlank(dbobj.getString(FieldType.VENDOR_ID.getName()))) {
								transReport.setVendorID(dbobj.getString(FieldType.VENDOR_ID.getName()));
							} else {
								transReport.setVendorID(CrmFieldConstants.NA.getValue());
							}
							if (StringUtils.isNotBlank(dbobj.getString(FieldType.SKU_CODE.getName()))) {
								transReport.setSKUCode(dbobj.getString(FieldType.SKU_CODE.getName()));
							} else {
								transReport.setSKUCode(CrmFieldConstants.NA.getValue());
							}

							if (StringUtils.isNotBlank(dbobj.getString(FieldType.CATEGORY_CODE.getName()))) {
								transReport.setCategoryCode(dbobj.getString(FieldType.CATEGORY_CODE.getName()));
							} else {
								transReport.setCategoryCode(CrmFieldConstants.NA.getValue());
							}
						}

					}

				}

				// if (!merchantPayId.equalsIgnoreCase("All") &&
				if (dbobj.containsKey(FieldType.SUB_MERCHANT_ID.getName())
						&& StringUtils.isNotBlank(dbobj.getString(FieldType.SUB_MERCHANT_ID.getName()))) {

					String subMerchant = dbobj.getString(FieldType.SUB_MERCHANT_ID.getName());
					User subMerchantUser = new User();

					if (userMap.get(subMerchant) != null && !userMap.get(subMerchant).getPayId().isEmpty()) {
						subMerchantUser = userMap.get(subMerchant);
					} else {
						subMerchantUser = userdao.findPayId(subMerchant);
						userMap.put(subMerchant, subMerchantUser);
					}
					if (subMerchantUser != null) {
						transReport.setSubMerchantId(subMerchantUser.getBusinessName());
					} else {
						transReport.setSubMerchantId(CrmFieldConstants.NA.getValue());
					}

				}

				if (null != dbobj.getString(FieldType.ORIG_TXNTYPE.toString())) {
					transReport.setTxnType(dbobj.getString(FieldType.ORIG_TXNTYPE.toString()));
				} else {
					// If ORIG_TXN_TYPE is not available incase of a timeout ,
					// set TXNTYPE instead
					// of ORIG_TXN_TYPE
					if (dbobj.getString(FieldType.STATUS.toString()).equalsIgnoreCase(StatusType.TIMEOUT.getName())) {
						transReport.setTxnType(dbobj.getString(FieldType.TXNTYPE.toString()));
					} else {
						transReport.setTxnType(CrmFieldConstants.NA.getValue());
					}

				}
				if (null != dbobj.getString(FieldType.ACQUIRER_TYPE.toString())) {
					transReport.setAcquirerType(dbobj.getString(FieldType.ACQUIRER_TYPE.toString()));
				} else {
					transReport.setAcquirerType(CrmFieldConstants.NA.getValue());
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.REFUND_ORDER_ID.toString()))) {
					transReport.setRefundOrderId(dbobj.getString(FieldType.REFUND_ORDER_ID.toString()));
				} else {
					transReport.setRefundOrderId(CrmFieldConstants.NA.getValue());
				}

				transReport.setAcquirerMode(dbobj.getString(FieldType.ACQUIRER_MODE.getName()));
				if (null != dbobj.getString(FieldType.PAYMENT_TYPE.toString())) {
					transReport.setPaymentMethods(
							PaymentType.getpaymentName(dbobj.getString(FieldType.PAYMENT_TYPE.toString())));
				} else {
					transReport.setPaymentMethods(CrmFieldConstants.NA.getValue());
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF11.getName()))) {
					transReport.setUDF11(dbobj.getString(FieldType.UDF11.getName()));
				} else {
					transReport.setUDF11(CrmFieldConstants.NA.getValue());
				}
				if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF12.getName()))) {
					transReport.setUDF12(dbobj.getString(FieldType.UDF12.getName()));
				} else {
					transReport.setUDF12(CrmFieldConstants.NA.getValue());
				}
				if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF13.getName()))) {
					transReport.setUDF13(dbobj.getString(FieldType.UDF13.getName()));
				} else {
					transReport.setUDF13(CrmFieldConstants.NA.getValue());
				}
				if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF14.getName()))) {
					transReport.setUDF14(dbobj.getString(FieldType.UDF14.getName()));
				} else {
					transReport.setUDF14(CrmFieldConstants.NA.getValue());
				}
				if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF15.getName()))) {
					transReport.setUDF15(dbobj.getString(FieldType.UDF15.getName()));
				} else {
					transReport.setUDF15(CrmFieldConstants.NA.getValue());
				}
				if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF16.getName()))) {
					transReport.setUDF16(dbobj.getString(FieldType.UDF16.getName()));
				} else {
					transReport.setUDF16(CrmFieldConstants.NA.getValue());
				}
				if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF17.getName()))) {
					transReport.setUDF17(dbobj.getString(FieldType.UDF17.getName()));
				} else {
					transReport.setUDF17(CrmFieldConstants.NA.getValue());
				}
				if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF18.getName()))) {
					transReport.setUDF18(dbobj.getString(FieldType.UDF18.getName()));
				} else {
					transReport.setUDF18(CrmFieldConstants.NA.getValue());
				}

				transReport.setStatus(dbobj.getString(FieldType.STATUS.toString()));
				if (StatusType.CAPTURED.getName().equalsIgnoreCase(dbobj.getString(FieldType.STATUS.getName()))) {
					transReport.setDateFrom(dbobj.getString(FieldType.CREATE_DATE.getName()));
				} else {
					transReport.setSettledDate(dbobj.getString(FieldType.CREATE_DATE.getName()));
					transReport.setDateFrom(dbobj.getString(FieldType.PG_DATE_TIME.getName()));
					if (StringUtils.isNotBlank(dbobj.getString(FieldType.RRN.getName()))
							&& !dbobj.getString(FieldType.RRN.getName()).equalsIgnoreCase("-")) {
						transReport.setRrn(dbobj.getString(FieldType.RRN.getName()));
					} else {
						transReport.setRrn(CrmFieldConstants.NA.getValue());
					}
				}

				transReport.setAmount(dbobj.getString(FieldType.AMOUNT.toString()));
				transReport.setOrderId(dbobj.getString(FieldType.ORDER_ID.toString()));
				if (dbobj.getString(FieldType.TOTAL_AMOUNT.toString()) != null) {
					transReport.setTotalAmount(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()));
				} else {
					transReport.setTotalAmount("");
				}

				// changes regarding partner reseller
				if (user.getUserType().equals(UserType.RESELLER) && user.isPartnerFlag()) {

					if (StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_CHARGES.toString()))) {

						transReport.setResellerCharges(String.format("%.2f",
								(Double.parseDouble(dbobj.getString(FieldType.RESELLER_CHARGES.toString())))));

					} else {
						transReport.setResellerCharges("0.00");
					}

					if (StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_GST.toString()))) {
						transReport.setResellerGST(String.format("%.2f",
								(Double.parseDouble(dbobj.getString(FieldType.RESELLER_GST.toString())))));

					} else {
						transReport.setResellerGST("0.00");
					}

					if (StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_CHARGES.toString()))) {
						if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
								&& (StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.toString())))) {

							transReport.setTdrOrSurcharge(String.format("%.2f",
									(Double.parseDouble(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
											+ Double.parseDouble(dbobj.getString(FieldType.PG_TDR_SC.toString()))
											+ Double.parseDouble(
													dbobj.getString(FieldType.RESELLER_CHARGES.toString())))));

						} else {
							transReport.setTdrOrSurcharge("0.00");
						}
					} else {
						if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
								&& (StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.toString())))) {

							transReport.setTdrOrSurcharge(String.format("%.2f",
									(Double.parseDouble(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
											+ Double.parseDouble(dbobj.getString(FieldType.PG_TDR_SC.toString())))));

						} else {
							transReport.setTdrOrSurcharge("0.00");
						}
					}

					if (StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_GST.toString()))) {
						if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.toString()))
								&& (StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.toString())))) {
							transReport.setGst(String.format("%.2f",
									(Double.parseDouble(dbobj.getString(FieldType.ACQUIRER_GST.toString()))
											+ Double.parseDouble(dbobj.getString(FieldType.PG_GST.toString()))
											+ Double.parseDouble(dbobj.getString(FieldType.RESELLER_GST.toString())))));

						} else {
							transReport.setGst("0.00");
						}
					} else {
						if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.toString()))
								&& (StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.toString())))) {
							transReport.setGst(String.format("%.2f",
									(Double.parseDouble(dbobj.getString(FieldType.ACQUIRER_GST.toString()))
											+ Double.parseDouble(dbobj.getString(FieldType.PG_GST.toString())))));

						} else {
							transReport.setGst("0.00");
						}
					}

				} // END

				else {

					if (StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_CHARGES.toString()))) {
						if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
								&& (StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.toString())))) {

							transReport.setTdrOrSurcharge(String.format("%.2f",
									(Double.parseDouble(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
											+ Double.parseDouble(dbobj.getString(FieldType.PG_TDR_SC.toString()))
											+ Double.parseDouble(
													dbobj.getString(FieldType.RESELLER_CHARGES.toString())))));

						} else {
							transReport.setTdrOrSurcharge("0.00");
						}
					} else {
						if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
								&& (StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.toString())))) {

							transReport.setTdrOrSurcharge(String.format("%.2f",
									(Double.parseDouble(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
											+ Double.parseDouble(dbobj.getString(FieldType.PG_TDR_SC.toString())))));

						} else {
							transReport.setTdrOrSurcharge("0.00");
						}
					}

					if (StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_GST.toString()))) {
						if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.toString()))
								&& (StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.toString())))) {
							transReport.setGst(String.format("%.2f",
									(Double.parseDouble(dbobj.getString(FieldType.ACQUIRER_GST.toString()))
											+ Double.parseDouble(dbobj.getString(FieldType.PG_GST.toString()))
											+ Double.parseDouble(dbobj.getString(FieldType.RESELLER_GST.toString())))));

						} else {
							transReport.setGst("0.00");
						}
					} else {
						if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.toString()))
								&& (StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.toString())))) {
							transReport.setGst(String.format("%.2f",
									(Double.parseDouble(dbobj.getString(FieldType.ACQUIRER_GST.toString()))
											+ Double.parseDouble(dbobj.getString(FieldType.PG_GST.toString())))));

						} else {
							transReport.setGst("0.00");
						}
					}

				}
				// end else

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.PAYMENT_TYPE.toString()))
						&& dbobj.getString(FieldType.PAYMENT_TYPE.getName()).equalsIgnoreCase(PaymentType.COD.getCode())
						&& StringUtils.isNotBlank(dbobj.getString(FieldType.ORIG_TXNTYPE.toString()))
						&& dbobj.getString(FieldType.ORIG_TXNTYPE.getName()).equalsIgnoreCase(TxnType.SALE.getName())) {

					if (isGlocal == true) {

						if (dbobj.containsKey(FieldType.UDF7.getName())) {

							if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF7.getName()))) {

								if (StringUtils.isNotBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
										&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.toString()))
										&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
										&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.toString()))
										&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.toString()))) {

									String merchantAmount = String.format("%.2f", Double
											.parseDouble(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
											- (Double.parseDouble(dbobj.getString(FieldType.PG_TDR_SC.toString()))
													+ Double.parseDouble(
															dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
													+ Double.parseDouble(dbobj.getString(FieldType.PG_GST.toString()))
													+ Double.parseDouble(
															dbobj.getString(FieldType.ACQUIRER_GST.toString()))));

									transReport.setDoctor(
											String.valueOf(new BigDecimal(dbobj.getString(FieldType.UDF7.getName()))
													.multiply(new BigDecimal(merchantAmount))
													.divide(new BigDecimal(Constants.MAX_NUMBER_OF_KEYS.getValue()))
													.setScale(2, BigDecimal.ROUND_HALF_UP)));
								} else {
									transReport.setDoctor("0");
								}

							} else {
								transReport.setDoctor("0");
							}
						} else {
							transReport.setDoctor("0");
						}

						if (dbobj.containsKey(FieldType.UDF8.getName())) {
							if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF8.getName()))) {

								if (StringUtils.isNotBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
										&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.toString()))
										&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
										&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.toString()))
										&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.toString()))) {

									String merchantAmount = String.format("%.2f", Double
											.parseDouble(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
											- (Double.parseDouble(dbobj.getString(FieldType.PG_TDR_SC.toString()))
													+ Double.parseDouble(
															dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
													+ Double.parseDouble(dbobj.getString(FieldType.PG_GST.toString()))
													+ Double.parseDouble(
															dbobj.getString(FieldType.ACQUIRER_GST.toString()))));

									transReport.setGlocal(
											String.valueOf(new BigDecimal(dbobj.getString(FieldType.UDF8.getName()))
													.multiply(new BigDecimal(merchantAmount))
													.divide(new BigDecimal(Constants.MAX_NUMBER_OF_KEYS.getValue()))
													.setScale(2, BigDecimal.ROUND_HALF_UP)));
								} else {
									transReport.setGlocal("0");
								}
							} else {
								transReport.setGlocal("0");
							}
						} else {
							transReport.setGlocal("0");
						}

						if (dbobj.containsKey(FieldType.UDF9.getName())) {
							if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF9.getName()))) {

								if (StringUtils.isNotBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
										&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.toString()))
										&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
										&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.toString()))
										&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.toString()))) {

									String merchantAmount = String.format("%.2f", Double
											.parseDouble(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
											- (Double.parseDouble(dbobj.getString(FieldType.PG_TDR_SC.toString()))
													+ Double.parseDouble(
															dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
													+ Double.parseDouble(dbobj.getString(FieldType.PG_GST.toString()))
													+ Double.parseDouble(
															dbobj.getString(FieldType.ACQUIRER_GST.toString()))));

									transReport.setPartner(
											String.valueOf(new BigDecimal(dbobj.getString(FieldType.UDF9.getName()))
													.multiply(new BigDecimal(merchantAmount))
													.divide(new BigDecimal(Constants.MAX_NUMBER_OF_KEYS.getValue()))
													.setScale(2, BigDecimal.ROUND_HALF_UP)));
								} else {
									transReport.setPartner("0");
								}
							} else {
								transReport.setPartner("0");
							}
						} else {
							transReport.setPartner("0");
						}

						if (dbobj.containsKey(FieldType.UDF11.getName())) {
							if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF11.getName()))) {
								transReport.setUniqueId(dbobj.getString(FieldType.UDF11.getName()));
							} else {
								transReport.setUniqueId("NA");
							}
						} else {
							transReport.setUniqueId("NA");
						}
					}

					if (StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_CHARGES.getName()))
							&& StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_GST.getName()))) {

						if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.getName()))
								&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.getName()))
								&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.getName()))
								&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.getName()))) {

							transReport.setMerchantAmount(
									String.valueOf(new BigDecimal(dbobj.getString(FieldType.PG_GST.getName()))
											.add(new BigDecimal(dbobj.getString(FieldType.PG_TDR_SC.getName())))
											.add(new BigDecimal(dbobj.getString(FieldType.ACQUIRER_TDR_SC.getName())))
											.add(new BigDecimal(dbobj.getString(FieldType.ACQUIRER_GST.getName())))
											.add(new BigDecimal(dbobj.getString(FieldType.RESELLER_CHARGES.getName())))
											.add(new BigDecimal(dbobj.getString(FieldType.RESELLER_GST.getName())))));

							if (transReport.getMerchantAmount().equalsIgnoreCase("0.00")) {
								transReport.setMerchantAmount(transReport.getMerchantAmount());
							} else {
								transReport.setMerchantAmount("-" + transReport.getMerchantAmount());
							}

						} else {
							transReport.setMerchantAmount(CrmFieldConstants.NA.getValue());
						}

					} else {

						if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.getName()))
								&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.getName()))
								&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.getName()))
								&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.getName()))) {

							transReport.setMerchantAmount(
									String.valueOf(new BigDecimal(dbobj.getString(FieldType.PG_GST.getName()))
											.add(new BigDecimal(dbobj.getString(FieldType.PG_TDR_SC.getName())))
											.add(new BigDecimal(dbobj.getString(FieldType.ACQUIRER_TDR_SC.getName())))
											.add(new BigDecimal(dbobj.getString(FieldType.ACQUIRER_GST.getName())))));

							if (transReport.getMerchantAmount().equalsIgnoreCase("0.00")) {
								transReport.setMerchantAmount(transReport.getMerchantAmount());
							} else {
								transReport.setMerchantAmount("-" + transReport.getMerchantAmount());
							}

						} else {
							transReport.setMerchantAmount(CrmFieldConstants.NA.getValue());
						}
					}

				} else if (StringUtils.isNotBlank(dbobj.getString(FieldType.PAYMENT_TYPE.toString()))
						&& dbobj.getString(FieldType.PAYMENT_TYPE.getName()).equalsIgnoreCase(PaymentType.COD.getCode())
						&& StringUtils.isNotBlank(dbobj.getString(FieldType.ORIG_TXNTYPE.toString()))
						&& dbobj.getString(FieldType.ORIG_TXNTYPE.getName())
								.equalsIgnoreCase(TxnType.REFUND.getName())) {
					transReport.setMerchantAmount("0.00");
					transReport.setTdrOrSurcharge("0.00");
					transReport.setGst("0.00");
				} else {
					if (StringUtils.isNotBlank(dbobj.getString(FieldType.ORIG_TXNTYPE.toString())) && dbobj
							.getString(FieldType.ORIG_TXNTYPE.getName()).equalsIgnoreCase(TxnType.REFUND.getName())) {

						if (StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_CHARGES.getName()))
								&& StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_GST.getName()))) {

							if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.getName()))
									&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.getName()))
									&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.getName()))
									&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.getName()))) {

								transReport.setMerchantAmount("-" + String.valueOf(
										new BigDecimal(dbobj.getString(FieldType.TOTAL_AMOUNT.getName())).subtract(
												new BigDecimal(dbobj.getString(FieldType.ACQUIRER_TDR_SC.getName()))
														.add(new BigDecimal(
																dbobj.getString(FieldType.ACQUIRER_GST.getName())))
														.add(new BigDecimal(
																dbobj.getString(FieldType.PG_TDR_SC.getName())))
														.add(new BigDecimal(
																dbobj.getString(FieldType.PG_GST.getName())))
														.add(new BigDecimal(
																dbobj.getString(FieldType.RESELLER_CHARGES.getName())))
														.add(new BigDecimal(
																dbobj.getString(FieldType.RESELLER_GST.getName()))))));
							} else {
								transReport.setMerchantAmount(CrmFieldConstants.NA.getValue());
							}

						} else {
							if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.getName()))
									&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.getName()))
									&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.getName()))
									&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.getName()))) {

								transReport.setMerchantAmount("-" + String.valueOf(
										new BigDecimal(dbobj.getString(FieldType.TOTAL_AMOUNT.getName())).subtract(
												new BigDecimal(dbobj.getString(FieldType.ACQUIRER_TDR_SC.getName()))
														.add(new BigDecimal(
																dbobj.getString(FieldType.ACQUIRER_GST.getName())))
														.add(new BigDecimal(
																dbobj.getString(FieldType.PG_TDR_SC.getName()))
																		.add(new BigDecimal(dbobj.getString(
																				FieldType.PG_GST.getName())))))));
							} else {
								transReport.setMerchantAmount(CrmFieldConstants.NA.getValue());
							}
						}

					} else {

						if (isGlocal == true) {

							if (dbobj.containsKey(FieldType.UDF7.getName())) {

								if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF7.getName()))) {

									if (StringUtils.isNotBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
											&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.toString()))
											&& StringUtils
													.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
											&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.toString()))
											&& StringUtils
													.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.toString()))) {

										String merchantAmount = String.format("%.2f", Double
												.parseDouble(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
												- (Double.parseDouble(dbobj.getString(FieldType.PG_TDR_SC.toString()))
														+ Double.parseDouble(
																dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
														+ Double.parseDouble(
																dbobj.getString(FieldType.PG_GST.toString()))
														+ Double.parseDouble(
																dbobj.getString(FieldType.ACQUIRER_GST.toString()))));

										transReport.setDoctor(
												String.valueOf(new BigDecimal(dbobj.getString(FieldType.UDF7.getName()))
														.multiply(new BigDecimal(merchantAmount))
														.divide(new BigDecimal(Constants.MAX_NUMBER_OF_KEYS.getValue()))
														.setScale(2, BigDecimal.ROUND_HALF_UP)));
									} else {
										transReport.setDoctor("0");
									}

								} else {
									transReport.setDoctor("0");
								}
							} else {
								transReport.setDoctor("0");
							}

							if (dbobj.containsKey(FieldType.UDF8.getName())) {
								if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF8.getName()))) {

									if (StringUtils.isNotBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
											&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.toString()))
											&& StringUtils
													.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
											&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.toString()))
											&& StringUtils
													.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.toString()))) {

										String merchantAmount = String.format("%.2f", Double
												.parseDouble(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
												- (Double.parseDouble(dbobj.getString(FieldType.PG_TDR_SC.toString()))
														+ Double.parseDouble(
																dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
														+ Double.parseDouble(
																dbobj.getString(FieldType.PG_GST.toString()))
														+ Double.parseDouble(
																dbobj.getString(FieldType.ACQUIRER_GST.toString()))));

										transReport.setGlocal(
												String.valueOf(new BigDecimal(dbobj.getString(FieldType.UDF8.getName()))
														.multiply(new BigDecimal(merchantAmount))
														.divide(new BigDecimal(Constants.MAX_NUMBER_OF_KEYS.getValue()))
														.setScale(2, BigDecimal.ROUND_HALF_UP)));
									} else {
										transReport.setGlocal("0");
									}
								} else {
									transReport.setGlocal("0");
								}
							} else {
								transReport.setGlocal("0");
							}

							if (dbobj.containsKey(FieldType.UDF9.getName())) {
								if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF9.getName()))) {

									if (StringUtils.isNotBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
											&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.toString()))
											&& StringUtils
													.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
											&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.toString()))
											&& StringUtils
													.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.toString()))) {

										String merchantAmount = String.format("%.2f", Double
												.parseDouble(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
												- (Double.parseDouble(dbobj.getString(FieldType.PG_TDR_SC.toString()))
														+ Double.parseDouble(
																dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
														+ Double.parseDouble(
																dbobj.getString(FieldType.PG_GST.toString()))
														+ Double.parseDouble(
																dbobj.getString(FieldType.ACQUIRER_GST.toString()))));

										transReport.setPartner(
												String.valueOf(new BigDecimal(dbobj.getString(FieldType.UDF9.getName()))
														.multiply(new BigDecimal(merchantAmount))
														.divide(new BigDecimal(Constants.MAX_NUMBER_OF_KEYS.getValue()))
														.setScale(2, BigDecimal.ROUND_HALF_UP)));
									} else {
										transReport.setPartner("0");
									}
								} else {
									transReport.setPartner("0");
								}
							} else {
								transReport.setPartner("0");
							}

							if (dbobj.containsKey(FieldType.UDF11.getName())) {
								if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF11.getName()))) {
									transReport.setUniqueId(dbobj.getString(FieldType.UDF11.getName()));
								} else {
									transReport.setUniqueId("NA");
								}
							} else {
								transReport.setUniqueId("NA");
							}
						}

						if (StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_CHARGES.getName()))
								&& StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_GST.getName()))) {

							if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.getName()))
									&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.getName()))
									&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.getName()))
									&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.getName()))) {

								transReport.setMerchantAmount(String.valueOf(
										new BigDecimal(dbobj.getString(FieldType.TOTAL_AMOUNT.getName())).subtract(
												new BigDecimal(dbobj.getString(FieldType.ACQUIRER_TDR_SC.getName()))
														.add(new BigDecimal(
																dbobj.getString(FieldType.ACQUIRER_GST.getName())))
														.add(new BigDecimal(
																dbobj.getString(FieldType.PG_TDR_SC.getName())))
														.add(new BigDecimal(
																dbobj.getString(FieldType.PG_GST.getName())))
														.add(new BigDecimal(
																dbobj.getString(FieldType.RESELLER_CHARGES.getName())))
														.add(new BigDecimal(
																dbobj.getString(FieldType.RESELLER_GST.getName()))))));
							} else {
								transReport.setMerchantAmount(CrmFieldConstants.NA.getValue());
							}
						} else {
							if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.getName()))
									&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.getName()))
									&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.getName()))
									&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.getName()))) {

								transReport.setMerchantAmount(String.valueOf(
										new BigDecimal(dbobj.getString(FieldType.TOTAL_AMOUNT.getName())).subtract(
												new BigDecimal(dbobj.getString(FieldType.ACQUIRER_TDR_SC.getName()))
														.add(new BigDecimal(
																dbobj.getString(FieldType.ACQUIRER_GST.getName())))
														.add(new BigDecimal(
																dbobj.getString(FieldType.PG_TDR_SC.getName()))
																		.add(new BigDecimal(dbobj.getString(
																				FieldType.PG_GST.getName())))))));
							} else {
								transReport.setMerchantAmount(CrmFieldConstants.NA.getValue());
							}
						}

					}
				}

				if (null != dbobj.getString(FieldType.MOP_TYPE.toString())) {
					transReport.setMoptype(MopType.getmopName(dbobj.getString(FieldType.MOP_TYPE.toString())));
				} else {
					transReport.setMoptype(CrmFieldConstants.NA.getValue());
				}

				if (null != dbobj.getString(FieldType.CARD_HOLDER_TYPE.toString())) {
					transReport.setCardHolderType(dbobj.getString(FieldType.CARD_HOLDER_TYPE.toString()));
				} else {
					transReport.setCardHolderType(CrmFieldConstants.NA.toString());
				}

				if (null != dbobj.getString(FieldType.RRN.toString())) {
					transReport.setRrn(dbobj.getString(FieldType.RRN.toString()));
				} else {
					transReport.setRrn(CrmFieldConstants.NA.toString());
				}

				if (null != dbobj.getString(FieldType.ACQ_ID.toString())) {
					transReport.setAcqId(dbobj.getString(FieldType.ACQ_ID.toString()));
				} else {
					transReport.setAcqId(CrmFieldConstants.NA.toString());
				}

				transactionList.add(transReport);
			}
			logger.info("transactionList created and size = " + transactionList.size());
			cursor.close();
			logger.info(
					"Inside TxnReports , searchPaymentForDownload , transactionListSize = " + transactionList.size());
			Comparator<PaymentSearchDownloadObject> comp = (PaymentSearchDownloadObject a,
					PaymentSearchDownloadObject b) -> {
				if (a.getDateFrom().compareTo(b.getDateFrom()) > 0) {
					return -1;
				} else if (a.getDateFrom().compareTo(b.getDateFrom()) < 0) {
					return 1;
				} else {
					return 0;
				}
			};
			Collections.sort(transactionList, comp);
			logger.info("transactionList created and Sorted");
			return transactionList;
		} catch (Exception e) {
			logger.error("Exception occured in TxnReports , searchPayment , Exception = ", e);
			return transactionList;
		}
	}

	public List<RefundRejection> searchRejectedRefund(String merchant, String orderId, String refundOrderId,
			String paymentType, String acquirer, String fromDate, String toDate) {
		List<RefundRejection> refundRejectionList = new ArrayList<RefundRejection>();
		try {
			BasicDBObject dateQuery = new BasicDBObject();
			List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();
			BasicDBObject acquirerQuery = new BasicDBObject();

			BasicDBObject allParamQuery = new BasicDBObject();
			/*
			 * List<BasicDBObject> acquirerConditionLst = new ArrayList<BasicDBObject>();
			 * List<BasicDBObject> saleOrAuthList = new ArrayList<BasicDBObject>();
			 */

			if (StringUtils.isBlank(orderId) && StringUtils.isBlank(refundOrderId)) {
				if (!fromDate.isEmpty()) {
					String currentDate = null;
					if (!toDate.isEmpty()) {
						currentDate = toDate;
					} else {
						DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						Calendar cal = Calendar.getInstance();
						currentDate = dateFormat.format(cal.getTime());
					}
					dateQuery.put(FieldType.REQUEST_DATE.getName(),
							BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(fromDate).toLocalizedPattern())
									.add("$lte", new SimpleDateFormat(currentDate).toLocalizedPattern()).get());
				}
			}

			if (!merchant.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.PAY_ID.getName(), merchant));
			}

			if (StringUtils.isNotBlank(orderId)) {
				paramConditionLst.add(new BasicDBObject(FieldType.ORDER_ID.getName(), orderId));
			}

			if (StringUtils.isNotBlank(refundOrderId)) {
				paramConditionLst.add(new BasicDBObject(FieldType.REFUND_ORDER_ID.getName(), refundOrderId));
			}

			if (!paymentType.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.PAYMENT_TYPE.getName(), paymentType));
			}

			if (!acquirer.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.ACQUIRER_TYPE.getName(), acquirer));
			}

			paramConditionLst.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.REFUND.getName()));
			ArrayList<String> list = new ArrayList<>();
			list.add(StatusType.CAPTURED.getName());
			list.add(StatusType.DECLINED.getName());
			list.add(StatusType.REJECTED.getName());
			list.add(StatusType.ERROR.getName());
			list.add(StatusType.FAILED.getName());
			list.add(StatusType.INVALID.getName());
			list.add(StatusType.CANCELLED.getName());
			list.add(StatusType.PENDING.getName());
			paramConditionLst.add(new BasicDBObject(FieldType.STATUS.getName(), new BasicDBObject("$in", list)));
			/*
			 * if(refundType.equals("DELTAREFUND")) { paramConditionLst.add(new
			 * BasicDBObject(FieldType.UDF6.getName(), Constants.Y.name())); }
			 */

			BasicDBObject refundConditionQuery = new BasicDBObject("$and", paramConditionLst);

			if (!paramConditionLst.isEmpty()) {
				allParamQuery = new BasicDBObject("$and", paramConditionLst);
			}

			List<BasicDBObject> allConditionQueryList = new ArrayList<BasicDBObject>();

			if (!acquirerQuery.isEmpty()) {
				allConditionQueryList.add(acquirerQuery);
			}
			if (!dateQuery.isEmpty()) {
				allConditionQueryList.add(dateQuery);
			}

			BasicDBObject allConditionQueryObj = new BasicDBObject();
			if (!allConditionQueryList.isEmpty()) {
				allConditionQueryObj = new BasicDBObject("$and", allConditionQueryList);
			}
			List<BasicDBObject> fianlList = new ArrayList<BasicDBObject>();

			if (!allParamQuery.isEmpty()) {
				fianlList.add(allParamQuery);
			}
			if (!allConditionQueryObj.isEmpty()) {
				fianlList.add(allConditionQueryObj);
			}

			BasicDBObject finalquery = new BasicDBObject("$and", fianlList);

			logger.info("Inside search summary report query , finalquery = " + finalquery);

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns
					.getCollection(PropertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));

			BasicDBObject match = new BasicDBObject("$match", finalquery);
			// Now the aggregate operation
			Document firstGroup = new Document("_id", new Document("REFUND_ORDER_ID", "$REFUND_ORDER_ID"));
			BasicDBObject firstGroupObject = new BasicDBObject(firstGroup);
			BasicDBObject secondGroup = new BasicDBObject("$push", "$$ROOT");
			BasicDBObject group = new BasicDBObject("$group", firstGroupObject.append("entries", secondGroup));
			BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject("CREATE_DATE", -1));
			List<BasicDBObject> pipeline = Arrays.asList(match, sort, group);
			AggregateIterable<Document> output = coll.aggregate(pipeline);
			output.allowDiskUse(true);
			MongoCursor<Document> cursor = output.iterator();
			Boolean capturedFlag = false;
			while (cursor.hasNext()) {
				capturedFlag = false;
				Document dbobj = cursor.next();
				List<Document> lstDoc = (List<Document>) dbobj.get("entries");
				for (int i = 0; i < lstDoc.size(); i++) {
					if (lstDoc.get(i).getString(FieldType.TXNTYPE.getName()).equals(TransactionType.REFUND.getName())
							&& lstDoc.get(i).getString(FieldType.STATUS.getName())
									.equals(StatusType.CAPTURED.getName())) {
						capturedFlag = true;
						break;
					}
				}
				if (!capturedFlag) {
					Document doc = lstDoc.get(0);
					RefundRejection refundRejection = new RefundRejection();
					/*
					 * List<Fields> fieldsList = new ArrayList<Fields>(); fieldsList =
					 * fieldsDao.getPreviousSaleCapturedForOrderId(doc.getString
					 * (FieldType.ORDER_ID. toString()));
					 */
					refundRejection.setOrderId(doc.getString(FieldType.ORDER_ID.toString()));
					refundRejection.setPgRefNum(doc.getString(FieldType.ORIG_TXN_ID.toString()));
					refundRejection.setRefundDate(doc.getString(FieldType.REQUEST_DATE.toString()));
					refundRejection.setRefundAmount(doc.getString(FieldType.AMOUNT.toString()));
					refundRejection.setTotalAmount(doc.getString(FieldType.SALE_AMOUNT.getName()));
					refundRejection.setRefundFlag(doc.getString(FieldType.REFUND_FLAG.toString()));
					// transactionSearch.setDateFrom(doc.getString(FieldType.PG_DATE_TIME.getName()));
					refundRejection.setRefundOrderId(doc.getString(FieldType.REFUND_ORDER_ID.getName().toString()));
					refundRejection.setStatus(doc.getString(FieldType.STATUS.getName().toString()));
					refundRejection.setCurrencyCode(doc.getString(FieldType.CURRENCY_CODE.getName().toString()));
					refundRejection.setPayId(doc.getString(FieldType.PAY_ID.getName().toString()));
					refundRejection.setProcessedDate(doc.getString(FieldType.CREATE_DATE.getName().toString()));
					// transactionSearch.setPgTxnMessage(doc.getString(FieldType.RESPONSE_MESSAGE.getName().toString()));
					refundRejectionList.add(refundRejection);
				}
			}
			cursor.close();
			Comparator<RefundRejection> comp = (RefundRejection a, RefundRejection b) -> {
				if (a.getRefundDate().compareTo(b.getRefundDate()) > 0) {
					return -1;
				} else if (a.getRefundDate().compareTo(b.getRefundDate()) < 0) {
					return 1;
				} else {
					return 0;
				}
			};
			Collections.sort(refundRejectionList, comp);
			return refundRejectionList;
		} catch (Exception e) {
			logger.error("Exception in getting records for refund reject report ", e);
		}
		return refundRejectionList;
	}

	public ManualRefundProcess searchForManualRefund(String pgRefNum, String payId, String refundedAmount,
			String refundAvailable, String chargebackAmount, String refundType, String chargebackStatus) {

		ManualRefundProcess refundProcess = new ManualRefundProcess();
		List<BasicDBObject> finalList = new ArrayList<BasicDBObject>();

		try {

			finalList.add(new BasicDBObject(FieldType.PG_REF_NUM.getName(), pgRefNum));
			finalList.add(new BasicDBObject(FieldType.PAY_ID.getName(), payId));
			finalList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName()));
			finalList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.CAPTURED.getName()));

			BasicDBObject finalquery = new BasicDBObject("$and", finalList);

			logger.info("Inside TxnReports , searchForManualRefund , finalquery = " + finalquery);
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns
					.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));
			BasicDBObject match = new BasicDBObject("$match", finalquery);
			// Now the aggregate operation ()In case any parameter is passed in
			// search query
			// , then show all records

			Document firstGroup;

			firstGroup = new Document("_id", new Document("_id", "$_id"));
			BasicDBObject firstGroupObject = new BasicDBObject(firstGroup);
			BasicDBObject secondGroup = new BasicDBObject("$push", "$$ROOT");
			BasicDBObject group = new BasicDBObject("$group", firstGroupObject.append("entries", secondGroup));
			BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject("INSERTION_DATE", -1));

			List<BasicDBObject> pipeline = Arrays.asList(match, sort, group);

			AggregateIterable<Document> output = coll.aggregate(pipeline);
			output.allowDiskUse(true);
			MongoCursor<Document> cursor = output.iterator();
			while (cursor.hasNext()) {
				Document mydata = cursor.next();
				List<Document> courses = (List<Document>) mydata.get("entries");
				Document dbobj = courses.get(0);

				refundProcess.setAmount(dbobj.getString(FieldType.TOTAL_AMOUNT.getName()));
				refundProcess.setCurrencyCode(propertiesManager
						.getAlphabaticCurrencyCode(dbobj.getString(FieldType.CURRENCY_CODE.getName())));
				refundProcess.setOrderId(dbobj.getString(FieldType.ORDER_ID.getName()));
				refundProcess.setTxnType(TransactionType.REFUND.getName());
				refundProcess.setPayId(payId);
				refundProcess.setMerchantName(userdao.getBusinessNameByPayId(payId));
				refundProcess.setPgRefNum(pgRefNum);
				refundProcess.setRefundedAmount(refundedAmount);
				refundProcess.setChargebackStatus(chargebackStatus);
				// refundProcess.setRefundAvailable(refundAvailable);

				if (!refundType.equalsIgnoreCase("chargebackRefund")) {
					if (StringUtils.isBlank(chargebackAmount)) {
						refundProcess.setRefundAvailable(refundAvailable);
						refundProcess.setChargebackAmount("");
					} else {
						if (StringUtils.isNotBlank(chargebackStatus) && (chargebackStatus.equalsIgnoreCase("Refunded")
								|| chargebackStatus.equalsIgnoreCase("Closed"))) {
							refundProcess.setRefundAvailable(refundAvailable);
						} else {
							refundProcess.setRefundAvailable(String.valueOf(
									new BigDecimal(refundAvailable).subtract(new BigDecimal(chargebackAmount))));

						}
						refundProcess.setChargebackAmount(chargebackAmount);
					}
				} else {
					refundProcess.setRefundAvailable(refundAvailable);
					refundProcess.setChargebackAmount(chargebackAmount);
				}

				if (StringUtils.isBlank(dbobj.getString(FieldType.SURCHARGE_FLAG.getName()))) {
					refundProcess.setSurchargeFlag("NA");
				} else {
					refundProcess.setSurchargeFlag(dbobj.getString(FieldType.SURCHARGE_FLAG.getName()));
				}
				if (dbobj.containsKey(FieldType.REG_NUMBER.getName())) {
					refundProcess.setRegNumber(dbobj.getString(FieldType.REG_NUMBER.getName()));
				}
			}

			// logger.info("transactionList created and size =
			// "+manualRefundProcessLst.size());
			cursor.close();
			// logger.info("Inside TxnReports , searchPayment ,
			// transactionListSize = " +
			// manualRefundProcessLst.size());

			logger.info("transactionList created and Sorted");
			return refundProcess;
		} catch (Exception ex) {
			logger.error("Exception in getting records for manual refund Process ", ex);
		}
		return refundProcess;
	}

	// check for Refund
	@SuppressWarnings("static-access")
	public List<TransactionSearch> refundForSaleCaputureTransaction(List<TransactionSearch> transList) {
		logger.info("inside the refundForSaleCaputureTransaction ");
		List<TransactionSearch> transactionSearchList = new ArrayList<TransactionSearch>();
		// ManualRefundProcess refundProcess = new ManualRefundProcess();
		try {

			if (!transList.isEmpty()) {
				MongoCursor<Document> cursor = null;
				for (TransactionSearch list : transList) {

					List<BasicDBObject> query = new ArrayList<BasicDBObject>();

					query.add(new BasicDBObject(FieldType.ORIG_TXN_ID.getName(), list.getPgRefNum()));
					query.add(new BasicDBObject(FieldType.ORDER_ID.getName(), list.getOrderId()));
					query.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.REFUND.getName()));

					List<String> refundStatusList = new ArrayList<>();
					refundStatusList.add(StatusType.CAPTURED.getName());
					refundStatusList.add(StatusType.INITIATED.getName());
					query.add(
							new BasicDBObject(FieldType.STATUS.getName(), new BasicDBObject("$in", refundStatusList)));
					BasicDBObject finalquery = new BasicDBObject("$and", query);

					logger.info("Inside TxnReports , searchPayment , finalquery = " + finalquery);
					MongoDatabase dbIns = mongoInstance.getDB();
					MongoCollection<Document> coll = dbIns.getCollection(
							propertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));

					BasicDBObject projectElement = new BasicDBObject();
					projectElement.put(FieldType.TOTAL_AMOUNT.getName(), 1);

					BasicDBObject project = new BasicDBObject("$project", projectElement);
					BasicDBObject match = new BasicDBObject("$match", finalquery);

					List<BasicDBObject> pipeline = Arrays.asList(match, project);

					AggregateIterable<Document> output = coll.aggregate(pipeline);
					output.allowDiskUse(true);
					cursor = output.iterator();
					BigDecimal refundAmount = new BigDecimal("0.00");
					BigDecimal saleAmount = new BigDecimal(list.getTotalAmount());
					list.setRefundedAmount(String.valueOf(refundAmount));
					list.setRefundAvailable(String.valueOf(saleAmount));

					while (cursor.hasNext()) {
						Document dbobj = cursor.next();
						refundAmount = refundAmount
								.add(refundAmount = new BigDecimal(dbobj.getString(FieldType.TOTAL_AMOUNT.getName())));
					}
					if (saleAmount.compareTo(refundAmount) == 0) {
						list.setRefundBtnText("Refunded");
					} else if (refundAmount.compareTo(new BigDecimal("0.00")) == 0) {
						list.setRefundBtnText("Refund");
					} else {
						list.setRefundBtnText("Partial Refund");
					}
					list.setRefundedAmount(String.valueOf(refundAmount));
					list.setRefundAvailable(String.valueOf(saleAmount.subtract(refundAmount)));

					transactionSearchList.add(list);
				}
				cursor.close();
			}
			logger.info("Calculate all Refund");
			return transactionSearchList;
		} catch (Exception e) {
			logger.info("Exception caught in Txn Reports getForRefundTransaction()", e);
		}
		return transactionSearchList;
	}

	@SuppressWarnings({ "unchecked", "static-access" })
	public List<MerchantPaymentAdviseDownloadObject> merchantPaymentAdviceDownloadForSale(String merchantPayId,
			String subMerchantPayId, String payoutDate, User user, Set<String> orderIdSet, String currency) {

		List<MerchantPaymentAdviseDownloadObject> paymentAdviseList = new ArrayList<MerchantPaymentAdviseDownloadObject>();

		List<String> merchantPayIdQueryList = new ArrayList<String>();
		BasicDBObject merchantPayIdQueryObject = null;
		BasicDBObject merchantPayIdQuery = new BasicDBObject();
		try {
			logger.info("Inside TxnReport, MerchantPaymentAdvice");
			List<BasicDBObject> finalList = new ArrayList<BasicDBObject>();
			BasicDBObject dateQuery = new BasicDBObject();
			String startTime = payoutDate + " 00:00:00";
			String endTime = payoutDate + " 23:59:59";

			dateQuery.put(FieldType.PAYOUT_DATE.getName(),
					BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(startTime).toLocalizedPattern())
							.add("$lt", new SimpleDateFormat(endTime).toLocalizedPattern()).get());

			if (user.getUserType().equals(UserType.PARENTMERCHANT)) {

				finalList.add(new BasicDBObject(FieldType.PARENT_PAY_ID.getName(), user.getPayId()));

			} else {
				if (merchantPayId.equalsIgnoreCase("All")) {
					List<Merchants> allMerchanList = new ArrayList<Merchants>();
					if (user.getUserType().equals(UserType.ADMIN) || user.getUserType().equals(UserType.SUBADMIN)
							|| user.getUserType().equals(UserType.RESELLER)) {
						if (user.getUserType().equals(UserType.RESELLER)) {
							allMerchanList = userdao.getMerchantListByResellerId(user.getResellerId());
						} else {
							allMerchanList = userdao.getActiveMerchantList();
						}
						for (Merchants merchant : allMerchanList) {
							merchantPayIdQueryList.add(merchant.getPayId());
						}
						merchantPayIdQueryObject = new BasicDBObject("$in", merchantPayIdQueryList);
						merchantPayIdQuery.append(FieldType.PAY_ID.getName(), merchantPayIdQueryObject);
					}

					if (merchantPayIdQueryObject != null) {
						finalList.add(merchantPayIdQuery);
					}
				} else {
					finalList.add(new BasicDBObject(FieldType.PAY_ID.getName(), merchantPayId));
				}
			}

			BasicDBObject orderIdConditionQuery = null;
			if (orderIdSet != null) {
				List<String> orderIdList = new ArrayList<>(orderIdSet);
				orderIdConditionQuery = new BasicDBObject("$in", orderIdList);
			}
			if (null != orderIdConditionQuery) {
				finalList.add(new BasicDBObject(FieldType.ORDER_ID.getName(), orderIdConditionQuery));
			}

			if (StringUtils.isNotBlank(subMerchantPayId) && !subMerchantPayId.equalsIgnoreCase("All")) {
				finalList.add(new BasicDBObject(FieldType.SUB_MERCHANT_ID.getName(), subMerchantPayId));
			}
			if (StringUtils.isNotBlank(currency) && !currency.equalsIgnoreCase("All")) {
				finalList.add(new BasicDBObject(FieldType.CURRENCY_CODE.getName(), currency));
			}

			List<BasicDBObject> refundConditionQueryList = new ArrayList<BasicDBObject>();
			refundConditionQueryList
					.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.REFUNDRECO.getName()));
			refundConditionQueryList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.SETTLED.getName()));
			BasicDBObject refundConditionQueryObj = new BasicDBObject("$and", refundConditionQueryList);

			List<BasicDBObject> saleConditionQueryList = new ArrayList<BasicDBObject>();
			saleConditionQueryList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.RECO.getName()));
			saleConditionQueryList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.SETTLED.getName()));
			BasicDBObject saleConditionQueryObj = new BasicDBObject("$and", saleConditionQueryList);

			List<BasicDBObject> saleAndRefundConditionQueryList = new ArrayList<BasicDBObject>();
			saleAndRefundConditionQueryList.add(saleConditionQueryObj);
			saleAndRefundConditionQueryList.add(refundConditionQueryObj);
			BasicDBObject saleAndRefundConditionQueryObj = new BasicDBObject("$or", saleAndRefundConditionQueryList);

			if (!saleAndRefundConditionQueryObj.isEmpty()) {
				finalList.add(saleAndRefundConditionQueryObj);
			}
			if (!dateQuery.isEmpty()) {
				finalList.add(dateQuery);
			}

			BasicDBObject finalquery = new BasicDBObject("$and", finalList);

			logger.info("Inside TxnReports , searchPayment , finalquery = " + finalquery);
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns
					.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));
			BasicDBObject match = new BasicDBObject("$match", finalquery);

			Document firstGroup;
			firstGroup = new Document("_id", new Document("_id", "$_id"));

			BasicDBObject firstGroupObject = new BasicDBObject(firstGroup);
			BasicDBObject secondGroup = new BasicDBObject("$push", "$$ROOT");
			BasicDBObject group = new BasicDBObject("$group", firstGroupObject.append("entries", secondGroup));
			BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject("PG_DATE_TIME", -1));

			List<BasicDBObject> pipeline = Arrays.asList(match, sort, group);

			AggregateIterable<Document> output = coll.aggregate(pipeline);
			output.allowDiskUse(true);
			MongoCursor<Document> cursor = output.iterator();
			while (cursor.hasNext()) {
				Document mydata = cursor.next();
				List<Document> courses = (List<Document>) mydata.get("entries");
				Document dbobj = courses.get(0);
				MerchantPaymentAdviseDownloadObject merchantPaymentReport = new MerchantPaymentAdviseDownloadObject();

				String mopType = dbobj.getString(FieldType.MOP_TYPE.getName());
				if (StringUtils.isBlank(mopType)) {
					mopType = "NA";
				}
				if (StringUtils.isNumeric(mopType)
						|| (StringUtils.isNotBlank(dbobj.getString(FieldType.PAYMENT_TYPE.toString()))
								&& dbobj.getString(FieldType.PAYMENT_TYPE.getName()).equalsIgnoreCase("WL"))) {
					merchantPaymentReport.setCardNetwork(MopType.getmopName(mopType));
				} else {
					switch (mopType) {
					case "VI":
						merchantPaymentReport.setCardNetwork(MopType.getmopName(mopType));
						break;

					case "MC":
						merchantPaymentReport.setCardNetwork(MopType.getmopName(mopType));
						break;

					case "RU":
						merchantPaymentReport.setCardNetwork(MopType.getmopName(mopType));
						break;

					case "DN":
						merchantPaymentReport.setCardNetwork(MopType.getmopName(mopType));
						break;

					case "CD":
						merchantPaymentReport.setCardNetwork(MopType.getmopName(mopType));
						break;

					case "UP":
						merchantPaymentReport.setCardNetwork(MopType.getmopName(mopType));
						break;

					case "WL":
						merchantPaymentReport.setCardNetwork(MopType.getmopName(mopType));
						break;

					case "NB":
						merchantPaymentReport.setCardNetwork(MopType.getmopName(mopType));
						break;

					case "NEFT":
						merchantPaymentReport.setCardNetwork(MopType.getmopName(mopType));
						break;
					case "IMPS":
						merchantPaymentReport.setCardNetwork(MopType.getmopName(mopType));
						break;
					case "RTGS":
						merchantPaymentReport.setCardNetwork(MopType.getmopName(mopType));
						break;
					}
				}

				String paymentType = dbobj.getString(FieldType.PAYMENT_TYPE.getName());
				if (paymentType == null) {
				} else {
					switch (paymentType) {
					case "NB":
						merchantPaymentReport.setPaymentType(PaymentType.getpaymentName(paymentType));
						break;

					case "UP":
						merchantPaymentReport.setPaymentType(PaymentType.getpaymentName(paymentType));
						break;

					case "WL":
						merchantPaymentReport.setPaymentType(PaymentType.getpaymentName(paymentType));
						break;
					case "CD":
						merchantPaymentReport.setPaymentType(PaymentType.getpaymentName(paymentType));
						break;

					case "CC":
						merchantPaymentReport.setPaymentType(PaymentType.getpaymentName(paymentType));
						break;

					case "DC":
						merchantPaymentReport.setPaymentType(PaymentType.getpaymentName(paymentType));
						break;

					case "PC":
						merchantPaymentReport.setPaymentType(PaymentType.getpaymentName(paymentType));
						break;

					case "AD":
						merchantPaymentReport.setPaymentType(PaymentType.getpaymentName(paymentType));
						break;

					case "DP":
						merchantPaymentReport.setPaymentType(PaymentType.getpaymentName(paymentType));
						break;

					case "EX":
						merchantPaymentReport.setPaymentType(PaymentType.getpaymentName(paymentType));
						break;

					case "RP":
						merchantPaymentReport.setPaymentType(PaymentType.getpaymentName(paymentType));
						break;

					case "EM":
						merchantPaymentReport.setPaymentType(PaymentType.getpaymentName(paymentType));
						break;

					case "NEFT":
						merchantPaymentReport.setPaymentType(PaymentType.getpaymentName(paymentType));
						break;

					case "IMPS":
						merchantPaymentReport.setPaymentType(PaymentType.getpaymentName(paymentType));
						break;

					case "RTGS":
						merchantPaymentReport.setPaymentType(PaymentType.getpaymentName(paymentType));
						break;
					}
				}
				// String paymentType =
				// dbobj.getString(FieldType.PAYMENT_TYPE.getName());
				// merchantPaymentReport.setPaymentType(PaymentType.getpaymentName(paymentType));
				if (StringUtils.isNotBlank(dbobj.getString(FieldType.PAYMENTS_REGION.getName()))) {
					merchantPaymentReport.setPaymentRegion(dbobj.getString(FieldType.PAYMENTS_REGION.getName()));
				} else {
					merchantPaymentReport.setPaymentRegion(CrmFieldConstants.NA.getValue());
				}
				if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_MODE.getName()))) {
					merchantPaymentReport.setAcquirerMode(dbobj.getString(FieldType.ACQUIRER_MODE.toString()));
				} else {
					merchantPaymentReport.setAcquirerMode(CrmFieldConstants.NA.getValue());
				}
				if (StringUtils.isNotBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.getName()))) {
					merchantPaymentReport.setGrossAmount(dbobj.getString(FieldType.TOTAL_AMOUNT.getName()));
				} else {
					merchantPaymentReport.setGrossAmount("0.00");
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.AMOUNT.getName()))) {
					merchantPaymentReport.setBaseAmount(dbobj.getString(FieldType.AMOUNT.getName()));
				} else {
					merchantPaymentReport.setBaseAmount("0.00");
				}
				if (StringUtils.isNotBlank(dbobj.getString(FieldType.SURCHARGE_FLAG.getName()))) {
					merchantPaymentReport.setSurcharge_flag(dbobj.getString(FieldType.SURCHARGE_FLAG.getName()));
				} else {
					merchantPaymentReport.setSurcharge_flag("0.00");
				}

				merchantPaymentReport.setOrigTxnType(dbobj.getString(FieldType.ORIG_TXNTYPE.getName()));

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.ORIG_TXNTYPE.toString()))
						&& dbobj.getString(FieldType.ORIG_TXNTYPE.getName()).equalsIgnoreCase(TxnType.SALE.getName())
						&& StringUtils.isNotBlank(dbobj.getString(FieldType.PAYMENT_TYPE.toString()))
						&& (dbobj.getString(FieldType.PAYMENT_TYPE.getName())
								.equalsIgnoreCase(PaymentType.COD.getCode()))) {

					if (user.getUserType().equals(UserType.RESELLER) && user.isPartnerFlag()) {
						if (StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_CHARGES.getName()))
								&& StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_GST.getName()))) {

							if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.getName()))
									&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.getName()))
									&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.getName()))
									&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.getName()))) {

								merchantPaymentReport.setResellerCharges(String.valueOf(
										new BigDecimal(dbobj.getString(FieldType.RESELLER_CHARGES.getName()))));

								merchantPaymentReport.setResellerGst(String
										.valueOf(new BigDecimal(dbobj.getString(FieldType.RESELLER_GST.getName()))));

								merchantPaymentReport.setTdr(String
										.valueOf(new BigDecimal(dbobj.getString(FieldType.ACQUIRER_TDR_SC.getName()))
												.add(new BigDecimal(dbobj.getString(FieldType.PG_TDR_SC.getName())))
												.add(new BigDecimal(
														dbobj.getString(FieldType.RESELLER_CHARGES.getName())))));
								merchantPaymentReport.setGst(
										String.valueOf(new BigDecimal(dbobj.getString(FieldType.ACQUIRER_GST.getName()))
												.add(new BigDecimal(dbobj.getString(FieldType.PG_GST.getName())))
												.add(new BigDecimal(
														dbobj.getString(FieldType.RESELLER_GST.getName())))));

								merchantPaymentReport.setNetAmount("-" + String
										.valueOf(new BigDecimal(dbobj.getString(FieldType.RESELLER_CHARGES.getName()))
												.add(new BigDecimal(dbobj.getString(FieldType.RESELLER_GST.getName())))
												.add(new BigDecimal(dbobj.getString(FieldType.ACQUIRER_GST.getName())))
												.add(new BigDecimal(
														dbobj.getString(FieldType.ACQUIRER_TDR_SC.getName())))
												.add(new BigDecimal(dbobj.getString(FieldType.PG_TDR_SC.getName())))
												.add(new BigDecimal(dbobj.getString(FieldType.PG_GST.getName())))));

							}
						} else {
							merchantPaymentReport.setResellerCharges(String.valueOf(0.00));
							merchantPaymentReport.setResellerGst(String.valueOf(0.00));
							merchantPaymentReport.setTdr(String.valueOf(0.00));
							merchantPaymentReport.setGst(String.valueOf(0.00));
							merchantPaymentReport.setNetAmount(String.valueOf(0.00));
							logger.info(
									"inside txnReport for download Payment Advise report and tdr/surcharge set 0.00");
						}

					} else {

						if (StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_CHARGES.getName()))
								&& StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_GST.getName()))) {

							if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.getName()))
									&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.getName()))
									&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.getName()))
									&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.getName()))) {

								merchantPaymentReport.setTdr(String
										.valueOf(new BigDecimal(dbobj.getString(FieldType.ACQUIRER_TDR_SC.getName()))
												.add(new BigDecimal(dbobj.getString(FieldType.PG_TDR_SC.getName())))
												.add(new BigDecimal(
														dbobj.getString(FieldType.RESELLER_CHARGES.getName())))));
								merchantPaymentReport.setGst(
										String.valueOf(new BigDecimal(dbobj.getString(FieldType.ACQUIRER_GST.getName()))
												.add(new BigDecimal(dbobj.getString(FieldType.PG_GST.getName())))
												.add(new BigDecimal(
														dbobj.getString(FieldType.RESELLER_GST.getName())))));

								merchantPaymentReport.setNetAmount("-" + String
										.valueOf(new BigDecimal(dbobj.getString(FieldType.ACQUIRER_TDR_SC.getName()))
												.add(new BigDecimal(dbobj.getString(FieldType.ACQUIRER_GST.getName())))
												.add(new BigDecimal(dbobj.getString(FieldType.PG_TDR_SC.getName())))
												.add(new BigDecimal(dbobj.getString(FieldType.PG_GST.getName())))
												.add(new BigDecimal(
														dbobj.getString(FieldType.RESELLER_CHARGES.getName())))
												.add(new BigDecimal(
														dbobj.getString(FieldType.RESELLER_GST.getName())))));

							} else {
								merchantPaymentReport.setTdr(String.valueOf(0.00));
								merchantPaymentReport.setGst(String.valueOf(0.00));
								merchantPaymentReport.setNetAmount(String.valueOf(0.00));
								logger.info(
										"inside txnReport for download Payment Advise report and tdr/surcharge set 0.00");
							}

						} else {

							if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.getName()))
									&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.getName()))
									&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.getName()))
									&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.getName()))) {

								merchantPaymentReport.setTdr(String
										.valueOf(new BigDecimal(dbobj.getString(FieldType.ACQUIRER_TDR_SC.getName()))
												.add(new BigDecimal(dbobj.getString(FieldType.PG_TDR_SC.getName())))));
								merchantPaymentReport.setGst(
										String.valueOf(new BigDecimal(dbobj.getString(FieldType.ACQUIRER_GST.getName()))
												.add(new BigDecimal(dbobj.getString(FieldType.PG_GST.getName())))));

								merchantPaymentReport.setNetAmount("-" + String
										.valueOf(new BigDecimal(dbobj.getString(FieldType.ACQUIRER_TDR_SC.getName()))
												.add(new BigDecimal(dbobj.getString(FieldType.ACQUIRER_GST.getName())))
												.add(new BigDecimal(dbobj.getString(FieldType.PG_TDR_SC.getName())))
												.add(new BigDecimal(dbobj.getString(FieldType.PG_GST.getName())))));

							} else {
								merchantPaymentReport.setTdr(String.valueOf(0.00));
								merchantPaymentReport.setGst(String.valueOf(0.00));
								merchantPaymentReport.setNetAmount(String.valueOf(0.00));
								logger.info(
										"inside txnReport for download Payment Advise report and tdr/surcharge set 0.00");
							}
						}
					}

				} else if (StringUtils.isNotBlank(dbobj.getString(FieldType.ORIG_TXNTYPE.toString()))
						&& dbobj.getString(FieldType.ORIG_TXNTYPE.getName()).equalsIgnoreCase(TxnType.REFUND.getName())
						&& StringUtils.isNotBlank(dbobj.getString(FieldType.PAYMENT_TYPE.toString()))
						&& (dbobj.getString(FieldType.PAYMENT_TYPE.getName())
								.equalsIgnoreCase(PaymentType.COD.getCode()))) {

					merchantPaymentReport.setNetAmount("0.00");
					if (user.getUserType().equals(UserType.RESELLER) && user.isPartnerFlag()) {
						merchantPaymentReport.setResellerCharges("0.00");
						merchantPaymentReport.setResellerGst("0.00");
						merchantPaymentReport.setTdr("0.00");
						merchantPaymentReport.setGst("0.00");
					} else {
						merchantPaymentReport.setTdr("0.00");
						merchantPaymentReport.setGst("0.00");
					}

				} else {

					if (user.getUserType().equals(UserType.RESELLER) && user.isPartnerFlag()) {
						if (StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_CHARGES.getName()))
								&& StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_GST.getName()))) {

							if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.getName()))
									&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.getName()))
									&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.getName()))
									&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.getName()))) {

								merchantPaymentReport.setResellerCharges(String.valueOf(
										new BigDecimal(dbobj.getString(FieldType.RESELLER_CHARGES.getName()))));

								merchantPaymentReport.setResellerGst(String
										.valueOf(new BigDecimal(dbobj.getString(FieldType.RESELLER_GST.getName()))));

								merchantPaymentReport.setTdr(String
										.valueOf(new BigDecimal(dbobj.getString(FieldType.ACQUIRER_TDR_SC.getName()))
												.add(new BigDecimal(dbobj.getString(FieldType.PG_TDR_SC.getName())))
												.add(new BigDecimal(
														dbobj.getString(FieldType.RESELLER_CHARGES.getName())))));
								merchantPaymentReport.setGst(
										String.valueOf(new BigDecimal(dbobj.getString(FieldType.ACQUIRER_GST.getName()))
												.add(new BigDecimal(dbobj.getString(FieldType.PG_GST.getName())))
												.add(new BigDecimal(
														dbobj.getString(FieldType.RESELLER_GST.getName())))));

								merchantPaymentReport.setNetAmount(
										String.valueOf(new BigDecimal(dbobj.getString(FieldType.TOTAL_AMOUNT.getName()))
												.subtract(new BigDecimal(merchantPaymentReport.getTdr()))
												.subtract(new BigDecimal(merchantPaymentReport.getGst()))));
							}
						} else {
							merchantPaymentReport.setResellerCharges("0.00");
							merchantPaymentReport.setResellerGst("0.00");
							merchantPaymentReport.setTdr("0.00");
							merchantPaymentReport.setGst("0.00");
							merchantPaymentReport.setNetAmount(
									String.valueOf(new BigDecimal(dbobj.getString(FieldType.TOTAL_AMOUNT.getName()))
											.subtract(new BigDecimal(merchantPaymentReport.getTdr()))
											.subtract(new BigDecimal(merchantPaymentReport.getGst()))));
						}

					} else {

						if (StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_CHARGES.getName()))
								&& StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_GST.getName()))) {

							if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.getName()))
									&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.getName()))
									&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.getName()))
									&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.getName()))) {

								merchantPaymentReport.setTdr(String
										.valueOf(new BigDecimal(dbobj.getString(FieldType.ACQUIRER_TDR_SC.getName()))
												.add(new BigDecimal(dbobj.getString(FieldType.PG_TDR_SC.getName())))
												.add(new BigDecimal(
														dbobj.getString(FieldType.RESELLER_CHARGES.getName())))));
								merchantPaymentReport.setGst(
										String.valueOf(new BigDecimal(dbobj.getString(FieldType.ACQUIRER_GST.getName()))
												.add(new BigDecimal(dbobj.getString(FieldType.PG_GST.getName())))
												.add(new BigDecimal(
														dbobj.getString(FieldType.RESELLER_GST.getName())))));

								merchantPaymentReport.setNetAmount(
										String.valueOf(new BigDecimal(dbobj.getString(FieldType.TOTAL_AMOUNT.getName()))
												.subtract(new BigDecimal(merchantPaymentReport.getTdr()))
												.subtract(new BigDecimal(merchantPaymentReport.getGst()))));
							} else {

								if (StringUtils.isNotBlank(dbobj.getString(FieldType.PAYMENT_TYPE.toString()))
										&& (dbobj.getString(FieldType.PAYMENT_TYPE.getName()).equalsIgnoreCase("RTGS")
												|| dbobj.getString(FieldType.PAYMENT_TYPE.getName())
														.equalsIgnoreCase("IMPS")
												|| dbobj.getString(FieldType.PAYMENT_TYPE.getName())
														.equalsIgnoreCase("NEFT"))) {

									merchantPaymentReport.setTdr(String.valueOf(0.00));
									merchantPaymentReport.setGst(String.valueOf(0.00));
									merchantPaymentReport.setNetAmount(String.valueOf(
											new BigDecimal(dbobj.getString(FieldType.TOTAL_AMOUNT.getName()))));

								} else {

									merchantPaymentReport.setTdr(String.valueOf(0.00));
									merchantPaymentReport.setGst(String.valueOf(0.00));
									merchantPaymentReport.setNetAmount(String.valueOf(0.00));
									logger.info(
											"inside txnReport for download Payment Advise report and tdr/surcharge set 0.00");
								}
							}

						} else {

							if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.getName()))
									&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.getName()))
									&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.getName()))
									&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.getName()))) {

								merchantPaymentReport.setTdr(String
										.valueOf(new BigDecimal(dbobj.getString(FieldType.ACQUIRER_TDR_SC.getName()))
												.add(new BigDecimal(dbobj.getString(FieldType.PG_TDR_SC.getName())))));
								merchantPaymentReport.setGst(
										String.valueOf(new BigDecimal(dbobj.getString(FieldType.ACQUIRER_GST.getName()))
												.add(new BigDecimal(dbobj.getString(FieldType.PG_GST.getName())))));

								merchantPaymentReport.setNetAmount(
										String.valueOf(new BigDecimal(dbobj.getString(FieldType.TOTAL_AMOUNT.getName()))
												.subtract(new BigDecimal(merchantPaymentReport.getTdr()))
												.subtract(new BigDecimal(merchantPaymentReport.getGst()))));
							} else {

								if (StringUtils.isNotBlank(dbobj.getString(FieldType.PAYMENT_TYPE.toString()))
										&& (dbobj.getString(FieldType.PAYMENT_TYPE.getName()).equalsIgnoreCase("RTGS")
												|| dbobj.getString(FieldType.PAYMENT_TYPE.getName())
														.equalsIgnoreCase("IMPS")
												|| dbobj.getString(FieldType.PAYMENT_TYPE.getName())
														.equalsIgnoreCase("NEFT"))) {

									merchantPaymentReport.setTdr(String.valueOf(0.00));
									merchantPaymentReport.setGst(String.valueOf(0.00));
									merchantPaymentReport.setNetAmount(String.valueOf(
											new BigDecimal(dbobj.getString(FieldType.TOTAL_AMOUNT.getName()))));

								} else {

									merchantPaymentReport.setTdr(String.valueOf(0.00));
									merchantPaymentReport.setGst(String.valueOf(0.00));
									merchantPaymentReport.setNetAmount(String.valueOf(0.00));
									logger.info(
											"inside txnReport for download Payment Advise report and tdr/surcharge set 0.00");
								}
							}
						}
					}
				}
				merchantPaymentReport.setPayId(dbobj.getString(FieldType.PAY_ID.getName()));
				merchantPaymentReport.setOid(dbobj.getString(FieldType.OID.getName()));
				if (StringUtils.isNotBlank(dbobj.getString(FieldType.CARD_HOLDER_TYPE.getName()))) {
					merchantPaymentReport.setCardHolderType(dbobj.getString(FieldType.CARD_HOLDER_TYPE.getName()));
				}
				if (StringUtils.isNotBlank(dbobj.getString(FieldType.UTR_NO.getName()))) {
					merchantPaymentReport.setUtrNo(dbobj.getString(FieldType.UTR_NO.getName()));
				}
				if (StringUtils.isNotBlank(dbobj.getString(FieldType.PG_DATE_TIME.getName()))) {
					merchantPaymentReport.setCreateDate(dbobj.getString(FieldType.PG_DATE_TIME.getName()));
				}
				paymentAdviseList.add(merchantPaymentReport);
			}
			cursor.close();
			return paymentAdviseList;
		} catch (MongoExecutionTimeoutException exception) {
			logger.error("mongo timeout exception caught while fetch data for payment advise report ", exception);
		} catch (MongoQueryException exception) {
			logger.error("mongoQuery Exception caught while fetch data for payment advise report ", exception);
		} catch (Exception exception) {
			logger.error("Exception caught while fetch data for payment advise report", exception);
		}
		return paymentAdviseList;
	}

	@SuppressWarnings("static-access")
	public List<TransactionSearch> getBillingDetails(List<TransactionSearch> transList) {
		logger.info("inside the getBillingDetails for Booking sale capture");
		try {

			if (!transList.isEmpty()) {
				for (TransactionSearch list : transList) {

					BasicDBObject finalquery = new BasicDBObject(FieldType.ORDER_ID.getName(), list.getOrderId());

					logger.info("Inside TxnReports , billingDetailForDownload , finalquery = " + finalquery);
					MongoDatabase dbIns = mongoInstance.getDB();
					MongoCollection<Document> coll = dbIns.getCollection(
							propertiesManager.propertiesMap.get(prefix + Constants.BILLING_COLLECTION.getValue()));

					MongoCursor<Document> cursor = coll.find(finalquery).iterator();

					while (cursor.hasNext()) {
						Document dbObj = cursor.next();

						if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
								&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
							dbObj = dataEncDecTool.decryptDocument(dbObj);
						}

						if (StringUtils.isNotBlank(dbObj.getString(FieldType.CUST_PHONE.getName()))) {
							list.setCustomerMobile(dbObj.getString(FieldType.CUST_PHONE.getName()));
						} else {
							list.setCustomerMobile("NA");
						}
						cursor.close();
						break;
					}
				}
			}
			return transList;
		} catch (Exception ex) {
			logger.info("Exception caught in TxnReports getBillingDetails()", ex);
		}
		return transList;
	}

	@SuppressWarnings("static-access")
	public List<PaymentSearchDownloadObject> getBillingDetailsForDownload(List<PaymentSearchDownloadObject> transList) {
		logger.info("inside the getBillingDetails for download bookingRecord");
		try {

			if (!transList.isEmpty()) {
				for (PaymentSearchDownloadObject list : transList) {

					BasicDBObject finalquery = new BasicDBObject(FieldType.ORDER_ID.getName(), list.getOrderId());

					logger.info("Inside TxnReports , billingDetailForDownload , finalquery = " + finalquery);
					MongoDatabase dbIns = mongoInstance.getDB();
					MongoCollection<Document> coll = dbIns.getCollection(
							propertiesManager.propertiesMap.get(prefix + Constants.BILLING_COLLECTION.getValue()));

					MongoCursor<Document> cursor = coll.find(finalquery).iterator();

					while (cursor.hasNext()) {
						Document dbObj = cursor.next();

						if (StringUtils.isNotBlank(dbObj.getString(FieldType.CUST_PHONE.getName()))) {
							list.setCustMobile(dbObj.getString(FieldType.CUST_PHONE.getName()));
						} else {
							list.setCustMobile("NA");
						}
						cursor.close();
						break;
					}
				}
			}
			return transList;
		} catch (Exception ex) {
			logger.info("Exception caught in TxnReports getBillingDetails()", ex);
		}
		return transList;
	}

	@SuppressWarnings("static-access")
	public List<TransactionSearch> searchPaymentForBookingRecord(String pgRefNum, String orderId, String custMobile,
			String custEmail, String merchantPayId, String subMerchantPayId, String paymentType, String Userstatus,
			String currency, String transactionType, String fromDate, String toDate, User user, int start, int length,
			String partSettleFlag, String subMerchantId, Set<String> orderIdSet) {
		Map<String, User> userMap = new HashMap<String, User>();

		logger.info("Inside TxnReports , searchPaymentForBookingRecord");
		boolean isGlocal = false;

		try {
			UserSettingData userSettings = userSettingDao.fetchDataUsingPayId(user.getPayId());

			if (!merchantPayId.equalsIgnoreCase("ALL")) {
				String identifierKey = propertiesManager.propertiesMap.get(Constants.MERCHANT_PAYID.getValue());
				if (StringUtils.isNotBlank(identifierKey) && identifierKey.contains(merchantPayId)) {
					isGlocal = true;
				}
			}
			String dispatchIdentifierKey = propertiesManager.propertiesMap
					.get(Constants.DISPATCH_SLIP_MERCHANT_PAYID.getValue());
			List<TransactionSearch> transactionList = new ArrayList<TransactionSearch>();

			PropertiesManager propManager = new PropertiesManager();
			List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();

			BasicDBObject dateIndexConditionQuery = new BasicDBObject();

			DateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.ENGLISH);
			Date dateStart = format.parse(fromDate);
			Date dateEnd = format.parse(toDate);

			LocalDate startDate = dateStart.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			LocalDate endDate = dateEnd.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

			String fromDateIndex = startDate.toString().replaceAll("-", "");
			String toDateIndex = endDate.toString().replaceAll("-", "");

			dateIndexConditionQuery.put("DATE_INDEX",
					BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(fromDateIndex).toLocalizedPattern())
							.add("$lte", new SimpleDateFormat(toDateIndex).toLocalizedPattern()).get());

			if (StringUtils.isNotBlank(merchantPayId) && !merchantPayId.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.PAY_ID.getName(), merchantPayId));
			}
			if (StringUtils.isNotBlank(subMerchantPayId) && !subMerchantPayId.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.SUB_MERCHANT_ID.getName(), subMerchantPayId));
			}
			if (user.getUserType().equals(UserType.RESELLER)) {
				paramConditionLst.add(new BasicDBObject(FieldType.RESELLER_ID.getName(), user.getResellerId()));
			}

			if (StringUtils.isNotBlank(subMerchantId)) {
				paramConditionLst.add(new BasicDBObject(FieldType.SUB_MERCHANT_ID.getName(), subMerchantId));
			}

			if (!pgRefNum.isEmpty()) {
				paramConditionLst.add(new BasicDBObject(FieldType.PG_REF_NUM.getName(), pgRefNum));
			}

			BasicDBObject orderIdConditionQuery = null;
			if (orderIdSet != null) {
				List<String> orderIdList = new ArrayList<>(orderIdSet);
				orderIdConditionQuery = new BasicDBObject("$in", orderIdList);
			}
			if (!orderId.isEmpty()) {
				paramConditionLst.add(new BasicDBObject(FieldType.ORDER_ID.getName(), orderId));
			} else if (null != orderIdConditionQuery) {
				paramConditionLst.add(new BasicDBObject(FieldType.ORDER_ID.getName(), orderIdConditionQuery));
			}

			if (StringUtils.isNotBlank(custEmail) && !custEmail.isEmpty()) {
				paramConditionLst.add(new BasicDBObject(FieldType.CUST_EMAIL.getName(), custEmail));
			}

			if (StringUtils.isNotBlank(Userstatus) && !Userstatus.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.STATUS.getName(), Userstatus));
			}

			if (StringUtils.isNotBlank(currency) && !currency.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.CURRENCY_CODE.getName(), currency));
			}
			if (StringUtils.isNotBlank(partSettleFlag) && !partSettleFlag.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.PART_SETTLE.getName(), partSettleFlag));
			}

			if (StringUtils.isNotBlank(transactionType) && !transactionType.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.ORIG_TXNTYPE.getName(), transactionType));
			}
			if (StringUtils.isNotBlank(paymentType) && !paymentType.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.PAYMENT_TYPE.getName(), paymentType));
			}

			if (StringUtils.isNotBlank(pgRefNum) || StringUtils.isNotBlank(orderId)) {

			} else {
				if (!dateIndexConditionQuery.isEmpty()) {
					paramConditionLst.add(dateIndexConditionQuery);
				}
			}

			BasicDBObject finalquery = new BasicDBObject("$and", paramConditionLst);

			logger.info("Inside TxnReports , searchPaymentForBookingRecord , finalquery = " + finalquery);
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns
					.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));
			BasicDBObject match = new BasicDBObject("$match", finalquery);
			BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject("CREATE_DATE", -1));
			BasicDBObject skip = new BasicDBObject("$skip", start);
			BasicDBObject limit = new BasicDBObject("$limit", length);

			List<BasicDBObject> pipeline = Arrays.asList(match, sort, skip, limit);

			AggregateIterable<Document> output = coll.aggregate(pipeline);
			output.allowDiskUse(true);
			MongoCursor<Document> cursor = output.iterator();

			while (cursor.hasNext()) {

				Document dbobj = cursor.next();
				if (StringUtils.isNotBlank(dbobj.getString("IS_ENCRYPTED"))
						&& dbobj.getString("IS_ENCRYPTED").equalsIgnoreCase("Y")) {
					dbobj = dataEncDecTool.decryptDocument(dbobj);
				}

				TransactionSearch transReport = new TransactionSearch();
				BigInteger txnID = new BigInteger(((Document) dbobj).getString(FieldType.TXN_ID.toString()));
				transReport.setTransactionId((txnID));
				transReport.setPgRefNum(((Document) dbobj).getString(FieldType.PG_REF_NUM.toString()));
				transReport.setPayId(((Document) dbobj).getString(FieldType.PAY_ID.toString()));

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.CUST_NAME.getName()))) {
					transReport.setCustomerName(((Document) dbobj).getString(FieldType.CUST_NAME.toString()));
				} else {
					transReport.setCustomerName(CrmFieldConstants.NA.getValue());
				}

				if (null != ((Document) dbobj).getString(FieldType.CUST_EMAIL.toString())) {
					transReport.setCustomerEmail(((Document) dbobj).getString(FieldType.CUST_EMAIL.toString()));
				} else {
					transReport.setCustomerEmail(CrmFieldConstants.NA.getValue());
				}

				if (dbobj.containsKey(FieldType.CUST_PHONE.getName())
						&& StringUtils.isNotBlank(dbobj.getString(FieldType.CUST_PHONE.getName()))) {
					transReport.setCustomerMobile(((Document) dbobj).getString(FieldType.CUST_PHONE.toString()));
				} else {
					transReport.setCustomerMobile(CrmFieldConstants.NA.getValue());
				}

				if (StringUtils.isNotBlank(dbobj.getString("IS_CUSTOM_HOSTED"))
						&& dbobj.getString("IS_CUSTOM_HOSTED").equalsIgnoreCase("Y")) {
					transReport.setCustomFlag(((Document) dbobj).getString("IS_CUSTOM_HOSTED"));
				} else {
					transReport.setCustomFlag("N");
				}

				/*
				 * if (dbobj.containsKey("CUSTOM_FLAG") &&
				 * StringUtils.isNotBlank(dbobj.getString("CUSTOM_FLAG"))) {
				 * transReport.setCustomFlag(((Document) dbobj).getString("CUSTOM_FLAG")); }
				 * else { transReport.setCustomFlag("N"); }
				 */
				transReport.setMerchants(dbobj.getString(CrmFieldType.BUSINESS_NAME.getName()));

				User user1 = new User();

				if (StringUtils.isNotBlank((String) dbobj.get(FieldType.PAY_ID.getName()))) {
					String payid = (String) dbobj.get(FieldType.PAY_ID.getName());

					if (userMap.get(payid) != null && !userMap.get(payid).getPayId().isEmpty()) {
						user1 = userMap.get(payid);
					} else {
						user1 = userdao.findPayId(payid);
						userMap.put(payid, user1);
					}
				}

				// String payid = (String)
				// dbobj.get(FieldType.PAY_ID.getName());
				// User user1 = new User();
				// if (userMap.get(payid) != null &&
				// !userMap.get(payid).getPayId().isEmpty()) {
				// user1 = userMap.get(payid);
				// } else {
				// user1 = userdao.findPayId(payid);
				// userMap.put(payid, user1);
				// }
				if (user1 != null) {
					transReport.setMerchants(user1.getBusinessName());
				}

				// if (!merchantPayId.equalsIgnoreCase("All") &&
				if (dbobj.containsKey(FieldType.SUB_MERCHANT_ID.getName())
						&& StringUtils.isNotBlank(dbobj.getString(FieldType.SUB_MERCHANT_ID.getName()))) {

					String subMerchant = dbobj.getString(FieldType.SUB_MERCHANT_ID.getName());
					User subMerchantUser = new User();

					if (userMap.get(subMerchant) != null && !userMap.get(subMerchant).getPayId().isEmpty()) {
						subMerchantUser = userMap.get(subMerchant);
					} else {
						subMerchantUser = userdao.findPayId(subMerchant);
						userMap.put(subMerchant, subMerchantUser);
					}
					if (subMerchantUser != null) {
						transReport.setSubMerchantId(subMerchantUser.getBusinessName());
					} else {
						transReport.setSubMerchantId(CrmFieldConstants.NA.getValue());
					}
				} else {
					if ((!pgRefNum.isEmpty() || !orderId.isEmpty())
							&& dbobj.containsKey(FieldType.SUB_MERCHANT_ID.getName())
							&& StringUtils.isNotBlank(dbobj.getString(FieldType.SUB_MERCHANT_ID.getName()))) {
						transReport.setSubMerchantId(
								userdao.getBusinessNameByPayId(dbobj.getString(FieldType.SUB_MERCHANT_ID.getName())));
					}
				}

				if (!merchantPayId.equalsIgnoreCase("ALL")) {
					if (StringUtils.isNotBlank(dispatchIdentifierKey)
							&& dispatchIdentifierKey.contains(merchantPayId)) {
						transReport.setDispatchSlipFlag(true);
						transReport.setInvoiceNo(CrmFieldConstants.NA.getValue());
						transReport.setCourierServiceProvider(CrmFieldConstants.NA.getValue());
						transReport.setDispatchSlipNo(CrmFieldConstants.NA.getValue());

						/*
						 * if (dbobj.containsKey("BASE64")) { transReport.setPdfDownloadFlag(true); }
						 * else { transReport.setPdfDownloadFlag(false); }
						 */
					}
				}

				if ((StringUtils.isNotBlank(dbobj.getString(FieldType.TXNTYPE.getName()))) && (dbobj
						.getString(FieldType.TXNTYPE.toString()).equalsIgnoreCase(TransactionType.REFUND.getName()))) {
					transReport.setTxnType(dbobj.getString(FieldType.TXNTYPE.toString()));
				} else if (StringUtils.isNotBlank(dbobj.getString(FieldType.ORIG_TXNTYPE.toString()))) {
					transReport.setTxnType(dbobj.getString(FieldType.ORIG_TXNTYPE.toString()));
				} else {

					// If ORIG_TXN_TYPE is not available incase of a timeout ,
					// set TXNTYPE instead
					// of ORIG_TXN_TYPE

					if (dbobj.getString(FieldType.STATUS.toString()).equalsIgnoreCase(StatusType.TIMEOUT.getName())) {
						transReport.setTxnType(dbobj.getString(FieldType.TXNTYPE.toString()));
					} else {
						transReport.setTxnType(CrmFieldConstants.NA.getValue());
					}
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.MOP_TYPE.toString()))) {
					transReport.setMopType(MopType.getmopName(dbobj.getString(FieldType.MOP_TYPE.toString())));
				} else {
					transReport.setMopType(CrmFieldConstants.NOT_AVAILABLE.getValue());
				}
				if (StringUtils.isNotBlank(dbobj.getString(FieldType.PAYMENT_TYPE.toString()))) {
					transReport.setPaymentMethods(
							PaymentType.getpaymentName(dbobj.getString(FieldType.PAYMENT_TYPE.toString())));
				} else {
					transReport.setPaymentMethods(CrmFieldConstants.NOT_AVAILABLE.getValue());
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TYPE.getName()))) {
					transReport.setAcquirerType(dbobj.getString(FieldType.ACQUIRER_TYPE.getName()));
				} else {
					transReport.setAcquirerType(CrmFieldConstants.NOT_AVAILABLE.getValue());
				}

				if (merchantPayId.equalsIgnoreCase("All")) {

					if (StringUtils.isNotBlank(dbobj.getString(FieldType.REFUND_DAYS.getName()))) {
						transReport.setRefundCycle(dbobj.getString(FieldType.REFUND_DAYS.getName()));
					} else {
						transReport.setRefundCycle(CrmFieldConstants.NA.getValue());
					}

					if (StringUtils.isNotBlank(dbobj.getString(FieldType.PRODUCT_PRICE.getName()))) {
						transReport.setProductPrice(dbobj.getString(FieldType.PRODUCT_PRICE.getName()));
					} else {
						transReport.setProductPrice(CrmFieldConstants.NA.getValue());
					}
					if (StringUtils.isNotBlank(dbobj.getString(FieldType.VENDOR_ID.getName()))) {
						transReport.setVendorID(dbobj.getString(FieldType.VENDOR_ID.getName()));
					} else {
						transReport.setVendorID(CrmFieldConstants.NA.getValue());
					}

					if (merchantPayId.equalsIgnoreCase("All")) {

						if (StringUtils.isNotBlank(dbobj.getString(FieldType.REFUND_DAYS.getName()))) {
							transReport.setRefundCycle(dbobj.getString(FieldType.REFUND_DAYS.getName()));
						} else {
							transReport.setRefundCycle(CrmFieldConstants.NA.getValue());
						}

						if (StringUtils.isNotBlank(dbobj.getString(FieldType.PRODUCT_PRICE.getName()))) {
							transReport.setProductPrice(dbobj.getString(FieldType.PRODUCT_PRICE.getName()));
						} else {
							transReport.setProductPrice(CrmFieldConstants.NA.getValue());
						}
						if (StringUtils.isNotBlank(dbobj.getString(FieldType.VENDOR_ID.getName()))) {
							transReport.setVendorID(dbobj.getString(FieldType.VENDOR_ID.getName()));
						} else {
							transReport.setVendorID(CrmFieldConstants.NA.getValue());
						}
						if (StringUtils.isNotBlank(dbobj.getString(FieldType.SKU_CODE.getName()))) {
							transReport.setSKUCode(dbobj.getString(FieldType.SKU_CODE.getName()));
						} else {
							transReport.setSKUCode(CrmFieldConstants.NA.getValue());
						}

						if (StringUtils.isNotBlank(dbobj.getString(FieldType.CATEGORY_CODE.getName()))) {
							transReport.setCategoryCode(dbobj.getString(FieldType.CATEGORY_CODE.getName()));
						} else {
							transReport.setCategoryCode(CrmFieldConstants.NA.getValue());
						}
					} else {

						if (user.getUserType().equals(UserType.ADMIN) || user.getUserType().equals(UserType.SUBADMIN)
								|| user.getUserType().equals(UserType.RESELLER)) {
							User merhant = userMap.get(merchantPayId); // userdao.findPayId(payid);
							UserSettingData merchantSettings = userSettingDao.fetchDataUsingPayId(merhant.getPayId());

							if (merchantSettings.isRetailMerchantFlag()) {
								if (StringUtils.isNotBlank(dbobj.getString(FieldType.REFUND_DAYS.getName()))) {
									transReport.setRefundCycle(dbobj.getString(FieldType.REFUND_DAYS.getName()));
								} else {
									transReport.setRefundCycle(CrmFieldConstants.NA.getValue());
								}

								if (StringUtils.isNotBlank(dbobj.getString(FieldType.PRODUCT_PRICE.getName()))) {
									transReport.setProductPrice(dbobj.getString(FieldType.PRODUCT_PRICE.getName()));
								} else {
									transReport.setProductPrice(CrmFieldConstants.NA.getValue());
								}
								if (StringUtils.isNotBlank(dbobj.getString(FieldType.VENDOR_ID.getName()))) {
									transReport.setVendorID(dbobj.getString(FieldType.VENDOR_ID.getName()));
								} else {
									transReport.setVendorID(CrmFieldConstants.NA.getValue());
								}
								if (StringUtils.isNotBlank(dbobj.getString(FieldType.SKU_CODE.getName()))) {
									transReport.setSKUCode(dbobj.getString(FieldType.SKU_CODE.getName()));
								} else {
									transReport.setSKUCode(CrmFieldConstants.NA.getValue());
								}

								if (StringUtils.isNotBlank(dbobj.getString(FieldType.CATEGORY_CODE.getName()))) {
									transReport.setCategoryCode(dbobj.getString(FieldType.CATEGORY_CODE.getName()));
								} else {
									transReport.setCategoryCode(CrmFieldConstants.NA.getValue());
								}
							}
						} else {

							if (userSettings.isRetailMerchantFlag()) {
								if (StringUtils.isNotBlank(dbobj.getString(FieldType.REFUND_DAYS.getName()))) {
									transReport.setRefundCycle(dbobj.getString(FieldType.REFUND_DAYS.getName()));
								} else {
									transReport.setRefundCycle(CrmFieldConstants.NA.getValue());
								}

								if (StringUtils.isNotBlank(dbobj.getString(FieldType.PRODUCT_PRICE.getName()))) {
									transReport.setProductPrice(dbobj.getString(FieldType.PRODUCT_PRICE.getName()));
								} else {
									transReport.setProductPrice(CrmFieldConstants.NA.getValue());
								}
								if (StringUtils.isNotBlank(dbobj.getString(FieldType.VENDOR_ID.getName()))) {
									transReport.setVendorID(dbobj.getString(FieldType.VENDOR_ID.getName()));
								} else {
									transReport.setVendorID(CrmFieldConstants.NA.getValue());
								}
								if (StringUtils.isNotBlank(dbobj.getString(FieldType.SKU_CODE.getName()))) {
									transReport.setSKUCode(dbobj.getString(FieldType.SKU_CODE.getName()));
								} else {
									transReport.setSKUCode(CrmFieldConstants.NA.getValue());
								}

								if (StringUtils.isNotBlank(dbobj.getString(FieldType.CATEGORY_CODE.getName()))) {
									transReport.setCategoryCode(dbobj.getString(FieldType.CATEGORY_CODE.getName()));
								} else {
									transReport.setCategoryCode(CrmFieldConstants.NA.getValue());
								}
							}

						}

					}

				} else {

					if (user.getUserType().equals(UserType.ADMIN) || user.getUserType().equals(UserType.SUBADMIN)
							|| user.getUserType().equals(UserType.RESELLER)) {
						User merhant = userMap.get(merchantPayId); // userdao.findPayId(payid);
						UserSettingData merchantSettings = userSettingDao.fetchDataUsingPayId(merhant.getPayId());

						if (merchantSettings.isRetailMerchantFlag()) {
							if (StringUtils.isNotBlank(dbobj.getString(FieldType.REFUND_DAYS.getName()))) {
								transReport.setRefundCycle(dbobj.getString(FieldType.REFUND_DAYS.getName()));
							} else {
								transReport.setRefundCycle(CrmFieldConstants.NA.getValue());
							}

							if (StringUtils.isNotBlank(dbobj.getString(FieldType.PRODUCT_PRICE.getName()))) {
								transReport.setProductPrice(dbobj.getString(FieldType.PRODUCT_PRICE.getName()));
							} else {
								transReport.setProductPrice(CrmFieldConstants.NA.getValue());
							}
							if (StringUtils.isNotBlank(dbobj.getString(FieldType.VENDOR_ID.getName()))) {
								transReport.setVendorID(dbobj.getString(FieldType.VENDOR_ID.getName()));
							} else {
								transReport.setVendorID(CrmFieldConstants.NA.getValue());
							}
							if (StringUtils.isNotBlank(dbobj.getString(FieldType.SKU_CODE.getName()))) {
								transReport.setSKUCode(dbobj.getString(FieldType.SKU_CODE.getName()));
							} else {
								transReport.setSKUCode(CrmFieldConstants.NA.getValue());
							}

							if (StringUtils.isNotBlank(dbobj.getString(FieldType.CATEGORY_CODE.getName()))) {
								transReport.setCategoryCode(dbobj.getString(FieldType.CATEGORY_CODE.getName()));
							} else {
								transReport.setCategoryCode(CrmFieldConstants.NA.getValue());
							}
						}
					} else {

						if (userSettings.isRetailMerchantFlag()) {
							if (StringUtils.isNotBlank(dbobj.getString(FieldType.REFUND_DAYS.getName()))) {
								transReport.setRefundCycle(dbobj.getString(FieldType.REFUND_DAYS.getName()));
							} else {
								transReport.setRefundCycle(CrmFieldConstants.NA.getValue());
							}

							if (StringUtils.isNotBlank(dbobj.getString(FieldType.PRODUCT_PRICE.getName()))) {
								transReport.setProductPrice(dbobj.getString(FieldType.PRODUCT_PRICE.getName()));
							} else {
								transReport.setProductPrice(CrmFieldConstants.NA.getValue());
							}
							if (StringUtils.isNotBlank(dbobj.getString(FieldType.VENDOR_ID.getName()))) {
								transReport.setVendorID(dbobj.getString(FieldType.VENDOR_ID.getName()));
							} else {
								transReport.setVendorID(CrmFieldConstants.NA.getValue());
							}
							if (StringUtils.isNotBlank(dbobj.getString(FieldType.SKU_CODE.getName()))) {
								transReport.setSKUCode(dbobj.getString(FieldType.SKU_CODE.getName()));
							} else {
								transReport.setSKUCode(CrmFieldConstants.NA.getValue());
							}

							if (StringUtils.isNotBlank(dbobj.getString(FieldType.CATEGORY_CODE.getName()))) {
								transReport.setCategoryCode(dbobj.getString(FieldType.CATEGORY_CODE.getName()));
							} else {
								transReport.setCategoryCode(CrmFieldConstants.NA.getValue());
							}
						}

					}

				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQ_ID.getName()))) {
					transReport.setAcqId(dbobj.getString(FieldType.ACQ_ID.getName()));
				} else {
					transReport.setAcqId(CrmFieldConstants.NA.getValue());
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.RRN.getName()))) {
					transReport.setRrn(dbobj.getString(FieldType.RRN.getName()));
				} else {
					transReport.setRrn(CrmFieldConstants.NA.getValue());
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.TXN_CAPTURE_FLAG.getName()))) {
					transReport.setTxnSettledType(dbobj.getString(FieldType.TXN_CAPTURE_FLAG.getName()));
				} else {
					transReport.setTxnSettledType(CrmFieldConstants.NA.getValue());
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.TRANSACTION_MODE.getName()))) {
					transReport.setTransactionMode(dbobj.getString(FieldType.TRANSACTION_MODE.getName()));
				} else {
				}

				// if
				// (StringUtils.isNotBlank(dbobj.getString(FieldType.POST_SETTLED_FLAG.getName())))
				// {
				// transReport.setPostSettledFlag(dbobj.getString(FieldType.POST_SETTLED_FLAG.getName()));
				// } else {
				// transReport.setPostSettledFlag(CrmFieldConstants.NA.getValue());
				// }

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.PAYMENT_TYPE.toString()))) {
					if (null != dbobj.getString(FieldType.CARD_MASK.toString())) {
						transReport.setCardNumber(dbobj.getString(FieldType.CARD_MASK.toString()));
					} else if (null != dbobj.getString(FieldType.PAYER_ADDRESS.getName())) {

						if (user.getUserType().equals(UserType.ADMIN) || user.getUserType().equals(UserType.SUBADMIN)) {
							transReport.setCardNumber(dbobj.getString(FieldType.PAYER_ADDRESS.getName()));
						} else {
							String vpaString = dbobj.getString(FieldType.PAYER_ADDRESS.getName());
							String[] vpaArray = vpaString.split("@");
							char[] vpaChar = vpaArray[0].toCharArray();
							StringBuilder vpastrBuilder = new StringBuilder();

							if (vpaChar.length > 3) {
								for (int i = 0; i < vpaChar.length - 3; i++) {
									vpastrBuilder.append(vpaChar[i]);
								}
								vpastrBuilder.append("***@");
								vpastrBuilder.append(vpaArray[1]);
							} else {
								vpastrBuilder.append(vpaChar[0]);
								vpastrBuilder.append("**@");
								vpastrBuilder.append(vpaArray[1]);
							}

							transReport.setCardNumber(vpastrBuilder.toString());
						}
					} else {
						transReport.setCardNumber(CrmFieldConstants.NA.getValue());
					}
				} else {
					transReport.setCardNumber(CrmFieldConstants.NA.getValue());
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.PART_SETTLE.toString()))
						&& dbobj.getString(FieldType.PART_SETTLE.toString()).equalsIgnoreCase(("Y"))) {
					transReport.setPartSettle(dbobj.getString(FieldType.PART_SETTLE.toString()));
				} else {
					transReport.setPartSettle(CrmFieldConstants.NA.getValue());
				}

				transReport.setStatus(dbobj.getString(FieldType.STATUS.toString()));
				transReport.setDateFrom(dbobj.getString(FieldType.CREATE_DATE.getName()));
				transReport.setAmount(dbobj.getString(FieldType.AMOUNT.toString()));
				transReport.setOrderId(dbobj.getString(FieldType.ORDER_ID.toString()));

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.REFUND_ORDER_ID.toString()))) {
					transReport.setRefundOrderId(dbobj.getString(FieldType.REFUND_ORDER_ID.toString()));
				} else {
					transReport.setRefundOrderId(CrmFieldConstants.NA.getValue());
				}

				transReport.setoId(dbobj.getString(FieldType.OID.toString()));

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.PRODUCT_DESC.toString()))) {
					transReport.setProductDesc(dbobj.getString(FieldType.PRODUCT_DESC.toString()));
				} else {
					transReport.setProductDesc(CrmFieldConstants.NA.getValue());
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.PG_DATE_TIME.getName()))) {
					transReport.setTransactionCaptureDate(dbobj.getString(FieldType.PG_DATE_TIME.toString()));
				} else {
					transReport.setTransactionCaptureDate(CrmFieldConstants.NA.getValue());
				}

				if (transReport.getTxnType().contains(TransactionType.REFUND.getName())) {
					transReport.setTxnType(TransactionType.REFUND.getName());
				} else {
					transReport.setTxnType(TransactionType.SALE.getName());
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.INTERNAL_CARD_ISSUER_BANK.getName()))) {
					transReport.setInternalCardIssusserBank(
							dbobj.getString(FieldType.INTERNAL_CARD_ISSUER_BANK.toString()));
				} else {
					transReport.setInternalCardIssusserBank(CrmFieldConstants.NA.getValue());
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.INTERNAL_CARD_ISSUER_COUNTRY.getName()))) {
					transReport.setInternalCardIssusserCountry(
							dbobj.getString(FieldType.INTERNAL_CARD_ISSUER_COUNTRY.toString()));
				} else {
					transReport.setInternalCardIssusserCountry(CrmFieldConstants.NA.getValue());
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.REFUNDABLE_AMOUNT.getName()))) {
					transReport.setRefundableAmount(dbobj.getString(FieldType.REFUNDABLE_AMOUNT.toString()));
				} else {
					transReport.setRefundableAmount(CrmFieldConstants.NA.getValue());
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.AMOUNT.getName()))) {
					transReport.setApprovedAmount(dbobj.getString(FieldType.AMOUNT.toString()));
				} else {
					transReport.setApprovedAmount(CrmFieldConstants.NA.getValue());
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))) {
					transReport.setTotalAmount(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()));
				} else {
					transReport.setTotalAmount("");
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.CURRENCY_CODE.toString()))) {
					transReport.setCurrency(
							propManager.getAlphabaticCurrencyCode(dbobj.getString(FieldType.CURRENCY_CODE.toString())));
				} else {
					transReport.setCurrency(CrmFieldConstants.NA.getValue());
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.PAYMENTS_REGION.toString()))) {
					transReport.setPaymentRegion(dbobj.getString(FieldType.PAYMENTS_REGION.toString()));

				} else {
					transReport.setPaymentRegion(CrmFieldConstants.NA.getValue());
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.CARD_HOLDER_TYPE.toString()))) {
					transReport.setCardHolderType(dbobj.getString(FieldType.CARD_HOLDER_TYPE.toString()));

				} else {
					transReport.setCardHolderType(CrmFieldConstants.NA.getValue());
				}

				if (dbobj.containsKey(FieldType.RESELLER_CHARGES.getName())
						&& StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_CHARGES.toString()))
						&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
						&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.toString()))) {

					transReport.setTdr_Surcharge(String.format("%.2f",
							(Double.parseDouble(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
									+ Double.parseDouble(dbobj.getString(FieldType.PG_TDR_SC.toString()))
									+ Double.parseDouble(dbobj.getString(FieldType.RESELLER_CHARGES.toString())))));

				} else {

					if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
							&& (StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.toString())))) {

						transReport.setTdr_Surcharge(String.format("%.2f",
								(Double.parseDouble(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
										+ Double.parseDouble(dbobj.getString(FieldType.PG_TDR_SC.toString())))));

					} else {
						transReport.setTdr_Surcharge(CrmFieldConstants.NA.getValue());
					}
				}

				if (dbobj.containsKey(FieldType.RESELLER_GST.getName())
						&& StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_GST.toString()))
						&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.toString()))
						&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.toString()))) {

					transReport.setGst_charge(String.format("%.2f",
							(Double.parseDouble(dbobj.getString(FieldType.ACQUIRER_GST.toString()))
									+ Double.parseDouble(dbobj.getString(FieldType.PG_GST.toString()))
									+ Double.parseDouble(dbobj.getString(FieldType.RESELLER_GST.getName())))));

				} else {

					if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.toString()))
							&& (StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.toString())))) {
						transReport.setGst_charge(String.format("%.2f",
								(Double.parseDouble(dbobj.getString(FieldType.ACQUIRER_GST.toString()))
										+ Double.parseDouble(dbobj.getString(FieldType.PG_GST.toString())))));

					} else {
						transReport.setGst_charge(CrmFieldConstants.NA.getValue());
					}
				}

				if (TxnType.SALE.getName().equalsIgnoreCase(dbobj.getString(FieldType.ORIG_TXNTYPE.getName()))) {

					if (StringUtils.isNotBlank(dbobj.getString(FieldType.PAYMENT_TYPE.toString()))) {

						if (dbobj.getString(FieldType.PAYMENT_TYPE.getName())
								.equalsIgnoreCase(PaymentType.COD.getCode())) {

							if (isGlocal == true) {

								transReport.setGlocalFlag(true);

								if (dbobj.containsKey(FieldType.UDF7.getName())) {

									if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF7.getName()))) {

										String merchantAmount = null;
										if (dbobj.containsKey(FieldType.RESELLER_CHARGES.getName())
												&& dbobj.containsKey(FieldType.RESELLER_GST.getName())
												&& StringUtils.isNotBlank(
														dbobj.getString(FieldType.RESELLER_CHARGES.getName()))
												&& StringUtils
														.isNotBlank(dbobj.getString(FieldType.RESELLER_GST.getName()))
												&& StringUtils
														.isNotBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
												&& StringUtils
														.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.toString()))
												&& StringUtils.isNotBlank(
														dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
												&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.toString()))
												&& StringUtils.isNotBlank(
														dbobj.getString(FieldType.ACQUIRER_GST.toString()))) {

											merchantAmount = String.format("%.2f", Double
													.parseDouble(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
													- (Double.parseDouble(
															dbobj.getString(FieldType.PG_TDR_SC.toString()))
															+ Double.parseDouble(dbobj
																	.getString(FieldType.ACQUIRER_TDR_SC.toString()))
															+ Double.parseDouble(
																	dbobj.getString(FieldType.PG_GST.toString()))
															+ Double.parseDouble(dbobj
																	.getString(FieldType.ACQUIRER_GST.toString()))));

											transReport.setDoctor(String
													.valueOf(new BigDecimal(dbobj.getString(FieldType.UDF7.getName()))
															.multiply(new BigDecimal(merchantAmount))
															.divide(new BigDecimal(
																	Constants.MAX_NUMBER_OF_KEYS.getValue()))
															.setScale(2, BigDecimal.ROUND_HALF_UP)));

										} else {
											if (StringUtils
													.isNotBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
													&& StringUtils
															.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.toString()))
													&& StringUtils.isNotBlank(
															dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
													&& StringUtils
															.isNotBlank(dbobj.getString(FieldType.PG_GST.toString()))
													&& StringUtils.isNotBlank(
															dbobj.getString(FieldType.ACQUIRER_GST.toString()))) {

												merchantAmount = String.format("%.2f", Double
														.parseDouble(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
														- (Double.parseDouble(
																dbobj.getString(FieldType.PG_TDR_SC.toString()))
																+ Double.parseDouble(dbobj.getString(
																		FieldType.ACQUIRER_TDR_SC.toString()))
																+ Double.parseDouble(
																		dbobj.getString(FieldType.PG_GST.toString()))
																+ Double.parseDouble(dbobj.getString(
																		FieldType.ACQUIRER_GST.toString()))));

												transReport.setDoctor(String.valueOf(
														new BigDecimal(dbobj.getString(FieldType.UDF7.getName()))
																.multiply(new BigDecimal(merchantAmount))
																.divide(new BigDecimal(
																		Constants.MAX_NUMBER_OF_KEYS.getValue()))
																.setScale(2, BigDecimal.ROUND_HALF_UP)));
											} else {
												transReport.setDoctor("0");
											}
										}
									} else {
										transReport.setDoctor("0");
									}
								} else {
									transReport.setDoctor("0");
								}

								if (dbobj.containsKey(FieldType.UDF8.getName())) {
									if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF8.getName()))) {

										String merchantAmount = null;
										if (dbobj.containsKey(FieldType.RESELLER_CHARGES.getName())
												&& dbobj.containsKey(FieldType.RESELLER_GST.getName())
												&& StringUtils.isNotBlank(
														dbobj.getString(FieldType.RESELLER_CHARGES.getName()))
												&& StringUtils
														.isNotBlank(dbobj.getString(FieldType.RESELLER_GST.getName()))
												&& StringUtils
														.isNotBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
												&& StringUtils
														.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.toString()))
												&& StringUtils.isNotBlank(
														dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
												&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.toString()))
												&& StringUtils.isNotBlank(
														dbobj.getString(FieldType.ACQUIRER_GST.toString()))) {

											merchantAmount = String.format("%.2f", Double
													.parseDouble(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
													- (Double.parseDouble(
															dbobj.getString(FieldType.PG_TDR_SC.toString()))
															+ Double.parseDouble(dbobj
																	.getString(FieldType.ACQUIRER_TDR_SC.toString()))
															+ Double.parseDouble(
																	dbobj.getString(FieldType.PG_GST.toString()))
															+ Double.parseDouble(dbobj
																	.getString(FieldType.ACQUIRER_GST.toString()))));

											transReport.setGlocal(String
													.valueOf(new BigDecimal(dbobj.getString(FieldType.UDF8.getName()))
															.multiply(new BigDecimal(merchantAmount))
															.divide(new BigDecimal(
																	Constants.MAX_NUMBER_OF_KEYS.getValue()))
															.setScale(2, BigDecimal.ROUND_HALF_UP)));

										} else {

											if (StringUtils
													.isNotBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
													&& StringUtils
															.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.toString()))
													&& StringUtils.isNotBlank(
															dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
													&& StringUtils
															.isNotBlank(dbobj.getString(FieldType.PG_GST.toString()))
													&& StringUtils.isNotBlank(
															dbobj.getString(FieldType.ACQUIRER_GST.toString()))) {

												merchantAmount = String.format("%.2f", Double
														.parseDouble(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
														- (Double.parseDouble(
																dbobj.getString(FieldType.PG_TDR_SC.toString()))
																+ Double.parseDouble(dbobj.getString(
																		FieldType.ACQUIRER_TDR_SC.toString()))
																+ Double.parseDouble(
																		dbobj.getString(FieldType.PG_GST.toString()))
																+ Double.parseDouble(dbobj.getString(
																		FieldType.ACQUIRER_GST.toString()))));

												transReport.setGlocal(String.valueOf(
														new BigDecimal(dbobj.getString(FieldType.UDF8.getName()))
																.multiply(new BigDecimal(merchantAmount))
																.divide(new BigDecimal(
																		Constants.MAX_NUMBER_OF_KEYS.getValue()))
																.setScale(2, BigDecimal.ROUND_HALF_UP)));
											} else {
												transReport.setGlocal("0");
											}
										}
									} else {
										transReport.setGlocal("0");
									}
								} else {
									transReport.setGlocal("0");
								}

								if (dbobj.containsKey(FieldType.UDF9.getName())) {
									if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF9.getName()))) {

										String merchantAmount = null;
										if (dbobj.containsKey(FieldType.RESELLER_CHARGES.getName())
												&& dbobj.containsKey(FieldType.RESELLER_GST.getName())
												&& StringUtils.isNotBlank(
														dbobj.getString(FieldType.RESELLER_CHARGES.getName()))
												&& StringUtils
														.isNotBlank(dbobj.getString(FieldType.RESELLER_GST.getName()))
												&& StringUtils
														.isNotBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
												&& StringUtils
														.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.toString()))
												&& StringUtils.isNotBlank(
														dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
												&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.toString()))
												&& StringUtils.isNotBlank(
														dbobj.getString(FieldType.ACQUIRER_GST.toString()))) {

											merchantAmount = String.format("%.2f", Double
													.parseDouble(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
													- (Double.parseDouble(
															dbobj.getString(FieldType.PG_TDR_SC.toString()))
															+ Double.parseDouble(dbobj
																	.getString(FieldType.ACQUIRER_TDR_SC.toString()))
															+ Double.parseDouble(
																	dbobj.getString(FieldType.PG_GST.toString()))
															+ Double.parseDouble(dbobj
																	.getString(FieldType.ACQUIRER_GST.toString()))));

											transReport.setPartner(String
													.valueOf(new BigDecimal(dbobj.getString(FieldType.UDF9.getName()))
															.multiply(new BigDecimal(merchantAmount))
															.divide(new BigDecimal(
																	Constants.MAX_NUMBER_OF_KEYS.getValue()))
															.setScale(2, BigDecimal.ROUND_HALF_UP)));

										} else {

											if (StringUtils
													.isNotBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
													&& StringUtils
															.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.toString()))
													&& StringUtils.isNotBlank(
															dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
													&& StringUtils
															.isNotBlank(dbobj.getString(FieldType.PG_GST.toString()))
													&& StringUtils.isNotBlank(
															dbobj.getString(FieldType.ACQUIRER_GST.toString()))) {

												merchantAmount = String.format("%.2f", Double
														.parseDouble(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
														- (Double.parseDouble(
																dbobj.getString(FieldType.PG_TDR_SC.toString()))
																+ Double.parseDouble(dbobj.getString(
																		FieldType.ACQUIRER_TDR_SC.toString()))
																+ Double.parseDouble(
																		dbobj.getString(FieldType.PG_GST.toString()))
																+ Double.parseDouble(dbobj.getString(
																		FieldType.ACQUIRER_GST.toString()))));

												transReport.setPartner(String.valueOf(
														new BigDecimal(dbobj.getString(FieldType.UDF9.getName()))
																.multiply(new BigDecimal(merchantAmount))
																.divide(new BigDecimal(
																		Constants.MAX_NUMBER_OF_KEYS.getValue()))
																.setScale(2, BigDecimal.ROUND_HALF_UP)));
											} else {
												transReport.setPartner("0");
											}
										}
									} else {
										transReport.setPartner("0");
									}
								} else {
									transReport.setPartner("0");
								}

								if (dbobj.containsKey(FieldType.UDF11.getName())) {
									if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF11.getName()))) {
										transReport.setUniqueId(dbobj.getString(FieldType.UDF11.getName()));
									} else {
										transReport.setUniqueId("NA");
									}
								} else {
									transReport.setUniqueId("NA");
								}
							} else {
								transReport.setGlocalFlag(false);
							}

							if (dbobj.containsKey(FieldType.RESELLER_CHARGES.getName())
									&& dbobj.containsKey(FieldType.RESELLER_GST.getName())
									&& StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_CHARGES.toString()))
									&& StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_GST.toString()))
									&& StringUtils.isNotBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
									&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.toString()))
									&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
									&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.toString()))
									&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.toString()))) {

								transReport.setTotalAmtPayable(String.format("%.2f", (Double
										.parseDouble(dbobj.getString(FieldType.PG_TDR_SC.toString()))
										+ Double.parseDouble(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
										+ Double.parseDouble(dbobj.getString(FieldType.PG_GST.toString()))
										+ Double.parseDouble(dbobj.getString(FieldType.ACQUIRER_GST.toString()))
										+ Double.parseDouble(dbobj.getString(FieldType.RESELLER_CHARGES.toString()))
										+ Double.parseDouble(dbobj.getString(FieldType.RESELLER_GST.toString())))));

								if (transReport.getTotalAmtPayable().equalsIgnoreCase("0.00")) {
									transReport.setTotalAmtPayable(transReport.getTotalAmtPayable());
								} else {
									transReport.setTotalAmtPayable("-" + transReport.getTotalAmtPayable());
								}
							} else {

								if (StringUtils.isNotBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
										&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.toString()))
										&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
										&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.toString()))
										&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.toString()))) {
									transReport.setTotalAmtPayable(String.format("%.2f", (Double
											.parseDouble(dbobj.getString(FieldType.PG_TDR_SC.toString()))
											+ Double.parseDouble(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
											+ Double.parseDouble(dbobj.getString(FieldType.PG_GST.toString()))
											+ Double.parseDouble(dbobj.getString(FieldType.ACQUIRER_GST.toString())))));

									if (transReport.getTotalAmtPayable().equalsIgnoreCase("0.00")) {
										transReport.setTotalAmtPayable(transReport.getTotalAmtPayable());
									} else {
										transReport.setTotalAmtPayable("-" + transReport.getTotalAmtPayable());
									}

								} else {
									transReport.setTotalAmtPayable("NA");
								}
							}

						} else if (isGlocal == true) {

							transReport.setGlocalFlag(true);

							if (dbobj.containsKey(FieldType.UDF7.getName())) {
								if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF7.getName()))) {

									String merchantAmount = null;
									if (dbobj.containsKey(FieldType.RESELLER_CHARGES.getName())
											&& dbobj.containsKey(FieldType.RESELLER_CHARGES.getName())
											&& StringUtils
													.isNotBlank(dbobj.getString(FieldType.RESELLER_CHARGES.toString()))
											&& StringUtils
													.isNotBlank(dbobj.getString(FieldType.RESELLER_GST.toString()))
											&& StringUtils
													.isNotBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
											&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.toString()))
											&& StringUtils
													.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
											&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.toString()))
											&& StringUtils
													.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.toString()))) {

										merchantAmount = String.format("%.2f", Double
												.parseDouble(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
												- (Double.parseDouble(dbobj.getString(FieldType.PG_TDR_SC.toString()))
														+ Double.parseDouble(
																dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
														+ Double.parseDouble(
																dbobj.getString(FieldType.PG_GST.toString()))
														+ Double.parseDouble(
																dbobj.getString(FieldType.ACQUIRER_GST.toString()))
														+ Double.parseDouble(
																dbobj.getString(FieldType.RESELLER_CHARGES.toString()))
														+ Double.parseDouble(
																dbobj.getString(FieldType.RESELLER_GST.toString()))));

										transReport.setDoctor(
												String.valueOf(new BigDecimal(dbobj.getString(FieldType.UDF7.getName()))
														.multiply(new BigDecimal(merchantAmount))
														.divide(new BigDecimal(Constants.MAX_NUMBER_OF_KEYS.getValue()))
														.setScale(2, BigDecimal.ROUND_HALF_UP)));

									} else {

										if (StringUtils.isNotBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
												&& StringUtils
														.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.toString()))
												&& StringUtils.isNotBlank(
														dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
												&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.toString()))
												&& StringUtils.isNotBlank(
														dbobj.getString(FieldType.ACQUIRER_GST.toString()))) {

											merchantAmount = String.format("%.2f", Double
													.parseDouble(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
													- (Double.parseDouble(
															dbobj.getString(FieldType.PG_TDR_SC.toString()))
															+ Double.parseDouble(dbobj
																	.getString(FieldType.ACQUIRER_TDR_SC.toString()))
															+ Double.parseDouble(
																	dbobj.getString(FieldType.PG_GST.toString()))
															+ Double.parseDouble(dbobj
																	.getString(FieldType.ACQUIRER_GST.toString()))));

											transReport.setDoctor(String
													.valueOf(new BigDecimal(dbobj.getString(FieldType.UDF7.getName()))
															.multiply(new BigDecimal(merchantAmount))
															.divide(new BigDecimal(
																	Constants.MAX_NUMBER_OF_KEYS.getValue()))
															.setScale(2, BigDecimal.ROUND_HALF_UP)));
										} else {
											transReport.setDoctor("0");
										}
									}
								} else {
									transReport.setDoctor("0");
								}
							} else {
								transReport.setDoctor("0");
							}

							if (dbobj.containsKey(FieldType.UDF8.getName())) {
								if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF8.getName()))) {

									String merchantAmount = null;
									if (dbobj.containsKey(FieldType.RESELLER_CHARGES.getName())
											&& dbobj.containsKey(FieldType.RESELLER_CHARGES.getName())
											&& StringUtils
													.isNotBlank(dbobj.getString(FieldType.RESELLER_CHARGES.toString()))
											&& StringUtils
													.isNotBlank(dbobj.getString(FieldType.RESELLER_GST.toString()))
											&& StringUtils
													.isNotBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
											&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.toString()))
											&& StringUtils
													.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
											&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.toString()))
											&& StringUtils
													.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.toString()))) {

										merchantAmount = String.format("%.2f", Double
												.parseDouble(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
												- (Double.parseDouble(dbobj.getString(FieldType.PG_TDR_SC.toString()))
														+ Double.parseDouble(
																dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
														+ Double.parseDouble(
																dbobj.getString(FieldType.PG_GST.toString()))
														+ Double.parseDouble(
																dbobj.getString(FieldType.ACQUIRER_GST.toString()))
														+ Double.parseDouble(
																dbobj.getString(FieldType.RESELLER_CHARGES.toString()))
														+ Double.parseDouble(
																dbobj.getString(FieldType.RESELLER_GST.toString()))));

										transReport.setGlocal(
												String.valueOf(new BigDecimal(dbobj.getString(FieldType.UDF8.getName()))
														.multiply(new BigDecimal(merchantAmount))
														.divide(new BigDecimal(Constants.MAX_NUMBER_OF_KEYS.getValue()))
														.setScale(2, BigDecimal.ROUND_HALF_UP)));

									} else {

										if (StringUtils.isNotBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
												&& StringUtils
														.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.toString()))
												&& StringUtils.isNotBlank(
														dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
												&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.toString()))
												&& StringUtils.isNotBlank(
														dbobj.getString(FieldType.ACQUIRER_GST.toString()))) {

											merchantAmount = String.format("%.2f", Double
													.parseDouble(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
													- (Double.parseDouble(
															dbobj.getString(FieldType.PG_TDR_SC.toString()))
															+ Double.parseDouble(dbobj
																	.getString(FieldType.ACQUIRER_TDR_SC.toString()))
															+ Double.parseDouble(
																	dbobj.getString(FieldType.PG_GST.toString()))
															+ Double.parseDouble(dbobj
																	.getString(FieldType.ACQUIRER_GST.toString()))));

											transReport.setGlocal(String
													.valueOf(new BigDecimal(dbobj.getString(FieldType.UDF8.getName()))
															.multiply(new BigDecimal(merchantAmount))
															.divide(new BigDecimal(
																	Constants.MAX_NUMBER_OF_KEYS.getValue()))
															.setScale(2, BigDecimal.ROUND_HALF_UP)));
										} else {
											transReport.setGlocal("0");
										}
									}
								} else {
									transReport.setGlocal("0");
								}
							} else {
								transReport.setGlocal("0");
							}

							if (dbobj.containsKey(FieldType.UDF9.getName())) {
								if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF9.getName()))) {

									String merchantAmount = null;
									if (dbobj.containsKey(FieldType.RESELLER_CHARGES.getName())
											&& dbobj.containsKey(FieldType.RESELLER_CHARGES.getName())
											&& StringUtils
													.isNotBlank(dbobj.getString(FieldType.RESELLER_CHARGES.toString()))
											&& StringUtils
													.isNotBlank(dbobj.getString(FieldType.RESELLER_GST.toString()))
											&& StringUtils
													.isNotBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
											&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.toString()))
											&& StringUtils
													.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
											&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.toString()))
											&& StringUtils
													.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.toString()))) {

										merchantAmount = String.format("%.2f", Double
												.parseDouble(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
												- (Double.parseDouble(dbobj.getString(FieldType.PG_TDR_SC.toString()))
														+ Double.parseDouble(
																dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
														+ Double.parseDouble(
																dbobj.getString(FieldType.PG_GST.toString()))
														+ Double.parseDouble(
																dbobj.getString(FieldType.ACQUIRER_GST.toString()))
														+ Double.parseDouble(
																dbobj.getString(FieldType.RESELLER_CHARGES.toString()))
														+ Double.parseDouble(
																dbobj.getString(FieldType.RESELLER_GST.toString()))));

										transReport.setPartner(
												String.valueOf(new BigDecimal(dbobj.getString(FieldType.UDF9.getName()))
														.multiply(new BigDecimal(merchantAmount))
														.divide(new BigDecimal(Constants.MAX_NUMBER_OF_KEYS.getValue()))
														.setScale(2, BigDecimal.ROUND_HALF_UP)));

									} else {

										if (StringUtils.isNotBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
												&& StringUtils
														.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.toString()))
												&& StringUtils.isNotBlank(
														dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
												&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.toString()))
												&& StringUtils.isNotBlank(
														dbobj.getString(FieldType.ACQUIRER_GST.toString()))) {

											merchantAmount = String.format("%.2f", Double
													.parseDouble(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
													- (Double.parseDouble(
															dbobj.getString(FieldType.PG_TDR_SC.toString()))
															+ Double.parseDouble(dbobj
																	.getString(FieldType.ACQUIRER_TDR_SC.toString()))
															+ Double.parseDouble(
																	dbobj.getString(FieldType.PG_GST.toString()))
															+ Double.parseDouble(dbobj
																	.getString(FieldType.ACQUIRER_GST.toString()))));

											transReport.setPartner(String
													.valueOf(new BigDecimal(dbobj.getString(FieldType.UDF9.getName()))
															.multiply(new BigDecimal(merchantAmount))
															.divide(new BigDecimal(
																	Constants.MAX_NUMBER_OF_KEYS.getValue()))
															.setScale(2, BigDecimal.ROUND_HALF_UP)));
										} else {
											transReport.setPartner("0");
										}
									}
								} else {
									transReport.setPartner("0");
								}
							} else {
								transReport.setPartner("0");
							}

							if (dbobj.containsKey(FieldType.UDF11.getName())) {
								if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF11.getName()))) {
									transReport.setUniqueId(dbobj.getString(FieldType.UDF11.getName()));
								} else {
									transReport.setUniqueId("NA");
								}
							} else {
								transReport.setUniqueId("NA");
							}
						} else {
							transReport.setGlocalFlag(false);
						}

						if (!dbobj.getString(FieldType.PAYMENT_TYPE.getName())
								.equalsIgnoreCase(PaymentType.COD.getCode())) {

							if (dbobj.containsKey(FieldType.RESELLER_CHARGES.getName())
									&& dbobj.containsKey(FieldType.RESELLER_GST.getName())
									&& StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_CHARGES.toString()))
									&& StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_GST.toString()))
									&& StringUtils.isNotBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
									&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
									&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.toString()))
									&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.toString()))
									&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.toString()))) {

								transReport.setTotalAmtPayable(String.format("%.2f",
										Double.parseDouble(dbobj.getString(FieldType.TOTAL_AMOUNT.toString())) - (Double
												.parseDouble(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
												+ Double.parseDouble(dbobj.getString(FieldType.ACQUIRER_GST.toString()))
												+ Double.parseDouble(dbobj.getString(FieldType.PG_TDR_SC.toString()))
												+ Double.parseDouble(dbobj.getString(FieldType.PG_GST.toString()))
												+ Double.parseDouble(
														dbobj.getString(FieldType.RESELLER_CHARGES.toString()))
												+ Double.parseDouble(
														dbobj.getString(FieldType.RESELLER_GST.toString())))));
							} else {

								if (StringUtils.isNotBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
										&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
										&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.toString()))
										&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.toString()))
										&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.toString()))) {
									transReport.setTotalAmtPayable(String.format("%.2f", Double
											.parseDouble(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
											- (Double.parseDouble(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
													+ Double.parseDouble(
															dbobj.getString(FieldType.ACQUIRER_GST.toString()))
													+ Double.parseDouble(
															dbobj.getString(FieldType.PG_TDR_SC.toString()))
													+ Double.parseDouble(
															dbobj.getString(FieldType.PG_GST.toString())))));
								} else {
									transReport.setTotalAmtPayable("NA");
								}
							}
						}

					} else {
						transReport.setTotalAmtPayable("NA");
					}

				} else if (TxnType.REFUND.getName()
						.equalsIgnoreCase(dbobj.getString(FieldType.ORIG_TXNTYPE.getName()))) {
					if (isGlocal == true) {
						transReport.setGlocalFlag(true);
					} else {
						transReport.setGlocalFlag(false);
					}
					if (StringUtils.isNotBlank(dbobj.getString(FieldType.PAYMENT_TYPE.getName()))) {

						if (dbobj.getString(FieldType.PAYMENT_TYPE.getName())
								.equalsIgnoreCase(PaymentType.COD.getCode())) {
							transReport.setGst_charge("0.00");
							transReport.setTdr_Surcharge("0.00");
							transReport.setTotalAmtPayable("0.00");
						} else {
							if (StringUtils.isNotBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))) {
								transReport.setTotalAmtPayable(String.format("%.2f",
										Double.parseDouble(dbobj.getString(FieldType.TOTAL_AMOUNT.toString())) * -1));
							} else {
								transReport.setTotalAmtPayable("NA");
							}
						}

					} else {
						transReport.setTotalAmtPayable("NA");
					}

				} else {
					transReport.setTotalAmtPayable("NA");
				}

				// Exclude certain records when loading search payment without
				// any filters

				Comparator<TransactionSearch> comp = (TransactionSearch a, TransactionSearch b) -> {

					if (a.getDateFrom().compareTo(b.getDateFrom()) > 0) {
						return -1;
					} else if (a.getDateFrom().compareTo(b.getDateFrom()) < 0) {
						return 1;
					} else {
						return 0;
					}
				};
				transactionList.add(transReport);
				Collections.sort(transactionList, comp);
			}
			cursor.close();
			logger.info("Inside TxnReports , searchPaymentForBookingRecord , transactionListSize = "
					+ transactionList.size());
			return transactionList;
		} catch (Exception e) {
			logger.error("Exception occured in TxnReports , searchPaymentForBookingRecord , Exception = ", e);
			return null;
		}
	}

	@SuppressWarnings("static-access")
	public int searchPaymentCountForBookingRecord(String pgRefNum, String orderId, String custMobile, String custEmail,
			String merchantPayId, String subMerchantPayId, String paymentType, String Userstatus, String currency,
			String transactionType, String fromDate, String toDate, User user, String partSettleFlag,
			String subMerchantId, Set<String> orderIdSet) {

		logger.info("Inside TxnReports , searchPaymentCount");
		try {
			int total;
			List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();
			BasicDBObject dateIndexConditionQuery = new BasicDBObject();

			DateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.ENGLISH);
			Date dateStart = format.parse(fromDate);
			Date dateEnd = format.parse(toDate);

			LocalDate startDate = dateStart.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			LocalDate endDate = dateEnd.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

			String fromDateIndex = startDate.toString().replaceAll("-", "");
			String toDateIndex = endDate.toString().replaceAll("-", "");

			dateIndexConditionQuery.put("DATE_INDEX",
					BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(fromDateIndex).toLocalizedPattern())
							.add("$lte", new SimpleDateFormat(toDateIndex).toLocalizedPattern()).get());

			if (StringUtils.isNotBlank(merchantPayId) && !merchantPayId.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.PAY_ID.getName(), merchantPayId));
			}
			if (StringUtils.isNotBlank(subMerchantPayId) && !subMerchantPayId.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.SUB_MERCHANT_ID.getName(), subMerchantPayId));
			}

			if (user.getUserType().equals(UserType.RESELLER)) {
				paramConditionLst.add(new BasicDBObject(FieldType.RESELLER_ID.getName(), user.getResellerId()));
			}

			if (StringUtils.isNotBlank(subMerchantId)) {
				paramConditionLst.add(new BasicDBObject(FieldType.SUB_MERCHANT_ID.getName(), subMerchantId));
			}

			if (!pgRefNum.isEmpty()) {
				paramConditionLst.add(new BasicDBObject(FieldType.PG_REF_NUM.getName(), pgRefNum));
			}

			BasicDBObject orderIdConditionQuery = null;
			if (orderIdSet != null) {
				List<String> orderIdList = new ArrayList<>(orderIdSet);
				orderIdConditionQuery = new BasicDBObject("$in", orderIdList);
			}
			if (!orderId.isEmpty()) {
				paramConditionLst.add(new BasicDBObject(FieldType.ORDER_ID.getName(), orderId));
			} else if (null != orderIdConditionQuery) {
				paramConditionLst.add(new BasicDBObject(FieldType.ORDER_ID.getName(), orderIdConditionQuery));
			}
			if (StringUtils.isNotBlank(custEmail) && !custEmail.isEmpty()) {
				paramConditionLst.add(new BasicDBObject(FieldType.CUST_EMAIL.getName(), custEmail));
			}
			if (StringUtils.isNotBlank(Userstatus) && !Userstatus.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.STATUS.getName(), Userstatus));
			}

			if (StringUtils.isNotBlank(currency) && !currency.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.CURRENCY_CODE.getName(), currency));
			}
			if (StringUtils.isNotBlank(partSettleFlag) && !partSettleFlag.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.PART_SETTLE.getName(), partSettleFlag));
			}

			if (StringUtils.isNotBlank(transactionType) && !transactionType.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.ORIG_TXNTYPE.getName(), transactionType));
			}
			if (StringUtils.isNotBlank(paymentType) && !paymentType.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.PAYMENT_TYPE.getName(), paymentType));
			}

			if (StringUtils.isNotBlank(pgRefNum) || StringUtils.isNotBlank(orderId)) {

			} else {
				if (!dateIndexConditionQuery.isEmpty()) {
					paramConditionLst.add(dateIndexConditionQuery);
				}
			}

			logger.info("Inside TxnReports , searchPaymentCount , fianlList = " + paramConditionLst);
			BasicDBObject finalquery = new BasicDBObject("$and", paramConditionLst);
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns
					.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));

			total = (int) coll.count(finalquery);
			logger.info("Inside search txn report count , total records from DB  = " + total);
			return total;

		} catch (Exception e) {
			logger.error("Exception occured in TxnReports , searchPaymentCount n exception = ", e);
			return 0;
		}
	}

	public List<PaymentSearchDownloadObject> searchPaymentDownloadForBookingRecord(String merchantPayId,
			String subMerchantPayId, String pgRefNum, String orderId, String custMobile, String custEmail,
			String paymentType, String status, String currency, String transactionType, String fromDate, String toDate,
			String partSettleFlag, boolean isGlocal, User sessionUser, boolean dispatchSlipFlag,
			Set<String> orderIdSet) {
		logger.info("Inside TxnReports , searchPaymentDownloadForBookingRecord");
		Map<String, User> userMap = new HashMap<String, User>();

		List<PaymentSearchDownloadObject> transactionList = new ArrayList<PaymentSearchDownloadObject>();
		try {
			UserSettingData userSettings = userSettingDao.fetchDataUsingPayId(sessionUser.getPayId());
			List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();

			BasicDBObject dateIndexConditionQuery = new BasicDBObject();

			DateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.ENGLISH);
			Date dateStart = format.parse(fromDate);
			Date dateEnd = format.parse(toDate);

			LocalDate startDate = dateStart.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			LocalDate endDate = dateEnd.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

			String fromDateIndex = startDate.toString().replaceAll("-", "");
			String toDateIndex = endDate.toString().replaceAll("-", "");

			dateIndexConditionQuery.put("DATE_INDEX",
					BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(fromDateIndex).toLocalizedPattern())
							.add("$lte", new SimpleDateFormat(toDateIndex).toLocalizedPattern()).get());

			if (StringUtils.isNotBlank(merchantPayId) && !merchantPayId.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.PAY_ID.getName(), merchantPayId));
			}

			if (StringUtils.isNotBlank(subMerchantPayId) && !subMerchantPayId.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.SUB_MERCHANT_ID.getName(), subMerchantPayId));
			}

			if (!status.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.STATUS.getName(), status));
			}

			if (StringUtils.isNotBlank(currency) && !currency.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.CURRENCY_CODE.getName(), currency));
			}
			if (!partSettleFlag.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.PART_SETTLE.getName(), partSettleFlag));
			}

			if (!transactionType.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.ORIG_TXNTYPE.getName(), transactionType));
			}
			if (StringUtils.isNotBlank(paymentType) && !paymentType.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.PAYMENT_TYPE.getName(), paymentType));
			}

			if (!pgRefNum.isEmpty() && !pgRefNum.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.PG_REF_NUM.getName(), pgRefNum));
			}

			BasicDBObject orderIdConditionQuery = null;
			if (orderIdSet != null) {
				List<String> orderIdList = new ArrayList<>(orderIdSet);
				orderIdConditionQuery = new BasicDBObject("$in", orderIdList);
			}
			if (!orderId.isEmpty() && !orderId.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.ORDER_ID.getName(), orderId));
			} else if (null != orderIdConditionQuery) {
				paramConditionLst.add(new BasicDBObject(FieldType.ORDER_ID.getName(), orderIdConditionQuery));
			}

			if (StringUtils.isNotBlank(custMobile) && !custMobile.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.CUST_PHONE.getName(), custMobile));
			}

			if (StringUtils.isNotBlank(custEmail)) {
				paramConditionLst.add(new BasicDBObject(FieldType.CUST_EMAIL.getName(), custEmail));
			}

			// if (StringUtils.isNotBlank(custId) &&
			// !custId.equalsIgnoreCase("ALL")) {
			// paramConditionLst.add(new
			// BasicDBObject(FieldType.CUST_ID.getName(), custId));
			// }

			if (StringUtils.isNotBlank(pgRefNum) || StringUtils.isNotBlank(orderId)) {

			} else {
				if (!dateIndexConditionQuery.isEmpty()) {
					paramConditionLst.add(dateIndexConditionQuery);
				}
			}

			BasicDBObject finalquery = new BasicDBObject("$and", paramConditionLst);

			logger.info("Inside TxnReports , searchPayment , finalquery = " + finalquery);
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns
					.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));
			BasicDBObject match = new BasicDBObject("$match", finalquery);
			// Now the aggregate operation ()In case any parameter is passed in
			// search query
			// , then show all records

			Document firstGroup = new Document("_id", new Document("_id", "$_id"));

			BasicDBObject firstGroupObject = new BasicDBObject(firstGroup);
			BasicDBObject secondGroup = new BasicDBObject("$push", "$$ROOT");
			BasicDBObject group = new BasicDBObject("$group", firstGroupObject.append("entries", secondGroup));
			BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject("CREATE_DATE", -1));

			List<BasicDBObject> pipeline = Arrays.asList(match, sort, group);

			AggregateIterable<Document> output = coll.aggregate(pipeline);
			output.allowDiskUse(true);
			MongoCursor<Document> cursor = output.iterator();
			int count = 0;
			while (cursor.hasNext()) {

				Document mydata = cursor.next();
				List<Document> courses = (List<Document>) mydata.get("entries");
				Document dbobj = courses.get(0);
				if (StringUtils.isNotBlank(dbobj.getString("IS_ENCRYPTED"))
						&& dbobj.getString("IS_ENCRYPTED").equalsIgnoreCase("Y")) {
					dbobj = dataEncDecTool.decryptDocument(dbobj);
				}
				PaymentSearchDownloadObject transReport = new PaymentSearchDownloadObject();
				transReport.setTransactionId(dbobj.getString(FieldType.TXN_ID.toString()));
				transReport.setPgRefNum(dbobj.getString(FieldType.PG_REF_NUM.toString()));
				if (StringUtils.isNotBlank(dbobj.getString(FieldType.PAYMENTS_REGION.getName()))) {
					transReport.setTransactionRegion(dbobj.getString(FieldType.PAYMENTS_REGION.getName()));
				} else {
					transReport.setTransactionRegion(CrmFieldConstants.NA.getValue());
				}
				transReport.setMerchants(dbobj.getString(CrmFieldType.BUSINESS_NAME.getName()));

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.TXN_CAPTURE_FLAG.getName()))) {
					transReport.setTxnSettledType(dbobj.getString(FieldType.TXN_CAPTURE_FLAG.getName()));
				} else {
					transReport.setTxnSettledType(CrmFieldConstants.NA.getValue());
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.TRANSACTION_MODE.getName()))) {
					transReport.setTransactionMode(dbobj.getString(FieldType.TRANSACTION_MODE.getName()));
				} else {
				}

				// if
				// (StringUtils.isNotBlank(dbobj.getString(FieldType.POST_SETTLED_FLAG.getName())))
				// {
				// transReport.setPostSettledFlag(dbobj.getString(FieldType.POST_SETTLED_FLAG.getName()));
				// } else {
				// transReport.setPostSettledFlag(CrmFieldConstants.NA.getValue());
				// }
				if (StringUtils.isNotBlank(dbobj.getString(FieldType.PART_SETTLE.toString()))
						&& dbobj.getString(FieldType.PART_SETTLE.toString()).equalsIgnoreCase(("Y"))) {
					transReport.setPartSettle(dbobj.getString(FieldType.PART_SETTLE.toString()));
				} else {
					transReport.setPartSettle(CrmFieldConstants.NA.getValue());
				}

				// transReport.setCardMask(dbobj.getString(FieldType.CARD_MASK.getName()));
				if (null != dbobj.getString(FieldType.PAYMENT_TYPE.toString())) {
					if (null != dbobj.getString(FieldType.CARD_MASK.toString())) {
						transReport.setCardMask(dbobj.getString(FieldType.CARD_MASK.toString()));
					} else if (null != dbobj.getString(FieldType.PAYER_ADDRESS.getName())) {

						if (sessionUser.getUserType().equals(UserType.ADMIN)
								|| sessionUser.getUserType().equals(UserType.SUBADMIN)) {
							transReport.setCardMask(dbobj.getString(FieldType.PAYER_ADDRESS.getName()));
						} else {
							String vpaString = dbobj.getString(FieldType.PAYER_ADDRESS.getName());
							String[] vpaArray = vpaString.split("@");
							char[] vpaChar = vpaArray[0].toCharArray();
							StringBuilder vpastrBuilder = new StringBuilder();

							if (vpaChar.length > 3) {
								for (int i = 0; i < vpaChar.length - 3; i++) {
									vpastrBuilder.append(vpaChar[i]);
								}
								vpastrBuilder.append("***@");
								vpastrBuilder.append(vpaArray[1]);
							} else {
								vpastrBuilder.append(vpaChar[0]);
								vpastrBuilder.append("**@");
								vpastrBuilder.append(vpaArray[1]);
							}

							transReport.setCardMask(vpastrBuilder.toString());
						}
					} else {
						transReport.setCardMask(CrmFieldConstants.NA.getValue());
					}
				} else {
					transReport.setCardMask(CrmFieldConstants.NA.getValue());
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.CUST_NAME.getName()))) {
					transReport.setCustName(dbobj.getString(FieldType.CUST_NAME.getName()));
				} else {
					transReport.setCustName("NA");
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.CUST_PHONE.getName()))) {
					transReport.setCustMobile(dbobj.getString(FieldType.CUST_PHONE.getName()));
				} else {
					transReport.setCustMobile("NA");
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.CUST_EMAIL.getName()))) {
					transReport.setCustEmail(dbobj.getString(FieldType.CUST_EMAIL.getName()));
				} else {
					transReport.setCustEmail("NA");
				}

				User user1 = new User();

				if (StringUtils.isNotBlank((String) dbobj.get(FieldType.PAY_ID.getName()))) {
					String payid = (String) dbobj.get(FieldType.PAY_ID.getName());

					if (userMap.get(payid) != null && !userMap.get(payid).getPayId().isEmpty()) {
						user1 = userMap.get(payid);
					} else {
						user1 = userdao.findPayId(payid);
						userMap.put(payid, user1);
					}
				}

				// String payid = (String)
				// dbobj.get(FieldType.PAY_ID.getName());
				//
				// User user1 = new User();
				//
				// if (userMap.get(payid) != null &&
				// !userMap.get(payid).getPayId().isEmpty()) {
				// user1 = userMap.get(payid);
				// } else {
				// user1 = userdao.findPayId(payid);
				// userMap.put(payid, user1);
				// }
				if (user1 != null) {
					transReport.setMerchants(user1.getBusinessName());
				}
				transReport.setUser(user1);
				if (merchantPayId.equalsIgnoreCase("All")) {

					if (StringUtils.isNotBlank(dbobj.getString(FieldType.REFUND_DAYS.getName()))) {
						transReport.setRefundCycle(dbobj.getString(FieldType.REFUND_DAYS.getName()));
					} else {
						transReport.setRefundCycle(CrmFieldConstants.NA.getValue());
					}

					if (StringUtils.isNotBlank(dbobj.getString(FieldType.PRODUCT_PRICE.getName()))) {
						transReport.setProductPrice(dbobj.getString(FieldType.PRODUCT_PRICE.getName()));
					} else {
						transReport.setProductPrice(CrmFieldConstants.NA.getValue());
					}
					if (StringUtils.isNotBlank(dbobj.getString(FieldType.VENDOR_ID.getName()))) {
						transReport.setVendorID(dbobj.getString(FieldType.VENDOR_ID.getName()));
					} else {
						transReport.setVendorID(CrmFieldConstants.NA.getValue());
					}
					if (StringUtils.isNotBlank(dbobj.getString(FieldType.SKU_CODE.getName()))) {
						transReport.setSKUCode(dbobj.getString(FieldType.SKU_CODE.getName()));
					} else {
						transReport.setSKUCode(CrmFieldConstants.NA.getValue());
					}

					if (StringUtils.isNotBlank(dbobj.getString(FieldType.CATEGORY_CODE.getName()))) {
						transReport.setCategoryCode(dbobj.getString(FieldType.CATEGORY_CODE.getName()));
					} else {
						transReport.setCategoryCode(CrmFieldConstants.NA.getValue());
					}
				} else {

					if (sessionUser.getUserType().equals(UserType.ADMIN)
							|| sessionUser.getUserType().equals(UserType.SUBADMIN)
							|| sessionUser.getUserType().equals(UserType.RESELLER)) {
						User merhant = userMap.get(merchantPayId); // userdao.findPayId(payid);

						if (userSettings.isRetailMerchantFlag()) {
							if (StringUtils.isNotBlank(dbobj.getString(FieldType.REFUND_DAYS.getName()))) {
								transReport.setRefundCycle(dbobj.getString(FieldType.REFUND_DAYS.getName()));
							} else {
								transReport.setRefundCycle(CrmFieldConstants.NA.getValue());
							}

							if (StringUtils.isNotBlank(dbobj.getString(FieldType.PRODUCT_PRICE.getName()))) {
								transReport.setProductPrice(dbobj.getString(FieldType.PRODUCT_PRICE.getName()));
							} else {
								transReport.setProductPrice(CrmFieldConstants.NA.getValue());
							}
							if (StringUtils.isNotBlank(dbobj.getString(FieldType.VENDOR_ID.getName()))) {
								transReport.setVendorID(dbobj.getString(FieldType.VENDOR_ID.getName()));
							} else {
								transReport.setVendorID(CrmFieldConstants.NA.getValue());
							}
							if (StringUtils.isNotBlank(dbobj.getString(FieldType.SKU_CODE.getName()))) {
								transReport.setSKUCode(dbobj.getString(FieldType.SKU_CODE.getName()));
							} else {
								transReport.setSKUCode(CrmFieldConstants.NA.getValue());
							}

							if (StringUtils.isNotBlank(dbobj.getString(FieldType.CATEGORY_CODE.getName()))) {
								transReport.setCategoryCode(dbobj.getString(FieldType.CATEGORY_CODE.getName()));
							} else {
								transReport.setCategoryCode(CrmFieldConstants.NA.getValue());
							}
						}
					} else {

						if (userSettings.isRetailMerchantFlag()) {
							if (StringUtils.isNotBlank(dbobj.getString(FieldType.REFUND_DAYS.getName()))) {
								transReport.setRefundCycle(dbobj.getString(FieldType.REFUND_DAYS.getName()));
							} else {
								transReport.setRefundCycle(CrmFieldConstants.NA.getValue());
							}

							if (StringUtils.isNotBlank(dbobj.getString(FieldType.PRODUCT_PRICE.getName()))) {
								transReport.setProductPrice(dbobj.getString(FieldType.PRODUCT_PRICE.getName()));
							} else {
								transReport.setProductPrice(CrmFieldConstants.NA.getValue());
							}
							if (StringUtils.isNotBlank(dbobj.getString(FieldType.VENDOR_ID.getName()))) {
								transReport.setVendorID(dbobj.getString(FieldType.VENDOR_ID.getName()));
							} else {
								transReport.setVendorID(CrmFieldConstants.NA.getValue());
							}
							if (StringUtils.isNotBlank(dbobj.getString(FieldType.SKU_CODE.getName()))) {
								transReport.setSKUCode(dbobj.getString(FieldType.SKU_CODE.getName()));
							} else {
								transReport.setSKUCode(CrmFieldConstants.NA.getValue());
							}

							if (StringUtils.isNotBlank(dbobj.getString(FieldType.CATEGORY_CODE.getName()))) {
								transReport.setCategoryCode(dbobj.getString(FieldType.CATEGORY_CODE.getName()));
							} else {
								transReport.setCategoryCode(CrmFieldConstants.NA.getValue());
							}
						}

					}

				}

				/*
				 * if (dbobj.containsKey("CUSTOM_FLAG") &&
				 * StringUtils.isNotBlank(dbobj.getString("CUSTOM_FLAG"))) {
				 * transReport.setCustomFlag(((Document) dbobj).getString("CUSTOM_FLAG")); }
				 * else { transReport.setCustomFlag("N"); }
				 */

				// if (!merchantPayId.equalsIgnoreCase("All") &&
				if (dbobj.containsKey(FieldType.SUB_MERCHANT_ID.getName())
						&& StringUtils.isNotBlank(dbobj.getString(FieldType.SUB_MERCHANT_ID.getName()))) {

					String subMerchant = dbobj.getString(FieldType.SUB_MERCHANT_ID.getName());
					User subMerchantUser = new User();

					if (userMap.get(subMerchant) != null && !userMap.get(subMerchant).getPayId().isEmpty()) {
						subMerchantUser = userMap.get(subMerchant);
					} else {
						subMerchantUser = userdao.findPayId(subMerchant);
						userMap.put(subMerchant, subMerchantUser);
					}
					if (subMerchantUser != null) {
						transReport.setSubMerchantId(subMerchantUser.getBusinessName());
					} else {
						transReport.setSubMerchantId(CrmFieldConstants.NA.getValue());
					}
				} else {
					if (((!pgRefNum.isEmpty() && !pgRefNum.equalsIgnoreCase("All"))
							|| (!orderId.isEmpty() && !orderId.equalsIgnoreCase("All")))
							&& dbobj.containsKey(FieldType.SUB_MERCHANT_ID.getName())
							&& StringUtils.isNotBlank(dbobj.getString(FieldType.SUB_MERCHANT_ID.getName()))) {
						transReport.setSubMerchantId(
								userdao.getBusinessNameByPayId(dbobj.getString(FieldType.SUB_MERCHANT_ID.getName())));
					}
				}
				transReport.setInvoiceNo(CrmFieldConstants.NA.getValue());
				transReport.setCourierServiceProvider(CrmFieldConstants.NA.getValue());
				transReport.setDispatchSlipNo(CrmFieldConstants.NA.getValue());

				if (null != dbobj.getString(FieldType.ORIG_TXNTYPE.toString())) {
					transReport.setTxnType(dbobj.getString(FieldType.ORIG_TXNTYPE.toString()));
				} else {
					// If ORIG_TXN_TYPE is not available incase of a timeout ,
					// set TXNTYPE instead
					// of ORIG_TXN_TYPE
					if (dbobj.getString(FieldType.STATUS.toString()).equalsIgnoreCase(StatusType.TIMEOUT.getName())) {
						transReport.setTxnType(dbobj.getString(FieldType.TXNTYPE.toString()));
					} else {
						transReport.setTxnType(CrmFieldConstants.NA.getValue());
					}

				}
				if (null != dbobj.getString(FieldType.ACQUIRER_TYPE.toString())) {
					transReport.setAcquirerType(dbobj.getString(FieldType.ACQUIRER_TYPE.toString()));
				} else {
					transReport.setAcquirerType(CrmFieldConstants.NA.getValue());
				}

				transReport.setAcquirerMode(dbobj.getString(FieldType.ACQUIRER_MODE.getName()));
				if (null != dbobj.getString(FieldType.PAYMENT_TYPE.toString())) {
					transReport.setPaymentMethods(
							PaymentType.getpaymentName(dbobj.getString(FieldType.PAYMENT_TYPE.toString())));
				} else {
					transReport.setPaymentMethods(CrmFieldConstants.NA.getValue());
				}
				transReport.setStatus(dbobj.getString(FieldType.STATUS.toString()));
				if (StatusType.CAPTURED.getName().equalsIgnoreCase(dbobj.getString(FieldType.STATUS.getName()))) {
					transReport.setDateFrom(dbobj.getString(FieldType.CREATE_DATE.getName()));
				} else {
					transReport.setSettledDate(dbobj.getString(FieldType.CREATE_DATE.getName()));
					transReport.setDateFrom(dbobj.getString(FieldType.PG_DATE_TIME.getName()));
				}

				transReport.setAmount(dbobj.getString(FieldType.AMOUNT.toString()));
				transReport.setOrderId(dbobj.getString(FieldType.ORDER_ID.toString()));
				if (dbobj.getString(FieldType.TOTAL_AMOUNT.toString()) != null) {
					transReport.setTotalAmount(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()));
				} else {
					transReport.setTotalAmount("");
				}
				if (dbobj.containsKey(FieldType.RESELLER_CHARGES.getName())
						&& StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_CHARGES.getName()))
						&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.getName()))
						&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.getName()))) {
					transReport.setTdrOrSurcharge(
							String.valueOf(new BigDecimal(dbobj.getString(FieldType.PG_TDR_SC.getName()))
									.add(new BigDecimal(dbobj.getString(FieldType.RESELLER_CHARGES.getName())))
									.add(new BigDecimal(dbobj.getString(FieldType.ACQUIRER_TDR_SC.getName())))));
				} else {

					if (StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.getName()))
							&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.getName()))) {

						transReport.setTdrOrSurcharge(
								String.valueOf(new BigDecimal(dbobj.getString(FieldType.PG_TDR_SC.getName()))
										.add(new BigDecimal(dbobj.getString(FieldType.ACQUIRER_TDR_SC.getName())))));

					} else {
						transReport.setTdrOrSurcharge(CrmFieldConstants.NA.getValue());
					}
				}
				if (dbobj.containsKey(FieldType.RESELLER_GST.getName())
						&& StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_GST.getName()))
						&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.getName()))
						&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.getName()))) {

					transReport.setGst(String.valueOf(new BigDecimal(dbobj.getString(FieldType.PG_GST.getName()))
							.add(new BigDecimal(dbobj.getString(FieldType.RESELLER_GST.getName())))
							.add(new BigDecimal(dbobj.getString(FieldType.ACQUIRER_GST.getName())))));

				} else {

					if (StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.getName()))
							&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.getName()))) {

						transReport.setGst(String.valueOf(new BigDecimal(dbobj.getString(FieldType.PG_GST.getName()))
								.add(new BigDecimal(dbobj.getString(FieldType.ACQUIRER_GST.getName())))));

					} else {

						transReport.setGst(CrmFieldConstants.NA.getValue());
					}
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.PAYMENT_TYPE.toString()))
						&& dbobj.getString(FieldType.PAYMENT_TYPE.getName()).equalsIgnoreCase(PaymentType.COD.getCode())
						&& StringUtils.isNotBlank(dbobj.getString(FieldType.ORIG_TXNTYPE.toString()))
						&& dbobj.getString(FieldType.ORIG_TXNTYPE.getName()).equalsIgnoreCase(TxnType.SALE.getName())) {

					if (isGlocal == true) {
						if (dbobj.containsKey(FieldType.UDF7.getName())) {
							if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF7.getName()))) {

								String merchantAmount = null;
								if (dbobj.containsKey(FieldType.RESELLER_CHARGES.getName())
										&& dbobj.containsKey(FieldType.RESELLER_GST.getName())
										&& StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_CHARGES.getName()))
										&& StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_GST.getName()))
										&& StringUtils.isNotBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
										&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.toString()))
										&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
										&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.toString()))
										&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.toString()))) {

									merchantAmount = String.format("%.2f", Double
											.parseDouble(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
											- (Double.parseDouble(dbobj.getString(FieldType.PG_TDR_SC.toString()))
													+ Double.parseDouble(
															dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
													+ Double.parseDouble(dbobj.getString(FieldType.PG_GST.toString()))
													+ Double.parseDouble(
															dbobj.getString(FieldType.ACQUIRER_GST.toString()))
													+ Double.parseDouble(
															dbobj.getString(FieldType.RESELLER_CHARGES.getName()))
													+ Double.parseDouble(
															dbobj.getString(FieldType.RESELLER_GST.getName()))));

									transReport.setDoctor(
											String.valueOf(new BigDecimal(dbobj.getString(FieldType.UDF7.getName()))
													.multiply(new BigDecimal(merchantAmount))
													.divide(new BigDecimal(Constants.MAX_NUMBER_OF_KEYS.getValue()))
													.setScale(2, BigDecimal.ROUND_HALF_UP)));

								} else {

									if (StringUtils.isNotBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
											&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.toString()))
											&& StringUtils
													.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
											&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.toString()))
											&& StringUtils
													.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.toString()))) {

										merchantAmount = String.format("%.2f", Double
												.parseDouble(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
												- (Double.parseDouble(dbobj.getString(FieldType.PG_TDR_SC.toString()))
														+ Double.parseDouble(
																dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
														+ Double.parseDouble(
																dbobj.getString(FieldType.PG_GST.toString()))
														+ Double.parseDouble(
																dbobj.getString(FieldType.ACQUIRER_GST.toString()))));

										transReport.setDoctor(
												String.valueOf(new BigDecimal(dbobj.getString(FieldType.UDF7.getName()))
														.multiply(new BigDecimal(merchantAmount))
														.divide(new BigDecimal(Constants.MAX_NUMBER_OF_KEYS.getValue()))
														.setScale(2, BigDecimal.ROUND_HALF_UP)));
									} else {
										transReport.setDoctor("0");
									}
								}
							} else {
								transReport.setDoctor("0");
							}
						} else {
							transReport.setDoctor("0");
						}

						if (dbobj.containsKey(FieldType.UDF8.getName())) {
							if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF8.getName()))) {

								String merchantAmount = null;
								if (dbobj.containsKey(FieldType.RESELLER_CHARGES.getName())
										&& dbobj.containsKey(FieldType.RESELLER_GST.getName())
										&& StringUtils
												.isNotBlank(dbobj.getString(FieldType.RESELLER_CHARGES.toString()))
										&& StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_GST.toString()))
										&& StringUtils.isNotBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
										&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.toString()))
										&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
										&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.toString()))
										&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.toString()))) {

									merchantAmount = String.format("%.2f", Double
											.parseDouble(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
											- (Double.parseDouble(dbobj.getString(FieldType.PG_TDR_SC.toString()))
													+ Double.parseDouble(
															dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
													+ Double.parseDouble(dbobj.getString(FieldType.PG_GST.toString()))
													+ Double.parseDouble(
															dbobj.getString(FieldType.ACQUIRER_GST.toString()))
													+ Double.parseDouble(
															dbobj.getString(FieldType.RESELLER_CHARGES.getName()))
													+ Double.parseDouble(
															dbobj.getString(FieldType.RESELLER_GST.getName()))));

									transReport.setGlocal(
											String.valueOf(new BigDecimal(dbobj.getString(FieldType.UDF8.getName()))
													.multiply(new BigDecimal(merchantAmount))
													.divide(new BigDecimal(Constants.MAX_NUMBER_OF_KEYS.getValue()))
													.setScale(2, BigDecimal.ROUND_HALF_UP)));

								} else {

									if (StringUtils.isNotBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
											&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.toString()))
											&& StringUtils
													.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
											&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.toString()))
											&& StringUtils
													.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.toString()))) {

										merchantAmount = String.format("%.2f", Double
												.parseDouble(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
												- (Double.parseDouble(dbobj.getString(FieldType.PG_TDR_SC.toString()))
														+ Double.parseDouble(
																dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
														+ Double.parseDouble(
																dbobj.getString(FieldType.PG_GST.toString()))
														+ Double.parseDouble(
																dbobj.getString(FieldType.ACQUIRER_GST.toString()))));

										transReport.setGlocal(
												String.valueOf(new BigDecimal(dbobj.getString(FieldType.UDF8.getName()))
														.multiply(new BigDecimal(merchantAmount))
														.divide(new BigDecimal(Constants.MAX_NUMBER_OF_KEYS.getValue()))
														.setScale(2, BigDecimal.ROUND_HALF_UP)));
									} else {
										transReport.setGlocal("0");
									}
								}
							} else {
								transReport.setGlocal("0");
							}
						} else {
							transReport.setGlocal("0");
						}

						if (dbobj.containsKey(FieldType.UDF9.getName())) {
							if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF9.getName()))) {

								String merchantAmount = null;

								if (dbobj.containsKey(FieldType.RESELLER_CHARGES.getName())
										&& dbobj.containsKey(FieldType.RESELLER_GST.getName())
										&& StringUtils
												.isNotBlank(dbobj.getString(FieldType.RESELLER_CHARGES.toString()))
										&& StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_GST.toString()))
										&& StringUtils.isNotBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
										&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.toString()))
										&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
										&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.toString()))
										&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.toString()))) {

									merchantAmount = String.format("%.2f", Double
											.parseDouble(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
											- (Double.parseDouble(dbobj.getString(FieldType.PG_TDR_SC.toString()))
													+ Double.parseDouble(
															dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
													+ Double.parseDouble(dbobj.getString(FieldType.PG_GST.toString()))
													+ Double.parseDouble(
															dbobj.getString(FieldType.ACQUIRER_GST.toString()))
													+ Double.parseDouble(
															dbobj.getString(FieldType.RESELLER_CHARGES.getName()))
													+ Double.parseDouble(
															dbobj.getString(FieldType.RESELLER_GST.getName()))));

									transReport.setPartner(
											String.valueOf(new BigDecimal(dbobj.getString(FieldType.UDF9.getName()))
													.multiply(new BigDecimal(merchantAmount))
													.divide(new BigDecimal(Constants.MAX_NUMBER_OF_KEYS.getValue()))
													.setScale(2, BigDecimal.ROUND_HALF_UP)));

								} else {

									if (StringUtils.isNotBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
											&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.toString()))
											&& StringUtils
													.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
											&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.toString()))
											&& StringUtils
													.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.toString()))) {

										merchantAmount = String.format("%.2f", Double
												.parseDouble(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
												- (Double.parseDouble(dbobj.getString(FieldType.PG_TDR_SC.toString()))
														+ Double.parseDouble(
																dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
														+ Double.parseDouble(
																dbobj.getString(FieldType.PG_GST.toString()))
														+ Double.parseDouble(
																dbobj.getString(FieldType.ACQUIRER_GST.toString()))));

										transReport.setPartner(
												String.valueOf(new BigDecimal(dbobj.getString(FieldType.UDF9.getName()))
														.multiply(new BigDecimal(merchantAmount))
														.divide(new BigDecimal(Constants.MAX_NUMBER_OF_KEYS.getValue()))
														.setScale(2, BigDecimal.ROUND_HALF_UP)));
									} else {
										transReport.setPartner("0");
									}
								}
							} else {
								transReport.setPartner("0");
							}
						} else {
							transReport.setPartner("0");
						}

						if (dbobj.containsKey(FieldType.UDF11.getName())) {
							if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF11.getName()))) {
								transReport.setUniqueId(dbobj.getString(FieldType.UDF11.getName()));
							} else {
								transReport.setUniqueId("NA");
							}
						} else {
							transReport.setUniqueId("NA");
						}
					}

					if (dbobj.containsKey(FieldType.RESELLER_CHARGES.getName())
							&& dbobj.containsKey(FieldType.RESELLER_GST.getName())
							&& StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_CHARGES.getName()))
							&& StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_GST.getName()))
							&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.getName()))
							&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.getName()))
							&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.getName()))
							&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.getName()))) {

						transReport.setMerchantAmount(
								"-" + String.valueOf(new BigDecimal(dbobj.getString(FieldType.PG_GST.getName()))
										.add(new BigDecimal(dbobj.getString(FieldType.PG_TDR_SC.getName())))
										.add(new BigDecimal(dbobj.getString(FieldType.ACQUIRER_TDR_SC.getName())))
										.add(new BigDecimal(dbobj.getString(FieldType.ACQUIRER_GST.getName())))
										.add(new BigDecimal(dbobj.getString(FieldType.RESELLER_CHARGES.getName())))
										.add(new BigDecimal(dbobj.getString(FieldType.RESELLER_GST.getName())))));

					} else {

						if (StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.getName()))
								&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.getName()))
								&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.getName()))
								&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.getName()))) {

							transReport.setMerchantAmount(
									"-" + String.valueOf(new BigDecimal(dbobj.getString(FieldType.PG_GST.getName()))
											.add(new BigDecimal(dbobj.getString(FieldType.PG_TDR_SC.getName())))
											.add(new BigDecimal(dbobj.getString(FieldType.ACQUIRER_TDR_SC.getName())))
											.add(new BigDecimal(dbobj.getString(FieldType.ACQUIRER_GST.getName())))));
						} else {
							transReport.setMerchantAmount(CrmFieldConstants.NA.getValue());
						}
					}

				} else if (StringUtils.isNotBlank(dbobj.getString(FieldType.PAYMENT_TYPE.toString()))
						&& dbobj.getString(FieldType.PAYMENT_TYPE.getName()).equalsIgnoreCase(PaymentType.COD.getCode())
						&& dbobj.getString(FieldType.ORIG_TXNTYPE.getName())
								.equalsIgnoreCase(TxnType.REFUND.getName())) {
					transReport.setMerchantAmount("0.00");
					transReport.setTdrOrSurcharge("0.00");
					transReport.setGst("0.00");
				} else {
					if (dbobj.getString(FieldType.ORIG_TXNTYPE.getName()).equalsIgnoreCase(TxnType.REFUND.getName())) {

						if (dbobj.containsKey(FieldType.RESELLER_CHARGES.getName())
								&& dbobj.containsKey(FieldType.RESELLER_GST.getName())
								&& StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_CHARGES.getName()))
								&& StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_GST.getName()))
								&& StringUtils.isNotBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.getName()))
								&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.getName()))
								&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.getName()))
								&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.getName()))
								&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.getName()))) {

							transReport.setMerchantAmount("-" + String
									.valueOf(new BigDecimal(dbobj.getString(FieldType.TOTAL_AMOUNT.getName())).subtract(
											new BigDecimal(dbobj.getString(FieldType.ACQUIRER_TDR_SC.getName()))
													.add(new BigDecimal(
															dbobj.getString(FieldType.ACQUIRER_GST.getName())))
													.add(new BigDecimal(dbobj.getString(FieldType.PG_TDR_SC.getName())))
													.add(new BigDecimal(dbobj.getString(FieldType.PG_GST.getName())))
													.add(new BigDecimal(
															dbobj.getString(FieldType.RESELLER_CHARGES.getName())))
													.add(new BigDecimal(
															dbobj.getString(FieldType.RESELLER_GST.getName()))))));

						} else {

							transReport.setMerchantAmount("-" + String
									.valueOf(new BigDecimal(dbobj.getString(FieldType.TOTAL_AMOUNT.getName())).subtract(
											new BigDecimal(dbobj.getString(FieldType.ACQUIRER_TDR_SC.getName()))
													.add(new BigDecimal(
															dbobj.getString(FieldType.ACQUIRER_GST.getName())))
													.add(new BigDecimal(dbobj.getString(FieldType.PG_TDR_SC.getName()))
															.add(new BigDecimal(
																	dbobj.getString(FieldType.PG_GST.getName())))))));
						}

					} else {

						if (isGlocal == true) {
							if (dbobj.containsKey(FieldType.UDF7.getName())) {
								if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF7.getName()))) {

									String merchantAmount = null;
									if (dbobj.containsKey(FieldType.RESELLER_CHARGES.getName())
											&& dbobj.containsKey(FieldType.RESELLER_GST.getName())
											&& StringUtils
													.isNotBlank(dbobj.getString(FieldType.RESELLER_CHARGES.getName()))
											&& StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_GST.getName()))
											&& StringUtils
													.isNotBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
											&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.toString()))
											&& StringUtils
													.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
											&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.toString()))
											&& StringUtils
													.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.toString()))) {

										merchantAmount = String.format("%.2f", Double
												.parseDouble(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
												- (Double.parseDouble(dbobj.getString(FieldType.PG_TDR_SC.toString()))
														+ Double.parseDouble(
																dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
														+ Double.parseDouble(
																dbobj.getString(FieldType.PG_GST.toString()))
														+ Double.parseDouble(
																dbobj.getString(FieldType.ACQUIRER_GST.toString()))
														+ Double.parseDouble(
																dbobj.getString(FieldType.RESELLER_CHARGES.getName()))
														+ Double.parseDouble(
																dbobj.getString(FieldType.RESELLER_GST.getName()))));

										transReport.setDoctor(
												String.valueOf(new BigDecimal(dbobj.getString(FieldType.UDF7.getName()))
														.multiply(new BigDecimal(merchantAmount))
														.divide(new BigDecimal(Constants.MAX_NUMBER_OF_KEYS.getValue()))
														.setScale(2, BigDecimal.ROUND_HALF_UP)));

									} else {

										if (StringUtils.isNotBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
												&& StringUtils
														.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.toString()))
												&& StringUtils.isNotBlank(
														dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
												&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.toString()))
												&& StringUtils.isNotBlank(
														dbobj.getString(FieldType.ACQUIRER_GST.toString()))) {

											merchantAmount = String.format("%.2f", Double
													.parseDouble(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
													- (Double.parseDouble(
															dbobj.getString(FieldType.PG_TDR_SC.toString()))
															+ Double.parseDouble(dbobj
																	.getString(FieldType.ACQUIRER_TDR_SC.toString()))
															+ Double.parseDouble(
																	dbobj.getString(FieldType.PG_GST.toString()))
															+ Double.parseDouble(dbobj
																	.getString(FieldType.ACQUIRER_GST.toString()))));

											transReport.setDoctor(String
													.valueOf(new BigDecimal(dbobj.getString(FieldType.UDF7.getName()))
															.multiply(new BigDecimal(merchantAmount))
															.divide(new BigDecimal(
																	Constants.MAX_NUMBER_OF_KEYS.getValue()))
															.setScale(2, BigDecimal.ROUND_HALF_UP)));
										} else {
											transReport.setDoctor("0");
										}
									}
								} else {
									transReport.setDoctor("0");
								}
							} else {
								transReport.setDoctor("0");
							}

							if (dbobj.containsKey(FieldType.UDF8.getName())) {
								if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF8.getName()))) {

									String merchantAmount = null;

									if (dbobj.containsKey(FieldType.RESELLER_CHARGES.getName())
											&& dbobj.containsKey(FieldType.RESELLER_GST.getName())
											&& StringUtils
													.isNotBlank(dbobj.getString(FieldType.RESELLER_CHARGES.getName()))
											&& StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_GST.getName()))
											&& StringUtils
													.isNotBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
											&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.toString()))
											&& StringUtils
													.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
											&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.toString()))
											&& StringUtils
													.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.toString()))) {

										merchantAmount = String.format("%.2f", Double
												.parseDouble(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
												- (Double.parseDouble(dbobj.getString(FieldType.PG_TDR_SC.toString()))
														+ Double.parseDouble(
																dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
														+ Double.parseDouble(
																dbobj.getString(FieldType.PG_GST.toString()))
														+ Double.parseDouble(
																dbobj.getString(FieldType.ACQUIRER_GST.toString()))
														+ Double.parseDouble(
																dbobj.getString(FieldType.RESELLER_CHARGES.getName()))
														+ Double.parseDouble(
																dbobj.getString(FieldType.RESELLER_GST.getName()))));

										transReport.setGlocal(
												String.valueOf(new BigDecimal(dbobj.getString(FieldType.UDF8.getName()))
														.multiply(new BigDecimal(merchantAmount))
														.divide(new BigDecimal(Constants.MAX_NUMBER_OF_KEYS.getValue()))
														.setScale(2, BigDecimal.ROUND_HALF_UP)));

									} else {

										if (StringUtils.isNotBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
												&& StringUtils
														.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.toString()))
												&& StringUtils.isNotBlank(
														dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
												&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.toString()))
												&& StringUtils.isNotBlank(
														dbobj.getString(FieldType.ACQUIRER_GST.toString()))) {

											merchantAmount = String.format("%.2f", Double
													.parseDouble(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
													- (Double.parseDouble(
															dbobj.getString(FieldType.PG_TDR_SC.toString()))
															+ Double.parseDouble(dbobj
																	.getString(FieldType.ACQUIRER_TDR_SC.toString()))
															+ Double.parseDouble(
																	dbobj.getString(FieldType.PG_GST.toString()))
															+ Double.parseDouble(dbobj
																	.getString(FieldType.ACQUIRER_GST.toString()))));

											transReport.setGlocal(String
													.valueOf(new BigDecimal(dbobj.getString(FieldType.UDF8.getName()))
															.multiply(new BigDecimal(merchantAmount))
															.divide(new BigDecimal(
																	Constants.MAX_NUMBER_OF_KEYS.getValue()))
															.setScale(2, BigDecimal.ROUND_HALF_UP)));
										} else {
											transReport.setGlocal("0");
										}
									}
								} else {
									transReport.setGlocal("0");
								}
							} else {
								transReport.setGlocal("0");
							}

							if (dbobj.containsKey(FieldType.UDF9.getName())) {
								if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF9.getName()))) {

									String merchantAmount = null;

									if (dbobj.containsKey(FieldType.RESELLER_CHARGES.getName())
											&& dbobj.containsKey(FieldType.RESELLER_GST.getName())
											&& StringUtils
													.isNotBlank(dbobj.getString(FieldType.RESELLER_CHARGES.getName()))
											&& StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_GST.getName()))
											&& StringUtils
													.isNotBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
											&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.toString()))
											&& StringUtils
													.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
											&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.toString()))
											&& StringUtils
													.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.toString()))) {

										merchantAmount = String.format("%.2f", Double
												.parseDouble(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
												- (Double.parseDouble(dbobj.getString(FieldType.PG_TDR_SC.toString()))
														+ Double.parseDouble(
																dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
														+ Double.parseDouble(
																dbobj.getString(FieldType.PG_GST.toString()))
														+ Double.parseDouble(
																dbobj.getString(FieldType.ACQUIRER_GST.toString()))
														+ Double.parseDouble(
																dbobj.getString(FieldType.RESELLER_CHARGES.getName()))
														+ Double.parseDouble(
																dbobj.getString(FieldType.RESELLER_GST.getName()))));

										transReport.setPartner(
												String.valueOf(new BigDecimal(dbobj.getString(FieldType.UDF9.getName()))
														.multiply(new BigDecimal(merchantAmount))
														.divide(new BigDecimal(Constants.MAX_NUMBER_OF_KEYS.getValue()))
														.setScale(2, BigDecimal.ROUND_HALF_UP)));

									} else {

										if (StringUtils.isNotBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
												&& StringUtils
														.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.toString()))
												&& StringUtils.isNotBlank(
														dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
												&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.toString()))
												&& StringUtils.isNotBlank(
														dbobj.getString(FieldType.ACQUIRER_GST.toString()))) {

											merchantAmount = String.format("%.2f", Double
													.parseDouble(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
													- (Double.parseDouble(
															dbobj.getString(FieldType.PG_TDR_SC.toString()))
															+ Double.parseDouble(dbobj
																	.getString(FieldType.ACQUIRER_TDR_SC.toString()))
															+ Double.parseDouble(
																	dbobj.getString(FieldType.PG_GST.toString()))
															+ Double.parseDouble(dbobj
																	.getString(FieldType.ACQUIRER_GST.toString()))));

											transReport.setPartner(String
													.valueOf(new BigDecimal(dbobj.getString(FieldType.UDF9.getName()))
															.multiply(new BigDecimal(merchantAmount))
															.divide(new BigDecimal(
																	Constants.MAX_NUMBER_OF_KEYS.getValue()))
															.setScale(2, BigDecimal.ROUND_HALF_UP)));
										} else {
											transReport.setPartner("0");
										}
									}
								} else {
									transReport.setPartner("0");
								}
							} else {
								transReport.setPartner("0");
							}

							if (dbobj.containsKey(FieldType.UDF11.getName())) {
								if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF11.getName()))) {
									transReport.setUniqueId(dbobj.getString(FieldType.UDF11.getName()));
								} else {
									transReport.setUniqueId("NA");
								}
							} else {
								transReport.setUniqueId("NA");
							}
						}

						if (dbobj.containsKey(FieldType.RESELLER_CHARGES.getName())
								&& dbobj.containsKey(FieldType.RESELLER_GST.getName())
								&& StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_CHARGES.getName()))
								&& StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_GST.getName()))
								&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.getName()))
								&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.getName()))
								&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.getName()))
								&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.getName()))) {

							transReport.setMerchantAmount(String
									.valueOf(new BigDecimal(dbobj.getString(FieldType.TOTAL_AMOUNT.getName())).subtract(
											new BigDecimal(dbobj.getString(FieldType.ACQUIRER_TDR_SC.getName()))
													.add(new BigDecimal(
															dbobj.getString(FieldType.ACQUIRER_GST.getName())))
													.add(new BigDecimal(dbobj.getString(FieldType.PG_TDR_SC.getName())))
													.add(new BigDecimal(dbobj.getString(FieldType.PG_GST.getName()))
															.add(new BigDecimal(dbobj
																	.getString(FieldType.RESELLER_CHARGES.getName()))
																			.add(new BigDecimal(dbobj
																					.getString(FieldType.RESELLER_GST
																							.getName()))))))));

						} else {

							if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.getName()))
									&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.getName()))
									&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.getName()))
									&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.getName()))) {

								transReport.setMerchantAmount(String.valueOf(
										new BigDecimal(dbobj.getString(FieldType.TOTAL_AMOUNT.getName())).subtract(
												new BigDecimal(dbobj.getString(FieldType.ACQUIRER_TDR_SC.getName()))
														.add(new BigDecimal(
																dbobj.getString(FieldType.ACQUIRER_GST.getName())))
														.add(new BigDecimal(
																dbobj.getString(FieldType.PG_TDR_SC.getName()))
																		.add(new BigDecimal(dbobj.getString(
																				FieldType.PG_GST.getName())))))));
							} else {
								transReport.setMerchantAmount(CrmFieldConstants.NA.getValue());
							}
						}
					}
				}

				if (null != dbobj.getString(FieldType.MOP_TYPE.toString())) {
					transReport.setMoptype(MopType.getmopName(dbobj.getString(FieldType.MOP_TYPE.toString())));
				} else {
					transReport.setMoptype(CrmFieldConstants.NA.getValue());
				}

				if (null != dbobj.getString(FieldType.CARD_HOLDER_TYPE.toString())) {
					transReport.setCardHolderType(dbobj.getString(FieldType.CARD_HOLDER_TYPE.toString()));
				} else {
					transReport.setCardHolderType(CrmFieldConstants.NA.toString());
				}

				transactionList.add(transReport);
			}
			cursor.close();
			logger.info("Inside TxnReports , searchPaymentDownloadForBookingRecord , transactionListSize = "
					+ transactionList.size());
			Comparator<PaymentSearchDownloadObject> comp = (PaymentSearchDownloadObject a,
					PaymentSearchDownloadObject b) -> {
				if (a.getDateFrom().compareTo(b.getDateFrom()) > 0) {
					return -1;
				} else if (a.getDateFrom().compareTo(b.getDateFrom()) < 0) {
					return 1;
				} else {
					return 0;
				}
			};
			Collections.sort(transactionList, comp);
			logger.info("transactionList created and Sorted");
			return transactionList;
		} catch (Exception e) {
			logger.error("Exception occured in TxnReports , searchPaymentDownloadForBookingRecord , Exception = ", e);
			return transactionList;
		}
	}

	// public List<Merchants> getAllActiveAndTxnMerchantList() {
	// List<Merchants> merchantsList = new ArrayList<Merchants>();
	// Map<String, Merchants> userMap = new HashMap<String, Merchants>();
	//
	// MongoDatabase dbIns = mongoInstance.getDB();
	// MongoCollection<Document> collection = dbIns
	// .getCollection(propertiesManager.propertiesMap.get(prefix +
	// Constants.COLLECTION_NAME.getValue()));
	// MongoCursor<Document> cursor = collection.find().iterator();
	//
	// while (cursor.hasNext()) {
	// Document documentObj = cursor.next();
	//
	// User user = new User();
	//
	// if (StringUtils.isNotBlank((String)
	// documentObj.get(FieldType.PAY_ID.getName()))) {
	// String payid = (String) documentObj.get(FieldType.PAY_ID.getName());
	//
	// if (userMap.get(payid) != null &&
	// !userMap.get(payid).getPayId().isEmpty()) {
	// } else {
	// user = userdao.findPayId(payid);
	//
	// Merchants merchant = new Merchants();
	// merchant.setEmailId(user.getEmailId());
	// merchant.setPayId(user.getPayId());
	// merchant.setBusinessName(user.getBusinessName());
	//
	// userMap.put(payid, merchant);
	// }
	// }
	//
	//// String payid = (String) documentObj.get(FieldType.PAY_ID.getName());
	//// User user = new User();
	////
	//// if (userMap.get(payid) != null &&
	// !userMap.get(payid).getPayId().isEmpty()) {
	//// } else {
	//// user = userdao.findPayId(payid);
	////
	//// Merchants merchant = new Merchants();
	//// merchant.setEmailId(user.getEmailId());
	//// merchant.setPayId(user.getPayId());
	//// merchant.setBusinessName(user.getBusinessName());
	////
	//// userMap.put(payid, merchant);
	//// }
	// }
	// cursor.close();
	//
	// List<Merchants> merchantsActiveList = userdao.getMerchantActiveList();
	//
	// for (Merchants merchant : merchantsActiveList) {
	//
	// userMap.put(merchant.getPayId(), merchant);
	// }
	//
	// for (Map.Entry<String, Merchants> entry : userMap.entrySet()) {
	// Merchants merchant = entry.getValue();
	//
	// merchantsList.add(merchant);
	// }
	//
	// return merchantsList;
	// }

	public ManualRefundProcess searchForStudentFeeManualRefund(String pgRefNum, String payId, String refundedAmount,
			String refundAvailable, String regNumber) {

		ManualRefundProcess refundProcess = new ManualRefundProcess();
		List<BasicDBObject> finalList = new ArrayList<BasicDBObject>();

		try {

			finalList.add(new BasicDBObject(FieldType.PG_REF_NUM.getName(), pgRefNum));
			finalList.add(new BasicDBObject(FieldType.PAY_ID.getName(), payId));
			finalList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName()));
			finalList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.CAPTURED.getName()));

			BasicDBObject finalquery = new BasicDBObject("$and", finalList);

			logger.info("Inside TxnReports , searchPayment , finalquery = " + finalquery);
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns
					.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));
			BasicDBObject match = new BasicDBObject("$match", finalquery);
			BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject("CREATE_DATE", -1));

			List<BasicDBObject> pipeline = Arrays.asList(match, sort);

			AggregateIterable<Document> output = coll.aggregate(pipeline);
			output.allowDiskUse(true);
			MongoCursor<Document> cursor = output.iterator();
			while (cursor.hasNext()) {
				Document dbobj = cursor.next();

				refundProcess.setAmount(dbobj.getString(FieldType.AMOUNT.getName()));
				refundProcess.setCurrencyCode(CurrencyTypes
						.getInstancefromName((dbobj.getString(FieldType.CURRENCY_CODE.getName()))).toString());
				refundProcess.setOrderId(dbobj.getString(FieldType.ORDER_ID.getName()));
				refundProcess.setTxnType(TransactionType.REFUND.getName());
				refundProcess.setPayId(payId);
				refundProcess.setMerchantName(userdao.getBusinessNameByPayId(payId));
				refundProcess.setPgRefNum(pgRefNum);
				refundProcess.setRefundedAmount(refundedAmount);
				refundProcess.setRefundAvailable(refundAvailable);

				if (StringUtils.isBlank(dbobj.getString(FieldType.SURCHARGE_FLAG.getName()))) {
					refundProcess.setSurchargeFlag("NA");
				} else {
					refundProcess.setSurchargeFlag(dbobj.getString(FieldType.SURCHARGE_FLAG.getName()));
				}
				if (StringUtils.isNotBlank(regNumber)) {
					refundProcess.setRegNumber(regNumber);
				}
			}
			cursor.close();

			logger.info("transactionList created and Sorted");
			return refundProcess;
		} catch (Exception ex) {
			logger.error("Exception in getting records for manual refund Process ", ex);
		}
		return refundProcess;
	}

	@SuppressWarnings("static-access")
	public List<TransactionSearch> deliveryStatusForSaleCaputureTransaction(List<TransactionSearch> transList) {
		logger.info("inside get the delivery status of sale capture report");
		List<TransactionSearch> transactionSearchList = new ArrayList<TransactionSearch>();
		try {
			if (!transList.isEmpty()) {
				MongoCursor<Document> cursor = null;
				for (TransactionSearch list : transList) {

					if (StringUtils.isNotBlank(list.getDeliveryStatus())) {
						String deliveryStatus = list.getDeliveryStatus();
						if (deliveryStatus.equalsIgnoreCase("All")) {
							list.setDeliveryStatus("PENDING");
						}

						List<BasicDBObject> query = new ArrayList<BasicDBObject>();
						query.add(new BasicDBObject(FieldType.ORDER_ID.getName(), list.getOrderId()));

						if (StringUtils.isNotBlank(deliveryStatus) && !deliveryStatus.equalsIgnoreCase("All")) {
							if (!deliveryStatus.equalsIgnoreCase("PENDING")) {
								query.add(new BasicDBObject(FieldType.STATUS.getName(), deliveryStatus));
							}
						}

						BasicDBObject finalQuery = new BasicDBObject("$and", query);
						logger.info(
								"Inside TxnReports , delivery status for sale capture , finalquery = " + finalQuery);
						MongoDatabase dbIns = mongoInstance.getDB();
						MongoCollection<Document> coll = dbIns.getCollection(propertiesManager.propertiesMap
								.get(prefix + Constants.DELIVERY_STATUS_COLLECTION_NAME.getValue()));
						FindIterable<Document> iterDoc = coll.find(finalQuery);
						cursor = iterDoc.iterator();

						if (cursor.hasNext() && !deliveryStatus.equalsIgnoreCase("All")) {
							Document dbobj = cursor.next();
							if (dbobj != null
									&& dbobj.getString(FieldType.STATUS.getName()).equalsIgnoreCase(deliveryStatus)) {
								list.setDeliveryStatus(dbobj.getString(FieldType.STATUS.getName()));
								transactionSearchList.add(list);
							}
						} else if (!cursor.hasNext() && !deliveryStatus.equalsIgnoreCase("All")
								&& deliveryStatus.equalsIgnoreCase("PENDING")) {
							transactionSearchList.add(list);
						} else if (cursor.hasNext() && deliveryStatus.equalsIgnoreCase("All")) {
							Document dbobj = cursor.next();
							list.setDeliveryStatus(dbobj.getString(FieldType.STATUS.getName()));
							transactionSearchList.add(list);
						} else if (deliveryStatus.equalsIgnoreCase("All")) {
							transactionSearchList.add(list);
						}

						cursor.close();
					} else {
						transactionSearchList.add(list);
					}
				}
			}
			return transactionSearchList;
		} catch (Exception e) {
			logger.info("Exception caught in Txn Reports delivery status for sale capture report ", e);
		}
		return transactionSearchList;
	}

	@SuppressWarnings("static-access")
	public List<PaymentSearchDownloadObject> downloadSaleCaputureTransactionForDeliveryStatus(
			List<PaymentSearchDownloadObject> transList) {
		logger.info("inside txn report get the delivery status for download sale capture report");
		List<PaymentSearchDownloadObject> transactionSearchList = new ArrayList<PaymentSearchDownloadObject>();
		try {
			if (!transList.isEmpty()) {
				MongoCursor<Document> cursor = null;
				for (PaymentSearchDownloadObject list : transList) {

					if (StringUtils.isNotBlank(list.getDeliveryStatus())) {
						String deliveryStatus = list.getDeliveryStatus();
						if (deliveryStatus.equalsIgnoreCase("All")) {
							list.setDeliveryStatus("PENDING");
						}

						List<BasicDBObject> query = new ArrayList<BasicDBObject>();
						query.add(new BasicDBObject(FieldType.ORDER_ID.getName(), list.getOrderId()));

						if (StringUtils.isNotBlank(deliveryStatus) && !deliveryStatus.equalsIgnoreCase("All")) {
							if (!deliveryStatus.equalsIgnoreCase("PENDING")) {
								query.add(new BasicDBObject(FieldType.STATUS.getName(), deliveryStatus));
							}
						}

						BasicDBObject finalQuery = new BasicDBObject("$and", query);
						logger.info(
								"Inside TxnReports , delivery status for download sale capture report , finalquery = "
										+ finalQuery);
						MongoDatabase dbIns = mongoInstance.getDB();
						MongoCollection<Document> coll = dbIns.getCollection(propertiesManager.propertiesMap
								.get(prefix + Constants.DELIVERY_STATUS_COLLECTION_NAME.getValue()));
						FindIterable<Document> iterDoc = coll.find(finalQuery);
						cursor = iterDoc.iterator();

						if (cursor.hasNext() && !deliveryStatus.equalsIgnoreCase("All")) {
							Document dbobj = cursor.next();
							if (dbobj != null
									&& dbobj.getString(FieldType.STATUS.getName()).equalsIgnoreCase(deliveryStatus)) {
								list.setDeliveryStatus(dbobj.getString(FieldType.STATUS.getName()));
								transactionSearchList.add(list);
							}
						} else if (!cursor.hasNext() && !deliveryStatus.equalsIgnoreCase("All")
								&& deliveryStatus.equalsIgnoreCase("PENDING")) {
							transactionSearchList.add(list);
						} else if (cursor.hasNext() && deliveryStatus.equalsIgnoreCase("All")) {
							Document dbobj = cursor.next();
							list.setDeliveryStatus(dbobj.getString(FieldType.STATUS.getName()));
							transactionSearchList.add(list);
						} else if (deliveryStatus.equalsIgnoreCase("All")) {
							transactionSearchList.add(list);
						}
						cursor.close();
					} else {
						transactionSearchList.add(list);
					}
				}
			}
			return transactionSearchList;
		} catch (Exception e) {
			logger.info("Exception caught in Txn Reports delivery status for sale capture report ", e);
		}
		return transactionSearchList;
	}

	public List<TransactionSearch> getDispatchDetails(List<TransactionSearch> transList) {
		logger.info("inside the txn report getDispatchDetails for Booking Record");
		try {
			if (!transList.isEmpty()) {
				for (TransactionSearch list : transList) {

					BasicDBObject finalquery = new BasicDBObject(FieldType.ORDER_ID.getName(), list.getOrderId());

					logger.info("Inside TxnReports , getDispatchDetails , finalquery = " + finalquery);
					MongoDatabase dbIns = mongoInstance.getDB();
					MongoCollection<Document> coll = dbIns.getCollection(propertiesManager.propertiesMap
							.get(prefix + Constants.DISPATCH_SLIP_COLLECTION_NAME.getValue()));

					MongoCursor<Document> cursor = coll.find(finalquery).iterator();

					while (cursor.hasNext()) {
						Document dbObj = cursor.next();

						if (dbObj.containsKey("INVOICE_NO") && StringUtils.isNotBlank(dbObj.getString("INVOICE_NO"))) {
							list.setInvoiceNo(dbObj.getString("INVOICE_NO"));
						} else {
							list.setInvoiceNo(CrmFieldConstants.NA.getValue());
						}

						if (dbObj.containsKey("COURIER_SERVICE_PROVIDER")
								&& StringUtils.isNotBlank(dbObj.getString("COURIER_SERVICE_PROVIDER"))) {
							list.setCourierServiceProvider(dbObj.getString("COURIER_SERVICE_PROVIDER"));
						} else {
							list.setCourierServiceProvider(CrmFieldConstants.NA.getValue());
						}

						if (dbObj.containsKey("DISPATCH_SLIP_NO")
								&& StringUtils.isNotBlank(dbObj.getString("DISPATCH_SLIP_NO"))) {
							list.setDispatchSlipNo(dbObj.getString("DISPATCH_SLIP_NO"));
						} else {
							list.setDispatchSlipNo(CrmFieldConstants.NA.getValue());
						}

						if (dbObj.containsKey("base64_Receipt") && StringUtils.isNotBlank("base64_Receipt")) {
							list.setPdfDownloadFlag(true);
						} else {
							list.setPdfDownloadFlag(false);
						}
						cursor.close();
						break;
					}
				}
			}
			return transList;
		} catch (Exception ex) {
			logger.info("Exception caught in TxnReports getDispatchDetails ", ex);
		}
		return transList;
	}

	public List<PaymentSearchDownloadObject> getDispatchDetailsForDownloadBookingRecord(
			List<PaymentSearchDownloadObject> transList) {
		logger.info("inside the txn report getDispatchDetails for Booking Record");
		try {
			if (!transList.isEmpty()) {
				for (PaymentSearchDownloadObject list : transList) {

					BasicDBObject finalquery = new BasicDBObject(FieldType.ORDER_ID.getName(), list.getOrderId());

					logger.info("Inside TxnReports , getDispatchDetails , finalquery = " + finalquery);
					MongoDatabase dbIns = mongoInstance.getDB();
					MongoCollection<Document> coll = dbIns.getCollection(propertiesManager.propertiesMap
							.get(prefix + Constants.DISPATCH_SLIP_COLLECTION_NAME.getValue()));

					MongoCursor<Document> cursor = coll.find(finalquery).iterator();

					while (cursor.hasNext()) {
						Document dbObj = cursor.next();

						if (dbObj.containsKey("INVOICE_NO") && StringUtils.isNotBlank(dbObj.getString("INVOICE_NO"))) {
							list.setInvoiceNo(dbObj.getString("INVOICE_NO"));
						} else {
							list.setInvoiceNo(CrmFieldConstants.NA.getValue());
						}

						if (dbObj.containsKey("COURIER_SERVICE_PROVIDER")
								&& StringUtils.isNotBlank(dbObj.getString("COURIER_SERVICE_PROVIDER"))) {
							list.setCourierServiceProvider(dbObj.getString("COURIER_SERVICE_PROVIDER"));
						} else {
							list.setCourierServiceProvider(CrmFieldConstants.NA.getValue());
						}

						if (dbObj.containsKey("DISPATCH_SLIP_NO")
								&& StringUtils.isNotBlank(dbObj.getString("DISPATCH_SLIP_NO"))) {
							list.setDispatchSlipNo(dbObj.getString("DISPATCH_SLIP_NO"));
						} else {
							list.setDispatchSlipNo(CrmFieldConstants.NA.getValue());
						}
						cursor.close();
						break;
					}
				}
			}
			return transList;
		} catch (Exception ex) {
			logger.info("Exception caught in TxnReports getDispatchDetails ", ex);
		}
		return transList;
	}

	@SuppressWarnings("static-access")
	public List<ImpsDownloadObject> searchImpsReportForDownload(String reportMerchant, String subMerchantPayId,
			String reportStatus, String reportDateFrom, String reportDateTo, User user, String channel) {
		logger.info("Inside TxnReports , searchPayment");
		Map<String, User> userMap = new HashMap<String, User>();
		Set<String> orderId = new HashSet<String>();

		boolean isParameterised = false;
		List<ImpsDownloadObject> transactionList = new ArrayList<ImpsDownloadObject>();
		try {
			List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();
			List<BasicDBObject> statusConditionList = new ArrayList<BasicDBObject>();
			BasicDBObject dateQuery = new BasicDBObject();
			BasicDBObject allParamQuery = new BasicDBObject();
			BasicDBObject statusQuery = new BasicDBObject();

			String currentDate = null;
			if (!reportDateFrom.isEmpty()) {
				if (!reportDateTo.isEmpty()) {
					currentDate = reportDateTo;
				} else {
					DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					Calendar cal = Calendar.getInstance();
					currentDate = dateFormat.format(cal.getTime());
				}
				dateQuery.put(FieldType.CREATE_DATE.getName(),
						BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(reportDateFrom).toLocalizedPattern())
								.add("$lte", new SimpleDateFormat(currentDate).toLocalizedPattern()).get());
			}

			if (!reportMerchant.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.PAY_ID.getName(), reportMerchant));

				isParameterised = true;
			}
			if (StringUtils.isNotBlank(subMerchantPayId) && !subMerchantPayId.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.SUB_MERCHANT_ID.getName(), subMerchantPayId));
				isParameterised = true;
			}

			if (StringUtils.isNotBlank(channel) && !channel.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.USER_TYPE.getName(), channel));
				isParameterised = true;
			}

			if (StringUtils.isNotBlank(reportStatus) && !reportStatus.equalsIgnoreCase("ALL")) {
				isParameterised = true;
				String statusArr[] = reportStatus.split(",");
				for (String sts : statusArr) {
					statusConditionList.add(new BasicDBObject(FieldType.STATUS.getName(), sts.trim()));
				}
				statusQuery.append("$or", statusConditionList);
			}
			List<BasicDBObject> allConditionQueryList = new ArrayList<BasicDBObject>();
			if (!dateQuery.isEmpty()) {
				allConditionQueryList.add(dateQuery);
			}

			if (!paramConditionLst.isEmpty()) {
				allParamQuery = new BasicDBObject("$and", paramConditionLst);
			}

			BasicDBObject allConditionQueryObj = new BasicDBObject("$and", allConditionQueryList);
			List<BasicDBObject> fianlList = new ArrayList<BasicDBObject>();

			if (!allParamQuery.isEmpty()) {
				fianlList.add(allParamQuery);
			}

			if (!allConditionQueryObj.isEmpty()) {
				fianlList.add(allConditionQueryObj);
			}

			if (!statusQuery.isEmpty()) {
				fianlList.add(statusQuery);
			}

			BasicDBObject finalquery = new BasicDBObject("$and", fianlList);

			logger.info("Inside TxnReports , downloadSearchPayment , finalquery = " + finalquery);
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					propertiesManager.propertiesMap.get(prefix + Constants.IMPS_SETTlEMENT_COLLECTION.getValue()));
			BasicDBObject match = new BasicDBObject("$match", finalquery);
			BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject("CREATE_DATE", -1));

			List<BasicDBObject> pipeline = Arrays.asList(match, sort/* , group */);

			AggregateIterable<Document> output = coll.aggregate(pipeline);
			output.allowDiskUse(true);
			MongoCursor<Document> cursor = output.iterator();

			while (cursor.hasNext()) {
				Document dbobj = cursor.next();
				if (dbobj.containsKey(FieldType.USER_TYPE.getName())
						&& !dbobj.getString(FieldType.USER_TYPE.getName()).equalsIgnoreCase("Verification"))
					orderId.add(dbobj.getString(FieldType.ORDER_ID.getName()));
			}
			cursor.close();
			for (String id : orderId) {
				BasicDBObject finalquery2 = new BasicDBObject(FieldType.ORDER_ID.getName(), id);

				BasicDBObject match1 = new BasicDBObject("$match", finalquery2);
				BasicDBObject sort1 = new BasicDBObject("$sort", new BasicDBObject(FieldType.CREATE_DATE.name(), -1));
				BasicDBObject limit = new BasicDBObject("$limit", 1);
				List<BasicDBObject> pipeline2 = Arrays.asList(match1, sort1, limit);
				AggregateIterable<Document> output2 = coll.aggregate(pipeline2);
				output2.allowDiskUse(true);
				MongoCursor<Document> cursor2 = output2.iterator();

				while (cursor2.hasNext()) {

					Document dbobj = cursor2.next();

					if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
							&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
						dbobj = dataEncDecTool.decryptDocument(dbobj);
					}
					ImpsDownloadObject impsReport = new ImpsDownloadObject();

					String payid = (String) dbobj.get(FieldType.PAY_ID.getName());

					// For Merchant Business Name

					User merchantUser = new User();

					if (userMap.get(payid) != null && !userMap.get(payid).getPayId().isEmpty()) {
						merchantUser = userMap.get(merchantUser);
					} else {
						merchantUser = userdao.findPayId(payid);
						userMap.put(payid, merchantUser);
					}

					if (merchantUser != null) {
						impsReport.setMerchant(merchantUser.getBusinessName());
					} else {
						impsReport.setMerchant(userdao.getBusinessNameByPayId(payid));
					}

					if (((!reportMerchant.equalsIgnoreCase("All")) || (reportMerchant.equalsIgnoreCase("All")))
							&& dbobj.containsKey(FieldType.SUB_MERCHANT_ID.getName())
							&& StringUtils.isNotBlank(dbobj.getString(FieldType.SUB_MERCHANT_ID.getName()))) {

						String subMerchant = dbobj.getString(FieldType.SUB_MERCHANT_ID.getName());
						User subMerchantUser = new User();

						if (userMap.get(subMerchant) != null && !userMap.get(subMerchant).getPayId().isEmpty()) {
							subMerchantUser = userMap.get(subMerchant);
						} else {
							subMerchantUser = userdao.findPayId(subMerchant);
							userMap.put(subMerchant, subMerchantUser);
						}
						if (subMerchantUser != null) {
							impsReport.setSubMerchant(subMerchantUser.getBusinessName());
						} else {
							impsReport.setSubMerchant(CrmFieldConstants.NA.getValue());
						}
					} else {
						impsReport.setSubMerchant(CrmFieldConstants.NA.getValue());
					}

					if (StringUtils.isNotBlank(dbobj.getString(FieldType.SUB_MERCHANT_ID.getName()))) {
						impsReport.setMerchantPayId(dbobj.getString(FieldType.SUB_MERCHANT_ID.getName()));
					} else {
						impsReport.setMerchantPayId(dbobj.getString(FieldType.PAY_ID.getName()));
					}

					if (StringUtils.isNotBlank(dbobj.getString(FieldType.CAPTURED_DATE_FROM.getName()))) {
						impsReport.setTxnsCapturedFrom(dbobj.getString(FieldType.CAPTURED_DATE_FROM.getName()));
					} else {
						impsReport.setTxnsCapturedFrom(CrmFieldConstants.NA.getValue());
					}

					if (StringUtils.isNotBlank(dbobj.getString(FieldType.CAPTURED_DATE_TO.getName()))) {
						impsReport.setTxnsCapturedTo(dbobj.getString(FieldType.CAPTURED_DATE_TO.getName()));
					} else {
						impsReport.setTxnsCapturedTo(CrmFieldConstants.NA.getValue());
					}

					if (StringUtils.isNotBlank(dbobj.getString(FieldType.SETTLED_DATE.getName()))) {
						impsReport.setSystemSettlementDate(dbobj.getString(FieldType.SETTLED_DATE.getName()));
					} else {
						impsReport.setSystemSettlementDate(CrmFieldConstants.NA.getValue());
					}

					if (StringUtils.isNotBlank(dbobj.getString(FieldType.RRN.getName()))) {
						impsReport.setImpsRefNum(dbobj.getString(FieldType.RRN.getName()));
					} else {
						impsReport.setImpsRefNum(CrmFieldConstants.NA.getValue());
					}

					if (StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TXN_MESSAGE.getName()))) {
						impsReport.setResponseMsg(dbobj.getString(FieldType.PG_TXN_MESSAGE.getName()));
					} else {
						impsReport.setResponseMsg(CrmFieldConstants.NA.getValue());
					}

					if (StringUtils.isNotBlank(dbobj.getString(FieldType.PHONE_NO.getName()))) {
						impsReport.setPhoneNo(dbobj.getString(FieldType.PHONE_NO.getName()));
					} else {
						impsReport.setPhoneNo(CrmFieldConstants.NA.getValue());
					}

					impsReport.setTxnId(dbobj.getString(FieldType.TXN_ID.getName()));
					impsReport.setOrderId(dbobj.getString(FieldType.ORDER_ID.getName()));
					impsReport.setAmount(dbobj.getString(FieldType.AMOUNT.getName()));
					impsReport.setBankAccountName(dbobj.getString(FieldType.BENE_NAME.getName()));
					impsReport.setBankAccountNumber(dbobj.getString(FieldType.BENE_ACCOUNT_NO.getName()));
					impsReport.setBankIFSC(dbobj.getString(FieldType.IFSC_CODE.getName()));
					impsReport.setDate(dbobj.getString(FieldType.CREATE_DATE.getName()));
					impsReport.setStatus(dbobj.getString(FieldType.STATUS.getName()));
					if (dbobj.containsKey(FieldType.USER_TYPE.getName())
							&& StringUtils.isNotBlank(dbobj.getString(FieldType.USER_TYPE.getName()))) {
						impsReport.setUserType((dbobj.getString(FieldType.USER_TYPE.getName())));
					} else {
						impsReport.setUserType(CrmFieldConstants.NA.getValue());
					}
					transactionList.add(impsReport);
				}
			}
			logger.info("transactionList created and size = " + transactionList.size());
			return transactionList;
		} catch (Exception e) {
			logger.error("Exception occured in TxnReports , searchPayment , Exception = ", e);
			return transactionList;
		}

	}

	@SuppressWarnings("static-access")
	public int eCollectionCount(String payId, String subMerchantPayIdd, String paymentMode, String status,
			String txnType, String dateFrom, String dateTo, User user) {

		logger.info("Inside TxnReports , eCollectionCount");
		boolean isParameterised = false;
		try {
			int total = 0;
			List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();

			BasicDBObject dateQuery = new BasicDBObject();
			BasicDBObject allParamQuery = new BasicDBObject();
			BasicDBObject statusQuery = new BasicDBObject();

			String currentDate = null;
			if (!dateFrom.isEmpty()) {
				if (!dateTo.isEmpty()) {
					currentDate = dateTo;
				} else {
					DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					Calendar cal = Calendar.getInstance();
					currentDate = dateFormat.format(cal.getTime());
				}

				dateQuery.put(FieldType.CREATE_DATE.getName(),
						BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(dateFrom).toLocalizedPattern())
								.add("$lte", new SimpleDateFormat(currentDate).toLocalizedPattern()).get());
			}
			if (user.getUserType().equals(UserType.PARENTMERCHANT)) {

				List<ParentMerchantMapping> parentMapping = parentMerchantMappingDao
						.findActiveMerchantByPayId(user.getPayId());

				List<String> parentMerchantChildIdList = new ArrayList<>();

				for (ParentMerchantMapping mapping : parentMapping) {
					parentMerchantChildIdList.add(mapping.getMerchantPayId());
				}

				paramConditionLst.add(new BasicDBObject(FieldType.PAY_ID.getName(),
						new BasicDBObject("$in", parentMerchantChildIdList)));

				isParameterised = true;

			} else if (!payId.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.PAY_ID.getName(), payId));

				isParameterised = true;
			}
			if (StringUtils.isNotBlank(subMerchantPayIdd) && !subMerchantPayIdd.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.SUB_MERCHANT_ID.getName(), subMerchantPayIdd));
				isParameterised = true;
			}
			if (user.getUserType().equals(UserType.RESELLER)) {
				paramConditionLst.add(new BasicDBObject(FieldType.RESELLER_ID.getName(), user.getResellerId()));
			}

			if (!paymentMode.equalsIgnoreCase("ALL")) {
				isParameterised = true;
				paramConditionLst.add(new BasicDBObject(FieldType.MOP_TYPE.getName(), paymentMode));
			} else {
			}

			if (!status.equalsIgnoreCase("ALL")) {
				isParameterised = true;
				paramConditionLst.add(new BasicDBObject(FieldType.STATUS.getName(), status));
			} else {
			}

			if (!txnType.equalsIgnoreCase("ALL")) {
				isParameterised = true;
				paramConditionLst.add(new BasicDBObject(FieldType.TXNTYPE.getName(), txnType));
			}

			if (!paramConditionLst.isEmpty()) {
				allParamQuery = new BasicDBObject("$and", paramConditionLst);
			}
			List<BasicDBObject> allConditionQueryList = new ArrayList<BasicDBObject>();
			if (!dateQuery.isEmpty()) {
				allConditionQueryList.add(dateQuery);
			}
			BasicDBObject allConditionQueryObj = new BasicDBObject("$and", allConditionQueryList);
			List<BasicDBObject> fianlList = new ArrayList<BasicDBObject>();

			if (!allParamQuery.isEmpty()) {
				fianlList.add(allParamQuery);
			}

			if (!allConditionQueryObj.isEmpty()) {
				fianlList.add(allConditionQueryObj);
			}
			BasicDBObject finalquery = new BasicDBObject("$and", fianlList);

			logger.info("Inside TxnReports , eCollectionCount , finalquery = " + finalquery);
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns
					.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.E_COLLECTION.getValue()));
			total = (int) coll.count(finalquery);
			logger.info("Inside search eCollection count , total records from DB  = " + total);
			return total;
		} catch (Exception e) {
			logger.error("Exception occured in TxnReports , E_CollectionCount n exception = ", e);
			return 0;
		}
	}

	@SuppressWarnings("static-access")
	public List<ECollectionObject> eCollectionData(String merchantPayId, String subMerchantPayIdd, String paymentMode,
			String status, String txnType, String dateFrom, String dateTo, User user, int start, int length) {
		Map<String, User> userMap = new HashMap<String, User>();

		logger.info("Inside TxnReports , eCollectionData()");

		boolean isParameterised = false;
		List<ECollectionObject> eCollectionDataList = new ArrayList<ECollectionObject>();
		try {

			List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();

			BasicDBObject dateQuery = new BasicDBObject();
			BasicDBObject allParamQuery = new BasicDBObject();

			String currentDate = null;
			if (!dateFrom.isEmpty()) {
				if (!dateTo.isEmpty()) {
					currentDate = dateTo;
				} else {
					DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					Calendar cal = Calendar.getInstance();
					currentDate = dateFormat.format(cal.getTime());
				}

				dateQuery.put(FieldType.CREATE_DATE.getName(),
						BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(dateFrom).toLocalizedPattern())
								.add("$lte", new SimpleDateFormat(currentDate).toLocalizedPattern()).get());
			}
			if (user.getUserType().equals(UserType.PARENTMERCHANT)) {

				List<ParentMerchantMapping> parentMapping = parentMerchantMappingDao
						.findActiveMerchantByPayId(user.getPayId());

				List<String> parentMerchantChildIdList = new ArrayList<>();

				for (ParentMerchantMapping mapping : parentMapping) {
					parentMerchantChildIdList.add(mapping.getMerchantPayId());
				}

				paramConditionLst.add(new BasicDBObject(FieldType.PAY_ID.getName(),
						new BasicDBObject("$in", parentMerchantChildIdList)));

				isParameterised = true;

			} else if (!merchantPayId.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.PAY_ID.getName(), merchantPayId));

				isParameterised = true;
			}
			if (StringUtils.isNotBlank(subMerchantPayIdd) && !subMerchantPayIdd.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.SUB_MERCHANT_ID.getName(), subMerchantPayIdd));
				isParameterised = true;
			}
			if (user.getUserType().equals(UserType.RESELLER)) {
				paramConditionLst.add(new BasicDBObject(FieldType.RESELLER_ID.getName(), user.getResellerId()));
			}
			if (!paymentMode.equalsIgnoreCase("ALL")) {
				isParameterised = true;
				paramConditionLst.add(new BasicDBObject(FieldType.MOP_TYPE.getName(), paymentMode));
			} else {
			}

			if (!status.equalsIgnoreCase("ALL")) {
				isParameterised = true;
				paramConditionLst.add(new BasicDBObject(FieldType.STATUS.getName(), status));
			} else {
			}

			if (!txnType.equalsIgnoreCase("ALL")) {
				isParameterised = true;
				paramConditionLst.add(new BasicDBObject(FieldType.TXNTYPE.getName(), txnType));
			}

			if (!paramConditionLst.isEmpty()) {
				allParamQuery = new BasicDBObject("$and", paramConditionLst);
			}
			List<BasicDBObject> allConditionQueryList = new ArrayList<BasicDBObject>();
			if (!dateQuery.isEmpty()) {
				allConditionQueryList.add(dateQuery);
			}
			BasicDBObject allConditionQueryObj = new BasicDBObject("$and", allConditionQueryList);
			List<BasicDBObject> fianlList = new ArrayList<BasicDBObject>();

			if (!allParamQuery.isEmpty()) {
				fianlList.add(allParamQuery);
			}

			if (!allConditionQueryObj.isEmpty()) {
				fianlList.add(allConditionQueryObj);
			}
			BasicDBObject finalquery = new BasicDBObject("$and", fianlList);

			logger.info("Inside TxnReports , eCollection, finalquery = " + finalquery);
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns
					.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.E_COLLECTION.getValue()));
			BasicDBObject match = new BasicDBObject("$match", finalquery);
			BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject("CREATE_DATE", -1));
			BasicDBObject skip = new BasicDBObject("$skip", start);
			BasicDBObject limit = new BasicDBObject("$limit", length);
			List<BasicDBObject> pipeline = Arrays.asList(match, sort, skip, limit);
			AggregateIterable<Document> output = coll.aggregate(pipeline);
			output.allowDiskUse(true);
			MongoCursor<Document> cursor = output.iterator();

			while (cursor.hasNext()) {
				Document dbobj = cursor.next();
				if (StringUtils.isNotBlank(dbobj.getString("IS_ENCRYPTED"))
						&& dbobj.getString("IS_ENCRYPTED").equalsIgnoreCase("Y")) {
					dbobj = dataEncDecTool.decryptDocument(dbobj);
				}

				ECollectionObject eCollectionList = new ECollectionObject();

				eCollectionList.setMerchant(dbobj.getString(CrmFieldType.BUSINESS_NAME.getName()));

				User user1 = new User();

				if (StringUtils.isNotBlank((String) dbobj.get(FieldType.PAY_ID.getName()))) {
					String payid = (String) dbobj.get(FieldType.PAY_ID.getName());

					if (userMap.get(payid) != null && !userMap.get(payid).getPayId().isEmpty()) {
						user1 = userMap.get(payid);
					} else {
						user1 = userdao.findPayId(payid);
						userMap.put(payid, user1);
					}
				}

				// String payid = (String)
				// dbobj.get(FieldType.PAY_ID.getName());
				// User user1 = new User();
				// if (userMap.get(payid) != null &&
				// !userMap.get(payid).getPayId().isEmpty()) {
				// user1 = userMap.get(payid);
				// } else {
				// user1 = userdao.findPayId(payid);
				// userMap.put(payid, user1);
				// }
				if (user1 != null) {
					eCollectionList.setMerchant(user1.getBusinessName());
				}

				// if (!merchantPayId.equalsIgnoreCase("All") &&
				if (dbobj.containsKey(FieldType.SUB_MERCHANT_ID.getName())
						&& StringUtils.isNotBlank(dbobj.getString(FieldType.SUB_MERCHANT_ID.getName()))) {

					String subMerchant = dbobj.getString(FieldType.SUB_MERCHANT_ID.getName());
					User subMerchantUser = new User();

					if (userMap.get(subMerchant) != null && !userMap.get(subMerchant).getPayId().isEmpty()) {
						subMerchantUser = userMap.get(subMerchant);
					} else {
						subMerchantUser = userdao.findPayId(subMerchant);
						userMap.put(subMerchant, subMerchantUser);
					}
					if (subMerchantUser != null) {
						eCollectionList.setSubMerchant(subMerchantUser.getBusinessName());
					} else {
						eCollectionList.setSubMerchant(CrmFieldConstants.NA.getValue());
					}
				}
				if (user.getUserType().equals(UserType.ADMIN) || user.getUserType().equals(UserType.SUBADMIN)) {
					eCollectionList.setPayId(dbobj.getString(FieldType.PAY_ID.toString()));
					eCollectionList
							.setMerchantVirtualAccountNumber(dbobj.getString(FieldType.VIRTUAL_AC_CODE.toString()));
					eCollectionList.setTransactionDate(dbobj.getString(FieldType.CREATE_DATE.toString()));
					if (StringUtils.isNotBlank(dbobj.getString(FieldType.CUSTOMER_CODE.toString()))) {
						eCollectionList.setpaymentGatewayCode(dbobj.getString(FieldType.CUSTOMER_CODE.toString()));
					} else {
						eCollectionList.setpaymentGatewayCode(CrmFieldConstants.NA.getValue());
					}
					if (StringUtils.isNotBlank(dbobj.getString(FieldType.CUSTOMER_ACCOUNT_NO.toString()))) {
						eCollectionList.setpaymentGatewayAccountNumber(
								dbobj.getString(FieldType.CUSTOMER_ACCOUNT_NO.toString()));
					} else {
						eCollectionList.setpaymentGatewayAccountNumber(CrmFieldConstants.NA.getValue());
					}
					eCollectionList.setPaymentMode(dbobj.getString(FieldType.PAYMENT_TYPE.toString()));
					if (StringUtils.isNotBlank(dbobj.getString(FieldType.TXNTYPE.toString()))) {
						if (dbobj.getString(FieldType.TXNTYPE.toString()).equalsIgnoreCase("COLLECTION")) {
							eCollectionList.setTxnType("ECOLLECTION");
						}
						if (dbobj.getString(FieldType.TXNTYPE.toString()).equalsIgnoreCase("Topup")) {
							eCollectionList.setTxnType("Topup");
						}
					} else {
						eCollectionList.setTxnType(CrmFieldConstants.NA.getValue());
					}
					if (StringUtils.isNotBlank(dbobj.getString(FieldType.PAYEE_NAME.toString()))) {
						eCollectionList.setPayeeName(dbobj.getString(FieldType.PAYEE_NAME.toString()));
					} else {
						eCollectionList.setPayeeName(CrmFieldConstants.NA.getValue());
					}
					if (StringUtils.isNotBlank(dbobj.getString(FieldType.PAYEE_ACCOUNT_NUMBER.toString()))) {
						eCollectionList
								.setPayeeAccountNumber(dbobj.getString(FieldType.PAYEE_ACCOUNT_NUMBER.toString()));
					} else {
						eCollectionList.setPayeeAccountNumber(CrmFieldConstants.NA.getValue());
					}
					if (StringUtils.isNotBlank(dbobj.getString(FieldType.PAYEE_BANK_IFSC.toString()))) {
						eCollectionList.setPayeeBankIFSC(dbobj.getString(FieldType.PAYEE_BANK_IFSC.toString()));
					} else {
						eCollectionList.setPayeeBankIFSC(CrmFieldConstants.NA.getValue());
					}
					if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQ_ID.toString()))) {
						eCollectionList.setBankTxnNumber(dbobj.getString(FieldType.ACQ_ID.toString()));
					} else {
						eCollectionList.setBankTxnNumber(CrmFieldConstants.NA.getValue());
					}
					eCollectionList.setTotalAmount(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()));
					eCollectionList.setAmount(dbobj.getString(FieldType.AMOUNT.toString()));
					eCollectionList.setStatus(dbobj.getString(FieldType.STATUS.toString()));
					if (StringUtils.isNotBlank(dbobj.getString(FieldType.SENDER_REMARK.toString()))) {
						eCollectionList.setSenderRemark(dbobj.getString(FieldType.SENDER_REMARK.toString()));
					} else {
						eCollectionList.setSenderRemark(CrmFieldConstants.NA.getValue());
					}
					if (StringUtils.isNotBlank(dbobj.getString(FieldType.FIXED_CHARGES.toString())))
						eCollectionList.setFixedCharges(dbobj.getString(FieldType.FIXED_CHARGES.toString()));
					else
						eCollectionList.setFixedCharges("0.00");

					if (StringUtils.isNotBlank(dbobj.getString(FieldType.PERCENTAGE_CHARGES.toString())))
						eCollectionList.setPercentageCharges(dbobj.getString(FieldType.PERCENTAGE_CHARGES.toString()));
					else
						eCollectionList.setPercentageCharges("0.00");

					if (StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.toString())))
						eCollectionList.setPgGst(dbobj.getString(FieldType.PG_GST.toString()));
					else
						eCollectionList.setPgGst("0.00");

					eCollectionList.setTotalPaCommission(String.valueOf(new BigDecimal(eCollectionList.getPgGst())
							.add(new BigDecimal(eCollectionList.getPercentageCharges())
									.add(new BigDecimal(eCollectionList.getFixedCharges())))));

					eCollectionDataList.add(eCollectionList);
				} else {
					eCollectionList.setPayId(dbobj.getString(FieldType.PAY_ID.toString()));
					eCollectionList
							.setMerchantVirtualAccountNumber(dbobj.getString(FieldType.VIRTUAL_AC_CODE.toString()));
					eCollectionList.setTransactionDate(dbobj.getString(FieldType.CREATE_DATE.toString()));
					eCollectionList.setPaymentMode(dbobj.getString(FieldType.PAYMENT_TYPE.toString()));
					if (StringUtils.isNotBlank(dbobj.getString(FieldType.TXNTYPE.toString()))) {
						if (dbobj.getString(FieldType.TXNTYPE.toString()).equalsIgnoreCase("COLLECTION")) {
							eCollectionList.setTxnType("ECOLLECTION");
						}
						if (dbobj.getString(FieldType.TXNTYPE.toString()).equalsIgnoreCase("Topup")) {
							eCollectionList.setTxnType("Topup");
						}
					} else {
						eCollectionList.setTxnType(CrmFieldConstants.NA.getValue());
					}
					if (StringUtils.isNotBlank(dbobj.getString(FieldType.PAYEE_NAME.toString()))) {
						eCollectionList.setPayeeName(dbobj.getString(FieldType.PAYEE_NAME.toString()));
					} else {
						eCollectionList.setPayeeName(CrmFieldConstants.NA.getValue());
					}
					if (StringUtils.isNotBlank(dbobj.getString(FieldType.PAYEE_ACCOUNT_NUMBER.toString()))) {
						eCollectionList
								.setPayeeAccountNumber(dbobj.getString(FieldType.PAYEE_ACCOUNT_NUMBER.toString()));
					} else {
						eCollectionList.setPayeeAccountNumber(CrmFieldConstants.NA.getValue());
					}
					if (StringUtils.isNotBlank(dbobj.getString(FieldType.PAYEE_BANK_IFSC.toString()))) {
						eCollectionList.setPayeeBankIFSC(dbobj.getString(FieldType.PAYEE_BANK_IFSC.toString()));
					} else {
						eCollectionList.setPayeeBankIFSC(CrmFieldConstants.NA.getValue());
					}
					if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQ_ID.toString()))) {
						eCollectionList.setBankTxnNumber(dbobj.getString(FieldType.ACQ_ID.toString()));
					} else {
						eCollectionList.setBankTxnNumber(CrmFieldConstants.NA.getValue());
					}
					eCollectionList.setTotalAmount(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()));
					eCollectionList.setAmount(dbobj.getString(FieldType.AMOUNT.toString()));
					eCollectionList.setStatus(dbobj.getString(FieldType.STATUS.toString()));
					if (StringUtils.isNotBlank(dbobj.getString(FieldType.SENDER_REMARK.toString()))) {
						eCollectionList.setSenderRemark(dbobj.getString(FieldType.SENDER_REMARK.toString()));
					} else {
						eCollectionList.setSenderRemark(CrmFieldConstants.NA.getValue());
					}

					if (StringUtils.isNotBlank(dbobj.getString(FieldType.FIXED_CHARGES.toString())))
						eCollectionList.setFixedCharges(dbobj.getString(FieldType.FIXED_CHARGES.toString()));
					else
						eCollectionList.setFixedCharges("0.00");

					if (StringUtils.isNotBlank(dbobj.getString(FieldType.PERCENTAGE_CHARGES.toString())))
						eCollectionList.setPercentageCharges(dbobj.getString(FieldType.PERCENTAGE_CHARGES.toString()));
					else
						eCollectionList.setPercentageCharges("0.00");

					if (StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.toString())))
						eCollectionList.setPgGst(dbobj.getString(FieldType.PG_GST.toString()));
					else
						eCollectionList.setPgGst("0.00");

					eCollectionList.setTotalPaCommission(String.valueOf(new BigDecimal(eCollectionList.getPgGst())
							.add(new BigDecimal(eCollectionList.getPercentageCharges())
									.add(new BigDecimal(eCollectionList.getFixedCharges())))));
					eCollectionDataList.add(eCollectionList);
				}
			}
			logger.info("eCollectionDataList created and size = " + eCollectionDataList.size());
			cursor.close();
			return eCollectionDataList;
		} catch (Exception e) {
			logger.error("Exception occured in TxnReports , E_Collection , Exception = ", e);
			return eCollectionDataList;
		}
	}

	public List<ECollectionObject> eCollectionForDownload(String merchantPayId, String subMerchantPayId,
			String ReportPaymentMode, String reportStatus, String reportTxnType, String ReportDateFrom,
			String ReportDateTo, User user) {
		logger.info("Inside TxnReports , eCollectionForDownload()");
		Map<String, User> userMap = new HashMap<String, User>();

		boolean isParameterised = false;
		List<ECollectionObject> eCollectionDataList = new ArrayList<ECollectionObject>();
		try {
			List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();
			BasicDBObject dateQuery = new BasicDBObject();
			BasicDBObject allParamQuery = new BasicDBObject();

			String currentDate = null;
			if (!ReportDateFrom.isEmpty()) {
				if (!ReportDateTo.isEmpty()) {
					currentDate = ReportDateTo;
				} else {
					DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					Calendar cal = Calendar.getInstance();
					currentDate = dateFormat.format(cal.getTime());
				}
				dateQuery.put(FieldType.CREATE_DATE.getName(),
						BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(ReportDateFrom).toLocalizedPattern())
								.add("$lte", new SimpleDateFormat(currentDate).toLocalizedPattern()).get());
			}
			if (!merchantPayId.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.PAY_ID.getName(), merchantPayId));

				isParameterised = true;
			}
			if (StringUtils.isNotBlank(subMerchantPayId) && !subMerchantPayId.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.SUB_MERCHANT_ID.getName(), subMerchantPayId));
				isParameterised = true;
			}
			if (user.getUserType().equals(UserType.RESELLER)) {
				paramConditionLst.add(new BasicDBObject(FieldType.RESELLER_ID.getName(), user.getResellerId()));
			}
			if (!ReportPaymentMode.equalsIgnoreCase("ALL")) {
				isParameterised = true;
				paramConditionLst.add(new BasicDBObject(FieldType.MOP_TYPE.getName(), ReportPaymentMode));
			} else {
			}

			if (!reportStatus.equalsIgnoreCase("ALL")) {
				isParameterised = true;
				paramConditionLst.add(new BasicDBObject(FieldType.STATUS.getName(), reportStatus));
			} else {
			}

			if (!reportTxnType.equalsIgnoreCase("ALL")) {
				isParameterised = true;
				paramConditionLst.add(new BasicDBObject(FieldType.TXNTYPE.getName(), reportTxnType));
			}

			if (!paramConditionLst.isEmpty()) {
				allParamQuery = new BasicDBObject("$and", paramConditionLst);
			}
			List<BasicDBObject> allConditionQueryList = new ArrayList<BasicDBObject>();
			if (!dateQuery.isEmpty()) {
				allConditionQueryList.add(dateQuery);
			}
			BasicDBObject allConditionQueryObj = new BasicDBObject("$and", allConditionQueryList);
			List<BasicDBObject> fianlList = new ArrayList<BasicDBObject>();

			if (!allParamQuery.isEmpty()) {
				fianlList.add(allParamQuery);
			}

			if (!allConditionQueryObj.isEmpty()) {
				fianlList.add(allConditionQueryObj);
			}
			BasicDBObject finalquery = new BasicDBObject("$and", fianlList);

			logger.info("Inside TxnReports , eCollection, finalquery = " + finalquery);
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns
					.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.E_COLLECTION.getValue()));
			BasicDBObject match = new BasicDBObject("$match", finalquery);
			BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject("CREATE_DATE", -1));

			List<BasicDBObject> pipeline = Arrays.asList(match, sort/* , group */);

			AggregateIterable<Document> output = coll.aggregate(pipeline);
			output.allowDiskUse(true);
			MongoCursor<Document> cursor = output.iterator();

			while (cursor.hasNext()) {
				Document dbobj = cursor.next();
				if (StringUtils.isNotBlank(dbobj.getString("IS_ENCRYPTED"))
						&& dbobj.getString("IS_ENCRYPTED").equalsIgnoreCase("Y")) {
					dbobj = dataEncDecTool.decryptDocument(dbobj);
				}

				ECollectionObject eCollectionList = new ECollectionObject();

				eCollectionList.setMerchant(dbobj.getString(CrmFieldType.BUSINESS_NAME.getName()));

				User user1 = new User();

				if (StringUtils.isNotBlank((String) dbobj.get(FieldType.PAY_ID.getName()))) {
					String payid = (String) dbobj.get(FieldType.PAY_ID.getName());

					if (userMap.get(payid) != null && !userMap.get(payid).getPayId().isEmpty()) {
						user1 = userMap.get(payid);
					} else {
						user1 = userdao.findPayId(payid);
						userMap.put(payid, user1);
					}
				}

				// String payid = (String)
				// dbobj.get(FieldType.PAY_ID.getName());
				// User user1 = new User();
				// if (userMap.get(payid) != null &&
				// !userMap.get(payid).getPayId().isEmpty()) {
				// user1 = userMap.get(payid);
				// } else {
				// user1 = userdao.findPayId(payid);
				// userMap.put(payid, user1);
				// }
				if (user1 != null) {
					eCollectionList.setMerchant(user1.getBusinessName());
				}

				// if (!merchantPayId.equalsIgnoreCase("All") &&
				if (dbobj.containsKey(FieldType.SUB_MERCHANT_ID.getName())
						&& StringUtils.isNotBlank(dbobj.getString(FieldType.SUB_MERCHANT_ID.getName()))) {

					String subMerchant = dbobj.getString(FieldType.SUB_MERCHANT_ID.getName());
					User subMerchantUser = new User();

					if (userMap.get(subMerchant) != null && !userMap.get(subMerchant).getPayId().isEmpty()) {
						subMerchantUser = userMap.get(subMerchant);
					} else {
						subMerchantUser = userdao.findPayId(subMerchant);
						userMap.put(subMerchant, subMerchantUser);
					}
					if (subMerchantUser != null) {
						eCollectionList.setSubMerchant(subMerchantUser.getBusinessName());
					} else {
						eCollectionList.setSubMerchant(CrmFieldConstants.NA.getValue());
					}
				} else {
					if (merchantPayId.equalsIgnoreCase("ALL")) {
						eCollectionList.setSubMerchant(CrmFieldConstants.NA.getValue());
					}
				}
				if (user.getUserType().equals(UserType.ADMIN) || user.getUserType().equals(UserType.SUBADMIN)) {
					eCollectionList.setPayId(dbobj.getString(FieldType.PAY_ID.toString()));
					eCollectionList
							.setMerchantVirtualAccountNumber(dbobj.getString(FieldType.VIRTUAL_AC_CODE.toString()));
					eCollectionList.setTransactionDate(dbobj.getString(FieldType.CREATE_DATE.toString()));
					if (StringUtils.isNotBlank(dbobj.getString(FieldType.CUSTOMER_CODE.toString()))) {
						eCollectionList.setpaymentGatewayCode(dbobj.getString(FieldType.CUSTOMER_CODE.toString()));
					} else {
						eCollectionList.setpaymentGatewayCode(CrmFieldConstants.NA.getValue());
					}
					if (StringUtils.isNotBlank(dbobj.getString(FieldType.CUSTOMER_ACCOUNT_NO.toString()))) {
						eCollectionList.setpaymentGatewayAccountNumber(
								dbobj.getString(FieldType.CUSTOMER_ACCOUNT_NO.toString()));
					} else {
						eCollectionList.setpaymentGatewayAccountNumber(CrmFieldConstants.NA.getValue());
					}
					eCollectionList.setPaymentMode(dbobj.getString(FieldType.PAYMENT_TYPE.toString()));
					if (StringUtils.isNotBlank(dbobj.getString(FieldType.TXNTYPE.toString()))) {
						if (dbobj.getString(FieldType.TXNTYPE.toString()).equalsIgnoreCase("COLLECTION")) {
							eCollectionList.setTxnType("ECOLLECTION");
						}
						if (dbobj.getString(FieldType.TXNTYPE.toString()).equalsIgnoreCase("Topup")) {
							eCollectionList.setTxnType("Topup");
						}
					} else {
						eCollectionList.setTxnType(CrmFieldConstants.NA.getValue());
					}
					if (StringUtils.isNotBlank(dbobj.getString(FieldType.PAYEE_NAME.toString()))) {
						eCollectionList.setPayeeName(dbobj.getString(FieldType.PAYEE_NAME.toString()));
					} else {
						eCollectionList.setPayeeName(CrmFieldConstants.NA.getValue());
					}
					if (StringUtils.isNotBlank(dbobj.getString(FieldType.PAYEE_ACCOUNT_NUMBER.toString()))) {
						eCollectionList
								.setPayeeAccountNumber(dbobj.getString(FieldType.PAYEE_ACCOUNT_NUMBER.toString()));
					} else {
						eCollectionList.setPayeeAccountNumber(CrmFieldConstants.NA.getValue());
					}
					if (StringUtils.isNotBlank(dbobj.getString(FieldType.PAYEE_BANK_IFSC.toString()))) {
						eCollectionList.setPayeeBankIFSC(dbobj.getString(FieldType.PAYEE_BANK_IFSC.toString()));
					} else {
						eCollectionList.setPayeeBankIFSC(CrmFieldConstants.NA.getValue());
					}
					if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQ_ID.toString()))) {
						eCollectionList.setBankTxnNumber(dbobj.getString(FieldType.ACQ_ID.toString()));
					} else {
						eCollectionList.setBankTxnNumber(CrmFieldConstants.NA.getValue());
					}
					eCollectionList.setTotalAmount(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()));
					eCollectionList.setAmount(dbobj.getString(FieldType.AMOUNT.toString()));
					eCollectionList.setStatus(dbobj.getString(FieldType.STATUS.toString()));
					if (StringUtils.isNotBlank(dbobj.getString(FieldType.SENDER_REMARK.toString()))) {
						eCollectionList.setSenderRemark(dbobj.getString(FieldType.SENDER_REMARK.toString()));
					} else {
						eCollectionList.setSenderRemark(CrmFieldConstants.NA.getValue());
					}

					if (StringUtils.isNotBlank(dbobj.getString(FieldType.FIXED_CHARGES.toString())))
						eCollectionList.setFixedCharges(dbobj.getString(FieldType.FIXED_CHARGES.toString()));
					else
						eCollectionList.setFixedCharges("0.00");

					if (StringUtils.isNotBlank(dbobj.getString(FieldType.PERCENTAGE_CHARGES.toString())))
						eCollectionList.setPercentageCharges(dbobj.getString(FieldType.PERCENTAGE_CHARGES.toString()));
					else
						eCollectionList.setPercentageCharges("0.00");

					if (StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.toString())))
						eCollectionList.setPgGst(dbobj.getString(FieldType.PG_GST.toString()));
					else
						eCollectionList.setPgGst("0.00");

					eCollectionList.setTotalPaCommission(String.valueOf(new BigDecimal(eCollectionList.getPgGst())
							.add(new BigDecimal(eCollectionList.getPercentageCharges())
									.add(new BigDecimal(eCollectionList.getFixedCharges())))));

					eCollectionDataList.add(eCollectionList);
				} else {
					eCollectionList.setPayId(dbobj.getString(FieldType.PAY_ID.toString()));
					eCollectionList
							.setMerchantVirtualAccountNumber(dbobj.getString(FieldType.VIRTUAL_AC_CODE.toString()));
					eCollectionList.setTransactionDate(dbobj.getString(FieldType.CREATE_DATE.toString()));
					eCollectionList.setPaymentMode(dbobj.getString(FieldType.PAYMENT_TYPE.toString()));
					if (StringUtils.isNotBlank(dbobj.getString(FieldType.TXNTYPE.toString()))) {
						if (dbobj.getString(FieldType.TXNTYPE.toString()).equalsIgnoreCase("COLLECTION")) {
							eCollectionList.setTxnType("ECOLLECTION");
						}
						if (dbobj.getString(FieldType.TXNTYPE.toString()).equalsIgnoreCase("Topup")) {
							eCollectionList.setTxnType("Topup");
						}
					} else {
						eCollectionList.setTxnType(CrmFieldConstants.NA.getValue());
					}
					if (StringUtils.isNotBlank(dbobj.getString(FieldType.PAYEE_NAME.toString()))) {
						eCollectionList.setPayeeName(dbobj.getString(FieldType.PAYEE_NAME.toString()));
					} else {
						eCollectionList.setPayeeName(CrmFieldConstants.NA.getValue());
					}
					if (StringUtils.isNotBlank(dbobj.getString(FieldType.PAYEE_ACCOUNT_NUMBER.toString()))) {
						eCollectionList
								.setPayeeAccountNumber(dbobj.getString(FieldType.PAYEE_ACCOUNT_NUMBER.toString()));
					} else {
						eCollectionList.setPayeeAccountNumber(CrmFieldConstants.NA.getValue());
					}
					if (StringUtils.isNotBlank(dbobj.getString(FieldType.PAYEE_BANK_IFSC.toString()))) {
						eCollectionList.setPayeeBankIFSC(dbobj.getString(FieldType.PAYEE_BANK_IFSC.toString()));
					} else {
						eCollectionList.setPayeeBankIFSC(CrmFieldConstants.NA.getValue());
					}
					if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQ_ID.toString()))) {
						eCollectionList.setBankTxnNumber(dbobj.getString(FieldType.ACQ_ID.toString()));
					} else {
						eCollectionList.setBankTxnNumber(CrmFieldConstants.NA.getValue());
					}
					eCollectionList.setTotalAmount(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()));
					eCollectionList.setAmount(dbobj.getString(FieldType.AMOUNT.toString()));
					eCollectionList.setStatus(dbobj.getString(FieldType.STATUS.toString()));
					if (StringUtils.isNotBlank(dbobj.getString(FieldType.SENDER_REMARK.toString()))) {
						eCollectionList.setSenderRemark(dbobj.getString(FieldType.SENDER_REMARK.toString()));
					} else {
						eCollectionList.setSenderRemark(CrmFieldConstants.NA.getValue());
					}
					if (StringUtils.isNotBlank(dbobj.getString(FieldType.FIXED_CHARGES.toString())))
						eCollectionList.setFixedCharges(dbobj.getString(FieldType.FIXED_CHARGES.toString()));
					else
						eCollectionList.setFixedCharges("0.00");

					if (StringUtils.isNotBlank(dbobj.getString(FieldType.PERCENTAGE_CHARGES.toString())))
						eCollectionList.setPercentageCharges(dbobj.getString(FieldType.PERCENTAGE_CHARGES.toString()));
					else
						eCollectionList.setPercentageCharges("0.00");

					if (StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.toString())))
						eCollectionList.setPgGst(dbobj.getString(FieldType.PG_GST.toString()));
					else
						eCollectionList.setPgGst("0.00");

					eCollectionList.setTotalPaCommission(String.valueOf(new BigDecimal(eCollectionList.getPgGst())
							.add(new BigDecimal(eCollectionList.getPercentageCharges())
									.add(new BigDecimal(eCollectionList.getFixedCharges())))));
					eCollectionDataList.add(eCollectionList);
				}
			}
			logger.info("eCollectionDataList created and size = " + eCollectionDataList.size());
			cursor.close();
			return eCollectionDataList;
		} catch (Exception e) {
			logger.error("Exception occured in TxnReports , E_Collection , Exception = ", e);
			return eCollectionDataList;
		}
	}

	@SuppressWarnings({ "unused", "static-access" })
	public List<VendorPayouts> getVendorPayoutViewTransactions(String orderId, String skuCode, String categoryCode,
			String merchant, String paymentMethod, String currency, String txntype, String date, int start, int length,
			Set<String> pgRefVendorIdSet, User sessionUser) {

		logger.info("inside txn Reports, getVendorPayoutViewTransactions function");
		Map<String, User> userMap = new HashMap<String, User>();
		Map<String, MerchantProcessingApplication> mpaMap = new HashMap<String, MerchantProcessingApplication>();
		boolean isParameterised = false;
		try {
			List<VendorPayouts> vendorPayoutList = new ArrayList<VendorPayouts>();
			List<String> txnTypeList = new ArrayList<String>();
			if (StringUtils.isBlank(txntype) || txntype.equalsIgnoreCase(CrmFieldConstants.ALL.getValue())) {
				txnTypeList.add(TransactionType.SALE.getName());
				txnTypeList.add(TransactionType.REFUND.getName());
				txnTypeList.add(TransactionType.REFUNDRECO.getName());
				txnTypeList.add(TransactionType.RECO.getName());
			} else {
				txnTypeList.add(txntype);
			}

			for (String pgRefVendor : pgRefVendorIdSet) {
				for (String txnType : txnTypeList) {
					String pgRefVendorArray[] = pgRefVendor.split("-");
					String pgRefNum = pgRefVendorArray[0];
					String vendor = pgRefVendorArray[1];
					List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();
					BasicDBObject allParamQuery = new BasicDBObject();

					paramConditionLst.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.SETTLED.getName()));

					if (StringUtils.isNotBlank(merchant)) {
						paramConditionLst.add(new BasicDBObject(FieldType.PAY_ID.getName(), merchant));
						isParameterised = true;
					}

					if (StringUtils.isNotBlank(pgRefNum) && !pgRefNum.isEmpty()) {
						isParameterised = true;
						paramConditionLst.add(new BasicDBObject(FieldType.PG_REF_NUM.getName(), pgRefNum));
					}

					if (StringUtils.isNotBlank(orderId) && !orderId.isEmpty()) {
						isParameterised = true;
						paramConditionLst.add(new BasicDBObject(FieldType.ORDER_ID.getName(), orderId));
					}

					if (StringUtils.isNotBlank(skuCode) && !skuCode.isEmpty()) {
						isParameterised = true;
						paramConditionLst.add(new BasicDBObject(FieldType.SKU_CODE.getName(),
								Pattern.compile(Pattern.quote(skuCode))));
					}

					if (StringUtils.isNotBlank(categoryCode) && !categoryCode.isEmpty()) {
						isParameterised = true;
						paramConditionLst.add(new BasicDBObject(FieldType.CATEGORY_CODE.getName(),
								Pattern.compile(Pattern.quote(categoryCode))));
					}

					if (StringUtils.isNotBlank(currency) && !currency.equalsIgnoreCase("ALL")) {
						isParameterised = true;
						paramConditionLst.add(new BasicDBObject(FieldType.CURRENCY_CODE.getName(), currency));
					}

					if (StringUtils.isNotBlank(paymentMethod) && !paymentMethod.equalsIgnoreCase("ALL")) {
						isParameterised = true;
						paramConditionLst.add(new BasicDBObject(FieldType.PAYMENT_TYPE.getName(), paymentMethod));
					}

					if (StringUtils.isNotBlank(txnType)
							&& !txnType.equalsIgnoreCase(CrmFieldConstants.ALL.getValue())) {
						isParameterised = true;
						paramConditionLst.add(new BasicDBObject(FieldType.TXNTYPE.getName(), txnType));
					}

					BasicDBObject dateQuery = new BasicDBObject();
					dateQuery.put(FieldType.CREATE_DATE.getName(),
							BasicDBObjectBuilder
									.start("$gte", new SimpleDateFormat(date + " 00:00:00").toLocalizedPattern())
									.add("$lte", new SimpleDateFormat(date + " 23:59:59").toLocalizedPattern()).get());

					if (!dateQuery.isEmpty()) {
						paramConditionLst.add(dateQuery);
					}

					if (!paramConditionLst.isEmpty()) {
						allParamQuery = new BasicDBObject("$and", paramConditionLst);
					}

					List<BasicDBObject> allConditionQueryList = new ArrayList<BasicDBObject>();

					List<BasicDBObject> fianlList = new ArrayList<BasicDBObject>();

					if (!allParamQuery.isEmpty()) {
						fianlList.add(allParamQuery);
					}

					if (!allConditionQueryList.isEmpty()) {
						BasicDBObject allConditionQueryObj = new BasicDBObject("$and", allConditionQueryList);
						if (!allConditionQueryObj.isEmpty()) {
							fianlList.add(allConditionQueryObj);
						}
					}

					BasicDBObject finalQuery = new BasicDBObject("$and", fianlList);

					logger.info("Inside TxnReports , getVendorPayoutViewTransactions function , finalQuery = "
							+ finalQuery);
					MongoDatabase dbIns = mongoInstance.getDB();
					MongoCollection<Document> coll = dbIns.getCollection(
							propertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));
					BasicDBObject match = new BasicDBObject("$match", finalQuery);
					BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject("CREATE_DATE", -1));
					List<BasicDBObject> pipeline;

					pipeline = Arrays.asList(match, sort);

					AggregateIterable<Document> output = coll.aggregate(pipeline);
					output.allowDiskUse(true);
					MongoCursor<Document> cursor = output.iterator();

					while (cursor.hasNext()) {

						Document dbObj = cursor.next();

						if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
								&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
							dbObj = dataEncDecTool.decryptDocument(dbObj);
						}

						VendorPayouts vendorPayout = new VendorPayouts();

						MerchantProcessingApplication mpa = new MerchantProcessingApplication();
						User user1 = new User();

						if (StringUtils.isNotBlank((String) dbObj.get(FieldType.PAY_ID.getName()))) {
							String payid = (String) dbObj.get(FieldType.PAY_ID.getName());

							if (userMap.get(payid) != null && !userMap.get(payid).getPayId().isEmpty()) {
								user1 = userMap.get(payid);
							} else {
								user1 = userdao.findPayId(payid);
								userMap.put(payid, user1);
							}

							// String payId = (String)
							// dbObj.get(FieldType.PAY_ID.getName());
							// User user1 = new User();
							//
							// if (userMap.get(payId) != null &&
							// !userMap.get(payId).getPayId().isEmpty()) {
							// user1 = userMap.get(payId);
							// } else {
							// user1 = userdao.findPayId(payId);
							// userMap.put(payId, user1);
							// }

							if (mpaMap.get(payid) != null && !mpaMap.get(payid).getPayId().isEmpty()) {
								mpa = mpaMap.get(payid);
							} else {
								mpa = mpaDao.fetchMPADataByPayId(payid);
								mpaMap.put(payid, mpa);
							}
							vendorPayout.setMerchant(user1.getBusinessName());
						}
						String vendorid;
						if (dbObj.containsKey(FieldType.SUB_MERCHANT_ID.getName())
								&& StringUtils.isNotBlank(dbObj.getString(FieldType.SUB_MERCHANT_ID.getName()))) {
							vendorid = dbObj.getString(FieldType.SUB_MERCHANT_ID.getName());
						} else {
							vendorid = vendor;
						}
						if (StringUtils.isNotBlank(vendorid)) {
							User user2 = new User();

							if (userMap.get(vendorid) != null && !userMap.get(vendorid).getPayId().isEmpty()) {
								user2 = userMap.get(vendorid);
							} else {
								user2 = userdao.findPayId(vendorid);
								userMap.put(vendorid, user2);
							}
							if (user2 != null) {

								String name = "";
								if (StringUtils.isBlank(user2.getBusinessName())) {
									name = user2.getFirstName() + " " + user2.getLastName();
								} else {
									name = user2.getBusinessName();
								}

								vendorPayout.setVendorName(name);
								vendorPayout.setVendorPayId(user2.getPayId());
							} else {
								vendorPayout.setVendorName(CrmFieldConstants.NA.getValue());
								vendorPayout.setVendorPayId(CrmFieldConstants.NA.getValue());
							}
						} else {
							vendorPayout.setVendorName(CrmFieldConstants.NA.getValue());
							vendorPayout.setVendorPayId(CrmFieldConstants.NA.getValue());
						}
						vendorPayout.setTxnType(dbObj.getString(FieldType.ORIG_TXNTYPE.getName()));
						vendorPayout.setTxnId(dbObj.getString(FieldType.TXN_ID.getName()));
						vendorPayout.setPgRefNum(dbObj.getString(FieldType.PG_REF_NUM.getName()));
						vendorPayout.setOrderId(dbObj.getString(FieldType.ORDER_ID.getName()));

						if (StringUtils.isNotBlank(dbObj.getString(FieldType.SKU_CODE.getName()))) {
							vendorPayout.setSkuCode(dbObj.getString(FieldType.SKU_CODE.getName()));
						} else {
							vendorPayout.setSkuCode("NA");
						}

						if (StringUtils.isNotBlank(dbObj.getString(FieldType.CATEGORY_CODE.getName()))) {
							vendorPayout.setCategoryCode(dbObj.getString(FieldType.CATEGORY_CODE.getName()));
						} else {
							vendorPayout.setCategoryCode("NA");
						}

						if (StringUtils.isNotBlank(mpa.getPaymentCycle())) {
							vendorPayout.setPaymentCycle(mpa.getPaymentCycle());
						}

						vendorPayout.setDate(dbObj.getString(FieldType.CREATE_DATE.getName()));
						vendorPayout.setPaymentMethod(dbObj.getString(FieldType.PAYMENT_TYPE.getName()));

						if (StringUtils.isNotBlank(dbObj.getString(FieldType.PAYMENTS_REGION.getName()))) {
							vendorPayout.setPaymentRegion(dbObj.getString(FieldType.PAYMENTS_REGION.getName()));
						} else {
							vendorPayout.setPaymentRegion(CrmFieldConstants.NA.getValue());
						}

						if (dbObj.containsKey(FieldType.CARD_HOLDER_TYPE.getName())
								&& StringUtils.isNotBlank(dbObj.getString(FieldType.CARD_HOLDER_TYPE.getName()))) {
							vendorPayout.setCardHolderType(dbObj.getString(FieldType.CARD_HOLDER_TYPE.getName()));
						} else {
							vendorPayout.setCardHolderType(CrmFieldConstants.NA.getValue());
						}

						if (StringUtils.isNotBlank(dbObj.getString(FieldType.CARD_MASK.getName()))) {
							vendorPayout.setCardMask(dbObj.getString(FieldType.CARD_MASK.getName()));
						} else if (StringUtils.isNotBlank(dbObj.getString(FieldType.PAYER_ADDRESS.getName()))) {

							if (sessionUser.getUserType().equals(UserType.ADMIN)
									|| sessionUser.getUserType().equals(UserType.SUBADMIN)) {
								vendorPayout.setCardMask(dbObj.getString(FieldType.PAYER_ADDRESS.getName()));
							} else {
								String vpaString = dbObj.getString(FieldType.PAYER_ADDRESS.getName());
								String[] vpaArray = vpaString.split("@");
								char[] vpaChar = vpaArray[0].toCharArray();
								StringBuilder vpastrBuilder = new StringBuilder();

								if (vpaChar.length > 3) {
									for (int i = 0; i < vpaChar.length - 3; i++) {
										vpastrBuilder.append(vpaChar[i]);
									}
									vpastrBuilder.append("***@");
									vpastrBuilder.append(vpaArray[1]);
								} else {
									vpastrBuilder.append(vpaChar[0]);
									vpastrBuilder.append("**@");
									vpastrBuilder.append(vpaArray[1]);
								}

								vendorPayout.setCardMask(vpastrBuilder.toString());
							}
						} else {
							vendorPayout.setCardMask(CrmFieldConstants.NA.getValue());
						}

						vendorPayout.setBaseAmount(dbObj.getString(FieldType.AMOUNT.getName()));
						vendorPayout.setTotalAmount(dbObj.getString(FieldType.TOTAL_AMOUNT.getName()));
						vendorPayout.setVendorPayoutDate(date);

						/*
						 * if (StringUtils.isNotBlank(mpa.getPaymentCycle())) {
						 * vendorPayout.setPaymentCycle(mpa.getPaymentCycle()); } else {
						 * vendorPayout.setPaymentCycle(CrmFieldConstants.NA. getValue()); }
						 */

						if (dbObj.containsKey(FieldType.CUST_NAME.getName())
								&& StringUtils.isNotBlank(FieldType.CUST_NAME.getName())) {
							vendorPayout.setCustName(dbObj.getString(FieldType.CUST_NAME.getName()));
						} else {
							vendorPayout.setCustName(CrmFieldConstants.NA.getValue());
						}

						if (dbObj.containsKey(FieldType.CUST_PHONE.getName())
								&& StringUtils.isNotBlank(FieldType.CUST_PHONE.getName())) {
							vendorPayout.setCustMobile(dbObj.getString(FieldType.CUST_PHONE.getName()));
						} else {
							vendorPayout.setCustMobile(CrmFieldConstants.NA.getValue());
						}

						if (dbObj.containsKey(FieldType.CUST_EMAIL.getName())
								&& StringUtils.isNotBlank(FieldType.CUST_EMAIL.getName())) {
							vendorPayout.setCustEmail(dbObj.getString(FieldType.CUST_EMAIL.getName()));
						} else {
							vendorPayout.setCustEmail(CrmFieldConstants.NA.getValue());
						}

						if (StringUtils.isNotBlank(dbObj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
								&& (StringUtils.isNotBlank(dbObj.getString(FieldType.PG_TDR_SC.toString())))) {

							vendorPayout.setTdrSurcharge(String.format("%.2f",
									(Double.parseDouble(dbObj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
											+ Double.parseDouble(dbObj.getString(FieldType.PG_TDR_SC.toString())))));

						} else {
							vendorPayout.setTdrSurcharge(CrmFieldConstants.NA.getValue());
						}

						if (StringUtils.isNotBlank(dbObj.getString(FieldType.ACQUIRER_GST.toString()))
								&& (StringUtils.isNotBlank(dbObj.getString(FieldType.PG_GST.toString())))) {
							vendorPayout.setGst(String.format("%.2f",
									(Double.parseDouble(dbObj.getString(FieldType.ACQUIRER_GST.toString()))
											+ Double.parseDouble(dbObj.getString(FieldType.PG_GST.toString())))));

						} else {
							vendorPayout.setGst(CrmFieldConstants.NA.getValue());
						}

						if (StringUtils.isNotBlank(dbObj.getString(FieldType.TOTAL_AMOUNT.toString()))
								&& StringUtils.isNotBlank(dbObj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
								&& StringUtils.isNotBlank(dbObj.getString(FieldType.ACQUIRER_GST.toString()))
								&& StringUtils.isNotBlank(dbObj.getString(FieldType.PG_TDR_SC.toString()))
								&& StringUtils.isNotBlank(dbObj.getString(FieldType.PG_GST.toString()))) {

							vendorPayout.setMerchantAmount(String.format("%.2f",
									Double.parseDouble(dbObj.getString(FieldType.TOTAL_AMOUNT.toString())) - (Double
											.parseDouble(dbObj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
											+ Double.parseDouble(dbObj.getString(FieldType.ACQUIRER_GST.toString()))
											+ Double.parseDouble(dbObj.getString(FieldType.PG_TDR_SC.toString()))
											+ Double.parseDouble(dbObj.getString(FieldType.PG_GST.toString())))));
						} else {
							vendorPayout.setMerchantAmount(CrmFieldConstants.NA.getValue());
						}
						vendorPayoutList.add(vendorPayout);
					}
					cursor.close();
				}
			}
			logger.info("Inside TxnReports , getVendorPayoutViewTransactions , vendorPayoutListSize = "
					+ vendorPayoutList.size());
			return vendorPayoutList;
		} catch (Exception ex) {
			logger.error("exception caugth in getVendorPayoutViewTransactions function ", ex);
			return null;
		}
	}

	@SuppressWarnings({ "unused", "static-access" })

	public Object[] countVendorPayoutTransactions(String pgRefNum, String orderId, String skuCode, String categoryCode,
			String merchant, List<MerchantProcessingApplication> vendorlist, String paymentMethod, String currency,
			String txnType, String date, int start, int length, List<Merchants> vendorUserList) {

		logger.info("inside txn Reports, countVendorPayoutTransactions function !!");
		boolean isParameterised = false;
		int total = 0;

		try {
			Set<String> pgRefVendorIdSet = new HashSet<String>();
			for (Merchants vendor : vendorUserList) {
				String vendorId = vendor.getPayId();
				List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();

				BasicDBObject allParamQuery = new BasicDBObject();

				// paramConditionLst.add(new
				// BasicDBObject(FieldType.STATUS.getName(),
				// StatusType.SETTLED.getName()));

				if (StringUtils.isNotBlank(merchant) && (!merchant.equalsIgnoreCase("ALL"))) {
					paramConditionLst.add(new BasicDBObject(FieldType.PAY_ID.getName(), merchant));
					isParameterised = true;
				}

				if (StringUtils.isNotBlank(pgRefNum) && (!pgRefNum.equalsIgnoreCase("ALL"))) {
					isParameterised = true;
					paramConditionLst.add(new BasicDBObject(FieldType.PG_REF_NUM.getName(), pgRefNum));
				}

				if (StringUtils.isNotBlank(orderId) && (!orderId.equalsIgnoreCase("ALL"))) {
					isParameterised = true;
					paramConditionLst.add(new BasicDBObject(FieldType.ORDER_ID.getName(), orderId));
				}

				if (StringUtils.isNotBlank(skuCode) && (!skuCode.equalsIgnoreCase("ALL"))) {
					isParameterised = true;
					paramConditionLst.add(
							new BasicDBObject(FieldType.SKU_CODE.getName(), Pattern.compile(Pattern.quote(skuCode))));
				}

				if (StringUtils.isNotBlank(categoryCode) && (!categoryCode.equalsIgnoreCase("ALL"))) {
					isParameterised = true;
					paramConditionLst.add(new BasicDBObject(FieldType.CATEGORY_CODE.getName(),
							Pattern.compile(Pattern.quote(categoryCode))));
				}

				if (StringUtils.isNotBlank(currency) && !currency.equalsIgnoreCase("ALL")) {
					isParameterised = true;
					paramConditionLst.add(new BasicDBObject(FieldType.CURRENCY_CODE.getName(), currency));
				}

				if (StringUtils.isNotBlank(paymentMethod) && !paymentMethod.equalsIgnoreCase("ALL")) {
					isParameterised = true;
					paramConditionLst.add(new BasicDBObject(FieldType.PAYMENT_TYPE.getName(), paymentMethod));
				}

				if (StringUtils.isNotBlank(txnType) && !txnType.equalsIgnoreCase(CrmFieldConstants.ALL.getValue())) {
					isParameterised = true;
					paramConditionLst.add(new BasicDBObject(FieldType.TXNTYPE.getName(), txnType));
				}

				if (StringUtils.isNotBlank(vendorId) && !vendorId.equalsIgnoreCase("ALL")) {
					isParameterised = true;
					paramConditionLst.add(new BasicDBObject("VENDOR_ID", vendorId));
				}

				BasicDBObject dateQuery = new BasicDBObject();
				dateQuery.put("SETTLEMENT_DATE",
						BasicDBObjectBuilder
								.start("$gte", new SimpleDateFormat(date + " 00:00:00").toLocalizedPattern())
								.add("$lte", new SimpleDateFormat(date + " 23:59:59").toLocalizedPattern()).get());

				if (!dateQuery.isEmpty()) {
					paramConditionLst.add(dateQuery);
				}

				if (!paramConditionLst.isEmpty()) {
					allParamQuery = new BasicDBObject("$and", paramConditionLst);
				}

				List<BasicDBObject> fianlList = new ArrayList<BasicDBObject>();

				if (!allParamQuery.isEmpty()) {
					fianlList.add(allParamQuery);
				}

				BasicDBObject finalQuery = new BasicDBObject("$and", fianlList);

				logger.info("Inside TxnReports , countVendorPayoutTransactions function , finalQuery = " + finalQuery);
				MongoDatabase dbIns = mongoInstance.getDB();
				MongoCollection<Document> coll = dbIns.getCollection(
						propertiesManager.propertiesMap.get(prefix + Constants.PROD_DESC_COLLECTION.getValue()));

				BasicDBObject match = new BasicDBObject("$match", finalQuery);

				List<BasicDBObject> pipeline;

				pipeline = Arrays.asList(match);

				AggregateIterable<Document> output = coll.aggregate(pipeline);
				output.allowDiskUse(true);
				MongoCursor<Document> cursor = output.iterator();

				while (cursor.hasNext()) {
					Document dbobj = cursor.next();
					pgRefVendorIdSet.add(dbobj.getString(FieldType.PG_REF_NUM.toString()) + "-"
							+ dbobj.getString(FieldType.VENDOR_ID.toString()));
				}
			}
			total = pgRefVendorIdSet.size();
			logger.info("Inside vendor Payout transaction count , total records from DB  = " + total);
			Object[] obj = new Object[2];
			obj[0] = total;
			obj[1] = pgRefVendorIdSet;
			return obj;
		} catch (Exception ex) {
			logger.error("exception caugth in countVendorPayoutTransactions function ", ex);
			return null;
		}
	}

	@SuppressWarnings("static-access")
	public List<VendorPayouts> billingDetailsForVendorPayoutTransactions(List<VendorPayouts> vendorPayoutList) {
		logger.info("inside the billingDetailsForVendorPayoutTransactions for vendor Payout");
		try {

			if (!vendorPayoutList.isEmpty()) {
				for (VendorPayouts list : vendorPayoutList) {

					BasicDBObject finalquery = new BasicDBObject(FieldType.ORDER_ID.getName(), list.getOrderId());

					logger.info("Inside TxnReports , billingDetailsForVendorPayoutTransactions , finalquery = "
							+ finalquery);
					MongoDatabase dbIns = mongoInstance.getDB();
					MongoCollection<Document> coll = dbIns.getCollection(
							propertiesManager.propertiesMap.get(prefix + Constants.BILLING_COLLECTION.getValue()));

					MongoCursor<Document> cursor = coll.find(finalquery).iterator();

					while (cursor.hasNext()) {
						Document dbObj = cursor.next();

						if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
								&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
							dbObj = dataEncDecTool.decryptDocument(dbObj);
						}

						if (StringUtils.isNotBlank(dbObj.getString(FieldType.CUST_PHONE.getName()))) {
							list.setCustMobile(dbObj.getString(FieldType.CUST_PHONE.getName()));
						} else {
							list.setCustMobile(CrmFieldConstants.NA.getValue());
						}
						cursor.close();
						break;
					}
				}
			}
			return vendorPayoutList;
		} catch (Exception ex) {
			logger.info("Exception caught in TxnReports billingDetailsForVendorPayoutTransactions function ", ex);
		}
		return vendorPayoutList;
	}

	@SuppressWarnings({ "unused", "static-access", "deprecation" })
	public int vendorPayoutReportCount(String merchant, List<MerchantProcessingApplication> vendorlist, String date,
			int start, int length, List<Merchants> vendorUserList, Set<String> orderIdSet) {

		logger.info("inside txnReports, vendorPayoutReportCount function !!");
		boolean isParameterised = false;
		int total = 0;

		try {

			for (MerchantProcessingApplication mpa : vendorlist) {

				String activationDate = null;
				for (Merchants merch : vendorUserList) {
					if (mpa.getPayId().equalsIgnoreCase(merch.getPayId())) {
						activationDate = merch.getRegistrationDate().replace(".0", "");
						break;
					}
				}

				List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();

				BasicDBObject allParamQuery = new BasicDBObject();

				paramConditionLst.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.SETTLED.getName()));

				if (StringUtils.isNotBlank(merchant)) {
					paramConditionLst.add(new BasicDBObject(FieldType.PAY_ID.getName(), merchant));
					isParameterised = true;
				}

				int paymentTurnCount = 0;
				BasicDBObject dateQuery = new BasicDBObject();

				String dateFrom = activationDate;
				String dateTo = date;
				String currentDate = null;

				if (!dateFrom.isEmpty()) {

					if (!dateTo.isEmpty()) {
						currentDate = dateTo;
					} else {
						DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						Calendar cal = Calendar.getInstance();
						currentDate = dateFormat.format(cal.getTime());
					}

					dateQuery.put(FieldType.CREATE_DATE.getName(),
							BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(dateFrom).toLocalizedPattern())
									.add("$lte", new SimpleDateFormat(currentDate).toLocalizedPattern()).get());
				}

				BasicDBObject dateIndexConditionQuery = new BasicDBObject();
				String startString = new SimpleDateFormat(dateFrom).toLocalizedPattern();
				String endString = new SimpleDateFormat(currentDate).toLocalizedPattern();

				DateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.ENGLISH);
				Date dateStart = format.parse(startString);
				Date dateEnd = format.parse(endString);

				LocalDate incrementingDate = dateStart.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
				LocalDate endDate = dateEnd.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

				List<String> allDatesIndex = new ArrayList<>();

				while (!incrementingDate.isAfter(endDate)) {
					allDatesIndex.add(incrementingDate.toString().replaceAll("-", ""));
					incrementingDate = incrementingDate.plusDays(1);
					paymentTurnCount++;
				}
				BasicDBObject dateIndexIn = new BasicDBObject("$in", allDatesIndex);

				if (StringUtils.isNotBlank(mpa.getPaymentCycle())
						&& new BigDecimal(paymentTurnCount).compareTo(new BigDecimal(mpa.getPaymentCycle())) == 0) {

					if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get(Constants.USE_DATE_INDEX.getValue()))
							&& propertiesManager.propertiesMap.get(Constants.USE_DATE_INDEX.getValue())
									.equalsIgnoreCase("Y")) {
						dateIndexConditionQuery.append("DATE_INDEX", dateIndexIn);
					}

					if (StringUtils.isNotBlank(mpa.getPayId())) {
						paramConditionLst.add(new BasicDBObject(FieldType.SUB_MERCHANT_ID.getName(), mpa.getPayId()));
						isParameterised = true;
					}
				} else {
					continue;
				}

				if (!paramConditionLst.isEmpty()) {
					allParamQuery = new BasicDBObject("$and", paramConditionLst);
				}

				List<BasicDBObject> allConditionQueryList = new ArrayList<BasicDBObject>();

				List<BasicDBObject> fianlList = new ArrayList<BasicDBObject>();

				if (!allParamQuery.isEmpty()) {
					fianlList.add(allParamQuery);
				}

				if (!allConditionQueryList.isEmpty()) {
					BasicDBObject allConditionQueryObj = new BasicDBObject("$and", allConditionQueryList);
					if (!allConditionQueryObj.isEmpty()) {
						fianlList.add(allConditionQueryObj);
					}
				}

				BasicDBObject finalQuery = new BasicDBObject("$and", fianlList);

				logger.info("Inside TxnReports , vendorPayoutReportCount function , finalQuery = " + finalQuery);
				MongoDatabase dbIns = mongoInstance.getDB();
				MongoCollection<Document> coll = dbIns.getCollection(
						propertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));
				total = +(int) coll.count(finalQuery);
			}
			logger.info("Inside vendorPayoutReportcount , total records from DB  = " + total);
			return total;
		} catch (Exception ex) {
			logger.error("exception caugth in vendorPayoutReportCount function ", ex);
			return 0;
		}
	}

	@SuppressWarnings({ "unused", "static-access" })
	public List<VendorPayouts> viewVendorPayoutReport(String merchant,
			List<MerchantProcessingApplication> vendorMpalist, String date, int start, int length,
			List<Merchants> vendorUserList, Set<String> orderIdSet) {

		logger.info("inside txn Reports, viewVendorPayoutReport function !!");
		Map<String, User> userMap = new HashMap<String, User>();
		boolean isParameterised = false;
		try {
			List<VendorPayouts> vendorPayoutList = new ArrayList<VendorPayouts>();

			for (MerchantProcessingApplication mpa : vendorMpalist) {
				BigDecimal saleAmount = new BigDecimal("0.00");
				BigDecimal refundAmount = new BigDecimal("0.00");
				String activationDate = null;

				for (Merchants merch : vendorUserList) {
					if (mpa.getPayId().equalsIgnoreCase(merch.getPayId())) {
						activationDate = merch.getRegistrationDate().replace(".0", "");
						break;
					}
				}
				List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();
				BasicDBObject allParamQuery = new BasicDBObject();

				paramConditionLst.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.RECO.getName()));
				paramConditionLst.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.SETTLED.getName()));

				if (StringUtils.isNotBlank(merchant)) {
					paramConditionLst.add(new BasicDBObject(FieldType.PAY_ID.getName(), merchant));
					isParameterised = true;
				}

				int paymentTurnCount = 0;
				BasicDBObject dateQuery = new BasicDBObject();

				String dateFrom = activationDate;
				String dateTo = date;
				String currentDate = null;

				if (!dateFrom.isEmpty()) {

					if (!dateTo.isEmpty()) {
						currentDate = dateTo;
					} else {
						DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						Calendar cal = Calendar.getInstance();
						currentDate = dateFormat.format(cal.getTime());
					}

					dateQuery.put(FieldType.CREATE_DATE.getName(),
							BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(dateFrom).toLocalizedPattern())
									.add("$lte", new SimpleDateFormat(currentDate).toLocalizedPattern()).get());
				}

				BasicDBObject dateIndexConditionQuery = new BasicDBObject();
				String startString = new SimpleDateFormat(dateFrom).toLocalizedPattern();
				String endString = new SimpleDateFormat(currentDate).toLocalizedPattern();

				DateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.ENGLISH);
				Date dateStart = format.parse(startString);
				Date dateEnd = format.parse(endString);

				LocalDate incrementingDate = dateStart.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
				LocalDate endDate = dateEnd.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

				List<String> allDatesIndex = new ArrayList<>();

				while (!incrementingDate.isAfter(endDate)) {
					allDatesIndex.add(incrementingDate.toString().replaceAll("-", ""));
					incrementingDate = incrementingDate.plusDays(1);
					paymentTurnCount++;
				}
				BasicDBObject dateIndexIn = new BasicDBObject("$in", allDatesIndex);

				if (StringUtils.isNotBlank(mpa.getPaymentCycle())
						&& new BigDecimal(paymentTurnCount).compareTo(new BigDecimal(mpa.getPaymentCycle())) == 0) {

					if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get(Constants.USE_DATE_INDEX.getValue()))
							&& propertiesManager.propertiesMap.get(Constants.USE_DATE_INDEX.getValue())
									.equalsIgnoreCase("Y")) {
						dateIndexConditionQuery.append("DATE_INDEX", dateIndexIn);
					}

					BasicDBObject orderIdConditionQuery = null;
					if (orderIdSet != null) {
						List<String> orderIdList = new ArrayList<>(orderIdSet);
						orderIdConditionQuery = new BasicDBObject("$in", orderIdList);
					}
					if (null != orderIdConditionQuery) {
						isParameterised = true;
						paramConditionLst.add(new BasicDBObject(FieldType.ORDER_ID.getName(), orderIdConditionQuery));
					}

					if (StringUtils.isNotBlank(mpa.getPayId())) {
						paramConditionLst.add(new BasicDBObject(FieldType.SUB_MERCHANT_ID.getName(), mpa.getPayId()));
						isParameterised = true;
					}
				} else {
					continue;
				}

				if (!paramConditionLst.isEmpty()) {
					allParamQuery = new BasicDBObject("$and", paramConditionLst);
				}

				List<BasicDBObject> allConditionQueryList = new ArrayList<BasicDBObject>();

				if (!dateQuery.isEmpty()) {
					allConditionQueryList.add(dateQuery);
				}
				if (!dateIndexConditionQuery.isEmpty()) {
					allConditionQueryList.add(dateIndexConditionQuery);
				}

				List<BasicDBObject> fianlList = new ArrayList<BasicDBObject>();

				if (!allParamQuery.isEmpty()) {
					fianlList.add(allParamQuery);
				}

				if (!allConditionQueryList.isEmpty()) {
					BasicDBObject allConditionQueryObj = new BasicDBObject("$and", allConditionQueryList);
					if (!allConditionQueryObj.isEmpty()) {
						fianlList.add(allConditionQueryObj);
					}
				}

				BasicDBObject finalQuery = new BasicDBObject("$and", fianlList);
				VendorPayouts vendorPayout = new VendorPayouts();

				logger.info("Inside TxnReports , viewVendorPayoutReport function , finalQuery = " + finalQuery);
				MongoDatabase dbIns = mongoInstance.getDB();
				MongoCollection<Document> coll = dbIns.getCollection(
						propertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));
				BasicDBObject match = new BasicDBObject("$match", finalQuery);
				BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject("CREATE_DATE", -1));

				List<BasicDBObject> pipeline;

				/*
				 * if(StringUtils.isNotBlank(String.valueOf(start)) &&
				 * StringUtils.isNotBlank(String.valueOf(length))) { BasicDBObject skip = new
				 * BasicDBObject("$skip", start); BasicDBObject limit = new
				 * BasicDBObject("$limit", length);
				 * 
				 * pipeline = Arrays.asList(match, sort, skip, limit); } else {
				 */
				pipeline = Arrays.asList(match, sort);
				// }

				AggregateIterable<Document> output = coll.aggregate(pipeline);
				output.allowDiskUse(true);
				MongoCursor<Document> cursor = output.iterator();

				/* calculation net sale amount for a vendor */
				while (cursor.hasNext()) {

					Document dbObj = cursor.next();
					String netAmount = null;
					if (StringUtils.isNotBlank(dbObj.getString(FieldType.TOTAL_AMOUNT.toString()))
							&& StringUtils.isNotBlank(dbObj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
							&& StringUtils.isNotBlank(dbObj.getString(FieldType.ACQUIRER_GST.toString()))
							&& StringUtils.isNotBlank(dbObj.getString(FieldType.PG_TDR_SC.toString()))
							&& StringUtils.isNotBlank(dbObj.getString(FieldType.PG_GST.toString()))) {

						netAmount = String.format("%.2f",
								Double.parseDouble(dbObj.getString(FieldType.TOTAL_AMOUNT.toString()))
										- (Double.parseDouble(dbObj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
												+ Double.parseDouble(dbObj.getString(FieldType.ACQUIRER_GST.toString()))
												+ Double.parseDouble(dbObj.getString(FieldType.PG_TDR_SC.toString()))
												+ Double.parseDouble(dbObj.getString(FieldType.PG_GST.toString()))));
					} else {
						netAmount = "0.00";
					}

					saleAmount = saleAmount.add(new BigDecimal(netAmount));

					User user1 = new User();

					if (StringUtils.isNotBlank((String) dbObj.get(FieldType.PAY_ID.getName()))) {
						String payid = (String) dbObj.get(FieldType.PAY_ID.getName());

						if (userMap.get(payid) != null && !userMap.get(payid).getPayId().isEmpty()) {
							user1 = userMap.get(payid);
						} else {
							user1 = userdao.findPayId(payid);
							userMap.put(payid, user1);
						}

						// String payId = (String)
						// dbObj.get(FieldType.PAY_ID.getName());
						// User user1 = new User();
						// if (userMap.get(payId) != null &&
						// !userMap.get(payId).getPayId().isEmpty()) {
						// user1 = userMap.get(payId);
						// } else {
						// user1 = userdao.findPayId(payId);
						// userMap.put(payId, user1);
						// }
						vendorPayout.setMerchant(user1.getBusinessName());
					}
					if (dbObj.containsKey(FieldType.SUB_MERCHANT_ID.getName())
							&& StringUtils.isNotBlank(dbObj.getString(FieldType.SUB_MERCHANT_ID.getName()))) {

						String vendor = dbObj.getString(FieldType.SUB_MERCHANT_ID.getName());
						User user2 = new User();

						if (userMap.get(vendor) != null && !userMap.get(vendor).getPayId().isEmpty()) {
							user2 = userMap.get(vendor);
						} else {
							user2 = userdao.findPayId(vendor);
							userMap.put(vendor, user2);
						}
						if (user2 != null) {
							vendorPayout.setVendor(user2.getBusinessName());

						} else {
							vendorPayout.setVendor(CrmFieldConstants.NA.getValue());
						}
					}
				}
				cursor.close();

				/* calculation net refund amount for a vendor */

				List<BasicDBObject> paramConditionLst1 = new ArrayList<BasicDBObject>();
				BasicDBObject allParamQuery1 = new BasicDBObject();

				paramConditionLst1
						.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.REFUNDRECO.getName()));
				paramConditionLst1.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.SETTLED.getName()));

				if (StringUtils.isNotBlank(merchant)) {
					paramConditionLst1.add(new BasicDBObject(FieldType.PAY_ID.getName(), merchant));
					isParameterised = true;
				}

				if (StringUtils.isNotBlank(mpa.getPayId())) {
					paramConditionLst1.add(new BasicDBObject(FieldType.SUB_MERCHANT_ID.getName(), mpa.getPayId()));
					isParameterised = true;
				}

				if (!paramConditionLst1.isEmpty()) {
					allParamQuery1 = new BasicDBObject("$and", paramConditionLst1);
				}

				List<BasicDBObject> allConditionQueryList1 = new ArrayList<BasicDBObject>();

				if (!dateQuery.isEmpty()) {
					allConditionQueryList1.add(dateQuery);
				}
				if (!dateIndexConditionQuery.isEmpty()) {
					allConditionQueryList1.add(dateIndexConditionQuery);
				}

				List<BasicDBObject> fianlList1 = new ArrayList<BasicDBObject>();

				if (!allParamQuery1.isEmpty()) {
					fianlList1.add(allParamQuery1);
				}

				if (!allConditionQueryList1.isEmpty()) {
					BasicDBObject allConditionQueryObj1 = new BasicDBObject("$and", allConditionQueryList1);
					if (!allConditionQueryObj1.isEmpty()) {
						fianlList1.add(allConditionQueryObj1);
					}
				}

				BasicDBObject finalQuery1 = new BasicDBObject("$and", fianlList1);

				logger.info("Inside TxnReports , viewVendorPayoutReport function , finalQuery = " + finalQuery1);
				MongoDatabase dbIns1 = mongoInstance.getDB();
				MongoCollection<Document> coll1 = dbIns1.getCollection(
						propertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));
				BasicDBObject match1 = new BasicDBObject("$match", finalQuery1);
				BasicDBObject sort1 = new BasicDBObject("$sort", new BasicDBObject("CREATE_DATE", -1));

				List<BasicDBObject> pipeline1;

				/*
				 * if(StringUtils.isNotBlank(String.valueOf(start)) &&
				 * StringUtils.isNotBlank(String.valueOf(length))) { BasicDBObject skip1 = new
				 * BasicDBObject("$skip", start); BasicDBObject limit1 = new
				 * BasicDBObject("$limit", length);
				 * 
				 * pipeline1 = Arrays.asList(match1, sort1, skip1, limit1); } else {
				 */
				pipeline1 = Arrays.asList(match1, sort1);
				// }

				AggregateIterable<Document> output1 = coll1.aggregate(pipeline1);
				output.allowDiskUse(true);
				MongoCursor<Document> cursor1 = output1.iterator();

				while (cursor1.hasNext()) {

					Document dbObj = cursor1.next();

					String refundNetAmount = null;
					if (StringUtils.isNotBlank(dbObj.getString(FieldType.TOTAL_AMOUNT.toString()))
							&& StringUtils.isNotBlank(dbObj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
							&& StringUtils.isNotBlank(dbObj.getString(FieldType.ACQUIRER_GST.toString()))
							&& StringUtils.isNotBlank(dbObj.getString(FieldType.PG_TDR_SC.toString()))
							&& StringUtils.isNotBlank(dbObj.getString(FieldType.PG_GST.toString()))) {

						refundNetAmount = String.format("%.2f",
								Double.parseDouble(dbObj.getString(FieldType.TOTAL_AMOUNT.toString()))
										- (Double.parseDouble(dbObj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
												+ Double.parseDouble(dbObj.getString(FieldType.ACQUIRER_GST.toString()))
												+ Double.parseDouble(dbObj.getString(FieldType.PG_TDR_SC.toString()))
												+ Double.parseDouble(dbObj.getString(FieldType.PG_GST.toString()))));
					} else {
						refundNetAmount = "0.00";
					}

					refundAmount = refundAmount.add(new BigDecimal(refundNetAmount));

				}
				cursor1.close();

				if (saleAmount.toString().equalsIgnoreCase("0.00")
						&& refundAmount.toString().equalsIgnoreCase("0.00")) {

				} else {
					if (StringUtils.isNotBlank(mpa.getPaymentCycle())) {
						vendorPayout.setPaymentCycle(mpa.getPaymentCycle());
					} else {
						vendorPayout.setPaymentCycle(CrmFieldConstants.NA.getValue());
					}
					vendorPayout.setVendorPayoutDate(date.split("\\s+")[0]);
					vendorPayout.setPeriod(activationDate.split("\\s+")[0] + " To " + date.split("\\s+")[0]);
					vendorPayout.setSaleAmount(saleAmount.toString());
					vendorPayout.setRefundAmount(refundAmount.toString());
					vendorPayout.setNetPayout(saleAmount.subtract(refundAmount).toString());
					vendorPayoutList.add(vendorPayout);
				}
			}
			logger.info("Inside TxnReports , viewVendorPayoutReport , vendorPayoutReportListSize = "
					+ vendorPayoutList.size());
			return vendorPayoutList;
		} catch (Exception ex) {
			logger.error("exception caugth in viewVendorPayoutReport function ", ex);
			return null;
		}
	}

	@SuppressWarnings("static-access")
	public int capturedDataCount(String merchantPayId, String subMerchantPayId, String pgRefNum, String orderId,
			String paymentType, String currency, String fromDate, String toDate, User user, int start, int length,
			String postSettledFlag, Set<String> orderIdSet) {
		try {
			int total = 0;

			List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();
			BasicDBObject dateQuery = new BasicDBObject();
			BasicDBObject postQuery = new BasicDBObject();

			String currentDate = null;
			if (!fromDate.isEmpty()) {
				if (!toDate.isEmpty()) {
					currentDate = toDate;
				} else {
					DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					Calendar cal = Calendar.getInstance();
					currentDate = dateFormat.format(cal.getTime());
				}

				dateQuery.put(FieldType.UPDATE_DATE.getName(),
						BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(fromDate).toLocalizedPattern())
								.add("$lte", new SimpleDateFormat(currentDate).toLocalizedPattern()).get());

			}

			BasicDBObject dateIndexConditionQuery = new BasicDBObject();

			DateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.ENGLISH);
			Date dateStart = format.parse(fromDate);
			Date dateEnd = format.parse(toDate);

			LocalDate startDate = dateStart.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			LocalDate endDate = dateEnd.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

			String fromDateIndex = startDate.toString().replaceAll("-", "");
			String toDateIndex = endDate.toString().replaceAll("-", "");

			if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get(Constants.USE_DATE_INDEX.getValue()))
					&& propertiesManager.propertiesMap.get(Constants.USE_DATE_INDEX.getValue()).equalsIgnoreCase("Y")) {
				dateIndexConditionQuery.put("TXN_DATE",
						BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(fromDateIndex).toLocalizedPattern())
								.add("$lte", new SimpleDateFormat(toDateIndex).toLocalizedPattern()).get());
			}

			if (!merchantPayId.equalsIgnoreCase("ALL") && StringUtils.isNotBlank(merchantPayId)) {
				paramConditionLst.add(new BasicDBObject(FieldType.PAY_ID.getName(), merchantPayId));
			}

			if (StringUtils.isNotBlank(subMerchantPayId) && !subMerchantPayId.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.SUB_MERCHANT_ID.getName(), subMerchantPayId));
			}
			if (user.getUserType().equals(UserType.RESELLER)) {
				paramConditionLst.add(new BasicDBObject(FieldType.RESELLER_ID.getName(), user.getResellerId()));
			}
			if (!paymentType.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.PAYMENT_TYPE.getName(), paymentType));
			} else {
			}

			if (!pgRefNum.isEmpty()) {
				paramConditionLst.add(new BasicDBObject(FieldType.PG_REF_NUM.getName(), pgRefNum));
			}

			BasicDBObject orderIdConditionQuery = null;
			if (orderIdSet != null) {
				List<String> orderIdList = new ArrayList<>(orderIdSet);
				orderIdConditionQuery = new BasicDBObject("$in", orderIdList);
			}
			if (!orderId.isEmpty()) {
				paramConditionLst.add(new BasicDBObject(FieldType.ORDER_ID.getName(), orderId));
			} else if (null != orderIdConditionQuery) {
				paramConditionLst.add(new BasicDBObject(FieldType.ORDER_ID.getName(), orderIdConditionQuery));
			}

			paramConditionLst.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName()));
			paramConditionLst.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.CAPTURED.getName()));

			if (!currency.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.CURRENCY_CODE.getName(), currency));
			}

			if (!postSettledFlag.isEmpty()) {
				if (!(postSettledFlag.equalsIgnoreCase("Y"))) {
					postQuery = new BasicDBObject("$not", Pattern.compile("^Y"));
					paramConditionLst.add(new BasicDBObject(FieldType.POST_SETTLED_FLAG.getName(), postQuery));
				} else {
					paramConditionLst.add(new BasicDBObject(FieldType.POST_SETTLED_FLAG.getName(), postSettledFlag));
				}
			}

			if (StringUtils.isNotBlank(pgRefNum) || StringUtils.isNotBlank(orderId)) {

			} else {
				if (!dateQuery.isEmpty()) {
					paramConditionLst.add(dateQuery);
				}
				if (!dateIndexConditionQuery.isEmpty()) {
					paramConditionLst.add(dateIndexConditionQuery);
				}
			}

			logger.info("Inside TxnReports , searchPaymentCount , fianlList = " + paramConditionLst);
			BasicDBObject finalquery = new BasicDBObject("$and", paramConditionLst);
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns
					.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));
			total = (int) coll.count(finalquery);
			logger.info("Inside search txn report count , total records from DB  = " + total);
			return total;
		} catch (Exception e) {
			logger.error("exception caugth in Custom Captured ReportCount function ", e);
			return 0;
		}

	}

	@SuppressWarnings("static-access")
	public List<TransactionSearch> capturedData(String merchantPayId, String subMerchantPayId, String pgRefNum,
			String orderId, String paymentType, String currency, String fromDate, String toDate, User user, int start,
			int length, String postSettledFlag, Set<String> orderIdSet) {
		Map<String, User> userMap = new HashMap<String, User>();
		try {
			List<TransactionSearch> transactionList = new ArrayList<TransactionSearch>();
			BasicDBObject dateQuery = new BasicDBObject();
			List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();
			BasicDBObject postQuery = new BasicDBObject();

			String currentDate = null;
			if (!fromDate.isEmpty()) {
				if (!toDate.isEmpty()) {
					currentDate = toDate;
				} else {
					DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					Calendar cal = Calendar.getInstance();
					currentDate = dateFormat.format(cal.getTime());
				}

				dateQuery.put(FieldType.UPDATE_DATE.getName(),
						BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(fromDate).toLocalizedPattern())
								.add("$lte", new SimpleDateFormat(currentDate).toLocalizedPattern()).get());

			}

			BasicDBObject dateIndexConditionQuery = new BasicDBObject();

			DateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.ENGLISH);
			Date dateStart = format.parse(fromDate);
			Date dateEnd = format.parse(toDate);

			LocalDate startDate = dateStart.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			LocalDate endDate = dateEnd.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

			String fromDateIndex = startDate.toString().replaceAll("-", "");
			String toDateIndex = endDate.toString().replaceAll("-", "");

			if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get(Constants.USE_DATE_INDEX.getValue()))
					&& propertiesManager.propertiesMap.get(Constants.USE_DATE_INDEX.getValue()).equalsIgnoreCase("Y")) {
				dateIndexConditionQuery.put("TXN_DATE",
						BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(fromDateIndex).toLocalizedPattern())
								.add("$lte", new SimpleDateFormat(toDateIndex).toLocalizedPattern()).get());
			}

			if (StringUtils.isNotBlank(merchantPayId) && !merchantPayId.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.PAY_ID.getName(), merchantPayId));
			}

			if (StringUtils.isNotBlank(subMerchantPayId) && !subMerchantPayId.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.SUB_MERCHANT_ID.getName(), subMerchantPayId));
			}
			if (user.getUserType().equals(UserType.RESELLER)) {
				paramConditionLst.add(new BasicDBObject(FieldType.RESELLER_ID.getName(), user.getResellerId()));
			}
			if (!paymentType.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.PAYMENT_TYPE.getName(), paymentType));
			}

			if (!pgRefNum.isEmpty()) {
				paramConditionLst.add(new BasicDBObject(FieldType.PG_REF_NUM.getName(), pgRefNum));
			}

			BasicDBObject orderIdConditionQuery = null;
			if (orderIdSet != null) {
				List<String> orderIdList = new ArrayList<>(orderIdSet);
				orderIdConditionQuery = new BasicDBObject("$in", orderIdList);
			}
			if (!orderId.isEmpty()) {
				paramConditionLst.add(new BasicDBObject(FieldType.ORDER_ID.getName(), orderId));
			} else if (null != orderIdConditionQuery) {
				paramConditionLst.add(new BasicDBObject(FieldType.ORDER_ID.getName(), orderIdConditionQuery));
			}

			paramConditionLst.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName()));
			paramConditionLst.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.CAPTURED.getName()));

			if (!currency.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.CURRENCY_CODE.getName(), currency));
			}

			if (!postSettledFlag.isEmpty()) {
				if (!(postSettledFlag.equalsIgnoreCase("Y"))) {

					postQuery = new BasicDBObject("$not", Pattern.compile("^Y"));
					paramConditionLst.add(new BasicDBObject(FieldType.POST_SETTLED_FLAG.getName(), postQuery));
				} else {
					paramConditionLst.add(new BasicDBObject(FieldType.POST_SETTLED_FLAG.getName(), postSettledFlag));
				}
			}

			if (StringUtils.isNotBlank(pgRefNum) || StringUtils.isNotBlank(orderId)) {

			} else {

				if (!dateQuery.isEmpty()) {
					paramConditionLst.add(dateQuery);
				}

				if (!dateIndexConditionQuery.isEmpty()) {
					paramConditionLst.add(dateIndexConditionQuery);
				}
			}

			logger.info("Inside TxnReports , searchPaymentCount , fianlList = " + paramConditionLst);
			BasicDBObject finalquery = new BasicDBObject("$and", paramConditionLst);
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns
					.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));
			BasicDBObject match = new BasicDBObject("$match", finalquery);
			BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject("CREATE_DATE", -1));
			BasicDBObject skip = new BasicDBObject("$skip", start);
			BasicDBObject limit = new BasicDBObject("$limit", length);
			List<BasicDBObject> pipeline = Arrays.asList(match, sort, skip, limit);
			AggregateIterable<Document> output = coll.aggregate(pipeline);
			output.allowDiskUse(true);
			MongoCursor<Document> cursor = output.iterator();

			while (cursor.hasNext()) {
				Document dbobj = cursor.next();

				if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
						&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
					dbobj = dataEncDecTool.decryptDocument(dbobj);
				}

				TransactionSearch transactionResult = new TransactionSearch();

				transactionResult.setMerchants(dbobj.getString(CrmFieldType.BUSINESS_NAME.getName()));

				User user1 = new User();

				if (StringUtils.isNotBlank((String) dbobj.get(FieldType.PAY_ID.getName()))) {
					String payid = (String) dbobj.get(FieldType.PAY_ID.getName());

					if (userMap.get(payid) != null && !userMap.get(payid).getPayId().isEmpty()) {
						user1 = userMap.get(payid);
					} else {
						user1 = userdao.findPayId(payid);
						userMap.put(payid, user1);
					}
					transactionResult.setMerchants(user1.getBusinessName());
				}

				// String payid = (String)
				// dbobj.get(FieldType.PAY_ID.getName());
				// User user1 = new User();
				// if (userMap.get(payid) != null &&
				// !userMap.get(payid).getPayId().isEmpty()) {
				// user1 = userMap.get(payid);
				// } else {
				// user1 = userdao.findPayId(payid);
				// userMap.put(payid, user1);
				// }

				// if (!merchantPayId.equalsIgnoreCase("All") &&
				if (dbobj.containsKey(FieldType.SUB_MERCHANT_ID.getName())
						&& StringUtils.isNotBlank(dbobj.getString(FieldType.SUB_MERCHANT_ID.getName()))) {

					String subMerchant = dbobj.getString(FieldType.SUB_MERCHANT_ID.getName());
					User subMerchantUser = new User();

					if (userMap.get(subMerchant) != null && !userMap.get(subMerchant).getPayId().isEmpty()) {
						subMerchantUser = userMap.get(subMerchant);
					} else {
						subMerchantUser = userdao.findPayId(subMerchant);
						userMap.put(subMerchant, subMerchantUser);
					}
					if (subMerchantUser != null) {
						transactionResult.setSubMerchantId(subMerchantUser.getBusinessName());
					} else {
						transactionResult.setSubMerchantId(CrmFieldConstants.NA.getValue());
					}
				}

				transactionResult.setTransactionIdString(dbobj.getString(FieldType.ORDER_ID.toString()));
				if (StringUtils.isNotBlank(dbobj.getString(FieldType.RRN.getName()))) {
					transactionResult.setRrn(dbobj.getString(FieldType.RRN.toString()));
				} else {
					transactionResult.setRrn(CrmFieldConstants.NA.getValue());
				}
				if (StringUtils.isNotBlank(dbobj.getString(FieldType.CUST_NAME.getName()))) {
					transactionResult.setCustomerName(dbobj.getString(FieldType.CUST_NAME.toString()));
				} else {
					transactionResult.setCustomerName(CrmFieldConstants.NA.getValue());
				}

				transactionResult.setPgRefNum(dbobj.getString(FieldType.PG_REF_NUM.toString()));
				transactionResult.setOrderId(dbobj.getString(FieldType.ORDER_ID.toString()));
				transactionResult.setTransactionCaptureDate(dbobj.getString(FieldType.UPDATE_DATE.getName()));
				transactionResult.setDateFrom(CrmFieldConstants.NA.getValue());
				if (StringUtils.isNotBlank(dbobj.getString(FieldType.PAYMENT_TYPE.toString()))) {
					transactionResult.setPaymentMethods(
							PaymentType.getpaymentName(dbobj.getString(FieldType.PAYMENT_TYPE.toString())));
				} else {
					transactionResult.setPaymentMethods(CrmFieldConstants.NOT_AVAILABLE.getValue());
				}
				if (StringUtils.isNotBlank(dbobj.getString(FieldType.MOP_TYPE.toString()))) {
					transactionResult.setMopType(MopType.getmopName(dbobj.getString(FieldType.MOP_TYPE.toString())));
				} else {
					transactionResult.setMopType(CrmFieldConstants.NOT_AVAILABLE.getValue());
				}
				if (StringUtils.isNotBlank(dbobj.getString(FieldType.PAYMENTS_REGION.toString()))) {
					transactionResult.setPaymentRegion(dbobj.getString(FieldType.PAYMENTS_REGION.toString()));

				} else {
					transactionResult.setPaymentRegion(CrmFieldConstants.NA.getValue());
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.CARD_HOLDER_TYPE.toString()))) {
					transactionResult.setCardHolderType(dbobj.getString(FieldType.CARD_HOLDER_TYPE.toString()));

				} else {
					transactionResult.setCardHolderType(CrmFieldConstants.NA.getValue());
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.PART_SETTLE.toString()))) {
					transactionResult.setPartSettle(dbobj.getString(FieldType.PART_SETTLE.toString()));

				} else {
					transactionResult.setPartSettle(CrmFieldConstants.NA.getValue());
				}
				if (StringUtils.isNotBlank(dbobj.getString(FieldType.PAYMENT_TYPE.toString()))) {
					if (null != dbobj.getString(FieldType.CARD_MASK.toString())) {
						transactionResult.setCardNumber(dbobj.getString(FieldType.CARD_MASK.toString()));
					} else if (null != dbobj.getString(FieldType.PAYER_ADDRESS.getName())) {

						if (user.getUserType().equals(UserType.ADMIN) || user.getUserType().equals(UserType.SUBADMIN)) {
							transactionResult.setCardNumber(dbobj.getString(FieldType.PAYER_ADDRESS.getName()));
						} else {
							String vpaString = dbobj.getString(FieldType.PAYER_ADDRESS.getName());
							String[] vpaArray = vpaString.split("@");
							char[] vpaChar = vpaArray[0].toCharArray();
							StringBuilder vpastrBuilder = new StringBuilder();

							if (vpaChar.length > 3) {
								for (int i = 0; i < vpaChar.length - 3; i++) {
									vpastrBuilder.append(vpaChar[i]);
								}
								vpastrBuilder.append("***@");
								vpastrBuilder.append(vpaArray[1]);
							} else {
								vpastrBuilder.append(vpaChar[0]);
								vpastrBuilder.append("**@");
								vpastrBuilder.append(vpaArray[1]);
							}

							transactionResult.setCardNumber(vpastrBuilder.toString());
						}
					} else {
						transactionResult.setCardNumber(CrmFieldConstants.NA.getValue());
					}
				} else {
					transactionResult.setCardNumber(CrmFieldConstants.NA.getValue());
				}

				transactionResult.setTxnType(dbobj.getString(FieldType.TXNTYPE.toString()));
				transactionResult.setStatus(dbobj.getString(FieldType.STATUS.toString()));
				transactionResult.setAmount(dbobj.getString(FieldType.AMOUNT.toString()));

				if (dbobj.containsKey(FieldType.RESELLER_CHARGES.getName())
						&& StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_CHARGES.getName()))) {

					transactionResult.setTdr_Surcharge(String.format("%.2f",
							(Double.parseDouble(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
									+ Double.parseDouble(dbobj.getString(FieldType.PG_TDR_SC.toString()))
									+ Double.parseDouble(dbobj.getString(FieldType.RESELLER_CHARGES.toString())))));

				} else {

					if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
							&& (StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.toString())))) {

						transactionResult.setTdr_Surcharge(String.format("%.2f",
								(Double.parseDouble(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
										+ Double.parseDouble(dbobj.getString(FieldType.PG_TDR_SC.toString())))));

					} else {
						transactionResult.setTdr_Surcharge("0.00");
					}
				}

				if (dbobj.containsKey(FieldType.RESELLER_GST.getName())
						&& StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_GST.getName()))) {

					transactionResult.setGst_charge(String.format("%.2f",
							(Double.parseDouble(dbobj.getString(FieldType.ACQUIRER_GST.toString()))
									+ Double.parseDouble(dbobj.getString(FieldType.PG_GST.toString()))
									+ Double.parseDouble(dbobj.getString(FieldType.RESELLER_GST.toString())))));

				} else {

					if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.toString()))
							&& (StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.toString())))) {
						transactionResult.setGst_charge(String.format("%.2f",
								(Double.parseDouble(dbobj.getString(FieldType.ACQUIRER_GST.toString()))
										+ Double.parseDouble(dbobj.getString(FieldType.PG_GST.toString())))));

					} else {
						transactionResult.setGst_charge("0.00");
					}
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))) {
					transactionResult.setTotalAmount(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()));
				} else {
					transactionResult.setTotalAmount("");
				}

				if (dbobj.containsKey(FieldType.RESELLER_CHARGES.getName())
						&& dbobj.containsKey(FieldType.RESELLER_GST.getName())
						&& StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_CHARGES.toString()))
						&& StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_GST.toString()))
						&& StringUtils.isNotBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
						&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
						&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.toString()))) {

					transactionResult.setTotalAmtPayable(String.format("%.2f",
							Double.parseDouble(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
									- (Double.parseDouble(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
											+ Double.parseDouble(dbobj.getString(FieldType.ACQUIRER_GST.toString()))
											+ Double.parseDouble(dbobj.getString(FieldType.PG_TDR_SC.toString()))
											+ Double.parseDouble(dbobj.getString(FieldType.PG_GST.toString()))
											+ Double.parseDouble(dbobj.getString(FieldType.RESELLER_CHARGES.toString()))
											+ Double.parseDouble(dbobj.getString(FieldType.RESELLER_GST.toString())))));

				} else {

					if (StringUtils.isNotBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
							&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
							&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.toString()))) {
						transactionResult.setTotalAmtPayable(String.format("%.2f",
								Double.parseDouble(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
										- (Double.parseDouble(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
												+ Double.parseDouble(dbobj.getString(FieldType.ACQUIRER_GST.toString()))
												+ Double.parseDouble(dbobj.getString(FieldType.PG_TDR_SC.toString()))
												+ Double.parseDouble(dbobj.getString(FieldType.PG_GST.toString())))));
					} else {
						transactionResult.setTotalAmtPayable("NA");
					}
				}

				/*
				 * if (StringUtils.isNotBlank(dbobj.getString(FieldType.
				 * TXN_CAPTURE_FLAG.getName()) )) {
				 * transactionResult.setTxnSettledType(dbobj.getString( FieldType.
				 * TXN_CAPTURE_FLAG.getName())); } else {
				 * transactionResult.setTxnSettledType(CrmFieldConstants.NA. getValue()); }
				 */

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.TRANSACTION_MODE.getName()))) {
					transactionResult.setTransactionMode(dbobj.getString(FieldType.TRANSACTION_MODE.getName()));
				} else {
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.POST_SETTLED_FLAG.getName()))) {
					transactionResult.setPostSettledFlag(dbobj.getString(FieldType.POST_SETTLED_FLAG.getName()));
				} else {
					transactionResult.setPostSettledFlag("N");
				}

				transactionList.add(transactionResult);

			}
			logger.info("transactionList created and size = " + transactionList.size());
			cursor.close();
			return transactionList;

		} catch (Exception e) {
			logger.error("exception caugth in Custom Capture Report function ", e);
			e.printStackTrace();
			return null;
		}

	}

	public List<TransactionSearch> capturedDataForDownload(String merchantPayId, String subMerchantPayId,
			String reportPaymentMethod, String pgRefNum, String orderId, String reportCurrency,
			String reportPostSettleFlag, String ReportDateFrom, String ReportDateTo, User user) {
		logger.info("Inside TxnReports , customCaptureDownload");
		Map<String, User> userMap = new HashMap<String, User>();

		List<TransactionSearch> transactionList = new ArrayList<TransactionSearch>();
		BasicDBObject dateQuery = new BasicDBObject();
		try {

			String currentDate = null;
			if (!ReportDateFrom.isEmpty()) {
				if (!ReportDateTo.isEmpty()) {
					currentDate = ReportDateTo;
				} else {
					DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					Calendar cal = Calendar.getInstance();
					currentDate = dateFormat.format(cal.getTime());
				}
				dateQuery.put(FieldType.UPDATE_DATE.getName(),
						BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(ReportDateFrom).toLocalizedPattern())
								.add("$lte", new SimpleDateFormat(currentDate).toLocalizedPattern()).get());
			}

			List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();
			BasicDBObject postQuery = new BasicDBObject();
			BasicDBObject dateIndexConditionQuery = new BasicDBObject();

			DateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.ENGLISH);
			Date dateStart = format.parse(ReportDateFrom);
			Date dateEnd = format.parse(ReportDateTo);

			LocalDate startDate = dateStart.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			LocalDate endDate = dateEnd.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

			String fromDateIndex = startDate.toString().replaceAll("-", "");
			String toDateIndex = endDate.toString().replaceAll("-", "");

			if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get(Constants.USE_DATE_INDEX.getValue()))
					&& propertiesManager.propertiesMap.get(Constants.USE_DATE_INDEX.getValue()).equalsIgnoreCase("Y")) {
				dateIndexConditionQuery.put("TXN_DATE",
						BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(fromDateIndex).toLocalizedPattern())
								.add("$lte", new SimpleDateFormat(toDateIndex).toLocalizedPattern()).get());
			}

			if (StringUtils.isNotBlank(merchantPayId) && !merchantPayId.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.PAY_ID.getName(), merchantPayId));
			}
			if (StringUtils.isNotBlank(subMerchantPayId) && !subMerchantPayId.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.SUB_MERCHANT_ID.getName(), subMerchantPayId));
			}
			if (user.getUserType().equals(UserType.RESELLER)) {
				paramConditionLst.add(new BasicDBObject(FieldType.RESELLER_ID.getName(), user.getResellerId()));
			}
			if (StringUtils.isNotBlank(reportPaymentMethod) && !reportPaymentMethod.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.PAYMENT_TYPE.getName(), reportPaymentMethod));
			}

			if (!pgRefNum.isEmpty()) {
				paramConditionLst.add(new BasicDBObject(FieldType.PG_REF_NUM.getName(), pgRefNum));
			}
			if (!orderId.isEmpty()) {
				paramConditionLst.add(new BasicDBObject(FieldType.ORDER_ID.getName(), orderId));
			}

			paramConditionLst.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName()));
			paramConditionLst.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.CAPTURED.getName()));

			if (StringUtils.isNotBlank(reportCurrency) && !reportCurrency.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.CURRENCY_CODE.getName(), reportCurrency));
			}

			if (!reportPostSettleFlag.isEmpty()) {
				if (!(reportPostSettleFlag.equalsIgnoreCase("Y"))) {

					postQuery = new BasicDBObject("$not", Pattern.compile("^Y"));
					paramConditionLst.add(new BasicDBObject(FieldType.POST_SETTLED_FLAG.getName(), postQuery));
				} else {
					paramConditionLst
							.add(new BasicDBObject(FieldType.POST_SETTLED_FLAG.getName(), reportPostSettleFlag));
				}
			}

			if (StringUtils.isNotBlank(pgRefNum) || StringUtils.isNotBlank(orderId)) {

			} else {

				if (!dateQuery.isEmpty()) {
					paramConditionLst.add(dateQuery);
				}

				if (!dateIndexConditionQuery.isEmpty()) {
					paramConditionLst.add(dateIndexConditionQuery);
				}
			}

			logger.info("Inside TxnReports , Custom Capture Count , fianlList = " + paramConditionLst);
			BasicDBObject finalquery = new BasicDBObject("$and", paramConditionLst);
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns
					.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));
			BasicDBObject match = new BasicDBObject("$match", finalquery);
			BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject("CREATE_DATE", -1));
			List<BasicDBObject> pipeline = Arrays.asList(match, sort/* , group */);

			AggregateIterable<Document> output = coll.aggregate(pipeline);
			output.allowDiskUse(true);
			MongoCursor<Document> cursor = output.iterator();
			while (cursor.hasNext()) {
				Document dbobj = cursor.next();

				if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
						&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
					dbobj = dataEncDecTool.decryptDocument(dbobj);
				}

				TransactionSearch transactionResult = new TransactionSearch();

				transactionResult.setMerchants(dbobj.getString(CrmFieldType.BUSINESS_NAME.getName()));

				User user1 = new User();

				if (StringUtils.isNotBlank((String) dbobj.get(FieldType.PAY_ID.getName()))) {
					String payid = (String) dbobj.get(FieldType.PAY_ID.getName());

					if (userMap.get(payid) != null && !userMap.get(payid).getPayId().isEmpty()) {
						user1 = userMap.get(payid);
					} else {
						user1 = userdao.findPayId(payid);
						userMap.put(payid, user1);
					}
					transactionResult.setMerchants(user1.getBusinessName());
				}
				// String payid = (String)
				// dbobj.get(FieldType.PAY_ID.getName());
				// User user1 = new User();
				// if (userMap.get(payid) != null &&
				// !userMap.get(payid).getPayId().isEmpty()) {
				// user1 = userMap.get(payid);
				// } else {
				// user1 = userdao.findPayId(payid);
				// userMap.put(payid, user1);
				// }

				// if (!merchantPayId.equalsIgnoreCase("All") &&
				if (dbobj.containsKey(FieldType.SUB_MERCHANT_ID.getName())
						&& StringUtils.isNotBlank(dbobj.getString(FieldType.SUB_MERCHANT_ID.getName()))) {

					String subMerchant = dbobj.getString(FieldType.SUB_MERCHANT_ID.getName());
					User subMerchantUser = new User();

					if (userMap.get(subMerchant) != null && !userMap.get(subMerchant).getPayId().isEmpty()) {
						subMerchantUser = userMap.get(subMerchant);
					} else {
						subMerchantUser = userdao.findPayId(subMerchant);
						userMap.put(subMerchant, subMerchantUser);
					}
					if (subMerchantUser != null) {
						transactionResult.setSubMerchantId(subMerchantUser.getBusinessName());
					} else {
						transactionResult.setSubMerchantId(CrmFieldConstants.NA.getValue());
					}
				}

				transactionResult.setTransactionIdString(dbobj.getString(FieldType.ORDER_ID.toString()));
				if (StringUtils.isNotBlank(dbobj.getString(FieldType.RRN.getName()))) {
					transactionResult.setRrn(dbobj.getString(FieldType.RRN.toString()));
				} else {
					transactionResult.setRrn(CrmFieldConstants.NA.getValue());
				}
				if (StringUtils.isNotBlank(dbobj.getString(FieldType.CUST_NAME.getName()))) {
					transactionResult.setCustomerName(dbobj.getString(FieldType.CUST_NAME.toString()));
				} else {
					transactionResult.setCustomerName(CrmFieldConstants.NA.getValue());
				}

				transactionResult.setPgRefNum(dbobj.getString(FieldType.PG_REF_NUM.toString()));
				transactionResult.setOrderId(dbobj.getString(FieldType.ORDER_ID.toString()));
				transactionResult.setTransactionCaptureDate(dbobj.getString(FieldType.UPDATE_DATE.getName()));
				transactionResult.setDateFrom(CrmFieldConstants.NA.getValue());
				if (StringUtils.isNotBlank(dbobj.getString(FieldType.PAYMENT_TYPE.toString()))) {
					transactionResult.setPaymentMethods(
							PaymentType.getpaymentName(dbobj.getString(FieldType.PAYMENT_TYPE.toString())));
				} else {
					transactionResult.setPaymentMethods(CrmFieldConstants.NOT_AVAILABLE.getValue());
				}
				if (StringUtils.isNotBlank(dbobj.getString(FieldType.MOP_TYPE.toString()))) {
					transactionResult.setMopType(MopType.getmopName(dbobj.getString(FieldType.MOP_TYPE.toString())));
				} else {
					transactionResult.setMopType(CrmFieldConstants.NOT_AVAILABLE.getValue());
				}
				if (StringUtils.isNotBlank(dbobj.getString(FieldType.PAYMENTS_REGION.toString()))) {
					transactionResult.setPaymentRegion(dbobj.getString(FieldType.PAYMENTS_REGION.toString()));

				} else {
					transactionResult.setPaymentRegion(CrmFieldConstants.NA.getValue());
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.CARD_HOLDER_TYPE.toString()))) {
					transactionResult.setCardHolderType(dbobj.getString(FieldType.CARD_HOLDER_TYPE.toString()));

				} else {
					transactionResult.setCardHolderType(CrmFieldConstants.NA.getValue());
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.PART_SETTLE.toString()))) {
					transactionResult.setPartSettle(dbobj.getString(FieldType.PART_SETTLE.toString()));

				} else {
					transactionResult.setPartSettle(CrmFieldConstants.NA.getValue());
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.PAYMENT_TYPE.toString()))) {
					if (null != dbobj.getString(FieldType.CARD_MASK.toString())) {
						transactionResult.setCardNumber(dbobj.getString(FieldType.CARD_MASK.toString()));
					} else if (null != dbobj.getString(FieldType.PAYER_ADDRESS.getName())) {

						if (user.getUserType().equals(UserType.ADMIN) || user.getUserType().equals(UserType.SUBADMIN)) {
							transactionResult.setCardNumber(dbobj.getString(FieldType.PAYER_ADDRESS.getName()));
						} else {
							String vpaString = dbobj.getString(FieldType.PAYER_ADDRESS.getName());
							String[] vpaArray = vpaString.split("@");
							char[] vpaChar = vpaArray[0].toCharArray();
							StringBuilder vpastrBuilder = new StringBuilder();

							if (vpaChar.length > 3) {
								for (int i = 0; i < vpaChar.length - 3; i++) {
									vpastrBuilder.append(vpaChar[i]);
								}
								vpastrBuilder.append("***@");
								vpastrBuilder.append(vpaArray[1]);
							} else {
								vpastrBuilder.append(vpaChar[0]);
								vpastrBuilder.append("**@");
								vpastrBuilder.append(vpaArray[1]);
							}

							transactionResult.setCardNumber(vpastrBuilder.toString());
						}
					} else {
						transactionResult.setCardNumber(CrmFieldConstants.NA.getValue());
					}
				} else {
					transactionResult.setCardNumber(CrmFieldConstants.NA.getValue());
				}
				transactionResult.setTxnType(dbobj.getString(FieldType.TXNTYPE.toString()));
				transactionResult.setStatus(dbobj.getString(FieldType.STATUS.toString()));
				transactionResult.setAmount(dbobj.getString(FieldType.AMOUNT.toString()));

				if (dbobj.containsKey(FieldType.RESELLER_CHARGES.getName())
						&& StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_CHARGES.getName()))
						&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
						&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.toString()))) {

					transactionResult.setTdr_Surcharge(String.format("%.2f",
							(Double.parseDouble(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
									+ Double.parseDouble(dbobj.getString(FieldType.PG_TDR_SC.toString()))
									+ Double.parseDouble(dbobj.getString(FieldType.RESELLER_CHARGES.getName())))));

				} else {

					if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
							&& (StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.toString())))) {

						transactionResult.setTdr_Surcharge(String.format("%.2f",
								(Double.parseDouble(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
										+ Double.parseDouble(dbobj.getString(FieldType.PG_TDR_SC.toString())))));

					} else {
						transactionResult.setTdr_Surcharge("0.00");
					}
				}

				if (dbobj.containsKey(FieldType.RESELLER_GST.getName())
						&& StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_GST.getName()))
						&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.toString()))
						&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.toString()))) {

					transactionResult.setGst_charge(String.format("%.2f",
							(Double.parseDouble(dbobj.getString(FieldType.ACQUIRER_GST.toString()))
									+ Double.parseDouble(dbobj.getString(FieldType.PG_GST.toString()))
									+ Double.parseDouble(dbobj.getString(FieldType.RESELLER_GST.getName())))));

				} else {

					if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.toString()))
							&& (StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.toString())))) {
						transactionResult.setGst_charge(String.format("%.2f",
								(Double.parseDouble(dbobj.getString(FieldType.ACQUIRER_GST.toString()))
										+ Double.parseDouble(dbobj.getString(FieldType.PG_GST.toString())))));

					} else {
						transactionResult.setGst_charge("0.00");
					}
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))) {
					transactionResult.setTotalAmount(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()));
				} else {
					transactionResult.setTotalAmount("");
				}

				if (dbobj.containsKey(FieldType.RESELLER_CHARGES.getName())
						&& dbobj.containsKey(FieldType.RESELLER_GST.getName())
						&& StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_CHARGES.toString()))
						&& StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_GST.toString()))
						&& StringUtils.isNotBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
						&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
						&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.toString()))
						&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.toString()))
						&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.toString()))) {

					transactionResult.setTotalAmtPayable(String.format("%.2f",
							Double.parseDouble(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
									- (Double.parseDouble(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
											+ Double.parseDouble(dbobj.getString(FieldType.ACQUIRER_GST.toString()))
											+ Double.parseDouble(dbobj.getString(FieldType.PG_TDR_SC.toString()))
											+ Double.parseDouble(dbobj.getString(FieldType.RESELLER_CHARGES.toString()))
											+ Double.parseDouble(dbobj.getString(FieldType.RESELLER_GST.toString()))
											+ Double.parseDouble(dbobj.getString(FieldType.PG_GST.toString())))));

				} else {

					if (StringUtils.isNotBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
							&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
							&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.toString()))
							&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.toString()))
							&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.toString()))) {

						transactionResult.setTotalAmtPayable(String.format("%.2f",
								Double.parseDouble(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
										- (Double.parseDouble(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
												+ Double.parseDouble(dbobj.getString(FieldType.ACQUIRER_GST.toString()))
												+ Double.parseDouble(dbobj.getString(FieldType.PG_TDR_SC.toString()))
												+ Double.parseDouble(dbobj.getString(FieldType.PG_GST.toString())))));
					} else {
						transactionResult.setTotalAmtPayable("NA");
					}
				}

				/*
				 * if (StringUtils.isNotBlank(dbobj.getString(FieldType.
				 * TXN_CAPTURE_FLAG.getName()) )) {
				 * transactionResult.setTxnSettledType(dbobj.getString( FieldType.
				 * TXN_CAPTURE_FLAG.getName())); } else {
				 * transactionResult.setTxnSettledType(CrmFieldConstants.NA. getValue()); }
				 */

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.TRANSACTION_MODE.getName()))) {
					transactionResult.setTransactionMode(dbobj.getString(FieldType.TRANSACTION_MODE.getName()));
				} else {
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.POST_SETTLED_FLAG.getName()))) {
					transactionResult.setPostSettledFlag(dbobj.getString(FieldType.POST_SETTLED_FLAG.getName()));
				} else {
					transactionResult.setPostSettledFlag("N");
				}

				transactionList.add(transactionResult);

			}
			logger.info("transactionList created and size = " + transactionList.size());
			cursor.close();
			return transactionList;
		} catch (Exception e) {
			logger.error("Exception occured in TxnReports , CapturedDataReport, Exception = ", e);
			return transactionList;
		}
	}

	private String findTotalAmount(String orderID) {
		String totalAmount = "";
		try {
			List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();

			BasicDBObject allParamQuery = new BasicDBObject();

			if (StringUtils.isNotBlank(orderID)) {
				paramConditionLst.add(new BasicDBObject(FieldType.ORDER_ID.getName(), orderID));
			}

			paramConditionLst.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.CAPTURED.getName()));

			List<BasicDBObject> fianlList = new ArrayList<BasicDBObject>();

			if (!allParamQuery.isEmpty()) {
				fianlList.add(allParamQuery);
			}

			if (!paramConditionLst.isEmpty()) {
				BasicDBObject allConditionQueryObj = new BasicDBObject("$and", paramConditionLst);
				if (!allConditionQueryObj.isEmpty()) {
					fianlList.add(allConditionQueryObj);
				}
			}

			BasicDBObject finalquery = new BasicDBObject("$and", fianlList);

			logger.info("Inside TxnReports , searchPayment , finalquery = " + finalquery);
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns
					.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));
			BasicDBObject match = new BasicDBObject("$match", finalquery);
			BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject("CREATE_DATE", -1));
			List<BasicDBObject> pipeline = null;

			pipeline = Arrays.asList(match, sort);
			AggregateIterable<Document> output = coll.aggregate(pipeline);
			output.allowDiskUse(true);
			MongoCursor<Document> cursor = output.iterator();

			while (cursor.hasNext()) {
				Document doc = cursor.next();
				totalAmount = doc.getString(FieldType.TOTAL_AMOUNT.toString());
			}
			cursor.close();
		} catch (Exception e) {
			logger.error("Exception : ", e);
		}
		return totalAmount;
	}

	public Set<String> findBySubuserId(String subuserId, String merchantPayId) {

		Set<String> invoiceIdSet = new HashSet<String>();

		BasicDBObject finalQuery = null;
		if (StringUtils.isNotBlank(subuserId)) {
			finalQuery = new BasicDBObject("CREATED_BY", subuserId);
		} else {
			finalQuery = new BasicDBObject("PAY_ID", merchantPayId);
		}

		try {
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.EPOS_TRANSACTION_COLLECTION.getValue()));

			FindIterable<Document> iterDoc = coll.find(finalQuery);
			MongoCursor<Document> cursor = iterDoc.iterator();
			while (cursor.hasNext()) {
				Document doc = cursor.next();
				invoiceIdSet.add((String) doc.getString("INVOICE_ID"));
			}
		} catch (Exception e) {
			logger.error("Exception : ", e);
		}
		return invoiceIdSet;
	}

	public boolean getPgfNumberForeposSubuser(Set<String> orderIdSet, String pgRefNum) {

		try {

			for (String orderId : orderIdSet) {
				String dbPgRefNumber = "";
				BasicDBObject orderIdQuery = new BasicDBObject();

				orderIdQuery.put(FieldType.ORDER_ID.getName(), orderId);

				MongoDatabase dbIns = mongoInstance.getDB();
				MongoCollection<Document> coll = dbIns.getCollection(
						propertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));
				MongoCursor<Document> cursor = coll.find(orderIdQuery)
						.sort(new BasicDBObject(FieldType.CREATE_DATE.getName(), -1)).iterator();

				while (cursor.hasNext()) {
					Document doc = cursor.next();

					dbPgRefNumber = doc.getString(FieldType.PG_REF_NUM.toString());
					if (StringUtils.isNotEmpty(dbPgRefNumber) && dbPgRefNumber.equalsIgnoreCase(pgRefNum)) {
						return true;
					}
				}

			}

			return false;
		} catch (Exception e) {
			return false;
		}
	}

	private boolean isNotEligibleForRefund(String refundEligibleTill, Document dbobj) {
		try {
			if (StringUtils.isBlank(refundEligibleTill)) {
				return false;
			}
			if (StringUtils.isBlank(dbobj.getString(FieldType.REFUND_ORDER_ID.toString()))) {
				return false;
			}
			if (isExpired(refundEligibleTill)) {
				return false;
			}
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	private boolean isExpired(String date) {
		if (date.isEmpty() || date.trim().equals("")) {
			return true;
		} else {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
			Date d = null;
			Date d1 = null;
			String today = getToday("yyyy-MM-dd hh:mm:ss");
			try {
				d = sdf.parse(date);
				d1 = sdf.parse(today);
				if (d1.compareTo(d) < 0) {
					return false;
				} else if (d.compareTo(d1) == 0) {
					if (d.getTime() < d1.getTime()) {
						return true;
					} else if (d.getTime() == d1.getTime()) {
						return true;
					} else {
						return false;
					}
				} else {
					return true;
				}
			} catch (ParseException e) {
				return true;
			}
		}
	}

	public String getToday(String format) {
		Date date = new Date();
		return new SimpleDateFormat(format).format(date);
	}

	private String calculateRefundDays(String date, String duration) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		Calendar c = Calendar.getInstance();
		try {
			c.setTime(sdf.parse(date));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		c.add(Calendar.DAY_OF_MONTH, Integer.parseInt(duration));
		String newDate = sdf.format(c.getTime());
		return newDate;
	}

	public List<String> searchSkuCodeListForManualRefund(String pgRefNum, String payId, String refundedAmount,
			String refundAvailable, String chargebackAmount) {

		List<BasicDBObject> finalList = new ArrayList<BasicDBObject>();
		List<String> skuCodeList = new ArrayList<String>();
		try {
			finalList.add(new BasicDBObject(FieldType.PG_REF_NUM.getName(), pgRefNum));
			finalList.add(new BasicDBObject(FieldType.PAY_ID.getName(), payId));
			finalList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName()));
			finalList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.CAPTURED.getName()));

			BasicDBObject finalquery = new BasicDBObject("$and", finalList);

			logger.info("Inside TxnReports , searchPayment , finalquery = " + finalquery);
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns
					.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));
			BasicDBObject match = new BasicDBObject("$match", finalquery);

			Document firstGroup;

			firstGroup = new Document("_id", new Document("_id", "$_id"));
			BasicDBObject firstGroupObject = new BasicDBObject(firstGroup);
			BasicDBObject secondGroup = new BasicDBObject("$push", "$$ROOT");
			BasicDBObject group = new BasicDBObject("$group", firstGroupObject.append("entries", secondGroup));
			BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject("INSERTION_DATE", -1));

			List<BasicDBObject> pipeline = Arrays.asList(match, sort, group);

			AggregateIterable<Document> output = coll.aggregate(pipeline);
			output.allowDiskUse(true);
			MongoCursor<Document> cursor = output.iterator();
			while (cursor.hasNext()) {
				Document mydata = cursor.next();
				List<Document> courses = (List<Document>) mydata.get("entries");
				Document dbobj = courses.get(0);
				String skuCode = "";
				if (StringUtils.isNotBlank(dbobj.getString(FieldType.SKU_CODE.getName()))) {
					skuCode = dbobj.getString(FieldType.SKU_CODE.getName());
					String[] skuCodeArray = skuCode.split(",");
					for (int i = 0; i < skuCodeArray.length; i++) {
						skuCodeList.add(skuCodeArray[i]);
					}
				} else {
					skuCodeList.add("NA");
				}
			}
			cursor.close();
			return skuCodeList;
		} catch (Exception ex) {
			logger.error("Exception in getting SKU code list for manual refund Process ", ex);
		}
		return skuCodeList;
	}

	public void updateProductDescriptionBasedOnSkuCode(String skuCode, String orderId, String refundOrderId) {
		List<BasicDBObject> finalList = new ArrayList<BasicDBObject>();
		try {
			finalList.add(new BasicDBObject(FieldType.SKU_CODE.getName(), skuCode));
			finalList.add(new BasicDBObject(FieldType.ORDER_ID.getName(), orderId));

			BasicDBObject finalquery = new BasicDBObject("$and", finalList);

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					propertiesManager.propertiesMap.get(prefix + Constants.PROD_DESC_COLLECTION.getValue()));
			BasicDBObject match = new BasicDBObject("$match", finalquery);

			List<BasicDBObject> pipeline = Arrays.asList(match);

			AggregateIterable<Document> output = coll.aggregate(pipeline);
			output.allowDiskUse(true);
			MongoCursor<Document> cursor = output.iterator();
			while (cursor.hasNext()) {
				Document dbobj = cursor.next();
				dbobj.put("_id", refundOrderId);
				dbobj.put("TXNTYPE", "REFUND");
				dbobj.put("REFUND_ORDER_ID", refundOrderId);
				coll.insertOne(dbobj);
			}
			cursor.close();
		} catch (Exception ex) {
			logger.error("Exception in inserting refunded value Process ", ex);
		}
	}

	@SuppressWarnings({ "unchecked", "static-access" })
	public List<MerchantPaymentAdviseDownloadObject> merchantPaymentAdviseDownloadAutoSendForSale(String merchantPayId,
			String subMerchantPayId, String fromDate, String toDate, User user, Set<String> orderIdSet) {

		List<MerchantPaymentAdviseDownloadObject> paymentAdviseList = new ArrayList<MerchantPaymentAdviseDownloadObject>();

		List<String> merchantPayIdQueryList = new ArrayList<String>();
		BasicDBObject merchantPayIdQueryObject = null;
		BasicDBObject merchantPayIdQuery = new BasicDBObject();
		try {
			logger.info("Inside TxnReport, MerchantPaymentAdvice");
			List<BasicDBObject> finalList = new ArrayList<BasicDBObject>();
			BasicDBObject dateQuery = new BasicDBObject();

			String currentDate = null;
			if (!fromDate.isEmpty()) {
				if (!toDate.isEmpty()) {
					currentDate = toDate;
				} else {
					DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					Calendar cal = Calendar.getInstance();
					currentDate = dateFormat.format(cal.getTime());
				}
				dateQuery.put(FieldType.CREATE_DATE.getName(),
						BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(fromDate).toLocalizedPattern())
								.add("$lte", new SimpleDateFormat(currentDate).toLocalizedPattern()).get());
			}

			BasicDBObject dateIndex = new BasicDBObject();
			List<String> dateList = new ArrayList<String>();
			BasicDBObject dateIndexQuery = new BasicDBObject();
			String startDateString = new SimpleDateFormat(fromDate).toLocalizedPattern();
			String endDateString = new SimpleDateFormat(currentDate).toLocalizedPattern();

			DateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.ENGLISH);
			Date dateStart = format.parse(startDateString);
			Date dateEnd = format.parse(endDateString);

			LocalDate incrementingDate = dateStart.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			LocalDate endDate = dateEnd.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

			List<String> allDateIndex = new ArrayList<>();

			while (!incrementingDate.isAfter(endDate)) {
				allDateIndex.add(incrementingDate.toString().replaceAll("-", ""));
				incrementingDate = incrementingDate.plusDays(1);
			}

			for (String date : allDateIndex) {
				dateList.add(date);
			}

			if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get(Constants.USE_DATE_INDEX.getValue()))
					&& propertiesManager.propertiesMap.get(Constants.USE_DATE_INDEX.getValue()).equalsIgnoreCase("Y")) {
				dateIndex.append("$in", dateList);
			}

			if (!dateIndex.isEmpty()) {
				dateIndexQuery.append(FieldType.DATE_INDEX.getName(), dateIndex);
			}
			if (merchantPayId.equalsIgnoreCase("All")) {
				if (user.getUserType().equals(UserType.RESELLER)) {
					List<Merchants> resellerMerchanList = userdao.getResellerMerchantList(user.getResellerId());

					for (Merchants resellerMerchant : resellerMerchanList) {
						merchantPayIdQueryList.add(resellerMerchant.getPayId());
					}
					merchantPayIdQueryObject = new BasicDBObject("$in", merchantPayIdQueryList);
					merchantPayIdQuery.append(FieldType.PAY_ID.getName(), merchantPayIdQueryObject);
				}
				if (user.getUserType().equals(UserType.ADMIN) || user.getUserType().equals(UserType.SUBADMIN)) {
					List<Merchants> allMerchanList = userdao.getActiveMerchantList();

					for (Merchants merchant : allMerchanList) {
						merchantPayIdQueryList.add(merchant.getPayId());
					}
					merchantPayIdQueryObject = new BasicDBObject("$in", merchantPayIdQueryList);
					merchantPayIdQuery.append(FieldType.PAY_ID.getName(), merchantPayIdQueryObject);
				}

				if (merchantPayIdQueryObject != null) {
					finalList.add(merchantPayIdQuery);
				}
			} else {
				finalList.add(new BasicDBObject(FieldType.PAY_ID.getName(), merchantPayId));
			}

			BasicDBObject orderIdConditionQuery = null;
			if (orderIdSet != null) {
				List<String> orderIdList = new ArrayList<>(orderIdSet);
				orderIdConditionQuery = new BasicDBObject("$in", orderIdList);
			}
			if (null != orderIdConditionQuery) {
				finalList.add(new BasicDBObject(FieldType.ORDER_ID.getName(), orderIdConditionQuery));
			}

			if (StringUtils.isNotBlank(subMerchantPayId) && !subMerchantPayId.equalsIgnoreCase("All")) {
				finalList.add(new BasicDBObject(FieldType.SUB_MERCHANT_ID.getName(), subMerchantPayId));
			}

			List<BasicDBObject> refundConditionQueryList = new ArrayList<BasicDBObject>();
			refundConditionQueryList
					.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.REFUNDRECO.getName()));
			refundConditionQueryList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.SETTLED.getName()));
			BasicDBObject refundConditionQueryObj = new BasicDBObject("$and", refundConditionQueryList);

			List<BasicDBObject> saleConditionQueryList = new ArrayList<BasicDBObject>();
			saleConditionQueryList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.RECO.getName()));
			saleConditionQueryList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.SETTLED.getName()));
			BasicDBObject saleConditionQueryObj = new BasicDBObject("$and", saleConditionQueryList);

			List<BasicDBObject> saleAndRefundConditionQueryList = new ArrayList<BasicDBObject>();
			saleAndRefundConditionQueryList.add(saleConditionQueryObj);
			saleAndRefundConditionQueryList.add(refundConditionQueryObj);
			BasicDBObject saleAndRefundConditionQueryObj = new BasicDBObject("$or", saleAndRefundConditionQueryList);

			if (!saleAndRefundConditionQueryObj.isEmpty()) {
				finalList.add(saleAndRefundConditionQueryObj);
			}
			if (!dateQuery.isEmpty()) {
				finalList.add(dateQuery);
			}
			if (!dateIndexQuery.isEmpty()) {
				finalList.add(dateIndexQuery);
			}

			BasicDBObject finalquery = new BasicDBObject("$and", finalList);

			logger.info("Inside TxnReports , searchPayment , finalquery = " + finalquery);
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns
					.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));
			BasicDBObject match = new BasicDBObject("$match", finalquery);

			Document firstGroup;
			firstGroup = new Document("_id", new Document("_id", "$_id"));

			BasicDBObject firstGroupObject = new BasicDBObject(firstGroup);
			BasicDBObject secondGroup = new BasicDBObject("$push", "$$ROOT");
			BasicDBObject group = new BasicDBObject("$group", firstGroupObject.append("entries", secondGroup));
			BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject("INSERTION_DATE", -1));

			List<BasicDBObject> pipeline = Arrays.asList(match, sort, group);

			AggregateIterable<Document> output = coll.aggregate(pipeline);
			output.allowDiskUse(true);
			MongoCursor<Document> cursor = output.iterator();
			while (cursor.hasNext()) {
				Document mydata = cursor.next();
				List<Document> courses = (List<Document>) mydata.get("entries");
				Document dbobj = courses.get(0);
				MerchantPaymentAdviseDownloadObject merchantPaymentReport = new MerchantPaymentAdviseDownloadObject();

				String mopType = dbobj.getString(FieldType.MOP_TYPE.getName());
				if (StringUtils.isBlank(mopType)) {
					mopType = "NA";
				}
				if (StringUtils.isNumeric(mopType)
						|| (StringUtils.isNotBlank(dbobj.getString(FieldType.PAYMENT_TYPE.toString()))
								&& dbobj.getString(FieldType.PAYMENT_TYPE.getName()).equalsIgnoreCase("WL"))) {
					merchantPaymentReport.setCardNetwork(MopType.getmopName(mopType));
				} else {
					switch (mopType) {
					case "VI":
						merchantPaymentReport.setCardNetwork(MopType.getmopName(mopType));
						break;

					case "MC":
						merchantPaymentReport.setCardNetwork(MopType.getmopName(mopType));
						break;

					case "RU":
						merchantPaymentReport.setCardNetwork(MopType.getmopName(mopType));
						break;

					case "DN":
						merchantPaymentReport.setCardNetwork(MopType.getmopName(mopType));
						break;

					case "CD":
						merchantPaymentReport.setCardNetwork(MopType.getmopName(mopType));
						break;

					case "UP":
						merchantPaymentReport.setCardNetwork(MopType.getmopName(mopType));
						break;

					case "WL":
						merchantPaymentReport.setCardNetwork(MopType.getmopName(mopType));
						break;

					case "NB":
						merchantPaymentReport.setCardNetwork(MopType.getmopName(mopType));
						break;

					case "NEFT":
						merchantPaymentReport.setCardNetwork(MopType.getmopName(mopType));
						break;
					case "IMPS":
						merchantPaymentReport.setCardNetwork(MopType.getmopName(mopType));
						break;
					case "RTGS":
						merchantPaymentReport.setCardNetwork(MopType.getmopName(mopType));
						break;
					}
				}

				String paymentType = dbobj.getString(FieldType.PAYMENT_TYPE.getName());
				if (paymentType == null) {
				} else {
					switch (paymentType) {
					case "NB":
						merchantPaymentReport.setPaymentType(PaymentType.getpaymentName(paymentType));
						break;

					case "UP":
						merchantPaymentReport.setPaymentType(PaymentType.getpaymentName(paymentType));
						break;

					case "WL":
						merchantPaymentReport.setPaymentType(PaymentType.getpaymentName(paymentType));
						break;
					case "CD":
						merchantPaymentReport.setPaymentType(PaymentType.getpaymentName(paymentType));
						break;

					case "CC":
						merchantPaymentReport.setPaymentType(PaymentType.getpaymentName(paymentType));
						break;

					case "DC":
						merchantPaymentReport.setPaymentType(PaymentType.getpaymentName(paymentType));
						break;

					case "PC":
						merchantPaymentReport.setPaymentType(PaymentType.getpaymentName(paymentType));
						break;

					case "AD":
						merchantPaymentReport.setPaymentType(PaymentType.getpaymentName(paymentType));
						break;

					case "DP":
						merchantPaymentReport.setPaymentType(PaymentType.getpaymentName(paymentType));
						break;

					case "EX":
						merchantPaymentReport.setPaymentType(PaymentType.getpaymentName(paymentType));
						break;

					case "RP":
						merchantPaymentReport.setPaymentType(PaymentType.getpaymentName(paymentType));
						break;

					case "EM":
						merchantPaymentReport.setPaymentType(PaymentType.getpaymentName(paymentType));
						break;

					case "NEFT":
						merchantPaymentReport.setPaymentType(PaymentType.getpaymentName(paymentType));
						break;

					case "IMPS":
						merchantPaymentReport.setPaymentType(PaymentType.getpaymentName(paymentType));
						break;

					case "RTGS":
						merchantPaymentReport.setPaymentType(PaymentType.getpaymentName(paymentType));
						break;
					}
				}
				// String paymentType =
				// dbobj.getString(FieldType.PAYMENT_TYPE.getName());
				// merchantPaymentReport.setPaymentType(PaymentType.getpaymentName(paymentType));
				if (StringUtils.isNotBlank(dbobj.getString(FieldType.PAYMENTS_REGION.getName()))) {
					merchantPaymentReport.setPaymentRegion(dbobj.getString(FieldType.PAYMENTS_REGION.getName()));
				} else {
					merchantPaymentReport.setPaymentRegion(CrmFieldConstants.NA.getValue());
					logger.info("Inside txnReport for download payment advise Report and Payment Region set "
							+ CrmFieldConstants.NA.getValue());
				}
				if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_MODE.getName()))) {
					merchantPaymentReport.setAcquirerMode(dbobj.getString(FieldType.ACQUIRER_MODE.toString()));
				} else {
					merchantPaymentReport.setAcquirerMode(CrmFieldConstants.NA.getValue());
					logger.info("Inside txnReport for download payment advise report and acquirer mode set"
							+ CrmFieldConstants.NA.getValue());
				}
				if (StringUtils.isNotBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.getName()))) {
					merchantPaymentReport.setGrossAmount(dbobj.getString(FieldType.TOTAL_AMOUNT.getName()));
				} else {
					merchantPaymentReport.setGrossAmount("0.00");
					logger.info("inside txnReport for download Payment Advise report and total amount set 0.00");
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.AMOUNT.getName()))) {
					merchantPaymentReport.setBaseAmount(dbobj.getString(FieldType.AMOUNT.getName()));
				} else {
					merchantPaymentReport.setBaseAmount("0.00");
					logger.info("inside txnReport for download Payment Advise report and base amount set 0.00");
				}

				merchantPaymentReport.setOrigTxnType(dbobj.getString(FieldType.ORIG_TXNTYPE.getName()));

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.ORIG_TXNTYPE.toString()))
						&& dbobj.getString(FieldType.ORIG_TXNTYPE.getName()).equalsIgnoreCase(TxnType.SALE.getName())
						&& StringUtils.isNotBlank(dbobj.getString(FieldType.PAYMENT_TYPE.toString()))
						&& (dbobj.getString(FieldType.PAYMENT_TYPE.getName())
								.equalsIgnoreCase(PaymentType.COD.getCode()))) {

					if (user.getUserType().equals(UserType.RESELLER) && user.isPartnerFlag()) {
						if (StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_CHARGES.getName()))
								&& StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_GST.getName()))
								&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_RESELLER_CHARGE.getName()))
								&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_RESELLER_GST.getName()))) {

							merchantPaymentReport.setResellerCharges(
									String.valueOf(new BigDecimal(dbobj.getString(FieldType.RESELLER_CHARGES.getName()))
											.add(new BigDecimal(dbobj.getString(FieldType.RESELLER_GST.getName())))
											.add(new BigDecimal(
													dbobj.getString(FieldType.PG_RESELLER_CHARGE.getName())))
											.add(new BigDecimal(
													dbobj.getString(FieldType.PG_RESELLER_GST.getName())))));

							merchantPaymentReport.setNetAmount("-" + String
									.valueOf(new BigDecimal(dbobj.getString(FieldType.RESELLER_CHARGES.getName()))
											.add(new BigDecimal(dbobj.getString(FieldType.RESELLER_GST.getName())))
											.add(new BigDecimal(
													dbobj.getString(FieldType.PG_RESELLER_CHARGE.getName())))
											.add(new BigDecimal(
													dbobj.getString(FieldType.PG_RESELLER_GST.getName())))));

						} else {
							merchantPaymentReport.setResellerCharges(String.valueOf(0.00));
							merchantPaymentReport.setNetAmount(String.valueOf(0.00));
							logger.info(
									"inside txnReport for download Payment Advise report and tdr/surcharge set 0.00");
						}
					} else {

						if (StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_CHARGES.getName()))
								&& StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_GST.getName()))
								&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_RESELLER_CHARGE.getName()))
								&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_RESELLER_GST.getName()))) {

							if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.getName()))
									&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.getName()))
									&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.getName()))
									&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.getName()))) {

								merchantPaymentReport.setTdr(String
										.valueOf(new BigDecimal(dbobj.getString(FieldType.ACQUIRER_TDR_SC.getName()))
												.add(new BigDecimal(dbobj.getString(FieldType.ACQUIRER_GST.getName())))
												.add(new BigDecimal(dbobj.getString(FieldType.PG_TDR_SC.getName())))
												.add(new BigDecimal(dbobj.getString(FieldType.PG_GST.getName())))
												.add(new BigDecimal(
														dbobj.getString(FieldType.RESELLER_CHARGES.getName())))
												.add(new BigDecimal(dbobj.getString(FieldType.RESELLER_GST.getName())))
												.add(new BigDecimal(
														dbobj.getString(FieldType.PG_RESELLER_CHARGE.getName())))
												.add(new BigDecimal(
														dbobj.getString(FieldType.PG_RESELLER_GST.getName())))));

								merchantPaymentReport.setNetAmount("-" + String
										.valueOf(new BigDecimal(dbobj.getString(FieldType.ACQUIRER_TDR_SC.getName()))
												.add(new BigDecimal(dbobj.getString(FieldType.ACQUIRER_GST.getName())))
												.add(new BigDecimal(dbobj.getString(FieldType.PG_TDR_SC.getName())))
												.add(new BigDecimal(dbobj.getString(FieldType.PG_GST.getName())))
												.add(new BigDecimal(
														dbobj.getString(FieldType.RESELLER_CHARGES.getName())))
												.add(new BigDecimal(dbobj.getString(FieldType.RESELLER_GST.getName())))
												.add(new BigDecimal(
														dbobj.getString(FieldType.PG_RESELLER_CHARGE.getName())))
												.add(new BigDecimal(
														dbobj.getString(FieldType.PG_RESELLER_GST.getName())))));

							} else {
								merchantPaymentReport.setTdr(String.valueOf(0.00));
								merchantPaymentReport.setNetAmount(String.valueOf(0.00));
								logger.info(
										"inside txnReport for download Payment Advise report and tdr/surcharge set 0.00");
							}

						} else {

							if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.getName()))
									&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.getName()))
									&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.getName()))
									&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.getName()))) {

								merchantPaymentReport.setTdr(String
										.valueOf(new BigDecimal(dbobj.getString(FieldType.ACQUIRER_TDR_SC.getName()))
												.add(new BigDecimal(dbobj.getString(FieldType.ACQUIRER_GST.getName())))
												.add(new BigDecimal(dbobj.getString(FieldType.PG_TDR_SC.getName())))
												.add(new BigDecimal(dbobj.getString(FieldType.PG_GST.getName())))));

								merchantPaymentReport.setNetAmount("-" + String
										.valueOf(new BigDecimal(dbobj.getString(FieldType.ACQUIRER_TDR_SC.getName()))
												.add(new BigDecimal(dbobj.getString(FieldType.ACQUIRER_GST.getName())))
												.add(new BigDecimal(dbobj.getString(FieldType.PG_TDR_SC.getName())))
												.add(new BigDecimal(dbobj.getString(FieldType.PG_GST.getName())))));

							} else {
								merchantPaymentReport.setTdr(String.valueOf(0.00));
								merchantPaymentReport.setNetAmount(String.valueOf(0.00));
								logger.info(
										"inside txnReport for download Payment Advise report and tdr/surcharge set 0.00");
							}
						}
					}

				} else if (StringUtils.isNotBlank(dbobj.getString(FieldType.ORIG_TXNTYPE.toString()))
						&& dbobj.getString(FieldType.ORIG_TXNTYPE.getName()).equalsIgnoreCase(TxnType.REFUND.getName())
						&& StringUtils.isNotBlank(dbobj.getString(FieldType.PAYMENT_TYPE.toString()))
						&& (dbobj.getString(FieldType.PAYMENT_TYPE.getName())
								.equalsIgnoreCase(PaymentType.COD.getCode()))) {

					merchantPaymentReport.setNetAmount("0.00");
					if (user.getUserType().equals(UserType.RESELLER) && user.isPartnerFlag()) {
						merchantPaymentReport.setResellerCharges("0.00");
					} else {
						merchantPaymentReport.setTdr("0.00");
					}

				} else {

					if (user.getUserType().equals(UserType.RESELLER) && user.isPartnerFlag()) {
						if (StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_CHARGES.getName()))
								&& StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_GST.getName()))
								&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_RESELLER_CHARGE.getName()))
								&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_RESELLER_GST.getName()))) {

							merchantPaymentReport.setResellerCharges(
									String.valueOf(new BigDecimal(dbobj.getString(FieldType.RESELLER_CHARGES.getName()))
											.add(new BigDecimal(dbobj.getString(FieldType.RESELLER_GST.getName())))
											.add(new BigDecimal(
													dbobj.getString(FieldType.PG_RESELLER_CHARGE.getName())))
											.add(new BigDecimal(
													dbobj.getString(FieldType.PG_RESELLER_GST.getName())))));

							merchantPaymentReport.setNetAmount(
									String.valueOf(new BigDecimal(dbobj.getString(FieldType.TOTAL_AMOUNT.getName()))
											.subtract(new BigDecimal(merchantPaymentReport.getResellerCharges()))));
						} else {
							merchantPaymentReport.setResellerCharges("0.00");
							merchantPaymentReport.setNetAmount(
									String.valueOf(new BigDecimal(dbobj.getString(FieldType.TOTAL_AMOUNT.getName()))
											.subtract(new BigDecimal(merchantPaymentReport.getResellerCharges()))));
						}

					} else {

						if (StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_CHARGES.getName()))
								&& StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_GST.getName()))
								&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_RESELLER_CHARGE.getName()))
								&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_RESELLER_GST.getName()))) {

							if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.getName()))
									&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.getName()))
									&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.getName()))
									&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.getName()))) {

								merchantPaymentReport.setTdr(String
										.valueOf(new BigDecimal(dbobj.getString(FieldType.ACQUIRER_TDR_SC.getName()))
												.add(new BigDecimal(dbobj.getString(FieldType.ACQUIRER_GST.getName())))
												.add(new BigDecimal(dbobj.getString(FieldType.PG_TDR_SC.getName())))
												.add(new BigDecimal(dbobj.getString(FieldType.PG_GST.getName())))
												.add(new BigDecimal(
														dbobj.getString(FieldType.RESELLER_CHARGES.getName())))
												.add(new BigDecimal(dbobj.getString(FieldType.RESELLER_GST.getName())))
												.add(new BigDecimal(
														dbobj.getString(FieldType.PG_RESELLER_CHARGE.getName())))
												.add(new BigDecimal(
														dbobj.getString(FieldType.PG_RESELLER_GST.getName())))));

								merchantPaymentReport.setNetAmount(
										String.valueOf(new BigDecimal(dbobj.getString(FieldType.TOTAL_AMOUNT.getName()))
												.subtract(new BigDecimal(merchantPaymentReport.getTdr()))));
							} else {

								if (StringUtils.isNotBlank(dbobj.getString(FieldType.PAYMENT_TYPE.toString()))
										&& (dbobj.getString(FieldType.PAYMENT_TYPE.getName()).equalsIgnoreCase("RTGS")
												|| dbobj.getString(FieldType.PAYMENT_TYPE.getName())
														.equalsIgnoreCase("IMPS")
												|| dbobj.getString(FieldType.PAYMENT_TYPE.getName())
														.equalsIgnoreCase("NEFT"))) {

									merchantPaymentReport.setTdr(String.valueOf(0.00));

									merchantPaymentReport.setNetAmount(String.valueOf(
											new BigDecimal(dbobj.getString(FieldType.TOTAL_AMOUNT.getName()))));

								} else {

									merchantPaymentReport.setTdr(String.valueOf(0.00));
									merchantPaymentReport.setNetAmount(String.valueOf(0.00));
									logger.info(
											"inside txnReport for download Payment Advise report and tdr/surcharge set 0.00");
								}
							}

						} else {

							if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.getName()))
									&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.getName()))
									&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.getName()))
									&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.getName()))) {

								merchantPaymentReport.setTdr(String
										.valueOf(new BigDecimal(dbobj.getString(FieldType.ACQUIRER_TDR_SC.getName()))
												.add(new BigDecimal(dbobj.getString(FieldType.ACQUIRER_GST.getName())))
												.add(new BigDecimal(dbobj.getString(FieldType.PG_TDR_SC.getName())))
												.add(new BigDecimal(dbobj.getString(FieldType.PG_GST.getName())))));

								merchantPaymentReport.setNetAmount(
										String.valueOf(new BigDecimal(dbobj.getString(FieldType.TOTAL_AMOUNT.getName()))
												.subtract(new BigDecimal(merchantPaymentReport.getTdr()))));
							} else {

								if (StringUtils.isNotBlank(dbobj.getString(FieldType.PAYMENT_TYPE.toString()))
										&& (dbobj.getString(FieldType.PAYMENT_TYPE.getName()).equalsIgnoreCase("RTGS")
												|| dbobj.getString(FieldType.PAYMENT_TYPE.getName())
														.equalsIgnoreCase("IMPS")
												|| dbobj.getString(FieldType.PAYMENT_TYPE.getName())
														.equalsIgnoreCase("NEFT"))) {

									merchantPaymentReport.setTdr(String.valueOf(0.00));

									merchantPaymentReport.setNetAmount(String.valueOf(
											new BigDecimal(dbobj.getString(FieldType.TOTAL_AMOUNT.getName()))));

								} else {

									merchantPaymentReport.setTdr(String.valueOf(0.00));
									merchantPaymentReport.setNetAmount(String.valueOf(0.00));
									logger.info(
											"inside txnReport for download Payment Advise report and tdr/surcharge set 0.00");
								}
							}
						}
					}
				}
				merchantPaymentReport.setPayId(dbobj.getString(FieldType.PAY_ID.getName()));
				merchantPaymentReport.setOid(dbobj.getString(FieldType.OID.getName()));
				merchantPaymentReport.setCardHolderType(dbobj.getString(FieldType.CARD_HOLDER_TYPE.getName()));
				paymentAdviseList.add(merchantPaymentReport);
			}
			cursor.close();
			return paymentAdviseList;
		} catch (MongoExecutionTimeoutException exception) {
			logger.error("mongo timeout exception caught while fetch data for payment advise report ", exception);
		} catch (MongoQueryException exception) {
			logger.error("mongoQuery Exception caught while fetch data for payment advise report ", exception);
		} catch (Exception exception) {
			logger.error("Exception caught while fetch data for payment advise report", exception);
		}
		return paymentAdviseList;
	}

	@SuppressWarnings("static-access")
	public List<TransactionSearch> fraudAnalyticsReportData(String merchantPayId, String subMerchantPayId,
			String paymentRegion, String countryCode, String status, String fromDate, String toDate, User user) {
		Map<String, User> userMap = new HashMap<String, User>();
		logger.info("Inside TxnReports , Fraud Analytics method");
		List<TransactionSearch> transactionList = new ArrayList<TransactionSearch>();
		try {

			boolean isParameterised = false;
			List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();

			BasicDBObject dateQuery = new BasicDBObject();
			BasicDBObject allParamQuery = new BasicDBObject();

			String currentDate = null;
			if (!fromDate.isEmpty()) {
				if (!toDate.isEmpty()) {
					currentDate = toDate;
				} else {
					DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					Calendar cal = Calendar.getInstance();
					currentDate = dateFormat.format(cal.getTime());
				}

				dateQuery.put(FieldType.CREATE_DATE.getName(),
						BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(fromDate).toLocalizedPattern())
								.add("$lte", new SimpleDateFormat(currentDate).toLocalizedPattern()).get());

			}
			BasicDBObject dateIndexConditionQuery = new BasicDBObject();
			String startString = new SimpleDateFormat(fromDate).toLocalizedPattern();
			String endString = new SimpleDateFormat(currentDate).toLocalizedPattern();

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
			BasicDBObject dateIndexIn = new BasicDBObject("$in", allDatesIndex);

			if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get(Constants.USE_DATE_INDEX.getValue()))
					&& propertiesManager.propertiesMap.get(Constants.USE_DATE_INDEX.getValue()).equalsIgnoreCase("Y")) {
				dateIndexConditionQuery.append(FieldType.DATE_INDEX.getName(), dateIndexIn);
			}

			if (StringUtils.isNotBlank(merchantPayId) && !merchantPayId.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.PAY_ID.getName(), merchantPayId));
				isParameterised = true;
			}

			if (StringUtils.isNotBlank(subMerchantPayId) && !subMerchantPayId.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.SUB_MERCHANT_ID.getName(), subMerchantPayId));
				isParameterised = true;
			}

			if (!paymentRegion.equalsIgnoreCase("ALL") && StringUtils.isNotBlank(paymentRegion)) {
				isParameterised = true;
				paramConditionLst.add(new BasicDBObject(FieldType.PAYMENTS_REGION.getName(), paymentRegion));
			} else {
			}

			if (!countryCode.equalsIgnoreCase("ALL") && StringUtils.isNotBlank(countryCode)) {
				isParameterised = true;
				paramConditionLst.add(new BasicDBObject(FieldType.INTERNAL_CUST_COUNTRY_NAME.getName(),
						CountryCodes.getCountryName(countryCode)));
			}

			paramConditionLst.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.DENIED_BY_FRAUD.getName()));

			if (!paramConditionLst.isEmpty()) {
				allParamQuery = new BasicDBObject("$and", paramConditionLst);
			}

			List<BasicDBObject> allConditionQueryList = new ArrayList<BasicDBObject>();

			if (!dateQuery.isEmpty()) {
				allConditionQueryList.add(dateQuery);
			}
			if (!dateIndexConditionQuery.isEmpty()) {
				allConditionQueryList.add(dateIndexConditionQuery);
			}
			List<BasicDBObject> fianlList = new ArrayList<BasicDBObject>();

			if (!allConditionQueryList.isEmpty()) {
				BasicDBObject allConditionQueryObj = new BasicDBObject("$and", allConditionQueryList);
				if (!allConditionQueryObj.isEmpty()) {
					fianlList.add(allConditionQueryObj);
				}
			}

			if (!allParamQuery.isEmpty()) {
				fianlList.add(allParamQuery);
			}

			logger.info("Inside TxnReports , Fraud Analytics , fianlList = " + fianlList);
			BasicDBObject finalquery = new BasicDBObject("$and", fianlList);
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns
					.getCollection(PropertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));
			BasicDBObject match1 = new BasicDBObject("$match", finalquery);
			BasicDBObject sort1 = new BasicDBObject("$sort", new BasicDBObject(FieldType.CREATE_DATE.getName(), -1));
			List<BasicDBObject> pipeline = Arrays.asList(match1, sort1);
			AggregateIterable<Document> output = coll.aggregate(pipeline);
			output.allowDiskUse(true);
			MongoCursor<Document> cursor = output.iterator();

			while (cursor.hasNext()) {
				Document dbobj = cursor.next();

				logger.info("fatch data from cursor2");
				TransactionSearch transactionResult = new TransactionSearch();

				// For Merchant Business Name
				String merchantId = dbobj.getString(FieldType.PAY_ID.getName());
				User merchantUser = new User();

				if (userMap.get(merchantId) != null && !userMap.get(merchantId).getPayId().isEmpty()) {
					merchantUser = userMap.get(merchantUser);
				} else {
					merchantUser = userdao.findPayId(merchantId);
					userMap.put(merchantId, merchantUser);
				}

				if (merchantUser != null) {
					transactionResult.setMerchants(merchantUser.getBusinessName());
				} else {
					if (StringUtils.isNotBlank(merchantPayId)) {
						transactionResult.setMerchants(userdao.getBusinessNameByPayId(merchantPayId));
					} else {
						transactionResult.setMerchants(
								userdao.getBusinessNameByPayId(dbobj.getString(FieldType.PAY_ID.getName())));
					}
				}

				// if (!merchantPayId.equalsIgnoreCase("All") &&
				if (dbobj.containsKey(FieldType.SUB_MERCHANT_ID.getName())
						&& StringUtils.isNotBlank(dbobj.getString(FieldType.SUB_MERCHANT_ID.getName()))) {

					String subMerchant = dbobj.getString(FieldType.SUB_MERCHANT_ID.getName());
					User subMerchantUser = new User();

					if (userMap.get(subMerchant) != null && !userMap.get(subMerchant).getPayId().isEmpty()) {
						subMerchantUser = userMap.get(subMerchant);
					} else {
						subMerchantUser = userdao.findPayId(subMerchant);
						userMap.put(subMerchant, subMerchantUser);
					}
					if (subMerchantUser != null) {
						transactionResult.setSubMerchantId(subMerchantUser.getBusinessName());
					} else {
						transactionResult.setSubMerchantId(CrmFieldConstants.NA.getValue());
					}
				}

				transactionResult.setOrderId(dbobj.getString(FieldType.ORDER_ID.getName()));
				transactionResult.setPgRefNum(dbobj.getString(FieldType.PG_REF_NUM.getName()));
				transactionResult.settDate(dbobj.getString(FieldType.CREATE_DATE.getName()));
				transactionResult.setCountry(dbobj.getString(FieldType.INTERNAL_CUST_COUNTRY_NAME.getName()));
				if (StringUtils.isNotBlank(dbobj.getString(FieldType.PAYMENTS_REGION.getName()))) {
					transactionResult.setPaymentRegion(dbobj.getString(FieldType.PAYMENTS_REGION.getName()));
				} else {
					transactionResult.setPaymentRegion(CrmFieldConstants.NA.getValue());
				}
				transactionResult.setPaymentMethods(dbobj.getString(FieldType.PAYMENT_TYPE.getName()));
				transactionResult.setAmount(dbobj.getString(FieldType.AMOUNT.getName()));
				transactionResult.setTotalAmount(dbobj.getString(FieldType.TOTAL_AMOUNT.getName()));
				transactionResult.setStatus(dbobj.getString(FieldType.STATUS.getName()));
				transactionResult.setPgTxnMessage(dbobj.getString(FieldType.PG_TXN_MESSAGE.getName()));
				transactionList.add(transactionResult);
				logger.info("fatch data from transactionList");
			}
			cursor.close();

			logger.info("Total data in impsList is " + transactionList.size());

		} catch (Exception e) {
			logger.error("exception caugth in Fraud Analytics Report function ", e);
		}

		return transactionList;
	}

	@SuppressWarnings({ "static-access", "unused" })
	public List<Enach> getEnachMandateDetailsForReport(String orderId, String umrnNumber, String merchantPayId,
			String subMerchantPayId, String resellerId, String status, String txnType, String fromDate, String toDate) {

		logger.info("Inside getEnachMandateRegistrationDetailsForReport get data from DB for registration report ");
		boolean isParameterised = false;

		String acqCharge = propertiesManager.propertiesMap.get(Constants.ICICI_ENACH_ACQUIRER_CHARGES.getValue());
		List<Enach> eNachList = new ArrayList<Enach>();
		try {
			List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();
			BasicDBObject dateQuery = new BasicDBObject();
			BasicDBObject allParamQuery = new BasicDBObject();

			String currentDate = null;
			if (!fromDate.isEmpty()) {

				if (!toDate.isEmpty()) {
					currentDate = toDate;
				} else {
					DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					Calendar cal = Calendar.getInstance();
					currentDate = dateFormat.format(cal.getTime());
				}

				dateQuery.put(FieldType.CREATE_DATE.getName(),
						BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(fromDate).toLocalizedPattern())
								.add("$lte", new SimpleDateFormat(currentDate).toLocalizedPattern()).get());
			}

			paramConditionLst.add(new BasicDBObject(FieldType.TXNTYPE.getName(), txnType));

			if (StringUtils.isNotBlank(merchantPayId) && !merchantPayId.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.PAY_ID.getName(), merchantPayId));
				isParameterised = true;
			}
			if (StringUtils.isNotBlank(subMerchantPayId) && !subMerchantPayId.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.SUB_MERCHANT_ID.getName(), subMerchantPayId));
				isParameterised = true;
			}

			if (StringUtils.isNotBlank(resellerId) && !resellerId.equalsIgnoreCase("ALL")) {
				isParameterised = true;
				paramConditionLst.add(new BasicDBObject(FieldType.RESELLER_ID.getName(), resellerId));
			}

			if (!orderId.isEmpty()) {
				isParameterised = true;
				paramConditionLst.add(new BasicDBObject(FieldType.ORDER_ID.getName(), orderId));
			}

			if (!umrnNumber.isEmpty()) {
				isParameterised = true;
				paramConditionLst.add(new BasicDBObject(FieldType.UMRN_NUMBER.getName(), umrnNumber));
			}

			if (StringUtils.isNotBlank(status) && !status.equalsIgnoreCase("ALL")) {
				isParameterised = true;
				paramConditionLst.add(new BasicDBObject(FieldType.STATUS.getName(), status));
			} else {
				// paramConditionLst.add(new
				// BasicDBObject(FieldType.STATUS.getName(),
				// StatusType.CAPTURED.getName()));
			}

			if (!paramConditionLst.isEmpty()) {
				allParamQuery = new BasicDBObject("$and", paramConditionLst);
			}

			List<BasicDBObject> allConditionQueryList = new ArrayList<BasicDBObject>();

			if (StringUtils.isNotBlank(umrnNumber) || StringUtils.isNotBlank(orderId)) {

			} else {
				if (!dateQuery.isEmpty()) {
					allConditionQueryList.add(dateQuery);
				}
			}

			List<BasicDBObject> fianlList = new ArrayList<BasicDBObject>();

			if (!allParamQuery.isEmpty()) {
				fianlList.add(allParamQuery);
			}

			if (!allConditionQueryList.isEmpty()) {
				BasicDBObject allConditionQueryObj = new BasicDBObject("$and", allConditionQueryList);
				if (!allConditionQueryObj.isEmpty()) {
					fianlList.add(allConditionQueryObj);
				}
			}

			BasicDBObject finalquery = new BasicDBObject("$and", fianlList);

			logger.info("finalquery = " + finalquery);
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns
					.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.ENACH_COLLECTION.getValue()));
			BasicDBObject match = new BasicDBObject("$match", finalquery);
			BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject("CREATE_DATE", -1));
			List<BasicDBObject> pipeline = Arrays.asList(match, sort);

			AggregateIterable<Document> output = coll.aggregate(pipeline);
			output.allowDiskUse(true);
			MongoCursor<Document> cursor = output.iterator();

			String tempPayId = "";
			String tempOrderId = "";
			try {
				while (cursor.hasNext()) {
					Document dbObj = cursor.next();
					if (StringUtils.isNotBlank(dbObj.getString("IS_ENCRYPTED"))
							&& dbObj.getString("IS_ENCRYPTED").equalsIgnoreCase("Y")) {
						dbObj = dataEncDecTool.decryptDocument(dbObj);
					}
					Enach eNach = new Enach();

					eNach.setMerchantName(userdao.getBusinessNameByPayId(dbObj.getString(FieldType.PAY_ID.getName())));
					eNach.setAcquirerCharges(acqCharge);
					if (dbObj.containsKey(FieldType.SUB_MERCHANT_ID.getName())
							&& StringUtils.isNotBlank(dbObj.getString(FieldType.SUB_MERCHANT_ID.getName()))) {
						eNach.setSubMerchantName(
								userdao.getBusinessNameByPayId(dbObj.getString(FieldType.SUB_MERCHANT_ID.getName())));
						eNach.setSubMerchantPayId(dbObj.getString(FieldType.SUB_MERCHANT_ID.getName()));
					}
					eNach.setPayId(dbObj.getString(FieldType.PAY_ID.getName()));
					eNach.setOrderId(dbObj.getString(FieldType.ORDER_ID.getName()));

					if (tempPayId.equalsIgnoreCase(eNach.getPayId())
							&& tempOrderId.equalsIgnoreCase(eNach.getOrderId())) {
						continue;
					} else {
						tempPayId = eNach.getPayId();
						tempOrderId = eNach.getOrderId();
					}

					if (dbObj.containsKey(FieldType.UMRN_NUMBER.getName())
							&& StringUtils.isNotBlank(dbObj.getString(FieldType.UMRN_NUMBER.getName()))) {
						eNach.setUmrnNumber(dbObj.getString(FieldType.UMRN_NUMBER.getName()));
					} else {
						eNach.setUmrnNumber(Constants.NA.getValue());
					}
					eNach.setCreateDate(dbObj.getString(FieldType.CREATE_DATE.getName()));
					eNach.setAccountHolderName(dbObj.getString(FieldType.ACCOUNT_HOLDER_NAME.getName()));

					if (dbObj.containsKey(FieldType.ACCOUNT_NO.getName())
							&& StringUtils.isNotBlank(dbObj.getString(FieldType.ACCOUNT_NO.getName()))) {
						eNach.setAccountNumber(fields.fieldMask(dbObj.getString(FieldType.ACCOUNT_NO.toString())));
					} else {
						eNach.setAccountNumber(Constants.NA.getValue());
					}

					if (dbObj.containsKey(FieldType.IFSC_CODE.getName())
							&& StringUtils.isNotBlank(dbObj.getString(FieldType.IFSC_CODE.getName()))) {
						eNach.setIfscCode(fields.fieldMask(dbObj.getString(FieldType.IFSC_CODE.getName())));
					} else {
						eNach.setIfscCode(Constants.NA.getValue());
					}

					if (dbObj.containsKey(FieldType.CUST_PHONE.getName())
							&& StringUtils.isNotBlank(dbObj.getString(FieldType.CUST_PHONE.getName()))) {
						eNach.setCustPhone(fields.fieldMask(dbObj.getString(FieldType.CUST_PHONE.getName())));
					} else {
						eNach.setCustPhone(Constants.NA.getValue());
					}

					if (dbObj.containsKey(FieldType.CUST_EMAIL.getName())
							&& StringUtils.isNotBlank(dbObj.getString(FieldType.CUST_EMAIL.getName()))) {
						eNach.setCustEmail(fields.maskEmail(dbObj.getString(FieldType.CUST_EMAIL.getName())));
					} else {
						eNach.setCustEmail(Constants.NA.getValue());
					}

					eNach.setPgRefNum(dbObj.getString(FieldType.PG_REF_NUM.getName()));
					eNach.setPaymentType(PaymentType.getpaymentName(dbObj.getString(FieldType.PAYMENT_TYPE.getName())));
					eNach.setBankName(dbObj.getString(FieldType.BANK_NAME.getName()));
					eNach.setFrequency(Frequency.getFrequencyName(dbObj.getString(FieldType.FREQUENCY.getName())));
					eNach.setTenure(dbObj.getString(FieldType.TENURE.getName()));
					eNach.setAmount(dbObj.getString(FieldType.AMOUNT.getName()));
					eNach.setStartDate(dbObj.getString(FieldType.DATEFROM.getName()));
					eNach.setEndDate(dbObj.getString(FieldType.DATETO.getName()));
					eNach.setMaxAmount(dbObj.getString(FieldType.MONTHLY_AMOUNT.getName()));
					eNach.setTotalAmount(dbObj.getString(FieldType.TOTAL_AMOUNT.getName()));
					eNach.setStatus(dbObj.getString(FieldType.STATUS.getName()));
					eNach.setDueDate(dbObj.getString(FieldType.DUE_DATE.getName()));

					if (dbObj.containsKey(FieldType.RESPONSE_MESSAGE.getName())
							&& StringUtils.isNotBlank(dbObj.getString(FieldType.RESPONSE_MESSAGE.getName()))) {
						eNach.setResponseMessage(dbObj.getString(FieldType.RESPONSE_MESSAGE.getName()));
					} else {
						eNach.setResponseMessage(Constants.NA.getValue());
					}

					if (dbObj.containsKey(FieldType.EMANDATE_URL.getName())
							&& StringUtils.isNotBlank(dbObj.getString(FieldType.EMANDATE_URL.getName()))) {
						eNach.seteMandateUrl(dbObj.getString(FieldType.EMANDATE_URL.getName()));
					} else {
						eNach.seteMandateUrl(Constants.NA.getValue());
					}

					if (dbObj.containsKey(FieldType.DEBIT_DATE.getName())
							&& StringUtils.isNotBlank(dbObj.getString(FieldType.DEBIT_DATE.getName()))) {
						eNach.setDebitDate(dbObj.getString(FieldType.DEBIT_DATE.getName()));
					} else {
						eNach.setDebitDate(Constants.NA.getValue());
					}
					/*
					 * Fields fields = new Fields(); fields.put(FieldType.AMOUNT.getName(),
					 * Amount.removeDecimalAmount(dbObj.getString(FieldType. AMOUNT.getName()),
					 * "356")); fields.put(FieldType.PAY_ID.getName(),
					 * dbObj.getString(FieldType.PAY_ID.getName()));
					 * fields.put(FieldType.ORDER_ID.getName(),
					 * dbObj.getString(FieldType.ORDER_ID.getName()));
					 * eNach.setStatusEnquiryHash(Hasher.getHash(fields));
					 */

					eNachList.add(eNach);
				}
			} finally {
				cursor.close();
			}
		} catch (Exception exception) {
			String message = "Error fetching getEnachMandateDetailsForReport from database ";
			logger.error(message, exception);
		}
		return eNachList;
	}

	@SuppressWarnings("unused")
	public List<Enach> getEnachTransactionReport(String orderId, String pgRefNum) {

		logger.info("inside eNach debitTransactionReport to get all debit txn");
		String tenure = null;
		String payId = null;
		String debitAmount = null;
		String regPgRefNum = null;
		String regDate = null;
		String custName = null;
		String custEmail = null;
		String custMobile = null;
		String totalAmount = null;
		String paymentType = null;

		List<Enach> eNachDebitList = new ArrayList<Enach>();
		Map<String, String> debitTxnMap = new HashMap<String, String>();

		List<BasicDBObject> queryList = new ArrayList<BasicDBObject>();
		queryList.add(new BasicDBObject(FieldType.ORDER_ID.getName(), orderId));

		if (StringUtils.isNotBlank(pgRefNum)) {
			queryList.add(new BasicDBObject(FieldType.PG_REF_NUM.getName(), pgRefNum));
		}

		queryList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.CAPTURED.getName()));
		queryList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), "Registration"));

		BasicDBObject query = new BasicDBObject("$and", queryList);
		logger.info("Inside TxnReports , eNach Registration, query = " + query);
		MongoDatabase dbIns = mongoInstance.getDB();
		MongoCollection<Document> coll = dbIns
				.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.ENACH_COLLECTION.getValue()));
		BasicDBObject match = new BasicDBObject("$match", query);
		BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject("CREATE_DATE", -1));
		List<BasicDBObject> pipeline = Arrays.asList(match, sort);

		AggregateIterable<Document> output = coll.aggregate(pipeline);
		output.allowDiskUse(true);
		MongoCursor<Document> cursor = output.iterator();
		while (cursor.hasNext()) {
			Document dbObj = cursor.next();
			if (StringUtils.isNotBlank(dbObj.getString("IS_ENCRYPTED"))
					&& dbObj.getString("IS_ENCRYPTED").equalsIgnoreCase("Y")) {
				dbObj = dataEncDecTool.decryptDocument(dbObj);
			}
			tenure = dbObj.getString(FieldType.TENURE.getName());
			payId = dbObj.getString(FieldType.PAY_ID.getName());
			debitAmount = dbObj.getString(FieldType.MONTHLY_AMOUNT.getName());
			regPgRefNum = dbObj.getString(FieldType.PG_REF_NUM.getName());
			regDate = dbObj.getString(FieldType.CREATE_DATE.getName());
			custName = dbObj.getString(FieldType.ACCOUNT_HOLDER_NAME.getName());
			custEmail = dbObj.getString(FieldType.CUST_EMAIL.getName());
			custMobile = dbObj.getString(FieldType.CUST_PHONE.getName());
			totalAmount = dbObj.getString(FieldType.TOTAL_AMOUNT.getName());
			paymentType = dbObj.getString(FieldType.PAYMENT_TYPE.getName());

		}

		List<BasicDBObject> saleQueryList = new ArrayList<BasicDBObject>();
		saleQueryList.add(new BasicDBObject(FieldType.ORDER_ID.getName(), orderId));
		saleQueryList.add(new BasicDBObject(FieldType.PAY_ID.getName(), payId));
		saleQueryList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), "Sale"));

		BasicDBObject finalQuery = new BasicDBObject("$and", saleQueryList);
		logger.info("Inside TxnReports , eNach Debit Transaction , finalquery = " + finalQuery);
		match = new BasicDBObject("$match", finalQuery);
		sort = new BasicDBObject("$sort", new BasicDBObject("CREATE_DATE", -1));
		pipeline = Arrays.asList(match, sort);
		output = coll.aggregate(pipeline);
		output.allowDiskUse(true);
		cursor = output.iterator();
		while (cursor.hasNext()) {
			Document dbObj = cursor.next();
			if (!debitTxnMap.containsKey(dbObj.getString(FieldType.PG_REF_NUM.getName()))) {
				debitTxnMap.put(dbObj.getString(FieldType.PG_REF_NUM.getName()),
						dbObj.getString(FieldType.TXN_ID.getName()));
			}
		}

		List<BasicDBObject> debitQueryList = new ArrayList<BasicDBObject>();

		debitQueryList.add(new BasicDBObject(FieldType.PAY_ID.getName(), payId));
		debitQueryList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), "Sale"));

		List<BasicDBObject> txnTypeConditionLst = new ArrayList<BasicDBObject>();
		BasicDBObject txnTypeQuery = new BasicDBObject();

		for (Map.Entry<String, String> txnId : debitTxnMap.entrySet()) {
			txnTypeConditionLst.add(new BasicDBObject(FieldType.TXN_ID.getName(), txnId.getValue()));
		}

		txnTypeQuery.append("$or", txnTypeConditionLst);

		debitQueryList.add(txnTypeQuery);

		BasicDBObject debitFinalQuery = new BasicDBObject("$and", debitQueryList);
		logger.info("Inside TxnReports , eNach Debit Transaction , debitFinalQuery = " + debitFinalQuery);
		match = new BasicDBObject("$match", debitFinalQuery);
		sort = new BasicDBObject("$sort", new BasicDBObject("CREATE_DATE", -1));
		pipeline = Arrays.asList(match, sort);
		output = coll.aggregate(pipeline);
		output.allowDiskUse(true);
		cursor = output.iterator();

		while (cursor.hasNext()) {

			Document dbObj = cursor.next();
			Enach eNach = new Enach();

			if (dbObj.containsKey(FieldType.SUB_MERCHANT_ID.getName())
					&& StringUtils.isNotBlank(dbObj.getString(FieldType.SUB_MERCHANT_ID.getName()))) {
				eNach.setSubMerchantName(
						userdao.getBusinessNameByPayId(dbObj.getString(FieldType.SUB_MERCHANT_ID.getName())));
				eNach.setSubMerchantPayId(dbObj.getString(FieldType.SUB_MERCHANT_ID.getName()));
			}

			if (dbObj.containsKey(FieldType.UMRN_NUMBER.getName())
					&& StringUtils.isNotBlank(dbObj.getString(FieldType.UMRN_NUMBER.getName()))) {
				eNach.setUmrnNumber(dbObj.getString(FieldType.UMRN_NUMBER.getName()));
			} else {
				eNach.setUmrnNumber(Constants.NA.getValue());
			}

			eNach.setMerchantName(userdao.getBusinessNameByPayId(dbObj.getString(FieldType.PAY_ID.getName())));
			eNach.setPayId(dbObj.getString(FieldType.PAY_ID.getName()));
			eNach.setOrderId(dbObj.getString(FieldType.ORDER_ID.getName()));
			eNach.setRegPgRefNum(regPgRefNum);
			eNach.setRegDate(regDate);
			eNach.setTotalAmount(totalAmount);

			eNach.setPgRefNum(dbObj.getString(FieldType.PG_REF_NUM.getName()));
			eNach.setAccountHolderName(custName);
			eNach.setCustEmail(fields.maskEmail(custEmail));
			eNach.setCustPhone(fields.fieldMask(custMobile));
			eNach.setAmount(dbObj.getString(FieldType.AMOUNT.getName()));
			eNach.setMaxAmount(dbObj.getString(FieldType.AMOUNT.getName()));
			eNach.setCreateDate(dbObj.getString(FieldType.CREATE_DATE.getName()));
			eNach.setStatus(dbObj.getString(FieldType.STATUS.getName()));
			eNach.setPaymentType(PaymentType.getpaymentName(paymentType));

			if (dbObj.containsKey(FieldType.DEBIT_DATE.getName())
					&& StringUtils.isNotBlank(dbObj.getString(FieldType.DEBIT_DATE.getName()))) {
				eNach.setDebitDate(dbObj.getString(FieldType.DEBIT_DATE.getName()));
			} else {
				eNach.setDebitDate(Constants.NA.getValue());
			}
			if (dbObj.containsKey(FieldType.CREATE_DATE.getName())
					&& StringUtils.isNotBlank(dbObj.getString(FieldType.CREATE_DATE.getName()))) {
				eNach.setCreateDate(dbObj.getString(FieldType.CREATE_DATE.getName()));
			} else {
				eNach.setCreateDate(Constants.NA.getValue());
			}
			eNach.setDueDate(dbObj.getString(FieldType.DUE_DATE.getName()));

			eNachDebitList.add(eNach);
		}
		return eNachDebitList;
	}

	public Fields getEnachRegistrationDetails(String orderId, String merchantPayId, String pgRefNum) {

		Fields fields = new Fields();
		logger.info("inside TxnReport getEnachRegistrationDetails function");
		try {

			List<BasicDBObject> queryList = new ArrayList<BasicDBObject>();
			queryList.add(new BasicDBObject(FieldType.ORDER_ID.getName(), orderId));
			queryList.add(new BasicDBObject(FieldType.PAY_ID.getName(), merchantPayId));
			queryList.add(new BasicDBObject(FieldType.PG_REF_NUM.getName(), pgRefNum));
			queryList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), "Sale"));

			BasicDBObject query = new BasicDBObject("$and", queryList);

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns
					.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.ENACH_COLLECTION.getValue()));
			MongoCursor<Document> cursor = coll.find(query).sort(new BasicDBObject(FieldType.CREATE_DATE.getName(), -1))
					.iterator();

			while (cursor.hasNext()) {
				Document doc = cursor.next();

				if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
						&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
					doc = dataEncDecTool.decryptDocument(doc);
				}

				fields.put(FieldType.PG_REF_NUM.getName(), doc.getString(FieldType.PG_REF_NUM.getName()));
				fields.put(FieldType.MONTHLY_AMOUNT.getName(), doc.getString(FieldType.MONTHLY_AMOUNT.getName()));
				fields.put(FieldType.TXNTYPE.getName(), "Sale");
				fields.put(FieldType.DATEFROM.getName(), doc.getString(FieldType.DATEFROM.getName()));
				fields.put(FieldType.DATETO.getName(), doc.getString(FieldType.DATETO.getName()));
				fields.put(FieldType.CURRENCY.getName(), doc.getString(FieldType.CURRENCY.getName()));
				fields.put(FieldType.AMOUNT.getName(), doc.getString(FieldType.AMOUNT.getName()));
				fields.put(FieldType.MONTHLY_AMOUNT.getName(), doc.getString(FieldType.MONTHLY_AMOUNT.getName()));
				fields.put(FieldType.TOTAL_AMOUNT.getName(), doc.getString(FieldType.TOTAL_AMOUNT.getName()));
				fields.put(FieldType.PAYMENT_TYPE.getName(), doc.getString(FieldType.PAYMENT_TYPE.getName()));
				fields.put(FieldType.CUST_PHONE.getName(), doc.getString(FieldType.CUST_PHONE.getName()));
				fields.put(FieldType.CUST_EMAIL.getName(), doc.getString(FieldType.CUST_EMAIL.getName()));
				fields.put(FieldType.AMOUNT_TYPE.getName(), doc.getString(FieldType.AMOUNT_TYPE.getName()));
				fields.put(FieldType.FREQUENCY.getName(), doc.getString(FieldType.FREQUENCY.getName()));
				fields.put(FieldType.ORIG_TXN_ID.getName(), doc.getString(FieldType.ORIG_TXN_ID.getName()));
				fields.put(FieldType.TENURE.getName(), doc.getString(FieldType.TENURE.getName()));
				fields.put(FieldType.BANK_NAME.getName(), doc.getString(FieldType.BANK_NAME.getName()));
				fields.put(FieldType.UMRN_NUMBER.getName(), doc.getString(FieldType.UMRN_NUMBER.getName()));
				fields.put("MANDATE_REGISTRATION_ID", doc.getString("MANDATE_REGISTRATION_ID"));
				fields.put(FieldType.ORDER_ID.getName(), doc.getString(FieldType.ORDER_ID.getName()));
				fields.put("COM_AMT", doc.getString("COM_AMT"));
				fields.put(FieldType.PAY_ID.getName(), doc.getString(FieldType.PAY_ID.getName()));
				fields.put(FieldType.REGISTRATION_DATE.getName(), doc.getString(FieldType.REGISTRATION_DATE.getName()));
				fields.put(FieldType.DUE_DATE.getName(), doc.getString(FieldType.DUE_DATE.getName()));

				if (doc.containsKey(FieldType.SUB_MERCHANT_ID.getName())
						&& StringUtils.isNotBlank(doc.getString(FieldType.SUB_MERCHANT_ID.getName()))) {
					fields.put(FieldType.SUB_MERCHANT_ID.getName(), doc.getString(FieldType.SUB_MERCHANT_ID.getName()));
				}

				if (doc.containsKey(FieldType.RESELLER_ID.getName())
						&& StringUtils.isNotBlank(doc.getString(FieldType.RESELLER_ID.getName()))) {
					fields.put(FieldType.RESELLER_ID.getName(), doc.getString(FieldType.RESELLER_ID.getName()));
				}

				if (doc.getString(FieldType.PAYMENT_TYPE.getName()).equalsIgnoreCase("NB")
						|| doc.getString(FieldType.PAYMENT_TYPE.getName()).equalsIgnoreCase("DC")) {

					fields.put(FieldType.ACCOUNT_NO.toString(), doc.getString(FieldType.ACCOUNT_NO.toString()));
					fields.put(FieldType.IFSC_CODE.getName(), doc.getString(FieldType.IFSC_CODE.getName()));
					fields.put(FieldType.ACCOUNT_HOLDER_NAME.getName(),
							doc.getString(FieldType.ACCOUNT_HOLDER_NAME.getName()));
					fields.put(FieldType.ACCOUNT_TYPE.getName(), doc.getString(FieldType.ACCOUNT_TYPE.getName()));

				} /*
					 * else {
					 * 
					 * fields.put(FieldType.CARD_MASK.getName(),
					 * doc.getString(FieldType.CARD_MASK.getName())); fields.put("EXP_MONTH",
					 * doc.getString("EXP_MONTH")); fields.put("EXP_YEAR",
					 * doc.getString("EXP_YEAR")); fields.put(FieldType.CARD_HOLDER_NAME.getName(),
					 * doc.getString(FieldType.CARD_HOLDER_NAME.getName()));
					 * fields.put(FieldType.MOP_TYPE.getName(),
					 * doc.getString(FieldType.MOP_TYPE.getName()));
					 * 
					 * }
					 */

			}
		} catch (Exception ex) {
			logger.info("caught exception getEnachRegistrationDetails : ", ex);
		}

		return fields;
	}

	public List<String> getAllCapturedRegistrationOrderId(String orderId, String umrnNumber, String merchantPayId,
			String subMerchantPayId, String resellerId, String status, String txnType, String dateFrom, String dateTo) {

		logger.info("Inside getAllCapturedRegistrationOrderId get captured registration orderId ");
		boolean isParameterised = false;

		List<String> orderIdList = new ArrayList<String>();
		try {
			List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();
			BasicDBObject dateQuery = new BasicDBObject();
			BasicDBObject allParamQuery = new BasicDBObject();

			String currentDate = null;
			if (!dateFrom.isEmpty()) {

				if (!dateTo.isEmpty()) {
					currentDate = dateTo;
				} else {
					DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					Calendar cal = Calendar.getInstance();
					currentDate = dateFormat.format(cal.getTime());
				}

				dateQuery.put(FieldType.CREATE_DATE.getName(),
						BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(dateFrom).toLocalizedPattern())
								.add("$lte", new SimpleDateFormat(currentDate).toLocalizedPattern()).get());
			}

			paramConditionLst.add(new BasicDBObject(FieldType.TXNTYPE.getName(), txnType));

			if (StringUtils.isNotBlank(merchantPayId) && !merchantPayId.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.PAY_ID.getName(), merchantPayId));
				isParameterised = true;
			}
			if (StringUtils.isNotBlank(subMerchantPayId) && !subMerchantPayId.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.SUB_MERCHANT_ID.getName(), subMerchantPayId));
				isParameterised = true;
			}
			if (StringUtils.isNotBlank(resellerId) && !resellerId.equalsIgnoreCase("ALL")) {
				isParameterised = true;
				paramConditionLst.add(new BasicDBObject(FieldType.RESELLER_ID.getName(), resellerId));
			}
			if (!orderId.isEmpty()) {
				isParameterised = true;
				paramConditionLst.add(new BasicDBObject(FieldType.ORDER_ID.getName(), orderId));
			}

			if (!umrnNumber.isEmpty()) {
				isParameterised = true;
				paramConditionLst.add(new BasicDBObject(FieldType.UMRN_NUMBER.getName(), umrnNumber));
			}

			if (StringUtils.isNotBlank(status) && !status.equalsIgnoreCase("ALL")) {
				isParameterised = true;
				paramConditionLst.add(new BasicDBObject(FieldType.STATUS.getName(), status));
			}

			if (!paramConditionLst.isEmpty()) {
				allParamQuery = new BasicDBObject("$and", paramConditionLst);
			}

			List<BasicDBObject> allConditionQueryList = new ArrayList<BasicDBObject>();

			if (StringUtils.isNotBlank(umrnNumber) || StringUtils.isNotBlank(orderId)) {

			} else {
				if (!dateQuery.isEmpty()) {
					allConditionQueryList.add(dateQuery);
				}
			}

			List<BasicDBObject> fianlList = new ArrayList<BasicDBObject>();

			if (!allParamQuery.isEmpty()) {
				fianlList.add(allParamQuery);
			}

			if (!allConditionQueryList.isEmpty()) {
				BasicDBObject allConditionQueryObj = new BasicDBObject("$and", allConditionQueryList);
				if (!allConditionQueryObj.isEmpty()) {
					fianlList.add(allConditionQueryObj);
				}
			}

			BasicDBObject finalquery = new BasicDBObject("$and", fianlList);

			logger.info("Inside TxnReports , searchPayment , finalquery = " + finalquery);
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns
					.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.ENACH_COLLECTION.getValue()));
			BasicDBObject match = new BasicDBObject("$match", finalquery);
			BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject(FieldType.CREATE_DATE.getName(), -1));

			List<BasicDBObject> pipeline = Arrays.asList(match, sort);

			AggregateIterable<Document> output = coll.aggregate(pipeline);
			output.allowDiskUse(true);
			MongoCursor<Document> cursor = output.iterator();
			while (cursor.hasNext()) {
				Document dbobj = cursor.next();
				orderIdList.add(dbobj.getString(FieldType.ORDER_ID.getName()));
			}
		} catch (Exception exception) {
			String message = "Error fetching getAllCapturedRegistrationOrderId from database ";
			logger.error(message, exception);
		}

		return orderIdList;
	}

	public List<TransactionSearch> txnRecordMonthlyInvoice(String payId, String subMerchantPayId, String datefrom,
			String dateto) {
		List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();
		List<TransactionSearch> datacharges = new ArrayList<TransactionSearch>();
		BasicDBObject dateQuery = new BasicDBObject();

		dateQuery.put(FieldType.CREATE_DATE.getName(),
				BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(datefrom).toLocalizedPattern())
						.add("$lt", new SimpleDateFormat(dateto).toLocalizedPattern()).get());
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.ENGLISH);
		Date dateStart = null;
		Date dateEnd = null;
		try {
			dateStart = format.parse(datefrom);
			dateEnd = format.parse(dateto);
		} catch (ParseException e) {
			logger.error("Exception in date parsing = ", e);
		}

		LocalDate incrementingDate = dateStart.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		LocalDate endDate = dateEnd.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

		List<String> allDatesIndex = new ArrayList<>();

		while (!incrementingDate.isAfter(endDate)) {
			allDatesIndex.add(incrementingDate.toString().replaceAll("-", ""));
			incrementingDate = incrementingDate.plusDays(1);
		}
		BasicDBObject dateIndexIn = new BasicDBObject("$in", allDatesIndex);
		// BasicDBObject statusQuery = new BasicDBObject();
		// BasicDBObject txntypeQuery = new BasicDBObject();
		BasicDBObject dateIndexConditionQuery = new BasicDBObject(FieldType.DATE_INDEX.getName(), dateIndexIn);
		BasicDBObject payIdQuery = new BasicDBObject(FieldType.PAY_ID.getName(), payId);

		// if
		// (payId.equals(PropertiesManager.propertiesMap.get("MSEDCL_PAY_ID")))
		// {
		BasicDBObject statusQuery = new BasicDBObject(FieldType.STATUS.getName(), "Captured");
		BasicDBObject txntypeQuery = new BasicDBObject(FieldType.TXNTYPE.getName(), "SALE");
		// } else {

		// statusQuery = new BasicDBObject(FieldType.STATUS.getName(),
		// "Settled");
		// txntypeQuery = new BasicDBObject(FieldType.TXNTYPE.getName(),
		// "RECO");
		// }

		if (StringUtils.isNotBlank(subMerchantPayId) && !subMerchantPayId.equalsIgnoreCase("All")) {
			BasicDBObject submerchantpayIdQuery = new BasicDBObject(FieldType.SUB_MERCHANT_ID.getName(),
					subMerchantPayId);
			paramConditionLst.add(submerchantpayIdQuery);
		}
		if (!dateQuery.isEmpty()) {
			paramConditionLst.add(dateQuery);
		}
		paramConditionLst.add(dateIndexConditionQuery);
		paramConditionLst.add(payIdQuery);
		paramConditionLst.add(statusQuery);
		paramConditionLst.add(txntypeQuery);
		BasicDBObject finalquery = new BasicDBObject("$and", paramConditionLst);
		logger.info("final query for monthly invoice " + finalquery);

		MongoDatabase dbIns = mongoInstance.getDB();
		MongoCollection<Document> coll = dbIns
				.getCollection(PropertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));
		BasicDBObject projectElement = new BasicDBObject();
		projectElement.put(FieldType.PG_TDR_SC.getName(), 1);
		projectElement.put(FieldType.PG_GST.getName(), 1);
		projectElement.put(FieldType.ACQUIRER_TDR_SC.getName(), 1);
		projectElement.put(FieldType.ACQUIRER_GST.getName(), 1);
		projectElement.put(FieldType.PAY_ID.getName(), 1);
		projectElement.put(FieldType.ORIG_TXNTYPE.getName(), 1);
		projectElement.put(FieldType.PAYMENT_TYPE.getName(), 1);
		projectElement.put(FieldType.MOP_TYPE.getName(), 1);
		projectElement.put(FieldType.AMOUNT.getName(), 1);
		projectElement.put(FieldType.PAYMENTS_REGION.getName(), 1);
		projectElement.put(FieldType.IS_ENCRYPTED.getName(), 1);

		BasicDBObject project = new BasicDBObject("$project", projectElement);

		BasicDBObject match = new BasicDBObject("$match", finalquery);

		List<BasicDBObject> pipeline = Arrays.asList(match, project);

		AggregateIterable<Document> output = coll.aggregate(pipeline);
		output.allowDiskUse(true);
		MongoCursor<Document> cursor = output.iterator();
		while (cursor.hasNext()) {

			Document dbobj = cursor.next();

			TransactionSearch obj = new TransactionSearch();
			if (StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.getName()))
					|| StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.getName()))) {
				obj.setTdr_Surcharge(String.valueOf(new BigDecimal(dbobj.getString(FieldType.PG_TDR_SC.getName()))
						.add(new BigDecimal(dbobj.getString(FieldType.ACQUIRER_TDR_SC.getName())))));
			} else {
				obj.setTdr_Surcharge("0.00");
			}
			if (StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.getName()))
					|| StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.getName()))) {
				obj.setGst_charge(String.valueOf(new BigDecimal(dbobj.getString(FieldType.PG_GST.getName()))
						.add(new BigDecimal(dbobj.getString(FieldType.ACQUIRER_GST.getName())))));
			} else {
				obj.setGst_charge("0.00");
			}
			obj.setPayId(dbobj.getString(FieldType.PAY_ID.getName()));
			obj.setOrigTxnType(dbobj.getString(FieldType.ORIG_TXNTYPE.getName()));
			String paymentType = dbobj.getString(FieldType.PAYMENT_TYPE.getName());
			if (paymentType == null) {
			} else {
				switch (paymentType) {
				case "NB":
					obj.setPaymentMethods(PaymentType.getpaymentName(paymentType));
					break;

				case "UP":
					obj.setPaymentMethods(PaymentType.getpaymentName(paymentType));
					break;

				case "WL":
					obj.setPaymentMethods(PaymentType.getpaymentName(paymentType));
					break;
				case "CD":
					obj.setPaymentMethods(PaymentType.getpaymentName(paymentType));
					break;

				case "CC":
					obj.setPaymentMethods(PaymentType.getpaymentName(paymentType));
					break;

				case "DC":
					obj.setPaymentMethods(PaymentType.getpaymentName(paymentType));
					break;

				case "PC":
					obj.setPaymentMethods(PaymentType.getpaymentName(paymentType));
					break;

				case "AD":
					obj.setPaymentMethods(PaymentType.getpaymentName(paymentType));
					break;

				case "DP":
					obj.setPaymentMethods(PaymentType.getpaymentName(paymentType));
					break;

				case "EX":
					obj.setPaymentMethods(PaymentType.getpaymentName(paymentType));
					break;

				case "RP":
					obj.setPaymentMethods(PaymentType.getpaymentName(paymentType));
					break;

				case "EM":
					obj.setPaymentMethods(PaymentType.getpaymentName(paymentType));
					break;

				case "NEFT":
					obj.setPaymentMethods(PaymentType.getpaymentName(paymentType));
					break;

				case "IMPS":
					obj.setPaymentMethods(PaymentType.getpaymentName(paymentType));
					break;

				case "RTGS":
					obj.setPaymentMethods(PaymentType.getpaymentName(paymentType));
					break;
				}
			}
			String mopType = dbobj.getString(FieldType.MOP_TYPE.getName());
			if (StringUtils.isBlank(mopType)) {
				mopType = "NA";
			}
			if (StringUtils.isNumeric(mopType)
					|| (StringUtils.isNotBlank(paymentType)) && paymentType.equalsIgnoreCase("WL")) {
				obj.setMopType(MopType.getmopName(mopType));
			} else {
				switch (mopType) {
				case "VI":
					obj.setMopType(MopType.getmopName(mopType));
					break;

				case "MC":
					obj.setMopType(MopType.getmopName(mopType));
					break;

				case "RU":
					obj.setMopType(MopType.getmopName(mopType));
					break;

				case "DN":
					obj.setMopType(MopType.getmopName(mopType));
					break;

				case "CD":
					obj.setMopType(MopType.getmopName(mopType));
					break;

				case "UP":
					obj.setMopType(MopType.getmopName(mopType));
					break;

				case "WL":
					obj.setMopType(MopType.getmopName(mopType));
					break;

				case "NB":
					obj.setMopType(MopType.getmopName(mopType));
					break;

				case "NEFT":
					obj.setMopType(MopType.getmopName(mopType));
					break;
				case "IMPS":
					obj.setMopType(MopType.getmopName(mopType));
					break;
				case "RTGS":
					obj.setMopType(MopType.getmopName(mopType));
					break;
				}
			}
			obj.setAmount(dbobj.getString(FieldType.AMOUNT.getName()));
			obj.setPaymentRegion(dbobj.getString(FieldType.PAYMENTS_REGION.getName()));

			datacharges.add(obj);
		}

		return datacharges;

	}

	@SuppressWarnings({ "static-access", "unused" })
	public List<Enach> getDownloadEnachTransactionReport(String orderId, String umrnNumber, String merchantPayId,
			String subMerchantPayId, String resellerId, String status, String txnType, String fromDate, String toDate) {

		logger.info("Inside getEnachMandateRegistrationDetailsForReport get data from DB for registration report ");
		boolean isParameterised = false;
		String acqCharges = propertiesManager.propertiesMap.get(Constants.ICICI_ENACH_ACQUIRER_CHARGES.getValue());

		List<Enach> eNachList = new ArrayList<Enach>();
		try {
			List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();
			BasicDBObject dateQuery = new BasicDBObject();
			BasicDBObject allParamQuery = new BasicDBObject();

			String currentDate = null;
			if (!fromDate.isEmpty()) {

				if (!toDate.isEmpty()) {
					currentDate = toDate;
				} else {
					DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					Calendar cal = Calendar.getInstance();
					currentDate = dateFormat.format(cal.getTime());
				}

				dateQuery.put(FieldType.CREATE_DATE.getName(),
						BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(fromDate).toLocalizedPattern())
								.add("$lte", new SimpleDateFormat(currentDate).toLocalizedPattern()).get());
			}

			paramConditionLst.add(new BasicDBObject(FieldType.TXNTYPE.getName(), txnType));

			if (StringUtils.isNotBlank(merchantPayId) && !merchantPayId.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.PAY_ID.getName(), merchantPayId));
				isParameterised = true;
			}
			if (StringUtils.isNotBlank(subMerchantPayId) && !subMerchantPayId.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.SUB_MERCHANT_ID.getName(), subMerchantPayId));
				isParameterised = true;
			}

			if (StringUtils.isNotBlank(resellerId) && !resellerId.equalsIgnoreCase("ALL")) {
				isParameterised = true;
				paramConditionLst.add(new BasicDBObject(FieldType.RESELLER_ID.getName(), resellerId));
			}

			if (!orderId.isEmpty()) {
				isParameterised = true;
				paramConditionLst.add(new BasicDBObject(FieldType.ORDER_ID.getName(), orderId));
			}

			if (!umrnNumber.isEmpty()) {
				isParameterised = true;
				paramConditionLst.add(new BasicDBObject(FieldType.UMRN_NUMBER.getName(), umrnNumber));
			}

			if (StringUtils.isNotBlank(status) && !status.equalsIgnoreCase("ALL")) {
				isParameterised = true;
				paramConditionLst.add(new BasicDBObject(FieldType.STATUS.getName(), status));
			} else {
				// paramConditionLst.add(new
				// BasicDBObject(FieldType.STATUS.getName(),
				// StatusType.CAPTURED.getName()));
			}

			if (!paramConditionLst.isEmpty()) {
				allParamQuery = new BasicDBObject("$and", paramConditionLst);
			}

			List<BasicDBObject> allConditionQueryList = new ArrayList<BasicDBObject>();

			if (StringUtils.isNotBlank(umrnNumber) || StringUtils.isNotBlank(orderId)) {

			} else {
				if (!dateQuery.isEmpty()) {
					allConditionQueryList.add(dateQuery);
				}
			}

			List<BasicDBObject> fianlList = new ArrayList<BasicDBObject>();

			if (!allParamQuery.isEmpty()) {
				fianlList.add(allParamQuery);
			}

			if (!allConditionQueryList.isEmpty()) {
				BasicDBObject allConditionQueryObj = new BasicDBObject("$and", allConditionQueryList);
				if (!allConditionQueryObj.isEmpty()) {
					fianlList.add(allConditionQueryObj);
				}
			}

			BasicDBObject finalquery = new BasicDBObject("$and", fianlList);

			logger.info("Inside TxnReports , getEnachMandateDetailsForReport , finalquery = " + finalquery);
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns
					.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.ENACH_COLLECTION.getValue()));
			BasicDBObject match = new BasicDBObject("$match", finalquery);
			BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject("CREATE_DATE", -1));
			/*
			 * BasicDBObject skip = new BasicDBObject("$skip", start); BasicDBObject limit =
			 * new BasicDBObject("$limit", length);
			 */

			List<BasicDBObject> pipeline = Arrays.asList(match, sort/* , skip, limit */);

			AggregateIterable<Document> output = coll.aggregate(pipeline);
			output.allowDiskUse(true);
			MongoCursor<Document> cursor = output.iterator();

			while (cursor.hasNext()) {

				Document doc = cursor.next();
				if (StringUtils.isNotBlank(doc.getString("IS_ENCRYPTED"))
						&& doc.getString("IS_ENCRYPTED").equalsIgnoreCase("Y")) {
					doc = dataEncDecTool.decryptDocument(doc);
				}
				Enach eNach = new Enach();

				eNach.setAcquirerCharges(acqCharges);
				eNach.setOrderId(doc.getString(FieldType.ORDER_ID.getName()));
				eNach.setPgRefNum(doc.getString(FieldType.PG_REF_NUM.getName()));
				eNach.setStatus(doc.getString(FieldType.STATUS.getName()));

				if (doc.containsKey(FieldType.SUB_MERCHANT_ID.getName())
						&& StringUtils.isNotBlank(doc.getString(FieldType.SUB_MERCHANT_ID.getName()))) {
					eNach.setSubMerchantName(
							userdao.getBusinessNameByPayId(doc.getString(FieldType.SUB_MERCHANT_ID.getName())));
					eNach.setSubMerchantPayId(doc.getString(FieldType.SUB_MERCHANT_ID.getName()));
				}

				if (doc.containsKey(FieldType.RESELLER_ID.getName())
						&& StringUtils.isNotBlank(doc.getString(FieldType.RESELLER_ID.getName()))) {
					User reseller = userdao.findUserByResellerId(doc.getString(FieldType.RESELLER_ID.getName()));
					eNach.setResellerName(reseller.getBusinessName());

					eNach.setResellerPayId(reseller.getPayId());
				}

				if (doc.containsKey(FieldType.UMRN_NUMBER.getName())
						&& StringUtils.isNotBlank(doc.getString(FieldType.UMRN_NUMBER.getName()))) {
					eNach.setUmrnNumber(doc.getString(FieldType.UMRN_NUMBER.getName()));
				} else {
					eNach.setUmrnNumber(Constants.NA.getValue());
				}

				eNach.setMerchantName(userdao.getBusinessNameByPayId(doc.getString(FieldType.PAY_ID.getName())));
				eNach.setPayId(doc.getString(FieldType.PAY_ID.getName()));
				if (doc.containsKey(FieldType.REGISTRATION_DATE.getName())
						&& StringUtils.isNotBlank(doc.getString(FieldType.REGISTRATION_DATE.getName()))) {
					eNach.setRegDate(doc.getString(FieldType.REGISTRATION_DATE.getName()));
				} else {
					eNach.setRegDate(Constants.NA.getValue());
				}

				eNach.setCreateDate(doc.getString(FieldType.CREATE_DATE.getName()));
				if (doc.containsKey(FieldType.DEBIT_DATE.getName())
						&& StringUtils.isNotBlank(doc.getString(FieldType.DEBIT_DATE.getName()))) {
					eNach.setDebitDate(doc.getString(FieldType.DEBIT_DATE.getName()));
				} else {
					eNach.setDebitDate(Constants.NA.getValue());
				}

				if (doc.containsKey(FieldType.SETTLED_DATE.getName())
						&& StringUtils.isNotBlank(doc.getString(FieldType.SETTLED_DATE.getName()))) {
					eNach.setSettledDate(doc.getString(FieldType.SETTLED_DATE.getName()));
				} else {
					eNach.setSettledDate(Constants.NA.getValue());
				}
				eNach.setDueDate(doc.getString(FieldType.DUE_DATE.getName()));
				eNach.setPaymentType(PaymentType.getpaymentName(doc.getString(FieldType.PAYMENT_TYPE.getName())));
				// For net Banking
				if (doc.containsKey(FieldType.ACCOUNT_NO.toString())
						&& StringUtils.isNotBlank(doc.getString(FieldType.ACCOUNT_NO.toString()))) {
					eNach.setAccountNumber(fields.fieldMask(doc.getString(FieldType.ACCOUNT_NO.toString())));
				} else {
					eNach.setAccountNumber(Constants.NA.getValue());
				}

				if (doc.containsKey(FieldType.IFSC_CODE.getName())
						&& StringUtils.isNotBlank(doc.getString(FieldType.IFSC_CODE.getName()))) {
					eNach.setIfscCode(fields.fieldMask(doc.getString(FieldType.IFSC_CODE.getName())));
				} else {
					eNach.setIfscCode(Constants.NA.getValue());
				}

				if (doc.containsKey(FieldType.ACCOUNT_HOLDER_NAME.getName())
						&& StringUtils.isNotBlank(doc.getString(FieldType.ACCOUNT_HOLDER_NAME.getName()))) {
					eNach.setAccountHolderName(doc.getString(FieldType.ACCOUNT_HOLDER_NAME.getName()));
				} else {
					eNach.setAccountHolderName(Constants.NA.getValue());
				}

				if (doc.containsKey(FieldType.ACCOUNT_TYPE.getName())
						&& StringUtils.isNotBlank(doc.getString(FieldType.ACCOUNT_TYPE.getName()))) {
					eNach.setAccountType(doc.getString(FieldType.ACCOUNT_TYPE.getName()));
				} else {
					eNach.setAccountType(Constants.NA.getValue());
				}

				// }

				eNach.setStartDate(doc.getString(FieldType.DATEFROM.getName()));
				eNach.setEndDate(doc.getString(FieldType.DATETO.getName()));
				eNach.setCustEmail(fields.maskEmail(doc.getString(FieldType.CUST_EMAIL.getName())));
				eNach.setCustPhone(fields.fieldMask(doc.getString(FieldType.CUST_PHONE.getName())));
				eNach.setTenure(doc.getString(FieldType.TENURE.getName()));
				eNach.setBankName(doc.getString(FieldType.BANK_NAME.getName()));
				eNach.setAmount(doc.getString(FieldType.AMOUNT.getName()));
				eNach.setMaxAmount(doc.getString(FieldType.MONTHLY_AMOUNT.getName()));
				eNach.setTotalAmount(doc.getString(FieldType.TOTAL_AMOUNT.getName()));

				eNachList.add(eNach);
			}

		} catch (Exception ex) {
			logger.info("Exception caught ", ex);
		}
		return eNachList;
	}

	public List<TransactionSearch> fetchP2MPayoutData(String merchantId, String orderId, String rrn, String dateFrom,
			String dateTo) {
		Map<String, String> userMap = new HashMap<String, String>();
		List<TransactionSearch> list = new ArrayList<TransactionSearch>();
		try {

			List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();
			DateFormat formatdate = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
			Date dateStart = null;
			Date dateEnd = null;
			try {
				dateStart = formatdate.parse(dateFrom);
				dateEnd = formatdate.parse(dateTo);
			} catch (ParseException e) {
				logger.error("Exception in date parsing ", e);
			}

			String startString = dateStart.toInstant().atZone(ZoneId.systemDefault()).toLocalDate() + " 00:00:00";
			String endString = dateEnd.toInstant().atZone(ZoneId.systemDefault()).toLocalDate() + " 23:59:59";

			BasicDBObject dateQuery = new BasicDBObject();
			dateQuery.put(FieldType.CREATE_DATE.getName(),
					BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(startString).toLocalizedPattern())
							.add("$lt", new SimpleDateFormat(endString).toLocalizedPattern()).get());
			paramConditionLst.add(dateQuery);

			LocalDate incrementingDate = dateStart.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			LocalDate endDate = dateEnd.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

			List<String> allDatesIndex = new ArrayList<>();

			while (!incrementingDate.isAfter(endDate)) {
				allDatesIndex.add(incrementingDate.toString().replaceAll("-", ""));
				incrementingDate = incrementingDate.plusDays(1);
			}
			BasicDBObject dateIndexIn = new BasicDBObject("$in", allDatesIndex);
			BasicDBObject dateIndexQuery = new BasicDBObject(FieldType.DATE_INDEX.getName(), dateIndexIn);
			paramConditionLst.add(dateIndexQuery);

			if (StringUtils.isNotBlank(merchantId) && !merchantId.equalsIgnoreCase("All")) {
				BasicDBObject payIdObj = new BasicDBObject(FieldType.PAY_ID.getName(), merchantId);
				paramConditionLst.add(payIdObj);
			}
			if (StringUtils.isNotBlank(orderId)) {
				BasicDBObject orderIdObj = new BasicDBObject(FieldType.ORDER_ID.getName(), orderId);
				paramConditionLst.add(orderIdObj);
			}
			if (StringUtils.isNotBlank(rrn)) {
				BasicDBObject rrnObj = new BasicDBObject(FieldType.RRN.getName(), rrn);
				paramConditionLst.add(rrnObj);
			}

			BasicDBObject finalquery = new BasicDBObject("$and", paramConditionLst);
			logger.info("final query for P2M Merchant Payout data = " + finalquery);

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.MERCHANT_P2M_PAYOUT.getValue()));

			BasicDBObject match = new BasicDBObject("$match", finalquery);
			BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject(FieldType.CREATE_DATE.getName(), -1));

			List<BasicDBObject> pipeline = Arrays.asList(match, sort);
			logger.info(pipeline.toString());

			AggregateIterable<Document> output = coll.aggregate(pipeline);
			output.allowDiskUse(true);
			MongoCursor<Document> cursor = output.iterator();

			while (cursor.hasNext()) {
				Document dbobj = cursor.next();

				if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
						&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
					dbobj = dataEncDecTool.decryptDocument(dbobj);
				}

				TransactionSearch data = new TransactionSearch();
				String payId = dbobj.getString(FieldType.PAY_ID.getName());
				String user;

				if (userMap.get(payId) != null && !userMap.get(payId).isEmpty()) {
					user = userMap.get(payId);
				} else {
					user = userdao.getBusinessNameByPayId(payId);
					userMap.put(payId, user);
				}
				data.setMerchantName(user);
				data.setTransactionCaptureDate(dbobj.getString(FieldType.CREATE_DATE.getName()));
				data.setRrn(dbobj.getString(FieldType.RRN.getName()));
				data.setOrderId(dbobj.getString(FieldType.ORDER_ID.getName()));
				data.setPayerName(dbobj.getString(FieldType.PAYER_NAME.getName()));
				data.setPayeeAddress(dbobj.getString(FieldType.PAYEE_ADDRESS.getName()));
				data.setAmount(dbobj.getString(FieldType.AMOUNT.getName()));
				data.setStatus(dbobj.getString(FieldType.STATUS.getName()));
				list.add(data);
			}
			cursor.close();
		} catch (Exception e) {
			logger.error("exception", e);
		}
		return list;

	}

	public List<TransactionSearchDownloadObject> downloadSearchTransactionData(String dataArray[], User sessionUser) {
		Map<String, User> userMap = new HashMap<String, User>();
		List<TransactionSearchDownloadObject> transactionList = new ArrayList<TransactionSearchDownloadObject>();
		try {
			UserSettingData userSettings = userSettingDao.fetchDataUsingPayId(sessionUser.getPayId());

			String acqId = null, rrnNo = null, pgRef = null, orderId = null;

			for (int i = 0; i < dataArray.length; i++) {
				switch (i) {
				case 0:
					acqId = dataArray[0];
					break;
				case 1:
					rrnNo = dataArray[1];
					break;
				case 2:
					pgRef = dataArray[2];
					break;
				case 3:
					orderId = dataArray[3];
					break;
				}
			}

			BasicDBObject finalquery = new BasicDBObject();

			if (StringUtils.isNotBlank(acqId) && !acqId.equalsIgnoreCase("NA") && !acqId.equalsIgnoreCase("0")) {
				finalquery.put(FieldType.ACQ_ID.getName(), acqId.replaceAll("\\s", ""));
			}

			if (StringUtils.isNotBlank(orderId) && !orderId.equalsIgnoreCase("NA") && !orderId.equalsIgnoreCase("0")) {
				finalquery.put(FieldType.ORDER_ID.getName(), orderId.replaceAll("\\s", ""));
			}
			if (StringUtils.isNotBlank(rrnNo) && !rrnNo.equalsIgnoreCase("NA") && !rrnNo.equalsIgnoreCase("0")) {
				finalquery.put(FieldType.RRN.getName(), rrnNo.replaceAll("\\s", ""));
			}
			if (StringUtils.isNotBlank(pgRef) && !pgRef.equalsIgnoreCase("NA") && !pgRef.equalsIgnoreCase("0")) {
				finalquery.put(FieldType.PG_REF_NUM.getName(), pgRef.replaceAll("\\s", ""));
			}

			if (finalquery.isEmpty()) {
				return transactionList;
			}
			MongoDatabase dbIns = mongoInstance.getDB();

			BasicDBObject projectElement = new BasicDBObject();
			projectElement.put(FieldType.TXN_ID.getName(), 1);
			projectElement.put(FieldType.PG_REF_NUM.getName(), 1);
			projectElement.put(FieldType.PAY_ID.getName(), 1);
			projectElement.put(FieldType.CUST_NAME.getName(), 1);
			projectElement.put(FieldType.CUST_PHONE.getName(), 1);
			projectElement.put(FieldType.CUST_EMAIL.getName(), 1);
			projectElement.put(FieldType.CUST_ID.getName(), 1);
			projectElement.put(FieldType.PAYOUT_DATE.getName(), 1);
			projectElement.put(FieldType.UTR_NO.getName(), 1);
			projectElement.put(CrmFieldType.BUSINESS_NAME.getName(), 1);
			projectElement.put(FieldType.SUB_MERCHANT_ID.getName(), 1);
			projectElement.put(FieldType.REFUND_DAYS.getName(), 1);
			projectElement.put(FieldType.PRODUCT_PRICE.getName(), 1);
			projectElement.put(FieldType.VENDOR_ID.getName(), 1);
			projectElement.put(FieldType.SKU_CODE.getName(), 1);
			projectElement.put(FieldType.CATEGORY_CODE.getName(), 1);
			projectElement.put(FieldType.TXNTYPE.getName(), 1);
			projectElement.put(FieldType.ORIG_TXNTYPE.getName(), 1);
			projectElement.put(FieldType.STATUS.getName(), 1);
			projectElement.put(FieldType.MOP_TYPE.getName(), 1);
			projectElement.put(FieldType.PAYMENT_TYPE.getName(), 1);
			projectElement.put(FieldType.ACQUIRER_TYPE.getName(), 1);
			projectElement.put(FieldType.ACQ_ID.getName(), 1);
			projectElement.put(FieldType.UDF11.getName(), 1);
			projectElement.put(FieldType.UDF12.getName(), 1);
			projectElement.put(FieldType.UDF13.getName(), 1);
			projectElement.put(FieldType.UDF14.getName(), 1);
			projectElement.put(FieldType.UDF15.getName(), 1);
			projectElement.put(FieldType.UDF16.getName(), 1);
			projectElement.put(FieldType.UDF17.getName(), 1);
			projectElement.put(FieldType.UDF18.getName(), 1);
			projectElement.put(FieldType.RRN.getName(), 1);
			projectElement.put(FieldType.TXN_CAPTURE_FLAG.getName(), 1);
			projectElement.put(FieldType.TRANSACTION_MODE.getName(), 1);
			projectElement.put(FieldType.RESPONSE_MESSAGE.getName(), 1);
			projectElement.put(FieldType.PG_TXN_MESSAGE.getName(), 1);
			projectElement.put(FieldType.CARD_MASK.getName(), 1);
			projectElement.put(FieldType.PAYER_ADDRESS.getName(), 1);
			projectElement.put(FieldType.PART_SETTLE.getName(), 1);
			projectElement.put(FieldType.CREATE_DATE.getName(), 1);
			projectElement.put(FieldType.AMOUNT.getName(), 1);
			projectElement.put(FieldType.ORDER_ID.getName(), 1);
			projectElement.put(FieldType.REFUND_ORDER_ID.getName(), 1);
			projectElement.put(FieldType.PRODUCT_DESC.getName(), 1);
			projectElement.put(FieldType.PG_DATE_TIME.getName(), 1);
			projectElement.put(FieldType.INTERNAL_CARD_ISSUER_BANK.getName(), 1);
			projectElement.put(FieldType.INTERNAL_CARD_ISSUER_COUNTRY.getName(), 1);
			projectElement.put(FieldType.REFUNDABLE_AMOUNT.getName(), 1);
			projectElement.put(FieldType.TOTAL_AMOUNT.getName(), 1);
			projectElement.put(FieldType.CURRENCY_CODE.getName(), 1);
			projectElement.put(FieldType.PAYMENTS_REGION.getName(), 1);
			projectElement.put(FieldType.CARD_HOLDER_TYPE.getName(), 1);
			projectElement.put(FieldType.RESELLER_CHARGES.getName(), 1);
			projectElement.put(FieldType.RESELLER_GST.getName(), 1);
			projectElement.put(FieldType.ACQUIRER_TDR_SC.getName(), 1);
			projectElement.put(FieldType.PG_TDR_SC.getName(), 1);
			projectElement.put(FieldType.ACQUIRER_GST.getName(), 1);
			projectElement.put(FieldType.PG_GST.getName(), 1);
			projectElement.put(FieldType.SETTLEMENT_FLAG.getName(), 1);
			projectElement.put(FieldType.SETTLEMENT_DATE.getName(), 1);
			projectElement.put(FieldType.IS_ENCRYPTED.getName(), 1);
			projectElement.put(FieldType.SUF_TDR.getName(), 1);
			projectElement.put(FieldType.SUF_GST.getName(), 1);
			BasicDBObject project = new BasicDBObject("$project", projectElement);

			MongoCollection<Document> coll = dbIns
					.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));

			BasicDBObject match = new BasicDBObject("$match", finalquery);
			BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject("CREATE_DATE", -1));

			List<BasicDBObject> pipeline = Arrays.asList(match, project, sort);

			AggregateIterable<Document> output = coll.aggregate(pipeline);
			output.allowDiskUse(true);
			MongoCursor<Document> cursor = output.iterator();

			while (cursor.hasNext()) {

				Document dbobj = cursor.next();
				if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
						&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
					dbobj = dataEncDecTool.decryptDocument(dbobj);
				}

				TransactionSearchDownloadObject transReport = new TransactionSearchDownloadObject();

				transReport.setTransactionId(dbobj.getString(FieldType.TXN_ID.toString()));
				transReport.setPgRefNum(dbobj.getString(FieldType.PG_REF_NUM.toString()));
				transReport.setPayId(dbobj.getString(FieldType.PAY_ID.toString()));
				if (StringUtils.isNotBlank(dbobj.getString(FieldType.PAYMENTS_REGION.getName()))) {
					transReport.setTransactionRegion(dbobj.getString(FieldType.PAYMENTS_REGION.getName()));
				} else {
					transReport.setTransactionRegion(CrmFieldConstants.NA.getValue());
				}
				transReport.setMerchants(dbobj.getString(CrmFieldType.BUSINESS_NAME.getName()));
				transReport.setTxnSettledType(dbobj.getString(FieldType.TXN_CAPTURE_FLAG.getName()));
				transReport.setResponseMessage(dbobj.getString(FieldType.RESPONSE_MESSAGE.getName()));
				if (!StringUtils.isEmpty(dbobj.getString(FieldType.PG_TXN_MESSAGE.getName()))) {
					transReport.setAccqResponseMessage(dbobj.getString(FieldType.PG_TXN_MESSAGE.getName()));
				} else {
					transReport.setAccqResponseMessage(CrmFieldConstants.NA.getValue());
				}
				if (dbobj.getString(FieldType.UDF6.getName()) != null) {
					transReport.setDeltaRefundFlag(dbobj.getString(FieldType.UDF6.getName()));
				} else {
					transReport.setDeltaRefundFlag("");
				}

				String payid = (String) dbobj.get(FieldType.PAY_ID.getName());

				User user1 = new User();

				if (userMap.get(payid) != null && !userMap.get(payid).getPayId().isEmpty()) {
					user1 = userMap.get(payid);
				} else {
					user1 = userdao.findPayId(payid);
					userMap.put(payid, user1);
				}
				transReport.setMerchants(user1.getBusinessName());

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.CARD_MASK.toString()))) {
					transReport.setCardNumber(((Document) dbobj).getString(FieldType.CARD_MASK.toString()));
				} else if (StringUtils.isNotBlank(dbobj.getString(FieldType.PAYER_ADDRESS.getName()))) {

					if (sessionUser.getUserType().equals(UserType.ADMIN)
							|| sessionUser.getUserType().equals(UserType.SUBADMIN)) {
						transReport.setCardNumber(dbobj.getString(FieldType.PAYER_ADDRESS.getName()));
					} else {
						String vpaString = dbobj.getString(FieldType.PAYER_ADDRESS.getName());
						String[] vpaArray = vpaString.split("@");
						char[] vpaChar = vpaArray[0].toCharArray();
						StringBuilder vpastrBuilder = new StringBuilder();

						if (vpaChar.length > 3) {
							for (int i = 0; i < vpaChar.length - 3; i++) {
								vpastrBuilder.append(vpaChar[i]);
							}
							vpastrBuilder.append("***@");
							vpastrBuilder.append(vpaArray[1]);
						} else {
							vpastrBuilder.append(vpaChar[0]);
							vpastrBuilder.append("**@");
							vpastrBuilder.append(vpaArray[1]);
						}

						transReport.setCardNumber(vpastrBuilder.toString());
					}
				} else {
					transReport.setCardNumber(CrmFieldConstants.NA.getValue());
				}

				// if (!merchantPayId.equalsIgnoreCase("All") &&
				if (dbobj.containsKey(FieldType.SUB_MERCHANT_ID.getName())
						&& StringUtils.isNotBlank(dbobj.getString(FieldType.SUB_MERCHANT_ID.getName()))) {

					String subMerchant = dbobj.getString(FieldType.SUB_MERCHANT_ID.getName());
					User subMerchantUser = new User();

					if (userMap.get(subMerchant) != null && !userMap.get(subMerchant).getPayId().isEmpty()) {
						subMerchantUser = userMap.get(subMerchant);
					} else {
						subMerchantUser = userdao.findPayId(subMerchant);
						userMap.put(subMerchant, subMerchantUser);
					}
					transReport.setSubMerchantId(subMerchantUser.getBusinessName());
				}
				if (StringUtils.isBlank(transReport.getSubMerchantId())) {
					transReport.setSubMerchantId("NA");
				}
				if (null != dbobj.getString(FieldType.ORIG_TXNTYPE.toString())) {

					if (StringUtils.isNotBlank(dbobj.getString(FieldType.SETTLEMENT_FLAG.getName()))
							&& dbobj.getString(FieldType.SETTLEMENT_FLAG.getName()).equals("Y")) {
						if (dbobj.getString(FieldType.ORIG_TXNTYPE.toString())
								.equals(TransactionType.REFUND.getName())) {
							transReport.setTxnType(TransactionType.REFUND.getName());
						} else {
							transReport.setTxnType(TransactionType.SALE.getName());
						}
					} else {
						transReport.setTxnType(dbobj.getString(FieldType.ORIG_TXNTYPE.toString()));
					}

				} else {
					// If ORIG_TXN_TYPE is not available incase of a timeout ,
					// set TXNTYPE instead
					// of ORIG_TXN_TYPE
					if (dbobj.getString(FieldType.STATUS.toString()).equalsIgnoreCase(StatusType.TIMEOUT.getName())) {
						transReport.setTxnType(dbobj.getString(FieldType.TXNTYPE.toString()));
					} else {
						transReport.setTxnType(CrmFieldConstants.NA.getValue());
					}
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.TRANSACTION_MODE.getName()))) {
					transReport.setTransactionMode(dbobj.getString(FieldType.TRANSACTION_MODE.getName()));
				} else {
				}
				if (null != dbobj.getString(FieldType.ACQUIRER_TYPE.toString())) {
					transReport.setAcquirerType(dbobj.getString(FieldType.ACQUIRER_TYPE.toString()));
				} else {
					transReport.setAcquirerType(CrmFieldConstants.NA.getValue());
				}

				if (null != dbobj.getString(FieldType.ACQUIRER_MODE.getName())) {
					transReport.setAcquirerMode(dbobj.getString(FieldType.ACQUIRER_MODE.getName()));
				} else {
					transReport.setAcquirerMode(CrmFieldConstants.NA.getValue());
				}

				if (null != dbobj.getString(FieldType.PAYMENT_TYPE.toString())) {
					transReport.setPaymentMethods(
							PaymentType.getpaymentName(dbobj.getString(FieldType.PAYMENT_TYPE.toString())));
				} else {
					transReport.setPaymentMethods(CrmFieldConstants.NA.getValue());
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF11.getName()))) {
					transReport.setUDF11(dbobj.getString(FieldType.UDF11.getName()));
				} else {
					transReport.setUDF11(CrmFieldConstants.NA.getValue());
				}
				if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF12.getName()))) {
					transReport.setUDF12(dbobj.getString(FieldType.UDF12.getName()));
				} else {
					transReport.setUDF12(CrmFieldConstants.NA.getValue());
				}
				if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF13.getName()))) {
					transReport.setUDF13(dbobj.getString(FieldType.UDF13.getName()));
				} else {
					transReport.setUDF13(CrmFieldConstants.NA.getValue());
				}
				if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF14.getName()))) {
					transReport.setUDF14(dbobj.getString(FieldType.UDF14.getName()));
				} else {
					transReport.setUDF14(CrmFieldConstants.NA.getValue());
				}
				if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF15.getName()))) {
					transReport.setUDF15(dbobj.getString(FieldType.UDF15.getName()));
				} else {
					transReport.setUDF15(CrmFieldConstants.NA.getValue());
				}
				if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF16.getName()))) {
					transReport.setUDF16(dbobj.getString(FieldType.UDF16.getName()));
				} else {
					transReport.setUDF16(CrmFieldConstants.NA.getValue());
				}
				if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF17.getName()))) {
					transReport.setUDF17(dbobj.getString(FieldType.UDF17.getName()));
				} else {
					transReport.setUDF17(CrmFieldConstants.NA.getValue());
				}
				if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF18.getName()))) {
					transReport.setUDF18(dbobj.getString(FieldType.UDF18.getName()));
				} else {
					transReport.setUDF18(CrmFieldConstants.NA.getValue());
				}

				// if customTransaction flag is true
				if (((sessionUser.getUserType().equals(UserType.MERCHANT))
						|| (sessionUser.getUserType().equals(UserType.MERCHANT)))
						&& userSettings.isCustomTransactionStatus()) {
					if (((dbobj.getString(FieldType.STATUS.toString())).equalsIgnoreCase(StatusType.TIMEOUT.getName()))
							|| ((dbobj.getString(FieldType.STATUS.toString()))
									.equalsIgnoreCase(StatusType.CAPTURED.getName()))
							|| ((dbobj.getString(FieldType.STATUS.toString()))
									.equalsIgnoreCase(StatusType.ENROLLED.getName()))
							|| ((dbobj.getString(FieldType.STATUS.toString()))
									.equalsIgnoreCase(StatusType.SETTLED.getName()))
							|| ((dbobj.getString(FieldType.STATUS.toString()))
									.equalsIgnoreCase(StatusType.SENT_TO_BANK.getName()))
							|| ((dbobj.getString(FieldType.STATUS.toString()))
									.equalsIgnoreCase(StatusType.PENDING.getName()))) {
						if (StringUtils.isNotBlank(dbobj.getString(FieldType.SETTLEMENT_FLAG.getName()))
								&& dbobj.getString(FieldType.SETTLEMENT_FLAG.getName()).equals("Y")) {
							transReport.setStatus(StatusType.SETTLED.getName());
						} else {
							transReport.setStatus(dbobj.getString(FieldType.STATUS.toString()));
						}

					} else {
						transReport.setStatus("Failed");
					}
				} else {
					// transReport.setStatus(dbobj.getString(FieldType.STATUS.toString()));
					if (StringUtils.isNotBlank(dbobj.getString(FieldType.SETTLEMENT_FLAG.getName()))
							&& dbobj.getString(FieldType.SETTLEMENT_FLAG.getName()).equals("Y")) {
						transReport.setStatus(StatusType.SETTLED.getName());
					} else {
						transReport.setStatus(dbobj.getString(FieldType.STATUS.toString()));
					}
				}
				if (StringUtils.isNotBlank(dbobj.getString(FieldType.SETTLEMENT_FLAG.getName()))
						&& dbobj.getString(FieldType.SETTLEMENT_FLAG.getName()).equals("Y")) {
					transReport.setDateFrom(dbobj.getString(FieldType.SETTLEMENT_DATE.getName()));
				} else {
					transReport.setDateFrom(dbobj.getString(FieldType.CREATE_DATE.getName()));
				}
				// transReport.setDateFrom(dbobj.getString(FieldType.CREATE_DATE.getName()));
				transReport.setAmount(dbobj.getString(FieldType.AMOUNT.toString()));
				transReport.setOrderId(dbobj.getString(FieldType.ORDER_ID.toString()));
				if (dbobj.getString(FieldType.TOTAL_AMOUNT.toString()) != null) {
					transReport.setTotalAmount(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()));
				} else {
					transReport.setTotalAmount("");
				}

				if (dbobj.getString(FieldType.ACQ_ID.toString()) != null) {
					transReport.setAcqId(dbobj.getString(FieldType.ACQ_ID.toString()));
				} else {
					transReport.setAcqId(CrmFieldConstants.NA.getValue());
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.RRN.getName()))) {
					transReport.setRrn(dbobj.getString(FieldType.RRN.toString()));
				} else {
					transReport.setRrn(CrmFieldConstants.NA.getValue());
				}

				if (dbobj.getString(FieldType.REFUND_ORDER_ID.toString()) != null) {
					transReport.setRefundOrderId(dbobj.getString(FieldType.REFUND_ORDER_ID.toString()));
				} else {
					transReport.setRefundOrderId(CrmFieldConstants.NA.getValue());
				}

				if (sessionUser.getUserType().equals(UserType.ADMIN)
						|| sessionUser.getUserType().equals(UserType.SUBADMIN)) {

					if (null != dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString())) {
						transReport.setAcquirerTdrOrSurcharge(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()));
					} else {
						transReport.setAcquirerTdrOrSurcharge(CrmFieldConstants.NA.getValue());
					}

					if (null != dbobj.getString(FieldType.ACQUIRER_GST.toString())) {
						transReport.setAcquirerGST(dbobj.getString(FieldType.ACQUIRER_GST.toString()));
					} else {
						transReport.setAcquirerGST(CrmFieldConstants.NA.getValue());
					}
					if (null != dbobj.getString(FieldType.PG_TDR_SC.toString())) {
						transReport.setPgTdrOrSurcharge(dbobj.getString(FieldType.PG_TDR_SC.toString()));
					} else {
						transReport.setPgTdrOrSurcharge(CrmFieldConstants.NA.getValue());
					}
					if (null != dbobj.getString(FieldType.PG_GST.toString())) {
						transReport.setPgGST(dbobj.getString(FieldType.PG_GST.toString()));
					} else {
						transReport.setPgGST(CrmFieldConstants.NA.getValue());
					}

					if (dbobj.containsKey(FieldType.RESELLER_CHARGES.getName())
							&& StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_CHARGES.getName()))) {
						transReport.setResellerCharges(dbobj.getString(FieldType.RESELLER_CHARGES.getName()));
					} else {
						transReport.setResellerCharges(CrmFieldConstants.NA.getValue());
					}

					if (dbobj.containsKey(FieldType.RESELLER_GST.getName())
							&& StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_GST.getName()))) {
						transReport.setResellerGst(dbobj.getString(FieldType.RESELLER_GST.getName()));
					} else {
						transReport.setResellerGst(CrmFieldConstants.NA.getValue());
					}

				} else {

					if (dbobj.containsKey(FieldType.RESELLER_CHARGES.getName())
							&& StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_CHARGES.toString()))) {

						if (null != dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString())
								&& (null != dbobj.getString(FieldType.PG_TDR_SC.toString()))) {

							transReport.setTdrOrSurcharge(String.format("%.2f", ((Double
									.parseDouble(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString())))
									+ (Double.parseDouble(dbobj.getString(FieldType.PG_TDR_SC.toString())))
									+ (Double.parseDouble(dbobj.getString(FieldType.RESELLER_CHARGES.toString()))))));
						} else {
							transReport.setTdrOrSurcharge("0.00");
						}

					} else {
						if (null != dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString())
								&& (null != dbobj.getString(FieldType.PG_TDR_SC.toString()))) {

							transReport.setTdrOrSurcharge(String.format("%.2f",
									(Double.parseDouble(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
											+ (Double.parseDouble(dbobj.getString(FieldType.PG_TDR_SC.toString()))))));
						} else {
							transReport.setTdrOrSurcharge("0.00");
						}
					}

					if (dbobj.containsKey(FieldType.RESELLER_GST.getName())
							&& StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_GST.toString()))) {

						if (null != dbobj.getString(FieldType.ACQUIRER_GST.toString())
								&& (null != dbobj.getString(FieldType.PG_GST.toString()))) {
							transReport.setGst(String.format("%.2f", ((Double
									.parseDouble(dbobj.getString(FieldType.ACQUIRER_GST.toString())))
									+ (Double.parseDouble(dbobj.getString(FieldType.PG_GST.toString())))
									+ (Double.parseDouble(dbobj.getString(FieldType.RESELLER_GST.toString()))))));

						} else {
							transReport.setGst("0.00");
						}
					} else {

						if (null != dbobj.getString(FieldType.ACQUIRER_GST.toString())
								&& (null != dbobj.getString(FieldType.PG_GST.toString()))) {
							transReport.setGst(String.format("%.2f",
									(Double.parseDouble(dbobj.getString(FieldType.ACQUIRER_GST.toString()))
											+ (Double.parseDouble(dbobj.getString(FieldType.PG_GST.toString()))))));

						} else {
							transReport.setGst("0.00");
						}
					}
				}

				if (null != dbobj.getString(FieldType.MOP_TYPE.toString())) {
					transReport.setMopType(MopType.getmopName(dbobj.getString(FieldType.MOP_TYPE.toString())));

				} else {
					transReport.setMopType(CrmFieldConstants.NA.toString());
				}

				if (null != dbobj.getString(FieldType.CARD_HOLDER_TYPE.toString())) {
					transReport.setCardHolderType(dbobj.getString(FieldType.CARD_HOLDER_TYPE.toString()));
				} else {
					transReport.setCardHolderType(CrmFieldConstants.NA.toString());
				}

				// Merchant TDR/SC and Merhcant GST
				if (dbobj.containsKey(FieldType.MERCHANT_TDR_SC.getName())
						&& dbobj.containsKey(FieldType.MERCHANT_GST.getName())
						&& StringUtils.isNotBlank(dbobj.getString(FieldType.MERCHANT_TDR_SC.getName()))
						&& StringUtils.isNotBlank(dbobj.getString(FieldType.MERCHANT_GST.getName()))) {

					transReport.setMerchantTdrOrSc(dbobj.getString(FieldType.MERCHANT_TDR_SC.getName()));
					transReport.setMerchantGst(dbobj.getString(FieldType.MERCHANT_GST.getName()));

				} else if (dbobj.containsKey(FieldType.PG_TDR_SC.getName())
						&& dbobj.containsKey(FieldType.PG_GST.getName())
						&& dbobj.containsKey(FieldType.ACQUIRER_TDR_SC.getName())
						&& dbobj.containsKey(FieldType.ACQUIRER_GST.getName())
						&& dbobj.containsKey(FieldType.RESELLER_CHARGES.getName())
						&& dbobj.containsKey(FieldType.RESELLER_GST.getName())
						&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.getName()))
						&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.getName()))
						&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.getName()))
						&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.getName()))
						&& StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_CHARGES.getName()))
						&& StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_GST.getName()))) {

					transReport.setMerchantTdrOrSc(String.format("%.2f",
							((Double.parseDouble(dbobj.getString(FieldType.PG_TDR_SC.toString())))
									+ (Double.parseDouble(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString())))
									+ (Double.parseDouble(dbobj.getString(FieldType.RESELLER_CHARGES.toString()))))));

					transReport.setMerchantGst(String.format("%.2f",
							((Double.parseDouble(dbobj.getString(FieldType.ACQUIRER_GST.toString())))
									+ (Double.parseDouble(dbobj.getString(FieldType.PG_GST.toString())))
									+ (Double.parseDouble(dbobj.getString(FieldType.RESELLER_GST.toString()))))));

				} else {

					if (dbobj.containsKey(FieldType.PG_TDR_SC.getName())
							&& dbobj.containsKey(FieldType.ACQUIRER_TDR_SC.getName())
							&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.getName()))
							&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.getName()))) {

						transReport.setMerchantTdrOrSc(String.format("%.2f",
								((Double.parseDouble(dbobj.getString(FieldType.PG_TDR_SC.toString()))) + (Double
										.parseDouble(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))))));
					} else {
						transReport.setMerchantTdrOrSc(CrmFieldConstants.NA.getValue());
					}

					if (dbobj.containsKey(FieldType.PG_GST.getName())
							&& dbobj.containsKey(FieldType.ACQUIRER_GST.getName())
							&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.getName()))
							&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.getName()))) {

						transReport.setMerchantGst(String.format("%.2f",
								((Double.parseDouble(dbobj.getString(FieldType.PG_GST.toString())))
										+ (Double.parseDouble(dbobj.getString(FieldType.ACQUIRER_GST.toString()))))));
					} else {
						transReport.setMerchantGst(CrmFieldConstants.NA.getValue());
					}

				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.SUF_GST.toString()))) {
					transReport.setSufGst(dbobj.getString(FieldType.SUF_GST.toString()));
				} else {
					transReport.setSufGst(CrmFieldConstants.NA.getValue());
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.SUF_TDR.toString()))) {
					transReport.setSufTdr(dbobj.getString(FieldType.SUF_TDR.toString()));
				} else {
					transReport.setSufTdr(CrmFieldConstants.NA.getValue());
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.CUST_ID.toString()))) {
					transReport.setConsumerNo(dbobj.getString(FieldType.CUST_ID.toString()));
				} else {
					transReport.setConsumerNo("Consumer No Not found");
				}

				transactionList.add(transReport);
			}
			cursor.close();
			return transactionList;
		} catch (Exception e) {
			logger.error("Exception in downloadSearchTransactionData : ", e);
			return transactionList;
		}
	}

	public int unsettledTransactionCount(String pgRefNum, String orderId, String customerEmail, String merchantPayId,
			String subMerchantPayId, String paymentType, String currency, String fromDate, String toDate,
			User sessionUser, String partSettleFlag, String paymentRegion, Set<String> orderIdSet,
			String transactionFlag, String deltaFlag, String autoRefund) {

		logger.info("Inside TxnReports , transactionCount");
		try {
			int total = 0;
			List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();

			BasicDBObject customerQuery = new BasicDBObject();
			BasicDBObject customerQueryMask = new BasicDBObject();
			List<BasicDBObject> txnTypeQueryList = new ArrayList<BasicDBObject>();
			List<BasicDBObject> settledFlagQueryList = new ArrayList<BasicDBObject>();

			BasicDBObject txnCapturedFlag = new BasicDBObject();

			BasicDBObject dateIndexConditionQuery = new BasicDBObject();

			DateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.ENGLISH);
			Date dateStart = format.parse(fromDate);
			Date dateEnd = format.parse(toDate);

			LocalDate startDate = dateStart.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			LocalDate endDate = dateEnd.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

			String fromDatesIndex = startDate.toString().replaceAll("-", "");
			String toDatesIndex = endDate.toString().replaceAll("-", "");

			dateIndexConditionQuery.put("DATE_INDEX",
					BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(fromDatesIndex).toLocalizedPattern())
							.add("$lte", new SimpleDateFormat(toDatesIndex).toLocalizedPattern()).get());

			if (!merchantPayId.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.PAY_ID.getName(), merchantPayId));
			}

			if (StringUtils.isNotBlank(subMerchantPayId) && !subMerchantPayId.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.SUB_MERCHANT_ID.getName(), subMerchantPayId));
			}
			if (StringUtils.isNotEmpty(paymentType) && !paymentType.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.PAYMENT_TYPE.getName(), paymentType));
			} else {

			}

			if (sessionUser.getUserType().equals(UserType.RESELLER)) {
				paramConditionLst.add(new BasicDBObject(FieldType.RESELLER_ID.getName(), sessionUser.getResellerId()));
			}

			if (!pgRefNum.isEmpty()) {
				paramConditionLst.add(new BasicDBObject(FieldType.PG_REF_NUM.getName(), pgRefNum));
			}

			BasicDBObject orderIdConditionQuery = null;
			if (orderIdSet != null) {
				List<String> orderIdList = new ArrayList<>(orderIdSet);
				orderIdConditionQuery = new BasicDBObject("$in", orderIdList);
			}
			if (!orderId.isEmpty()) {
				paramConditionLst.add(new BasicDBObject(FieldType.ORDER_ID.getName(), orderId));
			} else if (null != orderIdConditionQuery) {
				paramConditionLst.add(new BasicDBObject(FieldType.ORDER_ID.getName(), orderIdConditionQuery));
			}

			if (StringUtils.isNotEmpty(customerEmail) && !customerEmail.isEmpty()) {
				customerQuery.append(FieldType.CUST_EMAIL.getName(), customerEmail);
				if (StringUtils.isNotBlank(customerEmail)) {
					String email = customerEmail;
					String emailMask = null;
					final String mask = "*****";
					final int at = email.indexOf("@");
					if (at > 2) {
						final int maskLen = Math.min(Math.max(at / 2, 2), 5);
						final int start1 = (at - maskLen) / 2;
						emailMask = email.substring(0, start1) + mask.substring(0, maskLen)
								+ email.substring(start1 + maskLen);
					}
					customerQueryMask.append(FieldType.CUST_EMAIL.getName(), emailMask);
				}
			}

			BasicDBObject settledCheckQuery = new BasicDBObject();
			settledFlagQueryList
					.add(new BasicDBObject(FieldType.SETTLEMENT_FLAG.getName(), new BasicDBObject("$exists", false)));
			settledFlagQueryList.add(new BasicDBObject(FieldType.SETTLEMENT_FLAG.getName(), null));
			settledCheckQuery.put("$or", settledFlagQueryList);
			paramConditionLst.add(settledCheckQuery);

			paramConditionLst.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.CAPTURED.getName()));

			if (!currency.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.CURRENCY_CODE.getName(), currency));
			} else {

			}
			if (!partSettleFlag.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.PART_SETTLE.getName(), partSettleFlag));
			} else {

			}
			;

			BasicDBObject txntype = new BasicDBObject();
			txnTypeQueryList.add(new BasicDBObject(FieldType.ORIG_TXNTYPE.getName(), TransactionType.SALE.getName()));
			txnTypeQueryList.add(new BasicDBObject(FieldType.ORIG_TXNTYPE.getName(), TransactionType.REFUND.getName()));
			txntype.put("$or", txnTypeQueryList);
			paramConditionLst.add(txntype);
			// paramConditionLst.add(new BasicDBObject(FieldType.ORIG_TXNTYPE.getName(),
			// TransactionType.SALE.getName()));

			if (!paymentRegion.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.PAYMENTS_REGION.getName(), paymentRegion));
			} else {
			}
			if (StringUtils.isNotBlank(transactionFlag) && !transactionFlag.equalsIgnoreCase("ALL")) {
				txnCapturedFlag.append("$in", transactionFlag.split(","));
				paramConditionLst.add(new BasicDBObject(FieldType.TXN_CAPTURE_FLAG.getName(), txnCapturedFlag));
			}

			if (StringUtils.isNotBlank(deltaFlag) && !deltaFlag.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.DELTA_REFUND_FLAG.getName(), deltaFlag));
			}

			if (StringUtils.isNotBlank(autoRefund) && !autoRefund.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.AUTO_REFUND_FLAG.getName(), autoRefund));
			}

			if (StringUtils.isNotBlank(pgRefNum) || StringUtils.isNotBlank(orderId)) {

			} else {
				if (!dateIndexConditionQuery.isEmpty()) {
					paramConditionLst.add(dateIndexConditionQuery);
				}
			}

			List<BasicDBObject> fianlQueryList = new ArrayList<BasicDBObject>();

			fianlQueryList.addAll(paramConditionLst);
			if (!customerQuery.isEmpty()) {
				paramConditionLst.add(customerQuery);
			}

			if (!customerQueryMask.isEmpty()) {
				fianlQueryList.add(customerQueryMask);
			}

			BasicDBObject finalquery = new BasicDBObject("$and", paramConditionLst);
			BasicDBObject finalqueryMask = new BasicDBObject("$and", fianlQueryList);

			if (!customerQueryMask.isEmpty()) {
				logger.info("Inside TxnReports , transactionCount , finalQuery = " + finalqueryMask);
			} else {
				logger.info("Inside TxnReports , transactionCount , finalQuery = " + finalquery);
			}

			MongoDatabase dbIns = mongoInstance.getDB();

			MongoCollection<Document> coll = dbIns.getCollection(
					propertiesManager.propertiesMap.get(prefix + Constants.TRANSACTION_STATUS_COLLECTION.getValue()));
			BasicDBObject match = new BasicDBObject("$match", finalquery);
			BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject("CREATE_DATE", -1));

			List<BasicDBObject> pipeline = Arrays.asList(match, sort);
			total = (int) coll.count(finalquery);
			logger.info("Inside transactionCount , total records from DB  = " + total);
			return total;

		} catch (Exception e) {
			logger.error("Exception occured in TxnReports , transactionCount n exception = ", e);
			return 0;
		}
	}

	public List<TransactionSearch> unsettledTransactionReport(String pgRefNum, String orderId, String customerEmail,
			String merchantPayId, String subMerchantPayId, String paymentType, String currency, String fromDate,
			String toDate, User sessionUser, int start, int length, String partSettleFlag, String paymentRegion,
			Set<String> orderIdSet, String transactionFlag, String deltaFlag, String autoRefund) {
		Map<String, User> userMap = new HashMap<String, User>();

		logger.info("Inside TxnReports , transactionReport");

		boolean isGlocal = false;

		try {
			UserSettingData userSettings = userSettingDao.fetchDataUsingPayId(sessionUser.getPayId());

			List<TransactionSearch> transactionList = new ArrayList<TransactionSearch>();
			PropertiesManager propManager = new PropertiesManager();
			List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();
			List<BasicDBObject> settledFlagQueryList = new ArrayList<BasicDBObject>();
			BasicDBObject customerQuery = new BasicDBObject();
			BasicDBObject customerQueryMask = new BasicDBObject();
			BasicDBObject txnCapturedFlag = new BasicDBObject();
			BasicDBObject dateIndexConditionQuery = new BasicDBObject();

			DateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.ENGLISH);
			Date dateStart = format.parse(fromDate);
			Date dateEnd = format.parse(toDate);

			LocalDate startDate = dateStart.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			LocalDate endDate = dateEnd.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

			String fromDatesIndex = startDate.toString().replaceAll("-", "");
			String toDatesIndex = endDate.toString().replaceAll("-", "");

			dateIndexConditionQuery.put("DATE_INDEX",
					BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(fromDatesIndex).toLocalizedPattern())
							.add("$lte", new SimpleDateFormat(toDatesIndex).toLocalizedPattern()).get());

			if (!merchantPayId.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.PAY_ID.getName(), merchantPayId));
			}

			if (StringUtils.isNotBlank(subMerchantPayId) && !subMerchantPayId.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.SUB_MERCHANT_ID.getName(), subMerchantPayId));
			}
			if (StringUtils.isNotEmpty(paymentType) && !paymentType.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.PAYMENT_TYPE.getName(), paymentType));
			} else {

			}
			if (sessionUser.getUserType().equals(UserType.RESELLER)) {
				paramConditionLst.add(new BasicDBObject(FieldType.RESELLER_ID.getName(), sessionUser.getResellerId()));
			}

			if (!pgRefNum.isEmpty()) {
				paramConditionLst.add(new BasicDBObject(FieldType.PG_REF_NUM.getName(), pgRefNum));
			}

			BasicDBObject orderIdConditionQuery = null;
			if (orderIdSet != null) {
				List<String> orderIdList = new ArrayList<>(orderIdSet);
				orderIdConditionQuery = new BasicDBObject("$in", orderIdList);
			}
			if (!orderId.isEmpty()) {
				paramConditionLst.add(new BasicDBObject(FieldType.ORDER_ID.getName(), orderId));
			} else if (null != orderIdConditionQuery) {
				paramConditionLst.add(new BasicDBObject(FieldType.ORDER_ID.getName(), orderIdConditionQuery));
			}

			if (StringUtils.isNotEmpty(customerEmail) && !customerEmail.isEmpty()) {
				customerQuery.append(FieldType.CUST_EMAIL.getName(), customerEmail);
				if (StringUtils.isNotBlank(customerEmail)) {
					String email = customerEmail;
					String emailMask = null;
					final String mask = "*****";
					final int at = email.indexOf("@");
					if (at > 2) {
						final int maskLen = Math.min(Math.max(at / 2, 2), 5);
						final int start1 = (at - maskLen) / 2;
						emailMask = email.substring(0, start1) + mask.substring(0, maskLen)
								+ email.substring(start1 + maskLen);
					}
					customerQueryMask.append(FieldType.CUST_EMAIL.getName(), emailMask);
				}
			}

			BasicDBObject settledCheckQuery = new BasicDBObject();
			settledFlagQueryList
					.add(new BasicDBObject(FieldType.SETTLEMENT_FLAG.getName(), new BasicDBObject("$exists", false)));
			settledFlagQueryList.add(new BasicDBObject(FieldType.SETTLEMENT_FLAG.getName(), null));
			settledCheckQuery.put("$or", settledFlagQueryList);
			paramConditionLst.add(settledCheckQuery);

			paramConditionLst.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.CAPTURED.getName()));

			if (!currency.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.CURRENCY_CODE.getName(), currency));
			}
			if (!partSettleFlag.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.PART_SETTLE.getName(), partSettleFlag));
			}
			paramConditionLst.add(new BasicDBObject(FieldType.ORIG_TXNTYPE.getName(), TransactionType.SALE.getName()));

			if (!paymentRegion.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.PAYMENTS_REGION.getName(), paymentRegion));
			}

			if (StringUtils.isNotBlank(transactionFlag) && !transactionFlag.equalsIgnoreCase("ALL")) {
				txnCapturedFlag.append("$in", transactionFlag.split(","));
				paramConditionLst.add(new BasicDBObject(FieldType.TXN_CAPTURE_FLAG.getName(), txnCapturedFlag));
			}

			if (StringUtils.isNotBlank(deltaFlag) && !deltaFlag.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.DELTA_REFUND_FLAG.getName(), deltaFlag));
			}

			if (StringUtils.isNotBlank(autoRefund) && !autoRefund.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.AUTO_REFUND_FLAG.getName(), autoRefund));
			}

			if (StringUtils.isNotBlank(pgRefNum) || StringUtils.isNotBlank(orderId)) {

			} else {

				if (!dateIndexConditionQuery.isEmpty()) {
					paramConditionLst.add(dateIndexConditionQuery);
				}
			}

			List<BasicDBObject> fianlQueryList = new ArrayList<BasicDBObject>();
			fianlQueryList.addAll(paramConditionLst);

			if (!customerQuery.isEmpty()) {
				paramConditionLst.add(customerQuery);
			}

			if (!customerQueryMask.isEmpty()) {
				fianlQueryList.add(customerQueryMask);
			}

			BasicDBObject finalquery = new BasicDBObject("$and", paramConditionLst);
			BasicDBObject finalqueryMask = new BasicDBObject("$and", fianlQueryList);

			if (!customerQueryMask.isEmpty()) {
				logger.info("Inside TxnReports , transactionReport , finalQuery = " + finalqueryMask);
			} else {
				logger.info("Inside TxnReports , transactionReport , finalQuery = " + finalquery);
			}

			MongoDatabase dbIns = mongoInstance.getDB();

			List<BasicDBObject> pipeline = null;
			BasicDBObject skip = null;
			BasicDBObject limit = null;

			BasicDBObject projectElement = new BasicDBObject();
			projectElement.put(FieldType.TXN_ID.getName(), 1);
			projectElement.put(FieldType.PG_REF_NUM.getName(), 1);
			projectElement.put(FieldType.PAY_ID.getName(), 1);
			projectElement.put(FieldType.CUST_NAME.getName(), 1);
			projectElement.put(FieldType.CUST_PHONE.getName(), 1);
			projectElement.put(FieldType.CUST_EMAIL.getName(), 1);
			projectElement.put(FieldType.PAYOUT_DATE.getName(), 1);
			projectElement.put(FieldType.UTR_NO.getName(), 1);
			projectElement.put(CrmFieldType.BUSINESS_NAME.getName(), 1);
			projectElement.put(FieldType.SUB_MERCHANT_ID.getName(), 1);
			projectElement.put(FieldType.REFUND_DAYS.getName(), 1);
			projectElement.put(FieldType.PRODUCT_PRICE.getName(), 1);
			projectElement.put(FieldType.VENDOR_ID.getName(), 1);
			projectElement.put(FieldType.SKU_CODE.getName(), 1);
			projectElement.put(FieldType.CATEGORY_CODE.getName(), 1);
			projectElement.put(FieldType.TXNTYPE.getName(), 1);
			projectElement.put(FieldType.ORIG_TXNTYPE.getName(), 1);
			projectElement.put(FieldType.STATUS.getName(), 1);
			projectElement.put(FieldType.MOP_TYPE.getName(), 1);
			projectElement.put(FieldType.PAYMENT_TYPE.getName(), 1);
			projectElement.put(FieldType.ACQUIRER_TYPE.getName(), 1);
			projectElement.put(FieldType.ACQ_ID.getName(), 1);
			projectElement.put(FieldType.UDF11.getName(), 1);
			projectElement.put(FieldType.UDF12.getName(), 1);
			projectElement.put(FieldType.UDF13.getName(), 1);
			projectElement.put(FieldType.UDF14.getName(), 1);
			projectElement.put(FieldType.UDF15.getName(), 1);
			projectElement.put(FieldType.UDF16.getName(), 1);
			projectElement.put(FieldType.UDF17.getName(), 1);
			projectElement.put(FieldType.UDF18.getName(), 1);
			projectElement.put(FieldType.RRN.getName(), 1);
			projectElement.put(FieldType.TXN_CAPTURE_FLAG.getName(), 1);
			projectElement.put(FieldType.TRANSACTION_MODE.getName(), 1);
			projectElement.put(FieldType.RESPONSE_MESSAGE.getName(), 1);
			projectElement.put(FieldType.PG_TXN_MESSAGE.getName(), 1);
			projectElement.put(FieldType.CARD_MASK.getName(), 1);
			projectElement.put(FieldType.PAYER_ADDRESS.getName(), 1);
			projectElement.put(FieldType.PART_SETTLE.getName(), 1);
			projectElement.put(FieldType.CREATE_DATE.getName(), 1);
			projectElement.put(FieldType.AMOUNT.getName(), 1);
			projectElement.put(FieldType.ORDER_ID.getName(), 1);
			projectElement.put(FieldType.REFUND_ORDER_ID.getName(), 1);
			projectElement.put(FieldType.PRODUCT_DESC.getName(), 1);
			projectElement.put(FieldType.PG_DATE_TIME.getName(), 1);
			projectElement.put(FieldType.INTERNAL_CARD_ISSUER_BANK.getName(), 1);
			projectElement.put(FieldType.INTERNAL_CARD_ISSUER_COUNTRY.getName(), 1);
			projectElement.put(FieldType.REFUNDABLE_AMOUNT.getName(), 1);
			projectElement.put(FieldType.TOTAL_AMOUNT.getName(), 1);
			projectElement.put(FieldType.CURRENCY_CODE.getName(), 1);
			projectElement.put(FieldType.PAYMENTS_REGION.getName(), 1);
			projectElement.put(FieldType.CARD_HOLDER_TYPE.getName(), 1);
			projectElement.put(FieldType.RESELLER_CHARGES.getName(), 1);
			projectElement.put(FieldType.RESELLER_GST.getName(), 1);
			projectElement.put(FieldType.ACQUIRER_TDR_SC.getName(), 1);
			projectElement.put(FieldType.PG_TDR_SC.getName(), 1);
			projectElement.put(FieldType.ACQUIRER_GST.getName(), 1);
			projectElement.put(FieldType.PG_GST.getName(), 1);
			projectElement.put(FieldType.SETTLEMENT_FLAG.getName(), 1);
			projectElement.put(FieldType.SETTLEMENT_DATE.getName(), 1);
			projectElement.put(FieldType.IS_ENCRYPTED.getName(), 1);

			BasicDBObject project = new BasicDBObject("$project", projectElement);

			MongoCollection<Document> coll = dbIns.getCollection(
					propertiesManager.propertiesMap.get(prefix + Constants.TRANSACTION_STATUS_COLLECTION.getValue()));
			BasicDBObject match = new BasicDBObject("$match", finalquery);
			BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject("CREATE_DATE", -1));

			skip = new BasicDBObject("$skip", start);
			limit = new BasicDBObject("$limit", length);
			pipeline = Arrays.asList(match, project, sort, skip, limit);
			AggregateIterable<Document> output = coll.aggregate(pipeline);
			output.allowDiskUse(true);
			MongoCursor<Document> cursor = output.iterator();
			Set<String> settledOrderIdSet = new HashSet<String>();
			Map<String, Document> saleTxnsMap = new HashMap<String, Document>();

			while (cursor.hasNext()) {
				Document dbobj = cursor.next();
				if (StringUtils.isNotBlank(dbobj.getString("IS_ENCRYPTED"))
						&& dbobj.getString("IS_ENCRYPTED").equalsIgnoreCase("Y")) {
					dbobj = dataEncDecTool.decryptDocument(dbobj);
				}

				// if(dbobj.getString(FieldType.STATUS.getName()).equals(StatusType.SETTLED.getName()))
				// {
				// settledOrderIdSet.add(dbobj.getString(FieldType.ORDER_ID.getName()));
				// }
				//
				// if(dbobj.getString(FieldType.STATUS.getName()).equals(StatusType.CAPTURED.getName()))
				// {
				// saleTxnsMap.put(dbobj.getString(FieldType.ORDER_ID.getName()),
				// dbobj);
				// }
				//
				//
				// }
				// for(Document dbobj : saleTxnsMap.values()) {
				//
				// if(settledOrderIdSet.contains(dbobj.getString(FieldType.ORDER_ID.getName())))
				// {
				// continue;
				// }

				TransactionSearch transReport = new TransactionSearch();
				BigInteger txnID = new BigInteger(((Document) dbobj).getString(FieldType.TXN_ID.toString()));
				transReport.setTransactionId((txnID));
				transReport.setPgRefNum(((Document) dbobj).getString(FieldType.PG_REF_NUM.toString()));
				transReport.setPayId(((Document) dbobj).getString(FieldType.PAY_ID.toString()));

				if (null != ((Document) dbobj).getString(FieldType.CUST_NAME.toString())) {
					transReport.setCustomerName(((Document) dbobj).getString(FieldType.CUST_NAME.toString()));
				} else {
					transReport.setCustomerName(CrmFieldConstants.NA.getValue());
				}

				if (null != ((Document) dbobj).getString(FieldType.CUST_PHONE.toString())) {
					transReport.setCustomerMobile(((Document) dbobj).getString(FieldType.CUST_PHONE.toString()));
				} else {
					transReport.setCustomerMobile(CrmFieldConstants.NA.getValue());
				}

				if (null != ((Document) dbobj).getString(FieldType.CUST_EMAIL.toString())) {
					transReport.setCustomerEmail(((Document) dbobj).getString(FieldType.CUST_EMAIL.toString()));
				} else {
					transReport.setCustomerEmail(CrmFieldConstants.NA.getValue());
				}

				if (null != ((Document) dbobj).getString(FieldType.PAYOUT_DATE.toString())) {
					transReport.setPayOutDate(((Document) dbobj).getString(FieldType.PAYOUT_DATE.toString()));
				} else {
					transReport.setPayOutDate(CrmFieldConstants.NA.getValue());
				}
				if (null != ((Document) dbobj).getString(FieldType.UTR_NO.toString())) {
					transReport.setUtrNo(((Document) dbobj).getString(FieldType.UTR_NO.toString()));
				} else {
					transReport.setUtrNo(CrmFieldConstants.NA.getValue());
				}

				if (StringUtils.isNotBlank(dbobj.getString("IS_CUSTOM_HOSTED"))
						&& dbobj.getString("IS_CUSTOM_HOSTED").equalsIgnoreCase("Y")) {
					transReport.setCustomFlag(((Document) dbobj).getString("IS_CUSTOM_HOSTED"));
				} else {
					transReport.setCustomFlag("N");
				}

				transReport.setMerchants(dbobj.getString(CrmFieldType.BUSINESS_NAME.getName()));

				User user1 = new User();

				if (StringUtils.isNotBlank((String) dbobj.get(FieldType.PAY_ID.getName()))) {
					String payid = (String) dbobj.get(FieldType.PAY_ID.getName());

					if (userMap.get(payid) != null && !userMap.get(payid).getPayId().isEmpty()) {
						user1 = userMap.get(payid);
					} else {
						user1 = userdao.findPayId(payid);
						userMap.put(payid, user1);
					}
				}
				if (user1 != null) {
					transReport.setMerchants(user1.getBusinessName());
				}

				if (dbobj.containsKey(FieldType.SUB_MERCHANT_ID.getName())
						&& StringUtils.isNotBlank(dbobj.getString(FieldType.SUB_MERCHANT_ID.getName()))) {

					String subMerchant = dbobj.getString(FieldType.SUB_MERCHANT_ID.getName());
					User subMerchantUser = new User();

					if (userMap.get(subMerchant) != null && !userMap.get(subMerchant).getPayId().isEmpty()) {
						subMerchantUser = userMap.get(subMerchant);
					} else {
						subMerchantUser = userdao.findPayId(subMerchant);
						userMap.put(subMerchant, subMerchantUser);
					}
					if (subMerchantUser != null) {
						transReport.setSubMerchantId(subMerchantUser.getBusinessName());
					} else {
						transReport.setSubMerchantId(CrmFieldConstants.NA.getValue());
					}
				} else {
					if ((!pgRefNum.isEmpty() || !orderId.isEmpty())
							&& dbobj.containsKey(FieldType.SUB_MERCHANT_ID.getName())
							&& StringUtils.isNotBlank(dbobj.getString(FieldType.SUB_MERCHANT_ID.getName()))) {
						transReport.setSubMerchantId(
								userdao.getBusinessNameByPayId(dbobj.getString(FieldType.SUB_MERCHANT_ID.getName())));
					}
				}

				if (merchantPayId.equalsIgnoreCase("All")) {

					if (sessionUser.getUserType().equals(UserType.RESELLER)) {

					} else {

						if (StringUtils.isNotBlank(dbobj.getString(FieldType.REFUND_DAYS.getName()))) {
							transReport.setRefundCycle(dbobj.getString(FieldType.REFUND_DAYS.getName()));
						} else {
							transReport.setRefundCycle(CrmFieldConstants.NA.getValue());
						}

						if (StringUtils.isNotBlank(dbobj.getString(FieldType.PRODUCT_PRICE.getName()))) {
							transReport.setProductPrice(dbobj.getString(FieldType.PRODUCT_PRICE.getName()));
						} else {
							transReport.setProductPrice(CrmFieldConstants.NA.getValue());
						}
						if (StringUtils.isNotBlank(dbobj.getString(FieldType.VENDOR_ID.getName()))) {
							transReport.setVendorID(dbobj.getString(FieldType.VENDOR_ID.getName()));
						} else {
							transReport.setVendorID(CrmFieldConstants.NA.getValue());
						}
						if (StringUtils.isNotBlank(dbobj.getString(FieldType.SKU_CODE.getName()))) {
							transReport.setSKUCode(dbobj.getString(FieldType.SKU_CODE.getName()));
						} else {
							transReport.setSKUCode(CrmFieldConstants.NA.getValue());
						}

						if (StringUtils.isNotBlank(dbobj.getString(FieldType.CATEGORY_CODE.getName()))) {
							transReport.setCategoryCode(dbobj.getString(FieldType.CATEGORY_CODE.getName()));
						} else {
							transReport.setCategoryCode(CrmFieldConstants.NA.getValue());
						}
					}
				} else {

					if (sessionUser.getUserType().equals(UserType.ADMIN)
							|| sessionUser.getUserType().equals(UserType.SUBADMIN)
							|| sessionUser.getUserType().equals(UserType.RESELLER)) {
						User merhant = userMap.get(merchantPayId); // userdao.findPayId(payid);
						UserSettingData merchantSettings = userSettingDao.fetchDataUsingPayId(merhant.getPayId());

						if (merchantSettings.isRetailMerchantFlag()) {
							if (StringUtils.isNotBlank(dbobj.getString(FieldType.REFUND_DAYS.getName()))) {
								transReport.setRefundCycle(dbobj.getString(FieldType.REFUND_DAYS.getName()));
							} else {
								transReport.setRefundCycle(CrmFieldConstants.NA.getValue());
							}

							if (StringUtils.isNotBlank(dbobj.getString(FieldType.PRODUCT_PRICE.getName()))) {
								transReport.setProductPrice(dbobj.getString(FieldType.PRODUCT_PRICE.getName()));
							} else {
								transReport.setProductPrice(CrmFieldConstants.NA.getValue());
							}
							if (StringUtils.isNotBlank(dbobj.getString(FieldType.VENDOR_ID.getName()))) {
								transReport.setVendorID(dbobj.getString(FieldType.VENDOR_ID.getName()));
							} else {
								transReport.setVendorID(CrmFieldConstants.NA.getValue());
							}
							if (StringUtils.isNotBlank(dbobj.getString(FieldType.SKU_CODE.getName()))) {
								transReport.setSKUCode(dbobj.getString(FieldType.SKU_CODE.getName()));
							} else {
								transReport.setSKUCode(CrmFieldConstants.NA.getValue());
							}

							if (StringUtils.isNotBlank(dbobj.getString(FieldType.CATEGORY_CODE.getName()))) {
								transReport.setCategoryCode(dbobj.getString(FieldType.CATEGORY_CODE.getName()));
							} else {
								transReport.setCategoryCode(CrmFieldConstants.NA.getValue());
							}
						}
					} else {

						if (userSettings.isRetailMerchantFlag()) {
							if (StringUtils.isNotBlank(dbobj.getString(FieldType.REFUND_DAYS.getName()))) {
								transReport.setRefundCycle(dbobj.getString(FieldType.REFUND_DAYS.getName()));
							} else {
								transReport.setRefundCycle(CrmFieldConstants.NA.getValue());
							}

							if (StringUtils.isNotBlank(dbobj.getString(FieldType.PRODUCT_PRICE.getName()))) {
								transReport.setProductPrice(dbobj.getString(FieldType.PRODUCT_PRICE.getName()));
							} else {
								transReport.setProductPrice(CrmFieldConstants.NA.getValue());
							}
							if (StringUtils.isNotBlank(dbobj.getString(FieldType.VENDOR_ID.getName()))) {
								transReport.setVendorID(dbobj.getString(FieldType.VENDOR_ID.getName()));
							} else {
								transReport.setVendorID(CrmFieldConstants.NA.getValue());
							}
							if (StringUtils.isNotBlank(dbobj.getString(FieldType.SKU_CODE.getName()))) {
								transReport.setSKUCode(dbobj.getString(FieldType.SKU_CODE.getName()));
							} else {
								transReport.setSKUCode(CrmFieldConstants.NA.getValue());
							}

							if (StringUtils.isNotBlank(dbobj.getString(FieldType.CATEGORY_CODE.getName()))) {
								transReport.setCategoryCode(dbobj.getString(FieldType.CATEGORY_CODE.getName()));
							} else {
								transReport.setCategoryCode(CrmFieldConstants.NA.getValue());
							}
						}

					}

				}

				if ((StringUtils.isNotBlank(dbobj.getString(FieldType.TXNTYPE.getName()))) && (dbobj
						.getString(FieldType.TXNTYPE.toString()).equalsIgnoreCase(TransactionType.REFUND.getName()))) {
					if (StringUtils.isNotBlank(dbobj.getString(FieldType.SETTLEMENT_FLAG.getName()))
							&& dbobj.getString(FieldType.SETTLEMENT_FLAG.getName()).equals("Y")) {
						transReport.setTxnType(TransactionType.REFUND.getName());
					} else {
						transReport.setTxnType(dbobj.getString(FieldType.TXNTYPE.toString()));
					}
				} else if (StringUtils.isNotBlank(dbobj.getString(FieldType.ORIG_TXNTYPE.toString()))) {

					if (StringUtils.isNotBlank(dbobj.getString(FieldType.SETTLEMENT_FLAG.getName()))
							&& dbobj.getString(FieldType.SETTLEMENT_FLAG.getName()).equals("Y")) {
						transReport.setTxnType(TransactionType.SALE.getName());
					} else {
						transReport.setTxnType(dbobj.getString(FieldType.ORIG_TXNTYPE.toString()));
					}
				} else {

					// If ORIG_TXN_TYPE is not available incase of a timeout ,
					// set TXNTYPE instead
					// of ORIG_TXN_TYPE

					if (dbobj.getString(FieldType.STATUS.toString()).equalsIgnoreCase(StatusType.TIMEOUT.getName())) {
						transReport.setTxnType(dbobj.getString(FieldType.TXNTYPE.toString()));
					} else {
						transReport.setTxnType(CrmFieldConstants.NA.getValue());
					}
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.MOP_TYPE.toString()))) {
					transReport.setMopType(MopType.getmopName(dbobj.getString(FieldType.MOP_TYPE.toString())));
				} else {
					transReport.setMopType(CrmFieldConstants.NOT_AVAILABLE.getValue());
				}
				if (StringUtils.isNotBlank(dbobj.getString(FieldType.PAYMENT_TYPE.toString()))) {
					transReport.setPaymentMethods(
							PaymentType.getpaymentName(dbobj.getString(FieldType.PAYMENT_TYPE.toString())));
				} else {
					transReport.setPaymentMethods(CrmFieldConstants.NOT_AVAILABLE.getValue());
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TYPE.getName()))) {
					transReport.setAcquirerType(dbobj.getString(FieldType.ACQUIRER_TYPE.getName()));
				} else {
					transReport.setAcquirerType(CrmFieldConstants.NOT_AVAILABLE.getValue());
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQ_ID.getName()))) {
					transReport.setAcqId(dbobj.getString(FieldType.ACQ_ID.getName()));
				} else {
					transReport.setAcqId(CrmFieldConstants.NA.getValue());
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF11.getName()))) {
					transReport.setUDF11(dbobj.getString(FieldType.UDF11.getName()));
				} else {
					transReport.setUDF11(CrmFieldConstants.NA.getValue());
				}
				if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF12.getName()))) {
					transReport.setUDF12(dbobj.getString(FieldType.UDF12.getName()));
				} else {
					transReport.setUDF12(CrmFieldConstants.NA.getValue());
				}
				if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF13.getName()))) {
					transReport.setUDF13(dbobj.getString(FieldType.UDF13.getName()));
				} else {
					transReport.setUDF13(CrmFieldConstants.NA.getValue());
				}
				if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF14.getName()))) {
					transReport.setUDF14(dbobj.getString(FieldType.UDF14.getName()));
				} else {
					transReport.setUDF14(CrmFieldConstants.NA.getValue());
				}
				if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF15.getName()))) {
					transReport.setUDF15(dbobj.getString(FieldType.UDF15.getName()));
				} else {
					transReport.setUDF15(CrmFieldConstants.NA.getValue());
				}
				if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF16.getName()))) {
					transReport.setUDF16(dbobj.getString(FieldType.UDF16.getName()));
				} else {
					transReport.setUDF16(CrmFieldConstants.NA.getValue());
				}
				if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF17.getName()))) {
					transReport.setUDF17(dbobj.getString(FieldType.UDF17.getName()));
				} else {
					transReport.setUDF17(CrmFieldConstants.NA.getValue());
				}
				if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF18.getName()))) {
					transReport.setUDF18(dbobj.getString(FieldType.UDF18.getName()));
				} else {
					transReport.setUDF18(CrmFieldConstants.NA.getValue());
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.RRN.getName()))
						&& !dbobj.getString(FieldType.RRN.getName()).equalsIgnoreCase("-")) {
					transReport.setRrn(dbobj.getString(FieldType.RRN.getName()));
				} else {
					transReport.setRrn(CrmFieldConstants.NA.getValue());
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.TXN_CAPTURE_FLAG.getName()))) {
					transReport.setTxnSettledType(dbobj.getString(FieldType.TXN_CAPTURE_FLAG.getName()));
				} else {
					transReport.setTxnSettledType(CrmFieldConstants.NA.getValue());
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.TRANSACTION_MODE.getName()))) {
					transReport.setTransactionMode(dbobj.getString(FieldType.TRANSACTION_MODE.getName()));
				} else {
				}

				transReport.setResponseMessage(dbobj.getString(CrmFieldType.RESPONSE_MESSAGE.toString()));
				if (!StringUtils.isEmpty(dbobj.getString(CrmFieldType.PG_TXN_MESSAGE.toString()))) {
					transReport.setAccqResponseMessage(dbobj.getString(CrmFieldType.PG_TXN_MESSAGE.toString()));
				} else {
					transReport.setAccqResponseMessage(CrmFieldConstants.NA.getValue());
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.PAYMENT_TYPE.toString()))) {
					if (null != dbobj.getString(FieldType.CARD_MASK.toString())) {
						transReport.setCardNumber(dbobj.getString(FieldType.CARD_MASK.toString()));
					} else if (null != dbobj.getString(FieldType.PAYER_ADDRESS.getName())) {

						if (sessionUser.getUserType().equals(UserType.ADMIN)
								|| sessionUser.getUserType().equals(UserType.SUBADMIN)) {
							transReport.setCardNumber(dbobj.getString(FieldType.PAYER_ADDRESS.getName()));
						} else {
							String vpaString = dbobj.getString(FieldType.PAYER_ADDRESS.getName());
							String[] vpaArray = vpaString.split("@");
							char[] vpaChar = vpaArray[0].toCharArray();
							StringBuilder vpastrBuilder = new StringBuilder();

							if (vpaChar.length > 3) {
								for (int i = 0; i < vpaChar.length - 3; i++) {
									vpastrBuilder.append(vpaChar[i]);
								}
								vpastrBuilder.append("***@");
								vpastrBuilder.append(vpaArray[1]);
							} else {
								vpastrBuilder.append(vpaChar[0]);
								vpastrBuilder.append("**@");
								vpastrBuilder.append(vpaArray[1]);
							}

							transReport.setCardNumber(vpastrBuilder.toString());
						}
					} else {
						transReport.setCardNumber(CrmFieldConstants.NA.getValue());
					}
				} else {
					transReport.setCardNumber(CrmFieldConstants.NA.getValue());
				}
				if (StringUtils.isNotBlank(dbobj.getString(FieldType.PART_SETTLE.toString()))
						&& dbobj.getString(FieldType.PART_SETTLE.toString()).equalsIgnoreCase(("Y"))) {
					transReport.setPartSettle(dbobj.getString(FieldType.PART_SETTLE.toString()));
				} else {
					transReport.setPartSettle(CrmFieldConstants.NA.getValue());
				}

				if (((sessionUser.getUserType().equals(UserType.MERCHANT))
						|| (sessionUser.getUserType().equals(UserType.SUBUSER)))
						&& userSettings.isCustomTransactionStatus()) {
					if (((dbobj.getString(FieldType.STATUS.toString())).equalsIgnoreCase(StatusType.TIMEOUT.getName()))
							|| ((dbobj.getString(FieldType.STATUS.toString()))
									.equalsIgnoreCase(StatusType.CAPTURED.getName()))
							|| ((dbobj.getString(FieldType.STATUS.toString()))
									.equalsIgnoreCase(StatusType.ENROLLED.getName()))
							|| ((dbobj.getString(FieldType.STATUS.toString()))
									.equalsIgnoreCase(StatusType.SETTLED.getName()))
							|| ((dbobj.getString(FieldType.STATUS.toString()))
									.equalsIgnoreCase(StatusType.SENT_TO_BANK.getName()))
							|| ((dbobj.getString(FieldType.STATUS.toString()))
									.equalsIgnoreCase(StatusType.PENDING.getName()))) {

						if (StringUtils.isNotBlank(dbobj.getString(FieldType.SETTLEMENT_FLAG.getName()))
								&& dbobj.getString(FieldType.SETTLEMENT_FLAG.getName()).equals("Y")) {
							transReport.setStatus(StatusType.SETTLED.getName());
						} else {
							transReport.setStatus(dbobj.getString(FieldType.STATUS.toString()));
						}
						// transReport.setStatus(dbobj.getString(FieldType.STATUS.toString()));
					} else {
						transReport.setStatus("Failed");
					}
				} else {
					if (StringUtils.isNotBlank(dbobj.getString(FieldType.SETTLEMENT_FLAG.getName()))
							&& dbobj.getString(FieldType.SETTLEMENT_FLAG.getName()).equals("Y")) {
						transReport.setStatus(StatusType.SETTLED.getName());
					} else {
						transReport.setStatus(dbobj.getString(FieldType.STATUS.toString()));
					}
					// transReport.setStatus(dbobj.getString(FieldType.STATUS.toString()));
				}
				if (StringUtils.isNotBlank(dbobj.getString(FieldType.SETTLEMENT_FLAG.getName()))
						&& dbobj.getString(FieldType.SETTLEMENT_FLAG.getName()).equals("Y")) {
					transReport.setDateFrom(dbobj.getString(FieldType.SETTLEMENT_DATE.getName()));
				} else {
					transReport.setDateFrom(dbobj.getString(FieldType.CREATE_DATE.getName()));
				}
				// transReport.setDateFrom(dbobj.getString(FieldType.CREATE_DATE.getName()));
				transReport.setAmount(dbobj.getString(FieldType.AMOUNT.toString()));
				transReport.setOrderId(dbobj.getString(FieldType.ORDER_ID.toString()));

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.REFUND_ORDER_ID.toString()))) {
					transReport.setRefundOrderId(dbobj.getString(FieldType.REFUND_ORDER_ID.toString()));
				} else {
					transReport.setRefundOrderId(CrmFieldConstants.NA.getValue());
				}

				transReport.setoId(dbobj.getString(FieldType.OID.toString()));

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.PRODUCT_DESC.toString()))) {
					transReport.setProductDesc(dbobj.getString(FieldType.PRODUCT_DESC.toString()));
				} else {
					transReport.setProductDesc(CrmFieldConstants.NA.getValue());
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.PG_DATE_TIME.getName()))) {
					transReport.setTransactionCaptureDate(dbobj.getString(FieldType.PG_DATE_TIME.toString()));
				} else {
					transReport.setTransactionCaptureDate(CrmFieldConstants.NA.getValue());
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.INTERNAL_CARD_ISSUER_BANK.getName()))) {
					transReport.setInternalCardIssusserBank(
							dbobj.getString(FieldType.INTERNAL_CARD_ISSUER_BANK.toString()));
				} else {
					transReport.setInternalCardIssusserBank(CrmFieldConstants.NA.getValue());
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.INTERNAL_CARD_ISSUER_COUNTRY.getName()))) {
					transReport.setInternalCardIssusserCountry(
							dbobj.getString(FieldType.INTERNAL_CARD_ISSUER_COUNTRY.toString()));
				} else {
					transReport.setInternalCardIssusserCountry(CrmFieldConstants.NA.getValue());
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.REFUNDABLE_AMOUNT.getName()))) {
					transReport.setRefundableAmount(dbobj.getString(FieldType.REFUNDABLE_AMOUNT.toString()));
				} else {
					transReport.setRefundableAmount(CrmFieldConstants.NA.getValue());
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.AMOUNT.getName()))) {
					transReport.setApprovedAmount(dbobj.getString(FieldType.AMOUNT.toString()));
				} else {
					transReport.setApprovedAmount(CrmFieldConstants.NA.getValue());
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))) {
					transReport.setTotalAmount(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()));
				} else {
					transReport.setTotalAmount("");
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.CURRENCY_CODE.toString()))) {
					transReport.setCurrency(
							propManager.getAlphabaticCurrencyCode(dbobj.getString(FieldType.CURRENCY_CODE.toString())));
				} else {
					transReport.setCurrency(CrmFieldConstants.NA.getValue());
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.PAYMENTS_REGION.toString()))) {
					transReport.setPaymentRegion(dbobj.getString(FieldType.PAYMENTS_REGION.toString()));

				} else {
					transReport.setPaymentRegion(CrmFieldConstants.NA.getValue());
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.CARD_HOLDER_TYPE.toString()))) {
					transReport.setCardHolderType(dbobj.getString(FieldType.CARD_HOLDER_TYPE.toString()));

				} else {
					transReport.setCardHolderType(CrmFieldConstants.NA.getValue());
				}
				// changes regarding partner reseller
				if (sessionUser.getUserType().equals(UserType.RESELLER) && sessionUser.isPartnerFlag()) {

					if (StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_CHARGES.toString()))) {

						transReport.setResellerCharges(String.format("%.2f",
								(Double.parseDouble(dbobj.getString(FieldType.RESELLER_CHARGES.toString())))));

					} else {
						transReport.setResellerCharges("0.00");
					}

					if (StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_GST.toString()))) {
						transReport.setResellerGST(String.format("%.2f",
								(Double.parseDouble(dbobj.getString(FieldType.RESELLER_GST.toString())))));

					} else {
						transReport.setResellerGST("0.00");
					}

					if (StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_CHARGES.toString()))) {
						if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
								&& (StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.toString())))) {

							transReport.setTdr_Surcharge(String.format("%.2f",
									(Double.parseDouble(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
											+ Double.parseDouble(dbobj.getString(FieldType.PG_TDR_SC.toString()))
											+ Double.parseDouble(
													dbobj.getString(FieldType.RESELLER_CHARGES.toString())))));

						} else {
							transReport.setTdr_Surcharge("0.00");
						}
					} else {
						if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
								&& (StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.toString())))) {

							transReport.setTdr_Surcharge(String.format("%.2f",
									(Double.parseDouble(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
											+ Double.parseDouble(dbobj.getString(FieldType.PG_TDR_SC.toString())))));

						} else {
							transReport.setTdr_Surcharge("0.00");
						}
					}

					if (StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_GST.toString()))) {
						if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.toString()))
								&& (StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.toString())))) {
							transReport.setGst_charge(String.format("%.2f",
									(Double.parseDouble(dbobj.getString(FieldType.ACQUIRER_GST.toString()))
											+ Double.parseDouble(dbobj.getString(FieldType.PG_GST.toString()))
											+ Double.parseDouble(dbobj.getString(FieldType.RESELLER_GST.toString())))));

						} else {
							transReport.setGst_charge("0.00");
						}
					} else {
						if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.toString()))
								&& (StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.toString())))) {
							transReport.setGst_charge(String.format("%.2f",
									(Double.parseDouble(dbobj.getString(FieldType.ACQUIRER_GST.toString()))
											+ Double.parseDouble(dbobj.getString(FieldType.PG_GST.toString())))));

						} else {
							transReport.setGst_charge("0.00");
						}
					}

				} // END

				else {

					if (StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_CHARGES.toString()))) {
						if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
								&& (StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.toString())))) {

							transReport.setTdr_Surcharge(String.format("%.2f",
									(Double.parseDouble(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
											+ Double.parseDouble(dbobj.getString(FieldType.PG_TDR_SC.toString()))
											+ Double.parseDouble(
													dbobj.getString(FieldType.RESELLER_CHARGES.toString())))));

						} else {
							transReport.setTdr_Surcharge("0.00");
						}
					} else {
						if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
								&& (StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.toString())))) {

							transReport.setTdr_Surcharge(String.format("%.2f",
									(Double.parseDouble(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
											+ Double.parseDouble(dbobj.getString(FieldType.PG_TDR_SC.toString())))));

						} else {
							transReport.setTdr_Surcharge("0.00");
						}
					}

					if (StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_GST.toString()))) {
						if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.toString()))
								&& (StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.toString())))) {
							transReport.setGst_charge(String.format("%.2f",
									(Double.parseDouble(dbobj.getString(FieldType.ACQUIRER_GST.toString()))
											+ Double.parseDouble(dbobj.getString(FieldType.PG_GST.toString()))
											+ Double.parseDouble(dbobj.getString(FieldType.RESELLER_GST.toString())))));

						} else {
							transReport.setGst_charge("0.00");
						}
					} else {
						if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.toString()))
								&& (StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.toString())))) {
							transReport.setGst_charge(String.format("%.2f",
									(Double.parseDouble(dbobj.getString(FieldType.ACQUIRER_GST.toString()))
											+ Double.parseDouble(dbobj.getString(FieldType.PG_GST.toString())))));

						} else {
							transReport.setGst_charge("0.00");
						}
					}

				}

				if (TxnType.SALE.getName().equalsIgnoreCase(dbobj.getString(FieldType.ORIG_TXNTYPE.getName()))) {

					if (StringUtils.isNotBlank(dbobj.getString(FieldType.PAYMENT_TYPE.toString()))) {

						if (dbobj.getString(FieldType.PAYMENT_TYPE.getName())
								.equalsIgnoreCase(PaymentType.COD.getCode())) {

							if (isGlocal == true) {
								transReport.setGlocalFlag(true);
								if (dbobj.containsKey(FieldType.UDF7.getName())) {

									if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF7.getName()))) {

										if (StringUtils.isNotBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
												&& StringUtils
														.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.toString()))
												&& StringUtils.isNotBlank(
														dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
												&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.toString()))
												&& StringUtils.isNotBlank(
														dbobj.getString(FieldType.ACQUIRER_GST.toString()))) {

											String merchantAmount = String.format("%.2f", Double
													.parseDouble(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
													- (Double.parseDouble(
															dbobj.getString(FieldType.PG_TDR_SC.toString()))
															+ Double.parseDouble(dbobj
																	.getString(FieldType.ACQUIRER_TDR_SC.toString()))
															+ Double.parseDouble(
																	dbobj.getString(FieldType.PG_GST.toString()))
															+ Double.parseDouble(dbobj
																	.getString(FieldType.ACQUIRER_GST.toString()))));

											transReport.setDoctor(String
													.valueOf(new BigDecimal(dbobj.getString(FieldType.UDF7.getName()))
															.multiply(new BigDecimal(merchantAmount))
															.divide(new BigDecimal(
																	Constants.MAX_NUMBER_OF_KEYS.getValue()))
															.setScale(2, BigDecimal.ROUND_HALF_UP)));
										} else {
											transReport.setDoctor("0");
										}
									} else {
										transReport.setDoctor("0");
									}
								} else {
									transReport.setDoctor("0");
								}

								if (dbobj.containsKey(FieldType.UDF8.getName())) {
									if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF8.getName()))) {

										if (StringUtils.isNotBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
												&& StringUtils
														.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.toString()))
												&& StringUtils.isNotBlank(
														dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
												&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.toString()))
												&& StringUtils.isNotBlank(
														dbobj.getString(FieldType.ACQUIRER_GST.toString()))) {

											String merchantAmount = String.format("%.2f", Double
													.parseDouble(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
													- (Double.parseDouble(
															dbobj.getString(FieldType.PG_TDR_SC.toString()))
															+ Double.parseDouble(dbobj
																	.getString(FieldType.ACQUIRER_TDR_SC.toString()))
															+ Double.parseDouble(
																	dbobj.getString(FieldType.PG_GST.toString()))
															+ Double.parseDouble(dbobj
																	.getString(FieldType.ACQUIRER_GST.toString()))));

											transReport.setGlocal(String
													.valueOf(new BigDecimal(dbobj.getString(FieldType.UDF8.getName()))
															.multiply(new BigDecimal(merchantAmount))
															.divide(new BigDecimal(
																	Constants.MAX_NUMBER_OF_KEYS.getValue()))
															.setScale(2, BigDecimal.ROUND_HALF_UP)));
										} else {
											transReport.setGlocal("0");
										}
									} else {
										transReport.setGlocal("0");
									}
								} else {
									transReport.setGlocal("0");
								}

								if (dbobj.containsKey(FieldType.UDF9.getName())) {
									if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF9.getName()))) {

										if (StringUtils.isNotBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
												&& StringUtils
														.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.toString()))
												&& StringUtils.isNotBlank(
														dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
												&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.toString()))
												&& StringUtils.isNotBlank(
														dbobj.getString(FieldType.ACQUIRER_GST.toString()))) {

											String merchantAmount = String.format("%.2f", Double
													.parseDouble(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
													- (Double.parseDouble(
															dbobj.getString(FieldType.PG_TDR_SC.toString()))
															+ Double.parseDouble(dbobj
																	.getString(FieldType.ACQUIRER_TDR_SC.toString()))
															+ Double.parseDouble(
																	dbobj.getString(FieldType.PG_GST.toString()))
															+ Double.parseDouble(dbobj
																	.getString(FieldType.ACQUIRER_GST.toString()))));

											transReport.setPartner(String
													.valueOf(new BigDecimal(dbobj.getString(FieldType.UDF9.getName()))
															.multiply(new BigDecimal(merchantAmount))
															.divide(new BigDecimal(
																	Constants.MAX_NUMBER_OF_KEYS.getValue()))
															.setScale(2, BigDecimal.ROUND_HALF_UP)));
										} else {
											transReport.setPartner("0");
										}
									} else {
										transReport.setPartner("0");
									}
								} else {
									transReport.setPartner("0");
								}

								if (dbobj.containsKey(FieldType.UDF11.getName())) {
									if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF11.getName()))) {
										transReport.setUniqueId(dbobj.getString(FieldType.UDF11.getName()));
									} else {
										transReport.setUniqueId("NA");
									}
								} else {
									transReport.setUniqueId("NA");
								}
							} else {
								transReport.setGlocalFlag(false);
							}

							if (StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_CHARGES.toString()))
									&& StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_GST.toString()))) {
								if (StringUtils.isNotBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
										&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.toString()))
										&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
										&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.toString()))
										&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.toString()))) {
									transReport.setTotalAmtPayable(String.format("%.2f", (Double
											.parseDouble(dbobj.getString(FieldType.PG_TDR_SC.toString()))
											+ Double.parseDouble(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
											+ Double.parseDouble(dbobj.getString(FieldType.PG_GST.toString()))
											+ Double.parseDouble(dbobj.getString(FieldType.ACQUIRER_GST.toString()))
											+ Double.parseDouble(dbobj.getString(FieldType.RESELLER_CHARGES.toString()))
											+ Double.parseDouble(dbobj.getString(FieldType.RESELLER_GST.toString())))));

									if (transReport.getTotalAmtPayable().equalsIgnoreCase("0.00")) {
										transReport.setTotalAmtPayable(transReport.getTotalAmtPayable());
									} else {
										transReport.setTotalAmtPayable("-" + transReport.getTotalAmtPayable());
									}
								} else {
									transReport.setTotalAmtPayable("NA");
								}

							} else {

								if (StringUtils.isNotBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
										&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.toString()))
										&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
										&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.toString()))
										&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.toString()))) {
									transReport.setTotalAmtPayable(String.format("%.2f", (Double
											.parseDouble(dbobj.getString(FieldType.PG_TDR_SC.toString()))
											+ Double.parseDouble(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
											+ Double.parseDouble(dbobj.getString(FieldType.PG_GST.toString()))
											+ Double.parseDouble(dbobj.getString(FieldType.ACQUIRER_GST.toString())))));
									if (transReport.getTotalAmtPayable().equalsIgnoreCase("0.00")) {
										transReport.setTotalAmtPayable(transReport.getTotalAmtPayable());
									} else {
										transReport.setTotalAmtPayable("-" + transReport.getTotalAmtPayable());
									}

								} else {
									transReport.setTotalAmtPayable("NA");
								}
							}

						} else if (isGlocal == true) {

							transReport.setGlocalFlag(true);

							if (dbobj.containsKey(FieldType.UDF7.getName())) {

								if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF7.getName()))) {

									if (StringUtils.isNotBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
											&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.toString()))
											&& StringUtils
													.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
											&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.toString()))
											&& StringUtils
													.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.toString()))) {

										String merchantAmount = String.format("%.2f", Double
												.parseDouble(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
												- (Double.parseDouble(dbobj.getString(FieldType.PG_TDR_SC.toString()))
														+ Double.parseDouble(
																dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
														+ Double.parseDouble(
																dbobj.getString(FieldType.PG_GST.toString()))
														+ Double.parseDouble(
																dbobj.getString(FieldType.ACQUIRER_GST.toString()))));

										transReport.setDoctor(
												String.valueOf(new BigDecimal(dbobj.getString(FieldType.UDF7.getName()))
														.multiply(new BigDecimal(merchantAmount))
														.divide(new BigDecimal(Constants.MAX_NUMBER_OF_KEYS.getValue()))
														.setScale(2, BigDecimal.ROUND_HALF_UP)));
									} else {
										transReport.setDoctor("0");
									}
								} else {
									transReport.setDoctor("0");
								}
							} else {
								transReport.setDoctor("0");
							}

							if (dbobj.containsKey(FieldType.UDF8.getName())) {
								if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF8.getName()))) {

									if (StringUtils.isNotBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
											&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.toString()))
											&& StringUtils
													.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
											&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.toString()))
											&& StringUtils
													.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.toString()))) {

										String merchantAmount = String.format("%.2f", Double
												.parseDouble(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
												- (Double.parseDouble(dbobj.getString(FieldType.PG_TDR_SC.toString()))
														+ Double.parseDouble(
																dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
														+ Double.parseDouble(
																dbobj.getString(FieldType.PG_GST.toString()))
														+ Double.parseDouble(
																dbobj.getString(FieldType.ACQUIRER_GST.toString()))));

										transReport.setGlocal(
												String.valueOf(new BigDecimal(dbobj.getString(FieldType.UDF8.getName()))
														.multiply(new BigDecimal(merchantAmount))
														.divide(new BigDecimal(Constants.MAX_NUMBER_OF_KEYS.getValue()))
														.setScale(2, BigDecimal.ROUND_HALF_UP)));
									} else {
										transReport.setGlocal("0");
									}
								} else {
									transReport.setGlocal("0");
								}
							} else {
								transReport.setGlocal("0");
							}

							if (dbobj.containsKey(FieldType.UDF9.getName())) {
								if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF9.getName()))) {

									if (StringUtils.isNotBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
											&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_TDR_SC.toString()))
											&& StringUtils
													.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
											&& StringUtils.isNotBlank(dbobj.getString(FieldType.PG_GST.toString()))
											&& StringUtils
													.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.toString()))) {

										String merchantAmount = String.format("%.2f", Double
												.parseDouble(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
												- (Double.parseDouble(dbobj.getString(FieldType.PG_TDR_SC.toString()))
														+ Double.parseDouble(
																dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
														+ Double.parseDouble(
																dbobj.getString(FieldType.PG_GST.toString()))
														+ Double.parseDouble(
																dbobj.getString(FieldType.ACQUIRER_GST.toString()))));

										transReport.setPartner(
												String.valueOf(new BigDecimal(dbobj.getString(FieldType.UDF9.getName()))
														.multiply(new BigDecimal(merchantAmount))
														.divide(new BigDecimal(Constants.MAX_NUMBER_OF_KEYS.getValue()))
														.setScale(2, BigDecimal.ROUND_HALF_UP)));
									} else {
										transReport.setPartner("0");
									}
								} else {
									transReport.setPartner("0");
								}
							} else {
								transReport.setPartner("0");
							}

							if (dbobj.containsKey(FieldType.UDF11.getName())) {
								if (StringUtils.isNotBlank(dbobj.getString(FieldType.UDF11.getName()))) {
									transReport.setUniqueId(dbobj.getString(FieldType.UDF11.getName()));
								} else {
									transReport.setUniqueId("NA");
								}
							} else {
								transReport.setUniqueId("NA");
							}
						} else {
							transReport.setGlocalFlag(false);
						}

						if (!dbobj.getString(FieldType.PAYMENT_TYPE.getName())
								.equalsIgnoreCase(PaymentType.COD.getCode())) {

							if (StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_CHARGES.toString()))
									&& StringUtils.isNotBlank(dbobj.getString(FieldType.RESELLER_GST.toString()))) {

								if (StringUtils.isNotBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
										&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
										&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.toString()))) {
									transReport.setTotalAmtPayable(String.format("%.2f", Double
											.parseDouble(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
											- (Double.parseDouble(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
													+ Double.parseDouble(
															dbobj.getString(FieldType.ACQUIRER_GST.toString()))
													+ Double.parseDouble(
															dbobj.getString(FieldType.PG_TDR_SC.toString()))
													+ Double.parseDouble(dbobj.getString(FieldType.PG_GST.toString()))
													+ Double.parseDouble(
															dbobj.getString(FieldType.RESELLER_CHARGES.toString()))
													+ Double.parseDouble(
															dbobj.getString(FieldType.RESELLER_GST.toString())))));
								} else {
									if (StringUtils.isNotBlank(dbobj.getString(FieldType.PAYMENT_TYPE.toString()))
											&& (dbobj.getString(FieldType.PAYMENT_TYPE.toString())
													.equalsIgnoreCase("RTGS")
													|| dbobj.getString(FieldType.PAYMENT_TYPE.toString())
															.equalsIgnoreCase("NEFT")
													|| dbobj.getString(FieldType.PAYMENT_TYPE.toString())
															.equalsIgnoreCase("IMPS"))) {

										transReport
												.setTotalAmtPayable(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()));
									} else {
										transReport.setTotalAmtPayable("NA");
									}
								}

							} else {

								if (StringUtils.isNotBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
										&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
										&& StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.toString()))) {
									transReport.setTotalAmtPayable(String.format("%.2f", Double
											.parseDouble(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
											- (Double.parseDouble(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))
													+ Double.parseDouble(
															dbobj.getString(FieldType.ACQUIRER_GST.toString()))
													+ Double.parseDouble(
															dbobj.getString(FieldType.PG_TDR_SC.toString()))
													+ Double.parseDouble(
															dbobj.getString(FieldType.PG_GST.toString())))));
								} else {
									if (StringUtils.isNotBlank(dbobj.getString(FieldType.PAYMENT_TYPE.toString()))
											&& (dbobj.getString(FieldType.PAYMENT_TYPE.toString())
													.equalsIgnoreCase("RTGS")
													|| dbobj.getString(FieldType.PAYMENT_TYPE.toString())
															.equalsIgnoreCase("NEFT")
													|| dbobj.getString(FieldType.PAYMENT_TYPE.toString())
															.equalsIgnoreCase("IMPS"))) {

										transReport
												.setTotalAmtPayable(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()));
									} else {
										transReport.setTotalAmtPayable("NA");
									}
								}
							}
						}

					} else {
						transReport.setTotalAmtPayable("NA");
					}

				} else if (TxnType.REFUND.getName()
						.equalsIgnoreCase(dbobj.getString(FieldType.ORIG_TXNTYPE.getName()))) {
					if (isGlocal == true) {
						transReport.setGlocalFlag(true);
					} else {
						transReport.setGlocalFlag(false);
					}
					if (StringUtils.isNotBlank(dbobj.getString(FieldType.PAYMENT_TYPE.getName()))) {

						if (dbobj.getString(FieldType.PAYMENT_TYPE.getName())
								.equalsIgnoreCase(PaymentType.COD.getCode())) {
							transReport.setGst_charge("0.00");
							transReport.setTdr_Surcharge("0.00");
							transReport.setTotalAmtPayable("0.00");
						} else {
							if (StringUtils.isNotBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))) {
								transReport.setTotalAmtPayable(String.format("%.2f",
										Double.parseDouble(dbobj.getString(FieldType.TOTAL_AMOUNT.toString())) * -1));
							} else {
								transReport.setTotalAmtPayable("NA");
							}
						}

					} else {
						transReport.setTotalAmtPayable("NA");
					}

				} else {
					transReport.setTotalAmtPayable("NA");
				}

				// Exclude certain records when loading search payment without
				// any filters

				Comparator<TransactionSearch> comp = (TransactionSearch a, TransactionSearch b) -> {

					if (a.getDateFrom().compareTo(b.getDateFrom()) > 0) {
						return -1;
					} else if (a.getDateFrom().compareTo(b.getDateFrom()) < 0) {
						return 1;
					} else {
						return 0;
					}
				};
				transactionList.add(transReport);
				Collections.sort(transactionList, comp);
			}
			cursor.close();
			logger.info("Inside TxnReports , transactionReport , transactionListSize = " + transactionList.size());
			return transactionList;
		}

		catch (Exception e) {
			logger.error("Exception occured in TxnReports , transactionReport , Exception = ", e);
			e.printStackTrace();
			return null;
		}
	}

	public Map<String, String> getEnachRegistrationDataToResendLink(String orderId, String pgRefNum) {

		String custEmail = null;
		String custMobile = null;
		String eMandateUrl = null;
		String returnUrl = null;
		String amount = null;
		String monthlyAmount = null;
		String frequency = null;
		String tenure = null;
		String payID = null;
		String hash = null;

		Map<String, String> responseMap = new HashMap<String, String>();

		List<BasicDBObject> queryList = new ArrayList<BasicDBObject>();

		queryList.add(new BasicDBObject(FieldType.ORDER_ID.getName(), orderId));
//		if (StringUtils.isNotBlank(pgRefNum)) {
//			queryList.add(new BasicDBObject(FieldType.PG_REF_NUM.getName(), pgRefNum));
//		}
		queryList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), "Registration"));
		queryList.add(new BasicDBObject(FieldType.EMANDATE_URL.getName(), new BasicDBObject("$exists", true)));

		BasicDBObject query = new BasicDBObject("$and", queryList);
		logger.info("Inside getEnachRegistrationDataToResendLink , query = " + query);
		MongoDatabase dbIns = mongoInstance.getDB();
		MongoCollection<Document> coll = dbIns
				.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.ENACH_COLLECTION.getValue()));
		BasicDBObject match = new BasicDBObject("$match", query);
		BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject("CREATE_DATE", -1));
		List<BasicDBObject> pipeline = Arrays.asList(match, sort);

		AggregateIterable<Document> output = coll.aggregate(pipeline);
		output.allowDiskUse(true);
		MongoCursor<Document> cursor = output.iterator();
		while (cursor.hasNext()) {
			Document dbObj = cursor.next();
			if (StringUtils.isNotBlank(dbObj.getString("IS_ENCRYPTED"))
					&& dbObj.getString("IS_ENCRYPTED").equalsIgnoreCase("Y")) {
				dbObj = dataEncDecTool.decryptDocument(dbObj);
			}
			custEmail = dbObj.getString(FieldType.CUST_EMAIL.getName());
			custMobile = dbObj.getString(FieldType.CUST_PHONE.getName());
			returnUrl = dbObj.getString(FieldType.RETURN_URL.getName());
			amount = dbObj.getString(FieldType.AMOUNT.getName());
			monthlyAmount = dbObj.getString(FieldType.MONTHLY_AMOUNT.getName());
			frequency = dbObj.getString(FieldType.FREQUENCY.getName());
			tenure = dbObj.getString(FieldType.TENURE.getName());
			payID = dbObj.getString(FieldType.PAY_ID.getName());
			hash = dbObj.getString(FieldType.HASH.getName());
			eMandateUrl = dbObj.getString(FieldType.EMANDATE_URL.getName());

			if (eMandateUrl != null && hash != null && returnUrl != null)
				break;
		}

		responseMap.put(FieldType.CUST_EMAIL.getName(), custEmail);
		responseMap.put(FieldType.CUST_MOBILE.getName(), custMobile);
		responseMap.put(FieldType.RETURN_URL.getName(), returnUrl);
		responseMap.put(FieldType.AMOUNT.getName(), amount);
		responseMap.put(FieldType.MONTHLY_AMOUNT.getName(), monthlyAmount);
		responseMap.put(FieldType.FREQUENCY.getName(), frequency);
		responseMap.put(FieldType.TENURE.getName(), tenure);
		responseMap.put(FieldType.PAY_ID.getName(), payID);
		responseMap.put(FieldType.HASH.getName(), hash);
		responseMap.put(FieldType.ORDER_ID.getName(), orderId);
		responseMap.put(FieldType.EMANDATE_URL.getName(), eMandateUrl);

		return responseMap;
	}

	public Map<String, String> getUpiAutopayRegistrationDataToResendLink(String orderId, String pgRefNum) {

		String custEmail = null;
		String custMobile = null;
		String eMandateUrl = null;
		String returnUrl = null;
		String amount = null;
		String monthlyAmount = null;
		String frequency = null;
		String tenure = null;
		String payID = null;
		String hash = null;
		String purpose = null;

		Map<String, String> responseMap = new HashMap<String, String>();

		List<BasicDBObject> queryList = new ArrayList<BasicDBObject>();

		queryList.add(new BasicDBObject(FieldType.ORDER_ID.getName(), orderId));
//		if (StringUtils.isNotBlank(pgRefNum)) {
//			queryList.add(new BasicDBObject(FieldType.PG_REF_NUM.getName(), pgRefNum));
//		}
		queryList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.REGISTRATION.getName()));
		queryList.add(new BasicDBObject(FieldType.EMANDATE_URL.getName(), new BasicDBObject("$exists", true)));

		BasicDBObject query = new BasicDBObject("$and", queryList);
		logger.info("Inside getUpiAutopayRegistrationDataToResendLink , query = " + query);
		MongoDatabase dbIns = mongoInstance.getDB();
		MongoCollection<Document> coll = dbIns.getCollection(
				propertiesManager.propertiesMap.get(prefix + Constants.UPI_AUTOPAY_COLLECTION.getValue()));
		BasicDBObject match = new BasicDBObject("$match", query);
		BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject("CREATE_DATE", -1));
		List<BasicDBObject> pipeline = Arrays.asList(match, sort);

		AggregateIterable<Document> output = coll.aggregate(pipeline);
		output.allowDiskUse(true);
		MongoCursor<Document> cursor = output.iterator();
		while (cursor.hasNext()) {
			Document dbObj = cursor.next();
			if (StringUtils.isNotBlank(dbObj.getString("IS_ENCRYPTED"))
					&& dbObj.getString("IS_ENCRYPTED").equalsIgnoreCase("Y")) {
				dbObj = dataEncDecTool.decryptDocument(dbObj);
			}
			custEmail = dbObj.getString(FieldType.CUST_EMAIL.getName());
			custMobile = dbObj.getString(FieldType.CUST_PHONE.getName());
			returnUrl = dbObj.getString(FieldType.RETURN_URL.getName());
			amount = dbObj.getString(FieldType.AMOUNT.getName());
			monthlyAmount = dbObj.getString(FieldType.MONTHLY_AMOUNT.getName());
			frequency = dbObj.getString(FieldType.FREQUENCY.getName());
			tenure = dbObj.getString(FieldType.TENURE.getName());
			payID = dbObj.getString(FieldType.PAY_ID.getName());
			hash = dbObj.getString(FieldType.HASH.getName());
			eMandateUrl = dbObj.getString(FieldType.EMANDATE_URL.getName());
			purpose = dbObj.getString(FieldType.PURPOSE.getName());

			if (eMandateUrl != null && hash != null)
				break;
		}

		responseMap.put(FieldType.CUST_EMAIL.getName(), custEmail);
		responseMap.put(FieldType.CUST_MOBILE.getName(), custMobile);
		responseMap.put(FieldType.RETURN_URL.getName(), returnUrl);
		responseMap.put(FieldType.AMOUNT.getName(), amount);
		responseMap.put(FieldType.MONTHLY_AMOUNT.getName(), monthlyAmount);
		responseMap.put(FieldType.FREQUENCY.getName(), frequency);
		responseMap.put(FieldType.TENURE.getName(), tenure);
		responseMap.put(FieldType.PAY_ID.getName(), payID);
		responseMap.put(FieldType.HASH.getName(), hash);
		responseMap.put(FieldType.ORDER_ID.getName(), orderId);
		responseMap.put(FieldType.EMANDATE_URL.getName(), eMandateUrl);
		responseMap.put(FieldType.PURPOSE.getName(), purpose);

		return responseMap;
	}

	public List<TransactionSearchDownloadObject> searchMprPaymentForDownload(String merchantPayId,
			String subMerchantPayId, String customerEmail, String paymentType, String status, String currency,
			String transactionType, String fromDate, String toDate, String paymentsRegion, String acquirer,
			String partSettleFlag, Set<String> orderIdSet, String transactionFlag, String deltaFlag, String pgRefNum,
			String orderId, String autoRefund) {

		logger.info("Inside TxnReports , searchMprPaymentForDownload");
		Map<String, User> userMap = new HashMap<String, User>();
		boolean isParameterised = false;

		List<TransactionSearchDownloadObject> transactionList = new ArrayList<TransactionSearchDownloadObject>();
		try {

			List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();
			List<String> acquirerConditionLst = new ArrayList<String>();
			BasicDBObject acquirerQuery = new BasicDBObject();
			BasicDBObject customerQuery = new BasicDBObject();
			BasicDBObject customerQueryMask = new BasicDBObject();
			BasicDBObject txnCapturedFlag = new BasicDBObject();
			BasicDBObject dateIndexConditionQuery = new BasicDBObject();

			DateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.ENGLISH);
			Date dateStart = format.parse(fromDate);
			Date dateEnd = format.parse(toDate);

			LocalDate startDate = dateStart.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			LocalDate endDate = dateEnd.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

			String fromDateIndex = startDate.toString().replaceAll("-", "");
			String toDateIndex = endDate.toString().replaceAll("-", "");

			dateIndexConditionQuery.put("DATE_INDEX",
					BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(fromDateIndex).toLocalizedPattern())
							.add("$lte", new SimpleDateFormat(toDateIndex).toLocalizedPattern()).get());

			BasicDBObject orderIdConditionQuery = null;
			if (orderIdSet != null) {
				List<String> orderIdList = new ArrayList<>(orderIdSet);
				orderIdConditionQuery = new BasicDBObject("$in", orderIdList);
			}

			if (!orderId.isEmpty()) {
				paramConditionLst.add(new BasicDBObject(FieldType.ORDER_ID.getName(), orderId));
			} else if (null != orderIdConditionQuery) {
				paramConditionLst.add(new BasicDBObject(FieldType.ORDER_ID.getName(), orderIdConditionQuery));
			}

			if (!pgRefNum.isEmpty()) {
				paramConditionLst.add(new BasicDBObject(FieldType.PG_REF_NUM.getName(), pgRefNum));
			}

			if (StringUtils.isNotBlank(merchantPayId) && !merchantPayId.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.PAY_ID.getName(), merchantPayId));
			}

			if (StringUtils.isNotBlank(subMerchantPayId) && !subMerchantPayId.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.SUB_MERCHANT_ID.getName(), subMerchantPayId));
			}

			if (StringUtils.isNotEmpty(customerEmail) && !customerEmail.isEmpty()
					&& !customerEmail.equalsIgnoreCase("ALL")) {
				isParameterised = true;
				customerQuery.append(FieldType.CUST_EMAIL.getName(), customerEmail);
				if (StringUtils.isNotBlank(customerEmail)) {
					String email = customerEmail;
					String emailMask = null;
					final String mask = "*****";
					final int at = email.indexOf("@");
					if (at > 2) {
						final int maskLen = Math.min(Math.max(at / 2, 2), 5);
						final int start1 = (at - maskLen) / 2;
						emailMask = email.substring(0, start1) + mask.substring(0, maskLen)
								+ email.substring(start1 + maskLen);
					}
					customerQueryMask.append(FieldType.CUST_EMAIL.getName(), emailMask);
				}
			}

			paramConditionLst.add(new BasicDBObject(FieldType.STATUS.getName(), status));

			if (StringUtils.isNotBlank(currency) && !currency.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.CURRENCY_CODE.getName(), currency));
			}

			if (StringUtils.isNotBlank(partSettleFlag) && !partSettleFlag.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.PART_SETTLE.getName(), partSettleFlag));
			}

//			if (StringUtils.isNotBlank(transactionType) && !transactionType.equalsIgnoreCase("ALL")) {
//				paramConditionLst.add(new BasicDBObject(FieldType.ORIG_TXNTYPE.getName(), transactionType));
//			}

			if (StringUtils.isNotBlank(paymentType) && !paymentType.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.PAYMENT_TYPE.getName(), paymentType));
			}

			if (StringUtils.isNotBlank(paymentsRegion) && !paymentsRegion.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.PAYMENTS_REGION.getName(), paymentsRegion));
			}

			if (StringUtils.isNotBlank(transactionFlag) && !transactionFlag.equalsIgnoreCase("ALL")) {
				txnCapturedFlag.append("$in", transactionFlag.split(","));
				paramConditionLst.add(new BasicDBObject(FieldType.TXN_CAPTURE_FLAG.getName(), txnCapturedFlag));
			}

			if (StringUtils.isNotBlank(deltaFlag) && !deltaFlag.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.DELTA_REFUND_FLAG.getName(), deltaFlag));
			}

			if (StringUtils.isNotBlank(autoRefund) && !autoRefund.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.AUTO_REFUND_FLAG.getName(), autoRefund));
			}

			if (StringUtils.isNotBlank(acquirer) && !acquirer.equalsIgnoreCase("ALL")) {
				List<String> acquirerList = Arrays.asList(acquirer.split(","));
				for (String acq : acquirerList) {
					acquirerConditionLst.add(acq.trim());
				}
				acquirerQuery.append("$in", acquirerConditionLst);
				paramConditionLst.add(new BasicDBObject(FieldType.ACQUIRER_TYPE.getName(), acquirerQuery));
			}

			List<BasicDBObject> fianlQueryList = new ArrayList<BasicDBObject>();

			if (StringUtils.isNotBlank(pgRefNum) || StringUtils.isNotBlank(orderId)) {

			} else {
				if (!dateIndexConditionQuery.isEmpty()) {
					paramConditionLst.add(dateIndexConditionQuery);
				}
			}

			fianlQueryList.addAll(paramConditionLst);
			if (!customerQuery.isEmpty()) {
				paramConditionLst.add(customerQuery);
			}

			if (!customerQueryMask.isEmpty()) {
				fianlQueryList.add(customerQueryMask);
			}

			BasicDBObject finalquery = new BasicDBObject("$and", paramConditionLst);
			BasicDBObject finalqueryMask = new BasicDBObject("$and", fianlQueryList);

			if (!customerQueryMask.isEmpty()) {
				logger.info("Inside TxnReports , searchMprPaymentForDownload , finalQuery = " + finalqueryMask);
			} else {
				logger.info("Inside TxnReports , searchMprPaymentForDownload , finalQuery = " + finalquery);
			}

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns
					.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));
			BasicDBObject match = new BasicDBObject("$match", finalquery);
			// Now the aggregate operation ()In case any parameter is passed in
			// search query
			// , then show all records
			BasicDBObject sort = new BasicDBObject("$sort", new BasicDBObject("CREATE_DATE", -1));

			List<BasicDBObject> pipeline = Arrays.asList(match, sort);

			AggregateIterable<Document> output = coll.aggregate(pipeline);
			output.allowDiskUse(true);
			MongoCursor<Document> cursor = output.iterator();
			int count = 0;
			while (cursor.hasNext()) {
				Document dbobj = cursor.next();
				if (StringUtils.isNotBlank(dbobj.getString("IS_ENCRYPTED"))
						&& dbobj.getString("IS_ENCRYPTED").equalsIgnoreCase("Y")) {
					dbobj = dataEncDecTool.decryptDocument(dbobj);
				}

				TransactionSearchDownloadObject transReport = new TransactionSearchDownloadObject();

				transReport.setTransactionId(dbobj.getString(FieldType.TXN_ID.toString()));

				transReport.setTxnType(dbobj.getString(FieldType.TXNTYPE.toString()));

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.PAYMENT_TYPE.toString()))) {
					transReport.setPaymentMethods(
							PaymentType.getpaymentName(dbobj.getString(FieldType.PAYMENT_TYPE.toString())));
				} else {
					transReport.setPaymentMethods(CrmFieldConstants.NA.getValue());
				}

				transReport.setDateFrom(dbobj.getString(FieldType.CREATE_DATE.getName()));

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))) {
					transReport.setTotalAmount(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()));
				} else {
					transReport.setTotalAmount(CrmFieldConstants.NA.getValue());
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQ_ID.getName()))) {
					transReport.setAcqId(dbobj.getString(FieldType.ACQ_ID.getName()));
				} else {
					transReport.setAcqId(CrmFieldConstants.NA.getValue());
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.PG_REF_NUM.toString()))) {
					transReport.setPgRefNum(dbobj.getString(FieldType.PG_REF_NUM.toString()));
				} else {
					transReport.setPgRefNum(CrmFieldConstants.NA.getValue());
				}
				if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()))) {
					transReport.setAcquirerTdrOrSurcharge(dbobj.getString(FieldType.ACQUIRER_TDR_SC.toString()));
				} else {
					transReport.setAcquirerTdrOrSurcharge(CrmFieldConstants.NA.getValue());
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.ACQUIRER_GST.toString()))) {
					transReport.setAcquirerGST(dbobj.getString(FieldType.ACQUIRER_GST.toString()));
				} else {
					transReport.setAcquirerGST(CrmFieldConstants.NA.getValue());
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.RRN.getName()))) {
					transReport.setRrn(dbobj.getString(FieldType.RRN.toString()));
				} else {
					transReport.setRrn(CrmFieldConstants.NA.getValue());
				}

				if (StringUtils.isNotBlank(dbobj.getString(FieldType.CARD_MASK.toString()))) {
					transReport.setCardNumber(((Document) dbobj).getString(FieldType.CARD_MASK.toString()));
				} else if (StringUtils.isNotBlank(dbobj.getString(FieldType.PAYER_ADDRESS.getName()))) {
					transReport.setCardNumber(dbobj.getString(FieldType.PAYER_ADDRESS.getName()));
				} else {
					transReport.setCardNumber(CrmFieldConstants.NA.getValue());
				}

				transactionList.add(transReport);
			}
			logger.info("transactionList created and size = " + transactionList.size());
			cursor.close();
			logger.info("Inside TxnReports , searchMPRPaymentForDownload , transactionListSize = "
					+ transactionList.size());
			Comparator<TransactionSearchDownloadObject> comp = (TransactionSearchDownloadObject a,
					TransactionSearchDownloadObject b) -> {
				if (a.getDateFrom().compareTo(b.getDateFrom()) > 0) {
					return -1;
				} else if (a.getDateFrom().compareTo(b.getDateFrom()) < 0) {
					return 1;
				} else {
					return 0;
				}
			};

			Collections.sort(transactionList, comp);
			logger.info("transactionList created and Sorted");
			return transactionList;
		} catch (Exception e) {
			logger.error("Exception occured in TxnReports , searchPayment , Exception = ", e);
			return transactionList;
		}
	}

}
