package com.paymentgateway.notification.sms.controller;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.paymentgateway.notification.sms.service.DashboardDataService;

@RestController
public class DasboardDataControllr {

	private static Logger logger = LoggerFactory.getLogger(DasboardDataControllr.class.getName());
	
	@Autowired
	private DashboardDataService dashboardDataService;
	
	@RequestMapping(method = RequestMethod.POST, value = "/generateDashboardData")
	public @ResponseBody String updateQR(@RequestBody String reqmap) {
		int count = 0;
		try {
			if (StringUtils.isNotBlank(reqmap)) {
				String date = reqmap.split(",")[0];
				count = Integer.parseInt(reqmap.split(",")[1]);
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				Date reqDate = sdf.parse(date);
				String reqDateStr = sdf.format(reqDate);
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(sdf.parse(date));

				if (reqDateStr.equals(date)) {
					while (count >= 1) {
						dashboardDataService.generateData(reqDateStr);
						calendar.add(Calendar.DATE, -1);
						reqDateStr = sdf.format(calendar.getTime());
						count--;

					}

				} else {
					return "Date Format is not correct, it should be in yyyy-MM-dd !";
				}

			}

			return "Inserted Successfully ";
		} catch (Exception exception) {
			logger.error("Exception", exception);
			if (count == 0) {
				return "Second value should be a number";
			} else {
				return "Something went wrong ";
			}
		}

	}
}
