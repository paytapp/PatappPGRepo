
package com.paymentgateway.pg.core.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import com.paymentgateway.commons.dao.UserSettingDao;
import com.paymentgateway.commons.exception.ErrorType;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserSettingData;
import com.paymentgateway.commons.util.ConfigurationConstants;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;
import com.paymentgateway.commons.util.MopType;
import com.paymentgateway.commons.util.PaymentType;
import com.paymentgateway.commons.util.PropertiesManager;

/**
 * @author Sunil
 * 
 */
@Service
public class RequestCreator extends HttpServlet {

	private static final long serialVersionUID = 5403277456046500953L;

	private static Logger logger = LoggerFactory.getLogger(RequestCreator.class.getName());

	@Autowired
	private TransactionResponser transactionResponser;

	@Autowired
	private UserDao userDao;

	@Autowired
	private UserSettingDao userSettingDao;

	@Autowired
	private PropertiesManager propertiesManager;
	public static final String ALGO = "AES";
	private static String key = "9A8984DDAE18356B802EB5653066B7DF";
	private static Key keyObj = null;

	public void EnrollRequest(Fields responseMap, HttpServletResponse response) {
		try {
			/************* Enrolled card condition starts here ************/
			String acsurl = responseMap.get(FieldType.ACS_URL.getName());
			String PAReq = responseMap.get(FieldType.PAREQ.getName());
			String paymentid = responseMap.get(FieldType.PAYMENT_ID.getName());
			String termURL = propertiesManager.propertiesMap.get("Request3DSURL");

			PrintWriter out = response.getWriter();

			StringBuilder httpRequest = new StringBuilder();
			httpRequest.append("<HTML>");
			httpRequest.append("<BODY OnLoad=\"OnLoadEvent();\" >");
			httpRequest.append("<center><h1>Please do not refresh this page...</h1></center>");
			httpRequest.append("<form name=\"form1\" action=\"");
			httpRequest.append(acsurl);
			httpRequest.append("\" method=\"post\">");

			if (responseMap.get(FieldType.MOP_TYPE.getName()).equals(MopType.RUPAY.getCode())) {
				httpRequest.append("<input type=\"hidden\" name=\"PaymentID\" value=\"");
				httpRequest.append(paymentid);
				httpRequest.append("\">");
			} else {

				httpRequest.append("<input type=\"hidden\" name=\"PaReq\" value=\"");
				httpRequest.append(PAReq);
				httpRequest.append("\">");

				httpRequest.append("<input type=\"hidden\" name=\"MD\" value=\"");
				httpRequest.append(paymentid);
				httpRequest.append("\">");
				httpRequest.append("<input type=\"hidden\" name=\"TermUrl\" value=\"");
				httpRequest.append(termURL);
				httpRequest.append("\">");
			}

			httpRequest.append("</form>");
			httpRequest.append("<script language=\"JavaScript\">");
			httpRequest.append("function OnLoadEvent()");
			httpRequest.append("{document.form1.submit();}");
			httpRequest.append("</script>");
			httpRequest.append("</BODY>");
			httpRequest.append("</HTML>");

			out.write(httpRequest.toString());
			/************* Enrolled card condition Ends here ************/
		} catch (Exception exception) {
			logger.error("Exception", exception);
		}
	}

	public void FirstDataEnrollRequest(Fields responseMap, HttpServletResponse response) {
		try {
			/************* Enrolled card condition starts here ************/
			String acsurl = responseMap.get(FieldType.ACS_URL.getName());
			String PAReq = responseMap.get(FieldType.PAREQ.getName());
			// String termURL =
			// propertiesManager.getSystemProperty("Request3DSURL");
			String termURL = propertiesManager.propertiesMap.get("FirstData3DSUrl");
			String md = responseMap.get(FieldType.MD.getName());
			PrintWriter out = response.getWriter();

			StringBuilder httpRequest = new StringBuilder();
			httpRequest.append("<HTML>");
			httpRequest.append("<BODY OnLoad=\"OnLoadEvent();\" >");
			httpRequest.append("<center><h1>Please do not refresh this page...</h1></center>");
			httpRequest.append("<form name=\"form1\" action=\"");
			httpRequest.append(acsurl);
			httpRequest.append("\" method=\"post\">");

			httpRequest.append("<input type=\"hidden\" name=\"PaReq\" value=\"");
			httpRequest.append(PAReq);
			httpRequest.append("\">");

			httpRequest.append("<input type=\"hidden\" name=\"MD\" value=\"");
			httpRequest.append(md);
			httpRequest.append("\">");
			httpRequest.append("<input type=\"hidden\" name=\"TermUrl\" value=\"");
			httpRequest.append(termURL);
			httpRequest.append("\">");

			httpRequest.append("</form>");
			httpRequest.append("<script language=\"JavaScript\">");
			httpRequest.append("function OnLoadEvent()");
			httpRequest.append("{document.form1.submit();}");
			httpRequest.append("</script>");
			httpRequest.append("</BODY>");
			httpRequest.append("</HTML>");

			out.write(httpRequest.toString());
			/************* Enrolled card condition Ends here ************/
		} catch (Exception exception) {
			logger.error("Exception", exception);
		}
	}

	public void cyberSourceEnrollRequest(Fields responseMap, HttpServletResponse response) {
		try {
			/************* Enrolled card condition starts here ************/
			String acsurl = responseMap.get(FieldType.ACS_URL.getName());
			String PAReq = responseMap.get(FieldType.PAREQ.getName());
			String termURL = null;

			UserSettingData merchntSettings = userSettingDao
					.fetchDataUsingPayId(responseMap.get(FieldType.PAY_ID.getName()));

			if (merchntSettings.isAllowCustomHostedUrl()) {
				termURL = (merchntSettings.getCustomHostedUrl() + "/pgui/jsp/cyberSource3ds?pgRefNo=");
			} else {
				termURL = propertiesManager.propertiesMap.get("CyberSource3DSUrl");
			}
			termURL = termURL + responseMap.get(FieldType.PG_REF_NUM.getName());
			String md = responseMap.get(FieldType.MD.getName());
			PrintWriter out = response.getWriter();

			StringBuilder httpRequest = new StringBuilder();
			httpRequest.append("<HTML>");
			httpRequest.append("<BODY OnLoad=\"OnLoadEvent();\" >");
			httpRequest.append("<center><h1>Please do not refresh this page...</h1></center>");
			httpRequest.append("<form name=\"form1\" action=\"");
			httpRequest.append(acsurl);
			httpRequest.append("\" method=\"post\">");

			httpRequest.append("<input type=\"hidden\" name=\"PaReq\" value=\"");
			httpRequest.append(PAReq);
			httpRequest.append("\">");

			httpRequest.append("<input type=\"hidden\" name=\"MD\" value=\"");
			httpRequest.append(md);
			httpRequest.append("\">");
			httpRequest.append("<input type=\"hidden\" name=\"TermUrl\" value=\"");
			httpRequest.append(termURL);
			httpRequest.append("\">");

			httpRequest.append("</form>");
			httpRequest.append("<script language=\"JavaScript\">");
			httpRequest.append("function OnLoadEvent()");
			httpRequest.append("{document.form1.submit();}");
			httpRequest.append("</script>");
			httpRequest.append("</BODY>");
			httpRequest.append("</HTML>");

			out.write(httpRequest.toString());
			/************* Enrolled card condition Ends here ************/
		} catch (Exception exception) {
			logger.error("Exception", exception);
		}
	}

	public void lyraEnrollRequest(Fields responseMap, HttpServletResponse response) {
		try {
			/************* Enrolled card condition starts here ************/
			String acsurl = responseMap.get(FieldType.ACS_URL.getName());

			String request = responseMap.get(FieldType.LYRA_FINAL_REQUEST.getName());

			PrintWriter out = response.getWriter();

			StringBuilder httpRequest = new StringBuilder();
			httpRequest.append("<HTML>");
			httpRequest.append("<BODY OnLoad=\"OnLoadEvent();\" >");
			httpRequest.append("<center><h1>Please do not refresh this page...</h1></center>");
			httpRequest.append("<form name=\"form1\" action=\"");
			httpRequest.append(acsurl);
			httpRequest.append("\" method=\"post\">");

			JSONObject object = new JSONObject(request);
			Iterator<String> keys = object.keys();

			while (keys.hasNext()) {
				String key = keys.next();
				String value = object.getString(key);
				httpRequest.append("<input type=\"hidden\" name=");
				httpRequest.append("\"");
				httpRequest.append(key);
				httpRequest.append("\"");
				httpRequest.append(" ");
				httpRequest.append("value=\"");
				httpRequest.append(value);
				httpRequest.append("\">");
			}

			httpRequest.append("</form>");
			httpRequest.append("<script language=\"JavaScript\">");
			httpRequest.append("function OnLoadEvent()");
			httpRequest.append("{document.form1.submit();}");
			httpRequest.append("</script>");
			httpRequest.append("</BODY>");
			httpRequest.append("</HTML>");

			out.write(httpRequest.toString());
			/************* Enrolled card condition Ends here ************/
		} catch (Exception exception) {
			logger.error("Exception", exception);
		}
	}

