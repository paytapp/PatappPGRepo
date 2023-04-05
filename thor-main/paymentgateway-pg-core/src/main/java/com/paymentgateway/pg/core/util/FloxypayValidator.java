package com.paymentgateway.pg.core.util;

import java.util.Collection;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PaymentType;
import com.paymentgateway.commons.util.SystemProperties;
import com.paymentgateway.commons.util.TransactionType;
import com.paymentgateway.commons.util.Validator;

@Service
final class FloxypayValidator implements Validator {

	@Override
	public void validate(Fields fields) throws SystemException {

		switch (TransactionType.getInstance(fields.get(FieldType.TXNTYPE.getName()))) {
		case SALE:
		case ENROLL:
			validateSaleTxn(fields);
			break;
		default:
			throw new SystemException(ErrorType.VALIDATION_FAILED, "Invalid TXNTYPE in Validator");
		}

	}// validate()

	public void validateSaleTxn(Fields fields) throws SystemException {

		if (StringUtils.isNotBlank(fields.get(FieldType.PAYMENT_TYPE.getName()))) {

			switch (PaymentType.getInstanceUsingCode(fields.get(FieldType.PAYMENT_TYPE.getName()))) {

			case CREDIT_CARD:
			case DEBIT_CARD:
			//	validateMandatoryFields(fields, SystemProperties.getRazorpayCardmandatoryfields());
				break;

			case WALLET:
			case NET_BANKING:
			//	validateMandatoryFields(fields, SystemProperties.getRazorpayNbWlmandatoryfields());
				break;

			case UPI:
			//	validateMandatoryFields(fields, SystemProperties.getRazorpayUpimandatoryfields());
				break;
			default:
				throw new SystemException(ErrorType.VALIDATION_FAILED, "Invalid PaymentType in Validator");

			}
		}
	}

	public void validateMandatoryFields(Fields fields, Collection<String> mandatoryEnrollmentFields)
			throws SystemException {

		for (String key : mandatoryEnrollmentFields) {

			if (StringUtils.isBlank(fields.get(key))) {
				throw new SystemException(ErrorType.VALIDATION_FAILED, key + " is a required field");
			}
		}

	}

}
