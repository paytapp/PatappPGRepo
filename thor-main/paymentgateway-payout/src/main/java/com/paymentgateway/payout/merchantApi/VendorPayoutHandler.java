package com.paymentgateway.payout.merchantApi;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.bson.Document;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.paymentgateway.commons.api.Hasher;
import com.paymentgateway.commons.api.TransactionControllerServiceProvider;
import com.paymentgateway.commons.dao.PayoutAcquirerMappingDao;
import com.paymentgateway.commons.dao.UserSettingDao;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.mongo.MongoInstance;
import com.paymentgateway.commons.user.Account;
import com.paymentgateway.commons.user.AccountCurrency;
import com.paymentgateway.commons.user.PayoutAcquireMapping;
import com.paymentgateway.commons.user.PayoutAcquirer;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserSettingData;
import com.paymentgateway.commons.util.AcquirerType;
import com.paymentgateway.commons.util.Amount;
import com.paymentgateway.commons.util.DateCreater;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PayoutPupose;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TransactionManager;
import com.paymentgateway.commons.util.UserStatusType;
import com.paymentgateway.payout.FloxyPay.FloxyPayService;
import com.paymentgateway.payout.apexPay.ApexPayService;
import com.paymentgateway.payout.cashfreePayout.CashfreePayoutService;
import com.paymentgateway.payout.fonePaisa.FonePaisaService;
import com.paymentgateway.payout.fonePaisa.Transaction;
import com.paymentgateway.payout.globalPay.GlobalPayService;
import com.paymentgateway.payout.icici.composite.Constants;
import com.paymentgateway.payout.icici.composite.IciciCommunicator;
import com.paymentgateway.payout.icici.composite.IciciResponseHandler;
import com.paymentgateway.payout.icici.composite.IciciTransactionConverter;
import com.paymentgateway.payout.qaicash.QaicashService;
import com.paymentgateway.payout.toshaniDigital.ToshaniDigitalService;
import com.paymentgateway.pg.core.util.CashfreeChecksumUtil;
import com.paymentgateway.pg.core.util.IciciUtil;

/**
 * @author Shiva
 *
 */
@Service
public class VendorPayoutHandler {

	@Autowired
	private VendorPayoutDao vendorPayoutDao;

	@Autowired
	private UserDao userDao;

	@Autowired
	private Fields field;

	@Autowired
	private IciciResponseHandler iciciResponseHandler;

	@Autowired
	private IciciTransactionConverter iciciTransactionConverter;

	@Autowired
	private IciciCommunicator iciciCommunicator;

	@Autowired
	private PayoutAcquirerMappingDao payoutAcquirerMappingDao;

	@Autowired
	private MongoInstance mongoInstance;

	@Autowired
	TransactionControllerServiceProvider transactionControllerServiceProvider;

	@Autowired
	private UserSettingDao userSettingDao;

	@Autowired
	private CashfreePayoutService cashfreePayoutService;

	@Autowired
	private IciciUtil iciciUtil;

	@Autowired
	private CashfreeChecksumUtil cashfreeChecksumUtil;

	@Autowired
	private ApexPayService apexPayService;

	@Autowired
	private FonePaisaService fonePaisaService;

	@Autowired
	private FloxyPayService floxyPayService;

	@Autowired
	private QaicashService qaicashService;

	@Autowired
	private ToshaniDigitalService toshaniDigitalService;

	@Autowired
	private GlobalPayService globalPayService;

	private static final String prefix = "MONGO_DB_";

	private static Logger logger = LoggerFactory.getLogger(VendorPayoutHandler.class.getName());

	public boolean validateHashForApi(Fields fields) throws SystemException {
		String fieldHash = fields.get(FieldType.HASH.getName());
		fields.remove(FieldType.HASH.getName());

		try {
			logger.info("Hash from Merchant :" + fieldHash);
			String hash = Hasher.getHash(fields);
			logger.info("Hash :" + hash);
			if (!hash.equals(fieldHash)) {
				logger.info("OrderId: " + fields.get(FieldType.ORDER_ID.getName()) + " Calculated hash : " + hash
						+ " Merchant hash " + fieldHash);
				return false;
			}
		} catch (SystemException e) {
			logger.error("Exception in hash validation ", e);

			fields.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
			fields.put(FieldType.PG_TXN_MESSAGE.getName(), ErrorType.INVALID_PAYID_ATTEMPT.getResponseMessage());
			fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_PAYID_ATTEMPT.getResponseCode());
			return false;
		}

