package com.paymentgateway.isgpay;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TransactionType;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * @author Rahul
 *
 */
@Service("iSGPayTransactionCommunicator")
public class TransactionCommunicator {

	private static Logger logger = LoggerFactory.getLogger(TransactionCommunicator.class.getName());

	public void updateSaleResponse(Fields fields, String request) {

		fields.put(FieldType.ISGPAY_FINAL_REQUEST.getName(), request.toString());
		fields.put(FieldType.STATUS.getName(), StatusType.SENT_TO_BANK.getName());
		fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.SUCCESS.getResponseCode());
		fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.SUCCESS.getResponseMessage());

	}

	public String vpaValidationResponse(Fields fields, String req) {
		String hostUrl = PropertiesManager.propertiesMap.get("ISGPAYSaleUrl");
		String connectionResponse = null;
		try {
			OkHttpClient client = new OkHttpClient().newBuilder().build();
			MultipartBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
					.addFormDataPart("MerchantId", fields.get(FieldType.MERCHANT_ID.getName()))
					.addFormDataPart("TerminalId", fields.get(FieldType.ADF2.getName()))
					.addFormDataPart("BankId", fields.get(FieldType.ADF1.getName())).addFormDataPart("Version", "1")
					.addFormDataPart("EncData", req).build();

			Request request = new Request.Builder().url(hostUrl).method("POST", body).build();
			Response response = client.newCall(request).execute();
			connectionResponse = response.body().string();

			logger.info("ISGPAY VPA validation response for PGREFNUM >> ",
					fields.get(FieldType.PG_REF_NUM.getName()) + " " + connectionResponse);
			Document document = Jsoup.parse(connectionResponse);
			Elements elementByTag = document.select("input[type=hidden]");
			String encdata = elementByTag.get(0).attr("value");
			return encdata;

		} catch (Exception e) {
			logger.error("error in isgpay VPA validation method >> ", e);
		}
		return null;

	}

	@SuppressWarnings("incomplete-switch")
	public String getResponse(String request, Fields fields) throws SystemException {

		String hostUrl = "";

		try {

			TransactionType transactionType = TransactionType.getInstance(fields.get(FieldType.TXNTYPE.getName()));
			switch (transactionType) {
			case SALE:
			case AUTHORISE:
				break;
			case ENROLL:
				break;
			case CAPTURE:
				break;
			case REFUND:
				hostUrl = PropertiesManager.propertiesMap.get(com.paymentgateway.isgpay.Constants.ISGPAY_REFUND_URL);
				break;
			case STATUS:
				hostUrl = PropertiesManager.propertiesMap
						.get(com.paymentgateway.isgpay.Constants.ISGPAY_STATUS_ENQ_URL);
				break;
			}

			logger.info("Request message to isgpay: TxnType = " + fields.get(FieldType.TXNTYPE.getName()) + " "
					+ "Order Id = " + fields.get(FieldType.ORDER_ID.getName()) + " " + request);

			String url = hostUrl + "?" + request;

			URL obj = new URL(url);
			HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
			con.setRequestMethod("POST");
			con.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.flush();
			wr.close();

			int responseCode = con.getResponseCode();
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();

			logger.info("Response mesage from isgpay: TxnType = " + fields.get(FieldType.TXNTYPE.getName()) + " "
					+ "Order id = " + fields.get(FieldType.ORDER_ID.getName()) + " " + response.toString());
			return response.toString();

		} catch (Exception e) {
			logger.error("Exception in isgpay txn Communicator ", e);
		}
		return null;

	}

}
