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
import com.paymentgateway.commons.dao.FieldsDao;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.threadpool.ThreadPoolProvider;

/**
 * @author Sandeep
 *
 */
@Service
public class CashfreeUpiResponseAction {

	public static Map<String, User> userMap = new HashMap<String, User>();

	@Autowired
	private TransactionControllerServiceProvider transactionControllerServiceProvider;

	@Autowired
	private FieldsDao fieldsDao;

	@Autowired
	private EPOSTransactionDao eposDao;

	private static Logger logger = LoggerFactory.getLogger(CashfreeUpiResponseAction.class.getName());

	public void cashfreeUpiResponseHandler(HttpServletRequest httpRequest, HttpServletResponse httpResponse)
			throws IOException {

		try {
			Map<String, String[]> fieldMapObj = httpRequest.getParameterMap();
			Map<String, String> response = new HashMap<String, String>();

			for (Entry<String, String[]> entry : fieldMapObj.entrySet()) {
				try {
					response.put(entry.getKey(), entry.getValue()[0]);
				} catch (ClassCastException classCastException) {
					logger.error("Exception", classCastException);
				}
			}
			logger.info("Cashfree Callback Response >>> " + response.toString());
			if (!response.isEmpty()) {
				String order_Id = response.get("orderId");
				String txStatus = response.get("txStatus");
				String orderAmount = response.get("orderAmount");
				String paymentMode = response.get("paymentMode");
				String txTime = response.get("txTime");
				String signature = response.get("signature");
				String txMsg = response.get("txMsg");
				String referenceId = response.get("referenceId");

				Fields fields = new Fields();
				String status = "";

				fields = fieldsDao.getPreviousForOrderId(order_Id);
				logger.info("After getPreviousForOrderId cashfree upi" + fields.getFieldsAsString());

				if (txStatus.equalsIgnoreCase("SUCCESS") && txMsg.equalsIgnoreCase("00::Transaction is Successful")) {
					status = StatusType.CAPTURED.getName();
					fields.put(FieldType.PG_RESP_CODE.getName(), ErrorType.SUCCESS.getCode());
					fields.put(FieldType.PG_TXN_STATUS.getName(), txStatus);
					fields.put(FieldType.PG_TXN_MESSAGE.getName(), txStatus);
					fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.SUCCESS.getCode());
					fields.put(FieldType.RESPONSE_MESSAGE.getName(), txStatus);
					logger.info("Transaction with pg ref num = " + fields.get(FieldType.PG_REF_NUM.getName())
							+ " is Successful");
				}

				else if (txStatus.equalsIgnoreCase("SUCCESS") && txMsg.equalsIgnoreCase("00::Transaction success")) {
					status = StatusType.CAPTURED.getName();
					fields.put(FieldType.PG_RESP_CODE.getName(), ErrorType.SUCCESS.getCode());
					fields.put(FieldType.PG_TXN_STATUS.getName(), txStatus);
					fields.put(FieldType.PG_TXN_MESSAGE.getName(), txStatus);
					fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.SUCCESS.getCode());
					fields.put(FieldType.RESPONSE_MESSAGE.getName(), txStatus);
					logger.info("Transaction with pg ref num = " + fields.get(FieldType.PG_REF_NUM.getName())
							+ " is Successful");
				} else {
					status = StatusType.FAILED.getName();
					fields.put(FieldType.PG_RESP_CODE.getName(), ErrorType.FAILED.getCode());
					fields.put(FieldType.PG_TXN_STATUS.getName(), txStatus);
					fields.put(FieldType.PG_TXN_MESSAGE.getName(), "Transaction fail");
					fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.FAILED.getCode());
					fields.put(FieldType.RESPONSE_MESSAGE.getName(), txStatus);
					logger.info("Transaction with pg ref num = " + fields.get(FieldType.PG_REF_NUM.getName())
							+ " is Failed");
				}

				fields.put(FieldType.STATUS.getName(), status);
				fields.put(FieldType.UDF1.getName(), fields.get(FieldType.PAYER_ADDRESS.getName()));
				fields.put(FieldType.UDF3.getName(), fields.get(FieldType.PAYER_ADDRESS.getName()));
				fields.put(FieldType.RRN.getName(), referenceId);
				fields.put(FieldType.ACQ_ID.getName(), referenceId);
				fields.put(FieldType.PG_DATE_TIME.getName(), txTime);
				fields.put(FieldType.STATUS.getName(), status.toString());
				fields.put(FieldType.INTERNAL_VALIDATE_HASH_YN.getName(), "N");
				logger.info("fields send to transact " + fields.getFieldsAsString());

				Map<String, String> resp = transactionControllerServiceProvider.transact(fields,
						Constants.TXN_WS_UPI_PROCESSOR.getValue());

				StringBuilder sb = new StringBuilder();

				for (Map.Entry<String, String> pair : resp.entrySet()) {
					sb.append(pair.getKey() + "=" + pair.getValue() + "&");
				}

				logger.info("Response received from WS for Cashfree upi " + sb.toString());

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

			} else {
				logger.info("reponse is empty! ");
			}

		} catch (Exception e) {
			logger.error("Error in Cashfree UPI callback = ", e);
			String path = httpRequest.getContextPath();
			logger.info(path);
			if (!StringUtils.isNotBlank(path) && !path.contains("pgui")) {
				String resultPath = httpRequest.getScheme() + "://" + httpRequest.getHeader("Host") + "/pgui/jsp/error";
				httpResponse.sendRedirect(resultPath);
			}
			httpResponse.sendRedirect("error");
		}
	}
}
