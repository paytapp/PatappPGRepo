package com.paymentgateway.icici.composite.api;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.paymentgateway.commons.api.Hasher;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.util.Amount;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.commons.util.StatusType;
import com.paymentgateway.commons.util.TransactionManager;
import com.paymentgateway.payout.VendorPayoutDao;
import com.paymentgateway.pg.core.util.IciciUtil;

@RestController
public class CompositeApiTransact {

	private static Logger logger = LoggerFactory.getLogger(CompositeApiTransact.class.getName());

	@Autowired
	private IciciTransactionConverter iciciTransactionConverter;

	@Autowired
	private IciciCommunicator iciciCommunicator;

	@Autowired
	private IciciResponseHandler iciciResponseHandler;

	@Autowired
	private VendorPayoutDao vendorPayoutDao;

	@Autowired
	private UserDao userDao;

	@Autowired
	private IciciUtil iciciUtil;

	@RequestMapping(method = RequestMethod.POST, value = "/impsCompositeApi", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)

	public @ResponseBody Map<String, String> impsApi(@RequestBody Map<String, String> reqmap) throws SystemException {

		logger.info("INSIDE IMPS COMPOSITE API");

		Fields fields = new Fields(reqmap);

		try {
			fields.logAllFieldsPayOut("IMPS Composite API Raw Request:");
			fields.clean();

			Map<String, String> responseMap = new HashMap<String, String>();

			boolean hashResult = iciciTransactionConverter.validateHashForApi(fields);
			if (!hashResult) {
				fields.put(FieldType.STATUS.getName(), StatusType.AUTHENTICATION_FAILED.getName());
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.AUTHENTICATION_FAILED.getResponseMessage());
				fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.AUTHENTICATION_FAILED.getResponseCode());
				fields.put(FieldType.HASH.getName(), Hasher.getHash(fields));
				logger.info("Hash Invalid For Request ORDER_ID " + fields.get(FieldType.ORDER_ID.getName())
						+ " fields are " + fields.maskFieldsRequest(fields.getFields()));
				return fields.getFields();
			}

			// Check All Fields not Empty & valid Value
			if (!iciciTransactionConverter.validateFields(fields)) {
				logger.info("Invalid fields " + fields.maskFieldsRequest(fields.getFields()) + " for orderID "
						+ fields.get(FieldType.ORDER_ID.getName()));
				return fields.getFields();
			}

			User user = userDao.findPayId(fields.get(FieldType.PAY_ID.getName()));
			if (StringUtils.isNotBlank(user.getSuperMerchantId())) {
				User user1 = userDao.findPayId(user.getSuperMerchantId());
				if (StringUtils.isNotBlank(user1.getResellerId())) {
					fields.put(FieldType.RESELLER_ID.getName(), user1.getResellerId());
				}
			} else {
				if (StringUtils.isNotBlank(user.getResellerId())) {
					fields.put(FieldType.RESELLER_ID.getName(), user.getResellerId());
				}
			}

			logger.info("Hash Verified & fields Verified");

			if ((StringUtils.isNotBlank(fields.get(FieldType.USER_TYPE.getName()))) && ((fields
					.get(FieldType.USER_TYPE.getName()).equalsIgnoreCase("Merchant Initiated Direct"))
					|| (fields.get(FieldType.USER_TYPE.getName()).equalsIgnoreCase("Merchant Initiated Indirect")))) {
				vendorPayoutDao.insertUpdateForClosing(fields);
			}

			// Check DB for BENE && hitting bene registration
			// No need to do bene registration for PAYBLE Merchant

			/*if (!iciciResponseHandler.isPaybleMerchant(fields)
					&& !iciciResponseHandler.isBeneAlreadyRegistered(fields)) {
				logger.info("inside the registration of beneficiary for IMPS "
						+ fields.maskFieldsRequest(fields.getFields()));

				fields.put(FieldType.TXN_ID.getName(), TransactionManager.getNewTransactionId());

				// Check the beneficiary
				String reqUrl = PropertiesManager.propertiesMap.get("ICICI_COMPOSITE_BENE_ADDITION");
				String beneApiKey;

				if (iciciResponseHandler.isPaybleMerchant(fields)) {
					beneApiKey = PropertiesManager.propertiesMap.get("PAYBLE_ICICI_COMPOSITE_BENE_REG_API_KEY");
				} else {
					beneApiKey = PropertiesManager.propertiesMap.get("ICICI_COMPOSITE_BENE_REG_API_KEY");
				}

				fields.put(FieldType.REQUEST_TYPE.getName(), FieldType.BENE_REGISTRATION.getName());

				Map<String, String> beneResponse = new HashMap<String, String>();

				String req = iciciTransactionConverter.createCompositeIMPSRequestForAddBene(fields);
				String resp = iciciCommunicator.getIciciCompositeApiResponse(req, reqUrl, fields, null, beneApiKey);
				beneResponse = iciciResponseHandler.compositeBeneAdditionResponseHandler(fields, resp);

				logger.info("Bene data Update ");

				if (!iciciResponseHandler.isBeneSuccess(beneResponse)) {
					logger.info(
							"Beneficary addition failed for " + beneResponse.get(FieldType.BENE_ACCOUNT_NO.getName()));
					fields.put(FieldType.STATUS.getName(), beneResponse.get(FieldType.STATUS.getName()));
					fields.put(FieldType.RESPONSE_MESSAGE.getName(),
							beneResponse.get(FieldType.RESPONSE_MESSAGE.getName()));
					fields.put(FieldType.RESPONSE_CODE.getName(), beneResponse.get(FieldType.RESPONSE_CODE.getName()));
					fields.put(FieldType.PG_TXN_MESSAGE.getName(),
							beneResponse.get(FieldType.PG_RESPONSE_MSG.getName()));
					fields.put(FieldType.PG_RESP_CODE.getName(), beneResponse.get(FieldType.PG_RESP_CODE.getName()));

					// Insert In Transaction
					fields.put(FieldType.AMOUNT.getName(), Amount.toDecimal(fields.get(FieldType.AMOUNT.getName()),
							fields.get(FieldType.CURRENCY_CODE.getName())));

					iciciResponseHandler.compositeIMPSTransactionResponseHandler(fields, resp);

					fields.put(FieldType.AMOUNT.getName(), Amount.formatAmount(fields.get(FieldType.AMOUNT.getName()),
							fields.get(FieldType.CURRENCY_CODE.getName())));

					fields.put(FieldType.HASH.getName(), Hasher.getHash(fields));

					return fields.getFields();
				}
			}

			logger.info("beneficiary verified");*/

