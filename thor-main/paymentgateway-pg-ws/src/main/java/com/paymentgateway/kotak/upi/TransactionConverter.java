package com.paymentgateway.kotak.upi;

import java.security.MessageDigest;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.TransactionType;
import com.paymentgateway.pg.core.util.AcquirerTxnAmountProvider;
import com.paymentgateway.pg.core.util.KotakUpiUtils;

@Service("kotakUpiTransactionConverter")
public class TransactionConverter {

	private static Logger logger = LoggerFactory.getLogger(TransactionConverter.class.getName());

	@Autowired
	private AcquirerTxnAmountProvider acquirerTxnAmountProvider;
	
	@Autowired
	@Qualifier("kotakUpiUtil")
	private KotakUpiUtils kotakUpiUtil;

	@SuppressWarnings("incomplete-switch")
	public JSONObject perpareRequest(Fields fields) throws Exception {

		JSONObject request = null;

		switch (TransactionType.getInstance(fields.get(FieldType.TXNTYPE.getName()))) {
		case REFUND:
			request = payRequest(fields);
			break;
		case SALE:
			request = collectRequest(fields);
			break;
		case ENQUIRY:
			request = statusEnquiryRequest(fields);
			break;
		}
		return request;

	}
	
	
	public String perpareHeader(Fields fields ,JSONObject req ) throws Exception {

		String header = "";
		header = getRequestHeader(fields , req);
		return header;

	}

	public String perpareRefundHeader(Fields fields ,JSONObject req ) throws Exception {

		String header = "";
		header = getRequestHeader(fields , req);
		return header;

	}
	
	public String authoriseRequest(Fields fields) {

		String clientCredential = PropertiesManager.propertiesMap.get(Constants.CLIENTCREDENTIAL);
		String clientId = fields.get(FieldType.ADF5.getName());
		String clientsecret = fields.get(FieldType.ADF6.getName());

		StringBuilder request = new StringBuilder();

		request.append(Constants.GRANDTYPE);
		request.append(Constants.EQUATOR);
		request.append(clientCredential);
		request.append(Constants.SEPARATOR);
		request.append(Constants.CLIENTID);
		request.append(Constants.EQUATOR);
		request.append(clientId);
		request.append(Constants.SEPARATOR);
		request.append(Constants.CLIENTSECRET);
		request.append(Constants.EQUATOR);
		request.append(clientsecret);

		return request.toString();

	}

	public JSONObject vpaValidatorRequest(Fields fields) {

		String CustomerId = fields.get(FieldType.ADF8.getName());
		String aggregatorId = fields.get(FieldType.ADF9.getName());
		String merchantId = fields.get(FieldType.ADF10.getName());
		String payerAddress = fields.get(FieldType.PAYER_ADDRESS.getName());

		JSONObject json = new JSONObject();
		json.put(Constants.AGGREGATORID, aggregatorId);
		json.put(Constants.CUSTOMERID, CustomerId);
		json.put(Constants.MERCHANTID, merchantId);
		json.put(Constants.VPA, payerAddress);

		return json;

	}

	public JSONObject collectRequest(Fields fields) throws SystemException {
	//	Transaction transaction = new Transaction();

		String amount = acquirerTxnAmountProvider.amountProvider(fields);
		String expiryTime = PropertiesManager.propertiesMap.get(Constants.EXPIRYTIME);
		String CustomerId = fields.get(FieldType.ADF8.getName());
		String merchantRefCode = fields.get(FieldType.ADF10.getName());
		String prefix = "KMBMIRCT";

		DateFormat currentDate = new SimpleDateFormat(Constants.DATEFORMAT);
		Calendar calobj = Calendar.getInstance();
		String timeStamp = currentDate.format(calobj.getTime());
		JSONObject json = new JSONObject();

		json.put(Constants.AGGREGATORVPA, fields.get(FieldType.ADF11.getName()));
		json.put(Constants.AMT, amount);
		json.put(Constants.CUSTOMERID, CustomerId);
		json.put(Constants.EXPIRY, expiryTime);
		json.put(Constants.MRC, merchantRefCode);
		json.put(Constants.ORDERID, fields.get(FieldType.ORDER_ID.getName()));
		json.put(Constants.PAYERVPA, fields.get(FieldType.PAYER_ADDRESS.getName()));
		json.put(Constants.REMARKS, Constants.TRNSINITIATED);
		json.put(Constants.TIMESTAMP, timeStamp);
		json.put(Constants.TXNID, prefix+fields.get(FieldType.PG_REF_NUM.getName()));

		JSONObject deviceDetails = new JSONObject();
		deviceDetails.put(Constants.MOBILE, fields.get(FieldType.ADF8.getName()));
		deviceDetails.put(Constants.APP, PropertiesManager.propertiesMap.get(Constants.APPVALUE));

		json.put(Constants.DEVICE, deviceDetails);

		return json;

	}
	
