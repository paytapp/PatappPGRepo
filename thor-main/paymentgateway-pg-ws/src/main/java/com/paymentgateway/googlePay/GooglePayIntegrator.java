package com.paymentgateway.googlePay;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.StatusType;

/**
 * @author vj
 *
 */
@Service
public class GooglePayIntegrator {
	private static Logger logger = LoggerFactory.getLogger(GooglePayIntegrator.class.getName());

	@Autowired
	@Qualifier("googlePayFactory")
	private TransactionFactory transactionFactory;

	@Autowired
	@Qualifier("googlePayTransactionConverter")
	private TransactionConverter converter;

	@Autowired
	@Qualifier("googlePayTransactionCommunicator")
	private TransactionCommunicator communicator;

	public void process(Fields fields) throws SystemException {
		transactionFactory.getInstance(fields);

		JSONObject accessTokenResponse = communicator.createAccessToken();
		logger.info("response of accessTokenResponse" + accessTokenResponse);
		if (accessTokenResponse.length() > 0) {
			if (accessTokenResponse.has(Constants.ACCESS_TOKEN)) {
				String accessToken = accessTokenResponse.getString(Constants.ACCESS_TOKEN);
				send(fields, accessToken);
			} else if (accessTokenResponse.has(Constants.TOKEN_FAILURE)) {
				communicator.updateGpayTokenFailureResponse(fields, accessTokenResponse);
			}
		}

	}

	public void send(Fields fields, String accessTokenResponse) throws SystemException {
		JSONObject authoriseRequest = converter.authoriseRequest(fields, accessTokenResponse);
		logger.info("Push notification request for googlePay" + authoriseRequest);

		JSONObject response = communicator.getResponse(authoriseRequest, fields, accessTokenResponse);
		logger.info("Push notification response for googlePay" + response);
		if (response.length() == 0) {
			communicator.updateSaleResponse(fields, response);
		} else if (response.length() > 0) {
			communicator.updateInvalidVpaResponse(fields, response);
		}

	}

}
