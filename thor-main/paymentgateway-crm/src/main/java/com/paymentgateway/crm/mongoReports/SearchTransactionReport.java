package com.paymentgateway.crm.mongoReports;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.paymentgateway.commons.dao.UserSettingDao;
import com.paymentgateway.commons.mongo.MongoInstance;
import com.paymentgateway.commons.user.SearchTransaction;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserSettingData;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CrmFieldConstants;
import com.paymentgateway.commons.util.DataEncDecTool;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.MopType;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TransactionType;

@Component
public class SearchTransactionReport {

	private static Logger logger = LoggerFactory.getLogger(SearchTransactionReport.class.getName());

	@Autowired
	private UserDao userdao;

	@Autowired
	PropertiesManager propertiesManager;

	@Autowired
	private TxnReports txnReports;

	@Autowired
	private MongoInstance mongoInstance;

	@Autowired
	private DataEncDecTool dataEncDecTool;

	@Autowired
	private UserSettingDao userSettingDao;

	private static final String prefix = "MONGO_DB_";

	public List<SearchTransaction> searchPayment(String orderId, String pgRefNum, User sessionUser, String consumerNo,
			String rrn, String acqId) {
		List<SearchTransaction> transactionList = new ArrayList<SearchTransaction>();

		try {
			String merchantPayId = "";
			String subMerchantPayId = "";

			UserSettingData merchantSettings = userSettingDao.fetchDataUsingPayId(sessionUser.getPayId());

			BasicDBObject andQuery = new BasicDBObject();
			List<BasicDBObject> obj = new ArrayList<BasicDBObject>();

			if (!(sessionUser.getUserType().equals(UserType.ADMIN)
					|| sessionUser.getUserType().equals(UserType.SUBADMIN)
					|| sessionUser.getUserType().equals(UserType.AGENT))) {

				if (sessionUser.getUserType().equals(UserType.SUBUSER)) {

					User user = userdao.findPayId(sessionUser.getParentPayId());

					if (user.isSuperMerchant() == false && StringUtils.isNotBlank(user.getSuperMerchantId())) {
						merchantPayId = user.getSuperMerchantId();
						subMerchantPayId = user.getPayId();
					} else {
						merchantPayId = user.getPayId();
					}

					if (!StringUtils.isEmpty(sessionUser.getSubUserType())
							&& sessionUser.getSubUserType().equalsIgnoreCase("ePosType")) {

						String subUserId = "";
						String txnOrderId = "";

						if (!userdao.isSubUserPrevilageTypeAll(sessionUser)) {
							subUserId = sessionUser.getPayId();
						}

						Set<String> orderIdSet = txnReports.findBySubuserId(subUserId, sessionUser.getParentPayId());

						for (String eposTxnOrderid : orderIdSet) {
							if (eposTxnOrderid.equalsIgnoreCase(orderId)) {
								if (!StringUtils.isBlank(orderId)) {
									txnOrderId = orderId;
									break;
								}
							}
						}
						if (!txnOrderId.isEmpty()) {
							obj.add(new BasicDBObject(FieldType.ORDER_ID.getName(), orderId));
						} else {
							if (!orderId.isEmpty())
								obj.add(new BasicDBObject(FieldType.ORDER_ID.getName(), " "));
						}
						boolean isPgfNumber = txnReports.getPgfNumberForeposSubuser(orderIdSet, pgRefNum);

						if (isPgfNumber) {
							obj.add(new BasicDBObject(FieldType.PG_REF_NUM.getName(), pgRefNum));
						} else {
							if (!pgRefNum.isEmpty())
								obj.add(new BasicDBObject(FieldType.PG_REF_NUM.getName(), " "));
						}

						if (StringUtils.isNotBlank(rrn)) {
							obj.add(new BasicDBObject(FieldType.RRN.getName(), rrn));
						}
						if (StringUtils.isNotBlank(consumerNo)) {
							obj.add(new BasicDBObject(FieldType.CUST_ID.getName(), consumerNo));
						}

						if (!StringUtils.isBlank(merchantPayId)) {
							obj.add(new BasicDBObject(FieldType.PAY_ID.getName(), merchantPayId));
						}
						if (!StringUtils.isBlank(subMerchantPayId)) {
							obj.add(new BasicDBObject(FieldType.SUB_MERCHANT_ID.getName(), subMerchantPayId));
						}
						sessionUser = user; // userdao.findPayId(sessionUser.getParentPayId());

					} else if (!StringUtils.isEmpty(sessionUser.getSubUserType())
							&& sessionUser.getSubUserType().equalsIgnoreCase("normalType")) {

						if (!StringUtils.isBlank(orderId)) {
							obj.add(new BasicDBObject(FieldType.ORDER_ID.getName(), orderId));
						}
						if (!StringUtils.isBlank(pgRefNum)) {
							obj.add(new BasicDBObject(FieldType.PG_REF_NUM.getName(), pgRefNum));
						}
						if (StringUtils.isNotBlank(rrn)) {
							obj.add(new BasicDBObject(FieldType.RRN.getName(), rrn));
						}
						if (StringUtils.isNotBlank(consumerNo)) {
							obj.add(new BasicDBObject(FieldType.CUST_ID.getName(), consumerNo));
						}
						if (!StringUtils.isBlank(merchantPayId)) {
							obj.add(new BasicDBObject(FieldType.PAY_ID.getName(), merchantPayId));
						}
						if (!StringUtils.isBlank(subMerchantPayId)) {
							obj.add(new BasicDBObject(FieldType.SUB_MERCHANT_ID.getName(), subMerchantPayId));
						}
					}
				} else {
					if (!StringUtils.isBlank(orderId)) {
						obj.add(new BasicDBObject(FieldType.ORDER_ID.getName(), orderId));
					}
					if (!StringUtils.isBlank(pgRefNum)) {
						obj.add(new BasicDBObject(FieldType.PG_REF_NUM.getName(), pgRefNum));
					}
					if (StringUtils.isNotBlank(rrn)) {
						obj.add(new BasicDBObject(FieldType.RRN.getName(), rrn));
					}
					if (StringUtils.isNotBlank(consumerNo)) {
						obj.add(new BasicDBObject(FieldType.CUST_ID.getName(), consumerNo));
					}
					if (!sessionUser.isSuperMerchant() && StringUtils.isNotBlank(sessionUser.getSuperMerchantId())) {
						obj.add(new BasicDBObject(FieldType.PAY_ID.getName(), sessionUser.getSuperMerchantId()));
						obj.add(new BasicDBObject(FieldType.SUB_MERCHANT_ID.getName(), sessionUser.getPayId()));
					} else {
						obj.add(new BasicDBObject(FieldType.PAY_ID.getName(), sessionUser.getPayId()));
					}
				}
			} else {
				if (!StringUtils.isBlank(orderId)) {
					obj.add(new BasicDBObject(FieldType.ORDER_ID.getName(), orderId));
				}
				if (!StringUtils.isBlank(pgRefNum)) {
					obj.add(new BasicDBObject(FieldType.PG_REF_NUM.getName(), pgRefNum));
				}
				if (StringUtils.isNotBlank(rrn)) {
					obj.add(new BasicDBObject(FieldType.RRN.getName(), rrn));
				}
				if (StringUtils.isNotBlank(consumerNo)) {
					obj.add(new BasicDBObject(FieldType.CUST_ID.getName(), consumerNo));
				}
				if (StringUtils.isNotBlank(acqId)) {
					obj.add(new BasicDBObject(FieldType.ACQ_ID.getName(), acqId));
				}
			}

			andQuery.put("$and", obj);

			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> coll = dbIns
					.getCollection(propertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));
			MongoCursor<Document> cursor = coll.find(andQuery)
					.sort(new BasicDBObject(FieldType.CREATE_DATE.getName(), -1)).iterator();

			while (cursor.hasNext()) {
				Document doc = cursor.next();

				if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
						&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
					doc = dataEncDecTool.decryptDocument(doc);
				}

				SearchTransaction searchTxn = new SearchTransaction();
				User user = new User();
				user = userdao.findPayId(doc.getString(FieldType.PAY_ID.toString()));
				BigInteger txnID = new BigInteger(doc.getString(FieldType.TXN_ID.toString()));
				searchTxn.setPayId(doc.getString(FieldType.PAY_ID.toString()));
				searchTxn.setTransactionId((txnID));
				searchTxn.setTxnId(doc.getString(FieldType.TXN_ID.toString()));
				searchTxn.setPgRefNum(doc.getString(FieldType.PG_REF_NUM.toString()));
				searchTxn.setMerchant(user.getBusinessName());
				searchTxn.setOrderId(doc.getString(FieldType.ORDER_ID.toString()));
				searchTxn.settDate(doc.getString(FieldType.CREATE_DATE.toString()));

				if (StringUtils.isNotBlank(doc.getString(FieldType.SUB_MERCHANT_ID.getName()))) {
					searchTxn.setSubMerchantId(
							userdao.getBusinessNameByPayId(doc.getString(FieldType.SUB_MERCHANT_ID.getName())));
				}

				if (StringUtils.isBlank(doc.getString(FieldType.PAYMENT_TYPE.toString()))) {
					searchTxn.setPaymentType(CrmFieldConstants.NA.toString());
				} else {
					searchTxn.setPaymentType(doc.getString(FieldType.PAYMENT_TYPE.toString()));
				}

				if (StringUtils.isBlank(doc.getString(FieldType.ACQUIRER_MODE.toString()))) {
					searchTxn.setAcquirerMode(CrmFieldConstants.NA.toString());
				} else {
					searchTxn.setAcquirerMode(doc.getString(FieldType.ACQUIRER_MODE.toString()));
				}

				if (StringUtils.isNotBlank(doc.getString(FieldType.TXN_CAPTURE_FLAG.getName()))) {
					searchTxn.setTxnSettledType(doc.getString(FieldType.TXN_CAPTURE_FLAG.getName()));
				} else {
					searchTxn.setTxnSettledType(CrmFieldConstants.NA.getValue());
				}

				if (StringUtils.isBlank(doc.getString(FieldType.MOP_TYPE.toString()))) {
					searchTxn.setMopType(CrmFieldConstants.NA.toString());
				} else {
					searchTxn.setMopType(MopType.getmopName(doc.getString(FieldType.MOP_TYPE.toString())));
				}
				if (doc.getString(FieldType.TXNTYPE.toString()).equals(TransactionType.RECO.getName())) {
					searchTxn.setTxnType(TransactionType.SALE.getName());
				} else if (doc.getString(FieldType.TXNTYPE.toString()).equals(TransactionType.REFUNDRECO.getName())) {
					searchTxn.setTxnType(TransactionType.REFUND.getName());
				} else {
					searchTxn.setTxnType(doc.getString(FieldType.TXNTYPE.toString()));
				}

				if (StringUtils.isBlank(doc.getString(FieldType.CARD_MASK.toString()))) {
					searchTxn.setCardNum(CrmFieldConstants.NA.toString());
				} else {
					searchTxn.setCardNum(doc.getString(FieldType.CARD_MASK.toString()));
				}

				if (StringUtils.isBlank(doc.getString(FieldType.UDF10.toString()))) {
					searchTxn.setUdf10(CrmFieldConstants.NA.toString());
				} else {
					searchTxn.setUdf10(doc.getString(FieldType.UDF10.toString()));
				}

				if (((sessionUser.getUserType().equals(UserType.MERCHANT))
						|| (sessionUser.getUserType().equals(UserType.SUBUSER)))
						&& merchantSettings.isCustomTransactionStatus()) {
					if (((doc.getString(FieldType.STATUS.toString())).equalsIgnoreCase(StatusType.TIMEOUT.getName()))
							|| ((doc.getString(FieldType.STATUS.toString()))
									.equalsIgnoreCase(StatusType.CAPTURED.getName()))
							|| ((doc.getString(FieldType.STATUS.toString()))
									.equalsIgnoreCase(StatusType.ENROLLED.getName()))
							|| ((doc.getString(FieldType.STATUS.toString()))
									.equalsIgnoreCase(StatusType.SETTLED.getName()))
							|| ((doc.getString(FieldType.STATUS.toString()))
									.equalsIgnoreCase(StatusType.SENT_TO_BANK.getName()))
							|| ((doc.getString(FieldType.STATUS.toString()))
									.equalsIgnoreCase(StatusType.PENDING.getName()))) {
						searchTxn.setStatus(doc.getString(FieldType.STATUS.toString()));
					} else {
						searchTxn.setStatus("Failed");
					}
				} else {
					searchTxn.setStatus(doc.getString(FieldType.STATUS.toString()));
				}

				searchTxn.setAmount(doc.getString(FieldType.AMOUNT.toString()));
				searchTxn.setTotalAmount(doc.getString(FieldType.TOTAL_AMOUNT.toString()));
				if (StringUtils.isBlank(doc.getString(FieldType.CUST_NAME.toString()))) {
					searchTxn.setCustName(CrmFieldConstants.NA.toString());
				} else {
					searchTxn.setCustName(doc.getString(FieldType.CUST_NAME.toString()));
				}

				if (StringUtils.isBlank(doc.getString(FieldType.RRN.toString()))) {
					searchTxn.setRrn(CrmFieldConstants.NA.toString());
				} else {
					searchTxn.setRrn(doc.getString(FieldType.RRN.toString()));
				}

				if (StringUtils.isBlank(doc.getString(FieldType.ACQ_ID.toString()))) {
					searchTxn.setAcqId(CrmFieldConstants.NA.toString());
				} else {
					searchTxn.setAcqId(doc.getString(FieldType.ACQ_ID.toString()));
				}

				if (StringUtils.isBlank(doc.getString(FieldType.RESPONSE_MESSAGE.toString()))) {
					searchTxn.setPgResponseMessage(CrmFieldConstants.NA.toString());
				} else {
					searchTxn.setPgResponseMessage(doc.getString(FieldType.RESPONSE_MESSAGE.toString()));
				}

				if (StringUtils.isBlank(doc.getString(FieldType.PG_TXN_MESSAGE.toString()))) {
					searchTxn.setAcquirerTxnMessage(CrmFieldConstants.NA.toString());
				} else {
					searchTxn.setAcquirerTxnMessage(doc.getString(FieldType.PG_TXN_MESSAGE.toString()));
				}
				if (StringUtils.isBlank(doc.getString(FieldType.REFUND_ORDER_ID.toString()))) {
					searchTxn.setRefund_txn_id(CrmFieldConstants.NA.toString());
				} else {
					searchTxn.setRefund_txn_id(doc.getString(FieldType.REFUND_ORDER_ID.toString()));
				}

				if (StringUtils.isBlank(doc.getString(FieldType.ACQUIRER_TDR_SC.toString()))) {
					searchTxn.setRefund_txn_id(CrmFieldConstants.NA.toString());
				} else {
					searchTxn.setACQUIRER_TDR_SC(doc.getString(FieldType.ACQUIRER_TDR_SC.toString()));
				}

				if (StringUtils.isBlank(doc.getString((FieldType.ACQUIRER_GST.toString())))) {
					searchTxn.setRefund_txn_id(CrmFieldConstants.NA.toString());
				} else {
					searchTxn.setACQUIRER_GST(doc.getString(FieldType.ACQUIRER_GST.toString()));
				}

				if (StringUtils.isBlank(doc.getString((FieldType.PG_GST.toString())))) {
					searchTxn.setRefund_txn_id(CrmFieldConstants.NA.toString());
				} else {
					searchTxn.setPG_GST(doc.getString(FieldType.PG_GST.toString()));

				}

				if (StringUtils.isBlank(doc.getString(FieldType.PG_TDR_SC.toString()))) {
					searchTxn.setRefund_txn_id(CrmFieldConstants.NA.toString());
				} else {
					searchTxn.setPG_TDR_SC(doc.getString(FieldType.PG_TDR_SC.toString()));

				}

				if (doc.containsKey(FieldType.RESELLER_CHARGES.getName())
						&& StringUtils.isBlank(doc.getString(FieldType.RESELLER_CHARGES.getName()))) {
					searchTxn.setResellerCharges(CrmFieldConstants.NA.getValue());
				} else {
					searchTxn.setResellerCharges(doc.getString(FieldType.RESELLER_CHARGES.getName()));
				}

				if (doc.containsKey(FieldType.RESELLER_GST.getName())
						&& StringUtils.isBlank(doc.getString(FieldType.RESELLER_GST.getName()))) {
					searchTxn.setResellerGst(CrmFieldConstants.NA.getValue());
				} else {
					searchTxn.setResellerGst(doc.getString(FieldType.RESELLER_GST.getName()));
				}

				if (StringUtils.isBlank(doc.getString(FieldType.ACQUIRER_TYPE.toString()))) {
					searchTxn.setRefund_txn_id(CrmFieldConstants.NA.toString());
					searchTxn.setAcquirerType(CrmFieldConstants.NA.toString());

				} else {
					searchTxn.setAcquirerType(doc.getString(FieldType.ACQUIRER_TYPE.toString()));
				}

				if (StringUtils.isBlank(doc.getString(FieldType.PAYMENTS_REGION.toString()))) {
					searchTxn.setPayment_Region(CrmFieldConstants.NA.toString());

				} else {
					searchTxn.setPayment_Region(doc.getString(FieldType.PAYMENTS_REGION.toString()));
				}

				if (StringUtils.isBlank(doc.getString(FieldType.CARD_HOLDER_TYPE.toString()))) {
					searchTxn.setCard_Holder_Type(CrmFieldConstants.NA.toString());

				} else {
					searchTxn.setCard_Holder_Type(doc.getString(FieldType.CARD_HOLDER_TYPE.toString()));
				}

				if (StringUtils.isBlank(doc.getString(FieldType.RESPONSE_CODE.toString()))) {
					searchTxn.setResponseCode(CrmFieldConstants.NA.toString());
				} else {
					searchTxn.setResponseCode(doc.getString(FieldType.RESPONSE_CODE.toString()));
				}

				if (StringUtils.isBlank(doc.getString(FieldType.CUST_ID.toString()))) {
					searchTxn.setConsumerNo("Consumer No Not found");
				} else {
					searchTxn.setConsumerNo(doc.getString(FieldType.CUST_ID.toString()));
				}

				if (StringUtils.isNotBlank(doc.getString(FieldType.PG_TDR_SC.getName()))
						&& StringUtils.isNotBlank(doc.getString(FieldType.ACQUIRER_TDR_SC.getName()))
						&& StringUtils.isNotBlank(doc.getString(FieldType.RESELLER_CHARGES.getName()))) {

					BigDecimal acqTdrSc = new BigDecimal(doc.getString(FieldType.ACQUIRER_TDR_SC.getName()));
					BigDecimal pgTdrSc = new BigDecimal(doc.getString(FieldType.PG_TDR_SC.getName()));
					BigDecimal resellerTdrSc = new BigDecimal(doc.getString(FieldType.RESELLER_CHARGES.getName()));

					searchTxn.setTotalChargeTdrSc(String.valueOf(acqTdrSc.add(pgTdrSc).add(resellerTdrSc)));

				} else {
					searchTxn.setTotalChargeTdrSc("");
				}

				if (doc.containsKey(FieldType.RESELLER_CHARGES.getName())
						&& StringUtils.isNotBlank(doc.getString(FieldType.RESELLER_CHARGES.getName()))
						&& StringUtils.isNotBlank(searchTxn.getACQUIRER_TDR_SC())
						&& StringUtils.isNotBlank(searchTxn.getPG_TDR_SC())) {

					BigDecimal acquirerTdrScAmount = new BigDecimal(searchTxn.getACQUIRER_TDR_SC());
					BigDecimal pgTdrScAmount = new BigDecimal(searchTxn.getPG_TDR_SC());
					BigDecimal resellerTdrScAmount = new BigDecimal(
							doc.getString(FieldType.RESELLER_CHARGES.getName()));
					BigDecimal acquirerGst = new BigDecimal(doc.getString(FieldType.ACQUIRER_GST.getName()));
					BigDecimal pgGst = new BigDecimal(doc.getString(FieldType.PG_GST.getName()));
					BigDecimal resellerGst = new BigDecimal(doc.getString(FieldType.RESELLER_GST.getName()));

					searchTxn.setTotalTdrSc(String.valueOf(pgTdrScAmount.add(acquirerTdrScAmount)
							.add(resellerTdrScAmount).add(acquirerGst).add(pgGst).add(resellerGst)));

				} else {

					if (StringUtils.isNotBlank(searchTxn.getACQUIRER_TDR_SC())
							&& StringUtils.isNotBlank(searchTxn.getPG_TDR_SC())) {
						BigDecimal acquirerTdrScAmount = new BigDecimal(searchTxn.getACQUIRER_TDR_SC());
						BigDecimal pgTdrScAmount = new BigDecimal(searchTxn.getPG_TDR_SC());
						searchTxn.setTotalTdrSc(String.valueOf(pgTdrScAmount.add(acquirerTdrScAmount)));
					} else {
						searchTxn.setTotalTdrSc("");
					}
				}

				if (doc.containsKey(FieldType.RESELLER_GST.getName())
						&& StringUtils.isNotBlank(doc.getString(FieldType.RESELLER_GST.getName()))
						&& StringUtils.isNotBlank(searchTxn.getACQUIRER_GST())
						&& StringUtils.isNotBlank(searchTxn.getPG_GST())) {

					BigDecimal acquirerGstAmount = new BigDecimal(searchTxn.getACQUIRER_GST());
					BigDecimal pgGstAmount = new BigDecimal(searchTxn.getPG_GST());
					BigDecimal resellerGstAmount = new BigDecimal(doc.getString(FieldType.RESELLER_GST.getName()));
					searchTxn.setTotalGst(String.valueOf(acquirerGstAmount.add(pgGstAmount).add(resellerGstAmount)));

				} else {

					if (StringUtils.isNotBlank(searchTxn.getACQUIRER_GST())
							&& StringUtils.isNotBlank(searchTxn.getPG_GST())) {
						BigDecimal acquirerGstAmount = new BigDecimal(searchTxn.getACQUIRER_GST());
						BigDecimal pgGstAmount = new BigDecimal(searchTxn.getPG_GST());
						searchTxn.setTotalGst(String.valueOf(acquirerGstAmount.add(pgGstAmount)));
					} else {
						searchTxn.setTotalGst("");
					}
				}

				transactionList.add(searchTxn);
			}
			String capturedDate = "";

