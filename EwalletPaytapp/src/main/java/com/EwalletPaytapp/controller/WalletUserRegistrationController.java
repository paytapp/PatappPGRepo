package com.EwalletPaytapp.controller;


import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import com.EwalletPaytapp.service.WalletUserRegistrationService;

@RestController
public class WalletUserRegistrationController {
	
private static Logger logger = LoggerFactory.getLogger(WalletUserRegistrationController.class.getName());
	

@Autowired
private WalletUserRegistrationService walletUserRegistrationService;

	@RequestMapping(method = RequestMethod.POST,value = "/walletUserRegistration", consumes = MediaType.APPLICATION_JSON_VALUE)
	public String  walletUserRegistration(@RequestBody  String collJson){
		String custWaalletJson=null;
		logger.info("WalletUserRegistration controller>>> Json String----> "+ new JSONObject(collJson).get("collName").toString());
		custWaalletJson=new JSONObject(collJson).get("collName").toString();
		
		walletUserRegistrationService.AddEwalletUser(custWaalletJson);
		//encryptCustData.encryptData(new JSONObject(collJson).get("collName").toString());
		//logger.info("Encryption request completed for collection >>> " + new JSONObject(collJson).get("collName").toString());
		return "SUCCESS";
		
	}
	//PCI DSS
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
}