	public String getRequestHeader(Fields fields ,JSONObject req) throws SystemException {
		String amount = acquirerTxnAmountProvider.amountProvider(fields);
		String expiryTime = PropertiesManager.propertiesMap.get(Constants.EXPIRYTIME);
		String CustomerId = fields.get(FieldType.ADF8.getName());
		String merchantRefCode = fields.get(FieldType.ADF10.getName());
		String prefix = "KMBMIRCT";
		String timeStamp = req.getString("timeStamp");

		StringBuilder request = new StringBuilder();
		request.append(fields.get(FieldType.ADF11.getName()));
		request.append(amount);
		request.append(CustomerId);
		request.append(PropertiesManager.propertiesMap.get(Constants.APPVALUE));
		request.append(fields.get(FieldType.ADF8.getName()));
		request.append(expiryTime);
		request.append(timeStamp);
		request.append(merchantRefCode);
		request.append(fields.get(FieldType.PAYER_ADDRESS.getName()));
		request.append(fields.get(FieldType.ORDER_ID.getName()));
		request.append(Constants.TRNSINITIATED);
		request.append(prefix+fields.get(FieldType.PG_REF_NUM.getName()));
		
		String checkSumval = kotakUpiUtil.CheckSum(request.toString(), fields);
		return checkSumval;

	}
	
	public String getRequestRefundHeader(Fields fields ,JSONObject req) throws SystemException {
		
		return null;
		}

	public JSONObject payRequest(Fields fields) throws SystemException {
		String amount = acquirerTxnAmountProvider.amountProvider(fields);
		String CustomerId = fields.get(FieldType.ADF5.getName());

		JSONObject json = new JSONObject();
		json.put(Constants.AMT, amount);
		json.put(Constants.CUSTOMERID, CustomerId);
		json.put(Constants.ACCHN, fields.get(FieldType.ADF5.getName()));
		json.put(Constants.ACCREFN, fields.get(FieldType.ADF5.getName()));
		json.put(Constants.ACCTYPE, fields.get(FieldType.ADF5.getName()));
		json.put(Constants.PAYERVPA, fields.get(FieldType.ADF5.getName()));
		json.put(Constants.REMARKS, fields.get(FieldType.ADF5.getName()));
		json.put(Constants.TXNID, fields.get(FieldType.TXN_ID.getName()));
		json.put(Constants.PAYEEVPA, fields.get(FieldType.ADF5.getName()));
		json.put(Constants.MCC, fields.get(FieldType.ADF5.getName()));
		json.put(Constants.IFSC, fields.get(FieldType.ADF5.getName()));
		json.put(Constants.MERCHANTREFID, fields.get(FieldType.ADF5.getName()));
		json.put(Constants.DEVICEDETAILS, fields.get(FieldType.ADF5.getName()));
		json.put(Constants.APPROVALREF, fields.get(FieldType.ADF5.getName()));
		json.put(Constants.CUSTOMEREFID, fields.get(FieldType.ADF5.getName()));
		json.put(Constants.APP, fields.get(FieldType.ADF5.getName()));
		json.put(Constants.CAPABILITY, fields.get(FieldType.ADF5.getName()));
		json.put(Constants.GCMID, fields.get(FieldType.ADF5.getName()));
		json.put(Constants.GEOCODE, fields.get(FieldType.ADF5.getName()));
		json.put(Constants.ID, fields.get(FieldType.ADF5.getName()));
		json.put(Constants.IP, fields.get(FieldType.ADF5.getName()));
		json.put(Constants.LOCATION, fields.get(FieldType.ADF5.getName()));
		json.put(Constants.MOBILE, fields.get(FieldType.ADF5.getName()));
		json.put(Constants.OS, fields.get(FieldType.ADF5.getName()));
		json.put(Constants.TYPE, fields.get(FieldType.ADF5.getName()));
		return json;
	}

	public JSONObject statusEnquiryRequest(Fields fields) {

		JSONObject json = new JSONObject();

		json.put(Constants.TYPE, fields.get(FieldType.ADF5.getName()));
		json.put(Constants.TXNID, fields.get(FieldType.TXN_ID.getName()));
		json.put(Constants.REFID, fields.get(FieldType.ADF5.getName()));
		json.put(Constants.REFID, fields.get(FieldType.ADF5.getName()));

		return json;
	}

	/*public static byte[] SHA256(String paramString) throws Exception {
		MessageDigest localMessageDigest = MessageDigest.getInstance("SHA-256");
		localMessageDigest.update(paramString.getBytes("UTF-8"));
		byte[] digest = localMessageDigest.digest();
		return digest;
	}

	public static byte[] hexStringToByteArray(String s) {
		byte[] b = new byte[s.length() / 2];
		for (int i = 0; i < b.length; i++) {
			int index = i * 2;
			int v = Integer.parseInt(s.substring(index, index + 2), 16);
			b[i] = (byte) v;
		}
		return b;
	}

	public static byte[] encrypt(byte[] key, byte[] checkSum) throws Exception {
		System.out.println(">>>>>>>>>KEY::" + key.length);
		SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
		byte[] iv = new byte[16];
		IvParameterSpec ivSpec = new IvParameterSpec(iv);
		Cipher acipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		acipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivSpec);
		byte[] arrayOfByte1 = acipher.doFinal(checkSum);
		return arrayOfByte1;
	}
	*/
	
	
	public Transaction toTransactionForCollect(JSONObject response) {

		Transaction transaction = new Transaction();
		Map<String, String> responseMap = new HashMap<String, String>();

		JSONObject RespValAddObject = new JSONObject();
		RespValAddObject = response.getJSONObject("");

		for (Object key : RespValAddObject.keySet()) {

			String key1 = key.toString();
			String value = RespValAddObject.get(key.toString()).toString();
			responseMap.put(key1, value);

		}
		transaction.setResponse(responseMap.get(Constants.SUCCESS_RESPONSE_MSG));
		//transaction.setResponseCode(responseMap.get(Constants.SUCCESS_RESPONSE));
		
		return transaction;

	}

}
