package com.paymentgateway.pgui.action;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.api.TransactionControllerServiceProvider;
import com.paymentgateway.commons.dao.EPOSTransactionDao;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.util.AcquirerType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TransactionType;
import com.paymentgateway.commons.util.threadpool.ThreadPoolProvider;
import com.paymentgateway.pg.core.util.KotakUpiUtils;
import com.paymentgateway.pg.core.util.UpiHistorian;

@Service
public class KotakUpiResponseAction {
	private static Logger logger = LoggerFactory.getLogger(KotakUpiResponseAction.class.getName());

	@Autowired
	TransactionControllerServiceProvider transactionControllerServiceProvider;

	@Autowired
	@Qualifier("kotakUpiUtil")
	private KotakUpiUtils kotakUpiUtil;

	@Autowired
	private UpiHistorian upiHistorian;

	@Autowired
	EPOSTransactionDao eposDao;

	public void kotakUpiResponseHandler(HttpServletRequest httpRequest, HttpServletResponse httpResponse)
			throws IOException {

		logger.info("Inside  KotakUpiResponseAction Execute");

		Fields responseField = null;
		try {

//			Object obj = JSONUtil.deserialize(httpRequest.getReader());
			StringBuilder buffer = new StringBuilder();
			BufferedReader reader = httpRequest.getReader();
			String line;
			while ((line = reader.readLine()) != null) {
				buffer.append(line);
			}
			// conveting object to json
//			ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
			String json = buffer.toString();
			JSONObject res = new JSONObject(json);
			String receivedCheckSum = res.getString(Constants.KOTAK_UPI_CHECKSUM.getValue());

			Fields fields = new Fields();

			logger.info("response received from bank kotak upi " + res);

			String generatedCheckSumResponse = kotakUpiUtil.matchChecksum(res);

			String responseCode = "";
			String payeeVpa = "";
			String responseMessage = "";
			String customerReference = "";
			String payerVpa = "";
			String pgTime = "";
			String remarks = "";
			String pgRefNum = "";
			String acqId = "";// to be changed according to requirement

			// matching call back checkSum response
			if (receivedCheckSum.equalsIgnoreCase(generatedCheckSumResponse)) {

				responseCode = res.getString(Constants.KOTAK_UPI_STATUS_CODE.getValue());
				payeeVpa = res.getString(Constants.KOTAK_UPI_PAYEEVPA.getValue());
				responseMessage = res.getString(Constants.KOTAK_UPI_STATUS.getValue());
				customerReference = res.getString(Constants.KOTAK_UPI_RRN.getValue());
				payerVpa = res.getString(Constants.KOTAK_UPI_PAYERVPA.getValue());
				pgTime = res.getString(Constants.KOTAK_UPI_TIMESTAMP.getValue());
				remarks = res.getString(Constants.KOTAK_UPI_REMARKS.getValue());
				acqId = res.getString(Constants.KOTAK_UPI_ACQID.getValue());

				String transactionid = res.getString(Constants.KOTAK_UPI_TRANSID.getValue());
				pgRefNum = transactionid.substring(8);

			} else {
				String transactionid = res.getString(Constants.KOTAK_UPI_TRANSID.getValue());
				pgRefNum = transactionid.substring(8);
				responseCode = Constants.KOTAK_UPI_CHECKSUM_FAILURE_CODE.getValue();
				responseMessage = Constants.KOTAK_UPI_CHECKSUM_FAILURE_RESPONSE.getValue();
			}

			String status = getStatusType(responseCode, responseMessage);
			ErrorType errorType = getErrorType(responseCode, responseMessage);

			logger.info("Merchant VPA kotak upi " + payeeVpa);

			fields.put(FieldType.ACQUIRER_TYPE.getName(), AcquirerType.KOTAK.getCode());
			fields.put(FieldType.PG_REF_NUM.getName(), pgRefNum);
			fields.put(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName());
			logger.info("fields before historian kotak upi " + fields.getFieldsAsString());
			upiHistorian.findPrevious(fields);
			logger.info("After historian kotak upi" + fields.getFieldsAsString());
			fields.put(FieldType.STATUS.getName(), status);
			fields.put(FieldType.RESPONSE_CODE.getName(), errorType.getResponseCode());
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), errorType.getResponseMessage());
			fields.put(FieldType.PG_RESP_CODE.getName(), responseCode);
			fields.put(FieldType.PG_TXN_STATUS.getName(), responseMessage);
			fields.put(FieldType.PG_TXN_MESSAGE.getName(), remarks);
			fields.put(FieldType.UDF1.getName(), payeeVpa);
			fields.put(FieldType.UDF3.getName(), payerVpa);

