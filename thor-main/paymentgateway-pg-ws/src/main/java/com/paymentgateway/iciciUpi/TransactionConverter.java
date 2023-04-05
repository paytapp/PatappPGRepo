package com.paymentgateway.iciciUpi;

import java.io.File;
import java.io.FileInputStream;
import java.security.PublicKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Calendar;

import javax.crypto.Cipher;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.TransactionType;
import com.paymentgateway.pg.core.util.AcquirerTxnAmountProvider;

/**
 * @author Rahul, Amitosh
 *
 */
@Service("iciciUpiTransactionConverter")
public class TransactionConverter {

	@Autowired
	private UserDao userDao;

	@Autowired
	private AcquirerTxnAmountProvider acquirerTxnAmountProvider;

	@Autowired
	private PropertiesManager propertiesManager;

	private static final Logger logger = LoggerFactory.getLogger(TransactionConverter.class.getName());

	@SuppressWarnings("incomplete-switch")
	public String perpareRequest(Fields fields, Transaction transaction) throws SystemException {

		String request = null;

		switch (TransactionType.getInstance(fields.get(FieldType.TXNTYPE.getName()))) {
		case AUTHORISE:
		case ENROLL:
			break;
		case REFUND:
			request = refundRequest(fields, transaction);
			break;
		case SALE:
			request = saleRequest(fields, transaction);
			break;
		case CAPTURE:
			break;
		case STATUS:
			request = statusEnquiryRequest(fields, transaction);
			break;
		}

		return request.toString();

	}

	public String saleRequest(Fields fields, Transaction transaction) throws SystemException {
		logger.info("Preparing sale request for ICICI UPI transaction");
		try {
			String amount = acquirerTxnAmountProvider.amountProvider(fields);
			DateFormat currDate = new SimpleDateFormat("dd/MM/yyyy hh:mm a");
			Calendar cal = Calendar.getInstance();
			cal.setTime(cal.getTime());
			cal.add(Calendar.MINUTE, Constants.COLLECT_REQUEST_TIMEOUT_MINUTES);
			String expDate = currDate.format(cal.getTime());

			JSONObject request = new JSONObject();
			request.put(Constants.PAYER_VA, fields.get(FieldType.PAYER_ADDRESS.getName()));
			request.put(Constants.AMOUNT, amount);
			request.put(Constants.NOTE, Constants.COLLECT_PAY_REQUEST);
			request.put(Constants.COLLECT_BY_DATE, expDate);
			request.put(Constants.MERCHANT_ID, fields.get(FieldType.MERCHANT_ID.getName()));
			request.put(Constants.MERCHANT_NAME,
					userDao.getBusinessNameByPayId(fields.get(FieldType.PAY_ID.getName())));

			if (StringUtils.isNotBlank(fields.get(FieldType.ADF1.getName()))) {
				request.put(Constants.SUB_MERCHANT_ID, fields.get(FieldType.ADF1.getName()));
			} else {
				request.put(Constants.SUB_MERCHANT_ID, fields.get(FieldType.MERCHANT_ID.getName()));
			}

			if (StringUtils.isNotBlank(fields.get(FieldType.ADF1.getName()))) {
				request.put(Constants.SUB_MERCHANT_NAME, fields.get(FieldType.ADF1.getName()));
			} else {
				request.put(Constants.SUB_MERCHANT_NAME,
						userDao.getBusinessNameByPayId(fields.get(FieldType.PAY_ID.getName())));
			}

			request.put(Constants.TERMINAL_ID, fields.get(FieldType.TXN_KEY.getName()));
			request.put(Constants.MERCHANT_TRANSACTION_ID, fields.get(FieldType.PG_REF_NUM.getName()));
			request.put(Constants.BILL_NUMBER, fields.get(FieldType.PG_REF_NUM.getName()));

			String saleRequest = request.toString();
			logger.info("Sale request to ICIC UPI, " + saleRequest);
			String encodedAndEncyptedSaleRequest = encryptAndEncodeRequest(request.toString());
			logger.info("Encrypted sale request to ICIC UPI, " + encodedAndEncyptedSaleRequest);
			return encodedAndEncyptedSaleRequest;
		} catch (Exception e) {
			logger.error("Exception caugth : " , e);
		}
		return null;
	}

