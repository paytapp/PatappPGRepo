package com.paymentgateway.pg.core.util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.paymentgateway.commons.api.SmsControllerServiceProvider;
import com.paymentgateway.commons.dao.TransactionDetailsService;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.mongo.MongoInstance;
import com.paymentgateway.commons.user.AccountCurrencyRegion;
import com.paymentgateway.commons.user.TransactionSearch;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.AWSSESEmailService;
import com.paymentgateway.commons.util.Amount;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CrmEmailer;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.MopType;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.RouterConfigurationService;
import com.paymentgateway.commons.util.StaticDataProvider;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TransactionManager;
import com.paymentgateway.commons.util.TransactionType;
import com.paymentgateway.commons.util.TxnType;
import com.paymentgateway.pg.core.security.Authenticator;
import com.paymentgateway.pg.core.security.AuthenticatorFactory;

@Service("updateProcessor")
public class UpdateProcessor implements Processor {

	@Autowired
	private CryptoManager cryptoManager;

	@Autowired
	private Fields field;

	@Autowired
	private UserDao userDao;

	@Autowired
	private CrmEmailer crmEmailer;

	@Autowired
	private TransactionDetailsService transactionDetailsService;

	@Autowired
	private SmsControllerServiceProvider smsControllerServiceProvider;

	@Autowired
	private RouterConfigurationService routerConfigurationService;

	@Autowired
	private MongoInstance mongoInstance;

	@Autowired
	private PropertiesManager propertiesManager;

	@Autowired
	private StaticDataProvider staticDataProvider;

	@Autowired
	private CalculateSurchargeAmount calculateSurchargeAmount;

	@Autowired
	AWSSESEmailService awsSESEmailService;

	private static final String prefix = "MONGO_DB_";

	private static Logger logger = LoggerFactory.getLogger(UpdateProcessor.class.getName());

	public void preProcess(Fields fields) throws SystemException {
		cryptoManager.secure(fields);
	}