	public void generateAkontPayRequest(Fields responseMap, HttpServletResponse response) {
		try {
			String acsurl = propertiesManager.propertiesMap.get("AKONTOPAYSaleUrl");
			String request = responseMap.get(FieldType.AKONTOPAY_FINAL_REQUEST.getName());

			PrintWriter out = response.getWriter();

			StringBuilder httpRequest = new StringBuilder();
			httpRequest.append("<HTML>");
			httpRequest.append("<BODY OnLoad=\"OnLoadEvent();\" >");
			httpRequest.append("<center><h1>Please do not refresh this page...</h1></center>");
			httpRequest.append("<form name=\"form1\" action=\"");
			httpRequest.append(acsurl);
			httpRequest.append("\" method=\"post\">");

			JSONObject object = new JSONObject(request);
			Iterator<String> keys = object.keys();

			while (keys.hasNext()) {
				String key = keys.next();
				String value = object.getString(key);
				httpRequest.append("<input type=\"hidden\" name=");
				httpRequest.append("\"");
				httpRequest.append(key);
				httpRequest.append("\"");
				httpRequest.append(" ");
				httpRequest.append("value=\"");
				httpRequest.append(value);
				httpRequest.append("\">");
			}

			httpRequest.append("</form>");
			httpRequest.append("<script language=\"JavaScript\">");
			httpRequest.append("function OnLoadEvent()");
			httpRequest.append("{document.form1.submit();}");
			httpRequest.append("</script>");
			httpRequest.append("</BODY>");
			httpRequest.append("</HTML>");

			out.write(httpRequest.toString());

		} catch (Exception exception) {
			logger.error("Exception", exception);
		}
	}

	public void patmWalletAndAndPayRedirectRequest(Fields responseMap, HttpServletResponse response) {
		try {
			/************* Enrolled card condition starts here ************/
			String acsurl = responseMap.get(FieldType.ACS_URL.getName());

			String request = responseMap.get(FieldType.LYRA_FINAL_REQUEST.getName());

			PrintWriter out = response.getWriter();

			StringBuilder httpRequest = new StringBuilder();
			httpRequest.append("<HTML>");
			httpRequest.append("<BODY OnLoad=\"OnLoadEvent();\" >");
			httpRequest.append("<center><h1>Please do not refresh this page...</h1></center>");
			httpRequest.append("<form name=\"form1\" action=\"");
			httpRequest.append(acsurl);
			httpRequest.append("\" method=\"post\">");

			JSONObject object = new JSONObject(request);
			Iterator<String> keys = object.keys();

			while (keys.hasNext()) {
				String key = keys.next();
				String value = object.getString(key);
				httpRequest.append("<input type=\"hidden\" name=");
				httpRequest.append("\"");
				httpRequest.append(key);
				httpRequest.append("\"");
				httpRequest.append(" ");
				httpRequest.append("value=\"");
				httpRequest.append(value);
				httpRequest.append("\">");
			}

			httpRequest.append("</form>");
			httpRequest.append("<script language=\"JavaScript\">");
			httpRequest.append("function OnLoadEvent()");
			httpRequest.append("{document.form1.submit();}");
			httpRequest.append("</script>");
			httpRequest.append("</BODY>");
			httpRequest.append("</HTML>");

			out.write(httpRequest.toString());
			/************* Enrolled card condition Ends here ************/
		} catch (Exception exception) {
			logger.error("Exception", exception);
		}
	}

	public void generateLyraDirectRequest(Fields responseMap, HttpServletResponse response) {
		try {

			String acsurl = propertiesManager.propertiesMap.get("LyraDirectSaleUrl");

			String request = responseMap.get(FieldType.LYRA_FINAL_REQUEST.getName());

			PrintWriter out = response.getWriter();

			StringBuilder httpRequest = new StringBuilder();
			httpRequest.append("<HTML>");
			httpRequest.append("<BODY OnLoad=\"OnLoadEvent();\" >");
			httpRequest.append("<center><h1>Please do not refresh this page...</h1></center>");
			httpRequest.append("<form name=\"form1\" action=\"");
			httpRequest.append(acsurl);
			httpRequest.append("\" method=\"post\">");

			JSONObject object = new JSONObject(request);
			Iterator<String> keys = object.keys();

			while (keys.hasNext()) {
				String key = keys.next();
				String value = object.getString(key);
				httpRequest.append("<input type=\"hidden\" name=");
				httpRequest.append("\"");
				httpRequest.append(key);
				httpRequest.append("\"");
				httpRequest.append(" ");
				httpRequest.append("value=\"");
				httpRequest.append(value);
				httpRequest.append("\">");
			}

			httpRequest.append("</form>");
			httpRequest.append("<script language=\"JavaScript\">");
			httpRequest.append("function OnLoadEvent()");
			httpRequest.append("{document.form1.submit();}");
			httpRequest.append("</script>");
			httpRequest.append("</BODY>");
			httpRequest.append("</HTML>");

			out.write(httpRequest.toString());

		} catch (Exception exception) {
			logger.error("Exception", exception);
		}
	}

	public void billDeskEnrollRequest(Fields responseMap, HttpServletResponse response) {
		try {
			/************* Enrolled card condition starts here ************/
			String acsurl = responseMap.get(FieldType.ACS_URL.getName());

			String request = responseMap.get(FieldType.BILLDESK_FINAL_REQUEST.getName());

			PrintWriter out = response.getWriter();

			StringBuilder httpRequest = new StringBuilder();
			httpRequest.append("<HTML>");
			httpRequest.append("<BODY OnLoad=\"OnLoadEvent();\" >");
			httpRequest.append("<center><h1>Please do not refresh this page...</h1></center>");
			httpRequest.append("<form name=\"form1\" action=\"");
			httpRequest.append(acsurl);
			httpRequest.append("\" method=\"post\">");

			JSONObject object = new JSONObject(request);
			Iterator<String> keys = object.keys();

			while (keys.hasNext()) {
				String key = keys.next();
				String value = object.getString(key);
				httpRequest.append("<input type=\"hidden\" name=");
				httpRequest.append("\"");
				httpRequest.append(key);
				httpRequest.append("\"");
				httpRequest.append(" ");
				httpRequest.append("value=\"");
				httpRequest.append(value);
				httpRequest.append("\">");
			}

			httpRequest.append("</form>");
			httpRequest.append("<script language=\"JavaScript\">");
			httpRequest.append("function OnLoadEvent()");
			httpRequest.append("{document.form1.submit();}");
			httpRequest.append("</script>");
			httpRequest.append("</BODY>");
			httpRequest.append("</HTML>");

			out.write(httpRequest.toString());
			/************* Enrolled card condition Ends here ************/
		} catch (Exception exception) {
			logger.error("Exception", exception);
		}
	}

	public void iciciMpgsEnrollRequest(Fields responseMap, HttpServletResponse response) {
		try {
			/************* Enrolled card condition starts here ************/
			String acsurl = responseMap.get(FieldType.ACS_URL.getName());
			String PAReq = responseMap.get(FieldType.PAREQ.getName());
			// String termURL =
			// propertiesManager.getSystemProperty("Request3DSURL");
			String termURL = propertiesManager.propertiesMap.get("IciciMpgsReturnUrl");
			String md = responseMap.get(FieldType.MD.getName());
			PrintWriter out = response.getWriter();

			StringBuilder httpRequest = new StringBuilder();
			httpRequest.append("<HTML>");
			httpRequest.append("<BODY OnLoad=\"OnLoadEvent();\" >");
			httpRequest.append("<center><h1>Please do not refresh this page...</h1></center>");
			httpRequest.append("<form name=\"form1\" action=\"");
			httpRequest.append(acsurl);
			httpRequest.append("\" method=\"post\">");

			httpRequest.append("<input type=\"hidden\" name=\"PaReq\" value=\"");
			httpRequest.append(PAReq);
			httpRequest.append("\">");

			httpRequest.append("<input type=\"hidden\" name=\"MD\" value=\"");
			httpRequest.append(md);
			httpRequest.append("\">");
			httpRequest.append("<input type=\"hidden\" name=\"TermUrl\" value=\"");
			httpRequest.append(termURL);
			httpRequest.append("\">");

			httpRequest.append("</form>");
			httpRequest.append("<script language=\"JavaScript\">");
			httpRequest.append("function OnLoadEvent()");
			httpRequest.append("{document.form1.submit();}");
			httpRequest.append("</script>");
			httpRequest.append("</BODY>");
			httpRequest.append("</HTML>");

			out.write(httpRequest.toString());
			/************* Enrolled card condition Ends here ************/
		} catch (Exception exception) {
			logger.error("Exception", exception);
		}
	}

