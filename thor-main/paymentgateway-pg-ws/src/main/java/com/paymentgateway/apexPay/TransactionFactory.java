package com.paymentgateway.apexPay;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.api.Hasher;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PaymentType;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.TransactionManager;
import com.paymentgateway.commons.util.TransactionType;

@Service("apexPayFactory")
public class TransactionFactory {

	private static Logger logger = LoggerFactory.getLogger(TransactionFactory.class.getName());

	@SuppressWarnings("incomplete-switch")
	public Transaction getInstance(Fields fields) {

		Transaction transaction = new Transaction();
		switch (TransactionType.getInstance(fields.get(FieldType.TXNTYPE.getName()))) {
		case AUTHORISE:
			break;
		case ENROLL:
			fields.put(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName());
			fields.put(FieldType.PG_REF_NUM.getName(), fields.get(FieldType.TXN_ID.getName()));
			fields.put(FieldType.ORIG_TXN_ID.getName(), fields.get(FieldType.TXN_ID.getName()));
			setEnrollment(fields, transaction);
			break;
		case REFUND:

			fields.put(FieldType.TOTAL_AMOUNT.getName(), fields.get(FieldType.AMOUNT.getName()));
			setRefund(fields, transaction);
			fields.put(FieldType.PG_REF_NUM.getName(), fields.get(FieldType.TXN_ID.getName()));

			break;
		case SALE:
			fields.put(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName());
			fields.put(FieldType.PG_REF_NUM.getName(), fields.get(FieldType.TXN_ID.getName()));
			fields.put(FieldType.ORIG_TXN_ID.getName(), fields.get(FieldType.TXN_ID.getName()));
			setEnrollment(fields, transaction);
			break;
		case CAPTURE:
			fields.put(FieldType.PG_REF_NUM.getName(), fields.get(FieldType.ORIG_TXN_ID.getName()));
			break;
		case STATUS:
			setStatusEnquiry(fields, transaction);
			break;
		}

		return transaction;
	}

	public void setEnrollment(Fields fields, Transaction transaction) {

		try {
			String returnUrl = PropertiesManager.propertiesMap.get(Constants.APEXPAY_RESPONSE_URL);

			transaction.setPayId(fields.get(FieldType.MERCHANT_ID.getName()));
			transaction.setSaltKey(fields.get(FieldType.PASSWORD.getName()));
			transaction.setEncKey(fields.get(FieldType.TXN_KEY.getName()));
			transaction.setAmount(fields.get(FieldType.TOTAL_AMOUNT.getName()));

			transaction.setOrderId(fields.get(FieldType.PG_REF_NUM.getName()));
			transaction.setCurrencyCode(fields.get(FieldType.CURRENCY_CODE.getName()));

			if (StringUtils.isNotBlank(fields.get(FieldType.PAYMENT_TYPE.getName()))
					&& (fields.get(FieldType.PAYMENT_TYPE.getName()).equalsIgnoreCase(PaymentType.CREDIT_CARD.getCode())
							|| fields.get(FieldType.PAYMENT_TYPE.getName())
									.equalsIgnoreCase(PaymentType.DEBIT_CARD.getCode()))) {

				transaction.setCardNumber(fields.get(FieldType.CARD_NUMBER.getName()));
				transaction.setExpiryDate(fields.get(FieldType.CARD_EXP_DT.getName()));
				transaction.setCavv(fields.get(FieldType.CVV.getName()));
			}

			if (StringUtils.isNotBlank(fields.get(FieldType.CUST_NAME.getName()))) {
				transaction.setCustName(fields.get(FieldType.CUST_NAME.getName()));
			}

			if (StringUtils.isNotBlank(fields.get(FieldType.CARD_HOLDER_NAME.getName()))) {
				transaction.setCardHolderName(fields.get(FieldType.CARD_HOLDER_NAME.getName()));
			} else {
				transaction.setCardHolderName("PayUser");
			}

			if (StringUtils.isNotBlank(fields.get(FieldType.CUST_EMAIL.getName()))) {
				transaction.setCustEmail(fields.get(FieldType.CUST_EMAIL.getName()));
			}

			if (StringUtils.isNotBlank(fields.get(FieldType.CUST_PHONE.getName()))) {
				transaction.setCustPhone(fields.get(FieldType.CUST_PHONE.getName()));
			}

			transaction.setPaymentType(fields.get(FieldType.PAYMENT_TYPE.getName()));
			transaction.setMopType(fields.get(FieldType.MOP_TYPE.getName()));
			// transaction.setMopType("1005");
			transaction.setRedirectUrl(returnUrl + fields.get(FieldType.PG_REF_NUM.getName()));

			if (StringUtils.isNotBlank(fields.get(FieldType.PAYMENT_TYPE.getName()))
					&& fields.get(FieldType.PAYMENT_TYPE.getName()).equalsIgnoreCase(PaymentType.UPI.getCode())) {
				transaction.setPayerAddress(fields.get(FieldType.PAYER_ADDRESS.getName()));
			}
		}

		catch (Exception e) {
			logger.error("Exception", e);
		}

	}

