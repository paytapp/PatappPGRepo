package com.paymentgateway.safexpay;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.JsonObject;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PaymentType;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.TransactionType;
import com.paymentgateway.pg.core.util.PayGateCryptoUtils;

@Service("safexpayTransactionConverter")
public class TransactionConverter {

	private static final Logger logger = LoggerFactory.getLogger(TransactionConverter.class.getName());
	private static final String SEPARATOR = "|";

	@Autowired
	private PayGateCryptoUtils payGateCryptoUtils;
	
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
		return request.toString();

	}

	public String saleRequest(Fields fields, Transaction transaction) throws SystemException {

		try {

			String saleUrl = PropertiesManager.propertiesMap.get(Constants.SAFEXPAY_SALE_REQUEST_URL);
			
			StringBuilder txn_details = new StringBuilder();
			StringBuilder pg_details = new StringBuilder();
			StringBuilder card_details = new StringBuilder();
			StringBuilder cust_details = new StringBuilder();
			StringBuilder bill_details = new StringBuilder();
			StringBuilder ship_details = new StringBuilder();
			StringBuilder item_details = new StringBuilder();
			StringBuilder upi_details = new StringBuilder();
			StringBuilder other_details = new StringBuilder();

			txn_details.append(transaction.getAg_id());
			txn_details.append(SEPARATOR);
			txn_details.append(transaction.getMe_id());
			txn_details.append(SEPARATOR);
			txn_details.append(transaction.getOrder_no());
			txn_details.append(SEPARATOR);
			txn_details.append(transaction.getAmount());
			txn_details.append(SEPARATOR);
			txn_details.append(transaction.getCountry());
			txn_details.append(SEPARATOR);
			txn_details.append(transaction.getCurrency());
			txn_details.append(SEPARATOR);
			txn_details.append(transaction.getTxn_type());
			txn_details.append(SEPARATOR);
			txn_details.append(transaction.getSuccess_url());
			txn_details.append(SEPARATOR);
			txn_details.append(transaction.getFailure_url());
			txn_details.append(SEPARATOR);
			txn_details.append(transaction.getChannel());

			
			pg_details.append(transaction.getPg_id());
			pg_details.append(SEPARATOR);
			pg_details.append(transaction.getPaymode());
			pg_details.append(SEPARATOR);
			pg_details.append(transaction.getScheme());
			pg_details.append(SEPARATOR);
			pg_details.append(transaction.getEmi_months());

			
			card_details.append(transaction.getCard_no());
			card_details.append(SEPARATOR);
			card_details.append(transaction.getExp_month());
			card_details.append(SEPARATOR);
			card_details.append(transaction.getExp_year());
			card_details.append(SEPARATOR);
			card_details.append(transaction.getCvv());
			card_details.append(SEPARATOR);
			card_details.append(transaction.getCard_name());

			
			cust_details.append(transaction.getCust_name());
			cust_details.append(SEPARATOR);
			cust_details.append(transaction.getEmail_id());
			cust_details.append(SEPARATOR);
			cust_details.append(transaction.getMobile_no());
			cust_details.append(SEPARATOR);
			cust_details.append(transaction.getUnique_id());
			cust_details.append(SEPARATOR);
			cust_details.append(transaction.getIs_logged_in());

			bill_details.append(transaction.getBill_address());
			bill_details.append(SEPARATOR);
			bill_details.append(transaction.getBill_city());
			bill_details.append(SEPARATOR);
			bill_details.append(transaction.getBill_state());
			bill_details.append(SEPARATOR);
			bill_details.append(transaction.getBill_country());
			bill_details.append(SEPARATOR);
			bill_details.append(transaction.getBill_zip());

			
			ship_details.append(transaction.getShip_address());
			ship_details.append(SEPARATOR);
			ship_details.append(transaction.getShip_city());
			ship_details.append(SEPARATOR);
			ship_details.append(transaction.getShip_state());
			ship_details.append(SEPARATOR);
			ship_details.append(transaction.getShip_country());
			ship_details.append(SEPARATOR);
			ship_details.append(transaction.getShip_zip());
			ship_details.append(SEPARATOR);
			ship_details.append(transaction.getShip_days());
			ship_details.append(SEPARATOR);
			ship_details.append(transaction.getAddress_count());

			
			item_details.append(transaction.getItem_count());
			item_details.append(SEPARATOR);
			item_details.append(transaction.getItem_value());
			item_details.append(SEPARATOR);
			item_details.append(transaction.getItem_category());

			other_details.append(transaction.getUdf_1());
			other_details.append(SEPARATOR);
			other_details.append(transaction.getUdf_2());
			other_details.append(SEPARATOR);
			other_details.append(transaction.getUdf_3());
			other_details.append(SEPARATOR);
			other_details.append(transaction.getUdf_4());
			other_details.append(SEPARATOR);
			other_details.append(transaction.getUdf_5());
			
			if (fields.get(FieldType.PAYMENT_TYPE.getName()).equalsIgnoreCase(PaymentType.UPI.getCode())) {
				
				upi_details.append(fields.get(FieldType.PAYER_ADDRESS.getName()));
				fields.put(FieldType.CARD_MASK.getName(),fields.get(FieldType.PAYER_ADDRESS.getName()));
			}
			
			
			
			logger.info("Safexpay Transaction Request Params for Pg Ref Num == " + transaction.getOrder_no() +" , txn_details == " +txn_details
					+ " , pg_details == " +pg_details + " , card_details == " +" *******Card Details Hidden******"+ " , cust_details == " +cust_details
					+ " , bill_details == " +bill_details + " , ship_details == " +ship_details + " , item_details == " +item_details 
					+ " , other_details == " +other_details+ " , upi_details == " +upi_details);
			
			
			String txn_details_enc = payGateCryptoUtils.encrypt(txn_details.toString(), transaction.getTxnKey());
			String pg_details_enc = payGateCryptoUtils.encrypt(pg_details.toString(), transaction.getTxnKey());
			String card_details_enc = payGateCryptoUtils.encrypt(card_details.toString(), transaction.getTxnKey());
			String cust_details_enc = payGateCryptoUtils.encrypt(cust_details.toString(), transaction.getTxnKey());
			String bill_details_enc = payGateCryptoUtils.encrypt(bill_details.toString(), transaction.getTxnKey());
			String ship_details_enc = payGateCryptoUtils.encrypt(ship_details.toString(), transaction.getTxnKey());
			String item_details_enc = payGateCryptoUtils.encrypt(item_details.toString(), transaction.getTxnKey());
			String other_details_enc = payGateCryptoUtils.encrypt(other_details.toString(), transaction.getTxnKey());
			String upi_details_enc = null;
			
			if (StringUtils.isNotBlank(upi_details.toString())) {
				upi_details_enc = payGateCryptoUtils.encrypt(other_details.toString(), transaction.getTxnKey());
			}

			StringBuilder outputHtml = new StringBuilder();
			outputHtml.append("<html>");
			outputHtml.append("<head>");
			outputHtml.append("<title>Payment GateWay Merchant Checkout Page</title>");
			outputHtml.append("</head>");
			outputHtml.append("<body>");
			outputHtml.append("<center><h1>Please do not refresh this page...</h1></center>");
			outputHtml.append("<form method='post' action='" + saleUrl + "' name='payment_form'>");

			outputHtml.append("<input type='hidden' name='me_id' value='" + transaction.getMe_id() + "'>");
			outputHtml.append("<input type='hidden' name='txn_details' value='" + txn_details_enc + "'>");
			outputHtml.append("<input type='hidden' name='pg_details' value='" + pg_details_enc + "'>");
			outputHtml.append("<input type='hidden' name='card_details' value='" + card_details_enc + "'>");
			outputHtml.append("<input type='hidden' name='cust_details' value='" + cust_details_enc + "'>");
			outputHtml.append("<input type='hidden' name='bill_details' value='" + bill_details_enc + "'>");
			outputHtml.append("<input type='hidden' name='ship_details' value='" + ship_details_enc + "'>");
			outputHtml.append("<input type='hidden' name='item_details' value='" + item_details_enc + "'>");
			outputHtml.append("<input type='hidden' name='other_details' value='" + other_details_enc + "'>");
			
			if (StringUtils.isNotBlank(upi_details_enc)) {
				outputHtml.append("<input type='hidden' name='upi_details' value='" + upi_details_enc + "'>");
			}
			
			outputHtml.append("</form>");
			outputHtml.append("<script type='text/javascript'>");
			outputHtml.append("document.payment_form.submit();");
			outputHtml.append("</script>");
			outputHtml.append("</body>");
			outputHtml.append("</html>");

			logger.info("Safexpay Transaction Final Submission form >>> " + outputHtml.toString());
			
			return outputHtml.toString();
		}

		catch (Exception exception) {
			logger.error("Exception in generating Payu sale request ", exception);
		}

		return null;
	}

	public String refundRequest(Fields fields, Transaction transaction) throws SystemException {

		try {

			JsonObject refRequest = new JsonObject();

			refRequest.addProperty("ag_id", fields.get(FieldType.ADF1.getName()).toString());
			refRequest.addProperty("me_id", fields.get(FieldType.MERCHANT_ID.getName()).toString());
			refRequest.addProperty("ag_ref", fields.get(FieldType.ACQ_ID.getName()).toString());
			refRequest.addProperty("refund_amount", transaction.getAmount());
			refRequest.addProperty("refund_reason", "CustomerRequestForRefund");
			
			logger.info("Safex Pay refund Request before encryption for Order Id = " + fields.get(FieldType.ORDER_ID.getName()) +"  >>  " +refRequest.toString());
			refRequest.addProperty("ag_ref",
					payGateCryptoUtils.encrypt(fields.get(FieldType.ACQ_ID.getName()).toString(),
							fields.get(FieldType.TXN_KEY.getName()).toString()));

			refRequest.addProperty("refund_amount", payGateCryptoUtils.encrypt(transaction.getAmount(),
					fields.get(FieldType.TXN_KEY.getName()).toString()));
			refRequest.addProperty("refund_reason", payGateCryptoUtils.encrypt("CustomerRequestForRefund",
					fields.get(FieldType.TXN_KEY.getName()).toString()));
			
			logger.info("Safex Pay refund Request after encryption for Order Id = " + fields.get(FieldType.ORDER_ID.getName()) +"  >>  " +refRequest.toString());
			return refRequest.toString();
			
		}

		catch (Exception e) {
			logger.error("Exception in generating safexpay refund request", e);
			return null;
		}

	}

	public Transaction toTransaction(String jsonResponse, Fields fields) {

		Transaction transaction = new Transaction();

		try {

			JSONObject respObj = new JSONObject(jsonResponse);
			
			transaction.setAg_ref(respObj.get("ag_ref").toString());
			transaction.setPg_ref(respObj.get("pg_ref").toString());
			transaction.setRes_code(respObj.get("res_code").toString());
			transaction.setRes_message(respObj.get("res_message").toString());
			transaction.setStatus(respObj.get("status").toString());
			transaction.setRefund_ref(respObj.get("refund_ref").toString());

		} catch (Exception e) {
			logger.error("Exception in parsing status enquiry response for safexpay" , e);
		}

		return transaction;

	}

	public TransactionConverter() {

	}

}
