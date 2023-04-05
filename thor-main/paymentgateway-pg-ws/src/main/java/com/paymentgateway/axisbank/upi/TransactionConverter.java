package com.paymentgateway.axisbank.upi;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;
import javax.net.ssl.HttpsURLConnection;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.Amount;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TransactionManager;
import com.paymentgateway.commons.util.TransactionType;
import com.paymentgateway.pg.core.util.AcquirerTxnAmountProvider;
import com.paymentgateway.pg.core.util.AxisBankUpiEncDecService;

/**
 * @author Rahul
 *
 */
@Service("axisBankUpiTransactionConverter")
public class TransactionConverter {

	@Autowired
	private AcquirerTxnAmountProvider acquirerTxnAmountProvider;

	@Autowired
	private PropertiesManager propertiesManager;

	@Autowired
	private AxisBankUpiEncDecService axisBankUpiChecksumGenerator;

	private static byte[] SALT = new byte[] { -57, -75, -103, -12, 75, 124, -127, 119 };
	private static Map<String, SecretKey> encDecMap = new HashMap<String, SecretKey>();

	private static final Logger logger = LoggerFactory.getLogger(TransactionConverter.class.getName());

	public static final String DOUBLE_PIPE = "||";
	public static final String PIPE = "|";

	@SuppressWarnings("incomplete-switch")
	public JSONObject perpareRequest(Fields fields) throws SystemException {

		JSONObject request = null;

		switch (TransactionType.getInstance(fields.get(FieldType.TXNTYPE.getName()))) {
		case AUTHORISE:
		case ENROLL:
			break;
		case REFUND:
			request = refundRequest(fields);
			break;
		case SALE:
			request = saleRequest(fields);
			break;
		case CAPTURE:
			break;
		case SETTLE:
			break;
		case STATUS:
			request = statusEnquiryRequest(fields);
			break;
		}

		return request;

	}

	public JSONObject saleRequest(Fields fields) throws SystemException {

		JSONObject request = new JSONObject();

		String merchId = fields.get(FieldType.ADF4.getName());
		String merchChanId = fields.get(FieldType.ADF5.getName());
		String unqTxnId = fields.get(FieldType.PG_REF_NUM.getName());
		String unqCustId = TransactionManager.getNewTransactionId();
		String amount = Amount.toDecimal(fields.get(FieldType.TOTAL_AMOUNT.getName()),
				fields.get(FieldType.CURRENCY_CODE.getName()));
		String txnDtl = Constants.TXN_DTL_VAL;
		String currency = Constants.CURRENCY_VAL;
		String orderId = TransactionManager.getNewTransactionId();
		String customerVpa = fields.get(FieldType.PAYER_ADDRESS.getName());
		String expiry = propertiesManager.propertiesMap.get(Constants.AXISBANK_UPI_EXPIRY);
		String sid = "";

		request.put(Constants.MERCH_ID, merchId);
		request.put(Constants.MERCH_CHAN_ID, merchChanId);
		request.put(Constants.UNQ_TXN_ID, unqTxnId);
		request.put(Constants.UNQ_CUST_ID, unqCustId);
		request.put(Constants.AMOUNT, amount);
		request.put(Constants.TXN_DTL, txnDtl);
		request.put(Constants.CURRENCY, currency);
		request.put(Constants.ORDER_ID, orderId);
		request.put(Constants.CUSTOMER_VPA, customerVpa);
		request.put(Constants.EXPIRY, expiry);
		request.put(Constants.SID, "");

		return request;
	}

	public JSONObject vpaValidatorRequest(Fields fields, JSONObject request) throws SystemException {

		StringBuilder serverResponse = new StringBuilder();
		String hostUrl = PropertiesManager.propertiesMap.get(Constants.AXISBANK_UPI_VPA_VALIDATION_URL);

		HttpsURLConnection connection = null;

		if (hostUrl.contains("https")) {
			try {

				URL url = new URL(hostUrl);
				connection = (HttpsURLConnection) url.openConnection();

				connection.setRequestMethod("POST");
				connection.setRequestProperty("Content-Type", "application/json");
				connection.setRequestProperty("Content-Length", request.toString());
				connection.setRequestProperty("Content-Language", "en-US");

				connection.setUseCaches(false);
				connection.setDoOutput(true);
				connection.setDoInput(true);

				// Send request
				OutputStream outputStream = connection.getOutputStream();
				DataOutputStream wr = new DataOutputStream(outputStream);
				wr.writeBytes(request.toString());
				wr.close();

				// Get Response
				InputStream is = connection.getInputStream();
				BufferedReader rd = new BufferedReader(new InputStreamReader(is));
				String line;

				int code = ((HttpURLConnection) connection).getResponseCode();
				int firstDigitOfCode = Integer.parseInt(Integer.toString(code).substring(0, 1));
				if (firstDigitOfCode == 4 || firstDigitOfCode == 5) {
					fields.put(FieldType.STATUS.getName(), StatusType.ACQUIRER_DOWN.getName());
					logger.error("Response code of txn :" + code);
					throw new SystemException(ErrorType.ACUIRER_DOWN,
							"Network Exception with hdfc Upi " + hostUrl.toString());
				}

				while ((line = rd.readLine()) != null) {
					serverResponse.append(line);

				}
				rd.close();

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (connection != null) {
					connection.disconnect();
				}
			}
		}

		return null;
	}

