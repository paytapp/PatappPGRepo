
package com.paymentgateway.pgui.action;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
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
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TransactionType;
import com.paymentgateway.commons.util.threadpool.ThreadPoolProvider;
import com.paymentgateway.pg.core.util.HdfcUpiUtil;
import com.paymentgateway.pg.core.util.UpiHistorian;

@Service
public class HdfcUpiResponseAction {

	private static Logger logger = LoggerFactory.getLogger(HdfcUpiResponseAction.class.getName());

	@Autowired
	private TransactionControllerServiceProvider transactionControllerServiceProvider;

	@Autowired
	private HdfcUpiUtil hdfcUpiUtil;

	@Autowired
	private UpiHistorian upiHistorian;

	@Autowired
	private EPOSTransactionDao eposDao;

	@SuppressWarnings("static-access")
	public void hdfcUpiResponseHandler(HttpServletRequest httpRequest, HttpServletResponse httpResponse)
			throws IOException {

		Fields responseField = null;
		try {
			Map<String, String[]> fieldMapObj = httpRequest.getParameterMap();
			Map<String, String> responseMap = new HashMap<String, String>();

			for (Entry<String, String[]> entry : fieldMapObj.entrySet()) {
				try {
					responseMap.put(entry.getKey().trim(), ((String[]) entry.getValue())[0].trim());
				} catch (ClassCastException classCastException) {
					logger.error("Exception", classCastException);
					String path = httpRequest.getContextPath();
					logger.info(path);
					if (!StringUtils.isNotBlank(path) && !path.contains("pgui")) {
						String resultPath = httpRequest.getScheme() + "://" + httpRequest.getHeader("Host")
								+ "/pgui/jsp/error";
						httpResponse.sendRedirect(resultPath);
					}
					httpResponse.sendRedirect("error");
				}
			}

			Fields fields = new Fields();
			Object fieldsObj = null;
			if (StringUtils.isNotBlank(
					httpRequest.getSession().getAttribute(FieldType.CHECKOUT_JS_FLAG.getName()).toString())
					&& httpRequest.getSession().getAttribute(FieldType.CHECKOUT_JS_FLAG.getName()).toString()
							.equalsIgnoreCase("Y")) {
				if (httpRequest.getSession().getAttribute("FIELDS").getClass().getSimpleName().toString()
						.equalsIgnoreCase("Fields")) {
					fieldsObj = (Fields) httpRequest.getSession().getAttribute("FIELDS");
				} else {
					String sessionFieldsObj = (String) httpRequest.getSession().getAttribute("FIELDS");
					Map<String, String> fieldsMap = new HashMap<String, String>();
					sessionFieldsObj = sessionFieldsObj.substring(1, sessionFieldsObj.length() - 1);
					String[] fieldArray = sessionFieldsObj.split(",");
					for (String key : fieldArray) {
						if (key.charAt(0) == ' ') {
							key = key.replaceFirst("^\\s*", "");
						}
						String[] namValuePair = key.split("=", 2);
						fieldsMap.put(namValuePair[0], namValuePair[1]);
					}
					fieldsObj = new Fields(fieldsMap);
					logger.info(fieldsMap.toString());
				}
			} else {
				fieldsObj = (Fields) httpRequest.getSession().getAttribute("FIELDS");
			}
			if (null != fieldsObj) {
				fields.put((Fields) fieldsObj);
			}
			PropertiesManager propertiesManager = new PropertiesManager();
			String encrypted = responseMap.get("meRes");
			String key = propertiesManager.propertiesMap.get(Constants.HDFC_UPI_MERCHANT_KEY.getValue());
			String decryptedString = "";
			try {
				decryptedString = hdfcUpiUtil.decrypt(encrypted, key);

			} catch (Exception e) { // TODO
				logger.error("Error in hdfc bank UPI callback = ", e);
			}

			String[] value_split = decryptedString.split("\\|");
			logger.info("HDFC UPI APPROVED COLLECT RESPONSE  " + decryptedString);

			String receivedResponseCode = value_split[6];

			String status = getStatusType(receivedResponseCode);
			ErrorType errorMsg = getErrorType(receivedResponseCode);

			String responseCode = value_split[6];
			String responseMsg = value_split[4];
			String txnMsg = value_split[5];
			String payeeApprovalNum = value_split[0];
			String ReferenceId = value_split[10];
			String dateTime = value_split[3];
			String customerReference = value_split[9];
			String txnId = value_split[1];
			String address = value_split[18];
			String[] merchantDetails = address.split("\\!");
			String merchantVPA = merchantDetails[0];

			logger.info("Merchant VPA " + merchantVPA);

			fields.put(FieldType.ACQUIRER_TYPE.getName(), AcquirerType.FSS.getCode());
			fields.put(FieldType.PG_REF_NUM.getName(), txnId);
			fields.put(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName());
			logger.info("fields before historian " + fields.getFieldsAsString());
			upiHistorian.findPrevious(fields);
			logger.info("After historian " + fields.getFieldsAsString());
			fields.put(FieldType.STATUS.getName(), status);
			fields.put(FieldType.RESPONSE_CODE.getName(), errorMsg.getResponseCode());
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), errorMsg.getResponseMessage());
			fields.put(FieldType.PG_RESP_CODE.getName(), responseCode);
			fields.put(FieldType.PG_TXN_STATUS.getName(), responseMsg);
			fields.put(FieldType.PG_TXN_MESSAGE.getName(), txnMsg);
			fields.put(FieldType.UDF1.getName(), merchantVPA);

