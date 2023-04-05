package com.paymentgateway.pg.core.fraudPrevention.core;

import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.user.FraudPrevention;
import com.paymentgateway.commons.user.ResponseObject;
import com.paymentgateway.commons.util.FraudRuleType;
import com.paymentgateway.pg.core.fraudPrevention.model.FraudPreventionDao;

/**
 * @author Harpreet,Rahul
 *
 */

@Service
public class CheckExistingFraudRule {

	@Autowired
	private FraudPreventionDao fraudPreventionDao;

	private static Logger logger = LoggerFactory.getLogger(CheckExistingFraudRule.class.getName());
	
	private ErrorType responseErrorType; // TODO need to review variable scope

	public ResponseObject exists(FraudPrevention fraudPrevention) {
		logger.info("CheckExistingFraudRule  " );
		StringBuilder querySb = new StringBuilder();
		ResponseObject response = new ResponseObject();
		// setting payId according to type of user
		String payId = fraudPrevention.getPayId();
		FraudRuleType fraudType = fraudPrevention.getFraudType();
		switch (fraudType) {
		case BLOCK_NO_OF_TXNS:
			querySb.append("from FraudPrevention where fraudType = '");
			querySb.append(fraudType);
			querySb.append("' and payId = '");
			querySb.append(payId);
			querySb.append("' and status = '");
			querySb.append("ACTIVE");
			querySb.append("'");
			responseErrorType = ErrorType.FRAUD_RULE_ALREADY_EXISTS;
			break;
		case BLOCK_TXN_AMOUNT:
			querySb.append("from FraudPrevention where");
			querySb.append(" payId = '");
			querySb.append(payId);
			querySb.append("'");
			querySb.append(" and currency = '");
			querySb.append(fraudPrevention.getCurrency());
			querySb.append("' and paymentType = '");
			querySb.append(fraudPrevention.getPaymentType());
			querySb.append("' and paymentRegion = '");
			querySb.append(fraudPrevention.getPaymentRegion());
			querySb.append("' and status = '");
			querySb.append("ACTIVE");
			querySb.append("'");
			responseErrorType = ErrorType.FRAUD_RULE_ALREADY_EXISTS;
			break;
		case BLOCK_IP_ADDRESS:
			querySb.append("from FraudPrevention where ipAddress = '");
			querySb.append(fraudPrevention.getIpAddress());
			querySb.append("' and payId = '");
			querySb.append(payId);
			querySb.append("' and status = '");
			querySb.append("ACTIVE");
			querySb.append("'");
			responseErrorType = ErrorType.FRAUD_RULE_ALREADY_EXISTS;
			break;
		case BLOCK_DOMAIN_NAME:
			querySb.append("from FraudPrevention where domainName = '");
			querySb.append(fraudPrevention.getDomainName());
			querySb.append("' and payId = '");
			querySb.append(payId);
			querySb.append("' and status = '");
			querySb.append("ACTIVE");
			querySb.append("'");
			responseErrorType = ErrorType.FRAUD_RULE_ALREADY_EXISTS;
			break;
		case BLOCK_EMAIL_ID:
			querySb.append("from FraudPrevention where email = '");
			querySb.append(fraudPrevention.getEmail());
			querySb.append("' and payId = '");
			querySb.append(payId);
			querySb.append("' and status = '");
			querySb.append("ACTIVE");
			querySb.append("'");
			responseErrorType = ErrorType.FRAUD_RULE_ALREADY_EXISTS;
			break;
		case BLOCK_USER_COUNTRY:
			querySb.append("from FraudPrevention where userCountry = '");
			querySb.append(fraudPrevention.getUserCountry());
			querySb.append("' and payId = '");
			querySb.append(payId);
			querySb.append("' and status = '");
			querySb.append("ACTIVE");
			querySb.append("'");
			responseErrorType = ErrorType.FRAUD_RULE_ALREADY_EXISTS;
			break;
		case BLOCK_CARD_ISSUER_COUNTRY:
			querySb.append("from FraudPrevention where issuerCountry = '");
			querySb.append(fraudPrevention.getIssuerCountry());
			querySb.append("' and payId = '");
			querySb.append(payId);
			querySb.append("' and status = '");
			querySb.append("ACTIVE");
			querySb.append("'");
			responseErrorType = ErrorType.FRAUD_RULE_ALREADY_EXISTS;
			break;
		case BLOCK_CARD_BIN:
			querySb.append("from FraudPrevention where negativeBin = '");
			querySb.append(fraudPrevention.getNegativeBin());
			querySb.append("' and payId = '");
			querySb.append(payId);
			querySb.append("' and status = '");
			querySb.append("ACTIVE");
			querySb.append("'");
			responseErrorType = ErrorType.FRAUD_RULE_ALREADY_EXISTS;
			break;
		case BLOCK_CARD_NO:
			querySb.append("from FraudPrevention where fraudType = '");
			querySb.append(fraudType);
			querySb.append("' and negativeCard = '");
			querySb.append(fraudPrevention.getNegativeCard());
			querySb.append("' and payId = '");
			querySb.append(payId);
			querySb.append("' and status = '");
			querySb.append("ACTIVE");
			querySb.append("'");
			responseErrorType = ErrorType.FRAUD_RULE_ALREADY_EXISTS;
			break;
		case BLOCK_CARD_TXN_THRESHOLD:
			querySb.append("from FraudPrevention where fraudType = '");
			querySb.append(fraudType);
			querySb.append("' and negativeCard = '");
			querySb.append(fraudPrevention.getNegativeCard());
			querySb.append("' and payId = '");
			querySb.append(payId);
			querySb.append("' and status = '");
			querySb.append("ACTIVE");
			querySb.append("'");
			responseErrorType = ErrorType.FRAUD_RULE_ALREADY_EXISTS;
			break;
		case BLOCK_TXN_VELOCITY:
			querySb.append("from FraudPrevention where fraudType = '");
			querySb.append(fraudType);
			querySb.append("' and paymentType = '");
			querySb.append(fraudPrevention.getPaymentType());
			querySb.append("' and paymentRegion = '");
			querySb.append(fraudPrevention.getPaymentRegion());
			querySb.append("' and timePeriod = '");
			querySb.append(fraudPrevention.getTimePeriod());
			querySb.append("' and payId = '");
			querySb.append(payId);
			querySb.append("' and status = '");
			querySb.append("ACTIVE");
			querySb.append("'");
			responseErrorType = ErrorType.FRAUD_RULE_ALREADY_EXISTS;
			break;
			
		case BLOCK_AMOUNT_VELOCITY:
			querySb.append("from FraudPrevention where fraudType = '");
			querySb.append(fraudType);
			querySb.append("' and paymentType = '");
			querySb.append(fraudPrevention.getPaymentType());
			querySb.append("' and paymentRegion = '");
			querySb.append(fraudPrevention.getPaymentRegion());
			querySb.append("' and timePeriod = '");
			querySb.append(fraudPrevention.getTimePeriod());
			querySb.append("' and payId = '");
			querySb.append(payId);
			querySb.append("' and status = '");
			querySb.append("ACTIVE");
			querySb.append("'");
			responseErrorType = ErrorType.FRAUD_RULE_ALREADY_EXISTS;
			break;
			
		case WHITE_LIST_IP_ADDRESS:
			logger.info("CheckExistingFraudRule   WHITE_LIST_IP_ADDRESS" );
			querySb.append("from FraudPrevention where whiteListIpAddress = '");
			querySb.append(fraudPrevention.getWhiteListIpAddress());
			querySb.append("' and payId = '");
			querySb.append(payId);
			querySb.append("' and status = '");
			querySb.append("ACTIVE");
			querySb.append("'");
			responseErrorType = ErrorType.FRAUD_RULE_ALREADY_EXISTS;
			break;
			
		case BLOCK_SALE_AMOUNT_VELOCITY:
			querySb.append("from FraudPrevention where fraudType = '");
			querySb.append(fraudType);
			querySb.append("' and paymentType = '");
			querySb.append(fraudPrevention.getPaymentType());
			querySb.append("' and paymentRegion = '");
			querySb.append(fraudPrevention.getPaymentRegion());
			querySb.append("' and timePeriod = '");
			querySb.append(fraudPrevention.getTimePeriod());
			querySb.append("' and payId = '");
			querySb.append(payId);
			querySb.append("' and status = '");
			querySb.append("ACTIVE");
			querySb.append("'");
			responseErrorType = ErrorType.FRAUD_RULE_ALREADY_EXISTS;
			break;
		case BLOCK_VPA:
			querySb.append("from FraudPrevention where fraudType = '");
			querySb.append(fraudType);
			querySb.append("' and vpaEncrypted = '");
			querySb.append(fraudPrevention.getVpaEncrypted());
			querySb.append("' and payId = '");
			querySb.append(payId);
			querySb.append("' and status = '");
			querySb.append("ACTIVE");
			querySb.append("'");
			responseErrorType = ErrorType.FRAUD_RULE_ALREADY_EXISTS;
			break;
		case BLOCK_VPA_TXN:
			querySb.append("from FraudPrevention where fraudType = '");
			querySb.append(fraudType);
			querySb.append("' and vpaEncrypted = '");
			querySb.append(fraudPrevention.getVpaEncrypted());
			querySb.append("' and payId = '");
			querySb.append(payId);
			querySb.append("' and status = '");
			querySb.append("ACTIVE");
			querySb.append("'");
			responseErrorType = ErrorType.FRAUD_RULE_ALREADY_EXISTS;
			break;
		default:
			logger.error("Something went wrong while checking fraud field values");
			break;
		}
		if (fraudPreventionDao.duplicateChecker(querySb.toString())) {
			logger.info("after CheckExistingFraudRule   duplicateChecker" );
			response.setResponseMessage(responseErrorType.getResponseMessage());
			response.setResponseCode(responseErrorType.getCode());
		} else {
			logger.info(" FRAUD_RULE_NOT_EXIST   duplicateChecker" );
			response.setResponseMessage(ErrorType.FRAUD_RULE_NOT_EXIST.getResponseMessage());
			response.setResponseCode(ErrorType.FRAUD_RULE_NOT_EXIST.getCode());
		}
		return response;
	}
}