			fields.put(FieldType.AMOUNT.getName(), Amount.toDecimal(fields.get(FieldType.AMOUNT.getName()),
					fields.get(FieldType.CURRENCY_CODE.getName())));
			fields.put(FieldType.BENE_NAME_REQUEST.getName(), fields.get(FieldType.BENE_NAME.getName()));

			String url = PropertiesManager.propertiesMap.get("ICICI_COMPOSITE_PAYMENT_URL");
			String priorty = PropertiesManager.propertiesMap.get("ICICI_IMPS_PRIORTY");
			String apiKey;

			if (iciciResponseHandler.isPaybleMerchant(fields)) {
				apiKey = PropertiesManager.propertiesMap.get("PAYBLE_ICICI_COMPOSITE_API_KEY");
			} else {
				apiKey = PropertiesManager.propertiesMap.get("ICICI_COMPOSITE_API_KEY");
			}

			String request = iciciTransactionConverter.createCompositeIMPSRequest(fields);

			// Copying Request in a separate String to avoid concurrency
			// exception
			String reqCopy = request;
			logger.info("Generated Copy for modification");
			logger.info("ICICI COMPOSITE IMPS request : " + request);

			String response = iciciCommunicator.getIciciCompositeApiResponse(request, url, fields, priorty, apiKey);

			iciciResponseHandler.compositeIMPSTransactionResponseHandler(fields, response);

			fields.put(FieldType.AMOUNT.getName(), Amount.formatAmount(fields.get(FieldType.AMOUNT.getName()),
					fields.get(FieldType.CURRENCY_CODE.getName())));

			fields.put(FieldType.HASH.getName(), Hasher.getHash(fields));

