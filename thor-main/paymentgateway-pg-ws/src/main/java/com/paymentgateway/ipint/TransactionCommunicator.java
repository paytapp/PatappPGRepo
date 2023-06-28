package com.paymentgateway.ipint;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StatusType;

@Service("ipintTransactionCommunicator")
public class TransactionCommunicator {

	private static Logger logger = LoggerFactory.getLogger(TransactionCommunicator.class.getName());

	public void updateSaleResponse(Fields fields, Transaction request , Transaction error) {
		if(StringUtils.isNotBlank(request.getSessionId())) {
			String saleUrl = PropertiesManager.propertiesMap.get(Constants.SALE_URL);
			saleUrl = saleUrl + request.getSessionId();
			fields.put(FieldType.IPINT_FINAL_REQUEST.getName(), saleUrl);
			fields.put(FieldType.STATUS.getName(), StatusType.SENT_TO_BANK.getName());
			fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.SUCCESS.getResponseCode());
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.SUCCESS.getResponseMessage());
			fields.put(FieldType.ACQ_ID.getName(), request.getSessionId());
		}else {
			fields.put(FieldType.STATUS.getName(), StatusType.FAILED.getName());
			fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.FAILED.getResponseCode());
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), error.getResultMsg()!=null?error.getResultMsg(): ErrorType.FAILED.getResponseMessage());
			
		}

	}

	public String getResponse(String request, Fields fields) throws SystemException {
		String serviceUrl = PropertiesManager.propertiesMap.get(Constants.SESSION_URL);
		try {
			CloseableHttpClient httpClient = HttpClientBuilder.create().build();
			HttpPost req = new HttpPost(serviceUrl);
			StringEntity params = new StringEntity(request);
			req.addHeader(Constants.CONTENT_TYPE, Constants.APPLICATION_JSON);
			req.addHeader(Constants.API_KEY, fields.get(FieldType.TXN_KEY.getName()));
			req.setEntity(params);
			HttpResponse resp = httpClient.execute(req);
			String responseBody = EntityUtils.toString(resp.getEntity());
			return responseBody;
		} catch (Exception exception) {
			logger.error("exception is ", exception);
			return null;
		}

	}
}
