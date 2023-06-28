package com.paymentgateway.safexpay;

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
import com.paymentgateway.commons.util.MopType;
import com.paymentgateway.commons.util.PaymentType;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.TransactionType;

@Service("safexpayFactory")
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
		case AUTHORISE:
			break;
		case ENROLL:
			fields.put(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName());
			fields.put(FieldType.PG_REF_NUM.getName(), fields.get(FieldType.TXN_ID.getName()));
			fields.put(FieldType.ORIG_TXN_ID.getName(), fields.get(FieldType.TXN_ID.getName()));
			setEnrollment(fields, transaction);
			fields.put(FieldType.CUST_NAME.getName(), transaction.getCust_name());
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
			String responseUrl = null;	
			UserSettingData userSetting = userSettingDao.fetchDataUsingPayId((fields.get(FieldType.PAY_ID.getName())));
			if (userSetting.isAllowCustomHostedUrl()) {
				responseUrl = (userSetting.getCustomHostedUrl() + "/pgui/jsp/safexpayResponse");
			} else {
				responseUrl = PropertiesManager.propertiesMap.get(Constants.SAFEXPAY_RESPONSE_URL);
			}
			// Append Pg Ref Num to return URL to fetch transaction if session is null in
			// response
			responseUrl = responseUrl+ "?pgRefNo=" + fields.get(FieldType.PG_REF_NUM.getName());

			String amount = Amount.toDecimal(fields.get(FieldType.TOTAL_AMOUNT.getName()),
					fields.get(FieldType.CURRENCY_CODE.getName()));

			transaction.setAmount(amount);

			if (StringUtils.isNotBlank(fields.get(FieldType.CUST_NAME.getName()))) {
				transaction.setCust_name(fields.get(FieldType.CUST_NAME.getName()));
			} else {
				transaction.setCust_name(propertiesManager.propertiesMap.get("SAFEXPAY_NAME").toString());
			}

			if (StringUtils.isNotBlank(fields.get(FieldType.CUST_EMAIL.getName()))) {
				transaction.setEmail_id(fields.get(FieldType.CUST_EMAIL.getName()));
			} else {
				transaction.setEmail_id(propertiesManager.propertiesMap.get("SAFEXPAY_EMAIL_ID").toString());
			}

			if (StringUtils.isNotBlank(fields.get(FieldType.CUST_PHONE.getName()))) {
				transaction.setMobile_no(fields.get(FieldType.CUST_PHONE.getName()));
			} else {
				transaction.setMobile_no(propertiesManager.propertiesMap.get("SAFEXPAY_MOBILE").toString());
			}

			// Scheme is fixed for NB as 7
			if (fields.get(FieldType.PAYMENT_TYPE.getName()).equalsIgnoreCase(PaymentType.NET_BANKING.getCode())) {
				transaction.setPaymode("NB");
				String bankCode = SafexpayMopType.getBankCode(fields.get(FieldType.MOP_TYPE.getName()));
				transaction.setPg_id(bankCode);
				transaction.setScheme("7");

				// Set Card Details blank for NB ad the parameters are still to be sent even in
				// case of NB transaction
				transaction.setCard_no("");
				transaction.setCard_name("");
				transaction.setCvv("");
				transaction.setExp_month("");
				transaction.setExp_year("");

			}

			// Scheme is fixed for VISA , MASTERCARD and RUPAY as 1,2, and 6 respectively
			if (fields.get(FieldType.PAYMENT_TYPE.getName()).equalsIgnoreCase(PaymentType.CREDIT_CARD.getCode())) {

				transaction.setPaymode("CC");
				transaction.setPg_id(fields.get(FieldType.ADF2.getName()));

				if (fields.get(FieldType.MOP_TYPE.getName()).equalsIgnoreCase(MopType.VISA.getCode())) {
					transaction.setScheme("1");
				} else if (fields.get(FieldType.MOP_TYPE.getName()).equalsIgnoreCase(MopType.MASTERCARD.getCode())) {
					transaction.setScheme("2");
				}

				else if (fields.get(FieldType.MOP_TYPE.getName()).equalsIgnoreCase(MopType.RUPAY.getCode())) {
					transaction.setScheme("6");
				}

				transaction.setCard_no(fields.get(FieldType.CARD_NUMBER.getName()));
				transaction.setCard_name(fields.get(FieldType.CARD_HOLDER_NAME.getName()));
				transaction.setCvv(fields.get(FieldType.CVV.getName()));
				transaction.setExp_month(fields.get(FieldType.CARD_EXP_DT.getName()).substring(0, 2));
				transaction.setExp_year(fields.get(FieldType.CARD_EXP_DT.getName()).substring(2, 6));
			}

			if (fields.get(FieldType.PAYMENT_TYPE.getName()).equalsIgnoreCase(PaymentType.DEBIT_CARD.getCode())) {
				transaction.setPaymode("DC");
				transaction.setPg_id(fields.get(FieldType.ADF2.getName()));

				if (fields.get(FieldType.MOP_TYPE.getName()).equalsIgnoreCase(MopType.VISA.getCode())) {
					transaction.setScheme("1");
				} else if (fields.get(FieldType.MOP_TYPE.getName()).equalsIgnoreCase(MopType.MASTERCARD.getCode())) {
					transaction.setScheme("2");
				}

				else if (fields.get(FieldType.MOP_TYPE.getName()).equalsIgnoreCase(MopType.RUPAY.getCode())) {
					transaction.setScheme("6");
				}

				transaction.setCard_no(fields.get(FieldType.CARD_NUMBER.getName()));
				transaction.setCard_name(fields.get(FieldType.CARD_HOLDER_NAME.getName()));
				transaction.setCvv(fields.get(FieldType.CVV.getName()));
				transaction.setExp_month(fields.get(FieldType.CARD_EXP_DT.getName()).substring(0, 2));
				transaction.setExp_year(fields.get(FieldType.CARD_EXP_DT.getName()).substring(2, 6));

			}

			// Scheme is fixed for WL as 7
			if (fields.get(FieldType.PAYMENT_TYPE.getName()).equalsIgnoreCase(PaymentType.WALLET.getCode())) {
				transaction.setPaymode("WA");
				String bankCode = SafexpayMopType.getBankCode(fields.get(FieldType.MOP_TYPE.getName()));
				transaction.setPg_id(bankCode);
				transaction.setScheme("7");

				// Set Card Details blank for NB ad the parameters are still to be sent even in
				// case of NB transaction
				transaction.setCard_no("");
				transaction.setCard_name("");
				transaction.setCvv("");
				transaction.setExp_month("");
				transaction.setExp_year("");

			}

			// Scheme is fixed for UPI as 7
			if (fields.get(FieldType.PAYMENT_TYPE.getName()).equalsIgnoreCase(PaymentType.UPI.getCode())) {
				transaction.setPaymode("UP");
				transaction.setPg_id(fields.get(FieldType.ADF3.getName()));
				transaction.setScheme("7");

				// Set Card Details blank for NB ad the parameters are still to be sent even in
				// case of NB transaction
				transaction.setCard_no("");
				transaction.setCard_name("");
				transaction.setCvv("");
				transaction.setExp_month("");
				transaction.setExp_year("");

			}

			transaction.setEmi_months(propertiesManager.propertiesMap.get("SAFEXPAY_EMI_MONTHS"));
			//transaction.setEmi_months("");

			transaction.setMe_id(fields.get(FieldType.MERCHANT_ID.getName()));
			transaction.setTxnKey(fields.get(FieldType.TXN_KEY.getName()));
			transaction.setAg_id(fields.get(FieldType.ADF1.getName()));
			transaction.setOrder_no(fields.get(FieldType.PG_REF_NUM.getName()));
			transaction.setCountry("IND");
			transaction.setCurrency("INR");
			transaction.setTxn_type("SALE");
			transaction.setSuccess_url(responseUrl);
			transaction.setFailure_url(responseUrl);
			transaction.setChannel("WEB");
			transaction.setUnique_id("");
			transaction.setIs_logged_in("N");

			transaction.setBill_address("");
			transaction.setBill_city("");
			transaction.setBill_state("");
			transaction.setBill_country("");
			transaction.setBill_zip("");

			transaction.setShip_address("");
			transaction.setShip_city("");
			transaction.setShip_state("");
			transaction.setShip_country("");
			transaction.setShip_zip("");
			transaction.setShip_days("");
			transaction.setAddress_count("");

			transaction.setItem_count("");
			transaction.setItem_value("");
			transaction.setItem_category("");

			transaction.setUdf_1("");
			transaction.setUdf_2("");
			transaction.setUdf_3("");
			transaction.setUdf_4("");
			transaction.setUdf_5("");

		}

		catch (Exception e) {
			logger.error("Exception in preparing request for Safexpay ", e);
		}

	}

	public void setStatusEnquiry(Fields fields, Transaction transaction) {

		try {

			// TODO
		}

		catch (Exception e) {
			logger.error("Exception in preparing Safexpay Status Enquiry Request", e);
		}

	}

	public void setRefund(Fields fields, Transaction transaction) {

		try {

			String amount = Amount.toDecimal(fields.get(FieldType.TOTAL_AMOUNT.getName()),
					fields.get(FieldType.CURRENCY_CODE.getName()));
			transaction.setAmount(amount);

		}

		catch (Exception e) {
			logger.error("Exception in preapring request for Safexpay ", e);
		}

	}

}
