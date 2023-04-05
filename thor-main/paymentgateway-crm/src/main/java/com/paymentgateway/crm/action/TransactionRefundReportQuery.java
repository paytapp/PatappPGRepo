package com.paymentgateway.crm.action;

import java.math.BigInteger;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.mongo.MongoInstance;
import com.paymentgateway.commons.user.TransactionSearch;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CrmFieldConstants;
import com.paymentgateway.commons.util.CrmFieldType;
import com.paymentgateway.commons.util.DataEncDecTool;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TransactionType;

@Component
public class TransactionRefundReportQuery {
	@Autowired
	private MongoInstance mongoInstance;

	@Autowired
	PropertiesManager propertiesManager;
	
	@Autowired
	private UserDao userDao;
	
	@Autowired
	private DataEncDecTool dataEncDecTool;

	private static final String alphabaticFileName = "alphabatic-currencycode.properties";
	private static final String prefix = "MONGO_DB_";

	public List<TransactionSearch> refundReport(String fromDate, String toDate, String payId, String subMerchantPayId, String paymentType,
			String acquirer, String currency, String pgRefNum, User user, int start, int length) throws SystemException {

		Map<String, User> userMap = new HashMap<String, User>();
		
		List<TransactionSearch> transactionList = new ArrayList<TransactionSearch>();
		BasicDBObject dateQuery = new BasicDBObject();
		List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();
		BasicDBObject currencyQuery = new BasicDBObject();
		BasicDBObject acquirerQuery = new BasicDBObject();

		BasicDBObject allParamQuery = new BasicDBObject();
		List<BasicDBObject> acquirerConditionLst = new ArrayList<BasicDBObject>();
		List<BasicDBObject> userTypeLst = new ArrayList<BasicDBObject>();
		List<BasicDBObject> currencyConditionLst = new ArrayList<BasicDBObject>();
		List<BasicDBObject> paymentTypeConditionLst = new ArrayList<BasicDBObject>();
		BasicDBObject paymentTypeQuery = new BasicDBObject();
		if (!fromDate.isEmpty()) {

			String currentDate = null;
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
		if (!payId.equalsIgnoreCase("ALL")) {
			paramConditionLst.add(new BasicDBObject(FieldType.PAY_ID.getName(), payId));
		}
		if (StringUtils.isNotBlank(subMerchantPayId) && !subMerchantPayId.equalsIgnoreCase("ALL")) {
			paramConditionLst.add(new BasicDBObject(FieldType.SUB_MERCHANT_ID.getName(), subMerchantPayId));
		}
		if (StringUtils.isNotBlank(pgRefNum)) {
			paramConditionLst.add(new BasicDBObject(FieldType.PG_REF_NUM.getName(), pgRefNum));
		}
		if (!currency.equalsIgnoreCase("ALL")) {
			paramConditionLst.add(new BasicDBObject(FieldType.CURRENCY_CODE.getName(), currency));
		} else {
			PropertiesManager propertiesManager = new PropertiesManager();
			Map<String, String> allCurrencyMap;
			allCurrencyMap = propertiesManager.getAllProperties(alphabaticFileName);
			for (Map.Entry<String, String> entry : allCurrencyMap.entrySet()) {

				currencyConditionLst.add(new BasicDBObject(FieldType.CURRENCY_CODE.getName(), entry.getKey()));
			}

			currencyQuery.append("$or", currencyConditionLst);
		}

		if (!acquirer.equalsIgnoreCase("ALL")) {

			List<String> acquirerList = Arrays.asList(acquirer.split(","));
			for (String acq : acquirerList) {

				acquirerConditionLst.add(new BasicDBObject(FieldType.ACQUIRER_TYPE.getName(), acq));
			}
			acquirerQuery.append("$or", acquirerConditionLst);

		} else {
			/*
			 * acquirerConditionLst.add(new BasicDBObject(FieldType.ACQUIRER_TYPE.getName(),
			 * "AMEX")); acquirerConditionLst.add(new
			 * BasicDBObject(FieldType.ACQUIRER_TYPE.getName(), "CITRUS"));
			 * acquirerConditionLst.add(new BasicDBObject(FieldType.ACQUIRER_TYPE.getName(),
			 * "EZEECLICK")); acquirerConditionLst.add(new
			 * BasicDBObject(FieldType.ACQUIRER_TYPE.getName(), "FSS"));
			 * acquirerConditionLst.add(new BasicDBObject(FieldType.ACQUIRER_TYPE.getName(),
			 * "MOBIKWIK")); acquirerConditionLst.add(new
			 * BasicDBObject(FieldType.ACQUIRER_TYPE.getName(), "PAYTM"));
			 * acquirerConditionLst.add(new BasicDBObject(FieldType.ACQUIRER_TYPE.getName(),
			 * "YESBANK")); acquirerConditionLst.add(new
			 * BasicDBObject(FieldType.ACQUIRER_TYPE.getName(), "KOTAK"));
			 * acquirerConditionLst.add(new BasicDBObject(FieldType.ACQUIRER_TYPE.getName(),
			 * "DIRECPAY")); acquirerConditionLst.add(new
			 * BasicDBObject(FieldType.ACQUIRER_TYPE.getName(), "BOB"));
			 * acquirerQuery.append("$or", acquirerConditionLst);
			 */
		}

		/*
		 * if (!paymentType.equalsIgnoreCase("ALL")) { paramConditionLst.add(new
		 * BasicDBObject(FieldType.PAYMENT_TYPE.getName(), paymentType)); } else {
		 * paymentTypeConditionLst .add(new
		 * BasicDBObject(FieldType.PAYMENT_TYPE.getName(),
		 * PaymentType.CREDIT_CARD.getCode())); paymentTypeConditionLst .add(new
		 * BasicDBObject(FieldType.PAYMENT_TYPE.getName(),
		 * PaymentType.DEBIT_CARD.getCode())); paymentTypeConditionLst .add(new
		 * BasicDBObject(FieldType.PAYMENT_TYPE.getName(),
		 * PaymentType.NET_BANKING.getCode())); paymentTypeConditionLst.add(new
		 * BasicDBObject(FieldType.PAYMENT_TYPE.getName(), PaymentType.EMI.getCode()));
		 * paymentTypeConditionLst .add(new
		 * BasicDBObject(FieldType.PAYMENT_TYPE.getName(),
		 * PaymentType.WALLET.getCode())); paymentTypeConditionLst .add(new
		 * BasicDBObject(FieldType.PAYMENT_TYPE.getName(),
		 * PaymentType.RECURRING_PAYMENT.getCode())); paymentTypeConditionLst .add(new
		 * BasicDBObject(FieldType.PAYMENT_TYPE.getName(),
		 * PaymentType.EXPRESS_PAY.getCode())); paymentTypeConditionLst .add(new
		 * BasicDBObject(FieldType.PAYMENT_TYPE.getName(), null));
		 * paymentTypeQuery.append("$or", paymentTypeConditionLst); }
		 */
		List<BasicDBObject> refundConditionList = new ArrayList<BasicDBObject>();
		refundConditionList.add(new BasicDBObject(FieldType.ORIG_TXNTYPE.getName(), TransactionType.REFUND.getName()));
		refundConditionList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.SETTLED.getName()));

