package com.paymentgateway.pgui.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.paymentgateway.commons.api.TransactionControllerServiceProvider;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.user.NBToken;
import com.paymentgateway.commons.user.Token;
import com.paymentgateway.commons.user.VpaToken;
import com.paymentgateway.commons.user.WLToken;
import com.paymentgateway.commons.util.ChecksumUtils;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.MopType;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.pgui.action.DownloadInvoicePDFAction;
import com.paymentgateway.pgui.action.ENachAction;
import com.paymentgateway.pgui.action.EPOSPay;
import com.paymentgateway.pgui.action.EmiSlabAction;
import com.paymentgateway.pgui.action.EnachRequestAction;
import com.paymentgateway.pgui.action.InvoicePay;
import com.paymentgateway.pgui.action.MerchantHostedRequestAction;
import com.paymentgateway.pgui.action.QRRequestAction;
import com.paymentgateway.pgui.action.SearchTransactionAction;
import com.paymentgateway.pgui.action.SearchTransactionGpayAction;
import com.paymentgateway.pgui.action.SendMail;
import com.paymentgateway.pgui.action.StaticPgQrPay;
import com.paymentgateway.pgui.action.UPIQRRequestAction;
import com.paymentgateway.pgui.action.UpiAutoPayMandateAction;
import com.paymentgateway.pgui.action.UpiAutoPayRequestAction;
import com.paymentgateway.pgui.action.UpiRedirectAction;
import com.paymentgateway.pgui.action.UpiRequestAction;
import com.paymentgateway.pgui.action.service.ActionService;
import com.paymentgateway.pgui.action.service.PgActionServiceFactory;
import com.paymentgateway.pgui.dao.RequestAction;
//import com.paymentgateway.pgui.session.SessionTimeoutHandler;

@Controller
@RequestMapping(value = "/jsp")
public class RequestController {

	@Autowired
	private RequestAction requestAction;

	@Autowired
	private MerchantHostedRequestAction merchantHostedRequestAction;

	// @Autowired
	// private SessionTimeoutHandler sessionTimeoutHandler;

	@Autowired
	private UpiRedirectAction upiRedirectAction;

	@Autowired
	private UpiRequestAction upiRequestAction;

	@Autowired
	private SearchTransactionAction searchTransactionAction;

	@Autowired
	private SearchTransactionGpayAction searchTransactionGpayAction;

	@Autowired
	private PropertiesManager propertiesManager;

	@Autowired
	private UPIQRRequestAction upiqrRequestAction;

	@Autowired
	private EnachRequestAction enachRequestAction;

	@Autowired
	private ENachAction eNachAction;

	@Autowired
	private UpiAutoPayRequestAction upiAutoPayRequestAction;

	@Autowired
	private TransactionControllerServiceProvider transactionControllerServiceProvider;

	@Autowired
	private InvoicePay invoicePay;

	@Autowired
	private UpiAutoPayMandateAction upiAutoPayMandateAction;

	@Autowired
	private EPOSPay eposPay;

	@Autowired
	private StaticPgQrPay staticPgQrPay;

	@Autowired
	private EmiSlabAction emiSlabAction;

	@Autowired
	private SendMail senMail;

	@Autowired
	private DownloadInvoicePDFAction downloadInvoicePDFAction;

	@Autowired
	private QRRequestAction qrRequestAction;

	private static Logger logger = LoggerFactory.getLogger(RequestController.class.getName());

