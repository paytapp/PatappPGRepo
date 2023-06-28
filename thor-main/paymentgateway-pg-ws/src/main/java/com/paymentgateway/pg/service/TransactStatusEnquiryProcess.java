package com.paymentgateway.pg.service;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.paymentgateway.commons.dao.FieldsDao;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CrmFieldType;
import com.paymentgateway.commons.util.CrmValidator;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StaticDataProvider;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TransactionType;
import com.paymentgateway.pg.core.pageintegrator.GeneralValidator;
import com.paymentgateway.pg.core.security.Authenticator;
import com.paymentgateway.pg.core.util.ProcessManager;
import com.paymentgateway.pg.core.util.Processor;
import com.paymentgateway.pg.core.util.ResponseCreator;
import com.paymentgateway.pg.history.Historian;
import com.paymentgateway.pg.security.ValidationProcessor;

@RestController
public class TransactStatusEnquiryProcess {

	@Autowired
	private Historian historian;

	@Autowired
	private GeneralValidator generalValidator;

	@Autowired
	private ResponseCreator responseCreator;

	@Autowired
	private Authenticator authenticator;

	@Autowired
	private StaticDataProvider staticDataProvider;

	@Autowired
	private UserDao userDao;

	@Autowired
	@Qualifier("updateProcessor")
	private Processor updateProcessor;

	private static Logger logger = LoggerFactory.getLogger(TransactStatusEnquiryProcess.class.getName());