		return true;
	}

	public void checkDuplicateOrderId(Fields fields) throws SystemException {
		vendorPayoutDao.checkDuplicateOrderId(fields);
	}

	public void checkDuplicateVpaOrderId(Fields fields) throws SystemException {
		vendorPayoutDao.checkVpaDuplicateOrderId(fields);
	}

	public void checkDuplicateOrderIdPayout(Fields fields) throws SystemException {
		vendorPayoutDao.checkDuplicateOrderIdMerchantInitiatedDirect(fields);
	}

	public boolean isDailyLimitExceed(Fields fields) {
		logger.info("Inside  isDailyLimitExceed()");

		try {
			String fieldAmount = Amount.toDecimal(fields.get(FieldType.AMOUNT.getName()),
					fields.get(FieldType.CURRENCY_CODE.getName()));

			BigDecimal closingTransactionDiff = vendorPayoutDao.getClosingTransactionAmount(fields);

			if (closingTransactionDiff.compareTo(BigDecimal.ZERO) <= 0) {
				insertInsufficientDataEntry(fields);
				return true;
			}

			BigDecimal checkDiff = new BigDecimal(0);
			checkDiff = checkDiff.add(closingTransactionDiff).setScale(2);
			checkDiff = checkDiff.subtract(new BigDecimal(fieldAmount).setScale(2));

			logger.info("Calculated final settled amount is " + checkDiff);

			if (checkDiff.compareTo(BigDecimal.ZERO) >= 0) {
				return false;
			} else {
				insertInsufficientDataEntry(fields);
			}

			return true;
		} catch (Exception e) {
			logger.error("Exception Declined due to insufficient balance , " + e);
		}
		return true;
	}

	public void insertInsufficientDataEntry(Fields fields) {
		try {
			if (StringUtils.isBlank(fields.get(FieldType.BENE_ACCOUNT_NO.getName()))) {
				fields.put(FieldType.TXNTYPE.getName(), "UPI");
			} else {
				fields.put(FieldType.TXNTYPE.getName(), "IMPS");
			}
			fields.put(FieldType.AMOUNT.getName(), Amount.toDecimal(fields.get(FieldType.AMOUNT.getName()),
					fields.get(FieldType.CURRENCY_CODE.getName())));
			String payId = fields.get(FieldType.PAY_ID.getName());
			fields.put(FieldType.USER_TYPE.getName(), "Merchant Initiated Direct");
			fields.put(FieldType.TXN_ID.getName(), TransactionManager.getId());
			fields.put(FieldType.STATUS.getName(), StatusType.DECLINED.getName());
			fields.put(FieldType.PG_TXN_MESSAGE.getName(),
					ErrorType.DECLINED_BY_INSUFFICIENT_BALANCE.getResponseMessage());
			fields.put(FieldType.RESPONSE_MESSAGE.getName(),
					ErrorType.DECLINED_BY_INSUFFICIENT_BALANCE.getResponseMessage());
			fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.DECLINED_BY_INSUFFICIENT_BALANCE.getCode());
			fields.put(FieldType.IS_STATUS_FINAL.getName(), "Y");
			User user = userDao.findPayId(payId);

			fields.put(FieldType.VIRTUAL_AC_CODE.getName(), user.getVirtualAccountNo());
			if (StringUtils.isNotBlank(user.getSuperMerchantId())) {
				fields.put(FieldType.SUB_MERCHANT_ID.getName(), fields.get(FieldType.PAY_ID.getName()));
				fields.put(FieldType.PAY_ID.getName(), user.getSuperMerchantId());
			}
			Date dNow = new Date();
			String dateNow = DateCreater.formatDateForDb(dNow);
			fields.put(FieldType.CREATE_DATE.getName(), dateNow);
			fields.put(FieldType.DATE_INDEX.getName(), dateNow.substring(0, 10).replace("-", ""));
			fields.put(FieldType.UPDATE_DATE.getName(), dateNow);

			if (fields.contains("flagBulk")) {
				fields.remove("flagBulk");
				field.updateIciciIMPSBulkTransaction(fields);
			} else {
				field.insertIciciCompositeFields(fields);
			}

			if (StringUtils.isNotBlank(user.getSuperMerchantId())) {
				fields.put(FieldType.PAY_ID.getName(), fields.get(FieldType.SUB_MERCHANT_ID.getName()));
				fields.remove(FieldType.SUB_MERCHANT_ID.getName());
			}
			fields.remove(FieldType.DATE_INDEX.getName());
			fields.remove(FieldType.CREATE_DATE.getName());
			fields.remove(FieldType.UPDATE_DATE.getName());
			fields.remove(FieldType.IS_STATUS_FINAL.getName());
			fields.remove(FieldType.VIRTUAL_AC_CODE.getName());
			fields.remove(FieldType.TXN_ID.getName());
			fields.remove(FieldType.TXNTYPE.getName());
			fields.remove(FieldType.USER_TYPE.getName());
			fields.put(FieldType.AMOUNT.getName(), Amount.formatAmount(fields.get(FieldType.AMOUNT.getName()),
					fields.get(FieldType.CURRENCY_CODE.getName())));
		} catch (Exception e) {
			logger.error("Exception Declined due to insufficient balance , " + e);
		}
	}

	public void getBeneVerificationAmount(Fields fields) throws SystemException {

		String amount = PropertiesManager.propertiesMap.get("BeneVerificationAmount");
		BigDecimal amountInDecimal = new BigDecimal(amount).setScale(2);

		fields.put(FieldType.AMOUNT.getName(), Amount.formatAmount(String.valueOf(amountInDecimal), "356"));
	}

	public void clearFieldsForBeneVerification(Fields fields) throws SystemException {
		fields.remove(FieldType.CREATE_DATE.getName());
		fields.remove(FieldType.PG_TXN_MESSAGE.getName());
		fields.remove(FieldType.RRN.getName());
		fields.remove(FieldType.TXNTYPE.getName());
		fields.remove(FieldType.PG_RESP_CODE.getName());
		fields.remove(FieldType.SUB_WALLET_ID.getName());
		fields.remove(FieldType.ACQUIRER_NAME.getName());
		fields.remove(FieldType.USER_TYPE.getName());
		fields.remove(FieldType.AMOUNT.getName());
		fields.remove(FieldType.BANK_REF_NUM.getName());
		fields.remove(FieldType.PG_REF_NUM.getName());
		fields.remove(FieldType.PG_TXN_STATUS.getName());
		fields.remove(FieldType.SUB_MERCHANT_ID.getName());
		fields.remove(FieldType.PURPOSE.getName());
		fields.remove(FieldType.CREATE_DATE.getName());
		fields.remove(FieldType.VIRTUAL_AC_CODE.getName());
		fields.remove(FieldType.PURPOSE.getName());
	}

	public void clearFieldsForVPAVerification(Fields fields) throws SystemException {
		fields.remove(FieldType.CREATE_DATE.getName());
		fields.remove(FieldType.PG_TXN_MESSAGE.getName());
		fields.remove(FieldType.RRN.getName());
		fields.remove(FieldType.TXNTYPE.getName());
		fields.remove(FieldType.PG_RESP_CODE.getName());
		// fields.remove(FieldType.TXN_ID.getName());
		// fields.remove(FieldType.USER_TYPE.getName());
		// fields.remove(FieldType.BANK_REF_NUM.getName());
		fields.remove(FieldType.PG_REF_NUM.getName());
		fields.remove(FieldType.PG_TXN_STATUS.getName());
		fields.remove(FieldType.CREATE_DATE.getName());
		fields.remove(FieldType.SUB_MERCHANT_ID.getName());
	}

	public void clearFieldsForMerchantDirect(Fields fields) {
		fields.remove(FieldType.USER_TYPE.getName());
		fields.remove(FieldType.BANK_REF_NUM.getName());
		fields.remove(FieldType.SUB_MERCHANT_ID.getName());
		fields.remove(FieldType.ACQUIRER_NAME.getName());
		fields.remove(FieldType.SUB_WALLET_ID.getName());
		fields.remove(FieldType.USER_TYPE.getName());
		fields.remove(FieldType.VIRTUAL_AC_CODE.getName());
		fields.remove(FieldType.RESELLER_ID.getName());
		fields.remove(FieldType.ADF1.getName());
		fields.remove(FieldType.ADF2.getName());
		fields.remove(FieldType.ADF3.getName());
		fields.remove(FieldType.ADF4.getName());
		fields.remove(FieldType.ADF5.getName());
		fields.remove(FieldType.ADF6.getName());
		fields.remove(FieldType.ADF7.getName());
		fields.remove(FieldType.ADF8.getName());
		fields.remove(FieldType.ADF9.getName());
		fields.remove(FieldType.ADF10.getName());
		fields.remove(FieldType.IS_STATUS_FINAL.getName());
	}

	public boolean isMendatoryFieldEmpty(Fields fields) throws SystemException {
		if (StringUtils.isBlank(fields.get(FieldType.ORDER_ID.getName()))) {
			fields.put(FieldType.STATUS.getName(), StatusType.ERROR.getName());
			fields.put(FieldType.PG_TXN_MESSAGE.getName(), ErrorType.VALIDATION_FAILED.getResponseMessage());
			fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.VALIDATION_FAILED.getResponseCode());
			return true;
		}
		if (StringUtils.isBlank(fields.get(FieldType.PAY_ID.getName()))) {
			fields.put(FieldType.STATUS.getName(), StatusType.ERROR.getName());
			fields.put(FieldType.PG_TXN_MESSAGE.getName(), ErrorType.VALIDATION_FAILED.getResponseMessage());
			fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.VALIDATION_FAILED.getResponseCode());
			return true;
		}
		if (StringUtils.isBlank(fields.get(FieldType.BENE_NAME.getName()))) {
			fields.put(FieldType.STATUS.getName(), StatusType.ERROR.getName());
			fields.put(FieldType.PG_TXN_MESSAGE.getName(), ErrorType.VALIDATION_FAILED.getResponseMessage());
			fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.VALIDATION_FAILED.getResponseCode());
			return true;
		}
		if (StringUtils.isBlank(fields.get(FieldType.IFSC_CODE.getName()))) {
			fields.put(FieldType.STATUS.getName(), StatusType.ERROR.getName());
			fields.put(FieldType.PG_TXN_MESSAGE.getName(), ErrorType.VALIDATION_FAILED.getResponseMessage());
			fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.VALIDATION_FAILED.getResponseCode());
			return true;
		}
		if (StringUtils.isBlank(fields.get(FieldType.BENE_ACCOUNT_NO.getName()))) {
			fields.put(FieldType.STATUS.getName(), StatusType.ERROR.getName());
			fields.put(FieldType.PG_TXN_MESSAGE.getName(), ErrorType.VALIDATION_FAILED.getResponseMessage());
			fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.VALIDATION_FAILED.getResponseCode());
			return true;
		}
		if (StringUtils.isBlank(fields.get(FieldType.CURRENCY_CODE.getName()))) {
			fields.put(FieldType.STATUS.getName(), StatusType.ERROR.getName());
			fields.put(FieldType.PG_TXN_MESSAGE.getName(), ErrorType.VALIDATION_FAILED.getResponseMessage());
			fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.VALIDATION_FAILED.getResponseCode());
			return true;
		}
		if (StringUtils.isBlank(fields.get(FieldType.AMOUNT.getName()))) {
			fields.put(FieldType.STATUS.getName(), StatusType.ERROR.getName());
			fields.put(FieldType.PG_TXN_MESSAGE.getName(), ErrorType.VALIDATION_FAILED.getResponseMessage());
			fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.VALIDATION_FAILED.getResponseCode());
			return true;
		}
		if (StringUtils.isBlank(fields.get(FieldType.PURPOSE.getName()))) {
			fields.put(FieldType.STATUS.getName(), StatusType.ERROR.getName());
			fields.put(FieldType.PG_TXN_MESSAGE.getName(), ErrorType.VALIDATION_FAILED.getResponseMessage());
			fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.VALIDATION_FAILED.getResponseCode());
			return true;
		}
		/*
		 * if (StringUtils.isBlank(fields.get(FieldType.TXNTYPE.getName()))) {
		 * fields.put(FieldType.STATUS.getName(), StatusType.ERROR.getName());
		 * fields.put(FieldType.PG_TXN_MESSAGE.getName(),
		 * ErrorType.VALIDATION_FAILED.getResponseMessage());
		 * fields.put(FieldType.RESPONSE_CODE.getName(),
		 * ErrorType.VALIDATION_FAILED.getResponseCode()); return true; }
		 */

		return false;

	}

	public boolean isMandatoryFieldEmptyForUpi(Fields fields) throws SystemException {
		if (StringUtils.isBlank(fields.get(FieldType.ORDER_ID.getName()))) {
			fields.put(FieldType.STATUS.getName(), StatusType.ERROR.getName());
			fields.put(FieldType.PG_TXN_MESSAGE.getName(), ErrorType.VALIDATION_FAILED.getResponseMessage());
			fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.VALIDATION_FAILED.getResponseCode());
			return true;
		}
		if (StringUtils.isBlank(fields.get(FieldType.PAY_ID.getName()))) {
			fields.put(FieldType.STATUS.getName(), StatusType.ERROR.getName());
			fields.put(FieldType.PG_TXN_MESSAGE.getName(), ErrorType.VALIDATION_FAILED.getResponseMessage());
			fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.VALIDATION_FAILED.getResponseCode());
			return true;
		}
		if (StringUtils.isBlank(fields.get(FieldType.PAYER_NAME.getName()))) {
			fields.put(FieldType.STATUS.getName(), StatusType.ERROR.getName());
			fields.put(FieldType.PG_TXN_MESSAGE.getName(), ErrorType.VALIDATION_FAILED.getResponseMessage());
			fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.VALIDATION_FAILED.getResponseCode());
			return true;
		}
		if (StringUtils.isBlank(fields.get(FieldType.PAYER_ADDRESS.getName()))) {
			fields.put(FieldType.STATUS.getName(), StatusType.ERROR.getName());
			fields.put(FieldType.PG_TXN_MESSAGE.getName(), ErrorType.VALIDATION_FAILED.getResponseMessage());
			fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.VALIDATION_FAILED.getResponseCode());
			return true;
		}
		if (StringUtils.isBlank(fields.get(FieldType.CURRENCY_CODE.getName()))) {
			fields.put(FieldType.STATUS.getName(), StatusType.ERROR.getName());
			fields.put(FieldType.PG_TXN_MESSAGE.getName(), ErrorType.VALIDATION_FAILED.getResponseMessage());
			fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.VALIDATION_FAILED.getResponseCode());
			return true;
		}
		if (StringUtils.isBlank(fields.get(FieldType.AMOUNT.getName()))) {
			fields.put(FieldType.STATUS.getName(), StatusType.ERROR.getName());
			fields.put(FieldType.PG_TXN_MESSAGE.getName(), ErrorType.VALIDATION_FAILED.getResponseMessage());
			fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.VALIDATION_FAILED.getResponseCode());
			return true;
		}
		if (StringUtils.isBlank(fields.get(FieldType.PURPOSE.getName()))) {
			fields.put(FieldType.STATUS.getName(), StatusType.ERROR.getName());
			fields.put(FieldType.PG_TXN_MESSAGE.getName(), ErrorType.VALIDATION_FAILED.getResponseMessage());
			fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.VALIDATION_FAILED.getResponseCode());
			return true;
		}
		/*
		 * if (StringUtils.isBlank(fields.get(FieldType.TXNTYPE.getName()))) {
		 * fields.put(FieldType.STATUS.getName(), StatusType.ERROR.getName());
		 * fields.put(FieldType.PG_TXN_MESSAGE.getName(),
		 * ErrorType.VALIDATION_FAILED.getResponseMessage());
		 * fields.put(FieldType.RESPONSE_CODE.getName(),
		 * ErrorType.VALIDATION_FAILED.getResponseCode()); return true; }
		 */

		return false;

	}

	public boolean isMendatoryFieldEmptyForBeneVerification(Fields fields) throws SystemException {
		if (StringUtils.isBlank(fields.get(FieldType.ORDER_ID.getName()))) {
			fields.put(FieldType.STATUS.getName(), StatusType.ERROR.getName());
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.VALIDATION_FAILED.getResponseMessage());
			fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.VALIDATION_FAILED.getResponseCode());
			logger.info("INVALID ORDER_ID " + fields.getFields());
			return true;
		}
		if (StringUtils.isBlank(fields.get(FieldType.PAY_ID.getName()))) {
			fields.put(FieldType.STATUS.getName(), StatusType.ERROR.getName());
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.VALIDATION_FAILED.getResponseMessage());
			fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.VALIDATION_FAILED.getResponseCode());
			logger.info("INVALID PAY_ID " + fields.getFields());
			return true;
		}
		if (StringUtils.isBlank(fields.get(FieldType.BENE_NAME.getName()))) {
			fields.put(FieldType.STATUS.getName(), StatusType.ERROR.getName());
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.VALIDATION_FAILED.getResponseMessage());
			fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.VALIDATION_FAILED.getResponseCode());
			logger.info("INVALID BENE_NAME " + fields.getFields());
			return true;
		}
		if (StringUtils.isBlank(fields.get(FieldType.IFSC_CODE.getName()))) {
			fields.put(FieldType.STATUS.getName(), StatusType.ERROR.getName());
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.VALIDATION_FAILED.getResponseMessage());
			fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.VALIDATION_FAILED.getResponseCode());
			logger.info("INVALID IFSC " + fields.getFields());
			return true;
		}
		if (StringUtils.isBlank(fields.get(FieldType.BENE_ACCOUNT_NO.getName()))) {
			fields.put(FieldType.STATUS.getName(), StatusType.ERROR.getName());
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.VALIDATION_FAILED.getResponseMessage());
			fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.VALIDATION_FAILED.getResponseCode());
			logger.info("INVALID BENE_ACC_NO " + fields.getFields());
			return true;
		}
		if (StringUtils.isBlank(fields.get(FieldType.CURRENCY_CODE.getName()))) {
			fields.put(FieldType.STATUS.getName(), StatusType.ERROR.getName());
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.VALIDATION_FAILED.getResponseMessage());
			fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.VALIDATION_FAILED.getResponseCode());
			logger.info("INVALID CURRENCY_CODE " + fields.getFields());
			return true;
		}
		// if (StringUtils.isBlank(fields.get(FieldType.HASH.getName()))) {
		// fields.put(FieldType.STATUS.getName(), StatusType.ERROR.getName());
		// fields.put(FieldType.RESPONSE_MESSAGE.getName(),
		// ErrorType.VALIDATION_FAILED.getResponseMessage());
		// fields.put(FieldType.RESPONSE_CODE.getName(),
		// ErrorType.VALIDATION_FAILED.getResponseCode());
		// logger.info("INVALID HASH "+fields.getFields());
		// return true;
		// }

		return false;

	}

	public boolean isUpiMendatoryFieldEmptyForBeneVerification(Fields fields) throws SystemException {

		if (StringUtils.isBlank(fields.get(FieldType.PAY_ID.getName()))) {
			fields.put(FieldType.STATUS.getName(), StatusType.ERROR.getName());
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.VALIDATION_FAILED.getResponseMessage());
			fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.VALIDATION_FAILED.getResponseCode());
			logger.info("INVALID PAY_ID " + fields.getFields());
			return true;
		}

		if (StringUtils.isBlank(fields.get(FieldType.PAYER_ADDRESS.getName()))) {
			fields.put(FieldType.STATUS.getName(), StatusType.ERROR.getName());
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.VALIDATION_FAILED.getResponseMessage());
			fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.VALIDATION_FAILED.getResponseCode());
			logger.info("INVALID VPA " + fields.getFields());
			return true;
		}

		return false;

	}

	public boolean isMerchantAllowed(Fields fields) {
		UserSettingData merchntSettings = userSettingDao.fetchDataUsingPayId(fields.get(FieldType.PAY_ID.getName()));

		if (merchntSettings.isAccountVerificationFlag()) {
			logger.info("Merchant Allowed to user");
			return true;
		}
		return false;
	}

	public boolean isVpaVerificationMerchantAllowed(Fields fields) {
		UserSettingData merchntSettings = userSettingDao.fetchDataUsingPayId(fields.get(FieldType.PAY_ID.getName()));

		if (merchntSettings.isVpaVerificationFlag()) {
			logger.info("Merchant Allowed to user");
			return true;
		}
		return false;
	}

	public boolean isDirectTransctionAllowed(Fields fields) {
		UserSettingData merchntSettings = userSettingDao.fetchDataUsingPayId(fields.get(FieldType.PAY_ID.getName()));
		return merchntSettings.isMerchantInitiatedDirectFlag();
	}

	public boolean isInDirectTransctionAllowed(Fields fields) {
		User user = userDao.findPayId(fields.get(FieldType.PAY_ID.getName()));
		return user.isVendorPayOutFlag();
	}

	public boolean isDuplicateAccountNumber(Fields fields) throws SystemException {

		boolean duplicateAccountFound = vendorPayoutDao.checkDuplicateAccountNumber(fields);

		if (duplicateAccountFound) {
			fields.put(FieldType.TXN_ID.getName(), TransactionManager.getNewTransactionId());
			fields.put(FieldType.USER_TYPE.getName(), "Verification");
			String payId = fields.get(FieldType.PAY_ID.getName());

			User user = userDao.findPayId(payId);

			if (StringUtils.isNotBlank(user.getSuperMerchantId())) {
				fields.put(FieldType.SUB_MERCHANT_ID.getName(), fields.get(FieldType.PAY_ID.getName()));
				fields.put(FieldType.PAY_ID.getName(), user.getSuperMerchantId());
			}
			field.insertIciciIMPSTransaction(fields);

			// returning subMerchant Id in Payid for return in response
			if (StringUtils.isNotBlank(user.getSuperMerchantId())) {
				fields.put(FieldType.PAY_ID.getName(), user.getSuperMerchantId());
				fields.remove(FieldType.SUB_MERCHANT_ID.getName());
			}

			String status = fields.get(FieldType.STATUS.getName());

			if (status.equalsIgnoreCase(StatusType.CAPTURED.getName())) {
				fields.put(FieldType.STATUS.getName(), StatusType.VERIFIED.getName());
			} else if (status.equalsIgnoreCase(StatusType.TIMEOUT.getName())) {
				fields.put(FieldType.STATUS.getName(), StatusType.PENDING.getName());
			} else {
				fields.put(FieldType.STATUS.getName(), StatusType.FAILED.getName());
			}
		}

		return duplicateAccountFound;
	}

	public boolean isDuplicateVPA(Fields fields) throws SystemException {

		boolean duplicateVpaFound = vendorPayoutDao.checkDuplicateVPA(fields);

		if (duplicateVpaFound) {

			// insert new Transaction with new TXN id
			fields.put(FieldType.TXN_ID.getName(), TransactionManager.getNewTransactionId());
			fields.put(FieldType.USER_TYPE.getName(), "Verification");
			String payId = fields.get(FieldType.PAY_ID.getName());

			User user = userDao.findPayId(payId);

			if (StringUtils.isNotBlank(user.getSuperMerchantId())) {
				fields.put(FieldType.SUB_MERCHANT_ID.getName(), fields.get(FieldType.PAY_ID.getName()));
				fields.put(FieldType.PAY_ID.getName(), user.getSuperMerchantId());
			}

			field.insertIciciIMPSTransaction(fields);

			// returning subMerchant Id in Payid for return in response
			if (StringUtils.isNotBlank(user.getSuperMerchantId())) {
				fields.put(FieldType.PAY_ID.getName(), user.getSuperMerchantId());
				fields.remove(FieldType.SUB_MERCHANT_ID.getName());
			}

			String status = fields.get(FieldType.STATUS.getName());

			if (status.equalsIgnoreCase(StatusType.CAPTURED.getName())) {
				fields.put(FieldType.STATUS.getName(), StatusType.VERIFIED.getName());
			} else if (status.equalsIgnoreCase(StatusType.TIMEOUT.getName())) {
				fields.put(FieldType.STATUS.getName(), StatusType.PENDING.getName());
			} else {
				fields.put(FieldType.STATUS.getName(), StatusType.FAILED.getName());
			}
		}
		return duplicateVpaFound;
	}

	public void checkBeneVerificationStatus(Fields fields) {
		vendorPayoutDao.getBeneData(fields);

	}

	public void checkVendorPayoutStatus(Fields fields) {
		vendorPayoutDao.getVendorPayoutData(fields);

	}

	public boolean isPayIdValid(Fields fields) {
		User user = userDao.findPayId(fields.get(FieldType.PAY_ID.getName()));

		if (user != null) {
			return true;
		}
		return false;
	}

	public void communicateCompositeTransaction(Fields fields) {
		logger.info("inside communicateCompositeTransaction()");
		try {

			// fetching acq mapping from DB
			PayoutAcquireMapping payoutAcquireMapping = payoutAcquirerMappingDao
					.fetchSavedMappingByPayId(fields.get(FieldType.PAY_ID.getName()));

			JSONObject adfFields;

			if (StringUtils.isNotBlank(payoutAcquireMapping.getAdfFields())) {
				adfFields = new JSONObject(payoutAcquireMapping.getAdfFields());
			} else {
				fields.put(FieldType.STATUS.getName(), StatusType.REJECTED_BY_PG.getName());
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.ACQUIRER_ERROR.getResponseMessage());
				fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.ACQUIRER_ERROR.getResponseCode());
				fields.put(FieldType.HASH.getName(), Hasher.getHash(fields));
				logger.info("Hash Invalid For Request ORDER_ID " + fields.get(FieldType.ORDER_ID.getName())
						+ " fields are " + fields.maskFieldsRequest(fields.getFields()));
				return;
			}

			fields.put(FieldType.AMOUNT.getName(), Amount.toDecimal(fields.get(FieldType.AMOUNT.getName()),
					fields.get(FieldType.CURRENCY_CODE.getName())));

			String url = adfFields.getString(Constants.ADF_9);
			String priorty = Constants.IMPS_PRIORITY;
			String apiKey = adfFields.getString(Constants.ADF_7);

			String request = iciciTransactionConverter.createCompositeIMPSRequest(fields, adfFields);

			// Closing Amount
			if ((StringUtils.isNotBlank(fields.get(FieldType.USER_TYPE.getName()))) && ((fields
					.get(FieldType.USER_TYPE.getName()).equalsIgnoreCase("Merchant Initiated Direct"))
					|| (fields.get(FieldType.USER_TYPE.getName()).equalsIgnoreCase("Merchant Initiated Indirect")))) {
				logger.info("Insertion from IMPS");
				vendorPayoutDao.insertUpdateForClosing(fields);
			}

			String response = iciciCommunicator.getIciciCompositeApiResponse(request, url, fields, priorty, apiKey,
					adfFields);
			iciciResponseHandler.compositeIMPSTransactionResponseHandler(fields, response);

			fields.put(FieldType.AMOUNT.getName(), Amount.formatAmount(fields.get(FieldType.AMOUNT.getName()),
					fields.get(FieldType.CURRENCY_CODE.getName())));
		} catch (Exception e) {
			logger.info("Exception in comunication with Compostie IMPS for txn Id"
					+ field.get(FieldType.TXN_ID.getName()) + " ", e);
		}
	}

	public void communicateCompositeNeftTransaction(Fields fields) {
		logger.info("inside communicateCompositeNeftTransaction()");
		try {

			// fetching acq mapping from DB
			PayoutAcquireMapping payoutAcquireMapping = payoutAcquirerMappingDao
					.fetchSavedMappingByPayId(fields.get(FieldType.PAY_ID.getName()));

			JSONObject adfFields;

			if (StringUtils.isNotBlank(payoutAcquireMapping.getAdfFields())) {
				adfFields = new JSONObject(payoutAcquireMapping.getAdfFields());
			} else {
				fields.put(FieldType.STATUS.getName(), StatusType.REJECTED_BY_PG.getName());
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.ACQUIRER_ERROR.getResponseMessage());
				fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.ACQUIRER_ERROR.getResponseCode());
				fields.put(FieldType.HASH.getName(), Hasher.getHash(fields));
				logger.info("Hash Invalid For Request ORDER_ID " + fields.get(FieldType.ORDER_ID.getName())
						+ " fields are " + fields.maskFieldsRequest(fields.getFields()));
				return;
			}

			fields.put(FieldType.AMOUNT.getName(), Amount.toDecimal(fields.get(FieldType.AMOUNT.getName()),
					fields.get(FieldType.CURRENCY_CODE.getName())));

			String url = adfFields.getString(Constants.ADF_9);
			String priorty = Constants.NEFT_PRIORITY;
			String apiKey = adfFields.getString(Constants.ADF_7);

			String request = iciciTransactionConverter.createCompositeNEFTRequest(fields, adfFields);

			// Closing Amount
			if ((StringUtils.isNotBlank(fields.get(FieldType.USER_TYPE.getName()))) && ((fields
					.get(FieldType.USER_TYPE.getName()).equalsIgnoreCase("Merchant Initiated Direct"))
					|| (fields.get(FieldType.USER_TYPE.getName()).equalsIgnoreCase("Merchant Initiated Indirect")))) {
				logger.info("Insertion from NEFT/RTGS");
				vendorPayoutDao.insertUpdateForClosing(fields);
			}

			String response = iciciCommunicator.getIciciCompositeApiResponse(request, url, fields, priorty, apiKey,
					adfFields);
			iciciResponseHandler.compositeRTGSNEFTTransactionResponseHandler(fields, response);

			fields.put(FieldType.AMOUNT.getName(), Amount.formatAmount(fields.get(FieldType.AMOUNT.getName()),
					fields.get(FieldType.CURRENCY_CODE.getName())));
		} catch (Exception e) {
			logger.info("Exception in comunication with Compostie IMPS for txn Id"
					+ field.get(FieldType.TXN_ID.getName()) + " ", e);
		}
	}

	public void communicateCompositeRtgsTransaction(Fields fields) {
		logger.info("inside communicateCompositeRtgsTransaction()");
		try {

			// fetching acq mapping from DB
			PayoutAcquireMapping payoutAcquireMapping = payoutAcquirerMappingDao
					.fetchSavedMappingByPayId(fields.get(FieldType.PAY_ID.getName()));

			JSONObject adfFields;

			if (StringUtils.isNotBlank(payoutAcquireMapping.getAdfFields())) {
				adfFields = new JSONObject(payoutAcquireMapping.getAdfFields());
			} else {
				fields.put(FieldType.STATUS.getName(), StatusType.REJECTED_BY_PG.getName());
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.ACQUIRER_ERROR.getResponseMessage());
				fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.ACQUIRER_ERROR.getResponseCode());
				fields.put(FieldType.HASH.getName(), Hasher.getHash(fields));
				logger.info("Hash Invalid For Request ORDER_ID " + fields.get(FieldType.ORDER_ID.getName())
						+ " fields are " + fields.maskFieldsRequest(fields.getFields()));
				return;
			}

			fields.put(FieldType.AMOUNT.getName(), Amount.toDecimal(fields.get(FieldType.AMOUNT.getName()),
					fields.get(FieldType.CURRENCY_CODE.getName())));

			String url = adfFields.getString(Constants.ADF_9);
			String priorty = Constants.RTGS_PRIORITY;
			String apiKey = adfFields.getString(Constants.ADF_7);

			String request = iciciTransactionConverter.createCompositeRTGSRequest(fields, adfFields);

			// Closing Amount
			if ((StringUtils.isNotBlank(fields.get(FieldType.USER_TYPE.getName()))) && ((fields
					.get(FieldType.USER_TYPE.getName()).equalsIgnoreCase("Merchant Initiated Direct"))
					|| (fields.get(FieldType.USER_TYPE.getName()).equalsIgnoreCase("Merchant Initiated Indirect")))) {
				logger.info("Insertion from RTGS");
				vendorPayoutDao.insertUpdateForClosing(fields);
			}

			String response = iciciCommunicator.getIciciCompositeApiResponse(request, url, fields, priorty, apiKey,
					adfFields);
			iciciResponseHandler.compositeRTGSNEFTTransactionResponseHandler(fields, response);

			fields.put(FieldType.AMOUNT.getName(), Amount.formatAmount(fields.get(FieldType.AMOUNT.getName()),
					fields.get(FieldType.CURRENCY_CODE.getName())));
		} catch (Exception e) {
			logger.info("Exception in comunication with Compostie RTGS for txn Id"
					+ field.get(FieldType.TXN_ID.getName()) + " ", e);
		}
	}

	public void communicateCompositeUPITransaction(Fields fields) {
		logger.info("inside communicateCompositeUPITransaction()");
		try {

			// fetching acq mapping from DB
			PayoutAcquireMapping payoutAcquireMapping = payoutAcquirerMappingDao
					.fetchSavedMappingByPayId(fields.get(FieldType.PAY_ID.getName()));

			JSONObject adfFields;

			if (StringUtils.isNotBlank(payoutAcquireMapping.getAdfFields())) {
				adfFields = new JSONObject(payoutAcquireMapping.getAdfFields());
			} else {
				fields.put(FieldType.STATUS.getName(), StatusType.REJECTED_BY_PG.getName());
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.ACQUIRER_ERROR.getResponseMessage());
				fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.ACQUIRER_ERROR.getResponseCode());
				fields.put(FieldType.HASH.getName(), Hasher.getHash(fields));
				logger.info("Hash Invalid For Request ORDER_ID " + fields.get(FieldType.ORDER_ID.getName())
						+ " fields are " + fields.maskFieldsRequest(fields.getFields()));
				return;
			}

			fields.put(FieldType.AMOUNT.getName(), Amount.toDecimal(fields.get(FieldType.AMOUNT.getName()),
					fields.get(FieldType.CURRENCY_CODE.getName())));

			String url = adfFields.getString(Constants.ADF_9);
			String priorty = Constants.UPI_PRIORITY;
			String apiKey = adfFields.getString(Constants.ADF_7);

			String request = iciciTransactionConverter.createCompositeUPIRequest(fields, adfFields);

			// insert Closing Amount
			if ((StringUtils.isNotBlank(fields.get(FieldType.USER_TYPE.getName()))) && ((fields
					.get(FieldType.USER_TYPE.getName()).equalsIgnoreCase("Merchant Initiated Direct"))
					|| (fields.get(FieldType.USER_TYPE.getName()).equalsIgnoreCase("Merchant Initiated Indirect")))) {
				logger.info("Insertion from UPI");
				vendorPayoutDao.insertUpdateForClosing(fields);
			}

			String response = iciciCommunicator.getIciciCompositeApiResponse(request, url, fields, priorty, apiKey,
					adfFields);

			iciciResponseHandler.compositeUPITransactionResponseHandler(fields, response);

			fields.put(FieldType.AMOUNT.getName(), Amount.formatAmount(fields.get(FieldType.AMOUNT.getName()),
					fields.get(FieldType.CURRENCY_CODE.getName())));
		} catch (Exception e) {
			logger.info("Exception in comunication with Compostie UPI for txn Id"
					+ field.get(FieldType.TXN_ID.getName()) + " ", e);
		}

	}

	public void communicateCompositeUPITransactionStatusEnq(Fields fields, PayoutAcquireMapping payoutAcquireMapping) {
		logger.info("inside communicateCompositeUPITransactionStatusEnq()");
		try {

			JSONObject adfFields;

			if (StringUtils.isNotBlank(payoutAcquireMapping.getAdfFields())) {
				adfFields = new JSONObject(payoutAcquireMapping.getAdfFields());
			} else {
				fields.put(FieldType.STATUS.getName(), StatusType.REJECTED_BY_PG.getName());
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.ACQUIRER_ERROR.getResponseMessage());
				fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.ACQUIRER_ERROR.getResponseCode());
				fields.put(FieldType.HASH.getName(), Hasher.getHash(fields));
				logger.info("Hash Invalid For Request ORDER_ID " + fields.get(FieldType.ORDER_ID.getName())
						+ " fields are " + fields.maskFieldsRequest(fields.getFields()));
				return;
			}

			fields.put(FieldType.AMOUNT.getName(), Amount.toDecimal(fields.get(FieldType.AMOUNT.getName()),
					fields.get(FieldType.CURRENCY_CODE.getName())));

			String url = adfFields.getString(Constants.ADF_11);
			String apiKey = adfFields.getString(Constants.ADF_7);

			String request = iciciTransactionConverter.createCompositeUPIStatusEnqRequest(fields, adfFields);
			String response = iciciCommunicator.getIciciCompositeApiResponse(request, url, fields, null, apiKey,
					adfFields);

			boolean isFinalStatus = checkIsStatusFlag(fields);
			iciciResponseHandler.compositeUPIStatusEnqResponseHandler(fields, response);
			String payId = fields.get(FieldType.PAY_ID.getName());

			if (!isFinalStatus) {

				User user = userDao.findPayId(payId);

				if (StringUtils.isNotBlank(user.getSuperMerchantId())) {
					fields.put(FieldType.SUB_MERCHANT_ID.getName(), fields.get(FieldType.PAY_ID.getName()));
					fields.put(FieldType.PAY_ID.getName(), user.getSuperMerchantId());
				}

				try {

					if (fields.contains(FieldType.USER_TYPE.getName())) {
						if (StringUtils.isNotBlank(fields.get(FieldType.USER_TYPE.getName())) && (fields
								.get(FieldType.USER_TYPE.getName()).equalsIgnoreCase("Merchant Initiated Direct")
								|| fields.get(FieldType.USER_TYPE.getName())
										.equalsIgnoreCase("Merchant Initiated Indirect"))) {
							if (fields.contains(FieldType.STATUS.getName())) {
								if (!((fields.get(FieldType.STATUS.getName())
										.equalsIgnoreCase(StatusType.CAPTURED.getName()))
										|| (fields.get(FieldType.STATUS.getName())
												.equalsIgnoreCase(StatusType.TIMEOUT.getName())
												|| (fields.get(FieldType.STATUS.getName())
														.equalsIgnoreCase(StatusType.PROCESSING.getName()))))) {
									vendorPayoutDao.UpdateForClosing(fields);
								}
							}
						}
					}
				} catch (Exception exception) {
					logger.error("Exception in updating closing collection", exception);
				} finally {
					removeSubMerchantId(fields);
				}
			}

			fields.put(FieldType.AMOUNT.getName(), Amount.formatAmount(fields.get(FieldType.AMOUNT.getName()),
					fields.get(FieldType.CURRENCY_CODE.getName())));

			UserSettingData userSetting = userSettingDao.fetchDataUsingPayId(payId);

			// callback to merchant
			if (userSetting.isAllowPayoutStatusEnquiryCallbackFlag()) {
				if (StringUtils.isNotBlank(userSetting.getPayoutCallbackUrl())) {
					payoutCallbackToMerchant(fields, userSetting);
				} else {
					// do nothing, just logging
					logger.info("Payout Status enquiry, Callback url is empty");
				}
			} else {
				// just logging
				logger.info("Payout Status enquiry, Callback flag not active");
			}

		} catch (Exception e) {
			logger.info("Exception in comunication with Compostie UPI for txn Id"
					+ field.get(FieldType.TXN_ID.getName()) + " ", e);
		}

	}

	private boolean checkIsStatusFlag(Fields fields) {

		return vendorPayoutDao.checkTxnFinalStatus(fields);
	}

	public void payoutCallbackToMerchant(Fields fields, UserSettingData userSetting) {

		logger.info("inside payoutCallbackToMerchant(), fields ", fields.getFields());
		try {

			if (StringUtils.isNotBlank(userSetting.getPayoutCallbackUrl())) {
				logger.info("sending callback to merchant on URL " + userSetting.getPayoutCallbackUrl() + " payId "
						+ fields.get(FieldType.PAY_ID.getName()) + " Txn id " + fields.get(FieldType.TXN_ID.getName()));

				String amount = fields.get(FieldType.AMOUNT.getName());

				if (amount.contains(".")) {
					amount = Amount.formatAmount(amount, fields.get(FieldType.CURRENCY_CODE.getName()));
					fields.put(FieldType.AMOUNT.getName(), amount);
				}

				Map<String, String> callbackResponse = new HashMap<String, String>();

				callbackResponse.put(FieldType.RESPONSE_DATE_TIME.getName(),
						(String) DateCreater.formatDateForDb(new Date()));
				callbackResponse.put(FieldType.ORDER_ID.getName(), fields.get(FieldType.ORDER_ID.getName()));
				callbackResponse.put(FieldType.PAY_ID.getName(), fields.get(FieldType.PAY_ID.getName()));
				callbackResponse.put(FieldType.TXN_ID.getName(), fields.get(FieldType.TXN_ID.getName()));

				if (StringUtils.isNotBlank(fields.get(FieldType.PG_RESP_CODE.getName()))) {
					callbackResponse.put(FieldType.PG_RESP_CODE.getName(),
							fields.get(FieldType.PG_RESP_CODE.getName()));
				}
				if (StringUtils.isNotBlank(fields.get(FieldType.RRN.getName()))) {
					callbackResponse.put(FieldType.RRN.getName(), fields.get(FieldType.RRN.getName()));
				}
				if (StringUtils.isNotBlank(fields.get(FieldType.PG_TXN_MESSAGE.getName()))) {
					callbackResponse.put(FieldType.PG_TXN_MESSAGE.getName(),
							fields.get(FieldType.PG_TXN_MESSAGE.getName()));
				}
				if (StringUtils.isNotBlank(fields.get(FieldType.UTR.getName()))) {
					callbackResponse.put(FieldType.UTR.getName(), fields.get(FieldType.UTR.getName()));
				}

				callbackResponse.put(FieldType.CURRENCY_CODE.getName(), fields.get(FieldType.CURRENCY_CODE.getName()));
				callbackResponse.put(FieldType.TXNTYPE.getName(), fields.get(FieldType.TXNTYPE.getName()));
				callbackResponse.put(FieldType.STATUS.getName(), fields.get(FieldType.STATUS.getName()));
				callbackResponse.put(FieldType.RESPONSE_CODE.getName(), fields.get(FieldType.RESPONSE_CODE.getName()));
				callbackResponse.put(FieldType.RESPONSE_MESSAGE.getName(),
						fields.get(FieldType.RESPONSE_MESSAGE.getName()));
				callbackResponse.put(FieldType.AMOUNT.getName(), amount);

				callbackResponse.put(FieldType.HASH.getName(), Hasher.getHash(new Fields(callbackResponse)));

				logger.info("Final Payout Callback Response to Merchant >> " + callbackResponse);

				transactionControllerServiceProvider.callBackforPayoutTransactions(callbackResponse,
						userSetting.getPayoutCallbackUrl());

			}
		} catch (Exception e) {
			logger.info("exception in payoutCallbackToMerchant(), for txnId " + fields.get(FieldType.TXN_ID.getName())
					+ " , ", e);
		}

	}

	public void communicateCompositeImpsTransactionStatusEnq(Fields fields, PayoutAcquireMapping payoutAcquireMapping) {
		logger.info("inside communicateCompositeImpsTransactionStatusEnq()");
		try {

			JSONObject adfFields;

			if (StringUtils.isNotBlank(payoutAcquireMapping.getAdfFields())) {
				adfFields = new JSONObject(payoutAcquireMapping.getAdfFields());
			} else {
				fields.put(FieldType.STATUS.getName(), StatusType.REJECTED_BY_PG.getName());
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.ACQUIRER_ERROR.getResponseMessage());
				fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.ACQUIRER_ERROR.getResponseCode());
				fields.put(FieldType.HASH.getName(), Hasher.getHash(fields));
				logger.info("Hash Invalid For Request ORDER_ID " + fields.get(FieldType.ORDER_ID.getName())
						+ " fields are " + fields.maskFieldsRequest(fields.getFields()));
				return;
			}

			fields.put(FieldType.AMOUNT.getName(), Amount.toDecimal(fields.get(FieldType.AMOUNT.getName()),
					fields.get(FieldType.CURRENCY_CODE.getName())));

			String url = adfFields.getString(Constants.ADF_10);
			String apiKey = adfFields.getString(Constants.ADF_7);

			String request = iciciTransactionConverter.createCompositeIMPSStatusEnqRequest(fields, adfFields);
			String response = iciciCommunicator.getIciciCompositeApiResponse(request, url, fields, null, apiKey,
					adfFields);

			boolean isFinalStatus = checkIsStatusFlag(fields);
			iciciResponseHandler.compositeIMPSStatusEnqResponseHandler(fields, response);
			String payId = fields.get(FieldType.PAY_ID.getName());

			if (!isFinalStatus) {

				User user = userDao.findPayId(payId);

				if (StringUtils.isNotBlank(user.getSuperMerchantId())) {
					fields.put(FieldType.SUB_MERCHANT_ID.getName(), fields.get(FieldType.PAY_ID.getName()));
					fields.put(FieldType.PAY_ID.getName(), user.getSuperMerchantId());
				}

				try {

					if (fields.contains(FieldType.USER_TYPE.getName())) {
						if (StringUtils.isNotBlank(fields.get(FieldType.USER_TYPE.getName())) && (fields
								.get(FieldType.USER_TYPE.getName()).equalsIgnoreCase("Merchant Initiated Direct")
								|| fields.get(FieldType.USER_TYPE.getName())
										.equalsIgnoreCase("Merchant Initiated Indirect"))) {
							if (fields.contains(FieldType.STATUS.getName())) {
								if (!((fields.get(FieldType.STATUS.getName())
										.equalsIgnoreCase(StatusType.CAPTURED.getName()))
										|| (fields.get(FieldType.STATUS.getName())
												.equalsIgnoreCase(StatusType.TIMEOUT.getName())
												|| (fields.get(FieldType.STATUS.getName())
														.equalsIgnoreCase(StatusType.PROCESSING.getName()))))) {
									vendorPayoutDao.UpdateForClosing(fields);
								}
							}
						}
					}
				} catch (Exception exception) {
					logger.error("Exception in updating closing collection", exception);
				} finally {
					removeSubMerchantId(fields);
				}
			}

			fields.put(FieldType.AMOUNT.getName(), Amount.formatAmount(fields.get(FieldType.AMOUNT.getName()),
					fields.get(FieldType.CURRENCY_CODE.getName())));

			UserSettingData userSetting = userSettingDao.fetchDataUsingPayId(payId);

			// callback to merchant
			if (userSetting.isAllowPayoutStatusEnquiryCallbackFlag()) {
				if (StringUtils.isNotBlank(userSetting.getPayoutCallbackUrl())) {
					payoutCallbackToMerchant(fields, userSetting);
				} else {
					// do nothing, just logging
					logger.info("Payout Status enquiry, Callback url is empty");
				}
			} else {
				// just logging
				logger.info("Payout Status enquiry, Callback flag not active");
			}

		} catch (Exception e) {
			logger.info("Exception in comunication with Compostie Imps for txn Id"
					+ field.get(FieldType.TXN_ID.getName()) + " ", e);
		}

	}

	public void communicateCompositeNeftTransactionStatusEnq(Fields fields, PayoutAcquireMapping payoutAcquireMapping) {
		logger.info("inside communicateCompositeNeftTransactionStatusEnq()");
		try {

			JSONObject adfFields;

			if (StringUtils.isNotBlank(payoutAcquireMapping.getAdfFields())) {
				adfFields = new JSONObject(payoutAcquireMapping.getAdfFields());
			} else {
				fields.put(FieldType.STATUS.getName(), StatusType.REJECTED_BY_PG.getName());
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.ACQUIRER_ERROR.getResponseMessage());
				fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.ACQUIRER_ERROR.getResponseCode());
				fields.put(FieldType.HASH.getName(), Hasher.getHash(fields));
				logger.info("Hash Invalid For Request ORDER_ID " + fields.get(FieldType.ORDER_ID.getName())
						+ " fields are " + fields.maskFieldsRequest(fields.getFields()));
				return;
			}

			fields.put(FieldType.AMOUNT.getName(), Amount.toDecimal(fields.get(FieldType.AMOUNT.getName()),
					fields.get(FieldType.CURRENCY_CODE.getName())));

			String url = adfFields.getString(Constants.ADF_36);

			fields.remove(FieldType.TXNTYPE.getName());

			String apiKey = adfFields.getString(Constants.ADF_7);

			String request = iciciTransactionConverter.createStatusCheckNeftRtgs(fields, adfFields);
			String response = iciciCommunicator.getIciciCompositeNeftRtgsApiResponse(request, url, fields, null, apiKey,
					adfFields);

			if (StringUtils.isNotBlank(response) && !response.equalsIgnoreCase("400")
					&& !response.equalsIgnoreCase("402") && !response.equalsIgnoreCase("403")
					&& !response.equalsIgnoreCase("501") && !response.equalsIgnoreCase("502")
					&& !response.equalsIgnoreCase("503") && !response.equalsIgnoreCase("500")) {

				boolean isFinalStatus = checkIsStatusFlag(fields);
				iciciResponseHandler.compositeNeftRtgsStatusEnqResponseHandler(fields, response);
				String payId = fields.get(FieldType.PAY_ID.getName());

				if (!isFinalStatus) {

					User user = userDao.findPayId(payId);

					if (StringUtils.isNotBlank(user.getSuperMerchantId())) {
						fields.put(FieldType.SUB_MERCHANT_ID.getName(), fields.get(FieldType.PAY_ID.getName()));
						fields.put(FieldType.PAY_ID.getName(), user.getSuperMerchantId());
					}

					try {

						if (fields.contains(FieldType.USER_TYPE.getName())) {
							if (StringUtils.isNotBlank(fields.get(FieldType.USER_TYPE.getName())) && (fields
									.get(FieldType.USER_TYPE.getName()).equalsIgnoreCase("Merchant Initiated Direct")
									|| fields.get(FieldType.USER_TYPE.getName())
											.equalsIgnoreCase("Merchant Initiated Indirect"))) {
								if (fields.contains(FieldType.STATUS.getName())) {
									if (!((fields.get(FieldType.STATUS.getName())
											.equalsIgnoreCase(StatusType.CAPTURED.getName()))
											|| (fields.get(FieldType.STATUS.getName())
													.equalsIgnoreCase(StatusType.TIMEOUT.getName())
													|| (fields.get(FieldType.STATUS.getName())
															.equalsIgnoreCase(StatusType.PROCESSING.getName()))))) {
										vendorPayoutDao.UpdateForClosing(fields);
									}
								}
							}
						}
					} catch (Exception exception) {
						logger.error("Exception in updating closing collection", exception);
					} finally {
						removeSubMerchantId(fields);
					}
				}
				fields.put(FieldType.AMOUNT.getName(), Amount.formatAmount(fields.get(FieldType.AMOUNT.getName()),
						fields.get(FieldType.CURRENCY_CODE.getName())));

				UserSettingData userSetting = userSettingDao.fetchDataUsingPayId(payId);

				// callback to merchant
				if (userSetting.isAllowPayoutStatusEnquiryCallbackFlag()) {
					if (StringUtils.isNotBlank(userSetting.getPayoutCallbackUrl())) {
						payoutCallbackToMerchant(fields, userSetting);
					} else {
						// do nothing, just logging
						logger.info("Payout Status enquiry, Callback url is empty");
					}
				} else {
					// just logging
					logger.info("Payout Status enquiry, Callback flag not active");
				}

			} else {
				fields.put(FieldType.STATUS.getName(), StatusType.FAILED.getName());
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), "Bank Server Down, Please Try again later");
			}

			if (!fields.get(FieldType.AMOUNT.getName()).contains(".")) {
				fields.put(FieldType.AMOUNT.getName(), Amount.formatAmount(fields.get(FieldType.AMOUNT.getName()),
						fields.get(FieldType.CURRENCY_CODE.getName())));
			}

		} catch (Exception e) {
			logger.info("Exception in comunication with Compostie Imps for txn Id"
					+ field.get(FieldType.TXN_ID.getName()) + " ", e);
		}

	}

	private void removeSubMerchantId(Fields fields) {
		if (StringUtils.isNotBlank(fields.get(FieldType.SUB_MERCHANT_ID.getName()))) {
			logger.info("Found Sub_Merchant ID " + fields.get(FieldType.SUB_MERCHANT_ID.getName()));

			fields.put(FieldType.PAY_ID.getName(), fields.get(FieldType.SUB_MERCHANT_ID.getName()));
			fields.remove(FieldType.SUB_MERCHANT_ID.getName());
		}
	}

	private void clearFieldsForUPIBeneVerification(Fields fields) {
		logger.info("field clearing for UPI Verification fields " + fields.getFields());
		fields.removeSecureFields();
		fields.remove(FieldType.ACQUIRER_TYPE.getName());
		fields.remove(FieldType.CUST_NAME.getName());
		fields.remove(FieldType.ACQ_ID.getName());
		fields.remove(FieldType.INTERNAL_ORIG_TXN_TYPE.getName());
		fields.remove(FieldType.CURRENCY_CODE.getName());
		logger.info("cleared fields are " + fields.getFields());
	}

	private void handleVpaResponse(Fields fields, JSONObject responseJson) {
		logger.info("inside handleVpaResponse()");
		try {

			String status = null;
			ErrorType errorType = null;
			String pgTxnMsg = null;

			if (responseJson.has("ResCode") && StringUtils.isNotBlank((String) responseJson.get("ResCode"))
					&& responseJson.getString("ResCode").equalsIgnoreCase("000")) {
				status = StatusType.CAPTURED.getName();
				errorType = ErrorType.SUCCESS;
				pgTxnMsg = ErrorType.SUCCESS.getResponseMessage();
			} else {
				status = StatusType.FAILED.getName();
				errorType = ErrorType.FAILED;
				pgTxnMsg = ErrorType.FAILED.getResponseMessage();
			}

			fields.put(FieldType.STATUS.getName(), status);
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), errorType.getResponseMessage());
			fields.put(FieldType.RESPONSE_CODE.getName(), errorType.getResponseCode());

			if (responseJson.has("ResCode") && StringUtils.isNotBlank((String) responseJson.get("ResCode"))) {
				fields.put(FieldType.PG_RESP_CODE.getName(), (String) responseJson.get("ResCode"));
			}
			if (responseJson.has("VerifiedName") && StringUtils.isNotBlank((String) responseJson.get("VerifiedName"))) {
				fields.put(FieldType.PAYER_NAME.getName(), (String) responseJson.get("VerifiedName"));
			}
			if (responseJson.has("AccType") && StringUtils.isNotBlank((String) responseJson.get("AccType"))) {
				fields.put(FieldType.ACCOUNT_TYPE.getName(), (String) responseJson.get("AccType"));
			}
			if (responseJson.has("IFSC") && StringUtils.isNotBlank((String) responseJson.get("IFSC"))) {
				fields.put(FieldType.PAYER_IFSC.getName(), (String) responseJson.get("IFSC"));
			}
			if (responseJson.has("TxnId") && StringUtils.isNotBlank((String) responseJson.get("TxnId"))) {
				fields.put(FieldType.BANK_REF_NUM.getName(), (String) responseJson.get("TxnId"));
			}
			if (responseJson.has("TxnRefId") && StringUtils.isNotBlank((String) responseJson.get("TxnRefId"))) {
				fields.put(FieldType.RRN.getName(), (String) responseJson.get("TxnRefId"));
			}
			if (responseJson.has("VerifiedFlag") && StringUtils.isNotBlank((String) responseJson.get("VerifiedFlag"))) {
				fields.put(FieldType.PG_TXN_STATUS.getName(), (String) responseJson.get("VerifiedFlag"));
			}
			if (responseJson.has("IIN") && StringUtils.isNotBlank((String) responseJson.get("IIN"))) {
				fields.put(FieldType.ACQ_ID.getName(), (String) responseJson.get("IIN"));
			}
			if (responseJson.has("ResDesc") && StringUtils.isNotBlank((String) responseJson.get("ResDesc"))) {
				fields.put(FieldType.PG_TXN_MESSAGE.getName(), (String) responseJson.get("ResDesc"));
			}
			if (responseJson.has("Type") && StringUtils.isNotBlank((String) responseJson.get("Type"))) {
				fields.put(FieldType.USER_TYPE.getName(), (String) responseJson.get("Type"));
			}

			logger.info("Fields Updated " + fields.getFields());

		} catch (Exception e) {
			logger.error("exception ", e);
		}

	}

	public boolean purposeCheck(Fields fields) {
		logger.info("Inside  purposeCheck()");
		try {
			if (StringUtils.isNotBlank(fields.get(FieldType.PURPOSE.getName()))) {
				PayoutPupose payoutInstance = PayoutPupose.getInstanceByCode(fields.get(FieldType.PURPOSE.getName()));
				if (payoutInstance == null) {
					insertImpsUPIBulkDataForPurpose(fields);
					return true;
				}
			}
		} catch (Exception e) {
			logger.error("Exception occured due to invalid purpose , ", e);
		}
		return false;
	}

	public void insertImpsUPIBulkDataForPurpose(Fields fields) {
		logger.info("Inside  insertImpsUPIBulkDataForPurpose()");
		try {
			if (StringUtils.isBlank(fields.get(FieldType.BENE_ACCOUNT_NO.getName()))) {
				fields.put(FieldType.TXNTYPE.getName(), "UPI");
			} else {
				fields.put(FieldType.TXNTYPE.getName(), "IMPS");
			}
			fields.put(FieldType.AMOUNT.getName(), Amount.toDecimal(fields.get(FieldType.AMOUNT.getName()),
					fields.get(FieldType.CURRENCY_CODE.getName())));
			String payId = fields.get(FieldType.PAY_ID.getName());
			fields.put(FieldType.USER_TYPE.getName(), "Merchant Initiated Direct");
			fields.put(FieldType.TXN_ID.getName(), TransactionManager.getId());
			fields.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
			fields.put(FieldType.PG_TXN_MESSAGE.getName(), ErrorType.INVALID_PAYOUT_PURPOSE.getResponseMessage());
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.INVALID_PAYOUT_PURPOSE.getResponseMessage());
			fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_PAYOUT_PURPOSE.getCode());
			fields.put(FieldType.IS_STATUS_FINAL.getName(), "Y");
			User user = userDao.findPayId(payId);

			fields.put(FieldType.VIRTUAL_AC_CODE.getName(), user.getVirtualAccountNo());
			if (StringUtils.isNotBlank(user.getSuperMerchantId())) {
				fields.put(FieldType.SUB_MERCHANT_ID.getName(), fields.get(FieldType.PAY_ID.getName()));
				fields.put(FieldType.PAY_ID.getName(), user.getSuperMerchantId());
			}
			Date dNow = new Date();
			String dateNow = DateCreater.formatDateForDb(dNow);
			fields.put(FieldType.CREATE_DATE.getName(), dateNow);
			fields.put(FieldType.DATE_INDEX.getName(), dateNow.substring(0, 10).replace("-", ""));
			fields.put(FieldType.UPDATE_DATE.getName(), dateNow);

			if (fields.contains("flagBulk")) {
				fields.remove("flagBulk");
				field.updateIciciIMPSBulkTransaction(fields);
			} else {
				field.insertIciciCompositeFields(fields);
			}

			if (StringUtils.isNotBlank(user.getSuperMerchantId())) {
				fields.put(FieldType.PAY_ID.getName(), fields.get(FieldType.SUB_MERCHANT_ID.getName()));
				fields.remove(FieldType.SUB_MERCHANT_ID.getName());
			}
			fields.remove(FieldType.DATE_INDEX.getName());
			fields.remove(FieldType.CREATE_DATE.getName());
			fields.remove(FieldType.UPDATE_DATE.getName());
			fields.remove(FieldType.IS_STATUS_FINAL.getName());
			fields.remove(FieldType.VIRTUAL_AC_CODE.getName());
			fields.remove(FieldType.TXN_ID.getName());
			fields.remove(FieldType.TXNTYPE.getName());
			fields.remove(FieldType.USER_TYPE.getName());
			fields.put(FieldType.AMOUNT.getName(), Amount.formatAmount(fields.get(FieldType.AMOUNT.getName()),
					fields.get(FieldType.CURRENCY_CODE.getName())));
		} catch (Exception e) {
			logger.error("Exception occured due to invalid purpose , ", e);
		}
	}

	public void communicatePayoutTransactionController(Fields fields, PayoutAcquireMapping payoutAcquireMapping,
			User user) throws SystemException {
		logger.info("inside the communicatePayoutTransactionController()");

		switch (PayoutAcquirer.getInstanceFromCode(payoutAcquireMapping.getBankName())) {
		case ICICI:
			logger.info("ICICI Acquirer for Payout, communicatePayoutTransactionController()");

			if (StringUtils.isNotBlank(fields.get(FieldType.TXNTYPE.getName()))) {

				if (fields.get(FieldType.TXNTYPE.getName()).equalsIgnoreCase("UPI"))
					communicateCompositeUPITransaction(fields);
				else if (fields.get(FieldType.TXNTYPE.getName()).equalsIgnoreCase("IMPS"))
					communicateCompositeTransaction(fields);
				else if (fields.get(FieldType.TXNTYPE.getName()).equalsIgnoreCase("NEFT"))
					communicateCompositeNeftTransaction(fields);
				else if (fields.get(FieldType.TXNTYPE.getName()).equalsIgnoreCase("RTGS"))
					communicateCompositeRtgsTransaction(fields);
			} else {
				logger.info("No TXNTYPE Found " + fields.getFields());

				if (StringUtils.isNotBlank(fields.get(FieldType.BENE_ACCOUNT_NO.getName()))) {

					BigDecimal amount = new BigDecimal(fields.get(FieldType.AMOUNT.getName()));
					if (amount.compareTo(new BigDecimal("20000000")) <= 0) {
						fields.put(FieldType.TXNTYPE.getName(), "IMPS");
						communicateCompositeTransaction(fields);
					} else {
						fields.put(FieldType.TXNTYPE.getName(), "RTGS");
						communicateCompositeRtgsTransaction(fields);
					}
				} else {
					fields.put(FieldType.TXNTYPE.getName(), "UPI");
					communicateCompositeUPITransaction(fields);
				}
			}
			break;
		case CASHFREE:
			logger.info("CashFree Acquirer for Payout, communicatePayoutTransactionController()");
			communicateCashfreeTransaction(fields, payoutAcquireMapping);
			break;
		case APEXPAY:
			logger.info("ApexPay Acquirer for Payout, communicatePayoutTransactionController()");
			communicateApexPayTransaction(fields, payoutAcquireMapping, user);
			break;
		case FONEPAISA:
			logger.info("FONEPAISA Acquirer for Payout, communicatePayoutTransactionController()");
			communicateFonePaisaTransaction(fields, payoutAcquireMapping, user);
			break;
		case FLOXYPAY:
			logger.info("FLOXYPAY Acquirer for Payout, communicatePayoutTransactionController()");
			communicateFloxyPayTransaction(fields, payoutAcquireMapping, user);
			break;
		case QAICASH:
			logger.info("QAICASH Acquirer for Payout, communicatePayoutTransactionController()");
			communicateQaicashTransaction(fields, payoutAcquireMapping, user);
			break;
		case TOSHANIDIGITAL:
			logger.info("TOSHANIDIGITAL Acquirer for Payout, communicatePayoutTransactionController()");
			communicateToshaniDigitalTransaction(fields, payoutAcquireMapping, user);
			break;
		case GLOBALPAY:
			logger.info("GLOBALPAY Acquirer for Payout, communicatePayoutTransactionController()");
			communicateGlobalPayTransaction(fields, payoutAcquireMapping, user);
			break;
		default:
			logger.info("No Acquirer for Payout, payId " + fields.get(FieldType.PAY_ID.getName()));
			fields.put(FieldType.STATUS.getName(), StatusType.REJECTED_BY_PG.getName());
			fields.put(FieldType.PG_TXN_MESSAGE.getName(), ErrorType.ACQUIRER_ERROR.getResponseMessage());
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.ACQUIRER_ERROR.getResponseMessage());
			fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.ACQUIRER_ERROR.getCode());
			logger.info("No Acquirer Found for Payout");
			break;
		}

	}

	private void communicateToshaniDigitalTransaction(Fields fields, PayoutAcquireMapping payoutAcquireMapping,
			User user) {
		logger.info("inside communicateToshaniDigitalTransaction()");
		try {
			getToshaniDigitalMappingData(fields);

			if (StringUtils.isBlank(fields.get(FieldType.MERCHANT_ID.getName()))) {
				logger.info("ADF fields not found in mapping for TOSHANIDIGITAL");
				fields.put(FieldType.STATUS.getName(), StatusType.REJECTED_BY_PG.getName());
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), "Mapping not found");
				fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.REJECTED_BY_PG.getResponseCode());
				removeMappingFields(fields);
				throw new SystemException("TOSHANIDIGITAL Mapping Not Found");
			}

			JSONObject adfFields;

			if (StringUtils.isNotBlank(payoutAcquireMapping.getAdfFields())) {
				adfFields = new JSONObject(payoutAcquireMapping.getAdfFields());
			} else {
				fields.put(FieldType.STATUS.getName(), StatusType.REJECTED_BY_PG.getName());
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.ACQUIRER_ERROR.getResponseMessage());
				fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.ACQUIRER_ERROR.getResponseCode());
				fields.put(FieldType.HASH.getName(), Hasher.getHash(fields));
				logger.info("ADF Fields Not Found for Request ORDER_ID " + fields.get(FieldType.ORDER_ID.getName())
						+ " fields are " + fields.maskFieldsRequest(fields.getFields()));
				removeMappingFields(fields);
				return;
			}

			fields.put(FieldType.AMOUNT.getName(), Amount.toDecimal(fields.get(FieldType.AMOUNT.getName()),
					fields.get(FieldType.CURRENCY_CODE.getName())));

			toshaniDigitalService.payoutTransaction(fields, adfFields, user);

			fields.put(FieldType.AMOUNT.getName(), Amount.formatAmount(fields.get(FieldType.AMOUNT.getName()),
					fields.get(FieldType.CURRENCY_CODE.getName())));

		} catch (Exception e) {
			logger.info("Exception in ToshaniDigital", e);
		} finally {
			if (fields.get(FieldType.AMOUNT.getName()).contains(".")) {
				fields.put(FieldType.AMOUNT.getName(), Amount.formatAmount(fields.get(FieldType.AMOUNT.getName()),
						fields.get(FieldType.CURRENCY_CODE.getName())));
			}
		}

	}

	private void communicateFloxyPayTransaction(Fields fields, PayoutAcquireMapping payoutAcquireMapping, User user) {
		logger.info("inside communicateFloxyPayTransaction()");
		try {
			getFloxyPayMappingData(fields);

			if (StringUtils.isBlank(fields.get(FieldType.ADF1.getName()))
					|| StringUtils.isBlank(fields.get(FieldType.ADF2.getName()))) {
				logger.info("ADF fields not found in mapping for FloxyPay");
				fields.put(FieldType.STATUS.getName(), StatusType.REJECTED_BY_PG.getName());
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), "Mapping not found");
				fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.REJECTED_BY_PG.getResponseCode());
				removeMappingFields(fields);
				throw new SystemException("FloxyPay Mapping Not Found");
			}

			JSONObject adfFields;

			if (StringUtils.isNotBlank(payoutAcquireMapping.getAdfFields())) {
				adfFields = new JSONObject(payoutAcquireMapping.getAdfFields());
			} else {
				fields.put(FieldType.STATUS.getName(), StatusType.REJECTED_BY_PG.getName());
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.ACQUIRER_ERROR.getResponseMessage());
				fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.ACQUIRER_ERROR.getResponseCode());
				fields.put(FieldType.HASH.getName(), Hasher.getHash(fields));
				logger.info("ADF Fields Not Found for Request ORDER_ID " + fields.get(FieldType.ORDER_ID.getName())
						+ " fields are " + fields.maskFieldsRequest(fields.getFields()));
				removeMappingFields(fields);
				return;
			}

			fields.put(FieldType.AMOUNT.getName(), Amount.toDecimal(fields.get(FieldType.AMOUNT.getName()),
					fields.get(FieldType.CURRENCY_CODE.getName())));

			floxyPayService.payoutTransaction(fields, adfFields, user);

			fields.put(FieldType.AMOUNT.getName(), Amount.formatAmount(fields.get(FieldType.AMOUNT.getName()),
					fields.get(FieldType.CURRENCY_CODE.getName())));

		} catch (Exception e) {
			logger.info("Exception in Apex Pay ", e);
		} finally {
			if (fields.get(FieldType.AMOUNT.getName()).contains(".")) {
				fields.put(FieldType.AMOUNT.getName(), Amount.formatAmount(fields.get(FieldType.AMOUNT.getName()),
						fields.get(FieldType.CURRENCY_CODE.getName())));
			}
		}
	}

	private void communicateQaicashTransaction(Fields fields, PayoutAcquireMapping payoutAcquireMapping, User user) {
		logger.info("inside communicateQaicashTransaction()");
		try {
			getQaicashMappingData(fields);

			if (StringUtils.isBlank(fields.get(FieldType.ADF1.getName()))
					|| StringUtils.isBlank(fields.get(FieldType.ADF2.getName()))) {
				logger.info("ADF fields not found in mapping for QAICASH");
				fields.put(FieldType.STATUS.getName(), StatusType.REJECTED_BY_PG.getName());
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), "Mapping not found");
				fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.REJECTED_BY_PG.getResponseCode());
				removeMappingFields(fields);
				throw new SystemException("QAICASH Mapping Not Found");
			}

			JSONObject adfFields;

			if (StringUtils.isNotBlank(payoutAcquireMapping.getAdfFields())) {
				adfFields = new JSONObject(payoutAcquireMapping.getAdfFields());
			} else {
				fields.put(FieldType.STATUS.getName(), StatusType.REJECTED_BY_PG.getName());
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.ACQUIRER_ERROR.getResponseMessage());
				fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.ACQUIRER_ERROR.getResponseCode());
				fields.put(FieldType.HASH.getName(), Hasher.getHash(fields));
				logger.info("ADF Fields Not Found for Request ORDER_ID " + fields.get(FieldType.ORDER_ID.getName())
						+ " fields are " + fields.maskFieldsRequest(fields.getFields()));
				removeMappingFields(fields);
				return;
			}

			fields.put(FieldType.AMOUNT.getName(), Amount.toDecimal(fields.get(FieldType.AMOUNT.getName()),
					fields.get(FieldType.CURRENCY_CODE.getName())));

			qaicashService.payoutTransaction(fields, adfFields, user);

			fields.put(FieldType.AMOUNT.getName(), Amount.formatAmount(fields.get(FieldType.AMOUNT.getName()),
					fields.get(FieldType.CURRENCY_CODE.getName())));

		} catch (Exception e) {
			logger.info("Exception in Qaicash", e);
		} finally {
			if (fields.get(FieldType.AMOUNT.getName()).contains(".")) {
				fields.put(FieldType.AMOUNT.getName(), Amount.formatAmount(fields.get(FieldType.AMOUNT.getName()),
						fields.get(FieldType.CURRENCY_CODE.getName())));
			}
		}
	}

	private void getFloxyPayMappingData(Fields fields) {
		try {
			String payId = fields.get(FieldType.PAY_ID.getName());

			logger.info("getTxnKey for Pay Id for " + payId);
			User user = userDao.findPayId(payId);
			Account account = null;
			Set<Account> accounts = null;

			if (StringUtils.isNotBlank(user.getSuperMerchantId()) && !user.isSuperMerchant()) {
				User superMerchant = userDao.findPayId(user.getSuperMerchantId());
				accounts = superMerchant.getAccounts();
			} else {
				accounts = user.getAccounts();
			}

			if (accounts == null || accounts.size() == 0) {
				logger.info("No account found for Pay ID = " + payId);
			} else {
				for (Account accountThis : accounts) {
					if (accountThis.getAcquirerName().equalsIgnoreCase(
							AcquirerType.getInstancefromCode(AcquirerType.FLOXYPAY.getCode()).getName())) {
						account = accountThis;
						break;
					}
				}
			}

			AccountCurrency accountCurrency = account.getAccountCurrency("356");
			fields.put(FieldType.ADF1.getName(), accountCurrency.getAdf1());
			fields.put(FieldType.ADF2.getName(), accountCurrency.getAdf2());

		} catch (Exception e) {
			logger.info("Exception in getPhonePaisaMappingData() ", e);
		}

	}

	public void getQaicashMappingData(Fields fields) {
		try {
			String payId = fields.get(FieldType.PAY_ID.getName());

			logger.info("getTxnKey for Pay Id for " + payId);
			User user = userDao.findPayId(payId);
			Account account = null;
			Set<Account> accounts = null;

			if (StringUtils.isNotBlank(user.getSuperMerchantId()) && !user.isSuperMerchant()) {
				User superMerchant = userDao.findPayId(user.getSuperMerchantId());
				accounts = superMerchant.getAccounts();
			} else {
				accounts = user.getAccounts();
			}

			if (accounts == null || accounts.size() == 0) {
				logger.info("No account found for Pay ID = " + payId);
			} else {
				for (Account accountThis : accounts) {
					if (accountThis.getAcquirerName().equalsIgnoreCase(
							AcquirerType.getInstancefromCode(AcquirerType.QAICASH.getCode()).getName())) {
						account = accountThis;
						break;
					}
				}
			}

			AccountCurrency accountCurrency = account.getAccountCurrency("356");
			fields.put(FieldType.ADF1.getName(), accountCurrency.getAdf1());
			fields.put(FieldType.ADF2.getName(), accountCurrency.getAdf2());

		} catch (Exception e) {
			logger.info("Exception in getQaicashMappingData() ", e);
		}

	}

	private void getToshaniDigitalMappingData(Fields fields) {
		try {
			String payId = fields.get(FieldType.PAY_ID.getName());

			logger.info("getTxnKey for Pay Id for " + payId);
			User user = userDao.findPayId(payId);
			Account account = null;
			Set<Account> accounts = null;

			if (StringUtils.isNotBlank(user.getSuperMerchantId()) && !user.isSuperMerchant()) {
				User superMerchant = userDao.findPayId(user.getSuperMerchantId());
				accounts = superMerchant.getAccounts();
			} else {
				accounts = user.getAccounts();
			}

			if (accounts == null || accounts.size() == 0) {
				logger.info("No account found for Pay ID = " + payId);
			} else {
				for (Account accountThis : accounts) {
					if (accountThis.getAcquirerName().equalsIgnoreCase(
							AcquirerType.getInstancefromCode(AcquirerType.TOSHANIDIGITAL.getCode()).getName())) {
						account = accountThis;
						break;
					}
				}
			}

			AccountCurrency accountCurrency = account.getAccountCurrency("356");
			fields.put(FieldType.MERCHANT_ID.getName(), accountCurrency.getMerchantId());

		} catch (Exception e) {
			logger.info("Exception in getToshaniDigitalMappingData() ", e);
		}

	}

	private void communicateFonePaisaTransaction(Fields fields, PayoutAcquireMapping payoutAcquireMapping, User user) {

		logger.info("inside communicateFonePaisaTransaction()");
		try {

			getFonePaisaMappingData(fields);

			if (StringUtils.isBlank(fields.get(FieldType.MERCHANT_ID.getName()))
					|| StringUtils.isBlank(fields.get(FieldType.TXN_KEY.getName()))) {
				logger.info("ADF fields not found in mapping for Fone Paisa");
				fields.put(FieldType.STATUS.getName(), StatusType.REJECTED_BY_PG.getName());
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), "Mapping not found");
				fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.REJECTED_BY_PG.getResponseCode());
				removeMappingFields(fields);
				throw new SystemException("Fone Paisa Mapping Not Found");
			}

			JSONObject adfFields;

			if (StringUtils.isNotBlank(payoutAcquireMapping.getAdfFields())) {
				adfFields = new JSONObject(payoutAcquireMapping.getAdfFields());
			} else {
				fields.put(FieldType.STATUS.getName(), StatusType.REJECTED_BY_PG.getName());
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.ACQUIRER_ERROR.getResponseMessage());
				fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.ACQUIRER_ERROR.getResponseCode());
				fields.put(FieldType.HASH.getName(), Hasher.getHash(fields));
				logger.info("ADF Fields Not Found for Request ORDER_ID " + fields.get(FieldType.ORDER_ID.getName())
						+ " fields are " + fields.maskFieldsRequest(fields.getFields()));
				removeMappingFields(fields);
				return;
			}

			fields.put(FieldType.AMOUNT.getName(), Amount.toDecimal(fields.get(FieldType.AMOUNT.getName()),
					fields.get(FieldType.CURRENCY_CODE.getName())));

			fonePaisaService.payoutTransaction(fields, adfFields, user);

			fields.put(FieldType.AMOUNT.getName(), Amount.formatAmount(fields.get(FieldType.AMOUNT.getName()),
					fields.get(FieldType.CURRENCY_CODE.getName())));

		} catch (Exception e) {
			logger.info("Exception in Apex Pay ", e);
		} finally {
			if (fields.get(FieldType.AMOUNT.getName()).contains(".")) {
				fields.put(FieldType.AMOUNT.getName(), Amount.formatAmount(fields.get(FieldType.AMOUNT.getName()),
						fields.get(FieldType.CURRENCY_CODE.getName())));
			}
		}

	}

	private void getFonePaisaMappingData(Fields fields) {

		try {
			String payId = fields.get(FieldType.PAY_ID.getName());

			logger.info("getTxnKey for Pay Id for " + payId);
			User user = userDao.findPayId(payId);
			Account account = null;
			Set<Account> accounts = null;

			if (StringUtils.isNotBlank(user.getSuperMerchantId()) && !user.isSuperMerchant()) {
				User superMerchant = userDao.findPayId(user.getSuperMerchantId());
				accounts = superMerchant.getAccounts();
			} else {
				accounts = user.getAccounts();
			}

			if (accounts == null || accounts.size() == 0) {
				logger.info("No account found for Pay ID = " + payId);
			} else {
				for (Account accountThis : accounts) {
					if (accountThis.getAcquirerName().equalsIgnoreCase(
							AcquirerType.getInstancefromCode(AcquirerType.FONEPAISA.getCode()).getName())) {
						account = accountThis;
						break;
					}
				}
			}

			AccountCurrency accountCurrency = account.getAccountCurrency("356");
			fields.put(FieldType.MERCHANT_ID.getName(), accountCurrency.getMerchantId());
			fields.put(FieldType.TXN_KEY.getName(), accountCurrency.getTxnKey());

		} catch (Exception e) {
			logger.info("Exception in getPhonePaisaMappingData() ", e);
		}

	}

	private void communicateApexPayTransaction(Fields fields, PayoutAcquireMapping payoutAcquireMapping, User user) {

		logger.info("inside communicateApexPayTransaction()");
		try {

			getApexPayMappingData(fields);

			if (StringUtils.isBlank(fields.get(FieldType.ADF1.getName()))
					|| StringUtils.isBlank(fields.get(FieldType.ADF2.getName()))) {
				logger.info("ADF fields not found in mapping for Apex Pay");
				fields.put(FieldType.STATUS.getName(), StatusType.REJECTED_BY_PG.getName());
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), "Mapping not found");
				fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.REJECTED_BY_PG.getResponseCode());
				removeMappingFields(fields);
				throw new SystemException("Apex Pay Mapping Not Found");
			}

			JSONObject adfFields;

			if (StringUtils.isNotBlank(payoutAcquireMapping.getAdfFields())) {
				adfFields = new JSONObject(payoutAcquireMapping.getAdfFields());
			} else {
				fields.put(FieldType.STATUS.getName(), StatusType.REJECTED_BY_PG.getName());
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.ACQUIRER_ERROR.getResponseMessage());
				fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.ACQUIRER_ERROR.getResponseCode());
				fields.put(FieldType.HASH.getName(), Hasher.getHash(fields));
				logger.info("ADF Fields Not Found for Request ORDER_ID " + fields.get(FieldType.ORDER_ID.getName())
						+ " fields are " + fields.maskFieldsRequest(fields.getFields()));
				removeMappingFields(fields);
				return;
			}

			fields.put(FieldType.AMOUNT.getName(), Amount.toDecimal(fields.get(FieldType.AMOUNT.getName()),
					fields.get(FieldType.CURRENCY_CODE.getName())));

			apexPayService.payoutTransaction(fields, adfFields, user);

			fields.put(FieldType.AMOUNT.getName(), Amount.formatAmount(fields.get(FieldType.AMOUNT.getName()),
					fields.get(FieldType.CURRENCY_CODE.getName())));

		} catch (Exception e) {
			logger.info("Exception in Apex Pay ", e);
		} finally {
			if (fields.get(FieldType.AMOUNT.getName()).contains(".")) {
				fields.put(FieldType.AMOUNT.getName(), Amount.formatAmount(fields.get(FieldType.AMOUNT.getName()),
						fields.get(FieldType.CURRENCY_CODE.getName())));
			}
		}

	}

	private void getApexPayMappingData(Fields fields) {

		try {
			String payId = fields.get(FieldType.PAY_ID.getName());

			logger.info("getTxnKey for Pay Id for " + payId);
			User user = userDao.findPayId(payId);
			Account account = null;
			Set<Account> accounts = null;

			if (StringUtils.isNotBlank(user.getSuperMerchantId()) && !user.isSuperMerchant()) {
				User superMerchant = userDao.findPayId(user.getSuperMerchantId());
				accounts = superMerchant.getAccounts();
			} else {
				accounts = user.getAccounts();
			}

			if (accounts == null || accounts.size() == 0) {
				logger.info("No account found for Pay ID = " + payId);
			} else {
				for (Account accountThis : accounts) {
					if (accountThis.getAcquirerName().equalsIgnoreCase(
							AcquirerType.getInstancefromCode(AcquirerType.APEXPAY.getCode()).getName())) {
						account = accountThis;
						break;
					}
				}
			}

			AccountCurrency accountCurrency = account.getAccountCurrency("356");
			fields.put(FieldType.ADF1.getName(), accountCurrency.getAdf1());
			fields.put(FieldType.ADF2.getName(), accountCurrency.getAdf2());
			fields.put(FieldType.ADF4.getName(), accountCurrency.getAdf4());

		} catch (Exception e) {
			logger.info("Exception in getApexPayMappingData() ", e);
		}

	}

	private void communicateCashfreeTransaction(Fields fields, PayoutAcquireMapping payoutAcquireMapping) {
		logger.info("inside communicateCashfreeTransaction()");
		try {

			getMappingData(fields);

			if (StringUtils.isBlank(fields.get(FieldType.ADF4.getName()))
					|| StringUtils.isBlank(fields.get(FieldType.ADF5.getName()))) {
				logger.info("ADF fields not found in mapping for cashfree");
				fields.put(FieldType.STATUS.getName(), StatusType.REJECTED_BY_PG.getName());
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), "Mapping not found");
				fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.REJECTED_BY_PG.getResponseCode());
				removeMappingFields(fields);
				throw new SystemException("Cashfree Mapping Not Found");
			}

			JSONObject adfFields;

			if (StringUtils.isNotBlank(payoutAcquireMapping.getAdfFields())) {
				adfFields = new JSONObject(payoutAcquireMapping.getAdfFields());
			} else {
				fields.put(FieldType.STATUS.getName(), StatusType.REJECTED_BY_PG.getName());
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.ACQUIRER_ERROR.getResponseMessage());
				fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.ACQUIRER_ERROR.getResponseCode());
				fields.put(FieldType.HASH.getName(), Hasher.getHash(fields));
				logger.info("ADF Fields Not Found for Request ORDER_ID " + fields.get(FieldType.ORDER_ID.getName())
						+ " fields are " + fields.maskFieldsRequest(fields.getFields()));
				removeMappingFields(fields);
				return;
			}

			// geting token
			String authToken = cashfreePayoutService.getCashfreeAuthToken(fields, adfFields);

			if (StringUtils.isBlank(authToken)) {
				fields.put(FieldType.STATUS.getName(), StatusType.FAILED.getName());
				fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.FAILED.getCode());
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), "Unable to get Auth Token");
				fields.put(FieldType.HASH.getName(), Hasher.getHash(fields));
				removeMappingFields(fields);
				return;
			}

			if (fields.get(FieldType.TXNTYPE.getName()).equalsIgnoreCase("UPI")) {
				String vpaStatus = cashfreePayoutService.validateVPACashfree(fields, adfFields, authToken);

				if (StringUtils.isNotBlank(vpaStatus) && vpaStatus.equalsIgnoreCase("YES")) {
					// Do Nothing
				} else if (StringUtils.isNotBlank(vpaStatus) && vpaStatus.equalsIgnoreCase("Error")) {
					fields.put(FieldType.STATUS.getName(), StatusType.REJECTED_BY_PG.getName());
					fields.put(FieldType.RESPONSE_CODE.getName(),
							ErrorType.DECLINED_BY_INSUFFICIENT_BALANCE.getResponseCode());
					removeMappingFields(fields);
					return;
				} else {
					fields.put(FieldType.STATUS.getName(), StatusType.REJECTED_BY_PG.getName());
					fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_VPA.getResponseCode());
					fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.INVALID_VPA.getResponseMessage());
					fields.put(FieldType.HASH.getName(), Hasher.getHash(fields));
					removeMappingFields(fields);
					return;
				}
			}

			logger.info("Requested Order Id : " + fields.get(FieldType.ORDER_ID.getName()) + ", Requested PayId : "
					+ fields.get(FieldType.PAY_ID.getName()));

			String txnId = fields.get(FieldType.TXN_ID.getName());

			if (StringUtils.isBlank(txnId)) {
				txnId = TransactionManager.getNewTransactionId();
				fields.put(FieldType.TXN_ID.getName(), txnId);
			}

			// Validate Auth Token

			if (StringUtils.isBlank(authToken)) {

				logger.info("Unable to get Auth Token from Cashfree");
				fields.put(FieldType.STATUS.getName(), StatusType.FAILED.getName());
				fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.FAILED.getCode());
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), "Unable to get Auth Token");
				removeMappingFields(fields);
				return;
			}

			String authTokenStatusRes = cashfreePayoutService.validateAuthToken(authToken, adfFields);

			JSONObject authTokenStatusJson = new JSONObject(authTokenStatusRes);

			String authTokenStatus = authTokenStatusJson.get("status").toString();
			if (StringUtils.isBlank(authTokenStatus) && authTokenStatus.equalsIgnoreCase("SUCCESS")) {
				authToken = cashfreePayoutService.getCashfreeAuthToken(fields, adfFields);
			}

			// Fetch / Add Beneficiary
			String beneId = cashfreePayoutService.fetchBene(fields, adfFields, authToken);

			if (StringUtils.isBlank(beneId)) {
				logger.info("Unable to fetch bene Id for cashfree payout");
				removeMappingFields(fields);
				return;
			}

			fields.put(FieldType.AMOUNT.getName(), Amount.toDecimal(fields.get(FieldType.AMOUNT.getName()),
					fields.get(FieldType.CURRENCY_CODE.getName())));

			if ((StringUtils.isNotBlank(fields.get(FieldType.USER_TYPE.getName()))) && ((fields
					.get(FieldType.USER_TYPE.getName()).equalsIgnoreCase("Merchant Initiated Direct"))
					|| (fields.get(FieldType.USER_TYPE.getName()).equalsIgnoreCase("Merchant Initiated Indirect")))) {
				logger.info("Insertion closing Amount From Paytm");
				vendorPayoutDao.insertUpdateForClosing(fields);
			}

			String payoutRes = cashfreePayoutService.sendPayout(fields, beneId, adfFields, authToken);
			logger.info("Cashfree Payout Response >> " + payoutRes);

			// if balance is not enough
			if (StringUtils.isNotBlank(fields.get(FieldType.IS_STATUS_FINAL.getName()))) {
				String payId = fields.get(FieldType.PAY_ID.getName());
				User user = userDao.findPayId(payId);

				if (StringUtils.isNotBlank(user.getSuperMerchantId())) {
					fields.put(FieldType.SUB_MERCHANT_ID.getName(), fields.get(FieldType.PAY_ID.getName()));
					fields.put(FieldType.PAY_ID.getName(), user.getSuperMerchantId());
				}

				try {
					if (fields.contains(FieldType.USER_TYPE.getName())) {
						if (StringUtils.isNotBlank(fields.get(FieldType.USER_TYPE.getName())) && (fields
								.get(FieldType.USER_TYPE.getName()).equalsIgnoreCase("Merchant Initiated Direct")
								|| fields.get(FieldType.USER_TYPE.getName())
										.equalsIgnoreCase("Merchant Initiated Indirect"))) {
							if (fields.contains(FieldType.STATUS.getName())) {
								if (!((fields.get(FieldType.STATUS.getName())
										.equalsIgnoreCase(StatusType.CAPTURED.getName()))
										|| (fields.get(FieldType.STATUS.getName())
												.equalsIgnoreCase(StatusType.TIMEOUT.getName())
												|| (fields.get(FieldType.STATUS.getName())
														.equalsIgnoreCase(StatusType.PROCESSING.getName()))))) {
									vendorPayoutDao.UpdateForClosing(fields);
								}
							}
						}
					}
				} catch (Exception exception) {
					logger.error("Exception in updating closing collection", exception);
				} finally {
					removeSubMerchantId(fields);
				}
			}

			removeMappingFields(fields);

			cashfreePayoutService.insertPayoutTxn(fields);

			fields.put(FieldType.AMOUNT.getName(), Amount.formatAmount(fields.get(FieldType.AMOUNT.getName()),
					fields.get(FieldType.CURRENCY_CODE.getName())));

		} catch (Exception e) {
			logger.info("Exception in communicatePaytmTransaction() for txn Id" + field.get(FieldType.TXN_ID.getName())
					+ " ", e);
		}

	}

	private void removeMappingFields(Fields fields) {
		fields.remove(FieldType.MERCHANT_ID.getName());
		fields.remove(FieldType.TXN_KEY.getName());
		fields.remove(FieldType.PASSWORD.getName());
		fields.remove(FieldType.ADF1.getName());
		fields.remove(FieldType.ADF2.getName());
		fields.remove(FieldType.ADF3.getName());
		fields.remove(FieldType.ADF4.getName());
		fields.remove(FieldType.ADF5.getName());
		fields.remove(FieldType.ADF6.getName());
		fields.remove(FieldType.ADF7.getName());
		fields.remove(FieldType.ADF8.getName());
	}

	private void getMappingData(Fields fields) {
		logger.info("inside getMappingData()");
		try {
			String payId = fields.get(FieldType.PAY_ID.getName());

			logger.info("getTxnKey for Pay Id for " + payId);
			User user = userDao.findPayId(payId);
			Account account = null;
			Set<Account> accounts = null;

			if (StringUtils.isNotBlank(user.getSuperMerchantId()) && !user.isSuperMerchant()) {
				User superMerchant = userDao.findPayId(user.getSuperMerchantId());
				accounts = superMerchant.getAccounts();
			} else {
				accounts = user.getAccounts();
			}

			if (accounts == null || accounts.size() == 0) {
				logger.info("No account found for Pay ID = " + payId);
			} else {
				for (Account accountThis : accounts) {
					if (accountThis.getAcquirerName().equalsIgnoreCase(
							AcquirerType.getInstancefromCode(AcquirerType.CASHFREE.getCode()).getName())) {
						account = accountThis;
						break;
					}
				}
			}

			AccountCurrency accountCurrency = account.getAccountCurrency("356");
			fields.put(FieldType.ADF4.getName(), accountCurrency.getAdf4());
			fields.put(FieldType.ADF5.getName(), accountCurrency.getAdf5());

		} catch (Exception e) {
			logger.info("excption finding cashfree mapping for payout ", e);
		}
	}

	public void communicatePayoutTransactionStatusEnquiryController(Fields fields,
			PayoutAcquireMapping payoutAcquireMapping) throws SystemException {
		logger.info("inside the communicatePayoutTransactionStatusEnquiryController()");

		switch (PayoutAcquirer.getInstanceFromCode(payoutAcquireMapping.getBankName())) {
		case ICICI:
			logger.info("ICICI Acquirer for Payout Status Enq, communicatePayoutTransactionStatusEnquiryController()");

			if (StringUtils.isNotBlank(fields.get(FieldType.TXNTYPE.getName()))) {

				if (fields.get(FieldType.TXNTYPE.getName()).equalsIgnoreCase("UPI"))
					communicateCompositeUPITransactionStatusEnq(fields, payoutAcquireMapping);
				else if (fields.get(FieldType.TXNTYPE.getName()).equalsIgnoreCase("IMPS"))
					communicateCompositeImpsTransactionStatusEnq(fields, payoutAcquireMapping);
				else if (fields.get(FieldType.TXNTYPE.getName()).equalsIgnoreCase("NEFT"))
					communicateCompositeNeftTransactionStatusEnq(fields, payoutAcquireMapping);
				else if (fields.get(FieldType.TXNTYPE.getName()).equalsIgnoreCase("RTGS"))
					communicateCompositeNeftTransactionStatusEnq(fields, payoutAcquireMapping);
			}

			break;
		case CASHFREE:
			logger.info(
					"CASHFREE Acquirer for Payout Status Enq, communicatePayoutTransactionStatusEnquiryController()");
			fields.put(FieldType.STATUS.getName(), StatusType.REJECTED.getName());
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), "Please Wait For Callback from cashfree");
			fields.put(FieldType.PG_TXN_MESSAGE.getName(), "Please Wait For Callback from cashfree");
			// communicateCashfreeTransactionStatusEnq(fields,
			// payoutAcquireMapping);
			break;

		case APEXPAY:
			logger.info(
					"Apex Pay Acquirer for Payout Status Enq, communicatePayoutTransactionStatusEnquiryController()");
			communicateApexPayTransactionStatusEnq(fields, payoutAcquireMapping);
			break;

		case FONEPAISA:
			logger.info(
					"FONEPAISA Acquirer for Payout Status Enq, communicatePayoutTransactionStatusEnquiryController()");
			communicateFonePaisaTransactionStatusEnq(fields, payoutAcquireMapping);
			break;

		case FLOXYPAY:
			logger.info(
					"FLOXYPAY Acquirer for Payout Status Enq, communicatePayoutTransactionStatusEnquiryController()");
			communicateFloxyPayTransactionStatusEnq(fields, payoutAcquireMapping);
			break;
		case QAICASH:
			logger.info(
					"QAICASH Acquirer for Payout Status Enq, communicatePayoutTransactionStatusEnquiryController()");
			communicateQaicashTransactionStatusEnq(fields, payoutAcquireMapping);
			break;

		case TOSHANIDIGITAL:
			logger.info(
					"TOSHANIDIGITAL Acquirer for Payout Status Enq, communicatePayoutTransactionStatusEnquiryController()");
			communicateToshaniDigitalTransactionStatusEnq(fields, payoutAcquireMapping);
			break;
		case GLOBALPAY:
			logger.info("GLOBALPAY Acquirer for Payout, communicatePayoutTransactionController()");
			communicateGlobalPayTransactionStatusEnq(fields, payoutAcquireMapping);
			break;
		default:
			logger.info("No Acquirer for Payout communicatePayoutTransactionStatusEnquiryController(), payId "
					+ fields.get(FieldType.PAY_ID.getName()));
			fields.put(FieldType.STATUS.getName(), StatusType.REJECTED_BY_PG.getName());
			fields.put(FieldType.PG_TXN_MESSAGE.getName(), ErrorType.ACQUIRER_ERROR.getResponseMessage());
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.ACQUIRER_ERROR.getResponseMessage());
			fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.ACQUIRER_ERROR.getCode());
			logger.info("No Acquirer Found for Payout, communicatePayoutTransactionStatusEnquiryController()");
			break;
		}

		removeMappingFields(fields);

	}

	private void communicateToshaniDigitalTransactionStatusEnq(Fields fields,
			PayoutAcquireMapping payoutAcquireMapping) {
		logger.info("inside communicateToshaniDigitalTransactionStatusEnq()");
		try {

			JSONObject adfFields;

			if (StringUtils.isNotBlank(payoutAcquireMapping.getAdfFields())) {
				adfFields = new JSONObject(payoutAcquireMapping.getAdfFields());
			} else {
				fields.put(FieldType.STATUS.getName(), StatusType.REJECTED_BY_PG.getName());
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.ACQUIRER_ERROR.getResponseMessage());
				fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.ACQUIRER_ERROR.getResponseCode());
				fields.put(FieldType.HASH.getName(), Hasher.getHash(fields));
				logger.info("Adf fields Invalid For Request ORDER_ID " + fields.get(FieldType.ORDER_ID.getName())
						+ " fields are " + fields.maskFieldsRequest(fields.getFields()));
				return;
			}

			getToshaniDigitalMappingData(fields);

			fields.put(FieldType.AMOUNT.getName(), Amount.toDecimal(fields.get(FieldType.AMOUNT.getName()),
					fields.get(FieldType.CURRENCY_CODE.getName())));

			boolean isFinalStatus = toshaniDigitalService.payoutStatus(fields, adfFields);

			String payId = fields.get(FieldType.PAY_ID.getName());

			if (!isFinalStatus) {

				User user = userDao.findPayId(payId);

				if (StringUtils.isNotBlank(user.getSuperMerchantId())) {
					fields.put(FieldType.SUB_MERCHANT_ID.getName(), fields.get(FieldType.PAY_ID.getName()));
					fields.put(FieldType.PAY_ID.getName(), user.getSuperMerchantId());
				}

				try {

					if (fields.contains(FieldType.USER_TYPE.getName())) {
						if (StringUtils.isNotBlank(fields.get(FieldType.USER_TYPE.getName())) && (fields
								.get(FieldType.USER_TYPE.getName()).equalsIgnoreCase("Merchant Initiated Direct")
								|| fields.get(FieldType.USER_TYPE.getName())
										.equalsIgnoreCase("Merchant Initiated Indirect"))) {
							if (fields.contains(FieldType.STATUS.getName())) {
								if (!((fields.get(FieldType.STATUS.getName())
										.equalsIgnoreCase(StatusType.CAPTURED.getName()))
										|| (fields.get(FieldType.STATUS.getName())
												.equalsIgnoreCase(StatusType.TIMEOUT.getName())
												|| (fields.get(FieldType.STATUS.getName())
														.equalsIgnoreCase(StatusType.PROCESSING.getName()))))) {
									vendorPayoutDao.UpdateForClosing(fields);
								}
							}
						}
					}
				} catch (Exception exception) {
					logger.error("Exception in updating closing collection", exception);
				} finally {
					removeSubMerchantId(fields);
				}
			}

			fields.put(FieldType.AMOUNT.getName(), Amount.formatAmount(fields.get(FieldType.AMOUNT.getName()),
					fields.get(FieldType.CURRENCY_CODE.getName())));

			UserSettingData userSetting = userSettingDao.fetchDataUsingPayId(payId);

			// callback to merchant
			if (userSetting.isAllowPayoutStatusEnquiryCallbackFlag()) {
				if (StringUtils.isNotBlank(userSetting.getPayoutCallbackUrl())) {
					payoutCallbackToMerchant(fields, userSetting);
				} else {
					// do nothing, just logging
					logger.info("Payout Status enquiry, Callback url is empty");
				}
			} else {
				// just logging
				logger.info("Payout Status enquiry, Callback flag not active");
			}

		} catch (Exception e) {
			logger.info("Exception in communicateToshaniDigitalTransactionStatusEnq() ", e);
		}

	}

	private void communicateFloxyPayTransactionStatusEnq(Fields fields, PayoutAcquireMapping payoutAcquireMapping) {
		logger.info("inside communicateFloxyPayTransactionStatusEnq()");
		try {

			JSONObject adfFields;

			if (StringUtils.isNotBlank(payoutAcquireMapping.getAdfFields())) {
				adfFields = new JSONObject(payoutAcquireMapping.getAdfFields());
			} else {
				fields.put(FieldType.STATUS.getName(), StatusType.REJECTED_BY_PG.getName());
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.ACQUIRER_ERROR.getResponseMessage());
				fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.ACQUIRER_ERROR.getResponseCode());
				fields.put(FieldType.HASH.getName(), Hasher.getHash(fields));
				logger.info("Adf fields Invalid For Request ORDER_ID " + fields.get(FieldType.ORDER_ID.getName())
						+ " fields are " + fields.maskFieldsRequest(fields.getFields()));
				return;
			}

			getFloxyPayMappingData(fields);

			fields.put(FieldType.AMOUNT.getName(), Amount.toDecimal(fields.get(FieldType.AMOUNT.getName()),
					fields.get(FieldType.CURRENCY_CODE.getName())));

			boolean isFinalStatus = floxyPayService.payoutStatus(fields, adfFields);

			String payId = fields.get(FieldType.PAY_ID.getName());

			if (!isFinalStatus) {

				User user = userDao.findPayId(payId);

				if (StringUtils.isNotBlank(user.getSuperMerchantId())) {
					fields.put(FieldType.SUB_MERCHANT_ID.getName(), fields.get(FieldType.PAY_ID.getName()));
					fields.put(FieldType.PAY_ID.getName(), user.getSuperMerchantId());
				}

				try {

					if (fields.contains(FieldType.USER_TYPE.getName())) {
						if (StringUtils.isNotBlank(fields.get(FieldType.USER_TYPE.getName())) && (fields
								.get(FieldType.USER_TYPE.getName()).equalsIgnoreCase("Merchant Initiated Direct")
								|| fields.get(FieldType.USER_TYPE.getName())
										.equalsIgnoreCase("Merchant Initiated Indirect"))) {
							if (fields.contains(FieldType.STATUS.getName())) {
								if (!((fields.get(FieldType.STATUS.getName())
										.equalsIgnoreCase(StatusType.CAPTURED.getName()))
										|| (fields.get(FieldType.STATUS.getName())
												.equalsIgnoreCase(StatusType.TIMEOUT.getName())
												|| (fields.get(FieldType.STATUS.getName())
														.equalsIgnoreCase(StatusType.PROCESSING.getName()))))) {
									vendorPayoutDao.UpdateForClosing(fields);
								}
							}
						}
					}
				} catch (Exception exception) {
					logger.error("Exception in updating closing collection", exception);
				} finally {
					removeSubMerchantId(fields);
				}
			}

			fields.put(FieldType.AMOUNT.getName(), Amount.formatAmount(fields.get(FieldType.AMOUNT.getName()),
					fields.get(FieldType.CURRENCY_CODE.getName())));

			UserSettingData userSetting = userSettingDao.fetchDataUsingPayId(payId);

			// callback to merchant
			if (userSetting.isAllowPayoutStatusEnquiryCallbackFlag()) {
				if (StringUtils.isNotBlank(userSetting.getPayoutCallbackUrl())) {
					payoutCallbackToMerchant(fields, userSetting);
				} else {
					// do nothing, just logging
					logger.info("Payout Status enquiry, Callback url is empty");
				}
			} else {
				// just logging
				logger.info("Payout Status enquiry, Callback flag not active");
			}

		} catch (Exception e) {
			logger.info("Exception in communicatePhonePaisaTransactionStatusEnq() ", e);
		}

	}

	private void communicateFonePaisaTransactionStatusEnq(Fields fields, PayoutAcquireMapping payoutAcquireMapping) {
		logger.info("inside communicateFonePaisaTransactionStatusEnq()");
		try {

			JSONObject adfFields;

			if (StringUtils.isNotBlank(payoutAcquireMapping.getAdfFields())) {
				adfFields = new JSONObject(payoutAcquireMapping.getAdfFields());
			} else {
				fields.put(FieldType.STATUS.getName(), StatusType.REJECTED_BY_PG.getName());
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.ACQUIRER_ERROR.getResponseMessage());
				fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.ACQUIRER_ERROR.getResponseCode());
				fields.put(FieldType.HASH.getName(), Hasher.getHash(fields));
				logger.info("Adf fields Invalid For Request ORDER_ID " + fields.get(FieldType.ORDER_ID.getName())
						+ " fields are " + fields.maskFieldsRequest(fields.getFields()));
				return;
			}

			getFonePaisaMappingData(fields);

			fields.put(FieldType.AMOUNT.getName(), Amount.toDecimal(fields.get(FieldType.AMOUNT.getName()),
					fields.get(FieldType.CURRENCY_CODE.getName())));

			boolean isFinalStatus = fonePaisaService.payoutStatus(fields, adfFields);

			String payId = fields.get(FieldType.PAY_ID.getName());

			if (!isFinalStatus) {

				User user = userDao.findPayId(payId);

				if (StringUtils.isNotBlank(user.getSuperMerchantId())) {
					fields.put(FieldType.SUB_MERCHANT_ID.getName(), fields.get(FieldType.PAY_ID.getName()));
					fields.put(FieldType.PAY_ID.getName(), user.getSuperMerchantId());
				}

				try {

					if (fields.contains(FieldType.USER_TYPE.getName())) {
						if (StringUtils.isNotBlank(fields.get(FieldType.USER_TYPE.getName())) && (fields
								.get(FieldType.USER_TYPE.getName()).equalsIgnoreCase("Merchant Initiated Direct")
								|| fields.get(FieldType.USER_TYPE.getName())
										.equalsIgnoreCase("Merchant Initiated Indirect"))) {
							if (fields.contains(FieldType.STATUS.getName())) {
								if (!((fields.get(FieldType.STATUS.getName())
										.equalsIgnoreCase(StatusType.CAPTURED.getName()))
										|| (fields.get(FieldType.STATUS.getName())
												.equalsIgnoreCase(StatusType.TIMEOUT.getName())
												|| (fields.get(FieldType.STATUS.getName())
														.equalsIgnoreCase(StatusType.PROCESSING.getName()))))) {
									vendorPayoutDao.UpdateForClosing(fields);
								}
							}
						}
					}
				} catch (Exception exception) {
					logger.error("Exception in updating closing collection", exception);
				} finally {
					removeSubMerchantId(fields);
				}
			}

			fields.put(FieldType.AMOUNT.getName(), Amount.formatAmount(fields.get(FieldType.AMOUNT.getName()),
					fields.get(FieldType.CURRENCY_CODE.getName())));

			UserSettingData userSetting = userSettingDao.fetchDataUsingPayId(payId);

			// callback to merchant
			if (userSetting.isAllowPayoutStatusEnquiryCallbackFlag()) {
				if (StringUtils.isNotBlank(userSetting.getPayoutCallbackUrl())) {
					payoutCallbackToMerchant(fields, userSetting);
				} else {
					// do nothing, just logging
					logger.info("Payout Status enquiry, Callback url is empty");
				}
			} else {
				// just logging
				logger.info("Payout Status enquiry, Callback flag not active");
			}

		} catch (Exception e) {
			logger.info("Exception in communicatePhonePaisaTransactionStatusEnq() ", e);
		}

	}

	private void communicateApexPayTransactionStatusEnq(Fields fields, PayoutAcquireMapping payoutAcquireMapping) {
		logger.info("inside communicateApexPayTransactionStatusEnq()");
		try {

			JSONObject adfFields;

			if (StringUtils.isNotBlank(payoutAcquireMapping.getAdfFields())) {
				adfFields = new JSONObject(payoutAcquireMapping.getAdfFields());
			} else {
				fields.put(FieldType.STATUS.getName(), StatusType.REJECTED_BY_PG.getName());
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.ACQUIRER_ERROR.getResponseMessage());
				fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.ACQUIRER_ERROR.getResponseCode());
				fields.put(FieldType.HASH.getName(), Hasher.getHash(fields));
				logger.info("Adf fields Invalid For Request ORDER_ID " + fields.get(FieldType.ORDER_ID.getName())
						+ " fields are " + fields.maskFieldsRequest(fields.getFields()));
				return;
			}

			getApexPayMappingData(fields);

			fields.put(FieldType.AMOUNT.getName(), Amount.toDecimal(fields.get(FieldType.AMOUNT.getName()),
					fields.get(FieldType.CURRENCY_CODE.getName())));

			boolean isFinalStatus = apexPayService.payoutStatus(fields, adfFields);

			String payId = fields.get(FieldType.PAY_ID.getName());

			if (!isFinalStatus) {

				User user = userDao.findPayId(payId);

				if (StringUtils.isNotBlank(user.getSuperMerchantId())) {
					fields.put(FieldType.SUB_MERCHANT_ID.getName(), fields.get(FieldType.PAY_ID.getName()));
					fields.put(FieldType.PAY_ID.getName(), user.getSuperMerchantId());
				}

				try {

					if (fields.contains(FieldType.USER_TYPE.getName())) {
						if (StringUtils.isNotBlank(fields.get(FieldType.USER_TYPE.getName())) && (fields
								.get(FieldType.USER_TYPE.getName()).equalsIgnoreCase("Merchant Initiated Direct")
								|| fields.get(FieldType.USER_TYPE.getName())
										.equalsIgnoreCase("Merchant Initiated Indirect"))) {
							if (fields.contains(FieldType.STATUS.getName())) {
								if (!((fields.get(FieldType.STATUS.getName())
										.equalsIgnoreCase(StatusType.CAPTURED.getName()))
										|| (fields.get(FieldType.STATUS.getName())
												.equalsIgnoreCase(StatusType.TIMEOUT.getName())
												|| (fields.get(FieldType.STATUS.getName())
														.equalsIgnoreCase(StatusType.PROCESSING.getName()))))) {
									vendorPayoutDao.UpdateForClosing(fields);
								}
							}
						}
					}
				} catch (Exception exception) {
					logger.error("Exception in updating closing collection", exception);
				} finally {
					removeSubMerchantId(fields);
				}
			}

			fields.put(FieldType.AMOUNT.getName(), Amount.formatAmount(fields.get(FieldType.AMOUNT.getName()),
					fields.get(FieldType.CURRENCY_CODE.getName())));

			UserSettingData userSetting = userSettingDao.fetchDataUsingPayId(payId);

			// callback to merchant
			if (userSetting.isAllowPayoutStatusEnquiryCallbackFlag()) {
				if (StringUtils.isNotBlank(userSetting.getPayoutCallbackUrl())) {
					payoutCallbackToMerchant(fields, userSetting);
				} else {
					// do nothing, just logging
					logger.info("Payout Status enquiry, Callback url is empty");
				}
			} else {
				// just logging
				logger.info("Payout Status enquiry, Callback flag not active");
			}

		} catch (Exception e) {
			logger.info("Exception in communicateApexPayTransactionStatusEnq() ", e);
		}

	}

	private void communicateCashfreeTransactionStatusEnq(Fields fields, PayoutAcquireMapping payoutAcquireMapping) {
		logger.info("inside communicateCashfreeTransactionStatusEnq()");
		try {

			JSONObject adfFields;

			if (StringUtils.isNotBlank(payoutAcquireMapping.getAdfFields())) {
				adfFields = new JSONObject(payoutAcquireMapping.getAdfFields());
			} else {
				fields.put(FieldType.STATUS.getName(), StatusType.REJECTED_BY_PG.getName());
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.ACQUIRER_ERROR.getResponseMessage());
				fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.ACQUIRER_ERROR.getResponseCode());
				fields.put(FieldType.HASH.getName(), Hasher.getHash(fields));
				logger.info("Adf fields Invalid For Request ORDER_ID " + fields.get(FieldType.ORDER_ID.getName())
						+ " fields are " + fields.maskFieldsRequest(fields.getFields()));
				return;
			}

			getMappingData(fields);

			// geting token
			String token = cashfreePayoutService.getCashfreeAuthToken(fields, adfFields);

			removeMappingFields(fields);

			if (StringUtils.isBlank(token)) {
				// fields.put(FieldType.STATUS.getName(),
				// StatusType.FAILED.getName());
				fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.FAILED.getCode());
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), "Unable to get Auth Token");
				fields.put(FieldType.HASH.getName(), Hasher.getHash(fields));
				return;
			}

			fields.put(FieldType.AMOUNT.getName(), Amount.toDecimal(fields.get(FieldType.AMOUNT.getName()),
					fields.get(FieldType.CURRENCY_CODE.getName())));

			cashfreePayoutService.payoutStatus(fields, adfFields, token);

			String payId = fields.get(FieldType.PAY_ID.getName());
			User user = userDao.findPayId(payId);

			if (StringUtils.isNotBlank(user.getSuperMerchantId())) {
				fields.put(FieldType.SUB_MERCHANT_ID.getName(), fields.get(FieldType.PAY_ID.getName()));
				fields.put(FieldType.PAY_ID.getName(), user.getSuperMerchantId());
			}

			try {
				if (fields.contains(FieldType.USER_TYPE.getName())) {
					if (StringUtils.isNotBlank(fields.get(FieldType.USER_TYPE.getName()))
							&& (fields.get(FieldType.USER_TYPE.getName()).equalsIgnoreCase("Merchant Initiated Direct")
									|| fields.get(FieldType.USER_TYPE.getName())
											.equalsIgnoreCase("Merchant Initiated Indirect"))) {
						if (fields.contains(FieldType.STATUS.getName())) {
							if (!((fields.get(FieldType.STATUS.getName())
									.equalsIgnoreCase(StatusType.CAPTURED.getName()))
									|| (fields.get(FieldType.STATUS.getName())
											.equalsIgnoreCase(StatusType.TIMEOUT.getName())
											|| (fields.get(FieldType.STATUS.getName())
													.equalsIgnoreCase(StatusType.PROCESSING.getName()))))) {
								vendorPayoutDao.UpdateForClosing(fields);
							}
						}
					}
				}
			} catch (Exception exception) {
				logger.error("Exception in updating closing collection", exception);
			} finally {
				removeSubMerchantId(fields);
			}

			fields.put(FieldType.AMOUNT.getName(), Amount.formatAmount(fields.get(FieldType.AMOUNT.getName()),
					fields.get(FieldType.CURRENCY_CODE.getName())));

			UserSettingData userSetting = userSettingDao.fetchDataUsingPayId(payId);

			// callback to merchant
			if (userSetting.isAllowPayoutStatusEnquiryCallbackFlag()) {
				if (StringUtils.isNotBlank(userSetting.getPayoutCallbackUrl())) {
					payoutCallbackToMerchant(fields, userSetting);
				} else {
					// do nothing, just logging
					logger.info("Payout Status enquiry, Callback url is empty");
				}
			} else {
				// just logging
				logger.info("Payout Status enquiry, Callback flag not active");
			}

		} catch (Exception e) {
			logger.info("Exception in comunication with communicatePaytmTransactionStatusEnq() for txn Id"
					+ field.get(FieldType.TXN_ID.getName()) + " ", e);
		}

	}

	public void updateOldVirtualDetails() {
		logger.info("inside updateOldVirtualDetails()");

		MongoDatabase dbIns = mongoInstance.getDB();
		MongoCollection<Document> collection = dbIns.getCollection(PropertiesManager.propertiesMap.get(
				"MONGO_DB_" + com.paymentgateway.commons.util.Constants.PAYOUT_VIRTUAL_ACCOUNT_DETAILS.getValue()));

		MongoCollection<Document> collection2 = dbIns.getCollection(PropertiesManager.propertiesMap
				.get("MONGO_DB_" + com.paymentgateway.commons.util.Constants.PAYOUT_MERCHANT_MAPPING.getValue()));

		List<User> allMerchantList = userDao.getAllMerchantList();

		List<Document> allVirtualData = new ArrayList<Document>();
		List<Document> allMerchantData = new ArrayList<Document>();

		for (User user : allMerchantList) {

			try {

				if (StringUtils.isNotBlank(user.getVirtualAccountNo())) {
					Document doc = new Document();
					Document doc2 = new Document();

					Date dNow = new Date();
					String dateNow = DateCreater.formatDateForDb(dNow);
					if (!user.isSuperMerchant() && StringUtils.isNotBlank(user.getSuperMerchantId())) {
						// subMerchant
						doc.put(FieldType.PAY_ID.getName(), user.getSuperMerchantId());
						doc.put(FieldType.SUB_MERCHANT_ID.getName(), user.getPayId());
						doc2.put(FieldType.PAY_ID.getName(), user.getSuperMerchantId());
						doc2.put(FieldType.SUB_MERCHANT_ID.getName(), user.getPayId());
					} else {
						// normalMerchant
						doc.put(FieldType.PAY_ID.getName(), user.getPayId());
						doc2.put(FieldType.PAY_ID.getName(), user.getPayId());
					}
					doc.put(FieldType.VPA.getName(), user.getMerchantVPA());
					doc.put(FieldType.VIRTUAL_ACC_NUM.getName(), user.getVirtualAccountNo());
					doc.put(FieldType.IFSC_CODE.getName(), user.getVirtualIfscCode());
					doc.put(FieldType.VIRTUAL_BENEFICIARY_NAME.getName(), user.getVirtualBeneficiaryName());
					doc.put(FieldType.STATUS.getName(), "SUCCESS");
					doc.put(FieldType.PG_RESPONSE_MSG.getName(), "SUCCESS");
					doc.put(FieldType.ACQUIRER_NAME.getName(), PayoutAcquirer.ICICI.name());
					doc.put(FieldType.CREATE_DATE.getName(), dateNow);
					doc.put(FieldType.DATE_INDEX.getName(), dateNow.substring(0, 10).replace("-", ""));

					allVirtualData.add(doc);

					// update in payout mapping

					doc2.put(FieldType.STATUS.getName(), "Active");
					doc2.put(FieldType.CREATE_DATE.getName(), dateNow);
					doc2.put(FieldType.DATE_INDEX.getName(), dateNow.substring(0, 10).replace("-", ""));
					doc2.put(FieldType.ACCOUNT_TYPE.getName(), "Current");
					doc2.put(FieldType.USER_TYPE.getName(), "Payment Gateway");
					doc2.put(FieldType.ACQUIRER_NAME.getName(), PayoutAcquirer.ICICI.name());

					doc2.put(FieldType.VIRTUAL_ACC_NUM.getName(), user.getVirtualAccountNo());
					doc2.put(FieldType.VIRTUAL_BENEFICIARY_NAME.getName(), user.getVirtualBeneficiaryName());
					doc2.put(FieldType.IFSC_CODE.getName(), user.getVirtualIfscCode());

					allMerchantData.add(doc2);
				}

			} catch (Exception exception) {
				String message = "Error while inserting acquirer Saving Payout Mapping in database";
				logger.error(message, exception);
			}

		}

		collection.insertMany(allVirtualData);
		collection2.insertMany(allMerchantData);
		logger.info("inserted Virtual Account details successfully");

	}

	public void updateBulkTxnStatus(Fields fields) {
		try {
			if (fields.contains("flagBulk")) {
				fields.remove("flagBulk");
				field.updateIciciIMPSBulkTransaction(fields);
			}
		} catch (Exception e) {
			logger.info("Exception while updating bulk payout status ", fields);
		}

	}

	public Fields fetchPayoutBalance(Fields fields) {

		try {

			// Validate Hash

			String payId = fields.get(FieldType.PAY_ID.getName());
			String hash = fields.get(FieldType.HASH.getName());
			fields.remove(FieldType.HASH.getName());

			Date dNow = new Date();
			String dateNow = DateCreater.formatDateForDb(dNow);

			if (StringUtils.isBlank(payId) || StringUtils.isBlank(hash)) {

				fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_REQUEST_FIELD.getCode());
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.INVALID_REQUEST_FIELD.getInternalMessage());
				fields.put(FieldType.PAYOUT_DATE.getName(), dateNow);
				fields.put(FieldType.HASH.getName(), Hasher.getHash(fields));
				return fields;

			}

			logger.info("Request Hash == " + hash);
			String calculatedHash = Hasher.getHash(fields);
			logger.info("Calculated Hash == " + calculatedHash);

			if (!calculatedHash.equalsIgnoreCase(hash)) {

				fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_HASH.getCode());
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.INVALID_HASH.getInternalMessage());
				fields.put(FieldType.PAYOUT_DATE.getName(), dateNow);
				fields.put(FieldType.HASH.getName(), Hasher.getHash(fields));
				return fields;
			}

			fields = vendorPayoutDao.getMerchantBalance(fields);

		} catch (Exception e) {
			logger.error("Exception  ", e);
			return fields;
		}
		return fields;

	}

	public void cashfreeCallbackResponseHandler(Fields fields) {
		logger.info("inside cashfreeCallbackResponseHandler()");
		try {

			vendorPayoutDao.getTransactionPayId(fields);

			if (StringUtils.isBlank(fields.get(FieldType.PAY_ID.getName()))) {
				logger.info(
						"cashfreeCallbackResponseHandler(), Pay Id not found with Txn id " + fields.get("transferId"));
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.TRANSACTION_NOT_FOUND.getResponseMessage());
				return;
			}

			getMappingData(fields);

			// CheckingCashfree Checksum
			boolean isValidSignature = cashfreeChecksumUtil.verifyingPayoutChecksum(fields);

			removeMappingFields(fields);

			if (!isValidSignature) {
				logger.info("Cashfree payout callback signature mismatched received signature ",
						fields.get("signature"));
				fields.remove(FieldType.PAY_ID.getName());

				fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.SIGNATURE_MISMATCH.getResponseMessage());

				return;
			}

			String status = cashfreePayoutService.checkTxnStatus(fields);
			String event = fields.get("event");

			if (StringUtils.isNotBlank(status)) {
				cashfreePayoutService.handlePayoutCallback(fields);

				String payId = fields.get(FieldType.PAY_ID.getName());
				String subMerchantId = fields.get(FieldType.SUB_MERCHANT_ID.getName());

				if (fields.contains(FieldType.STATUS.getName())) {
					if (!((fields.get(FieldType.STATUS.getName()).equalsIgnoreCase(StatusType.CAPTURED.getName()))
							|| (fields.get(FieldType.STATUS.getName())
									.equalsIgnoreCase(StatusType.TIMEOUT.getName())))) {
						vendorPayoutDao.UpdateForClosing(fields);
					}
				}
				removeSubMerchantId(fields);

				if (!(status.equalsIgnoreCase(StatusType.CAPTURED.getName())
						&& event.equalsIgnoreCase("TRANSFER_SUCCESS"))) {

					UserSettingData userSetting = userSettingDao.fetchDataUsingPayId(payId);
					// callback to merchant
					if (userSetting.isAllowPayoutStatusEnquiryCallbackFlag()) {
						if (StringUtils.isNotBlank(userSetting.getPayoutCallbackUrl())) {
							payoutCallbackToMerchant(fields, userSetting);
						} else {
							// do nothing, just logging
							logger.info("Payout Cashfree enquiry response, Callback url is empty");
						}
					} else {
						// just logging
						logger.info("Payout Status enquiry, Callback flag not active for payId " + payId);
					}
				}
			}

		} catch (Exception exception) {
			logger.error("Exception in updating closing collection", exception);
		} finally {
			removeSubMerchantId(fields);
		}

	}

	public void addRequestInFields(HttpServletRequest httpRequest, HttpServletResponse httpResponse, Fields fields) {
		try {

			Map<String, String[]> fieldMapObj = httpRequest.getParameterMap();

			for (Entry<String, String[]> entry : fieldMapObj.entrySet()) {
				try {
					fields.put(entry.getKey(), entry.getValue()[0]);
				} catch (ClassCastException classCastException) {
					logger.error("Exception", classCastException);
				}
			}

			logger.info("response cashfree payout Callback response = " + fields.getFields());

		} catch (Exception e) {
			logger.info("exception in addRequestInFields()", e);
		}

	}

	public String decryptPayoutEncData(String response) {

		return iciciUtil.compositeApiDecryption(response, false);
	}

	public void cashfreePayoutAddBalance(Fields fields) {

		try {
			if (StringUtils.isBlank(fields.get(FieldType.PAY_ID.getName()))) {
				logger.info("cashfreePayoutAddBalance(), Pay Id not found " + fields.getFields());
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.PAY_ID.getResponseMessage());
				return;
			}

			getMappingData(fields);

			// CheckingCashfree Checksum
			boolean isValidSignature = cashfreeChecksumUtil.verifyingPayoutChecksum(fields);

			removeMappingFields(fields);

			if (!isValidSignature) {
				logger.info("Cashfree payout Add balance signature mismatched received signature ",
						fields.get("signature"));
				fields.remove(FieldType.PAY_ID.getName());

				fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.SIGNATURE_MISMATCH.getResponseMessage());

				return;
			}

			String response = cashfreePayoutService.handleAddBalaceRequest(fields);
			if (StringUtils.isNotBlank(response))
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), response);
			else
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), "Failed");

		} catch (Exception e) {
			logger.info("exception in cashfreePayoutAddBalance() ", e);
		}

	}

	public boolean verifyAmountandTxnType(Fields fields) {

		String txnType = fields.get(FieldType.TXNTYPE.getName());
		BigDecimal amount = new BigDecimal(fields.get(FieldType.AMOUNT.getName()));
		if (txnType.equalsIgnoreCase("RTGS")) {
			logger.info("Amount verifing with RTGS Transaction");
			// Must greater or equal 2 lac
			if (amount.compareTo(new BigDecimal("20000000")) < 0) {
				fields.put(FieldType.STATUS.getName(), StatusType.REJECTED_BY_PG.getName());
				fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_AMOUNT.getCode());
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.INVALID_AMOUNT.getResponseMessage());
				fields.put(FieldType.PG_TXN_MESSAGE.getName(), ErrorType.INVALID_AMOUNT.getResponseMessage());
				return false;
			}
			return true;
		} else {
			logger.info("Amount verifing with IMPS/NEFT/UPI Transaction");
			// Must less than 2 lac
			if (amount.compareTo(new BigDecimal("20000000")) >= 0) {
				fields.put(FieldType.STATUS.getName(), StatusType.REJECTED_BY_PG.getName());
				fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_AMOUNT.getCode());
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.INVALID_AMOUNT.getResponseMessage());
				fields.put(FieldType.PG_TXN_MESSAGE.getName(), ErrorType.INVALID_AMOUNT.getResponseMessage());
				return false;
			}
			return true;
		}
	}

	public void saveFailedTxnInDb(Fields fields, User user) {
		try {

			if (StringUtils.isBlank(fields.get(FieldType.TXN_ID.getName()))) {
				fields.put(FieldType.TXN_ID.getName(), TransactionManager.getNewTransactionId());
				fields.put(FieldType.PG_REF_NUM.getName(), fields.get(FieldType.TXN_ID.getName()));
			}

			String payId = fields.get(FieldType.PAY_ID.getName());

			if (user == null)
				user = userDao.findPayId(payId);

			fields.put(FieldType.VIRTUAL_AC_CODE.getName(), user.getVirtualAccountNo());

			if (StringUtils.isNotBlank(user.getSuperMerchantId())) {
				fields.put(FieldType.SUB_MERCHANT_ID.getName(), fields.get(FieldType.PAY_ID.getName()));
				fields.put(FieldType.PAY_ID.getName(), user.getSuperMerchantId());
			}

			fields.put(FieldType.IS_STATUS_FINAL.getName(), "Y");

			String amount = fields.get(FieldType.AMOUNT.getName());

			fields.put(FieldType.AMOUNT.getName(), Amount.toDecimal(fields.get(FieldType.AMOUNT.getName()),
					fields.get(FieldType.CURRENCY_CODE.getName())));

			if (fields.contains("flagBulk")) {
				fields.remove("flagBulk");
				field.updateIciciIMPSBulkTransaction(fields);
			} else {
				field.insertIciciCompositeFields(fields);
			}

			fields.put(FieldType.AMOUNT.getName(), Amount.formatAmount(fields.get(FieldType.AMOUNT.getName()),
					fields.get(FieldType.CURRENCY_CODE.getName())));

			if (StringUtils.isNotBlank(fields.get(FieldType.SUB_MERCHANT_ID.getName())))
				removeSubMerchantId(fields);

			fields.remove(FieldType.VIRTUAL_AC_CODE.getName());

		} catch (Exception e) {
			logger.info("Exception in saveFailedTxnInDb() ", e);
		}

	}

	public boolean useStatusActive(User user) throws SystemException {

		if (user.getUserStatus().equals(UserStatusType.ACTIVE))
			return true;
		else
			return false;
	}

	public void handleApexCallback(Fields fields) {

		logger.info("Inside handleApexCallback");
		try {

			String response = null;

			JSONObject resJson = new JSONObject();
			resJson.put("STATUS", fields.get("STATUS").toString());
			resJson.put("CLIENT_ID", fields.get("CLIENT_ID").toString());
			resJson.put("RRN", fields.get("RRN").toString());
			resJson.put("AMOUNT", fields.get("AMOUNT").toString());
			resJson.put("MSG", fields.get("MSG").toString());

			response = resJson.toString();
			fields.clear();

			// Populate fields
			populatePayoutFields(fields, resJson.get("CLIENT_ID").toString(), PayoutAcquirer.APEXPAY.name());

			boolean isFinalStatus = apexPayService.apexPayoutCallbackResponse(fields, response);

			String payId = fields.get(FieldType.PAY_ID.getName());

			if (!isFinalStatus) {

				User user = userDao.findPayId(payId);

				if (StringUtils.isNotBlank(user.getSuperMerchantId())) {
					fields.put(FieldType.SUB_MERCHANT_ID.getName(), fields.get(FieldType.PAY_ID.getName()));
					fields.put(FieldType.PAY_ID.getName(), user.getSuperMerchantId());
				}

				try {

					if (fields.contains(FieldType.USER_TYPE.getName())) {
						if (StringUtils.isNotBlank(fields.get(FieldType.USER_TYPE.getName())) && (fields
								.get(FieldType.USER_TYPE.getName()).equalsIgnoreCase("Merchant Initiated Direct")
								|| fields.get(FieldType.USER_TYPE.getName())
										.equalsIgnoreCase("Merchant Initiated Indirect"))) {
							if (fields.contains(FieldType.STATUS.getName())) {
								if (!((fields.get(FieldType.STATUS.getName())
										.equalsIgnoreCase(StatusType.CAPTURED.getName()))
										|| (fields.get(FieldType.STATUS.getName())
												.equalsIgnoreCase(StatusType.TIMEOUT.getName())
												|| (fields.get(FieldType.STATUS.getName())
														.equalsIgnoreCase(StatusType.PROCESSING.getName()))))) {
									vendorPayoutDao.UpdateForClosing(fields);
								}
							}
						}
					}
				} catch (Exception exception) {
					logger.error("Exception in updating closing collection", exception);
				} finally {
					removeSubMerchantId(fields);
				}
			}

			fields.put(FieldType.AMOUNT.getName(), Amount.formatAmount(fields.get(FieldType.AMOUNT.getName()),
					fields.get(FieldType.CURRENCY_CODE.getName())));

			UserSettingData userSetting = userSettingDao.fetchDataUsingPayId(payId);

			// callback to merchant
			if (userSetting.isAllowPayoutStatusEnquiryCallbackFlag()) {
				if (StringUtils.isNotBlank(userSetting.getPayoutCallbackUrl())) {
					payoutCallbackToMerchant(fields, userSetting);
				} else {
					// do nothing, just logging
					logger.info("Payout Status enquiry, Callback url is empty");
				}
			} else {
				// just logging
				logger.info("Payout Status enquiry, Callback flag not active");
			}

		} catch (Exception e) {
			logger.info("Exception in communicateApexPayTransactionStatusEnq() ", e);
		}

	}

	public void populatePayoutFields(Fields fields, String pgRefNum, String acquirer) {

		try {
			MongoDatabase dbIns = mongoInstance.getDB();
			MongoCollection<Document> collection = dbIns
					.getCollection(PropertiesManager.propertiesMap.get(prefix + "ImpsSettlementCollection"));

			List<BasicDBObject> conditionList = new ArrayList<BasicDBObject>();
			conditionList.add(new BasicDBObject(FieldType.TXN_ID.getName(), pgRefNum));
			conditionList.add(new BasicDBObject(FieldType.ACQUIRER_NAME.getName(), acquirer));

			BasicDBObject query = new BasicDBObject("$and", conditionList);
			logger.info("Getting payout fields for callback , query = " + query.toString());

			MongoCursor<Document> cursor = collection.find(query).iterator();

			if (cursor.hasNext()) {

				Document doc = cursor.next();

				fields.put(FieldType.PAY_ID.getName(), doc.get(FieldType.PAY_ID.getName()).toString());
				fields.put(FieldType.ORDER_ID.getName(), doc.get(FieldType.ORDER_ID.getName()).toString());
				fields.put(FieldType.TXN_ID.getName(), doc.get(FieldType.TXN_ID.getName()).toString());
				fields.put(FieldType.AMOUNT.getName(), doc.get(FieldType.AMOUNT.getName()).toString());
				fields.put(FieldType.USER_TYPE.getName(), doc.get(FieldType.USER_TYPE.getName()).toString());
				fields.put(FieldType.CREATE_DATE.getName(), doc.get(FieldType.CREATE_DATE.getName()).toString());
				fields.put(FieldType.ACQUIRER_NAME.getName(), doc.get(FieldType.ACQUIRER_NAME.getName()).toString());

				if (doc.get(FieldType.SUB_MERCHANT_ID.getName()) != null) {
					fields.put(FieldType.SUB_MERCHANT_ID.getName(),
							doc.get(FieldType.SUB_MERCHANT_ID.getName()).toString());
				}

			}

			else {
				logger.info("Not found payout fields for callback , query = " + query.toString());
			}
		} catch (Exception e) {
			logger.info("exception in populatePayoutFields", e);
		}
	}

	public void handleFonePaisaCallback(String reqJson, Fields fields) throws SystemException {
		logger.info("Inside handleFonePaisaCallback");
		try {

			String response = null;

			Transaction transaction = new Transaction(reqJson);

			if (StringUtils.isBlank(transaction.getMerchantRefNo())) {
				throw new SystemException("Merchant Ref No not found");
			}

			// Populate fields
			populatePayoutFields(fields, transaction.getMerchantRefNo(), PayoutAcquirer.FONEPAISA.name());

			if (!fields.getFields().isEmpty()) {

				boolean isFinalStatus = fonePaisaService.payoutStatusCallbackRespones(fields, reqJson);

				String payId = fields.get(FieldType.PAY_ID.getName());

				if (!isFinalStatus) {

					User user = userDao.findPayId(payId);

					if (StringUtils.isNotBlank(user.getSuperMerchantId())) {
						fields.put(FieldType.SUB_MERCHANT_ID.getName(), fields.get(FieldType.PAY_ID.getName()));
						fields.put(FieldType.PAY_ID.getName(), user.getSuperMerchantId());
					}

					try {

						if (fields.contains(FieldType.USER_TYPE.getName())) {
							if (StringUtils.isNotBlank(fields.get(FieldType.USER_TYPE.getName())) && (fields
									.get(FieldType.USER_TYPE.getName()).equalsIgnoreCase("Merchant Initiated Direct")
									|| fields.get(FieldType.USER_TYPE.getName())
											.equalsIgnoreCase("Merchant Initiated Indirect"))) {
								if (fields.contains(FieldType.STATUS.getName())) {
									if (!((fields.get(FieldType.STATUS.getName())
											.equalsIgnoreCase(StatusType.CAPTURED.getName()))
											|| (fields.get(FieldType.STATUS.getName())
													.equalsIgnoreCase(StatusType.TIMEOUT.getName())
													|| (fields.get(FieldType.STATUS.getName())
															.equalsIgnoreCase(StatusType.PROCESSING.getName()))))) {
										vendorPayoutDao.UpdateForClosing(fields);
									}
								}
							}
						}
					} catch (Exception exception) {
						logger.error("Exception in updating closing collection", exception);
					} finally {
						removeSubMerchantId(fields);
					}
				}

				fields.put(FieldType.AMOUNT.getName(), Amount.formatAmount(fields.get(FieldType.AMOUNT.getName()),
						fields.get(FieldType.CURRENCY_CODE.getName())));

				UserSettingData userSetting = userSettingDao.fetchDataUsingPayId(payId);

				// callback to merchant
				if (userSetting.isAllowPayoutStatusEnquiryCallbackFlag()) {
					if (StringUtils.isNotBlank(userSetting.getPayoutCallbackUrl())) {
						payoutCallbackToMerchant(fields, userSetting);
					} else {
						// do nothing, just logging
						logger.info("Payout Status enquiry, Callback url is empty");
					}
				} else {
					// just logging
					logger.info("Payout Status enquiry, Callback flag not active");
				}

			} else {
				throw new SystemException("No Transaction Found");
			}

		} catch (Exception e) {
			logger.info("Exception in communicateFonePaisaTransactionStatusEnq() ", e);
			throw new SystemException(e.getMessage());
		}

	}

	private void communicateQaicashTransactionStatusEnq(Fields fields, PayoutAcquireMapping payoutAcquireMapping) {
		logger.info("inside communicateQaicashTransactionStatusEnq()");
		try {

			JSONObject adfFields;

			if (StringUtils.isNotBlank(payoutAcquireMapping.getAdfFields())) {
				adfFields = new JSONObject(payoutAcquireMapping.getAdfFields());
			} else {
				fields.put(FieldType.STATUS.getName(), StatusType.REJECTED_BY_PG.getName());
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.ACQUIRER_ERROR.getResponseMessage());
				fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.ACQUIRER_ERROR.getResponseCode());
				fields.put(FieldType.HASH.getName(), Hasher.getHash(fields));
				logger.info("Adf fields Invalid For Request ORDER_ID " + fields.get(FieldType.ORDER_ID.getName())
						+ " fields are " + fields.maskFieldsRequest(fields.getFields()));
				return;
			}

			getQaicashMappingData(fields);

			fields.put(FieldType.AMOUNT.getName(), Amount.toDecimal(fields.get(FieldType.AMOUNT.getName()),
					fields.get(FieldType.CURRENCY_CODE.getName())));

			boolean isFinalStatus = qaicashService.payoutStatus(fields, adfFields);

			String payId = fields.get(FieldType.PAY_ID.getName());

			if (!isFinalStatus) {

				User user = userDao.findPayId(payId);

				if (StringUtils.isNotBlank(user.getSuperMerchantId())) {
					fields.put(FieldType.SUB_MERCHANT_ID.getName(), fields.get(FieldType.PAY_ID.getName()));
					fields.put(FieldType.PAY_ID.getName(), user.getSuperMerchantId());
				}

				try {

					if (fields.contains(FieldType.USER_TYPE.getName())) {
						if (StringUtils.isNotBlank(fields.get(FieldType.USER_TYPE.getName())) && (fields
								.get(FieldType.USER_TYPE.getName()).equalsIgnoreCase("Merchant Initiated Direct")
								|| fields.get(FieldType.USER_TYPE.getName())
										.equalsIgnoreCase("Merchant Initiated Indirect"))) {
							if (fields.contains(FieldType.STATUS.getName())) {
								if (!((fields.get(FieldType.STATUS.getName())
										.equalsIgnoreCase(StatusType.CAPTURED.getName()))
										|| (fields.get(FieldType.STATUS.getName())
												.equalsIgnoreCase(StatusType.TIMEOUT.getName())
												|| (fields.get(FieldType.STATUS.getName())
														.equalsIgnoreCase(StatusType.PROCESSING.getName()))))) {
									vendorPayoutDao.UpdateForClosing(fields);
								}
							}
						}
					}
				} catch (Exception exception) {
					logger.error("Exception in updating closing collection", exception);
				} finally {
					removeSubMerchantId(fields);
				}
			}

			fields.put(FieldType.AMOUNT.getName(), Amount.formatAmount(fields.get(FieldType.AMOUNT.getName()),
					fields.get(FieldType.CURRENCY_CODE.getName())));

			UserSettingData userSetting = userSettingDao.fetchDataUsingPayId(payId);

			// callback to merchant
			if (userSetting.isAllowPayoutStatusEnquiryCallbackFlag()) {
				if (StringUtils.isNotBlank(userSetting.getPayoutCallbackUrl())) {
					payoutCallbackToMerchant(fields, userSetting);
				} else {
					// do nothing, just logging
					logger.info("Payout Status enquiry, Callback url is empty");
				}
			} else {
				// just logging
				logger.info("Payout Status enquiry, Callback flag not active");
			}

		} catch (Exception e) {
			logger.info("Exception in communicatePhonePaisaTransactionStatusEnq() ", e);
		}

	}

	private void communicateGlobalPayTransaction(Fields fields, PayoutAcquireMapping payoutAcquireMapping, User user) {
		logger.info("inside communicateToshaniDigitalTransaction()");
		try {
			getGlobalPayMappingData(fields);

			if (StringUtils.isBlank(fields.get(FieldType.MERCHANT_ID.getName()))
					|| StringUtils.isBlank(fields.get(FieldType.TXN_KEY.getName()))) {
				logger.info("ADF fields not found in mapping for GLOBALPAY");
				fields.put(FieldType.STATUS.getName(), StatusType.REJECTED_BY_PG.getName());
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), "Mapping not found");
				fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.REJECTED_BY_PG.getResponseCode());
				removeMappingFields(fields);
				throw new SystemException("GLOBALPAY Mapping Not Found");
			}

			JSONObject adfFields;

			if (StringUtils.isNotBlank(payoutAcquireMapping.getAdfFields())) {
				adfFields = new JSONObject(payoutAcquireMapping.getAdfFields());
			} else {
				fields.put(FieldType.STATUS.getName(), StatusType.REJECTED_BY_PG.getName());
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.ACQUIRER_ERROR.getResponseMessage());
				fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.ACQUIRER_ERROR.getResponseCode());
				fields.put(FieldType.HASH.getName(), Hasher.getHash(fields));
				logger.info("ADF Fields Not Found for Request ORDER_ID " + fields.get(FieldType.ORDER_ID.getName())
						+ " fields are " + fields.maskFieldsRequest(fields.getFields()));
				removeMappingFields(fields);
				return;
			}

			fields.put(FieldType.AMOUNT.getName(), Amount.toDecimal(fields.get(FieldType.AMOUNT.getName()),
					fields.get(FieldType.CURRENCY_CODE.getName())));

			globalPayService.payoutTransaction(fields, adfFields, user);

			fields.put(FieldType.AMOUNT.getName(), Amount.formatAmount(fields.get(FieldType.AMOUNT.getName()),
					fields.get(FieldType.CURRENCY_CODE.getName())));

		} catch (Exception e) {
			logger.info("Exception in ToshaniDigital", e);
		} finally {
			if (fields.get(FieldType.AMOUNT.getName()).contains(".")) {
				fields.put(FieldType.AMOUNT.getName(), Amount.formatAmount(fields.get(FieldType.AMOUNT.getName()),
						fields.get(FieldType.CURRENCY_CODE.getName())));
			}
		}

	}

	private void getGlobalPayMappingData(Fields fields) {
		try {
			String payId = fields.get(FieldType.PAY_ID.getName());

			logger.info("getTxnKey for Pay Id for " + payId);
			User user = userDao.findPayId(payId);
			Account account = null;
			Set<Account> accounts = null;

			if (StringUtils.isNotBlank(user.getSuperMerchantId()) && !user.isSuperMerchant()) {
				User superMerchant = userDao.findPayId(user.getSuperMerchantId());
				accounts = superMerchant.getAccounts();
			} else {
				accounts = user.getAccounts();
			}

			if (accounts == null || accounts.size() == 0) {
				logger.info("No account found for Pay ID = " + payId);
			} else {
				for (Account accountThis : accounts) {
					if (accountThis.getAcquirerName().equalsIgnoreCase(
							AcquirerType.getInstancefromCode(AcquirerType.GLOBALPAY.getCode()).getName())) {
						account = accountThis;
						break;
					}
				}
			}

			AccountCurrency accountCurrency = account.getAccountCurrency("356");
			fields.put(FieldType.MERCHANT_ID.getName(), accountCurrency.getMerchantId());
			fields.put(FieldType.TXN_KEY.getName(), accountCurrency.getTxnKey());

		} catch (Exception e) {
			logger.info("Exception in getGlobalPayMappingData() ", e);
		}

	}

	private void communicateGlobalPayTransactionStatusEnq(Fields fields, PayoutAcquireMapping payoutAcquireMapping) {
		logger.info("inside communicateGlobalPayTransactionStatusEnq()");
		try {

			JSONObject adfFields;

			if (StringUtils.isNotBlank(payoutAcquireMapping.getAdfFields())) {
				adfFields = new JSONObject(payoutAcquireMapping.getAdfFields());
			} else {
				fields.put(FieldType.STATUS.getName(), StatusType.REJECTED_BY_PG.getName());
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.ACQUIRER_ERROR.getResponseMessage());
				fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.ACQUIRER_ERROR.getResponseCode());
				fields.put(FieldType.HASH.getName(), Hasher.getHash(fields));
				logger.info("Adf fields Invalid For Request ORDER_ID " + fields.get(FieldType.ORDER_ID.getName())
						+ " fields are " + fields.maskFieldsRequest(fields.getFields()));
				return;
			}

			getGlobalPayMappingData(fields);

			fields.put(FieldType.AMOUNT.getName(), Amount.toDecimal(fields.get(FieldType.AMOUNT.getName()),
					fields.get(FieldType.CURRENCY_CODE.getName())));

			boolean isFinalStatus = globalPayService.payoutStatus(fields, adfFields);

			String payId = fields.get(FieldType.PAY_ID.getName());

			if (!isFinalStatus) {

				User user = userDao.findPayId(payId);

				if (StringUtils.isNotBlank(user.getSuperMerchantId())) {
					fields.put(FieldType.SUB_MERCHANT_ID.getName(), fields.get(FieldType.PAY_ID.getName()));
					fields.put(FieldType.PAY_ID.getName(), user.getSuperMerchantId());
				}

				try {

					if (fields.contains(FieldType.USER_TYPE.getName())) {
						if (StringUtils.isNotBlank(fields.get(FieldType.USER_TYPE.getName())) && (fields
								.get(FieldType.USER_TYPE.getName()).equalsIgnoreCase("Merchant Initiated Direct")
								|| fields.get(FieldType.USER_TYPE.getName())
										.equalsIgnoreCase("Merchant Initiated Indirect"))) {
							if (fields.contains(FieldType.STATUS.getName())) {
								if (!((fields.get(FieldType.STATUS.getName())
										.equalsIgnoreCase(StatusType.CAPTURED.getName()))
										|| (fields.get(FieldType.STATUS.getName())
												.equalsIgnoreCase(StatusType.TIMEOUT.getName())
												|| (fields.get(FieldType.STATUS.getName())
														.equalsIgnoreCase(StatusType.PROCESSING.getName()))))) {
									vendorPayoutDao.UpdateForClosing(fields);
								}
							}
						}
					}
				} catch (Exception exception) {
					logger.error("Exception in updating closing collection", exception);
				} finally {
					removeSubMerchantId(fields);
				}
			}

			fields.put(FieldType.AMOUNT.getName(), Amount.formatAmount(fields.get(FieldType.AMOUNT.getName()),
					fields.get(FieldType.CURRENCY_CODE.getName())));

			UserSettingData userSetting = userSettingDao.fetchDataUsingPayId(payId);

			// callback to merchant
			if (userSetting.isAllowPayoutStatusEnquiryCallbackFlag()) {
				if (StringUtils.isNotBlank(userSetting.getPayoutCallbackUrl())) {
					payoutCallbackToMerchant(fields, userSetting);
				} else {
					// do nothing, just logging
					logger.info("Payout Status enquiry, Callback url is empty");
				}
			} else {
				// just logging
				logger.info("Payout Status enquiry, Callback flag not active");
			}

		} catch (Exception e) {
			logger.info("Exception in communicateGlobalPayTransactionStatusEnq() ", e);
		}

	}

	public void updateStatusForPayoutTransaction(Fields fields) {

		logger.info("Orignal response data for payout before updating status >> " + fields.getFields());

		if (!fields.get(FieldType.STATUS.getName()).equalsIgnoreCase(StatusType.CAPTURED.getName())) {
			fields.put(FieldType.STATUS.getName(), StatusType.PROCESSING.getName());
			fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.PROCESSING.getCode());
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.PROCESSING.getResponseMessage());
		}

	}

}
