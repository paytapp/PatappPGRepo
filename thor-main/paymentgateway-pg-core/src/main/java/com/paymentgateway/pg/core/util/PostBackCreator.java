package com.paymentgateway.pg.core.util;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.paymentgateway.commons.util.FieldType;
import com.paymentgateway.commons.util.Fields;

/**
 * @author Puneet,Chandan
 *
 *
 *         To send asynch post to the merchant
 * 
 *
 */

@Service
public class PostBackCreator {

	private static Logger logger = LoggerFactory.getLogger(PostBackCreator.class.getName());

	// HTTP POST request
	@Async
	public void sendPostBack(Fields fields) {
		String returnUrl = fields.get(FieldType.RETURN_URL.getName());

		fields.removeInternalFields();
		PostMethod postMethod = new PostMethod(returnUrl);

		for (String key : fields.keySet()) {
			postMethod.addParameter(key, fields.get(key));
		}

		try {
			HttpClient httpClient = new HttpClient();
			httpClient.executeMethod(postMethod);
			logger.info("Postback status code " + postMethod.getStatusCode());
			// Catch all sorts of exceptions
		} catch (Exception exception) {
			logger.error("Error sending postback " , exception);
		}
	}
}
