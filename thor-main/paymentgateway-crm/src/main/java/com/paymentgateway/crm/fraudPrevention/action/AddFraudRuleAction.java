package com.paymentgateway.crm.fraudPrevention.action;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.ModelDriven;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.user.ResponseObject;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CrmFieldConstants;
import com.paymentgateway.commons.util.CrmFieldType;
import com.paymentgateway.commons.util.CrmValidator;
import com.paymentgateway.commons.util.FraudRuleType;
import com.paymentgateway.commons.util.Helper;
import com.paymentgateway.crm.action.AbstractSecureAction;
import com.paymentgateway.crm.actionBeans.SessionUserIdentifier;
import com.paymentgateway.crm.fraudPrevention.actionBeans.FraudRuleCreator;
import com.paymentgateway.pg.core.fraudPrevention.model.FraudRuleModel;

/**
 * @author Harpreet, Rahul
 *
 */
public class AddFraudRuleAction extends AbstractSecureAction implements ModelDriven<FraudRuleModel> {

	@Autowired
	private SessionUserIdentifier sessionUserIdentifier;

	@Autowired
	private FraudRuleCreator ruleCreator;

	@Autowired
	private CrmValidator validator;
	@Autowired
	EditFraudRuleAction editFraudRuleAction;

	private static Logger logger = LoggerFactory.getLogger(AddFraudRuleAction.class.getName());

	private static final long serialVersionUID = 1676422870817349417L;
	private FraudRuleModel fraudRuleModel = new FraudRuleModel();

	@Autowired
	private ResponseObject responseObject;

	@SuppressWarnings("unchecked")
	public String execute() {

		try {
			User sessionUser = (User) sessionMap.get(Constants.USER.getValue());
			String merchantPayId = sessionUserIdentifier.getUserPayId(sessionUser, fraudRuleModel.getPayId());
			logger.info("merchant PayId" + merchantPayId);
			fraudRuleModel.setPayId(merchantPayId);
			responseObject = ruleCreator.createFraudRule(fraudRuleModel, sessionUser);
			fraudRuleModel.setResponseCode(responseObject.getResponseCode());
			fraudRuleModel.setResponseMsg(responseObject.getResponseMessage());
			return SUCCESS;
		} catch (Exception exception) {
			logger.error("Fraud Prevention System - Exception :" , exception);
			return ERROR;
		}
	}

	@Override
	public FraudRuleModel getModel() {
		return fraudRuleModel;
	}

