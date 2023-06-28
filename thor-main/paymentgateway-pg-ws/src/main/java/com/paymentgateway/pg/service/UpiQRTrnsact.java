package com.paymentgateway.pg.service;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.paymentgateway.commons.api.Hasher;
import com.paymentgateway.commons.dao.PaymentOptionsDao;
import com.paymentgateway.commons.dao.UserSettingDao;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.user.AccountCurrencyRegion;
import com.paymentgateway.commons.user.CardHolderType;
import com.paymentgateway.commons.user.PaymentOptions;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserSettingData;
import com.paymentgateway.commons.util.AcquirerType;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.MopType;
import com.paymentgateway.commons.util.PaymentType;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TransactionManager;
import com.paymentgateway.commons.util.TransactionType;
import com.paymentgateway.commons.util.UserStatusType;
import com.paymentgateway.commons.util.Validator;
import com.paymentgateway.commons.util.onUsOffUs;
import com.paymentgateway.iciciUpi.IciciUpiQrIntegrator;
import com.paymentgateway.pg.core.util.TransactionResponser;
import com.paymentgateway.pg.security.SecurityProcessor;

@RestController
public class UpiQRTrnsact {

	@Autowired
	private IciciUpiQrIntegrator iciciUpiQrIntegrator;

	@Autowired
	private SecurityProcessor securityProcessor;

	@Autowired
	private Fields field;

	@Autowired
	TransactionResponser transactionResponser;

	@Autowired
	private UserDao userDao;

	@Autowired
	private PaymentOptionsDao paymentOptionsDao;
	
	@Autowired
	private Validator generalValidator;
	
	@Autowired
	private UserSettingDao userSettingDao;


	private static Logger logger = LoggerFactory.getLogger(UpiQRTrnsact.class.getName());

