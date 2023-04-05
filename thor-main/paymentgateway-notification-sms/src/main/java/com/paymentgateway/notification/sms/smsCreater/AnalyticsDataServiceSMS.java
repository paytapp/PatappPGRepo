package com.paymentgateway.notification.sms.smsCreater;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.paymentgateway.commons.mongo.MongoInstance;
import com.paymentgateway.commons.user.AnalyticsData;
import com.paymentgateway.commons.user.TransactionSearch;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.PaymentType;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TransactionType;

@Service
public class AnalyticsDataServiceSMS {

	@Autowired
	private MongoInstance mongoInstance;

	private static Logger logger = LoggerFactory.getLogger(AnalyticsDataServiceSMS.class.getName());
	private static final String prefix = "MONGO_DB_";
	public static final BigDecimal ONE_HUNDRED = new BigDecimal(100);

	private static final String CAPTURED = "Captured";
	private static final String ERROR = "Error";
	private static final String TIMEOUT = "Timeout";
	private static final String CANCELLED = "Cancelled";
	private static final String DENIED = "Denied by risk";
	private static final String REJECTED = "Rejected";
	private static final String FAILED = "Failed";
	private static final String INVALID = "Invalid";
	private static final String DENIED_BY_FRAUD = "Denied due to fraud";

	public AnalyticsData getTransactionCount(String fromDate, String toDate, String payId, String paymentType,
			String acquirer, User user, String param) {

		try {
			BasicDBObject dateQuery = new BasicDBObject();
			List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();
			BasicDBObject acquirerQuery = new BasicDBObject();

			BasicDBObject allParamQuery = new BasicDBObject();
			List<BasicDBObject> acquirerConditionLst = new ArrayList<BasicDBObject>();

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
			if (!acquirer.equalsIgnoreCase("ALL")) {

				List<String> acquirerList = Arrays.asList(acquirer.split(","));
				for (String acq : acquirerList) {

					acquirerConditionLst.add(new BasicDBObject(FieldType.ACQUIRER_TYPE.getName(), acq));
				}
				acquirerQuery.append("$or", acquirerConditionLst);
			}

			if (!paymentType.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.PAYMENT_TYPE.getName(), paymentType));
			}

			if (!paramConditionLst.isEmpty()) {
				allParamQuery = new BasicDBObject("$and", paramConditionLst);
			}

