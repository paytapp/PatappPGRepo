package com.paymentgateway.payout;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.lang.StringUtils;
import org.bson.Document;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.paymentgateway.commons.api.Hasher;
import com.paymentgateway.commons.dao.UserSettingDao;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.user.ImpsDownloadObject;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserSettingData;
import com.paymentgateway.commons.util.AcquirerType;
import com.paymentgateway.commons.util.Amount;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.DateCreater;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PayoutPupose;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TransactionManager;
import com.paymentgateway.commons.util.TransactionType;
import com.paymentgateway.icici.composite.api.IciciCommunicator;
import com.paymentgateway.icici.composite.api.IciciResponseHandler;
import com.paymentgateway.icici.composite.api.IciciTransactionConverter;
import com.paymentgateway.idfcUpi.IdfcUpiIntegrator;
import com.paymentgateway.pg.service.VpaValidationIDFC;

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
	private IdfcUpiIntegrator idfcIntegrator;
	
	@Autowired
	private UserSettingDao userSettingDao;

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
	/*
	 * public boolean isDailyLimitExceed(Fields fields) {
	 * logger.info("Inside  isDailyLimitExceed()"); // String fieldAmount = //
	 * Amount.toDecimal(fields.get(FieldType.AMOUNT.getName()), //
	 * fields.get(FieldType.CURRENCY_CODE.getName()));
	 * 
	 * String fieldAmount = fields.get(FieldType.AMOUNT.getName());
	 * 
	 * BigDecimal settledTransactionDiff =
	 * vendorPayoutDao.getSettleTransactionDiff(fields);
	 * 
	 * if (settledTransactionDiff.compareTo(BigDecimal.ZERO) <= 0) { return
	 * true; }
	 * 
	 * BigDecimal totalImpsTransactionAmount =
	 * vendorPayoutDao.getImpsTransactionAmount(fields); BigDecimal
	 * totalChargebackAmount =
	 * vendorPayoutDao.getChargebackTransactionAmount(fields);
	 * 
	 * BigDecimal checkDiff = new BigDecimal(0); checkDiff =
	 * checkDiff.add(settledTransactionDiff).setScale(2); checkDiff =
	 * checkDiff.subtract(totalChargebackAmount).setScale(2); checkDiff =
	 * checkDiff.subtract(totalImpsTransactionAmount).setScale(2); checkDiff =
	 * checkDiff.subtract(new BigDecimal(fieldAmount).setScale(2));
	 * 
	 * logger.info("Calculated final settled amount is " + checkDiff);
	 * 
	 * if (checkDiff.compareTo(BigDecimal.ZERO) >= 0) { return false; }
	 * 
	 * return true;
	 * 
	 * }
	 */

	/*
	 * public boolean isDailyLimitExceed(Fields fields) {
	 * logger.info("Inside  isDailyLimitExceed()");
	 * 
	 * String fieldAmount = fields.get(FieldType.AMOUNT.getName());
	 * 
	 * BigDecimal eCollectionTransactionDiff =
	 * vendorPayoutDao.getECollectionTransactionAmount(fields);
	 * 
	 * if (eCollectionTransactionDiff.compareTo(BigDecimal.ZERO) <= 0) { return
	 * true; }
	 * 
	 * BigDecimal totalImpsTransactionAmount =
	 * vendorPayoutDao.getImpsTransactionAmount(fields);
	 * 
	 * BigDecimal checkDiff = new BigDecimal(0); checkDiff =
	 * checkDiff.add(eCollectionTransactionDiff).setScale(2); checkDiff =
	 * checkDiff.subtract(totalImpsTransactionAmount).setScale(2); checkDiff =
	 * checkDiff.subtract(new BigDecimal(fieldAmount).setScale(2));
	 * 
	 * logger.info("Calculated final settled amount is " + checkDiff);
	 * 
	 * if (checkDiff.compareTo(BigDecimal.ZERO) >= 0) { return false; }
	 * 
	 * return true;
	 * 
	 * }
	 */
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
			logger.error("Exception Declined due to insufficient balance : ", e);
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

			field.insertIciciCompositeFields(fields);
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
			logger.error("Exception Declined due to insufficient balance : ", e);
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
		// fields.remove(FieldType.TXN_ID.getName());
		fields.remove(FieldType.USER_TYPE.getName());
		fields.remove(FieldType.AMOUNT.getName());
		fields.remove(FieldType.BANK_REF_NUM.getName());
		fields.remove(FieldType.PG_REF_NUM.getName());
		fields.remove(FieldType.PG_TXN_STATUS.getName());
		fields.remove(FieldType.SUB_MERCHANT_ID.getName());
		fields.remove(FieldType.CREATE_DATE.getName());
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

	public void clearFieldsForMerchantDirect(Fields fields) throws SystemException {
		fields.remove(FieldType.USER_TYPE.getName());
		fields.remove(FieldType.BANK_REF_NUM.getName());
		fields.remove(FieldType.SUB_MERCHANT_ID.getName());
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
		// if (StringUtils.isBlank(fields.get(FieldType.ORDER_ID.getName()))) {
		// fields.put(FieldType.STATUS.getName(), StatusType.ERROR.getName());
		// fields.put(FieldType.RESPONSE_MESSAGE.getName(),
		// ErrorType.VALIDATION_FAILED.getResponseMessage());
		// fields.put(FieldType.RESPONSE_CODE.getName(),
		// ErrorType.VALIDATION_FAILED.getResponseCode());
		// logger.info("INVALID ORDER_ID " + fields.getFields());
		// return true;
		// }
		if (StringUtils.isBlank(fields.get(FieldType.PAY_ID.getName()))) {
			fields.put(FieldType.STATUS.getName(), StatusType.ERROR.getName());
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.VALIDATION_FAILED.getResponseMessage());
			fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.VALIDATION_FAILED.getResponseCode());
			logger.info("INVALID PAY_ID " + fields.getFields());
			return true;
		}
		// if (StringUtils.isBlank(fields.get(FieldType.PAYER_NAME.getName())))
		// {
		// fields.put(FieldType.STATUS.getName(), StatusType.ERROR.getName());
		// fields.put(FieldType.RESPONSE_MESSAGE.getName(),
		// ErrorType.VALIDATION_FAILED.getResponseMessage());
		// fields.put(FieldType.RESPONSE_CODE.getName(),
		// ErrorType.VALIDATION_FAILED.getResponseCode());
		// logger.info("INVALID BENE_NAME " + fields.getFields());
		// return true;
		// }
		//
		if (StringUtils.isBlank(fields.get(FieldType.PAYER_ADDRESS.getName()))) {
			fields.put(FieldType.STATUS.getName(), StatusType.ERROR.getName());
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.VALIDATION_FAILED.getResponseMessage());
			fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.VALIDATION_FAILED.getResponseCode());
			logger.info("INVALID VPA " + fields.getFields());
			return true;
		}
		// if
		// (StringUtils.isBlank(fields.get(FieldType.CURRENCY_CODE.getName())))
		// {
		// fields.put(FieldType.STATUS.getName(), StatusType.ERROR.getName());
		// fields.put(FieldType.RESPONSE_MESSAGE.getName(),
		// ErrorType.VALIDATION_FAILED.getResponseMessage());
		// fields.put(FieldType.RESPONSE_CODE.getName(),
		// ErrorType.VALIDATION_FAILED.getResponseCode());
		// logger.info("INVALID CURRENCY_CODE " + fields.getFields());
		// return true;
		// }
		return false;

	}

	public boolean isMerchantAllowed(Fields fields) {
		User user = userDao.findPayId(fields.get(FieldType.PAY_ID.getName()));
		if (user.isAccountVerificationFlag()) {
			logger.info("Merchant Allowed to user");
			return true;
		}
		return false;
	}

	public boolean isVpaVerificationMerchantAllowed(Fields fields) {
		UserSettingData userSetting = userSettingDao.fetchDataUsingPayId(fields.get(FieldType.PAY_ID.getName()));
		if (userSetting.isVpaVerificationFlag()) {
			logger.info("Merchant Allowed to user");
			return true;
		}
		return false;
	}

	public boolean isDirectTransctionAllowed(Fields fields) {
		User user = userDao.findPayId(fields.get(FieldType.PAY_ID.getName()));

		return user.isMerchantInitiatedDirectFlag();
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
			// Check DB for BENE

			if ((StringUtils.isNotBlank(fields.get(FieldType.USER_TYPE.getName()))) && ((fields
					.get(FieldType.USER_TYPE.getName()).equalsIgnoreCase("Merchant Initiated Direct"))
					|| (fields.get(FieldType.USER_TYPE.getName()).equalsIgnoreCase("Merchant Initiated Indirect")))) {
				logger.info("insertion from IMPS");
				vendorPayoutDao.insertUpdateForClosing(fields);
			}

			// if (!iciciResponseHandler.isBeneAlreadyRegistered(fields)) {
			// logger.info("inside the registration of beneficiary for IMPS " +
			// fields.getFields());
			//
			// fields.put(FieldType.TXN_ID.getName(),
			// TransactionManager.getNewTransactionId());
			// // Check the beneficiary
			// String reqUrl =
			// PropertiesManager.propertiesMap.get("ICICI_COMPOSITE_BENE_ADDITION");
			// String beneApiKey;
			//
			// if(iciciResponseHandler.isPaybleMerchant(fields)){
			// beneApiKey =
			// PropertiesManager.propertiesMap.get("PAYBLE_ICICI_COMPOSITE_BENE_REG_API_KEY");
			// }else{
			// beneApiKey =
			// PropertiesManager.propertiesMap.get("ICICI_COMPOSITE_BENE_REG_API_KEY");
			// }
			// fields.put(FieldType.REQUEST_TYPE.getName(),
			// FieldType.BENE_REGISTRATION.getName());
			// Map<String, String> beneResponse = new HashMap<String, String>();
			//
			// String req =
			// iciciTransactionConverter.createCompositeIMPSRequestForAddBene(fields);
			// String resp = iciciCommunicator.getIciciCompositeApiResponse(req,
			// reqUrl, fields, null, beneApiKey);
			// beneResponse =
			// iciciResponseHandler.compositeBeneAdditionResponseHandler(fields,
			// resp);
			// logger.info("Bene data Update ");
			//
			// if (!iciciResponseHandler.isBeneSuccess(beneResponse)) {
			// logger.info("Beneficary addition failed for "
			// +
			// beneResponse.get(beneResponse.get(FieldType.BENE_ACCOUNT_NO.getName())));
			// fields.put(FieldType.STATUS.getName(),
			// beneResponse.get(FieldType.STATUS.getName()));
			// fields.put(FieldType.RESPONSE_MESSAGE.getName(),
			// beneResponse.get(FieldType.RESPONSE_MESSAGE.getName()));
			// fields.put(FieldType.RESPONSE_CODE.getName(),
			// beneResponse.get(FieldType.RESPONSE_CODE.getName()));
			// fields.put(FieldType.PG_TXN_MESSAGE.getName(),
			// beneResponse.get(FieldType.PG_RESPONSE_MSG.getName()));
			// fields.put(FieldType.PG_RESP_CODE.getName(),
			// beneResponse.get(FieldType.PG_RESP_CODE.getName()));
			//
			// // Insert In Transaction
			// fields.put(FieldType.AMOUNT.getName(),
			// Amount.toDecimal(fields.get(FieldType.AMOUNT.getName()),
			// fields.get(FieldType.CURRENCY_CODE.getName())));
			//
			// iciciResponseHandler.compositeIMPSTransactionResponseHandler(fields,
			// resp);
			//
			// fields.put(FieldType.AMOUNT.getName(),
			// Amount.formatAmount(fields.get(FieldType.AMOUNT.getName()),
			// fields.get(FieldType.CURRENCY_CODE.getName())));
			//
			// fields.put(FieldType.HASH.getName(), Hasher.getHash(fields));
			//
			// logger.info("Bene Registration Failed " + fields.getFields());
			// return;
			// }
			// }
			//
			// logger.info("beneficiary verified");

			fields.put(FieldType.AMOUNT.getName(), Amount.toDecimal(fields.get(FieldType.AMOUNT.getName()),
					fields.get(FieldType.CURRENCY_CODE.getName())));

			String url = PropertiesManager.propertiesMap.get("ICICI_COMPOSITE_PAYMENT_URL");
			String priorty = PropertiesManager.propertiesMap.get("ICICI_IMPS_PRIORTY");
			String apiKey = PropertiesManager.propertiesMap.get("ICICI_COMPOSITE_API_KEY");

			if (iciciResponseHandler.isPaybleMerchant(fields)) {
				apiKey = PropertiesManager.propertiesMap.get("PAYBLE_ICICI_COMPOSITE_API_KEY");
			} else {
				apiKey = PropertiesManager.propertiesMap.get("ICICI_COMPOSITE_API_KEY");
			}

			String request = iciciTransactionConverter.createCompositeIMPSRequest(fields);
			String response = iciciCommunicator.getIciciCompositeApiResponse(request, url, fields, priorty, apiKey);
			iciciResponseHandler.compositeIMPSTransactionResponseHandler(fields, response);

			fields.put(FieldType.AMOUNT.getName(), Amount.formatAmount(fields.get(FieldType.AMOUNT.getName()),
					fields.get(FieldType.CURRENCY_CODE.getName())));
		} catch (Exception e) {
			logger.error("Exception in comunication with Compostie IMPS for txn Id"
					+ field.get(FieldType.TXN_ID.getName()) + " ", e);
		}
	}

	public void communicateCompositeUPITransaction(Fields fields) {
		logger.info("inside communicateCompositeUPITransaction()");
		try {

			if ((StringUtils.isNotBlank(fields.get(FieldType.USER_TYPE.getName()))) && ((fields
					.get(FieldType.USER_TYPE.getName()).equalsIgnoreCase("Merchant Initiated Direct"))
					|| (fields.get(FieldType.USER_TYPE.getName()).equalsIgnoreCase("Merchant Initiated Indirect")))) {
				logger.info("Insertion from UPI");
				vendorPayoutDao.insertUpdateForClosing(fields);
			}

			// // Check DB for BENE
			// if (!iciciResponseHandler.isBeneAlreadyRegistered(fields)) {
			// logger.info("inside the registration of beneficiary for VPA " +
			// fields.getFields());
			//
			// fields.put(FieldType.TXN_ID.getName(),
			// TransactionManager.getNewTransactionId());
			//
			// // Check the beneficiary
			// String reqUrl =
			// PropertiesManager.propertiesMap.get("ICICI_COMPOSITE_VPA_BENE_ADDITION");
			// String beneApiKey;
			//
			// if(iciciResponseHandler.isPaybleMerchant(fields)){
			// beneApiKey =
			// PropertiesManager.propertiesMap.get("PAYBLE_ICICI_COMPOSITE_BENE_REG_API_KEY");
			// }else{
			// beneApiKey =
			// PropertiesManager.propertiesMap.get("ICICI_COMPOSITE_BENE_REG_API_KEY");
			// }
			// fields.put(FieldType.REQUEST_TYPE.getName(),
			// FieldType.BENE_REGISTRATION.getName());
			// Map<String, String> beneResponse = new HashMap<String, String>();
			//
			// String req =
			// iciciTransactionConverter.createCompositeUPIRequestForAddBene(fields);
			// String resp = iciciCommunicator.getIciciCompositeApiResponse(req,
			// reqUrl, fields, null, beneApiKey);
			// beneResponse =
			// iciciResponseHandler.compositeBeneAdditionResponseHandler(fields,
			// resp);
			// logger.info("Bene data Update ");
			//
			// if (!iciciResponseHandler.isBeneSuccess(beneResponse)) {
			// logger.info(
			// "Beneficary addition failed for " +
			// beneResponse.get(FieldType.PAYEE_ADDRESS.getName()));
			//
			// fields.put(FieldType.STATUS.getName(),
			// beneResponse.get(FieldType.STATUS.getName()));
			// fields.put(FieldType.RESPONSE_MESSAGE.getName(),
			// beneResponse.get(FieldType.RESPONSE_MESSAGE.getName()));
			// fields.put(FieldType.RESPONSE_CODE.getName(),
			// beneResponse.get(FieldType.RESPONSE_CODE.getName()));
			// fields.put(FieldType.PG_TXN_MESSAGE.getName(),
			// beneResponse.get(FieldType.PG_RESPONSE_MSG.getName()));
			// fields.put(FieldType.PG_RESP_CODE.getName(),
			// beneResponse.get(FieldType.PG_RESP_CODE.getName()));
			//
			// // Insert In Transaction
			// fields.put(FieldType.AMOUNT.getName(),
			// Amount.toDecimal(fields.get(FieldType.AMOUNT.getName()),
			// fields.get(FieldType.CURRENCY_CODE.getName())));
			//
			// iciciResponseHandler.compositeUPITransactionResponseHandler(fields,
			// resp);
			//
			// fields.put(FieldType.AMOUNT.getName(),
			// Amount.formatAmount(fields.get(FieldType.AMOUNT.getName()),
			// fields.get(FieldType.CURRENCY_CODE.getName())));
			//
			// fields.put(FieldType.HASH.getName(), Hasher.getHash(fields));
			// return;
			// }
			// }
			//
			// logger.info("beneficiary verified");

			fields.put(FieldType.AMOUNT.getName(), Amount.toDecimal(fields.get(FieldType.AMOUNT.getName()),
					fields.get(FieldType.CURRENCY_CODE.getName())));

			String url = PropertiesManager.propertiesMap.get("ICICI_COMPOSITE_PAYMENT_URL");
			String priorty = PropertiesManager.propertiesMap.get("ICICI_UPI_PRIORTY");
			String apiKey;

			if (iciciResponseHandler.isPaybleMerchant(fields)) {
				apiKey = PropertiesManager.propertiesMap.get("PAYBLE_ICICI_COMPOSITE_API_KEY");
			} else {
				apiKey = PropertiesManager.propertiesMap.get("ICICI_COMPOSITE_API_KEY");
			}

			String request = iciciTransactionConverter.createCompositeUPIRequest(fields);
			String response = iciciCommunicator.getIciciCompositeApiResponse(request, url, fields, priorty, apiKey);

			iciciResponseHandler.compositeUPITransactionResponseHandler(fields, response);

			fields.put(FieldType.AMOUNT.getName(), Amount.formatAmount(fields.get(FieldType.AMOUNT.getName()),
					fields.get(FieldType.CURRENCY_CODE.getName())));
		} catch (Exception e) {
			logger.info("Exception in comunication with Compostie UPI for txn Id"
					+ field.get(FieldType.TXN_ID.getName()) + " ", e);
		}

	}

	public void communicateVpaVerification(Fields fields) {
		try {

			String acquirerType = PropertiesManager.propertiesMap.get("VPA_VERIFICATION_ACQUIRER");

			switch (AcquirerType.getInstancefromCode(acquirerType)) {

			case IDFCUPI:

				fields.put(FieldType.ADF5.getName(), PropertiesManager.propertiesMap.get("IDFC_VPA_VERIFY_ADF5"));
				fields.put(FieldType.ADF6.getName(), PropertiesManager.propertiesMap.get("IDFC_VPA_VERIFY_ADF6"));
				fields.put(FieldType.ADF7.getName(), PropertiesManager.propertiesMap.get("IDFC_VPA_VERIFY_ADF7"));
				fields.put(FieldType.ADF8.getName(), PropertiesManager.propertiesMap.get("IDFC_VPA_VERIFY_ADF8"));
				fields.put(FieldType.ADF9.getName(), PropertiesManager.propertiesMap.get("IDFC_VPA_VERIFY_ADF9"));
				fields.put(FieldType.ADF10.getName(), PropertiesManager.propertiesMap.get("IDFC_VPA_VERIFY_ADF10"));

				JSONObject responseJson = idfcIntegrator.vpaValidation(fields, fields.get(FieldType.ADF6.getName()));
				logger.info("Bene VPA Verification Response >>> " + responseJson);

				if (responseJson != null) {
					handleVpaResponseIdfc(fields, responseJson);
					clearFieldsForUPIBeneVerification(fields);

					vendorPayoutDao.insertUpiBeneVerification(fields);
					logger.info("Bene VPA Verification Inserted in DB");
				}

				break;

			case CASHFREE:

				// token create
				String tokenResponse = cashFreeTokenResponse();
				String token = null;
				if (StringUtils.isNotBlank(tokenResponse)) {

					JSONObject jsontokenResposne = new JSONObject(tokenResponse);

					if (jsontokenResposne.getString("subCode").equals("200")) {
						token = jsontokenResposne.getJSONObject("data").getString("token");
					}

					String response = null;
					if (StringUtils.isNotBlank(token)) {
						response = getVpaResponse(token, fields);
					}

					handleVpaResponseCashFree(fields, response);
					
					vendorPayoutDao.insertUpiBeneVerification(fields);
					logger.info("Bene VPA Verification Inserted in DB");

				} else {
					logger.info("cashFree Token is null token >> " + token);
					clearFieldsForUPIBeneVerification(fields);
				}
				break;

			default:
				logger.info("No VPA ACQUIRER MATCHED " + acquirerType);
				break;
			}

		} catch (Exception e) {
			clearFieldsForUPIBeneVerification(fields);
			logger.info("Exception in comunication with UPI Verification for txn Id"
					+ field.get(FieldType.TXN_ID.getName()) + " ", e);
		}

	}

	private void handleVpaResponseCashFree(Fields fields, String response) {
		logger.info("inside handleVpaResponseCashFree()");
		try {
			JSONObject responseJson;

			if (StringUtils.isNotBlank(response)) {
				responseJson = new JSONObject(response);
			} else {
				responseJson = new JSONObject();
			}

			String status = null;
			ErrorType errorType = null;
			String pgTxnMsg = null;
			
			
			JSONObject data = new JSONObject();
			
			if (responseJson.has("data")) {
				data = responseJson.getJSONObject("data");
				if (data.has("nameAtBank") && StringUtils.isNotBlank((String) data.get("nameAtBank"))) {
					fields.put(FieldType.PAYER_NAME.getName(), (String) data.get("nameAtBank"));
				}

			}

			if (responseJson.has("subCode") && StringUtils.isNotBlank((String) responseJson.get("subCode"))
					&& responseJson.getString("subCode").equalsIgnoreCase("200")) {
				if(data.has("accountExists") && StringUtils.isNotBlank((String) data.get("accountExists"))
						&& data.getString("accountExists").equalsIgnoreCase("YES")){
					status = StatusType.CAPTURED.getName();
					errorType = ErrorType.SUCCESS;
					pgTxnMsg = ErrorType.SUCCESS.getResponseMessage();
					
					if (responseJson.has("message") && StringUtils.isNotBlank((String) responseJson.get("message"))) {
						fields.put(FieldType.PG_TXN_MESSAGE.getName(), (String) responseJson.get("message"));
					}
				}else{
					status = StatusType.FAILED.getName();
					errorType = ErrorType.FAILED;
					pgTxnMsg = ErrorType.FAILED.getResponseMessage();
					
					fields.put(FieldType.PG_TXN_MESSAGE.getName(), "Bank Account Not Found");
				}
			} else {
				status = StatusType.FAILED.getName();
				errorType = ErrorType.FAILED;
				pgTxnMsg = ErrorType.FAILED.getResponseMessage();
				
				if (responseJson.has("message") && StringUtils.isNotBlank((String) responseJson.get("message"))) {
					fields.put(FieldType.PG_TXN_MESSAGE.getName(), (String) responseJson.get("message"));
				}
			}
			
			fields.put(FieldType.STATUS.getName(), status);
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), errorType.getResponseMessage());
			fields.put(FieldType.RESPONSE_CODE.getName(), pgTxnMsg);

			if (responseJson.has("subCode") && StringUtils.isNotBlank((String) responseJson.get("subCode"))) {
				fields.put(FieldType.PG_RESP_CODE.getName(), (String) responseJson.get("subCode"));
			}

			

			logger.info("Fields Updated " + fields.getFields());

		} catch (Exception e) {
			logger.info("exception ", e);
		}
	}

	private String getVpaResponse(String token, Fields fields) {

		HttpsURLConnection connection = null;
		try {

			String vpaValidateUrl = PropertiesManager.propertiesMap.get("CASHFREE_VPA_VERIFY_URL");

			StringBuilder reqUrl = new StringBuilder();

			reqUrl.append(vpaValidateUrl);
			reqUrl.append("?vpa=");
			reqUrl.append(fields.get(FieldType.PAYER_ADDRESS.getName()));
			reqUrl.append("&name=");
			reqUrl.append("VpaVerification");
			
			logger.info("URL "+reqUrl.toString());

			StringBuilder serverResponse = new StringBuilder();
			URL url = new URL(reqUrl.toString());

			connection = (HttpsURLConnection) url.openConnection();
			connection.setRequestProperty("Authorization", "Bearer " + token);
			connection.setRequestMethod("GET");
			connection.setUseCaches(false);
			connection.setDoOutput(true);
//			connection.setDoInput(true);
			InputStream is = connection.getInputStream();
			BufferedReader rd = new BufferedReader(new InputStreamReader(is));
			String line;
			while ((line = rd.readLine()) != null) {
				serverResponse.append(line);
				serverResponse.append('\r');
			}
			rd.close();
			String str = serverResponse.toString();
			logger.info("Cashfree VPA Validation Response " + str);
			return str;

		} catch (Exception e) {
			logger.error("Exception in Cashfree VPA Validation ", e);

			return null;
		} finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
	}

	private String cashFreeTokenResponse() {
		String hostUrl = PropertiesManager.propertiesMap.get("CASHFREE_VPA_VERIFY_TOKEN_GEN");
		String responseData = "";

		String CASHFREE_CLIENT_ID = PropertiesManager.propertiesMap.get("CASHFREE_VPA_VERIFY_CLIEND_ID");
		String CASHFREE_CLIENT_SECRET = PropertiesManager.propertiesMap.get("CASHFREE_VPA_VERIFY_CLIENT_SECRET");
		try {

			logger.info("------url-----" + hostUrl);
			URL url = new URL(hostUrl);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setRequestProperty("X-Client-Id", CASHFREE_CLIENT_ID);
			connection.setRequestProperty("X-Client-Secret", CASHFREE_CLIENT_SECRET);

			connection.setUseCaches(false);
			connection.setDoInput(true);
			connection.setDoOutput(true);

			DataOutputStream requestWriter = new DataOutputStream(connection.getOutputStream());
			requestWriter.writeBytes("");
			requestWriter.close();
			InputStream is = connection.getInputStream();
			BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(is));
			String decodedString;
			while ((decodedString = bufferedreader.readLine()) != null) {
				responseData = responseData + decodedString;
			}
			bufferedreader.close();

		} catch (Exception e) {
			logger.error("Exception in CashFree ", e);
		}
		logger.info("CashFree token response data " + responseData);
		return responseData;
	}

	private void clearFieldsForUPIBeneVerification(Fields fields) {
		logger.info("field clearing for UPI Verification fields " + fields.getFields());
		fields.removeSecureFields();
		fields.remove(FieldType.ACQUIRER_TYPE.getName());
		fields.remove(FieldType.CUST_NAME.getName());
		fields.remove(FieldType.ACQ_ID.getName());
		fields.remove(FieldType.INTERNAL_ORIG_TXN_TYPE.getName());
		fields.remove(FieldType.CURRENCY_CODE.getName());
		fields.remove(FieldType.ADF5.getName());
		fields.remove(FieldType.ADF6.getName());
		fields.remove(FieldType.ADF7.getName());
		fields.remove(FieldType.ADF8.getName());
		fields.remove(FieldType.ADF9.getName());
		fields.remove(FieldType.ADF10.getName());

		logger.info("cleared fields are " + fields.getFields());
	}

	private void handleVpaResponseIdfc(Fields fields, JSONObject responseJson) {
		logger.info("inside handleVpaResponseIdfc()");
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
			logger.info("exception ", e);
		}

	}

	public boolean purposeCheck(Fields fields) {
		logger.info("Inside  purposeCheck()");
		try {
			if (StringUtils.isNotBlank(fields.get(FieldType.PURPOSE.getName()))) {
				PayoutPupose payoutInstance = PayoutPupose.getInstance(fields.get(FieldType.PURPOSE.getName()));
				if (payoutInstance == null) {
					insertImpsUPIBulkDataForPurpose(fields);
					return true;
				}
			}
		} catch (Exception e) {
			logger.error("Exception occured due to invalid purpose : ", e);
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

			field.insertIciciCompositeFields(fields);
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
			logger.error("Exception occured due to invalid purpose : ", e);
		}
	}

}
