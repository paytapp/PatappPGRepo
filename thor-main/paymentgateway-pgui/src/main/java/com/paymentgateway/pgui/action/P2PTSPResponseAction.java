package com.paymentgateway.pgui.action;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.api.TransactionControllerServiceProvider;
import com.paymentgateway.commons.dao.FieldsDao;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.StatusType;

@Service
public class P2PTSPResponseAction {

	private static Logger logger = LoggerFactory.getLogger(P2PTSPResponseAction.class.getName());

	@Autowired
	private TransactionControllerServiceProvider transactionControllerServiceProvider;

	@Autowired
	private FieldsDao fieldsDao;

	public void p2pTSPResponseHandler(HttpServletRequest httpRequest, HttpServletResponse httpResponse, String data)
			throws IOException {

		try {
			JSONObject JsonData = new JSONObject(data);

			logger.info("P2PTSP ResponseHandler " + JsonData);

			Fields fields = new Fields();
			if (JsonData.has("trackId") && JsonData.has("response_code")) {
				fields = fieldsDao.getPreviousForPgRefNum(JsonData.get("trackId").toString());
				if (JsonData.get("response_code").toString().equalsIgnoreCase("000")) {
					fields.put(FieldType.RRN.getName(), JsonData.get("utr").toString());
					fields.put(FieldType.STATUS.getName(), StatusType.CAPTURED.getName());
					fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.SUCCESS.getCode());
					fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.SUCCESS.getResponseMessage());
				} else {
					fields.put(FieldType.STATUS.getName(), StatusType.FAILED.getName());
					fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.FAILED.getCode());
					fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.FAILED.getResponseMessage());
				}

				logger.info("P2pTSP response fields to transact " + fields.getFieldsAsString());
				transactionControllerServiceProvider.transact(fields, Constants.TXN_WS_UPI_PROCESSOR.getValue());

				logger.info("P2pTSP response fields to transact db entry successfuly");

			}

		} catch (Exception e) {
			logger.error("Error in P2pTSP callback = ", e);

		}

	}

}
