
package com.paymentgateway.pg.service;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class IPManagerUtil {

	private static Logger logger = LoggerFactory.getLogger(IPManagerUtil.class.getName());

	public String getIpAndPort(HttpServletRequest request) {
		String ipAndPort = "IP: Unable to extract and Port: Unable to extract";
		try {
			String ip = request.getHeader("X-Forwarded-For");
			String port = request.getHeader("x-forwarded-port");
			String ipArray[] = null;

			if (ip != null && ip.contains(",")) {
				ipArray = ip.split(",");
				ip = ipArray[0];
			}

			if (ip == null) {
				ip = request.getRemoteAddr();
			}

			ipAndPort = "IP: " + ip + ", Port: " + port + ", Header value: " + request.getHeader("User-Agent");

		} catch (Exception e) {

		}

		return ipAndPort;
	}

	public String getIp(HttpServletRequest request) {
		String ipAndPort = "IP: Unable to extract";
		String ip = null;
		try {
			ip = request.getHeader("X-Forwarded-For");

			if (ip == null) {
				ip = request.getRemoteAddr();
			}

			ipAndPort = "IP: " + ip + ", Header value: " + request.getHeader("User-Agent");
			logger.info(ipAndPort);
		} catch (Exception e) {

		}

		return ip;
	}
}