	public void process(Fields fields) throws SystemException {

		if (fields.contains(FieldType.MOP_TYPE.getName()) && fields.contains(FieldType.UPI_QR_CODE.getName())) {
			if ((fields.get(FieldType.MOP_TYPE.getName()).equals(MopType.UPI_QR.getCode()))
					&& (StringUtils.isNotBlank(fields.get(FieldType.UPI_QR_CODE.getName())))) {
				if (isExisting(fields)) {
					updatePgRefNumCharrgesTotalAmount(fields);
					return;
				}
			}
		}

		String response = fields.get(FieldType.RESPONSE_CODE.getName());
		logger.info("inside updateProcessor in process method ==> " + fields.get(FieldType.TXNTYPE.getName()) + " "
				+ "Txn id" + fields.get(FieldType.TXN_ID.getName()) + " " + response);
		if (response.equals(ErrorType.VALIDATION_FAILED.getCode())) {
			if (!fields.get(FieldType.ORIG_TXNTYPE.getName()).equalsIgnoreCase(TransactionType.STATUS.getName())) {
				addOid(fields);
				prepareInvalidTransactionForStorage(fields);
			}
			return;
		}
		if (response.equals(ErrorType.TRANSACTION_NOT_FOUND.getCode())) {
			return;
		}

		// Do not add txn to DB when Hash Validation Has failed
		if (response.equals(ErrorType.VALIDATION_FAILED.getCode())) {
			return;
		}
		if (fields.get(FieldType.INTERNAL_ORIG_TXN_TYPE.getName()).equals(TransactionType.STATUS.getName())
				|| fields.get(FieldType.INTERNAL_ORIG_TXN_TYPE.getName()).equals(TransactionType.VERIFY.getName())
				|| fields.get(FieldType.INTERNAL_ORIG_TXN_TYPE.getName()).equals(TransactionType.RECO.getName())
				|| fields.get(FieldType.INTERNAL_ORIG_TXN_TYPE.getName()).equals(TransactionType.REFUNDRECO.getName())
				|| ((StringUtils.isNotBlank(fields.get(FieldType.IS_MERCHANT_HOSTED.getName()))
						&& !fields.get(FieldType.IS_MERCHANT_HOSTED.getName()).equals("Y"))
						&& fields.get(FieldType.RESPONSE_CODE.getName())
								.equals(ErrorType.INVALID_VPA.getResponseCode()))) {
			return;
		}

		if (fields.get(FieldType.RESPONSE_CODE.getName())
				.equalsIgnoreCase(ErrorType.DUPLICATE_ORDER_ID.getResponseCode())
				&& fields.get(FieldType.RESPONSE_MESSAGE.getName())
						.equalsIgnoreCase(ErrorType.DUPLICATE_ORDER_ID.getResponseMessage())
				|| fields.get(FieldType.RESPONSE_CODE.getName())
						.equalsIgnoreCase(ErrorType.DUPLICATE_REFUND_ORDER_ID.getResponseCode())
						&& fields.get(FieldType.RESPONSE_MESSAGE.getName())
								.equalsIgnoreCase(ErrorType.DUPLICATE_REFUND_ORDER_ID.getResponseMessage())) {
			return;
		}

//		if(StringUtils.isNotEmpty(fields.get(FieldType.SKU_CODE.getName()))
//				&& StringUtils.isNotEmpty(fields.get(FieldType.CATEGORY_CODE.getName()))
//				&& StringUtils.isNotEmpty(fields.get(FieldType.REFUND_DAYS.getName()))
//				&& StringUtils.isNotEmpty(fields.get(FieldType.PRODUCT_PRICE.getName()))
//				&& StringUtils.isNotEmpty(fields.get(FieldType.VENDOR_ID.getName()))) {
//			
//			
//			String amountString = fields.get(FieldType.AMOUNT.getName());
//			String []priceDataArray = fields.get(FieldType.PRODUCT_PRICE.getName()).split(",");
//			
//			String amount = "0";
//			if (!StringUtils.isEmpty(amountString) && !StringUtils.isEmpty(fields.get(FieldType.CURRENCY_CODE.getName()))) {
//				if (!amountString.contains(".")) {
//					amount = Amount.toDecimal(amountString, fields.get(FieldType.CURRENCY_CODE.getName()));
//				} else {
//					amount = amountString;
//				}
//			}
//			
//			BigDecimal amountInDecimal = new BigDecimal(amount);
//			boolean priceFlag = false;
//			BigDecimal productPriceSum = new BigDecimal("0.0");
//			for(String price : priceDataArray) {
//				productPriceSum = productPriceSum.add(new BigDecimal(price));
//			}
//			if(amountInDecimal.compareTo(productPriceSum) == 1 || amountInDecimal.compareTo(productPriceSum) == -1) {
//				return;
//			}
//			
//			
//		}

		addOid(fields);

		String transactionType = fields.get(FieldType.TXNTYPE.getName());
		String responseCode = fields.get(FieldType.RESPONSE_CODE.getName());

		if (responseCode.equalsIgnoreCase(ErrorType.ACUIRER_DOWN.getCode())) {
			routerConfigurationService.autoUdpateRouterConfiguration(fields, responseCode);
			logger.info("inside updateProcessor in process method  for .ACUIRER_DOWN.==> "
					+ fields.get(FieldType.TXNTYPE.getName()) + " " + "Txn id" + fields.get(FieldType.TXN_ID.getName())
					+ " " + response);

		}

		if (null == transactionType) {
			prepareInvalidTransactionForStorage(fields);
		} else if (responseCode.equals(ErrorType.VALIDATION_FAILED.getCode())) {
			if (!transactionType.equals(TransactionType.REFUND.getName())
					&& !transactionType.equals(TransactionType.STATUS.getName())
					&& !transactionType.equals(TransactionType.CAPTURE.getName())
					&& !StringUtils.isBlank(fields.get(FieldType.INTERNAL_ORIG_TXN_TYPE.getName()))) {
				fields.put(FieldType.TXNTYPE.getName(), fields.get(FieldType.INTERNAL_ORIG_TXN_TYPE.getName()));
			}
			prepareInvalidTransactionForStorage(fields);
		} else if (responseCode.equals(ErrorType.DENIED_BY_FRAUD.getCode())) {
			String pgrefNum = fields.get(FieldType.PG_REF_NUM.getName());
			if (pgrefNum == null) {
				fields.put(FieldType.PG_REF_NUM.getName(), fields.get(FieldType.TXN_ID.getName()));
			}
			field.insert(fields);
		} else {
			field.insert(fields);
			// fields.insert();
		}

	}

