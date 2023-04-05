package com.paymentgateway.crm.actionBeans;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.bson.Document;
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
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.mongo.MongoInstance;
import com.paymentgateway.commons.user.AccountCurrencyRegion;
import com.paymentgateway.commons.user.AnalyticsData;
import com.paymentgateway.commons.user.CardHolderType;
import com.paymentgateway.commons.user.Surcharge;
import com.paymentgateway.commons.user.SurchargeDao;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.AcquirerType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.MopType;
import com.paymentgateway.commons.util.PaymentType;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TransactionType;
import com.paymentgateway.commons.util.TxnType;
import com.paymentgateway.crm.action.TdrPojo;

@Service
public class AnalyticsDataService {

	@Autowired
	private MongoInstance mongoInstance;

	@Autowired
	private UserDao userDao;

	@Autowired
	private SurchargeDao surchargeDao;

	@Autowired
	private PropertiesManager propertiesManager;

	private static Logger logger = LoggerFactory.getLogger(AnalyticsDataService.class.getName());
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

	private static final String DECLINED = "Declined";
	private static final String PENDING = "Pending";
	private static final String ACQUIRER_DOWN = "Acquirer Down";
	private static final String FAILED_AT_ACQUIRER = "Failed at Acquirer";
	private static final String ACQUIRER_TIMEOUT = "Acquirer Timeout";
	private static final String BROWSER_CLOSED = "Browser Closed";
	private static final String DUPLICATE = "Duplicate";
	private static final String AUTHENTICATION_FAILED = "Authentication Failed";
	private static final String REJECTED_BY_PG = "Rejected by PG";

	List<Surcharge> surchargeList = new ArrayList<Surcharge>();
	private double postSettledTransactionCount = 0;
	Map<String, User> userMap = new HashMap<String, User>();

	public static List finalList = new ArrayList();

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public AnalyticsData getTransactionCount(String fromDate, String toDate, String payId, String paymentType,
			String acquirer, User user, String param, String txnType, String mopType, String currency) {

		logger.info(" inside getTransactionCount for AnalyticsDataService ");

		try {
			finalList.clear();
			BasicDBObject dateQuery = new BasicDBObject();
			List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();
			BasicDBObject acquirerQuery = new BasicDBObject();
			BasicDBObject mopTypeQuery = new BasicDBObject();

			String saleStatusList = "Captured,Failed,Invalid,Cancelled,Denied due to fraud,Denied by risk,Timeout,Rejected,Declined,Error,Browser Closed,Duplicate,Authentication Failed,Acquirer down,Failed at Acquirer,Timed out at Acquirer,Rejected by PG,Pending";
			String refundStatusList = "Captured,Failed,Invalid,Cancelled,Timeout,Declined,Rejected,Acquirer Down,Failed at Acquirer,Timed out at Acquirer,Pending,Error,Browser Closed,Duplicate,Authentication Failed,Rejected by PG";

			String saleHeader = "Payment Type, Total Txn, Captured(%),Failed(%),Cancelled(%),Invalid(%),Fraud(%),Dropped(%),Rejected(%)";
			String refundHeader = "Payment Type, Total Txn, Captured(%),Failed(%),Declined(%),Invalid(%),Dropped(%),Rejected(%),Pending(%),Acquirer Down(%),Failed At Acquirer(%),Acquirer Timeout(%)";

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
				paramConditionLst.add(dateQuery);
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

			BasicDBObject datIndexQuery = new BasicDBObject("$in", allDatesIndex);
			if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get(Constants.USE_DATE_INDEX.getValue()))
					&& propertiesManager.propertiesMap.get(Constants.USE_DATE_INDEX.getValue()).equalsIgnoreCase("Y")) {
				dateIndexConditionQuery.append(FieldType.DATE_INDEX.getName(), datIndexQuery);
				paramConditionLst.add(dateIndexConditionQuery);
			}

			if (!payId.equalsIgnoreCase("ALL")) {
				BasicDBObject payIdObj = new BasicDBObject(FieldType.PAY_ID.getName(), payId);
				paramConditionLst.add(payIdObj);
			}
			if (!acquirer.equalsIgnoreCase("ALL")) {

				List<String> acquirerList = Arrays.asList(acquirer.split(","));
				BasicDBObject acquirerListQuery = new BasicDBObject("$in", acquirerList);
				acquirerQuery.append(FieldType.ACQUIRER_TYPE.getName(), acquirerListQuery);
				paramConditionLst.add(acquirerQuery);
			}

			if (StringUtils.isNotBlank(mopType) && !mopType.equalsIgnoreCase("ALL")) {

				List<String> mopTypeList = Arrays.asList(mopType.split(","));
				BasicDBObject mopTypeListQuery = new BasicDBObject("$in", mopTypeList);
				mopTypeQuery.append(FieldType.MOP_TYPE.getName(), mopTypeListQuery);
				paramConditionLst.add(mopTypeQuery);
			}

			if (!paymentType.equalsIgnoreCase("ALL")) {
				BasicDBObject paymentTypeObj = new BasicDBObject(FieldType.PAYMENT_TYPE.getName(), paymentType);
				paramConditionLst.add(paymentTypeObj);
			}
			if (StringUtils.isNotBlank(currency) && !currency.equalsIgnoreCase("ALL")) {
				paramConditionLst.add(new BasicDBObject(FieldType.CURRENCY_CODE.getName(), currency));
			}

			BasicDBObject saleConditionQuery = new BasicDBObject(FieldType.ORIG_TXNTYPE.getName(), txnType);
			paramConditionLst.add(saleConditionQuery);

			BasicDBObject finalQuery = new BasicDBObject("$and", paramConditionLst);
			logger.info("final query for performance report " + finalQuery);
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns.getCollection(
					PropertiesManager.propertiesMap.get(prefix + Constants.TRANSACTION_STATUS_COLLECTION.getValue()));

			double ccSuccess = 0;
			double ccFailed = 0;
			double ccInvalid = 0;
			double ccRejected = 0;
			double ccCancelled = 0;
			double ccFraud = 0;
			double ccDropped = 0;
			double ccDeclined = 0;
			double ccPending = 0;
			double ccAcqDown = 0;
			double ccFailedAtAcq = 0;
			double ccAcqTimeOut = 0;

			double dcSuccess = 0;
			double dcFailed = 0;
			double dcInvalid = 0;
			double dcRejected = 0;
			double dcCancelled = 0;
			double dcFraud = 0;
			double dcDropped = 0;
			double dcDeclined = 0;
			double dcPending = 0;
			double dcAcqDown = 0;
			double dcFailedAtAcq = 0;
			double dcAcqTimeOut = 0;

			double upSuccess = 0;
			double upFailed = 0;
			double upInvalid = 0;
			double upRejected = 0;
			double upCancelled = 0;
			double upFraud = 0;
			double upDropped = 0;
			double upDeclined = 0;
			double upPending = 0;
			double upAcqDown = 0;
			double upFailedAtAcq = 0;
			double upAcqTimeOut = 0;

			double nbSuccess = 0;
			double nbFailed = 0;
			double nbInvalid = 0;
			double nbRejected = 0;
			double nbCancelled = 0;
			double nbFraud = 0;
			double nbDropped = 0;
			double nbDeclined = 0;
			double nbPending = 0;
			double nbAcqDown = 0;
			double nbFailedAtAcq = 0;
			double nbAcqTimeOut = 0;

			double wlSuccess = 0;
			double wlFailed = 0;
			double wlInvalid = 0;
			double wlRejected = 0;
			double wlCancelled = 0;
			double wlFraud = 0;
			double wlDropped = 0;
			double wlDeclined = 0;
			double wlPending = 0;
			double wlAcqDown = 0;
			double wlFailedAtAcq = 0;
			double wlAcqTimeOut = 0;

			double cdSuccess = 0;
			double cdFailed = 0;
			double cdInvalid = 0;
			double cdRejected = 0;
			double cdCancelled = 0;
			double cdFraud = 0;
			double cdDropped = 0;
			double cdDeclined = 0;
			double cdPending = 0;
			double cdAcqDown = 0;
			double cdFailedAtAcq = 0;
			double cdAcqTimeOut = 0;

			double emSuccess = 0;
			double emFailed = 0;
			double emInvalid = 0;
			double emRejected = 0;
			double emCancelled = 0;
			double emFraud = 0;
			double emDropped = 0;
			double emDeclined = 0;
			double emPending = 0;
			double emAcqDown = 0;
			double emFailedAtAcq = 0;
			double emAcqTimeOut = 0;

			double totalTxn = 0;
			double totalCCTxn = 0;
			double totalDCTxn = 0;
			double totalUPTxn = 0;
			double totalNBTxn = 0;
			double totalWLTxn = 0;
			double totalEMTxn = 0;
			double totalCDTxn = 0;

			int unknownTransactions = 0;

			int totalIntenationalCaptured = 0;
			int totalDomesticCaptured = 0;