	@RequestMapping(method = RequestMethod.POST, value = "/transactStatusEnquiry", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Map<String, String> transactStatusEnquiry(@RequestBody Map<String, String> reqmap) {

		try {
			Fields fields = new Fields(reqmap);
			fields.logAllFields("Raw Request:");
			fields.removeInternalFields();
			fields.clean();
			fields.removeExtraFields();
			// To put request blob
			String fieldsAsString = fields.getFieldsAsBlobString();
			fields.put(FieldType.INTERNAL_REQUEST_FIELDS.getName(), fieldsAsString);
			fields.logAllFields("Refine Request:");
			Map<String, String> responseMap = getStatusEnquiryResponse(fields);
			responseMap.remove(FieldType.INTERNAL_CUSTOM_MDC.getName());
			return responseMap;
		} catch (Exception exception) {
			// Ideally this should be a non-reachable code
			logger.error("Exception", exception);
			return null;
		}

	}

	public Map<String, String> getStatusEnquiryResponse(Fields fields) {

		try {
			validate(fields);

			fields.addDefaultFields();

			fields.logAllFields("Refined Request:");

			authenticate(fields);

			validateSuperMerchantDetails(fields);

			validateParentMerchants(fields);

			addPreviousFields(fields);

			validateDupicateOrderId(fields);

			generalValidator.processorValidations(fields);

		} catch (SystemException systemException) {
			fields.setValid(false);
			logger.error("inside the TransactStatusEnquiryProcess in systemExcption in catch block  :",
					systemException);
			String origTxnType = fields.get(FieldType.INTERNAL_ORIG_TXN_TYPE.getName());
			String previousOrigTxnId = fields.get(FieldType.ORIG_TXN_ID.getName());
			if (systemException.getErrorType().getResponseCode()
					.equals(ErrorType.VALIDATION_FAILED.getResponseCode())) {
				String orderId = fields.get(FieldType.ORDER_ID.getName());
				if (StringUtils.isBlank(orderId)
						|| !(new CrmValidator().validateField(CrmFieldType.ORDER_ID, orderId))) {
					// Put order id as 000 if its invalid
					fields.put(FieldType.ORDER_ID.getName(), ErrorType.SUCCESS.getCode());
				}
				if (!StringUtils.isBlank(origTxnType)) {
					fields.put(FieldType.TXNTYPE.getName(), origTxnType);
				} else {
					fields.put(FieldType.ORIG_TXNTYPE.getName(), fields.get(FieldType.TXNTYPE.getName()));
					fields.put(FieldType.TXNTYPE.getName(), TransactionType.INVALID.getName());
				}
				if (!StringUtils.isBlank(fields.get(FieldType.INTERNAL_TXN_CHANNEL.getName()))) {
					fields.put(FieldType.INTERNAL_TXN_CHANNEL.getName(),
							fields.get(FieldType.INTERNAL_TXN_CHANNEL.getName()));
				} else {
					fields.put(FieldType.INTERNAL_TXN_CHANNEL.getName(), TransactionType.INVALID.getName());
				}
				if (!StringUtils.isBlank(systemException.getMessage())) {
					fields.put(FieldType.RESPONSE_MESSAGE.getName(), systemException.getMessage());
					fields.put(FieldType.PG_TXN_MESSAGE.getName(), systemException.getMessage());
				}
			}

			if (fields.get(FieldType.TXNTYPE.getName()).equals(TransactionType.ENROLL.getName())
					&& !StringUtils.isBlank(origTxnType)) {
				fields.put(FieldType.TXNTYPE.getName(), origTxnType);
			}
			if (fields.get(FieldType.TXNTYPE.getName()).equals(TransactionType.NEWORDER.getName())
					&& !StringUtils.isBlank(origTxnType)) {
				fields.put(FieldType.TXNTYPE.getName(), origTxnType);
			}
			fields.put(FieldType.RESPONSE_CODE.getName(), systemException.getErrorType().getCode());
			String txnType = fields.get(FieldType.TXNTYPE.getName());
			if (!txnType.equals(TransactionType.REFUND.getName())) {
				fields.put(FieldType.ORIG_TXN_ID.getName(), fields.get(FieldType.TXN_ID.getName()));
			}
			fields.put(FieldType.PG_REF_NUM.getName(), fields.get(FieldType.TXN_ID.getName()));
			if (systemException.getErrorType().getResponseCode().equals(ErrorType.DENIED_BY_FRAUD.getResponseCode())) {
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), systemException.getErrorType().getResponseMessage());
				fields.put(FieldType.PG_TXN_MESSAGE.getName(), fields.get(Constants.PG_FRAUD_TYPE.getValue()));
			}

			if (systemException.getErrorType().getResponseCode()
					.equals(ErrorType.ACQUIRER_NOT_FOUND.getResponseCode())) {
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), systemException.getErrorType().getResponseMessage());
				fields.put(FieldType.PG_TXN_MESSAGE.getName(), Constants.PG_ACQUIRER_ERROR.getValue());
				fields.put(FieldType.STATUS.getName(), StatusType.FAILED.getName());
				logger.error(
						"inside the TransactStatusEnquiryProcess in systemExcption in catch block in acquirer not found  :",
						systemException);
			}
			if (systemException.getErrorType().getResponseCode().equals(ErrorType.ACUIRER_DOWN.getResponseCode())) {
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), systemException.getErrorType().getResponseMessage());
				fields.put(FieldType.PG_TXN_MESSAGE.getName(), Constants.PG_ACQUIRER_ERROR.getValue());
				fields.put(FieldType.STATUS.getName(), StatusType.FAILED.getName());
				logger.error(
						"inside the TransactStatusEnquiryProcess in systemExcption in catch block in ACUIRER_DOWN :",
						systemException);
			}
			if (systemException.getErrorType().getResponseCode()
					.equals(ErrorType.INTERNAL_SYSTEM_ERROR.getResponseCode())) {
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), systemException.getErrorType().getResponseMessage());
				fields.put(FieldType.PG_TXN_MESSAGE.getName(), Constants.PG_ACQUIRER_ERROR.getValue());
				fields.put(FieldType.STATUS.getName(), StatusType.FAILED.getName());
			}

			if (systemException.getErrorType().getResponseCode()
					.equals(ErrorType.REFUND_AMOUNT_MISMATCH.getResponseCode())) {
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), systemException.getErrorType().getResponseMessage());
				fields.put(FieldType.STATUS.getName(), StatusType.REJECTED.getName());
			}

			if (systemException.getErrorType().getResponseCode().equals(ErrorType.REFUND_DENIED.getResponseCode())) {
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), systemException.getErrorType().getResponseMessage());
				fields.put(FieldType.INTERNAL_ORIG_TXN_TYPE.getName(), TransactionType.REFUND.getName());
				fields.put(FieldType.STATUS.getName(), StatusType.REJECTED.getName());
				fields.put(FieldType.TOTAL_AMOUNT.getName(), fields.get(FieldType.AMOUNT.getName()));
				fields.put(FieldType.ORIG_TXN_ID.getName(), previousOrigTxnId);
			}

			if (systemException.getErrorType().getResponseCode().equals(ErrorType.REFUND_REJECTED.getResponseCode())) {
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), systemException.getErrorType().getResponseMessage());
				fields.put(FieldType.INTERNAL_ORIG_TXN_TYPE.getName(), TransactionType.REFUND.getName());
				fields.put(FieldType.STATUS.getName(), StatusType.REJECTED.getName());
				fields.put(FieldType.TOTAL_AMOUNT.getName(), fields.get(FieldType.AMOUNT.getName()));
				fields.put(FieldType.ORIG_TXN_ID.getName(), previousOrigTxnId);
			}

			if (systemException.getErrorType().getResponseCode()
					.equals(ErrorType.REFUND_FLAG_AMOUNT_NOT_MATCH.getResponseCode())) {
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), systemException.getErrorType().getResponseMessage());
				fields.put(FieldType.INTERNAL_ORIG_TXN_TYPE.getName(), TransactionType.REFUND.getName());
				fields.put(FieldType.STATUS.getName(), StatusType.REJECTED.getName());
				fields.put(FieldType.TOTAL_AMOUNT.getName(), fields.get(FieldType.AMOUNT.getName()));
				fields.put(FieldType.ORIG_TXN_ID.getName(), previousOrigTxnId);
			}

			if (systemException.getErrorType().getResponseCode()
					.equals(ErrorType.TRANSACTION_NOT_FOUND.getResponseCode())) {
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), systemException.getErrorType().getResponseMessage());
				fields.put(FieldType.INTERNAL_ORIG_TXN_TYPE.getName(), TransactionType.REFUND.getName());
				fields.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
			}

			if (systemException.getErrorType().getResponseCode()
					.equals(ErrorType.DUPLICATE_ORDER_ID.getResponseCode())) {
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), systemException.getErrorType().getResponseMessage());
				fields.put(FieldType.RESPONSE_CODE.getName(), systemException.getErrorType().getCode());
				fields.put(FieldType.INTERNAL_ORIG_TXN_TYPE.getName(), TransactionType.SALE.getName());
				fields.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
				fields.put(FieldType.TOTAL_AMOUNT.getName(), fields.get(FieldType.AMOUNT.getName()));
				fields.put(FieldType.ORIG_TXN_ID.getName(), previousOrigTxnId);
			}

			if (systemException.getErrorType().getResponseCode()
					.equals(ErrorType.DUPLICATE_REFUND_ORDER_ID.getResponseCode())) {
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), systemException.getErrorType().getResponseMessage());
				fields.put(FieldType.RESPONSE_CODE.getName(), systemException.getErrorType().getCode());
				fields.put(FieldType.INTERNAL_ORIG_TXN_TYPE.getName(), TransactionType.REFUND.getName());
				fields.put(FieldType.STATUS.getName(), StatusType.INVALID.getName());
				fields.put(FieldType.TOTAL_AMOUNT.getName(), fields.get(FieldType.AMOUNT.getName()));
				fields.put(FieldType.ORIG_TXN_ID.getName(), previousOrigTxnId);
			}

			if (systemException.getErrorType().getResponseCode().equals(ErrorType.DUPLICATE.getResponseCode())) {
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), systemException.getErrorType().getResponseMessage());
				fields.put(FieldType.RESPONSE_CODE.getName(), systemException.getErrorType().getCode());
				fields.put(FieldType.INTERNAL_ORIG_TXN_TYPE.getName(), TransactionType.SALE.getName());
				fields.put(FieldType.STATUS.getName(), StatusType.DUPLICATE.getName());
				fields.put(FieldType.TOTAL_AMOUNT.getName(), fields.get(FieldType.AMOUNT.getName()));
				fields.put(FieldType.ORIG_TXN_ID.getName(), previousOrigTxnId);
			}
			if (systemException.getErrorType().getResponseCode()
					.equals(ErrorType.PERMISSION_DENIED.getResponseCode())) {
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), systemException.getErrorType().getResponseMessage());
				fields.put(FieldType.RESPONSE_CODE.getName(), systemException.getErrorType().getCode());
				fields.put(FieldType.INTERNAL_ORIG_TXN_TYPE.getName(), TransactionType.SALE.getName());
				fields.put(FieldType.TOTAL_AMOUNT.getName(), fields.get(FieldType.AMOUNT.getName()));
			}
			logger.error("SystemException ", systemException);

		} catch (Exception exception) {
			fields.setValid(false);

			fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.UNKNOWN.getCode());
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.UNKNOWN.getResponseMessage());

			logger.error("Exception ", exception);
		}

		ProcessManager.flow(updateProcessor, fields, true);

		createResponse(fields);

		return fields.getFields();
	}

	public void validateDupicateOrderId(Fields fields) throws SystemException {

		String origTxnType = fields.get(FieldType.INTERNAL_ORIG_TXN_TYPE.getName());
		if (StringUtils.isEmpty(origTxnType)) {
			origTxnType = "";
		}

		if (((fields.get(FieldType.TXNTYPE.getName()).equals(TransactionType.NEWORDER.getName())))
				|| ((fields.get(FieldType.TXNTYPE.getName()).equals(TransactionType.REFUND.getName()))
						&& (!origTxnType.equalsIgnoreCase(TransactionType.STATUS.getName())))) {
			historian.validateDuplicateOrderId(fields);
		}
		if (!origTxnType.equalsIgnoreCase(TransactionType.STATUS.getName())) {
			if (((fields.get(FieldType.TXNTYPE.getName()).equals(TransactionType.SALE.getName())))
					|| ((fields.get(FieldType.TXNTYPE.getName()).equals(TransactionType.ENROLL.getName())))) {
				if (StringUtils.isNotBlank(fields.get(FieldType.PG_REF_NUM.getName()))
						&& StringUtils.isNotBlank(fields.get(FieldType.ACQ_ID.getName()))) {
					historian.validateDuplicateOrderId(fields);
				}
			}
		}
	}

	public void validate(Fields fields) throws SystemException {

		Processor validationProcessor = new ValidationProcessor();
		validationProcessor.preProcess(fields);
		validationProcessor.process(fields);
		validationProcessor.postProcess(fields);
	}

	public void authenticate(Fields fields) throws SystemException {
		User user = null;

		// Decide whether to use static usermap or get data from DAO
		if (PropertiesManager.propertiesMap.get(Constants.USE_STATIC_DATA.getValue()) != null
				&& PropertiesManager.propertiesMap.get(Constants.USE_STATIC_DATA.getValue())
						.equalsIgnoreCase(Constants.Y_FLAG.getValue())) {

			user = staticDataProvider.getUserData(fields.get(FieldType.PAY_ID.getName()));
		} else {
			user = userDao.findPayId(fields.get(FieldType.PAY_ID.getName()));

		}

		authenticator.setUser(user);
		authenticator.authenticate(fields);
	}

	public void validateSuperMerchantDetails(Fields fields) throws SystemException {
		String txntype = fields.get(FieldType.TXNTYPE.getName());
		if (txntype.equals(TransactionType.STATUS.getName()) || txntype.equals(TransactionType.REFUND.getName())) {

			User user = null;
			if (PropertiesManager.propertiesMap.get(Constants.USE_STATIC_DATA.getValue()) != null
					&& PropertiesManager.propertiesMap.get(Constants.USE_STATIC_DATA.getValue())
							.equalsIgnoreCase(Constants.Y_FLAG.getValue())) {

				user = staticDataProvider.getUserData(fields.get(FieldType.SUB_MERCHANT_ID.getName()));
			} else {
				user = userDao.findPayId(fields.get(FieldType.PAY_ID.getName()));
			}
			if (user != null && StringUtils.isNotBlank(user.getSuperMerchantId())) {
				fields.put(FieldType.PAY_ID.getName(), user.getSuperMerchantId());
				fields.put(FieldType.SUB_MERCHANT_ID.getName(), user.getPayId());
				fields.put(FieldType.IS_SUB_MERCHANT.getName(), "Y");
			}
		}

	}

	public void validateParentMerchants(Fields fields) throws SystemException {
		String txntype = fields.get(FieldType.TXNTYPE.getName());
		if (txntype.equals(TransactionType.STATUS.getName()) || txntype.equals(TransactionType.REFUND.getName())) {

			User user = null;
			user = userDao.findPayId(fields.get(FieldType.PAY_ID.getName()));

			if (user != null && user.getUserType() == UserType.PARENTMERCHANT) {
				FieldsDao fieldsDao = new FieldsDao();
				Fields newFields = fieldsDao.getPreviousForPgRefNum(fields.get(FieldType.PG_REF_NUM.getName()));
				fields.put(FieldType.PARENT_PAY_ID.getName(), newFields.get(FieldType.PARENT_PAY_ID.getName()));
				fields.put(FieldType.PAY_ID.getName(), newFields.get(FieldType.PAY_ID.getName()));
				fields.put(FieldType.IS_PARENT_MERCHANT.getName(), "Y");
			}
		}

	}

	public void addPreviousFields(Fields fields) throws SystemException {
		// Ideally previous fields are responsibility of history processor,
		// but we are putting it here for smart router
		String txntype = fields.get(FieldType.TXNTYPE.getName());
		if (txntype.equals(TransactionType.STATUS.getName())) {
			historian.findPreviousForStatus(fields);
		} else if (txntype.equals(TransactionType.RECO.getName())
				|| txntype.equals(TransactionType.REFUNDRECO.getName())) {
			historian.findPreviousForReco(fields);
		} else if (txntype.equals(TransactionType.VERIFY.getName())) {
			historian.findPreviousForVerify(fields);
		} else {
			historian.findPrevious(fields);
		}
		historian.populateFieldsFromPrevious(fields);
	}

	public void createResponse(Fields fields) {
		responseCreator.create(fields);
	}
}
