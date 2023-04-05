package com.paymentgateway.pgui.action;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.api.TransactionControllerServiceProvider;
import com.paymentgateway.commons.util.AcquirerType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.pg.core.util.RequestCreator;
import com.paymentgateway.pgui.action.service.ActionService;
import com.paymentgateway.pgui.action.service.PgActionServiceFactory;

@Service
public class UpiRedirectAction {

	private static Logger logger = LoggerFactory.getLogger(UpiRedirectAction.class.getName());

	@Autowired
	private TransactionControllerServiceProvider transactionControllerServiceProvider;

	@Autowired
	private RequestCreator requestCreator;

	public void upiRedirect(HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException {

		try {

			Fields fields = null;
			ActionService service = PgActionServiceFactory.getActionService();
			Fields newfields = service.prepareFields(httpRequest.getParameterMap());

			if (newfields.contains(FieldType.CHECKOUT_JS_FLAG.getName())
					&& newfields.get(FieldType.CHECKOUT_JS_FLAG.getName()).equalsIgnoreCase("true")) {
				httpRequest.getSession().invalidate();
			} else {
				fields = (Fields) httpRequest.getSession().getAttribute(Constants.FIELDS.getValue());
			}
			if (null != fields) {
			} else {
				if (StringUtils.isNotBlank(PropertiesManager.propertiesMap.get("ADMIN_PAYID"))
						&& StringUtils.isNotBlank(newfields.get("encSessionData"))) {
					Map<String, String> fieldsMap = new HashMap<String, String>();
					Map<String, String> responseMap = transactionControllerServiceProvider.hostedDecrypt(
							PropertiesManager.propertiesMap.get("ADMIN_PAYID"), newfields.get("encSessionData"));
					if (!responseMap.isEmpty()) {
						String decryptedString = responseMap.get(FieldType.ENCDATA.getName());
						String[] fieldArray = decryptedString.split("~");

						for (String key : fieldArray) {
							String[] namValuePair = key.split("=", 2);
							httpRequest.getSession().setAttribute(namValuePair[0], namValuePair[1]);
						}
					}
					String sessionFields = (String) httpRequest.getSession().getAttribute(Constants.FIELDS.getValue());
					sessionFields = sessionFields.substring(1, sessionFields.length() - 1);
					List<String> fieldList = split(sessionFields);
					// String refinedFields = sessionFields.replace(cahfreeRequest, "");
//					String[] refinedArray = sessionFields.trim().split(",");
					for (String key : fieldList) {
						if (key.charAt(0) == ' ') {
							key = key.replaceFirst("^\\s*", "");
						}
						String[] namValuePair = key.trim().split("=", 2);
						fieldsMap.put(namValuePair[0], namValuePair[1]);
					}
					fields = new Fields(fieldsMap);
					logger.info(fieldsMap.toString());

				} else {
					logger.info("session fields lost");
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

			String acquirer = fields.get(FieldType.ACQUIRER_TYPE.getName());

			if (acquirer.equalsIgnoreCase(AcquirerType.BOB.getCode())) {
				requestCreator.generateBobRequest(fields, httpResponse);
			} else if (acquirer.equalsIgnoreCase(AcquirerType.PAYPHI.getCode())) {
				requestCreator.generatePayphiRequest(fields, httpResponse);
			} else if (acquirer.equalsIgnoreCase(AcquirerType.PAYU.getCode())) {
				requestCreator.generatePayuRequest(fields, httpResponse);
			} else if (acquirer.equalsIgnoreCase(AcquirerType.FSSPAY.getCode())) {
				requestCreator.generateFssPayRequest(fields, httpResponse);
			} else if (acquirer.equalsIgnoreCase(AcquirerType.APEXPAY.getCode())) {
				requestCreator.generateApexPayRequest(fields, httpResponse);
			}else if (acquirer.equalsIgnoreCase(AcquirerType.AIRPAY.getCode())) {
				requestCreator.generateAirPayPeRequest(fields, httpResponse);
			} else if (acquirer.equalsIgnoreCase(AcquirerType.QAICASH.getCode())) {
				requestCreator.generateQaicashRequest(fields, httpResponse);
			} else if (acquirer.equalsIgnoreCase(AcquirerType.FLOXYPAY.getCode())) {
				requestCreator.generateFloxypayRequest(fields, httpResponse);
			} else if (acquirer.equalsIgnoreCase(AcquirerType.DIGITALSOLUTIONS.getCode())) {
				requestCreator.generateDigitalSolutionRequest(fields, httpResponse);
			} else if (acquirer.equalsIgnoreCase(AcquirerType.GREZPAY.getCode())) {
				requestCreator.generateGrezpayRequest(fields, httpResponse);
			}  else if (acquirer.equalsIgnoreCase(AcquirerType.UPIGATEWAY.getCode())) {
				requestCreator.generateUpigatewayRequest(fields, httpResponse);
			} else if (acquirer.equalsIgnoreCase(AcquirerType.GLOBALPAY.getCode())) {
				requestCreator.generateGlobalpayRequest(fields, httpResponse);
			} 


//			return getAcquirerFlag();
		} catch (Exception e) {
			logger.error("Exception", e);
			String path = httpRequest.getContextPath();
			logger.info(path);
			if (!StringUtils.isNotBlank(path) && !path.contains("pgui")) {
				String resultPath = httpRequest.getScheme() + "://" + httpRequest.getHeader("Host") + "/pgui/jsp/error";
				httpResponse.sendRedirect(resultPath);
			}
			httpResponse.sendRedirect("error");
		}

	}

	public static List<String> split(String input) {
		int nParens = 0;
		int start = 0;
		List<String> result = new ArrayList<>();
		for (int i = 0; i < input.length(); i++) {
			switch (input.charAt(i)) {
			case ',':
				if (nParens == 0) {
					result.add(input.substring(start, i));
					start = i + 1;
				}
				break;
			case '{':
				nParens++;
				break;
			case '}':
				nParens--;
				if (nParens < 0)
					throw new IllegalArgumentException("Unbalanced parenthesis at offset #" + i);
				break;
			case '<':
				nParens++;
				break;
			case '>':
				nParens--;
				if (nParens < 0)
					throw new IllegalArgumentException("Unbalanced parenthesis at offset #" + i);
				break;

			}
		}
		if (nParens > 0)
			throw new IllegalArgumentException("Missing closing parenthesis");
		result.add(input.substring(start));
		return result;
	}

}
