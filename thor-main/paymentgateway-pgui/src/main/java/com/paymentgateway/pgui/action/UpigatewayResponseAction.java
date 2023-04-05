package com.paymentgateway.pgui.action;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.api.TransactionControllerServiceProvider;
import com.paymentgateway.commons.dao.FieldsDao;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.user.Account;
import com.paymentgateway.commons.user.AccountCurrency;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.AcquirerType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.pg.core.util.ResponseCreator;

@Service
public class UpigatewayResponseAction {

	private static Logger logger = LoggerFactory.getLogger(UpigatewayResponseAction.class.getName());

	@Autowired
	private TransactionControllerServiceProvider transactionControllerServiceProvider;

	@Autowired
	private ResponseCreator responseCreator;

	@Autowired
	private FieldsDao fieldsDao;

	@Autowired
	private UserDao userDao;
	
	private Fields responseMap = null;

	public Map<String, String> upigatewayResposneHandler(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse) throws IOException {
		try {

			StringBuilder responseString = new StringBuilder();
			String pgRefNo = httpRequest.getParameter("client_txn_id");
			String txn_id = httpRequest.getParameter("txn_id");
			
			if (StringUtils.isNotBlank(httpRequest.getHeader("X-Forwarded-For"))) {
				httpRequest.getSession().setAttribute((FieldType.INTERNAL_CUST_IP.getName()),
						httpRequest.getHeader("X-Forwarded-For").split(",")[0]);
				logger.info("UPIGateway Response received from IP {} for response {} and Pg Ref Num {}", httpRequest.getHeader("X-Forwarded-For"),responseString.toString(),pgRefNo);
			}
			
			Fields fields = new Fields();

			Map<String, String> sessionMap = new HashMap<String, String>();
			Enumeration<String> keys = httpRequest.getSession().getAttributeNames();
			while (keys.hasMoreElements()) {
				String key = (String) keys.nextElement();
				sessionMap.put(key, (String) httpRequest.getSession().getAttribute(key).toString());
			}
			if (!sessionMap.isEmpty()) {

				StringBuilder sesssionsb = new StringBuilder();
				Iterator itr = sessionMap.entrySet().iterator();
				while (itr.hasNext()) {
					Map.Entry obj = (Entry) itr.next();
					if (obj.getKey().toString().equalsIgnoreCase(FieldType.TXN_KEY.getName())) {
						logger.info("Txn Key Present in FIELDS map");
						continue;
					}
					sesssionsb.append(obj.getKey() + " = " + obj.getValue() + " ~");
				}
				logger.info("UPIGateway sessionMap Has values");
				
			} else {
				logger.info("UPIGateway Session Map is empty");
			}

			if (sessionMap.isEmpty()) {
				// Check if fields is empty
				if (StringUtils.isBlank(fields.get(FieldType.PAY_ID.getName()))) {
					logger.info("FIELDS is blank in session Map, getting data from DB for Pg Ref Num {}" , pgRefNo);

					fields = fieldsDao.getPreviousForPgRefNum(pgRefNo);
					String internalRequestFields = fieldsDao.getPreviousForOID(fields.get(FieldType.OID.getName()));
					String[] paramaters = internalRequestFields.split("~");
					Map<String, String> paramMap = new HashMap<String, String>();
					for (String param : paramaters) {
						String[] parameterPair = param.split("=");
						if (parameterPair.length > 1) {
							paramMap.put(parameterPair[0].trim(), parameterPair[1].trim());
						}
					}
					String checkout_Flag = "";
					boolean checkout_flag = userDao.getCheckoutFlag(fields.get(FieldType.PAY_ID.getName()));
					if (checkout_flag == false) {
						checkout_Flag = "N";
					} else {
						checkout_Flag = "Y";
					}
					fields.put(FieldType.CHECKOUT_JS_FLAG.getName(), checkout_Flag);
					httpRequest.getSession().setAttribute(FieldType.CHECKOUT_JS_FLAG.getName(), checkout_Flag);
					fields.put(FieldType.RETURN_URL.getName(), paramMap.get(FieldType.RETURN_URL.getName()));
					httpRequest.getSession().setAttribute(FieldType.RETURN_URL.getName(),
							paramMap.get(FieldType.RETURN_URL.getName()));
					if (StringUtils.isNotBlank(paramMap.get(FieldType.INTERNAL_CUST_IP.getName()))) {
						fields.put((FieldType.INTERNAL_CUST_IP.getName()),
								paramMap.get(FieldType.INTERNAL_CUST_IP.getName()));
					}
					if (StringUtils.isNotBlank(paramMap.get(FieldType.INTERNAL_CUST_COUNTRY_NAME.getName()))) {
						fields.put((FieldType.INTERNAL_CUST_COUNTRY_NAME.getName()),
								paramMap.get(FieldType.INTERNAL_CUST_COUNTRY_NAME.getName()));
					}
				}
			} else {
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
				
				// Check if Pg Ref Num in session is same as Pg Ref Num in the response
				if (StringUtils.isNotBlank(pgRefNo) && StringUtils.isNotBlank(txn_id)) {
					if(!pgRefNo.equalsIgnoreCase(fields.get(FieldType.PG_REF_NUM.getName()))) {
						logger.info("Pg Ref is different in response from UPIGateway and session map , pg ref in sessionMap {} , pg ref in response {}",fields.get(FieldType.PG_REF_NUM.getName()),pgRefNo);
						String path = httpRequest.getContextPath();
						if (!StringUtils.isNotBlank(path) && !path.contains("pgui")) {
							String resultPath = httpRequest.getScheme() + "://" + httpRequest.getHeader("Host")
									+ "/pgui/jsp/error";
							httpResponse.sendRedirect(resultPath);
						}
						httpResponse.sendRedirect("error");
						return null;
					}
					
					if(!txn_id.equalsIgnoreCase(fields.get(FieldType.ACQ_ID.getName()))) {
						logger.info("txn_id is different in response from UPIGateway and session map , ACQ_ID in sessionMap {} , txn_id in response {}",fields.get(FieldType.PG_REF_NUM.getName()),txn_id);
						String path = httpRequest.getContextPath();
						if (!StringUtils.isNotBlank(path) && !path.contains("pgui")) {
							String resultPath = httpRequest.getScheme() + "://" + httpRequest.getHeader("Host")
									+ "/pgui/jsp/error";
							httpResponse.sendRedirect(resultPath);
						}
						httpResponse.sendRedirect("error");
						return null;
					}
				}
				
				else {
					logger.info("UPIGateway Response does not have the client_txn_id or txn_id , client_txn_id: {} , txn_id: {}",pgRefNo,txn_id);
					String path = httpRequest.getContextPath();
					if (!StringUtils.isNotBlank(path) && !path.contains("pgui")) {
						String resultPath = httpRequest.getScheme() + "://" + httpRequest.getHeader("Host")
								+ "/pgui/jsp/error";
						httpResponse.sendRedirect(resultPath);
					}
					httpResponse.sendRedirect("error");
					return null;
				}
				
			}

			// Check if fields is empty
			if (StringUtils.isBlank(fields.get(FieldType.PAY_ID.getName()))) {
				logger.info("UPIGateway FIELDS is blank in session Map, getting data from DB for Letzpaycheckout");

				fields = fieldsDao.getPreviousForPgRefNum(pgRefNo);
				String internalRequestFields = null;
				internalRequestFields = fieldsDao.getPreviousForOID(fields.get(FieldType.OID.getName()));

				if (StringUtils.isBlank(internalRequestFields)) {
					logger.info("UPIGateway getting data from SENT TO BANK "
							+ fields.get(FieldType.OID.getName()));
					internalRequestFields = fieldsDao.getPreviousForOID(fields.get(FieldType.OID.getName()));
				} else {
					logger.info("UPIGateway New Order entry found for this OID in New Order - Pending txn"
							+ fields.get(FieldType.OID.getName()));
				}

				String[] paramaters = internalRequestFields.split("~");
				Map<String, String> paramMap = new HashMap<String, String>();
				for (String param : paramaters) {
					String[] parameterPair = param.split("=");
					if (parameterPair.length > 1) {
						paramMap.put(parameterPair[0].trim(), parameterPair[1].trim());
					}
				}

				if (StringUtils.isNotBlank(paramMap.get(FieldType.RETURN_URL.getName()))) {
					logger.info("UPIGateway Return URL found for ORDER ID " + paramMap.get(FieldType.ORDER_ID.getName())
							+ "Return URL >> " + paramMap.get(FieldType.RETURN_URL.getName()));
					fields.put(FieldType.RETURN_URL.getName(), paramMap.get(FieldType.RETURN_URL.getName()));
					sessionMap.put(FieldType.RETURN_URL.getName(), paramMap.get(FieldType.RETURN_URL.getName()));
				} else {
					logger.info(
							"UPIGateway Return URL not found for ORDER ID " + paramMap.get(FieldType.ORDER_ID.getName()));
				}

				if (StringUtils.isNotBlank(paramMap.get(FieldType.IS_MERCHANT_HOSTED.getName()))) {
					logger.info("UPIGateway IS_MERCHANT_HOSTED flag found for ORDER ID "
							+ paramMap.get(FieldType.ORDER_ID.getName()));
					fields.put(FieldType.IS_MERCHANT_HOSTED.getName(),
							paramMap.get(FieldType.IS_MERCHANT_HOSTED.getName()));
					sessionMap.put(FieldType.IS_MERCHANT_HOSTED.getName(),
							paramMap.get(FieldType.IS_MERCHANT_HOSTED.getName()));
				} else {
					logger.info("UPIGateway IS_MERCHANT_HOSTED not found for ORDER ID "
							+ paramMap.get(FieldType.ORDER_ID.getName()));
				}

			}
			
			
			// If session is null, fetch data from DB and match PG_REF_NUM and ACQ_ID
			if (StringUtils.isNotBlank(pgRefNo) && StringUtils.isNotBlank(txn_id)) {
				if(!pgRefNo.equalsIgnoreCase(fields.get(FieldType.PG_REF_NUM.getName()))) {
					logger.info("Pg Ref is different in response from UPIGateway and session map , pg ref in sessionMap {} , pg ref in response {}",fields.get(FieldType.PG_REF_NUM.getName()),pgRefNo);
					String path = httpRequest.getContextPath();
					if (!StringUtils.isNotBlank(path) && !path.contains("pgui")) {
						String resultPath = httpRequest.getScheme() + "://" + httpRequest.getHeader("Host")
								+ "/pgui/jsp/error";
						httpResponse.sendRedirect(resultPath);
					}
					httpResponse.sendRedirect("error");
					return null;
				}
				
				if(!txn_id.equalsIgnoreCase(fields.get(FieldType.ACQ_ID.getName()))) {
					logger.info("txn_id is different in response from UPIGateway and session map , ACQ_ID in sessionMap {} , txn_id in response {}",fields.get(FieldType.PG_REF_NUM.getName()),txn_id);
					String path = httpRequest.getContextPath();
					if (!StringUtils.isNotBlank(path) && !path.contains("pgui")) {
						String resultPath = httpRequest.getScheme() + "://" + httpRequest.getHeader("Host")
								+ "/pgui/jsp/error";
						httpResponse.sendRedirect(resultPath);
					}
					httpResponse.sendRedirect("error");
					return null;
				}
			}

			JSONObject responseJson = new JSONObject();
			responseJson.put(FieldType.PG_REF_NUM.getName(), pgRefNo);
			logger.info("Response received from UPIGateway for Pg Ref Num : " + pgRefNo);
			fields.put(FieldType.UPIGATEWAY_RESPONSE_FIELD.getName(), responseJson.toString().replace(" ", ""));
			fields.logAllFields("UPIGateway Response Recieved :");

			fields.logAllFields("Updated 3DS Recieved Map TxnType = " + fields.get(FieldType.TXNTYPE.getName()) + " "
					+ "Txn id = " + fields.get(FieldType.TXN_ID.getName()));
			fields.put(FieldType.ACQUIRER_TYPE.getName(), AcquirerType.UPIGATEWAY.getCode());

			if (StringUtils.isNotBlank((String) sessionMap.get(FieldType.INTERNAL_ORIG_TXN_TYPE.getName()))) {
				fields.put(FieldType.TXNTYPE.getName(),
						(String) sessionMap.get(FieldType.INTERNAL_ORIG_TXN_TYPE.getName()));
			}

			if (StringUtils.isNotBlank((String) sessionMap.get(FieldType.PAYMENTS_REGION.getName()))) {
				fields.put((FieldType.PAYMENTS_REGION.getName()),
						(String) sessionMap.get(FieldType.PAYMENTS_REGION.getName()));
			}

			if (StringUtils.isNotBlank((String) sessionMap.get(FieldType.CARD_HOLDER_TYPE.getName()))) {
				fields.put((FieldType.CARD_HOLDER_TYPE.getName()),
						(String) sessionMap.get(FieldType.CARD_HOLDER_TYPE.getName()));
			}

			fields.put(FieldType.INTERNAL_VALIDATE_HASH_YN.getName(), "N");

			if (StringUtils.isNotBlank((String) sessionMap.get(FieldType.OID.getName()))) {
				fields.put((FieldType.OID.getName()), (String) sessionMap.get(FieldType.OID.getName()));

			}

			if (StringUtils.isNotBlank(fields.get(FieldType.INTERNAL_CARD_ISSUER_BANK.getName()))) {
				sessionMap.put(FieldType.INTERNAL_CARD_ISSUER_BANK.getName(),
						fields.get(FieldType.INTERNAL_CARD_ISSUER_BANK.getName()));
			}

			if (StringUtils.isNotBlank(fields.get(FieldType.INTERNAL_CARD_ISSUER_COUNTRY.getName()))) {
				sessionMap.put(FieldType.INTERNAL_CARD_ISSUER_COUNTRY.getName(),
						fields.get(FieldType.INTERNAL_CARD_ISSUER_COUNTRY.getName()));
			}

			Map<String, String> response = transactionControllerServiceProvider.transact(fields,
					Constants.TXN_WS_UPIGATEWAY_PROCESSOR.getValue());
			responseMap = new Fields(response);

			String isMerchantHosted = (String) httpRequest.getSession()
					.getAttribute(FieldType.IS_MERCHANT_HOSTED.getName());
			if (StringUtils.isNotBlank(isMerchantHosted)) {
				responseMap.put(FieldType.IS_MERCHANT_HOSTED.getName(), isMerchantHosted);
			}
			fields.put(FieldType.RETURN_URL.getName(),
					(String) httpRequest.getSession().getAttribute(FieldType.RETURN_URL.getName()));

			String cardIssuerBank = (String) httpRequest.getSession()
					.getAttribute(FieldType.INTERNAL_CARD_ISSUER_BANK.getName());
			String cardIssuerCountry = (String) httpRequest.getSession()
					.getAttribute(FieldType.INTERNAL_CARD_ISSUER_COUNTRY.getName());
			if (StringUtils.isNotBlank(cardIssuerBank)) {
				responseMap.put(FieldType.CARD_ISSUER_BANK.getName(), cardIssuerBank);
			}
			if (StringUtils.isNotBlank(cardIssuerCountry)) {
				responseMap.put(FieldType.CARD_ISSUER_COUNTRY.getName(), cardIssuerCountry);
			}
			responseMap.put(FieldType.CHECKOUT_JS_FLAG.getName(),
					(String) httpRequest.getSession().getAttribute(FieldType.CHECKOUT_JS_FLAG.getName()));
			responseMap.put(FieldType.INTERNAL_SHOPIFY_YN.getName(),
					(String) httpRequest.getSession().getAttribute(FieldType.INTERNAL_SHOPIFY_YN.getName()));
			if (httpRequest.getSession() != null) {
				httpRequest.getSession().setAttribute(Constants.TRANSACTION_COMPLETE_FLAG.getValue(),
						Constants.Y_FLAG.getValue());
				httpRequest.getSession().invalidate();
			}
			responseMap.remove(FieldType.HASH.getName());
			responseMap.remove(FieldType.TXN_KEY.getName());
			responseMap.remove(FieldType.ACQUIRER_TYPE.getName());
			responseMap.remove(FieldType.PASSWORD.getName());
			responseMap.remove(FieldType.IS_INTERNAL_REQUEST.getName());
			responseCreator.create(responseMap);
			responseCreator.ResponsePost(responseMap, httpResponse);
			return responseMap.getFields();

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
		return responseMap.getFields();
	}
	
	public AccountCurrency getAccountCurrency(Fields fields) throws SystemException {

		AccountCurrency accountCurrency = null;

		User user = userDao.findPayId(fields.get(FieldType.PAY_ID.getName()));
		Account account = null;
		Set<Account> accounts = user.getAccounts();

		if (accounts == null || accounts.size() == 0) {
			logger.info("No account found for Pay ID = " + fields.get(FieldType.PAY_ID.getName()) + " and ORDER ID = "
					+ fields.get(FieldType.ORDER_ID.getName()));
		} else {
			for (Account accountThis : accounts) {
				if (accountThis.getAcquirerName()
						.equalsIgnoreCase(AcquirerType.getInstancefromCode(AcquirerType.UPIGATEWAY.getCode()).getName())) {
					account = accountThis;
					break;
				}
			}
		}

		accountCurrency = account.getAccountCurrency(fields.get(FieldType.CURRENCY_CODE.getName()));
		return accountCurrency;

	}


}
