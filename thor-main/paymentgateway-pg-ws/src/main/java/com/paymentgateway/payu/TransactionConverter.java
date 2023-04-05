package com.paymentgateway.payu;

import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.TransactionType;

/**
 * @author Rahul
 *
 */
@Service("payuTransactionConverter")
public class TransactionConverter {

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
		}

		// Case Status and capture is missing .
		return request;

	}

	public String saleRequest(Fields fields, Transaction transaction) throws SystemException {

		try {

			String requestURL = PropertiesManager.propertiesMap.get("PAYUSaleUrl");
			String expMonth = "";
			String expYear = "";
			String pg = transaction.getPg();

			String cardExpMonYr = transaction.getCcExpYr();
			if (cardExpMonYr != null) {
				expMonth = cardExpMonYr.substring(0, cardExpMonYr.length() - 4);
				expYear = cardExpMonYr.substring(cardExpMonYr.length() - 4);
			}

			TreeMap<String, String> payuParams = new TreeMap<String, String>();

			payuParams.put(Constants.KEY, transaction.getKey());
			payuParams.put(Constants.TXNID, transaction.getTxnId());
			payuParams.put(Constants.AMOUNT, transaction.getAmount());
			payuParams.put(Constants.PRODUCT_INFO, transaction.getProductInfo());
			payuParams.put(Constants.FIRSTNAME, transaction.getFirstName());
			payuParams.put(Constants.EMAIL, transaction.getEmail());
			payuParams.put(Constants.SURL, transaction.getSurl());
			payuParams.put(Constants.FURL, transaction.getFurl());
			payuParams.put(Constants.PG, transaction.getPg());
			payuParams.put(Constants.BANKCODE, transaction.getBankCode());
			if(pg.equals(Constants.CC)||pg.equals(Constants.DC)) {
				payuParams.put(Constants.CCNUM, transaction.getCcnum());
				payuParams.put(Constants.CCNAME, transaction.getCcname());
				payuParams.put(Constants.CCVV, transaction.getCcvv());
				payuParams.put(Constants.CCEXPMON, expMonth);
				payuParams.put(Constants.CCEXPYR, expYear);
			}
			if(pg.equals(Constants.UPI)) {
				payuParams.put(Constants.VPA, transaction.getVpa());
			}
			payuParams.put("Consent_shared", transaction.getConsentShared());

			StringBuilder outputHtml = new StringBuilder();
			outputHtml.append("<html>");
			outputHtml.append("<head>");
			outputHtml.append("<title>Payment GateWay Merchant Checkout Page</title>");
			outputHtml.append("</head>");
			outputHtml.append("<body>");
			outputHtml.append("<center><h1>Please do not refresh this page...</h1></center>");
			outputHtml.append("<form method='post' action='" + requestURL + "' name='payu_form'>");
			String requestStart ="{";
			String requestEnd ="}";
			String request="";
			for (Map.Entry<String, String> entry : payuParams.entrySet()) {
				outputHtml
						.append("<input type='hidden' name='" + entry.getKey() + "' value='" + entry.getValue() + "'>");
				//request=request.concat("\\"+'"'+entry.getKey()+"\\"+'"'+":"+"\\"+'"'+entry.getValue()+"\\"+'"'+",");
				request=request.concat('"' +entry.getKey() + '"' + ":" + '"' + entry.getValue()+'"' + ",");
				
			}

			outputHtml.append("<input type='hidden' name='hash' value='" + transaction.getHash() + "'>");
			outputHtml.append("</form>");
			outputHtml.append("<script type='text/javascript'>");
			outputHtml.append("document.payu_form.submit();");
			outputHtml.append("</script>");
			outputHtml.append("</body>");
			outputHtml.append("</html>");

			log("PayU Request >>>"+requestStart+request+requestEnd);
			return outputHtml.toString();
			
			
		}

		catch (Exception e) {
			logger.error("Exception in generating Payu sale request ", e);
		}

		return null;
	}

	public String refundRequest(Fields fields, Transaction transaction) throws SystemException {

		try {

			StringBuilder request = new StringBuilder();

			request.append(Constants.RKEY);
			request.append(transaction.getKey());
			request.append("&");
			request.append(Constants.RCOMMAND);
			request.append(Constants.CANCEL_REFUND_TRANSACTION);
			request.append("&");
			request.append(Constants.RHASH);
			request.append(transaction.getHash());
			request.append("&");
			request.append(Constants.RVAR1);
			request.append(transaction.getMihPayuId());
			request.append("&");
			request.append(Constants.RVAR2);
			request.append(transaction.getRefundToken());
			request.append("&");
			request.append(Constants.RVAR3);
			request.append(transaction.getRefundAmount());

			String post_data = request.toString();
			return post_data;

		}

		catch (Exception e) {
			logger.error("Exception in generating payu refund request", e);
		}
		return null;

	}

	public Transaction toTransaction(String jsonResponse, String txnType) {

		Transaction transaction = new Transaction();

		if (StringUtils.isBlank(jsonResponse)) {

			logger.info("Empty response received for payu refund");
			return transaction;
		}

		JSONObject respObj = new JSONObject(jsonResponse);

		if (respObj.has(Constants.STATUS)) {

			JSONObject respBody = new JSONObject(jsonResponse);

			if (respBody.has(Constants.MSG)) {

				if (respBody.has(Constants.STATUS)) {

					String status = respBody.get(Constants.STATUS).toString();
					transaction.setStatus(status);
				}

				if (respBody.has(Constants.MSG)) {

					String msg = respBody.get(Constants.MSG).toString();
					transaction.setResponseMsg(msg);
				}
				if (respBody.has(Constants.MIHPAYID)) {

					String mihpayid = respBody.get(Constants.MIHPAYID).toString();
					transaction.setMihPayuId(mihpayid);
				}
				if (respBody.has(Constants.ERROR_CODE)) {

					String error_code = respBody.get(Constants.ERROR_CODE).toString();
					transaction.setResponseCode(error_code);
				}
			}
		}

		return transaction;

	}

	public TransactionConverter() {

	}

	
	private static void log(String message){
		message = Pattern.compile("(ccexpyr\":\")([\\s\\S]*?)(\",\")").matcher(message).replaceAll("$1$3");
		message = Pattern.compile("(ccexpmon\":\")([\\s\\S]*?)(\",\")").matcher(message).replaceAll("$1$3");
		message = Pattern.compile("(ccnum\":\")([\\s\\S]*?)(\",\")").matcher(message).replaceAll("$1$3");
		message = Pattern.compile("(ccvv\":\")([\\s\\S]*?)(\",\")").matcher(message).replaceAll("$1$3");
		logger.info(message);
	}
	
	
}