			fields.put(FieldType.ACQ_ID.getName(), payeeApprovalNum);
			fields.put(FieldType.RRN.getName(), customerReference);
			fields.put(FieldType.PG_DATE_TIME.getName(), dateTime);
			fields.put(FieldType.STATUS.getName(), status.toString());
			fields.put(FieldType.AUTH_CODE.getName(), ReferenceId);
			fields.put(FieldType.INTERNAL_VALIDATE_HASH_YN.getName(), "N");

			logger.info("fields send to transact " + fields.getFieldsAsString());

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

			logger.info("Response received from WS " + responseField);

		} catch (Exception e) {
			logger.error("Error in hdfc bank UPI callback = ", e);
			String path = httpRequest.getContextPath();
			logger.info(path);
			if (!StringUtils.isNotBlank(path) && !path.contains("pgui")) {
				String resultPath = httpRequest.getScheme() + "://" + httpRequest.getHeader("Host") + "/pgui/jsp/error";
				httpResponse.sendRedirect(resultPath);
			}
			httpResponse.sendRedirect("error");
		}

	}

	public String getStatusType(String receivedResponse) {
		String status = null;
		if (receivedResponse.equals(Constants.HDFC_UPI_SUCCESS_CODE.getValue())) {
			status = StatusType.CAPTURED.getName();
		} else if (receivedResponse.equals(Constants.HDFC_UPI_ERROR_RESPONSE_CODE.getValue())) {
			status = StatusType.FAILED.getName();
		} else {
			status = StatusType.REJECTED.getName();
		}

		return status;
	}

	public ErrorType getErrorType(String receivedResponse) {
		ErrorType error = null;

		if (receivedResponse.equals(Constants.HDFC_UPI_SUCCESS_CODE.getValue())) {
			error = ErrorType.SUCCESS;
		} else if (receivedResponse.equals(Constants.HDFC_UPI_ERROR_RESPONSE_CODE.getValue())) {
			error = ErrorType.INVALID_REQUEST_FIELD;
		} else if (receivedResponse.equals(Constants.HDFC_UPI_INVALID_REQUEST_FIELD_CODE.getValue())) {
			error = ErrorType.CANCELLED;
		} else if (receivedResponse.equals(Constants.HDFC_UPI_CANCELLED_CODE.getValue())) {
			error = ErrorType.INTERNAL_SYSTEM_ERROR;
		} else if (receivedResponse.equals(Constants.HDFC_UPI_INTERNAL_SYSTEM_ERROR_CODE.getValue())) {
			error = ErrorType.CANCELLED;
		} else {
			error = ErrorType.DECLINED;
		}

		return error;
	}

}
