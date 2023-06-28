package com.paymentgateway.upigateway;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.dao.UserSettingDao;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.user.UserSettingData;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.TransactionType;
import com.paymentgateway.pg.core.util.AcquirerTxnAmountProvider;

/**
 * @author Shaiwal
 *
 */
@Service("upigatewayTransactionConverter")
public class TransactionConverter {

	@Autowired
	private UserSettingDao userSettingDao;

	@Autowired
	private AcquirerTxnAmountProvider acquirerTxnAmountProvider;

	private static final Logger logger = LoggerFactory.getLogger(TransactionConverter.class.getName());

	@SuppressWarnings("incomplete-switch")
	public String perpareRequest(Fields fields, Transaction transaction) throws SystemException {

		String request = null;

		switch (TransactionType.getInstance(fields.get(FieldType.TXNTYPE.getName()))) {
		case SALE:
			request = saleRequest(fields, transaction);
			break;
		case STATUS:
			request = statusEnquiryRequest(fields, transaction);
			break;
		}
		return request;

	}

	public String saleRequest(Fields fields, Transaction transaction) throws SystemException {

		logger.info("Preparing UPIGateway payment request");

		JSONObject jsonRequest = new JSONObject();

		String returnUrl = null;
		UserSettingData userSetting = userSettingDao.fetchDataUsingPayId((fields.get(FieldType.PAY_ID.getName())));
		if (userSetting.isAllowCustomHostedUrl()) {
			returnUrl = (userSetting.getCustomHostedUrl() + "/pgui/jsp/upiGatewayResponse");
		} else {
			returnUrl = PropertiesManager.propertiesMap.get(Constants.UPIGATEWAY_RETURN_URL);
		}

		String amount = acquirerTxnAmountProvider.amountProvider(fields);
		transaction.setAmount(amount);

		jsonRequest.put(Constants.key, transaction.getKey());
		jsonRequest.put(Constants.client_txn_id, transaction.getClient_txn_id());
		jsonRequest.put(Constants.amount, transaction.getAmount());
		jsonRequest.put(Constants.p_info, transaction.getP_info());
		jsonRequest.put(Constants.customer_name, transaction.getCustomer_name());
		jsonRequest.put(Constants.customer_email, transaction.getCustomer_email());
		jsonRequest.put(Constants.customer_mobile, transaction.getCustomer_mobile());
		jsonRequest.put(Constants.redirect_url, returnUrl);
		jsonRequest.put(Constants.udf1, "");
		jsonRequest.put(Constants.udf2, "");
		jsonRequest.put(Constants.udf3, "");

		logger.info("Prepared UPIGateway payment request : {}", jsonRequest.toString());

		return jsonRequest.toString();

	}

	public String refundRequest(Fields fields, Transaction transaction) throws SystemException {
		// No Refund API for UPIGateway

		JSONArray requestArray = new JSONArray();
		return requestArray.toString();
	}

	public String statusEnquiryRequest(Fields fields, Transaction transaction) throws SystemException {

		JSONObject jsonRequest = new JSONObject();
		jsonRequest.put(Constants.key, transaction.getKey());
		jsonRequest.put(Constants.client_txn_id, transaction.getClient_txn_id());
		jsonRequest.put(Constants.txn_date, transaction.getTxn_date());
		return jsonRequest.toString();
	}

	public Transaction toTransaction(String response, Fields fields) {

		Transaction transaction = new Transaction();
		try {

			JSONObject resJson = new JSONObject(response);
			transaction.setClient_txn_id(resJson.get(FieldType.PG_REF_NUM.getName()).toString());
		}

		catch (Exception e) {
			logger.error("Exception", e);
		}

		return transaction;
	}// toTransaction()

	public Transaction toTransactionRefund(String response) {

		Transaction transaction = new Transaction();
		return transaction;
	}

	public Transaction toStatusTransaction(String response) {

		Transaction transaction = new Transaction();
		try {
			JSONObject resJson = new JSONObject(response);

			String status = null;
			if (response.contains(Constants.status)) {
				status = resJson.get(Constants.status).toString();
			}

			if (StringUtils.isNotBlank(status) && status.equalsIgnoreCase("true")) {

				transaction.setMsg(resJson.get(Constants.msg).toString());

				JSONObject dataJson = resJson.getJSONObject(Constants.data);

				if (dataJson.has(Constants.id)) {
					transaction.setId(dataJson.get(Constants.id).toString());
				}

				if (dataJson.has(Constants.amount)) {
					transaction.setAmount(dataJson.get(Constants.amount).toString());
				}

				if (dataJson.has(Constants.client_txn_id)) {
					transaction.setClient_txn_id(dataJson.get(Constants.client_txn_id).toString());
				}

				if (dataJson.has(Constants.customer_name)) {
					transaction.setCustomer_name(dataJson.get(Constants.customer_name).toString());
				}

				if (dataJson.has(Constants.customer_email)) {
					transaction.setCustomer_email(dataJson.get(Constants.customer_email).toString());
				}
				if (dataJson.has(Constants.customer_mobile)) {
					transaction.setCustomer_mobile(dataJson.get(Constants.customer_mobile).toString());
				}
				if (dataJson.has(Constants.upi_txn_id)) {
					transaction.setUpi_txn_id(dataJson.get(Constants.upi_txn_id).toString());
				}
				if (dataJson.has(Constants.status)) {
					transaction.setStatus(dataJson.get(Constants.status).toString());
				}
				if (dataJson.has(Constants.remark)) {
					transaction.setRemark(dataJson.get(Constants.remark).toString());
				}

			} else {
				transaction.setStatus("false");
				transaction.setMsg("Record not found");
			}

		} catch (Exception e) {
			logger.error("Exception ", e);
		}

		return transaction;
	}// toTransaction()

	public String getPaymentUrl(String response, Fields fields) {
		String paymentUrl = null;
		try {

			JSONObject resJson = new JSONObject(response);

			String status = null;
			if (response.contains(Constants.status)) {
				status = resJson.get(Constants.status).toString();
			}

			if (status.equalsIgnoreCase("true")) {

				if (response.contains(Constants.data)) {
					fields.put(FieldType.AUTH_CODE.getName(), resJson.get(Constants.data).toString());
				}
				if (response.contains(Constants.data)) {

					JSONObject dataJson = resJson.getJSONObject(Constants.data);

					if (dataJson.has(Constants.order_id)) {
						fields.put(FieldType.ACQ_ID.getName(), dataJson.get(Constants.order_id).toString());
					}

					if (dataJson.has(Constants.payment_url)) {
						paymentUrl = dataJson.get(Constants.payment_url).toString();
					}

				}

			} else {
				logger.info("UPIGateway payment Response does not contain payment URL , {} ", response);

				if (response.contains(Constants.msg)) {
					fields.put(FieldType.PG_RESPONSE_MSG.getName(), resJson.get(Constants.msg).toString());
				}
			}

			return paymentUrl;
		} catch (Exception e) {
			logger.error("Exception in parsing UPIGateway payment response to get payment URL ", e);
		}
		return paymentUrl;

	}

}
