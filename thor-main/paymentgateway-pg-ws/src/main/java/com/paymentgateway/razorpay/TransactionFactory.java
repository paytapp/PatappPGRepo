package com.paymentgateway.razorpay;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PaymentType;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.TransactionType;
import com.paymentgateway.pg.core.util.AcquirerTxnAmountProvider;

@Service("razorpayFactory")
final class TransactionFactory {

	@Autowired
	private PropertiesManager propertiesManager;

	@Autowired
	private AcquirerTxnAmountProvider acquirerTxnAmountProvider;
	
	@Autowired
	private UserDao userDao;

	private static Logger logger = LoggerFactory.getLogger(TransactionFactory.class.getName());

	@SuppressWarnings("incomplete-switch")
	public Transaction getInstance(Fields fields) throws SystemException {

		Transaction transaction = new Transaction();
		switch (TransactionType.getInstance(fields.get(FieldType.TXNTYPE.getName()))) {
		case REFUND:

			fields.put(FieldType.TOTAL_AMOUNT.getName(), fields.get(FieldType.AMOUNT.getName()));
			setRefund(fields, transaction);
			fields.put(FieldType.PG_REF_NUM.getName(), fields.get(FieldType.TXN_ID.getName()));

			break;
		case SALE:
		case ENROLL:
			fields.put(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName());
			fields.put(FieldType.PG_REF_NUM.getName(), fields.get(FieldType.TXN_ID.getName()));
			fields.put(FieldType.ORIG_TXN_ID.getName(), fields.get(FieldType.TXN_ID.getName()));
			setEnrollment(fields, transaction);
			break;

		default:
			throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR,
					"Invalid txnType to get Transaction instance for Razorpay");
		}

		return transaction;
	}

	private void setEnrollment(Fields fields, Transaction transaction) throws SystemException {

		try {

			String returnUrl = null;
			User user = userDao.findPayId(fields.get(FieldType.PAY_ID.getName()));
			if (user.isAllowCustomHostedUrl()) {
				returnUrl = (user.getCustomHostedUrl() + "/pgui/jsp/phonepeResponse");
			} else {
				returnUrl = PropertiesManager.propertiesMap.get(Constants.RAZORPAY_RESPONSE_URL);
			}
			
			// Append Pg Ref Num to return URL to fetch transaction if session is null in
			// response
			StringBuilder returnUrlUpdated = new StringBuilder();
			
			returnUrlUpdated.append(returnUrl);
			returnUrlUpdated.append("?pgRefNo=");
			returnUrlUpdated.append(fields.get(FieldType.PG_REF_NUM.getName()));

			transaction.setCallback_url(returnUrlUpdated.toString());
			transaction.setAmount(acquirerTxnAmountProvider.amountProvider(fields));
			transaction.setCurrency(fields.get(FieldType.CURRENCY_CODE.getName()));

			if (StringUtils.isNotBlank(fields.get(FieldType.CUST_PHONE.getName()))) {
				transaction.setContact(fields.get(FieldType.CUST_PHONE.getName()));
			} else {
				transaction.setContact(propertiesManager.propertiesMap.get("RAZORPAY_MOBILE").toString());
			}

			if (StringUtils.isNotBlank(fields.get(FieldType.CUST_EMAIL.getName()))) {
				transaction.setEmail(fields.get(FieldType.CUST_EMAIL.getName()));
			} else {
				transaction.setEmail(propertiesManager.propertiesMap.get("RAZORPAY_EMAIL_ID").toString());
			}

			if (fields.get(FieldType.PAYMENT_TYPE.getName()).equalsIgnoreCase(PaymentType.NET_BANKING.getCode())) {

				transaction.setMethod(Constants.NET_BANKING);

				RazorpayMopType razorpayMopType = RazorpayMopType
						.getInstanceFromCode(fields.get(FieldType.MOP_TYPE.getName()));
				transaction.setBank(razorpayMopType.getBankCode());

			}

			if (fields.get(FieldType.PAYMENT_TYPE.getName()).equalsIgnoreCase(PaymentType.CREDIT_CARD.getCode())
					|| fields.get(FieldType.PAYMENT_TYPE.getName())
							.equalsIgnoreCase(PaymentType.DEBIT_CARD.getCode())) {

				transaction.setMethod(Constants.CARD);
				transaction.setNumber(fields.get(FieldType.CARD_NUMBER.getName()));
				transaction.setExpiry_month(fields.get(FieldType.CARD_EXP_DT.getName()).substring(0, 2));
				transaction.setExpiry_year(fields.get(FieldType.CARD_EXP_DT.getName()).substring(2, 6));
				transaction.setCvv(fields.get(FieldType.CVV.getName()));

				if (StringUtils.isNotBlank(fields.get(FieldType.CUST_NAME.getName()))) {
					transaction.setName(fields.get(FieldType.CUST_NAME.getName()));
				} else {
					transaction.setName(propertiesManager.propertiesMap.get("RAZORPAY_NAME").toString());
				}

			}

			if (fields.get(FieldType.PAYMENT_TYPE.getName()).equalsIgnoreCase(PaymentType.WALLET.getCode())) {
				transaction.setMethod(Constants.WALLET);
				
				RazorpayMopType safexpayMopType = RazorpayMopType
						.getInstanceFromCode(fields.get(FieldType.MOP_TYPE.getName()));
				transaction.setWallet(safexpayMopType.getBankCode());
				
			}

			if (fields.get(FieldType.PAYMENT_TYPE.getName()).equalsIgnoreCase(PaymentType.UPI.getCode())) {
				transaction.setMethod(Constants.UPI);
				transaction.setVpa(fields.get(FieldType.PAYER_ADDRESS.getName()));
			}

			transaction.setIp(propertiesManager.propertiesMap.get("RAZORPAY_IP").toString());
			transaction.setReferrer(propertiesManager.propertiesMap.get("RAZORPAY_REFERRER").toString());
			transaction.setUser_agent(propertiesManager.propertiesMap.get("RAZORPAY_USER_AGENT").toString());
			
		}

		catch (Exception e) {
			throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR, e,
					"Exception in preparing sale request for Razorpay");
		}

	}

	private void setRefund(Fields fields, Transaction transaction) throws SystemException {

		try {

			String amount = fields.get(FieldType.TOTAL_AMOUNT.getName());
			transaction.setAmount(amount);

		}

		catch (Exception e) {
			throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR, e,
					"Exception in preparing refund request for Razorpay");
		}

	}

}