	public void setStatusEnquiry(Fields fields, Transaction transaction) {

		try {

			transaction.setPayId(fields.get(FieldType.MERCHANT_ID.getName()));
			transaction.setOrderId(fields.get(FieldType.PG_REF_NUM.getName()));
			transaction.setTxnType("STATUS");
			transaction.setAmount(fields.get(FieldType.TOTAL_AMOUNT.getName()));
			transaction.setCurrencyCode("356");

			transaction.setSaltKey(fields.get(FieldType.PASSWORD.getName()));

			Fields hashFields = new Fields();

			hashFields.put(FieldType.PAY_ID.getName(), transaction.getPayId());
			hashFields.put(FieldType.ORDER_ID.getName(), transaction.getOrderId());
			hashFields.put(FieldType.AMOUNT.getName(), transaction.getAmount());
			hashFields.put(FieldType.TXNTYPE.getName(), transaction.getTxnType());
			hashFields.put(FieldType.CURRENCY_CODE.getName(), transaction.getCurrencyCode());

			String hash = Hasher.getHashWithSalt(hashFields, transaction.getSaltKey());
			transaction.setHaskey(hash);
		}

		catch (Exception e) {
			logger.error("Exception in preparing checkout Status Enquiry Request", e);
		}

	}

	public void setRefund(Fields fields, Transaction transaction) {

		try {

			transaction.setCurrencyCode("356");
			transaction.setPayId(fields.get(FieldType.MERCHANT_ID.getName()));
			transaction.setOrderId(fields.get(FieldType.PG_REF_NUM.getName()));
			transaction.setRefundOrderId(TransactionManager.getNewTransactionId());
			transaction.setTxnType("REFUND");
			transaction.setAmount(fields.get(FieldType.TOTAL_AMOUNT.getName()));
			transaction.setCurrencyCode("356");
			transaction.setPgRefNum(fields.get(FieldType.PG_REF_NUM.getName()));
			transaction.setSaltKey(fields.get(FieldType.PASSWORD.getName()));

			Fields hashFields = new Fields();

			hashFields.put(FieldType.CURRENCY_CODE.getName(), "356");
			hashFields.put(FieldType.REFUND_FLAG.getName(), "C");
			hashFields.put(FieldType.PAY_ID.getName(), transaction.getPayId());
			hashFields.put(FieldType.ORDER_ID.getName(), transaction.getOrderId());
			hashFields.put(FieldType.AMOUNT.getName(), transaction.getAmount());
			hashFields.put(FieldType.TXNTYPE.getName(), transaction.getTxnType());
			hashFields.put(FieldType.REFUND_ORDER_ID.getName(), transaction.getRefundOrderId());
			hashFields.put(FieldType.PG_REF_NUM.getName(), transaction.getPgRefNum());

			String hash = Hasher.getHashWithSalt(hashFields, transaction.getSaltKey());
			transaction.setHaskey(hash);

		}

		catch (Exception e) {
			logger.error("Exception in preapring request ", e);
		}

	}

}
