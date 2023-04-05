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
import com.paymentgateway.commons.dao.FieldsDao;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.AcquirerType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.IdfcUpiHmacAlgo;
import com.paymentgateway.commons.util.IdfcUpiUpiResultType;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TransactionType;
import com.paymentgateway.commons.util.threadpool.ThreadPoolProvider;
import com.paymentgateway.pg.core.util.UpiHistorian;

@Service
public class IdfcUpiResponseAction {
	private static Logger logger = LoggerFactory.getLogger(IdfcUpiResponseAction.class.getName());

	@Autowired
	private TransactionControllerServiceProvider transactionControllerServiceProvider;

	@Autowired
	@Qualifier("idfcUpiHmacAlgo")
	private IdfcUpiHmacAlgo idfcUpiHmacAlgo;

	@Autowired
	private UpiHistorian upiHistorian;

	@Autowired
	private EPOSTransactionDao eposDao;

	@Autowired
	private FieldsDao fieldsDao;

	public void idfcUpiResponseHandler(HttpServletRequest httpRequest, HttpServletResponse httpResponse)
			throws IOException {

		logger.info("Inside  idfcUpiResponseAction Execute");
		Fields responseField = null;

		String status = "";
		ErrorType errorType = null;

		try {

			StringBuilder buffer = new StringBuilder();
			BufferedReader reader = httpRequest.getReader();
			String line;
			while ((line = reader.readLine()) != null) {
				buffer.append(line);
			}
			String json = buffer.toString();
			logger.info("response received from bank idfc upi 1 " + json);
			JSONObject res = new JSONObject(json);
			Fields fields = new Fields();

			logger.info("response received from bank idfc upi 2" + res.toString());
			String pgRefNum = "";

			String responseCode = res.getString("ResCode");
			String responseMessage = res.getString("ResDesc").replaceAll("'", " -apostrophe_symbol- ");

			errorType = updateStatusResponse(responseCode, responseMessage);

			String payeeVpa = res.getString("PayeeVirAddr");
			String customerReference = res.getString("OrgTxnRefId");
			String payerVpa = res.getString("PayerVirAddr");
			String pgTime = res.getString("TimeStamp");
			String resDesc = res.getString("ResDesc").replaceAll("'", " -apostrophe_symbol- ");
			String transactionid = res.getString("OrgTxnId");
			String acqId = res.getString("OrgCustRefId");// to be changed according to requirement

			pgRefNum = transactionid.substring(5, 21);

			logger.info("Merchant VPA idfc upi " + payeeVpa);

			fields.put(FieldType.ACQUIRER_TYPE.getName(), AcquirerType.IDFCUPI.getCode());
			fields.put(FieldType.PG_REF_NUM.getName(), pgRefNum);
			fields.put(FieldType.TXNTYPE.getName(), TransactionType.SALE.getName());
			logger.info("fields before historian idfc upi " + fields.getFieldsAsString());
			upiHistorian.findPrevious(fields);
			logger.info("After historian idfc upi" + fields.getFieldsAsString());

			if (errorType == null) {
				status = StatusType.REJECTED.getName();
				logger.info("Transaction with pg ref num = " + fields.get(FieldType.PG_REF_NUM.getName())
						+ " is Failed because error Type is null");
				errorType = ErrorType.DECLINED;
			} else if (errorType == ErrorType.SUCCESS) {
				status = StatusType.CAPTURED.getName();
				logger.info("Transaction with pg ref num = " + fields.get(FieldType.PG_REF_NUM.getName())
						+ " is Successful");
			} else {
				status = StatusType.REJECTED.getName();
				logger.info(
						"Transaction with pg ref num = " + fields.get(FieldType.PG_REF_NUM.getName()) + " is Failed");
			}
			fields.put(FieldType.STATUS.getName(), status);
			fields.put(FieldType.RESPONSE_CODE.getName(), errorType.getResponseCode());
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), errorType.getResponseMessage());
			fields.put(FieldType.PG_RESP_CODE.getName(), responseCode);
			fields.put(FieldType.PG_TXN_STATUS.getName(), responseMessage);
			fields.put(FieldType.PG_TXN_MESSAGE.getName(), resDesc);
			fields.put(FieldType.UDF1.getName(), payeeVpa);
			fields.put(FieldType.UDF3.getName(), payerVpa);
			fields.put(FieldType.RRN.getName(), acqId);
			fields.put(FieldType.ACQ_ID.getName(), acqId);
			fields.put(FieldType.PG_DATE_TIME.getName(), pgTime);
			fields.put(FieldType.STATUS.getName(), status.toString());
			fields.put(FieldType.INTERNAL_VALIDATE_HASH_YN.getName(), "N");
			logger.info("fields send to transact " + fields.getFieldsAsString());