	public void InvalidRequest(Fields fields, HttpServletResponse response) {
		try {
			/************* Invalid Request ************/

			PrintWriter out = response.getWriter();

			// TO remove internal custom MDC
			fields.removeInternalFields();
			transactionResponser.removeInvalidResponseFields(fields);

			StringBuilder httpRequest = new StringBuilder();
			httpRequest.append("<HTML>");
			httpRequest.append("<BODY OnLoad=\"OnLoadEvent();\" >");
			httpRequest.append("<center><h1>Please do not refresh this page...</h1></center>");
			httpRequest.append("<form name=\"form1\" action=\"");
			httpRequest.append(fields.get(FieldType.RETURN_URL.getName()));
			httpRequest.append("\" method=\"post\">");
			for (String key : fields.keySet()) {
				httpRequest.append("<input type=\"hidden\" name=\"");
				httpRequest.append(key);
				httpRequest.append("\" value=\"");
				httpRequest.append(fields.get(key));
				httpRequest.append("\">");
			}
			httpRequest.append("</form>");
			httpRequest.append("<script language=\"JavaScript\">");
			httpRequest.append("function OnLoadEvent()");
			httpRequest.append("{document.form1.submit();}");
			httpRequest.append("</script>");
			httpRequest.append("</BODY>");
			httpRequest.append("</HTML>");

			out.write(httpRequest.toString());
			/************* Invalid Request ************/
		} catch (Exception exception) {
			logger.error("Exception", exception);
		}
	}

	// public void WebsitePackageRequest(String PAY_ID,String ORDER_ID,String
	// AMOUNT,String TXNTYPE,String CUST_NAME,String CUST_EMAIL,String
	// PRODUCT_DESC,String CURRENCY_CODE,String RETURN_URL,String HASH) {
	public void WebsitePackageRequest(Fields fields, HttpServletResponse response) {
		try {
			String requestURL = propertiesManager.propertiesMap.get("RequestURL");
			PrintWriter out = response.getWriter();
			StringBuilder httpRequest = new StringBuilder();
			httpRequest.append("<HTML>");
			httpRequest.append("<BODY OnLoad=\"OnLoadEvent();\" >");
			httpRequest.append("<center><h1>Please do not refresh this page...</h1></center>");
			httpRequest.append("<form name=\"form1\" action=\"");
			httpRequest.append(requestURL);
			httpRequest.append("\" method=\"post\">");
			for (String key : fields.keySet()) {
				httpRequest.append("<input type=\"hidden\" name=\"");
				httpRequest.append(key);
				httpRequest.append("\" value=\"");
				httpRequest.append(fields.get(key));
				httpRequest.append("\">");
			}

			httpRequest.append("</form>");
			httpRequest.append("<script language=\"JavaScript\">");
			httpRequest.append("function OnLoadEvent()");
			httpRequest.append("{document.form1.submit();}");
			httpRequest.append("</script>");
			httpRequest.append("</BODY>");
			httpRequest.append("</HTML>");

			out.write(httpRequest.toString());
		} catch (Exception exception) {
			logger.error("Exception", exception);
		}
	}

	public void generatePaymentGatewayRequest(Fields fields, HttpServletResponse response) {
		String finalRequest = fields.get(FieldType.PAYMENT_GATEWAY_FINAL_REQUEST.getName());
		try {
			PrintWriter out = response.getWriter();

			StringBuilder httpRequest = new StringBuilder();
			httpRequest.append("<HTML>");
			httpRequest.append("<BODY OnLoad=\"OnLoadEvent();\" >");
			httpRequest.append("<center><h1>Please do not refresh this page...</h1></center>");
			httpRequest.append("<script language=\"JavaScript\">");
			httpRequest.append("function OnLoadEvent() { ");
			httpRequest.append("window.location.assign('");
			httpRequest.append(finalRequest);
			httpRequest.append("') }");
			httpRequest.append("</script>");
			httpRequest.append("</BODY>");
			httpRequest.append("</HTML>");

			out.write(httpRequest.toString());

			logger.info("Final request sent " + httpRequest);

		} catch (IOException exception) {
			logger.error("Exception", exception);
		}
	}

