package com.paymentgateway.api.controller;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.paymentgateway.api.utils.EncryptCustData;
import com.paymentgateway.api.utils.UpDateDBRecords;

@RestController
public class CustomerDataEncController {

	@Autowired
	private EncryptCustData encryptCustData;
	
	@Autowired
	private UpDateDBRecords dateDBRecords;
	
	private static Logger logger = LoggerFactory.getLogger(CustomerDataEncController.class.getName());
	
	@RequestMapping(method = RequestMethod.POST,value = "/encCustData", consumes = MediaType.APPLICATION_JSON_VALUE)
	public String  sendPromoSMS(@RequestBody  String collJson){
		
		logger.info("Encryption request received for collection >>> " + new JSONObject(collJson).get("collName").toString());
		
		encryptCustData.encryptData(new JSONObject(collJson).get("collName").toString());
		
		logger.info("Encryption request completed for collection >>> " + new JSONObject(collJson).get("collName").toString());
		return "SUCCESS";
		
	}
	
	@RequestMapping(method = RequestMethod.POST,value = "/decCustData", consumes = MediaType.APPLICATION_JSON_VALUE)
	public String  decCustData(@RequestBody  String collJson){
		
		logger.info("Decryption request received for collection >>> " + new JSONObject(collJson).get("collName").toString());
		
		encryptCustData.decryptData(new JSONObject(collJson).get("collName").toString());
		
		logger.info("Decryption request completed for collection >>> " + new JSONObject(collJson).get("collName").toString());
		return "SUCCESS";
		
	}
	
	@RequestMapping(method = RequestMethod.POST,value = "/updateDBTablesRecords", consumes = MediaType.APPLICATION_JSON_VALUE)
	public String  updateDBTablesRecords(@RequestBody  String collJson){
		JSONObject json = new JSONObject(collJson);
		logger.info("Update db records request received for update start >>> " + json.toString());
		
		if(json.has("mongo") && json.getString("mongo").equalsIgnoreCase("y")) {
			dateDBRecords.upDateMongoRecords("customerQR", "CUSTOMER_ACCOUNT_NO");
		}
		if(json.has("sql") && json.getString("sql").equalsIgnoreCase("y")) {
			dateDBRecords.upDateSQLRecords("users","virtualAccountNo");
		}
		
		logger.info("Update db records request received for update end >>> ");
		return "SUCCESS";
		
	}
	
	
}
