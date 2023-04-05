package com.paymentgateway.pgui.action;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.api.Hasher;
import com.paymentgateway.commons.dao.UserSettingDao;
import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.user.User;
import com.paymentgateway.commons.user.UserDao;
import com.paymentgateway.commons.user.UserSettingData;
import com.paymentgateway.commons.util.Amount;
import com.paymentgateway.commons.util.Base64EncodeDecode;
import com.paymentgateway.commons.util.ConfigurationConstants;
import com.paymentgateway.commons.util.Constants;
import com.paymentgateway.commons.util.CrmFieldConstants;
import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.PropertiesManager;

/**
 * Alam, sandeep
 */

@Service
public class StaticPgQrPay {
	private static Logger logger = LoggerFactory.getLogger(StaticPgQrPay.class.getName());

	@Autowired
	private UserDao userDao;

	@Autowired
	private Base64EncodeDecode base64EncodeDecode;

	@Autowired
	private Hasher hasher;

	@Autowired
	private PropertiesManager propertiesManager;

	@Autowired
	private UserSettingDao userSettingDao;

	@SuppressWarnings("static-access")
	public Map<String, String> staticPgQrPayRequestHandling(HttpServletRequest request, HttpServletResponse response)
			throws IOException {

		Map<String, String> responseMap = new HashMap<String, String>();
		try {

			User user = userDao.findPayId(request.getParameter("id"));
			UserSettingData merchantSettings = userSettingDao.fetchDataUsingPayId(user.getPayId());

			if (user != null && user.isLogoFlag()) {
				responseMap.put("merchantLogo", getBase64LogoPerMerchant(merchantSettings));
			} else {
				responseMap.put("merchantLogo", "");
			}
			String paytensePayId = propertiesManager.propertiesMap.get("PAYTENSE_PAY_ID");

			responseMap.put("PAYTENSE_PAY_ID", paytensePayId);
			if (paytensePayId.contains(user.getPayId())) {
				responseMap.put("returnUrl", propertiesManager.propertiesMap.get("PAYTENSE_RETURN_URL"));
			} else {
				responseMap.put("returnUrl",
						propertiesManager.propertiesMap.get(CrmFieldConstants.STATIC_PGQR_RETURN_URL.getValue()));
			}
			responseMap.put("businessName", user.getBusinessName());
			responseMap.put("payId", user.getPayId());

		} catch (Exception exception) {
			logger.error("Exception", exception);
			String path = request.getContextPath();
			logger.info(path);
			if (!StringUtils.isNotBlank(path) && !path.contains("pgui")) {
				String resultPath = request.getScheme() + "://" + request.getHeader("Host") + "/pgui/jsp/error";
				response.sendRedirect(resultPath);
			}
			response.sendRedirect("error");
		}
		return responseMap;
	}

	@SuppressWarnings("static-access")
	public void redirectToPayment(HttpServletRequest request, HttpServletResponse response) throws IOException {
		Map<String, String> fieldsMap = new TreeMap<String, String>();
		try {
			PrintWriter out = response.getWriter();

			fieldsMap.put(FieldType.PAY_ID.getName(), request.getParameter("payId"));
			fieldsMap.put(FieldType.ORDER_ID.getName(), request.getParameter("orderId"));
			fieldsMap.put(FieldType.AMOUNT.getName(), Amount.formatAmount(request.getParameter("amount"), "356"));
			fieldsMap.put(FieldType.TXNTYPE.getName(), "SALE");
			fieldsMap.put(FieldType.CUST_NAME.getName(), request.getParameter("name"));
			fieldsMap.put(FieldType.CUST_EMAIL.getName(), request.getParameter("email"));
			fieldsMap.put(FieldType.CUST_PHONE.getName(), request.getParameter("mobile"));
			fieldsMap.put(FieldType.CURRENCY_CODE.getName(), "356");
			String paytensePayId = propertiesManager.propertiesMap.get("PAYTENSE_PAY_ID");
			if (paytensePayId.contains(request.getParameter("payId"))) {
				fieldsMap.put(FieldType.RETURN_URL.getName(),
						propertiesManager.propertiesMap.get("PAYTENSE_RETURN_URL"));
			} else {
				fieldsMap.put(FieldType.RETURN_URL.getName(),
						propertiesManager.propertiesMap.get(CrmFieldConstants.STATIC_PGQR_RETURN_URL.getValue()));
			}

			String hash = creatHash(fieldsMap);
			fieldsMap.put(FieldType.HASH.getName(), hash);
			fieldsMap.put(FieldType.REQUEST_URL.getName(),
					propertiesManager.propertiesMap.get(CrmFieldConstants.STATIC_PGQR_REQUEST_URL.getValue()));
			String finalResponse = sendFormPost(fieldsMap);

			logger.info("final response sent " + finalResponse);
			out.write(finalResponse);
			out.flush();
			out.close();

		} catch (Exception exception) {
			logger.error("Exception Cought while redirecting PgQr Txn to payment page : ", exception);
			String path = request.getContextPath();
			logger.info(path);
			if (!StringUtils.isNotBlank(path) && !path.contains("pgui")) {
				String resultPath = request.getScheme() + "://" + request.getHeader("Host") + "/pgui/jsp/error";
				response.sendRedirect(resultPath);
			}
			response.sendRedirect("error");
		}
	}