	public void validate() {

		logger.info("enter validation" + "IpAddress");

		if ((validator.validateBlankField(fraudRuleModel.getPayId()))) {
			addFieldError(CrmFieldType.PAY_ID.getName(), validator.getResonseObject().getResponseMessage());
		}
		// custom validation for payId
		else if (!fraudRuleModel.getPayId().equalsIgnoreCase(CrmFieldConstants.ALL.getValue())
				&& !validator.validateField(CrmFieldType.PAY_ID, fraudRuleModel.getPayId())) {
			addFieldError(CrmFieldType.PAY_ID.getName(), validator.getResonseObject().getResponseMessage());
		}
		if (validator.validateBlankField(fraudRuleModel.getFraudType())) {
			addFieldError(null, ErrorType.COMMON_ERROR.getResponseMessage());
		}

		FraudRuleType fraudType = FraudRuleType.getInstance(fraudRuleModel.getFraudType());
		switch (fraudType) {
		case WHITE_LIST_IP_ADDRESS:
			if (validator.validateBlankField(fraudRuleModel.getWhiteListIpAddress())) {
				addFieldError(null, ErrorType.INVALID_FIELD.getResponseMessage());
				break;
			}

			Collection<String> whiteListIpAddressList = Helper.parseFields(fraudRuleModel.getWhiteListIpAddress());
			for (String oneIpAddress : whiteListIpAddressList) {
				if (!validator.validateField(CrmFieldType.FRAUD_IP_ADDRESS, oneIpAddress)) {
					addFieldError(CrmFieldType.FRAUD_IP_ADDRESS.getName(),
							validator.getResonseObject().getResponseMessage());
					break;
				}
			}

			break;
		case BLOCK_IP_ADDRESS:
			if (validator.validateBlankField(fraudRuleModel.getIpAddress())) {
				addFieldError(null, ErrorType.INVALID_FIELD.getResponseMessage());
				break;
			}
			Collection<String> ipAddressList = Helper.parseFields(fraudRuleModel.getIpAddress());
			for (String oneIpAddress : ipAddressList) {
				if (!validator.validateField(CrmFieldType.FRAUD_IP_ADDRESS, oneIpAddress)) {
					addFieldError(CrmFieldType.FRAUD_IP_ADDRESS.getName(),
							validator.getResonseObject().getResponseMessage());
					logger.info("LIST ITERATOR FOR IpAddress" + validator.getResonseObject().getResponseMessage());
					break;
				}
			}

			break;
		case BLOCK_USER_COUNTRY:
			if (validator.validateBlankField(fraudRuleModel.getUserCountry())) {
				addFieldError(null, ErrorType.INVALID_FIELD.getResponseMessage());
				break;
			}
			Collection<String> userCountryList = Helper.parseFields(fraudRuleModel.getUserCountry());
			for (String oneUserCountry : userCountryList) {
				if (!validator.validateField(CrmFieldType.FRAUD_USER_COUNTRY, oneUserCountry)) {
					addFieldError(CrmFieldType.FRAUD_USER_COUNTRY.getName(),
							validator.getResonseObject().getResponseMessage());
					break;
				}
			}
			break;
		case BLOCK_CARD_BIN:
			if (validator.validateBlankField(fraudRuleModel.getNegativeBin())) {
				addFieldError(null, ErrorType.INVALID_FIELD.getResponseMessage());
				break;
			}
			Collection<String> blockCardBinList = Helper.parseFields(fraudRuleModel.getNegativeBin());
			for (String oneCardBin : blockCardBinList) {
				if (!validator.validateField(CrmFieldType.FRAUD_NEGATIVE_BIN, oneCardBin)) {
					addFieldError(CrmFieldType.FRAUD_NEGATIVE_BIN.getName(),
							validator.getResonseObject().getResponseMessage());
					break;
				}
			}

			break;
		case BLOCK_CARD_ISSUER_COUNTRY:
			if (validator.validateBlankField(fraudRuleModel.getIssuerCountry())) {
				addFieldError(null, ErrorType.INVALID_FIELD.getResponseMessage());
				break;
			}
			Collection<String> blockCardIssuerList = Helper.parseFields(fraudRuleModel.getIssuerCountry());
			for (String oneCardIssuer : blockCardIssuerList) {
				if (!validator.validateField(CrmFieldType.FRAUD_ISSUER_COUNTRY, oneCardIssuer)) {
					addFieldError(CrmFieldType.FRAUD_ISSUER_COUNTRY.getName(),
							validator.getResonseObject().getResponseMessage());
					break;
				}
			}

			break;
		case BLOCK_CARD_NO:
			if (validator.validateBlankField(fraudRuleModel.getNegativeCard())) {
				addFieldError(null, ErrorType.INVALID_FIELD.getResponseMessage());
				break;
			}
			Collection<String> blockCardNoList = Helper.parseFields(fraudRuleModel.getNegativeCard());
			for (String oneCardIssuer : blockCardNoList) {
				Integer length = oneCardIssuer.length();
				int minimum = 13;
				Integer max = 19;
				int aa = length.compareTo(minimum);
				int bb = max.compareTo(length);

				if ((aa >= 0) && (bb >= 0)) {
					break;
				} else {
					addFieldError(CrmFieldType.FRAUD_NEGATIVE_CARD.getName(),
							validator.getResonseObject().getResponseMessage());
					break;
				}
			}

			break;
		case BLOCK_EMAIL_ID:
			if (validator.validateBlankField(fraudRuleModel.getEmail())) {
				addFieldError(null, ErrorType.INVALID_FIELD.getResponseMessage());
				break;
			}
			Collection<String> blockEmailIDList = Helper.parseFields(fraudRuleModel.getEmail());
			for (String oneEmailId : blockEmailIDList) {
				if (!validator.isValidEmailId(oneEmailId)) {
					addFieldError(CrmFieldType.FRAUD_EMAIL.getName(),
							validator.getResonseObject().getResponseMessage());
					break;
				}
			}

			break;
		case BLOCK_CARD_TXN_THRESHOLD:
			if (validator.validateBlankField(fraudRuleModel.getPerCardTransactionAllowed())) {
				addFieldError(null, ErrorType.INVALID_FIELD.getResponseMessage());
				break;
			}
			if (!validator.validateField(CrmFieldType.FRAUD_MIN_TRANSACTION_AMOUNT,
					fraudRuleModel.getPerCardTransactionAllowed())) {
				addFieldError(CrmFieldType.FRAUD_MIN_TRANSACTION_AMOUNT.getName(),
						validator.getResonseObject().getResponseMessage());
			}

			break;
		case BLOCK_DOMAIN_NAME:
			if (validator.validateBlankField(fraudRuleModel.getDomainName())) {
				addFieldError(null, ErrorType.INVALID_FIELD.getResponseMessage());
				break;
			}

			Collection<String> blockDomainList = Helper.parseFields(fraudRuleModel.getDomainName());
			for (String oneBlockDomain : blockDomainList) {
				if (!validator.validateField(CrmFieldType.FRAUD_DOMAIN_NAME, oneBlockDomain)) {
					addFieldError(CrmFieldType.FRAUD_DOMAIN_NAME.getName(),
							validator.getResonseObject().getResponseMessage());
					break;
				}

			}
			break;
		case BLOCK_NO_OF_TXNS:

			if (!validator.validateField(CrmFieldType.FRAUD_MINUTE_TXN_LIMIT, fraudRuleModel.getMinutesTxnLimit())) {
				addFieldError(CrmFieldType.FRAUD_MINUTE_TXN_LIMIT.getName(),
						validator.getResonseObject().getResponseMessage());
			}
			break;
		case BLOCK_TXN_AMOUNT:
			if (!validator.validateField(CrmFieldType.FRAUD_CURRENCY, fraudRuleModel.getCurrency())) {
				addFieldError(CrmFieldType.FRAUD_CURRENCY.getName(), validator.getResonseObject().getResponseMessage());
			}
			if (!validator.validateField(CrmFieldType.FRAUD_MIN_TRANSACTION_AMOUNT,
					fraudRuleModel.getMinTransactionAmount())) {
				addFieldError(CrmFieldType.FRAUD_MIN_TRANSACTION_AMOUNT.getName(),
						validator.getResonseObject().getResponseMessage());
			}
			if (!validator.validateField(CrmFieldType.FRAUD_MAX_TRANSACTION_AMOUNT,
					fraudRuleModel.getMaxTransactionAmount())) {
				addFieldError(CrmFieldType.FRAUD_MAX_TRANSACTION_AMOUNT.getName(),
						validator.getResonseObject().getResponseMessage());
			}
			if (!validator.validateField(CrmFieldType.PAYMENT_TYPE, fraudRuleModel.getPaymentType())) {
				addFieldError(CrmFieldType.PAYMENT_TYPE.getName(), validator.getResonseObject().getResponseMessage());
			}
			if (!validator.validateField(CrmFieldType.PAYMENTS_REGION, fraudRuleModel.getPaymentRegion())) {
				addFieldError(CrmFieldType.PAYMENTS_REGION.getName(),
						validator.getResonseObject().getResponseMessage());
			}
			if (!(Float.parseFloat(fraudRuleModel.getMaxTransactionAmount()) >= Float
					.parseFloat(fraudRuleModel.getMinTransactionAmount()))) {
				addFieldError(
						CrmFieldType.FRAUD_MIN_TRANSACTION_AMOUNT.getName() + Constants.COMMA.getValue()
								+ CrmFieldType.FRAUD_MAX_TRANSACTION_AMOUNT.getName(),
						validator.getResonseObject().getResponseMessage());
			}
			break;
		case BLOCK_TXN_VELOCITY:
			if (validator.validateBlankField(fraudRuleModel.getNoOfTransactionAllowed())) {
				addFieldError(null, ErrorType.INVALID_FIELD.getResponseMessage());
				break;
			}
			if (!validator.validateField(CrmFieldType.PAYMENT_TYPE, fraudRuleModel.getPaymentType())) {
				addFieldError(CrmFieldType.PAYMENT_TYPE.getName(), validator.getResonseObject().getResponseMessage());
			}
			if (!validator.validateField(CrmFieldType.PAYMENTS_REGION, fraudRuleModel.getPaymentRegion())) {
				addFieldError(CrmFieldType.PAYMENTS_REGION.getName(),
						validator.getResonseObject().getResponseMessage());
			}
			break;
		case BLOCK_AMOUNT_VELOCITY:
			if (!validator.validateField(CrmFieldType.PAYMENT_TYPE, fraudRuleModel.getPaymentType())) {
				addFieldError(CrmFieldType.PAYMENT_TYPE.getName(), validator.getResonseObject().getResponseMessage());
			}
			if (!validator.validateField(CrmFieldType.PAYMENTS_REGION, fraudRuleModel.getPaymentRegion())) {
				addFieldError(CrmFieldType.PAYMENTS_REGION.getName(),
						validator.getResonseObject().getResponseMessage());
			}
			if (!validator.validateField(CrmFieldType.FRAUD_MAX_TRANSACTION_AMOUNT,
					fraudRuleModel.getAmountAllowed())) {
				addFieldError(CrmFieldType.FRAUD_MAX_TRANSACTION_AMOUNT.getName(),
						validator.getResonseObject().getResponseMessage());
			}
			break;
			
		case BLOCK_SALE_AMOUNT_VELOCITY:
			if (!validator.validateField(CrmFieldType.PAYMENT_TYPE, fraudRuleModel.getPaymentType())) {
				addFieldError(CrmFieldType.PAYMENT_TYPE.getName(), validator.getResonseObject().getResponseMessage());
			}
			if (!validator.validateField(CrmFieldType.PAYMENTS_REGION, fraudRuleModel.getPaymentRegion())) {
				addFieldError(CrmFieldType.PAYMENTS_REGION.getName(),
						validator.getResonseObject().getResponseMessage());
			}
			if (!validator.validateField(CrmFieldType.FRAUD_MAX_TRANSACTION_AMOUNT,
					fraudRuleModel.getAmountAllowed())) {
				addFieldError(CrmFieldType.FRAUD_MAX_TRANSACTION_AMOUNT.getName(),
						validator.getResonseObject().getResponseMessage());
			}
			break;
		case BLOCK_VPA:
			if (validator.validateBlankField(fraudRuleModel.getVpa())) {
				addFieldError(null, ErrorType.INVALID_FIELD.getResponseMessage());
				break;
			}

			break;
		case BLOCK_VPA_TXN:
			if (validator.validateBlankField(fraudRuleModel.getVpa())) {
				addFieldError(null, ErrorType.INVALID_FIELD.getResponseMessage());
				break;
			}
			if (validator.validateBlankField(fraudRuleModel.getVpaTotalTransactionAllowed())) {
				addFieldError(null, ErrorType.INVALID_FIELD.getResponseMessage());
				break;
			}
			if (!validator.validateField(CrmFieldType.FRAUD_MIN_TRANSACTION_AMOUNT,
					fraudRuleModel.getVpaTotalTransactionAllowed())) {
				addFieldError(CrmFieldType.FRAUD_MIN_TRANSACTION_AMOUNT.getName(),
						validator.getResonseObject().getResponseMessage());
			}

			break;

		}
	}
}