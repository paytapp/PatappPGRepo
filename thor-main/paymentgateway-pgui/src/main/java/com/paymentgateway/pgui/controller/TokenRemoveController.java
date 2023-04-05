package com.paymentgateway.pgui.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.paymentgateway.commons.api.TransactionControllerServiceProvider;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.user.NBToken;
import com.paymentgateway.commons.user.Token;
import com.paymentgateway.commons.user.VpaToken;
import com.paymentgateway.commons.user.WLToken;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.MopType;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.pgui.dao.RequestAction;

@Controller
@RequestMapping(value = "/jsp")
public class TokenRemoveController {
	@Autowired
	private TransactionControllerServiceProvider transactionControllerServiceProvider;

	@Autowired
	private RequestAction requestAction;

	private static Logger logger = LoggerFactory.getLogger(TokenRemoveController.class.getName());

	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/deleteCard", method = RequestMethod.POST)
	public @ResponseBody Map<String, String> deleteSavedCard(HttpServletRequest request, HttpServletResponse res,
			@RequestBody Map<String, String> reqMap) throws SystemException {
		if (reqMap.containsKey(FieldType.CHECKOUT_JS_FLAG.getName())
				&& reqMap.get(FieldType.CHECKOUT_JS_FLAG.getName()).equalsIgnoreCase("true")) {
			request.getSession().invalidate();
			retriveSession(request, reqMap);
		}
		Map<String, String> cardTokenData = new HashMap<String, String>();
		logger.info("removing save card token with token id >>> " + reqMap.get("tokenId"));
		requestAction.deleteSavedCard(request, reqMap);
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
			cardTokenData.put("cardToken", cardTokenArray.toString());
		} else {
			cardTokenData.put("cardToken",
					(String) request.getSession().getAttribute(Constants.TOKEN.getValue()).toString());
		}
		return cardTokenData;
	}

	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/deletePayablecard", method = RequestMethod.POST)
	public @ResponseBody Map<String, String> deletePayablecard(HttpServletRequest request, HttpServletResponse res,
			@RequestBody Map<String, String> reqMap) throws SystemException {
		if (reqMap.containsKey(FieldType.CHECKOUT_JS_FLAG.getName())
				&& reqMap.get(FieldType.CHECKOUT_JS_FLAG.getName()).equalsIgnoreCase("true")) {
			request.getSession().invalidate();
			retriveSession(request, reqMap);
		}
		Map<String, String> cardTokenData = new HashMap<String, String>();
		logger.info("removing save card token with token id >>> " + reqMap.get("tokenId"));
		requestAction.deleteSavedCard(request, reqMap);
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
			cardTokenData.put("cardToken", cardTokenArray.toString());
		} else {
			cardTokenData.put("cardToken",
					(String) request.getSession().getAttribute(Constants.TOKEN.getValue()).toString());
		}
		return cardTokenData;
	}

	@RequestMapping(value = "/deleteVpa", method = RequestMethod.POST)
	public @ResponseBody Map<String, String> deleteVpa(HttpServletRequest request, HttpServletResponse res,
			@RequestBody Map<String, String> reqMap) throws SystemException {
		if (reqMap.containsKey(FieldType.CHECKOUT_JS_FLAG.getName())
				&& reqMap.get(FieldType.CHECKOUT_JS_FLAG.getName()).equalsIgnoreCase("true")) {
			request.getSession().invalidate();
			retriveSession(request, reqMap);
		}
		Map<String, String> vpaTokenData = new HashMap<String, String>();
		logger.info("removing vpa token with token id >>> " + reqMap.get("tokenId"));
		requestAction.deleteVpa(request, reqMap);
		if (!request.getSession().getAttribute(Constants.VPA_TOKEN.getValue()).toString().equalsIgnoreCase("NA")) {
			JSONObject vpaTokenJson;
			JSONArray vpaTokenArray = new JSONArray();
			@SuppressWarnings("unchecked")
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
			vpaTokenData.put("vpaToken", vpaTokenArray.toString());
		} else {
			vpaTokenData.put("vpaToken",
					(String) request.getSession().getAttribute(Constants.VPA_TOKEN.getValue()).toString());
		}
		return vpaTokenData;
	}

	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/deleteNbToken", method = RequestMethod.POST)
	public @ResponseBody Map<String, String> deleteNbToken(HttpServletRequest request, HttpServletResponse res,
			@RequestBody Map<String, String> reqMap) throws SystemException {
		if (reqMap.containsKey(FieldType.CHECKOUT_JS_FLAG.getName())
				&& reqMap.get(FieldType.CHECKOUT_JS_FLAG.getName()).equalsIgnoreCase("true")) {
			request.getSession().invalidate();
			retriveSession(request, reqMap);
		}
		Map<String, String> nbTokenData = new HashMap<String, String>();
		logger.info("removing Netbanking token with token id >>> " + reqMap.get("tokenId"));
		requestAction.deleteNbToken(request, reqMap);
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
			nbTokenData.put("nbToken", nbTokenArray.toString());
		} else {
			nbTokenData.put("nbToken",
					(String) request.getSession().getAttribute(Constants.NB_TOKEN.getValue()).toString());
		}
		return nbTokenData;
	}

	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/deleteWlToken", method = RequestMethod.POST)
	public @ResponseBody Map<String, String> deleteWlToken(HttpServletRequest request, HttpServletResponse res,
			@RequestBody Map<String, String> reqMap) throws SystemException {
		if (reqMap.containsKey(FieldType.CHECKOUT_JS_FLAG.getName())
				&& reqMap.get(FieldType.CHECKOUT_JS_FLAG.getName()).equalsIgnoreCase("true")) {
			request.getSession().invalidate();
			retriveSession(request, reqMap);
		}
		Map<String, String> wlTokenData = new HashMap<String, String>();
		logger.info("removing wallet token with token id >>> " + reqMap.get("tokenId"));
		requestAction.deleteWlToken(request, reqMap);
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
			wlTokenData.put("wlToken", wlTokenArray.toString());
		} else {
			wlTokenData.put("wlToken",
					(String) request.getSession().getAttribute(Constants.WL_TOKEN.getValue()).toString());
		}
		return wlTokenData;
	}

	private void retriveSession(HttpServletRequest request, Map<String, String> reqMap) throws SystemException {
		request.getSession().invalidate();
		if (StringUtils.isNotBlank(PropertiesManager.propertiesMap.get("ADMIN_PAYID"))
				&& StringUtils.isNotBlank(reqMap.get("encSessionData"))) {
			Map<String, String> responseMap = transactionControllerServiceProvider
					.hostedDecrypt(PropertiesManager.propertiesMap.get("ADMIN_PAYID"), reqMap.get("encSessionData"));
			if (!responseMap.isEmpty()) {
				String decryptedString = responseMap.get(FieldType.ENCDATA.getName());
				String[] fieldArray = decryptedString.split("~");

				for (String key : fieldArray) {
					String[] namValuePair = key.split("=", 2);
					request.getSession().setAttribute(namValuePair[0], namValuePair[1]);
				}
			}
		}
	}
}
