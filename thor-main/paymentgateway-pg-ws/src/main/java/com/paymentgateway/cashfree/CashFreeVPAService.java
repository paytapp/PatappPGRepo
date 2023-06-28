package com.paymentgateway.cashfree;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.api.Hasher;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.CrmFieldType;
import com.paymentgateway.commons.util.CrmValidator;
import com.paymentgateway.commons.util.CustomerIdGenerator;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.pg.core.pageintegrator.GeneralValidator;

/*
*@auther Vishal Yadav
*/

@Service
public class CashFreeVPAService {

	@Autowired
	private GeneralValidator generalValidator;

	@Autowired
	private CrmValidator crmValidator;

	@Autowired
	private CashFreeVPADBEntry cashFreeDBEntry;

	@Autowired
	private UserDao userDao;

	@Autowired
	private CustomerIdGenerator customerIdGenerator;

	private static Logger logger = LoggerFactory.getLogger(CashFreeVPAService.class.getName());
	private static final String prefix = "MONGO_DB_";

	public boolean validateHash(Fields fields) throws SystemException {
		String recievedHash = fields.remove(FieldType.HASH.getName());
		if (StringUtils.isEmpty(recievedHash)) {
			return false;
		}
		String calculateHash = Hasher.getHash(fields);
		if (!calculateHash.equalsIgnoreCase(recievedHash)) {
			StringBuilder hashMessage = new StringBuilder("Recieved hash >>>> ");
			hashMessage.append(recievedHash);
			hashMessage.append(", Calculated Hash >>>> ");
			hashMessage.append(calculateHash);
			logger.error(hashMessage.toString());
			return false;
		}
		return true;

	}

	public Map<String, String> validateFieldsToken(Fields fields) {
		Map<String, String> response = new HashMap<String, String>();
		if (StringUtils.isBlank(fields.get(FieldType.PAY_ID.getName()))
				|| !(crmValidator.validateField(CrmFieldType.PAY_ID, fields.get(FieldType.PAY_ID.getName())))) {
			response.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
			response.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.INVALID_PAYID_ATTEMPT.getResponseMessage());
			response.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_PAYID_ATTEMPT.getResponseCode());
			return response;
		}
		response.put(FieldType.STATUS.getName(), ErrorType.SUCCESS.getResponseMessage());
		response.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.SUCCESS.getResponseMessage());
		response.put(FieldType.RESPONSE_CODE.getName(), ErrorType.SUCCESS.getResponseCode());
		return response;
	}

	public Map<String, String> validateFieldsVirtualAccount(Fields fields) {
		Map<String, String> response = new HashMap<String, String>();
		if (StringUtils.isBlank(fields.get(FieldType.PAY_ID.getName()))
				|| !(crmValidator.validateField(CrmFieldType.PAY_ID, fields.get(FieldType.PAY_ID.getName())))) {
			response.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
			response.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.INVALID_PAYID_ATTEMPT.getResponseMessage());
			response.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_PAYID_ATTEMPT.getResponseCode());
			return response;
		}

		if (StringUtils.isBlank(fields.get(FieldType.CUST_ID.getName()))) {
			response.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
			response.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.REQUIRED_CUST_ID.getResponseMessage());
			response.put(FieldType.RESPONSE_CODE.getName(), ErrorType.REQUIRED_CUST_ID.getResponseCode());
			return response;
		}
