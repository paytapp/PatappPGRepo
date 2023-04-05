package com.paymentgateway.pgui.action;

import java.io.BufferedReader;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class LyraUpiServerResponseAction {

	private static Logger logger = LoggerFactory.getLogger(LyraUpiServerResponseAction.class.getName());

	public void lyraUpiServerResponseHandler(HttpServletRequest httpRequest, HttpServletResponse httpResponse)
			throws IOException {

		try {
			BufferedReader inputBuffered = httpRequest.getReader();
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = inputBuffered.readLine()) != null) {
				response.append(inputLine);
			}
			inputBuffered.close();
			logger.info("Lyra UPI Callback Response >>> " + response.toString());
		} catch (Exception e) {
			logger.error("Error in Lyra UPI callback = ", e);
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
