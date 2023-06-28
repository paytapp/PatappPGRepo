package com.paymentgateway.pg.ws;

import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class TestISGPay {
	private static String encdata = "LrKu3eo5sqTmgR+/phOcFNhieRK8JsiQFWz0M+2ruljcwP7pzTmP8clslpHee55hoqKZMFM+/IInkzA9xSk94LZkPWW9RugBvoRv4TDraUu5Is2raeUdYAYnKA/RiV7rhvSgGqd3KQZKFDKK82+Eb8vZYUbup/VyIXr/PCfW3DryW/k6mbDHnt5yG/vGEBc8dX1dF9EvJw+xqu6RcurbC3Cf5n0STjQnsZ0aTj52739VYahHB0orzb/C7CcvF/H7duEa8pOUiX1I0JjDcru3lotucDFpchA2G/R5u1gH4A7mJhd9QO+jW1r47lkFW0ksu00ok4c7bNIU1slE4pMkDWu6t6GLenZgTlxhZIFI8ZEDN28X+P0KSJiZAkGUEhFjn9k7JATMEAl9FyuJNCT195/y59WgUC8U8ZOmvxCRoQEau6APBQibQRqifg8H+Nq+Pp6qFqmeJEEiG0U/JMyoiso9a0RViY4jbcLTaZL+fpWtxznSD17pru2DsBvKVoRg";

	private static String url = "https://sandbox.isgpay.com/ISGPay-Genius/request.action";

	public static void main(String[] args) throws IOException {

		JSONObject json = new JSONObject();
		json.put("merchantId", "120000000001581");
		json.put("terminalId", "11001581");
		json.put("bankId", "000004");
		json.put("version", "1");
		json.put("encData", encdata);

		String response = connection(json, url);

//		Map<String, String> dataResponse = new HashMap<String, String>();
//		dataResponse = getFormResponse(response);

//		String encData
//		String merchantId
//		String terminalId
//		String bankId 

		System.out.println("REsponse getting from ISGPAY >>> " + response);

	}

	public static String connection(JSONObject data, String url) throws IOException {

		OkHttpClient client = new OkHttpClient().newBuilder().build();
		MultipartBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
				.addFormDataPart("MerchantId", data.getString("merchantId"))
				.addFormDataPart("TerminalId", data.getString("terminalId"))
				.addFormDataPart("BankId", data.getString("bankId"))
				.addFormDataPart("Version", data.getString("version"))
				.addFormDataPart("EncData", data.getString("encData")).build();

		Request request = new Request.Builder().url(url).method("POST", body).build();
		Response response = client.newCall(request).execute();
		String connectionResponse = response.body().string();

		if (StringUtils.isNotBlank(connectionResponse)) {
			return connectionResponse;
		} else {
			return null;
		}
	}

//	public static Map<String, String> getFormResponse(String responseData) {
//		Map<String, String> data = new HashMap<String, String>();
//
//	}

}
