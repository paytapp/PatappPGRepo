package com.paymentgateway.federal;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.SystemConstants;
import com.paymentgateway.commons.util.TransactionType;
import com.paymentgateway.pg.core.util.AcquirerTxnAmountProvider;

/**
 * @author Rahul
 *
 */
@Service("federalTransactionConverter")
public class TransactionConverter {
	
	@Autowired
	private AcquirerTxnAmountProvider acquirerTxnAmountProvider;

	private static Logger logger = LoggerFactory.getLogger(TransactionConverter.class.getName());

	@SuppressWarnings("incomplete-switch")
	public String perpareRequest(Fields fields, Transaction transaction) throws SystemException {

		String request = null;

		switch (TransactionType.getInstance(fields.get(FieldType.TXNTYPE.getName()))) {
		case AUTHORISE:
			break;
		case ENROLL:
			request = enrollRequest(fields, transaction);
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

	public String enrollRequest(Fields fields, Transaction transaction) {
		StringBuilder httpRequest = new StringBuilder();
		try {
			String requestUrl = PropertiesManager.propertiesMap.get(Constants.FEDERAL_ENROLLMENT_URL);
			String expYear = transaction.getExpyear();
			String expMonth = transaction.getExpmonth();
			String cardExp = expYear.concat(expMonth);
			String hash = getHash(transaction, cardExp, fields);
			
			String amount = acquirerTxnAmountProvider.amountProvider(fields);

			httpRequest.append("<HTML>");
			httpRequest.append("<BODY OnLoad=\"OnLoadEvent();\" >");
			httpRequest.append("<form name=\"form1\" action=\"");
			httpRequest.append(requestUrl);
			httpRequest.append("\" method=\"post\">");

			httpRequest.append("<input type=\"hidden\" name=\"pan\" value=\"");
			httpRequest.append(transaction.getCard());
			httpRequest.append("\">");
			httpRequest.append("<input type=\"hidden\" name=\"expiry\" value=\"");
			httpRequest.append(cardExp);
			httpRequest.append("\">");
			httpRequest.append("<input type=\"hidden\" name=\"merchant_id\" value=\"");
			httpRequest.append(transaction.getId());
			httpRequest.append("\">");
			httpRequest.append("<input type=\"hidden\" name=\"currency\" value=\"");
			httpRequest.append(transaction.getCurrencycode());
			httpRequest.append("\">");
			httpRequest.append("<input type=\"hidden\" name=\"purchase_amount\" value=\"");
			httpRequest.append(amount);
			httpRequest.append("\">");
			httpRequest.append("<input type=\"hidden\" name=\"order_desc\" value=\"");
			httpRequest.append(transaction.getOrder_desc());
			httpRequest.append("\">");
			httpRequest.append("<input type=\"hidden\" name=\"md\" value=\"");
			httpRequest.append(transaction.getMd());
			httpRequest.append("\">");
			httpRequest.append("<input type=\"hidden\" name=\"message_hash\" value=\"");
			httpRequest.append(hash);
			httpRequest.append("\">");

			httpRequest.append("</form>");
			httpRequest.append("<script language=\"JavaScript\">");
			httpRequest.append("function OnLoadEvent()");
			httpRequest.append("{document.form1.submit();}");
			httpRequest.append("</script>");
			httpRequest.append("</BODY>");
			httpRequest.append("</HTML>");
		} catch (Exception exception) {
			logger.error("Exception", exception);
		}
		return httpRequest.toString();
	}

	public String saleRequest(Fields fields, Transaction transaction) throws SystemException {
		
		String perform = PropertiesManager.propertiesMap.get(Constants.FEDERAL_SALE_PERFORM);
		String merchantId = fields.get(FieldType.ADF1.getName());
		String pgInstanceId = fields.get(FieldType.ADF2.getName());
		String deviceId = fields.get(FieldType.ADF3.getName());
		
		String amount = acquirerTxnAmountProvider.amountProvider(fields);
		/*
		String addSurchargeFlag = PropertiesManager.propertiesMap.get(Constants.FEDERAL_ADD_SURCHARGE_FLAG);
		String amount = null;
		if (addSurchargeFlag.equals("Y")) {
			amount = transaction.getTotalAmt();
		} else {
			amount = transaction.getAmt();
		}*/
		
		String hash = getSaleHash(transaction, fields, perform);
		try {
			hash = URLEncoder.encode(hash, SystemConstants.DEFAULT_ENCODING_UTF_8);
		} catch (UnsupportedEncodingException exception) {
			logger.error("Exception : " , exception);
		}
		String expDate = fields.get(FieldType.CARD_EXP_DT.getName());
		String expMonth = expDate.substring(0, 2);
		String expYear = expDate.substring(2, 6);
		StringBuilder request = new StringBuilder();
		request.append(Constants.PG_INSTANCE_ID);
		request.append(Constants.EQUATOR);
		request.append(pgInstanceId);
		request.append(Constants.SEPARATOR);
		request.append(Constants.MERCHANT_ID);
		request.append(Constants.EQUATOR);
		request.append(merchantId);
		request.append(Constants.SEPARATOR);
		request.append(Constants.PERFORM);
		request.append(Constants.EQUATOR);
		request.append(perform);
		request.append(Constants.SEPARATOR);
		request.append(Constants.PAN);
		request.append(Constants.EQUATOR);
		request.append(transaction.getCard());
		request.append(Constants.SEPARATOR);
		request.append(Constants.EXP_YEAR);
		request.append(Constants.EQUATOR);
		request.append(expYear);
		request.append(Constants.SEPARATOR);
		request.append(Constants.EXP_MONTH);
		request.append(Constants.EQUATOR);
		request.append(expMonth);
		request.append(Constants.SEPARATOR);
		request.append(Constants.CVV);
		request.append(Constants.EQUATOR);
		request.append(transaction.getCvv2());
		request.append(Constants.SEPARATOR);
		request.append(Constants.NAME_ON_CARD);
		request.append(Constants.EQUATOR);
		request.append(fields.get(FieldType.CUST_NAME.getName()));
		request.append(Constants.SEPARATOR);
		request.append(Constants.CURRENCY_CODE);
		request.append(Constants.EQUATOR);
		request.append(fields.get(FieldType.CURRENCY_CODE.getName()));
		request.append(Constants.SEPARATOR);
		request.append(Constants.AMOUNT);
		request.append(Constants.EQUATOR);
		request.append(amount);
		request.append(Constants.SEPARATOR);
		request.append(Constants.MERCHNAT_REF_NO);
		request.append(Constants.EQUATOR);
		request.append(fields.get(FieldType.PG_REF_NUM.getName()));
		request.append(Constants.SEPARATOR);
		request.append(Constants.ORDER_DESC);
		request.append(Constants.EQUATOR);
		request.append(fields.get(FieldType.PRODUCT_DESC.getName()));
		request.append(Constants.SEPARATOR);
		request.append(Constants.CUSTOMER_DEVICE_ID);
		request.append(Constants.EQUATOR);
		request.append(deviceId);
		request.append(Constants.SEPARATOR);
		request.append(Constants.MPI_ID);
		request.append(Constants.EQUATOR);
		request.append(fields.get(FieldType.FEDERAL_MPI_ID.getName()));
		request.append(Constants.SEPARATOR);
		request.append(Constants.STATUS);
		request.append(Constants.EQUATOR);
		request.append(fields.get(FieldType.FEDERAL_STATUS.getName()));
		request.append(Constants.SEPARATOR);
		request.append(Constants.ECI);
		request.append(Constants.EQUATOR);
		request.append(fields.get(FieldType.FEDERAL_ECI.getName()));
		request.append(Constants.SEPARATOR);
		request.append(Constants.XID);
		request.append(Constants.EQUATOR);
		request.append(fields.get(FieldType.FEDERAL_XID.getName()));
		request.append(Constants.SEPARATOR);
		request.append(Constants.CAVV);
		request.append(Constants.EQUATOR);
		request.append(fields.get(FieldType.FEDERAL_CAVV.getName()));
		request.append(Constants.SEPARATOR);
		request.append(Constants.MERCHANT_HASH);
		request.append(Constants.EQUATOR);
		request.append(Constants.COLLECT_REQ_BODY_HEADER);
		request.append(hash);
		
		return request.toString();

	}

	public String refundRequest(Fields fields, Transaction transaction) {

		String perform = PropertiesManager.propertiesMap.get(Constants.FEDERAL_REFUND_PERFORM);
		String merchantId = fields.get(FieldType.ADF1.getName());
		String pgInstanceId = fields.get(FieldType.ADF2.getName());
		String loginKey = fields.get(FieldType.ADF4.getName());
		String refundFlag = fields.get(FieldType.REFUND_FLAG.getName());
		if(refundFlag.equals(Constants.PAYMENT_GATEWAY_REFUND_FLAG)){
			refundFlag = Constants.REFUND_INCLUDING_SURCHARGE_FLAG;
		} else {
			refundFlag = Constants.REFUND_TXN_AMOUNT_FLAG;
		}
		String hash = getRefundHash(transaction, fields);
		try {
			hash = URLEncoder.encode(hash, SystemConstants.DEFAULT_ENCODING_UTF_8);
		} catch (UnsupportedEncodingException exception) {
			logger.error("Exception : " , exception);
		}

		StringBuilder request = new StringBuilder();
		request.append(Constants.PG_INSTANCE_ID);
		request.append(Constants.EQUATOR);
		request.append(pgInstanceId);
		request.append(Constants.SEPARATOR);
		request.append(Constants.MERCHANT_ID);
		request.append(Constants.EQUATOR);
		request.append(merchantId);
		request.append(Constants.SEPARATOR);
		request.append(Constants.PERFORM);
		request.append(Constants.EQUATOR);
		request.append(perform);
		request.append(Constants.SEPARATOR);
		request.append(Constants.ORIGINAL_TXN_ID);
		request.append(Constants.EQUATOR);
		request.append(fields.get(FieldType.ACQ_ID.getName()));
		request.append(Constants.SEPARATOR);
		request.append(Constants.ORIGINAL_MERCHANT_REF_NO);
		request.append(Constants.EQUATOR);
		request.append(fields.get(FieldType.ORIG_TXN_ID.getName()));
		request.append(Constants.SEPARATOR);
		request.append(Constants.NEW_MERCHANT_REF_NO);
		request.append(Constants.EQUATOR);
		request.append(fields.get(FieldType.TXN_ID.getName()));
		request.append(Constants.SEPARATOR);
		request.append(Constants.LOGIN_ID);
		request.append(Constants.EQUATOR);
		request.append(loginKey);
		request.append(Constants.SEPARATOR);
		request.append(Constants.AMOUNT);
		request.append(Constants.EQUATOR);
		request.append(fields.get(FieldType.AMOUNT.getName()));
		request.append(Constants.SEPARATOR);
		request.append(Constants.REFUND_TYPE);
		request.append(Constants.EQUATOR);
		request.append(refundFlag);
		request.append(Constants.SEPARATOR);
		request.append(Constants.MERCHANT_HASH);
		request.append(Constants.EQUATOR);
		request.append(Constants.REFUND_REQ_BODY_HEADER);
		request.append(hash);

		return request.toString();

	}

	public static String getRefundHash(Transaction transaction, Fields fields) {

		String response = null;

		String hashKey = fields.get(FieldType.PASSWORD.getName());
		String merchantId = fields.get(FieldType.ADF1.getName());
		String pgInstanceId = fields.get(FieldType.ADF2.getName());
		String perform = PropertiesManager.propertiesMap.get(Constants.FEDERAL_REFUND_PERFORM);
		String loginKey = fields.get(FieldType.ADF4.getName());

		StringBuilder request = new StringBuilder();
		request.append(pgInstanceId);
		request.append(Constants.PIPE_SEPARATOR);
		request.append(merchantId);
		request.append(Constants.PIPE_SEPARATOR);
		request.append(perform);
		request.append(Constants.PIPE_SEPARATOR);
		request.append(fields.get(FieldType.ACQ_ID.getName()));
		request.append(Constants.PIPE_SEPARATOR);
		request.append(fields.get(FieldType.ORIG_TXN_ID.getName()));
		request.append(Constants.PIPE_SEPARATOR);
		request.append(loginKey);
		request.append(Constants.PIPE_SEPARATOR);
		request.append(hashKey);
		request.append(Constants.PIPE_SEPARATOR);

		MessageDigest messageDigest = null;
		try {
			messageDigest = MessageDigest.getInstance(PropertiesManager.propertiesMap.get(Constants.FEDERAL_HASH_ALGO));
		} catch (NoSuchAlgorithmException exception) {
			logger.error("Exception : " , exception);
		}
		try {
			response = new String(encodeMessageHash(messageDigest.digest(request.toString().getBytes(SystemConstants.DEFAULT_ENCODING_UTF_8))));
		} catch (UnsupportedEncodingException exception) {
			logger.error("Exception : " , exception);
		}

		return response;
	}

	public String statusEnquiryRequest(Fields fields, Transaction transaction) {

		String perform = PropertiesManager.propertiesMap.get(Constants.FEDERAL_STATUS_ENQ_PERFORM);
		String merchantId = fields.get(FieldType.ADF1.getName());
		String pgInstanceId = fields.get(FieldType.ADF2.getName());
		String hash = getStatusEnqHash(transaction, fields);
		try {
			hash = URLEncoder.encode(hash, SystemConstants.DEFAULT_ENCODING_UTF_8);
		} catch (UnsupportedEncodingException exception) {
			logger.error("Exception : " , exception);
		}

		StringBuilder request = new StringBuilder();
		request.append(Constants.PG_INSTANCE_ID);
		request.append(Constants.EQUATOR);
		request.append(pgInstanceId);
		request.append(Constants.SEPARATOR);
		request.append(Constants.MERCHANT_ID);
		request.append(Constants.EQUATOR);
		request.append(merchantId);
		request.append(Constants.SEPARATOR);
		request.append(Constants.PERFORM);
		request.append(Constants.EQUATOR);
		request.append(perform);
		request.append(Constants.SEPARATOR);
		request.append(Constants.CURRENCY_CODE);
		request.append(Constants.EQUATOR);
		request.append(fields.get(FieldType.CURRENCY_CODE.getName()));
		request.append(Constants.SEPARATOR);
		request.append(Constants.AMOUNT);
		request.append(Constants.EQUATOR);
		request.append(fields.get(FieldType.AMOUNT.getName()));
		request.append(Constants.SEPARATOR);
		request.append(Constants.MERCHNAT_REF_NO);
		request.append(Constants.EQUATOR);
		request.append(fields.get(FieldType.PG_REF_NUM.getName()));
		request.append(Constants.SEPARATOR);
		request.append(Constants.TRANSACTION_TYPE);
		request.append(Constants.EQUATOR);
		request.append("9030");
		request.append(Constants.SEPARATOR);
		request.append(Constants.MERCHANT_HASH);
		request.append(Constants.EQUATOR);
		request.append(Constants.ENQ_REQ_BODY_HEADER);
		request.append(hash);

		return request.toString();

	}

	public static String getStatusEnqHash(Transaction transaction, Fields fields) {
		String response = null;

		String hashKey = fields.get(FieldType.PASSWORD.getName());
		String merchantId = fields.get(FieldType.ADF1.getName());
		String pgInstanceId = fields.get(FieldType.ADF2.getName());
		String perform = PropertiesManager.propertiesMap.get(Constants.FEDERAL_STATUS_ENQ_PERFORM);

		StringBuilder request = new StringBuilder();
		request.append(pgInstanceId);
		request.append(Constants.PIPE_SEPARATOR);
		request.append(merchantId);
		request.append(Constants.PIPE_SEPARATOR);
		request.append(perform);
		request.append(Constants.PIPE_SEPARATOR);
		request.append(fields.get(FieldType.CURRENCY_CODE.getName()));
		request.append(Constants.PIPE_SEPARATOR);
		request.append(fields.get(FieldType.AMOUNT.getName()));
		request.append(Constants.PIPE_SEPARATOR);
		request.append(fields.get(FieldType.PG_REF_NUM.getName()));
		request.append(Constants.PIPE_SEPARATOR);
		request.append(hashKey);
		request.append(Constants.PIPE_SEPARATOR);

		MessageDigest messageDigest = null;
		try {
			messageDigest = MessageDigest.getInstance(PropertiesManager.propertiesMap.get(Constants.FEDERAL_HASH_ALGO));
		} catch (NoSuchAlgorithmException exception) {
			logger.error("Exception : " , exception);
		}
		try {
			response = new String(encodeMessageHash(messageDigest.digest(request.toString().getBytes(SystemConstants.DEFAULT_ENCODING_UTF_8))));
		} catch (UnsupportedEncodingException exception) {
			logger.error("Exception : " , exception);
		}

		return response;
	}

	public String getHash(Transaction transaction, String cardExp, Fields fields) throws SystemException {
		String response = null;

		String hashKey = fields.get(FieldType.TXN_KEY.getName());
		String amount = acquirerTxnAmountProvider.amountProvider(fields);
		StringBuilder request = new StringBuilder();
		request.append(transaction.getId());
		request.append(Constants.PIPE_SEPARATOR);
		request.append(transaction.getCard());
		request.append(Constants.PIPE_SEPARATOR);
		request.append(cardExp);
		request.append(Constants.PIPE_SEPARATOR);
		request.append(amount);
		request.append(Constants.PIPE_SEPARATOR);
		request.append(transaction.getMd());
		request.append(Constants.PIPE_SEPARATOR);
		request.append(hashKey);

		MessageDigest messageDigest = null;
		try {
			messageDigest = MessageDigest.getInstance(PropertiesManager.propertiesMap.get(Constants.FEDERAL_HASH_ALGO));
		} catch (NoSuchAlgorithmException exception) {
			logger.error("Exception : " , exception);
		}
		try {
			response = new String(encodeMessageHash(messageDigest.digest(request.toString().getBytes(SystemConstants.DEFAULT_ENCODING_UTF_8))));
		} catch (UnsupportedEncodingException exception) {
			logger.error("Exception : " , exception);
		}

		return response;
	}

	public static String encodeMessageHash(byte[] data) {
		String res = DatatypeConverter.printBase64Binary(data);
		return res;
	}

	public String getSaleHash(Transaction transaction, Fields fields, String perform) throws SystemException {
		String response = null;

		String hashKey = fields.get(FieldType.PASSWORD.getName());
		String merchantId = fields.get(FieldType.ADF1.getName());
		String pgInstanceId = fields.get(FieldType.ADF2.getName());
		String deviceId = fields.get(FieldType.ADF3.getName());
		String amount = acquirerTxnAmountProvider.amountProvider(fields);
		StringBuilder request = new StringBuilder();

		String expDate = fields.get(FieldType.CARD_EXP_DT.getName());
		String expMonth = expDate.substring(0, 2);
		String expYear = expDate.substring(2, 6);
		String cardExpiry = expYear.concat(expMonth);
		request.append(pgInstanceId);
		request.append(Constants.PIPE_SEPARATOR);
		request.append(merchantId);
		request.append(Constants.PIPE_SEPARATOR);
		request.append(perform);
		request.append(Constants.PIPE_SEPARATOR);
		request.append(fields.get(FieldType.CURRENCY_CODE.getName()));
		request.append(Constants.PIPE_SEPARATOR);
		request.append(amount);
		request.append(Constants.PIPE_SEPARATOR);
		request.append(fields.get(FieldType.PG_REF_NUM.getName()));
		request.append(Constants.PIPE_SEPARATOR);
		request.append(transaction.getCard());
		request.append(Constants.PIPE_SEPARATOR);
		request.append(cardExpiry);
		request.append(Constants.PIPE_SEPARATOR);
		request.append(transaction.getCvv2());
		request.append(Constants.PIPE_SEPARATOR);
		request.append(fields.get(FieldType.CUST_NAME.getName()));
		request.append(Constants.PIPE_SEPARATOR);
		request.append(deviceId);
		request.append(Constants.PIPE_SEPARATOR);
		request.append(fields.get(FieldType.FEDERAL_MPI_ID.getName()));
		request.append(Constants.PIPE_SEPARATOR);
		request.append(fields.get(FieldType.FEDERAL_STATUS.getName()));
		request.append(Constants.PIPE_SEPARATOR);
		request.append(fields.get(FieldType.FEDERAL_ECI.getName()));
		request.append(Constants.PIPE_SEPARATOR);
		request.append(fields.get(FieldType.FEDERAL_XID.getName()));
		request.append(Constants.PIPE_SEPARATOR);
		request.append(fields.get(FieldType.FEDERAL_CAVV.getName()));
		request.append(Constants.PIPE_SEPARATOR);
		request.append(hashKey);
		request.append(Constants.PIPE_SEPARATOR);
		// request.append("))");

		MessageDigest messageDigest = null;
		try {
			messageDigest = MessageDigest.getInstance(PropertiesManager.propertiesMap.get(Constants.FEDERAL_HASH_ALGO));
		} catch (NoSuchAlgorithmException exception) {
			logger.error("Exception : " , exception);
		}
		try {
			response = new String(encodeMessageHash(messageDigest.digest(request.toString().getBytes(SystemConstants.DEFAULT_ENCODING_UTF_8))));
		} catch (UnsupportedEncodingException exception) {
			logger.error("Exception : " , exception);
		}

		return response;
	}

	public Transaction toTransaction(String response) {

		Transaction transaction = new Transaction();

		String[] keyValuePairs = response.split(Constants.SEPARATOR);
		Map<String, String> requestMap = new HashMap<String, String>();

		for (String pair : keyValuePairs) {
			String[] keyValue = pair.split(Constants.EQUATOR);
			int arrayLength = keyValue.length;
			if (arrayLength == 1) {
				requestMap.put(keyValue[0], Constants.ZERO_VALUE);
			} else {
				requestMap.put(keyValue[0], keyValue[1]);
			}

		}

		transaction.setTransaction_id(requestMap.get(Constants.TXN_ID));
		transaction.setStatus(requestMap.get(Constants.STATUS));
		transaction.setPg_error_code(requestMap.get(Constants.PG_ERROR_CODE));
		transaction.setPg_error_detail(requestMap.get(Constants.PG_ERROR_DETAILS));
		transaction.setApproval_code(requestMap.get(Constants.APPROVAL_CODE));

		return transaction;
	}

}
