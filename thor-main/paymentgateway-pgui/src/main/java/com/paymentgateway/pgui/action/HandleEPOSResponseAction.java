package com.paymentgateway.pgui.action;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.pgui.action.beans.HandleEPOSSaleResponse;

/**
 * @author Amitosh Aanand
 *
 */
@Service
public class HandleEPOSResponseAction {

	@Autowired
	private HandleEPOSSaleResponse handleEPOSSaleResponse;

	private Map<String, String> responseMap = new HashMap<String, String>();

	private static Logger logger = LoggerFactory.getLogger(HandleEPOSResponseAction.class.getName());

	public void httpResponseHandler(HttpServletRequest httpRequest) {
		logger.info("Inside Handle EPOS Pay Response");
		if (httpRequest == null) {
			httpRequest.setAttribute(Constants.STATUS.getValue(), "Transaction Failed");
			logger.info("HttpRequest is null , sending failed response");
//			return SUCCESS;
		}
		Map<String, String[]> fieldMapObj = httpRequest.getParameterMap();
		Map<String, String> requestMap = new HashMap<String, String>();

		for (Entry<String, String[]> entry : fieldMapObj.entrySet()) {
			try {
				requestMap.put(entry.getKey(), ((String[]) entry.getValue())[0]);
			} catch (ClassCastException classCastException) {
				logger.error("Exception", classCastException);
			}
		}

		if (requestMap.get(FieldType.RESPONSE_CODE.name()).equalsIgnoreCase(ErrorType.SUCCESS.getCode())) {
			logger.info("EPOS Sale captured.");
			handleEPOSSaleResponse.handleSuccessResponse(requestMap, httpRequest);
		} else {
			handleEPOSSaleResponse.handleFailureResponse(requestMap, httpRequest);
			logger.info(
					"Unable to capture EPOS sale transction as " + requestMap.get(FieldType.RESPONSE_MESSAGE.name()));
		}
//		return SUCCESS;
	}

	public Map<String, String> getResponseMap() {
		return responseMap;
	}

	public void setResponseMap(Map<String, String> responseMap) {
		this.responseMap = responseMap;
	}

}