			for (SearchTransaction searchTransaction : transactionList) {
				if (searchTransaction.getStatus().equalsIgnoreCase(StatusType.CAPTURED.getName())) {
					capturedDate = searchTransaction.gettDate();
				}
			}

			if (StringUtils.isNotBlank(capturedDate) && StringUtils.isNotBlank(orderId)) {
				transactionList = new ArrayList<SearchTransaction>();
				merchantSettings = userSettingDao.fetchDataUsingPayId(sessionUser.getPayId());

				BasicDBObject andQuery1 = new BasicDBObject();
				List<BasicDBObject> obj1 = new ArrayList<BasicDBObject>();
				BasicDBObject dateQuery = new BasicDBObject();
				DateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.ENGLISH);
				DateFormat format1 = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
				Date date = format.parse(capturedDate);
				String dateFrom = format1.format(date) + " 00:00:00";
				String dateTo = capturedDate;
				dateQuery.put(FieldType.CREATE_DATE.getName(),
						BasicDBObjectBuilder.start("$gte", new SimpleDateFormat(dateFrom).toLocalizedPattern())
								.add("$lte", new SimpleDateFormat(dateTo).toLocalizedPattern()).get());
				obj1.add(new BasicDBObject(dateQuery));

				BasicDBObject dateIndexConditionQuery = new BasicDBObject();
				String startString = new SimpleDateFormat(dateFrom).toLocalizedPattern();
				String endString = new SimpleDateFormat(dateTo).toLocalizedPattern();

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
				dateIndexConditionQuery.append("DATE_INDEX", dateIndexIn);
				obj1.add(new BasicDBObject(dateIndexConditionQuery));