			Map<String, String> resp = transactionControllerServiceProvider.transact(fields,
					Constants.TXN_WS_UPI_PROCESSOR.getValue());
			responseField = new Fields(resp);

			StringBuilder sb = new StringBuilder();

			for (Map.Entry<String, String> pair : resp.entrySet()) {
				sb.append(pair.getKey() + "=" + pair.getValue() + "&");
			}

			logger.info("Response received from WS for idfc upi " + sb.toString());

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

		} catch (Exception e) {
			logger.error("Error in idfc bank UPI callback = ", e);
			String path = httpRequest.getContextPath();
			logger.info(path);
			if (!StringUtils.isNotBlank(path) && !path.contains("pgui")) {
				String resultPath = httpRequest.getScheme() + "://" + httpRequest.getHeader("Host") + "/pgui/jsp/error";
				httpResponse.sendRedirect(resultPath);
			}
			httpResponse.sendRedirect("error");
		}
	}

	public ErrorType updateStatusResponse(String receivedResponseCode, String receivedResponse) throws SystemException {

		try {

			String status = "";
			ErrorType errorType = null;
			String pgTxnMsg = "";

			logger.info(" inside IDFC UPI Response action in  updateStatusResponse method response code is ==  "
					+ receivedResponseCode);
			if (receivedResponseCode.equals(Constants.IDFC_UPI_SUCCESS_CODE.getValue())
					&& receivedResponse.equals(Constants.IDFC_UPI_RESPONSE_MSG.getValue())) {
				status = StatusType.CAPTURED.getName();
				errorType = ErrorType.SUCCESS;
				pgTxnMsg = receivedResponse;
			} else {
				if (StringUtils.isNotBlank(receivedResponseCode)) {
					IdfcUpiUpiResultType resultInstance = IdfcUpiUpiResultType
							.getInstanceFromName(receivedResponseCode);
					logger.info(
							" inside IDFC UPI Response action in  updateStatusResponse method resultInstance is : == "
									+ resultInstance);
					if (resultInstance != null) {
						if (resultInstance.getPaymentGatewayCode() != null) {
							logger.info(
									" inside IDFC UPI Response action in  updateStatusResponse method resultInstance is ==  "
											+ resultInstance.getStatusName() + (resultInstance.getPaymentGatewayCode()));
							status = resultInstance.getStatusName();
							errorType = ErrorType.getInstanceFromCode(resultInstance.getPaymentGatewayCode());
							pgTxnMsg = resultInstance.getMessage();
						} else {
							status = StatusType.REJECTED.getName();
							errorType = ErrorType.DECLINED;
							pgTxnMsg = ErrorType.REJECTED.getResponseMessage();

						}

					} else {
						status = StatusType.REJECTED.getName();
						errorType = ErrorType.DECLINED;
						pgTxnMsg = ErrorType.REJECTED.getResponseMessage();

					}

				} else {
					status = StatusType.REJECTED.getName();
					errorType = ErrorType.DECLINED;
					pgTxnMsg = ErrorType.REJECTED.getResponseMessage();

				}
			}

			return errorType;
		} catch (Exception e) {
			logger.error("Unknown Exception :" + e.getMessage());
			throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR, e,
					"unknown exception in  idfcUpiResponseAction");
		}
	}
}
