package com.paymentgateway.idfcUpi;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.TransactionType;

/**
 * @author VJ
 *
 */
@Service("idfcUpiIntegrator")
public class IdfcUpiIntegrator {
	private static Logger logger = LoggerFactory.getLogger(IdfcUpiIntegrator.class.getName());

	@Autowired
	@Qualifier("idfcUpiFactory")
	private TransactionFactory transactionFactory;
	@Autowired
	@Qualifier("idfcUpiTransactionConverter")
	private TransactionConverter converter;

	@Autowired
	@Qualifier("idfcUpiTransactionCommunicator")
	private TransactionCommunicator communicator;

	@Autowired
	@Qualifier("idfcUpiTransformer")
	private IdfcUpiTransformer upiTransformer;

	public void process(Fields fields) throws SystemException {
		Transaction transactionResponse = new Transaction();

		transactionFactory.getInstance(fields);
		String encryptedDEK = fields.get(FieldType.ADF6.getName());
		String transactionType = fields.get(FieldType.TXNTYPE.getName());
		if (transactionType.equals(TransactionType.SALE.getName())) {
			JSONObject vpaStatus = vpaValidation(fields, encryptedDEK);
			transactionResponse = converter.toVpaTransaction(vpaStatus, fields);
			if (StringUtils.isNotBlank(transactionResponse.getStatus())
					&& transactionResponse.getStatus().equals(Constants.SUCCESS_RESPONSE)) {
				send(fields, encryptedDEK);
			} else if ((StringUtils.isNotBlank(transactionResponse.getStatus())
					&& transactionResponse.getStatus().equals(Constants.VPA_FAILURE_RESPONSE))
					|| transactionResponse.getStatus().equals(Constants.VPA_FAILURE_RESPONSE_U17)) {
				upiTransformer.updateInvalidVpaResponse(fields, vpaStatus.toString());
			} else {
				upiTransformer.updateResponse(fields, transactionResponse);
			}

		} else {
			send(fields, encryptedDEK);
		}
	}

	public JSONObject vpaValidation(Fields fields, String encryptedDEK) throws SystemException {
		JSONObject vpaRequest = converter.vpaValidatorRequest(fields, encryptedDEK);
		JSONObject VpaResponse = communicator.getVpaResponse(vpaRequest, fields);
		logger.info("vpa validation API response" + fields.get(FieldType.TXNTYPE.getName()) + " " + "Txn id "
				+ fields.get(FieldType.TXN_ID.getName()) + " " + VpaResponse.toString());

		return VpaResponse;
	}

	public void send(Fields fields, String encryptedDEK) throws SystemException {
		Transaction transactionResponse = new Transaction();
		JSONObject request = converter.perpareRequest(fields, encryptedDEK);
		JSONObject jsonResponse = communicator.getResponse(request, fields);
		switch (TransactionType.getInstance(fields.get(FieldType.TXNTYPE.getName()))) {
		case SALE:
			if (StringUtils.isNotBlank(jsonResponse.toString())) {
				transactionResponse = converter.toTransaction(jsonResponse, fields);
				upiTransformer.updateResponse(fields, transactionResponse);
				break;
			} else {
				logger.info(
						"Collect API  Collect Response, if response is blank " + fields.get(FieldType.TXNTYPE.getName())
								+ " " + "Txn id " + fields.get(FieldType.TXN_ID.getName()) + " " + jsonResponse);
				upiTransformer.updateResponse(fields, transactionResponse);
				break;
			}
		case REFUND:
			if (StringUtils.isNotBlank(jsonResponse.toString())) {
				String code = jsonResponse.getString(Constants.RES_CODE);
				logger.info("Code received in refeund response, "+code);
				if (code.equalsIgnoreCase(Constants.REFUND_FAILURE_CODE)) {
					transactionResponse = converter.toTransactionFailureRes(jsonResponse, fields);
				} else if (code.equalsIgnoreCase(Constants.REFUND_SUCCESS_CODE)) {
					transactionResponse = converter.toRefundTransaction(jsonResponse, fields);
				} else {
					transactionResponse = converter.toTransactionFailureRes(jsonResponse, fields);
				}

				upiTransformer.updateResponse(fields, transactionResponse);
				break;
			} else {
				logger.info("Collect API  REFUND Response, if response is blank = "
						+ fields.get(FieldType.TXNTYPE.getName()) + " " + "Txn id "
						+ fields.get(FieldType.TXN_ID.getName()) + " " + jsonResponse.toString());
				upiTransformer.updateResponse(fields, transactionResponse);
				break;
			}

		case ENQUIRY:
			if (StringUtils.isNotBlank(jsonResponse.toString())) {
				transactionResponse = converter.toTransactionStatusEnquiry(jsonResponse, fields);
				upiTransformer.updateResponse(fields, transactionResponse);
				break;
			} else {
				logger.info("Collect API  Status enquiry Response, if response is blank = "
						+ fields.get(FieldType.TXNTYPE.getName()) + " " + "Txn id "
						+ fields.get(FieldType.TXN_ID.getName()) + " " + jsonResponse.toString());
				upiTransformer.updateResponse(fields, transactionResponse);
				break;
			}
		default:
			break;
		}
	}
}