				if (!(sessionUser.getUserType().equals(UserType.ADMIN)
						|| sessionUser.getUserType().equals(UserType.SUBADMIN)
						|| sessionUser.getUserType().equals(UserType.AGENT))) {

					if (sessionUser.getUserType().equals(UserType.SUBUSER)) {

						User user = userdao.findPayId(sessionUser.getParentPayId());

						if (user.isSuperMerchant() == false && StringUtils.isNotBlank(user.getSuperMerchantId())) {
							merchantPayId = user.getSuperMerchantId();
							subMerchantPayId = user.getPayId();
						} else {
							merchantPayId = user.getPayId();
						}

						if (!StringUtils.isEmpty(sessionUser.getSubUserType())
								&& sessionUser.getSubUserType().equalsIgnoreCase("ePosType")) {

							String subUserId = "";
							String txnOrderId = "";

							if (!userdao.isSubUserPrevilageTypeAll(sessionUser)) {
								subUserId = sessionUser.getPayId();
							}

							Set<String> orderIdSet = txnReports.findBySubuserId(subUserId,
									sessionUser.getParentPayId());

							for (String eposTxnOrderid : orderIdSet) {
								if (eposTxnOrderid.equalsIgnoreCase(orderId)) {
									if (!StringUtils.isBlank(orderId)) {
										txnOrderId = orderId;
										break;
									}
								}
							}
							if (!txnOrderId.isEmpty()) {
								obj1.add(new BasicDBObject(FieldType.ORDER_ID.getName(), orderId));
							} else {
								if (!orderId.isEmpty())
									obj1.add(new BasicDBObject(FieldType.ORDER_ID.getName(), " "));
							}
							boolean isPgfNumber = txnReports.getPgfNumberForeposSubuser(orderIdSet, pgRefNum);

							if (isPgfNumber) {
								obj1.add(new BasicDBObject(FieldType.PG_REF_NUM.getName(), pgRefNum));
							} else {
								if (!pgRefNum.isEmpty())
									obj1.add(new BasicDBObject(FieldType.PG_REF_NUM.getName(), " "));
							}

							if (StringUtils.isNotBlank(rrn)) {
								obj1.add(new BasicDBObject(FieldType.RRN.getName(), rrn));
							}
							if (StringUtils.isNotBlank(consumerNo)) {
								obj1.add(new BasicDBObject(FieldType.CUST_ID.getName(), consumerNo));
							}

							if (!StringUtils.isBlank(merchantPayId)) {
								obj1.add(new BasicDBObject(FieldType.PAY_ID.getName(), merchantPayId));
							}
							if (!StringUtils.isBlank(subMerchantPayId)) {
								obj1.add(new BasicDBObject(FieldType.SUB_MERCHANT_ID.getName(), subMerchantPayId));
							}
							sessionUser = user; // userdao.findPayId(sessionUser.getParentPayId());

						} else if (!StringUtils.isEmpty(sessionUser.getSubUserType())
								&& sessionUser.getSubUserType().equalsIgnoreCase("normalType")) {

							if (!StringUtils.isBlank(orderId)) {
								obj1.add(new BasicDBObject(FieldType.ORDER_ID.getName(), orderId));
							}
							if (!StringUtils.isBlank(pgRefNum)) {
								obj1.add(new BasicDBObject(FieldType.PG_REF_NUM.getName(), pgRefNum));
							}
							if (StringUtils.isNotBlank(rrn)) {
								obj1.add(new BasicDBObject(FieldType.RRN.getName(), rrn));
							}
							if (StringUtils.isNotBlank(consumerNo)) {
								obj1.add(new BasicDBObject(FieldType.CUST_ID.getName(), consumerNo));
							}
							if (!StringUtils.isBlank(merchantPayId)) {
								obj1.add(new BasicDBObject(FieldType.PAY_ID.getName(), merchantPayId));
							}
							if (!StringUtils.isBlank(subMerchantPayId)) {
								obj1.add(new BasicDBObject(FieldType.SUB_MERCHANT_ID.getName(), subMerchantPayId));
							}
						}
					} else {
						if (!StringUtils.isBlank(orderId)) {
							obj1.add(new BasicDBObject(FieldType.ORDER_ID.getName(), orderId));
						}
						if (!StringUtils.isBlank(pgRefNum)) {
							obj1.add(new BasicDBObject(FieldType.PG_REF_NUM.getName(), pgRefNum));
						}
						if (StringUtils.isNotBlank(rrn)) {
							obj1.add(new BasicDBObject(FieldType.RRN.getName(), rrn));
						}
						if (StringUtils.isNotBlank(consumerNo)) {
							obj1.add(new BasicDBObject(FieldType.CUST_ID.getName(), consumerNo));
						}
						if (!sessionUser.isSuperMerchant()
								&& StringUtils.isNotBlank(sessionUser.getSuperMerchantId())) {
							obj1.add(new BasicDBObject(FieldType.PAY_ID.getName(), sessionUser.getSuperMerchantId()));
							obj1.add(new BasicDBObject(FieldType.SUB_MERCHANT_ID.getName(), sessionUser.getPayId()));
						} else {
							obj1.add(new BasicDBObject(FieldType.PAY_ID.getName(), sessionUser.getPayId()));
						}
					}
				} else {
					if (!StringUtils.isBlank(orderId)) {
						obj1.add(new BasicDBObject(FieldType.ORDER_ID.getName(), orderId));
					}
					if (!StringUtils.isBlank(pgRefNum)) {
						obj1.add(new BasicDBObject(FieldType.PG_REF_NUM.getName(), pgRefNum));
					}
					if (StringUtils.isNotBlank(rrn)) {
						obj1.add(new BasicDBObject(FieldType.RRN.getName(), rrn));
					}
					if (StringUtils.isNotBlank(consumerNo)) {
						obj1.add(new BasicDBObject(FieldType.CUST_ID.getName(), consumerNo));
					}
					if (StringUtils.isNotBlank(acqId)) {
						obj1.add(new BasicDBObject(FieldType.ACQ_ID.getName(), acqId));
					}
				}
				andQuery1.put("$and", obj1);