			return fields.getFields();

		} catch (Exception exception) {
			logger.error("Exception in Composte IMPS Txn id ," + fields.get(FieldType.TXN_ID.getName()) + " ",
					exception);
			if (StringUtils.isNotBlank(fields.get(FieldType.AMOUNT.getName()))
					&& fields.get(FieldType.AMOUNT.getName()).contains(".")) {
				fields.put(FieldType.AMOUNT.getName(), Amount.formatAmount(fields.get(FieldType.AMOUNT.getName()),
						fields.get(FieldType.CURRENCY_CODE.getName())));
			}

			fields.put(FieldType.STATUS.getName(), StatusType.ERROR.getName());
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.COMMON_ERROR.getResponseMessage());
			fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.COMMON_ERROR.getResponseCode());
			fields.put(FieldType.HASH.getName(), Hasher.getHash(fields));

			return fields.getFields();
		}
	}

	@RequestMapping(method = RequestMethod.POST, value = "/impsCompositeStatusEnq", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Map<String, String> impsStatusApi(@RequestBody Map<String, String> reqmap)
			throws SystemException {

		logger.info("INSIDE IMPS COMPOSITE Status API");

		Fields fields = new Fields(reqmap);

		try {
			fields.logAllFields("IMPS Composite Status API Raw Request:");
			fields.clean();

			Map<String, String> responseMap = new HashMap<String, String>();

			boolean hashResult = iciciTransactionConverter.validateHashForApi(fields);
			if (!hashResult) {
				fields.put(FieldType.STATUS.getName(), StatusType.AUTHENTICATION_FAILED.getName());
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.AUTHENTICATION_FAILED.getResponseMessage());
				fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.AUTHENTICATION_FAILED.getResponseCode());
				fields.put(FieldType.HASH.getName(), Hasher.getHash(fields));
				logger.info("Authentication Failed Fields Are " + fields.getFields());
				return fields.getFields();
			}

			logger.info("Hash Verified");

			String url = PropertiesManager.propertiesMap.get("ICICI_COMPOSITE_IMPS_PAYMENT_STATUS_URL");
			String priorty = PropertiesManager.propertiesMap.get("ICICI_IMPS_PRIORTY");
			String apiKey;

			if (iciciResponseHandler.isPaybleMerchant(fields)) {
				apiKey = PropertiesManager.propertiesMap.get("PAYBLE_ICICI_COMPOSITE_API_KEY");
			} else {
				apiKey = PropertiesManager.propertiesMap.get("ICICI_COMPOSITE_API_KEY");
			}

			String request = iciciTransactionConverter.createCompositeIMPSStatusEnqRequest(fields);
			String response = iciciCommunicator.getIciciCompositeApiResponse(request, url, fields, null, apiKey);
			iciciResponseHandler.compositeIMPSStatusEnqResponseHandler(fields, response);
			//
			// fields.put(FieldType.AMOUNT.getName(),
			// Amount.formatAmount(fields.get(FieldType.AMOUNT.getName()),
			// fields.get(FieldType.CURRENCY_CODE.getName())));

			fields.put(FieldType.HASH.getName(), Hasher.getHash(fields));

			return fields.getFields();

		} catch (Exception exception) {
			logger.error("Exception in IMPS ", exception);
			if (StringUtils.isNotBlank(fields.get(FieldType.AMOUNT.getName()))
					&& fields.get(FieldType.AMOUNT.getName()).contains(".")) {
				fields.put(FieldType.AMOUNT.getName(), Amount.formatAmount(fields.get(FieldType.AMOUNT.getName()),
						fields.get(FieldType.CURRENCY_CODE.getName())));
			}
			fields.put(FieldType.STATUS.getName(), StatusType.ERROR.getName());
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.COMMON_ERROR.getResponseMessage());
			fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.COMMON_ERROR.getResponseCode());
			fields.put(FieldType.HASH.getName(), Hasher.getHash(fields));

			return fields.getFields();
		}
	}

	@RequestMapping(method = RequestMethod.POST, value = "/upiCompositeApi", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Map<String, String> upiApi(@RequestBody Map<String, String> reqmap) throws SystemException {

		logger.info("INSIDE UPI COMPOSITE API");

		Fields fields = new Fields(reqmap);
		try {

			fields.logAllFields("UPI Composite API Raw Request:");
			fields.clean();

			Map<String, String> responseMap = new HashMap<String, String>();
			boolean hashResult = iciciTransactionConverter.validateHashForApi(fields);

			if (!hashResult) {
				fields.put(FieldType.STATUS.getName(), StatusType.AUTHENTICATION_FAILED.getName());
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.AUTHENTICATION_FAILED.getResponseMessage());
				fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.AUTHENTICATION_FAILED.getResponseCode());
				fields.put(FieldType.HASH.getName(), Hasher.getHash(fields));
				return fields.getFields();
			}

			logger.info("Hash Verified");

			// Check All Fields not Empty & valid Value
			if (!iciciTransactionConverter.validateFieldsUPI(fields)) {
				logger.info("Found invalid Field in validation request from merchant");
				return fields.getFields();
			}
			logger.info("fields validated successfully");

			User user = userDao.findPayId(fields.get(FieldType.PAY_ID.getName()));
			if (StringUtils.isNotBlank(user.getSuperMerchantId())) {
				User user1 = userDao.findPayId(user.getSuperMerchantId());
				if (StringUtils.isNotBlank(user1.getResellerId())) {
					fields.put(FieldType.RESELLER_ID.getName(), user1.getResellerId());
				}
			} else {
				if (StringUtils.isNotBlank(user.getResellerId())) {
					fields.put(FieldType.RESELLER_ID.getName(), user.getResellerId());
				}
			}

			if ((StringUtils.isNotBlank(fields.get(FieldType.USER_TYPE.getName()))) && ((fields
					.get(FieldType.USER_TYPE.getName()).equalsIgnoreCase("Merchant Initiated Direct"))
					|| (fields.get(FieldType.USER_TYPE.getName()).equalsIgnoreCase("Merchant Initiated Indirect")))) {
				vendorPayoutDao.insertUpdateForClosing(fields);
			}

			// Bene Registration not needed for PAYBLE MERCHANT
			/*if (!iciciResponseHandler.isPaybleMerchant(fields)
					&& !iciciResponseHandler.isBeneAlreadyRegistered(fields)) {
				logger.info("Inside the registration of beneficiary for VPA " + fields.getFields());

				fields.put(FieldType.TXN_ID.getName(), TransactionManager.getNewTransactionId());

				// Check the beneficiary
				String reqUrl = PropertiesManager.propertiesMap.get("ICICI_COMPOSITE_VPA_BENE_ADDITION");
				String beneApiKey;

				if (iciciResponseHandler.isPaybleMerchant(fields)) {
					beneApiKey = PropertiesManager.propertiesMap.get("PAYBLE_ICICI_COMPOSITE_BENE_REG_API_KEY");
				} else {
					beneApiKey = PropertiesManager.propertiesMap.get("ICICI_COMPOSITE_BENE_REG_API_KEY");
				}

				fields.put(FieldType.REQUEST_TYPE.getName(), FieldType.BENE_REGISTRATION.getName());
				Map<String, String> beneResponse = new HashMap<String, String>();

				String req = iciciTransactionConverter.createCompositeUPIRequestForAddBene(fields);
				String resp = iciciCommunicator.getIciciCompositeApiResponse(req, reqUrl, fields, null, beneApiKey);
				beneResponse = iciciResponseHandler.compositeBeneAdditionResponseHandler(fields, resp);
				logger.info("Bene data Update ");

				if (!iciciResponseHandler.isBeneSuccess(beneResponse)) {
					logger.info("Beneficary addition failed for "
							+ beneResponse.get(beneResponse.get(FieldType.PAYER_ADDRESS.getName())));
					fields.put(FieldType.STATUS.getName(), beneResponse.get(FieldType.STATUS.getName()));
					fields.put(FieldType.RESPONSE_MESSAGE.getName(),
							beneResponse.get(FieldType.RESPONSE_MESSAGE.getName()));
					fields.put(FieldType.RESPONSE_CODE.getName(), beneResponse.get(FieldType.RESPONSE_CODE.getName()));
					fields.put(FieldType.PG_TXN_MESSAGE.getName(),
							beneResponse.get(FieldType.PG_RESPONSE_MSG.getName()));
					fields.put(FieldType.PG_RESP_CODE.getName(), beneResponse.get(FieldType.PG_RESP_CODE.getName()));

					// Insert In Transaction
					fields.put(FieldType.AMOUNT.getName(), Amount.toDecimal(fields.get(FieldType.AMOUNT.getName()),
							fields.get(FieldType.CURRENCY_CODE.getName())));

					iciciResponseHandler.compositeUPITransactionResponseHandler(fields, resp);

					fields.put(FieldType.AMOUNT.getName(), Amount.formatAmount(fields.get(FieldType.AMOUNT.getName()),
							fields.get(FieldType.CURRENCY_CODE.getName())));

					fields.put(FieldType.HASH.getName(), Hasher.getHash(fields));

					return fields.getFields();
				}
			}

			logger.info("beneficiary verified");*/

			fields.put(FieldType.AMOUNT.getName(), Amount.toDecimal(fields.get(FieldType.AMOUNT.getName()),
					fields.get(FieldType.CURRENCY_CODE.getName())));

			String url = PropertiesManager.propertiesMap.get("ICICI_COMPOSITE_PAYMENT_URL");
			String priorty = PropertiesManager.propertiesMap.get("ICICI_UPI_PRIORTY");
			String apiKey;

			if (iciciResponseHandler.isPaybleMerchant(fields)) {
				apiKey = PropertiesManager.propertiesMap.get("PAYBLE_ICICI_COMPOSITE_API_KEY");
			} else {
				apiKey = PropertiesManager.propertiesMap.get("ICICI_COMPOSITE_API_KEY");
			}

			String request = iciciTransactionConverter.createCompositeUPIRequest(fields);
			String response = iciciCommunicator.getIciciCompositeApiResponse(request, url, fields, priorty, apiKey);
			responseMap = iciciResponseHandler.compositeUPITransactionResponseHandler(fields, response);

			fields.put(FieldType.AMOUNT.getName(), Amount.formatAmount(fields.get(FieldType.AMOUNT.getName()),
					fields.get(FieldType.CURRENCY_CODE.getName())));

			fields.put(FieldType.HASH.getName(), Hasher.getHash(fields));

			return fields.getFields();

		} catch (Exception exception) {
			logger.error("Exception in Composite UPI Txn id ," + fields.get(FieldType.TXN_ID.getName()) + " ",
					exception);

			if (StringUtils.isNotBlank(fields.get(FieldType.AMOUNT.getName()))
					&& fields.get(FieldType.AMOUNT.getName()).contains(".")) {
				fields.put(FieldType.AMOUNT.getName(), Amount.formatAmount(fields.get(FieldType.AMOUNT.getName()),
						fields.get(FieldType.CURRENCY_CODE.getName())));
			}

			fields.put(FieldType.STATUS.getName(), StatusType.ERROR.getName());
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.COMMON_ERROR.getResponseMessage());
			fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.COMMON_ERROR.getResponseCode());
			fields.put(FieldType.HASH.getName(), Hasher.getHash(fields));

			return fields.getFields();
		}
	}

	@RequestMapping(method = RequestMethod.POST, value = "/upiCompositeStatusEnq", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Map<String, String> upiStatusEnqApi(@RequestBody Map<String, String> reqmap)
			throws SystemException {

		logger.info("INSIDE UPI COMPOSITE STATUS ENQUIRY API");

		Fields fields = new Fields(reqmap);
		try {

			fields.logAllFields("UPI Composite Status Enquiry API Raw Request:");
			fields.clean();

			Map<String, String> responseMap = new HashMap<String, String>();

			boolean hashResult = iciciTransactionConverter.validateHashForApi(fields);

			if (!hashResult) {
				fields.put(FieldType.STATUS.getName(), StatusType.AUTHENTICATION_FAILED.getName());
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.AUTHENTICATION_FAILED.getResponseMessage());
				fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.AUTHENTICATION_FAILED.getResponseCode());
				fields.put(FieldType.HASH.getName(), Hasher.getHash(fields));
				return fields.getFields();
			}

			logger.info("Hash Verified");

			// fields.put(FieldType.AMOUNT.getName(),
			// Amount.toDecimal(fields.get(FieldType.AMOUNT.getName()),
			// fields.get(FieldType.CURRENCY_CODE.getName())));

			String url = PropertiesManager.propertiesMap.get("ICICI_COMPOSITE_UPI_PAYMENT_STATUS_URL");
//			String priorty = PropertiesManager.propertiesMap.get("ICICI_UPI_PRIORTY");
			String apiKey;

			if (iciciResponseHandler.isPaybleMerchant(fields)) {
				apiKey = PropertiesManager.propertiesMap.get("PAYBLE_ICICI_COMPOSITE_API_KEY");
			} else {
				apiKey = PropertiesManager.propertiesMap.get("ICICI_COMPOSITE_API_KEY");
			}

			String request = iciciTransactionConverter.createCompositeUPIStatusEnqRequest(fields);
			String response = iciciCommunicator.getIciciCompositeApiResponse(request, url, fields, null, apiKey);
			iciciResponseHandler.compositeUPIStatusEnqResponseHandler(fields, response);

			// fields.put(FieldType.AMOUNT.getName(),
			// Amount.formatAmount(fields.get(FieldType.AMOUNT.getName()),
			// fields.get(FieldType.CURRENCY_CODE.getName())));

			fields.put(FieldType.HASH.getName(), Hasher.getHash(fields));

			return fields.getFields();

		} catch (Exception exception) {
			logger.error("Exception in UPI ", exception);
			if (StringUtils.isNotBlank(fields.get(FieldType.AMOUNT.getName()))
					&& fields.get(FieldType.AMOUNT.getName()).contains(".")) {
				fields.put(FieldType.AMOUNT.getName(), Amount.formatAmount(fields.get(FieldType.AMOUNT.getName()),
						fields.get(FieldType.CURRENCY_CODE.getName())));
			}

			fields.put(FieldType.STATUS.getName(), StatusType.ERROR.getName());
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.COMMON_ERROR.getResponseMessage());
			fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.COMMON_ERROR.getResponseCode());
			fields.put(FieldType.HASH.getName(), Hasher.getHash(fields));

			return fields.getFields();
		}
	}

	@RequestMapping(method = RequestMethod.POST, value = "/testCompositeApi", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Map<String, String> testCompositeApi(@RequestBody Map<String, Map<String, String>> reqMap)
			throws SystemException {

		logger.info("INSIDE Test Composite Api");

		Map<String, String> responseMap = new HashMap<String, String>();

		try {

			JSONObject dataJson = new JSONObject();

			for (Map.Entry<String, String> entry : reqMap.get("data").entrySet()) {

				dataJson.put(entry.getKey(), entry.getValue());
			}

			JSONObject headerJson = new JSONObject();

			for (Map.Entry<String, String> entry : reqMap.get("header").entrySet()) {

				headerJson.put(entry.getKey(), entry.getValue());
			}

			JSONObject reqUrl = new JSONObject(reqMap.get("url"));
			for (Map.Entry<String, String> entry : reqMap.get("url").entrySet()) {

				reqUrl.put(entry.getKey(), entry.getValue());
			}

			String url = reqUrl.getString("URL");

			String apiKey = headerJson.getString("apikey");
			String priorty = null;
			if (headerJson.has("priority")) {
				priorty = headerJson.getString("priority");
			}

			String resp = iciciCommunicator.getIciciCompositeApiResponse(dataJson.toString(), url, new Fields(),
					priorty, apiKey);
			logger.info("response is " + resp + " request" + dataJson.toString());

//			resp = iciciUtil.compositeApiBeneDecryption(resp.toString(), true);

			logger.info("response is " + resp + " request" + dataJson.toString());

			if (resp != null) {
				JSONObject responseJson = new JSONObject(resp);
				for (String jString : responseJson.keySet()) {

					responseMap.put(jString, responseJson.get(jString).toString());
				}
			}
			return responseMap;

		} catch (Exception e) {
			logger.error("exception e ", e);
		}
		return responseMap;

	}

	@RequestMapping(method = RequestMethod.POST, value = "/impsAddBeneCompositeApi", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Map<String, String> impsAddBeneApi(@RequestBody Map<String, String> reqmap)
			throws SystemException {

		Fields fields = new Fields(reqmap);
		Map<String, String> beneResponse = new HashMap<String, String>();
		try {

			boolean hashResult = iciciTransactionConverter.validateHashForApi(fields);
			if (!hashResult) {
				fields.put(FieldType.STATUS.getName(), StatusType.AUTHENTICATION_FAILED.getName());
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.AUTHENTICATION_FAILED.getResponseMessage());
				fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.AUTHENTICATION_FAILED.getResponseCode());
				fields.put(FieldType.HASH.getName(), Hasher.getHash(fields));
				logger.info("Hash Invalid For Request ORDER_ID " + fields.get(FieldType.ORDER_ID.getName())
						+ " fields are " + fields.getFields());
				return fields.getFields();
			}

			// Check All Fields not Empty & valid Value
			if (!iciciTransactionConverter.validateFieldsForAddBene(fields)) {
				logger.info("Invalid fields " + fields.getFields() + " for orderID "
						+ fields.get(FieldType.ORDER_ID.getName()));
				return fields.getFields();
			}
			logger.info("inside the registration of beneficiary for IMPS " + fields.getFields());

			User user = userDao.findPayId(fields.get(FieldType.PAY_ID.getName()));
			if (StringUtils.isNotBlank(user.getSuperMerchantId())) {
				User user1 = userDao.findPayId(user.getSuperMerchantId());
				if (StringUtils.isNotBlank(user1.getResellerId())) {
					fields.put(FieldType.RESELLER_ID.getName(), user1.getResellerId());
				}
			} else {
				if (StringUtils.isNotBlank(user.getResellerId())) {
					fields.put(FieldType.RESELLER_ID.getName(), user.getResellerId());
				}
			}

			fields.put(FieldType.TXN_ID.getName(), TransactionManager.getNewTransactionId());

			// Check the beneficiary
			String reqUrl = PropertiesManager.propertiesMap.get("ICICI_COMPOSITE_BENE_ADDITION");
			String beneApiKey;

			if (iciciResponseHandler.isPaybleMerchant(fields)) {
				beneApiKey = PropertiesManager.propertiesMap.get("PAYBLE_ICICI_COMPOSITE_BENE_REG_API_KEY");
			} else {
				beneApiKey = PropertiesManager.propertiesMap.get("ICICI_COMPOSITE_BENE_REG_API_KEY");
			}
			fields.put(FieldType.REQUEST_TYPE.getName(), FieldType.BENE_REGISTRATION.getName());

			String req = iciciTransactionConverter.createCompositeIMPSRequestForAddBene(fields);
			String resp = iciciCommunicator.getIciciCompositeApiResponse(req, reqUrl, fields, null, beneApiKey);
			beneResponse = iciciResponseHandler.compositeBeneAdditionResponseHandler(fields, resp);

			logger.info("Bene data Update ");

			if (!iciciResponseHandler.isBeneSuccess(beneResponse)) {
				logger.info("Beneficary addition failed for " + beneResponse.get(FieldType.BENE_ACCOUNT_NO.getName()));
				fields.put(FieldType.STATUS.getName(), beneResponse.get(FieldType.STATUS.getName()));
				fields.put(FieldType.RESPONSE_MESSAGE.getName(),
						beneResponse.get(FieldType.RESPONSE_MESSAGE.getName()));
				fields.put(FieldType.RESPONSE_CODE.getName(), beneResponse.get(FieldType.RESPONSE_CODE.getName()));
				fields.put(FieldType.PG_TXN_MESSAGE.getName(), beneResponse.get(FieldType.PG_RESPONSE_MSG.getName()));
				fields.put(FieldType.PG_RESP_CODE.getName(), beneResponse.get(FieldType.PG_RESP_CODE.getName()));
				fields.put(FieldType.HASH.getName(), Hasher.getHash(fields));

				return fields.getFields();
			}
			return beneResponse;
		} catch (Exception e) {
			logger.error("exception e ", e);
		}

		logger.info("beneficiary verified");
		return beneResponse;
	}

	@RequestMapping(method = RequestMethod.POST, value = "/upiAddBeneCompositeApi", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Map<String, String> upiAddBeneApi(@RequestBody Map<String, String> reqmap)
			throws SystemException {

		Fields fields = new Fields(reqmap);
		Map<String, String> beneResponse = new HashMap<String, String>();
		try {

			boolean hashResult = iciciTransactionConverter.validateHashForApi(fields);

			if (!hashResult) {
				fields.put(FieldType.STATUS.getName(), StatusType.AUTHENTICATION_FAILED.getName());
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.AUTHENTICATION_FAILED.getResponseMessage());
				fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.AUTHENTICATION_FAILED.getResponseCode());
				fields.put(FieldType.HASH.getName(), Hasher.getHash(fields));
				return fields.getFields();
			}

			logger.info("Hash Verified");

			// Check All Fields not Empty & valid Value
			if (!iciciTransactionConverter.validateFieldsAddBeneUPI(fields)) {
				logger.info("Found invalid Field in validation request from merchant");
				return fields.getFields();
			}
			logger.info("fields validated successfully");

			logger.info("inside the registration of beneficiary for VPA " + fields.getFields());

			User user = userDao.findPayId(fields.get(FieldType.PAY_ID.getName()));
			if (StringUtils.isNotBlank(user.getSuperMerchantId())) {
				User user1 = userDao.findPayId(user.getSuperMerchantId());
				if (StringUtils.isNotBlank(user1.getResellerId())) {
					fields.put(FieldType.RESELLER_ID.getName(), user1.getResellerId());
				}
			} else {
				if (StringUtils.isNotBlank(user.getResellerId())) {
					fields.put(FieldType.RESELLER_ID.getName(), user.getResellerId());
				}
			}

			fields.put(FieldType.TXN_ID.getName(), TransactionManager.getNewTransactionId());

			// Check the beneficiary
			String reqUrl = PropertiesManager.propertiesMap.get("ICICI_COMPOSITE_VPA_BENE_ADDITION");
			String beneApiKey;

			if (iciciResponseHandler.isPaybleMerchant(fields)) {
				beneApiKey = PropertiesManager.propertiesMap.get("PAYBLE_ICICI_COMPOSITE_BENE_REG_API_KEY");
			} else {
				beneApiKey = PropertiesManager.propertiesMap.get("ICICI_COMPOSITE_BENE_REG_API_KEY");
			}
			fields.put(FieldType.REQUEST_TYPE.getName(), FieldType.BENE_REGISTRATION.getName());

			String req = iciciTransactionConverter.createCompositeUPIRequestForAddBene(fields);
			String resp = iciciCommunicator.getIciciCompositeApiResponse(req, reqUrl, fields, null, beneApiKey);
			beneResponse = iciciResponseHandler.compositeBeneAdditionResponseHandler(fields, resp);
			logger.info("Bene data Update ");

			if (!iciciResponseHandler.isBeneSuccess(beneResponse)) {
				logger.info("Beneficary addition failed for "
						+ beneResponse.get(beneResponse.get(FieldType.PAYER_ADDRESS.getName())));
				fields.put(FieldType.STATUS.getName(), beneResponse.get(FieldType.STATUS.getName()));
				fields.put(FieldType.RESPONSE_MESSAGE.getName(),
						beneResponse.get(FieldType.RESPONSE_MESSAGE.getName()));
				fields.put(FieldType.RESPONSE_CODE.getName(), beneResponse.get(FieldType.RESPONSE_CODE.getName()));
				fields.put(FieldType.PG_TXN_MESSAGE.getName(), beneResponse.get(FieldType.PG_RESPONSE_MSG.getName()));
				fields.put(FieldType.PG_RESP_CODE.getName(), beneResponse.get(FieldType.PG_RESP_CODE.getName()));

				fields.put(FieldType.HASH.getName(), Hasher.getHash(fields));

				return fields.getFields();
			}
			return beneResponse;
		} catch (Exception e) {
			logger.error("exception e ", e);
		}

		logger.info("beneficiary verified");
		return beneResponse;

	}

	@RequestMapping(method = RequestMethod.POST, value = "/compositeAccountStatement", consumes = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Map<String, String> AccountStatement(@RequestBody Map<String, String> reqmap) {

		logger.info("INSIDE IMPS COMPOSITE Account Statement API");

		Fields fields = new Fields(reqmap);

		try {
			fields.logAllFieldsPayOut("IMPS Composite Account Statement API Raw Request:");
			fields.clean();
			
			String userType=fields.get(FieldType.USER_TYPE.getName());
			String fileType=fields.get(FieldType.FILE_TYPE.getName());

			boolean availableLastTrId = false;
			
			String apiKey;
			
			if(StringUtils.isNotBlank(fileType) && fileType.equalsIgnoreCase("Current")){
				if(StringUtils.isNotBlank(userType) && userType.equalsIgnoreCase("Payble")){
					apiKey = PropertiesManager.propertiesMap.get("PAYBLE_ICICI_COMPOSITE_BENE_REG_API_KEY");
				}else{
					apiKey = PropertiesManager.propertiesMap.get("ICICI_COMPOSITE_BENE_REG_API_KEY");
				}
			}else{
				apiKey = PropertiesManager.propertiesMap.get("ICICI_NODAL_COMPOSITE_API_KEY");
			}

			String reqUrl = PropertiesManager.propertiesMap.get("ICICI_COMPOSITE_ACCOUNT_STATEMENT_URL");
			

			do {
//				fields.put(FieldType.REQUEST_TYPE.getName(), Constants.REQ_ACCOUNT_STATEMENT);
				String req = iciciTransactionConverter.createCompositeAccountStatementRequest(fields);
				String resp = iciciCommunicator.getIciciCompositeAccountStatementResponse(req, reqUrl, fields, apiKey);
				logger.info("Decrypted Account Statement Response " + resp);
				iciciResponseHandler.compositeAccountStatementResponseHandler(fields, resp);

				if (StringUtils.isNotBlank(fields.get(Constants.LASTTRID))) {
					availableLastTrId = true;
				} else {
					availableLastTrId = false;
				}

			} while (availableLastTrId);

		} catch (Exception e) {
			logger.error("Exception in composite Account Statement ", e);
		}

		return fields.getFields();

	}

	@RequestMapping(method = RequestMethod.POST, value = "/rtgsCompositeApi", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Map<String, String> rtgsApi(@RequestBody Map<String, String> reqmap) throws SystemException {

		logger.info("INSIDE RTGS COMPOSITE API");

		try {
			Fields fields = new Fields(reqmap);
			fields.logAllFields("RTGS Composite API Raw Request:");
			fields.clean();

			Map<String, String> responseMap = new HashMap<String, String>();
			boolean hashResult = iciciTransactionConverter.validateHashForApi(fields);
			if (!hashResult) {
				fields.put(FieldType.STATUS.getName(), StatusType.AUTHENTICATION_FAILED.getName());
				fields.put(FieldType.PG_TXN_MESSAGE.getName(), ErrorType.INVALID_HASH.getResponseMessage());
				fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_HASH.getResponseCode());
				return fields.getFields();
			}

			logger.info("Hash Verified");

			// Check All Fields not Empty & valid Value
			if (!iciciTransactionConverter.validateFields(fields)) {
				logger.info("Invalid fields " + fields.maskFieldsRequest(fields.getFields()) + " for orderID "
						+ fields.get(FieldType.ORDER_ID.getName()));
				return fields.getFields();
			}

			User user = userDao.findPayId(fields.get(FieldType.PAY_ID.getName()));
			if (StringUtils.isNotBlank(user.getSuperMerchantId())) {
				User user1 = userDao.findPayId(user.getSuperMerchantId());
				if (StringUtils.isNotBlank(user1.getResellerId())) {
					fields.put(FieldType.RESELLER_ID.getName(), user1.getResellerId());
				}
			} else {
				if (StringUtils.isNotBlank(user.getResellerId())) {
					fields.put(FieldType.RESELLER_ID.getName(), user.getResellerId());
				}
			}
			fields.put(FieldType.TXNTYPE.getName(), "RTGS");
			logger.info("Hash Verified & fields Verified");

			if ((StringUtils.isNotBlank(fields.get(FieldType.USER_TYPE.getName()))) && ((fields
					.get(FieldType.USER_TYPE.getName()).equalsIgnoreCase("Merchant Initiated Direct"))
					|| (fields.get(FieldType.USER_TYPE.getName()).equalsIgnoreCase("Merchant Initiated Indirect")))) {
				vendorPayoutDao.insertUpdateForClosing(fields);
			}

			/*
			 * if (!iciciResponseHandler.isBeneAlreadyRegistered(fields)) {
			 * logger.info("inside the registration of beneficiary for " +
			 * fields.getFields());
			 * 
			 * fields.put(FieldType.TXN_ID.getName(),
			 * TransactionManager.getNewTransactionId());
			 * 
			 * // Check the beneficiary String reqUrl =
			 * PropertiesManager.propertiesMap.get(
			 * "ICICI_COMPOSITE_VPA_BENE_ADDITION"); String beneApiKey;
			 * 
			 * if (iciciResponseHandler.isPaybleMerchant(fields)) { beneApiKey =
			 * PropertiesManager.propertiesMap.get(
			 * "PAYBLE_ICICI_COMPOSITE_BENE_REG_API_KEY"); } else { beneApiKey =
			 * PropertiesManager.propertiesMap.get(
			 * "ICICI_COMPOSITE_BENE_REG_API_KEY"); }
			 * 
			 * Map<String, String> beneResponse = new HashMap<String, String>();
			 * 
			 * String req =
			 * iciciTransactionConverter.createCompositeIMPSRequestForAddBene(
			 * fields); String resp =
			 * iciciCommunicator.getIciciCompositeApiResponse(req, reqUrl,
			 * fields, null, beneApiKey); beneResponse =
			 * iciciResponseHandler.compositeBeneAdditionResponseHandler(fields,
			 * resp); logger.info("Bene data Update ");
			 * 
			 * fields.put(FieldType.TXNTYPE.getName(), Constants.RTGS);
			 * 
			 * if (!iciciResponseHandler.isBeneSuccess(beneResponse)) {
			 * logger.info("Beneficary addition failed for " +
			 * beneResponse.get(beneResponse.get(FieldType.BENE_ACCOUNT_NO.
			 * getName()))); fields.put(FieldType.STATUS.getName(),
			 * beneResponse.get(FieldType.STATUS.getName()));
			 * fields.put(FieldType.RESPONSE_MESSAGE.getName(),
			 * beneResponse.get(FieldType.RESPONSE_MESSAGE.getName()));
			 * fields.put(FieldType.RESPONSE_CODE.getName(),
			 * beneResponse.get(FieldType.RESPONSE_CODE.getName()));
			 * fields.put(FieldType.PG_TXN_MESSAGE.getName(),
			 * beneResponse.get(FieldType.PG_RESPONSE_MSG.getName()));
			 * fields.put(FieldType.PG_RESP_CODE.getName(),
			 * beneResponse.get(FieldType.PG_RESP_CODE.getName()));
			 * 
			 * // Insert In Transaction fields.put(FieldType.AMOUNT.getName(),
			 * Amount.toDecimal(fields.get(FieldType.AMOUNT.getName()),
			 * fields.get(FieldType.CURRENCY_CODE.getName())));
			 * 
			 * iciciResponseHandler.compositeRTGSNEFTTransactionResponseHandler(
			 * fields, resp);
			 * 
			 * fields.put(FieldType.AMOUNT.getName(),
			 * Amount.formatAmount(fields.get(FieldType.AMOUNT.getName()),
			 * fields.get(FieldType.CURRENCY_CODE.getName())));
			 * 
			 * fields.put(FieldType.HASH.getName(), Hasher.getHash(fields));
			 * 
			 * return fields.getFields(); } }
			 * 
			 * logger.info("beneficiary verified");
			 */

			fields.put(FieldType.AMOUNT.getName(), Amount.toDecimal(fields.get(FieldType.AMOUNT.getName()),
					fields.get(FieldType.CURRENCY_CODE.getName())));

			String url = PropertiesManager.propertiesMap.get("ICICI_COMPOSITE_PAYMENT_URL");
			String priorty = "0001";
			String apiKey;

			if (iciciResponseHandler.isPaybleMerchant(fields)) {
				apiKey = PropertiesManager.propertiesMap.get("PAYBLE_ICICI_COMPOSITE_API_KEY");
			} else {
				apiKey = PropertiesManager.propertiesMap.get("ICICI_COMPOSITE_API_KEY");
			}

			String request = iciciTransactionConverter.createCompositeRTGSRequest(fields);
			String response = iciciCommunicator.getIciciCompositeApiResponse(request, url, fields, priorty, apiKey);
			iciciResponseHandler.compositeRTGSNEFTTransactionResponseHandler(fields, response);

			fields.put(FieldType.AMOUNT.getName(), Amount.formatAmount(fields.get(FieldType.AMOUNT.getName()),
					fields.get(FieldType.CURRENCY_CODE.getName())));

			fields.put(FieldType.HASH.getName(), Hasher.getHash(fields));

			return fields.getFields();
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return null;
		}
	}

	@RequestMapping(method = RequestMethod.POST, value = "/neftCompositeApi", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Map<String, String> neftApi(@RequestBody Map<String, String> reqmap) throws SystemException {

		logger.info("INSIDE NEFT COMPOSITE API");

		try {
			Fields fields = new Fields(reqmap);
			fields.logAllFields("NEFT Composite API Raw Request:");
			fields.clean();

			Map<String, String> responseMap = new HashMap<String, String>();
			boolean hashResult = iciciTransactionConverter.validateHashForApi(fields);
			if (!hashResult) {
				fields.put(FieldType.STATUS.getName(), StatusType.AUTHENTICATION_FAILED.getName());
				fields.put(FieldType.PG_TXN_MESSAGE.getName(), ErrorType.INVALID_HASH.getResponseMessage());
				fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.INVALID_HASH.getResponseCode());
				return fields.getFields();
			}

			logger.info("Hash Verified");

			// Check All Fields not Empty & valid Value
			if (!iciciTransactionConverter.validateFields(fields)) {
				logger.info("Invalid fields " + fields.maskFieldsRequest(fields.getFields()) + " for orderID "
						+ fields.get(FieldType.ORDER_ID.getName()));
				return fields.getFields();
			}

			User user = userDao.findPayId(fields.get(FieldType.PAY_ID.getName()));
			if (StringUtils.isNotBlank(user.getSuperMerchantId())) {
				User user1 = userDao.findPayId(user.getSuperMerchantId());
				if (StringUtils.isNotBlank(user1.getResellerId())) {
					fields.put(FieldType.RESELLER_ID.getName(), user1.getResellerId());
				}
			} else {
				if (StringUtils.isNotBlank(user.getResellerId())) {
					fields.put(FieldType.RESELLER_ID.getName(), user.getResellerId());
				}
			}

			logger.info("Hash Verified & fields Verified");
			fields.put(FieldType.TXNTYPE.getName(), "NEFT");

			if ((StringUtils.isNotBlank(fields.get(FieldType.USER_TYPE.getName()))) && ((fields
					.get(FieldType.USER_TYPE.getName()).equalsIgnoreCase("Merchant Initiated Direct"))
					|| (fields.get(FieldType.USER_TYPE.getName()).equalsIgnoreCase("Merchant Initiated Indirect")))) {
				vendorPayoutDao.insertUpdateForClosing(fields);
			}

			/*
			 * if (!iciciResponseHandler.isPaybleMerchant(fields) &&
			 * !iciciResponseHandler.isBeneAlreadyRegistered(fields)) {
			 * logger.info("inside the registration of beneficiary for " +
			 * fields.getFields());
			 * 
			 * fields.put(FieldType.TXN_ID.getName(),
			 * TransactionManager.getNewTransactionId());
			 * 
			 * // Check the beneficiary String reqUrl =
			 * PropertiesManager.propertiesMap.get(
			 * "ICICI_COMPOSITE_VPA_BENE_ADDITION"); String beneApiKey =
			 * PropertiesManager.propertiesMap.get(
			 * "ICICI_COMPOSITE_BENE_REG_API_KEY");
			 * 
			 * Map<String, String> beneResponse = new HashMap<String, String>();
			 * 
			 * String req =
			 * iciciTransactionConverter.createCompositeIMPSRequestForAddBene(
			 * fields); String resp =
			 * iciciCommunicator.getIciciCompositeApiResponse(req, reqUrl,
			 * fields, null, beneApiKey); beneResponse =
			 * iciciResponseHandler.compositeBeneAdditionResponseHandler(fields,
			 * resp); logger.info("Bene data Update ");
			 * 
			 * if (!iciciResponseHandler.isBeneSuccess(beneResponse)) {
			 * logger.info("Beneficary addition failed for " +
			 * beneResponse.get(beneResponse.get(FieldType.PAYER_ADDRESS.getName
			 * ()))); fields.put(FieldType.STATUS.getName(),
			 * beneResponse.get(FieldType.STATUS.getName()));
			 * fields.put(FieldType.RESPONSE_MESSAGE.getName(),
			 * beneResponse.get(FieldType.RESPONSE_MESSAGE.getName()));
			 * fields.put(FieldType.RESPONSE_CODE.getName(),
			 * beneResponse.get(FieldType.RESPONSE_CODE.getName()));
			 * fields.put(FieldType.PG_TXN_MESSAGE.getName(),
			 * beneResponse.get(FieldType.PG_RESPONSE_MSG.getName()));
			 * fields.put(FieldType.PG_RESP_CODE.getName(),
			 * beneResponse.get(FieldType.PG_RESP_CODE.getName()));
			 * 
			 * // Insert In Transaction fields.put(FieldType.AMOUNT.getName(),
			 * Amount.toDecimal(fields.get(FieldType.AMOUNT.getName()),
			 * fields.get(FieldType.CURRENCY_CODE.getName())));
			 * 
			 * iciciResponseHandler.compositeRTGSNEFTTransactionResponseHandler(
			 * fields, resp);
			 * 
			 * fields.put(FieldType.AMOUNT.getName(),
			 * Amount.formatAmount(fields.get(FieldType.AMOUNT.getName()),
			 * fields.get(FieldType.CURRENCY_CODE.getName())));
			 * 
			 * fields.put(FieldType.HASH.getName(), Hasher.getHash(fields));
			 * 
			 * return fields.getFields(); } }
			 */

			fields.put(FieldType.AMOUNT.getName(), Amount.toDecimal(fields.get(FieldType.AMOUNT.getName()),
					fields.get(FieldType.CURRENCY_CODE.getName())));

			String url = PropertiesManager.propertiesMap.get("ICICI_COMPOSITE_PAYMENT_URL");
			String priorty = "0010";
			String apiKey;

			if (iciciResponseHandler.isPaybleMerchant(fields)) {
				apiKey = PropertiesManager.propertiesMap.get("PAYBLE_ICICI_COMPOSITE_API_KEY");
			} else {
				apiKey = PropertiesManager.propertiesMap.get("ICICI_COMPOSITE_API_KEY");
			}

			String request = iciciTransactionConverter.createCompositeNEFTRequest(fields);
			String response = iciciCommunicator.getIciciCompositeApiResponse(request, url, fields, priorty, apiKey);
			iciciResponseHandler.compositeRTGSNEFTTransactionResponseHandler(fields, response);

			fields.put(FieldType.AMOUNT.getName(), Amount.formatAmount(fields.get(FieldType.AMOUNT.getName()),
					fields.get(FieldType.CURRENCY_CODE.getName())));
			fields.put(FieldType.HASH.getName(), Hasher.getHash(fields));

			return fields.getFields();
		} catch (Exception exception) {
			logger.error("Exception", exception);
			return null;
		}
	}

	@RequestMapping(method = RequestMethod.POST, value = "/neftRtgsCompositeStatusEnq", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Map<String, String> neftRtgsStatusApi(@RequestBody Map<String, String> reqmap)
			throws SystemException {

		logger.info("INSIDE NEFT/RTGS COMPOSITE Status API");

		Fields fields = new Fields(reqmap);

		try {
			fields.logAllFields("NEFT/RTGS Composite Status API Raw Request:");
			fields.clean();

			Map<String, String> responseMap = new HashMap<String, String>();

			boolean hashResult = iciciTransactionConverter.validateHashForApi(fields);
			if (!hashResult) {
				fields.put(FieldType.STATUS.getName(), StatusType.AUTHENTICATION_FAILED.getName());
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.AUTHENTICATION_FAILED.getResponseMessage());
				fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.AUTHENTICATION_FAILED.getResponseCode());
				fields.put(FieldType.HASH.getName(), Hasher.getHash(fields));
				logger.info("Authentication Failed Fields Are " + fields.getFields());
				return fields.getFields();
			}

			logger.info("Hash Verified");

			String url = PropertiesManager.propertiesMap.get("ICICI_COMPOSITE_NEFT_RTGS_PAYMENT_STATUS_URL");

			String priorty = null;

			if (fields.get(FieldType.TXNTYPE.getName()).equalsIgnoreCase("NEFT"))
				priorty = "0010";
			else if (fields.get(FieldType.TXNTYPE.getName()).equalsIgnoreCase("RTGS"))
				priorty = "0001";

			fields.remove(FieldType.TXNTYPE.getName());

			String apiKey;

			if (iciciResponseHandler.isPaybleMerchant(fields)) {
				apiKey = PropertiesManager.propertiesMap.get("PAYBLE_ICICI_COMPOSITE_API_KEY");
			} else {
				apiKey = PropertiesManager.propertiesMap.get("ICICI_COMPOSITE_API_KEY");
			}

			String request = iciciTransactionConverter.createStatusCheckNeftRtgs(fields);
			String response = iciciCommunicator.getIciciCompositeNeftRtgsApiResponse(request, url, fields, priorty,
					apiKey);
			if (StringUtils.isNotBlank(response) && !response.equalsIgnoreCase("400") && !response.equalsIgnoreCase("402") && !response.equalsIgnoreCase("403") && !response.equalsIgnoreCase("501")
					&& !response.equalsIgnoreCase("502") && !response.equalsIgnoreCase("503")
					&& !response.equalsIgnoreCase("500")){
				iciciResponseHandler.compositeNeftRtgsStatusEnqResponseHandler(fields, response);
			}else{
				fields.put(FieldType.STATUS.getName(), StatusType.FAILED.getName());
				fields.put(FieldType.RESPONSE_MESSAGE.getName(), "Bank Server Down, Please Try again later");
			}
			//
			// fields.put(FieldType.AMOUNT.getName(),
			// Amount.formatAmount(fields.get(FieldType.AMOUNT.getName()),
			// fields.get(FieldType.CURRENCY_CODE.getName())));

			fields.put(FieldType.HASH.getName(), Hasher.getHash(fields));

			return fields.getFields();

		} catch (Exception exception) {
			logger.error("Exception in NEFT/RTGS ", exception);
			if (StringUtils.isNotBlank(fields.get(FieldType.AMOUNT.getName()))
					&& fields.get(FieldType.AMOUNT.getName()).contains(".")) {
				fields.put(FieldType.AMOUNT.getName(), Amount.formatAmount(fields.get(FieldType.AMOUNT.getName()),
						fields.get(FieldType.CURRENCY_CODE.getName())));
			}
			fields.put(FieldType.STATUS.getName(), StatusType.ERROR.getName());
			fields.put(FieldType.RESPONSE_MESSAGE.getName(), ErrorType.COMMON_ERROR.getResponseMessage());
			fields.put(FieldType.RESPONSE_CODE.getName(), ErrorType.COMMON_ERROR.getResponseCode());
			fields.put(FieldType.HASH.getName(), Hasher.getHash(fields));

			return fields.getFields();
		}
	}

}