			fields.put(FieldType.RRN.getName(), customerReference);
			fields.put(FieldType.ACQ_ID.getName(), acqId);
			fields.put(FieldType.PG_DATE_TIME.getName(), pgTime);
			fields.put(FieldType.STATUS.getName(), status.toString());

			logger.info("fields send to transact " + fields.getFieldsAsString());

			Map<String, String> resp = transactionControllerServiceProvider.transact(fields,
					Constants.TXN_WS_UPI_PROCESSOR.getValue());
			responseField = new Fields(resp);

			Fields Fields = new Fields();
			Fields.put(FieldType.ORDER_ID.getName(), fields.get(FieldType.ORDER_ID.getName()));
			Fields.put(FieldType.STATUS.getName(), fields.get(FieldType.STATUS.getName()));
			Fields.put(FieldType.MOP_TYPE.getName(), fields.get(FieldType.MOP_TYPE.getName()));
			if (Boolean.valueOf(fields.get(FieldType.EPOS_MERCHANT.getName()))) {
				ExecutorService es = ThreadPoolProvider.getExecutorService();
				es.execute(new Runnable() {
					@Override
					public void run() {
						eposDao.updateEposCharges(Fields);
						Fields.removeInternalFields();
						Fields.removeSecureFields();
						Fields.remove(FieldType.ORIG_TXN_ID.getName());
						Fields.remove(FieldType.HASH.getName());
					}
				});
				es.shutdown();
			}
			logger.info("Response received from WS for kotak upi " + responseField);

		} catch (Exception e) {
			logger.error("Error in Kotak bank UPI callback = ", e);
			String path = httpRequest.getContextPath();
			logger.info(path);
			if (!StringUtils.isNotBlank(path) && !path.contains("pgui")) {
				String resultPath = httpRequest.getScheme() + "://" + httpRequest.getHeader("Host") + "/pgui/jsp/error";
				httpResponse.sendRedirect(resultPath);
			}
			httpResponse.sendRedirect("error");
		}

	}

	public String getStatusType(String receivedResponse, String receivedResponseMsg) {
		String status = null;
		if (receivedResponse.equals(Constants.KOTAK_UPI_SUCCESS_CODE.getValue())
				&& receivedResponseMsg.equals(Constants.KOTAK_UPI_RESPONSE.getValue())) {
			status = StatusType.CAPTURED.getName();
		} else if (receivedResponse.equals(Constants.KOTAK_UPI_CHECKSUM_FAILURE_CODE.getValue())
				&& receivedResponseMsg.equals(Constants.KOTAK_UPI_CHECKSUM_FAILURE_RESPONSE.getValue())) {
			status = StatusType.DENIED_BY_FRAUD.getName();
		}

		else {
			status = StatusType.REJECTED.getName();
		}

		return status;
	}

	public ErrorType getErrorType(String receivedResponse, String receivedResponseMsg) {
		ErrorType error = null;

		if (receivedResponse.equals(Constants.KOTAK_UPI_SUCCESS_CODE.getValue())
				&& receivedResponseMsg.equals(Constants.KOTAK_UPI_RESPONSE.getValue())) {
			error = ErrorType.SUCCESS;
		} else if (receivedResponse.equals(Constants.KOTAK_UPI_CHECKSUM_FAILURE_CODE.getValue())
				&& receivedResponseMsg.equals(Constants.KOTAK_UPI_CHECKSUM_FAILURE_RESPONSE.getValue())) {
			error = ErrorType.SIGNATURE_MISMATCH;
		}

		else {
			error = ErrorType.DECLINED;
		}

		return error;
	}

}
