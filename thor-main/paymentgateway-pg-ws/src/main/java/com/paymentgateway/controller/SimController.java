package com.paymentgateway.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONObject;
import org.json.XML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SimController {

	private static Logger logger = LoggerFactory.getLogger(SimController.class.getName());
	
	@Autowired
	MessageService messageService;
	
	@RequestMapping("enroll")
	@ResponseBody
	public String provideEnrollementUrl(HttpServletRequest request){
		String payId = null;
		try {
		Reader reader = request.getReader();
		 BufferedReader bufferReader = new BufferedReader(reader);
	     String line;
	     StringBuilder buffer = new StringBuilder();
	     try {
	            while ((line = bufferReader.readLine()) != null) {
	                buffer.append(line);
	            }
	        } catch (IOException e) {
	            logger.error("Exception in provideEnrollementUrl , exception = " , e);
	        }
		
	     String xmlString = buffer.toString();
	     JSONObject xmlJSONObj = XML.toJSONObject(xmlString);
	     payId = xmlString;
		System.out.println(buffer.toString());
		
		}
		catch (Exception e) {
			
		}
		
		return messageService.getMsg(payId);
}
}