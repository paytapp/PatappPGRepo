package com.paymentgateway.payu;

import java.util.TreeMap;

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
import com.paymentgateway.pg.core.util.AcquirerTxnAmountProvider;
import com.paymentgateway.pg.core.util.PayuUtil;

/**
 * @author Rahul
 *
 */
@Service("payuFactory")
public class TransactionFactory {

	private static Logger logger = LoggerFactory.getLogger(TransactionFactory.class.getName());
	
	@Autowired
	private UserDao userDao;
	
	@Autowired
	private AcquirerTxnAmountProvider acquirerTxnAmountProvider;
	
	@Autowired
	private UserSettingDao userSettingDao;


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
			fields.put(FieldType.CUST_NAME.getName(), transaction.getFirstName());
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
				responseUrl = (userSetting.getCustomHostedUrl() + "/pgui/jsp/payuResponse");
			} else {
				responseUrl = PropertiesManager.propertiesMap.get(Constants.PAYU_RESPONSE_URL);
			}
			String amount = acquirerTxnAmountProvider.amountProvider(fields);
			/*
			 * String amount =
			 * Amount.toDecimal(fields.get(FieldType.TOTAL_AMOUNT.getName()),
			 * fields.get(FieldType.CURRENCY_CODE.getName()));
			 */
			String firstName = fields.get(FieldType.CUST_NAME.getName());
			String paymentType = fields.get(FieldType.PAYMENT_TYPE.getName());
			String mopType = fields.get(FieldType.MOP_TYPE.getName());
			String bankCode = fields.get(FieldType.MOP_TYPE.getName());
			if(paymentType.equals(Constants.WL)) {
				paymentType= Constants.CASH;
			}
			if(paymentType.equals(Constants.NB)||paymentType.equals(Constants.CASH)) {
				bankCode = PayuNBMopType.getBankCode(mopType);
			} 
			else if(paymentType.equals(PaymentType.UPI.getCode())) {
				bankCode = Constants.UPI;
				paymentType= Constants.UPI;
				transaction.setVpa(fields.get(FieldType.PAYER_ADDRESS.getName()));
				fields.put(FieldType.CARD_MASK.getName(),fields.get(FieldType.PAYER_ADDRESS.getName()));
			}
			else {
				transaction.setCcnum(fields.get(FieldType.CARD_NUMBER.getName()));
				transaction.setCcname(fields.get(FieldType.CARD_HOLDER_NAME.getName())); 
				transaction.setCcvv(fields.get(FieldType.CVV.getName()));
				transaction.setCcExpYr(fields.get(FieldType.CARD_EXP_DT.getName()));
				transaction.setCcExpMon(fields.get(FieldType.CARD_EXP_DT.getName()));
			}
			
			transaction.setKey(fields.get(FieldType.MERCHANT_ID.getName()));
			transaction.setTxnId(fields.get(FieldType.PG_REF_NUM.getName()));
			transaction.setAmount(amount);
			transaction.setProductInfo(Constants.PAYMENT_GATEWAY_PRODUCT);
			 
			if(StringUtils.isEmpty(firstName)) {
				fields.put(FieldType.CUST_NAME.getName(), Constants.PAYMENT_GATEWAY);
			} else if (firstName.length() > 20){
				firstName = firstName.substring(0, 20);
				fields.put(FieldType.CUST_NAME.getName(), firstName); 
			}
			transaction.setFirstName(fields.get(FieldType.CUST_NAME.getName()));
			transaction.setEmail(Constants.PAYMENT_GATEWAY_PAYU_EMAIL);
			transaction.setSurl(responseUrl);
			transaction.setFurl(responseUrl);
			transaction.setPg(paymentType);
			transaction.setBankCode(bankCode);
			transaction.setConsentShared("0");
			transaction.setSalt(fields.get(FieldType.PASSWORD.getName()));

			TreeMap<String, String> payuParams = new TreeMap<String, String>();

			payuParams.put(Constants.KEY, transaction.getKey());
			payuParams.put(Constants.TXNID, transaction.getTxnId());
			payuParams.put(Constants.AMOUNT, transaction.getAmount());
			payuParams.put(Constants.PRODUCT_INFO, transaction.getProductInfo());
			payuParams.put(Constants.FIRSTNAME, transaction.getFirstName());
			payuParams.put(Constants.EMAIL, transaction.getEmail());
			payuParams.put(Constants.SALT, transaction.getSalt());
			String hash = PayuUtil.payuSaleRequestHash(payuParams);
			transaction.setHash(hash);

		}

		catch (Exception e) {
			logger.error("Exception in preapring request for Payu ", e);
		}

	}

	public void setStatusEnquiry(Fields fields, Transaction transaction) {

		try {

			transaction.setKey(fields.get(FieldType.MERCHANT_ID.getName()));
			transaction.setTxnId(fields.get(FieldType.ORDER_ID.getName()));
			transaction.setSalt(fields.get(FieldType.PASSWORD.getName()));

			TreeMap<String, String> payuParams = new TreeMap<String, String>();

			payuParams.put(Constants.KEY, transaction.getKey());
			payuParams.put(Constants.COMMAND, Constants.VERIFY_PAYMENT);
			payuParams.put(Constants.VAR1, transaction.getTxnId());
			payuParams.put(Constants.SALT, transaction.getSalt());

			String hash = PayuUtil.payuRefundAndStatusEnqHash(payuParams);
			transaction.setHash(hash);

		}

		catch (Exception e) {
			logger.error("Exception in preparing Payu Status Enquiry Request", e);
		}

	}

	public void setRefund(Fields fields, Transaction transaction) {

		try {
			transaction.setKey(fields.get(FieldType.MERCHANT_ID.getName()));
			transaction.setMihPayuId(fields.get(FieldType.ACQ_ID.getName()));
			transaction.setSalt(fields.get(FieldType.PASSWORD.getName()));
			transaction.setRefundToken(fields.get(FieldType.REFUND_ORDER_ID.getName()));
			String amount = Amount.toDecimal(fields.get(FieldType.TOTAL_AMOUNT.getName()),
					fields.get(FieldType.CURRENCY_CODE.getName()));
			transaction.setRefundAmount(amount);

			TreeMap<String, String> payuParams = new TreeMap<String, String>();

			payuParams.put(Constants.KEY, transaction.getKey());
			payuParams.put(Constants.COMMAND, Constants.CANCEL_REFUND_TRANSACTION);
			payuParams.put(Constants.VAR1, transaction.getMihPayuId());
			payuParams.put(Constants.VAR2, transaction.getRefundToken());
			payuParams.put(Constants.VAR3, transaction.getRefundAmount());
			payuParams.put(Constants.SALT, transaction.getSalt());

			String hash = PayuUtil.payuRefundAndStatusEnqHash(payuParams);
			transaction.setHash(hash);

		}

		catch (Exception e) {
			logger.error("Exception in preapring request for Paytu ", e);
		}

	}

}
