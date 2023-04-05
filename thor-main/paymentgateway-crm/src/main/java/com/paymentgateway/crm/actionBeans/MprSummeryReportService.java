package com.paymentgateway.crm.actionBeans;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.mongo.MongoInstance;
import com.paymentgateway.commons.user.MprTransaction;
import com.paymentgateway.commons.user.NodalAmoutDao;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.AcquirerType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.PaymentType;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TransactionType;
import com.paymentgateway.commons.util.TxnType;

@Service
public class MprSummeryReportService {

	@Autowired
	private MongoInstance mongoInstance;

	@Autowired
	UserDao userDao;
	
	@Autowired
	NodalAmoutDao nodalDao;
	

	private static Logger logger = LoggerFactory.getLogger(MprSummeryReportService.class.getName());
	private static final String prefix = "MONGO_DB_";
	public static final BigDecimal ONE_HUNDRED = new BigDecimal(100);


	public List<MprTransaction> getMprCountService(String fromDate, String acquirer,
			String paymentType, User user) {
        		List<MprTransaction> mprTxnList = new ArrayList<MprTransaction>();
		List<String> txnTypeList = new ArrayList<String>();
		txnTypeList.add(TxnType.SALE.getName());
		txnTypeList.add(TxnType.REFUND.getName());
        
		try {

			BasicDBObject dateQuery = new BasicDBObject();
			List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();
			BasicDBObject acquirerQuery = new BasicDBObject();

			BasicDBObject allParamQuery = new BasicDBObject();
			List<BasicDBObject> acquirerConditionLst = new ArrayList<BasicDBObject>();
			List<BasicDBObject> saleSettledList = new ArrayList<BasicDBObject>();
			List<BasicDBObject> refundSettledList = new ArrayList<BasicDBObject>();

			if (!fromDate.isEmpty()) {
				String currentDate = null;
				
				DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				Calendar cal = Calendar.getInstance();
				 // add days to from date				
				Date date1= dateFormat.parse(fromDate);	
				cal.setTime(date1);
				cal.add(Calendar.DATE, 1);
				currentDate = dateFormat.format(cal.getTime());	

				dateQuery.put(FieldType.CREATE_DATE.getName(),
						BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(fromDate).toLocalizedPattern())
								.add("$lt", new SimpleDateFormat(currentDate).toLocalizedPattern()).get());
			}


			if (!acquirer.equalsIgnoreCase("ALL")) {

				List<String> acquirerList = Arrays.asList(acquirer.split(","));
				for (String acq : acquirerList) {
                   
					acquirerConditionLst.add(new BasicDBObject(FieldType.ACQUIRER_TYPE.getName(),AcquirerType.getAcquirerCode(acq)));

				}
				acquirerQuery.append("$or", acquirerConditionLst);
			}
			
			

			if (paymentType.equals("CC-DC")) {
				ArrayList<String> list = new ArrayList<>();
				list.add(PaymentType.CREDIT_CARD.getCode());
				list.add(PaymentType.DEBIT_CARD.getCode());
				paramConditionLst
						.add(new BasicDBObject(FieldType.PAYMENT_TYPE.getName(), new BasicDBObject("$in", list)));
			} else if (paymentType.equals("UPI")) {
				paramConditionLst.add(new BasicDBObject(FieldType.PAYMENT_TYPE.getName(), PaymentType.UPI.getCode()));
			} else if (paymentType.equalsIgnoreCase("ALL")) {
				ArrayList<String> list = new ArrayList<>();
				list.add(PaymentType.CREDIT_CARD.getCode());
				list.add(PaymentType.DEBIT_CARD.getCode());
				list.add(PaymentType.UPI.getCode());
				paramConditionLst
						.add(new BasicDBObject(FieldType.PAYMENT_TYPE.getName(), new BasicDBObject("$in", list)));
			}

			if (!paramConditionLst.isEmpty()) {
				allParamQuery = new BasicDBObject("$and", paramConditionLst);
			}

	
			// SALE Settled query
			List<BasicDBObject> saleSettledConditionList = new ArrayList<BasicDBObject>();
			saleSettledConditionList
					.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.RECO.getName()));
			saleSettledConditionList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.SETTLED.getName()));
			saleSettledConditionList
					.add(new BasicDBObject(FieldType.RESPONSE_CODE.getName(), ErrorType.SUCCESS.getCode()));

			BasicDBObject saleConditionQuery = new BasicDBObject("$and", saleSettledConditionList);
			saleSettledList.add(saleConditionQuery);

			// REFUND Settled query
			List<BasicDBObject> refundSettledConditionList = new ArrayList<BasicDBObject>();
			refundSettledConditionList
					.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.REFUNDRECO.getName()));
			refundSettledConditionList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.SETTLED.getName()));
			refundSettledConditionList
					.add(new BasicDBObject(FieldType.RESPONSE_CODE.getName(), ErrorType.SUCCESS.getCode()));

			BasicDBObject refundConditionQuery = new BasicDBObject("$and", refundSettledConditionList);
			refundSettledList.add(refundConditionQuery);
			
			
			List<BasicDBObject> transList = new ArrayList<BasicDBObject>();
			transList.add(saleConditionQuery);
			transList.add(refundConditionQuery);
			BasicDBObject transQuery = new BasicDBObject("$or", transList);
			
		
			List<BasicDBObject> allConditionQueryList = new ArrayList<BasicDBObject>();

			if (!acquirerQuery.isEmpty()) {
				allConditionQueryList.add(acquirerQuery);
			}
			if (!dateQuery.isEmpty()) {
				allConditionQueryList.add(dateQuery);
			}

			if (!transQuery.isEmpty()) {
				allConditionQueryList.add(transQuery);
			}

			BasicDBObject allConditionQueryObj = new BasicDBObject("$and", allConditionQueryList);

			List<BasicDBObject> fianlList = new ArrayList<BasicDBObject>();

			if (!allParamQuery.isEmpty()) {
				fianlList.add(allParamQuery);
			}
			if (!allConditionQueryObj.isEmpty()) {
				fianlList.add(allConditionQueryObj);
			}

			BasicDBObject finalQuery = new BasicDBObject("$and",fianlList);
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns
					.getCollection(PropertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));

			int totalSettled = (int) coll.count(finalQuery);
			// TODO remove delta count

			MongoCursor<Document> cursor = coll.find(finalQuery).iterator();

			boolean isExist = false;
			// Remove all data from an earlier map
			while (cursor.hasNext()) {

				Document dbobj = cursor.next();
				
				String payment = "";
				if (dbobj.getString("PAYMENT_TYPE").equals(PaymentType.CREDIT_CARD.getCode())
						|| dbobj.getString("PAYMENT_TYPE").equals(PaymentType.DEBIT_CARD.getCode())) {
					payment = "CC-DC";
				} else if (dbobj.getString("PAYMENT_TYPE").equals(PaymentType.UPI.getCode())) {
					payment = "UPI";
				}
				isExist = false;
				for(int i = 0; i < mprTxnList.size(); i++)
				{

					if (dbobj.getString(FieldType.PAY_ID.getName()).equals(mprTxnList.get(i).getMerchant())
							&& (mprTxnList.get(i).getPaymentType())
									.contains(dbobj.getString(FieldType.PAYMENT_TYPE.getName()))) {
						if (dbobj.getString(FieldType.TXNTYPE.getName()).equals(TransactionType.RECO.getName())) {

							Integer noOfTxn = mprTxnList.get(i).getSaleTxn();

							if (noOfTxn == null) {
								noOfTxn = 0;
							}

							noOfTxn += 1;
							mprTxnList.get(i).setSaleTxn(noOfTxn);
							BigDecimal amount = mprTxnList.get(i).getSaleMprAmount();
							if (amount == null) {
								amount = BigDecimal.ZERO;
							}

							BigDecimal txnAmount = new BigDecimal(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
									.setScale(2, RoundingMode.HALF_DOWN);
							;
							BigDecimal totalTxnAmount = amount.add(txnAmount).setScale(2, RoundingMode.HALF_DOWN);
							mprTxnList.get(i).setSaleMprAmount(totalTxnAmount);
							isExist = true;
							break;

						}

						else {

							Integer noOfTxn = mprTxnList.get(i).getRefundTxn();

							if (noOfTxn == null) {
								noOfTxn = 0;
							}

							noOfTxn += 1;
							mprTxnList.get(i).setRefundTxn(noOfTxn);
							BigDecimal amount = mprTxnList.get(i).getRefundMprAmount();
							if (amount == null) {
								amount = BigDecimal.ZERO;
							}

							BigDecimal txnAmount = new BigDecimal(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
									.setScale(2, RoundingMode.HALF_DOWN);
							;
							BigDecimal totalTxnAmount = amount.add(txnAmount).setScale(2, RoundingMode.HALF_DOWN);
							mprTxnList.get(i).setRefundMprAmount(totalTxnAmount);
							isExist = true;
							break;

						}

					}
				}
				if (!isExist) {

					MprTransaction transaction = new MprTransaction();
					if (dbobj.getString(FieldType.TXNTYPE.getName()).equals(TransactionType.RECO.getName())) {
						transaction.setSaleTxn(1);
						transaction.setRefundTxn(0);
						transaction.setAcquirer(dbobj.getString(FieldType.ACQUIRER_TYPE.getName()));
						transaction.setMerchant(dbobj.getString(FieldType.PAY_ID.getName()));
						transaction.setPaymentType(payment);
						BigDecimal txnAmount = new BigDecimal(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
								.setScale(2, RoundingMode.HALF_DOWN);
						transaction.setSaleMprAmount(txnAmount);
						transaction.setRefundMprAmount(BigDecimal.ZERO);
						mprTxnList.add(transaction);

					} else {

						transaction.setRefundTxn(1);
						transaction.setSaleTxn(0);
						transaction.setAcquirer(dbobj.getString(FieldType.ACQUIRER_TYPE.getName()));
						transaction.setMerchant(dbobj.getString(FieldType.PAY_ID.getName()));
						transaction.setPaymentType(payment);
						BigDecimal txnAmount = new BigDecimal(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
								.setScale(2, RoundingMode.HALF_DOWN);
						transaction.setRefundMprAmount(txnAmount);
						transaction.setSaleMprAmount(BigDecimal.ZERO);
						mprTxnList.add(transaction);

					}
				}
			}
			
			
			
		
			cursor.close();

			for(int i =mprTxnList.size()-1; i>=0;i--) {
				if((StringUtils.isBlank(String.valueOf(mprTxnList.get(i).getSaleMprAmount())) || (mprTxnList.get(i).getSaleMprAmount()).equals(BigDecimal.ZERO)) 
						&& (StringUtils.isBlank(String.valueOf(mprTxnList.get(i).getRefundMprAmount())) || (mprTxnList.get(i).getRefundMprAmount()).equals(BigDecimal.ZERO))) {
					mprTxnList.remove(i);
				}
			}
			
			
			for (MprTransaction mpr : mprTxnList) {
				BigDecimal nodalCreditAmount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_DOWN);
				String amountCreditDate = null;
				Integer salTxn = mpr.getSaleTxn();
				Integer refundTxn = mpr.getRefundTxn();
				BigDecimal saleAmount = mpr.getSaleMprAmount();
				BigDecimal refundAmount = mpr.getRefundMprAmount();
				
				String merchantId=mpr.getMerchant();
				String merchant=userDao.getMerchantByPayId(merchantId);
				mpr.setMerchant(merchant);

				if (saleAmount == null) {
					saleAmount = BigDecimal.ZERO;
				}
				if (refundAmount == null) {
					refundAmount = BigDecimal.ZERO;
				}

				if (salTxn == null) {
					salTxn = 0;
				}

				if (refundTxn == null) {
					refundTxn = 0;
				}
				Integer netTotalTxn = salTxn + refundTxn;

				
				BigDecimal netTotalAmount = saleAmount.subtract(refundAmount).setScale(2, RoundingMode.HALF_DOWN);
//				BigDecimal nodalDifference = netTotalAmount.subtract(nodalCreditAmount).setScale(2,
//						RoundingMode.HALF_DOWN);
				mpr.setNetTxn(netTotalTxn);
				mpr.setNetMprAmount(netTotalAmount);
//				mpr.setAmountNodalDifference(nodalDifference);
				mpr.setAmountCreditDate(amountCreditDate);
				mpr.setAmountCreditNodal(nodalCreditAmount);

			}
			
			
		
			
			
			return mprTxnList;
		

		} catch (Exception e) {
			logger.error("Exception in transaction summary count service " , e);
		}
		return null;
	}

	

}