		BasicDBObject refundConditionQuery = new BasicDBObject("$and", refundConditionList);
		if (!paramConditionLst.isEmpty()) {
			allParamQuery = new BasicDBObject("$and", paramConditionLst);
		}

		List<BasicDBObject> allConditionQueryList = new ArrayList<BasicDBObject>();
		if (!currencyQuery.isEmpty()) {
			allConditionQueryList.add(currencyQuery);
		}
		if (!acquirerQuery.isEmpty()) {
			allConditionQueryList.add(acquirerQuery);
		}
		if (!dateQuery.isEmpty()) {
			allConditionQueryList.add(dateQuery);
		}
		if (!refundConditionQuery.isEmpty()) {
			allConditionQueryList.add(refundConditionQuery);
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

			Document doc = cursor.next();
			if (StringUtils.isNotBlank(doc.getString("IS_ENCRYPTED"))
					&& doc.getString("IS_ENCRYPTED").equalsIgnoreCase("Y")) {
				doc = dataEncDecTool.decryptDocument(doc);
			}
			TransactionSearch transReport = new TransactionSearch();
			transReport.setRefundDate(doc.getString(FieldType.CREATE_DATE.getName()));
			transReport.setRefundFlag(doc.getString(FieldType.REFUND_FLAG.toString()));
			BigInteger txnId = new BigInteger(doc.getString(FieldType.TXN_ID.toString()));

			transReport.setRefundTxnId(txnId);
//			transReport.setCustomerName(doc.getString(CrmFieldType.BUSINESS_NAME.getName()));
			
			if(!payId.equalsIgnoreCase("All") && doc.containsKey(FieldType.SUB_MERCHANT_ID.getName()) && StringUtils.isNotBlank(doc.getString(FieldType.SUB_MERCHANT_ID.getName()))) {
				transReport.setSubMerchantId(userDao.getBusinessNameByPayId(doc.getString(FieldType.SUB_MERCHANT_ID.getName())));
			}
			
			String payid = (String) doc.get(FieldType.PAY_ID.getName());
			User user1 = new User();
			if (userMap.get(payid) != null && !userMap.get(payid).getPayId().isEmpty()) {
				user1 = userMap.get(payid);
			} else {
				user1 = userDao.findPayId(payid);
				userMap.put(payid, user1);
			}
			transReport.setMerchants(user1.getBusinessName());
			
			transReport.setOrderId(doc.getString(FieldType.ORDER_ID.toString()));
			transReport.setCustomerEmail(doc.getString(FieldType.CUST_EMAIL.toString()));
			transReport.setPaymentMethods(doc.getString(FieldType.PAYMENT_TYPE.toString()));
			if (null != doc.getString(FieldType.CURRENCY_CODE.toString())) {
				transReport.setCurrency(
						propertiesManager.getAlphabaticCurrencyCode(doc.getString(FieldType.CURRENCY_CODE.toString())));
			} else {
				transReport.setCurrency(CrmFieldConstants.NOT_AVAILABLE.getValue());
			}
			transReport.setRefundAmount(doc.getString(FieldType.AMOUNT.toString()));
			transReport.setRefundStatus(doc.getString(FieldType.STATUS.toString()));
			transReport.setPgRefNum(doc.getString(FieldType.PG_REF_NUM.toString()));
			transReport.setOrigTxnId(doc.getString(FieldType.ORIG_TXN_ID.toString()));

			transactionList.add(transReport);

		}
		cursor.close();

