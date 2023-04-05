package com.paymentgateway.pgui.action;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
import com.paymentgateway.pg.core.util.UpiHistorian;

/**
 * @author Rahul
 *
 */

@Service
public class FedUpiRefundResponseAction {

	@Autowired
	TransactionControllerServiceProvider transactionControllerServiceProvider;

	@Autowired
	private UpiHistorian upiHistorian;

	@Autowired
	EPOSTransactionDao eposDao;

	private static Logger logger = LoggerFactory.getLogger(FedUpiRefundResponseAction.class.getName());

	public void fedUpiRefundResponseHandler(HttpServletRequest httpRequest, HttpServletResponse httpResponse)
			throws IOException {

		Fields responseField = null;
		try {

//			Object obj = JSONUtil.deserialize(httpRequest.getReader());
			StringBuilder buffer = new StringBuilder();
			BufferedReader reader = httpRequest.getReader();
			String line;
			while ((line = reader.readLine()) != null) {
				buffer.append(line);
			}

			String response = buffer.toString();

			logger.info("Refund response received from federal " + response);
			if (response.contains(Constants.FED_UPI_RESP_VAL_ADD.getValue())) {
//				return Action.NONE;

			}

			int start = response.lastIndexOf("{");
			int end = response.indexOf("}");

			String formattedString = response.substring(start + 1, end);
			String formattedStringArray[] = formattedString.split(", ");

			Map<String, String> responseMap = new HashMap<String, String>();

			for (int i = 0; i < formattedStringArray.length; i++) {

				String str = formattedStringArray[i];
				String strArray[] = str.split(Constants.EQUATOR.getValue());

				responseMap.put(strArray[0], strArray[1]);

			}
			String responseCode = responseMap.get(Constants.FED_UPI_RESPONSE_CODE);
			String payeeApprovalNum = null;
			String responseMessage = responseMap.get(Constants.FED_UPI_RESPONSE);
			String customerReference = responseMap.get(Constants.FED_UPI_CUST_REFERENCE);
			String payerApprovalNum = null;
			String pgTime = responseMap.get(Constants.FED_UPI_APPROVAL_TIME);
			if (responseCode.equals(Constants.FED_UPI_SUCCESS_CODE)) {
				payeeApprovalNum = responseMap.get(Constants.FED_UPI_PAYEE_APPROVAL_NUM);
				payerApprovalNum = responseMap.get(Constants.FED_UPI_PAYER_APPROVAL_NUM);
			}

			String status = getStatusType(responseCode);
			ErrorType errorType = getErrorType(responseCode);

			Fields fields = new Fields();
			fields.put(FieldType.TXNTYPE.getName(), TransactionType.REFUND.getName());
			fields.put(FieldType.ACQUIRER_TYPE.getName(), AcquirerType.FEDERAL.getCode());
			fields.put(FieldType.UDF5.getName(), customerReference);
			upiHistorian.findPreviousForRefund(fields);

			fields.put(FieldType.RESPONSE_CODE.getName(), errorType.getResponseCode());
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), errorType.getResponseMessage());
			fields.put(FieldType.STATUS.getName(), status);
			fields.put(FieldType.PG_RESP_CODE.getName(), responseCode);
			fields.put(FieldType.PG_TXN_MESSAGE.getName(), responseMessage);
			fields.put(FieldType.ACQ_ID.getName(), payeeApprovalNum);
			fields.put(FieldType.INTERNAL_VALIDATE_HASH_YN.getName(), "N");
			fields.put(FieldType.RRN.getName(), payerApprovalNum);
			fields.put(FieldType.PG_DATE_TIME.getName(), pgTime);

			Map<String, String> res = transactionControllerServiceProvider.transact(fields,
					Constants.TXN_WS_UPI_PROCESSOR.getValue());
			responseField = new Fields(res);

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

//			return Action.NONE;
		} catch (Exception exception) {
			logger.error("Exception", exception);
			String path = httpRequest.getContextPath();
			logger.info(path);
			if (!StringUtils.isNotBlank(path) && !path.contains("pgui")) {
				String resultPath = httpRequest.getScheme() + "://" + httpRequest.getHeader("Host") + "/pgui/jsp/error";
				httpResponse.sendRedirect(resultPath);
			}
			httpResponse.sendRedirect("error");
		}
	}

	public String getStatusType(String federalResponseCode) {
		String status = null;
		if (federalResponseCode.equals(Constants.FED_UPI_SUCCESS_CODE)) {
			status = StatusType.CAPTURED.getName();
		} else if (federalResponseCode.equals("U19")) {
			status = StatusType.FAILED.getName();
		} else {
			status = StatusType.REJECTED.getName();
		}

		return status;
	}

	public ErrorType getErrorType(String federalResponseCode) {
		ErrorType error = null;

		if (federalResponseCode.equals(Constants.FED_UPI_SUCCESS_CODE)) {
			error = ErrorType.SUCCESS;
		} else if (federalResponseCode.equals("U19")) {
			error = ErrorType.INVALID_REQUEST_FIELD;
		} else if (federalResponseCode.equals("002")) {
			error = ErrorType.CANCELLED;
		} else if (federalResponseCode.equals("003")) {
			error = ErrorType.INTERNAL_SYSTEM_ERROR;
		} else if (federalResponseCode.equals("004")) {
			error = ErrorType.CANCELLED;
		} else {
			error = ErrorType.DECLINED;
		}

		return error;
	}
}