				MongoDatabase dbIns1 = mongoInstance.getDB();
				MongoCollection<Document> coll1 = dbIns1.getCollection(
						propertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));
				MongoCursor<Document> cursor1 = coll1.find(andQuery1)
						.sort(new BasicDBObject(FieldType.CREATE_DATE.getName(), -1)).iterator();

				while (cursor1.hasNext()) {
					Document doc = cursor1.next();

					if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
							&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
						doc = dataEncDecTool.decryptDocument(doc);
					}

					SearchTransaction searchTxn = new SearchTransaction();
					User user = new User();
					user = userdao.findPayId(doc.getString(FieldType.PAY_ID.toString()));
					BigInteger txnID = new BigInteger(doc.getString(FieldType.TXN_ID.toString()));
					searchTxn.setPayId(doc.getString(FieldType.PAY_ID.toString()));
					searchTxn.setTransactionId((txnID));
					searchTxn.setTxnId(doc.getString(FieldType.TXN_ID.toString()));
					searchTxn.setPgRefNum(doc.getString(FieldType.PG_REF_NUM.toString()));
					searchTxn.setMerchant(user.getBusinessName());
					searchTxn.setOrderId(doc.getString(FieldType.ORDER_ID.toString()));
					searchTxn.settDate(doc.getString(FieldType.CREATE_DATE.toString()));

					if (StringUtils.isNotBlank(doc.getString(FieldType.SUB_MERCHANT_ID.getName()))) {
						searchTxn.setSubMerchantId(
								userdao.getBusinessNameByPayId(doc.getString(FieldType.SUB_MERCHANT_ID.getName())));
					}

					if (StringUtils.isBlank(doc.getString(FieldType.PAYMENT_TYPE.toString()))) {
						searchTxn.setPaymentType(CrmFieldConstants.NA.toString());
					} else {
						searchTxn.setPaymentType(doc.getString(FieldType.PAYMENT_TYPE.toString()));
					}

					if (StringUtils.isBlank(doc.getString(FieldType.ACQUIRER_MODE.toString()))) {
						searchTxn.setAcquirerMode(CrmFieldConstants.NA.toString());
					} else {
						searchTxn.setAcquirerMode(doc.getString(FieldType.ACQUIRER_MODE.toString()));
					}

					if (StringUtils.isNotBlank(doc.getString(FieldType.TXN_CAPTURE_FLAG.getName()))) {
						searchTxn.setTxnSettledType(doc.getString(FieldType.TXN_CAPTURE_FLAG.getName()));
					} else {
						searchTxn.setTxnSettledType(CrmFieldConstants.NA.getValue());
					}

					if (StringUtils.isBlank(doc.getString(FieldType.MOP_TYPE.toString()))) {
						searchTxn.setMopType(CrmFieldConstants.NA.toString());
					} else {
						searchTxn.setMopType(MopType.getmopName(doc.getString(FieldType.MOP_TYPE.toString())));
					}
					if (doc.getString(FieldType.TXNTYPE.toString()).equals(TransactionType.RECO.getName())) {
						searchTxn.setTxnType(TransactionType.SALE.getName());
					} else if (doc.getString(FieldType.TXNTYPE.toString())
							.equals(TransactionType.REFUNDRECO.getName())) {
						searchTxn.setTxnType(TransactionType.REFUND.getName());
					} else {
						searchTxn.setTxnType(doc.getString(FieldType.TXNTYPE.toString()));
					}

					if (StringUtils.isBlank(doc.getString(FieldType.CARD_MASK.toString()))) {
						searchTxn.setCardNum(CrmFieldConstants.NA.toString());
					} else {
						searchTxn.setCardNum(doc.getString(FieldType.CARD_MASK.toString()));
					}

					if (StringUtils.isBlank(doc.getString(FieldType.UDF10.toString()))) {
						searchTxn.setUdf10(CrmFieldConstants.NA.toString());
					} else {
						searchTxn.setUdf10(doc.getString(FieldType.UDF10.toString()));
					}

					if (((sessionUser.getUserType().equals(UserType.MERCHANT))
							|| (sessionUser.getUserType().equals(UserType.SUBUSER)))
							&& merchantSettings.isCustomTransactionStatus()) {
						if (((doc.getString(FieldType.STATUS.toString()))
								.equalsIgnoreCase(StatusType.TIMEOUT.getName()))
								|| ((doc.getString(FieldType.STATUS.toString()))
										.equalsIgnoreCase(StatusType.CAPTURED.getName()))
								|| ((doc.getString(FieldType.STATUS.toString()))
										.equalsIgnoreCase(StatusType.ENROLLED.getName()))
								|| ((doc.getString(FieldType.STATUS.toString()))
										.equalsIgnoreCase(StatusType.SETTLED.getName()))
								|| ((doc.getString(FieldType.STATUS.toString()))
										.equalsIgnoreCase(StatusType.SENT_TO_BANK.getName()))
								|| ((doc.getString(FieldType.STATUS.toString()))
										.equalsIgnoreCase(StatusType.PENDING.getName()))) {
							searchTxn.setStatus(doc.getString(FieldType.STATUS.toString()));
						} else {
							searchTxn.setStatus("Failed");
						}
					} else {
						searchTxn.setStatus(doc.getString(FieldType.STATUS.toString()));
					}

					searchTxn.setAmount(doc.getString(FieldType.AMOUNT.toString()));
					searchTxn.setTotalAmount(doc.getString(FieldType.TOTAL_AMOUNT.toString()));
					if (StringUtils.isBlank(doc.getString(FieldType.CUST_NAME.toString()))) {
						searchTxn.setCustName(CrmFieldConstants.NA.toString());
					} else {
						searchTxn.setCustName(doc.getString(FieldType.CUST_NAME.toString()));
					}

					if (StringUtils.isBlank(doc.getString(FieldType.RRN.toString()))) {
						searchTxn.setRrn(CrmFieldConstants.NA.toString());
					} else {
						searchTxn.setRrn(doc.getString(FieldType.RRN.toString()));
					}

					if (StringUtils.isBlank(doc.getString(FieldType.ACQ_ID.toString()))) {
						searchTxn.setAcqId(CrmFieldConstants.NA.toString());
					} else {
						searchTxn.setAcqId(doc.getString(FieldType.ACQ_ID.toString()));
					}

					if (StringUtils.isBlank(doc.getString(FieldType.RESPONSE_MESSAGE.toString()))) {
						searchTxn.setPgResponseMessage(CrmFieldConstants.NA.toString());
					} else {
						searchTxn.setPgResponseMessage(doc.getString(FieldType.RESPONSE_MESSAGE.toString()));
					}

					if (StringUtils.isBlank(doc.getString(FieldType.PG_TXN_MESSAGE.toString()))) {
						searchTxn.setAcquirerTxnMessage(CrmFieldConstants.NA.toString());
					} else {
						searchTxn.setAcquirerTxnMessage(doc.getString(FieldType.PG_TXN_MESSAGE.toString()));
					}
					if (StringUtils.isBlank(doc.getString(FieldType.REFUND_ORDER_ID.toString()))) {
						searchTxn.setRefund_txn_id(CrmFieldConstants.NA.toString());
					} else {
						searchTxn.setRefund_txn_id(doc.getString(FieldType.REFUND_ORDER_ID.toString()));
					}

					if (StringUtils.isBlank(doc.getString(FieldType.ACQUIRER_TDR_SC.toString()))) {
						searchTxn.setRefund_txn_id(CrmFieldConstants.NA.toString());
					} else {
						searchTxn.setACQUIRER_TDR_SC(doc.getString(FieldType.ACQUIRER_TDR_SC.toString()));
					}

					if (StringUtils.isBlank(doc.getString((FieldType.ACQUIRER_GST.toString())))) {
						searchTxn.setRefund_txn_id(CrmFieldConstants.NA.toString());
					} else {
						searchTxn.setACQUIRER_GST(doc.getString(FieldType.ACQUIRER_GST.toString()));
					}

					if (StringUtils.isBlank(doc.getString((FieldType.PG_GST.toString())))) {
						searchTxn.setRefund_txn_id(CrmFieldConstants.NA.toString());
					} else {
						searchTxn.setPG_GST(doc.getString(FieldType.PG_GST.toString()));

					}

					if (StringUtils.isBlank(doc.getString(FieldType.PG_TDR_SC.toString()))) {
						searchTxn.setRefund_txn_id(CrmFieldConstants.NA.toString());
					} else {
						searchTxn.setPG_TDR_SC(doc.getString(FieldType.PG_TDR_SC.toString()));

					}

					if (doc.containsKey(FieldType.RESELLER_CHARGES.getName())
							&& StringUtils.isBlank(doc.getString(FieldType.RESELLER_CHARGES.getName()))) {
						searchTxn.setResellerCharges(CrmFieldConstants.NA.getValue());
					} else {
						searchTxn.setResellerCharges(doc.getString(FieldType.RESELLER_CHARGES.getName()));
					}

					if (doc.containsKey(FieldType.RESELLER_GST.getName())
							&& StringUtils.isBlank(doc.getString(FieldType.RESELLER_GST.getName()))) {
						searchTxn.setResellerGst(CrmFieldConstants.NA.getValue());
					} else {
						searchTxn.setResellerGst(doc.getString(FieldType.RESELLER_GST.getName()));
					}

					if (StringUtils.isBlank(doc.getString(FieldType.ACQUIRER_TYPE.toString()))) {
						searchTxn.setRefund_txn_id(CrmFieldConstants.NA.toString());
						searchTxn.setAcquirerType(CrmFieldConstants.NA.toString());

					} else {
						searchTxn.setAcquirerType(doc.getString(FieldType.ACQUIRER_TYPE.toString()));
					}

					/*
					 * else { if ((sessionUser.getUserType().equals(UserType.MERCHANT) ||
					 * (sessionUser.getUserType().equals(UserType.SUBUSER)))) {
					 * searchTxn.setAcquirerType(doc.getString(FieldType.ACQUIRER_TYPE.toString()));
					 * searchTxn.setAcquirerMode(doc.getString(FieldType.ACQUIRER_MODE.toString()));
					 * } }
					 */

					if (StringUtils.isBlank(doc.getString(FieldType.PAYMENTS_REGION.toString()))) {
						searchTxn.setPayment_Region(CrmFieldConstants.NA.toString());

					} else {
						searchTxn.setPayment_Region(doc.getString(FieldType.PAYMENTS_REGION.toString()));
					}

					if (StringUtils.isBlank(doc.getString(FieldType.CARD_HOLDER_TYPE.toString()))) {
						searchTxn.setCard_Holder_Type(CrmFieldConstants.NA.toString());

					} else {
						searchTxn.setCard_Holder_Type(doc.getString(FieldType.CARD_HOLDER_TYPE.toString()));
					}

					if (StringUtils.isBlank(doc.getString(FieldType.RESPONSE_CODE.toString()))) {
						searchTxn.setResponseCode(CrmFieldConstants.NA.toString());
					} else {
						searchTxn.setResponseCode(doc.getString(FieldType.RESPONSE_CODE.toString()));
					}

					if (StringUtils.isBlank(doc.getString(FieldType.CUST_ID.toString()))) {
						searchTxn.setConsumerNo("Consumer No Not found");
					} else {
						searchTxn.setConsumerNo(doc.getString(FieldType.CUST_ID.toString()));
					}

					if (StringUtils.isNotBlank(doc.getString(FieldType.PG_TDR_SC.getName()))
							&& StringUtils.isNotBlank(doc.getString(FieldType.ACQUIRER_TDR_SC.getName()))
							&& StringUtils.isNotBlank(doc.getString(FieldType.RESELLER_CHARGES.getName()))) {

						BigDecimal acqTdrSc = new BigDecimal(doc.getString(FieldType.ACQUIRER_TDR_SC.getName()));
						BigDecimal pgTdrSc = new BigDecimal(doc.getString(FieldType.PG_TDR_SC.getName()));
						BigDecimal resellerTdrSc = new BigDecimal(doc.getString(FieldType.RESELLER_CHARGES.getName()));

						searchTxn.setTotalChargeTdrSc(String.valueOf(acqTdrSc.add(pgTdrSc).add(resellerTdrSc)));

					} else {
						searchTxn.setTotalChargeTdrSc("");
					}

					if (doc.containsKey(FieldType.RESELLER_CHARGES.getName())
							&& StringUtils.isNotBlank(doc.getString(FieldType.RESELLER_CHARGES.getName()))
							&& StringUtils.isNotBlank(searchTxn.getACQUIRER_TDR_SC())
							&& StringUtils.isNotBlank(searchTxn.getPG_TDR_SC())) {

						BigDecimal acquirerTdrScAmount = new BigDecimal(searchTxn.getACQUIRER_TDR_SC());
						BigDecimal pgTdrScAmount = new BigDecimal(searchTxn.getPG_TDR_SC());
						BigDecimal resellerTdrScAmount = new BigDecimal(
								doc.getString(FieldType.RESELLER_CHARGES.getName()));
						BigDecimal acquirerGst = new BigDecimal(doc.getString(FieldType.ACQUIRER_GST.getName()));
						BigDecimal pgGst = new BigDecimal(doc.getString(FieldType.PG_GST.getName()));
						BigDecimal resellerGst = new BigDecimal(doc.getString(FieldType.RESELLER_GST.getName()));

						searchTxn.setTotalTdrSc(String.valueOf(pgTdrScAmount.add(acquirerTdrScAmount)
								.add(resellerTdrScAmount).add(acquirerGst).add(pgGst).add(resellerGst)));

					} else {

						if (StringUtils.isNotBlank(searchTxn.getACQUIRER_TDR_SC())
								&& StringUtils.isNotBlank(searchTxn.getPG_TDR_SC())) {
							BigDecimal acquirerTdrScAmount = new BigDecimal(searchTxn.getACQUIRER_TDR_SC());
							BigDecimal pgTdrScAmount = new BigDecimal(searchTxn.getPG_TDR_SC());
							searchTxn.setTotalTdrSc(String.valueOf(pgTdrScAmount.add(acquirerTdrScAmount)));
						} else {
							searchTxn.setTotalTdrSc("");
						}
					}

					if (doc.containsKey(FieldType.RESELLER_GST.getName())
							&& StringUtils.isNotBlank(doc.getString(FieldType.RESELLER_GST.getName()))
							&& StringUtils.isNotBlank(searchTxn.getACQUIRER_GST())
							&& StringUtils.isNotBlank(searchTxn.getPG_GST())) {

						BigDecimal acquirerGstAmount = new BigDecimal(searchTxn.getACQUIRER_GST());
						BigDecimal pgGstAmount = new BigDecimal(searchTxn.getPG_GST());
						BigDecimal resellerGstAmount = new BigDecimal(doc.getString(FieldType.RESELLER_GST.getName()));
						searchTxn
								.setTotalGst(String.valueOf(acquirerGstAmount.add(pgGstAmount).add(resellerGstAmount)));

					} else {

						if (StringUtils.isNotBlank(searchTxn.getACQUIRER_GST())
								&& StringUtils.isNotBlank(searchTxn.getPG_GST())) {
							BigDecimal acquirerGstAmount = new BigDecimal(searchTxn.getACQUIRER_GST());
							BigDecimal pgGstAmount = new BigDecimal(searchTxn.getPG_GST());
							searchTxn.setTotalGst(String.valueOf(acquirerGstAmount.add(pgGstAmount)));
						} else {
							searchTxn.setTotalGst("");
						}
					}

					transactionList.add(searchTxn);
				}

			}

			/*
			 * Comparator<SearchTransaction> comp = (SearchTransaction a, SearchTransaction
			 * b) -> {
			 * 
			 * if (a.gettDate().compareTo(b.gettDate()) > 0) { return 1; } else if
			 * (a.gettDate().compareTo(b.gettDate()) < 0) { return -1; } else { return 0; }
			 * };
			 * 
			 * Collections.sort(transactionList, comp);
			 */

			return transactionList;
		} catch (Exception e) {
			logger.error("Exception occured Agent Search , Exception = ", e);
			return null;
		}
	}

	public List<SearchTransaction> searchPaymentFromStatus(String orderId, String pgRefNum, User sessionUser,
			String consumerNo, String rrn, String acqId) {

		logger.info("inside searchPaymentFromStatus");

		List<SearchTransaction> transactionList = new ArrayList<SearchTransaction>();

		try {

			BasicDBObject andQuery = new BasicDBObject();
			List<BasicDBObject> obj = new ArrayList<BasicDBObject>();

			if (!StringUtils.isBlank(orderId)) {
				obj.add(new BasicDBObject(FieldType.ORDER_ID.getName(), orderId));
			}
			if (!StringUtils.isBlank(pgRefNum)) {
				obj.add(new BasicDBObject(FieldType.PG_REF_NUM.getName(), pgRefNum));
			}
			if (StringUtils.isNotBlank(rrn)) {
				obj.add(new BasicDBObject(FieldType.RRN.getName(), rrn));
			}
			if (StringUtils.isNotBlank(consumerNo)) {
				obj.add(new BasicDBObject(FieldType.CUST_ID.getName(), consumerNo));
			}
			if (StringUtils.isNotBlank(acqId)) {
				obj.add(new BasicDBObject(FieldType.ACQ_ID.getName(), acqId));
			}

			andQuery.put("$and", obj);

			List<SearchTransaction> transactionStatusList = new ArrayList<SearchTransaction>();
			transactionStatusList = findDataFromStatusTable(andQuery);
			transactionList.addAll(transactionStatusList);

		} catch (Exception e) {
			logger.error("Exception occured in Agent Search from transactionStatus table, Exception = ", e);
			return null;
		}

		return transactionList;
	}

	private List<SearchTransaction> findDataFromStatusTable(BasicDBObject andQuery) {

		logger.info("inside findDataFromStatusTable");
		List<SearchTransaction> transactionStatusList = new ArrayList<SearchTransaction>();

		MongoDatabase dbIns = mongoInstance.getDB();
		MongoCollection<Document> coll = dbIns.getCollection(
				propertiesManager.propertiesMap.get(prefix + Constants.TRANSACTION_STATUS_COLLECTION.getValue()));
		MongoCursor<Document> cursor = coll.find(andQuery).sort(new BasicDBObject(FieldType.CREATE_DATE.getName(), -1))
				.iterator();

		while (cursor.hasNext()) {
			Document doc = cursor.next();

			if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("EncryptDBData"))
					&& propertiesManager.propertiesMap.get("EncryptDBData").equalsIgnoreCase("Y")) {
				doc = dataEncDecTool.decryptDocument(doc);
			}

			SearchTransaction searchTxn = new SearchTransaction();
			User user = new User();
			user = userdao.findPayId(doc.getString(FieldType.PAY_ID.toString()));
			BigInteger txnID = new BigInteger(doc.getString(FieldType.TXN_ID.toString()));
			searchTxn.setPayId(doc.getString(FieldType.PAY_ID.toString()));
			searchTxn.setTransactionId((txnID));
			searchTxn.setTxnId(doc.getString(FieldType.TXN_ID.toString()));
			searchTxn.setPgRefNum(doc.getString(FieldType.PG_REF_NUM.toString()));
			searchTxn.setMerchant(user.getBusinessName());
			searchTxn.setOrderId(doc.getString(FieldType.ORDER_ID.toString()));
			searchTxn.settDate(doc.getString(FieldType.CREATE_DATE.toString()));

			if (StringUtils.isNotBlank(doc.getString(FieldType.SUB_MERCHANT_ID.getName()))) {
				searchTxn.setSubMerchantId(
						userdao.getBusinessNameByPayId(doc.getString(FieldType.SUB_MERCHANT_ID.getName())));
			}

			if (StringUtils.isBlank(doc.getString(FieldType.PAYMENT_TYPE.toString()))) {
				searchTxn.setPaymentType(CrmFieldConstants.NA.toString());
			} else {
				searchTxn.setPaymentType(doc.getString(FieldType.PAYMENT_TYPE.toString()));
			}

			if (StringUtils.isBlank(doc.getString(FieldType.ACQUIRER_MODE.toString()))) {
				searchTxn.setAcquirerMode(CrmFieldConstants.NA.toString());
			} else {
				searchTxn.setAcquirerMode(doc.getString(FieldType.ACQUIRER_MODE.toString()));
			}

			if (StringUtils.isNotBlank(doc.getString(FieldType.TXN_CAPTURE_FLAG.getName()))) {
				searchTxn.setTxnSettledType(doc.getString(FieldType.TXN_CAPTURE_FLAG.getName()));
			} else {
				searchTxn.setTxnSettledType(CrmFieldConstants.NA.getValue());
			}

			if (StringUtils.isBlank(doc.getString(FieldType.MOP_TYPE.toString()))) {
				searchTxn.setMopType(CrmFieldConstants.NA.toString());
			} else {
				searchTxn.setMopType(MopType.getmopName(doc.getString(FieldType.MOP_TYPE.toString())));
			}
			if (doc.getString(FieldType.TXNTYPE.toString()).equals(TransactionType.RECO.getName())) {
				searchTxn.setTxnType(TransactionType.SALE.getName());
			} else if (doc.getString(FieldType.TXNTYPE.toString()).equals(TransactionType.REFUNDRECO.getName())) {
				searchTxn.setTxnType(TransactionType.REFUND.getName());
			} else {
				searchTxn.setTxnType(doc.getString(FieldType.TXNTYPE.toString()));
			}

			if (StringUtils.isBlank(doc.getString(FieldType.CARD_MASK.toString()))) {
				searchTxn.setCardNum(CrmFieldConstants.NA.toString());
			} else {
				searchTxn.setCardNum(doc.getString(FieldType.CARD_MASK.toString()));
			}

			if (StringUtils.isBlank(doc.getString(FieldType.UDF10.toString()))) {
				searchTxn.setUdf10(CrmFieldConstants.NA.toString());
			} else {
				searchTxn.setUdf10(doc.getString(FieldType.UDF10.toString()));
			}

			/*
			 * if (((sessionUser.getUserType().equals(UserType.MERCHANT)) ||
			 * (sessionUser.getUserType().equals(UserType.SUBUSER))) &&
			 * sessionUser.isCustomTransactionStatus()) { if
			 * (((doc.getString(FieldType.STATUS.toString())).equalsIgnoreCase(StatusType.
			 * TIMEOUT.getName())) || ((doc.getString(FieldType.STATUS.toString()))
			 * .equalsIgnoreCase(StatusType.CAPTURED.getName())) ||
			 * ((doc.getString(FieldType.STATUS.toString()))
			 * .equalsIgnoreCase(StatusType.ENROLLED.getName())) ||
			 * ((doc.getString(FieldType.STATUS.toString())).equalsIgnoreCase(StatusType.
			 * SETTLED.getName())) || ((doc.getString(FieldType.STATUS.toString()))
			 * .equalsIgnoreCase(StatusType.SENT_TO_BANK.getName())) ||
			 * ((doc.getString(FieldType.STATUS.toString()))
			 * .equalsIgnoreCase(StatusType.PENDING.getName()))) {
			 * searchTxn.setStatus(doc.getString(FieldType.STATUS.toString())); } else {
			 * searchTxn.setStatus("Failed"); } } else {
			 */
			searchTxn.setStatus(doc.getString(FieldType.STATUS.toString()));
			// }

			searchTxn.setAmount(doc.getString(FieldType.AMOUNT.toString()));
			searchTxn.setTotalAmount(doc.getString(FieldType.TOTAL_AMOUNT.toString()));
			if (StringUtils.isBlank(doc.getString(FieldType.CUST_NAME.toString()))) {
				searchTxn.setCustName(CrmFieldConstants.NA.toString());
			} else {
				searchTxn.setCustName(doc.getString(FieldType.CUST_NAME.toString()));
			}

			if (StringUtils.isBlank(doc.getString(FieldType.RRN.toString()))) {
				searchTxn.setRrn(CrmFieldConstants.NA.toString());
			} else {
				searchTxn.setRrn(doc.getString(FieldType.RRN.toString()));
			}

			if (StringUtils.isBlank(doc.getString(FieldType.ACQ_ID.toString()))) {
				searchTxn.setAcqId(CrmFieldConstants.NA.toString());
			} else {
				searchTxn.setAcqId(doc.getString(FieldType.ACQ_ID.toString()));
			}

			if (StringUtils.isBlank(doc.getString(FieldType.RESPONSE_MESSAGE.toString()))) {
				searchTxn.setPgResponseMessage(CrmFieldConstants.NA.toString());
			} else {
				searchTxn.setPgResponseMessage(doc.getString(FieldType.RESPONSE_MESSAGE.toString()));
			}

			if (StringUtils.isBlank(doc.getString(FieldType.PG_TXN_MESSAGE.toString()))) {
				searchTxn.setAcquirerTxnMessage(CrmFieldConstants.NA.toString());
			} else {
				searchTxn.setAcquirerTxnMessage(doc.getString(FieldType.PG_TXN_MESSAGE.toString()));
			}
			if (StringUtils.isBlank(doc.getString(FieldType.REFUND_ORDER_ID.toString()))) {
				searchTxn.setRefund_txn_id(CrmFieldConstants.NA.toString());
			} else {
				searchTxn.setRefund_txn_id(doc.getString(FieldType.REFUND_ORDER_ID.toString()));
			}

			if (StringUtils.isBlank(doc.getString(FieldType.ACQUIRER_TDR_SC.toString()))) {
				searchTxn.setRefund_txn_id(CrmFieldConstants.NA.toString());
			} else {
				searchTxn.setACQUIRER_TDR_SC(doc.getString(FieldType.ACQUIRER_TDR_SC.toString()));
			}

			if (StringUtils.isBlank(doc.getString((FieldType.ACQUIRER_GST.toString())))) {
				searchTxn.setRefund_txn_id(CrmFieldConstants.NA.toString());
			} else {
				searchTxn.setACQUIRER_GST(doc.getString(FieldType.ACQUIRER_GST.toString()));
			}

			if (StringUtils.isBlank(doc.getString((FieldType.PG_GST.toString())))) {
				searchTxn.setRefund_txn_id(CrmFieldConstants.NA.toString());
			} else {
				searchTxn.setPG_GST(doc.getString(FieldType.PG_GST.toString()));

			}

			if (StringUtils.isBlank(doc.getString(FieldType.PG_TDR_SC.toString()))) {
				searchTxn.setRefund_txn_id(CrmFieldConstants.NA.toString());
			} else {
				searchTxn.setPG_TDR_SC(doc.getString(FieldType.PG_TDR_SC.toString()));

			}

			if (doc.containsKey(FieldType.RESELLER_CHARGES.getName())
					&& StringUtils.isBlank(doc.getString(FieldType.RESELLER_CHARGES.getName()))) {
				searchTxn.setResellerCharges(CrmFieldConstants.NA.getValue());
			} else {
				searchTxn.setResellerCharges(doc.getString(FieldType.RESELLER_CHARGES.getName()));
			}

			if (doc.containsKey(FieldType.RESELLER_GST.getName())
					&& StringUtils.isBlank(doc.getString(FieldType.RESELLER_GST.getName()))) {
				searchTxn.setResellerGst(CrmFieldConstants.NA.getValue());
			} else {
				searchTxn.setResellerGst(doc.getString(FieldType.RESELLER_GST.getName()));
			}

			if (StringUtils.isBlank(doc.getString(FieldType.ACQUIRER_TYPE.toString()))) {
				searchTxn.setRefund_txn_id(CrmFieldConstants.NA.toString());
				searchTxn.setAcquirerType(CrmFieldConstants.NA.toString());

			} else {
				searchTxn.setAcquirerType(doc.getString(FieldType.ACQUIRER_TYPE.toString()));
			}

			/*
			 * else { if ((sessionUser.getUserType().equals(UserType.MERCHANT) ||
			 * (sessionUser.getUserType().equals(UserType.SUBUSER)))) {
			 * searchTxn.setAcquirerType(doc.getString(FieldType.ACQUIRER_TYPE.toString()));
			 * searchTxn.setAcquirerMode(doc.getString(FieldType.ACQUIRER_MODE.toString()));
			 * } }
			 */

			if (StringUtils.isBlank(doc.getString(FieldType.PAYMENTS_REGION.toString()))) {
				searchTxn.setPayment_Region(CrmFieldConstants.NA.toString());

			} else {
				searchTxn.setPayment_Region(doc.getString(FieldType.PAYMENTS_REGION.toString()));
			}

			if (StringUtils.isBlank(doc.getString(FieldType.CARD_HOLDER_TYPE.toString()))) {
				searchTxn.setCard_Holder_Type(CrmFieldConstants.NA.toString());

			} else {
				searchTxn.setCard_Holder_Type(doc.getString(FieldType.CARD_HOLDER_TYPE.toString()));
			}

			if (StringUtils.isBlank(doc.getString(FieldType.RESPONSE_CODE.toString()))) {
				searchTxn.setResponseCode(CrmFieldConstants.NA.toString());
			} else {
				searchTxn.setResponseCode(doc.getString(FieldType.RESPONSE_CODE.toString()));
			}

			if (StringUtils.isBlank(doc.getString(FieldType.CUST_ID.toString()))) {
				searchTxn.setConsumerNo("Consumer No Not Found");
			} else {
				searchTxn.setConsumerNo(doc.getString(FieldType.CUST_ID.toString()));
			}

			if (StringUtils.isNotBlank(doc.getString(FieldType.PG_TDR_SC.getName()))
					&& StringUtils.isNotBlank(doc.getString(FieldType.ACQUIRER_TDR_SC.getName()))
					&& StringUtils.isNotBlank(doc.getString(FieldType.RESELLER_CHARGES.getName()))) {

				BigDecimal acqTdrSc = new BigDecimal(doc.getString(FieldType.ACQUIRER_TDR_SC.getName()));
				BigDecimal pgTdrSc = new BigDecimal(doc.getString(FieldType.PG_TDR_SC.getName()));
				BigDecimal resellerTdrSc = new BigDecimal(doc.getString(FieldType.RESELLER_CHARGES.getName()));

				searchTxn.setTotalChargeTdrSc(String.valueOf(acqTdrSc.add(pgTdrSc).add(resellerTdrSc)));

			} else {
				searchTxn.setTotalChargeTdrSc("");
			}

			if (doc.containsKey(FieldType.RESELLER_CHARGES.getName())
					&& StringUtils.isNotBlank(doc.getString(FieldType.RESELLER_CHARGES.getName()))
					&& StringUtils.isNotBlank(searchTxn.getACQUIRER_TDR_SC())
					&& StringUtils.isNotBlank(searchTxn.getPG_TDR_SC())) {

				BigDecimal acquirerTdrScAmount = new BigDecimal(searchTxn.getACQUIRER_TDR_SC());
				BigDecimal pgTdrScAmount = new BigDecimal(searchTxn.getPG_TDR_SC());
				BigDecimal resellerTdrScAmount = new BigDecimal(doc.getString(FieldType.RESELLER_CHARGES.getName()));
				BigDecimal acquirerGst = new BigDecimal(doc.getString(FieldType.ACQUIRER_GST.getName()));
				BigDecimal pgGst = new BigDecimal(doc.getString(FieldType.PG_GST.getName()));
				BigDecimal resellerGst = new BigDecimal(doc.getString(FieldType.RESELLER_GST.getName()));

				searchTxn.setTotalTdrSc(String.valueOf(pgTdrScAmount.add(acquirerTdrScAmount).add(resellerTdrScAmount)
						.add(acquirerGst).add(pgGst).add(resellerGst)));

			} else {

				if (StringUtils.isNotBlank(searchTxn.getACQUIRER_TDR_SC())
						&& StringUtils.isNotBlank(searchTxn.getPG_TDR_SC())) {
					BigDecimal acquirerTdrScAmount = new BigDecimal(searchTxn.getACQUIRER_TDR_SC());
					BigDecimal pgTdrScAmount = new BigDecimal(searchTxn.getPG_TDR_SC());
					searchTxn.setTotalTdrSc(String.valueOf(pgTdrScAmount.add(acquirerTdrScAmount)));
				} else {
					searchTxn.setTotalTdrSc("");
				}
			}

			if (doc.containsKey(FieldType.RESELLER_GST.getName())
					&& StringUtils.isNotBlank(doc.getString(FieldType.RESELLER_GST.getName()))
					&& StringUtils.isNotBlank(searchTxn.getACQUIRER_GST())
					&& StringUtils.isNotBlank(searchTxn.getPG_GST())) {

				BigDecimal acquirerGstAmount = new BigDecimal(searchTxn.getACQUIRER_GST());
				BigDecimal pgGstAmount = new BigDecimal(searchTxn.getPG_GST());
				BigDecimal resellerGstAmount = new BigDecimal(doc.getString(FieldType.RESELLER_GST.getName()));
				searchTxn.setTotalGst(String.valueOf(acquirerGstAmount.add(pgGstAmount).add(resellerGstAmount)));

			} else {

				if (StringUtils.isNotBlank(searchTxn.getACQUIRER_GST())
						&& StringUtils.isNotBlank(searchTxn.getPG_GST())) {
					BigDecimal acquirerGstAmount = new BigDecimal(searchTxn.getACQUIRER_GST());
					BigDecimal pgGstAmount = new BigDecimal(searchTxn.getPG_GST());
					searchTxn.setTotalGst(String.valueOf(acquirerGstAmount.add(pgGstAmount)));
				} else {
					searchTxn.setTotalGst("");
				}
			}

			transactionStatusList.add(searchTxn);

		}

		return transactionStatusList;
	}

}