			// SALE Captured query
			List<BasicDBObject> txnConditionsList = new ArrayList<BasicDBObject>();
			List<BasicDBObject> saleCapturedConditionList = new ArrayList<BasicDBObject>();
			saleCapturedConditionList
					.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName()));
			saleCapturedConditionList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.CAPTURED.getName()));

			BasicDBObject saleConditionQuery = new BasicDBObject("$and", saleCapturedConditionList);
			txnConditionsList.add(saleConditionQuery);

			// FAIL query
			List<BasicDBObject> failedConditionList = new ArrayList<BasicDBObject>();
			failedConditionList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName()));
			failedConditionList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.FAILED.getName()));

			BasicDBObject failedConditionQuery = new BasicDBObject("$and", failedConditionList);
			txnConditionsList.add(failedConditionQuery);
			// Error query
			List<BasicDBObject> errorConditionList = new ArrayList<BasicDBObject>();
			errorConditionList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName()));
			errorConditionList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.ERROR.getName()));

			BasicDBObject errorConditionQuery = new BasicDBObject("$and", errorConditionList);

			txnConditionsList.add(errorConditionQuery);

			// Cancelled query
			List<BasicDBObject> cancelledConditionList = new ArrayList<BasicDBObject>();
			cancelledConditionList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName()));
			cancelledConditionList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.CANCELLED.getName()));

			BasicDBObject cancelledConditionQuery = new BasicDBObject("$and", cancelledConditionList);
			txnConditionsList.add(cancelledConditionQuery);

			// Invalid query
			List<BasicDBObject> invalidConditionList = new ArrayList<BasicDBObject>();
			invalidConditionList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName()));
			invalidConditionList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.INVALID.getName()));

			BasicDBObject invalidConditionQuery = new BasicDBObject("$and", invalidConditionList);
			txnConditionsList.add(invalidConditionQuery);

			// Fraud query
			List<BasicDBObject> fraudConditionList = new ArrayList<BasicDBObject>();
			fraudConditionList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName()));
			fraudConditionList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.DENIED_BY_FRAUD.getName()));

			BasicDBObject fraudConditionQuery = new BasicDBObject("$and", fraudConditionList);
			txnConditionsList.add(fraudConditionQuery);

			// Dropped query
			List<BasicDBObject> droppedConditionList = new ArrayList<BasicDBObject>();
			droppedConditionList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName()));
			droppedConditionList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.TIMEOUT.getName()));

			BasicDBObject droppedConditionQuery = new BasicDBObject("$and", droppedConditionList);
			txnConditionsList.add(droppedConditionQuery);

			// Rejected query
			List<BasicDBObject> rejectedConditionList = new ArrayList<BasicDBObject>();
			rejectedConditionList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName()));
			rejectedConditionList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.REJECTED.getName()));

			BasicDBObject rejectedConditionQuery = new BasicDBObject("$and", rejectedConditionList);
			txnConditionsList.add(rejectedConditionQuery);

			BasicDBObject saleCapturedConditionQuery = new BasicDBObject("$or", txnConditionsList);

			List<BasicDBObject> allConditionQueryList = new ArrayList<BasicDBObject>();

			if (!acquirerQuery.isEmpty()) {
				allConditionQueryList.add(acquirerQuery);
			}
			if (!dateQuery.isEmpty()) {
				allConditionQueryList.add(dateQuery);
			}
			allConditionQueryList.add(saleCapturedConditionQuery);

			BasicDBObject allConditionQueryObj = new BasicDBObject("$and", allConditionQueryList);

			List<BasicDBObject> fianlList = new ArrayList<BasicDBObject>();

			if (!allParamQuery.isEmpty()) {
				fianlList.add(allParamQuery);
			}
			if (!allConditionQueryObj.isEmpty()) {
				fianlList.add(allConditionQueryObj);
			}

			BasicDBObject finalQuery = new BasicDBObject("$and", fianlList);
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns
					.getCollection(PropertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));

			double ccSuccess = 0;
			double ccFailed = 0;
			double ccCancelled = 0;
			double ccInvalid = 0;
			double ccFraud = 0;
			double ccDropped = 0;
			double ccRejected = 0;

			double dcSuccess = 0;
			double dcFailed = 0;
			double dcCancelled = 0;
			double dcInvalid = 0;
			double dcFraud = 0;
			double dcDropped = 0;
			double dcRejected = 0;

			double upSuccess = 0;
			double upFailed = 0;
			double upCancelled = 0;
			double upInvalid = 0;
			double upFraud = 0;
			double upDropped = 0;
			double upRejected = 0;

			double nbSuccess = 0;
			double nbFailed = 0;
			double nbCancelled = 0;
			double nbInvalid = 0;
			double nbFraud = 0;
			double nbDropped = 0;
			double nbRejected = 0;

			double wlSuccess = 0;
			double wlFailed = 0;
			double wlCancelled = 0;
			double wlInvalid = 0;
			double wlFraud = 0;
			double wlDropped = 0;
			double wlRejected = 0;

			double emSuccess = 0;
			double emFailed = 0;
			double emCancelled = 0;
			double emInvalid = 0;
			double emFraud = 0;
			double emDropped = 0;
			double emRejected = 0;

			double cdSuccess = 0;
			double cdFailed = 0;
			double cdCancelled = 0;
			double cdInvalid = 0;
			double cdFraud = 0;
			double cdDropped = 0;
			double cdRejected = 0;

			double totalTxn = 0;
			double totalCCTxn = 0;
			double totalDCTxn = 0;
			double totalUPTxn = 0;
			double totalNBTxn = 0;
			double totalWLTxn = 0;
			double totalEMTxn = 0;
			double totalCDTxn = 0;

			int unknownTransactions = 0;

			BigDecimal totalTxnAmount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_DOWN);

			BigDecimal totalCCTxnAmount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_DOWN);
			BigDecimal totalDCTxnAmount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_DOWN);
			BigDecimal totalUPTxnAmount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_DOWN);
			BigDecimal totalNBTxnAmount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_DOWN);
			BigDecimal totalWLTxnAmount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_DOWN);
			BigDecimal totalEMTxnAmount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_DOWN);
			BigDecimal totalCDTxnAmount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_DOWN);

			MongoCursor<Document> cursor = coll.find(finalQuery).iterator();

			// Remove all data from an earlier map
			while (cursor.hasNext()) {

				totalTxn++;
				Document dbobj = cursor.next();

				if (StringUtils.isBlank(dbobj.getString(FieldType.PAYMENT_TYPE.toString()))) {
					unknownTransactions++;
					continue;
				}
				switch (PaymentType.getInstanceUsingCode(dbobj.getString(FieldType.PAYMENT_TYPE.toString()))) {

				case DEBIT_CARD:

					totalDCTxn++;
					switch (dbobj.getString(FieldType.STATUS.toString())) {

					case CAPTURED:

						if (!StringUtils.isBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))) {
							BigDecimal txnAmount = new BigDecimal(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
									.setScale(2, RoundingMode.HALF_DOWN);
							totalTxnAmount = totalTxnAmount.add(txnAmount).setScale(2, RoundingMode.HALF_DOWN);
							totalDCTxnAmount = totalDCTxnAmount.add(txnAmount).setScale(2, RoundingMode.HALF_DOWN);

						}

						dcSuccess++;
						break;

					case ERROR:
						dcFailed++;
						break;

					case TIMEOUT:
						dcDropped++;
						break;

					case CANCELLED:
						dcCancelled++;
						break;

					case DENIED:
						dcRejected++;
						break;

					case REJECTED:
						dcRejected++;
						break;

					case FAILED:
						dcFailed++;
						break;

					case INVALID:
						dcInvalid++;
						break;

					case DENIED_BY_FRAUD:
						dcFraud++;
						break;

					default:
						break;
					}

					break;

				case CREDIT_CARD:

					totalCCTxn++;
					switch (dbobj.getString(FieldType.STATUS.toString())) {

					case CAPTURED:

						if (!StringUtils.isBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))) {
							BigDecimal txnAmount = new BigDecimal(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
									.setScale(2, RoundingMode.HALF_DOWN);
							;
							totalTxnAmount = totalTxnAmount.add(txnAmount).setScale(2, RoundingMode.HALF_DOWN);
							totalCCTxnAmount = totalCCTxnAmount.add(txnAmount).setScale(2, RoundingMode.HALF_DOWN);
						}

						ccSuccess++;

						break;

					case ERROR:
						ccFailed++;
						break;

					case TIMEOUT:
						ccDropped++;
						break;

					case CANCELLED:
						ccCancelled++;
						break;

					case DENIED:
						ccRejected++;
						break;

					case REJECTED:
						ccRejected++;
						break;

					case FAILED:
						ccFailed++;
						break;

					case INVALID:
						ccInvalid++;
						break;

					case DENIED_BY_FRAUD:
						ccFraud++;
						break;

					default:
						break;
					}

					break;

				case UPI:

					totalUPTxn++;
					switch (dbobj.getString(FieldType.STATUS.toString())) {

					case CAPTURED:

						if (!StringUtils.isBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))) {
							BigDecimal txnAmount = new BigDecimal(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
									.setScale(2, RoundingMode.HALF_DOWN);
							totalTxnAmount = totalTxnAmount.add(txnAmount).setScale(2, RoundingMode.HALF_DOWN);
							totalUPTxnAmount = totalUPTxnAmount.add(txnAmount).setScale(2, RoundingMode.HALF_DOWN);
						}

						upSuccess++;
						break;

					case ERROR:
						upFailed++;
						break;

					case TIMEOUT:
						upDropped++;
						break;

					case CANCELLED:
						upCancelled++;
						break;

					case DENIED:
						upRejected++;
						break;

					case REJECTED:
						upRejected++;
						break;

					case FAILED:
						upFailed++;
						break;

					case INVALID:
						upInvalid++;
						break;

					case DENIED_BY_FRAUD:
						upFraud++;
						break;

					default:
						break;
					}

					break;

				case NET_BANKING:

					totalNBTxn++;
					switch (dbobj.getString(FieldType.STATUS.toString())) {

					case CAPTURED:

						if (!StringUtils.isBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))) {
							BigDecimal txnAmount = new BigDecimal(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
									.setScale(2, RoundingMode.HALF_DOWN);
							totalTxnAmount = totalTxnAmount.add(txnAmount).setScale(2, RoundingMode.HALF_DOWN);
							totalNBTxnAmount = totalNBTxnAmount.add(txnAmount).setScale(2, RoundingMode.HALF_DOWN);
						}

						nbSuccess++;
						break;

					case ERROR:
						nbFailed++;
						break;

					case TIMEOUT:
						nbDropped++;
						break;

					case CANCELLED:
						nbCancelled++;
						break;

					case DENIED:
						nbRejected++;
						break;

					case REJECTED:
						nbRejected++;
						break;

					case FAILED:
						nbFailed++;
						break;

					case INVALID:
						nbInvalid++;
						break;

					case DENIED_BY_FRAUD:
						nbFraud++;
						break;

					default:
						break;
					}

					break;
				case WALLET:

					totalWLTxn++;
					switch (dbobj.getString(FieldType.STATUS.toString())) {

					case CAPTURED:

						if (!StringUtils.isBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))) {
							BigDecimal txnAmount = new BigDecimal(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
									.setScale(2, RoundingMode.HALF_DOWN);
							totalTxnAmount = totalTxnAmount.add(txnAmount).setScale(2, RoundingMode.HALF_DOWN);
							totalWLTxnAmount = totalWLTxnAmount.add(txnAmount).setScale(2, RoundingMode.HALF_DOWN);
						}

						wlSuccess++;
						break;

					case ERROR:
						wlFailed++;
						break;

					case TIMEOUT:
						wlDropped++;
						break;

					case CANCELLED:
						wlCancelled++;
						break;

					case DENIED:
						wlRejected++;
						break;

					case REJECTED:
						wlRejected++;
						break;

					case FAILED:
						wlFailed++;
						break;

					case INVALID:
						wlInvalid++;
						break;

					case DENIED_BY_FRAUD:
						wlFraud++;
						break;

					default:
						break;
					}

					break;
				case EMI:

					totalEMTxn++;
					switch (dbobj.getString(FieldType.STATUS.toString())) {

					case CAPTURED:

						if (!StringUtils.isBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))) {
							BigDecimal txnAmount = new BigDecimal(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
									.setScale(2, RoundingMode.HALF_DOWN);
							totalTxnAmount = totalTxnAmount.add(txnAmount).setScale(2, RoundingMode.HALF_DOWN);
							totalEMTxnAmount = totalEMTxnAmount.add(txnAmount).setScale(2, RoundingMode.HALF_DOWN);
						}

						emSuccess++;
						break;

					case ERROR:
						emFailed++;
						break;

					case TIMEOUT:
						emDropped++;
						break;

					case CANCELLED:
						emCancelled++;
						break;

					case DENIED:
						emRejected++;
						break;

					case REJECTED:
						emRejected++;
						break;

					case FAILED:
						emFailed++;
						break;

					case INVALID:
						emInvalid++;
						break;

					case DENIED_BY_FRAUD:
						emFraud++;
						break;

					default:
						break;
					}

					break;
				case COD:

					totalCDTxn++;
					switch (dbobj.getString(FieldType.STATUS.toString())) {

					case CAPTURED:

						if (!StringUtils.isBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))) {
							BigDecimal txnAmount = new BigDecimal(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
									.setScale(2, RoundingMode.HALF_DOWN);
							totalTxnAmount = totalTxnAmount.add(txnAmount).setScale(2, RoundingMode.HALF_DOWN);
							totalCDTxnAmount = totalCDTxnAmount.add(txnAmount).setScale(2, RoundingMode.HALF_DOWN);
						}

						cdSuccess++;
						break;

					case ERROR:
						cdFailed++;
						break;

					case TIMEOUT:
						cdDropped++;
						break;

					case CANCELLED:
						cdCancelled++;
						break;

					case DENIED:
						cdRejected++;
						break;

					case REJECTED:
						cdRejected++;
						break;

					case FAILED:
						cdFailed++;
						break;

					case INVALID:
						cdInvalid++;
						break;

					case DENIED_BY_FRAUD:
						cdFraud++;
						break;

					default:
						break;
					}

					break;
				default:
					break;

				}
			}

			cursor.close();

			// totalTxnAmount = totalTxnAmount.setScale(2, RoundingMode.HALF_DOWN);
			AnalyticsData analyticsData = new AnalyticsData();

			double totalTxnSuccess = ccSuccess + dcSuccess + upSuccess + nbSuccess + wlSuccess + emSuccess + cdSuccess;
			double totalFailed = ccFailed + dcFailed + upFailed + nbSuccess + wlSuccess + emSuccess + cdSuccess;
			double totalRejected = ccRejected + dcRejected + upRejected + nbSuccess + wlSuccess + emSuccess + cdSuccess;

			analyticsData.setTotalTxnCount(String.format("%.0f", totalTxn));
			analyticsData.setSuccessTxnCount(String.format("%.0f", totalTxnSuccess));
			analyticsData.setFailedTxnCount(String.format("%.2f", totalFailed));

			if (totalTxn == 0.00) {
				analyticsData.setSuccessTxnPercent("0.00");
			} else {
				double successTxnPercent = (totalTxnSuccess / (totalTxn - totalRejected)) * 100;
				analyticsData.setSuccessTxnPercent(String.format("%.2f", successTxnPercent));
			}

			if (totalTxn == 0.00) {
				analyticsData.setTotalRejectedTxnPercent("0.00");
			} else {
				double totalRejectedTxnPercent = (totalRejected / totalTxn) * 100;
				analyticsData.setTotalRejectedTxnPercent(String.format("%.2f", totalRejectedTxnPercent));
			}

			BigDecimal totalTxnCountBD = new BigDecimal(totalTxnSuccess);
			totalTxnCountBD = totalTxnCountBD.setScale(2, RoundingMode.HALF_DOWN);

			if (totalTxnCountBD.compareTo(BigDecimal.ONE) < 0) {
				analyticsData.setAvgTkt("0.00");
			} else {

				String totalTxnAmountString = String.valueOf(totalTxnAmount);

				double totalTxnAmountDouble = Double.valueOf(totalTxnAmountString);
				double totalTxnSuccessDouble = Double.valueOf(totalTxnSuccess);
				double avgTicketSizeDouble = totalTxnAmountDouble / totalTxnSuccessDouble;

				analyticsData.setAvgTkt(String.format("%.2f", avgTicketSizeDouble));
			}

			if (totalTxnSuccess < 1) {
				analyticsData.setCCTxnPercent("0.00");
				analyticsData.setDCTxnPercent("0.00");
				analyticsData.setUPTxnPercent("0.00");
				analyticsData.setNBTxnPercent("0.00");
				analyticsData.setWLTxnPercent("0.00");
				analyticsData.setEMTxnPercent("0.00");
				analyticsData.setCDTxnPercent("0.00");
			}

			else {

				double cCTxnPercent = (ccSuccess / totalTxnSuccess) * 100;
				double dCTxnPercent = (dcSuccess / totalTxnSuccess) * 100;
				double upTxnPercent = (upSuccess / totalTxnSuccess) * 100;
				double nbTxnPercent = (nbSuccess / totalTxnSuccess) * 100;
				double wlTxnPercent = (wlSuccess / totalTxnSuccess) * 100;
				double cdTxnPercent = (cdSuccess / totalTxnSuccess) * 100;
				double emTxnPercent = (emSuccess / totalTxnSuccess) * 100;

				analyticsData.setCCTxnPercent(String.format("%.2f", cCTxnPercent));
				analyticsData.setDCTxnPercent(String.format("%.2f", dCTxnPercent));
				analyticsData.setUPTxnPercent(String.format("%.2f", upTxnPercent));
				analyticsData.setNBTxnPercent(String.format("%.2f", nbTxnPercent));
				analyticsData.setWLTxnPercent(String.format("%.2f", wlTxnPercent));
				analyticsData.setEMTxnPercent(String.format("%.2f", emTxnPercent));
				analyticsData.setCDTxnPercent(String.format("%.2f", cdTxnPercent));
			}

			if (ccSuccess + ccFailed < 1) {
				analyticsData.setCCSuccessRate("0.00");
			} else {
				double cCSuccessRate = (ccSuccess / (ccSuccess + ccFailed)) * 100;
				analyticsData.setCCSuccessRate(String.format("%.2f", cCSuccessRate));
			}

			if (dcSuccess + dcFailed < 1) {
				analyticsData.setDCSuccessRate("0.00");
			} else {
				double dCSuccessRate = (dcSuccess / (dcSuccess + dcFailed)) * 100;
				analyticsData.setDCSuccessRate(String.format("%.2f", dCSuccessRate));
			}

			if (upSuccess + upFailed < 1) {
				analyticsData.setUPSuccessRate("0.00");
			} else {
				double uPSuccessRate = (upSuccess / (upSuccess + upFailed)) * 100;
				analyticsData.setUPSuccessRate(String.format("%.2f", uPSuccessRate));
			}

			if (emSuccess + emFailed < 1) {
				analyticsData.setEMSuccessRate("0.00");
			} else {
				double eMSuccessRate = (emSuccess / (emSuccess + emFailed)) * 100;
				analyticsData.setEMSuccessRate(String.format("%.2f", eMSuccessRate));
			}

			if (wlSuccess + wlFailed < 1) {
				analyticsData.setWLSuccessRate("0.00");
			} else {
				double wLSuccessRate = (wlSuccess / (wlSuccess + wlFailed)) * 100;
				analyticsData.setWLSuccessRate(String.format("%.2f", wLSuccessRate));
			}

			if (nbSuccess + nbFailed < 1) {
				analyticsData.setNBSuccessRate("0.00");
			} else {
				double nBSuccessRate = (nbSuccess / (nbSuccess + nbFailed)) * 100;
				analyticsData.setNBSuccessRate(String.format("%.2f", nBSuccessRate));
			}

			if (cdSuccess + cdFailed < 1) {
				analyticsData.setCDSuccessRate("0.00");
			} else {
				double cDSuccessRate = (cdSuccess / (cdSuccess + cdFailed)) * 100;
				analyticsData.setCDSuccessRate(String.format("%.2f", cDSuccessRate));
			}

			if (totalCDTxn < 1) {
				analyticsData.setTotalCDTxn("0");
				analyticsData.setTotalCDSuccessTxnPercent("0.00");
				analyticsData.setTotalCDFailedTxnPercent("0.00");
				analyticsData.setTotalCDCancelledTxnPercent("0.00");
				analyticsData.setTotalCDInvalidTxnPercent("0.00");
				analyticsData.setTotalCDFraudTxnPercent("0.00");
				analyticsData.setTotalCDDroppedTxnPercent("0.00");
				analyticsData.setTotalCDRejectedTxnPercent("0.00");

			} else {

				double totalCDSuccessTxnPercent = (cdSuccess / (totalCDTxn)) * 100;
				double totalCDFailedTxnPercent = (cdFailed / (totalCDTxn)) * 100;
				double totalCDCancelledTxnPercent = (cdCancelled / (totalCDTxn)) * 100;
				double totalCDInvalidTxnPercent = (cdInvalid / (totalCDTxn)) * 100;
				double totalCDFraudTxnPercent = (cdFraud / (totalCDTxn)) * 100;
				double totalCDDroppedTxnPercent = (cdDropped / (totalCDTxn)) * 100;
				double totalCDRejectedTxnPercent = (cdRejected / (totalCDTxn)) * 100;

				analyticsData.setTotalCDTxn(String.format("%.0f", totalCDTxn));
				analyticsData.setTotalCDSuccessTxnPercent(String.format("%.2f", totalCDSuccessTxnPercent));
				analyticsData.setTotalCDFailedTxnPercent(String.format("%.2f", totalCDFailedTxnPercent));
				analyticsData.setTotalCDCancelledTxnPercent(String.format("%.2f", totalCDCancelledTxnPercent));
				analyticsData.setTotalCDInvalidTxnPercent(String.format("%.2f", totalCDInvalidTxnPercent));
				analyticsData.setTotalCDFraudTxnPercent(String.format("%.2f", totalCDFraudTxnPercent));
				analyticsData.setTotalCDDroppedTxnPercent(String.format("%.2f", totalCDDroppedTxnPercent));
				analyticsData.setTotalCDRejectedTxnPercent(String.format("%.2f", totalCDRejectedTxnPercent));

			}

			if (totalEMTxn < 1) {
				analyticsData.setTotalEMTxn("0");
				analyticsData.setTotalEMSuccessTxnPercent("0.00");
				analyticsData.setTotalEMFailedTxnPercent("0.00");
				analyticsData.setTotalEMCancelledTxnPercent("0.00");
				analyticsData.setTotalEMInvalidTxnPercent("0.00");
				analyticsData.setTotalEMFraudTxnPercent("0.00");
				analyticsData.setTotalEMDroppedTxnPercent("0.00");
				analyticsData.setTotalEMRejectedTxnPercent("0.00");
			} else {
				double totalEMSuccessTxnPercent = (emSuccess / (totalEMTxn)) * 100;
				double totalEMFailedTxnPercent = (emFailed / (totalEMTxn)) * 100;
				double totalEMCancelledTxnPercent = (emCancelled / (totalEMTxn)) * 100;
				double totalEMInvalidTxnPercent = (emInvalid / (totalEMTxn)) * 100;
				double totalEMFraudTxnPercent = (emFraud / (totalEMTxn)) * 100;
				double totalEMDroppedTxnPercent = (emDropped / (totalEMTxn)) * 100;
				double totalEMRejectedTxnPercent = (emRejected / (totalEMTxn)) * 100;

				analyticsData.setTotalEMTxn(String.format("%.0f", totalEMTxn));
				analyticsData.setTotalEMSuccessTxnPercent(String.format("%.2f", totalEMSuccessTxnPercent));
				analyticsData.setTotalEMFailedTxnPercent(String.format("%.2f", totalEMFailedTxnPercent));
				analyticsData.setTotalEMCancelledTxnPercent(String.format("%.2f", totalEMCancelledTxnPercent));
				analyticsData.setTotalEMInvalidTxnPercent(String.format("%.2f", totalEMInvalidTxnPercent));
				analyticsData.setTotalEMFraudTxnPercent(String.format("%.2f", totalEMFraudTxnPercent));
				analyticsData.setTotalEMDroppedTxnPercent(String.format("%.2f", totalEMDroppedTxnPercent));
				analyticsData.setTotalEMRejectedTxnPercent(String.format("%.2f", totalEMRejectedTxnPercent));

			}

			if (totalWLTxn < 1) {
				analyticsData.setTotalWLTxn("0");
				analyticsData.setTotalWLSuccessTxnPercent("0.00");
				analyticsData.setTotalWLFailedTxnPercent("0.00");
				analyticsData.setTotalWLCancelledTxnPercent("0.00");
				analyticsData.setTotalWLInvalidTxnPercent("0.00");
				analyticsData.setTotalWLFraudTxnPercent("0.00");
				analyticsData.setTotalWLDroppedTxnPercent("0.00");
				analyticsData.setTotalWLRejectedTxnPercent("0.00");

			} else {
				double totalWLSuccessTxnPercent = (wlSuccess / (totalWLTxn)) * 100;
				double totalWLFailedTxnPercent = (wlFailed / (totalWLTxn)) * 100;
				double totalWLCancelledTxnPercent = (wlCancelled / (totalWLTxn)) * 100;
				double totalWLInvalidTxnPercent = (wlInvalid / (totalWLTxn)) * 100;
				double totalWLFraudTxnPercent = (wlFraud / (totalWLTxn)) * 100;
				double totalWLDroppedTxnPercent = (wlDropped / (totalWLTxn)) * 100;
				double totalWLRejectedTxnPercent = (wlRejected / (totalWLTxn)) * 100;

				analyticsData.setTotalWLTxn(String.format("%.0f", totalWLTxn));
				analyticsData.setTotalWLSuccessTxnPercent(String.format("%.2f", totalWLSuccessTxnPercent));
				analyticsData.setTotalWLFailedTxnPercent(String.format("%.2f", totalWLFailedTxnPercent));
				analyticsData.setTotalWLCancelledTxnPercent(String.format("%.2f", totalWLCancelledTxnPercent));
				analyticsData.setTotalWLInvalidTxnPercent(String.format("%.2f", totalWLInvalidTxnPercent));
				analyticsData.setTotalWLFraudTxnPercent(String.format("%.2f", totalWLFraudTxnPercent));
				analyticsData.setTotalWLDroppedTxnPercent(String.format("%.2f", totalWLDroppedTxnPercent));
				analyticsData.setTotalWLRejectedTxnPercent(String.format("%.2f", totalWLRejectedTxnPercent));
			}

			if (totalNBTxn < 1) {
				analyticsData.setTotalNBTxn("0");
				analyticsData.setTotalNBSuccessTxnPercent("0.00");
				analyticsData.setTotalNBFailedTxnPercent("0.00");
				analyticsData.setTotalNBCancelledTxnPercent("0.00");
				analyticsData.setTotalNBInvalidTxnPercent("0.00");
				analyticsData.setTotalNBFraudTxnPercent("0.00");
				analyticsData.setTotalNBDroppedTxnPercent("0.00");
				analyticsData.setTotalNBRejectedTxnPercent("0.00");
			} else {
				double totalNBSuccessTxnPercent = (nbSuccess / (totalNBTxn)) * 100;
				double totalNBFailedTxnPercent = (nbFailed / (totalNBTxn)) * 100;
				double totalNBCancelledTxnPercent = (nbCancelled / (totalNBTxn)) * 100;
				double totalNBInvalidTxnPercent = (nbInvalid / (totalNBTxn)) * 100;
				double totalNBFraudTxnPercent = (nbFraud / (totalNBTxn)) * 100;
				double totalNBDroppedTxnPercent = (nbDropped / (totalNBTxn)) * 100;
				double totalNBRejectedTxnPercent = (nbRejected / (totalNBTxn)) * 100;

				analyticsData.setTotalNBTxn(String.format("%.0f", totalNBTxn));
				analyticsData.setTotalNBSuccessTxnPercent(String.format("%.2f", totalNBSuccessTxnPercent));
				analyticsData.setTotalNBFailedTxnPercent(String.format("%.2f", totalNBFailedTxnPercent));
				analyticsData.setTotalNBCancelledTxnPercent(String.format("%.2f", totalNBCancelledTxnPercent));
				analyticsData.setTotalNBInvalidTxnPercent(String.format("%.2f", totalNBInvalidTxnPercent));
				analyticsData.setTotalNBFraudTxnPercent(String.format("%.2f", totalNBFraudTxnPercent));
				analyticsData.setTotalNBDroppedTxnPercent(String.format("%.2f", totalNBDroppedTxnPercent));
				analyticsData.setTotalNBRejectedTxnPercent(String.format("%.2f", totalNBRejectedTxnPercent));

			}

			if (totalCCTxn < 1) {
				analyticsData.setTotalCCTxn("0");
				analyticsData.setTotalCCSuccessTxnPercent("0.00");
				analyticsData.setTotalCCFailedTxnPercent("0.00");
				analyticsData.setTotalCCCancelledTxnPercent("0.00");
				analyticsData.setTotalCCInvalidTxnPercent("0.00");
				analyticsData.setTotalCCFraudTxnPercent("0.00");
				analyticsData.setTotalCCDroppedTxnPercent("0.00");
				analyticsData.setTotalCCRejectedTxnPercent("0.00");

			} else {

				double totalCCSuccessTxnPercent = (ccSuccess / (totalCCTxn)) * 100;
				double totalCCFailedTxnPercent = (ccFailed / (totalCCTxn)) * 100;
				double totalCCCancelledTxnPercent = (ccCancelled / (totalCCTxn)) * 100;
				double totalCCInvalidTxnPercent = (ccInvalid / (totalCCTxn)) * 100;
				double totalCCFraudTxnPercent = (ccFraud / (totalCCTxn)) * 100;
				double totalCCDroppedTxnPercent = (ccDropped / (totalCCTxn)) * 100;
				double totalCCRejectedTxnPercent = (ccRejected / (totalCCTxn)) * 100;

				analyticsData.setTotalCCTxn(String.format("%.0f", totalCCTxn));
				analyticsData.setTotalCCSuccessTxnPercent(String.format("%.2f", totalCCSuccessTxnPercent));
				analyticsData.setTotalCCFailedTxnPercent(String.format("%.2f", totalCCFailedTxnPercent));
				analyticsData.setTotalCCCancelledTxnPercent(String.format("%.2f", totalCCCancelledTxnPercent));
				analyticsData.setTotalCCInvalidTxnPercent(String.format("%.2f", totalCCInvalidTxnPercent));
				analyticsData.setTotalCCFraudTxnPercent(String.format("%.2f", totalCCFraudTxnPercent));
				analyticsData.setTotalCCDroppedTxnPercent(String.format("%.2f", totalCCDroppedTxnPercent));
				analyticsData.setTotalCCRejectedTxnPercent(String.format("%.2f", totalCCRejectedTxnPercent));

			}

			if (totalDCTxn < 1) {

				analyticsData.setTotalDCTxn("0");
				analyticsData.setTotalDCSuccessTxnPercent("0.00");
				analyticsData.setTotalDCFailedTxnPercent("0.00");
				analyticsData.setTotalDCCancelledTxnPercent("0.00");
				analyticsData.setTotalDCInvalidTxnPercent("0.00");
				analyticsData.setTotalDCFraudTxnPercent("0.00");
				analyticsData.setTotalDCDroppedTxnPercent("0.00");
				analyticsData.setTotalDCRejectedTxnPercent("0.00");

			} else {

				double totalDCSuccessTxnPercent = (dcSuccess / (totalDCTxn)) * 100;
				double totalDCFailedTxnPercent = (dcFailed / (totalDCTxn)) * 100;
				double totalDCCancelledTxnPercent = (dcCancelled / (totalDCTxn)) * 100;
				double totalDCInvalidTxnPercent = (dcInvalid / (totalDCTxn)) * 100;
				double totalDCFraudTxnPercent = (dcFraud / (totalDCTxn)) * 100;
				double totalDCDroppedTxnPercent = (dcDropped / (totalDCTxn)) * 100;
				double totalDCRejectedTxnPercent = (dcRejected / (totalDCTxn)) * 100;

				analyticsData.setTotalDCTxn(String.format("%.0f", totalDCTxn));
				analyticsData.setTotalDCSuccessTxnPercent(String.format("%.2f", totalDCSuccessTxnPercent));
				analyticsData.setTotalDCFailedTxnPercent(String.format("%.2f", totalDCFailedTxnPercent));
				analyticsData.setTotalDCCancelledTxnPercent(String.format("%.2f", totalDCCancelledTxnPercent));
				analyticsData.setTotalDCInvalidTxnPercent(String.format("%.2f", totalDCInvalidTxnPercent));
				analyticsData.setTotalDCFraudTxnPercent(String.format("%.2f", totalDCFraudTxnPercent));
				analyticsData.setTotalDCDroppedTxnPercent(String.format("%.2f", totalDCDroppedTxnPercent));
				analyticsData.setTotalDCRejectedTxnPercent(String.format("%.2f", totalDCRejectedTxnPercent));

			}

			if (totalUPTxn < 1) {

				analyticsData.setTotalUPTxn("0");
				analyticsData.setTotalUPSuccessTxnPercent("0.00");
				analyticsData.setTotalUPFailedTxnPercent("0.00");
				analyticsData.setTotalUPCancelledTxnPercent("0.00");
				analyticsData.setTotalUPInvalidTxnPercent("0.00");
				analyticsData.setTotalUPFraudTxnPercent("0.00");
				analyticsData.setTotalUPDroppedTxnPercent("0.00");
				analyticsData.setTotalUPRejectedTxnPercent("0.00");
			} else {

				analyticsData.setTotalUPTxn(String.format("%.0f", totalUPTxn));

				double totalUPSuccessTxnPercent = (upSuccess / (totalUPTxn)) * 100;
				double totalUPFailedTxnPercent = (upFailed / (totalUPTxn)) * 100;
				double totalUPCancelledTxnPercent = (upCancelled / (totalUPTxn)) * 100;
				double totalUPInvalidTxnPercent = (upInvalid / (totalUPTxn)) * 100;
				double totalUPFraudTxnPercent = (upFraud / (totalUPTxn)) * 100;
				double totalUPDroppedTxnPercent = (upDropped / (totalUPTxn)) * 100;
				double totalUPRejectedTxnPercent = (upRejected / (totalUPTxn)) * 100;

				analyticsData.setTotalUPSuccessTxnPercent(String.format("%.2f", totalUPSuccessTxnPercent));
				analyticsData.setTotalUPFailedTxnPercent(String.format("%.2f", totalUPFailedTxnPercent));
				analyticsData.setTotalUPCancelledTxnPercent(String.format("%.2f", totalUPCancelledTxnPercent));
				analyticsData.setTotalUPInvalidTxnPercent(String.format("%.2f", totalUPInvalidTxnPercent));
				analyticsData.setTotalUPFraudTxnPercent(String.format("%.2f", totalUPFraudTxnPercent));
				analyticsData.setTotalUPDroppedTxnPercent(String.format("%.2f", totalUPDroppedTxnPercent));
				analyticsData.setTotalUPRejectedTxnPercent(String.format("%.2f", totalUPRejectedTxnPercent));

			}

			if (paymentType.equals(PaymentType.CREDIT_CARD.getCode())) {

				analyticsData.setCaptured(String.format("%.0f", ccSuccess));
				analyticsData.setFailed(String.format("%.0f", ccFailed));
				analyticsData.setCancelled(String.format("%.0f", ccCancelled));
				analyticsData.setInvalid(String.format("%.0f", ccInvalid));
				analyticsData.setFraud(String.format("%.0f", ccFraud));
				analyticsData.setDropped(String.format("%.0f", ccDropped));
				analyticsData.setRejected(String.format("%.0f", ccRejected));

				analyticsData.setCapturedPercent(String.format("%.2f", (ccSuccess / totalCCTxn) * 100));
				analyticsData.setFailedPercent(String.format("%.2f", (ccFailed / totalCCTxn) * 100));
				analyticsData.setCancelledPercent(String.format("%.2f", (ccCancelled / totalCCTxn) * 100));
				analyticsData.setInvalidPercent(String.format("%.2f", (ccInvalid / totalCCTxn) * 100));
				analyticsData.setFraudPercent(String.format("%.2f", (ccFraud / totalCCTxn) * 100));
				analyticsData.setDroppedPercent(String.format("%.2f", (ccDropped / totalCCTxn) * 100));
				analyticsData.setRejectedPercent(String.format("%.2f", (ccRejected / totalCCTxn) * 100));

			} else if (paymentType.equals(PaymentType.DEBIT_CARD.getCode())) {

				analyticsData.setCaptured(String.format("%.0f", dcSuccess));
				analyticsData.setFailed(String.format("%.0f", dcFailed));
				analyticsData.setCancelled(String.format("%.0f", dcCancelled));
				analyticsData.setInvalid(String.format("%.0f", dcInvalid));
				analyticsData.setFraud(String.format("%.0f", dcFraud));
				analyticsData.setDropped(String.format("%.0f", dcDropped));
				analyticsData.setRejected(String.format("%.0f", dcRejected));

				analyticsData.setCapturedPercent(String.format("%.2f", (dcSuccess / totalDCTxn) * 100));
				analyticsData.setFailedPercent(String.format("%.2f", (dcFailed / totalDCTxn) * 100));
				analyticsData.setCancelledPercent(String.format("%.2f", (dcCancelled / totalDCTxn) * 100));
				analyticsData.setInvalidPercent(String.format("%.2f", (dcInvalid / totalDCTxn) * 100));
				analyticsData.setFraudPercent(String.format("%.2f", (dcFraud / totalDCTxn) * 100));
				analyticsData.setDroppedPercent(String.format("%.2f", (dcDropped / totalDCTxn) * 100));
				analyticsData.setRejectedPercent(String.format("%.2f", (dcRejected / totalDCTxn) * 100));

			} else if (paymentType.equals(PaymentType.UPI.getCode())) {

				analyticsData.setCaptured(String.format("%.0f", upSuccess));
				analyticsData.setFailed(String.format("%.0f", upFailed));
				analyticsData.setCancelled(String.format("%.0f", upCancelled));
				analyticsData.setInvalid(String.format("%.0f", upInvalid));
				analyticsData.setFraud(String.format("%.0f", upFraud));
				analyticsData.setDropped(String.format("%.0f", upDropped));
				analyticsData.setRejected(String.format("%.0f", upRejected));

				analyticsData.setCapturedPercent(String.format("%.2f", (upSuccess / totalUPTxn) * 100));
				analyticsData.setFailedPercent(String.format("%.2f", (upFailed / totalUPTxn) * 100));
				analyticsData.setCancelledPercent(String.format("%.2f", (upCancelled / totalUPTxn) * 100));
				analyticsData.setInvalidPercent(String.format("%.2f", (upInvalid / totalUPTxn) * 100));
				analyticsData.setFraudPercent(String.format("%.2f", (upFraud / totalUPTxn) * 100));
				analyticsData.setDroppedPercent(String.format("%.2f", (upDropped / totalUPTxn) * 100));
				analyticsData.setRejectedPercent(String.format("%.2f", (upRejected / totalUPTxn) * 100));

			} else if (paymentType.equals(PaymentType.WALLET.getCode())) {

				analyticsData.setCaptured(String.format("%.0f", wlSuccess));
				analyticsData.setFailed(String.format("%.0f", wlFailed));
				analyticsData.setCancelled(String.format("%.0f", wlCancelled));
				analyticsData.setInvalid(String.format("%.0f", wlInvalid));
				analyticsData.setFraud(String.format("%.0f", wlFraud));
				analyticsData.setDropped(String.format("%.0f", wlDropped));
				analyticsData.setRejected(String.format("%.0f", wlRejected));

				analyticsData.setCapturedPercent(String.format("%.2f", (wlSuccess / totalWLTxn) * 100));
				analyticsData.setFailedPercent(String.format("%.2f", (wlFailed / totalWLTxn) * 100));
				analyticsData.setCancelledPercent(String.format("%.2f", (wlCancelled / totalWLTxn) * 100));
				analyticsData.setInvalidPercent(String.format("%.2f", (wlInvalid / totalWLTxn) * 100));
				analyticsData.setFraudPercent(String.format("%.2f", (wlFraud / totalWLTxn) * 100));
				analyticsData.setDroppedPercent(String.format("%.2f", (wlDropped / totalWLTxn) * 100));
				analyticsData.setRejectedPercent(String.format("%.2f", (wlRejected / totalWLTxn) * 100));

			} else if (paymentType.equals(PaymentType.EMI.getCode())) {
				analyticsData.setCaptured(String.format("%.0f", emSuccess));
				analyticsData.setFailed(String.format("%.0f", emFailed));
				analyticsData.setCancelled(String.format("%.0f", emCancelled));
				analyticsData.setInvalid(String.format("%.0f", emInvalid));
				analyticsData.setFraud(String.format("%.0f", emFraud));
				analyticsData.setDropped(String.format("%.0f", emDropped));
				analyticsData.setRejected(String.format("%.0f", emRejected));

				analyticsData.setCapturedPercent(String.format("%.2f", (emSuccess / totalEMTxn) * 100));
				analyticsData.setFailedPercent(String.format("%.2f", (emFailed / totalEMTxn) * 100));
				analyticsData.setCancelledPercent(String.format("%.2f", (emCancelled / totalEMTxn) * 100));
				analyticsData.setInvalidPercent(String.format("%.2f", (emInvalid / totalEMTxn) * 100));
				analyticsData.setFraudPercent(String.format("%.2f", (emFraud / totalEMTxn) * 100));
				analyticsData.setDroppedPercent(String.format("%.2f", (emDropped / totalEMTxn) * 100));
				analyticsData.setRejectedPercent(String.format("%.2f", (emRejected / totalEMTxn) * 100));
			} else if (paymentType.equals(PaymentType.COD.getCode())) {
				analyticsData.setCaptured(String.format("%.0f", cdSuccess));
				analyticsData.setFailed(String.format("%.0f", cdFailed));
				analyticsData.setCancelled(String.format("%.0f", cdCancelled));
				analyticsData.setInvalid(String.format("%.0f", cdInvalid));
				analyticsData.setFraud(String.format("%.0f", cdFraud));
				analyticsData.setDropped(String.format("%.0f", cdDropped));
				analyticsData.setRejected(String.format("%.0f", cdRejected));

				analyticsData.setCapturedPercent(String.format("%.2f", (cdSuccess / totalCDTxn) * 100));
				analyticsData.setFailedPercent(String.format("%.2f", (cdFailed / totalCDTxn) * 100));
				analyticsData.setCancelledPercent(String.format("%.2f", (cdCancelled / totalCDTxn) * 100));
				analyticsData.setInvalidPercent(String.format("%.2f", (cdInvalid / totalCDTxn) * 100));
				analyticsData.setFraudPercent(String.format("%.2f", (cdFraud / totalCDTxn) * 100));
				analyticsData.setDroppedPercent(String.format("%.2f", (cdDropped / totalCDTxn) * 100));
				analyticsData.setRejectedPercent(String.format("%.2f", (cdRejected / totalCDTxn) * 100));
			} else if (paymentType.equals(PaymentType.NET_BANKING.getCode())) {
				analyticsData.setCaptured(String.format("%.0f", nbSuccess));
				analyticsData.setFailed(String.format("%.0f", nbFailed));
				analyticsData.setCancelled(String.format("%.0f", nbCancelled));
				analyticsData.setInvalid(String.format("%.0f", nbInvalid));
				analyticsData.setFraud(String.format("%.0f", nbFraud));
				analyticsData.setDropped(String.format("%.0f", nbDropped));
				analyticsData.setRejected(String.format("%.0f", nbRejected));

				analyticsData.setCapturedPercent(String.format("%.2f", (nbSuccess / totalNBTxn) * 100));
				analyticsData.setFailedPercent(String.format("%.2f", (nbFailed / totalNBTxn) * 100));
				analyticsData.setCancelledPercent(String.format("%.2f", (nbCancelled / totalNBTxn) * 100));
				analyticsData.setInvalidPercent(String.format("%.2f", (nbInvalid / totalNBTxn) * 100));
				analyticsData.setFraudPercent(String.format("%.2f", (nbFraud / totalNBTxn) * 100));
				analyticsData.setDroppedPercent(String.format("%.2f", (nbDropped / totalNBTxn) * 100));
				analyticsData.setRejectedPercent(String.format("%.2f", (nbRejected / totalNBTxn) * 100));
			}

			else {

				analyticsData.setCaptured("0");
				analyticsData.setFailed("0");
				analyticsData.setCancelled("0");
				analyticsData.setInvalid("0");
				analyticsData.setFraud("0");
				analyticsData.setDropped("0");
				analyticsData.setRejected("0");

				analyticsData.setCapturedPercent("0.00");
				analyticsData.setFailedPercent("0.00");
				analyticsData.setCancelledPercent("0.00");
				analyticsData.setInvalidPercent("0.00");
				analyticsData.setFraudPercent("0.00");
				analyticsData.setDroppedPercent("0.00");
				analyticsData.setRejectedPercent("0.00");

			}

			analyticsData.setTotalCCTxnAmount(String.format("%.2f", totalCCTxnAmount));
			analyticsData.setTotalDCTxnAmount(String.format("%.2f", totalDCTxnAmount));
			analyticsData.setTotalUPTxnAmount(String.format("%.2f", totalUPTxnAmount));
			analyticsData.setTotalNBTxnAmount(String.format("%.2f", totalNBTxnAmount));
			analyticsData.setTotalCDTxnAmount(String.format("%.2f", totalCDTxnAmount));
			analyticsData.setTotalEMTxnAmount(String.format("%.2f", totalEMTxnAmount));
			analyticsData.setTotalWLTxnAmount(String.format("%.2f", totalWLTxnAmount));

			analyticsData.setTotalCCCapturedCount(String.format("%.0f", ccSuccess));
			analyticsData.setTotalDCCapturedCount(String.format("%.0f", dcSuccess));
			analyticsData.setTotalUPCapturedCount(String.format("%.0f", upSuccess));
			analyticsData.setTotalNBCapturedCount(String.format("%.0f", nbSuccess));
			analyticsData.setTotalWLCapturedCount(String.format("%.0f", wlSuccess));
			analyticsData.setTotalCDCapturedCount(String.format("%.0f", cdSuccess));
			analyticsData.setTotalEMCapturedCount(String.format("%.0f", emSuccess));

			analyticsData.setUnknownTxnCount(String.valueOf(unknownTransactions));
			analyticsData.setTotalCapturedTxnAmount(String.format("%.2f", totalTxnAmount));

			// Refund and settled query
			List<BasicDBObject> saleSettleConditionsList = new ArrayList<BasicDBObject>();

			List<BasicDBObject> saleSettledConditionList = new ArrayList<BasicDBObject>();
			saleSettledConditionList
					.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.RECO.getName()));
			saleSettledConditionList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.SETTLED.getName()));

			BasicDBObject saleSettledQuery = new BasicDBObject("$and", saleSettledConditionList);

			saleSettleConditionsList.add(saleSettledQuery);

			List<BasicDBObject> deltaRefundCapturedConditionsList = new ArrayList<BasicDBObject>();

			List<BasicDBObject> deltaRefundCapturedConditionList = new ArrayList<BasicDBObject>();
			deltaRefundCapturedConditionList
					.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.REFUND.getName()));
			deltaRefundCapturedConditionList
					.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.CAPTURED.getName()));
			deltaRefundCapturedConditionList.add(new BasicDBObject(FieldType.UDF6.getName(), Constants.Y.getValue()));

			BasicDBObject deltaRefundQuery = new BasicDBObject("$and", deltaRefundCapturedConditionList);

			deltaRefundCapturedConditionsList.add(deltaRefundQuery);

			List<BasicDBObject> postSettleConditionsList = new ArrayList<BasicDBObject>();

			List<BasicDBObject> postSettleConditionList = new ArrayList<BasicDBObject>();
			postSettleConditionList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.RECO.getName()));
			postSettleConditionList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.SETTLED.getName()));
			postSettleConditionList
					.add(new BasicDBObject(FieldType.POST_SETTLED_FLAG.getName(), Constants.Y.getValue()));

			BasicDBObject postSettleQuery = new BasicDBObject("$and", postSettleConditionList);

			postSettleConditionsList.add(postSettleQuery);

			BasicDBObject saleSettledConditionQuery = new BasicDBObject("$or", saleSettleConditionsList);
			BasicDBObject deltaRefundConditionQuery = new BasicDBObject("$or", deltaRefundCapturedConditionsList);
			BasicDBObject postSettleConditionQuery = new BasicDBObject("$or", postSettleConditionsList);

			List<BasicDBObject> allConditionQueryList1 = new ArrayList<BasicDBObject>();
			List<BasicDBObject> allConditionQueryList2 = new ArrayList<BasicDBObject>();
			List<BasicDBObject> allConditionQueryList3 = new ArrayList<BasicDBObject>();

			if (!acquirerQuery.isEmpty()) {
				allConditionQueryList1.add(acquirerQuery);
			}
			if (!dateQuery.isEmpty()) {
				allConditionQueryList1.add(dateQuery);
			}
			allConditionQueryList1.add(saleSettledConditionQuery);

			if (!acquirerQuery.isEmpty()) {
				allConditionQueryList2.add(acquirerQuery);
			}
			if (!dateQuery.isEmpty()) {
				allConditionQueryList2.add(dateQuery);
			}
			allConditionQueryList2.add(deltaRefundConditionQuery);

			if (!acquirerQuery.isEmpty()) {
				allConditionQueryList3.add(acquirerQuery);
			}
			if (!dateQuery.isEmpty()) {
				allConditionQueryList3.add(dateQuery);
			}
			allConditionQueryList3.add(postSettleConditionQuery);

			BasicDBObject allConditionQueryObj1 = new BasicDBObject("$and", allConditionQueryList1);
			BasicDBObject allConditionQueryObj2 = new BasicDBObject("$and", allConditionQueryList2);
			BasicDBObject allConditionQueryObj3 = new BasicDBObject("$and", allConditionQueryList3);

			List<BasicDBObject> finalList1 = new ArrayList<BasicDBObject>();
			List<BasicDBObject> finalList2 = new ArrayList<BasicDBObject>();
			List<BasicDBObject> finalList3 = new ArrayList<BasicDBObject>();

			if (!allParamQuery.isEmpty()) {
				finalList1.add(allParamQuery);
			}
			if (!allConditionQueryObj1.isEmpty()) {
				finalList1.add(allConditionQueryObj1);
			}

			if (!allParamQuery.isEmpty()) {
				finalList2.add(allParamQuery);
			}
			if (!allConditionQueryObj2.isEmpty()) {
				finalList2.add(allConditionQueryObj2);
			}

			if (!allParamQuery.isEmpty()) {
				finalList3.add(allParamQuery);
			}
			if (!allConditionQueryObj2.isEmpty()) {
				finalList3.add(allConditionQueryObj3);
			}

			BasicDBObject finalQuery1 = new BasicDBObject("$and", finalList1);
			BasicDBObject finalQuery2 = new BasicDBObject("$and", finalList2);
			BasicDBObject finalQuery3 = new BasicDBObject("$and", finalList3);

			double totalSettled = (double) coll.count(finalQuery1);
			double totalDeltaRefund = (double) coll.count(finalQuery2);
			double totalPostSettled = (double) coll.count(finalQuery3);

			if (totalSettled == 0.0) {
				analyticsData.setMerchantPgRatio("0.00 %");
				analyticsData.setAcquirerPgRatio("0.00 %");
			}

			else {
				double merchantPgRatio = (((totalSettled - totalDeltaRefund) / (totalSettled)) * 100);
				double acquirerPgRatio = (((totalSettled - totalPostSettled) / (totalSettled)) * 100);

				analyticsData.setMerchantPgRatio(String.format("%.2f", merchantPgRatio) + " %");
				analyticsData.setAcquirerPgRatio(String.format("%.2f", acquirerPgRatio) + " %");
			}

			return analyticsData;
		}

		catch (Exception e) {
			logger.error("Exception in transaction summary count service " , e);
		}
		return null;
	}

	public AnalyticsData getSettledTransaction(String fromDate, String toDate, String payId, String paymentType,
			String acquirer, User user) {

		try {
			BasicDBObject dateQuery = new BasicDBObject();
			List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();
			BasicDBObject acquirerQuery = new BasicDBObject();

			BasicDBObject allParamQuery = new BasicDBObject();
			List<BasicDBObject> acquirerConditionLst = new ArrayList<BasicDBObject>();

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
			if (!acquirer.equalsIgnoreCase("ALL")) {

				List<String> acquirerList = Arrays.asList(acquirer.split(","));
				for (String acq : acquirerList) {

					acquirerConditionLst.add(new BasicDBObject(FieldType.ACQUIRER_TYPE.getName(), acq));
				}
				acquirerQuery.append("$or", acquirerConditionLst);
			}

			if (!paymentType.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.PAYMENT_TYPE.getName(), paymentType));
			}

			if (!paramConditionLst.isEmpty()) {
				allParamQuery = new BasicDBObject("$and", paramConditionLst);
			}

			// SALE Captured query
			List<BasicDBObject> txnConditionsList = new ArrayList<BasicDBObject>();
			List<BasicDBObject> saleCapturedConditionList = new ArrayList<BasicDBObject>();
			saleCapturedConditionList
					.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.RECO.getName()));
			saleCapturedConditionList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.SETTLED.getName()));

			BasicDBObject saleConditionQuery = new BasicDBObject("$and", saleCapturedConditionList);
			txnConditionsList.add(saleConditionQuery);

			// FAIL query
			List<BasicDBObject> failedConditionList = new ArrayList<BasicDBObject>();
			failedConditionList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName()));
			failedConditionList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.FAILED.getName()));

			BasicDBObject failedConditionQuery = new BasicDBObject("$and", failedConditionList);
			txnConditionsList.add(failedConditionQuery);
			// Error query
			List<BasicDBObject> errorConditionList = new ArrayList<BasicDBObject>();
			errorConditionList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName()));
			errorConditionList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.ERROR.getName()));

			BasicDBObject errorConditionQuery = new BasicDBObject("$and", errorConditionList);

			txnConditionsList.add(errorConditionQuery);

			// Cancelled query
			List<BasicDBObject> cancelledConditionList = new ArrayList<BasicDBObject>();
			cancelledConditionList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName()));
			cancelledConditionList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.CANCELLED.getName()));

			BasicDBObject cancelledConditionQuery = new BasicDBObject("$and", cancelledConditionList);
			txnConditionsList.add(cancelledConditionQuery);

			// Invalid query
			List<BasicDBObject> invalidConditionList = new ArrayList<BasicDBObject>();
			invalidConditionList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName()));
			invalidConditionList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.INVALID.getName()));

			BasicDBObject invalidConditionQuery = new BasicDBObject("$and", invalidConditionList);
			txnConditionsList.add(invalidConditionQuery);

			// Fraud query
			List<BasicDBObject> fraudConditionList = new ArrayList<BasicDBObject>();
			fraudConditionList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName()));
			fraudConditionList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.DENIED_BY_FRAUD.getName()));

			BasicDBObject fraudConditionQuery = new BasicDBObject("$and", fraudConditionList);
			txnConditionsList.add(fraudConditionQuery);

			// Dropped query
			List<BasicDBObject> droppedConditionList = new ArrayList<BasicDBObject>();
			droppedConditionList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName()));
			droppedConditionList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.TIMEOUT.getName()));

			BasicDBObject droppedConditionQuery = new BasicDBObject("$and", droppedConditionList);
			txnConditionsList.add(droppedConditionQuery);

			// Rejected query
			List<BasicDBObject> rejectedConditionList = new ArrayList<BasicDBObject>();
			rejectedConditionList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName()));
			rejectedConditionList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.REJECTED.getName()));

			BasicDBObject rejectedConditionQuery = new BasicDBObject("$and", rejectedConditionList);
			txnConditionsList.add(rejectedConditionQuery);

			BasicDBObject saleCapturedConditionQuery = new BasicDBObject("$or", txnConditionsList);

			List<BasicDBObject> allConditionQueryList = new ArrayList<BasicDBObject>();

			if (!acquirerQuery.isEmpty()) {
				allConditionQueryList.add(acquirerQuery);
			}
			if (!dateQuery.isEmpty()) {
				allConditionQueryList.add(dateQuery);
			}
			allConditionQueryList.add(saleCapturedConditionQuery);

			BasicDBObject allConditionQueryObj = new BasicDBObject("$and", allConditionQueryList);

			List<BasicDBObject> fianlList = new ArrayList<BasicDBObject>();

			if (!allParamQuery.isEmpty()) {
				fianlList.add(allParamQuery);
			}
			if (!allConditionQueryObj.isEmpty()) {
				fianlList.add(allConditionQueryObj);
			}

			BasicDBObject finalQuery = new BasicDBObject("$and", fianlList);
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns
					.getCollection(PropertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));

			double ccSuccess = 0;
			double ccFailed = 0;
			double ccCancelled = 0;
			double ccInvalid = 0;
			double ccFraud = 0;
			double ccDropped = 0;
			double ccRejected = 0;

			double dcSuccess = 0;
			double dcFailed = 0;
			double dcCancelled = 0;
			double dcInvalid = 0;
			double dcFraud = 0;
			double dcDropped = 0;
			double dcRejected = 0;

			double upSuccess = 0;
			double upFailed = 0;
			double upCancelled = 0;
			double upInvalid = 0;
			double upFraud = 0;
			double upDropped = 0;
			double upRejected = 0;

			double nbSuccess = 0;
			double nbFailed = 0;
			double nbCancelled = 0;
			double nbInvalid = 0;
			double nbFraud = 0;
			double nbDropped = 0;
			double nbRejected = 0;

			double wlSuccess = 0;
			double wlFailed = 0;
			double wlCancelled = 0;
			double wlInvalid = 0;
			double wlFraud = 0;
			double wlDropped = 0;
			double wlRejected = 0;

			double emSuccess = 0;
			double emFailed = 0;
			double emCancelled = 0;
			double emInvalid = 0;
			double emFraud = 0;
			double emDropped = 0;
			double emRejected = 0;

			double cdSuccess = 0;
			double cdFailed = 0;
			double cdCancelled = 0;
			double cdInvalid = 0;
			double cdFraud = 0;
			double cdDropped = 0;
			double cdRejected = 0;

			double totalTxn = 0;
			double totalCCTxn = 0;
			double totalDCTxn = 0;
			double totalUPTxn = 0;
			double totalNBTxn = 0;
			double totalWLTxn = 0;
			double totalEMTxn = 0;
			double totalCDTxn = 0;

			int unknownTransactions = 0;

			BigDecimal totalTxnAmount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_DOWN);

			BigDecimal totalCCTxnAmount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_DOWN);
			BigDecimal totalDCTxnAmount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_DOWN);
			BigDecimal totalUPTxnAmount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_DOWN);
			BigDecimal totalNBTxnAmount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_DOWN);
			BigDecimal totalWLTxnAmount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_DOWN);
			BigDecimal totalEMTxnAmount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_DOWN);
			BigDecimal totalCDTxnAmount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_DOWN);

			MongoCursor<Document> cursor = coll.find(finalQuery).iterator();

			// Remove all data from an earlier map
			while (cursor.hasNext()) {

				totalTxn++;
				Document dbobj = cursor.next();

				if (StringUtils.isBlank(dbobj.getString(FieldType.PAYMENT_TYPE.toString()))) {
					unknownTransactions++;
					continue;
				}
				switch (PaymentType.getInstanceUsingCode(dbobj.getString(FieldType.PAYMENT_TYPE.toString()))) {

				case DEBIT_CARD:

					totalDCTxn++;
					switch (dbobj.getString(FieldType.STATUS.toString())) {

					case CAPTURED:

						if (!StringUtils.isBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))) {
							BigDecimal txnAmount = new BigDecimal(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
									.setScale(2, RoundingMode.HALF_DOWN);
							totalTxnAmount = totalTxnAmount.add(txnAmount).setScale(2, RoundingMode.HALF_DOWN);
							totalDCTxnAmount = totalDCTxnAmount.add(txnAmount).setScale(2, RoundingMode.HALF_DOWN);

						}

						dcSuccess++;
						break;

					case ERROR:
						dcFailed++;
						break;

					case TIMEOUT:
						dcDropped++;
						break;

					case CANCELLED:
						dcCancelled++;
						break;

					case DENIED:
						dcRejected++;
						break;

					case REJECTED:
						dcRejected++;
						break;

					case FAILED:
						dcFailed++;
						break;

					case INVALID:
						dcInvalid++;
						break;

					case DENIED_BY_FRAUD:
						dcFraud++;
						break;

					default:
						break;
					}

					break;

				case CREDIT_CARD:

					totalCCTxn++;
					switch (dbobj.getString(FieldType.STATUS.toString())) {

					case CAPTURED:

						if (!StringUtils.isBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))) {
							BigDecimal txnAmount = new BigDecimal(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
									.setScale(2, RoundingMode.HALF_DOWN);
							;
							totalTxnAmount = totalTxnAmount.add(txnAmount).setScale(2, RoundingMode.HALF_DOWN);
							totalCCTxnAmount = totalCCTxnAmount.add(txnAmount).setScale(2, RoundingMode.HALF_DOWN);
						}

						ccSuccess++;

						break;

					case ERROR:
						ccFailed++;
						break;

					case TIMEOUT:
						ccDropped++;
						break;

					case CANCELLED:
						ccCancelled++;
						break;

					case DENIED:
						ccRejected++;
						break;

					case REJECTED:
						ccRejected++;
						break;

					case FAILED:
						ccFailed++;
						break;

					case INVALID:
						ccInvalid++;
						break;

					case DENIED_BY_FRAUD:
						ccFraud++;
						break;

					default:
						break;
					}

					break;

				case UPI:

					totalUPTxn++;
					switch (dbobj.getString(FieldType.STATUS.toString())) {

					case CAPTURED:

						if (!StringUtils.isBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))) {
							BigDecimal txnAmount = new BigDecimal(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
									.setScale(2, RoundingMode.HALF_DOWN);
							totalTxnAmount = totalTxnAmount.add(txnAmount).setScale(2, RoundingMode.HALF_DOWN);
							totalUPTxnAmount = totalUPTxnAmount.add(txnAmount).setScale(2, RoundingMode.HALF_DOWN);
						}

						upSuccess++;
						break;

					case ERROR:
						upFailed++;
						break;

					case TIMEOUT:
						upDropped++;
						break;

					case CANCELLED:
						upCancelled++;
						break;

					case DENIED:
						upRejected++;
						break;

					case REJECTED:
						upRejected++;
						break;

					case FAILED:
						upFailed++;
						break;

					case INVALID:
						upInvalid++;
						break;

					case DENIED_BY_FRAUD:
						upFraud++;
						break;

					default:
						break;
					}

					break;

				default:
					break;

				case NET_BANKING:

					totalNBTxn++;
					switch (dbobj.getString(FieldType.STATUS.toString())) {

					case CAPTURED:

						if (!StringUtils.isBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))) {
							BigDecimal txnAmount = new BigDecimal(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
									.setScale(2, RoundingMode.HALF_DOWN);
							totalTxnAmount = totalTxnAmount.add(txnAmount).setScale(2, RoundingMode.HALF_DOWN);
							totalNBTxnAmount = totalNBTxnAmount.add(txnAmount).setScale(2, RoundingMode.HALF_DOWN);

						}

						nbSuccess++;
						break;

					case ERROR:
						nbFailed++;
						break;

					case TIMEOUT:
						nbDropped++;
						break;

					case CANCELLED:
						nbCancelled++;
						break;

					case DENIED:
						nbRejected++;
						break;

					case REJECTED:
						nbRejected++;
						break;

					case FAILED:
						nbFailed++;
						break;

					case INVALID:
						nbInvalid++;
						break;

					case DENIED_BY_FRAUD:
						nbFraud++;
						break;

					default:
						break;
					}

					break;

				case COD:
					totalCDTxn++;
					switch (dbobj.getString(FieldType.STATUS.toString())) {

					case CAPTURED:

						if (!StringUtils.isBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))) {
							BigDecimal txnAmount = new BigDecimal(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
									.setScale(2, RoundingMode.HALF_DOWN);
							totalTxnAmount = totalTxnAmount.add(txnAmount).setScale(2, RoundingMode.HALF_DOWN);
							totalCDTxnAmount = totalCDTxnAmount.add(txnAmount).setScale(2, RoundingMode.HALF_DOWN);

						}

						cdSuccess++;
						break;

					case ERROR:
						cdFailed++;
						break;

					case TIMEOUT:
						cdDropped++;
						break;

					case CANCELLED:
						cdCancelled++;
						break;

					case DENIED:
						cdRejected++;
						break;

					case REJECTED:
						cdRejected++;
						break;

					case FAILED:
						cdFailed++;
						break;

					case INVALID:
						cdInvalid++;
						break;

					case DENIED_BY_FRAUD:
						cdFraud++;
						break;

					default:
						break;
					}

					break;

				case EMI:
					totalEMTxn++;
					switch (dbobj.getString(FieldType.STATUS.toString())) {

					case CAPTURED:

						if (!StringUtils.isBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))) {
							BigDecimal txnAmount = new BigDecimal(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
									.setScale(2, RoundingMode.HALF_DOWN);
							totalTxnAmount = totalTxnAmount.add(txnAmount).setScale(2, RoundingMode.HALF_DOWN);
							totalEMTxnAmount = totalEMTxnAmount.add(txnAmount).setScale(2, RoundingMode.HALF_DOWN);

						}

						emSuccess++;
						break;

					case ERROR:
						emFailed++;
						break;

					case TIMEOUT:
						emDropped++;
						break;

					case CANCELLED:
						emCancelled++;
						break;

					case DENIED:
						emRejected++;
						break;

					case REJECTED:
						emRejected++;
						break;

					case FAILED:
						emFailed++;
						break;

					case INVALID:
						emInvalid++;
						break;

					case DENIED_BY_FRAUD:
						emFraud++;
						break;

					default:
						break;
					}

					break;

				case WALLET:
					totalWLTxn++;
					switch (dbobj.getString(FieldType.STATUS.toString())) {

					case CAPTURED:

						if (!StringUtils.isBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))) {
							BigDecimal txnAmount = new BigDecimal(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))
									.setScale(2, RoundingMode.HALF_DOWN);
							totalTxnAmount = totalTxnAmount.add(txnAmount).setScale(2, RoundingMode.HALF_DOWN);
							totalWLTxnAmount = totalWLTxnAmount.add(txnAmount).setScale(2, RoundingMode.HALF_DOWN);

						}

						wlSuccess++;
						break;

					case ERROR:
						wlFailed++;
						break;

					case TIMEOUT:
						wlDropped++;
						break;

					case CANCELLED:
						wlCancelled++;
						break;

					case DENIED:
						wlRejected++;
						break;

					case REJECTED:
						wlRejected++;
						break;

					case FAILED:
						wlFailed++;
						break;

					case INVALID:
						wlInvalid++;
						break;

					case DENIED_BY_FRAUD:
						wlFraud++;
						break;

					default:
						break;
					}

					break;

				}
			}

			cursor.close();

			// totalTxnAmount = totalTxnAmount.setScale(2, RoundingMode.HALF_DOWN);
			AnalyticsData analyticsData = new AnalyticsData();

			double totalTxnSuccess = ccSuccess + dcSuccess + upSuccess + nbSuccess + emSuccess + cdSuccess + wlSuccess;
			double totalFailed = ccFailed + dcFailed + upFailed + nbSuccess + emSuccess + cdSuccess + wlSuccess;

			analyticsData.setTotalTxnCount(String.format("%.0f", totalTxn));
			analyticsData.setSuccessTxnCount(String.format("%.0f", totalTxnSuccess));
			analyticsData.setFailedTxnCount(String.format("%.2f", totalFailed));

			if (totalTxn == 0.00) {
				analyticsData.setSuccessTxnPercent("0.00");
			} else {
				double successTxnPercent = (totalTxnSuccess / totalTxn) * 100;
				analyticsData.setSuccessTxnPercent(String.format("%.2f", successTxnPercent));
			}

			BigDecimal totalTxnCountBD = new BigDecimal(totalTxnSuccess);
			totalTxnCountBD = totalTxnCountBD.setScale(2, RoundingMode.HALF_DOWN);

			if (totalTxnCountBD.compareTo(BigDecimal.ONE) < 0) {
				analyticsData.setAvgTkt("0.00");
			} else {
				String totalTxnAmountString = String.valueOf(totalTxnAmount);
				double totalTxnAmountDouble = Double.valueOf(totalTxnAmountString);
				double totalTxnSuccessDouble = Double.valueOf(totalTxnSuccess);
				double avgTicketSizeDouble = totalTxnAmountDouble / totalTxnSuccessDouble;

				analyticsData.setAvgTkt(String.format("%.2f", avgTicketSizeDouble));
			}

			if (totalTxnSuccess < 1) {
				analyticsData.setCCTxnPercent("0.00");
				analyticsData.setDCTxnPercent("0.00");
				analyticsData.setUPTxnPercent("0.00");
				analyticsData.setNBTxnPercent("0.00");
				analyticsData.setWLTxnPercent("0.00");
				analyticsData.setEMTxnPercent("0.00");
				analyticsData.setCDTxnPercent("0.00");
			}

			else {

				double cCTxnPercent = (ccSuccess / totalTxnSuccess) * 100;
				double dCTxnPercent = (dcSuccess / totalTxnSuccess) * 100;
				double upTxnPercent = (upSuccess / totalTxnSuccess) * 100;
				double nbTxnPercent = (nbSuccess / totalTxnSuccess) * 100;
				double emTxnPercent = (emSuccess / totalTxnSuccess) * 100;
				double wlTxnPercent = (wlSuccess / totalTxnSuccess) * 100;
				double cdTxnPercent = (cdSuccess / totalTxnSuccess) * 100;

				analyticsData.setCCTxnPercent(String.format("%.2f", cCTxnPercent));
				analyticsData.setDCTxnPercent(String.format("%.2f", dCTxnPercent));
				analyticsData.setUPTxnPercent(String.format("%.2f", upTxnPercent));
				analyticsData.setNBTxnPercent(String.format("%.2f", nbTxnPercent));
				analyticsData.setEMTxnPercent(String.format("%.2f", emTxnPercent));
				analyticsData.setWLTxnPercent(String.format("%.2f", wlTxnPercent));
				analyticsData.setCDTxnPercent(String.format("%.2f", cdTxnPercent));
			}

			if (cdSuccess + cdFailed < 1) {
				analyticsData.setCDSuccessRate("0.00");
			} else {
				double cDSuccessRate = (cdSuccess / (cdSuccess + cdFailed)) * 100;
				analyticsData.setCDSuccessRate(String.format("%.2f", cDSuccessRate));
			}
			
			if (emSuccess + emFailed < 1) {
				analyticsData.setEMSuccessRate("0.00");
			} else {
				double eMSuccessRate = (emSuccess / (emSuccess + emFailed)) * 100;
				analyticsData.setEMSuccessRate(String.format("%.2f", eMSuccessRate));
			}
			
			if (wlSuccess + wlFailed < 1) {
				analyticsData.setWLSuccessRate("0.00");
			} else {
				double wLSuccessRate = (wlSuccess / (wlSuccess + wlFailed)) * 100;
				analyticsData.setWLSuccessRate(String.format("%.2f", wLSuccessRate));
			}
			
			if (nbSuccess + nbFailed < 1) {
				analyticsData.setNBSuccessRate("0.00");
			} else {
				double nBSuccessRate = (nbSuccess / (nbSuccess + nbFailed)) * 100;
				analyticsData.setNBSuccessRate(String.format("%.2f", nBSuccessRate));
			}
			
			if (ccSuccess + ccFailed < 1) {
				analyticsData.setCCSuccessRate("0.00");
			} else {
				double cCSuccessRate = (ccSuccess / (ccSuccess + ccFailed)) * 100;
				analyticsData.setCCSuccessRate(String.format("%.2f", cCSuccessRate));
			}

			if (dcSuccess + dcFailed < 1) {
				analyticsData.setDCSuccessRate("0.00");
			} else {
				double dCSuccessRate = (dcSuccess / (dcSuccess + dcFailed)) * 100;
				analyticsData.setDCSuccessRate(String.format("%.2f", dCSuccessRate));
			}

			if (upSuccess + upFailed < 1) {
				analyticsData.setUPSuccessRate("0.00");
			} else {
				double uPSuccessRate = (upSuccess / (upSuccess + upFailed)) * 100;
				analyticsData.setUPSuccessRate(String.format("%.2f", uPSuccessRate));
			}

			if (totalEMTxn < 1) {

				analyticsData.setTotalEMTxn("0");
				analyticsData.setTotalEMSuccessTxnPercent("0.00");
				analyticsData.setTotalEMFailedTxnPercent("0.00");
				analyticsData.setTotalEMCancelledTxnPercent("0.00");
				analyticsData.setTotalEMInvalidTxnPercent("0.00");
				analyticsData.setTotalEMFraudTxnPercent("0.00");
				analyticsData.setTotalEMDroppedTxnPercent("0.00");
				analyticsData.setTotalEMRejectedTxnPercent("0.00");

			} else {

				double totalEMSuccessTxnPercent = (emSuccess / (totalEMTxn)) * 100;
				double totalEMFailedTxnPercent = (emFailed / (totalEMTxn)) * 100;
				double totalEMCancelledTxnPercent = (emCancelled / (totalEMTxn)) * 100;
				double totalEMInvalidTxnPercent = (emInvalid / (totalEMTxn)) * 100;
				double totalEMFraudTxnPercent = (emFraud / (totalEMTxn)) * 100;
				double totalEMDroppedTxnPercent = (emDropped / (totalEMTxn)) * 100;
				double totalEMRejectedTxnPercent = (emRejected / (totalEMTxn)) * 100;

				analyticsData.setTotalEMTxn(String.format("%.0f", totalEMTxn));
				analyticsData.setTotalEMSuccessTxnPercent(String.format("%.2f", totalEMSuccessTxnPercent) + " %");
				analyticsData.setTotalEMFailedTxnPercent(String.format("%.2f", totalEMFailedTxnPercent));
				analyticsData.setTotalEMCancelledTxnPercent(String.format("%.2f", totalEMCancelledTxnPercent));
				analyticsData.setTotalEMInvalidTxnPercent(String.format("%.2f", totalEMInvalidTxnPercent));
				analyticsData.setTotalEMFraudTxnPercent(String.format("%.2f", totalEMFraudTxnPercent));
				analyticsData.setTotalEMDroppedTxnPercent(String.format("%.2f", totalEMDroppedTxnPercent));
				analyticsData.setTotalEMRejectedTxnPercent(String.format("%.2f", totalEMRejectedTxnPercent));

			}
			if (totalWLTxn < 1) {

				analyticsData.setTotalWLTxn("0");
				analyticsData.setTotalWLSuccessTxnPercent("0.00");
				analyticsData.setTotalWLFailedTxnPercent("0.00");
				analyticsData.setTotalWLCancelledTxnPercent("0.00");
				analyticsData.setTotalWLInvalidTxnPercent("0.00");
				analyticsData.setTotalWLFraudTxnPercent("0.00");
				analyticsData.setTotalWLDroppedTxnPercent("0.00");
				analyticsData.setTotalWLRejectedTxnPercent("0.00");

			} else {

				double totalWLSuccessTxnPercent = (wlSuccess / (totalWLTxn)) * 100;
				double totalWLFailedTxnPercent = (wlFailed / (totalWLTxn)) * 100;
				double totalWLCancelledTxnPercent = (wlCancelled / (totalWLTxn)) * 100;
				double totalWLInvalidTxnPercent = (wlInvalid / (totalWLTxn)) * 100;
				double totalWLFraudTxnPercent = (wlFraud / (totalWLTxn)) * 100;
				double totalWLDroppedTxnPercent = (wlDropped / (totalWLTxn)) * 100;
				double totalWLRejectedTxnPercent = (wlRejected / (totalWLTxn)) * 100;

				analyticsData.setTotalWLTxn(String.format("%.0f", totalWLTxn));
				analyticsData.setTotalWLSuccessTxnPercent(String.format("%.2f", totalWLSuccessTxnPercent) + " %");
				analyticsData.setTotalWLFailedTxnPercent(String.format("%.2f", totalWLFailedTxnPercent));
				analyticsData.setTotalWLCancelledTxnPercent(String.format("%.2f", totalWLCancelledTxnPercent));
				analyticsData.setTotalWLInvalidTxnPercent(String.format("%.2f", totalWLInvalidTxnPercent));
				analyticsData.setTotalWLFraudTxnPercent(String.format("%.2f", totalWLFraudTxnPercent));
				analyticsData.setTotalWLDroppedTxnPercent(String.format("%.2f", totalWLDroppedTxnPercent));
				analyticsData.setTotalWLRejectedTxnPercent(String.format("%.2f", totalWLRejectedTxnPercent));

			}
			if (totalCDTxn < 1) {

				analyticsData.setTotalCDTxn("0");
				analyticsData.setTotalCDSuccessTxnPercent("0.00");
				analyticsData.setTotalCDFailedTxnPercent("0.00");
				analyticsData.setTotalCDCancelledTxnPercent("0.00");
				analyticsData.setTotalCDInvalidTxnPercent("0.00");
				analyticsData.setTotalCDFraudTxnPercent("0.00");
				analyticsData.setTotalCDDroppedTxnPercent("0.00");
				analyticsData.setTotalCDRejectedTxnPercent("0.00");

			} else {

				double totalCDSuccessTxnPercent = (cdSuccess / (totalCDTxn)) * 100;
				double totalCDFailedTxnPercent = (cdFailed / (totalCDTxn)) * 100;
				double totalCDCancelledTxnPercent = (cdCancelled / (totalCDTxn)) * 100;
				double totalCDInvalidTxnPercent = (cdInvalid / (totalCDTxn)) * 100;
				double totalCDFraudTxnPercent = (cdFraud / (totalCDTxn)) * 100;
				double totalCDDroppedTxnPercent = (cdDropped / (totalCDTxn)) * 100;
				double totalCDRejectedTxnPercent = (cdRejected / (totalCDTxn)) * 100;

				analyticsData.setTotalCDTxn(String.format("%.0f", totalCDTxn));
				analyticsData.setTotalCDSuccessTxnPercent(String.format("%.2f", totalCDSuccessTxnPercent) + " %");
				analyticsData.setTotalCDFailedTxnPercent(String.format("%.2f", totalCDFailedTxnPercent));
				analyticsData.setTotalCDCancelledTxnPercent(String.format("%.2f", totalCDCancelledTxnPercent));
				analyticsData.setTotalCDInvalidTxnPercent(String.format("%.2f", totalCDInvalidTxnPercent));
				analyticsData.setTotalCDFraudTxnPercent(String.format("%.2f", totalCDFraudTxnPercent));
				analyticsData.setTotalCDDroppedTxnPercent(String.format("%.2f", totalCDDroppedTxnPercent));
				analyticsData.setTotalCDRejectedTxnPercent(String.format("%.2f", totalCDRejectedTxnPercent));

			}
			
			if (totalNBTxn < 1) {

				analyticsData.setTotalNBTxn("0");
				analyticsData.setTotalNBSuccessTxnPercent("0.00");
				analyticsData.setTotalNBFailedTxnPercent("0.00");
				analyticsData.setTotalNBCancelledTxnPercent("0.00");
				analyticsData.setTotalNBInvalidTxnPercent("0.00");
				analyticsData.setTotalNBFraudTxnPercent("0.00");
				analyticsData.setTotalNBDroppedTxnPercent("0.00");
				analyticsData.setTotalNBRejectedTxnPercent("0.00");

			} else {

				double totalNBSuccessTxnPercent = (nbSuccess / (totalNBTxn)) * 100;
				double totalNBFailedTxnPercent = (nbFailed / (totalNBTxn)) * 100;
				double totalNBCancelledTxnPercent = (nbCancelled / (totalNBTxn)) * 100;
				double totalNBInvalidTxnPercent = (nbInvalid / (totalNBTxn)) * 100;
				double totalNBFraudTxnPercent = (nbFraud / (totalNBTxn)) * 100;
				double totalNBDroppedTxnPercent = (nbDropped / (totalNBTxn)) * 100;
				double totalNBRejectedTxnPercent = (nbRejected / (totalNBTxn)) * 100;

				analyticsData.setTotalNBTxn(String.format("%.0f", totalNBTxn));
				analyticsData.setTotalNBSuccessTxnPercent(String.format("%.2f", totalNBSuccessTxnPercent) + " %");
				analyticsData.setTotalNBFailedTxnPercent(String.format("%.2f", totalNBFailedTxnPercent));
				analyticsData.setTotalNBCancelledTxnPercent(String.format("%.2f", totalNBCancelledTxnPercent));
				analyticsData.setTotalNBInvalidTxnPercent(String.format("%.2f", totalNBInvalidTxnPercent));
				analyticsData.setTotalNBFraudTxnPercent(String.format("%.2f", totalNBFraudTxnPercent));
				analyticsData.setTotalNBDroppedTxnPercent(String.format("%.2f", totalNBDroppedTxnPercent));
				analyticsData.setTotalNBRejectedTxnPercent(String.format("%.2f", totalNBRejectedTxnPercent));

			}
			
			if (totalCCTxn < 1) {

				analyticsData.setTotalCCTxn("0");
				analyticsData.setTotalCCSuccessTxnPercent("0.00");
				analyticsData.setTotalCCFailedTxnPercent("0.00");
				analyticsData.setTotalCCCancelledTxnPercent("0.00");
				analyticsData.setTotalCCInvalidTxnPercent("0.00");
				analyticsData.setTotalCCFraudTxnPercent("0.00");
				analyticsData.setTotalCCDroppedTxnPercent("0.00");
				analyticsData.setTotalCCRejectedTxnPercent("0.00");

			} else {

				double totalCCSuccessTxnPercent = (ccSuccess / (totalCCTxn)) * 100;
				double totalCCFailedTxnPercent = (ccFailed / (totalCCTxn)) * 100;
				double totalCCCancelledTxnPercent = (ccCancelled / (totalCCTxn)) * 100;
				double totalCCInvalidTxnPercent = (ccInvalid / (totalCCTxn)) * 100;
				double totalCCFraudTxnPercent = (ccFraud / (totalCCTxn)) * 100;
				double totalCCDroppedTxnPercent = (ccDropped / (totalCCTxn)) * 100;
				double totalCCRejectedTxnPercent = (ccRejected / (totalCCTxn)) * 100;

				analyticsData.setTotalCCTxn(String.format("%.0f", totalCCTxn));
				analyticsData.setTotalCCSuccessTxnPercent(String.format("%.2f", totalCCSuccessTxnPercent) + " %");
				analyticsData.setTotalCCFailedTxnPercent(String.format("%.2f", totalCCFailedTxnPercent));
				analyticsData.setTotalCCCancelledTxnPercent(String.format("%.2f", totalCCCancelledTxnPercent));
				analyticsData.setTotalCCInvalidTxnPercent(String.format("%.2f", totalCCInvalidTxnPercent));
				analyticsData.setTotalCCFraudTxnPercent(String.format("%.2f", totalCCFraudTxnPercent));
				analyticsData.setTotalCCDroppedTxnPercent(String.format("%.2f", totalCCDroppedTxnPercent));
				analyticsData.setTotalCCRejectedTxnPercent(String.format("%.2f", totalCCRejectedTxnPercent));

			}

			if (totalDCTxn < 1) {

				analyticsData.setTotalDCTxn("0");
				analyticsData.setTotalDCSuccessTxnPercent("0.00");
				analyticsData.setTotalDCFailedTxnPercent("0.00");
				analyticsData.setTotalDCCancelledTxnPercent("0.00");
				analyticsData.setTotalDCInvalidTxnPercent("0.00");
				analyticsData.setTotalDCFraudTxnPercent("0.00");
				analyticsData.setTotalDCDroppedTxnPercent("0.00");
				analyticsData.setTotalDCRejectedTxnPercent("0.00");

			} else {

				double totalDCSuccessTxnPercent = (dcSuccess / (totalDCTxn)) * 100;
				double totalDCFailedTxnPercent = (dcFailed / (totalDCTxn)) * 100;
				double totalDCCancelledTxnPercent = (dcCancelled / (totalDCTxn)) * 100;
				double totalDCInvalidTxnPercent = (dcInvalid / (totalDCTxn)) * 100;
				double totalDCFraudTxnPercent = (dcFraud / (totalDCTxn)) * 100;
				double totalDCDroppedTxnPercent = (dcDropped / (totalDCTxn)) * 100;
				double totalDCRejectedTxnPercent = (dcRejected / (totalDCTxn)) * 100;

				analyticsData.setTotalDCTxn(String.format("%.0f", totalDCTxn));
				analyticsData.setTotalDCSuccessTxnPercent(String.format("%.2f", totalDCSuccessTxnPercent));
				analyticsData.setTotalDCFailedTxnPercent(String.format("%.2f", totalDCFailedTxnPercent));
				analyticsData.setTotalDCCancelledTxnPercent(String.format("%.2f", totalDCCancelledTxnPercent));
				analyticsData.setTotalDCInvalidTxnPercent(String.format("%.2f", totalDCInvalidTxnPercent));
				analyticsData.setTotalDCFraudTxnPercent(String.format("%.2f", totalDCFraudTxnPercent));
				analyticsData.setTotalDCDroppedTxnPercent(String.format("%.2f", totalDCDroppedTxnPercent));
				analyticsData.setTotalDCRejectedTxnPercent(String.format("%.2f", totalDCRejectedTxnPercent));

			}

			if (totalUPTxn < 1) {

				analyticsData.setTotalUPTxn("0");
				analyticsData.setTotalUPSuccessTxnPercent("0.00");
				analyticsData.setTotalUPFailedTxnPercent("0.00");
				analyticsData.setTotalUPCancelledTxnPercent("0.00");
				analyticsData.setTotalUPInvalidTxnPercent("0.00");
				analyticsData.setTotalUPFraudTxnPercent("0.00");
				analyticsData.setTotalUPDroppedTxnPercent("0.00");
				analyticsData.setTotalUPRejectedTxnPercent("0.00");
			} else {

				analyticsData.setTotalUPTxn(String.format("%.0f", totalUPTxn));

				double totalUPSuccessTxnPercent = (upSuccess / (totalUPTxn)) * 100;
				double totalUPFailedTxnPercent = (upFailed / (totalUPTxn)) * 100;
				double totalUPCancelledTxnPercent = (upCancelled / (totalUPTxn)) * 100;
				double totalUPInvalidTxnPercent = (upInvalid / (totalUPTxn)) * 100;
				double totalUPFraudTxnPercent = (upFraud / (totalUPTxn)) * 100;
				double totalUPDroppedTxnPercent = (upDropped / (totalUPTxn)) * 100;
				double totalUPRejectedTxnPercent = (upRejected / (totalUPTxn)) * 100;

				analyticsData.setTotalUPSuccessTxnPercent(String.format("%.2f", totalUPSuccessTxnPercent));
				analyticsData.setTotalUPFailedTxnPercent(String.format("%.2f", totalUPFailedTxnPercent));
				analyticsData.setTotalUPCancelledTxnPercent(String.format("%.2f", totalUPCancelledTxnPercent));
				analyticsData.setTotalUPInvalidTxnPercent(String.format("%.2f", totalUPInvalidTxnPercent));
				analyticsData.setTotalUPFraudTxnPercent(String.format("%.2f", totalUPFraudTxnPercent));
				analyticsData.setTotalUPDroppedTxnPercent(String.format("%.2f", totalUPDroppedTxnPercent));
				analyticsData.setTotalUPRejectedTxnPercent(String.format("%.2f", totalUPRejectedTxnPercent));

			}
			if (paymentType.equals(PaymentType.COD.getCode())) {
				analyticsData.setCaptured(String.format("%.0f", cdSuccess));
				analyticsData.setFailed(String.format("%.0f", cdFailed));
				analyticsData.setCancelled(String.format("%.0f", cdCancelled));
				analyticsData.setInvalid(String.format("%.0f", cdInvalid));
				analyticsData.setFraud(String.format("%.0f", cdFraud));
				analyticsData.setDropped(String.format("%.0f", cdDropped));
				analyticsData.setRejected(String.format("%.0f", cdRejected));

				analyticsData.setCapturedPercent(String.format("%.2f", (cdSuccess / totalCDTxn) * 100));
				analyticsData.setFailedPercent(String.format("%.2f", (cdFailed / totalCDTxn) * 100));
				analyticsData.setCancelledPercent(String.format("%.2f", (cdCancelled / totalCDTxn) * 100));
				analyticsData.setInvalidPercent(String.format("%.2f", (cdInvalid / totalCDTxn) * 100));
				analyticsData.setFraudPercent(String.format("%.2f", (cdFraud / totalCDTxn) * 100));
				analyticsData.setDroppedPercent(String.format("%.2f", (cdDropped / totalCDTxn) * 100));
				analyticsData.setRejectedPercent(String.format("%.2f", (cdRejected / totalCDTxn) * 100));
			}
			if (paymentType.equals(PaymentType.EMI.getCode())) {
				analyticsData.setCaptured(String.format("%.0f", emSuccess));
				analyticsData.setFailed(String.format("%.0f", emFailed));
				analyticsData.setCancelled(String.format("%.0f", emCancelled));
				analyticsData.setInvalid(String.format("%.0f", emInvalid));
				analyticsData.setFraud(String.format("%.0f", emFraud));
				analyticsData.setDropped(String.format("%.0f", emDropped));
				analyticsData.setRejected(String.format("%.0f", emRejected));

				analyticsData.setCapturedPercent(String.format("%.2f", (emSuccess / totalEMTxn) * 100));
				analyticsData.setFailedPercent(String.format("%.2f", (emFailed / totalEMTxn) * 100));
				analyticsData.setCancelledPercent(String.format("%.2f", (emCancelled / totalEMTxn) * 100));
				analyticsData.setInvalidPercent(String.format("%.2f", (emInvalid / totalEMTxn) * 100));
				analyticsData.setFraudPercent(String.format("%.2f", (emFraud / totalEMTxn) * 100));
				analyticsData.setDroppedPercent(String.format("%.2f", (emDropped / totalEMTxn) * 100));
				analyticsData.setRejectedPercent(String.format("%.2f", (emRejected / totalEMTxn) * 100));
			}
			if (paymentType.equals(PaymentType.WALLET.getCode())) {
				analyticsData.setCaptured(String.format("%.0f", wlSuccess));
				analyticsData.setFailed(String.format("%.0f", wlFailed));
				analyticsData.setCancelled(String.format("%.0f", wlCancelled));
				analyticsData.setInvalid(String.format("%.0f", wlInvalid));
				analyticsData.setFraud(String.format("%.0f", wlFraud));
				analyticsData.setDropped(String.format("%.0f", wlDropped));
				analyticsData.setRejected(String.format("%.0f", wlRejected));

				analyticsData.setCapturedPercent(String.format("%.2f", (wlSuccess / totalWLTxn) * 100));
				analyticsData.setFailedPercent(String.format("%.2f", (wlFailed / totalWLTxn) * 100));
				analyticsData.setCancelledPercent(String.format("%.2f", (wlCancelled / totalWLTxn) * 100));
				analyticsData.setInvalidPercent(String.format("%.2f", (wlInvalid / totalWLTxn) * 100));
				analyticsData.setFraudPercent(String.format("%.2f", (wlFraud / totalWLTxn) * 100));
				analyticsData.setDroppedPercent(String.format("%.2f", (wlDropped / totalWLTxn) * 100));
				analyticsData.setRejectedPercent(String.format("%.2f", (wlRejected / totalWLTxn) * 100));
			}
			if (paymentType.equals(PaymentType.NET_BANKING.getCode())) {
				analyticsData.setCaptured(String.format("%.0f", nbSuccess));
				analyticsData.setFailed(String.format("%.0f", nbFailed));
				analyticsData.setCancelled(String.format("%.0f", nbCancelled));
				analyticsData.setInvalid(String.format("%.0f", nbInvalid));
				analyticsData.setFraud(String.format("%.0f", nbFraud));
				analyticsData.setDropped(String.format("%.0f", nbDropped));
				analyticsData.setRejected(String.format("%.0f", nbRejected));

				analyticsData.setCapturedPercent(String.format("%.2f", (nbSuccess / totalNBTxn) * 100));
				analyticsData.setFailedPercent(String.format("%.2f", (nbFailed / totalNBTxn) * 100));
				analyticsData.setCancelledPercent(String.format("%.2f", (nbCancelled / totalNBTxn) * 100));
				analyticsData.setInvalidPercent(String.format("%.2f", (nbInvalid / totalNBTxn) * 100));
				analyticsData.setFraudPercent(String.format("%.2f", (nbFraud / totalNBTxn) * 100));
				analyticsData.setDroppedPercent(String.format("%.2f", (nbDropped / totalNBTxn) * 100));
				analyticsData.setRejectedPercent(String.format("%.2f", (nbRejected / totalNBTxn) * 100));
			}
			if (paymentType.equals(PaymentType.CREDIT_CARD.getCode())) {

				analyticsData.setCaptured(String.format("%.0f", ccSuccess));
				analyticsData.setFailed(String.format("%.0f", ccFailed));
				analyticsData.setCancelled(String.format("%.0f", ccCancelled));
				analyticsData.setInvalid(String.format("%.0f", ccInvalid));
				analyticsData.setFraud(String.format("%.0f", ccFraud));
				analyticsData.setDropped(String.format("%.0f", ccDropped));
				analyticsData.setRejected(String.format("%.0f", ccRejected));

				analyticsData.setCapturedPercent(String.format("%.2f", (ccSuccess / totalCCTxn) * 100));
				analyticsData.setFailedPercent(String.format("%.2f", (ccFailed / totalCCTxn) * 100));
				analyticsData.setCancelledPercent(String.format("%.2f", (ccCancelled / totalCCTxn) * 100));
				analyticsData.setInvalidPercent(String.format("%.2f", (ccInvalid / totalCCTxn) * 100));
				analyticsData.setFraudPercent(String.format("%.2f", (ccFraud / totalCCTxn) * 100));
				analyticsData.setDroppedPercent(String.format("%.2f", (ccDropped / totalCCTxn) * 100));
				analyticsData.setRejectedPercent(String.format("%.2f", (ccRejected / totalCCTxn) * 100));

			} else if (paymentType.equals(PaymentType.DEBIT_CARD.getCode())) {

				analyticsData.setCaptured(String.format("%.0f", dcSuccess));
				analyticsData.setFailed(String.format("%.0f", dcFailed));
				analyticsData.setCancelled(String.format("%.0f", dcCancelled));
				analyticsData.setInvalid(String.format("%.0f", dcInvalid));
				analyticsData.setFraud(String.format("%.0f", dcFraud));
				analyticsData.setDropped(String.format("%.0f", dcDropped));
				analyticsData.setRejected(String.format("%.0f", dcRejected));

				analyticsData.setCapturedPercent(String.format("%.2f", (dcSuccess / totalDCTxn) * 100));
				analyticsData.setFailedPercent(String.format("%.2f", (dcFailed / totalDCTxn) * 100));
				analyticsData.setCancelledPercent(String.format("%.2f", (dcCancelled / totalDCTxn) * 100));
				analyticsData.setInvalidPercent(String.format("%.2f", (dcInvalid / totalDCTxn) * 100));
				analyticsData.setFraudPercent(String.format("%.2f", (dcFraud / totalDCTxn) * 100));
				analyticsData.setDroppedPercent(String.format("%.2f", (dcDropped / totalDCTxn) * 100));
				analyticsData.setRejectedPercent(String.format("%.2f", (dcRejected / totalDCTxn) * 100));

			} else if (paymentType.equals(PaymentType.UPI.getCode())) {

				analyticsData.setCaptured(String.format("%.0f", upSuccess));
				analyticsData.setFailed(String.format("%.0f", upFailed));
				analyticsData.setCancelled(String.format("%.0f", upCancelled));
				analyticsData.setInvalid(String.format("%.0f", upInvalid));
				analyticsData.setFraud(String.format("%.0f", upFraud));
				analyticsData.setDropped(String.format("%.0f", upDropped));
				analyticsData.setRejected(String.format("%.0f", upRejected));

				analyticsData.setCapturedPercent(String.format("%.2f", (upSuccess / totalUPTxn) * 100));
				analyticsData.setFailedPercent(String.format("%.2f", (upFailed / totalUPTxn) * 100));
				analyticsData.setCancelledPercent(String.format("%.2f", (upCancelled / totalUPTxn) * 100));
				analyticsData.setInvalidPercent(String.format("%.2f", (upInvalid / totalUPTxn) * 100));
				analyticsData.setFraudPercent(String.format("%.2f", (upFraud / totalUPTxn) * 100));
				analyticsData.setDroppedPercent(String.format("%.2f", (upDropped / totalUPTxn) * 100));
				analyticsData.setRejectedPercent(String.format("%.2f", (upRejected / totalUPTxn) * 100));

			}

			else {

				analyticsData.setCaptured("0");
				analyticsData.setFailed("0");
				analyticsData.setCancelled("0");
				analyticsData.setInvalid("0");
				analyticsData.setFraud("0");
				analyticsData.setDropped("0");
				analyticsData.setRejected("0");

				analyticsData.setCapturedPercent("0.00");
				analyticsData.setFailedPercent("0.00");
				analyticsData.setCancelledPercent("0.00");
				analyticsData.setInvalidPercent("0.00");
				analyticsData.setFraudPercent("0.00");
				analyticsData.setDroppedPercent("0.00");
				analyticsData.setRejectedPercent("0.00");

			}

			analyticsData.setTotalCCTxnAmount(String.format("%.2f", totalCCTxnAmount));
			analyticsData.setTotalDCTxnAmount(String.format("%.2f", totalDCTxnAmount));
			analyticsData.setTotalUPTxnAmount(String.format("%.2f", totalUPTxnAmount));
			analyticsData.setTotalNBTxnAmount(String.format("%.2f", totalNBTxnAmount));
			analyticsData.setTotalCDTxnAmount(String.format("%.2f", totalCDTxnAmount));
			analyticsData.setTotalEMTxnAmount(String.format("%.2f", totalEMTxnAmount));
			analyticsData.setTotalWLTxnAmount(String.format("%.2f", totalWLTxnAmount));

			analyticsData.setTotalCCCapturedCount(String.format("%.0f", ccSuccess));
			analyticsData.setTotalDCCapturedCount(String.format("%.0f", dcSuccess));
			analyticsData.setTotalUPCapturedCount(String.format("%.0f", upSuccess));
			analyticsData.setTotalNBCapturedCount(String.format("%.0f", nbSuccess));
			analyticsData.setTotalEMCapturedCount(String.format("%.0f", emSuccess));
			analyticsData.setTotalCDCapturedCount(String.format("%.0f", cdSuccess));
			analyticsData.setTotalWLCapturedCount(String.format("%.0f", wlSuccess));			

			analyticsData.setUnknownTxnCount(String.valueOf(unknownTransactions));

			// Refund and settled query
			List<BasicDBObject> saleSettleConditionsList = new ArrayList<BasicDBObject>();

			List<BasicDBObject> saleSettledConditionList = new ArrayList<BasicDBObject>();
			saleSettledConditionList
					.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.RECO.getName()));
			saleSettledConditionList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.SETTLED.getName()));

			BasicDBObject saleSettledQuery = new BasicDBObject("$and", saleSettledConditionList);

			saleSettleConditionsList.add(saleSettledQuery);

			List<BasicDBObject> deltaRefundCapturedConditionsList = new ArrayList<BasicDBObject>();

			List<BasicDBObject> deltaRefundCapturedConditionList = new ArrayList<BasicDBObject>();
			deltaRefundCapturedConditionList
					.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.REFUND.getName()));
			deltaRefundCapturedConditionList
					.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.CAPTURED.getName()));
			deltaRefundCapturedConditionList.add(new BasicDBObject(FieldType.UDF6.getName(), Constants.Y.getValue()));

			BasicDBObject deltaRefundQuery = new BasicDBObject("$and", deltaRefundCapturedConditionList);

			deltaRefundCapturedConditionsList.add(deltaRefundQuery);

			List<BasicDBObject> postSettleConditionsList = new ArrayList<BasicDBObject>();

			List<BasicDBObject> postSettleConditionList = new ArrayList<BasicDBObject>();
			postSettleConditionList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName()));
			postSettleConditionList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.CAPTURED.getName()));
			postSettleConditionList
					.add(new BasicDBObject(FieldType.POST_SETTLED_FLAG.getName(), Constants.Y.getValue()));

			BasicDBObject postSettleQuery = new BasicDBObject("$and", postSettleConditionList);

			postSettleConditionsList.add(postSettleQuery);

			BasicDBObject saleSettledConditionQuery = new BasicDBObject("$or", saleSettleConditionsList);
			BasicDBObject deltaRefundConditionQuery = new BasicDBObject("$or", deltaRefundCapturedConditionsList);
			BasicDBObject postSettleConditionQuery = new BasicDBObject("$or", postSettleConditionsList);

			List<BasicDBObject> allConditionQueryList1 = new ArrayList<BasicDBObject>();
			List<BasicDBObject> allConditionQueryList2 = new ArrayList<BasicDBObject>();
			List<BasicDBObject> allConditionQueryList3 = new ArrayList<BasicDBObject>();

			if (!acquirerQuery.isEmpty()) {
				allConditionQueryList1.add(acquirerQuery);
			}
			if (!dateQuery.isEmpty()) {
				allConditionQueryList1.add(dateQuery);
			}
			allConditionQueryList1.add(saleSettledConditionQuery);

			if (!acquirerQuery.isEmpty()) {
				allConditionQueryList2.add(acquirerQuery);
			}
			if (!dateQuery.isEmpty()) {
				allConditionQueryList2.add(dateQuery);
			}
			allConditionQueryList2.add(deltaRefundConditionQuery);

			if (!acquirerQuery.isEmpty()) {
				allConditionQueryList3.add(acquirerQuery);
			}
			if (!dateQuery.isEmpty()) {
				allConditionQueryList3.add(dateQuery);
			}
			allConditionQueryList3.add(postSettleConditionQuery);

			BasicDBObject allConditionQueryObj1 = new BasicDBObject("$and", allConditionQueryList1);
			BasicDBObject allConditionQueryObj2 = new BasicDBObject("$and", allConditionQueryList2);
			BasicDBObject allConditionQueryObj3 = new BasicDBObject("$and", allConditionQueryList3);

			List<BasicDBObject> finalList1 = new ArrayList<BasicDBObject>();
			List<BasicDBObject> finalList2 = new ArrayList<BasicDBObject>();
			List<BasicDBObject> finalList3 = new ArrayList<BasicDBObject>();

			if (!allParamQuery.isEmpty()) {
				finalList1.add(allParamQuery);
			}
			if (!allConditionQueryObj1.isEmpty()) {
				finalList1.add(allConditionQueryObj1);
			}

			if (!allParamQuery.isEmpty()) {
				finalList2.add(allParamQuery);
			}
			if (!allConditionQueryObj2.isEmpty()) {
				finalList2.add(allConditionQueryObj2);
			}

			if (!allParamQuery.isEmpty()) {
				finalList3.add(allParamQuery);
			}
			if (!allConditionQueryObj2.isEmpty()) {
				finalList3.add(allConditionQueryObj3);
			}

			BasicDBObject finalQuery1 = new BasicDBObject("$and", finalList1);
			BasicDBObject finalQuery2 = new BasicDBObject("$and", finalList2);
			BasicDBObject finalQuery3 = new BasicDBObject("$and", finalList3);

			double totalSettled = (double) coll.count(finalQuery1);
			double totalDeltaRefund = (double) coll.count(finalQuery2);
			double totalPostSettled = (double) coll.count(finalQuery3);

			if (totalSettled == 0.0) {
				analyticsData.setMerchantPgRatio("0.00 %");
				analyticsData.setAcquirerPgRatio("0.00 %");
			}

			else {
				double merchantPgRatio = (((totalSettled - totalDeltaRefund) / (totalSettled)) * 100);
				double acquirerPgRatio = (((totalSettled - totalPostSettled) / (totalSettled)) * 100);

				analyticsData.setMerchantPgRatio(String.format("%.2f", merchantPgRatio) + " %");
				analyticsData.setAcquirerPgRatio(String.format("%.2f", acquirerPgRatio) + " %");
			}

			return analyticsData;
		}

		catch (Exception e) {
			logger.error("Exception in transaction summary count service " , e);
		}
		return null;
	}

	public boolean isTxnCapturedForMerchant(String payId, String fromDate, String toDate) {

		logger.info("Checking transactions in last one hour for payId = " + payId);
		try {

			PropertiesManager propManager = new PropertiesManager();

			List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();
			BasicDBObject dateQuery = new BasicDBObject();
			BasicDBObject allParamQuery = new BasicDBObject();
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

			paramConditionLst.add(new BasicDBObject(FieldType.PAY_ID.getName(), payId));
			paramConditionLst.add(new BasicDBObject(FieldType.ORIG_TXNTYPE.getName(), TransactionType.SALE.getCode()));
			paramConditionLst.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.CAPTURED.getName()));

			if (!paramConditionLst.isEmpty()) {
				allParamQuery = new BasicDBObject("$and", paramConditionLst);
			}

			List<BasicDBObject> allConditionQueryList = new ArrayList<BasicDBObject>();

			if (!dateQuery.isEmpty()) {
				allConditionQueryList.add(dateQuery);
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
					.getCollection(propManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));

			double txnCount = coll.count(finalquery);
			logger.info("transaction count in last one hour for payId = " + txnCount);
			if (txnCount > 0) {
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			return false;
		}
	}

}