	@RequestMapping(method = RequestMethod.POST, value = "/upiQRProcessor", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Map<String, String> upiQRProcessor(@RequestBody Map<String, String> reqmap) {

		try {
			Fields fields = new Fields(reqmap);
			fields.logAllFields("UPI QR Request:");
			fields.clean();
			//generalValidator.validate(fields);
			boolean hashResult = validateHash(fields);
			String txnId = TransactionManager.getNewTransactionId();
			fields.put(FieldType.TXN_ID.getName(), txnId);
			fields.put(FieldType.PG_REF_NUM.getName(), txnId);
			fields.put(FieldType.TXNTYPE.getName(), TransactionType.SALE.getCode());
			if (!hashResult) {
				fields.put(FieldType.STATUS.getName(), StatusType.AUTHENTICATION_FAILED.getName());
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.AUTHENTICATION_FAILED.getResponseMessage());
				fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.AUTHENTICATION_FAILED.getResponseCode());
				field.insertUpiQRRequest(fields);
				transactionResponser.addHash(fields);
				return fields.getFields();
			}
			User user = userDao.findPayId(fields.get(FieldType.PAY_ID.getName()));
			UserSettingData userSetting = userSettingDao.fetchDataUsingPayId((fields.get(FieldType.PAY_ID.getName())));
			if (user.getUserStatus() != UserStatusType.ACTIVE) {
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.USER_INACTIVE.getResponseMessage());
				fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.USER_INACTIVE.getCode());
				fields.put(FieldType.STATUS.getName(), StatusType.REJECTED.getName());
				transactionResponser.addHash(fields);
				return fields.getFields();
			}
			if(!userSetting.isAllowUpiQRFlag()) {
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.REJECTED_BY_PG.getResponseMessage());
				fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.REJECTED_BY_PG.getCode());
				fields.put(FieldType.STATUS.getName(), StatusType.REJECTED.getName());
				transactionResponser.addHash(fields);
				return fields.getFields();
			}
			PaymentOptions paymentOption = paymentOptionsDao.getPaymentOption(user.getPayId());

			if (paymentOption == null) {
				fields.put(FieldType.STATUS.getName(), StatusType.FAILED.getName());
				fields.put(FieldType.RESPONSE_MESSAGE.getName(),
						ErrorType.PAYMENT_OPTIONS_NOT_CONFIGURED.getResponseMessage());
				fields.put(FieldType.RESPONSE_CODE.getName(),
						ErrorType.PAYMENT_OPTIONS_NOT_CONFIGURED.getResponseCode());
				transactionResponser.addHash(fields);
				return fields.getFields();
			} else {
				if ((!paymentOption.isUpiQr())) {
					fields.put(FieldType.STATUS.getName(), StatusType.FAILED.getName());
					fields.put(FieldType.RESPONSE_MESSAGE.getName(),
							ErrorType.PAYMENT_OPTIONS_NOT_CONFIGURED.getResponseMessage());
					fields.put(FieldType.RESPONSE_CODE.getName(),
							ErrorType.PAYMENT_OPTIONS_NOT_CONFIGURED.getResponseCode());
					transactionResponser.addHash(fields);
					return fields.getFields();
				}
			}
			boolean duplicateCheck = field.validateDuplicateUpiQRRequest(fields.get(FieldType.ORDER_ID.getName()));
			if (duplicateCheck) {
				fields.put(FieldType.STATUS.getName(), StatusType.DUPLICATE.getName());
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.DUPLICATE.getResponseMessage());
				fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.DUPLICATE.getResponseCode());
				transactionResponser.addHash(fields);
				return fields.getFields();
			}
			fields.put(FieldType.INTERNAL_ORIG_TXN_ID.getName(), txnId);
			fields.put(FieldType.ACQUIRER_TYPE.getName(), AcquirerType.ICICIUPI.getCode());
			fields.put(FieldType.CURRENCY_CODE.getName(), "356");

			securityProcessor.addAcquirerFields(fields);
			iciciUpiQrIntegrator.process(fields);
			fields.removeInternalFields();
			fields.removeSecureFields();
			fields.remove(FieldType.MERCHANT_ID.getName());

			field.insertUpiQRRequest(fields);
			Fields dbFields = new Fields(fields);
			dbFields.put(FieldType.SURCHARGE_FLAG.getName(), ((userSetting.isSurchargeFlag()) ? "Y" : "N"));
			dbFields.put(FieldType.TRANSACTION_MODE.getName(), "Direct");
			dbFields.put(FieldType.PAYMENTS_REGION.getName(), AccountCurrencyRegion.DOMESTIC.name());
			dbFields.put(FieldType.CARD_HOLDER_TYPE.getName(), CardHolderType.CONSUMER.name());
			dbFields.put(FieldType.TOTAL_AMOUNT.getName(), fields.get(FieldType.AMOUNT.getName()));
			dbFields.put(FieldType.ORIG_TXN_ID.getName(), fields.get(FieldType.TXN_ID.getName()));
			dbFields.put(FieldType.ORIG_TXNTYPE.getName(), fields.get(FieldType.TXNTYPE.getName()));
			dbFields.put(FieldType.INTERNAL_ORIG_TXN_TYPE.getName(), fields.get(FieldType.TXNTYPE.getName()));
			dbFields.put(FieldType.ACQUIRER_MODE.getName(), onUsOffUs.OFF_US.name());
			dbFields.put(FieldType.MOP_TYPE.getName(), MopType.UPI_QR.getCode());
			dbFields.put(FieldType.PAYMENT_TYPE.getName(), PaymentType.UPI.getCode());
			dbFields.put(FieldType.STATUS.getName(), StatusType.SENT_TO_BANK.getName());
			dbFields.remove(FieldType.UPI_QR_CODE.getName());
			field.insert(dbFields);
			fields.remove(FieldType.ACQUIRER_TYPE.getName());
			fields.remove(FieldType.TXNTYPE.getName());
			transactionResponser.addHash(fields);
			return fields.getFields();
		} catch (Exception exception) {
			// Ideally this should be a non-reachable code
			logger.error("Exception", exception);
			Fields res = new Fields();
			res.put(FieldType.RESPONSE_MESSAGE.getName(), exception.getMessage());
			res.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_PAYID_ATTEMPT.getCode());
			return res.getFields();
		}

	}

	public boolean validateHash(Fields fields) throws SystemException {
		String merchantHash = fields.remove(FieldType.HASH.getName());
		if (StringUtils.isEmpty(merchantHash)) {
			return false;
		}
		String calculateHash = Hasher.getHash(fields);
		if (!calculateHash.equalsIgnoreCase(merchantHash)) {
			StringBuilder hashMessage = new StringBuilder("Merchant hash =");
			hashMessage.append(merchantHash);
			hashMessage.append(", Calculated Hash=");
			hashMessage.append(calculateHash);
			logger.error(hashMessage.toString());
			return false;
		}
		return true;

	}

}