	@SuppressWarnings("static-access")
	private String creatHash(Map<String, String> fieldsMap) throws SystemException {

		StringBuilder allFieldsBuilder = new StringBuilder();
		Map<String, String> sortedMap = new TreeMap<String, String>(fieldsMap);
		for (String key : sortedMap.keySet()) {
			allFieldsBuilder.append(ConfigurationConstants.FIELD_SEPARATOR.getValue());
			allFieldsBuilder.append(key);
			allFieldsBuilder.append(ConfigurationConstants.FIELD_EQUATOR.getValue());
			allFieldsBuilder.append(sortedMap.get(key));
		}
		String salt = (new PropertiesManager()).getSalt(fieldsMap.get(FieldType.PAY_ID.getName()));
		allFieldsBuilder.deleteCharAt(0);
		allFieldsBuilder.append(salt);
		return hasher.getHash(allFieldsBuilder.toString());
	}

	private String sendFormPost(Map<String, String> fieldsMap) {

		String returnUrl = fieldsMap.get(FieldType.REQUEST_URL.getName());
		fieldsMap.remove(FieldType.REQUEST_URL.getName());

		StringBuilder httpRequest = new StringBuilder();
		httpRequest.append("<HTML>");
		httpRequest.append("<BODY OnLoad=\"OnLoadEvent();\" >");
		httpRequest.append("<form name=\"form1\" action=\"");
		httpRequest.append(returnUrl);
		httpRequest.append("\" method=\"post\">");
		for (String key : fieldsMap.keySet()) {
			httpRequest.append("<input type=\"hidden\" name=\"");
			httpRequest.append(key);
			httpRequest.append("\" value=\"");
			httpRequest.append(fieldsMap.get(key));
			httpRequest.append("\">");
		}
		httpRequest.append("</form>");
		httpRequest.append("<script language=\"JavaScript\">");
		httpRequest.append("function OnLoadEvent()");
		httpRequest.append("{document.form1.submit();}");
		httpRequest.append("</script>");
		httpRequest.append("</BODY>");
		httpRequest.append("</HTML>");
		return httpRequest.toString();

	}

	public String getBase64LogoPerMerchant(UserSettingData merchantSettings) {
		String base64File = "";
		StringBuilder base64EncodeImage = new StringBuilder();
		String finalLogoLocation = null;
		File imageLocation = null;

		try {

			if (merchantSettings.isLogoFlag()) {
				imageLocation = new File(
						PropertiesManager.propertiesMap.get(Constants.LOGO_FILE_UPLOAD_LOCATION.getValue()) + "//"
								+ merchantSettings.getPayId(),
						merchantSettings.getPayId() + ".png");

				if (!imageLocation.exists()) {
					logger.info("no such a directory for merchant logo ");
				} else {

					finalLogoLocation = imageLocation.toString();
					if (finalLogoLocation.contains(".png")) {
						base64EncodeImage.append("data:image/png;base64,");
						base64EncodeImage.append(base64EncodeDecode.base64Encoder(new File(finalLogoLocation)));

					} else {

						finalLogoLocation = imageLocation.toString();
						if (finalLogoLocation.contains(".png")) {
							base64EncodeImage.append("data:image/png;base64,");
							base64EncodeImage.append(base64EncodeDecode.base64Encoder(new File(finalLogoLocation)));
						} else {
							base64EncodeImage.append("data:image/jpg;base64,");
							base64EncodeImage.append(base64EncodeDecode.base64Encoder(new File(finalLogoLocation)));
						}

						// logger.info(base64EncodeImage.toString());
						base64File = base64EncodeImage.toString();
					}
				}
			}
		} catch (Exception e) {
			logger.error("Exception caught while encoding into Base64, " + e);
			return "";
		}
		return base64File;
	}
}