			BigDecimal totalTxnAmount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_DOWN);

			BigDecimal totalCCTxnAmount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_DOWN);
			BigDecimal totalDCTxnAmount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_DOWN);
			BigDecimal totalUPTxnAmount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_DOWN);
			BigDecimal totalNBTxnAmount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_DOWN);
			BigDecimal totalWLTxnAmount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_DOWN);
			BigDecimal totalEMTxnAmount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_DOWN);
			BigDecimal totalCDTxnAmount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_DOWN);

			BasicDBObject projectElement = new BasicDBObject();
			projectElement.put(FieldType.TOTAL_AMOUNT.getName(), 1);
			projectElement.put(FieldType.PAYMENT_TYPE.getName(), 1);
			projectElement.put(FieldType.PAYMENTS_REGION.getName(), 1);
			projectElement.put(FieldType.TXNTYPE.getName(), 1);
			projectElement.put(FieldType.ORIG_TXNTYPE.getName(), 1);
			projectElement.put(FieldType.STATUS.getName(), 1);
			projectElement.put(FieldType.IS_ENCRYPTED.getName(), 1);
			
			BasicDBObject project = new BasicDBObject("$project", projectElement);

			BasicDBObject match = new BasicDBObject("$match", finalQuery);
			List<BasicDBObject> pipeline = Arrays.asList(match, project);
			AggregateIterable<Document> output = coll.aggregate(pipeline);
			output.allowDiskUse(true);
			MongoCursor<Document> cursor = output.iterator();

			// MongoCursor<Document> cursor = coll.find(finalQuery).iterator();

			// Remove all data from an earlier map
			while (cursor.hasNext()) {
			
				Document dbobj = cursor.next();
				if (StringUtils.isNotBlank(dbobj.getString(FieldType.ORIG_TXNTYPE.getName()))
						&& TxnType.SALE.getName().equalsIgnoreCase(dbobj.getString(FieldType.ORIG_TXNTYPE.getName()))) {

					if (StringUtils.isNotBlank(dbobj.getString(FieldType.STATUS.toString()))
							&& saleStatusList.contains(dbobj.getString(FieldType.STATUS.toString()))) {
						totalTxn++;
					}
					if (StringUtils.isNotBlank(dbobj.getString(FieldType.STATUS.toString()))
							&& !saleStatusList.contains(dbobj.getString(FieldType.STATUS.toString()))) {
						logger.info("SALE_STATUS_NOT_FOUND_FOR : " + dbobj.getString(FieldType.STATUS.toString()));
					}
				} else if (StringUtils.isNotBlank(dbobj.getString(FieldType.ORIG_TXNTYPE.getName())) && TxnType.REFUND
						.getName().equalsIgnoreCase(dbobj.getString(FieldType.ORIG_TXNTYPE.getName()))) {
					if (StringUtils.isNotBlank(dbobj.getString(FieldType.STATUS.toString()))
							&& refundStatusList.contains(dbobj.getString(FieldType.STATUS.toString()))) {
						totalTxn++;
					}
					if (StringUtils.isNotBlank(dbobj.getString(FieldType.STATUS.toString()))
							&& !refundStatusList.contains(dbobj.getString(FieldType.STATUS.toString()))) {
						logger.info("REFUND_STATUS_NOT_FOUND_FOR : " + dbobj.getString(FieldType.STATUS.toString()));
					}

				}
			
				if (txnType.equalsIgnoreCase(TxnType.SALE.getName())) {

					if (StringUtils.isBlank(dbobj.getString(FieldType.PAYMENT_TYPE.toString()))
							&& StringUtils.isNotBlank(dbobj.getString(FieldType.STATUS.toString()))
							&& saleStatusList.contains(dbobj.getString(FieldType.STATUS.toString()))) {
						unknownTransactions++;
						continue;
					}

				} else if (StringUtils.isBlank(dbobj.getString(FieldType.PAYMENT_TYPE.toString()))
						&& StringUtils.isNotBlank(dbobj.getString(FieldType.STATUS.toString()))
						&& refundStatusList.contains(dbobj.getString(FieldType.STATUS.toString()))) {
					unknownTransactions++;
					continue;
				}

				if (StringUtils
						.isBlank(PaymentType.getpaymentName(dbobj.getString(FieldType.PAYMENT_TYPE.toString())))) {
					continue;
				}

				switch (PaymentType.getInstanceUsingCode(dbobj.getString(FieldType.PAYMENT_TYPE.toString()))) {

				case DEBIT_CARD:
					if (TxnType.SALE.getName().equalsIgnoreCase(dbobj.getString(FieldType.TXNTYPE.getName()))) {
						if (StringUtils.isNotBlank(dbobj.getString(FieldType.STATUS.toString()))
								&& saleStatusList.contains(dbobj.getString(FieldType.STATUS.toString()))) {
							totalDCTxn++;
						}
					} else if (TxnType.REFUND.getName()
							.equalsIgnoreCase(dbobj.getString(FieldType.TXNTYPE.getName()))) {
						if (StringUtils.isNotBlank(dbobj.getString(FieldType.STATUS.toString()))
								&& refundStatusList.contains(dbobj.getString(FieldType.STATUS.getName()))) {
							totalDCTxn++;
						}
					}
					if (StringUtils.isNotBlank(dbobj.getString(FieldType.STATUS.toString()))) {
						switch (dbobj.getString(FieldType.STATUS.toString())) {

						case CAPTURED:

							if (!StringUtils.isBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))) {
								BigDecimal txnAmount = new BigDecimal(
										dbobj.getString(FieldType.TOTAL_AMOUNT.toString())).setScale(2,
												RoundingMode.HALF_DOWN);
								totalTxnAmount = totalTxnAmount.add(txnAmount).setScale(2, RoundingMode.HALF_DOWN);
								totalDCTxnAmount = totalDCTxnAmount.add(txnAmount).setScale(2, RoundingMode.HALF_DOWN);
								if (StringUtils.isNotBlank(dbobj.getString(FieldType.PAYMENTS_REGION.toString()))) {
									if (dbobj.getString(FieldType.PAYMENTS_REGION.toString())
											.equalsIgnoreCase(AccountCurrencyRegion.INTERNATIONAL.name())) {
										totalIntenationalCaptured++;
									} else if (dbobj.getString(FieldType.PAYMENTS_REGION.toString())
											.equalsIgnoreCase(AccountCurrencyRegion.DOMESTIC.name())) {
										totalDomesticCaptured++;
									}
								} else {
									totalDomesticCaptured++;
								}
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
							dcFraud++;
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

						case DECLINED:
							if (txnType.equalsIgnoreCase(TxnType.SALE.getName())) {
								dcRejected++;
							} else {
								dcDeclined++;
							}
							break;

						case ACQUIRER_DOWN:
							if (txnType.equalsIgnoreCase(TxnType.SALE.getName())) {
								dcFailed++;
							} else {
								dcAcqDown++;
							}

							break;

						case FAILED_AT_ACQUIRER:
							if (txnType.equalsIgnoreCase(TxnType.SALE.getName())) {
								dcFailed++;
							} else {
								dcFailedAtAcq++;
							}

							break;

						case ACQUIRER_TIMEOUT:
							if (txnType.equalsIgnoreCase(TxnType.SALE.getName())) {
								dcDropped++;
							} else {
								dcAcqTimeOut++;
							}
							break;

						case BROWSER_CLOSED:
							dcCancelled++;
							break;

						case DUPLICATE:
							dcInvalid++;
							break;

						case REJECTED_BY_PG:
							dcRejected++;
							break;

						case AUTHENTICATION_FAILED:
							dcRejected++;
							break;

						case PENDING:
							dcPending++;
							break;

						default:
							break;
						}
					}
					break;

				case CREDIT_CARD:
					if (TxnType.SALE.getName().equalsIgnoreCase(dbobj.getString(FieldType.TXNTYPE.getName()))) {
						if (StringUtils.isNotBlank(dbobj.getString(FieldType.STATUS.toString()))
								&& saleStatusList.contains(dbobj.getString(FieldType.STATUS.toString()))) {
							totalCCTxn++;
						}
					} else if (TxnType.REFUND.getName()
							.equalsIgnoreCase(dbobj.getString(FieldType.TXNTYPE.getName()))) {
						if (StringUtils.isNotBlank(dbobj.getString(FieldType.STATUS.toString()))
								&& refundStatusList.contains(dbobj.getString(FieldType.STATUS.getName()))) {
							totalCCTxn++;
						}
					}

					if (StringUtils.isNotBlank(dbobj.getString(FieldType.STATUS.toString()))) {
						switch (dbobj.getString(FieldType.STATUS.toString())) {

						case CAPTURED:

							if (!StringUtils.isBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))) {
								BigDecimal txnAmount = new BigDecimal(
										dbobj.getString(FieldType.TOTAL_AMOUNT.toString())).setScale(2,
												RoundingMode.HALF_DOWN);
								totalTxnAmount = totalTxnAmount.add(txnAmount).setScale(2, RoundingMode.HALF_DOWN);
								totalCCTxnAmount = totalCCTxnAmount.add(txnAmount).setScale(2, RoundingMode.HALF_DOWN);
								if (StringUtils.isNotBlank(dbobj.getString(FieldType.PAYMENTS_REGION.toString()))) {
									if (dbobj.getString(FieldType.PAYMENTS_REGION.toString())
											.equalsIgnoreCase(AccountCurrencyRegion.INTERNATIONAL.name())) {
										totalIntenationalCaptured++;
									} else if (dbobj.getString(FieldType.PAYMENTS_REGION.toString())
											.equalsIgnoreCase(AccountCurrencyRegion.DOMESTIC.name())) {
										totalDomesticCaptured++;
									}
								} else {
									totalDomesticCaptured++;
								}
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
							ccFraud++;
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

						case DECLINED:
							if (txnType.equalsIgnoreCase(TxnType.SALE.getName())) {
								ccRejected++;
							} else {
								ccDeclined++;
							}
							break;

						case ACQUIRER_DOWN:
							if (txnType.equalsIgnoreCase(TxnType.SALE.getName())) {
								ccFailed++;
							} else {
								ccAcqDown++;
							}

							break;

						case FAILED_AT_ACQUIRER:
							if (txnType.equalsIgnoreCase(TxnType.SALE.getName())) {
								ccFailed++;
							} else {
								ccFailedAtAcq++;
							}

							break;

						case ACQUIRER_TIMEOUT:
							if (txnType.equalsIgnoreCase(TxnType.SALE.getName())) {
								ccDropped++;
							} else {
								ccAcqTimeOut++;
							}
							break;

						case BROWSER_CLOSED:
							ccCancelled++;
							break;

						case DUPLICATE:
							ccInvalid++;
							break;

						case REJECTED_BY_PG:
							ccRejected++;
							break;

						case AUTHENTICATION_FAILED:
							ccRejected++;
							break;

						case PENDING:
							ccPending++;
							break;

						default:
							break;
						}
					}
					break;

				case UPI:
					if (TxnType.SALE.getName().equalsIgnoreCase(dbobj.getString(FieldType.TXNTYPE.getName()))) {
						if (StringUtils.isNotBlank(dbobj.getString(FieldType.STATUS.toString()))
								&& saleStatusList.contains(dbobj.getString(FieldType.STATUS.toString()))) {
							totalUPTxn++;
						}
					} else if (TxnType.REFUND.getName()
							.equalsIgnoreCase(dbobj.getString(FieldType.TXNTYPE.getName()))) {
						if (StringUtils.isNotBlank(dbobj.getString(FieldType.STATUS.toString()))
								&& refundStatusList.contains(dbobj.getString(FieldType.STATUS.getName()))) {
							totalUPTxn++;
						}
					}

					if (StringUtils.isNotBlank(dbobj.getString(FieldType.STATUS.toString()))) {
						switch (dbobj.getString(FieldType.STATUS.toString())) {

						case CAPTURED:

							if (!StringUtils.isBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))) {
								BigDecimal txnAmount = new BigDecimal(
										dbobj.getString(FieldType.TOTAL_AMOUNT.toString())).setScale(2,
												RoundingMode.HALF_DOWN);
								totalTxnAmount = totalTxnAmount.add(txnAmount).setScale(2, RoundingMode.HALF_DOWN);
								totalUPTxnAmount = totalUPTxnAmount.add(txnAmount).setScale(2, RoundingMode.HALF_DOWN);
								if (StringUtils.isNotBlank(dbobj.getString(FieldType.PAYMENTS_REGION.toString()))) {
									if (dbobj.getString(FieldType.PAYMENTS_REGION.toString())
											.equalsIgnoreCase(AccountCurrencyRegion.INTERNATIONAL.name())) {
										totalIntenationalCaptured++;
									} else if (dbobj.getString(FieldType.PAYMENTS_REGION.toString())
											.equalsIgnoreCase(AccountCurrencyRegion.DOMESTIC.name())) {
										totalDomesticCaptured++;
									}
								} else {
									totalDomesticCaptured++;
								}
							
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
							upFraud++;
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

						case DECLINED:
							if (txnType.equalsIgnoreCase(TxnType.SALE.getName())) {
								upRejected++;
							} else {
								upDeclined++;
							}
							break;

						case ACQUIRER_DOWN:
							if (txnType.equalsIgnoreCase(TxnType.SALE.getName())) {
								upFailed++;
							} else {
								upAcqDown++;
							}

							break;

						case FAILED_AT_ACQUIRER:
							if (txnType.equalsIgnoreCase(TxnType.SALE.getName())) {
								upFailed++;
							} else {
								upFailedAtAcq++;
							}

							break;

						case ACQUIRER_TIMEOUT:
							if (txnType.equalsIgnoreCase(TxnType.SALE.getName())) {
								upDropped++;
							} else {
								upAcqTimeOut++;
							}
							break;

						case BROWSER_CLOSED:
							upCancelled++;
							break;

						case DUPLICATE:
							upInvalid++;
							break;

						case REJECTED_BY_PG:
							upRejected++;
							break;

						case AUTHENTICATION_FAILED:
							upRejected++;
							break;

						case PENDING:
							upPending++;
							break;

						default:
							break;
						}
					}
					break;

				case NET_BANKING:
					if (TxnType.SALE.getName().equalsIgnoreCase(dbobj.getString(FieldType.TXNTYPE.getName()))) {
						if (StringUtils.isNotBlank(dbobj.getString(FieldType.STATUS.toString()))
								&& saleStatusList.contains(dbobj.getString(FieldType.STATUS.toString()))) {
							totalNBTxn++;
						}
					} else if (TxnType.REFUND.getName()
							.equalsIgnoreCase(dbobj.getString(FieldType.TXNTYPE.getName()))) {
						if (StringUtils.isNotBlank(dbobj.getString(FieldType.STATUS.toString()))
								&& refundStatusList.contains(dbobj.getString(FieldType.STATUS.getName()))) {
							totalNBTxn++;
						}
					}

					if (StringUtils.isNotBlank(dbobj.getString(FieldType.STATUS.toString()))) {
						switch (dbobj.getString(FieldType.STATUS.toString())) {

						case CAPTURED:
							if (!StringUtils.isBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))) {
								BigDecimal txnAmount = new BigDecimal(
										dbobj.getString(FieldType.TOTAL_AMOUNT.toString())).setScale(2,
												RoundingMode.HALF_DOWN);
								totalTxnAmount = totalTxnAmount.add(txnAmount).setScale(2, RoundingMode.HALF_DOWN);
								totalNBTxnAmount = totalNBTxnAmount.add(txnAmount).setScale(2, RoundingMode.HALF_DOWN);
								if (StringUtils.isNotBlank(dbobj.getString(FieldType.PAYMENTS_REGION.toString()))) {
									if (dbobj.getString(FieldType.PAYMENTS_REGION.toString())
											.equalsIgnoreCase(AccountCurrencyRegion.INTERNATIONAL.name())) {
										totalIntenationalCaptured++;
									} else if (dbobj.getString(FieldType.PAYMENTS_REGION.toString())
											.equalsIgnoreCase(AccountCurrencyRegion.DOMESTIC.name())) {
										totalDomesticCaptured++;
									}
								} else {
									totalDomesticCaptured++;
								}
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
							nbFraud++;
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

						case DECLINED:
							if (txnType.equalsIgnoreCase(TxnType.SALE.getName())) {
								nbRejected++;
							} else {
								nbDeclined++;
							}
							break;

						case ACQUIRER_DOWN:
							if (txnType.equalsIgnoreCase(TxnType.SALE.getName())) {
								nbFailed++;
							} else {
								nbAcqDown++;
							}

							break;

						case FAILED_AT_ACQUIRER:
							if (txnType.equalsIgnoreCase(TxnType.SALE.getName())) {
								nbFailed++;
							} else {
								nbFailedAtAcq++;
							}

							break;

						case ACQUIRER_TIMEOUT:
							if (txnType.equalsIgnoreCase(TxnType.SALE.getName())) {
								nbDropped++;
							} else {
								nbAcqTimeOut++;
							}
							break;

						case BROWSER_CLOSED:
							nbCancelled++;
							break;

						case DUPLICATE:
							nbInvalid++;
							break;

						case REJECTED_BY_PG:
							nbRejected++;
							break;

						case AUTHENTICATION_FAILED:
							nbRejected++;
							break;

						case PENDING:
							nbPending++;
							break;

						default:
							break;
						}
					}
					break;

				case WALLET:
					if (TxnType.SALE.getName().equalsIgnoreCase(dbobj.getString(FieldType.TXNTYPE.getName()))) {
						if (StringUtils.isNotBlank(dbobj.getString(FieldType.STATUS.toString()))
								&& saleStatusList.contains(dbobj.getString(FieldType.STATUS.toString()))) {
							totalWLTxn++;
						}
					} else if (TxnType.REFUND.getName()
							.equalsIgnoreCase(dbobj.getString(FieldType.TXNTYPE.getName()))) {
						if (StringUtils.isNotBlank(dbobj.getString(FieldType.STATUS.toString()))
								&& refundStatusList.contains(dbobj.getString(FieldType.STATUS.getName()))) {
							totalWLTxn++;
						}
					}

					if (StringUtils.isNotBlank(dbobj.getString(FieldType.STATUS.toString()))) {
						switch (dbobj.getString(FieldType.STATUS.toString())) {

						case CAPTURED:
							if (!StringUtils.isBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))) {
								BigDecimal txnAmount = new BigDecimal(
										dbobj.getString(FieldType.TOTAL_AMOUNT.toString())).setScale(2,
												RoundingMode.HALF_DOWN);
								totalTxnAmount = totalTxnAmount.add(txnAmount).setScale(2, RoundingMode.HALF_DOWN);
								totalWLTxnAmount = totalWLTxnAmount.add(txnAmount).setScale(2, RoundingMode.HALF_DOWN);
								if (StringUtils.isNotBlank(dbobj.getString(FieldType.PAYMENTS_REGION.toString()))) {
									if (dbobj.getString(FieldType.PAYMENTS_REGION.toString())
											.equalsIgnoreCase(AccountCurrencyRegion.INTERNATIONAL.name())) {
										totalIntenationalCaptured++;
									} else if (dbobj.getString(FieldType.PAYMENTS_REGION.toString())
											.equalsIgnoreCase(AccountCurrencyRegion.DOMESTIC.name())) {
										totalDomesticCaptured++;
									}
								} else {
									totalDomesticCaptured++;
								}
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
							wlFraud++;
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

						case DECLINED:
							if (txnType.equalsIgnoreCase(TxnType.SALE.getName())) {
								wlRejected++;
							} else {
								wlDeclined++;
							}
							break;

						case ACQUIRER_DOWN:
							if (txnType.equalsIgnoreCase(TxnType.SALE.getName())) {
								wlFailed++;
							} else {
								wlAcqDown++;
							}

							break;

						case FAILED_AT_ACQUIRER:
							if (txnType.equalsIgnoreCase(TxnType.SALE.getName())) {
								wlFailed++;
							} else {
								wlFailedAtAcq++;
							}

							break;

						case ACQUIRER_TIMEOUT:
							if (txnType.equalsIgnoreCase(TxnType.SALE.getName())) {
								wlDropped++;
							} else {
								wlAcqTimeOut++;
							}
							break;

						case BROWSER_CLOSED:
							wlCancelled++;
							break;

						case DUPLICATE:
							wlInvalid++;
							break;

						case REJECTED_BY_PG:
							wlRejected++;
							break;

						case AUTHENTICATION_FAILED:
							wlRejected++;
							break;

						case PENDING:
							wlPending++;
							break;

						default:
							break;
						}
					}
					break;

				case EMI:
					if (TxnType.SALE.getName().equalsIgnoreCase(dbobj.getString(FieldType.TXNTYPE.getName()))) {
						if (StringUtils.isNotBlank(dbobj.getString(FieldType.STATUS.toString()))
								&& saleStatusList.contains(dbobj.getString(FieldType.STATUS.toString()))) {
							totalEMTxn++;
						}
					} else if (TxnType.REFUND.getName()
							.equalsIgnoreCase(dbobj.getString(FieldType.TXNTYPE.getName()))) {
						if (StringUtils.isNotBlank(dbobj.getString(FieldType.STATUS.toString()))
								&& refundStatusList.contains(dbobj.getString(FieldType.STATUS.getName()))) {
							totalEMTxn++;
						}
					}

					if (StringUtils.isNotBlank(dbobj.getString(FieldType.STATUS.toString()))) {
						switch (dbobj.getString(FieldType.STATUS.toString())) {

						case CAPTURED:
							if (!StringUtils.isBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))) {
								BigDecimal txnAmount = new BigDecimal(
										dbobj.getString(FieldType.TOTAL_AMOUNT.toString())).setScale(2,
												RoundingMode.HALF_DOWN);
								totalTxnAmount = totalTxnAmount.add(txnAmount).setScale(2, RoundingMode.HALF_DOWN);
								totalEMTxnAmount = totalEMTxnAmount.add(txnAmount).setScale(2, RoundingMode.HALF_DOWN);
								if (StringUtils.isNotBlank(dbobj.getString(FieldType.PAYMENTS_REGION.toString()))) {
									if (dbobj.getString(FieldType.PAYMENTS_REGION.toString())
											.equalsIgnoreCase(AccountCurrencyRegion.INTERNATIONAL.name())) {
										totalIntenationalCaptured++;
									} else if (dbobj.getString(FieldType.PAYMENTS_REGION.toString())
											.equalsIgnoreCase(AccountCurrencyRegion.DOMESTIC.name())) {
										totalDomesticCaptured++;
									}
								} else {
									totalDomesticCaptured++;
								}
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
							emFraud++;
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

						case DECLINED:
							if (txnType.equalsIgnoreCase(TxnType.SALE.getName())) {
								emRejected++;
							} else {
								emDeclined++;
							}
							break;

						case ACQUIRER_DOWN:
							if (txnType.equalsIgnoreCase(TxnType.SALE.getName())) {
								emFailed++;
							} else {
								emAcqDown++;
							}

							break;

						case FAILED_AT_ACQUIRER:
							if (txnType.equalsIgnoreCase(TxnType.SALE.getName())) {
								emFailed++;
							} else {
								emFailedAtAcq++;
							}

							break;

						case ACQUIRER_TIMEOUT:
							if (txnType.equalsIgnoreCase(TxnType.SALE.getName())) {
								emDropped++;
							} else {
								emAcqTimeOut++;
							}
							break;

						case BROWSER_CLOSED:
							emCancelled++;
							break;

						case DUPLICATE:
							emInvalid++;
							break;

						case REJECTED_BY_PG:
							emRejected++;
							break;

						case AUTHENTICATION_FAILED:
							emRejected++;
							break;

						case PENDING:
							emPending++;
							break;

						default:
							break;
						}
					}
					break;

				case COD:
					if (TxnType.SALE.getName().equalsIgnoreCase(dbobj.getString(FieldType.TXNTYPE.getName()))) {
						if (StringUtils.isNotBlank(dbobj.getString(FieldType.STATUS.toString()))
								&& saleStatusList.contains(dbobj.getString(FieldType.STATUS.toString()))) {
							totalCDTxn++;
						}
					} else if (TxnType.REFUND.getName()
							.equalsIgnoreCase(dbobj.getString(FieldType.TXNTYPE.getName()))) {
						if (StringUtils.isNotBlank(dbobj.getString(FieldType.STATUS.toString()))
								&& refundStatusList.contains(dbobj.getString(FieldType.STATUS.getName()))) {
							totalCDTxn++;
						}
					}

					if (StringUtils.isNotBlank(dbobj.getString(FieldType.STATUS.toString()))) {
						switch (dbobj.getString(FieldType.STATUS.toString())) {

						case CAPTURED:
							if (!StringUtils.isBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))) {
								BigDecimal txnAmount = new BigDecimal(
										dbobj.getString(FieldType.TOTAL_AMOUNT.toString())).setScale(2,
												RoundingMode.HALF_DOWN);
								totalTxnAmount = totalTxnAmount.add(txnAmount).setScale(2, RoundingMode.HALF_DOWN);
								totalCDTxnAmount = totalCDTxnAmount.add(txnAmount).setScale(2, RoundingMode.HALF_DOWN);
								if (StringUtils.isNotBlank(dbobj.getString(FieldType.PAYMENTS_REGION.toString()))) {
									if (dbobj.getString(FieldType.PAYMENTS_REGION.toString())
											.equalsIgnoreCase(AccountCurrencyRegion.INTERNATIONAL.name())) {
										totalIntenationalCaptured++;
									} else if (dbobj.getString(FieldType.PAYMENTS_REGION.toString())
											.equalsIgnoreCase(AccountCurrencyRegion.DOMESTIC.name())) {
										totalDomesticCaptured++;
									}
								} else {
									totalDomesticCaptured++;
								}
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
							cdFraud++;
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

						case DECLINED:
							if (txnType.equalsIgnoreCase(TxnType.SALE.getName())) {
								cdRejected++;
							} else {
								cdDeclined++;
							}
							break;

						case ACQUIRER_DOWN:
							if (txnType.equalsIgnoreCase(TxnType.SALE.getName())) {
								cdFailed++;
							} else {
								cdAcqDown++;
							}

							break;

						case FAILED_AT_ACQUIRER:
							if (txnType.equalsIgnoreCase(TxnType.SALE.getName())) {
								cdFailed++;
							} else {
								cdFailedAtAcq++;
							}

							break;

						case ACQUIRER_TIMEOUT:
							if (txnType.equalsIgnoreCase(TxnType.SALE.getName())) {
								cdDropped++;
							} else {
								cdAcqTimeOut++;
							}
							break;

						case BROWSER_CLOSED:
							cdCancelled++;
							break;

						case DUPLICATE:
							cdInvalid++;
							break;

						case REJECTED_BY_PG:
							cdRejected++;
							break;

						case AUTHENTICATION_FAILED:
							cdRejected++;
							break;

						case PENDING:
							cdPending++;
							break;

						default:
							break;
						}
					}
					break;

				default:
					break;
				}
			}

			cursor.close();

			logger.info("Stating Calculation");

			// totalTxnAmount = totalTxnAmount.setScale(2,
			// RoundingMode.HALF_DOWN);
			AnalyticsData analyticsData = new AnalyticsData();
			List performanceList = new ArrayList();
			List<StringBuilder> ccList = new ArrayList<StringBuilder>();
			List<StringBuilder> dcList = new ArrayList<StringBuilder>();
			List<StringBuilder> upList = new ArrayList<StringBuilder>();
			List<StringBuilder> nbList = new ArrayList<StringBuilder>();
			List<StringBuilder> wlList = new ArrayList<StringBuilder>();
			List<StringBuilder> emList = new ArrayList<StringBuilder>();
			List<StringBuilder> cdList = new ArrayList<StringBuilder>();

			List<String> ccData = new ArrayList<String>();
			List<String> dcData = new ArrayList<String>();
			List<String> upData = new ArrayList<String>();
			List<String> nbData = new ArrayList<String>();
			List<String> wlData = new ArrayList<String>();
			List<String> emData = new ArrayList<String>();
			List<String> cdData = new ArrayList<String>();
			List<String> naData = new ArrayList<String>();

			/*
			 * StringBuilder ccData = new StringBuilder(); StringBuilder dcData = new
			 * StringBuilder(); StringBuilder upData = new StringBuilder(); StringBuilder
			 * nbData = new StringBuilder(); StringBuilder wlData = new StringBuilder();
			 * StringBuilder emData = new StringBuilder(); StringBuilder cdData = new
			 * StringBuilder();
			 */

			double totalTxnSuccess = ccSuccess + dcSuccess + upSuccess + nbSuccess + cdSuccess + emSuccess + wlSuccess;
			double totalFailed = ccFailed + dcFailed + upFailed + nbFailed + cdFailed + emFailed + wlFailed;
			double totalRejected = ccRejected + dcRejected + upRejected + nbRejected + cdRejected + emRejected
					+ wlRejected + ccAcqDown + dcAcqDown + upAcqDown + nbAcqDown + cdAcqDown + emAcqDown + wlAcqDown
					+ ccFailedAtAcq + dcFailedAtAcq + upFailedAtAcq + nbFailedAtAcq + cdFailedAtAcq + emFailedAtAcq
					+ wlFailedAtAcq + ccAcqTimeOut + dcAcqTimeOut + upAcqTimeOut + nbAcqTimeOut + cdAcqTimeOut
					+ emAcqTimeOut + wlAcqTimeOut + ccDeclined + dcDeclined + upDeclined + nbDeclined + cdDeclined
					+ emDeclined + wlDeclined + ccPending + dcPending + upPending + nbPending + cdPending + emPending
					+ wlPending;

			analyticsData.setTotalTxnCount(String.format("%.0f", totalTxn));
			analyticsData.setSuccessTxnCount(String.format("%.0f", totalTxnSuccess));
			analyticsData.setFailedTxnCount(String.format("%.2f", totalFailed));
			analyticsData.setTotalIntenationalCapturedCount(String.valueOf(totalIntenationalCaptured));
			analyticsData.setTotalDomesticCapturedCount(String.valueOf(totalDomesticCaptured));

			if (totalTxn == 0.00) {
				analyticsData.setSuccessTxnPercent("0.00");
				analyticsData.setTotalIntenationalCapturedPercentage("0.00");
				analyticsData.setTotalDomesticCapturedPercentage("0.00");
			} else {
				double successTxnPercent = (totalTxnSuccess / totalTxn) * 100;
				analyticsData.setSuccessTxnPercent(String.format("%.2f", successTxnPercent));
				double totalIntenationCapturedPercent = (totalIntenationalCaptured / totalTxnSuccess) * 100;
				double totalDomesticCapturedPercent = (totalDomesticCaptured / totalTxnSuccess) * 100;
				analyticsData
						.setTotalIntenationalCapturedPercentage(String.format("%.2f", totalIntenationCapturedPercent));
				analyticsData.setTotalDomesticCapturedPercentage(String.format("%.2f", totalDomesticCapturedPercent));
			}
			if (totalDomesticCaptured == 0) {
				analyticsData.setTotalDomesticCapturedPercentage("0.00");
			}
			if (totalIntenationalCaptured == 0) {
				analyticsData.setTotalIntenationalCapturedPercentage("0.00");
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
				double emTxnPercent = (emSuccess / totalTxnSuccess) * 100;
				double cdTxnPercent = (cdSuccess / totalTxnSuccess) * 100;

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

			if (nbSuccess + nbFailed < 1) {
				analyticsData.setNBSuccessRate("0.00");
			} else {
				double nBSuccessRate = (nbSuccess / (nbSuccess + nbFailed)) * 100;
				analyticsData.setNBSuccessRate(String.format("%.2f", nBSuccessRate));
			}

			if (wlSuccess + wlFailed < 1) {
				analyticsData.setWLSuccessRate("0.00");
			} else {
				double wLSuccessRate = (wlSuccess / (wlSuccess + wlFailed)) * 100;
				analyticsData.setWLSuccessRate(String.format("%.2f", wLSuccessRate));
			}

			if (emSuccess + emFailed < 1) {
				analyticsData.setEMSuccessRate("0.00");
			} else {
				double eMSuccessRate = (emSuccess / (emSuccess + emFailed)) * 100;
				analyticsData.setEMSuccessRate(String.format("%.2f", eMSuccessRate));
			}

			if (cdSuccess + cdFailed < 1) {
				analyticsData.setCDSuccessRate("0.00");
			} else {
				double cDSuccessRate = (cdSuccess / (cdSuccess + cdFailed)) * 100;
				analyticsData.setCDSuccessRate(String.format("%.2f", cDSuccessRate));
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
				analyticsData.setTotalCCDeclinedTxnPercent("0.00");
				analyticsData.setTotalCCPendingTxnPercent("0.00");
				analyticsData.setTotalCCAcqDownTxnPercent("0.00");
				analyticsData.setTotalCCFailedAtAcqTxnPercent("0.00");
				analyticsData.setTotalCCAcqTimeOutTxnPercent("0.00");

				if (txnType.equalsIgnoreCase(TxnType.SALE.getName())) {
					ccData.add("0");
					ccData.add("0.00");
					ccData.add("0.00");
					ccData.add("0.00");
					ccData.add("0.00");
					ccData.add("0.00");
					ccData.add("0.00");
					ccData.add("0.00");
				} else if (txnType.equalsIgnoreCase(TxnType.REFUND.getName())) {
					ccData.add("0");
					ccData.add("0.00");
					ccData.add("0.00");
					ccData.add("0.00");
					ccData.add("0.00");
					ccData.add("0.00");
					ccData.add("0.00");
					ccData.add("0.00");
					ccData.add("0.00");
					ccData.add("0.00");
					ccData.add("0.00");
				}

				/*
				 * ccData.append("totalTxn").append(":").append("0.00").append(",").append(
				 * "captured").append(":").append("0.00").append(",")
				 * .append("failed").append(":").append("0.00").append(",").append("cancelled").
				 * append(":").append("0.00").append(",").append("invalid").append(":").append(
				 * "0.00")
				 * .append(",").append("fraud").append(":").append("0.00").append(",").append(
				 * "droped").append(":").append("0.00").append(",").append("rejected").append(
				 * ":")
				 * .append("0.00").append(",").append("declined").append(":").append("0.00").
				 * append(",").append("pending").append(":").append("0.00").append(",")
				 * .append("acqDown").append(":").append("0.00").append(",").append(
				 * "failedAtAcq").append(":").append("0.00").append(",").append("acqTimeout").
				 * append(":").append("0.00");
				 */

			} else {

				double totalCCSuccessTxnPercent = (ccSuccess / (totalCCTxn)) * 100;
				double totalCCFailedTxnPercent = (ccFailed / (totalCCTxn)) * 100;
				double totalCCCancelledTxnPercent = (ccCancelled / (totalCCTxn)) * 100;
				double totalCCInvalidTxnPercent = (ccInvalid / (totalCCTxn)) * 100;
				double totalCCFraudTxnPercent = (ccFraud / (totalCCTxn)) * 100;
				double totalCCDroppedTxnPercent = (ccDropped / (totalCCTxn)) * 100;
				double totalCCRejectedTxnPercent = (ccRejected / (totalCCTxn)) * 100;
				double totalCCDeclinedTxnPercent = (ccDeclined / (totalCCTxn)) * 100;
				double totalCCPendingTxnPercent = (ccPending / (totalCCTxn)) * 100;
				double totalCCAcqDownTxnPercent = (ccAcqDown / (totalCCTxn)) * 100;
				double totalCCFailedAtAcqTxnPercent = (ccFailedAtAcq / (totalCCTxn)) * 100;
				double totalCCAcqTimeOutTxnPercent = (ccAcqTimeOut / (totalCCTxn)) * 100;

				analyticsData.setTotalCCTxn(String.format("%.0f", totalCCTxn));
				analyticsData.setTotalCCSuccessTxnPercent(String.format("%.2f", totalCCSuccessTxnPercent));
				analyticsData.setTotalCCFailedTxnPercent(String.format("%.2f", totalCCFailedTxnPercent));
				analyticsData.setTotalCCCancelledTxnPercent(String.format("%.2f", totalCCCancelledTxnPercent));
				analyticsData.setTotalCCInvalidTxnPercent(String.format("%.2f", totalCCInvalidTxnPercent));
				analyticsData.setTotalCCFraudTxnPercent(String.format("%.2f", totalCCFraudTxnPercent));
				analyticsData.setTotalCCDroppedTxnPercent(String.format("%.2f", totalCCDroppedTxnPercent));
				analyticsData.setTotalCCRejectedTxnPercent(String.format("%.2f", totalCCRejectedTxnPercent));
				analyticsData.setTotalCCDeclinedTxnPercent(String.format("%.2f", totalCCDeclinedTxnPercent));
				analyticsData.setTotalCCPendingTxnPercent(String.format("%.2f", totalCCPendingTxnPercent));
				analyticsData.setTotalCCAcqDownTxnPercent(String.format("%.2f", totalCCAcqDownTxnPercent));
				analyticsData.setTotalCCFailedAtAcqTxnPercent(String.format("%.2f", totalCCFailedAtAcqTxnPercent));
				analyticsData.setTotalCCAcqTimeOutTxnPercent(String.format("%.2f", totalCCAcqTimeOutTxnPercent));

				if (txnType.equalsIgnoreCase(TxnType.SALE.getName())) {
					ccData.add(String.format("%.0f", totalCCTxn));
					ccData.add(String.format("%.2f", totalCCSuccessTxnPercent));
					ccData.add(String.format("%.2f", totalCCFailedTxnPercent));
					ccData.add(String.format("%.2f", totalCCCancelledTxnPercent));
					ccData.add(String.format("%.2f", totalCCInvalidTxnPercent));
					ccData.add(String.format("%.2f", totalCCFraudTxnPercent));
					ccData.add(String.format("%.2f", totalCCDroppedTxnPercent));
					ccData.add(String.format("%.2f", totalCCRejectedTxnPercent));
				} else if (txnType.equalsIgnoreCase(TxnType.REFUND.getName())) {
					ccData.add(String.format("%.0f", totalCCTxn));
					ccData.add(String.format("%.2f", totalCCSuccessTxnPercent));
					ccData.add(String.format("%.2f", totalCCFailedTxnPercent));
					ccData.add(String.format("%.2f", totalCCDeclinedTxnPercent));
					ccData.add(String.format("%.2f", totalCCInvalidTxnPercent));
					ccData.add(String.format("%.2f", totalCCDroppedTxnPercent));
					ccData.add(String.format("%.2f", totalCCRejectedTxnPercent));
					ccData.add(String.format("%.2f", totalCCPendingTxnPercent));
					ccData.add(String.format("%.2f", totalCCAcqDownTxnPercent));
					ccData.add(String.format("%.2f", totalCCFailedAtAcqTxnPercent));
					ccData.add(String.format("%.2f", totalCCAcqTimeOutTxnPercent));
				}
				/*
				 * ccData.append("totalTxn").append(":").append(totalCCTxn).append(",").append(
				 * "captured").append(":").append(String.format("%.2f",totalCCSuccessTxnPercent)
				 * ) .append(",").append("failed").append(":").append(String.format("%.2f",
				 * totalCCFailedTxnPercent))
				 * .append(",").append("cancelled").append(":").append(String.format("%.2f",
				 * totalCCCancelledTxnPercent)).append(",").append("invalid").append(":")
				 * .append(String.format("%.2f",
				 * totalCCInvalidTxnPercent)).append(",").append("fraud").append(":")
				 * .append(String.format("%.2f",
				 * totalCCFraudTxnPercent)).append(",").append("dropped").append(":").append(
				 * String.format("%.2f", totalCCDroppedTxnPercent)).append(",")
				 * .append("rejected").append(":").append(String.format("%.2f",
				 * totalCCRejectedTxnPercent)).append(",").append("declined").append(":")
				 * .append(String.format("%.2f",
				 * totalCCDeclinedTxnPercent)).append(",").append("pending").append(":")
				 * .append(String.format("%.2f",
				 * totalCCPendingTxnPercent)).append(",").append("acqDown").append(":").append(
				 * String.format("%.2f", totalCCAcqDownTxnPercent)).append(",")
				 * .append("failedAtAcq").append(":").append(String.format("%.2f",
				 * totalCCFailedAtAcqTxnPercent)).append(",").append("acqTimeout").append(":")
				 * .append(String.format("%.2f", totalCCAcqTimeOutTxnPercent));
				 */

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
				analyticsData.setTotalDCDeclinedTxnPercent("0.00");
				analyticsData.setTotalDCPendingTxnPercent("0.00");
				analyticsData.setTotalDCAcqDownTxnPercent("0.00");
				analyticsData.setTotalDCFailedAtAcqTxnPercent("0.00");
				analyticsData.setTotalDCAcqTimeOutTxnPercent("0.00");

				if (txnType.equalsIgnoreCase(TxnType.SALE.getName())) {
					dcData.add("0");
					dcData.add("0.00");
					dcData.add("0.00");
					dcData.add("0.00");
					dcData.add("0.00");
					dcData.add("0.00");
					dcData.add("0.00");
					dcData.add("0.00");
				} else if (txnType.equalsIgnoreCase(TxnType.REFUND.getName())) {
					dcData.add("0");
					dcData.add("0.00");
					dcData.add("0.00");
					dcData.add("0.00");
					dcData.add("0.00");
					dcData.add("0.00");
					dcData.add("0.00");
					dcData.add("0.00");
					dcData.add("0.00");
					dcData.add("0.00");
					dcData.add("0.00");
				}
				/*
				 * dcData.append("0.00").append("0.00").append("0.00").append("0.00").append(
				 * "0.00")
				 * .append("0.00").append("0.00").append("0.00").append("0.00").append("0.00")
				 * .append("0.00").append("0.00").append("0.00");
				 */

			} else {

				double totalDCSuccessTxnPercent = (dcSuccess / (totalDCTxn)) * 100;
				double totalDCFailedTxnPercent = (dcFailed / (totalDCTxn)) * 100;
				double totalDCCancelledTxnPercent = (dcCancelled / (totalDCTxn)) * 100;
				double totalDCInvalidTxnPercent = (dcInvalid / (totalDCTxn)) * 100;
				double totalDCFraudTxnPercent = (dcFraud / (totalDCTxn)) * 100;
				double totalDCDroppedTxnPercent = (dcDropped / (totalDCTxn)) * 100;
				double totalDCRejectedTxnPercent = (dcRejected / (totalDCTxn)) * 100;
				double totalDCDeclinedTxnPercent = (dcDeclined / (totalDCTxn)) * 100;
				double totalDCPendingTxnPercent = (dcPending / (totalDCTxn)) * 100;
				double totalDCAcqDownTxnPercent = (dcAcqDown / (totalDCTxn)) * 100;
				double totalDCFailedAtAcqTxnPercent = (dcFailedAtAcq / (totalDCTxn)) * 100;
				double totalDCAcqTimeOutTxnPercent = (dcAcqTimeOut / (totalDCTxn)) * 100;
				if (txnType.equalsIgnoreCase(TxnType.SALE.getName())) {
					dcData.add(String.format("%.0f", totalDCTxn));
					dcData.add(String.format("%.2f", totalDCSuccessTxnPercent));
					dcData.add(String.format("%.2f", totalDCFailedTxnPercent));
					dcData.add(String.format("%.2f", totalDCCancelledTxnPercent));
					dcData.add(String.format("%.2f", totalDCInvalidTxnPercent));
					dcData.add(String.format("%.2f", totalDCFraudTxnPercent));
					dcData.add(String.format("%.2f", totalDCDroppedTxnPercent));
					dcData.add(String.format("%.2f", totalDCRejectedTxnPercent));
				} else if (txnType.equalsIgnoreCase(TxnType.REFUND.getName())) {
					dcData.add(String.format("%.0f", totalDCTxn));
					dcData.add(String.format("%.2f", totalDCSuccessTxnPercent));
					dcData.add(String.format("%.2f", totalDCFailedTxnPercent));
					dcData.add(String.format("%.2f", totalDCDeclinedTxnPercent));
					dcData.add(String.format("%.2f", totalDCInvalidTxnPercent));
					dcData.add(String.format("%.2f", totalDCDroppedTxnPercent));
					dcData.add(String.format("%.2f", totalDCRejectedTxnPercent));
					dcData.add(String.format("%.2f", totalDCPendingTxnPercent));
					dcData.add(String.format("%.2f", totalDCAcqDownTxnPercent));
					dcData.add(String.format("%.2f", totalDCFailedAtAcqTxnPercent));
					dcData.add(String.format("%.2f", totalDCAcqTimeOutTxnPercent));
				}
				/*
				 * dcData.append(totalDCTxn).append(",").append(String.format("%.2f",
				 * totalDCSuccessTxnPercent)).append(",").append(String.format("%.2f",
				 * totalDCFailedTxnPercent)) .append(",").append(String.format("%.2f",
				 * totalDCCancelledTxnPercent)).append(",").append(String.format("%.2f",
				 * totalDCInvalidTxnPercent)).append(",") .append(String.format("%.2f",
				 * totalDCFraudTxnPercent)).append(",").append(String.format("%.2f",
				 * totalDCDroppedTxnPercent)).append(",") .append(String.format("%.2f",
				 * totalDCRejectedTxnPercent)).append(",").append(String.format("%.2f",
				 * totalDCDeclinedTxnPercent)).append(",") .append(String.format("%.2f",
				 * totalDCPendingTxnPercent)).append(",").append(String.format("%.2f",
				 * totalDCAcqDownTxnPercent)).append(",") .append(String.format("%.2f",
				 * totalDCFailedAtAcqTxnPercent)).append(",").append(String.format("%.2f",
				 * totalDCAcqTimeOutTxnPercent));
				 */

				analyticsData.setTotalDCTxn(String.format("%.0f", totalDCTxn));
				analyticsData.setTotalDCSuccessTxnPercent(String.format("%.2f", totalDCSuccessTxnPercent));
				analyticsData.setTotalDCFailedTxnPercent(String.format("%.2f", totalDCFailedTxnPercent));
				analyticsData.setTotalDCCancelledTxnPercent(String.format("%.2f", totalDCCancelledTxnPercent));
				analyticsData.setTotalDCInvalidTxnPercent(String.format("%.2f", totalDCInvalidTxnPercent));
				analyticsData.setTotalDCFraudTxnPercent(String.format("%.2f", totalDCFraudTxnPercent));
				analyticsData.setTotalDCDroppedTxnPercent(String.format("%.2f", totalDCDroppedTxnPercent));
				analyticsData.setTotalDCRejectedTxnPercent(String.format("%.2f", totalDCRejectedTxnPercent));
				analyticsData.setTotalDCDeclinedTxnPercent(String.format("%.2f", totalDCDeclinedTxnPercent));
				analyticsData.setTotalDCPendingTxnPercent(String.format("%.2f", totalDCPendingTxnPercent));
				analyticsData.setTotalDCAcqDownTxnPercent(String.format("%.2f", totalDCAcqDownTxnPercent));
				analyticsData.setTotalDCFailedAtAcqTxnPercent(String.format("%.2f", totalDCFailedAtAcqTxnPercent));
				analyticsData.setTotalDCAcqTimeOutTxnPercent(String.format("%.2f", totalDCAcqTimeOutTxnPercent));

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
				analyticsData.setTotalUPDeclinedTxnPercent("0.00");
				analyticsData.setTotalUPPendingTxnPercent("0.00");
				analyticsData.setTotalUPAcqDownTxnPercent("0.00");
				analyticsData.setTotalUPFailedAtAcqTxnPercent("0.00");
				analyticsData.setTotalUPAcqTimeOutTxnPercent("0.00");

				if (txnType.equalsIgnoreCase(TxnType.SALE.getName())) {
					upData.add("0");
					upData.add("0.00");
					upData.add("0.00");
					upData.add("0.00");
					upData.add("0.00");
					upData.add("0.00");
					upData.add("0.00");
					upData.add("0.00");
				} else if (txnType.equalsIgnoreCase(TxnType.REFUND.getName())) {
					upData.add("0");
					upData.add("0.00");
					upData.add("0.00");
					upData.add("0.00");
					upData.add("0.00");
					upData.add("0.00");
					upData.add("0.00");
					upData.add("0.00");
					upData.add("0.00");
					upData.add("0.00");
					upData.add("0.00");
				}
				/*
				 * upData.append("0.00").append("0.00").append("0.00").append("0.00").append(
				 * "0.00")
				 * .append("0.00").append("0.00").append("0.00").append("0.00").append("0.00")
				 * .append("0.00").append("0.00").append("0.00");
				 */

			} else {

				analyticsData.setTotalUPTxn(String.format("%.0f", totalUPTxn));

				double totalUPSuccessTxnPercent = (upSuccess / (totalUPTxn)) * 100;
				double totalUPFailedTxnPercent = (upFailed / (totalUPTxn)) * 100;
				double totalUPCancelledTxnPercent = (upCancelled / (totalUPTxn)) * 100;
				double totalUPInvalidTxnPercent = (upInvalid / (totalUPTxn)) * 100;
				double totalUPFraudTxnPercent = (upFraud / (totalUPTxn)) * 100;
				double totalUPDroppedTxnPercent = (upDropped / (totalUPTxn)) * 100;
				double totalUPRejectedTxnPercent = (upRejected / (totalUPTxn)) * 100;
				double totalUPDeclinedTxnPercent = (upDeclined / (totalUPTxn)) * 100;
				double totalUPPendingTxnPercent = (upPending / (totalUPTxn)) * 100;
				double totalUPAcqDownTxnPercent = (upAcqDown / (totalUPTxn)) * 100;
				double totalUPFailedAtAcqTxnPercent = (upFailedAtAcq / (totalUPTxn)) * 100;
				double totalUPAcqTimeOutTxnPercent = (upAcqTimeOut / (totalUPTxn)) * 100;

				if (txnType.equalsIgnoreCase(TxnType.SALE.getName())) {
					upData.add(String.format("%.0f", totalUPTxn));
					upData.add(String.format("%.2f", totalUPSuccessTxnPercent));
					upData.add(String.format("%.2f", totalUPFailedTxnPercent));
					upData.add(String.format("%.2f", totalUPCancelledTxnPercent));
					upData.add(String.format("%.2f", totalUPInvalidTxnPercent));
					upData.add(String.format("%.2f", totalUPFraudTxnPercent));
					upData.add(String.format("%.2f", totalUPDroppedTxnPercent));
					upData.add(String.format("%.2f", totalUPRejectedTxnPercent));
				} else if (txnType.equalsIgnoreCase(TxnType.REFUND.getName())) {
					upData.add(String.format("%.0f", totalUPTxn));
					upData.add(String.format("%.2f", totalUPSuccessTxnPercent));
					upData.add(String.format("%.2f", totalUPFailedTxnPercent));
					upData.add(String.format("%.2f", totalUPDeclinedTxnPercent));
					upData.add(String.format("%.2f", totalUPInvalidTxnPercent));
					upData.add(String.format("%.2f", totalUPDroppedTxnPercent));
					upData.add(String.format("%.2f", totalUPRejectedTxnPercent));
					upData.add(String.format("%.2f", totalUPPendingTxnPercent));
					upData.add(String.format("%.2f", totalUPAcqDownTxnPercent));
					upData.add(String.format("%.2f", totalUPFailedAtAcqTxnPercent));
					upData.add(String.format("%.2f", totalUPAcqTimeOutTxnPercent));
				}
				/*
				 * upData.append(totalUPTxn).append(",").append(String.format("%.2f",
				 * totalUPSuccessTxnPercent)).append(",").append(String.format("%.2f",
				 * totalUPFailedTxnPercent)) .append(",").append(String.format("%.2f",
				 * totalUPCancelledTxnPercent)).append(",").append(String.format("%.2f",
				 * totalUPInvalidTxnPercent)).append(",") .append(String.format("%.2f",
				 * totalUPFraudTxnPercent)).append(",").append(String.format("%.2f",
				 * totalUPDroppedTxnPercent)).append(",") .append(String.format("%.2f",
				 * totalUPRejectedTxnPercent)).append(",").append(String.format("%.2f",
				 * totalUPDeclinedTxnPercent)).append(",") .append(String.format("%.2f",
				 * totalUPPendingTxnPercent)).append(",").append(String.format("%.2f",
				 * totalUPAcqDownTxnPercent)).append(",") .append(String.format("%.2f",
				 * totalUPFailedAtAcqTxnPercent)).append(",").append(String.format("%.2f",
				 * totalUPAcqTimeOutTxnPercent));
				 */

				analyticsData.setTotalUPSuccessTxnPercent(String.format("%.2f", totalUPSuccessTxnPercent));
				analyticsData.setTotalUPFailedTxnPercent(String.format("%.2f", totalUPFailedTxnPercent));
				analyticsData.setTotalUPCancelledTxnPercent(String.format("%.2f", totalUPCancelledTxnPercent));
				analyticsData.setTotalUPInvalidTxnPercent(String.format("%.2f", totalUPInvalidTxnPercent));
				analyticsData.setTotalUPFraudTxnPercent(String.format("%.2f", totalUPFraudTxnPercent));
				analyticsData.setTotalUPDroppedTxnPercent(String.format("%.2f", totalUPDroppedTxnPercent));
				analyticsData.setTotalUPRejectedTxnPercent(String.format("%.2f", totalUPRejectedTxnPercent));
				analyticsData.setTotalUPDeclinedTxnPercent(String.format("%.2f", totalUPDeclinedTxnPercent));
				analyticsData.setTotalUPPendingTxnPercent(String.format("%.2f", totalUPPendingTxnPercent));
				analyticsData.setTotalUPAcqDownTxnPercent(String.format("%.2f", totalUPAcqDownTxnPercent));
				analyticsData.setTotalUPFailedAtAcqTxnPercent(String.format("%.2f", totalUPFailedAtAcqTxnPercent));
				analyticsData.setTotalUPAcqTimeOutTxnPercent(String.format("%.2f", totalUPAcqTimeOutTxnPercent));

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
				analyticsData.setTotalNBDeclinedTxnPercent("0.00");
				analyticsData.setTotalNBPendingTxnPercent("0.00");
				analyticsData.setTotalNBAcqDownTxnPercent("0.00");
				analyticsData.setTotalNBFailedAtAcqTxnPercent("0.00");
				analyticsData.setTotalNBAcqTimeOutTxnPercent("0.00");

				if (txnType.equalsIgnoreCase(TxnType.SALE.getName())) {
					nbData.add("0");
					nbData.add("0.00");
					nbData.add("0.00");
					nbData.add("0.00");
					nbData.add("0.00");
					nbData.add("0.00");
					nbData.add("0.00");
					nbData.add("0.00");
				} else if (txnType.equalsIgnoreCase(TxnType.REFUND.getName())) {
					nbData.add("0");
					nbData.add("0.00");
					nbData.add("0.00");
					nbData.add("0.00");
					nbData.add("0.00");
					nbData.add("0.00");
					nbData.add("0.00");
					nbData.add("0.00");
					nbData.add("0.00");
					nbData.add("0.00");
					nbData.add("0.00");
				}
				/*
				 * nbData.append("0.00").append("0.00").append("0.00").append("0.00").append(
				 * "0.00")
				 * .append("0.00").append("0.00").append("0.00").append("0.00").append("0.00")
				 * .append("0.00").append("0.00").append("0.00");
				 */

			} else {
				double totalNBSuccessTxnPercent = (nbSuccess / (totalNBTxn)) * 100;
				double totalNBFailedTxnPercent = (nbFailed / (totalNBTxn)) * 100;
				double totalNBCancelledTxnPercent = (nbCancelled / (totalNBTxn)) * 100;
				double totalNBInvalidTxnPercent = (nbInvalid / (totalNBTxn)) * 100;
				double totalNBFraudTxnPercent = (nbFraud / (totalNBTxn)) * 100;
				double totalNBDroppedTxnPercent = (nbDropped / (totalNBTxn)) * 100;
				double totalNBRejectedTxnPercent = (nbRejected / (totalNBTxn)) * 100;
				double totalNBDeclinedTxnPercent = (nbDeclined / (totalNBTxn)) * 100;
				double totalNBPendingTxnPercent = (nbPending / (totalNBTxn)) * 100;
				double totalNBAcqDownTxnPercent = (nbAcqDown / (totalNBTxn)) * 100;
				double totalNBFailedAtAcqTxnPercent = (nbFailedAtAcq / (totalNBTxn)) * 100;
				double totalNBAcqTimeOutTxnPercent = (nbAcqTimeOut / (totalNBTxn)) * 100;

				if (txnType.equalsIgnoreCase(TxnType.SALE.getName())) {
					nbData.add(String.format("%.0f", totalNBTxn));
					nbData.add(String.format("%.2f", totalNBSuccessTxnPercent));
					nbData.add(String.format("%.2f", totalNBFailedTxnPercent));
					nbData.add(String.format("%.2f", totalNBCancelledTxnPercent));
					nbData.add(String.format("%.2f", totalNBInvalidTxnPercent));
					nbData.add(String.format("%.2f", totalNBFraudTxnPercent));
					nbData.add(String.format("%.2f", totalNBDroppedTxnPercent));
					nbData.add(String.format("%.2f", totalNBRejectedTxnPercent));
				} else if (txnType.equalsIgnoreCase(TxnType.REFUND.getName())) {
					nbData.add(String.format("%.0f", totalNBTxn));
					nbData.add(String.format("%.2f", totalNBSuccessTxnPercent));
					nbData.add(String.format("%.2f", totalNBFailedTxnPercent));
					nbData.add(String.format("%.2f", totalNBDeclinedTxnPercent));
					nbData.add(String.format("%.2f", totalNBInvalidTxnPercent));
					nbData.add(String.format("%.2f", totalNBDroppedTxnPercent));
					nbData.add(String.format("%.2f", totalNBRejectedTxnPercent));
					nbData.add(String.format("%.2f", totalNBPendingTxnPercent));
					nbData.add(String.format("%.2f", totalNBAcqDownTxnPercent));
					nbData.add(String.format("%.2f", totalNBFailedAtAcqTxnPercent));
					nbData.add(String.format("%.2f", totalNBAcqTimeOutTxnPercent));

				}
				/*
				 * nbData.append(totalNBTxn).append(",").append(String.format("%.2f",
				 * totalNBSuccessTxnPercent)).append(",").append(String.format("%.2f",
				 * totalNBFailedTxnPercent)) .append(",").append(String.format("%.2f",
				 * totalNBCancelledTxnPercent)).append(",").append(String.format("%.2f",
				 * totalNBInvalidTxnPercent)).append(",") .append(String.format("%.2f",
				 * totalNBFraudTxnPercent)).append(",").append(String.format("%.2f",
				 * totalNBDroppedTxnPercent)).append(",") .append(String.format("%.2f",
				 * totalNBRejectedTxnPercent)).append(",").append(String.format("%.2f",
				 * totalNBDeclinedTxnPercent)).append(",") .append(String.format("%.2f",
				 * totalNBPendingTxnPercent)).append(",").append(String.format("%.2f",
				 * totalNBAcqDownTxnPercent)).append(",") .append(String.format("%.2f",
				 * totalNBFailedAtAcqTxnPercent)).append(",").append(String.format("%.2f",
				 * totalNBAcqTimeOutTxnPercent));
				 */

				analyticsData.setTotalNBTxn(String.format("%.0f", totalNBTxn));
				analyticsData.setTotalNBSuccessTxnPercent(String.format("%.2f", totalNBSuccessTxnPercent));
				analyticsData.setTotalNBFailedTxnPercent(String.format("%.2f", totalNBFailedTxnPercent));
				analyticsData.setTotalNBCancelledTxnPercent(String.format("%.2f", totalNBCancelledTxnPercent));
				analyticsData.setTotalNBInvalidTxnPercent(String.format("%.2f", totalNBInvalidTxnPercent));
				analyticsData.setTotalNBFraudTxnPercent(String.format("%.2f", totalNBFraudTxnPercent));
				analyticsData.setTotalNBDroppedTxnPercent(String.format("%.2f", totalNBDroppedTxnPercent));
				analyticsData.setTotalNBRejectedTxnPercent(String.format("%.2f", totalNBRejectedTxnPercent));
				analyticsData.setTotalNBDeclinedTxnPercent(String.format("%.2f", totalNBDeclinedTxnPercent));
				analyticsData.setTotalNBPendingTxnPercent(String.format("%.2f", totalNBPendingTxnPercent));
				analyticsData.setTotalNBAcqDownTxnPercent(String.format("%.2f", totalNBAcqDownTxnPercent));
				analyticsData.setTotalNBFailedAtAcqTxnPercent(String.format("%.2f", totalNBFailedAtAcqTxnPercent));
				analyticsData.setTotalNBAcqTimeOutTxnPercent(String.format("%.2f", totalNBAcqTimeOutTxnPercent));

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
				analyticsData.setTotalWLDeclinedTxnPercent("0.00");
				analyticsData.setTotalWLPendingTxnPercent("0.00");
				analyticsData.setTotalWLAcqDownTxnPercent("0.00");
				analyticsData.setTotalWLFailedAtAcqTxnPercent("0.00");
				analyticsData.setTotalWLAcqTimeOutTxnPercent("0.00");

				if (txnType.equalsIgnoreCase(TxnType.SALE.getName())) {
					wlData.add("0");
					wlData.add("0.00");
					wlData.add("0.00");
					wlData.add("0.00");
					wlData.add("0.00");
					wlData.add("0.00");
					wlData.add("0.00");
					wlData.add("0.00");
				} else if (txnType.equalsIgnoreCase(TxnType.REFUND.getName())) {
					wlData.add("0");
					wlData.add("0.00");
					wlData.add("0.00");
					wlData.add("0.00");
					wlData.add("0.00");
					wlData.add("0.00");
					wlData.add("0.00");
					wlData.add("0.00");
					wlData.add("0.00");
					wlData.add("0.00");
					wlData.add("0.00");
				}
				/*
				 * wlData.append("0.00").append("0.00").append("0.00").append("0.00").append(
				 * "0.00")
				 * .append("0.00").append("0.00").append("0.00").append("0.00").append("0.00")
				 * .append("0.00").append("0.00").append("0.00");
				 */

			} else {

				double totalWLSuccessTxnPercent = (wlSuccess / (totalWLTxn)) * 100;
				double totalWLFailedTxnPercent = (wlFailed / (totalWLTxn)) * 100;
				double totalWLCancelledTxnPercent = (wlCancelled / (totalWLTxn)) * 100;
				double totalWLInvalidTxnPercent = (wlInvalid / (totalWLTxn)) * 100;
				double totalWLFraudTxnPercent = (wlFraud / (totalWLTxn)) * 100;
				double totalWLDroppedTxnPercent = (wlDropped / (totalWLTxn)) * 100;
				double totalWLRejectedTxnPercent = (wlRejected / (totalWLTxn)) * 100;
				double totalWLDeclinedTxnPercent = (wlDeclined / (totalWLTxn)) * 100;
				double totalWLPendingTxnPercent = (wlPending / (totalWLTxn)) * 100;
				double totalWLAcqDownTxnPercent = (wlAcqDown / (totalWLTxn)) * 100;
				double totalWLFailedAtAcqTxnPercent = (wlFailedAtAcq / (totalWLTxn)) * 100;
				double totalWLAcqTimeOutTxnPercent = (wlAcqTimeOut / (totalWLTxn)) * 100;

				if (txnType.equalsIgnoreCase(TxnType.SALE.getName())) {
					wlData.add(String.format("%.0f", totalWLTxn));
					wlData.add(String.format("%.2f", totalWLSuccessTxnPercent));
					wlData.add(String.format("%.2f", totalWLFailedTxnPercent));
					wlData.add(String.format("%.2f", totalWLCancelledTxnPercent));
					wlData.add(String.format("%.2f", totalWLInvalidTxnPercent));
					wlData.add(String.format("%.2f", totalWLFraudTxnPercent));
					wlData.add(String.format("%.2f", totalWLDroppedTxnPercent));
					wlData.add(String.format("%.2f", totalWLRejectedTxnPercent));
				} else if (txnType.equalsIgnoreCase(TxnType.REFUND.getName())) {
					wlData.add(String.format("%.0f", totalWLTxn));
					wlData.add(String.format("%.2f", totalWLSuccessTxnPercent));
					wlData.add(String.format("%.2f", totalWLFailedTxnPercent));
					wlData.add(String.format("%.2f", totalWLDeclinedTxnPercent));
					wlData.add(String.format("%.2f", totalWLInvalidTxnPercent));
					wlData.add(String.format("%.2f", totalWLDroppedTxnPercent));
					wlData.add(String.format("%.2f", totalWLRejectedTxnPercent));
					wlData.add(String.format("%.2f", totalWLPendingTxnPercent));
					wlData.add(String.format("%.2f", totalWLAcqDownTxnPercent));
					wlData.add(String.format("%.2f", totalWLFailedAtAcqTxnPercent));
					wlData.add(String.format("%.2f", totalWLAcqTimeOutTxnPercent));

				}
				/*
				 * wlData.append(totalWLTxn).append(",").append(String.format("%.2f",
				 * totalWLSuccessTxnPercent)).append(",").append(String.format("%.2f",
				 * totalWLFailedTxnPercent)) .append(",").append(String.format("%.2f",
				 * totalWLCancelledTxnPercent)).append(",").append(String.format("%.2f",
				 * totalWLInvalidTxnPercent)).append(",") .append(String.format("%.2f",
				 * totalWLFraudTxnPercent)).append(",").append(String.format("%.2f",
				 * totalWLDroppedTxnPercent)).append(",") .append(String.format("%.2f",
				 * totalWLRejectedTxnPercent)).append(",").append(String.format("%.2f",
				 * totalWLDeclinedTxnPercent)).append(",") .append(String.format("%.2f",
				 * totalWLPendingTxnPercent)).append(",").append(String.format("%.2f",
				 * totalWLAcqDownTxnPercent)).append(",") .append(String.format("%.2f",
				 * totalWLFailedAtAcqTxnPercent)).append(",").append(String.format("%.2f",
				 * totalWLAcqTimeOutTxnPercent));
				 */

				analyticsData.setTotalWLTxn(String.format("%.0f", totalWLTxn));
				analyticsData.setTotalWLSuccessTxnPercent(String.format("%.2f", totalWLSuccessTxnPercent));
				analyticsData.setTotalWLFailedTxnPercent(String.format("%.2f", totalWLFailedTxnPercent));
				analyticsData.setTotalWLCancelledTxnPercent(String.format("%.2f", totalWLCancelledTxnPercent));
				analyticsData.setTotalWLInvalidTxnPercent(String.format("%.2f", totalWLInvalidTxnPercent));
				analyticsData.setTotalWLFraudTxnPercent(String.format("%.2f", totalWLFraudTxnPercent));
				analyticsData.setTotalWLDroppedTxnPercent(String.format("%.2f", totalWLDroppedTxnPercent));
				analyticsData.setTotalWLRejectedTxnPercent(String.format("%.2f", totalWLRejectedTxnPercent));
				analyticsData.setTotalWLDeclinedTxnPercent(String.format("%.2f", totalWLDeclinedTxnPercent));
				analyticsData.setTotalWLPendingTxnPercent(String.format("%.2f", totalWLPendingTxnPercent));
				analyticsData.setTotalWLAcqDownTxnPercent(String.format("%.2f", totalWLAcqDownTxnPercent));
				analyticsData.setTotalWLFailedAtAcqTxnPercent(String.format("%.2f", totalWLFailedAtAcqTxnPercent));
				analyticsData.setTotalWLAcqTimeOutTxnPercent(String.format("%.2f", totalWLAcqTimeOutTxnPercent));

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
				analyticsData.setTotalEMDeclinedTxnPercent("0.00");
				analyticsData.setTotalEMPendingTxnPercent("0.00");
				analyticsData.setTotalEMAcqDownTxnPercent("0.00");
				analyticsData.setTotalEMFailedAtAcqTxnPercent("0.00");
				analyticsData.setTotalEMAcqTimeOutTxnPercent("0.00");

				if (txnType.equalsIgnoreCase(TxnType.SALE.getName())) {
					emData.add("0");
					emData.add("0.00");
					emData.add("0.00");
					emData.add("0.00");
					emData.add("0.00");
					emData.add("0.00");
					emData.add("0.00");
					emData.add("0.00");
				} else if (txnType.equalsIgnoreCase(TxnType.REFUND.getName())) {
					emData.add("0");
					emData.add("0.00");
					emData.add("0.00");
					emData.add("0.00");
					emData.add("0.00");
					emData.add("0.00");
					emData.add("0.00");
					emData.add("0.00");
					emData.add("0.00");
					emData.add("0.00");
					emData.add("0.00");
				}
				/*
				 * emData.append("0.00").append("0.00").append("0.00").append("0.00").append(
				 * "0.00")
				 * .append("0.00").append("0.00").append("0.00").append("0.00").append("0.00")
				 * .append("0.00").append("0.00").append("0.00");
				 */

			} else {

				double totalEMSuccessTxnPercent = (emSuccess / (totalEMTxn)) * 100;
				double totalEMFailedTxnPercent = (emFailed / (totalEMTxn)) * 100;
				double totalEMCancelledTxnPercent = (emCancelled / (totalEMTxn)) * 100;
				double totalEMInvalidTxnPercent = (emInvalid / (totalEMTxn)) * 100;
				double totalEMFraudTxnPercent = (emFraud / (totalEMTxn)) * 100;
				double totalEMDroppedTxnPercent = (emDropped / (totalEMTxn)) * 100;
				double totalEMRejectedTxnPercent = (emRejected / (totalEMTxn)) * 100;
				double totalEMDeclinedTxnPercent = (emDeclined / (totalEMTxn)) * 100;
				double totalEMPendingTxnPercent = (emPending / (totalEMTxn)) * 100;
				double totalEMAcqDownTxnPercent = (emAcqDown / (totalEMTxn)) * 100;
				double totalEMFailedAtAcqTxnPercent = (emFailedAtAcq / (totalEMTxn)) * 100;
				double totalEMAcqTimeOutTxnPercent = (emAcqTimeOut / (totalEMTxn)) * 100;

				if (txnType.equalsIgnoreCase(TxnType.SALE.getName())) {
					emData.add(String.format("%.0f", totalEMTxn));
					emData.add(String.format("%.2f", totalEMSuccessTxnPercent));
					emData.add(String.format("%.2f", totalEMFailedTxnPercent));
					emData.add(String.format("%.2f", totalEMCancelledTxnPercent));
					emData.add(String.format("%.2f", totalEMInvalidTxnPercent));
					emData.add(String.format("%.2f", totalEMFraudTxnPercent));
					emData.add(String.format("%.2f", totalEMDroppedTxnPercent));
					emData.add(String.format("%.2f", totalEMRejectedTxnPercent));
				} else if (txnType.equalsIgnoreCase(TxnType.REFUND.getName())) {

					emData.add(String.format("%.0f", totalEMTxn));
					emData.add(String.format("%.2f", totalEMSuccessTxnPercent));
					emData.add(String.format("%.2f", totalEMFailedTxnPercent));
					emData.add(String.format("%.2f", totalEMDeclinedTxnPercent));
					emData.add(String.format("%.2f", totalEMInvalidTxnPercent));
					emData.add(String.format("%.2f", totalEMDroppedTxnPercent));
					emData.add(String.format("%.2f", totalEMRejectedTxnPercent));
					emData.add(String.format("%.2f", totalEMPendingTxnPercent));
					emData.add(String.format("%.2f", totalEMAcqDownTxnPercent));
					emData.add(String.format("%.2f", totalEMFailedAtAcqTxnPercent));
					emData.add(String.format("%.2f", totalEMAcqTimeOutTxnPercent));
				}
				/*
				 * emData.append(totalEMTxn).append(",").append(String.format("%.2f",
				 * totalEMSuccessTxnPercent)).append(",").append(String.format("%.2f",
				 * totalEMFailedTxnPercent)) .append(",").append(String.format("%.2f",
				 * totalEMCancelledTxnPercent)).append(",").append(String.format("%.2f",
				 * totalEMInvalidTxnPercent)).append(",") .append(String.format("%.2f",
				 * totalEMFraudTxnPercent)).append(",").append(String.format("%.2f",
				 * totalEMDroppedTxnPercent)).append(",") .append(String.format("%.2f",
				 * totalEMRejectedTxnPercent)).append(",").append(String.format("%.2f",
				 * totalEMDeclinedTxnPercent)).append(",") .append(String.format("%.2f",
				 * totalEMPendingTxnPercent)).append(",").append(String.format("%.2f",
				 * totalEMAcqDownTxnPercent)).append(",") .append(String.format("%.2f",
				 * totalEMFailedAtAcqTxnPercent)).append(",").append(String.format("%.2f",
				 * totalEMAcqTimeOutTxnPercent));
				 */

				analyticsData.setTotalEMTxn(String.format("%.0f", totalEMTxn));
				analyticsData.setTotalEMSuccessTxnPercent(String.format("%.2f", totalEMSuccessTxnPercent));
				analyticsData.setTotalEMFailedTxnPercent(String.format("%.2f", totalEMFailedTxnPercent));
				analyticsData.setTotalEMCancelledTxnPercent(String.format("%.2f", totalEMCancelledTxnPercent));
				analyticsData.setTotalEMInvalidTxnPercent(String.format("%.2f", totalEMInvalidTxnPercent));
				analyticsData.setTotalEMFraudTxnPercent(String.format("%.2f", totalEMFraudTxnPercent));
				analyticsData.setTotalEMDroppedTxnPercent(String.format("%.2f", totalEMDroppedTxnPercent));
				analyticsData.setTotalEMRejectedTxnPercent(String.format("%.2f", totalEMRejectedTxnPercent));
				analyticsData.setTotalEMDeclinedTxnPercent(String.format("%.2f", totalEMDeclinedTxnPercent));
				analyticsData.setTotalEMPendingTxnPercent(String.format("%.2f", totalEMPendingTxnPercent));
				analyticsData.setTotalEMAcqDownTxnPercent(String.format("%.2f", totalEMAcqDownTxnPercent));
				analyticsData.setTotalEMFailedAtAcqTxnPercent(String.format("%.2f", totalEMFailedAtAcqTxnPercent));
				analyticsData.setTotalEMAcqTimeOutTxnPercent(String.format("%.2f", totalEMAcqTimeOutTxnPercent));

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
				analyticsData.setTotalCDDeclinedTxnPercent("0.00");
				analyticsData.setTotalCDPendingTxnPercent("0.00");
				analyticsData.setTotalCDAcqDownTxnPercent("0.00");
				analyticsData.setTotalCDFailedAtAcqTxnPercent("0.00");
				analyticsData.setTotalCDAcqTimeOutTxnPercent("0.00");

				if (txnType.equalsIgnoreCase(TxnType.SALE.getName())) {
					cdData.add("0");
					cdData.add("0.00");
					cdData.add("0.00");
					cdData.add("0.00");
					cdData.add("0.00");
					cdData.add("0.00");
					cdData.add("0.00");
					cdData.add("0.00");
				} else if (txnType.equalsIgnoreCase(TxnType.REFUND.getName())) {
					cdData.add("0");
					cdData.add("0.00");
					cdData.add("0.00");
					cdData.add("0.00");
					cdData.add("0.00");
					cdData.add("0.00");
					cdData.add("0.00");
					cdData.add("0.00");
					cdData.add("0.00");
					cdData.add("0.00");
					cdData.add("0.00");
				}
				/*
				 * cdData.append("0.00").append("0.00").append("0.00").append("0.00").append(
				 * "0.00")
				 * .append("0.00").append("0.00").append("0.00").append("0.00").append("0.00")
				 * .append("0.00").append("0.00").append("0.00");
				 */

			} else {

				double totalCDSuccessTxnPercent = (cdSuccess / (totalCDTxn)) * 100;
				double totalCDFailedTxnPercent = (cdFailed / (totalCDTxn)) * 100;
				double totalCDCancelledTxnPercent = (cdCancelled / (totalCDTxn)) * 100;
				double totalCDInvalidTxnPercent = (cdInvalid / (totalCDTxn)) * 100;
				double totalCDFraudTxnPercent = (cdFraud / (totalCDTxn)) * 100;
				double totalCDDroppedTxnPercent = (cdDropped / (totalCDTxn)) * 100;
				double totalCDRejectedTxnPercent = (cdRejected / (totalCDTxn)) * 100;
				double totalCDDeclinedTxnPercent = (cdDeclined / (totalCDTxn)) * 100;
				double totalCDPendingTxnPercent = (cdPending / (totalCDTxn)) * 100;
				double totalCDAcqDownTxnPercent = (cdAcqDown / (totalCDTxn)) * 100;
				double totalCDFailedAtAcqTxnPercent = (cdFailedAtAcq / (totalCDTxn)) * 100;
				double totalCDAcqTimeOutTxnPercent = (cdAcqTimeOut / (totalCDTxn)) * 100;

				if (txnType.equalsIgnoreCase(TxnType.SALE.getName())) {
					cdData.add(String.format("%.0f", totalCDTxn));
					cdData.add(String.format("%.2f", totalCDSuccessTxnPercent));
					cdData.add(String.format("%.2f", totalCDFailedTxnPercent));
					cdData.add(String.format("%.2f", totalCDCancelledTxnPercent));
					cdData.add(String.format("%.2f", totalCDInvalidTxnPercent));
					cdData.add(String.format("%.2f", totalCDFraudTxnPercent));
					cdData.add(String.format("%.2f", totalCDDroppedTxnPercent));
					cdData.add(String.format("%.2f", totalCDRejectedTxnPercent));
				} else if (txnType.equalsIgnoreCase(TxnType.REFUND.getName())) {
					cdData.add(String.format("%.0f", totalCDTxn));
					cdData.add(String.format("%.2f", totalCDSuccessTxnPercent));
					cdData.add(String.format("%.2f", totalCDFailedTxnPercent));
					cdData.add(String.format("%.2f", totalCDDeclinedTxnPercent));
					cdData.add(String.format("%.2f", totalCDInvalidTxnPercent));
					cdData.add(String.format("%.2f", totalCDDroppedTxnPercent));
					cdData.add(String.format("%.2f", totalCDRejectedTxnPercent));
					cdData.add(String.format("%.2f", totalCDPendingTxnPercent));
					cdData.add(String.format("%.2f", totalCDAcqDownTxnPercent));
					cdData.add(String.format("%.2f", totalCDFailedAtAcqTxnPercent));
					cdData.add(String.format("%.2f", totalCDAcqTimeOutTxnPercent));
				}

				/*
				 * cdData.append(totalCDTxn).append(",").append(String.format("%.2f",
				 * totalCDSuccessTxnPercent)).append(",").append(String.format("%.2f",
				 * totalCDFailedTxnPercent)) .append(",").append(String.format("%.2f",
				 * totalCDCancelledTxnPercent)).append(",").append(String.format("%.2f",
				 * totalCDInvalidTxnPercent)).append(",") .append(String.format("%.2f",
				 * totalCDFraudTxnPercent)).append(",").append(String.format("%.2f",
				 * totalCDDroppedTxnPercent)).append(",") .append(String.format("%.2f",
				 * totalCDRejectedTxnPercent)).append(",").append(String.format("%.2f",
				 * totalCDDeclinedTxnPercent)).append(",") .append(String.format("%.2f",
				 * totalCDPendingTxnPercent)).append(",").append(String.format("%.2f",
				 * totalCDAcqDownTxnPercent)).append(",") .append(String.format("%.2f",
				 * totalCDFailedAtAcqTxnPercent)).append(",").append(String.format("%.2f",
				 * totalCDAcqTimeOutTxnPercent));
				 */

				analyticsData.setTotalCDTxn(String.format("%.0f", totalCDTxn));
				analyticsData.setTotalCDSuccessTxnPercent(String.format("%.2f", totalCDSuccessTxnPercent));
				analyticsData.setTotalCDFailedTxnPercent(String.format("%.2f", totalCDFailedTxnPercent));
				analyticsData.setTotalCDCancelledTxnPercent(String.format("%.2f", totalCDCancelledTxnPercent));
				analyticsData.setTotalCDInvalidTxnPercent(String.format("%.2f", totalCDInvalidTxnPercent));
				analyticsData.setTotalCDFraudTxnPercent(String.format("%.2f", totalCDFraudTxnPercent));
				analyticsData.setTotalCDDroppedTxnPercent(String.format("%.2f", totalCDDroppedTxnPercent));
				analyticsData.setTotalCDRejectedTxnPercent(String.format("%.2f", totalCDRejectedTxnPercent));
				analyticsData.setTotalCDDeclinedTxnPercent(String.format("%.2f", totalCDDeclinedTxnPercent));
				analyticsData.setTotalCDPendingTxnPercent(String.format("%.2f", totalCDPendingTxnPercent));
				analyticsData.setTotalCDAcqDownTxnPercent(String.format("%.2f", totalCDAcqDownTxnPercent));
				analyticsData.setTotalCDFailedAtAcqTxnPercent(String.format("%.2f", totalCDFailedAtAcqTxnPercent));
				analyticsData.setTotalCDAcqTimeOutTxnPercent(String.format("%.2f", totalCDAcqTimeOutTxnPercent));

			}

			if (paymentType.equals(PaymentType.CREDIT_CARD.getCode())) {

				analyticsData.setCaptured(String.format("%.0f", ccSuccess));
				analyticsData.setFailed(String.format("%.0f", ccFailed));
				analyticsData.setCancelled(String.format("%.0f", ccCancelled));
				analyticsData.setInvalid(String.format("%.0f", ccInvalid));
				analyticsData.setFraud(String.format("%.0f", ccFraud));
				analyticsData.setDropped(String.format("%.0f", ccDropped));
				analyticsData.setRejected(String.format("%.0f", ccRejected));
				analyticsData.setDeclined(String.format("%.0f", ccDeclined));
				analyticsData.setPending(String.format("%.0f", ccPending));
				analyticsData.setAcquirerDown(String.format("%.0f", ccAcqDown));
				analyticsData.setFailedAtAcquirer(String.format("%.0f", ccFailedAtAcq));
				analyticsData.setAcquirerTimeOut(String.format("%.0f", ccAcqTimeOut));

				analyticsData.setCapturedPercent(String.format("%.2f", (ccSuccess / totalCCTxn) * 100));
				analyticsData.setFailedPercent(String.format("%.2f", (ccFailed / totalCCTxn) * 100));
				analyticsData.setCancelledPercent(String.format("%.2f", (ccCancelled / totalCCTxn) * 100));
				analyticsData.setInvalidPercent(String.format("%.2f", (ccInvalid / totalCCTxn) * 100));
				analyticsData.setFraudPercent(String.format("%.2f", (ccFraud / totalCCTxn) * 100));
				analyticsData.setDroppedPercent(String.format("%.2f", (ccDropped / totalCCTxn) * 100));
				analyticsData.setRejectedPercent(String.format("%.2f", (ccRejected / totalCCTxn) * 100));
				analyticsData.setDeclinedPercent(String.format("%.2f", (ccDeclined / totalCCTxn) * 100));
				analyticsData.setPendingPercent(String.format("%.2f", (ccPending / totalCCTxn) * 100));
				analyticsData.setAcquirerDownPercent(String.format("%.2f", (ccAcqDown / totalCCTxn) * 100));
				analyticsData.setFailedAtAcquirerPercent(String.format("%.2f", (ccFailedAtAcq / totalCCTxn) * 100));
				analyticsData.setAcquirerTimeOutPercent(String.format("%.2f", (ccAcqTimeOut / totalCCTxn) * 100));

			} else if (paymentType.equals(PaymentType.DEBIT_CARD.getCode())) {

				analyticsData.setCaptured(String.format("%.0f", dcSuccess));
				analyticsData.setFailed(String.format("%.0f", dcFailed));
				analyticsData.setCancelled(String.format("%.0f", dcCancelled));
				analyticsData.setInvalid(String.format("%.0f", dcInvalid));
				analyticsData.setFraud(String.format("%.0f", dcFraud));
				analyticsData.setDropped(String.format("%.0f", dcDropped));
				analyticsData.setRejected(String.format("%.0f", dcRejected));
				analyticsData.setDeclined(String.format("%.0f", dcDeclined));
				analyticsData.setPending(String.format("%.0f", dcPending));
				analyticsData.setAcquirerDown(String.format("%.0f", dcAcqDown));
				analyticsData.setFailedAtAcquirer(String.format("%.0f", dcFailedAtAcq));
				analyticsData.setAcquirerTimeOut(String.format("%.0f", dcAcqTimeOut));

				analyticsData.setCapturedPercent(String.format("%.2f", (dcSuccess / totalDCTxn) * 100));
				analyticsData.setFailedPercent(String.format("%.2f", (dcFailed / totalDCTxn) * 100));
				analyticsData.setCancelledPercent(String.format("%.2f", (dcCancelled / totalDCTxn) * 100));
				analyticsData.setInvalidPercent(String.format("%.2f", (dcInvalid / totalDCTxn) * 100));
				analyticsData.setFraudPercent(String.format("%.2f", (dcFraud / totalDCTxn) * 100));
				analyticsData.setDroppedPercent(String.format("%.2f", (dcDropped / totalDCTxn) * 100));
				analyticsData.setRejectedPercent(String.format("%.2f", (dcRejected / totalDCTxn) * 100));
				analyticsData.setDeclinedPercent(String.format("%.2f", (dcDeclined / totalCCTxn) * 100));
				analyticsData.setPendingPercent(String.format("%.2f", (dcPending / totalCCTxn) * 100));
				analyticsData.setAcquirerDownPercent(String.format("%.2f", (dcAcqDown / totalCCTxn) * 100));
				analyticsData.setFailedAtAcquirerPercent(String.format("%.2f", (dcFailedAtAcq / totalCCTxn) * 100));
				analyticsData.setAcquirerTimeOutPercent(String.format("%.2f", (dcAcqTimeOut / totalCCTxn) * 100));

			} else if (paymentType.equals(PaymentType.UPI.getCode())) {

				analyticsData.setCaptured(String.format("%.0f", upSuccess));
				analyticsData.setFailed(String.format("%.0f", upFailed));
				analyticsData.setCancelled(String.format("%.0f", upCancelled));
				analyticsData.setInvalid(String.format("%.0f", upInvalid));
				analyticsData.setFraud(String.format("%.0f", upFraud));
				analyticsData.setDropped(String.format("%.0f", upDropped));
				analyticsData.setRejected(String.format("%.0f", upRejected));
				analyticsData.setDeclined(String.format("%.0f", upDeclined));
				analyticsData.setPending(String.format("%.0f", upPending));
				analyticsData.setAcquirerDown(String.format("%.0f", upAcqDown));
				analyticsData.setFailedAtAcquirer(String.format("%.0f", upFailedAtAcq));
				analyticsData.setAcquirerTimeOut(String.format("%.0f", upAcqTimeOut));

				analyticsData.setCapturedPercent(String.format("%.2f", (upSuccess / totalUPTxn) * 100));
				analyticsData.setFailedPercent(String.format("%.2f", (upFailed / totalUPTxn) * 100));
				analyticsData.setCancelledPercent(String.format("%.2f", (upCancelled / totalUPTxn) * 100));
				analyticsData.setInvalidPercent(String.format("%.2f", (upInvalid / totalUPTxn) * 100));
				analyticsData.setFraudPercent(String.format("%.2f", (upFraud / totalUPTxn) * 100));
				analyticsData.setDroppedPercent(String.format("%.2f", (upDropped / totalUPTxn) * 100));
				analyticsData.setRejectedPercent(String.format("%.2f", (upRejected / totalUPTxn) * 100));
				analyticsData.setDeclinedPercent(String.format("%.2f", (upDeclined / totalUPTxn) * 100));
				analyticsData.setPendingPercent(String.format("%.2f", (upPending / totalUPTxn) * 100));
				analyticsData.setAcquirerDownPercent(String.format("%.2f", (upAcqDown / totalUPTxn) * 100));
				analyticsData.setFailedAtAcquirerPercent(String.format("%.2f", (upFailedAtAcq / totalUPTxn) * 100));
				analyticsData.setAcquirerTimeOutPercent(String.format("%.2f", (upAcqTimeOut / totalUPTxn) * 100));

			}

			else if (paymentType.equals(PaymentType.NET_BANKING.getCode())) {

				analyticsData.setCaptured(String.format("%.0f", nbSuccess));
				analyticsData.setFailed(String.format("%.0f", nbFailed));
				analyticsData.setCancelled(String.format("%.0f", nbCancelled));
				analyticsData.setInvalid(String.format("%.0f", nbInvalid));
				analyticsData.setFraud(String.format("%.0f", nbFraud));
				analyticsData.setDropped(String.format("%.0f", nbDropped));
				analyticsData.setRejected(String.format("%.0f", nbRejected));
				analyticsData.setDeclined(String.format("%.0f", nbDeclined));
				analyticsData.setPending(String.format("%.0f", nbPending));
				analyticsData.setAcquirerDown(String.format("%.0f", nbAcqDown));
				analyticsData.setFailedAtAcquirer(String.format("%.0f", nbFailedAtAcq));
				analyticsData.setAcquirerTimeOut(String.format("%.0f", nbAcqTimeOut));

				analyticsData.setCapturedPercent(String.format("%.2f", (nbSuccess / totalNBTxn) * 100));
				analyticsData.setFailedPercent(String.format("%.2f", (nbFailed / totalNBTxn) * 100));
				analyticsData.setCancelledPercent(String.format("%.2f", (nbCancelled / totalNBTxn) * 100));
				analyticsData.setInvalidPercent(String.format("%.2f", (nbInvalid / totalNBTxn) * 100));
				analyticsData.setFraudPercent(String.format("%.2f", (nbFraud / totalNBTxn) * 100));
				analyticsData.setDroppedPercent(String.format("%.2f", (nbDropped / totalNBTxn) * 100));
				analyticsData.setRejectedPercent(String.format("%.2f", (nbRejected / totalNBTxn) * 100));
				analyticsData.setDeclinedPercent(String.format("%.2f", (nbDeclined / totalNBTxn) * 100));
				analyticsData.setPendingPercent(String.format("%.2f", (nbPending / totalNBTxn) * 100));
				analyticsData.setAcquirerDownPercent(String.format("%.2f", (nbAcqDown / totalNBTxn) * 100));
				analyticsData.setFailedAtAcquirerPercent(String.format("%.2f", (nbFailedAtAcq / totalNBTxn) * 100));
				analyticsData.setAcquirerTimeOutPercent(String.format("%.2f", (nbAcqTimeOut / totalNBTxn) * 100));

			} else if (paymentType.equals(PaymentType.WALLET.getCode())) {

				analyticsData.setCaptured(String.format("%.0f", wlSuccess));
				analyticsData.setFailed(String.format("%.0f", wlFailed));
				analyticsData.setCancelled(String.format("%.0f", wlCancelled));
				analyticsData.setInvalid(String.format("%.0f", wlInvalid));
				analyticsData.setFraud(String.format("%.0f", wlFraud));
				analyticsData.setDropped(String.format("%.0f", wlDropped));
				analyticsData.setRejected(String.format("%.0f", wlRejected));
				analyticsData.setDeclined(String.format("%.0f", wlDeclined));
				analyticsData.setPending(String.format("%.0f", wlPending));
				analyticsData.setAcquirerDown(String.format("%.0f", wlAcqDown));
				analyticsData.setFailedAtAcquirer(String.format("%.0f", wlFailedAtAcq));
				analyticsData.setAcquirerTimeOut(String.format("%.0f", wlAcqTimeOut));

				analyticsData.setCapturedPercent(String.format("%.2f", (wlSuccess / totalWLTxn) * 100));
				analyticsData.setFailedPercent(String.format("%.2f", (wlFailed / totalWLTxn) * 100));
				analyticsData.setCancelledPercent(String.format("%.2f", (wlCancelled / totalWLTxn) * 100));
				analyticsData.setInvalidPercent(String.format("%.2f", (wlInvalid / totalWLTxn) * 100));
				analyticsData.setFraudPercent(String.format("%.2f", (wlFraud / totalWLTxn) * 100));
				analyticsData.setDroppedPercent(String.format("%.2f", (wlDropped / totalWLTxn) * 100));
				analyticsData.setRejectedPercent(String.format("%.2f", (wlRejected / totalWLTxn) * 100));
				analyticsData.setDeclinedPercent(String.format("%.2f", (wlDeclined / totalWLTxn) * 100));
				analyticsData.setPendingPercent(String.format("%.2f", (wlPending / totalWLTxn) * 100));
				analyticsData.setAcquirerDownPercent(String.format("%.2f", (wlAcqDown / totalWLTxn) * 100));
				analyticsData.setFailedAtAcquirerPercent(String.format("%.2f", (wlFailedAtAcq / totalWLTxn) * 100));
				analyticsData.setAcquirerTimeOutPercent(String.format("%.2f", (wlAcqTimeOut / totalWLTxn) * 100));

			} else if (paymentType.equals(PaymentType.EMI.getCode())) {

				analyticsData.setCaptured(String.format("%.0f", emSuccess));
				analyticsData.setFailed(String.format("%.0f", emFailed));
				analyticsData.setCancelled(String.format("%.0f", emCancelled));
				analyticsData.setInvalid(String.format("%.0f", emInvalid));
				analyticsData.setFraud(String.format("%.0f", emFraud));
				analyticsData.setDropped(String.format("%.0f", emDropped));
				analyticsData.setRejected(String.format("%.0f", emRejected));
				analyticsData.setDeclined(String.format("%.0f", emDeclined));
				analyticsData.setPending(String.format("%.0f", emPending));
				analyticsData.setAcquirerDown(String.format("%.0f", emAcqDown));
				analyticsData.setFailedAtAcquirer(String.format("%.0f", emFailedAtAcq));
				analyticsData.setAcquirerTimeOut(String.format("%.0f", emAcqTimeOut));

				analyticsData.setCapturedPercent(String.format("%.2f", (emSuccess / totalEMTxn) * 100));
				analyticsData.setFailedPercent(String.format("%.2f", (emFailed / totalEMTxn) * 100));
				analyticsData.setCancelledPercent(String.format("%.2f", (emCancelled / totalEMTxn) * 100));
				analyticsData.setInvalidPercent(String.format("%.2f", (emInvalid / totalEMTxn) * 100));
				analyticsData.setFraudPercent(String.format("%.2f", (emFraud / totalEMTxn) * 100));
				analyticsData.setDroppedPercent(String.format("%.2f", (emDropped / totalEMTxn) * 100));
				analyticsData.setRejectedPercent(String.format("%.2f", (emRejected / totalEMTxn) * 100));
				analyticsData.setDeclinedPercent(String.format("%.2f", (emDeclined / totalEMTxn) * 100));
				analyticsData.setPendingPercent(String.format("%.2f", (emPending / totalEMTxn) * 100));
				analyticsData.setAcquirerDownPercent(String.format("%.2f", (emAcqDown / totalEMTxn) * 100));
				analyticsData.setFailedAtAcquirerPercent(String.format("%.2f", (emFailedAtAcq / totalEMTxn) * 100));
				analyticsData.setAcquirerTimeOutPercent(String.format("%.2f", (emAcqTimeOut / totalEMTxn) * 100));

			} else if (paymentType.equals(PaymentType.COD.getCode())) {

				analyticsData.setCaptured(String.format("%.0f", cdSuccess));
				analyticsData.setFailed(String.format("%.0f", cdFailed));
				analyticsData.setCancelled(String.format("%.0f", cdCancelled));
				analyticsData.setInvalid(String.format("%.0f", cdInvalid));
				analyticsData.setFraud(String.format("%.0f", cdFraud));
				analyticsData.setDropped(String.format("%.0f", cdDropped));
				analyticsData.setRejected(String.format("%.0f", cdRejected));
				analyticsData.setDeclined(String.format("%.0f", cdDeclined));
				analyticsData.setPending(String.format("%.0f", cdPending));
				analyticsData.setAcquirerDown(String.format("%.0f", cdAcqDown));
				analyticsData.setFailedAtAcquirer(String.format("%.0f", cdFailedAtAcq));
				analyticsData.setAcquirerTimeOut(String.format("%.0f", cdAcqTimeOut));

				analyticsData.setCapturedPercent(String.format("%.2f", (cdSuccess / totalCDTxn) * 100));
				analyticsData.setFailedPercent(String.format("%.2f", (cdFailed / totalCDTxn) * 100));
				analyticsData.setCancelledPercent(String.format("%.2f", (cdCancelled / totalCDTxn) * 100));
				analyticsData.setInvalidPercent(String.format("%.2f", (cdInvalid / totalCDTxn) * 100));
				analyticsData.setFraudPercent(String.format("%.2f", (cdFraud / totalCDTxn) * 100));
				analyticsData.setDroppedPercent(String.format("%.2f", (cdDropped / totalCDTxn) * 100));
				analyticsData.setRejectedPercent(String.format("%.2f", (cdRejected / totalCDTxn) * 100));
				analyticsData.setDeclinedPercent(String.format("%.2f", (cdDeclined / totalCDTxn) * 100));
				analyticsData.setPendingPercent(String.format("%.2f", (cdPending / totalCDTxn) * 100));
				analyticsData.setAcquirerDownPercent(String.format("%.2f", (cdAcqDown / totalCDTxn) * 100));
				analyticsData.setFailedAtAcquirerPercent(String.format("%.2f", (cdFailedAtAcq / totalCDTxn) * 100));
				analyticsData.setAcquirerTimeOutPercent(String.format("%.2f", (cdAcqTimeOut / totalCDTxn) * 100));

			} else {

				analyticsData.setCaptured("0");
				analyticsData.setFailed("0");
				analyticsData.setCancelled("0");
				analyticsData.setInvalid("0");
				analyticsData.setFraud("0");
				analyticsData.setDropped("0");
				analyticsData.setRejected("0");
				analyticsData.setDeclined("0");
				analyticsData.setPending("0");
				analyticsData.setAcquirerDown("0");
				analyticsData.setFailedAtAcquirer("0");
				analyticsData.setAcquirerTimeOut("0");

				analyticsData.setCapturedPercent("0.00");
				analyticsData.setFailedPercent("0.00");
				analyticsData.setCancelledPercent("0.00");
				analyticsData.setInvalidPercent("0.00");
				analyticsData.setFraudPercent("0.00");
				analyticsData.setDroppedPercent("0.00");
				analyticsData.setRejectedPercent("0.00");
				analyticsData.setDeclinedPercent("0.00");
				analyticsData.setPendingPercent("0.00");
				analyticsData.setAcquirerDownPercent("0.00");
				analyticsData.setFailedAtAcquirerPercent("0.00");
				analyticsData.setAcquirerTimeOutPercent("0.00");

			}

			analyticsData.setTotalCCTxnAmount(String.format("%.2f", totalCCTxnAmount));
			analyticsData.setTotalDCTxnAmount(String.format("%.2f", totalDCTxnAmount));
			analyticsData.setTotalUPTxnAmount(String.format("%.2f", totalUPTxnAmount));
			analyticsData.setTotalNBTxnAmount(String.format("%.2f", totalNBTxnAmount));
			analyticsData.setTotalWLTxnAmount(String.format("%.2f", totalWLTxnAmount));
			analyticsData.setTotalEMTxnAmount(String.format("%.2f", totalEMTxnAmount));
			analyticsData.setTotalCDTxnAmount(String.format("%.2f", totalCDTxnAmount));

			analyticsData.setTotalCCCapturedCount(String.format("%.0f", ccSuccess));
			analyticsData.setTotalDCCapturedCount(String.format("%.0f", dcSuccess));
			analyticsData.setTotalUPCapturedCount(String.format("%.0f", upSuccess));
			analyticsData.setTotalNBCapturedCount(String.format("%.0f", nbSuccess));
			analyticsData.setTotalWLCapturedCount(String.format("%.0f", wlSuccess));
			analyticsData.setTotalEMCapturedCount(String.format("%.0f", emSuccess));
			analyticsData.setTotalCDCapturedCount(String.format("%.0f", cdSuccess));

			analyticsData.setUnknownTxnCount(String.valueOf(unknownTransactions));
			analyticsData.setTotalCapturedTxnAmount(String.format("%.2f", totalTxnAmount));
			analyticsData.setMerchantPgRatio("0.00 %");
			analyticsData.setAcquirerPgRatio("0.00 %");
			naData.add(String.valueOf(unknownTransactions));
			performanceList.add(ccData);
			performanceList.add(dcData);
			performanceList.add(upData);
			performanceList.add(nbData);
			performanceList.add(wlData);
			performanceList.add(emData);
			performanceList.add(cdData);
			performanceList.add(naData);

			analyticsData.setPerformanceData(performanceList);
			finalList.add(performanceList);
			logger.info("Calculation Finised");
			if (txnType.equalsIgnoreCase(TxnType.SALE.getName())) {
				analyticsData.setStatusList(saleHeader.split(","));
			} else if (txnType.equalsIgnoreCase(TxnType.REFUND.getName())) {
				analyticsData.setStatusList(refundHeader.split(","));
			}

			return analyticsData;
		}

		catch (Exception e) {
			logger.error("Exception in transaction summary count service ", e);
		}
		return null;
	}

	// get Payment Gateway Profit

	public AnalyticsData getMerchantSMSData(String fromDate, String toDate, String payId, String paymentType,
			String acquirer, User user, String param) {
		logger.info(" inside getMerchantSMSData for Preparing Payment GateWay capturedData SMS  ");
		AnalyticsData analyticsData = new AnalyticsData();

		try {

			/*
			 * fromDate = DateCreater.toDateTimeformatCreater(fromDate); toDate =
			 * DateCreater.formDateTimeformatCreater(toDate);
			 */

			BasicDBObject dateQuery = new BasicDBObject();
			List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();
			BasicDBObject currencyQuery = new BasicDBObject();
			BasicDBObject acquirerQuery = new BasicDBObject();

			BasicDBObject allParamQuery = new BasicDBObject();
			List<BasicDBObject> acquirerConditionLst = new ArrayList<BasicDBObject>();
			List<BasicDBObject> saleCapturedList = new ArrayList<BasicDBObject>();
			List<BasicDBObject> refundCapturedList = new ArrayList<BasicDBObject>();

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

			// SALE Settled query
			List<BasicDBObject> saleCapturedConditionList = new ArrayList<BasicDBObject>();
			saleCapturedConditionList
					.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName()));
			saleCapturedConditionList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.CAPTURED.getName()));

			BasicDBObject saleConditionQuery = new BasicDBObject("$and", saleCapturedConditionList);
			saleCapturedList.add(saleConditionQuery);

			// REFUND Settled query
			List<BasicDBObject> refundCapturedConditionList = new ArrayList<BasicDBObject>();
			refundCapturedConditionList
					.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.REFUND.getName()));
			refundCapturedConditionList
					.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.CAPTURED.getName()));

			BasicDBObject refundConditionQuery = new BasicDBObject("$and", refundCapturedConditionList);
			saleCapturedList.add(refundConditionQuery);

			BasicDBObject saleCapturedConditionQuery = new BasicDBObject("$or", saleCapturedList);
			BasicDBObject refundCapturedConditionQuery = new BasicDBObject("$or", refundCapturedList);

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
			allConditionQueryList.add(saleCapturedConditionQuery);
			// allConditionQueryList.add(refundSettledConditionQuery);

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

			BigDecimal saleCapturedAmount = BigDecimal.ZERO;
			BigDecimal refundCapturedAmount = BigDecimal.ZERO;
			BigDecimal paymnetGatewayProfitAmount = BigDecimal.ZERO;

			int ccCapturedCount = 0;
			int dcCapturedCount = 0;
			int upCapturedCount = 0;
			int nbCapturedCount = 0;
			int wlCapturedCount = 0;
			int emCapturedCount = 0;
			int cdCapturedCount = 0;

			Double ccTxnPer = 0.00;
			Double ccAmtPer = 0.00;
			BigDecimal ccTotalAmt = BigDecimal.ZERO;
			Double dcTxnPer = 0.00;
			Double dcAmtPer = 0.00;
			BigDecimal dcTotalAmt = BigDecimal.ZERO;
			Double upTxnPer = 0.00;
			Double upAmtPer = 0.00;
			BigDecimal upTotalAmt = BigDecimal.ZERO;
			Double nbTxnPer = 0.00;
			Double nbAmtPer = 0.00;
			BigDecimal nbTotalAmt = BigDecimal.ZERO;
			Double wlTxnPer = 0.00;
			Double wlAmtPer = 0.00;
			BigDecimal wlTotalAmt = BigDecimal.ZERO;
			Double emTxnPer = 0.00;
			Double emAmtPer = 0.00;
			BigDecimal emTotalAmt = BigDecimal.ZERO;
			Double cdTxnPer = 0.00;
			Double cdAmtPer = 0.00;
			BigDecimal cdTotalAmt = BigDecimal.ZERO;

			List<String> captureDateArray = new ArrayList<String>();

			MongoCursor<Document> cursor = coll.find(finalQuery).iterator();

			while (cursor.hasNext()) {

				Document dbobj = cursor.next();

				if (dbobj.getString(FieldType.TXNTYPE.getName()).equalsIgnoreCase(TransactionType.SALE.getName())) {
					saleCapturedAmount = saleCapturedAmount
							.add(new BigDecimal(dbobj.getString(FieldType.AMOUNT.getName())));
					if (dbobj.getString(FieldType.PAYMENT_TYPE.getName())
							.equalsIgnoreCase(PaymentType.CREDIT_CARD.getCode())) {
						ccCapturedCount++;
						ccTotalAmt = ccTotalAmt.add(new BigDecimal(dbobj.getString(FieldType.AMOUNT.getName())));
					} else if (dbobj.getString(FieldType.PAYMENT_TYPE.getName())
							.equalsIgnoreCase(PaymentType.DEBIT_CARD.getCode())) {
						dcCapturedCount++;
						dcTotalAmt = dcTotalAmt.add(new BigDecimal(dbobj.getString(FieldType.AMOUNT.getName())));
					} else if (dbobj.getString(FieldType.PAYMENT_TYPE.getName())
							.equalsIgnoreCase(PaymentType.UPI.getCode())) {
						upCapturedCount++;
						upTotalAmt = upTotalAmt.add(new BigDecimal(dbobj.getString(FieldType.AMOUNT.getName())));
					} else if (dbobj.getString(FieldType.PAYMENT_TYPE.getName())
							.equalsIgnoreCase(PaymentType.NET_BANKING.getCode())) {
						nbCapturedCount++;
						nbTotalAmt = nbTotalAmt.add(new BigDecimal(dbobj.getString(FieldType.AMOUNT.getName())));
					} else if (dbobj.getString(FieldType.PAYMENT_TYPE.getName())
							.equalsIgnoreCase(PaymentType.WALLET.getCode())) {
						wlCapturedCount++;
						wlTotalAmt = wlTotalAmt.add(new BigDecimal(dbobj.getString(FieldType.AMOUNT.getName())));
					} else if (dbobj.getString(FieldType.PAYMENT_TYPE.getName())
							.equalsIgnoreCase(PaymentType.EMI.getCode())) {
						emCapturedCount++;
						emTotalAmt = emTotalAmt.add(new BigDecimal(dbobj.getString(FieldType.AMOUNT.getName())));
					} else if (dbobj.getString(FieldType.PAYMENT_TYPE.getName())
							.equalsIgnoreCase(PaymentType.COD.getCode())) {
						cdCapturedCount++;
						cdTotalAmt = cdTotalAmt.add(new BigDecimal(dbobj.getString(FieldType.AMOUNT.getName())));
					}

					captureDateArray.add(dbobj.getString(FieldType.CREATE_DATE.getName()));

				} else {
					refundCapturedAmount = refundCapturedAmount
							.add(new BigDecimal(dbobj.getString(FieldType.AMOUNT.getName())));
				}

			}

			cursor.close();
			Collections.sort(captureDateArray);

			if ((ccCapturedCount + dcCapturedCount + upCapturedCount + nbCapturedCount + wlCapturedCount
					+ emCapturedCount + cdCapturedCount) > 0) {

				ccTxnPer = (Double.valueOf(ccCapturedCount) / Double.valueOf(ccCapturedCount + dcCapturedCount
						+ upCapturedCount + nbCapturedCount + wlCapturedCount + emCapturedCount + cdCapturedCount))
						* 100;
				dcTxnPer = (Double.valueOf(dcCapturedCount) / Double.valueOf(ccCapturedCount + dcCapturedCount
						+ upCapturedCount + nbCapturedCount + wlCapturedCount + emCapturedCount + cdCapturedCount))
						* 100;
				upTxnPer = (Double.valueOf(upCapturedCount) / Double.valueOf(ccCapturedCount + dcCapturedCount
						+ upCapturedCount + nbCapturedCount + wlCapturedCount + emCapturedCount + cdCapturedCount))
						* 100;
				nbTxnPer = (Double.valueOf(nbCapturedCount) / Double.valueOf(ccCapturedCount + dcCapturedCount
						+ upCapturedCount + nbCapturedCount + wlCapturedCount + emCapturedCount + cdCapturedCount))
						* 100;
				wlTxnPer = (Double.valueOf(wlCapturedCount) / Double.valueOf(ccCapturedCount + dcCapturedCount
						+ upCapturedCount + nbCapturedCount + wlCapturedCount + emCapturedCount + cdCapturedCount))
						* 100;
				emTxnPer = (Double.valueOf(emCapturedCount) / Double.valueOf(ccCapturedCount + dcCapturedCount
						+ upCapturedCount + nbCapturedCount + wlCapturedCount + emCapturedCount + cdCapturedCount))
						* 100;
				cdTxnPer = (Double.valueOf(cdCapturedCount) / Double.valueOf(ccCapturedCount + dcCapturedCount
						+ upCapturedCount + nbCapturedCount + wlCapturedCount + emCapturedCount + cdCapturedCount))
						* 100;

				ccAmtPer = (ccTotalAmt.divide(saleCapturedAmount, 2, RoundingMode.HALF_DOWN))
						.multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_DOWN).doubleValue();
				dcAmtPer = (dcTotalAmt.divide(saleCapturedAmount, 2, RoundingMode.HALF_DOWN))
						.multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_DOWN).doubleValue();
				upAmtPer = (upTotalAmt.divide(saleCapturedAmount, 2, RoundingMode.HALF_DOWN))
						.multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_DOWN).doubleValue();
				nbAmtPer = (nbTotalAmt.divide(saleCapturedAmount, 2, RoundingMode.HALF_DOWN))
						.multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_DOWN).doubleValue();
				wlAmtPer = (wlTotalAmt.divide(saleCapturedAmount, 2, RoundingMode.HALF_DOWN))
						.multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_DOWN).doubleValue();
				emAmtPer = (emTotalAmt.divide(saleCapturedAmount, 2, RoundingMode.HALF_DOWN))
						.multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_DOWN).doubleValue();
				cdAmtPer = (cdTotalAmt.divide(saleCapturedAmount, 2, RoundingMode.HALF_DOWN))
						.multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_DOWN).doubleValue();

				paymnetGatewayProfitAmount = saleCapturedAmount.subtract(refundCapturedAmount);
			}

			SimpleDateFormat outFormat = new SimpleDateFormat("dd-MMM-yyyy");

			Date dateCapFrom = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss", Locale.ENGLISH)
					.parse(captureDateArray.get(0));
			Date dateCapTo = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss", Locale.ENGLISH)
					.parse(captureDateArray.get(captureDateArray.size() - 1));

			String dateCapFromString = outFormat.format(dateCapFrom);
			String dateCapToString = outFormat.format(dateCapTo);

			if (captureDateArray.size() > 0) {

				if (dateCapFromString.equalsIgnoreCase(dateCapToString)) {

					analyticsData.setDateCaptured(dateCapFromString);
				} else {
					analyticsData.setDateCaptured(dateCapFromString + " to " + dateCapToString);
				}

			} else {
				analyticsData.setDateCaptured("No Data Found");
			}

			Date dateSettleFrom = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss", Locale.ENGLISH).parse(fromDate);
			Date dateSettleTo = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss", Locale.ENGLISH).parse(toDate);

			String dateSettleFromString = outFormat.format(dateSettleFrom);
			String dateSettleToString = outFormat.format(dateSettleTo);

			if (dateSettleFromString.equalsIgnoreCase(dateSettleToString)) {
				analyticsData.setDateSettled(dateSettleFromString);
			} else {
				analyticsData.setDateSettled(dateSettleFromString + " to " + dateSettleToString);
			}

			analyticsData.setpaymentGatewayProfitAmount(format(String.format("%.0f", paymnetGatewayProfitAmount)) + ".00");

			return analyticsData;
		}

		catch (Exception e) {

			logger.error("Exception in AnalyticsData service ", e);
			analyticsData.setDateCaptured("Invalid Date Range");
			analyticsData.setDateSettled("Invalid Date Range");
			analyticsData.setpaymentGatewayProfitAmount("0.00");

			return analyticsData;
		}

	}

	public String format(String amount) {
		StringBuilder stringBuilder = new StringBuilder();
		char amountArray[] = amount.toCharArray();
		int a = 0, b = 0;
		for (int i = amountArray.length - 1; i >= 0; i--) {
			if (a < 3) {
				stringBuilder.append(amountArray[i]);
				a++;
			} else if (b < 2) {
				if (b == 0) {
					stringBuilder.append(",");
					stringBuilder.append(amountArray[i]);
					b++;
				} else {
					stringBuilder.append(amountArray[i]);
					b = 0;
				}
			}
		}
		return stringBuilder.reverse().toString();
	}
	// total Amount inc/exc GST

	public AnalyticsData getTransactionTotalProfitCount(String fromDate, String toDate, String payId,
			String paymentType, String acquirer, User user, String param) {
		logger.info(" inside getTransactionTotalProfitCount for Preparing Payment Gateway capturedData SMS  ");
		List<AnalyticsData> analyticsDataSearchList = new ArrayList<AnalyticsData>();
		List<String> txnTypeList = new ArrayList<String>();
		txnTypeList.add(TxnType.SALE.getName());
		txnTypeList.add(TxnType.REFUND.getName());

		try {

			surchargeList.clear();
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH);
			Date date1 = format.parse(fromDate);
			Date date2 = format.parse(toDate);

			surchargeList = surchargeDao.findAllSurchargeByDate(date1, date2);
			postSettledTransactionCount = 0;
			BasicDBObject dateQuery = new BasicDBObject();
			List<BasicDBObject> paramConditionLst = new ArrayList<BasicDBObject>();
			BasicDBObject currencyQuery = new BasicDBObject();
			BasicDBObject acquirerQuery = new BasicDBObject();

			BasicDBObject allParamQuery = new BasicDBObject();
			List<BasicDBObject> acquirerConditionLst = new ArrayList<BasicDBObject>();
			List<BasicDBObject> saleCapturedList = new ArrayList<BasicDBObject>();
			List<BasicDBObject> refundCapturedList = new ArrayList<BasicDBObject>();

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
			List<BasicDBObject> saleCapturedConditionList = new ArrayList<BasicDBObject>();
			saleCapturedConditionList
					.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName()));
			saleCapturedConditionList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.CAPTURED.getName()));
			saleCapturedConditionList
					.add(new BasicDBObject(FieldType.RESPONSE_CODE.getName(), ErrorType.SUCCESS.getCode()));

			BasicDBObject saleConditionQuery = new BasicDBObject("$and", saleCapturedConditionList);
			saleCapturedList.add(saleConditionQuery);

			// REFUND Captured query
			List<BasicDBObject> refundCapturedConditionList = new ArrayList<BasicDBObject>();
			refundCapturedConditionList
					.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.REFUND.getName()));
			refundCapturedConditionList
					.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.CAPTURED.getName()));
			refundCapturedConditionList
					.add(new BasicDBObject(FieldType.RESPONSE_CODE.getName(), ErrorType.SUCCESS.getCode()));

			BasicDBObject refundConditionQuery = new BasicDBObject("$and", refundCapturedConditionList);
			refundCapturedList.add(refundConditionQuery);

			BasicDBObject saleSettledConditionQuery = new BasicDBObject("$or", saleCapturedList);
			BasicDBObject refundSettledConditionQuery = new BasicDBObject("$or", refundCapturedList);

			AnalyticsData analyticsData = new AnalyticsData();
			analyticsData.setAcquirer(acquirer);

			if (!paymentType.equalsIgnoreCase("ALL")) {
				analyticsData.setPaymentMethod(PaymentType.getpaymentName(paymentType));
			} else {
				analyticsData.setPaymentMethod(paymentType);
			}

			for (String txnType : txnTypeList) {

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
				if (!dateQuery.isEmpty()) {
					allConditionQueryList.add(dateQuery);
				}

				if (txnType.equalsIgnoreCase(TransactionType.SALE.getName())) {
					if (!saleSettledConditionQuery.isEmpty()) {
						allConditionQueryList.add(saleSettledConditionQuery);
					}
				} else {
					if (!refundSettledConditionQuery.isEmpty()) {
						allConditionQueryList.add(refundSettledConditionQuery);
					}
				}

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
				MongoCollection<Document> coll = dbIns.getCollection(
						PropertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));

				int totalSettled = (int) coll.count(finalQuery);
				// TODO remove delta count
				if (txnType.equalsIgnoreCase(TransactionType.SALE.getName())) {

					analyticsData.setSaleCapturedCount(String.valueOf(totalSettled));
				} else {
					analyticsData.setRefundCapturedCount(String.valueOf(totalSettled));
				}
				MongoCursor<Document> cursor = coll.find(finalQuery).iterator();

				// Remove all data from an earlier map
				while (cursor.hasNext()) {
					Document dbobj = cursor.next();
					AnalyticsData analyticsDataObj = new AnalyticsData();
					analyticsDataObj = findDetails(dbobj);
					analyticsDataObj.setSaleCapturedCount(analyticsData.getSaleCapturedCount());
					analyticsDataObj.setRefundCapturedCount(analyticsData.getRefundCapturedCount());
					analyticsData.setGst(analyticsDataObj.getGst());
					analyticsDataSearchList.add(analyticsDataObj);

				}

				cursor.close();
			}

			BigDecimal saleCapturedAmount = BigDecimal.ZERO;
			BigDecimal pgSaleSurcharge = BigDecimal.ZERO;
			BigDecimal acquirerSaleSurcharge = BigDecimal.ZERO;
			BigDecimal pgSaleGst = BigDecimal.ZERO;
			BigDecimal acquirerSaleGst = BigDecimal.ZERO;

			BigDecimal refundCapturedAmount = BigDecimal.ZERO;
			BigDecimal pgRefundSurcharge = BigDecimal.ZERO;
			BigDecimal acquirerRefundSurcharge = BigDecimal.ZERO;
			BigDecimal pgRefundGst = BigDecimal.ZERO;
			BigDecimal acquirerRefundGst = BigDecimal.ZERO;

			BigDecimal totalMerchantAmount = BigDecimal.ZERO;
			BigDecimal merchantSaleCapturedAmount = BigDecimal.ZERO;
			BigDecimal merchantRefundCapturedAmount = BigDecimal.ZERO;

			BigDecimal totalCapturedAmountActual = BigDecimal.ZERO;
			BigDecimal totalSettledAmountDelta = BigDecimal.ZERO;

			double ccCapturedCount = 0;
			double dcCapturedCount = 0;
			double upCapturedCount = 0;
			double nbCapturedCount = 0;
			double wlCapturedCount = 0;
			double emCapturedCount = 0;
			double cdCapturedCount = 0;
			for (AnalyticsData analyticsDataSearchObj : analyticsDataSearchList) {

				if (analyticsDataSearchObj.getTxnType().equalsIgnoreCase(TransactionType.SALE.getName())) {

					BigDecimal saleCapturedAmountObj = new BigDecimal(analyticsDataSearchObj.getSaleCapturedAmount());
					BigDecimal pgSaleSurchargeObj = new BigDecimal(analyticsDataSearchObj.getPgSaleSurcharge());
					BigDecimal acquirerSaleSurchargeObj = new BigDecimal(
							analyticsDataSearchObj.getAcquirerSaleSurcharge());
					BigDecimal pgSaleGstObj = new BigDecimal(analyticsDataSearchObj.getPgSaleGst());
					BigDecimal acquirerSaleGstObj = new BigDecimal(analyticsDataSearchObj.getAcquirerSaleGst());

					saleCapturedAmount = saleCapturedAmount.add(saleCapturedAmountObj).setScale(2,
							RoundingMode.HALF_DOWN);
					pgSaleSurcharge = pgSaleSurcharge.add(pgSaleSurchargeObj).setScale(2, RoundingMode.HALF_DOWN);
					acquirerSaleSurcharge = acquirerSaleSurcharge.add(acquirerSaleSurchargeObj).setScale(2,
							RoundingMode.HALF_DOWN);
					pgSaleGst = pgSaleGst.add(pgSaleGstObj).setScale(2, RoundingMode.HALF_DOWN);
					acquirerSaleGst = acquirerSaleGst.add(acquirerSaleGstObj).setScale(2, RoundingMode.HALF_DOWN);

					if (analyticsDataSearchObj.getPaymentMethod().equalsIgnoreCase(PaymentType.CREDIT_CARD.getCode())) {
						ccCapturedCount++;
					} else if (analyticsDataSearchObj.getPaymentMethod()
							.equalsIgnoreCase(PaymentType.DEBIT_CARD.getCode())) {
						dcCapturedCount++;
					} else if (analyticsDataSearchObj.getPaymentMethod().equalsIgnoreCase(PaymentType.UPI.getCode())) {
						upCapturedCount++;
					} else if (analyticsDataSearchObj.getPaymentMethod()
							.equalsIgnoreCase(PaymentType.NET_BANKING.getCode())) {
						nbCapturedCount++;
					} else if (analyticsDataSearchObj.getPaymentMethod()
							.equalsIgnoreCase(PaymentType.WALLET.getCode())) {
						wlCapturedCount++;
					} else if (analyticsDataSearchObj.getPaymentMethod().equalsIgnoreCase(PaymentType.EMI.getCode())) {
						emCapturedCount++;
					} else if (analyticsDataSearchObj.getPaymentMethod().equalsIgnoreCase(PaymentType.COD.getCode())) {
						cdCapturedCount++;
					}

					// totalMerchantAmount =
					// totalMerchantAmount.add(saleSettledAmountObj);

				} else {

					BigDecimal refundCapturedAmountObj = new BigDecimal(
							analyticsDataSearchObj.getRefundCapturedAmount());
					BigDecimal pgRefundSurchargeObj = new BigDecimal(analyticsDataSearchObj.getPgRefundSurcharge());
					BigDecimal acquirerRefundSurchargeObj = new BigDecimal(
							analyticsDataSearchObj.getAcquirerRefundSurcharge());
					BigDecimal pgRefundGstObj = new BigDecimal(analyticsDataSearchObj.getPgRefundGst());
					BigDecimal acquirerRefundGstObj = new BigDecimal(analyticsDataSearchObj.getAcquirerRefundGst());

					refundCapturedAmount = refundCapturedAmount.add(refundCapturedAmountObj).setScale(2,
							RoundingMode.HALF_DOWN);
					pgRefundSurcharge = pgRefundSurcharge.add(pgRefundSurchargeObj).setScale(2, RoundingMode.HALF_DOWN);
					acquirerRefundSurcharge = acquirerRefundSurcharge.add(acquirerRefundSurchargeObj).setScale(2,
							RoundingMode.HALF_DOWN);
					pgRefundGst = pgRefundGst.add(pgRefundGstObj).setScale(2, RoundingMode.HALF_DOWN);
					acquirerRefundGst = acquirerRefundGst.add(acquirerRefundGstObj).setScale(2, RoundingMode.HALF_DOWN);

					// totalMerchantAmount =
					// totalMerchantAmount.add(refundSettledAmountObj);

				}

			}

			if (payId.equalsIgnoreCase("ALL")) {
				analyticsData.setMerchantName(payId);
			} else {
				analyticsData.setMerchantName(userDao.findPayId(payId).getBusinessName());
			}

			merchantSaleCapturedAmount = saleCapturedAmount
					.subtract(pgSaleSurcharge.add(acquirerSaleSurcharge).add(pgSaleGst).add(acquirerSaleGst))
					.setScale(2, RoundingMode.HALF_DOWN);
			merchantRefundCapturedAmount = refundCapturedAmount
					.subtract(pgRefundSurcharge.add(acquirerRefundSurcharge).add(pgRefundGst).add(acquirerRefundGst))
					.setScale(2, RoundingMode.HALF_DOWN);

			analyticsData.setSaleCapturedAmount(String.valueOf(saleCapturedAmount));
			analyticsData.setPgSaleSurcharge(String.valueOf(pgSaleSurcharge.setScale(2, RoundingMode.HALF_DOWN)));
			analyticsData.setAcquirerSaleSurcharge(
					String.valueOf(acquirerSaleSurcharge.setScale(2, RoundingMode.HALF_DOWN)));
			analyticsData.setPgSaleGst(String.valueOf(pgSaleGst.setScale(2, RoundingMode.HALF_DOWN)));
			analyticsData.setAcquirerSaleGst(String.valueOf(acquirerSaleGst.setScale(2, RoundingMode.HALF_DOWN)));
			// analyticsData.setGst(String.valueOf(saleGst.setScale(2,
			// RoundingMode.HALF_DOWN)));

			analyticsData
					.setRefundCapturedAmount(String.valueOf(refundCapturedAmount.setScale(2, RoundingMode.HALF_DOWN)));
			analyticsData.setPgRefundSurcharge(String.valueOf(pgRefundSurcharge.setScale(2, RoundingMode.HALF_DOWN)));
			analyticsData.setAcquirerRefundSurcharge(
					String.valueOf(acquirerRefundSurcharge.setScale(2, RoundingMode.HALF_DOWN)));
			analyticsData.setPgRefundGst(String.valueOf(pgRefundGst.setScale(2, RoundingMode.HALF_DOWN)));
			analyticsData.setAcquirerRefundGst(String.valueOf(acquirerRefundGst.setScale(2, RoundingMode.HALF_DOWN)));
			// analyticsData.setGst(String.valueOf(refundGst.setScale(2,
			// RoundingMode.HALF_DOWN)));

			totalMerchantAmount = merchantSaleCapturedAmount
					.subtract(merchantRefundCapturedAmount.setScale(2, RoundingMode.HALF_DOWN));
			// total amnt including gst
			analyticsData.setpaymentGatewayProfitInclGstCumm(String.valueOf(pgSaleSurcharge.add(pgSaleGst)
					.subtract(pgRefundSurcharge).subtract(pgRefundGst).setScale(2, RoundingMode.HALF_DOWN)));// total
																												// amt
			analyticsData.setTotalMerchantAmount(String.valueOf(totalMerchantAmount));

			analyticsData.setMerchantSaleSettledAmount(
					String.valueOf(merchantSaleCapturedAmount.setScale(2, RoundingMode.HALF_DOWN)));
			analyticsData.setMerchantRefundSettledAmount(
					String.valueOf(merchantRefundCapturedAmount.setScale(2, RoundingMode.HALF_DOWN)));

			double totalCapturedCount = ccCapturedCount + dcCapturedCount + upCapturedCount + nbCapturedCount
					+ wlCapturedCount + emCapturedCount + cdCapturedCount;
			double ccCapturedTxnPercent = 0;
			double dcCapturedTxnPercent = 0;
			double upCapturedTxnPercent = 0;
			double nbCapturedTxnPercent = 0;
			double wlCapturedTxnPercent = 0;
			double emCapturedTxnPercent = 0;
			double cdCapturedTxnPercent = 0;

			if (totalCapturedCount > 0) {
				ccCapturedTxnPercent = (ccCapturedCount / totalCapturedCount) * 100;
				dcCapturedTxnPercent = (dcCapturedCount / totalCapturedCount) * 100;
				upCapturedTxnPercent = (upCapturedCount / totalCapturedCount) * 100;
				nbCapturedTxnPercent = (nbCapturedCount / totalCapturedCount) * 100;
				wlCapturedTxnPercent = (wlCapturedCount / totalCapturedCount) * 100;
				emCapturedTxnPercent = (emCapturedCount / totalCapturedCount) * 100;
				cdCapturedTxnPercent = (cdCapturedCount / totalCapturedCount) * 100;
			}

			BigDecimal avgCapturedAmount = new BigDecimal(0);

			if (totalCapturedCount > 0) {

				BigDecimal totalCapturedCountBD = new BigDecimal(totalCapturedCount).setScale(2,
						RoundingMode.HALF_DOWN);
				avgCapturedAmount = saleCapturedAmount.divide(totalCapturedCountBD, 2, RoundingMode.HALF_UP);
				avgCapturedAmount = avgCapturedAmount.setScale(2, RoundingMode.HALF_DOWN);
			} else {
				avgCapturedAmount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_DOWN);
			}

			analyticsData.setCcSettledPercentage(String.format("%.2f", ccCapturedTxnPercent));
			analyticsData.setDcSettledPercentage(String.format("%.2f", dcCapturedTxnPercent));
			analyticsData.setUpSettledPercentage(String.format("%.2f", upCapturedTxnPercent));
			analyticsData.setNbSettledPercentage(String.format("%.2f", nbCapturedTxnPercent));
			analyticsData.setWlSettledPercentage(String.format("%.2f", wlCapturedTxnPercent));
			analyticsData.setEmSettledPercentage(String.format("%.2f", emCapturedTxnPercent));
			analyticsData.setCdSettledPercentage(String.format("%.2f", cdCapturedTxnPercent));
			analyticsData.setAvgSettlementAmount(String.format("%.2f", avgCapturedAmount));

			List<BasicDBObject> deltaConditionsList = new ArrayList<BasicDBObject>();
			List<BasicDBObject> deltaConditionList = new ArrayList<BasicDBObject>();

			deltaConditionList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.RECO.getName()));
			deltaConditionList.add(new BasicDBObject(FieldType.STATUS.getName(), StatusType.RECONCILED.getName()));
			deltaConditionList.add(new BasicDBObject(FieldType.UDF6.getName(), Constants.Y.getValue()));

			BasicDBObject deltaSettledQuery = new BasicDBObject("$and", deltaConditionList);

			deltaConditionsList.add(deltaSettledQuery);
			BasicDBObject deltaConditionQuery = new BasicDBObject("$or", deltaConditionsList);

			List<BasicDBObject> allConditionQueryList1 = new ArrayList<BasicDBObject>();

			if (!acquirerQuery.isEmpty()) {
				allConditionQueryList1.add(acquirerQuery);
			}
			if (!dateQuery.isEmpty()) {
				allConditionQueryList1.add(dateQuery);
			}
			allConditionQueryList1.add(deltaConditionQuery);

			BasicDBObject allConditionQueryObj1 = new BasicDBObject("$and", allConditionQueryList1);

			List<BasicDBObject> finalList1 = new ArrayList<BasicDBObject>();

			if (!allParamQuery.isEmpty()) {
				finalList1.add(allParamQuery);
			}
			if (!allConditionQueryObj1.isEmpty()) {
				finalList1.add(allConditionQueryObj1);
			}

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns
					.getCollection(PropertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));

			BasicDBObject finalQuery1 = new BasicDBObject("$and", finalList1);

			double totalDelta = (double) coll.count(finalQuery1);

			MongoCursor<Document> cursor = coll.find(finalQuery1).iterator();

			// Remove all data from an earlier map
			while (cursor.hasNext()) {
				Document dbobj = cursor.next();
				if (StringUtils.isNotBlank(dbobj.getString(FieldType.TOTAL_AMOUNT.toString()))) {
					totalSettledAmountDelta = totalSettledAmountDelta
							.add(new BigDecimal(dbobj.getString(FieldType.TOTAL_AMOUNT.toString())));
				}
			}

			totalCapturedAmountActual = saleCapturedAmount.subtract(totalSettledAmountDelta);

			String totalCapturedAmountActualString = String
					.valueOf(totalCapturedAmountActual.setScale(2, RoundingMode.HALF_DOWN));

			analyticsData.setPostSettledTransactionCount(String.valueOf(totalDelta));
			analyticsData.setActualSettlementAmount(totalCapturedAmountActualString);

			return analyticsData;
		}

		catch (Exception e) {
			logger.error("Exception in transaction summary count service ", e);
		}
		return null;
	}

	public AnalyticsData findDetails(Document dbobj) {
		BigDecimal merchantGstAmount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_DOWN);
		BigDecimal acquirerGstAmount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_DOWN);
		TdrPojo tdrPojo = new TdrPojo();
		BigDecimal st = null;
		String bussinessType = null;
		String bussinessName = "";
		String payId = (dbobj.getString(FieldType.PAY_ID.toString()));
		if (!StringUtils.isBlank(payId)) {

			User user = new User();

			if (userMap.get(payId) != null) {
				user = userMap.get(payId);
			} else {
				user = userDao.findPayId(payId);
				userMap.put(payId, user);
			}

			if (StringUtils.isNotBlank(dbobj.getString(FieldType.POST_SETTLED_FLAG.toString()))) {
				if (dbobj.getString(FieldType.POST_SETTLED_FLAG.toString()).equalsIgnoreCase("Y")) {
					postSettledTransactionCount++;
				}
			}

			String amount = (dbobj.getString(FieldType.AMOUNT.toString()));
			String totalAmount = (dbobj.getString(FieldType.TOTAL_AMOUNT.toString()));
			bussinessType = user.getIndustryCategory();
			bussinessName = user.getBusinessName();

			st = new BigDecimal(PropertiesManager.propertiesMap.get("SERVICE_TAX"));
			st = st.setScale(2, RoundingMode.HALF_DOWN);

			if (!StringUtils.isBlank(dbobj.getString(FieldType.SURCHARGE_FLAG.toString()))) {

				if (dbobj.getString(FieldType.SURCHARGE_FLAG.toString()).equals("Y")) {
					String txnAmount = amount;
					String surchargeAmount = totalAmount;
					BigDecimal nettxnAmount = new BigDecimal(txnAmount);
					PaymentType paymentType = PaymentType
							.getInstanceUsingCode(dbobj.getString(FieldType.PAYMENT_TYPE.toString()));
					if (paymentType == null) {
						return null;
					}

					AcquirerType acquirerType = AcquirerType
							.getInstancefromCode(dbobj.getString(FieldType.ACQUIRER_TYPE.toString()));

					if (acquirerType == null) {
						return null;
					}

					MopType mopType = MopType.getmop(dbobj.getString(FieldType.MOP_TYPE.toString()));

					if (mopType == null) {
						return null;
					}

					String paymentsRegion = (dbobj.getString(FieldType.PAYMENTS_REGION.toString()));

					if (paymentsRegion == null) {
						paymentsRegion = AccountCurrencyRegion.DOMESTIC.toString();
					}

					StringBuilder surchargeIdentifier = new StringBuilder();
					surchargeIdentifier.append(payId);
					surchargeIdentifier.append(paymentType.getName());
					surchargeIdentifier.append(acquirerType.getName());
					surchargeIdentifier.append(mopType.getName());
					surchargeIdentifier.append(paymentsRegion);

					Date surchargeStartDate = null;
					Date surchargeEndDate = null;
					Date settlementDate = null;
					Surcharge surcharge = new Surcharge();
					String transactionRegion = null;
					if (StringUtils.isNotBlank(dbobj.getString(FieldType.PAYMENTS_REGION.toString()))) {
						transactionRegion = dbobj.getString(FieldType.PAYMENTS_REGION.toString());
					} else {
						transactionRegion = AccountCurrencyRegion.DOMESTIC.name();
					}
					SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH);
					try {
						for (Surcharge surchargeData : surchargeList) {

							if (AcquirerType.getInstancefromName(surchargeData.getAcquirerName()).toString()
									.equalsIgnoreCase(dbobj.getString(FieldType.ACQUIRER_TYPE.toString()))
									&& surchargeData.getPaymentType().getCode()
											.equalsIgnoreCase(dbobj.getString(FieldType.PAYMENT_TYPE.toString()))
									&& surchargeData.getMopType().getCode()
											.equalsIgnoreCase(dbobj.getString(FieldType.MOP_TYPE.toString()))
									&& surchargeData.getPaymentsRegion().name().equalsIgnoreCase(transactionRegion)
									&& surchargeData.getPayId()
											.equalsIgnoreCase(dbobj.getString(FieldType.PAY_ID.toString()))) {

								surchargeStartDate = format.parse(surchargeData.getCreatedDate().toString());
								surchargeEndDate = format.parse(surchargeData.getUpdatedDate().toString());
								if (surchargeStartDate.compareTo(surchargeEndDate) == 0) {
									surchargeEndDate = new Date();
								}

								settlementDate = format.parse(dbobj.getString(FieldType.CREATE_DATE.toString()));

								if (settlementDate.compareTo(surchargeStartDate) >= 0
										&& settlementDate.compareTo(surchargeEndDate) <= 0) {
									surcharge = surchargeData;
									break;
								} else {
									continue;
								}

							}
						}
					} catch (Exception e) {
						logger.error("Exception ", e);
					}
					BigDecimal bankSurchargeFC;
					BigDecimal bankSurchargePercent;

					if (StringUtils.isBlank(dbobj.getString(FieldType.CARD_HOLDER_TYPE.toString()))) {
						bankSurchargeFC = surcharge.getBankSurchargeAmountCustomer();
						bankSurchargePercent = surcharge.getBankSurchargePercentageCustomer();
					}

					else if ((dbobj.getString(FieldType.CARD_HOLDER_TYPE.toString()))
							.equalsIgnoreCase(CardHolderType.CONSUMER.toString())) {
						bankSurchargeFC = surcharge.getBankSurchargeAmountCustomer();
						bankSurchargePercent = surcharge.getBankSurchargePercentageCustomer();
					} else {
						bankSurchargeFC = surcharge.getBankSurchargeAmountCommercial();
						bankSurchargePercent = surcharge.getBankSurchargePercentageCommercial();
					}

					BigDecimal netsurchargeAmount = new BigDecimal(surchargeAmount);

					BigDecimal netcalculatedSurcharge = netsurchargeAmount.subtract(nettxnAmount);
					netcalculatedSurcharge = netcalculatedSurcharge.setScale(2, RoundingMode.HALF_DOWN);

					BigDecimal gstCalculate = netcalculatedSurcharge.multiply(st).divide(((ONE_HUNDRED).add(st)), 2,
							RoundingMode.HALF_DOWN);

					BigDecimal pgSurchargeAmount;
					BigDecimal acquirerSurchargeAmount;

					if (netcalculatedSurcharge.equals(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_DOWN))) {
						pgSurchargeAmount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_DOWN);
						acquirerSurchargeAmount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_DOWN);
					}

					else {
						acquirerSurchargeAmount = nettxnAmount.multiply(bankSurchargePercent).divide(((ONE_HUNDRED)), 2,
								RoundingMode.HALF_DOWN);
						acquirerSurchargeAmount = acquirerSurchargeAmount.add(bankSurchargeFC);

						pgSurchargeAmount = netcalculatedSurcharge.subtract(acquirerSurchargeAmount);
						pgSurchargeAmount = pgSurchargeAmount.subtract(gstCalculate);
						pgSurchargeAmount = pgSurchargeAmount.setScale(2, RoundingMode.HALF_DOWN);

					}

					BigDecimal totalSurcharge = netcalculatedSurcharge.subtract(gstCalculate);
					BigDecimal totalAmtPaytoMerchant = netsurchargeAmount.subtract(gstCalculate.add(totalSurcharge));

					acquirerGstAmount = acquirerSurchargeAmount.multiply(st).divide(ONE_HUNDRED, 2,
							RoundingMode.HALF_DOWN);

					merchantGstAmount = pgSurchargeAmount.multiply(st).divide(ONE_HUNDRED, 2, RoundingMode.HALF_DOWN);

					String gstCalculateString = String.valueOf(gstCalculate);
					String totalSurchargeString = String.valueOf(totalSurcharge);
					String totalAmtPaytoMerchantString = String.valueOf(totalAmtPaytoMerchant);
					tdrPojo.setTotalAmtPaytoMerchant(totalAmtPaytoMerchantString);
					tdrPojo.setTotalGstOnMerchant(gstCalculateString);
					tdrPojo.setNetMerchantPayableAmount(totalAmtPaytoMerchantString);
					tdrPojo.setMerchantTdrCalculate(totalSurchargeString);
					tdrPojo.setTotalAmount(surchargeAmount);
					tdrPojo.setAcquirerSurchargeAmount(String.valueOf(acquirerSurchargeAmount));
					tdrPojo.setPgSurchargeAmount(String.valueOf(pgSurchargeAmount));

					AnalyticsData analyticsDataCountSearchObj = new AnalyticsData();
					analyticsDataCountSearchObj.setGst(String.valueOf(st));

					if (dbobj.getString(FieldType.TXNTYPE.toString()).equalsIgnoreCase(TransactionType.RECO.getName())
							|| dbobj.getString(FieldType.TXNTYPE.toString())
									.equalsIgnoreCase(TransactionType.SALE.getName())) {

						analyticsDataCountSearchObj
								.setPaymentMethod(dbobj.getString(FieldType.PAYMENT_TYPE.toString()));
						analyticsDataCountSearchObj.setTxnType(TransactionType.SALE.getName());
						analyticsDataCountSearchObj.setSaleCapturedAmount(tdrPojo.getTotalAmount());
						analyticsDataCountSearchObj.setPgSaleSurcharge(tdrPojo.getPgSurchargeAmount());
						analyticsDataCountSearchObj.setAcquirerSaleSurcharge(tdrPojo.getAcquirerSurchargeAmount());
						analyticsDataCountSearchObj.setPgSaleGst(String.valueOf(merchantGstAmount));
						analyticsDataCountSearchObj.setAcquirerSaleGst(String.valueOf(acquirerGstAmount));
					} else if (dbobj.getString(FieldType.TXNTYPE.toString())
							.equalsIgnoreCase(TransactionType.REFUNDRECO.getName())
							|| dbobj.getString(FieldType.TXNTYPE.toString())
									.equalsIgnoreCase(TransactionType.REFUND.getName())) {
						{

							analyticsDataCountSearchObj
									.setPaymentMethod(dbobj.getString(FieldType.PAYMENT_TYPE.toString()));
							analyticsDataCountSearchObj.setTxnType(TransactionType.REFUND.getName());
							analyticsDataCountSearchObj.setRefundCapturedAmount(tdrPojo.getTotalAmount());
							analyticsDataCountSearchObj.setPgRefundSurcharge(tdrPojo.getPgSurchargeAmount());
							analyticsDataCountSearchObj
									.setAcquirerRefundSurcharge(tdrPojo.getAcquirerSurchargeAmount());
							analyticsDataCountSearchObj.setPgRefundGst(String.valueOf(merchantGstAmount));
							analyticsDataCountSearchObj.setAcquirerRefundGst(String.valueOf(acquirerGstAmount));
						}

					}

					return analyticsDataCountSearchObj;
				}
			}

			else {
				// Get TDR Mode report values here
			}

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

			// totalTxnAmount = totalTxnAmount.setScale(2,
			// RoundingMode.HALF_DOWN);
			AnalyticsData analyticsData = new AnalyticsData();

			double totalTxnSuccess = ccSuccess + dcSuccess + upSuccess + nbSuccess + wlSuccess + emSuccess + cdSuccess;
			double totalFailed = ccFailed + dcFailed + upFailed + nbFailed + wlFailed + emFailed + cdFailed;

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
				analyticsData.setEMTxnPercent("0.00");
				analyticsData.setWLTxnPercent("0.00");
				analyticsData.setCDTxnPercent("0.00");
			}

			else {

				double cCTxnPercent = (ccSuccess / totalTxnSuccess) * 100;
				double dCTxnPercent = (dcSuccess / totalTxnSuccess) * 100;
				double upTxnPercent = (upSuccess / totalTxnSuccess) * 100;
				double nbTxnPercent = (nbSuccess / totalTxnSuccess) * 100;
				double wlTxnPercent = (wlSuccess / totalTxnSuccess) * 100;
				double emTxnPercent = (emSuccess / totalTxnSuccess) * 100;
				double cdTxnPercent = (cdSuccess / totalTxnSuccess) * 100;

				analyticsData.setCCTxnPercent(String.format("%.2f", cCTxnPercent));
				analyticsData.setDCTxnPercent(String.format("%.2f", dCTxnPercent));
				analyticsData.setUPTxnPercent(String.format("%.2f", upTxnPercent));
				analyticsData.setNBTxnPercent(String.format("%.2f", nbTxnPercent));
				analyticsData.setEMTxnPercent(String.format("%.2f", emTxnPercent));
				analyticsData.setWLTxnPercent(String.format("%.2f", wlTxnPercent));
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

			if (nbSuccess + nbFailed < 1) {
				analyticsData.setNBSuccessRate("0.00");
			} else {
				double nBSuccessRate = (nbSuccess / (nbSuccess + nbFailed)) * 100;
				analyticsData.setNBSuccessRate(String.format("%.2f", nBSuccessRate));
			}
			if (wlSuccess + wlFailed < 1) {
				analyticsData.setWLSuccessRate("0.00");
			} else {
				double wLSuccessRate = (wlSuccess / (wlSuccess + wlFailed)) * 100;
				analyticsData.setWLSuccessRate(String.format("%.2f", wLSuccessRate));
			}
			if (emSuccess + emFailed < 1) {
				analyticsData.setEMSuccessRate("0.00");
			} else {
				double eMSuccessRate = (emSuccess / (emSuccess + emFailed)) * 100;
				analyticsData.setEMSuccessRate(String.format("%.2f", eMSuccessRate));
			}
			if (cdSuccess + cdFailed < 1) {
				analyticsData.setCDSuccessRate("0.00");
			} else {
				double cDSuccessRate = (cdSuccess / (cdSuccess + cdFailed)) * 100;
				analyticsData.setCDSuccessRate(String.format("%.2f", cDSuccessRate));
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

			} else {

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
			analyticsData.setTotalUPTxnAmount(String.format("%.2f", totalNBTxnAmount));
			analyticsData.setTotalUPTxnAmount(String.format("%.2f", totalWLTxnAmount));
			analyticsData.setTotalUPTxnAmount(String.format("%.2f", totalEMTxnAmount));
			analyticsData.setTotalUPTxnAmount(String.format("%.2f", totalCDTxnAmount));

			analyticsData.setTotalCCCapturedCount(String.format("%.0f", ccSuccess));
			analyticsData.setTotalDCCapturedCount(String.format("%.0f", dcSuccess));
			analyticsData.setTotalUPCapturedCount(String.format("%.0f", upSuccess));
			analyticsData.setTotalUPCapturedCount(String.format("%.0f", nbSuccess));
			analyticsData.setTotalUPCapturedCount(String.format("%.0f", wlSuccess));
			analyticsData.setTotalUPCapturedCount(String.format("%.0f", emSuccess));
			analyticsData.setTotalUPCapturedCount(String.format("%.0f", cdSuccess));

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
			logger.error("Exception in transaction summary count service ", e);
		}
		return null;
	}

}