	private boolean isExisting(Fields fields) {
		BasicDBObject qrQuery = new BasicDBObject(FieldType.OID.getName(),
				fields.get(FieldType.INTERNAL_ORIG_TXN_ID.getName()));
		MongoDatabase dbIns = mongoInstance.getDB();
		MongoCollection<Document> coll = dbIns
				.getCollection(PropertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));
		MongoCursor<Document> cursor = coll.find(qrQuery).iterator();
		if (cursor.hasNext()) {
			return true;
		}
		return false;
	}

	public void updatePgRefNumCharrgesTotalAmount(Fields fields) {

		try {
			updateTotalAmountField(fields);
		} catch (SystemException e) {
			logger.error("Exception in Update Processor class = ", e);
		}
		MongoDatabase dbIns = mongoInstance.getDB();
		MongoCollection<Document> coll = dbIns
				.getCollection(PropertiesManager.propertiesMap.get(prefix + Constants.COLLECTION_NAME.getValue()));
		List<BasicDBObject> queryList = new ArrayList<BasicDBObject>();
		queryList.add(new BasicDBObject(FieldType.PAY_ID.getName(), fields.get(FieldType.PAY_ID.getName())));
		queryList.add(new BasicDBObject(FieldType.ORDER_ID.getName(), fields.get(FieldType.ORDER_ID.getName())));
//		queryList.add(new BasicDBObject(FieldType.TXNTYPE.getName(), TransactionType.NEWORDER.getName()));

		BasicDBObject query = new BasicDBObject();
		query.append("$and", queryList);

		BasicDBObject setData = new BasicDBObject();
		setData.append("$set",
				new BasicDBObject().append(FieldType.PG_REF_NUM.getName(), fields.get(FieldType.PG_REF_NUM.getName()))
						.append(FieldType.RESELLER_CHARGES.getName(), fields.get(FieldType.RESELLER_CHARGES.getName()))
						.append(FieldType.RESELLER_GST.getName(), fields.get(FieldType.RESELLER_GST.getName()))
						.append(FieldType.MERCHANT_TDR_SC.getName(), fields.get(FieldType.MERCHANT_TDR_SC.getName()))
						.append(FieldType.MERCHANT_GST.getName(), fields.get(FieldType.MERCHANT_GST.getName()))
						.append(FieldType.PG_TDR_SC.getName(), fields.get(FieldType.PG_TDR_SC.getName()))
						.append(FieldType.PG_GST.getName(), fields.get(FieldType.PG_GST.getName()))
						.append(FieldType.ACQUIRER_TDR_SC.getName(), fields.get(FieldType.ACQUIRER_TDR_SC.getName()))
						.append(FieldType.ACQUIRER_GST.getName(), fields.get(FieldType.ACQUIRER_GST.getName()))
						.append(FieldType.TOTAL_AMOUNT.getName(), fields.get(FieldType.TOTAL_AMOUNT.getName())));
		logger.info("finding transaction = " + query);
		logger.info("updating transaction = " + setData);
		coll.updateOne(query, setData);
		logger.info("updated PG REF NUM for UPI_QR");
	}

	public void updateTotalAmountField(Fields fields) throws SystemException {
		if (StringUtils.isNoneBlank(fields.get(FieldType.SURCHARGE_FLAG.getName()))) {
			if (fields.get(FieldType.SURCHARGE_FLAG.getName()).equalsIgnoreCase("Y")) {
				BigDecimal[] surUPAmount = calculateSurchargeAmount.fetchUPSurchargeDetails(
						Amount.toDecimal(fields.get(FieldType.AMOUNT.getName()),
								fields.get(FieldType.CURRENCY_CODE.getName())),
						fields.get(FieldType.PAY_ID.getName()), AccountCurrencyRegion.DOMESTIC,
						fields.get(FieldType.SLAB_ID.getName()), fields.get(FieldType.RESELLER_ID.getName()));

				BigDecimal surchargeUPAmount = surUPAmount[1];

				fields.put(FieldType.TOTAL_AMOUNT.getName(), surchargeUPAmount.toString());
			}
		} else {
			fields.put(FieldType.TOTAL_AMOUNT.getName(), Amount.toDecimal(fields.get(FieldType.AMOUNT.getName()),
					fields.get(FieldType.CURRENCY_CODE.getName())));
		}
	}

	public void prepareInvalidTransactionForStorage(Fields fields) throws SystemException {
		String payId = fields.get(FieldType.PAY_ID.getName());
		if (!StringUtils.isEmpty(payId)) {
			if (!fields.get(FieldType.TXNTYPE.getName()).equalsIgnoreCase(TransactionType.INVALID.getName())) {
				Authenticator authenticator = AuthenticatorFactory.getAuthenticator();
				authenticator.isUserExists(fields);

				String txnId = fields.get(FieldType.TXN_ID.getName());
				if (StringUtils.isEmpty(txnId) || fields.get(FieldType.RESPONSE_CODE.getName())
						.equalsIgnoreCase(ErrorType.PAYMENT_OPTION_NOT_SUPPORTED.getCode())) {
					txnId = TransactionManager.getNewTransactionId();
				}
				String responseCode = fields.get(FieldType.RESPONSE_CODE.getName());
				String responseMessage = fields.get(FieldType.RESPONSE_MESSAGE.getName());
				String detailMessage = fields.get(FieldType.PG_TXN_MESSAGE.getName());
				String transactionType = fields.get(FieldType.TXNTYPE.getName());
				String returnUrl = fields.get(FieldType.RETURN_URL.getName());
				String orderId = fields.get(FieldType.ORDER_ID.getName());
				Fields internalFields = fields.removeInternalFields();
				String oid = fields.get(FieldType.OID.getName());
				String amount = fields.get(FieldType.AMOUNT.getName());
				String totalAmount = fields.get(FieldType.TOTAL_AMOUNT.getName());
				String currency = fields.get(FieldType.CURRENCY_CODE.getName());
				String moptype = fields.get(FieldType.MOP_TYPE.getName());
				String paymentType = fields.get(FieldType.PAYMENT_TYPE.getName());
				String paymentRegion = fields.get(FieldType.PAYMENTS_REGION.getName());
				String paymentAddress = fields.get(FieldType.PAYER_ADDRESS.getName());
				if (StringUtils.isEmpty(oid)) {
					oid = txnId;
				}
				fields.clear();
				fields.put(internalFields);
				fields.put(FieldType.TXN_ID.getName(), txnId);
				fields.put(FieldType.OID.getName(), oid);
				fields.put(FieldType.AMOUNT.getName(), amount);
				fields.put(FieldType.TOTAL_AMOUNT.getName(), totalAmount);
				fields.put(FieldType.CURRENCY_CODE.getName(), currency);
				fields.put(FieldType.MOP_TYPE.getName(), moptype);
				fields.put(FieldType.PAYMENT_TYPE.getName(), paymentType);
				fields.put(FieldType.PAYMENTS_REGION.getName(), paymentRegion);
				fields.put(FieldType.PAYER_ADDRESS.getName(), paymentAddress);
				fields.put(FieldType.RESPONSE_CODE.getName(), responseCode);
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), responseMessage);
				fields.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
				fields.put(FieldType.PAY_ID.getName(), payId);
				fields.put(FieldType.TXNTYPE.getName(), transactionType);
				fields.put(FieldType.RETURN_URL.getName(), returnUrl);
				fields.put(FieldType.ORDER_ID.getName(), orderId);
				fields.put(FieldType.PG_TXN_MESSAGE.getName(), detailMessage);

				String pgrefNum = fields.get(FieldType.PG_REF_NUM.getName());
				if (pgrefNum == null) {
					fields.put(FieldType.PG_REF_NUM.getName(), txnId);
				}
				field.insert(fields);
			}
		}
	}

	public void addOid(Fields fields) {
		String oid = fields.get(FieldType.OID.getName());
		// Oid is already present, return
		if (!StringUtils.isEmpty(oid)) {
			return;
		}

		String transactionType = fields.get(FieldType.TXNTYPE.getName());
		if (null != transactionType && transactionType.equals(TransactionType.NEWORDER.getName())) {
			oid = fields.get(FieldType.TXN_ID.getName());
		} else {
			String origOid = fields.get(FieldType.INTERNAL_ORIG_TXN_ID.getName());
			if (!StringUtils.isEmpty(origOid)) {
				oid = origOid;
			} else {
				// to add OID for transactions that are through webservice or
				// invalid
				oid = fields.get(FieldType.TXN_ID.getName());
			}
		}
		if (StringUtils.isEmpty(oid)) {
			logger.warn("Unable to add OID, this may cause some issues!");
		} else {
			fields.put(FieldType.OID.getName(), oid);
		}
	}

	public void postProcess(Fields fields) {

		Runnable runnable = new Runnable() {
			public void run() {
				try {

					TransactionSearch transactionDetail = transactionDetailsService
							.getSaleCaptureTransactionByOrderId(fields.get(FieldType.ORDER_ID.getName()));

					User user = null;

					if (propertiesManager.propertiesMap.get(Constants.USE_STATIC_DATA.getValue()) != null
							&& propertiesManager.propertiesMap.get(Constants.USE_STATIC_DATA.getValue())
									.equalsIgnoreCase(Constants.Y_FLAG.getValue())) {
						user = staticDataProvider.getUserData(fields.get(FieldType.PAY_ID.getName()));

					} else {
						user = userDao.findPayId(fields.get(FieldType.PAY_ID.getName()));

					}

					String status = fields.get(FieldType.STATUS.getName());
					String txnType = fields.get(FieldType.TXNTYPE.getName());
					
					if (StringUtils.isBlank(status)) {
						logger.info("Status is null");
					}
					if (StringUtils.isBlank(txnType)) {
						logger.info("TxnType is null");
					}
					
					if (StringUtils.isNotBlank(status) && StringUtils.isNotBlank(txnType) &&  status.equalsIgnoreCase(StatusType.CAPTURED.toString())
							&& txnType.equalsIgnoreCase(TxnType.REFUND.getName())) {

						logger.info("Sending Refund Transaction email and SMS to Customer & Merchant for Order Id= "
								+ fields.get(FieldType.ORDER_ID.getName()));

						if (StringUtils.isNotBlank(user.getSuperMerchantId())) {

							User superMerchant = null;

							if (propertiesManager.propertiesMap.get(Constants.USE_STATIC_DATA.getValue()) != null
									&& propertiesManager.propertiesMap.get(Constants.USE_STATIC_DATA.getValue())
											.equalsIgnoreCase(Constants.Y_FLAG.getValue())) {
								superMerchant = staticDataProvider.getUserData(user.getSuperMerchantId());

							} else {
								superMerchant = userDao.findPayId(user.getSuperMerchantId());

							}

							if (superMerchant.isRefundTransactionMerchantEmailFlag()) {
								logger.info("sending Refund Transaction email to Merchant");
								awsSESEmailService.sendRefundTransactionEmailToMerchant(fields, user);
							}
							if (superMerchant.isRefundTransactionCustomerEmailFlag()) {
								logger.info("sending Transaction email to Customer");
								awsSESEmailService.sendRefundTransactionEmailToCustomer(fields, user);
							}

							String custPhone = transactionDetail.getCustomerMobile();
							String merchPhone = user.getMobile();
							String totalAmount = Amount.toDecimal(fields.get(FieldType.TOTAL_AMOUNT.getName()),
									fields.get(FieldType.CURRENCY_CODE.getName()));
							String orderId = fields.get(FieldType.ORDER_ID.getName());

							if (superMerchant.isTransactionRefundCustomerSMSFlag()) {
								logger.info("sending Refund Transaction SMS to Customer");
								smsControllerServiceProvider.transactionRefundSmsForCustomer(custPhone, totalAmount,
										orderId);
							}
							if (superMerchant.isTransactionRefundMerchantSMSFlag()) {
								logger.info("sending Refund Transaction SMS to Merchant");
								smsControllerServiceProvider.transactionRefundSmsForMerchant(merchPhone, totalAmount,
										orderId);
							}

						} else {

							if (user.isRefundTransactionMerchantEmailFlag()) {
								logger.info("sending Refund Transaction email to Merchant");
								awsSESEmailService.sendRefundTransactionEmailToMerchant(fields, user);
							}
							if (user.isRefundTransactionCustomerEmailFlag()) {
								logger.info("sending Refund Transaction email to Customer");
								awsSESEmailService.sendRefundTransactionEmailToCustomer(fields, user);
							}

							String custPhone = transactionDetail.getCustomerMobile();
							String merchPhone = user.getMobile();
							String totalAmount = Amount.toDecimal(fields.get(FieldType.TOTAL_AMOUNT.getName()),
									fields.get(FieldType.CURRENCY_CODE.getName()));
							String orderId = fields.get(FieldType.ORDER_ID.getName());

							if (user.isTransactionRefundCustomerSMSFlag()) {
								logger.info("sending Refund Transaction SMS to Customer");
								smsControllerServiceProvider.transactionRefundSmsForCustomer(custPhone, totalAmount,
										orderId);
							}
							if (user.isTransactionRefundMerchantSMSFlag()) {
								logger.info("sending Refund Transaction SMS to Merchant");
								smsControllerServiceProvider.transactionRefundSmsForMerchant(merchPhone, totalAmount,
										orderId);
							}

						}

					}
				} catch (Exception e) {
					logger.error("exception in sending email to customer or merchant : ", e);
				}
			}

		};

		propertiesManager.executorImpl(runnable);

	}
}
