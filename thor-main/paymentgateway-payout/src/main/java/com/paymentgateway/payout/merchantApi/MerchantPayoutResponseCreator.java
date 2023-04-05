/**
 * 
 */
package com.paymentgateway.payout.merchantApi;

import java.util.HashMap;
import java.util.Map;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.api.Hasher;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.Amount;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.StatusType;

/**
 * @author Amitosh Aanand
 *
 */
@Service
public class MerchantPayoutResponseCreator {

	private static Logger logger = LoggerFactory.getLogger(MerchantPayoutResponseCreator.class.getName());

	public Map<String, String> generateSuccessResponse(Map<String, String> reqmap) {
		logger.info("Generating merchant payout successful request receival response");
		Map<String, String> responseMap = new HashMap<String, String>();

		responseMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.SUCCESS.getCode());
		responseMap.put(FieldType.TXN_ID.getName(), reqmap.get(FieldType.TXN_ID.getName()));
		responseMap.put(FieldType.BENE_NAME.getName(), reqmap.get(FieldType.BENE_NAME.getName()));
		responseMap.put(FieldType.PHONE_NO.getName(), reqmap.get(FieldType.PHONE_NO.getName()));
		// TODO TXNTYPE and CURRENCY_CODE to be picked dynamically instead of hard
		// coding
		responseMap.put(FieldType.TXNTYPE.getName(), "IMPS");
		responseMap.put(FieldType.CURRENCY_CODE.getName(), "356");
		responseMap.put(FieldType.RRN.getName(), null);
		responseMap.put(FieldType.STATUS.getName(), ErrorType.PENDING.getCode());
		responseMap.put("REQUESTED_ON", reqmap.get(FieldType.CREATE_DATE.getName()));
		responseMap.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.PENDING.getResponseMessage());
		responseMap.put(FieldType.PAY_ID.getName(), reqmap.get(FieldType.PAY_ID.getName()));
		responseMap.put(FieldType.ORDER_ID.getName(), reqmap.get(FieldType.ORDER_ID.getName()));
		responseMap.put(FieldType.IFSC_CODE.getName(), reqmap.get(FieldType.IFSC_CODE.getName()));
		responseMap.put(FieldType.BENE_ACCOUNT_NO.getName(), reqmap.get(FieldType.BENE_ACCOUNT_NO.getName()));
		responseMap.put(FieldType.AMOUNT.getName(), Amount.formatAmount(reqmap.get(FieldType.AMOUNT.getName()),
				reqmap.get(FieldType.CURRENCY_CODE.getName())));
		responseMap.put(FieldType.HASH.getName(), generateResponseHash(responseMap));
		return responseMap;
	}

	public Map<String, String> generateFailureResponse(Map<String, String> reqmap) {
		logger.info("Generating merchant payout failure request receival response");
		Map<String, String> responseMap = new HashMap<String, String>();

		responseMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.COMMON_ERROR.getCode());
		responseMap.put(FieldType.TXN_ID.getName(), reqmap.get(FieldType.TXN_ID.getName()));
		responseMap.put(FieldType.BENE_NAME.getName(), reqmap.get(FieldType.BENE_NAME.getName()));
		responseMap.put(FieldType.PHONE_NO.getName(), reqmap.get(FieldType.PHONE_NO.getName()));
		// TODO TXNTYPE and CURRENCY_CODE to be picked dynamically instead of
		// hard-coding
		responseMap.put(FieldType.TXNTYPE.getName(), "IMPS");
		responseMap.put(FieldType.CURRENCY_CODE.getName(), "356");
		responseMap.put(FieldType.RRN.getName(), null);
		responseMap.put(FieldType.STATUS.getName(), ErrorType.COMMON_ERROR.getCode());
		responseMap.put("REQUESTED_ON", reqmap.get(FieldType.CREATE_DATE.getName()));
		responseMap.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.COMMON_ERROR.getResponseMessage());
		responseMap.put(FieldType.PAY_ID.getName(), reqmap.get(FieldType.PAY_ID.getName()));
		responseMap.put(FieldType.ORDER_ID.getName(), reqmap.get(FieldType.ORDER_ID.getName()));
		responseMap.put(FieldType.IFSC_CODE.getName(), reqmap.get(FieldType.IFSC_CODE.getName()));
		responseMap.put(FieldType.BENE_ACCOUNT_NO.getName(), reqmap.get(FieldType.BENE_ACCOUNT_NO.getName()));
		responseMap.put(FieldType.AMOUNT.getName(), Amount.formatAmount(reqmap.get(FieldType.AMOUNT.getName()),
				reqmap.get(FieldType.CURRENCY_CODE.getName())));
		responseMap.put(FieldType.HASH.getName(), generateResponseHash(responseMap));
		return responseMap;
	}

	private String generateResponseHash(Map<String, String> responseMap) {
		try {
			return Hasher.getHash(new Fields(responseMap));
		} catch (SystemException e) {
			logger.error("Exception caugth while generating response hash, " , e);
		}
		return null;
	}

	public Map<String, String> missingParameterResponse(String name) {
		logger.info("Generating merchant payout enquiry missing parameter response");
		Map<String, String> responseMap = new HashMap<String, String>();
		responseMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.COMMON_ERROR.getCode());
		responseMap.put(FieldType.RESPONSE_MESSAGE.getName(), name + " is missing from the request!");
		responseMap.put(FieldType.HASH.getName(), generateResponseHash(responseMap));
		return responseMap;
	}

	public Map<String, String> generateMerchantPayoutEnquiryResponse(Document data) {
		logger.info("Generating merchant payout enquiry response");
		Map<String, String> responseMap = new HashMap<String, String>();
		if (data == null) {
			responseMap.put(FieldType.STATUS.getName(), StatusType.ERROR.getName());
			responseMap.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.COMMON_ERROR.getResponseMessage());
			responseMap.put(FieldType.RESPONSE_CODE.getName(), ErrorType.COMMON_ERROR.getResponseCode());
			return responseMap;
		}

		responseMap.put(FieldType.RESPONSE_CODE.getName(), data.getString(FieldType.RESPONSE_CODE.getName()));
		responseMap.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.SUCCESS.name());
		responseMap.put(FieldType.BENE_NAME.getName(), data.getString(FieldType.BENE_NAME.getName()));
		responseMap.put(FieldType.PHONE_NO.getName(), data.getString(FieldType.PHONE_NO.getName()));
		// TODO TXNTYPE and CURRENCY_CODE to be picked dynamically instead of
		// hard-coding
		responseMap.put(FieldType.TXNTYPE.getName(), "IMPS");
		responseMap.put(FieldType.CURRENCY_CODE.getName(), "356");
		responseMap.put(FieldType.RRN.getName(), null);
		responseMap.put(FieldType.STATUS.getName(), data.getString(FieldType.STATUS.getName()));
		responseMap.put("REQUESTED_ON", data.getString(FieldType.CREATE_DATE.getName()));
		responseMap.put("PROCESSED_ON", data.getString(FieldType.UPDATE_DATE.getName()));
		responseMap.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.PENDING.getResponseMessage());
		responseMap.put(FieldType.CREATE_DATE.getName(), data.getString(FieldType.CREATE_DATE.getName()));
		responseMap.put(FieldType.PAY_ID.getName(), data.getString(FieldType.PAY_ID.getName()));
		responseMap.put(FieldType.ORDER_ID.getName(), data.getString(FieldType.ORDER_ID.getName()));
		responseMap.put(FieldType.IFSC_CODE.getName(), data.getString(FieldType.IFSC_CODE.getName()));
		responseMap.put(FieldType.BENE_ACCOUNT_NO.getName(), data.getString(FieldType.BENE_ACCOUNT_NO.getName()));
		responseMap.put(FieldType.AMOUNT.getName(), data.getString(FieldType.AMOUNT.getName()));
		responseMap.put(FieldType.HASH.getName(), generateResponseHash(responseMap));
		return responseMap;
	}

}