		return transactionList;

	}

	public int refundReportCount(String fromDate, String toDate, String payId, String subMerchantPayId, String paymentType, String acquirer,
			String currency, User user) throws SystemException {
		int total;

		List<TransactionSearch> transactionList = new ArrayList<TransactionSearch>();
		BasicDBObject dateQuery = new BasicDBObject();
		List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();
		BasicDBObject currencyQuery = new BasicDBObject();
		BasicDBObject acquirerQuery = new BasicDBObject();

		BasicDBObject allParamQuery = new BasicDBObject();
		List<BasicDBObject> acquirerConditionLst = new ArrayList<BasicDBObject>();
		List<BasicDBObject> userTypeLst = new ArrayList<BasicDBObject>();
		List<BasicDBObject> currencyConditionLst = new ArrayList<BasicDBObject>();
		List<BasicDBObject> paymentTypeConditionLst = new ArrayList<BasicDBObject>();
		BasicDBObject paymentTypeQuery = new BasicDBObject();

		if (!fromDate.isEmpty()) {

			String currentDate = null;
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
		if (!payId.equalsIgnoreCase("ALL")) {
			paramConditionLst.add(new BasicDBObject(FieldType.PAY_ID.getName(), payId));
		}
		if (StringUtils.isNotBlank(subMerchantPayId) && !subMerchantPayId.equalsIgnoreCase("ALL")) {
			paramConditionLst.add(new BasicDBObject(FieldType.SUB_MERCHANT_ID.getName(), subMerchantPayId));
		}
		if (!currency.equalsIgnoreCase("ALL")) {
			paramConditionLst.add(new BasicDBObject(FieldType.CURRENCY_CODE.getName(), currency));
		} else {
			PropertiesManager propertiesManager = new PropertiesManager();
			Map<String, String> allCurrencyMap;
			allCurrencyMap = propertiesManager.getAllProperties(alphabaticFileName);
			for (Map.Entry<String, String> entry : allCurrencyMap.entrySet()) {

				currencyConditionLst.add(new BasicDBObject(FieldType.CURRENCY_CODE.getName(), entry.getKey()));
			}

			currencyQuery.append("$or", currencyConditionLst);
		}

		if (!acquirer.equalsIgnoreCase("ALL")) {

			List<String> acquirerList = Arrays.asList(acquirer.split(","));
			for (String acq : acquirerList) {

				acquirerConditionLst.add(new BasicDBObject(FieldType.ACQUIRER_TYPE.getName(), acq));
			}
			acquirerQuery.append("$or", acquirerConditionLst);

		} else {
			/*
			 * acquirerConditionLst.add(new BasicDBObject(FieldType.ACQUIRER_TYPE.getName(),
			 * "AMEX")); acquirerConditionLst.add(new
			 * BasicDBObject(FieldType.ACQUIRER_TYPE.getName(), "CITRUS"));
			 * acquirerConditionLst.add(new BasicDBObject(FieldType.ACQUIRER_TYPE.getName(),
			 * "EZEECLICK")); acquirerConditionLst.add(new
			 * BasicDBObject(FieldType.ACQUIRER_TYPE.getName(), "FSS"));
			 * acquirerConditionLst.add(new BasicDBObject(FieldType.ACQUIRER_TYPE.getName(),
			 * "MOBIKWIK")); acquirerConditionLst.add(new
			 * BasicDBObject(FieldType.ACQUIRER_TYPE.getName(), "PAYTM"));
			 * acquirerConditionLst.add(new BasicDBObject(FieldType.ACQUIRER_TYPE.getName(),
			 * "YESBANK")); acquirerConditionLst.add(new
			 * BasicDBObject(FieldType.ACQUIRER_TYPE.getName(), "KOTAK"));
			 * acquirerConditionLst.add(new BasicDBObject(FieldType.ACQUIRER_TYPE.getName(),
			 * "DIRECPAY")); acquirerConditionLst.add(new
			 * BasicDBObject(FieldType.ACQUIRER_TYPE.getName(), "BOB"));
			 * acquirerQuery.append("$or", acquirerConditionLst);
			 */
		}

		/*
		 * if (!paymentType.equalsIgnoreCase("ALL")) { paramConditionLst.add(new
		 * BasicDBObject(FieldType.PAYMENT_TYPE.getName(), paymentType)); } else {
		 * paymentTypeConditionLst .add(new
		 * BasicDBObject(FieldType.PAYMENT_TYPE.getName(),
		 * PaymentType.CREDIT_CARD.getCode())); paymentTypeConditionLst .add(new
		 * BasicDBObject(FieldType.PAYMENT_TYPE.getName(),
		 * PaymentType.DEBIT_CARD.getCode())); paymentTypeConditionLst .add(new
		 * BasicDBObject(FieldType.PAYMENT_TYPE.getName(),
		 * PaymentType.NET_BANKING.getCode())); paymentTypeConditionLst.add(new
		 * BasicDBObject(FieldType.PAYMENT_TYPE.getName(), PaymentType.EMI.getCode()));
		 * paymentTypeConditionLst .add(new
		 * BasicDBObject(FieldType.PAYMENT_TYPE.getName(),
		 * PaymentType.WALLET.getCode())); paymentTypeConditionLst .add(new
		 * BasicDBObject(FieldType.PAYMENT_TYPE.getName(),
		 * PaymentType.RECURRING_PAYMENT.getCode())); paymentTypeConditionLst .add(new
		 * BasicDBObject(FieldType.PAYMENT_TYPE.getName(),
		 * PaymentType.EXPRESS_PAY.getCode())); paymentTypeConditionLst .add(new
		 * BasicDBObject(FieldType.PAYMENT_TYPE.getName(), null));
		 * paymentTypeQuery.append("$or", paymentTypeConditionLst); }
		 */
		List<BasicDBObject> refundConditionList = new ArrayList<BasicDBObject>();
		refundConditionList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.REFUNDRECO.getName()));
		refundConditionList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.SETTLED.getName()));

		BasicDBObject refundConditionQuery = new BasicDBObject("$and", refundConditionList);
		if (!paramConditionLst.isEmpty()) {
			allParamQuery = new BasicDBObject("$and", paramConditionLst);
		}

		List<BasicDBObject> allConditionQueryList = new ArrayList<BasicDBObject>();
		if (!currencyQuery.isEmpty()) {
			allConditionQueryList.add(currencyQuery);
		}
		if (!acquirerQuery.isEmpty()) {
			allConditionQueryList.add(acquirerQuery);
		}
		if (!dateQuery.isEmpty()) {
			allConditionQueryList.add(dateQuery);
		}
		if (!refundConditionQuery.isEmpty()) {
			allConditionQueryList.add(refundConditionQuery);
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

		MongoDatabase dbIns = mongoInstance.getDB();
		MongoCollection<Document> coll = dbIns
				.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));

		total = (int) coll.count(finalquery);

		return total;

	}

}