	@SuppressWarnings({ "static-access", "unchecked" })
	@RequestMapping(value = "/paymentrequest", method = RequestMethod.POST)
	public ModelAndView paymentRequest(HttpServletRequest request, HttpServletResponse response,
			@ModelAttribute FormDataFields user) throws IOException, SystemException {
		Map<String, String> pgPageData = new HashMap<String, String>();
		JSONObject pgPageDataJson = new JSONObject();
		ModelAndView modelAndView = new ModelAndView();
		pgPageData = requestAction.paymentPageRequest(request, response);
		if (pgPageData.containsKey("suportedPaymentTypeMap")) {
			user.setAMOUNT((String) request.getSession().getAttribute(FieldType.AMOUNT.getName()).toString());
			pgPageDataJson.put("userData", user.toJsonData());
			pgPageDataJson.put("suportedPaymentTypeMap", pgPageData.get("suportedPaymentTypeMap"));

			if (!request.getSession().getAttribute(Constants.TOKEN.getValue()).toString().equalsIgnoreCase("NA")) {
				JSONObject cardTokenJson;
				JSONArray cardTokenArray = new JSONArray();
				HashMap<String, Token> cardtoken = (HashMap<String, Token>) request.getSession()
						.getAttribute(Constants.TOKEN.getValue());
				for (String key : cardtoken.keySet()) {
					Token values = (Token) cardtoken.get(key);
					cardTokenJson = new JSONObject();
					cardTokenJson.put("paymentType", values.getPaymentType());
					cardTokenJson.put("expiryDate", values.getExpiryDate());
					cardTokenJson.put("mopType", values.getMopType());
					cardTokenJson.put("cardHolderType", values.getCardHolderType());
					cardTokenJson.put("paymentsRegion", values.getPaymentsRegion());
					cardTokenJson.put("key", values.getTokenId());
					cardTokenJson.put("cardIssuerBank", values.getCardIssuerBank());
					cardTokenJson.put("cardMask", values.getCardMask());
					cardTokenArray.put(cardTokenJson);
				}
				pgPageDataJson.put("cardToken", cardTokenArray.toString());
			} else {
				pgPageDataJson.put("cardToken",
						(String) request.getSession().getAttribute(Constants.TOKEN.getValue()).toString());
			}
			if (!request.getSession().getAttribute(Constants.VPA_TOKEN.getValue()).toString().equalsIgnoreCase("NA")) {
				JSONObject vpaTokenJson;
				JSONArray vpaTokenArray = new JSONArray();
				HashMap<String, VpaToken> vpaToken = (HashMap<String, VpaToken>) request.getSession()
						.getAttribute(Constants.VPA_TOKEN.getValue());
				for (String key : vpaToken.keySet()) {
					VpaToken values = (VpaToken) vpaToken.get(key);
					vpaTokenJson = new JSONObject();
					vpaTokenJson.put("key", values.getTokenId());
					vpaTokenJson.put("vpa", values.getVpa());
					vpaTokenJson.put("vpaMask", values.getVpaMask());
					vpaTokenArray.put(vpaTokenJson);
				}
				pgPageDataJson.put("vpaToken", vpaTokenArray.toString());
			} else {
				pgPageDataJson.put("vpaToken",
						(String) request.getSession().getAttribute(Constants.VPA_TOKEN.getValue()).toString());
			}
			if (!request.getSession().getAttribute(Constants.NB_TOKEN.getValue()).toString().equalsIgnoreCase("NA")) {
				JSONObject nbTokenJson;
				JSONArray nbTokenArray = new JSONArray();
				HashMap<String, NBToken> nbtoken = (HashMap<String, NBToken>) request.getSession()
						.getAttribute(Constants.NB_TOKEN.getValue());
				for (String key : nbtoken.keySet()) {
					NBToken values = (NBToken) nbtoken.get(key);
					nbTokenJson = new JSONObject();
					nbTokenJson.put("key", values.getTokenId());
					nbTokenJson.put("code", values.getMopType());
					nbTokenJson.put("value", MopType.getmopName(values.getMopType()));
					nbTokenArray.put(nbTokenJson);
				}
				pgPageDataJson.put("nbToken", nbTokenArray.toString());
			} else {
				pgPageDataJson.put("nbToken",
						(String) request.getSession().getAttribute(Constants.NB_TOKEN.getValue()).toString());
			}

			if (!request.getSession().getAttribute(Constants.WL_TOKEN.getValue()).toString().equalsIgnoreCase("NA")) {
				JSONObject wlTokenJson;
				JSONArray wlTokenArray = new JSONArray();
				HashMap<String, WLToken> wltoken = (HashMap<String, WLToken>) request.getSession()
						.getAttribute(Constants.WL_TOKEN.getValue());
				for (String key : wltoken.keySet()) {
					WLToken values = (WLToken) wltoken.get(key);
					if (!values.getMopType().equalsIgnoreCase(MopType.MOBIKWIK_WALLET.getCode())) {
						wlTokenJson = new JSONObject();
						wlTokenJson.put("key", values.getTokenId());
						wlTokenJson.put("code", values.getMopType());
						wlTokenJson.put("value", MopType.getmopName(values.getMopType()));
						wlTokenArray.put(wlTokenJson);
					}
				}
				pgPageDataJson.put("wlToken", wlTokenArray.toString());
			} else {
				pgPageDataJson.put("wlToken",
						(String) request.getSession().getAttribute(Constants.WL_TOKEN.getValue()).toString());
			}
			// pgPageDataJson.put("sessionCreationTime",
			// (String)
			// request.getSession().getAttribute("sessionCreationTime"));

			if (request.getSession().getAttribute(FieldType.CHECKOUT_JS_FLAG.getName()).toString()
					.equalsIgnoreCase("Y")) {
				Map<String, String> sessionSavedFieldsMap = new HashMap<String, String>();
				Fields sessionSavedFields = (Fields) request.getSession().getAttribute(Constants.FIELDS.getValue());
				for (String keyFields : sessionSavedFields.keySet()) {
					sessionSavedFieldsMap.put(keyFields, sessionSavedFields.get(keyFields));
				}

				request.getSession().setAttribute(Constants.FIELDS.getValue(), sessionSavedFieldsMap.toString());
				Map<String, String> sessionValues = new HashMap<String, String>();
				Enumeration<String> sessionFields = request.getSession().getAttributeNames();
				for (String key : Collections.list(sessionFields)) {
					if (StringUtils.isNotBlank(request.getSession().getAttribute(key).toString())) {
						sessionValues.put(key, request.getSession().getAttribute(key).toString());
					}
				}
				String encSessionString = ChecksumUtils.getString(sessionValues);
				String encSessionData = transactionControllerServiceProvider
						.hostedEncrypt(propertiesManager.propertiesMap.get("ADMIN_PAYID"), encSessionString)
						.get(FieldType.ENCDATA.getName());
				pgPageDataJson.put("encSessionData", encSessionData);
			}
			pgPageDataJson.put("PAYBLE_PAY_ID", propertiesManager.propertiesMap.get("PAYBLE_PAY_ID"));
			pgPageDataJson.put("KASHI_PAY_ID", propertiesManager.propertiesMap.get("KASHI_PAY_ID"));
			pgPageDataJson.put("BLUE_WHALE_PAYID", propertiesManager.propertiesMap.get("BLUE_WHALE_PAYID"));
			pgPageDataJson.put("VERVE_PAY_ID", propertiesManager.propertiesMap.get("VERVE_PAY_ID"));
			pgPageDataJson.put("PAYTENSE_PAY_ID", propertiesManager.propertiesMap.get("PAYTENSE_PAY_ID"));
			pgPageDataJson.put("SMT_SUPER_MERCHANT_PAYID",
					propertiesManager.propertiesMap.get("SMT_SUPER_MERCHANT_PAYID"));
			pgPageDataJson.put("IS_MOBIKWIK_WALLET", propertiesManager.propertiesMap.get("IS_MOBIKWIK_WALLET"));
			pgPageDataJson.put("MOBIKWIK_WALLET_PAY_ID", propertiesManager.propertiesMap.get("MOBIKWIK_WALLET_PAY_ID"));
			pgPageDataJson.put("SEPARATED_PHONEPE_PAYID",
					propertiesManager.propertiesMap.get("SEPARATED_PHONEPE_PAYID"));
			pgPageDataJson.put("IS_PAYTM_WALLET", propertiesManager.propertiesMap.get("IS_PAYTM_WALLET"));
			pgPageDataJson.put("PAYTM_WALLET_PAY_ID", propertiesManager.propertiesMap.get("PAYTM_WALLET_PAY_ID"));

			// for regex and printing logs
			requestAction.log("data from preparePaymentPage >>>>>>" + pgPageDataJson.toString());
			modelAndView.addObject("pageData", pgPageDataJson);

			if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("PAYBLE_PAY_ID"))
					&& propertiesManager.propertiesMap.get("PAYBLE_PAY_ID").contains(user.getPAY_ID())) {
				modelAndView.setViewName("payblePayment");
			} else if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("PAYTENSE_PAY_ID"))
					&& propertiesManager.propertiesMap.get("PAYTENSE_PAY_ID").contains(user.getPAY_ID())) {
				modelAndView.setViewName("paytensePayment");
			} else if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("BLUE_WHALE_PAYID"))
					&& propertiesManager.propertiesMap.get("BLUE_WHALE_PAYID").contains(user.getPAY_ID())) {
				modelAndView.setViewName("blueWhalePayment");
			} else if (StringUtils.isNotBlank(propertiesManager.propertiesMap.get("VERVE_PAY_ID"))
					&& propertiesManager.propertiesMap.get("VERVE_PAY_ID").contains(user.getPAY_ID())) {
				modelAndView.setViewName("vervePayment");
			} else if (request.getSession().getAttribute(FieldType.CHECKOUT_JS_FLAG.getName()).toString()
					.equalsIgnoreCase("Y")) {
				modelAndView.setViewName("checkout/index");
			} else {
				modelAndView.setViewName("index");
			}
		} else {
			for (String keyFields : pgPageData.keySet()) {
				pgPageDataJson.put(keyFields, pgPageData.get(keyFields));
			}
			modelAndView.addObject("pageData", pgPageDataJson);
			modelAndView.setViewName("response");
		}
		return modelAndView;
	}

	@RequestMapping(value = "/addAndPayRequest", method = RequestMethod.POST)
	public ModelAndView addAndPayRequest(HttpServletRequest request, HttpServletResponse res)
			throws IOException, SystemException {
		Map<String, String> addAndPayRequestMap = new HashMap<String, String>();
		ModelAndView modelAndView = new ModelAndView();

		JSONObject addAndPayObj = new JSONObject();

		JSONArray nbList = new JSONArray();
		String paytmNbList[] = (propertiesManager.propertiesMap.get("PAYTMNBMOP")).split(",");
		int paytmNbLength = paytmNbList.length;
		for (int i = 0; i < paytmNbLength; i++) {
			nbList.put(MopType.getmopName(paytmNbList[i]));
		}

		JSONArray creditCardList = new JSONArray();
		String creditCardArr[] = (propertiesManager.propertiesMap.get("PAYTMCCMOP")).split(",");
		int paytmCreditCardLength = creditCardArr.length;
		for (int i = 0; i < paytmCreditCardLength; i++) {
			creditCardList.put(creditCardArr[i]);
		}

		JSONArray debitCardList = new JSONArray();
		String debitCardArr[] = (propertiesManager.propertiesMap.get("PAYTMDCMOP")).split(",");
		int paytmDebitCardLength = debitCardArr.length;
		for (int i = 0; i < paytmDebitCardLength; i++) {
			debitCardList.put(debitCardArr[i]);
		}

		addAndPayObj.put("nbMopType", nbList);
		addAndPayObj.put("ccMopTypes", creditCardList);
		addAndPayObj.put("dcMopTypes", debitCardList);

		Map<String, String> sessionSavedFieldsMap = new HashMap<String, String>();
		Fields sessionSavedFields = (Fields) request.getSession().getAttribute(Constants.FIELDS.getValue());
		for (String keyFields : sessionSavedFields.keySet()) {
			sessionSavedFieldsMap.put(keyFields, sessionSavedFields.get(keyFields));
		}
		request.getSession().setAttribute(Constants.FIELDS.getValue(), sessionSavedFieldsMap.toString());
		Map<String, String> sessionValues = new HashMap<String, String>();
		Enumeration<String> sessionFields = request.getSession().getAttributeNames();
		for (String key : Collections.list(sessionFields)) {
			if (StringUtils.isNotBlank(request.getSession().getAttribute(key).toString())) {
				sessionValues.put(key, request.getSession().getAttribute(key).toString());
			}
		}
		String encSessionString = ChecksumUtils.getString(sessionValues);
		String encSessionData = transactionControllerServiceProvider
				.hostedEncrypt(propertiesManager.propertiesMap.get("ADMIN_PAYID"), encSessionString)
				.get(FieldType.ENCDATA.getName());
		addAndPayObj.put("encSessionData", encSessionData);

		modelAndView.addObject("pageData", addAndPayObj);
		modelAndView.setViewName("checkout/index");
		return modelAndView;
	}

	@RequestMapping(value = "/hostedpaymentrequest", method = RequestMethod.POST)
	public ModelAndView merchantHostedRequest(HttpServletRequest request, HttpServletResponse response,
			@ModelAttribute FormDataFields user) throws IOException {
		Map<String, String> pgPageData = new HashMap<String, String>();
		JSONObject pgPageDataJson = new JSONObject();
		ModelAndView modelAndView = new ModelAndView();
		pgPageData = merchantHostedRequestAction.merchantHostedPaymentPageRequest(request, response);
		if (pgPageData.containsKey("upiLoader") && pgPageData.get("upiLoader").equalsIgnoreCase("y")) {
			for (String keyFields : pgPageData.keySet()) {
				if (!keyFields.equalsIgnoreCase("upiLoader")) {
					pgPageDataJson.put(keyFields, pgPageData.get(keyFields));
				}
			}
			modelAndView.addObject("pageData", pgPageDataJson);
			modelAndView.setViewName("upiMerchantHosted");
			return modelAndView;
		}
		return null;
	}

	@RequestMapping(value = "/sessionTimeout", method = RequestMethod.POST)
	public ModelAndView sessionTimeout(HttpServletRequest request, HttpServletResponse res) throws SystemException {

		ModelAndView modelAndView = new ModelAndView();
		ActionService service = PgActionServiceFactory.getActionService();
		Fields fields = service.prepareFields(request.getParameterMap());

		// sessionTimeoutHandler.handleTimeOut(fields);

		modelAndView.setViewName("sessionTimeout");
		return modelAndView;
	}

	@RequestMapping(value = "/txncancel", method = RequestMethod.POST)
	public ModelAndView cancel(HttpServletRequest request, HttpServletResponse response) {
		Map<String, String> cancelResponse = new HashMap<String, String>();
		ModelAndView modelAndView = new ModelAndView();
		JSONObject pgPageCancelResponseJson = new JSONObject();
		cancelResponse = requestAction.cancelByUser(request, response);
		logger.info("response from txncancel controller >> " + cancelResponse);
		for (String keyFields : cancelResponse.keySet()) {
			pgPageCancelResponseJson.put(keyFields, cancelResponse.get(keyFields));
		}
		modelAndView.addObject("pageData", pgPageCancelResponseJson);
		modelAndView.setViewName("response");
		return modelAndView;
	}

	@RequestMapping(value = "/pay", method = RequestMethod.POST)
	public void pay(HttpServletRequest request, HttpServletResponse res) throws IOException {
		requestAction.acquirerHandler(request, res);
	}

	@RequestMapping(value = "/returnToMerchant", method = RequestMethod.POST)
	public void returnToMerchant(HttpServletRequest request, HttpServletResponse res) {
		requestAction.returnToMerchant(request, res);
	}

	@RequestMapping(value = "/redirectToPaymentPage", method = RequestMethod.POST)
	public ModelAndView redirectToPaymentPage(HttpServletRequest request, HttpServletResponse res) throws IOException {
		Map<String, String> redirectToPaymentPageResponse = new HashMap<String, String>();
		ModelAndView modelAndView = new ModelAndView();
		JSONObject redirectToPaymentPageResponseJson = new JSONObject();
		redirectToPaymentPageResponse = requestAction.redirectToPaymentPage(request, res);
		logger.info("response from txncancel controller >> " + redirectToPaymentPageResponse);
		if (redirectToPaymentPageResponse.containsKey("timeout")
				&& redirectToPaymentPageResponse.get("timeout").equalsIgnoreCase("timeOut")) {
			modelAndView.setViewName("sessionTimeout");
			return modelAndView;
		}
		for (String keyFields : redirectToPaymentPageResponse.keySet()) {
			redirectToPaymentPageResponseJson.put(keyFields, redirectToPaymentPageResponse.get(keyFields));
		}
		modelAndView.addObject("pageData", redirectToPaymentPageResponseJson);
		modelAndView.setViewName("paymentrequest");
		return modelAndView;
	}

	@RequestMapping(value = "/upiRedirect", method = RequestMethod.POST)
	public void upiRedirect(HttpServletRequest request, HttpServletResponse res) throws IOException {
		upiRedirectAction.upiRedirect(request, res);
	}

	@RequestMapping(value = "/upiQrPay", method = RequestMethod.POST)
	public @ResponseBody Map<String, String> upiQrPayRequest(HttpServletRequest request, HttpServletResponse res,
			@RequestBody Map<String, String> reqmap) throws IOException {
		Map<String, String> upiQrPayResponse = new HashMap<String, String>();
		upiQrPayResponse = upiqrRequestAction.upiqrRequestHandler(request, res, reqmap);
		logger.info("data from upiQrPay >>>>>> " + upiQrPayResponse.toString());
		return upiQrPayResponse;
	}

	@RequestMapping(value = "/upiPay", method = RequestMethod.POST)
	public @ResponseBody Map<String, String> upiPay(HttpServletRequest request, HttpServletResponse res,
			@RequestBody Map<String, String> reqmap) throws IOException, SystemException {
		Map<String, String> upiPayResponseMap = new HashMap<String, String>();
		Map<String, String> upiPayResponse = new HashMap<String, String>();
		JSONObject upiPayResponseFields = new JSONObject();

		upiPayResponseMap = upiRequestAction.upiRequestHandler(request, res, reqmap);
		logger.info("response from upi class >> " + upiPayResponseMap);
		if (upiPayResponseMap.get(FieldType.RESPONSE_CODE.getName())
				.equalsIgnoreCase(ErrorType.DENIED_BY_FRAUD.getResponseCode())) {
			for (String keyFields : upiPayResponseMap.keySet()) {
				upiPayResponseFields.put(keyFields, upiPayResponseMap.get(keyFields));
			}
			upiPayResponse.put("responseFields", upiPayResponseFields.toString());
		} else {
			upiPayResponse.put("mopType", upiPayResponseMap.get(FieldType.MOP_TYPE.getName()));
			upiPayResponse.put("paymentType", upiPayResponseMap.get(FieldType.PAYMENT_TYPE.getName()));
			upiPayResponse.put("paymentsRegion", upiPayResponseMap.get(FieldType.PAYMENTS_REGION.getName()));
			upiPayResponse.put("pgRefNum", upiPayResponseMap.get(FieldType.PG_REF_NUM.getName()));
			upiPayResponse.put("redirectURL", upiPayResponseMap.get("REDIRECT_URL"));
			upiPayResponse.put("responseCode", upiPayResponseMap.get(FieldType.RESPONSE_CODE.getName()));

			upiPayResponseFields.put("RESPONSE_DATE_TIME",
					upiPayResponseMap.get(FieldType.RESPONSE_DATE_TIME.getName()));
			upiPayResponseFields.put("MERCHANT_GST", upiPayResponseMap.get(FieldType.MERCHANT_GST.getName()));
			upiPayResponseFields.put("CUST_ID", upiPayResponseMap.get(FieldType.CUST_ID.getName()));

			upiPayResponse.put("responseFields", upiPayResponseFields.toString());
			upiPayResponse.put("responseMessage", upiPayResponseMap.get(FieldType.RESPONSE_MESSAGE.getName()));
			upiPayResponse.put("transactionStatus", upiPayResponseMap.get(FieldType.STATUS.getName()));
			upiPayResponse.put("txnType", upiPayResponseMap.get(FieldType.TXNTYPE.getName()));
			upiPayResponse.put("vpa", upiPayResponseMap.get(FieldType.PAYER_ADDRESS.getName()));
			upiPayResponse.put("vpaSaveflag", reqmap.get("vpaSaveflag"));
			if (request.getSession().getAttribute(FieldType.CHECKOUT_JS_FLAG.getName()).toString()
					.equalsIgnoreCase("Y")) {
				Map<String, String> sessionSavedFieldsMap = new HashMap<String, String>();
				Fields sessionSavedFields = (Fields) request.getSession().getAttribute(Constants.FIELDS.getValue());
				for (String keyFields : sessionSavedFields.keySet()) {
					sessionSavedFieldsMap.put(keyFields, sessionSavedFields.get(keyFields));
				}

				request.getSession().setAttribute(Constants.FIELDS.getValue(), sessionSavedFieldsMap.toString());
				Map<String, String> sessionValues = new HashMap<String, String>();
				Enumeration<String> sessionFields = request.getSession().getAttributeNames();
				for (String key : Collections.list(sessionFields)) {
					if (StringUtils.isNotBlank(request.getSession().getAttribute(key).toString())) {
						sessionValues.put(key, request.getSession().getAttribute(key).toString());
					}
				}
				String encSessionString = ChecksumUtils.getString(sessionValues);
				String encSessionData = transactionControllerServiceProvider
						.hostedEncrypt(propertiesManager.propertiesMap.get("ADMIN_PAYID"), encSessionString)
						.get(FieldType.ENCDATA.getName());
				upiPayResponse.put("encSessionData", encSessionData);
			}
		}
		return upiPayResponse;
	}

	@RequestMapping(value = "/verifyUpiResponse", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Map<String, String> verifyUpiResponse(HttpServletRequest request, HttpServletResponse res,
			@RequestBody Map<String, String> reqmap) throws IOException {
		Map<String, String> verifyUpiResponseMap = new HashMap<String, String>();
		verifyUpiResponseMap = searchTransactionAction.verifyUpiRequest(request, res, reqmap);
		// logger.info("response from upi class >> " + verifyUpiResponseMap);

		return verifyUpiResponseMap;
	}

	@RequestMapping(value = "/verifyUpiGpayResponse", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public @ResponseBody Map<String, String> verifyUpiGpayResponse(HttpServletRequest request, HttpServletResponse res,
			@RequestBody Map<String, String> reqmap) throws IOException {
		Map<String, String> verifyUpiGpayResponseMap = new HashMap<String, String>();
		verifyUpiGpayResponseMap = searchTransactionGpayAction.verifyUpiGpayRequest(request, res, reqmap);
		// logger.info("response from verifyUpiGpayResponseMap >> " +
		// verifyUpiGpayResponseMap);

		return verifyUpiGpayResponseMap;
	}

	@RequestMapping("/pdf/{fileName:.+}")
	public void downloader(HttpServletRequest request, HttpServletResponse response,
			@PathVariable("fileName") String fileName) {

		String downloadFolder /* = context.getRealPath("/WEB-INF/downloads/") */ = null;
		Path file = Paths.get(downloadFolder, fileName);
		// Check if file exists
		if (Files.exists(file)) {
			// set content type
			response.setContentType("application/pdf");
			// add response header
			response.addHeader("Content-Disposition", "attachment; filename=" + fileName);
			try {
				// copies all bytes from a file to an output stream
				Files.copy(file, response.getOutputStream());
				// flushes output stream
				response.getOutputStream().flush();
			} catch (IOException e) {
				logger.error("Error :- ", e.getMessage());
			}
		} else {
			logger.info("Sorry File not found!!!!");
		}
	}

	@RequestMapping(value = "/enachMandateSign", method = RequestMethod.POST)
	public ModelAndView enachMandateSignRequest(HttpServletRequest request, HttpServletResponse res)
			throws IOException {
		Map<String, String> enachRequestMap = new HashMap<String, String>();
		ModelAndView modelAndView = new ModelAndView();
		JSONObject enachRequestJson = new JSONObject();
		enachRequestMap = enachRequestAction.enachRequestHandler(request, res);
		// logger.info("response from txncancel controller >> " +
		// enachRequestMap);
		for (String keyFields : enachRequestMap.keySet()) {
			enachRequestJson.put(keyFields, enachRequestMap.get(keyFields));
		}
		modelAndView.addObject("pageData", enachRequestJson);
		modelAndView.setViewName("eNachRegistration");
		return modelAndView;
	}

	@RequestMapping(value = "/getEnachFormToken", method = RequestMethod.POST)
	public @ResponseBody Map<String, String> getEnachFormToken(@RequestBody Map<String, String> reqmap) {
		Map<String, String> getEnachFormTokenResponse = new HashMap<String, String>();
		getEnachFormTokenResponse = eNachAction.eNachHandler(reqmap);
		// logger.info("response from getEnachFormToken >> " +
		// getEnachFormTokenResponse);
		return getEnachFormTokenResponse;
	}

	@RequestMapping(value = "/upiAutoPayMandateSign", method = RequestMethod.POST)
	public ModelAndView upiAutoPayMandateSignRequest(HttpServletRequest request, HttpServletResponse res)
			throws IOException {
		Map<String, String> upiAutoPayRequestMap = new HashMap<String, String>();
		ModelAndView modelAndView = new ModelAndView();
		JSONObject upiAutoPayRequestJson = new JSONObject();
		upiAutoPayRequestMap = upiAutoPayRequestAction.upiAutoPayRequestHandler(request, res);
		// logger.info("response from upiAutoPayMandateSign controller >> " +
		// upiAutoPayRequestMap);
		for (String keyFields : upiAutoPayRequestMap.keySet()) {
			upiAutoPayRequestJson.put(keyFields, upiAutoPayRequestMap.get(keyFields));
		}
		modelAndView.addObject("pageData", upiAutoPayRequestJson);
		modelAndView.setViewName("autoPayMandate");
		return modelAndView;
	}

	@RequestMapping(value = "/getUpiAutoPayToken", method = RequestMethod.POST)
	public @ResponseBody Map<String, String> getUpiAutoPayToken(@RequestBody Map<String, String> reqMap,
			HttpServletResponse res) {
		Map<String, String> upiAutoPayTokenResponse = new HashMap<String, String>();
		upiAutoPayTokenResponse = upiAutoPayMandateAction.upiAutoPayMandateHandler(reqMap, res);
		// logger.info("response from getEnachFormToken >> " +
		// upiAutoPayTokenResponse);
		return upiAutoPayTokenResponse;
	}

	@RequestMapping(value = "/payInvoice", method = RequestMethod.GET)
	public ModelAndView payInvoice(HttpServletRequest request, HttpServletResponse res) throws IOException {
		Map<String, String> payInvoiceMap = new HashMap<String, String>();
		ModelAndView modelAndView = new ModelAndView();
		JSONObject payInvoiceJson = new JSONObject();
		payInvoiceMap = invoicePay.invoicePayHandler(request, res);
		// logger.info("response from txncancel controller >> " +
		// payInvoiceMap);
		for (String keyFields : payInvoiceMap.keySet()) {
			payInvoiceJson.put(keyFields, payInvoiceMap.get(keyFields));
		}
		modelAndView.addObject("pageData", payInvoiceJson);
		modelAndView.setViewName("invoicePay");
		return modelAndView;
	}

	@RequestMapping(value = "/eposPay", method = RequestMethod.GET)
	public ModelAndView eposPay(HttpServletRequest request, HttpServletResponse res) throws IOException {
		Map<String, String> eposPayMap = new HashMap<String, String>();
		ModelAndView modelAndView = new ModelAndView();
		JSONObject eposPayJson = new JSONObject();
		eposPayMap = eposPay.ePosRequestHandler(request);
		for (String keyFields : eposPayMap.keySet()) {
			eposPayJson.put(keyFields, eposPayMap.get(keyFields));
		}
		modelAndView.addObject("pageData", eposPayJson);
		modelAndView.setViewName("eposPay");
		return modelAndView;

	}

	@RequestMapping(value = "/staticPgQrPay", method = RequestMethod.GET)
	public ModelAndView staticPgQrPay(HttpServletRequest request, HttpServletResponse res) throws IOException {
		Map<String, String> staticPgQrMap = new HashMap<String, String>();
		ModelAndView modelAndView = new ModelAndView();
		JSONObject staticPgQrJson = new JSONObject();
		staticPgQrMap = staticPgQrPay.staticPgQrPayRequestHandling(request, res);
		for (String keyFields : staticPgQrMap.keySet()) {
			staticPgQrJson.put(keyFields, staticPgQrMap.get(keyFields));
		}
		// logger.info("pageData >> " + staticPgQrJson);
		modelAndView.addObject("pageData", staticPgQrJson);
		modelAndView.setViewName("staticPgQrResponse");
		return modelAndView;

	}

	@RequestMapping(value = "/redirectToPaymentAction", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public void redirectToPaymentAction(HttpServletRequest request, HttpServletResponse response) throws IOException {
		staticPgQrPay.redirectToPayment(request, response);
	}

	@RequestMapping(value = "/fetchEmiDetail", method = RequestMethod.POST)
	public @ResponseBody Map<String, String> fetchEmiDetail(@RequestBody Map<String, String> reqmap) {
		JSONObject fetchEmiDetailJson = new JSONObject();
		Map<String, String> fetchEmiDetailMap = new HashMap<String, String>();
		fetchEmiDetailJson = emiSlabAction.emiSlabRequestHandler(reqmap);
		fetchEmiDetailMap.put("emiSlab", fetchEmiDetailJson.get("emiSlab").toString());
		return fetchEmiDetailMap;

	}

	@RequestMapping(value = "/emiBinCheck", method = RequestMethod.POST)
	public @ResponseBody Map<String, String> emiBinCheack(@RequestBody Map<String, String> reqmap,
			HttpServletRequest request, HttpServletResponse response) {
		Map<String, String> resMap = new HashMap<String, String>();
		try {
			Map<String, String> requestPerameters = new HashMap<String, String>();
			resMap = transactionControllerServiceProvider.emiBincheck(reqmap);
		} catch (SystemException e) {
			e.printStackTrace();
		}
		return resMap;
	}

	@RequestMapping(value = "/sendMail", method = RequestMethod.POST)
	public @ResponseBody Map<String, String> sendMail(@RequestBody Map<String, String> reqmap,
			HttpServletRequest request, HttpServletResponse response) {
		try {
			return senMail.sendMailHandler(reqmap, request);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@PostMapping("/downloadPDF")
	public ResponseEntity<ByteArrayResource> capturedDataDownload(HttpServletRequest request,
			HttpServletResponse response) {
		try {
			logger.info("downloadPDF");
			Map<String, String> fieldMapObj = decodeRequest(request);
			return downloadInvoicePDFAction.merchantInvoicePdf(fieldMapObj.get("ORDER_ID"));

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private Map<String, String> decodeRequest(HttpServletRequest request) {
		Map<String, String[]> fieldMapObj = request.getParameterMap();
		Map<String, String> requestMap = new HashMap<String, String>();
		for (Entry<String, String[]> entry : fieldMapObj.entrySet()) {
			try {
				@SuppressWarnings("deprecation")
				String result = java.net.URLDecoder.decode((entry.getValue())[0]);
				requestMap.put(entry.getKey(), result);
			} catch (ClassCastException classCastException) {
				String path = request.getContextPath();
				logger.error("Exception: " + classCastException);
				logger.info(path);
				return null;
			}
		}
		return requestMap;
	}

	@RequestMapping(value = "/qrPay", method = RequestMethod.POST)
	public @ResponseBody Map<String, String> qrPayRequest(HttpServletRequest request, HttpServletResponse res,
			@RequestBody Map<String, String> reqmap) throws IOException {
		Map<String, String> qrPayResponse = new HashMap<String, String>();
		qrPayResponse = qrRequestAction.qrRequestHandler(request, res, reqmap);
		logger.info("data from MQrPay >>>>>> " + qrPayResponse.toString());
		return qrPayResponse;
	}

	@RequestMapping(value = "/submitUtr", method = RequestMethod.POST)
	public @ResponseBody Map<String, String> submitUtrRequest(HttpServletRequest request, HttpServletResponse res,
			@RequestBody Map<String, String> reqmap) throws IOException, SystemException {
		Map<String, String> submitUtrResponse = new HashMap<String, String>();
		submitUtrResponse = qrRequestAction.submitUtrRequestHandler(request, res, reqmap);
		logger.info("data from submitUtr API >>>>>> " + submitUtrResponse.toString());

		return submitUtrResponse;
	}
}
