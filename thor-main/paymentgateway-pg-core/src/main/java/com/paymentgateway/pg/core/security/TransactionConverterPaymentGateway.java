package com.paymentgateway.pg.core.security;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.api.Hasher;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.Amount;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PaymentType;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.pg.core.util.ConstantsPaymentGateway;

@Service("paymentGatewayTransactionConverter")
public class TransactionConverterPaymentGateway {

	public StringBuilder mapChecksum(StringBuilder requestfields) throws SystemException {
		String checksum = Hasher.getHash(requestfields.toString());

		requestfields.append("|");
		requestfields.append(ConstantsPaymentGateway.CHECKSUM).append(ConstantsPaymentGateway.EQUATOR).append(checksum);

		return requestfields;
	}

	public StringBuilder mapSaleFields(Fields fields) {
		StringBuilder request = new StringBuilder();

		String currencyCode = fields.get(FieldType.CURRENCY_CODE.getName());
		String amount = Amount.toDecimal(fields.get(FieldType.AMOUNT.getName()), currencyCode);
		String status = fields.get(FieldType.STATUS.getName());
		String responseCode = fields.get(FieldType.RESPONSE_CODE.getName());
		if ((status.equals(StatusType.CAPTURED.getName()) && (responseCode.equals(ErrorType.SUCCESS.getCode())))) {
			status = ConstantsPaymentGateway.SUCCESS;
		} else {
			status = ConstantsPaymentGateway.FAILURE;
		}

		String statusDesc = fields.get(FieldType.PG_TXN_MESSAGE.getName());
		if (StringUtils.isBlank(statusDesc)) {
			statusDesc = fields.get(FieldType.RESPONSE_MESSAGE.getName());
		}

		String countryCode = fields.get(FieldType.CARD_ISSUER_COUNTRY.getName());
		if (StringUtils.isBlank(countryCode)) {
			countryCode = "";
		}

		String bankName = fields.get(FieldType.CARD_ISSUER_BANK.getName());
		if (StringUtils.isBlank(bankName)) {
			bankName = "";
		}

		String binNo = fields.get(FieldType.CARD_MASK.getName());
		if (binNo != null && StringUtils.isNotBlank(binNo)) {
			binNo = binNo.substring(0, 6);
		} else {
			binNo = "";
		}
		
		String paymentMode = fields.get(FieldType.PAYMENT_TYPE.getName());
		if (StringUtils.isBlank(paymentMode)) {
			paymentMode = "";
		} else if (paymentMode.equals(PaymentType.UPI.getCode())) {
			paymentMode = PaymentType.UPI.getName();
		}
		
		request.append(ConstantsPaymentGateway.MERCHANT_CODE).append(ConstantsPaymentGateway.EQUATOR)
				.append(fields.get(FieldType.PAY_ID.getName()));
		request.append("|");
		request.append(ConstantsPaymentGateway.RESERVATION_ID).append(ConstantsPaymentGateway.EQUATOR)
				.append(fields.get(FieldType.ORDER_ID.getName()));
		request.append("|");
		request.append(ConstantsPaymentGateway.BANK_TXN_ID).append(ConstantsPaymentGateway.EQUATOR)
				.append(fields.get(FieldType.PG_REF_NUM.getName()));
		request.append("|");
		request.append(ConstantsPaymentGateway.TXN_AMOUNT).append(ConstantsPaymentGateway.EQUATOR).append(amount);
		request.append("|");
		request.append(ConstantsPaymentGateway.STATUS).append(ConstantsPaymentGateway.EQUATOR).append(status);
		request.append("|");
		request.append(ConstantsPaymentGateway.STATUS_DESC).append(ConstantsPaymentGateway.EQUATOR).append(statusDesc);
		request.append("|");
		request.append(ConstantsPaymentGateway.PAYMENT_MODE).append(ConstantsPaymentGateway.EQUATOR)
				.append(paymentMode);
		request.append("|");
		request.append(ConstantsPaymentGateway.BANK_NAME).append(ConstantsPaymentGateway.EQUATOR).append(bankName);
		request.append("|");
		request.append(ConstantsPaymentGateway.COUNTRY_CODE).append(ConstantsPaymentGateway.EQUATOR).append(countryCode);
		request.append("|");
		request.append(ConstantsPaymentGateway.BINNO).append(ConstantsPaymentGateway.EQUATOR).append(binNo);

		return request;
	}

	public StringBuilder mapDoubleVerificationFields(Fields fields) {
		String statusDesc = fields.get(FieldType.PG_TXN_MESSAGE.getName());
		if (statusDesc != null && StringUtils.isNotBlank(statusDesc)) {
		} else {
			statusDesc = fields.get(FieldType.RESPONSE_MESSAGE.getName());
		}
		String status = fields.get(FieldType.STATUS.getName());
		String responseCode = fields.get(FieldType.RESPONSE_CODE.getName());
		if ((status.equals(StatusType.CAPTURED.getName()) && (responseCode.equals(ErrorType.SUCCESS.getCode())))) {
			status = ConstantsPaymentGateway.SUCCESS;
		} else {
			status = ConstantsPaymentGateway.FAILURE;
			statusDesc = ErrorType.NO_SUCH_TRANSACTION.getResponseMessage();
		}

		
		StringBuilder response = new StringBuilder();
		String currencyCode = fields.get(FieldType.CURRENCY_CODE.getName());
		String amount = Amount.toDecimal(fields.get(FieldType.AMOUNT.getName()), currencyCode);

		response.append(ConstantsPaymentGateway.MERCHANT_CODE).append(ConstantsPaymentGateway.EQUATOR)
				.append(fields.get(FieldType.PAY_ID.getName()));
		response.append("|");
		response.append(ConstantsPaymentGateway.RESERVATION_ID).append(ConstantsPaymentGateway.EQUATOR)
				.append(fields.get(FieldType.ORDER_ID.getName()));
		response.append("|");
		response.append(ConstantsPaymentGateway.BANK_TXN_ID).append(ConstantsPaymentGateway.EQUATOR)
				.append(fields.get(FieldType.PG_REF_NUM.getName()));
		response.append("|");
		response.append(ConstantsPaymentGateway.TXN_AMOUNT).append(ConstantsPaymentGateway.EQUATOR).append(amount);
		response.append("|");
		response.append(ConstantsPaymentGateway.STATUS).append(ConstantsPaymentGateway.EQUATOR).append(status);
		response.append("|");
		response.append(ConstantsPaymentGateway.STATUS_DESC).append(ConstantsPaymentGateway.EQUATOR).append(statusDesc);

		return response;
	}
	
}
