/**
 * 
 */
package com.paymentgateway.payout.merchantApi;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.api.Hasher;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TransactionManager;

/**
 * @author Amitosh Aanand
 *
 */
@Service
public class MerchantPayoutHandler {

	@Autowired
	private MerchantPayoutDao merchantPayoutDao;

	@Autowired
	private MerchantPayoutResponseCreator merchantPayoutResponseCreator;
	
	@Autowired
	private UserDao userDao;

	private static Logger logger = LoggerFactory.getLogger(MerchantPayoutHandler.class.getName());

	public boolean validateHashForApi(Fields fields) throws SystemException {
		String merchantHash = fields.remove(FieldType.HASH.getName());
		logger.info("Hash from merchant :" + merchantHash);
		String hash = Hasher.getHash(fields);
		logger.info("Hash :" + hash);
		if (!hash.equals(merchantHash)) {
			logger.info("OrderId: "+fields.get(FieldType.ORDER_ID.getName())+" Calculated hash : " + hash+" Merchant hash "+merchantHash);
			return false;
		}
		return true;
	}

	public Map<String, String> processMerchantPayout(Map<String, String> reqmap) {
		logger.info("Inserting merchant payout request into DB");
		String id = TransactionManager.getNewTransactionId();
		String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
		reqmap.put(FieldType.CREATE_DATE.getName(), date);
		reqmap.put(FieldType.UPDATE_DATE.getName(), date);
		reqmap.put(FieldType.TXN_ID.getName(), id);
		if (merchantPayoutDao.insertPayoutRequestIntoDB(reqmap)) {
			return merchantPayoutResponseCreator.generateSuccessResponse(reqmap);
		} else {
			return merchantPayoutResponseCreator.generateFailureResponse(reqmap);
		}
	}

	public Map<String, String> merchantPayoutEnquiry(Map<String, String> reqmap) {
		if (!reqmap.containsKey(FieldType.PAY_ID.getName())) {
			logger.info("PAY_ID was missing from merchant payout status enquiry request");
			return merchantPayoutResponseCreator.missingParameterResponse(FieldType.PAY_ID.getName());
		}
		if (!reqmap.containsKey(FieldType.ORDER_ID.getName())) {
			logger.info("ORDER_IDPay ID was missing from merchant payout status enquiry request");
			return merchantPayoutResponseCreator.missingParameterResponse(FieldType.ORDER_ID.getName());
		}
		if (!reqmap.containsKey(FieldType.AMOUNT.getName())) {
			logger.info("AMOUNT was missing from merchant payout status enquiry request");
			return merchantPayoutResponseCreator.missingParameterResponse(FieldType.AMOUNT.getName());
		}
		if (!reqmap.containsKey(FieldType.CURRENCY_CODE.getName())) {
			logger.info("CURRENCY_CODE was missing from merchant payout status enquiry request");
			return merchantPayoutResponseCreator.missingParameterResponse(FieldType.CURRENCY_CODE.getName());
		}

		Document data = merchantPayoutDao.merchantPayoutEnquiry(reqmap);

		return merchantPayoutResponseCreator.generateMerchantPayoutEnquiryResponse(data);

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
		if (StringUtils.isBlank(fields.get(FieldType.HASH.getName()))) {
			fields.put(FieldType.STATUS.getName(), StatusType.ERROR.getName());
			fields.put(FieldType.PG_TXN_MESSAGE.getName(), ErrorType.VALIDATION_FAILED.getResponseMessage());
			fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.VALIDATION_FAILED.getResponseCode());
			return true;
		}
		
		return false;

	}
	
	public void checkDuplicateOrderId(Fields fields) throws SystemException {
		merchantPayoutDao.checkDuplicateOrderId(fields);
	        
	}
}