	public JSONObject refundRequest(Fields fields) throws SystemException {

		JSONObject request = new JSONObject();

		String merchId = fields.get(FieldType.ADF4.getName());
		String merchChanId = fields.get(FieldType.ADF5.getName());
		String txnRefundId = fields.get(FieldType.PG_REF_NUM.getName());
		String unqTxnId = fields.get(FieldType.ORIG_TXN_ID.getName());
		String unqCustId = TransactionManager.getNewTransactionId().substring(0, 10);
		String amount = Amount.toDecimal(fields.get(FieldType.AMOUNT.getName()),
				fields.get(FieldType.CURRENCY_CODE.getName()));
		String txnDtl = Constants.TXN_DTL_VAL;
		String sid = "";

		request.put(Constants.MERCH_ID, merchId);
		request.put(Constants.MERCH_CHAN_ID, merchChanId);
		request.put(Constants.TXN_REFUND_ID, txnRefundId);
		request.put(Constants.MOB_NO, unqCustId);
		request.put(Constants.TXN_REFUND_AMOUNT, amount);
		request.put(Constants.UNQ_TXN_ID, unqTxnId);
		request.put(Constants.REFUND_REASON, txnDtl);
		request.put(Constants.SID, sid);

		String checkSumString = merchId + merchChanId + txnRefundId + unqCustId + amount + unqTxnId + txnDtl + sid;

		String checkSum = axisBankUpiChecksumGenerator.generateCheckSum(checkSumString);
		request.put(Constants.CHECKSUM, checkSum);
		logger.info("Axis Upi Refund Request >>>  "+request.toString());
		return request;
	}

	public JSONObject statusEnquiryRequest(Fields fields) throws SystemException {

		JSONObject request = new JSONObject();

		String merchId = fields.get(FieldType.ADF4.getName());
		String merchChanId = fields.get(FieldType.ADF5.getName());
		String tranid = fields.get(FieldType.PG_REF_NUM.getName());
		String unqCustId = TransactionManager.getNewTransactionId().substring(0, 10);

		request.put("merchid", merchId);
		request.put("merchchanid", merchChanId);
		request.put("tranid", tranid);
		request.put("mobilenumber", unqCustId);

		String checkSumString = merchId + merchChanId + tranid + unqCustId;

		String checkSum = axisBankUpiChecksumGenerator.generateCheckSum(checkSumString);
		request.put(Constants.CHECKSUM_STATUS, checkSum);

		logger.info("Axis Upi Status Enq Request >>>  "+request.toString());
		return request;
	}

	public Transaction toTransaction(String response, Fields fields) throws SystemException {

		Transaction transaction = new Transaction();
		JSONObject jsonObj = new JSONObject(response);

		if (jsonObj.get(Constants.CODE) != null) {
			transaction.setCode(jsonObj.get(Constants.CODE).toString());
		}

		if (jsonObj.get(Constants.RESULT) != null) {
			transaction.setResult(jsonObj.get(Constants.RESULT).toString());
		}

		if (jsonObj.get(Constants.DATA) != null) {

			String data = jsonObj.get(Constants.DATA).toString();
			JSONObject jsonObjData = new JSONObject(data);

			if (jsonObjData.get(Constants.W_COLLECT_TXN_ID) != null) {
				transaction.setwCollectTxnId(jsonObjData.get(Constants.W_COLLECT_TXN_ID).toString());
			}

			if (jsonObjData.get(Constants.MERCH_TRAN_ID) != null) {
				transaction.setMerchId(jsonObjData.get(Constants.MERCH_TRAN_ID).toString());
			}

		}

		return transaction;
	}

	public Transaction toTransactionFailure(String response, Fields fields) {

		Transaction transaction = new Transaction();

		transaction.setCode("NA");
		transaction.setResult("NA");

		return transaction;
	}

	public Transaction toTransactionRefund(String response, Fields fields) {

		Transaction transaction = new Transaction();
		JSONObject jsonObj = new JSONObject(response);

		if (jsonObj.get(Constants.CODE) != null) {
			transaction.setCode(jsonObj.get(Constants.CODE).toString());
		}

		if (jsonObj.get(Constants.RESULT) != null) {
			transaction.setResult(jsonObj.get(Constants.RESULT).toString());
		}

		if (jsonObj.get(Constants.DATA) != null) {
			transaction.setData(jsonObj.get(Constants.DATA).toString());
		}

		return transaction;

	}

	public Transaction toTransactionStatusEnquiry(String response, Fields fields) {

		Transaction transaction = new Transaction();

		return transaction;
	}

	public TransactionConverter() {
	}

}
