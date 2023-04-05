package com.paymentgateway.yesbankcb;

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
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.TransactionType;

@Service
public class YesBankCbIntegrator {
	private static Logger logger = LoggerFactory.getLogger(YesBankCbIntegrator.class.getName());

	@Autowired
	@Qualifier("yesBankCbTransactionConverter")
	private TransactionConverter converter;

	@Autowired
	@Qualifier("yesBankCbFactory")
	private TransactionFactory transactionFactory;

	@Autowired
	@Qualifier("yesBankCbTransformer")
	private YesBankCbTransformer upiTransformer;

	@Autowired
	@Qualifier("yesBankCbTransactionCommunicator")
	private TransactionCommunicator communicator;
	public void process(Fields fields) throws SystemException {
		transactionFactory.getInstance(fields);

		String vpaFlag = PropertiesManager.propertiesMap.get(Constants.VPA_VAIDATION_FLAG);
		String transactionType = fields.get(FieldType.TXNTYPE.getName());

		if (vpaFlag.equalsIgnoreCase(Constants.Y_FLAG) && transactionType.equals(TransactionType.SALE.getName())) {

			String vpaValidationResp = vpaValidation(fields);

			if (StringUtils.isNotBlank(vpaValidationResp)) {
				if (vpaValidationResp.contains(Constants.VPA_FAILURE_RES)) {
					send(fields);
				} else if (vpaValidationResp.equalsIgnoreCase(Constants.VPA_SUCCESSFULLY_STATUS_CODE)) {
					send(fields);
				} else if (vpaValidationResp.equalsIgnoreCase(Constants.VPA_INVALID_STATUS_CODE)) {

					upiTransformer.updateInvalidVpaResponse(fields, vpaValidationResp);
				} else {
					upiTransformer.updateInvalidVpaResponse(fields, vpaValidationResp);
				}
			}
		} else {
			send(fields);
		}
	}

	public String vpaValidation(Fields fields) throws SystemException {

		String vpaStatus = "";
		JSONObject vpaRequest = converter.vpaValidatorRequest(fields);
		String encryptedVpaResponse = communicator.getVpaResponse(vpaRequest, fields);

		if (StringUtils.isNotBlank(encryptedVpaResponse)) {

			if (encryptedVpaResponse.contains(Constants.VPA_FAILURE_RES)) {
				vpaStatus = Constants.VPA_FAILURE_RES;
			} else {
				vpaStatus = converter.toVpaTransaction(encryptedVpaResponse, fields);
			}

		} else {
			logger.info("VPA response received is blank, continue collect request without VPA Validation");
			vpaStatus = Constants.VPA_SUCCESSFULLY_STATUS_CODE;

		}

		return vpaStatus;
	}

	public void send(Fields fields) throws SystemException {
		Transaction transactionResponse = new Transaction();
		JSONObject request = converter.perpareRequest(fields);
		String encryptedResponse = communicator.getResponse(request, fields);

		switch (TransactionType.getInstance(fields.get(FieldType.TXNTYPE.getName()))) {
		case SALE:

			if (StringUtils.isNotBlank(encryptedResponse)) {
				if (encryptedResponse.contains(Constants.COLLECT_FAILURE_RES)) {

					logger.info("Collect API  collect failure Response, if response is decrypted=  " + encryptedResponse
							+ " For Order Id " + fields.get(FieldType.ORDER_ID.getName()));

					transactionResponse = converter.toTransactionCollectFailureRes(encryptedResponse, fields);
					upiTransformer.updateResponse(fields, transactionResponse);
					break;
				} else {
					transactionResponse = converter.toTransaction(encryptedResponse, fields);
					upiTransformer.updateResponse(fields, transactionResponse);
					break;
				}

			} else {
				logger.info("Yes bank Collect API Response is blank for Order Id "
						+ fields.get(FieldType.ORDER_ID.getName()));
				upiTransformer.updateResponse(fields, transactionResponse);
				break;
			}

		case REFUND:
			if (StringUtils.isNotBlank(encryptedResponse)) {
				if (encryptedResponse.contains(Constants.REFUND_FAILURE_RES)
						|| encryptedResponse.contains(Constants.REFUND_FAILURE)
						|| encryptedResponse.contains(Constants.REFUND_RESCODE_MC04)) {
					logger.info("Yes Bank UPI Refund Failed response " + encryptedResponse + " for Order Id "
							+ fields.get(FieldType.ORDER_ID.getName()));

					transactionResponse = converter.toTransactionFailureRes(encryptedResponse, fields);
					upiTransformer.updateResponse(fields, transactionResponse);
					break;
				} else {

					if (encryptedResponse.contains(Constants.REFUND_FAILURE_RES_PENDING)) {
						transactionResponse = converter.toTransactionRefundFail(encryptedResponse, fields);

						logger.info("Yes Bank UPI Refund Failed response " + encryptedResponse + " for Order Id "
								+ fields.get(FieldType.ORDER_ID.getName()));
						upiTransformer.updateResponse(fields, transactionResponse);
						break;
					} else {
						transactionResponse = converter.toTransaction(encryptedResponse, fields);
						logger.info("Yes Bank UPI Refund response " + encryptedResponse + " for Order Id "
								+ fields.get(FieldType.ORDER_ID.getName()));

						upiTransformer.updateResponse(fields, transactionResponse);
						break;
					}
				}
			} else {
				logger.info("Yes Bank UPI Refund response is blank for Order Id "
						+ fields.get(FieldType.ORDER_ID.getName()));
				upiTransformer.updateResponse(fields, transactionResponse);
				break;
			}

		case ENQUIRY:
			if (StringUtils.isNotBlank(encryptedResponse)) {
			
				transactionResponse = converter.toTransactionStatusEnquiry(encryptedResponse, fields);

				upiTransformer.updateResponse(fields, transactionResponse);
				break;
			} else {
				logger.info("Yes Bank UPI Status response is blank for Order Id "
						+ fields.get(FieldType.ORDER_ID.getName()));
				upiTransformer.updateResponse(fields, transactionResponse);
				break;
			}
		default:
			break;
		}
	}
}