	public String refundRequest(Fields fields, Transaction transaction) throws SystemException {
		logger.info("Preparing refund request for ICICI UPI");
		try {
			JSONObject request = new JSONObject();
			String amount = acquirerTxnAmountProvider.amountProvider(fields);
			request.put(Constants.MERCHANT_ID, fields.get(FieldType.MERCHANT_ID.getName()));
			if (StringUtils.isNotBlank(fields.get(FieldType.ADF1.getName()))) {
				request.put(Constants.SUB_MERCHANT_ID, fields.get(FieldType.ADF1.getName()));
			} else {
				request.put(Constants.SUB_MERCHANT_ID, fields.get(FieldType.MERCHANT_ID.getName()));
			}
			request.put(Constants.TERMINAL_ID, fields.get(FieldType.TXN_KEY.getName()));
			request.put(Constants.BANK_RRN, fields.get(FieldType.RRN.getName()));
			request.put(Constants.MERCHANT_TRANSACTION_ID, fields.get(FieldType.PG_REF_NUM.getName()));
			request.put(Constants.ORIGINAL_MERCHANT_TRANSACTION_ID, fields.get(FieldType.ORIG_TXN_ID.getName()));
			request.put(Constants.REFUND_AMOUNT, amount);
			request.put(Constants.PAYEE_VA, fields.get(FieldType.UDF3.getName()));
			request.put(Constants.NOTE, Constants.REFUND_NOTE);

			/*
			 * "Y" for online refund and "N" for Offline refund Online Refund Amount will
			 * instantly credited to customer's account. Offline refund Amount will settled
			 * in T+1 day to customer's account.
			 */

			request.put(Constants.ONLINE_REFUND, Constants.Y);
			request.put(Constants.TERMINAL_ID, fields.get(FieldType.TXN_KEY.getName()));
			String refundRequest = request.toString();
			logger.info("Refund request to ICICI UPI, " + refundRequest);
			String encodedAndEncyptedRefundRequest = encryptAndEncodeRequest(request.toString());
			logger.info("Encrypted Refund request to ICIC UPI, " + encodedAndEncyptedRefundRequest);
			return encodedAndEncyptedRefundRequest;
		} catch (Exception e) {
			logger.error("Exception caugth : " , e);
		}
		return null;
	}

	public String statusEnquiryRequest(Fields fields, Transaction transaction) {
		logger.info("Preparing status enquiry request for ICICI UPI");
		try {
			JSONObject request = new JSONObject();
			request.put(Constants.MERCHANT_ID, fields.get(FieldType.MERCHANT_ID.getName()));
			if (StringUtils.isNotBlank(fields.get(FieldType.ADF1.getName()))) {
				request.put(Constants.SUB_MERCHANT_ID, fields.get(FieldType.ADF1.getName()));
			} else {
				request.put(Constants.SUB_MERCHANT_ID, fields.get(FieldType.MERCHANT_ID.getName()));
			}
			request.put(Constants.TERMINAL_ID, fields.get(FieldType.TXN_KEY.getName()));
			request.put(Constants.MERCHANT_TRANSACTION_ID, fields.get(FieldType.PG_REF_NUM.getName()));
			String statusEnquiryRequest = request.toString();
			logger.info("Status Enquiry Request to ICIC UPI, " + statusEnquiryRequest);
			String encodedAndEncyptedstatusEnquiryRequest = encryptAndEncodeRequest(request.toString());
			logger.info("Encrypted Status Enquiry Request to ICIC UPI, " + encodedAndEncyptedstatusEnquiryRequest);
			return encodedAndEncyptedstatusEnquiryRequest;
		} catch (Exception e) {
			logger.error("Exception caugth : " , e);
		}
		return null;
	}

	protected String encryptAndEncodeRequest(String saleRequest) {
		try {
			logger.info("ICICI UPI Encryption Start");
			PublicKey publicKey = getPublicKey(new File(Constants.PUBLIC_KEY_FILE_NAME));
			logger.info("Raw Request " + saleRequest);
			Cipher cipher = Cipher.getInstance(propertiesManager.propertiesMap.get("Encrypt_Decrypt_Algo"));
			cipher.init(Cipher.ENCRYPT_MODE, publicKey);
			logger.info("Icici Encryption Finished");
			return Base64.getEncoder().encodeToString(cipher.doFinal(saleRequest.getBytes()));
		} catch (Exception e) {
			logger.error("Exception : " , e);
			return null;
		}
	}

	private static PublicKey getPublicKey(File filename) {
		try {
			FileInputStream inputStream = new FileInputStream(filename);
			CertificateFactory cert = CertificateFactory.getInstance("X.509");
			X509Certificate cer = (X509Certificate) cert.generateCertificate(inputStream);
			inputStream.close();
			return cer.getPublicKey();
		} catch (Exception e) {
			logger.error("Exception : " , e);
			return null;
		}
	}

	public Transaction toTransaction(String xml) {
		Transaction transaction = new Transaction();
		return transaction;
	}
}