//		if (StringUtils.isNotBlank(fields.get(FieldType.CUST_EMAIL.getName()))) {
//			Boolean isEmail = generalValidator.isValidEmailId(fields.get(FieldType.CUST_EMAIL.getName()));
//			if (!isEmail) {
//				response.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
//				response.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.INVALID_EMAILID.getResponseMessage());
//				response.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_EMAILID.getResponseCode());
//				return response;
//			}
//		}
//		if (StringUtils.isBlank(fields.get(FieldType.CUST_NAME.getName()))) {
//			response.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
//			response.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.REQUIRED_EMAIL.getResponseMessage());
//			response.put(FieldType.RESPONSE_CODE.getName(), ErrorType.REQUIRED_EMAIL.getResponseCode());
//			return response;
//		}
//
//		if (StringUtils.isBlank(fields.get(FieldType.CUST_PHONE.getName()))
//				|| !(crmValidator.validateField(CrmFieldType.MOBILE, fields.get(FieldType.CUST_PHONE.getName())))) {
//			response.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
//			response.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.INVALID_PHONE.getResponseMessage());
//			response.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_PHONE.getResponseCode());
//			return response;
//		}
		response.put(FieldType.STATUS.getName(), ErrorType.SUCCESS.getResponseMessage());
		response.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.SUCCESS.getResponseMessage());
		response.put(FieldType.RESPONSE_CODE.getName(), ErrorType.SUCCESS.getResponseCode());
		return response;
	}

	public Map<String, String> validateFieldsVPA(Fields fields) {
		Map<String, String> response = new HashMap<String, String>();
		if (StringUtils.isBlank(fields.get(FieldType.PAY_ID.getName()))
				|| !(crmValidator.validateField(CrmFieldType.PAY_ID, fields.get(FieldType.PAY_ID.getName())))) {
			response.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
			response.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.INVALID_PAYID_ATTEMPT.getResponseMessage());
			response.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_PAYID_ATTEMPT.getResponseCode());
			return response;
		}
		if (StringUtils.isBlank(fields.get(FieldType.VIRTUAL_VPA_NUM.getName()))) {
			response.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
			response.put(FieldType.RESPONSE_MESSAGE.getName(),
					ErrorType.REQUIRED_VIRTUAL_ACCOUNT_NUM.getResponseMessage());
			response.put(FieldType.RESPONSE_CODE.getName(), ErrorType.REQUIRED_VIRTUAL_ACCOUNT_NUM.getResponseCode());
			return response;
		}
		if (StringUtils.isNotBlank(fields.get(FieldType.CUST_EMAIL.getName()))) {
			Boolean isEmail = generalValidator.isValidEmailId(fields.get(FieldType.CUST_EMAIL.getName()));
			if (!isEmail) {
				response.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
				response.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.INVALID_EMAILID.getResponseMessage());
				response.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_EMAILID.getResponseCode());
				return response;
			}
		}
		if (StringUtils.isBlank(fields.get(FieldType.CUST_EMAIL.getName()))) {
			response.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
			response.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.REQUIRED_EMAIL.getResponseMessage());
			response.put(FieldType.RESPONSE_CODE.getName(), ErrorType.REQUIRED_EMAIL.getResponseCode());
			return response;
		}
		if (StringUtils.isBlank(fields.get(FieldType.CUST_NAME.getName()))) {
			response.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
			response.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.REQUIRED_CUST_NAME.getResponseMessage());
			response.put(FieldType.RESPONSE_CODE.getName(), ErrorType.REQUIRED_CUST_NAME.getResponseCode());
			return response;
		}

		if (StringUtils.isBlank(fields.get(FieldType.CUST_PHONE.getName()))
				|| !(crmValidator.validateField(CrmFieldType.MOBILE, fields.get(FieldType.CUST_PHONE.getName())))) {
			response.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
			response.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.INVALID_PHONE.getResponseMessage());
			response.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_PHONE.getResponseCode());
			return response;
		}
		response.put(FieldType.STATUS.getName(), ErrorType.SUCCESS.getResponseMessage());
		response.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.SUCCESS.getResponseMessage());
		response.put(FieldType.RESPONSE_CODE.getName(), ErrorType.SUCCESS.getResponseCode());
		return response;
	}

	public Map<String, String> validateFieldsQRCode(Fields fields) {
		Map<String, String> response = new HashMap<String, String>();
		if (StringUtils.isBlank(fields.get(FieldType.PAY_ID.getName()))
				|| !(crmValidator.validateField(CrmFieldType.PAY_ID, fields.get(FieldType.PAY_ID.getName())))) {
			response.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
			response.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.INVALID_PAYID_ATTEMPT.getResponseMessage());
			response.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_PAYID_ATTEMPT.getResponseCode());
			return response;
		}
		if (StringUtils.isBlank(fields.get(FieldType.VPA.getName()))) {
			response.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
			response.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.INVALID_VPA.getResponseMessage());
			response.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_VPA.getResponseCode());
			return response;
		} else {
			if (!cashFreeDBEntry.qrCodeValidation(fields, null, fields.get(FieldType.VPA.getName()))) {
				response.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
				response.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.INVALID_VPA.getResponseMessage());
				response.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_VPA.getResponseCode());
				return response;
			}

		}
		response.put(FieldType.STATUS.getName(), ErrorType.SUCCESS.getResponseMessage());
		response.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.SUCCESS.getResponseMessage());
		response.put(FieldType.RESPONSE_CODE.getName(), ErrorType.SUCCESS.getResponseCode());
		return response;
	}

	public Map<String, String> validateGenrateCashFreeQRCode(Fields fields) {
		Map<String, String> response = new HashMap<String, String>();
		if (StringUtils.isBlank(fields.get(FieldType.PAY_ID.getName()))
				|| !(crmValidator.validateField(CrmFieldType.PAY_ID, fields.get(FieldType.PAY_ID.getName())))) {
			response.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
			response.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.INVALID_PAYID_ATTEMPT.getResponseMessage());
			response.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_PAYID_ATTEMPT.getResponseCode());
			return response;
		}

		if (StringUtils.isBlank(fields.get(FieldType.CUSTOMER_ACCOUNT_NO.getName()))) {
			response.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
			response.put(FieldType.RESPONSE_MESSAGE.getName(),
					ErrorType.REQUIRED_CUSTOMER_ACCOUNT_NO.getResponseMessage());
			response.put(FieldType.RESPONSE_CODE.getName(), ErrorType.REQUIRED_CUSTOMER_ACCOUNT_NO.getResponseCode());
			return response;
		}
		if (StringUtils.isNotBlank(fields.get(FieldType.CUST_EMAIL.getName()))) {
			Boolean isEmail = generalValidator.isValidEmailId(fields.get(FieldType.CUST_EMAIL.getName()));
			if (!isEmail) {
				response.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
				response.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.INVALID_EMAILID.getResponseMessage());
				response.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_EMAILID.getResponseCode());
				return response;
			}
		}
		if (StringUtils.isBlank(fields.get(FieldType.CUST_EMAIL.getName()))) {

			response.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
			response.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.REQUIRED_EMAIL.getResponseMessage());
			response.put(FieldType.RESPONSE_CODE.getName(), ErrorType.REQUIRED_EMAIL.getResponseCode());
			return response;
		}

		if (StringUtils.isBlank(fields.get(FieldType.CUST_NAME.getName()))) {

			response.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
			response.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.REQUIRED_CUST_NAME.getResponseMessage());
			response.put(FieldType.RESPONSE_CODE.getName(), ErrorType.REQUIRED_CUST_NAME.getResponseCode());
			return response;
		}

		if (StringUtils.isBlank(fields.get(FieldType.CUST_PHONE.getName()))
				|| !(crmValidator.validateField(CrmFieldType.MOBILE, fields.get(FieldType.CUST_PHONE.getName())))) {
			response.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
			response.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.INVALID_PHONE.getResponseMessage());
			response.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_PHONE.getResponseCode());
			return response;
		}
		if (StringUtils.isBlank(fields.get(FieldType.COMPANY_NAME.getName()))) {
			response.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
			response.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.REQUIRED_COMPANY_NAME.getResponseMessage());
			response.put(FieldType.RESPONSE_CODE.getName(), ErrorType.REQUIRED_COMPANY_NAME.getResponseCode());
			return response;
		}

		User user = new User();
		user = userDao.findPayId(fields.get(FieldType.PAY_ID.getName()));
		if (!user.isSuperMerchant() && StringUtils.isBlank(user.getSuperMerchantId())
				&& user.getUserType().equals(UserType.MERCHANT)) {
			String resellerId = userDao.getResellerIdByPayId(fields.get(FieldType.PAY_ID.getName()));
			if (StringUtils.isNotBlank(resellerId)) {
				fields.put(FieldType.RESELLER_ID.getName(), resellerId);
			}
		} else if (!user.isSuperMerchant() && StringUtils.isNotBlank(user.getSuperMerchantId())
				&& user.getUserType().equals(UserType.MERCHANT)) {
			fields.put(FieldType.PAY_ID.getName(), user.getSuperMerchantId());
			fields.put(FieldType.SUB_MERCHANT_ID.getName(), user.getPayId());
			String resellerId = userDao.getResellerIdByPayId(user.getSuperMerchantId());
			if (StringUtils.isNotBlank(resellerId)) {
				fields.put(FieldType.RESELLER_ID.getName(), resellerId);
			}
		} else {
			response.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
			response.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.INVALID_PAYID_ATTEMPT.getResponseMessage());
			response.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_PAYID_ATTEMPT.getResponseCode());
			return response;
		}
		int no = cashFreeDBEntry.cashFreeAccountValidationByAccountNo(fields);
		if (no == 1) {
			fields.put(FieldType.CUST_ID.getName(), customerIdGenerator.getNewCustomerId());
		}
		logger.info("Case no >> " + no + " for CashFree QR Code");
		fields.put(FieldType.SLAB_ID.getName(), no + "");
		response.put(FieldType.STATUS.getName(), ErrorType.SUCCESS.getResponseMessage());
		response.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.SUCCESS.getResponseMessage());
		response.put(FieldType.RESPONSE_CODE.getName(), ErrorType.SUCCESS.getResponseCode());
		return response;
	}

	public Map<String, String> validateGenrateCashFreeDynamicQRCode(Fields fields) {

		Map<String, String> response = new HashMap<String, String>();

		if (StringUtils.isBlank(fields.get(FieldType.PAY_ID.getName()))
				|| !(crmValidator.validateField(CrmFieldType.PAY_ID, fields.get(FieldType.PAY_ID.getName())))) {
			response.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
			response.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.INVALID_PAYID_ATTEMPT.getResponseMessage());
			response.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_PAYID_ATTEMPT.getResponseCode());
			return response;
		}

		if (StringUtils.isBlank(fields.get(FieldType.CUSTOMER_ACCOUNT_NO.getName()))) {
			response.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
			response.put(FieldType.RESPONSE_MESSAGE.getName(),
					ErrorType.REQUIRED_CUSTOMER_ACCOUNT_NO.getResponseMessage());
			response.put(FieldType.RESPONSE_CODE.getName(), ErrorType.REQUIRED_CUSTOMER_ACCOUNT_NO.getResponseCode());
			return response;
		}
		if (StringUtils.isNotBlank(fields.get(FieldType.CUST_EMAIL.getName()))) {
			Boolean isEmail = generalValidator.isValidEmailId(fields.get(FieldType.CUST_EMAIL.getName()));
			if (!isEmail) {
				response.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
				response.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.INVALID_EMAILID.getResponseMessage());
				response.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_EMAILID.getResponseCode());
				return response;
			}
		}
		if (StringUtils.isBlank(fields.get(FieldType.CUST_EMAIL.getName()))) {
			response.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
			response.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.REQUIRED_EMAIL.getResponseMessage());
			response.put(FieldType.RESPONSE_CODE.getName(), ErrorType.REQUIRED_EMAIL.getResponseCode());
			return response;
		}

		if (StringUtils.isBlank(fields.get(FieldType.CUST_NAME.getName()))) {
			response.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
			response.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.REQUIRED_CUST_NAME.getResponseMessage());
			response.put(FieldType.RESPONSE_CODE.getName(), ErrorType.REQUIRED_CUST_NAME.getResponseCode());
			return response;
		}

		if (StringUtils.isBlank(fields.get(FieldType.CUST_PHONE.getName()))
				|| !(crmValidator.validateField(CrmFieldType.MOBILE, fields.get(FieldType.CUST_PHONE.getName())))) {
			response.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
			response.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.INVALID_PHONE.getResponseMessage());
			response.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_PHONE.getResponseCode());
			return response;
		}
		if (StringUtils.isBlank(fields.get(FieldType.COMPANY_NAME.getName()))) {
			response.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
			response.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.REQUIRED_COMPANY_NAME.getResponseMessage());
			response.put(FieldType.RESPONSE_CODE.getName(), ErrorType.REQUIRED_COMPANY_NAME.getResponseCode());
			return response;
		}

		User user = new User();
		user = userDao.findPayId(fields.get(FieldType.PAY_ID.getName()));
		if (!user.isSuperMerchant() && StringUtils.isBlank(user.getSuperMerchantId())
				&& user.getUserType().equals(UserType.MERCHANT)) {
			String resellerId = userDao.getResellerIdByPayId(fields.get(FieldType.PAY_ID.getName()));
			if (StringUtils.isNotBlank(resellerId)) {
				fields.put(FieldType.RESELLER_ID.getName(), resellerId);
			}
		} else if (!user.isSuperMerchant() && StringUtils.isNotBlank(user.getSuperMerchantId())
				&& user.getUserType().equals(UserType.MERCHANT)) {
			fields.put(FieldType.PAY_ID.getName(), user.getSuperMerchantId());
			fields.put(FieldType.SUB_MERCHANT_ID.getName(), user.getPayId());
			String resellerId = userDao.getResellerIdByPayId(user.getSuperMerchantId());
			if (StringUtils.isNotBlank(resellerId)) {
				fields.put(FieldType.RESELLER_ID.getName(), resellerId);
			}
		} else {
			response.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
			response.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.INVALID_PAYID_ATTEMPT.getResponseMessage());
			response.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_PAYID_ATTEMPT.getResponseCode());
			return response;
		}
		int no = cashFreeDBEntry.cashFreeAccountValidationByAccountNo(fields);
		if (no == 1) {
			fields.put(FieldType.CUST_ID.getName(), customerIdGenerator.getNewCustomerId());
		}

		logger.info("Serial no >> " + no);
		fields.put(FieldType.SLAB_ID.getName(), no + "");
		response.put(FieldType.STATUS.getName(), ErrorType.SUCCESS.getResponseMessage());
		response.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.SUCCESS.getResponseMessage());
		response.put(FieldType.RESPONSE_CODE.getName(), ErrorType.SUCCESS.getResponseCode());
		return response;
	}

}
