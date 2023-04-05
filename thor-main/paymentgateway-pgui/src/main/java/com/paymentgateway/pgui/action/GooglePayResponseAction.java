package com.paymentgateway.pgui.action;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts2.interceptor.ServletRequestAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.Action;
import com.paymentgateway.commons.api.TransactionControllerServiceProvider;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.pg.core.util.UpiHistorian;

public class GooglePayResponseAction extends AbstractSecureAction implements ServletRequestAware {

	private static final long serialVersionUID = 2382298172065463916L;

	@Autowired
	TransactionControllerServiceProvider transactionControllerServiceProvider;

	@Autowired
	private UpiHistorian upiHistorian;

	private static Logger logger = LoggerFactory.getLogger(GooglePayResponseAction.class.getName());

	private HttpServletRequest httpRequest;

	@Override
	public void setServletRequest(HttpServletRequest request) {
		httpRequest = request;
	}

	@SuppressWarnings("unchecked")
	public String execute() {

		Fields responseField = null;
		try {
			Map<String, String[]> fieldMapObj = httpRequest.getParameterMap();
			Map<String, String> responseMap = new HashMap<String, String>();

			for (Entry<String, String[]> entry : fieldMapObj.entrySet()) {
				try {
					responseMap.put(entry.getKey().trim(), ((String[]) entry.getValue())[0].trim());
				} catch (ClassCastException classCastException) {
					logger.error("Exception", classCastException);
				}
			}
			Map<String, String> res = transactionControllerServiceProvider.transact(responseField,
					Constants.TXN_WS_GOOGLEPAY_PROCESSOR.getValue());
			responseField = new Fields(res);

			logger.info("Response received from WS " + responseField);

			return Action.NONE;

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return Action.NONE;
	}

}
