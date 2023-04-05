package com.paymentgateway.api.crm;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.paymentgateway.commons.dao.UserSettingDao;


@Controller
public class CrmController {

	private static Logger logger = LoggerFactory.getLogger(CrmController.class.getName());

	@Autowired
	private UserSettingDao userSettingDao;
	
	@RequestMapping(method = RequestMethod.POST, value = "/addUserSettingfields")
	public @ResponseBody String addUserSetting() {
		
		//userSettingDao.addUserFlagsInMongoDb();
		
		userSettingDao.updatePayIdInMongo();
		
		return "Done";
	}
	
}
