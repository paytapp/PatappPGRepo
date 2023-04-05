package com.paymentgateway.pgui.action;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.api.TransactionControllerServiceProvider;
import com.paymentgateway.commons.util.Amount;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.pg.core.util.ResponseCreator;
import com.paymentgateway.pgui.action.beans.SearchTransactionActionBean;

/**
 * @author Shaiwal
 *
 */
@Service
public class UpiResponseAction {

	private static Logger logger = LoggerFactory.getLogger(UpiResponseAction.class.getName());

	@Autowired
	TransactionControllerServiceProvider transactionControllerServiceProvider;

	@Autowired
	private ResponseCreator responseCreator;

	@Autowired
	private SearchTransactionActionBean searchTransactionActionBean;

	public void upiResponseHandling(HttpServletRequest httpRequest, HttpServletResponse httpResponse)
			throws IOException {
		try {
			Map<String, String[]> fieldMapObj = httpRequest.getParameterMap();
			Map<String, String> requestMap = new HashMap<String, String>();

			for (Entry<String, String[]> entry : fieldMapObj.entrySet()) {
				try {
					requestMap.put(entry.getKey(), ((String[]) entry.getValue())[0]);

				} catch (ClassCastException classCastException) {
					logger.error("Exception", classCastException);
				}
			}

			String pgRefNum = requestMap.get(FieldType.PG_REF_NUM.getName());

			// added by Amitosh to handle scan and pay
			String oid = "";
			if (requestMap.containsKey(FieldType.OID.getName())) {
				oid = requestMap.get(FieldType.OID.getName());
			}

			String returnUrl = requestMap.get(FieldType.RETURN_URL.getName());
			Map<String, String> responseMap = new HashMap<String, String>();

			responseMap = searchTransactionActionBean.searchPayment(pgRefNum, oid);

			// Added by Amitosh for transforming amount from rupees to paisa
			String currencyCode = responseMap.get(FieldType.CURRENCY_CODE.getName());
			if (responseMap.get(FieldType.AMOUNT.getName()) != null) {
				String amount = responseMap.get(FieldType.AMOUNT.getName());
				responseMap.put(FieldType.AMOUNT.getName(), Amount.formatAmount(amount, currencyCode));
			}
			if (responseMap.get(FieldType.TOTAL_AMOUNT.getName()) != null) {
				String upTotalAmount = responseMap.get(FieldType.TOTAL_AMOUNT.getName());
				responseMap.put(FieldType.TOTAL_AMOUNT.getName(), Amount.formatAmount(upTotalAmount, currencyCode));
			}

			Fields fields = new Fields(responseMap);
			if (requestMap.containsKey(FieldType.CHECKOUT_JS_FLAG.getName())
					&& requestMap.get(FieldType.CHECKOUT_JS_FLAG.getName()).equalsIgnoreCase("true")) {
				fields.put(FieldType.CHECKOUT_JS_FLAG.getName(), "Y");
			}
			fields.put(FieldType.RETURN_URL.getName(), returnUrl);
			responseMap.put(FieldType.IS_INTERNAL_REQUEST.getName(), "N");
			responseCreator.create(fields);
			responseCreator.ResponsePost(fields, httpResponse);

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
}
