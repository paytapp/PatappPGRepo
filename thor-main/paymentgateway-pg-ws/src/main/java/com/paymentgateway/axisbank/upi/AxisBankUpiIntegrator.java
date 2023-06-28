package com.paymentgateway.axisbank.upi;

import java.util.HashMap;
import java.util.Map;

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
import com.paymentgateway.pg.core.util.AxisBankUpiEncDecService;

/**
 * @author Rahul
 *
 */

@Service
public class AxisBankUpiIntegrator {
	private static Logger logger = LoggerFactory.getLogger(AxisBankUpiIntegrator.class.getName());

	@Autowired
	@Qualifier("axisBankUpiTransactionConverter")
	private TransactionConverter converter;

	@Autowired
	@Qualifier("axisBankUpiFactory")
	private TransactionFactory transactionFactory;

	@Autowired
	@Qualifier("axisBankUpiTransformer")
	private AxisBankUpiTransformer axisBankUpiTransformer;

	@Autowired
	@Qualifier("axisBankUpiTransactionCommunicator")
	private TransactionCommunicator communicator;

	@Autowired
	private AxisBankUpiEncDecService checksumGenerator;

	public void process(Fields fields) throws SystemException {

		transactionFactory.getInstance(fields);
		String vpaFlag = PropertiesManager.propertiesMap.get(Constants.VPA_VAIDATION_FLAG);
		String allowOnFail = PropertiesManager.propertiesMap.get(Constants.ALLOW_COLLECT_ON_VPA_FAIL);

		String transactionType = fields.get(FieldType.TXNTYPE.getName());

		if (vpaFlag.equalsIgnoreCase(Constants.Y_FLAG) && transactionType.equals(TransactionType.SALE.getName())) {

			String vpaStatus = vpaValidation(fields);
			if (StringUtils.isNotBlank(vpaStatus)) {

				if (vpaStatus.contains(Constants.VPA_VAIDATION_FAILED)
						|| vpaStatus.contains(Constants.VPA_VAIDATION_INVALID)
						|| vpaStatus.contains(Constants.VPA_VAIDATION_REQ_FAILED)) {

					if (StringUtils.isNotBlank(allowOnFail) && allowOnFail.equalsIgnoreCase("Y")) {
						send(fields);
					} else {
						axisBankUpiTransformer.updateInvalidVpaResponse(fields, vpaStatus);
					}
				}

				else if (vpaStatus.equalsIgnoreCase(Constants.VPA_VAIDATION_SUCCESS)) {
					send(fields);
				}
			} else {
				send(fields);
			}
		} else {
			send(fields);
		}
	}

	public String vpaValidation(Fields fields) throws SystemException {

		String merchId = fields.get(FieldType.ADF4.getName());
		String merchChanId = fields.get(FieldType.ADF5.getName());
		String vpa = fields.get(FieldType.PAYER_ADDRESS.getName());
		String vpaStatus = "";
		String checksumString = merchId + merchChanId + vpa;

		try {
			String encryptedCheckSum = checksumGenerator.generateCheckSum(checksumString);

			Map<String, String> requestMap = new HashMap<String, String>();

			requestMap.put("merchId", merchId);
			requestMap.put("merchChanId", merchChanId);
			requestMap.put("customerVpa", vpa);
			requestMap.put("checkSum", encryptedCheckSum);

			JSONObject request = new JSONObject();
			request.put(Constants.MERCH_ID, requestMap.get(Constants.MERCH_ID));
			request.put(Constants.MERCH_CHAN_ID, requestMap.get(Constants.MERCH_CHAN_ID));
			request.put(Constants.CUSTOMER_VPA, requestMap.get(Constants.CUSTOMER_VPA));
			request.put(Constants.CHECKSUM, requestMap.get(Constants.CHECKSUM));

			String vpaValidationResponse = communicator.getVpaResponse(request, fields);

			logger.info("vpa validation API response >>> " + fields.get(FieldType.TXNTYPE.getName()) + " " + "Order Id >>> " 
					+ fields.get(FieldType.ORDER_ID.getName()) + " " + vpaValidationResponse);

			if (StringUtils.isNotBlank(vpaValidationResponse)) {

				JSONObject vpaResponse = new JSONObject(vpaValidationResponse);

				if (StringUtils.isNotBlank(vpaResponse.getString("code"))) {

					if (vpaResponse.getString("code").contains(Constants.VPA_VAIDATION_SUCCESS)) {
						vpaStatus = Constants.VPA_VAIDATION_SUCCESS;
					} 
					
					else if (vpaResponse.getString("code").contains(Constants.VPA_VAIDATION_INVALID)) {
						vpaStatus = Constants.VPA_VAIDATION_INVALID;
					} 
					else {
						vpaStatus = Constants.VPA_VAIDATION_FAILED;
					}
				}
			} else {
				vpaStatus = Constants.VPA_VAIDATION_FAILED;

			}
		}

		catch (Exception e) {
			logger.error("Error in VPA validaiton response parsing for Axis Bank ", e);
		}

		return vpaStatus;
	}

	public void send(Fields fields) throws SystemException {
		
		Transaction transactionResponse = new Transaction();
		String jsonResponse = null;
		JSONObject request = converter.perpareRequest(fields);

		
		if (fields.get(FieldType.TXNTYPE.getName()).equalsIgnoreCase(TransactionType.SALE.getName())) {
			String token = communicator.getToken(request, fields);
			
			if (StringUtils.isNotBlank(token)) {
				jsonResponse = communicator.getSaleResponse(token, fields);
			}
		}
		else if (fields.get(FieldType.TXNTYPE.getName()).equalsIgnoreCase(TransactionType.REFUND.getName())) {
			jsonResponse = communicator.getResponse(request.toString(),fields);
		}
		
		else if (fields.get(FieldType.TXNTYPE.getName()).equalsIgnoreCase(TransactionType.STATUS.getName())) {
			jsonResponse = communicator.getResponse(request.toString(),fields);
		}
		
		switch (TransactionType.getInstance(fields.get(FieldType.TXNTYPE.getName()))) {
		case SALE:
			if (StringUtils.isNotBlank(jsonResponse)) {
				if (jsonResponse.contains(Constants.CODE)) {
					transactionResponse = converter.toTransaction(jsonResponse, fields);
					axisBankUpiTransformer.updateSaleResponse(fields, transactionResponse);
					break;
				} else {
					transactionResponse = converter.toTransactionFailure(jsonResponse, fields);
					axisBankUpiTransformer.updateSaleResponse(fields, transactionResponse);
					break;
				}

			} else {
				axisBankUpiTransformer.updateSaleResponse(fields, transactionResponse);
				break;
			}

		case REFUND:
			if (StringUtils.isNotBlank(jsonResponse)) {
				if (jsonResponse.contains(Constants.CODE)) {
					transactionResponse = converter.toTransactionRefund(jsonResponse, fields);
					axisBankUpiTransformer.updateRefundResponse(fields, transactionResponse);
					break;
				} else {
					transactionResponse = converter.toTransactionFailure(jsonResponse, fields);
					axisBankUpiTransformer.updateRefundResponse(fields, transactionResponse);
					break;
				}
			} else {
				axisBankUpiTransformer.updateRefundResponse(fields, transactionResponse);
				break;
			}

		default:
			break;
		}
	}
}
