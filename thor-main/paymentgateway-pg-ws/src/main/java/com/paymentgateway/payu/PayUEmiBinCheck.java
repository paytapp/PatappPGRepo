package com.paymentgateway.payu;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.exception.SystemException;
import com.paymentgateway.commons.util.PropertiesManager;
import com.paymentgateway.pg.core.util.PayuUtil;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Service
public class PayUEmiBinCheck {

	private static Logger logger = LoggerFactory.getLogger(PayUEmiBinCheck.class.getName());

	public Map<String, String> getResponse(Map<String, String> reqMap) throws SystemException {
		String hostUrl = "";
		Map<String, String> responseMap = new HashMap<String, String>();

		try {

			hostUrl = PropertiesManager.propertiesMap.get(Constants.PAYU_EMI_BIN_CHECK__URL);
			URL url = new URL(hostUrl);

			// "key=H16ki6&command=eligibleBinsForEMI&var1=bin&var2=434668&hash=\"
			String request = "key=" + PropertiesManager.propertiesMap.get("PAYUEmiBinkey") + "&"
					+ "command=eligibleBinsForEMI&var1=bin&var2=" + reqMap.get("cardBin") + "&hash="
					+ PayuUtil.payuEmiBinEnqHash("eligibleBinsForEMI", "bin",
							PropertiesManager.propertiesMap.get("PAYUEmiBinkey"),
							PropertiesManager.propertiesMap.get("PAYUEmiBinSalt"));

			logger.info("Request for Payu Emi Bin  >> " + request);

			OkHttpClient client = new OkHttpClient().newBuilder().build();
			MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
			RequestBody body = RequestBody.create(mediaType, request);
			Request requestAirPay = new Request.Builder().url(hostUrl).method("POST", body)
					.addHeader("Content-Type", "application/x-www-form-urlencoded").build();
			Response responsepayuEmiBinPay = client.newCall(requestAirPay).execute();
			String emiBinResponse = responsepayuEmiBinPay.body().string();

			logger.info("Response for Payu Emi Bin  >> " + emiBinResponse);

			responseMap = emiBinResoinseHandle(emiBinResponse, reqMap.get("txnAmount"), reqMap.get("bankName"));
		} catch (Exception e) {
			logger.error("Exception in Payu emiBinRequest ", e);
			responseMap.put("status", "fail");
		}
		return responseMap;
	}

	public static Map<String, String> emiBinResoinseHandle(String responeAPi, String amount, String bankName) {
		Map<String, String> responseMap = new HashMap<String, String>();
		try {

			// PayuNBMopType.getBankCodeByBankName(fields.get(FieldType.CARD_INFO.getName()));

			JSONObject responeJson = new JSONObject(responeAPi);
			if (responeJson.has("status") && responeJson.getInt("status") != 0 && responeJson.has("details")) {
				JSONObject details = responeJson.getJSONObject("details");
				if (details.has("minAmount")) {
					if (Double.valueOf(amount) >= Double.valueOf(details.getInt("minAmount"))) {
						responseMap.put("status", "success");
						responseMap.put("bank", details.getString("bank"));
						responseMap.put("msg", responeJson.getString("msg"));
						if (details.getString("bank").contains(bankName)) {
							responseMap.put("bankNameMsg", "match");
						} else {
							responseMap.put("bankNameMsg", "not match");
						}
					} else {
						// amount validation
						responseMap.put("status", "fail");
						responseMap.put("msg", "Emi for minimum amount " + details.getInt("minAmount"));
					}
				} else {
					// response in not minimum amount
					responseMap.put("status", "fail");
					responseMap.put("msg", "Something went wrong!");
				}

			} else {
				// api response fail
				responseMap.put("status", "fail");
				responseMap.put("msg", "Something went wrong!");
			}

		} catch (Exception e) {
			logger.error("Exception in payuEmiBin response handle ", e);
			responseMap.put("status", "fail");
			responseMap.put("msg", "Something went wrong!");
		}
		return responseMap;

	}

}
