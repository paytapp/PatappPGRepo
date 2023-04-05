package com.paymentgateway.pgui.action;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.dao.EPOSTransactionDao;
import com.paymentgateway.commons.user.EPOSTransaction;
import com.paymentgateway.commons.util.Amount;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CrmFieldConstants;
import com.paymentgateway.commons.util.Currency;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.pgui.action.beans.InvoiceHasher;

/**
 * @author Amitosh Aanand, Sandeep Sharma
 *
 */

@Service
public class EPOSPay {

	@Autowired
	private EPOSTransactionDao eposDao;

	@Autowired
	private InvoiceHasher invoiceHasher;

	private static Logger logger = LoggerFactory.getLogger(EPOSPay.class.getName());
	private String svalue;
	private EPOSTransaction epos = new EPOSTransaction();
	Map<String, String> requestJson = new HashMap<String, String>();

	public Map<String, String> ePosRequestHandler(HttpServletRequest httpRequest) {

		try {
			epos = eposDao.findByInvoiceId(httpRequest.getParameter("svalue"));
			if (null == epos) {
//				return ERROR;
			}
			if (StringUtils.isNotBlank(epos.getPAY_ID())) {
				requestJson.put("PAY_ID", epos.getPAY_ID());
			}
			if (StringUtils.isNotBlank(epos.getAMOUNT())) {
				requestJson.put("AMOUNT", epos.getAMOUNT());
			}
			if (StringUtils.isNotBlank(epos.getCUST_NAME())) {
				requestJson.put("CUST_NAME", epos.getCUST_NAME());
			}
			if (StringUtils.isNotBlank(epos.getINVOICE_ID())) {
				requestJson.put("INVOICE_ID", epos.getINVOICE_ID());
			}
			if (StringUtils.isNotBlank(epos.getEPOS_PAYMENT_OPTION())) {
				requestJson.put("EPOS_PAYMENT_OPTION", epos.getEPOS_PAYMENT_OPTION());
			}
			if (StringUtils.isNotBlank(epos.getCREATE_DATE())) {
				requestJson.put("CREATE_DATE", epos.getCREATE_DATE());
			}
			if (StringUtils.isNotBlank(epos.getEXPIRY_DATE())) {
				requestJson.put("EXPIRY_DATE", epos.getEXPIRY_DATE());
			}
			if (StringUtils.isNotBlank(epos.getBUSINESS_NAME())) {
				requestJson.put("BUSINESS_NAME", epos.getBUSINESS_NAME());
			}
			if (StringUtils.isNotBlank(epos.getCUST_EMAIL())) {
				requestJson.put("CUST_EMAIL", epos.getCUST_EMAIL());
			}
			if (StringUtils.isNotBlank(epos.getCUST_MOBILE())) {
				requestJson.put("CUST_MOBILE", epos.getCUST_MOBILE());
			}
			if (StringUtils.isNotBlank(epos.getCURRENCY_CODE())) {
				requestJson.put("CURRENCY_CODE", epos.getCURRENCY_CODE());
			}
			if (StringUtils.isNotBlank(epos.getRETURN_URL())) {
				requestJson.put("RETURN_URL", epos.getRETURN_URL());
			}
			if (StringUtils.isNotBlank(epos.getUDF11())) {
				requestJson.put("UDF11", epos.getUDF11());
			} else {
				requestJson.put("UDF11", "");
			}
			if (StringUtils.isNotBlank(epos.getUDF12())) {
				requestJson.put("UDF12", epos.getUDF12());
			} else {
				requestJson.put("UDF12", "");
			}
			if (StringUtils.isNotBlank(epos.getUDF13())) {
				requestJson.put("UDF13", epos.getUDF13());
			} else {
				requestJson.put("UDF13", "");
			}
			if (StringUtils.isNotBlank(epos.getUDF14())) {
				requestJson.put("UDF14", epos.getUDF14());
			} else {
				requestJson.put("UDF14", "");
			}
			if (StringUtils.isNotBlank(epos.getUDF15())) {
				requestJson.put("UDF15", epos.getUDF15());
			} else {
				requestJson.put("UDF15", "");
			}
			if (StringUtils.isNotBlank(epos.getUDF16())) {
				requestJson.put("UDF16", epos.getUDF16());
			} else {
				requestJson.put("UDF16", "");
			}
			if (StringUtils.isNotBlank(epos.getUDF17())) {
				requestJson.put("UDF17", epos.getUDF17());
			} else {
				requestJson.put("UDF17", "");
			}
			if (StringUtils.isNotBlank(epos.getUDF18())) {
				requestJson.put("UDF18", epos.getUDF18());
			} else {
				requestJson.put("UDF18", "");
			}
			requestJson.put("EPOS_SALE_PAYMENT_URL", PropertiesManager.propertiesMap.get("EPOS_SALE_PAYMENT_URL"));

			requestJson.put("eposUrl", PropertiesManager.propertiesMap.get(CrmFieldConstants.INVOICE_URL.getValue())
					+ epos.getINVOICE_ID());
			requestJson.put("hash", invoiceHasher.createEposHash(epos));
			requestJson.put("totalamount", Amount.formatAmount(epos.getAMOUNT(), epos.getCURRENCY_CODE()));
			requestJson.put("currencyName", Currency.getAlphabaticCode(epos.getCURRENCY_CODE()));
			getEnablePayNow();
		} catch (Exception exception) {
			MDC.put(Constants.CRM_LOG_USER_PREFIX.getValue(), epos.getPAY_ID() + svalue);
			logger.error("Exception", exception);
//			return ERROR;
		}
		return requestJson;
	}

	private void getEnablePayNow() throws ParseException {
		try {
			DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
			String expiryDateString = epos.getEXPIRY_DATE();

			String parseDate = LocalDateTime.parse(expiryDateString, formatter1).format(formatter);
			LocalDateTime dateTime = LocalDateTime.parse(parseDate, formatter);

			LocalDateTime todayDate = LocalDateTime.now();
			String dateToday = formatter.format(todayDate);
			LocalDateTime todayTime = LocalDateTime.parse(dateToday, formatter);

			if (dateTime.isBefore(todayTime)) {
				requestJson.put("enablePay", "FALSE");
			} else {
				requestJson.put("enablePay", "TRUE");
			}
		} catch (Exception exception) {
			logger.error("Exception", exception);
		}
	}

	public String getSvalue() {
		return svalue;
	}

	public void setSvalue(String svalue) {
		this.svalue = svalue;
	}

	public EPOSTransaction getEpos() {
		return epos;
	}

	public void setEpos(EPOSTransaction epos) {
		this.epos = epos;
	}
}