	public String generateFederalRequest(Fields fields, HttpServletResponse response) throws SystemException {

		String finalRequest = fields.get(FieldType.FEDERAL_ENROLL_FINAL_REQUEST.getName());
		PrintWriter out;
		try {
			out = response.getWriter();

			out.write(finalRequest);

		} catch (IOException iOException) {
			logger.error("Exception : ", iOException);
			throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR, iOException,
					"Network Exception with Federal for auth");
		}
		return ErrorType.SUCCESS.getResponseMessage();
	}

	public String generateBobRequest(Fields fields, HttpServletResponse response) {
		StringBuilder httpRequest = new StringBuilder();
		try {
			PrintWriter out = response.getWriter();
			String finalRequest = fields.get(FieldType.BOB_FINAL_REQUEST.getName());

			httpRequest.append("<HTML>");
			httpRequest.append("<BODY OnLoad=\"OnLoadEvent();\" >");
			httpRequest.append("<center><h1>Please do not refresh this page...</h1></center>");
			httpRequest.append("<script language=\"JavaScript\">");
			httpRequest.append("function OnLoadEvent()");
			httpRequest.append("{window.location= '" + finalRequest + "';}");
			httpRequest.append("</script>");
			httpRequest.append("</BODY>");
			httpRequest.append("</HTML>");
			out.write(httpRequest.toString());
		} catch (Exception exception) {
			logger.error("Exception", exception);
		}
		return httpRequest.toString();
	}

	public String generateSbiCardRequest(Fields fields, HttpServletResponse response) {
		StringBuilder httpRequest = new StringBuilder();
		try {
			PrintWriter out = response.getWriter();
			String finalRequest = fields.get(FieldType.SBI_FINAL_REQUEST.getName());

			httpRequest.append("<HTML>");
			httpRequest.append("<BODY OnLoad=\"OnLoadEvent();\" >");
			httpRequest.append("<center><h1>Please do not refresh this page...</h1></center>");
			httpRequest.append("<script language=\"JavaScript\">");
			httpRequest.append("function OnLoadEvent()");
			httpRequest.append("{window.location= '" + finalRequest + "';}");
			httpRequest.append("</script>");
			httpRequest.append("</BODY>");
			httpRequest.append("</HTML>");
			out.write(httpRequest.toString());
		} catch (Exception exception) {
			logger.error("Exception", exception);
		}
		return httpRequest.toString();
	}

	public void generateSafexpayRequest(Fields fields, HttpServletResponse response) {
		try {

			String paymentString = fields.get(FieldType.SAFEXPAY_FINAL_REQUEST.getName());
			PrintWriter out = response.getWriter();
			out.write(paymentString);

		} catch (Exception exception) {
			logger.error("Exception", exception);
		}
	}

	public String generatePayphiRequest(Fields fields, HttpServletResponse response) {
		StringBuilder httpRequest = new StringBuilder();
		String saleUrl = propertiesManager.propertiesMap.get("PAYPHISaleUrl");
		try {
			PrintWriter out = response.getWriter();

			String finalRequest = fields.get(FieldType.PAYPHI_FINAL_REQUEST.getName());
			String finalRequestSplit[] = finalRequest.split("&");

			httpRequest.append("<HTML>");
			httpRequest.append("<BODY OnLoad=\"OnLoadEvent();\" >");
			httpRequest.append("<center><h1>Please do not refresh this page...</h1></center>");
			httpRequest.append("<form name=\"form1\" action=\"");
			httpRequest.append(saleUrl);
			httpRequest.append("\" method=\"post\">");

			for (String entry : finalRequestSplit) {

				String entrySplit[] = entry.split("=");

				httpRequest.append("<input type=\"hidden\" name=\"");
				httpRequest.append(entrySplit[0]);
				httpRequest.append("\" value=\"");
				httpRequest.append(entrySplit[1]);
				httpRequest.append("\">");
			}

			httpRequest.append("</form>");
			httpRequest.append("<script language=\"JavaScript\">");
			httpRequest.append("function OnLoadEvent()");
			httpRequest.append("{document.form1.submit();}");
			httpRequest.append("</script>");
			httpRequest.append("</BODY>");
			httpRequest.append("</HTML>");
			out.write(httpRequest.toString());
		} catch (Exception exception) {
			logger.error("Exception", exception);
		}
		return httpRequest.toString();
	}

	public void generateApexPayRequest(Fields fields, HttpServletResponse response) {
		try {
			String requestURL = fields.get(FieldType.ADF3.getName());
			
			if (StringUtils.isBlank(requestURL)) {
				logger.info("Apexpay Request URL is null for merchant with MERCHANT_ID {}" , fields.get(FieldType.MERCHANT_ID.getName()));
				return;
			}
			PrintWriter out = response.getWriter();
			StringBuilder httpRequest = new StringBuilder();
			httpRequest.append("<HTML>");
			httpRequest.append("<BODY OnLoad=\"OnLoadEvent();\" >");
			httpRequest.append("<form name=\"form1\" action=\"");
			httpRequest.append(requestURL);
			httpRequest.append("\" method=\"post\">");
			httpRequest.append("<input type=\"hidden\" name=\"");
			httpRequest.append("ENCDATA");
			httpRequest.append("\" value=\"");
			httpRequest.append(fields.get(FieldType.APEXPAY_FINAL_REQUEST.getName()));
			httpRequest.append("\">");

			httpRequest.append("<input type=\"hidden\" name=\"");
			httpRequest.append("PAY_ID");
			httpRequest.append("\" value=\"");
			httpRequest.append(fields.get(FieldType.MERCHANT_ID.getName()));
			httpRequest.append("\">");
			httpRequest.append("</form>");
			httpRequest.append("<script language=\"JavaScript\">");
			httpRequest.append("function OnLoadEvent()");
			httpRequest.append("{document.form1.submit();}");
			httpRequest.append("</script>");
			httpRequest.append("</BODY>");
			httpRequest.append("</HTML>");
			out.write(httpRequest.toString());
		} catch (Exception exception) {
			logger.error("Exception", exception);
		}
	}

	public String generateHDfcRequest(Fields fields, HttpServletResponse response) {
		StringBuilder httpRequest = new StringBuilder();
		try {
			PrintWriter out = response.getWriter();
			String finalRequest = fields.get(FieldType.HDFC_FINAL_REQUEST.getName());

			httpRequest.append("<HTML>");
			httpRequest.append("<BODY OnLoad=\"OnLoadEvent();\" >");
			httpRequest.append("<center><h1>Please do not refresh this page...</h1></center>");
			httpRequest.append("<script language=\"JavaScript\">");
			httpRequest.append("function OnLoadEvent()");
			httpRequest.append("{window.location= '" + finalRequest + "';}");
			httpRequest.append("</script>");
			httpRequest.append("</BODY>");
			httpRequest.append("</HTML>");
			out.write(httpRequest.toString());
		} catch (Exception exception) {
			logger.error("Exception", exception);
		}
		return httpRequest.toString();
	}

	public void generateKotakRequest(Fields fields, HttpServletResponse response) {
		try {
			String requestURL = PropertiesManager.propertiesMap.get("KotakSaleUrl");
			PrintWriter out = response.getWriter();
			StringBuilder httpRequest = new StringBuilder();
			httpRequest.append("<HTML>");
			httpRequest.append("<BODY OnLoad=\"OnLoadEvent();\" >");
			httpRequest.append("<center><h1>Please do not refresh this page...</h1></center>");
			httpRequest.append("<form name=\"form1\" action=\"");
			httpRequest.append(requestURL);
			httpRequest.append("\" method=\"post\">");

			httpRequest.append("<input type=\"hidden\" name=\"");
			httpRequest.append("EncData");
			httpRequest.append("\" value=\"");
			httpRequest.append(fields.get(FieldType.KOTAK_FINAL_REQUEST.getName()));
			httpRequest.append("\">");
			httpRequest.append("<input type=\"hidden\" name=\"");
			httpRequest.append("MerchantId");
			httpRequest.append("\" value=\"");
			httpRequest.append(fields.get(FieldType.MERCHANT_ID.getName()));
			httpRequest.append("\">");

			httpRequest.append("</form>");
			httpRequest.append("<script language=\"JavaScript\">");
			httpRequest.append("function OnLoadEvent()");
			httpRequest.append("{document.form1.submit();}");
			httpRequest.append("</script>");
			httpRequest.append("</BODY>");
			httpRequest.append("</HTML>");

			out.write(httpRequest.toString());
		} catch (Exception exception) {
			logger.error("Exception", exception);
		}
	}

	public void generateIdbiRequest(Fields fields, HttpServletResponse response) {
		try {
			String requestURL = PropertiesManager.propertiesMap.get("IdbiSaleUrl");
			PrintWriter out = response.getWriter();
			StringBuilder httpRequest = new StringBuilder();
			httpRequest.append("<HTML>");
			httpRequest.append("<BODY OnLoad=\"OnLoadEvent();\" >");
			httpRequest.append("<center><h1>Please do not refresh this page...</h1></center>");
			httpRequest.append("<form name=\"form1\" action=\"");
			httpRequest.append(requestURL);
			httpRequest.append("\" method=\"post\">");

			httpRequest.append("<input type=\"hidden\" name=\"");
			httpRequest.append("merchantRequest");
			httpRequest.append("\" value=\"");
			httpRequest.append(fields.get(FieldType.IDBI_FINAL_REQUEST.getName()));
			httpRequest.append("\">");
			httpRequest.append("<input type=\"hidden\" name=\"");
			httpRequest.append("MID");
			httpRequest.append("\" value=\"");
			httpRequest.append(fields.get(FieldType.MERCHANT_ID.getName()));
			httpRequest.append("\">");
			httpRequest.append("</form>");
			httpRequest.append("<script language=\"JavaScript\">");
			httpRequest.append("function OnLoadEvent()");
			httpRequest.append("{document.form1.submit();}");
			httpRequest.append("</script>");
			httpRequest.append("</BODY>");
			httpRequest.append("</HTML>");
			out.write(httpRequest.toString());
		} catch (Exception exception) {
			logger.error("Exception", exception);
		}
	}

	public String generateFssPayRequest(Fields fields, HttpServletResponse response) {
		StringBuilder httpRequest = new StringBuilder();
		try {
			PrintWriter out = response.getWriter();
			String finalRequest = fields.get(FieldType.FSS_PAY_FINAL_REQUEST.getName());

			httpRequest.append("<HTML>");
			httpRequest.append("<BODY OnLoad=\"OnLoadEvent();\" >");
			httpRequest.append("<center><h1>Please do not refresh this page...</h1></center>");
			httpRequest.append("<script language=\"JavaScript\">");
			httpRequest.append("function OnLoadEvent()");
			httpRequest.append("{window.location= '" + finalRequest + "';}");
			httpRequest.append("</script>");
			httpRequest.append("</BODY>");
			httpRequest.append("</HTML>");
			out.write(httpRequest.toString());
		} catch (Exception exception) {
			logger.error("Exception", exception);
		}
		return httpRequest.toString();
	}

	public void generateIdfcNetBankingRequest(Fields fields, HttpServletResponse response) {
		StringBuilder httpRequest = new StringBuilder();
		try {
			String requestURL = PropertiesManager.propertiesMap.get("idfcNetBankingSaleUrl");
			PrintWriter out = response.getWriter();
			httpRequest.append("<HTML>");
			httpRequest.append("<BODY OnLoad=\"OnLoadEvent();\" >");
			httpRequest.append("<center><h1>Please do not refresh this page...</h1></center>");
			httpRequest.append("<form name=\"form1\" action=\"");
			httpRequest.append(requestURL);
			httpRequest.append("\" method=\"post\">");
			httpRequest.append("<input type=\"hidden\" name=\"");
			httpRequest.append("merchantRequest");
			httpRequest.append("\" value=\"");
			httpRequest.append(fields.get(FieldType.IDFC_NETBANKING_FINAL_REQUEST.getName()));
			httpRequest.append("\">");
			httpRequest.append("</form>");
			httpRequest.append("<script language=\"JavaScript\">");
			httpRequest.append("function OnLoadEvent()");
			httpRequest.append("{document.form1.submit();}");
			httpRequest.append("</script>");
			httpRequest.append("</BODY>");
			httpRequest.append("</HTML>");
			out.write(httpRequest.toString());
		} catch (Exception exception) {
			logger.error("Exception", exception);
		}
	}

	public void generateBillDeskRequest(Fields fields, HttpServletResponse response) {
		try {
			String requestURL = PropertiesManager.propertiesMap.get("billDeskSaleUrl");
			PrintWriter out = response.getWriter();
			StringBuilder httpRequest = new StringBuilder();
			httpRequest.append("<HTML>");
			httpRequest.append("<BODY OnLoad=\"OnLoadEvent();\" >");
			httpRequest.append("<center><h1>Please do not refresh this page...</h1></center>");
			httpRequest.append("<form name=\"form1\" action=\"");
			httpRequest.append(requestURL);
			httpRequest.append("\" method=\"post\">");

			httpRequest.append("<input type=\"hidden\" name=\"");
			httpRequest.append("msg");
			httpRequest.append("\" value=\"");
			httpRequest.append(fields.get(FieldType.BILLDESK_FINAL_REQUEST.getName()));
			httpRequest.append("\">");
			/*
			 * httpRequest.append("<input type=\"hidden\" name=\"");
			 * httpRequest.append("MID"); httpRequest.append("\" value=\"");
			 * httpRequest.append(fields.get(FieldType.MERCHANT_ID.getName()));
			 * httpRequest.append("\">");
			 */
			httpRequest.append("</form>");
			httpRequest.append("<script language=\"JavaScript\">");
			httpRequest.append("function OnLoadEvent()");
			httpRequest.append("{document.form1.submit();}");
			httpRequest.append("</script>");
			httpRequest.append("</BODY>");
			httpRequest.append("</HTML>");
			out.write(httpRequest.toString());
		} catch (Exception exception) {
			logger.error("Exception", exception);
		}
	}

	public void generateSbiRequest(Fields fields, HttpServletResponse response) {
		try {
			String requestURL = PropertiesManager.propertiesMap.get("SbiSaleUrl");
			PrintWriter out = response.getWriter();
			StringBuilder httpRequest = new StringBuilder();
			httpRequest.append("<HTML>");
			httpRequest.append("<BODY OnLoad=\"OnLoadEvent();\" >");
			httpRequest.append("<center><h1>Please do not refresh this page...</h1></center>");
			httpRequest.append("<form name=\"form1\" action=\"");
			httpRequest.append(requestURL);
			httpRequest.append("\" method=\"post\">");

			httpRequest.append("<input type=\"hidden\" name=\"");
			httpRequest.append("encdata");
			httpRequest.append("\" value=\"");
			httpRequest.append(fields.get(FieldType.SBI_FINAL_REQUEST.getName()));
			httpRequest.append("\">");

			httpRequest.append("<input type=\"hidden\" name=\"");
			httpRequest.append("merchant_code");
			httpRequest.append("\" value=\"");
			httpRequest.append(fields.get(FieldType.ADF5.getName()));
			httpRequest.append("\">");

			httpRequest.append("</form>");
			httpRequest.append("<script language=\"JavaScript\">");
			httpRequest.append("function OnLoadEvent()");
			httpRequest.append("{document.form1.submit();}");
			httpRequest.append("</script>");
			httpRequest.append("</BODY>");
			httpRequest.append("</HTML>");
			logger.info("final form submit to SBI " + fields.get(FieldType.PG_REF_NUM.getName()) + ":"
					+ httpRequest.toString());

			out.write(httpRequest.toString());
		} catch (Exception exception) {
			logger.error("Exception", exception);
		}
	}

	public void generatePayuRequest(Fields fields, HttpServletResponse response) {
		try {
			String paymentString = null;
			if (fields.get(FieldType.PAYMENT_TYPE.getName()).equalsIgnoreCase(PaymentType.CREDIT_CARD.getCode())
					|| fields.get(FieldType.PAYMENT_TYPE.getName())
							.equalsIgnoreCase(PaymentType.DEBIT_CARD.getCode())) {
				RequestCreator rc = new RequestCreator();

				rc.Crypto(key);
				paymentString = decrypt(fields.get(FieldType.PAYU_FINAL_REQUEST.getName()));
			} else {
				paymentString = fields.get(FieldType.PAYU_FINAL_REQUEST.getName());
			}
			PrintWriter out = response.getWriter();
			out.write(paymentString);
		} catch (Exception exception) {
			logger.error("Exception while posting PAYU payment request ", exception);
		}
	}

	public void Crypto(String key) {
		this.key = key;
		this.keyObj = new SecretKeySpec(key.getBytes(), ALGO);
		System.out.println("tst");
	}

	public static String decrypt(String data) {
		try {
			IvParameterSpec iv = new IvParameterSpec(key.substring(0, 16).getBytes("UTF-8"));
			Cipher cipher = Cipher.getInstance(ALGO + "/CBC/PKCS5PADDING");
			cipher.init(Cipher.DECRYPT_MODE, keyObj, iv);
			byte[] decodedData = Base64.getDecoder().decode(data);
			byte[] decValue = cipher.doFinal(decodedData);

			return new String(decValue);

		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IOException
				| IllegalBlockSizeException | BadPaddingException
				| InvalidAlgorithmParameterException scramblerExceptionException) {
			scramblerExceptionException.printStackTrace();

		}
		return "";
	}

	public void generatePaytmRequest(Fields fields, HttpServletResponse response) {
		try {
			/************* Condition starts here ************/
			String acsurl = fields.get(FieldType.ACS_URL.getName());

			String request = fields.get(FieldType.PAYTM_FINAL_REQUEST.getName());

			PrintWriter out = response.getWriter();

			StringBuilder httpRequest = new StringBuilder();
			httpRequest.append("<HTML>");
			httpRequest.append("<BODY OnLoad=\"OnLoadEvent();\" >");
			httpRequest.append("<center><h1>Please do not refresh this page...</h1></center>");
			httpRequest.append("<form name=\"form1\" action=\"");
			httpRequest.append(acsurl);
			httpRequest.append("\" method=\"post\">");

			JSONObject object = new JSONObject(request);
			Iterator<String> keys = object.keys();

			while (keys.hasNext()) {
				String key = keys.next();
				String value = object.getString(key);
				httpRequest.append("<input type=\"hidden\" name=");
				httpRequest.append("\"");
				httpRequest.append(key);
				httpRequest.append("\"");
				httpRequest.append(" ");
				httpRequest.append("value=\"");
				httpRequest.append(value);
				httpRequest.append("\">");
			}

			httpRequest.append("</form>");
			httpRequest.append("<script language=\"JavaScript\">");
			httpRequest.append("function OnLoadEvent()");
			httpRequest.append("{document.form1.submit();}");
			httpRequest.append("</script>");
			httpRequest.append("</BODY>");
			httpRequest.append("</HTML>");

			out.write(httpRequest.toString());
			/************* Condition Ends here ************/
		} catch (Exception exception) {
			logger.error("Exception", exception);
		}
	}

	public void generateIsgpayRequest(Fields fields, HttpServletResponse response) {
		try {
			String requestURL = PropertiesManager.propertiesMap.get("ISGPAYSaleUrl");
			String rupayFlag = PropertiesManager.propertiesMap.get("selectMidForRupay");
			String merchantId = null;
			String terminalId = null;

			if (fields.get(FieldType.MOP_TYPE.getName()).equalsIgnoreCase(MopType.RUPAY.getCode())
					&& StringUtils.isNotBlank(rupayFlag) && rupayFlag.equalsIgnoreCase("Y")) {
				merchantId = fields.get(FieldType.ADF6.getName());
				terminalId = fields.get(FieldType.ADF9.getName());
			} else {
				merchantId = fields.get(FieldType.MERCHANT_ID.getName());
				terminalId = fields.get(FieldType.TERMINAL_ID.getName());
			}

			PrintWriter out = response.getWriter();
			StringBuilder httpRequest = new StringBuilder();
			httpRequest.append("<HTML>");
			httpRequest.append("<BODY OnLoad=\"OnLoadEvent();\" >");
			httpRequest.append("<center><h1>Please do not refresh this page...</h1></center>");
			httpRequest.append("<form name=\"form1\" action=\"");
			httpRequest.append(requestURL);
			httpRequest.append("\" method=\"post\">");

			httpRequest.append("<input type=\"hidden\" name=\"");
			httpRequest.append("MerchantId");
			httpRequest.append("\" value=\"");
			httpRequest.append(merchantId);
			httpRequest.append("\">");
			httpRequest.append("<input type=\"hidden\" name=\"");
			httpRequest.append("TerminalId");
			httpRequest.append("\" value=\"");
			httpRequest.append(terminalId);
			httpRequest.append("\">");
			httpRequest.append("<input type=\"hidden\" name=\"");
			httpRequest.append("BankId");
			httpRequest.append("\" value=\"");
			httpRequest.append(fields.get(FieldType.BANK_ID.getName()));
			httpRequest.append("\">");
			httpRequest.append("<input type=\"hidden\" name=\"");
			httpRequest.append("Version");
			httpRequest.append("\" value=\"");
			httpRequest.append("1");
			httpRequest.append("\">");
			httpRequest.append("<input type=\"hidden\" name=\"");
			httpRequest.append("EncData");
			httpRequest.append("\" value=\"");
			httpRequest.append(fields.get(FieldType.ISGPAY_FINAL_REQUEST.getName()));
			httpRequest.append("\">");
			httpRequest.append("</form>");
			httpRequest.append("<script language=\"JavaScript\">");
			httpRequest.append("function OnLoadEvent()");
			httpRequest.append("{document.form1.submit();}");
			httpRequest.append("</script>");
			httpRequest.append("</BODY>");
			httpRequest.append("</HTML>");
			out.write(httpRequest.toString());
		} catch (Exception exception) {
			logger.error("Exception", exception);
		}
	}

	public void createDccRequestPage(Map<String, String> request, HttpServletResponse response) {
		try {

			PrintWriter out = response.getWriter();
			StringBuilder httpRequest = new StringBuilder();
			httpRequest.append("<HTML>");
			httpRequest.append("<BODY OnLoad=\"OnLoadEvent();\" >");
			httpRequest.append("<center><h1>Please do not refresh this page...</h1></center>");
			httpRequest.append("<form name=\"form1\" action=\"");
			httpRequest.append("https://test.ipg-online.com/connect/gateway/processing");
			httpRequest.append("\" method=\"post\">");

			httpRequest.append("<input type=\"hidden\" name=\"");
			httpRequest.append("paymentMethod");
			httpRequest.append("\" value=\"");
			httpRequest.append("M");
			httpRequest.append("\">");
			httpRequest.append("<input type=\"hidden\" name=\"");
			httpRequest.append("txntype");
			httpRequest.append("\" value=\"");
			httpRequest.append(request.get("Transaction Type"));
			httpRequest.append("\">");
			httpRequest.append("<input type=\"hidden\" name=\"");
			httpRequest.append("currency");
			httpRequest.append("\" value=\"");
			httpRequest.append(request.get("Currency"));
			httpRequest.append("\">");
			httpRequest.append("<input type=\"hidden\" name=\"");
			httpRequest.append("storename");
			httpRequest.append("\" value=\"");
			httpRequest.append("3344000689");
			httpRequest.append("\">");
			httpRequest.append("<input type=\"hidden\" name=\"");
			httpRequest.append("oid");
			httpRequest.append("\" value=\"");
			httpRequest.append(request.get("Order Id"));
			httpRequest.append("\">");
			httpRequest.append("<input type=\"hidden\" name=\"");
			httpRequest.append("mode");
			httpRequest.append("\" value=\"");
			httpRequest.append("payonly");
			httpRequest.append("\">");
			httpRequest.append("<input type=\"hidden\" name=\"");
			httpRequest.append("chargetotal");
			httpRequest.append("\" value=\"");
			httpRequest.append(request.get("Chargetotal"));
			httpRequest.append("\">");
			httpRequest.append("<input type=\"hidden\" name=\"");
			httpRequest.append("timezone");
			httpRequest.append("\" value=\"");
			httpRequest.append("Asia/Calcutta");
			httpRequest.append("\">");
			httpRequest.append("<input type=\"hidden\" name=\"");
			httpRequest.append("txndatetime");
			httpRequest.append("\" value=\"");
			httpRequest.append(request.get("Date Time"));
			httpRequest.append("\">");
			httpRequest.append("<input type=\"hidden\" name=\"");
			httpRequest.append("responseFailURL");
			httpRequest.append("\" value=\"");
			httpRequest.append(request.get("Response URL"));
			httpRequest.append("\">");
			httpRequest.append("<input type=\"hidden\" name=\"");
			httpRequest.append("responseSuccessURL");
			httpRequest.append("\" value=\"");
			httpRequest.append(request.get("Response URL"));
			httpRequest.append("\">");
			httpRequest.append("<input type=\"hidden\" name=\"");
			httpRequest.append("sharedsecret");
			httpRequest.append("\" value=\"");
			httpRequest.append("hgH!k23#Lt");
			httpRequest.append("\">");
			httpRequest.append("<input type=\"hidden\" name=\"");
			httpRequest.append("hash_algorithm");
			httpRequest.append("\" value=\"");
			httpRequest.append("SHA256");
			httpRequest.append("\">");
			httpRequest.append("<input type=\"hidden\" name=\"");
			httpRequest.append("hash");
			httpRequest.append("\" value=\"");
			httpRequest.append(request.get("Calculated Hash"));
			httpRequest.append("\">");

			httpRequest.append("</form>");
			httpRequest.append("<script language=\"JavaScript\">");
			httpRequest.append("function OnLoadEvent()");
			httpRequest.append("{document.form1.submit();}");
			httpRequest.append("</script>");
			httpRequest.append("</BODY>");
			httpRequest.append("</HTML>");
			out.write(httpRequest.toString());
		} catch (Exception exception) {
			logger.error("Exception", exception);
		}
	}

	public String sendMigsEnrollTransaction(Fields fields, HttpServletResponse response) throws SystemException {

		String url = ConfigurationConstants.AXIS_MIGS_TRANSACTION_URL.getValue();
		String finalRequest = fields.get(FieldType.MIGS_FINAL_REQUEST.getName());
		PrintWriter out;
		try {
			out = response.getWriter();

			out.write(finalRequest);

		} catch (IOException iOException) {
			logger.error("Exception : ", iOException);
			throw new SystemException(ErrorType.INTERNAL_SYSTEM_ERROR, iOException,
					"Network Exception with Amex for auth" + url.toString());
		}
		return ErrorType.SUCCESS.getResponseMessage();
	}

	public void generateEnrollIdbiRequest(Fields fields, HttpServletResponse response) {
		try {

			String returnUrl = PropertiesManager.propertiesMap.get("IdbiReturnUrl");
			PrintWriter out = response.getWriter();
			StringBuilder httpRequest = new StringBuilder();
			String AcsReq = fields.get(FieldType.ACS_REQ_MAP.getName());
			String value = HtmlUtils.htmlUnescape(AcsReq);
			JSONObject jsonData = new JSONObject(value);
			Map<String, String> requestMap = new HashMap<String, String>();
			for (Object key : jsonData.keySet()) {
				requestMap.put(key.toString(), jsonData.getString(key.toString()));
			}
			httpRequest.append("<HTML>");
			httpRequest.append("<BODY OnLoad=\"OnLoadEvent();\" >");
			httpRequest.append("<center><h1>Please do not refresh this page...</h1></center>");
			httpRequest.append("<form name=\"form1\" action=\"");
			httpRequest.append(fields.get(FieldType.ACS_URL.getName()));
			httpRequest.append("\" method=\"post\">");

			for (Entry<String, String> entry : requestMap.entrySet()) {
				httpRequest.append("<input type=\"hidden\" name=\"");
				httpRequest.append(entry.getKey());
				httpRequest.append("\" value=\"");
				httpRequest.append(entry.getValue());
				httpRequest.append("\">");
			}
			httpRequest.append("<input type=\"hidden\" name=\"");
			httpRequest.append(fields.get(FieldType.ACS_RETURN_URL.getName()));
			httpRequest.append("\" value=\"");
			httpRequest.append(returnUrl);
			httpRequest.append("\">");

			httpRequest.append("</form>");
			httpRequest.append("<script language=\"JavaScript\">");
			httpRequest.append("function OnLoadEvent()");
			httpRequest.append("{document.form1.submit();}");
			httpRequest.append("</script>");
			httpRequest.append("</BODY>");
			httpRequest.append("</HTML>");
			out.write(httpRequest.toString());
		} catch (Exception exception) {
			logger.error("Exception", exception);
		}
	}

	public void generateCommonNBRequest(Fields fields, HttpServletResponse response) {
		String finalRequest = fields.get(FieldType.PAYMENT_GATEWAY_FINAL_REQUEST.getName());
		try {
			PrintWriter out = response.getWriter();
			StringBuilder httpRequest = new StringBuilder();
			httpRequest.append("<HTML>");
			httpRequest.append("<BODY OnLoad=\"OnLoadEvent();\" >");
			httpRequest.append("<center><h1>Please do not refresh this page...</h1></center>");
			httpRequest.append("<script language=\"JavaScript\">");
			httpRequest.append("function OnLoadEvent() { ");
			httpRequest.append("window.location.assign('");
			httpRequest.append(finalRequest);
			httpRequest.append("') }");
			httpRequest.append("</script>");
			httpRequest.append("</BODY>");
			httpRequest.append("</HTML>");
			out.write(httpRequest.toString());
			logger.info("Final request sent " + httpRequest);
		} catch (IOException exception) {
			logger.error("Exception", exception);
		}
	}

	public void generateIPintRequest(Fields fields, HttpServletResponse response) {
		String finalRequest = fields.get(FieldType.IPINT_FINAL_REQUEST.getName());
		try {
			PrintWriter out = response.getWriter();

			StringBuilder httpRequest = new StringBuilder();
			httpRequest.append("<HTML>");
			httpRequest.append("<BODY OnLoad=\"OnLoadEvent();\" >");
			httpRequest.append("<center><h1>Please do not refresh this page...</h1></center>");
			httpRequest.append("<script language=\"JavaScript\">");
			httpRequest.append("function OnLoadEvent() { ");
			httpRequest.append("window.location.assign('");
			httpRequest.append(finalRequest);
			httpRequest.append("') }");
			httpRequest.append("</script>");
			httpRequest.append("</BODY>");
			httpRequest.append("</HTML>");

			out.write(httpRequest.toString());

			logger.info("Final request sent " + httpRequest);

		} catch (IOException exception) {
			logger.error("Exception", exception);
		}
	}

	public void generateAamarpayRequest(Fields fields, HttpServletResponse response) {
		String finalRequest = fields.get(FieldType.AAMARPAY_FINAL_REQUEST.getName());
		try {
			PrintWriter out = response.getWriter();

			StringBuilder httpRequest = new StringBuilder();
			httpRequest.append("<HTML>");
			httpRequest.append("<BODY OnLoad=\"OnLoadEvent();\" >");
			httpRequest.append("<center><h1>Please do not refresh this page...</h1></center>");
			httpRequest.append("<script language=\"JavaScript\">");
			httpRequest.append("function OnLoadEvent() { ");
			httpRequest.append("window.location.assign('");
			httpRequest.append(finalRequest);
			httpRequest.append("') }");
			httpRequest.append("</script>");
			httpRequest.append("</BODY>");
			httpRequest.append("</HTML>");

			out.write(httpRequest.toString());

			logger.info("Final request sent " + httpRequest);

		} catch (IOException exception) {
			logger.error("Exception", exception);
		}
	}

	public void generateFreeChargeRequest(Fields fields, HttpServletResponse response) {
		try {
			String requestURL = PropertiesManager.propertiesMap.get("FREECHARGESaleUrl");
			PrintWriter out = response.getWriter();
			StringBuilder httpRequest = new StringBuilder();
			String finalRequest = fields.get(FieldType.FREECHARGE_FINAL_REQUEST.getName());
			String finalRequestSplit[] = finalRequest.split("~");

			httpRequest.append("<HTML>");
			httpRequest.append("<BODY OnLoad=\"OnLoadEvent();\" >");
			httpRequest.append("<form name=\"form1\" action=\"");
			httpRequest.append(requestURL);
			httpRequest.append("\" method=\"post\">");

			for (String entry : finalRequestSplit) {

				String entrySplit[] = entry.split("=");

				httpRequest.append("<input type=\"hidden\" name=\"");
				httpRequest.append(entrySplit[0]);
				httpRequest.append("\" value=\"");
				httpRequest.append(entrySplit[1]);
				httpRequest.append("\">");
			}

			httpRequest.append("</form>");
			httpRequest.append("<script language=\"JavaScript\">");
			httpRequest.append("function OnLoadEvent()");
			httpRequest.append("{document.form1.submit();}");
			httpRequest.append("</script>");
			httpRequest.append("</BODY>");
			httpRequest.append("</HTML>");
			out.write(httpRequest.toString());
		} catch (Exception exception) {
			logger.error("Exception", exception);
		}
	}

	public void generateCashfreeRequest(Fields fields, HttpServletResponse response) {
		try {
			String requestURL = PropertiesManager.propertiesMap.get("CASHFREESaleUrl");
			JSONObject reqJson = new JSONObject(fields.get(FieldType.CASHFREE_FINAL_REQUEST.getName()));

			PrintWriter out = response.getWriter();
			StringBuilder httpRequest = new StringBuilder();
			httpRequest.append("<HTML>");
			httpRequest.append("<BODY OnLoad=\"OnLoadEvent();\" >");
			httpRequest.append("<center><h1>Please do not refresh this page...</h1></center>");
			httpRequest.append("<form name=\"form1\" action=\"");
			httpRequest.append(requestURL);
			httpRequest.append("\" method=\"post\">");

			for (String keys : reqJson.keySet()) {
				String row = " <input type=\"hidden\" name=\"" + keys + "\" value=\"" + reqJson.get(keys).toString()
						+ "\"/>";
				httpRequest.append(row);
			}

			httpRequest.append("</form>");
			httpRequest.append("<script language=\"JavaScript\">");
			httpRequest.append("function OnLoadEvent()");
			httpRequest.append("{document.form1.submit();}");
			httpRequest.append("</script>");
			httpRequest.append("</BODY>");
			httpRequest.append("</HTML>");

			out.write(httpRequest.toString());
		} catch (Exception exception) {
			logger.error("Exception", exception);
		}
	}

	public void generatePhonePeRequest(Fields fields, HttpServletResponse response) {
		try {
			String paymentString = fields.get(FieldType.PHONEPE_FINAL_REQUEST.getName());

			PrintWriter out = response.getWriter();
			StringBuilder httpRequest = new StringBuilder();
			httpRequest.append("<HTML>");
			httpRequest.append("<BODY OnLoad=\"OnLoadEvent();\" >");
			httpRequest.append("<center><h1>Please do not refresh this page...</h1></center>");
			httpRequest.append("<form name=\"form1\" action=\"");
			httpRequest.append(paymentString);
			httpRequest.append("\" method=\"post\">");

			httpRequest.append("</form>");
			httpRequest.append("<script language=\"JavaScript\">");
			httpRequest.append("function OnLoadEvent()");
			httpRequest.append("{document.form1.submit();}");
			httpRequest.append("</script>");
			httpRequest.append("</BODY>");
			httpRequest.append("</HTML>");
			out.write(httpRequest.toString());

		} catch (Exception exception) {
			logger.error("Exception while posting PhonePe payment request ", exception);
		}
	}

	public void generateAirPayPeRequest(Fields fields, HttpServletResponse response) {
		try {
			String requestURL = PropertiesManager.propertiesMap.get("AIRPAYSaleUrl");
			JSONObject reqJson = new JSONObject(fields.get(FieldType.AIRPAY_FINAL_REQUEST.getName()));

			PrintWriter out = response.getWriter();
			StringBuilder httpRequest = new StringBuilder();
			httpRequest.append("<HTML>");
			httpRequest.append("<BODY OnLoad=\"OnLoadEvent();\" >");
			httpRequest.append("<center><h1>Do Not Refresh or Press Back <br/> Redirecting to airpay</h1></center>");
			httpRequest.append("<form name=\"form1\" action=\"");
			httpRequest.append(requestURL);
			httpRequest.append("\" method=\"post\">");

			for (String keys : reqJson.keySet()) {
				String row = " <input type=\"hidden\" name=\"" + keys + "\" value=\"" + reqJson.get(keys).toString()
						+ "\"/>";
				httpRequest.append(row);
			}

			httpRequest.append("</form>");
			httpRequest.append("<script language=\"JavaScript\">");
			httpRequest.append("function OnLoadEvent()");
			httpRequest.append("{document.form1.submit();}");
			httpRequest.append("</script>");
			httpRequest.append("</BODY>");
			httpRequest.append("</HTML>");
			out.write(httpRequest.toString());

		} catch (Exception exception) {
			logger.error("Exception while posting AirPAy payment request ", exception);
		}
	}
	
	
	public void generateQaicashRequest(Fields fields, HttpServletResponse response) {
		try {
			String paymentString = null;
			paymentString = fields.get(FieldType.QAICASH_FINAL_REQUEST.getName());

			StringBuilder httpRequest = new StringBuilder();
			httpRequest.append("<HTML>");
			httpRequest.append("<BODY OnLoad=\"OnLoadEvent();\" >");
			httpRequest.append("<center><h1>Please do not refresh this page...</h1></center>");
			httpRequest.append("<script language=\"JavaScript\">");
			httpRequest.append("function OnLoadEvent()");
			httpRequest.append("{window.location= '" + paymentString + "';}");
			httpRequest.append("</script>");
			httpRequest.append("</BODY>");
			httpRequest.append("</HTML>");

			PrintWriter out = response.getWriter();
			out.write(httpRequest.toString());

		} catch (Exception exception) {
			logger.error("Exception while posting Qaicash payment request ", exception);
		}
	}
	
	public void generateGlobalpayRequest(Fields fields, HttpServletResponse response) {
		try {
			String paymentString = null;
			paymentString = fields.get(FieldType.GLOBALPAY_FINAL_REQUEST.getName());

			StringBuilder httpRequest = new StringBuilder();
			httpRequest.append("<HTML>");
			httpRequest.append("<BODY OnLoad=\"OnLoadEvent();\" >");
			httpRequest.append("<center><h1>Please do not refresh this page...</h1></center>");
			httpRequest.append("<script language=\"JavaScript\">");
			httpRequest.append("function OnLoadEvent()");
			httpRequest.append("{window.location= '" + paymentString + "';}");
			httpRequest.append("</script>");
			httpRequest.append("</BODY>");
			httpRequest.append("</HTML>");

			PrintWriter out = response.getWriter();
			out.write(httpRequest.toString());

		} catch (Exception exception) {
			logger.error("Exception while posting Qaicash payment request ", exception);
		}
	}
	
	public void generateGrezpayRequest(Fields fields, HttpServletResponse response) {
		try {
			String paymentString = null;
			paymentString = fields.get(FieldType.GREZPAY_FINAL_REQUEST.getName());

			StringBuilder httpRequest = new StringBuilder();
			httpRequest.append("<HTML>");
			httpRequest.append("<BODY OnLoad=\"OnLoadEvent();\" >");
			httpRequest.append("<center><h1>Please do not refresh this page...</h1></center>");
			httpRequest.append("<script language=\"JavaScript\">");
			httpRequest.append("function OnLoadEvent()");
			httpRequest.append("{window.location= '" + paymentString + "';}");
			httpRequest.append("</script>");
			httpRequest.append("</BODY>");
			httpRequest.append("</HTML>");

			PrintWriter out = response.getWriter();
			out.write(httpRequest.toString());

		} catch (Exception exception) {
			logger.error("Exception while posting Grezpay payment request ", exception);
		}
	}
	
	public void generateUpigatewayRequest(Fields fields, HttpServletResponse response) {
		try {
			String paymentString = null;
			paymentString = fields.get(FieldType.UPIGATEWAY_FINAL_REQUEST.getName());

			StringBuilder httpRequest = new StringBuilder();
			httpRequest.append("<HTML>");
			httpRequest.append("<BODY OnLoad=\"OnLoadEvent();\" >");
			httpRequest.append("<center><h1>Please do not refresh this page...</h1></center>");
			httpRequest.append("<script language=\"JavaScript\">");
			httpRequest.append("function OnLoadEvent()");
			httpRequest.append("{window.location= '" + paymentString + "';}");
			httpRequest.append("</script>");
			httpRequest.append("</BODY>");
			httpRequest.append("</HTML>");

			PrintWriter out = response.getWriter();
			out.write(httpRequest.toString());

		} catch (Exception exception) {
			logger.error("Exception while posting Grezpay payment request ", exception);
		}
	}
	
	public void generateFloxypayRequest(Fields fields, HttpServletResponse response) {
		try {
			String paymentString = null;
			paymentString = fields.get(FieldType.FLOXYPAY_FINAL_REQUEST.getName());

			StringBuilder httpRequest = new StringBuilder();
			httpRequest.append("<HTML>");
			httpRequest.append("<BODY OnLoad=\"OnLoadEvent();\" >");
			httpRequest.append("<center><h1>Please do not refresh this page...</h1></center>");
			httpRequest.append("<script language=\"JavaScript\">");
			httpRequest.append("function OnLoadEvent()");
			httpRequest.append("{window.location= '" + paymentString + "';}");
			httpRequest.append("</script>");
			httpRequest.append("</BODY>");
			httpRequest.append("</HTML>");

			PrintWriter out = response.getWriter();
			out.write(httpRequest.toString());

		} catch (Exception exception) {
			logger.error("Exception while posting Floxypay payment request ", exception);
		}
	}
	
	public void generateDigitalSolutionRequest(Fields fields, HttpServletResponse response) {
		try {
			String paymentString = null;
			paymentString = fields.get(FieldType.DIGITALSOLUTION_FINAL_REQUEST.getName());

			StringBuilder httpRequest = new StringBuilder();
			httpRequest.append("<HTML>");
			httpRequest.append("<BODY OnLoad=\"OnLoadEvent();\" >");
			httpRequest.append("<center><h1>Please do not refresh this page...</h1></center>");
			httpRequest.append("<script language=\"JavaScript\">");
			httpRequest.append("function OnLoadEvent()");
			httpRequest.append("{window.location= '" + paymentString + "';}");
			httpRequest.append("</script>");
			httpRequest.append("</BODY>");
			httpRequest.append("</HTML>");

			PrintWriter out = response.getWriter();
			out.write(httpRequest.toString());

		} catch (Exception exception) {
			logger.error("Exception while posting DIGITAL SOLUTION payment request ", exception);
		}
	}
	
	public void generateRazorpayRequest(Fields fields, HttpServletResponse response) {
		try {

			PrintWriter out = response.getWriter();
			String paymentString = fields.get(FieldType.RAZORPAY_FINAL_REQUEST.getName());
			out.write(paymentString);
		} catch (Exception exception) {
			logger.error("Exception", exception);
		}

	}
	
	public void generatePaygRequest(Fields fields, HttpServletResponse response) {
		try {
			String paymentString = null;
			paymentString = fields.get(FieldType.PAYG_FINAL_REQUEST.getName());

			StringBuilder httpRequest = new StringBuilder();
			httpRequest.append("<HTML>");
			httpRequest.append("<BODY OnLoad=\"OnLoadEvent();\" >");
			httpRequest.append("<center><h1>Please do not refresh this page...</h1></center>");
			httpRequest.append("<script language=\"JavaScript\">");
			httpRequest.append("function OnLoadEvent()");
			httpRequest.append("{window.location= '" + paymentString + "';}");
			httpRequest.append("</script>");
			httpRequest.append("</BODY>");
			httpRequest.append("</HTML>");

			PrintWriter out = response.getWriter();
			out.write(httpRequest.toString());
		} catch (Exception exception) {
			logger.error("Exception while posting PAYG payment request ", exception);
		}
	}

	public String generateZaakpayRequest(Fields fields, HttpServletResponse response) {
		StringBuilder httpRequest = new StringBuilder();
		String saleUrl = propertiesManager.propertiesMap.get("ZAAKAPAYSaleUrl");
		try {
			PrintWriter out = response.getWriter();

			String finalRequest = fields.get(FieldType.ZAAKPAY_FINAL_REQUEST.getName());
			JSONObject paymentJson = new JSONObject(finalRequest);
			Iterator<String> keys = paymentJson.keys();

			httpRequest.append("<HTML>");
			httpRequest.append("<BODY OnLoad=\"OnLoadEvent();\" >");
			httpRequest.append("<center><h1>Please do not refresh this page...</h1></center>");
			httpRequest.append("<form name=\"form1\" action=\"");
			httpRequest.append(saleUrl);
			httpRequest.append("\" method=\"post\">");

			while (keys.hasNext()) {
				String key = keys.next();
				httpRequest.append("<input type=\"hidden\" name=\"");
				httpRequest.append(key);
				httpRequest.append("\" value=\"");
				httpRequest.append(paymentJson.get(key).toString());
				httpRequest.append("\">");

			}

			httpRequest.append("</form>");
			httpRequest.append("<script language=\"JavaScript\">");
			httpRequest.append("function OnLoadEvent()");
			httpRequest.append("{document.form1.submit();}");
			httpRequest.append("</script>");
			httpRequest.append("</BODY>");
			httpRequest.append("</HTML>");
			out.write(httpRequest.toString());
		} catch (Exception exception) {
			logger.error("Exception", exception);
		}
		return httpRequest.toString();
	}
}
