package com.paymentgateway.vepay;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.dao.UserSettingDao;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserSettingData;
import com.paymentgateway.commons.util.Amount;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PaymentType;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.TransactionType;

@Service("vepayFactory")
public class TransactionFactory {

	@Autowired
	private PropertiesManager propertiesManager;
	
	@Autowired
	private UserDao userDao;
	
	@Autowired
	private UserSettingDao userSettingDao;


	private static Logger logger = LoggerFactory.getLogger(TransactionFactory.class.getName());

	@SuppressWarnings("incomplete-switch")
	public Transaction getInstance(Fields fields) {

		Transaction transaction = new Transaction();
		switch (TransactionType.getInstance(fields.get(FieldType.TXNTYPE.getName()))) {
		case ENROLL:
		case SALE:
			fields.put(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName());
			fields.put(FieldType.PG_REF_NUM.getName(), fields.get(FieldType.TXN_ID.getName()));
			fields.put(FieldType.ORIG_TXN_ID.getName(), fields.get(FieldType.TXN_ID.getName()));
			setEnrollment(fields, transaction);
			fields.put(FieldType.CUST_NAME.getName(), transaction.getCustomer_first_name());
			break;
		case REFUND:

			fields.put(FieldType.TOTAL_AMOUNT.getName(), fields.get(FieldType.AMOUNT.getName()));
			setRefund(fields, transaction);
			fields.put(FieldType.PG_REF_NUM.getName(), fields.get(FieldType.TXN_ID.getName()));

			break;
		}

		return transaction;
	}

	public void setEnrollment(Fields fields, Transaction transaction) {

		try {
			String responseUrl = null;	
			UserSettingData userSetting = userSettingDao.fetchDataUsingPayId((fields.get(FieldType.PAY_ID.getName())));
			if (userSetting.isAllowCustomHostedUrl()) {
				responseUrl = (userSetting.getCustomHostedUrl() + "/pgui/jsp/vepayResponse");
			} else {
				responseUrl = PropertiesManager.propertiesMap.get(Constants.VEPAY_RETURN_URL);
			}
			// Append Pg Ref Num to return URL to fetch transaction if session is null in
			// response
			responseUrl = responseUrl+ "?pgRefNo=" + fields.get(FieldType.PG_REF_NUM.getName());

			String amount = Amount.toDecimal(fields.get(FieldType.TOTAL_AMOUNT.getName()),
					fields.get(FieldType.CURRENCY_CODE.getName()));

			transaction.setAmount(amount);

			if (StringUtils.isNotBlank(fields.get(FieldType.CUST_NAME.getName()))) {
				transaction.setCustomer_first_name(fields.get(FieldType.CUST_NAME.getName()));
				transaction.setCustomer_last_name(fields.get(FieldType.CUST_NAME.getName()));
			} else {
				transaction.setCustomer_first_name(propertiesManager.propertiesMap.get("VEPAY_NAME").toString());
				transaction.setCustomer_last_name(propertiesManager.propertiesMap.get("VEPAY_NAME").toString());
			}

			if (StringUtils.isNotBlank(fields.get(FieldType.CUST_EMAIL.getName()))) {
				transaction.setCustomer_email(fields.get(FieldType.CUST_EMAIL.getName()));
			} else {
				transaction.setCustomer_email(propertiesManager.propertiesMap.get("VEPAY_EMAIL_ID").toString());
			}

			if (StringUtils.isNotBlank(fields.get(FieldType.CUST_PHONE.getName()))) {
				transaction.setCustomer_mobile(fields.get(FieldType.CUST_PHONE.getName()));
			} else {
				transaction.setCustomer_mobile(propertiesManager.propertiesMap.get("VEPAY_MOBILE").toString());
			}

			if (fields.get(FieldType.PAYMENT_TYPE.getName()).equalsIgnoreCase(PaymentType.NET_BANKING.getCode())) {
				transaction.setType("NETBANKING");
				String bankCode = VepayMopType.getBankCode(fields.get(FieldType.MOP_TYPE.getName()));
				transaction.setBank_code(bankCode);
			}


			if (fields.get(FieldType.PAYMENT_TYPE.getName()).equalsIgnoreCase(PaymentType.WALLET.getCode())) {
				transaction.setType("WL");
				String bankCode = VepayMopType.getBankCode(fields.get(FieldType.MOP_TYPE.getName()));
				transaction.setProvider(bankCode);

			}

			// Scheme is fixed for UPI as 7
			if (fields.get(FieldType.PAYMENT_TYPE.getName()).equalsIgnoreCase(PaymentType.UPI.getCode())) {
				transaction.setType("UPI");
				transaction.setVpa(fields.get(FieldType.PAYER_ADDRESS.getName()));
			}


			transaction.setMid(fields.get(FieldType.MERCHANT_ID.getName()));
			transaction.setApi_key(fields.get(FieldType.TXN_KEY.getName()));
			transaction.setPassword(fields.get(FieldType.ADF1.getName()));
			transaction.setMerchant_order_token(fields.get(FieldType.PG_REF_NUM.getName()));
			transaction.setCurr_code("INR");
			transaction.setSuccess_url(responseUrl);
			transaction.setFail_url(responseUrl);
			transaction.setPg_cancel_url(responseUrl);

		}

		catch (Exception e) {
			logger.error("Exception in preparing request for Vepay ", e);
		}

	}

	public void setRefund(Fields fields, Transaction transaction) {

		try {

			String amount = Amount.toDecimal(fields.get(FieldType.TOTAL_AMOUNT.getName()),
					fields.get(FieldType.CURRENCY_CODE.getName()));
			transaction.setAmount(amount);

		}

		catch (Exception e) {
			logger.error("Exception in preapring request for Vepay ", e);
		}

	}

}
