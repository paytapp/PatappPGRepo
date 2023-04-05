package com.paymentgateway.pgui.action;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.api.TransactionControllerServiceProvider;
import com.paymentgateway.commons.dao.FieldsDao;
import com.paymentgateway.commons.dao.UserSettingDao;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.kms.EncryptDecryptService;
import com.paymentgateway.commons.user.Account;
import com.paymentgateway.commons.user.AccountCurrency;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserSettingData;
import com.paymentgateway.commons.util.AcquirerType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.DateCreater;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.pg.core.util.ISGPayDecryption;

/**
 * @author Sandeep
 *
 */
@Service
public class IsgPayUpiResponseAction {

	private static Logger logger = LoggerFactory.getLogger(IsgPayUpiResponseAction.class.getName());

	@Autowired
	private TransactionControllerServiceProvider transactionControllerServiceProvider;

	@Autowired
	private UserDao userDao;

	@Autowired
	private FieldsDao fieldsDao;

	@Autowired
	private EncryptDecryptService encryptDecryptService;

	@Autowired
	private ISGPayDecryption iSGPayDecryption;

	@Autowired
	private UserSettingDao userSettingDao;

	public void isgPayResponseHandler(HttpServletRequest httpRequest, HttpServletResponse httpResponse)
			throws IOException {
		Fields responseMap = null;
		try {
			Map<String, String[]> fieldMapObj = httpRequest.getParameterMap();
			Map<String, String> requestMap = new HashMap<String, String>();
			for (Entry<String, String[]> entry : fieldMapObj.entrySet()) {
				try {
					requestMap.put(entry.getKey(), entry.getValue()[0]);

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
			String EncData = requestMap.get("EncData");
			String respTid = requestMap.get("TerminalId");
			String respMid = requestMap.get("MerchantId");
			String respBankid = requestMap.get("BankId");

			LinkedHashMap<String, String> hmDecryptedValue = new LinkedHashMap<String, String>();

			hmDecryptedValue.put("EncData", EncData);
			hmDecryptedValue.put("TerminalId", respTid);
			hmDecryptedValue.put("MerchantId", respMid);
			hmDecryptedValue.put("BankId", respBankid);

			String txnKey = null;
			String password = null;

			Fields fields = new Fields();
			UserSettingData userSettings = userSettingDao.fetchDataUsingIsgPayMID(respMid);
			String payId = userSettings.getPayId();
			String merchantDetail = getTxnKey(payId);
			String[] merchantParam = merchantDetail.split(",");
			Map<String, String> detailParamMap = new HashMap<String, String>();
			for (String param : merchantParam) {
				String[] parameterPair = param.split("=");
				if (parameterPair.length > 1) {
					detailParamMap.put(parameterPair[0].trim(), parameterPair[1].trim());
				}
			}
			txnKey = detailParamMap.get(FieldType.TXN_KEY.getName());
			password = detailParamMap.get(FieldType.PASSWORD.getName());

			iSGPayDecryption.decrypt(hmDecryptedValue, txnKey, password);

			StringBuilder decrytedString = new StringBuilder();

			for (Map.Entry<String, String> entry : hmDecryptedValue.entrySet()) {
				String key = entry.getKey();
				String value = entry.getValue();
				decrytedString.append(key).append("=").append(value).append("||");
			}
			// Fields fields = new Fields();
			fields.logAllFields("ISGPay Decrypted Response Recieved : " + decrytedString.toString());

			String pgRef = hmDecryptedValue.get("TxnRefNo");
			if (StringUtils.isNotBlank(pgRef)) {
				fields = fieldsDao.getPreviousForPgRefNum(pgRef);

				String txnStatus = hmDecryptedValue.get("Status");
				String txnMessage = hmDecryptedValue.get("Message");
				String retRefNo = hmDecryptedValue.get("RetRefNo");
				String status = "";

				if (txnStatus.equalsIgnoreCase("00") && txnMessage.equalsIgnoreCase("Transaction Successful")) {
					status = StatusType.CAPTURED.getName();
					fields.put(FieldType.PG_RESP_CODE.getName(), ErrorType.SUCCESS.getCode());
					fields.put(FieldType.PG_TXN_STATUS.getName(), txnStatus);
					fields.put(FieldType.PG_TXN_MESSAGE.getName(), txnMessage);
					fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.SUCCESS.getCode());
					fields.put(FieldType.RESPONSE_MESSAGE.getName(), txnMessage);
					logger.info("Transaction with pg ref num = " + fields.get(FieldType.PG_REF_NUM.getName())
							+ " is Successful");
				}

				else {
					status = StatusType.FAILED.getName();
					fields.put(FieldType.PG_RESP_CODE.getName(), ErrorType.FAILED.getCode());
					fields.put(FieldType.PG_TXN_STATUS.getName(), txnStatus);
					fields.put(FieldType.PG_TXN_MESSAGE.getName(), "Transaction fail");
					fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.FAILED.getCode());
					fields.put(FieldType.RESPONSE_MESSAGE.getName(), txnMessage);
					logger.info("Transaction with pg ref num = " + fields.get(FieldType.PG_REF_NUM.getName())
							+ " is Failed");
				}
				fields.put(FieldType.STATUS.getName(), status);
				fields.put(FieldType.UDF1.getName(), fields.get(FieldType.PAYER_ADDRESS.getName()));
				fields.put(FieldType.UDF3.getName(), fields.get(FieldType.PAYER_ADDRESS.getName()));
				fields.put(FieldType.RRN.getName(), retRefNo);
				fields.put(FieldType.ACQ_ID.getName(), retRefNo);
				fields.put(FieldType.PG_DATE_TIME.getName(), DateCreater.formatDateForDb(new Date()));
				fields.put(FieldType.STATUS.getName(), status.toString());
				fields.put(FieldType.INTERNAL_VALIDATE_HASH_YN.getName(), "N");
				logger.info("fields send to transact " + fields.getFieldsAsString());

				Map<String, String> response = transactionControllerServiceProvider.transact(fields,
						Constants.TXN_WS_UPI_PROCESSOR.getValue());
				responseMap = new Fields(response);

				logger.info("Response received from WS for ISGPAY UPI " + responseMap.getFieldsAsString());
			} else {
				logger.info("Response received in ISGpaycallback with pgref >>> " + pgRef + " is empty");
			}
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

	public String getTxnKey(String payId) throws SystemException {
		StringBuilder req = new StringBuilder();
		User user = userDao.findPayId(payId);
		Account account = null;
		Set<Account> accounts = user.getAccounts();

		if (accounts == null || accounts.size() == 0) {
			logger.info("No account found for Pay ID = " + payId);
		} else {
			for (Account accountThis : accounts) {
				if (accountThis.getAcquirerName()
						.equalsIgnoreCase(AcquirerType.getInstancefromCode(AcquirerType.ISGPAY.getCode()).getName())) {
					account = accountThis;
					break;
				}
			}
		}

		AccountCurrency accountCurrency = account.getAccountCurrency("356");
		String txnKey = accountCurrency.getTxnKey();
		String password = encryptDecryptService.decrypt(payId, accountCurrency.getPassword());

		req.append(FieldType.TXN_KEY.getName());
		req.append("=");
		req.append(txnKey);
		req.append(",");
		req.append(FieldType.PASSWORD.getName());
		req.append("=");
		req.append(password);
		req.append(",");

		return req.toString();
	}
}
